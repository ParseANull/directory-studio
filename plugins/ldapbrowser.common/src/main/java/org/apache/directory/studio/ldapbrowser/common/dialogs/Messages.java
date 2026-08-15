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

package org.apache.directory.studio.ldapbrowser.common.dialogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING BETWEEN SPECIES ──────────────────────
// C-3PO is fluent in over six million forms of communication and acts as the
// galaxy's universal translator: you hand him a concept key ("greet the Wookiee")
// and he looks up the right phrase in his memory banks, returning the exact words
// that the audience will understand.  If he doesn't have a translation he wraps
// the key in exclamation marks so everyone knows something went wrong.
// This class does exactly the same thing for UI strings: you hand it a key
// (like "DeleteDialog.UseTreeDeleteControl") and it returns the localised
// human-readable string from the {@code messages.properties} resource bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static helper for retrieving localised UI strings from the
 * {@code messages.properties} resource bundle in this package.  Every dialog
 * and widget in this package uses this class instead of hard-coding English text
 * directly in the source, so the strings can be translated without touching Java
 * code.
 * Think of this class as C-3PO: hand it a key, get back the right phrase in the
 * right language — and if the phrase is missing it wraps the key in {@code !…!}
 * so you can spot the gap immediately.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO TRANSLATES A KEY INTO THE LOCAL DIALECT ────────────────────────
    // A Rebel soldier hands C-3PO a reference card: "key = DeleteConfirmTitle".
    // C-3PO flips through his memory banks, finds the corresponding phrase in the
    // current locale, and speaks it aloud.  If the phrase is missing from his
    // records he says "!DeleteConfirmTitle!" so no one silently receives nothing.
    // We look up the key in the ResourceBundle and return the localised string, or
    // wrap the key in '!' markers if it is absent — helpful during development.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the localised string for the given key in this package's
     * {@code messages.properties} resource bundle.  If the key is missing —
     * for example because a translation was not yet added — we return the key
     * wrapped in exclamation marks ({@code !key!}) so the gap is immediately
     * visible in the UI rather than silently showing nothing.
     *
     * <p>For example — C-3PO translates on demand:</p>
     * <pre>
     *   Messages.getString("DeleteDialog.UseTreeDeleteControl")
     *   // → "Use Tree Delete Control"  (from messages.properties)
     *
     *   Messages.getString("Missing.Key")
     *   // → "!Missing.Key!"  (key not found — C-3PO flags it)
     * </pre>
     *
     * @param key  the message key as defined in {@code messages.properties}; must not be {@code null}
     * @return     the localised string, or {@code !key!} if the key has no mapping
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
