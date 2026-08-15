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
package org.apache.directory.studio.schemaeditor.view.views;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Reading the Jawa Dialect ─────────────────────────
// On Tatooine, a band of Jawas babbles in their own cryptic dialect — all
// clicks and buzzes that mean nothing to Luke or Obi-Wan. C-3PO steps in
// and translates each utterance into something a human can actually act on.
// This class does exactly that: our .properties file speaks in opaque message
// keys, and Messages.getString() translates each key into the localized
// string the UI can display to the user.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Loads and vends localized UI strings from this package's {@code messages.properties} file.
 * We keep all user-visible text in that file so translators can swap in new locales without
 * touching a single line of Java. Think of this class as C-3PO: fluent in over six million
 * forms of communication — or at least one .properties file.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Translates a Single Jawa Utterance ────────────────────────────
    // The Jawas gesture and squeak; Luke has no idea what they're saying.
    // C-3PO listens, cross-references the dialect in his memory banks, and
    // delivers the English equivalent calmly.
    // We hand this method a property key (the Jawa squeak) and get back the
    // readable UI string (the translation) — or a bracketed fallback if the
    // key is missing so we know something went wrong.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by its property key and returns the localized value.
     * If the key doesn't exist in the bundle — maybe a typo, maybe a missing translation —
     * we return {@code !key!} instead of crashing, which makes the missing key very
     * obvious in the UI so it gets fixed quickly.
     *
     * <p>For example — C-3PO handles an unknown Jawa phrase:</p>
     * <pre>
     *   Luke:     "What did it say?"
     *   C-3PO:    "I'm not sure, sir — it appears the word is unregistered."
     *   C-3PO:    Returns "!unknownKey!" so you notice the gap immediately.
     * </pre>
     *
     * @param key  the property key from messages.properties, e.g. {@code "ProblemsView.Error"}
     * @return     the localized string for that key, or {@code !key!} if the key is missing
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
