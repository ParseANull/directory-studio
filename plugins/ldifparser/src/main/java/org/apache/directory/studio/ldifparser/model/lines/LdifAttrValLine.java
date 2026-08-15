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

package org.apache.directory.studio.ldifparser.model.lines;


import org.apache.directory.studio.ldifparser.LdifParserConstants;
import org.apache.directory.studio.ldifparser.LdifUtils;


// ── CLASS: LdifAttrValLine — REBEL COMMUNIQUÉ FIELD LINE ─────────────────────
// Each field in the Rebel communiqué reads "attributeName: value" or
// "attributeName:: base64value" — a label, a separator, and the payload.
// The factory methods automatically choose the right separator and apply
// Base64 encoding when the value contains characters that require it.
// LdifAttrValLine is that field line in the LDIF model, providing
// getRawAttributeDescription/getUnfoldedAttributeDescription accessors and
// two factory methods for safe-string and binary values.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF attribute-value line: {@code attributeName: value} or
 * {@code attributeName:: base64value}.
 * Extends {@link LdifValueLineBase} — the attribute description is the
 * line-start segment, the value type is {@code ":"} or {@code "::"}, and the
 * value is the payload.
 * Use the factory methods {@link #create(String, String)} and
 * {@link #create(String, byte[])} to build lines programmatically; they
 * automatically apply Base64 encoding when required.
 * Think of this as one labelled field in the Rebel communiqué.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifAttrValLine extends LdifValueLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates an attribute-value line with the given raw segments.
     *
     * @param offset               byte offset in the document
     * @param attributeDescripton  the raw attribute description (name)
     * @param valueType            the separator ({@code ":"} or {@code "::"})
     * @param value                the raw (possibly Base64-encoded) value
     * @param newLine              the raw line-ending characters
     */
    public LdifAttrValLine( int offset, String attributeDescripton, String valueType, String value, String newLine )
    {
        super( offset, attributeDescripton, valueType, value, newLine );
    }


    // ── ATTRIBUTE DESCRIPTION ACCESSORS ──────────────────────────────────────
    /**
     * Returns the raw attribute description string (the attribute name before
     * the separator).
     *
     * @return the raw attribute description
     */
    public String getRawAttributeDescription()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded attribute description string.
     *
     * @return the unfolded attribute description
     */
    public String getUnfoldedAttributeDescription()
    {
        return super.getUnfoldedLineStart();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code "Missing attribute name"} when the unfolded attribute
     * description is empty; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedAttributeDescription().length() == 0 )
        {
            return "Missing attribute name";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY: STRING VALUE ─────────────────────────────────────────────────
    /**
     * Creates an attribute-value line for a String value.  If the value
     * requires Base64 encoding ({@link LdifUtils#mustEncode}), it is encoded
     * automatically.
     *
     * @param name   the attribute name
     * @param value  the string value
     * @return a new {@link LdifAttrValLine}
     */
    public static LdifAttrValLine create( String name, String value )
    {
        if ( LdifUtils.mustEncode( value ) )
        {
            return create( name, LdifUtils.utf8encode( value ) );
        }
        else
        {
            return new LdifAttrValLine( 0, name, ":", value, LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$
        }
    }


    // ── FACTORY: BINARY VALUE ─────────────────────────────────────────────────
    /**
     * Creates an attribute-value line for a binary value using Base64 encoding.
     *
     * @param name   the attribute name
     * @param value  the binary value
     * @return a new {@link LdifAttrValLine} with a {@code "::"} separator
     */
    public static LdifAttrValLine create( String name, byte[] value )
    {
        return new LdifAttrValLine( 0, name, "::", LdifUtils.base64encode( value ), LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$
    }
}
