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

package org.apache.directory.studio.schemaeditor.view;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FOR R2-D2 ────────────────────────────
// In the Mos Eisley cantina, C-3PO leans over to R2-D2 and listens patiently
// to a stream of beeps and whistles, then converts each one into plain Basic
// that the humans at the table can actually understand.
// We do the same here: the UI hands us a raw key string, and we look it up in
// the properties file to get back the human-readable text that belongs on screen.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Translates resource-bundle keys into human-readable UI strings for the
 * {@code view} package, loading them from the accompanying
 * {@code messages.properties} file at class-load time.
 * Think of this class as C-3PO: every time a UI component needs to say
 * something to the user, it hands us a cryptic key and we hand back the
 * right words in the right language.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── R2 BEEPS; C-3PO TRANSLATES ───────────────────────────────────────────
    // R2-D2 pipes in a compact beep-sequence representing an urgent message.
    // C-3PO cross-references his six-million-language database and returns the
    // human-readable translation on the spot.
    // When C-3PO can't find the key — say, it's a typo or the properties file
    // is out of sync — he wraps it in exclamation marks so everyone nearby knows
    // the translation failed: "!Unknown.Key!" style.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a display string in the {@code messages.properties} file by its
     * resource key and returns it to the caller.
     * We need this so that all user-visible text lives in one properties file
     * instead of being scattered as hard-coded literals throughout the code.
     * If the key doesn't exist — maybe someone renamed it and forgot to update
     * the properties file — we return {@code !key!} so the mistake is
     * immediately visible in the UI rather than silently swallowed.
     *
     * <p>For example — C-3PO encounters an unrecognised beep sequence from R2:</p>
     * <pre>
     *   R2 transmits key: "ViewUtils.AllowedCharacters"
     *   C-3PO finds the match and returns: "[a-zA-Z][a-zA-Z0-9-]*"
     *
     *   R2 transmits key: "ViewUtils.TotallyMadeUp"
     *   C-3PO finds nothing — returns: "!ViewUtils.TotallyMadeUp!"
     * </pre>
     *
     * @param key  the resource-bundle key to look up, e.g. {@code "ViewUtils.AllowedCharacters"}
     * @return     the localised string for that key, or {@code !key!} if no mapping exists
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
