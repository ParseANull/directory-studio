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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.LdifUtils;


// ── CLASS: LdifValueLineBase — C-3PO DECODES THE VALUE SEGMENT ───────────────
// After reading the line label, C-3PO reads the separator (: :: or :<) and
// the value that follows — then decodes it: plain text stays as-is, double-
// colon triggers Base64 decode, and colon-less triggers a URL file read.
// LdifValueLineBase adds the rawValueType and rawValue fields to
// LdifNonEmptyLineBase, and provides the three getValueAs* methods that
// return the decoded value as a String, byte array, or Object.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for every LDIF line that carries a value
 * (attribute-value, DN, changetype, newrdn, etc.).
 * Extends {@link LdifNonEmptyLineBase} with {@code rawValueType} (the
 * separator: {@code ":"}, {@code "::"}, or {@code ":<"}) and {@code rawValue}
 * (the encoded value string).
 * Provides:
 * <ul>
 *   <li>{@link #getValueAsString()} — decoded UTF-8 value</li>
 *   <li>{@link #getValueAsBinary()} — decoded byte array</li>
 *   <li>{@link #getValueAsObject()} — decoded String or byte[] depending on
 *       value type</li>
 *   <li>Value-type predicates: {@link #isValueTypeSafe()},
 *       {@link #isValueTypeBase64()}, {@link #isValueTypeURL()}</li>
 * </ul>
 * Think of this as C-3PO's value-decoding protocol — he reads the separator
 * to know which codec to apply, then returns the real data.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifValueLineBase extends LdifNonEmptyLineBase
{
    /** The raw value-type separator ({@code ":"}, {@code "::"}, or {@code ":<"}). */
    private String rawValueType;

    /** The raw (possibly Base64-encoded or URL) value. */
    private String rawValue;


    // ── CONSTRUCTORS ──────────────────────────────────────────────────────────
    /**
     * Default constructor for subclass use.
     */
    protected LdifValueLineBase()
    {
    }


    /**
     * Creates a value line with all five raw segments.
     *
     * @param offset        byte offset in the document
     * @param rawLineStart  the keyword or attribute name before the separator
     * @param rawValueType  the separator ({@code ":"}, {@code "::"}, or {@code ":<"})
     * @param rawValue      the (possibly encoded) value text
     * @param rawNewLine    the raw line-ending characters
     */
    public LdifValueLineBase( int offset, String rawLineStart, String rawValueType, String rawValue, String rawNewLine )
    {
        super( offset, rawLineStart, rawNewLine );

        this.rawValueType = rawValueType;
        this.rawValue = rawValue;
    }


    // ── VALUE TYPE ACCESSORS ──────────────────────────────────────────────────
    /**
     * Returns the raw value-type separator, or empty string if null.
     *
     * @return the raw separator string
     */
    public String getRawValueType()
    {
        return getNonNull( rawValueType );
    }


    /**
     * Returns the unfolded form of {@link #getRawValueType()}.
     *
     * @return the unfolded separator string
     */
    public String getUnfoldedValueType()
    {
        return unfold( getRawValueType() );
    }


    // ── VALUE ACCESSORS ───────────────────────────────────────────────────────
    /**
     * Returns the raw (possibly encoded) value string, or empty string if null.
     *
     * @return the raw value string
     */
    public String getRawValue()
    {
        return getNonNull( rawValue );
    }


    /**
     * Returns the unfolded form of {@link #getRawValue()}.
     *
     * @return the unfolded value string
     */
    public String getUnfoldedValue()
    {
        return unfold( getRawValue() );
    }


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Concatenates lineStart + valueType + value + newLine.</p>
     */
    public String toRawString()
    {
        return getRawLineStart() + getRawValueType() + getRawValue() + getRawNewLine();
    }


    /**
     * {@inheritDoc}
     *
     * <p>Unfolds the raw string, normalises the value-type separator to the
     * configured space-after-colon setting, replaces the newline, then
     * re-folds at the configured line width.</p>
     */
    public String toFormattedString( LdifFormatParameters formatParameters )
    {
        String raw = toRawString();
        String unfolded = unfold( raw );

        // Fix for DIRSTUDIO-285: We must take care that we only check
        // the first colon in the line. If there is another :: or :<
        // in the value we must not use that as separator.
        int firstColonIndex = unfolded.indexOf( ":" ); //$NON-NLS-1$
        int firstDoubleColonIndex = unfolded.indexOf( "::" ); //$NON-NLS-1$
        int firstColonLessIndex = unfolded.indexOf( ":<" ); //$NON-NLS-1$

        if ( firstDoubleColonIndex > -1 && firstDoubleColonIndex == firstColonIndex )
        {
            unfolded = unfolded.replaceFirst( "::[ ]*", formatParameters.isSpaceAfterColon() ? ":: " : "::" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else if ( firstColonLessIndex > -1 && firstColonLessIndex == firstColonIndex )
        {
            unfolded = unfolded.replaceFirst( ":<[ ]*", formatParameters.isSpaceAfterColon() ? ":< " : ":<" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else if ( firstColonIndex > -1 )
        {
            unfolded = unfolded.replaceFirst( ":[ ]*", formatParameters.isSpaceAfterColon() ? ": " : ":" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if ( rawNewLine != null )
        {
            int index = unfolded.lastIndexOf( rawNewLine );
            if ( index > -1 )
            {
                unfolded = unfolded.substring( 0, unfolded.length() - rawNewLine.length() );
                unfolded = unfolded + formatParameters.getLineSeparator();
            }
        }

        return fold( unfolded, 0, formatParameters );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when the superclass check passes and both {@code rawValueType}
     * and {@code rawValue} are non-null.</p>
     */
    public boolean isValid()
    {
        return super.isValid() && rawValueType != null && rawValue != null;
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns a message for the first missing segment.</p>
     */
    public String getInvalidString()
    {
        if ( rawValueType == null )
        {
            return "Missing value type ':', '::' or ':<'";
        }
        else if ( rawValue == null )
        {
            return "Missing value";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── DECODED VALUE ACCESSORS ───────────────────────────────────────────────
    /**
     * Returns the decoded value as a {@link String} — Base64-decoded and
     * UTF-8-decoded if the value type is {@code "::"}, URL-file-read if
     * {@code ":<"}, or plain unfolded if {@code ":"}.
     *
     * @return the decoded string value (never {@code null}, may be empty)
     */
    public final String getValueAsString()
    {
        Object o = getValueAsObject();
        if ( o instanceof String )
        {
            return ( String ) o;
        }
        else if ( o instanceof byte[] )
        {
            return LdifUtils.utf8decode( ( byte[] ) o );
        }
        else
        {
            return ""; //$NON-NLS-1$
        }
    }


    /**
     * Returns the decoded value as a byte array.
     *
     * @return the UTF-8 bytes of the value (never {@code null}, may be empty)
     */
    public final byte[] getValueAsBinary()
    {
        Object o = getValueAsObject();
        if ( o instanceof String )
        {
            return LdifUtils.utf8encode( ( String ) o );
        }
        else if ( o instanceof byte[] )
        {
            return ( byte[] ) o;
        }
        else
        {
            return new byte[0];
        }
    }


    /**
     * Returns the decoded value as an Object:
     * <ul>
     *   <li>A {@link String} when the value type is {@code ":"} (safe string).</li>
     *   <li>A {@code byte[]} when the value type is {@code "::"} (Base64) or
     *       {@code ":<"} (URL pointing to a file).</li>
     *   <li>{@code null} on URL read failure or unknown value type.</li>
     * </ul>
     *
     * @return the decoded value, or {@code null}
     */
    public final Object getValueAsObject()
    {
        if ( isValueTypeSafe() )
        {
            return getUnfoldedValue();
        }
        else if ( isValueTypeBase64() )
        {
            return LdifUtils.base64decodeToByteArray( getUnfoldedValue() );
        }
        else if ( isValueTypeURL() )
        {
            FileInputStream fis = null;

            try
            {
                try
                {
                    File file = new File( getUnfoldedValue() );
                    byte[] data = new byte[( int ) file.length()];
                    fis = new FileInputStream( file );
                    fis.read( data );
                    return data;
                }
                finally
                {
                    if ( fis != null )
                    {
                        fis.close();
                    }
                }
            }
            catch ( IOException ioe )
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }


    // ── VALUE TYPE PREDICATES ─────────────────────────────────────────────────
    /**
     * Returns {@code true} if the value type is {@code ":<"} (URL reference).
     *
     * @return {@code true} for URL values
     */
    public boolean isValueTypeURL()
    {
        return getUnfoldedValueType().startsWith( ":<" ); //$NON-NLS-1$
    }


    /**
     * Returns {@code true} if the value type is {@code "::"} (Base64-encoded).
     *
     * @return {@code true} for Base64 values
     */
    public boolean isValueTypeBase64()
    {
        return getUnfoldedValueType().startsWith( "::" ); //$NON-NLS-1$
    }


    /**
     * Returns {@code true} if the value type is a plain {@code ":"} (safe
     * string — not Base64, not URL).
     *
     * @return {@code true} for plain string values
     */
    public boolean isValueTypeSafe()
    {
        return getUnfoldedValueType().startsWith( ":" ) && !isValueTypeBase64() && !isValueTypeURL(); //$NON-NLS-1$
    }

}
