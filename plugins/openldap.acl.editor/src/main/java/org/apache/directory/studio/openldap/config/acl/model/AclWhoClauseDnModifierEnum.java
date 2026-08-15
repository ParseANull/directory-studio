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


// ── CLASS: AclWhoClauseDnModifierEnum — IMPERIAL DN EXPANSION DIRECTIVE ──────
// When C-3PO reads a DN pattern in a who-clause he sometimes encounters the
// "expand" modifier — it means the pattern contains back-references (like
// $1, $2) that should be filled in from a regex match earlier in the rule.
// Think of it as Tarkin writing an order with a fill-in-the-blank: "admit
// the holder of clearance $1 from sector $2." This enum names that one modifier.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The optional modifier for a DN who-clause. Currently only {@code EXPAND}
 * is defined — it tells OpenLDAP to substitute regex back-references in the
 * DN pattern before matching. Written as {@code dn[.type,expand]="pattern"}.
 * Think of this enum as the single "fill in the blanks" directive that makes
 * Tarkin's DN patterns dynamic rather than fixed strings.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclWhoClauseDnModifierEnum
{
    EXPAND;


    // ── Converting the Modifier to Its Protocol Keyword ───────────────────────
    // The only modifier we know is "expand" — one word, lower-case, exactly
    // as OpenLDAP expects to see it in the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLDAP wire-format name for this modifier. Currently always
     * {@code "expand"} since that is the only modifier defined.
     *
     * <p>For example — C-3PO writing Tarkin's expansion modifier into the ACL:</p>
     * <pre>
     *   AclWhoClauseDnModifierEnum.EXPAND.toString() // → "expand"
     * </pre>
     *
     * @return  The OpenLDAP keyword for this modifier.
     */
    public String toString()
    {
        switch ( this )
        {
            case EXPAND:
                return "expand";
        }

        return super.toString();
    }
}
