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

package org.apache.directory.studio.valueeditors.msad;


import org.apache.commons.codec.binary.Hex;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.HexValueEditor;


// ── CLASS: InPlaceMsAdObjectGuidValueEditor — C-3PO Translating A Droid Serial ──
// In the Mos Eisley cantina, C-3PO decodes a cryptic droid serial number from binary
// into a human-readable fleet identifier that the crew can actually understand and use.
// This editor does exactly that for Active Directory objectGUID: raw 16-byte binary
// in, a formatted curly-braced UUID string out — with the MS AD byte-swap applied.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * A read-display value editor for the Microsoft Active Directory {@code objectGUID} attribute.
 * Active Directory stores GUIDs as 16 raw bytes in a mixed-endian layout: the first three
 * components are little-endian and the last two are big-endian, which means we cannot just
 * hex-encode the bytes in order — we have to swap them first.
 * The result is rendered in the curly-braced GUID string syntax:
 * {@code {xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}}.
 * Modification still requires editing the raw hex string directly.
 * Think of this class as C-3PO at his translation console: given an opaque binary serial,
 * he produces the recognizable fleet ID that the rest of the crew can work with.
 *
 * <p>References:</p>
 * <ul>
 *   <li><a href="http://msdn.microsoft.com/en-us/library/dd302644(PROT.10).aspx">MS-DTYP: GUID</a></li>
 *   <li><a href="http://en.wikipedia.org/wiki/Globally_Unique_Identifier">Wikipedia: GUID</a></li>
 *   <li><a href="http://msdn.microsoft.com/en-us/library/cc230316(PROT.10).aspx">Curly Braced GUID Syntax</a></li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InPlaceMsAdObjectGuidValueEditor extends HexValueEditor
{
    // ── C-3PO Translates The Binary Serial For The Crew ───────────────────────────
    // The cantina datapad shows a string of raw bytes — meaningless to everyone but C-3PO.
    // C-3PO decodes it: swaps the byte order on the first three segments, then formats the whole
    // thing as a curly-braced fleet ID the crew can read, copy, and use in queries.
    // If showRawValues is on, we skip translation and let the hex editor show the bytes directly.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable string for an Active Directory {@code objectGUID} attribute value.
     * When raw-values mode is off and the underlying data is a 16-byte array, we call
     * {@link #convertToString(byte[])} to apply the MS AD byte-swap and format the result as a
     * curly-braced GUID.
     * In raw-values mode, or when the data is not binary, we fall back to the hex editor's
     * default display.
     *
     * <p>For example — C-3PO translates a droid serial for the crew:</p>
     * <pre>
     *   Raw bytes: 78 56 34 12 34 12 78 56 ...
     *   C-3PO: "{12345678-1234-5678-...}"
     *   Raw-values mode on: C-3PO steps aside and shows the hex directly.
     * </pre>
     *
     * @param value the LDAP attribute value to display; may be null
     * @return the GUID string in curly-braced syntax, or the hex editor's default if not applicable
     */
    public String getDisplayValue( IValue value )
    {
        if ( !showRawValues() )
        {
            Object rawValue = super.getRawValue( value );

            if ( rawValue instanceof byte[] )
            {
                byte[] bytes = ( byte[] ) rawValue;

                return convertToString( bytes );
            }
        }

        return super.getDisplayValue( value );
    }


    // ── C-3PO Applies The Fleet ID Byte-Swap Protocol ─────────────────────────────
    // The Galactic Empire stores fleet IDs in mixed-endian binary — first three segments
    // are in little-endian order, the last two in big-endian. C-3PO knows the protocol
    // and reorders the bytes before composing the curly-braced identifier string.
    // We encode to hex, pick bytes in the MS AD order, and assemble the UUID with dashes.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a raw 16-byte MS AD GUID into a curly-braced UUID string, applying the
     * mixed-endian byte-swap that Active Directory uses.
     * The first 8 bytes (three GUID components) are stored little-endian, so we reverse
     * their byte order; the last 8 bytes (two components) are big-endian and taken as-is.
     * Returns an error string if the input is null or not exactly 16 bytes.
     *
     * <p>For example — C-3PO reorders the bytes and formats the fleet ID:</p>
     * <pre>
     *   Input: 16 bytes from the AD objectGUID attribute
     *   C-3PO swaps bytes 0-3 (little-endian Data1), bytes 4-5 (Data2), bytes 6-7 (Data3).
     *   Output: "{12345678-1234-5678-abcd-ef0123456789}"
     * </pre>
     *
     * @param bytes the raw GUID bytes from the LDAP attribute; must be exactly 16 bytes
     * @return the formatted GUID string, or an error message if the input is invalid
     */
    String convertToString( byte[] bytes )
    {
        if ((  bytes == null ) || ( bytes.length != 16 ) )
        {
            return Messages.getString( "InPlaceMsAdObjectGuidValueEditor.InvalidGuid" ); //$NON-NLS-1$
        }

        char[] hex = Hex.encodeHex( bytes );
        StringBuffer sb = new StringBuffer();
        sb.append( '{' );
        sb.append( hex, 6, 2 );
        sb.append( hex, 4, 2 );
        sb.append( hex, 2, 2 );
        sb.append( hex, 0, 2 );
        sb.append( '-' );
        sb.append( hex, 10, 2 );
        sb.append( hex, 8, 2 );
        sb.append( '-' );
        sb.append( hex, 14, 2 );
        sb.append( hex, 12, 2 );
        sb.append( '-' );
        sb.append( hex, 16, 4 );
        sb.append( '-' );
        sb.append( hex, 20, 12 );
        sb.append( '}' );

        return Strings.toLowerCase( sb.toString() );
    }
}
