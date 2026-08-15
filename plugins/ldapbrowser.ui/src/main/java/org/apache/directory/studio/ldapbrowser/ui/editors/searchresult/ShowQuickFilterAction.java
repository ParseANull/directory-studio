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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: ShowQuickFilterAction — R2 Popping Open the Hidden Access Panel ───
// When R2-D2 approaches the Death Star's tractor beam control panel, there's a
// hidden port he can pop open to interface directly.  It's not always visible —
// only when he needs to plug in.  Once he's done, it retracts.
// This action is R2's toggle: pressing it once pops open the quick-filter widget
// (a text field at the top of the search result table), pressing again retracts it.
// The state is persisted in dialog settings so it survives restarts.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toggle action that shows or hides the quick-filter bar in the search result editor.
 * When activated, it calls {@link SearchResultEditorQuickFilterWidget#setActive(boolean)}
 * to create or destroy the inner composite.  The state is persisted in dialog settings
 * under {@link #SHOW_QUICKFILTER_DIALOGSETTING_KEY} so the widget starts in the
 * correct state on the next Eclipse launch.
 * Also wired to the workbench Find/Replace keybinding
 * ({@link IWorkbenchActionDefinitionIds#FIND_REPLACE}) for keyboard access.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowQuickFilterAction extends Action
{

    /** The dialog settings key used to persist the toggle state across sessions. */
    public static final String SHOW_QUICKFILTER_DIALOGSETTING_KEY = ShowQuickFilterAction.class.getName()
        + ".showQuickFilter"; //$NON-NLS-1$

    /** The quick filter widget. */
    private SearchResultEditorQuickFilterWidget quickFilterWidget;


    // ── R2 Finds the Hidden Panel ─────────────────────────────────────────────
    // R2 is given a reference to the widget he'll toggle.  We read the persisted
    // state from dialog settings (defaulting to hidden if never set) and apply it
    // immediately so the widget starts in the right state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the show/hide quick-filter action connected to the given widget.
     * The initial checked state is read from dialog settings.  If no setting
     * has ever been saved, we default to hidden ({@code false}).
     *
     * @param quickFilterWidget the quick-filter widget to show or hide
     */
    public ShowQuickFilterAction( SearchResultEditorQuickFilterWidget quickFilterWidget )
    {
        super( Messages.getString( "ShowQuickFilterAction.ShowQuickFilter" ), AS_CHECK_BOX ); //$NON-NLS-1$
        super.setToolTipText( Messages.getString( "ShowQuickFilterAction.ShowQuickFilterToolTip" ) ); //$NON-NLS-1$
        super.setImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
            BrowserCommonConstants.IMG_FILTER ) );
        super.setActionDefinitionId( IWorkbenchActionDefinitionIds.FIND_REPLACE );
        super.setEnabled( true );

        this.quickFilterWidget = quickFilterWidget;

        if ( BrowserUIPlugin.getDefault().getDialogSettings().get( SHOW_QUICKFILTER_DIALOGSETTING_KEY ) == null )
        {
            BrowserUIPlugin.getDefault().getDialogSettings().put( SHOW_QUICKFILTER_DIALOGSETTING_KEY, false );
        }
        super.setChecked( BrowserUIPlugin.getDefault().getDialogSettings().getBoolean(
            SHOW_QUICKFILTER_DIALOGSETTING_KEY ) );
        quickFilterWidget.setActive( isChecked() );
    }


    // ── R2 Toggles the Panel ──────────────────────────────────────────────────
    // The toggle logic is inverted here: run() is called with the *old* checked
    // state, so we flip it manually.  We then persist the new state and tell the
    // widget to open or close.
    // Note: setChecked() is overridden to be a no-op, so the only way the state
    // changes is through this run() method.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Toggles the quick-filter widget visibility.
     * We manually flip the checked state (see {@link #setChecked(boolean)} for why),
     * persist it to dialog settings, and notify the widget.
     */
    public void run()
    {
        boolean checked = isChecked();
        super.setChecked( !checked );

        BrowserUIPlugin.getDefault().getDialogSettings().put( SHOW_QUICKFILTER_DIALOGSETTING_KEY, isChecked() );

        if ( quickFilterWidget != null )
        {
            quickFilterWidget.setActive( isChecked() );
        }
    }


    // ── R2 Ignores External Checked State Changes ─────────────────────────────
    // Eclipse sometimes calls setChecked() from outside (e.g. when restoring
    // action state after a perspective switch).  We override it to a no-op
    // because our toggle state is managed entirely inside run().  The only
    // authoritative source of truth is the dialog settings + the run() call.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — toggling the checked state is handled exclusively inside {@link #run()}.
     * This prevents Eclipse from resetting the checked state from outside this class.
     *
     * @param checked ignored
     */
    public void setChecked( boolean checked )
    {
    }


    // ── R2 Unplugs From the Panel ─────────────────────────────────────────────
    // The editor is closing — release the reference to the quick-filter widget.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the reference to the quick-filter widget.
     */
    public void dispose()
    {
        quickFilterWidget = null;
    }

}
