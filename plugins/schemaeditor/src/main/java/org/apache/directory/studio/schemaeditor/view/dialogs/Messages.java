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
package org.apache.directory.studio.schemaeditor.view.dialogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — C-3PO READING THE JAWA DIALECT ─────────────────────────
// On the Tantive IV, C-3PO listens to the Jawas arguing among themselves in their
// rapid-fire, high-pitched dialect and translates each utterance on demand:
// "Oh my — they're saying the hyperdrive motivator is three power converters and
// a bag of ionite." He looks up the key ("hyperdrive.motivator"), finds the right
// phrase in the dialect dictionary, and returns plain galactic standard.
// We do the same: callers pass a dot-separated key; we look it up in the
// {@code messages.properties} resource bundle for this package and return the
// localised string. If the key isn't in the bundle, we wrap it in exclamation marks
// so the missing translation is obviously broken rather than silently empty.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for looking up localised strings from the {@code messages.properties}
 * resource bundle in this package.
 * Every dialog and widget in the {@code view.dialogs} package uses this class to
 * retrieve the text it displays — button labels, error messages, window titles —
 * so that we never hard-code English strings in the Java source.
 * Think of this class as C-3PO: it speaks the bundle's dialect so the rest of the
 * codebase doesn't have to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── Threepio Looks Up the Phrase in His Dictionary ───────────────────────
    // C-3PO flips to the right page in his dialect dictionary, finds the Jawa
    // phrase that matches the key, and reads the galactic-standard translation
    // back to whoever asked. If the page is missing, he flags it with loud
    // exclamation marks so nobody mistakes the gap for real content.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up the localised string for the given key in the {@code messages.properties} bundle.
     * If the key is absent from the bundle — for example, because a translation was accidentally
     * omitted — we return {@code "!key!"} rather than throwing an exception or returning null.
     * That sentinel format makes missing translations immediately visible in the UI without
     * crashing the application.
     *
     * @param key  the dot-separated message key, e.g. {@code "AbstractAliasesDialog.Add"}
     * @return     the localised string for that key, or {@code "!key!"} if the key is not found
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
