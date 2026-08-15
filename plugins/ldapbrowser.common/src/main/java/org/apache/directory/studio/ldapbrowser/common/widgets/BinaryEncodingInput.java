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


import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;


// ── CLASS: BinaryEncodingInput — HAN SOLO PACKING CARGO AT MOS EISLEY ────────
// Han stands in the Falcon's hold eyeing a suspicious crate of binary data.
// Does he ignore it, seal it in Base64 crates, or stamp it in hex markings?
// BinaryEncodingInput locks that choice to exactly those three options — no improvising.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A pre-configured {@link OptionsInput} for choosing how to encode binary LDAP attribute values.
 * The three options are fixed: Ignore, BASE-64, and HEX — no free-form custom input is allowed.
 * Think of this class as Han Solo's cargo manifest for the Falcon: you pick a packing
 * strategy from a locked list, and the default is always "Ignore" (leave the crate as-is).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BinaryEncodingInput extends OptionsInput
{

    // ── HAN CHECKS THE CARGO MANIFEST ────────────────────────────────────────────
    // Han eyes the hold in Docking Bay 94 — the binary shipment is already loaded.
    // He runs down the options with Chewie: ignore it, Base64 it, or hex-stamp it.
    // We pass those fixed choices straight through to OptionsInput and set the initial pick.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a BinaryEncodingInput pre-loaded with the three fixed binary-encoding choices.
     * Delegates straight to {@link OptionsInput} — we're just supplying the cargo manifest.
     *
     * <p>For example — Han reviews the options with Chewie before departure:</p>
     * <pre>
     *   "Okay pal, three choices for the binary crate: ignore it, Base64 it, or hex it."
     *   "Default's Ignore — safest option. Warm up the engines."
     * </pre>
     *
     * @param initialRawValue  The encoding to pre-select when the widget opens; should be
     *                         one of the {@link BrowserCoreConstants} BINARYENCODING_* int
     *                         constants converted to a String — falls back to Ignore if unknown.
     * @param asGroup          When {@code true}, wraps the radios in a labeled SWT Group border
     *                         so the control looks like a titled section rather than floating buttons.
     */
    public BinaryEncodingInput( String initialRawValue, boolean asGroup )
    {
        super(
            Messages.getString( "BinaryEncodingInput.BinaryEncoding" ), getDefaultDisplayValue(), getDefaultRawValue(), getOtherDisplayValues(), //$NON-NLS-1$
            getOtherRawValues(), initialRawValue, asGroup, false );

    }


    // ── HAN NAMES THE DEFAULT PLAN ────────────────────────────────────────────────
    // Han shrugs — "If in doubt, just ignore it." That's always the default cargo handling.
    // He tells Chewie to stamp "Ignore" on the manifest header.
    // We return the localized display label for the default "Ignore" option.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable label for the default encoding choice, always "Ignore".
     * This is what gets displayed next to the default radio button in the UI.
     *
     * <p>For example — Han files the paperwork:</p>
     * <pre>
     *   manifest.defaultLabel = "Ignore";  // "Don't touch the binary data."
     * </pre>
     *
     * @return  The localized display string for the "Ignore" encoding option.
     */
    private static String getDefaultDisplayValue()
    {
        return Messages.getString( "BinaryEncodingInput.Ignore" ); //$NON-NLS-1$
    }


    // ── HAN STAMPS THE CRATE WITH THE INTERNAL CODE ───────────────────────────────
    // Behind the "Ignore" label there's an internal code — a raw integer as a string.
    // Han doesn't show customers the stock number, but the system needs it to match preferences.
    // We return the integer constant for BINARYENCODING_IGNORE converted to a String.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the internal (raw) value for the default "Ignore" encoding option.
     * The raw value is the integer constant {@link BrowserCoreConstants#BINARYENCODING_IGNORE}
     * serialized as a String — it's what gets stored in preferences, not what the user sees.
     *
     * <p>For example — the stock code behind the label:</p>
     * <pre>
     *   rawValue = Integer.toString(BINARYENCODING_IGNORE);  // e.g. "0"
     * </pre>
     *
     * @return  The raw string representation of the BINARYENCODING_IGNORE constant.
     */
    private static String getDefaultRawValue()
    {
        return Integer.toString( BrowserCoreConstants.BINARYENCODING_IGNORE );
    }


    // ── HAN LISTS ALL PACKING OPTIONS ON THE BOARD ───────────────────────────────
    // On the wall of the cargo bay is a board with all recognized packing styles.
    // Han reads them off: Ignore, BASE-64, HEX — those are your choices, pal.
    // We return the user-facing display labels for all three options to populate the combo.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display labels for all available binary-encoding options.
     * These are the human-readable strings that appear in the dropdown combo box.
     * The order matches {@link #getOtherRawValues()} exactly — index 0 in display
     * corresponds to index 0 in raw values.
     *
     * <p>For example — the board on the cargo bay wall:</p>
     * <pre>
     *   options[0] = "Ignore"   // leave binary as-is
     *   options[1] = "BASE-64"  // standard safe encoding for text systems
     *   options[2] = "HEX"      // hex-dump style for debugging
     * </pre>
     *
     * @return  A String array of the three display labels: Ignore, BASE-64, HEX.
     */
    private static String[] getOtherDisplayValues()
    {
        return new String[]
            { Messages.getString( "BinaryEncodingInput.Ignore" ), "BASE-64", "HEX" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    // ── HAN READS THE INTERNAL STOCK CODES FOR EACH OPTION ───────────────────────
    // Each packing style on the board has an internal stock number the warehouse uses.
    // Han reads them off the back of the board — customers see names, the system sees ints.
    // We return the raw integer-as-string constants matching each display label.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the internal (raw) values for all available binary-encoding options.
     * These are the {@link BrowserCoreConstants} integer constants serialized as Strings —
     * they're stored in preferences and compared programmatically, not shown to users.
     *
     * <p>For example — the stock codes behind each label:</p>
     * <pre>
     *   raw[0] = Integer.toString(BINARYENCODING_IGNORE)   // e.g. "0"
     *   raw[1] = Integer.toString(BINARYENCODING_BASE64)   // e.g. "1"
     *   raw[2] = Integer.toString(BINARYENCODING_HEX)      // e.g. "2"
     * </pre>
     *
     * @return  A String array of the three raw encoding constants in display-label order.
     */
    private static String[] getOtherRawValues()
    {
        return new String[]
            { Integer.toString( BrowserCoreConstants.BINARYENCODING_IGNORE ),
                Integer.toString( BrowserCoreConstants.BINARYENCODING_BASE64 ),
                Integer.toString( BrowserCoreConstants.BINARYENCODING_HEX ) };
    }

}
