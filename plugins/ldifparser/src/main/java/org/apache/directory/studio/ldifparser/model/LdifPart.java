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


import org.apache.directory.studio.ldifparser.LdifFormatParameters;


// ── CLASS: LdifPart — DEATH STAR BLUEPRINT FRAGMENT INTERFACE ─────────────────
// Every individual section of the Death Star blueprint — the throne room, the
// reactor shaft, the superlaser — is a fragment of the larger plan: each
// fragment knows where it starts, how long it is, whether it's valid, and how
// to serialise itself back to raw or formatted text.
// LdifPart is that blueprint-fragment contract: the interface every parsed LDIF
// token (line, separator, modspec, EOF marker) implements so the rest of the
// model code can treat all parts uniformly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base interface for every parsed token in an LDIF document.
 * Each implementing class represents one logical token: a DN line, an
 * attribute-value line, a changetype line, a comment, a separator, a modspec,
 * an EOF marker, or an invalid fragment.
 * Think of this as the contract every Death Star blueprint fragment must
 * satisfy so the architect can query any piece uniformly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface LdifPart
{
    // ── POSITION ──────────────────────────────────────────────────────────────
    /**
     * Returns the byte offset of this part's first character in the original
     * LDIF document.
     *
     * @return the zero-based character offset
     */
    int getOffset();


    /**
     * Returns the number of characters this part occupies.
     *
     * @return the character length (may be {@code 0} for EOF parts)
     */
    int getLength();


    // ── VALIDITY ──────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this part was parsed successfully and carries
     * valid LDIF content.
     *
     * @return {@code true} for valid parts, {@code false} for
     *         {@link LdifInvalidPart}
     */
    boolean isValid();


    /**
     * Returns a human-readable description of why this part is invalid.
     * Returns an empty string for valid parts.
     *
     * @return the invalid-reason string, or empty string if valid
     */
    String getInvalidString();


    // ── SERIALISATION ─────────────────────────────────────────────────────────
    /**
     * Returns this part exactly as it appeared in the original LDIF input,
     * including the trailing newline.
     *
     * @return the raw (unformatted) string for this part
     */
    String toRawString();


    /**
     * Returns a re-formatted version of this part using the supplied
     * {@code formatParameters} (line width, space-after-colon, line separator).
     *
     * @param formatParameters  the formatting options to apply
     * @return the formatted string for this part
     */
    String toFormattedString( LdifFormatParameters formatParameters );


    // ── OFFSET ADJUSTMENT ────────────────────────────────────────────────────
    /**
     * Shifts the stored offset by {@code adjust} characters.
     * Called by {@link LdifFile#replace} after inserting or removing containers
     * to keep absolute offsets consistent.
     *
     * @param adjust  the number of characters to add to the stored offset
     *                (may be negative)
     */
    void adjustOffset( int adjust );
}
