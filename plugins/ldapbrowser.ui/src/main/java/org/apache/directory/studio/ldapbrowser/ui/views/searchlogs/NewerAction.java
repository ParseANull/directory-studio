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


// ── CLASS: NewerAction — LUKE STEPS FORWARD ON HIS HERO'S JOURNEY ────────────
// Luke's journey moves from Tatooine toward his destiny one step at a time.
// Each step forward takes him to a newer chapter. The log view has multiple
// rotation files; "newer" means a lower index (index 0 = most recent).
// This action moves forward — decrements the index — to show a more recent
// page of search logs, like Luke advancing to the next chapter of his story.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Navigates the search logs view to a more recent log file rotation.
 * The search logger keeps multiple rolling log files; index 0 is the newest.
 * This action decrements the current file index by 1, showing the next-newer file.
 * Think of Luke stepping one chapter forward: closer to the present,
 * closer to where the action is happening right now.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewerAction extends BrowserAction
{

    /** The search logs view. */
    private SearchLogsView view;


    // ── Luke Checks the Road Ahead ────────────────────────────────────────────────
    // Luke glances ahead along the path: is there a newer chapter to step into?
    // He keeps track of where he is so he can move forward when the time comes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NewerAction bound to the given search logs view.
     * We store the view so we can tell the universal listener to load the
     * next-newer file when the user clicks the button.
     *
     * @param view  the search logs view to navigate.
     */
    public NewerAction( SearchLogsView view )
    {
        this.view = view;
    }


    // ── Luke Moves On ────────────────────────────────────────────────────────────
    // Luke doesn't cling to previous chapters — he releases them and steps forward.
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


    // ── Luke Takes the Step ───────────────────────────────────────────────────────
    // One step forward: Tatooine is behind him, the next chapter awaits.
    // We decrement the file index (index - 1 = one file newer) and ask the
    // universal listener to load it, then scroll to the top of the new page.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Loads the next-newer log file by decrementing the rotation index.
     * Index 0 is the newest file; we move toward it by subtracting 1.
     * We scroll to the top (oldest entries in the file) after the load
     * so the user sees the beginning of the newer log first.
     */
    @Override
    public void run()
    {
        SearchLogsViewInput oldInput = ( SearchLogsViewInput ) getInput();
        SearchLogsViewInput newInput = new SearchLogsViewInput( oldInput.getBrowserConnection(),
            oldInput.getIndex() - 1 );
        view.getUniversalListener().setInput( newInput );
        view.getUniversalListener().scrollToOldest();

        // go to top
        view.getMainWidget().getSourceViewer().setTopIndex( 0 );
    }


    // ── The Chapter Title ────────────────────────────────────────────────────────
    // Luke's next chapter always has a name; this action's label tells the user
    // what clicking it will do.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised label for this action (e.g. "Newer").
     *
     * @return  the localised action label.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "NewerAction.Newer" ); //$NON-NLS-1$
    }


    // ── Luke Holds the Forward Arrow ─────────────────────────────────────────────
    // A forward arrow icon: Luke is always moving toward his destiny.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the "next" arrow icon for the toolbar button.
     *
     * @return  the image descriptor for the forward/next icon.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_NEXT );
    }


    // ── Luke Checks He's Not Already at the Newest Chapter ───────────────────────
    // There's no "forward" if Luke is already in the present — he can't skip
    // ahead of now.
    // We disable the action when the current index is already 0 (the newest file).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the Eclipse command ID for this action; {@code null} here because
     * this action has no keyboard shortcut.
     *
     * @return  {@code null}.
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── There Must Be a Newer Chapter ────────────────────────────────────────────
    // Luke can only step forward if the story continues — if index > 0 there's
    // a newer file to move to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Enables the action only when the current file index is greater than 0,
     * meaning there is a newer (lower index) log file to navigate to.
     *
     * @return  {@code true} if the current input index is {@code > 0}.
     */
    @Override
    public boolean isEnabled()
    {
        return ( getInput() instanceof SearchLogsViewInput )
            && ( ( SearchLogsViewInput ) getInput() ).getIndex() > 0;
    }

}
