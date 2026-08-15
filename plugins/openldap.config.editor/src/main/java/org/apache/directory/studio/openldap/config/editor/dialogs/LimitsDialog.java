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
package org.apache.directory.studio.openldap.config.editor.dialogs;


import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.TableWidget;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.apache.directory.studio.ldapbrowser.core.utils.SchemaObjectLoader;
import org.apache.directory.studio.openldap.common.ui.model.DnSpecStyleEnum;
import org.apache.directory.studio.openldap.common.ui.model.DnSpecTypeEnum;
import org.apache.directory.studio.openldap.common.ui.model.LimitSelectorEnum;
import org.apache.directory.studio.openldap.config.editor.wrappers.LimitDecorator;
import org.apache.directory.studio.openldap.config.editor.wrappers.LimitWrapper;
import org.apache.directory.studio.openldap.config.editor.wrappers.LimitsWrapper;


// Like Princess Leia transmitting the full Rebellion access policy —
// who gets in, what they can do, and how much they can ask for —
// we project this dialog so the operator can define selector rules
// and attach a list of specific limits to each one.
/**
 * A dialog for editing an OpenLDAP "limits" configuration entry.
 * We let the operator choose a selector (Any, Anonymous, Users, DN, or Group)
 * and attach a list of size/time limit entries to it. A read-only text
 * field shows the resulting limits string as edits happen.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +-------------------------------------------------------+
 * | Limits                                                |
 * | .---------------------------------------------------. |
 * | | (o) Any                                           | |
 * | | (o) Anonymous                                     | |
 * | | (o) Users                                         | |
 * | |            .------------------------------------. | |
 * | |            | Type :    [--------------------|v] | | |
 * | | (o) DN     | Style :   [--------------------|v] | | |
 * | |            | Pattern : [----------------------] | | |
 * | |            '------------------------------------' | |
 * | |            .------------------------------------. | |
 * | |            | ObjectClass :   [--------------|v] | | |
 * | | (o) Group  | AttributeType : [--------------|v] | | |
 * | |            | Pattern :       [----------------] | | |
 * | |            '------------------------------------' | |
 * | |                                                   | |
 * | | Limits :                                          | |
 * | | +-------------------------------------+           | |
 * | | |{1}defxyz12                          | (Add...)  | |
 * | | |{2}aaa                               | (Edit...) | |
 * | | |                                     | (Delete)  | |
 * | | |                                     | --------- | |
 * | | |                                     | (Up...)   | |
 * | | |                                     | (Down...) | |
 * | | +-------------------------------------+           | |
 * | '---------------------------------------------------' |
 * | Resulting Limits                                      |
 * | .---------------------------------------------------. |
 * | | <///////////////////////////////////////////////> | |
 * | '---------------------------------------------------' |
 * |                                                       |
 * |  (Cancel)                                      (OK)   |
 * +-------------------------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LimitsDialog extends AddEditDialog<LimitsWrapper>
{
    /** The Any radio button */
    private Button anyButton;

    /** The Anonymous radio button */
    private Button anonymousButton;

    /** The Users radio button */
    private Button usersButton;

    /** The DNSpec radio button */
    private Button dnSpecButton;

    /** The DNSpec type */
    private Combo dnSpecTypeCombo;

    /** The DNSpec style */
    private Combo dnSpecStyleCombo;

    /** The DNSpec pattern */
    private Text dnSpecPatternText;

    /** The Group radio button */
    private Button groupButton;

    /** The Group ObjectClass type */
    private Combo groupObjectClassCombo;

    /** The Group AttributeType style */
    private Combo groupAttributeTypeCombo;

    /** The Group pattern */
    private Text groupPatternText;

    /** The (time/size)Limit parameter */
    private TableWidget<LimitWrapper> limitsTableWidget;

    /** The resulting Limits Text */
    private Text limitsText;

    /** The Attribute list loader */
    private SchemaObjectLoader schemaObjectLoader;

    // Like cutting the power to DN and Group controls when the operator
    // selects Any, Anonymous, or Users — those selectors don't need
    // extra detail — we disable all the dnSpec and group widgets so
    // the operator doesn't get confused by irrelevant fields.
    /**
     * Disables all the DN spec and Group sub-controls so they can't
     * be interacted with when a simple selector (Any/Anonymous/Users)
     * is chosen.
     */
    private void disableDnSpecGroupButtons()
    {
        dnSpecTypeCombo.setEnabled( false );
        dnSpecStyleCombo.setEnabled( false );
        dnSpecPatternText.setEnabled( false );
        groupAttributeTypeCombo.setEnabled( false );
        groupObjectClassCombo.setEnabled( false );
        groupPatternText.setEnabled( false );
    }


    // Like the Rebellion selectively powering up either the DN targeting
    // system or the group identification scanner depending on which
    // selector the operator chose, we enable the right sub-controls
    // and disable the wrong ones in one coordinated call.
    /**
     * Enables or disables the DN spec and Group sub-controls independently,
     * based on which selector the operator is working with.
     *
     * @param dnSpecStatus {@code true} to enable the DN spec controls, {@code false} to disable them
     * @param groupStatus {@code true} to enable the group controls, {@code false} to disable them
     */
    private void setDnSpecGroupButtons( boolean dnSpecStatus, boolean groupStatus )
    {
        groupAttributeTypeCombo.setEnabled( groupStatus );
        groupObjectClassCombo.setEnabled( groupStatus );
        groupPatternText.setEnabled( groupStatus );
        dnSpecTypeCombo.setEnabled( dnSpecStatus );
        dnSpecStyleCombo.setEnabled( dnSpecStatus );
        dnSpecPatternText.setEnabled( dnSpecStatus  );
    }


    // Like resetting the targeting computer before switching to a different
    // selector type so old selector data doesn't contaminate the new one,
    // we null out the dnSpec fields, objectClass, attributeType, and
    // pattern on the edited element in one clean sweep.
    /**
     * Clears all selector-specific fields on the edited element —
     * DN spec style, type, pattern, object class, attribute type —
     * so stale data from a previous selector doesn't bleed through.
     */
    private void clearEditedElement()
    {
        getEditedElement().setDnSpecStyle( null );
        getEditedElement().setDnSpecType( null );
        getEditedElement().setAttributeType( null );
        getEditedElement().setObjectClass( null );
        getEditedElement().setSelectorPattern( null );
    }


    /**
     * Listeners for the Selector radioButtons. It will enable or disable the dnSpec or Group accordingly
     * to the selection.
     */
    private SelectionListener selectorButtonsSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            if ( event.getSource() instanceof Button )
            {
                Button button = (Button)event.getSource();

                if ( button == anyButton )
                {
                    if ( button.getSelection() )
                    {
                        disableDnSpecGroupButtons();
                        clearEditedElement();
                        getEditedElement().setSelector( LimitSelectorEnum.ANY );
                        limitsText.setText( getEditedElement().toString() );
                    }
                }
                else if ( button == anonymousButton )
                {
                    if ( button.getSelection() )
                    {
                        disableDnSpecGroupButtons();
                        clearEditedElement();
                        getEditedElement().setSelector( LimitSelectorEnum.ANONYMOUS );
                        limitsText.setText( getEditedElement().toString() );
                    }
                }
                else if ( button == usersButton )
                {
                    if ( button.getSelection() )
                    {
                        disableDnSpecGroupButtons();
                        clearEditedElement();
                        getEditedElement().setSelector( LimitSelectorEnum.USERS );
                        limitsText.setText( getEditedElement().toString() );
                    }
                }
                else if ( button == dnSpecButton )
                {
                    setDnSpecGroupButtons( dnSpecButton.getSelection(), false );
                    clearEditedElement();
                    getEditedElement().setSelector( LimitSelectorEnum.DNSPEC );
                    limitsText.setText( getEditedElement().toString() );
                }
                else if ( button == groupButton )
                {
                    setDnSpecGroupButtons( false, groupButton.getSelection() );
                    clearEditedElement();
                    getEditedElement().setSelector( LimitSelectorEnum.GROUP );
                    limitsText.setText( getEditedElement().toString() );
                }
            }
        }
    };


    /**
     * The dnSpecTypeCombo listener
     */
    private SelectionListener dnSpecTypeComboListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            getEditedElement().setDnSpecType( DnSpecTypeEnum.getType( dnSpecTypeCombo.getText() ) );
            limitsText.setText( getEditedElement().toString() );
        }
    };


    /**
     * The dnSpecTypeCombo listener
     */
    private SelectionListener dnSpecStyleComboListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            getEditedElement().setDnSpecStyle( DnSpecStyleEnum.getStyle( dnSpecStyleCombo.getText() ) );
            limitsText.setText( getEditedElement().toString() );
        }
    };


    /**
     * The groupAttributeTypeCombo listener
     */
    private SelectionListener groupAttributeTypeComboListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            getEditedElement().setAttributeType( groupAttributeTypeCombo.getText() );
            limitsText.setText( getEditedElement().toString() );
        }
    };


    /**
     * The groupObjectClassCombo listener
     */
    private SelectionListener groupObjectClassComboListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            getEditedElement().setObjectClass( groupObjectClassCombo.getText() );
            limitsText.setText( getEditedElement().toString() );
        }
    };

    /**
     * The dnSpecPatternText and groupPatternText listener
     */
    private ModifyListener patternTextListener = event ->
        {
            if ( event.getSource() == dnSpecPatternText )
            {
                getEditedElement().setSelectorPattern( dnSpecPatternText.getText() );
            }
            else
            {
                getEditedElement().setSelectorPattern( groupPatternText.getText() );
            }

            limitsText.setText( getEditedElement().toString() );
        };


    /**
     * The olcLimits listener
     */
    private WidgetModifyListener limitsTableWidgetListener = event ->
        {
            getEditedElement().setLimits( limitsTableWidget.getElements() );
            limitsText.setText( getEditedElement().toString() );
        };


    // Like Leia opening a fresh hologram channel with a resizable frame
    // and a schema loader ready to populate the combo boxes, we create
    // the dialog with the RESIZE style and initialize the schema
    // object loader for attribute/objectclass lookups.
    /**
     * Creates a new LimitsDialog attached to the given parent shell.
     * We initialize the schema object loader so the group combos can
     * be populated with known attribute names and object class names.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public LimitsDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        schemaObjectLoader = new SchemaObjectLoader();
    }


    // Like Leia pre-loading an existing mission briefing into the hologram
    // before opening it for review, we parse the provided limits string
    // and set it as the edited element so the dialog opens in the right state.
    /**
     * Creates a new LimitsDialog pre-populated with the given limits string.
     * We parse the string into a {@link LimitsWrapper} and set it as the
     * starting edited element.
     *
     * @param parentShell the parent shell this dialog belongs to
     * @param limitsStr the existing limits configuration string to pre-load
     */
    public LimitsDialog( Shell parentShell, String limitsStr )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        schemaObjectLoader = new SchemaObjectLoader();

        setEditedElement( new LimitsWrapper( limitsStr ) );
    }


    // Like labeling the hologram projector so everyone knows this is
    // the "Limits" briefing, we stamp the dialog shell with the
    // "Limits" title before the window appears to the operator.
    /**
     * Configures the dialog shell by setting its title to "Limits".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Limits" );
    }


    // Like Leia's hologram materializing with two clear sections — the
    // editable mission parameters up top and the computed result below —
    // we build the dialog area with a limits input group and a read-only
    // resulting limits display, then initialize and wire everything up.
    /**
     * Builds the full dialog content area with a limits-input group
     * (selector radio buttons, DN/Group sub-controls, and limit table)
     * and a read-only resulting-limits text field below.
     *
     * <pre>
     * +-------------------------------------------------------+
     * | Limits                                                |
     * | .---------------------------------------------------. |
     * | | (o) Any                                           | |
     * | | (o) Anonymous                                     | |
     * | | (o) Users                                         | |
     * | |            .------------------------------------. | |
     * | |            | Type :    [--------------------|v] | | |
     * | | (o) DN     | Style :   [--------------------|v] | | |
     * | |            | Pattern : [----------------------] | | |
     * | |            '------------------------------------' | |
     * | |            .------------------------------------. | |
     * | |            | ObjectClass :   [--------------|v] | | |
     * | | (o) Group  | AttributeType : [--------------|v] | | |
     * | |            | Pattern :       [----------------] | | |
     * | |            '------------------------------------' | |
     * | |                                                   | |
     * | | Limits :                                          | |
     * | | +-------------------------------------+           | |
     * | | |{1}defxyz12                          | (Add...)  | |
     * | | |{2}aaa                               | (Edit...) | |
     * | | |                                     | (Delete)  | |
     * | | |                                     | --------- | |
     * | | |                                     | (Up...)   | |
     * | | |                                     | (Down...) | |
     * | | +-------------------------------------+           | |
     * | '---------------------------------------------------' |
     * | Resulting Limits                                      |
     * | .---------------------------------------------------. |
     * | | Limits : <//////////////////////////////////////> | |
     * | '---------------------------------------------------' |
     * |                                                       |
     * |  (Cancel)                                      (OK)   |
     * +-------------------------------------------------------+
     * </pre>
     *
     * @param parent the parent composite to build our content inside
     * @return the fully assembled dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );


        createLimitsEditGroup( composite );
        createLimitsShowGroup( composite );

        initDialog();
        addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // Like assembling the full tactical operations panel — selector
    // radio buttons, DN detail controls, group detail controls, and
    // the orderable limits list — we build the input group here so
    // the operator has everything they need in one panel.
    /**
     * Builds the limits input group with selector radio buttons (Any,
     * Anonymous, Users, DN, Group), DN spec sub-controls, Group sub-controls,
     * and the orderable limit entries table.
     *
     * <pre>
     * Limits
     * .---------------------------------------------------.
     * | (o) Any                                           |
     * | (o) Anonymous                                     |
     * | (o) Users                                         |
     * |            .------------------------------------. |
     * |            | Type :    [--------------------|v] | |
     * | (o) DN     | Style :   [--------------------|v] | |
     * |            | Pattern : [----------------------] | |
     * |            '------------------------------------' |
     * |            .------------------------------------. |
     * |            | ObjectClass :   [--------------|v] | |
     * | (o) Group  | AttributeType : [--------------|v] | |
     * |            | Pattern :       [----------------] | |
     * |            '------------------------------------' |
     * |                                                   |
     * | Limits :                                          |
     * | +-------------------------------------+           |
     * | |{1}defxyz12                          | (Add...)  |
     * | |{2}aaa                               | (Edit...) |
     * | |                                     | (Delete)  |
     * | |                                     | --------- |
     * | |                                     | (Up...)   |
     * | |                                     | (Down...) |
     * | +-------------------------------------+           |
     * '---------------------------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the input group to
     */
    private void createLimitsEditGroup( Composite parent )
    {
        // Selector Group
        Group selectorGroup = BaseWidgetUtils.createGroup( parent, "Limit input", 1 );
        GridLayout selectorGridLayout = new GridLayout( 2, false );
        selectorGroup.setLayout( selectorGridLayout );
        selectorGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Any button
        anyButton = BaseWidgetUtils.createRadiobutton( selectorGroup, "Any", 2 );
        anyButton.addSelectionListener( selectorButtonsSelectionListener );

        // Anonymous button
        anonymousButton = BaseWidgetUtils.createRadiobutton( selectorGroup, "Anonymous", 2 );
        anonymousButton.addSelectionListener( selectorButtonsSelectionListener );

        // Users button
        usersButton = BaseWidgetUtils.createRadiobutton( selectorGroup, "Users", 2 );
        usersButton.addSelectionListener( selectorButtonsSelectionListener );

        // DNSpec button
        dnSpecButton = BaseWidgetUtils.createRadiobutton( selectorGroup, "DN", 1 );
        dnSpecButton.addSelectionListener( selectorButtonsSelectionListener );

        // The group associated with the DN Sepc
        Group dnSpecGroup = BaseWidgetUtils.createGroup( selectorGroup, "", 2 );
        GridLayout dnSpecGridLayout = new GridLayout( 2, false );
        dnSpecGroup.setLayout( dnSpecGridLayout );
        dnSpecGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // The DNSpec type Combo
        BaseWidgetUtils.createLabel( dnSpecGroup, "Type :", 1 );
        dnSpecTypeCombo = BaseWidgetUtils.createCombo( dnSpecGroup, DnSpecTypeEnum.getNames(), -1, 1 );
        dnSpecTypeCombo.setEnabled( false );
        dnSpecTypeCombo.addSelectionListener( dnSpecTypeComboListener );

        // The DNSpec style Combo
        BaseWidgetUtils.createLabel( dnSpecGroup, "Style :", 1 );
        dnSpecStyleCombo = BaseWidgetUtils.createCombo( dnSpecGroup, DnSpecStyleEnum.getNames(), -1, 1 );
        dnSpecStyleCombo.setEnabled( false );
        dnSpecStyleCombo.addSelectionListener( dnSpecStyleComboListener );

        // The DNSpec pattern Text
        BaseWidgetUtils.createLabel( dnSpecGroup, "Pattern :", 1 );
        dnSpecPatternText = BaseWidgetUtils.createText( dnSpecGroup, "", 1 );
        dnSpecPatternText.setEnabled( false );
        dnSpecPatternText.addModifyListener( patternTextListener );

        // Group button
        groupButton = BaseWidgetUtils.createRadiobutton( selectorGroup, "Group", 1 );
        groupButton.addSelectionListener( selectorButtonsSelectionListener );

        // The group associated with the Group
        Group groupGroup = BaseWidgetUtils.createGroup( selectorGroup, "", 2 );
        GridLayout groupGridLayout = new GridLayout( 2, false );
        groupGroup.setLayout( groupGridLayout );
        groupGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        schemaObjectLoader = new SchemaObjectLoader();

        // The ObjectClass Combo
        BaseWidgetUtils.createLabel( groupGroup, "ObjectClass :", 1 );
        groupObjectClassCombo = BaseWidgetUtils.createCombo( groupGroup, schemaObjectLoader.getObjectClassNamesAndOids(), -1, 1 );
        groupObjectClassCombo.setEnabled( false );
        groupObjectClassCombo.addSelectionListener( groupObjectClassComboListener );

        // The AttributeType Combo
        BaseWidgetUtils.createLabel( groupGroup, "Attribute Type :", 1 );
        groupAttributeTypeCombo = BaseWidgetUtils.createCombo( groupGroup, schemaObjectLoader.getAttributeNamesAndOids(), -1, 1 );
        groupAttributeTypeCombo.setEnabled( false );
        groupAttributeTypeCombo.addSelectionListener( groupAttributeTypeComboListener );

        // The Group pattern Text
        BaseWidgetUtils.createLabel( groupGroup, "Pattern :", 1 );
        groupPatternText = BaseWidgetUtils.createText( groupGroup, "", 1 );
        groupPatternText.setEnabled( false );
        dnSpecPatternText.addModifyListener( patternTextListener );

        // The Limits table
        BaseWidgetUtils.createLabel( selectorGroup, "Limits :", 1 );

        limitsTableWidget = new TableWidget<>(
            new LimitDecorator( parent.getShell() , "Limit") );

        limitsTableWidget.createWidgetWithEdit( selectorGroup, null );
        limitsTableWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        limitsTableWidget.addWidgetModifyListener( limitsTableWidgetListener );
    }


    // Like adding a read-only status board to the operations room so
    // commanders can see the synthesized limits string at a glance
    // without having to decode the individual form fields, we create
    // the resulting-limits display group with a non-editable text area.
    /**
     * Builds the read-only "Resulting Limits" display group, showing
     * the computed limits string so the operator can immediately see
     * what the current form field values will produce.
     *
     * <pre>
     * Resulting Limits
     * .------------------------------------.
     * | <////////////////////////////////> |
     * '------------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the display group to
     */
    private void createLimitsShowGroup( Composite parent )
    {
        // Limits Group
        Group limitsGroup = BaseWidgetUtils.createGroup( parent, "Resulting Limits", 1 );
        GridLayout limitsGroupGridLayout = new GridLayout( 2, false );
        limitsGroup.setLayout( limitsGroupGridLayout );
        limitsGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Limits Text
        limitsText = BaseWidgetUtils.createText( limitsGroup, "", 1 );
        limitsText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        limitsText.setEditable( false );
    }


    // Like connecting all the status feeds from each sub-panel back
    // to the operations display so every change is reflected immediately,
    // we wire up the various listeners that update the edited element
    // and the resulting-limits text when the operator makes changes.
    /**
     * Attaches the event listeners to the dialog's interactive controls
     * so the edited element and the resulting-limits text stay in sync
     * as the operator makes selections and enters values.
     */
    private void addListeners()
    {
        /*
        softLimitText.addModifyListener( softLimitTextListener );
        softUnlimitedCheckbox.addSelectionListener( softUnlimitedCheckboxSelectionListener );
        hardLimitText.addModifyListener( hardLimitTextListener );
        hardUnlimitedCheckbox.addSelectionListener( hardUnlimitedCheckboxSelectionListener );
        hardSoftCheckbox.addSelectionListener( hardSoftCheckboxSelectionListener );
        globalLimitText.addModifyListener( globalLimitTextListener );
        globalUnlimitedCheckbox.addSelectionListener( globalUnlimitedCheckboxSelectionListener );
        */
    }


    // Like Leia starting a brand-new transmission from a blank template,
    // we seed the dialog with an empty LimitsWrapper so the operator
    // starts from a clean slate when adding a new limits entry.
    /**
     * Seeds the dialog with a new empty {@link LimitsWrapper} when the
     * operator is adding a brand-new limits configuration entry.
     */
    @Override
    public void addNewElement()
    {
        setEditedElement( new LimitsWrapper( "" ) );
    }


    // Like the hologram operator reviewing the existing mission parameters
    // before the briefing goes live so the radio buttons and combos
    // reflect the current selector state, we read the edited element's
    // selector value and enable or disable the right sub-controls.
    /**
     * Initializes the dialog controls from the current {@link LimitsWrapper},
     * selecting the correct selector radio button and enabling only the
     * sub-controls that are relevant to that selector type.
     */
    protected void initDialog()
    {
        LimitsWrapper editedElement = getEditedElement();

        if ( editedElement != null )
        {
            LimitSelectorEnum selector = editedElement.getSelector();

            if ( selector != null )
            {
                switch ( editedElement.getSelector() )
                {
                    case ANONYMOUS :
                        anonymousButton.setSelection( true );
                        dnSpecStyleCombo.setEnabled( false );
                        dnSpecTypeCombo.setEnabled( false );
                        dnSpecPatternText.setEnabled( false );
                        groupAttributeTypeCombo.setEnabled( false );
                        groupObjectClassCombo.setEnabled( false );
                        groupPatternText.setEnabled( false );
                        break;

                    case ANY :
                        anyButton.setSelection( true );
                        dnSpecStyleCombo.setEnabled( false );
                        dnSpecTypeCombo.setEnabled( false );
                        dnSpecPatternText.setEnabled( false );
                        groupAttributeTypeCombo.setEnabled( false );
                        groupObjectClassCombo.setEnabled( false );
                        groupPatternText.setEnabled( false );
                        break;

                    case USERS :
                        usersButton.setSelection( true );
                        dnSpecStyleCombo.setEnabled( false );
                        dnSpecTypeCombo.setEnabled( false );
                        dnSpecTypeCombo.setEnabled( true );
                        dnSpecPatternText.setEnabled( false );
                        groupAttributeTypeCombo.setEnabled( false );
                        groupObjectClassCombo.setEnabled( false );
                        groupPatternText.setEnabled( false );
                        break;

                    case DNSPEC :
                        dnSpecButton.setSelection( true );
                        dnSpecStyleCombo.setEnabled( true );
                        dnSpecTypeCombo.setEnabled( true );
                        dnSpecPatternText.setEnabled( true );
                        groupAttributeTypeCombo.setEnabled( false );
                        groupObjectClassCombo.setEnabled( false );
                        groupPatternText.setEnabled( false );
                        break;

                    case GROUP :
                        groupButton.setSelection( true );
                        dnSpecStyleCombo.setEnabled( false );
                        dnSpecTypeCombo.setEnabled( false );
                        dnSpecPatternText.setEnabled( false );
                        groupAttributeTypeCombo.setEnabled( true );
                        groupObjectClassCombo.setEnabled( true );
                        groupPatternText.setEnabled( true );
                        break;

                    default :
                        dnSpecStyleCombo.setEnabled( false );
                        dnSpecTypeCombo.setEnabled( false );
                        dnSpecTypeCombo.setEnabled( false );
                        dnSpecPatternText.setEnabled( false );
                        groupAttributeTypeCombo.setEnabled( false );
                        groupObjectClassCombo.setEnabled( false );
                        groupPatternText.setEnabled( false );
                        break;
                }
            }
            else
            {
                dnSpecStyleCombo.setEnabled( false );
                dnSpecTypeCombo.setEnabled( false );
                dnSpecTypeCombo.setEnabled( false );
                dnSpecPatternText.setEnabled( false );
                groupAttributeTypeCombo.setEnabled( false );
                groupObjectClassCombo.setEnabled( false );
                groupPatternText.setEnabled( false );
            }
        }
    }
}
