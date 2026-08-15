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
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.common.ui.dialogs.AttributeDialog;
import org.apache.directory.studio.openldap.common.ui.widgets.EntryWidget;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.editor.dialogs.AbstractOverlayDialogConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.OverlayDialog;
import org.apache.directory.studio.openldap.config.model.overlay.OlcRefintConfig;


// Like the Imperial construction crews assembling the referential-integrity
// enforcement module onto the second Death Star — wiring up the attributes
// roster that must stay consistent across deletes and renames, the placeholder
// DN that replaces dangling references, and the modifier-name identity used
// when the overlay writes its housekeeping changes — we build the configuration
// block that governs how the Referential Integrity overlay keeps the directory clean.
/**
 * This class implements the configuration block for the Referential Integrity
 * overlay. We present a table of watched attributes (with Add and Delete
 * buttons), a placeholder-value DN picker for replacing stale references, and
 * a modifier-name DN picker, and we read/write all of these to and from the
 * {@link OlcRefintConfig} model object on refresh and save.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReferentialIntegrityOverlayConfigurationBlock extends
    AbstractOverlayDialogConfigurationBlock<OlcRefintConfig>
{
    /** The default modifier name */
    private static final String DEFAULT_MODIFIER_NAME = "cn=Referential Integrity Overlay";

    /** The attributes list */
    private List<String> attributes = new ArrayList<>();

    // UI widgets
    private TableViewer attributesTableViewer;
    private Button addAttributeButton;
    private Button deleteAttributeButton;
    private EntryWidget placeholderValueEntryWidget;
    private EntryWidget modifierNameEntryWidget;

    // Listeners
    private ISelectionChangedListener attributesTableViewerSelectionChangedListener =  event ->
        deleteAttributeButton.setEnabled( !attributesTableViewer.getSelection().isEmpty() );

    private SelectionListener addAttributeButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            AttributeDialog dialog = new AttributeDialog( addAttributeButton.getShell(), browserConnection );
            if ( dialog.open() == AttributeDialog.OK )
            {
                String attribute = dialog.getAttribute();

                if ( !attributes.contains( attribute ) )
                {
                    attributes.add( attribute );
                    attributesTableViewer.refresh();
                    attributesTableViewer.setSelection( new StructuredSelection( attribute ) );
                }
            }
        }
    };
    private SelectionListener deleteAttributeButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            StructuredSelection selection = ( StructuredSelection ) attributesTableViewer.getSelection();

            if ( !selection.isEmpty() )
            {
                String selectedAttribute = ( String ) selection.getFirstElement();

                attributes.remove( selectedAttribute );
                attributesTableViewer.refresh();
            }
        }
    };


    // Like the crew initializing a fresh referential-integrity module with
    // no watched attributes, no placeholder DN, and the default modifier
    // name — ready to be configured by the administrator — we create the
    // block with a new empty OlcRefintConfig and an empty attributes list.
    /**
     * Creates a new ReferentialIntegrityOverlayConfigurationBlock with a fresh,
     * empty {@link OlcRefintConfig} as the backing model and an empty attributes list.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param connection the browser connection used for schema and DN lookups
     */
    public ReferentialIntegrityOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection connection )
    {
        super( dialog, connection );
        setOverlay( new OlcRefintConfig() );
    }


    // Like the crew installing a pre-configured referential-integrity module
    // that already knows which attributes to watch and which placeholder DN
    // to substitute for broken references, we accept an existing config and
    // store it — falling back to a fresh one if null was passed.
    /**
     * Creates a new ReferentialIntegrityOverlayConfigurationBlock backed by the
     * given {@link OlcRefintConfig}. If {@code overlay} is {@code null} we
     * create a fresh default config instead.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param connection the browser connection used for schema and DN lookups
     * @param overlay the existing referential-integrity overlay config to edit, or {@code null}
     */
    public ReferentialIntegrityOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection connection,
        OlcRefintConfig overlay )
    {
        super( dialog, connection );

        if ( overlay == null )
        {
            setOverlay( new OlcRefintConfig() );
        }
        else
        {
            setOverlay( overlay );
        }
    }


    // Like the construction crew building the referential-integrity module's
    // control panel — an attributes roster with Add and Delete buttons, a
    // DN picker for the placeholder value that fills in for deleted entries,
    // and a DN picker for the modifier name identity — we create all the widgets.
    /**
     * Creates the block content area with an attributes {@link TableViewer}
     * (with Add and Delete buttons), a "Placeholder Value" DN entry widget,
     * and a "Modifier's Name" DN entry widget.
     *
     * @param parent the parent composite to attach our content to
     */
    public void createBlockContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );

        // Attributes
        BaseWidgetUtils.createLabel( composite, "Attributes:", 1 );
        Composite attributesComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        // Attributes TableViewer
        attributesTableViewer = new TableViewer( attributesComposite );
        GridData tableViewerGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 3 );
        tableViewerGridData.heightHint = 20;
        tableViewerGridData.widthHint = 100;
        attributesTableViewer.getControl().setLayoutData( tableViewerGridData );
        attributesTableViewer.setContentProvider( new ArrayContentProvider() );
        attributesTableViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public Image getImage( Object element )
            {
                return OpenLdapConfigurationPlugin.getDefault().getImage(
                    OpenLdapConfigurationPluginConstants.IMG_ATTRIBUTE );
            }
        } );
        attributesTableViewer.setInput( attributes );
        attributesTableViewer.addSelectionChangedListener( attributesTableViewerSelectionChangedListener );

        // Attribute Add Button
        addAttributeButton = BaseWidgetUtils.createButton( attributesComposite, "Add...", 1 );
        addAttributeButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        addAttributeButton.addSelectionListener( addAttributeButtonSelectionListener );

        // Attribute Delete Button
        deleteAttributeButton = BaseWidgetUtils.createButton( attributesComposite, "Delete", 1 );
        deleteAttributeButton.setEnabled( false );
        deleteAttributeButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        deleteAttributeButton.addSelectionListener( deleteAttributeButtonSelectionListener );

        // Placeholder Value
        BaseWidgetUtils.createLabel( composite, "Placeholder Value:", 1 );
        placeholderValueEntryWidget = new EntryWidget( getDialog().getBrowserConnection() );
        placeholderValueEntryWidget.createWidget( composite );
        placeholderValueEntryWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Modifier Name
        BaseWidgetUtils.createLabel( composite, "Modifier's Name:", 1 );
        modifierNameEntryWidget = new EntryWidget( getDialog().getBrowserConnection() );
        modifierNameEntryWidget.createWidget( composite );
        modifierNameEntryWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // Like the crew reading the referential-integrity module's current settings
    // out of the configuration record and populating the control panel —
    // loading watched attributes into the roster, setting the placeholder DN,
    // and defaulting the modifier name to the standard overlay identity —
    // we push each overlay value into its corresponding widget.
    /**
     * Refreshes the block widgets from the current {@link OlcRefintConfig},
     * populating the attributes table, the placeholder-value DN, and the
     * modifier-name DN. When the modifier name is absent we default it to
     * {@value #DEFAULT_MODIFIER_NAME}.
     */
    public void refresh()
    {
        if ( overlay != null )
        {
            // Attributes
            List<String> attributeValues = overlay.getOlcRefintAttribute();

            if ( ( attributeValues != null ) && attributeValues.isEmpty() )
            {
                for ( String attribute : attributeValues )
                {
                    attributes.add( attribute );
                }
            }

            attributesTableViewer.refresh();

            // Placeholder Value
            Dn placeholderValue = overlay.getOlcRefintNothing();

            if ( placeholderValue != null )
            {
                placeholderValueEntryWidget.setInput( placeholderValue );
            }
            else
            {
                placeholderValueEntryWidget.setInput( Dn.EMPTY_DN );
            }

            // Modifier Name
            Dn modifierName = overlay.getOlcRefintModifiersName();

            if ( modifierName != null )
            {
                modifierNameEntryWidget.setInput( modifierName );
            }
            else
            {
                try
                {
                    modifierNameEntryWidget.setInput( new Dn( DEFAULT_MODIFIER_NAME ) );
                }
                catch ( LdapInvalidDnException e )
                {
                    // Nothing to do.
                }
            }
        }
    }


    // Like the crew writing the updated referential-integrity settings back
    // into the station's configuration record — saving the attributes list,
    // the placeholder DN, and the modifier name (clearing fields that match
    // the empty or default values so the overlay uses its built-in defaults) —
    // we persist every widget value into the OlcRefintConfig model.
    /**
     * Saves the current widget values back into the {@link OlcRefintConfig},
     * writing the attributes list, the placeholder-value DN (cleared if empty),
     * and the modifier-name DN (cleared if empty or equal to the default).
     */
    public void save()
    {
        if ( overlay != null )
        {
            // Attributes
            overlay.setOlcRefintAttribute( attributes );

            // Placeholder Value
            Dn placeholderValue = placeholderValueEntryWidget.getDn();

            if ( ( placeholderValue != null ) && ( !Dn.EMPTY_DN.equals( placeholderValue ) ) )
            {
                overlay.setOlcRefintNothing( placeholderValue );
            }
            else
            {
                overlay.setOlcRefintNothing( null );
            }

            // Modifier Name
            Dn modifierName = modifierNameEntryWidget.getDn();

            if ( ( modifierName != null ) && ( !Dn.EMPTY_DN.equals( modifierName ) )
                && ( !modifierName.toString().equals( DEFAULT_MODIFIER_NAME ) ) )
            {
                overlay.setOlcRefintModifiersName( modifierName );
            }
            else
            {
                overlay.setOlcRefintModifiersName( null );
            }
        }
    }
}
