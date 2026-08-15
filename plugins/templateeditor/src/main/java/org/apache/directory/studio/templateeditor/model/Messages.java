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
package org.apache.directory.studio.templateeditor.model;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO ON DUTY IN THE MODEL CORRIDOR ────────────────────────
// C-3PO stands at the junction of the model package, ready to translate any
// message key into its human-readable form. He checks the resource bundle first;
// if the key is missing (perhaps from an Imperial scramble), he wraps it in
// exclamation marks so at least you know something went wrong. We never
// instantiate him — he's always on duty as a static helper.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static message-lookup helper for the {@code model} package. Delegates all
 * key-to-string translations to the {@code messages.properties} resource bundle.
 * Returns {@code !key!} for any missing key so UI strings degrade visibly rather
 * than silently.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.templateeditor.model.messages"; //$NON-NLS-1$

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
    // C-3PO looks up the key in his resource bundle. If he finds it, he returns
    // the translation. If the key is missing, he wraps it in "!" markers so we
    // know exactly which phrase went untranslated.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given message key. Returns
     * {@code !key!} if the key is not found in the resource bundle.
     *
     * @param key  the message key defined in {@code messages.properties}
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
