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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translates Between Languages ─────────────────────
// In A New Hope, C-3PO introduces himself proudly: "I am C-3PO, human-cyborg
// relations — fluent in over six million forms of communication." Whenever Luke
// needs to know what the Jawas said, or what the cantina bartender meant, he
// asks Threepio. This class is that translator: every dialog, label and tooltip
// in the browser package asks us for a human-readable string by key, and we
// consult our messages.properties "protocol droid" and hand back the translation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A thin wrapper around a Java {@link ResourceBundle} that turns message keys
 * like {@code "BrowserSorterDialog.Ascending"} into the localised display
 * strings defined in {@code messages.properties}.
 * Every class in this package uses us instead of hard-coding string literals,
 * which keeps all UI text in one file and makes localisation straightforward.
 * Think of this class as C-3PO: you hand him a phrase key, he hands you back
 * the right words for the audience.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO TRANSLATES A PHRASE ──────────────────────────────────────────────
    // Luke holds up an alien pendant and says "Threepio, what does this say?"
    // C-3PO consults his vast language database and reads it back in plain Basic.
    // If the pendant is from a language he doesn't recognise, he wraps it in
    // exclamation marks so Luke immediately sees something went wrong.
    // That's what we do: if the key exists, return its value; otherwise wrap
    // the key in "!" so developers can spot the missing translation instantly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by its message key from the package-level
     * {@code messages.properties} file.
     * If the key doesn't exist (a typo or a forgotten entry), we return
     * {@code "!key!"} instead of throwing an exception, so the UI still works
     * and the problem is visible without crashing.
     *
     * <p>For example — C-3PO handles a translation request:</p>
     * <pre>
     *   getString("BrowserSorterDialog.Ascending")
     *     → looks up RESOURCE_BUNDLE
     *     → returns "Ascending"           // found
     *
     *   getString("SomeDialog.Typo")
     *     → MissingResourceException caught
     *     → returns "!SomeDialog.Typo!"   // not found — obvious marker
     * </pre>
     *
     * @param key  The message key exactly as written in {@code messages.properties},
     *             e.g. {@code "BrowserSorterDialog.NoSorting"}.
     * @return     The localised string for that key, or {@code "!key!"} if the
     *             key is missing from the bundle.
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
