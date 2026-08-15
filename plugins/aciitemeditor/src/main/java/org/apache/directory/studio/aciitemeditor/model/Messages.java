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
package org.apache.directory.studio.aciitemeditor.model;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — THE CLEARANCE MANIFEST TRANSLATION TABLE ────────────────
// The Imperial Security Bureau clearance manifest uses code numbers for every
// protected-item and user-class category; a translation table converts those codes
// to human-readable labels for the officers reading the printed form.
// This class is that translation table for the model package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS message lookup utility for the {@code model} package.
 * Provides localised display names for {@link ProtectedItemWrapper} and
 * {@link UserClassWrapper} instances so the table viewer can show readable
 * labels rather than raw ACI syntax strings.
 * Think of this class as the ISB translation table: code {@code "entry"}
 * becomes the label "Entry" in whatever language the officer is reading.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── DECODE THE CATEGORY LABEL ─────────────────────────────────────────────
    // An ISB officer scans the code printed on the manifest row against the
    // translation table and reads out the full label.
    // If the code is unknown, the table returns a bracketed warning so nothing
    // silently passes without notice.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised string for {@code key}.
     * Falls back to {@code '!' + key + '!'} if the key is absent from the bundle,
     * making gaps visible during development.
     *
     * <p>For example — a wrapper retrieves its display name:</p>
     * <pre>
     *   String label = Messages.getString("ProtectedItemWrapper.entry");
     *   // Returns "Entry" from model/messages.properties
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
