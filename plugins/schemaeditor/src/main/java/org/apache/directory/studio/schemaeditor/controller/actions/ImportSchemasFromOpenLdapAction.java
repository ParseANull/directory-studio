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
import org.apache.directory.studio.schemaeditor.view.wizards.ImportSchemasFromOpenLdapWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ImportSchemasFromOpenLdapAction — ASSEMBLING THE SECOND DEATH STAR ─
// Supply freighters from across the outer systems arrive at Endor carrying raw
// structural panels — mined on remote worlds and shaped to Imperial spec —
// ready to be lifted into the superstructure of the second Death Star.
// We do something analogous: OpenLDAP schema files are mined from the user's
// filesystem and we open the wizard that lifts them into our schema workspace,
// panel by panel, attribute type by object class.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse action that opens the Import Schemas from OpenLDAP Files wizard,
 * letting users pull in schema definitions stored in OpenLDAP's native
 * {@code .schema} file format and add them to the current Schema Editor project.
 * Paired with {@link ImportSchemasFromOpenLdapWizard} which does all the heavy
 * parsing; this class is purely the launch trigger.
 * Think of this class as the Imperial cargo controller who receives the parts
 * manifest and opens the docking clamps so the freighters can unload.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportSchemasFromOpenLdapAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── CARGO CONTROLLER REPORTS FOR DUTY ────────────────────────────────────
    // The cargo controller steps onto the platform, checks the arrival schedule,
    // clips on the Imperial insignia, and switches the docking bay indicators
    // to "ready" — fully configured, standing by for the first freighter.
    // We do exactly that: set label, tooltip, icon, and enabled flag so Eclipse
    // can render this action in the menu the moment the plugin activates.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and fully configures this action for immediate display in Eclipse
     * menus and toolbars.
     * We pull the label from the message bundle, attach the schema-import icon,
     * and leave ourselves enabled so the user can trigger us at any time.
     */
    public ImportSchemasFromOpenLdapAction()
    {
        super( Messages.getString( "ImportSchemasFromOpenLdapAction.SchemaFromOpenLDAPFilesAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT ) );
        setEnabled( true );
    }


    // ── DOCKING CLAMPS RELEASE — WIZARD OPENS ────────────────────────────────
    // The cargo controller punches in the clearance code and the docking clamps
    // release with a hydraulic hiss; the freighter glides in and the crew begins
    // unloading structural panels onto the assembly deck.
    // We create the ImportSchemasFromOpenLdapWizard, wrap it in a WizardDialog,
    // and open it — letting the user navigate step-by-step through file selection
    // until the schema definitions are fully unloaded into the workspace.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs this action: creates and opens the {@link ImportSchemasFromOpenLdapWizard}
     * inside an Eclipse {@link WizardDialog}.
     * The wizard handles file selection and parsing; we just get it on screen.
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        ImportSchemasFromOpenLdapWizard wizard = new ImportSchemasFromOpenLdapWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── PASSING THE ORDER DOWN THE CHAIN ─────────────────────────────────────
    // The senior officer relays the arrival signal to the cargo controller
    // without adding his own commentary — a clean pass-through.
    // Eclipse calls this overload when invoking us as an IAction delegate;
    // we forward straight to run() with no extra logic.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when Eclipse invokes us as an
     * {@link IWorkbenchWindowActionDelegate}.
     *
     * @param action  the proxy action from the workbench; not used here
     */
    public void run( IAction action )
    {
        run();
    }


    // ── CARGO BAY CLOSES AFTER UNLOADING ─────────────────────────────────────
    // Once the last panel rolls off the freighter, the bay doors close and
    // the crew disperses — no lingering personnel, no open hatches.
    // We have no resources to release, so this lifecycle callback is empty.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed.
     * We hold no resources, so nothing to clean up.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── BRIEFING ON THE DOCKING PLATFORM CONTROLS ────────────────────────────
    // Before the first freighter arrives the cargo controller is given a tour
    // of the platform — where the controls are, which consoles do what.
    // Eclipse calls init() once with the active window; we don't need it here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is initialized with the
     * active workbench window.
     * We don't need the window reference, so we do nothing.
     *
     * @param window  the active workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── IGNORING CREW MOVEMENT ON THE DECK ───────────────────────────────────
    // Technicians shuffle around the docking bay, shifting from console to console,
    // but the cargo controller stays focused on the freighter — crew movement
    // doesn't change his job.
    // Our action doesn't depend on workbench selection, so we ignore this callback.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * This action is selection-independent, so we do nothing.
     *
     * @param action     the proxy action; unused
     * @param selection  the new workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
