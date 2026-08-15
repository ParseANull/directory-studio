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
package org.apache.directory.studio.connection.ui.dialogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S TRANSLATION UNIT (DIALOGS) ─────────────────────────
// C-3PO is fluent in over six million forms of communication.  This class is our
// C-3PO for the dialogs sub-package: it loads a .properties resource bundle keyed
// to this package and hands back the human-readable string for any given message
// key.  If a key has no translation, we return '!key!' so it stands out in the UI
// rather than silently showing nothing.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Message-lookup helper for the {@code connection.ui.dialogs} package.
 *
 * <p>Loads the {@code messages.properties} resource bundle co-located with this
 * class.  Call {@link #getString(String)} with a dot-separated message key to get
 * the corresponding localised string.  An unknown key returns {@code '!key!'} so
 * it's immediately visible in the UI rather than silently empty.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    // ── RESOURCE BUNDLE ───────────────────────────────────────────────────────────
    /** The resource bundle for the dialogs package messages. */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── CONSTRUCTOR — PRIVATE UTILITY CLASS ───────────────────────────────────────
    /**
     * Private constructor — this class is a pure utility class and should never
     * be instantiated.
     */
    private Messages()
    {
    }


    // ── GET STRING ────────────────────────────────────────────────────────────────
    /**
     * Looks up the localised string for the given resource bundle key.
     *
     * <p>If the key is not found (i.e. the .properties file is missing the entry)
     * we return {@code '!key!'} so the missing translation is immediately visible
     * in the UI rather than silently empty.</p>
     *
     * @param key  The message key as defined in {@code messages.properties}.
     * @return The corresponding string, or {@code '!key!'} if the key is missing.
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
