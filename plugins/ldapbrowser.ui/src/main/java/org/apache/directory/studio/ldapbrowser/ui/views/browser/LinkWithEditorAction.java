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


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.ldapbrowser.ui.editors.searchresult.SearchResultEditorInput;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;


// ── CLASS: LinkWithEditorAction — OBI-WAN SENSES A DISTURBANCE IN THE FORCE ──
// Obi-Wan Kenobi, meditating on Tatooine, suddenly feels a ripple in the
// Force — something changed far away, and he knows instinctively what
// direction to look. The "Link with Editor" feature works the same way:
// when the editor's input changes (user opens a different entry), we sense
// that disturbance and immediately update the browser tree selection to
// match — keeping the two views perfectly in sync.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Concrete "Link with Editor" action for the LDAP browser view.
 * When this toggle is on, any change to the active editor's input automatically
 * moves the browser tree selection to show the same entry or search.
 * This prevents the frustrating situation where the editor shows entry X but
 * the browser is still pointing at entry Y — Obi-Wan always knows where the
 * disturbance is coming from.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LinkWithEditorAction extends AbstractLinkWithEditorAction
{
    /** The browser view */
    private BrowserView browserView;


    // ── Obi-Wan Opens His Senses ─────────────────────────────────────────────────
    // Obi-Wan settles into his desert hut, quiets his thoughts, and opens
    // himself to whatever ripples might come through the Force — ready to
    // react the moment something changes far away.
    // We wire up the parent class listeners so we immediately begin tracking
    // editor input changes and can sync the browser selection to match.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new LinkWithEditorAction for the given browser view.
     * We call {@code super.init()} at the end to register the part/property
     * listeners only after all fields are set — if we called init earlier,
     * the listeners might fire before {@code browserView} is assigned.
     *
     * @param browserView  the browser view this action belongs to; used to
     *                     reach the viewer and trigger a selection update.
     */
    public LinkWithEditorAction( BrowserView browserView )
    {
        super( browserView, Messages.getString( "LinkWithEditorAction.LinkWithEditor" ) ); //$NON-NLS-1$
        this.browserView = browserView;
        super.init();
    }


    // ── Obi-Wan Locates the Disturbance ─────────────────────────────────────────
    // Obi-Wan feels the ripple and immediately knows: "It came from there."
    // He turns his gaze in exactly the right direction without needing to search.
    // We inspect the active editor's input, extract the relevant entry or search,
    // and select it in the browser tree — but only if it isn't already selected,
    // to avoid triggering infinite selection-change loops.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Syncs the browser view's tree selection to match whatever the given editor
     * currently has open. We handle both entry editors (showing a single LDAP
     * entry) and search result editors (showing a search and its results).
     * We skip the selection update if the target is already selected — this is
     * critical to prevent an infinite loop: selecting in the browser fires
     * a selection event that would otherwise call this method again.
     *
     * @param part  the workbench part (typically an editor) whose input we'll
     *              read to figure out what to select in the browser.
     */
    @Override
    protected void linkViewWithEditor( IWorkbenchPart part )
    {
        if ( part != null && browserView != null
            && part.getSite().getWorkbenchWindow() == browserView.getSite().getWorkbenchWindow() )
        {
            Object objectToSelect = null;

            if ( part instanceof IEditorPart )
            {
                IEditorPart editor = ( IEditorPart ) part;
                IEditorInput input = editor.getEditorInput();
                if ( input instanceof EntryEditorInput )
                {
                    EntryEditorInput eei = ( EntryEditorInput ) input;
                    objectToSelect = eei.getInput();
                }
                else if ( input instanceof SearchResultEditorInput )
                {
                    SearchResultEditorInput srei = ( SearchResultEditorInput ) input;
                    objectToSelect = srei.getSearch();
                }
            }

            if ( objectToSelect != null )
            {
                // do not select if already selected!
                // necessary to avoid infinite loops!
                IStructuredSelection selection = ( IStructuredSelection ) browserView.getMainWidget().getViewer()
                    .getSelection();
                if ( selection.size() != 1 || !selection.getFirstElement().equals( objectToSelect ) )
                {
                    browserView.select( objectToSelect );
                }
            }
        }
    }

}
