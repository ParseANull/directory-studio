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
package org.apache.directory.studio.ldapbrowser.common.widgets;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;


// ── CLASS: ListContentProposalProvider — R2-D2 PROJECTING LEIA'S HOLOGRAM ────
// R2 stores Leia's complete message in memory and, when asked, projects only the parts
// that match what Obi-Wan has already heard — filtering the full recording to what's relevant.
// ListContentProposalProvider keeps a list of all possible completions and, on each
// keystroke, filters it down to just the entries that start with what the user has typed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An {@link IContentProposalProvider} that filters a dynamic list of Strings into
 * autocomplete proposals based on the user's current input prefix.
 * Used throughout Directory Studio to power the Ctrl+Space autocomplete on LDAP
 * attribute-type combos — the list of proposals is updated externally via
 * {@link #setProposals(List)} whenever the schema changes.
 * Think of R2-D2: he carries the full message in memory and projects only the relevant
 * fragment that matches the current playback position.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ListContentProposalProvider implements IContentProposalProvider
{

    /** The dynamic list of proposals */
    private List<String> proposals;


    // ── R2 LOADS A LIST-FORMAT MESSAGE ────────────────────────────────────────────
    // R2 is handed a pre-built list of holographic frames to store — no conversion needed.
    // He stashes them in memory ready to play back on demand.
    // We delegate to setProposals() to copy and store the list safely.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a ListContentProposalProvider pre-loaded with the given list of proposal strings.
     * The list is copied internally so later changes to the caller's list don't affect us.
     *
     * <p>For example — R2 stores the list-format message:</p>
     * <pre>
     *   proposals = List.of("cn", "sn", "uid", "mail");
     *   provider  = new ListContentProposalProvider(proposals);
     * </pre>
     *
     * @param proposals  The initial list of candidate strings for autocomplete; may be {@code null}
     *                   (treated as empty list).
     */
    public ListContentProposalProvider( List<String> proposals )
    {
        setProposals( proposals );
    }


    // ── R2 LOADS AN ARRAY-FORMAT MESSAGE ──────────────────────────────────────────
    // R2 is handed an array of frames instead — he converts it to a list and stashes it.
    // Same storage, different input format for the caller's convenience.
    // We wrap the array in a List and delegate to setProposals().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a ListContentProposalProvider pre-loaded with the given array of proposal strings.
     * Convenience overload that converts the array to a list internally.
     *
     * <p>For example — R2 loads an array of frames:</p>
     * <pre>
     *   String[] attrs = {"cn", "sn", "uid", "mail"};
     *   provider = new ListContentProposalProvider(attrs);
     * </pre>
     *
     * @param proposals  The initial array of candidate strings for autocomplete; must not be {@code null}.
     */
    public ListContentProposalProvider( String[] proposals )
    {
        setProposals( new ArrayList<String>( Arrays.asList( proposals ) ) );
    }


    // ── R2 PLAYS BACK THE MATCHING FRAMES OF THE HOLOGRAM ────────────────────────
    // Obi-Wan has heard "Help me, Obi-Wan" so far — R2 fast-forwards through the recording
    // and projects only the frames that start with those words, sorted alphabetically.
    // We filter our proposals list to those whose prefix matches the typed input (case-insensitive).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an array of autocomplete proposals that start with the text the user has
     * typed up to {@code position}.
     * The matching is case-insensitive and prefix-only. If the typed string is empty,
     * no proposals are returned (we don't flood the popup with the full list).
     * Results are sorted alphabetically before filtering.
     *
     * <p>For example — R2 projects the matching frames:</p>
     * <pre>
     *   contents  = "cn"  (user typed "cn")
     *   position  = 2
     *   proposals = ["cn", "commonName"]  // only entries starting with "cn"
     * </pre>
     *
     * @param contents  The full current text of the input field.
     * @param position  The cursor position within {@code contents}; proposals match the prefix
     *                  {@code contents.substring(0, position)}.
     * @return          An array of {@link IContentProposal} whose content starts with the typed prefix;
     *                  empty array if the prefix is empty or nothing matches.
     */
    public IContentProposal[] getProposals( String contents, int position )
    {
        String string = contents.substring( 0, position );

        Collections.sort( proposals );

        List<IContentProposal> proposalList = new ArrayList<IContentProposal>();
        for ( int k = 0; k < proposals.size(); k++ )
        {
            final String proposal = proposals.get( k );
            if ( proposal.toUpperCase().startsWith( string.toUpperCase() ) && !"".equals( string ) ) //$NON-NLS-1$
            {
                IContentProposal p = new IContentProposal()
                {
                    public String getContent()
                    {
                        return proposal;
                    }


                    public String getDescription()
                    {
                        return proposal;
                    }


                    public String getLabel()
                    {
                        return proposal;
                    }


                    public int getCursorPosition()
                    {
                        return proposal.length();
                    }
                };
                proposalList.add( p );
            }
        }
        return proposalList.toArray( new IContentProposal[proposalList.size()] );
    }


    // ── R2 SWAPS IN A NEW RECORDING ────────────────────────────────────────────────
    // The mission controller swaps out R2's memory chip with an updated list of frames.
    // R2 discards the old recording and loads the new one, copying it so the original is safe.
    // We replace our internal list with a defensive copy of newProposals (or an empty list if null).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the current list of autocomplete proposals with a new one.
     * The new list is copied defensively — changes to the caller's list after this call
     * won't affect what the provider shows. A {@code null} argument is treated as an
     * empty list so callers don't need to null-check before calling.
     *
     * <p>For example — R2 swaps his memory chip:</p>
     * <pre>
     *   provider.setProposals(updatedAttributeNames);
     *   // Provider now offers the new schema's attribute names
     * </pre>
     *
     * @param newProposals  The new candidate strings to use for autocomplete; {@code null} clears the list.
     */
    public void setProposals( List<String> newProposals )
    {
        if ( newProposals == null )
        {
            this.proposals = new ArrayList<String>();
        }
        else
        {
            this.proposals = new ArrayList<String>( newProposals );
        }
    }

}
