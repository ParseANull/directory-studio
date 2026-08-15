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


import org.apache.directory.studio.ldapbrowser.core.model.filter.LdapFilter;
import org.apache.directory.studio.ldapbrowser.core.model.filter.parser.LdapFilterParser;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;


// ── CLASS: FilterCharacterPairMatcher — R2 LOCKS DOCKING CLAMPS ──────────────
// In Cloud City, R2-D2 scans the Millennium Falcon's docking port and precisely
// matches each docking clamp to its partner on the station — the left clamp
// locks to the right, the right to the left, and R2 records which side he
// engaged first so the crew knows which end to release.
// We do the same for parentheses: when the cursor sits on a '(' or ')', we
// find its matching partner in the filter tree and highlight the entire span
// between them, telling Eclipse which end (LEFT or RIGHT) the cursor is on.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements Eclipse's {@link ICharacterPairMatcher} for the LDAP filter editor.
 * When the user places their cursor next to a parenthesis, we highlight the
 * matching partner — i.e. the opening {@code (} lights up when you're at the
 * closing {@code )}, and vice versa.
 * Think of this class as R2-D2 locking docking clamps: every left bracket is
 * paired with its right counterpart and we track which side initiated the match.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterCharacterPairMatcher implements ICharacterPairMatcher
{

    /** The filter parser. */
    private LdapFilterParser parser;

    /** The anchor. */
    private int anchor;


    // ── R2 CONNECTS TO THE DOCKING CONTROL SYSTEM ────────────────────────────
    // As the Falcon drifts into Cloud City, R2 jacks into the docking bay's
    // control computer, loading the bay layout and resetting all clamp states
    // to their default "left anchor" position before the ship arrives.
    // We grab the parser and call clear() so we start in a known, clean state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code FilterCharacterPairMatcher} and resets it to a
     * clean initial state. We only need the parser — the source viewer
     * parameter is accepted for API compatibility but not stored.
     *
     * @param sourceViewer  the source viewer hosting the filter editor —
     *                      accepted for API symmetry but not used internally
     * @param parser        the live filter parser whose model we query to find
     *                      matching parenthesis offsets
     */
    public FilterCharacterPairMatcher( ISourceViewer sourceViewer, LdapFilterParser parser )
    {
        this.parser = parser;
        clear();
    }


    // ── R2 RELEASES ALL CLAMPS AND SHUTS DOWN ────────────────────────────────
    // When the Falcon departs, R2 orderly releases every active clamp and
    // powers down the docking control interface so no port is left locked open.
    // We honour the same contract: clean up any resources when Eclipse is done
    // with this matcher.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this matcher. In our case there is nothing
     * to release, but Eclipse requires us to implement this method as part of
     * the {@link ICharacterPairMatcher} contract.
     */
    public void dispose()
    {
    }


    // ── R2 RESETS THE CLAMP STATE TO DEFAULT ─────────────────────────────────
    // Between docking operations, R2 resets the clamp anchor back to its
    // default "left" position so each new match starts from a known baseline.
    // We reset the {@code anchor} field to LEFT so Eclipse knows the default
    // starting side for any new bracket-match operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets the anchor to {@link ICharacterPairMatcher#LEFT}. Eclipse calls
     * this before starting a fresh match — we make sure the matcher is in a
     * clean state so a stale anchor from a previous match can't bleed through.
     */
    public void clear()
    {
        anchor = LEFT;
    }


    // ── R2 FINDS THE MATCHING CLAMP ───────────────────────────────────────────
    // The Falcon is at position X. R2 checks his clamp map, finds the clamp
    // whose pin is at X, then identifies the partner clamp on the opposite side
    // and records which side he's standing at — left or right.
    // Here the "clamp" is a parenthesis: we ask the filter tree which filter
    // owns the character just before the cursor, then return a Region spanning
    // from the opening '(' to the closing ')'.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Finds the matching parenthesis for the character immediately before
     * {@code offset} and returns an {@link IRegion} spanning the pair.
     * Eclipse uses this to highlight both parentheses simultaneously. We set
     * {@link #anchor} to LEFT when the cursor is on the opener, RIGHT when
     * on the closer.
     *
     * <p>For example — R2 locks Cloud City's docking clamps:</p>
     * <pre>
     *   filter text = "(&(cn=Luke)(sn=Sky*))"
     *   cursor at offset 1 (just after '(')
     *   R2 checks clamp map → left=0, right=20
     *   anchor = LEFT, region = [0, 21)
     * </pre>
     *
     * @param document  the document containing the filter string
     * @param offset    the cursor position (we look at the character at
     *                  {@code offset - 1})
     * @return          a {@link Region} spanning from the opening to the closing
     *                  parenthesis, or {@code null} if there is no pair at this
     *                  position
     */
    public IRegion match( IDocument document, int offset )
    {
        LdapFilter model = parser.getModel();
        if ( model != null )
        {
            LdapFilter filter = parser.getModel().getFilter( offset - 1 );

            if ( filter != null && filter.getStartToken() != null && filter.getStopToken() != null )
            {

                int left = filter.getStartToken().getOffset();
                int right = filter.getStopToken().getOffset();

                if ( left == offset - 1 )
                {
                    anchor = LEFT;
                    IRegion region = new Region( left, right - left + 1 );
                    return region;
                }
                if ( right == offset - 1 )
                {
                    anchor = RIGHT;
                    IRegion region = new Region( left, right - left + 1 );
                    return region;
                }
            }
        }

        return null;
    }


    // ── R2 REPORTS WHICH CLAMP HE ENGAGED FIRST ──────────────────────────────
    // After locking the clamps, R2 signals back to the Falcon's crew whether
    // he started from the port side (left) or the starboard side (right).
    // Eclipse needs this so it knows which end of the highlighted region the
    // cursor is actually on, which affects how it draws the highlight.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the anchor side of the most recent match — either
     * {@link ICharacterPairMatcher#LEFT} (cursor was on the opening paren) or
     * {@link ICharacterPairMatcher#RIGHT} (cursor was on the closing paren).
     * Eclipse uses this to decide how to draw the bracket-match highlight.
     *
     * @return  {@link ICharacterPairMatcher#LEFT} or
     *          {@link ICharacterPairMatcher#RIGHT}
     */
    public int getAnchor()
    {
        return anchor;
    }

}
