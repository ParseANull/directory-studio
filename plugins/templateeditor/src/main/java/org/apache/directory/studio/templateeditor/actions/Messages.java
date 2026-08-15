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
package org.apache.directory.studio.templateeditor.actions;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FOR THE ACTIONS PACKAGE ─────────────────
// C-3PO is on duty in the actions corridor of the ship, handling translations for
// every action label and tooltip in this package. Hand him a key, he gives back the
// human-readable string from the actions message bundle. Exactly the same pattern
// as the root Messages class, but scoped to the actions package bundle.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility for looking up localized strings from the actions package
 * message bundle ({@code actions/messages.properties}). Every action label and
 * tooltip in this package is stored there — never hard-coded in Java source.
 * Returns {@code !key!} for any missing key so nothing silently goes blank.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.templateeditor.actions.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── PRIVATE CONSTRUCTOR: 3PO IS ONE-OF-A-KIND ────────────────────────────────
    // There's only one C-3PO. Private constructor prevents accidental instantiation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private — all access is through the static {@link #getString(String)} method.
     */
    private Messages()
    {
    }


    // ── GET STRING: 3PO LOOKS UP THE TRANSLATION ─────────────────────────────────
    // Hand C-3PO a phrase key and he delivers the translation. If the key is
    // missing from the bundle, he returns "!key!" so it's immediately visible
    // in the UI rather than rendering as an empty string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given key from the actions bundle.
     * Returns {@code !key!} if the key is not found.
     *
     * @param key  the message key; never {@code null}
     * @return the localized string, or {@code !key!} if missing
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
