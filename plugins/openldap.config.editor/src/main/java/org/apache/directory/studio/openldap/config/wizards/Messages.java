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
package org.apache.directory.studio.openldap.config.wizards;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating the Wizard's UI Text ─────────────────
// C-3PO is fluent in over six million forms of communication — and in this case
// he's translating the wizard's English labels and prompts into whatever locale
// the user has configured. Every wizard label, title, and button goes through
// C-3PO's ResourceBundle lookup before it reaches the screen.
// Messages is the standard Eclipse NLS utility for the wizards package:
// it loads the localized .properties file and returns the translated string
// for a given key (or wraps the key in "!...!" if the translation is missing).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS (National Language Support) utility class for the wizards package.
 * Loads the localized message strings from the {@code wizards.properties} resource
 * bundle and returns them by key. Missing keys are returned as {@code "!key!"}.
 * Think of this as C-3PO translating the wizard's UI text into the right locale.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    private static final String BUNDLE_NAME = "org.apache.directory.studio.openldap.config.wizards"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );


    private Messages()
    {
    }


    // ── getString — C-3PO Translates a Key into the Localized String ──────────────
    // C-3PO looks up the given key in his phrase-book (the ResourceBundle) and
    // returns the localized translation. If the key is missing — a gap in his
    // training data — he returns "!key!" so it's obviously wrong rather than silently
    // blank.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized message string for the given key.
     * If the key is not found in the resource bundle, returns {@code "!key!"} so
     * missing translations are visible rather than silently empty.
     *
     * <p>For example — C-3PO translates a UI key:</p>
     * <pre>
     *   String title = Messages.getString( "NewOpenLdapConfigurationFileWizard.title" );
     *   // "New OpenLDAP Configuration File" (or "!key!" if missing)
     * </pre>
     *
     * @param key  the NLS message key from the wizards.properties resource bundle
     * @return     the localized string, or "!key!" if the key is not found
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
