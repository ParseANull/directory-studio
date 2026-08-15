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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;


// ── CLASS: SingleTabSearchResultEditorMatchingStrategy — Mace Windu at the Door ──
// Mace Windu stands at the Senate chamber door with a simple rule: "If you're a
// Jedi, you're in the right place — come in to this chamber."  He doesn't check
// which Jedi you are, just the type.  Every search result input gets the same
// treatment: this is the right editor, come on in and reuse the tab.
// This strategy tells Eclipse to always reuse the existing search result editor
// tab for any new search result input — rather than opening a new tab each time
// the user picks a different search.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An {@link IEditorMatchingStrategy} that makes all {@link SearchResultEditorInput}
 * objects match the existing search result editor tab.
 * When Eclipse is about to open an editor for a new input, it calls {@link #matches}
 * on each open editor reference.  If we return {@code true}, Eclipse reuses that
 * editor instead of opening a new tab.
 * We return {@code true} for any {@link SearchResultEditorInput} — so all searches
 * are shown in a single reusable tab.  The tab's content updates via
 * {@link SearchResultEditor#showEditorInput}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingleTabSearchResultEditorMatchingStrategy implements IEditorMatchingStrategy
{

    // ── Mace Windu Checks the Type Badge ──────────────────────────────────────
    // If the input is a SearchResultEditorInput, it gets into this editor.
    // We don't care which search it is — that's the whole point of single-tab mode.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the given input is a {@link SearchResultEditorInput},
     * causing Eclipse to reuse the referenced editor tab rather than opening a new one.
     * Returns {@code false} for any other input type, meaning we won't steal
     * unrelated editor tabs.
     *
     * @param editorRef the existing open editor reference (not used — we only care about input type)
     * @param input     the editor input Eclipse is trying to open
     * @return {@code true} if the input is a {@link SearchResultEditorInput}
     */
    public boolean matches( IEditorReference editorRef, IEditorInput input )
    {
        if ( !( input instanceof SearchResultEditorInput ) )
        {
            return false;
        }

        return true;
    }

}
