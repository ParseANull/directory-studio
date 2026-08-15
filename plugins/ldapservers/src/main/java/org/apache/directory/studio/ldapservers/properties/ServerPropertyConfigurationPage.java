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
package org.apache.directory.studio.ldapservers.properties;


import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterConfigurationPage;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterConfigurationPageModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;


// ── CLASS: ServerPropertyConfigurationPage — THE ENGINEERING CONFIGURATION PANEL ─────────
// When Imperial engineers open a ship's Properties, the Configuration tab is the live
// engineering panel — they can adjust port settings, installation paths, and other adapter-
// specific parameters, then save those changes back to the ship's technical record.
// This is that panel: it embeds the adapter's configuration page and wires up save/validation.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Configuration" tab in a server's Properties dialog.
 * Embeds the adapter-specific {@link LdapServerAdapterConfigurationPage} so the user can
 * edit adapter parameters (ports, paths, etc.) and saves them back via
 * {@link LdapServersManager#saveServersToStore()} on OK.
 * Think of it as the engineering configuration panel: read the schematic, adjust the settings.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerPropertyConfigurationPage extends PropertyPage implements IWorkbenchPropertyPage,
    LdapServerAdapterConfigurationPageModifyListener
{
    /** The LDAP server*/
    private LdapServer ldapServer;

    /** The configuration page */
    private LdapServerAdapterConfigurationPage configurationPage;


    // ── Opening The Engineering Panel — No Defaults Button ────────────────────────────────────
    // The engineering panel doesn't have a "Restore Defaults" option — configuration is live
    // and adapter-specific; there's no safe universal default to restore to.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the page and suppresses "Apply" / "Restore Defaults" — adapter configuration
     * pages manage their own validation and save cycle via {@link #performOk()}.
     */
    public ServerPropertyConfigurationPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    // ── Building The Engineering Panel From The Adapter's Schematic ──────────────────────────
    // The adapter's configuration page knows exactly which controls to show for this server type.
    // We ask it to build its control tree, then load the server's current settings into it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the configuration page's widgets by delegating to the adapter's
     * {@link LdapServerAdapterConfigurationPage}.
     * Loads the server's current configuration so all fields start populated.
     *
     * @param parent  the parent composite provided by the Properties dialog framework
     * @return the top-level control created by the configuration page
     */
    protected Control createContents( Composite parent )
    {
        // Getting the server
        ldapServer = ( LdapServer ) getElement();

        if ( ldapServer != null )
        {
            configurationPage = ldapServer.getLdapServerAdapterExtension()
                .getNewConfigurationPageInstance();
            configurationPage.setModifyListener( this );

            Control control = configurationPage.createControl( parent );
            configurationPage.loadConfiguration( ldapServer );

            return control;
        }

        return parent;
    }


    // ── Live Validation As The Engineer Types ─────────────────────────────────────────────────
    // When the engineer changes a field, the adapter's configuration page raises a modify event.
    // We forward the error message and page-complete state to Eclipse's Properties dialog so
    // it can show or clear the error banner and enable/disable OK.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the adapter's configuration page whenever the user modifies a field.
     * Pushes the current error message and page-complete flag into Eclipse's Properties dialog
     * to update the OK button and error banner.
     *
     * <p>For example — the engineer types a bad port number:</p>
     * <pre>
     *   configurationPageModified() → setErrorMessage("Port must be 1–65535") → OK grays out.
     *   User fixes it → setErrorMessage(null) → setValid(true) → OK re-enables.
     * </pre>
     */
    public void configurationPageModified()
    {
        if ( ldapServer != null )
        {
            setErrorMessage( configurationPage.getErrorMessage() );
            setValid( configurationPage.isPageComplete() );
        }
    }


    // ── Saving Configuration Changes When The Engineer Clicks OK ──────────────────────────────
    // The engineer has adjusted the ports and paths; clicking OK commits those changes.
    // We save them into the server object via the adapter page, then persist to disk via
    // LdapServersManager so the changes survive the next Studio restart.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the user clicks OK.
     * Delegates to {@link LdapServerAdapterConfigurationPage#saveConfiguration(LdapServer)}
     * to commit the field values, then calls {@link LdapServersManager#saveServersToStore()}
     * to persist everything to {@code ldapServers.xml}.
     *
     * @return {@code true} if the Properties dialog should close; {@code false} to keep it open
     */
    public boolean performOk()
    {
        if ( ldapServer != null )
        {
            // Saving the configuration
            configurationPage.saveConfiguration( ldapServer );

            // Saving the server to the file store.
            LdapServersManager.getDefault().saveServersToStore();

        }

        return super.performOk();
    }
}
