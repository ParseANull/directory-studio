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
package org.apache.directory.studio.schemaeditor.view.wrappers.difference;


import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;


// ── CLASS: ObjectClassDifferenceWrapper — Yoda Inspecting the Cockpit Canopy ──
// While the X-wing hovers in mid-lift over the Dagobah swamp, Yoda turns his
// attention to the cockpit canopy — the structural shell that defines what the
// ship IS, not just one of its sub-components.
// ObjectClassDifferenceWrapper is the equivalent: it carries the "before" and
// "after" versions of a single object class definition — the container type
// that groups attribute types together and defines an entry's identity in LDAP.
// Like the cockpit canopy, the object class is the defining structure; changes
// to it ripple through everything beneath it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the difference between two versions of a single object class in the schema diff view.
 * When we compare two schema versions, each object class that was added, removed, or changed
 * gets wrapped in an instance of this class so the diff TreeViewer can render it with the
 * appropriate colour and label.
 * This is a concrete subclass of {@link AbstractDifferenceWrapper} — no new fields are added;
 * the class merely constrains the wrapper type to object class comparisons specifically.
 * Think of Yoda focused on the cockpit canopy while the full X-wing lift is managed by
 * the abstract base: this wrapper handles the "object class" slice of the diff tree.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassDifferenceWrapper extends AbstractDifferenceWrapper
{
    // ── Yoda Notes the Canopy's Before and After — State Deferred ────────────
    // Yoda holds both versions of the canopy in his Force grip: the cracked "before"
    // and the intact "after." He hasn't filed the damage report yet — the state label
    // will come once he has inspected the rest of the ship.
    // This constructor stores original and modified, deferring the WrapperState.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper for an object class difference without an explicit state.
     * Use this when the {@link WrapperState} will be assigned later via {@link #setState}.
     *
     * <p>For example — Yoda noting the canopy's before and after, classification pending:</p>
     * <pre>
     *   ObjectClassDifferenceWrapper w =
     *       new ObjectClassDifferenceWrapper( originalOC, modifiedOC, parentNode );
     *   // w.getState() == null
     * </pre>
     *
     * @param originalObject  the object class definition before the change; {@code null} for ADDED
     * @param modifiedObject  the object class definition after the change; {@code null} for REMOVED
     * @param parent          the parent {@link TreeNode} in the diff view tree
     */
    public ObjectClassDifferenceWrapper( Object originalObject, Object modifiedObject, TreeNode parent )
    {
        super( originalObject, modifiedObject, parent );
    }


    // ── Yoda Files the Canopy Report with a Clear Classification ─────────────
    // After inspecting both versions, Yoda stamps the canopy report: "ADDED — this
    // canopy did not exist in the earlier version of the fighter at all."
    // This constructor records everything — before, after, state, parent — at once.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper for an object class difference with an explicit {@link WrapperState}.
     * Use this when the diff engine has already determined the change classification —
     * everything needed for the diff view row is set in a single construction call.
     *
     * <p>For example — Yoda filing the canopy report with a clear damage classification:</p>
     * <pre>
     *   ObjectClassDifferenceWrapper w =
     *       new ObjectClassDifferenceWrapper( originalOC, modifiedOC, WrapperState.ADDED, parentNode );
     *   // w.getState() == WrapperState.ADDED — diff view renders this row in green
     * </pre>
     *
     * @param originalObject  the object class definition before the change; {@code null} for ADDED
     * @param modifiedObject  the object class definition after the change; {@code null} for REMOVED
     * @param state           classifies the kind of change — ADDED, MODFIED, REMOVED, or IDENTICAL
     * @param parent          the parent {@link TreeNode} in the diff view tree
     */
    public ObjectClassDifferenceWrapper( Object originalObject, Object modifiedObject, WrapperState state,
        TreeNode parent )
    {
        super( originalObject, modifiedObject, state, parent );
    }
}
