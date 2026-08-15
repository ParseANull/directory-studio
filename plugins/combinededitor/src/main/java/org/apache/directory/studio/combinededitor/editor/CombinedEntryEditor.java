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
package org.apache.directory.studio.combinededitor.editor;


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.EntryEditorUtils;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import org.apache.directory.studio.combinededitor.CombinedEditorPlugin;
import org.apache.directory.studio.combinededitor.CombinedEditorPluginConstants;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginUtils;
import org.apache.directory.studio.templateeditor.actions.SwitchTemplateListener;
import org.apache.directory.studio.templateeditor.editor.TemplateEditorWidget;
import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: CombinedEntryEditor — The Tantive IV Bridge: Two Views, One Entry ──
// The Tantive IV's bridge gives Captain Antilles and Princess Leia simultaneous
// views of the same crisis: the nav console shows raw coordinates, the tactical
// display shows the visual picture, and the comms screen shows the raw signal —
// all three panels looking at the same reality from different angles.
// CombinedEntryEditor is exactly that: one LDAP entry, three tab views
// (Template, Table, LDIF), all edits routed through a shared working copy so
// changes in one view immediately appear in the others.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The abstract base for the combined entry editor — an Eclipse editor that shows
 * a single LDAP directory entry through three simultaneous tab views.
 * <ul>
 *   <li>The <b>Template</b> tab renders the entry using a declarative form template.</li>
 *   <li>The <b>Table</b> tab shows all attributes in a tree/table widget.</li>
 *   <li>The <b>LDIF</b> tab shows the raw LDIF text representation.</li>
 * </ul>
 * All three views share a single working copy of the entry — edits in one tab
 * propagate to the others through the
 * {@link EntryEditorInput#getSharedWorkingCopy(IEntryEditor)} mechanism.
 * Concrete subclasses ({@link SingleTabCombinedEntryEditor},
 * {@link MultiTabCombinedEntryEditor}) differ only in whether they auto-save
 * or wait for an explicit Save command.
 * Think of this as the Tantive IV bridge — multiple views of the same situation,
 * co-ordinated by a central shared data model.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class CombinedEntryEditor extends EditorPart implements INavigationLocationProvider, IEntryEditor,
    IReusableEditor, IShowEditorInput, SwitchTemplateListener
{
    /** The Template Editor page */
    private TemplateEditorPage templateEditorPage;

    /** The Table Editor page */
    private TableEditorPage tableEditorPage;

    /** The LDIF Editor page */
    private LdifEditorPage ldifEditorPage;

    /** The Tab Folder */
    private CTabFolder tabFolder;

    /** The tab associated with the Template Editor */
    private CTabItem templateEditorTab;

    /** The tab associated with the Table Editor */
    private CTabItem tableEditorTab;

    /** The tab associated with the LDIF Editor */
    private CTabItem ldifEditorTab;


    // ── Antilles Receives the Mission Briefing — Editor Is Registered ─────────
    // Captain Antilles takes the mission packet, confirms the ship (site) and
    // the target (input), and reports ready to the fleet command.
    // init() is Eclipse's hook to give us the editor site and initial input —
    // we store both via the parent class methods.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse to initialise the editor with its site and input.
     * We store the site (which gives us access to the workbench page) and the
     * input (which wraps the LDAP entry we're editing) via the parent class.
     *
     * @param site   the editor site — lets us register selection providers and
     *               access the navigation history.
     * @param input  the initial {@link EntryEditorInput} wrapping the LDAP entry.
     * @throws PartInitException  if the editor cannot be initialised.
     */
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        setSite( site );
        setInput( input );
    }


    // ── The Bridge Is Assembled — Three Stations Come Online ──────────────────
    // When the Tantive IV launches, Antilles activates the three bridge stations
    // in sequence: Template (tactical display), Table (nav console), and LDIF
    // (raw comms screen).  He then selects the correct station based on standing
    // orders (the user's preference for which tab to show first).
    // createPartControl() builds the CTabFolder and the three editor pages,
    // then selects the first tab based on the default-editor preference.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the editor's SWT widget tree and selects the default starting tab.
     * We create a {@link CTabFolder} at the bottom, add the three editor pages
     * as tab items, and immediately select (and initialise) the tab that matches
     * the user's default-editor preference.  If the user prefers Template but
     * no template matches this entry, we auto-switch to their chosen fallback tab.
     *
     * @param parent  the SWT composite provided by Eclipse — we create the
     *                {@link CTabFolder} as its child.
     */
    public void createPartControl( Composite parent )
    {
        // Creating the TabFolder
        tabFolder = new CTabFolder( parent, SWT.BOTTOM );

        // Creating the editor pages and tab items
        // The Template editor item
        templateEditorPage = new TemplateEditorPage( this );
        templateEditorTab = templateEditorPage.getTabItem();

        // The Table editor item
        tableEditorPage = new TableEditorPage( this );
        tableEditorTab = tableEditorPage.getTabItem();

        // The LDIF editor item
        ldifEditorPage = new LdifEditorPage( this );
        ldifEditorTab = ldifEditorPage.getTabItem();

        // Getting the preference store
        IPreferenceStore store = CombinedEditorPlugin.getDefault().getPreferenceStore();

        // Getting the default editor
        int defaultEditor = store.getInt( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR );

        switch ( defaultEditor )
        {
            case CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TEMPLATE :
                // Getting the boolean indicating if the user wants to auto-switch the template editor
                boolean autoSwitchToAnotherEditor = store
                    .getBoolean( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_TO_ANOTHER_EDITOR );

                if ( autoSwitchToAnotherEditor && !canBeHandledWithATemplate() )
                {
                    switch ( store.getInt( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR ) )
                    {
                        case CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR_TABLE :
                            // Selecting the Table Editor
                            tabFolder.setSelection( tableEditorTab );
                            // Forcing the initialization of the first tab item,
                            // because the listener is not triggered when selecting a tab item programmatically
                            tableEditorPage.init();
                            break;

                        case  CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR_LDIF :
                            // Selecting the LDIF Editor
                            tabFolder.setSelection( ldifEditorTab );
                            // Forcing the initialization of the first tab item,
                            // because the listener is not triggered when selecting a tab item programmatically
                            ldifEditorPage.init();
                    }
                }
                else
                {
                    // Selecting the Template Editor
                    tabFolder.setSelection( templateEditorTab );
                    // Forcing the initialization of the first tab item,
                    // because the listener is not triggered when selecting a tab item programmatically
                    templateEditorPage.init();
                }

                break;

            case CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TABLE :
                // Selecting the Table Editor
                tabFolder.setSelection( tableEditorTab );
                // Forcing the initialization of the first tab item,
                // because the listener is not triggered when selecting a tab item programmatically
                tableEditorPage.init();

                break;

            case CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_LDIF :
                // Selecting the LDIF Editor
                tabFolder.setSelection( ldifEditorTab );
                // Forcing the initialization of the first tab item,
                // because the listener is not triggered when selecting a tab item programmatically
                ldifEditorPage.init();
        }
    }


    // ── A Crew Member Edits the Navigation Data — Working Copy Is Modified ─────
    // A nav officer changes a coordinate on the Tantive IV; Antilles sees the
    // "modified" light go on and routes the update to all other bridge stations
    // so they're all looking at the same revised picture.
    // workingCopyModified() is triggered when any page modifies the shared
    // working copy — we update all pages and (if not auto-save) fire the dirty flag.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by any page (via the shared working copy) when the entry's data changes.
     * We refresh the currently visible page and, if we're not in auto-save mode,
     * fire a property change to mark the editor as dirty (unsaved changes).
     *
     * @param source  the object that triggered the modification — used by the
     *                shared working copy infrastructure to avoid echo updates.
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


    // ── The Bridge Shuts Down — All Stations Released ────────────────────────
    // When the Tantive IV is decommissioned, Antilles systematically powers down
    // every station in the correct order: tab folder first, then each tab item,
    // then each page — making sure nothing holds a dangling reference.
    // dispose() does the same: null checks prevent double-dispose, and each
    // component is released before the parent super.dispose() is called.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Releases all SWT resources held by the editor and its three pages.
     * We dispose the tab folder, each tab item, and each page in turn.
     * Guard conditions ({@code isDisposed()} checks) prevent errors if Eclipse
     * calls this more than once or in an unexpected order.
     */
    public void dispose()
    {
        //
        // Disposing the TabFolder, its tabs and Editor Pages
        //

        // Tab Folder
        if ( ( tabFolder != null ) && ( !tabFolder.isDisposed() ) )
        {
            tabFolder.dispose();
        }

        // Template Editor Tab
        if ( ( templateEditorTab != null ) && ( !templateEditorTab.isDisposed() ) )
        {
            templateEditorTab.dispose();
        }

        // Table Editor Tab
        if ( ( tableEditorTab != null ) && ( !tableEditorTab.isDisposed() ) )
        {
            tableEditorTab.dispose();
        }

        // LDIF Editor Tab
        if ( ( ldifEditorTab != null ) && ( !ldifEditorTab.isDisposed() ) )
        {
            ldifEditorTab.dispose();
        }

        // Template Editor Page
        if ( templateEditorPage != null )
        {
            templateEditorPage.dispose();
        }

        // Table Editor Page
        if ( tableEditorPage != null )
        {
            tableEditorPage.dispose();
        }

        // LDIF Editor Page
        if ( ldifEditorPage != null )
        {
            ldifEditorPage.dispose();
        }

        super.dispose();
    }


    // ── Any Entry Can Come Aboard the Tantive IV ──────────────────────────────
    // The Tantive IV isn't specialised — it can carry any type of cargo.
    // canHandle() always returns true because the combined editor works for any
    // LDAP entry regardless of object class or schema.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} for any LDAP entry — this editor handles all entries.
     * The combined editor is the catch-all editor that works for every entry
     * regardless of whether a specific template exists.
     *
     * @param entry  the LDAP entry to check — not used, we always return {@code true}.
     * @return       always {@code true}.
     */
    public boolean canHandle( IEntry entry )
    {
        return true;
    }


    // ── Does the Cargo Match a Known Manifest Template? ───────────────────────
    // Before loading cargo the nav officer checks the cargo manifest to see if
    // there's a known template for this type of shipment — if yes, the Template
    // tab will have something useful to show; if no, we might want to switch.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given entry matches at least one registered template.
     * If it doesn't, the Template tab won't be able to render anything useful and
     * the user's auto-switch preference kicks in to select a different tab.
     *
     * @param entry  the LDAP entry to check against the template registry.
     * @return       {@code true} if at least one template matches this entry.
     */
    private boolean canBeHandledWithATemplate( IEntry entry )
    {
        return ( EntryTemplatePluginUtils.getMatchingTemplates( entry ).size() > 0 );
    }


    // ── Does the Current Input Match a Known Template? ────────────────────────
    // The nav officer checks the current cargo manifest without the caller having
    // to supply the entry themselves — we pull it from the editor input.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the editor's current entry matches at least one template.
     * This is the no-arg variant used internally; it pulls the entry from the
     * current {@link EntryEditorInput} and delegates to
     * {@link #canBeHandledWithATemplate(IEntry)}.
     *
     * @return  {@code true} if the current entry has a matching template,
     *          {@code false} if the editor input is absent or has no matching template.
     */
    private boolean canBeHandledWithATemplate()
    {
        IEditorInput editorInput = getEditorInput();

        if ( editorInput instanceof EntryEditorInput )
        {
            IEntry entry = ( ( EntryEditorInput ) editorInput ).getResolvedEntry();

            if ( entry != null )
            {
                return canBeHandledWithATemplate( entry );
            }
        }

        return false;
    }


    // ── Antilles Commits the Mission Data to Permanent Record ─────────────────
    // When the mission is complete Antilles commits the nav data to the ship's
    // permanent log — that's the "save" operation in LDAP terms: pushing the
    // working copy back to the actual directory entry on the server.
    // doSave() triggers the EntryEditorInput to persist the shared working copy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the editor's current working copy back to the LDAP directory.
     * In auto-save mode this is a no-op (changes are already persisted inline);
     * otherwise we call {@link EntryEditorInput#saveSharedWorkingCopy} which
     * writes the modified entry back to the server.
     *
     * @param monitor  an Eclipse progress monitor — we pass it through to the
     *                 save operation for progress reporting.
     */
    public void doSave( IProgressMonitor monitor )
    {
        if ( !isAutoSave() )
        {
            EntryEditorInput eei = getEntryEditorInput();
            eei.saveSharedWorkingCopy( true, this );
        }
    }


    // ── Has the Nav Data Been Modified Since Last Save? ───────────────────────
    // Antilles checks whether any station has made unsaved changes to the mission
    // data — the "dirty" flag tells him whether a Save is needed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the shared working copy has unsaved changes.
     * Eclipse uses this to decide whether to show an asterisk in the editor tab
     * and prompt the user before closing.
     *
     * @return  {@code true} if the working copy has been modified since the last save.
     */
    public boolean isDirty()
    {
        return getEntryEditorInput().isSharedWorkingCopyDirty( this );
    }


    // ── The Tantive IV Doesn't Have a "Save As New Ship" Feature ─────────────
    // You can't rename the Tantive IV mid-mission and call it something else —
    // Save As is not a concept that applies here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code false} — Save As is not supported by this editor.
     * LDAP entries have a fixed DN (path); we can't "save as" to a different DN
     * from this editor (use the Rename action for that).
     *
     * @return  always {@code false}.
     */
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    // ── No-Op: Save As Is Never Called ───────────────────────────────────────
    // The "Save As" button is disabled; this method will never be invoked.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — {@link #isSaveAsAllowed()} returns {@code false} so this is never called.
     */
    public void doSaveAs()
    {
        // Nothing to do, will never occur as "Save As..." is not allowed
    }


    // ── The Bridge Takes the Conn — Focus to the Tab Folder ──────────────────
    // Antilles takes the conn by placing his hand on the main control surface
    // of the bridge; the tab folder is our equivalent — the main interactive area.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Routes keyboard focus to the tab folder (and by extension to the active page).
     * Eclipse calls this when the editor is activated or when the user navigates
     * back to it from another part.
     */
    public void setFocus()
    {
        if ( ( tabFolder != null ) && ( !tabFolder.isDisposed() ) )
        {
            tabFolder.setFocus();
        }
    }


    // ── Antilles Reads the Current Mission Manifest ───────────────────────────
    // Antilles picks up the current mission manifest from the bridge station —
    // it's an EntryEditorInput wrapping the LDAP entry we're editing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the typed {@link EntryEditorInput} for the current editor input.
     * Convenience cast so callers don't have to cast {@link #getEditorInput()}
     * themselves.  Returns {@code null} if the editor input is not an
     * {@link EntryEditorInput} (which shouldn't happen in normal use).
     *
     * @return  the current {@link EntryEditorInput}, or {@code null}.
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


    // ── Route the Update Order to the Active Station ──────────────────────────
    // Antilles calls "Active station: update your display" — only the currently
    // selected tab gets the order; the others will be refreshed when selected.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Forwards an update request to whichever page is currently visible.
     * We only update the selected page; the others will pick up the change
     * the next time the user clicks their tab (via the tab-selection listener).
     */
    private void update()
    {
        ICombinedEntryEditorPage selectedPage = getEditorPageFromSelectedTab();

        if ( selectedPage != null )
        {
            selectedPage.update();
        }
    }


    // ── New Target Locked — Editor Input Changes ──────────────────────────────
    // Antilles locks onto a new target and updates the Tantive IV's part name
    // so the nav display shows the right destination label.
    // setInput() is called when the editor is reused for a different entry
    // (IReusableEditor contract) — we store the new input and update the tab title.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets a new editor input and updates the editor part name shown in the tab.
     * Eclipse (via the {@link IReusableEditor} contract) calls this when we're
     * being reused for a different entry rather than opening a brand new editor.
     *
     * @param input  the new editor input wrapping the new LDAP entry.
     */
    public void setInput( IEditorInput input )
    {
        super.setInput( input );

        setPartName( input.getName() );
    }


    // ── Fleet History Records an Empty Position ───────────────────────────────
    // The fleet's navigation history sometimes needs to record a placeholder —
    // this editor doesn't support that, so we return null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — we don't use empty navigation locations.
     * This is required by the {@link INavigationLocationProvider} contract.
     *
     * @return  always {@code null}.
     */
    public INavigationLocation createEmptyNavigationLocation()
    {
        return null;
    }


    // ── Fleet History Records the Current Position ────────────────────────────
    // The Tantive IV's nav computer logs the current hyperspace coordinates so
    // the crew can jump back here later (browser back button).
    // createNavigationLocation() gives Eclipse a snapshot of the current entry
    // so the navigation history can restore it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation location snapshot of the current editor input.
     * Eclipse stores this in the navigation history so the user can navigate
     * backward and forward between previously visited entries (like a browser).
     *
     * @return  a new {@link CombinedEntryEditorNavigationLocation} for this entry.
     */
    public INavigationLocation createNavigationLocation()
    {
        return new CombinedEntryEditorNavigationLocation( this );
    }


    // ── New Coordinates Arrive — Switch to the New Entry ──────────────────────
    // A new set of nav coordinates comes in and Antilles reconfigures all
    // bridge stations to point at the new destination, first asking the crew
    // to save any unsaved changes from the previous mission.
    // showEditorInput() is IShowEditorInput's callback: we switch to a new entry
    // while reusing the same editor window.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Switches this reusable editor to a different LDAP entry.
     * Called by the browser when the user selects a different entry in the tree
     * and the preference is to reuse the existing editor rather than open a new one.
     * We skip if the same entry is already showing (optimisation), prompt for save
     * if dirty, then update the input and notify all three pages.
     *
     * @param input  the new {@link EntryEditorInput} to display.
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

            // Getting the preference store
            IPreferenceStore store = CombinedEditorPlugin.getDefault().getPreferenceStore();

            // Getting the default editor
            switch ( store.getInt( CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR ) )
            {
                case CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TEMPLATE :
                    // Getting the boolean indicating if the user wants to auto-switch the template editor
                    boolean autoSwitchToAnotherEditor = store
                        .getBoolean( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_TO_ANOTHER_EDITOR );

                    if ( autoSwitchToAnotherEditor && !canBeHandledWithATemplate() )
                    {
                        switch ( store.getInt( CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR ) )
                        {
                            case  CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR_TABLE :
                                // Selecting the Table Editor
                                tabFolder.setSelection( tableEditorTab );
                                // Forcing the initialization of the first tab item,
                                // because the listener is not triggered when selecting a tab item programmatically
                                tableEditorPage.init();
                                break;

                            case CombinedEditorPluginConstants.PREF_AUTO_SWITCH_EDITOR_LDIF :
                                // Selecting the LDIF Editor
                                tabFolder.setSelection( ldifEditorTab );
                                // Forcing the initialization of the first tab item,
                                // because the listener is not triggered when selecting a tab item programmatically
                                ldifEditorPage.init();
                        }
                    }
                    else
                    {
                        // Selecting the Template Editor
                        tabFolder.setSelection( templateEditorTab );
                        // Forcing the initialization of the first tab item,
                        // because the listener is not triggered when selecting a tab item programmatically
                        templateEditorPage.init();
                    }

                    break;

                case CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_TABLE :
                    // Selecting the Table Editor
                    tabFolder.setSelection( tableEditorTab );
                    // Forcing the initialization of the first tab item,
                    // because the listener is not triggered when selecting a tab item programmatically
                    tableEditorPage.init();
                    break;

                case  CombinedEditorPluginConstants.PREF_DEFAULT_EDITOR_LDIF :
                    // Selecting the LDIF Editor
                    tabFolder.setSelection( ldifEditorTab );
                    // Forcing the initialization of the first tab item,
                    // because the listener is not triggered when selecting a tab item programmatically
                    ldifEditorPage.init();
                    break;
            }

            // Noticing all pages that the editor input has changed
            templateEditorPage.editorInputChanged();
            tableEditorPage.editorInputChanged();
            ldifEditorPage.editorInputChanged();
        }
    }


    // ── Antilles Identifies Which Station Is Active ────────────────────────────
    // Antilles looks at the station indicator lights and identifies which one is
    // currently selected — he then routes the update order only to that station.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the editor page that corresponds to the currently selected tab.
     * We compare the selected {@link CTabItem} against each known tab item
     * and return the matching page.
     *
     * @return  the currently active {@link ICombinedEntryEditorPage}, or
     *          {@code null} if no tab is selected.
     */
    private ICombinedEntryEditorPage getEditorPageFromSelectedTab()
    {
        CTabItem selectedTabItem = getSelectedTabItem();

        if ( selectedTabItem != null )
        {
            // Template Editor Tab
            if ( selectedTabItem.equals( templateEditorTab ) )
            {
                return templateEditorPage;
            }
            // Table Editor Tab
            else if ( selectedTabItem.equals( tableEditorTab ) )
            {
                return tableEditorPage;
            }
            // LDIF Editor Tab
            else if ( selectedTabItem.equals( ldifEditorTab ) )
            {
                return ldifEditorPage;
            }
        }

        return null;
    }


    // ── Template Display Updates After a Manual Template Switch ───────────────
    // The tactical officer manually switches the template overlay to a different
    // one; the Template station needs to redraw its form with the new layout.
    // templateSwitched() relays this signal down to the TemplateEditorPage.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the template infrastructure when the user manually switches templates.
     * We forward the notification to the {@link TemplateEditorPage} which handles
     * the actual UI update.
     *
     * @param templateEditorWidget  the widget that fired the switch event.
     * @param template              the newly selected template.
     */
    public void templateSwitched( TemplateEditorWidget templateEditorWidget, Template template )
    {
        if ( templateEditorPage != null )
        {
            templateEditorPage.templateSwitched( templateEditorWidget, template );
        }
    }


    // ── Access the Bridge's Station Panel ────────────────────────────────────
    // Any crew member can grab a handle to the main station panel (tab folder)
    // to query or manipulate its state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link CTabFolder} that hosts the three editor pages.
     * Used by editor pages to create their own {@link CTabItem} children and
     * to register tab-selection listeners.
     *
     * @return  the tab folder widget.
     */
    public CTabFolder getTabFolder()
    {
        return tabFolder;
    }


    // ── Which Station Has the Conn Right Now? ─────────────────────────────────
    // Antilles checks which station has the current "conn" indicator — that's
    // the tab currently selected in the tab folder.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected {@link CTabItem} in the editor's tab folder.
     *
     * @return  the selected tab item, or {@code null} if nothing is selected.
     */
    public CTabItem getSelectedTabItem()
    {
        return tabFolder.getSelection();
    }


    /**
     * Get the {@link TemplateEditorPage} page.
     *
     * @return the {@link TemplateEditorPage} page
     *
    public TemplateEditorPage getTemplateEditorPage()
    {
        return templateEditorPage;
    }


    /**
     * Get the {@link TableEditorPage} page.
     *
     * @return the {@link TableEditorPage} page
     *
    public TableEditorPage getTableEditorPage()
    {
        return tableEditorPage;
    }


    /**
     * Get the {@link LdifEditorPage} page.
     *
     * @return the {@link LdifEditorPage} page
     *
    public LdifEditorPage getLdifEditorPage()
    {
        return ldifEditorPage;
    }*/
}
