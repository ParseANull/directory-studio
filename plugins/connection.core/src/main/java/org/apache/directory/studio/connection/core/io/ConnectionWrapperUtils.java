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
package org.apache.directory.studio.connection.core.io;


import java.util.ArrayList;

import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.connection.core.IReferralHandler;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;


// ── CLASS: ConnectionWrapperUtils — C-3PO'S REFERRAL ROUTING HELPER ───────────
// When C-3PO gets a referral, he doesn't just hand the user a new address —
// he also makes sure the target ship (connection) is actually running before
// trying to talk to it.  If it's not running, he fires it up first.
// This utility class handles exactly that: it uses the IReferralHandler
// to ask the user which connection to use, then ensures that connection
// is open and authenticated before returning it to the caller.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for {@link ConnectionWrapper} implementations.
 * Currently provides one helper: {@link #getReferralConnection}, which
 * resolves an LDAP referral to a live, authenticated {@link Connection}.
 * The logic is non-trivial: we ask the {@link IReferralHandler} for the
 * preferred connection, then connect and bind it if it isn't already open,
 * and fire the connection-opened event so UI components stay in sync.
 * Think of this as C-3PO's referral routing sub-routine: he figures out which
 * ship to redirect to, wakes it up if necessary, and hands it to the caller.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionWrapperUtils
{
    // ── GET REFERRAL CONNECTION — RESOLVE A REFERRAL TO A LIVE CONNECTION ──────────
    // C-3PO receives the referral URL list, asks the handler which connection to use,
    // then wakes it up (connect + bind + fire events) if it's not already running.
    // Returns null if no handler is registered or the user cancelled.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resolves an LDAP referral to a live, authenticated {@link Connection}.
     * We ask the registered {@link IReferralHandler} which saved connection to route
     * through.  If the chosen connection is not yet open, we connect and bind it,
     * then notify all {@link IConnectionListener}s and fire the
     * {@link ConnectionEventRegistry} event so the UI updates.
     * Returns {@code null} if no referral handler is registered or the user
     * cancelled the selection dialog.
     *
     * @param referral  The LDAP referral returned by the server, containing the
     *                  list of alternative URLs to try.
     * @param monitor   Progress monitor for the connect/bind operations.
     * @param source    The source object for the {@link ConnectionEventRegistry} event.
     * @return  A live, authenticated {@link Connection}, or {@code null} if unavailable.
     */
    public static Connection getReferralConnection( Referral referral, StudioProgressMonitor monitor, Object source )
    {
        Connection referralConnection = null;
        IReferralHandler referralHandler = ConnectionCorePlugin.getDefault().getReferralHandler();
        if ( referralHandler != null )
        {
            referralConnection = referralHandler
                .getReferralConnection( new ArrayList<String>( referral.getLdapUrls() ) );

            // open connection if not yet open
            if ( referralConnection != null && !referralConnection.getConnectionWrapper().isConnected() )
            {
                referralConnection.getConnectionWrapper().connect( monitor );
                referralConnection.getConnectionWrapper().bind( monitor );
                for ( IConnectionListener listener : ConnectionCorePlugin.getDefault().getConnectionListeners() )
                {
                    listener.connectionOpened( referralConnection, monitor );
                }
                ConnectionEventRegistry.fireConnectionOpened( referralConnection, source );
            }
        }
        return referralConnection;
    }
}
