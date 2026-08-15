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
package org.apache.directory.studio.schemaeditor.view.dialogs;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.search.SearchPage;
import org.apache.directory.studio.schemaeditor.view.search.SearchPage.SearchInEnum;
import org.apache.directory.studio.schemaeditor.view.views.SearchView;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


// ── CLASS: PreviousSearchesDialog — LUKE'S BINARY SUNSET ─────────────────────
// Luke stands at the edge of the Lars homestead moisture-farm, gazing at Tatooine's
// twin suns sinking toward the horizon. He's reviewing everything that's already
// happened — two suns, two chapters of his life already written — and deciding which
// thread to pick back up. He can stare at the sky (browse the history) or head back
// inside to re-run an old mission (reopen a previous search). He can also erase a
// chapter he no longer wants to think about (remove a search from the history).
// This dialog shows Luke — the user — a scrollable list of past search strings and
// lets them either re-execute one or remove it from the record entirely.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that displays the user's saved search history from the Schema Editor's search view.
 * The user can browse past search strings, open one to re-run it in the search view,
 * or permanently remove an entry they no longer want in their history.
 * Think of this dialog as Luke's binary sunset: a moment to look back at the searches
 * already run and decide which path to retrace.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PreviousSearchesDialog extends Dialog
{
    /** The associated view */
    private SearchView view;

    // UI Fields
    private TableViewer tableViewer;
    private Button openButton;
    private Button removeButton;


    // ── Luke Carries a Reminder of the Search View ───────────────────────────
    // Luke doesn't gaze at the sunset in isolation — he keeps in mind which door
    // leads back into the homestead (the SearchView). When he picks a memory to
    // relive, he knows exactly where to walk back to.
    // We store a reference to the SearchView so that when the user picks a previous
    // search and clicks Open, we can push that search string back into the live view.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the dialog, holding a reference to the {@link SearchView} that will
     * receive the replayed search when the user clicks Open.
     *
     * @param view  the Schema Editor search view to replay the selected search into
     */
    public PreviousSearchesDialog( SearchView view )
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        this.view = view;
    }


    // ── The Horizon Is Labeled "Previous Searches" ────────────────────────────
    // As the suns sink, a title card fades in across the horizon so there's no
    // ambiguity about what this scene is showing us. We set the shell title.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the window title bar text to the localised "Previous Searches" label.
     * JFace calls this before the dialog becomes visible.
     *
     * @param newShell  the freshly created Shell JFace hands us to configure
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( Messages.getString( "PreviousSearchesDialog.Previous" ) ); //$NON-NLS-1$
    }


    // ── Luke Spreads His Memories Across the Horizon ──────────────────────────
    // As Luke gazes out at the twin suns, the sky fills with images of past searches —
    // each one a small icon on the horizon, the query string beside it. He sees a
    // Remove button to erase ones he'd rather forget, and an Open button appears
    // in the button bar for the one he wants to revisit.
    // We build the table viewer (backed by the search history), the Remove button,
    // and the inline label, then load the history list immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog's content area: an instruction label, a scrollable table of past
     * search strings (each decorated with the search-history icon), and a Remove button.
     * The Open button lives in the button bar — see {@link #createButtonsForButtonBar}.
     * Double-clicking a row triggers the same action as clicking Open.
     *
     * @param parent  the parent composite supplied by the JFace Dialog framework
     * @return        the assembled composite, handed back to JFace for embedding
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        Label label = new Label( composite, SWT.NONE );
        label.setText( Messages.getString( "PreviousSearchesDialog.ShowResultsInView" ) ); //$NON-NLS-1$
        label.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        tableViewer = new TableViewer( composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE );
        GridData gd = new GridData( SWT.FILL, SWT.NONE, true, false );
        gd.widthHint = 300;
        gd.heightHint = 200;
        tableViewer.getTable().setLayoutData( gd );
        tableViewer.setContentProvider( new ArrayContentProvider() );
        tableViewer.setLabelProvider( new LabelProvider()
        {
            public Image getImage( Object element )
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_SEARCH_HISTORY_ITEM );
            }
        } );
        tableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                openButton.setEnabled( !event.getSelection().isEmpty() );
                removeButton.setEnabled( !event.getSelection().isEmpty() );
            }
        } );
        tableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                buttonPressed( IDialogConstants.OK_ID );
            }
        } );

        removeButton = new Button( composite, SWT.NONE );
        removeButton.setText( Messages.getString( "PreviousSearchesDialog.Remove" ) ); //$NON-NLS-1$
        removeButton.setLayoutData( new GridData( SWT.NONE, SWT.BEGINNING, false, false ) );
        removeButton.setEnabled( false );
        removeButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                StructuredSelection selection = ( StructuredSelection ) tableViewer.getSelection();
                String selectedSearch = ( String ) selection.getFirstElement();
                SearchPage.removeSearchStringHistory( selectedSearch );
                initTableViewer();
            }
        } );

        initTableViewer();

        return composite;
    }


    // ── Luke Loads His Memories from the Archive ──────────────────────────────
    // Luke reaches back into his memory and pulls up every previous search he's
    // run — the archive loads them all in order. If one has just been erased
    // (via Remove), this refreshes the list to reflect the deletion.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads (or reloads) the table viewer from the persisted search-string history.
     * We call this on initial open and again after the user removes an entry,
     * so the displayed list stays in sync with the stored history.
     */
    private void initTableViewer()
    {
        tableViewer.setInput( SearchPage.loadSearchStringHistory() );
    }


    // ── Luke Has Only Two Paths: Revisit or Walk Away ────────────────────────
    // As the suns touch the horizon, Luke knows he either walks back inside to
    // re-run the chosen search (Open) or turns away from the memory entirely (Cancel).
    // The Open button starts disabled and only lights up when Luke focuses on
    // a specific memory in the list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the dialog's button bar with a Cancel button and an Open button.
     * The Open button is initially disabled because nothing is selected yet;
     * the selection-changed listener in {@link #createDialogArea} enables it
     * as soon as the user highlights a row.
     *
     * @param parent  the button bar composite JFace hands us
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
        openButton = createButton( parent, IDialogConstants.OK_ID, Messages.getString( "PreviousSearchesDialog.Open" ), //$NON-NLS-1$
            true );
        openButton.setEnabled( false );
    }


    // ── Luke Steps Back Through the Door and Relives the Search ──────────────
    // Luke makes his decision: he steps away from the sunset, walks back inside,
    // and picks up the thread where he left it. He hands the search string to the
    // SearchView, which replays it with the same scope and "search in" settings
    // that were active when the search was first saved.
    // If Cancel was pressed instead, we just hand control up to the parent without
    // touching the view.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Handles button presses for this dialog.
     * When the user clicks Open (OK_ID) with a row selected, we extract the selected
     * search string and replay it into the {@link SearchView} using the last-saved
     * search-in settings and scope from the {@link SearchPage}.
     * Any other button ID (Cancel) is passed straight up to the parent handler.
     *
     * @param buttonId  the ID of the button that was pressed, as defined in {@link IDialogConstants}
     */
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            if ( !tableViewer.getSelection().isEmpty() )
            {
                StructuredSelection selection = ( StructuredSelection ) tableViewer.getSelection();
                String selectedSearch = ( String ) selection.getFirstElement();

                view.setSearchInput( selectedSearch, SearchPage.loadSearchIn().toArray( new SearchInEnum[0] ),
                    SearchPage.loadScope() );
            }
        }

        super.buttonPressed( buttonId );
    }
}
