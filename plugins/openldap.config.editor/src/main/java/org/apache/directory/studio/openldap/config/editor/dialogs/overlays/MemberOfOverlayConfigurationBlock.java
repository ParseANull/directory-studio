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
package org.apache.directory.studio.openldap.config.editor.dialogs.overlays;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.common.ui.widgets.EntryWidget;
import org.apache.directory.studio.openldap.config.editor.dialogs.AbstractOverlayDialogConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.OverlayDialog;
import org.apache.directory.studio.openldap.config.model.overlay.OlcMemberOf;
import org.apache.directory.studio.openldap.config.model.overlay.OlcMemberOfDanglingReferenceBehaviorEnum;


// Like the Imperial construction crews assembling the memberOf back-reference
// module onto the second Death Star — wiring the group object-class selector,
// the group and entry attribute-type pickers, the modifier-name identity, the
// dangling-reference behavior chooser and error-code selector, and the referential-
// integrity toggle — we build the MemberOf overlay configuration block that
// automatically maintains reverse membership pointers on user entries.
/**
 * This class implements the configuration block for the MemberOf overlay.
 * We present combo pickers for group object class, group attribute type,
 * and entry attribute type; a modifier-name DN entry widget; a dangling-reference
 * behavior combo; a dangling-reference error-code combo; and a "Maintain Referential
 * Integrity" checkbox, all of which we read/write to and from the {@link OlcMemberOf}
 * model object on refresh and save.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MemberOfOverlayConfigurationBlock extends AbstractOverlayDialogConfigurationBlock<OlcMemberOf>
{
    /** The connection's attribute types */
    private List<String> connectionAttributeTypes;

    /** The connection's objectClasses */
    private List<String> connectionObjectClasses;

    /** The list of result codes */
    private List<ResultCodeEnum> resultCodes;

    // UI widgets
    private ComboViewer groupObjectClassComboViewer;
    private ComboViewer groupAttributeTypeComboViewer;
    private ComboViewer entryAttributeTypeComboViewer;
    private EntryWidget modifierNameEntryWidget;
    private ComboViewer danglingReferenceBehaviorComboViewer;
    private ComboViewer danglingReferenceErrorCodeComboViewer;
    private Button maintianReferentialIntegrityCheckbox;


    // Like the construction crew powering up a fresh MemberOf module with
    // no prior configuration, cataloguing all the schema types from the
    // connection so the combo pickers are populated, we create the block
    // with a new empty OlcMemberOf and call init() to fill the lists.
    /**
     * Creates a new MemberOfOverlayConfigurationBlock with a fresh, empty
     * {@link OlcMemberOf} as the backing model and initializes the attribute
     * type, object class, and result code lists from the browser connection's schema.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param browserConnection the browser connection used for schema lookups
     */
    public MemberOfOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection browserConnection )
    {
        super( dialog, browserConnection );
        setOverlay( new OlcMemberOf() );

        init();
    }


    // Like the crew installing a pre-configured MemberOf module that already
    // has group-class and attribute-type settings from a previous deployment,
    // we accept an existing OlcMemberOf and store it — defaulting to a fresh
    // one if null — then still call init() to populate the schema lists.
    /**
     * Creates a new MemberOfOverlayConfigurationBlock backed by the given
     * {@link OlcMemberOf}. If {@code overlay} is {@code null} we create a
     * fresh default config instead. Either way, the schema attribute-type,
     * object-class, and result-code lists are initialized from the connection.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param browserConnection the browser connection used for schema lookups
     * @param overlay the existing MemberOf overlay config to edit, or {@code null}
     */
    public MemberOfOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection browserConnection,
        OlcMemberOf overlay )
    {
        super( dialog, browserConnection );

        if ( overlay == null )
        {
            setOverlay( new OlcMemberOf() );
        }
        else
        {
            setOverlay( overlay );
        }

        init();
    }


    // Like the crew running the initial systems check that populates the
    // attribute-type, object-class, and result-code lookup tables so the
    // combo pickers in the UI have data to offer, we call both init helpers.
    /**
     * Initializes the list of attribute types, object classes, and result codes
     * by delegating to {@link #initAttributeTypesAndObjectClassesLists()} and
     * {@link #initResultCodesList()}.
     */
    private void init()
    {
        initAttributeTypesAndObjectClassesLists();
        initResultCodesList();
    }


    // Like the crew querying the station's schema catalog to build sorted
    // name lists for all known attribute types and object classes so the
    // combo pickers can offer them as auto-complete suggestions, we walk
    // the browser connection's schema and collect every name alphabetically.
    /**
     * Populates {@code connectionAttributeTypes} and {@code connectionObjectClasses}
     * by querying all attribute type and object class descriptions from the browser
     * connection's schema, collecting every declared name, and sorting both lists
     * case-insensitively. Does nothing when the browser connection is absent.
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
            Comparator<String> ignoreCaseComparator = ( o1, o2 ) ->  o1.compareToIgnoreCase( o2 );

            // Sorting the lists
            Collections.sort( connectionAttributeTypes, ignoreCaseComparator );
            Collections.sort( connectionObjectClasses, ignoreCaseComparator );
        }
    }


    // Like the crew cataloguing every known LDAP result code into a
    // sorted list so the dangling-reference error-code combo can present
    // a complete menu of numeric codes and their human-readable messages,
    // we build and sort the full ResultCodeEnum roster here.
    /**
     * Populates the {@code resultCodes} list with all known {@link ResultCodeEnum}
     * values in ascending numeric order, for use as the data input of the
     * dangling-reference error-code combo viewer.
     */
    private void initResultCodesList()
    {
        // Initializing the list
        resultCodes = new ArrayList<>();

        // Adding all result codes to the list
        resultCodes.add( ResultCodeEnum.SUCCESS );
        resultCodes.add( ResultCodeEnum.PARTIAL_RESULTS );
        resultCodes.add( ResultCodeEnum.COMPARE_FALSE );
        resultCodes.add( ResultCodeEnum.COMPARE_TRUE );
        resultCodes.add( ResultCodeEnum.REFERRAL );
        resultCodes.add( ResultCodeEnum.SASL_BIND_IN_PROGRESS );
        resultCodes.add( ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED );
        resultCodes.add( ResultCodeEnum.STRONG_AUTH_REQUIRED );
        resultCodes.add( ResultCodeEnum.CONFIDENTIALITY_REQUIRED );
        resultCodes.add( ResultCodeEnum.ALIAS_DEREFERENCING_PROBLEM );
        resultCodes.add( ResultCodeEnum.INAPPROPRIATE_AUTHENTICATION );
        resultCodes.add( ResultCodeEnum.INVALID_CREDENTIALS );
        resultCodes.add( ResultCodeEnum.INSUFFICIENT_ACCESS_RIGHTS );
        resultCodes.add( ResultCodeEnum.OPERATIONS_ERROR );
        resultCodes.add( ResultCodeEnum.PROTOCOL_ERROR );
        resultCodes.add( ResultCodeEnum.TIME_LIMIT_EXCEEDED );
        resultCodes.add( ResultCodeEnum.SIZE_LIMIT_EXCEEDED );
        resultCodes.add( ResultCodeEnum.ADMIN_LIMIT_EXCEEDED );
        resultCodes.add( ResultCodeEnum.UNAVAILABLE_CRITICAL_EXTENSION );
        resultCodes.add( ResultCodeEnum.BUSY );
        resultCodes.add( ResultCodeEnum.UNAVAILABLE );
        resultCodes.add( ResultCodeEnum.UNWILLING_TO_PERFORM );
        resultCodes.add( ResultCodeEnum.LOOP_DETECT );
        resultCodes.add( ResultCodeEnum.NO_SUCH_ATTRIBUTE );
        resultCodes.add( ResultCodeEnum.UNDEFINED_ATTRIBUTE_TYPE );
        resultCodes.add( ResultCodeEnum.INAPPROPRIATE_MATCHING );
        resultCodes.add( ResultCodeEnum.CONSTRAINT_VIOLATION );
        resultCodes.add( ResultCodeEnum.ATTRIBUTE_OR_VALUE_EXISTS );
        resultCodes.add( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX );
        resultCodes.add( ResultCodeEnum.NO_SUCH_OBJECT );
        resultCodes.add( ResultCodeEnum.ALIAS_PROBLEM );
        resultCodes.add( ResultCodeEnum.INVALID_DN_SYNTAX );
        resultCodes.add( ResultCodeEnum.NAMING_VIOLATION );
        resultCodes.add( ResultCodeEnum.OBJECT_CLASS_VIOLATION );
        resultCodes.add( ResultCodeEnum.NOT_ALLOWED_ON_NON_LEAF );
        resultCodes.add( ResultCodeEnum.NOT_ALLOWED_ON_RDN );
        resultCodes.add( ResultCodeEnum.ENTRY_ALREADY_EXISTS );
        resultCodes.add( ResultCodeEnum.OBJECT_CLASS_MODS_PROHIBITED );
        resultCodes.add( ResultCodeEnum.AFFECTS_MULTIPLE_DSAS );
        resultCodes.add( ResultCodeEnum.OTHER );
        resultCodes.add( ResultCodeEnum.CANCELED );
        resultCodes.add( ResultCodeEnum.NO_SUCH_OPERATION );
        resultCodes.add( ResultCodeEnum.TOO_LATE );
        resultCodes.add( ResultCodeEnum.CANNOT_CANCEL );
        resultCodes.add( ResultCodeEnum.UNKNOWN );

        // Sorting the list
        Collections.sort( resultCodes, ( o1, o2 ) -> Integer.compare( o1.getResultCode(), o2.getResultCode() ) );
    }


    // Like the construction crew building the MemberOf module's full control
    // panel — combo pickers for group object class, group attribute type, and
    // entry attribute type; a DN picker for the modifier name; a dangling-reference
    // behavior combo with a custom Ignore/Drop/Error label provider; a dangling-
    // reference error-code combo showing "code (message)" labels; and a
    // referential-integrity checkbox — we create all block content widgets here.
    /**
     * Creates the block content area with two-column rows for Group Object Class,
     * Group Attribute Type, Entry Attribute Type, Modifier's Name, Dangling Ref.
     * Behavior (with Ignore/Drop/Error choices), Dangling Ref. Error Code (with
     * numeric code and message labels), and a "Maintain Referential Integrity"
     * checkbox spanning both columns.
     *
     * @param parent the parent composite to attach our content to
     */
    public void createBlockContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );

        // Group Object Class
        BaseWidgetUtils.createLabel( composite, "Group Object Class:", 1 );
        groupObjectClassComboViewer = new ComboViewer( new Combo( composite, SWT.DROP_DOWN ) );
        groupObjectClassComboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        groupObjectClassComboViewer.setContentProvider( new ArrayContentProvider() );
        groupObjectClassComboViewer.setInput( connectionObjectClasses );

        // Group Attribute Type
        BaseWidgetUtils.createLabel( composite, "Group Attribute Type:", 1 );
        groupAttributeTypeComboViewer = new ComboViewer( new Combo( composite, SWT.DROP_DOWN ) );
        groupAttributeTypeComboViewer.getControl()
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        groupAttributeTypeComboViewer.setContentProvider( new ArrayContentProvider() );
        groupAttributeTypeComboViewer.setInput( connectionAttributeTypes );

        // Entry Attribute Type
        BaseWidgetUtils.createLabel( composite, "Entry Attribute Type:", 1 );
        entryAttributeTypeComboViewer = new ComboViewer( new Combo( composite, SWT.DROP_DOWN ) );
        entryAttributeTypeComboViewer.getControl()
            .setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        entryAttributeTypeComboViewer.setContentProvider( new ArrayContentProvider() );
        entryAttributeTypeComboViewer.setInput( connectionAttributeTypes );

        // Modifier Name
        BaseWidgetUtils.createLabel( composite, "Modifier's Name:", 1 );
        modifierNameEntryWidget = new EntryWidget( browserConnection );
        modifierNameEntryWidget.createWidget( composite );
        modifierNameEntryWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Dangling Reference Behavior
        BaseWidgetUtils.createLabel( composite, "Dangling Ref. Behavior:", 1 );
        danglingReferenceBehaviorComboViewer = new ComboViewer( composite );
        danglingReferenceBehaviorComboViewer.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.NONE, true, false ) );
        danglingReferenceBehaviorComboViewer.setContentProvider( new ArrayContentProvider() );
        danglingReferenceBehaviorComboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof OlcMemberOfDanglingReferenceBehaviorEnum )
                {
                    OlcMemberOfDanglingReferenceBehaviorEnum behavior = ( OlcMemberOfDanglingReferenceBehaviorEnum ) element;

                    switch ( behavior )
                    {
                        case IGNORE:
                            return "Ignore";
                        case DROP:
                            return "Drop";
                        case ERROR:
                            return "Error";
                    }
                }

                return super.getText( element );
            }
        } );
        danglingReferenceBehaviorComboViewer.setInput( new OlcMemberOfDanglingReferenceBehaviorEnum[]
            {
                OlcMemberOfDanglingReferenceBehaviorEnum.IGNORE,
                OlcMemberOfDanglingReferenceBehaviorEnum.DROP,
                OlcMemberOfDanglingReferenceBehaviorEnum.ERROR
        } );

        // Dangling Reference Error Code
        BaseWidgetUtils.createLabel( composite, "Dangling Ref. Error Code:", 1 );
        danglingReferenceErrorCodeComboViewer = new ComboViewer( composite );
        danglingReferenceErrorCodeComboViewer.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.NONE, true, false ) );
        danglingReferenceErrorCodeComboViewer.setContentProvider( new ArrayContentProvider() );
        danglingReferenceErrorCodeComboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof ResultCodeEnum )
                {
                    ResultCodeEnum resultCode = ( ResultCodeEnum ) element;

                    return NLS.bind( "{0} ({1})", new Object[]
                        { resultCode.getResultCode(), resultCode.getMessage() } );
                }

                return super.getText( element );
            }
        } );
        danglingReferenceErrorCodeComboViewer.setInput( resultCodes );

        // Maintain Referential Integrity
        maintianReferentialIntegrityCheckbox = BaseWidgetUtils.createCheckbox( composite,
            "Maintain Referential Integrity", 2 );
    }


    // Like the crew reading the MemberOf module's current settings out of
    // the station's configuration record and populating each control on the
    // panel — the group class, the attribute type pickers, the modifier DN,
    // the dangling-reference behavior and error code, and the referential-
    // integrity flag — we push each overlay value into its corresponding widget.
    /**
     * Refreshes all block widgets from the current {@link OlcMemberOf}, setting
     * the group object class, group attribute type, entry attribute type,
     * modifier-name DN, dangling-reference behavior (defaulting to IGNORE),
     * dangling-reference error code (defaulting to CONSTRAINT_VIOLATION), and
     * the referential-integrity checkbox.
     */
    public void refresh()
    {
        if ( overlay != null )
        {
            // Group Object Class
            setComboViewerText( groupObjectClassComboViewer, overlay.getOlcMemberOfGroupOC() );

            // Group Attribute Type
            setComboViewerText( groupAttributeTypeComboViewer, overlay.getOlcMemberOfMemberAD() );

            // Entry Attribute Type
            setComboViewerText( entryAttributeTypeComboViewer, overlay.getOlcMemberOfMemberOfAD() );

            // Modifier Name
            Dn modifierName = overlay.getOlcMemberOfDN();

            if ( modifierName != null )
            {
                modifierNameEntryWidget.setInput( modifierName );
            }
            else
            {
                modifierNameEntryWidget.setInput( Dn.EMPTY_DN );
            }

            // Dangling Reference Behavior
            String danglingReferenceBehaviorString = overlay.getOlcMemberOfDangling();

            if ( danglingReferenceBehaviorString != null )
            {
                OlcMemberOfDanglingReferenceBehaviorEnum danglingReferenceBehavior = OlcMemberOfDanglingReferenceBehaviorEnum
                    .fromString( danglingReferenceBehaviorString );

                if ( danglingReferenceBehavior != null )
                {
                    danglingReferenceBehaviorComboViewer.setSelection( new StructuredSelection(
                        danglingReferenceBehavior ) );
                }
                else
                {
                    danglingReferenceBehaviorComboViewer.setSelection( new StructuredSelection(
                        OlcMemberOfDanglingReferenceBehaviorEnum.IGNORE ) );
                }
            }
            else
            {
                danglingReferenceBehaviorComboViewer.setSelection( new StructuredSelection(
                    OlcMemberOfDanglingReferenceBehaviorEnum.IGNORE ) );
            }

            // Dangling Reference Error Code
            String danglingReferenceErrorCode = overlay.getOlcMemberOfDanglingError();

            if ( danglingReferenceErrorCode != null )
            {
                try
                {
                    // Getting the error code as a ResultCodeEnum value
                    ResultCodeEnum resultCode = ResultCodeEnum.getResultCode( Integer
                        .parseInt( danglingReferenceErrorCode ) );

                    danglingReferenceErrorCodeComboViewer.setSelection( new StructuredSelection( resultCode ) );

                }
                catch ( NumberFormatException e )
                {
                    // The error code is not an int value
                    danglingReferenceErrorCodeComboViewer.setSelection( new StructuredSelection(
                        ResultCodeEnum.CONSTRAINT_VIOLATION ) );
                }
            }
            else
            {
                danglingReferenceErrorCodeComboViewer.setSelection( new StructuredSelection(
                    ResultCodeEnum.CONSTRAINT_VIOLATION ) );
            }

            // Maintain Referential Integrity
            Boolean maintainReferentialIntegrity = overlay.getOlcMemberOfRefInt();

            if ( maintainReferentialIntegrity != null )
            {
                maintianReferentialIntegrityCheckbox.setSelection( maintainReferentialIntegrity );
            }
            else
            {
                maintianReferentialIntegrityCheckbox.setSelection( false );
            }
        }
    }


    // Like the crew writing all the updated MemberOf settings back into the
    // station's configuration record — clearing fields that are empty or equal
    // to the overlay's built-in defaults so the model stays clean, and saving
    // everything else — we persist every widget value into the OlcMemberOf model.
    /**
     * Saves the current widget values back into the {@link OlcMemberOf}, writing
     * group object class, group attribute type, entry attribute type, modifier-name
     * DN (cleared when empty), dangling-reference behavior (cleared when null),
     * dangling-reference error code (cleared when CONSTRAINT_VIOLATION or absent),
     * and the referential-integrity flag.
     */
    public void save()
    {
        if ( overlay != null )
        {
            // Group Object Class
            String groupObjectClass = getComboViewerText( groupObjectClassComboViewer );

            if ( ( groupObjectClass != null ) && ( !groupObjectClass.isEmpty() ) )
            {
                overlay.setOlcMemberOfGroupOC( groupObjectClass );
            }
            else
            {
                overlay.setOlcMemberOfGroupOC( null );
            }

            // Group Attribute Type
            String groupAttributeType = getComboViewerText( groupAttributeTypeComboViewer );

            if ( ( groupAttributeType != null ) && ( !groupAttributeType.isEmpty() ) )
            {
                overlay.setOlcMemberOfMemberAD( groupAttributeType );
            }
            else
            {
                overlay.setOlcMemberOfMemberAD( null );
            }

            // Entry Attribute Type
            String entryAttributeType = getComboViewerText( entryAttributeTypeComboViewer );

            if ( ( entryAttributeType != null ) && ( !entryAttributeType.isEmpty() ) )
            {
                overlay.setOlcMemberOfMemberOfAD( entryAttributeType );
            }
            else
            {
                overlay.setOlcMemberOfMemberOfAD( null );
            }

            // Modifier Name
            Dn modifierName = modifierNameEntryWidget.getDn();

            if ( ( modifierName != null ) && ( !Dn.EMPTY_DN.equals( modifierName ) ) )
            {
                overlay.setOlcMemberOfDN( modifierName );
            }
            else
            {
                overlay.setOlcMemberOfDN( null );
            }

            // Dangling Reference Behavior
            OlcMemberOfDanglingReferenceBehaviorEnum danglingReferenceBehavior = getSelectedDanglingReferenceBehavior();

            if ( danglingReferenceBehavior != null )
            {
                overlay.setOlcMemberOfDangling( danglingReferenceBehavior.toString() );
            }
            else
            {

                overlay.setOlcMemberOfDangling( null );
            }

            // Dangling Reference Error Code
            ResultCodeEnum danglingReferenceErrorCode = getSelectedDanglingReferenceErrorCode();

            if ( ( danglingReferenceErrorCode != null )
                && ( !ResultCodeEnum.CONSTRAINT_VIOLATION.equals( danglingReferenceErrorCode ) ) )
            {
                overlay.setOlcMemberOfDanglingError( Integer.toString( danglingReferenceErrorCode.getResultCode() ) );
            }
            else
            {
                overlay.setOlcMemberOfDanglingError( null );
            }

            // Maintain Referential Integrity
            overlay.setOlcMemberOfRefInt( maintianReferentialIntegrityCheckbox.getSelection() );

        }
    }


    // Like the crew reading the current text out of a combo picker's editable
    // field so it can be stored back into the model — whether the operator
    // selected from the drop-down list or typed a custom value directly —
    // we pull the raw text from the underlying Combo widget.
    /**
     * Returns the raw text currently displayed in the given {@link ComboViewer}'s
     * underlying {@link Combo} widget, including any user-typed custom value.
     *
     * @param viewer the combo viewer whose text to retrieve
     * @return the current text in the combo's edit field
     */
    private String getComboViewerText( ComboViewer viewer )
    {
        return viewer.getCombo().getText();
    }


    // Like the crew pre-loading a combo picker with a value from the model
    // so the operator sees the currently configured setting when the panel
    // first opens — defaulting to an empty string if the model has no value —
    // we set the combo's text directly.
    /**
     * Sets the text displayed in the given {@link ComboViewer}'s underlying
     * {@link Combo} widget to {@code text}, or clears it to an empty string
     * when {@code text} is {@code null}.
     *
     * @param viewer the combo viewer whose text to update
     * @param text the value to display, or {@code null} to clear the field
     */
    private void setComboViewerText( ComboViewer viewer, String text )
    {
        if ( text != null )
        {
            viewer.getCombo().setText( text );
        }
        else
        {
            viewer.getCombo().setText( "" );
        }
    }


    // Like the crew reading which dangling-reference behavior the operator
    // selected from the behavior combo — Ignore, Drop, or Error — and casting
    // the selected element to the expected enum type, we extract the selection.
    /**
     * Returns the currently selected {@link OlcMemberOfDanglingReferenceBehaviorEnum}
     * from the dangling-reference behavior combo viewer, or {@code null} if the
     * selection is empty or not an instance of the expected enum.
     *
     * @return the selected dangling-reference behavior, or {@code null}
     */
    private OlcMemberOfDanglingReferenceBehaviorEnum getSelectedDanglingReferenceBehavior()
    {
        StructuredSelection selection = ( StructuredSelection ) danglingReferenceBehaviorComboViewer.getSelection();

        if ( ( selection != null ) && ( !selection.isEmpty() ) )
        {
            Object firstElement = selection.getFirstElement();

            if ( firstElement instanceof OlcMemberOfDanglingReferenceBehaviorEnum )
            {
                return ( OlcMemberOfDanglingReferenceBehaviorEnum ) firstElement;
            }
            else
            {
                return null;
            }
        }

        return null;
    }


    // Like the crew reading which LDAP result code the operator chose for
    // dangling-reference errors — the numeric code the overlay should return
    // when it detects a broken group reference — we extract the selection from
    // the error-code combo and cast it to ResultCodeEnum.
    /**
     * Returns the currently selected {@link ResultCodeEnum} from the dangling-reference
     * error-code combo viewer, or {@code null} if the selection is empty or not
     * an instance of {@link ResultCodeEnum}.
     *
     * @return the selected dangling-reference error code, or {@code null}
     */
    private ResultCodeEnum getSelectedDanglingReferenceErrorCode()
    {
        StructuredSelection selection = ( StructuredSelection ) danglingReferenceErrorCodeComboViewer.getSelection();

        if ( ( selection != null ) && ( !selection.isEmpty() ) )
        {
            Object firstElement = selection.getFirstElement();

            if ( firstElement instanceof ResultCodeEnum )
            {
                return ( ResultCodeEnum ) firstElement;
            }
            else
            {
                return null;
            }
        }

        return null;
    }
}
