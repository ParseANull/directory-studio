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


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.ValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueModifiedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueMultiModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ValueRenamedEvent;
import org.apache.directory.studio.ldapbrowser.core.jobs.UpdateEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;


// ── CLASS: SearchResultEditor — The Imperial Census Terminal ─────────────────
// The Empire keeps census records for every sector, and a data terminal is the
// front-end: it shows the records on screen (the table), lets operators update
// individual fields (inline cell editing), and when a field changes it immediately
// dispatches a write-back to the central Imperial database (the LDAP server).
// This class is that terminal.  It wires together all the sub-systems:
//   • SearchResultEditorWidget — the SWT table and quick-filter bar
//   • SearchResultEditorConfiguration — lazy factory for all component singletons
//   • SearchResultEditorActionGroup — toolbar / menus / context menu
//   • SearchResultEditorUniversalListener — all event wiring
// The inner entryUpdateListener is the write-back path: when the cell modifier
// commits a change to the in-memory model, we compute an LDIF diff and dispatch
// UpdateEntryRunnable to push it to the directory.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main editor part for the search result editor.
 * Implements {@link IReusableEditor} so Eclipse reuses one tab for all searches
 * (via {@link SingleTabSearchResultEditorMatchingStrategy}),
 * {@link INavigationLocationProvider} so searches appear in Eclipse's back/forward
 * history, and {@link IShowEditorInput} so the tab content can be swapped without
 * reopening.
 * The inner {@link #entryUpdateListener} is the critical write-back path: it fires
 * whenever the in-memory LDAP model changes, computes an LDIF diff between the
 * reference copy and the modified entry, and runs {@link UpdateEntryRunnable} to
 * save the change to the directory.
 * Think of this as the Imperial census terminal: display, inline editing, and
 * immediate write-back to the central database.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditor extends EditorPart implements INavigationLocationProvider, IReusableEditor,
    IShowEditorInput, IPropertyChangeListener
{

    /** The configuration. */
    private SearchResultEditorConfiguration configuration;

    /** The action group. */
    private SearchResultEditorActionGroup actionGroup;

    /** The main widget. */
    private SearchResultEditorWidget mainWidget;

    /** The universal listener. */
    private SearchResultEditorUniversalListener universalListener;

    // ── The Terminal's Write-Back Mechanism ────────────────────────────────────
    // When the user edits a cell and the cell modifier commits to the model,
    // this listener fires.  We check that the event is a real value modification
    // (not a transient empty-value placeholder), verify no empty values remain,
    // compute the LDIF diff between the reference copy and the modified entry,
    // and dispatch UpdateEntryRunnable to push the change to the directory.
    // ─────────────────────────────────────────────────────────────────────────────
    protected EntryUpdateListener entryUpdateListener = new EntryUpdateListener()
    {
        public void entryUpdated( EntryModificationEvent event )
        {
            if ( mainWidget.getViewer() == null || mainWidget.getViewer().getInput() == null )
            {
                return;
            }

            IEntry modifiedEntry = event.getModifiedEntry();
            IEntry originalEntry = modifiedEntry.getBrowserConnection().getEntryFromCache( modifiedEntry.getDn() );
            ISearchResult referenceCopy = configuration.getCursor( mainWidget.getViewer() ).getSelectedReferenceCopy();
            ISearchResult workingCopy = configuration.getCursor( mainWidget.getViewer() ).getSelectedSearchResult();

            // check on object identity, nothing should be done for equal objects from other editors
            if ( workingCopy != null && workingCopy.getEntry() == modifiedEntry )
            {
                // only save if we receive a real value modification event
                if ( !( event instanceof ValueAddedEvent || event instanceof ValueDeletedEvent
                    || event instanceof ValueModifiedEvent || event instanceof ValueRenamedEvent || event instanceof ValueMultiModificationEvent ) )
                {
                    return;
                }
                // consistency check: don't save if there is an empty value, silently return in that case
                for ( IAttribute attribute : modifiedEntry.getAttributes() )
                {
                    for ( IValue value : attribute.getValues() )
                    {
                        if ( value.isEmpty() )
                        {
                            return;
                        }
                    }
                }

                LdifFile diff = Utils.computeDiff( referenceCopy.getEntry(), modifiedEntry );
                if ( diff != null )
                {
                    // save
                    UpdateEntryRunnable runnable = new UpdateEntryRunnable( originalEntry, diff
                        .toFormattedString( LdifFormatParameters.DEFAULT ) );
                    new StudioBrowserJob( runnable ).execute();
                }
                configuration.getCursor( mainWidget.getViewer() ).resetCopies();
            }
        }
    };


    // ── Terminal Returns Its Own ID ───────────────────────────────────────────
    // Eclipse uses the editor ID string to look up the right editor for a given input.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse editor ID for the search result editor.
     * Used by the workbench to route {@link SearchResultEditorInput} to this editor.
     *
     * @return the editor ID constant from {@link BrowserUIConstants#EDITOR_SEARCH_RESULT}
     */
    public static String getId()
    {
        return BrowserUIConstants.EDITOR_SEARCH_RESULT;
    }


    // ── Terminal Loads a New Search ────────────────────────────────────────────
    // Called by the universal listener when the browser view selection changes
    // or the back/forward navigation restores a location.  We update the tab name
    // and ask the universal listener to load the new search.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the editor input and updates the tab label.
     * If the listener is already wired up (i.e. we're fully initialised), we also
     * push the new search through the universal listener.
     *
     * @param input the new editor input; should be a {@link SearchResultEditorInput}
     */
    public void setInput( IEditorInput input )
    {
        super.setInput( input );

        if ( input instanceof SearchResultEditorInput && universalListener != null )
        {
            setPartName( input.getName() );

            SearchResultEditorInput srei = ( SearchResultEditorInput ) input;
            setSearchResultEditorWidgetInput( srei );
        }
    }


    // ── Terminal Swaps Content Without Reopening ───────────────────────────────
    // IShowEditorInput allows the workbench to swap our content when the same
    // editor is reused.  We skip the update if the input is already active
    // (no-op optimisation) and otherwise set the input and mark a navigation point.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the workbench when reusing this editor tab for a different input.
     * Skips the update if the input is already set (optimisation).
     * Otherwise sets the input, marks a navigation history location, and fires
     * an {@code INPUT_CHANGED} property change so interested parties can react.
     *
     * @param input the new input to show; should be a {@link SearchResultEditorInput}
     */
    public void showEditorInput( IEditorInput input )
    {
        if ( input instanceof SearchResultEditorInput )
        {
            /*
             * Optimization: no need to set the input again if the same input is already set
             */
            if ( getEditorInput() != null && getEditorInput().equals( input ) )
            {
                return;
            }

            // now set the real input and mark history location
            setInput( input );
            getSite().getPage().getNavigationHistory().markLocation( this );
            firePropertyChange( BrowserUIConstants.INPUT_CHANGED );
        }
    }


    // ── Terminal Extracts the Search and Pushes It to the Listener ────────────
    // Private helper: unwrap the SearchResultEditorInput and give the ISearch
    // to the universal listener.
    // ─────────────────────────────────────────────────────────────────────────────
    private void setSearchResultEditorWidgetInput( SearchResultEditorInput srei )
    {
        ISearch search = srei.getSearch();
        universalListener.setInput( search );
    }


    // ── Terminal Refreshes Its Display ────────────────────────────────────────
    // Called by the property-change listener when a relevant preference changes.
    // We just ask the universal listener to re-run refreshInput().
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the search result table.
     * Delegates to {@link SearchResultEditorUniversalListener#refreshInput()} if
     * the listener is initialised.  Called e.g. when the "show DN" preference changes.
     */
    public void refresh()
    {
        if ( universalListener != null )
        {
            universalListener.refreshInput();
        }
    }


    // ── Terminal Powers Up ────────────────────────────────────────────────────
    // Eclipse calls init() to set the site and initial input.  We also register
    // the entryUpdateListener here so the write-back path is in place before
    // createPartControl() wires everything else.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the editor with its site and initial input.
     * Also registers the {@link #entryUpdateListener} on the global event bus so
     * the write-back path is active from the moment the editor exists.
     *
     * @param site  the editor site
     * @param input the initial editor input
     * @throws PartInitException if initialisation fails
     */
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        setSite( site );
        setInput( input );

        EventRegistry
            .addEntryUpdateListener( entryUpdateListener, BrowserCommonActivator.getDefault().getEventRunner() );
    }


    // ── Terminal Builds Its UI ────────────────────────────────────────────────
    // Eclipse calls createPartControl() when it's time to build the SWT widgets.
    // We create everything in order: configuration → widget → action group →
    // universal listener → selection provider → push initial input.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT widgets and wires all sub-systems together.
     * Order matters:
     * <ol>
     *   <li>Create {@link SearchResultEditorConfiguration} (lazy factory)</li>
     *   <li>Create {@link SearchResultEditorWidget} (the SWT table)</li>
     *   <li>Create {@link SearchResultEditorActionGroup} (toolbar / menus)</li>
     *   <li>Create {@link SearchResultEditorUniversalListener} (event wiring)</li>
     *   <li>Set the selection provider on the site (cursor broadcasts selections)</li>
     *   <li>Push the initial editor input through setInput()</li>
     * </ol>
     *
     * @param parent the parent composite provided by Eclipse
     */
    public void createPartControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        // layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout( layout );

        PlatformUI.getWorkbench().getHelpSystem().setHelp( composite,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_search_result_editor" ); //$NON-NLS-1$ //$NON-NLS-2$

        // create configuration
        configuration = new SearchResultEditorConfiguration( this );

        // create main widget
        mainWidget = new SearchResultEditorWidget( configuration );
        mainWidget.createWidget( composite );

        // create actions and context menu (and register global actions)
        actionGroup = new SearchResultEditorActionGroup( this );
        actionGroup.fillToolBar( mainWidget.getToolBarManager() );
        actionGroup.fillMenu( mainWidget.getMenuManager() );
        actionGroup.enableGlobalActionHandlers( getEditorSite().getActionBars() );
        actionGroup.fillContextMenu( configuration.getContextMenuManager( mainWidget.getViewer() ) );

        // create the listener
        universalListener = new SearchResultEditorUniversalListener( this );
        getSite().setSelectionProvider( configuration.getCursor( mainWidget.getViewer() ) );
        this.setInput( getEditorInput() );

        BrowserUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
    }


    // ── Terminal Hands Focus to the Table ─────────────────────────────────────
    // Eclipse calls this to move keyboard focus into the editor.  We delegate to
    // the widget which delegates to the cursor.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the search result table.
     * Delegates to {@link SearchResultEditorWidget#setFocus()}.
     */
    public void setFocus()
    {
        mainWidget.setFocus();
    }


    // ── Terminal Shuts Down ────────────────────────────────────────────────────
    // Release everything in the correct order: event listener → action group →
    // universal listener → widget → configuration → selection provider →
    // preference change listener.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all resources held by this editor.
     * Deregisters all listeners and nulls all fields to prevent memory leaks.
     * Called by Eclipse when the editor tab is closed.
     */
    public void dispose()
    {
        if ( configuration != null )
        {
            EventRegistry.removeEntryUpdateListener( entryUpdateListener );
            actionGroup.dispose();
            actionGroup = null;
            universalListener.dispose();
            universalListener = null;
            mainWidget.dispose();
            mainWidget = null;
            configuration.dispose();
            configuration = null;
            getSite().setSelectionProvider( null );
            BrowserUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
        }

        super.dispose();
    }


    // ── Terminal Doesn't Save to Disk ─────────────────────────────────────────
    // Saves go directly to the LDAP directory via UpdateEntryRunnable.
    // Eclipse's "save" concept doesn't apply here.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — changes are saved directly to the LDAP directory, not to a file.
     *
     * @param monitor not used
     */
    public void doSave( IProgressMonitor monitor )
    {
    }


    // ── Terminal Has No "Save As" ─────────────────────────────────────────────
    /**
     * No-op — "Save As" is not applicable to the search result editor.
     */
    public void doSaveAs()
    {
    }


    // ── Terminal Is Never "Dirty" ─────────────────────────────────────────────
    // Changes are committed immediately to the directory; there's no unsaved state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code false} — changes are committed to the directory immediately,
     * so there is no "unsaved" dirty state.
     *
     * @return {@code false}
     */
    public boolean isDirty()
    {
        return false;
    }


    // ── Terminal Can't Save As ────────────────────────────────────────────────
    /**
     * Returns {@code false} — "Save As" is not supported.
     *
     * @return {@code false}
     */
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    // ── Navigation History: Empty Placeholder ─────────────────────────────────
    // Eclipse calls this when creating a placeholder entry in the history.
    // We return null — we never create empty placeholder locations.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — we do not create empty navigation history placeholders.
     *
     * @return {@code null}
     */
    public INavigationLocation createEmptyNavigationLocation()
    {
        return null;
    }


    // ── Navigation History: Real Location ─────────────────────────────────────
    // Eclipse calls this when marking a real navigation point (e.g. after showEditorInput).
    // We wrap the current editor in a SearchResultEditorNavigationLocation.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation history entry for the current search.
     * The returned {@link SearchResultEditorNavigationLocation} records the current
     * search name and connection ID so the user can navigate back to it.
     *
     * @return a new navigation location for the current editor state
     */
    public INavigationLocation createNavigationLocation()
    {
        return new SearchResultEditorNavigationLocation( this );
    }


    // ── Terminal Adapts to IShowIn Protocol ───────────────────────────────────
    // When the user triggers "Show In → Browser", Eclipse asks us for
    // IShowInSource (what to show) and IShowInTargetList (which views to offer).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Adapts to {@link IShowInTargetList} (which views appear in the "Show In" menu)
     * and {@link IShowInSource} (what selection to pass to those views).
     * For all other adapter types we delegate to the superclass.
     *
     * @param required the requested adapter type
     * @return the adapter object, or the superclass result for unrecognised types
     */
    public Object getAdapter( Class required )
    {

        if ( IShowInTargetList.class.equals( required ) )
        {
            return new IShowInTargetList()
            {
                public String[] getShowInTargetIds()
                {
                    return new String[]
                        { BrowserView.getId() };
                }
            };
        }

        if ( IShowInSource.class.equals( required ) )
        {
            return new IShowInSource()
            {
                public ShowInContext getShowInContext()
                {
                    ISelection selection = getConfiguration().getCursor( getMainWidget().getViewer() ).getSelection();
                    return new ShowInContext( getMainWidget().getViewer().getInput(), selection );
                }
            };
        }

        return super.getAdapter( required );
    }


    // ── Terminal Exposes Its Sub-Systems ──────────────────────────────────────
    // Getters for the sub-systems used by the universal listener and action group.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the action group managing toolbar, menus, and context menu actions.
     *
     * @return the action group; never null after {@link #createPartControl}
     */
    public SearchResultEditorActionGroup getActionGroup()
    {
        return actionGroup;
    }


    /**
     * Returns the configuration (lazy factory for all editor sub-systems).
     *
     * @return the configuration; never null after {@link #createPartControl}
     */
    public SearchResultEditorConfiguration getConfiguration()
    {
        return configuration;
    }


    /**
     * Returns the main widget holding the SWT table and quick-filter bar.
     *
     * @return the main widget; never null after {@link #createPartControl}
     */
    public SearchResultEditorWidget getMainWidget()
    {
        return mainWidget;
    }


    /**
     * Returns the universal listener that manages all events for this editor.
     *
     * @return the universal listener; never null after {@link #createPartControl}
     */
    public SearchResultEditorUniversalListener getUniversalListener()
    {
        return universalListener;
    }


    // ── Terminal Responds to Preference Changes ────────────────────────────────
    // When a relevant preference (e.g. show DN, show links) changes, Eclipse
    // fires this callback.  We simply call refresh() to rebuild the column layout.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a preference in the BrowserUIPlugin preference store changes.
     * We call {@link #refresh()} unconditionally — it's cheap to refresh.
     *
     * @param event the property change event; not inspected
     */
    public void propertyChange( PropertyChangeEvent event )
    {
        refresh();
    }

}
