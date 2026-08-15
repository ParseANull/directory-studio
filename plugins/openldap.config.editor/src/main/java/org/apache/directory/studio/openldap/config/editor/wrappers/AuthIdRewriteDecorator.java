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

// ── CLASS: AuthIdRewriteDecorator — Obi-Wan's Identity Translation ────────────
// When Obi-Wan uses a Jedi mind trick, he rewrites what observers perceive — a
// stormtrooper's identity is quietly redirected before it reaches the checkpoint.
// AuthIdRewriteDecorator does the same for LDAP identity rewriting rules: it
// bridges the AuthIdRewrite table to its display representation so the UI can
// render each olcAuthIDRewrite rule as a plain string.  There is no dialog to
// open, so the constructor does nothing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the AuthIdRewrite table in the Options page.
 * It renders each {@link AuthIdRewriteWrapper} as its string representation
 * (the raw rewrite rule).  There is no associated edit dialog — the constructor
 * intentionally does nothing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AuthIdRewriteDecorator extends TableDecorator<AuthIdRewriteWrapper>
{
    // ── Constructor — The Mind Trick Needs No Ceremony ─────────────────────────
    // Obi-Wan waves his hand and the redirect happens silently.  No dialog is
    // registered here; the constructor is intentionally empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of AuthIdRewriteDecorator
     * @param parentShell The parent Shell
     */
    public AuthIdRewriteDecorator( Shell parentShell )
    {
        // Nothing to do
    }


    // ── getText — Show the Rewrite Rule as Plain Text ──────────────────────────
    // Obi-Wan's redirect is transparent — we just show the rule string exactly
    // as it appears in the olcAuthIDRewrite attribute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for an AuthIdRewriteWrapper.
     *
     * @return the wrapper's string representation, or the default label if the
     *         element is not an {@link AuthIdRewriteWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof AuthIdRewriteWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon for the Redirect ───────────────────────────────────
    // The mind trick works without any visual signal — we return no image.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Get the image. We have none
     *
     * @return always {@code null}
     */
    @Override
    public Image getImage( Object element )
    {
        return null;
    }


    // ── compare — Order Rewrites by Their Natural Comparison ──────────────────
    // Redirects are ordered by their compareTo result; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( AuthIdRewriteWrapper e1, AuthIdRewriteWrapper e2 )
    {
        if ( e1 != null )
        {
            if ( e2 == null )
            {
                return 1;
            }
            else
            {
                return e1.compareTo( e2 );
            }
        }
        else
        {
            if ( e2 == null )
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }
}
