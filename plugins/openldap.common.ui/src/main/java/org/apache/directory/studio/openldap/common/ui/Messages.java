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
package org.apache.directory.studio.openldap.common.ui;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — REBEL BASE COMMUNICATIONS CENTER ───────────────────────
// Think of this as the comm officer at Echo Base who translates all encrypted
// Rebel transmissions into plain Basic so everyone on the base can understand
// them. We keep a single resource bundle loaded and pull strings out of it on
// demand so the rest of the UI never has to worry about i18n plumbing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We handle all UI message lookups for the openldap.common.ui plugin here.
 * Think of us as the comm center: callers ask for a message key and we hand
 * back the localized string. If the key is missing from the bundle, we return
 * a clearly marked placeholder so the bug is obvious rather than silent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── CONSTRUCTOR: Messages — SEALING THE COMM CENTER ──────────────────────
    // The comm center is a shared facility — nobody should create their own
    // private instance. This private constructor makes sure the class is used
    // only through its static helper method.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — we're a pure static utility class, so we prevent
     * instantiation entirely. All access goes through {@link #getString(String)}.
     */
    private Messages()
    {
    }


    // ── METHOD: getString — DECODING THE REBEL TRANSMISSION ──────────────────
    // The comm officer receives the coded key, looks it up in the encrypted
    // Rebel communication bundle, and returns the plain-language message for
    // everyone to read. If the key doesn't exist in the bundle, she wraps it
    // in exclamation marks so the team knows the translation is missing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a localized string by its bundle key. If the key exists in the
     * resource bundle we return the translated value; if it's missing we return
     * the key wrapped in exclamation marks (e.g., {@code !my.key!}) so the
     * missing entry is immediately visible in the UI.
     *
     * @param key  the resource bundle key to look up
     * @return     the localized string, or {@code !key!} if the key is absent
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
