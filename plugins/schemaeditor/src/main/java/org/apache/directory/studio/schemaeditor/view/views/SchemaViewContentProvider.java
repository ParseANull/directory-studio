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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.hierarchy.HierarchyManager;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.FirstNameSorter;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder.FolderType;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.OidSorter;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaSorter;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaViewRoot;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: SchemaViewContentProvider — Lando Running Cloud City ───────────────
// Lando Calrissian doesn't just visit Cloud City — he runs every inch of it.
// Residents arrive (attributeTypeAdded, objectClassAdded, schemaAdded), relocate
// (attributeTypeModified, objectClassModified), and depart (attributeTypeRemoved,
// objectClassRemoved, schemaRemoved). Lando tracks every citizen in a registry,
// knows who lives under whom (the hierarchy), organizes them into sectors (folders
// or mixed grouping), sorts the display order (name or OID, ascending or descending),
// and keeps the manifest synchronized whenever anything changes.
// This content provider is Lando. It supplies the Schema View tree with its nodes,
// maintains an elements-to-wrappers map (the city registry), supports both flat
// (by schema) and hierarchical (by inheritance) presentation modes, and responds
// to add/modify/remove events from the SchemaHandler so the tree stays live.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the Schema View tree with its children, parents, and hasChildren flags.
 * The Schema View can show schema objects in two modes: "Flat" (grouped by schema name,
 * optionally with AT/OC sub-folders) or "Hierarchical" (arranged by LDAP inheritance
 * using {@link HierarchyManager}). In either mode we maintain an
 * {@code elementsToWrappersMap} that maps each raw schema object to its tree node wrapper(s),
 * allowing efficient add/modify/remove updates without rebuilding the entire tree. Think
 * of it as Lando running Cloud City: managing every citizen, tracking hierarchy, and
 * keeping the manifest consistent through every arrival, departure, and relocation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
    /** The preferences store */
    private IPreferenceStore store;

    /** The FirstName Sorter */
    private FirstNameSorter firstNameSorter;

    /** The OID Sorter */
    private OidSorter oidSorter;

    /** The Schema Sorter */
    private SchemaSorter schemaSorter;

    /** The RootWrapper */
    private SchemaViewRoot root;

    /** The 'Elements To Wrappers' Map */
    private ListValuedMap<Object, TreeNode> elementsToWrappersMap;

    private HierarchyManager hierarchyManager;


    // ── Lando Opens the City's Administration Office ──────────────────────────
    // Before any citizen can arrive, Lando sets up the administrative machinery:
    // the sorting protocols for names and OIDs, the schema-level sorter, and a
    // reference to the preference store. The elements-to-wrappers map and root
    // are initialized lazily on first getChildren call.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new content provider and initializes the three sorters and the preference store.
     * We create {@link FirstNameSorter}, {@link OidSorter}, and {@link SchemaSorter} here
     * so they're ready for every {@link #getChildren(Object)} call. The
     * {@code elementsToWrappersMap} and {@code root} are set lazily when the tree first
     * asks for children.
     */
    public SchemaViewContentProvider()
    {
        store = Activator.getDefault().getPreferenceStore();

        firstNameSorter = new FirstNameSorter();
        oidSorter = new OidSorter();
        schemaSorter = new SchemaSorter();
    }


    // ── Lando Reviews the City Manifest ──────────────────────────────────────
    // The top-level view of Cloud City is the same as asking for the first level
    // of residents — getElements delegates to getChildren since root and children
    // logic are identical.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the root-level elements for the Schema View tree.
     * Delegates directly to {@link #getChildren(Object)} — there's no distinction
     * between root elements and children in this tree.
     *
     * @param inputElement  the {@link SchemaViewRoot}
     * @return              the top-level tree nodes
     */
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }


    // ── Lando Steps Away — Nothing to Clean Up ────────────────────────────────
    // Cloud City keeps running even when Lando's not at his desk. No listeners
    // or OS resources need to be released here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called when this content provider is being released. Nothing to clean up.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── A New Census Arrives ──────────────────────────────────────────────────
    // When a new census replaces the old one, Lando doesn't have to rebuild the
    // whole city — the map rebuilds lazily on the next getChildren call. Nothing
    // to cache or clear here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called when the viewer's input changes. Since we rebuild lazily from the root
     * on each {@link #getChildren(Object)} call, we have nothing to cache here.
     *
     * @param viewer    the viewer whose input changed
     * @param oldInput  the previous root
     * @param newInput  the new root
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do
    }


    // ── Lando Shows the Residents of a Given Sector ──────────────────────────
    // Whether someone's asking about the whole city (SchemaViewRoot), a sector
    // (Folder), a specific resident's household (AttributeTypeWrapper / ObjectClassWrapper),
    // or a schema district (SchemaWrapper) — Lando produces the sorted list of
    // whoever lives there. getChildren handles all four cases, reading grouping,
    // sorting-by, and sorting-order from the preference store.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the children of the given tree node in the appropriate sort order.
     * The behavior depends on the node type:
     * <ul>
     *   <li>{@link SchemaViewRoot}: builds the whole tree if empty (flat or hierarchical mode)</li>
     *   <li>{@link Folder}: returns the folder's children, sorted</li>
     *   <li>{@link AttributeTypeWrapper}/{@link ObjectClassWrapper}: returns inheritance children, sorted</li>
     *   <li>{@link SchemaWrapper}: returns the schema's direct children, sorted</li>
     * </ul>
     * We read presentation mode, grouping, sort-by, and sort-order from the preference
     * store on every call, so changes to those settings take effect on the next tree expand.
     *
     * @param parentElement  the node whose children to return
     * @return               the sorted array of child {@link TreeNode} instances
     */
    public Object[] getChildren( Object parentElement )
    {
        List<TreeNode> children = new ArrayList<TreeNode>();

        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
        int sortBy = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY );
        int sortOrder = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER );

        if ( parentElement instanceof SchemaViewRoot )
        {
            root = ( SchemaViewRoot ) parentElement;

            if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
            {
                if ( root.getChildren().isEmpty() )
                {
                    elementsToWrappersMap = new ArrayListValuedHashMap<>();

                    SchemaHandler schemaHandler = Activator.getDefault().getSchemaHandler();
                    if ( schemaHandler != null )
                    {
                        List<Schema> schemas = schemaHandler.getSchemas();
                        for ( Schema schema : schemas )
                        {
                            addSchemaFlatPresentation( schema );
                        }
                    }
                }

                children = root.getChildren();

                Collections.sort( children, schemaSorter );
            }
            else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
            {
                if ( root.getChildren().isEmpty() )
                {
                    elementsToWrappersMap = new ArrayListValuedHashMap<>();

                    hierarchyManager = new HierarchyManager();

                    if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
                    {
                        Folder atFolder = new Folder( FolderType.ATTRIBUTE_TYPE, root );
                        Folder ocFolder = new Folder( FolderType.OBJECT_CLASS, root );
                        root.addChild( atFolder );
                        root.addChild( ocFolder );

                        List<Object> rootChildren = hierarchyManager.getChildren( hierarchyManager.getRootObject() );
                        if ( ( rootChildren != null ) && ( rootChildren.size() > 0 ) )
                        {
                            for ( Object rootChild : rootChildren )
                            {
                                TreeNode childNode = null;

                                // Creating the wrapper
                                if ( rootChild instanceof AttributeType )
                                {
                                    AttributeType at = ( AttributeType ) rootChild;
                                    childNode = new AttributeTypeWrapper( at, atFolder );
                                    atFolder.addChild( childNode );
                                }
                                else if ( rootChild instanceof ObjectClass )
                                {
                                    ObjectClass oc = ( ObjectClass ) rootChild;
                                    childNode = new ObjectClassWrapper( oc, ocFolder );
                                    ocFolder.addChild( childNode );
                                }

                                // Filling the 'Elements To Wrappers' Map
                                elementsToWrappersMap.put( rootChild, childNode );

                                // Recursively creating the hierarchy for all children
                                // of the root element.
                                addHierarchyChildren( childNode, hierarchyManager.getChildren( rootChild ) );
                            }
                        }
                    }
                    else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                    {
                        addHierarchyChildren( root, hierarchyManager.getChildren( hierarchyManager.getRootObject() ) );
                    }
                }

                children = root.getChildren();

                if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                {
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

        }
        else if ( parentElement instanceof Folder )
        {
            children = ( ( TreeNode ) parentElement ).getChildren();

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
        else if ( ( parentElement instanceof AttributeTypeWrapper ) || ( parentElement instanceof ObjectClassWrapper ) )
        {
            children = ( ( TreeNode ) parentElement ).getChildren();

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
        else if ( parentElement instanceof SchemaWrapper )
        {
            children = ( ( TreeNode ) parentElement ).getChildren();

            if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
            {
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

        return children.toArray();
    }


    // ── Lando Registers a Citizen's Household Recursively ────────────────────
    // When a new family arrives in Cloud City, Lando doesn't just register the
    // head of household — he registers every child, every dependent, all the way
    // down. addHierarchyChildren recursively walks the hierarchy and creates
    // wrapper nodes for each AT or OC child.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Recursively builds the hierarchy subtree for the given node.
     * For each item in {@code children}, we create either an {@link AttributeTypeWrapper}
     * or {@link ObjectClassWrapper}, add it as a child of {@code node}, register it in
     * {@code elementsToWrappersMap}, and then recurse into that child's own hierarchy
     * children via the {@link HierarchyManager}.
     *
     * @param node      the parent tree node to add children to
     * @param children  the schema objects to wrap and attach (from HierarchyManager)
     */
    private void addHierarchyChildren( TreeNode node, List<Object> children )
    {
        if ( ( children != null ) && ( children.size() > 0 ) )
        {
            for ( Object child : children )
            {
                TreeNode childNode = null;
                if ( child instanceof AttributeType )
                {
                    AttributeType at = ( AttributeType ) child;
                    childNode = new AttributeTypeWrapper( at, node );
                    node.addChild( childNode );
                }
                else if ( child instanceof ObjectClass )
                {
                    ObjectClass oc = ( ObjectClass ) child;
                    childNode = new ObjectClassWrapper( oc, node );
                    node.addChild( childNode );
                }

                // Filling the 'Elements To Wrappers' Map
                elementsToWrappersMap.put( child, childNode );

                // Recursively creating the hierarchy for all children
                // of the given element.
                addHierarchyChildren( childNode, hierarchyManager.getChildren( child ) );
            }
        }
    }


    // ── Lando Traces a Resident to Their District ─────────────────────────────
    // Every resident of Cloud City lives in a district — Lando can trace any
    // tree node back to its parent. getParent returns the TreeNode's parent so
    // JFace can programmatically reveal or select a node.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent node of the given tree element.
     * JFace calls this to reveal items programmatically (e.g., when the controller
     * wants to select a newly added attribute type in the tree).
     *
     * @param element  the tree element whose parent to find
     * @return         the parent {@link TreeNode}, or {@code null} if the element is root
     */
    public Object getParent( Object element )
    {
        if ( element instanceof TreeNode )
        {
            return ( ( TreeNode ) element ).getParent();
        }

        // Default
        return null;
    }


    // ── Does This District Have Sub-Districts? ────────────────────────────────
    // Not every sector of Cloud City has sub-sectors. hasChildren lets JFace know
    // whether to draw an expand arrow next to a tree node.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given tree node has children.
     * JFace uses this to decide whether to draw a collapse/expand arrow. Schemas have
     * children (their AT/OC content); folders have children; individual ATs or OCs may
     * have children in hierarchical mode (their subtypes); in flat mode they have none.
     *
     * @param element  the tree node to test
     * @return         {@code true} if this node has at least one child
     */
    public boolean hasChildren( Object element )
    {
        if ( element instanceof TreeNode )
        {
            return ( ( TreeNode ) element ).hasChildren();
        }

        // Default
        return false;
    }


    // ── Lando Looks Up All Households for an Object ──────────────────────────
    // In hierarchical mode an AT or OC might appear in multiple places — as a
    // child of two different parents. getWrappers returns all tree node wrappers
    // for a given schema object.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns all tree node wrappers associated with the given schema object.
     * In hierarchical mode a single schema object can appear multiple times in the tree
     * (as a child of each of its parents). The {@code elementsToWrappersMap} tracks all
     * such associations. Returns an empty list if the object has no wrappers yet.
     *
     * @param o  the schema object (AttributeType, ObjectClass, Schema, etc.)
     * @return   the list of all tree node wrappers for this object
     */
    @SuppressWarnings("unchecked")
    public List<TreeNode> getWrappers( Object o )
    {
        return ( List<TreeNode> ) elementsToWrappersMap.get( o );
    }


    // ── Lando Looks Up the Primary Household ─────────────────────────────────
    // When you only need one representative node (the "primary residence"), use
    // getWrapper. It returns the first entry from getWrappers.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first tree node wrapper for the given schema object, or {@code null}.
     * Convenience method that calls {@link #getWrappers(Object)} and returns the first
     * element. In flat mode each object has exactly one wrapper; in hierarchical mode
     * there may be multiple — this gives you the first one.
     *
     * @param o  the schema object to look up
     * @return   the first wrapper, or {@code null} if none exists
     */
    public TreeNode getWrapper( Object o )
    {
        List<TreeNode> wrappers = getWrappers( o );
        if ( ( wrappers != null ) && ( wrappers.size() > 0 ) )
        {
            return wrappers.get( 0 );
        }

        // Default
        return null;
    }


    // ── Lando Checks the City's Own Address ──────────────────────────────────
    // The city itself is the root — every district and every resident traces back
    // to it. getRoot returns the SchemaViewRoot so the controller can refresh
    // the whole tree.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the root node of the Schema View tree.
     * The controller uses this to force a full refresh of the tree when, for example,
     * a preference change requires rebuilding the entire structure.
     *
     * @return  the {@link SchemaViewRoot} that owns all top-level tree nodes
     */
    public SchemaViewRoot getRoot()
    {
        return root;
    }


    // ── Lando Registers a New Object in the City Registry ────────────────────
    // A new resident arrives and Lando adds their entry to the city registry
    // (the elements-to-wrappers map). The controller calls this after creating
    // a new wrapper in response to an add event.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Adds a mapping from a schema object to its tree node wrapper.
     * The controller calls this when it creates a new wrapper after receiving an
     * add event. We store the mapping in {@code elementsToWrappersMap} so later
     * lookups via {@link #getWrapper(Object)} or {@link #getWrappers(Object)} will find it.
     *
     * @param element  the schema object (key)
     * @param wrapper  the newly created tree node wrapper (value)
     */
    public void addElementToWrapper( Object element, TreeNode wrapper )
    {
        elementsToWrappersMap.put( element, wrapper );
    }


    // ── Lando Removes a Specific Registry Entry ───────────────────────────────
    // When a resident moves out and their specific wrapper is retired, Lando removes
    // exactly that mapping from the registry. removeElementToWrapper(element, wrapper)
    // removes a single element-wrapper pair.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes one specific element-to-wrapper mapping.
     * In hierarchical mode an object may have multiple wrappers; this removes just the
     * specified one without affecting others. Used when a subtree node is detached but
     * the object still exists elsewhere in the tree.
     *
     * @param element  the schema object
     * @param wrapper  the specific wrapper to remove
     */
    public void removeElementToWrapper( Object element, TreeNode wrapper )
    {
        elementsToWrappersMap.removeMapping( element, wrapper );
    }


    // ── Lando Removes All Registry Entries for an Object ─────────────────────
    // When a resident leaves Cloud City entirely (object removed), Lando wipes all
    // their entries from the registry. removeElementToWrapper(element) removes every
    // wrapper for the given schema object.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes all element-to-wrapper mappings for the given schema object.
     * Called when an attribute type, object class, or schema is fully removed from the
     * schema. After this call, {@link #getWrapper(Object)} will return {@code null} for
     * this object.
     *
     * @param element  the schema object whose all wrappers to remove
     */
    public void removeElementToWrapper( Object element )
    {
        elementsToWrappersMap.remove( element );
    }


    // ── A New Attribute Type Arrives in Cloud City ────────────────────────────
    // A new citizen arrives at Cloud City — an attribute type. Lando routes them
    // to the right sector based on the current presentation mode (flat or hierarchical).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles the addition of an attribute type to the schema.
     * Dispatches to the flat or hierarchical presentation handler based on the current
     * preference. The tree viewer is updated by the controller after this call.
     *
     * @param at  the newly added attribute type
     */
    public void attributeTypeAdded( AttributeType at )
    {
        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            attributeTypeAddedFlatPresentation( at );
        }
        else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            attributeTypeAddedHierarchicalPresentation( at );
        }
    }


    // ── The New AT Moves Into the Flat Schema District ────────────────────────
    // In flat mode, the new attribute type goes into its schema's district — either
    // into the AT folder (if folder grouping is on) or directly into the schema
    // wrapper (if mixed). Lando creates the wrapper and adds it to the registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Adds an attribute type wrapper when flat presentation mode is active.
     * We look up the schema wrapper for this AT's schema, then either find the AT folder
     * inside it (folder grouping) or use the schema wrapper directly (mixed grouping).
     * We create an {@link AttributeTypeWrapper}, add it as a child of the appropriate
     * node, and register it in {@code elementsToWrappersMap}.
     *
     * @param at  the newly added attribute type
     */
    public void attributeTypeAddedFlatPresentation( AttributeType at )
    {
        SchemaWrapper schemaWrapper = ( SchemaWrapper ) getWrapper( Activator.getDefault().getSchemaHandler()
            .getSchema( at.getSchemaName() ) );
        if ( schemaWrapper != null )
        {
            AttributeTypeWrapper atw = null;
            int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
            if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
            {
                for ( TreeNode child : schemaWrapper.getChildren() )
                {
                    if ( ( ( Folder ) child ).getType() == FolderType.ATTRIBUTE_TYPE )
                    {
                        atw = new AttributeTypeWrapper( at, child );
                        break;
                    }
                }
            }
            else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
            {
                atw = new AttributeTypeWrapper( at, schemaWrapper );
            }

            atw.getParent().addChild( atw );
            elementsToWrappersMap.put( at, atw );
        }
    }


    // ── The New AT Moves Into the Hierarchy District ──────────────────────────
    // In hierarchical mode it's more complex: we tell the hierarchy manager the AT
    // arrived, find its parents in the hierarchy, create wrappers under those parents,
    // then re-parent any existing children of the new AT that were previously floating
    // at the root level.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Adds an attribute type wrapper when hierarchical presentation mode is active.
     * We notify the {@link HierarchyManager} of the addition, look up the AT's parents
     * in the hierarchy, create a wrapper under each parent, and then move any of the AT's
     * known children to be children of the newly created wrappers. This handles the case
     * where children of the new AT were previously attached to the root (because their
     * parent didn't exist yet).
     *
     * @param at  the newly added attribute type
     */
    public void attributeTypeAddedHierarchicalPresentation( AttributeType at )
    {
        hierarchyManager.attributeTypeAdded( at );

        List<TreeNode> createdWrappers = new ArrayList<TreeNode>();

        List<Object> parents = hierarchyManager.getParents( at );

        if ( parents != null )
        {
            for ( Object parent : parents )
            {
                AttributeTypeWrapper parentATW = ( AttributeTypeWrapper ) getWrapper( parent );
                AttributeTypeWrapper atw = null;
                if ( parentATW == null )
                {
                    int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
                    if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
                    {
                        for ( TreeNode child : root.getChildren() )
                        {
                            if ( child instanceof Folder )
                            {
                                Folder folder = ( Folder ) child;
                                if ( folder.getType().equals( FolderType.ATTRIBUTE_TYPE ) )
                                {
                                    atw = new AttributeTypeWrapper( at, folder );
                                    break;
                                }
                            }
                        }
                    }
                    else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                    {
                        atw = new AttributeTypeWrapper( at, root );
                    }

                }
                else
                {
                    atw = new AttributeTypeWrapper( at, parentATW );
                }
                atw.getParent().addChild( atw );
                createdWrappers.add( atw );
                elementsToWrappersMap.put( at, atw );
            }
        }

        List<Object> children = hierarchyManager.getChildren( at );
        if ( children != null )
        {
            for ( Object child : children )
            {
                AttributeTypeWrapper childATW = ( AttributeTypeWrapper ) getWrapper( child );
                elementsToWrappersMap.remove( child );
                childATW.getParent().removeChild( childATW );

                for ( TreeNode createdWrapper : createdWrappers )
                {
                    AttributeTypeWrapper atw = new AttributeTypeWrapper( ( AttributeType ) child, createdWrapper );
                    atw.getParent().addChild( atw );
                    elementsToWrappersMap.put( child, atw );
                }
            }
        }
    }


    // ── An Attribute Type Changes Apartments ──────────────────────────────────
    // A citizen changes their details — maybe they change their SUP (parent) in
    // hierarchical mode, which means they move to a different part of the tree.
    // In flat mode this doesn't affect the tree structure (only the label changes,
    // and the label provider handles that independently).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles modification of an attribute type.
     * Dispatches to the flat or hierarchical handler. In flat mode this is a no-op
     * for the tree structure (the label provider handles display changes). In hierarchical
     * mode we may need to re-parent the node if the SUP changed.
     *
     * @param at  the modified attribute type
     */
    public void attributeTypeModified( AttributeType at )
    {
        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            attributeTypeModifiedFlatPresentation( at );
        }
        else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            attributeTypeModifiedHierarchicalPresentation( at );
        }
    }


    // ── Flat Mode: Modification Has No Tree Effect ────────────────────────────
    // In flat mode, changing an AT's properties doesn't move it in the tree — it
    // stays in the same schema, in the same folder. Nothing to do here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * No-op for attribute type modification in flat presentation mode.
     * In flat mode the tree structure doesn't change when an AT is modified — only the
     * label may change, and the label provider handles that by responding to viewer refresh.
     *
     * @param at  the modified attribute type (ignored here)
     */
    public void attributeTypeModifiedFlatPresentation( AttributeType at )
    {
        // Nothing to do
    }


    // ── Hierarchy Mode: The AT Moves to Its New Parent ────────────────────────
    // In hierarchical mode, if an AT's SUP changed, its position in the tree changes.
    // We remove all existing wrappers, propagate the change to the hierarchy manager,
    // find the new parents, and create new wrappers under them, recursively rebuilding
    // their subtrees.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Updates the tree when an attribute type is modified in hierarchical mode.
     * We propagate the change to the {@link HierarchyManager}, remove all existing
     * wrappers from the tree, then look up the AT's (possibly changed) parents and
     * create new wrappers under them. We also rebuild the AT's children subtree under
     * each new wrapper.
     *
     * @param at  the modified attribute type
     */
    public void attributeTypeModifiedHierarchicalPresentation( AttributeType at )
    {
        // Propagating the modification to the hierarchy manager
        hierarchyManager.attributeTypeModified( at );

        // Removing the Wrappers
        List<TreeNode> wrappers = getWrappers( at );
        if ( wrappers != null )
        {
            for ( TreeNode wrapper : wrappers )
            {
                wrapper.getParent().removeChild( wrapper );
            }

            elementsToWrappersMap.remove( at );
        }

        // Creating the wrapper
        List<Object> parents = hierarchyManager.getParents( at );
        if ( parents != null )
        {
            for ( Object parent : parents )
            {
                AttributeTypeWrapper parentATW = ( AttributeTypeWrapper ) getWrapper( parent );
                AttributeTypeWrapper atw = null;
                if ( parentATW == null )
                {
                    int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
                    if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
                    {
                        for ( TreeNode child : root.getChildren() )
                        {
                            if ( child instanceof Folder )
                            {
                                Folder folder = ( Folder ) child;
                                if ( folder.getType().equals( FolderType.ATTRIBUTE_TYPE ) )
                                {
                                    atw = new AttributeTypeWrapper( at, folder );
                                    break;
                                }
                            }
                        }
                    }
                    else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                    {
                        atw = new AttributeTypeWrapper( at, root );
                    }
                }
                else
                {
                    atw = new AttributeTypeWrapper( at, parentATW );
                }
                atw.getParent().addChild( atw );
                elementsToWrappersMap.put( at, atw );
                addHierarchyChildren( atw, hierarchyManager.getChildren( at ) );
            }
        }
    }


    // ── An Attribute Type Leaves Cloud City ───────────────────────────────────
    // A citizen departs. Lando routes the departure handling to the flat or
    // hierarchical handler, which removes the wrapper from the tree and the registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles removal of an attribute type from the schema.
     * Dispatches to the flat or hierarchical removal handler.
     *
     * @param at  the removed attribute type
     */
    public void attributeTypeRemoved( AttributeType at )
    {
        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            attributeTypeRemovedFlatPresentation( at );
        }
        else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            attributeTypeRemovedHierarchicalPresentation( at );
        }
    }


    // ── Flat Mode: Remove the AT's Wrapper ───────────────────────────────────
    // In flat mode removal is simple: look up the wrapper, detach it from its parent,
    // and remove it from the registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes an attribute type wrapper in flat presentation mode.
     * Looks up the wrapper in the map, removes it from its parent node, and removes
     * the mapping from {@code elementsToWrappersMap}.
     *
     * @param at  the removed attribute type
     */
    private void attributeTypeRemovedFlatPresentation( AttributeType at )
    {
        AttributeTypeWrapper atw = ( AttributeTypeWrapper ) getWrapper( at );
        if ( atw != null )
        {
            atw.getParent().removeChild( atw );
            elementsToWrappersMap.removeMapping( at, atw );
        }
    }


    // ── Hierarchy Mode: Re-Parent the AT's Children First ────────────────────
    // In hierarchical mode, before we remove the AT we first promote its children
    // to the root (or AT folder) level — they still exist, they just lost their parent.
    // Then we remove the AT's own wrappers and their subtrees, and finally propagate
    // the removal to the hierarchy manager.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes an attribute type wrapper in hierarchical presentation mode.
     * First, for each child of the removed AT in the hierarchy, we create new wrappers
     * at the root level (or AT folder) because those children's parent is gone. Then we
     * remove all wrappers for the removed AT (and recursively their subtrees), and finally
     * propagate the removal to the {@link HierarchyManager}.
     *
     * @param at  the removed attribute type
     */
    private void attributeTypeRemovedHierarchicalPresentation( AttributeType at )
    {
        // Creating children nodes of the AT
        // and attaching them to the root
        List<Object> children = hierarchyManager.getChildren( at );
        if ( children != null )
        {
            for ( Object child : children )
            {
                AttributeTypeWrapper atw = null;
                int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
                if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
                {
                    for ( TreeNode rootChild : root.getChildren() )
                    {
                        if ( rootChild instanceof Folder )
                        {
                            Folder folder = ( Folder ) rootChild;
                            if ( folder.getType().equals( FolderType.ATTRIBUTE_TYPE ) )
                            {
                                atw = new AttributeTypeWrapper( ( AttributeType ) child, folder );
                                break;
                            }
                        }
                    }
                }
                else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                {
                    atw = new AttributeTypeWrapper( ( AttributeType ) child, root );
                }

                atw.getParent().addChild( atw );
                elementsToWrappersMap.put( child, atw );
            }
        }

        // Removing the Wrappers
        List<TreeNode> wrappers = getWrappers( at );
        if ( wrappers != null )
        {
            for ( TreeNode wrapper : wrappers )
            {
                wrapper.getParent().removeChild( wrapper );
                removeRecursiveChildren( wrapper );
            }

            elementsToWrappersMap.remove( at );
        }

        // Propagating the removal to the hierarchy manager
        hierarchyManager.attributeTypeRemoved( at );
    }


    // ── Lando Clears an Entire Sub-Block ─────────────────────────────────────
    // When an entire housing block is demolished, Lando walks through every
    // apartment and clears the registry entries for all residents within.
    // removeRecursiveChildren does this: it recursively removes all children's
    // map entries so nothing points to a stale node.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Recursively removes all descendant nodes from {@code elementsToWrappersMap}.
     * Called when an AT or OC wrapper is being removed and we need to clean up every
     * wrapper in its subtree. We walk every child, remove its map entry, and recurse.
     * This prevents stale entries from accumulating in the map.
     *
     * @param wrapper  the parent wrapper whose entire subtree to de-register
     */
    private void removeRecursiveChildren( TreeNode wrapper )
    {
        for ( TreeNode child : wrapper.getChildren() )
        {
            if ( child instanceof AttributeTypeWrapper )
            {
                AttributeTypeWrapper atw = ( AttributeTypeWrapper ) child;
                elementsToWrappersMap.removeMapping( atw.getAttributeType(), child );
                removeRecursiveChildren( atw );
            }
            else if ( child instanceof ObjectClassWrapper )
            {
                ObjectClassWrapper ocw = ( ObjectClassWrapper ) child;
                elementsToWrappersMap.removeMapping( ocw.getObjectClass(), child );
                removeRecursiveChildren( ocw );
            }
            else
            {
                removeRecursiveChildren( child );
            }
        }
    }


    // ── A New Object Class Arrives ────────────────────────────────────────────
    // Another new citizen — this time an object class. The routing logic is the
    // same as for attribute types: flat or hierarchical handler.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles the addition of an object class to the schema.
     * Dispatches to the flat or hierarchical presentation handler.
     *
     * @param oc  the newly added object class
     */
    public void objectClassAdded( ObjectClass oc )
    {
        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            objectClassAddedFlatPresentation( oc );
        }
        else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            objectClassAddedHierarchicalPresentation( oc );
        }
    }


    // ── Flat Mode: The OC Moves Into Its Schema District ─────────────────────
    // Same as for ATs in flat mode: find the schema wrapper, find the OC folder
    // (or use the schema directly in mixed mode), create the wrapper, add and register.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Adds an object class wrapper in flat presentation mode.
     * Finds the schema wrapper for this OC's schema, then creates an {@link ObjectClassWrapper}
     * under either the OC folder (folder grouping) or the schema wrapper itself (mixed).
     * Registers the new wrapper in {@code elementsToWrappersMap}.
     *
     * @param oc  the newly added object class
     */
    public void objectClassAddedFlatPresentation( ObjectClass oc )
    {
        SchemaWrapper schemaWrapper = ( SchemaWrapper ) getWrapper( Activator.getDefault().getSchemaHandler()
            .getSchema( oc.getSchemaName() ) );
        if ( schemaWrapper != null )
        {
            ObjectClassWrapper ocw = null;
            int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
            if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
            {
                for ( TreeNode child : schemaWrapper.getChildren() )
                {
                    if ( ( ( Folder ) child ).getType() == FolderType.OBJECT_CLASS )
                    {
                        ocw = new ObjectClassWrapper( oc, child );
                        break;
                    }
                }
            }
            else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
            {
                ocw = new ObjectClassWrapper( oc, schemaWrapper );
            }

            ocw.getParent().addChild( ocw );
            elementsToWrappersMap.put( oc, ocw );
        }
    }


    // ── Hierarchy Mode: The OC Inserts Itself Into the Inheritance Chain ─────
    // Adding an OC in hierarchical mode is the most complex case: we must remove
    // any "orphan" children that were floating at the root (because their parent
    // didn't exist yet), notify the hierarchy manager, create new wrappers under
    // the OC's parents, and then move the OC's children to be under the new wrappers.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Adds an object class wrapper in hierarchical presentation mode.
     * First, for each existing child of the new OC (including "top"'s children), we
     * remove any root-level wrappers (orphans that were floating because their parent
     * wasn't loaded yet). Then we notify the {@link HierarchyManager}, find the new OC's
     * parents, create wrappers under them, and move the OC's children to be under the
     * new wrappers. This correctly handles both the common case and the special case of
     * adding "top" (OID 2.5.6.0).
     *
     * @param oc  the newly added object class
     */
    public void objectClassAddedHierarchicalPresentation( ObjectClass oc )
    {
        // Removing unattached nodes for "top"
        List<Object> ocChildren = new ArrayList<Object>();
        List<Object> ocChildren2 = null;
        if ( "2.5.6.0".equals( oc.getOid() ) ) //$NON-NLS-1$
        {
            ocChildren2 = hierarchyManager.getChildren( "2.5.6.0" ); //$NON-NLS-1$
            if ( ocChildren2 != null )
            {
                ocChildren.addAll( ocChildren2 );
            }
            ocChildren2 = hierarchyManager.getChildren( "top" ); //$NON-NLS-1$
            if ( ocChildren2 != null )
            {
                ocChildren.addAll( ocChildren2 );
            }
        }
        ocChildren2 = hierarchyManager.getChildren( oc );
        if ( ocChildren2 != null )
        {
            ocChildren.addAll( ocChildren2 );
        }
        for ( Object ocChild : ocChildren )
        {
            List<TreeNode> wrappers = getWrappers( ocChild );
            if ( wrappers != null )
            {
                for ( TreeNode wrapper : wrappers )
                {
                    int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
                    if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
                    {
                        if ( wrapper.getParent().getParent().equals( root ) )
                        {
                            wrapper.getParent().removeChild( wrapper );
                            elementsToWrappersMap.removeMapping( oc, wrapper );
                        }
                    }
                    else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                    {
                        if ( wrapper.getParent().equals( root ) )
                        {
                            wrapper.getParent().removeChild( wrapper );
                            elementsToWrappersMap.removeMapping( oc, wrapper );
                        }
                    }
                    removeRecursiveChildren( wrapper );
                }
            }
        }

        // Propagating the addition to the hierarchy manager
        hierarchyManager.objectClassAdded( oc );

        List<TreeNode> createdWrappers = new ArrayList<TreeNode>();

        List<Object> parents = hierarchyManager.getParents( oc );

        if ( parents != null )
        {
            for ( Object parent : parents )
            {
                ObjectClassWrapper parentOCW = ( ObjectClassWrapper ) getWrapper( parent );
                ObjectClassWrapper ocw = null;
                if ( parentOCW == null )
                {
                    int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
                    if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
                    {
                        for ( TreeNode child : root.getChildren() )
                        {
                            if ( child instanceof Folder )
                            {
                                Folder folder = ( Folder ) child;
                                if ( folder.getType().equals( FolderType.OBJECT_CLASS ) )
                                {
                                    ocw = new ObjectClassWrapper( oc, folder );
                                    break;
                                }
                            }
                        }
                    }
                    else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                    {
                        ocw = new ObjectClassWrapper( oc, root );
                    }

                }
                else
                {
                    ocw = new ObjectClassWrapper( oc, parentOCW );
                }
                ocw.getParent().addChild( ocw );
                createdWrappers.add( ocw );
                elementsToWrappersMap.put( oc, ocw );
            }
        }

        List<Object> children = hierarchyManager.getChildren( oc );
        if ( children != null )
        {
            for ( Object child : children )
            {
                List<TreeNode> childOCWs = getWrappers( child );
                if ( childOCWs != null )
                {
                    for ( TreeNode childOCW : childOCWs )
                    {
                        if ( root.equals( childOCW.getParent() ) )
                        {
                            elementsToWrappersMap.remove( child );
                            childOCW.getParent().removeChild( childOCW );
                        }
                    }
                }

                for ( TreeNode createdWrapper : createdWrappers )
                {
                    ObjectClassWrapper ocw = new ObjectClassWrapper( ( ObjectClass ) child, createdWrapper );
                    ocw.getParent().addChild( ocw );
                    elementsToWrappersMap.put( child, ocw );
                    addHierarchyChildren( ocw, hierarchyManager.getChildren( child ) );
                }
            }
        }
    }


    // ── An Object Class Changes Details ───────────────────────────────────────
    // A citizen updates their record. In flat mode nothing moves; in hierarchical
    // mode the OC might have changed its SUP and needs to be repositioned.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles modification of an object class.
     * Dispatches to the flat or hierarchical modification handler.
     *
     * @param oc  the modified object class
     */
    public void objectClassModified( ObjectClass oc )
    {
        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            objectClassModifiedFlatPresentation( oc );
        }
        else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            objectClassModifiedHierarchicalPresentation( oc );
        }
    }


    // ── Flat Mode: OC Modification Has No Tree Effect ────────────────────────
    // In flat mode the tree structure doesn't change when an OC is modified — the
    // label refreshes independently. Nothing to do here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * No-op for object class modification in flat presentation mode.
     *
     * @param oc  the modified object class (ignored)
     */
    public void objectClassModifiedFlatPresentation( ObjectClass oc )
    {
        // Nothing to do
    }


    // ── Hierarchy Mode: The OC Moves to Its New Parent ───────────────────────
    // In hierarchical mode an OC's modification may change its SUP — meaning it
    // needs to be moved to a new parent node in the tree. We propagate to the
    // hierarchy manager, remove old wrappers, create new ones under the updated
    // parents, and recursively rebuild the children subtrees.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Updates the tree when an object class is modified in hierarchical presentation mode.
     * Propagates the change to the {@link HierarchyManager}, removes old wrappers, looks up
     * the new parent set, creates new wrappers under those parents, and rebuilds the
     * subtree for each new wrapper using {@link #addHierarchyChildren}.
     *
     * @param oc  the modified object class
     */
    public void objectClassModifiedHierarchicalPresentation( ObjectClass oc )
    {
        // Propagating the modification to the hierarchy manager
        hierarchyManager.objectClassModified( oc );

        // Removing the Wrappers
        List<TreeNode> wrappers = getWrappers( oc );
        if ( wrappers != null )
        {
            for ( TreeNode wrapper : wrappers )
            {
                wrapper.getParent().removeChild( wrapper );
            }

            elementsToWrappersMap.remove( oc );
        }

        // Creating the wrapper
        List<Object> parents = hierarchyManager.getParents( oc );
        if ( parents != null )
        {
            for ( Object parent : parents )
            {
                ObjectClassWrapper parentOCW = ( ObjectClassWrapper ) getWrapper( parent );
                ObjectClassWrapper ocw = null;
                if ( parentOCW == null )
                {
                    int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
                    if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
                    {
                        for ( TreeNode child : root.getChildren() )
                        {
                            if ( child instanceof Folder )
                            {
                                Folder folder = ( Folder ) child;
                                if ( folder.getType().equals( FolderType.OBJECT_CLASS ) )
                                {
                                    ocw = new ObjectClassWrapper( oc, folder );
                                    break;
                                }
                            }
                        }
                    }
                    else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                    {
                        ocw = new ObjectClassWrapper( oc, root );
                    }
                }
                else
                {
                    ocw = new ObjectClassWrapper( oc, parentOCW );
                }
                ocw.getParent().addChild( ocw );
                elementsToWrappersMap.put( oc, ocw );
                addHierarchyChildren( ocw, hierarchyManager.getChildren( oc ) );
            }
        }
    }


    // ── An Object Class Leaves Cloud City ─────────────────────────────────────
    // A citizen departs. Routes to the flat or hierarchical removal handler.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles removal of an object class from the schema.
     * Dispatches to the flat or hierarchical removal handler.
     *
     * @param oc  the removed object class
     */
    public void objectClassRemoved( ObjectClass oc )
    {
        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            objectClassRemovedFlatPresentation( oc );
        }
        else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            objectClassRemovedHierarchicalPresentation( oc );
        }
    }


    // ── Flat Mode: Remove the OC's Wrapper ────────────────────────────────────
    // Look up the wrapper, detach from its parent, remove from the registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes an object class wrapper in flat presentation mode.
     *
     * @param oc  the removed object class
     */
    public void objectClassRemovedFlatPresentation( ObjectClass oc )
    {
        ObjectClassWrapper ocw = ( ObjectClassWrapper ) getWrapper( oc );
        if ( ocw != null )
        {
            ocw.getParent().removeChild( ocw );
            elementsToWrappersMap.removeMapping( oc, ocw );
        }
    }


    // ── Hierarchy Mode: Re-Parent the OC's Children First ────────────────────
    // Same pattern as for ATs in hierarchical remove: promote the OC's children
    // to the root level before removing the OC itself, then propagate to the
    // hierarchy manager.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes an object class wrapper in hierarchical presentation mode.
     * First promotes the OC's direct children to the root (or OC folder) level — they
     * still exist, they just lost their parent. Then removes all wrappers for this OC
     * (and recursively their subtrees). Finally propagates the removal to the
     * {@link HierarchyManager}.
     *
     * @param oc  the removed object class
     */
    public void objectClassRemovedHierarchicalPresentation( ObjectClass oc )
    {
        // Creating children nodes of the OC
        // and attaching them to the root
        List<Object> children = hierarchyManager.getChildren( oc );
        if ( children != null )
        {
            for ( Object child : children )
            {
                ObjectClassWrapper ocw = null;
                int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
                if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
                {
                    for ( TreeNode rootChild : root.getChildren() )
                    {
                        if ( rootChild instanceof Folder )
                        {
                            Folder folder = ( Folder ) rootChild;
                            if ( folder.getType().equals( FolderType.OBJECT_CLASS ) )
                            {
                                ocw = new ObjectClassWrapper( ( ObjectClass ) child, folder );
                                break;
                            }
                        }
                    }
                }
                else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
                {
                    ocw = new ObjectClassWrapper( ( ObjectClass ) child, root );
                }

                ocw.getParent().addChild( ocw );
                elementsToWrappersMap.put( child, ocw );
                addHierarchyChildren( ocw, hierarchyManager.getChildren( child ) );
            }
        }

        // Removing the Wrappers
        List<TreeNode> wrappers = getWrappers( oc );
        if ( wrappers != null )
        {
            for ( TreeNode wrapper : wrappers )
            {
                wrapper.getParent().removeChild( wrapper );
                removeRecursiveChildren( wrapper );
            }

            elementsToWrappersMap.remove( oc );
        }

        // Propagating the removal to the hierarchy manager
        hierarchyManager.objectClassRemoved( oc );
    }


    // ── An Entire Schema District Opens ──────────────────────────────────────
    // A whole new district opens in Cloud City — a new schema with all its ATs
    // and OCs. Lando dispatches to the flat or hierarchical schema-add handler.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles the addition of an entire schema to the schema view.
     * Dispatches to the flat or hierarchical handler to integrate all the schema's
     * attribute types and object classes into the tree.
     *
     * @param schema  the newly added schema
     */
    public void schemaAdded( Schema schema )
    {
        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            schemaAddedFlatPresentation( schema );
        }
        else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            schemaAddedHierarchicalPresentation( schema );
        }
    }


    // ── Flat Mode: Add the Schema's District ─────────────────────────────────
    // In flat mode, adding a schema just means building its district tree. We
    // delegate to addSchemaFlatPresentation which creates the schema wrapper,
    // folders, and all AT/OC wrappers.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Adds a schema and all its contents in flat presentation mode.
     * Delegates to {@link #addSchemaFlatPresentation(Schema)} which creates the full
     * schema wrapper subtree.
     *
     * @param schema  the newly added schema
     */
    private void schemaAddedFlatPresentation( Schema schema )
    {
        addSchemaFlatPresentation( schema );
    }


    // ── Hierarchy Mode: Add All of the Schema's Contents ────────────────────
    // In hierarchical mode there's no schema-level node — the ATs and OCs are
    // positioned by their inheritance relationships. We iterate the schema's
    // contents and call the individual add handlers for each AT and OC.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Adds a schema's contents in hierarchical presentation mode.
     * In hierarchical mode schemas don't appear as top-level nodes — instead their
     * attribute types and object classes are arranged by inheritance. We call
     * {@link #attributeTypeAddedHierarchicalPresentation} and
     * {@link #objectClassAddedHierarchicalPresentation} for each item.
     *
     * @param schema  the newly added schema
     */
    private void schemaAddedHierarchicalPresentation( Schema schema )
    {
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            attributeTypeAddedHierarchicalPresentation( at );
        }

        for ( ObjectClass oc : schema.getObjectClasses() )
        {
            objectClassAddedHierarchicalPresentation( oc );
        }
    }


    // ── A Schema District Closes ──────────────────────────────────────────────
    // A whole district is closed and its residents cleared. Routes to the flat or
    // hierarchical schema-remove handler.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles removal of an entire schema from the schema view.
     * Dispatches to the flat or hierarchical handler to remove all the schema's
     * attribute types, object classes, and the schema wrapper itself.
     *
     * @param schema  the removed schema
     */
    public void schemaRemoved( Schema schema )
    {
        int presentation = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT )
        {
            schemaRemovedFlatPresentation( schema );
        }
        else if ( presentation == PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_HIERARCHICAL )
        {
            schemaRemovedHierarchicalPresentation( schema );
        }
    }


    // ── Flat Mode: Remove the Schema's District and All Its Residents ────────
    // In flat mode removing a schema means finding its schema wrapper, detaching it
    // from root, removing it from the registry, and recursively clearing all its
    // AT and OC child wrappers.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes a schema and all its wrappers in flat presentation mode.
     * Finds the schema wrapper via the registry, detaches it from the root, removes it
     * from the map, and calls {@link #removeRecursiveChildren} to de-register all AT and
     * OC wrappers within it.
     *
     * @param schema  the removed schema
     */
    private void schemaRemovedFlatPresentation( Schema schema )
    {
        SchemaWrapper sw = ( SchemaWrapper ) getWrapper( schema );
        if ( sw != null )
        {
            sw.getParent().removeChild( sw );
            elementsToWrappersMap.removeMapping( schema, sw );
            removeRecursiveChildren( sw );
        }
    }


    // ── Hierarchy Mode: Remove All of the Schema's Contents ──────────────────
    // In hierarchical mode we iterate the schema's ATs and OCs and call the
    // individual hierarchical-remove handlers for each, which handles re-parenting
    // orphaned children and cleaning up the hierarchy manager.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes a schema's contents in hierarchical presentation mode.
     * Iterates the schema's attribute types and object classes and calls the appropriate
     * hierarchical removal handler for each.
     *
     * @param schema  the removed schema
     */
    private void schemaRemovedHierarchicalPresentation( Schema schema )
    {
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            attributeTypeRemovedHierarchicalPresentation( at );
        }

        for ( ObjectClass oc : schema.getObjectClasses() )
        {
            objectClassRemovedHierarchicalPresentation( oc );
        }
    }


    // ── Lando Builds a New Schema District From Scratch ──────────────────────
    // When a brand new schema district opens, Lando builds out the whole thing:
    // creates the schema wrapper, optionally adds AT and OC folder sectors, and
    // adds a wrapper for each AT and OC in the schema. Everything goes into the
    // registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds and attaches the full flat-presentation subtree for a schema.
     * Creates a {@link SchemaWrapper} under the root, registers it, then (depending on
     * grouping preference) either creates AT and OC {@link Folder} nodes containing
     * the wrapped attribute types and object classes, or adds wrappers directly under
     * the schema wrapper in mixed mode. All wrappers are registered in
     * {@code elementsToWrappersMap}.
     *
     * <p>For example — Lando opens a new district:</p>
     * <pre>
     *   addSchemaFlatPresentation(inetorgpersonSchema)
     *   → SchemaWrapper(inetorgperson) at root
     *     → Folder(ATTRIBUTE_TYPE)
     *       → AttributeTypeWrapper(cn), AttributeTypeWrapper(sn), ...
     *     → Folder(OBJECT_CLASS)
     *       → ObjectClassWrapper(inetOrgPerson), ...
     * </pre>
     *
     * @param schema  the schema to build the flat-presentation subtree for
     */
    public void addSchemaFlatPresentation( Schema schema )
    {
        SchemaWrapper schemaWrapper = new SchemaWrapper( schema, root );
        root.addChild( schemaWrapper );
        elementsToWrappersMap.put( schema, schemaWrapper );

        int group = store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
        if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS )
        {
            Folder atFolder = new Folder( FolderType.ATTRIBUTE_TYPE, schemaWrapper );
            schemaWrapper.addChild( atFolder );

            for ( AttributeType attributeType : schema.getAttributeTypes() )
            {
                AttributeTypeWrapper atw = new AttributeTypeWrapper( attributeType, atFolder );
                atw.getParent().addChild( atw );
                elementsToWrappersMap.put( attributeType, atw );
            }

            Folder ocFolder = new Folder( FolderType.OBJECT_CLASS, schemaWrapper );
            schemaWrapper.addChild( ocFolder );

            for ( ObjectClass objectClass : schema.getObjectClasses() )
            {
                ObjectClassWrapper ocw = new ObjectClassWrapper( objectClass, ocFolder );
                ocw.getParent().addChild( ocw );
                elementsToWrappersMap.put( objectClass, ocw );
            }
        }
        else if ( group == PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_MIXED )
        {
            for ( AttributeType attributeType : schema.getAttributeTypes() )
            {
                AttributeTypeWrapper atw = new AttributeTypeWrapper( attributeType, schemaWrapper );
                atw.getParent().addChild( atw );
                elementsToWrappersMap.put( attributeType, atw );
            }

            for ( ObjectClass objectClass : schema.getObjectClasses() )
            {
                ObjectClassWrapper ocw = new ObjectClassWrapper( objectClass, schemaWrapper );
                ocw.getParent().addChild( ocw );
                elementsToWrappersMap.put( objectClass, ocw );
            }
        }
    }
}
