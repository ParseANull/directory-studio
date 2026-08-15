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


// ── CLASS: DescriptionDifference — Mace Notes The Story Changed ──────────────
// Mace Windu checks the official biography of the accused: the DESCRIPTION field.
// An LDAP schema element's description is a free-text summary of what it's for
// (e.g. "The common name of a person").  If that text was added, removed, or
// rewritten between two schema versions, that's a DescriptionDifference —
// Mace noting that Palpatine rewrote his own official bio mid-confrontation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records that the free-text {@code DESCRIPTION} field on a schema element
 * (AttributeType or ObjectClass) was added, removed, or reworded between two
 * schema snapshots.
 * The verdict can be ADDED (was null, now has text), REMOVED (had text, now
 * null), or MODIFIED (text changed).
 * Think of it as Mace noting: "The official description on this schema element
 * was altered — the accused rewrote their own biography."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DescriptionDifference extends AbstractPropertyDifference
{
    // ── Mace Files A Description Charge With Pre-Known Verdict ───────────────
    // The engine knows the verdict at detection time: ADDED if it only exists in
    // the destination, REMOVED if only in the source, MODIFIED if both exist but
    // the text differs.  We stamp it up front and record the strings via setters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a description difference with verdict already known.
     * Follow up immediately with {@link #setOldValue}/{@link #setNewValue} to
     * record the actual description strings that changed.
     *
     * <p>For example — Mace files a description-modified charge:</p>
     * <pre>
     *   DescriptionDifference diff =
     *       new DescriptionDifference( at1, at2, DifferenceType.MODIFIED );
     *   diff.setOldValue( "cn" );
     *   diff.setNewValue( "The common name of the object" );
     * </pre>
     *
     * @param source       the original "before" schema object
     * @param destination  the new "after" schema object
     * @param type         ADDED, REMOVED, or MODIFIED
     */
    public DescriptionDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Opens A Description Charge Without A Verdict Yet ────────────────
    // Less commonly used variant — verdict determined and set separately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a description difference without a verdict; call {@link #setType}
     * afterwards.
     * Prefer the three-argument constructor when the type is already known.
     *
     * <p>For example — Mace opens the sheet before deciding the verdict:</p>
     * <pre>
     *   DescriptionDifference diff = new DescriptionDifference( at1, at2 );
     *   diff.setType( DifferenceType.ADDED );
     *   diff.setNewValue( "New description text" );
     * </pre>
     *
     * @param source       the original "before" schema object
     * @param destination  the new "after" schema object
     */
    public DescriptionDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
