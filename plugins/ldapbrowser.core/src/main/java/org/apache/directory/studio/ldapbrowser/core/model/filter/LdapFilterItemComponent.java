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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken;


// ── CLASS: LdapFilterItemComponent — C-3PO PARSING A SINGLE JAWA COMPARISON ──
// The simplest Jawa phrase C-3PO encounters is a comparison: "cn equals Luke"
// or "uid is present" or "sn approximately matches Skywalker".  Each of those
// has three parts: the attribute word (cn), the operator symbol (=, >=, <=, ~=,
// :=, *), and the value word (Luke).  C-3PO slots those three tokens in order
// and considers the item valid when all the required ones are present.
// LdapFilterItemComponent holds one such comparison: attribute token + filter-
// type token + optional value token.  It is a leaf node in the AST — it never
// contains child filters.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a simple LDAP filter item (leaf node) in the filter AST —
 * e.g. {@code cn=Luke}, {@code sn>=Skywalker}, or {@code mail=*} (presence).
 * Contains three slots: attribute token (the attribute name), filtertype token
 * (the comparison operator), and value token (the comparison value, optional
 * for presence checks).  No child filters are accepted.
 *
 * <p>Think of this as C-3PO parsing a single Jawa comparison phrase: subject
 * word, operator symbol, object word.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapFilterItemComponent extends LdapFilterComponent
{

    /** The filtertype token. */
    private LdapFilterToken filtertypeToken;

    /** The value token. */
    private LdapFilterToken valueToken;


    // ── C-3PO Opens A New Comparison Phrase Slot ─────────────────────────────────
    /**
     * Creates a new instance of LdapFilterItemComponent.
     *
     * @param parent the parent filter
     */
    public LdapFilterItemComponent( LdapFilter parent )
    {
        super( parent );
        this.filtertypeToken = null;
        this.valueToken = null;
    }


    // ── C-3PO Records The Attribute Name Token ────────────────────────────────────
    // "First word received: the attribute name (e.g. 'cn')."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#setStartToken(org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken)
     */
    public boolean setStartToken( LdapFilterToken attributeToken )
    {
        if ( attributeToken != null && attributeToken.getType() == LdapFilterToken.ATTRIBUTE )
        {
            super.setStartToken( attributeToken );
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Records The Attribute Token Via The Alias Method ───────────────────
    /**
     * Sets the attribute token.
     *
     * @param attributeToken the attribute token
     * @return {@code true} if setting the attribute token was successful, {@code false} otherwise
     */
    public boolean setAttributeToken( LdapFilterToken attributeToken )
    {
        return this.setStartToken( attributeToken );
    }


    // ── C-3PO Reads The Attribute Token ──────────────────────────────────────────
    /**
     * Gets the attribute token.
     *
     * @return the attribute token, {@code null} if not set
     */
    public LdapFilterToken getAttributeToken()
    {
        return getStartToken();
    }


    // ── C-3PO Records The Comparison Operator Token ───────────────────────────────
    // "Operator received: '=', '>=', '<=', '~=', ':=', or '*' (presence)."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the filtertype token.
     *
     * @param filtertypeToken the filtertype token
     * @return {@code true} if setting the filtertype token was successful, {@code false} otherwise
     */
    public boolean setFiltertypeToken( LdapFilterToken filtertypeToken )
    {
        if ( this.filtertypeToken == null
            && filtertypeToken != null
            && ( filtertypeToken.getType() == LdapFilterToken.EQUAL
                || filtertypeToken.getType() == LdapFilterToken.GREATER
                || filtertypeToken.getType() == LdapFilterToken.LESS
                || filtertypeToken.getType() == LdapFilterToken.APROX
                || filtertypeToken.getType() == LdapFilterToken.PRESENT || filtertypeToken.getType() == LdapFilterToken.SUBSTRING ) )
        {
            this.filtertypeToken = filtertypeToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The Comparison Operator Token ────────────────────────────────
    /**
     * Gets the filter token.
     *
     * @return the filter token, {@code null} if not set
     */
    public LdapFilterToken getFilterToken()
    {
        return filtertypeToken;
    }


    // ── C-3PO Records The Comparison Value Token ──────────────────────────────────
    // "Value word received: 'Luke', 'Skywalker', or a substring pattern."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the value token.
     *
     * @param valueToken the value token
     * @return {@code true} if setting the value token was successful, {@code false} otherwise
     */
    public boolean setValueToken( LdapFilterToken valueToken )
    {
        if ( this.valueToken == null && valueToken != null && valueToken.getType() == LdapFilterToken.VALUE )
        {
            this.valueToken = valueToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The Value Token ───────────────────────────────────────────────
    /**
     * Gets the value token.
     *
     * @return the value token, {@code null} if not set
     */
    public LdapFilterToken getValueToken()
    {
        return valueToken;
    }


    // ── C-3PO Validates The Comparison Phrase ─────────────────────────────────────
    // "Attribute + operator + value?  Valid.  Presence (attribute + '*')?
    // Also valid — value is optional for presence checks."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#isValid()
     */
    public boolean isValid()
    {
        return startToken != null && filtertypeToken != null
            && ( valueToken != null || filtertypeToken.getType() == LdapFilterToken.PRESENT );
    }


    // ── C-3PO Collects All Three Tokens Of The Comparison ────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getTokens()
     */
    public LdapFilterToken[] getTokens()
    {
        // collect tokens
        List<LdapFilterToken> tokenList = new ArrayList<LdapFilterToken>();
        if ( startToken != null )
        {
            tokenList.add( startToken );
        }
        if ( filtertypeToken != null )
        {
            tokenList.add( filtertypeToken );
        }
        if ( valueToken != null )
        {
            tokenList.add( valueToken );
        }

        // sort tokens
        LdapFilterToken[] tokens = tokenList.toArray( new LdapFilterToken[tokenList.size()] );
        Arrays.sort( tokens );

        // return
        return tokens;
    }


    // ── C-3PO Renders The Comparison As A String ──────────────────────────────────
    // "cn=Luke"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return ( startToken != null ? startToken.getValue() : "" ) //$NON-NLS-1$
            + ( filtertypeToken != null ? filtertypeToken.getValue() : "" ) //$NON-NLS-1$
            + ( valueToken != null ? valueToken.getValue() : "" ); //$NON-NLS-1$
    }


    // ── C-3PO Refuses Child Sub-Sentences For A Leaf Clause ──────────────────────
    // "A comparison phrase has no nested sub-sentences — always returns false."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * This implementation does nothing and returns always false.
     *
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#addFilter(org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilter)
     */
    public boolean addFilter( LdapFilter filter )
    {
        return false;
    }


    // ── C-3PO Returns The Broken Phrase If It Is Invalid ────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getInvalidFilters()
     */
    public LdapFilter[] getInvalidFilters()
    {
        if ( isValid() )
        {
            return new LdapFilter[0];
        }
        else
        {
            return new LdapFilter[]
                { parent };
        }
    }


    // ── C-3PO Finds Which Token Is At A Given Cursor Position ────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getFilter(int)
     */
    public LdapFilter getFilter( int offset )
    {
        if ( startToken != null && startToken.getOffset() <= offset
            && offset < startToken.getOffset() + startToken.getLength() )
        {
            return parent;
        }
        else if ( filtertypeToken != null && filtertypeToken.getOffset() <= offset
            && offset < filtertypeToken.getOffset() + filtertypeToken.getLength() )
        {
            return parent;
        }
        else if ( valueToken != null && valueToken.getOffset() <= offset
            && offset < valueToken.getOffset() + valueToken.getLength() )
        {
            return parent;
        }
        else
        {
            return null;
        }
    }


    // ── C-3PO Explains Why The Comparison Phrase Is Invalid ──────────────────────
    // "Missing attribute name?  Missing operator?  Missing value?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getInvalidCause()
     */
    public String getInvalidCause()
    {
        if ( startToken == null )
        {
            return Messages.LdapFilterItemComponent_MissingAttributeName;
        }
        else if ( filtertypeToken == null )
        {
            return Messages.LdapFilterItemComponent_MissingFilterType;
        }
        else if ( valueToken == null )
        {
            return Messages.LdapFilterItemComponent_MissingValue;
        }
        else
        {
            return null;
        }
    }

}
