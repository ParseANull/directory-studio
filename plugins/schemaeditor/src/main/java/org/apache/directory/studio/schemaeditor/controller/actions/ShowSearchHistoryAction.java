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
package org.apache.directory.studio.schemaeditor.controller.actions;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.dialogs.PreviousSearchesDialog;
import org.apache.directory.studio.schemaeditor.view.search.SearchPage;
import org.apache.directory.studio.schemaeditor.view.search.SearchPage.SearchInEnum;
import org.apache.directory.studio.schemaeditor.view.views.SearchView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: ShowSearchHistoryAction — Luke Scanning The Horizon With His Macrobinoculars ──────
// On Tatooine, Luke sweeps the macrobinoculars across the horizon, recalling everywhere
// he's already looked — each previous scan listed, the most recent highlighted.
// This action opens a drop-down of past searches and a full history dialog, so the user
// can jump back to any query they've already run without typing it again.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * An action that shows the search history — either as a drop-down menu of recent searches
 * or as a full {@link PreviousSearchesDialog} when clicked directly.
 * It's a drop-down-menu action, so clicking the arrow expands the history list while
 * clicking the icon body opens the full dialog.
 * Think of this as Luke sweeping his macrobinoculars across every bearing he's already
 * surveyed, with one-click re-runs for each.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowSearchHistoryAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private SearchView view;


    // ── Luke Raises The Macrobinoculars And Wires The History Dropdown ────────────────────────
    // Luke grabs his macrobinoculars and flips to "recall mode," where each previous
    // sighting is queued in the viewfinder.  We attach a MenuCreator so the dropdown
    // renders the history list each time the user clicks the arrow.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowSearchHistoryAction wired to the given SearchView.
     * We set the style to {@code AS_DROP_DOWN_MENU} so Eclipse renders an arrow beside
     * the button, and we attach a {@link MenuCreator} that builds the history list on demand.
     *
     * <p>For example — Luke flips to recall mode:</p>
     * <pre>
     *   Luke: "What did I scan before?"
     *   MenuCreator assembles the list → each previous search becomes a menu item
     * </pre>
     *
     * @param view  the SearchView whose search history we will display
     */
    public ShowSearchHistoryAction( SearchView view )
    {
        super( Messages.getString( "ShowSearchHistoryAction.SearchHistoryAction" ), AS_DROP_DOWN_MENU ); //$NON-NLS-1$
        this.view = view;
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SHOW_SEARCH_HISTORY ) );
        setEnabled( true );
        setMenuCreator( new MenuCreator( view ) );
    }


    // ── Luke Opens The Full Galactic Log ──────────────────────────────────────────────────────
    // When Luke presses the body of the button (not the arrow), he wants the full
    // catalog — not just the recent shortlist.  We open the PreviousSearchesDialog
    // so he can browse the complete history and re-run any entry.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link PreviousSearchesDialog}, which shows the complete search history
     * and lets the user select and re-run any past search.
     * This fires when the user clicks the action's icon directly (not the dropdown arrow).
     *
     * <p>For example — Luke opens the full mission log:</p>
     * <pre>
     *   Luke clicks icon → PreviousSearchesDialog opens
     *   Full history listed → Luke picks a previous search to rerun
     * </pre>
     */
    public void run()
    {
        PreviousSearchesDialog dialog = new PreviousSearchesDialog( view );
        dialog.open();
    }


    // ── Luke Passes The Signal Through The Workbench Relay ───────────────────────────────────
    // When the workbench delegate path fires, Luke treats it as the same "open full log"
    // command and delegates straight to run().
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when triggered via the workbench action delegate path.
     *
     * @param action  the workbench action proxy; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Luke Lowers The Macrobinoculars, No Cleanup Needed ───────────────────────────────────
    // He sets them down cleanly — no subscriptions, no listeners to remove.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes this action's resources.
     * We hold nothing requiring explicit cleanup, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Luke Notes His Window Assignment ─────────────────────────────────────────────────────
    // The workbench window is noted but we need nothing from it — all context came
    // through the constructor.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is bound to a workbench window.
     * We have everything we need from the constructor, so this is empty.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Luke Keeps His Position Regardless Of Other Selections ───────────────────────────────
    // This action doesn't change based on what's selected elsewhere in the workbench —
    // search history is always accessible.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * This action is always enabled regardless of selection, so this is intentionally empty.
     *
     * @param action     the workbench action proxy; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}

