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


// ── CLASS: LdifNewsuperiorLine — REBEL OPERATIVE TRANSFER DESTINATION ─────────
// When a Rebel operative moves to a different cell, the moddn order carries a
// "newsuperior:" line specifying the DN of the new parent node — the new
// division they're being transferred into.  This field is optional; without it
// the operative stays in the same parent.
// LdifNewsuperiorLine models that transfer-destination line: extends
// LdifValueLineBase with getRawNewSuperiorSpec/getRawNewSuperiorDn accessors,
// validity messages for missing values, and a factory that auto-applies
// Base64 encoding.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF {@code newsuperior: <dn>} or {@code newsuperior:: <base64dn>} line.
 * Extends {@link LdifValueLineBase} — the {@code "newsuperior"} keyword is the
 * line-start segment, the separator is {@code ":"} or {@code "::"}, and the new
 * parent DN is the value.
 * Use {@link #create(String)} to build the line; Base64 encoding is applied
 * automatically when required.
 * Think of this as the Rebel operative transfer-destination line.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifNewsuperiorLine extends LdifValueLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a newsuperior line with all five raw segments.
     *
     * @param offset               byte offset in the document
     * @param rawNewSuperiorSpec   the raw {@code "newsuperior"} keyword
     * @param rawValueType         the separator ({@code ":"} or {@code "::"})
     * @param rawNewSuperiorDn     the raw (possibly Base64-encoded) new parent DN
     * @param rawNewLine           the raw line-ending characters
     */
    public LdifNewsuperiorLine( int offset, String rawNewSuperiorSpec, String rawValueType, String rawNewSuperiorDn,
        String rawNewLine )
    {
        super( offset, rawNewSuperiorSpec, rawValueType, rawNewSuperiorDn, rawNewLine );
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────
    /**
     * Returns the raw {@code "newsuperior"} keyword.
     *
     * @return the raw newsuperior-spec segment
     */
    public String getRawNewSuperiorSpec()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded {@code "newsuperior"} keyword.
     *
     * @return the unfolded newsuperior-spec segment
     */
    public String getUnfoldedNewSuperiorSpec()
    {
        return super.getUnfoldedLineStart();
    }


    /**
     * Returns the raw new parent DN string.
     *
     * @return the raw new superior DN
     */
    public String getRawNewSuperiorDn()
    {
        return super.getRawValue();
    }


    /**
     * Returns the unfolded new parent DN string.
     *
     * @return the unfolded new superior DN
     */
    public String getUnfoldedNewSuperiorDn()
    {
        return super.getUnfoldedValue();
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns descriptive messages for missing newsuperior spec or missing
     * DN; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedNewSuperiorSpec().length() == 0 )
        {
            return "Missing new superior spec 'newsuperior'";
        }
        else if ( getUnfoldedNewSuperiorDn().length() == 0 )
        {
            return "Missing new superior Dn";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a newsuperior line for {@code newsuperior}, applying Base64
     * encoding automatically when required.
     *
     * @param newsuperior  the new parent DN string
     * @return a new {@link LdifNewsuperiorLine}
     */
    public static LdifNewsuperiorLine create( String newsuperior )
    {
        if ( LdifUtils.mustEncode( newsuperior ) )
        {
            return new LdifNewsuperiorLine( 0, "newsuperior", "::", LdifUtils.base64encode( LdifUtils //$NON-NLS-1$ //$NON-NLS-2$
                .utf8encode( newsuperior ) ), LdifParserConstants.LINE_SEPARATOR );
        }
        else
        {
            return new LdifNewsuperiorLine( 0, "newsuperior", ":", newsuperior, LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

}
