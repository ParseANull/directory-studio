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


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.editors.entry.EntryEditor;
import org.apache.directory.studio.ldapbrowser.ui.editors.searchresult.SearchResultEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: AbstractLinkWithEditorAction — OBI-WAN SENSES A DISTURBANCE ───────
// Obi-Wan Kenobi, deep in meditation, opens himself to the Force and listens
// for disturbances — ripples that tell him something has changed somewhere.
// When he senses one, he acts. This base class is exactly that: it listens
// for editors opening and closing (part events) and for editors changing
// their input (property events), then calls subclasses to react accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base for the "Link with Editor" toggle actions used in browser/connection views.
 * When enabled, this action wires the view to follow whatever the active editor is showing —
 * so clicking on an entry in the editor highlights it in the tree, keeping them in sync.
 * Think of Obi-Wan in his Tatooine hut: he doesn't go looking for disturbances,
 * he just stays open and reacts the moment the Force tells him something changed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractLinkWithEditorAction extends Action
{
    /** The view */
    private ViewPart viewPart;

    /** The listener listening on opening/closing editors */
    private IPartListener partListener = new IPartListener()
    {

        @Override
        public void partOpened( IWorkbenchPart part )
        {
            registerPropertyListener( part );

        }


        @Override
        public void partClosed( IWorkbenchPart part )
        {
            unregisterPropertyListener( part );
        }


        @Override
        public void partDeactivated( IWorkbenchPart part )
        {
        }


        @Override
        public void partBroughtToTop( IWorkbenchPart part )
        {
        }


        @Override
        public void partActivated( IWorkbenchPart part )
        {
        }
    };

    /** The listener listening on input changes in editors */
    private IPropertyListener propertyListener = new IPropertyListener()
    {
        @Override
        public void propertyChanged( Object source, int propId )
        {
            if ( source instanceof IEditorPart && propId == BrowserUIConstants.INPUT_CHANGED )
            {
                linkViewWithEditor( ( IEditorPart ) source );
            }
        }
    };


    // ── Obi-Wan Monitors the Force Signature of Each Part ────────────────────────
    // Obi-Wan extends his awareness to cover every living thing that just entered
    // the room, attuning himself to their Force signatures so he'll know the moment
    // any of them changes.
    // We iterate all currently open editors and attach our property listener to
    // the entry/search-result editors, then attach the part listener so we catch
    // any new editors opening in the future.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers the part listener (for editors opening/closing) and property listeners
     * on all currently open entry editors and search result editors.
     * We need this two-step approach because editors that were already open
     * before this action was constructed won't trigger {@code partOpened} —
     * so we have to back-fill the property listeners manually.
     */
    private void registerListeners()
    {
        // register part listeners
        viewPart.getSite().getWorkbenchWindow().getPartService().addPartListener( partListener );

        // register property listener
        IEditorReference[] editorReferences = viewPart.getSite().getPage().getEditorReferences();
        for ( IEditorReference editorReference : editorReferences )
        {
            IEditorPart editor = editorReference.getEditor( false );
            registerPropertyListener( editor );
        }
    }


    // ── Obi-Wan Tunes In to One Part ────────────────────────────────────────────
    // Obi-Wan focuses his awareness specifically on one person in the room —
    // he'll feel any change in their Force presence immediately.
    // We attach the property listener to a specific editor part, but only if
    // it's an EntryEditor or SearchResultEditor — we don't care about other parts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the input-change property listener to the given part if it's an
     * entry editor or search result editor.
     * We filter by type because we only want to sync when the LDAP-related
     * editors change — reacting to, say, a text editor changing would be wrong.
     *
     * @param part  the workbench part that just opened; may be null if the editor
     *              hasn't been materialised yet (lazy initialisation), in which
     *              case we do nothing.
     */
    private void registerPropertyListener( IWorkbenchPart part )
    {
        if ( part instanceof EntryEditor || part instanceof SearchResultEditor )
        {
            part.addPropertyListener( propertyListener );
        }
    }


    // ── Obi-Wan Withdraws His Awareness From All Parts ──────────────────────────
    // Obi-Wan consciously lets go of his attunement to the room — he stops
    // monitoring, conserving his strength for when it's needed again.
    // We remove both the part listener and all per-editor property listeners.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters the part listener and removes property listeners from all
     * currently open entry/search-result editors.
     * Call this when the "link" toggle is turned off so we stop reacting to
     * editor changes that the user no longer wants to drive the view selection.
     */
    private void unregisterListeners()
    {
        // unregister part listener
        viewPart.getSite().getWorkbenchWindow().getPartService().removePartListener( partListener );

        // unregister property listener
        IEditorReference[] editorReferences = viewPart.getSite().getPage().getEditorReferences();
        for ( IEditorReference editorReference : editorReferences )
        {
            IEditorPart editor = editorReference.getEditor( false );
            unregisterPropertyListener( editor );
        }
    }


    // ── Obi-Wan Releases One Part From His Attention ────────────────────────────
    // He lets a specific Force signature fade from his awareness, moving on.
    // We remove our property listener from one specific editor part.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the input-change property listener from a specific part, but only
     * if it's an entry/search-result editor.
     * Harmless to call with non-editor parts or null — we check the type first.
     *
     * @param part  the workbench part that closed or that we're cleaning up.
     */
    private void unregisterPropertyListener( IWorkbenchPart part )
    {
        if ( part instanceof EntryEditor || part instanceof SearchResultEditor )
        {
            part.removePropertyListener( propertyListener );
        }
    }


    // ── Obi-Wan Takes His Post ───────────────────────────────────────────────────
    // Obi-Wan arrives at his vantage point, settles in, and prepares to watch —
    // setting the initial checked state from memory (the preference store).
    // We create the action with the right icon and label, read the stored preference
    // so the toggle button reflects whether linking was on or off last session.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action as a checkbox button wired to the view.
     * We read the preference store to restore the last-known check state so
     * the button doesn't silently reset to "off" every time the view is opened.
     * Subclasses must call {@link #init()} after their own constructor finishes
     * to avoid registering listeners before they're fully initialised.
     *
     * @param viewPart  the Eclipse view this action belongs to; used to reach
     *                  the workbench window's part service.
     * @param message   the label text for the toolbar/menu button.
     */
    public AbstractLinkWithEditorAction( ViewPart viewPart, String message )
    {
        super( message, AS_CHECK_BOX ); //$NON-NLS-1$
        setImageDescriptor(
            BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_LINK_WITH_EDITOR ) );
        setEnabled( true );
        setChecked( BrowserUIPlugin.getDefault().getPreferenceStore()
            .getBoolean( BrowserUIConstants.PREFERENCE_BROWSER_LINK_WITH_EDITOR ) );
        this.viewPart = viewPart;
    }


    // ── Obi-Wan Opens His Senses for the First Time ─────────────────────────────
    // After settling in, Obi-Wan deliberately opens himself to the Force —
    // only now does he begin actively monitoring for disturbances.
    // We register listeners only if the toggle starts checked; if it starts
    // unchecked we don't listen until the user explicitly enables it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the listener registration based on the current check state.
     * Subclasses call this at the end of their own constructor, after all their
     * fields are set, so the listeners can safely call back into the subclass.
     */
    protected void init()
    {
        // Enable the listeners
        if ( isChecked() )
        {
            registerListeners();
        }
    }


    // ── Obi-Wan Toggles His Awareness On or Off ──────────────────────────────────
    // Obi-Wan decides: "I will watch" or "I will stand down." He persists that
    // choice in the Force (the preference store) and acts accordingly —
    // immediately syncing if he's just switched on, stopping if he's off.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Toggles "link with editor" on or off, persists the preference, and
     * registers or unregisters listeners to match.
     * When switching on we also immediately sync the browser to whatever the
     * active editor currently shows — so there's no lag after the user enables it.
     */
    public void run()
    {
        setChecked( isChecked() );
        BrowserUIPlugin.getDefault().getPreferenceStore()
            .setValue( BrowserUIConstants.PREFERENCE_BROWSER_LINK_WITH_EDITOR, isChecked() );

        if ( isChecked() )
        {
            // Enable the listener
            registerListeners();

            // link
            IEditorPart activeEditor = viewPart.getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
            linkViewWithEditor( activeEditor );
        }
        else
        {
            // Disable the listener
            unregisterListeners();
        }
    }


    // ── Obi-Wan Reacts to the Disturbance ────────────────────────────────────────
    // Obi-Wan feels the ripple in the Force and turns his attention to the source.
    // Subclasses decide exactly what to do once the disturbance is identified —
    // typically navigating the browser tree to the relevant entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called whenever the linked editor changes its input.
     * Subclasses implement this to navigate the view (tree selection, scroll, etc.)
     * to match the editor's new content.
     *
     * @param part  the editor whose input just changed; may be null if there is
     *              no active editor.
     */
    protected abstract void linkViewWithEditor( IWorkbenchPart part );


    // ── Obi-Wan Withdraws to His Hut ────────────────────────────────────────────
    // Obi-Wan stops watching, clears his mind, releases all his attunements —
    // the vigil is over and resources are freed.
    // We unregister all listeners and null out references so there are no
    // memory leaks from listeners holding the view alive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up all listeners and releases references held by this action.
     * Must be called when the owning view is disposed, otherwise the listeners
     * will keep the view object in memory and may fire events into a dead view.
     */
    public void dispose()
    {
        if ( partListener != null && propertyListener != null )
        {
            unregisterListeners();
            propertyListener = null;
            partListener = null;
        }

        viewPart = null;
    }

}
