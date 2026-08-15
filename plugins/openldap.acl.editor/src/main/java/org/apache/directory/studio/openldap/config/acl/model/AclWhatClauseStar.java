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

// ── CLASS: AclWhatClauseStar — DEATH STAR MANIFEST: EVERYTHING IN THE GALAXY ─
// On Tarkin's clearance manifest the catch-all "access to *" applies the rule
// to every single entry in the entire LDAP directory — no DN filter, no
// attribute filter. This is the nuclear option: protect everything with this
// one rule. This class models that universal what-clause.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete what-clause that matches every entry in the directory. In an
 * OpenLDAP ACL the "access to *" form is the universal selector — nothing is
 * excluded. When we serialise this we just return {@code "*"}.
 * Think of this class as the Death Star's "all sectors" marker on Tarkin's
 * manifest — one notation that covers the entire directory tree.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhatClauseStar extends AclWhatClause
{
    // ── Rendering the Universal What-Clause ───────────────────────────────────
    // There is nothing to print except the star. No DN, no filter, no attrs —
    // just the single character that says "everything".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLDAP wire-format representation of this what-clause.
     * Since this is the universal selector there is nothing more to say than
     * {@code "*"}.
     *
     * <p>For example — Tarkin writing the all-sectors notation:</p>
     * <pre>
     *   new AclWhatClauseStar().toString() // → "*"
     * </pre>
     *
     * @return  The string {@code "*"}.
     */
    public String toString()
    {
        return "*";
    }
}
