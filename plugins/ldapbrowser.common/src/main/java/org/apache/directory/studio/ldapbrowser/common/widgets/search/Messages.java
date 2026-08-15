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
package org.apache.directory.studio.ldapbrowser.common.widgets.search;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating Between Machine Keys and Human Phrases ────────
// In A New Hope, C-3PO prides himself on fluency in over six million forms of
// communication. When someone hands him a raw machine code — say "AliasesDereferencingWidget
// .AliasesDereferencing" — he looks it up in his protocol database and hands back the
// polished human-readable phrase that belongs in the UI label. If the code isn't in
// his database, he improvises with a "!key!" placeholder so we at least know what went
// wrong, rather than crashing the whole mission.
// ──────────────────────────────────────────────────────────────────────────────────────
/**
 * A standard Eclipse i18n (internationalisation) helper that looks up UI strings
 * by key from the {@code messages.properties} file sitting in the same package.
 * Think of this class as C-3PO: you hand him a machine key, he hands you back the
 * localised human phrase.
 *
 * <p>Every widget in this package calls {@link #getString(String)} to get its
 * button labels, group titles, and tooltip strings, rather than hard-coding English
 * directly in the Java source. This keeps translated text separate from logic.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Looks Up a Phrase in His Protocol Database ─────────────────────────────
    // A Rebel officer hands C-3PO a machine code: "BrowserConnectionWidget.BrowseButton".
    // 3PO opens his protocol database, finds the matching entry, and reads the phrase
    // aloud: "Browse...". If the key is missing he says "!BrowserConnectionWidget.BrowseButton!"
    // so everyone knows the translation database has a gap.
    // ─────────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up and returns the localised UI string associated with the given key.
     * The key must exactly match an entry in the package's {@code messages.properties}
     * file. If the key is not found — for example, because a developer forgot to add
     * a new string — we return {@code !key!} as a visible placeholder rather than
     * throwing an exception and crashing the widget.
     *
     * <p>For example — C-3PO translates a machine code:</p>
     * <pre>
     *   key    = "AliasesDereferencingWidget.FindingBaseDN";
     *   result = "Finding Base DN";      // from messages.properties
     *   // if missing: "!AliasesDereferencingWidget.FindingBaseDN!"
     * </pre>
     *
     * @param key  The property key to look up. Must not be null.
     * @return     The localised string for the key, or {@code !key!} if not found.
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
