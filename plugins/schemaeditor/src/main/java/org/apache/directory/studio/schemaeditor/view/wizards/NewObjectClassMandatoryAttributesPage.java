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


// ── CLASS: NewObjectClassMandatoryAttributesPage — Clone Troopers Executing Order 66 ──
// Palpatine transmits Order 66 and every clone trooper in the galaxy snaps to it without
// question, without exception. No negotiation, no opt-out — if you're in the Order, you
// execute the command. Period.
// Mandatory attributes in LDAP work the same way: every entry of this object class MUST
// include every attribute on this list, or the LDAP server rejects the entry outright.
// This page is where we declare which attributes are required, non-negotiable, mandatory.
// ──────────────────────────────────────────────────────────────────────────────────────
/**
 * The fourth wizard page in the New Object Class wizard, specifying mandatory attribute types.
 * Any attribute type added here must be present in every LDAP entry of this object class —
 * the server will reject an add or modify that omits them.
 * Think of these as Order 66 for the LDAP entry: every clone (entry) must carry out the
 * order (include the attribute) with no exceptions allowed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewObjectClassMandatoryAttributesPage extends WizardPage
{
    /** The mandatory attribute types list */
    private List<AttributeType> mandatoryAttributeTypesList;

    // UI Fields
    private TableViewer mandatoryAttributeTypesTableViewer;
    private Button mandatoryAttributeTypesAddButton;
    private Button mandatoryAttributeTypesRemoveButton;


    // ── Palpatine Transmits The Order, Clone Ranks Stand Ready ───────────────────────
    // The transmission goes out, the clone battalions assemble, and the roster is initialized
    // blank — no mandatory attributes yet, but the formation is ready to receive orders.
    // We set the page title, description, image, and initialize an empty mandatory list.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs this wizard page and sets its title, description, and image.
     * We also initialize the empty {@code mandatoryAttributeTypesList} that the user will
     * populate by adding attribute types through this page's UI.
     */
    protected NewObjectClassMandatoryAttributesPage()
    {
        super( "NewObjectClassMandatoryAttributesPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewObjectClassMandatoryAttributesPage.MandatoryAttributeTypes" ) ); //$NON-NLS-1$
        setDescription( Messages
            .getString( "NewObjectClassMandatoryAttributesPage.SpecifiyMandatoryAttributeTypeForObjectClass" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_OBJECT_CLASS_NEW_WIZARD ) );
        mandatoryAttributeTypesList = new ArrayList<AttributeType>();
    }


    // ── Palpatine Draws Up The Mandatory Orders Bulletin Board ───────────────────────
    // Palpatine posts the mandatory-orders board: a table listing every required action,
    // plus Add and Remove buttons for amending the list before it's transmitted.
    // We build the table viewer and its two control buttons, then wire up their listeners.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT widgets for this page: a table viewer for the mandatory attribute types
     * list, and Add/Remove buttons to manage its contents.
     * Eclipse calls this once when the page first becomes visible.
     *
     * @param parent  the parent composite Eclipse provides — we nest our layout inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Mandatory Attribute Types Group
        Group mandatoryAttributeTypesGroup = new Group( composite, SWT.NONE );
        mandatoryAttributeTypesGroup.setText( Messages
            .getString( "NewObjectClassMandatoryAttributesPage.MandatoryAttributeTypes" ) ); //$NON-NLS-1$
        mandatoryAttributeTypesGroup.setLayout( new GridLayout( 2, false ) );
        mandatoryAttributeTypesGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Mandatory Attribute Types
        Table mandatoryAttributeTypesTable = new Table( mandatoryAttributeTypesGroup, SWT.BORDER );
        GridData mandatoryAttributeTypesTableGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 2 );
        mandatoryAttributeTypesTableGridData.heightHint = 100;
        mandatoryAttributeTypesTable.setLayoutData( mandatoryAttributeTypesTableGridData );
        mandatoryAttributeTypesTableViewer = new TableViewer( mandatoryAttributeTypesTable );
        mandatoryAttributeTypesTableViewer.setContentProvider( new ArrayContentProvider() );
        mandatoryAttributeTypesTableViewer.setLabelProvider( new LabelProvider()
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
                                Messages.getString( "NewObjectClassMandatoryAttributesPage.AliasOID" ), new String[] { ViewUtils.concateAliases( names ), at.getOid() } ); //$NON-NLS-1$
                    }
                    else
                    {
                        return NLS
                            .bind(
                                Messages.getString( "NewObjectClassMandatoryAttributesPage.NoneOID" ), new String[] { at.getOid() } ); //$NON-NLS-1$
                    }
                }
                // Default
                return super.getText( element );
            }
        } );
        mandatoryAttributeTypesTableViewer.setInput( mandatoryAttributeTypesList );
        mandatoryAttributeTypesTableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                mandatoryAttributeTypesRemoveButton.setEnabled( !event.getSelection().isEmpty() );
            }
        } );
        mandatoryAttributeTypesAddButton = new Button( mandatoryAttributeTypesGroup, SWT.PUSH );
        mandatoryAttributeTypesAddButton.setText( Messages.getString( "NewObjectClassMandatoryAttributesPage.Add" ) ); //$NON-NLS-1$
        mandatoryAttributeTypesAddButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );
        mandatoryAttributeTypesAddButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                addMandatoryAttributeType();
            }
        } );
        mandatoryAttributeTypesRemoveButton = new Button( mandatoryAttributeTypesGroup, SWT.PUSH );
        mandatoryAttributeTypesRemoveButton.setText( Messages
            .getString( "NewObjectClassMandatoryAttributesPage.Remove" ) ); //$NON-NLS-1$
        mandatoryAttributeTypesRemoveButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );
        mandatoryAttributeTypesRemoveButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                removeMandatoryAttributeType();
            }
        } );
        mandatoryAttributeTypesRemoveButton.setEnabled( false );

        setControl( composite );
    }


    // ── Palpatine Adds A New Trooper To The Mandatory Formation ──────────────────────
    // Palpatine calls a new clone battalion into the mandatory formation — "You are now
    // required. Your participation is not optional." The trooper takes their place.
    // We open the attribute type selection dialog; if the user picks one, it's added to
    // the mandatory list and the table refreshes.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the attribute type selection dialog so the user can add a mandatory attribute.
     * We pass the currently mandatory list as hidden items so the user can't add duplicates.
     * If the user confirms a selection, we add the attribute and refresh the table.
     */
    private void addMandatoryAttributeType()
    {
        AttributeTypeSelectionDialog dialog = new AttributeTypeSelectionDialog();
        List<AttributeType> hiddenAttributes = new ArrayList<AttributeType>();
        hiddenAttributes.addAll( mandatoryAttributeTypesList );
        dialog.setHiddenAttributeTypes( hiddenAttributes );
        if ( dialog.open() == Dialog.OK )
        {
            mandatoryAttributeTypesList.add( dialog.getSelectedAttributeType() );
            updateMandatoryAttributeTypesTableTable();
        }
    }


    // ── Palpatine Revokes A Trooper's Mandatory Orders ────────────────────────────────
    // Palpatine pulls a clone's order slip from the formation — "You are relieved of this
    // mandatory duty." That attribute no longer needs to be present in every entry.
    // We remove the selected attribute type from the mandatory list and refresh the table.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected attribute type from the mandatory list and refreshes
     * the table viewer.
     * Does nothing if no row is selected (the button is disabled in that case anyway).
     */
    private void removeMandatoryAttributeType()
    {
        StructuredSelection selection = ( StructuredSelection ) mandatoryAttributeTypesTableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            mandatoryAttributeTypesList.remove( selection.getFirstElement() );
            updateMandatoryAttributeTypesTableTable();
        }
    }


    // ── Palpatine Re-Drills The Formation Into Alphabetical Order ─────────────────────
    // After any change to the mandatory formation, Palpatine has the clones re-sort
    // themselves alphabetically by designation — discipline requires orderly ranks.
    // We sort the mandatory list alphabetically by first name and tell the viewer to repaint.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sorts the mandatory attribute types list alphabetically by the first name of each
     * attribute type, then tells the table viewer to repaint.
     * Called after every add or remove to keep the displayed list consistently ordered.
     */
    private void updateMandatoryAttributeTypesTableTable()
    {
        Collections.sort( mandatoryAttributeTypesList, new Comparator<AttributeType>()
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

        mandatoryAttributeTypesTableViewer.refresh();
    }


    // ── Palpatine Reads Back The Full Mandatory Formation At Inspection ───────────────
    // At the final inspection, Palpatine reads the full list of clones in the mandatory
    // formation — every name, no absentees allowed.
    // The wizard uses this to set the MUST list on the new ObjectClass at finish time.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full list of mandatory {@link AttributeType} objects the user selected.
     * The wizard hands this list to the new ObjectClass at finish time to set its MUST
     * (required attributes) list.
     *
     * @return  the list of mandatory AttributeType objects; may be empty.
     */
    public List<AttributeType> getMandatoryAttributeTypes()
    {
        return mandatoryAttributeTypesList;
    }


    // ── Palpatine Reads Back Just The Designations, Not The Full Dossiers ─────────────
    // "Give me the names — not the full dossiers. Just what I need for the transmission."
    // The wizard only needs names (not full objects) when building the LDAP schema entry.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns just the names of the mandatory attribute types, as strings.
     * Callers that need to build the LDAP schema text representation use names rather than
     * full {@link AttributeType} objects.
     *
     * @return  a list of attribute type name strings; may be empty.
     */
    public List<String> getMandatoryAttributeTypesNames()
    {
        List<String> names = new ArrayList<String>();
        for ( AttributeType at : mandatoryAttributeTypesList )
        {
            names.add( at.getName() );
        }
        return names;
    }
}
