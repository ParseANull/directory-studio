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


import java.io.File;

import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.io.api.LdifSearchLogger;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: OlderAction — LUKE RETRACES STEPS ON HIS JOURNEY ─────────────────
// Luke sometimes has to look back: back to Tatooine, back to the early
// missions, back to understand how he got where he is. The log view's rotation
// goes backwards in time too — higher index = older file. OlderAction steps
// back one chapter by incrementing the index, showing the previous log file.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Navigates the search logs view to an older (previous) log file rotation.
 * The search logger keeps multiple rolling log files; incrementing the index
 * moves to an older file. We also check that the older file actually exists
 * before enabling the button.
 * Think of Luke casting his mind back to an earlier chapter: "What was the
 * search traffic like last week?" — this action takes you there.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlderAction extends BrowserAction
{

    /** The search logs view. */
    private SearchLogsView view;


    // ── Luke Looks Back to Remember ──────────────────────────────────────────────
    // Luke keeps a reference to his past so he can re-examine it.
    // We store the view so we can tell the universal listener which older file
    // to load when the user clicks the button.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlderAction bound to the given search logs view.
     *
     * @param view  the search logs view whose file rotation we'll navigate.
     */
    public OlderAction( SearchLogsView view )
    {
        this.view = view;
    }


    // ── Luke Lets Go of the Memory ────────────────────────────────────────────────
    // After the reflection is done, Luke releases it — he doesn't carry the
    // past with him forever.
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


    // ── Luke Steps Back One Chapter ───────────────────────────────────────────────
    // One step backward: from last week to the week before, from index N to N+1.
    // We increment the index and ask the listener to load and display that file,
    // then scroll to the newest entry in it (the bottom of the older file).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Loads the next-older log file by incrementing the rotation index.
     * Index 0 is newest; higher values are older files. We scroll to the newest
     * entry within the older file so the user sees where that period ended.
     */
    @Override
    public void run()
    {
        SearchLogsViewInput oldInput = ( SearchLogsViewInput ) getInput();
        SearchLogsViewInput newInput = new SearchLogsViewInput( oldInput.getBrowserConnection(),
            oldInput.getIndex() + 1 );
        view.getUniversalListener().setInput( newInput );
        view.getUniversalListener().scrollToNewest();
    }


    // ── The Chapter Title ────────────────────────────────────────────────────────
    // Every step back in Luke's story has a name; this one is "Older."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised label for this action (e.g. "Older").
     *
     * @return  the localised action label.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "OlderAction.Older" ); //$NON-NLS-1$
    }


    // ── Luke Holds the Back Arrow ────────────────────────────────────────────────
    // A backwards arrow: retracing steps, looking back.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the "previous" arrow icon for the toolbar button.
     *
     * @return  the image descriptor for the backward/previous icon.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_PREVIOUS );
    }


    // ── No Keybinding — Walk There Manually ──────────────────────────────────────
    // Going back in memory takes deliberate effort — no shortcut for that.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} — no Eclipse command ID is assigned to this action.
     *
     * @return  {@code null}.
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── Luke Checks the Past Actually Exists ──────────────────────────────────────
    // You can only retrace a step that was actually taken; if there's no older
    // log file on disk, the button makes no sense.
    // We ask the logger whether the next-older file exists and is readable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Enables the action only when the next-older log file (index + 1) actually
     * exists on disk and is readable.
     * We check this explicitly because the log rotation may have fewer files
     * than the maximum, so we can't just check the index bounds.
     *
     * @return  {@code true} if there is a readable older log file to navigate to.
     */
    @Override
    public boolean isEnabled()
    {
        if ( ( getInput() instanceof SearchLogsViewInput ) )
        {
            SearchLogsViewInput input = ( SearchLogsViewInput ) getInput();
            if ( input.getBrowserConnection().getConnection() != null )
            {
                LdifSearchLogger searchLogger = ConnectionCorePlugin.getDefault().getLdifSearchLogger();
                File[] files = searchLogger.getFiles( input.getBrowserConnection().getConnection() );
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
