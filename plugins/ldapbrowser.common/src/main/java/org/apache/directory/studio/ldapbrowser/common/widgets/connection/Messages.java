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
package org.apache.directory.studio.ldapbrowser.common.widgets.connection;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating Between Wookiee And Basic ────────────────────────
// C-3PO stands on the bridge of the Falcon, fluent in over six million forms of communication.
// Han tosses him a raw code key ("BrowserParameterPage.FetchBaseDNs"), and Threepio instantly
// retrieves the human-readable phrase from his translation database.
// If the key is unknown, he politely wraps it in exclamation marks rather than crashing the ship.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Thin i18n helper that looks up localised strings for the {@code connection} widget package.
 * Every translatable label, tooltip, and error message in this package is stored in a
 * {@code messages.properties} file next to this class, keyed by a string identifier.
 * Think of this class as C-3PO: you hand him a key, he hands back the translation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Opens His Translation Database ─────────────────────────────────────────────
    // Threepio's knowledge base is loaded once when the class is initialised and shared
    // by everyone who calls him.  No sense in each crew member carrying their own copy
    // of the six-million-language dictionary.
    // The private constructor ensures nobody accidentally instantiates Threepio — he's a
    // singleton service, not something you clone.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this class is a pure utility class with only static methods.
     * Instantiating it would be like giving C-3PO a twin: unnecessary and confusing.
     * All callers use {@link #getString(String)} directly on the class.
     */
    private Messages()
    {
        // utility class — do not instantiate
    }


    // ── C-3PO Looks Up A Translation ──────────────────────────────────────────────────────
    // Han shouts a code word across the cockpit; Threepio consults his memory banks and
    // calls back the translated phrase in Standard Basic so everyone understands.
    // If Han fumbles and says a word Threepio doesn't recognise, Threepio wraps it in
    // "!...!" rather than going silent — at least you know something went wrong.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given resource key, looked up from the
     * {@code messages.properties} bundle next to this class.
     * If the key is missing from the bundle (a bug — someone forgot to add a translation),
     * we return {@code "!" + key + "!"} as a visible sentinel rather than throwing an exception
     * — C-3PO says "I don't know that one, sir" rather than shutting down.
     *
     * <p>For example — Han requests a label:</p>
     * <pre>
     *   Messages.getString( "BrowserParameterPage.FetchBaseDNs" )
     *   // → "Fetch Base DNs"  (from messages.properties)
     *
     *   Messages.getString( "UnknownKey" )
     *   // → "!UnknownKey!"   (missing key sentinel)
     * </pre>
     *
     * @param key  the resource bundle key to look up; must not be {@code null}
     * @return the translated string, or {@code "!" + key + "!"} if the key is not found
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
