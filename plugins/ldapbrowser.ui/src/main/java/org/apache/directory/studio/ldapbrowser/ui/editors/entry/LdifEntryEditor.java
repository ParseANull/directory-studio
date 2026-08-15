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
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.apache.directory.studio.ldifeditor.editor.LdifOutlinePage;
import org.apache.directory.studio.utils.ActionUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


// ── CLASS: LdifEntryEditor — TANTIVE IV BRIDGE, RAW COORDINATES ON SCREEN ────
// On the Tantive IV bridge, some consoles show processed displays — nice labels,
// icons, pretty tables. But the comms officer's raw telemetry screen just shows
// the actual signal data: formatted characters, protocol frames, the real bits.
// LdifEntryEditor is that raw-data console: it presents the LDAP entry as plain
// LDIF text — the same format the LDAP protocol actually speaks — so power users
// can read and edit the entry at the protocol level.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The base class for LDAP entry editors that display entries in LDIF text format.
 * LDIF (LDAP Data Interchange Format) is the text representation of LDAP entries —
 * it's what you'd see if you exported an entry to a file. This editor extends
 * the LDIF text editor and adds LDAP-specific behavior: loading the entry as LDIF,
 * saving edits back to the server, and a Refresh action that reloads attributes.
 * Think of it as the Tantive IV's raw telemetry feed: no pretty widgets, just the
 * real data in the real format.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class LdifEntryEditor extends LdifEditor implements IEntryEditor, IShowEditorInput
{

    private static final String REFRESH_ACTION = "RefreshAction"; //$NON-NLS-1$

    private static final String FETCH_OPERATIONAL_ATTRIBUTES_ACTION = "FetchOperationalAttributesAction"; //$NON-NLS-1$

    private boolean inShowEditorInput = false;

    private IAction refreshAction = new Action()
    {
        @Override
        public ImageDescriptor getImageDescriptor()
        {
            return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_REFRESH );
        }


        @Override
        public String getText()
        {
            return org.apache.directory.studio.ldapbrowser.common.actions.Messages
                .getString( "RefreshAction.RelaodAttributes" ); //$NON-NLS-1$
        }


        @Override
        public boolean isEnabled()
        {
            return ( getEntryEditorInput().getResolvedEntry() != null );
        }


        @Override
        public String getActionDefinitionId()
        {
            return "org.eclipse.ui.file.refresh"; //$NON-NLS-1$
        }


        @Override
        public void run()
        {
            IEntry entry = getEntryEditorInput().getResolvedEntry();
            new StudioBrowserJob( new InitializeAttributesRunnable( entry ) ).execute();
        }
    };

    private IAction fetchOperationalAttributesAction = new Action()
    {
        @Override
        public int getStyle()
        {
            return Action.AS_CHECK_BOX;
        }


        @Override
        public String getText()
        {
            return org.apache.directory.studio.ldapbrowser.common.actions.Messages
                .getString( "FetchOperationalAttributesAction.FetchOperationalAttributes" ); //$NON-NLS-1$
        }


        @Override
        public boolean isEnabled()
        {
            IEntry entry = getEntryEditorInput().getResolvedEntry();
            if ( entry != null )
            {
                entry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );

                return !entry.getBrowserConnection().isFetchOperationalAttributes();
            }

            return false;
        }


        @Override
        public void run()
        {
            IEntry entry = getEntryEditorInput().getResolvedEntry();
            entry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );

            boolean init = !entry.isInitOperationalAttributes();
            entry.setInitOperationalAttributes( init );
            new StudioBrowserJob( new InitializeAttributesRunnable( entry ) ).execute();
        }
    };


    // ── THE RAW TELEMETRY CONSOLE BOOTS UP ────────────────────────────────────
    // The telemetry console on the Tantive IV bridge starts up — it replaces the
    // default display driver with a custom one that writes directly to the LDAP server.
    // We install our own document provider so that saving the LDIF text triggers
    // a live LDAP modify operation rather than writing to a local file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new LDIF entry editor, installing our custom document provider.
     * The parent {@link LdifEditor} would use a file-based document provider by default;
     * we replace it with {@link LdifEntryEditorDocumentProvider} that talks to the LDAP server.
     */
    public LdifEntryEditor()
    {
        super();

        // use our own document provider that saves changes to the directory
        setDocumentProvider( new LdifEntryEditorDocumentProvider( this ) );
    }


    // ── CONSOLE TAKES THE CONN ON THE BRIDGE ──────────────────────────────────
    // The raw telemetry console is assigned its station on the Tantive IV bridge —
    // it hooks into the ship's site and gets its first data feed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes this editor with its Eclipse site and input.
     * We just delegate to the superclass — the custom document provider handles
     * all the LDAP-specific initialization.
     *
     * @param site   The Eclipse editor site.
     * @param input  The editor input (expected to be an {@link EntryEditorInput}).
     * @throws PartInitException if initialization fails.
     */
    @Override
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        super.init( site, input );
    }


    // ── CONSOLE TUNES TO THE NEW SIGNAL FREQUENCY ────────────────────────────
    // The telemetry console switches from a stale data feed to a live one —
    // it reconfigures its receiver for the new entry's connection parameters.
    // We extract the resolved entry and set the browser connection so the LDIF
    // editor's syntax highlighter and schema-aware features work for this server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads a new LDAP entry into the LDIF text editor.
     * Called whenever the editor input changes — we resolve the entry and configure
     * the underlying LDIF editor's connection so schema-aware features work correctly.
     *
     * @param input  The new editor input; expected to be an {@link EntryEditorInput}.
     * @throws CoreException if the input cannot be loaded (e.g., invalid format).
     */
    @Override
    protected void doSetInput( IEditorInput input ) throws CoreException
    {
        super.doSetInput( input );

        IEntry entry = getEntryEditorInput().getResolvedEntry();
        if ( entry != null )
        {
            setConnection( entry.getBrowserConnection() );
        }
    }


    // ── CONSOLE HIDES ITS OWN TOOLBAR TO SAVE SPACE ON THE BRIDGE ────────────
    // The raw telemetry console doesn't need its own toolbar cluttering the bridge —
    // the ship's main controls handle everything. We suppress the default toolbar
    // so the LDIF editor looks clean inside the Eclipse editor frame.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the LDIF editor's UI but suppresses the default toolbar.
     * The entry editor frame already provides a toolbar via {@link EntryEditorActionGroup};
     * showing the LDIF editor's built-in toolbar too would be redundant and confusing.
     *
     * @param parent  The SWT composite that Eclipse provides as our container.
     */
    @Override
    public void createPartControl( Composite parent )
    {
        // don't show the tool bar
        showToolBar = false;

        super.createPartControl( parent );
    }


    // ── CONSOLE REFUSES TO WRITE TO A DIFFERENT ARCHIVE ──────────────────────
    // The raw telemetry feed only records to the official ship's log — there's no
    // mechanism to redirect the output to an alternate storage location.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — "Save As" is not supported.
     * LDAP entries live in the directory; there's no local file path to save to.
     *
     * @return always {@code false}.
     */
    @Override
    public boolean isSaveAsAllowed()
    {
        // Allowing "Save As..." requires an IPathEditorInput.
        // Would makes things much more complex, maybe we could add this later.
        return false;
    }


    // ── CONSOLE POWERS DOWN ───────────────────────────────────────────────────
    // The telemetry console shuts off at the end of the mission — superclass
    // handles all the cleanup since we don't hold extra resources.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Releases resources held by this editor.
     * We delegate entirely to the superclass since the LDIF editor manages all SWT resources.
     */
    @Override
    public void dispose()
    {
        super.dispose();
    }


    // ── CONSOLE REDIRECTS SPECIAL REQUESTS TO THE RIGHT STATION ──────────────
    // A crew member asks the telemetry console for the briefing room (outline page)
    // — it redirects them to the LDIF outline instead of the default entry outline.
    // We override the LDIF editor's adapter to supply an LDIF-aware outline page.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns adapters for requested interfaces, overriding the outline page.
     * We supply a {@link LdifOutlinePage} (which shows LDIF record structure)
     * instead of the default entry outline, and fall back to the superclass for everything else.
     *
     * @param required  The interface class being requested.
     * @return the requested adapter, or whatever the superclass provides.
     */
    public Object getAdapter( Class required )
    {
        // Overwriting the outline page
        if ( IContentOutlinePage.class.equals( required ) )
        {
            if ( outlinePage == null || outlinePage.getControl() == null || outlinePage.getControl().isDisposed() )
            {
                outlinePage = new LdifOutlinePage( this, true );
            }
            return outlinePage;
        }

        // In all other cases, refering to the super class
        return super.getAdapter( required );
    }


    // ── CONSOLE REGISTERS ITS CUSTOM COMMANDS WITH THE BRIDGE ────────────────
    // The telemetry console plugs its two custom buttons — Refresh and Fetch Op Attrs —
    // into the ship's command routing system so crew members can trigger them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers the Refresh and Fetch Operational Attributes actions with the editor.
     * We call super to get all the standard LDIF editor actions, then add our two
     * LDAP-specific actions under their own action IDs for menu and keybinding wiring.
     */
    @Override
    protected void createActions()
    {
        super.createActions();

        setAction( REFRESH_ACTION, refreshAction );
        setAction( FETCH_OPERATIONAL_ATTRIBUTES_ACTION, fetchOperationalAttributesAction );
    }


    // ── CONSOLE UPDATES THE CONTEXT MENU BEFORE THE CREW READS IT ────────────
    // Right before a crew member reads the context menu on the telemetry console,
    // we update the checkbox states and add our LDAP-specific items to the list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds LDAP-specific items to the LDIF editor's context menu.
     * We let the superclass populate standard LDIF items, then append a Refresh action
     * and a Fetch Operational Attributes toggle (with its current checked state).
     *
     * @param menu  The context menu manager to populate.
     */
    @Override
    protected void editorContextMenuAboutToShow( IMenuManager menu )
    {
        super.editorContextMenuAboutToShow( menu );

        IEntry entry = getEntryEditorInput().getResolvedEntry();
        fetchOperationalAttributesAction.setChecked( ( entry != null ) ? entry.isInitOperationalAttributes() : false );

        addAction( menu, ITextEditorActionConstants.GROUP_REST, REFRESH_ACTION );
        addAction( menu, ITextEditorActionConstants.GROUP_REST, FETCH_OPERATIONAL_ATTRIBUTES_ACTION );
    }


    // ── CONSOLE ARMS GLOBAL KEYBINDINGS WHEN FOCUSED ─────────────────────────
    // When the Tantive IV crew focuses on the telemetry console, the console arms
    // the global F5 key so it triggers a refresh from anywhere on the bridge.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Wires the Refresh action into Eclipse's global keybinding system.
     * Called when the editor gains focus — the F5 key (standard Eclipse Refresh binding)
     * will now trigger a full attribute reload from the LDAP server.
     */
    @Override
    public void activateGlobalActionHandlers()
    {
        ActionUtils.activateActionHandler( refreshAction );
    }


    // ── CONSOLE DISARMS GLOBAL KEYBINDINGS WHEN UNFOCUSED ────────────────────
    // When another console takes focus, the telemetry console disarms its global
    // key bindings so F5 doesn't accidentally trigger a reload from the wrong context.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the Refresh action from Eclipse's global keybinding system.
     * Called when the editor loses focus — we unregister the handler we registered
     * in {@link #activateGlobalActionHandlers()}.
     */
    @Override
    public void deactivateGlobalActionHandlers()
    {
        ActionUtils.deactivateActionHandler( refreshAction );
    }


    // ── CONSOLE LOGS THE CURRENT POSITION IN THE NAV HISTORY ─────────────────
    // The telemetry console records the current LDIF cursor position in the ship's
    // flight log so the crew can navigate back here with the Back button.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation history location capturing the current entry and text selection.
     * Eclipse calls this when the user navigates away so Back/Forward work correctly.
     *
     * @return a new {@link LdifEntryEditorNavigationLocation} for the current state.
     */
    @Override
    public INavigationLocation createNavigationLocation()
    {
        return new LdifEntryEditorNavigationLocation( this, true );
    }


    // ── CONSOLE CREATES AN EMPTY POSITION MARKER ──────────────────────────────
    // The navigator creates a blank position marker for the history system to use
    // as a placeholder before the real coordinates are known.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty navigation history location for use as a placeholder.
     * Eclipse uses this for "empty" navigation history slots before they're populated.
     *
     * @return a new uninitialized {@link LdifEntryEditorNavigationLocation}.
     */
    @Override
    public INavigationLocation createEmptyNavigationLocation()
    {
        return new LdifEntryEditorNavigationLocation( this, false );
    }


    // ── CONSOLE NEVER AUTO-COMMITS CHANGES ───────────────────────────────────
    // The raw telemetry feed doesn't auto-broadcast — changes are buffered and only
    // sent when the operator explicitly hits "transmit." Always returns false.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — the LDIF entry editor never auto-saves.
     * The LDIF text editor follows the standard open-edit-save-close lifecycle;
     * auto-save would make the text editable but commit on every keypress, which
     * is dangerous for LDAP entries.
     *
     * @return always {@code false}.
     */
    public boolean isAutoSave()
    {
        return false;
    }


    // ── CONSOLE ACCEPTS ANY ENTRY TYPE FOR DISPLAY ───────────────────────────
    // The raw telemetry feed can display any signal — it doesn't care what object
    // class the entry has; LDIF is universal. Always returns true.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code true} — this editor can handle any LDAP entry.
     * LDIF format is generic enough to represent any entry regardless of its schema.
     *
     * @param entry  The entry being evaluated; not used by this implementation.
     * @return always {@code true}.
     */
    public boolean canHandle( IEntry entry )
    {
        return true;
    }


    // ── CONSOLE RETRIEVES THE TYPED MISSION DOSSIER ───────────────────────────
    // The telemetry officer asks for the typed mission parameters — not the raw
    // sealed envelope, but the structured dossier they can actually work with.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current editor input as a typed {@link EntryEditorInput}.
     * The raw Eclipse editor input is untyped; this gives us the strongly-typed
     * version that carries the LDAP entry and extension references.
     *
     * @return the current input as {@link EntryEditorInput}.
     */
    public EntryEditorInput getEntryEditorInput()
    {
        return EntryEditorUtils.getEntryEditorInput( getEditorInput() );
    }


    // ── CONSOLE RELAYS A MODEL CHANGE TO THE DOCUMENT ────────────────────────
    // When the LDAP model changes beneath the LDIF text (e.g., an attribute was
    // added via the attribute table editor), the telemetry console updates its
    // raw text display to reflect the new state — keeping text and model in sync.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the shared LDAP working copy is modified externally.
     * We delegate to the document provider to update the LDIF text so it stays
     * in sync with any model changes made outside this editor (e.g., from the
     * attribute table editor or a programmatic batch operation).
     *
     * @param source  The object that triggered the modification; passed through to
     *                the document provider to break echo loops.
     */
    public void workingCopyModified( Object source )
    {
        ( ( LdifEntryEditorDocumentProvider ) getDocumentProvider() ).workingCopyModified( getEntryEditorInput(),
            source );
    }


    // ── CONSOLE SWITCHES TO A NEW SIGNAL SOURCE, AVOIDING RECURSION ──────────
    // The telemetry console is told to retune to a different entry — but it checks
    // first: "are we already switching?" to avoid a chain of recursive retuning
    // that would make the console loop forever and lock up the bridge.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Switches this reusable LDIF editor to display a different LDAP entry.
     * We guard against re-entrance (the flag {@code inShowEditorInput}) because the
     * navigation history selection can cascade: entry selected → input changed event →
     * LinkWithEditor selects entry in tree → tree selection fires → openEditor called again.
     * Without the guard, this method would recurse indefinitely.
     *
     * @param input  The new editor input; must be an {@link EntryEditorInput}.
     */
    public void showEditorInput( IEditorInput input )
    {
        if ( inShowEditorInput )
        {
            /*
             *  This is to avoid recursion when using history navigation:
             *  - when selecting an entry this method is called
             *  - this method fires an input changed event
             *  - the LinkWithEditorAction gets the event and selects the entry in the browser tree
             *  - this selection fires an selection event
             *  - the selection event causes an openEditor() that calls this method again
             */
            return;
        }

        try
        {
            inShowEditorInput = true;

            if ( input instanceof EntryEditorInput )
            {
                EntryEditorInput eei = ( EntryEditorInput ) input;

                /*
                 * Optimization: no need to set the input again if the same input is already set
                 */
                if ( getEntryEditorInput() != null
                    && getEntryEditorInput().getResolvedEntry() == eei.getResolvedEntry() )
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
                doSetInput( input );
                getSite().getPage().getNavigationHistory().markLocation( this );
                firePropertyChange( BrowserUIConstants.INPUT_CHANGED );
            }
        }
        catch ( CoreException e )
        {
            throw new RuntimeException( e );
        }
        finally
        {
            inShowEditorInput = false;
        }
    }
}
