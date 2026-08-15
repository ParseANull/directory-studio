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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING THE WIZARD DIALECT ──────────────────
// C-3PO is fluent in over six million forms of communication, and the wizard
// package has its own vocabulary: button labels, page titles, error messages,
// confirmation dialogs. This class is C-3PO for the wizards: hand it a key
// and it hands you back the right phrase in the user's locale.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides locale-aware UI strings for the ldapbrowser.ui wizards package.
 * Every user-visible label, title, description, and error message in this
 * package is looked up through this class so that translations only require
 * updating the messages.properties file — not touching any Java code.
 * Think of C-3PO: give him a key and he returns the right phrase; if the key
 * is missing he returns {@code !key!} so developers immediately notice the gap.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Consults His Language Banks ────────────────────────────────────────
    // "Excuse me sir, but that phrase is not in my memory banks." C-3PO never
    // crashes — he tells you politely when he doesn't know something.
    // We return !key! instead of throwing so the UI shows something visible
    // rather than an exception trace.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Retrieves a localised string from the wizards messages resource bundle.
     * If the key has no mapping — perhaps a new message key wasn't added to
     * the properties file — we return {@code !key!} so the gap is obvious
     * in the UI without crashing.
     *
     * @param key  the message key as defined in messages.properties.
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
