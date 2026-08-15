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


import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.LdifUtils;
import org.apache.directory.studio.ldifparser.model.LdifPart;


// ── CLASS: LdifLineBase — C-3PO READS A SINGLE TRANSMISSION LINE ─────────────
// C-3PO reads one line of the incoming communiqué: he knows its byte position
// in the scroll, whether it ends with a proper line terminator, and how to
// print it back out as either the raw original or a cleanly re-formatted
// version with the correct line separator and folded/unfolded line wrapping.
// LdifLineBase is the base class for every single LDIF line: it stores the
// offset and the raw newline, implements the fold/unfold logic as protected
// static helpers, and delegates raw and formatted serialisation to subclasses.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for every single LDIF line.
 * Stores the byte {@code offset} and the raw newline string
 * ({@code rawNewLine}), and provides final implementations of
 * {@link #getOffset()}, {@link #getLength()}, {@link #adjustOffset},
 * {@link #toFormattedString}, and {@link #toString()}.
 * Also provides protected static helpers {@link #fold} and {@link #unfold}
 * that handle LDIF line-folding (continuation lines starting with a space).
 * Think of this as C-3PO's per-line protocol: every line knows its position,
 * its newline, and how to serialise itself with correct folding.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class LdifLineBase implements LdifPart
{
    /** The byte offset of the first character of this line in the document. */
    protected int offset;

    /** The raw newline string ({@code "\n"}, {@code "\r\n"}, etc.), or {@code null} if missing. */
    protected String rawNewLine;


    // ── CONSTRUCTORS ──────────────────────────────────────────────────────────
    /**
     * Default constructor for subclass use.
     */
    protected LdifLineBase()
    {
        super();
    }


    /**
     * Creates a line at {@code offset} with the given raw newline.
     *
     * @param offset      the zero-based byte offset of this line in the document
     * @param rawNewLine  the raw line-ending characters, or {@code null} if the
     *                    line is not yet terminated
     */
    protected LdifLineBase( int offset, String rawNewLine )
    {
        this.offset = offset;
        this.rawNewLine = rawNewLine;
    }


    // ── POSITION ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public final int getOffset()
    {
        return offset;
    }


    /**
     * Returns the raw newline string for this line, or an empty string if
     * {@link #rawNewLine} is {@code null}.
     *
     * @return the raw newline string, never {@code null}
     */
    public final String getRawNewLine()
    {
        return getNonNull( rawNewLine );
    }


    /**
     * Returns the unfolded form of {@link #getRawNewLine()}.
     *
     * @return the unfolded newline string
     */
    public String getUnfoldedNewLine()
    {
        return unfold( getRawNewLine() );
    }


    /**
     * {@inheritDoc}
     *
     * <p>Shifts the stored offset by {@code adjust} characters.</p>
     */
    public final void adjustOffset( int adjust )
    {
        offset += adjust;
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns the length of {@link #toRawString()}.</p>
     */
    public final int getLength()
    {
        return toRawString().length();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} when {@link #rawNewLine} is not {@code null}.</p>
     */
    public boolean isValid()
    {
        return rawNewLine != null;
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code "Missing new line"} when {@link #rawNewLine} is null,
     * otherwise {@code null}.</p>
     */
    public String getInvalidString()
    {
        if ( rawNewLine == null )
        {
            return "Missing new line";
        }
        else
        {
            return null;
        }
    }


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Default implementation returns {@link #getRawNewLine()}.  Subclasses
     * prepend their content fields.</p>
     */
    public String toRawString()
    {
        return getRawNewLine();
    }


    /**
     * {@inheritDoc}
     *
     * <p>Unfolds the raw string (removes fold continuations), normalises the
     * line separator to {@code formatParameters.getLineSeparator()}, then
     * returns the result.  Subclasses override to also normalise the
     * value-type separator and re-fold at the configured line width.</p>
     */
    public String toFormattedString( LdifFormatParameters formatParameters )
    {
        String raw = toRawString();
        String unfolded = unfold( raw );

        if ( rawNewLine != null )
        {
            int index = unfolded.lastIndexOf( rawNewLine );
            if ( index > -1 )
            {
                unfolded = unfolded.substring( 0, unfolded.length() - rawNewLine.length() );
                unfolded = unfolded + formatParameters.getLineSeparator();
            }
        }

        return unfolded;
    }


    /**
     * Returns a debug-friendly description: class name, offset, length, and
     * the raw text with newlines shown as {@code \\n} / {@code \\r}.
     */
    public final String toString()
    {
        String text = toRawString();
        text = LdifUtils.convertNlRcToString( text ); //$NON-NLS-1$ //$NON-NLS-2$

        return getClass().getName() + " (" + getOffset() + "," + getLength() + "): '" + text + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }


    // ── UTILITY: NULL GUARD ───────────────────────────────────────────────────
    /**
     * Returns {@code s} if non-null, otherwise returns an empty string.
     *
     * @param s  the string to guard
     * @return   {@code s} or {@code ""} if {@code s} is {@code null}
     */
    protected static String getNonNull( String s )
    {
        return s != null ? s : ""; //$NON-NLS-1$
    }


    // ── UTILITY: UNFOLD ───────────────────────────────────────────────────────
    // Remove LDIF line-folding: wherever a newline is immediately followed by
    // a space, discard both characters (they are the fold continuation marker).
    /**
     * Removes LDIF line-folding from {@code s}.  A newline ({@code \n},
     * {@code \r}, or {@code \r\n} / {@code \n\r}) immediately followed by a
     * space character is treated as a line continuation — both the newline and
     * the leading space are discarded.  Other newlines are kept as-is.
     *
     * @param s  the folded LDIF string to unfold
     * @return   the unfolded string
     */
    protected static String unfold( String s )
    {
        char[] newString = s.toCharArray();
        int pos = 0;
        int length = newString.length;

        for ( int i = 0; i < length; i++ )
        {
            char c = newString[i];

            if ( c == '\n' )
            {
                if ( i + 1 < length )
                {
                    switch ( newString[i + 1] )
                    {
                        case ' ':
                            i++;
                            break;

                        case '\r':
                            if ( ( i + 2 < length ) && ( newString[i + 2] == ' ' ) )
                            {
                                i += 2;
                            }
                            else
                            {
                                newString[pos++] = c;
                                newString[pos++] = '\r';
                                i++;
                            }

                            break;

                        default:
                            newString[pos++] = c;
                            break;
                    }
                }
                else
                {
                    newString[pos++] = c;
                }
            }
            else if ( c == '\r' )
            {
                if ( i + 1 < length )
                {
                    switch ( newString[i + 1] )
                    {
                        case ' ':
                            i++;
                            break;

                        case '\n':
                            if ( ( i + 2 < length ) && ( newString[i + 2] == ' ' ) )
                            {
                                i += 2;
                            }
                            else
                            {
                                newString[pos++] = c;
                                newString[pos++] = '\n';
                                i++;
                            }

                            break;

                        default:
                            newString[pos++] = c;
                            break;
                    }
                }
                else
                {
                    newString[pos++] = c;
                }
            }
            else
            {
                newString[pos++] = c;
            }
        }

        return new String( newString, 0, pos );
    }


    // ── UTILITY: FOLD ─────────────────────────────────────────────────────────
    // Wrap long lines by inserting a newline + leading space every
    // (lineWidth - indent) characters.
    /**
     * Folds {@code value} at {@code formatParameters.getLineWidth()} characters
     * by inserting a {@code getLineSeparator() + " "} continuation sequence.
     * The first line is indented by {@code indent} characters (i.e. the
     * effective first-line width is {@code lineWidth - indent}).
     *
     * @param value            the string to fold
     * @param indent           the number of prefix characters already used on
     *                         the first line
     * @param formatParameters the formatting settings
     * @return                 the folded string
     */
    protected static String fold( String value, int indent, LdifFormatParameters formatParameters )
    {
        StringBuffer formattedLdif = new StringBuffer();
        int offset = formatParameters.getLineWidth() - indent;
        int endIndex = 0 + offset;
        while ( endIndex + formatParameters.getLineSeparator().length() < value.length() )
        {
            formattedLdif.append( value.substring( endIndex - offset, endIndex ) );
            formattedLdif.append( formatParameters.getLineSeparator() );
            formattedLdif.append( ' ' );
            offset = formatParameters.getLineWidth() - 1;
            endIndex += offset;
        }
        String rest = value.substring( endIndex - offset, value.length() );
        formattedLdif.append( rest );

        // return
        return formattedLdif.toString();
    }

}
