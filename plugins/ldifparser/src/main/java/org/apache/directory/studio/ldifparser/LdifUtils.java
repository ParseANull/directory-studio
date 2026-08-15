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

package org.apache.directory.studio.ldifparser;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;


// ── CLASS: LdifUtils — C-3PO'S ENCODING AND DECODING TOOLKIT ─────────────────
// C-3PO is fluent in over six million forms of communication, which means he
// knows exactly when a DN needs Base64 encoding, when a URL escape is required,
// and how to make a newline printable in a debug log without breaking the
// transmission format.
// LdifUtils is that toolkit: static helpers for UTF-8 encode/decode, Base64
// encode/decode, hex encode, URL encode, "must encode?" checks, and a small
// newline-to-escape-sequence converter for safe debug printing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility methods for LDAP/LDIF encoding and decoding.
 * Covers UTF-8 byte conversion, Base64 encode/decode, hex encoding, URL
 * encoding, "must-encode" checks for LDIF values and DNs, and a helper that
 * converts {@code \n} / {@code \r} to printable escape sequences.
 * Think of this as C-3PO's encoding toolkit — he knows exactly which
 * character set to use for every situation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifUtils
{

    // ── UTF-8 ENCODE A STRING ─────────────────────────────────────────────────
    /**
     * Encodes {@code s} to a UTF-8 byte array.
     * Falls back to the platform default encoding if UTF-8 is somehow not
     * available (should never happen in practice).
     *
     * @param s  the string to encode
     * @return   the UTF-8 byte representation
     */
    public static byte[] utf8encode( String s )
    {
        try
        {
            return s.getBytes( "UTF-8" ); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e )
        {
            return s.getBytes();
        }
    }


    // ── URL-ENCODE A STRING ───────────────────────────────────────────────────
    /**
     * URL-encodes {@code s} using UTF-8 percent-encoding.
     * Returns {@code s} unchanged if UTF-8 is somehow unavailable.
     *
     * @param s  the string to URL-encode
     * @return   the percent-encoded string
     */
    public static String urlEncode( String s )
    {
        try
        {
            return URLEncoder.encode( s, "UTF-8" ); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e )
        {
            return s;
        }
    }


    // ── BASE64-ENCODE A BYTE ARRAY ────────────────────────────────────────────
    /**
     * Encodes {@code b} using Base64 and returns the result as a UTF-8 string.
     * Used to produce the {@code ::}-prefixed values in LDIF output.
     *
     * @param b  the byte array to encode
     * @return   the Base64-encoded string
     */
    public static String base64encode( byte[] b )
    {
        return utf8decode( Base64.encodeBase64( b ) );
    }


    // ── HEX-ENCODE A BYTE ARRAY ───────────────────────────────────────────────
    /**
     * Encodes {@code data} as a lowercase hexadecimal string.
     * Returns {@code null} if {@code data} is {@code null}.
     *
     * @param data  the byte array to encode
     * @return      the hex-encoded string, or {@code null}
     */
    public static String hexEncode( byte[] data )
    {
        if ( data == null )
        {
            return null;
        }

        char[] c = Hex.encodeHex( data );
        String s = new String( c );

        return s;
    }


    // ── UTF-8 DECODE A BYTE ARRAY ─────────────────────────────────────────────
    /**
     * Decodes a UTF-8 byte array to a {@link String}.
     * Falls back to the platform default encoding if UTF-8 is unavailable.
     *
     * @param b  the byte array to decode
     * @return   the decoded string
     */
    public static String utf8decode( byte[] b )
    {
        try
        {
            return new String( b, "UTF-8" ); //$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e )
        {
            return new String( b );
        }
    }


    // ── BASE64-DECODE A STRING TO BYTES ───────────────────────────────────────
    /**
     * Decodes a Base64-encoded string to its raw byte array.
     *
     * @param s  the Base64-encoded string
     * @return   the decoded byte array
     */
    public static byte[] base64decodeToByteArray( String s )
    {
        return Base64.decodeBase64( utf8encode( s ) );
    }


    // ── CHECK WHETHER A DN NEEDS ENCODING ────────────────────────────────────
    /**
     * Returns {@code true} if {@code dn} contains characters that require
     * Base64 encoding in LDIF (see {@link #mustEncode}).
     *
     * @param dn  the distinguished name string to check
     * @return    {@code true} if encoding is required
     */
    public static boolean mustEncodeDN( String dn )
    {
        return mustEncode( dn );
    }


    // ── CHECK WHETHER A VALUE NEEDS ENCODING ─────────────────────────────────
    // LDIF requires Base64 encoding for values that start with space/colon/<,
    // end with space, or contain control characters or non-ASCII.
    /**
     * Returns {@code true} if {@code value} must be Base64-encoded to appear
     * safely in an LDIF file.
     * The triggers are:
     * <ul>
     *   <li>starts with {@code ' '}, {@code ':'}, or {@code '<'}</li>
     *   <li>ends with {@code ' '}</li>
     *   <li>contains {@code \r}, {@code \n}, {@code NUL}, or any code point
     *       above U+007F</li>
     * </ul>
     *
     * @param value  the value string to check
     * @return       {@code true} if Base64 encoding is required
     */
    public static boolean mustEncode( String value )
    {
        if ( ( value == null ) || ( value.length() < 1 ) )
        {
            return false;
        }

        if ( value.startsWith( " " ) || value.startsWith( ":" ) || value.startsWith( "<" ) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        {
            return true;
        }

        if ( value.endsWith( " " ) ) //$NON-NLS-1$
        {
            return true;
        }

        for ( int i = 0; i < value.length(); i++ )
        {
            if ( ( value.charAt( i ) == '\r' ) || ( value.charAt( i ) == '\n' ) || ( value.charAt( i ) == ' ' )
                || ( value.charAt( i ) > '' ) )
            {
                return true;
            }
        }

        return false;
    }


    // ── CONVERT NEWLINES TO ESCAPE SEQUENCES ─────────────────────────────────
    // Makes a string safe to print in debug output without injecting real newlines.
    /**
     * Replaces {@code \n} and {@code \r} characters in {@code s} with their
     * two-character escape-sequence representations ({@code \\n} and
     * {@code \\r}).  Returns an empty string for {@code null} input.
     *
     * @param s  the string to convert
     * @return   the string with newlines replaced by printable escapes
     */
    public static String convertNlRcToString( String s )
    {
        if ( s == null )
        {
            return "";
        }

        // Worth case, the new string is three times bigger
        char[] result = new char[s.length() * 3];
        int pos = 0;

        for ( char c : s.toCharArray() )
        {
            if ( c == '\n' )
            {
                result[pos++] = '\\';
                result[pos++] = 'n';
            }
            else if ( c == '\r' )
            {
                result[pos++] = '\\';
                result[pos++] = 'r';
            }
            else
            {
                result[pos++] = c;
            }
        }

        return new String( result, 0, pos );
    }
}
