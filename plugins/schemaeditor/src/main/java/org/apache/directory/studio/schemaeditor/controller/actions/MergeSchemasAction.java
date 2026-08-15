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
import org.apache.directory.studio.schemaeditor.view.wizards.MergeSchemasWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: MergeSchemasAction — YODA LIFTING LUKE'S X-WING FROM THE SWAMP ───
// On Dagobah, Luke's submerged X-wing looks like a hopeless mess — two halves
// of a fighter sinking in different directions, coated in swamp muck.
// Yoda closes his eyes, reaches out with the Force, and lifts them both
// simultaneously, combining their momentum into a single unified trajectory
// until the whole craft rises out of the water as one intact ship.
// That's what schema merging does: take separate, disparate schema definitions
// and combine them into a single unified project, resolving overlaps calmly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse action that opens the Merge Schemas wizard, letting users pull
 * definitions from one schema into another and resolve any conflicts
 * interactively.
 * All the actual merging logic lives in {@link MergeSchemasWizard}; we just
 * provide the menu/toolbar entry point that launches it.
 * Think of this class as Yoda raising his hand — the gesture that kicks off
 * the lift.  The wizard is the Force that actually moves things.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MergeSchemasAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── YODA STANDS AT THE SWAMP'S EDGE, READY ───────────────────────────────
    // Before he lifts the X-wing, Yoda plants his feet, closes his eyes, and
    // breathes — fully configured, icon in hand, waiting for the signal.
    // We set up our action the same way: label from the message bundle, the
    // import icon, enabled flag set so the user can trigger us any time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures this action for immediate display in Eclipse
     * menus and toolbars.
     * We read the label from the message bundle, attach the schema-import icon,
     * and leave ourselves enabled so the user can fire the merge at any time.
     */
    public MergeSchemasAction()
    {
        super( Messages.getString( "MergeSchemasAction.MergeSchemas" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT ) );
        setEnabled( true );
    }


    // ── YODA RAISES HIS HAND — THE X-WINGS RISE ──────────────────────────────
    // Yoda reaches out and lifts both halves of the X-wing from the swamp in
    // one smooth motion, guiding them together until they lock into a single
    // whole — step by deliberate step.
    // We instantiate the MergeSchemasWizard and open it inside a WizardDialog;
    // the wizard walks the user through picking source and target schemas and
    // handling conflicts until everything is merged into one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs this action: creates the {@link MergeSchemasWizard} and opens it
     * inside an Eclipse {@link WizardDialog}.
     * The wizard owns all the merge logic; we just get it in front of the user.
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        MergeSchemasWizard wizard = new MergeSchemasWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── LUKE RELAYS THE COMMAND ───────────────────────────────────────────────
    // Luke says "do it" and Yoda does it — the instruction passes through
    // unchanged, no interpretation required.
    // Eclipse calls this overload when using us as an IAction delegate; we
    // simply forward to run().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when Eclipse calls us as an
     * {@link IWorkbenchWindowActionDelegate}.
     *
     * @param action  the proxy action from the workbench; not used here
     */
    public void run( IAction action )
    {
        run();
    }


    // ── THE X-WING IS UP — YODA LOWERS HIS HAND ──────────────────────────────
    // Once the ship is on the bank, Yoda opens his eyes and lowers his hand —
    // the effort is done, no lingering energy expenditure.
    // We hold no resources, so dispose is a clean no-op.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed.
     * We hold no resources to release.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── YODA SURVEYS THE SWAMP BEFORE ACTING ─────────────────────────────────
    // Before the lift, Yoda takes a moment to assess the environment —
    // then decides he doesn't actually need any extra information from it.
    // Eclipse calls init() once; we don't need the window reference here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is initialized with the
     * active workbench window.
     * We don't need the window reference for our merge action.
     *
     * @param window  the active workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── LUKE SHIFTING HIS WEIGHT DOESN'T BREAK THE LIFT ──────────────────────
    // While Yoda is lifting, Luke paces nervously on the bank — Yoda ignores
    // it completely and keeps his concentration on the X-wing.
    // Workbench selection changes don't affect this action's enabled state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * This action is always enabled regardless of selection, so we do nothing.
     *
     * @param action     the proxy action; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
