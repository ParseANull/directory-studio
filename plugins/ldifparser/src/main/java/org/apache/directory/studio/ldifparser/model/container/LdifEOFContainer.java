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


// ── CLASS: LdifEOFContainer — REBEL TRANSMISSION END-OF-FILE MARKER ──────────
// The very last item in every Rebel data transmission is the end-of-file
// container: a structural wrapper around the zero-length EOF sentinel that
// gives the rest of the model a concrete LdifContainer to hold in the list
// rather than a bare sentinel part.
// LdifEOFContainer wraps a single LdifEOFPart and is valid when that part is
// still its last child.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container that wraps the end-of-file sentinel ({@link LdifEOFPart}).
 * Always the last container in a parsed {@link org.apache.directory.studio.ldifparser.model.LdifFile}.
 * {@link #isValid()} returns {@code true} when the last child part is a
 * {@link LdifEOFPart}.
 * Think of this as the structural wrapper around the "Transmission Complete"
 * sentinel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEOFContainer extends LdifContainer
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates an EOF container wrapping {@code eofPart}.
     *
     * @param eofPart  the EOF part to wrap (must not be {@code null})
     */
    public LdifEOFContainer( LdifEOFPart eofPart )
    {
        super( eofPart );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return {@code true} when the last part is a {@link LdifEOFPart}
     */
    public boolean isValid()
    {
        return getLastPart() instanceof LdifEOFPart;
    }
}
