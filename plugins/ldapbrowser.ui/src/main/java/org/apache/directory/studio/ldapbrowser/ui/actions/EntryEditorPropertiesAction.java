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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.ldapbrowser.common.actions.PropertiesAction;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.ui.editors.entry.EntryEditor;
import org.eclipse.ui.IEditorInput;


// ── CLASS: EntryEditorPropertiesAction — CLONE TROOPER EXECUTES THE ORDER ───
// A clone trooper receives the signal — "Order 66, execute" — identifies the
// target from his current assignment, and carries out the directive without
// hesitation. This action does the same: it receives a reference to the
// currently active entry editor, identifies the entry being edited, and opens
// the Properties dialog for it when the command fires.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse Properties dialog for the entry currently being displayed
 * in an {@link EntryEditor}.
 * This action is a specialisation of {@link PropertiesAction} for the entry
 * editor context: rather than deriving the selected entry from the global
 * workbench selection, it reads the entry from the editor's own input, which
 * is more reliable when the editor has focus.
 * Think of this as the clone trooper who knows exactly which Jedi he's assigned
 * to — he doesn't need to search the crowd, he already has the target.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorPropertiesAction extends PropertiesAction
{
    private EntryEditor entryEditor;


    // ── Trooper Receives His Assignment ──────────────────────────────────────
    // The trooper is told which Jedi he's tracking — from this moment on,
    // he watches only that target and ignores the rest of the battle.
    // We store the reference to the entry editor that owns this action so
    // {@link #getSelectedEntries()} can pull the entry from its input.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EntryEditorPropertiesAction} bound to the given editor.
     * The editor reference is used in {@link #getSelectedEntries()} to locate
     * the entry being edited, rather than consulting the global workbench selection.
     *
     * @param entryEditor  the entry editor that owns this action; may be null,
     *                     in which case the action will return no selected entries
     */
    public EntryEditorPropertiesAction( EntryEditor entryEditor )
    {
        super();
        this.entryEditor = entryEditor;
    }


    // ── Trooper Identifies the Target Entry ──────────────────────────────────
    // The trooper checks his assignment: who is he watching right now? He only
    // reports a target when no individual values are selected (opening properties
    // on a value is a different operation); otherwise he returns the entry from
    // the editor's current input.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry currently displayed in the bound {@link EntryEditor},
     * but only when no attribute values are selected (which would trigger a
     * value-properties dialog instead).
     * We read the entry from the editor's {@link EntryEditorInput} rather than
     * the global selection, so this works correctly even when the editor's
     * selection state differs from the workbench selection.
     *
     * @return  a single-element array containing the current entry, or an empty
     *          array if no entry is available or values are selected
     */
    public IEntry[] getSelectedEntries()
    {
        // We're only returning the entry when no value is selected
        if ( getSelectedValues().length == 0 )
        {
            if ( entryEditor != null )
            {
                IEditorInput input = entryEditor.getEditorInput();
                if ( input instanceof EntryEditorInput )
                {
                    IEntry entry = ( ( EntryEditorInput ) input ).getResolvedEntry();
                    if ( entry != null )
                    {
                        return new IEntry[]
                            { entry };
                    }
                }
            }
        }

        return new IEntry[0];
    }
}
