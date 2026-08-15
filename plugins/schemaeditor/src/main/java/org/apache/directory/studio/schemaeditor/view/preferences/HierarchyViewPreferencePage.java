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


// ── CLASS: HierarchyViewPreferencePage — PALPATINE ISSUING ORDER 66 ──────────
// In his Senate office, Palpatine calmly surveys the holocomm panel before him,
// each dial and control precisely positioned. When he is ready, he issues
// Order 66 — a precise, deliberate directive that changes how every clone
// trooper in the galaxy behaves, instantly and permanently.
// This preference page works the same way: the administrator adjusts the
// controls (label format, length limits, secondary label settings) and when
// they click Apply, every one of those directives is written to the preference
// store and the Hierarchy View reconfigures itself accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse preference page that controls how nodes are labelled in the
 * Hierarchy View — which name format to show (first alias, all aliases, or
 * OID), whether to truncate long labels, and whether to display a secondary
 * label beneath each node.
 * It extends {@link PreferencePage} and implements
 * {@link IWorkbenchPreferencePage} so Eclipse registers it under
 * Window → Preferences → Schema Editor → Hierarchy View.
 * Think of this as Palpatine's control panel: every setting here is an Order
 * that the Hierarchy View executes the moment the administrator clicks OK.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HierarchyViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    /** The preference page ID */
    public static final String ID = PluginConstants.PREF_PAGE_HIERARCHY_VIEW_ID;

    // UI fields
    private Combo labelCombo;
    private Button limitButton;
    private Text lengthText;
    private Button secondaryLabelButtonDisplay;
    private Combo secondaryLabelCombo;
    private Button secondaryLabelLimitButton;
    private Text secondaryLabelLengthText;


    // ── PALPATINE TAKES HIS SEAT AND OPENS THE BRIEFING ──────────────────────
    // Palpatine settles into his Senate chair, the blue glow of the control
    // panel illuminating his face. Before he issues any orders, he first
    // attaches himself to the right communication channel — in our case, the
    // plugin's preference store — and sets the description that will appear
    // at the top of the preference panel.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link HierarchyViewPreferencePage} and wires it to the
     * plugin's preference store.
     * Eclipse instantiates this via the preferences extension point; we call
     * {@code super()} to initialise the JFace machinery, then attach our store
     * and set the human-readable description shown at the top of the page.
     *
     * <p>For example — Palpatine opens the comm channel before issuing orders:</p>
     * <pre>
     *   new HierarchyViewPreferencePage()
     *   // page wired to plugin preference store
     *   // description: "General settings for the Hierarchy View."
     * </pre>
     */
    public HierarchyViewPreferencePage()
    {
        super();
        super.setPreferenceStore( Activator.getDefault().getPreferenceStore() );
        super.setDescription( Messages.getString( "HierarchyViewPreferencePage.GeneralSettings" ) ); //$NON-NLS-1$
    }


    // ── PALPATINE ARRANGES THE CONTROL PANELS IN THE WAR ROOM ────────────────
    // Before issuing Order 66, Palpatine has his aides lay out every control
    // panel in the room: primary comm channel, secondary channel, truncation
    // dials — each labelled and positioned exactly where his fingers will reach.
    // We build the preference page UI here: primary label group, abbreviation
    // controls, secondary label group, and their respective length inputs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the preference page UI inside the provided parent composite.
     * Eclipse calls this when the user navigates to this preference page in
     * the Preferences dialog; we construct two groups of controls (primary
     * label and secondary label), wire up verify listeners to keep the length
     * fields numeric-only, load current values, and attach change listeners.
     *
     * <p>For example — Palpatine's war room is prepared:</p>
     * <pre>
     *   Label Group:
     *     Use [First Name | All Aliases | OID] as label
     *     [ ] Limit label length to [___] characters
     *   Secondary Label Group:
     *     [ ] Display secondary label
     *     Use [First Name | All Aliases | OID] as secondary label
     *     [ ] Limit secondary label length to [___] characters
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
        labelGroup.setText( Messages.getString( "HierarchyViewPreferencePage.Label" ) ); //$NON-NLS-1$
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
        useLabel.setText( Messages.getString( "HierarchyViewPreferencePage.Use" ) ); //$NON-NLS-1$

        // Label Combo
        labelCombo = new Combo( labelComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        labelCombo.setLayoutData( new GridData() );
        labelCombo
            .setItems( new String[]
                {
                    Messages.getString( "HierarchyViewPreferencePage.FirstName" ), Messages.getString( "HierarchyViewPreferencePage.AllAliases" ), Messages.getString( "HierarchyViewPreferencePage.OID" ) } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        labelCombo.setEnabled( true );

        // As label Label
        Label asLabel = new Label( labelComposite, SWT.NONE );
        asLabel.setText( Messages.getString( "HierarchyViewPreferencePage.AsLabel" ) ); //$NON-NLS-1$

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
        limitButton.setText( Messages.getString( "HierarchyViewPreferencePage.LimitLabel" ) ); //$NON-NLS-1$
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
        charactersLabel.setText( Messages.getString( "HierarchyViewPreferencePage.Characters" ) ); //$NON-NLS-1$

        // Secondary Label Group
        Group secondaryLabelGroup = new Group( composite, SWT.NONE );
        secondaryLabelGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        secondaryLabelGroup.setText( Messages.getString( "HierarchyViewPreferencePage.SecondaryLabel" ) ); //$NON-NLS-1$
        secondaryLabelGroup.setLayout( new GridLayout() );
        Composite secondaryLabelGroupComposite = new Composite( secondaryLabelGroup, SWT.NONE );
        gl = new GridLayout( 1, false );
        gl.marginHeight = gl.marginWidth = 0;
        secondaryLabelGroupComposite.setLayout( gl );
        secondaryLabelGroupComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        secondaryLabelButtonDisplay = new Button( secondaryLabelGroupComposite, SWT.CHECK );
        secondaryLabelButtonDisplay.setText( Messages.getString( "HierarchyViewPreferencePage.DisplaySecondaryLabel" ) ); //$NON-NLS-1$

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
        useLabel2.setText( Messages.getString( "HierarchyViewPreferencePage.Use" ) ); //$NON-NLS-1$

        // Label Combo
        secondaryLabelCombo = new Combo( secondaryLabelComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
        secondaryLabelCombo.setLayoutData( new GridData() );
        secondaryLabelCombo
            .setItems( new String[]
                {
                    Messages.getString( "HierarchyViewPreferencePage.FirstName" ), Messages.getString( "HierarchyViewPreferencePage.AllAliases" ), Messages.getString( "HierarchyViewPreferencePage.OID" ) } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        secondaryLabelCombo.setEnabled( true );

        // As label Label
        Label asLabel2 = new Label( secondaryLabelComposite, SWT.NONE );
        asLabel2.setText( Messages.getString( "HierarchyViewPreferencePage.AsSecondaryLabel" ) ); //$NON-NLS-1$

        // Abbreviate row composite
        Composite abbreviateComposite2 = new Composite( secondaryLabelGroup, SWT.NONE );
        gl = new GridLayout( 3, false );
        gl.marginHeight = gl.marginWidth = 0;
        abbreviateComposite2.setLayout( gl );
        gd = new GridData( SWT.FILL, SWT.NONE, true, false );
        gd.horizontalSpan = 1;
        abbreviateComposite2.setLayoutData( gd );

        // Limit label length to Label
        secondaryLabelLimitButton = new Button( abbreviateComposite2, SWT.CHECK );
        secondaryLabelLimitButton.setText( Messages.getString( "HierarchyViewPreferencePage.LimitSecondaryLabel" ) ); //$NON-NLS-1$
        gd = new GridData();
        gd.horizontalSpan = 1;
        secondaryLabelLimitButton.setLayoutData( gd );

        // Length Text
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
        secondaryLabelcharactersLabel.setText( Messages.getString( "HierarchyViewPreferencePage.Characters" ) ); //$NON-NLS-1$

        initFieldsFromPreferences();

        initListeners();

        applyDialogFont( parent );

        return parent;
    }


    // ── PALPATINE READS THE CURRENT INTELLIGENCE BRIEFING ────────────────────
    // Before issuing new orders, Palpatine reviews the existing disposition of
    // his clone troopers — where they are, what they are already doing — so
    // he can issue precise adjustments rather than starting from scratch.
    // We read the current preference values from the store and use them to
    // populate every widget on the page before the administrator sees it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all Hierarchy View preferences from the plugin's preference store
     * and pushes the values into the UI controls so the page opens in a state
     * that accurately reflects what is currently configured.
     * Without this, every control would be at its default (blank or unchecked)
     * regardless of what the user had previously saved.
     *
     * <p>For example — Palpatine reviews the current clone deployment:</p>
     * <pre>
     *   store says: label = FIRST_NAME, abbreviate = true, maxLength = 20
     *   → labelCombo selects index 0, limitButton checked, lengthText = "20"
     * </pre>
     */
    private void initFieldsFromPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        labelCombo.select( store.getInt( PluginConstants.PREFS_HIERARCHY_VIEW_LABEL ) );
        limitButton.setSelection( store.getBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE ) );
        lengthText.setEnabled( limitButton.getSelection() );
        lengthText.setText( store.getString( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE_MAX_LENGTH ) );

        secondaryLabelButtonDisplay.setSelection( store
            .getBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_DISPLAY ) );
        secondaryLabelCombo.select( store.getInt( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL ) );
        secondaryLabelLimitButton.setSelection( store
            .getBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE ) );
        secondaryLabelLengthText.setText( store
            .getString( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH ) );
        if ( store.getBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_DISPLAY ) )
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


    // ── PALPATINE INSTALLS HIS AGENTS IN THE SENATE ──────────────────────────
    // Palpatine places loyal agents at every critical node in the Senate — eyes
    // and ears that report back the moment anything changes so he can respond
    // with a new directive.
    // We attach SWT selection listeners to the checkboxes so that enabling or
    // disabling one control cascades to dependent controls immediately, without
    // the user having to click Apply first.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches selection listeners to the checkbox buttons so they can enable
     * or disable dependent widgets in real time as the user interacts with
     * the preference page.
     * For example, when the user checks "Limit label length," the length text
     * field becomes editable immediately; unchecking it greys the field out.
     *
     * <p>For example — Palpatine's agents report and he responds:</p>
     * <pre>
     *   limitButton checked   → lengthText enabled
     *   limitButton unchecked → lengthText disabled
     *   secondaryLabelButtonDisplay unchecked
     *     → secondaryLabelCombo, secondaryLabelLimitButton, secondaryLabelLengthText all disabled
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


    // ── PALPATINE RECALLS THE BASE DEPLOYMENT PLAN ───────────────────────────
    // When new information suggests the current strategy is flawed, Palpatine
    // reverts every clone to the base deployment plan — the factory defaults
    // that were in place before any custom directives were issued.
    // We do the same: reset all UI controls to the default values baked into
    // the plugin's preference store defaults.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets all UI controls to the plugin's default preference values when
     * the user clicks the "Restore Defaults" button.
     * We read defaults from the preference store (the values set in the
     * plugin's initialiser) rather than hard-coding them here, so defaults
     * stay in one place.
     * We also update dependent widget enablement so the page is consistent
     * after the reset, then delegate to the parent class to handle anything
     * else JFace needs.
     *
     * <p>For example — Palpatine reverts to the base plan:</p>
     * <pre>
     *   Default: label = FIRST_NAME, no abbreviation, no secondary label
     *   → All controls snap back to that state when user clicks "Restore Defaults"
     * </pre>
     */
    protected void performDefaults()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        labelCombo.select( store.getDefaultInt( PluginConstants.PREFS_HIERARCHY_VIEW_LABEL ) );
        limitButton.setSelection( store.getDefaultBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE ) );
        lengthText.setEnabled( limitButton.getSelection() );
        lengthText.setText( store.getDefaultString( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE_MAX_LENGTH ) );

        secondaryLabelButtonDisplay.setSelection( store
            .getDefaultBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_DISPLAY ) );
        secondaryLabelCombo.select( store.getDefaultInt( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL ) );
        secondaryLabelLimitButton.setSelection( store
            .getDefaultBoolean( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE ) );
        secondaryLabelLengthText.setText( store
            .getDefaultString( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH ) );

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

        super.performDefaults();
    }


    // ── PALPATINE TRANSMITS ORDER 66 ─────────────────────────────────────────
    // "Execute Order 66." Palpatine activates the holoprojecter, and in an
    // instant every directive he has set on the control panel is transmitted
    // to every clone trooper in the galaxy — the label format, the length
    // limits, the secondary label configuration — all written permanently into
    // the preference store.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists all the current UI control values to the plugin's preference
     * store when the user clicks OK or Apply.
     * We translate the human-readable combo selections back to their integer
     * constant equivalents and write everything to the store in one pass.
     * The Hierarchy View listens for preference-change events and will refresh
     * its labels automatically once the store is updated.
     *
     * <p>For example — Palpatine transmits the order:</p>
     * <pre>
     *   user selected "First Name" → store.setValue(PREFS_HIERARCHY_VIEW_LABEL, FIRST_NAME)
     *   limitButton checked, length = "25" → store abbreviate=true, maxLength="25"
     * </pre>
     *
     * @return  {@code true} always; we write everything successfully or not at all
     */
    public boolean performOk()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "HierarchyViewPreferencePage.FirstName" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_LABEL,
                PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_FIRST_NAME );
        }
        else if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "HierarchyViewPreferencePage.AllAliases" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_LABEL,
                PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_ALL_ALIASES );
        }
        else if ( labelCombo.getItem( labelCombo.getSelectionIndex() ).equals(
            Messages.getString( "HierarchyViewPreferencePage.OID" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_LABEL, PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_OID );
        }
        store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE, limitButton.getSelection() );
        store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE_MAX_LENGTH, lengthText.getText() );

        store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_DISPLAY, secondaryLabelButtonDisplay
            .getSelection() );
        if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "HierarchyViewPreferencePage.FirstName" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_FIRST_NAME );
        }
        else if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "HierarchyViewPreferencePage.AllAliases" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_ALL_ALIASES );
        }
        else if ( secondaryLabelCombo.getItem( secondaryLabelCombo.getSelectionIndex() ).equals(
            Messages.getString( "HierarchyViewPreferencePage.OID" ) ) ) //$NON-NLS-1$
        {
            store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL,
                PluginConstants.PREFS_HIERARCHY_VIEW_LABEL_OID );
        }
        store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE, secondaryLabelLimitButton
            .getSelection() );
        store.setValue( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH,
            secondaryLabelLengthText.getText() );

        return true;
    }


    // ── PALPATINE ENTERS THE CHAMBER — NOTHING TO PREPARE ────────────────────
    // Palpatine strides into the Senate chamber with calm authority — he needs
    // no briefing from the workbench itself because this preference page draws
    // everything it needs directly from the preference store.
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
    }
}
