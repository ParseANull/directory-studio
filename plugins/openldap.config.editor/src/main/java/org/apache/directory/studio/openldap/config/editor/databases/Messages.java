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
package org.apache.directory.studio.openldap.config.editor.databases;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating for R2-D2 ─────────────────────────────
// C-3PO doesn't know what R2 is talking about — so he looks it up in his
// protocol banks and produces a human-readable translation on the spot.
// This class does the same: it looks up a key in the messages.properties
// resource bundle and returns the matching human-readable UI string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides i18n message strings for the OpenLDAP databases editor pages.
 * It's a thin wrapper around Java's {@link ResourceBundle} — you give it a key,
 * it gives you back the right locale-specific string from the properties file.
 * Think of this class as C-3PO: whenever someone in the UI needs to say something
 * to a human, they go through this translator.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    // ── No Instances, Please ─────────────────────────────────────────────────
    // C-3PO is a unique droid — you don't build a fleet of them. Similarly,
    // this utility class is entirely static; there's no reason to instantiate it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a pure static utility class.
     * Instantiation is meaningless; all methods are static.
     */
    private Messages()
    {
        // Nothing to do
    }


    /** The messages.properties resource bundle for this package. */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── Translate a Key into Human Language ───────────────────────────────────
    // C-3PO receives a beep from R2-D2, consults his protocol banks
    // (the resource bundle), and returns the plain-language equivalent.
    // If the key isn't found, he wraps it in exclamation marks so we know
    // something broke — "!missing.key!" is our "I have a bad feeling about this."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the UI string for the given resource bundle key.
     * Used by all database page classes to fetch locale-aware labels, button text,
     * and section titles rather than hard-coding English strings in Java.
     * If the key is not found in the bundle we return {@code !key!} so the problem
     * is immediately visible in the UI rather than throwing an unchecked exception.
     *
     * <p>For example — C-3PO looks up a translation:</p>
     * <pre>
     *   String label = Messages.getString( "OpenLDAPMDBConfiguration.Directory" );
     *   // returns "Directory:" from messages.properties
     * </pre>
     *
     * @param key  the resource bundle key to look up
     * @return     the translated string, or {@code !key!} if the key is not found
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
