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
package org.apache.directory.studio.ldapbrowser.ui.dialogs.preferences;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.entryeditors.EntryEditorExtension;
import org.apache.directory.studio.entryeditors.EntryEditorManager;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: EntryEditorsPreferencePage — PALPATINE ISSUES ORDER 66 ────────────
// Palpatine's Order 66 directive told every clone which Jedi to trust and in
// what priority — the ones highest on the list were hunted first.  This preference
// page does the same for entry editors: it lets you rank which editor plugins
// Eclipse will open first when the user double-clicks an LDAP entry, and whether
// to respect the application-wide "open mode" or fall back to historical behavior.
// Like Order 66, getting the priority order wrong here has immediate and visible
// consequences — the wrong editor opens and the user gets confused.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse preference page for configuring entry editor plugin priority and open mode.
 * It lets users choose between "historical behavior" (each entry editor picks its
 * own tab) and "application-wide open mode," and reorder the registered entry
 * editor extensions by priority.
 * Think of this page as Palpatine's priority matrix — entries at the top of the
 * list get activated first when the user opens a directory entry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    /** A flag indicating whether or not to use the user's priority for entry editors */
    private boolean useUserPriority = false;

    /** The open mode setting value */
    private int openMode = 0;

    /** The ordered list of entry editors */
    private List<EntryEditorExtension> sortedEntryEditorsList;

    // UI fields
    private Button historicalBehaviorButton;
    private Button useApplicationWideOpenModeButton;
    private TableViewer entryEditorsTableViewer;
    private Button upEntryEditorButton;
    private Button downEntryEditorButton;
    private Button restoreDefaultsEntryEditorsButton;


    // ── PALPATINE OPENS THE ORDER 66 BRIEFING ROOM ────────────────────────────
    // Before Palpatine can issue his directives, someone has to set up the briefing
    // room — title on the door, preference store connected, description on the
    // board so every clone knows what this session is about.
    // We do the same: set the page title, wire up the plugin preference store,
    // and put a description on the page so users know what they're configuring.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the preference page, setting its title and description from NLS
     * strings and wiring it to the BrowserUI plugin's preference store.
     * Eclipse calls this when the user navigates to this page in the Preferences dialog.
     *
     * <p>For example — Palpatine prepares the briefing:</p>
     * <pre>
     *   door label: "Entry Editors"
     *   board description: "Configure which editor opens for a selected entry"
     *   store: BrowserUIPlugin.getDefault().getPreferenceStore()
     * </pre>
     */
    public EntryEditorsPreferencePage()
    {
        super( Messages.getString( "EntryEditorsPreferencePage.EntryEditorsPrefPageTitle" ) ); //$NON-NLS-1$
        super.setPreferenceStore( BrowserUIPlugin.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "EntryEditorsPreferencePage.EntryEditorsPrefPageDescription" ) ); //$NON-NLS-1$
    }


    // ── PALPATINE READS THE CURRENT DEPLOYMENT STATUS ─────────────────────────
    // Before issuing new orders, Palpatine consults the current state — how many
    // clones are deployed, what mode they're operating in — so his directives
    // reflect reality rather than assumption.
    // We load the current openMode and useUserPriority preference values here so
    // the UI renders the right radio button and editor order when the page opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads the current open mode and user-priority preference values from the
     * plugin's preference store.
     * Eclipse calls this once when the preference page is initialized; the values
     * we load here drive which radio button is selected in {@link #createContents}.
     *
     * <p>For example — Palpatine checks the current deployment status:</p>
     * <pre>
     *   openMode = PREFERENCE_ENTRYEDITORS_OPEN_MODE  (historical or app-wide)
     *   useUserPriority = PREFERENCE_ENTRYEDITORS_USE_USER_PRIORITIES
     * </pre>
     *
     * @param workbench  The Eclipse workbench — we don't use it directly but must
     *                   accept it because we implement {@link IWorkbenchPreferencePage}.
     */
    public void init( IWorkbench workbench )
    {
        openMode = BrowserUIPlugin.getDefault().getPluginPreferences().getInt(
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE );

        useUserPriority = BrowserUIPlugin.getDefault().getPluginPreferences().getBoolean(
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_USE_USER_PRIORITIES );
    }


    // ── PALPATINE CONSTRUCTS THE PRIORITY BRIEFING BOARD ─────────────────────
    // The Emperor's briefing room has two sections: "Open Mode" (how clones should
    // react when an enemy is sighted) and "Entry Editors" (the ranked list of who
    // gets called first).  Each section has controls for adjusting priorities,
    // and a description area so commanders know why each editor is in the list.
    // We build the equivalent UI here: two radio buttons for open mode and a
    // sortable table of registered entry editor extensions.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page UI: an "Open Mode" group with two radio buttons
     * and an "Entry Editors" group with a sortable table of registered editor
     * plugins plus Up / Down / Restore Defaults buttons.
     * Selecting a row in the table shows its description at the bottom.
     *
     * <p>For example — Palpatine lays out the priority board:</p>
     * <pre>
     *   Open Mode group:
     *     ○ Historical Behavior  (each editor manages its own tab)
     *     ● Application-Wide Setting  (Eclipse controls the tab strategy)
     *   Entry Editors group:
     *     [TableViewer: Single-tab Editor | Multi-tab Editor | LDIF Editor]
     *     [Up] [Down] [Restore Defaults]
     *     Description: "Shows entry attributes in a simple table view."
     * </pre>
     *
     * @param parent  The parent composite provided by Eclipse's preference dialog.
     * @return        The top-level composite we constructed.
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Open Mode Group
        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        Group openModeGroup = BaseWidgetUtils.createGroup( BaseWidgetUtils.createColumnContainer( composite, 1, 1 ),
            Messages.getString( "EntryEditorsPreferencePage.OpenMode" ), 1 ); //$NON-NLS-1$

        // Historical Behavior Button
        historicalBehaviorButton = BaseWidgetUtils.createRadiobutton( openModeGroup, Messages
            .getString( "EntryEditorsPreferencePage.HistoricalBehavior" ), 1 ); //$NON-NLS-1$
        Composite historicalBehaviorComposite = BaseWidgetUtils.createColumnContainer( openModeGroup, 2, 1 );
        BaseWidgetUtils.createRadioIndent( historicalBehaviorComposite, 1 );
        Label historicalBehaviourLabel = BaseWidgetUtils.createWrappedLabel( historicalBehaviorComposite, Messages
            .getString( "EntryEditorsPreferencePage.HistoricalBehaviorTooltip" ), 1 ); //$NON-NLS-1$
        GridData historicalBehaviourLabelGridData = new GridData( GridData.FILL_HORIZONTAL );
        historicalBehaviourLabelGridData.widthHint = 300;
        historicalBehaviourLabel.setLayoutData( historicalBehaviourLabelGridData );

        // Use Application Wide Open Mode Button
        useApplicationWideOpenModeButton = BaseWidgetUtils.createRadiobutton( openModeGroup, Messages
            .getString( "EntryEditorsPreferencePage.ApplicationWideSetting" ), 1 ); //$NON-NLS-1$
        Composite useApplicationWideOpenModeComposite = BaseWidgetUtils.createColumnContainer( openModeGroup, 2, 1 );
        BaseWidgetUtils.createRadioIndent( useApplicationWideOpenModeComposite, 1 );
        Link link = BaseWidgetUtils.createLink( useApplicationWideOpenModeComposite, Messages
            .getString( "EntryEditorsPreferencePage.ApplicationWideSettingTooltip" ), 1 ); //$NON-NLS-1$
        GridData linkGridData = new GridData( GridData.FILL_HORIZONTAL );
        linkGridData.widthHint = 300;
        link.setLayoutData( linkGridData );
        link.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                PreferencesUtil.createPreferenceDialogOn( getShell(),
                    "org.eclipse.ui.preferencePages.Workbench", null, null ); //$NON-NLS-1$
            }
        } );

        // Initializing the UI from the preferences value
        if ( openMode == BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_HISTORICAL_BEHAVIOR )
        {
            historicalBehaviorButton.setSelection( true );
            useApplicationWideOpenModeButton.setSelection( false );
        }
        else if ( openMode == BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_APPLICATION_WIDE )
        {
            historicalBehaviorButton.setSelection( false );
            useApplicationWideOpenModeButton.setSelection( true );
        }

        // Entry Editors Group
        BaseWidgetUtils.createSpacer( composite, 1 );
        BaseWidgetUtils.createSpacer( composite, 1 );
        Group entryEditorsGroup = BaseWidgetUtils.createGroup(
            BaseWidgetUtils.createColumnContainer( composite, 1, 1 ), Messages
                .getString( "EntryEditorsPreferencePage.EntryEditors" ), 1 ); //$NON-NLS-1$

        // Entry Editors Label
        Label entryEditorsLabel = BaseWidgetUtils.createWrappedLabel( entryEditorsGroup, Messages
            .getString( "EntryEditorsPreferencePage.EntryEditorsLabel" ), 1 ); //$NON-NLS-1$
        GridData entryEditorsLabelGridData = new GridData( GridData.FILL_HORIZONTAL );
        entryEditorsLabelGridData.widthHint = 300;
        entryEditorsLabel.setLayoutData( entryEditorsLabelGridData );

        // Entry Editors Composite
        Composite entryEditorsComposite = new Composite( entryEditorsGroup, SWT.NONE );
        GridLayout gl = new GridLayout( 2, false );
        gl.marginHeight = gl.marginWidth = 0;
        entryEditorsComposite.setLayout( gl );
        entryEditorsComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // SchemaConnectors TableViewer
        entryEditorsTableViewer = new TableViewer( entryEditorsComposite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION );
        GridData gridData = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 3 );
        gridData.heightHint = 125;
        entryEditorsTableViewer.getTable().setLayoutData( gridData );
        entryEditorsTableViewer.setContentProvider( new ArrayContentProvider() );
        entryEditorsTableViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                return ( ( EntryEditorExtension ) element ).getName();
            }


            public Image getImage( Object element )
            {
                return ( ( EntryEditorExtension ) element ).getIcon().createImage();
            }
        } );
        entryEditorsTableViewer.setInput( BrowserUIPlugin.getDefault().getEntryEditorManager()
            .getEntryEditorExtensions() );

        // Up Button
        upEntryEditorButton = BaseWidgetUtils.createButton( entryEditorsComposite, Messages
            .getString( "EntryEditorsPreferencePage.Up" ), 1 ); //$NON-NLS-1$
        upEntryEditorButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        upEntryEditorButton.setEnabled( false );
        upEntryEditorButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                moveSelectedEntryEditor( MoveEntryEditorDirectionEnum.UP );
            }
        } );

        // Down Button
        downEntryEditorButton = BaseWidgetUtils.createButton( entryEditorsComposite, Messages
            .getString( "EntryEditorsPreferencePage.Down" ), 1 ); //$NON-NLS-1$
        downEntryEditorButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        downEntryEditorButton.setEnabled( false );
        downEntryEditorButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                moveSelectedEntryEditor( MoveEntryEditorDirectionEnum.DOWN );
            }
        } );

        // Restore Defaults Button
        restoreDefaultsEntryEditorsButton = BaseWidgetUtils.createButton( entryEditorsComposite, Messages
            .getString( "EntryEditorsPreferencePage.RestoreDefaults" ), 1 ); //$NON-NLS-1$
        restoreDefaultsEntryEditorsButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        restoreDefaultsEntryEditorsButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                performDefaultsEntryEditors();
            }
        } );

        // Description Label
        BaseWidgetUtils.createLabel( entryEditorsGroup, Messages
            .getString( "EntryEditorsPreferencePage.DescriptionColon" ), 1 ); //$NON-NLS-1$

        // Description Text
        final Text descriptionText = new Text( entryEditorsGroup, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY );
        descriptionText.setEditable( false );
        gridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        gridData.heightHint = 27;
        gridData.widthHint = 300;
        descriptionText.setLayoutData( gridData );
        entryEditorsTableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                // Getting the selected entry editor
                EntryEditorExtension entryEditor = ( EntryEditorExtension ) ( ( StructuredSelection ) entryEditorsTableViewer
                    .getSelection() ).getFirstElement();
                if ( entryEditor != null )
                {
                    // Updating the description text field
                    descriptionText.setText( entryEditor.getDescription() );

                    // Updating the state of the buttons
                    updateButtonsState( entryEditor );
                }
            }
        } );

        if ( useUserPriority )
        {
            sortEntryEditorsByUserPriority();
        }
        else
        {
            sortEntryEditorsByDefaultPriority();
        }

        // Selecting the first entry editor
        if ( sortedEntryEditorsList.size() > 0 )
        {
            entryEditorsTableViewer.setSelection( new StructuredSelection( sortedEntryEditorsList.get( 0 ) ) );
        }

        return composite;
    }


    // ── PALPATINE APPLIES CUSTOM CLONE RANKINGS ───────────────────────────────
    // When a commander has reorganized the clone battalions by their own criteria,
    // Palpatine respects those rankings rather than reverting to the original list.
    // We load the user's custom editor ordering from preferences and display it
    // in the table so the page reflects the user's deliberate choices.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads and displays the entry editors sorted by the user's saved priority order.
     * We also store the sorted list locally so the Up/Down buttons can manipulate it
     * without re-querying the manager.
     *
     * <p>For example — Palpatine uses the commander's custom deployment order:</p>
     * <pre>
     *   user's saved order: [LDIF Editor, Multi-tab, Single-tab]
     *   table shows that order instead of the plugin default
     * </pre>
     */
    private void sortEntryEditorsByUserPriority()
    {
        // Getting the entry editors sorted by user's priority
        sortedEntryEditorsList = new ArrayList<EntryEditorExtension>( BrowserUIPlugin.getDefault()
            .getEntryEditorManager().getEntryEditorExtensionsSortedByUserPriority() );

        // Assigning the sorted editors to the viewer
        entryEditorsTableViewer.setInput( sortedEntryEditorsList );
    }


    // ── PALPATINE RESTORES THE ORIGINAL DEPLOYMENT ORDER ─────────────────────
    // When no commander has customized anything, Palpatine falls back to the
    // original order defined in the imperial charter — the plugin's default ranking.
    // We load the factory-default ordering from the entry editor manager so users
    // who haven't tweaked anything see a sensible, deterministic list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads and displays the entry editors sorted by their default (plugin-declared)
     * priority order.
     * Called on first page open when no user customization exists, and when the
     * user clicks "Restore Defaults."
     *
     * <p>For example — Palpatine reads from the original imperial charter:</p>
     * <pre>
     *   default order: [Single-tab, Multi-tab, LDIF Editor]
     *   that's what the table shows until the user reorders things
     * </pre>
     */
    private void sortEntryEditorsByDefaultPriority()
    {
        // Getting the entry editors sorted by default priority
        sortedEntryEditorsList = new ArrayList<EntryEditorExtension>( BrowserUIPlugin.getDefault()
            .getEntryEditorManager().getEntryEditorExtensionsSortedByDefaultPriority() );

        // Assigning the sorted editors to the viewer
        entryEditorsTableViewer.setInput( sortedEntryEditorsList );
    }


    // ── PALPATINE PROMOTES OR DEMOTES A CLONE UNIT ───────────────────────────
    // Order 66 had a priority list — Palpatine could move any clone battalion
    // higher or lower in the deployment sequence.  Up = higher priority (acts
    // first), Down = lower priority.
    // We swap the selected editor with its neighbor in {@code sortedEntryEditorsList}
    // and refresh the table to show the new order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Moves the currently selected entry editor one position up or down in the
     * sorted list, then refreshes the table and updates the button states.
     * Also sets {@code useUserPriority = true} so we know to persist this custom
     * order when the user clicks OK.
     *
     * <p>For example — Palpatine promotes the LDIF editor above the table editor:</p>
     * <pre>
     *   selected: LDIF Editor (index 2) → direction UP → swapped with index 1 →
     *   table refreshed → Up/Down buttons re-evaluated for new position
     * </pre>
     *
     * @param direction  {@code UP} to increase priority (move toward index 0),
     *                   {@code DOWN} to decrease it.
     */
    private void moveSelectedEntryEditor( MoveEntryEditorDirectionEnum direction )
    {
        StructuredSelection selection = ( StructuredSelection ) entryEditorsTableViewer.getSelection();
        if ( selection.size() == 1 )
        {
            EntryEditorExtension entryEditor = ( EntryEditorExtension ) selection.getFirstElement();
            if ( sortedEntryEditorsList.contains( entryEditor ) )
            {
                int oldIndex = sortedEntryEditorsList.indexOf( entryEditor );
                int newIndex = 0;

                // Determining the new index number
                switch ( direction )
                {
                    case UP:
                        newIndex = oldIndex - 1;
                        break;
                    case DOWN:
                        newIndex = oldIndex + 1;
                        break;
                }

                // Checking bounds
                if ( ( newIndex >= 0 ) && ( newIndex < sortedEntryEditorsList.size() ) )
                {
                    // Switching the two entry editors
                    EntryEditorExtension newIndexEntryEditorBackup = sortedEntryEditorsList.set( newIndex, entryEditor );
                    sortedEntryEditorsList.set( oldIndex, newIndexEntryEditorBackup );

                    // Reloading the viewer
                    entryEditorsTableViewer.refresh();

                    // Updating the state of the buttons
                    updateButtonsState( entryEditor );

                    // Setting the "Use User Priority" to true
                    useUserPriority = true;
                }
            }
        }
    }


    // ── PALPATINE CHECKS WHICH UNITS CAN STILL BE RERANKED ───────────────────
    // After a unit is promoted or demoted, Palpatine re-checks the board: the top
    // unit can't be promoted further, the bottom unit can't be demoted further,
    // so the corresponding buttons must be disabled to prevent invalid moves.
    // We enable/disable Up and Down based on the selected editor's position in
    // the sorted list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the Up and Down buttons based on where the given editor
     * sits in the sorted list.
     * Up is disabled when the editor is already first (index 0); Down is disabled
     * when it's last (index == size - 1).
     *
     * <p>For example — Palpatine checks rank limits:</p>
     * <pre>
     *   selected editor at index 0 → Up disabled, Down enabled
     *   selected editor at index 2 of 3 → Up enabled, Down disabled
     * </pre>
     *
     * @param entryEditor  The currently selected entry editor extension whose
     *                     position in the list determines button states.
     */
    private void updateButtonsState( EntryEditorExtension entryEditor )
    {
        // Getting the index of the entry editor in the list
        int index = sortedEntryEditorsList.indexOf( entryEditor );

        // Updating up button state
        upEntryEditorButton.setEnabled( index > 0 );

        // Updating down button state
        downEntryEditorButton.setEnabled( index <= ( sortedEntryEditorsList.size() - 2 ) );
    }


    // ── PALPATINE REFRESHES THE BUTTON STATUS FOR CURRENT SELECTION ───────────
    // When Palpatine needs to refresh the board without knowing which unit is
    // selected, he looks at the current selection and re-evaluates from there.
    // We overload updateButtonsState to pull the selection from the viewer rather
    // than requiring the caller to pass the editor explicitly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Re-evaluates the Up/Down button states based on whatever is currently
     * selected in the table viewer.
     * Called after restore-defaults operations when we need to reset button states
     * without knowing which editor ends up selected.
     *
     * <p>For example — Palpatine re-checks the board after a reset:</p>
     * <pre>
     *   viewer selection queried → first element extracted →
     *   updateButtonsState(entryEditor) called with that element
     * </pre>
     */
    private void updateButtonsState()
    {
        StructuredSelection selection = ( StructuredSelection ) entryEditorsTableViewer.getSelection();
        if ( selection.size() == 1 )
        {
            EntryEditorExtension entryEditor = ( EntryEditorExtension ) selection.getFirstElement();

            // Updating the state of the buttons
            updateButtonsState( entryEditor );
        }
    }

    // ── INTERNAL ENUM: Direction for moving entry editors ──────────────────────
    /**
     * Direction enum used internally by {@link #moveSelectedEntryEditor}.
     * UP means increase priority (move toward index 0 in the list),
     * DOWN means decrease it.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private enum MoveEntryEditorDirectionEnum
    {
        UP, DOWN
    }


    // ── PALPATINE TRANSMITS THE UPDATED ORDER 66 ─────────────────────────────
    // When the briefing is done and the commander clicks OK, Palpatine transmits
    // the finalized directives to every clone in the galaxy via the holographic
    // network — the preference store.  Every plugin that reads these preferences
    // will now activate according to the new priority list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current UI state back to the plugin's preference store.
     * Persists open mode, the user-priority flag, and (if user-priority is active)
     * the ordered list of editor IDs joined by the priorities separator.
     *
     * <p>For example — Palpatine transmits the final directives:</p>
     * <pre>
     *   openMode = HISTORICAL_BEHAVIOR → stored
     *   useUserPriority = true → stored
     *   sorted editor IDs = "singleTab:multiTab:ldif" → stored
     *   → next time the user opens an entry, the right editor activates first
     * </pre>
     *
     * @return  Always {@code true}; the preference store never rejects our writes.
     */
    public boolean performOk()
    {
        if ( historicalBehaviorButton.getSelection() )
        {
            BrowserUIPlugin.getDefault().getPluginPreferences().setValue(
                BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE,
                BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_HISTORICAL_BEHAVIOR );
        }
        else if ( useApplicationWideOpenModeButton.getSelection() )
        {
            BrowserUIPlugin.getDefault().getPluginPreferences().setValue(
                BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE,
                BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_APPLICATION_WIDE );
        }

        BrowserUIPlugin.getDefault().getPluginPreferences().setValue(
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_USE_USER_PRIORITIES, useUserPriority );

        if ( useUserPriority )
        {
            StringBuilder sb = new StringBuilder();
            for ( EntryEditorExtension entryEditor : sortedEntryEditorsList )
            {
                sb.append( entryEditor.getId() + EntryEditorManager.PRIORITIES_SEPARATOR );
            }

            if ( sb.length() > 0 )
            {
                sb.deleteCharAt( sb.length() - 1 );
            }

            BrowserUIPlugin.getDefault().getPluginPreferences().setValue(
                BrowserUIConstants.PREFERENCE_ENTRYEDITORS_USER_PRIORITIES, sb.toString() );
        }

        return true;
    }


    // ── PALPATINE REVERTS TO THE ORIGINAL IMPERIAL CHARTER ───────────────────
    // When Palpatine's custom orders are revoked, everything reverts to the
    // original charter — the defaults baked in before any customization happened.
    // We read the plugin's default preference values and reset the UI to match,
    // then rebuild the editor list from the default priority order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets the open mode and entry editor order to plugin-default values.
     * Reads defaults from the preference store (not current values), resets the
     * radio buttons, and calls {@link #performDefaultsEntryEditors()} to rebuild
     * the editor table from the default priority order.
     *
     * <p>For example — Palpatine's orders are revoked and originals restored:</p>
     * <pre>
     *   default openMode = HISTORICAL_BEHAVIOR → historicalBehaviorButton selected
     *   default useUserPriority = false → sortEntryEditorsByDefaultPriority called
     * </pre>
     */
    protected void performDefaults()
    {
        openMode = BrowserUIPlugin.getDefault().getPluginPreferences().getDefaultInt(
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE );

        if ( openMode == BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_HISTORICAL_BEHAVIOR )
        {
            historicalBehaviorButton.setSelection( true );
            useApplicationWideOpenModeButton.setSelection( false );
        }
        else if ( openMode == BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_APPLICATION_WIDE )
        {
            historicalBehaviorButton.setSelection( false );
            useApplicationWideOpenModeButton.setSelection( true );
        }

        performDefaultsEntryEditors();

        super.performDefaults();
    }


    // ── PALPATINE RESETS JUST THE EDITOR PRIORITY LIST ───────────────────────
    // Sometimes only the deployment order needs resetting, not the open-mode
    // directive — so Palpatine resets just the priority matrix to its default.
    // We reset the useUserPriority flag and rebuild the table from default or
    // user priority depending on what the defaults say.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets only the entry editors priority list to its default state.
     * Called both from {@link #performDefaults()} and from the "Restore Defaults"
     * button in the Entry Editors group.
     * Reads the default value of the use-user-priority flag, then rebuilds the
     * sorted editor list and refreshes the Up/Down button states.
     *
     * <p>For example — only the clone priority ranking is reset, not the open mode:</p>
     * <pre>
     *   default useUserPriority = false → sortEntryEditorsByDefaultPriority()
     *   button states recalculated for the newly selected first row
     * </pre>
     */
    private void performDefaultsEntryEditors()
    {
        useUserPriority = BrowserUIPlugin.getDefault().getPluginPreferences().getDefaultBoolean(
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_USE_USER_PRIORITIES );

        if ( useUserPriority )
        {
            sortEntryEditorsByUserPriority();
        }
        else
        {
            sortEntryEditorsByDefaultPriority();
        }

        updateButtonsState();
    }
}
