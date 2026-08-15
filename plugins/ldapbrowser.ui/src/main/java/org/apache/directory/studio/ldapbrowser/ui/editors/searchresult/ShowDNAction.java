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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.action.Action;


// ── CLASS: ShowDNAction — Luke Watching the Binary Sunset ────────────────────
// Luke stands at the Lars homestead, watching the twin suns set over Tatooine.
// He can choose to look at the whole horizon (DN column visible) or to narrow
// his focus and ignore the obvious (DN column hidden).
// This action is that choice: a simple toggle that makes the DN column appear
// or disappear in the search result table.  The state is persisted in the
// preference store so it survives across sessions.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toggle action that shows or hides the DN column in the search result editor.
 * The state is persisted in the preference store under
 * {@link BrowserUIConstants#PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN}.
 * When toggled, the universal listener picks up the preference change and
 * rebuilds the column layout.
 * Think of Luke's choice to look at the whole horizon or look away.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowDNAction extends Action
{

    // ── Luke Looks at the Horizon ─────────────────────────────────────────────
    // We create a checkbox action whose initial checked state mirrors whatever
    // is stored in the preference store — so the table starts in the correct state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the show/hide DN column action.
     * The initial checked state is read from the preference store so the table
     * layout is consistent with the user's previous choice.
     */
    public ShowDNAction()
    {
        super( Messages.getString( "ShowDNAction.ShowDN" ), AS_CHECK_BOX ); //$NON-NLS-1$
        super.setToolTipText( getText() );
        super.setEnabled( true );
        super.setChecked( BrowserUIPlugin.getDefault().getPreferenceStore().getBoolean(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN ) );
    }


    // ── Luke Makes His Choice ─────────────────────────────────────────────────
    // The user clicked the toggle — we write the new checked state to the
    // preference store.  The property-change listener in SearchResultEditor
    // picks it up and calls refresh(), which rebuilds the columns.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the new toggle state to the preference store.
     * The {@link SearchResultEditor} property-change listener triggers a refresh
     * when the preference changes, which rebuilds the column layout.
     */
    public void run()
    {
        BrowserUIPlugin.getDefault().getPreferenceStore().setValue(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN, super.isChecked() );
    }


    // ── Luke Puts Away His Binoculars ─────────────────────────────────────────
    // Nothing to release — the action holds no resources.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — this action holds no resources.
     */
    public void dispose()
    {
    }

}
