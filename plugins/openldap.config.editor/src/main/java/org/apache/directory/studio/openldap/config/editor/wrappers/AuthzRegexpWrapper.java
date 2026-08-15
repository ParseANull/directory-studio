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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: AuthzRegexpWrapper — C-3PO's Match-and-Replace Dialect Card ────────
// Every C-3PO translation has two parts: the pattern he recognizes and the
// canonical form he uses in response.  AuthzRegexpWrapper stores an
// olcAuthzRegexp entry the same way — a match regex and a replace string.
// The constructor captures the match part; toString returns both, separated by
// a space, ready for the LDAP attribute value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for a single value stored in the olcAuthzRegexp attribute.
 * It holds two fields: the match regex (required) and the replace string
 * (optional until the dialog fills it in).  Ordering is not yet implemented
 * ({@link #compareTo} always returns 0).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AuthzRegexpWrapper implements Cloneable, Comparable<AuthzRegexpWrapper>
{
    /** The match part */
    private String match;

    /** The replace part */
    private String replace;


    // ── Constructor — Handing C-3PO the Pattern Card ──────────────────────────
    // We hand C-3PO just the match pattern for now; the replace string gets
    // filled in later through the edit dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of AuthzRegexpWrapper using a String value
     *
     * @param regexp The value
     */
    public AuthzRegexpWrapper( String authzRegexp )
    {
        this.match = authzRegexp;
    }


    // ── compareTo — Ordering Is Not Yet Enforced ──────────────────────────────
    // C-3PO hasn't been given ranking instructions — we return 0 for all
    // comparisons until a real ordering strategy is defined.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( AuthzRegexpWrapper that )
    {
        if ( that == null )
        {
            return 1;
        }

        //
        return 0;
    }


    // ── toString — Show the Full Match-and-Replace Pair ───────────────────────
    // C-3PO recites both parts of the translation: pattern and replacement,
    // joined by a space as required by the olcAuthzRegexp attribute syntax.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return match + ' ' + replace;
    }
}
