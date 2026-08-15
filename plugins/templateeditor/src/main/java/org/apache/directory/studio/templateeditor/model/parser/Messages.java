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
package org.apache.directory.studio.templateeditor.model.parser;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO ON DUTY IN THE PARSER CORRIDOR ──────────────────────
// C-3PO stands at the door to the parser package, translating message keys into
// human-readable strings for error messages and UI labels. If a key is missing
// from his databanks, he wraps it in "!" so you know exactly which translation
// failed. We never construct him directly — he's always on duty as a static helper.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static message-lookup helper for the {@code model.parser} package. Delegates all
 * key-to-string translations to the parser's {@code messages.properties} resource
 * bundle. Returns {@code !key!} for any missing key so errors are visible rather
 * than silent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.templateeditor.model.parser.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── CONSTRUCTOR: PRIVATE — C-3PO NEVER SLEEPS ────────────────────────────────
    /**
     * Private constructor — this class is a pure static utility and should never
     * be instantiated.
     */
    private Messages()
    {
    }


    // ── GET STRING: TRANSLATE A MESSAGE KEY ──────────────────────────────────────
    // C-3PO consults his resource bundle. If the key exists, he returns the
    // translated string. If not, he marks it with "!" delimiters so we know
    // exactly which phrase is untranslated.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given message key. Returns
     * {@code !key!} if the key is not found in the resource bundle.
     *
     * @param key  the message key defined in the parser's {@code messages.properties}
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
