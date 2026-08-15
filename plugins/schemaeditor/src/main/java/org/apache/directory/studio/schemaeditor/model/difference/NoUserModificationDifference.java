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


// ── CLASS: NoUserModificationDifference — Mace Notes The Lock Changed ────────
// Mace Windu checks whether the "no user modification" lock was toggled.
// In LDAP, the NO-USER-MODIFICATION flag means regular users can't write to that
// attribute — only the server itself can (think: operational attributes like
// 'entryDN').  If that lock was toggled between two schema versions, that's a
// NoUserModificationDifference — Mace noting that someone changed whether the
// Senate chamber doors are open to the public or sealed to the Emperor alone.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records that the {@code NO-USER-MODIFICATION} boolean on an LDAP AttributeType
 * was flipped between two schema snapshots — always a
 * {@link DifferenceType#MODIFIED} verdict.
 * The old and new {@code Boolean} values are stored via the inherited setters.
 * Think of it as Mace noting: "The write-lock on this attribute was changed —
 * either the public lost write access or gained it back."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NoUserModificationDifference extends AbstractPropertyDifference
{
    // ── Mace Flags The Lock Toggle ────────────────────────────────────────────
    // NO-USER-MODIFICATION is always present as a boolean; it can only flip, not
    // appear or disappear.  We stamp MODIFIED at construction and let the engine
    // fill the old/new boolean values immediately after.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a no-user-modification difference, always stamped as
     * {@link DifferenceType#MODIFIED} since the flag is always present and can
     * only toggle between true and false.
     * Set the old and new {@code Boolean} values right after via
     * {@link #setOldValue} and {@link #setNewValue}.
     *
     * <p>For example — Mace flags the lock toggle:</p>
     * <pre>
     *   NoUserModificationDifference diff =
     *       new NoUserModificationDifference( at1, at2 );
     *   diff.setOldValue( Boolean.FALSE );
     *   diff.setNewValue( Boolean.TRUE );
     *   // "The attribute is now read-only for regular users — lock was engaged."
     * </pre>
     *
     * @param source       the original "before" AttributeType
     * @param destination  the new "after" AttributeType
     */
    public NoUserModificationDifference( Object source, Object destination )
    {
        super( source, destination, DifferenceType.MODIFIED );
    }
}
