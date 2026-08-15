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
package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATES THE JAWA DIALECT ──────────────────────────────
// C-3PO is fluent in over six million forms of communication, including the Jawa dialect
// spoken by the scavengers on Tatooine.  Nobody else on the crew could understand what
// the Jawas were saying — C-3PO stepped in and converted their rapid, bleeping speech
// into clear sentences.  That's exactly what this class does for the attributetype
// editor package: you hand it a cryptic bundle key like
// "AttributeTypeEditorOverviewPage.Aliases" and it hands you back a human-readable label
// in the current locale.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Thin wrapper around a Java ResourceBundle for the {@code view.editors.attributetype}
 * package.
 * Every label, error message, and section title used by the attribute type editor UI
 * is stored in a .properties file; this class loads that bundle and provides a single
 * static accessor so all classes in this package can look up their strings consistently.
 * Think of this as C-3PO: hand it a key, get back a human-readable string; hand it a
 * missing key, get back a clearly-marked error token instead of an exception.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Decodes the Jawa Phrase ────────────────────────────────────────────────
    // Someone shouts a Jawa phrase at C-3PO and he reaches into his linguistic database,
    // finds the matching translation, and speaks it aloud.  If the phrase isn't in the
    // database — which would be a development bug — he announces the garbled key wrapped
    // in exclamation marks so it's unmistakably obvious that something is missing.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localised message string by its bundle key.
     * If the key is found in the messages.properties file for the current locale, we
     * return the translated string.  If the key is missing — which would be a bug during
     * development — we return {@code !key!} rather than throwing, so the UI stays
     * functional and the bad key is visually obvious.
     *
     * <p>For example — C-3PO translates or signals an error:</p>
     * <pre>
     *   Messages.getString("AttributeTypeEditorOverviewPage.Aliases")
     *       // → "Aliases:"  (from the .properties file)
     *
     *   Messages.getString("NonExistent.Key")
     *       // → "!NonExistent.Key!"  (visible placeholder for a missing entry)
     * </pre>
     *
     * @param key  the dot-separated message key matching an entry in messages.properties;
     *             must not be null
     * @return     the localised message string, or {@code !key!} if the key is missing
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
