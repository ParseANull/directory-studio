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

package org.apache.directory.studio.ldapbrowser.ui.views.searchlogs;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: ClearAction — OBI-WAN DISABLES THE DEATH STAR TRACTOR BEAM ────────
// Obi-Wan slips through the corridors of the Death Star, reaches the tractor
// beam generator, and — with calm precision — flips the switch. The beam
// stops. No fanfare, just gone. This action does the same for the search log
// files: it walks up to the logger, disposes every log file for the current
// connection, and blanks the text widget. Gone. Clean. The ship can leave.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Deletes all search log files for the currently displayed connection
 * and clears the search logs view.
 * Log files accumulate on disk and in the view; this action gives the user
 * a one-click way to wipe them out when they're no longer needed.
 * Think of Obi-Wan disabling the tractor beam: one decisive action and the
 * hold that the logs had on disk space is released.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ClearAction extends BrowserAction
{

    /** The search logs view. */
    private SearchLogsView view;


    // ── Obi-Wan Locates the Generator Room ───────────────────────────────────────
    // Obi-Wan memorises which tractor beam generator he's targeting before he
    // starts his walk through the Death Star corridors.
    // We store the view reference so we can reach the universal listener
    // that owns the log file deletion logic.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ClearAction bound to the given search logs view.
     * We need the view reference so we can call its universal listener's
     * {@code clearInput()} method when the user confirms the deletion.
     *
     * @param view  the search logs view whose log files we'll delete on run.
     */
    public ClearAction( SearchLogsView view )
    {
        this.view = view;
    }


    // ── Obi-Wan Has No Special Keybinding ────────────────────────────────────────
    // The tractor beam disable switch doesn't have a shortcut key on the Death Star —
    // you have to walk there and flip it manually.
    // This action likewise has no Eclipse command ID; it's triggered by toolbar
    // button only.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} because this action has no associated Eclipse command ID.
     * It is triggered via its toolbar button only, not via keyboard shortcut.
     *
     * @return  {@code null}.
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── Obi-Wan Reaches for the Switch ──────────────────────────────────────────
    // The generator room is dark but Obi-Wan knows exactly what the switch looks like.
    // We return the trash-can icon so the button is immediately recognisable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the "clear" icon for the toolbar button.
     *
     * @return  the image descriptor for the clear/delete icon.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_CLEAR );
    }


    // ── Obi-Wan Reads the Generator's Label ─────────────────────────────────────
    // Even in the dark, Obi-Wan reads the label on the generator — he wants
    // to be sure he has the right one.
    // We return the localised label text for the menu or tooltip.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised display name for this action (e.g. "Clear").
     *
     * @return  the localised action label.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "ClearAction.Clear" ); //$NON-NLS-1$
    }


    // ── Obi-Wan Checks He Can Actually Reach the Generator ───────────────────────
    // Obi-Wan makes sure the path to the generator room isn't blocked and that
    // the room actually contains a generator worth disabling.
    // We enable the action only when there is a valid search log input to clear.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Enables the action only when the view currently shows a {@link SearchLogsViewInput}.
     * Without a valid input there are no log files to delete, so running would
     * be a no-op at best.
     *
     * @return  {@code true} if the current input is a SearchLogsViewInput.
     */
    @Override
    public boolean isEnabled()
    {
        return getInput() instanceof SearchLogsViewInput;
    }


    // ── Obi-Wan Flips the Switch ─────────────────────────────────────────────────
    // Obi-Wan pauses just long enough to be sure, then flips the switch. The
    // beam dies. He turns and walks back, mission complete.
    // We confirm with the user first (the beam is important — deleting logs
    // is irreversible), then call clearInput() and refresh the view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Prompts the user for confirmation, then deletes all search log files for
     * the current connection and clears the view's text widget.
     * We ask first because deletion is irreversible — the log files can't be
     * recovered once gone.
     */
    @Override
    public void run()
    {
        if ( MessageDialog.openConfirm( this.getShell(),
            Messages.getString( "ClearAction.Delete" ), Messages.getString( "ClearAction.DeleteAllLogFiles" ) ) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
            view.getUniversalListener().clearInput();
            new RefreshAction( view ).run();
        }
    }


    // ── Obi-Wan Exits the Generator Room ─────────────────────────────────────────
    // After the mission is done, Obi-Wan releases his focus on the generator
    // and moves on — he doesn't hold onto it.
    // We delegate cleanup to the superclass.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Releases any resources held by this action. Delegates to the superclass
     * implementation which cleans up the input reference.
     */
    @Override
    public void dispose()
    {
        super.dispose();
    }

}
