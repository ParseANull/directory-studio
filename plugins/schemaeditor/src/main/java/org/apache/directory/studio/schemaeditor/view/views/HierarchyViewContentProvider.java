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
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.hierarchy.HierarchyManager;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: HierarchyViewContentProvider — Yoda Lifting the X-wing ────────────
// On Dagobah, Luke stares at his sunken X-wing and insists "it's too big."
// Yoda closes his eyes, reaches out through the Force, and lifts the whole ship
// out of the swamp — transforming a flat, muddy mess into a structured,
// navigable form hovering in the air. Luke didn't see the shape in the mud;
// Yoda made it visible.
// This class does the same thing: it takes a flat schema object (an ObjectClass
// or AttributeType sitting in the "mud" of the schema registry) and transforms it
// into a structured parent/child tree that the JFace TreeViewer can navigate.
// Parents float above, children hang below, and the current type sits in the middle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies the tree structure for the Hierarchy View — turning flat schema objects into
 * a navigable parent/child hierarchy. JFace's {@link ITreeContentProvider} contract means
 * we answer three questions: what are this node's children, what is this node's parent,
 * and does this node have children at all? We support three display modes: "type hierarchy"
 * (both ancestors and descendants), "supertype" (ancestors only), and "subtype"
 * (descendants only), all controlled by user preferences. Think of it as Yoda lifting the
 * X-wing: we transform something that was just sitting flat in a registry into a structured
 * shape you can navigate top-to-bottom.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HierarchyViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
    // ── Yoda Lifts the Ship into View ────────────────────────────────────────
    // This is the entry point — the moment Yoda raises his hand and the ship
    // begins to move. getElements is called by the TreeViewer for the root-level
    // content; we just delegate to getChildren since the logic is identical.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the top-level elements for the tree. Since the hierarchy always starts
     * at the root of the inheritance chain (or at the selected type itself in subtype
     * mode), we just delegate to {@link #getChildren(Object)} — the logic is the same
     * for root and child nodes.
     *
     * @param inputElement  the root input object (an {@link AttributeType} or {@link ObjectClass})
     * @return              the top-level tree nodes for the hierarchy
     */
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }


    // ── The Ship Takes Its Shape ─────────────────────────────────────────────
    // As the X-wing rises, you can see its structure: wings, engines, cockpit —
    // each part connected to the next. getChildren reveals the structure of any
    // given node: if it's an ObjectClass or AttributeType, we build the full
    // hierarchy from scratch; if it's already a TreeNode wrapper, we just
    // return its pre-built children.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the children of the given tree node.
     * For raw schema objects ({@link ObjectClass} or {@link AttributeType}) we build
     * the full hierarchy tree from scratch using the {@link HierarchyManager} and
     * the current display mode preference. For already-built {@link TreeNode} wrappers,
     * we simply return the children that were already computed.
     *
     * <p>For example — Yoda reveals the shape:</p>
     * <pre>
     *   input = ObjectClass("inetOrgPerson")
     *   mode  = TYPE_HIERARCHY
     *   → returns [top → person → organizationalPerson → inetOrgPerson → (subtypes)]
     * </pre>
     *
     * @param parentElement  an {@link ObjectClass}, {@link AttributeType}, or {@link TreeNode}
     * @return               the array of child {@link TreeNode} objects for this element
     */
    public Object[] getChildren( Object parentElement )
    {
        List<TreeNode> children = new ArrayList<TreeNode>();

        if ( parentElement instanceof ObjectClass )
        {
            ObjectClass oc = ( ObjectClass ) parentElement;

            children = createTypeHierarchyObjectClass( oc );
        }
        else if ( parentElement instanceof AttributeType )
        {
            AttributeType at = ( AttributeType ) parentElement;

            children = createTypeHierarchyAttributeType( at );
        }
        else if ( parentElement instanceof TreeNode )
        {
            children = ( ( TreeNode ) parentElement ).getChildren();
        }

        return children.toArray();
    }


    // ── The Whole Ship's Structure for Object Classes ─────────────────────────
    // Yoda doesn't just lift the nose cone — he lifts the whole ship, with all
    // its interconnected parts arranged correctly. For an ObjectClass we need
    // to lay out all ancestors above it and all descendants below it, respecting
    // the user's chosen mode (type hierarchy, supertype only, or subtype only).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the full type hierarchy tree for the given object class.
     * Depending on the preference setting we either show the full chain (ancestors
     * above the type, descendants below), only the supertypes, or only the subtypes.
     * Returns the root-level nodes to display in the tree.
     *
     * <p>For example — lifting the whole structure:</p>
     * <pre>
     *   mode = TYPE_HIERARCHY, oc = "inetOrgPerson"
     *   → top → person → organizationalPerson → [inetOrgPerson → subtypes]
     *   mode = SUPERTYPE,    oc = "inetOrgPerson"
     *   → top → person → organizationalPerson → [inetOrgPerson]
     *   mode = SUBTYPE,      oc = "inetOrgPerson"
     *   → [inetOrgPerson → subtypes]
     * </pre>
     *
     * @param oc  the object class to build the hierarchy around
     * @return    the list of root-level {@link TreeNode} objects for this hierarchy
     */
    private List<TreeNode> createTypeHierarchyObjectClass( ObjectClass oc )
    {
        List<TreeNode> children = new ArrayList<TreeNode>();

        HierarchyManager hierarchyManager = new HierarchyManager();

        // Creating the wrapper of the object class
        ObjectClassWrapper ocw = new ObjectClassWrapper( oc );

        int mode = Activator.getDefault().getDialogSettings().getInt( PluginConstants.PREFS_HIERARCHY_VIEW_MODE );
        if ( mode == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_TYPE )
        {
            // Creating its children's wrappers
            createChildrenHierarchy( ocw, hierarchyManager.getChildren( oc ), hierarchyManager );

            // Creating its parents' wrappers
            createParentHierarchy( hierarchyManager.getParents( oc ), children, ocw, hierarchyManager );
        }
        else if ( mode == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_SUPERTYPE )
        {
            // Creating its parents' wrappers
            createParentHierarchy( hierarchyManager.getParents( oc ), children, ocw, hierarchyManager );
        }
        else if ( mode == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_SUBTYPE )
        {
            // Creating its children's wrappers
            createChildrenHierarchy( ocw, hierarchyManager.getChildren( oc ), hierarchyManager );

            children.add( ocw );
        }

        return children;
    }


    // ── The Cockpit Connects to the Wing to the Engine ────────────────────────
    // Each section of the X-wing connects to the sections above it. createParentHierarchy
    // walks upward from a given node and builds the chain of ancestor wrappers,
    // nesting each one inside the one above it, until we reach the root (no more parents).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Recursively builds the parent chain for a given object class wrapper, working
     * upward through the inheritance hierarchy until there are no more parents.
     * Each parent becomes a new wrapper that contains the current node as a child,
     * and we keep climbing until we hit the top (a type with no parents, like {@code top}).
     *
     * <p>For example — Yoda builds the chain from the ground up:</p>
     * <pre>
     *   createParentHierarchy(parents=[organizationalPerson], ..., ocw=inetOrgPerson)
     *   → wraps inetOrgPerson inside organizationalPerson
     *   → recurses: wraps organizationalPerson inside person
     *   → recurses: wraps person inside top
     *   → top has no parents → adds top to children list
     * </pre>
     *
     * @param parents           the list of parent objects for the current node
     * @param children          the root-level list that collects the topmost ancestors
     * @param ocw               the wrapper for the current (child) object class
     * @param hierarchyManager  used to look up each parent's own parents
     */
    private void createParentHierarchy( List<Object> parents, List<TreeNode> children, ObjectClassWrapper ocw,
        HierarchyManager hierarchyManager )
    {
        if ( parents != null )
        {
            for ( Object parent : parents )
            {
                if ( parent instanceof ObjectClass )
                {
                    ObjectClass parentOC = ( ObjectClass ) parent;
                    ObjectClassWrapper duplicatedOCW = ( ObjectClassWrapper ) duplicateTreeNode( ocw );

                    ObjectClassWrapper ocw2 = new ObjectClassWrapper( parentOC );
                    duplicatedOCW.setParent( ocw2 );
                    ocw2.addChild( duplicatedOCW );

                    createParentHierarchy( hierarchyManager.getParents( parentOC ), children, ocw2, hierarchyManager );
                }
                else
                {
                    children.add( ocw );
                }
            }
        }
        else
        {
            children.add( ocw );
        }
    }


    // ── Yoda Duplicates the Ship's Blueprint ─────────────────────────────────
    // In the Force, Yoda can hold two versions of the same plan simultaneously
    // without one interfering with the other. When an object class has multiple
    // parents, we need separate copies of the subtree for each parent branch.
    // duplicateTreeNode clones a node and all its children recursively so
    // each parent branch gets its own independent copy.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given {@link TreeNode} and its entire subtree.
     * This is needed when a type has multiple parents — each parent branch needs
     * its own copy of the subtree so they don't share the same wrapper objects.
     * Currently only handles {@link ObjectClassWrapper} nodes; returns {@code null}
     * for anything else.
     *
     * <p>For example — the Force holds two ships at once:</p>
     * <pre>
     *   original = ObjectClassWrapper(inetOrgPerson, children=[MyCustomSubclass])
     *   duplicate = duplicateTreeNode(original)
     *   → new ObjectClassWrapper(inetOrgPerson), independently contains [MyCustomSubclass copy]
     * </pre>
     *
     * @param node  the {@link TreeNode} to duplicate (must be an {@link ObjectClassWrapper})
     * @return      a deep copy of the node, or {@code null} if the node is null or unsupported
     */
    public TreeNode duplicateTreeNode( TreeNode node )
    {
        if ( node != null )
        {
            if ( node instanceof ObjectClassWrapper )
            {
                ObjectClassWrapper ocNode = ( ObjectClassWrapper ) node;

                ObjectClassWrapper duplicatedOCNode = new ObjectClassWrapper( ocNode.getObjectClass(), ocNode
                    .getParent() );

                for ( TreeNode child : ocNode.getChildren() )
                {
                    TreeNode duplicatedChild = duplicateTreeNode( child );
                    if ( duplicatedChild != null )
                    {
                        duplicatedOCNode.addChild( duplicatedChild );
                    }
                }

                return duplicatedOCNode;
            }
        }

        return null;
    }


    // ── Lifting the Attribute Type Structure ─────────────────────────────────
    // Attribute types form a simpler hierarchy than object classes — each AT can
    // only have one direct superior (unlike OCs which can have multiple). So
    // the parent-chain walk for ATs is a single linear climb rather than a tree.
    // This method lifts the attribute type's hierarchy using that simpler traversal.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the full type hierarchy tree for the given attribute type.
     * Attribute types form a strict single-parent hierarchy (each AT has at most
     * one superior), so we walk a simple linked chain upward for the parent side.
     * Returns root-level nodes based on the same three display modes as the
     * object class version.
     *
     * <p>For example — lifting the AT chain:</p>
     * <pre>
     *   mode = TYPE_HIERARCHY, at = "sn" (superior: "name")
     *   → [name → sn → (subtypes of sn)]
     *   mode = SUPERTYPE, at = "sn"
     *   → [name → sn]
     *   mode = SUBTYPE,   at = "name"
     *   → [name → sn, cn, givenName, ...]
     * </pre>
     *
     * @param at  the attribute type to build the hierarchy around
     * @return    the list of root-level {@link TreeNode} objects for this hierarchy
     */
    private List<TreeNode> createTypeHierarchyAttributeType( AttributeType at )
    {
        List<TreeNode> children = new ArrayList<TreeNode>();
        HierarchyManager hierarchyManager = new HierarchyManager();
        int mode = Activator.getDefault().getDialogSettings().getInt( PluginConstants.PREFS_HIERARCHY_VIEW_MODE );

        // Creating the wrapper of the attribute type
        AttributeTypeWrapper atw = new AttributeTypeWrapper( at );

        if ( mode == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_TYPE )
        {
            // Creating the children's wrappers
            createChildrenHierarchy( atw, hierarchyManager.getChildren( at ), hierarchyManager );

            // Creating its parents' wrappers
            List<Object> parents = hierarchyManager.getParents( at );
            while ( ( parents != null ) && ( parents.size() == 1 ) )
            {
                Object parent = parents.get( 0 );
                if ( parent instanceof AttributeType )
                {
                    AttributeType parentAT = ( AttributeType ) parent;

                    AttributeTypeWrapper atw2 = new AttributeTypeWrapper( parentAT );
                    atw.setParent( atw2 );
                    atw2.addChild( atw );

                    atw = atw2;

                    parents = hierarchyManager.getParents( parentAT );
                }
                else
                {
                    break;
                }
            }

            children.add( atw );
        }
        else if ( mode == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_SUPERTYPE )
        {
            // Creating its parents' wrappers
            List<Object> parents = hierarchyManager.getParents( at );
            while ( ( parents != null ) && ( parents.size() == 1 ) )
            {
                Object parent = parents.get( 0 );
                if ( parent instanceof AttributeType )
                {
                    AttributeType parentAT = ( AttributeType ) parent;

                    AttributeTypeWrapper atw2 = new AttributeTypeWrapper( parentAT );
                    atw.setParent( atw2 );
                    atw2.addChild( atw );

                    atw = atw2;

                    parents = hierarchyManager.getParents( parentAT );
                }
                else
                {
                    break;
                }
            }

            children.add( atw );
        }
        else if ( mode == PluginConstants.PREFS_HIERARCHY_VIEW_MODE_SUBTYPE )
        {
            // Creating the children's wrappers
            createChildrenHierarchy( atw, hierarchyManager.getChildren( at ), hierarchyManager );

            children.add( atw );
        }

        return children;
    }


    // ── Each Wing Sprouts Its Sub-sections ───────────────────────────────────
    // Once the X-wing is in the air you can see each wing has its own sub-sections —
    // landing struts, weapons mounts, engine pods. createChildrenHierarchy
    // recursively builds all the descendant nodes for a given parent, attaching
    // each child to its parent wrapper and then recursing into the child's children.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Recursively builds the descendant subtree for the given parent node.
     * For each child in the list we create the right wrapper ({@link AttributeTypeWrapper}
     * or {@link ObjectClassWrapper}), attach it to the parent, then recursively build
     * that child's own subtree. The tree gets built depth-first.
     *
     * <p>For example — each wing sprouts its sub-sections:</p>
     * <pre>
     *   createChildrenHierarchy(node=top, children=[person, device, ...])
     *   → attaches person to top
     *   → recurses: attaches [organizationalPerson, residentialPerson] to person
     *   → recurses deeper: attaches [inetOrgPerson] to organizationalPerson
     * </pre>
     *
     * @param node              the parent node to attach children to
     * @param children          the raw schema objects that are direct children of this node
     * @param hierarchyManager  used to look up each child's own children for recursion
     */
    private void createChildrenHierarchy( TreeNode node, List<Object> children, HierarchyManager hierarchyManager )
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

                // Recursively creating the hierarchy for all children
                // of the given element.
                createChildrenHierarchy( childNode, hierarchyManager.getChildren( child ), hierarchyManager );
            }
        }
    }


    // ── Finding the Wing's Parent Section ────────────────────────────────────
    // You can trace any part of the X-wing back to the section it's attached to.
    // getParent does the same: given any TreeNode wrapper, it returns the wrapper
    // one level up in the tree — its parent node.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent of the given tree node, or {@code null} if it has none.
     * JFace calls this when it needs to reveal or scroll to a specific node — it
     * walks up the parent chain to find what needs to be expanded.
     *
     * @param element  the tree element whose parent we need
     * @return         the parent {@link TreeNode}, or {@code null} if the element is the root
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


    // ── Does This Section Have Sub-sections? ─────────────────────────────────
    // Not every part of the X-wing has children — the individual bolt doesn't
    // expand. hasChildren tells JFace whether to show an expand arrow next to
    // a node, before it asks for the actual children (avoiding pointless work).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given tree element has any children.
     * JFace calls this before calling {@link #getChildren(Object)} so it can decide
     * whether to show an expand arrow. We return {@code true} for any {@link TreeNode}
     * wrapper (the tree structure is already built at this point) and {@code false}
     * for anything else.
     *
     * @param element  the tree element to test
     * @return         {@code true} if this node has children in the hierarchy tree
     */
    public boolean hasChildren( Object element )
    {
        if ( element instanceof TreeNode )
        {
            return true;
        }

        // Default
        return false;
    }


    // ── The Ship Is Lowered Back into the Swamp ──────────────────────────────
    // When Yoda's demonstration is over, the Force subsides and everything settles.
    // dispose() is called when the content provider is no longer needed — here
    // there's nothing to clean up, but we implement the method to satisfy the
    // IContentProvider contract.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when this content provider is no longer needed.
     * We have no listeners or resources to release, so this is a no-op — but
     * we implement it to satisfy the {@link org.eclipse.jface.viewers.IContentProvider} contract.
     */
    public void dispose()
    {
    }


    // ── The Swamp Changes, So the Ship Must Be Re-lifted ─────────────────────
    // If the swamp shifts, Yoda would need to adjust the lift. inputChanged is
    // called by JFace when the root input to the tree viewer changes (e.g., the
    // user selects a different schema type). We don't need to do anything here
    // because getChildren() always computes fresh from the new input.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the viewer's input changes.
     * Since our {@link #getChildren(Object)} method always computes the hierarchy fresh
     * from whatever input it receives, we don't need to maintain any cached state here.
     *
     * @param viewer    the tree viewer whose input changed
     * @param oldInput  the previous input object
     * @param newInput  the new input object (an {@link AttributeType} or {@link ObjectClass})
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }
}
