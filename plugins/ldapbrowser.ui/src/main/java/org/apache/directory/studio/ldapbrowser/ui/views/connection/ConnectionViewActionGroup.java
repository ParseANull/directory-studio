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

package org.apache.directory.studio.ldapbrowser.ui.views.connection;


import org.apache.directory.studio.connection.ui.actions.ConnectionViewActionProxy;
import org.apache.directory.studio.connection.ui.widgets.ConnectionActionGroup;
import org.apache.directory.studio.ldapbrowser.ui.actions.ExportConnectionsAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.ImportConnectionsAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.ImportExportAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.OpenSchemaBrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.PasswordModifyExtendedOperationAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.ReloadSchemaAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchActionConstants;


// ── CLASS: ConnectionViewActionGroup — LANDO RUNS CLOUD CITY ─────────────────
// Lando Calrissian keeps Cloud City running smoothly — he knows every baron,
// every department head, every operation happening on every level, and he can
// dispatch the right person or shut down any wing at a moment's notice.
// This class is Cloud City's operations center: it owns and coordinates every
// toolbar and context-menu action available in the connection view.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages all toolbar, menu, and context-menu actions for the connection view.
 * It extends {@link ConnectionActionGroup} with LDAP-browser-specific actions
 * like import/export, schema browser, and extended operations.
 * Think of this class as Lando running Cloud City — he keeps every department
 * (action) staffed, enabled, and dispatched at the right moment.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionViewActionGroup extends ConnectionActionGroup
{

    /** The connection view */
    private ConnectionView view;

    /** The link with editor action. */
    private LinkWithEditorAction linkWithEditorAction;

    /** The Constant importDsmlAction. */
    private static final String importDsmlAction = "importDsmlAction"; //$NON-NLS-1$

    /** The Constant exportDsmlAction. */
    private static final String exportDsmlAction = "exportDsmlAction"; //$NON-NLS-1$

    /** The Constant importLdifAction. */
    private static final String importLdifAction = "importLdifAction"; //$NON-NLS-1$

    /** The Constant exportLdifAction. */
    private static final String exportLdifAction = "exportLdifAction"; //$NON-NLS-1$

    /** The Constant exportCsvAction. */
    private static final String exportCsvAction = "exportCsvAction"; //$NON-NLS-1$

    /** The Constant exportExcelAction. */
    private static final String exportExcelAction = "exportExcelAction"; //$NON-NLS-1$

    /** The Constant exportOdfAction. */
    private static final String exportOdfAction = "exportOdfAction"; //$NON-NLS-1$

    /** The Constant importConnectionsAction. */
    private static final String importConnectionsAction = "importConnectionsAction"; //$NON-NLS-1$

    /** The Constant importConnectionsAction. */
    private static final String exportConnectionsAction = "exportConnectionsAction"; //$NON-NLS-1$

    /** The Constant openSchemaBrowserAction. */
    private static final String openSchemaBrowserAction = "openSchemaBrowserAction"; //$NON-NLS-1$

    /** The Constant reloadSchemaAction. */
    private static final String reloadSchemaAction = "reloadSchemaAction"; //$NON-NLS-1$

    /** The Constant passwordModifyExtendedOperationAction. */
    private static final String passwordModifyExtendedOperationAction = "passwordModifyExtendedOperation"; //$NON-NLS-1$


    // ── Constructor: Lando Assigns Every Department Head ────────────────────
    // On Lando's first day running Cloud City, he walks every corridor and
    // assigns a department head to each operation — mining, hospitality,
    // security — so every function is covered and ready to execute.
    // We register every action proxy in the map so they're ready to be
    // dispatched to the toolbar, menu, or context menu on demand.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this action group and registers every connection view action.
     * We call {@link ConnectionActionGroup}'s constructor for the base actions,
     * then add LDAP-browser-specific ones: import/export, schema browser,
     * schema reload, and password-modify extended operation.
     *
     * <p>For example — Lando staffs up every Cloud City department on day one:</p>
     * <pre>
     *   connectionActionMap.put( importLdifAction, new ImportExportAction(...) );
     *   connectionActionMap.put( exportCsvAction,  new ImportExportAction(...) );
     *   // ... and so on for every operation
     * </pre>
     *
     * @param view  the connection view that owns this action group
     */
    public ConnectionViewActionGroup( ConnectionView view )
    {
        super( view.getMainWidget(), view.getConfiguration() );
        this.view = view;
        TreeViewer viewer = view.getMainWidget().getViewer();

        linkWithEditorAction = new LinkWithEditorAction( view );
        connectionActionMap.put( importDsmlAction, new ConnectionViewActionProxy( viewer, this, new ImportExportAction(
            ImportExportAction.TYPE_IMPORT_DSML ) ) );
        connectionActionMap.put( exportDsmlAction, new ConnectionViewActionProxy( viewer, this, new ImportExportAction(
            ImportExportAction.TYPE_EXPORT_DSML ) ) );
        connectionActionMap.put( importLdifAction, new ConnectionViewActionProxy( viewer, this, new ImportExportAction(
            ImportExportAction.TYPE_IMPORT_LDIF ) ) );
        connectionActionMap.put( exportLdifAction, new ConnectionViewActionProxy( viewer, this, new ImportExportAction(
            ImportExportAction.TYPE_EXPORT_LDIF ) ) );
        connectionActionMap.put( exportCsvAction, new ConnectionViewActionProxy( viewer, this, new ImportExportAction(
            ImportExportAction.TYPE_EXPORT_CSV ) ) );
        connectionActionMap.put( exportExcelAction, new ConnectionViewActionProxy( viewer, this,
            new ImportExportAction( ImportExportAction.TYPE_EXPORT_EXCEL ) ) );
        connectionActionMap.put( exportOdfAction, new ConnectionViewActionProxy( viewer, this, new ImportExportAction(
            ImportExportAction.TYPE_EXPORT_ODF ) ) );
        connectionActionMap.put( importConnectionsAction, new ConnectionViewActionProxy( viewer, this,
            new ImportConnectionsAction() ) );
        connectionActionMap.put( exportConnectionsAction, new ConnectionViewActionProxy( viewer, this,
            new ExportConnectionsAction() ) );

        connectionActionMap.put( openSchemaBrowserAction, new ConnectionViewActionProxy( viewer, this,
            new OpenSchemaBrowserAction() ) );
        connectionActionMap.put( reloadSchemaAction, new ConnectionViewActionProxy( viewer, this,
            new ReloadSchemaAction() ) );

        connectionActionMap.put( passwordModifyExtendedOperationAction, new ConnectionViewActionProxy( viewer, this,
            new PasswordModifyExtendedOperationAction() ) );
    }


    // ── dispose: Lando Evacuates Cloud City ──────────────────────────────────
    // When the Empire arrives, Lando triggers the evacuation — every
    // department shuts down in an orderly sequence, no resource left running.
    // We dispose the link-with-editor action first, null out the view
    // reference, then delegate to super to clean up the base actions.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Releases all resources held by this action group.
     * We dispose the link-with-editor action, clear the view reference, then
     * delegate to {@link ConnectionActionGroup#dispose()} for the base actions.
     *
     * <p>For example — Lando evacuates Cloud City department by department:</p>
     * <pre>
     *   linkWithEditorAction.dispose(); // comms shut down
     *   view = null;                    // reference released
     *   super.dispose();                // base departments cleared
     * </pre>
     */
    @Override
    public void dispose()
    {
        if ( view != null )
        {
            linkWithEditorAction.dispose();
            linkWithEditorAction = null;
            view = null;
        }
        super.dispose();
    }


    // ── menuAboutToShow: Lando Briefs the Department Heads ──────────────────
    // Just before a Cloud City council meeting, Lando gathers the department
    // heads and assigns each one a seat at the table in the right order.
    // We populate the context menu with every action in the correct grouping:
    // new, open/close, schema, edit, import/export, and properties.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Populates the context menu right before it's displayed to the user.
     * JFace calls this just-in-time so we can check action states (e.g., is the
     * connection open or closed?) and add only the relevant entries.
     * We group actions with {@link Separator}s so the menu stays organized.
     *
     * <p>For example — Lando seats department heads at the council table:</p>
     * <pre>
     *   menuManager.add( newConnectionAction );   // new connections first
     *   menuManager.add( new Separator() );       // visual divider
     *   menuManager.add( openOrCloseAction );     // context-sensitive open/close
     *   menuManager.add( importMenuManager );     // nested import submenu
     *   menuManager.add( exportMenuManager );     // nested export submenu
     * </pre>
     *
     * @param menuManager  the JFace menu manager we populate just before display
     */
    @Override
    public void menuAboutToShow( IMenuManager menuManager )
    {

        // add
        menuManager.add( ( IAction ) connectionActionMap.get( NEW_CONNECTION_ACTION ) );
        menuManager.add( ( IAction ) connectionActionMap.get( NEW_CONNECTION_FOLDER_ACTION ) );
        menuManager.add( new Separator() );

        // open/close
        if ( ( ( IAction ) connectionActionMap.get( CLOSE_CONNECTION_ACTION ) ).isEnabled() )
        {
            menuManager.add( ( IAction ) connectionActionMap.get( CLOSE_CONNECTION_ACTION ) );
        }
        else if ( ( ( IAction ) connectionActionMap.get( OPEN_CONNECTION_ACTION ) ).isEnabled() )
        {
            menuManager.add( ( IAction ) connectionActionMap.get( OPEN_CONNECTION_ACTION ) );
        }
        menuManager.add( new Separator() );

        menuManager.add( ( IAction ) connectionActionMap.get( openSchemaBrowserAction ) );
        menuManager.add( ( IAction ) connectionActionMap.get( reloadSchemaAction ) );
        menuManager.add( new Separator() );

        // copy/paste/...
        menuManager.add( ( IAction ) connectionActionMap.get( COPY_CONNECTION_ACTION ) );
        menuManager.add( ( IAction ) connectionActionMap.get( PASTE_CONNECTION_ACTION ) );
        menuManager.add( ( IAction ) connectionActionMap.get( DELETE_CONNECTION_ACTION ) );
        menuManager.add( ( IAction ) connectionActionMap.get( RENAME_CONNECTION_ACTION ) );
        menuManager.add( new Separator() );

        // import/export
        MenuManager importMenuManager = new MenuManager( Messages.getString( "ConnectionViewActionGroup.Import" ) ); //$NON-NLS-1$
        importMenuManager.add( ( IAction ) connectionActionMap.get( importLdifAction ) );
        importMenuManager.add( ( IAction ) connectionActionMap.get( importDsmlAction ) );
        importMenuManager.add( new Separator() );
        importMenuManager.add( ( IAction ) connectionActionMap.get( importConnectionsAction ) );
        importMenuManager.add( new Separator() );
        menuManager.add( importMenuManager );
        MenuManager exportMenuManager = new MenuManager( Messages.getString( "ConnectionViewActionGroup.Export" ) ); //$NON-NLS-1$
        exportMenuManager.add( ( IAction ) connectionActionMap.get( exportLdifAction ) );
        exportMenuManager.add( ( IAction ) connectionActionMap.get( exportDsmlAction ) );
        exportMenuManager.add( new Separator() );
        exportMenuManager.add( ( IAction ) connectionActionMap.get( exportCsvAction ) );
        exportMenuManager.add( ( IAction ) connectionActionMap.get( exportExcelAction ) );
        exportMenuManager.add( ( IAction ) connectionActionMap.get( exportOdfAction ) );
        exportMenuManager.add( new Separator() );
        exportMenuManager.add( ( IAction ) connectionActionMap.get( exportConnectionsAction ) );
        exportMenuManager.add( new Separator() );
        menuManager.add( exportMenuManager );
        menuManager.add( new Separator() );

        // additions
        menuManager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
        MenuManager extendedOperationsMenuManager = new MenuManager(
            Messages.getString( "ConnectionViewActionGroup.ExtendedOperations" ) ); //$NON-NLS-1$
        extendedOperationsMenuManager.add( connectionActionMap.get( passwordModifyExtendedOperationAction ) );
        menuManager.add( extendedOperationsMenuManager );
        menuManager.add( new Separator() );

        // properties
        menuManager.add( ( IAction ) connectionActionMap.get( PROPERTY_DIALOG_ACTION ) );
    }

}
