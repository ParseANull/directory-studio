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

package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.view.editors.AbstractSchemaObjectEditorPage;


// ── CLASS: AbstractAttributeTypeEditorPage — TANTIVE IV BRIDGE, AT BATTLE STATIONS ──
// The Tantive IV bridge is the nerve centre that ties together helm, comms, and weapons.
// Each station (page) knows its own job but relies on the bridge for shared services:
// who is captain, what is the ship's current state, and how do we signal that something
// changed?  This abstract class is that shared bridge for all attribute type editor pages
// — the Overview page, Source Code page, and Used By page are the individual stations.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all pages within the Attribute Type Editor.
 * It narrows {@link AbstractSchemaObjectEditorPage} specifically to
 * {@link AttributeTypeEditor} and adds the two attribute-type-specific helpers that
 * every page needs: read the original (as-saved) attribute type for comparison, and
 * read the modified (working-copy) attribute type to display and edit.
 * Think of this as the Tantive IV bridge: each concrete page is a different crew
 * station, but they all reach back to the same captain (the editor) for state.
 */
public abstract class AbstractAttributeTypeEditorPage extends AbstractSchemaObjectEditorPage<AttributeTypeEditor>
{
    /** The flag to indicate if the page has been initialized */
    protected boolean initialized = false;


    // ── New Bridge Station Commissioned ─────────────────────────────────────────────
    // Captain Antilles assigns a new crew member to the Tantive IV's bridge, tells
    // them which console they are at, and gives them their station name — then they
    // wait for orders.  This constructor wires the page to its parent editor, its
    // stable ID for page-switching, and its tab title.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Wires this attribute type editor page into its parent {@link AttributeTypeEditor}.
     * We pass all three arguments straight to the grandparent FormPage constructor;
     * the real work happens in {@code createFormContent()} when Eclipse asks for the UI.
     *
     * <p>For example — Captain Antilles assigns the station:</p>
     * <pre>
     *   // The editor is the ship; id is the console label; title is the tab text.
     *   super( editor, AttributeTypeEditorOverviewPage.ID, "Overview" );
     * </pre>
     *
     * @param editor  the {@link AttributeTypeEditor} that owns this page; used to reach
     *                the original and modified attribute type objects
     * @param id      a unique string identifier for this page within the editor
     * @param title   the human-readable label shown on the tab strip
     */
    public AbstractAttributeTypeEditorPage( AttributeTypeEditor editor, String id, String title )
    {
        super( editor, id, title );
    }


    // ── Reading the Ship's Last Known Good State ─────────────────────────────────────
    // Before a refit the engineer checks the original schematics — the last known good
    // version saved to the archive — to understand what was there before any edits.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the original (pre-edit) attribute type from the parent editor.
     * We use this to compare the current state with the baseline — for example to detect
     * whether the OID was changed, or whether the superior type still resolves.
     * "Original" means the version that was last persisted to the schema handler; it
     * doesn't change as the user makes edits.
     *
     * <p>For example — the engineer checks the original schematics:</p>
     * <pre>
     *   AttributeType original = getOriginalAttributeType();
     *   // Was this OID already taken by someone else?
     *   if ( !original.getOid().equals( modifiedOid ) &amp;&amp; schemaHandler.isOidAlreadyTaken( modifiedOid ) ) { ... }
     * </pre>
     *
     * @return  the unmodified AttributeType as loaded from the schema handler; the caller
     *          must not mutate this object
     */
    public AttributeType getOriginalAttributeType()
    {
        return getEditor().getOriginalAttributeType();
    }


    // ── Reading the Ship's Live Working State ────────────────────────────────────────
    // During a refit the engineer works from a live copy of the schematics — the one
    // that has every in-progress change applied.  This is what the crew is actually
    // flying the ship with right now.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the in-progress (post-edit) attribute type from the parent editor.
     * This is the mutable working copy that accumulates changes as the user edits widgets.
     * When the user saves, this copy replaces the original in the schema handler.
     * Pages modify this object directly (e.g. {@code getModifiedAttributeType().setOid(...)})
     * and then call {@link #setEditorDirty()} so Eclipse knows a save is needed.
     *
     * <p>For example — the engineer works from the live schematics:</p>
     * <pre>
     *   AttributeType at = getModifiedAttributeType();
     *   at.setDescription( descriptionText.getText() );
     *   setEditorDirty();
     * </pre>
     *
     * @return  the mutable working-copy AttributeType; callers are expected to mutate
     *          this object as the user makes changes
     */
    public AttributeType getModifiedAttributeType()
    {
        return getEditor().getModifiedAttributeType();
    }


    // ── Sounding the General Alarm ───────────────────────────────────────────────────
    // When a crew member spots something that requires the captain's attention — an
    // enemy ship, a hull breach — they sound the general alarm.  The captain then knows
    // the ship is in a changed state and needs to act.
    // Here, the "alarm" is the editor's dirty flag: "something changed, a save is needed."
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent editor as dirty (i.e. having unsaved changes).
     * Call this from any listener that modifies the working-copy attribute type so that
     * Eclipse shows the unsaved-changes indicator on the editor tab and enables the Save
     * action.  Forgetting to call this means the user can close the editor without being
     * prompted to save.
     *
     * <p>For example — the crew member sounds the alarm:</p>
     * <pre>
     *   getModifiedAttributeType().setObsolete( true );
     *   setEditorDirty();  // tells Eclipse "this editor needs saving"
     * </pre>
     */
    protected void setEditorDirty()
    {
        getEditor().setDirty( true );
    }
}
