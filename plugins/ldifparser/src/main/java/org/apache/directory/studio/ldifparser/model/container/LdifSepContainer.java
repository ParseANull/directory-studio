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


import org.apache.directory.studio.ldifparser.model.lines.LdifSepLine;


// ── CLASS: LdifSepContainer — REBEL COMMS BLANK-LINE DIVIDER ──────────────────
// The Rebel comms network uses blank lines between transmission records as
// dividers: one or more consecutive blank lines form a separator block that
// tells the receiver "one record ended here, the next one starts after this".
// LdifSepContainer holds those blank-line blocks as top-level containers so
// the full document structure is preserved even between records.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for one or more consecutive blank-line separators that
 * appear between top-level records (as distinct from the trailing separator
 * that terminates a record itself).
 * {@link #isValid()} delegates to {@link #isAbstractValid()}.
 * Think of this as the Rebel comms blank-line block between transmissions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifSepContainer extends LdifContainer
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a separator container starting with {@code sep} as its first line.
     *
     * @param sep  the first separator line (must not be {@code null})
     */
    public LdifSepContainer( LdifSepLine sep )
    {
        super( sep );
    }


    // ── ADD A SEPARATOR LINE ──────────────────────────────────────────────────
    /**
     * Appends another blank {@link LdifSepLine} to this container.
     *
     * @param sep  the separator line to append (must not be {@code null})
     * @throws IllegalArgumentException if {@code sep} is {@code null}
     */
    public void addSep( LdifSepLine sep )
    {
        if ( sep == null )
        {
            throw new IllegalArgumentException( "null argument" ); //$NON-NLS-1$
        }

        ldifParts.add( sep );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #isAbstractValid()}.</p>
     */
    public boolean isValid()
    {
        return super.isAbstractValid();
    }
}
