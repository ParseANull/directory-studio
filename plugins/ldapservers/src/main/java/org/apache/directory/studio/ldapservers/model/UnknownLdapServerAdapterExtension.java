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


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;


// ── CLASS: UnknownLdapServerAdapterExtension — THE MYSTERIOUS BOUNTY HUNTER IN CARBONITE ─
// In Cloud City, Boba Fett delivers Han Solo frozen in carbonite — a recognizable figure,
// but one whose status is unknown and who can't actually do anything useful until thawed.
// This class is the equivalent: a server record remains in the XML file (we know its ID,
// name, vendor, version from the saved data) but its plugin is no longer installed, so every
// operation — start, stop, add, openConfiguration — just pops up a warning and does nothing.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A stub {@link LdapServerAdapterExtension} used when a server's adapter plugin cannot be found.
 * When we load {@code ldapServers.xml} and can't find the adapter by ID in the extension
 * registry (e.g., the plugin was uninstalled), we create one of these so the server still
 * appears in the list with its saved metadata — but every operation shows a warning dialog
 * instead of actually doing anything.
 * Think of it as Han Solo in carbonite: present, recognizable, but non-functional.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UnknownLdapServerAdapterExtension extends LdapServerAdapterExtension
{
    // ── Han Solo Is Delivered, Still Frozen ──────────────────────────────────────────────────
    // Boba Fett delivers the carbonite block — Jabba's palace knows exactly who it is (ID, name,
    // vendor, version) but there's nothing useful it can do with him yet.
    // We wire up a stub LdapServerAdapter that pops a warning on any real operation attempt.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the unknown extension and wires up a stub adapter that warns on every operation.
     * The warning dialog explains which adapter ID, name, vendor, and version is missing so
     * the user knows exactly which plugin needs to be reinstalled.
     *
     * <p>For example — Jabba inspects the carbonite block:</p>
     * <pre>
     *   "I recognize this one — ApacheDS 3.0, vendor Apache. But I can't do anything with it."
     *   Every action → "Server adapter not available. Please reinstall the plugin."
     * </pre>
     */
    public UnknownLdapServerAdapterExtension()
    {
        // Setting behavior for this particular LDAP Server Adapter Extension
        setInstance( new LdapServerAdapter()
        {
            /**
             * {@inheritDoc}
             */
            public void add( LdapServer server, StudioProgressMonitor monitor ) throws Exception
            {
                showWarningDialog();
            }


            /**
             * {@inheritDoc}
             */
            public void delete( LdapServer server, StudioProgressMonitor monitor ) throws Exception
            {
                // Nothing to do
            }


            /**
             * {@inheritDoc}
             */
            public void openConfiguration( LdapServer server, StudioProgressMonitor monitor ) throws Exception
            {
                showWarningDialog();
            }


            /**
             * {@inheritDoc}
             */
            public void start( LdapServer server, StudioProgressMonitor monitor ) throws Exception
            {
                showWarningDialog();

                server.setStatus( LdapServerStatus.STOPPED );
            }


            /**
             * {@inheritDoc}
             */
            public void stop( LdapServer server, StudioProgressMonitor monitor ) throws Exception
            {
                showWarningDialog();

                server.setStatus( LdapServerStatus.STOPPED );
            }


            /**
             * Shows the warning dialog.
             */
            private void showWarningDialog()
            {
                Display.getDefault().asyncExec( new Runnable()
                {
                    public void run()
                    {
                        CommonUIUtils.openWarningDialog(
                            Messages.getString( "UnknownLdapServerAdapterExtension.ServerAdapterNotAvailable" ), //$NON-NLS-1$
                            NLS.bind(
                                Messages
                                    .getString( "UnknownLdapServerAdapterExtension.ServerCreatedWithServerAdapterNoLongerAvailable" ), //$NON-NLS-1$
                                new String[]
                                    { getId(), getName(), getVendor(), getVersion() } ) );
                    }
                } );
            }


            /**
             * {@inheritDoc}
             */
            public String[] checkPortsBeforeServerStart( LdapServer server )
            {
                return new String[0];
            }
        } );

    }
}
