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
package org.apache.directory.studio.valueeditors;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO FLUENT IN SIX MILLION FORMS OF COMMUNICATION ────
// C-3PO stands in the Millennium Falcon's hold, ready to translate any alien
// phrase the crew throws at him — he looks up the meaning in his vast internal
// databanks and speaks it back in plain Basic.  If a word isn't in the banks,
// he sheepishly admits it with a "!" marker so nobody is left guessing.
// This class does exactly that: it looks up UI strings by key from the plugin's
// .properties resource bundle and returns the human-readable text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the valueeditors plugin from its
 * {@code messages.properties} resource bundle.
 * Every dialog label, button caption, and error message in this plugin is stored
 * as a key in that file; this class is how the Java code fetches them at runtime.
 * Think of this class as C-3PO — fluent in over six million forms of communication,
 * retrieving the right phrase for the right situation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Checks His Databanks ───────────────────────────────────────────
    // C-3PO receives a request phrase ("What does 'Poodoo' mean?") and silently
    // checks his internal translation matrix for a match.
    // The string key is our "phrase request" and the bundle lookup is the check.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string by its resource-bundle key.
     * If the key exists in {@code messages.properties} we get back the translated
     * text; if it's missing (maybe a typo, maybe a forgotten entry), we return
     * {@code !key!} so it's immediately obvious in the UI that something is wrong.
     *
     * <p>For example — C-3PO translates on demand:</p>
     * <pre>
     *   String label = Messages.getString("AddressDialog.Title");
     *   // → "Edit Postal Address"
     *   String oops  = Messages.getString("Typo.Key");
     *   // → "!Typo.Key!"  (visible error, not a silent blank)
     * </pre>
     *
     * @param key  The dot-separated key exactly as it appears in messages.properties.
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
