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

package org.apache.directory.studio.connection.ui.properties;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S PROPERTIES TRANSLATION UNIT ────────────────────────
// Same translation-unit pattern as every other Messages class in the codebase.
// C-3PO keeps the localised strings for the properties package here.  Unknown
// keys come back wrapped in '!' characters.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for loading localised message strings for the
 * {@code properties} package.
 *
 * <p>Strings are loaded from the {@code messages.properties} resource bundle
 * co-located with this class.  Missing keys return {@code "!<key>!"} so they
 * are immediately visible at runtime.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    // ── RESOURCE BUNDLE ───────────────────────────────────────────────────────────

    /**
     * The resource bundle loaded from the {@code messages.properties} file in
     * the same package as this class.
     */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" ); //$NON-NLS-1$


    // ── CONSTRUCTOR — UTILITY CLASS ───────────────────────────────────────────────
    /**
     * Private constructor — this is a utility class and must not be instantiated.
     */
    private Messages()
    {
    }


    // ── GET STRING ────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for the given message key.
     *
     * <p>Returns {@code "!<key>!"} rather than throwing if the key is absent.</p>
     *
     * @param key The message key defined in {@code messages.properties}.
     * @return The localised string, or {@code "!<key>!"} if the key is missing.
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
