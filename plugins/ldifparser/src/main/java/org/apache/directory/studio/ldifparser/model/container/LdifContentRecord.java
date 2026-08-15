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
import org.apache.directory.studio.ldifparser.model.lines.LdifDnLine;


// ── CLASS: LdifContentRecord — REBEL AGENT PROFILE RECORD ────────────────────
// Each Rebel agent in the network has a profile record: a DN (their codename)
// followed by a list of attribute-value pairs describing their skills,
// location, and contact information.  No changetype lines here — this is
// purely descriptive content, not a modification order.
// LdifContentRecord is that profile: a content-only LDIF record holding one
// or more attribute-value lines, with a factory method for programmatic
// construction from a plain DN string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for a content record — a DN line followed by one or more
 * {@link LdifAttrValLine}s terminated by a separator or EOF.
 * A content record is valid when the superclass structural checks pass and at
 * least one attribute-value line is present.
 * Use the static factory {@link #create(String)} to build a content record
 * programmatically from a plain DN string.
 * Think of this as the Rebel agent profile record — pure descriptive content,
 * no change operations.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifContentRecord extends LdifRecord
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a content record starting with the given DN line.
     *
     * @param dn  the distinguished-name line for this record
     */
    public LdifContentRecord( LdifDnLine dn )
    {
        super( dn );
    }


    // ── ADD AN ATTRIBUTE-VALUE LINE ───────────────────────────────────────────
    /**
     * Appends an {@link LdifAttrValLine} to this content record.
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

        for ( LdifPart ldifPart : ldifParts )
        {
            if ( ldifPart instanceof LdifAttrValLine )
            {
                ldifAttrValLines.add( ( LdifAttrValLine ) ldifPart );
            }
        }

        return ldifAttrValLines.toArray( new LdifAttrValLine[ldifAttrValLines.size()] );
    }


    // ── COUNT ATTRIBUTE-VALUE LINES ───────────────────────────────────────────
    /**
     * Returns the number of {@link LdifAttrValLine} parts in this record.
     *
     * @return the attribute-value line count
     */
    private int getSize()
    {
        int size = 0;

        for ( LdifPart ldifPart : ldifParts )
        {
            if ( ldifPart instanceof LdifAttrValLine )
            {
                size++;
            }
        }

        return size;
    }


    // ── FACTORY METHOD ────────────────────────────────────────────────────────
    /**
     * Creates a new content record with a DN line built from the plain
     * {@code dn} string.
     *
     * @param dn  the plain-text distinguished name (not Base64 encoded)
     * @return    a new {@link LdifContentRecord}
     */
    public static LdifContentRecord create( String dn )
    {
        return new LdifContentRecord( LdifDnLine.create( dn ) );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Valid when the superclass structural checks pass and at least one
     * {@link LdifAttrValLine} is present.</p>
     */
    public boolean isValid()
    {
        if ( !super.isAbstractValid() )
        {
            return false;
        }

        return getSize() > 0;

    }


    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code "Record must contain attribute value lines"} when the
     * record has no attribute-value lines; otherwise delegates to the
     * superclass.</p>
     */
    public String getInvalidString()
    {
        if ( getSize() > 0 )
        {
            return super.getInvalidString();
        }
        else
        {
            return "Record must contain attribute value lines";
        }
    }
}
