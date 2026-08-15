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
package org.apache.directory.studio.valueeditors.address;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FOR THE ADDRESS CREW ─────────────────
// Aboard the Falcon, C-3PO listens to whatever alien dialect the address port
// control tower is using and converts it to plain Galactic Basic for the crew.
// When he doesn't recognise a phrase, he admits it loudly with "!unknown!" so
// nobody assumes silence means success.
// This class does the same for the address sub-package: it fetches localised
// strings from the address/messages.properties resource file.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the address value-editor sub-package from its
 * {@code messages.properties} resource bundle.
 * Every label and button caption used in the AddressDialog lives in that file;
 * this class retrieves them at runtime by key.
 * Think of this class as C-3PO's address-specific phrasebook — he pulls out
 * exactly the right translation for each line of dialog.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Checks the Address Phrasebook ──────────────────────────────────
    // C-3PO is asked to translate the street name on the cargo manifest.
    // He flips to the right page in his address dialect module and reads it back.
    // If the phrase is missing, he flags it as "!unknown!" so nobody is misled.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the address sub-package's resource bundle by key.
     * If the key is found we return the localised text; if it's missing we return
     * {@code !key!} — a deliberate visible marker so a missing translation is
     * immediately obvious rather than silently blank.
     *
     * <p>For example — C-3PO retrieves the dialog title string:</p>
     * <pre>
     *   String title = Messages.getString("AddressDialog.AddressEditor");
     *   // → "Address Editor"
     *   String bad   = Messages.getString("Nonexistent.Key");
     *   // → "!Nonexistent.Key!"
     * </pre>
     *
     * @param key  The property key, exactly as it appears in messages.properties.
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
