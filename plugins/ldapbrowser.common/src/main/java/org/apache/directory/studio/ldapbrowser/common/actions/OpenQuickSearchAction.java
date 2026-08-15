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


import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.impl.QuickSearch;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenQuickSearchAction — HAN GUNS THE FALCON TO HYPERSPACE ─────────
// In A New Hope, Han Solo doesn't mess around when they need a fast escape:
// he slams the hyperdrive lever, the stars stretch into lines, and the Falcon
// is gone before the Star Destroyers can blink.  Speed is the whole point.
// That's what Quick Search is — a fast, low-friction way to search the LDAP
// directory without building a full saved search from scratch.  We set up the
// search base (the jump coordinates), open a slim configuration dialog, and
// fire off the search job the moment the user clicks OK.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements the "Open Quick Search" action for the LDAP browser.
 *
 * <p>A Quick Search is a lightweight, one-off LDAP search anchored to the
 * currently selected entry (or the Root DSE if nothing is selected).  Unlike a
 * saved search, it lives ephemerally on the connection and is meant for fast
 * exploration rather than reuse.</p>
 *
 * <p>Think of this class as Han Solo's hyperspace jump: we set coordinates
 * (the search base), configure the jump (the property dialog), and execute
 * immediately — no lengthy setup required.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenQuickSearchAction extends BrowserAction
{
    // ── Han Punches the Hyperdrive and They're Gone ───────────────────────────
    // Han grabs the lever — if there's already a calculated jump, he uses it;
    // if not, he punches in new coordinates on the fly and fires.
    // We either reuse an existing quick search or create a fresh one rooted at
    // the selected entry, open the config dialog, and execute the search.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the action: finds (or creates) a {@link IQuickSearch} for the
     * current browser connection, opens a property dialog so the user can
     * adjust the search filter and scope, then fires off the search job if no
     * results have been fetched yet.
     *
     * <p>For example — Han plots the hyperspace jump:</p>
     * <pre>
     *   Han: "Punch it, Chewie!"  [No saved coordinates?  Plot them now.]
     *   The Falcon lurches into hyperspace the moment the nav-computer confirms.
     *   We fire a StudioBrowserJob the moment the dialog returns OK.
     * </pre>
     */
    public void run()
    {
        IBrowserConnection browserConnection = getBrowserConnection();

        if ( browserConnection != null )
        {
            // Getting the current quick search
            IQuickSearch quickSearch = browserConnection.getQuickSearch();

            // Creating a new quick search with the currently selected entry
            // if there's no current quick search or quick search isn't selected
            if ( ( quickSearch == null ) || !isQuickSearchSelected() )
            {
                // Setting a default search base on Root DSE
                IEntry searchBase = browserConnection.getRootDSE();

                // Getting the selected entry
                IEntry selectedEntry = getSelectedEntry();

                if ( selectedEntry != null )
                {
                    // Setting the selected entry as search base
                    searchBase = selectedEntry;
                }

                // Creating a new quick search
                quickSearch = new QuickSearch( searchBase, browserConnection );
                browserConnection.setQuickSearch( quickSearch );
            }

            // Creating and opening the dialog
            PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn( getShell(), quickSearch,
                BrowserCommonConstants.PROP_SEARCH, null, null );
            dialog.getShell().setText(
                NLS.bind( Messages.getString( "PropertiesAction.PropertiesForX" ), //$NON-NLS-1$
                    Utils.shorten( quickSearch.getName(), 30 ) ) );
            if ( dialog.open() == PreferenceDialog.OK )
            {
                // Performing the quick search if it has not been performed before
                // (ie. the quick search was not modified at in the dialog)
                if ( quickSearch.getSearchResults() == null )
                {
                    new StudioBrowserJob( new SearchRunnable( new ISearch[]
                        { quickSearch } ) ).execute();
                }
            }
        }
    }


    // ── The Navicomputer Display Reads "Quick Jump" ───────────────────────────
    // Every button on the Falcon's console has a label — Han needs to know at a
    // glance which lever triggers the hyperdrive vs. the shields.
    // We return the localized menu label so users see exactly what this does.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action — the text that
     * appears in menus and toolbar tooltips.
     *
     * @return the localized label string; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "OpenQuickSearchAction.OpenQuickSearch" ); //$NON-NLS-1$
    }


    // ── The Hyperdrive Lever Has a Distinctive Shape ──────────────────────────
    // Among all the controls in the Falcon's cockpit, the hyperdrive lever is
    // visually distinct — you can grab it in the dark without looking.
    // Our icon makes this action visually identifiable in toolbars and menus.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action, loaded from the plugin's image
     * registry.  Displayed in toolbar buttons and menu items.
     *
     * @return the {@link ImageDescriptor} for the quick-search icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_QUICKSEARCH );
    }


    // ── The Jump Has No Registered Flight Plan ────────────────────────────────
    // Han's quick escapes don't go through Imperial traffic control — there's
    // no official command ID filed anywhere.  This action is similarly
    // unregistered in the Eclipse command framework.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action.  Quick Search has no
     * associated command ID (and therefore no keybinding), so we return
     * {@code null}.
     *
     * @return {@code null} — no command ID is registered for this action
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Han Can Only Jump If the Hyperdrive Is Connected ─────────────────────
    // Han can't make the jump to hyperspace if the Falcon isn't connected to a
    // live power source — the drive needs a live connection to spool up.
    // We only enable this action when we actually have a browser connection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this action should be enabled.  We need an active
     * {@link IBrowserConnection} — without one there's no directory to search.
     *
     * @return {@code true} if a browser connection is available; {@code false} otherwise
     */
    public boolean isEnabled()
    {
        return getBrowserConnection() != null;
    }


    // ── Han Reads the Coordinates From the Current Display ───────────────────
    // Han checks the navicomputer: is the input the connection itself, or a
    // search result, or a selected entry?  He uses whatever's on screen.
    // We do the same — try multiple sources to find the active connection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Finds the currently active {@link IBrowserConnection} by checking the
     * editor input first, then falling back to selected search results, then
     * selected entries, then selected searches.
     *
     * @return the active browser connection, or {@code null} if none is found
     */
    private IBrowserConnection getBrowserConnection()
    {
        if ( getInput() instanceof IBrowserConnection )
        {
            return ( IBrowserConnection ) getInput();
        }
        else if ( getSelectedSearchResults().length > 0 )
        {
            return getSelectedSearchResults()[0].getEntry().getBrowserConnection();
        }
        else if ( getSelectedEntries().length > 0 )
        {
            return getSelectedEntries()[0].getBrowserConnection();
        }
        else if ( getSelectedSearches().length > 0 )
        {
            return getSelectedSearches()[0].getBrowserConnection();
        }

        return null;
    }


    // ── Han Checks Which Planet They're Jumping To ───────────────────────────
    // Before punching in coordinates, Han verifies the destination — is it an
    // entry the user has highlighted, or is the system choosing the default?
    // We look for a single selected entry to use as the search base.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected entry to use as the quick search base,
     * or {@code null} if no single entry is selected.  If nothing is selected,
     * we fall back to the Root DSE in {@link #run()}.
     *
     * @return the selected {@link IEntry}, or {@code null}
     */
    private IEntry getSelectedEntry()
    {
        if ( getSelectedEntries().length == 1 )
        {
            return getSelectedEntries()[0];
        }

        return null;
    }


    // ── Han Checks If the Hyperdrive Is Already Spooled ──────────────────────
    // Before resetting coordinates, Han glances at the panel: is there already
    // an active quick-jump sequence selected and queued?  No need to overwrite.
    // We check whether the currently selected search is a QuickSearch.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the currently selected search object is a
     * {@link IQuickSearch}.  If so, we'll reuse it rather than creating a new
     * one, which avoids clobbering settings the user already configured.
     *
     * @return {@code true} if the selected search is a quick search;
     *         {@code false} otherwise
     */
    private boolean isQuickSearchSelected()
    {
        if ( getSelectedSearches().length == 1 )
        {
            return getSelectedSearches()[0] instanceof IQuickSearch;
        }

        return false;
    }
}
