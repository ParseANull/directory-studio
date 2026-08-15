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
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportConnectionsWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportConnectionsAction — CLONE TROOPER EXECUTES EXPORT ORDER ────
// The Emperor transmits Order 66; clone trooper CT-7567 receives the signal,
// confirms the target, and executes without hesitation. This class does the
// same: when the user triggers "Export Connections", we immediately launch the
// export wizard — no conditions, no second-guessing, always ready to execute.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Launches the {@link ExportConnectionsWizard}, which guides the user through
 * exporting their saved LDAP connection definitions to a file.
 * This action is always enabled — there's no reason to grey it out — and
 * always opens the wizard against the active workbench window's selection.
 * Think of this as the clone trooper who always reports for duty and never
 * refuses an order.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportConnectionsAction extends BrowserAction
{
    // ── Trooper Reports for Export Duty ──────────────────────────────────────
    // CT-7567 snaps to attention — no special configuration needed, he's
    // already trained and equipped for the export mission.
    // We call the parent constructor to set up default action state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ExportConnectionsAction} ready to launch the export
     * wizard on demand. No arguments needed — everything is resolved from the
     * active workbench at execution time.
     */
    public ExportConnectionsAction()
    {
        super();
    }


    // ── Trooper Executes the Export Mission ──────────────────────────────────
    // The signal comes in, the trooper acts: he initialises the wizard with the
    // current workbench context, opens the dialog, and waits for it to complete
    // before returning — fully blocking so the caller knows when we're done.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Launches the {@link ExportConnectionsWizard} in a blocking {@link WizardDialog}.
     * The wizard is initialised with the active workbench window and its current
     * selection. The dialog blocks until the user finishes or cancels.
     */
    public void run()
    {
        ExportConnectionsWizard wizard = new ExportConnectionsWizard();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        wizard.init( window.getWorkbench(), ( IStructuredSelection ) window.getSelectionService().getSelection() );
        WizardDialog dialog = new WizardDialog( getShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        dialog.open();
    }


    // ── Trooper Announces the Mission Name ───────────────────────────────────
    // "Export Connections" — that's the order. The trooper repeats it back
    // so it appears as the menu item label.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name "Export Connections" for this action.
     *
     * @return  the label shown in menus; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "ExportConnectionsAction.ExportConnections" ); //$NON-NLS-1$
    }


    // ── Trooper Displays His Export Badge ────────────────────────────────────
    // The trooper shows his mission insignia — the export-connections icon —
    // so the user can identify this item at a glance in a crowded menu.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the "export connections" icon.
     *
     * @return  the {@link ImageDescriptor} for the icon; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_CONNECTIONS );
    }


    // ── Trooper Checks His Command Code ──────────────────────────────────────
    // No registered keyboard shortcut for this mission — null is the correct
    // response when the command registry has no entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action has no registered Eclipse
     * command ID and therefore no keyboard shortcut binding.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Trooper Is Always Mission-Ready ──────────────────────────────────────
    // Clone troopers don't take days off — this action is always enabled
    // because there's never a reason to grey out "export connections."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} unconditionally — this action is always available
     * regardless of what is selected.
     *
     * @return  {@code true} always
     */
    public boolean isEnabled()
    {
        return true;
    }
}
