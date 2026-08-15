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
package org.apache.directory.studio.valueeditors.administrativerole;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — PALPATINE'S ADMINISTRATIVE DECREE LIBRARY ───────────────
// In the Imperial Senate, Palpatine's clerk maintains a bound volume of every
// official decree — each one indexed by a short code so the Senators can look
// up the exact wording on demand.  When a code doesn't exist, the clerk flags
// the gap openly rather than inventing something.
// This class is that decree library for the administrativerole sub-package.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the administrativerole sub-package from its
 * {@code messages.properties} resource bundle.
 * Every label in the AdministrativeRoleDialog is retrieved via this class so
 * the UI can be translated without touching Java code.
 * Think of this class as Palpatine's indexed decree library — any string needed
 * by the administrative role editor is fetched here by key.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── Palpatine's Clerk Looks Up a Decree ──────────────────────────────────
    // A Senator asks for the wording of Administrative Decree #4792.
    // The clerk checks the bound index and reads the text back verbatim.
    // If the decree number doesn't exist, he stamps "!unknown!" on the request
    // form so the gap is obvious rather than silently returning nothing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the administrativerole sub-package's resource bundle by key.
     * Returns the localised text if found; returns {@code !key!} if the key is missing
     * so missing translations are immediately visible in the UI.
     *
     * <p>For example — the clerk retrieves the dialog title:</p>
     * <pre>
     *   String title = Messages.getString(
     *       "AdministrativeRoleDialog.AdministrativeRoleEditor");
     *   // → "Administrative Role Editor"
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
