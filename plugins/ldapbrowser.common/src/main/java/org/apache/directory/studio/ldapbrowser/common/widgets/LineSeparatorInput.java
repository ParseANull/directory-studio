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

package org.apache.directory.studio.ldapbrowser.common.widgets;


import java.util.Iterator;
import java.util.Map;

import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.eclipse.core.runtime.Platform;


// ── CLASS: LineSeparatorInput — HAN SOLO PLOTTING THE KESSEL RUN ─────────────
// Han punches in the Kessel Run coordinates — but every planet in the system uses
// a different navigation protocol: Unix uses a single newline (\n), Windows uses
// carriage-return+newline (\r\n), and old Mac systems used just \r.
// LineSeparatorInput lets you pick which protocol your export file will speak.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A pre-configured {@link OptionsInput} for selecting the line separator to use in
 * exported files.
 * The default is always the platform's own line separator (whatever OS Directory Studio
 * is running on). The drop-down lists all line separators known to the Eclipse
 * {@link Platform} — typically Unix ({@code \n}), Windows ({@code \r\n}), and old Mac ({@code \r}).
 * Think of this as Han Solo choosing a hyperspace route: he defaults to the shortest path
 * from his current position, but can switch to whatever protocol the destination expects.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LineSeparatorInput extends OptionsInput
{

    // ── HAN LOCKS IN THE DEFAULT HYPERSPACE ROUTE ─────────────────────────────────
    // Han checks his nav computer — the Falcon knows the standard route out of the current system.
    // He notes it on the manifest and loads the full list of known routes for the drop-down.
    // We pass the platform's line separator as default and all known separators as alternatives.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a LineSeparatorInput pre-configured with the platform's default line separator
     * as the default option and every separator known to {@link Platform#knownPlatformLineSeparators()}
     * in the drop-down list.
     *
     * <p>For example — Han plots from Mos Eisley:</p>
     * <pre>
     *   "Default route: Unix (\n) — that's what this system runs on."
     *   "Alternatives: Windows (\r\n), old Mac (\r) — in case the destination needs them."
     * </pre>
     *
     * @param initialRawValue  The line-separator string to pre-select (e.g. {@code "\n"});
     *                         falls back to the platform default if not recognized.
     * @param asGroup          When {@code true}, wraps the control in a labeled SWT Group border.
     */
    public LineSeparatorInput( String initialRawValue, boolean asGroup )
    {
        super(
            Messages.getString( "LineSeparatorInput.LineSeparator" ), getDefaultDisplayValue(), getDefaultRawValue(), getOtherDisplayValues(), //$NON-NLS-1$
            getOtherRawValues(), initialRawValue, asGroup, false );

    }


    // ── HAN READS THE CURRENT SYSTEM'S ROUTE NAME FROM THE DISPLAY ───────────────
    // The nav computer shows "Unix (\n)" as the current system's standard route.
    // Han reads the label and formats it with the escape-sequence notation for clarity.
    // We search the platform separator map for the entry matching the default raw value.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display label for the platform's default line separator.
     * Searches {@link Platform#knownPlatformLineSeparators()} for the entry whose value
     * matches the default raw separator, then formats it as "Unix (\n)" or "Windows (\r\n)".
     * If no match is found (shouldn't happen in practice), the raw separator is returned.
     *
     * <p>For example — Han reads the route name on the display:</p>
     * <pre>
     *   defaultRaw = "\n"
     *   mapEntry   = "Unix" → formatted as "Unix (\n)"
     *   return     = "Unix (\\n)"
     * </pre>
     *
     * @return  A formatted display label for the default line separator, e.g. {@code "Unix (\n)"}.
     */
    private static String getDefaultDisplayValue()
    {
        Map lsMap = Platform.knownPlatformLineSeparators();
        for ( Iterator iter = lsMap.keySet().iterator(); iter.hasNext(); )
        {
            String k = ( String ) iter.next();
            String v = ( String ) lsMap.get( k );
            if ( v.equals( getDefaultRawValue() ) )
            {
                k = k + " (" + ( v.replaceAll( "\n", "\\\\n" ).replaceAll( "\r", "\\\\r" ) ) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                return k;
            }
        }
        return getDefaultRawValue();
    }


    // ── HAN READS THE RAW COORDINATES FOR THE DEFAULT ROUTE ──────────────────────
    // The nav computer stores the actual separator characters — not the pretty name.
    // Han needs the real bytes, not the marketing label, to plug into the hyperdrive.
    // We return BrowserCoreConstants.LINE_SEPARATOR, the actual separator for this platform.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the actual line-separator character(s) for the current platform.
     * This is the raw value stored in preferences — the literal {@code "\n"}, {@code "\r\n"},
     * or {@code "\r"} string that gets written into exported files.
     *
     * <p>For example — the nav computer's raw coordinates:</p>
     * <pre>
     *   rawValue = BrowserCoreConstants.LINE_SEPARATOR;  // e.g. "\n" on Unix
     * </pre>
     *
     * @return  The platform's default line-separator string from {@link BrowserCoreConstants}.
     */
    private static String getDefaultRawValue()
    {
        return BrowserCoreConstants.LINE_SEPARATOR;
    }


    // ── HAN LISTS ALL AVAILABLE ROUTES WITH THEIR JUMP SIGNATURES ────────────────
    // The nav computer displays every known hyperspace lane with its readable name and
    // the actual escape-sequence code so pilots know exactly what they're selecting.
    // We format all platform separator names as "Name (\code)" for the drop-down display.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display labels for all platform-known line separators.
     * Each label is formatted as "Name (\n)" or "Name (\r\n)" so users can see both
     * the friendly name and the actual characters.
     * The order matches {@link #getOtherRawValues()} — index i in display = index i in raw.
     *
     * <p>For example — the nav computer's route list:</p>
     * <pre>
     *   displayValues[0] = "Unix (\\n)"
     *   displayValues[1] = "Windows (\\r\\n)"
     *   displayValues[2] = "Mac OS (\\r)"
     * </pre>
     *
     * @return  An array of formatted display strings for all known line separators.
     */
    @SuppressWarnings("unchecked")
    private static String[] getOtherDisplayValues()
    {
        Map<String, String> lsMap = Platform.knownPlatformLineSeparators();
        String[] displayValues = lsMap.keySet().toArray( new String[lsMap.size()] );
        for ( int i = 0; i < displayValues.length; i++ )
        {
            displayValues[i] = displayValues[i]
                + " (" //$NON-NLS-1$
                + ( ( ( String ) lsMap.get( displayValues[i] ) ).replaceAll( "\n", "\\\\n" ).replaceAll( "\r", "\\\\r" ) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + ")"; //$NON-NLS-1$
        }
        return displayValues;
    }


    // ── HAN LISTS THE RAW JUMP COORDINATES FOR EVERY ROUTE ───────────────────────
    // Behind each pretty route name are the actual bytes the hyperdrive uses.
    // Han pulls the raw separator characters for each entry in the platform map.
    // We extract the values (not the keys) from the Platform.knownPlatformLineSeparators() map.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the actual line-separator characters for all platform-known separators.
     * These are the raw values stored in preferences when the user makes a selection.
     * The order matches {@link #getOtherDisplayValues()} — same key order from the platform map.
     *
     * <p>For example — the raw jump coordinates:</p>
     * <pre>
     *   rawValues[0] = "\n"    // Unix
     *   rawValues[1] = "\r\n"  // Windows
     *   rawValues[2] = "\r"    // old Mac
     * </pre>
     *
     * @return  An array of raw line-separator strings from {@link Platform#knownPlatformLineSeparators()}.
     */
    @SuppressWarnings("unchecked")
    private static String[] getOtherRawValues()
    {
        Map<String, String> lsMap = Platform.knownPlatformLineSeparators();
        String[] displayValues = lsMap.keySet().toArray( new String[lsMap.size()] );
        String[] rawValues = new String[displayValues.length];
        for ( int i = 0; i < rawValues.length; i++ )
        {
            rawValues[i] = ( String ) lsMap.get( displayValues[i] );
        }
        return rawValues;
    }

}
