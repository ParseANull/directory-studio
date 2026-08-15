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

package org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: NewerAction — LUKE STEPS FORWARD ON HIS HERO'S JOURNEY ────────────
// Luke's hero journey moves relentlessly forward: from Tatooine to Yavin,
// from Hoth to Dagobah, each step brings him closer to the present moment.
// When he steps forward he is moving toward the most recent events.
// This action does the same: it decrements the log-file index (moving toward
// index 0, the newest file) and reloads the view with that more-recent log.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toolbar action that moves the modification logs view to the next-newer
 * log file in the rotating set.
 * Log files are indexed from 0 (newest) upward; decrementing the index moves
 * toward more-recent modifications. This action is only enabled when the
 * current index is greater than 0 (i.e., we're not already on the newest file).
 * Think of Luke stepping forward on his journey — each click brings us closer
 * to the most recent LDAP operations.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewerAction extends BrowserAction
{

    /** The modification logs view. */
    private ModificationLogsView view;


    // ── Constructor: Luke Takes the Next Step Toward Yavin ───────────────────
    // Luke hasn't left Tatooine yet, but he packs his bag and prepares —
    // he's holding the destination in mind, waiting for the moment to act.
    // We store the view reference so run() can push the new input when triggered.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this action with a reference to the modification logs view.
     * We store the view so {@link #run()} can update its universal listener
     * with the new (decremented) log-file index.
     *
     * <p>For example — Luke packs his bag, ready to step toward Yavin:</p>
     * <pre>
     *   NewerAction action = new NewerAction( modificationLogsView );
     *   // action holds the view reference, ready to navigate on run()
     * </pre>
     *
     * @param view  the modification logs view to navigate when this action runs
     */
    public NewerAction( ModificationLogsView view )
    {
        this.view = view;
    }


    // ── dispose: Luke Completes This Stage of the Journey ────────────────────
    // Luke arrives at his destination and this chapter of the journey closes —
    // he passes the baton to the Force and lets the scene conclude.
    // We delegate to super; no extra resources to release here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action.
     * We delegate entirely to {@link BrowserAction#dispose()}.
     *
     * <p>For example — Luke completes this stage and moves on:</p>
     * <pre>
     *   action.dispose(); // super handles cleanup
     * </pre>
     */
    @Override
    public void dispose()
    {
        super.dispose();
    }


    // ── run: Luke Strides Forward One Step on the Journey ────────────────────
    // "Stay on target." Luke pushes the throttle and moves one beat forward —
    // past Hoth, past the asteroid field, one step closer to the present.
    // We build a new input with index decremented by 1 (toward 0 = newest),
    // set it on the listener, and scroll to the oldest entry in the new file.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Navigates to the next-newer log file by decrementing the current index.
     * We construct a new {@link ModificationLogsViewInput} with
     * {@code oldIndex - 1}, push it to the universal listener, and scroll to
     * the top of the newly loaded file so the user starts reading from the oldest
     * entry in that newer volume.
     *
     * <p>For example — Luke strides one step closer to Yavin:</p>
     * <pre>
     *   int newerIndex = oldInput.getIndex() - 1; // toward 0 = newest
     *   ModificationLogsViewInput newInput = new ModificationLogsViewInput( conn, newerIndex );
     *   view.getUniversalListener().setInput( newInput );
     *   view.getUniversalListener().scrollToOldest(); // start at top of newer file
     * </pre>
     */
    @Override
    public void run()
    {
        ModificationLogsViewInput oldInput = ( ModificationLogsViewInput ) getInput();
        ModificationLogsViewInput newInput = new ModificationLogsViewInput( oldInput.getBrowserConnection(), oldInput
            .getIndex() - 1 );
        view.getUniversalListener().setInput( newInput );
        view.getUniversalListener().scrollToOldest();

        // go to top
        view.getMainWidget().getSourceViewer().setTopIndex( 0 );
    }


    // ── getText: Luke Announces "Forward!" ────────────────────────────────────
    // Luke rallies the Rebellion: "Forward!" — the word that signals progress
    // and movement toward the goal.
    // We return the localized "Newer" label for the toolbar button.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action.
     * Shown as the toolbar tooltip text.
     *
     * <p>For example — Luke calls "Forward!" to rally the Rebels:</p>
     * <pre>
     *   String label = action.getText(); // "Newer" (localized)
     * </pre>
     *
     * @return  the localized "Newer" label string
     */
    @Override
    public String getText()
    {
        return Messages.getString( "NewerAction.Newer" ); //$NON-NLS-1$
    }


    // ── getImageDescriptor: Luke's X-Wing Points Forward ─────────────────────
    // Luke's X-wing is angled forward, engines at full throttle — the visual
    // cue that we're moving toward the next destination.
    // We return the "next" icon from the plugin's image registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for the "Newer" toolbar button.
     * We use the "next" (forward arrow) image from the plugin's image registry.
     *
     * <p>For example — Luke's X-wing points toward the next waypoint:</p>
     * <pre>
     *   ImageDescriptor img = action.getImageDescriptor(); // forward-arrow icon
     * </pre>
     *
     * @return  the {@link ImageDescriptor} for the next/forward icon
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_NEXT );
    }


    // ── getCommandId: No Special Rebel Alliance Command Code ─────────────────
    // Luke doesn't need a special Rebel Alliance ID code for this maneuver —
    // he just acts directly on the Force's guidance.
    // We return null because this action isn't bound to an Eclipse command.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action — always null here since
     * this action has no global command binding.
     *
     * @return  always {@code null}
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── isEnabled: Luke Can Only Step Forward If He Hasn't Arrived Yet ────────
    // Luke can't step forward past the present moment — if he's already at
    // Yavin (index 0, the newest log), there's nowhere newer to go.
    // We enable this action only when the current index is greater than 0.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns true only when there is a newer log file to navigate to.
     * The newest file is at index 0; if the current index is already 0 we
     * can't go any newer, so we disable the button.
     *
     * <p>For example — Luke can only step forward if he hasn't reached Yavin yet:</p>
     * <pre>
     *   boolean canStep = action.isEnabled();
     *   // true only if current index > 0 (there is a newer file available)
     * </pre>
     *
     * @return  {@code true} when the current log-file index is greater than 0
     */
    @Override
    public boolean isEnabled()
    {
        return ( getInput() instanceof ModificationLogsViewInput )
            && ( ( ModificationLogsViewInput ) getInput() ).getIndex() > 0;
    }

}
