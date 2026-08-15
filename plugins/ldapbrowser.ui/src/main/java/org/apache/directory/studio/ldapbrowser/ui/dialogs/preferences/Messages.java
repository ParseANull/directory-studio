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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.preferences;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING PREFERENCE PAGE LABELS ───────────────
// C-3PO's translation modules cover every label and description that appears
// on a preference page, from "Enable modification logging" to "Log file count."
// Rather than scattering raw strings through preference page code, we centralize
// them here so translators only have to touch one .properties file.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS message bundle accessor for the ldapbrowser.ui dialogs/preferences package.
 * Loads the sibling {@code messages.properties} file and lets preference page
 * classes call {@code Messages.getString("some.key")} for all user-visible text.
 * Think of this class as C-3PO's preference-page translation subsystem — labels,
 * tooltips, and descriptions keyed and localized in one place.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO RETRIEVES A PREFERENCE LABEL ────────────────────────────────────
    // C-3PO is asked to translate "log file rotation settings" into something a
    // human can read on a preference page; he checks the archive and returns the
    // right phrasing without exposing raw key names in the UI.
    // We do the same: return the localized string or a visible !key! sentinel
    // so missing translations are immediately obvious during development.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by key in the bundled messages.properties file.
     * Missing keys come back as {@code !key!} rather than throwing an exception,
     * making translation gaps visible without crashing the preference page.
     *
     * <p>For example — C-3PO translates a preference label:</p>
     * <pre>
     *   getString("ModificationLogsPreferencePage.EnableModificationLogs")
     *       → "Enable modification logging"
     * </pre>
     *
     * @param key  The message key defined in messages.properties; never null.
     * @return     The localized string, or {@code !key!} if the key is missing.
     */
    public static String getString( String key )
    {
        try
        {
            return RESOURCE_BUNDLE.getString( key );
        }
        catch ( MissingResourceException e )
        {
            return '!' + key + '!';
        }
    }
}
