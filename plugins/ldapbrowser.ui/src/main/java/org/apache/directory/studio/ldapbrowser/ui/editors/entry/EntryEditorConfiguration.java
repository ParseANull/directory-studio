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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetConfiguration;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: EntryEditorConfiguration — PALPATINE ISSUES THE STANDING ORDERS ───
// Before Order 66 goes out, Palpatine sets the standing parameters: which troopers
// carry blasters, what mode they operate in, which targets are authorized.
// EntryEditorConfiguration is those standing orders for the entry editor widget:
// it controls how values are edited, whether auto-save is on, and which editor
// manager gets handed to the action group when it needs to open a value editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds and creates the configuration objects for the entry editor widget.
 * The base class ({@link EntryEditorWidgetConfiguration}) already provides
 * the content/label providers and sorter; we extend it to add a lazily-created
 * {@link ValueEditorManager} that knows whether auto-save is active.
 * Think of this as the standing orders document: every component of the editor
 * checks it to find out the rules they operate under.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorConfiguration extends EntryEditorWidgetConfiguration
{
    /** The entry editor */
    private EntryEditor entryEditor;

    // ── PALPATINE SIGNS THE CONFIGURATION DECREE ─────────────────────────────
    // Palpatine hands the standing orders to a specific battalion commander —
    // from this point on, the commander knows whose authority they answer to.
    // We store a reference to the entry editor so we can later ask whether
    // auto-save is active when creating the value editor manager.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new configuration bound to the given entry editor.
     * We need the editor reference so we can query its auto-save setting when
     * lazily constructing the {@link ValueEditorManager} on first use.
     *
     * @param entryEditor  The entry editor this configuration belongs to.
     */
    public EntryEditorConfiguration( EntryEditor entryEditor )
    {
        this.entryEditor = entryEditor;
    }


    // ── PALPATINE AUTHORIZES THE WEAPONS MANIFEST ─────────────────────────────
    // The battalion asks Palpatine for the list of authorized weapons — he checks
    // whether the current rules allow auto-fire (auto-save) and issues the right
    // manifest on first request, then hands the same one out on every later request.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value editor manager, creating it lazily on first call.
     * The manager knows which value editor to open for each attribute type.
     * We pass {@code true} for the "rename" flag and check auto-save mode so
     * the embedded cell editors behave correctly (immediate commit vs. deferred).
     *
     * <p>For example — the weapons manifest is issued:</p>
     * <pre>
     *   if (manifest == null) {
     *     manifest = new ValueEditorManager(tree, renameEnabled, autoSave);
     *   }
     *   return manifest; // same one every time after that
     * </pre>
     *
     * @param viewer  The tree viewer that will host inline cell editors; we pull
     *                the underlying SWT tree from it to construct the manager.
     * @return the shared {@link ValueEditorManager} for this editor instance.
     */
    public ValueEditorManager getValueEditorManager( TreeViewer viewer )
    {
        if ( valueEditorManager == null )
        {
            valueEditorManager = new ValueEditorManager( viewer.getTree(), true, entryEditor.isAutoSave() );
        }

        return valueEditorManager;
    }
}
