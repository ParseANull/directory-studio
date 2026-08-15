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
package org.apache.directory.studio.ldapbrowser.common.actions;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING AT THE TANTIVE IV COMMS CONSOLE ──────
// C-3PO is fluent in over six million forms of communication. Whenever the crew
// needs to understand something in an alien language — a Huttese phrase, a
// Wookiee growl, a Bocce trade term — they ask C-3PO. He looks up the phrase
// in his internal linguistic database and returns the correct translation.
// This Messages class does exactly the same thing: it holds a reference to the
// Java ResourceBundle (the linguistic database), and any class in this package
// that needs a localized string asks for it by key. The key goes in, the
// translated string comes out. If the key is missing, C-3PO delivers a
// conspicuously wrapped error marker ("!key!") rather than crashing the mission.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides access to the NLS (National Language Support) message bundle for
 * the {@code ldapbrowser.common.actions} package. Every user-visible string
 * in this package — menu labels, dialog titles, warning messages — lives in
 * a {@code messages.properties} file and is fetched by key through this class.
 * If a key is missing from the bundle (e.g., during development), the method
 * returns {@code "!key!"} so the problem is immediately obvious without
 * crashing the application.
 * Think of this class as C-3PO: the universal translator that every action
 * class calls when it needs to say something to the user.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO LOOKS UP THE PHRASE IN HIS DATABASE ─────────────────────────────
    // Given a key (the alien-language phrase), C-3PO searches his linguistic
    // database and returns the localized translation. If the phrase is unknown,
    // he wraps the key in exclamation marks so the crew knows immediately that
    // the translation is missing — rather than returning an empty string that
    // would silently hide the problem.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localized string by key in the actions message bundle.
     * Returns the translated string if found, or {@code "!key!"} if the key
     * is missing — making missing translations visible without throwing exceptions.
     *
     * <p>For example — C-3PO translating a Huttese phrase for Han:</p>
     * <pre>
     *   String label = Messages.getString( "CopyAction.CopyEntryDN" );
     *   // returns "Copy Entry DN" in English, or the locale's equivalent
     *   // returns "!CopyAction.CopyEntryDN!" if the key is missing
     * </pre>
     *
     * @param key  the message bundle key to look up; must not be null.
     * @return the localized string for {@code key}, or {@code "!" + key + "!"}
     *         if {@code key} is not found in the bundle.
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
