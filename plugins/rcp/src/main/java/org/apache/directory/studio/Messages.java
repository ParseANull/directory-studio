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

package org.apache.directory.studio;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating for the Rebel Alliance ───────────────
// C-3PO stands on the Yavin base deck, fluent in over six million forms of
// communication, translating R2's beeps and the Rebel commanders' jargon into
// plain Galactic Basic so everyone understands the mission briefing.
// We do the same here — every UI string lives in a .properties file and this
// class fetches the human-readable text so our code never has raw strings in it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Centralises all UI string lookups for the rcp plugin.
 * Rather than scattering literal strings through the code, we keep every
 * user-visible label, title, and message in a .properties file and fetch
 * them through this helper. That makes translation and copy-editing trivial.
 * Think of this class as C-3PO — it speaks human so our code doesn't have to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── C-3PO Goes Offline — Nobody Asks a Droid to Socialise ────────────────
    // C-3PO is designed to translate, not to be instantiated like a person.
    // Similarly, every method on this class is static — there's no reason to
    // create an instance, and this private constructor makes that perfectly clear.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Prevents instantiation — all methods here are static utilities.
     * We make the constructor private to signal that this is a pure
     * utility class; just call {@link #getString(String)} directly.
     */
    private Messages()
    {
    }


    // ── C-3PO Delivers the Translation ───────────────────────────────────────
    // A Rebel officer shouts a question at C-3PO; the droid consults his
    // encyclopaedic memory banks and returns the answer in plain Basic.
    // If the key doesn't exist C-3PO says "I'm afraid I don't know that one"
    // — we wrap the missing key in exclamation marks so it stands out in the UI.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the localised string for the given message key.
     * We use a .properties resource bundle so every visible label is in one
     * place — if the key is missing we return {@code !key!} rather than
     * crashing, which keeps the UI running even with an incomplete bundle.
     *
     * <p>For example — C-3PO receives the officer's question and replies:</p>
     * <pre>
     *   officer.ask("ShutdownPreferencesPage.PageTitle")
     *   → C-3PO checks his databanks
     *   → returns "Shutdown" in whatever language the locale demands
     * </pre>
     *
     * @param key  the message key defined in messages.properties — typically
     *             in the form {@code ClassName.ShortDescription}.
     * @return     the localised string, or {@code !key!} if no mapping exists.
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
