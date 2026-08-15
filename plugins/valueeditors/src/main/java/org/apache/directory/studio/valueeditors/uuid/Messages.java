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
package org.apache.directory.studio.valueeditors.uuid;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;


// ── CLASS: Messages — LANDO'S IDENTIFICATION REGISTRY PHRASE BOOK ─────────────
// Lando Calrissian's identification registry at Cloud City holds every label
// used by the UUID editor — specifically the "Invalid UUID" error message shown
// when a 16-byte value can't be formatted into a valid UUID string.
// Think of this as Lando's registry phrase book: compact, one key, one label.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the UUID value-editor sub-package from its
 * {@code messages.properties} resource bundle.
 * Currently the only key is the error label shown when binary UUID bytes cannot
 * be formatted into a valid UUID string.
 * Think of this as Lando's identification registry phrase book — one label,
 * always consistent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages extends NLS
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── Lando's Registry Looks Up an Error Label ──────────────────────────────
    // An officer presents a badge that can't be decoded as a UUID — Lando's
    // registry returns the "Invalid UUID" label so the display is never blank.
    // A missing key earns "!key!" so a translation gap is immediately visible.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the UUID sub-package's resource bundle by key.
     * Returns the localised text if found; returns {@code !key!} if the key is
     * missing so that absent translations are immediately visible in the UI.
     *
     * <p>For example — Lando's registry retrieves the invalid-UUID label:</p>
     * <pre>
     *   String label = Messages.getString("InPlaceUuidValueEditor.InvalidUuid");
     *   // → "(invalid UUID)"
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
