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
import org.apache.directory.studio.schemaeditor.view.wizards.NewAttributeTypeWizard;
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


// ── CLASS: NewAttributeTypeAction — ASSEMBLING THE SECOND DEATH STAR ─────────
// Above Endor, an engineer receives an assignment: fabricate a new component
// panel and slot it into the superstructure.  First they check which section
// of the station they're working on, then they fire up the fabrication bay and
// build the part to spec.
// We do the same: inspect which schema the user has selected in the tree, then
// open the New Attribute Type wizard pre-pointed at that schema so the freshly
// created attribute type lands in exactly the right place.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse action that opens the New Attribute Type wizard, letting users
 * define a brand-new LDAP attribute type and add it to the currently selected
 * schema.
 * We inspect the tree selection before opening the wizard so we can pre-select
 * the right target schema for the user — they can always change it in the wizard,
 * but defaulting to what they have highlighted saves clicks.
 * Think of this class as the fabrication engineer who reads the current work
 * order (tree selection) before firing up the wizard to build a new part.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewAttributeTypeAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── FABRICATION ENGINEER REPORTS FOR DUTY ────────────────────────────────
    // The engineer arrives at the fabrication bay, checks the duty board,
    // clips on the correct insignia, and waits — disabled until a work section
    // is confirmed (the user selects a schema in the tree).
    // We configure label, tooltip, command ID, icon, and start disabled since
    // we need a schema selection before the user can meaningfully create an AT.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures this action, starting it in the disabled state.
     * We're disabled by default because creating an attribute type only makes
     * sense once the user has a schema in view — the controller that manages
     * us will enable us when the selection is appropriate.
     *
     * @param viewer  the tree viewer whose selection tells us which schema
     *                to pre-populate in the wizard
     */
    public NewAttributeTypeAction( TreeViewer viewer )
    {
        super( Messages.getString( "NewAttributeTypeAction.NewAttributeTypeAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "NewAttributeTypeAction.NewAttributeTypeToolTip" ) ); //$NON-NLS-1$
        setId( PluginConstants.CMD_NEW_ATTRIBUTE_TYPE );
        setActionDefinitionId( PluginConstants.CMD_NEW_ATTRIBUTE_TYPE );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_ATTRIBUTE_TYPE_NEW ) );
        setEnabled( false );
        this.viewer = viewer;
    }


    // ── READING THE WORK ORDER — OPENING THE FABRICATION WIZARD ──────────────
    // The engineer checks the assignment board — which section of the Death Star
    // is this part for?  They walk to the right fabrication bay, pre-load the
    // target section's blueprints, and kick off production.
    // We read the tree selection to figure out which schema the user is working
    // in, then open the NewAttributeTypeWizard pre-pointed at that schema.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs this action: inspects the current tree selection to determine the
     * target schema, then creates and opens the {@link NewAttributeTypeWizard}
     * with that schema pre-selected.
     * If we can't infer a schema from the selection (e.g. the view is empty or
     * we're in hierarchical mode) the wizard opens with no pre-selection and the
     * user picks one manually.
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
        NewAttributeTypeWizard wizard = new NewAttributeTypeWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        wizard.setSelectedSchema( selectedSchema );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── OFFICER PASSES THE ORDER DOWN THE LINE ────────────────────────────────
    // The deck officer relays the fabrication order verbatim to the engineer
    // — no extra commentary, clean chain of command.
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


    // ── PART FABRICATED — ENGINEER STANDS DOWN ────────────────────────────────
    // With the new component slotted into the superstructure, the engineer
    // stows their tools and clears the fabrication bay.
    // We hold no resources, so nothing to release.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed.
     * We hold no resources to release.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── ENGINEER BRIEFED ON THE CONTROL LAYOUT ───────────────────────────────
    // Before fabrication starts, the engineer is shown which consoles belong
    // to which section.  We don't need the workbench window reference here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is initialized.
     * We don't need the window reference for our logic.
     *
     * @param window  the active workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── IGNORING FOOT TRAFFIC IN THE FABRICATION BAY ─────────────────────────
    // Other crews walk past on their own assignments; the engineer keeps focus
    // on the part spec in front of them — workbench selection changes that hit
    // this callback are not ones we need to respond to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * We don't update our enabled state here — that's handled by the controller
     * that manages us via a dedicated selection listener.
     *
     * @param action     the proxy action; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
