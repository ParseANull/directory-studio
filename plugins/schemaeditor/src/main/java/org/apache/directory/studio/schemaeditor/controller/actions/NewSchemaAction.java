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
package org.apache.directory.studio.schemaeditor.controller.actions;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.wizards.NewSchemaWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewSchemaAction — Construction of the Second Death Star ────────────
// Deep in the Endor system, Imperial engineers receive the order to begin the
// second Death Star: gather the crew, lay the structural foundation, and open
// the blast doors so construction can proceed under Palpatine's watchful eye.
// We do the same thing here — when the user fires "New Schema," we spin up the
// wizard, initialize it with an empty selection, and open it so they can lay
// the foundation for a new schema container.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Launches the {@link NewSchemaWizard} so the user can create a brand-new schema.
 * This is the action wired to the "New Schema" toolbar button and menu entry in the
 * Schema Editor plugin.
 * Think of this class as the Imperial construction crew receiving the order to build:
 * it assembles the wizard, initializes the worksite (empty selection), and opens
 * the door so the user can start laying schema foundations.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewSchemaAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── Engineering Crew Reports For Duty ────────────────────────────────────────
    // The Imperial construction chief arrives at the Endor site and reads out the
    // standing orders: here is the label, here is the tooltip, here is your station ID.
    // Before the first girder goes up, every role must be assigned — that's what this
    // constructor does, mapping label, tooltip, command ID, icon, and initial state.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of NewSchemaAction and configures all the visual properties.
     * We set the label, tooltip, command ID (so keybindings work), and the icon, then
     * start the action disabled — it only makes sense once a project is open.
     */
    public NewSchemaAction()
    {
        super( Messages.getString( "NewSchemaAction.NewSchemaAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "NewSchemaAction.NewSchemaToolTip" ) ); //$NON-NLS-1$
        setId( PluginConstants.CMD_NEW_SCHEMA );
        setActionDefinitionId( PluginConstants.CMD_NEW_SCHEMA );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMA_NEW ) );
        setEnabled( false );
    }


    // ── Blast Doors Open, Construction Begins ────────────────────────────────────
    // The Emperor's signal reaches Endor: the blast doors grind open, the engineering
    // team pours onto the scaffolding, and the second Death Star's superstructure
    // begins to take shape under the foreman's direction.
    // Here we instantiate the wizard, hand it an empty workbench selection, wrap it
    // in a WizardDialog, and open it — the user steps inside and starts building.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the New Schema wizard so the user can define and name a new schema.
     * We create the wizard, initialize it with an empty selection (no pre-selected
     * elements needed), wrap it in a {@link WizardDialog}, and call {@code open()}.
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        NewSchemaWizard wizard = new NewSchemaWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── Foreman Relays The Order ──────────────────────────────────────────────────
    // A junior officer relays the Emperor's construction mandate down the chain —
    // the foreman just calls through to the crew without adding anything extra.
    // This IAction-flavored overload lets Eclipse's command framework invoke us;
    // we simply delegate to our no-arg run().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} so Eclipse's workbench action framework can invoke us.
     * The {@code action} parameter is unused — we don't need it here.
     *
     * @param action  the IAction proxy passed by the framework; we ignore it
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Construction Site Decommissioned ─────────────────────────────────────────
    // The Death Star is destroyed; the construction site is cleared and the crew
    // stand down — no ongoing resources to release here.
    // This action holds no listeners or heavy resources, so dispose is a no-op.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up any resources held by this action when the workbench disposes it.
     * We have nothing to release, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Engineering Chief Receives Window Assignment ──────────────────────────────
    // The Imperial construction chief is handed a datapad listing which sector window
    // they are responsible for — but our crew needs no window-specific wiring.
    // We implement this IWorkbenchWindowActionDelegate method as required by the
    // interface; there's no per-window setup needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is bound to a workbench window.
     * We don't need the window reference for anything, so this is intentionally empty.
     *
     * @param window  the workbench window we're being initialized into; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Scout Reports Back, Construction Continues Regardless ────────────────────
    // A scout droid reports a change in which sector the Rebels are attacking, but
    // the Death Star construction crew keeps welding — the selection in the UI
    // doesn't affect whether we can create a new schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the workbench selection changes.
     * We don't gate "New Schema" on what's selected, so we ignore this notification.
     *
     * @param action     the IAction proxy; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
