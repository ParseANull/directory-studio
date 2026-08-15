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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;


// -- CLASS: EntryEditorWidgetPreferences -- HAN CALIBRATES THE FALCON'S NAV CONTROLS --
// Han Solo is in the Millennium Falcon's cockpit before the Kessel Run, tuning each nav
// parameter one by one: hyperdrive multiplier, deflection shield threshold, route priority.
// This class is that control panel. Each getter reads one setting from the Eclipse preference
// store -- folding threshold, sort order, attribute grouping -- so the sorter, content
// provider, and filter widget all pull their configuration from one canonical place.
// ---------------------------------------------------------------------------------
/**
 * A thin wrapper around the Eclipse preference store for the entry editor widget.
 * Rather than letting every component reach into the preference store directly, we centralise
 * all the entry-editor settings here. Any class that needs to know "should I fold?" or
 * "what's the default sort order?" just asks us.
 * Think of this class as the Falcon's nav-calibration panel: Han sets the parameters once
 * and every system on the ship reads them from the same source.
 *
 * <p>Settings managed here:</p>
 * <ul>
 *   <li>PREFERENCE_ENTRYEDITOR_ENABLE_FOLDING -- fold multi-value attributes?</li>
 *   <li>PREFERENCE_ENTRYEDITOR_FOLDING_THRESHOLD -- how many values trigger folding?</li>
 *   <li>PREFERENCE_ENTRYEDITOR_AUTO_EXPAND_FOLDED_ATTRIBUTES -- auto-expand when set?</li>
 *   <li>PREFERENCE_ENTRYEDITOR_OBJECTCLASS_AND_MUST_ATTRIBUTES_FIRST -- priority grouping</li>
 *   <li>PREFERENCE_ENTRYEDITOR_OPERATIONAL_ATTRIBUTES_LAST -- push system attrs to bottom</li>
 *   <li>PREFERENCE_ENTRYEDITOR_DEFAULT_SORT_BY -- sort by attribute name or by value?</li>
 *   <li>PREFERENCE_ENTRYEDITOR_DEFAULT_SORT_ORDER -- ascending, descending, or none?</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetPreferences
{
    /** The viewer. */
    protected Viewer viewer;


    // -- HAN SETTLES INTO THE PILOT SEAT ----------------------------------------
    // Han drops into the Falcon's seat and rests his hands on the controls.
    // Nothing is set yet -- we just create the object and wait for connect() to
    // wire it to a specific viewer.
    // ---------------------------------------------------------------------------------
    /**
     * Creates a new preferences instance with no viewer attached yet.
     * Call {@link #connect(TreeViewer)} before using this object to read preferences
     * that need viewer context.
     *
     * <p>For example -- Han takes the pilot seat:</p>
     * <pre>
     *   Han sits down. No destination plotted yet.
     *   "She'll be ready when you are, kid."
     * </pre>
     */
    public EntryEditorWidgetPreferences()
    {
    }


    // -- HAN PLUGS IN THE NAV COMPUTER ------------------------------------------
    // Han slots the hyperspace coordinate cartridge into the nav computer and
    // tells the ship's systems: "that viewer over there is our current mission."
    // We store the viewer reference so preference-change notifications can
    // trigger a refresh of the right table.
    // ---------------------------------------------------------------------------------
    /**
     * Wires this preferences object to the given {@link TreeViewer}.
     * Some preference changes need to refresh the viewer -- storing the reference
     * here lets subclasses or listeners do that without needing to pass it around.
     *
     * <p>For example -- Han plugs in the nav cartridge:</p>
     * <pre>
     *   Han slots the cartridge into the Falcon's nav socket.
     *   "Alright -- coordinates locked. Ready to jump on your mark."
     * </pre>
     *
     * @param viewer  the entry editor tree viewer to associate with these preferences
     */
    public void connect( TreeViewer viewer )
    {
        this.viewer = viewer;
    }


    // -- HAN POWERS DOWN THE NAV COMPUTER AFTER THE RUN ------------------------
    // The Kessel Run is done. Han shuts off the nav computer and clears the
    // coordinate buffer so the memory is freed up for the next mission.
    // ---------------------------------------------------------------------------------
    /**
     * Releases the viewer reference when the entry editor is being torn down.
     * After this call, the preferences object should not be used again.
     *
     * <p>For example -- Han powers down after the jump:</p>
     * <pre>
     *   Han flips the nav computer off.
     *   "Good run. Wipe the coordinates -- no sense leaving them in the buffer."
     * </pre>
     */
    public void dispose()
    {
        viewer = null;
    }


    // -- HAN CHECKS THE FOLDING TOGGLE ------------------------------------------
    // Han glances at the fold-threshold switch on the co-pilot's console.
    // If it's flipped on, multi-value attributes above the threshold get collapsed
    // into a single summary row rather than flooding the table.
    // ---------------------------------------------------------------------------------
    /**
     * Returns whether attribute-value folding is enabled.
     * When folding is on, attributes with more values than the threshold are shown
     * as a single collapsible row instead of one row per value.
     *
     * <p>For example -- Han reads the fold switch:</p>
     * <pre>
     *   Han: "Folding engaged? Let me check the console."
     *   Switch is ON -> return true  (large attribute groups are collapsed)
     *   Switch is OFF -> return false (all values shown individually)
     * </pre>
     *
     * @return  {@code true} if folding is enabled in the preference store
     */
    public boolean isUseFolding()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_ENABLE_FOLDING );
    }


    // -- HAN READS THE FOLD THRESHOLD DIAL --------------------------------------
    // The Falcon's nav computer has a dial that says "collapse route segments
    // with more than N waypoints."  Han reads it so the content provider knows
    // where to draw the line between flat rows and folded groups.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the number of values an attribute must exceed before it is folded.
     * If an attribute has more values than this threshold and folding is enabled,
     * the content provider shows it as a single collapsed row.
     *
     * <p>For example -- Han reads the threshold dial:</p>
     * <pre>
     *   Dial reads 50. Attribute "memberOf" has 200 values.
     *   200 > 50 -> fold it into a group row.
     * </pre>
     *
     * @return  the folding threshold (integer, from the preference store)
     */
    public int getFoldingThreshold()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_FOLDING_THRESHOLD );
    }


    // -- HAN CHECKS THE AUTO-EXPAND SWITCH --------------------------------------
    // There's a secondary toggle: "when you fold a group, auto-expand it if the
    // user had it open before?"  Han reads it to decide whether to restore the
    // previous expanded state after a refresh.
    // ---------------------------------------------------------------------------------
    /**
     * Returns whether folded attribute groups should be automatically expanded
     * when the entry editor re-loads an entry whose attributes were previously expanded.
     *
     * <p>For example -- Han checks the auto-expand switch:</p>
     * <pre>
     *   Switch: auto-expand ON -> restore expanded groups after refresh
     *   Switch: auto-expand OFF -> always start collapsed
     * </pre>
     *
     * @return  {@code true} if folded attributes should be auto-expanded on reload
     */
    public boolean isAutoExpandFoldedAttributes()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTO_EXPAND_FOLDED_ATTRIBUTES );
    }


    // -- HAN CHECKS THE PRIORITY-FIRST TOGGLE -----------------------------------
    // Some nav routes list critical waypoints first regardless of alphabetical
    // order. This toggle tells the sorter to put objectClass and must-attributes
    // at the top of the table before everything else.
    // ---------------------------------------------------------------------------------
    /**
     * Returns whether objectClass and must-attributes should be sorted to the top
     * of the entry editor, ahead of optional (may) attributes.
     * When true, the sorter uses a two-tier approach: priority group first, then
     * alphabetical within each group.
     *
     * <p>For example -- Han reads the priority-route toggle:</p>
     * <pre>
     *   Toggle ON  -> objectClass first, then cn/sn (must), then mail (may)
     *   Toggle OFF -> pure alphabetical or sort-by setting wins
     * </pre>
     *
     * @return  {@code true} if objectClass and must attributes should be sorted first
     */
    public boolean isObjectClassAndMustAttributesFirst()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_OBJECTCLASS_AND_MUST_ATTRIBUTES_FIRST );
    }


    // -- HAN CHECKS THE OPERATIONAL-ATTRS-LAST TOGGLE ---------------------------
    // Operational attributes (things the server manages, like createTimestamp)
    // are like engine telemetry -- useful for diagnostics but not for normal
    // navigation. This toggle sends them to the bottom of the table.
    // ---------------------------------------------------------------------------------
    /**
     * Returns whether operational (server-managed) attributes should be pushed to the
     * bottom of the entry editor, after all user-visible attributes.
     * Operational attributes are things like {@code createTimestamp} and {@code entryUUID}
     * that the LDAP server manages internally.
     *
     * <p>For example -- Han stows the engine telemetry at the bottom:</p>
     * <pre>
     *   Toggle ON  -> createTimestamp, modifyTimestamp, etc. go last
     *   Toggle OFF -> operational attrs sort in with everything else
     * </pre>
     *
     * @return  {@code true} if operational attributes should appear last in the table
     */
    public boolean isOperationalAttributesLast()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_OPERATIONAL_ATTRIBUTES_LAST );
    }


    // -- HAN READS THE DEFAULT SORT-BY SELECTOR ---------------------------------
    // On the nav panel there's a selector: "sort route segments by distance or
    // by waypoint label?"  Here it's "sort by attribute description or by value?"
    // The sorter reads this to know which column drives the default sort order.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the default column to sort by when no user sort has been applied.
     * One of {@link BrowserCoreConstants#SORT_BY_ATTRIBUTE_DESCRIPTION} (sort by the
     * attribute name), {@link BrowserCoreConstants#SORT_BY_VALUE} (sort by the value),
     * or {@link BrowserCoreConstants#SORT_BY_NONE}.
     *
     * <p>For example -- Han reads the sort-column selector:</p>
     * <pre>
     *   Selector -> SORT_BY_ATTRIBUTE_DESCRIPTION
     *   Sorter will default to alphabetical order by attribute name.
     * </pre>
     *
     * @return  the default sort-by constant from {@link BrowserCoreConstants}
     */
    public int getDefaultSortBy()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_DEFAULT_SORT_BY );
    }


    // -- HAN READS THE DEFAULT SORT-DIRECTION DIAL ------------------------------
    // The last nav parameter: which direction do we travel through the sorted list?
    // Ascending (A-Z / smallest first), descending (Z-A / largest first), or
    // no sorting at all?  The sorter uses this as its fallback when no column
    // header has been clicked.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the default sort direction when no user sort has been applied.
     * One of {@link BrowserCoreConstants#SORT_ORDER_NONE},
     * {@link BrowserCoreConstants#SORT_ORDER_ASCENDING}, or
     * {@link BrowserCoreConstants#SORT_ORDER_DESCENDING}.
     *
     * <p>For example -- Han sets the route direction:</p>
     * <pre>
     *   Dial -> SORT_ORDER_ASCENDING
     *   Entry editor lists attributes A -> Z by default.
     * </pre>
     *
     * @return  the default sort-order constant from {@link BrowserCoreConstants}
     */
    public int getDefaultSortOrder()
    {
        return BrowserCommonActivator.getDefault().getPreferenceStore().getInt(
            BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_DEFAULT_SORT_ORDER );
    }
}
