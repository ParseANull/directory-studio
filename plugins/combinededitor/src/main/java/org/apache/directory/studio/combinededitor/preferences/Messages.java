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
package org.apache.directory.studio.combinededitor.preferences;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Relays Preference Screen Labels ──────────────────
// When the Rebel crew needs to configure mission parameters, C-3PO reads out
// the correct label for each setting from the ship's multilingual phrase book —
// "Default Editor:", "Auto Switch:", "Template Editor", "LDIF Editor", and so on.
// This Messages class is that phrase book for the preferences package: it
// looks up UI strings from the preferences message bundle so all preference
// page labels are externalized and translatable.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides localised string lookups for the combinededitor preferences UI.
 * All user-visible strings on the {@link CombinedEntryEditorPreferencePage}
 * are fetched through {@link #getString(String)} rather than being hardcoded,
 * which makes them easy to translate or override.
 * Think of this as C-3PO's phrase book for the preferences package — the right
 * label, in the right language, on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.combinededitor.preferences.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── C-3PO Stays Off the Bridge — No Instances Needed ─────────────────────
    // C-3PO is a singleton protocol droid: there's only one of him and you call
    // his methods directly — you don't clone him.  The private constructor
    // prevents anyone from creating an instance of this utility class.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a static utility class; do not instantiate.
     */
    private Messages()
    {
    }


    // ── C-3PO Translates a Key into a Human-Readable Label ───────────────────
    // The crew hands C-3PO a message key and he looks it up in the phrase book,
    // returning the localised string.  If the key is missing he returns a
    // clearly-marked placeholder so the developer immediately spots the gap.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given message key.
     * We look up {@code key} in the preferences resource bundle.
     * If the key is missing — for example during development when a new string
     * hasn't been added yet — we return {@code !key!} so the gap is obvious
     * in the UI rather than causing a crash.
     *
     * @param key  the message bundle key, e.g. {@code "CombinedEntryEditorPreferencePage.TableEditor"}.
     * @return     the localised string, or {@code !key!} if the key is not found.
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
