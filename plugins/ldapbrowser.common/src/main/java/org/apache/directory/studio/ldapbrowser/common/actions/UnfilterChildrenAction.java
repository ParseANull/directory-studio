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
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: UnfilterChildrenAction — HAN BLASTS THE TRASH COMPACTOR CONTROLS ──
// In A New Hope, Han, Luke, Leia, and Chewie are trapped in the trash compactor
// as the walls close in.  R2-D2 gets the call and shuts down the compactor —
// the walls stop, and the full space opens back up.  That's UnfilterChildren:
// the LDAP browser has a "children filter" that restricts which child entries
// are shown under a parent (the walls closing in, narrowing the view).  This
// action blasts that filter away so all children are visible again.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Removes the children filter from the currently selected LDAP entry and
 * reloads its children from the server.
 *
 * <p>When a children filter is applied to an entry, the browser only shows
 * child entries that match the filter criteria — useful for navigating large
 * directories, but sometimes you want to see everything again.  This action
 * clears that filter (sets it to {@code null}) and fires a background job to
 * re-initialize the full child list from the LDAP server.</p>
 *
 * <p>Think of this as Han and R2-D2 shutting down the trash compactor: the
 * walls (filter) stop closing in and the full space (all children) is
 * available again.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UnfilterChildrenAction extends BrowserAction
{
    // ── Han Readies His Blaster ───────────────────────────────────────────────
    // Han doesn't need any special gear to blast those controls — he's always
    // ready to act when someone calls.  Same here: no elaborate initialization,
    // just delegate to the parent and be ready.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code UnfilterChildrenAction}.  Delegates to the
     * parent constructor for standard Eclipse action setup.
     */
    public UnfilterChildrenAction()
    {
        super();
    }


    // ── Han Shoots the Control Panel — Walls Stop Immediately ─────────────────
    // Han blasts the compactor control, R2 shuts it down, and the walls freeze.
    // Then everyone can breathe again — the full space is available.
    // We clear the children filter and kick off a background reload.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the action: clears the children filter on the selected entry
     * (by setting it to {@code null}) and kicks off a
     * {@link InitializeChildrenRunnable} via a background job to reload the
     * full child list from the LDAP server.
     *
     * <p>For example — Han blasts the compactor controls:</p>
     * <pre>
     *   Han fires (entry.setChildrenFilter(null) — filter gone).
     *   R2 shuts it down (InitializeChildrenRunnable fires).
     *   The compactor walls pull back: all child entries are now visible.
     * </pre>
     */
    public void run()
    {
        if ( getSelectedEntries().length == 1 )
        {
            IEntry entry = getSelectedEntries()[0];
            entry.setChildrenFilter( null );
            new StudioBrowserJob( new InitializeChildrenRunnable( true, entry ) ).execute();
        }
    }


    // ── Han Names the Move Plainly: "Remove the Filter" ──────────────────────
    // Han doesn't dress up what he did — "I blasted the controls."  Succinct,
    // accurate, no fluff.  We return the localized label for the menu item.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action — something like
     * "Remove Children Filter".
     *
     * @return the localized label; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "UnfilterChildrenAction.RemoveChildrenFilter" ); //$NON-NLS-1$
    }


    // ── Han's Blaster Has a Recognizable Look ────────────────────────────────
    // You can spot Han's DL-44 blaster from across the cantina — distinctive,
    // memorable.  Our unfilter icon is equally recognizable in the toolbar.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action, loaded from the plugin's image
     * registry — the "unfilter DIT" image.
     *
     * @return the {@link ImageDescriptor} for the unfilter-children icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_UNFILTER_DIT );
    }


    // ── The Move Has No Official Rebel Command ID ─────────────────────────────
    // Han's improvised tactics aren't in the Rebel operations manual — there's
    // no keybinding registered for this action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action.  Unfilter Children has
     * no associated command ID (and therefore no keybinding), so we return
     * {@code null}.
     *
     * @return {@code null} — no command ID registered
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Han Only Shoots When the Compactor Is Actually Running ────────────────
    // Han won't blast random controls for no reason — the compactor has to
    // actually be active (filter applied) and exactly one entry has to be
    // in trouble.  We check the same conditions.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this action should be enabled.  We require:
     * <ul>
     *   <li>Exactly one entry selected (not a search result, bookmark, or search)</li>
     *   <li>The selected entry has a non-{@code null} children filter applied</li>
     * </ul>
     *
     * @return {@code true} if the conditions are met to remove a filter;
     *         {@code false} otherwise
     */
    public boolean isEnabled()
    {
        return getSelectedSearches().length + getSelectedSearchResults().length + getSelectedBookmarks().length == 0
            && getSelectedEntries().length == 1 && getSelectedEntries()[0].getChildrenFilter() != null;
    }
}
