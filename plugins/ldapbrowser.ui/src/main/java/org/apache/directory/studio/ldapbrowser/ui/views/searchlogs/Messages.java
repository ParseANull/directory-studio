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

package org.apache.directory.studio.ldapbrowser.ui.views.searchlogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING THE JAWA DIALECT ────────────────────
// C-3PO can speak six million languages and here he's working overtime:
// the search log view needs human-readable labels for every button and menu
// item, and C-3PO is the one fetching those translations from the resource
// bundle on demand.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides locale-aware UI strings for the search logs view package.
 * Every user-visible label, tooltip, and menu item in this package runs
 * through this class so that adding a new locale only requires updating
 * the messages.properties file — not touching any Java code.
 * Think of C-3PO: give him a key and he'll hand you back the right phrase
 * in whatever language the user's environment demands.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Consults His Language Banks ────────────────────────────────────────
    // "I am fluent in over six million forms of communication." C-3PO doesn't
    // guess — he looks it up, and if the phrase isn't in his memory banks he
    // makes it obvious by returning something clearly wrong.
    // We do the same: look up the key, and if it's missing return !key! so
    // developers immediately see there's a missing translation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Retrieves a localised string from the search logs messages resource bundle.
     * If the key has no mapping — perhaps someone forgot to add it to the properties
     * file — we return {@code !key!} instead of throwing, so the UI shows something
     * visible rather than crashing or showing nothing at all.
     *
     * @param key  the message key as defined in messages.properties.
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
