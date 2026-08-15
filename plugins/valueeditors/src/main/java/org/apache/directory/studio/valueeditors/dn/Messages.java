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
package org.apache.directory.studio.valueeditors.dn;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO'S CHAIN-OF-COMMAND PHRASE BOOK ───────────────────
// When C-3PO computes the full title and chain of command for a fleet officer,
// he retrieves each term from his diplomatic phrase book — "DN Editor", and so on.
// This class is that phrase book for the DN value-editor sub-package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the DN value-editor sub-package from its
 * {@code messages.properties} resource bundle.
 * Labels used when opening the DN picker dialog are fetched via this class
 * so they can be translated without touching Java code.
 * Think of this as C-3PO's diplomatic phrase book for the DN domain —
 * every label is indexed and consistent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Consults His Phrase Book ────────────────────────────────────────
    // C-3PO needs the label "DN Editor" for the dialog title.  He checks his
    // diplomatic phrase book, finds the entry, and returns it verbatim.
    // A missing entry earns an "!key!" warning tag so nobody is left guessing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the DN sub-package's resource bundle by key.
     * Returns the localised text if found; returns {@code !key!} if the key is
     * missing so that absent translations are immediately visible in the UI.
     *
     * <p>For example — C-3PO retrieves the dialog title:</p>
     * <pre>
     *   String title = Messages.getString("DnValueEditor.DNEditor");
     *   // → "DN Editor"
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
