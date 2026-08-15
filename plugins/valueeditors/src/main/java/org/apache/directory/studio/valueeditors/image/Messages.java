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
package org.apache.directory.studio.valueeditors.image;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — R2-D2's Projection System Phrase Module ────────────────
// R2-D2 carries a compact phrase module containing every label his projector needs.
// "Current Image," "Save," "Unsupported Format" — they are all stored in there.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Provides localized strings for the image value editor UI.
 * All user-visible text in the image package is looked up here by key, loaded from the
 * {@code messages.properties} file sitting next to this class in the same package.
 * Think of this class as R2-D2's phrase module: every label the projector displays —
 * titles, status messages, error notices — lives in here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── R2 Looks Up A Phrase By Code Word ────────────────────────────────────────
    // Obi-Wan speaks a code word and R2 plays back the matching phrase from his module.
    // If R2 does not recognize the code, he projects "!key!" so the team knows something is missing.
    // We look the key up in the ResourceBundle and fall back to the bracketed key on a miss.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Retrieves the localized string associated with the given key from the image package's
     * {@code messages.properties} resource bundle.
     * If the key is missing — a bug meaning someone added UI text without a bundle entry —
     * we return the key itself wrapped in exclamation marks so it stands out during testing.
     *
     * <p>For example — Obi-Wan requests a phrase and R2 plays it back:</p>
     * <pre>
     *   Obi-Wan: "ImageDialog.CurrentImage"
     *   R2: "Current Image"
     *   Unknown code: R2 projects "!ImageDialog.CurrentImage!"
     * </pre>
     *
     * @param key the message key defined in {@code messages.properties}
     * @return the localized string, or {@code "!" + key + "!"} if the key is absent
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
