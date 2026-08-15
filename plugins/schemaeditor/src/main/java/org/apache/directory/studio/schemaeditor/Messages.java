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
package org.apache.directory.studio.schemaeditor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translates R2-D2's Beeps For The Crew ────────────
// R2-D2 blurts out a stream of electronic chirps and whistles that only he
// understands — C-3PO listens, processes, and renders the meaning as plain
// English so the crew can act on it. This class is C-3PO: callers hand it an
// opaque message key (the beeping) and it returns the human-readable string
// from the resource bundle (the translation).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides access to the localized string resources for the Schema Editor plugin's
 * root package. Callers pass in a message key and get back the corresponding
 * translated string from the {@code messages.properties} file on the classpath.
 * Think of this class as C-3PO: it takes R2's raw signal (the key) and gives
 * back something the humans can read (the message text).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Decodes R2's Transmission And Speaks To The Room ──────────────────
    // R2-D2 emits a series of beeps; C-3PO processes them and announces the meaning
    // to everyone present — or, if R2 said something completely garbled, C-3PO flags
    // it as unintelligible rather than making something up. If the key has no entry
    // in our resource bundle, we wrap it in exclamation marks so it's visibly wrong.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the human-readable message for the given key in our resource bundle.
     * We need this because Java UI code shouldn't have raw strings baked in — they
     * all live in messages.properties so they can be translated or changed centrally.
     * If the key isn't found we return {@code !key!} as a visible placeholder so
     * missing translations are obvious during development.
     *
     * <p>For example — C-3PO decodes R2's message:</p>
     * <pre>
     *   R2 beeps: "Activator.UnablePluginProperties"
     *   C-3PO replies: "Unable to load plugin properties."
     *   If R2 beeps an unknown code: C-3PO says "!UnknownCode!"
     * </pre>
     *
     * @param key  the message key as defined in messages.properties — not null.
     * @return     the translated string, or {@code !key!} if the key is not found.
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
