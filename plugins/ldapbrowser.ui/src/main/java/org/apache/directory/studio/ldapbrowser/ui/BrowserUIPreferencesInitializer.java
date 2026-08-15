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

package org.apache.directory.studio.ldapbrowser.ui;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


// ── CLASS: BrowserUIPreferencesInitializer — Palpatine Sets the Defaults ─────
// Before the Emperor ever transmits Order 66, he has already decided what the
// default state of every clone is: loyal, disciplined, awaiting activation.
// This class does the same thing for our UI preferences — before any user has
// opened the Preferences dialog, we stamp in sensible defaults so the plugin
// behaves correctly on the very first launch without any explicit configuration.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Seeds the Eclipse preference store with default values for all Browser UI
 * preferences the first time the plugin runs, before the user has touched anything.
 * Without this, preferences with no stored value would come back as empty strings
 * or zero — almost certainly the wrong behaviour. Think of this class as
 * Palpatine pre-programming the clones: every preference starts in the state we
 * want, and the user can override from there.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserUIPreferencesInitializer extends AbstractPreferenceInitializer
{
    // ── Order 66 Is Transmitted — Defaults Go Out ────────────────────────────
    // Palpatine activates the biochip in every clone trooper simultaneously,
    // locking in their default behaviour galaxy-wide in a single transmission.
    // We call {@code store.setDefault()} for each preference key in exactly the
    // same spirit: one method, all defaults, fired once at plugin start-up.
    // "Execute Order 66." — and just like that, every clone knows what to do.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the out-of-the-box default values into the plugin's preference store.
     * Eclipse calls this exactly once, before any preference page has been shown.
     * We set defaults for browser linking, search result display options, and
     * entry editor open-mode behaviour — everything the UI needs to function
     * sensibly on a clean install.
     *
     * <p>For example — Palpatine stamps in the defaults before anyone can ask:</p>
     * <pre>
     *   store.setDefault(LINK_WITH_EDITOR, true);   // clones are loyal by default
     *   store.setDefault(SHOW_DN, true);             // show Distinguished Names
     *   store.setDefault(SORT_FILTER_LIMIT, 10000);  // cap at 10k results
     * </pre>
     */
    @Override
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = BrowserUIPlugin.getDefault().getPreferenceStore();

        // Browser
        store.setDefault( BrowserUIConstants.PREFERENCE_BROWSER_LINK_WITH_EDITOR, true );

        // Search Result Editor
        store.setDefault( BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN, true );
        store.setDefault( BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_LINKS, true );
        store.setDefault( BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SORT_FILTER_LIMIT, 10000 );

        // Entry Editors
        store.setDefault( BrowserUIConstants.PREFERENCE_ENTRYEDITORS_USE_USER_PRIORITIES, false );
        store.setDefault( BrowserUIConstants.PREFERENCE_ENTRYEDITORS_USER_PRIORITIES, "" ); //$NON-NLS-1$
        store.setDefault( BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE,
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_HISTORICAL_BEHAVIOR );
    }

}
