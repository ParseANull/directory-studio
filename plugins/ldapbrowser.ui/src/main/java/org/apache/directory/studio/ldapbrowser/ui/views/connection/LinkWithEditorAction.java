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

package org.apache.directory.studio.ldapbrowser.ui.views.connection;


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.ui.editors.searchresult.SearchResultEditorInput;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.AbstractLinkWithEditorAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;


// ── CLASS: LinkWithEditorAction — OBI-WAN SENSES THE EDITOR'S DISTURBANCE ───
// When Obi-Wan feels a new presence in the Force — Luke piloting the X-wing,
// Leia in danger — he immediately reacts and draws the relevant party's
// attention to the right place, synchronizing awareness across the galaxy.
// This class does exactly that: it listens to the active editor and, when the
// editor's input changes, it synchronizes the connection view's selection to
// show which connection that entry or search result belongs to.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Keeps the connection view in sync with the currently active editor.
 * When "link with editor" is enabled and the editor switches to a different
 * entry or search, we look up its owning connection and highlight it in the
 * connection view so the user always knows which server they're browsing.
 * This action is invisible to the user — it operates purely as a background
 * listener wired up by the connection view.
 * Think of this as Obi-Wan's Force awareness keeping the team in sync.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LinkWithEditorAction extends AbstractLinkWithEditorAction
{
    /** The connection view */
    private ConnectionView connectionView;


    // ── Constructor: Obi-Wan Tunes Into the Editor's Frequency ───────────────
    // Obi-Wan sits in quiet meditation, opens his Force senses to Luke's
    // presence, and begins tracking him across the galaxy in real time.
    // We call super to initialize the toggle state and icon, store the view
    // reference, then call {@code init()} to register the part/property listeners.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this action and starts listening to editor changes immediately.
     * We delegate to {@link AbstractLinkWithEditorAction} for the checkbox setup
     * and listener wiring, storing the connection view so we can select items in it.
     *
     * <p>For example — Obi-Wan locks onto Luke's Force signature:</p>
     * <pre>
     *   super( connectionView, labelText ); // checkbox action initialized
     *   this.connectionView = connectionView;
     *   super.init(); // listeners activated — Obi-Wan is now tracking
     * </pre>
     *
     * @param connectionView  the connection view we will update when the editor changes
     */
    public LinkWithEditorAction( ConnectionView connectionView )
    {
        super( connectionView, Messages.getString( "LinkWithEditorAction.LinkWithEditor" ) ); //$NON-NLS-1$
        this.connectionView = connectionView;
        super.init();
    }


    // ── linkViewWithEditor: Obi-Wan Points Out the Right Connection ───────────
    // Obi-Wan senses Luke's X-wing moving through the Force and reaches out to
    // the Rebel fleet commander, saying "look there — that's where he is."
    // We inspect the active editor's input, find the owning connection for the
    // entry or search result, and select it in the connection view — but only if
    // it isn't already selected, to avoid infinite selection loops.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Selects the owning connection in the connection view based on the given editor's input.
     * We handle {@link EntryEditorInput} (single LDAP entry) and
     * {@link SearchResultEditorInput} (search results) — in both cases we find the
     * backing connection and highlight it in the list.
     * We guard against re-selecting an already-selected connection to prevent
     * infinite selection-change loops between the view and the editor.
     *
     * <p>For example — Obi-Wan points the fleet to Luke's location:</p>
     * <pre>
     *   IEntry entry = entryEditorInput.getResolvedEntry();
     *   Connection conn = entry.getBrowserConnection().getConnection();
     *   connectionView.select( conn ); // highlight the right server
     * </pre>
     *
     * @param part  the workbench part (usually an IEditorPart) whose input changed
     */
    @Override
    protected void linkViewWithEditor( IWorkbenchPart part )
    {
        if ( part != null && connectionView != null
            && part.getSite().getWorkbenchWindow() == connectionView.getSite().getWorkbenchWindow() )
        {
            Object objectToSelect = null;

            if ( part instanceof IEditorPart )
            {
                IEditorPart editor = ( IEditorPart ) part;
                IEditorInput input = editor.getEditorInput();
                if ( input instanceof EntryEditorInput )
                {
                    EntryEditorInput eei = ( EntryEditorInput ) input;
                    IEntry entry = eei.getResolvedEntry();
                    if ( entry != null )
                    {
                        objectToSelect = entry.getBrowserConnection().getConnection();
                    }
                }
                else if ( input instanceof SearchResultEditorInput )
                {
                    SearchResultEditorInput srei = ( SearchResultEditorInput ) input;
                    ISearch search = srei.getSearch();
                    if ( search != null )
                    {
                        objectToSelect = search.getBrowserConnection().getConnection();
                    }
                }
            }

            if ( objectToSelect != null )
            {
                // do not select if already selected!
                // necessary to avoid infinite loops!
                IStructuredSelection selection = ( IStructuredSelection ) connectionView.getMainWidget().getViewer()
                    .getSelection();
                if ( selection.size() != 1 || !selection.getFirstElement().equals( objectToSelect ) )
                {
                    connectionView.select( objectToSelect );
                }
            }
        }
    }

}
