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
import org.apache.directory.studio.ldapbrowser.core.jobs.ImportLdifRunnable;
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


// ── CLASS: ImportLdifWizard — C-3PO READS THE LDIF SCROLL TO THE SERVER ──────
// C-3PO receives a stack of LDIF paper (the native language of LDAP servers)
// and reads each operation to the server in order: add, modify, delete.
// The LDIF import wizard adds two extras that the DSML import lacks:
// logging (so every operation and its outcome is written to a log file) and
// two error-handling options (update if entry exists; continue on error).
// It's the most full-featured of all the import wizards.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * One-page wizard that imports an LDIF file into an LDAP server.
 * The main page lets the user select the source LDIF file, the target connection,
 * optional logging (default or custom log file), and two options:
 * "update if entry exists" and "continue on error". The wizard dispatches an
 * {@link ImportLdifRunnable} as an async background job. Two constructors:
 * no-arg for the Eclipse IImportWizard entry point, and a constructor that
 * accepts a pre-selected connection.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportLdifWizard extends Wizard implements IImportWizard
{

    /** The main page. */
    private ImportLdifMainWizardPage mainPage;

    /** The ldif filename. */
    private String ldifFilename;

    /** The import connection. */
    private IBrowserConnection importConnection;

    /** The enable logging flag. */
    private boolean enableLogging;

    /** The log filename. */
    private String logFilename;

    /** The update if entry exists flag. */
    private boolean updateIfEntryExists;

    /** The continue on error flag. */
    private boolean continueOnError;


    // ── C-3PO Announces Himself: No Pre-Set Connection ────────────────────────────
    // When opened from File → Import, the connection is derived from the current
    // workbench selection in init().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ImportLdifWizard with the localised "LDIF Import" title.
     * The import connection will be derived from the current selection in
     * {@link #init}.
     */
    public ImportLdifWizard()
    {
        super();
        setWindowTitle( Messages.getString( "ImportLdifWizard.LDIFImport" ) ); //$NON-NLS-1$
    }


    // ── C-3PO Accepts a Pre-Set Connection ───────────────────────────────────────
    // When an action already knows the target connection, it passes it here
    // so the UI pre-populates the connection widget.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ImportLdifWizard with a pre-selected target connection.
     *
     * @param importConnection  the connection to pre-populate on the main page.
     */
    public ImportLdifWizard( IBrowserConnection importConnection )
    {
        super.setWindowTitle( Messages.getString( "ImportLdifWizard.LDIFImport" ) ); //$NON-NLS-1$
        this.importConnection = importConnection;
    }


    // ── C-3PO Has a Registry ID ───────────────────────────────────────────────────
    // Actions can open this wizard by constant ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the import LDIF wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_IMPORT_LDIF;
    }


    // ── C-3PO Derives the Target from Context ────────────────────────────────────
    // When opened from the Eclipse File → Import menu, we inspect the first
    // element of the selection to find a suitable IBrowserConnection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Derives the target connection from the current workbench selection.
     * Walks the model hierarchy from any IEntry, ISearchResult, IBookmark,
     * IAttribute, IValue, IBrowserConnection, Connection, or BrowserCategory
     * to find the IBrowserConnection.
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


    // ── C-3PO Opens the Briefing Page ────────────────────────────────────────────
    // One page covers all options: LDIF source, connection, logging, and flags.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the single {@link ImportLdifMainWizardPage}.
     */
    public void addPages()
    {
        mainPage = new ImportLdifMainWizardPage( ImportLdifMainWizardPage.class.getName(), this );
        addPage( mainPage );
    }


    // ── C-3PO Connects the Help System ────────────────────────────────────────────
    // F1 opens the LDIF import help article.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Registers the LDIF import help context ID on the main page.
     *
     * @param pageContainer  the wizard page container.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( mainPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_ldifimport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── C-3PO Reads the LDIF Scroll Aloud ────────────────────────────────────────
    // The import job is dispatched: with a log file if logging is enabled,
    // without if not. Returns false only if no filename was set (shouldn't
    // happen after successful validation).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings and launches an async {@link ImportLdifRunnable}.
     * Uses the two-argument constructor if logging is disabled, or the four-argument
     * constructor (with log file, update flag, continue-on-error flag) if logging
     * is enabled.
     *
     * @return  {@code true} if the job launched; {@code false} if no LDIF filename is set.
     */
    public boolean performFinish()
    {
        mainPage.saveDialogSettings();

        if ( ldifFilename != null && !"".equals( ldifFilename ) ) //$NON-NLS-1$
        {
            File ldifFile = new File( ldifFilename );

            if ( enableLogging )
            {
                File logFile = new File( logFilename );
                new StudioBrowserJob( new ImportLdifRunnable( importConnection, ldifFile, logFile, updateIfEntryExists,
                    continueOnError ) ).execute();
            }
            else
            {
                new StudioBrowserJob( new ImportLdifRunnable( importConnection, ldifFile, updateIfEntryExists,
                    continueOnError ) ).execute();
            }

            return true;
        }
        return false;
    }


    // ── C-3PO Gets and Sets the Target Connection ─────────────────────────────────
    // The main page pushes connection changes here via setImportConnection().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the target connection for the import.
     *
     * @return  the import connection, or {@code null} if not yet set.
     */
    public IBrowserConnection getImportConnection()
    {
        return importConnection;
    }


    /**
     * Sets the target connection. Called by the main page's connection-widget listener.
     *
     * @param importConnection  the new target connection.
     */
    public void setImportConnection( IBrowserConnection importConnection )
    {
        this.importConnection = importConnection;
    }


    /**
     * Sets the LDIF source file path. Called by the main page's file-browser widget.
     *
     * @param ldifFilename  the path to the LDIF file to import.
     */
    public void setLdifFilename( String ldifFilename )
    {
        this.ldifFilename = ldifFilename;
    }


    /**
     * Sets the "update if entry exists" flag.
     * When true, the import runnable attempts to update entries that already exist
     * in the server (instead of failing with an "already exists" error).
     *
     * @param updateIfEntryExists  {@code true} to update pre-existing entries.
     */
    public void setUpdateIfEntryExists( boolean updateIfEntryExists )
    {
        this.updateIfEntryExists = updateIfEntryExists;
    }


    /**
     * Sets the "continue on error" flag.
     * When true, the import keeps processing subsequent operations even if one fails.
     *
     * @param continueOnError  {@code true} to continue past errors.
     */
    public void setContinueOnError( boolean continueOnError )
    {
        this.continueOnError = continueOnError;
    }


    /**
     * Sets the log file path for import logging.
     * Only used when enableLogging is {@code true}.
     *
     * @param logFilename  the path to write import log entries.
     */
    public void setLogFilename( String logFilename )
    {
        this.logFilename = logFilename;
    }


    /**
     * Sets the enable-logging flag.
     * When true, every import operation and its outcome is logged to the log file.
     *
     * @param b  {@code true} to enable logging.
     */
    public void setEnableLogging( boolean b )
    {
        this.enableLogging = b;
    }

}
