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


// ── CLASS: LdifCommentLine — REBEL ARCHIVIST'S MARGIN NOTE LINE ───────────────
// The Rebel archivist scrawls a margin note in the communiqué: a line
// starting with "#" followed by free-form text — no colon, no value
// segment, just the pound sign and the comment text as a single raw line.
// LdifCommentLine models that margin note: extends LdifNonEmptyLineBase with
// getRawComment/getUnfoldedComment accessors and a factory method.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF comment line — a line starting with {@code #} and free-form text.
 * Extends {@link LdifNonEmptyLineBase}: the comment text (including the
 * {@code #} prefix) is the line-start segment.
 * Use {@link #create(String)} to build a comment programmatically.
 * Think of this as the Rebel archivist's inline margin note.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifCommentLine extends LdifNonEmptyLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a comment line with the given raw segments.
     *
     * @param offset      byte offset in the document
     * @param rawComment  the raw comment text (including the {@code #} prefix)
     * @param rawNewLine  the raw line-ending characters
     */
    public LdifCommentLine( int offset, String rawComment, String rawNewLine )
    {
        super( offset, rawComment, rawNewLine );
    }


    // ── COMMENT ACCESSORS ─────────────────────────────────────────────────────
    /**
     * Returns the raw comment text (the full {@code #...} string).
     *
     * @return the raw comment text
     */
    public String getRawComment()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded comment text.
     *
     * @return the unfolded comment text
     */
    public String getUnfoldedComment()
    {
        return super.getUnfoldedLineStart();
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new comment line at offset 0 using the platform
     * {@link LdifParserConstants#LINE_SEPARATOR}.
     *
     * @param comment  the raw comment text (should start with {@code #})
     * @return a new {@link LdifCommentLine}
     */
    public static LdifCommentLine create( String comment )
    {
        return new LdifCommentLine( 0, comment, LdifParserConstants.LINE_SEPARATOR );
    }

}
