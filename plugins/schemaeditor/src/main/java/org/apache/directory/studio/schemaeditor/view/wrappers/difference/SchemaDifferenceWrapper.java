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


// ── CLASS: SchemaDifferenceWrapper — Yoda Lifting the Whole X-Wing at Once ───
// This is the full lift: not just a strut, not just the canopy, but the entire
// X-wing hauled out of the Dagobah swamp in one Force-driven sweep.
// Yoda holds both the submerged ship (original schema) and the raised ship
// (modified schema) simultaneously, and the effort label on the whole event is
// clear: ADDED, REMOVED, MODIFIED, or IDENTICAL — the state of the entire schema.
// SchemaDifferenceWrapper is the top-level diff node: it wraps a whole schema
// (not just one attribute or class) and sits at the top of the diff tree, with
// attribute type and object class difference wrappers nested beneath it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the difference between two versions of an entire schema in the schema diff view.
 * When comparing two schema sets, each schema that was added, removed, or changed
 * gets wrapped here. Its children are {@link AttributeTypeDifferenceWrapper} and
 * {@link ObjectClassDifferenceWrapper} nodes for the individual elements inside it.
 * This is a concrete subclass of {@link AbstractDifferenceWrapper} — no new fields are added;
 * the class constrains the wrapper to whole-schema comparisons at the top level of the diff tree.
 * Think of it as Yoda holding the complete X-wing: the biggest unit of the schema diff,
 * containing everything beneath it.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaDifferenceWrapper extends AbstractDifferenceWrapper
{
    // ── Yoda Grasps the Whole Ship — State Not Labelled Yet ──────────────────
    // Yoda reaches out and takes hold of the whole X-wing — both the sunken version
    // and the raised version — without yet labelling the scale of the transformation.
    // The state will be determined once the diff engine has inspected its contents.
    // This constructor stores original and modified schema, deferring the WrapperState.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper for a schema-level difference without an explicit state.
     * Use this when the {@link WrapperState} will be assigned later via {@link #setState},
     * or when the diff engine populates the state incrementally after inspecting child elements.
     *
     * <p>For example — Yoda grasping the full X-wing, damage classification to follow:</p>
     * <pre>
     *   SchemaDifferenceWrapper w =
     *       new SchemaDifferenceWrapper( originalSchema, modifiedSchema, parentNode );
     *   // w.getState() == null — will be set once child elements are assessed
     * </pre>
     *
     * @param originalObject  the schema as it existed before the change; {@code null} for ADDED schemas
     * @param modifiedObject  the schema as it exists after the change; {@code null} for REMOVED schemas
     * @param parent          the parent {@link TreeNode} in the diff view tree (typically the diff root)
     */
    public SchemaDifferenceWrapper( Object originalObject, Object modifiedObject, TreeNode parent )
    {
        super( originalObject, modifiedObject, parent );
    }


    // ── Yoda Lifts the Ship and Files the Full Mission Report ─────────────────
    // The lift is complete. Yoda turns to the Rebel engineers and says:
    // "IDENTICAL — this schema has not changed at all between the two versions."
    // Or: "REMOVED — the old ship is gone, replaced entirely."
    // This constructor records the whole schema diff in one fully classified call.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper for a schema-level difference with an explicit {@link WrapperState}.
     * Use this when the diff engine has fully determined the classification and wants to
     * create the schema-level node with all information in a single construction call.
     *
     * <p>For example — Yoda filing the complete mission report on the X-wing restoration:</p>
     * <pre>
     *   SchemaDifferenceWrapper w =
     *       new SchemaDifferenceWrapper( originalSchema, modifiedSchema, WrapperState.MODFIED, parentNode );
     *   // w.getState() == WrapperState.MODFIED — this schema changed; expand it to see what
     * </pre>
     *
     * @param originalObject  the schema before the change; {@code null} for ADDED schemas
     * @param modifiedObject  the schema after the change; {@code null} for REMOVED schemas
     * @param state           classifies the scope of the change — ADDED, MODFIED, REMOVED, or IDENTICAL
     * @param parent          the parent {@link TreeNode} in the diff view tree
     */
    public SchemaDifferenceWrapper( Object originalObject, Object modifiedObject, WrapperState state, TreeNode parent )
    {
        super( originalObject, modifiedObject, state, parent );
    }
}
