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


import java.nio.charset.Charset;

import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;


// ── CLASS: FileEncodingInput — C-3PO IN JABBA'S THRONE ROOM ──────────────────
// C-3PO stands in Jabba's court ready to translate into any of six million known
// communication forms — he picks the right language for whoever is listening.
// FileEncodingInput does the same: it populates itself with every charset the JVM
// knows about and lets the user pick the right one for their file.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A pre-configured {@link OptionsInput} for selecting the character encoding (charset)
 * to use when reading or writing a file.
 * The default is always the platform's own encoding (e.g. UTF-8 on most modern systems).
 * The full drop-down list is populated from {@link Charset#availableCharsets()} — every
 * encoding the JVM knows about, which can be quite a list.
 * Think of this as C-3PO in Jabba's palace: he defaults to the local dialect, but has
 * six million alternatives ready if you need something more exotic.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FileEncodingInput extends OptionsInput
{

    // ── C-3PO REPORTS FOR DUTY ────────────────────────────────────────────────────
    // C-3PO straightens up in Jabba's court and announces the default translation mode.
    // He consults his internal database to list every other language he can handle.
    // We pass the platform's default charset plus the full availableCharsets() list to OptionsInput.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a FileEncodingInput pre-loaded with the platform's default charset as the default
     * option and every charset from {@link Charset#availableCharsets()} in the drop-down list.
     * No free-form custom input is allowed — you must pick from the known charsets.
     *
     * <p>For example — C-3PO prepares for translation duty:</p>
     * <pre>
     *   "I am fluent in over six million forms of communication."
     *   "Default dialect: UTF-8 (platform standard)."
     *   "Alternatives: all of Charset.availableCharsets()."
     * </pre>
     *
     * @param initialRawValue  The charset name to pre-select (e.g. {@code "UTF-8"}); if it doesn't
     *                         match any known charset, the default platform encoding is used instead.
     * @param asGroup          When {@code true}, wraps the widget in a labeled SWT Group border.
     */
    public FileEncodingInput( String initialRawValue, boolean asGroup )
    {
        super(
            Messages.getString( "FileEncodingInput.FileEncoding" ), getDefaultDisplayValue(), getDefaultRawValue(), getOtherDisplayValues(), //$NON-NLS-1$
            getOtherRawValues(), initialRawValue, asGroup, false );

    }


    // ── C-3PO ANNOUNCES THE LOCAL DIALECT ────────────────────────────────────────
    // Before listing alternatives, C-3PO declares the local language for the court.
    // He looks up the platform's default charset name and formats it nicely for display.
    // We delegate to getCharsetDisplayValue() using the default raw value.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display label for the platform's default charset.
     * Used as the label next to the default radio button in the widget.
     *
     * <p>For example — C-3PO announces the local dialect:</p>
     * <pre>
     *   "The local tongue is: UTF-8."
     *   getCharsetDisplayValue("UTF-8") → "UTF-8" (or its full display name)
     * </pre>
     *
     * @return  The display name of the platform's default charset.
     */
    private static String getDefaultDisplayValue()
    {
        return getCharsetDisplayValue( getDefaultRawValue() );
    }


    // ── C-3PO LOOKS UP THE PLATFORM'S INTERNAL CODE ──────────────────────────────
    // Every language in C-3PO's database has an internal ID the protocol droids use.
    // He reads the platform's default encoding ID — the raw charset name stored in prefs.
    // We return BrowserCoreConstants.DEFAULT_ENCODING, which holds the JVM's default charset name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw charset name for the platform's default encoding.
     * This is the string stored in preferences and compared programmatically —
     * it's the canonical name as returned by {@link BrowserCoreConstants#DEFAULT_ENCODING}.
     *
     * <p>For example — C-3PO's internal reference ID:</p>
     * <pre>
     *   rawValue = BrowserCoreConstants.DEFAULT_ENCODING;  // e.g. "UTF-8"
     * </pre>
     *
     * @return  The canonical charset name of the platform's default encoding.
     */
    private static String getDefaultRawValue()
    {
        return BrowserCoreConstants.DEFAULT_ENCODING;
    }


    // ── C-3PO READS OUT HIS LIST OF AVAILABLE LANGUAGES ──────────────────────────
    // C-3PO recites the display name for every language in his database.
    // He takes each raw charset ID, looks it up, and returns its official display name.
    // We map each raw value from getOtherRawValues() through getCharsetDisplayValue().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display names for all available charsets.
     * The order matches {@link #getOtherRawValues()} — index i in the display array
     * corresponds to index i in the raw array.
     *
     * <p>For example — C-3PO lists the languages:</p>
     * <pre>
     *   displayValues[0] = Charset.forName("Big5").displayName();
     *   displayValues[1] = Charset.forName("EUC-JP").displayName();
     *   // ... hundreds more ...
     * </pre>
     *
     * @return  An array of display-name strings, one per available charset.
     */
    private static String[] getOtherDisplayValues()
    {
        String[] otherEncodingsRawValues = getOtherRawValues();
        String[] otherEncodingsDisplayValues = new String[otherEncodingsRawValues.length];
        for ( int i = 0; i < otherEncodingsDisplayValues.length; i++ )
        {
            String rawValue = otherEncodingsRawValues[i];
            otherEncodingsDisplayValues[i] = getCharsetDisplayValue( rawValue );
        }
        return otherEncodingsDisplayValues;
    }


    // ── C-3PO PULLS HIS COMPLETE LANGUAGE REGISTRY ───────────────────────────────
    // C-3PO opens his memory banks and extracts every canonical language ID he knows.
    // This is the raw list — internal codes, not the pretty names shown to the user.
    // We call Charset.availableCharsets().keySet() to get every charset name the JVM supports.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the canonical names of all charsets available in this JVM.
     * These raw names (e.g. "UTF-8", "ISO-8859-1") are what get stored in preferences
     * and passed to {@link Charset#forName(String)} — the display names are derived from these.
     *
     * <p>For example — C-3PO's internal language registry:</p>
     * <pre>
     *   rawValues = Charset.availableCharsets().keySet().toArray(new String[0]);
     *   // ["Big5", "EUC-JP", "ISO-8859-1", "UTF-8", ...]
     * </pre>
     *
     * @return  An array of canonical charset names from {@link Charset#availableCharsets()}.
     */
    private static String[] getOtherRawValues()
    {
        String[] otherEncodingsRawValues = ( String[] ) Charset.availableCharsets().keySet().toArray( new String[0] );
        return otherEncodingsRawValues;
    }


    // ── C-3PO TRANSLATES AN INTERNAL CODE TO A READABLE NAME ─────────────────────
    // Given an internal language ID, C-3PO looks it up in his database and returns the
    // human-readable name — or falls back to the raw ID if he doesn't recognize it.
    // We use Charset.forName() to get the display name, with a catch for unknown charsets.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a raw charset name (e.g. "UTF-8") into its human-readable display name.
     * If the charset is not recognized by the JVM, the raw name itself is returned as a fallback
     * so we never show an empty or broken label.
     *
     * <p>For example — C-3PO translates the ID:</p>
     * <pre>
     *   getCharsetDisplayValue("UTF-8") → "UTF-8"
     *   getCharsetDisplayValue("??")    → "??"  (fallback — unknown charset)
     * </pre>
     *
     * @param charsetRawValue  The canonical charset name to look up; must not be {@code null}.
     * @return                 The display name from {@link Charset#displayName()}, or the raw
     *                         value itself if the charset is not found.
     */
    private static String getCharsetDisplayValue( String charsetRawValue )
    {
        try
        {
            Charset charset = Charset.forName( charsetRawValue );
            return charset.displayName();
        }
        catch ( Exception e )
        {
            return charsetRawValue;
        }
    }

}
