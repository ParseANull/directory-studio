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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.difference.SchemaDifference;
import org.apache.directory.studio.schemaeditor.view.widget.Folder.FolderType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: DifferencesWidgetSchemaContentProvider — THE REBEL BRIEFING HOLOGRAM ─
// In the Rebel briefing room on Yavin, the hologram projects a hierarchical model
// of the Death Star: top level is the station itself, then sectors, then individual
// gun emplacements. The briefing officer navigates the hierarchy for the pilots.
// Our class does the same for schema differences: top level is the schema list,
// then each schema expands into its attribute-type and object-class sub-folders,
// and those folders expand into the individual differences.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link ITreeContentProvider} that builds the left-panel tree inside
 * {@link DifferencesWidget}. It accepts a {@code List<SchemaDifference>} as the
 * root input and recursively provides children for each level:
 * schemas → folders (optional) → individual attribute-type/object-class differences.
 * Grouping (flat mixed vs. folder-separated) and sort order are driven by the plugin's
 * preference store, so the tree adapts immediately when the user changes a preference.
 * Think of it as the Rebel briefing hologram: it always shows the hierarchy in the
 * format that best matches the current tactical preference.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DifferencesWidgetSchemaContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
    /** The preferences store */
    private IPreferenceStore store;

    /** The FirstName Sorter */
    private FirstNameSorter firstNameSorter;

    /** The OID Sorter */
    private OidSorter oidSorter;

    /** The Schema Sorter */
    private SchemaDifferenceSorter schemaDifferenceSorter;


    // ── THE BRIEFING OFFICER ARRIVES AT THE HOLOGRAM PROJECTOR ───────────────────
    // Before the pilots file in, the briefing officer sets up the projector: grabs
    // the preference store (which determines grouping and sort mode), and pre-creates
    // the three sorters so they are ready to be applied on demand.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new content provider, wiring it to the plugin preference store
     * and pre-creating all three sorters. We initialise early so that
     * {@link #getChildren(Object)} never has to allocate during a UI callback.
     */
    public DifferencesWidgetSchemaContentProvider()
    {
        store = Activator.getDefault().getPreferenceStore();

        firstNameSorter = new FirstNameSorter();
        oidSorter = new OidSorter();
        schemaDifferenceSorter = new SchemaDifferenceSorter();
    }


    // ── TOP OF THE HOLOGRAM: FLAT LIST FOR THE STRUCTURED VIEWER ─────────────────
    // The briefing room hologram shows the top level of the Death Star when first
    // activated. JFace calls getElements() to populate the top level of the tree —
    // we just delegate to getChildren() since the logic is the same.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the top-level elements for the tree, which for us is just the list of
     * {@link SchemaDifference} objects. We delegate straight to
     * {@link #getChildren(Object)} because the top-level population logic is identical
     * to the child-expansion logic for a list input.
     *
     * @param inputElement  the root input — expected to be a {@code List<SchemaDifference>}
     * @return              the top-level tree items as an {@code Object[]}
     */
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }


    // ── THE HOLOGRAM PROJECTOR POWERS DOWN ───────────────────────────────────────
    // After the briefing is over, the projector is switched off. We hold no SWT
    // resources, so there is genuinely nothing to release — the JVM GC handles it.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the tree viewer is disposed or its content provider is
     * replaced. We hold no SWT or other disposable resources, so nothing to do here.
     *
     * {@inheritDoc}
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── NEW TACTICAL DATA ARRIVES: INPUT CHANGES ──────────────────────────────────
    // Fresh intelligence arrives from the field and the hologram is updated. We do
    // not need to cache any viewer reference because getChildren() reads the store
    // live; the tree viewer handles the redraw automatically after this call.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the tree viewer's input is replaced. We do not need to
     * react to the old/new transition here; the viewer will call {@link #getChildren}
     * again with the new input and the tree will rebuild itself.
     *
     * @param viewer    the tree viewer whose input just changed
     * @param oldInput  the previous root input
     * @param newInput  the new root input
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do
    }


    // ── EXPANDING A NODE IN THE HOLOGRAM ─────────────────────────────────────────
    // The briefing officer zooms into a Death Star sector to show the sub-systems.
    // When the user expands a tree node, JFace calls getChildren(). We handle three
    // levels: a raw list (top-level schemas), a SchemaDifference (expands to folders
    // or mixed items), and a Folder (expands to individual differences inside it).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the child nodes for a given tree element. The exact children depend
     * on both the element type and the current grouping/sorting preferences:
     * <ul>
     *   <li>A {@code List<SchemaDifference>} returns the sorted top-level schema nodes.</li>
     *   <li>A {@link SchemaDifference} returns either two {@link Folder} nodes
     *       (for attribute types and object classes) or a flat sorted mix of both.</li>
     *   <li>A {@link Folder} returns its children sorted by the active sort preference.</li>
     * </ul>
     *
     * <p>For example — zooming into the Death Star hologram:</p>
     * <pre>
     *   List of schemas  →  [cosine (modified), core (identical), inetorgperson (added)]
     *   cosine expanded  →  [AT Folder, OC Folder]  or  [atDiff1, atDiff2, ocDiff1]
     *   AT Folder        →  [atDiff1, atDiff2]  sorted by first name or OID
     * </pre>
     *
     * @param parentElement  the tree node being expanded
     * @return               sorted array of child nodes, or an empty array if none
     */
    @SuppressWarnings("unchecked")
    public Object[] getChildren( Object parentElement )
    {
        List<Object> children = new ArrayList<Object>();

        int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
        int sortBy = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY );
        int sortOrder = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER );

        if ( parentElement instanceof List )
        {
            List<SchemaDifference> schemaDifferences = ( List<SchemaDifference> ) parentElement;

            children.addAll( schemaDifferences );

            Collections.sort( children, schemaDifferenceSorter );
        }
        else if ( parentElement instanceof SchemaDifference )
        {
            SchemaDifference difference = ( SchemaDifference ) parentElement;

            if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
            {
                Folder atFolder = new Folder( FolderType.ATTRIBUTE_TYPE );
                atFolder.addAllChildren( difference.getAttributeTypesDifferences() );
                children.add( atFolder );

                Folder ocFolder = new Folder( FolderType.OBJECT_CLASS );
                ocFolder.addAllChildren( difference.getObjectClassesDifferences() );
                children.add( ocFolder );
            }
            else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
            {
                children.addAll( difference.getAttributeTypesDifferences() );
                children.addAll( difference.getObjectClassesDifferences() );

                // Sort by
                if ( sortBy == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_FIRSTNAME )
                {
                    Collections.sort( children, firstNameSorter );
                }
                else if ( sortBy == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_OID )
                {
                    Collections.sort( children, oidSorter );
                }

                // Sort Order
                if ( sortOrder == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER_DESCENDING )
                {
                    Collections.reverse( children );
                }
            }
        }
        else if ( parentElement instanceof Folder )
        {
            children.addAll( ( ( Folder ) parentElement ).getChildren() );

            // Sort by
            if ( sortBy == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_FIRSTNAME )
            {
                Collections.sort( children, firstNameSorter );
            }
            else if ( sortBy == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_OID )
            {
                Collections.sort( children, oidSorter );
            }

            // Sort Order
            if ( sortOrder == PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER_DESCENDING )
            {
                Collections.reverse( children );
            }
        }

        return children.toArray();
    }


    // ── WHO BUILT THIS PART OF THE DEATH STAR?: FINDING THE PARENT ───────────────
    // The briefing hologram does not track upward navigation — we only ever drill
    // down, never back up. So getParent() always returns null; the tree viewer
    // handles collapsing internally without needing us to supply a parent reference.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent of the given element. We do not need to support upward
     * navigation in this tree, so we always return {@code null}. JFace handles
     * tree collapsing internally and does not require this method to work.
     *
     * @param element  the tree element whose parent is requested
     * @return         always {@code null}
     */
    public Object getParent( Object element )
    {
        // Default
        return null;
    }


    // ── CAN WE ZOOM IN FURTHER: DOES THIS NODE HAVE CHILDREN ─────────────────────
    // Before the briefing officer tries to expand a hologram node, they check whether
    // there is anything to zoom in on. SchemaDifference nodes always have children
    // (AT and OC lists), and Folder nodes always contain differences. Leaf-level
    // differences (individual AT/OC diffs) have no children.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given element has child nodes in the tree.
     * {@link SchemaDifference} and {@link Folder} nodes are always expandable;
     * individual difference items (attribute types, object classes) are leaf nodes.
     * JFace uses this to decide whether to show the expand triangle next to a node.
     *
     * @param element  the tree node to check
     * @return         {@code true} if it can be expanded, {@code false} if it is a leaf
     */
    public boolean hasChildren( Object element )
    {
        if ( element instanceof SchemaDifference )
        {
            return true;
        }
        else if ( element instanceof Folder )
        {
            return true;
        }

        // Default
        return false;
    }
}
