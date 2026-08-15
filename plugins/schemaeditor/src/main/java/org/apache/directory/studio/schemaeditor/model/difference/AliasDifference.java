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


// ── CLASS: AliasDifference — Mace Checks The Name On The Dossier ─────────────
// Mace Windu confronts Palpatine with a very specific allegation: "The name on
// this schema object changed."  In LDAP, aliases are the human-readable names
// for an attribute type or object class (like "cn" or "commonName").  If a name
// was added or removed between two schema versions, that's an AliasDifference —
// Mace noting that the accused's name plate has been swapped.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records that an alias (human-readable name) was added to or removed from a
 * schema element between two schema snapshots.
 * Each instance captures one name change — if three aliases changed, there will
 * be three separate AliasDifference objects, one per name.
 * Think of it as Mace Windu noting: "The name on this dossier was changed — it
 * used to say 'commonName' and now it says 'cn' (or vice versa)."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AliasDifference extends AbstractPropertyDifference
{
    // ── Mace Files A Specific Name Charge With A Pre-Known Verdict ────────────
    // Mace already knows whether the name was added or removed (only one side
    // of the comparison has this alias), so he stamps the verdict up front and
    // records the specific alias that appeared or disappeared via setOldValue /
    // setNewValue immediately after construction.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an alias difference with source, destination, and verdict known
     * at construction time.
     * Typically followed immediately by {@code setOldValue(name)} for REMOVED
     * or {@code setNewValue(name)} for ADDED, so the UI can show which alias
     * specifically changed.
     *
     * <p>For example — Mace files a name-removal charge:</p>
     * <pre>
     *   AliasDifference diff = new AliasDifference( at1, at2, DifferenceType.REMOVED );
     *   diff.setOldValue( "commonName" );
     *   // "The name 'commonName' was removed from AttributeType 'cn'."
     * </pre>
     *
     * @param source       the original "before" schema object whose alias list changed
     * @param destination  the new "after" schema object
     * @param type         {@link DifferenceType#ADDED} or {@link DifferenceType#REMOVED}
     */
    public AliasDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Files A Name Charge Without A Pre-Known Verdict ──────────────────
    // Less commonly used — for when the verdict will be determined and set
    // separately after construction via setType().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an alias difference without a verdict; the type must be set later
     * via {@link #setType}.
     * Use the three-argument constructor instead whenever the type is already
     * known — this variant exists for framework completeness.
     *
     * <p>For example — Mace opens the charge sheet before determining direction:</p>
     * <pre>
     *   AliasDifference diff = new AliasDifference( at1, at2 );
     *   diff.setType( DifferenceType.ADDED );
     *   diff.setNewValue( "sn" );
     * </pre>
     *
     * @param source       the original "before" schema object
     * @param destination  the new "after" schema object
     */
    public AliasDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
