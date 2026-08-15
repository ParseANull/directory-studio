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


// ── CLASS: MultiTabEntryEditor — TANTIVE IV, MULTIPLE CREW STATIONS ACTIVE ────
// On the Tantive IV bridge, multiple crew members can be working simultaneously —
// the navigator at one station, the communications officer at another, the weapons
// officer at a third — each on their own mission, each with their own console.
// MultiTabEntryEditor is the multi-station configuration: each LDAP entry gets its
// own dedicated Eclipse editor tab so multiple entries can be open at once, each with
// its own edit state, auto-save setting, and lifecycle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An entry editor that opens a new editor tab for each LDAP entry.
 * In contrast to {@link SingleTabEntryEditor} (which reuses one tab), this editor
 * creates a separate Eclipse editor part for each entry the user opens — like
 * having each crew member at their own dedicated console.
 * Auto-save behavior is read from the multi-tab preference setting so users can
 * configure it independently of the single-tab editor.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultiTabEntryEditor extends EntryEditor
{
    // ── CREW MEMBER ASKS "WHICH STATION AM I?" ────────────────────────────────
    // Each crew member on the Tantive IV has a unique station ID so the ship's
    // computer can route commands to exactly the right console.
    // Eclipse uses this ID to find and register this editor type in the workbench.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse editor ID for this multi-tab entry editor.
     * Eclipse uses this string to look up the editor in the extension registry,
     * open it by ID, and match editor inputs to the right editor type.
     *
     * @return the editor ID constant from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.EDITOR_MULTI_TAB_ENTRY_EDITOR;
    }


    // ── CREW MEMBER CHECKS IF AUTO-COMMIT IS ENABLED FOR THEIR STATION ────────
    // The navigator checks their console settings — is this station set to
    // auto-transmit changes, or does everything need a manual "send" command?
    // We read the multi-tab-specific auto-save preference so the behavior can
    // differ between the single-tab and multi-tab editor instances.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this editor commits changes automatically on every edit.
     * The auto-save setting for the multi-tab editor is stored separately from
     * the single-tab editor's setting, so users can configure them independently.
     *
     * @return {@code true} if changes should be committed to LDAP immediately without an explicit save.
     */
    @Override
    public boolean isAutoSave()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_MULTI_TAB );
    }
}
