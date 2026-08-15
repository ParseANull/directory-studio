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
package org.apache.directory.studio.apacheds.configuration.editor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING IMPERIAL COMMUNIQUÉS ─────────────────────────────────────
// In The Empire Strikes Back, C-3PO is fluent in over six million forms of communication.
// Whenever the crew needs to understand a cryptic Imperial transmission, they hand the raw
// message key to Threepio and he looks it up in his vast linguistic database, then reads
// back the polished translation in plain Galactic Basic.
// This class does exactly the same thing: the UI hands it a raw message key (e.g.,
// {@code "ErrorPage.ErrorOpeningEditor"}) and we look it up in the NLS resource bundle,
// returning the human-readable string appropriate for the current locale.
// If the key doesn't exist in the bundle, we wrap it in exclamation marks — Threepio's
// equivalent of "I have a very bad feeling about this."
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * NLS (Native Language Support) helper class for the {@code editor} package.
 * All user-facing strings in this package are stored in a {@code messages.properties}
 * resource bundle; this class provides the {@link #getString(String)} bridge that
 * converts a message key into the localised string. If a key is missing we return
 * a bracketed placeholder rather than throwing, so the UI degrades gracefully.
 * Think of this class as C-3PO: you hand him a key, he looks it up in his linguistic
 * database, and reads back the translation. Unknown keys get the Threepio treatment:
 * {@code !key!}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO RETRIEVES THE TRANSLATION ──────────────────────────────────────────────────────────
    // The crew hands Threepio a message key: "ErrorPage.ErrorOpeningEditor."
    // He flips through his linguistic database, finds the entry, and reads back the
    // polished translation: "Error Opening Editor."
    // If the key is not in the database he reports: "!ErrorPage.ErrorOpeningEditor!" —
    // clearly flagging the missing translation for any developer who spots it.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the localised string for the given message key. We read from the
     * {@code messages.properties} resource bundle co-located with this class.
     * If the key is missing — which means someone added a UI string without adding
     * the corresponding bundle entry — we return {@code !key!} as a visible
     * placeholder rather than propagating the {@link MissingResourceException}.
     *
     * <p>For example — Threepio retrieves a translation:</p>
     * <pre>
     *   // messages.properties: ErrorPage.ErrorOpeningEditor=Error Opening Editor
     *   String label = Messages.getString("ErrorPage.ErrorOpeningEditor");
     *   // label == "Error Opening Editor"
     *
     *   String missing = Messages.getString("Nonexistent.Key");
     *   // missing == "!Nonexistent.Key!"
     * </pre>
     *
     * @param key  The NLS message key (must not be {@code null}).
     * @return     The localised string, or {@code !key!} if the key is absent.
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
