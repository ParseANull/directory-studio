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

package org.apache.directory.studio.ldifparser.model.container;


import org.apache.directory.studio.ldifparser.model.LdifEOFPart;
import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;


// ── CLASS: LdifRecord — REBEL COMMUNIQUÉ BASE CLASS ──────────────────────────
// Every Rebel communiqué in the smuggling network starts with a destination
// (the DN line) and ends with a blank separator or an end-of-file marker.
// In between it may carry comments, attribute-value pairs, changetype
// directives, or control lines — but those are for the subclasses.
// LdifRecord establishes that contract: it takes a DN line at construction,
// provides addComment/finish, getDnLine/getSepLine accessors, and enforces
// the "must start with DN, must end with sep or EOF" validity rule.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for both content records ({@link LdifContentRecord}) and
 * change records ({@link LdifChangeRecord}).
 * A record must start with a {@link LdifDnLine} and end with either a
 * {@link LdifSepLine} (blank line) or a {@link LdifEOFPart}.
 * Comments ({@link LdifCommentLine}) may appear anywhere after the DN line.
 * Think of this as the Rebel communiqué template — every data record must have
 * a destination DN and a clean terminator.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class LdifRecord extends LdifContainer
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new record whose first part is {@code dn}.
     *
     * @param dn  the distinguished-name line that opens this record
     */
    protected LdifRecord( LdifDnLine dn )
    {
        super( dn );
    }


    // ── ADD A COMMENT ─────────────────────────────────────────────────────────
    /**
     * Appends a {@link LdifCommentLine} to this record.
     *
     * @param comment  the comment line to add (must not be {@code null})
     * @throws IllegalArgumentException if {@code comment} is {@code null}
     */
    public void addComment( LdifCommentLine comment )
    {
        if ( comment == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( comment );
    }


    // ── FINISH WITH SEPARATOR ─────────────────────────────────────────────────
    /**
     * Terminates this record with a blank-line separator.
     *
     * @param sep  the separator line (must not be {@code null})
     * @throws IllegalArgumentException if {@code sep} is {@code null}
     */
    public void finish( LdifSepLine sep )
    {
        if ( sep == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( sep );
    }


    // ── FINISH WITH EOF ───────────────────────────────────────────────────────
    /**
     * Terminates this record with an end-of-file marker.
     *
     * @param eof  the EOF part (must not be {@code null})
     * @throws IllegalArgumentException if {@code eof} is {@code null}
     */
    public void finish( LdifEOFPart eof )
    {
        if ( eof == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( eof );
    }


    // ── DN LINE ACCESSOR ──────────────────────────────────────────────────────
    /**
     * Returns the first part cast to {@link LdifDnLine}.
     *
     * @return the DN line that opened this record
     */
    public LdifDnLine getDnLine()
    {
        return ( LdifDnLine ) ldifParts.get( 0 );
    }


    // ── SEPARATOR LINE ACCESSOR ────────────────────────────────────────────────
    /**
     * Returns the {@link LdifSepLine} in this record's part list, or
     * {@code null} if none has been added yet (record not yet terminated).
     *
     * @return the separator line, or {@code null}
     */
    public LdifSepLine getSepLine()
    {
        for ( LdifPart ldifPart : ldifParts )
        {
            if ( ldifPart instanceof LdifSepLine )
            {
                return ( LdifSepLine ) ldifPart;
            }
        }

        return null;
    }


    // ── INVALID STRING ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns a descriptive message for the first structural problem:
     * missing DN, invalid DN, missing separator, invalid separator, or
     * delegates to the superclass for part-level errors.</p>
     */
    public String getInvalidString()
    {
        LdifDnLine dnLine = getDnLine();
        LdifSepLine sepLine = getSepLine();

        if ( dnLine == null )
        {
            return "Record must start with Dn";
        }
        else if ( !dnLine.isValid() )
        {
            return dnLine.getInvalidString();
        }

        if ( sepLine == null )
        {
            return "Record must end with an empty line";
        }
        else if ( !sepLine.isValid() )
        {
            return sepLine.getInvalidString();
        }

        return super.getInvalidString();
    }


    // ── ABSTRACT VALIDITY (RECORD-LEVEL) ──────────────────────────────────────
    /**
     * Extends the base container validity check with record-specific rules:
     * the DN line must be valid, and the last part must be a valid
     * {@link LdifSepLine} or {@link LdifEOFPart}.
     *
     * @return {@code true} if the record passes all structural checks
     */
    protected boolean isAbstractValid()
    {
        if ( !super.isAbstractValid() )
        {
            return false;
        }

        LdifPart lastPart = getLastPart();

        return getDnLine().isValid() && ( ( lastPart instanceof LdifSepLine ) || ( lastPart instanceof LdifEOFPart ) )
            && lastPart.isValid();
    }
}
