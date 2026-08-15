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

package org.apache.directory.studio.apacheds.configuration.actions;


import java.io.File;

import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.apache.directory.studio.apacheds.configuration.editor.Configuration;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditor;
import org.apache.directory.studio.apacheds.configuration.jobs.LoadConfigurationRunnable;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;


// ── CLASS: EditorImportConfigurationAction — REBEL COURIER DELIVERS NEW PLANS TO THE EDITOR
// A Rebel agent retrieves a different set of plans from another source and brings them into
// the engineering room.  The old plans are discarded after a confirmation; the new ones replace
// them and the editor reloads with the imported configuration.
// This action opens a file-selection dialog (workspace tree in IDE mode, native file dialog in
// RCP mode), asks for confirmation, reads the selected file, and resets the editor's configuration.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Toolbar/menu action in the {@link ServerConfigurationEditor} that imports a configuration
 * from a user-chosen LDIF file, replacing the editor's current configuration.
 * Handles both IDE (workspace file picker) and RCP (native file dialog) environments.
 * Logs and displays an error dialog if the operation fails.
 * Think of it as the Rebel courier delivering a replacement set of plans.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorImportConfigurationAction extends Action
{
    private static final String DIALOG_TITLE = Messages
        .getString( "EditorImportConfigurationAction.SelectConfigurationFile" ); //$NON-NLS-1$

    /** The associated editor */
    private ServerConfigurationEditor editor;


    // ── Wiring Up The Action To Its Editor ────────────────────────────────────────────────────
    // The action needs a reference to the editor to check for unsaved changes and reset config.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the import action and stores a reference to its owning editor.
     *
     * @param editor  the configuration editor that owns this action
     */
    public EditorImportConfigurationAction( ServerConfigurationEditor editor )
    {
        this.editor = editor;
    }


    // ── Returning The Import Icon ─────────────────────────────────────────────────────────────
    // The import icon appears on the toolbar button for this action.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the import icon image descriptor.
     *
     * @return the image descriptor for the import toolbar icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return ApacheDS2ConfigurationPlugin.getDefault().getImageDescriptor(
            ApacheDS2ConfigurationPluginConstants.IMG_IMPORT );
    }


    // ── Returning The Action's Menu Label ────────────────────────────────────────────────────
    // The localised label appears in the editor's toolbar dropdown and context menu.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised action label ("Import Configuration").
     *
     * @return the action text
     */
    public String getText()
    {
        return Messages.getString( "EditorImportConfigurationAction.ImportConfiguration" ); //$NON-NLS-1$
    }


    // ── Running The Import ────────────────────────────────────────────────────────────────────
    // When the engineer clicks "Import":
    //   1. If the editor has unsaved changes, ask for confirmation before discarding them.
    //   2. Open a file-selection dialog (IDE: workspace tree; RCP: native dialog).
    //   3. Ask for final confirmation that the existing configuration will be overwritten.
    //   4. Read the selected file as a Configuration object.
    //   5. Reset the editor to display the new configuration.
    // Logs and shows an error dialog if anything goes wrong.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Runs the import flow: checks for unsaved changes, prompts for file selection, confirms
     * the overwrite, reads the configuration file, and resets the editor.
     * Logs and displays an error dialog on failure.
     *
     * <p>For example — importing a backup configuration:</p>
     * <pre>
     *   run() → confirm discard if dirty → file dialog → confirm overwrite
     *         → LoadConfigurationRunnable.readConfiguration(file)
     *         → editor.resetConfiguration(configuration)
     * </pre>
     */
    public void run()
    {
        try
        {
            // Checking if the editor has unsaved modifications
            if ( editor.isDirty() )
            {
                // Requiring a confirmation from the user before discarding the unsaved modifications
                if ( !MessageDialog
                    .openConfirm(
                        editor.getSite().getShell(),
                        Messages.getString( "EditorImportConfigurationAction.UnsavedModifications" ), //$NON-NLS-1$
                        Messages
                            .getString( "EditorImportConfigurationAction.ConfigurationHasUnsavedModificationsSureToContinue" ) ) ) //$NON-NLS-1$
                {
                    return;
                }
            }

            // The file that will be used to load the configuration
            File file = null;

            // detect IDE or RCP:
            boolean isIDE = CommonUIUtils.isIDEEnvironment();

            if ( isIDE )
            {
                // Opening a dialog for file selection
                ElementTreeSelectionDialog dialog = createWorkspaceFileSelectionDialog();

                if ( dialog.open() == Dialog.OK )
                {
                    // Getting the input stream for the selected file
                    Object firstResult = dialog.getFirstResult();

                    if ( firstResult instanceof IFile )
                    {
                        file = ( ( IFile ) firstResult ).getLocation().toFile();
                    }
                }
                else
                {
                    // Cancel button has been clicked
                    return;
                }
            }
            else
            {
                // Opening a dialog for file selection
                FileDialog dialog = new FileDialog( editor.getSite().getShell(), SWT.OPEN | SWT.SINGLE );
                dialog.setText( DIALOG_TITLE );
                dialog.setFilterPath( System.getProperty( "user.home" ) ); //$NON-NLS-1$
                String filePath = dialog.open();

                if ( filePath == null )
                {
                    // Cancel button has been clicked
                    return;
                }

                // Checking the file
                file = new File( filePath );

                if ( !file.exists() || !file.isFile() || !file.canRead() )
                {
                    // This is not a valid file
                    return;
                }
            }

            // Checking if we found an input stream
            if ( file == null )
            {
                return;
            }

            // Requiring a confirmation from the user
            if ( !MessageDialog
                .openConfirm(
                    editor.getSite().getShell(),
                    Messages.getString( "EditorImportConfigurationAction.OverwriteExistingConfiguration" ), //$NON-NLS-1$
                    Messages
                        .getString( "EditorImportConfigurationAction.AreYouSureYouWantToOverwriteTheExistingConfiguration" ) ) ) //$NON-NLS-1$
            {
                return;
            }

            // Reading the configuration of the file
            Configuration configuration = LoadConfigurationRunnable.readConfiguration( file );

            // Resetting the configuration back to the editor
            editor.resetConfiguration( configuration );
        }
        catch ( Exception e )
        {
            ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                new Status( Status.ERROR, "org.apache.directory.studio.apacheds.configuration", //$NON-NLS-1$
                    e.getMessage() ) );

            MessageDialog
                .openError(
                    editor.getSite().getShell(),
                    Messages.getString( "EditorImportConfigurationAction.ErrorImportingConfigurationFile" ), //$NON-NLS-1$
                    NLS.bind(
                        Messages
                            .getString( "EditorImportConfigurationAction.AnErrorOccurredWhenImportingTheSelectedFile" ), //$NON-NLS-1$
                        e.getMessage() ) );
        }
    }


    // ── Building The IDE-Mode Workspace File Selection Dialog ─────────────────────────────────
    // In IDE mode we use an Eclipse ElementTreeSelectionDialog that shows the full workspace
    // tree.  The validator ensures only an IFile (not a folder) can be selected.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an Eclipse workspace file-selection dialog restricted to single file selection.
     * The built-in validator rejects folders and empty selections (OK stays disabled for those).
     *
     * @return the configured dialog (not yet opened)
     */
    private ElementTreeSelectionDialog createWorkspaceFileSelectionDialog()
    {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog( editor.getSite().getShell(),
            new WorkbenchLabelProvider(), new WorkbenchContentProvider() );
        dialog.setInput( ResourcesPlugin.getWorkspace().getRoot() );
        dialog.setMessage( Messages.getString( "EditorImportConfigurationAction.SelectConfigurationFileToImport" ) ); //$NON-NLS-1$
        dialog.setTitle( DIALOG_TITLE );
        dialog.setAllowMultiple( false );
        dialog.setStatusLineAboveButtons( false );
        dialog.setValidator( new ISelectionStatusValidator()
        {
            /** The validated status */
            private Status validated = new Status( IStatus.OK, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                IStatus.OK, "", null ); //$NON-NLS-1$

            /** The not validated status */
            private Status notValidated = new Status( IStatus.ERROR, ApacheDS2ConfigurationPluginConstants.PLUGIN_ID,
                IStatus.ERROR, "", null ); //$NON-NLS-1$

            public IStatus validate( Object[] selection )
            {
                if ( selection != null )
                {
                    if ( selection.length > 0 )
                    {
                        Object selectedObject = selection[0];
                        if ( selectedObject instanceof IFile )
                        {
                            return validated;
                        }
                    }
                }

                return notValidated;
            }
        } );

        return dialog;
    }
}
