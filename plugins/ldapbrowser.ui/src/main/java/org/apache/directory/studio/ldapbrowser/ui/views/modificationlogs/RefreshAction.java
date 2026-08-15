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


// ── CLASS: RefreshAction — R2-D2 RE-PLUGS INTO THE DEATH STAR COMPUTER ───────
// R2-D2 is already inside the Death Star's computer system — but something has
// changed, a new record has been written, and he needs to re-query the data banks
// to pull the latest information. He unplugs, re-plugs, and the fresh data flows.
// This action does exactly that: it tells the universal listener to re-read the
// current log file from disk and reload the display with the latest content.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toolbar action that reloads the current modification log file from disk.
 * Useful when log entries have been added since the view was last loaded and
 * the user wants to see the latest state without switching connections.
 * Think of this as R2-D2 unplugging and re-plugging into the data port —
 * he comes back with the freshest information available.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RefreshAction extends BrowserAction
{

    /** The modification logs view. */
    private ModificationLogsView view;


    // ── Constructor: R2-D2 Locates the Data Port ─────────────────────────────
    // R2-D2 rolls up to the data terminal and identifies the right port to plug
    // into — he's ready to query the data banks on command.
    // We store the view reference so run() can trigger a reload through the listener.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this action with a reference to the modification logs view.
     * We need the view to call its universal listener's {@code refreshInput()}
     * when the action is triggered.
     *
     * <p>For example — R2-D2 identifies the correct data port:</p>
     * <pre>
     *   RefreshAction action = new RefreshAction( modificationLogsView );
     *   // R2 is docked and ready to query
     * </pre>
     *
     * @param view  the modification logs view to refresh when this action runs
     */
    public RefreshAction( ModificationLogsView view )
    {
        this.view = view;
    }


    // ── dispose: R2-D2 Retracts His Interface Probe ──────────────────────────
    // Mission complete, R2-D2 retracts his interface probe and rolls away —
    // the connection to the data terminal is cleanly released.
    // We delegate to super; nothing extra to release here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action.
     * We delegate entirely to {@link BrowserAction#dispose()}.
     *
     * <p>For example — R2-D2 retracts his probe and rolls away:</p>
     * <pre>
     *   action.dispose(); // super handles cleanup
     * </pre>
     */
    @Override
    public void dispose()
    {
        super.dispose();
    }


    // ── run: R2-D2 Queries the Data Banks Again ───────────────────────────────
    // R2-D2 plugs back into the data port and sends the query — the freshest
    // log data flows back and the display updates with the latest records.
    // We tell the universal listener to refresh its input (re-read from disk)
    // and scroll to the newest entry so the user sees the latest change.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the current log file from disk and scrolls to the newest entry.
     * We call {@link ModificationLogsViewUniversalListener#refreshInput()} which
     * forces a disk re-read, then scroll to the newest log container so the
     * user sees the most-recent modification right away.
     *
     * <p>For example — R2-D2 re-queries and surfaces the latest records:</p>
     * <pre>
     *   view.getUniversalListener().refreshInput();   // re-read from disk
     *   view.getUniversalListener().scrollToNewest(); // jump to latest entry
     * </pre>
     */
    @Override
    public void run()
    {
        // int topIndex = view.getMainWidget().getSourceViewer().getTopIndex();
        view.getUniversalListener().refreshInput();
        view.getUniversalListener().scrollToNewest();
        // view.getMainWidget().getSourceViewer().setTopIndex(topIndex);
    }


    // ── getText: R2-D2 Announces "Re-query!" ─────────────────────────────────
    // R2-D2 beeps and whistles — his way of saying "I'm going back in for
    // another look." The toolbar button needs a label to say the same.
    // We return the localized "Refresh" label from the resource bundle.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action.
     * Shown as the toolbar tooltip text.
     *
     * <p>For example — R2-D2 beeps "Re-query!" in Basic:</p>
     * <pre>
     *   String label = action.getText(); // "Refresh" (localized)
     * </pre>
     *
     * @return  the localized "Refresh" label string
     */
    @Override
    public String getText()
    {
        return Messages.getString( "RefreshAction.Refresh" ); //$NON-NLS-1$
    }


    // ── getImageDescriptor: R2-D2's Interface Probe Lights Up ────────────────
    // R2-D2's interface probe glows green — the visual signal that he's ready
    // to plug in and retrieve fresh data from the terminal.
    // We return the "refresh" icon from the plugin's image registry.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for the "Refresh" toolbar button.
     * We use the standard refresh image from the plugin's image registry.
     *
     * <p>For example — R2's probe lights up green, ready to re-query:</p>
     * <pre>
     *   ImageDescriptor img = action.getImageDescriptor(); // refresh/cycle icon
     * </pre>
     *
     * @return  the {@link ImageDescriptor} for the refresh icon
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_REFRESH );
    }


    // ── getCommandId: R2-D2 Needs No Special Access Code ─────────────────────
    // R2-D2 doesn't need an Imperial clearance code — he just plugs in directly.
    // This action isn't bound to a global Eclipse command.
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


    // ── isEnabled: R2-D2 Checks There's a Terminal to Plug Into ─────────────
    // R2-D2 won't roll up to an empty room — he checks there's actually a data
    // terminal with records before attempting to query.
    // We enable this action only when the current input is a valid log input.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns true only when there's a valid modification log input in the view.
     * Refreshing makes no sense when nothing is loaded, so we gate on the input type.
     *
     * <p>For example — R2-D2 checks there's a terminal to plug into:</p>
     * <pre>
     *   boolean ready = action.isEnabled();
     *   // true only if getInput() instanceof ModificationLogsViewInput
     * </pre>
     *
     * @return  {@code true} if the current input is a {@link ModificationLogsViewInput}
     */
    @Override
    public boolean isEnabled()
    {
        return getInput() instanceof ModificationLogsViewInput;
    }
}
