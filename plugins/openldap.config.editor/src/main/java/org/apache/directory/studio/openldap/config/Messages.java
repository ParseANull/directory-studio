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
package org.apache.directory.studio.openldap.config;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translates For The Whole Crew ────────────────────
// C-3PO is fluent in over six million forms of communication. His job on the
// Millennium Falcon is to take whatever R2-D2 or another droid says in raw
// machine language and translate it into something the human crew can read and
// act on. He's a pure translator — no agenda of his own, just faithful lookup
// and conversion.
// This class is our C-3PO: it takes short symbolic message keys (like
// "OpenLdapConfigurationPlugin.UnableGetProperties") and translates them into
// the full, human-readable strings stored in the plugin's messages.properties file.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides access to the plugin's externalized string resources.
 * All user-visible strings live in a messages.properties file (keyed by the bundle
 * name) so they can be localized and kept out of the source code. We use a static
 * {@link ResourceBundle} to load those strings and expose them via {@link #getString}.
 * Think of this class as C-3PO: we translate opaque key codes into human-readable
 * messages for the rest of the plugin.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.openldap.config"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── C-3PO Is Not For Casual Conversation ─────────────────────────────────
    // "I am not much more than an interpreter, and not very good at telling
    // stories." — C-3PO doesn't socialize; he translates. This utility class
    // has no instance state, so we prevent instantiation entirely.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a pure static utility class. Nothing to
     * instantiate, nothing to configure. Just call {@link #getString(String)}.
     */
    private Messages()
    {
    }


    // ── C-3PO Looks Up The Translation And Delivers It ───────────────────────
    // Han asks "what did R2 say?" and C-3PO checks his internal translation
    // matrices, finds the entry for that particular beep sequence, and delivers
    // the English equivalent. If the sequence is unknown, he says so — he doesn't
    // just make something up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a message string by key from the plugin's resource bundle.
     * If the key doesn't exist in the bundle (because a message was removed or
     * misspelled), we return "!key!" as a visible debugging signal rather than
     * throwing an exception. That way the UI still works but the missing key
     * is obvious.
     *
     * @param key  the message key from messages.properties (e.g.
     *             "OpenLdapConfigurationPlugin.UnableGetProperties")
     * @return     the localized string for that key, or "!key!" if not found
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
