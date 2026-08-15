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


// ── CLASS: LdifDeloldrdnLine — REBEL CODENAME RETIREMENT FLAG ─────────────────
// When a Rebel operative is assigned a new callsign, command decides whether
// to retire the old one immediately (deleteoldrdn: 1) or keep it as an alias
// (deleteoldrdn: 0).  This single boolean decision travels in the moddn record.
// LdifDeloldrdnLine models that flag: extends LdifValueLineBase with
// getRawDeleteOldrdnSpec/getRawDeleteOldrdn accessors, an isDeleteOldRdn()
// predicate, and factories create0()/create1() for the two possible values.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF {@code deleteoldrdn: 0|1} line inside a moddn change record.
 * Extends {@link LdifValueLineBase} — the {@code "deleteoldrdn"} keyword is
 * the line-start segment, the separator is {@code ":"}, and the value is
 * {@code "0"} or {@code "1"}.
 * Valid only when the value is exactly {@code "0"} or {@code "1"}.
 * Use {@link #create0()} or {@link #create1()} to build standard lines.
 * Think of this as the Rebel codename retirement flag — {@code 1} retires the
 * old callsign, {@code 0} keeps it as an alias.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifDeloldrdnLine extends LdifValueLineBase
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a deleteoldrdn line with all five raw segments.
     *
     * @param offset                byte offset in the document
     * @param rawDeleteOldrdnSpec   the raw {@code "deleteoldrdn"} keyword
     * @param rawValueType          the separator ({@code ":"})
     * @param rawDeleteOldrdn       the raw value ({@code "0"} or {@code "1"})
     * @param rawNewLine            the raw line-ending characters
     */
    public LdifDeloldrdnLine( int offset, String rawDeleteOldrdnSpec, String rawValueType, String rawDeleteOldrdn,
        String rawNewLine )
    {
        super( offset, rawDeleteOldrdnSpec, rawValueType, rawDeleteOldrdn, rawNewLine );
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────
    /**
     * Returns the raw {@code "deleteoldrdn"} keyword.
     *
     * @return the raw deleteoldrdn-spec segment
     */
    public String getRawDeleteOldrdnSpec()
    {
        return super.getRawLineStart();
    }


    /**
     * Returns the unfolded {@code "deleteoldrdn"} keyword.
     *
     * @return the unfolded deleteoldrdn-spec segment
     */
    public String getUnfoldedDeleteOldrdnSpec()
    {
        return super.getUnfoldedLineStart();
    }


    /**
     * Returns the raw value string ({@code "0"} or {@code "1"}).
     *
     * @return the raw deleteoldrdn value
     */
    public String getRawDeleteOldrdn()
    {
        return super.getRawValue();
    }


    /**
     * Returns the unfolded value string.
     *
     * @return the unfolded deleteoldrdn value
     */
    public String getUnfoldedDeleteOldrdn()
    {
        return super.getUnfoldedValue();
    }


    /**
     * Returns {@code true} if the value is {@code "1"} (delete the old RDN).
     *
     * @return {@code true} to retire the old RDN
     */
    public boolean isDeleteOldRdn()
    {
        return "1".equals( getUnfoldedDeleteOldrdn() ); //$NON-NLS-1$
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when the superclass check passes and the value is exactly
     * {@code "0"} or {@code "1"}.</p>
     */
    public boolean isValid()
    {
        if ( !super.isValid() )
        {
            return false;
        }

        return ( "0".equals( getUnfoldedDeleteOldrdn() ) || "1".equals( getUnfoldedDeleteOldrdn() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns descriptive messages for missing spec or invalid value.</p>
     */
    public String getInvalidString()
    {
        if ( getUnfoldedDeleteOldrdnSpec().length() == 0 )
        {
            return "Missing delete old Rdn spec 'deleteoldrdn'";
        }
        else if ( !"0".equals( getUnfoldedDeleteOldrdn() ) && !"1".equals( getUnfoldedDeleteOldrdn() ) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
            return "Invalid value of delete old Rdn, must be '0' or '1'";
        }
        else
        {
            return super.getInvalidString();
        }
    }


    // ── FACTORY METHODS ───────────────────────────────────────────────────────
    /**
     * Creates a {@code deleteoldrdn: 0} line at offset 0.
     *
     * @return a new {@link LdifDeloldrdnLine} with value {@code "0"}
     */
    public static LdifDeloldrdnLine create0()
    {
        return new LdifDeloldrdnLine( 0, "deleteoldrdn", ":", "0", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    /**
     * Creates a {@code deleteoldrdn: 1} line at offset 0.
     *
     * @return a new {@link LdifDeloldrdnLine} with value {@code "1"}
     */
    public static LdifDeloldrdnLine create1()
    {
        return new LdifDeloldrdnLine( 0, "deleteoldrdn", ":", "1", LdifParserConstants.LINE_SEPARATOR ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
