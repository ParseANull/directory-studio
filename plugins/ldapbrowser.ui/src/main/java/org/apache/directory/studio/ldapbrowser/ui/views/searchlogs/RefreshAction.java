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
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: RefreshAction — R2-D2 PLUGS BACK INTO THE COMPUTER ────────────────
// R2-D2 successfully extracted the plans once, and now he plugs back in to
// get the latest version — maybe new schematics were uploaded since the last
// query. This action does exactly that: it tells the universal listener to
// re-read the log file from disk and display whatever has been written since
// the last load, then scrolls to the newest entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Reloads the current search log file from disk and refreshes the view.
 * The search log file may have grown since the view last loaded it (new searches
 * were performed), so this action re-reads it and scrolls to the newest entry.
 * Think of R2-D2 plugging back in to pull the latest data from the Imperial
 * computer — what was there before is still there, plus whatever was added.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RefreshAction extends BrowserAction
{

    /** The search logs view. */
    private SearchLogsView view;


    // ── R2 Identifies Which Computer to Plug Into ────────────────────────────────
    // R2-D2 extends his interface probe toward the correct terminal — he needs
    // to know which view's listener to trigger the re-read on.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new RefreshAction bound to the given search logs view.
     * We need the view so we can call its universal listener's {@code refreshInput()}
     * and {@code scrollToNewest()} methods.
     *
     * @param view  the search logs view to refresh.
     */
    public RefreshAction( SearchLogsView view )
    {
        this.view = view;
    }


    // ── R2 Unplugs When Done ─────────────────────────────────────────────────────
    // R2 retracts his probe once the data transfer is complete — no resource leaks.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Releases resources held by this action.
     */
    @Override
    public void dispose()
    {
        super.dispose();
    }


    // ── R2 Reads the Latest Data ─────────────────────────────────────────────────
    // R2 plugs in, pulls the freshest version of the plans, and presents them
    // to the crew — scrolled to the most recent entry.
    // We tell the universal listener to re-read the log file and then scroll
    // the viewer to the newest LDIF container in the document.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Re-reads the current search log file from disk and scrolls the view
     * to the most recently written log entries.
     * This is useful when you know searches were performed after the view
     * last loaded the file — typically triggered manually by the user.
     */
    @Override
    public void run()
    {
        // int topIndex = view.getMainWidget().getSourceViewer().getTopIndex();
        view.getUniversalListener().refreshInput();
        view.getUniversalListener().scrollToNewest();
        // view.getMainWidget().getSourceViewer().setTopIndex(topIndex);
    }


    // ── R2 Reads the Refresh Label ────────────────────────────────────────────────
    // The terminal's label says "Refresh" — R2 knows what he's there for.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised label for this action (e.g. "Refresh").
     *
     * @return  the localised action label.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "RefreshAction.Refresh" ); //$NON-NLS-1$
    }


    // ── R2's Interface Indicator ─────────────────────────────────────────────────
    // R2's display panel shows the refresh icon — a circular arrow indicating
    // a new data pull.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the refresh icon image descriptor for the toolbar button.
     *
     * @return  the image descriptor for the circular-arrow refresh icon.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_REFRESH );
    }


    // ── No Special Access Code ────────────────────────────────────────────────────
    // R2 just plugs in directly — no passcode required for the refresh operation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} — this action has no associated Eclipse command ID.
     *
     * @return  {@code null}.
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── The Terminal Must Be Active ───────────────────────────────────────────────
    // R2 can only pull data if there's a live terminal with data to pull.
    // We enable the action only when there is a valid search log input.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Enables the action only when the view has a valid {@link SearchLogsViewInput}.
     * Without a valid input there are no log files to re-read.
     *
     * @return  {@code true} if the current input is a SearchLogsViewInput.
     */
    @Override
    public boolean isEnabled()
    {
        return getInput() instanceof SearchLogsViewInput;
    }
}
