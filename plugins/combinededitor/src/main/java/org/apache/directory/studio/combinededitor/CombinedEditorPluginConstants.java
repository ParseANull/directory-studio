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


// ── CLASS: CombinedEditorPluginConstants — The Tantive IV's Configuration Log ──
// Aboard the Tantive IV, Captain Antilles keeps a configuration log that records
// every important ID, mode flag, and system code in one place — so any crew
// member can look up the same number without transcribing it by hand.
// This interface does the same for the combinededitor plugin: every preference
// key and integer code lives here, referenced by name throughout the codebase.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds every constant used across the combinededitor plugin.
 * Preference keys, integer editor-mode codes, and the plugin ID all live here
 * so we change a string in one place rather than hunting through every class.
 * Think of this as the Tantive IV's ship configuration log — one authoritative
 * record that every crew member consults.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface CombinedEditorPluginConstants
{
    /** The plug-in ID */
    String PLUGIN_ID = CombinedEditorPluginConstants.class.getPackage().getName();

    // Preferences
    String PREF_DEFAULT_EDITOR = PLUGIN_ID + ".prefs.DefaultEditor"; //$NON-NLS-1$
    int PREF_DEFAULT_EDITOR_TEMPLATE = 1;
    int PREF_DEFAULT_EDITOR_TABLE = 2;
    int PREF_DEFAULT_EDITOR_LDIF = 3;
    String PREF_AUTO_SWITCH_TO_ANOTHER_EDITOR = PLUGIN_ID + ".prefs.AutoSwitchToAnotherEditor"; //$NON-NLS-1$
    String PREF_AUTO_SWITCH_EDITOR = PLUGIN_ID + ".prefs.AutoSwitchEditor"; //$NON-NLS-1$
    int PREF_AUTO_SWITCH_EDITOR_TABLE = 1;
    int PREF_AUTO_SWITCH_EDITOR_LDIF = 2;
}
