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

package org.apache.directory.studio.ldapservers.dialogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S DIALOGS TRANSLATION MODULE ────────────────────────────────────
// C-3PO carries a specialized translation module for diplomatic exchanges — given a phrase
// code, he returns the correct words for the current locale without the caller needing to
// know anything about language files.
// This is the NLS (Native Language Support) helper for the {@code dialogs} sub-package: it
// loads the local messages.properties bundle and exposes a safe lookup method.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Loads and exposes localized strings for the {@code ldapservers.dialogs} package.
 * All user-visible dialog text (titles, questions, button labels) lives in the accompanying
 * {@code messages.properties} file; this class gives us a single place to access them.
 * Think of it as C-3PO's translation module for dialog speech.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Retrieves The Correct Diplomatic Phrase ───────────────────────────────────────
    // An officer hands C-3PO a phrase code and C-3PO immediately returns the right words.
    // If the code is unknown, he responds with a bracketed placeholder — clearly wrong, easy to spot.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given key from this package's {@code messages.properties}.
     * Returns {@code !key!} for any missing key so gaps in translation are immediately visible
     * in the UI rather than silently swallowed.
     *
     * @param key  the message key matching an entry in messages.properties
     * @return the localized string, or {@code !key!} if the key is absent
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
