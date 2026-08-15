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
package org.apache.directory.studio.openldap.config.editor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Bridges Languages For The Alliance ───────────────
// C-3PO is fluent in over six million forms of communication — whenever the
// rebels need to talk to a Jawa or negotiate with a Hutt, C-3PO steps in and
// bridges the gap between raw meaning and local dialect.
// This class does the same: it bridges our raw string keys and the locale-
// specific messages in our .properties file, so the rest of the plugin never
// has to care which language the user is running.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class that pulls translated UI strings from our {@code messages.properties}
 * resource bundle.
 * The rest of the editor package calls {@code Messages.getString("some.key")} to
 * get a displayable string — this class does the lookup and gracefully returns
 * {@code !key!} if the key is missing (so the UI fails visibly but not fatally).
 * Think of it as C-3PO: we give it a key, it gives back the right words in the
 * right language.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    // ── C-3PO Does Not Speak For Himself ─────────────────────────────────────
    // C-3PO is purely a conduit — he exists to serve communication, not to be
    // instantiated as an individual with his own agenda.
    // We make the constructor private so nobody accidentally creates a Messages
    // object; all the useful behavior lives on static methods.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a static utility class, so we prevent
     * instantiation to make that explicit.
     */
    private Messages()
    {
        // Do nothing
    }


    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Translates The Jawa Dialect ────────────────────────────────────
    // On Tatooine, C-3PO listens to a Jawa's rapid-fire chatter and turns it
    // into Basic so Luke can understand what's on offer.
    // We hand him a raw key string and he looks it up in the bundle, returning
    // the human-readable message in the right locale.
    // If the key doesn't exist, he helpfully wraps it in exclamation marks so
    // we can spot the gap immediately in the UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by key from the {@code messages.properties} resource bundle.
     * If the key exists, we return its value; if it's missing, we return
     * {@code !key!} so the gap shows up visibly in the UI rather than silently
     * breaking.
     *
     * @param key  the bundle key to look up — typically something like
     *             {@code "SomeDialog.Title"}
     * @return the translated string, or {@code !key!} if no mapping exists
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
