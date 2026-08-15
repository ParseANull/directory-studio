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

package org.apache.directory.studio.ldapbrowser.common.actions;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.dialogs.FilterWidgetDialog;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: FilterChildrenAction — R2-D2 FILTERING SENSOR NOISE ────────────────
// R2-D2 is scanning a corridor full of interference — Imperial sensor ghosts,
// equipment echoes, random noise from the Death Star's systems. Rather than
// showing all of it to Luke, R2 applies a filter: "show me only heat signatures
// that match this pattern." FilterChildrenAction does the same thing for the
// LDAP browser tree. When an entry has hundreds or thousands of children, the
// user can apply an LDAP filter (e.g., "(objectClass=person)") so the browser
// only loads and shows matching child entries. Opening this action pops up a
// dialog where you type the filter; confirming it saves it on the entry and
// re-fetches the children through that filter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens a filter dialog that lets the user set an LDAP filter on the selected
 * entry's children. Once set, only children matching the filter are loaded from
 * the server and shown in the browser tree. This is useful when browsing entries
 * with very large numbers of children — it narrows the view to just what you
 * need. Clearing the filter (entering an empty string) removes the restriction
 * and shows all children again.
 * Think of this class as R2 filtering the sensor feed to show only the signals
 * that matter.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FilterChildrenAction extends BrowserAction
{

    // ── R2 POWERS UP HIS SENSORS ──────────────────────────────────────────────
    // Default constructor — no special setup required. The action is ready to
    // open the filter dialog as soon as it's invoked.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new FilterChildrenAction. No arguments required — the action
     * reads the current selection state at runtime.
     */
    public FilterChildrenAction()
    {
        super();
    }


    // ── R2 APPLIES THE FILTER TO THE SENSOR STREAM ────────────────────────────
    // We open the FilterWidgetDialog pre-populated with the entry's current
    // children filter (or empty if none is set). If the user confirms, we apply
    // the new filter: empty input clears the filter (null = no filter), non-empty
    // trimmed input is stored as the children filter. Then we re-initialize the
    // entry's children so the new filter takes effect immediately in the tree view.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link FilterWidgetDialog} pre-populated with the selected entry's
     * current children filter. If the user confirms:
     * <ul>
     *   <li>An empty or blank filter string → clears the filter ({@code null})</li>
     *   <li>A non-blank filter string → stores it as the new children filter</li>
     * </ul>
     * After updating the filter, dispatches a background
     * {@link InitializeChildrenRunnable} to reload the entry's children through
     * the new filter. Does nothing if no entry is selected or the user cancels.
     *
     * <p>For example — R2 applying the filter and rescanning the corridor:</p>
     * <pre>
     *   entry.setChildrenFilter( "(objectClass=person)" );
     *   new StudioBrowserJob( new InitializeChildrenRunnable( true, entry ) ).execute();
     * </pre>
     */
    public void run()
    {
        if ( getSelectedEntries().length == 1 )
        {
            IEntry entry = getSelectedEntries()[0];
            FilterWidgetDialog dialog = new FilterWidgetDialog( getShell(), Messages
                .getString( "FilterChildrenAction.FilterChildren" ), //$NON-NLS-1$
                entry.getChildrenFilter(), entry.getBrowserConnection() );
            if ( dialog.open() == Dialog.OK )
            {
                String newFilter = dialog.getFilter();

                if ( newFilter == null || "".equals( newFilter.trim() ) ) //$NON-NLS-1$
                {
                    entry.setChildrenFilter( null );
                }
                else
                {
                    entry.setChildrenFilter( newFilter.trim() );
                }
                new StudioBrowserJob( new InitializeChildrenRunnable( true, entry ) ).execute();
            }
        }
    }


    // ── R2 LABELS THE FILTER CONTROL ──────────────────────────────────────────
    // The label is retrieved from the NLS message bundle for localization.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action ("Filter Children" or the localized
     * equivalent). Retrieved from the NLS message bundle.
     *
     * @return the localized action label.
     */
    public String getText()
    {
        return Messages.getString( "FilterChildrenAction.FilterChildrenLabel" ); //$NON-NLS-1$
    }


    // ── R2 HOLDS UP THE FILTER ICON ───────────────────────────────────────────
    // The custom DIT-filter icon from the browser image registry visually
    // distinguishes this action from other tree-management actions.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DIT-filter icon from the browser common image registry,
     * used to visually identify this action in menus.
     *
     * @return the filter-DIT image descriptor.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_FILTER_DIT );
    }


    // ── R2 HAS NO SHORTCUT — DELIBERATE FILTERING ─────────────────────────────
    // No Eclipse command ID is associated with this action — it's menu-only.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — no Eclipse command ID and no keyboard shortcut.
     *
     * @return null.
     */
    public String getCommandId()
    {
        return null;
    }


    // ── R2 CHECKS IF THERE IS SOMETHING TO FILTER ─────────────────────────────
    // The action only makes sense when exactly one non-search-result, non-search,
    // non-bookmark entry is selected, and that entry either has children (so
    // there is something to filter) or already has a filter active (so it can
    // be edited or cleared).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when exactly one entry is selected (and no searches,
     * search results, or bookmarks), and that entry either has children or already
     * has a children filter set. An entry with neither has nothing to filter.
     *
     * @return {@code true} if the filter dialog can usefully be opened.
     */
    public boolean isEnabled()
    {
        return getSelectedSearches().length + getSelectedSearchResults().length + getSelectedBookmarks().length == 0
            && getSelectedEntries().length == 1
            && ( getSelectedEntries()[0].hasChildren() || getSelectedEntries()[0].getChildrenFilter() != null );
    }
}
