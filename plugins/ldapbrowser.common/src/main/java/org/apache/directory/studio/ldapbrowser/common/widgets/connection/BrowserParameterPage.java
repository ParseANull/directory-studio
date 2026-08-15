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

package org.apache.directory.studio.ldapbrowser.common.widgets.connection;


import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.ldap.model.url.LdapUrl.Extension;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.ui.AbstractConnectionParameterPage;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.AliasesDereferencingWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.LimitWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.ReferralsHandlingWidget;
import org.apache.directory.studio.ldapbrowser.core.jobs.FetchBaseDNsRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.impl.BrowserConnection;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// ── CLASS: BrowserParameterPage — Han Punches In Coordinates At Mos Eisley ──────────────
// Han Solo is at the Falcon's nav console just before departure from Mos Eisley.
// He's entering every jump parameter: starting point, search limits, how to handle
// nav shortcuts (aliases), whether to chase pointer entries (referrals), and special
// controls like ManageDsaIT and paged search — every knob that shapes how the Falcon
// (our browser) traverses the LDAP galaxy.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * UI parameter page for configuring how the LDAP browser explores a connection.
 * This covers the base DN (where to start browsing), result limits, alias dereferencing,
 * referral handling, and advanced controls like paged search and operational attribute fetching.
 * Think of this class as Han Solo's nav console — every field here is a coordinate or
 * a flight parameter that determines how far and how fast the Falcon searches the directory.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserParameterPage extends AbstractConnectionParameterPage
{

    private static final String X_BASE_DN = "X-BASE-Dn"; //$NON-NLS-1$

    private static final String X_COUNT_LIMIT = "X-COUNT-LIMIT"; //$NON-NLS-1$

    private static final String X_TIME_LIMIT = "X-TIME-LIMIT"; //$NON-NLS-1$

    private static final String X_ALIAS_HANDLING = "X-ALIAS-HANDLING"; //$NON-NLS-1$

    private static final String X_ALIAS_HANDLING_FINDING = "FINDING"; //$NON-NLS-1$

    private static final String X_ALIAS_HANDLING_SEARCHING = "SEARCHING"; //$NON-NLS-1$

    private static final String X_ALIAS_HANDLING_NEVER = "NEVER"; //$NON-NLS-1$

    private static final String X_REFERRAL_HANDLING = "X-REFERRAL-HANDLING"; //$NON-NLS-1$

    private static final String X_REFERRAL_HANDLING_IGNORE = "IGNORE"; //$NON-NLS-1$

    private static final String X_REFERRAL_HANDLING_FOLLOW = "FOLLOW"; //$NON-NLS-1$

    private static final String X_MANAGE_DSA_IT = "X-MANAGE-DSA-IT"; //$NON-NLS-1$

    private static final String X_FETCH_SUBENTRIES = "X-FETCH-SUBENTRIES"; //$NON-NLS-1$

    private static final String X_FETCH_OPERATIONAL_ATTRIBUTES = "X-FETCH-OPERATIONAL-ATTRIBUTES"; //$NON-NLS-1$

    private static final String X_PAGED_SEARCH = "X-PAGED-SEARCH"; //$NON-NLS-1$

    private static final String X_PAGED_SEARCH_SIZE = "X-PAGED-SEARCH-SIZE"; //$NON-NLS-1$

    private static final String X_PAGED_SEARCH_SCROLL_MODE = "X-PAGED-SEARCH-SCROLL-MODE"; //$NON-NLS-1$

    /** The checkbox to fetch the base Dn's from namingContexts whenever opening the connection */
    private Button autoFetchBaseDnsButton;

    /** The button to fetch the base Dn's from namingContexts attribute */
    private Button fetchBaseDnsButton;

    /** The combo that displays the fetched base Dn's */
    private Combo baseDNCombo;

    /** The widget with the count and time limits */
    private LimitWidget limitWidget;

    /** The widget to select the alias dereferencing method */
    private AliasesDereferencingWidget aliasesDereferencingWidget;

    /** The widget to select the referrals handling method */
    private ReferralsHandlingWidget referralsHandlingWidget;

    /** The ManageDsaIT control button. */
    private Button manageDsaItButton;

    /** The fetch subentries button. */
    private Button fetchSubentriesButton;

    /** The paged search button. */
    private Button pagedSearchButton;

    /** The paged search size label. */
    private Label pagedSearchSizeLabel;

    /** The paged search size text. */
    private Text pagedSearchSizeText;

    /** The paged search scroll mode button. */
    private Button pagedSearchScrollModeButton;

    /** The fetch operational attributes button. */
    private Button fetchOperationalAttributesButton;


    // ── Han Approaches The Nav Console ────────────────────────────────────────────────────
    // Han strides onto the bridge of the Falcon and settles into the pilot's seat.
    // Nothing is entered yet — the console is blank, waiting for him to start punching in coords.
    // This constructor creates a fresh page with no state; the wizard framework populates it later.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty BrowserParameterPage ready to be populated by the connection wizard.
     * The Eclipse wizard framework calls this via extension-point reflection, so we keep the
     * constructor argument-free.
     */
    public BrowserParameterPage()
    {
    }


    // ── Does Han Have Auto-Pilot Locked On? ───────────────────────────────────────────────
    // Before Han punches in manual coordinates, he checks whether the Falcon's auto-nav
    // is engaged — if it is, he lets it figure out the starting point on its own from
    // the Root DSE entry rather than typing in a base DN by hand.
    // This method reads that auto-pilot toggle so the rest of the page can respond accordingly.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Tells us whether the "auto-fetch base DNs" checkbox is ticked.
     * When it is, the browser queries the server's Root DSE to discover base DNs automatically
     * instead of using the manually entered value — Han trusts the auto-nav.
     *
     * <p>For example — Han checks the toggle:</p>
     * <pre>
     *   if ( autoFetchBaseDnsButton.getSelection() ) {
     *       // Falcon queries Root DSE on connect
     *   }
     * </pre>
     *
     * @return {@code true} if the auto-fetch checkbox is selected
     */
    private boolean isAutoFetchBaseDns()
    {
        return autoFetchBaseDnsButton.getSelection();
    }


    // ── Han Reads The Starting Waypoint ───────────────────────────────────────────────────
    // The base DN (Distinguished Name) is like the jump origin in the nav computer —
    // it tells the browser where in the LDAP tree to start exploring.
    // Han reads the waypoint from the combo box, but only if auto-nav is off.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the base DN typed in the combo, or {@code null} if auto-fetch is enabled.
     * The base DN is the root of the subtree the browser will display — think of it as
     * the entry point coordinate in the LDAP directory hierarchy.
     *
     * <p>For example — Han reads his origin:</p>
     * <pre>
     *   String origin = isAutoFetchBaseDns() ? null : baseDNCombo.getText();
     *   // null means "let the server tell us where to start"
     * </pre>
     *
     * @return the base DN string, or {@code null} if the server should supply it automatically
     */
    private String getBaseDN()
    {
        return isAutoFetchBaseDns() ? null : baseDNCombo.getText();
    }


    // ── Han Sets The Search Entry Limit ───────────────────────────────────────────────────
    // Han doesn't want the nav computer scanning every single entry in the galaxy — that
    // would take forever and blow the power cells.  The count limit caps how many LDAP
    // entries the server may return in a single search response.
    // This method pulls that cap from the limit widget.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of LDAP entries the server should return per search.
     * Zero means "no limit" — use that carefully on large directories.
     * This value feeds directly into the LDAP search request's sizeLimit parameter.
     *
     * <p>For example — Han caps his scan:</p>
     * <pre>
     *   int maxEntries = limitWidget.getCountLimit(); // e.g. 1000
     *   // Server will stop sending after 1000 entries
     * </pre>
     *
     * @return the count limit (0 = unlimited)
     */
    private int getCountLimit()
    {
        return limitWidget.getCountLimit();
    }


    // ── Han Checks The Jump Timer ─────────────────────────────────────────────────────────
    // Alongside the entry count, Han sets a time budget — if the server hasn't responded
    // within that many seconds, abort the search.  Nobody wants the Falcon sitting in
    // hyperspace waiting for an answer that never comes.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum time (in seconds) the server is allowed to spend on a search before
     * cutting it off.  Zero means "wait as long as it takes."
     * This maps to the LDAP search request's timeLimit parameter.
     *
     * <p>For example — Han watches the clock:</p>
     * <pre>
     *   int seconds = limitWidget.getTimeLimit(); // e.g. 30
     *   // Server aborts the search after 30 s
     * </pre>
     *
     * @return the time limit in seconds (0 = unlimited)
     */
    private int getTimeLimit()
    {
        return limitWidget.getTimeLimit();
    }


    // ── Han Configures Alias Shortcuts ────────────────────────────────────────────────────
    // Some LDAP entries are aliases — shortcut pointers to entries that live elsewhere in
    // the tree, like a hyperspace relay beacon.  Han gets to decide whether to follow those
    // shortcuts while finding entries, while searching, never, or always.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the selected alias-dereferencing method.
     * LDAP aliases are shortcut entries that point to other entries.  This setting controls
     * whether those shortcuts are silently resolved (followed) during browsing or left as-is.
     *
     * <p>For example — Han decides on relay beacons:</p>
     * <pre>
     *   ALWAYS   - always follow shortcuts
     *   FINDING  - only follow them when locating the base entry
     *   SEARCH   - only follow them during the search scan
     *   NEVER    - treat alias entries as plain entries, never dereference
     * </pre>
     *
     * @return the chosen {@link Connection.AliasDereferencingMethod}
     */
    private Connection.AliasDereferencingMethod getAliasesDereferencingMethod()
    {
        return aliasesDereferencingWidget.getAliasesDereferencingMethod();
    }


    // ── Han Decides Whether To Chase Pointer Beacons ─────────────────────────────────────
    // LDAP referrals are entries that say "what you're looking for is over there on another
    // server" — like a beacon redirecting the Falcon to a different star system.
    // Han can follow those redirects automatically, manually, or ignore them entirely.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the selected referral-handling method.
     * When an LDAP server returns a referral, it's pointing us to a different server or subtree.
     * This setting decides how we handle that: follow automatically, prompt the user, or ignore.
     *
     * <p>For example — Han handles a redirect beacon:</p>
     * <pre>
     *   FOLLOW          - Falcon jumps to the other server automatically
     *   FOLLOW_MANUALLY - Han decides each time whether to chase the beacon
     *   IGNORE          - treat the referral entry like any other — don't chase it
     * </pre>
     *
     * @return the chosen {@link Connection.ReferralHandlingMethod}
     */
    private Connection.ReferralHandlingMethod getReferralsHandlingMethod()
    {
        return referralsHandlingWidget.getReferralsHandlingMethod();
    }


    // ── Han Engages The DSA Management Override ───────────────────────────────────────────
    // The ManageDsaIT LDAP control is a special flag that tells the server "treat referral
    // and alias entries as ordinary entries — don't follow them, just show me the raw object."
    // Han toggles this when he needs to inspect the beacon hardware itself, not follow where it points.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the ManageDsaIT LDAP control should be sent with every search.
     * When active, the server treats referral and DSA entries as normal objects instead of
    * redirects — useful for administering the directory topology itself.
     *
     * <p>For example — Han inspects a beacon directly:</p>
     * <pre>
     *   manageDsaItButton.getSelection() == true
     *   // Server returns the referral entry itself instead of following the pointer
     * </pre>
     *
     * @return {@code true} if ManageDsaIT is enabled
     */
    private boolean manageDsaIT()
    {
        return manageDsaItButton.getSelection();
    }


    // ── Han Scans For Hidden Subspace Entries ─────────────────────────────────────────────
    // Subentries in LDAP are special administrative entries (like collective attribute
    // subentries) that are normally invisible — like hidden compartments in the Falcon.
    // This flag tells the browser to fetch those hidden entries too.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the browser should also fetch LDAP subentries during browsing.
     * Subentries (RFC 3672) are administrative entries not returned by ordinary searches.
     * Enabling this makes them visible in the tree view — Han opening every hidden compartment.
     *
     * <p>For example — Han enables compartment scanning:</p>
     * <pre>
     *   fetchSubentriesButton.getSelection() == true
     *   // Subentries appear alongside normal entries in the browser tree
     * </pre>
     *
     * @return {@code true} if subentry fetching is enabled
     */
    private boolean isFetchSubentries()
    {
        return fetchSubentriesButton.getSelection();
    }


    // ── Han Pulls The Full Sensor Readout ─────────────────────────────────────────────────
    // Operational attributes (like createTimestamp, modifyTimestamp, entryDN) are attributes
    // maintained by the server itself — the Falcon's internal diagnostics, not the cargo manifest.
    // This flag tells the browser to request those hidden diagnostics alongside normal attributes.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether operational attributes should be fetched alongside user attributes.
     * Operational attributes (e.g. {@code createTimestamp}, {@code entryDN}) are maintained
     * by the server and not returned by default — we have to ask explicitly with a "+" in
     * the attribute list.  Han requesting full diagnostics from the ship computer.
     *
     * <p>For example — Han asks for everything:</p>
     * <pre>
     *   fetchOperationalAttributesButton.getSelection() == true
     *   // Search requests include "+" to retrieve operational attrs
     * </pre>
     *
     * @return {@code true} if operational attributes should be fetched
     */
    private boolean isFetchOperationalAttributes()
    {
        return fetchOperationalAttributesButton.getSelection();
    }


    // ── Han Engages Paged Hyperdrive ──────────────────────────────────────────────────────
    // Instead of getting all search results in one massive data dump, paged search breaks the
    // results into chunks — like receiving the galaxy map sector by sector rather than all at once.
    // This is essential for large directories; it keeps the UI responsive.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether paged search (LDAP Simple Paged Results control, RFC 2696) is enabled.
     * When on, searches return results in pages of a configurable size rather than all at once.
     * This is critical for large LDAP directories where a single search might return millions
     * of entries — think of it as Han downloading the galaxy map one sector at a time.
     *
     * <p>For example — Han enables sector-by-sector download:</p>
     * <pre>
     *   pagedSearchButton.getSelection() == true
     *   // Each search round-trip returns at most pagedSearchSize entries
     * </pre>
     *
     * @return {@code true} if paged search is enabled
     */
    private boolean isPagedSearch()
    {
        return pagedSearchButton.getSelection();
    }


    // ── Han Sets The Sector Chunk Size ────────────────────────────────────────────────────
    // When paged search is on, this is how many entries the server sends per page.
    // Han telling R2-D2 to download the map 100 sectors at a time — small enough not to
    // crash the nav computer, large enough to make progress.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of entries per page when paged search is active.
     * If the text field contains a non-numeric value for some reason, we fall back to 100
     * as a sensible default — Han trusts the Falcon's fallback calibration.
     *
     * <p>For example — Han configures chunk size:</p>
     * <pre>
     *   pageSize = Integer.valueOf( pagedSearchSizeText.getText() ); // e.g. 100
     *   // Server sends 100 entries per round trip
     * </pre>
     *
     * @return the page size (entries per page); defaults to 100 on parse errors
     */
    private int getPagedSearchSize()
    {
        int pageSize;
        try
        {
            pageSize = Integer.valueOf( pagedSearchSizeText.getText() );
        }
        catch ( NumberFormatException e )
        {
            pageSize = 100;
        }

        return pageSize;
    }


    // ── Han Enables Scroll Mode Navigation ───────────────────────────────────────────────
    // Scroll mode tells the server to maintain a cursor across pages rather than starting
    // a new search each time — like Han keeping the hyperdrive engaged between jumps
    // instead of powering it down and back up for each sector.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether scroll mode should be used with paged search.
     * In scroll mode the server keeps a search cursor open between pages (stateful),
     * which is more efficient when scrolling through results sequentially.
     * The alternative is stateless paging where each page triggers a fresh search.
     *
     * <p>For example — Han keeps the nav thread alive:</p>
     * <pre>
     *   pagedSearchScrollModeButton.getSelection() == true
     *   // Server maintains search context cookie between page requests
     * </pre>
     *
     * @return {@code true} if scroll mode (stateful paging) is selected
     */
    private boolean isPagedSearchScrollMode()
    {
        return pagedSearchScrollModeButton.getSelection();
    }


    // ── Han Fires Up A Test Jump ──────────────────────────────────────────────────────────
    // Before committing to a real connection, Han wants to test the coordinates with a
    // throw-away connection object — check the nav parameters are valid without actually
    // jumping the whole ship to hyperspace.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a temporary {@link Connection} object from the current state of all parameter
     * pages.  We use this throwaway connection to query the server for base DNs without
     * persisting anything to the connection store — Han's dry-run jump before the real one.
     *
     * <p>For example — Han primes the test sequence:</p>
     * <pre>
     *   ConnectionParameter cp = listener.getTestConnectionParameters();
     *   Connection testConn = new Connection( cp );
     *   // testConn used only to query Root DSE, then discarded
     * </pre>
     *
     * @return a short-lived {@link Connection} built from the current UI parameters
     */
    private Connection getTestConnection()
    {
        ConnectionParameter cp = connectionParameterPageModifyListener.getTestConnectionParameters();
        Connection conn = new Connection( cp );
        return conn;
    }


    // ── Han Lays Out The Entire Console ───────────────────────────────────────────────────
    // The full nav console has four sections: the starting-point (base DN) inputs, the
    // search limit dials, the LDAP-control toggles, and the feature checkboxes.
    // This method arranges all four sections inside the parent composite — Han wiring up
    // every panel before the pre-flight checklist begins.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the entire UI for this parameter page by composing four input sections.
     * Called by the Eclipse wizard framework when it is time to render this page.
     * Each section corresponds to a logical group of related LDAP browser settings.
     *
     * <p>For example — Han assembles the console:</p>
     * <pre>
     *   addBaseDNInput( parent );   // starting coordinate
     *   addLimitInput( parent );    // how far and how long to search
     *   addControlInput( parent );  // special LDAP control toggles
     *   addFeaturesInput( parent ); // extra fetch flags
     * </pre>
     *
     * @param parent  the SWT composite provided by the wizard framework to host our widgets
     */
    protected void createComposite( Composite parent )
    {
        addBaseDNInput( parent );
        addLimitInput( parent );
        addControlInput( parent );
        addFeaturesInput( parent );
    }


    // ── Han Enters The Origin Coordinate ─────────────────────────────────────────────────
    // The base DN is the jump origin — the root of the LDAP subtree the browser will show.
    // Han can let the Falcon auto-detect it from the Root DSE, or type in a DN manually.
    // This method builds that input group on screen.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Base DN" UI group containing the auto-fetch checkbox, the manual-fetch
     * button, and the combo where base DNs are entered or selected.
     * The base DN (Distinguished Name) is the starting point in the LDAP tree — like the
     * system origin in Han's nav computer before plotting the hyperspace route.
     *
     * <p>For example — Han sets up his origin panel:</p>
     * <pre>
     *   autoFetchBaseDnsButton  → "Get base DNs from Root DSE" checkbox
     *   fetchBaseDnsButton      → "Fetch Base DNs" push button (queries server now)
     *   baseDNCombo             → drop-down / editable text for the actual DN
     * </pre>
     *
     * @param parent  the SWT composite in which to place this group
     */
    private void addBaseDNInput( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        Group group = BaseWidgetUtils.createGroup( composite,
            Messages.getString( "BrowserParameterPage.BaseDNGroup" ), 1 ); //$NON-NLS-1$
        Composite groupComposite = BaseWidgetUtils.createColumnContainer( group, 3, 1 );
        GridData gd;

        autoFetchBaseDnsButton = BaseWidgetUtils.createCheckbox( groupComposite, Messages
            .getString( "BrowserParameterPage.GetBaseDNsFromRootDSE" ), 2 ); //$NON-NLS-1$
        autoFetchBaseDnsButton.setSelection( true );

        fetchBaseDnsButton = new Button( groupComposite, SWT.PUSH );
        fetchBaseDnsButton.setText( Messages.getString( "BrowserParameterPage.FetchBaseDNs" ) ); //$NON-NLS-1$
        fetchBaseDnsButton.setEnabled( true );
        gd = new GridData();
        gd.horizontalAlignment = SWT.RIGHT;
        fetchBaseDnsButton.setLayoutData( gd );

        BaseWidgetUtils.createLabel( groupComposite, Messages.getString( "BrowserParameterPage.BaseDN" ), 1 ); //$NON-NLS-1$
        baseDNCombo = BaseWidgetUtils.createCombo( groupComposite, new String[0], 0, 2 );
    }


    // ── Han Configures The LDAP Control Panel ─────────────────────────────────────────────
    // Beyond basic limits there are special LDAP protocol controls — ManageDsaIT,
    // fetch subentries, paged search with its page size and scroll mode.
    // Han is flipping the switches on the Falcon's advanced systems panel.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Controls" UI group containing checkboxes for ManageDsaIT,
     * fetch-subentries, and paged search (with its page-size field and scroll-mode toggle).
     * Each of these maps to an LDAP protocol control that changes how the server processes our requests.
     *
     * <p>For example — Han flips the advanced switches:</p>
     * <pre>
     *   manageDsaItButton           → send ManageDsaIT control with searches
     *   fetchSubentriesButton       → also fetch subentries
     *   pagedSearchButton           → enable paged results (RFC 2696)
     *   pagedSearchSizeText         → entries per page (default 100)
     *   pagedSearchScrollModeButton → stateful scroll vs stateless paging
     * </pre>
     *
     * @param parent  the SWT composite in which to place this group
     */
    private void addControlInput( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        Group group = BaseWidgetUtils.createGroup( composite, Messages.getString( "BrowserParameterPage.Controls" ), 1 ); //$NON-NLS-1$
        Composite groupComposite = BaseWidgetUtils.createColumnContainer( group, 1, 1 );

        // ManageDsaIT control
        manageDsaItButton = BaseWidgetUtils.createCheckbox( groupComposite, Messages
            .getString( "BrowserParameterPage.ManageDsaItWhileBrowsing" ), 1 ); //$NON-NLS-1$
        manageDsaItButton.setToolTipText( Messages.getString( "BrowserParameterPage.ManageDsaItWhileBrowsingTooltip" ) ); //$NON-NLS-1$
        manageDsaItButton.setSelection( false );

        // fetch subentries control
        fetchSubentriesButton = BaseWidgetUtils.createCheckbox( groupComposite, Messages
            .getString( "BrowserParameterPage.FetchSubentriesWhileBrowsing" ), 1 ); //$NON-NLS-1$
        fetchSubentriesButton.setToolTipText( Messages
            .getString( "BrowserParameterPage.FetchSubentriesWhileBrowsingTooltip" ) ); //$NON-NLS-1$
        fetchSubentriesButton.setSelection( false );

        // paged search control
        Composite sprcComposite = BaseWidgetUtils.createColumnContainer( groupComposite, 4, 1 );
        pagedSearchButton = BaseWidgetUtils.createCheckbox( sprcComposite, Messages
            .getString( "BrowserParameterPage.PagedSearch" ), 1 ); //$NON-NLS-1$
        pagedSearchButton.setToolTipText( Messages.getString( "BrowserParameterPage.PagedSearchTooltip" ) ); //$NON-NLS-1$

        pagedSearchSizeLabel = BaseWidgetUtils.createLabel( sprcComposite, Messages
            .getString( "BrowserParameterPage.PageSize" ), 1 ); //$NON-NLS-1$
        pagedSearchSizeText = BaseWidgetUtils.createText( sprcComposite, "100", 5, 1 ); //$NON-NLS-1$
        pagedSearchScrollModeButton = BaseWidgetUtils.createCheckbox( sprcComposite, Messages
            .getString( "BrowserParameterPage.ScrollMode" ), 1 ); //$NON-NLS-1$
        pagedSearchScrollModeButton.setToolTipText( Messages.getString( "BrowserParameterPage.ScrollModeTooltip" ) ); //$NON-NLS-1$
        pagedSearchScrollModeButton.setSelection( true );
    }


    // ── Han Switches On The Full Sensor Suite ─────────────────────────────────────────────
    // This section covers optional features that aren't LDAP protocol controls but affect
    // how the browser fetches data — specifically, whether to also pull operational attributes.
    // Han enabling the Falcon's full sensor sweep beyond the standard scan.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Features" UI group, which currently contains the fetch-operational-attributes
     * checkbox.  Operational attributes (e.g. {@code modifyTimestamp}) are not returned by
     * default and must be requested explicitly — Han enabling the extended sensor suite.
     *
     * <p>For example — Han requests deep diagnostics:</p>
     * <pre>
     *   fetchOperationalAttributesButton selected
     *   → search attribute list includes "+" to retrieve server-maintained attributes
     * </pre>
     *
     * @param parent  the SWT composite in which to place this group
     */
    private void addFeaturesInput( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        Group group = BaseWidgetUtils.createGroup( composite, Messages.getString( "BrowserParameterPage.Features" ), 1 ); //$NON-NLS-1$
        Composite groupComposite = BaseWidgetUtils.createColumnContainer( group, 1, 1 );

        // fetch operational attributes feature
        fetchOperationalAttributesButton = BaseWidgetUtils.createCheckbox( groupComposite, Messages
            .getString( "BrowserParameterPage.FetchOperationalAttributesWhileBrowsing" ), 1 ); //$NON-NLS-1$
        fetchOperationalAttributesButton.setToolTipText( Messages
            .getString( "BrowserParameterPage.FetchOperationalAttributesWhileBrowsingTooltip" ) ); //$NON-NLS-1$
        fetchOperationalAttributesButton.setSelection( false );
    }


    // ── Han Dials In Limits And Nav Mode ─────────────────────────────────────────────────
    // The limit section holds three side-by-side widgets: the count/time limits dial,
    // the alias-dereferencing selector, and the referral-handling selector.
    // Han is calibrating how far to search, how to handle shortcuts, and how to handle
    // forwarding beacons — all the flight parameters in one row.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the limits and navigation mode row, which contains the {@link LimitWidget}
     * (count + time limits), the {@link AliasesDereferencingWidget}, and the
     * {@link ReferralsHandlingWidget} laid out in a three-column composite.
     * These three widgets work together to define how aggressively the browser searches.
     *
     * <p>For example — Han sets three dials in one motion:</p>
     * <pre>
     *   limitWidget       → sizeLimit=1000, timeLimit=0
     *   aliasWidget       → ALWAYS dereference
     *   referralsWidget   → FOLLOW_MANUALLY
     * </pre>
     *
     * @param parent  the SWT composite in which to place this input row
     */
    public void addLimitInput( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        limitWidget = new LimitWidget( 1000, 0 );
        limitWidget.createWidget( composite );

        aliasesDereferencingWidget = new AliasesDereferencingWidget( Connection.AliasDereferencingMethod.ALWAYS );
        aliasesDereferencingWidget.createWidget( composite );

        referralsHandlingWidget = new ReferralsHandlingWidget( Connection.ReferralHandlingMethod.FOLLOW_MANUALLY );
        referralsHandlingWidget.createWidget( composite, true );
    }


    // ── Han Runs The Pre-Jump Systems Check ───────────────────────────────────────────────
    // Before Han commits to hyperspace, the Falcon's computer runs a self-check —
    // are all the values in range?  Is the base DN a valid DN string?  Are the
    // paged-search controls showing only when paging is enabled?
    // This method enforces all of that, setting error messages where needed.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current page state and adjusts widget enable/disable states.
     * Specifically: the base DN combo is disabled when auto-fetch is on; paged-search
     * sub-controls are disabled when paging is off; and an error message is set if the
     * manually entered base DN is not a valid LDAP DN.  No message means we're good to go.
     *
     * <p>For example — Han's pre-jump checklist:</p>
     * <pre>
     *   baseDNCombo.setEnabled( !isAutoFetchBaseDns() );
     *   if ( !Dn.isValid( getBaseDN() ) ) {
     *       message = "Enter a valid base DN";
     *   }
     * </pre>
     */
    protected void validate()
    {
        // set enabled/disabled state of fields and buttons
        baseDNCombo.setEnabled( !isAutoFetchBaseDns() );
        pagedSearchSizeLabel.setEnabled( isPagedSearch() );
        pagedSearchSizeText.setEnabled( isPagedSearch() );
        pagedSearchScrollModeButton.setEnabled( isPagedSearch() );

        // validate input fields
        message = null;
        infoMessage = null;
        errorMessage = null;
        if ( !isAutoFetchBaseDns() )
        {
            if ( !Dn.isValid( getBaseDN() ) )
            {
                message = Messages.getString( "BrowserParameterPage.EnterValidBaseDN" ); //$NON-NLS-1$
            }
        }
    }


    // ── Han Reads The Stored Nav Computer Settings ────────────────────────────────────────
    // When the connection wizard opens an existing connection for editing, it calls this
    // method to load previously saved parameters back into the UI widgets.
    // Han pulling up last flight's saved coordinates and restoring every dial to its previous state.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates all UI widgets from the given {@link ConnectionParameter} object.
     * Called when the wizard is editing an existing connection — we read every stored
     * extended property and restore it into the corresponding checkbox, combo, or text field.
     *
     * <p>For example — Han restores last flight's settings:</p>
     * <pre>
     *   boolean fetchBaseDns = parameter.getExtendedBoolProperty( ... );
     *   autoFetchBaseDnsButton.setSelection( fetchBaseDns );
     *   limitWidget.setCountLimit( countLimit );
     *   // ... and so on for each parameter
     * </pre>
     *
     * @param parameter  the saved connection parameters to load into the UI
     */
    protected void loadParameters( ConnectionParameter parameter )
    {
        this.connectionParameter = parameter;

        boolean fetchBaseDns = parameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_BASE_DNS );
        autoFetchBaseDnsButton.setSelection( fetchBaseDns );
        String baseDn = parameter.getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_BASE_DN );
        baseDNCombo.setText( baseDn != null ? baseDn : "" ); //$NON-NLS-1$

        int countLimit = parameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_COUNT_LIMIT );
        limitWidget.setCountLimit( countLimit );
        int timeLimit = parameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_TIME_LIMIT );
        limitWidget.setTimeLimit( timeLimit );

        int referralsHandlingMethodOrdinal = parameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_REFERRALS_HANDLING_METHOD );
        Connection.ReferralHandlingMethod referralsHandlingMethod = Connection.ReferralHandlingMethod
            .getByOrdinal( referralsHandlingMethodOrdinal );
        referralsHandlingWidget.setReferralsHandlingMethod( referralsHandlingMethod );

        int aliasesDereferencingMethodOrdinal = parameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD );
        Connection.AliasDereferencingMethod aliasesDereferencingMethod = Connection.AliasDereferencingMethod
            .getByOrdinal( aliasesDereferencingMethodOrdinal );
        aliasesDereferencingWidget.setAliasesDereferencingMethod( aliasesDereferencingMethod );

        boolean manageDsaIT = parameter.getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_MANAGE_DSA_IT );
        manageDsaItButton.setSelection( manageDsaIT );

        boolean fetchSubentries = parameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_SUBENTRIES );
        fetchSubentriesButton.setSelection( fetchSubentries );

        boolean pagedSearch = parameter.getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH );
        pagedSearchButton.setSelection( pagedSearch );
        String pagedSearchSize = parameter
            .getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SIZE );
        pagedSearchSizeText.setText( pagedSearchSize != null ? pagedSearchSize : "100" ); //$NON-NLS-1$
        boolean pagedSearchScrollMode = parameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SCROLL_MODE );
        pagedSearchScrollModeButton.setSelection( pagedSearch ? pagedSearchScrollMode : true );

        boolean fetchOperationalAttributes = parameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_OPERATIONAL_ATTRIBUTES );
        fetchOperationalAttributesButton.setSelection( fetchOperationalAttributes );
    }


    // ── Han Wires Up The Cockpit Buttons ─────────────────────────────────────────────────
    // Once the UI is built and parameters loaded, we attach event listeners so that any
    // change to a checkbox or text field immediately triggers re-validation.
    // Han connecting each switch on the console to the nav computer's feedback loop —
    // flip a switch, the computer responds.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers SWT event listeners on all interactive widgets so that any user change
     * triggers {@link #connectionPageModified()} — which cascades into {@link #validate()}
     * and updates the wizard's OK/Finish button state.
     * The "Fetch Base DNs" button also fires a live query to the server and shows the results
     * in a dialog — like Han actually pinging the nav beacon to confirm coordinates.
     */
    protected void initListeners()
    {
        autoFetchBaseDnsButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                connectionPageModified();
            }
        } );

        fetchBaseDnsButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                Connection connection = getTestConnection();
                IBrowserConnection browserConnection = new BrowserConnection( connection );

                FetchBaseDNsRunnable runnable = new FetchBaseDNsRunnable( browserConnection );
                IStatus status = RunnableContextRunner.execute( runnable, runnableContext, true );
                if ( status.isOK() )
                {
                    if ( !runnable.getBaseDNs().isEmpty() )
                    {
                        List<String> baseDNs = runnable.getBaseDNs();
                        baseDNCombo.setItems( baseDNs.toArray( new String[baseDNs.size()] ) );
                        baseDNCombo.select( 0 );

                        String msg = Messages.getString( "BrowserParameterPage.BaseDNResult" ); //$NON-NLS-1$
                        for ( String baseDN : baseDNs )
                        {
                            msg += "\n  - " + baseDN; //$NON-NLS-1$
                        }
                        MessageDialog.openInformation( Display.getDefault().getActiveShell(), Messages
                            .getString( "BrowserParameterPage.FetchBaseDNs" ), msg ); //$NON-NLS-1$
                    }
                    else
                    {
                        MessageDialog.openWarning( Display.getDefault().getActiveShell(), Messages
                            .getString( "BrowserParameterPage.FetchBaseDNs" ), //$NON-NLS-1$
                            Messages.getString( "BrowserParameterPage.NoBaseDNReturnedFromServer" ) ); //$NON-NLS-1$
                        autoFetchBaseDnsButton.setSelection( false );
                    }
                }
            }
        } );

        baseDNCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent event )
            {
                connectionPageModified();
            }
        } );

        manageDsaItButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                connectionPageModified();
            }
        } );

        fetchSubentriesButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                connectionPageModified();
            }
        } );

        pagedSearchButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                connectionPageModified();
            }
        } );
        pagedSearchSizeText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );
        pagedSearchSizeText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                connectionPageModified();
            }
        } );

        fetchOperationalAttributesButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                connectionPageModified();
            }
        } );
    }


    // ── Han Locks In The Coordinates ─────────────────────────────────────────────────────
    // When the user clicks Finish in the wizard, the framework calls this to persist
    // the current widget values into the connection parameter object for long-term storage.
    // Han locking in his final coordinates before hitting the hyperspace lever.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the current UI widget values into the given {@link ConnectionParameter} object.
     * This is the commit step — everything the user configured on this page gets serialized
     * into extended properties on the parameter object for storage and later retrieval.
     *
     * <p>For example — Han commits the nav program:</p>
     * <pre>
     *   parameter.setExtendedBoolProperty( FETCH_BASE_DNS, isAutoFetchBaseDns() );
     *   parameter.setExtendedProperty( BASE_DN, getBaseDN() );
     *   parameter.setExtendedIntProperty( COUNT_LIMIT, getCountLimit() );
     *   // ... all other fields
     * </pre>
     *
     * @param parameter  the {@link ConnectionParameter} to write our settings into
     */
    public void saveParameters( ConnectionParameter parameter )
    {
        parameter
            .setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_BASE_DNS, isAutoFetchBaseDns() );
        parameter.setExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_BASE_DN, getBaseDN() );
        parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_COUNT_LIMIT, getCountLimit() );
        parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_TIME_LIMIT, getTimeLimit() );
        parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_REFERRALS_HANDLING_METHOD,
            getReferralsHandlingMethod().getOrdinal() );
        parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD,
            getAliasesDereferencingMethod().getOrdinal() );

        parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_MANAGE_DSA_IT, manageDsaIT() );
        parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_SUBENTRIES,
            isFetchSubentries() );
        parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH, isPagedSearch() );
        parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SIZE,
            getPagedSearchSize() );
        parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SCROLL_MODE,
            isPagedSearchScrollMode() );
        parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_OPERATIONAL_ATTRIBUTES,
            isFetchOperationalAttributes() );
    }


    // ── Han Saves Nothing Extra To Dialog State ───────────────────────────────────────────
    // Some parameter pages persist additional UI state (e.g. window sizes) to Eclipse's
    // dialog settings store.  This page has no such extras — Han's nav console doesn't
    // remember its window position between sessions.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves any page-level dialog settings (e.g. column widths) to Eclipse's dialog-settings
     * store.  This page has nothing extra to store beyond the connection parameters themselves,
     * so this method is intentionally empty.
     */
    public void saveDialogSettings()
    {
    }


    // ── Han Points The Crosshair At The Base DN Field ────────────────────────────────────
    // When the wizard page becomes active, we move keyboard focus to the most important
    // input — the base DN combo.  Han's first instinct when he sits down: reach for the
    // starting coordinate field.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the base DN combo when this page becomes active in the wizard.
     * Gives the user an obvious starting point for data entry without requiring a mouse click.
     */
    public void setFocus()
    {
        baseDNCombo.setFocus();
    }


    // ── Han Checks Whether Any Dial Has Moved ────────────────────────────────────────────
    // The wizard needs to know if anything actually changed so it can decide whether to
    // show a "reconnect" prompt.  This method compares the current widget values against
    // the last saved parameter object and returns true if anything differs.
    // Han eyeballing the console to see if any settings drifted from the last flight.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if any parameter on this page differs from the last saved value.
     * The wizard framework uses this to decide whether to prompt the user about applying changes.
     * We compare limits, control flags, and paged-search settings — anything that can be changed
     * without requiring a full reconnect is checked here.
     *
     * <p>For example — Han's diff check:</p>
     * <pre>
     *   return isReconnectionRequired()
     *       || countLimit != getCountLimit()
     *       || pagedSearch != isPagedSearch()
     *       || ...;
     * </pre>
     *
     * @return {@code true} if the user has changed at least one parameter
     */
    public boolean areParametersModifed()
    {
        int countLimit = connectionParameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_COUNT_LIMIT );
        int timeLimit = connectionParameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_TIME_LIMIT );

        boolean manageDsaIT = connectionParameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_MANAGE_DSA_IT );
        boolean fetchSubentries = connectionParameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_SUBENTRIES );
        boolean pagedSearch = connectionParameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH );
        int pagedSearchSize = connectionParameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SIZE );
        boolean pagedSearchScrollMode = connectionParameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SCROLL_MODE );

        return isReconnectionRequired() || countLimit != getCountLimit() || timeLimit != getTimeLimit()
            || manageDsaIT != manageDsaIT() || fetchSubentries != isFetchSubentries() || pagedSearch != isPagedSearch()
            || pagedSearchSize != getPagedSearchSize() || pagedSearchScrollMode != isPagedSearchScrollMode();
    }


    // ── Han Decides Whether To Abort And Re-Plot ─────────────────────────────────────────
    // Some parameter changes (like changing the base DN or alias handling) require dropping
    // the current LDAP connection and reconnecting.  Others (like changing count limit) take
    // effect without a reconnect.  This method figures out which case we're in.
    // Han checking: "Do I need to power down the hyperdrive and restart, or can I adjust on the fly?"
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the changed parameters require dropping and re-establishing the
     * LDAP connection.  Parameters that affect how the connection is set up (base DN, alias
     * dereferencing method, referral handling, fetch-operational-attributes) force a reconnect;
     * parameters that only affect search behavior (count limit, paged search size) do not.
     *
     * <p>For example — Han's reconnect check:</p>
     * <pre>
     *   if ( fetchBaseDns != isAutoFetchBaseDns() ) return true; // must reconnect
     *   if ( aliasMethod != getAliasesDereferencingMethod() ) return true;
     *   return false; // just tweak the search params in-place
     * </pre>
     *
     * @return {@code true} if a full reconnect is needed, {@code false} if parameters
     *         can be applied to the live connection
     */
    public boolean isReconnectionRequired()
    {
        if ( connectionParameter == null )
        {
            return true;
        }

        boolean fetchBaseDns = connectionParameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_BASE_DNS );
        String baseDn = connectionParameter.getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_BASE_DN );
        int referralsHandlingMethodOrdinal = connectionParameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_REFERRALS_HANDLING_METHOD );
        Connection.ReferralHandlingMethod referralsHandlingMethod = Connection.ReferralHandlingMethod
            .getByOrdinal( referralsHandlingMethodOrdinal );
        int aliasesDereferencingMethodOrdinal = connectionParameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD );
        Connection.AliasDereferencingMethod aliasesDereferencingMethod = Connection.AliasDereferencingMethod
            .getByOrdinal( aliasesDereferencingMethodOrdinal );
        boolean fetchOperationalAttributes = connectionParameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_OPERATIONAL_ATTRIBUTES );

        return fetchBaseDns != isAutoFetchBaseDns() || !StringUtils.equals( baseDn, getBaseDN() )
            || referralsHandlingMethod != getReferralsHandlingMethod()
            || aliasesDereferencingMethod != getAliasesDereferencingMethod()
            || fetchOperationalAttributes != isFetchOperationalAttributes();
    }


    // ── Han Broadcasts Coordinates Over The Comm ─────────────────────────────────────────
    // When exporting connection settings as an LDAP URL, we pack our browser-specific
    // parameters into non-standard LDAP URL extensions (X-BASE-DN, X-COUNT-LIMIT, etc.).
    // Han transmitting the Falcon's nav settings to another ship so they can follow the same route.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes this page's parameters as custom extensions on the given {@link LdapUrl}.
     * Only non-default values are written to keep the URL compact — the defaults are well-known
     * and can be reconstructed without explicit encoding.
     * The LDAP URL format doesn't natively carry browser settings, so we use {@code X-} extension keys.
     *
     * <p>For example — Han broadcasts his settings:</p>
     * <pre>
     *   ldapUrl extensions:
     *     X-BASE-DN=dc=example,dc=com
     *     X-COUNT-LIMIT=1000
     *     X-ALIAS-HANDLING=NEVER
     *     X-PAGED-SEARCH  (flag, no value)
     *     X-PAGED-SEARCH-SIZE=100
     * </pre>
     *
     * @param parameter  the source of truth for the parameter values to encode
     * @param ldapUrl    the URL object to append extension entries to
     */
    public void mergeParametersToLdapURL( ConnectionParameter parameter, LdapUrl ldapUrl )
    {
        boolean fetchBaseDns = parameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_BASE_DNS );
        String baseDn = parameter.getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_BASE_DN );
        if ( !fetchBaseDns && StringUtils.isNotEmpty( baseDn ) )
        {
            ldapUrl.getExtensions().add( new Extension( false, X_BASE_DN, baseDn ) );
        }

        int countLimit = parameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_COUNT_LIMIT );
        if ( countLimit != 0 )
        {
            ldapUrl.getExtensions().add(
                new Extension( false, X_COUNT_LIMIT, parameter
                    .getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_COUNT_LIMIT ) ) );
        }

        int timeLimit = parameter.getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_TIME_LIMIT );
        if ( timeLimit != 0 )
        {
            ldapUrl.getExtensions().add(
                new Extension( false, X_TIME_LIMIT, parameter
                    .getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_TIME_LIMIT ) ) );
        }

        int aliasesDereferencingMethodOrdinal = parameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD );
        Connection.AliasDereferencingMethod aliasesDereferencingMethod = Connection.AliasDereferencingMethod
            .getByOrdinal( aliasesDereferencingMethodOrdinal );
        switch ( aliasesDereferencingMethod )
        {
            case ALWAYS:
                // default
                break;
            case FINDING:
                ldapUrl.getExtensions().add( new Extension( false, X_ALIAS_HANDLING, X_ALIAS_HANDLING_FINDING ) );
                break;
            case SEARCH:
                ldapUrl.getExtensions().add( new Extension( false, X_ALIAS_HANDLING, X_ALIAS_HANDLING_SEARCHING ) );
                break;
            case NEVER:
                ldapUrl.getExtensions().add( new Extension( false, X_ALIAS_HANDLING, X_ALIAS_HANDLING_NEVER ) );
                break;
        }

        int referralsHandlingMethodOrdinal = parameter
            .getExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_REFERRALS_HANDLING_METHOD );
        Connection.ReferralHandlingMethod referralsHandlingMethod = Connection.ReferralHandlingMethod
            .getByOrdinal( referralsHandlingMethodOrdinal );
        switch ( referralsHandlingMethod )
        {
            case FOLLOW_MANUALLY:
                // default
                break;
            case IGNORE:
                ldapUrl.getExtensions().add( new Extension( false, X_REFERRAL_HANDLING, X_REFERRAL_HANDLING_IGNORE ) );
                break;
            case FOLLOW:
                ldapUrl.getExtensions().add( new Extension( false, X_REFERRAL_HANDLING, X_REFERRAL_HANDLING_FOLLOW ) );
                break;
        }

        // ManageDsaIT control
        boolean manageDsaIt = parameter.getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_MANAGE_DSA_IT );
        if ( manageDsaIt )
        {
            ldapUrl.getExtensions().add( new Extension( false, X_MANAGE_DSA_IT, null ) );
        }

        // fetch subentries
        boolean fetchSubentries = parameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_SUBENTRIES );
        if ( fetchSubentries )
        {
            ldapUrl.getExtensions().add( new Extension( false, X_FETCH_SUBENTRIES, null ) );
        }

        // paged search
        boolean pagedSearch = parameter.getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH );
        if ( pagedSearch )
        {
            ldapUrl.getExtensions().add( new Extension( false, X_PAGED_SEARCH, null ) );
            ldapUrl.getExtensions().add(
                new Extension( false, X_PAGED_SEARCH_SIZE, parameter
                    .getExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SIZE ) ) );
            boolean pagedSearchScrollMode = parameter
                .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SCROLL_MODE );
            if ( pagedSearchScrollMode )
            {
                ldapUrl.getExtensions().add( new Extension( false, X_PAGED_SEARCH_SCROLL_MODE, null ) );
            }
        }

        // fetch operational attributes
        boolean fetchOperationalAttributes = parameter
            .getExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_OPERATIONAL_ATTRIBUTES );
        if ( fetchOperationalAttributes )
        {
            ldapUrl.getExtensions().add( new Extension( false, X_FETCH_OPERATIONAL_ATTRIBUTES, null ) );
        }
    }


    // ── Han Decodes A Received Nav Transmission ───────────────────────────────────────────
    // When importing a connection from an LDAP URL, we do the reverse of mergeParametersToLdapURL —
    // parse the X-extension values back into the connection parameter object.
    // Han receiving coordinates from another ship and loading them into the Falcon's nav computer.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads browser-specific parameters from the given {@link LdapUrl}'s custom extensions and
     * writes them into the given {@link ConnectionParameter}.  This is the inverse of
     * {@link #mergeParametersToLdapURL}.  Missing or unrecognized extension values fall back
     * to sensible defaults (e.g. count limit 0 if missing, alias handling ALWAYS if unrecognized).
     *
     * <p>For example — Han reads an incoming nav packet:</p>
     * <pre>
     *   String baseDn = ldapUrl.getExtensionValue( "X-BASE-DN" );
     *   if ( baseDn == null ) {
     *       // auto-fetch from Root DSE
     *   } else {
     *       parameter.setExtendedProperty( BASE_DN, baseDn );
     *   }
     * </pre>
     *
     * @param ldapUrl    the source LDAP URL containing X-extension values to decode
     * @param parameter  the target {@link ConnectionParameter} to populate
     */
    public void mergeLdapUrlToParameters( LdapUrl ldapUrl, ConnectionParameter parameter )
    {
        // base Dn, get from Root DSE if absent, may be empty
        String baseDn = ldapUrl.getExtensionValue( X_BASE_DN );
        if ( baseDn == null )
        {
            parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_BASE_DNS, true );
            parameter.setExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_BASE_DN, null );
        }
        else
        {
            parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_BASE_DNS, false );
            parameter.setExtendedProperty( IBrowserConnection.CONNECTION_PARAMETER_BASE_DN, baseDn );
        }

        // count limit, 1000 if non-numeric or absent
        String countLimit = ldapUrl.getExtensionValue( X_COUNT_LIMIT );
        try
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_COUNT_LIMIT,
                Integer.valueOf( countLimit ) );
        }
        catch ( NumberFormatException e )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_COUNT_LIMIT, 0 );
        }

        // time limit, 0 if non-numeric or absent
        String timeLimit = ldapUrl.getExtensionValue( X_TIME_LIMIT );
        try
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_TIME_LIMIT,
                Integer.valueOf( timeLimit ) );
        }
        catch ( NumberFormatException e )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_TIME_LIMIT, 0 );
        }

        // alias handling, ALWAYS if unknown or absent
        String alias = ldapUrl.getExtensionValue( X_ALIAS_HANDLING );
        if ( StringUtils.isNotEmpty( alias ) && X_ALIAS_HANDLING_FINDING.equalsIgnoreCase( alias ) )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD,
                Connection.AliasDereferencingMethod.FINDING.getOrdinal() );
        }
        else if ( StringUtils.isNotEmpty( alias ) && X_ALIAS_HANDLING_SEARCHING.equalsIgnoreCase( alias ) )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD,
                Connection.AliasDereferencingMethod.SEARCH.getOrdinal() );
        }
        else if ( StringUtils.isNotEmpty( alias ) && X_ALIAS_HANDLING_NEVER.equalsIgnoreCase( alias ) )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD,
                Connection.AliasDereferencingMethod.NEVER.getOrdinal() );
        }
        else
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD,
                Connection.AliasDereferencingMethod.ALWAYS.getOrdinal() );
        }

        // referral handling, FOLLOW_MANUALLY if unknown or absent
        String referral = ldapUrl.getExtensionValue( X_REFERRAL_HANDLING );
        if ( StringUtils.isNotEmpty( referral ) && X_REFERRAL_HANDLING_IGNORE.equalsIgnoreCase( referral ) )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_REFERRALS_HANDLING_METHOD,
                Connection.ReferralHandlingMethod.IGNORE.getOrdinal() );
        }
        else if ( StringUtils.isNotEmpty( referral ) && X_REFERRAL_HANDLING_FOLLOW.equalsIgnoreCase( referral ) )
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_REFERRALS_HANDLING_METHOD,
                Connection.ReferralHandlingMethod.FOLLOW.getOrdinal() );
        }
        else
        {
            parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_REFERRALS_HANDLING_METHOD,
                Connection.ReferralHandlingMethod.FOLLOW_MANUALLY.getOrdinal() );
        }

        // ManageDsaIT control
        Extension manageDsaIT = ldapUrl.getExtension( X_MANAGE_DSA_IT );
        parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_MANAGE_DSA_IT, manageDsaIT != null );

        // fetch subentries
        Extension fetchSubentries = ldapUrl.getExtension( X_FETCH_SUBENTRIES );
        parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_SUBENTRIES,
            fetchSubentries != null );

        // paged search
        Extension pagedSearch = ldapUrl.getExtension( X_PAGED_SEARCH );
        String pagedSearchSize = ldapUrl.getExtensionValue( X_PAGED_SEARCH_SIZE );
        Extension pagedSearchScrollMode = ldapUrl.getExtension( X_PAGED_SEARCH_SCROLL_MODE );
        if ( pagedSearch != null )
        {
            parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH,
                pagedSearch != null );
            try
            {
                parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SIZE,
                    Integer.valueOf( pagedSearchSize ) );
            }
            catch ( NumberFormatException e )
            {
                parameter.setExtendedIntProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SIZE, 100 );
            }
            parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_PAGED_SEARCH_SCROLL_MODE,
                pagedSearchScrollMode != null );
        }

        // fetch operational attributes
        Extension fetchOperationalAttributes = ldapUrl.getExtension( X_FETCH_OPERATIONAL_ATTRIBUTES );
        parameter.setExtendedBoolProperty( IBrowserConnection.CONNECTION_PARAMETER_FETCH_OPERATIONAL_ATTRIBUTES,
            fetchOperationalAttributes != null );
    }
}
