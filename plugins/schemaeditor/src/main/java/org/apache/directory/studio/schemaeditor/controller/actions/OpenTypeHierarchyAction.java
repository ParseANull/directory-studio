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


import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.views.HierarchyView;
import org.apache.directory.studio.schemaeditor.view.views.SchemaView;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenTypeHierarchyAction — Luke's Binary Sunset on Tatooine ─────────
// Luke stands outside the Lars homestead at dusk, staring at the twin suns as
// they sink below the Dune Sea horizon — suddenly the whole landscape reveals
// itself: where things come from, how they relate, how far they stretch.
// That's exactly what the Hierarchy View does: it shows a schema element — an
// attribute type or object class — in the context of its full ancestry and
// descendants, so you see the big picture at a glance.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the selected schema element in the Hierarchy View, revealing its full
 * inheritance chain — supertypes above, subtypes below.
 * We support being invoked from the Schema View tree, the Hierarchy View tree,
 * an Attribute Type editor, or an Object Class editor — wherever we're called
 * from, we extract the target element and display it in the Hierarchy View.
 * Think of this as Luke's binary sunset: suddenly you see how everything connects
 * and where your type fits in the larger schema family.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenTypeHierarchyAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── Luke Turns And Sees The Horizon For The First Time ────────────────────────
    // Luke steps out of the homestead and scans the desert: he registers what he's
    // looking at (the twin suns) and starts paying attention to the landscape around
    // him. Our constructor registers a selection listener on the viewer so we know
    // when there's an attribute type or object class selected — only then does the
    // "Open Type Hierarchy" button light up.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenTypeHierarchyAction tied to the given tree viewer.
     * We attach a selection listener immediately and enable ourselves only when
     * exactly one attribute type or object class wrapper is highlighted.
     *
     * @param viewer  the tree viewer we watch for selection changes
     */
    public OpenTypeHierarchyAction( TreeViewer viewer )
    {
        super( Messages.getString( "OpenTypeHierarchyAction.OpenTypeAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenTypeHierarchyAction.OpenTypeToolTip" ) ); //$NON-NLS-1$
        setId( PluginConstants.CMD_OPEN_TYPE_HIERARCHY );
        setActionDefinitionId( PluginConstants.CMD_OPEN_TYPE_HIERARCHY );
        setEnabled( false );
        this.viewer = viewer;
        this.viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();
                setEnabled( ( selection.size() == 1 )
                    && ( ( selection.getFirstElement() instanceof AttributeTypeWrapper )
                    || ( selection.getFirstElement() instanceof ObjectClassWrapper ) ) );
            }
        } );
    }


    // ── The Twin Suns Reveal The Whole Landscape ──────────────────────────────────
    // As the suns drop below the horizon, Luke sees the full expanse of Tatooine
    // laid out before him — every dune, every ridge, the full picture.
    // We figure out which Eclipse part is currently active (Schema View, Hierarchy
    // View, AT editor, OC editor) and extract the selected element from it. Then
    // we hand that element to the Hierarchy View, which opens/brings it to front
    // and shows the full inheritance tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Determines the selected schema element from the currently active part and
     * opens it in the Hierarchy View to show its full type hierarchy.
     * We check which view or editor is active and delegate to the appropriate
     * helper to extract the element before displaying it.
     */
    public void run()
    {
        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();

        if ( part instanceof SchemaView )
        {
            openTypeHierarchyFromTreeViewer( ( ( SchemaView ) part ).getViewer() );
        }
        else if ( part instanceof HierarchyView )
        {
            openTypeHierarchyFromTreeViewer( ( ( HierarchyView ) part ).getViewer() );
        }
        else if ( part instanceof AttributeTypeEditor )
        {
            openTypeHierarchy( ( ( AttributeTypeEditor ) part ).getOriginalAttributeType() );
        }
        else if ( part instanceof ObjectClassEditor )
        {
            openTypeHierarchy( ( ( ObjectClassEditor ) part ).getOriginalObjectClass() );
        }
    }


    // ── Luke Identifies Which Dune He's Looking At ───────────────────────────────
    // Luke narrows his gaze to a specific shape on the horizon — is it an object
    // class ridge or an attribute type dune? He identifies it, then fixes his
    // focus on exactly that landmark to understand it fully.
    // We unwrap the first selected element from the tree and forward the underlying
    // schema object to openTypeHierarchy().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the selected schema element from the given tree viewer and forwards
     * it to {@link #openTypeHierarchy(SchemaObject)}.
     * Only the first selected element is used — we only enable ourselves when
     * exactly one item is selected.
     *
     * @param treeViewer  the tree viewer holding the current selection
     */
    private void openTypeHierarchyFromTreeViewer( TreeViewer treeViewer )
    {
        Object firstElement = ( ( StructuredSelection ) treeViewer.getSelection() ).getFirstElement();

        if ( firstElement instanceof ObjectClassWrapper )
        {
            ObjectClassWrapper ocw = ( ObjectClassWrapper ) firstElement;
            openTypeHierarchy( ocw.getObjectClass() );
        }
        else if ( firstElement instanceof AttributeTypeWrapper )
        {
            AttributeTypeWrapper atw = ( AttributeTypeWrapper ) firstElement;
            openTypeHierarchy( atw.getAttributeType() );
        }
    }


    // ── The Horizon Snaps Into Focus ──────────────────────────────────────────────
    // Luke locks his gaze on the twin suns and the full panorama crystallizes —
    // everything connects, every relationship visible at once.
    // We find (or open) the Hierarchy View and call setInput() on it with our
    // element, then bring it to the front of the editor area.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens (or focuses) the Hierarchy View and sets it to display the given
     * schema element's inheritance chain.
     * If the Hierarchy View isn't already open we open it; if that fails we log
     * the error and show the user a dialog. Then we bring the view to front.
     *
     * @param element  the attribute type or object class to display in the hierarchy
     */
    private void openTypeHierarchy( SchemaObject element )
    {
        HierarchyView view = ( HierarchyView ) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
            .findView( HierarchyView.ID );

        if ( view == null )
        {
            try
            {
                view = ( HierarchyView ) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                    HierarchyView.ID );
            }
            catch ( PartInitException e )
            {
                PluginUtils.logError( Messages.getString( "OpenTypeHierarchyAction.ErrorOpeningView" ), e ); //$NON-NLS-1$
                ViewUtils
                    .displayErrorMessageDialog(
                        Messages.getString( "OpenTypeHierarchyAction.Error" ), Messages.getString( "OpenTypeHierarchyAction.ErrorOpeningView" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        if ( view != null )
        {
            view.setInput( element );
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().bringToTop( view );
        }
    }


    // ── Luke Turns To Go — Same Destination Either Way ───────────────────────────
    // Whether Ben tells Luke to come or Luke decides on his own, the destination
    // is the same — delegate straight through.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} so Eclipse's command framework can invoke us.
     *
     * @param action  the IAction proxy; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Luke Heads Back Inside, Suns Have Set ─────────────────────────────────────
    // The moment passes; Luke goes back inside — nothing to clean up.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action. We hold none, so this is a no-op.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Luke Is Assigned To The Lars Homestead ────────────────────────────────────
    // Luke knows which homestead he belongs to — no special per-window work needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when this action is bound to a workbench window. No per-window setup needed.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Landscape Shifts, But Luke's Gaze Is Steady ──────────────────────────────
    // The wind picks up and the dunes shift — but Luke's tree-viewer listener already
    // handles enable/disable from selection events; this callback has nothing to add.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes. Our tree viewer's own
     * listener manages enable/disable; nothing extra needed here.
     *
     * @param action     the IAction proxy; unused
     * @param selection  the current selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
