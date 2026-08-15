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
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.core.events.EmptyValueAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.actions.OpenSearchResultAction;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;


// ── CLASS: SearchResultEditorUniversalListener — Obi-Wan Sensing a Disturbance ──
// "I felt a great disturbance in the Force, as if millions of voices suddenly
// cried out in terror."  Obi-Wan is connected to everything — he senses shifts
// in the galaxy, notices when Alderaan vanishes, when Luke is in danger, when
// Han needs backup.  He doesn't act impulsively; he routes each sensation to the
// right response.
// This listener is Obi-Wan: it wires up every event source the editor cares about
// (browser selection changes, part activate/deactivate, search updates, entry updates,
// mouse moves for DN hyperlinks, keyboard for inline editing) and routes each to
// the correct handler.  It's the editor's central nervous system.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The central event hub for the search result editor.
 * We register with six different event sources and route their events to the
 * appropriate editor sub-systems:
 * <ul>
 *   <li>{@link INullSelectionListener} — when the browser view selects a search,
 *       we load it into the editor</li>
 *   <li>{@link IPartListener2} — activate/deactivate keyboard shortcuts when the
 *       editor gains/loses focus</li>
 *   <li>DN hyperlink listeners — render a clickable Hyperlink widget over the DN
 *       cell when the mouse moves into it</li>
 *   <li>{@link SelectionListener} + {@link MouseListener} + {@link KeyListener} on
 *       the cursor — start the value editor on double-click, enter, or printable key</li>
 *   <li>{@link SearchUpdateListener} — refresh the table when the search model changes</li>
 *   <li>{@link EntryUpdateListener} — refresh the table when an entry model changes;
 *       auto-start inline editing when an empty value is added</li>
 * </ul>
 * Think of Obi-Wan: connected to everything, routes every disturbance to the right response.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorUniversalListener implements SearchUpdateListener, EntryUpdateListener
{

    /** The search result editor */
    private SearchResultEditor editor;

    /** The table viewer */
    private TableViewer viewer;

    /** The cursor */
    private SearchResultEditorCursor cursor;

    /** The action used to start the default value editor */
    private OpenBestEditorAction startEditAction;

    /** The selected search that is displayed in the search result editor */
    private ISearch selectedSearch;

    /** The hyperlink used for DNs */
    private Hyperlink dnLink;

    /** The table editor, used to display the hyperlink */
    private TableEditor tableEditor;

    /** Token used to activate and deactivate shortcuts in the editor */
    private IContextActivation contextActivation;

    // ── Obi-Wan Senses a New Search Selection ─────────────────────────────────
    // The browser view's selection changed — Obi-Wan checks whether a single ISearch
    // was selected and tells the editor to load it.  Any other selection type clears
    // the editor to "no search".
    // ─────────────────────────────────────────────────────────────────────────────
    /** Listener that listens for selections of ISearch objects. */
    private INullSelectionListener searchSelectionListener = new INullSelectionListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation sets the editor's input when a search is selected.
         */
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            if ( editor != null && part != null )
            {
                if ( editor.getSite().getWorkbenchWindow() == part.getSite().getWorkbenchWindow() )
                {
                    ISearch[] searches = BrowserSelectionUtils.getSearches( selection );
                    Object[] objects = BrowserSelectionUtils.getObjects( selection );
                    if ( searches.length == 1 && objects.length == 1 )
                    {
                        editor.setInput( new SearchResultEditorInput( searches[0] ) );
                    }
                    else
                    {
                        editor.setInput( new SearchResultEditorInput( null ) );
                    }
                }
            }
        }
    };

    // ── Obi-Wan Toggles His Focus ─────────────────────────────────────────────
    // When the editor part is activated, Obi-Wan activates the keyboard shortcut
    // context and registers global action handlers.  When deactivated, he releases
    // both.  This is how Eclipse knows which keybindings to apply to which editor.
    // ─────────────────────────────────────────────────────────────────────────────
    /** The part listener used to activate and deactivate the shortcuts */
    private IPartListener2 partListener = new IPartListener2()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation deactivates the shortcuts when the part is deactivated.
         */
        public void partDeactivated( IWorkbenchPartReference partRef )
        {
            if ( partRef.getPart( false ) == editor && contextActivation != null )
            {
                editor.getActionGroup().deactivateGlobalActionHandlers();

                IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                    IContextService.class );
                contextService.deactivateContext( contextActivation );
                contextActivation = null;
            }
        }


        /**
         * {@inheritDoc}
         *
         * This implementation activates the shortcuts when the part is activated.
         */
        public void partActivated( IWorkbenchPartReference partRef )
        {
            if ( partRef.getPart( false ) == editor )
            {
                IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                    IContextService.class );
                contextActivation = contextService.activateContext( BrowserCommonConstants.CONTEXT_WINDOWS );

                editor.getActionGroup().activateGlobalActionHandlers();
            }
        }


        /**
         * {@inheritDoc}
         */
        public void partBroughtToTop( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partClosed( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partOpened( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partHidden( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partVisible( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partInputChanged( IWorkbenchPartReference partRef )
        {
        }
    };

    // ── Obi-Wan Opens the Hyperlink Door ──────────────────────────────────────
    // When the user clicks a DN hyperlink, Obi-Wan runs OpenSearchResultAction —
    // opening the full entry editor for that search result.
    // ─────────────────────────────────────────────────────────────────────────────
    /** The listener used to handle clicks to the Dn hyper link */
    private IHyperlinkListener dnHyperlinkListener = new IHyperlinkListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation opens the search result when clicking thd Dn link.
         */
        public void linkActivated( HyperlinkEvent e )
        {
            ISearchResult sr = ( ISearchResult ) e.widget.getData();
            OpenSearchResultAction action = new OpenSearchResultAction();
            action.setSelectedSearchResults( new ISearchResult[]
                { sr } );
            action.run();
        }


        /**
         * {@inheritDoc}
         */
        public void linkEntered( HyperlinkEvent e )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void linkExited( HyperlinkEvent e )
        {
        }
    };

    // ── Obi-Wan Hides the Door When You Step Away ─────────────────────────────
    // When the mouse exits the hyperlink widget, Obi-Wan hides it and removes
    // it from the table editor — no stray overlapping widgets.
    // ─────────────────────────────────────────────────────────────────────────────
    /** This listener removes the Dn link when then mouse exits the hyperlink control */
    private MouseTrackListener dnMouseTrackListener = new MouseTrackListener()
    {
        /**
         * {@inheritDoc}
         */
        public void mouseEnter( MouseEvent e )
        {
        }


        /**
         * {@inheritDoc}
         *
         * This implementation removed the Dn link.
         */
        public void mouseExit( MouseEvent e )
        {
            if ( !dnLink.isDisposed() )
            {
                dnLink.setVisible( false );
                tableEditor.setEditor( null );
            }
        }


        public void mouseHover( MouseEvent e )
        {
        }
    };

    // ── Obi-Wan Renders the DN Door From the Cursor ───────────────────────────
    // When the cursor (TableCursor widget) is over column 0 and that column is "Dn",
    // Obi-Wan asks checkDnLink() to overlay the hyperlink widget on the cell.
    // ─────────────────────────────────────────────────────────────────────────────
    /** This listener renders the Dn hyperlink when the mouse cursor moves over the Dn */
    private MouseMoveListener cursorMouseMoveListener = new MouseMoveListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation renders the Dn link.
         */
        public void mouseMove( MouseEvent e )
        {
            if ( !cursor.isDisposed() )
            {
                TableItem item = cursor.getRow();
                if ( cursor.getColumn() == 0
                    && "Dn".equalsIgnoreCase( cursor.getRow().getParent().getColumns()[0].getText() ) ) //$NON-NLS-1$
                {
                    checkDnLink( item );
                }
            }
        }
    };

    // ── Obi-Wan Renders the DN Door From the Table ────────────────────────────
    // When the mouse moves over the viewer table itself (not the cursor widget),
    // Obi-Wan checks if it's hovering over the DN column and shows the hyperlink.
    // ─────────────────────────────────────────────────────────────────────────────
    /** This listener renders the Dn link when the mouse cursor moves over the Dn */
    private MouseMoveListener viewerMouseMoveListener = new MouseMoveListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation renders the Dn link.
         */
        public void mouseMove( MouseEvent e )
        {
            if ( !viewer.getTable().isDisposed() )
            {
                TableItem item = viewer.getTable().getItem( new Point( e.x, e.y ) );
                viewer.getTable().getColumns()[0].getWidth();
                if ( e.x > 0 && e.x < viewer.getTable().getColumns()[0].getWidth()
                    && "Dn".equalsIgnoreCase( viewer.getTable().getColumns()[0].getText() ) ) //$NON-NLS-1$
                {
                    checkDnLink( item );
                }
            }
        }
    };

    // ── Obi-Wan Highlights an Editable Cell ───────────────────────────────────
    // When the cursor moves to a new cell (widgetSelected), Obi-Wan colors the
    // cursor background: blue if the cell can be edited, grey if read-only.
    // On widgetDefaultSelected (Enter), he starts the value editor.
    // ─────────────────────────────────────────────────────────────────────────────
    /** This listener starts the value editor and toggles the cursor's background color */
    private SelectionListener cursorSelectionListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation sets the cursor's background color.
         */
        public void widgetSelected( SelectionEvent e )
        {
            // viewer.setSelection(new StructuredSelection(getRow()), true);
            // viewer.getTable().setSelection(new TableItem[]{getRow()});
            viewer.setSelection( null, true );
            viewer.getTable().setSelection( new TableItem[0] );

            ISearchResult result = cursor.getSelectedSearchResult();
            String property = cursor.getSelectedProperty();
            if ( property != null && result != null && viewer.getCellModifier().canModify( result, property ) )
            {
                cursor.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_LIST_SELECTION ) );
            }
            else
            {
                cursor.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_TITLE_INACTIVE_FOREGROUND ) );
            }

            // cursor.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
        }


        /**
         * {@inheritDoc}
         *
         * This implementation starts the value editor when pressing enter.
         */
        public void widgetDefaultSelected( SelectionEvent e )
        {
            viewer.setSelection( null, true );
            viewer.getTable().setSelection( new TableItem[0] );
            if ( startEditAction.isEnabled() )
                startEditAction.run();
        }
    };

    // ── Obi-Wan Opens the Editor on Double-Click ──────────────────────────────
    // A double-click on a cell is unambiguous intent to edit — Obi-Wan runs
    // the startEditAction immediately.
    // ─────────────────────────────────────────────────────────────────────────────
    /** This listener starts the value editor when double-clicking a cell */
    private MouseListener cursorMouseListener = new MouseAdapter()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation starts the value editor when double-clicking a cell.
         */
        public void mouseDoubleClick( MouseEvent e )
        {
            viewer.setSelection( null, true );
            viewer.getTable().setSelection( new TableItem[0] );
            if ( startEditAction.isEnabled() )
                startEditAction.run();
        }


        /**
         * {@inheritDoc}
         */
        public void mouseDown( MouseEvent e )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void mouseUp( MouseEvent e )
        {
        }
    };

    // ── Obi-Wan Starts Inline Editing on Keypress ─────────────────────────────
    // If the user presses a printable character (not control keys, not modifier-only)
    // and the best value editor is a TextCellEditor, Obi-Wan opens it and seeds it
    // with the typed character so the user can continue typing naturally.
    // ─────────────────────────────────────────────────────────────────────────────
    /** This listener starts the value editor when typing */
    private KeyListener cursorKeyListener = new KeyListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation starts the value editor when a non-control key is pressed.
         */
        public void keyPressed( KeyEvent e )
        {
            if ( e.character != '\0' && e.character != SWT.CR && e.character != SWT.LF && e.character != SWT.BS
                && e.character != SWT.DEL && e.character != SWT.TAB && e.character != SWT.ESC
                && ( e.stateMask == 0 || e.stateMask == SWT.SHIFT ) )
            {

                if ( startEditAction.isEnabled()
                    && startEditAction.getBestValueEditor().getCellEditor() instanceof TextCellEditor )
                {
                    startEditAction.run();
                    CellEditor editor = viewer.getCellEditors()[cursor.getColumn()];
                    if ( editor instanceof TextCellEditor )
                    {
                        editor.setValue( String.valueOf( e.character ) );
                        ( ( Text ) editor.getControl() ).setSelection( 1 );
                    }
                }

            }
        }


        /**
         * {@inheritDoc}
         */
        public void keyReleased( KeyEvent e )
        {
        }
    };


    // ── Obi-Wan Opens All His Channels ────────────────────────────────────────
    // The constructor wires up every listener: DN hyperlink, cursor events,
    // viewer mouse moves, part lifecycle, browser view selection, and the global
    // event registry for search and entry updates.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the universal listener and wires up all event sources.
     * Creates the DN hyperlink overlay widget, registers all inner listeners on the
     * cursor and viewer, subscribes to part lifecycle events, browser view selection
     * changes, and the global search/entry update event bus.
     *
     * @param editor the search result editor whose events we manage
     */
    public SearchResultEditorUniversalListener( SearchResultEditor editor )
    {
        this.editor = editor;

        startEditAction = editor.getActionGroup().getOpenBestEditorAction();
        viewer = editor.getMainWidget().getViewer();
        cursor = editor.getConfiguration().getCursor( viewer );

        // create dn link control
        dnLink = new Hyperlink( viewer.getTable(), SWT.NONE );
        dnLink.setLayoutData( new GridData( SWT.BOTTOM, SWT.LEFT, true, true ) );
        dnLink.setText( "" ); //$NON-NLS-1$
        dnLink.setMenu( viewer.getTable().getMenu() );
        tableEditor = new TableEditor( viewer.getTable() );
        tableEditor.horizontalAlignment = SWT.LEFT;
        tableEditor.verticalAlignment = SWT.BOTTOM;
        tableEditor.grabHorizontal = true;
        tableEditor.grabVertical = true;

        // init listeners
        dnLink.addHyperlinkListener( dnHyperlinkListener );
        dnLink.addMouseTrackListener( dnMouseTrackListener );

        cursor.addMouseMoveListener( cursorMouseMoveListener );
        cursor.addSelectionListener( cursorSelectionListener );
        cursor.addMouseListener( cursorMouseListener );
        cursor.addKeyListener( cursorKeyListener );

        viewer.getTable().addMouseMoveListener( viewerMouseMoveListener );

        editor.getSite().getPage().addPartListener( partListener );
        editor.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener( BrowserView.getId(),
            searchSelectionListener );

        EventRegistry.addSearchUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
    }


    // ── Obi-Wan Goes Into the Force ────────────────────────────────────────────
    // The editor is closing — Obi-Wan severs all connections, deregisters from
    // every event source, and nulls his references.  "If you strike me down, I
    // shall become more powerful than you can possibly imagine" — but for cleanup
    // purposes, he just becomes null.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters all event listeners and releases all references.
     * Safe to call multiple times.
     */
    public void dispose()
    {
        if ( editor != null )
        {
            editor.getSite().getPage().removePartListener( partListener );
            editor.getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(
                BrowserView.getId(), searchSelectionListener );

            EventRegistry.removeSearchUpdateListener( this );
            EventRegistry.removeEntryUpdateListener( this );

            selectedSearch = null;
            startEditAction = null;
            cursor = null;
            viewer = null;
            editor = null;
        }
    }


    // ── Obi-Wan Senses a Search Update ────────────────────────────────────────
    // The search model changed (results refreshed, search renamed, etc.) — if the
    // updated search is the one we're currently displaying, refresh the input.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * This implementation refreshes the search result editor when the currently
     * displayed search is the one that was updated.
     */
    public void searchUpdated( SearchUpdateEvent searchUpdateEvent )
    {
        if ( selectedSearch == searchUpdateEvent.getSearch() )
        {
            refreshInput();
        }
    }


    // ── Obi-Wan Senses an Entry Update ────────────────────────────────────────
    // An entry was modified.  If it's an EmptyValueAddedEvent (the user created
    // a new empty value), and the cursor is on that attribute, we start the value
    // editor immediately.  Otherwise we just refresh the table display.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * This implementation refreshes the search result editor or starts the value
     * editor if an empty value was added to the currently selected attribute.
     */
    public void entryUpdated( EntryModificationEvent event )
    {
        if ( event instanceof EmptyValueAddedEvent && !editor.getActionGroup().isEditorActive() )
        {
            EmptyValueAddedEvent evae = ( EmptyValueAddedEvent ) event;
            IAttribute att = evae.getAddedValue().getAttribute();
            AttributeHierarchy ah = cursor.getSelectedAttributeHierarchy();
            if ( ah != null && ah.contains( att ) )
            {
                viewer.setSelection( null, true );
                viewer.getTable().setSelection( new TableItem[0] );
                if ( startEditAction.isEnabled() )
                {
                    startEditAction.run();
                }
            }
        }
        else
        {
            viewer.refresh( true );
            cursor.notifyListeners( SWT.Selection, new Event() );
        }
    }


    // ── Obi-Wan Tunes In a New Search ─────────────────────────────────────────
    // The editor's input changed — store the new search, refresh the table, and
    // tell the action group so it can update its enabled states.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the currently displayed search and triggers a full input refresh.
     * Also notifies the action group of the new search so action enabled states
     * can be recalculated.
     *
     * @param search the new search to display, or {@code null} to clear the editor
     */
    void setInput( ISearch search )
    {
        selectedSearch = search;
        refreshInput();
        editor.getActionGroup().setInput( search );
    }


    // ── Obi-Wan Reconfigures the Table Layout ────────────────────────────────
    // The search may have different returning attributes than the previous one.
    // We rebuild the column headers, set the viewer input, update cell editors,
    // and hide any extra columns.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the viewer to match the current search's returning attributes.
     * Creates new table columns if needed, sets column header text, updates the
     * label provider and cell editors, and hides unused columns.
     * Called both when the search changes and when the search model fires an update.
     */
    void refreshInput()
    {
        // create at least on column
        ensureColumnCount( 1 );

        // get all columns
        TableColumn[] columns = viewer.getTable().getColumns();

        // number of used columns
        int usedColumns;

        if ( selectedSearch != null )
        {

            // get displayed attributes
            boolean showDn = BrowserUIPlugin.getDefault().getPreferenceStore().getBoolean(
                BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_DN )
                || selectedSearch.getReturningAttributes().length == 0;
            String[] attributes;
            if ( showDn )
            {
                attributes = new String[selectedSearch.getReturningAttributes().length + 1];
                attributes[0] = "Dn"; //$NON-NLS-1$
                System.arraycopy( selectedSearch.getReturningAttributes(), 0, attributes, 1, attributes.length - 1 );
            }
            else
            {
                attributes = selectedSearch.getReturningAttributes();
            }

            // create missing columns
            if ( attributes.length > columns.length )
            {
                ensureColumnCount( attributes.length );
                columns = viewer.getTable().getColumns();
            }

            // set column headers
            for ( int i = 0; i < attributes.length; i++ )
            {
                columns[i].setText( attributes[i] );
            }
            viewer.setColumnProperties( attributes );

            // set input
            ( ( SearchResultEditorLabelProvider ) viewer.getLabelProvider() ).inputChanged( selectedSearch, showDn );

            viewer.setInput( selectedSearch );
            // this.viewer.refresh();

            // update cell editors
            CellEditor[] editors = new CellEditor[attributes.length];
            viewer.setCellEditors( editors );

            if ( attributes.length > 0 )
            {
                int width = viewer.getTable().getClientArea().width / attributes.length;
                for ( int i = 0; i < attributes.length; i++ )
                {
                    columns[i].setWidth( width );
                }
            }

            // layout columns
            // for(int i=0; i<attributes.length; i++) {
            // columns[i].pack();
            // }
            usedColumns = attributes.length;
        }
        else
        {
            viewer.setInput( null );
            columns[0].setText( "Dn" ); //$NON-NLS-1$
            columns[0].pack();
            usedColumns = 1;
        }

        // make unused columns invisible
        for ( int i = usedColumns; i < columns.length; i++ )
        {
            columns[i].setWidth( 0 );
            columns[i].setText( " " ); //$NON-NLS-1$
        }

        // refresh content provider (sorter and filter)
        editor.getConfiguration().getContentProvider( editor.getMainWidget() ).refresh();

        // this.cursor.setFocus();
    }


    // ── Obi-Wan Makes Sure There Are Enough Columns ───────────────────────────
    // If the table has fewer columns than needed, we create the missing ones.
    // New columns start invisible (width 0) so they don't clutter the display
    // until the column header text is set in refreshInput().
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Ensures the table has at least {@code count} columns.
     * New columns are created with zero width and empty text; they're populated
     * and resized by {@link #refreshInput()}.
     *
     * @param count the minimum number of table columns required
     */
    private void ensureColumnCount( int count )
    {
        TableColumn[] columns = viewer.getTable().getColumns();
        if ( columns.length < count )
        {
            for ( int i = columns.length; i < count; i++ )
            {
                TableColumn column = new TableColumn( viewer.getTable(), SWT.LEFT );
                column.setText( "" ); //$NON-NLS-1$
                column.setWidth( 0 );
                column.setResizable( true );
                column.setMoveable( true );
            }
        }
    }


    // ── Obi-Wan Renders the DN Hyperlink Overlay ─────────────────────────────
    // If the "show links" preference is on and the mouse is over a real search
    // result row's DN cell, we overlay a styled Hyperlink widget on top of the
    // cell.  Clicking it opens the entry editor for that result.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Renders the DN hyperlink overlay on the given table item if appropriate.
     * The overlay is only shown if the "show links" preference is enabled and the
     * item holds an {@link ISearchResult}.  Otherwise the overlay is hidden.
     *
     * @param item the table item the mouse is hovering over; may be {@code null}
     */
    private void checkDnLink( TableItem item )
    {
        if ( dnLink == null || dnLink.isDisposed() || tableEditor == null || viewer.getTable().isDisposed()
            || cursor.isDisposed() )
        {
            return;
        }

        boolean showLinks = BrowserUIPlugin.getDefault().getPreferenceStore().getBoolean(
            BrowserUIConstants.PREFERENCE_SEARCHRESULTEDITOR_SHOW_LINKS );
        if ( showLinks )
        {

            boolean linkVisible = false;

            if ( item != null )
            {
                Object data = item.getData();

                if ( data instanceof ISearchResult )
                {
                    ISearchResult sr = ( ISearchResult ) data;

                    item.getFont();
                    viewer.getTable().getColumn( 0 ).getWidth();
                    viewer.getTable().getItemHeight();

                    // dnLink.setText("<a>"+sr.getDn().toString()+"</a>");
                    dnLink.setData( sr );
                    dnLink.setText( sr.getDn().getName() );
                    dnLink.setUnderlined( true );
                    dnLink.setFont( item.getFont() );
                    dnLink.setForeground( item.getForeground() );
                    dnLink.setBackground( item.getBackground() );
                    dnLink.setBounds( item.getBounds( 0 ) );
                    tableEditor.setEditor( dnLink, item, 0 );

                    linkVisible = true;
                }

            }

            if ( !linkVisible )
            {
                dnLink.setVisible( false );
                tableEditor.setEditor( null );
            }
        }
    }

}
