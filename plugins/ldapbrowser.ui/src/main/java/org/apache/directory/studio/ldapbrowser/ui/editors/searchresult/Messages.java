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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Reading the Jawa Dialect ─────────────────────────
// On Tatooine, C-3PO stands between Luke and the Jawas, translating their
// rapid-fire beeping and clicking into plain English that Luke can act on.
// He doesn't make up the translation — he looks it up in his language database
// and returns exactly what the source says.
// This class is that translation layer: given a key, look it up in the
// messages.properties bundle and return the human-readable string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for looking up localized message strings in this package's
 * resource bundle ({@code messages.properties}).
 * Every user-visible string in the search result editor goes through here so
 * we can localize the UI without scattering hard-coded English strings everywhere.
 * Think of this class as C-3PO: fluent in over six million forms of communication,
 * always ready to translate a key into the right phrase.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Looks Up the Phrase ─────────────────────────────────────────────
    // The Jawa shrieks something unintelligible; C-3PO consults his language
    // matrix and returns "They want thirty credits for the R2 unit, Master Luke."
    // If the phrase isn't in the matrix, he flags it clearly: "!key!" so we know
    // the translation is missing and can fix the bundle.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localized string by key from this package's resource bundle.
     * If the key is missing we return {@code "!key!"} so the missing translation
     * stands out visually in the UI instead of crashing or showing nothing.
     *
     * <p>For example — C-3PO looks up a phrase for Luke:</p>
     * <pre>
     *   getString("ShowDNAction.ShowDN")
     *   → "Show DN"   (found in messages.properties)
     *
     *   getString("no.such.key")
     *   → "!no.such.key!"   (missing — flagged for the developer)
     * </pre>
     *
     * @param key  the message key defined in {@code messages.properties}; must not be null
     * @return     the localized string for {@code key}, or {@code "!key!"} if the key is absent
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
