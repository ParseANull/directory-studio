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
package org.apache.directory.studio.templateeditor;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


// ── CLASS: PreferenceInitializer — PALPATINE ISSUING STANDING ORDERS ─────────────
// Before the Senate session even opens, Palpatine has already drafted the standing
// orders that govern how the Empire operates by default — who answers to whom, what
// the default patrol routes are, and which systems are monitored. These orders exist
// before any user ever touches a preference. This class does the same thing for
// Eclipse: it runs once at startup and populates the preference store with sensible
// default values so the plugin works correctly the first time it launches, before the
// user has touched a single setting.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Populates the plugin's preference store with factory defaults the very first
 * time the workbench starts up (or whenever a preference key has no stored value).
 * Eclipse calls {@link #initializeDefaultPreferences()} automatically via the
 * {@code org.eclipse.core.runtime.preferences} extension point.
 * Think of this as Palpatine issuing standing orders: they kick in immediately and
 * remain in force until a user explicitly overrides them in Preferences.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    // ── INITIALIZE DEFAULTS: PALPATINE SIGNS THE STANDING ORDERS ────────────────
    // Palpatine puts his seal on a stack of standing orders: "By default, patrol
    // routes use the standard formation, all units report by object class, and only
    // entries with a matching template use the template editor." No commander needs
    // to ask — the defaults are already in force from day one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers default values for every preference key this plugin owns.
     * Called once by Eclipse at startup before any user preferences are loaded.
     * If a user has never touched a setting, they'll silently get these values.
     *
     * <p>For example — Palpatine signs the default patrol orders:</p>
     * <pre>
     *   "All units: default to object-class presentation mode."
     *   "Template editor activates only for entries that have a matching template."
     *   "Import dialog opens in the user's home directory."
     *   initializeDefaultPreferences() stamps all three defaults into the store.
     * </pre>
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = EntryTemplatePlugin.getDefault().getPreferenceStore();

        // Preferences
        store.setDefault( EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION,
            EntryTemplatePluginConstants.PREF_TEMPLATES_PRESENTATION_OBJECT_CLASS );
        store.setDefault( EntryTemplatePluginConstants.PREF_DISABLED_TEMPLATES, "" ); //$NON-NLS-1$
        store.setDefault( EntryTemplatePluginConstants.PREF_USE_TEMPLATE_EDITOR_FOR,
            EntryTemplatePluginConstants.PREF_USE_TEMPLATE_EDITOR_FOR_ENTRIES_WITH_TEMPLATE );

        // Dialogs
        store.setDefault( EntryTemplatePluginConstants.DIALOG_IMPORT_TEMPLATES, System.getProperty( "user.home" ) ); //$NON-NLS-1$
    }
}
