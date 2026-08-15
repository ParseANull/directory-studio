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
package org.apache.directory.studio.schemaeditor.model;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating R2-D2's Beeps For The Crew ───────────
// C-3PO stands between R2-D2's cryptic whistles and the humans who need plain
// English. He intercepts every signal (a string key), looks it up in his
// translation matrix (the resource bundle), and hands back something the crew
// can actually act on. When R2 emits a code that's not in the dictionary,
// C-3PO wraps it in exclamation marks so nobody mistakes gibberish for a
// real message.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up user-visible strings from the model package's resource bundle.
 * We centralise all message retrieval here so the rest of the model code
 * never has to know where strings come from or how the bundle is loaded.
 * Think of this class as C-3PO: hand it a key, get back a human-readable
 * string; if the key is unknown, you get {@code !key!} as a visible warning.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Translates A Signal From R2 ───────────────────────────────────────
    // R2 emits a whistle (the key). C-3PO looks it up in his linguistic database
    // and returns the human-readable translation. If the whistle doesn't match
    // any known pattern, C-3PO flags it with "!" so the crew knows something
    // went wrong rather than silently getting a null or an exception.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string associated with the given key from this
     * package's resource bundle. We use this everywhere in the model that needs
     * an error message or user-visible label.
     * If the key doesn't exist in the bundle, we return {@code !key!} so the
     * missing translation is immediately obvious during development.
     *
     * <p>For example — C-3PO translates a signal:</p>
     * <pre>
     *   getString("DependenciesComputer.Schema") → "Schema ''{0}'' not found."
     *   getString("unknownKey")                  → "!unknownKey!"
     * </pre>
     *
     * @param key  the message key as declared in the messages.properties file
     * @return     the localised string, or {@code !key!} if the key is missing
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
