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
package org.apache.directory.studio.valueeditors.adtime;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — R2-D2'S CLOCK CONSOLE LABEL LIBRARY ───────────────────
// R2-D2's mission clock console needs labels for every panel and field — "Time",
// "Date", "Raw Value", error dialogs for bogus timestamps — and they all live in
// this package's messages.properties file.  R2-D2 fetches them by key whenever
// a panel needs a label, and sounds a warning beep (!key!) if a label is missing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the Active Directory time value-editor
 * sub-package from its {@code messages.properties} resource bundle.
 * Every label used in {@code ActiveDirectoryTimeValueDialog} and
 * {@code ActiveDirectoryTimeValueEditor} comes through here.
 * Think of this class as R2-D2's console label store — he fetches the right
 * text for each instrument panel on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );

    // ── R2-D2 Fetches a Console Label ────────────────────────────────────────
    // A panel on R2-D2's clock console requests the "Time" label text.
    // R2-D2 looks it up in his label dictionary and returns the localised string.
    // If the key is missing, he emits a warning beep — "!key!" — so nobody is
    // left staring at a blank panel wondering what went wrong.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the adtime sub-package's resource bundle by key.
     * Returns the localised text if found; returns {@code !key!} if the key is
     * missing so that absent translations are visible rather than silently blank.
     *
     * <p>For example — R2-D2 retrieves a dialog label:</p>
     * <pre>
     *   String label = Messages.getString(
     *       "ActiveDirectoryTimeValueDialog.Time");
     *   // → "Time"
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
