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

package org.apache.directory.studio.ldapbrowser.common.filtereditor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATES BETWEEN SPECIES ───────────────────────
// Aboard the Tantive IV, C-3PO stands at the communications console, fluent in
// over six million forms of communication. Any time a crew member needs to
// speak to someone in another language, C-3PO looks up the right phrase from
// his translation database and delivers it. If the phrase is missing from his
// database, he wraps the key in exclamation marks — his polite way of saying
// "I have no translation for that."
// We do exactly the same: look up localised UI strings from a properties file
// by key, and return {@code !key!} if the key is absent.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides localised string resources for the {@code filtereditor} package.
 * All user-visible strings live in the accompanying {@code messages.properties}
 * file; this class is the single gateway for fetching them by key at runtime.
 * Think of this class as C-3PO consulting his translation database: give him a
 * key and he returns the right phrase in the current locale.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO TRANSLATES A PHRASE BY KEY ─────────────────────────────────────
    // A crew member hands C-3PO a phrase identifier. C-3PO flips through his
    // translation database and returns the localised phrase. If the identifier
    // is not in his database, he wraps it in exclamation marks — "!key!" — so
    // the engineers can spot the missing translation immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the localised string for the given {@code key} in the
     * {@code messages.properties} resource bundle. If no entry is found — for
     * example, because the properties file is out of sync with the code — we
     * return {@code !key!} so the missing translation is immediately visible in
     * the UI rather than silently empty.
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
