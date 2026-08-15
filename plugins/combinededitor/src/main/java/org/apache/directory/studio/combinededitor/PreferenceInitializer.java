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
package org.apache.directory.studio.combinededitor;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


// ── CLASS: PreferenceInitializer — Tantive IV Pre-Flight Checklist ────────────
// Before the Tantive IV jumps to hyperspace, Captain Antilles' crew runs through
// the pre-flight checklist and sets every system to its default operating mode:
// navigation to auto, shields at full, and comms on standard frequency.
// PreferenceInitializer does the same for the combined editor plugin — it runs
// before any user settings are loaded to ensure sensible defaults are in place.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Sets the factory defaults for the combined entry editor's preferences.
 * Eclipse calls {@link #initializeDefaultPreferences()} once when the preference
 * store is first accessed.  Without this, preference keys would have no default
 * value and code that reads them without checking would see zeros or {@code null}.
 * Think of this as the Tantive IV's pre-flight configuration — everything is
 * set to a known-good state before the first user interaction.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    // ── Captain Antilles Runs the Pre-Flight Checklist ────────────────────────
    // Antilles checks every system in sequence: default display mode set to
    // Template Editor, auto-switch-on-no-template enabled, fallback editor set
    // to Table Editor.  All within the standard operating parameters.
    // initializeDefaultPreferences() applies exactly those three defaults to
    // the CombinedEditorPlugin's preference store.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers factory-default values for all combined editor preferences.
     * We default to the Template editor tab, with auto-switching to the Table
     * editor if no matching template exists for the current entry.
     * Eclipse calls this once before reading any user-saved preferences, so
     * these values are what the user sees on a fresh install and what "Restore
     * Defaults" reverts to.
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = CombinedEditorPlugin.getDefault().getPreferenceStore();

        // Preferences
        store.setDefault( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR,
            CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TEMPLATE );
        store.setDefault( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_TO_ANOTHER_EDITOR, true );
        store.setDefault( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR,
            CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR_TABLE );
    }
}
