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
package org.apache.directory.studio.openldap.config.acl.widgets;


import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;


// ── CLASS: OpenLdapAclTabFolderComposite — GRAND MOFF MANAGING TWO TERMINALS ─
// Grand Moff Tarkin oversees the Death Star's two command displays: the Visual
// Editor holotable and the raw Source text terminal. This composite is that
// command centre. When Tarkin switches from one display to the other, this
// composite syncs the two views — pushing the Visual model into the Source
// viewer on a Visual-to-Source switch, and vice versa on a Source-to-Visual
// switch. The dialog delegates getInput(), format(), and saveWidgetSettings()
// through here so the currently visible tab always responds.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT {@link Composite} containing a {@link TabFolder} with two tabs:
 * <ol>
 *   <li><b>Visual Editor</b> (index 0) — the GUI form editor.</li>
 *   <li><b>Source</b> (index 1) — the JFace SourceViewer text editor.</li>
 * </ol>
 * Synchronisation happens on tab switch: switching to Source calls
 * {@link OpenLdapAclSourceEditorComposite#refresh()}, switching to Visual calls
 * {@link OpenLdapAclVisualEditorComposite#refresh()}.
 *
 * <p>Think of this class as Grand Moff Tarkin toggling between his holotable
 * and the raw-code terminal — the active view always shows the latest state.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclTabFolderComposite extends Composite
{
    /** The index of the visual tab */
    public static final int VISUAL_TAB_INDEX = 0;

    /** The index of the source tab */
    public static final int SOURCE_TAB_INDEX = 1;

    /** The tab folder */
    private TabFolder tabFolder;

    /** The visual tab */
    private TabItem visualTab;

    /** The inner container of the visual tab */
    private Composite visualContainer;

    /** The visual editor composite */
    private OpenLdapAclVisualEditorComposite visualComposite;

    /** Tehe source tab */
    private TabItem sourceTab;

    /** The inner container of the visual tab */
    private Composite sourceContainer;

    /** The source editor composite */
    private OpenLdapAclSourceEditorComposite sourceComposite;

    /** The ACL context */
    private OpenLdapAclValueWithContext context;


    // ── Constructing the Tab Folder ────────────────────────────────────────────
    // Grand Moff Tarkin installs both display terminals side by side in the
    // command room: first the tab folder frame, then the Visual tab, then the
    // Source tab, then the selection listener that keeps them in sync.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new tab folder composite. Builds the tab folder, the Visual tab
     * (containing {@link OpenLdapAclVisualEditorComposite}), the Source tab
     * (containing {@link OpenLdapAclSourceEditorComposite}), and wires the
     * selection listener that syncs tabs on switch.
     *
     * @param parent   The parent composite.
     * @param context  The ACL context shared between the two tabs.
     * @param style    SWT style bits for the composite.
     */
    public OpenLdapAclTabFolderComposite( Composite parent, OpenLdapAclValueWithContext context, int style )
    {
        super( parent, style );

        this.context = context;

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout( layout );

        createTabFolder();
        createVisualTab();
        createSourceTab();

        initListeners();
    }


    // ── Wiring the Tab-Selection Listener ─────────────────────────────────────
    // Grand Moff Tarkin's aide watches the control panel. The moment the
    // selection changes she calls tabSelected() to sync the new display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a selection listener to the tab folder that calls {@link #tabSelected()}
     * whenever the user switches tabs.
     */
    private void initListeners()
    {
        tabFolder.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                tabSelected();
            }
        } );
    }


    // ── Creating the Source Tab ────────────────────────────────────────────────
    // Tarkin installs the raw text terminal: a bordered composite that fills
    // with the SourceEditor composite and labels the tab "Source".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Source tab at index {@link #SOURCE_TAB_INDEX}. Builds a
     * bordered inner container, creates the {@link OpenLdapAclSourceEditorComposite}
     * inside it, and attaches the container to the tab item.
     */
    private void createSourceTab()
    {
        // create inner container
        sourceContainer = new Composite( tabFolder, SWT.BORDER );
        sourceContainer.setLayout( new FillLayout() );

        // create source editor
        sourceComposite = new OpenLdapAclSourceEditorComposite( sourceContainer, context, SWT.NONE );

        // create tab
        sourceTab = new TabItem( tabFolder, SWT.NONE, SOURCE_TAB_INDEX );
        sourceTab.setText( "Source" );
        sourceTab.setControl( sourceContainer );
    }


    // ── Creating the Visual Tab ────────────────────────────────────────────────
    // Tarkin installs the holotable: a grid-layout composite that fills with
    // the Visual editor and labels the tab "Visual Editor".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Visual Editor tab at index {@link #VISUAL_TAB_INDEX}. Builds a
     * grid-layout container, creates the {@link OpenLdapAclVisualEditorComposite}
     * inside it, and attaches the container to the tab item.
     */
    private void createVisualTab()
    {
        // create inner container
        visualContainer = new Composite( tabFolder, SWT.NONE );
        visualContainer.setLayout( new GridLayout() );
        visualContainer.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // create the visual ACIItem composite
        visualComposite = new OpenLdapAclVisualEditorComposite( visualContainer, context, SWT.NONE );
        visualComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // create tab
        visualTab = new TabItem( tabFolder, SWT.NONE, VISUAL_TAB_INDEX );
        visualTab.setText( "Visual Editor" );
        visualTab.setControl( visualContainer );
    }


    // ── Creating the Tab Folder Frame ─────────────────────────────────────────
    // The enclosing tab folder fills the composite and anchors both tabs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT {@link TabFolder} that hosts both tabs, filling the
     * composite with {@link SWT#TOP} tab style.
     */
    private void createTabFolder()
    {
        tabFolder = new TabFolder( this, SWT.TOP );
        tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    }


    // ── Synchronising Tabs on Switch ─────────────────────────────────────────
    // When Tarkin switches displays, the newly active terminal refreshes itself
    // from the model so both views always agree on the current ACL state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a tab is selected. Synchronises the two editors:
     * <ul>
     *   <li>Switching to Source: calls {@link OpenLdapAclSourceEditorComposite#refresh()}
     *       to push the current model text into the source viewer.</li>
     *   <li>Switching to Visual: calls {@link OpenLdapAclVisualEditorComposite#refresh()}
     *       to redraw the visual form from the model.</li>
     * </ul>
     */
    private void tabSelected()
    {
        int index = tabFolder.getSelectionIndex();

        if ( index == SOURCE_TAB_INDEX )
        {
            sourceComposite.refresh();
        }
        else if ( index == VISUAL_TAB_INDEX )
        {
            visualComposite.refresh();
        }
    }


    // ── Returning the Canonical ACL String From the Active Tab ────────────────
    // On OK, the dialog asks Tarkin for the canonical ACL string. He delegates
    // to whichever display is currently active.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the canonical ACL string from the currently active tab. Delegates
     * to {@link OpenLdapAclVisualEditorComposite#getInput()} when the Visual tab
     * is active, or to {@link OpenLdapAclSourceEditorComposite#getInput()} when
     * the Source tab is active.
     *
     * @return  The canonical ACL string.
     * @throws ParseException  If the active tab's content is syntactically invalid.
     */
    public String getInput() throws ParseException
    {
        int index = tabFolder.getSelectionIndex();

        if ( index == VISUAL_TAB_INDEX )
        {
            return visualComposite.getInput();
        }
        else
        {
            return sourceComposite.getInput();
        }
    }


    // ── Triggering the Format Pass Via the Active Tab ─────────────────────────
    // The Format button in the dialog is only meaningful when the Source tab is
    // active. Tarkin ignores the button when Visual is shown.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Runs the formatting pass on the source viewer content. Has no effect when
     * the Visual Editor tab is active. Delegates to
     * {@link OpenLdapAclSourceEditorComposite#format()} when the Source tab is
     * selected.
     */
    public void format()
    {
        if ( tabFolder.getSelectionIndex() == SOURCE_TAB_INDEX )
        {
            sourceComposite.format();
        }
    }


    // ── Persisting Widget Settings Via the Visual Tab ─────────────────────────
    // The dialog calls this before closing so the visual composite can persist
    // any user preferences (e.g. expand/collapse state of the expandable sections).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link OpenLdapAclVisualEditorComposite#saveWidgetSettings()} to
     * persist any widget-level preferences (such as expand/collapse state).
     * Called by the dialog before it closes.
     */
    public void saveWidgetSettings()
    {
        visualComposite.saveWidgetSettings();
    }
}
