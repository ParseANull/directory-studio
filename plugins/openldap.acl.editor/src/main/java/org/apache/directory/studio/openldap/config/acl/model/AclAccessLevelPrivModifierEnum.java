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


// ── CLASS: AclAccessLevelPrivModifierEnum — DEATH STAR DIRECTIVE OPERATORS ──
// Before Tarkin lists the privilege codes he stamps one of three operators:
// "=" means exactly these privileges and nothing else; "+" means grant
// these privileges on top of whatever they already have; "-" means revoke
// these specific privileges. These three symbols are the privilege set's
// leading character in OpenLDAP ACL syntax.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The three operators that precede a custom privilege set in an OpenLDAP ACL
 * "by" clause. {@code EQUAL} ({@code =}) sets the exact privilege set;
 * {@code PLUS} ({@code +}) adds privileges; {@code MINUS} ({@code -}) removes
 * them. Together with {@link AclAccessLevelPrivilegeEnum} they let us build
 * constructs like {@code +rsc} or {@code =w}.
 * Think of this enum as Tarkin's three directive operators — replace, add, or
 * revoke — before he lists which specific clearances are being adjusted.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclAccessLevelPrivModifierEnum
{
    EQUAL,
    PLUS,
    MINUS;


    // ── Rendering the Operator as Its Symbol ──────────────────────────────────
    // In the ACL text the operator is just a symbol — "=", "+", "-" — so
    // we return the symbol string rather than the Java enum name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the single-character operator symbol OpenLDAP expects immediately
     * before the privilege letter string in a custom access level.
     *
     * <p>For example — Tarkin choosing which directive operator to stamp:</p>
     * <pre>
     *   AclAccessLevelPrivModifierEnum.EQUAL.toString() // → "="
     *   AclAccessLevelPrivModifierEnum.PLUS.toString()  // → "+"
     *   AclAccessLevelPrivModifierEnum.MINUS.toString() // → "-"
     * </pre>
     *
     * @return  The operator symbol as a one-character string.
     */
    public String toString()
    {
        switch ( this )
        {
            case EQUAL:
                return "=";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
        }

        return super.toString();
    }
}
