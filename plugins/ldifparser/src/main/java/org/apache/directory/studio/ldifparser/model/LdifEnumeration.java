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

package org.apache.directory.studio.ldifparser.model;


import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.studio.ldifparser.model.container.LdifContainer;


// ── CLASS: LdifEnumeration — REBEL ARCHIVIST HANDS OUT RECORD SCROLLS ─────────
// The Rebel archivist works through a stack of transmission scrolls one by one:
// before handing each one over she checks whether there is another, and the
// caller can ask for the next at any time.
// LdifEnumeration is that protocol: a simple two-method interface (hasNext,
// next) for iterating over LdifContainer objects, throwing LdapException if
// something goes wrong during retrieval.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Iterator-style interface for stepping through a sequence of
 * {@link LdifContainer} objects produced by the LDIF parser.
 * Callers check {@link #hasNext()} before each call to {@link #next()}.
 * Both methods declare {@link LdapException} because implementations may
 * perform I/O (streaming parsers) or LDAP lookups during enumeration.
 * Think of this as the Rebel archivist's protocol for handing out parsed LDIF
 * records one at a time.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface LdifEnumeration
{
    // ── hasNext ───────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this enumeration has at least one more
     * {@link LdifContainer} to return.
     *
     * @return {@code true} if {@link #next()} will succeed
     * @throws LdapException if a retrieval error occurs
     */
    boolean hasNext() throws LdapException;


    // ── next ──────────────────────────────────────────────────────────────────
    /**
     * Returns the next {@link LdifContainer} in the sequence.
     * Returns {@code null} if {@link #hasNext()} is {@code false}.
     *
     * @return the next LDIF container, or {@code null} if exhausted
     * @throws LdapException if a retrieval error occurs
     */
    LdifContainer next() throws LdapException;
}
