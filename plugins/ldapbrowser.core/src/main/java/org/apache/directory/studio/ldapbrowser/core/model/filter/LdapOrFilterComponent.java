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


// ── CLASS: LdapOrFilterComponent — C-3PO PARSING THE JAWA "OR" DISJUNCTION ──
// When C-3PO spots the "|" marker in a Jawa filter phrase, he knows: "At least
// one of the following sub-clauses must be true."  He records the "|" token,
// collects all the following sub-filter sentences, and checks that at least
// one of them is valid for the whole clause to pass.
// LdapOrFilterComponent is that OR-disjunction node: it wraps the "|" start
// token and a list of child {@link LdapFilter} sub-clauses where at least one
// needs to match.  Used by the parser when it sees "(|(sub1)(sub2))".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents an LDAP OR filter branch in the filter AST — e.g.
 * {@code (|(cn=Luke)(cn=Anakin))}.
 * The start token is the {@code |} character; child filters are accumulated via
 * {@link #addFilter(LdapFilter)}.  At least one child must be valid for this
 * node to be considered valid.
 *
 * <p>Think of this as C-3PO parsing the Jawa "OR" disjunction: any one of
 * the sub-sentences following the "|" marker can satisfy the clause.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapOrFilterComponent extends LdapFilterComponent
{

    // ── C-3PO Opens A New "Any Must Match" Clause ────────────────────────────────
    /**
     * Creates a new instance of LdapOrFilterComponent.
     *
     * @param parent the parent filter
     */
    public LdapOrFilterComponent( LdapFilter parent )
    {
        super( parent );
    }


    // ── C-3PO Records The OR Operator Token ───────────────────────────────────────
    // "The '|' token starts an OR clause — accept it only if it's the right type."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#setStartToken(org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken)
     */
    public boolean setStartToken( LdapFilterToken orToken )
    {
        if ( orToken != null && orToken.getType() == LdapFilterToken.OR )
        {
            return super.setStartToken( orToken );
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Explains Why The OR Clause Is Invalid ──────────────────────────────
    // "Missing '|'?  No sub-filters?  Or one of the sub-filters is broken?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getInvalidCause()
     */
    public String getInvalidCause()
    {
        if ( startToken == null )
        {
            return Messages.LdapOrFilterComponent_MissingOrCharacter;
        }
        else if ( filterList.isEmpty() )
        {
            return Messages.LdapOrFilterComponent_MissingFilters;
        }
        else
        {
            return Messages.LdapOrFilterComponent_InvalidOrFilter;
        }
    }


    // ── C-3PO Renders The OR Clause As A String ───────────────────────────────────
    // "|(sub1)(sub2)..."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String s = startToken != null ? "|" : ""; //$NON-NLS-1$ //$NON-NLS-2$
        for ( LdapFilter filter : filterList )
        {
            if ( filter != null )
            {
                s += filter.toString();
            }
        }
        return s;
    }

}
