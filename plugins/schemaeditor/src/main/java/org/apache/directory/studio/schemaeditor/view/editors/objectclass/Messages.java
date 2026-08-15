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
package org.apache.directory.studio.schemaeditor.view.editors.objectclass;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO TRANSLATING AT JABBA'S PALACE ───────────────────
// C-3PO stands in Jabba's throne room, fluent in over six million forms of
// communication, ready to translate whatever cryptic squawks and blurts come
// his way into something the Rebels can actually understand.
// This class is our C-3PO: it looks up opaque string keys from a
// .properties resource bundle and hands back the human-readable message text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class that retrieves localized UI strings for the objectclass editor package.
 * It wraps a {@link ResourceBundle} loaded from the sibling {@code messages.properties}
 * file and provides a single static lookup method used throughout this package.
 * Think of it as C-3PO: you hand it a cryptic key, it hands back something legible.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── C-3PO Looks Up the Right Translation ─────────────────────────────────
    // In Jabba's throne room, when Jabba wants R2's message decoded, C-3PO
    // consults his internal language banks and returns the correct phrasing.
    // If no translation exists, C-3PO admits it — we wrap the key in bangs so
    // developers immediately notice a missing message entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the {@code messages.properties} resource bundle by key.
     * We use this everywhere in the objectclass editor to keep UI text out of
     * Java source and in externalized, translatable property files.
     * If the key is missing, we return {@code !key!} rather than throwing, so the
     * UI degrades gracefully and the missing entry stands out during QA.
     *
     * @param key  the property key to look up, e.g. {@code "ObjectClassEditor.Error"}
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
