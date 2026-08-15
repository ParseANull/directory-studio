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
package org.apache.directory.studio.combinededitor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Relays the Bridge Announcements ──────────────────
// On the Tantive IV, C-3PO stands by ready to translate every announcement that
// comes over the intercom into whatever language the crew needs — whether that's
// Rebel Standard or something more exotic.
// This class does the same for the combinededitor plugin: every user-visible
// string lives in a .properties file and this helper fetches it by key.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Centralises all UI string lookups for the combinededitor plugin.
 * Every user-facing label, button text, and error message is stored in
 * {@code combinededitor/messages.properties}; this class fetches them
 * so we never scatter raw strings through the code.
 * Think of this class as C-3PO on the Tantive IV bridge — always ready with
 * the right phrase in the right language.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.combinededitor.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── C-3PO Is Not for Casual Conversation — No Instantiation Needed ────────
    // C-3PO serves as a translator, not as a conversationalist — you call on
    // him when you need something said, not to create a permanent companion.
    // This private constructor enforces that pattern: call getString() directly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Prevents instantiation — all lookups go through the static {@link #getString} method.
     */
    private Messages()
    {
    }


    // ── C-3PO Translates the Message Key into Plain Galactic Basic ────────────
    // The bridge officer calls out a code and C-3PO consults his databanks to
    // return the full phrase; if the code is unknown he signals the error clearly
    // so no one mistakes silence for a valid response.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given message key.
     * If the key is missing from the bundle we return {@code !key!} rather than
     * throwing, which keeps the UI running even with an incomplete bundle.
     *
     * @param key  the bundle key, e.g. {@code "CombinedEditorPlugin.UnableToGetPluginProperties"}.
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
