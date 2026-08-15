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


import org.apache.directory.studio.schemaeditor.view.wrappers.AbstractTreeNode;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;


// ── CLASS: AbstractDifferenceWrapper — Yoda Lifting Luke's X-Wing from the Swamp
// On Dagobah, Luke's X-wing sits half-submerged in the murky swamp — the "before"
// state. Yoda reaches out with the Force and, in one smooth motion, lifts it clear
// and sets it on dry ground — the "after" state. The entire transformation is held
// in a single act: what it was, what it became, and the state of the change itself.
// AbstractDifferenceWrapper captures exactly that: it holds the original object
// (the sunken fighter), the modified object (the raised fighter), and a {@link WrapperState}
// that labels what kind of change occurred — IDENTICAL, ADDED, MODIFIED, or REMOVED.
// Concrete subclasses specialise this for schemas, attribute types, and object classes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides the base structure for tree nodes that represent a difference between
 * an original schema element and its modified counterpart.
 * The Schema Editor's diff view compares two schema versions and wraps each
 * element in a subclass of this class — carrying both the before and after objects
 * plus a {@link WrapperState} label so the UI knows how to colour-code the row.
 * Think of it as Yoda's Force-lift: the original object (in the swamp), the modified
 * object (on dry ground), and the state that says how they got from one to the other.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractDifferenceWrapper extends AbstractTreeNode
{
    /** The original object */
    private Object originalObject;

    /** The modified object */
    private Object modifiedObject;

    /** The state */
    private WrapperState state;


    // ── Yoda Observes the X-Wing Before and After — State Unknown Yet ─────────
    // Yoda watches the fighter: here is how it looked in the swamp (original),
    // here is how it looks on the ground (modified). The "state" of the transformation
    // — how much Force was needed, what changed — has not been labelled yet.
    // This constructor holds original and modified but leaves state null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a difference wrapper holding the original and modified objects, with no explicit state.
     * Use this constructor when the {@link WrapperState} will be set later via {@link #setState},
     * or when the caller wants to defer determining the kind of change.
     *
     * <p>For example — Yoda seeing the before and after of the X-wing, change type TBD:</p>
     * <pre>
     *   AbstractDifferenceWrapper w = new ConcreteWrapper( originalAT, modifiedAT, parentNode );
     *   // w.getState() == null until setState() is called
     * </pre>
     *
     * @param originalObject  the schema element as it existed before the change; may be {@code null} for ADDED elements
     * @param modifiedObject  the schema element as it exists after the change; may be {@code null} for REMOVED elements
     * @param parent          the parent {@link TreeNode} in the diff view tree
     */
    public AbstractDifferenceWrapper( Object originalObject, Object modifiedObject, TreeNode parent )
    {
        super( parent );
        this.originalObject = originalObject;
        this.modifiedObject = modifiedObject;
    }


    // ── Yoda Names the Transformation — This Is What Changed ─────────────────
    // After lifting the X-wing, Yoda turns to Luke and says: "Size matters not —
    // but the difference between effort and surrender, that I must label for you."
    // He stamps the transformation with a clear state: it was raised, it was changed.
    // This constructor records original, modified, AND the WrapperState all at once.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a difference wrapper holding the original object, the modified object,
     * and an explicit {@link WrapperState} that classifies the change.
     * This is the fully specified constructor — use it when the diff engine has already
     * determined whether the element was added, removed, modified, or unchanged.
     *
     * <p>For example — Yoda naming the transformation clearly after lifting the X-wing:</p>
     * <pre>
     *   AbstractDifferenceWrapper w = new ConcreteWrapper(
     *       originalAT, modifiedAT, WrapperState.MODFIED, parentNode );
     *   // w.getState() == WrapperState.MODFIED
     * </pre>
     *
     * @param originalObject  the schema element before the change; {@code null} for ADDED
     * @param modifiedObject  the schema element after the change; {@code null} for REMOVED
     * @param state           classifies what kind of change this wrapper represents
     * @param parent          the parent {@link TreeNode} in the diff view tree
     */
    public AbstractDifferenceWrapper( Object originalObject, Object modifiedObject, WrapperState state, TreeNode parent )
    {
        super( parent );
        this.originalObject = originalObject;
        this.modifiedObject = modifiedObject;
        this.state = state;
    }


    // ── Reaching Into the Swamp — What Was There Before? ─────────────────────
    // Yoda recalls exactly what the X-wing looked like half-submerged in the mud —
    // the original configuration, the "before" snapshot.
    // getOriginalObject() returns that snapshot so the diff UI can display the old value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema element as it existed before the change.
     * For elements with {@link WrapperState#ADDED}, this will be {@code null} (there was no original).
     * The diff view's label provider uses this to show the "was:" side of a comparison row.
     *
     * <p>For example — Yoda's memory of the X-wing submerged in Dagobah's swamp:</p>
     * <pre>
     *   Object original = wrapper.getOriginalObject();
     *   // Cast to AttributeType or Schema etc. to read the old definition
     * </pre>
     *
     * @return  the original (pre-change) schema element; {@code null} for ADDED elements
     */
    public Object getOriginalObject()
    {
        return originalObject;
    }


    // ── Re-Stamping the "Before" Snapshot ─────────────────────────────────────
    // Sometimes the diff engine recalculates what the original was and needs to
    // update the snapshot — like correcting a memory of what the X-wing looked like.
    // setOriginalObject() lets the diff engine update the reference after construction.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the original (pre-change) object reference.
     * Used when the diff engine needs to reassign the "before" snapshot after the wrapper
     * has already been created — for example during an incremental diff update.
     *
     * <p>For example — Yoda correcting his memory of the X-wing's pre-lift position:</p>
     * <pre>
     *   wrapper.setOriginalObject( correctedOriginalAT );
     * </pre>
     *
     * @param originalObject  the corrected original schema element; may be {@code null}
     */
    public void setOriginalObject( Object originalObject )
    {
        this.originalObject = originalObject;
    }


    // ── Admiring the X-Wing on Dry Ground — The New State ────────────────────
    // After the lift, Luke stares at his ship sitting cleanly on the bank.
    // The modified object is how things look NOW — the "after" snapshot.
    // getModifiedObject() returns that snapshot so the diff UI can show the new value.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema element as it exists after the change.
     * For elements with {@link WrapperState#REMOVED}, this will be {@code null} (nothing remains).
     * The diff view's label provider uses this to show the "now:" side of a comparison row.
     *
     * <p>For example — Luke seeing his X-wing raised and placed on dry Dagobah ground:</p>
     * <pre>
     *   Object modified = wrapper.getModifiedObject();
     *   // Cast to AttributeType or Schema etc. to read the new definition
     * </pre>
     *
     * @return  the modified (post-change) schema element; {@code null} for REMOVED elements
     */
    public Object getModifiedObject()
    {
        return modifiedObject;
    }


    // ── Yoda Updates the "After" View ─────────────────────────────────────────
    // Occasionally the diff engine finds a newer "after" version and needs to replace
    // the one it stored — like discovering the X-wing was moved a second time.
    // setModifiedObject() lets the diff engine update the reference after construction.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the modified (post-change) object reference.
     * Used when the diff engine needs to update the "after" snapshot after the wrapper
     * has already been created.
     *
     * <p>For example — discovering the X-wing was repositioned a second time after the lift:</p>
     * <pre>
     *   wrapper.setModifiedObject( newerModifiedAT );
     * </pre>
     *
     * @param modifiedObject  the updated post-change schema element; may be {@code null}
     */
    public void setModifiedObject( Object modifiedObject )
    {
        this.modifiedObject = modifiedObject;
    }


    // ── What Kind of Change Was This? ─────────────────────────────────────────
    // Luke asks Yoda: "Was the X-wing ADDED here, or was it CHANGED from what was
    // there before?" Yoda answers with a single label — the WrapperState.
    // getState() returns that label so the UI knows how to colour-code the row.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link WrapperState} that classifies what happened to this schema element.
     * The diff view's label and decorator providers switch on this value to decide
     * the row's colour (green for ADDED, amber for MODFIED, red for REMOVED, plain for IDENTICAL).
     *
     * <p>For example — Yoda labelling the transformation as the X-wing settles on dry ground:</p>
     * <pre>
     *   WrapperState state = wrapper.getState();
     *   // WrapperState.MODFIED — "it changed, young one"
     * </pre>
     *
     * @return  the change state; may be {@code null} if constructed with the two-arg constructor
     */
    public WrapperState getState()
    {
        return state;
    }


    // ── Relabelling the Transformation After the Fact ─────────────────────────
    // Sometimes the diff engine re-evaluates a wrapper after initial creation
    // and decides the original state label was wrong — maybe it is actually IDENTICAL,
    // not MODFIED, once the comparison is refined.
    // setState() lets the engine correct the label without rebuilding the whole wrapper.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the {@link WrapperState} label for this wrapper.
     * Called by the diff engine when it refines its comparison and needs to
     * reclassify a wrapper that was initially given a provisional state.
     *
     * <p>For example — Yoda reconsidering and relabelling the transformation:</p>
     * <pre>
     *   wrapper.setState( WrapperState.IDENTICAL );
     *   // "On reflection, nothing actually changed here."
     * </pre>
     *
     * @param state  the new {@link WrapperState} to assign to this wrapper
     */
    public void setState( WrapperState state )
    {
        this.state = state;
    }
}
