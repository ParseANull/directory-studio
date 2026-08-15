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


import java.util.Collection;
import java.util.List;


// ── CLASS: TreeNode — Rebel Briefing Room Hologram of Yavin ──────────────────
// In the Rebel briefing room on Yavin, Mon Mothma projects a glowing 3D hologram
// of the Death Star — a branching, hierarchical model that every pilot must navigate.
// Every section of that model knows its parent, its sub-sections, and how to add or
// remove pieces on the fly. This interface is that hologram blueprint: the universal
// contract that every node in our schema tree must honour so the TreeViewer can
// walk the entire structure without caring what's actually inside.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Defines the contract for any node that participates in the Schema Editor's tree views.
 * Everything rendered in a JFace TreeViewer — schemas, folders, attribute types,
 * object classes, errors, warnings — must implement this interface.
 * Think of it as the holographic blueprint on the Rebel briefing table: every node
 * knows its parent, its children, and how to modify that relationship so the viewer
 * can expand, collapse, and refresh the tree on demand.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface TreeNode
{
    // ── Rebels Studying the Hologram's Branches ──────────────────────────────
    // In the briefing room, pilots lean over the hologram tracing each branch
    // of the Death Star's structure, following paths that lead deeper into
    // the battle station's interior.
    // Each sub-structure they follow is a "child" of the section above it.
    // We hand the caller the same list: a direct view of this node's children.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of child nodes hanging below this one in the tree.
     * The JFace TreeViewer calls this every time it needs to expand a row,
     * so we give it a live List — changes to the list are reflected immediately.
     *
     * <p>For example — Mon Mothma tracing branches in the Yavin hologram:</p>
     * <pre>
     *   List children = hologramSection.getChildren();
     *   // "There — the trench, and the exhaust port at its end."
     * </pre>
     *
     * @return  the mutable list of child TreeNodes; implementations should never return {@code null}
     */
    List<TreeNode> getChildren();


    // ── Checking Whether the Hologram Has Sub-Branches ───────────────────────
    // Before a pilot commits to flying down a particular trench on the hologram,
    // they check: does this section branch further, or is it a dead end?
    // We save the TreeViewer the cost of calling getChildren() just to check length.
    // "Gold Leader, does that section open up further?" — "Negative. Dead end."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this node has at least one child.
     * The TreeViewer uses this to decide whether to draw the expand-arrow next to a row.
     * No expand-arrow means no unnecessary click, sparing us a pointless getChildren() call.
     *
     * <p>For example — Gold Leader checking the hologram before the final run:</p>
     * <pre>
     *   if ( trench.hasChildren() ) {
     *     goldLeader.flyIntoTrench();  // expand and explore
     *   }
     * </pre>
     *
     * @return  {@code true} if there is at least one child node; {@code false} if this is a leaf
     */
    boolean hasChildren();


    // ── Tracing Back Up the Hologram to the Command Level ────────────────────
    // After zooming deep into one section of the Death Star hologram, a pilot
    // needs to trace back up to the level above — who "owns" this section?
    // getParent() is how we climb one level back up the tree hierarchy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the node one level above this one in the tree, or {@code null} if this is the root.
     * The TreeViewer needs this to navigate upward — for example when collapsing a branch
     * or deciding which ancestor to refresh after a child changes.
     *
     * <p>For example — a pilot following the trench back out to the surface:</p>
     * <pre>
     *   TreeNode surface = trench.getParent();
     *   // "Back up — where does this section connect to?"
     * </pre>
     *
     * @return  the parent TreeNode, or {@code null} if this node is the root of the tree
     */
    TreeNode getParent();


    // ── Re-assigning a Section in the Hologram ───────────────────────────────
    // Sometimes the Rebels reorder the hologram projection — a sub-section gets
    // re-parented under a different part of the plan as tactical priorities shift.
    // setParent() is how we tell a node "you now belong to a different branch."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the parent reference for this node.
     * We call this when moving a node to a different place in the tree, or when
     * wiring a freshly created node into the existing hierarchy after construction.
     *
     * <p>For example — the briefing officer re-projecting a section under a new command:</p>
     * <pre>
     *   trenchSection.setParent( northApproach );
     *   // "This section now falls under the north approach vector."
     * </pre>
     *
     * @param parent  the new parent node; pass {@code null} to promote this node to a root
     */
    void setParent( TreeNode parent );


    // ── Adding a New Section to the Hologram ─────────────────────────────────
    // Fresh intelligence arrives: a new turbolaser emplacement has been spotted.
    // The hologram operator snaps it onto the right branch of the projection.
    // addChild() does the same — attaches one new node beneath this parent.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches a single child node below this one.
     * Called when a new schema element (attribute type, object class, folder) is
     * added to the tree — we attach it here and the next viewer refresh picks it up.
     *
     * <p>For example — the hologram operator marking a newly discovered section:</p>
     * <pre>
     *   deathStar.addChild( turbolaserEmplacement );
     *   // "Mark that on the projection — it just showed up on long-range scans."
     * </pre>
     *
     * @param node  the child TreeNode to attach; should not be {@code null}
     */
    void addChild( TreeNode node );


    // ── Erasing a Section from the Hologram ──────────────────────────────────
    // When a section of the Death Star is destroyed or confirmed a decoy,
    // the hologram operator wipes it from the projection.
    // removeChild() prunes exactly one node from this parent's children.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches a single child node from this parent.
     * Called when a schema element is deleted from the project — we pull it out of
     * the children list and the viewer stops rendering it on the next refresh.
     *
     * <p>For example — Mon Mothma ordering a destroyed section cleared from the briefing:</p>
     * <pre>
     *   deathStar.removeChild( destroyedWingSection );
     *   // "That section's gone — clear it from the projection."
     * </pre>
     *
     * @param node  the child TreeNode to remove; no-op if it is not actually a child here
     */
    void removeChild( TreeNode node );


    // ── Loading the Full Intelligence Package Into the Hologram ──────────────
    // Before the briefing starts, the Rebels dump the entire intelligence package
    // into the hologram projector all at once — no point adding nodes one by one
    // when we have a whole wing's worth of data ready to go.
    // addAllChildren() is that bulk load, like populating an entire schema folder.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Bulk-attaches a collection of child nodes to this parent in one call.
     * More efficient than calling addChild() in a loop when we're populating an entire
     * folder or schema for the first time — fewer list operations, one pass through.
     *
     * <p>For example — loading the full Death Star intelligence package before the briefing:</p>
     * <pre>
     *   deathStar.addAllChildren( allDiscoveredSections );
     *   // "Load everything we have into the hologram — briefing starts in five minutes."
     * </pre>
     *
     * @param c  the collection of child TreeNodes to add; must not be {@code null}
     * @return   {@code true} if the children list changed as a result (at least one was actually added)
     * @see java.util.List#addAll(Collection)
     */
    boolean addAllChildren( Collection<? extends TreeNode> c );
}
