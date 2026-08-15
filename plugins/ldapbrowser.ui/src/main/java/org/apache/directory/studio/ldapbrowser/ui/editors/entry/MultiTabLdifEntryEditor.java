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


// ── CLASS: MultiTabLdifEntryEditor — TANTIVE IV, RAW TELEMETRY ON EVERY STATION
// On the Tantive IV bridge, each station has its own raw telemetry readout —
// the navigator's screen, the comms officer's screen, the weapons officer's screen —
// each showing the real signal data in plain format, each one a separate console.
// MultiTabLdifEntryEditor is that multi-station raw-data variant: it opens a new
// LDIF text editor tab for every entry the user opens, so they can edit multiple
// entries in LDIF format simultaneously in separate tabs.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An LDIF-format entry editor that opens a new tab for each entry.
 * Combines the multi-tab behavior of {@link MultiTabEntryEditor} with the LDIF
 * text display of {@link LdifEntryEditor} — each LDAP entry gets its own dedicated
 * LDIF text editor tab, allowing multiple entries to be open and edited at once.
 * Auto-save is always off in LDIF editors (see {@link LdifEntryEditor#isAutoSave()}).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultiTabLdifEntryEditor extends LdifEntryEditor
{

    // ── THE LDIF STATION REPORTS ITS CONSOLE ID ───────────────────────────────
    // The raw telemetry station identifies itself to the ship's computer so
    // it can be addressed by the right command routing system.
    // Eclipse uses this ID to find and open this specific editor type.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse editor ID for this multi-tab LDIF entry editor.
     * Eclipse uses this string to look up the editor in the extension registry
     * and to open it when the user requests an LDIF view for an entry in multi-tab mode.
     *
     * @return the editor ID constant from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.EDITOR_MULTI_TAB_LDIF_ENTRY_EDITOR;
    }

}
