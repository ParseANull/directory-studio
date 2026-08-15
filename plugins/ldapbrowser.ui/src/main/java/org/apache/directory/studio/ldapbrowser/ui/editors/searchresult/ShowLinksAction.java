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


// ── CLASS: ShowLinksAction — Obi-Wan Disabling the Tractor Beam ───────────────
// Obi-Wan slips through the Death Star to the tractor beam control panel.
// He can leave it active (DN values are plain text) or he can disable it
// (DN values become hyperlinks, letting the user click through to the entry editor).
// This toggle action is that moment: one press disables the "tractor beam" that
// keeps DN values inert, and they become live hyperlinks.  Press again and
// they go back to plain text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toggle action that enables or disables DN values as clickable hyperlinks
 * in the search result editor.
 * The state is persisted in the preference store under
 * {@link BrowserUIConstants#PREFERENCE_SEARCHRESULTEDITOR_SHOW_LINKS}.
 * When enabled, the universal listener overlays a {@code Hyperlink} widget on
 * the DN cell when the mouse hovers over it.
 * Think of Obi-Wan at the tractor beam: one toggle changes whether the DN
 * column grabs you and lets you navigate, or just sits there inert.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowLinksAction extends Action
{

    // ── Obi-Wan Finds the Control Panel ───────────────────────────────────────
    // We create a checkbox action whose initial state mirrors the preference store —
    // so hyperlinks are on or off exactly as the user last left them.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the show/hide DN hyperlinks action.
     * The initial checked state is read from the preference store.
     */
    public ShowLinksAction()
    {
        super( Messages.getString( "ShowLinksAction.DNAsLink" ), AS_CHECK_BOX ); //$NON-NLS-1$
        super.setToolTipText( getText() );
        super.setEnabled( true );
        super.setChecked( BrowserUIPlugin.getDefault().getPreferenceStore().getBoolean(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_LINKS ) );
    }


    // ── Obi-Wan Flips the Switch ──────────────────────────────────────────────
    // The user toggled the action — write the new state to the preference store.
    // The universal listener checks this preference on every mouse-move, so the
    // change takes effect immediately on the next hover event.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the new toggle state to the preference store.
     * The {@link SearchResultEditorUniversalListener} reads this preference
     * in {@code checkDnLink()} on every mouse-move event over the DN column.
     */
    public void run()
    {
        BrowserUIPlugin.getDefault().getPreferenceStore().setValue(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_LINKS, super.isChecked() );
    }


    // ── Obi-Wan Steps Away From the Panel ─────────────────────────────────────
    // Nothing to clean up — the action holds no resources.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — this action holds no resources.
     */
    public void dispose()
    {
    }

}
