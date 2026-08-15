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
package org.apache.directory.studio.schemaeditor.controller;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenSearchViewPreferenceAction;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenSearchViewSortingDialogAction;
import org.apache.directory.studio.schemaeditor.controller.actions.RunCurrentSearchAgainAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ShowSearchFieldAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ShowSearchHistoryAction;
import org.apache.directory.studio.schemaeditor.view.views.SearchView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


// ── CLASS: SearchViewController — R2-D2 PLUGS INTO THE DEATH STAR COMPUTER ──
// R2-D2 trundles across the Death Star's corridor, extends his data probe,
// and jacks into the station's computer — pulling up schematics, maps, and
// records in seconds flat.  This controller does the same for the Schema
// Editor's Search View: it wires up the search field, the search-history
// replay button, sorting options, and the preference listener so the view
// stays snappy and responsive the moment the user starts querying.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Controller for the Search View in the Schema Editor.
 * We initialize the toolbar, menu, authorized preferences list, and the
 * preference-change listener so the view refreshes whenever relevant settings
 * change.
 * Think of this class as R2-D2 at the Death Star terminal — we plug in once
 * and keep the search interface responsive for as long as the view is open.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchViewController
{
    /** The associated view */
    private SearchView view;

    /** The authorized Preferences keys*/
    private List<String> authorizedPrefs;

    // The Actions
    private ShowSearchFieldAction showSearchField;
    private RunCurrentSearchAgainAction runCurrentSearchAgain;
    private ShowSearchHistoryAction searchHistory;
    private OpenSearchViewSortingDialogAction openSearchViewSortingDialog;
    private OpenSearchViewPreferenceAction openSearchViewPreference;


    // ── R2 Extends His Data Probe And Connects ────────────────────────────────
    // R2-D2 inserts his data probe into the Death Star's socket, initializes
    // the connection, and immediately starts pulling up the schematics he needs.
    // We do the same: wire up all five actions, the toolbar, the menu, the
    // authorized-prefs list, and the preference listener in one constructor call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the controller and fully initializes the SearchView.
     * We set up all actions, the toolbar, the drop-down menu, the authorized
     * preferences allowlist, and the preference-change listener in sequence
     * so the view is immediately ready when it first opens.
     *
     * <p>For example — R2 doesn't initialize half a connection and wait:</p>
     * <pre>
     *   controller = new SearchViewController( view );
     *   // Toolbar live, menu live, preference listener active
     * </pre>
     *
     * @param view  the SearchView we are controlling; must not be null
     */
    public SearchViewController( SearchView view )
    {
        this.view = view;

        initActions();
        initToolbar();
        initMenu();
        initAuthorizedPrefs();
        initPreferencesListener();
    }


    // ── R2 Loads His Search Utilities ────────────────────────────────────────
    // R2 rapidly loads the schematics utility, the history retrieval program,
    // the sorting subroutine, and the settings module into active memory before
    // the first query arrives.
    // We instantiate all five search-related actions so toolbar and menu
    // have live, configured handlers ready to go.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Instantiates all Action objects used by this view's toolbar and drop-down menu.
     * We create them before any UI element queries their enabled state or label.
     *
     * <p>For example — R2 loads every subroutine before the first query fires:</p>
     * <pre>
     *   showSearchField     = new ShowSearchFieldAction( view );
     *   runCurrentSearchAgain = new RunCurrentSearchAgainAction( view );
     *   searchHistory       = new ShowSearchHistoryAction( view );
     *   // ... and the two dialog/preference actions
     * </pre>
     */
    private void initActions()
    {
        showSearchField = new ShowSearchFieldAction( view );
        runCurrentSearchAgain = new RunCurrentSearchAgainAction( view );
        searchHistory = new ShowSearchHistoryAction( view );
        openSearchViewSortingDialog = new OpenSearchViewSortingDialogAction();
        openSearchViewPreference = new OpenSearchViewPreferenceAction();
    }


    // ── R2 Lays Out His Control Buttons ──────────────────────────────────────
    // R2 arranges his primary controls on the face of the terminal interface —
    // the search field toggle front and center, the re-run and history buttons
    // just beside it, separated so they're clearly a different kind of action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the view toolbar with the search field toggle, re-run, and history actions.
     * A separator visually groups the toggle from the re-run and history buttons.
     *
     * <p>For example — R2's most-used controls are right on the terminal face:</p>
     * <pre>
     *   toolbar: [ showSearchField | --- | runCurrentSearchAgain | searchHistory ]
     * </pre>
     */
    private void initToolbar()
    {
        IToolBarManager toolbar = view.getViewSite().getActionBars().getToolBarManager();
        toolbar.add( showSearchField );
        toolbar.add( new Separator() );
        toolbar.add( runCurrentSearchAgain );
        toolbar.add( searchHistory );
    }


    // ── R2 Pulls Up The Options Panel ────────────────────────────────────────
    // R2 opens a secondary panel on the terminal that shows sorting options
    // and system settings — everything the operator might want to adjust
    // without interrupting the main search display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the view's drop-down menu with the sorting dialog and preferences actions.
     * A separator makes it clear that sorting and preferences are distinct concerns.
     *
     * <p>For example — R2's options panel is always one toggle away:</p>
     * <pre>
     *   menu: [ openSortingDialog | --- | openSearchViewPreference ]
     * </pre>
     */
    private void initMenu()
    {
        IMenuManager menu = view.getViewSite().getActionBars().getMenuManager();
        menu.add( openSearchViewSortingDialog );
        menu.add( new Separator() );
        menu.add( openSearchViewPreference );
    }


    // ── R2 Notes Which System Flags Are Worth Watching ───────────────────────
    // R2 quickly scans the Death Star's status board and catalogues exactly
    // which system flags he needs to monitor — not every blinking light,
    // just the ones that affect the search display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the allowlist of preference keys that should trigger a search view refresh.
     * We include only prefs that genuinely affect how search results are rendered —
     * label format, grouping, sorting — and ignore everything else.
     *
     * <p>For example — R2 monitors only the status lights that matter to his mission:</p>
     * <pre>
     *   authorizedPrefs = [ LABEL, ABBREVIATE, MAX_LENGTH, SECONDARY_LABEL_DISPLAY,
     *                        SECONDARY_LABEL, SECONDARY_LABEL_ABBREVIATE, ...,
     *                        GROUPING, SORTING_BY, SORTING_ORDER ]
     * </pre>
     */
    private void initAuthorizedPrefs()
    {
        authorizedPrefs = new ArrayList<String>();
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_LABEL );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE_MAX_LENGTH );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_DISPLAY );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_SCHEMA_LABEL_DISPLAY );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_GROUPING );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY );
        authorizedPrefs.add( PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER );
    }


    // ── R2 Reacts The Instant A System Flag Changes ───────────────────────────
    // R2 has set up an interrupt routine: the moment one of his monitored
    // status flags flips, he immediately re-renders the display — no delay,
    // no manual refresh required.
    // We attach a preference-change listener that calls view.refresh() the
    // instant any authorized preference changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches a preference-change listener to the plugin's preference store.
     * Only preferences in our authorized list trigger a refresh — all others
     * are ignored to avoid unnecessary redraws of search results.
     *
     * <p>For example — R2's interrupt routine fires the instant a relevant flag changes:</p>
     * <pre>
     *   preferenceStore.addPropertyChangeListener( ... );
     *   // PREFS_SEARCH_VIEW_LABEL change → view.refresh()
     *   // PREFS_SOME_OTHER_PREF  change → ignored
     * </pre>
     */
    private void initPreferencesListener()
    {
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener()
        {
            /**
             * {@inheritDoc}
             */
            public void propertyChange( PropertyChangeEvent event )
            {
                if ( authorizedPrefs.contains( event.getProperty() ) )
                {
                    view.refresh();
                }
            }
        } );
    }
}
