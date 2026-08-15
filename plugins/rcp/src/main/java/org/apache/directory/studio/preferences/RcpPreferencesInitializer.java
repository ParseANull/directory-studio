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

package org.apache.directory.studio.preferences;


import org.apache.directory.studio.Activator;
import org.apache.directory.studio.PluginConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


// ── CLASS: RcpPreferencesInitializer — Setting the Rebel Base's Default Protocols ──
// Before the Rebels scramble for Yavin, the base commanders set every system to
// its default configuration: shields at full, evac routes pre-programmed, and
// the "confirm before evacuation" alarm enabled by default so nobody accidentally
// triggers it with a stray elbow.
// RcpPreferencesInitializer does the same for Studio: it runs once at startup
// to ensure every preference has a sensible default before the user changes anything.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Sets the factory-default values for all rcp plugin preferences.
 * Eclipse calls {@link #initializeDefaultPreferences} once when the preference
 * store is first accessed, before any user-configured values are loaded.
 * This ensures that preferences have reasonable values even on a fresh install.
 * Think of this as the Yavin base commander setting all systems to their
 * pre-battle default configuration.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RcpPreferencesInitializer extends AbstractPreferenceInitializer
{
    // ── Yavin Base Commander Sets the Default Protocols ───────────────────────
    // The base commander walks the pre-battle checklist: confirm-on-exit alarm
    // is enabled by default so that closing the last window always asks the user
    // whether they really mean it — nobody wants to lose unsaved work by accident.
    // initializeDefaultPreferences() applies exactly that default to the store.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers the default values for all rcp preferences in the preference store.
     * Eclipse calls this once, before reading any user-saved values, so it provides
     * the "factory defaults" that are shown when the user clicks "Restore Defaults".
     * We default the exit-confirmation dialog to {@code true} (enabled) so a
     * fresh install prompts before closing the last window.
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault( PluginConstants.PREFERENCE_EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, true );
    }
}
