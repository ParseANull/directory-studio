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


import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDeloldrdnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifNewrdnLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifNewsuperiorLine;


// ── CLASS: LdifChangeModDnRecord — REBEL AGENT CODENAME REASSIGNMENT ─────────
// When the Rebellion reassigns an operative's codename or moves them to a new
// cell, command issues a moddn order: the old DN, "changetype: modrdn" (or
// moddn), the new RDN, whether to delete the old RDN, and optionally the new
// parent (newsuperior).
// LdifChangeModDnRecord models that reassignment order: a change record with
// changetype modrdn/moddn that requires at minimum a newrdn line and a
// deleteoldrdn line to be valid.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for a {@code changetype: modrdn} or {@code changetype: moddn}
 * record.
 * Extends {@link LdifChangeRecord} with setters and getters for the three
 * moddn-specific lines: {@link LdifNewrdnLine}, {@link LdifDeloldrdnLine}, and
 * the optional {@link LdifNewsuperiorLine}.
 * A record is valid when the superclass abstract checks pass and both the
 * newrdn and deleteoldrdn lines are present.
 * Think of this as the Rebel codename reassignment order — the old DN, the
 * new callsign, and whether to retire the old one.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifChangeModDnRecord extends LdifChangeRecord
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a change-moddn record starting with the given DN line.
     *
     * @param dn  the distinguished-name line for this record
     */
    public LdifChangeModDnRecord( LdifDnLine dn )
    {
        super( dn );
    }


    // ── SET THE NEW RDN LINE ──────────────────────────────────────────────────
    /**
     * Appends the {@link LdifNewrdnLine} to this record.
     *
     * @param newrdn  the new RDN line (must not be {@code null})
     * @throws IllegalArgumentException if {@code newrdn} is {@code null}
     */
    public void setNewrdn( LdifNewrdnLine newrdn )
    {
        if ( newrdn == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( newrdn );
    }


    // ── SET THE DELETE-OLD-RDN LINE ───────────────────────────────────────────
    /**
     * Appends the {@link LdifDeloldrdnLine} to this record.
     *
     * @param deloldrdn  the deleteoldrdn line (must not be {@code null})
     * @throws IllegalArgumentException if {@code deloldrdn} is {@code null}
     */
    public void setDeloldrdn( LdifDeloldrdnLine deloldrdn )
    {
        if ( deloldrdn == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( deloldrdn );
    }


    // ── SET THE NEWSUPERIOR LINE ──────────────────────────────────────────────
    /**
     * Appends the optional {@link LdifNewsuperiorLine} to this record.
     *
     * @param newsuperior  the newsuperior line (must not be {@code null})
     * @throws IllegalArgumentException if {@code newsuperior} is {@code null}
     */
    public void setNewsuperior( LdifNewsuperiorLine newsuperior )
    {
        if ( newsuperior == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( newsuperior );
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdifNewrdnLine} in this record, or {@code null} if
     * not yet set.
     *
     * @return the new RDN line, or {@code null}
     */
    public LdifNewrdnLine getNewrdnLine()
    {
        for ( LdifPart part : ldifParts )
        {
            if ( part instanceof LdifNewrdnLine )
            {
                return ( LdifNewrdnLine ) part;
            }
        }

        return null;
    }


    /**
     * Returns the {@link LdifDeloldrdnLine} in this record, or {@code null}
     * if not yet set.
     *
     * @return the deleteoldrdn line, or {@code null}
     */
    public LdifDeloldrdnLine getDeloldrdnLine()
    {
        for ( Object part : ldifParts )
        {
            if ( part instanceof LdifDeloldrdnLine )
            {
                return ( LdifDeloldrdnLine ) part;
            }
        }

        return null;
    }


    /**
     * Returns the optional {@link LdifNewsuperiorLine} in this record, or
     * {@code null} if not present.
     *
     * @return the newsuperior line, or {@code null}
     */
    public LdifNewsuperiorLine getNewsuperiorLine()
    {
        for ( Object part : ldifParts )
        {
            if ( part instanceof LdifNewsuperiorLine )
            {
                return ( LdifNewsuperiorLine ) part;
            }
        }

        return null;
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new change-moddn record for {@code dn} with a
     * {@code changetype: moddn} line already set.
     *
     * @param dn  the plain-text distinguished name of the entry to rename
     * @return    a new {@link LdifChangeModDnRecord}
     */
    public static LdifChangeModDnRecord create( String dn )
    {
        LdifChangeModDnRecord record = new LdifChangeModDnRecord( LdifDnLine.create( dn ) );
        record.setChangeType( LdifChangeTypeLine.createModDn() );

        return record;
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when the superclass abstract check passes and both the newrdn
     * and deleteoldrdn lines are present.</p>
     */
    public boolean isValid()
    {
        if ( !super.isAbstractValid() )
        {
            return false;
        }

        return ( getNewrdnLine() != null ) && ( getDeloldrdnLine() != null );
    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns descriptive messages for the two required lines.</p>
     */
    public String getInvalidString()
    {
        if ( getNewrdnLine() == null )
        {
            return "Missing new Rdn";
        }
        else if ( getDeloldrdnLine() == null )
        {
            return "Missing delete old Rdn";
        }
        else
        {
            return super.getInvalidString();
        }
    }
}
