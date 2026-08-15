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
package org.apache.directory.studio.schemaeditor.model.difference;


// ── CLASS: SuperiorATDifference — PALPATINE SWITCHES HIS SUPERIOR ────────────
// Mace Windu's investigation reveals the key betrayal: Palpatine no longer
// answers to the Senate (his former "superior") — he now answers to Darth
// Plagueis's legacy, a completely different chain of authority.
// This class records the equivalent schema-level betrayal: the "superior"
// attribute type (the parent an attribute type inherits syntax and rules from)
// has been added, removed, or replaced.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to the superior (parent) attribute type of an attribute type definition.
 * In LDAP, an attribute type can inherit syntax and matching rules from a superior attribute type,
 * much like a subclass inherits from a parent class.
 * Think of this as Mace discovering that Palpatine has switched allegiance from the Republic
 * (his declared "superior") to the Sith — a fundamental change in the chain of authority.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SuperiorATDifference extends AbstractPropertyDifference
{
    // ── Mace Formally Names The New Superior ─────────────────────────────────────
    // Mace identifies not just that Palpatine changed masters, but exactly how
    // to classify that change — the superior was added, removed, or replaced.
    // We use this constructor when the verdict is already known.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SuperiorATDifference with an explicit difference type.
     * Use this when the engine already knows whether the superior attribute type
     * was introduced, dropped entirely, or swapped for a different one.
     *
     * <p>For example — Mace files the classified finding:</p>
     * <pre>
     *   SuperiorATDifference diff =
     *       new SuperiorATDifference("name", "uid", DifferenceType.MODIFIED);
     *   // "His declared master has changed — this is the evidence."
     * </pre>
     *
     * @param source       the original superior attribute type name or OID (before the change)
     * @param destination  the updated superior attribute type name or OID (after the change)
     * @param type         ADDED, REMOVED, or MODIFIED — how the superior changed
     */
    public SuperiorATDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Senses The Allegiance Shift ─────────────────────────────────────────
    // Mace knows something changed at the top of the authority chain but
    // defaults to MODIFIED until the full picture emerges.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SuperiorATDifference using the default difference type (MODIFIED).
     * Use this when the superior attribute type has clearly changed but the engine
     * isn't committing to ADDED or REMOVED yet.
     *
     * @param source       the original superior attribute type (before the change)
     * @param destination  the updated superior attribute type (after the change)
     */
    public SuperiorATDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
