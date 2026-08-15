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
package org.apache.directory.studio.valueeditors.integer;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO's Calculator Phrase Book ─────────────────────────
// C-3PO keeps a phrase book of every term his calculation console uses.
// "Integer Editor," error messages, labels — all of them live in here, indexed by key.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Provides localized strings for the integer value editor UI.
 * All user-visible text in the integer package is looked up here by key, loaded from the
 * {@code messages.properties} file sitting next to this class in the same package.
 * Think of this class as C-3PO's phrase book: every term the calculation console displays
 * — titles, labels, error messages — is catalogued here by code word.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Looks Up A Term In His Phrase Book ──────────────────────────────────
    // Someone asks C-3PO for a term by its code word; C-3PO flips to the right page and reads it.
    // If the code word is not in the book he announces "!key!" so the team knows it is missing.
    // We look the key up in the ResourceBundle and fall back to the bracketed key on a miss.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Retrieves the localized string associated with the given key from the integer package's
     * {@code messages.properties} resource bundle.
     * If the key is missing — meaning someone added UI text without a corresponding bundle
     * entry — we return the key wrapped in exclamation marks so it stands out during testing.
     *
     * <p>For example — C-3PO looks up a term in his phrase book:</p>
     * <pre>
     *   Request: "IntegerDialog.IntegerEditor"
     *   C-3PO: "Integer Editor"
     *   Unknown code: C-3PO reports "!IntegerDialog.IntegerEditor!"
     * </pre>
     *
     * @param key the message key defined in {@code messages.properties}
     * @return the localized string, or {@code "!" + key + "!"} if the key is absent
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
