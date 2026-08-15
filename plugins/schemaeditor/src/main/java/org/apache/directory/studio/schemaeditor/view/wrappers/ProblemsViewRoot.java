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


// ── CLASS: ProblemsViewRoot — The Rebel Briefing Room at Yavin ───────────────
// In the great briefing room at Yavin IV, every pilot, every analyst, every
// senior officer gathers in one place. All intelligence — every detected threat,
// every flagged concern about the Death Star — is funnelled into this single room.
// The room itself is the root: it has no parent command above it (the Alliance
// has gathered everything here), and everything beneath it is a problem to address.
// ProblemsViewRoot is exactly that briefing room: the invisible root node of the
// Problems View tree. The TreeViewer never shows it directly, but every error
// and warning folder hangs off it, making this the logical anchor of the whole view.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Serves as the invisible root node of the Schema Editor's Problems View tree.
 * JFace TreeViewers need a root object even though they never render it directly —
 * the content provider uses it as the starting point to fetch top-level children
 * (typically an Errors folder and a Warnings folder).
 * There is exactly one instance of this class per Problems View; equality is
 * identity-based ({@code ==}) because two distinct roots would mean two distinct views.
 * Think of it as the Yavin briefing room: an invisible container that all detected
 * problems flow into, never rendered itself but essential for the tree to function.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProblemsViewRoot extends AbstractTreeNode
{
    // ── The Briefing Room Opens Its Doors — Ready for Problems ───────────────
    // Mon Mothma opens the doors to the Yavin briefing room: the space is ready,
    // no parent command above it (this is the top of the Rebel hierarchy), and
    // nothing inside yet — it fills up as intelligence comes in.
    // Our constructor just calls super(null): root nodes have no parent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the single root node for the Problems View tree.
     * We pass {@code null} as the parent because this node IS the root — there is
     * nothing above it in the hierarchy. The TreeViewer's content provider will
     * ask this node for its children to populate the top level of the Problems View.
     *
     * <p>For example — Mon Mothma unlocking the Yavin briefing room before the mission:</p>
     * <pre>
     *   ProblemsViewRoot root = new ProblemsViewRoot();
     *   // root.getParent() == null — top of the tree, nothing above
     * </pre>
     */
    public ProblemsViewRoot()
    {
        super( null );
    }


    // ── Is This the Same Briefing Room? ──────────────────────────────────────
    // There is only one briefing room on Yavin IV — if someone asks "is this
    // the briefing room?", the only correct answer is physical identity: is this
    // literally the same room (the same object in memory)?
    // equals() uses reference equality (==) because there should only be one root.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} only if {@code obj} is literally the same object as this root.
     * We use reference equality ({@code ==}) because there is only ever one Problems View root
     * per view instance — two different root objects mean two different view contexts,
     * which should never be considered equal to each other.
     *
     * <p>For example — confirming we're talking about the one real Yavin briefing room:</p>
     * <pre>
     *   root.equals( root );   // true — same object
     *   root.equals( other );  // false — even if other is also a ProblemsViewRoot
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if and only if {@code obj == this}
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ProblemsViewRoot )
        {
            return this == obj;
        }

        return false;
    }
}
