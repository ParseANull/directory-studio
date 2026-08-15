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

// ── CLASS: AuthIdRewriteWrapper — Obi-Wan's Redirect Slip ────────────────────
// "These aren't the droids you're looking for" — Obi-Wan hands over a redirect
// slip that makes the checkpoint operator believe a different identity passed
// through.  AuthIdRewriteWrapper holds one such slip: a single olcAuthIDRewrite
// string that tells OpenLDAP how to transform an incoming authentication
// identity before checking credentials.  The raw rule string is stored as-is
// and returned verbatim by toString.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for a single value stored in the olcAuthIDRewrite attribute.
 * It holds the raw rewrite rule string and returns it verbatim.  Ordering
 * is not yet implemented ({@link #compareTo} always returns 0).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AuthIdRewriteWrapper implements Cloneable, Comparable<AuthIdRewriteWrapper>
{
    /** The rewrite */
    private String rewrite;


    // ── Constructor — Taking the Redirect Slip ─────────────────────────────────
    // Obi-Wan hands over the redirect slip with the rewrite rule already written
    // on it.  We store it verbatim.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of AuthIdRewriteWrapper using a String value
     *
     * @param rewrite The value
     */
    public AuthIdRewriteWrapper( String rewrite )
    {
        this.rewrite = rewrite;
    }


    // ── compareTo — Ordering Is Not Yet Enforced ──────────────────────────────
    // The checkpoint operator hasn't been given ranking instructions yet — we
    // return 0 for all comparisons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( AuthIdRewriteWrapper that )
    {
        if ( that == null )
        {
            return 1;
        }

        //
        return 0;
    }


    // ── toString — Show the Redirect Rule ─────────────────────────────────────
    // Return the raw rewrite string — the redirect slip exactly as written.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return rewrite;
    }
}
