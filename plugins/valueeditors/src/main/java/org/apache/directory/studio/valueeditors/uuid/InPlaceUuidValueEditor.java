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


import org.apache.commons.codec.binary.Hex;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.valueeditors.HexValueEditor;


// ── CLASS: InPlaceUuidValueEditor — LANDO'S CLOUD CITY IDENTIFICATION SCANNER ─
// At Cloud City's entry gate, Lando Calrissian's scanner reads a visitor's
// 16-byte identification chip and converts it to the canonical UUID display
// format: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx".  If the chip already holds
// a valid UUID string it's shown as-is; if it holds raw binary it gets hex-
// encoded and hyphenated; if the chip is malformed (not 16 bytes) the scanner
// stamps "(invalid UUID)".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * In-place value editor for LDAP UUID syntax (OID 1.3.6.1.1.16.1), used by
 * attributes such as {@code entryUUID}.
 * A UUID may be stored either as a plain string in the canonical form
 * {@code "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"} or as raw 16-byte binary.
 * In the table we attempt to display the value in canonical form:
 * <ul>
 *   <li>String values that already match the UUID pattern are shown as-is.</li>
 *   <li>16-byte binary values are hex-encoded and hyphenated.</li>
 *   <li>Anything else is labelled "(invalid UUID)".</li>
 * </ul>
 * In-place editing is not specially validated — the parent {@link HexValueEditor}
 * handles the raw input.
 * Think of this as Lando's Cloud City identification scanner — reads any 16-byte
 * chip and outputs the standard UUID format.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InPlaceUuidValueEditor extends HexValueEditor
{
    private static final String UUID_REGEX = "^[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}$"; //$NON-NLS-1$


    // ── Lando's Scanner Reads the Identification Chip ─────────────────────────
    // In raw-values mode the scanner hands off to the parent hex display.
    // Otherwise it checks the raw bytes: if they decode to a valid UUID string,
    // it shows that string; if the bytes are 16 raw bytes, it hex-encodes them
    // into canonical UUID form; otherwise the parent hex display handles it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the UUID in canonical form {@code "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}.
     * If the underlying bytes decode to a valid UUID string, returns it directly.
     * If they are 16 raw bytes, hex-encodes them via {@link #convertToString(byte[])}.
     * Otherwise falls back to the parent {@link HexValueEditor} display.
     * In raw-values mode the parent display is always used.
     *
     * <p>For example — Lando's scanner reads a chip:</p>
     * <pre>
     *   byte[] chip = ...; // 16 raw bytes
     *   // → "550e8400-e29b-41d4-a716-446655440000"
     * </pre>
     *
     * @param value  The LDAP attribute value containing the UUID (string or binary).
     * @return       The canonical UUID string, or the hex-editor fallback.
     */
    public String getDisplayValue( IValue value )
    {
        if ( !showRawValues() )
        {
            Object rawValue = super.getRawValue( value );

            if ( rawValue instanceof byte[] )
            {
                byte[] bytes = ( byte[] ) rawValue;
                String string = Strings.utf8ToString( bytes );

                if ( string.matches( UUID_REGEX ) || Strings.isEmpty( string ) )
                {
                    return string;
                }
                else
                {
                    return convertToString( bytes );
                }
            }
        }

        return super.getDisplayValue( value );
    }


    // ── Lando's Scanner Formats 16 Raw Bytes as a UUID String ─────────────────
    // 16 raw bytes are hex-encoded to a 32-character sequence and then split
    // with hyphens at the standard UUID group boundaries (8-4-4-4-12).
    // If the byte array is null or not exactly 16 bytes, we return the invalid-
    // UUID label.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a 16-byte array to the canonical UUID string format
     * {@code "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"} (lowercase hex).
     * Returns the "(invalid UUID)" label if {@code bytes} is {@code null} or
     * not exactly 16 bytes long.
     *
     * <p>For example — the scanner formats a raw chip:</p>
     * <pre>
     *   byte[] raw = { 0x55, 0x0e, ... }; // 16 bytes
     *   String uuid = convertToString(raw);
     *   // → "550e8400-e29b-41d4-a716-446655440000"
     * </pre>
     *
     * @param bytes  The 16-byte UUID value to format.
     * @return       The canonical UUID string, or the invalid-UUID label.
     */
    String convertToString( byte[] bytes )
    {
        if ( bytes == null || bytes.length != 16 )
        {
            return Messages.getString( "InPlaceUuidValueEditor.InvalidUuid" ); //$NON-NLS-1$
        }

        char[] hex = Hex.encodeHex( bytes );
        StringBuffer sb = new StringBuffer();
        sb.append( hex, 0, 8 );
        sb.append( '-' );
        sb.append( hex, 8, 4 );
        sb.append( '-' );
        sb.append( hex, 12, 4 );
        sb.append( '-' );
        sb.append( hex, 16, 4 );
        sb.append( '-' );
        sb.append( hex, 20, 12 );

        return Strings.toLowerCase( sb.toString() );
    }
}
