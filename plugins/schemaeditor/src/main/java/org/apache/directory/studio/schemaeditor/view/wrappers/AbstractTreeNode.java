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
package org.apache.directory.studio.schemaeditor.view.wrappers;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


// ── CLASS: AbstractTreeNode — Luke's Binary Sunset on Tatooine ───────────────
// Luke stands at the edge of the Lars homestead watching the twin suns set —
// seeing the whole landscape at once: horizon, sky, the world above and the
// ground below, each layer connected to the next.
// This abstract class is that panoramic vantage point: it implements every
// parent-child relationship in the tree (the full landscape) so that concrete
// subclasses only need to add the specific object they're wrapping, not re-invent
// the entire hierarchy machinery from scratch.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides the shared parent-child tree structure for every node in the Schema Editor's views.
 * Concrete subclasses ({@link AttributeTypeWrapper}, {@link SchemaWrapper}, {@link Folder}, etc.)
 * extend this class and inherit the full list-management machinery for free.
 * Think of it as Luke looking out at the binary sunset — the abstract base sees the big picture
 * (parent, children, equality, hashing) so subclasses can focus on their own payload.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractTreeNode implements TreeNode
{
    /** The children */
    protected List<TreeNode> fChildren;

    /** The parent */
    protected TreeNode fParent;


    // ── Luke Sets Out from the Lars Homestead ────────────────────────────────
    // Before Luke can see that binary sunset he needs to be standing somewhere —
    // in the world, connected to it, with a place to call "home base."
    // This constructor wires up our node's single essential link: who is above us.
    // Everything else (children, identity) is built lazily as needed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new tree node and records its parent.
     * We wire up the parent reference here at birth; the children list starts out
     * {@code null} and is created lazily the first time it is needed — saving memory
     * when a node has no children at all (which is common for leaves like attribute types).
     *
     * <p>For example — Luke stepping outside with a clear sense of where he stands:</p>
     * <pre>
     *   TreeNode parent = homestead;   // "This is where I come from."
     *   AbstractTreeNode node = new ConcreteNode( parent );
     * </pre>
     *
     * @param parent  the node that sits one level above this one in the tree; {@code null} for a root node
     */
    public AbstractTreeNode( TreeNode parent )
    {
        fParent = parent;
    }


    // ── Luke Checks Whether Anything Is Below Him on the Horizon ─────────────
    // Standing at the homestead, Luke scans the landscape: is there anything
    // moving out there beyond the moisture vaporators, or is it empty desert?
    // hasChildren() answers the same quick question without materialising the full list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this node has at least one child.
     * We check the list reference first — if it has never been created we know for sure
     * there are no children, so we skip the isEmpty() call entirely.
     *
     * <p>For example — Luke scanning the dunes before committing to a path:</p>
     * <pre>
     *   if ( node.hasChildren() ) { treeViewer.showExpandArrow( node ); }
     * </pre>
     *
     * @return  {@code true} if the children list exists and is non-empty; {@code false} otherwise
     */
    public boolean hasChildren()
    {
        if ( fChildren == null )
        {
            return false;
        }

        return !fChildren.isEmpty();
    }


    // ── Luke Looks Back at the Homestead Behind Him ───────────────────────────
    // After walking out to the ridge, Luke can still look back and see the homestead
    // — the place he came from, the level above his current vantage point.
    // getParent() hands us that reference so we can climb back up the tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent node that sits one level above us in the tree.
     * The TreeViewer and content providers use this to navigate upward — for example
     * when refreshing a subtree or collapsing a branch all the way to its root.
     *
     * <p>For example — Luke glancing back at the homestead from the ridge:</p>
     * <pre>
     *   TreeNode parent = currentNode.getParent();  // "That's where I came from."
     * </pre>
     *
     * @return  the parent TreeNode, or {@code null} if this is the root of the tree
     */
    public TreeNode getParent()
    {
        return fParent;
    }


    // ── Luke Is Told He Belongs to a Different Part of the World ─────────────
    // Imagine Obi-Wan telling Luke "You're not just a moisture farmer —
    // you belong to a larger galaxy." That re-parents Luke in his own mental model.
    // setParent() does the same: moves a node to a new place in the hierarchy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the parent reference for this node to a new parent.
     * We typically call this when a node is moved, or during tree reconstruction after
     * a schema reload — the node needs to know its new position in the hierarchy.
     *
     * <p>For example — Luke's world expanding beyond the Lars homestead:</p>
     * <pre>
     *   node.setParent( newSchemaFolder );
     *   // "You belong under this schema now, not the old one."
     * </pre>
     *
     * @param parent  the new parent node; pass {@code null} to make this node a root
     */
    public void setParent( TreeNode parent )
    {
        fParent = parent;
    }


    // ── Luke Counts the Shapes Moving on the Horizon ─────────────────────────
    // Luke squints at the landscape: moisture vaporators, the ridge, maybe a sandcrawler
    // far off. He wants the full list so he can account for everything he sees below him.
    // getChildren() returns that list, creating it lazily if it doesn't exist yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live list of child nodes below this one, creating it lazily if needed.
     * The first call initialises the list to an empty {@link ArrayList}; subsequent calls
     * return the same instance. Adding to or removing from the returned list directly
     * affects the node's children.
     *
     * <p>For example — Luke cataloguing everything visible from the homestead ridge:</p>
     * <pre>
     *   List children = node.getChildren();  // created fresh if this is the first call
     *   children.size();  // 0 until something is added
     * </pre>
     *
     * @return  the mutable list of child TreeNodes; never {@code null}
     */
    public List<TreeNode> getChildren()
    {
        if ( fChildren == null )
        {
            fChildren = new ArrayList<TreeNode>();
        }

        return fChildren;
    }


    // ── A New Shape Appears on Luke's Horizon ────────────────────────────────
    // A Jawa sandcrawler crests the dune — something new has entered Luke's world.
    // He marks it in his mental map: it belongs here, under the ridge he stands on.
    // addChild() adds exactly one new node, guarding against duplicates.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches a single child node below this one in the tree, skipping duplicates.
     * We initialise the children list on first use, then check if the node is already
     * present before adding — so calling this twice with the same node is safe.
     *
     * <p>For example — Luke spotting the Jawa sandcrawler and noting its position:</p>
     * <pre>
     *   schemaNode.addChild( attributeTypeWrapper );
     *   // If it's already there, nothing changes — no double entries.
     * </pre>
     *
     * @param node  the child TreeNode to attach; must not be {@code null}
     */
    public void addChild( TreeNode node )
    {
        if ( fChildren == null )
        {
            fChildren = new ArrayList<TreeNode>();
        }

        if ( !fChildren.contains( node ) )
        {
            fChildren.add( node );
        }
    }


    // ── A Shape Vanishes from Luke's Horizon ─────────────────────────────────
    // The sandcrawler disappears behind a dune — gone from Luke's view of the world.
    // removeChild() is how we erase a node from this parent's children list.
    // If the children list doesn't exist at all, there's nothing to remove, so we bail.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches a single child node from this parent's list.
     * Safe to call even if the children list has never been created — in that case
     * we do nothing rather than throw a NullPointerException.
     *
     * <p>For example — Luke watching the sandcrawler disappear over the horizon:</p>
     * <pre>
     *   schemaNode.removeChild( deletedAttributeWrapper );
     *   // "It's gone — no longer part of what I can see from here."
     * </pre>
     *
     * @param node  the child TreeNode to remove; no-op if not present or list is empty
     */
    public void removeChild( TreeNode node )
    {
        if ( fChildren != null )
        {
            fChildren.remove( node );
        }
    }


    // ── The Whole Landscape Comes Into View at Once ───────────────────────────
    // Just before those twin suns fully set, the entire desert landscape is
    // illuminated at once — every ridge and rock visible in a single glance.
    // addAllChildren() does the equivalent: loads a whole collection in one shot,
    // rather than adding shapes to the horizon one by one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Bulk-attaches a collection of child nodes to this parent in one call.
     * Delegates directly to {@link List#addAll(Collection)}, creating the children
     * list first if it hasn't been initialised yet. Use this when populating an
     * entire schema folder from scratch — fewer round-trips than repeated addChild().
     *
     * <p>For example — the whole Tatooine landscape appearing in one final sweep of light:</p>
     * <pre>
     *   schemaFolder.addAllChildren( allAttributeWrappers );
     *   // Whole schema populated in one pass — ready for the TreeViewer to render.
     * </pre>
     *
     * @param c  the collection of child TreeNodes to add; must not be {@code null}
     * @return   {@code true} if the children list changed as a result of the call
     */
    public boolean addAllChildren( Collection<? extends TreeNode> c )
    {
        if ( fChildren == null )
        {
            fChildren = new ArrayList<TreeNode>();
        }

        return fChildren.addAll( c );
    }


    // ── Checking Whether Two Sunsets Look the Same ────────────────────────────
    // Two moments on Tatooine: both show twin suns, same sky, same ridge.
    // Are they the same moment? Only if the vantage point (parent) matches too.
    // equals() compares nodes by their parent reference — subclasses extend this
    // by also comparing their specific payload (the schema element they wrap).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is an {@link AbstractTreeNode} with an equivalent parent.
     * We compare parents to anchor two nodes in the same position in the tree.
     * Subclasses override this and also compare their wrapped schema objects
     * to guarantee full structural equality.
     *
     * <p>For example — two Tatooine sunsets are the same if the vantage point matches:</p>
     * <pre>
     *   node1.equals( node2 );
     *   // true only if both sit under the same parent node in the tree
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if {@code obj} is an {@link AbstractTreeNode} with an equal parent
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof AbstractTreeNode )
        {
            AbstractTreeNode atn = ( AbstractTreeNode ) obj;

            if ( ( fParent != null ) && ( !fParent.equals( atn.getParent() ) ) )
            {
                return false;
            }

            return true;
        }

        // Default
        return false;
    }


    // ── Encoding the Sunset Moment as a Unique Number ─────────────────────────
    // Every memory has a unique emotional fingerprint — Luke's binary sunset is
    // unlike any other because of exactly where he stood and what he felt.
    // hashCode() gives each node a numeric fingerprint that reflects its position
    // in the tree (via its parent), so collections can bucket nodes correctly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a hash code that factors in this node's parent.
     * We use the classic 17/37 prime-multiplication formula — start with 17,
     * multiply by 37 for each significant field. Since {@link #equals} compares
     * parents, hashCode must incorporate the parent too to satisfy the equals/hashCode contract.
     *
     * <p>For example — every sunset moment gets its own fingerprint based on where Luke stood:</p>
     * <pre>
     *   int hash = node.hashCode();
     *   // Combines 17 + 37 * parent.hashCode() — unique per position in the tree.
     * </pre>
     *
     * @return  a hash code consistent with {@link #equals}
     */
    public int hashCode()
    {
        int result = 17;

        if ( fParent != null )
        {
            result = 37 * result + fParent.hashCode();
        }

        return result;
    }
}
