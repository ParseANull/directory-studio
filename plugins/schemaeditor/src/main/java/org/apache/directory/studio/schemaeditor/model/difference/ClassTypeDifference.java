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


// ── CLASS: ClassTypeDifference — Mace Notes The Title Change ─────────────────
// Mace Windu confronts Palpatine with a very specific allegation: "You changed
// your title."  In LDAP object classes, the class type is STRUCTURAL, AUXILIARY,
// or ABSTRACT — like whether you're a Senator, a Chancellor, or a Sith Lord.
// If that category changes between two schema versions, that's a ClassTypeDifference.
// It's always a MODIFIED verdict — you can't add or remove a class type, only swap it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records that the {@code objectClass} type (STRUCTURAL, AUXILIARY, or ABSTRACT)
 * changed between two schema snapshots — always a {@link DifferenceType#MODIFIED}
 * verdict since the type field always exists, it can only change value.
 * The old and new values (both {@code ObjectClassTypeEnum} instances) are stored
 * via the inherited {@link AbstractPropertyDifference} setters.
 * Think of it as Mace noting: "Palpatine changed his title from Supreme Chancellor
 * to Emperor — the role descriptor was swapped."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ClassTypeDifference extends AbstractPropertyDifference
{
    // ── Mace Files The Title-Change Charge ────────────────────────────────────
    // We always stamp MODIFIED here because a class type can only switch from
    // one value to another — it can't be absent.  The engine populates old/new
    // values immediately after via setOldValue / setNewValue.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a class-type difference, always stamping the verdict as
     * {@link DifferenceType#MODIFIED} since the type field can only change, not
     * appear or disappear.
     * Call {@link #setOldValue} and {@link #setNewValue} right after construction
     * to record the actual {@code ObjectClassTypeEnum} values (e.g. STRUCTURAL →
     * AUXILIARY).
     *
     * <p>For example — Mace files the title-change charge:</p>
     * <pre>
     *   ClassTypeDifference diff = new ClassTypeDifference( oc1, oc2 );
     *   diff.setOldValue( ObjectClassTypeEnum.STRUCTURAL );
     *   diff.setNewValue( ObjectClassTypeEnum.AUXILIARY );
     *   // "The class type changed from STRUCTURAL to AUXILIARY — verdict: MODIFIED."
     * </pre>
     *
     * @param source       the original "before" ObjectClass
     * @param destination  the new "after" ObjectClass
     */
    public ClassTypeDifference( Object source, Object destination )
    {
        super( source, destination, DifferenceType.MODIFIED );
    }
}
