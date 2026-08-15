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
package org.apache.directory.studio.schemaeditor.view.search;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FOR R2-D2 IN THE SEARCH WING ────────
// R2-D2 has just plugged into the Death Star's data port and is transmitting
// rapid-fire binary codes that represent search button labels, group headings,
// and error messages — all too fast and cryptic for the humans watching.
// C-3PO stands by, intercepting each code and converting it into plain,
// readable Basic so the Search Page can display the right text to the user.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Translates resource-bundle keys into human-readable UI strings for the
 * {@code view.search} package, loading them from the local
 * {@code messages.properties} file at class-load time.
 * The {@link SearchPage} and related search UI components call
 * {@link #getString} whenever they need a label, placeholder, or error
 * message, keeping all display text in one properties file rather than
 * scattered as hard-coded literals.
 * Think of this as C-3PO at R2-D2's side in the Death Star's computer room:
 * R2 knows exactly which port to plug into; C-3PO knows exactly how to
 * translate what comes back into something humans can read.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── R2 TRANSMITS A SEARCH KEY; C-3PO SPEAKS IT ALOUD ────────────────────
    // R2-D2 chirps out "SearchPage.SearchIn" in a burst of binary.
    // C-3PO checks his database without missing a beat and announces:
    // "Search in:" — exactly the label the Search Page needs for its group box.
    // If C-3PO has no match — perhaps someone added a new key and forgot the
    // properties entry — he wraps it in exclamation marks so the gap is
    // immediately obvious in the rendered UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string in the {@code messages.properties} file by its
     * resource key and returns it to the search UI component that asked for it.
     * Without this, every search widget would embed its own text as Java string
     * literals, making localisation and global text changes a painful find-and-
     * replace exercise across multiple source files.
     * When a key is missing — typo, stale properties file, new key not yet
     * added — we return {@code !key!} so the problem is visible in the UI
     * immediately rather than silently producing a blank label.
     *
     * <p>For example — C-3PO translates R2's search-wing transmissions:</p>
     * <pre>
     *   R2 transmits: "SearchPage.Aliases"
     *   C-3PO returns: "Aliases"
     *
     *   R2 transmits: "SearchPage.NonExistentKey"
     *   C-3PO returns: "!SearchPage.NonExistentKey!"
     * </pre>
     *
     * @param key  the resource-bundle key to look up; must not be {@code null}
     * @return     the localised string for that key, or {@code !key!} if absent
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
