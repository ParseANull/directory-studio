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


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;


// ── CLASS: SingleTabEntryEditor — TANTIVE IV, ONE SHARED MISSION CONSOLE ──────
// On a smaller rebel craft, there's only one main console — not enough room for
// a separate station per crew member. Everyone who needs to look at the displays
// takes turns at the same console, which gets reconfigured for each user.
// SingleTabEntryEditor is that shared console: it reuses one Eclipse editor tab
// for every LDAP entry the user opens, replacing the current content with the new
// entry each time instead of opening a fresh tab.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An entry editor that reuses a single Eclipse editor tab for all entries.
 * In contrast to {@link MultiTabEntryEditor} (one tab per entry), this editor
 * recycles its tab — when the user opens a new entry, the same tab is updated to
 * show the new content. This keeps the editor area uncluttered for users who
 * browse many entries quickly.
 * Auto-save behavior is read from the single-tab preference setting so it can be
 * configured independently from the multi-tab editor's setting.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingleTabEntryEditor extends EntryEditor
{
    // ── THE SHARED CONSOLE IDENTIFIES ITSELF ──────────────────────────────────
    // The single-console craft announces its registration number to the fleet
    // computer so commands can be routed to exactly this kind of console.
    // Eclipse uses this ID to find, open, and reuse this editor type.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse editor ID for this single-tab entry editor.
     * Eclipse uses this string to look up the editor in the extension registry and
     * to reuse the existing tab when the same editor ID is already open.
     *
     * @return the editor ID constant from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.EDITOR_SINGLE_TAB_ENTRY_EDITOR;
    }


    // ── THE CONSOLE CHECKS ITS OWN AUTO-TRANSMIT SETTING ─────────────────────
    // The single console checks its own configuration panel — is auto-transmit
    // on or off for this type of console? The answer drives whether edits fire
    // immediately to the LDAP server or wait for an explicit save command.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this editor auto-saves changes immediately on every edit.
     * We read the single-tab specific preference so this editor can be configured
     * independently of the multi-tab editor's auto-save setting.
     *
     * @return {@code true} if edits should be immediately committed to the LDAP server.
     */
    @Override
    public boolean isAutoSave()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_SINGLE_TAB );
    }
}
