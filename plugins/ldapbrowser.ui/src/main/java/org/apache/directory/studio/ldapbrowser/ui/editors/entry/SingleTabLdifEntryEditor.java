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


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;


// ── CLASS: SingleTabLdifEntryEditor — TANTIVE IV, ONE SHARED RAW-FEED SCREEN ──
// On a small rebel transport, one screen shows the raw telemetry feed — no
// pretty displays, just the raw signal data for whoever needs to read it now.
// The screen is shared: whoever sits down at the console gets the current signal,
// and the feed repoints to a new target when they change course.
// SingleTabLdifEntryEditor is that shared raw-feed screen: one Eclipse editor tab,
// LDIF text format, reused for every entry the user opens.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An LDIF-format entry editor that reuses a single Eclipse editor tab for all entries.
 * Combines the single-tab reuse behavior of {@link SingleTabEntryEditor} with the LDIF
 * text display of {@link LdifEntryEditor} — opening a new entry replaces the LDIF text
 * in the same tab rather than opening a new one.
 * Auto-save is always off in LDIF editors (see {@link LdifEntryEditor#isAutoSave()}).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingleTabLdifEntryEditor extends LdifEntryEditor
{

    // ── THE SHARED SCREEN ANNOUNCES ITS CONSOLE ID ────────────────────────────
    // The single raw-feed screen identifies itself to the ship's console registry
    // so the routing system knows which editor to reuse when the user opens an entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse editor ID for this single-tab LDIF entry editor.
     * Eclipse uses this string to look up and reuse the existing editor tab
     * rather than opening a new one when this editor type is already open.
     *
     * @return the editor ID constant from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.EDITOR_SINGLE_TAB_LDIF_ENTRY_EDITOR;
    }

}
