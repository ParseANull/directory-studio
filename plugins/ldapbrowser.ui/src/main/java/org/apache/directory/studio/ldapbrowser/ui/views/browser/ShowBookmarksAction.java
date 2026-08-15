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

package org.apache.directory.studio.ldapbrowser.ui.views.browser;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;


// ── CLASS: ShowBookmarksAction — LUKE SEES THE LANDMARKS ON THE HORIZON ──────
// Luke stands outside the Lars homestead, the twin suns painting the sky, and
// spots the distant landmarks — the Anchorhead spire, the shape of the
// Jundland Wastes — that help him navigate Tatooine's featureless expanse.
// Bookmarks in the browser are exactly that: named landmarks that let users
// quickly navigate back to important entries without traversing the whole tree.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toggles visibility of the Bookmarks category in the LDAP browser view.
 * Bookmarks are user-saved shortcuts to specific LDAP entries — quick jumps
 * so you don't have to traverse the full tree every time.
 * Think of this action as Luke choosing whether the landmarks are visible
 * on the horizon: flip the toggle and your saved navigation points appear
 * or disappear from the browser panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowBookmarksAction extends Action
{

    // ── Luke Spots the Distant Markers ──────────────────────────────────────────
    // Luke's eyes scan the horizon and he consciously decides: "I want to see
    // those landmarks." He doesn't walk to them yet — just acknowledges that
    // he wants them in view for navigation.
    // We construct this toggle with the right label and restore the user's
    // last preference so the bookmarks category starts visible or hidden
    // exactly as they left it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowBookmarksAction and reads the current preference so the
     * toggle button reflects the user's saved setting right away.
     * Without reading the preference here, the button would default to unchecked
     * every time and the user's bookmarks would be hidden on every restart.
     */
    public ShowBookmarksAction()
    {
        super( Messages.getString( "ShowBookmarksAction.ShowBookmarks" ), IAction.AS_CHECK_BOX ); //$NON-NLS-1$
        setEnabled( true );
        setChecked( BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_BOOKMARKS ) );
    }


    // ── Luke Decides: Landmarks In or Out of View ────────────────────────────────
    // In that quiet sunset moment, Luke makes a choice — to look or to look
    // away — and the landmarks are present or absent accordingly.
    // When the user toggles this button, we persist their preference so the
    // bookmarks panel remembers its visibility state across restarts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Persists the new visibility preference for the Bookmarks category.
     * The content provider reads this preference to decide whether to include
     * the bookmarks node in the tree, so saving it here is what actually
     * makes the category appear or disappear.
     */
    public void run()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().setValue(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_BOOKMARKS, isChecked() );
    }

}
