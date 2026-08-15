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
import org.apache.directory.studio.schemaeditor.view.wizards.NewProjectWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewProjectAction — ASSEMBLING THE SECOND DEATH STAR ────────────────
// The Emperor makes the call: a second Death Star, more powerful than the first,
// is to be built from scratch.  Moff Jerjerrod receives the order and
// immediately begins standing up the construction project — assigning the
// orbital scaffold, opening the resource allocation ledger, and issuing the
// first construction manifest.
// Creating a new schema project is exactly that moment: the user says "I want
// a fresh schema workspace" and we open the wizard that walks them through
// naming it, choosing its type, and laying the foundation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse action that opens the New Project wizard, letting users create a
 * fresh schema project from scratch in the Schema Editor.
 * A schema project is the top-level container — it groups together one or more
 * schemas and tracks the LDAP server connection (or offline mode) they apply to.
 * Think of this class as Moff Jerjerrod's first command: open the ledger, stand
 * up the scaffold, and let the wizard guide everything that comes next.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewProjectAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── MOFF JERJERROD RECEIVES THE EMPEROR'S ORDER ───────────────────────────
    // Jerjerrod snaps to attention, straightens his uniform, confirms the command,
    // and posts himself at the operations console — fully configured, badge
    // pinned, standing by to issue the construction mandate the moment it arrives.
    // We set our label, tooltip, command ID, icon, and leave ourselves enabled
    // so the user can create a new project any time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and fully configures this action for immediate display in menus
     * and toolbars.
     * We read the label and tooltip from the message bundle, register command IDs
     * so keyboard shortcuts can trigger us, attach the new-project icon, and
     * leave ourselves enabled because creating a project is always available.
     */
    public NewProjectAction()
    {
        super( Messages.getString( "NewProjectAction.NewSchemaProjectAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "NewProjectAction.NewSchemaProjectToolTip" ) ); //$NON-NLS-1$
        setId( PluginConstants.CMD_NEW_PROJECT );
        setActionDefinitionId( PluginConstants.CMD_NEW_PROJECT );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_PROJECT_NEW ) );
        setEnabled( true );
    }


    // ── OPENING THE CONSTRUCTION LEDGER — WIZARD LAUNCHES ────────────────────
    // Jerjerrod opens the orbital scaffold allocation ledger and kicks off the
    // project manifest wizard — each page of the wizard is another section of
    // the ledger being filled in: project name, server type, initial schemas.
    // We create the NewProjectWizard, wrap it in a WizardDialog, and open it
    // so the user can walk through the setup steps.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs this action: creates the {@link NewProjectWizard} and opens it
     * inside an Eclipse {@link WizardDialog}.
     * The wizard owns all the project-creation logic; we just get it on screen.
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        NewProjectWizard wizard = new NewProjectWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── THE EMPEROR'S ORDER PASSES THROUGH THE CHAIN ─────────────────────────
    // When Vader relays the Emperor's command to Jerjerrod, the instruction
    // arrives unchanged — Jerjerrod acts on it without adding his own spin.
    // Eclipse uses this overload when invoking us as an IAction delegate; we
    // just forward to run().
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


    // ── CONSTRUCTION PROJECT CLOSED OUT — JERJERROD DISMISSED ────────────────
    // Once the project ledger is closed and the scaffold decommissioned, Jerjerrod
    // is relieved of duty — no lingering responsibilities, no open files.
    // We hold no resources, so this lifecycle callback is a clean no-op.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed.
     * We hold no resources to release.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── JERJERROD BRIEFED ON THE OPERATIONS CENTER LAYOUT ────────────────────
    // Before the first manifest is filed, Jerjerrod is shown which consoles do
    // what in the Death Star operations center.
    // We don't need the workbench window reference for our action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is initialized with the
     * active workbench window.
     * We don't need the window reference here.
     *
     * @param window  the active workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── IGNORING CORRIDOR TRAFFIC NEAR OPERATIONS ────────────────────────────
    // Officers scurry past the operations center on their own errands; Jerjerrod
    // keeps his eyes on the construction ledger — our action doesn't change
    // based on what the user has selected in the workbench.
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
