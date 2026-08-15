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

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.FileBrowserWidget;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ImportConnectionsWizardPage — HAN READS THE CARGO MANIFEST ────────
// Han stands at the docking bay entrance: "Which archive are we unloading?"
// This page asks for the source .lbc archive to import. Unlike the export page,
// there's no overwrite checkbox — importing always adds new connections to the
// existing set (duplicate detection happens in the wizard's performFinish).
// Validation checks that the file exists, is a file (not a directory),
// and is readable.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The single wizard page for the import connections wizard.
 * Shows a FileBrowserWidget for picking a source .lbc archive (TYPE_OPEN).
 * Validates in real time: errors if blank, if the file doesn't exist, if it's
 * a directory, or if it's not readable.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportConnectionsWizardPage extends WizardPage
{
    private FileBrowserWidget fileBrowserWidget;


    // ── Han Opens the Cargo Bay Manifest ─────────────────────────────────────────
    // Title, description, and wizard icon set. Page starts incomplete — no file
    // chosen yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ImportConnectionsWizardPage with title, description, and
     * the import-connections wizard icon. Starts incomplete — the user must pick
     * a source archive before the Finish button enables.
     */
    protected ImportConnectionsWizardPage()
    {
        super( ImportConnectionsWizardPage.class.getName() );
        setTitle( Messages.getString( "ImportConnectionsWizardPage.ImportConnections" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ImportConnectionsWizardPage.ImportConnectionsFromFilesystem" ) ); //$NON-NLS-1$
        setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor(
            BrowserUIConstants.IMG_IMPORT_CONNECTIONS_WIZARD ) );
        setPageComplete( false );
    }


    // ── Han Lays Out the Source File Panel ────────────────────────────────────────
    // A three-column grid with a label and a file-open browser widget.
    // No overwrite checkbox since importing adds to the set, not replaces it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI: a three-column grid with a "From File" label and a
     * {@link FileBrowserWidget} pre-filtered to *.lbc archives (TYPE_OPEN mode).
     * Change events trigger validation so the Finish button tracks live state.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        // Main Composite
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        // From File
        BaseWidgetUtils.createLabel( composite, Messages.getString( "ImportConnectionsWizardPage.FromFile" ), 1 ); //$NON-NLS-1$
        fileBrowserWidget = new FileBrowserWidget(
            Messages.getString( "ImportConnectionsWizardPage.ChooseFile" ), new String[] //$NON-NLS-1$
            { "*.lbc", "*" }, FileBrowserWidget.TYPE_OPEN ); //$NON-NLS-1$
        fileBrowserWidget.createWidget( composite );
        fileBrowserWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        setControl( composite );
    }


    /**
     * Validates this page. This method is responsible for displaying errors,
     * as well as enabling/disabling the "Finish" button
     */
    private void validate()
    {
        boolean ok = true;
        File file = new File( fileBrowserWidget.getFilename() );
        if ( "".equals( fileBrowserWidget.getFilename() ) ) //$NON-NLS-1$
        {
            setErrorMessage( null );
            ok = false;
        }
        else if ( !file.exists() )
        {
            setErrorMessage( Messages.getString( "ImportConnectionsWizardPage.ErrorFileNotExists" ) ); //$NON-NLS-1$
            ok = false;
        }
        else if ( file.isDirectory() )
        {
            setErrorMessage( Messages.getString( "ImportConnectionsWizardPage.ErrorFileNotFile" ) ); //$NON-NLS-1$
            ok = false;
        }
        else if ( file.exists() && !file.canRead() )
        {
            setErrorMessage( Messages.getString( "ImportConnectionsWizardPage.ErrorFileNotReadable" ) ); //$NON-NLS-1$
            ok = false;
        }

        if ( ok )
        {
            setErrorMessage( null );
        }

        setPageComplete( ok );
    }


    // ── Han Reads the Chosen Archive Path ────────────────────────────────────────
    // The wizard needs the path to open the ZipFile in performFinish().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the source archive file path chosen by the user.
     * Used by {@link ImportConnectionsWizard#performFinish()} to open the
     * ZIP archive.
     *
     * @return  the selected source file path.
     */
    public String getImportFileName()
    {
        return fileBrowserWidget.getFilename();
    }


    // ── Han Logs the Source for Next Time ────────────────────────────────────────
    // The file browser remembers the last directory so next import starts nearby.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the FileBrowserWidget's current directory to dialog settings
     * so the file browser opens in the same location next time.
     */
    public void saveDialogSettings()
    {
        fileBrowserWidget.saveDialogSettings();
    }
}
