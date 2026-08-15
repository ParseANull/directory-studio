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


import java.io.File;

import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.io.api.LdifModificationLogger;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: OlderAction — LUKE RETRACES STEPS ON HIS HERO'S JOURNEY ──────────
// Luke's hero journey can be reviewed in reverse — from the Death Star throne
// room back through Cloud City, Dagobah, Hoth, all the way to Tatooine.
// Each step backward in time reveals older events in the saga.
// This action does the same: it increments the log-file index (moving away
// from 0, the newest file) to load an older rotating log and display it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toolbar action that moves the modification logs view to the next-older
 * log file in the rotating set.
 * Log files are indexed from 0 (newest) upward; incrementing the index moves
 * toward older modifications. This action is only enabled when an older log
 * file actually exists on disk.
 * Think of Luke retracing his hero's journey — each click steps further back
 * through the LDAP operation history.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlderAction extends BrowserAction
{

    /** The modification logs view. */
    private ModificationLogsView view;


    // ── Constructor: Luke Prepares to Retrace the Journey ────────────────────
    // Luke stands at Yavin and prepares to walk the journey in reverse —
    // he needs the map (view reference) to know where to step next.
    // We store the view reference so run() can push the older-index input.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this action with a reference to the modification logs view.
     * We store the view so {@link #run()} can update its universal listener
     * with the new (incremented) log-file index.
     *
     * <p>For example — Luke holds the map as he prepares to retrace his steps:</p>
     * <pre>
     *   OlderAction action = new OlderAction( modificationLogsView );
     *   // view stored; ready to navigate backward when run() is called
     * </pre>
     *
     * @param view  the modification logs view to navigate when this action runs
     */
    public OlderAction( ModificationLogsView view )
    {
        this.view = view;
    }


    // ── dispose: Luke Closes This Chapter of the Review ──────────────────────
    // Luke has reviewed enough of the past — he closes the journal and moves on.
    // We delegate to super; no extra resources to release here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action.
     * We delegate entirely to {@link BrowserAction#dispose()}.
     *
     * <p>For example — Luke closes the journal and moves on:</p>
     * <pre>
     *   action.dispose(); // super handles cleanup
     * </pre>
     */
    @Override
    public void dispose()
    {
        super.dispose();
    }


    // ── run: Luke Steps Backward One Stage on the Journey ────────────────────
    // Luke walks back from Yavin to Cloud City, picking up the thread of events
    // that led to the present — each step backward illuminates what came before.
    // We build a new input with index incremented by 1 (away from 0 = older),
    // push it to the listener, and scroll to the newest entry in the older file.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Navigates to the next-older log file by incrementing the current index.
     * We construct a new {@link ModificationLogsViewInput} with
     * {@code oldIndex + 1}, push it to the universal listener, and scroll to
     * the newest entry in that older volume.
     *
     * <p>For example — Luke steps one stage back on his hero's journey:</p>
     * <pre>
     *   int olderIndex = oldInput.getIndex() + 1; // away from 0 = older
     *   ModificationLogsViewInput newInput = new ModificationLogsViewInput( conn, olderIndex );
     *   view.getUniversalListener().setInput( newInput );
     *   view.getUniversalListener().scrollToNewest(); // bottom of older file
     * </pre>
     */
    @Override
    public void run()
    {
        ModificationLogsViewInput oldInput = ( ModificationLogsViewInput ) getInput();
        ModificationLogsViewInput newInput = new ModificationLogsViewInput( oldInput.getBrowserConnection(), oldInput
            .getIndex() + 1 );
        view.getUniversalListener().setInput( newInput );
        view.getUniversalListener().scrollToNewest();
    }


    // ── getText: Luke Announces "Back to the Beginning" ──────────────────────
    // Luke says "Let's trace back to where this started" — the signal that
    // we're moving toward the older, earlier parts of the record.
    // We return the localized "Older" label for the toolbar button.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action.
     * Shown as the toolbar tooltip text.
     *
     * <p>For example — Luke signals a step back toward Tatooine:</p>
     * <pre>
     *   String label = action.getText(); // "Older" (localized)
     * </pre>
     *
     * @return  the localized "Older" label string
     */
    @Override
    public String getText()
    {
        return Messages.getString( "OlderAction.Older" ); //$NON-NLS-1$
    }


    // ── getImageDescriptor: Luke's X-Wing Points Backward ────────────────────
    // Luke's X-wing banks and reverses course — the backward-arrow visual
    // cue that we're navigating toward older history.
    // We return the "previous" icon from the plugin's image registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for the "Older" toolbar button.
     * We use the "previous" (back arrow) image from the plugin's image registry.
     *
     * <p>For example — Luke's X-wing banks and points back toward Tatooine:</p>
     * <pre>
     *   ImageDescriptor img = action.getImageDescriptor(); // back-arrow icon
     * </pre>
     *
     * @return  the {@link ImageDescriptor} for the previous/back icon
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_PREVIOUS );
    }


    // ── getCommandId: No Special Rebel Alliance Command Code ─────────────────
    // Luke doesn't need a special code to step backward — this action has no
    // global Eclipse command binding.
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


    // ── isEnabled: Luke Can Only Step Back If There's More History ────────────
    // Luke can only retrace his steps if those steps actually happened —
    // if there's no older log file on disk, there's nowhere to go.
    // We check that the file at index+1 exists, is non-null, and is readable.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns true only when there is an older log file to navigate to.
     * We ask the {@link LdifModificationLogger} for the file array and check
     * that the file at {@code currentIndex + 1} actually exists and is readable.
     *
     * <p>For example — Luke can only retrace steps that actually happened:</p>
     * <pre>
     *   File[] files = modificationLogger.getFiles( connection );
     *   int olderIdx = input.getIndex() + 1;
     *   return olderIdx &lt; files.length &amp;&amp; files[olderIdx] != null &amp;&amp; files[olderIdx].exists();
     * </pre>
     *
     * @return  {@code true} when the next-older log file exists and is readable
     */
    @Override
    public boolean isEnabled()
    {
        if ( ( getInput() instanceof ModificationLogsViewInput ) )
        {
            ModificationLogsViewInput input = ( ModificationLogsViewInput ) getInput();
            if ( input.getBrowserConnection().getConnection() != null )
            {
                LdifModificationLogger modificationLogger = ConnectionCorePlugin.getDefault()
                    .getLdifModificationLogger();
                File[] files = modificationLogger.getFiles( input.getBrowserConnection().getConnection() );
                int i = input.getIndex() + 1;
                if ( 0 <= i && i < files.length && files[i] != null && files[i].exists() && files[i].canRead() )
                {
                    return true;
                }
            }
        }

        return false;
    }

}
