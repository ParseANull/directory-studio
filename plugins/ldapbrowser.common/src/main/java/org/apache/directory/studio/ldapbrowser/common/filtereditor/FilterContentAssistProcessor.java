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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilter;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterExtensibleComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilterItemComponent;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterToken;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;


// ── CLASS: FilterContentAssistProcessor — R2 PROJECTS THE DEATH STAR PLANS ───
// In the Rebel base on Yavin IV, R2-D2 projects the holographic Death Star
// schematics and highlights every possible attack route as the pilots lean in.
// He knows the full layout (the schema) and, depending on where the briefing
// pointer is hovering, he highlights the relevant section of the plan.
// We do exactly that: using the LDAP schema as our "schematics", we look at
// where the cursor is in the filter string and project the most relevant
// attribute types, filter operators, object classes, or matching rules as
// autocomplete proposals for the user to pick from.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Computes autocomplete proposals for the LDAP filter editor. We inspect the
 * current cursor position in the filter string, figure out what kind of token
 * we're editing (attribute type, filter operator, object class value, or
 * extensible matching rule), and return a sorted list of matching suggestions
 * drawn from the connected directory's LDAP schema.
 * Think of this as R2-D2 projecting the Death Star hologram: point to any spot
 * and R2 highlights what's relevant right there.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterContentAssistProcessor extends TemplateCompletionProcessor implements IContentAssistProcessor,
    IContentProposalProvider
{

    private static final Comparator<String> NAME_AND_OID_COMPARATOR = new Comparator<String>()
    {
        public int compare( String s1, String s2 )
        {
            if ( s1.matches( "[0-9\\.]+" ) && !s2.matches( "[0-9\\.]+" ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                return 1;
            }
            else if ( !s1.matches( "[0-9\\.]+" ) && s2.matches( "[0-9\\.]+" ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                return -1;
            }
            else
            {
                return s1.compareToIgnoreCase( s2 );
            }
        }
    };

    /** The parser. */
    private LdapFilterParser parser;

    /** The source viewer, may be null. */
    private ISourceViewer sourceViewer;

    /** The auto activation characters. */
    private char[] autoActivationCharacters;

    /** The schema, used to retrieve attributeType and objectClass information. */
    private Schema schema;

    /** The possible attribute types. */
    private Map<String, AttributeType> possibleAttributeTypes;

    /** The possible filter types. */
    private Map<String, String> possibleFilterTypes;

    /** The possible object classes. */
    private Map<String, ObjectClass> possibleObjectClasses;

    /** The possible matching rules. */
    private Map<String, MatchingRule> possibleMatchingRules;


    // ── R2 SETS UP WITHOUT A DISPLAY SCREEN ──────────────────────────────────
    // Sometimes R2 can project the plans without a full briefing room — just
    // him and the parser, no viewer screen attached. He still queues up the
    // full schema lookup chain, ready to answer when someone asks.
    // We delegate to the two-arg constructor with a null source viewer so the
    // logic stays in one place.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a processor with no source viewer. Use this when the filter
     * editor is embedded in a simple text field (not a full
     * {@link ISourceViewer}) — template proposals won't be offered, but all
     * schema-driven proposals still work.
     *
     * @param parser  the filter parser we use to locate the cursor's position
     *                inside the filter tree
     */
    public FilterContentAssistProcessor( LdapFilterParser parser )
    {
        this( null, parser );
    }


    // ── R2 FIRES UP THE HOLOGRAM PROJECTOR ───────────────────────────────────
    // Standing before the full Rebel briefing room, R2 initialises the
    // holographic projector and pre-loads the activation triggers: every key
    // the pilots might press that could reveal a new attack route gets
    // registered so the projection pops up automatically.
    // We do the same: store the parser and source viewer, then build the full
    // auto-activation character array covering every alphanumeric and LDAP
    // operator character.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a fully-configured processor wired to both a source viewer and
     * a parser. We pre-build the auto-activation character array (all letters,
     * digits, and LDAP operator characters) so Eclipse pops up the proposal list
     * automatically as the user types.
     *
     * @param sourceViewer  the source viewer hosting the filter editor;
     *                      may be {@code null} if running in a simple text field
     * @param parser        the filter parser we call to find the current token
     *                      under the cursor
     */
    public FilterContentAssistProcessor( ISourceViewer sourceViewer, LdapFilterParser parser )
    {
        this.parser = parser;
        this.sourceViewer = sourceViewer;

        this.autoActivationCharacters = new char[7 + 10 + 26 + 26];
        this.autoActivationCharacters[0] = '(';
        this.autoActivationCharacters[1] = ')';
        this.autoActivationCharacters[2] = '&';
        this.autoActivationCharacters[3] = '|';
        this.autoActivationCharacters[4] = '!';
        this.autoActivationCharacters[5] = ':';
        this.autoActivationCharacters[6] = '.';
        int i = 7;
        for ( char c = 'a'; c <= 'z'; c++, i++ )
        {
            this.autoActivationCharacters[i] = c;
        }
        for ( char c = 'A'; c <= 'Z'; c++, i++ )
        {
            this.autoActivationCharacters[i] = c;
        }
        for ( char c = '0'; c <= '9'; c++, i++ )
        {
            this.autoActivationCharacters[i] = c;
        }
    }


    // ── R2 LOADS THE DEATH STAR SCHEMATICS ───────────────────────────────────
    // Before the briefing, a Rebel technician hands R2 the complete Death Star
    // blueprints. R2 indexes every corridor, turret, and exhaust port so he can
    // instantly answer any question about any part of the station.
    // We index all attribute types, filter operators, object classes, and matching
    // rules from the LDAP schema so proposal lookups are fast.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads an LDAP {@link Schema} and pre-indexes all attribute types, object
     * classes, filter operators, and matching rules into sorted maps. After this
     * call, all proposal methods can run in O(log n) time against the pre-built
     * indexes.
     * <p>
     * Call this whenever the user connects to a different LDAP server or the
     * schema changes — the old index is discarded and rebuilt from scratch.
     * </p>
     *
     * @param schema  the schema to index; passing {@code null} clears all
     *                proposals (useful when we are disconnected)
     */
    public void setSchema( Schema schema )
    {
        this.schema = schema;

        possibleAttributeTypes = new TreeMap<String, AttributeType>( NAME_AND_OID_COMPARATOR );
        possibleFilterTypes = new LinkedHashMap<String, String>();
        possibleObjectClasses = new TreeMap<String, ObjectClass>( NAME_AND_OID_COMPARATOR );
        possibleMatchingRules = new TreeMap<String, MatchingRule>( NAME_AND_OID_COMPARATOR );

        if ( schema != null )
        {
            Collection<AttributeType> attributeTypeDescriptions = schema.getAttributeTypeDescriptions();
            for ( AttributeType atd : attributeTypeDescriptions )
            {
                possibleAttributeTypes.put( atd.getOid(), atd );
                for ( String atdName : atd.getNames() )
                {
                    possibleAttributeTypes.put( atdName, atd );
                }
            }

            possibleFilterTypes.put( "=", Messages.getString( "FilterContentAssistProcessor.Equals" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            possibleFilterTypes.put( "=*", Messages.getString( "FilterContentAssistProcessor.Present" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            possibleFilterTypes.put( "<=", Messages.getString( "FilterContentAssistProcessor.LessThanOrEquals" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            possibleFilterTypes.put( ">=", Messages.getString( "FilterContentAssistProcessor.GreaterThanOrEquals" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            possibleFilterTypes.put( "~=", Messages.getString( "FilterContentAssistProcessor.Approximately" ) ); //$NON-NLS-1$ //$NON-NLS-2$

            Collection<ObjectClass> ocds = schema.getObjectClassDescriptions();
            for ( ObjectClass ocd : ocds )
            {
                possibleObjectClasses.put( ocd.getOid(), ocd );
                for ( String name : ocd.getNames() )
                {
                    possibleObjectClasses.put( name, ocd );
                }
            }

            Collection<MatchingRule> matchingRuleDescriptions = schema.getMatchingRuleDescriptions();
            for ( MatchingRule description : matchingRuleDescriptions )
            {
                possibleMatchingRules.put( description.getOid(), description );
                for ( String name : description.getNames() )
                {
                    possibleMatchingRules.put( name, description );
                }
            }
        }
    }


    // ── R2 TELLS PILOTS WHICH KEYS TRIGGER THE HOLOGRAM ──────────────────────
    // R2 registers every button on the briefing console that should auto-trigger
    // the hologram projection so pilots don't have to press a special key —
    // the plans appear the moment they start typing a relevant character.
    // We return our pre-built array of auto-activation characters to Eclipse.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the set of characters that should automatically trigger the
     * content-assist popup without the user pressing Ctrl+Space. We activate on
     * all letters, digits, and LDAP filter punctuation ({@code ( ) & | ! : .}).
     *
     * @return  the auto-activation character array
     */
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return autoActivationCharacters;
    }


    // ── R2 PROJECTS PLANS FOR THE CURRENT POINTER POSITION ───────────────────
    // A pilot points his laser at a specific section of the Death Star hologram.
    // R2 reads the pointer position, finds the relevant section, and highlights
    // all the attack routes that apply to that spot.
    // We adapt the ITextViewer-based Eclipse API to our internal offset-based
    // logic by extracting the offset and delegating.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Computes completion proposals for the given cursor position in the filter
     * editor. This is the method Eclipse calls for source-viewer-style content
     * assist — we just extract the offset and delegate to our internal method.
     *
     * @param viewer  the text viewer hosting the filter editor
     * @param offset  the cursor position in the document
     * @return        an array of matching proposals (never {@code null})
     */
    public ICompletionProposal[] computeCompletionProposals( ITextViewer viewer, int offset )
    {
        return computeCompletionProposals( offset );
    }


    // ── R2 ADAPTS THE HOLOGRAM FOR A SIMPLE FIELD READER ─────────────────────
    // Some crew members don't have a full holographic display — they use a
    // portable data pad instead. R2 reformats the same Death Star plans into
    // a simpler data-pad compatible format without losing any information.
    // We convert Eclipse's ICompletionProposal objects to the simpler
    // IContentProposal format used by the JFace field-assist API.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Computes proposals in the simpler JFace field-assist format
     * ({@link IContentProposal}). This is used when the filter editor is
     * embedded in a plain text widget rather than a full source viewer. We run
     * the normal proposal computation and then wrap each result in an adapter
     * that speaks the {@link IContentProposalProvider} contract.
     *
     * @param contents  the full filter string currently in the field
     * @param position  the cursor position within {@code contents}
     * @return          an array of adapted proposals
     */
    public IContentProposal[] getProposals( final String contents, final int position )
    {
        parser.parse( contents );

        ICompletionProposal[] oldProposals = computeCompletionProposals( position );
        IContentProposal[] proposals = new IContentProposal[oldProposals.length];
        for ( int i = 0; i < oldProposals.length; i++ )
        {
            final ICompletionProposal oldProposal = oldProposals[i];
            final Document document = new Document( contents );
            oldProposal.apply( document );

            proposals[i] = new IContentProposal()
            {
                public String getContent()
                {
                    return document.get();
                }


                public int getCursorPosition()
                {
                    return oldProposal.getSelection( document ).x;
                }


                public String getDescription()
                {
                    return oldProposal.getAdditionalProposalInfo();
                }


                public String getLabel()
                {
                    return oldProposal.getDisplayString();
                }


                public String toString()
                {
                    return getContent();
                }
            };
        }

        return proposals;
    }


    // ── R2 IDENTIFIES THE ATTACK ROUTE AT THE CURSOR ─────────────────────────
    // When the briefing pointer stops at a spot on the Death Star hologram, R2
    // analyses exactly what structure is at that location — a corridor, a
    // turret emplacement, the exhaust port — and projects the relevant attack
    // options accordingly.
    // We inspect the filter parse tree at the cursor offset and build proposals
    // appropriate to the token type: attribute name, filter operator, object
    // class value, or extensible matching rule.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Core proposal computation. We look at the cursor offset in the currently
     * parsed filter tree and branch across four cases:
     * <ol>
     *   <li>Cursor after {@code (} with no filter component yet — offer templates
     *       and all attribute types.</li>
     *   <li>Cursor on a simple filter's attribute token — offer matching attribute
     *       types.</li>
     *   <li>Cursor on a simple filter's operator — offer matching filter
     *       operators.</li>
     *   <li>Cursor inside an extensible filter — offer attribute types, dn: flag,
     *       and matching rules as appropriate.</li>
     * </ol>
     *
     * @param offset  the cursor position in the filter string
     * @return        a (possibly empty) array of matching proposals
     */
    private ICompletionProposal[] computeCompletionProposals( int offset )
    {
        String[] possibleObjectClasses = schema == null ? new String[0] : SchemaUtils.getNamesAsArray( schema
            .getObjectClassDescriptions() );
        Arrays.sort( possibleObjectClasses );

        List<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>();
        LdapFilter filter = parser.getModel().getFilter( offset );
        if ( filter != null && offset > 0 )
        {
            // case 0: open curly started, show templates and all attribute types
            if ( filter.getStartToken() != null && filter.getFilterComponent() == null )
            {
                if ( sourceViewer != null )
                {
                    ICompletionProposal[] templateProposals = super.computeCompletionProposals( sourceViewer, offset );
                    if ( templateProposals != null )
                    {
                        proposalList.addAll( Arrays.asList( templateProposals ) );
                    }
                }
                addPossibleAttributeTypes( proposalList, "", offset ); //$NON-NLS-1$
            }

            // case A: simple filter
            if ( filter.getFilterComponent() instanceof LdapFilterItemComponent )
            {
                LdapFilterItemComponent fc = ( LdapFilterItemComponent ) filter.getFilterComponent();

                // case A1: editing attribute type: show matching attribute types
                if ( fc.getStartToken().getOffset() <= offset
                    && offset <= fc.getStartToken().getOffset() + fc.getStartToken().getLength() )
                {
                    addPossibleAttributeTypes( proposalList, fc.getAttributeToken().getValue(), fc.getAttributeToken()
                        .getOffset() );
                }

                String attributeType = null;
                if ( schema != null && schema.hasAttributeTypeDescription( fc.getAttributeToken().getValue() ) )
                {
                    attributeType = fc.getAttributeToken().getValue();
                }

                // case A2: after attribte type: show possible filter types and extensible match options
                if ( attributeType != null )
                {
                    if ( ( fc.getAttributeToken().getOffset() <= offset || fc.getFilterToken() != null )
                        && offset <= fc.getAttributeToken().getOffset() + fc.getAttributeToken().getLength()
                            + ( fc.getFilterToken() != null ? fc.getFilterToken().getLength() : 0 ) )
                    {
                        //String attributeType = fc.getAttributeToken().getValue();
                        String filterType = fc.getFilterToken() != null ? fc.getFilterToken().getValue() : ""; //$NON-NLS-1$
                        int filterTypeOffset = fc.getAttributeToken().getOffset() + fc.getAttributeToken().getLength();
                        addPossibleFilterTypes( proposalList, attributeType, filterType, filterTypeOffset );
                    }
                }

                // case A3: editing objectClass attribute: show matching object classes
                if ( attributeType != null && SchemaConstants.OBJECT_CLASS_AT.equalsIgnoreCase( attributeType ) )
                {
                    if ( ( fc.getValueToken() != null && fc.getValueToken().getOffset() <= offset || fc
                        .getFilterToken() != null )
                        && offset <= fc.getAttributeToken().getOffset() + fc.getAttributeToken().getLength()
                            + ( fc.getFilterToken() != null ? fc.getFilterToken().getLength() : 0 )
                            + ( fc.getValueToken() != null ? fc.getValueToken().getLength() : 0 ) )
                    {
                        addPossibleObjectClasses( proposalList, fc.getValueToken() == null ? "" : fc.getValueToken() //$NON-NLS-1$
                            .getValue(), fc.getValueToken() == null ? offset : fc.getValueToken().getOffset() );
                    }
                }
            }

            // case B: extensible filter
            if ( filter.getFilterComponent() instanceof LdapFilterExtensibleComponent )
            {
                LdapFilterExtensibleComponent fc = ( LdapFilterExtensibleComponent ) filter.getFilterComponent();

                // case B1: editing extensible attribute type: show matching attribute types
                if ( fc.getAttributeToken() != null && fc.getAttributeToken().getOffset() <= offset
                    && offset <= fc.getAttributeToken().getOffset() + fc.getAttributeToken().getLength() )
                {
                    addPossibleAttributeTypes( proposalList, fc.getAttributeToken().getValue(), fc.getAttributeToken()
                        .getOffset() );
                }

                // case B2: editing dn
                if ( fc.getDnAttrToken() != null && fc.getDnAttrToken().getOffset() <= offset
                    && offset <= fc.getDnAttrToken().getOffset() + fc.getDnAttrToken().getLength() )
                {
                    addDnAttr( proposalList, fc.getDnAttrToken().getValue(), fc.getDnAttrToken().getOffset() );
                }

                // case B3: editing matching rule
                if ( fc.getMatchingRuleColonToken() != null
                    && fc.getMatchingRuleToken() == null
                    && fc.getMatchingRuleColonToken().getOffset() <= offset
                    && offset <= fc.getMatchingRuleColonToken().getOffset()
                        + fc.getMatchingRuleColonToken().getLength() )
                {
                    if ( fc.getDnAttrColonToken() == null )
                    {
                        addDnAttr( proposalList, "", offset ); //$NON-NLS-1$
                    }
                    addPossibleMatchingRules( proposalList, "", offset, fc.getEqualsColonToken(), fc.getEqualsToken() ); //$NON-NLS-1$
                }
                if ( fc.getMatchingRuleToken() != null && fc.getMatchingRuleToken().getOffset() <= offset
                    && offset <= fc.getMatchingRuleToken().getOffset() + fc.getMatchingRuleToken().getLength() )
                {
                    if ( fc.getDnAttrColonToken() == null )
                    {
                        addDnAttr( proposalList, fc.getMatchingRuleToken().getValue(), fc.getMatchingRuleToken()
                            .getOffset() );
                    }

                    String matchingRuleValue = fc.getMatchingRuleToken().getValue();
                    addPossibleMatchingRules( proposalList, matchingRuleValue, fc.getMatchingRuleToken().getOffset(),
                        fc.getEqualsColonToken(), fc.getEqualsToken() );
                }
            }
        }

        return proposalList.toArray( new ICompletionProposal[0] );
    }


    // ── R2 HIGHLIGHTS ATTRIBUTE CORRIDORS ON THE HOLOGRAM ────────────────────
    // R2 scans the schematics index and illuminates every corridor whose label
    // starts with the prefix the pilot has already typed — giving them a
    // narrowed-down list of valid routes to the target.
    // We filter the pre-built attribute-type map by the typed prefix and add
    // a CompletionProposal for each match.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends all known attribute types whose name or OID starts with
     * {@code attributeType} (case-insensitive) to {@code proposalList}.
     * Each proposal includes the type's OID and human-readable name as the
     * display string, and the LDIF schema line as the additional info popup.
     *
     * @param proposalList   the list we are building; proposals are added here
     * @param attributeType  the prefix the user has typed so far — may be empty
     *                       to list all types
     * @param offset         document offset at which the replacement starts
     */
    private void addPossibleAttributeTypes( List<ICompletionProposal> proposalList, String attributeType, int offset )
    {
        if ( schema != null )
        {
            for ( String possibleAttributeType : possibleAttributeTypes.keySet() )
            {
                AttributeType description = possibleAttributeTypes.get( possibleAttributeType );
                if ( possibleAttributeType.toUpperCase().startsWith( attributeType.toUpperCase() ) )
                {
                    String replacementString = possibleAttributeType;
                    String displayString = possibleAttributeType;
                    if ( displayString.equals( description.getOid() ) )
                    {
                        displayString += " (" + SchemaUtils.toString( description ) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    else
                    {
                        displayString += " (" + description.getOid() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    String info = SchemaUtils.getLdifLine( description );
                    ICompletionProposal proposal = new CompletionProposal( replacementString, offset, attributeType
                        .length(), replacementString.length(), getAttributeTypeImage(), displayString, null, info );
                    proposalList.add( proposal );
                }
            }
        }
    }


    // ── R2 HIGHLIGHTS VALID ATTACK OPERATORS ─────────────────────────────────
    // Once a pilot has identified an attribute corridor, R2 highlights only the
    // attack operators that make sense for that corridor type — equality strike,
    // presence scan, range bombardment — filtering out options that don't apply.
    // We consult the attribute's matching rules in the schema and remove filter
    // operators that aren't supported.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends matching filter-operator proposals ({@code =}, {@code <=},
     * {@code >=}, {@code ~=}, {@code =*}) to {@code proposalList}. We first
     * prune operators that the given attribute's schema doesn't support — for
     * example, if there is no ordering matching rule we remove {@code <=} and
     * {@code >=}.
     *
     * @param proposalList   the list we are building
     * @param attributeType  the attribute type the user has already typed —
     *                       used to look up supported matching rules
     * @param filterType     the operator prefix the user has typed so far
     * @param offset         document offset at which the replacement starts
     */
    private void addPossibleFilterTypes( List<ICompletionProposal> proposalList, String attributeType,
        String filterType, int offset )
    {
        if ( schema != null )
        {
            Map<String, String> copy = new LinkedHashMap<String, String>( possibleFilterTypes );
            if ( SchemaUtils.getEqualityMatchingRuleNameOrNumericOidTransitive( schema
                .getAttributeTypeDescription( attributeType ), schema ) == null )
            {
                copy.remove( "=" ); //$NON-NLS-1$
                copy.remove( "~=" ); //$NON-NLS-1$
            }
            if ( SchemaUtils.getOrderingMatchingRuleNameOrNumericOidTransitive( schema
                .getAttributeTypeDescription( attributeType ), schema ) == null )
            {
                copy.remove( "<=" ); //$NON-NLS-1$
                copy.remove( ">=" ); //$NON-NLS-1$
            }

            for ( String possibleFilterType : copy.keySet() )
            {
                String replacementString = possibleFilterType;
                String displayString = copy.get( possibleFilterType );

                ICompletionProposal proposal = new CompletionProposal( replacementString, offset, filterType.length(),
                    possibleFilterType.length(), getFilterTypeImage(), displayString, null, null );
                proposalList.add( proposal );
            }
        }
    }


    // ── R2 HIGHLIGHTS KNOWN SPECIES ON THE HOLOGRAM ──────────────────────────
    // The Death Star schematics include a registry of all known alien species
    // docked in the station. R2 searches that registry for species names that
    // match the prefix the pilot typed and highlights them on the hologram.
    // We do the same for LDAP object classes: filter the map and add proposals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends object-class proposals matching the given prefix to
     * {@code proposalList}. This is triggered when the user is editing the
     * value part of an {@code objectClass=} filter — we show only the object
     * classes defined in the schema that start with the typed prefix.
     *
     * @param proposalList  the list we are building
     * @param objectClass   the prefix the user has typed so far
     * @param offset        document offset at which the replacement starts
     */
    private void addPossibleObjectClasses( List<ICompletionProposal> proposalList, String objectClass, int offset )
    {
        if ( schema != null )
        {
            for ( String possibleObjectClass : possibleObjectClasses.keySet() )
            {
                ObjectClass description = possibleObjectClasses.get( possibleObjectClass );
                if ( possibleObjectClass.toUpperCase().startsWith( objectClass.toUpperCase() ) )
                {
                    String replacementString = possibleObjectClass;
                    String displayString = possibleObjectClass;
                    if ( displayString.equals( description.getOid() ) )
                    {
                        displayString += " (" + SchemaUtils.toString( description ) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    else
                    {
                        displayString += " (" + description.getOid() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }

                    ICompletionProposal proposal = new CompletionProposal( replacementString, offset, objectClass
                        .length(), replacementString.length(), getObjectClassImage(), displayString, null, SchemaUtils
                        .getLdifLine( schema.getObjectClassDescription( possibleObjectClass ) ) );
                    proposalList.add( proposal );
                }
            }
        }
    }


    // ── R2 HIGHLIGHTS PRECISION TARGETING SYSTEMS ────────────────────────────
    // For the exhaust port shot, the schematics list all targeting computers
    // available. R2 highlights those whose name prefix matches what the pilot
    // typed, and auto-appends the colon-equals suffix if not yet present.
    // We build matching-rule proposals and conditionally append ":" and "=" to
    // make the filter syntactically complete.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends matching-rule proposals to {@code proposalList}. Used for
     * extensible filters ({@code attr:matchingRule:=value}). We auto-complete
     * the colon and equals-sign suffixes if they haven't been typed yet, so a
     * single proposal click produces a ready-to-use extensible filter operator.
     *
     * @param proposalList       the list we are building
     * @param matchingRule       the prefix the user has typed so far
     * @param offset             document offset at which the replacement starts
     * @param equalsColonToken   the token for the colon before {@code =};
     *                           {@code null} if not yet present in the filter
     * @param equalsToken        the token for the {@code =} sign;
     *                           {@code null} if not yet present
     */
    private void addPossibleMatchingRules( List<ICompletionProposal> proposalList, String matchingRule, int offset,
        LdapFilterToken equalsColonToken, LdapFilterToken equalsToken )
    {
        if ( schema != null )
        {
            for ( String possibleMatchingRule : possibleMatchingRules.keySet() )
            {
                if ( possibleMatchingRule.toUpperCase().startsWith( matchingRule.toUpperCase() ) )
                {
                    MatchingRule description = schema.getMatchingRuleDescription( possibleMatchingRule );
                    String replacementString = possibleMatchingRule;
                    if ( equalsColonToken == null )
                    {
                        replacementString += ":"; //$NON-NLS-1$
                    }
                    if ( equalsToken == null )
                    {
                        replacementString += "="; //$NON-NLS-1$
                    }
                    String displayString = possibleMatchingRule;
                    if ( displayString.equals( description.getOid() ) )
                    {
                        displayString += " (" + SchemaUtils.toString( description ) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    else
                    {
                        displayString += " (" + description.getOid() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    String info = SchemaUtils.getLdifLine( description );
                    ICompletionProposal proposal = new CompletionProposal( replacementString, offset, matchingRule
                        .length(), replacementString.length(), getMatchingRuleImage(), displayString, null, info );
                    proposalList.add( proposal );
                }
            }
        }
    }


    // ── R2 ADDS THE DN ROUTE TO THE PROPOSAL LIST ────────────────────────────
    // In the schematics, R2 recognises "dn:" as the special route through the
    // Death Star's DN corridor — he only adds it to the route list if the pilot
    // has typed a prefix that could still match "dn".
    // We add the "dn:" proposal when the user might be typing the dn:attr flag
    // of an extensible filter.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@code dn:} proposal if the given prefix could be a prefix of
     * {@code "dn"}. In LDAP extensible filters, {@code dn:} means "also match
     * the DN components"; we offer it here so users don't have to remember the
     * exact syntax.
     *
     * @param proposalList  the list we are building
     * @param dnAttr        the prefix the user has typed so far
     * @param offset        document offset at which the replacement starts
     */
    private void addDnAttr( List<ICompletionProposal> proposalList, String dnAttr, int offset )
    {
        if ( "dn".toUpperCase().startsWith( dnAttr.toUpperCase() ) ) //$NON-NLS-1$
        {
            String replacementString = "dn:"; //$NON-NLS-1$
            String displayString = "dn: ()"; //$NON-NLS-1$
            ICompletionProposal proposal = new CompletionProposal( replacementString, offset, dnAttr.length(),
                replacementString.length(), null, displayString, null, null );
            proposalList.add( proposal );
        }
    }


    // ── R2 FETCHES THE ATTRIBUTE TYPE ICON ───────────────────────────────────
    // Each section of the hologram has its own icon so pilots can distinguish
    // corridors from turrets at a glance. R2 fetches the attribute-type icon
    // from the shared plugin image registry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Image} used to represent an attribute-type proposal in
     * the completion popup. Loaded from the shared plugin image registry.
     *
     * @return  the attribute-type icon
     */
    private Image getAttributeTypeImage()
    {
        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_ATD );
    }


    // ── R2 FETCHES THE FILTER OPERATOR ICON ──────────────────────────────────
    // Filter operators get their own hologram symbol — a targeting reticle —
    // so the pilot can spot them instantly in the proposal list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Image} used to represent a filter-operator proposal
     * ({@code =}, {@code <=}, etc.) in the completion popup.
     *
     * @return  the filter-type icon
     */
    private Image getFilterTypeImage()
    {
        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_FILTER_EDITOR );
    }


    // ── R2 FETCHES THE OBJECT CLASS ICON ─────────────────────────────────────
    // Object classes appear as alien-species badges in the hologram — R2 pulls
    // the right badge image from the image registry so each proposal is easy
    // to recognise.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Image} used to represent an object-class proposal in
     * the completion popup.
     *
     * @return  the object-class icon
     */
    private Image getObjectClassImage()
    {
        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_OCD );
    }


    // ── R2 FETCHES THE MATCHING RULE ICON ────────────────────────────────────
    // Matching rules appear as precision-targeting symbols in the hologram.
    // R2 retrieves the icon from the shared plugin image registry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Image} used to represent a matching-rule proposal in
     * the completion popup.
     *
     * @return  the matching-rule icon
     */
    private Image getMatchingRuleImage()
    {
        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_MRD );
    }


    // ── R2 RETRIEVES THE SAVED ATTACK ROUTE TEMPLATES ────────────────────────
    // Before the briefing, the Rebellion stored a set of pre-approved attack
    // route templates in R2's memory. He retrieves them by context type so the
    // right templates appear for the right part of the hologram.
    // We retrieve Eclipse JFace filter templates from the plugin's template store.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse JFace {@link Template} objects that apply to the given
     * context type. These are the filter snippet templates the user can configure
     * via {@code Window > Preferences > LDAP Browser > Filter Templates}.
     *
     * @param contextTypeId  the template context type ID
     * @return               the matching templates from the plugin's store
     */
    protected Template[] getTemplates( String contextTypeId )
    {
        Template[] templates = BrowserCommonActivator.getDefault().getFilterTemplateStore().getTemplates(
            BrowserCommonConstants.FILTER_TEMPLATE_ID );
        return templates;
    }


    // ── R2 LOOKS UP THE CONTEXT TYPE FOR THE CURRENT REGION ──────────────────
    // Different parts of the Death Star hologram use different template
    // coordinate systems. R2 resolves which coordinate system applies to the
    // region the pilot is pointing at.
    // We look up the Eclipse template context type registered for the filter
    // editor so the right templates appear.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse {@link TemplateContextType} for the filter editor.
     * This tells the template engine which template variables and resolvers apply
     * to the current editing context.
     *
     * @param viewer  the text viewer (not used; kept for API symmetry)
     * @param region  the region being edited (not used)
     * @return        the filter-editor template context type
     */
    protected TemplateContextType getContextType( ITextViewer viewer, IRegion region )
    {
        TemplateContextType contextType = BrowserCommonActivator.getDefault().getFilterTemplateContextTypeRegistry()
            .getContextType( BrowserCommonConstants.FILTER_TEMPLATE_ID );
        return contextType;
    }


    // ── R2 RESOLVES THE TEMPLATE'S HOLOGRAM BADGE ────────────────────────────
    // Each saved template has a hologram badge so pilots can distinguish route
    // templates from live schema entries. R2 fetches the right badge image.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Image} to display next to a template proposal in the
     * completion popup. All filter templates share the same "template" icon.
     *
     * @param template  the template whose icon we need (the template itself is
     *                  not used — we always return the generic template icon)
     * @return          the template icon
     */
    protected Image getImage( Template template )
    {
        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_TEMPLATE );
    }


    // ── R2 FIELDS THE CONTEXT INFO REQUEST ───────────────────────────────────
    // Sometimes a pilot asks for more detailed context about a highlighted
    // section. R2 doesn't provide a separate context info panel for filter
    // editing — the hover tooltip covers that use case instead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns context-information popups for the current cursor position.
     * We don't use context information for the filter editor (the hover tooltip
     * handles that), so we always return {@code null}.
     *
     * @param viewer          the text viewer (not used)
     * @param documentOffset  the cursor position (not used)
     * @return                {@code null} always
     */
    public IContextInformation[] computeContextInformation( ITextViewer viewer, int documentOffset )
    {
        return null;
    }


    // ── R2 REPORTS NO AUTO-TRIGGER FOR CONTEXT INFO ───────────────────────────
    // R2 doesn't have any special characters that auto-pop the context-info
    // panel — that feature isn't wired up for filter editing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the characters that trigger the context-information popup
     * automatically. We don't use context information, so we always return
     * {@code null}.
     *
     * @return  {@code null} always
     */
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }


    // ── R2 REPORTS NO CURRENT ERROR IN THE PROPOSAL SYSTEM ───────────────────
    // R2 doesn't track a "last error" for the proposal system — if something
    // goes wrong, he just returns an empty list silently.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns an error message describing the last failure of the content-assist
     * computation, or {@code null} if there was no error. We don't track errors
     * internally, so we always return {@code null}.
     *
     * @return  {@code null} always
     */
    public String getErrorMessage()
    {
        return null;
    }


    // ── R2 REPORTS NO CONTEXT INFO VALIDATOR ─────────────────────────────────
    // Context-information validation isn't needed for filter editing, so R2
    // doesn't wire one up.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the validator for context-information popups. We don't use context
     * information, so we always return {@code null}.
     *
     * @return  {@code null} always
     */
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }

}
