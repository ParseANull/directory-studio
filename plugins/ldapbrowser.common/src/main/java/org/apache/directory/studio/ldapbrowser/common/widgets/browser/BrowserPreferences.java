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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: BrowserPreferences — HAN CALIBRATING THE FALCON'S CONTROLS ────────
// Before every mission, Han Solo settles into the pilot's seat and calibrates
// the Millennium Falcon's controls: sets the sensitivity of the navigation
// thrusters, adjusts the display brightness, decides which diagnostic panels
// to show. When a shipboard tech adjusts a setting, the Falcon's systems
// update automatically — Han doesn't need to manually reload anything.
// BrowserPreferences is that calibration panel: it wraps the Eclipse preference
// stores and exposes all the browser's display settings (sort order, folding
// size, label format, which categories to show) as simple getter methods.
// It also listens for preference changes and triggers a tree refresh so the
// display updates immediately without requiring a restart.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wraps the Eclipse preference stores for the browser tree widget, exposing
 * all display and behaviour settings as convenient getter methods.
 * When any preference changes (in either the common or core store), this class
 * refreshes the connected tree viewer automatically.
 * Think of this as Han's Falcon calibration panel — it reads the current
 * settings on demand and triggers a display update whenever a dial turns.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserPreferences implements IPropertyChangeListener, Preferences.IPropertyChangeListener
{

    /** The tree viewer */
    protected TreeViewer viewer;


    // ── HAN SITS DOWN AND LISTENS FOR SHIPBOARD ALERTS ───────────────────────
    // Han straps in and registers for shipboard alerts on both channels:
    // the pilot's console (common preferences) and the engine room feed
    // (core plugin preferences). Any change on either channel triggers a
    // cockpit update so Han sees the latest readings.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BrowserPreferences and registers as a listener on both
     * Eclipse preference stores — the common UI store and the core plugin store.
     * This way, when the user changes any relevant setting in Eclipse preferences,
     * we hear about it and can trigger a tree refresh.
     *
     * <p>For example — Han registers for alerts on both Falcon control channels:</p>
     * <pre>
     *   pilotConsole.addListener(han);    // common preferences
     *   engineRoomFeed.addListener(han);  // core plugin preferences
     * </pre>
     */
    public BrowserPreferences()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().addPropertyChangeListener( this );
        BrowserCorePlugin.getDefault().getPluginPreferences().addPropertyChangeListener( this );
    }


    // ── HAN PLUGS IN THE HUD TO THE COCKPIT DISPLAY ──────────────────────────
    // Han connects the heads-up display to the Falcon's main sensor feed so
    // when a new sensor reading comes in, it refreshes on the HUD in real time.
    // We wire the tree viewer here so our propertyChange listener knows which
    // widget to refresh when a preference changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Connects the tree viewer to this preferences instance.
     * We store the viewer reference so that when a preference changes, we can
     * call {@code viewer.refresh()} to immediately update the display.
     * Call this right after creating the viewer and before showing it.
     *
     * <p>For example — Han plugs the HUD into the Falcon's live sensor feed:</p>
     * <pre>
     *   falcon.connectHUD(mainSensorFeed);
     *   // Now HUD refreshes whenever a new reading arrives
     * </pre>
     *
     * @param viewer   the browser's tree viewer to refresh when preferences change
     */
    public void connect( TreeViewer viewer )
    {
        this.viewer = viewer;
    }


    // ── HAN POWERS DOWN THE CALIBRATION PANEL BEFORE DOCKING ─────────────────
    // When the Falcon docks and the crew disembarks, Han powers down the
    // calibration panel — removes himself from the alert channels so the ship
    // doesn't waste power listening for signals nobody's acting on.
    // We unregister from both preference stores and null the viewer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters from the preference stores and releases the viewer reference.
     * Call this when the browser widget is disposed to prevent memory leaks
     * (preference stores hold strong references to listeners, so we must remove ourselves).
     *
     * <p>For example — Han powers down the Falcon's calibration panel before docking:</p>
     * <pre>
     *   pilotConsole.removeListener(han);
     *   engineRoomFeed.removeListener(han);
     *   han.disconnectHUD();
     * </pre>
     */
    public void dispose()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().removePropertyChangeListener( this );
        BrowserCorePlugin.getDefault().getPluginPreferences().removePropertyChangeListener( this );
        viewer = null;
    }


    // ── HAN CHECKS THE SORT ORDER DIAL ───────────────────────────────────────
    // Han glances at the nav computer dial to see how cargo gets sorted: by
    // weight, by destination, or not at all. The sorter reads this to decide
    // whether to sort tree entries by full RDN, RDN value, or leave them as-is.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns how browser entries should be sorted.
     * One of {@code BrowserCoreConstants.SORT_BY_NONE}, {@code SORT_BY_RDN},
     * or {@code SORT_BY_RDN_VALUE}. The sorter uses this to decide the sort key.
     *
     * <p>For example — Han checks the Falcon's cargo sort dial:</p>
     * <pre>
     *   int sortMode = prefs.getSortEntriesBy();
     *   // SORT_BY_RDN_VALUE — sort by the right-most name component's value
     * </pre>
     *
     * @return the sort-by constant from the preference store
     */
    public int getSortEntriesBy()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_SORT_BY );
    }


    // ── HAN CHECKS THE SORT DIRECTION SWITCH ─────────────────────────────────
    // Is the Falcon's cargo sorted A→Z (ascending) or Z→A (descending), or
    // not sorted at all? Han checks the direction switch on the console.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sort direction for browser entries.
     * One of {@code BrowserCoreConstants.SORT_ORDER_NONE}, {@code SORT_ORDER_ASCENDING},
     * or {@code SORT_ORDER_DESCENDING}.
     *
     * <p>For example — Han checks whether cargo loads go A→Z or Z→A:</p>
     * <pre>
     *   int order = prefs.getSortEntriesOrder(); // SORT_ORDER_ASCENDING
     * </pre>
     *
     * @return the sort order constant from the preference store
     */
    public int getSortEntriesOrder()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_SORT_ORDER );
    }


    // ── CHECKING THE SEARCHES SECTION SORT DIRECTION ─────────────────────────
    // A separate dial controls the sort order for the Searches section (as opposed
    // to the DIT entries). Han checks it independently.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sort direction for saved searches in the browser tree.
     * Searches can be sorted independently from directory entries.
     *
     * <p>For example — Han checks the sort order for the Searches cargo bay:</p>
     * <pre>
     *   int order = prefs.getSortSearchesOrder(); // SORT_ORDER_ASCENDING
     * </pre>
     *
     * @return the sort order constant for searches from the preference store
     */
    public int getSortSearchesOrder()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_SORT_SEARCHES_ORDER );
    }


    // ── CHECKING THE BOOKMARKS SECTION SORT DIRECTION ────────────────────────
    // And the Bookmarks section also has its own sort direction dial.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sort direction for bookmarks in the browser tree.
     * Bookmarks can be sorted independently from directory entries and searches.
     *
     * <p>For example — Han checks the bookmarks bay sort direction:</p>
     * <pre>
     *   int order = prefs.getSortBookmarksOrder(); // SORT_ORDER_NONE
     * </pre>
     *
     * @return the sort order constant for bookmarks from the preference store
     */
    public int getSortBookmarksOrder()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_SORT_BOOKMARKS_ORDER );
    }


    // ── CHECKING THE SORT PERFORMANCE LIMITER ────────────────────────────────
    // Han sets a performance limiter on the sort routine: "don't try to sort
    // more than 1000 items — it slows the ship down." Above that limit the
    // sorter just skips sorting for that subtree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of items to sort before giving up.
     * Sorting is O(n log n) and can be slow for very large subtrees.
     * Above this threshold the sorter skips sorting to keep the UI responsive.
     *
     * <p>For example — Han sets the Falcon's sort performance cap:</p>
     * <pre>
     *   int limit = prefs.getSortLimit(); // 1000 items max
     * </pre>
     *
     * @return the sort limit from the preference store
     */
    public int getSortLimit()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_SORT_LIMIT );
    }


    // ── LEAF ENTRIES: DO THEY GO FIRST? ──────────────────────────────────────
    // Han prefers empty cargo containers (leaf entries — no children) to be
    // stacked at the front of the hold so you see them right away.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if leaf entries (entries with no children) should be displayed
     * before non-leaf entries.
     *
     * <p>For example — Han's preference to put empty containers first in the hold:</p>
     * <pre>
     *   boolean leafFirst = prefs.isLeafEntriesFirst(); // true
     * </pre>
     *
     * @return true if leaf entries should sort before container entries
     */
    public boolean isLeafEntriesFirst()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_LEAF_ENTRIES_FIRST );
    }


    // ── CONTAINER ENTRIES: DO THEY GO FIRST? ─────────────────────────────────
    // Alternatively, Han might prefer full cargo containers (entries with children)
    // at the front so you see the interesting stuff first.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if container entries (entries with children) should be displayed
     * before leaf entries.
     *
     * <p>For example — Han's preference to put full containers first in the hold:</p>
     * <pre>
     *   boolean containerFirst = prefs.isContainerEntriesFirst(); // false
     * </pre>
     *
     * @return true if entries with children should sort before leaf entries
     */
    public boolean isContainerEntriesFirst()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_CONTAINER_ENTRIES_FIRST );
    }


    // ── META ENTRIES: PUSH THEM TO THE BACK ──────────────────────────────────
    // Schema and directory metadata entries are the boring technical labels on the
    // crates — Han prefers to push them to the back of the hold so they don't clutter the view.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if meta-data entries (schema, subschema, etc.) should be
     * displayed after regular entries.
     *
     * <p>For example — Han pushes meta-data crates to the back of the hold:</p>
     * <pre>
     *   boolean metaLast = prefs.isMetaEntriesLast(); // true
     * </pre>
     *
     * @return true if meta entries should sort to the end
     */
    public boolean isMetaEntriesLast()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_META_ENTRIES_LAST );
    }


    // ── IS THE BOOKMARKS PANEL VISIBLE? ──────────────────────────────────────
    // Han checks whether the Bookmarks instrument panel is toggled on in the cockpit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the Bookmarks category should be shown in the browser tree.
     *
     * <p>For example — Han checks whether the Bookmarks panel is switched on:</p>
     * <pre>
     *   boolean showBM = prefs.isShowBookmarks(); // true
     * </pre>
     *
     * @return true if the Bookmarks category is enabled
     */
    public boolean isShowBookmarks()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_BOOKMARKS );
    }


    // ── IS THE DIT PANEL VISIBLE? ─────────────────────────────────────────────
    // Is the main directory tree (DIT) panel switched on in the cockpit?
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the DIT (Directory Information Tree) category should be shown.
     *
     * <p>For example — Han checks whether the main directory panel is on:</p>
     * <pre>
     *   boolean showDIT = prefs.isShowDIT(); // true — usually want this on
     * </pre>
     *
     * @return true if the DIT category is enabled
     */
    public boolean isShowDIT()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_DIT );
    }


    // ── IS THE SEARCHES PANEL VISIBLE? ───────────────────────────────────────
    // Is the Searches instrument panel switched on?
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the Searches category should be shown in the browser tree.
     *
     * <p>For example — Han checks whether the Searches panel is switched on:</p>
     * <pre>
     *   boolean showSearches = prefs.isShowSearches(); // true
     * </pre>
     *
     * @return true if the Searches category is enabled
     */
    public boolean isShowSearches()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_SEARCHES );
    }


    // ── FOLDING THRESHOLD: HOW MANY ITEMS FIT IN ONE PAGE? ───────────────────
    // Han sets the Falcon's cargo auto-sort threshold: above 50 crates,
    // group them into numbered bins. Below 50, show them flat.
    // This controls when the browser starts grouping children into paginated bins.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the folding threshold — the maximum number of entries/results
     * to show flat before switching to paginated sub-groups.
     * When a parent entry has more children than this, they appear as numbered
     * page nodes ("[1...50]", "[51...100]", etc.) instead of all at once.
     *
     * <p>For example — Han sets the Falcon's auto-bin threshold:</p>
     * <pre>
     *   int threshold = prefs.getFoldingSize(); // 50
     *   if (children.length &gt; threshold) showAsBins();
     * </pre>
     *
     * @return the folding page size from the preference store
     */
    public int getFoldingSize()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_FOLDING_SIZE );
    }


    // ── IS FOLDING ENABLED AT ALL? ────────────────────────────────────────────
    // Han checks whether the auto-bin feature is switched on in the first place —
    // sometimes you want everything flat and don't care about paging.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if folding (pagination of large child lists) is enabled.
     * If false, all children are shown flat regardless of how many there are.
     *
     * <p>For example — Han checks whether auto-bin mode is switched on:</p>
     * <pre>
     *   boolean foldingOn = prefs.isUseFolding(); // true
     * </pre>
     *
     * @return true if folding is active
     */
    public boolean isUseFolding()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENABLE_FOLDING );
    }


    // ── ARE DIRECTORY META ENTRIES SHOWN? ────────────────────────────────────
    // Are the schema and metadata crates visible in the hold, or hidden behind
    // a panel? Most users don't need to see them daily.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if directory metadata entries (schema subentry, etc.) should
     * be shown in the browser tree.
     *
     * <p>For example — Han checks whether the metadata crates are visible:</p>
     * <pre>
     *   boolean showMeta = prefs.isShowDirectoryMetaEntries(); // false
     * </pre>
     *
     * @return true if metadata entries are visible in the tree
     */
    public boolean isShowDirectoryMetaEntries()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_DIRECTORY_META_ENTRIES );
    }


    // ── ARE ENTRY LABELS ABBREVIATED? ────────────────────────────────────────
    // Han has a console display that's only 30 characters wide. When entry labels
    // are too long, should we truncate them with "..." to fit?
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if entry labels should be truncated when they exceed the max length.
     *
     * <p>For example — Han checks whether the 30-char display clips long labels:</p>
     * <pre>
     *   boolean clip = prefs.isEntryAbbreviate(); // true — truncate long labels
     * </pre>
     *
     * @return true if abbreviation is enabled for entry labels
     */
    public boolean isEntryAbbreviate()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE );
    }


    // ── HOW LONG CAN AN ENTRY LABEL BE? ──────────────────────────────────────
    // Han checks the max character count that fits on his console display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum label length for entries before truncation kicks in.
     *
     * <p>For example — Han reads the max display width on the Falcon's console:</p>
     * <pre>
     *   int maxLen = prefs.getEntryAbbreviateMaxLength(); // 30 characters
     * </pre>
     *
     * @return the maximum display length for entry labels
     */
    public int getEntryAbbreviateMaxLength()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_ABBREVIATE_MAX_LENGTH );
    }


    // ── WHAT FORMAT DOES THE ENTRY LABEL USE? ────────────────────────────────
    // Han can choose how each entry is labelled: the full DN, just the RDN
    // ("cn=Luke,ou=pilots,..." → "cn=Luke"), or the RDN value ("Luke").
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label format for directory entries.
     * One of {@code BrowserCommonConstants.SHOW_DN}, {@code SHOW_RDN},
     * or {@code SHOW_RDN_VALUE}.
     *
     * <p>For example — Han picks the label format for the cargo manifest:</p>
     * <pre>
     *   int format = prefs.getEntryLabel(); // SHOW_RDN_VALUE → just "Luke"
     * </pre>
     *
     * @return the label format constant from the preference store
     */
    public int getEntryLabel()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_ENTRY_LABEL );
    }


    // ── ARE SEARCH RESULT LABELS ABBREVIATED? ────────────────────────────────
    // Same question as for entry labels, but specifically for search result nodes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if search result labels should be truncated when too long.
     *
     * <p>For example — Han checks truncation for the Searches panel display:</p>
     * <pre>
     *   boolean clip = prefs.isSearchResultAbbreviate(); // true
     * </pre>
     *
     * @return true if abbreviation is enabled for search result labels
     */
    public boolean isSearchResultAbbreviate()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE );
    }


    // ── HOW LONG CAN A SEARCH RESULT LABEL BE? ───────────────────────────────
    // Same as entry labels but for the search results panel.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum label length for search result entries before truncation.
     *
     * <p>For example — Han checks the max display width for search result entries:</p>
     * <pre>
     *   int maxLen = prefs.getSearchResultAbbreviateMaxLength(); // 30
     * </pre>
     *
     * @return the maximum display length for search result labels
     */
    public int getSearchResultAbbreviateMaxLength()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_ABBREVIATE_MAX_LENGTH );
    }


    // ── WHAT FORMAT DO SEARCH RESULT LABELS USE? ─────────────────────────────
    // Same as entry label format but for search result nodes in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label format for search result entries.
     * One of {@code BrowserCommonConstants.SHOW_DN}, {@code SHOW_RDN},
     * or {@code SHOW_RDN_VALUE}.
     *
     * <p>For example — Han picks how search result entries are labelled:</p>
     * <pre>
     *   int format = prefs.getSearchResultLabel(); // SHOW_DN — full distinguished name
     * </pre>
     *
     * @return the label format constant for search results
     */
    public int getSearchResultLabel()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_BROWSER_SEARCH_RESULT_LABEL );
    }


    // ── SHOULD BASE ENTRIES EXPAND ON CONNECT? ───────────────────────────────
    // When Han opens a new hyperspace route (connects to LDAP), should the base
    // entries auto-expand so you immediately see the top level? Or stay collapsed?
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the base entries should be automatically expanded when
     * a connection is opened in the browser.
     *
     * <p>For example — Han decides whether to auto-expand the manifest on docking:</p>
     * <pre>
     *   boolean expand = prefs.isExpandBaseEntries(); // true
     * </pre>
     *
     * @return true if base entries auto-expand on connection
     */
    public boolean isExpandBaseEntries()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_EXPAND_BASE_ENTRIES );
    }


    // ── SHOULD WE CHECK FOR CHILDREN BEFORE SHOWING THE EXPAND ARROW? ────────
    // Should the Falcon's sensors actively check whether each directory folder
    // actually has sub-folders, or just assume they all do (faster, but less accurate)?
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the browser should perform an LDAP query to check whether
     * an entry has children before showing the expand triangle.
     * When false, entries always show an expand arrow (optimistic assumption).
     * When true, a quick LDAP check confirms whether children exist first.
     *
     * <p>For example — Han decides whether to actively scan for sub-compartments:</p>
     * <pre>
     *   boolean check = prefs.isCheckForChildren(); // false — assume they're there
     * </pre>
     *
     * @return true if we should query LDAP to verify child existence
     */
    public boolean isCheckForChildren()
    {
        Preferences coreStore = BrowserCorePlugin.getDefault().getPluginPreferences();
        return coreStore.getBoolean( BrowserCoreConstants.PREFERENCE_CHECK_FOR_CHILDREN );
    }


    // ── THE FALCON'S ALERT SYSTEM FIRES: REFRESH THE COCKPIT DISPLAY ─────────
    // A technician in the engine room turns a knob — the change propagates to the
    // cockpit's alert system and Han's HUD refreshes automatically. This is the
    // JFace {@link IPropertyChangeListener} callback for the UI preference store.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when a JFace preference changes; refreshes the connected viewer.
     * This is the {@link IPropertyChangeListener} callback. When any UI preference
     * changes, we immediately refresh the browser tree so the user sees the new
     * display format, sort order, etc. without having to restart.
     *
     * <p>For example — the engine-room knob turns and the Falcon's HUD refreshes:</p>
     * <pre>
     *   engineRoom.turnKnob(SORT_ORDER, ASCENDING);
     *   // cockpit.refresh() fires automatically
     * </pre>
     *
     * @param event   the property change event describing what changed (unused;
     *                we always do a full tree refresh)
     */
    public void propertyChange( PropertyChangeEvent event )
    {
        if ( viewer != null )
        {
            viewer.refresh();
        }
    }


    // ── THE CORE PLUGIN ALERT FIRES: REFRESH THE COCKPIT DISPLAY ─────────────
    // Same thing, but from the core plugin's preference store instead of the
    // common UI store. Eclipse has two separate stores, so we implement two
    // versions of propertyChange().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when a core plugin preference changes; refreshes the connected viewer.
     * This is the {@link Preferences.IPropertyChangeListener} callback for the core plugin's
     * preferences (a different store from the JFace one above). Same behavior: we refresh
     * the browser tree so changes take effect immediately.
     *
     * <p>For example — the core engine notifies the Falcon's HUD of a sensor change:</p>
     * <pre>
     *   coreEngine.sensorAlert(CHECK_FOR_CHILDREN, true);
     *   // cockpit.refresh() fires automatically
     * </pre>
     *
     * @param event   the Eclipse-level property change event (unused; we always full-refresh)
     */
    public void propertyChange( org.eclipse.core.runtime.Preferences.PropertyChangeEvent event )
    {
        if ( viewer != null )
        {
            viewer.refresh();
        }
    }

}
