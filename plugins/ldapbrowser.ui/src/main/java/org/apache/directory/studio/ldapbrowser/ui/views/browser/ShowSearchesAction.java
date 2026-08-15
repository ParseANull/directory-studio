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


// ── CLASS: ShowSearchesAction — R2-D2 SHOWS THE DEATH STAR BLUEPRINTS ────────
// R2-D2 has just plugged into the Death Star's main computer and found
// the stolen plans. He can either display them on his holoprojector or keep
// them hidden until the right moment — that's exactly the choice this action
// offers: show the Searches category in the browser, or tuck it away.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toggles visibility of the Searches category in the LDAP browser view.
 * Saved searches let users re-run common LDAP queries instantly without
 * retyping filter strings — hiding this category de-clutters the browser
 * if you're not using saved searches.
 * Think of R2-D2 deciding whether to project the Death Star blueprints:
 * one button press and all your saved search definitions either appear
 * or vanish from the tree.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowSearchesAction extends Action
{

    // ── R2 Primes the Holoprojector ──────────────────────────────────────────────
    // R2-D2 spins up his projector dome, checks whether the plans were already
    // set to display, and starts in the correct state — ready to show or hide
    // on command.
    // We read the stored preference so the toggle button starts checked or
    // unchecked exactly as the user left it last session.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowSearchesAction and restores the user's saved visibility
     * preference so the Searches category starts in the right state.
     * If we skipped reading the preference, the category would always default
     * to hidden and users would have to re-enable it every single time they
     * opened the app.
     */
    public ShowSearchesAction()
    {
        super( Messages.getString( "ShowSearchesAction.ShowSearches" ), IAction.AS_CHECK_BOX ); //$NON-NLS-1$
        setEnabled( true );
        setChecked( BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_SEARCHES ) );
    }


    // ── R2 Projects or Retracts the Plans ───────────────────────────────────────
    // R2 tilts his dome and either opens the holoprojector to show the full
    // schematic, or folds it back in and goes quiet — the change is immediate.
    // When the user clicks this toggle, we persist the new preference so the
    // content provider knows whether to include the Searches node in the tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves the new visibility preference for the Searches category.
     * The browser's content provider reads this preference on refresh, so
     * saving it here is what makes the category actually appear or disappear.
     */
    public void run()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().setValue(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_SEARCHES, isChecked() );
    }

}
