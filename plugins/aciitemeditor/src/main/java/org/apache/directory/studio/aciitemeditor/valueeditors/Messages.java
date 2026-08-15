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
package org.apache.directory.studio.aciitemeditor.valueeditors;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — THE ISB TERMINAL LABEL REGISTRY ─────────────────────────
// Each ISB data-entry terminal displays button labels and field captions pulled
// from a central registry so that the entire terminal bank can be relabelled
// in one place without rewiring every terminal.
// This class is that registry for the valueeditors package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS message lookup utility for the {@code valueeditors} package.
 * Each value-editor dialog (attribute type, subtree specification, filter, etc.)
 * calls {@link #getString(String)} instead of hard-coding its button captions and
 * field labels, keeping localisation central.
 * Think of this class as the ISB terminal label registry: one lookup, one label,
 * zero duplicated strings across the value-editor suite.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── FETCH THE TERMINAL LABEL ──────────────────────────────────────────────
    // An ISB technician queries the central registry with a terminal code and
    // receives the official label text to display on that terminal.
    // If the registry has no entry for that code, it returns a bracketed sentinel
    // so broken terminals are immediately obvious during inspection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for {@code key}.
     * Falls back to {@code '!' + key + '!'} if the key is missing from the bundle,
     * providing a visible sentinel during development.
     *
     * <p>For example — a dialog fetches its "Browse" button label:</p>
     * <pre>
     *   String label = Messages.getString("AttributeTypeDialog.button.browse");
     *   // Returns "Browse..." from valueeditors/messages.properties
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
