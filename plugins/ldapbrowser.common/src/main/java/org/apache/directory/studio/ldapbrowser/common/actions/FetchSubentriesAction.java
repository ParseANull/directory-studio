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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: FetchSubentriesAction — YODA DESCENDING INTO THE CAVE OF EVIL ─────
// On Dagobah, Yoda points Luke toward a dark cave. "That place," he says,
// "is strong with the dark side of the Force. A domain of evil it is." Luke
// goes in — and finds something that normally stays hidden below the surface.
// Subentries in LDAP are exactly like that: they live in the directory tree
// but most searches don't return them by default. Subentries (defined in
// RFC 3672) hold administrative policies — access control, collective attributes,
// schema rules. FetchSubentriesAction is the toggle that tells the browser to
// include them in the children list, or leave them hidden. It is only available
// when the connection is not already configured to always fetch subentries.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A checkbox action that toggles whether LDAP subentries are included when
 * loading the children of the selected entries. Subentries (RFC 3672) hold
 * administrative data like collective attribute specifications and access
 * control rules. LDAP servers normally suppress them in regular subtree
 * searches. This action requests them explicitly by setting a subentries
 * search control. Only available when the connection is not already globally
 * configured to fetch subentries for all operations.
 * Think of this class as Yoda sending Luke into the Cave of Evil — reaching
 * into the hidden space where the policy entries live.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FetchSubentriesAction extends BrowserAction
{
    // ── YODA POINTS TOWARD THE CAVE ───────────────────────────────────────────
    // Default constructor — the action is ready immediately. No setup is
    // needed because all behavior is driven by the selection at runtime.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new FetchSubentriesAction. No arguments required — enabled
     * and checked states are computed dynamically from the current selection
     * and connection configuration.
     */
    public FetchSubentriesAction()
    {
    }


    // ── LUKE'S CHOICE — ENTER OR STAY OUT — IS A TOGGLE ──────────────────────
    // The action renders as a checkbox: click to start fetching subentries,
    // click again to stop.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@link Action#AS_CHECK_BOX} so the action renders as a toggle
     * checkbox in menus. The checked state reflects whether subentry fetching
     * is currently active for the selected entries.
     *
     * @return the SWT/JFace style constant for a checkbox action.
     */
    @Override
    public int getStyle()
    {
        return Action.AS_CHECK_BOX;
    }


    // ── YODA NAMES THE HIDDEN PATH ────────────────────────────────────────────
    // The label is fetched from the NLS message bundle for localization.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action ("Fetch Subentries" or the
     * localized equivalent). Retrieved from the NLS message bundle.
     *
     * @return the localized action label.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "FetchOperationalAttributesAction.FetchSubentries" ); //$NON-NLS-1$
    }


    // ── THE CAVE HAS NO SIGN — NO ICON ────────────────────────────────────────
    // This action has no icon and appears as text only in the context menu.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — no icon; the action appears as text only.
     *
     * @return null.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── NO KEYBINDING ─────────────────────────────────────────────────────────
    // No Eclipse command ID is wired up for this action.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — no Eclipse command ID and no keyboard shortcut.
     *
     * @return null.
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── YODA CHECKS: CAN LUKE ENTER? ──────────────────────────────────────────
    // The action is available only when at least one entry is selected AND the
    // connection isn't already globally configured to always fetch subentries.
    // If it always fetches them, this per-entry toggle is redundant.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when at least one entry is selected and the connection
     * is NOT already globally configured to always fetch subentries. If the
     * connection fetches them globally, this per-entry toggle is redundant and
     * is disabled.
     *
     * @return {@code true} if subentry fetching can be toggled for the selection.
     */
    @Override
    public boolean isEnabled()
    {
        List<IEntry> entries = getEntries();
        return !entries.isEmpty() && !entries.iterator().next().getBrowserConnection().isFetchSubentries();
    }


    // ── YODA CHECKS WHETHER LUKE IS ALREADY INSIDE ────────────────────────────
    // The checkbox is checked when ALL selected entries already have
    // fetchSubentries = true. If any one doesn't, the checkbox shows unchecked.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when all selected entries currently have subentry
     * fetching enabled ({@code isFetchSubentries() == true}). Returns
     * {@code false} if any entry has it disabled or if no entries are selected.
     *
     * @return {@code true} if all selected entries are fetching subentries.
     */
    @Override
    public boolean isChecked()
    {
        boolean checked = true;
        List<IEntry> entries = getEntries();
        if ( entries.isEmpty() )
        {
            checked = false;
        }
        else
        {
            for ( IEntry entry : entries )
            {
                if ( !entry.isFetchSubentries() )
                {
                    checked = false;
                }
            }
        }
        return checked;
    }


    // ── LUKE ENTERS THE CAVE — THE SUBENTRIES APPEAR ──────────────────────────
    // We flip the fetchSubentries flag to the opposite of the current state
    // on every selected entry, then fire off a background job to re-initialize
    // the children. The browser tree refreshes to include or exclude subentry
    // nodes accordingly.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Toggles subentry fetching for all selected entries and re-initializes
     * their children. Sets each entry's {@code fetchSubentries} flag to the
     * opposite of the current checked state, then dispatches a background
     * {@link InitializeChildrenRunnable} to reload the children with the new
     * setting applied.
     *
     * <p>For example — Luke entering the cave, the hidden entries revealed:</p>
     * <pre>
     *   entry.setFetchSubentries( !isChecked() );
     *   new StudioBrowserJob( new InitializeChildrenRunnable( true, entries ) ).execute();
     * </pre>
     */
    @Override
    public void run()
    {
        IEntry[] entries = getEntries().toArray( new IEntry[0] );
        boolean init = !isChecked();
        for ( IEntry entry : entries )
        {
            entry.setFetchSubentries( init );
        }
        new StudioBrowserJob( new InitializeChildrenRunnable( true, entries ) ).execute();
    }


    // ── YODA KNOWS WHICH ENTRIES ARE NEAR THE CAVE ────────────────────────────
    // Returns the directly selected entries. Subentry fetching is a per-entry
    // tree-navigation concern — search results and bookmarks are not included.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the directly selected entries. Only entries selected in the
     * browser tree are returned — search results and bookmarks are excluded,
     * since subentry fetching is a tree-navigation setting.
     *
     * @return a list of selected entries; may be empty, never null.
     */
    protected List<IEntry> getEntries()
    {
        List<IEntry> entriesList = new ArrayList<IEntry>();
        entriesList.addAll( Arrays.asList( getSelectedEntries() ) );
        return entriesList;
    }

}
