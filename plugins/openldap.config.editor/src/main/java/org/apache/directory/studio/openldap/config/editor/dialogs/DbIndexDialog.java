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


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.dialogs.AttributeDialog;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.TableWidget;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.common.ui.wrappers.StringValueWrapper;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.utils.SchemaObjectLoader;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.apache.directory.studio.openldap.common.ui.model.DbIndexTypeEnum;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.editor.wrappers.DbIndexWrapper;
import org.apache.directory.studio.openldap.config.editor.wrappers.StringValueDecorator;


// Like Princess Leia transmitting the complete technical briefing
// on how to configure database indexing, we present an interactive
// dialog that lets the operator pick attributes and index types
// in one focused session before confirming their choices.
/**
 * A dialog for editing a database index configuration in the OpenLDAP
 * config editor. We let the operator choose one or more attributes to
 * index (or the default) and then tick off which index types (pres, eq,
 * approx, sub, subinitial, subany, subfinal, nolang, nosubtypes, notags)
 * they want enabled.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +--------------------------------------------------+
 * |  Attributes                                      |
 * | .----------------------------------------------. |
 * | |     +----------------------------+           | |
 * | | (o) |                            | (Add...)  | |
 * | |     |                            | (Delete)  | |
 * | |     +----------------------------+           | |
 * | | (o) Default                                  | |
 * | '----------------------------------------------' |
 * |  Indices                                         |
 * | .----------------------------------------------. |
 * | | [] pres        [] eq          [] approx      | |
 * | | [] nolang      [] noSubtypes  [] notags      | |
 * | | [] sub                                       | |
 * | |    [] subinitial                             | |
 * | |    [] subany                                 | |
 * | |    [] subfinal                               | |
 * | '----------------------------------------------' |
 * +--------------------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DbIndexDialog extends AddEditDialog<DbIndexWrapper>
{
    /** The Attribute list loader */
    private SchemaObjectLoader attributeLoader;

    // UI widgets
    // The attribute's group
    private Button attributesCheckbox;
    private TableWidget<StringValueWrapper> attributeTable;
    private Button defaultCheckbox;

    // The index type section
    private Button presCheckbox;
    private Button eqCheckbox;
    private Button approxCheckbox;
    private Button subCheckbox;
    private Button noLangCheckbox;
    private Button noSubtypesCheckbox;
    private Button noTagsCheckbox;
    private Button subInitialCheckbox;
    private Button subAnyCheckbox;
    private Button subFinalCheckbox;

    // The list of all the type buttons
    private Button[] typeButtons = new Button[10];

    /**
     * Listeners for the Attributes radioButton. It will enable the Attributes table.
     */
    private SelectionListener attributesCheckboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            attributeTable.enable();
            getEditedElement().setDefault( false );
            checkAndUpdateOkButtonEnableState();
        }
    };


    /**
     * The attribute table listener
     */
    private WidgetModifyListener attributeTableListener = event ->
        {
            getEditedElement().getAttributes().clear();

            for ( StringValueWrapper attribute : attributeTable.getElements() )
            {
                getEditedElement().getAttributes().add( attribute.getValue() );
            }

            checkAndUpdateOkButtonEnableState();
        };


    /**
     * A listener on the Default radio button. It will disable the Attributes table
     * and the associated buttons.
     */
    private SelectionListener defaultCheckboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            attributeTable.disable();
            getEditedElement().setDefault( true );
            checkAndUpdateOkButtonEnableState();
        }
    };


    /**
     * A listener on one of the indexType checkboxes (but SUB and SUBxxx)
     */
    private SelectionListener typeButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            checkAndUpdateOkButtonEnableState();
            DbIndexWrapper indexWrapper = getEditedElement();

            // Update the edited element
            Button selectedCheckbox = (Button)e.getSource();

            for ( int i = 0; i < typeButtons.length; i++ )
            {
                if ( typeButtons[i] == selectedCheckbox )
                {
                    DbIndexTypeEnum indexType = DbIndexTypeEnum.getIndexType( i );

                    if ( selectedCheckbox.getSelection() )
                    {
                        indexWrapper.getTypes().add( indexType );
                    }
                    else
                    {
                        indexWrapper.getTypes().remove( indexType );
                    }
                }
            }
        }
    };


    /**
     * A listener on the SUB indice check box. If it's selected, we will grey all the sub-sub indexes.
     * If it's delected, we will remove all the sub-sub indexes
     */
    private SelectionListener subCheckboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            DbIndexWrapper indexWrapper = getEditedElement();

            if ( subCheckbox.getSelection() )
            {
                indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUB );
                indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBINITIAL );
                indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBANY );
                indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBFINAL );

                subInitialCheckbox.setSelection( true );
                subAnyCheckbox.setSelection( true );
                subFinalCheckbox.setSelection( true );
            }
            else
            {
                indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUB );
                indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBINITIAL );
                indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBANY );
                indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBFINAL );

                subInitialCheckbox.setSelection( false );
                subAnyCheckbox.setSelection( false );
                subFinalCheckbox.setSelection( false );
            }

            checkAndUpdateOkButtonEnableState();
        }
    };


    /**
     * A listener on the SUB related indices check boxes. We will disable the SUB checkbox, no matter what
     */
    private SelectionListener subSubCheckboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            // Check that we aren't coming from a modification of the SUB button
            DbIndexWrapper indexWrapper = getEditedElement();

            Button button = (Button)e.getSource();

            // First, update the indexTypes set
            if ( button == subAnyCheckbox )
            {
                if ( button.getSelection() )
                {
                    if ( subInitialCheckbox.getSelection() && subFinalCheckbox.getSelection() )
                    {
                        // The three subXXX indexes are selected : select the SUB checkbox
                        subCheckbox.setSelection( true );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBINITIAL );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBANY );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBFINAL );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUB );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                    }
                    else if ( subInitialCheckbox.getSelection() || subFinalCheckbox.getSelection() )
                    {
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBANY );
                    }
                    else
                    {
                        // Gray the sub checkbox, and select it
                        subCheckbox.setSelection( true );
                        subCheckbox.setGrayed( true );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBANY );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUB );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                    }
                }
                else
                {
                    if ( !subInitialCheckbox.getSelection() && !subFinalCheckbox.getSelection() )
                    {
                        subCheckbox.setGrayed( false );
                        subCheckbox.setSelection( false );
                    }
                    else if ( subInitialCheckbox.getSelection() && subFinalCheckbox.getSelection() )
                    {
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBINITIAL );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBFINAL );
                        subCheckbox.setGrayed( true );
                    }

                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBANY );
                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUB );
                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                }
            }
            else if ( button == subInitialCheckbox )
            {
                if ( button.getSelection() )
                {
                    if ( subAnyCheckbox.getSelection() && subFinalCheckbox.getSelection() )
                    {
                        // The three subXXX indexes are selected : select the SUB checkbox
                        subCheckbox.setSelection( true );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBINITIAL );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBANY );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBFINAL );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUB );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                    }
                    else if ( subAnyCheckbox.getSelection() || subFinalCheckbox.getSelection() )
                    {
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBINITIAL );
                    }
                    else
                    {
                        // Gray the sub checkbox, and select it
                        subCheckbox.setSelection( true );
                        subCheckbox.setGrayed( true );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBANY );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUB );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                    }
                }
                else
                {
                    if ( !subAnyCheckbox.getSelection() && !subFinalCheckbox.getSelection() )
                    {
                        subCheckbox.setGrayed( false );
                        subCheckbox.setSelection( false );
                    }
                    else if ( subAnyCheckbox.getSelection() && subFinalCheckbox.getSelection() )
                    {
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBANY );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBFINAL );
                        subCheckbox.setGrayed( true );
                    }

                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBINITIAL );
                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUB );
                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                }
            }
            else if ( button == subFinalCheckbox )
            {
                if ( button.getSelection() )
                {
                    if ( subAnyCheckbox.getSelection() && subInitialCheckbox.getSelection() )
                    {
                        // The three subXXX indexes are selected : select the SUB checkbox
                        subCheckbox.setSelection( true );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBINITIAL );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBANY );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBFINAL );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUB );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                    }
                    else if ( subAnyCheckbox.getSelection() || subInitialCheckbox.getSelection() )
                    {
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBFINAL );
                    }
                    else
                    {
                        // Gray the sub checkbox, and select it
                        subCheckbox.setSelection( true );
                        subCheckbox.setGrayed( true );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBFINAL );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUB );
                        indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                    }
                }
                else
                {
                    if ( !subAnyCheckbox.getSelection() && !subInitialCheckbox.getSelection() )
                    {
                        subCheckbox.setGrayed( false );
                        subCheckbox.setSelection( false );
                    }
                    else if ( subAnyCheckbox.getSelection() && subInitialCheckbox.getSelection() )
                    {
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBANY );
                        indexWrapper.getIndexTypes().add( DbIndexTypeEnum.SUBINITIAL );
                        subCheckbox.setGrayed( true );
                    }

                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBFINAL );
                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUB );
                    indexWrapper.getIndexTypes().remove( DbIndexTypeEnum.SUBSTR );
                }
            }

            // Last, update the subCheckbox state
            checkAndUpdateOkButtonEnableState();
        }
    };


    // Like Leia initializing the hologram projector before the briefing
    // begins, we set up the dialog with a browser connection reference
    // and prepare the attribute schema loader so it's ready to populate
    // the attribute table when the dialog opens.
    /**
     * Creates a new DbIndexDialog attached to the given parent shell.
     * We initialize the schema object loader here so the attribute table
     * can be populated with known attribute names.
     *
     * @param parentShell the parent shell this dialog belongs to
     * @param browserConnection the LDAP browser connection (reserved for future use)
     */
    public DbIndexDialog( Shell parentShell, IBrowserConnection browserConnection )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        attributeLoader = new SchemaObjectLoader();
    }


    // Like labeling the hologram frame so the Rebellion knows they're
    // watching the "Index" briefing, we stamp the shell title so the
    // operator can immediately see what this dialog is all about.
    /**
     * Configures the dialog shell by setting its title to "Index".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Index" );
    }


    // Like Leia's hologram materializing the full technical layout —
    // attributes section on top, index types below — we build both
    // groups in sequence, then seed the UI with whatever data is
    // already in the index wrapper before handing control to the operator.
    /**
     * Builds the full dialog content area with an attributes group and
     * an indices group, then initializes the controls from the current
     * index wrapper so existing values are pre-selected.
     *
     * <pre>
     * +--------------------------------------------------+
     * |  Attributes                                      |
     * | .----------------------------------------------. |
     * | |     +----------------------------+           | |
     * | | (o) |                            | (Add...)  | |
     * | |     |                            | (Delete)  | |
     * | |     +----------------------------+           | |
     * | | (o) Default                                  | |
     * | '----------------------------------------------' |
     * |  Indices                                         |
     * | .----------------------------------------------. |
     * | | [] pres        [] eq          [] approx      | |
     * | | [] nolang      [] noSubtypes  [] notags      | |
     * | | [] sub                                       | |
     * | |    [] subinitial                             | |
     * | |    [] subany                                 | |
     * | |    [] subfinal                               | |
     * | '----------------------------------------------' |
     * +--------------------------------------------------+
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

        // The attributes group
        createAttributesGroup( composite );

        // The indices grouo
        createIndicesGroup( composite );

        // Load the dialog if this was an Edit call
        initDialog();
        //addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // Like Leia's hologram laying out the attributes section of the
    // briefing — a radio for specific attributes plus a table to list
    // them, and a radio for the "Default" catch-all — we build the
    // attributes group panel and wire up all its listeners.
    /**
     * Builds the attributes group UI, creating the attribute list radio
     * button, the scrollable attribute table with add/delete controls,
     * and the Default radio button below it.
     *
     * <pre>
     *   Attributes
     *  .----------------------------------------------.
     *  |     +----------------------------+           |
     *  | (o) |                            | (Add...)  |
     *  |     |                            | (Delete)  |
     *  |     +----------------------------+           |
     *  | (o) Default                                  |
     *  '----------------------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the attributes group to
     */
    private void createAttributesGroup( Composite parent )
    {
        // Attributes Group
        Group attributesGroup = BaseWidgetUtils.createGroup( parent, "Attributes", 1 );
        GridLayout attributesGroupGridLayout = new GridLayout( 2, false );
        attributesGroup.setLayout( attributesGroupGridLayout );
        attributesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Attributes Checkbox
        attributesCheckbox = BaseWidgetUtils.createRadiobutton( attributesGroup, "", 1 );
        attributesCheckbox.setLayoutData( new GridData( SWT.NONE, SWT.CENTER, false, false ) );
        attributesCheckbox.setSelection( true );
        attributesCheckbox.addSelectionListener( attributesCheckboxSelectionListener );

        // Attributes table
        StringValueDecorator decorator = new StringValueDecorator( parent.getShell(), "Attribute" );
        decorator.setImage( OpenLdapConfigurationPlugin.getDefault().getImage(
                    OpenLdapConfigurationPluginConstants.IMG_ATTRIBUTE ) );
        AttributeDialog attributeDialog = new AttributeDialog( parent.getShell() );
        attributeDialog.setAttributeNamesAndOids( attributeLoader.getAttributeNamesAndOids() );
        decorator.setDialog( attributeDialog );
        attributeTable = new TableWidget<>( decorator );

        attributeTable.createWidgetNoEdit( attributesGroup, null );
        attributeTable.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 1, 1 ) );
        attributeTable.addWidgetModifyListener( attributeTableListener );

        // Attributes Checkbox
        defaultCheckbox = BaseWidgetUtils.createRadiobutton( attributesGroup, "  Default", 2 );
        defaultCheckbox.addSelectionListener( defaultCheckboxSelectionListener );
    }


    // Like Leia's hologram switching to the index type section of the
    // briefing and laying out all the index checkboxes in a neat grid,
    // we create the indices panel with pres, eq, approx, nolang,
    // nosubtypes, notags, sub, and the sub-sub checkboxes below it.
    /**
     * Builds the indices group UI, creating checkboxes for each index
     * type and nesting the subinitial/subany/subfinal options beneath
     * the parent sub checkbox.
     *
     * <pre>
     *  Indices
     * .----------------------------------------------.
     * | [] pres        [] eq          [] approx      |
     * | [] nolang      [] noSubtypes  [] notags      |
     * | [] sub                                       |
     * |    [] subinitial                             |
     * |    [] subany                                 |
     * |    [] subfinal                               |
     * '----------------------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the indices group to
     */
    private void createIndicesGroup( Composite parent )
    {
        // Indices Group
        Group indicesGroup = BaseWidgetUtils.createGroup( parent, "Indices", 1 );
        GridLayout indicesGroupGridLayout = new GridLayout( 3, true );
        indicesGroupGridLayout.verticalSpacing = indicesGroupGridLayout.horizontalSpacing = 0;
        indicesGroup.setLayout( indicesGroupGridLayout );
        indicesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Pres Checkbox
        presCheckbox = BaseWidgetUtils.createCheckbox( indicesGroup, "pres", 1 );
        presCheckbox.addSelectionListener( typeButtonSelectionListener );
        typeButtons[DbIndexTypeEnum.PRES.getNumber()] = presCheckbox;

        // Eq Checkbox
        eqCheckbox = BaseWidgetUtils.createCheckbox( indicesGroup, "eq", 1 );
        eqCheckbox.addSelectionListener( typeButtonSelectionListener );
        typeButtons[DbIndexTypeEnum.EQ.getNumber()] = eqCheckbox;

        // Approx Checkbox
        approxCheckbox = BaseWidgetUtils.createCheckbox( indicesGroup, "approx", 1 );
        approxCheckbox.addSelectionListener( typeButtonSelectionListener );
        typeButtons[DbIndexTypeEnum.APPROX.getNumber()] = approxCheckbox;

        // NoLang Checkbox
        noLangCheckbox = BaseWidgetUtils.createCheckbox( indicesGroup, "nolang", 1 );
        noLangCheckbox.addSelectionListener( typeButtonSelectionListener );
        typeButtons[DbIndexTypeEnum.NOLANG.getNumber()] = noLangCheckbox;

        // NoSybtypes Checkbox
        noSubtypesCheckbox = BaseWidgetUtils.createCheckbox( indicesGroup, "nosubtypes", 1 );
        noSubtypesCheckbox.addSelectionListener( typeButtonSelectionListener );
        typeButtons[DbIndexTypeEnum.NOSUBTYPES.getNumber()] = noSubtypesCheckbox;

        // NoTags Checkbox
        noTagsCheckbox = BaseWidgetUtils.createCheckbox( indicesGroup, "notags", 1 );
        noTagsCheckbox.addSelectionListener( typeButtonSelectionListener );
        typeButtons[DbIndexTypeEnum.NOTAGS.getNumber()] = noTagsCheckbox;

        // Sub Checkbox
        subCheckbox = BaseWidgetUtils.createCheckbox( indicesGroup, "sub", 1 );
        subCheckbox.addSelectionListener( subCheckboxSelectionListener );
        typeButtons[DbIndexTypeEnum.SUB.getNumber()] = subCheckbox;

        // Sub Composite
        Composite subComposite = new Composite( indicesGroup, SWT.NONE );
        GridLayout subCompositeGridLayout = new GridLayout( 2, false );
        subCompositeGridLayout.marginHeight = subCompositeGridLayout.marginWidth = 0;
        subCompositeGridLayout.verticalSpacing = subCompositeGridLayout.horizontalSpacing = 0;
        subComposite.setLayout( subCompositeGridLayout );

        // SubInitial Checkbox
        BaseWidgetUtils.createRadioIndent( subComposite, 1 );
        subInitialCheckbox = BaseWidgetUtils.createCheckbox( subComposite, "subinitial", 1 );
        subInitialCheckbox.addSelectionListener( subSubCheckboxSelectionListener );
        typeButtons[DbIndexTypeEnum.SUBINITIAL.getNumber()] = subInitialCheckbox;

        // SubAny Checkbox
        BaseWidgetUtils.createRadioIndent( subComposite, 1 );
        subAnyCheckbox = BaseWidgetUtils.createCheckbox( subComposite, "subany", 1 );
        subAnyCheckbox.addSelectionListener( subSubCheckboxSelectionListener );
        typeButtons[DbIndexTypeEnum.SUBANY.getNumber()] = subAnyCheckbox;

        // SubFinal Checkbox
        BaseWidgetUtils.createRadioIndent( subComposite, 1 );
        subFinalCheckbox = BaseWidgetUtils.createCheckbox( subComposite, "subfinal", 1 );
        subFinalCheckbox.addSelectionListener( subSubCheckboxSelectionListener );
        typeButtons[DbIndexTypeEnum.SUBFINAL.getNumber()] = subFinalCheckbox;
    }


    // Like flipping all three sub-index toggles in one coordinated
    // move — the way the Rebellion would coordinate a simultaneous
    // strike — we set subinitial, subany, and subfinal all at once
    // and clear any grayed state on the parent sub checkbox.
    /**
     * Sets the selection state for all three sub-index checkboxes
     * (subinitial, subany, subfinal) simultaneously, and clears
     * any grayed state on the parent sub checkbox.
     *
     * @param selection {@code true} to select all three, {@code false} to deselect
     */
    private void setSelectionForSubCheckboxes( boolean selection )
    {
        subCheckbox.setGrayed( false );
        subInitialCheckbox.setSelection( selection );
        subAnyCheckbox.setSelection( selection );
        subFinalCheckbox.setSelection( selection );
    }


    // Like checking whether the Rebellion's sub-attack is fully
    // coordinated or only partially underway, we look at the three
    // sub-index checkboxes and update the parent sub checkbox to
    // reflect whether all, some, or none of them are selected.
    /**
     * Examines the selection state of the three sub-index checkboxes
     * and updates the parent sub checkbox accordingly — grayed when
     * partially selected, checked when all selected, unchecked when none.
     */
    private void checkAndUpdateSubCheckboxSelectionState()
    {
        boolean atLeastOneSelected = subInitialCheckbox.getSelection()
            || subAnyCheckbox.getSelection() || subFinalCheckbox.getSelection();
        boolean allSelected = subInitialCheckbox.getSelection()
            && subAnyCheckbox.getSelection() && subFinalCheckbox.getSelection();
        subCheckbox.setGrayed( atLeastOneSelected && !allSelected );
        subCheckbox.setSelection( atLeastOneSelected );
    }


    // Like loading the Rebellion's known attribute list into the
    // briefing display before the hologram goes live, we convert
    // the raw attribute strings into wrapper objects and push them
    // into the table widget so the operator can see what's indexed.
    /**
     * Populates the attribute table widget from the given set of
     * attribute name strings, wrapping each in a {@link StringValueWrapper}
     * before loading them into the table.
     *
     * @param attributes the set of attribute names to display in the table
     */
    private void initAttributeTable( Set<String> attributes )
    {
        List<StringValueWrapper> attributeWrappers = new ArrayList<>();

        if ( attributes != null )
        {
            for ( String attribute : attributes )
            {
                attributeWrappers.add( new StringValueWrapper( attribute, false ) );
            }
        }

        attributeTable.setElements( attributeWrappers );
    }


    // Like the hologram operator loading the pre-existing mission data
    // before the briefing starts so everyone sees the current state,
    // we pull values out of the index wrapper and pre-populate every
    // checkbox and table entry so edits start from the right baseline.
    /**
     * Initializes the dialog controls from the current {@link DbIndexWrapper},
     * pre-selecting the attribute table entries and all the index type
     * checkboxes that match the wrapper's stored configuration.
     */
    protected void initDialog()
    {
        DbIndexWrapper editedElement = getEditedElement();

        if ( editedElement != null )
        {
            // Attributes
            initAttributeTable( editedElement.getAttributes() );

            // Default
            if ( editedElement.isDefault() )
            {
                attributesCheckbox.setSelection( false );
                attributeTable.disable();
                defaultCheckbox.setSelection( true );
            }

            // Index types
            Set<DbIndexTypeEnum> indexTypes = editedElement.getTypes();

            if ( ( indexTypes != null ) && !indexTypes.isEmpty() )
            {
                presCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.PRES ) );
                eqCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.EQ ) );
                approxCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.APPROX ) );

                if ( indexTypes.contains( DbIndexTypeEnum.SUB ) )
                {
                    subCheckbox.setSelection( true );
                    setSelectionForSubCheckboxes( indexTypes.contains( DbIndexTypeEnum.SUB ) );
                }
                else
                {
                    subInitialCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.SUBINITIAL ) );
                    subAnyCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.SUBANY ) );
                    subFinalCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.SUBFINAL ) );
                    checkAndUpdateSubCheckboxSelectionState();
                }

                noLangCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.NOLANG ) );
                noSubtypesCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.NOSUBTYPES ) );
                noTagsCheckbox.setSelection( indexTypes.contains( DbIndexTypeEnum.NOTAGS ) );
            }
        }
    }


    // Like the Rebellion cloning an existing mission briefing to use
    // as the starting point for a new operation, we clone the given
    // index wrapper and set it as the element being edited so the
    // operator starts from a known-good baseline.
    /**
     * Prepares the dialog for editing a new element by cloning the
     * given {@link DbIndexWrapper} and setting the clone as the
     * edited element.
     *
     * @param editedElement the index wrapper to clone as the starting point
     */
    protected void addNewElement( DbIndexWrapper editedElement )
    {
        DbIndexWrapper newElement = editedElement.clone();
        setEditedElement( newElement );
    }


    // Like the Rebellion starting a fresh mission plan from a blank
    // slate, we create a new empty index wrapper and set it as the
    // element being edited when the operator adds a brand-new index.
    /**
     * Prepares the dialog for adding a brand-new index entry by
     * setting a fresh empty {@link DbIndexWrapper} as the edited element.
     */
    public void addNewElement()
    {
        setEditedElement( new DbIndexWrapper( "" ) );
    }


    // Like making sure the transmit button on the hologram projector
    // starts out locked until there's actually something worth sending,
    // we create the OK button but disable it if the index wrapper is
    // empty or has no attributes yet.
    /**
     * Creates the dialog buttons, disabling the OK button at startup
     * when the current index wrapper has no attributes configured yet.
     *
     * @param parent the button bar composite to add buttons to
     * @param id the button's ID constant
     * @param label the text label for the button
     * @param defaultButton whether this is the default button
     * @return the newly created button widget
     */
    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton)
    {
        Button button = super.createButton(parent, id, label, defaultButton);

        if ( id == IDialogConstants.OK_ID )
        {
            DbIndexWrapper dbIndexWrapper = getEditedElement();

            if ( ( dbIndexWrapper == null ) || dbIndexWrapper.getAttributes().isEmpty() )
            {
                button.setEnabled( false );
            }
        }

        return button;
    }


    // Like the Rebellion's mission controller checking whether the plan
    // is complete enough to authorize the strike, we evaluate the current
    // dialog state and enable or disable the OK button based on whether
    // the operator has made a valid and complete index configuration.
    /**
     * Evaluates the current selection state and enables the OK button
     * only when the configuration is complete — either the default checkbox
     * is selected with at least one index type chosen, or the attributes
     * table contains at least one attribute.
     */
    private void checkAndUpdateOkButtonEnableState()
    {
        Button okButton = getButton( IDialogConstants.OK_ID );

        if ( defaultCheckbox.getSelection() )
        {
            okButton.setEnabled(
                presCheckbox.getSelection() ||
                eqCheckbox.getSelection() ||
                approxCheckbox.getSelection() ||
                subCheckbox.getSelection() ||
                subInitialCheckbox.getSelection() ||
                subAnyCheckbox.getSelection() ||
                subFinalCheckbox.getSelection() ||
                noLangCheckbox.getSelection() ||
                noTagsCheckbox.getSelection() ||
                noSubtypesCheckbox.getSelection() );
        }
        else
        {
            okButton.setEnabled( !getEditedElement().getAttributes().isEmpty() );
        }
    }
}
