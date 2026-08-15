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


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING JAWA DIALECT ─────────────────────────
// C-3PO is fluent in over six million forms of communication; when Luke and Uncle
// Owen need to understand the Jawas arguing over the droids, C-3PO steps in and
// reads the Jawa dialect straight from a dictionary.
// Messages does exactly that: it reads localized strings from a properties file
// (the "Jawa dialect dictionary") so the rest of the code never has to hardcode
// user-visible text — just ask C-3PO by key and he hands back the translation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A static utility class for retrieving localized strings for the entry editor package.
 * All user-visible text in this package (button labels, error messages, menu items) is
 * stored in a {@code messages.properties} file alongside this class; callers look up
 * strings by key rather than hardcoding them.
 * Think of this as C-3PO with his Jawa phrasebook: give him a key, he returns the right phrase.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO OPENS HIS PHRASEBOOK TO THE RIGHT PAGE ─────────────────────────
    // C-3PO rifles through his Jawa phrasebook, finds the right entry, and reads
    // the translation aloud — if the page is missing, he says "!key!" to signal
    // that the dictionary doesn't have a translation for what was asked.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given message key.
     * Looks up the key in the package's {@code messages.properties} resource bundle.
     * If the key doesn't exist (e.g., a typo or a missing translation), we return
     * {@code !key!} so the missing string is visible rather than silently empty.
     *
     * <p>For example — C-3PO translating:</p>
     * <pre>
     *   Messages.getString("ToggleAutosaveAction.Autosave"); // returns "Autosave"
     *   Messages.getString("NonExistentKey");               // returns "!NonExistentKey!"
     * </pre>
     *
     * @param key  The resource bundle key for the desired string; must not be {@code null}.
     * @return the localized string, or {@code !key!} if the key is not found.
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
