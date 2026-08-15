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

package org.apache.directory.studio.ldapbrowser.ui.dialogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FROM THE PROTOCOL ARCHIVES ───────────
// C-3PO speaks six million forms of communication, but he still has to look
// things up — he consults his internal archives and returns the right phrase
// for whatever situation the crew finds themselves in.
// We do the same: look up a key in a .properties file and hand back the
// localized string every dialog and label needs.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS message bundle accessor for the ldapbrowser.ui dialogs package.
 * Loads the sibling {@code messages.properties} file and lets every class
 * in this package call {@code Messages.getString("some.key")} instead of
 * hard-coding UI strings.
 * Think of this class as C-3PO's translation subroutines — pass in a key,
 * get back the right words.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO CONSULTS THE LINGUISTIC DATABASE ────────────────────────────────
    // C-3PO is asked for the Huttese word for "negotiation" mid-standoff; he
    // flips through his six-million-language database and returns the answer.
    // If the key doesn't exist he produces an embarrassed "!" prefix instead of
    // throwing an exception and crashing the whole conversation.
    // We follow the same pattern: missing keys surface visibly as !key! so
    // developers notice them immediately without blowing up the UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by key in the bundled messages.properties file.
     * If the key is missing we return {@code !key!} so it's immediately obvious
     * in the UI rather than causing a crash.
     *
     * <p>For example — C-3PO translates on the fly:</p>
     * <pre>
     *   "What does he want?" → C-3PO checks archives → "He says he wants the Wookiee."
     *   getString("EncoderDecoderDialog.Title") → "LDAP Encode/Decoder"
     * </pre>
     *
     * @param key  The message key defined in messages.properties; never null.
     * @return     The localized string, or {@code !key!} if the key isn't found.
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
