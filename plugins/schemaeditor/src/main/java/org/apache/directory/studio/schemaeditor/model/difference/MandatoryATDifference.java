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


// ── CLASS: MandatoryATDifference — Mace Notes A Required Field Was Changed ───
// Mace Windu pores over the MUST clause in an object class's dossier: the list
// of attribute types that every entry of that class MUST carry (like requiring
// a 'cn' on every person entry).  If a required attribute type was added to or
// removed from that MUST list between two schema versions, that's a
// MandatoryATDifference — Mace flagging: "A required credential changed hands."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records that a mandatory attribute type (MUST clause entry) was added to or
 * removed from an LDAP ObjectClass between two schema snapshots.
 * Each instance represents one name that entered or left the MUST list.
 * Think of it as Mace noting: "A required identification credential was either
 * added to or stripped from the list of things this object class demands."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MandatoryATDifference extends AbstractPropertyDifference
{
    // ── Mace Files A Required-Field Charge With Pre-Known Verdict ────────────
    // The engine knows whether a MUST attribute was added (only in destination)
    // or removed (only in source) at detection time.  Verdict is stamped up
    // front; the specific attribute name is recorded via setOldValue/setNewValue.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a mandatory-attribute-type difference with verdict pre-stamped.
     * Immediately after, call {@link #setOldValue} (for REMOVED) or
     * {@link #setNewValue} (for ADDED) with the attribute type name/OID that
     * entered or left the MUST list.
     *
     * <p>For example — Mace files the charge:</p>
     * <pre>
     *   MandatoryATDifference diff =
     *       new MandatoryATDifference( oc1, oc2, DifferenceType.ADDED );
     *   diff.setNewValue( "mail" );
     *   // "'mail' was added to the MUST list of ObjectClass 'inetOrgPerson'."
     * </pre>
     *
     * @param source       the original "before" ObjectClass
     * @param destination  the new "after" ObjectClass
     * @param type         ADDED or REMOVED
     */
    public MandatoryATDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Opens The Required-Field Charge Without A Verdict ───────────────
    // Deferred-verdict variant for framework completeness.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a mandatory-attribute-type difference without a verdict; call
     * {@link #setType} afterwards.
     * Prefer the three-argument constructor in practice.
     *
     * <p>For example — Mace opens the sheet first:</p>
     * <pre>
     *   MandatoryATDifference diff = new MandatoryATDifference( oc1, oc2 );
     *   diff.setType( DifferenceType.REMOVED );
     *   diff.setOldValue( "sn" );
     * </pre>
     *
     * @param source       the original "before" ObjectClass
     * @param destination  the new "after" ObjectClass
     */
    public MandatoryATDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
