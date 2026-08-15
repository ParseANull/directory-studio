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
package org.apache.directory.studio.templateeditor.editor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO ON DUTY IN THE EDITOR CORRIDOR ──────────────────────
// C-3PO is stationed in the editor section of the ship, translating every label
// and status message that appears in the template entry editor. Hand him a key,
// get back the human-readable string from the editor message bundle.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility for looking up localized strings from the editor package message
 * bundle ({@code editor/messages.properties}). All user-visible text in the
 * template entry editor (status messages, error text) comes from here.
 * Returns {@code !key!} for missing keys so broken strings are immediately visible.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.templateeditor.editor.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── PRIVATE CONSTRUCTOR: ONE C-3PO PER SHIP ──────────────────────────────────
    // Static-only utility class — never instantiated.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private — use the static {@link #getString(String)} method.
     */
    private Messages()
    {
    }


    // ── GET STRING: 3PO DELIVERS THE TRANSLATION ─────────────────────────────────
    // Returns the localized string for the given key, or {@code !key!} if missing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given key from the editor bundle.
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
