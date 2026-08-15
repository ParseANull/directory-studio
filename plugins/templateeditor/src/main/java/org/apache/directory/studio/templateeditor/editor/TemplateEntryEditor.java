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
package org.apache.directory.studio.templateeditor.editor;


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.EntryEditorUtils;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;


// ── CLASS: TemplateEntryEditor — MON MOTHMA RUNNING THE BRIEFING ─────────────────
// Mon Mothma doesn't just walk into a room and wing it — she works from a prepared
// briefing format that she uses the same way every time: open the dossier, present
// the template, take questions, then commit the plan. This abstract base class is
// that repeatable briefing format. Concrete subclasses (single-tab, multi-tab) only
// need to declare whether saves happen automatically or on demand — Mon Mothma's
// briefing procedure takes care of everything else.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for the template-based LDAP entry editor. This class is
 * an Eclipse {@link EditorPart} that wires together the lifecycle events (init,
 * create, dispose, save, focus) with a {@link TemplateEditorWidget} that does the
 * actual rendering. Concrete subclasses override {@link #isAutoSave()} to pick
 * single-tab-or-multi-tab and explicit-or-automatic saves.
 * Think of this as Mon Mothma's repeatable briefing format — same procedure every
 * time, but the venue (single tab vs. multiple tabs) differs.
 * <p>
 * This editor presents three logical areas:
 * <ul>
 *   <li>the template-driven form tab</li>
 *   <li>the table editor tab</li>
 *   <li>the LDIF editor tab</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class TemplateEntryEditor extends EditorPart implements INavigationLocationProvider, IEntryEditor,
    IReusableEditor, IShowEditorInput
{
    /** The Template Editor page */
    private TemplateEditorWidget templateEditorWidget;


    // ── INIT: MON MOTHMA TAKES HER SEAT AT THE TABLE ─────────────────────────────
    // Before the briefing can begin, Mon Mothma takes her seat and registers with
    // the room's communication system (the Eclipse site). We store the site and the
    // initial input so the editor knows where to direct events and what entry to show.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes this editor with its site and input. Called by Eclipse right after
     * instantiation — before the UI is created. We stash both references so subsequent
     * lifecycle methods can reach them.
     *
     * <p>For example — Mon Mothma takes her seat at the briefing table:</p>
     * <pre>
     *   setSite(site);   // "I'm connected to the room's comm."
     *   setInput(input); // "Hand me the dossier."
     * </pre>
     *
     * @param site   the editor site Eclipse provides; never {@code null}
     * @param input  the initial editor input (an LDAP entry wrapper); never {@code null}
     * @throws PartInitException if initialization fails fatally
     */
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        setSite( site );
        setInput( input );
    }


    // ── CREATE PART CONTROL: UNFURL THE BRIEFING HOLOGRAM ────────────────────────
    // Mon Mothma activates the holographic projector and the room's display comes
    // to life. We instantiate the TemplateEditorWidget — which builds the SWT form
    // from the matched template — and attach it to the parent composite Eclipse gave us.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT controls for this editor inside the given parent composite.
     * Instantiates a {@link TemplateEditorWidget} and calls its {@code init()} to
     * build the scrolled form, toolbar, and context menu.
     *
     * <p>For example — Mon Mothma activates the briefing hologram:</p>
     * <pre>
     *   templateEditorWidget = new TemplateEditorWidget(this);
     *   templateEditorWidget.init(parent); // "Hologram online."
     * </pre>
     *
     * @param parent  the SWT composite to build inside; provided by Eclipse
     */
    public void createPartControl( Composite parent )
    {
        templateEditorWidget = new TemplateEditorWidget( this );
        templateEditorWidget.init( parent );
    }


    // ── WORKING COPY MODIFIED: THE FIELD REPORT JUST CHANGED ─────────────────────
    // A field report comes in with updated information. Mon Mothma refreshes the
    // display so everyone in the room sees the latest data. If we're not in
    // auto-save mode she also marks the briefing as "pending approval" (dirty) so
    // someone has to explicitly commit the changes before they're locked in.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the shared working-copy mechanism whenever the underlying LDAP entry
     * data changes (e.g., the user typed in a field). We refresh the widget and, if
     * {@link #isAutoSave()} is {@code false}, fire a dirty notification so Eclipse
     * enables the Save action.
     *
     * <p>For example — the field report just came in:</p>
     * <pre>
     *   update(); // Refresh the display.
     *   if (!isAutoSave()) firePropertyChange(PROP_DIRTY); // "Needs sign-off."
     * </pre>
     *
     * @param source  the object that triggered the modification (ignored here)
     */
    public void workingCopyModified( Object source )
    {
        update();

        if ( !isAutoSave() )
        {
            // mark as dirty
            firePropertyChange( PROP_DIRTY );
        }
    }


    // ── DISPOSE: PACK UP THE BRIEFING ROOM ───────────────────────────────────────
    // The mission is over — Mon Mothma packs up the hologram and rolls up the maps.
    // We dispose the TemplateEditorWidget (which releases SWT resources like the
    // FormToolkit, the form itself, and all widget composites) before handing off
    // to EditorPart's own cleanup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes this editor, releasing all SWT resources held by the
     * {@link TemplateEditorWidget}. Always calls {@code super.dispose()} last so
     * Eclipse's EditorPart cleanup runs too.
     *
     * <p>For example — packing up the briefing room:</p>
     * <pre>
     *   templateEditorWidget.dispose(); // "Hologram down. Toolkit released."
     *   super.dispose();                // "Room closed."
     * </pre>
     */
    public void dispose()
    {
        // Template Editor Widget
        if ( templateEditorWidget != null )
        {
            templateEditorWidget.dispose();
        }

        super.dispose();
    }


    // ── CAN HANDLE: DOES THIS DOSSIER FIT OUR BRIEFING SCOPE? ───────────────────
    // Mon Mothma checks whether the entry's object classes match any template
    // we've loaded. If the preference is "show for any entry," we always say yes.
    // If it's "show only when a template matches," we run the BFS lookup. If there's
    // no entry (null) yet, we optimistically say yes — the widget will handle it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this editor can handle the given LDAP entry. The answer depends
     * on the user's "use template editor for" preference: always, or only when at
     * least one matching template exists.
     *
     * <p>For example — checking if the dossier fits our scope:</p>
     * <pre>
     *   if (prefs == ANY_ENTRY) return true;
     *   else return getMatchingTemplates(entry).size() > 0;
     * </pre>
     *
     * @param entry  the LDAP entry to check; may be {@code null}
     * @return {@code true} if this editor can display the entry
     */
    public boolean canHandle( IEntry entry )
    {
        int useTemplateEditorFor = EntryTemplatePlugin.getDefault().getPreferenceStore().getInt(
            EntryTemplatePluginConstants.PREF_USE_TEMPLATE_EDITOR_FOR );
        if ( useTemplateEditorFor == EntryTemplatePluginConstants.PREF_USE_TEMPLATE_EDITOR_FOR_ANY_ENTRY )
        {
            return true;
        }
        else if ( useTemplateEditorFor == EntryTemplatePluginConstants.PREF_USE_TEMPLATE_EDITOR_FOR_ENTRIES_WITH_TEMPLATE )
        {
            if ( entry == null )
            {
                return true;
            }

            return canBeHandledWithATemplate( entry );
        }

        return false;
    }


    // ── CAN BE HANDLED WITH A TEMPLATE: DOES THE DOSSIER HAVE A MATCHING FORM? ──
    // We check whether the TemplatesManager has at least one template whose object
    // classes overlap with the entry's — if yes, we can display it with a form.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the template registry contains at least one template
     * whose object-class set matches the given entry. Used by {@link #canHandle(IEntry)}
     * when the preference is set to "entries with template only."
     *
     * @param entry  the LDAP entry to check; must not be {@code null}
     * @return {@code true} if at least one matching template exists
     */
    private boolean canBeHandledWithATemplate( IEntry entry )
    {
        return ( EntryTemplatePluginUtils.getMatchingTemplates( entry ).size() > 0 );
    }


    // ── DO SAVE: COMMIT THE PLAN TO THE COMMAND CENTER ───────────────────────────
    // Mon Mothma signs the document and transmits it to fleet command. We call
    // saveSharedWorkingCopy() on the EntryEditorInput — which writes the modified
    // attributes back to the LDAP server. Only runs if isAutoSave() is false (the
    // auto-save path saves on every keystroke via workingCopyModified instead).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the editor's shared working copy to the LDAP server. Only meaningful
     * when {@link #isAutoSave()} returns {@code false} — in auto-save mode, changes
     * are written immediately via {@link #workingCopyModified(Object)}.
     *
     * <p>For example — Mon Mothma signs and transmits the plan:</p>
     * <pre>
     *   eei.saveSharedWorkingCopy(true, this);
     *   // "Changes committed to the LDAP server."
     * </pre>
     *
     * @param monitor  the progress monitor for long-running saves (may show a progress dialog)
     */
    public void doSave( IProgressMonitor monitor )
    {
        if ( !isAutoSave() )
        {
            EntryEditorInput eei = getEntryEditorInput();
            eei.saveSharedWorkingCopy( true, this );
        }
    }


    // ── IS DIRTY: HAS THE BRIEFING BEEN UPDATED SINCE SIGN-OFF? ─────────────────
    // Mon Mothma checks whether anyone has scribbled new notes on the plan since
    // the last formal sign-off. The shared working copy tracks this for us.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the working copy has unsaved changes — i.e., the user
     * has modified field values but hasn't saved yet. Eclipse uses this to enable
     * the Save action and show the "unsaved" asterisk in the editor tab title.
     *
     * @return {@code true} if there are unsaved changes in the working copy
     */
    public boolean isDirty()
    {
        return getEntryEditorInput().isSharedWorkingCopyDirty( this );
    }


    // ── IS SAVE AS ALLOWED: NO — WE DON'T COPY ENTRIES THIS WAY ────────────────
    // "File > Save As..." doesn't make sense for an LDAP entry editor — we don't
    // export to files here. Always returns false to disable that menu item.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — "Save As…" is not supported. LDAP entries are
     * saved back to the server, not exported to files through this mechanism.
     *
     * @return {@code false} always
     */
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    // ── DO SAVE AS: NO-OP — NEVER CALLED ─────────────────────────────────────────
    // isSaveAsAllowed() returns false, so Eclipse will never invoke this. It exists
    // only because EditorPart requires it as part of the contract.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — {@link #isSaveAsAllowed()} returns {@code false} so this is never
     * called by Eclipse. Required by the {@link EditorPart} contract.
     */
    public void doSaveAs()
    {
        // Nothing to do, will never occur as "Save As..." is not allowed
    }


    // ── SET FOCUS: POINT THE HOLOGRAM PROJECTOR AT THE FORM ─────────────────────
    // When the user clicks on the editor, we redirect keyboard focus into the
    // scrolled form so the next keypress lands in the right field. The widget
    // handles the details of which SWT control actually gets focus.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Transfers keyboard focus to the {@link TemplateEditorWidget}'s scrolled form.
     * Eclipse calls this when the user activates this editor (e.g., by clicking its
     * tab) so SWT knows where to route keyboard events.
     */
    public void setFocus()
    {
        if ( templateEditorWidget != null )
        {
            templateEditorWidget.setFocus();
        }
    }


    // ── GET ENTRY EDITOR INPUT: UNWRAP THE DOSSIER ───────────────────────────────
    // The raw Eclipse editor input might be any IEditorInput — we need it to be an
    // EntryEditorInput (our LDAP-specific subclass). We cast-check and return null
    // if the types don't match, so callers can guard against unexpected inputs.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current editor input cast to {@link EntryEditorInput}, which gives
     * access to the LDAP entry, the shared working copy, and the editor extension.
     * Returns {@code null} if the current input is not an {@link EntryEditorInput}.
     *
     * <p>For example — unwrapping the dossier:</p>
     * <pre>
     *   EntryEditorInput eei = getEntryEditorInput();
     *   IEntry entry = eei.getSharedWorkingCopy(this);
     * </pre>
     *
     * @return the current {@link EntryEditorInput}, or {@code null}
     */
    public EntryEditorInput getEntryEditorInput()
    {
        Object editorInput = getEditorInput();

        if ( editorInput instanceof EntryEditorInput )
        {
            return ( EntryEditorInput ) editorInput;
        }

        return null;
    }


    // ── UPDATE: REFRESH THE HOLOGRAM WITH CURRENT DATA ───────────────────────────
    // The briefing hologram needs to refresh when the underlying data changes — we
    // forward the call to the widget, which re-reads all attribute values from the
    // shared working copy and repopulates the form fields.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Tells the {@link TemplateEditorWidget} to refresh all its field values from
     * the current state of the shared working copy. Called from
     * {@link #workingCopyModified(Object)} whenever the entry data changes.
     */
    private void update()
    {
        if ( templateEditorWidget != null )
        {
            templateEditorWidget.update();
        }
    }


    // ── SET INPUT: SWAP THE DOSSIER ON THE TABLE ─────────────────────────────────
    // A new entry dossier replaces the previous one. We let the EditorPart superclass
    // store the new input, then update the tab title so it matches the new entry's DN.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the editor's input and refreshes the part name (the tab title) to
     * reflect the new entry's display name. Called by Eclipse when the editor input
     * changes (e.g., the user navigates to a different LDAP entry).
     *
     * @param input  the new editor input; must not be {@code null}
     */
    public void setInput( IEditorInput input )
    {
        super.setInput( input );

        setPartName( input.getName() );
    }


    // ── CREATE EMPTY NAVIGATION LOCATION: NO LOCATION YET ───────────────────────
    // Eclipse navigation history needs an empty placeholder location for this editor
    // type. We return null — no empty-state location is needed for template editors.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — template entry editors do not need an empty navigation
     * location placeholder. Eclipse uses this for editors that want a "before any
     * entry was selected" history entry, which we don't.
     *
     * @return {@code null} always
     */
    public INavigationLocation createEmptyNavigationLocation()
    {
        return null;
    }


    // ── CREATE NAVIGATION LOCATION: STAMP THIS ENTRY IN THE LOG ─────────────────
    // Every time the user navigates to a different entry, Eclipse calls this to
    // create a "breadcrumb" for the Back/Forward buttons. We create a
    // TemplateEntryEditorNavigationLocation that knows how to save/restore this
    // editor's current entry to Eclipse's navigation history memento.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link TemplateEntryEditorNavigationLocation} that captures the
     * current LDAP entry (DN, connection, type) so Eclipse can restore it when the
     * user clicks Back or Forward in the navigation history.
     *
     * <p>For example — stamping this entry in the navigation log:</p>
     * <pre>
     *   return new TemplateEntryEditorNavigationLocation(this);
     *   // "Entry cn=Luke,dc=rebels,dc=org logged in navigation history."
     * </pre>
     *
     * @return a new {@link TemplateEntryEditorNavigationLocation} for this editor
     */
    public INavigationLocation createNavigationLocation()
    {
        return new TemplateEntryEditorNavigationLocation( this );
    }


    // ── SHOW EDITOR INPUT: SWAP TO A NEW ENTRY IN THE SAME TAB ──────────────────
    // The single-tab editor reuses one tab for all entries, so Eclipse calls this
    // when the user picks a different entry in the browser tree. We guard against
    // showing the same entry twice, prompt a save if there are dirty changes, then
    // swap the input and rebuild the form via editorInputChanged().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Switches this editor to display a different LDAP entry without opening a new
     * tab. Called by Eclipse when the editor implements {@link IReusableEditor}.
     * Guards against no-op re-shows, prompts for unsaved changes, then updates the
     * input and rebuilds the form.
     *
     * <p>For example — swapping the mission dossier on the briefing table:</p>
     * <pre>
     *   setInput(newEntry);
     *   templateEditorWidget.editorInputChanged(); // Rebuild form for new entry.
     * </pre>
     *
     * @param input  the new editor input to display; typically an {@link EntryEditorInput}
     */
    public void showEditorInput( IEditorInput input )
    {
        if ( input instanceof EntryEditorInput )
        {
            /*
             * Optimization: no need to set the input again if the same input is already set
             */
            if ( getEntryEditorInput() != null
                && getEntryEditorInput().getResolvedEntry() == ( ( EntryEditorInput ) input ).getResolvedEntry() )
            {
                return;
            }

            // If the editor is dirty, let's ask for a save before changing the input
            if ( isDirty() )
            {
                if ( !EntryEditorUtils.askSaveSharedWorkingCopyBeforeInputChange( this ) )
                {
                    return;
                }
            }

            // now set the real input and mark history location
            setInput( input );
            getSite().getPage().getNavigationHistory().markLocation( this );
            firePropertyChange( BrowserUIConstants.INPUT_CHANGED );

            // Updating the input on the template editor widget
            templateEditorWidget.editorInputChanged();
        }
    }

}
