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
import org.apache.directory.studio.schemaeditor.view.wizards.ImportSchemasFromXmlWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ImportSchemasFromXmlAction — ASSEMBLING THE SECOND DEATH STAR ──────
// While the superstructure takes shape above Endor, Imperial engineers slot in
// prefabricated XML-spec data modules — encoded design files beamed in from the
// central archive and assembled into the station's core systems.
// Here we do the same thing: the user picks XML schema files they previously
// exported and we open a wizard that reads those structured files and assembles
// their contents into the live Schema Editor workspace.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse action that opens the Import Schemas from XML Files wizard, letting
 * users load schema definitions stored in the Schema Editor's own XML export
 * format back into a project.
 * All the actual XML parsing happens in {@link ImportSchemasFromXmlWizard}; this
 * class is just the menu/toolbar entry point that gets the wizard on screen.
 * Think of this as the data-module installation team that receives the encoded
 * archive and begins slotting each definition into the battle station's systems.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportSchemasFromXmlAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── DATA MODULE TEAM ARRIVES AT THE INSTALLATION BAY ─────────────────────
    // The installation crew steps off the lift, checks the duty roster, and
    // clips their data-module readers to their belts — all geared up before
    // the first XML cartridge arrives from the archive shuttle.
    // We configure our label, tooltip, icon, and enabled state so Eclipse can
    // show us in the menu the moment we're registered.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures this action for immediate use in Eclipse menus
     * and toolbars.
     * We read the label from the message bundle, set the import icon, and mark
     * ourselves enabled so the user can fire us whenever they like.
     */
    public ImportSchemasFromXmlAction()
    {
        super( Messages.getString( "ImportSchemasFromXmlAction.SchemasFromXMLFilesAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT ) );
        setEnabled( true );
    }


    // ── SLOTTING IN THE XML DATA MODULE ──────────────────────────────────────
    // An engineer slots the first XML cartridge into the installation reader;
    // it whirrs to life and starts streaming schema definitions into the
    // Death Star's core systems, step by methodical step.
    // We create the ImportSchemasFromXmlWizard, hand it to a WizardDialog, and
    // open it — the multi-page UI walks the user through picking files and
    // confirming the import before anything actually changes in the workspace.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs this action: creates and opens the {@link ImportSchemasFromXmlWizard}
     * inside an Eclipse {@link WizardDialog}.
     * The wizard owns the file selection and parsing logic; we just get it
     * in front of the user.
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        ImportSchemasFromXmlWizard wizard = new ImportSchemasFromXmlWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── OFFICER RELAYS THE INSTALLATION ORDER ────────────────────────────────
    // A deck officer passes the installation command down to the engineer
    // without alteration — chain of command, clean and simple.
    // Eclipse uses this overload when invoking us as an IAction delegate; we
    // just forward straight to run().
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


    // ── MODULE INSTALLED — TEAM STANDS DOWN ───────────────────────────────────
    // With the cartridge fully read and the module locked into the superstructure,
    // the installation crew packs up their gear and clears the bay.
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


    // ── PRE-INSTALLATION BRIEFING ON BAY LAYOUT ───────────────────────────────
    // Before the cartridges arrive, the crew is walked through the bay layout:
    // which stations are which, where the safety overrides are.
    // Eclipse calls init() once with the workbench window; we don't need it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is initialized.
     * We don't need the window reference, so we do nothing.
     *
     * @param window  the active workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── IGNORING FOOT TRAFFIC IN THE INSTALLATION BAY ────────────────────────
    // Other crews shuttle past on their own tasks; our engineers keep their
    // heads down and focus on the XML cartridge in front of them.
    // Our action doesn't change based on workbench selection, so we ignore it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * This action is always enabled regardless of selection, so we ignore this.
     *
     * @param action     the proxy action; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
