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


// ── CLASS: LdapFilterComponent — C-3PO'S GRAMMAR TEMPLATE ────────────────────
// Every Jawa sentence has a core grammatical structure that C-3PO must follow:
// a start marker (the & / | / ! / attribute), zero or more sub-sentences, and
// a rule for validating the whole thing.  C-3PO uses this same template for
// every sentence type — the specifics vary (AND vs. OR vs. NOT vs. a simple
// item) but the skeleton is always the same.
// LdapFilterComponent is that skeleton: an abstract base for every filter node
// in the AST.  Concrete subclasses (AND, OR, NOT, item, extensible) override
// setStartToken and getInvalidCause while the shared logic for addFilter,
// isValid, getFilters, getTokens, and getFilter lives here.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The base class for all filter components in the LDAP filter AST.
 * Holds the parent {@link LdapFilter}, the type-specific start token, and a
 * list of sub-filter {@link LdapFilter} children.  Provides default
 * implementations for token collection, validity checking, and cursor-position
 * lookup; concrete subclasses override {@link #setStartToken(LdapFilterToken)}
 * and {@link #getInvalidCause()}.
 *
 * <p>Think of this as C-3PO's grammar template — every filter type uses the
 * same skeleton but fills in its own specific start-word and validation rules.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class LdapFilterComponent
{

    /** The parent filter. */
    protected final LdapFilter parent;

    /** The start token. */
    protected LdapFilterToken startToken;

    /** The filter list. */
    protected final List<LdapFilter> filterList;


    // ── C-3PO Attaches This Grammar Template To Its Parent Sentence ───────────────
    // "Which sentence does this clause belong to?  I need that reference."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * The Constructor.
     *
     * @param parent the parent filter, not null
     * @throws IllegalArgumentException if {@code parent} is {@code null}.
     */
    protected LdapFilterComponent( LdapFilter parent )
    {
        if ( parent == null )
        {
            throw new IllegalArgumentException( Messages.LdapFilterComponent_ParentIsNull );
        }

        this.parent = parent;
        this.startToken = null;
        this.filterList = new ArrayList<LdapFilter>( 2 );
    }


    // ── C-3PO Reads The Parent Sentence Reference ────────────────────────────────
    /**
     * Returns the parent filter of this filter component.
     *
     * @return the parent filter, never null.
     */
    public final LdapFilter getParent()
    {
        return parent;
    }


    // ── C-3PO Marks The Start-Of-Clause Token ────────────────────────────────────
    // "First token received — the clause is now open."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the start token of the filter component. Checks if start token
     * is not set yet and if the given start token is not null.
     *
     * @param startToken the start token to set
     * @return {@code true} if setting the start token was successful, {@code false}
     *         otherwise.
     */
    public boolean setStartToken( LdapFilterToken startToken )
    {
        if ( this.startToken == null && startToken != null )
        {
            this.startToken = startToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The Start-Of-Clause Token ────────────────────────────────────
    /**
     * Returns the start token of this filter component.
     *
     * @return the start token or {@code null} if not set.
     */
    public final LdapFilterToken getStartToken()
    {
        return startToken;
    }


    // ── C-3PO Adds A Sub-Sentence To This Clause ─────────────────────────────────
    // "Sub-sentence appended to the list.  Start token must be set first."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a filter to the list of subfilters. Checks if the start token
     * was set before and if the filter is not null.
     *
     * @param filter the subfilter to add
     * @return {@code true} if adding the filter was successful, {@code false} otherwise.
     */
    public boolean addFilter( LdapFilter filter )
    {
        if ( startToken != null && filter != null )
        {
            filterList.add( filter );
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Lists All Sub-Sentences ────────────────────────────────────────────
    /**
     * Returns the subfilters of this filter component.
     *
     * @return an array of subfilters or an empty array.
     */
    public LdapFilter[] getFilters()
    {
        LdapFilter[] filters = new LdapFilter[filterList.size()];
        filterList.toArray( filters );
        return filters;
    }


    // ── C-3PO Validates The Whole Clause ─────────────────────────────────────────
    // "Start token set?  At least one sub-sentence?  Each sub-sentence valid?
    // If all three, the clause is grammatically correct."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks if this filter component including all subfilters is valid.
     *
     * @return {@code true} if this filter component is valid.
     */
    public boolean isValid()
    {
        if ( startToken == null )
        {
            return false;
        }

        if ( filterList.isEmpty() )
        {
            return false;
        }

        for ( LdapFilter filter : filterList )
        {
            if ( filter == null || !filter.isValid() )
            {
                return false;
            }
        }

        return true;
    }


    // ── C-3PO Explains The Grammatical Error (Subclass Must Implement) ───────────
    /**
     * Returns the invalid cause.
     *
     * @return the invalid cause, or {@code null} if this filter is valid.
     */
    public abstract String getInvalidCause();


    // ── C-3PO Collects All Broken Sub-Sentences ───────────────────────────────────
    // "Walk every sub-sentence and gather all the ones that failed."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the invalid filters. This may be the whole parent filter or
     * any of the subfilters.
     *
     * @return an array of invalid filters or an empty array if all filters
     *         are valid.
     */
    public LdapFilter[] getInvalidFilters()
    {
        if ( startToken == null || filterList.isEmpty() )
        {
            return new LdapFilter[]
                { parent };
        }
        else
        {
            List<LdapFilter> invalidFilterList = new ArrayList<LdapFilter>();
            for ( LdapFilter filter : filterList )
            {
                if ( filter != null )
                {
                    invalidFilterList.addAll( Arrays.asList( filter.getInvalidFilters() ) );
                }
            }
            return invalidFilterList.toArray( new LdapFilter[invalidFilterList.size()] );
        }
    }


    // ── C-3PO Collects All Tokens In Order ────────────────────────────────────────
    // "Gather the start token and every token from every sub-sentence, sorted."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all tokens of the filter component including all subfilters.
     *
     * @return an array of tokens or an empty array.
     */
    public LdapFilterToken[] getTokens()
    {
        // collect tokens
        List<LdapFilterToken> tokenList = new ArrayList<LdapFilterToken>();
        if ( startToken != null )
        {
            tokenList.add( startToken );
        }
        for ( LdapFilter filter : filterList )
        {
            if ( filter != null )
            {
                tokenList.addAll( Arrays.asList( filter.getTokens() ) );
            }
        }

        // sort tokens
        LdapFilterToken[] tokens = tokenList.toArray( new LdapFilterToken[tokenList.size()] );
        Arrays.sort( tokens );

        // return
        return tokens;
    }


    // ── C-3PO Finds Which Clause Is At A Cursor Position ─────────────────────────
    // "At character offset 7, which sub-sentence is the user's cursor inside?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the filter at the given offset. This may be the whole parent
     * filter or one of the subfilters.
     *
     * @param offset the offset
     * @return the filter at the given offset or {@code null} if offset is out of
     *         range.
     */
    public LdapFilter getFilter( int offset )
    {
        if ( startToken != null && startToken.getOffset() == offset )
        {
            return parent;
        }
        else if ( !filterList.isEmpty() )
        {
            for ( LdapFilter filter : filterList )
            {
                if ( filter != null && filter.getFilter( offset ) != null )
                {
                    return filter.getFilter( offset );
                }
            }
            return null;
        }
        else
        {
            return null;
        }
    }

}
