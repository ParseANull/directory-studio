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

package org.apache.directory.studio.ldapbrowser.ui.dialogs.properties;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING PROPERTY PAGE LABELS ─────────────────
// C-3PO handles all the text that appears on property pages — entry labels,
// attribute descriptions, bookmark names, and error messages — by looking
// them up in a centralized translation archive rather than hard-coding strings
// across a dozen property page classes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS message bundle accessor for the ldapbrowser.ui dialogs/properties package.
 * Loads the sibling {@code messages.properties} file so all property page classes
 * can retrieve localized strings by key instead of embedding raw text.
 * Think of this class as C-3PO's property-page translation archive — every label
 * and tooltip has a key, and this class hands back the right words.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO PRODUCES THE RIGHT PROPERTY LABEL ───────────────────────────────
    // Leia asks C-3PO what "createTimestamp" means for a human reading the
    // properties panel; he translates it to "Create Timestamp" without any fuss.
    // If the key is missing — say a new property was added but the .properties
    // file wasn't updated — we return !key! so it's glaringly obvious.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by key in the bundled messages.properties file.
     * Missing keys come back as {@code !key!} rather than throwing, keeping
     * property pages functional even when translations are incomplete.
     *
     * <p>For example — C-3PO translates an entry property label:</p>
     * <pre>
     *   getString("EntryPropertyPage.DN") → "Distinguished Name:"
     *   getString("no.such.key")          → "!no.such.key!"
     * </pre>
     *
     * @param key  The message key defined in messages.properties; never null.
     * @return     The localized string, or {@code !key!} if the key is missing.
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
