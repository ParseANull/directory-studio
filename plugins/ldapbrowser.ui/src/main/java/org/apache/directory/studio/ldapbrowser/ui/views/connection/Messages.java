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

package org.apache.directory.studio.ldapbrowser.ui.views.connection;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO READS THE JAWA DIALECT ──────────────────────────
// C-3PO is fluent in over six million forms of communication — when Luke
// needs to know what the Jawas are saying, 3PO dips into his language banks
// and retrieves the exact phrase. If the dialect is unknown, he admits it
// with an apologetic "!key!" rather than crashing the mission.
// This class is our C-3PO: given a message key, it retrieves the localized
// string from the resource bundle, gracefully returning a bracketed key when
// the translation is missing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for retrieving localized strings from the connection view's
 * {@code messages.properties} resource bundle.
 * We use it throughout the connection view package instead of hard-coding
 * UI strings, so everything can be translated without touching Java code.
 * Think of this class as C-3PO: hand it a key, get back a human-readable phrase.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── getString: C-3PO Translates a Specific Phrase ────────────────────────
    // Luke asks C-3PO what the Jawas just said; 3PO consults his language banks
    // and returns the translation, or says "!phrase!" if it's truly unknown.
    // We look up the key in our resource bundle and wrap any missing key in
    // exclamation marks so it's obvious in the UI that a translation is absent.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Looks up and returns the localized string for the given key.
     * If the key doesn't exist in the resource bundle we return {@code !key!}
     * instead of throwing, so the UI degrades gracefully and the developer
     * can spot the missing translation immediately.
     *
     * <p>For example — C-3PO translates from Jawa to Basic:</p>
     * <pre>
     *   String label = Messages.getString( "LinkWithEditorAction.LinkWithEditor" );
     *   // Returns "Link with Editor" if the properties file has it,
     *   // or "!LinkWithEditorAction.LinkWithEditor!" if it doesn't
     * </pre>
     *
     * @param key  the message key defined in messages.properties
     * @return     the translated string, or {@code !key!} if the key is missing
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
