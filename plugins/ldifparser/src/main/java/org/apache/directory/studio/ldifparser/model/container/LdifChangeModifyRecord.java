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
import org.apache.directory.studio.ldifparser.model.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;


// ── CLASS: LdifChangeModifyRecord — REBEL AGENT PROFILE UPDATE ORDER ─────────
// When Command needs to update an operative's profile — change their
// communication channel, add a new cover identity, remove an outdated
// attribute — they issue a modify order that lists one or more mod-specs,
// each describing exactly one attribute operation.
// LdifChangeModifyRecord models that update order: a change record with
// changetype "modify" whose payload is a sequence of LdifModSpec sub-containers.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for a {@code changetype: modify} record.
 * Extends {@link LdifChangeRecord} with the ability to add {@link LdifModSpec}
 * sub-containers (each representing one attribute modification operation) and
 * a factory method {@link #create(String)}.
 * A record is valid when the superclass abstract checks pass (at least one mod
 * spec is not required — an empty modify record is structurally valid per RFC
 * 4511).
 * Think of this as the Rebel operative profile-update order — a DN, a modify
 * directive, and a sequence of attribute modification specs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifChangeModifyRecord extends LdifChangeRecord
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a change-modify record starting with the given DN line.
     *
     * @param dn  the distinguished-name line for this record
     */
    public LdifChangeModifyRecord( LdifDnLine dn )
    {
        super( dn );
    }


    // ── ADD A MOD-SPEC ────────────────────────────────────────────────────────
    /**
     * Appends a {@link LdifModSpec} to this modify record.
     *
     * @param modSpec  the mod-spec to add (must not be {@code null})
     * @throws IllegalArgumentException if {@code modSpec} is {@code null}
     */
    public void addModSpec( LdifModSpec modSpec )
    {
        if ( modSpec == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( modSpec );
    }


    // ── GET ALL MOD-SPECS ─────────────────────────────────────────────────────
    /**
     * Returns all {@link LdifModSpec} parts in this record.
     *
     * @return array of mod-specs (may be empty)
     */
    public LdifModSpec[] getModSpecs()
    {
        List<LdifModSpec> ldifModSpecs = new ArrayList<LdifModSpec>();

        for ( LdifPart part : ldifParts )
        {
            if ( part instanceof LdifModSpec )
            {
                ldifModSpecs.add( ( LdifModSpec ) part );
            }
        }

        return ldifModSpecs.toArray( new LdifModSpec[ldifModSpecs.size()] );
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new change-modify record for {@code dn} with a
     * {@code changetype: modify} line already set.
     *
     * @param dn  the plain-text distinguished name of the entry to modify
     * @return    a new {@link LdifChangeModifyRecord}
     */
    public static LdifChangeModifyRecord create( String dn )
    {
        LdifChangeModifyRecord record = new LdifChangeModifyRecord( LdifDnLine.create( dn ) );
        record.setChangeType( LdifChangeTypeLine.createModify() );

        return record;
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to the superclass abstract validity check.</p>
     */
    public boolean isValid()
    {
        return super.isAbstractValid();
    }
}
