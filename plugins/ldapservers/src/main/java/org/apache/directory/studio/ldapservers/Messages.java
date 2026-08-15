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
package org.apache.directory.studio.ldapservers;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S TRANSLATION DESK ──────────────────────────────────────────────
// C-3PO fluently translates between the Rebellion and every alien species — given a message
// key, he reaches into his language banks and returns the correct phrase in the current locale.
// This is the standard Eclipse NLS (Native Language Support) helper: it wraps a ResourceBundle
// so the rest of the plugin can call {@code Messages.getString("key")} without worrying about
// locale loading or missing-key errors.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Loads and exposes localized strings for the {@code ldapservers} root package.
 * All user-visible text for this package is stored in the accompanying {@code messages.properties}
 * file; this class is the single access point so changes to the bundle are isolated here.
 * Think of it as C-3PO standing between the code and the human: hand him a key, get back words.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Looks Up A Phrase In His Language Banks ───────────────────────────────────────
    // An officer hands C-3PO a phrase code — "ConsolesManager.LdapServer" — and C-3PO instantly
    // retrieves the correct human-readable string from his language banks.
    // If the code is unknown, he returns a bracketed placeholder so we can spot the gap quickly.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string associated with the given key from the package's
     * {@code messages.properties} resource bundle.
     * If the key doesn't exist (e.g., a typo or a missing translation), we return {@code !key!}
     * so the gap is immediately obvious in the UI rather than throwing an exception.
     *
     * <p>For example — C-3PO translates on demand:</p>
     * <pre>
     *   getString("ConsolesManager.LdapServer") → "LDAP Server"
     *   getString("typo.key.missing")           → "!typo.key.missing!"
     * </pre>
     *
     * @param key  the message key from messages.properties; should match exactly
     * @return the localized string, or {@code !key!} if the key is not found
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
