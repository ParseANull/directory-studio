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


// ── CLASS: CollectiveDifference — Mace Notes The Collective Flag Change ───────
// Mace Windu checks the attribute type dossier and flags a very specific charge:
// "The 'collective' flag was toggled."  In LDAP, a collective attribute is one
// whose values are shared across all entries in a subtree — like a policy that
// applies to an entire unit.  If that boolean flips between two schema versions,
// that's a CollectiveDifference — always MODIFIED, since it's a boolean toggle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records that the {@code COLLECTIVE} boolean on an LDAP AttributeType changed
 * between two schema snapshots.
 * The verdict is always {@link DifferenceType#MODIFIED} — the flag is always
 * present, it simply flipped from true to false or vice versa.
 * Think of it as Mace noting: "This attribute's collective status was switched —
 * a squad-wide order was quietly promoted (or demoted) to a solo order."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CollectiveDifference extends AbstractPropertyDifference
{
    // ── Mace Flags The Collective Toggle ─────────────────────────────────────
    // The collective flag is always present on an attribute type; it either
    // changed (true → false or false → true) or it didn't.  When it changed,
    // we stamp MODIFIED and let the engine fill in the old/new boolean values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a collective difference, always stamped as
     * {@link DifferenceType#MODIFIED} since the flag can only toggle, not appear
     * or disappear.
     * Set the old and new {@code Boolean} values immediately after via
     * {@link #setOldValue} and {@link #setNewValue}.
     *
     * <p>For example — Mace flags the toggle:</p>
     * <pre>
     *   CollectiveDifference diff = new CollectiveDifference( at1, at2 );
     *   diff.setOldValue( Boolean.FALSE );
     *   diff.setNewValue( Boolean.TRUE );
     *   // "The attribute became collective — verdict: MODIFIED."
     * </pre>
     *
     * @param source       the original "before" AttributeType
     * @param destination  the new "after" AttributeType
     */
    public CollectiveDifference( Object source, Object destination )
    {
        super( source, destination, DifferenceType.MODIFIED );
    }
}
