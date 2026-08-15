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
package org.apache.directory.studio.schemaeditor.model.hierarchy;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


// ── CLASS: HierarchyWrapper — C-3PO Translating for R2-D2 ────────────────────
// C-3PO doesn't change what R2-D2 is saying — he wraps it, adds context, points
// to R2 when talking ("he says..."), and makes R2's meaning legible to humans.
// That's exactly what HierarchyWrapper does: it takes any schema object (attribute
// type, object class, or the RootObject) and wraps it with the parent/child
// navigation data the tree viewer needs, without changing the object itself.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A thin wrapper around any schema object that adds parent and children
 * references for use by the hierarchy tree viewer.
 * Used by the hierarchy view's content provider to navigate the type tree
 * without modifying the underlying schema objects.
 * Think of this class as C-3PO: he doesn't change what R2 says — he just
 * provides the surrounding context that makes it useful to whoever's listening.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HierarchyWrapper
{
    /** The wrapped object */
    private Object wrappedObject;

    /** The parent */
    private Object parent;

    /** The children */
    private List<HierarchyWrapper> children = new ArrayList<HierarchyWrapper>();


    // ── C-3PO Takes on a New Assignment with No Handler ──────────────────────
    // C-3PO is introduced to the rebel fleet and assigned to translate for a new
    // droid he's never met, with no existing handler to pass him to.
    // We wrap the object and leave parent null, meaning this node sits at the
    // top of its local sub-tree or hasn't been linked yet.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wraps the given object without a parent — use this for root-level nodes.
     * The children list starts empty; add children separately.
     *
     * @param obj  the schema object to wrap — any type is accepted
     */
    public HierarchyWrapper( Object obj )
    {
        wrappedObject = obj;
        parent = null;
    }


    // ── C-3PO Is Introduced to His New Handler ───────────────────────────────
    // C-3PO arrives alongside Luke and is immediately placed in Luke's care —
    // he knows from the start who he belongs to and who's in charge.
    // We wrap the object and record the parent at construction time when both
    // are already known.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wraps the given object and records its parent.
     * Use this when the parent is known at construction time so the tree
     * navigation works upward immediately.
     *
     * @param obj     the schema object to wrap
     * @param parent  the parent wrapper or raw object this node sits under
     */
    public HierarchyWrapper( Object obj, Object parent )
    {
        wrappedObject = obj;
        this.parent = parent;
    }


    // ── C-3PO Adds Another Droid to His Translation Team ─────────────────────
    // C-3PO can't do everything himself; he recruits one more droid to handle
    // a specific dialect and adds them to his crew.
    // We add a single child wrapper to our children list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a single child wrapper to this node's children list.
     * Returns true as per the general contract of {@link java.util.Collection#add}.
     *
     * @param child  the child HierarchyWrapper to attach — must not be null
     * @return       true if the list changed as a result of the call
     */
    public boolean addChild( HierarchyWrapper child )
    {
        return children.add( child );
    }


    // ── C-3PO Absorbs an Entire Translation Unit Into His Team ───────────────
    // A whole battalion of protocol droids transfers to C-3PO's command at once;
    // he logs all of them into his crew manifest in one sweep.
    // We bulk-add a collection of child wrappers to the children list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends all wrappers in the given collection to our children list.
     * Useful when building the tree top-down and we have a batch of children
     * ready at the same time.
     *
     * @param children  the collection of child wrappers to add — must not be null
     * @return          true if the list changed as a result of the call
     */
    public boolean addChildren( Collection<? extends HierarchyWrapper> children )
    {
        return this.children.addAll( children );
    }


    // ── C-3PO Checks Whether a Droid is the Same as His Principal ────────────
    // Someone asks C-3PO whether this droid in front of them is the same droid
    // he's translating for — he checks the serial number rather than the shell.
    // We delegate equality checking to the wrapped object, not the wrapper itself.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tests whether the given object equals the wrapped schema object.
     * The tree viewer uses this to find a wrapper given an unwrapped selection.
     *
     * @param obj  the object to compare with our wrapped object
     * @return     true if obj.equals(wrappedObject), false otherwise or if obj is null
     */
    public boolean equalsWrappedObject( Object obj )
    {
        if ( obj != null )
        {
            return obj.equals( wrappedObject );
        }

        return false;
    }


    // ── C-3PO Lists His Translation Crew ─────────────────────────────────────
    // Someone asks C-3PO who he has on his team; he reads off the list of droids
    // currently assigned to him.
    // Returns the live children list — the caller can read but should not mutate.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of direct child wrappers for this node.
     * The tree content provider calls this to find which nodes to display
     * below this one.
     *
     * @return  the list of child HierarchyWrappers — may be empty, never null
     */
    public List<HierarchyWrapper> getChildren()
    {
        return children;
    }


    // ── C-3PO Points to His Commanding Officer ───────────────────────────────
    // "And who do you report to, Threepio?" — "Why, Master Luke, of course!"
    // We return the parent reference so the tree can navigate upward.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent of this wrapped node.
     * The parent may be another HierarchyWrapper or a raw schema object,
     * depending on how the tree was built.
     *
     * @return  the parent, or null if this is a root-level node
     */
    public Object getParent()
    {
        return parent;
    }


    // ── C-3PO Reveals Who He's Translating For ───────────────────────────────
    // Someone ignores C-3PO entirely and asks who the REAL droid is behind him;
    // he graciously steps aside and introduces R2-D2.
    // We peel off the wrapper and hand back the underlying schema object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying schema object this wrapper is holding.
     * Callers that need to cast to AttributeType or ObjectClass should use this.
     *
     * @return  the wrapped object — never null after construction
     */
    public Object getWrappedObject()
    {
        return wrappedObject;
    }


    // ── C-3PO Lets a Droid Leave the Team ────────────────────────────────────
    // A protocol droid in C-3PO's unit gets reassigned elsewhere; C-3PO
    // removes them from his roster — one less voice in the chorus.
    // We remove the specified child wrapper from the children list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a single child wrapper from this node's children list.
     *
     * @param child  the child HierarchyWrapper to remove
     * @return       true if the list contained and removed the element
     */
    public boolean removeChild( HierarchyWrapper child )
    {
        return children.remove( child );
    }


    // ── C-3PO Disbands an Entire Translation Unit ────────────────────────────
    // An entire squad of protocol droids is recalled to Coruscant; C-3PO
    // removes all of them from his roster in one batch.
    // Bulk-removes a collection of child wrappers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes all wrappers in the given collection from our children list.
     *
     * @param children  the collection of child wrappers to remove
     * @return          true if the list changed as a result of the call
     */
    public boolean removeChildren( Collection<? extends HierarchyWrapper> children )
    {
        return this.children.removeAll( children );
    }


    // ── C-3PO Swaps Out His Entire Translation Crew ──────────────────────────
    // The mission changes; C-3PO replaces his whole team of droids with a new
    // set better suited for the current operation.
    // Replaces the entire children list outright — use when rebuilding a subtree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire children list with the given list.
     * Use this when you're rebuilding a subtree from scratch rather than
     * adding or removing individual children.
     *
     * @param children  the new list of child wrappers — must not be null
     */
    public void setChildren( List<HierarchyWrapper> children )
    {
        this.children = children;
    }


    // ── C-3PO Gets a New Commanding Officer ──────────────────────────────────
    // C-3PO is transferred from Luke's care to General Leia's command;
    // he updates his internal record of who he reports to.
    // Sets the parent link on this wrapper.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the parent of this node.
     * Call this when the node is re-positioned within the tree.
     *
     * @param parent  the new parent object — may be another wrapper or a raw schema object
     */
    public void setParent( Object parent )
    {
        this.parent = parent;
    }


    // ── C-3PO Starts Translating for a Different Droid ───────────────────────
    // C-3PO's principal droid is swapped out mid-mission; he now speaks on
    // behalf of a completely different unit while his role stays the same.
    // Replaces the wrapped object without touching the parent/children wiring.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the wrapped schema object without changing the parent or children.
     * Useful when the same position in the tree needs to show a different object.
     *
     * @param wrappedObject  the new object to wrap — must not be null
     */
    public void setWrappedObject( Object wrappedObject )
    {
        this.wrappedObject = wrappedObject;
    }


    // ── C-3PO Gives His Diplomatic Summary ───────────────────────────────────
    // "I am C-3PO, wrapping {R2-D2}, currently supervising {BB-8, R4-P17}."
    // A quick diagnostic dump of what this wrapper holds and what's below it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a debug-friendly string showing the wrapped object and children list.
     *
     * @return  a string in the form "{|wrappedObject|children}"
     */
    public String toString()
    {
        return "{|" + wrappedObject + "|" + children + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
