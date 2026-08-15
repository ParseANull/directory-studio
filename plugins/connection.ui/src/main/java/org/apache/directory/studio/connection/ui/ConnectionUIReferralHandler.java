/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.apache.directory.studio.connection.ui;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.IReferralHandler;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateAdapter;
import org.apache.directory.studio.connection.ui.dialogs.SelectReferralConnectionDialog;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ConnectionUIReferralHandler — C-3PO ROUTING THE "GO TO ANOTHER OUTPOST" MESSAGE ──
// When the LDAP server returns a REFERRAL result code, it's essentially saying
// "we don't have what you need; try this other server instead."  C-3PO's job is
// to figure out which outpost to redirect to and to handle the diplomatic routing.
// This class is C-3PO for referrals:
//   1. We check a URL → Connection cache first.  If we've already resolved a
//      referral to this URL before, we return the cached Connection (avoiding
//      the dialog for repeat visits).
//   2. If the connection was removed from the manager, we evict the stale cache
//      entry.
//   3. If the cache misses, we open SelectReferralConnectionDialog on the SWT UI
//      thread and let the user pick which saved connection to use.
//   4. We store the user's choice back in the cache for next time.
// We also extend ConnectionUpdateAdapter to listen for connectionClosed events:
// when any connection closes, we clear the whole cache because closed connections
// can't serve referrals any more.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Production implementation of {@link IReferralHandler} that prompts the user
 * via {@link SelectReferralConnectionDialog} when a referral needs to be resolved.
 *
 * <p>We maintain a URL-to-{@link Connection} cache (normalised LDAP URL string as key)
 * so repeated referrals to the same host skip the dialog.  The cache is cleared
 * whenever any connection is closed (via {@link ConnectionUpdateAdapter#connectionClosed})
 * to avoid stale entries.</p>
 *
 * <p>Registered with {@link ConnectionCorePlugin} by {@link ConnectionUIPlugin#start}.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionUIReferralHandler extends ConnectionUpdateAdapter implements IReferralHandler
{
    /**
     * Cache mapping normalised referral URL strings to the {@link Connection}
     * the user chose to service them.
     * Concurrent because the cache may be read from a background LDAP job thread
     * while the UI thread clears it on connection-close.
     */
    private Map<String, Connection> referralUrlToReferralConnectionCache = new ConcurrentHashMap<>();


    // ── CONSTRUCTOR — REGISTER FOR CONNECTION CLOSE EVENTS ────────────────────────
    /**
     * Creates the handler and registers it as a connection update listener so we
     * can clear the cache whenever a connection is closed.
     */
    public ConnectionUIReferralHandler()
    {
        super();
        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionCorePlugin.getDefault().getEventRunner() );
    }


    // ── CONNECTION CLOSED — CLEAR THE REFERRAL CACHE ──────────────────────────────
    // A closed connection can no longer service referrals, so we nuke the whole
    // cache.  We'll re-populate it on demand if needed.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Clears the referral URL cache when any connection is closed.
     */
    @Override
    public void connectionClosed( Connection connection )
    {
        referralUrlToReferralConnectionCache.clear();
    }


    // ── GET REFERRAL CONNECTION — RESOLVE A REFERRAL URL TO A CONNECTION ──────────
    // Step 1: check the cache (with stale-entry eviction).
    // Step 2: open the dialog on the UI thread.
    // Step 3: store the choice back in the cache.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns the {@link Connection} to use for the given list of referral URLs.
     *
     * <p>Checks the normalised-URL cache first.  If the cached connection is no
     * longer in the connection manager (it was deleted), the stale entry is evicted
     * and we fall through to the dialog.  The dialog is shown on the SWT UI thread
     * via {@link org.eclipse.swt.widgets.Display#syncExec}.</p>
     *
     * @param referralUrls  The list of LDAP referral URL strings from the server response.
     * @return  The {@link Connection} the user selected, or {@code null} if the user
     *          cancelled the dialog.
     */
    public Connection getReferralConnection( final List<String> referralUrls )
    {
        final Connection[] referralConnections = new Connection[1];

        try
        {
            // ── STEP 1: CHECK THE CACHE ────────────────────────────────────────────
            // Normalise each URL and see if we have a cached entry for it.
            // If the cached connection is no longer in the manager, evict it.
            // ──────────────────────────────────────────────────────────────────────
            for ( String url : referralUrls )
            {
                String normalizedUrl = Utils.getSimpleNormalizedUrl( new LdapUrl( url ) );

                if ( referralUrlToReferralConnectionCache.containsKey( normalizedUrl ) )
                {
                    // check if referral connection still exists in connection manager
                    Connection referralConnection = referralUrlToReferralConnectionCache.get( normalizedUrl );
                    Connection[] connections = ConnectionCorePlugin.getDefault().getConnectionManager()
                        .getConnections();

                    for ( Connection connection : connections )
                    {
                        if ( connection.equals( referralConnection ) )
                        {
                            return referralConnection;
                        }
                    }

                    // referral connection was removed from the manager — evict the stale cache entry
                    referralUrlToReferralConnectionCache.remove( normalizedUrl );
                }
            }

            // ── STEP 2: OPEN THE DIALOG ON THE UI THREAD ──────────────────────────
            // syncExec blocks the caller (the LDAP background thread) until the
            // user makes a choice in the dialog.
            // ──────────────────────────────────────────────────────────────────────
            PlatformUI.getWorkbench().getDisplay().syncExec( () ->
            {
                SelectReferralConnectionDialog dialog = new SelectReferralConnectionDialog( PlatformUI
                    .getWorkbench()
                    .getDisplay().getActiveShell(), referralUrls );

                if ( dialog.open() == SelectReferralConnectionDialog.OK )
                {
                    Connection connection = dialog.getReferralConnection();
                    referralConnections[0] = connection;
                }
            } );

            // ── STEP 3: CACHE THE CHOICE ───────────────────────────────────────────
            // Store the chosen connection under every referral URL variant so that
            // future referrals to the same server skip the dialog.
            // ──────────────────────────────────────────────────────────────────────
            if ( referralConnections[0] != null )
            {
                for ( String url : referralUrls )
                {
                    String normalizedUrl = Utils.getSimpleNormalizedUrl( new LdapUrl( url ) );
                    referralUrlToReferralConnectionCache.put( normalizedUrl, referralConnections[0] );
                }
            }
        }
        catch ( LdapURLEncodingException luee )
        {
            // Will never occur — the referral URLs come from the LDAP server and are
            // already well-formed.
        }

        return referralConnections[0];
    }
}
