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
package org.apache.directory.studio.schemaeditor.view.preferences;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: SearchViewPreferencePage — PALPATINE ISSUING ORDER 66 ─────────────
// Palpatine opens a third command channel — this one directed at the Search
// View battalion. Just as he configured the Schema View troopers to label
// nodes a certain way, he now issues the same style of directives specifically
// for the Search Results display: which label format to use, how long labels
// may be, whether secondary labels appear beneath each result, and whether the
// schema name shows alongside every hit.
// The administrator adjusts the controls on this page and clicks Apply; the
// search results view reconfigures itself exactly as directed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse preference page that controls how schema elements are labelled
 * in the Search View results list — label format (first alias, all aliases,
 * or OID), maximum label length, secondary label options, and schema name
 * visibility.
 * It extends {@link PreferencePage} and implements
 * {@link IWorkbenchPreferencePage} so Eclipse registers it under
 * Window → Preferences → Schema Editor → Search View.
 * Think of this as Palpatine's Search View command panel: every setting here
 * is an Order that the Search View executes the moment Apply is pressed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    /** The preference page ID */
    public static final String ID = PluginConstants.PREF_PAGE_SEARCH_VIEW_ID;

    // UI fields
    private Combo labelCombo;
    private Button limitButton;
    private Text lengthText;
    private Button secondaryLabelButtonDisplay;
    private Combo secondaryLabelCombo;
    private Button secondaryLabelLimitButton;
    private Text secondaryLabelLengthText;
    private Button schemaLabelButtonDisplay;


    // ── PALPATINE OPENS THE SEARCH VIEW COMMAND CHANNEL ──────────────────────
    // Palpatine activates a dedicated holocomm frequency for the Search View
    // battalion — separate from the Schema View and Hierarchy View channels —
    // and wires it to the correct preference store before the page opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link SearchViewPreferencePage} and wires it to the
     * plugin's preference store.
     * Eclipse instantiates this via the preferences extension point; we call
     * {@code super()} to initialise the JFace preference-page machinery, then
     * attach our store and set the description shown at the top of the page.
     *
     * <p>For example — Palpatine activates the Search View comm channel:</p>
     * <pre>
     *   new SearchViewPreferencePage()
     *   // wired to plugin preference store
     *   // description: "General settings for the Search View."
     * </pre>
     */
    public SearchViewPreferencePage()
    {
        super();
        setPreferenceStore( Activator.getDefault().getPreferenceStore() );
        setDescription( Messages.getString( "SearchViewPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── PALPATINE LAYS OUT THE SEARCH VIEW CONTROL PANELS ────────────────────
    // Palpatine arranges the Search View controls in front of him: primary
    // label format, truncation dial, secondary label toggle, and the schema
    // name visibility switch — everything the Search View battalion needs to
    // know about how to display search results.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page UI inside the provided parent composite.
     * Eclipse calls this when the user navigates to this preference page; we
     * construct four groups of controls: primary label, secondary label,
     * schema-label visibility, and their accompanying length text fields.
     * We also attach verify listeners to keep the length fields numeric-only,
     * load current values, and wire up change listeners.
     *
     * <p>For example — Palpatine's Search View war room is prepared:</p>
     * <pre>
     *   Label Group:
     *     Use [First Name | All Aliases | OID] as label
     *     [ ] Limit label length to [___] characters
     *   Secondary Label Group:
     *     [ ] Display secondary label
     *     Use [First Name | All Aliases | OID] as secondary label
     *   Schema Label Group:
     *     [ ] Display schema label
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides as the parent container
     * @return        the outermost control we created, handed back to Eclipse
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Label Group
        Group labelGroup = new Group( composite, SWT.NONE );
        labelGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        labelGroup.setText( Messages.getString( "SearchViewPreferencePage.Label" ) ); //$NON-NLS-1$
        labelGroup.setLayout( new GridLayout() );
        Composite labelGroupComposite = new Composite( labelGroup, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        gl.marginHeight = gl.marginWidth = 0;
        labelGroupComposite.setLayout( gl );
        labelGroupComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Label row composite
        Composite labelComposite = new Composite( labelGroupComposite, SWT.NONE );
        gl = new GridLayout( 3, false );
        gl.marginHeight = gl.marginWidth = 0;
        labelComposite.setLayout( gl );
        GridData gd = new GridData( SWT.FILL, SWT.NONE, true, false );
        gd.horizontalSpan = 1;
        labelComposite.setLayoutData( gd );

        // Use Label
        Label useLabel = new Label( labelComposite, SWT.NONE );
        useLabel.setText( Messages.getString( "SearchViewPreferencePage.Use" ) ); //$NON-NLS-1$

        // Label Combo
        labelCombo = new Combo( labelComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        labelCombo.setLayoutData( new GridData() );
        labelCombo
            .setItems( new String[]
                {
                    Messages.getString( "SearchViewPreferencePage.FirstName" ), Messages.getString( "SearchViewPreferencePage.AllAliases" ), Messages.getString( "SearchViewPreferencePage.OID" ) } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        labelCombo.setEnabled( true );

        // As label Label
        Label asLabel = new Label( labelComposite, SWT.NONE );
        asLabel.setText( Messages.getString( "SearchViewPreferencePage.AsLabel" ) ); //$NON-NLS-1$

        // Abbreviate row composite
        Composite abbreviateComposite = new Composite( labelGroupComposite, SWT.NONE );
        gl = new GridLayout( 3, false );
        gl.marginHeight = gl.marginWidth = 0;
        abbreviateComposite.setLayout( gl );
        gd = new GridData( SWT.FILL, SWT.NONE, true, false );
        gd.horizontalSpan = 1;
        abbreviateComposite.setLayoutData( gd );

        // Limit label lenght to Label
        limitButton = new Button( abbreviateComposite, SWT.CHECK );
        limitButton.setText( Messages.getString( "SearchViewPreferencePage.LimitLength" ) ); //$NON-NLS-1$
        gd = new GridData();
        gd.horizontalSpan = 1;
        limitButton.setLayoutData( gd );

        // Lenght Text
        lengthText = new Text( abbreviateComposite, SWT.NONE | SWT.BORDER );
        GridData gridData = new GridData();
        gridData.horizontalSpan = 1;
        gridData.widthHint = 9 * 3;
        lengthText.setLayoutData( gridData );
        lengthText.setTextLimit( 3 );
        lengthText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
                if ( "".equals( lengthText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    e.doit = false;
                }
            }
        } );

        // Characters Label
        Label charactersLabel = new Label( abbreviateComposite, SWT.NONE );
        charactersLabel.setText( Messages.getString( "SearchViewPreferencePage.Characters" ) ); //$NON-NLS-1$

        // Secondary Label Group
        Group secondaryLabelGroup = new Group( composite, SWT.NONE );
        secondaryLabelGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        secondaryLabelGroup.setText( Messages.getString( "SearchViewPreferencePage.SecondaryLabel" ) ); //$NON-NLS-1$
        secondaryLabelGroup.setLayout( new GridLayout() );
        Composite secondaryLabelGroupComposite = new Composite( secondaryLabelGroup, SWT.NONE );
        gl = new GridLayout( 1, false );
        gl.marginHeight = gl.marginWidth = 0;
        secondaryLabelGroupComposite.setLayout( gl );
        secondaryLabelGroupComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        secondaryLabelButtonDisplay = new Button( secondaryLabelGroupComposite, SWT.CHECK );
        secondaryLabelButtonDisplay.setText( Messages.getString( "SearchViewPreferencePage.DisplaySecondaryLabel" ) ); //$NON-NLS-1$

        // Label row composite
        Composite secondaryLabelComposite = new Composite( secondaryLabelGroupComposite, SWT.NONE );
        gl = new GridLayout( 3, false );
        gl.marginHeight = gl.marginWidth = 0;
        secondaryLabelComposite.setLayout( gl );
        gd = new GridData( SWT.FILL, SWT.NONE, true, false );
        gd.horizontalSpan = 1;
        secondaryLabelComposite.setLayoutData( gd );

        // Use Label
        Label useLabel2 = new Label( secondaryLabelComposite, SWT.NONE );
        useLabel2.setText( Messages.getString( "SearchViewPreferencePage.Use" ) ); //$NON-NLS-1$

        // Label Combo
        secondaryLabelCombo = new Combo( secondaryLabelComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        secondaryLabelCombo.setLayoutData( new GridData() );
        secondaryLabelCombo
            .setItems( new String[]
                {
                    Messages.getString( "SearchViewPreferencePage.FirstName" ), Messages.getString( "SearchViewPreferencePage.AllAliases" ), Messages.getString( "SearchViewPreferencePage.OID" ) } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        secondaryLabelCombo.setEnabled( true );

        // As label Label
        Label asLabel2 = new Label( secondaryLabelComposite, SWT.NONE );
        asLabel2.setText( Messages.getString( "SearchViewPreferencePage.AsSecondaryLabel" ) ); //$NON-NLS-1$

        // Abbreviate row composite
        Composite abbreviateComposite2 = new Composite( secondaryLabelGroup, SWT.NONE );
        gl = new GridLayout( 3, false );
        gl.marginHeight = gl.marginWidth = 0;
        abbreviateComposite2.setLayout( gl );
        gd = new GridData( SWT.FILL, SWT.NONE, true, false );
        gd.horizontalSpan = 1;
        abbreviateComposite2.setLayoutData( gd );

        // Limit label lenght to Label
        secondaryLabelLimitButton = new Button( abbreviateComposite2, SWT.CHECK );
        secondaryLabelLimitButton.setText( Messages.getString( "SearchViewPreferencePage.LimitSecondaryLabel" ) ); //$NON-NLS-1$
        gd = new GridData();
        gd.horizontalSpan = 1;
        secondaryLabelLimitButton.setLayoutData( gd );

        // Lenght Text
        secondaryLabelLengthText = new Text( abbreviateComposite2, SWT.NONE | SWT.BORDER );
        gridData = new GridData();
        gridData.horizontalSpan = 1;
        gridData.widthHint = 9 * 3;
        secondaryLabelLengthText.setLayoutData( gridData );
        secondaryLabelLengthText.setTextLimit( 3 );
        secondaryLabelLengthText.addVerifyListener( new VerifyListener()
        {
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
                if ( "".equals( secondaryLabelLengthText.getText() ) && e.text.matches( "[0]" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    e.doit = false;
                }
            }
        } );

        // Characters Label
        Label secondaryLabelcharactersLabel = new Label( abbreviateComposite2, SWT.NONE );
        secondaryLabelcharactersLabel.setText( Messages.getString( "SearchViewPreferencePage.Characters" ) ); //$NON-NLS-1$

        // Schema Label Group
        Group schemaLabelGroup = new Group( composite, SWT.NONE );
        schemaLabelGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        schemaLabelGroup.setText( Messages.getString( "SearchViewPreferencePage.SchemaLabel" ) ); //$NON-NLS-1$
        schemaLabelGroup.setLayout( new GridLayout() );
        Composite schemaLabelGroupComposite = new Composite( schemaLabelGroup, SWT.NONE );
        gl = new GridLayout( 1, false );
        gl.marginHeight = gl.marginWidth = 0;
        schemaLabelGroupComposite.setLayout( gl );
        schemaLabelGroupComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        schemaLabelButtonDisplay = new Button( schemaLabelGroupComposite, SWT.CHECK );
        schemaLabelButtonDisplay.setText( Messages.getString( "SearchViewPreferencePage.DisplaySchemaLabel" ) ); //$NON-NLS-1$

        initFieldsFromPreferences();

        initListeners();

        applyDialogFont( parent );

        return parent;
    }


    // ── PALPATINE REVIEWS THE SEARCH VIEW STATUS REPORT ──────────────────────
    // Palpatine reads the Search View battalion's current disposition: which
    // label format they are using, whether results are being truncated, what
    // secondary information is being displayed.
    // We read those values from the preference store and populate every UI
    // control so the page opens showing the current live configuration.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all Search View preferences from the plugin's preference store and
     * pushes those values into the UI controls so the page opens in a state
     * that accurately reflects what is currently configured.
     * We also update dependent widget enablement so the page is consistent
     * the moment the user first sees it.
     *
     * <p>For example — Palpatine reads the Search View status report:</p>
     * <pre>
     *   store: label=OID, abbreviate=true, maxLength="15", secondaryDisplay=false
     *   → labelCombo selects index 2, limitButton checked, lengthText="15",
     *     secondaryLabel controls all disabled
     * </pre>
     */
    private void initFieldsFromPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        labelCombo.select( store.getInt( PluginConstants.PREFS_SEARCH_VIEW_LABEL ) );
        limitButton.setSelection( store.getBoolean( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE ) );
        lengthText.setEnabled( limitButton.getSelection() );
        lengthText.setText( store.getString( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE_MAX_LENGTH ) );

        secondaryLabelButtonDisplay.setSelection( store
            .getBoolean( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_DISPLAY ) );
        secondaryLabelCombo.select( store.getInt( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL ) );
        secondaryLabelLimitButton.setSelection( store
            .getBoolean( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE ) );
        secondaryLabelLengthText.setText( store
            .getString( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH ) );
        if ( store.getBoolean( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_DISPLAY ) )
        {
            secondaryLabelCombo.setEnabled( true );
            secondaryLabelLimitButton.setEnabled( true );
            secondaryLabelLengthText.setEnabled( secondaryLabelLimitButton.getSelection() );
        }
        else
        {
            secondaryLabelCombo.setEnabled( false );
            secondaryLabelLimitButton.setEnabled( false );
            secondaryLabelLengthText.setEnabled( false );
        }

        schemaLabelButtonDisplay.setSelection( store
            .getBoolean( PluginConstants.PREFS_SEARCH_VIEW_SCHEMA_LABEL_DISPLAY ) );
    }


    // ── PALPATINE'S SEARCH VIEW COMLINKS GO LIVE ─────────────────────────────
    // Each Search View trooper's comlink lights up, ready to receive and act
    // on real-time directive changes as the administrator interacts with the
    // preference page controls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches selection listeners to the checkbox buttons so they can enable
     * or disable dependent widgets in real time as the user interacts with
     * the preference page.
     * For example, when the user unchecks "Display secondary label," all the
     * secondary label controls grey out immediately without requiring Apply.
     *
     * <p>For example — comlinks respond to directive changes:</p>
     * <pre>
     *   limitButton checked   → lengthText enabled
     *   secondaryLabelButtonDisplay unchecked
     *     → secondaryLabelCombo, secondaryLabelLimitButton,
     *        secondaryLabelLengthText all disabled
     * </pre>
     */
    private void initListeners()
    {
        limitButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                lengthText.setEnabled( limitButton.getSelection() );
            }
        } );

        secondaryLabelButtonDisplay.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( secondaryLabelButtonDisplay.getSelection() )
                {
                    secondaryLabelCombo.setEnabled( true );
                    secondaryLabelLimitButton.setEnabled( true );
                    secondaryLabelLengthText.setEnabled( secondaryLabelLimitButton.getSelection() );
                }
                else
                {
                    secondaryLabelCombo.setEnabled( false );
                    secondaryLabelLimitButton.setEnabled( false );
                    secondaryLabelLengthText.setEnabled( false );
                }
            }
        } );

        secondaryLabelLimitButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                secondaryLabelLengthText.setEnabled( secondaryLabelLimitButton.getSelection() );
            }
        } );
    }


    // ── PALPATINE RECALLS THE SEARCH VIEW BASE PLAN ──────────────────────────
    // When a directive revision is needed, Palpatine recalls every Search View
    // trooper to the original factory configuration — the defaults that were
    // baked in when the plugin was first installed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets all UI controls to the plugin's default Search View preference
     * values when the user clicks "Restore Defaults."
     * We read defaults from the preference store (not hard-coded here) and
     * update dependent widget enablement so the page is consistent after the
     * reset, then delegate to the parent class for any remaining JFace work.
     *
     * <p>For example — Palpatine recalls the Search View base plan:</p>
     * <pre>
     *   Default: label=FIRST_NAME, no abbreviation, no secondary label,
     *            schema label hidden
     *   → All controls snap back when the user clicks "Restore Defaults"
     * </pre>
     */
    protected void performDefaults()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        labelCombo.select( store.getDefaultInt( PluginConstants.PREFS_SEARCH_VIEW_LABEL ) );
        limitButton.setSelection( store.getDefaultBoolean( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE ) );
        lengthText.setEnabled( limitButton.getSelection() );
        lengthText.setText( store.getDefaultString( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE_MAX_LENGTH ) );

        secondaryLabelButtonDisplay.setSelection( store
            .getDefaultBoolean( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_DISPLAY ) );
        secondaryLabelCombo.select( store.getDefaultInt( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL ) );
        secondaryLabelLimitButton.setSelection( store
            .getDefaultBoolean( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE ) );
        secondaryLabelLengthText.setText( store
            .getDefaultString( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH ) );

        if ( secondaryLabelButtonDisplay.getSelection() )
        {
            secondaryLabelCombo.setEnabled( true );
            secondaryLabelLimitButton.setEnabled( true );
            secondaryLabelLengthText.setEnabled( secondaryLabelLimitButton.getSelection() );
        }
        else
        {
            secondaryLabelCombo.setEnabled( false );
            secondaryLabelLimitButton.setEnabled( false );
            secondaryLabelLengthText.setEnabled( false );
        }

        schemaLabelButtonDisplay.setSelection( store
            .getDefaultBoolean( PluginConstants.PREFS_SEARCH_VIEW_SCHEMA_LABEL_DISPLAY ) );

        super.performDefaults();
    }


    // ── PALPATINE TRANSMITS THE SEARCH VIEW ORDERS ───────────────────────────
    // Palpatine presses the final control and the Search View battalion
    // receives its directives: display format locked, truncation configured,
    // secondary label and schema name visibility set exactly as specified.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists all current UI control values to the plugin's preference store
     * when the user clicks OK or Apply.
     * We translate combo selections back to their integer preference constants
     * and write everything — label format, abbreviation, secondary label
     * settings, schema label visibility — in one pass.
     * The Search View listens for preference change events and will refresh
     * its display automatically once the store is updated.
     *
     * <p>For example — Palpatine transmits the Search View order:</p>
     * <pre>
     *   user selected "OID", limit=false, schemaLabel=true
     *   → store: LABEL=OID, ABBREVIATE=false, SCHEMA_LABEL_DISPLAY=true
     * </pre>
     *
     * @return  {@code true} always; we write everything to the store successfully
     */
    public boolean performOk()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SearchViewPreferencePage.FirstName" ) ) ) //$NON-NLS-1$
        {
            store
                .setValue( PluginConstants.PREFS_SEARCH_VIEW_LABEL, PluginConstants.PREFS_SEARCH_VIEW_LABEL_FIRST_NAME );
        }
        else if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SearchViewPreferencePage.AllAliases" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SEARCH_VIEW_LABEL,
                PluginConstants.PREFS_SEARCH_VIEW_LABEL_ALL_ALIASES );
        }
        else if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SearchViewPreferencePage.OID" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SEARCH_VIEW_LABEL, PluginConstants.PREFS_SEARCH_VIEW_LABEL_OID );
        }
        store.setValue( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE, limitButton.getSelection() );
        store.setValue( PluginConstants.PREFS_SEARCH_VIEW_ABBREVIATE_MAX_LENGTH, lengthText.getText() );

        store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_DISPLAY, secondaryLabelButtonDisplay
            .getSelection() );
        if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SearchViewPreferencePage.FirstName" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_SEARCH_VIEW_LABEL_FIRST_NAME );
        }
        else if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SearchViewPreferencePage.AllAliases" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_SEARCH_VIEW_LABEL_ALL_ALIASES );
        }
        else if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SearchViewPreferencePage.OID" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_SEARCH_VIEW_LABEL_OID );
        }
        store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE, secondaryLabelLimitButton
            .getSelection() );
        store.setValue( PluginConstants.PREFS_SEARCH_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH,
            secondaryLabelLengthText.getText() );

        store
            .setValue( PluginConstants.PREFS_SEARCH_VIEW_SCHEMA_LABEL_DISPLAY, schemaLabelButtonDisplay.getSelection() );

        return true;
    }


    // ── PALPATINE'S SEARCH VIEW COMMAND ROOM NEEDS NO WORKBENCH BRIEFING ─────
    // Palpatine doesn't need anything from the workbench to configure the
    // Search View. Everything this page requires comes directly from the
    // plugin's preference store.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench initialises this preference page;
     * we have nothing to do here because all state comes from the preference
     * store, not from the workbench instance.
     *
     * @param workbench  the current Eclipse workbench instance; not used here
     */
    public void init( IWorkbench workbench )
    {
        // Nothing to do
    }
}
