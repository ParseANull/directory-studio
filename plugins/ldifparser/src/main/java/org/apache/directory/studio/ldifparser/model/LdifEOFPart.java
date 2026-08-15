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


// ── CLASS: LdifEOFPart — REBEL TRANSMISSION ENDS WITH "TRANSMISSION COMPLETE" ─
// Every Rebel data transmission ends with a "Transmission Complete" sentinel
// token — a zero-length marker that tells the receiver the data is done and
// no more bytes are expected.
// LdifEOFPart is that sentinel in the LDIF model: a zero-length, always-valid
// LdifPart placed at the end of every parsed container so the rest of the
// model code has a safe "end of input" marker to check against.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A zero-length, always-valid sentinel {@link LdifPart} placed at the end of
 * a parsed LDIF container to mark end-of-input.
 * {@link #getLength()} always returns {@code 0} and {@link #toRawString()}
 * and {@link #toFormattedString} both return the empty string.
 * Think of this as the "Transmission Complete" marker at the end of every
 * Rebel data packet.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class LdifEOFPart implements LdifPart
{
    /** The byte offset of this EOF marker in the original input. */
    private int offset;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates an EOF part at the given {@code offset}.
     *
     * @param offset  the byte position where the input ended
     */
    public LdifEOFPart( int offset )
    {
        this.offset = offset;
    }


    // ── POSITION ACCESSORS ────────────────────────────────────────────────────
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
     * <p>Always {@code 0} — an EOF part occupies no characters.</p>
     */
    public int getLength()
    {
        return 0;
    }


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return an empty string — EOF parts produce no output
     */
    public String toRawString()
    {
        return ""; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @return an empty string — EOF parts produce no output
     */
    public String toFormattedString( LdifFormatParameters formatParameters )
    {
        return ""; //$NON-NLS-1$
    }


    /**
     * Returns a debug-friendly description including class name, offset, and
     * length.
     */
    public String toString()
    {
        return getClass().getName() + " (" + getOffset() + "," + getLength() + "): ''"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Always {@code true} — an EOF marker is structurally valid.</p>
     */
    public boolean isValid()
    {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @return an empty string — there is no validation error message for EOF
     */
    public String getInvalidString()
    {
        return ""; //$NON-NLS-1$
    }


    // ── OFFSET ADJUSTMENT ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Shifts the stored offset by {@code adjust} characters.</p>
     */
    public void adjustOffset( int adjust )
    {
        offset += adjust;
    }
}
