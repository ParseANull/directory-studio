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
package org.apache.directory.studio.valueeditors.time;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


// ── CLASS: Messages — MON MOTHMA'S MISSION BRIEFING PHRASE BOOK ──────────────
// At Rebel Alliance headquarters, Mon Mothma's mission-briefing console stores
// every label used by the date/time editor — "Time", "Date", "Timezone",
// "Raw Value", "Discard Fraction" — in a sealed phrase book that any terminal
// in the base can consult without hard-coding the wording.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Looks up localised UI strings for the GeneralizedTime value-editor sub-package
 * from its {@code messages.properties} resource bundle.
 * Every label shown in {@link GeneralizedTimeValueDialog} is fetched here so
 * the text can be translated without touching Java source.
 * Think of this as Mon Mothma's mission-briefing phrase book — every timestamp
 * label is indexed and consistent across all terminals in the Rebel base.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages
{
    /** The resource name */
    private static final ResourceBundle RESOURCE_BUNDLE =
        ResourceBundle.getBundle( Messages.class.getPackage().getName() + ".messages" );


    // ── The Rebel Console Looks Up a Label ────────────────────────────────────
    // A navigator asks the console for the label "Timezone".  The console checks
    // the phrase book and returns the localised text.  A missing key earns
    // "!key!" so a translation gap is immediately visible in the UI.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up a UI string from the time sub-package's resource bundle by key.
     * Returns the localised text if found; returns {@code !key!} if the key is
     * missing so that absent translations are immediately visible in the UI.
     *
     * <p>For example — the console retrieves a label:</p>
     * <pre>
     *   String label = Messages.getString("GeneralizedTimeValueDialog.Time");
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
