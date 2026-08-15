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

package org.apache.directory.studio.ldapbrowser.ui;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Reads the Messages Bundle ───────────────────────
// C-3PO is fluent in over six million forms of communication — he reads the raw
// transmission and hands back something the humans can actually understand.
// This class is our C-3PO: it bridges the raw .properties file and every class
// that needs a human-readable string, translating a key into a localised message.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS (National Language Support) helper that loads localised strings from the
 * {@code messages.properties} file sitting next to this class on the classpath.
 * Every UI string that might be shown to a user should come from here rather than
 * being hard-coded, so we can support multiple locales without touching Java code.
 * Think of this class as C-3PO — it takes a cryptic key and hands back the
 * message in whatever language the environment speaks.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Translates the Incoming Transmission ───────────────────────────
    // R2 beeps a code; C-3PO reads the dictionary and speaks plain English back.
    // If R2's code doesn't map to anything in the dictionary, 3PO doesn't crash —
    // he just repeats the code wrapped in exclamation marks so you know it's raw.
    // That's exactly what we do: unknown key → {@code !key!} as a visible signal.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up and returns the localised string for the given message key from
     * the {@code messages.properties} bundle. If the key isn't found (e.g. a typo
     * or a key that exists in code but got dropped from the properties file), we
     * return the key wrapped in exclamation marks — {@code !key!} — so it's
     * immediately obvious in the UI that a translation is missing.
     *
     * @param key  the property key to look up, e.g. {@code "BrowserUIPlugin.UnableGetPluginProperties"}
     * @return the localised message string, or {@code !key!} if the key is absent
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
