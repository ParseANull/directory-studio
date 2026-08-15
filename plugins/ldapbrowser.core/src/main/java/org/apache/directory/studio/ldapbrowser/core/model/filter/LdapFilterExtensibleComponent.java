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


// ── CLASS: LdapFilterExtensibleComponent — C-3PO PARSING A JAWA EXTENDED MATCH ─
// The most complex Jawa comparison C-3PO encounters is the "extensible match":
// "cn:dn:2.5.13.5:=Luke".  It has up to eight parts: attribute name, an
// optional ":dn" DN-attribute flag, an optional ":ruleOid" matching-rule OID,
// a ":=" separator, and a value.  C-3PO must accept them in any valid
// combination — attribute-only, matching-rule-only, or both together.
// LdapFilterExtensibleComponent holds all eight token slots for RFC 4515
// extensible match filters and validates that the required combination is
// present.  Like an item component it is a leaf node — no child filters.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents an LDAP extensible match filter in the filter AST — e.g.
 * {@code cn:dn:2.5.13.5:=Luke} or {@code :1.2.3.4:=value}.
 * Contains slots for the attribute token, optional dn-attr colon and flag,
 * optional matching-rule colon and OID, the {@code :=} separator, and the value.
 * No child filters are accepted.
 *
 * <p>Think of this as C-3PO parsing a complex Jawa extended-match phrase —
 * up to eight positional tokens with several optional parts that must fit
 * together in a valid combination.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapFilterExtensibleComponent extends LdapFilterComponent
{
    private LdapFilterToken attributeToken;

    private LdapFilterToken dnAttrColonToken;
    private LdapFilterToken dnAttrToken;

    private LdapFilterToken matchingRuleColonToken;
    private LdapFilterToken matchingRuleToken;

    private LdapFilterToken equalsColonToken;
    private LdapFilterToken equalsToken;

    private LdapFilterToken valueToken;


    // ── C-3PO Opens A New Extensible Match Phrase Slot ───────────────────────────
    /**
     * Creates a new instance of LdapFilterExtensibleComponent.
     *
     * @param parent the parent filter
     */
    public LdapFilterExtensibleComponent( LdapFilter parent )
    {
        super( parent );
    }


    // ── C-3PO Records The Attribute Name Token ────────────────────────────────────
    // "Optional attribute name — if present, it identifies the attribute to match."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the attribute token.
     *
     * @param attributeToken the attribute token
     * @return {@code true} if setting the attribute token was successful, {@code false} otherwise.
     */
    public boolean setAttributeToken( LdapFilterToken attributeToken )
    {
        if ( this.attributeToken == null && attributeToken != null
            && attributeToken.getType() == LdapFilterToken.EXTENSIBLE_ATTRIBUTE )
        {
            if ( super.getStartToken() == null )
            {
                super.setStartToken( attributeToken );
            }
            this.attributeToken = attributeToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The Attribute Token ──────────────────────────────────────────
    /**
     * Gets the attribute token.
     *
     * @return the attribute token, or {@code null} if not set
     */
    public LdapFilterToken getAttributeToken()
    {
        return attributeToken;
    }


    // ── C-3PO Records The ":dn" Colon Separator ──────────────────────────────────
    /**
     * Sets the dn attr colon token.
     *
     * @param dnAttrColonToken the dn attr colon token
     * @return {@code true} if setting the dn attr colon token was successful, {@code false} otherwise
     */
    public boolean setDnAttrColonToken( LdapFilterToken dnAttrColonToken )
    {
        if ( this.dnAttrColonToken == null && dnAttrColonToken != null
            && dnAttrColonToken.getType() == LdapFilterToken.EXTENSIBLE_DNATTR_COLON )
        {
            if ( super.getStartToken() == null )
            {
                super.setStartToken( dnAttrColonToken );
            }
            this.dnAttrColonToken = dnAttrColonToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The ":dn" Colon Token ────────────────────────────────────────
    /**
     * Gets the dn attr colon token.
     *
     * @return the dn attr colon token, or {@code null} if not set
     */
    public LdapFilterToken getDnAttrColonToken()
    {
        return dnAttrColonToken;
    }


    // ── C-3PO Records The "dn" Flag Token ────────────────────────────────────────
    /**
     * Sets the dn attr token.
     *
     * @param dnAttrToken the dn attr token
     * @return {@code true} if setting the dn attr token was successful, {@code false} otherwise
     */
    public boolean setDnAttrToken( LdapFilterToken dnAttrToken )
    {
        if ( this.dnAttrToken == null && dnAttrToken != null
            && dnAttrToken.getType() == LdapFilterToken.EXTENSIBLE_DNATTR )
        {
            this.dnAttrToken = dnAttrToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The "dn" Flag Token ──────────────────────────────────────────
    /**
     * Gets the dn attr token.
     *
     * @return the dn attr token, or {@code null} if not set
     */
    public LdapFilterToken getDnAttrToken()
    {
        return dnAttrToken;
    }


    // ── C-3PO Records The Matching-Rule Colon Separator ──────────────────────────
    /**
     * Sets the matching rule colon token.
     *
     * @param matchingRuleColonToken the matching rule colon token
     * @return {@code true} if setting the matching rule colon token was successful, {@code false} otherwise
     */
    public boolean setMatchingRuleColonToken( LdapFilterToken matchingRuleColonToken )
    {
        if ( this.matchingRuleColonToken == null && matchingRuleColonToken != null
            && matchingRuleColonToken.getType() == LdapFilterToken.EXTENSIBLE_MATCHINGRULEOID_COLON )
        {
            if ( super.getStartToken() == null )
            {
                super.setStartToken( matchingRuleColonToken );
            }
            this.matchingRuleColonToken = matchingRuleColonToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The Matching-Rule Colon Token ────────────────────────────────
    /**
     * Gets the matching rule colon token.
     *
     * @return the matching rule colon token, or {@code null} if not set
     */
    public LdapFilterToken getMatchingRuleColonToken()
    {
        return matchingRuleColonToken;
    }


    // ── C-3PO Records The Matching-Rule OID Token ────────────────────────────────
    /**
     * Sets the matching rule token.
     *
     * @param matchingRuleToken the matching rule token
     * @return {@code true} if setting the matching rule token was successful, {@code false} otherwise
     */
    public boolean setMatchingRuleToken( LdapFilterToken matchingRuleToken )
    {
        if ( this.matchingRuleToken == null && matchingRuleToken != null
            && matchingRuleToken.getType() == LdapFilterToken.EXTENSIBLE_MATCHINGRULEOID )
        {
            this.matchingRuleToken = matchingRuleToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The Matching-Rule OID Token ──────────────────────────────────
    /**
     * Gets the matching rule token.
     *
     * @return the matching rule token, or {@code null} if not set
     */
    public LdapFilterToken getMatchingRuleToken()
    {
        return matchingRuleToken;
    }


    // ── C-3PO Records The ":=" Colon Token ───────────────────────────────────────
    /**
     * Sets the equals colon token.
     *
     * @param equalsColonToken the equals colon token
     * @return {@code true} if setting the equals colon token was successful, {@code false} otherwise
     */
    public boolean setEqualsColonToken( LdapFilterToken equalsColonToken )
    {
        if ( this.equalsColonToken == null && equalsColonToken != null
            && equalsColonToken.getType() == LdapFilterToken.EXTENSIBLE_EQUALS_COLON )
        {
            this.equalsColonToken = equalsColonToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The ":=" Colon Token ─────────────────────────────────────────
    /**
     * Gets the equals colon token.
     *
     * @return the equals colon token, or {@code null} if not set
     */
    public LdapFilterToken getEqualsColonToken()
    {
        return equalsColonToken;
    }


    // ── C-3PO Records The "=" Equals Token ───────────────────────────────────────
    /**
     * Sets the equals token.
     *
     * @param equalsToken the equals token
     * @return {@code true} if setting the equals token was successful, {@code false} otherwise
     */
    public boolean setEqualsToken( LdapFilterToken equalsToken )
    {
        if ( this.equalsToken == null && equalsToken != null && equalsToken.getType() == LdapFilterToken.EQUAL )
        {
            this.equalsToken = equalsToken;
            return true;
        }
        else
        {
            return false;
        }
    }


    // ── C-3PO Reads The "=" Equals Token ─────────────────────────────────────────
    /**
     * Gets the equals token.
     *
     * @return the equals token, or {@code null} if not set
     */
    public LdapFilterToken getEqualsToken()
    {
        return equalsToken;
    }


    // ── C-3PO Records The Value Token ────────────────────────────────────────────
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
     * @return the value token, or {@code null} if not set
     */
    public LdapFilterToken getValueToken()
    {
        return this.valueToken;
    }


    // ── C-3PO Validates The Extended Match Phrase ────────────────────────────────
    // "Required: start token + `:=` separator + `=` sign + value.
    // Optional: attribute name and/or matching-rule OID — but if a colon is
    // present for one, the actual token must follow."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#isValid()
     */
    public boolean isValid()
    {
        return startToken != null
            && equalsColonToken != null
            & equalsToken != null
            && valueToken != null
            &&

            ( ( attributeToken != null
                && ( ( dnAttrColonToken == null && dnAttrToken == null ) || ( dnAttrColonToken != null && dnAttrToken != null ) ) && ( ( matchingRuleColonToken == null && matchingRuleToken == null ) || ( matchingRuleColonToken != null && matchingRuleToken != null ) ) ) || ( attributeToken == null
                && ( ( dnAttrColonToken == null && dnAttrToken == null ) || ( dnAttrColonToken != null && dnAttrToken != null ) )
                && matchingRuleColonToken != null && matchingRuleToken != null ) );
    }


    // ── C-3PO Collects All Tokens Of The Extended Match Phrase ───────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getTokens()
     */
    public LdapFilterToken[] getTokens()
    {
        // collect tokens
        List<LdapFilterToken> tokenList = new ArrayList<LdapFilterToken>();
        if ( attributeToken != null )
        {
            tokenList.add( attributeToken );
        }
        if ( dnAttrColonToken != null )
        {
            tokenList.add( dnAttrColonToken );
        }
        if ( dnAttrToken != null )
        {
            tokenList.add( dnAttrToken );
        }
        if ( matchingRuleColonToken != null )
        {
            tokenList.add( matchingRuleColonToken );
        }
        if ( matchingRuleToken != null )
        {
            tokenList.add( matchingRuleToken );
        }
        if ( equalsColonToken != null )
        {
            tokenList.add( equalsColonToken );
        }
        if ( equalsToken != null )
        {
            tokenList.add( equalsToken );
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


    // ── C-3PO Renders The Extended Match Phrase As A String ──────────────────────
    // "cn:dn:2.5.13.5:=Luke"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return ( attributeToken != null ? startToken.getValue() : "" ) //$NON-NLS-1$
            + ( dnAttrColonToken != null ? dnAttrColonToken.getValue() : "" ) //$NON-NLS-1$
            + ( dnAttrToken != null ? dnAttrToken.getValue() : "" ) //$NON-NLS-1$
            + ( matchingRuleColonToken != null ? matchingRuleColonToken.getValue() : "" ) //$NON-NLS-1$
            + ( matchingRuleToken != null ? matchingRuleToken.getValue() : "" ) //$NON-NLS-1$
            + ( equalsColonToken != null ? equalsColonToken.getValue() : "" ) //$NON-NLS-1$
            + ( equalsToken != null ? equalsToken.getValue() : "" ) //$NON-NLS-1$
            + ( valueToken != null ? valueToken.getValue() : "" ); //$NON-NLS-1$
    }


    // ── C-3PO Refuses Child Sub-Sentences For A Leaf Clause ──────────────────────
    /**
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
            && offset < startToken.getOffset() + toString().length() )
        {
            return parent;
        }
        else
        {
            return null;
        }
    }


    // ── C-3PO Explains Why The Extended Match Phrase Is Invalid ──────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterComponent#getInvalidCause()
     */
    public String getInvalidCause()
    {
        if ( dnAttrColonToken != null && dnAttrToken == null )
        {
            return Messages.LdapFilterExtensibleComponent_MissingDn;
        }
        else if ( matchingRuleColonToken != null && matchingRuleToken == null )
        {
            return Messages.LdapFilterExtensibleComponent_MissingMatchingRule;
        }
        else if ( equalsColonToken == null )
        {
            return Messages.LdapFilterExtensibleComponent_MissingColon;
        }
        else if ( equalsToken == null )
        {
            return Messages.LdapFilterExtensibleComponent_MissingEquals;
        }
        else if ( attributeToken == null )
        {
            return Messages.LdapFilterExtensibleComponent_MissingAttributeType;
        }
        else if ( valueToken != null )
        {
            return Messages.LdapFilterExtensibleComponent_MissingValue;
        }
        else
        {
            return null;
        }
    }

}
