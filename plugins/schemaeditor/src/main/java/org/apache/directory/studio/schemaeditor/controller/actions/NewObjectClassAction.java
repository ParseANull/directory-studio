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
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.wizards.NewObjectClassWizard;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewObjectClassAction — ASSEMBLING THE SECOND DEATH STAR ────────────
// Above Endor a structural engineer receives the order to fabricate a new
// command module — a larger, higher-level section that will house multiple
// sub-components.  They check which sector the module belongs to, then head
// to the fabrication bay and build it to blueprint.
// Object classes are the "command modules" of LDAP schemas: they group together
// attribute types and define what kind of entry an LDAP record is.  We read the
// user's tree selection to find the target schema, then open the wizard that
// guides them through defining a new one from scratch.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse action that opens the New Object Class wizard, letting users
 * define a brand-new LDAP object class and add it to the currently selected
 * schema.
 * We inspect the tree viewer's selection before opening the wizard so we can
 * pre-select the right target schema — a small UX convenience that saves the
 * user from having to pick it manually when it's already obvious from context.
 * Think of this class as the structural engineer who reads the sector assignment
 * (tree selection) before heading to the fabrication bay (wizard).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewObjectClassAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── STRUCTURAL ENGINEER REPORTS FOR DUTY ──────────────────────────────────
    // The engineer arrives, checks the assignment board, pins on their badge,
    // and waits at the disabled fabrication console — they need a sector
    // assignment before they can start.
    // We configure label, tooltip, command ID, icon, and start disabled because
    // creating an object class only makes sense once the user has a schema
    // selected; the controller enables us when the selection qualifies.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures this action, starting it in the disabled state.
     * We start disabled because the action only makes sense when the user
     * has a schema (or schema element) selected in the tree.
     *
     * @param viewer  the tree viewer whose selection tells us which schema
     *                to pre-populate in the wizard
     */
    public NewObjectClassAction( TreeViewer viewer )
    {
        super( Messages.getString( "NewObjectClassAction.NewObjectClassAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "NewObjectClassAction.NewObjectClassToolTip" ) ); //$NON-NLS-1$
        setId( PluginConstants.CMD_NEW_OBJECT_CLASS );
        setActionDefinitionId( PluginConstants.CMD_NEW_OBJECT_CLASS );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_OBJECT_CLASS_NEW ) );
        setEnabled( false );
        this.viewer = viewer;
    }


    // ── READING THE SECTOR ASSIGNMENT — OPENING THE FABRICATION WIZARD ────────
    // The engineer checks the sector map, identifies which part of the station
    // this new module will be attached to, loads that section's blueprints into
    // the fabrication system, and kicks off the wizard-guided build sequence.
    // We navigate the tree selection to find the parent schema (handling all the
    // wrapper nesting that the flat view introduces), then open
    // NewObjectClassWizard pre-pointed at that schema.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs this action: reads the current tree selection to infer the target
     * schema, then creates and opens the {@link NewObjectClassWizard} with that
     * schema pre-selected.
     * If no schema can be inferred (empty selection, hierarchical view mode,
     * unexpected wrapper type) the wizard opens without a pre-selection and
     * the user chooses one in the wizard itself.
     */
    public void run()
    {
        // Getting the selection
        Schema selectedSchema = null;

        int presentation = Activator.getDefault().getPreferenceStore().getInt(
            PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
            if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
            {
                Object firstElement = selection.getFirstElement();
                if ( firstElement instanceof SchemaWrapper )
                {
                    selectedSchema = ( ( SchemaWrapper ) firstElement ).getSchema();
                }
                else if ( firstElement instanceof Folder )
                {
                    selectedSchema = ( ( SchemaWrapper ) ( ( Folder ) firstElement ).getParent() ).getSchema();
                }
                else if ( firstElement instanceof AttributeTypeWrapper )
                {
                    TreeNode parent = ( ( AttributeTypeWrapper ) firstElement ).getParent();

                    if ( parent instanceof Folder )
                    {
                        selectedSchema = ( ( SchemaWrapper ) ( ( Folder ) parent ).getParent() ).getSchema();

                    }
                    else if ( parent instanceof SchemaWrapper )
                    {
                        selectedSchema = ( ( SchemaWrapper ) parent ).getSchema();
                    }
                }
                else if ( firstElement instanceof ObjectClassWrapper )
                {
                    TreeNode parent = ( ( ObjectClassWrapper ) firstElement ).getParent();

                    if ( parent instanceof Folder )
                    {
                        selectedSchema = ( ( SchemaWrapper ) ( ( Folder ) parent ).getParent() ).getSchema();
                    }
                    else if ( parent instanceof SchemaWrapper )
                    {
                        selectedSchema = ( ( SchemaWrapper ) parent ).getSchema();
                    }
                }
            }
        }

        // Instantiates and initializes the wizard
        NewObjectClassWizard wizard = new NewObjectClassWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        wizard.setSelectedSchema( selectedSchema );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── PASSING THE BUILD ORDER DOWN THE CHAIN ────────────────────────────────
    // The sector commander relays the fabrication order to the engineer verbatim.
    // Eclipse uses this overload when calling us as an IAction delegate; we just
    // forward to run().
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


    // ── MODULE INSTALLED — FABRICATION BAY CLEARED ────────────────────────────
    // With the new command module locked into position, the engineer stows their
    // tools and clears the bay for the next assignment.
    // We have no resources to release.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed.
     * We hold no resources to release.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── ENGINEER RECEIVES STATION LAYOUT BRIEFING ─────────────────────────────
    // Before the build begins, the engineer is shown the full station layout.
    // We don't need the workbench window reference for our action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is initialized.
     * We don't need the window reference.
     *
     * @param window  the active workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── IGNORING PERSONNEL MOVEMENT IN THE FABRICATION BAY ───────────────────
    // Other crews shuffle in and out of the bay; our engineer stays focused on
    // the blueprint — our enabled state is driven by a dedicated selection
    // listener in the controller, not by this callback.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * Our enabled state is managed by the controller's selection listener, not here.
     *
     * @param action     the proxy action; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
