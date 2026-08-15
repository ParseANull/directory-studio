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

import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken;


// ── CLASS: LdapFilter — C-3PO PARSING ONE COMPLETE JAWA SENTENCE ─────────────
// C-3PO hears "({filter-body})" and knows that's a complete thought: an opening
// parenthesis, a filter expression in the middle, and a closing parenthesis.
// If any of those three pieces is missing, the sentence is broken and C-3PO
// flags it as invalid.  He can also point to sub-sentences nested inside: when
// the body is another AND/OR/NOT cluster, each child becomes its own sentence
// for C-3PO to recurse into.
// LdapFilter is the top-level AST node for one LDAP filter clause: a '(' start
// token, a filter component (AND/OR/NOT/item), and a ')' stop token.  The
// parser builds a tree of these to represent the full filter string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents one LDAP filter clause in the AST — the unit bounded by a pair
 * of parentheses.  Contains a start {@code (} token, a {@link LdapFilterComponent}
 * that provides the logic (AND, OR, NOT, item, or extensible), and a stop {@code )}
 * token.  Any unexpected tokens are recorded in the "other tokens" list and
 * cause {@link #isValid()} to return {@code false}.
 *
 * <p>Think of this as C-3PO parsing one complete Jawa sentence: open paren = start
 * word, filter body = meaning, close paren = sentence-end marker.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapFilter
{

    private LdapFilterToken startToken;

    private LdapFilterComponent filterComponent;

    private LdapFilterToken stopToken;

    private List<LdapFilterToken> otherTokens;


    // ── C-3PO Opens A Fresh Sentence Slot ─────────────────────────────────────────
    // "New sentence template: waiting for opening paren, body, and closing paren."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of LdapFilter.
     */
    public LdapFilter()
    {
        this.startToken = null;
        this.filterComponent = null;
        this.stopToken = null;
        this.otherTokens = new ArrayList<LdapFilterToken>( 2 );
    }


    // ── C-3PO Marks The Opening Parenthesis ──────────────────────────────────────
    // "Opening paren received and recorded.  Ready for the body."
    // Only accepts an LPAR token, and only if no start token is set yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the start token.
     *
     * @param startToken the start token (must be type {@link LdapFilterToken#LPAR})
     * @return {@code true} if setting the start token was successful, {@code false} otherwise
     */
    public boolean setStartToken( LdapFilterToken startToken )
    {
        if ( this.startToken == null && startToken != null && startToken.getType() == LdapFilterToken.LPAR )
        {
            this.startToken = startToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Slots In The Filter Body ───────────────────────────────────────────
    // "Body received.  Start token must be set first, and body must be non-null."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the filter component.
     *
     * @param filterComponent the filter component
     * @return {@code true} if setting the filter component was successful, {@code false} otherwise
     */
    public boolean setFilterComponent( LdapFilterComponent filterComponent )
    {
        if ( this.startToken != null && this.filterComponent == null && filterComponent != null )
        {
            this.filterComponent = filterComponent;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Marks The Closing Parenthesis ──────────────────────────────────────
    // "Closing paren received.  Start token must already be set."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the stop token.
     *
     * @param stopToken the stop token (must be type {@link LdapFilterToken#RPAR})
     * @return {@code true} if setting the stop token was successful, {@code false} otherwise
     */
    public boolean setStopToken( LdapFilterToken stopToken )
    {
        if ( this.startToken != null && this.stopToken == null && stopToken != null
            && stopToken.getType() == LdapFilterToken.RPAR )
        {
            this.stopToken = stopToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Records A Stray Token He Cannot Classify ───────────────────────────
    // "Unknown token in the sentence — I'll track it so validity fails."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds another token.
     *
     * @param otherToken the other token
     */
    public void addOtherToken( LdapFilterToken otherToken )
    {
        otherTokens.add( otherToken );
    }


    // ── C-3PO Reads The Opening Paren Token ──────────────────────────────────────
    /**
     * Gets the start token.
     *
     * @return the start token, or {@code null} if not set
     */
    public LdapFilterToken getStartToken()
    {
        return startToken;
    }


    // ── C-3PO Reads The Filter Body ───────────────────────────────────────────────
    /**
     * Gets the filter component.
     *
     * @return the filter component, or {@code null} if not set
     */
    public LdapFilterComponent getFilterComponent()
    {
        return filterComponent;
    }


    // ── C-3PO Reads The Closing Paren Token ──────────────────────────────────────
    /**
     * Gets the stop token.
     *
     * @return the stop token or {@code null} if not set
     */
    public LdapFilterToken getStopToken()
    {
        return stopToken;
    }


    // ── C-3PO Collects All Tokens In Order ────────────────────────────────────────
    // "Give me every token in this sentence tree, sorted by position."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets all the tokens.
     *
     * @return the tokens
     */
    public LdapFilterToken[] getTokens()
    {
        // collect tokens
        List<LdapFilterToken> tokenList = new ArrayList<LdapFilterToken>();
        if ( startToken != null )
        {
            tokenList.add( startToken );
        }
        if ( stopToken != null )
        {
            tokenList.add( stopToken );
        }
        if ( filterComponent != null )
        {
            tokenList.addAll( Arrays.asList( filterComponent.getTokens() ) );
        }
        tokenList.addAll( otherTokens );

        // sort tokens
        LdapFilterToken[] tokens = tokenList.toArray( new LdapFilterToken[tokenList.size()] );
        Arrays.sort( tokens );

        // return
        return tokens;
    }


    // ── C-3PO Verifies The Sentence Is Grammatically Complete ────────────────────
    // "Start paren? Check.  Body? Check and valid?  Check.  Close paren? Check.
    // No stray tokens? Check.  Sentence is grammatically correct."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks if this filter and all its subfilters are valid.
     *
     * @return {@code true} if this filter and all its subfilters are valid
     */
    public boolean isValid()
    {
        return startToken != null && filterComponent != null && filterComponent.isValid() && stopToken != null
            && otherTokens.isEmpty();
    }


    // ── C-3PO Lists All The Invalid Sub-Sentences ────────────────────────────────
    // "Here are all the broken parts I found — either this whole sentence or
    // the bad sub-sentences inside the body."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the invalid filters. This may be this filter itself or any of the subfilters.
     *
     * @return an array of invalid filters or an empty array if all subfilters
     *         are valid.
     */
    public LdapFilter[] getInvalidFilters()
    {
        if ( startToken == null || filterComponent == null || stopToken == null )
        {
            return new LdapFilter[]
                { this };
        }
        else
        {
            return filterComponent.getInvalidFilters();
        }
    }


    // ── C-3PO Finds Which Sentence Is At A Given Character Position ──────────────
    // "At cursor position 12, which filter clause is the user editing?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the filter at the given offset. This may be this filter
     * or one of the subfilters.
     *
     * @param offset the offset
     * @return the filter at the given offset or {@code null} if offset is out of
     *         range.
     */
    public LdapFilter getFilter( int offset )
    {
        if ( startToken != null && startToken.getOffset() == offset )
        {
            return this;
        }
        else if ( stopToken != null && stopToken.getOffset() == offset )
        {
            return this;
        }

        if ( otherTokens != null && otherTokens.size() > 0 )
        {
            for ( int i = 0; i < otherTokens.size(); i++ )
            {
                LdapFilterToken otherToken = otherTokens.get( i );
                if ( otherToken != null && otherToken.getOffset() <= offset
                    && offset < otherToken.getOffset() + otherToken.getLength() )
                {
                    return this;
                }
            }
        }

        if ( filterComponent != null )
        {
            return filterComponent.getFilter( offset );
        }

        return this;
    }


    // ── C-3PO Explains Why The Sentence Failed Validation ────────────────────────
    // "The closing parenthesis is missing — that's why this sentence is broken."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the invalid cause.
     *
     * @return the invalid cause, or {@code null} if this filter is valid.
     */
    public String getInvalidCause()
    {
        if ( stopToken == null )
        {
            return BrowserCoreMessages.model_filter_missing_closing_parenthesis;
        }
        else if ( filterComponent == null )
        {
            return BrowserCoreMessages.model_filter_missing_filter_expression;
        }
        else
        {
            return filterComponent.getInvalidCause();
        }
    }


    // ── C-3PO Prints The Clean Canonical Form Of The Sentence ────────────────────
    // "Invalid tokens and whitespace stripped — only valid filter text remains."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the string representation of this LDAP filter. Invalid tokens and
     * white spaces are removed, but incomplete filter parts are kept.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        LdapFilterToken[] tokens = getTokens();
        for ( LdapFilterToken token : tokens )
        {
            if ( token.getType() != LdapFilterToken.UNKNOWN && token.getType() != LdapFilterToken.WHITESPACE
                && token.getType() != LdapFilterToken.ERROR && token.getType() != LdapFilterToken.EOF )
            {
                sb.append( token.getValue() );
            }
        }
        return sb.toString();
    }


    // ── C-3PO Echoes Back Exactly What The User Typed ────────────────────────────
    // "Original user input preserved — spaces, typos, and all."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the string representation of this LDAP filter, as provided by the user.
     * It may contain white spaces and invalid tokens.
     */
    public String toUserProvidedString()
    {
        // add _all_ tokens to the string, including invalid tokens and whitespace tokens
        StringBuffer sb = new StringBuffer();
        LdapFilterToken[] tokens = getTokens();
        for ( LdapFilterToken token : tokens )
        {
            sb.append( token.getValue() );
        }
        return sb.toString();
    }

}
