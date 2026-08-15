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
package org.apache.directory.studio.connection.ui;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S TRANSLATION UNIT (connection.ui) ───────────────────
// C-3PO keeps a huge dictionary so he can say the right thing in the right
// language at the right time.  This class is that dictionary for connection.ui:
// a thin wrapper around a standard Java ResourceBundle that loads the
// "messages.properties" file from the same package as this class.
// When code needs a localised string it calls Messages.getString("SomeKey") and
// gets back the translated value.  If the key is missing (a typo or a forgotten
// entry), we return "!SomeKey!" so it's obvious during testing.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * NLS helper for the {@code connection.ui} package.
 * Loads {@code messages.properties} (and locale-specific variants) from the
 * same package directory.
 *
 * <p>Usage: {@code Messages.getString("SomeKey")}.
 * If the key is not found, the returned string is {@code "!SomeKey!"} so missing
 * translations are easy to spot during development.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    /** The resource bundle loaded from {@code <package>/messages.properties}. */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
        .getBundle( Messages.class.getPackage().getName() + ".messages" );


    /**
     * Prevents instantiation.  All methods are static.
     */
    private Messages()
    {
    }


    // ── GET STRING — LOOK UP A LOCALISED STRING ───────────────────────────────────
    /**
     * Returns the localised string for the given key.
     * If the key is not found in the resource bundle, returns {@code "!key!"} to
     * make the missing translation immediately visible.
     *
     * @param key  The message key defined in {@code messages.properties}.
     * @return  The localised string, or {@code "!key!"} if not found.
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
