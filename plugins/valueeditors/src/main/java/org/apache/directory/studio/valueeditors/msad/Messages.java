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
package org.apache.directory.studio.valueeditors.msad;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;


// ── CLASS: Messages — The Imperial Identification Office Phrase Book ──────────
// The Imperial identification office keeps a phrase book of every standard response
// the checkpoint officers use: "Invalid SID," "Invalid GUID," and so on.
// Every message the msad editors need is catalogued here, indexed by code.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Provides localized strings for the MS AD value editor UI.
 * All user-visible text in the msad package is looked up here by key, loaded from the
 * {@code messages.properties} file sitting next to this class in the same package.
 * Think of this class as the Imperial identification office's phrase book: every standard
 * response the GUID and SID editors display — error notices, labels — is filed here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages extends NLS
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── The Office Looks Up A Standard Response By Code ───────────────────────────
    // A checkpoint officer requests a phrase by its code word and the office hands it back.
    // If the code is not on file, the office returns "!key!" so the gap is immediately visible.
    // We look the key up in the ResourceBundle and fall back to the bracketed key on a miss.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Retrieves the localized string associated with the given key from the msad package's
     * {@code messages.properties} resource bundle.
     * If the key is missing — meaning a new UI message was added without a bundle entry —
     * we return the key wrapped in exclamation marks so the gap stands out during testing.
     *
     * <p>For example — the checkpoint officer requests the "invalid SID" response:</p>
     * <pre>
     *   Request: "InPlaceMsAdObjectSidValueEditor.InvalidSid"
     *   Office: "Invalid SID"
     *   Unknown code: Office returns "!InPlaceMsAdObjectSidValueEditor.InvalidSid!"
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
