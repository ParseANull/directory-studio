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
package org.apache.directory.studio.schemaeditor.view.editors.schema;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO AT THE REBEL ALLIANCE BRIEFING ON YAVIN 4 ────────
// C-3PO stands at the back of the briefing room on Yavin 4, ready to translate
// the tactical display readouts and communications into something every Rebel
// pilot and general in the room can understand — one key word in, one
// clear phrase out.
// This class is our C-3PO for the schema editor package: given an opaque
// property key, it returns the matching human-readable UI string from the
// sibling messages.properties resource bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class that retrieves localized UI strings for the schema editor package.
 * It wraps a {@link ResourceBundle} loaded from the sibling {@code messages.properties}
 * file and provides a single static lookup method used throughout this package.
 * Think of it as C-3PO at the Rebel briefing: you give it a key, it returns
 * the right phrase — and if the key's missing, it returns {@code !key!} so the
 * gap is impossible to miss during testing.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Scans His Tactical Language Banks ───────────────────────────────
    // When General Dodonna asks for a translation, C-3PO consults his internal
    // banks and returns the precise phrase — if the phrase isn't there, he
    // admits it rather than guessing, so no one acts on a mistranslation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the {@code messages.properties} resource bundle by key.
     * We use this throughout the schema editor to keep UI text externalized and
     * translatable. If the key is absent from the bundle, we return {@code !key!}
     * rather than throwing, so the UI degrades gracefully and missing entries
     * are immediately obvious during QA.
     *
     * @param key  the property key to look up, e.g. {@code "SchemaEditorOverviewPage.Overview"}
     * @return     the localized string for that key, or {@code !key!} if the key is absent
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
