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


// ── CLASS: AttributeTypeDifferenceWrapper — Yoda Lifting the Landing Strut ───
// After lifting Luke's whole X-wing from the Dagobah swamp, Yoda focuses on a
// single component: the bent landing strut that touched down wrong during the
// crash — the one part that specifically changed between the "before" and "after"
// states of the fighter.
// AttributeTypeDifferenceWrapper is that specific part: it carries the "before"
// and "after" versions of a single attribute type definition, with a {@link WrapperState}
// saying whether this particular element was added, removed, or modified between
// two schema versions being compared in the diff view.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the difference between two versions of a single attribute type in the schema diff view.
 * When we compare two schemas, each attribute type that changed (or was added or removed)
 * gets wrapped in an instance of this class so the diff TreeViewer can render it with
 * the appropriate label and colour.
 * This is a concrete, leaf-level subclass of {@link AbstractDifferenceWrapper} — it adds
 * no new fields, only constrains the wrapper to attribute type objects specifically.
 * Think of it as Yoda focusing on one specific component of the X-wing: the overall
 * lift (AbstractDifferenceWrapper) handles the full structure, while this class zeroes in
 * on the attribute type alone.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeDifferenceWrapper extends AbstractDifferenceWrapper
{
    // ── Yoda Identifies the Bent Strut — State Not Yet Determined ────────────
    // Yoda holds both versions of the strut: here is how it was before the crash
    // (originalObject), here is how it looks now (modifiedObject).
    // He hasn't labelled the damage yet — that comes later.
    // This constructor defers the WrapperState, leaving it null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper for an attribute type difference without an explicit state.
     * Use this when the diff engine will call {@link #setState} separately to assign
     * the {@link WrapperState} after the wrapper has been inserted into the tree.
     *
     * <p>For example — Yoda noting the two versions of the landing strut, state TBD:</p>
     * <pre>
     *   AttributeTypeDifferenceWrapper w =
     *       new AttributeTypeDifferenceWrapper( originalAT, modifiedAT, parentNode );
     *   // w.getState() == null — will be set by the diff engine
     * </pre>
     *
     * @param originalObject  the attribute type definition before the change; {@code null} for ADDED
     * @param modifiedObject  the attribute type definition after the change; {@code null} for REMOVED
     * @param parent          the parent {@link TreeNode} in the diff view tree
     */
    public AttributeTypeDifferenceWrapper( Object originalObject, Object modifiedObject, TreeNode parent )
    {
        super( originalObject, modifiedObject, parent );
    }


    // ── Yoda Labels the Damage Before Handing the Report to Luke ─────────────
    // After inspecting both versions of the strut, Yoda stamps the repair report
    // with a clear classification: "MODFIED — bent, not broken, can be straightened."
    // This constructor records all four things — before, after, state, and parent — at once.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper for an attribute type difference with an explicit {@link WrapperState}.
     * Use this when the diff engine has already determined the change classification and
     * wants to record everything in a single construction call.
     *
     * <p>For example — Yoda labelling the strut damage in the repair report before filing it:</p>
     * <pre>
     *   AttributeTypeDifferenceWrapper w =
     *       new AttributeTypeDifferenceWrapper( originalAT, modifiedAT, WrapperState.MODFIED, parentNode );
     *   // w.getState() == WrapperState.MODFIED — diff view renders this row in amber
     * </pre>
     *
     * @param originalObject  the attribute type definition before the change; {@code null} for ADDED
     * @param modifiedObject  the attribute type definition after the change; {@code null} for REMOVED
     * @param state           classifies the kind of change — ADDED, MODFIED, REMOVED, or IDENTICAL
     * @param parent          the parent {@link TreeNode} in the diff view tree
     */
    public AttributeTypeDifferenceWrapper( Object originalObject, Object modifiedObject, WrapperState state,
        TreeNode parent )
    {
        super( originalObject, modifiedObject, state, parent );
    }
}
