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

package org.apache.directory.studio.ldapbrowser.ui.views.browser;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING THE JAWA DIALECT ────────────────────
// C-3PO fluent in over six million forms of communication, listening to the
// Jawas' rapid-fire chatter and converting it into something Luke can
// actually understand. This class does the same: given a raw key string,
// it finds the right human-readable phrase from the resource bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up locale-sensitive UI strings from this package's messages resource bundle.
 * We need this because hardcoding English text in Java source files makes
 * internationalization impossible — the bundle is the single source of truth for labels.
 * Think of this class as C-3PO: it takes a raw key and translates it into
 * whatever language the user's environment speaks.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Looks Up the Translation ──────────────────────────────────────────
    // C-3PO, aboard the Millennium Falcon, flips through his internal translation
    // matrices looking for the Jawa phrase "utinni!" so he can tell Luke what
    // they actually said.
    // We do the same: we hand this method a key string and get back the
    // display-ready label to show in a button, menu item, or tooltip.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fetches a UI string from the resource bundle by key.
     * If the key is missing — maybe someone forgot to add a translation —
     * we return the key itself wrapped in exclamation marks so it's obvious
     * in the UI that something is wrong rather than silently showing nothing.
     *
     * <p>For example — C-3PO translates on demand:</p>
     * <pre>
     *   "ShowDITAction.ShowDIT" → "Show DIT"
     *   "OpenBrowserPreferencePageAction.Preferences" → "Preferences"
     *   missing key → "!missing.key!"
     * </pre>
     *
     * @param key  the message key defined in the messages.properties file for this package.
     * @return     the localised string, or {@code !key!} if no mapping exists.
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
