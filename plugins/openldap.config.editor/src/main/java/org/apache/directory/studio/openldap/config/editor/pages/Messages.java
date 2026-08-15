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
package org.apache.directory.studio.openldap.config.editor.pages;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Reading the Jawa Dialect ────────────────────────
// On Tatooine, C-3PO encounters a Jawa who speaks in a fast, cryptic dialect.
// C-3PO reaches into his linguistic database, looks up the right phrase bundle
// for that dialect, and translates the key into something meaningful for the
// rebels around him.  This utility class does the same thing for our editor
// pages: it takes a message key (like "LoadingPage.LoadingConfiguration"),
// looks it up in the Java ResourceBundle for this package, and returns the
// human-readable string — or wraps the key in "!" markers so a developer
// immediately spots a missing translation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A static utility class that retrieves localized strings for the OpenLDAP
 * editor pages from the {@code messages.properties} resource bundle.
 * All UI labels, titles, and messages for the pages package live in that
 * bundle; we look them up here so the strings stay out of the Java source.
 * Think of it as C-3PO translating Jawa phrases on demand — give us a key,
 * we give you the right human-readable sentence.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    // ── Private Constructor — C-3PO Can't Be Cloned ───────────────────────────
    // C-3PO is a one-of-a-kind protocol droid; you wouldn't want two of him
    // roaming the same ship arguing about which message bundle is authoritative.
    // This class is pure static utility — instantiation is pointless.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this class is a static utility class and must not
     * be instantiated.
     */
    private Messages()
    {
        //Nothing to do
    }


    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── getString — C-3PO Translates the Key ─────────────────────────────────
    // C-3PO is handed a Jawa phrase key; he flips open the linguistic scroll
    // for this particular Jawa clan, finds the corresponding phrase, and reads
    // it aloud.  If the key doesn't exist in the bundle he wraps it in
    // exclamation marks so everyone knows something is missing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a message string from the pages resource bundle by key.
     * If the key is missing (common during development when a new key hasn't
     * been added to messages.properties yet) we return {@code !key!} so the
     * broken string is immediately visible in the UI rather than silently null.
     *
     * <p>For example — C-3PO looks up the phrase:</p>
     * <pre>
     *   getString("LoadingPage.LoadingConfiguration")
     *   → "Loading Configuration..."
     *
     *   getString("NonExistent.Key")
     *   → "!NonExistent.Key!"   // obvious red flag in the UI
     * </pre>
     *
     * @param key  the resource bundle key for the message — must not be null
     * @return     the localized string, or {@code !key!} if the key is missing
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
