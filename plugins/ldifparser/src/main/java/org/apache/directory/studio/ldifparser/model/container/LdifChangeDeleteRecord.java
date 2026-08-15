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


import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;


// ── CLASS: LdifChangeDeleteRecord — REBEL AGENT REMOVAL ORDER ────────────────
// When a Rebel operative is compromised or retires, command issues a removal
// order: just the DN and "changetype: delete" — no attribute lines needed,
// the entry is simply erased.  Validity requires both the changetype and the
// trailing separator to be present.
// LdifChangeDeleteRecord models that removal order: the simplest change record
// (DN + changetype: delete + blank-line terminator) with no payload lines.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for a {@code changetype: delete} record.
 * Extends {@link LdifChangeRecord} — no attribute lines are needed for a
 * delete.  A record is valid when the superclass abstract checks pass and both
 * the changetype line and the separator line are present.
 * A factory method {@link #create(String)} builds a complete delete record
 * from a plain DN string.
 * Think of this as the Rebel operative removal order — just the DN and the
 * delete directive.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifChangeDeleteRecord extends LdifChangeRecord
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a change-delete record starting with the given DN line.
     *
     * @param dn  the distinguished-name line for this record
     */
    public LdifChangeDeleteRecord( LdifDnLine dn )
    {
        super( dn );
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new change-delete record for {@code dn} with a
     * {@code changetype: delete} line already set.
     *
     * @param dn  the plain-text distinguished name of the entry to delete
     * @return    a new {@link LdifChangeDeleteRecord}
     */
    public static LdifChangeDeleteRecord create( String dn )
    {
        LdifChangeDeleteRecord record = new LdifChangeDeleteRecord( LdifDnLine.create( dn ) );
        record.setChangeType( LdifChangeTypeLine.createDelete() );

        return record;
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when the superclass abstract check passes, the changetype line
     * is present, and the separator line is present.</p>
     */
    public boolean isValid()
    {
        if ( !super.isAbstractValid() )
        {
            return false;
        }

        return ( getChangeTypeLine() != null ) && ( getSepLine() != null );
    }
}
