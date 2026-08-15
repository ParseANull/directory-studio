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
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.HexValueEditor;


// ── CLASS: InPlaceMsAdObjectSidValueEditor — Imperial Officer Validating TK-421 ─
// An Imperial checkpoint officer receives a stormtrooper's binary SID badge and decodes
// it on the spot: revision level, authority code, and a chain of sub-authority numbers —
// producing "S-1-5-21-..." that everyone in the Empire can read and verify.
// This editor does exactly that for Active Directory objectSid: raw bytes in,
// the standard "S-R-I-SA..." string out.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * A read-display value editor for the Microsoft Active Directory {@code objectSid} attribute.
 * Active Directory stores Security Identifiers (SIDs) as variable-length binary structures
 * with a mixed-endian layout: the identifier authority is big-endian, while each
 * sub-authority value is little-endian.
 * We decode all fields and produce the canonical SID string format: {@code S-R-I-SA1-SA2-...}
 * where R is the revision, I is the authority, and SA1..SAn are the sub-authority values.
 * Modification still requires editing the raw hex string directly.
 * Think of this class as the Imperial checkpoint officer: he reads the badge bytes,
 * verifies the structure, and announces the human-readable ID to the garrison.
 *
 * <p>Reference: <a href="http://msdn.microsoft.com/en-us/library/cc230371(PROT.10).aspx">
 * MS-DTYP: SID</a></p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InPlaceMsAdObjectSidValueEditor extends HexValueEditor
{

    // ── The Officer Decodes The Stormtrooper's SID Badge ─────────────────────────
    // At the checkpoint, the officer receives the binary badge, checks it is not blank,
    // and runs it through the SID decoder to produce the readable designation.
    // If raw-values mode is on, the officer steps aside and the hex bytes are shown directly.
    // We delegate to convertToString when the raw value is a byte array in normal mode.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable string for an Active Directory {@code objectSid} attribute value.
     * When raw-values mode is off and the underlying data is a byte array, we call
     * {@link #convertToString(byte[])} to decode the SID binary structure into the canonical
     * {@code S-R-I-SA...} format.
     * In raw-values mode, or when the data is not binary, we fall back to the hex editor's
     * default display.
     *
     * <p>For example — the checkpoint officer reads the stormtrooper's badge:</p>
     * <pre>
     *   Officer receives binary badge data.
     *   Officer decodes: "S-1-5-21-1234567890-123456789-123456789-1001"
     *   Raw-values mode on: the officer steps back and the raw hex is displayed.
     * </pre>
     *
     * @param value the LDAP attribute value to display; may be null
     * @return the SID string in S-R-I-SA format, or the hex editor's default if not applicable
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


    // ── The Officer Reads All Fields Of The SID Badge ─────────────────────────────
    // The officer checks the revision number, counts sub-authority slots, reads the authority
    // code (big-endian), then reads each sub-authority in little-endian order.
    // He composes the full "S-1-5-21-..." designation and hands it to the garrison log.
    // We parse the binary SID structure field by field and build the canonical string.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts raw SID bytes into the canonical Windows SID string {@code S-R-I-SA1-SA2-...}.
     * The binary layout (per MS-DTYP) is:
     * <ul>
     *   <li>byte 0: Revision (1 byte, unsigned)</li>
     *   <li>byte 1: SubAuthorityCount (1 byte, unsigned, max 15)</li>
     *   <li>bytes 2-7: IdentifierAuthority (6 bytes, big-endian)</li>
     *   <li>bytes 8+: SubAuthority array (4 bytes each, little-endian)</li>
     * </ul>
     * We validate the byte array length against the declared sub-authority count before
     * parsing, returning an error string if the structure is malformed.
     *
     * <p>For example — the officer reads each field of TK-421's SID badge:</p>
     * <pre>
     *   Revision: 1. SubAuthorityCount: 5. Authority: 5.
     *   SubAuthorities (little-endian each): 21, 1234567890, ...
     *   Result: "S-1-5-21-1234567890-..."
     * </pre>
     *
     * @param bytes the raw SID bytes from the LDAP attribute; must be at least 8 bytes
     * @return the canonical SID string, or an error message if the input is invalid
     */
    protected String convertToString( byte[] bytes )
    {
        /*
         * The binary data structure, from http://msdn.microsoft.com/en-us/library/cc230371(PROT.10).aspx:
         *   byte[0] - Revision (1 byte): An 8-bit unsigned integer that specifies the revision level of the SID structure. This value MUST be set to 0x01.
         *   byte[1] - SubAuthorityCount (1 byte): An 8-bit unsigned integer that specifies the number of elements in the SubAuthority array. The maximum number of elements allowed is 15.
         *   byte[2-7] - IdentifierAuthority (6 bytes): A SID_IDENTIFIER_AUTHORITY structure that contains information, which indicates the authority under which the SID was created. It describes the entity that created the SID and manages the account.
         *               Six element arrays of 8-bit unsigned integers that specify the top-level authority
         *               big-endian!
         *   and then - SubAuthority (variable): A variable length array of unsigned 32-bit integers that uniquely identifies a principal relative to the IdentifierAuthority. Its length is determined by SubAuthorityCount.
         *              little-endian!
         */

        if ( ( bytes == null ) || ( bytes.length < 8 ) )
        {
            return Messages.getString( "InPlaceMsAdObjectSidValueEditor.InvalidSid" ); //$NON-NLS-1$
        }

        char[] hex = Hex.encodeHex( bytes );
        StringBuffer sb = new StringBuffer();

        // start with 'S'
        sb.append( 'S' );

        // revision
        int revision = Integer.parseInt( new String( hex, 0, 2 ), 16 );
        sb.append( '-' );
        sb.append( revision );

        // get count
        int count = Integer.parseInt( new String( hex, 2, 2 ), 16 );

        // check length
        if ( bytes.length != ( 8 + count * 4 ) )
        {
            return Messages.getString( "InPlaceMsAdObjectSidValueEditor.InvalidSid" ); //$NON-NLS-1$
        }

        // get authority, big-endian
        long authority = Long.parseLong( new String( hex, 4, 12 ), 16 );
        sb.append( '-' );
        sb.append( authority );

        // sub-authorities, little-endian
        for ( int i = 0; i < count; i++ )
        {
            StringBuffer rid = new StringBuffer();

            for ( int k = 3; k >= 0; k-- )
            {
                rid.append( hex[16 + ( i * 8 ) + ( k * 2 )] );
                rid.append( hex[16 + ( i * 8 ) + ( k * 2 ) + 1] );
            }

            long subAuthority = Long.parseLong( rid.toString(), 16 );
            sb.append( '-' );
            sb.append( subAuthority );
        }

        return sb.toString();
    }
}
