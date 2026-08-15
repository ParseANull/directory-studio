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
package org.apache.directory.studio.schemaeditor.view.views;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaViewController;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaViewRoot;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: SchemaView — Luke Watching the Binary Sunset ──────────────────────
// Luke stands at the edge of the moisture farm, taking in the full panoramic
// view of both suns setting over Tatooine. He's not looking at one detail — he
// sees the whole landscape at once: every mesa, every shadow, the full expanse.
// SchemaView is that panoramic view for our LDAP schema: a tree that shows every
// schema, every attribute type, every object class in a single navigable widget.
// Like Luke taking in the horizon, users can see the entire schema at a glance
// and click into any piece of it. When the user selects something here, other
// views (like the Hierarchy View or the editors) react to that selection.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Schema View — the primary Eclipse ViewPart for browsing the entire loaded schema.
 * It renders schemas, attribute types, and object classes in a tree, supporting both
 * flat and hierarchical presentation modes. Selections here drive other views: picking
 * an object class shows its hierarchy in the Hierarchy View; double-clicking opens
 * the editor. Think of it as Luke's panoramic binary sunset — the whole landscape,
 * navigable from one spot.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaView extends ViewPart
{
    /** The ID of the View */
    public static final String ID = PluginConstants.VIEW_SCHEMA_VIEW_ID;

    /** The viewer */
    private TreeViewer treeViewer;


    // ── Luke Steps Outside and Takes In the View ─────────────────────────────
    // You can't see the binary sunset from inside the moisture farm. Luke has to
    // walk out, face the right direction, and let his eyes adjust. createPartControl
    // does that: it initializes the tree viewer, registers it as the selection
    // provider so other views can react to what the user clicks, installs the
    // controller, and registers with the help system.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Schema View UI — called by Eclipse when this view is first opened.
     * We initialize the tree viewer, then register it with the Eclipse site as the
     * selection provider — this is what makes other views react when you click a node
     * here. We then install {@link SchemaViewController} which adds all the toolbar
     * actions and event listeners, and register with the help system for F1 support.
     *
     * <p>For example — stepping outside to see the full view:</p>
     * <pre>
     *   createPartControl(parent)
     *     → initViewer(parent)
     *     → getSite().setSelectionProvider(treeViewer)  // "other views, watch this"
     *     → new SchemaViewController(this)
     *     → setHelp("schema_view")
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides for us to draw into
     */
    @Override
    public void createPartControl( Composite parent )
    {
        initViewer( parent );

        // Registering the Viewer, so other views can be notified when the viewer selection changes
        getSite().setSelectionProvider( treeViewer );

        // Adding the controller
        new SchemaViewController( this );

        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent, PluginConstants.PLUGIN_ID + "." + "schema_view" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── The Twin Suns Need the Right Horizon ─────────────────────────────────
    // Without a clear, unobstructed horizon you can't see both suns at once.
    // initViewer builds that horizon: the TreeViewer with multi-select and
    // scroll bars, the content and label providers that know how to populate
    // and render each node, and the CSS class name for styling via Eclipse themes.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures the {@link TreeViewer} for the schema browser.
     * We use multi-select mode so users can select multiple schema elements at once
     * (e.g., to delete a batch). The {@link DecoratingLabelProvider} wrapper lets
     * Eclipse's decorator framework add overlay icons (error badges, etc.) automatically.
     * The tree starts disabled — the controller will enable it once a project is loaded.
     *
     * <p>For example — building the horizon:</p>
     * <pre>
     *   treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER)
     *   treeViewer.setContentProvider(new SchemaViewContentProvider())
     *   treeViewer.setLabelProvider(new DecoratingLabelProvider(...))
     *   treeViewer.getTree().setEnabled(false)
     * </pre>
     *
     * @param parent  the SWT composite to embed the tree into
     */
    private void initViewer( Composite parent )
    {
        treeViewer = new TreeViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        treeViewer.getTree().setData( "org.eclipse.e4.ui.css.CssClassName", "studio-schema-tree" );
        treeViewer.setContentProvider( new SchemaViewContentProvider() );
        treeViewer.setLabelProvider( new DecoratingLabelProvider( new SchemaViewLabelProvider(), Activator.getDefault()
            .getWorkbench().getDecoratorManager().getLabelDecorator() ) );
        treeViewer.getTree().setEnabled( false );
    }


    // ── Luke's Gaze Settles on the Horizon ───────────────────────────────────
    // When Luke stares at the sunset, his focus is fully there — nothing else
    // competes. setFocus routes keyboard input to our tree so pressing arrow
    // keys or Enter navigates the schema immediately after clicking our tab.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Gives keyboard focus to the schema tree widget.
     * Eclipse calls this when the user activates our view's tab. Routing focus
     * directly to the tree widget means keyboard navigation works immediately.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        treeViewer.getTree().setFocus();
    }


    // ── Hand the Controller the View ─────────────────────────────────────────
    // Luke doesn't own the sunset — he just observes it and reports back. The
    // controller needs the viewer to register listeners, drive toolbar actions,
    // and update the selection. We hand it over directly.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying {@link TreeViewer} for this view.
     * The {@link SchemaViewController} uses this reference to register toolbar actions,
     * respond to double-clicks, and trigger programmatic refreshes.
     *
     * @return  the {@link TreeViewer} displaying the schema tree
     */
    public TreeViewer getViewer()
    {
        return treeViewer;
    }


    // ── The Suns Set — Time to Reset the View ────────────────────────────────
    // A new day means a fresh horizon. When a project is loaded or changed,
    // we want to rebuild the tree from scratch with a new SchemaViewRoot, which
    // causes the content provider to re-query the schema handler and rebuild
    // every node from the current loaded schema.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the tree from scratch by setting a fresh {@link SchemaViewRoot} as the input.
     * This triggers the {@link SchemaViewContentProvider} to re-fetch all schemas from the
     * {@link org.apache.directory.studio.schemaeditor.controller.SchemaHandler} and rebuild
     * the entire tree. Call this after a project is opened or a schema is imported.
     */
    public void reloadViewer()
    {
        treeViewer.setInput( new SchemaViewRoot() );
    }


    // ── The Scene Refreshes In-Place ─────────────────────────────────────────
    // The sun doesn't completely reset between glances — the scene just updates.
    // Sometimes we don't need to rebuild from scratch; we just need the viewer
    // to repaint whatever it already has, which is cheaper and preserves
    // the user's current expand/collapse state.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the tree viewer in-place without rebuilding from scratch.
     * This re-queries the content provider for every visible node and repaints them.
     * Use this after small schema changes where the tree structure hasn't fundamentally
     * changed — it's faster than {@link #reloadViewer()} and preserves expand state.
     */
    public void refresh()
    {
        treeViewer.refresh();
    }


    // ── Refresh and Lock Focus on a Specific Node ────────────────────────────
    // Luke's gaze isn't always wide — sometimes he's looking at one specific
    // point in that horizon. This overload refreshes the tree and then
    // programmatically selects a specific node so the user's focus lands there.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the tree viewer and then applies the given selection.
     * Use this when you've just created or modified a schema element and you want
     * the tree to update and the focus to land on that element automatically.
     *
     * <p>For example — refresh and focus a newly added type:</p>
     * <pre>
     *   schemaView.refresh(new StructuredSelection(newAttributeType));
     *   // tree repaints, then scrolls to and highlights newAttributeType
     * </pre>
     *
     * @param selection  the {@link ISelection} to apply after refreshing; typically a
     *                   {@link org.eclipse.jface.viewers.StructuredSelection} wrapping
     *                   the schema element you want highlighted
     */
    public void refresh( ISelection selection )
    {
        treeViewer.refresh();
        treeViewer.setSelection( selection );
    }
}
