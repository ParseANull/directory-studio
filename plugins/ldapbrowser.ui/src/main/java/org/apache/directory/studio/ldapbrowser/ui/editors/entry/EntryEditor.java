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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.EntryEditorUtils;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


// ── CLASS: EntryEditor — TANTIVE IV BRIDGE, ALL STATIONS MANNED ──────────────
// On the Tantive IV bridge, each crew member — navigator, comms officer, gunner —
// has a dedicated station; the captain holds them together as a unit under fire.
// EntryEditor is that captain: it wires up the attribute-table widget, action group,
// universal listener, and outline page so users get a coherent panel for one LDAP entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The base class for all LDAP entry editor panels in Directory Studio.
 * It coordinates the attribute-table widget, toolbar actions, event listener,
 * and the optional outline page so the user can view and edit a single LDAP entry.
 * Think of this class as the Tantive IV bridge: each component has its station,
 * and this class makes sure they all fire in the right order and talk to each other.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class EntryEditor extends EditorPart implements IEntryEditor, INavigationLocationProvider,
    IReusableEditor, IShowEditorInput
{

    /** The editor configuration. */
    protected EntryEditorConfiguration configuration;

    /** The action group. */
    protected EntryEditorActionGroup actionGroup;

    /** The main widget. */
    protected EntryEditorWidget mainWidget;

    /** The universal listener. */
    protected EntryEditorUniversalListener universalListener;

    /** The outline page. */
    protected EntryEditorOutlinePage outlinePage;

    IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener()
    {
        @Override
        public void propertyChange( org.eclipse.jface.util.PropertyChangeEvent event )
        {
            // set the input again if the auto-save option has been changed
            if ( event.getProperty() != null )
            {
                if ( event.getProperty().equals( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_SINGLE_TAB )
                    || event.getProperty().equals( BrowserCommonConstants.PREFERENCE_ENTRYEDITOR_AUTOSAVE_MULTI_TAB ) )
                {
                    setInput( getEditorInput() );
                }
            }
        }
    };


    // ── CAPTAIN TAKES THE CONN ────────────────────────────────────────────────
    // The Tantive IV captain strides onto the bridge, claims his post, and links
    // into the ship's communications network so fleet orders reach him in real time.
    // We hook up the editor site, push the first input, and subscribe to preference
    // changes so we react when the user toggles auto-save later on.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes this editor with its Eclipse site and the first editor input.
     * Called by the Eclipse workbench when the part is first created — we register
     * a preference listener here so we can react to auto-save toggle changes.
     *
     * <p>For example — the captain does:</p>
     * <pre>
     *   setSite(bridge);       // claim your station on the Tantive IV
     *   setInput(missionPlan); // know the current target
     *   listenForNewOrders();  // stay tuned to command channel
     * </pre>
     *
     * @param site   The Eclipse editor site — gives us the workbench page and action bars.
     * @param input  The initial editor input, typically an {@link EntryEditorInput} wrapping an LDAP entry.
     * @throws PartInitException if something goes wrong during initialization (e.g., bad input type).
     */
    @Override
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        setSite( site );
        setInput( input );
        BrowserCommonActivator.getDefault().getPreferenceStore().addPropertyChangeListener( propertyChangeListener );
    }


    // ── NEW ORDERS ARRIVE ON THE BRIDGE ──────────────────────────────────────
    // A new mission directive reaches the Tantive IV bridge — the captain relays
    // it to every station: navigator updates the course, comms updates the channel.
    // When a new LDAP entry is loaded, we push it to the widget, update the
    // tab name, and tell the outline page to refresh its tree view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Pushes a new editor input into this editor, updating all components.
     * Called both during initialization and whenever the user navigates to a
     * different LDAP entry — it keeps the widget, tab title, and outline in sync.
     *
     * <p>For example — when new orders arrive:</p>
     * <pre>
     *   setInput(newMissionPlan);  // Tantive IV receives updated target coordinates
     *   updateNavigator();         // widget shows the new entry's attributes
     *   updateBriefingRoom();      // outline page tree reflects the new entry
     * </pre>
     *
     * @param input  The new editor input; expected to be an {@link EntryEditorInput}.
     */
    @Override
    public void setInput( IEditorInput input )
    {
        super.setInput( input );

        EntryEditorInput eei = getEntryEditorInput();
        setEntryEditorWidgetInput( eei );
        setEditorName( eei );

        // refresh outline
        if ( outlinePage != null )
        {
            outlinePage.refresh();
        }
    }


    // ── ALL BRIDGE STATIONS COME ONLINE ──────────────────────────────────────
    // The Tantive IV bridge is being built out — consoles light up one by one,
    // each officer takes their position, and the captain hooks them all together.
    // We create the widget, config, action group, and listener in sequence
    // so they can all reference each other correctly from the start.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the editor's SWT UI inside the given parent composite.
     * Eclipse calls this once when the editor tab is first opened; we create
     * the attribute-table widget, wire up actions/menus, and start the event listener.
     *
     * <p>For example — standing up the Tantive IV bridge:</p>
     * <pre>
     *   console = new NavigatorConsole(bridge);   // widget inside parent composite
     *   weaponsBay = new ActionGroup(console);    // actions wired to toolbar/menus
     *   commsOfficer = new UniversalListener(me); // starts listening for events
     * </pre>
     *
     * @param parent  The SWT composite that Eclipse provides as the editor's container.
     */
    @Override
    public void createPartControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        composite.setLayout( layout );

        PlatformUI.getWorkbench().getHelpSystem().setHelp( composite,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_table_entry_editor" ); //$NON-NLS-1$ //$NON-NLS-2$

        // create configuration
        configuration = new EntryEditorConfiguration( this );

        // create main widget
        mainWidget = new EntryEditorWidget( configuration );
        mainWidget.createWidget( composite );

        // create actions and context menu and register global actions
        actionGroup = new EntryEditorActionGroup( this );
        actionGroup.fillToolBar( mainWidget.getToolBarManager() );
        actionGroup.fillMenu( mainWidget.getMenuManager() );
        actionGroup.enableGlobalActionHandlers( getEditorSite().getActionBars() );
        actionGroup.fillContextMenu( mainWidget.getContextMenuManager() );

        // create the listener
        getSite().setSelectionProvider( mainWidget.getViewer() );
        universalListener = new EntryEditorUniversalListener( this );
        setInput( getEditorInput() );
    }


    // ── CAPTAIN FOCUSES ON THE MAIN SCREEN ───────────────────────────────────
    // The captain turns toward the main viewport, giving it full attention.
    // Keyboard focus goes to the attribute-table widget so the user can
    // start typing or navigating immediately without clicking around.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Routes keyboard focus to the main attribute-table widget.
     * Eclipse calls this when the user clicks on our editor tab; we need to
     * explicitly forward focus so shortcuts work without an extra click.
     */
    @Override
    public void setFocus()
    {
        mainWidget.setFocus();
    }


    // ── REQUESTING THE BRIEFING ROOM ─────────────────────────────────────────
    // A crew member asks the captain for access to the briefing room (outline view).
    // If it hasn't been set up yet, or was destroyed and needs rebuilding, the
    // captain creates a fresh one and hands over the access code.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an adapter for a requested interface, most importantly the outline page.
     * Eclipse calls this when the Outline view wants to show content for our editor;
     * we lazily create the outline page here if it hasn't been created yet.
     *
     * <p>For example — the briefing room is ready:</p>
     * <pre>
     *   if (request == IContentOutlinePage.class) {
     *     if (briefingRoom == null || briefingRoom.isDestroyed()) {
     *       briefingRoom = new EntryEditorOutlinePage(this);
     *     }
     *     return briefingRoom; // hand it over
     *   }
     * </pre>
     *
     * @param required  The interface class being requested (usually {@link IContentOutlinePage}).
     * @return  The requested adapter, or whatever the superclass provides if we don't handle it.
     */
    @Override
    public Object getAdapter( Class required )
    {
        if ( IContentOutlinePage.class.equals( required ) )
        {
            if ( outlinePage == null || outlinePage.getControl() == null || outlinePage.getControl().isDisposed() )
            {
                outlinePage = new EntryEditorOutlinePage( this );
            }

            return outlinePage;
        }

        return super.getAdapter( required );
    }


    // ── CREW STANDS DOWN, BRIDGE GOES DARK ───────────────────────────────────
    // The Tantive IV is about to be decommissioned — each officer shuts down
    // their console in reverse order, and the captain disconnects from fleet comms.
    // We null out each component after disposing it so GC can do its job and
    // there's no chance of dangling references causing NPEs later.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Tears down this editor, releasing all held resources.
     * Eclipse calls this when the editor tab is closed; we dispose each component
     * in dependency order (listener → widget → actions → config) and remove our
     * preference listener so we don't keep the editor alive via the store.
     */
    @Override
    public void dispose()
    {
        if ( configuration != null )
        {
            universalListener.dispose();
            universalListener = null;
            mainWidget.dispose();
            mainWidget = null;
            actionGroup.dispose();
            actionGroup = null;
            configuration.dispose();
            configuration = null;
            getSite().setSelectionProvider( null );
            BrowserCommonActivator.getDefault().getPreferenceStore().removePropertyChangeListener(
                propertyChangeListener );
        }

        super.dispose();
    }


    // ── CAPTAIN SIGNS AND TRANSMITS THE MISSION REPORT ───────────────────────
    // The captain finalizes the mission log, committing all changes permanently
    // to the fleet database before handing control back to command.
    // In manual-save mode we flush the shared working copy to the LDAP server,
    // persisting all the edits the user has made since the last save.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current entry to the LDAP server when not in auto-save mode.
     * Eclipse calls this in response to Ctrl+S or the File → Save menu item.
     * In auto-save mode this is a no-op because changes are sent immediately as they're made.
     *
     * @param monitor  Progress monitor Eclipse provides for long-running save operations.
     */
    @Override
    public void doSave( final IProgressMonitor monitor )
    {
        if ( !isAutoSave() )
        {
            EntryEditorInput eei = getEntryEditorInput();
            eei.saveSharedWorkingCopy( true, this );
        }
    }


    // ── CAPTAIN HAS NO SPARE COPY OF THE LOG ─────────────────────────────────
    // The captain is asked if they can write the log to a different location.
    // There's only one official archive — we don't support Save As here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — we don't support "Save As" for LDAP entries.
     * The LDAP directory is the only storage back-end; there's no meaningful
     * "save to a different location" concept for live directory entries.
     */
    @Override
    public void doSaveAs()
    {
    }


    // ── CAPTAIN CHECKS IF THE LOG HAS UNSAVED CHANGES ────────────────────────
    // Before the ship leaves dock, the captain checks whether any mission notes
    // are still pending — unsaved entries need attention before they're lost.
    // We delegate to the shared working copy to find out if edits are pending.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this editor has unsaved changes.
     * Eclipse uses this to decide whether to show a "save before close?" dialog
     * and to drive the dirty indicator (asterisk) in the editor tab title.
     *
     * @return {@code true} if the working copy has been modified since the last save.
     */
    @Override
    public boolean isDirty()
    {
        return getEntryEditorInput().isSharedWorkingCopyDirty( this );
    }


    // ── NO ALTERNATE ARCHIVE EXISTS ──────────────────────────────────────────
    // Eclipse asks: "Can this log be saved somewhere else?" The captain's
    // answer is always no — there's only one canonical archive in the fleet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — "Save As" is not supported.
     * Eclipse checks this to decide whether to enable the Save As menu item;
     * we always return false because LDAP entries have no file-system target.
     *
     * @return always {@code false}.
     */
    @Override
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    // ── REQUESTING THE WEAPONS OFFICER ───────────────────────────────────────
    // A crew member asks for the weapons officer reference — the captain points
    // them to the right station on the Tantive IV bridge.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the action group that owns the editor's toolbar and context-menu actions.
     * Callers (like the universal listener or subclasses) use this to activate
     * or deactivate global keyboard shortcuts.
     *
     * @return the action group for this editor.
     */
    public EntryEditorActionGroup getActionGroup()
    {
        return actionGroup;
    }


    // ── REQUESTING THE NAVIGATOR'S CONFIGURATION LOG ─────────────────────────
    // The communications officer asks for the navigator's config file
    // to understand how content is presented and sorted on the bridge.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the configuration object that controls how this editor presents data.
     * The configuration bundles the content provider, label provider, sorter,
     * filter, and value editor manager for the attribute-table widget.
     *
     * @return the editor configuration.
     */
    public EntryEditorConfiguration getConfiguration()
    {
        return configuration;
    }


    // ── REQUESTING THE MAIN VIEWPORT ─────────────────────────────────────────
    // A crew member asks for direct access to the bridge's main display screen
    // so they can push new data or read what's currently shown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the main SWT widget that renders the LDAP entry's attributes and values.
     * The widget contains the tree-viewer; callers use this to get the viewer
     * for selection management or to trigger a refresh.
     *
     * @return the entry editor widget.
     */
    public EntryEditorWidget getMainWidget()
    {
        return mainWidget;
    }


    // ── REQUESTING THE BRIEFING ROOM REFERENCE ───────────────────────────────
    // A crew member asks where the briefing room is — the captain hands over
    // the reference without creating a new room; it may not exist yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the outline page if it has been created, or {@code null} if not yet initialized.
     * The outline page shows a tree view of the entry's attributes alongside the editor.
     * Use {@link #getAdapter(Class)} with {@link IContentOutlinePage} to get-or-create it.
     *
     * @return the outline page, or {@code null}.
     */
    public EntryEditorOutlinePage getOutlinePage()
    {
        return outlinePage;
    }


    // ── REQUESTING THE COMMS OFFICER ─────────────────────────────────────────
    // The captain is asked who's monitoring the communication channels on the bridge.
    // The universal listener is that officer — it routes all events to the right station.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the universal listener that routes LDAP model events to this editor's components.
     * The listener watches the LDAP model for changes and triggers refreshes on the widget
     * and outline page when entries are modified externally.
     *
     * @return the universal listener.
     */
    public EntryEditorUniversalListener getUniversalListener()
    {
        return universalListener;
    }


    // ── CAPTAIN DECLINES TO LOG AN EMPTY WAYPOINT ────────────────────────────
    // The navigation computer asks if we want to record a blank position marker —
    // the captain says no; an empty history entry would just clutter the log.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — we don't create placeholder navigation history entries.
     * Eclipse's navigation history calls this for "empty" slots; returning null
     * signals that we don't need one.
     *
     * @return always {@code null}.
     */
    @Override
    public INavigationLocation createEmptyNavigationLocation()
    {
        return null;
    }


    // ── CAPTAIN LOGS CURRENT POSITION IN THE NAV HISTORY ─────────────────────
    // The navigator marks the Tantive IV's current coordinates in the flight log
    // so the crew can return here if they fly off to explore another system.
    // We create a navigation location snapshot of the current entry so the user
    // can hit Back in Eclipse's history and come back to this same entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation history entry for the currently displayed LDAP entry.
     * Eclipse calls this when the user navigates away so they can use the
     * Back/Forward buttons to return to this entry later.
     *
     * @return a new {@link EntryEditorNavigationLocation} capturing the current input.
     */
    @Override
    public INavigationLocation createNavigationLocation()
    {
        return new EntryEditorNavigationLocation( this );
    }


    // ── CAPTAIN UPDATES THE MISSION NAMEPLATE ────────────────────────────────
    // The Tantive IV mission changes — the nameplate above the bridge door is
    // updated so everyone boarding knows which operation this run is for.
    // We update the editor tab title to reflect the new LDAP entry's name.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the editor tab title to match the current entry editor input's display name.
     * Called every time the input changes so the tab reflects what's being shown.
     *
     * @param input  The new editor input whose {@code getName()} becomes the tab label.
     */
    protected void setEditorName( EntryEditorInput input )
    {
        setPartName( input.getName() );
    }


    // ── CAPTAIN ACCEPTS ALL MISSION TYPES ────────────────────────────────────
    // The fleet command asks the Tantive IV: "Can you handle this kind of entry?"
    // The captain says yes — this ship is fully general-purpose.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code true} — this editor can handle any LDAP entry.
     * Subclasses may override to restrict to specific object classes if needed.
     *
     * <p>For example — the Tantive IV captain answers:</p>
     * <pre>
     *   return true; // we handle all entry types
     * </pre>
     *
     * @param entry  The LDAP entry being evaluated; ignored by this base implementation.
     * @return always {@code true}.
     */
    @Override
    public boolean canHandle( IEntry entry )
    {
        return true;
    }


    // ── CAPTAIN RETRIEVES THE OFFICIAL MISSION DOSSIER ───────────────────────
    // The navigation officer asks for the typed mission dossier, not the raw
    // sealed envelope — they need it in a form they can actually read and work with.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current editor input cast to an {@link EntryEditorInput}.
     * Raw Eclipse editor inputs are untyped; this gives us the strongly-typed
     * version that carries the LDAP entry reference.
     *
     * @return the current input as {@link EntryEditorInput}, never {@code null} if the editor is properly initialized.
     */
    @Override
    public EntryEditorInput getEntryEditorInput()
    {
        return EntryEditorUtils.getEntryEditorInput( getEditorInput() );
    }


    // ── CREW MEMBER REPORTS A CHANGE IN THE MISSION LOG ─────────────────────
    // A crew member rushes onto the bridge: "The mission parameters changed!"
    // The captain refreshes the main display and, if not on auto-commit, marks
    // the dirty flag so the navigator knows a save is needed before departure.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the LDAP working copy is modified — refreshes the viewer and marks dirty if needed.
     * The LDAP model calls this whenever an attribute or value changes in the shared
     * working copy so we can update the displayed table and the editor's dirty state.
     *
     * @param source  The object that triggered the modification; used to avoid echo-loops
     *                when this editor itself caused the change.
     */
    @Override
    public void workingCopyModified( Object source )
    {
        if ( mainWidget != null && !mainWidget.getViewer().isCellEditorActive() )
        {
            ISelection selection = mainWidget.getViewer().getSelection();
            mainWidget.getViewer().refresh();
            mainWidget.getViewer().setSelection( selection );
        }

        if ( !isAutoSave() )
        {
            // mark as dirty
            firePropertyChange( PROP_DIRTY );
        }
    }


    // ── NAVIGATOR LOADS NEW COORDINATES INTO THE HELM ────────────────────────
    // The navigator punches new destination coordinates into the helm console,
    // clearing out the old course so the pilot isn't confused by stale data.
    // We push a fresh working copy to the listener and deliberately deselect
    // any previously highlighted rows to avoid stale action states.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Connects the widget to the shared working copy for the given entry editor input.
     * We explicitly clear any existing selection before loading the new entry to prevent
     * actions from staying enabled/disabled based on the old selection state — this matters
     * especially when switching between an ISearchResult and its underlying IEntry.
     *
     * @param eei  The new entry editor input whose working copy we push to the widget.
     */
    private void setEntryEditorWidgetInput( EntryEditorInput eei )
    {
        if ( mainWidget != null )
        {
            universalListener.setInput( eei.getSharedWorkingCopy( this ) );

            /*
             * Explicitly deselect previously selected attributes and values.
             * This avoids disabled actions if the new input is equal but not
             * identical to the previous input. This happens for example if
             * an ISearchResult or IBookmark object is open and afterwards
             * the IEntry object is opened.
             */
            mainWidget.getViewer().setSelection( StructuredSelection.EMPTY );
        }
    }


    // ── FLEET COMMAND REASSIGNS THE SHIP TO A NEW MISSION ────────────────────
    // Fleet command radios in: "Tantive IV, you're being redirected to Alderaan."
    // The captain checks the current mission state — if there are unsaved field
    // notes, they ask the crew to sign off before accepting the new assignment.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Switches this reusable editor to display a different LDAP entry.
     * Called by Eclipse's {@link IReusableEditor} mechanism when another entry
     * should be shown in the same tab (single-tab mode). We short-circuit if
     * the input is identical, prompt for save if dirty, then swap the entry and
     * mark a history location so Back/Forward still works.
     *
     * @param input  The new editor input to display; must be an {@link EntryEditorInput}.
     */
    @Override
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
            if ( isDirty() && !EntryEditorUtils.askSaveSharedWorkingCopyBeforeInputChange( this ) )
            {
                return;
            }

            // now set the real input and mark history location
            setInput( input );
            getSite().getPage().getNavigationHistory().markLocation( this );
            firePropertyChange( BrowserUIConstants.INPUT_CHANGED );
        }
    }
}
