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

// ── CLASS: AclWhoClause — DEATH STAR CLEARANCE MANIFEST: WHO SIDE CONTRACT ───
// Every entry on Tarkin's clearance manifest has a "who" section — which person
// or group is being granted or denied access. No matter what type of "who" it
// is (anonymous, a DN, a group, a security-strength requirement), they all must
// answer the same two questions: what access level does this subject get, and
// what happens to evaluation flow after this rule matches? This interface
// enforces that contract on every concrete who-clause type.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The marker contract for every "by" clause in an OpenLDAP ACL. All who-clause
 * types — anonymous, self, users, dn, group, ssf variants — implement this
 * interface so the rest of the model can work with them uniformly: reading or
 * writing an {@link AclAccessLevel} and an optional {@link AclControlEnum}.
 * Think of this interface as Tarkin's clearance form template — every subject
 * on every manifest must have an access level and a flow-control word stamped
 * on their entry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface AclWhoClause
{
    // ── Reading the Clearance Level ───────────────────────────────────────────
    // Tarkin's adjutant reads the clearance level from the manifest entry so
    // he knows how much access this subject is permitted.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the access level granted to the subject described by this clause.
     * The level controls what operations (read, write, manage, etc.) this
     * particular "who" is allowed to perform on the protected resource.
     *
     * <p>For example — reading the access tier from Tarkin's clearance entry:</p>
     * <pre>
     *   AclAccessLevel level = whoClause.getAccessLevel();
     *   // level.getLevel() == AclAccessLevelLevelEnum.READ
     * </pre>
     *
     * @return  The {@link AclAccessLevel} for this who-clause; may be {@code null}
     *          if not yet set.
     */
    AclAccessLevel getAccessLevel();


    // ── Reading the Flow-Control Stamp ────────────────────────────────────────
    // After this entry is processed the adjutant checks the flow-control stamp:
    // stop, continue, or break. This method reads that stamp.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the optional ACL evaluation control word for this clause. Controls
     * whether evaluation stops ({@code stop}), moves to the next rule
     * ({@code continue}), or moves to the next ACL list ({@code break}) after
     * this clause is matched.
     *
     * <p>For example — reading the flow-control directive:</p>
     * <pre>
     *   AclControlEnum ctrl = whoClause.getControl();
     *   // ctrl == AclControlEnum.STOP  →  "stop" appended to ACL text
     * </pre>
     *
     * @return  The {@link AclControlEnum}, or {@code null} if no control was specified.
     */
    AclControlEnum getControl();


    // ── Stamping the Clearance Level ──────────────────────────────────────────
    // Tarkin's adjutant stamps the access level onto a manifest entry when
    // building or editing an ACL rule.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the access level for this who-clause. Called by the parser when it
     * encounters the access-level token in a "by" clause, and by the visual
     * editor when the user changes the level in the UI.
     *
     * <p>For example — the parser stamping READ onto a who-clause entry:</p>
     * <pre>
     *   whoClause.setAccessLevel(new AclAccessLevel(AclAccessLevelLevelEnum.READ));
     * </pre>
     *
     * @param accessLevel  The {@link AclAccessLevel} to apply to this clause.
     */
    void setAccessLevel( AclAccessLevel accessLevel );


    // ── Stamping the Flow-Control Directive ───────────────────────────────────
    // Tarkin stamps the evaluation-flow directive onto the entry to tell the
    // server what to do after this rule fires.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the evaluation flow-control word for this who-clause. Called by the
     * parser when it finds a control keyword (stop/continue/break) after the
     * access level token.
     *
     * <p>For example — the parser recording STOP on a who-clause:</p>
     * <pre>
     *   whoClause.setControl(AclControlEnum.STOP);
     * </pre>
     *
     * @param control  The {@link AclControlEnum} to apply.
     */
    void setControl( AclControlEnum control );
}
