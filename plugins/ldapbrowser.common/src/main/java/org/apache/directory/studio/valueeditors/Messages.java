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
package org.apache.directory.studio.valueeditors;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATES BETWEEN SPECIES ───────────────────────
// Aboard the Millennium Falcon, C-3PO stands ready at the communications
// console, fluent in over six million forms of communication. Any time the
// value editors need to show a human-readable string — a dialog title, a label,
// an error message — they hand C-3PO a key and he returns the right phrase in
// the current locale. If the phrase isn't in his translation database, he wraps
// the key in exclamation marks — his polite way of admitting a gap.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides localised string resources for the {@code valueeditors} package. All
 * user-visible strings live in the accompanying {@code messages.properties}
 * file; this class is the single gateway for fetching them by key at runtime.
 * Think of this class as C-3PO consulting his translation matrix: hand him a
 * key and he returns the right phrase in the current locale, or {@code !key!}
 * if the translation is missing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO DELIVERS A TRANSLATION BY KEY ──────────────────────────────────
    // A crew member hands C-3PO a phrase identifier. He scans his translation
    // matrix and returns the localised string. If the identifier is absent from
    // the matrix — perhaps because the properties file is out of sync — he
    // wraps it in exclamation marks so developers spot the gap immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the localised string for the given {@code key} in the
     * {@code messages.properties} resource bundle. If no entry is found, we
     * return {@code !key!} so the missing translation is immediately visible in
     * the UI rather than silently absent or replaced with an exception stack
     * trace.
     *
     * @param key  the message key, matching an entry in {@code messages.properties}
     * @return     the localised message, or {@code !key!} if the key is absent
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
