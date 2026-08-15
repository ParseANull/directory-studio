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
package org.apache.directory.studio.openldap.config.actions;


import java.io.File;

import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.DirectoryDialog;

import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.model.OpenLdapConfiguration;
import org.apache.directory.studio.openldap.config.model.io.ConfigurationReader;


// ── CLASS: EditorImportConfigurationAction — Clone Troopers Execute Order 66 ─
// In Revenge of the Sith, Palpatine issues Order 66 and clone troopers execute
// their mission swiftly and completely — but unlike the export action, this one
// has a heavier consequence: it overwrites the existing state. Before a trooper
// fires, they make absolutely sure the order is real and the target is confirmed.
// This action imports a configuration directory into the editor — overwriting the
// current configuration in memory. We check for unsaved changes, prompt for
// confirmation, validate the chosen directory, then read and load the new
// configuration. At every step we guard against mistakes before overwriting.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse {@link Action} that imports an OpenLDAP server configuration from a
 * user-selected slapd.d directory, replacing the editor's current configuration.
 * Because this is a destructive operation (it discards what's currently open), we
 * prompt the user to confirm before overwriting both unsaved changes and the
 * existing configuration.
 * Think of this as the clone troopers acting on Order 66 — swift, decisive, but
 * not without first confirming the order is genuine and the situation is clear.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorImportConfigurationAction extends Action
{
    /** The editor into which we'll load the imported configuration. */
    private OpenLdapServerConfigurationEditor editor;


    // ── The Trooper Receives Their Assignment ─────────────────────────────────
    // Clone CT-7567 (Captain Rex) knows which unit he's attached to. Before
    // executing any operation, he needs to know which editor context he's working
    // within. We store the editor reference for use when the action fires.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new import action bound to the given editor.
     * We hold onto the editor reference so that when the action fires, we know
     * where to load the imported configuration into.
     *
     * @param editor  the OpenLDAP server configuration editor to import into
     */
    public EditorImportConfigurationAction( OpenLdapServerConfigurationEditor editor )
    {
        this.editor = editor;
    }


    // ── The Trooper Puts On Their Insignia ────────────────────────────────────
    // Import and export are different operations that need different insignia
    // so the user can tell them apart at a glance. We return the import icon.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action — the import icon shown in the editor toolbar.
     *
     * @return  the import image descriptor
     */
    public ImageDescriptor getImageDescriptor()
    {
        return OpenLdapConfigurationPlugin.getDefault().getImageDescriptor(
            OpenLdapConfigurationPluginConstants.IMG_IMPORT );
    }


    // ── The Trooper States Their Mission ──────────────────────────────────────
    // The label shown to the user in menus and tooltips identifies this as the
    // import operation, distinct from export.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action as shown in menus and tooltips.
     *
     * @return  "Import Configuration"
     */
    public String getText()
    {
        return "Import Configuration";
    }


    // ── The Trooper Verifies The Order Then Executes ───────────────────────────
    // A clone trooper never executes Order 66 on hearsay — they verify the
    // authorization (check for unsaved changes), confirm the target (validate the
    // directory), get final confirmation from command (overwrite dialog), and only
    // then do they act. We follow the same careful sequence before loading the
    // new configuration over the old one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the import: prompts for confirmation if there are unsaved changes,
     * opens a directory chooser, validates the chosen directory, asks the user to
     * confirm the overwrite, then reads and loads the new configuration into the editor.
     * Every guard clause is there to prevent accidental data loss — this is a
     * destructive operation and we treat it accordingly.
     * If anything fails (invalid directory, parse error, I/O error), we show an
     * error dialog and leave the editor's current state untouched.
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
                        "Unsaved Modifications",
                        "The configuration has unsaved modifications. All recent changes will be lost. Are you sure you want to continue?" ) )
                {
                    return;
                }
            }

            // The path of the directory
            String path = null;

            // Opening a dialog for directory selection
            DirectoryDialog dialog = new DirectoryDialog( editor.getSite().getShell() );
            dialog.setText( "Select Configuration Directory" );
            dialog.setFilterPath( System.getProperty( "user.home" ) );

            while ( true )
            {
                // Opening the dialog
                path = dialog.open();

                // Checking the returned path
                if ( path == null )
                {
                    // Cancel button has been clicked
                    return;
                }

                // Getting the directory indicated by the user
                final File directory = new File( path );

                // Checking if the directory exists
                if ( !directory.exists() )
                {
                    CommonUIUtils.openErrorDialog( "The directory does not exist." );
                    continue;
                }

                // Checking if the location is a directory
                if ( !directory.isDirectory() )
                {
                    CommonUIUtils.openErrorDialog( "The location is not a directory." );
                    continue;
                }

                // Checking if the directory is writable
                if ( !directory.canRead() )
                {
                    CommonUIUtils.openErrorDialog( "The directory is not writable." );
                    continue;
                }

                // The directory meets all requirements
                break;
            }

            // Checking the directory
            File configurationDirectory = new File( path );
            if ( !configurationDirectory.exists() || !configurationDirectory.isDirectory()
                || !configurationDirectory.canRead() )
            {
                // This is not a valid directory
                return;
            }

            // Requiring a confirmation from the user
            if ( !MessageDialog
                .openConfirm(
                    editor.getSite().getShell(),
                    "Overwrite Existing Configuration",
                    "Are you sure you want to overwrite the existing configuration with the contents of the selected file?" ) )
            {
                return;
            }

            // Reading the configuration of the file
            OpenLdapConfiguration configuration = ConfigurationReader.readConfiguration( configurationDirectory );

            // Resetting the configuration back to the editor
            editor.resetConfiguration( configuration );
        }
        catch ( Exception e )
        {
            MessageDialog
                .openError(
                    editor.getSite().getShell(),
                    "Error Importing Configuration File",
                    NLS.bind(
                        "An error occurred when importing the selected file:\n{0}\n\nIt does not seem to be a correct LDIF configuration file.",
                        e.getMessage() ) );
        }
    }
}
