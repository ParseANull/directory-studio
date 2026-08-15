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

package org.apache.directory.studio.ldapbrowser.core.model.filter;


import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken;


// ── CLASS: LdapNotFilterComponent — C-3PO PARSING THE JAWA "NOT" NEGATION ────
// When C-3PO sees the "!" marker, he knows: "The following single sub-clause
// must NOT be true."  He records the "!" token and then accepts exactly one
// sub-filter sentence — no more.  If someone tries to add a second sub-filter,
// C-3PO refuses: a NOT clause in Jawa grammar takes only one argument.
// LdapNotFilterComponent is that NOT-negation node: it wraps the "!" start
// token and exactly one child {@link LdapFilter}.  Used by the parser when
// it sees "(!(sub))".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents an LDAP NOT filter branch in the filter AST — e.g.
 * {@code (!(objectClass=groupOfNames))}.
 * The start token is the {@code !} character; exactly one child filter is
 * accepted via {@link #addFilter(LdapFilter)} — a second call is rejected.
 *
 * <p>Think of this as C-3PO parsing the Jawa "NOT" negation: the "!" marker
 * introduces exactly one sub-sentence that must be false for the clause to hold.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapNotFilterComponent extends LdapFilterComponent
{

    // ── C-3PO Opens A New "Must Not Match" Clause ────────────────────────────────
    /**
     * Creates a new instance of LdapNotFilterComponent.
     *
     * @param parent the parent filter
     */
    public LdapNotFilterComponent( LdapFilter parent )
    {
        super( parent );
    }


    // ── C-3PO Records The NOT Operator Token ──────────────────────────────────────
    // "The '!' token starts a NOT clause — accept it only if it's the right type."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#setStartToken(org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken)
     */
    public boolean setStartToken( LdapFilterToken notToken )
    {
        if ( notToken != null && notToken.getType() == LdapFilterToken.NOT )
        {
            return super.setStartToken( notToken );
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Accepts Only One Sub-Sentence For The NOT Clause ───────────────────
    // "A NOT clause in Jawa grammar takes exactly one argument — refuse any more."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#addFilter(org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilter)
     */
    public boolean addFilter( LdapFilter filter )
    {
        if ( filterList.isEmpty() )
        {
            return super.addFilter( filter );
        }
        else
        {
            // There is already a filter in the list. A NOT filter
            // can only contain one filter.
            return false;
        }
    }


    // ── C-3PO Explains Why The NOT Clause Is Invalid ──────────────────────────────
    // "Missing '!'?  No sub-filter yet?  Or the one sub-filter itself is broken?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getInvalidCause()
     */
    public String getInvalidCause()
    {
        if ( startToken == null )
        {
            return Messages.LdapNotFilterComponent_MissingNotCharacter;
        }
        else if ( filterList.isEmpty() )
        {
            return Messages.LdapNotFilterComponent_MissingFilterExpression;
        }
        else
        {
            return Messages.LdapNotFilterComponent_InvalidNotFilter;
        }
    }


    // ── C-3PO Renders The NOT Clause As A String ──────────────────────────────────
    // "!(sub)"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return ( startToken != null ? "!" : "" ) + ( !filterList.isEmpty() ? filterList.get( 0 ).toString() : "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