// ── CLASS: MenuCreator — Luke's Viewfinder Assembling The Previous Sightings List ────────────
// Luke sweeps the viewfinder through each previous scan direction, lining them up
// as quick-select entries.  If the log is empty, the viewfinder shows "(None)."
// A "full log" option and a "clear log" option sit at the bottom of every drop-down.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * An {@link IMenuCreator} that builds the search-history dropdown menu on demand.
 * Each time the user clicks the dropdown arrow, this class queries the saved search history,
 * creates a menu item per entry, and appends "History..." and "Clear History" at the bottom.
 * Think of this as Luke's viewfinder recall mode — every previous bearing shows up in the list.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
class MenuCreator implements IMenuCreator
{
    /** The menu */
    private Menu menu;

    /** The associated view */
    private SearchView view;


    // ── Luke Picks Up The Viewfinder With The Right Console ──────────────────────────────────
    // He grabs the viewfinder unit that's paired with this particular search console,
    // so re-run selections land in the right view.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new MenuCreator paired with the given SearchView.
     * We hold the view reference so we can call {@code setSearchInput} when the user
     * picks a history entry.
     *
     * @param view  the SearchView that will receive the re-run request when a history item is clicked
     */
    public MenuCreator( SearchView view )
    {
        this.view = view;
    }


    // ── Luke Cleans Up The Viewfinder Between Uses ───────────────────────────────────────────
    // Each time the menu is closed, we dispose of the SWT Menu widget and null the reference
    // so we don't leak native handles.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the SWT {@link Menu} created by this creator, preventing native resource leaks.
     * We null the reference afterwards so stale disposal doesn't happen twice.
     */
    public void dispose()
    {
        if ( menu != null )
        {
            menu.dispose();
            menu = null;
        }
    }


    // ── Luke Sweeps The Viewfinder And Lists Every Previous Bearing ───────────────────────────
    // Luke scrolls through the history log and adds each previous search as a radio-button
    // menu item.  The currently active search is pre-selected.  "None" appears when the log
    // is empty, and "History..." / "Clear History" sit at the bottom of every build.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the drop-down {@link Menu} anchored to the given control.
     * We load the saved search string history, create one radio {@link MenuItem} per entry
     * (pre-selecting the one that matches the view's current search string), then append
     * a separator and "History..." / "Clear History" push items.
     *
     * <p>For example — Luke's viewfinder shows three previous bearings:</p>
     * <pre>
     *   ● "cn=Luke"       ← currently active (pre-selected)
     *   ○ "objectClass=*"
     *   ○ "mail=*@rebel*"
     *   ─────────────────
     *   History...
     *   Clear History
     * </pre>
     *
     * @param parent  the SWT control the menu will be attached to; used to construct the Menu
     * @return        the fully-built SWT menu ready for display
     */
    public Menu getMenu( Control parent )
    {
        menu = new Menu( parent );

        // Previous searches
        String[] previousSearches = SearchPage.loadSearchStringHistory();
        for ( final String search : previousSearches )
        {
            MenuItem item = new MenuItem( menu, SWT.RADIO );
            item.setText( search );
            item.setImage( Activator.getDefault().getImage( PluginConstants.IMG_SEARCH_HISTORY_ITEM ) );
            item.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    view.setSearchInput( search, SearchPage.loadSearchIn().toArray( new SearchInEnum[0] ), SearchPage
                        .loadScope() );
                }
            } );
            if ( search.equals( view.getSearchString() ) )
            {
                item.setSelection( true );
            }
        }

        // No search history
        if ( previousSearches.length == 0 )
        {
            MenuItem item = new MenuItem( menu, SWT.RADIO );
            item.setText( Messages.getString( "ShowSearchHistoryAction.None" ) ); //$NON-NLS-1$
            item.setEnabled( false );
            item.setSelection( true );
        }

        // Menu Separator
        new MenuItem( menu, SWT.SEPARATOR );

        MenuItem item = new MenuItem( menu, SWT.PUSH );
        item.setText( Messages.getString( "ShowSearchHistoryAction.History" ) ); //$NON-NLS-1$
        item.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                PreviousSearchesDialog dialog = new PreviousSearchesDialog( view );
                dialog.open();
            }
        } );
        item = new MenuItem( menu, SWT.PUSH );
        item.setText( Messages.getString( "ShowSearchHistoryAction.ClearHistory" ) ); //$NON-NLS-1$
        item.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                SearchPage.clearSearchHistory();
            }
        } );

        return menu;
    }


    // ── Luke Can't Build A Sub-Viewfinder Inside Another Viewfinder ──────────────────────────
    // The nested-menu variant isn't supported in this implementation — we simply return null.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns null — we don't support building a submenu inside a parent {@link Menu}.
     * Only the {@link Control}-based variant is used by Eclipse for toolbar dropdown menus.
     *
     * @param parent  the parent menu; unused
     * @return        always null
     */
    public Menu getMenu( Menu parent )
    {
        return null;
    }
}
