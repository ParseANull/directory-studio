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


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ImportConnectionsWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ImportConnectionsAction — CLONE TROOPER EXECUTES IMPORT ORDER ────
// The Emperor's order arrives: "Import the new assets." Clone trooper CT-9904
// receives the signal, mobilises immediately, and launches the import operation
// without hesitation. This action does the same: the user triggers "Import
// Connections" and we immediately open the import wizard, no questions asked,
// always ready.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Launches the {@link ImportConnectionsWizard}, which guides the user through
 * importing LDAP connection definitions from a file into Directory Studio.
 * Always enabled — you can import connections at any point regardless of what
 * is currently selected.
 * Think of this as the clone trooper who stands ready for the import mission
 * twenty-four hours a day, no conditions attached.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportConnectionsAction extends BrowserAction
{
    // ── Trooper Reports for Import Duty ──────────────────────────────────────
    // CT-9904 snaps to attention — no configuration, he's already trained
    // and ready for the import mission.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ImportConnectionsAction} ready to launch the import
     * wizard on demand.
     */
    public ImportConnectionsAction()
    {
        super();
    }


    // ── Trooper Executes the Import Mission ──────────────────────────────────
    // The signal fires: initialise the wizard with the active workbench, open
    // the dialog, block until it closes. The trooper does not return early.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Launches the {@link ImportConnectionsWizard} in a blocking {@link WizardDialog}.
     * The wizard is initialised with the active workbench window and its current
     * selection. The dialog blocks until the user finishes or cancels.
     */
    public void run()
    {
        ImportConnectionsWizard wizard = new ImportConnectionsWizard();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        wizard.init( window.getWorkbench(), ( IStructuredSelection ) window.getSelectionService().getSelection() );
        WizardDialog dialog = new WizardDialog( getShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        dialog.open();
    }


    // ── Trooper Announces the Mission Name ───────────────────────────────────
    // "Import Connections" — the trooper repeats the order back as the menu label.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name "Import Connections" for this action.
     *
     * @return  the menu label; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "ImportConnectionsAction.ImportConnections" ); //$NON-NLS-1$
    }


    // ── Trooper Shows His Import Badge ───────────────────────────────────────
    // The trooper displays the import-connections insignia so the user can spot
    // this menu item at a glance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the "import connections" icon.
     *
     * @return  the {@link ImageDescriptor} for the icon; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_IMPORT_CONNECTIONS );
    }


    // ── Trooper Checks the Command Registry ──────────────────────────────────
    // No registered keyboard shortcut for this import mission.
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


    // ── Trooper Is Always Ready ───────────────────────────────────────────────
    // Clone troopers never stand down — this action is always enabled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} unconditionally — import connections is always available.
     *
     * @return  {@code true} always
     */
    public boolean isEnabled()
    {
        return true;
    }
}
