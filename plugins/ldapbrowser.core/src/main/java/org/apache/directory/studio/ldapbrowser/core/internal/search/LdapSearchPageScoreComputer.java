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

package org.apache.directory.studio.ldapbrowser.core.internal.search;


import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.eclipse.search.ui.ISearchPageScoreComputer;


// ── CLASS: LdapSearchPageScoreComputer — C-3PO RANKS THE JAWA DIALECT MATCH ─
// C-3PO is fluent in over six million forms of communication.  When the Jawas
// hand him a transmission, he quickly assesses: "This message is in Jawa
// dialect — 90% probability it belongs in the LDAP search channel."  If the
// message is in some other alien tongue, he scores it 0 and ignores it.
// Eclipse's Search framework does the same thing: it calls all registered
// ISearchPageScoreComputer implementations and picks the search page with the
// highest confidence score for the current input object.  This implementation
// always scores 90 for our LDAP search page ID and 0 for everything else,
// telling Eclipse "yes, this input probably belongs in the LDAP search page."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse {@link ISearchPageScoreComputer} that tells the Search dialog how
 * likely the LDAP search page is a good fit for the current selection.
 * When a user opens the Eclipse Search dialog, Eclipse polls every registered
 * score computer; the highest scorer wins and becomes the default tab.  We
 * return 90 for our LDAP search page and 0 for all others, so when the
 * active view is LDAP-related the LDAP tab comes to the front.
 * Think of it as C-3PO flagging Jawa dialect transmissions as "high confidence
 * LDAP content."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapSearchPageScoreComputer implements ISearchPageScoreComputer
{

    /**
     * The Eclipse extension-point ID for our LDAP search page.
     * Mirrors {@link BrowserCoreConstants#LDAP_SEARCH_PAGE_ID} so callers
     * can reference it from this class without pulling in the constants class.
     */
    public static final String LDAP_SEARCH_PAGE_ID = BrowserCoreConstants.LDAP_SEARCH_PAGE_ID;


    // ── C-3PO Rates The Dialect Match ────────────────────────────────────────────
    // C-3PO reads the incoming message header: does it say "LDAP dialect"?
    // If yes, confidence is 90 — high, but not absolute.  If it's a different
    // protocol (Binary Large Object dialect, text dialect) C-3PO scores it 0
    // and lets the appropriate specialist handle it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a relevance score (0–100) indicating how appropriate the LDAP search
     * page is for the given {@code pageId} and {@code input}.
     * We return 90 when {@code pageId} matches our LDAP search page, signalling
     * Eclipse that the LDAP tab should be the default.  We return 0 for every other
     * page ID, leaving those pages to their own score computers.
     *
     * <p>For example — Eclipse polling at search-dialog open:</p>
     * <pre>
     *   int score = computer.computeScore(LDAP_SEARCH_PAGE_ID, selectedEntry);
     *   // → 90  (LDAP page should be selected)
     *
     *   int other = computer.computeScore("org.eclipse.jdt.ui.JavaSearchPage", selectedEntry);
     *   // → 0   (not our page, not our problem)
     * </pre>
     *
     * @param pageId the Eclipse search page ID being evaluated.
     * @param input  the current selection or active editor input (not used by us).
     * @return 90 if {@code pageId} equals {@link #LDAP_SEARCH_PAGE_ID}, 0 otherwise.
     */
    public int computeScore( String pageId, Object input )
    {
        if ( pageId.equals( LDAP_SEARCH_PAGE_ID ) )
        {
            return 90;
        }
        return 0;
    }

}
