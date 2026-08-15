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
package org.apache.directory.studio.openldap.config.editor.pages;


import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.editor.databases.DatabasesMasterDetailsBlock;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: DatabasesPage — The Bridge of a Star Destroyer (Databases Station) ─
// The bridge of a Star Destroyer has multiple workstations, each dedicated to
// a different system: weapons, navigation, shields, communications.  The
// Databases station is the one where the crew monitors and configures all the
// LDAP backend databases attached to this OpenLDAP server.  DatabasesPage is
// that station — it's one tab in the multi-tab editor, and it delegates all
// its actual work to the DatabasesMasterDetailsBlock which provides the classic
// left-list / right-details split layout.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "Databases" tab page of the OpenLDAP server configuration editor.
 * It hosts the {@link DatabasesMasterDetailsBlock} which provides a master/
 * details split: the left pane lists all configured databases, and the right
 * pane shows the settings for whichever database is selected.
 * Think of it as the databases workstation on a Star Destroyer's bridge —
 * one focused station in a multi-station command deck.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DatabasesPage extends OpenLDAPServerConfigurationEditorPage
{
    /** The Page ID*/
    public static final String ID = DatabasesPage.class.getName(); //$NON-NLS-1$

    /** The Page Title */
    private static final String TITLE = Messages.getString( "OpenLDAPDatabasesPage.Title" ); //$NON-NLS-1$

    /** The master details block */
    private DatabasesMasterDetailsBlock masterDetailsBlock;


    // ── Constructor — The Databases Station Comes Online ──────────────────────
    // A new crew member takes the databases station, logs in, and reports to
    // the bridge commander (the editor).
    // We register this page with its parent editor under the standard page ID.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DatabasesPage and registers it with the given editor.
     * The editor reference lets us reach the shared OpenLdapConfiguration model
     * through the base class.
     *
     * @param editor  the OpenLdapServerConfigurationEditor that owns this page
     */
    public DatabasesPage( OpenLdapServerConfigurationEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── createFormContent — The Station Builds Its Console ────────────────────
    // The databases workstation powers up and assembles its split-panel display:
    // the master list of databases on the left and the detail panel on the right.
    // We delegate to DatabasesMasterDetailsBlock which owns that layout.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds this page's form content by creating and activating the
     * databases master/details block.
     * We pass the block our managed form so it can register section parts and
     * wire up selection events.
     *
     * @param parent   the SWT composite provided by the base class
     * @param toolkit  the Eclipse Forms toolkit for creating styled widgets
     */
    protected void createFormContent( Composite parent, FormToolkit toolkit )
    {
        masterDetailsBlock = new DatabasesMasterDetailsBlock( this );
        masterDetailsBlock.createContent( getManagedForm() );
    }


    // ── refreshUI — The Station Refreshes Its Displays ────────────────────────
    // When new sensor data arrives, the databases station operator presses
    // refresh and all the readouts update with the latest configuration values.
    // We delegate to the master/details block if it's ready.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes all UI widgets in this page from the current configuration model.
     * Only performs the refresh if the page is fully initialized; calling it
     * before initialization is a no-op to avoid NPEs during editor startup.
     */
    public void refreshUI()
    {
        if ( isInitialized() )
        {
            masterDetailsBlock.refreshUI();
        }
    }


    // ── doSave — The Station Commits Its Configuration Changes ────────────────
    // The station operator locks in the configuration changes and fires the
    // save sequence, which propagates through to the master/details block.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current state of the databases page.
     * We delegate to the master/details block which knows how to write the
     * database configuration back to the underlying model and ultimately to
     * the LDAP server.
     *
     * @param monitor  the Eclipse progress monitor for reporting save progress
     */
    @Override
    public void doSave( IProgressMonitor monitor )
    {
        if ( masterDetailsBlock != null )
        {
            masterDetailsBlock.doSave( monitor );
        }
    }
}
