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
package org.apache.directory.studio.schemaeditor.view.wizards;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Translating For R2-D2 ────────────────────────────
// Aboard the Tantive IV, R2-D2 bleeps something urgent; nobody else understands
// him, so C-3PO steps in: "He says the chances of survival are approximately
// three-thousand-seven-hundred-and-twenty to one."  C-3PO is the translation
// layer between R2's binary squeaks and human-readable English.  This class is
// exactly that: a thin translation layer between message keys (the squeak) and
// human-readable UI strings pulled from the messages.properties bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for looking up internationalised (i18n) strings from the wizard
 * package's {@code messages.properties} resource bundle.
 * Every UI string in this package goes through {@link #getString(String)} so that
 * labels, error messages, and button text can be localised without touching the
 * Java code.
 * Think of this class as C-3PO: given a key (R2's bleep), it returns the
 * human-readable translation from the properties file — and if the key is missing,
 * it wraps the key in exclamation marks so you know something went wrong.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Translates R2's Bleep Into English ─────────────────────────────
    // R2 emits a specific bleep — a key like "ImportSchemasWizard.ErrorNoDirectory" —
    // and C-3PO immediately looks it up in his linguistic database and returns the
    // full human-readable sentence.  If the key isn't in the database, 3PO wraps it
    // in exclamation marks: "!UnknownKey!" — so you know the translation is missing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the UI string for the given message key from the {@code messages.properties}
     * resource bundle and returns it.
     * If no entry exists for the key (which would be a bug — someone forgot to add a
     * translation), we return {@code !key!} so the missing message is immediately
     * visible in the UI rather than silently blank.
     *
     * <p>For example — C-3PO translates R2's bleep:</p>
     * <pre>
     *   R2 bleeps: "ImportSchemasFromOpenLdapWizardPage.ErrorNoDirectorySelected"
     *   3PO checks the linguistic database.
     *   3PO returns: "Please select a directory to import from."
     *   If the key is missing: 3PO says "!ImportSchemasFromOpenLdapWizardPage.ErrorNoDirectorySelected!"
     * </pre>
     *
     * @param key  the message key string, matching an entry in {@code messages.properties};
     *             must not be null.
     * @return     the translated UI string, or {@code !key!} if the key is absent.
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
