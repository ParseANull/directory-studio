/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.widgets;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;


// ── CLASS: AttributesWidgetContentProposalProvider — C-3PO'S FULL SCHEMA LOOKUP
// C-3PO consults three sources to build his completion list for the attrs= field:
// first the two built-in keywords (entry, children), then the attribute types
// from the live schema, then all objectClasses (each offered twice — once with
// "@" and once with "!"). The list is sorted: keywords first, attribute types
// second, objectClasses last, and alphabetically within each group. When the
// user types, getProposals() extracts the current token (the part after the
// last comma or space) and returns every entry whose label starts with that
// prefix (case-insensitive). The auto-activation characters are rebuilt every
// time the proposal list changes so the dropdown fires on any character that
// could start a completion.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An {@link IContentProposalProvider} for the ACL attributes widget text field.
 * Builds a sorted proposal list from:
 * <ol>
 *   <li>Keywords: {@code entry}, {@code children}</li>
 *   <li>Attribute types from the connected schema</li>
 *   <li>ObjectClasses from the connected schema (prefixed with {@code @} and {@code !})</li>
 * </ol>
 * The current token is extracted by scanning backwards from the cursor to the
 * last comma or whitespace, and only proposals that start with the current token
 * (case-insensitive) are returned.
 *
 * <p>Think of this class as C-3PO's full schema lookup service — he knows every
 * attribute type and objectClass in the Empire's directory schema and filters the
 * list down to what the officer is likely typing.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributesWidgetContentProposalProvider implements IContentProposalProvider
{
    /** The content proposal adapter */
    private ContentProposalAdapter proposalAdapter;

    /** The browser connection */
    private IBrowserConnection browserConnection;

    /** The proposals */
    private List<AttributesWidgetContentProposal> proposals;


    // ── Constructing the Provider and Building the Initial Proposal List ───────
    // C-3PO powers up his schema database immediately on construction and builds
    // the initial proposal list (keywords only, since no connection is set yet).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new proposal provider. Immediately builds the initial proposal
     * list (keywords only, since no connection is available yet).
     */
    public AttributesWidgetContentProposalProvider()
    {
        // Initializing the proposals list
        proposals = new ArrayList<AttributesWidgetContentProposal>();

        // Building the proposals list
        buildProposals();
    }


    // ── Filtering Proposals by the Current Token ──────────────────────────────
    // C-3PO extracts whatever the officer has typed since the last comma or
    // space, then filters the full list down to entries that start with that
    // prefix. He also records how many characters were already typed so each
    // proposal knows what suffix to insert.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the subset of proposals whose label starts with the current token
     * (the text from the last comma or whitespace up to the cursor). Case-insensitive.
     * Also sets {@link AttributesWidgetContentProposal#setStartPosition(int)} on
     * each matching proposal so it knows how many leading characters to skip when
     * inserted.
     *
     * <p>For example — C-3PO filtering completions after the user typed "uid":</p>
     * <pre>
     *   proposals = provider.getProposals("uid,cn,", 7); // "cn," typed after "uid,"
     *   // returns proposals starting with "cn" (case-insensitive)
     * </pre>
     *
     * @param contents  The full text field contents.
     * @param position  The current cursor position.
     * @return          An array of matching proposals; never {@code null}.
     */
    public IContentProposal[] getProposals( String contents, int position )
    {
        String value = getCurrentValue( contents, position );

        List<AttributesWidgetContentProposal> matchingProposals = new ArrayList<AttributesWidgetContentProposal>();
        for ( AttributesWidgetContentProposal proposal : proposals )
        {
            if ( proposal.getLabel().toUpperCase().startsWith( value.toUpperCase() ) )
            {
                matchingProposals.add( proposal );
                proposal.setStartPosition( value.length() );
            }
        }

        return matchingProposals.toArray( new AttributesWidgetContentProposal[0] );
    }


    // ── Returning Auto-Activation Characters (Empty by Default) ───────────────
    // The actual auto-activation characters are set on the ContentProposalAdapter
    // after the proposals are built. This method returns an empty array by default.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns an empty char array — auto-activation characters are set directly
     * on the {@link ContentProposalAdapter} via {@link #setAutoActivationChars()}
     * after the proposal list is built.
     *
     * {@inheritDoc}
     *
     * @return  An empty {@code char[]} array.
     */
    public char[] getAutoActivationCharacters()
    {
        return new char[0];
    }


    // ── Extracting the Current Token Before the Cursor ────────────────────────
    // C-3PO scans backwards from the cursor to find the start of the current
    // word (the last comma or whitespace) and extracts the partial input.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the current word from the text field by scanning backwards from
     * {@code position} to the last comma or whitespace character.
     *
     * <p>For example — extracting the current token:</p>
     * <pre>
     *   getCurrentValue("entry,uid,cn", 12); // → "cn"
     *   getCurrentValue("uid,", 4);          // → ""
     * </pre>
     *
     * @param contents  The full text field contents.
     * @param position  The cursor position.
     * @return          The current partial token being typed; never {@code null}.
     */
    private String getCurrentValue( String contents, int position )
    {
        int start = 0;

        for ( int i = position - 1; i >= 0; i-- )
        {
            char c = contents.charAt( i );
            if ( c == ',' || Character.isWhitespace( c ) )
            {
                start = i + 1;
                break;
            }
        }

        return contents.substring( start, position );
    }


    // ── Rebuilding the Proposal List From Scratch ─────────────────────────────
    // Called after a connection change or on construction. C-3PO clears the old
    // list, adds keywords, adds schema proposals, sorts everything, and refreshes
    // the auto-activation characters on the adapter.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Rebuilds the entire proposal list: clears the previous list, adds keyword
     * proposals (entry, children), adds schema-derived proposals (attribute types
     * and objectClasses), sorts the combined list, and updates auto-activation
     * characters on the adapter.
     */
    private void buildProposals()
    {
        // Reseting previous proposals
        proposals.clear();

        // Adding proposals
        addKeywordProposals();
        addConnectionProposals();

        // Sorting the proposals
        sortProposals();

        // Setting auto-activation characters
        setAutoActivationChars();
    }


    // ── Refreshing Auto-Activation Characters on the Adapter ─────────────────
    // C-3PO collects every character that appears in any proposal label and
    // registers them (plus ',') with the adapter so the dropdown fires eagerly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Computes the union of all characters appearing in proposal labels (both
     * lower-case and upper-case) and sets them as auto-activation characters on
     * the {@link ContentProposalAdapter}, along with the comma character {@code ','}.
     * Has no effect if no adapter has been set yet.
     */
    private void setAutoActivationChars()
    {
        if ( proposalAdapter != null )
        {
            Set<Character> characterSet = new HashSet<Character>();
            for ( IContentProposal proposal : proposals )
            {
                String string = proposal.getLabel();
                for ( int k = 0; k < string.length(); k++ )
                {
                    char ch = string.charAt( k );
                    characterSet.add( Character.toLowerCase( ch ) );
                    characterSet.add( Character.toUpperCase( ch ) );
                }
            }

            char[] autoActivationCharacters = new char[characterSet.size() + 1];
            autoActivationCharacters[0] = ',';
            int i = 1;
            for ( Iterator<Character> it = characterSet.iterator(); it.hasNext(); )
            {
                Character ch = it.next();
                autoActivationCharacters[i] = ch.charValue();
                i++;
            }

            proposalAdapter.setAutoActivationCharacters( autoActivationCharacters );
        }
    }


    // ── Adding Fixed Keyword Proposals ────────────────────────────────────────
    // The two special tokens "entry" and "children" are always available, even
    // without a live connection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds fixed keyword proposals ({@code "entry"} and {@code "children"}) to
     * the proposal list. These are always available regardless of connection state.
     */
    private void addKeywordProposals()
    {
        proposals.add( new KeywordContentProposal( "entry" ) );
        proposals.add( new KeywordContentProposal( "children" ) );
    }


    // ── Adding Schema-Derived Proposals From the Live Connection ─────────────
    // C-3PO queries the live schema for all attribute type names and all
    // objectClass names, wrapping each in the appropriate proposal subclass.
    // ObjectClasses are added twice: with "@" and with "!" prefixes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds attribute type and objectClass proposals from the connected schema.
     * Each attribute type name becomes an {@link AttributeTypeContentProposal}.
     * Each objectClass name is added twice as an {@link ObjectClassContentProposal}:
     * once with the {@code @} prefix and once with the {@code !} prefix.
     * Has no effect when {@link #browserConnection} is {@code null}.
     */
    private void addConnectionProposals()
    {
        if ( browserConnection != null )
        {
            // Attribute types
            Collection<String> atNames = SchemaUtils.getNames( browserConnection.getSchema().getAttributeTypeDescriptions() );
            for ( String atName : atNames )
            {
                proposals.add( new AttributeTypeContentProposal( atName ) );
            }

            // Object classes
            Collection<String> ocNames = SchemaUtils.getNames( browserConnection.getSchema().getObjectClassDescriptions() );
            for ( String ocName : ocNames )
            {
                proposals.add( new ObjectClassContentProposal( "@" + ocName ) );
                proposals.add( new ObjectClassContentProposal( "!" + ocName ) );
            }
        }
    }


    // ── Sorting the Proposals in Priority Order ───────────────────────────────
    // C-3PO sorts the combined list: keywords first, then attribute types, then
    // objectClasses, and alphabetically within each group.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sorts the proposal list so that keywords appear first, then attribute types,
     * then objectClasses. Within each group proposals are sorted alphabetically
     * (case-insensitive).
     */
    private void sortProposals()
    {
        Comparator<? super AttributesWidgetContentProposal> comparator = new Comparator<AttributesWidgetContentProposal>()
        {
            public int compare( AttributesWidgetContentProposal o1, AttributesWidgetContentProposal o2 )
            {
                if ( ( o1 instanceof KeywordContentProposal ) && !( o2 instanceof KeywordContentProposal ) )
                {
                    return -2;
                }
                else if ( !( o1 instanceof KeywordContentProposal ) && ( o2 instanceof KeywordContentProposal ) )
                {
                    return 2;
                }

                else if ( ( o1 instanceof AttributeTypeContentProposal )
                    && !( o2 instanceof AttributeTypeContentProposal ) )
                {
                    return -3;
                }
                else if ( !( o1 instanceof AttributeTypeContentProposal )
                    && ( o2 instanceof AttributeTypeContentProposal ) )
                {
                    return 3;
                }

                else if ( ( o1 instanceof ObjectClassContentProposal )
                    && !( o2 instanceof ObjectClassContentProposal ) )
                {
                    return -3;
                }
                else if ( !( o1 instanceof ObjectClassContentProposal )
                    && ( o2 instanceof ObjectClassContentProposal ) )
                {
                    return 3;
                }

                return o1.getLabel().compareToIgnoreCase( o2.getLabel() );
            }
        };
        Collections.sort( proposals, comparator );
    }


    // ── Setting the Browser Connection and Rebuilding Proposals ──────────────
    // When the attributes widget learns which LDAP connection is being used, it
    // calls this method to refresh the proposal list with live schema data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP browser connection and rebuilds the proposal list to include
     * attribute types and objectClasses from the new connection's schema. Also
     * refreshes auto-activation characters.
     *
     * @param browserConnection  The new LDAP browser connection; may be {@code null}
     *                           (in which case only keyword proposals remain).
     */
    public void setBrowserConnection( IBrowserConnection browserConnection )
    {
        this.browserConnection = browserConnection;

        // Re-building proposals
        buildProposals();
    }


    // ── Registering the ContentProposalAdapter ────────────────────────────────
    // The attributes widget registers the adapter so we can push updated
    // auto-activation characters to it whenever the proposal list changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the {@link ContentProposalAdapter} so that
     * {@link #setAutoActivationChars()} can update its auto-activation character
     * set when the proposal list is rebuilt.
     *
     * @param proposalAdapter  The adapter to register.
     */
    public void setProposalAdapter( ContentProposalAdapter proposalAdapter )
    {
        this.proposalAdapter = proposalAdapter;
    }
}
