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

import org.apache.directory.studio.ldifparser.model.LdifPart;
import org.apache.directory.studio.ldifparser.model.lines.LdifAttrValLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;


// ── CLASS: LdifChangeAddRecord — REBEL NEW AGENT ENROLLMENT ORDER ─────────────
// When the Rebellion recruits a new agent it issues an enrollment order:
// a DN (the new codename), "changetype: add", and then all the attribute lines
// describing the agent's profile.  At least one attribute must be present or
// the enrollment is incomplete.
// LdifChangeAddRecord models that enrollment order: a change record with
// changetype "add" whose validity requires at least one LdifAttrValLine.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for a {@code changetype: add} record.
 * Extends {@link LdifChangeRecord} with the ability to add
 * {@link LdifAttrValLine}s and a factory method {@link #create(String)}.
 * A record is valid when the superclass abstract checks pass and at least one
 * attribute-value line is present.
 * Think of this as the Rebel new-agent enrollment order — the DN, the add
 * directive, and the full attribute list.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifChangeAddRecord extends LdifChangeRecord
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a change-add record starting with the given DN line.
     *
     * @param dn  the distinguished-name line for this record
     */
    public LdifChangeAddRecord( LdifDnLine dn )
    {
        super( dn );
    }


    // ── ADD AN ATTRIBUTE-VALUE LINE ───────────────────────────────────────────
    /**
     * Appends an {@link LdifAttrValLine} to this add record.
     *
     * @param attrVal  the attribute-value line to add (must not be {@code null})
     * @throws IllegalArgumentException if {@code attrVal} is {@code null}
     */
    public void addAttrVal( LdifAttrValLine attrVal )
    {
        if ( attrVal == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( attrVal );
    }


    // ── GET ALL ATTRIBUTE-VALUE LINES ─────────────────────────────────────────
    /**
     * Returns all {@link LdifAttrValLine} parts in this record.
     *
     * @return array of attribute-value lines (may be empty for invalid records)
     */
    public LdifAttrValLine[] getAttrVals()
    {
        List<LdifAttrValLine> ldifAttrValLines = new ArrayList<LdifAttrValLine>();

        for ( LdifPart part : ldifParts )
        {
            if ( part instanceof LdifAttrValLine )
            {
                ldifAttrValLines.add( ( LdifAttrValLine ) part );
            }
        }

        return ldifAttrValLines.toArray( new LdifAttrValLine[ldifAttrValLines.size()] );
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new change-add record for {@code dn} with a
     * {@code changetype: add} line already set.
     *
     * @param dn  the plain-text distinguished name for the new entry
     * @return    a new {@link LdifChangeAddRecord}
     */
    public static LdifChangeAddRecord create( String dn )
    {
        LdifChangeAddRecord record = new LdifChangeAddRecord( LdifDnLine.create( dn ) );
        record.setChangeType( LdifChangeTypeLine.createAdd() );

        return record;
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when the superclass abstract check passes and at least one
     * {@link LdifAttrValLine} is present in the parts list.</p>
     */
    public boolean isValid()
    {
        if ( !super.isAbstractValid() )
        {
            return false;
        }

        for ( LdifPart part : ldifParts )
        {
            if ( part instanceof LdifAttrValLine )
            {
                return true;
            }
        }

        return false;
    }
}
