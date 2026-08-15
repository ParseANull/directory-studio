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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifControlLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;


// ── CLASS: LdifChangeRecord — REBEL OPERATIONS CHANGE ORDER BASE ──────────────
// The Rebel Alliance issues change orders to its network: some add new agents,
// some modify existing ones, some delete compromised ones, and some rename
// callsigns.  Every change order starts with a DN, carries optional LDAP
// control lines, and requires a "changetype:" line declaring what kind of
// operation it is.
// LdifChangeRecord is that change-order base class: extends LdifRecord with
// addControl/setChangeType mutations, getControls/getChangeTypeLine accessors,
// and the validity rule "must have a changetype line".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract LDIF container for any change-type record (add, modify, delete,
 * or moddn).
 * Extends {@link LdifRecord} with support for LDAP control lines and the
 * mandatory {@code changetype:} line.  A change record is valid when the
 * superclass checks pass and a {@link LdifChangeTypeLine} is present.
 * Think of this as the Rebel change-order base — every operation must declare
 * what kind of change it is making.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifChangeRecord extends LdifRecord
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a change record starting with the given DN line.
     *
     * @param dn  the distinguished-name line for this record
     */
    public LdifChangeRecord( LdifDnLine dn )
    {
        super( dn );
    }


    // ── ADD A CONTROL LINE ────────────────────────────────────────────────────
    /**
     * Appends an LDAP {@link LdifControlLine} to this record.
     * Control lines appear between the DN and the changetype line.
     *
     * @param controlLine  the control line to add (must not be {@code null})
     * @throws IllegalArgumentException if {@code controlLine} is {@code null}
     */
    public void addControl( LdifControlLine controlLine )
    {
        if ( controlLine == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( controlLine );
    }


    // ── SET THE CHANGETYPE LINE ───────────────────────────────────────────────
    /**
     * Appends the {@link LdifChangeTypeLine} to this record.
     * May only be called once per record.
     *
     * @param changeTypeLine  the changetype line (must not be {@code null})
     * @throws IllegalArgumentException if {@code changeTypeLine} is {@code null}
     *                                  or if a changetype line is already set
     */
    public void setChangeType( LdifChangeTypeLine changeTypeLine )
    {
        if ( changeTypeLine == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        if ( getChangeTypeLine() != null )
        {
            throw new IllegalArgumentException( "changetype is already set" );
        }

        ldifParts.add( changeTypeLine );
    }


    // ── GET CONTROL LINES ─────────────────────────────────────────────────────
    /**
     * Returns all {@link LdifControlLine} parts in this record.
     *
     * @return array of control lines (may be empty)
     */
    public LdifControlLine[] getControls()
    {
        List<LdifControlLine> ldifControlLines = new ArrayList<LdifControlLine>();

        for ( Object part : ldifParts )
        {
            if ( part instanceof LdifControlLine )
            {
                ldifControlLines.add( ( LdifControlLine ) part );
            }
        }

        return ldifControlLines.toArray( new LdifControlLine[ldifControlLines.size()] );
    }


    // ── GET THE CHANGETYPE LINE ───────────────────────────────────────────────
    /**
     * Returns the {@link LdifChangeTypeLine} in this record, or {@code null}
     * if none has been set yet.
     *
     * @return the changetype line, or {@code null}
     */
    public LdifChangeTypeLine getChangeTypeLine()
    {
        for ( Object part : ldifParts )
        {
            if ( part instanceof LdifChangeTypeLine )
            {
                return ( LdifChangeTypeLine ) part;
            }
        }

        return null;
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>A change record is abstractly valid if the superclass checks pass and
     * a {@link LdifChangeTypeLine} is present.</p>
     */
    protected boolean isAbstractValid()
    {
        if ( !super.isAbstractValid() )
        {
            return false;
        }

        return getChangeTypeLine() != null;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isValid()
    {
        return isAbstractValid();
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code "Missing changetype line"} if no changetype has been
     * set; otherwise delegates to the superclass.</p>
     */
    public String getInvalidString()
    {
        if ( getChangeTypeLine() == null )
        {
            return "Missing changetype line";
        }

        return super.getInvalidString();
    }
}
