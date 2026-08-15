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


// ── CLASS: LdifModSpecSepLine — REBEL MODIFICATION ORDER CLOSING DASH ─────────
// Every Rebel field-modification order ends with a single dash on a line by
// itself — "-" — signalling "this mod-spec is done, the next one starts next".
// It's the simplest possible close marker: just a minus sign and a newline.
// LdifModSpecSepLine is that dash line: extends LdifNonEmptyLineBase with
// a getRawMinus accessor, a validity check that the unfolded content is exactly
// "-", and a factory method.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF mod-spec separator line — a single {@code -} on its own line,
 * terminating one modification specification inside a
 * {@code changetype: modify} record.
 * Extends {@link LdifNonEmptyLineBase}: the {@code -} character is the
 * line-start segment.
 * Valid only when the unfolded line-start is exactly {@code "-"}.
 * Use {@link #create()} to construct a standard separator line.
 * Think of this as the Rebel modification order closing dash — one hyphen
 * that says "this field operation is complete".
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifModSpecSepLine extends LdifNonEmptyLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a mod-spec separator line.
     *
     * @param offset      byte offset in the document
     * @param rawMinus    the raw minus character ({@code "-"})
     * @param rawNewLine  the raw line-ending characters
     */
    public LdifModSpecSepLine( int offset, String rawMinus, String rawNewLine )
    {
        super( offset, rawMinus, rawNewLine );
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────
    /**
     * Returns the raw minus character.
     *
     * @return the raw minus string
     */
    public String getRawMinus()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded minus character.
     *
     * @return the unfolded minus string
     */
    public String getUnfoldedMinus()
    {
        return super.getUnfoldedLineStart();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when the superclass check passes and the unfolded line-start
     * is exactly {@code "-"}.</p>
     */
    public boolean isValid()
    {
        return super.isValid() && getUnfoldedMinus().equals( "-" ); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code "Missing '-'"} when the line-start is not a single
     * dash; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( !getUnfoldedMinus().equals( "-" ) ) //$NON-NLS-1$
        {
            return "Missing '-'";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a standard mod-spec separator line at offset 0.
     *
     * @return a new {@link LdifModSpecSepLine}
     */
    public static LdifModSpecSepLine create()
    {
        return new LdifModSpecSepLine( 0, "-", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$
    }

}
