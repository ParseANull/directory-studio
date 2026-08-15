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

package org.apache.directory.studio.ldapservers.model;


import org.apache.directory.studio.ldapservers.actions.CreateConnectionActionHelper;
import org.eclipse.ui.IActionFilter;


// ── CLASS: LdapServerActionFilterAdapter — C-3PO FILTERING MESSAGES BEFORE THE COUNCIL ───
// Before any message reaches the Rebel Council, C-3PO screens it — is it relevant?
// Is it from a trusted source?  Is this really the right person to be speaking right now?
// Eclipse does the same with toolbar actions: it evaluates {@link IActionFilter#testAttribute}
// on the selected object to decide which buttons to enable.  This class is the screener.
// It answers questions like "is this server currently stopped?" or "does it have a config page?"
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * An {@link IActionFilter} implementation that Eclipse evaluates against a selected
 * {@link LdapServer} to decide which context-menu items and toolbar buttons are active.
 * Eclipse's declarative action system can declare conditions like {@code status=="stopped"}
 * in plugin.xml; this class is what actually checks that condition against the live server.
 * Think of it as C-3PO's screening service: every action query goes through here first.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServerActionFilterAdapter implements IActionFilter
{
    // Identifier and value strings
    private static final String ID = "id"; //$NON-NLS-1$
    private static final String NAME = "name"; //$NON-NLS-1$
    private static final Object STATUS = "status"; //$NON-NLS-1$
    private static final Object STATUS_STARTED = "started"; //$NON-NLS-1$
    private static final Object STATUS_STARTING = "starting"; //$NON-NLS-1$
    private static final Object STATUS_STOPPED = "stopped"; //$NON-NLS-1$
    private static final Object STATUS_STOPPING = "stopping"; //$NON-NLS-1$
    private static final Object STATUS_UNKNOWN = "unknown"; //$NON-NLS-1$
    private static final Object EXTENSION_ID = "extensionId"; //$NON-NLS-1$
    private static final Object EXTENSION_NAME = "extensionName"; //$NON-NLS-1$
    private static final Object EXTENSION_VERSION = "extensionVersion"; //$NON-NLS-1$
    private static final Object EXTENSION_VENDOR = "extensionVendor"; //$NON-NLS-1$
    private static final Object HAS_CONFIGURATION_PAGE = "hasConfigurationPage"; //$NON-NLS-1$
    private static final Object IS_LDAP_PERSPECTIVE_AVAILABLE = "isLdapPerspectiveAvailable"; //$NON-NLS-1$

    /** The class instance */
    private static LdapServerActionFilterAdapter INSTANCE = new LdapServerActionFilterAdapter();


    // ── C-3PO Initializes His Screening Protocols ───────────────────────────────────────────
    // C-3PO's screening station is set up once — there is only one screening desk for the
    // entire Council chamber, shared by all action queries.
    // Private singleton; access via {@link #getInstance()}.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a singleton.
     * Use {@link #getInstance()} to get the shared instance.
     */
    private LdapServerActionFilterAdapter()
    {
        // Nothing to initialize
    }


    // ── Calling C-3PO To The Screening Desk ─────────────────────────────────────────────────
    // Every officer who needs a message screened goes to C-3PO's desk — there is one, and
    // it's always at the same location.
    // Standard singleton accessor.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared singleton {@link LdapServerActionFilterAdapter} instance.
     * Eclipse calls this via the adapter mechanism from {@link LdapServer#getAdapter}.
     *
     * @return the single shared instance
     */
    public static LdapServerActionFilterAdapter getInstance()
    {
        return INSTANCE;
    }


    // ── C-3PO Answers The Question About The Server ──────────────────────────────────────────
    // Eclipse hands C-3PO a question: "Is the Falcon's status 'stopped'?"
    // C-3PO inspects the Falcon's status board and returns yes or no.
    // We dispatch on the attribute name and compare the server's actual state to the expected value.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Tests whether the given {@link LdapServer} matches the attribute condition declared in plugin.xml.
     * Eclipse calls this when evaluating action enablement expressions like
     * {@code <enabledWhen><with variable="selection"><test property="status" value="stopped"/>...}.
     * Supported attributes: {@code id}, {@code name}, {@code status}, {@code extensionId},
     * {@code extensionName}, {@code extensionVersion}, {@code extensionVendor},
     * {@code hasConfigurationPage}, {@code isLdapPerspectiveAvailable}.
     *
     * <p>For example — C-3PO screens an enablement query:</p>
     * <pre>
     *   Eclipse asks: testAttribute(server, "status", "stopped")
     *   C-3PO: server.getStatus() == STOPPED? → true → Start button enabled.
     *   Eclipse asks: testAttribute(server, "status", "started")
     *   C-3PO: server.getStatus() == STOPPED? No → false → Stop button disabled.
     * </pre>
     *
     * @param target  the selected object — we only handle {@link LdapServer} instances
     * @param name    the attribute name to test (e.g., "status", "id")
     * @param value   the expected value to compare against (e.g., "stopped", "started")
     * @return {@code true} if the server's attribute matches the given value; {@code false} otherwise
     */
    public boolean testAttribute( Object target, String name, String value )
    {
        if ( target instanceof LdapServer )
        {
            LdapServer server = ( LdapServer ) target;

            // ID
            if ( ID.equals( name ) )
            {
                return value.equals( server.getId() );
            }
            // NAME
            else if ( NAME.equals( name ) )
            {
                return value.equals( server.getName() );
            }
            // STATUS
            else if ( STATUS.equals( name ) )
            {
                switch ( server.getStatus() )
                {
                    case STARTED:
                        return value.equals( STATUS_STARTED );
                    case STARTING:
                        return value.equals( STATUS_STARTING );
                    case STOPPED:
                        return value.equals( STATUS_STOPPED );
                    case STOPPING:
                        return value.equals( STATUS_STOPPING );
                    case UNKNOWN:
                        return value.equals( STATUS_UNKNOWN );
                }
            }
            // EXTENSION ID
            else if ( EXTENSION_ID.equals( name ) )
            {
                if ( server.getLdapServerAdapterExtension() != null )
                {
                    return value.equals( server.getLdapServerAdapterExtension().getId() );
                }
            }
            // EXTENSION NAME
            else if ( EXTENSION_NAME.equals( name ) )
            {
                if ( server.getLdapServerAdapterExtension() != null )
                {
                    return value.equals( server.getLdapServerAdapterExtension().getName() );
                }
            }
            // EXTENSION VERSION
            else if ( EXTENSION_VERSION.equals( name ) )
            {
                if ( server.getLdapServerAdapterExtension() != null )
                {
                    return value.equals( server.getLdapServerAdapterExtension().getVersion() );
                }
            }
            // EXTENSION VENDOR
            else if ( EXTENSION_VENDOR.equals( name ) )
            {
                if ( server.getLdapServerAdapterExtension() != null )
                {
                    return value.equals( server.getLdapServerAdapterExtension().getVendor() );
                }
            }
            // HAS CONFIGURATION PAGE
            else if ( HAS_CONFIGURATION_PAGE.equals( name ) )
            {
                String configurationPageClassName = server.getLdapServerAdapterExtension()
                    .getConfigurationPageClassName();

                boolean hasConfigurationPage = ( ( configurationPageClassName != null ) && ( !"" //$NON-NLS-1$
                .equals( configurationPageClassName ) ) );

                return value.equalsIgnoreCase( hasConfigurationPage ? "true" : "false" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            // IS LDAP PERSPECTIVE AVAILABLE
            else if ( IS_LDAP_PERSPECTIVE_AVAILABLE.equals( name ) )
            {
                boolean isLdapPerspectiveAvailable = CreateConnectionActionHelper.isLdapBrowserPluginsAvailable();
                boolean booleanValue = Boolean.parseBoolean( value );

                return isLdapPerspectiveAvailable == booleanValue;
            }
        }

        return false;
    }
}
