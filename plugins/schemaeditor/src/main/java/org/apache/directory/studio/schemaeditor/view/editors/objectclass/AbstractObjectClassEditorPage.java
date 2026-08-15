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

package org.apache.directory.studio.schemaeditor.view.editors.objectclass;


import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.view.editors.AbstractSchemaObjectEditorPage;


// ── CLASS: AbstractObjectClassEditorPage — TANTIVE IV BRIDGE ─────────────────
// Leia stands at the center of the Tantive IV's bridge, every officer
// station wired into her command — communications, navigation, weapons,
// all drawing from the same central console.
// This abstract class is that bridge: every object class editor page
// (Overview, SourceCode) extends it and gets shared access to the same
// editor, the same object class data, and the same dirty-flag mechanism.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for all pages that live inside the {@link ObjectClassEditor}.
 * Subclasses get shared, type-safe access to the parent editor plus convenience
 * methods for retrieving the original and modified {@link ObjectClass} instances.
 * Think of this as Leia's bridge console — every specialized page officer plugs
 * in here and draws their orders from the same command line.
 */
public abstract class AbstractObjectClassEditorPage extends AbstractSchemaObjectEditorPage<ObjectClassEditor>
{
    /** The flag to indicate if the page has been initialized */
    protected boolean initialized = false;


    // ── Leia Opens the Bridge Stations ───────────────────────────────────────
    // Leia strides onto the Tantive IV bridge and assigns each officer to their
    // station — this constructor wires the new page into the parent editor,
    // giving it an ID so Eclipse can track it and a title for the tab label.
    // Without this, the page would have no identity inside the multi-page editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wires this editor page into the parent {@link ObjectClassEditor}.
     * We delegate immediately to the superclass, which handles the Eclipse
     * FormPage plumbing — registering the page with the editor and setting
     * its tab title.
     *
     * @param editor  the parent {@link ObjectClassEditor} that owns this page
     * @param id      a unique string identifier for this page within the editor
     * @param title   the human-readable label shown on the page's tab
     */
    public AbstractObjectClassEditorPage( ObjectClassEditor editor, String id, String title )
    {
        super( editor, id, title );
    }


    // ── Leia Reads the Ship's Original Manifest ───────────────────────────────
    // The Tantive IV bridge keeps the original mission manifest locked in the
    // command console — untouched, the ground truth we compare everything against.
    // This method is our ticket to that manifest: the object class exactly as it
    // existed in the schema before the user started making changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the original, unmodified {@link ObjectClass} that this editor was opened on.
     * We need this to detect changes (comparing original vs. modified) and to
     * notify the schema handler which object to replace when saving.
     *
     * @return  the original {@link ObjectClass} as it was when the editor opened
     */
    public ObjectClass getOriginalObjectClass()
    {
        return getEditor().getOriginalObjectClass();
    }


    // ── Leia Reads the Current Working Orders ────────────────────────────────
    // Alongside the original manifest, the bridge also tracks the in-progress
    // mission orders — what's changed since departure. This method hands us the
    // working copy of the object class that accumulates the user's edits.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the working (modified) copy of the {@link ObjectClass} being edited.
     * Every user change is applied to this copy, leaving the original intact until
     * the user explicitly saves. Pages read from and write to this object.
     *
     * @return  the modified {@link ObjectClass} that holds the current in-progress edits
     */
    public ObjectClass getModifiedObjectClass()
    {
        return getEditor().getModifiedObjectClass();
    }


    // ── Leia Raises the Red Alert ─────────────────────────────────────────────
    // When a sensor picks up something unexpected, Leia doesn't stay quiet —
    // she raises red alert so the whole ship knows the situation has changed.
    // This method is our red alert: it tells the editor that unsaved changes
    // exist, which triggers the dirty indicator in the Eclipse editor tab.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent editor as dirty, meaning the user has made unsaved changes.
     * Eclipse uses the dirty state to show the asterisk in the editor tab title
     * and to decide whether to prompt "Save changes?" when closing.
     */
    protected void setEditorDirty()
    {
        getEditor().setDirty( true );
    }
}
