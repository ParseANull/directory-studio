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


// ── CLASS: UsageDifference — PALPATINE IS NO LONGER "USED" AS CHANCELLOR ─────
// After the confrontation, Chancellor Palpatine is no longer used in the role
// the Republic assigned him — his usage has fundamentally changed, from
// "elected official" to "Emperor."
// This class records the schema-level equivalent: an attribute type's USAGE field
// (which governs whether it's for regular user data, operational data, or DSA-specific
// data) has flipped, and the change type is always MODIFIED.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to the USAGE field of an attribute type.
 * LDAP attribute types have a USAGE that tells servers how to treat them:
 * {@code userApplications} (regular directory data), {@code directoryOperation},
 * {@code distributedOperation}, or {@code dSAOperation} (internal server bookkeeping).
 * Changing usage typically means a schema overhaul — the attribute's role in the
 * directory has fundamentally shifted.
 * Think of this as the moment Palpatine's role changes from Chancellor to Emperor —
 * the same entity, but a completely different USAGE in the system.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UsageDifference extends AbstractPropertyDifference
{
    // ── The Role Has Irrevocably Changed ─────────────────────────────────────────
    // There is no ambiguity about Palpatine's new role — the verdict is
    // MODIFIED because USAGE can only change value, never be absent entirely.
    // We hard-code DifferenceType.MODIFIED for the same reason.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new UsageDifference recording that an attribute type's USAGE field has changed.
     * The type is always {@link DifferenceType#MODIFIED} — every attribute type has a USAGE value,
     * so this change is always a modification, never an addition or removal.
     *
     * <p>For example — Mace files the final charge:</p>
     * <pre>
     *   UsageDifference diff =
     *       new UsageDifference(UsageEnum.USER_APPLICATIONS, UsageEnum.DIRECTORY_OPERATION);
     *   // "He was Chancellor. Now he rules as Emperor. That's a USAGE change."
     * </pre>
     *
     * @param source       the old USAGE value (before the change)
     * @param destination  the new USAGE value (after the change)
     */
    public UsageDifference( Object source, Object destination )
    {
        super( source, destination, DifferenceType.MODIFIED );
    }
}
