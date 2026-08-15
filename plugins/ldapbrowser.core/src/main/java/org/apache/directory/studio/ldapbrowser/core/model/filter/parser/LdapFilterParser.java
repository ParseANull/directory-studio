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

package org.apache.directory.studio.ldapbrowser.core.model.filter.parser;


import java.util.Stack;

import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapAndFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilter;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterExtensibleComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterItemComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapNotFilterComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapOrFilterComponent;


// ── CLASS: LdapFilterParser — C-3PO ASSEMBLING THE FULL JAWA PARSE TREE ──────
// C-3PO has the scanner reading word cards one at a time.  Now he has to
// assemble those word cards into a proper parse tree: nested parentheses become
// nested filter objects, & / | / ! operators become AND / OR / NOT nodes, and
// simple attribute=value pairs become item nodes.  He uses a stack to track
// which filter is currently "open" — pushing a new filter when he sees '(' and
// popping it when he sees ')'.  If a token doesn't fit where it arrived, he
// quietly marks it as an ERROR token so the UI can highlight it in red.
// LdapFilterParser drives LdapFilterScanner in a token loop, dispatching each
// token type to the correct AST-building action.  Call {@link #parse(String)}
// then {@link #getModel()} to get the root {@link LdapFilter} node.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Parses a raw LDAP filter string (RFC 4515 / RFC 2254) into a tree of
 * {@link LdapFilter} and {@link LdapFilterComponent} nodes by driving a
 * {@link LdapFilterScanner} token loop.
 *
 * <p>We use a {@link Stack} of open filters to track nesting depth: each
 * {@code (} pushes a new {@link LdapFilter} and each {@code )} pops it.
 * Unexpected tokens become ERROR tokens attached to the nearest open filter
 * so the calling UI can highlight them without crashing.</p>
 *
 * <p>Think of this as C-3PO assembling a complete Jawa sentence tree from the
 * word cards produced by {@link LdapFilterScanner} — stacking open parentheses
 * like nested sub-clauses, then collapsing them as each one closes.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapFilterParser
{

    /** The scanner. */
    private LdapFilterScanner scanner;

    /** The filter stack. */
    private Stack<LdapFilter> filterStack;

    /** The parsed LDAP filter model. */
    private LdapFilter model;


    // ── C-3PO Initialises His Parse Stack And Loads The Scanner ──────────────────
    // "Parser online.  Scanner ready.  Empty model filter created.
    // Awaiting first call to parse()."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of LdapFilterParser.
     * Initialises the internal {@link LdapFilterScanner} and an empty root
     * {@link LdapFilter} model ready for the first {@link #parse(String)} call.
     */
    public LdapFilterParser()
    {
        this.scanner = new LdapFilterScanner();
        this.model = new LdapFilter();
    }


    // ── C-3PO Returns The Last Assembled Sentence Tree ───────────────────────────
    // "Here is the fully assembled parse tree from our last call to parse()."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the parsed LDAP filter model.
     * Returns the root {@link LdapFilter} produced by the most recent call
     * to {@link #parse(String)}.  The model is never {@code null} — an empty
     * (invalid) model is returned when {@link #parse(String)} has not yet been
     * called.
     *
     * @return the parsed model
     */
    public LdapFilter getModel()
    {
        return model;
    }


    // ── C-3PO Reads The Full Jawa Phrase And Builds The AST ──────────────────────
    // "New phrase received.  Resetting stack and scanner.  Reading tokens one
    // by one: '(' pushes a new filter node, '&'/'|'/'!' sets the component type,
    // ATTRIBUTE starts a leaf node, VALUE / filter-type tokens fill it in, ')'
    // pops and closes.  Any token that doesn't fit becomes an ERROR node."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the given LDAP filter string and rebuilds the internal model.
     * After this method returns, call {@link #getModel()} to retrieve the
     * resulting AST root.
     *
     * <p>The parse loop dispatches on {@link LdapFilterToken} type constants:
     * {@code LPAR} / {@code RPAR} manage the open-filter stack; {@code AND} /
     * {@code OR} / {@code NOT} create the matching component; {@code ATTRIBUTE}
     * creates an item or extensible component; {@code VALUE}, filter-type
     * tokens, and extensible sub-tokens fill in the open component.  Tokens
     * that arrive in an unexpected context are converted to ERROR tokens via
     * {@link #handleError(boolean, LdapFilterToken, LdapFilter)}.</p>
     *
     * @param ldapFilter the LDAP filter string to parse; must not be
     *                   {@code null}
     */
    public void parse( String ldapFilter )
    {
        // reset state
        filterStack = new Stack<LdapFilter>();
        scanner.reset( ldapFilter );
        model = new LdapFilter();

        // handle error tokens before filter
        LdapFilterToken token = scanner.nextToken();
        while ( token.getType() != LdapFilterToken.LPAR && token.getType() != LdapFilterToken.EOF )
        {
            handleError( false, token, model );
            token = scanner.nextToken();
        }

        // check filter start
        if ( token.getType() == LdapFilterToken.LPAR )
        {
            // start top level filter
            model.setStartToken( token );
            filterStack.push( model );

            // loop till filter end or EOF
            do
            {
                // next token
                token = scanner.nextToken();

                switch ( token.getType() )
                {
                    case LdapFilterToken.LPAR:
                    {
                        LdapFilter newFilter = new LdapFilter();
                        newFilter.setStartToken( token );

                        LdapFilter currentFilter = filterStack.peek();
                        LdapFilterComponent filterComponent = currentFilter.getFilterComponent();
                        if ( filterComponent != null && filterComponent.addFilter( newFilter ) )
                        {
                            filterStack.push( newFilter );
                        }
                        else
                        {
                            currentFilter.addOtherToken( token );
                        }

                        break;
                    }
                    case LdapFilterToken.RPAR:
                    {
                        LdapFilter currentFilter = filterStack.pop();
                        handleError( currentFilter.setStopToken( token ), token, currentFilter );
                        /*
                         * if(!filterStack.isEmpty()) { LdapFilter parentFilter =
                         * (LdapFilter) filterStack.peek(); LdapFilterComponent
                         * filterComponent = parentFilter.getFilterComponent();
                         * filterComponent.addFilter(currentFilter); }
                         */
                        break;
                    }
                    case LdapFilterToken.AND:
                    {
                        LdapFilter currentFilter = filterStack.peek();
                        LdapAndFilterComponent filterComponent = new LdapAndFilterComponent( currentFilter );
                        filterComponent.setStartToken( token );
                        handleError( currentFilter.setFilterComponent( filterComponent ), token, currentFilter );
                        break;
                    }
                    case LdapFilterToken.OR:
                    {
                        LdapFilter currentFilter = filterStack.peek();
                        LdapOrFilterComponent filterComponent = new LdapOrFilterComponent( currentFilter );
                        filterComponent.setStartToken( token );
                        handleError( currentFilter.setFilterComponent( filterComponent ), token, currentFilter );
                        break;
                    }
                    case LdapFilterToken.NOT:
                    {
                        LdapFilter currentFilter = filterStack.peek();
                        LdapNotFilterComponent filterComponent = new LdapNotFilterComponent( currentFilter );
                        filterComponent.setStartToken( token );
                        handleError( currentFilter.setFilterComponent( filterComponent ), token, currentFilter );
                        break;
                    }
                    case LdapFilterToken.ATTRIBUTE:
                    {
                        LdapFilter currentFilter = filterStack.peek();
                        LdapFilterItemComponent filterComponent = new LdapFilterItemComponent( currentFilter );
                        filterComponent.setAttributeToken( token );
                        handleError( currentFilter.setFilterComponent( filterComponent ), token, currentFilter );
                        break;
                    }
                    case LdapFilterToken.VALUE:
                    {
                        LdapFilter currentFilter = filterStack.peek();
                        LdapFilterComponent filterComponent = currentFilter.getFilterComponent();
                        if ( filterComponent instanceof LdapFilterItemComponent )
                        {
                            handleError( ( filterComponent instanceof LdapFilterItemComponent )
                                && ( ( LdapFilterItemComponent ) filterComponent ).setValueToken( token ), token,
                                currentFilter );
                        }
                        else if ( filterComponent instanceof LdapFilterExtensibleComponent )
                        {
                            handleError( ( filterComponent instanceof LdapFilterExtensibleComponent )
                                && ( ( LdapFilterExtensibleComponent ) filterComponent ).setValueToken( token ), token,
                                currentFilter );
                        }
                        else
                        {
                            handleError( false, token, currentFilter );
                        }
                        break;
                    }
                    case LdapFilterToken.EQUAL:
                    case LdapFilterToken.GREATER:
                    case LdapFilterToken.LESS:
                    case LdapFilterToken.APROX:
                    case LdapFilterToken.PRESENT:
                    case LdapFilterToken.SUBSTRING:
                    {
                        LdapFilter currentFilter = filterStack.peek();
                        LdapFilterComponent filterComponent = currentFilter.getFilterComponent();
                        if ( filterComponent instanceof LdapFilterItemComponent )
                        {
                            handleError( ( filterComponent instanceof LdapFilterItemComponent )
                                && ( ( LdapFilterItemComponent ) filterComponent ).setFiltertypeToken( token ), token,
                                currentFilter );
                        }
                        else if ( filterComponent instanceof LdapFilterExtensibleComponent )
                        {
                            handleError( ( filterComponent instanceof LdapFilterExtensibleComponent )
                                && ( ( LdapFilterExtensibleComponent ) filterComponent ).setEqualsToken( token ),
                                token, currentFilter );
                        }
                        else
                        {
                            handleError( false, token, currentFilter );
                        }
                        break;
                    }
                    case LdapFilterToken.WHITESPACE:
                    {
                        LdapFilter currentFilter = filterStack.peek();
                        currentFilter.addOtherToken( token );
                        break;
                    }
                    case LdapFilterToken.EXTENSIBLE_ATTRIBUTE:
                    {
                        LdapFilter currentFilter = ( LdapFilter ) filterStack.peek();
                        LdapFilterExtensibleComponent filterComponent = new LdapFilterExtensibleComponent(
                            currentFilter );
                        filterComponent.setAttributeToken( token );
                        handleError( currentFilter.setFilterComponent( filterComponent ), token, currentFilter );
                        break;
                    }
                    case LdapFilterToken.EXTENSIBLE_DNATTR_COLON:
                    {
                        LdapFilter currentFilter = ( LdapFilter ) filterStack.peek();
                        LdapFilterComponent filterComponent = currentFilter.getFilterComponent();
                        if ( filterComponent == null )
                        {
                            filterComponent = new LdapFilterExtensibleComponent( currentFilter );
                            ( ( LdapFilterExtensibleComponent ) filterComponent ).setDnAttrColonToken( token );
                            handleError( currentFilter.setFilterComponent( filterComponent ), token, currentFilter );
                        }
                        else
                        {
                            handleError( ( filterComponent instanceof LdapFilterExtensibleComponent )
                                && ( ( LdapFilterExtensibleComponent ) filterComponent ).setDnAttrColonToken( token ),
                                token, currentFilter );
                        }
                        break;
                    }
                    case LdapFilterToken.EXTENSIBLE_DNATTR:
                    {
                        LdapFilter currentFilter = ( LdapFilter ) filterStack.peek();
                        LdapFilterComponent filterComponent = currentFilter.getFilterComponent();
                        handleError( ( filterComponent instanceof LdapFilterExtensibleComponent )
                            && ( ( LdapFilterExtensibleComponent ) filterComponent ).setDnAttrToken( token ), token,
                            currentFilter );
                        break;
                    }
                    case LdapFilterToken.EXTENSIBLE_MATCHINGRULEOID_COLON:
                    {
                        LdapFilter currentFilter = ( LdapFilter ) filterStack.peek();
                        LdapFilterComponent filterComponent = currentFilter.getFilterComponent();
                        if ( filterComponent == null )
                        {
                            filterComponent = new LdapFilterExtensibleComponent( currentFilter );
                            ( ( LdapFilterExtensibleComponent ) filterComponent ).setMatchingRuleColonToken( token );
                            handleError( currentFilter.setFilterComponent( filterComponent ), token, currentFilter );
                        }
                        else
                        {
                            handleError( ( filterComponent instanceof LdapFilterExtensibleComponent )
                                && ( ( LdapFilterExtensibleComponent ) filterComponent )
                                    .setMatchingRuleColonToken( token ), token, currentFilter );
                        }
                        break;
                    }
                    case LdapFilterToken.EXTENSIBLE_MATCHINGRULEOID:
                    {
                        LdapFilter currentFilter = ( LdapFilter ) filterStack.peek();
                        LdapFilterComponent filterComponent = currentFilter.getFilterComponent();
                        handleError( ( filterComponent instanceof LdapFilterExtensibleComponent )
                            && ( ( LdapFilterExtensibleComponent ) filterComponent ).setMatchingRuleToken( token ),
                            token, currentFilter );
                        break;
                    }
                    case LdapFilterToken.EXTENSIBLE_EQUALS_COLON:
                    {
                        LdapFilter currentFilter = ( LdapFilter ) filterStack.peek();
                        LdapFilterComponent filterComponent = currentFilter.getFilterComponent();
                        handleError( ( filterComponent instanceof LdapFilterExtensibleComponent )
                            && ( ( LdapFilterExtensibleComponent ) filterComponent ).setEqualsColonToken( token ),
                            token, currentFilter );
                        break;
                    }

                    case LdapFilterToken.EOF:
                    {
                        model.addOtherToken( token );
                        break;
                    }
                    default:
                    {
                        LdapFilter currentFilter = filterStack.peek();
                        handleError( false, token, currentFilter );
                    }
                }
            }
            while ( !filterStack.isEmpty() && token.getType() != LdapFilterToken.EOF );
        }

        // handle error token after filter
        token = scanner.nextToken();
        while ( token.getType() != LdapFilterToken.EOF )
        {
            handleError( false, token, model );
            token = scanner.nextToken();
        }
    }


    // ── Han Shoots First When A Token Arrives Out Of Place ───────────────────────
    // When a token doesn't fit the current filter's context — Greedo reaching
    // for his blaster — we don't wait to see what happens.  We immediately wrap
    // it in an ERROR token and attach it to the open filter so the caller can
    // flag it in the UI without the parse blowing up entirely.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Helper method to handle parse errors.
     * If {@code success} is {@code false}, wraps {@code token} as an
     * {@link LdapFilterToken#ERROR} token and attaches it to {@code filter}
     * via {@link LdapFilter#addOtherToken(LdapFilterToken)}.  This keeps the
     * parse moving forward rather than throwing an exception.
     *
     * @param success {@code true} if the caller's action succeeded and no
     *                error token is needed; {@code false} to record an error
     * @param token   the token that caused the problem
     * @param filter  the currently open filter to attach the error to
     */
    private void handleError( boolean success, LdapFilterToken token, LdapFilter filter )
    {
        if ( !success )
        {
            filter.addOtherToken( new LdapFilterToken( LdapFilterToken.ERROR, token.getValue(), token.getOffset() ) );
        }
    }

}
