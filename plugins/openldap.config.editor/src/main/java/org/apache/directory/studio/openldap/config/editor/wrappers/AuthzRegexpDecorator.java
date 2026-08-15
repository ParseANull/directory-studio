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

// ── CLASS: AuthzRegexpDecorator — C-3PO Pattern-Matching Dialects ─────────────
// C-3PO is fluent in over six million forms of communication — he recognizes a
// pattern and replaces it with its correct equivalent instantly.
// AuthzRegexpDecorator acts as C-3PO for authorization regexp rules: it renders
// each olcAuthzRegexp entry as a readable string in the table.  There is no
// dialog wired in (the constructor does nothing), and the text is always the
// wrapper's plain toString.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the AuthzRegexp table in the Options page.
 * It renders each {@link AuthzRegexpWrapper} as its string representation
 * (match and replace parts separated by a space).  No edit dialog is
 * registered — the constructor intentionally does nothing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AuthzRegexpDecorator extends TableDecorator<AuthzRegexpWrapper>
{
    // ── Constructor — C-3PO Stands Ready, No Setup Required ───────────────────
    // C-3PO loads his dialect banks at startup — we don't need to do anything
    // special here, so the constructor body is empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of AuthzRegexpDecorator
     * @param parentShell The parent Shell
     */
    public AuthzRegexpDecorator( Shell parentShell )
    {
        // Nothing to do
    }


    // ── getText — Translate the Regexp Pair to a Readable Label ───────────────
    // C-3PO reads the match and replace pair aloud in a format the user can
    // understand — we delegate to the wrapper's toString.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for an AuthIdRewriteWrapper.
     *
     * @return the wrapper's string representation, or the default label if the
     *         element is not an {@link AuthzRegexpWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof AuthzRegexpWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — C-3PO Works in Text Only ───────────────────────────────────
    // No icon is needed — C-3PO is all voice, no pictures.
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


    // ── compare — Order Regexp Rules by Their Natural Ordering ─────────────────
    // C-3PO sorts his dialect database; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( AuthzRegexpWrapper e1, AuthzRegexpWrapper e2 )
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
