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


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.HierarchyViewController;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: HierarchyView — The Rebel Briefing Room Hologram of Yavin ─────────
// In the Yavin briefing room, Mon Mothma and General Dodonna project a glowing
// 3D hologram of the Death Star above the table — everyone can see the whole
// structure, how the trenches connect to the exhaust port, where the turrets are,
// how the attack runs relate to each other. It's a tree of spatial relationships
// rendered as a navigable visual.
// HierarchyView does the same thing for LDAP schema types: it projects a glowing
// tree of parent/child inheritance relationships — "inetOrgPerson extends
// organizationalPerson extends person extends top" — so you can navigate the
// inheritance chain and understand how types relate.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Hierarchy View — an Eclipse ViewPart that shows the inheritance hierarchy of
 * a selected attribute type or object class. LDAP schema types form an inheritance
 * tree (similar to Java class hierarchies), and this view lets you navigate up to
 * ancestors and down to subtypes in a single tree widget. It also displays a one-line
 * summary above the tree showing the selected type's name, OID, and schema. Think of
 * it as the Rebel briefing room hologram: the full structure made visible and
 * navigable at once.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HierarchyView extends ViewPart
{
    /** The view's ID */
    public static final String ID = PluginConstants.VIEW_HIERARCHY_VIEW_ID;

    /** The tree viewer */
    private TreeViewer viewer;

    /** The controller */
    private HierarchyViewController controller;

    /** The Overview label */
    private Label overviewLabel;


    // ── The Briefing Room Opens for Business ─────────────────────────────────
    // The lights dim, the holographic projector hums to life, and Mon Mothma
    // steps to the front. The room is configured — labels at the top, separator,
    // hologram in the center — before the first word is spoken.
    // createPartControl does the same: zero-margin layout, overview label, separator,
    // tree viewer, controller — all initialized in order so the view is immediately
    // useful the moment Eclipse shows it.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Hierarchy View UI — called by Eclipse when this view is first shown.
     * We use a zero-margin GridLayout so the tree fills the view edge-to-edge, add
     * a wrapping overview label (the selected type's name, OID, and schema), a visual
     * separator, then the tree viewer. Finally we install the {@link HierarchyViewController}
     * which wires up the selection listener that populates the tree when the user picks
     * a type in another view.
     *
     * <p>For example — the briefing room goes live:</p>
     * <pre>
     *   createPartControl(parent)
     *     → overviewLabel.setText("")  // blank until a type is selected
     *     → initViewer(parent)         // holographic projector online
     *     → new HierarchyViewController(this)  // Mon Mothma takes the stage
     * </pre>
     *
     * @param parent  the SWT composite that Eclipse gives us to draw into
     */
    @Override
    public void createPartControl( Composite parent )
    {
        GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        gridLayout.verticalSpacing = 0;
        parent.setLayout( gridLayout );

        // Overview Label
        overviewLabel = new Label( parent, SWT.WRAP );
        overviewLabel.setText( "" ); //$NON-NLS-1$
        overviewLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Separator Label
        Label separatorLabel = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
        separatorLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        initViewer( parent );

        controller = new HierarchyViewController( this );

        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent, PluginConstants.PLUGIN_ID + "." + "hierarchy_view" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── The Holographic Projector Is Powered Up ──────────────────────────────
    // Before any hologram can appear, the projector needs to be calibrated:
    // the right content provider (knows what to show for each node), the right
    // label provider (knows how to render each node), and the rendering surface
    // itself — the TreeViewer widget with its CSS class name for styling.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates and configures the {@link TreeViewer} for the inheritance tree.
     * We attach {@link HierarchyViewContentProvider} to supply parent/child relationships
     * and a {@link DecoratingLabelProvider} wrapping {@link HierarchyViewLabelProvider}
     * so Eclipse decorators can add overlays (error badges etc.) on top of our icons.
     * The tree starts disabled — it has no content yet, and the controller will enable
     * it once a valid schema type is selected.
     *
     * <p>For example — powering up the projector:</p>
     * <pre>
     *   viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL)
     *   viewer.setContentProvider(new HierarchyViewContentProvider())
     *   viewer.setLabelProvider(new DecoratingLabelProvider(...))
     *   viewer.getTree().setEnabled(false)  // nothing to show yet
     * </pre>
     *
     * @param parent  the SWT composite to embed the tree into
     */
    private void initViewer( Composite parent )
    {
        viewer = new TreeViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
        viewer.getTree().setData( "org.eclipse.e4.ui.css.CssClassName", "studio-schema-tree" );
        viewer.setContentProvider( new HierarchyViewContentProvider() );
        viewer.setLabelProvider( new DecoratingLabelProvider( new HierarchyViewLabelProvider( viewer ), Activator
            .getDefault().getWorkbench().getDecoratorManager().getLabelDecorator() ) );
        viewer.getTree().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        viewer.getTree().setEnabled( false );
    }


    // ── Everyone's Eyes on the Hologram ──────────────────────────────────────
    // When the briefing starts, Dodonna points at the hologram and the room's
    // attention snaps to it. "Setfocus" is the UI equivalent: keyboard input
    // goes to our tree so the user can navigate it immediately.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Gives keyboard focus to the hierarchy tree widget.
     * Eclipse calls this whenever the user activates our view's tab. Routing focus
     * to the tree means arrow keys and Enter immediately navigate the hierarchy.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        viewer.getControl().setFocus();
    }


    // ── The Hologram Console Is Accessible ───────────────────────────────────
    // Other Rebel operatives need access to the projector console — to point it
    // at a new target, to expand it, to refresh the display. The controller gets
    // the viewer reference so it can drive the tree programmatically.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying {@link TreeViewer} for this view.
     * The {@link HierarchyViewController} uses this to set new inputs, trigger
     * refreshes, and respond to selection events from other views.
     *
     * @return  the {@link TreeViewer} showing the type hierarchy
     */
    public TreeViewer getViewer()
    {
        return viewer;
    }


    // ── The Hologram Refreshes with New Data ─────────────────────────────────
    // Mid-briefing, new reconnaissance data comes in and Dodonna updates the
    // projection — same structure, but the details have changed. The hologram
    // refreshes in place, then expands so everyone can see every node.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the tree viewer and expands all nodes so the full hierarchy is visible.
     * Call this after schema changes that may have affected the currently displayed
     * type's ancestors or descendants.
     */
    public void refresh()
    {
        viewer.refresh();
        viewer.expandAll();
    }


    // ── The Hologram Switches to a New Target ────────────────────────────────
    // "Change target from the exhaust port to the reactor core." Dodonna updates
    // the hologram's subject, expands the new view, and the overview label at the
    // top of the room changes to name the new target. setInput does this: it
    // replaces the tree's content and rebuilds the overview label for the new type.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the tree's content with the hierarchy for the given schema object,
     * expands all nodes, and updates the overview label.
     * Pass {@code null} to clear the view (e.g., when the user deselects everything).
     * The input can be an {@link AttributeType} or an {@link ObjectClass} — we dispatch
     * to the appropriate overview-label builder for each.
     *
     * <p>For example — switching target:</p>
     * <pre>
     *   setInput(inetOrgPersonObjectClass)
     *     → viewer.setInput(inetOrgPersonObjectClass)
     *     → viewer.expandAll()
     *     → overviewLabel = "inetOrgPerson (OID: 2.16.840.1.113730.3.2.2) [inetorgperson]"
     * </pre>
     *
     * @param input  the {@link AttributeType}, {@link ObjectClass}, or {@code null} to clear
     */
    public void setInput( Object input )
    {
        viewer.setInput( input );
        viewer.expandAll();
        if ( input == null )
        {
            overviewLabel.setText( "" ); //$NON-NLS-1$
        }
        else
        {
            if ( input instanceof AttributeType )
            {
                setOverviewLabel( ( AttributeType ) input );
            }
            else if ( input instanceof ObjectClass )
            {
                setOverviewLabel( ( ObjectClass ) input );
            }
            else
            {
                overviewLabel.setText( "" ); //$NON-NLS-1$
            }
        }
    }


    // ── The Briefing Header Names the Target ─────────────────────────────────
    // Every Rebel briefing starts with a header: "The target is the Death Star,
    // OID: DS-1, schema: Imperial." It names what we're looking at before we
    // dive into the detail. The overview label does exactly that — it identifies
    // the schema object by its aliases, OID, and schema name.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds and sets the overview label text for the given schema object.
     * The label shows the type's human-readable name (or "(None)" if it has no
     * aliases), its OID, and the name of the schema it belongs to. This is a private
     * helper shared by both the AttributeType and ObjectClass paths in {@link #setInput(Object)}.
     *
     * <p>For example — the briefing header:</p>
     * <pre>
     *   object = "cn" (OID: 2.5.4.3, schema: system)
     *   → overviewLabel = "cn (OID: 2.5.4.3) [system]"
     * </pre>
     *
     * @param object  the schema object whose details should appear in the overview label
     */
    private void setOverviewLabel( SchemaObject object )
    {
        StringBuffer sb = new StringBuffer();

        List<String> names = object.getNames();
        if ( ( names != null ) && ( names.size() > 0 ) )
        {
            sb.append( ViewUtils.concateAliases( names ) );
        }
        else
        {
            sb.append( Messages.getString( "HierarchyView.None" ) ); //$NON-NLS-1$
        }
        sb.append( NLS.bind(
            Messages.getString( "HierarchyView.Schema" ), new String[] { object.getOid(), object.getSchemaName() } ) ); //$NON-NLS-1$

        overviewLabel.setText( sb.toString() );
    }


    // ── The Briefing Room Stands Down ────────────────────────────────────────
    // The mission is complete or cancelled. The hologram powers down, the room
    // is cleared, and the controller releases everything it was holding — listeners,
    // references, subscriptions. We call super.dispose() to let the ViewPart clean up too.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the view is being closed or the workbench is shutting down.
     * We delegate to the controller's {@code dispose()} to unregister event listeners,
     * then call {@code super.dispose()} to let the base {@link ViewPart} clean up.
     * Skipping either step would cause listener leaks or resource leaks.
     *
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose()
    {
        controller.dispose();

        super.dispose();
    }
}
