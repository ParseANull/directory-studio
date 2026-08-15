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


// ── CLASS: LdifVersionLine — IMPERIAL COMMS PROTOCOL VERSION HEADER ──────────
// The Imperial comms network requires every transmission to begin with a
// protocol version declaration: "version: 1" — just two words separated by a
// colon.  Without this line the receiver knows the file predates the standard
// but tolerates it anyway.
// LdifVersionLine represents that header line in the LDIF model: the "version"
// keyword, the ":" separator, the "1" value, and a factory that creates the
// standard version-1 line.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF {@code version: 1} header line at the start of a file.
 * Extends {@link LdifValueLineBase} — the version spec ({@code "version"}) is
 * the line-start segment, the separator is {@code ":"}, and the version number
 * is the value.
 * Use {@link #create()} to construct the standard version-1 line.
 * Think of this as the Imperial comms protocol header that opens every
 * standards-compliant LDIF transmission.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifVersionLine extends LdifValueLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a version line with all five raw segments.
     *
     * @param offset          byte offset in the document
     * @param rawVersionSpec  the raw version keyword (e.g. {@code "version"})
     * @param rawValueType    the separator ({@code ":"})
     * @param rawVersion      the raw version number (e.g. {@code "1"})
     * @param rawNewLine      the raw line-ending characters
     */
    public LdifVersionLine( int offset, String rawVersionSpec, String rawValueType, String rawVersion, String rawNewLine )
    {
        super( offset, rawVersionSpec, rawValueType, rawVersion, rawNewLine );
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────
    /**
     * Returns the raw version spec ({@code "version"}).
     *
     * @return the raw version-spec segment
     */
    public String getRawVersionSpec()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded version spec.
     *
     * @return the unfolded version-spec segment
     */
    public String getUnfoldedVersionSpec()
    {
        return super.getUnfoldedLineStart();
    }


    /**
     * Returns the raw version number string.
     *
     * @return the raw version number
     */
    public String getRawVersion()
    {
        return super.getRawValue();
    }


    /**
     * Returns the unfolded version number string.
     *
     * @return the unfolded version number
     */
    public String getUnfoldedVersion()
    {
        return super.getUnfoldedValue();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns descriptive messages when the version spec or version number
     * is missing; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedVersionSpec().length() == 0 )
        {
            return "Missing version spec";
        }
        else if ( getUnfoldedVersion().length() == 0 )
        {
            return "Missing version";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates the standard {@code version: 1} line at offset 0.
     *
     * @return a new {@link LdifVersionLine}
     */
    public static LdifVersionLine create()
    {
        return new LdifVersionLine( 0, "version", ":", "1", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
