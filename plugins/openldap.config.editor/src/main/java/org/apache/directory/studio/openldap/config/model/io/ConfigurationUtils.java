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
package org.apache.directory.studio.openldap.config.model.io;


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeRootDSERunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;


// ── CLASS: ConfigurationUtils — C-3PO Smoothing the Path ─────────────────────
// Before R2 can download or upload the Death Star plans, C-3PO has to figure out
// where the Imperial network terminal is, open the connection, and authenticate —
// all the diplomatic legwork that makes R2's mission possible.
// ConfigurationUtils is exactly that: a collection of utility methods that locate
// the cn=config DN, open LDAP connections, and provide a safe default when the
// server doesn't advertise its own config location.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility helpers used by the configuration I/O layer.
 * Handles locating the cn=config base DN (by querying the Root DSE or falling back
 * to the well-known "cn=config"), and opening/binding an LDAP connection.
 * Think of this as C-3PO smoothing the diplomatic path before R2 gets to work.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConfigurationUtils
{
    private ConfigurationUtils()
    {
        // Nothing to do
    }


    /** The default OpenLDAP configuration DN */
    public static final String DEFAULT_CONFIG_DN = "cn=config";


    // ── getConfigurationDn — C-3PO Locates the Imperial Terminal ─────────────────
    // C-3PO checks the Root DSE of the station to find where the main configuration
    // terminal is — the configcontext attribute advertises the exact DN.
    // If it's missing, he falls back to the well-known "cn=config" address.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the base DN for the cn=config tree on the connected server.
     * We first try the 'configcontext' attribute in the Root DSE (which OpenLDAP
     * advertises); if that's absent we fall back to the default "cn=config" DN.
     *
     * <p>For example — C-3PO locates the configuration terminal:</p>
     * <pre>
     *   IBrowserConnection conn = ...;
     *   Dn configDn = ConfigurationUtils.getConfigurationDn( conn );
     *   // typically cn=config or the value from configcontext
     * </pre>
     *
     * @param browserConnection  the Studio browser connection wrapping the live LDAP connection
     * @return                   the base DN for the cn=config tree
     * @throws ConfigurationException  if the Root DSE can't be read or the DN is invalid
     */
    public static Dn getConfigurationDn( IBrowserConnection browserConnection )
        throws ConfigurationException
    {
        IProgressMonitor progressMonitor = new NullProgressMonitor();
        StudioProgressMonitor monitor = new StudioProgressMonitor( progressMonitor );

        // Opening the connection (if needed)
        openConnection( browserConnection.getConnection(), monitor );

        // Load Root DSE (if needed)
        if ( browserConnection.getRootDSE() == null )
        {
            InitializeRootDSERunnable.loadRootDSE( browserConnection, monitor );
        }

        // Getting the Root DSE
        IRootDSE rootDse = browserConnection.getRootDSE();

        try
        {
            // Getting the 'configcontext' attribute
            IAttribute configContextAttribute = rootDse.getAttribute( "configcontext" );
            if ( ( configContextAttribute != null ) && ( configContextAttribute.getValueSize() > 0 ) )
            {
                return new Dn( configContextAttribute.getStringValue() );
            }
            else
            {
                return getDefaultConfigurationDn();
            }
        }
        catch ( LdapInvalidDnException e )
        {
            throw new ConfigurationException( e );
        }
    }


    // ── getDefaultConfigurationDn — C-3PO Falls Back to the Known Address ────────
    // When the Root DSE doesn't advertise the config location, C-3PO falls back to
    // the standard Imperial configuration terminal address: "cn=config".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the well-known default configuration DN ("cn=config") as a Dn object.
     * This is the safe fallback used when the server's Root DSE doesn't advertise
     * a 'configcontext' attribute.
     *
     * <p>For example — C-3PO uses the standard address:</p>
     * <pre>
     *   Dn defaultDn = ConfigurationUtils.getDefaultConfigurationDn();
     *   // new Dn( "cn=config" )
     * </pre>
     *
     * @return                   a Dn representing "cn=config"
     * @throws ConfigurationException  if parsing "cn=config" fails (shouldn't happen in practice)
     */
    public static Dn getDefaultConfigurationDn() throws ConfigurationException
    {
        try
        {
            return new Dn( DEFAULT_CONFIG_DN );
        }
        catch ( LdapInvalidDnException e )
        {
            throw new ConfigurationException( e );
        }
    }


    // ── openConnection — C-3PO Opens the Imperial Comm Channel ───────────────────
    // Before R2 can plug in and start downloading, C-3PO has to establish the comm
    // channel — connecting, binding, and notifying all registered listeners that
    // the channel is open.
    // We open and bind the connection only if it's not already connected.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens and binds the given Connection if it isn't already connected.
     * After a successful bind, we fire 'connectionOpened' to all registered
     * IConnectionListeners so the rest of the Studio UI knows the connection is live.
     *
     * <p>For example — C-3PO opens the comm channel before R2 plugs in:</p>
     * <pre>
     *   ConfigurationUtils.openConnection( connection, monitor );
     *   // connection is now bound and ready for LDAP operations
     * </pre>
     *
     * @param connection  the Studio Connection to open; null-safe (does nothing if null)
     * @param monitor     the progress monitor that receives error reports if the open fails
     */
    public static void openConnection( Connection connection, StudioProgressMonitor monitor )
    {
        if ( connection != null && !connection.getConnectionWrapper().isConnected() )
        {
            connection.getConnectionWrapper().connect( monitor );
            if ( connection.getConnectionWrapper().isConnected() )
            {
                connection.getConnectionWrapper().bind( monitor );
            }

            if ( connection.getConnectionWrapper().isConnected() )
            {
                for ( IConnectionListener listener : ConnectionCorePlugin.getDefault()
                    .getConnectionListeners() )
                {
                    listener.connectionOpened( connection, monitor );
                }
                ConnectionEventRegistry.fireConnectionOpened( connection, null );
            }
        }
    }
}
