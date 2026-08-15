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

package org.apache.directory.studio.entryeditors;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FOR THE CREW ────────────────────────
// C-3PO fluent in over six million forms of communication, reading the right
// phrase out of the protocol archives whenever someone needs a localised label.
// This NLS bundle class does the same: looks up UI strings by key so the rest
// of the package never has hard-coded text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS (National Language Support) helper for the entryeditors package.
 * Reads localised strings from the {@code messages.properties} file in this package.
 * Every user-visible string in this package goes through here so we stay
 * translatable without littering hard-coded English across the codebase.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    /**
     * Looks up and returns the localised string for the given key.
     * If the key is missing from the bundle (typo, missing translation),
     * we return {@code !key!} rather than throwing — a visible but non-fatal
     * signal that a translation is missing.
     *
     * @param key  the message key to look up in the resource bundle
     * @return     the localised string, or {@code !key!} if the key is not found
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
