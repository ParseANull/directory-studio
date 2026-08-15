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


// ── CLASS: SchemaViewPreferencePage — PALPATINE ISSUING ORDER 66 ─────────────
// Palpatine stands before his holographic command interface, each clone
// trooper's channel open and waiting. He dials in the exact configuration he
// wants — label format, length limits, secondary labels, schema name visibility
// — and when he presses the final control every trooper in the Schema View
// executes the directive simultaneously.
// This preference page works the same way: the administrator configures how
// the Schema View should label its nodes, and when they click Apply every
// setting is written to the preference store and the view updates accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse preference page that controls how schema elements are labelled
 * in the Schema View — label format (first alias, all aliases, or OID),
 * maximum label length, secondary label options, and whether to display
 * the schema name alongside each element.
 * It extends {@link PreferencePage} and implements
 * {@link IWorkbenchPreferencePage} so Eclipse wires it into
 * Window → Preferences → Schema Editor → Schema View.
 * Think of this as Palpatine's personal command panel: every knob and dial
 * here is an Order that the Schema View will execute the moment Apply is pressed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    /** The preference page ID */
    public static final String ID = PluginConstants.PREF_PAGE_SCHEMA_VIEW_ID;

    // UI fields
    private Combo labelCombo;
    private Button limitButton;
    private Text lengthText;
    private Button secondaryLabelButtonDisplay;
    private Combo secondaryLabelCombo;
    private Button secondaryLabelLimitButton;
    private Text secondaryLabelLengthText;
    private Button schemaLabelButtonDisplay;


    // ── PALPATINE ACTIVATES THE SCHEMA VIEW COMMAND CHANNEL ──────────────────
    // Palpatine opens a dedicated channel specifically for Schema View orders —
    // a separate frequency from the Hierarchy View, calibrated for a different
    // battalion of clone troopers.
    // We wire this preference page to the plugin's preference store and set the
    // page description text that appears at the top of the preference panel.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link SchemaViewPreferencePage} and connects it to the
     * plugin's preference store.
     * Eclipse instantiates this via the preferences extension point; we call
     * {@code super()} to initialise the JFace preference-page machinery, then
     * attach the store and write the description shown at the top of the page.
     *
     * <p>For example — Palpatine opens the Schema View command channel:</p>
     * <pre>
     *   new SchemaViewPreferencePage()
     *   // wired to plugin preference store
     *   // description: "General settings for the Schema View."
     * </pre>
     */
    public SchemaViewPreferencePage()
    {
        super();
        setPreferenceStore( Activator.getDefault().getPreferenceStore() );
        setDescription( Messages.getString( "SchemaViewPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── PALPATINE LAYS OUT THE SCHEMA VIEW CONTROL PANELS ────────────────────
    // Palpatine has three sets of controls arranged in front of him for the
    // Schema View battalion: primary label dials, secondary label dials, and
    // a schema-name visibility toggle. Each is clearly labelled and positioned
    // so his fingers never reach for the wrong lever.
    // We build those three groups as SWT composites with their associated
    // combos, checkboxes, and length text fields.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page UI inside the provided parent composite.
     * Eclipse calls this when the user navigates to this preference page; we
     * construct three groups of controls: primary label, secondary label, and
     * schema-label visibility, then load current values and wire up listeners.
     *
     * <p>For example — Palpatine's Schema View war room is prepared:</p>
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
        labelGroup.setText( Messages.getString( "SchemaViewPreferencePage.Label" ) ); //$NON-NLS-1$
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
        useLabel.setText( Messages.getString( "SchemaViewPreferencePage.Use" ) ); //$NON-NLS-1$

        // Label Combo
        labelCombo = new Combo( labelComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        labelCombo.setLayoutData( new GridData() );
        labelCombo
            .setItems( new String[]
                {
                    Messages.getString( "SchemaViewPreferencePage.FirstName" ), Messages.getString( "SchemaViewPreferencePage.AllAliases" ), Messages.getString( "SchemaViewPreferencePage.OID" ) } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        labelCombo.setEnabled( true );

        // As label Label
        Label asLabel = new Label( labelComposite, SWT.NONE );
        asLabel.setText( Messages.getString( "SchemaViewPreferencePage.AsLabel" ) ); //$NON-NLS-1$

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
        limitButton.setText( Messages.getString( "SchemaViewPreferencePage.LimitLabel" ) ); //$NON-NLS-1$
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
        charactersLabel.setText( Messages.getString( "SchemaViewPreferencePage.Characters" ) ); //$NON-NLS-1$

        // Secondary Label Group
        Group secondaryLabelGroup = new Group( composite, SWT.NONE );
        secondaryLabelGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        secondaryLabelGroup.setText( Messages.getString( "SchemaViewPreferencePage.SecondaryLabel" ) ); //$NON-NLS-1$
        secondaryLabelGroup.setLayout( new GridLayout() );
        Composite secondaryLabelGroupComposite = new Composite( secondaryLabelGroup, SWT.NONE );
        gl = new GridLayout( 1, false );
        gl.marginHeight = gl.marginWidth = 0;
        secondaryLabelGroupComposite.setLayout( gl );
        secondaryLabelGroupComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        secondaryLabelButtonDisplay = new Button( secondaryLabelGroupComposite, SWT.CHECK );
        secondaryLabelButtonDisplay.setText( Messages.getString( "SchemaViewPreferencePage.DisplaySecondaryLabel" ) ); //$NON-NLS-1$

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
        useLabel2.setText( Messages.getString( "SchemaViewPreferencePage.Use" ) ); //$NON-NLS-1$

        // Label Combo
        secondaryLabelCombo = new Combo( secondaryLabelComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        secondaryLabelCombo.setLayoutData( new GridData() );
        secondaryLabelCombo
            .setItems( new String[]
                {
                    Messages.getString( "SchemaViewPreferencePage.FirstName" ), Messages.getString( "SchemaViewPreferencePage.AllAliases" ), Messages.getString( "SchemaViewPreferencePage.OID" ) } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        secondaryLabelCombo.setEnabled( true );

        // As label Label
        Label asLabel2 = new Label( secondaryLabelComposite, SWT.NONE );
        asLabel2.setText( Messages.getString( "SchemaViewPreferencePage.AsSecondaryLabel" ) ); //$NON-NLS-1$

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
        secondaryLabelLimitButton.setText( Messages.getString( "SchemaViewPreferencePage.LimitSecondaryLabel" ) ); //$NON-NLS-1$
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

        // Schema Label Group
        Group schemaLabelGroup = new Group( composite, SWT.NONE );
        schemaLabelGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        schemaLabelGroup.setText( Messages.getString( "SchemaViewPreferencePage.SchemaLabel" ) ); //$NON-NLS-1$
        schemaLabelGroup.setLayout( new GridLayout() );
        Composite schemaLabelGroupComposite = new Composite( schemaLabelGroup, SWT.NONE );
        gl = new GridLayout( 1, false );
        gl.marginHeight = gl.marginWidth = 0;
        schemaLabelGroupComposite.setLayout( gl );
        schemaLabelGroupComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        schemaLabelButtonDisplay = new Button( schemaLabelGroupComposite, SWT.CHECK );
        schemaLabelButtonDisplay.setText( Messages.getString( "SchemaViewPreferencePage.DisplaySchemaLabel" ) ); //$NON-NLS-1$

        // Characters Label
        Label secondaryLabelcharactersLabel = new Label( abbreviateComposite2, SWT.NONE );
        secondaryLabelcharactersLabel.setText( Messages.getString( "SchemaViewPreferencePage.Characters" ) ); //$NON-NLS-1$

        initFieldsFromPreferences();

        initListeners();

        applyDialogFont( parent );

        return parent;
    }


    // ── PALPATINE READS THE SCHEMA VIEW INTELLIGENCE BRIEFING ────────────────
    // Palpatine reviews the current Schema View battalion's status report —
    // exactly which label format they are using, what their length limits are,
    // whether secondary labels are active — before deciding what to change.
    // We read those values from the preference store and populate every UI
    // control so the page accurately reflects the current configuration.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all Schema View preferences from the plugin's preference store and
     * pushes those values into the UI controls so the page opens showing the
     * current configuration rather than blank defaults.
     * We also update dependent widget enablement (e.g. the length text field
     * is disabled when abbreviation is turned off) so the page is consistent
     * from the moment the user first sees it.
     *
     * <p>For example — Palpatine reads the Schema View status report:</p>
     * <pre>
     *   store: label=ALL_ALIASES, abbreviate=false, secondaryDisplay=true
     *   → labelCombo selects index 1, limitButton unchecked (lengthText disabled),
     *     secondaryLabelButtonDisplay checked
     * </pre>
     */
    private void initFieldsFromPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        labelCombo.select( store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_LABEL ) );
        limitButton.setSelection( store.getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE ) );
        lengthText.setEnabled( limitButton.getSelection() );
        lengthText.setText( store.getString( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE_MAX_LENGTH ) );

        secondaryLabelButtonDisplay.setSelection( store
            .getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY ) );
        secondaryLabelCombo.select( store.getInt( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL ) );
        secondaryLabelLimitButton.setSelection( store
            .getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE ) );
        secondaryLabelLengthText.setText( store
            .getString( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH ) );
        if ( store.getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY ) )
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
            .getBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_LABEL_DISPLAY ) );
    }


    // ── PALPATINE ACTIVATES EACH TROOPER'S COMLINK RECEIVER ──────────────────
    // Palpatine configures each clone trooper's helmet comlink to respond the
    // moment it receives a changed directive — no delay, no polling.
    // We attach selection listeners to the checkboxes so dependent widgets
    // enable or disable the instant the user toggles a control.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches selection listeners to the checkbox buttons so they can enable
     * or disable dependent widgets in real time as the user interacts with
     * the preference page.
     * This gives the page a responsive feel: toggling "Display secondary label"
     * immediately greys out or activates the secondary label controls without
     * requiring the user to press Apply first.
     *
     * <p>For example — Palpatine's comlinks activate:</p>
     * <pre>
     *   limitButton checked   → lengthText enabled
     *   secondaryLabelButtonDisplay unchecked
     *     → secondaryLabelCombo, secondaryLabelLimitButton, secondaryLabelLengthText disabled
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


    // ── PALPATINE RECALLS THE BASE SCHEMA VIEW PLAN ──────────────────────────
    // When Palpatine determines the Schema View battalion has drifted too far
    // from optimal configuration, he recalls every trooper to the base plan —
    // the factory defaults that were established when the plugin was first
    // installed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets all UI controls to the plugin's default Schema View preference
     * values when the user clicks "Restore Defaults."
     * We read defaults from the preference store rather than hard-coding them
     * here, then update dependent widget enablement so the page looks correct
     * after the reset, and delegate to the parent for any remaining JFace work.
     *
     * <p>For example — Palpatine recalls the Schema View base plan:</p>
     * <pre>
     *   Default: label=FIRST_NAME, no abbreviation, no secondary label,
     *            schema label hidden
     *   → All controls snap back when user clicks "Restore Defaults"
     * </pre>
     */
    protected void performDefaults()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        labelCombo.select( store.getDefaultInt( PluginConstants.PREFS_SCHEMA_VIEW_LABEL ) );
        limitButton.setSelection( store.getDefaultBoolean( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE ) );
        lengthText.setEnabled( limitButton.getSelection() );
        lengthText.setText( store.getDefaultString( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE_MAX_LENGTH ) );

        secondaryLabelButtonDisplay.setSelection( store
            .getDefaultBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY ) );
        secondaryLabelCombo.select( store.getDefaultInt( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL ) );
        secondaryLabelLimitButton.setSelection( store
            .getDefaultBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE ) );
        secondaryLabelLengthText.setText( store
            .getDefaultString( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH ) );

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
            .getDefaultBoolean( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_LABEL_DISPLAY ) );

        super.performDefaults();
    }


    // ── PALPATINE TRANSMITS THE SCHEMA VIEW ORDERS ───────────────────────────
    // "It is done, then." Palpatine closes the comm channel and the Schema View
    // battalion executes: label format locked in, abbreviation rules active,
    // secondary label and schema name display configured exactly as specified.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists all current UI control values to the plugin's preference store
     * when the user clicks OK or Apply.
     * We translate combo selections back to their integer preference constants
     * and write everything — label format, abbreviation, secondary label
     * settings, schema label visibility — in one pass.
     * The Schema View listens for preference change events and will refresh
     * its labels automatically once these values are committed.
     *
     * <p>For example — Palpatine transmits the Schema View order:</p>
     * <pre>
     *   user selected "All Aliases", limit=true, length="30", schemaLabel=true
     *   → store: LABEL=ALL_ALIASES, ABBREVIATE=true, MAX_LENGTH="30",
     *            SCHEMA_LABEL_DISPLAY=true
     * </pre>
     *
     * @return  {@code true} always; we write everything to the store successfully
     */
    public boolean performOk()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SchemaViewPreferencePage.FirstName" ) ) ) //$NON-NLS-1$
        {
            store
                .setValue( PluginConstants.PREFS_SCHEMA_VIEW_LABEL, PluginConstants.PREFS_SCHEMA_VIEW_LABEL_FIRST_NAME );
        }
        else if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SchemaViewPreferencePage.AllAliases" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_LABEL,
                PluginConstants.PREFS_SCHEMA_VIEW_LABEL_ALL_ALIASES );
        }
        else if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SchemaViewPreferencePage.OID" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_LABEL, PluginConstants.PREFS_SCHEMA_VIEW_LABEL_OID );
        }
        store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE, limitButton.getSelection() );
        store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE_MAX_LENGTH, lengthText.getText() );

        store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY, secondaryLabelButtonDisplay
            .getSelection() );
        if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SchemaViewPreferencePage.FirstName" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_SCHEMA_VIEW_LABEL_FIRST_NAME );
        }
        else if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SchemaViewPreferencePage.AllAliases" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_SCHEMA_VIEW_LABEL_ALL_ALIASES );
        }
        else if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "SchemaViewPreferencePage.OID" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_SCHEMA_VIEW_LABEL_OID );
        }
        store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE, secondaryLabelLimitButton
            .getSelection() );
        store.setValue( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH,
            secondaryLabelLengthText.getText() );

        store
            .setValue( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_LABEL_DISPLAY, schemaLabelButtonDisplay.getSelection() );

        return true;
    }


    // ── PALPATINE'S WAR ROOM IS READY — NOTHING FROM THE WORKBENCH NEEDED ────
    // Palpatine doesn't need a briefing from the workbench; everything this
    // page requires comes directly from the preference store and the plugin
    // registry.
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
