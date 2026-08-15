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
package org.apache.directory.studio.schemaeditor.view.editors;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO READS THE JAWA DIALECT ──────────────────────────────────
// When the Jawas found R2-D2 and C-3PO on Tatooine, nobody else could talk to those
// little scavengers — their dialect was impenetrable.  C-3PO stepped in and decoded
// their speech into something everyone could understand.  This class does the same
// job: it takes a bare string key (like "EditorsUtils.SaveDialogTitle") and translates
// it into a human-readable message from the locale-specific .properties bundle.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Thin wrapper around a Java ResourceBundle for the {@code view.editors} package.
 * UI code throughout this package calls {@link #getString(String)} with a message key
 * instead of hard-coding English strings, which keeps all translatable text in one
 * place and makes internationalization straightforward.
 * Think of this class as C-3PO: you hand it a cryptic key and it hands you back a
 * sentence a human can actually read.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Decodes a Jawa Phrase ──────────────────────────────────────────────────
    // Someone hands C-3PO a strange sound ("EditorsUtils.SaveDialogTitle") and he
    // reaches into his linguistic database, finds the matching translation, and speaks
    // it aloud.  If the phrase isn't in his database, he improvises: he wraps it in
    // exclamation marks so the crew at least knows something went wrong.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localised message string by its bundle key.
     * If the key exists in the {@code messages.properties} file for the current locale,
     * we return the translated string.  If the key is missing — which would be a bug —
     * we return {@code !key!} rather than throwing, so the UI stays functional even
     * during development when a key hasn't been added to the bundle yet.
     *
     * <p>For example — C-3PO translates or signals an error:</p>
     * <pre>
     *   Messages.getString("EditorsUtils.SaveDialogTitle") // → "Save Resources"
     *   Messages.getString("NonExistent.Key")              // → "!NonExistent.Key!"
     * </pre>
     *
     * @param key  the dot-separated message key matching an entry in messages.properties;
     *             should never be null
     * @return     the localised message string, or {@code !key!} if the key is not found
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
