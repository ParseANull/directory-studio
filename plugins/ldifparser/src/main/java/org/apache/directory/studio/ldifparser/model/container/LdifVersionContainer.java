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


import org.apache.directory.studio.ldifparser.model.lines.LdifVersionLine;


// ── CLASS: LdifVersionContainer — IMPERIAL COMMS PROTOCOL HEADER ─────────────
// Every Imperial transmission begins with a protocol header declaring the
// version of the comms standard in use: "version: 1".  Without this header
// the receiver may not know how to interpret what follows.
// LdifVersionContainer wraps that single LdifVersionLine and validates that
// the last part is still a version line — a structural check so a malformed
// header is caught immediately.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * LDIF container for the optional {@code version: 1} header line at the top
 * of an LDIF file.
 * {@link #isValid()} returns {@code true} when the superclass abstract check
 * passes and the last part is a {@link LdifVersionLine}.
 * Think of this as the Imperial comms protocol header that opens every
 * standards-compliant LDIF transmission.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifVersionContainer extends LdifContainer
{
    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a version container wrapping {@code versionLine}.
     *
     * @param versionLine  the {@code version: 1} line (must not be {@code null})
     */
    public LdifVersionContainer( LdifVersionLine versionLine )
    {
        super( versionLine );
    }


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return {@code true} when the superclass abstract check passes and the
     *         last child part is a {@link LdifVersionLine}
     */
    public boolean isValid()
    {
        if ( !super.isAbstractValid() )
        {
            return false;
        }

        return getLastPart() instanceof LdifVersionLine;
    }
}
