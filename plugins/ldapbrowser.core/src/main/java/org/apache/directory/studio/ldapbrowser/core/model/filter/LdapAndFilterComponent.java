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


// ── CLASS: LdapAndFilterComponent — C-3PO PARSING THE JAWA "AND" CONJUNCTION ─
// When C-3PO encounters the "&" marker in a Jawa filter phrase, he knows: "All
// of the following sub-clauses must be true."  He records the "&" token, then
// accumulates as many sub-filter sentences as follow.  If the "&" is missing
// or there are no sub-filters, C-3PO flags the whole phrase as broken and
// explains exactly why.
// LdapAndFilterComponent is that AND-conjunction node: it wraps the "&" start
// token and a list of child {@link LdapFilter} sub-clauses that all need to
// match.  Used by the parser when it sees "(&(sub1)(sub2))".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents an LDAP AND filter branch in the filter AST — e.g.
 * {@code (&(cn=Luke)(sn=Skywalker))}.
 * The start token is the {@code &} character; child filters are accumulated via
 * {@link #addFilter(LdapFilter)}.  All children must be valid for this node to
 * be considered valid.
 *
 * <p>Think of this as C-3PO parsing the Jawa "AND" conjunction: every
 * sub-sentence that follows the "&" marker must hold true.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapAndFilterComponent extends LdapFilterComponent
{

    // ── C-3PO Opens A New "All Must Match" Clause ────────────────────────────────
    /**
     * Creates a new instance of LdapAndFilterComponent.
     *
     * @param parent the parent filter
     */
    public LdapAndFilterComponent( LdapFilter parent )
    {
        super( parent );
    }


    // ── C-3PO Records The AND Operator Token ─────────────────────────────────────
    // "The '&' token is the start of an AND clause — record it only if it's
    // really an AND token."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#setStartToken(org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken)
     */
    public boolean setStartToken( LdapFilterToken andToken )
    {
        if ( andToken != null && andToken.getType() == LdapFilterToken.AND )
        {
            return super.setStartToken( andToken );
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Explains Why The AND Clause Is Invalid ──────────────────────────────
    // "Missing '&'?  No sub-filters?  Or one of the sub-filters is broken?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getInvalidCause()
     */
    public String getInvalidCause()
    {
        if ( startToken == null )
        {
            return Messages.LdapAndFilterComponent_MissingAndCharacter;
        }
        else if ( filterList.isEmpty() )
        {
            return Messages.LdapAndFilterComponent_MissingFilters;
        }
        else
        {
            return Messages.LdapAndFilterComponent_InvalidAndFilter;
        }
    }


    // ── C-3PO Renders The AND Clause As A String ──────────────────────────────────
    // "&(sub1)(sub2)..."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String s = startToken != null ? "&" : ""; //$NON-NLS-1$ //$NON-NLS-2$
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
