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


// ── CLASS: FetchReferralsAction — FOLLOWING THE CABLE ON KAMINO ───────────────
// On Kamino, Obi-Wan follows a nav beacon that wasn't in the Jedi Archives.
// He traces the signal: the coordinates lead to a rain-soaked ocean planet
// where an entire clone army has been secretly built. An LDAP referral is
// exactly like that beacon: it's not the real entry — it's a pointer saying
// "what you're looking for is actually over there at that other LDAP server."
// FetchReferralsAction controls whether we follow those pointers (referrals)
// when loading an entry's children, or just display the referral node as-is.
// The connection must not be in ManageDsaIT mode (which turns referrals into
// plain entries) for this toggle to make sense.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A checkbox action that toggles whether LDAP referrals are followed when
 * loading the children of the selected entries. An LDAP referral is a special
 * entry that points to content on a different server or base DN. When this is
 * enabled, the browser follows the pointer to fetch the real children; when
 * disabled, referral nodes are displayed as-is without dereferencing.
 * Only available when the connection is not in ManageDsaIT mode (which
 * converts referrals to normal entries and renders this toggle pointless).
 * Think of this class as Obi-Wan tracing the nav beacon to Kamino — following
 * the pointer to find what's really on the other end.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FetchReferralsAction extends BrowserAction
{
    // ── OBI-WAN PREPARES TO TRACE THE BEACON ──────────────────────────────────
    // Default constructor — no setup required. The action queries the
    // selection and connection state at runtime to determine its behavior.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new FetchReferralsAction. No arguments required — the
     * enabled/checked states are computed dynamically from the current selection
     * and connection configuration.
     */
    public FetchReferralsAction()
    {
    }


    // ── TRACING THE BEACON IS A TOGGLE ────────────────────────────────────────
    // The action renders as a checkbox — click it to start following referrals,
    // click again to stop and display them as raw referral nodes.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@link Action#AS_CHECK_BOX} so the action renders as a toggle
     * checkbox in menus. The checked state reflects whether referral following
     * is currently active for the selected entries.
     *
     * @return the SWT/JFace style constant for a checkbox action.
     */
    @Override
    public int getStyle()
    {
        return Action.AS_CHECK_BOX;
    }


    // ── OBI-WAN NAMES THE BEACON HE'S FOLLOWING ───────────────────────────────
    // The label is retrieved from the NLS message bundle for localization.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action ("Fetch Referrals" or the localized
     * equivalent). Retrieved from the NLS message bundle.
     *
     * @return the localized action label.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "FetchOperationalAttributesAction.FetchReferrals" ); //$NON-NLS-1$
    }


    // ── NO ICON — OBI-WAN SENSES WITHOUT CEREMONY ─────────────────────────────
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
    // No Eclipse command ID is associated with this action.
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


    // ── OBI-WAN CAN ONLY FOLLOW THE BEACON IF IT'S NOT MASKED ────────────────
    // ManageDsaIT mode tells the server to treat referrals as ordinary entries
    // (no automatic following). When that mode is active, this per-entry toggle
    // is meaningless and is disabled. Also requires at least one entry selected.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when at least one entry is selected and the connection
     * is not in ManageDsaIT mode. In ManageDsaIT mode, referrals are treated as
     * ordinary entries, so this toggle has no effect and is disabled.
     *
     * @return {@code true} if referral following can be toggled.
     */
    @Override
    public boolean isEnabled()
    {
        List<IEntry> entries = getEntries();
        return !entries.isEmpty() && !entries.iterator().next().getBrowserConnection().isManageDsaIT();
    }


    // ── OBI-WAN CHECKS WHETHER HE'S ALREADY FOLLOWING THE BEACON ─────────────
    // The checkbox is checked when ALL selected entries have fetchReferrals = true.
    // If even one doesn't, the box shows unchecked.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when all selected entries currently have referral
     * following enabled ({@code isFetchReferrals() == true}). Returns
     * {@code false} if any entry has it disabled or if no entries are selected.
     *
     * @return {@code true} if all selected entries are fetching referrals.
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
                if ( !entry.isFetchReferrals() )
                {
                    checked = false;
                }
            }
        }
        return checked;
    }


    // ── OBI-WAN TRACES THE BEACON — THE REFERRAL CHILDREN LOAD ───────────────
    // We flip the fetchReferrals flag to the opposite of the current state on
    // every selected entry, then fire off a background job to re-initialize the
    // children with the new setting. The tree view refreshes to show the
    // dereferenced children (or revert to raw referral nodes if toggling off).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Toggles referral following for all selected entries and re-initializes
     * their children. Sets each entry's {@code fetchReferrals} flag to the
     * opposite of the current checked state, then dispatches a background
     * {@link InitializeChildrenRunnable} to reload the children with the
     * updated setting.
     *
     * <p>For example — Obi-Wan tracing the beacon, the real destination revealed:</p>
     * <pre>
     *   entry.setFetchReferrals( !isChecked() );
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
            entry.setFetchReferrals( init );
        }
        new StudioBrowserJob( new InitializeChildrenRunnable( true, entries ) ).execute();
    }


    // ── OBI-WAN IDENTIFIES WHICH ENTRIES HAVE BEACONS ────────────────────────
    // Returns the directly selected entries only. Referral following is a
    // per-entry tree-navigation concern, so search results and bookmarks are
    // not included — only explicit tree selections count.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the directly selected entries. Only the entries selected in the
     * browser tree are returned — search results and bookmarks are excluded since
     * referral following is a tree-navigation setting.
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
