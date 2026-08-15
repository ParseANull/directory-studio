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

package org.apache.directory.studio.common.ui;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


// ── CLASS: CommonUIPreferencesInitializer — IMPERIAL CONFIGURATION TERMINAL ─
// When the Death Star's systems boot up, an Imperial technician runs through
// the factory-reset checklist on the configuration terminal, making sure every
// switch is at its safe default before any operator touches it.  We play that
// role for the common.ui plugin: we set all color preference keys to their
// default (empty) value so the active Eclipse theme CSS can take over unless
// the user explicitly overrides them.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We initialize the default values for all color-related preference keys in
 * the common.ui plugin.  By setting every key to the empty string default, we
 * tell Eclipse to defer to the active theme's CSS rules rather than hard-coding
 * any particular color.  This class is wired in via the plugin's extension
 * point and called automatically at startup.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CommonUIPreferencesInitializer extends AbstractPreferenceInitializer
{
    // ── METHOD initializeDefaultPreferences — RUNNING THE FACTORY RESET ──────
    // The Imperial technician runs down the checklist: each color preference key
    // gets set to the empty string sentinel, which means "defer to CSS".  This
    // is the factory state — no user-chosen color scheme has been applied yet,
    // so the active Eclipse theme is free to paint everything its own way.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We set every semantic color preference to its default value, which is the
     * empty string.  This defers color choices to the active Eclipse theme's
     * CSS files ({@code default.css} and {@code dark.css}).  Eclipse calls this
     * method automatically during startup via the preferences initializer
     * extension point.
     * {@inheritDoc}
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = CommonUIPlugin.getDefault().getPreferenceStore();

        // Actual colors are defined in default.css and dark.css
        String dflt = IPreferenceStore.STRING_DEFAULT_DEFAULT;
        store.setDefault( CommonUIConstants.DEFAULT_COLOR, dflt );
        store.setDefault( CommonUIConstants.DISABLED_COLOR, dflt );
        store.setDefault( CommonUIConstants.ERROR_COLOR, dflt );
        store.setDefault( CommonUIConstants.COMMENT_COLOR, dflt );
        store.setDefault( CommonUIConstants.KEYWORD_1_COLOR, dflt );
        store.setDefault( CommonUIConstants.KEYWORD_2_COLOR, dflt );
        store.setDefault( CommonUIConstants.OBJECT_CLASS_COLOR, dflt );
        store.setDefault( CommonUIConstants.ATTRIBUTE_TYPE_COLOR, dflt );
        store.setDefault( CommonUIConstants.VALUE_COLOR, dflt );
        store.setDefault( CommonUIConstants.OID_COLOR, dflt );
        store.setDefault( CommonUIConstants.SEPARATOR_COLOR, dflt );
        store.setDefault( CommonUIConstants.ADD_COLOR, dflt );
        store.setDefault( CommonUIConstants.DELETE_COLOR, dflt );
        store.setDefault( CommonUIConstants.MODIFY_COLOR, dflt );
        store.setDefault( CommonUIConstants.RENAME_COLOR, dflt );
    }

}
