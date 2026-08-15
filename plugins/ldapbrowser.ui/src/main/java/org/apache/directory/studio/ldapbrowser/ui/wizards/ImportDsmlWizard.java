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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import java.io.File;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserCategory;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.jobs.ImportDsmlRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ImportDsmlWizard — C-3PO READS THE XML SCROLL TO THE SERVER ───────
// C-3PO receives a DSML XML scroll from another system and translates it
// aloud to the LDAP server — each DSML operation is executed in order.
// The server's responses are then optionally saved to a response file
// so we can verify what happened. Unlike LDIF import, DSML can optionally
// save the full server response, which is uniquely useful for debugging.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * One-page wizard that imports a DSML XML file into an LDAP server.
 * The main page lets the user select the source DSML file, the target connection,
 * and optionally a response file to capture server output. The wizard dispatches
 * an {@link ImportDsmlRunnable} (with or without a response file) as an async
 * background job. Two constructors: one for the Eclipse IImportWizard entry point
 * (no pre-set connection) and one for programmatic use (pre-set connection).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportDsmlWizard extends Wizard implements IImportWizard
{
    /** The connection attached to the import */
    private IBrowserConnection importConnection;

    /** The main page of the wizard */
    private ImportDsmlMainWizardPage mainPage;

    /** The DSML Filename */
    private String dsmlFilename;

    /** The Save Filename */
    private String responseFilename;

    /** The Save Response flag */
    private boolean saveResponse;


    // ── C-3PO Introduces Himself ──────────────────────────────────────────────────
    // The no-arg constructor is used when Eclipse opens the wizard from the
    // File → Import menu; no connection is pre-selected.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ImportDsmlWizard with the localised "DSML Import" title.
     * The import connection will be derived from the current selection in
     * {@link #init}.
     */
    public ImportDsmlWizard()
    {
        super();
        setWindowTitle( Messages.getString( "ImportDsmlWizard.DSMLImport" ) ); //$NON-NLS-1$
    }


    // ── C-3PO Accepts a Pre-Selected Audience ─────────────────────────────────────
    // When triggered by an action that already knows the target connection,
    // the connection is passed directly so the UI pre-populates it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ImportDsmlWizard with the localised "DSML Import" title and
     * a pre-selected target connection.
     *
     * @param selectedConnection  the connection to pre-populate on the main page.
     */
    public ImportDsmlWizard( IBrowserConnection selectedConnection )
    {
        setWindowTitle( Messages.getString( "ImportDsmlWizard.DSMLImport" ) ); //$NON-NLS-1$
        this.importConnection = selectedConnection;
    }


    // ── C-3PO Announces His Registry ID ──────────────────────────────────────────
    // The DSML import wizard is reachable by constant ID from any action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the import DSML wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_IMPORT_DSML;
    }


    // ── C-3PO Sends the DSML Commands ────────────────────────────────────────────
    // If the DSML file is set, we launch the import job — with or without
    // saving the server response, depending on the saveResponse flag.
    // Returns false (wizard stays open) if no DSML file was selected.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings and launches the async {@link ImportDsmlRunnable}.
     * If the saveResponse flag is true, a response File is also passed to the
     * runnable so it captures the server's XML response. Returns {@code false}
     * if no DSML filename is set (shouldn't happen after successful validation).
     *
     * @return  {@code true} if the import job was launched; {@code false} if no file was set.
     */
    public boolean performFinish()
    {
        mainPage.saveDialogSettings();

        if ( dsmlFilename != null && !"".equals( dsmlFilename ) ) //$NON-NLS-1$
        {
            File dsmlFile = new File( dsmlFilename );

            if ( saveResponse )
            {
                File responseFile = new File( responseFilename );
                new StudioBrowserJob( new ImportDsmlRunnable( importConnection, dsmlFile, responseFile ) ).execute();
            }
            else
            {
                new StudioBrowserJob( new ImportDsmlRunnable( importConnection, dsmlFile ) ).execute();
            }

            return true;
        }
        return false;
    }


    // ── C-3PO Derives the Target Connection from Context ─────────────────────────
    // When opened from the Eclipse File → Import menu, we look at the current
    // selection to find the best match for a target connection. The first element
    // of the selection is inspected, and we walk the model hierarchy to find the
    // IBrowserConnection it belongs to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Derives the target connection from the current workbench selection.
     * Accepts IEntry, ISearchResult, IBookmark, IAttribute, IValue, IBrowserConnection,
     * Connection, or BrowserCategory as starting points.
     *
     * @param workbench  the current workbench (unused).
     * @param selection  the current structured selection.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        Object o = selection.getFirstElement();
        if ( o instanceof IEntry )
        {
            importConnection = ( ( IEntry ) o ).getBrowserConnection();
        }
        else if ( o instanceof ISearchResult )
        {
            importConnection = ( ( ISearchResult ) o ).getEntry().getBrowserConnection();
        }
        else if ( o instanceof IBookmark )
        {
            importConnection = ( ( IBookmark ) o ).getBrowserConnection();
        }
        else if ( o instanceof IAttribute )
        {
            importConnection = ( ( IAttribute ) o ).getEntry().getBrowserConnection();
        }
        else if ( o instanceof IValue )
        {
            importConnection = ( ( IValue ) o ).getAttribute().getEntry().getBrowserConnection();
        }
        else if ( o instanceof IBrowserConnection )
        {
            importConnection = ( IBrowserConnection ) o;
        }
        else if ( o instanceof Connection )
        {
            importConnection = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnection( ( Connection ) o );
        }
        else if ( o instanceof BrowserCategory )
        {
            importConnection = ( ( BrowserCategory ) o ).getParent();
        }
        else
        {
            importConnection = null;
        }
    }


    // ── C-3PO Opens the Script ────────────────────────────────────────────────────
    // The single main page covers all import options: source file, connection,
    // and optional response capture.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the single {@link ImportDsmlMainWizardPage}.
     */
    public void addPages()
    {
        mainPage = new ImportDsmlMainWizardPage( ImportDsmlMainWizardPage.class.getName(), this );
        addPage( mainPage );
    }


    // ── C-3PO Connects the Help System ────────────────────────────────────────────
    // F1 on the page opens the DSML import help article.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Registers the DSML import help context ID on the main page.
     *
     * @param pageContainer  the wizard page container.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( mainPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_dsmlimport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── C-3PO Reads the Assigned Connection ──────────────────────────────────────
    // The main page reads this to pre-populate the connection dropdown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the target connection for the import.
     * Read by the main page to pre-populate the connection dropdown and by
     * {@code performFinish()} to pass to the runnable.
     *
     * @return  the import connection, or {@code null} if not yet set.
     */
    public IBrowserConnection getImportConnection()
    {
        return importConnection;
    }


    // ── C-3PO Stores the Target Connection ───────────────────────────────────────
    // Called by the main page when the user changes the connection dropdown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the target connection. Called by the main page's connection-widget
     * listener when the user selects a different connection.
     *
     * @param connection  the new target connection.
     */
    public void setImportConnection( IBrowserConnection connection )
    {
        this.importConnection = connection;
    }


    // ── C-3PO Notes the Source File ───────────────────────────────────────────────
    // The main page stores the DSML source path here as the user types it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DSML source file path. Called by the main page's file-browser widget
     * listener.
     *
     * @param dsmlFilename  the path to the DSML XML file to import.
     */
    public void setDsmlFilename( String dsmlFilename )
    {
        this.dsmlFilename = dsmlFilename;
    }


    // ── C-3PO Notes the Response File ────────────────────────────────────────────
    // If the user wants to capture the server response, they provide a path here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the response file path where the server's DSML response will be written.
     * Only used if {@link #setSaveResponse} is set to {@code true}.
     *
     * @param saveFilename  the path to the response output file.
     */
    public void setResponseFilename( String saveFilename )
    {
        this.responseFilename = saveFilename;
    }


    // ── C-3PO Marks Whether to Save the Response ─────────────────────────────────
    // The "Save Response" checkbox on the main page drives this flag.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether to save the server's DSML response to a file.
     * Called by the main page's "Save Response" checkbox listener.
     *
     * @param b  {@code true} to write the server response to {@code responseFilename}.
     */
    public void setSaveResponse( boolean b )
    {
        this.saveResponse = b;
    }
}
