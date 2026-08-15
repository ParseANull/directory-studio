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
import org.apache.directory.studio.schemaeditor.view.wizards.ImportProjectsWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ImportProjectsAction — ASSEMBLING THE SECOND DEATH STAR ────────────
// Above Endor, Imperial engineers receive Palpatine's command and begin slotting
// prefabricated sections into the half-finished battle station — each module
// arriving from the outer systems and snapping into place under tight supervision.
// We do the same thing here: the user triggers this action and we open a wizard
// that walks them through picking schema-project files from disk and loading them
// into the workspace one structured step at a time.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse action that opens the Import Projects wizard so the user can bring
 * previously exported schema projects back into the Schema Editor workspace.
 * It lives in the workbench menu/toolbar and delegates all the real work to
 * {@link ImportProjectsWizard} — we just wire up the launch point.
 * Think of this class as the Imperial foreman who receives the construction order
 * and opens the hangar bay doors so the project modules can roll in.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportProjectsAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── RECEIVING THE EMPEROR'S CONSTRUCTION ORDER ────────────────────────────
    // Palpatine issues the command from his throne room: "Begin construction."
    // The Imperial foreman stands at attention, acknowledges the order, and
    // configures his station — icon ready, tooltip set, standing by for execution.
    // We set up our action the same way: label, icon, and enabled-state all
    // configured so Eclipse can render us in the menu immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and fully configures this action so Eclipse can display it in
     * menus and toolbars right away.
     * We grab our label from the message bundle, set the import icon, and leave
     * ourselves enabled so the user can click us any time.
     */
    public ImportProjectsAction()
    {
        super( Messages.getString( "ImportProjectsAction.SchemaProjectsAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_PROJECT_IMPORT ) );
        setEnabled( true );
    }


    // ── OPENING THE HANGAR BAY — WIZARD LAUNCHES ─────────────────────────────
    // The foreman punches in the code and the massive hangar bay doors slide open;
    // the first prefab section of the Death Star glides in on repulsor sleds.
    // That's us creating the ImportProjectsWizard and wrapping it in a WizardDialog
    // — the multi-step UI that guides the user through selecting and loading projects.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs this action: instantiates the {@link ImportProjectsWizard}, wraps it in
     * an Eclipse {@link WizardDialog}, and opens it so the user can walk through
     * the import steps.
     * The wizard handles all the file selection and parsing; we just launch the
     * container and let it take over.
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        ImportProjectsWizard wizard = new ImportProjectsWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── RELAYING THE ORDER THROUGH THE CHAIN OF COMMAND ──────────────────────
    // When a senior officer relays Palpatine's command verbatim down the chain,
    // each link in the hierarchy passes it along without modification.
    // This overload exists so the workbench can invoke us as an IAction delegate;
    // we just forward to our own run() — no extra logic needed here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when Eclipse calls us as an
     * {@link IWorkbenchWindowActionDelegate}.
     * We ignore the {@code action} parameter — the workbench passes it for
     * context but we don't need it.
     *
     * @param action  the proxy action the workbench invoked; we don't use it
     */
    public void run( IAction action )
    {
        run();
    }


    // ── STANDING DOWN AFTER THE STATION IS COMPLETE ───────────────────────────
    // Once the module is locked in place, the foreman dismisses his crew —
    // no loose ends, no resources left dangling.
    // We have nothing to clean up here; Eclipse calls this when our action is
    // being retired and we honor the contract by doing nothing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed.
     * We hold no resources, so there is nothing to release here.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── BRIEFING THE FOREMAN ON HIS WORKBENCH ────────────────────────────────
    // Before construction begins, the foreman is shown the bridge layout —
    // which consoles to use, where the controls are.
    // Eclipse calls this once with the active workbench window so we can grab
    // a reference if needed; we don't need one, so we leave it empty.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is initialized with the
     * active workbench window.
     * We don't need a window reference for this action, so we do nothing.
     *
     * @param window  the active workbench window; unused here
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── IGNORING DISTRACTIONS ON THE CONSTRUCTION DECK ───────────────────────
    // While modules are being assembled, officers on the deck shift around and
    // chatter — the foreman keeps his eyes on the work and ignores the noise.
    // Eclipse notifies us when the workbench selection changes, but this action
    // doesn't gate on selection, so we ignore it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * This action is always enabled regardless of selection, so we do nothing here.
     *
     * @param action     the proxy action; unused
     * @param selection  the new workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
