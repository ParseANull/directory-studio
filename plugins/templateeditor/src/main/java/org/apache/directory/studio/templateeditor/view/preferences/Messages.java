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
package org.apache.directory.studio.templateeditor.view.preferences;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — PALPATINE'S STANDING-ORDERS TRANSLATION DESK ────────────────
// When Palpatine issues standing orders for the preferences package, every label,
// button caption, and error message is looked up from this translation desk. If a
// key is missing from the resource bundle, the desk marks it with "!" delimiters
// so the missing translation stands out clearly. We never instantiate this class;
// it is always on duty as a static helper.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static message-lookup helper for the {@code view.preferences} package. Delegates
 * all key-to-string translations to the preferences package's
 * {@code messages.properties} resource bundle. Returns {@code !key!} for any
 * missing key so errors are visible rather than silent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.templateeditor.view.preferences.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── CONSTRUCTOR: PRIVATE — ALWAYS ON DUTY ─────────────────────────────────────
    /**
     * Private constructor — this class is a pure static utility and should never
     * be instantiated.
     */
    private Messages()
    {
    }


    // ── GET STRING: TRANSLATE A MESSAGE KEY ──────────────────────────────────────
    // Palpatine's translation desk looks up the key in the resource bundle. If the
    // key is found, the translation is returned; if not, the key is wrapped in "!"
    // so the missing translation is immediately visible.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given message key. Returns
     * {@code !key!} if the key is not found in the resource bundle.
     *
     * @param key  the message key defined in the preferences {@code messages.properties}
     * @return the corresponding string, or {@code !key!} if missing
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
