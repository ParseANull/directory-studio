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


import org.apache.directory.studio.ldifparser.model.lines.LdifCommentLine;


// ── CLASS: LdifCommentContainer — REBEL ARCHIVIST'S MARGIN NOTES ─────────────
// The Rebel archivist scribbles notes in the margins of the transmission log:
// context, warnings, attribution.  These comment blocks stand on their own
// between records — they're not part of any entry, just free-standing
// annotation.
// LdifCommentContainer holds those margin notes: one or more consecutive
// comment lines grouped into a single top-level container.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container holding one or more consecutive top-level comment lines
 * ({@link LdifCommentLine}s that appear outside any record).
 * {@link #isValid()} delegates to {@link #isAbstractValid()}.
 * Think of this as the Rebel archivist's free-standing margin-note block.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifCommentContainer extends LdifContainer
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a comment container starting with {@code comment} as its first
     * line.
     *
     * @param comment  the first comment line (must not be {@code null})
     */
    public LdifCommentContainer( LdifCommentLine comment )
    {
        super( comment );
    }


    // ── ADD A COMMENT LINE ────────────────────────────────────────────────────
    /**
     * Appends another {@link LdifCommentLine} to this container.
     *
     * @param comment  the comment line to append (must not be {@code null})
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
