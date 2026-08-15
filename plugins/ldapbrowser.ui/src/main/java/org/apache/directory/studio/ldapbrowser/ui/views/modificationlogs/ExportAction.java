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

package org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportModificationLogsWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;


// ── CLASS: ExportAction — CLONE TROOPERS EXECUTE THE ORDER ───────────────────
// The clone troopers receive their orders and execute with precision —
// each trooper knows exactly what to do, moves without hesitation, and
// delivers the mission result to the designated location.
// This action is that trooper: when triggered, it opens the export wizard,
// marshals the modification log data, and delivers it to a file outside
// the workspace — no hand-holding, no second-guessing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toolbar action that launches the {@link ExportModificationLogsWizard} to
 * save modification log files to a location outside the Eclipse workspace.
 * Only enabled when a real connection (not just a placeholder) is selected.
 * Think of this as a clone trooper executing their mission — decisive,
 * targeted, and delivering results to the specified destination.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportAction extends BrowserAction
{

    // ── Constructor: Clone Trooper Stands Ready ───────────────────────────────
    // The clone trooper is assembled, armored, and standing at attention —
    // waiting for the order to execute. No special configuration needed.
    // We create a bare-bones action; all display properties come from getters.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportAction with no special initialization.
     * All display properties (text, icon, enabled state) are returned via
     * the getter methods below, so the constructor is intentionally empty.
     *
     * <p>For example — the trooper assembles and awaits orders:</p>
     * <pre>
     *   ExportAction action = new ExportAction();
     *   // Ready to execute when run() is called
     * </pre>
     */
    public ExportAction()
    {
    }


    // ── getCommandId: Trooper Has No Special Clearance Code ──────────────────
    // This trooper operates on direct verbal orders, not encoded clearance IDs —
    // no HoloNet command code is required for this mission.
    // We return null because this action isn't bound to an Eclipse command.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action, or null if unbound.
     * This action isn't registered as a global command handler.
     *
     * <p>For example — the trooper operates on direct orders, no clearance code needed:</p>
     * <pre>
     *   String id = action.getCommandId(); // null
     * </pre>
     *
     * @return  always {@code null}
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── getImageDescriptor: Trooper Puts On the Export Insignia ──────────────
    // Each clone trooper wears an insignia identifying their mission specialty —
    // this one wears the export badge so the user knows what action does.
    // We return the export icon from the plugin's image registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon descriptor for the export toolbar button.
     * We use the standard export image from the plugin's image registry.
     *
     * <p>For example — the trooper wears the export mission badge:</p>
     * <pre>
     *   ImageDescriptor img = action.getImageDescriptor(); // export icon
     * </pre>
     *
     * @return  the {@link ImageDescriptor} for the export icon
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT );
    }


    // ── getText: Trooper States the Mission Name ──────────────────────────────
    // Before executing, the trooper confirms the mission name for the record:
    // "Export Modification Logs, sir."
    // We return the localized label so the menu and toolbar show the right text.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action.
     * Shown as the toolbar tooltip and menu item text.
     *
     * <p>For example — the trooper states the mission name:</p>
     * <pre>
     *   String label = action.getText(); // "Export Modification Logs" (localized)
     * </pre>
     *
     * @return  the localized export label
     */
    @Override
    public String getText()
    {
        return Messages.getString( "ExportAction.ExportModificationLogs" ); //$NON-NLS-1$
    }


    // ── isEnabled: Trooper Confirms There's a Target to Engage ───────────────
    // The trooper won't fire unless there's a confirmed target — no point
    // executing an order if there's nothing to export.
    // We check that the input is a real log input AND that its underlying
    // connection isn't null (a disconnected placeholder has no logs).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns true only when a real (connected or previously-connected) browser
     * connection is loaded as the view's input.
     * We check both the input type and that its underlying {@link Connection}
     * isn't null — a null connection means no log files exist yet.
     *
     * <p>For example — trooper confirms a live target before engaging:</p>
     * <pre>
     *   boolean go = action.isEnabled();
     *   // true only if input is ModificationLogsViewInput AND connection != null
     * </pre>
     *
     * @return  {@code true} when there's a valid connection with potential log files
     */
    @Override
    public boolean isEnabled()
    {
        return ( getInput() instanceof ModificationLogsViewInput )
            && ( ( ModificationLogsViewInput ) getInput() ).getBrowserConnection().getConnection() != null;
    }


    // ── run: Trooper Executes the Export Mission ──────────────────────────────
    // The order is given; the clone trooper moves without hesitation — opens
    // the export channel, loads the target data, and delivers it to its
    // destination with precision.
    // We create the export wizard, seed it with the current connection, open
    // the wizard dialog, and block until the user completes or cancels.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link ExportModificationLogsWizard} pre-configured for the
     * currently selected connection's log files.
     * We seed the wizard's search with the current browser connection so the
     * first page already knows which connection's logs to export.
     *
     * <p>For example — the trooper executes the export mission step by step:</p>
     * <pre>
     *   ExportModificationLogsWizard wizard = new ExportModificationLogsWizard();
     *   wizard.getSearch().setBrowserConnection( input.getBrowserConnection() );
     *   WizardDialog dialog = new WizardDialog( getShell(), wizard );
     *   dialog.open(); // user guides the export; trooper delivers
     * </pre>
     */
    @Override
    public void run()
    {
        ModificationLogsViewInput input = ( ModificationLogsViewInput ) getInput();
        if ( input.getBrowserConnection().getConnection() != null )
        {
            ExportModificationLogsWizard wizard = new ExportModificationLogsWizard();
            wizard.getSearch().setBrowserConnection( input.getBrowserConnection() );
            WizardDialog dialog = new WizardDialog( getShell(), wizard );
            dialog.setBlockOnOpen( true );
            dialog.create();
            dialog.open();
        }
    }

}
