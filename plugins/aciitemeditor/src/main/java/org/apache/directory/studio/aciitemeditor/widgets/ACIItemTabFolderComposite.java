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
package org.apache.directory.studio.aciitemeditor.widgets;


import java.text.ParseException;

import org.apache.directory.studio.aciitemeditor.ACIITemConstants;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


// ── CLASS: ACIItemTabFolderComposite — GRAND MOFF'S DUAL-VIEW BRIDGE ──────────
// The Grand Moff's editing console has two views of the same directive:
// a structured visual form and a raw ACI text source.  Switching between them
// triggers a round-trip: visual → parse-to-text or text → parse-to-visual.
// If either parse fails an error dialog appears and the user is returned to the
// safe view.  ACIItemTabFolderComposite manages that dual-view bridge.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Composite} hosting a {@link TabFolder} with two tabs:
 * the Visual editor ({@link ACIItemVisualEditorComposite}) and the Source
 * editor ({@link ACIItemSourceEditorComposite}).
 * Manages bidirectional synchronisation on tab switch and exposes a unified
 * {@code getInput()} / {@code setInput(String)} interface to the dialog.
 * Think of this as the Grand Moff's dual-view bridge: flip between the form
 * and the raw text, parse on every switch, show an error and snap back if
 * anything goes wrong.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemTabFolderComposite extends Composite
{
    /** The index of the visual tab */
    public static final int VISUAL_TAB_INDEX = 0;

    /** The index of the source tab */
    public static final int SOURCE_TAB_INDEX = 1;

    /** The tab folder */
    private TabFolder tabFolder;

    /** The visual editor composite */
    private ACIItemVisualEditorComposite visualComposite;

    /** The source editor composite */
    private ACIItemSourceEditorComposite sourceComposite;


    // ── CONSTRUCT THE DUAL-VIEW BRIDGE ────────────────────────────────────────
    // The two-tab layout is built: visual tab first (index 0), source tab
    // second (index 1); then the tab-switch listener is installed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ACIItemTabFolderComposite}.
     * Builds the tab folder, creates the visual and source tabs, and wires
     * the tab-selection listener that synchronises them.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemTabFolderComposite( Composite parent, int style )
    {
        super( parent, style );
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        setLayout( layout );
        setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        createTabFolder();
        createVisualTab();
        createSourceTab();

        initListeners();
    }


    // ── WIRE THE TAB-SWITCH LISTENER ──────────────────────────────────────────
    // When the officer clicks a tab the listener fires tabSelected() which
    // handles the round-trip parse.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Installs the tab-selection listener that triggers synchronisation
     * between the visual and source editors on every tab switch.
     */
    private void initListeners()
    {
        tabFolder.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                tabSelected();
            }
        } );
    }


    // ── BUILD THE SOURCE TAB ──────────────────────────────────────────────────
    // The source tab wraps ACIItemSourceEditorComposite in its own container
    // so the editor fills the tab page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the source tab, instantiates the {@link ACIItemSourceEditorComposite},
     * and adds the tab to the tab folder at {@link #SOURCE_TAB_INDEX}.
     */
    private void createSourceTab()
    {
        // create inner container
        Composite sourceContainer = new Composite( tabFolder, SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        sourceContainer.setLayout( layout );
        sourceContainer.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // create source editor
        sourceComposite = new ACIItemSourceEditorComposite( sourceContainer, SWT.NONE );
        sourceComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // create tab
        TabItem sourceTab = new TabItem( tabFolder, SWT.NONE, SOURCE_TAB_INDEX );
        sourceTab.setText( Messages.getString( "ACIItemTabFolderComposite.source.tab" ) ); //$NON-NLS-1$
        sourceTab.setControl( sourceContainer );
    }


    // ── BUILD THE VISUAL TAB ──────────────────────────────────────────────────
    // The visual tab wraps ACIItemVisualEditorComposite in its own container.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the visual tab, instantiates the {@link ACIItemVisualEditorComposite},
     * and adds the tab to the tab folder at {@link #VISUAL_TAB_INDEX}.
     */
    private void createVisualTab()
    {
        // create inner container
        Composite visualContainer = new Composite( tabFolder, SWT.NONE );
        visualContainer.setLayout( new GridLayout() );
        visualContainer.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // create the visual ACIItem composite
        visualComposite = new ACIItemVisualEditorComposite( visualContainer, SWT.NONE );
        visualComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // create tab
        TabItem visualTab = new TabItem( tabFolder, SWT.NONE, VISUAL_TAB_INDEX );
        visualTab.setText( Messages.getString( "ACIItemTabFolderComposite.visual.tab" ) ); //$NON-NLS-1$
        visualTab.setControl( visualContainer );
    }


    // ── CREATE THE TAB FOLDER ─────────────────────────────────────────────────
    /**
     * Instantiates the {@link TabFolder} with tabs at the top.
     */
    private void createTabFolder()
    {
        tabFolder = new TabFolder( this, SWT.TOP );
        tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    }


    // ── HANDLE TAB SWITCH ─────────────────────────────────────────────────────
    // Switching to source: serialise the visual editor to ACI text and load it.
    // Switching to visual: parse the source text and load the visual editor.
    // On either parse failure: show an error dialog and return to the safe tab.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the officer clicks a tab.
     * Synchronises the editors: on switch-to-source, serialises the visual model;
     * on switch-to-visual, parses the source text.
     * On parse failure shows an error dialog and returns to the previous tab.
     */
    private void tabSelected()
    {
        int index = tabFolder.getSelectionIndex();

        if ( index == SOURCE_TAB_INDEX )
        {
            // switched to source tab: serialize visual and set to source
            // on parse error: print message and return to visual tab
            try
            {
                String input = visualComposite.getInput();
                sourceComposite.setInput( input );
            }
            catch ( ParseException pe )
            {
                IStatus status = new Status( IStatus.ERROR, ACIITemConstants.PLUGIN_ID, 1, Messages
                    .getString( "ACIItemTabFolderComposite.error.onVisualEditor" ), pe ); //$NON-NLS-1$
                ErrorDialog.openError( getShell(),
                    Messages.getString( "ACIItemTabFolderComposite.error.title" ), null, status ); //$NON-NLS-1$
                tabFolder.setSelection( VISUAL_TAB_INDEX );
            }
        }
        else if ( index == VISUAL_TAB_INDEX )
        {
            // switched to visual tab: parse source and populate to visual
            // on parse error: print message and return to source tab
            try
            {
                String input = sourceComposite.getInput();
                visualComposite.setInput( input );
            }
            catch ( ParseException pe )
            {
                IStatus status = new Status( IStatus.ERROR, ACIITemConstants.PLUGIN_ID, 1, Messages
                    .getString( "ACIItemTabFolderComposite.error.onSourceEditor" ), pe ); //$NON-NLS-1$
                ErrorDialog.openError( getShell(),
                    Messages.getString( "ACIItemTabFolderComposite.error.title" ), null, status ); //$NON-NLS-1$
                tabFolder.setSelection( SOURCE_TAB_INDEX );
            }
        }
    }


    // ── SET INPUT TO BOTH EDITORS ─────────────────────────────────────────────
    // The dialog calls this when it opens to pre-fill both views from the
    // existing ACI string.  If the string fails to parse as ACI, the source
    // tab is activated so the officer can see the raw text and fix it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads {@code input} into both the source editor and the visual editor.
     * If the visual editor cannot parse the input, shows an error dialog and
     * activates the source tab.
     *
     * <p>For example — the dialog opens with an existing ACI item:</p>
     * <pre>
     *   tabFolder.setInput(existingAciString);
     *   // → source editor shows raw text
     *   // → visual editor populates all four sub-tabs from parsed ACI
     * </pre>
     *
     * @param input  the ACI string to load
     */
    public void setInput( String input )
    {
        // set input to source editor
        sourceComposite.forceSetInput( input );

        // set input to visual editor, on parse error switch to source editor
        try
        {
            visualComposite.setInput( input );
        }
        catch ( ParseException pe )
        {
            IStatus status = new Status( IStatus.ERROR, ACIITemConstants.PLUGIN_ID, 1, Messages
                .getString( "ACIItemTabFolderComposite.error.onInput" ), pe ); //$NON-NLS-1$
            ErrorDialog.openError( getShell(),
                Messages.getString( "ACIItemTabFolderComposite.error.title" ), null, status ); //$NON-NLS-1$

            tabFolder.setSelection( SOURCE_TAB_INDEX );
        }
    }


    // ── GET INPUT FROM THE ACTIVE EDITOR ──────────────────────────────────────
    // The dialog calls this on Format or Check Syntax to get the current value.
    // We delegate to whichever tab is currently active.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ACI string from the currently active tab.
     * Performs a syntax check before returning; throws {@link ParseException}
     * if the current content is invalid.
     *
     * @return the valid ACI string from the active editor
     * @throws ParseException  if the active editor's content fails to parse
     */
    public String getInput() throws ParseException
    {
        int index = tabFolder.getSelectionIndex();
        if ( index == VISUAL_TAB_INDEX )
        {
            String input = visualComposite.getInput();
            return input;
        }
        else
        {
            String input = sourceComposite.getInput();
            return input;
        }
    }


    // ── SET THE CONNECTION CONTEXT ────────────────────────────────────────────
    // The context (connection + entry + ACI string) is needed by the visual
    // and source editors for schema-driven content assist.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Passes the {@link ACIItemValueWithContext} to both editors so they can
     * use the connection's schema for content assist.
     *
     * @param context  the value context carrying connection and entry information
     */
    public void setContext( ACIItemValueWithContext context )
    {
        sourceComposite.setContext( context );
        visualComposite.setContext( context );
    }


    // ── FORMAT THE SOURCE EDITOR CONTENT ──────────────────────────────────────
    /**
     * Triggers pretty-printing in the source editor.
     * The visual editor does not currently support formatting.
     */
    public void format()
    {
        sourceComposite.format();
        //visualComposite.format();
    }
}
