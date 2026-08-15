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
package org.apache.directory.studio.templateeditor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

// ── CLASS: Messages — C-3PO TRANSLATING FOR ORGANIC BEINGS ──────────────────────
// C-3PO is fluent in over six million forms of communication. When a rebel officer
// needs to talk to a Jawa, 3PO looks up the right phrase in his memory banks and
// hands back the translation. This class does the same: given a message key, it
// looks up the human-readable string from the plugin's .properties file so we
// never hard-code UI text into Java source.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility for looking up localized strings in this plugin's message bundle.
 * All user-visible text lives in {@code messages.properties}; callers ask by key
 * and get back the translated phrase. If the key is missing we return a bracketed
 * placeholder so nothing silently goes blank.
 * Think of this class as C-3PO: hand it a key, get back a translation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.templateeditor.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    // ── NO INSTANCES ALLOWED: 3PO IS THE TRANSLATOR, NOT A TEMPLATE ─────────────
    // C-3PO is a unique droid — there is only one of him per ship. We make the
    // constructor private so no one accidentally tries to create a second instance;
    // all access is via the static getString() method.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a pure static utility class, never instantiated.
     * All translations go through the static {@link #getString(String)} method.
     */
    private Messages()
    {
    }


    // ── GET STRING: 3PO LOOKS UP THE PHRASE AND TRANSLATES ───────────────────────
    // An officer hands 3PO a phrase key ("greeting.jawa") and 3PO flips through
    // his memory banks to produce "Utinni!" If he can't find the phrase he says
    // "!greeting.jawa!" so we know exactly what's missing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized string for the given message key, loaded from this
     * plugin's {@code messages.properties} resource bundle.
     * If the key doesn't exist in the bundle (e.g. a typo or missing entry) we
     * return {@code !key!} so it shows up visibly in the UI rather than hiding.
     *
     * <p>For example — C-3PO looks up a translation:</p>
     * <pre>
     *   Officer: "3PO, what does 'PREF_TEMPLATES_PRESENTATION' say in Basic?"
     *   3PO: "It says 'Template presentation mode', sir."
     *   getString("PREF_TEMPLATES_PRESENTATION") does the same.
     * </pre>
     *
     * @param key  the message key from {@code messages.properties}; never {@code null}
     * @return the localized string, or {@code !key!} if the key is missing
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
