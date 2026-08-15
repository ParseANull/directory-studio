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
package org.apache.directory.studio.ldapbrowser.common.wizards;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FOR THE REBELLION ────────────────────
// C-3PO stands at the heart of every diplomatic exchange, fluent in over six
// million forms of communication — whenever the Rebels need a phrase rendered
// in Basic, they hand him a concept and he returns the perfectly worded sentence.
// This class plays exactly that role: given a raw bundle key (the "alien word"),
// it looks up and returns the human-readable localised string from
// messages.properties so every wizard label speaks the user's language.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for fetching localised strings from the wizard resource bundle.
 * Every user-visible label, title, and description in this package flows through
 * here so that translations live in one central place and swapping locales
 * requires no code changes whatsoever.
 * Think of this class as C-3PO at a diplomatic reception — you hand it a code
 * word and it comes back with the right phrase in the right language.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Retrieves the Exact Phrase ─────────────────────────────────────
    // C-3PO listens to a cryptic request from Han, digs into his vast linguistic
    // database, and returns a perfectly formed sentence in standard Basic.
    // If that word isn't in his memory banks, he wraps it in exclamation marks
    // so everyone on the bridge knows immediately that something is missing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localised string by its bundle key from messages.properties.
     * If the key doesn't exist we return {@code !key!} as a visible sentinel —
     * that way a missing translation shows up obviously in the UI rather than
     * silently rendering as a blank label.
     *
     * <p>For example — C-3PO translates a Wookiee grunt for the rest of the crew:</p>
     * <pre>
     *   Messages.getString("AttributeWizard.NewAttribute")
     *   // → "New Attribute"  (entry found in messages.properties)
     *
     *   Messages.getString("NoSuchKey")
     *   // → "!NoSuchKey!"   (key absent — sentinel returned)
     * </pre>
     *
     * @param key  The bundle key for the string you want; it must match an
     *             entry in messages.properties or you get the {@code !key!}
     *             sentinel back so the gap is impossible to miss.
     * @return     The localised string, or {@code !key!} if the key is absent.
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
