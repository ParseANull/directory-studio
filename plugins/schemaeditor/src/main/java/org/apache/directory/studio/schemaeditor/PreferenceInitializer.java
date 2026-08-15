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
package org.apache.directory.studio.schemaeditor;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


// ── CLASS: PreferenceInitializer — Palpatine Broadcasts Order 66 ─────────────
// From his throne on Coruscant, Palpatine issues a single galaxy-wide command
// that instantly configures every clone trooper with the same set of orders —
// no individual negotiation, no defaults left to chance. Our initializeDefaultPreferences
// is that transmission: one call, and every view in the plugin knows exactly how
// to present itself the very first time the user opens it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Sets the out-of-the-box default values for every Schema Editor preference key
 * when the plugin is installed for the first time on a workspace. Eclipse calls
 * this exactly once via the {@code org.eclipse.core.runtime.preferences} extension
 * point, before any preference page has been shown. Think of it as Palpatine's Order
 * 66: one broadcast that simultaneously configures the entire system so there are
 * never undefined defaults.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    // ── Palpatine Issues The Order — Every Trooper Configured At Once ─────────────
    // The holographic transmission goes out across the galaxy: every clone receives
    // their standing orders simultaneously, with no room for ambiguity. Every
    // preference key in the Schema Editor gets its factory default here — sort order,
    // label style, abbreviation limits, file dialog start paths — so there is always
    // a sensible value even if the user has never opened the preference pages.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes default values for all Schema Editor preferences into the plugin's preference
     * store. Eclipse guarantees this is called before anyone reads these preferences for
     * the first time, so views and dialogs always get a defined value from the store even
     * on a fresh workspace. If a user has already saved a value for any key, that stored
     * value takes precedence — the defaults set here are only used when no explicit choice
     * has been made.
     *
     * <p>For example — Palpatine's standing orders wire every view:</p>
     * <pre>
     *   Schema View    → flat presentation, all aliases, max 50 chars, OID secondary label
     *   Hierarchy View → all aliases, max 50 chars, OID secondary label
     *   Search View    → all aliases, mixed grouping, ascending sort
     *   File dialogs   → open at user home directory
     * </pre>
     */
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        // DifferencesWidget
        store.setDefault( PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING,
            PluginConstants.PREFS_DIFFERENCES_WIDGET_GROUPING_PROPERTY );

        // SchemaView Preference Page
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION,
            PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION_FLAT );
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_LABEL, PluginConstants.PREFS_SCHEMA_VIEW_LABEL_ALL_ALIASES );
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE, true );
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE_MAX_LENGTH, "50" ); //$NON-NLS-1$
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY, true );
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL,
            PluginConstants.PREFS_SCHEMA_VIEW_LABEL_OID );
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE, false );
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH, "50" ); //$NON-NLS-1$
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_LABEL_DISPLAY, false );

        // SchemaView Sorting
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING,
            PluginConstants.PREFS_SCHEMA_VIEW_GROUPING_FOLDERS );
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY,
            PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY_FIRSTNAME );
        store.setDefault( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER,
            PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER_ASCENDING );

        // HierarchyView Preference Page
        store.setDefault( PluginConstants.PREFS_HIERARCHY_VIEW_LABEL,
            PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_ALL_ALIASES );
        store.setDefault( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE, true );
        store.setDefault( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE_MAX_LENGTH, "50" ); //$NON-NLS-1$
        store.setDefault( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_DISPLAY, true );
        store.setDefault( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL,
            PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_OID );
        store.setDefault( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE, false );
        store.setDefault( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH, "50" ); //$NON-NLS-1$

        // SearchView Preference Page
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_LABEL, PluginConstants.PREFS_SEARCH_VIEW_LABEL_ALL_ALIASES );
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE, true );
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE_MAX_LENGTH, "50" ); //$NON-NLS-1$
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_DISPLAY, true );
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL,
            PluginConstants.PREFS_SEARCH_VIEW_LABEL_OID );
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE, false );
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH, "50" ); //$NON-NLS-1$
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_SCHEMA_LABEL_DISPLAY, true );

        // SearchView Sorting
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_GROUPING, PluginConstants.PREFS_SEARCH_VIEW_GROUPING_MIXED );
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY,
            PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_FIRSTNAME );
        store.setDefault( PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER,
            PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER_ASCENDING );

        // File Dialogs
        store.setDefault( PluginConstants.FILE_DIALOG_EXPORT_PROJECTS, System.getProperty( "user.home" ) ); //$NON-NLS-1$
        store.setDefault( PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_OPENLDAP, System.getProperty( "user.home" ) ); //$NON-NLS-1$
        store.setDefault( PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_XML, System.getProperty( "user.home" ) ); //$NON-NLS-1$
        store.setDefault( PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_APACHE_DS, System.getProperty( "user.home" ) ); //$NON-NLS-1$
        store.setDefault( PluginConstants.FILE_DIALOG_IMPORT_PROJECTS, System.getProperty( "user.home" ) ); //$NON-NLS-1$
        store.setDefault( PluginConstants.FILE_DIALOG_IMPORT_SCHEMAS_OPENLDAP, System.getProperty( "user.home" ) ); //$NON-NLS-1$
        store.setDefault( PluginConstants.FILE_DIALOG_IMPORT_SCHEMAS_XML, System.getProperty( "user.home" ) ); //$NON-NLS-1$
    }
}
