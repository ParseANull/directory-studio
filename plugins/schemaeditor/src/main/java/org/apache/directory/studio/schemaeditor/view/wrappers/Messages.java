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
package org.apache.directory.studio.schemaeditor.view.wrappers;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO Reading the Jawa Dialect ─────────────────────────
// In the Jundland Wastes, C-3PO crouches beside the battered Jawa sandcrawler
// and reads a stream of incomprehensible beeps and squeals off a data scroll,
// then turns to Luke: "It says 'All your droids belong to us' — roughly."
// This class is our C-3PO: it takes a raw bundle key (the Jawa gibberish) and
// translates it into a human-readable string from the properties file, so that
// every label, folder name, and tooltip in the wrappers package is properly
// localised rather than hard-coded in English.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Centralises all localised string lookups for the {@code view.wrappers} package.
 * Every display string — folder labels, wrapper toString values — comes through here
 * so we have a single place to plug in the right resource bundle.
 * Think of this as C-3PO standing between the code and the properties file, reading
 * Jawa data scrolls and handing back plain-language translations.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── C-3PO Reads the Jawa Data Scroll ─────────────────────────────────────
    // C-3PO scans the scroll for a specific passage, decodes it, and reads it aloud.
    // If the scroll is torn and the passage is missing, he improvises a polite
    // "I'm afraid I can't read that part" — he never just crashes.
    // getString() does the same: look up the key, fall back gracefully if it's absent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a localised string from the wrappers resource bundle by key.
     * If the key is missing from the bundle — perhaps a properties file was not
     * updated after a rename — we return {@code !key!} instead of throwing, so the
     * UI stays functional and the bad key stands out visibly for the developer to fix.
     *
     * <p>For example — C-3PO translating a Jawa entry from the data scroll:</p>
     * <pre>
     *   String label = Messages.getString( "Folder.AttributeTypes" );
     *   // Returns "Attribute Types" from messages.properties,
     *   // or "!Folder.AttributeTypes!" if that key is not there.
     * </pre>
     *
     * @param key  the properties-file key to look up; must not be {@code null}
     * @return     the localised string, or {@code !key!} if the key is not found in the bundle
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
