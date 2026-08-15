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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: ShowQuickSearchAction — R2-D2 at the Death Star Terminal ───────────
// In A New Hope, Luke asks: "R2, are you able to access the computer?" R2 plugs
// into the Death Star's terminal, queries the station systems, and displays the
// results right there. But he can also disconnect — the interface is toggleable.
// ShowQuickSearchAction is that toggle: it shows or hides the quick-search bar
// in the browser view, persists the choice in preferences, and tells the
// BrowserQuickSearchWidget to activate or deactivate itself accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toggle {@link Action} that shows or hides the quick-search widget above
 * the browser tree.
 * The checked state is persisted in the Eclipse preference store under
 * {@code PREFERENCE_BROWSER_SHOW_QUICK_SEARCH} so the user's choice survives
 * across restarts. It's also wired to the workbench "Find/Replace" key binding
 * so pressing Ctrl+F in the browser view toggles the search bar.
 * Think of this action as R2's interface plug: in means the terminal is active,
 * out means it's hidden.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowQuickSearchAction extends Action
{

    /** The quick search widget. */
    private BrowserQuickSearchWidget quickSearchWidget;


    // ── R2 PLUGS INTO THE TERMINAL AND READS CURRENT STATUS ───────────────────
    // The moment R2 connects to the Death Star terminal he doesn't wait for
    // orders — he immediately reads the current status: "Is the search interface
    // already active?" He checks the preference store, sets his own checked
    // state to match, and calls run() to make the UI reflect reality right away.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the action as a checkbox-style toggle and immediately
     * syncs its checked state with the persisted preference.
     * We call {@link #run()} at the end of construction so the widget's
     * visibility matches the stored preference from the very first render —
     * no flicker, no mismatch.
     *
     * <p>For example — R2 plugs in and checks the terminal state:</p>
     * <pre>
     *   setChecked(preferenceStore.getBoolean(PREFERENCE_SHOW_QUICK_SEARCH));
     *   run();  // apply immediately so UI matches the stored setting
     * </pre>
     *
     * @param quickSearchWidget  The {@link BrowserQuickSearchWidget} whose
     *                           visibility this action controls. We call
     *                           {@code setActive()} on it when toggled.
     */
    public ShowQuickSearchAction( BrowserQuickSearchWidget quickSearchWidget )
    {
        super( Messages.getString( "ShowQuickSearchAction.ShowQuickSearch" ), IAction.AS_CHECK_BOX ); //$NON-NLS-1$
        this.quickSearchWidget = quickSearchWidget;
        setActionDefinitionId( IWorkbenchActionDefinitionIds.FIND_REPLACE );
        setEnabled( true );
        setChecked( BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_QUICK_SEARCH ) );
        run();
    }


    // ── R2 TOGGLES THE SEARCH INTERFACE ON OR OFF ─────────────────────────────
    // "R2, shut down all the garbage mashers on the detention level!" Luke gives
    // the command and R2 flips the switch — on or off depending on what Luke
    // asked. Here, the user's toolbar click flips the checked state, we save
    // it to the preference store (so it persists), and then we tell the widget
    // to show or hide itself to match.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current checked state to the preference store and applies it
     * to the quick-search widget.
     * Called by Eclipse when the user clicks the toggle button or presses the
     * bound keyboard shortcut (Ctrl+F by default in the browser view).
     * Writing to the preference store here means the setting survives an IDE
     * restart without any extra save step.
     */
    @Override
    public void run()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().setValue(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_QUICK_SEARCH, isChecked() );

        if ( quickSearchWidget != null )
        {
            quickSearchWidget.setActive( isChecked() );
        }
    }


    // ── R2 UNPLUGS FROM THE TERMINAL ──────────────────────────────────────────
    // When the mission is over R2 retracts his interface arm from the terminal.
    // We null out the widget reference so this action can be garbage collected
    // cleanly — holding onto the widget reference after the browser is disposed
    // would cause a memory leak (and a lot of confused beeping).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases the reference to the quick-search widget so this action and
     * the widget can both be garbage collected when the browser is closed.
     * Call this when the owning browser widget is being disposed.
     */
    public void dispose()
    {
        quickSearchWidget = null;
    }

}
