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
import java.util.List;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
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
import org.apache.directory.studio.openldap.common.ui.widgets.EntryWidget;
import org.apache.directory.studio.openldap.config.model.overlay.OlcValSortMethodEnum;
import org.apache.directory.studio.openldap.config.model.overlay.OlcValSortValue;


// Like Princess Leia's hologram delivering a precise value-sorting directive —
// specifying which attribute to sort, the base DN scope, and whether to sort
// alphabetically, numerically, or by weight — we present a focused dialog
// where the administrator configures one value-sorting rule for the ValSort
// overlay, complete with an optional secondary sort method when weighted
// sorting is selected.
/**
 * The ValueSortingValueDialog is used to edit a single value from the Value
 * Sorting overlay configuration. We present combos for attribute, base DN,
 * sort method, and (when weighted) secondary sort method, pulling attribute
 * type suggestions from the connected directory's schema.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueSortingValueDialog extends Dialog
{
    /** The 'weighted' combo viewer option */
    private static final String WEIGHTED_OPTION = "Weighted";

    /** The '&lt;none&gt;' combo viewer option */
    private static final String NONE_OPTION = "<none>";

    /** The connection's attribute types */
    private List<String> connectionAttributeTypes;

    /** The connection */
    private IBrowserConnection browserConnection;

    /** The value */
    private OlcValSortValue value;

    // UI widgets
    private Button okButton;
    private ComboViewer attributeComboViewer;
    private EntryWidget baseDnEntryWidget;
    private ComboViewer sortMethodComboViewer;
    private ComboViewer secondarySortMethodComboViewer;

    // Listeners

    private ModifyListener attributeComboViewerListener = event -> checkAndUpdateOkButtonEnableState();

    private WidgetModifyListener baseDnEntryWidgetListener = event -> checkAndUpdateOkButtonEnableState();

    private ISelectionChangedListener sortMethodComboViewerListener = event ->
        {
            Object selectedSortMethod = getSelectedSortMethod();

            if ( WEIGHTED_OPTION.equals( selectedSortMethod ) )
            {
                secondarySortMethodComboViewer.getCombo().setEnabled( true );
            }
            else
            {
                secondarySortMethodComboViewer.getCombo().setEnabled( false );
                secondarySortMethodComboViewer.setSelection( new StructuredSelection( NONE_OPTION ) );
            }
        };


    // Like Leia loading a pre-existing value-sorting rule into the hologram
    // so the operator can review and revise it, we parse the provided value
    // string into an OlcValSortValue and pull the attribute type list from
    // the directory schema before opening.
    /**
     * Creates a new ValueSortingValueDialog pre-populated from the given
     * value string. If the string cannot be parsed we start from an empty value.
     *
     * @param parentShell the parent shell
     * @param browserConnection the connection used for schema lookups
     * @param value the existing value-sorting rule string to edit
     */
    public ValueSortingValueDialog( Shell parentShell, IBrowserConnection browserConnection, String value )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.browserConnection = browserConnection;

        // Parsing the value
        try
        {
            this.value = OlcValSortValue.parse( value );

            if ( this.value == null )
            {
                this.value = new OlcValSortValue();
            }
        }
        catch ( ParseException e )
        {
            this.value = new OlcValSortValue();
        }

        initAttributeTypesList();
    }


    // Like Leia spinning up a new value-sorting briefing from scratch when
    // no prior rule exists, we create the dialog with an empty OlcValSortValue
    // and load the schema attribute type list for combo suggestions.
    /**
     * Creates a new ValueSortingValueDialog with no pre-existing value,
     * ready for the operator to define a brand-new value-sorting rule.
     *
     * @param parentShell the parent shell
     * @param browserConnection the connection used for schema lookups
     */
    public ValueSortingValueDialog( Shell parentShell, IBrowserConnection browserConnection )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.browserConnection = browserConnection;

        this.value = new OlcValSortValue();

        initAttributeTypesList();
    }


    // Like Leia's intelligence analyst pulling the directory's complete
    // attribute type catalog so the operator can pick from a sorted list
    // rather than typing attribute names blind, we collect all attribute type
    // names from the schema and sort them case-insensitively.
    /**
     * Populates the {@code connectionAttributeTypes} list from the connected
     * directory's schema, sorted case-insensitively. If no connection is
     * available, the list is left empty.
     */
    private void initAttributeTypesList()
    {
        connectionAttributeTypes = new ArrayList<>();

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

            // Sorting the list
            Collections.sort( connectionAttributeTypes, ( o1, o2 ) -> o1.compareToIgnoreCase( o2 ) );
        }
    }


    // Like labeling the hologram channel "Value Sort" so the operator
    // knows they're defining a value-sorting rule, we stamp the shell
    // title before the dialog opens.
    /**
     * Configures the dialog shell by setting its title to "Value Sort".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Value Sort" );
    }


    // Like the hologram crew equipping the briefing with an OK button that
    // stays disabled until the required attribute and base DN fields are
    // filled in, we create the button bar and immediately check the enable state.
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


    // Like Leia finalizing the value-sorting directive and committing it
    // to the configuration file before transmitting it to the overlay,
    // we read attribute, base DN, and sort method from the UI and write
    // them into the value object before closing.
    /**
     * Reads attribute, base DN, sort method, and (if weighted) secondary sort
     * method from the UI and stores them in the {@link OlcValSortValue}, then
     * delegates to the superclass {@code okPressed()} to close the dialog.
     */
    @Override
    protected void okPressed()
    {
        // Attribute
        value.setAttribute( attributeComboViewer.getCombo().getText() );

        // Base DN
        value.setBaseDn( baseDnEntryWidget.getDn() );

        // Sort Method
        Object selectedSortMethod = getSelectedSortMethod();

        if ( WEIGHTED_OPTION.equals( selectedSortMethod ) )
        {
            value.setWeighted( true );

            // Secondary Sort Method
            Object selectedSecondarySortMethod = getSelectedSecondarySortMethod();

            if ( NONE_OPTION.equals( selectedSecondarySortMethod ) )
            {
                value.setSortMethod( null );
            }
            else
            {
                value.setSortMethod( ( OlcValSortMethodEnum ) selectedSecondarySortMethod );
            }
        }
        else
        {
            value.setSortMethod( ( OlcValSortMethodEnum ) selectedSortMethod );
        }

        super.okPressed();
    }


    // Like Leia's hologram projecting the full value-sorting briefing with
    // attribute, base DN, sort method, and secondary sort method fields
    // so the operator can configure the complete rule in one view, we build
    // the dialog content area and wire up the listeners.
    /**
     * Builds the dialog content area with combos for attribute, base DN,
     * sort method, and secondary sort method, initializes them from the
     * current value, and attaches listeners.
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

        // Attribute
        BaseWidgetUtils.createLabel( composite, "Attribute:", 1 );
        attributeComboViewer = new ComboViewer( new Combo( composite, SWT.DROP_DOWN ) );
        attributeComboViewer.getControl()
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        attributeComboViewer.setContentProvider( new ArrayContentProvider() );
        attributeComboViewer.setInput( connectionAttributeTypes );

        // Base DN
        BaseWidgetUtils.createLabel( composite, "Base DN:", 1 );
        baseDnEntryWidget = new EntryWidget( browserConnection );
        baseDnEntryWidget.createWidget( composite );
        baseDnEntryWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Sort Method
        BaseWidgetUtils.createLabel( composite, "Sort Method:", 1 );
        sortMethodComboViewer = new ComboViewer( composite );
        sortMethodComboViewer.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.NONE, true, false ) );
        sortMethodComboViewer.setContentProvider( new ArrayContentProvider() );
        sortMethodComboViewer.setLabelProvider( new OlcValSortMethodEnumLabelProvider() );
        sortMethodComboViewer.setInput( new Object[]
            {
                OlcValSortMethodEnum.ALPHA_ASCEND,
                OlcValSortMethodEnum.ALPHA_DESCEND,
                OlcValSortMethodEnum.NUMERIC_ASCEND,
                OlcValSortMethodEnum.NUMERIC_DESCEND,
                WEIGHTED_OPTION
        } );

        // Secondary Sort Method
        BaseWidgetUtils.createLabel( composite, "Secondary Sort Method:", 1 );
        secondarySortMethodComboViewer = new ComboViewer( composite );
        secondarySortMethodComboViewer.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.NONE, true, false ) );
        secondarySortMethodComboViewer.setContentProvider( new ArrayContentProvider() );
        secondarySortMethodComboViewer.setLabelProvider( new OlcValSortMethodEnumLabelProvider() );
        secondarySortMethodComboViewer.setInput( new Object[]
            {
                NONE_OPTION,
                OlcValSortMethodEnum.ALPHA_ASCEND,
                OlcValSortMethodEnum.ALPHA_DESCEND,
                OlcValSortMethodEnum.NUMERIC_ASCEND,
                OlcValSortMethodEnum.NUMERIC_DESCEND
        } );

        initFromValue();

        addListeners();

        applyDialogFont( composite );
        return composite;
    }


    // Like wiring the hologram's control panel so every field change triggers
    // an immediate OK-button eligibility check, we attach listeners to the
    // attribute combo, base DN widget, and sort method selector.
    /**
     * Attaches modify and selection listeners to the attribute combo, base DN
     * widget, and sort method combo so the OK button state updates whenever
     * the operator makes any change.
     */
    private void addListeners()
    {
        attributeComboViewer.getCombo().addModifyListener( attributeComboViewerListener );
        baseDnEntryWidget.addWidgetModifyListener( baseDnEntryWidgetListener );
        sortMethodComboViewer.addSelectionChangedListener( sortMethodComboViewerListener );
    }


    // Like loading the existing value-sorting rule data into the hologram
    // display before opening it so the operator sees the current values,
    // we read attribute, base DN, and sort method out of the value object
    // and set the corresponding UI widgets — enabling the secondary sort
    // method combo only when weighted is selected.
    /**
     * Initializes the attribute combo, base DN widget, sort method combo,
     * and secondary sort method combo from the current {@link OlcValSortValue}.
     * Enables the secondary sort method combo only when the weighted sort method
     * is selected.
     */
    private void initFromValue()
    {
        // Attribute
        String attribute = value.getAttribute();

        if ( attribute != null )
        {
            attributeComboViewer.getCombo().setText( attribute );
        }
        else
        {
            attributeComboViewer.getCombo().setText( "" );
        }

        // Base DN
        Dn baseDn = value.getBaseDn();

        if ( baseDn != null )
        {
            baseDnEntryWidget.setInput( baseDn );
        }
        else
        {
            baseDnEntryWidget.setInput( Dn.EMPTY_DN );
        }

        // Sort Method
        if ( value.isWeighted() )
        {
            sortMethodComboViewer.setSelection( new StructuredSelection( WEIGHTED_OPTION ) );
        }
        else
        {
            OlcValSortMethodEnum secondarySortMethod = value.getSortMethod();

            if ( secondarySortMethod != null )
            {
                sortMethodComboViewer.setSelection( new StructuredSelection( secondarySortMethod ) );
            }
            else
            {
                sortMethodComboViewer
                    .setSelection( new StructuredSelection( OlcValSortMethodEnum.ALPHA_ASCEND ) );
            }
        }

        // Secondary Sort Method
        if ( value.isWeighted() )
        {
            OlcValSortMethodEnum secondarySortMethod = value.getSortMethod();

            if ( secondarySortMethod != null )
            {
                secondarySortMethodComboViewer.setSelection( new StructuredSelection( secondarySortMethod ) );
            }
            else
            {
                secondarySortMethodComboViewer.setSelection( new StructuredSelection( NONE_OPTION ) );
            }
        }
        else
        {
            secondarySortMethodComboViewer.setSelection( new StructuredSelection( NONE_OPTION ) );
            secondarySortMethodComboViewer.getControl().setEnabled( false );
        }
    }


    // Like checking whether both the target attribute and the base DN
    // are specified before the hologram operator is allowed to finalize
    // and transmit the directive, we evaluate those two required fields
    // and enable or disable the OK button accordingly.
    /**
     * Evaluates whether the OK button should be enabled. We require a
     * non-empty attribute name and a non-null, non-empty base DN.
     */
    private void checkAndUpdateOkButtonEnableState()
    {
        boolean enableOkButton = true;

        // Attribute
        String attribute = attributeComboViewer.getCombo().getText();

        if ( ( attribute == null ) || ( attribute.isEmpty() ) )
        {
            enableOkButton = false;
        }

        // Base DN
        if ( enableOkButton )
        {
            Dn baseDn = baseDnEntryWidget.getDn();

            if ( ( baseDn == null ) || ( Dn.EMPTY_DN.equals( baseDn ) ) )
            {
                enableOkButton = false;
            }
        }

        okButton.setEnabled( enableOkButton );
    }


    // Like reading which sort method the operator highlighted on the briefing
    // form so the rest of the logic knows how to order the attribute values,
    // we return the currently selected sort method object from the combo.
    /**
     * Returns the currently selected sort method object from the primary sort
     * method combo — either a {@link OlcValSortMethodEnum} constant or the
     * {@code WEIGHTED_OPTION} string — or {@code null} if nothing is selected.
     *
     * @return the selected sort method object, or {@code null}
     */
    private Object getSelectedSortMethod()
    {
        StructuredSelection selection = ( StructuredSelection ) sortMethodComboViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            return selection.getFirstElement();
        }

        return null;
    }


    // Like reading the secondary sort method from the briefing form when
    // weighted sorting is in play so the caller knows which fallback method
    // to apply, we return the current secondary combo selection.
    /**
     * Returns the currently selected secondary sort method object from the
     * secondary sort method combo — either a {@link OlcValSortMethodEnum}
     * constant or the {@code NONE_OPTION} string — or {@code null} if
     * nothing is selected.
     *
     * @return the selected secondary sort method object, or {@code null}
     */
    private Object getSelectedSecondarySortMethod()
    {
        StructuredSelection selection = ( StructuredSelection ) secondarySortMethodComboViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            return selection.getFirstElement();
        }

        return null;
    }


    // Like handing the completed value-sorting directive back to the overlay
    // editor so it can be stored in the configuration, we return the final
    // string representation of the edited value.
    /**
     * Returns the string representation of the edited value-sorting rule,
     * as produced by {@link OlcValSortValue#toString()}.
     *
     * @return the value-sorting rule string
     */
    public String getValue()
    {
        return value.toString();
    }

    // Like a Rebellion translator who knows the human-readable name for
    // every sort method enum constant, we extend LabelProvider to turn
    // OlcValSortMethodEnum values into display strings for the combos.
    /**
     * A {@link LabelProvider} for {@link OlcValSortMethodEnum} objects that
     * returns a human-readable label for each sort method constant.
     */
    private class OlcValSortMethodEnumLabelProvider extends LabelProvider
    {
        // Like the translator delivering the proper display name for each
        // sort method constant so the operator reads meaningful labels
        // instead of raw enum names, we switch on the enum value and
        // return the corresponding human-friendly string.
        /**
         * Returns a human-readable label for the given sort method object.
         * Falls back to the default label provider for unrecognized types.
         *
         * @param element the sort method object to label
         * @return the human-readable label string
         */
        @Override
        public String getText( Object element )
        {
            if ( element instanceof OlcValSortMethodEnum )
            {
                OlcValSortMethodEnum sortMethod = ( OlcValSortMethodEnum ) element;

                switch ( sortMethod )
                {
                    case ALPHA_ASCEND:
                        return "Alpha Ascendant";
                    case ALPHA_DESCEND:
                        return "Alpha Descendant";
                    case NUMERIC_ASCEND:
                        return "Numeric Ascendant";
                    case NUMERIC_DESCEND:
                        return "Numeric Descendant";
                }
            }

            return super.getText( element );
        }
    }
}
