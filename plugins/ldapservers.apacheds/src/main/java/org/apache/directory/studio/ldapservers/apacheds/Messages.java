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
package org.apache.directory.studio.ldapservers.apacheds;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating the Falcon's Status Phrases ──────────
// C-3PO stands at the Mos Eisley docking bay console translating every status
// label and error message for the crew.  When Han asks "what does this readout
// say?" C-3PO looks up the phrase in his phrase book (the .properties file)
// and reads it aloud.  If the phrase isn't there he reports the key wrapped in
// exclamation marks so Han knows something's missing.
// Messages is C-3PO for this plugin: it wraps a ResourceBundle and exposes
// getString(key) as the only public method.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised strings from this plugin's {@code messages.properties} file.
 * Call {@link #getString(String)} with a message key to get the corresponding
 * human-readable string.  If the key is not found, returns
 * {@code "!" + key + "!"} so the gap is immediately obvious at development time.
 * Think of this as C-3PO translating Falcon control-panel readouts into plain
 * Basic for the crew in Mos Eisley docking bay.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Doesn't Need a Constructor — He Just Translates ─────────────────
    // Static utility class; no instances needed or allowed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a static utility class.
     */
    private Messages()
    {
    }


    // ── Look Up the Phrase in the Control-Panel Readout Book ──────────────────
    // If the key exists, return its value.  If not, wrap the key in bangs so
    // the missing translation stands out immediately during development.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given message key.
     * Returns {@code "!" + key + "!"} if the key is not found.
     *
     * @param key  the message key to look up.
     * @return     the localised string, or {@code "!" + key + "!"} if not found.
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
