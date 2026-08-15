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

package org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO READS THE MODIFICATION LOG DIALECT ───────────────
// C-3PO is fluent in over six million forms of communication — when Leia needs
// a particular phrase in Huttese, 3PO consults his memory banks and returns
// the exact translation. If the phrase isn't in his banks, he returns a
// bracketed marker rather than crashing the diplomatic mission.
// This class is 3PO for the modification logs package: given a key, it
// retrieves the localized string from the resource bundle gracefully.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for retrieving localized strings from the modification logs view's
 * {@code messages.properties} resource bundle.
 * Every user-visible string in the modificationlogs package goes through here
 * so translations can be added without touching Java code.
 * Think of this class as C-3PO: hand it a key, get back a human-readable phrase.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── getString: C-3PO Translates On Demand ────────────────────────────────
    // "Oh my! I believe I know what they're saying." C-3PO dips into his
    // language banks and returns the translation instantly — and if it's a
    // dialect he doesn't know, he wraps it in "!...!" so the crew knows
    // something needs attention.
    // We look up the key and fall back to the bracketed key on MissingResourceException.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Looks up and returns the localized string for the given key.
     * If the key isn't in the resource bundle we return {@code !key!} rather
     * than throwing, so the UI degrades gracefully and the missing translation
     * is immediately obvious.
     *
     * <p>For example — C-3PO translates or flags the unknown phrase:</p>
     * <pre>
     *   String label = Messages.getString( "ClearAction.Clear" );
     *   // Returns "Clear" if present, or "!ClearAction.Clear!" if missing
     * </pre>
     *
     * @param key  the message key defined in messages.properties
     * @return     the translated string, or {@code !key!} if missing
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
