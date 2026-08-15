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


// ── CLASS: LdifNewrdnLine — REBEL OPERATIVE NEW CALLSIGN LINE ─────────────────
// The new callsign for a renamed Rebel operative is carried in the moddn order
// as "newrdn: cn=Leia" or "newrdn:: <base64>".  The factory method auto-selects
// Base64 encoding when the callsign contains unsafe characters.
// LdifNewrdnLine models that new-callsign line: extends LdifValueLineBase with
// getRawNewrdnSpec/getRawNewrdn accessors, validity messages for missing values,
// and a factory that applies Base64 encoding when needed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF {@code newrdn: <rdn>} or {@code newrdn:: <base64rdn>} line.
 * Extends {@link LdifValueLineBase} — the {@code "newrdn"} keyword is the
 * line-start segment, the separator is {@code ":"} or {@code "::"}, and the
 * new RDN value is the payload.
 * Use {@link #create(String)} to build the line; it automatically applies
 * Base64 encoding when required.
 * Think of this as the Rebel operative's new callsign assignment line.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifNewrdnLine extends LdifValueLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a newrdn line with all five raw segments.
     *
     * @param offset          byte offset in the document
     * @param rawNewrdnSpec   the raw {@code "newrdn"} keyword
     * @param rawValueType    the separator ({@code ":"} or {@code "::"})
     * @param rawNewrdn       the raw (possibly Base64-encoded) new RDN value
     * @param rawNewLine      the raw line-ending characters
     */
    public LdifNewrdnLine( int offset, String rawNewrdnSpec, String rawValueType, String rawNewrdn, String rawNewLine )
    {
        super( offset, rawNewrdnSpec, rawValueType, rawNewrdn, rawNewLine );
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────
    /**
     * Returns the raw {@code "newrdn"} keyword.
     *
     * @return the raw newrdn-spec segment
     */
    public String getRawNewrdnSpec()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded {@code "newrdn"} keyword.
     *
     * @return the unfolded newrdn-spec segment
     */
    public String getUnfoldedNewrdnSpec()
    {
        return super.getUnfoldedLineStart();
    }


    /**
     * Returns the raw new RDN value.
     *
     * @return the raw newrdn value
     */
    public String getRawNewrdn()
    {
        return super.getRawValue();
    }


    /**
     * Returns the unfolded new RDN value.
     *
     * @return the unfolded newrdn value
     */
    public String getUnfoldedNewrdn()
    {
        return super.getUnfoldedValue();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns descriptive messages for missing newrdn spec or missing new
     * RDN value; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedNewrdnSpec().length() == 0 )
        {
            return "Missing new Rdn spec 'newrdn'";
        }
        else if ( getUnfoldedNewrdn().length() == 0 )
        {
            return "Missing new Rdn";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new RDN line for {@code newrdn}, applying Base64 encoding
     * automatically when required.
     *
     * @param newrdn  the new RDN string (e.g. {@code "cn=Leia"})
     * @return a new {@link LdifNewrdnLine}
     */
    public static LdifNewrdnLine create( String newrdn )
    {
        if ( LdifUtils.mustEncode( newrdn ) )
        {
            return new LdifNewrdnLine( 0, "newrdn", "::", LdifUtils.base64encode( LdifUtils.utf8encode( newrdn ) ), //$NON-NLS-1$ //$NON-NLS-2$
                LdifParserConstants.LINE_SEPARATOR );
        }
        else
        {
            return new LdifNewrdnLine( 0, "newrdn", ":", newrdn, LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
