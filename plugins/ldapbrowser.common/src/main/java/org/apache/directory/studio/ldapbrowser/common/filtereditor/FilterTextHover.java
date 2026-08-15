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

package org.apache.directory.studio.ldapbrowser.common.filtereditor;


import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilter;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterExtensibleComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterItemComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;


// ── CLASS: FilterTextHover — R2 BEEPS A WARNING TO LUKE ──────────────────────
// During the trench run on the Death Star, R2-D2 monitors the sensor data
// behind Luke and beeps urgent warnings about TIE fighters approaching and
// damage to the astromech socket. Luke hovers his hand near a control and R2
// immediately projects the relevant readout — either "that's a valid attribute
// type here's its schema definition" or "warning: that part of the filter is
// broken, here's why."
// We do exactly that: when the user hovers the mouse over any token in the
// filter editor, we inspect that token and return either the LDAP schema
// description (for attribute types, object classes, matching rules) or an error
// message (for invalid filter fragments and unrecognised characters).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements Eclipse's {@link ITextHover} for the LDAP filter editor. When
 * the user hovers over any part of the filter string, we return a tooltip:
 * either the LDAP schema definition for a recognised attribute type / object
 * class / matching rule, or an error message explaining why a fragment is
 * syntactically invalid.
 * Think of this class as R2-D2 beeping context-sensitive warnings and readouts
 * to Luke during the trench run — right information, right moment.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterTextHover implements ITextHover
{

    /** The filter parser. */
    private LdapFilterParser parser;

    /** The schema, used to retrieve attributeType and objectClass information. */
    private Schema schema;


    // ── R2 PLUGS INTO LUKE'S TARGETING COMPUTER ───────────────────────────────
    // R2 jacks into the X-wing's sensor bus so he can read what Luke's targeting
    // computer is pointing at. He keeps the parser reference so he can ask "what
    // filter token is the cursor hovering over right now?"
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code FilterTextHover} wired to the given filter parser.
     * We don't have a schema yet at construction time — call
     * {@link #setSchema(Schema)} after connecting to an LDAP server.
     *
     * @param parser  the filter parser whose current model we inspect to find
     *                the token under the hover cursor
     */
    public FilterTextHover( LdapFilterParser parser )
    {
        this.parser = parser;
    }


    // ── R2 DOWNLOADS THE IMPERIAL DATABASE ───────────────────────────────────
    // Once the X-wing is in range of the Rebel base's database, R2 downloads
    // the full schema — all known attribute types, object classes, and matching
    // rules — so he can give Luke accurate readouts during the run.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP schema used to look up attribute-type, object-class, and
     * matching-rule descriptions for tooltip content. Call this whenever the
     * user connects to a different LDAP server or the schema changes. Passing
     * {@code null} disables schema-based tooltips (error tooltips still work).
     *
     * @param schema  the current LDAP schema, or {@code null} if not connected
     */
    public void setSchema( Schema schema )
    {
        this.schema = schema;
    }


    // ── R2 READS THE SENSOR AT THE HOVER POSITION ────────────────────────────
    // Luke's targeting reticle stops over a symbol on the trench map. R2 looks
    // up what that symbol means in the Rebel database: if it's a known attribute
    // or object class, he projects its LDIF schema line; if it's a corrupted
    // nav coordinate or unrecognised character, he flashes the error reason.
    // We check schema tokens first, then invalid-filter annotations, then
    // individual error tokens, and return the most relevant string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tooltip text for the token at {@code hoverRegion}. We check
     * three things in priority order:
     * <ol>
     *   <li>If the schema is loaded and the hovered token is an attribute type,
     *       object class value (for {@code objectClass=} filters), or extensible
     *       matching rule, we return the LDIF schema definition line.</li>
     *   <li>If the hover position overlaps an invalid filter fragment, we return
     *       the parser's explanation of why it is invalid.</li>
     *   <li>If the hover position overlaps an ERROR token, we return a generic
     *       "invalid characters" message.</li>
     * </ol>
     * Returns {@code null} if none of the above apply (no tooltip is shown).
     *
     * <p>For example — R2 reads Luke's targeting reticle:</p>
     * <pre>
     *   Hovering over "commonName" in "(commonName=Luke*)"
     *   R2 checks database → attributeType "cn" found
     *   Tooltip = "( 2.5.4.3 NAME 'cn' SUP name )"
     * </pre>
     *
     * @param textViewer   the text viewer hosting the filter editor (not used
     *                     directly — we read from the parser model instead)
     * @param hoverRegion  the region the mouse is hovering over — we use its
     *                     offset to locate the relevant token
     * @return             the tooltip string, or {@code null} if no tooltip
     *                     applies at this position
     */
    public String getHoverInfo( ITextViewer textViewer, IRegion hoverRegion )
    {
        // check attribute type, object class or matching rule values
        if ( schema != null )
        {
            LdapFilter filter = parser.getModel().getFilter( hoverRegion.getOffset() );

            if ( filter.getFilterComponent() instanceof LdapFilterItemComponent )
            {
                LdapFilterItemComponent fc = ( LdapFilterItemComponent ) filter.getFilterComponent();
                if ( fc.getAttributeToken() != null
                    && fc.getAttributeToken().getOffset() <= hoverRegion.getOffset()
                    && hoverRegion.getOffset() <= fc.getAttributeToken().getOffset()
                        + fc.getAttributeToken().getLength() )
                {
                    String attributeType = fc.getAttributeToken().getValue();
                    AttributeType attributeTypeDescription = schema
                        .getAttributeTypeDescription( attributeType );
                    String ldifLine = SchemaUtils.getLdifLine( attributeTypeDescription );
                    return ldifLine;
                }
                if ( fc.getAttributeToken() != null
                    && SchemaConstants.OBJECT_CLASS_AT.equalsIgnoreCase( fc.getAttributeToken().getValue() )
                    && fc.getValueToken() != null && fc.getValueToken().getOffset() <= hoverRegion.getOffset()
                    && hoverRegion.getOffset() <= fc.getValueToken().getOffset() + fc.getValueToken().getLength() )
                {
                    String objectClass = fc.getValueToken().getValue();
                    ObjectClass objectClassDescription = schema.getObjectClassDescription( objectClass );
                    String ldifLine = SchemaUtils.getLdifLine( objectClassDescription );
                    return ldifLine;
                }
            }
            if ( filter.getFilterComponent() instanceof LdapFilterExtensibleComponent )
            {
                LdapFilterExtensibleComponent fc = ( LdapFilterExtensibleComponent ) filter.getFilterComponent();
                if ( fc.getAttributeToken() != null
                    && fc.getAttributeToken().getOffset() <= hoverRegion.getOffset()
                    && hoverRegion.getOffset() <= fc.getAttributeToken().getOffset()
                        + fc.getAttributeToken().getLength() )
                {
                    String attributeType = fc.getAttributeToken().getValue();
                    AttributeType attributeTypeDescription = schema
                        .getAttributeTypeDescription( attributeType );
                    String ldifLine = SchemaUtils.getLdifLine( attributeTypeDescription );
                    return ldifLine;
                }
                if ( fc.getMatchingRuleToken() != null
                    && fc.getMatchingRuleToken().getOffset() <= hoverRegion.getOffset()
                    && hoverRegion.getOffset() <= fc.getMatchingRuleToken().getOffset()
                        + fc.getMatchingRuleToken().getLength() )
                {
                    String matchingRule = fc.getMatchingRuleToken().getValue();
                    MatchingRule matchingRuleDescription = schema.getMatchingRuleDescription( matchingRule );
                    String info = SchemaUtils.getLdifLine( matchingRuleDescription );
                    return info;
                }
            }
        }

        // check invalid tokens
        LdapFilter[] invalidFilters = parser.getModel().getInvalidFilters();
        for ( int i = 0; i < invalidFilters.length; i++ )
        {
            if ( invalidFilters[i].getStartToken() != null )
            {
                int start = invalidFilters[i].getStartToken().getOffset();
                int stop = invalidFilters[i].getStopToken() != null ? invalidFilters[i].getStopToken().getOffset()
                    + invalidFilters[i].getStopToken().getLength() : start
                    + invalidFilters[i].getStartToken().getLength();
                if ( start <= hoverRegion.getOffset() && hoverRegion.getOffset() < stop )
                {
                    return invalidFilters[i].getInvalidCause();
                }
            }
        }

        // check error tokens
        LdapFilterToken[] tokens = parser.getModel().getTokens();
        for ( int i = 0; i < tokens.length; i++ )
        {
            if ( tokens[i].getType() == LdapFilterToken.ERROR )
            {

                int start = tokens[i].getOffset();
                int stop = start + tokens[i].getLength();
                if ( start <= hoverRegion.getOffset() && hoverRegion.getOffset() < stop )
                {
                    return Messages.getString( "FilterTextHover.InvalidCharacters" ); //$NON-NLS-1$
                }
            }
        }
        return null;
    }


    // ── R2 DEFINES THE SENSOR SCAN WINDOW ────────────────────────────────────
    // R2's sensor returns a 1-character scan window centred on the hover point —
    // just enough to identify which token the targeting reticle is touching
    // without grabbing too wide a swath of the trench map.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the hover region for a given cursor offset. We always return a
     * single-character region starting at {@code offset} — this is sufficient
     * to identify which filter token the mouse is over, and Eclipse handles the
     * rest of the tooltip positioning logic.
     *
     * @param textViewer  the text viewer (not used)
     * @param offset      the document offset the mouse is hovering at
     * @return            a one-character {@link Region} at {@code offset}
     */
    public IRegion getHoverRegion( ITextViewer textViewer, int offset )
    {
        return new Region( offset, 1 );
    }

}
