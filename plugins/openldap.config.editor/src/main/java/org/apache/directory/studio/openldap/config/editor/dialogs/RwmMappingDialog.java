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


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.apache.directory.studio.openldap.config.model.overlay.OlcRwmMapValue;
import org.apache.directory.studio.openldap.config.model.overlay.OlcRwmMapValueTypeEnum;


// Like Princess Leia's hologram delivering a precise mapping directive —
// "translate attribute X on our side to attribute Y on theirs" — we
// present a compact dialog where the administrator picks the mapping type
// (attribute or object class) and enters local and foreign names to
// define one rewrite/remap translation rule.
/**
 * A dialog for editing a single RWM (Rewrite/Remap) mapping value, which
 * consists of a type (attribute type or object class), a local schema
 * name, and a foreign schema name. We pull attribute type and object class
 * suggestions from the connected directory's schema and populate the
 * name combos accordingly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RwmMappingDialog extends Dialog
{
    /** The connection's attribute types */
    private List<String> connectionAttributeTypes;

    /** The connection's object classes */
    private List<String> connectionObjectClasses;

    /** The connection */
    private IBrowserConnection browserConnection;

    /** The value */
    private OlcRwmMapValue value;

    // UI widgets
    private Button okButton;
    private ComboViewer typeComboViewer;
    private ComboViewer localNameComboViewer;
    private ComboViewer foreignNameComboViewer;

    // Listeners

    private ModifyListener namesComboViewerListener = event -> checkAndUpdateOkButtonEnableState();

    private ISelectionChangedListener typeComboViewerSelectionListener = event ->
        {
            OlcRwmMapValueTypeEnum selectedType = getSelectedType();

            // Backing up the combos text
            String localNameText = localNameComboViewer.getCombo().getText();
            String foreignNameText = foreignNameComboViewer.getCombo().getText();

            // Adding the correct suggestions to the viewers
            if ( OlcRwmMapValueTypeEnum.ATTRIBUTE.equals( selectedType ) )
            {
                localNameComboViewer.setInput( connectionAttributeTypes );
                foreignNameComboViewer.setInput( connectionAttributeTypes );
            }
            else if ( OlcRwmMapValueTypeEnum.OBJECTCLASS.equals( selectedType ) )
            {
                localNameComboViewer.setInput( connectionObjectClasses );
                foreignNameComboViewer.setInput( connectionObjectClasses );
            }

            // Restoring the combos text
            localNameComboViewer.getCombo().setText( localNameText );
            foreignNameComboViewer.getCombo().setText( foreignNameText );
        };


    // Like Leia loading a pre-existing mapping directive into the hologram
    // so the operator can review and revise it, we parse the provided value
    // string into an OlcRwmMapValue and pre-populate the UI when the dialog
    // opens, while also pulling schema lists from the connected directory.
    /**
     * Creates a new RwmMappingDialog pre-populated from the given value string.
     * If the string cannot be parsed we start from an empty mapping.
     *
     * @param parentShell the parent shell
     * @param browserConnection the connection used for schema lookups
     * @param value the existing mapping value string to edit
     */
    public RwmMappingDialog( Shell parentShell, IBrowserConnection browserConnection, String value )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.browserConnection = browserConnection;

        // Parsing the value
        try
        {
            this.value = OlcRwmMapValue.parse( value );

            if ( this.value == null )
            {
                this.value = new OlcRwmMapValue();
            }
        }
        catch ( ParseException e )
        {
            this.value = new OlcRwmMapValue();
        }

        initAttributeTypesAndObjectClassesLists();
    }


    // Like Leia spinning up a brand-new mapping briefing with no prior
    // directive so the operator defines the translation from scratch,
    // we create the dialog with an empty OlcRwmMapValue and load the
    // schema lists for name suggestions.
    /**
     * Creates a new RwmMappingDialog with no pre-existing value, ready
     * for the operator to define a brand-new mapping.
     *
     * @param parentShell the parent shell
     * @param browserConnection the connection used for schema lookups
     */
    public RwmMappingDialog( Shell parentShell, IBrowserConnection browserConnection )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.browserConnection = browserConnection;

        this.value = new OlcRwmMapValue();

        initAttributeTypesAndObjectClassesLists();
    }


    // Like Leia's intelligence analyst pulling the directory's full attribute
    // type and object class catalogs so the operator can pick names from a
    // drop-down instead of typing them blind, we collect and sort the schema
    // names from the connected directory and add the wildcard "*" at the top.
    /**
     * Populates the {@code connectionAttributeTypes} and
     * {@code connectionObjectClasses} lists from the connected directory's
     * schema, sorted case-insensitively with "*" prepended to each list.
     * If there is no connection, both lists are left empty.
     */
    private void initAttributeTypesAndObjectClassesLists()
    {
        connectionAttributeTypes = new ArrayList<>();
        connectionObjectClasses = new ArrayList<>();

        if ( browserConnection != null )
        {
            // Attribute Types
            Collection<AttributeType> atds = browserConnection.getSchema().getAttributeTypeDescriptions();

            for ( AttributeType atd : atds )
            {
                for ( String name : atd.getNames() )
                {
                    connectionAttributeTypes.add( name );
                }
            }

            // Object Classes
            Collection<ObjectClass> ocds = browserConnection.getSchema().getObjectClassDescriptions();

            for ( ObjectClass ocd : ocds )
            {
                for ( String name : ocd.getNames() )
                {
                    connectionObjectClasses.add( name );
                }
            }

            // Creating a case insensitive comparator
            Comparator<String> ignoreCaseComparator = ( o1, o2 ) -> o1.compareToIgnoreCase( o2 );

            // Sorting the lists
            Collections.sort( connectionAttributeTypes, ignoreCaseComparator );
            Collections.sort( connectionObjectClasses, ignoreCaseComparator );

            // Adding the '*' special name
            connectionAttributeTypes.add( 0, "*" );
            connectionObjectClasses.add( 0, "*" );
        }
    }


    // Like labeling the hologram channel "Mapping" so the operator knows
    // they're configuring a rewrite-remap translation rule, we stamp the
    // dialog shell with that title before it opens.
    /**
     * Configures the dialog shell by setting its title to "Mapping".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Mapping" );
    }


    // Like the hologram crew equipping the briefing with an OK button that
    // stays disabled until the required fields are filled in, we create
    // the button bar and immediately check the initial enable state.
    /**
     * Creates the dialog button bar with OK and Cancel buttons, then
     * immediately evaluates whether the OK button should be enabled
     * based on the current field values.
     *
     * @param parent the parent composite to attach the button bar to
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        checkAndUpdateOkButtonEnableState();
    }


    // Like Leia finalizing the mapping directive and committing it to the
    // mission file before transmitting it to the fleet, we read the type,
    // local name, and foreign name from the UI and write them into the
    // value object before closing.
    /**
     * Reads the type, local name, and foreign name from the UI and stores
     * them in the {@link OlcRwmMapValue}, setting names to {@code null}
     * when the operator left them blank, then delegates to the superclass
     * {@code okPressed()} to close the dialog.
     */
    @Override
    protected void okPressed()
    {
        // Type
        value.setType( getSelectedType() );

        // Local Name
        String localName = localNameComboViewer.getCombo().getText();

        if ( ( localName != null ) && ( !localName.isEmpty() ) )
        {
            value.setLocalName( localName );
        }
        else
        {
            value.setLocalName( null );
        }

        // Foreign Name
        String foreignName = foreignNameComboViewer.getCombo().getText();

        if ( ( foreignName != null ) && ( !foreignName.isEmpty() ) )
        {
            value.setForeignName( foreignName );
        }
        else
        {
            value.setForeignName( null );
        }

        super.okPressed();
    }


    // Like Leia's hologram projecting the mapping form with Type, Local Name,
    // and Foreign Name fields so the operator can define the full translation
    // rule in one view, we build the dialog content area with three labeled
    // combos and wire up the listeners.
    /**
     * Builds the dialog content area with type, local name, and foreign name
     * combos, initializes them from the current value, and attaches listeners.
     *
     * @param parent the parent composite to build our content inside
     * @return the fully assembled dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // Creating the dialog composites
        Composite dialogComposite = ( Composite ) super.createDialogArea( parent );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        gridData.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        dialogComposite.setLayoutData( gridData );
        Composite composite = BaseWidgetUtils.createColumnContainer( dialogComposite, 2, 1 );

        // Type
        BaseWidgetUtils.createLabel( composite, "Type:", 1 );
        typeComboViewer = new ComboViewer( composite );
        typeComboViewer.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.NONE, true, false ) );
        typeComboViewer.setContentProvider( new ArrayContentProvider() );
        typeComboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof OlcRwmMapValueTypeEnum )
                {
                    OlcRwmMapValueTypeEnum type = ( OlcRwmMapValueTypeEnum ) element;

                    switch ( type )
                    {
                        case ATTRIBUTE:
                            return "Attribute Type";
                        case OBJECTCLASS:
                            return "Object Class";
                    }
                }

                return super.getText( element );
            }
        } );
        typeComboViewer.setInput( new OlcRwmMapValueTypeEnum[]
            {
                OlcRwmMapValueTypeEnum.ATTRIBUTE,
                OlcRwmMapValueTypeEnum.OBJECTCLASS
        } );

        // Local Name
        BaseWidgetUtils.createLabel( composite, "Local Name:", 1 );
        localNameComboViewer = new ComboViewer( new Combo( composite, SWT.DROP_DOWN ) );
        localNameComboViewer.getControl()
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        localNameComboViewer.setContentProvider( new ArrayContentProvider() );

        // Foreign Name
        BaseWidgetUtils.createLabel( composite, "Foreign Name:", 1 );
        foreignNameComboViewer = new ComboViewer( new Combo( composite, SWT.DROP_DOWN ) );
        foreignNameComboViewer.getControl()
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        foreignNameComboViewer.setContentProvider( new ArrayContentProvider() );

        initFromValue();

        addListeners();

        applyDialogFont( composite );
        return composite;
    }


    // Like wiring the hologram's control panel so every combo change
    // triggers an immediate OK-button eligibility check, we attach
    // listeners to the type selector and both name combos.
    /**
     * Attaches a selection listener to the type combo and modify listeners
     * to the local and foreign name combos so the OK button state updates
     * whenever the operator makes any change.
     */
    private void addListeners()
    {
        typeComboViewer.addSelectionChangedListener( typeComboViewerSelectionListener );
        localNameComboViewer.getCombo().addModifyListener( namesComboViewerListener );
        foreignNameComboViewer.getCombo().addModifyListener( namesComboViewerListener );
    }


    // Like loading the existing mapping data into the hologram display before
    // opening it so the operator sees the current values and can edit from
    // the right baseline, we read type, local name, and foreign name out of
    // the value object and set the corresponding UI widgets.
    /**
     * Initializes the type combo, local name combo, and foreign name combo
     * from the current {@link OlcRwmMapValue}. We also populate the name
     * combos with the appropriate schema suggestions based on the selected type.
     */
    private void initFromValue()
    {
        // Type
        OlcRwmMapValueTypeEnum type = value.getType();

        if ( type != null )
        {
            typeComboViewer.setSelection( new StructuredSelection( type ) );

            // Adding the correct suggestions to the viewers
            if ( OlcRwmMapValueTypeEnum.ATTRIBUTE.equals( type ) )
            {
                localNameComboViewer.setInput( connectionAttributeTypes );
                foreignNameComboViewer.setInput( connectionAttributeTypes );
            }
            else if ( OlcRwmMapValueTypeEnum.OBJECTCLASS.equals( type ) )
            {
                localNameComboViewer.setInput( connectionObjectClasses );
                foreignNameComboViewer.setInput( connectionObjectClasses );
            }
        }
        else
        {
            typeComboViewer.setSelection( new StructuredSelection( OlcRwmMapValueTypeEnum.ATTRIBUTE ) );

            // Adding the suggestions to the viewers
            localNameComboViewer.setInput( connectionAttributeTypes );
            foreignNameComboViewer.setInput( connectionAttributeTypes );
        }

        // Local Name
        String localName = value.getLocalName();

        if ( localName != null )
        {
            localNameComboViewer.getCombo().setText( localName );
        }
        else
        {
            localNameComboViewer.getCombo().setText( "" );
        }

        // Local Name
        String foreignName = value.getForeignName();

        if ( foreignName != null )
        {
            foreignNameComboViewer.getCombo().setText( foreignName );
        }
        else
        {
            foreignNameComboViewer.getCombo().setText( "" );
        }
    }


    // Like the intelligence analyst checking whether the mapping type and
    // foreign name are both specified before stamping the directive as
    // ready to file, we evaluate the current combo values and enable
    // or disable the OK button accordingly.
    /**
     * Evaluates whether the OK button should be enabled. We require a
     * non-null type selection and a non-empty foreign name; the local name
     * is optional and not checked here.
     */
    private void checkAndUpdateOkButtonEnableState()
    {
        boolean enableOkButton = true;

        do
        {
            // Type
            if ( getSelectedType() == null )
            {
                enableOkButton = false;
                break;
            }

            // Local Name can be omitted, so we don't check it

            // Foreign Name can't be omitted
            String foreignName = foreignNameComboViewer.getCombo().getText();

            if ( ( foreignName == null ) || ( foreignName.isEmpty() ) )
            {
                enableOkButton = false;
                break;
            }
        }
        while ( false );

        okButton.setEnabled( enableOkButton );
    }


    // Like reading which mapping type is highlighted on the briefing form
    // so the rest of the logic knows whether to work with attribute types
    // or object classes, we return the currently selected type enum value.
    /**
     * Returns the currently selected {@link OlcRwmMapValueTypeEnum} from
     * the type combo, or {@code null} if nothing is selected.
     *
     * @return the selected mapping type, or {@code null}
     */
    private OlcRwmMapValueTypeEnum getSelectedType()
    {
        StructuredSelection selection = ( StructuredSelection ) typeComboViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            return ( OlcRwmMapValueTypeEnum ) selection.getFirstElement();
        }

        return null;
    }


    // Like handing the completed mapping directive back to the overlay
    // editor so it can be stored in the configuration, we return the
    // final string representation of the mapping value.
    /**
     * Returns the string representation of the edited mapping value,
     * as produced by {@link OlcRwmMapValue#toString()}.
     *
     * @return the mapping value string
     */
    public String getValue()
    {
        return value.toString();
    }
}
