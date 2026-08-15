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
package org.apache.directory.studio.schemaeditor.view.preferences;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING FOR R2-D2 IN THE PREFERENCES CORRIDOR ──
// R2-D2 rolls through the preferences section of the Imperial facility,
// emitting a series of beeps that represent labels, section headings, and
// button captions — raw keys that mean nothing to a human.
// C-3PO follows close behind, converting each beep into a proper phrase so
// the preference page widgets know exactly what text to display.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Translates resource-bundle keys into human-readable UI strings for the
 * {@code view.preferences} package, loading them from the local
 * {@code messages.properties} file at class-load time.
 * Every preference page in this package calls {@link #getString} whenever it
 * needs a label, description, or button caption — keeping all display text in
 * one properties file rather than scattered as hard-coded literals.
 * Think of this as C-3PO accompanying R2-D2: R2 carries the key, C-3PO
 * produces the translation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── R2 BEEPS A PREFERENCES KEY; C-3PO TRANSLATES IT ─────────────────────
    // R2-D2 chirps out "HierarchyViewPreferencePage.LimitLabel" in rapid-fire
    // binary. C-3PO checks his database and returns "Limit label length to:"
    // so the checkbox on screen makes sense to the administrator.
    // If C-3PO has no entry for that beep sequence — perhaps someone added a
    // new preference key but forgot to update the properties file — he wraps
    // the raw key in exclamation marks so the problem is immediately obvious.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string in the {@code messages.properties} file by its
     * resource key and returns it to the preference page that asked for it.
     * Without this, every preference page would have to embed its own text as
     * Java string literals, which makes localisation and consistency nearly
     * impossible to maintain.
     * When a key is not found — typo, stale properties file, new key added
     * without a matching entry — we return {@code !key!} so the gap shows up
     * visibly in the rendered preference page.
     *
     * <p>For example — C-3PO handles a preferences-corridor translation:</p>
     * <pre>
     *   R2 transmits: "HierarchyViewPreferencePage.SecondaryLabel"
     *   C-3PO returns: "Secondary label:"
     *
     *   R2 transmits: "HierarchyViewPreferencePage.NonExistentKey"
     *   C-3PO returns: "!HierarchyViewPreferencePage.NonExistentKey!"
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
