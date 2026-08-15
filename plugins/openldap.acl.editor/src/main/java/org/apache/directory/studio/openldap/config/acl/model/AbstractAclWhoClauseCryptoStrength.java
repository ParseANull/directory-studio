/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.model;


// ── CLASS: AbstractAclWhoClauseCryptoStrength — IMPERIAL ENCRYPTION CLEARANCE BASE ──
// Tarkin's security bureau sometimes grants access based on the cryptographic
// strength of the connection rather than the identity of the caller. All four
// SSF who-clause variants (ssf, tls_ssf, sasl_ssf, transport_ssf) share one
// thing: a minimum strength integer. This abstract base class factors that
// common integer field and its getter/setter out so all four variants can
// inherit it without duplicating code.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base for all SSF (Security Strength Factor) based who-clause
 * subclasses. Adds a {@code strength} integer that the parser fills in after
 * recognising {@code ssf=N}, {@code tls_ssf=N}, {@code sasl_ssf=N}, or
 * {@code transport_ssf=N} in the ACL text. Subclasses prepend their specific
 * keyword when serialising.
 * Think of this class as the shared badge template for Tarkin's cryptographic
 * checkpoints — every checkpoint type needs to store a minimum-strength number,
 * so we factor it out here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractAclWhoClauseCryptoStrength extends AbstractAclWhoClause
{
    /** The strength */
    protected int strength;


    // ── Reading the Minimum Strength Requirement ──────────────────────────────
    // Tarkin's checkpoint guard reads the minimum strength value off the badge
    // template so the serialiser knows what number to write after the "=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the minimum Security Strength Factor (SSF) stored in this clause.
     * SSF is roughly equivalent to effective encryption key length in bits.
     * Subclasses prefix this value with their specific keyword (e.g. "ssf=").
     *
     * <p>For example — reading the minimum strength off a tls_ssf clause:</p>
     * <pre>
     *   clause.setStrength(128);
     *   int s = clause.getStrength(); // → 128
     * </pre>
     *
     * @return  The minimum SSF integer.
     */
    public int getStrength()
    {
        return strength;
    }


    // ── Stamping the Minimum Strength Requirement ─────────────────────────────
    // The parser reads "ssf=128" and calls this setter to store 128; later
    // the serialiser reads it back and puts it after the keyword.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the minimum Security Strength Factor for this clause. Called by the
     * ANTLR parser after it extracts the integer from tokens like {@code ssf=56}.
     * Also called by the visual editor when the user adjusts the strength spinner.
     *
     * <p>For example — the parser storing the strength from "tls_ssf=256":</p>
     * <pre>
     *   clause.setStrength(256);
     *   clause.toString(); // → "tls_ssf=256 read"
     * </pre>
     *
     * @param strength  The minimum SSF value extracted from the ACL text.
     */
    public void setStrength( int strength )
    {
        this.strength = strength;
    }
}
