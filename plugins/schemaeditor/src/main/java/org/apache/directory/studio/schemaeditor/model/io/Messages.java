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
package org.apache.directory.studio.schemaeditor.model.io;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating Between Droids and Humans ────────────
// C-3PO is fluent in over six million forms of communication; when someone
// hands him a phrase code (a key) he looks it up in his linguistic database
// and returns the right human-language string.  If the key doesn't exist he
// wraps it in exclamation marks so at least you can tell something went wrong.
// That's all this class does: wrap a ResourceBundle lookup with a graceful
// fallback so callers never get a raw MissingResourceException.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Provides localised UI strings for the {@code model.io} package by delegating
 * to a {@link ResourceBundle} loaded from the {@code messages.properties} file
 * in the same package.
 * Any missing key returns {@code !key!} rather than blowing up, so the UI stays
 * functional even if a translation is incomplete.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Looks Up a Phrase in His Linguistic Database ───────────────────
    // Someone hands C-3PO a phrase code — "FetchingSchema" — and he looks it up
    // in his six-million-language database and returns the matching human string.
    // If the code isn't in his database he wraps it in "!" so you know the
    // translation failed without throwing an exception in your face.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given key from the resource bundle.
     * If the key is not found, returns {@code !key!} so the caller can see
     * exactly which key is missing without crashing.
     *
     * @param key  the message key to look up — must not be null
     * @return     the localised string, or {@code !key!} if not found
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
