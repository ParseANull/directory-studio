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
import org.eclipse.ui.PartInitException;


// ── CLASS: MultiTabEntryEditorMatchingStrategy — MACE WINDU CONFRONTING PALPATINE ──
// Mace Windu strides into the Chancellor's office, lightsaber raised, and runs a
// precise three-part check: is this actually a Sith Lord? Is this the same office?
// Does the face match the file? Only when all three pass does he declare a match.
// This strategy does the same — it checks the editor type, the multi-window flag,
// and whether the resolved entries match before deciding to reuse an existing tab.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Tells Eclipse when a new {@link EntryEditorInput} matches an already-open
 * multi-tab (multi-window) entry editor, so we reuse the existing tab instead
 * of opening a duplicate.
 * Multi-tab editors open a fresh tab per distinct LDAP entry, so we match when
 * the editor ID is the same AND the resolved DN is the same — two tabs showing
 * "cn=Luke" in the same multi-window editor would just confuse everyone.
 * Think of Mace Windu's confirmation ritual: the suspect must pass every check
 * before he rules "same target."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultiTabEntryEditorMatchingStrategy implements IEditorMatchingStrategy
{

    // ── Mace Windu Runs His Three-Point Sith Identification Check ───────────────
    // First: is the person in the room even a Force user? Second: is this the right
    // editor type (multi-window)? Third: do the resolved entries share a DN?
    // Only a clean pass on every question earns a "match" — reuse this tab.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the open editor referenced by {@code editorRef} should absorb
     * the incoming {@code input} rather than letting Eclipse open a new tab.
     * We say yes only when all three conditions hold: the input is an
     * {@link EntryEditorInput}, its extension declares multi-window mode, the editor
     * part ID matches, and both inputs resolve to the same LDAP entry.
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
        if ( !entryEditorInput.getExtension().isMultiWindow() )
        {
            return false;
        }
        if ( !editorRef.getId().equals( entryEditorInput.getExtension().getEditorId() ) )
        {
            return false;
        }

        try
        {
            IEditorInput otherInput = editorRef.getEditorInput();
            if ( !( otherInput instanceof EntryEditorInput ) )
            {
                return false;
            }
            EntryEditorInput otherEntryEditorInput = ( EntryEditorInput ) otherInput;
            if ( entryEditorInput.getResolvedEntry() == null && otherEntryEditorInput.getResolvedEntry() == null )
            {
                return true;
            }
            return entryEditorInput.getResolvedEntry() != null && otherEntryEditorInput.getResolvedEntry() != null
                && entryEditorInput.getResolvedEntry().equals( otherEntryEditorInput.getResolvedEntry() );
        }
        catch ( PartInitException e )
        {
            return false;
        }
    }

}
