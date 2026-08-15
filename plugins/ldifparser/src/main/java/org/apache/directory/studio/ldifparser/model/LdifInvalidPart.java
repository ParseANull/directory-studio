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

package org.apache.directory.studio.ldifparser.model;


import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.LdifUtils;


// ── CLASS: LdifInvalidPart — HAN SAYS "I'VE GOT A BAD FEELING ABOUT THIS" ────
// Han reads back a fragment of the incoming transmission that just doesn't
// parse — it's garbled, out of sequence, or simply not valid LDIF syntax.
// Rather than throw an exception and halt, he bags the fragment, labels it
// "Unexpected Token", and keeps going so the rest of the file can still be
// processed.
// LdifInvalidPart is that bag: a concrete LdifPart holding the raw
// unparseable text so the UI can display an error annotation without
// discarding the surrounding valid data.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete {@link LdifPart} that wraps a fragment of LDIF text that the
 * parser could not recognise as valid syntax.
 * {@link #isValid()} returns {@code false} and {@link #getInvalidString()}
 * returns {@code "Unexpected Token"}.  The raw fragment is preserved verbatim
 * so callers can display it with an error annotation.
 * Think of this as Han Solo's "bad feeling" bag — something's wrong here, but
 * we're not stopping the mission.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class LdifInvalidPart implements LdifPart
{
    /** The byte offset of this invalid fragment in the original document. */
    private int offset;

    /** The verbatim unparseable text. */
    private String unknown;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates an invalid part at {@code offset} holding {@code unknown} text.
     *
     * @param offset   the byte position of the invalid fragment
     * @param unknown  the verbatim unparseable text
     */
    public LdifInvalidPart( int offset, String unknown )
    {
        this.offset = offset;
        this.unknown = unknown;
    }


    // ── POSITION ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int getOffset()
    {
        return offset;
    }


    /**
     * {@inheritDoc}
     *
     * <p>Equals the length of {@link #toRawString()}, i.e. the raw invalid
     * text.</p>
     */
    public int getLength()
    {
        return toRawString().length();
    }


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return the verbatim unparseable text, unchanged
     */
    public String toRawString()
    {
        return unknown;
    }


    /**
     * {@inheritDoc}
     *
     * @return the verbatim unparseable text — invalid fragments are passed
     *         through without reformatting
     */
    public String toFormattedString( LdifFormatParameters formatParameters )
    {
        return unknown;
    }


    /**
     * Returns a debug-friendly description including class name, offset, length,
     * and the raw text with newlines converted to printable escapes.
     */
    public String toString()
    {
        String text = toRawString();
        text = LdifUtils.convertNlRcToString( text );

        return getClass().getName() + " (" + getOffset() + "," + getLength() + "): '" + text + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return always {@code false} — this part is by definition invalid
     */
    public boolean isValid()
    {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @return {@code "Unexpected Token"}
     */
    public String getInvalidString()
    {
        return "Unexpected Token";
    }


    // ── OFFSET ADJUSTMENT ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void adjustOffset( int adjust )
    {
        offset += adjust;
    }
}
