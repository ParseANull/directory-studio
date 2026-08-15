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


// ── CLASS: SchemaViewRoot — Luke's Binary Sunset on Tatooine ─────────────────
// Luke stands alone at the edge of the moisture farm, watching both suns set
// over the desert horizon. He sees everything: the whole expanse of Tatooine
// laid out before him, not one detail or one corner of it, but the full sweep
// of the world — schemas, attribute types, object classes, all of it in one
// serene, unobstructed view.
// SchemaViewRoot is that vantage point: it is the invisible root of the Schema View
// tree, the node that sits above all schemas and gives the TreeViewer a single
// anchor from which to render the entire schema landscape.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Serves as the invisible root node of the Schema Editor's Schema View tree.
 * JFace TreeViewers need a root object that the content provider uses as a
 * starting point to fetch top-level children — in this case, all the
 * {@link SchemaWrapper} nodes that represent the loaded schemas.
 * This node is never rendered directly; the user sees the schemas beneath it.
 * Like Luke's binary sunset, it is the anchor perspective from which the whole
 * schema world is visible, even if no one looks at the vantage point itself.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaViewRoot extends AbstractTreeNode
{
    // ── Luke Steps Outside — The Horizon Has No Parent Above It ──────────────
    // Luke walks out of the homestead and looks across the desert — there is no
    // higher ground above this moment, no "parent" vantage point to zoom out to.
    // This is the top. We call super(null) because a root node has no parent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the single root node for the Schema View tree.
     * We pass {@code null} as the parent because this node sits at the very top —
     * nothing in the tree is above it. All {@link SchemaWrapper} instances are added
     * as children, and the TreeViewer renders them as the top level of the Schema View.
     *
     * <p>For example — Luke stepping out to the ridge, no higher ground in sight:</p>
     * <pre>
     *   SchemaViewRoot root = new SchemaViewRoot();
     *   // root.getParent() == null — there's nothing above the horizon
     * </pre>
     */
    public SchemaViewRoot()
    {
        super( null );
    }


    // ── Is This the Same Sunset Moment? ──────────────────────────────────────
    // There is only one binary sunset — Luke's specific moment at the Lars homestead.
    // If someone asks "is this that same moment?", the only honest answer is
    // identity: is this literally the same instance in memory?
    // equals() uses reference equality (==) — one root per Schema View, no duplicates.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} only if {@code obj} is literally the same object as this root.
     * We use reference equality ({@code ==}) because there is only one Schema View root
     * per view instance — two different root objects represent two different view contexts
     * and must never be considered equivalent to each other.
     *
     * <p>For example — confirming it's the same unique sunset moment at the Lars homestead:</p>
     * <pre>
     *   root.equals( root );   // true — same object in memory
     *   root.equals( other );  // false — even if other is also a SchemaViewRoot
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if and only if {@code obj == this}
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof SchemaViewRoot )
        {
            return this == obj;
        }

        return false;
    }
}
