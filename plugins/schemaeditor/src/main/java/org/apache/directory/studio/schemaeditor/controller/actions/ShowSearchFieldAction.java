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
import org.apache.directory.studio.schemaeditor.view.views.SearchView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: ShowSearchFieldAction — Luke Raising Or Lowering His Macrobinoculars ──────────────
// On Tatooine, Luke stands at the edge of the desert and raises his macrobinoculars —
// the moment he lifts them to his eyes the big picture snaps into focus; when he lowers
// them the view collapses back.  This toggle action shows or hides the search input field
// in the SearchView, and persists the user's preference across sessions via dialog settings.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A toggle action that shows or hides the search field section inside the {@link SearchView}.
 * It persists its checked state to Eclipse's dialog settings so the panel stays in the
 * same state the next time the workbench opens.
 * Think of this class as Luke's macrobinoculars toggle — raise them to reveal the search
 * horizon, lower them to tuck it away.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowSearchFieldAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The String for storing the checked state of the action */
    private static final String SHOW_SEARCH_FIELD_DS_KEY = ShowSearchFieldAction.class.getName() + ".dialogsettingkey"; //$NON-NLS-1$

    /** The associated view */
    private SearchView view;


    // ── Luke Picks Up The Macrobinoculars For The First Time ─────────────────────────────────
    // Luke reaches for the macrobinoculars on the shelf.  He checks whether he left them
    // raised or lowered last time (dialog settings), restores that state, and immediately
    // applies it so the view matches what he remembers.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowSearchFieldAction, restoring the last-known show/hide state from dialog settings.
     * If no saved state exists yet we default to hidden (false).  We also immediately apply
     * the restored state to the view so the UI is consistent from the moment the action is created.
     *
     * <p>For example — Luke restores the macrobinoculars to where he left them:</p>
     * <pre>
     *   Luke: "Were they raised or lowered last time?"
     *   Dialog settings: "Lowered (false)."
     *   Luke lowers macrobinoculars → view.hideSearchFieldSection()
     * </pre>
     *
     * @param view  the SearchView whose search field section this action shows or hides
     */
    public ShowSearchFieldAction( SearchView view )
    {
        super( Messages.getString( "ShowSearchFieldAction.ShowSearchFieldAction" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SHOW_SEARCH_FIELD ) );
        setEnabled( true );
        this.view = view;

        // Setting up the default key value (if needed)
        if ( Activator.getDefault().getDialogSettings().get( SHOW_SEARCH_FIELD_DS_KEY ) == null )
        {
            Activator.getDefault().getDialogSettings().put( SHOW_SEARCH_FIELD_DS_KEY, false );
        }

        // Setting state from the dialog settings
        setChecked( Activator.getDefault().getDialogSettings().getBoolean( SHOW_SEARCH_FIELD_DS_KEY ) );

        if ( isChecked() )
        {
            view.showSearchFieldSection();
        }
        else
        {
            view.hideSearchFieldSection();
        }
    }


    // ── Luke Toggles The Macrobinoculars Up Or Down ───────────────────────────────────────────
    // Luke raises or lowers the macrobinoculars based on the current toggle state,
    // then records the new position in dialog settings so it survives a restart.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Toggles the search field section visible or hidden and persists the new state.
     * We read the current checked state, save it to dialog settings, then tell the view
     * to show or hide the section accordingly.
     *
     * <p>For example — Luke decides to raise the macrobinoculars:</p>
     * <pre>
     *   Toggle → checked = true
     *   Dialog settings saved: true
     *   view.showSearchFieldSection() → search bar appears
     * </pre>
     */
    public void run()
    {
        setChecked( isChecked() );
        Activator.getDefault().getDialogSettings().put( SHOW_SEARCH_FIELD_DS_KEY, isChecked() );

        if ( isChecked() )
        {
            view.showSearchFieldSection();
        }
        else
        {
            view.hideSearchFieldSection();
        }
    }


    // ── Luke Responds To The Relay Signal From The Cockpit ───────────────────────────────────
    // When someone triggers the action through the workbench delegate path, Luke treats it
    // identically to flipping the toggle himself — same movement, same effect.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when triggered via the workbench action delegate path.
     * The {@code action} parameter is Eclipse's proxy; we ignore it and call run() directly.
     *
     * @param action  the workbench action proxy; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Luke Sets Down The Macrobinoculars, No Cleanup Required ──────────────────────────────
    // He sets them gently on the shelf — nothing to release, nothing to unsubscribe.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes resources held by this action.
     * We don't allocate anything requiring explicit cleanup, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Luke Notes The Window Assignment But Needs No Briefing ───────────────────────────────
    // The workbench tells Luke which window he's in; he already knows everything he needs
    // from the constructor, so there's nothing to set up here.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is bound to a workbench window.
     * We have everything we need from the constructor, so this is intentionally empty.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Luke Ignores The General Selection Broadcast ──────────────────────────────────────────
    // This toggle doesn't change based on what's selected elsewhere — Luke keeps his
    // macrobinoculars in whatever position he last put them, regardless of other activity.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * This action's enabled/checked state is independent of selection, so this is empty.
     *
     * @param action     the workbench action proxy; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
