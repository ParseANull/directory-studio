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


// ── CLASS: ShowDITAction — LUKE GAZES AT THE BINARY SUNSET ──────────────────
// Luke stands outside the moisture farm on Tatooine, staring at the twin suns
// setting over the endless desert — the full vastness of the landscape laid
// bare before him. The DIT (Directory Information Tree) is that landscape:
// a branching hierarchy of entries that you can choose to show or hide in
// the browser view.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toggles visibility of the DIT category in the LDAP browser view.
 * The DIT is the main directory tree — every entry lives here, and hiding it
 * gives you a cleaner panel when you only care about searches or bookmarks.
 * Think of this action as the choice of whether Luke looks out at the twin
 * suns: flip the toggle and the whole landscape appears or disappears.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowDITAction extends Action
{

    // ── Luke Decides to Look at the Horizon ─────────────────────────────────────
    // Luke steps out of the homestead and squints toward the horizon, choosing
    // in this moment to actually look — to let the full scope of Tatooine's
    // landscape become visible to him.
    // We set up this toggle action with the right label, check state, and enabled
    // flag so the user can make the same choice about the DIT category.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowDITAction and reads the current preference so the
     * toggle button starts in the right checked/unchecked state.
     * We need to read the preference at construction time — if we didn't,
     * the button would always start unchecked and the user's saved setting
     * would be silently ignored.
     */
    public ShowDITAction()
    {
        super( Messages.getString( "ShowDITAction.ShowDIT" ), IAction.AS_CHECK_BOX ); //$NON-NLS-1$
        setEnabled( true );
        setChecked( BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_DIT ) );
    }


    // ── The Twin Suns Rise or Set ────────────────────────────────────────────────
    // Luke makes his choice: he either steps outside to watch the sunset or goes
    // back inside — and either way, the landscape changes for him personally.
    // When the user clicks this toggle, we write the new preference so the DIT
    // category appears or disappears, and the setting survives restarts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Persists the new visibility preference for the DIT category.
     * Saving it here means the browser remembers whether the user wanted the DIT
     * visible next time they open the application.
     */
    public void run()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().setValue(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_DIT, isChecked() );
    }

}
