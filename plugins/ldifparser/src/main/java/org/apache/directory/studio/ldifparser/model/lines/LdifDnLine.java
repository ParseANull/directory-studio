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


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldifparser.LdifParserConstants;
import org.apache.directory.studio.ldifparser.LdifUtils;


// ── CLASS: LdifDnLine — REBEL COMMUNIQUÉ DESTINATION ADDRESS ─────────────────
// Every Rebel communiqué starts with the destination address — a DN that says
// "this record is about the agent or entry at this location in the directory
// tree".  The DN may arrive as plain text or Base64-encoded, and it must be
// syntactically valid or the record is rejected by the network.
// LdifDnLine models that address line: extends LdifValueLineBase with
// getRawDnSpec/getRawDn accessors, DN syntax validation via
// Dn.isValid(), and a factory that auto-selects Base64 encoding.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF distinguished-name line: {@code dn: <dn>} or {@code dn:: <base64dn>}.
 * Extends {@link LdifValueLineBase} — the {@code "dn"} keyword is the
 * line-start segment, the separator is {@code ":"} or {@code "::"}, and the
 * DN string is the value.
 * {@link #isValid()} also checks DN syntax via {@link Dn#isValid}.
 * Use {@link #create(String)} to build a DN line, which automatically applies
 * Base64 encoding when required.
 * Think of this as the Rebel communiqué destination address — every record
 * must have a syntactically valid DN.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifDnLine extends LdifValueLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a DN line with all five raw segments.
     *
     * @param offset      byte offset in the document
     * @param rawDnSpec   the raw DN spec keyword ({@code "dn"})
     * @param rawValueType the separator ({@code ":"} or {@code "::"})
     * @param rawDn       the raw (possibly Base64-encoded) DN string
     * @param rawNewLine  the raw line-ending characters
     */
    public LdifDnLine( int offset, String rawDnSpec, String rawValueType, String rawDn, String rawNewLine )
    {
        super( offset, rawDnSpec, rawValueType, rawDn, rawNewLine );
    }


    // ── DN ACCESSORS ──────────────────────────────────────────────────────────
    /**
     * Returns the raw DN spec keyword ({@code "dn"}).
     *
     * @return the raw dn-spec segment
     */
    public String getRawDnSpec()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded DN spec keyword.
     *
     * @return the unfolded dn-spec segment
     */
    public String getUnfoldedDnSpec()
    {
        return super.getUnfoldedLineStart();
    }


    /**
     * Returns the raw (possibly Base64-encoded) DN value string.
     *
     * @return the raw DN string
     */
    public String getRawDn()
    {
        return super.getRawValue();
    }


    /**
     * Returns the unfolded DN value string.
     *
     * @return the unfolded raw DN string
     */
    public String getUnfoldedDn()
    {
        return super.getUnfoldedValue();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Additionally validates the decoded DN string via
     * {@link Dn#isValid(String)} — a structurally valid but syntactically
     * invalid DN makes this line invalid.</p>
     */
    public boolean isValid()
    {
        return super.isValid() && Dn.isValid( getValueAsString() );
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns descriptive messages for missing dn spec, missing DN value,
     * and invalid DN syntax; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedDnSpec().length() == 0 )
        {
            return "Missing Dn spec 'dn'";
        }
        else if ( getUnfoldedDn().length() == 0 )
        {
            return "Missing Dn";
        }
        else if ( !Dn.isValid( getValueAsString() ) )
        {
            return "Invalid Dn";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new DN line for {@code dn}, automatically applying Base64
     * encoding if {@link LdifUtils#mustEncode(String)} returns {@code true}.
     *
     * @param dn  the plain-text distinguished name
     * @return    a new {@link LdifDnLine}
     */
    public static LdifDnLine create( String dn )
    {
        if ( LdifUtils.mustEncode( dn ) )
        {
            return new LdifDnLine( 0, "dn", "::", LdifUtils.base64encode( LdifUtils.utf8encode( dn ) ), //$NON-NLS-1$ //$NON-NLS-2$
                LdifParserConstants.LINE_SEPARATOR );
        }
        else
        {
            return new LdifDnLine( 0, "dn", ":", dn, LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
