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

package org.apache.directory.studio.ldapservers.actions;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING IMPERIAL COMMUNIQUÉS ─────────────────────────────────
// C-3PO stands in the Rebel briefing room, fluent in over six million forms of communication,
// ready to translate any Imperial dispatch into plain Galactic Basic on demand.
// When someone shouts a message key at him he dives into his memory banks (the .properties
// bundle) and returns the human-readable string — and if the key is nowhere to be found
// he wraps it in exclamation marks so everyone knows the translation failed.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * NLS (National Language Support) helper that looks up localised strings for the
 * {@code ldapservers.actions} package.  Every user-visible label, tooltip, and dialog
 * message in this package is stored as a key-value pair inside a {@code messages.properties}
 * file; this class is the single door through which all of that text flows.
 * Think of this class as C-3PO — hand him a key and he'll retrieve the right phrase
 * from his six-million-form memory bank.  Hand him a key he doesn't recognise and he'll
 * wrap it in {@code !exclamation marks!} to flag the gap.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO RETRIEVES THE TRANSLATION FROM HIS MEMORY BANKS ───────────────────────────────
    // C-3PO cross-references the incoming message key against his vast linguistic database
    // and produces the correct Galactic Basic phrase in an instant.
    // If the key is not in his memory — perhaps from an obscure dialect — he reports the
    // failure clearly by surrounding the unknown key with exclamation marks.
    // We do the same: look up the key in the resource bundle, and if it's missing, return
    // '!key!' so the developer immediately spots the untranslated entry.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up and returns the localised string associated with the given key.
     * The string comes from the {@code messages.properties} file bundled alongside
     * this class.  If the key doesn't exist in the bundle (a sign that someone forgot
     * to add the translation), we return {@code '!' + key + '!'} so it shows up
     * visibly wrong in the UI and is easy to find and fix.
     *
     * <p>For example — C-3PO handles a translation request:</p>
     * <pre>
     *   String label = threepio.translate( "DeleteAction.Delete" );
     *   // returns "Delete" if the key exists in messages.properties
     *   // returns "!DeleteAction.Delete!" if threepio has no record of it
     * </pre>
     *
     * @param key  The message key to look up — e.g. {@code "DeleteAction.Delete"}.
     *             Think of it as the Imperial communiqué code C-3PO needs to decode.
     * @return     The localised human-readable string for that key, or {@code '!key!'}
     *             if the key is not found in the resource bundle.
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
