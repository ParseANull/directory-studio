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

package org.apache.directory.studio.entryeditors;


import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;


// ── CLASS: SingleTabEntryEditorMatchingStrategy — MACE WINDU'S SIMPLER CHECK ─
// When Mace Windu is looking for a specific senator's office — not verifying Sith
// identity, just confirming "is this the right room?" — he only needs to check the
// name on the door. He doesn't need to inspect the occupant's midi-chlorian count.
// Single-tab editors work the same way: we only care that the editor type ID matches.
// If this is the right kind of editor, it always absorbs the new entry — no DN check.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Tells Eclipse when a new {@link EntryEditorInput} matches an already-open
 * single-tab entry editor, so the existing tab is reused instead of opening a new one.
 * Single-tab editors show only one entry at a time, replacing whatever was there.
 * So for us "match" just means the editor part ID is right — same door, same room.
 * We don't check the resolved entry at all; single-tab editors are always reused
 * for any entry that comes their way.
 * Think of Mace Windu confirming the room number but not bothering to identify
 * the person already seated inside.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingleTabEntryEditorMatchingStrategy implements IEditorMatchingStrategy
{

    // ── Mace Windu Checks the Name on the Door, Nothing More ────────────────────
    // Mace knocks, reads the nameplate — "Chancellor's Office, Type: single-window"
    // — and if it matches, he goes in without asking who is inside.
    // We do the same: verify the input is an EntryEditorInput marked single-window,
    // verify the editor part ID matches, and declare a match immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the open editor referenced by {@code editorRef} should absorb
     * the incoming {@code input} rather than letting Eclipse open a new tab.
     * For single-tab editors we only need two things to be true: the input must
     * declare single-window mode (not multi-window), and the editor part ID must
     * match. We deliberately don't compare resolved entries — the existing tab will
     * simply replace its content with the new entry.
     *
     * @param editorRef  the reference to an already-open editor in the workbench
     * @param input      the new input Eclipse is about to open
     * @return           {@code true} if the open editor should absorb this input (reuse tab);
     *                   {@code false} to let Eclipse open a new tab
     */
    public boolean matches( IEditorReference editorRef, IEditorInput input )
    {
        if ( !( input instanceof EntryEditorInput ) )
        {
            return false;
        }
        EntryEditorInput entryEditorInput = ( EntryEditorInput ) input;

        if ( entryEditorInput.getExtension() == null )
        {
            return false;
        }
        if ( entryEditorInput.getExtension().isMultiWindow() )
        {
            return false;
        }
        if ( !editorRef.getId().equals( entryEditorInput.getExtension().getEditorId() ) )
        {
            return false;
        }

        return true;
    }

}
