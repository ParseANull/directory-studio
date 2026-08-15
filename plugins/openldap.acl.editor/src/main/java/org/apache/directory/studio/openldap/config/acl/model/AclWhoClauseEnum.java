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


// ── CLASS: AclWhoClauseEnum — CLOUD CITY IDENTITY CATEGORY REGISTRY ──────────
// Lando runs a tight registry at Cloud City: every visitor is stamped with a
// category before entering — star (everyone), anonymous, registered user, self,
// a specific DN, a DN-attribute reference, a group, or a security-strength
// requirement (SSF/TLS/SASL/Transport). This enum lists all those categories
// and provides a factory method that maps a live AclWhoClause object back to
// its category label — perfect for the combo box in the visual editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An enumeration of all the "who" clause types that OpenLDAP supports. Each
 * constant corresponds to one concrete {@link AclWhoClause} subclass. The
 * static {@link #get(AclWhoClause)} factory method lets the UI determine which
 * category a given clause object belongs to, so it can pre-select the right
 * item in the "who" type combo box.
 * Think of this enum as Lando's visitor category registry — every who-clause
 * gets stamped with exactly one of these categories when it arrives.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclWhoClauseEnum
{
    STAR,
    ANONYMOUS,
    USERS,
    SELF,
    DN,
    DNATTR,
    GROUP,
    SASL_SSF,
    SSF,
    TLS_SSF,
    TRANSPORT_SSF;


    // ── Categorising a Live Who-Clause Object ─────────────────────────────────
    // Lando's door guard glances at a visitor, checks which type they are, and
    // stamps the right category on their badge. We do the same: inspect the
    // runtime type of the clause with instanceof and return the enum constant
    // that matches. Returns null for unknown types (should not happen in practice).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Determines which enum constant corresponds to a given {@link AclWhoClause}
     * instance by checking its runtime type. The visual editor uses this to
     * pre-select the correct "who type" in its combo box when loading an
     * existing ACL rule.
     *
     * <p>For example — Lando's guard stamping a visitor badge:</p>
     * <pre>
     *   AclWhoClause clause = new AclWhoClauseUsers();
     *   AclWhoClauseEnum type = AclWhoClauseEnum.get(clause);
     *   // type == AclWhoClauseEnum.USERS
     * </pre>
     *
     * @param clause  The who-clause object to categorise.
     * @return        The matching enum constant, or {@code null} if the type is unrecognised.
     */
    public static AclWhoClauseEnum get( AclWhoClause clause )
    {
        if ( clause instanceof AclWhoClauseStar )
        {
            return STAR;
        }
        else if ( clause instanceof AclWhoClauseAnonymous )
        {
            return ANONYMOUS;
        }
        else if ( clause instanceof AclWhoClauseUsers )
        {
            return USERS;
        }
        else if ( clause instanceof AclWhoClauseSelf )
        {
            return SELF;
        }
        else if ( clause instanceof AclWhoClauseDn )
        {
            return DN;
        }
        else if ( clause instanceof AclWhoClauseDnAttr )
        {
            return DNATTR;
        }
        else if ( clause instanceof AclWhoClauseGroup )
        {
            return GROUP;
        }
        else if ( clause instanceof AclWhoClauseSaslSsf )
        {
            return SASL_SSF;
        }
        else if ( clause instanceof AclWhoClauseSsf )
        {
            return SSF;
        }
        else if ( clause instanceof AclWhoClauseTlsSsf )
        {
            return TLS_SSF;
        }
        else if ( clause instanceof AclWhoClauseSaslSsf )
        {
            return SASL_SSF;
        }

        return null;
    }
}
