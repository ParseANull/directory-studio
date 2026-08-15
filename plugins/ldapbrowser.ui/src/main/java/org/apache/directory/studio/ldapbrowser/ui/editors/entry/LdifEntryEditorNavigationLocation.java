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


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.EntryEditorUtils;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextSelectionNavigationLocation;


// ── CLASS: LdifEntryEditorNavigationLocation — R2-D2 LOGS THE CURSOR POSITION ─
// R2-D2 plugs into the Death Star's computer and notes not just which sector
// he's in, but exactly which terminal port he's using — cursor position matters.
// LdifEntryEditorNavigationLocation does the same for the LDIF text editor:
// it records both the LDAP entry being displayed AND the text cursor position
// within the LDIF content, so Back/Forward in Eclipse restores the exact view.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A navigation history snapshot for the LDIF entry editor that includes text selection.
 * The LDIF editor is a text editor, so navigation history must capture not just which
 * LDAP entry is being shown but also where the cursor (text selection) was in the LDIF text.
 * Think of this as R2 logging both the sector AND the exact terminal port he was using.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEntryEditorNavigationLocation extends TextSelectionNavigationLocation
{

    // ── R2 JACKS IN AND STARTS LOGGING ───────────────────────────────────────
    // R2 inserts his interface arm into the terminal and begins logging:
    // which port am I at? What sector? He records both immediately.
    // We delegate to the superclass which captures the text editor and selection state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation location for the given text editor, optionally initializing
     * from the editor's current state (text selection + entry input).
     * When {@code initialize} is {@code true}, the superclass captures the current
     * selection immediately; when {@code false}, it creates an empty placeholder.
     *
     * @param part        The LDIF text editor this location belongs to.
     * @param initialize  {@code true} to capture the current state; {@code false} for a placeholder.
     */
    public LdifEntryEditorNavigationLocation( ITextEditor part, boolean initialize )
    {
        super( part, initialize );
    }


    // ── R2 LABELS THE LOG ENTRY FOR THE REBELS ───────────────────────────────
    // R2 converts his internal log entry into a short, human-readable status
    // message that the rebels can read on a screen above the terminal.
    // We use the entry editor utility to format the entry's name nicely, falling
    // back to the superclass default (text offset) if no entry name is available.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable label for this navigation history entry.
     * Eclipse displays this in the navigation history dropdown. We prefer the
     * LDAP entry's display name over the default text-position-based label.
     *
     * @return the entry's display name, or the superclass text-offset label if unavailable.
     */
    public String getText()
    {
        String text = EntryEditorUtils.getHistoryNavigationText( getEntryEditorInput() );
        return text != null ? text : super.getText();
    }


    // ── R2 CHECKS IF THIS PORT IS THE SAME AS THE LAST ONE VISITED ───────────
    // Before logging a new port visit, R2 checks: "Is this the same terminal
    // I was just at?" — he doesn't want to log the same location twice in a row.
    // We compare entry inputs (not text positions) to decide whether to merge.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this location represents the same LDAP entry as {@code currentLocation}.
     * Eclipse calls this to collapse consecutive identical history entries. We compare
     * on entry input identity, not text position — two visits to the same entry but different
     * cursor positions are still the "same" navigation location for history purposes.
     *
     * @param currentLocation  The current top-of-stack navigation location to compare against.
     * @return {@code true} if both locations point to the same LDAP input object.
     */
    public boolean mergeInto( INavigationLocation currentLocation )
    {
        if ( currentLocation == null )
        {
            return false;
        }

        if ( getClass() != currentLocation.getClass() )
        {
            return false;
        }

        LdifEntryEditorNavigationLocation location = ( LdifEntryEditorNavigationLocation ) currentLocation;
        Object other = location.getEntryEditorInput().getInput();
        Object entry = getEntryEditorInput().getInput();

        if ( other == null && entry == null )
        {
            return true;
        }
        else if ( other == null || entry == null )
        {
            return false;
        }
        else
        {
            return entry.equals( other );
        }
    }


    // ── R2 RETRIEVES THE TYPED DOSSIER FROM HIS MEMORY ───────────────────────
    // R2 pulls the structured mission dossier out of his memory banks —
    // not the raw binary dump, but the typed, organized format the team can use.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the stored input as a typed {@link EntryEditorInput}, or returns {@code null}.
     * The base class stores an untyped input object; this helper casts it safely.
     *
     * @return the {@link EntryEditorInput} associated with this location, or {@code null} if none.
     */
    private EntryEditorInput getEntryEditorInput()
    {
        Object editorInput = getInput();
        if ( editorInput instanceof EntryEditorInput )
        {
            EntryEditorInput entryEditorInput = ( EntryEditorInput ) editorInput;
            return entryEditorInput;
        }

        return null;
    }
}
