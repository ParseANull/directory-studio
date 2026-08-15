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
package org.apache.directory.studio.valueeditors.password;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — IMPERIAL SECURITY PHRASE BOOK ──────────────────────────
// Vader's security terminal at the Death Star's command deck holds every label
// used by the password editor — "Current Password", "Hash Method", "Verify",
// "Authentication Successful" — in a sealed phrase book that any terminal on
// the station can consult without hard-coding the words.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the password value-editor sub-package from
 * its {@code messages.properties} resource bundle.
 * Every label shown in {@link PasswordDialog} (tab names, button text, field
 * labels, status messages) is retrieved through this class so the strings can
 * be translated without changing Java code.
 * Think of this as the Imperial security phrase book — sealed, consistent,
 * and consulted by every widget on the credential terminal.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── The Security Terminal Looks Up a Phrase ───────────────────────────────
    // An officer asks the terminal for the label "Verify".  The terminal checks
    // the phrase book and returns the localised text.  If the code is not in the
    // book, the terminal stamps "!key!" so a missing translation is visible at
    // once rather than silently producing a blank label.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the password sub-package's resource bundle by key.
     * Returns the localised text if found; returns {@code !key!} if the key is
     * missing so that absent translations are immediately visible in the UI.
     *
     * <p>For example — the security terminal retrieves a label:</p>
     * <pre>
     *   String label = Messages.getString("PasswordDialog.Verify");
     *   // → "Verify Password"
     * </pre>
     *
     * @param key  The property key as defined in messages.properties.
     * @return     The localised string, or {@code !key!} if the key is not found.
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
