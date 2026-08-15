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

package org.apache.directory.studio.ldapbrowser.core.utils;


import org.apache.directory.studio.ldapbrowser.core.model.IValue;


// ── CLASS: LdapFilterUtils — C-3PO ENCODING FILTER VALUES FOR JAWA DIALECT ──
// C-3PO translates outgoing filter terms into LDAP-safe encoding:
// string values go through RFC 2254 escape sequences, binary values become
// a sequence of \XX hex escapes per byte.  Without C-3PO the Jawas (LDAP
// servers) would reject unescaped wildcards, parentheses, and null bytes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility methods for building and encoding LDAP search filters.
 *
 * <p>Think of this as C-3PO translating filter values into the special
 * dialect that LDAP servers require: special characters escaped, binary
 * data hex-encoded per RFC 2254.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapFilterUtils
{

    // ── C-3PO Builds A Filter Assertion From An Entry Value ───────────────────────
    // For string values, C-3PO wraps the RFC 2254-encoded string in (attr=value).
    // For binary values, he converts each byte to a \XX hex escape sequence.
    // The resulting assertion can be pasted directly into an LDAP filter string.
    // Returns a well-formed LDAP filter term ready for use in a search request.
    /**
     * Creates a filter assertion from the given value.
     * String values are RFC 2254-escaped; binary values are hex-encoded.
     *
     * @param value the value to build the filter from
     * @return the filter assertion string, e.g. {@code (cn=Skywalker)} or
     *         {@code (userCertificate=\30\82...)}
     */
    public static String getFilter( IValue value )
    {
        if ( value.isString() )
        {
            return "(" + value.getAttribute().getDescription() + "=" + getEncodedValue( value.getStringValue() ) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else
        {
            StringBuffer filter = new StringBuffer();
            filter.append( "(" ); //$NON-NLS-1$
            filter.append( value.getAttribute().getDescription() );
            filter.append( "=" ); //$NON-NLS-1$

            byte[] bytes = value.getBinaryValue();
            for ( int i = 0; i < bytes.length; i++ )
            {
                int b = ( int ) bytes[i];
                if ( b < 0 )
                {
                    b = 256 + b;
                }
                String s = Integer.toHexString( b );
                filter.append( "\\" ); //$NON-NLS-1$
                if ( s.length() == 1 )
                {
                    filter.append( "0" ); //$NON-NLS-1$
                }
                filter.append( s );
            }

            filter.append( ")" ); //$NON-NLS-1$
            return filter.toString();
        }
    }


    // ── C-3PO Applies RFC 2254 Escape Sequences To A Raw Filter Value ────────────
    // C-3PO knows that five characters must be escaped in LDAP filter values:
    // backslash first (to avoid double-escaping), then NUL, *, (, and ).
    // Each forbidden character is replaced with its \XX hex escape sequence.
    // The resulting string is safe to embed in any LDAP filter assertion.
    /**
     * Encodes the given string value per RFC 2254.
     * Escapes {@code \}, NUL (0x00), {@code *}, {@code (}, and {@code )} as
     * their {@code \XX} hex equivalents.
     *
     * @param value the raw filter value string
     * @return the RFC 2254-encoded value
     */
    public static String getEncodedValue( String value )
    {
        value = value.replaceAll( "\\\\", "\\\\5c" ); //$NON-NLS-1$ //$NON-NLS-2$
        value = value.replaceAll( "" + '\u0000', "\\\\00" ); //$NON-NLS-1$ //$NON-NLS-2$
        value = value.replaceAll( "\\*", "\\\\2a" ); //$NON-NLS-1$ //$NON-NLS-2$
        value = value.replaceAll( "\\(", "\\\\28" ); //$NON-NLS-1$ //$NON-NLS-2$
        value = value.replaceAll( "\\)", "\\\\29" ); //$NON-NLS-1$ //$NON-NLS-2$
        return value;
    }

}
