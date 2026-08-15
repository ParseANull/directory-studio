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
package org.apache.directory.studio.combinededitor.actions;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Relays the Crew's Action Orders ──────────────────
// When the Tantive IV crew shouts an order, C-3PO translates it into the
// appropriate language for whoever needs to hear it — making sure the right
// words appear on screen.
// This class does the same for the actions sub-package: it's the message lookup
// for all action labels, tooltips, and error text in the actions package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Centralises string lookups for the combinededitor actions package.
 * Every user-facing string from action classes is pulled from
 * {@code combinededitor/actions/messages.properties} through this helper.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.combinededitor.actions.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── C-3PO Is a Tool, Not a Guest ─────────────────────────────────────────
    // You call on C-3PO; you don't create a new one every time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Prevents instantiation — use {@link #getString(String)} directly.
     */
    private Messages()
    {
    }


    // ── C-3PO Delivers the Translation — Or Flags the Unknown Code ────────────
    // C-3PO looks up the given code in his databanks and returns the phrase;
    // if the code is unknown he wraps it in exclamation marks so nobody misses it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given key, or {@code !key!} if missing.
     *
     * @param key  the bundle key to look up.
     * @return     the localised string, or {@code !key!} if not found.
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
