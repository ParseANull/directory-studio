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


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;

import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditorUtils;


// ── CLASS: EditorExportConfigurationAction — Clone Troopers Execute Order 66 ─
// In Revenge of the Sith, Palpatine issues Order 66 and the clone troopers spring
// into action without hesitation: a single command triggers a swift, decisive
// operation. Each trooper knows exactly what to do — no deliberation, just execution.
// This action class does the same: when the user clicks "Export Configuration",
// we spring into action — call the editor utilities to do a "Save As" operation and
// write the current configuration out to a file the user selects. One click, clean
// execution, error dialog if anything goes wrong.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse {@link Action} that exports the current OpenLDAP server configuration
 * to a file or directory chosen by the user. Bound to the "Export Configuration"
 * toolbar/menu entry in the OpenLDAP configuration editor.
 * We delegate the actual file-writing to
 * {@link OpenLdapServerConfigurationEditorUtils#saveAs}, which shows a file chooser
 * and persists the configuration. If anything goes wrong we show an error dialog.
 * Think of this action as a clone trooper: it receives the command and executes it
 * cleanly and completely.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorExportConfigurationAction extends Action
{
    /** The editor whose current configuration we'll export. */
    private OpenLdapServerConfigurationEditor editor;


    // ── The Trooper Receives Their Assignment ─────────────────────────────────
    // Each clone trooper is assigned to a specific unit. Before they can execute
    // Order 66, they need to know which Jedi general they're assigned to eliminate.
    // We store a reference to the editor so run() knows whose configuration to export.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new export action bound to the given editor.
     * We hold onto the editor reference so that when the action fires, we can
     * read the current configuration from it and export it.
     *
     * @param editor  the OpenLDAP server configuration editor whose config to export
     */
    public EditorExportConfigurationAction( OpenLdapServerConfigurationEditor editor )
    {
        this.editor = editor;
    }


    // ── The Trooper Puts On Their Insignia ────────────────────────────────────
    // Clone troopers wear the Empire's insignia — the visual marker that identifies
    // what this action represents in the UI toolbar. We return the export icon.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action — the export icon shown in the editor toolbar.
     * We look it up from the plugin's image registry using the IMG_EXPORT constant.
     *
     * @return  the export image descriptor for the toolbar/menu button
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return OpenLdapConfigurationPlugin.getDefault().getImageDescriptor(
            OpenLdapConfigurationPluginConstants.IMG_EXPORT );
    }


    // ── The Trooper States Their Mission ──────────────────────────────────────
    // Every trooper knows the name of their operation. This action's label is
    // "Export Configuration" — what the user sees in menus and tooltips.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action as shown in menus and tooltips.
     *
     * @return  "Export Configuration"
     */
    @Override
    public String getText()
    {
        return "Export Configuration";
    }


    // ── The Trooper Executes The Order ────────────────────────────────────────
    // Order received. No deliberation. The clone trooper moves swiftly — calling
    // the saveAs utility to let the user pick a destination and write the config.
    // If anything goes wrong (file I/O error, bad destination), we catch it and
    // show an error dialog so the user knows what happened.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the export: invokes the "Save As" dialog so the user can pick a
     * destination, then writes the current editor configuration there.
     * If the export fails for any reason, we catch the exception and show an
     * error dialog rather than letting the exception bubble up into the Eclipse
     * workbench framework.
     */
    @Override
    public void run()
    {
        try
        {
            OpenLdapServerConfigurationEditorUtils.saveAs( editor.getConfiguration(), editor.getSite()
                .getShell(), false );
        }
        catch ( Exception e )
        {
            MessageDialog.openError( editor.getSite().getShell(), "Error Exporting Configuration File",
                NLS.bind( "An error occurred when exporting the selected file:\n{0}", e.getMessage() ) );
        }
    }
}
