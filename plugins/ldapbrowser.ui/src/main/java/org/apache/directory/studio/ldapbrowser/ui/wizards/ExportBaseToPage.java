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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ExportBaseToPage — LUKE CHOOSES THE DELIVERY POINT ────────────────
// Luke's final briefing question is "where do we deliver the payload?"
// This abstract page answers it: a FileBrowserWidget for picking a path and
// an "overwrite existing file" checkbox. Subclasses supply the file extensions
// and the human-readable format name so error messages are accurate.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for the "To" (destination selection) page in all export wizards.
 * Renders a {@link FileBrowserWidget} for picking a file path and an overwrite
 * checkbox. Validates the path in real time: errors if blank, if it's a directory,
 * if it exists and overwrite isn't checked, or if the parent directory isn't writable.
 * Think of Luke specifying the Rebel base coordinates where the cargo is delivered:
 * the address must be valid and accessible before the mission can proceed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class ExportBaseToPage extends WizardPage
{

    /** The wizard. */
    protected ExportBaseWizard wizard;

    /** The file browser widget. */
    protected FileBrowserWidget fileBrowserWidget;

    /** The overwrite file button. */
    protected Button overwriteFileButton;


    // ── Luke Checks the Delivery Brief ───────────────────────────────────────────
    // The To page knows the format name before the wizard opens so it can show
    // "Please enter a CSV file path" rather than a generic message.
    // We start incomplete because no file has been chosen yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportBaseToPage. The title and description include the format
     * name returned by {@link #getFileType()} so users see "Please enter a CSV file"
     * rather than a generic message. Starts incomplete — the user must pick a file.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportBaseToPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName );
        setPageComplete( false );
        setTitle( NLS.bind( Messages.getString( "ExportBaseToPage.FileType" ), getFileType() ) ); //$NON-NLS-1$
        setDescription( NLS.bind( Messages.getString( "ExportBaseToPage.PleaseEnterTargetFile" ), getFileType() ) ); //$NON-NLS-1$

        this.wizard = wizard;
    }


    /**
     * Validates this page. This method is responsible for displaying errors,
     * as well as enabling/disabling the "Finish" button
     */
    protected void validate()
    {
        boolean ok = true;
        File file = new File( fileBrowserWidget.getFilename() );
        if ( "".equals( fileBrowserWidget.getFilename() ) ) //$NON-NLS-1$
        {
            setErrorMessage( null );
            ok = false;
        }
        else if ( file.isDirectory() )
        {
            setErrorMessage( NLS.bind(
                Messages.getString( "ExportBaseToPage.ErrorNotAFile" ), new String[] { getFileType() } ) ); //$NON-NLS-1$
            ok = false;
        }
        else if ( file.exists() && !overwriteFileButton.getSelection() )
        {
            setErrorMessage( NLS
                .bind(
                    Messages.getString( "ExportBaseToPage.ErrorFileExists" ), new String[] { getFileType(), getFileType(), getFileType() } ) ); //$NON-NLS-1$
            ok = false;
        }
        else if ( file.exists() && !file.canWrite() )
        {
            setErrorMessage( NLS.bind(
                Messages.getString( "ExportBaseToPage.ErrorFileNotWritable" ), new String[] { getFileType() } ) ); //$NON-NLS-1$
            ok = false;
        }
        else if ( file.getParentFile() == null )
        {
            setErrorMessage( NLS.bind(
                Messages.getString( "ExportBaseToPage.ErrorDirectoryNotWritable" ), new String[] { getFileType() } ) ); //$NON-NLS-1$
            ok = false;
        }

        if ( ok )
        {
            setErrorMessage( null );
        }

        setPageComplete( ok && wizard.getExportFilename() != null && !"".equals( wizard.getExportFilename() ) ); //$NON-NLS-1$
    }


    // ── Luke Builds the Delivery Address Panel ────────────────────────────────────
    // The panel shows a labelled file-browser widget and an overwrite checkbox.
    // Every change fires validation so the Finish button tracks the state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI: a labelled {@link FileBrowserWidget} for picking the
     * destination file, and an "Overwrite existing file" checkbox.
     * Change events on the file browser update the wizard's exportFilename and
     * trigger re-validation. The overwrite checkbox also triggers re-validation.
     *
     * @param composite  the parent composite (subclasses pass a pre-created container).
     */
    public void createControl( Composite composite )
    {
        // Export file
        BaseWidgetUtils.createLabel( composite, NLS.bind(
            Messages.getString( "ExportBaseToPage.FileTypeColon" ), getFileType() ), 1 ); //$NON-NLS-1$
        fileBrowserWidget = new FileBrowserWidget( NLS.bind(
            Messages.getString( "ExportBaseToPage.SelectFileType" ), new String[] { getFileType() } ), //$NON-NLS-1$
            getExtensions(), FileBrowserWidget.TYPE_SAVE );
        fileBrowserWidget.createWidget( composite );
        fileBrowserWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                wizard.setExportFilename( fileBrowserWidget.getFilename() );
                validate();
            }
        } );
        BaseWidgetUtils.createRadioIndent( composite, 1 );
        overwriteFileButton = BaseWidgetUtils.createCheckbox( composite, NLS.bind( Messages
            .getString( "ExportBaseToPage.OverwriteExistingFile" ), new String[] { getFileType() } ), 2 ); //$NON-NLS-1$
        overwriteFileButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                validate();
            }
        } );

        fileBrowserWidget.setFocus();
        setControl( composite );
        validate();
    }


    // ── Luke Checks the Allowed File Types ───────────────────────────────────────
    // The file-browser dialog only shows files matching the export format's
    // extensions so the user can quickly find the right location.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file-extension patterns accepted by the save dialog (e.g. "*.csv", "*").
     * Subclasses must override this to return the extensions appropriate for their format.
     *
     * @return  an array of glob patterns (e.g., {@code new String[]{"*.csv","*"}}).
     */
    protected abstract String[] getExtensions();


    // ── Luke Checks the Format Name ───────────────────────────────────────────────
    // The format name is used in labels and error messages ("Please enter a CSV file").
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable file-format name used in page titles and error messages.
     * Subclasses must override this (e.g., return "CSV", "LDIF", "DSML").
     *
     * @return  the format name string.
     */
    protected abstract String getFileType();


    // ── Luke Saves the File Path for Next Time ────────────────────────────────────
    // After delivering the package, Luke logs the delivery address so next time
    // he can start from where he left off.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the FileBrowserWidget's current path to dialog settings so the
     * file browser opens in the same directory next time this wizard is used.
     */
    public void saveDialogSettings()
    {
        fileBrowserWidget.saveDialogSettings();
    }

}
