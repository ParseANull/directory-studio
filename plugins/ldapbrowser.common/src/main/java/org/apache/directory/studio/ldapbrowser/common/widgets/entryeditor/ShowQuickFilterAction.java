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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: ShowQuickFilterAction — R2-D2 Toggles the Death Star Search Filters
// In A New Hope, R2-D2 plugs into the Death Star's computer terminal and starts
// toggling search filters across millions of data streams — narrowing down the
// detention block locations, the tractor beam controls, the garbage masher. Each
// toggle of the filter either reveals or hides information. That's exactly what
// this action does: it shows or hides the instant-search bar in the entry editor
// so users can quickly narrow down which LDAP attributes are visible.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toggle action that shows or hides the quick-filter bar at the top of the
 * entry editor. When visible, the quick-filter bar lets users type a search
 * term and instantly see only the attributes whose name or value matches.
 *
 * <p>The checked state (filter shown vs. hidden) is persisted to the plugin's
 * dialog settings so the user's preference survives between sessions. Toggling
 * is handled entirely inside {@link #run()} — the inherited
 * {@link #setChecked(boolean)} is intentionally left as a no-op to prevent the
 * platform from flipping the check mark at the wrong time.</p>
 *
 * <p>Think of this class as R2-D2 at the Death Star terminal, one stubby
 * manipulator on the filter toggle: press it and the search bar appears; press
 * it again and it vanishes.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowQuickFilterAction extends Action
{

    /** The Constant SHOW_QUICKFILTER_DIALOGSETTING_KEY. */
    public static final String SHOW_QUICKFILTER_DIALOGSETTING_KEY = ShowQuickFilterAction.class.getName()
        + ".showQuickFilter"; //$NON-NLS-1$

    /** The quick filter widget. */
    private EntryEditorWidgetQuickFilterWidget quickFilterWidget;


    // ── R2-D2 Plugs In and Reads the Terminal's Last Filter State ───────────────
    // R2 jacks into the Death Star terminal. First he reads what state the filters
    // were in before (persisted dialog settings), then he sets both the toggle and
    // the actual filter widget to match — so the UI is consistent from the start.
    // He also registers the Find/Replace keybinding so Ctrl+F triggers this toggle.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action, sets up its label, tooltip, icon, and keybinding, then
     * restores its checked state from the plugin's dialog settings. If no setting
     * has been saved yet we default to {@code false} (filter hidden). We then push
     * that state to the quick-filter widget so the UI matches from the very first
     * paint.
     *
     * <p>For example — R2 reads the terminal's filter state on connection:</p>
     * <pre>
     *   dialogSettings.get(KEY) == null → put(KEY, false)  (default: hidden)
     *   super.setChecked(dialogSettings.getBoolean(KEY))   → restore saved state
     *   quickFilterWidget.setActive(isChecked())           → sync the widget
     * </pre>
     *
     * @param quickFilterWidget  The actual quick-filter bar widget this action
     *                           shows and hides; must not be {@code null} at
     *                           construction time.
     */
    public ShowQuickFilterAction( EntryEditorWidgetQuickFilterWidget quickFilterWidget )
    {
        super( Messages.getString( "ShowQuickFilterAction.ShowQuickFilter" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "ShowQuickFilterAction.ShowQuickFilter" ) ); //$NON-NLS-1$
        setImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_FILTER ) );
        setActionDefinitionId( IWorkbenchActionDefinitionIds.FIND_REPLACE );
        setEnabled( true );

        this.quickFilterWidget = quickFilterWidget;

        if ( BrowserCommonActivator.getDefault().getDialogSettings().get( SHOW_QUICKFILTER_DIALOGSETTING_KEY ) == null )
        {
            BrowserCommonActivator.getDefault().getDialogSettings().put( SHOW_QUICKFILTER_DIALOGSETTING_KEY, false );
        }

        // call the super implementation here because the local implementation
        // does nothing.
        super.setChecked( BrowserCommonActivator.getDefault().getDialogSettings().getBoolean(
            SHOW_QUICKFILTER_DIALOGSETTING_KEY ) );
        quickFilterWidget.setActive( isChecked() );
    }


    // ── R2-D2 Flips the Filter Toggle on the Terminal ───────────────────────────
    // R2's manipulator arm flips the filter switch: if the filter was on, it goes
    // off; if off, it comes on. He persists the new state to the terminal's memory
    // so the next time he connects, the filter starts in the right position.
    // Then he tells the quick-filter widget to show or hide itself accordingly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Toggles the quick-filter bar on or off. We invert the current checked state,
     * save the new state to the plugin's dialog settings (so it survives session
     * restarts), and then activate or deactivate the quick-filter widget.
     *
     * <p>Note: we use {@code super.setChecked()} here, not our own override, because
     * our {@link #setChecked(boolean)} is deliberately a no-op.</p>
     *
     * <p>For example — R2 toggles the Death Star filter switch:</p>
     * <pre>
     *   wasChecked = isChecked()       → read current state
     *   super.setChecked(!wasChecked)  → flip the toggle mark
     *   dialogSettings.put(KEY, ...)   → persist the new state
     *   quickFilterWidget.setActive(isChecked()) → show or hide the bar
     * </pre>
     */
    public void run()
    {
        boolean checked = isChecked();
        super.setChecked( !checked );

        BrowserCommonActivator.getDefault().getDialogSettings().put( SHOW_QUICKFILTER_DIALOGSETTING_KEY, isChecked() );

        if ( quickFilterWidget != null )
        {
            quickFilterWidget.setActive( isChecked() );
        }
    }


    // ── R2-D2 Ignores External Override Commands ─────────────────────────────────
    // Stormtroopers try to override R2's terminal session remotely, but R2 ignores
    // the command — the toggle is only flipped from inside run(). If we allowed
    // setChecked() to work normally the platform would flip the check mark
    // independently of our state management, causing the toggle and the widget to
    // fall out of sync.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Intentionally does nothing. The SWT/JFace framework sometimes calls
     * {@code setChecked()} on toggle actions automatically, but we need full
     * control over when the checked state changes — which is only inside
     * {@link #run()}. Overriding this as a no-op prevents the platform from
     * flipping the check mark at the wrong moment.
     *
     * <p>For example — R2 ignores the remote override:</p>
     * <pre>
     *   external call: setChecked(true)  → R2 beeps and does nothing
     *   // State only changes via run() where we manage the full sequence
     * </pre>
     *
     * @param checked  The requested checked state; ignored.
     */
    public void setChecked( boolean checked )
    {
    }


    // ── R2-D2 Unplugs from the Terminal ─────────────────────────────────────────
    // Mission accomplished. R2 retracts his manipulator arm, the interface
    // disconnects, and he rolls away. We null the widget reference so this action
    // no longer holds the widget alive after the editor has been closed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the reference to the quick-filter widget, allowing it to be
     * garbage-collected. Call this when the entry editor widget is closing to
     * prevent a stale reference from keeping the filter bar in memory.
     *
     * <p>For example — R2 retracts and disconnects cleanly:</p>
     * <pre>
     *   quickFilterWidget = null  → release the widget reference
     *   // The widget is now free to be collected
     * </pre>
     */
    public void dispose()
    {
        quickFilterWidget = null;
    }

}
