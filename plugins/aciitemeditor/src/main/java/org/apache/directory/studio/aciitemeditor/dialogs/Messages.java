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
package org.apache.directory.studio.aciitemeditor.dialogs;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — THE ISB BULLETIN BOARD ──────────────────────────────────
// At the Imperial Security Bureau, every label on every form is sourced from a
// single master bulletin board so that translations can be swapped without
// touching the dialog code.
// This class is that bulletin board for the dialogs package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS message lookup utility for the {@code dialogs} package.
 * Loads the {@code messages.properties} resource bundle once and exposes a
 * single static method so dialog classes can fetch localised strings without
 * duplicating try/catch boilerplate.
 * Think of this class as the ISB's master bulletin board: one place for every
 * caption, title, and button label used across the ACI dialogs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── RETRIEVE THE BULLETIN ENTRY ───────────────────────────────────────────
    // An ISB officer looks up a form-field code in the master binder and reads
    // out the official label for that field.
    // If the code is missing, the binder flags it with exclamation marks so
    // the gap is immediately visible.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for {@code key}.
     * Falls back to {@code '!' + key + '!'} if the key has no mapping in the
     * bundle, making missing translations easy to spot during testing.
     *
     * <p>For example — a dialog retrieves its title string:</p>
     * <pre>
     *   String title = Messages.getString("ACIItemDialog.title");
     *   // Returns "ACI Item Editor" from dialogs/messages.properties
     * </pre>
     *
     * @param key  the resource-bundle key for the desired string
     * @return     the localised message, or {@code "!key!"} when the key is absent
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
