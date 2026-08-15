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


import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditor;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditorUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;


// ── CLASS: EditorExportConfigurationAction — IMPERIAL DATA COURIER: EXTRACT THE SCHEMATICS ─
// When the Rebellion needs a copy of the Death Star plans, Cassian extracts them and hands
// off the data card to a courier.  The original stays in place; the exported copy goes
// wherever the engineer wants it.
// This action does exactly that: it triggers a "Save As…" dialog so the engineer can export
// the current configuration to a different file location without altering the original.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Toolbar/menu action in the {@link ServerConfigurationEditor} that exports the current
 * configuration to a user-chosen file via a "Save As…" dialog.
 * Logs and displays an error dialog if the export fails.
 * Think of it as the Imperial data courier: extract the schematics and deliver them elsewhere.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorExportConfigurationAction extends Action
{
    /** The associated editor */
    private ServerConfigurationEditor editor;


    // ── Wiring Up The Action To Its Editor ────────────────────────────────────────────────────
    // The action needs a reference to the editor so it can pull the configuration and shell.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the export action and stores a reference to its owning editor.
     *
     * @param editor  the configuration editor that owns this action
     */
    public EditorExportConfigurationAction( ServerConfigurationEditor editor )
    {
        this.editor = editor;
    }


    // ── Returning The Export Icon ─────────────────────────────────────────────────────────────
    // The export icon appears on the toolbar button for this action.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the export icon image descriptor.
     *
     * @return the image descriptor for the export toolbar icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return ApacheDS2ConfigurationPlugin.getDefault().getImageDescriptor(
            ApacheDS2ConfigurationPluginConstants.IMG_EXPORT );
    }


    // ── Returning The Action's Menu Label ────────────────────────────────────────────────────
    // The localised label appears in the editor's toolbar dropdown and context menu.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised action label ("Export Configuration").
     *
     * @return the action text
     */
    public String getText()
    {
        return Messages.getString( "EditorExportConfigurationAction.ExportConfiguration" ); //$NON-NLS-1$
    }


    // ── Running The Export ────────────────────────────────────────────────────────────────────
    // When the engineer clicks "Export", we delegate to ServerConfigurationEditorUtils.saveAs(),
    // which opens a Save-As dialog and writes the configuration to the chosen location.
    // If anything goes wrong, we log it and show an error dialog with the exception message.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a "Save As…" dialog and writes the current configuration to the selected file.
     * Logs and displays an error dialog if the operation fails.
     *
     * <p>For example — Cassian copies the plans to a data card:</p>
     * <pre>
     *   run() → saveAs(config, targetFile) → writes config.ldif to the chosen location
     *   On error → log + error dialog → user can retry
     * </pre>
     */
    public void run()
    {
        try
        {
            ServerConfigurationEditorUtils.saveAs( new NullProgressMonitor(), editor.getSite()
                .getShell(), editor.getEditorInput(), editor.getConfigWriter(), editor.getConfiguration(), false );
        }
        catch ( Exception e )
        {
            ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                new Status( Status.ERROR, "org.apache.directory.studio.apacheds.configuration", //$NON-NLS-1$
                    e.getMessage() ) );
            MessageDialog
                .openError( editor.getSite().getShell(),
                    Messages.getString( "EditorExportConfigurationAction.ErrorExportingConfigurationFile" ), //$NON-NLS-1$
                    NLS.bind(
                        Messages
                            .getString( "EditorExportConfigurationAction.AnErrorOccurredWhenExportingTheSelectedFile" ), e.getMessage() ) ); //$NON-NLS-1$
        }
    }
}
