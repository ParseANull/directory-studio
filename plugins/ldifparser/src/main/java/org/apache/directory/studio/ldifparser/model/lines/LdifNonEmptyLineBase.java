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


// ── CLASS: LdifNonEmptyLineBase — C-3PO READS A NON-BLANK TRANSMISSION LINE ──
// C-3PO distinguishes between blank separator lines and lines that actually
// carry content (DN, attribute name, comment prefix, keyword, etc.).
// Every non-blank LDIF line starts with a "line start" segment — the keyword
// or label before the colon.  LdifNonEmptyLineBase adds that rawLineStart
// field to LdifLineBase and updates the validity/toRawString logic
// accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract intermediate base class for every non-empty LDIF line (i.e. lines
 * that carry at least a label or keyword prefix).
 * Extends {@link LdifLineBase} with a {@code rawLineStart} field representing
 * the "line-start" segment (attribute name, keyword, or comment prefix).
 * Valid when both {@code rawLineStart} and the inherited {@code rawNewLine}
 * are non-null.
 * Think of this as C-3PO's protocol for content lines — every line must have
 * a non-empty opening segment before the colon.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class LdifNonEmptyLineBase extends LdifLineBase
{

    /** The segment before the separator — e.g. the attribute name or keyword. */
    private String rawLineStart;


    // ── CONSTRUCTORS ──────────────────────────────────────────────────────────
    /**
     * Default constructor for subclass use.
     */
    protected LdifNonEmptyLineBase()
    {
    }


    /**
     * Creates a non-empty line at {@code offset} with the given segments.
     *
     * @param offset        the zero-based byte offset in the document
     * @param rawLineStart  the opening segment (attribute name, keyword, etc.)
     * @param rawNewLine    the raw line-ending characters
     */
    public LdifNonEmptyLineBase( int offset, String rawLineStart, String rawNewLine )
    {
        super( offset, rawNewLine );
        this.rawLineStart = rawLineStart;
    }


    // ── LINE START ACCESSORS ──────────────────────────────────────────────────
    /**
     * Returns the raw line-start segment, or an empty string if it is null.
     *
     * @return the raw opening segment, never {@code null}
     */
    public String getRawLineStart()
    {
        return getNonNull( rawLineStart );
    }


    /**
     * Returns the unfolded form of {@link #getRawLineStart()}.
     *
     * @return the unfolded line-start segment
     */
    public String getUnfoldedLineStart()
    {
        return unfold( getRawLineStart() );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when both {@code rawLineStart} and the inherited
     * {@code rawNewLine} are non-null.</p>
     */
    public boolean isValid()
    {
        return super.isValid() && rawLineStart != null;
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code "Missing line start"} when {@code rawLineStart} is
     * null; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( rawLineStart == null )
        {
            return "Missing line start";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Concatenates {@link #getRawLineStart()} + {@link #getRawNewLine()}.</p>
     */
    public String toRawString()
    {
        return getRawLineStart() + getRawNewLine();
    }


    // ── FOLD DETECTION ────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this line's raw text contains an LDIF
     * line-folding continuation sequence (newline followed by a space).
     *
     * @return {@code true} if the raw string contains {@code "\n "} or
     *         {@code "\r "}
     */
    public boolean isFolded()
    {
        String rawString = toRawString();
        return rawString.indexOf( "\n " ) > -1 || rawString.indexOf( "\r " ) > -1; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
