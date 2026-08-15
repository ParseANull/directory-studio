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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportCsvWizard;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportDsmlWizard;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportExcelWizard;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportLdifWizard;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportOdfWizard;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ImportDsmlWizard;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ImportLdifWizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;


// ── CLASS: ImportExportAction — CLONE TROOPER EXECUTES THE IMPORT/EXPORT ORDER
// Palpatine issues Order 66 in one transmission — one signal, many mission
// types. Clone troopers know from the type constant which specific operation
// to execute: LDIF import, CSV export, EXCEL export, ODF export, DSML import,
// or DSML export. This class works the same way: constructed with a type
// constant, it launches exactly the right wizard when the signal fires.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A multi-purpose import/export action that launches the appropriate wizard
 * depending on the {@code type} constant provided at construction time.
 * Supported operations: LDIF import/export, CSV export, Excel export, ODF
 * export, DSML import/export.
 * The action resolves the target connection/entry/search from the current
 * selection and wires it into the wizard so the user doesn't have to re-select
 * the source every time they open the wizard.
 * Think of this as a clone trooper who knows exactly which mission to run
 * based on which order code he received.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportExportAction extends BrowserAction
{
    /**
     * LDIF Import Type
     */
    public static final int TYPE_IMPORT_LDIF = 0;

    /**
     * LDIF Export Type
     */
    public static final int TYPE_EXPORT_LDIF = 1;

    /**
     * CSV Export Type
     */
    public static final int TYPE_EXPORT_CSV = 2;

    /**
     * EXCEL Export Type
     */
    public static final int TYPE_EXPORT_EXCEL = 3;

    /**
     * DSML Import Type
     */
    public static final int TYPE_IMPORT_DSML = 4;

    /**
     * DSML Export Type
     */
    public static final int TYPE_EXPORT_DSML = 5;

    /**
     * ODF Export Type
     */
    public static final int TYPE_EXPORT_ODF = 6;

    private int type;


    // ── Trooper Receives His Mission Type ─────────────────────────────────────
    // Each clone trooper knows his assignment before the signal fires — LDIF
    // import, CSV export, or something else. We lock in the type at construction
    // so the right wizard is always launched when run() is called.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ImportExportAction} bound to the given operation type.
     * Each instance permanently handles one import or export type — create
     * separate instances for each operation you want in a menu.
     *
     * @param type  one of the {@code TYPE_*} constants defined in this class;
     *              determines which wizard is launched by {@link #run()}
     */
    public ImportExportAction( int type )
    {
        super();
        this.type = type;
    }


    // ── Trooper Announces the Mission Name ────────────────────────────────────
    // Each mission type has its own designation — "LDIF Import", "CSV Export",
    // "Excel Export" — so the trooper announces the right one for the menu label.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name for this import/export type
     * (e.g., "LDIF Import", "CSV Export", "DSML Export").
     *
     * @return  the menu label; never {@code null}
     */
    public String getText()
    {
        if ( this.type == TYPE_IMPORT_LDIF )
        {
            return Messages.getString( "ImportExportAction.LDIFImport" ); //$NON-NLS-1$
        }
        else if ( this.type == TYPE_EXPORT_LDIF )
        {
            return Messages.getString( "ImportExportAction.LDIFExport" ); //$NON-NLS-1$
        }
        else if ( this.type == TYPE_EXPORT_CSV )
        {
            return Messages.getString( "ImportExportAction.CVSExport" ); //$NON-NLS-1$
        }
        else if ( this.type == TYPE_EXPORT_EXCEL )
        {
            return Messages.getString( "ImportExportAction.ExcelExport" ); //$NON-NLS-1$
        }
        else if ( this.type == TYPE_EXPORT_ODF )
        {
            return Messages.getString( "ImportExportAction.OdfExport" ); //$NON-NLS-1$
        }
        else if ( this.type == TYPE_IMPORT_DSML )
        {
            return Messages.getString( "ImportExportAction.DSMLImport" ); //$NON-NLS-1$
        }
        else if ( this.type == TYPE_EXPORT_DSML )
        {
            return Messages.getString( "ImportExportAction.DSMLExport" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "ImportExportAction.Export" ); //$NON-NLS-1$
        }
    }


    // ── Trooper Displays His Mission Badge ───────────────────────────────────
    // Each mission type has a distinct insignia — LDIF import looks different
    // from DSML export. We return the right icon for this type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the icon representing this import/export type.
     * Each type has a distinct icon so users can tell them apart in menus.
     *
     * @return  the {@link ImageDescriptor} for the icon, or {@code null} for unrecognised types
     */
    public ImageDescriptor getImageDescriptor()
    {
        if ( this.type == TYPE_IMPORT_LDIF )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_IMPORT_LDIF );
        }
        else if ( this.type == TYPE_EXPORT_LDIF )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_LDIF );
        }
        else if ( this.type == TYPE_EXPORT_CSV )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_CSV );
        }
        else if ( this.type == TYPE_EXPORT_EXCEL )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_XLS );
        }
        else if ( this.type == TYPE_EXPORT_ODF )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_ODF );
        }
        else if ( this.type == TYPE_IMPORT_DSML )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_IMPORT_DSML );
        }
        else if ( this.type == TYPE_EXPORT_DSML )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_DSML );
        }
        else
        {
            return null;
        }
    }


    // ── Trooper Checks His Command Registry ──────────────────────────────────
    // No registered keyboard shortcut for this operation type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action has no registered Eclipse
     * command ID and therefore no keyboard shortcut.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Trooper Verifies Mission Conditions ──────────────────────────────────
    // The trooper checks his target list before reporting ready: he needs at
    // least one resolvable target — an entry, a connection, a search, or a
    // connection passed directly as input.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when the current selection or input contains something
     * we can use as the source or destination for the import/export — an entry,
     * a connection, a search, or a browser connection input.
     *
     * @return  {@code true} if the action can be performed in the current context
     */
    public boolean isEnabled()
    {
        return getEntry() != null || getConnection() != null || getSearch() != null || getConnectionInput() != null;

    }


    // ── Trooper Executes the Right Mission ────────────────────────────────────
    // The trooper receives the order code and launches the correct wizard for
    // this type. For import operations, we wire in the connection we find in
    // the selection. For export operations, the wizard handles everything else.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Launches the appropriate import or export wizard based on the configured
     * type. For import wizards, we pre-populate the connection from the current
     * selection. For export wizards, the wizard discovers the target itself.
     * The dialog blocks until the user finishes or cancels.
     */
    public void run()
    {
        IWizard wizard = null;

        if ( this.type == TYPE_IMPORT_LDIF )
        {
            if ( getEntry() != null )
            {
                wizard = new ImportLdifWizard( getEntry().getBrowserConnection() );
            }
            else if ( getSearch() != null )
            {
                wizard = new ImportLdifWizard( getSearch().getBrowserConnection() );
            }
            else if ( getConnectionInput() != null )
            {
                wizard = new ImportLdifWizard( getConnectionInput() );
            }
            else if ( getConnection() != null )
            {
                wizard = new ImportLdifWizard( getConnection() );
            }
        }
        else if ( this.type == TYPE_IMPORT_DSML )
        {
            if ( getEntry() != null )
            {
                wizard = new ImportDsmlWizard( getEntry().getBrowserConnection() );
            }
            else if ( getSearch() != null )
            {
                wizard = new ImportDsmlWizard( getSearch().getBrowserConnection() );
            }
            else if ( getConnectionInput() != null )
            {
                wizard = new ImportDsmlWizard( getConnectionInput() );
            }
            else if ( getConnection() != null )
            {
                wizard = new ImportDsmlWizard( getConnection() );
            }
        }
        else if ( this.type == TYPE_EXPORT_LDIF )
        {
            wizard = new ExportLdifWizard();
        }
        else if ( this.type == TYPE_EXPORT_CSV )
        {
            wizard = new ExportCsvWizard();
        }
        else if ( this.type == TYPE_EXPORT_EXCEL )
        {
            wizard = new ExportExcelWizard();
        }
        else if ( this.type == TYPE_EXPORT_ODF )
        {
            wizard = new ExportOdfWizard();
        }
        else if ( this.type == TYPE_EXPORT_DSML )
        {
            wizard = new ExportDsmlWizard();
        }

        if ( wizard != null )
        {
            WizardDialog dialog = new WizardDialog( getShell(), wizard );
            dialog.setBlockOnOpen( true );
            dialog.create();
            dialog.open();
        }

    }


    // ── Trooper Identifies the Target Entry ──────────────────────────────────
    // The trooper scans for his primary target: selected entries first, then
    // search results, then bookmarks. Returns the first one he finds.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first {@link IEntry} found in the current selection —
     * checking selected entries, search results, and bookmarks in that order.
     *
     * @return  the target entry, or {@code null} if none is selected
     */
    protected IEntry getEntry()
    {
        IEntry entry = null;
        if ( getSelectedEntries().length > 0 )
        {
            entry = getSelectedEntries()[0];
        }
        else if ( getSelectedSearchResults().length > 0 )
        {
            entry = getSelectedSearchResults()[0].getEntry();
        }
        else if ( getSelectedBookmarks().length > 0 )
        {
            entry = getSelectedBookmarks()[0].getEntry();
        }

        return entry != null ? entry : null;
    }


    // ── Trooper Identifies the Target Connection ──────────────────────────────
    // The trooper checks whether a connected server is available in the selection —
    // only connected connections are valid import/export targets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IBrowserConnection} for the first selected connection,
     * but only if that connection is currently connected to the LDAP server.
     * Disconnected connections are ignored because we can't import/export without
     * a live server.
     *
     * @return  the browser connection, or {@code null} if none is selected or connected
     */
    protected IBrowserConnection getConnection()
    {
        if ( getSelectedConnections().length > 0
            && getSelectedConnections()[0].getConnectionWrapper().isConnected() )
        {
            Connection connection = getSelectedConnections()[0];
            IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnection( connection );
            return browserConnection;
        }
        else
        {
            return null;
        }
    }


    // ── Trooper Identifies the Target Search ──────────────────────────────────
    // The trooper checks if the target is a saved search rather than a direct entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first selected {@link ISearch}, or {@code null} if no search
     * is selected.
     *
     * @return  the selected search, or {@code null}
     */
    protected ISearch getSearch()
    {
        return getSelectedSearches().length > 0 ? getSelectedSearches()[0] : null;
    }


    // ── Trooper Checks Connection Passed as Input ─────────────────────────────
    // Sometimes the connection is passed as the editor/view input rather than
    // a selection — the trooper checks that channel too.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IBrowserConnection} from the current workbench input,
     * if the input is an {@link IBrowserConnection} (as opposed to an entry or search).
     *
     * @return  the connection input, or {@code null} if the input is not a connection
     */
    protected IBrowserConnection getConnectionInput()
    {
        if ( getInput() instanceof IBrowserConnection )
        {
            return ( IBrowserConnection ) getInput();
        }
        else
        {
            return null;
        }
    }
}
