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
package org.apache.directory.studio.schemaeditor.view.wizards;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.dialogs.AttributeTypeSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;


// ── CLASS: NewObjectClassOptionalAttributesPage — Lando Running Cloud City ────────────
// Lando Calrissian runs Cloud City on his own terms — some services are included in the
// deal, others are perks he offers at his discretion. "You have my word," he tells Vader,
// about the mandatory stuff. But the luxury suites, the tibanna gas upgrades, the fine
// dining? Those are optional extras he can offer or withhold depending on the situation.
// Optional attributes in LDAP work the same way: entries of this object class MAY include
// them, but nobody's going to reject the entry if they're missing.
// This page is where we declare which attributes are available but not required.
// ──────────────────────────────────────────────────────────────────────────────────────
/**
 * The fifth and final wizard page in the New Object Class wizard, specifying optional attribute types.
 * Attributes added here may be present in entries of this object class, but their absence
 * will not cause the LDAP server to reject the entry.
 * Think of Lando's optional Cloud City perks: the entry works fine without them, but
 * having them available makes the object class more expressive and flexible.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewObjectClassOptionalAttributesPage extends WizardPage
{
    /** The optional attribute types list */
    private List<AttributeType> optionalAttributeTypesList;

    // UI Fields
    private TableViewer optionalAttributeTypesTableViewer;
    private Button optionalAttributeTypesAddButton;
    private Button optionalAttributeTypesRemoveButton;


    // ── Lando Opens The Cloud City Welcome Center ─────────────────────────────────────
    // Lando greets arrivals with a wide smile, explains what Cloud City offers, and opens
    // the amenities brochure — which starts empty until someone requests an upgrade.
    // We set the page title, description, and image, and initialize the empty optional list.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs this wizard page and sets its title, description, and image.
     * We initialize the empty {@code optionalAttributeTypesList} that the user will populate
     * by adding attribute types through this page's UI.
     */
    protected NewObjectClassOptionalAttributesPage()
    {
        super( "NewObjectClassOptionalAttributesPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewObjectClassOptionalAttributesPage.OptionalAttributeTypes" ) ); //$NON-NLS-1$
        setDescription( Messages
            .getString( "NewObjectClassOptionalAttributesPage.SpecifiyOptionalAttributeTypesForObjectClass" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_OBJECT_CLASS_NEW_WIZARD ) );
        optionalAttributeTypesList = new ArrayList<AttributeType>();
    }


    // ── Lando Lays Out The Optional Amenities Menu ────────────────────────────────────
    // Lando displays the full amenities menu on the welcome desk: a scrollable list of
    // everything Cloud City can offer, plus buttons to add or remove items at will.
    // We build the table viewer and its Add/Remove buttons here and wire up their listeners.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT widgets for this page: a table viewer for the optional attribute types
     * and Add/Remove buttons to manage the list.
     * Eclipse calls this once when the page first becomes visible.
     *
     * @param parent  the parent composite Eclipse provides — we nest our layout inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Optional Attribute Types Group
        Group optionalAttributeTypesGroup = new Group( composite, SWT.NONE );
        optionalAttributeTypesGroup.setText( Messages
            .getString( "NewObjectClassOptionalAttributesPage.OptionalAttributeTypes" ) ); //$NON-NLS-1$
        optionalAttributeTypesGroup.setLayout( new GridLayout( 2, false ) );
        optionalAttributeTypesGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Optional Attribute Types
        Table optionalAttributeTypesTable = new Table( optionalAttributeTypesGroup, SWT.BORDER );
        GridData optionalAttributeTypesTableGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 2 );
        optionalAttributeTypesTableGridData.heightHint = 100;
        optionalAttributeTypesTable.setLayoutData( optionalAttributeTypesTableGridData );
        optionalAttributeTypesTableViewer = new TableViewer( optionalAttributeTypesTable );
        optionalAttributeTypesTableViewer.setContentProvider( new ArrayContentProvider() );
        optionalAttributeTypesTableViewer.setLabelProvider( new LabelProvider()
        {
            public Image getImage( Object element )
            {
                if ( element instanceof AttributeType )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_ATTRIBUTE_TYPE );
                }

                // Default
                return super.getImage( element );
            }


            public String getText( Object element )
            {
                if ( element instanceof AttributeType )
                {
                    AttributeType at = ( AttributeType ) element;

                    List<String> names = at.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        return NLS
                            .bind(
                                Messages.getString( "NewObjectClassOptionalAttributesPage.AliasOID" ), new String[] { ViewUtils.concateAliases( names ), at.getOid() } ); //$NON-NLS-1$
                    }
                    else
                    {
                        return NLS
                            .bind(
                                Messages.getString( "NewObjectClassOptionalAttributesPage.NoneOID" ), new String[] { at.getOid() } ); //$NON-NLS-1$
                    }
                }
                // Default
                return super.getText( element );
            }
        } );
        optionalAttributeTypesTableViewer.setInput( optionalAttributeTypesList );
        optionalAttributeTypesTableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                optionalAttributeTypesRemoveButton.setEnabled( !event.getSelection().isEmpty() );
            }
        } );
        optionalAttributeTypesAddButton = new Button( optionalAttributeTypesGroup, SWT.PUSH );
        optionalAttributeTypesAddButton.setText( Messages.getString( "NewObjectClassOptionalAttributesPage.Add" ) ); //$NON-NLS-1$
        optionalAttributeTypesAddButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );
        optionalAttributeTypesAddButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                addOptionalAttributeType();
            }
        } );
        optionalAttributeTypesRemoveButton = new Button( optionalAttributeTypesGroup, SWT.PUSH );
        optionalAttributeTypesRemoveButton
            .setText( Messages.getString( "NewObjectClassOptionalAttributesPage.Remove" ) ); //$NON-NLS-1$
        optionalAttributeTypesRemoveButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );
        optionalAttributeTypesRemoveButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                removeOptionalAttributeType();
            }
        } );
        optionalAttributeTypesRemoveButton.setEnabled( false );

        setControl( composite );
    }


    // ── Lando Adds A New Amenity To The Cloud City Menu ──────────────────────────────
    // "Interested in a tibanna gas upgrade? Allow me to add that to your suite package."
    // Lando personally signs off on each addition to the optional amenities list.
    // We open the attribute type selection dialog, skip any already-optional types, and
    // add the user's selection to the optional list.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the attribute type selection dialog so the user can add an optional attribute.
     * We pass the currently optional list as hidden items so duplicates can't be added.
     * If the user confirms, we add the selection to the list and refresh the table.
     */
    private void addOptionalAttributeType()
    {
        AttributeTypeSelectionDialog dialog = new AttributeTypeSelectionDialog();
        List<AttributeType> hiddenAttributes = new ArrayList<AttributeType>();
        hiddenAttributes.addAll( optionalAttributeTypesList );
        dialog.setHiddenAttributeTypes( hiddenAttributes );
        if ( dialog.open() == Dialog.OK )
        {
            optionalAttributeTypesList.add( dialog.getSelectedAttributeType() );
            updateOptionalAttributeTypesTableTable();
        }
    }


    // ── Lando Removes An Amenity From The Suite Package ───────────────────────────────
    // "I'm afraid I've had to alter the deal. The tibanna upgrade is no longer available."
    // Lando removes the selected optional perk from the package — the guest won't miss it
    // because it was never required.
    // We remove the selected attribute type and refresh the table.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected attribute type from the optional list and refreshes
     * the table viewer.
     * The Remove button is disabled when nothing is selected, but we guard defensively anyway.
     */
    private void removeOptionalAttributeType()
    {
        StructuredSelection selection = ( StructuredSelection ) optionalAttributeTypesTableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            optionalAttributeTypesList.remove( selection.getFirstElement() );
            updateOptionalAttributeTypesTableTable();
        }
    }


    // ── Lando Re-Alphabetizes The Amenities Menu After Every Change ───────────────────
    // After any addition or removal, Lando has his staff re-sort the amenities brochure
    // alphabetically so it looks professional when guests browse it.
    // We sort and repaint the optional attribute types table after every edit.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sorts the optional attribute types list alphabetically by first name and tells the
     * table viewer to repaint.
     * Called after every add or remove to keep the list consistently ordered.
     */
    private void updateOptionalAttributeTypesTableTable()
    {
        Collections.sort( optionalAttributeTypesList, new Comparator<AttributeType>()
        {
            public int compare( AttributeType o1, AttributeType o2 )
            {
                List<String> at1Names = o1.getNames();
                List<String> at2Names = o2.getNames();

                if ( ( at1Names != null ) && ( at2Names != null ) && ( at1Names.size() > 0 ) && ( at2Names.size() > 0 ) )
                {
                    return at1Names.get( 0 ).compareToIgnoreCase( at2Names.get( 0 ) );
                }

                // Default
                return 0;
            }
        } );

        optionalAttributeTypesTableViewer.refresh();
    }


    // ── Lando Presents The Full Optional Amenities Dossier ────────────────────────────
    // At checkout, Lando hands over the complete list of optional amenities the guest
    // selected — the full dossier, not just the names, because the wizard needs the objects.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full list of optional {@link AttributeType} objects the user selected.
     * The wizard uses this list at finish time to set the MAY (optional attributes) list
     * on the new ObjectClass.
     *
     * @return  the list of optional AttributeType objects; may be empty.
     */
    public List<AttributeType> getOptionalAttributeTypes()
    {
        return optionalAttributeTypesList;
    }


    // ── Lando Reads Just The Amenity Names For The Checkout Record ────────────────────
    // "For the official record, just the names — not the full price sheets."
    // The wizard needs only names when building the LDAP schema text representation.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns just the names of the optional attribute types as strings.
     * Callers that build the LDAP schema text use names rather than full
     * {@link AttributeType} objects.
     *
     * @return  a list of attribute type name strings; may be empty.
     */
    public List<String> getOptionalAttributeTypesNames()
    {
        List<String> names = new ArrayList<String>();
        for ( AttributeType at : optionalAttributeTypesList )
        {
            names.add( at.getName() );
        }
        return names;
    }
}
