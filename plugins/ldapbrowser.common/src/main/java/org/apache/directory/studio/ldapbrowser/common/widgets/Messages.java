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
package org.apache.directory.studio.ldapbrowser.common.widgets;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO DECODING R2'S BEEPS INTO BASIC ───────────────────
// C-3PO stands at Obi-Wan's side and translates every bleep and whistle R2 produces
// into clean, human-readable protocol language — that's exactly what this class does.
// Given a key (the bleep), getString() looks it up in the resource bundle (C-3PO's memory)
// and returns the localized string (the translation). If the key is unknown, he wraps
// it in exclamation marks so you know something went wrong.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility class for retrieving localized UI strings from this package's
 * {@code messages.properties} resource bundle.
 * Every widget in this package uses {@code Messages.getString("SomeKey")} instead of
 * hard-coding English text, so labels and error messages can be translated without
 * touching the Java source.
 * Think of this as C-3PO: he holds the translation table, speaks when called upon,
 * and politely flags any key he doesn't recognize with {@code !key!}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO TRANSLATES AN R2 BLEEP INTO WORDS ───────────────────────────────────
    // R2 beeps a code ("BinaryEncodingInput.Ignore") and C-3PO immediately produces
    // the translation: "Ignore" — clean, readable, ready for the UI label.
    // If R2 bleeps an unrecognized code, C-3PO wraps it in "!" as a distress signal.
    // We look up the key in RESOURCE_BUNDLE and return the value, or "!key!" on miss.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localized string by its message key from this package's resource bundle.
     * If the key exists, the corresponding translated string is returned. If it's missing
     * (probably a typo or a bundle that isn't up to date), the key itself is returned
     * wrapped in exclamation marks — e.g. {@code "!BinaryEncodingInput.Ignore!"} — so
     * the problem is obvious in the UI rather than silently swallowed.
     *
     * <p>For example — C-3PO translates R2's bleep:</p>
     * <pre>
     *   Messages.getString("BinaryEncodingInput.Ignore") → "Ignore"
     *   Messages.getString("Missing.Key")               → "!Missing.Key!"
     * </pre>
     *
     * @param key  The message key defined in {@code messages.properties}; must not be {@code null}.
     * @return     The localized string for the key, or {@code "!" + key + "!"} if the key is missing.
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
