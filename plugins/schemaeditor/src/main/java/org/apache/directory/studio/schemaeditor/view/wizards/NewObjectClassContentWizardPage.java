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

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.dialogs.ObjectClassSelectionDialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;


// ── CLASS: NewObjectClassContentWizardPage — Palpatine Issues The Death Star Blueprints ─
// Palpatine stands in the Imperial Senate chamber and issues the structural blueprint for
// the Death Star: which previous stations this new one inherits from (superiors), what kind
// of station it is (structural, abstract, auxiliary), and whether it's already decommissioned
// (obsolete). These aren't the attribute details — they're the architectural meta-decisions.
// This page captures the same thing for a new LDAP object class: its superclass hierarchy,
// its class type, and the obsolete flag.
// ──────────────────────────────────────────────────────────────────────────────────────
/**
 * The second wizard page in the New Object Class wizard, covering structural content.
 * We collect the object class's superior classes (what it inherits from), its type
 * (structural, abstract, or auxiliary), and the obsolete flag.
 * Think of Palpatine finalizing the structural blueprint: which previous Imperial stations
 * this one inherits from, what kind of weapon platform it is, and whether to retire it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewObjectClassContentWizardPage extends WizardPage
{
    /** The superiors object classes */
    private List<ObjectClass> superiorsList;

    /** The type of the object class */
    private ObjectClassTypeEnum type = ObjectClassTypeEnum.STRUCTURAL;

    // UI Fields
    private TableViewer superiorsTableViewer;
    private Button superiorsAddButton;
    private Button superiorsRemoveButton;
    private Button structuralRadio;
    private Button abstractRadio;
    private Button auxiliaryRadio;
    private Button obsoleteCheckbox;


    // ── Palpatine Opens The Blueprint Session With Full Ceremony ─────────────────────
    // Palpatine formally opens the Imperial design session: title announced, purpose stated,
    // the crest of the Empire displayed behind him. The bureaucracy is initialized.
    // We set the page title, description, and image here, and initialize an empty list
    // for the superior object classes the user will pick.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs this wizard page and sets its title, description, and image.
     * We also initialize the empty {@code superiorsList} that will hold whatever
     * parent object classes the user selects.
     */
    protected NewObjectClassContentWizardPage()
    {
        super( "NewObjectClassContentWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewObjectClassContentWizardPage.ObjectClassContent" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewObjectClassContentWizardPage.EnterObjectClassContent" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_OBJECT_CLASS_NEW_WIZARD ) );
        superiorsList = new ArrayList<ObjectClass>();
    }


    // ── Palpatine Lays Out The Structural Design Tables ──────────────────────────────
    // Palpatine spreads the architectural drawings across three tables: one for the
    // predecessor stations (superiors), one for the station classification (class type),
    // one for decommission status (properties). Each table has its own purpose.
    // We create the three SWT groups with their widgets and listeners here.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all SWT widgets for this page: a Superiors table, a Class Type radio group,
     * and a Properties section with the Obsolete checkbox.
     * Eclipse calls this once when the page is first shown.
     *
     * @param parent  the parent composite Eclipse provides — we embed our layout inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Superiors
        Group superiorsGroup = new Group( composite, SWT.NONE );
        superiorsGroup.setText( Messages.getString( "NewObjectClassContentWizardPage.Superiors" ) ); //$NON-NLS-1$
        superiorsGroup.setLayout( new GridLayout( 2, false ) );
        superiorsGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Superiors
        Table superiorsTable = new Table( superiorsGroup, SWT.BORDER );
        GridData superiorsTableGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 2 );
        superiorsTableGridData.heightHint = 100;
        superiorsTable.setLayoutData( superiorsTableGridData );
        superiorsTableViewer = new TableViewer( superiorsTable );
        superiorsTableViewer.setLabelProvider( new LabelProvider()
        {
            public Image getImage( Object element )
            {
                if ( element instanceof ObjectClass )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_OBJECT_CLASS );
                }

                // Default
                return super.getImage( element );
            }


            public String getText( Object element )
            {
                if ( element instanceof ObjectClass )
                {
                    ObjectClass oc = ( ObjectClass ) element;

                    List<String> names = oc.getNames();
                    if ( ( names != null ) && ( names.size() > 0 ) )
                    {
                        return NLS
                            .bind(
                                Messages.getString( "NewObjectClassContentWizardPage.AliasOID" ), new String[] { ViewUtils.concateAliases( names ), oc.getOid() } ); //$NON-NLS-1$
                    }
                    else
                    {
                        return NLS
                            .bind(
                                Messages.getString( "NewObjectClassContentWizardPage.NoneOID" ), new String[] { oc.getOid() } ); //$NON-NLS-1$
                    }
                }
                // Default
                return super.getText( element );
            }
        } );
        superiorsTableViewer.setContentProvider( new ArrayContentProvider() );
        superiorsTableViewer.setInput( superiorsList );
        superiorsTableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                superiorsRemoveButton.setEnabled( !event.getSelection().isEmpty() );
            }
        } );
        superiorsAddButton = new Button( superiorsGroup, SWT.PUSH );
        superiorsAddButton.setText( Messages.getString( "NewObjectClassContentWizardPage.Add" ) ); //$NON-NLS-1$
        superiorsAddButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );
        superiorsAddButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                addSuperiorObjectClass();
            }
        } );
        superiorsRemoveButton = new Button( superiorsGroup, SWT.PUSH );
        superiorsRemoveButton.setText( Messages.getString( "NewObjectClassContentWizardPage.Remove" ) ); //$NON-NLS-1$
        superiorsRemoveButton.setLayoutData( new GridData( SWT.FILL, SWT.NONE, false, false ) );
        superiorsRemoveButton.setEnabled( false );
        superiorsRemoveButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                removeSuperiorObjectClass();
            }
        } );

        // Class Type Group
        Group classTypeGroup = new Group( composite, SWT.NONE );
        classTypeGroup.setText( Messages.getString( "NewObjectClassContentWizardPage.ClassType" ) ); //$NON-NLS-1$
        classTypeGroup.setLayout( new GridLayout( 5, false ) );
        classTypeGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Class Type
        Label classTypeLable = new Label( classTypeGroup, SWT.NONE );
        classTypeLable.setText( Messages.getString( "NewObjectClassContentWizardPage.ClassTypeColon" ) ); //$NON-NLS-1$
        new Label( classTypeGroup, SWT.NONE ).setText( "          " ); //$NON-NLS-1$
        structuralRadio = new Button( classTypeGroup, SWT.RADIO );
        structuralRadio.setText( Messages.getString( "NewObjectClassContentWizardPage.Structural" ) ); //$NON-NLS-1$
        GridData structuralRadioGridData = new GridData( SWT.LEFT, SWT.NONE, false, false );
        structuralRadioGridData.widthHint = 115;
        structuralRadio.setLayoutData( structuralRadioGridData );
        structuralRadio.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                type = ObjectClassTypeEnum.STRUCTURAL;
            }
        } );
        structuralRadio.setSelection( true );
        abstractRadio = new Button( classTypeGroup, SWT.RADIO );
        abstractRadio.setText( Messages.getString( "NewObjectClassContentWizardPage.Abstract" ) ); //$NON-NLS-1$
        GridData abstractRadioGridData = new GridData( SWT.LEFT, SWT.NONE, false, false );
        abstractRadioGridData.widthHint = 115;
        abstractRadio.setLayoutData( structuralRadioGridData );
        abstractRadio.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                type = ObjectClassTypeEnum.ABSTRACT;
            }
        } );
        auxiliaryRadio = new Button( classTypeGroup, SWT.RADIO );
        auxiliaryRadio.setText( Messages.getString( "NewObjectClassContentWizardPage.Auxiliary" ) ); //$NON-NLS-1$
        GridData auxiliaryRadioGridData = new GridData( SWT.LEFT, SWT.NONE, false, false );
        auxiliaryRadioGridData.widthHint = 115;
        auxiliaryRadio.setLayoutData( structuralRadioGridData );
        auxiliaryRadio.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                type = ObjectClassTypeEnum.AUXILIARY;
            }
        } );

        // Properties Group
        Group propertiesGroup = new Group( composite, SWT.NONE );
        propertiesGroup.setText( Messages.getString( "NewObjectClassContentWizardPage.Properties" ) ); //$NON-NLS-1$
        propertiesGroup.setLayout( new GridLayout() );
        propertiesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Obsolete
        obsoleteCheckbox = new Button( propertiesGroup, SWT.CHECK );
        obsoleteCheckbox.setText( Messages.getString( "NewObjectClassContentWizardPage.Obsolete" ) ); //$NON-NLS-1$

        setControl( composite );
    }


    // ── Palpatine Calls In A Predecessor Station For Inspection ──────────────────────
    // Palpatine summons an existing Imperial platform to the blueprint table and adds it
    // to the lineage record — the new Death Star inherits from it.
    // We open the ObjectClass selection dialog; if the user picks one, we add it to
    // the superiors list and refresh the table.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a dialog so the user can pick a superior object class, then adds the selection
     * to our superiors list and refreshes the table.
     * Superiors in LDAP are like parent classes — the new object class inherits all their
     * required and optional attributes.
     */
    private void addSuperiorObjectClass()
    {
        ObjectClassSelectionDialog dialog = new ObjectClassSelectionDialog();
        dialog.setHiddenObjectClasses( superiorsList );
        if ( dialog.open() == Dialog.OK )
        {
            superiorsList.add( dialog.getSelectedObjectClass() );
            updateSuperiorsTable();
        }
    }


    // ── Palpatine Scratches A Predecessor Off The Blueprint ───────────────────────────
    // Palpatine crosses out one of the predecessor stations from the design record —
    // "We no longer inherit from that platform."
    // We remove the currently selected object class from the superiors list and refresh.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected object class from the superiors list and refreshes
     * the table viewer.
     * Does nothing if no row is selected (the button should be disabled in that case, but
     * we guard defensively).
     */
    private void removeSuperiorObjectClass()
    {
        StructuredSelection selection = ( StructuredSelection ) superiorsTableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            superiorsList.remove( selection.getFirstElement() );
            updateSuperiorsTable();
        }
    }


    // ── Palpatine Re-Files The Predecessor List In Alphabetical Order ─────────────────
    // After every change to the lineage record, Palpatine re-sorts the predecessor stations
    // alphabetically and re-displays the table to keep everything orderly.
    // We sort the superiors list by first name and tell the viewer to refresh.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sorts the superiors list alphabetically by the object class's first name, then tells
     * the table viewer to repaint.
     * We call this after every add or remove to keep the list consistently ordered.
     */
    private void updateSuperiorsTable()
    {
        Collections.sort( superiorsList, new Comparator<ObjectClass>()
        {
            public int compare( ObjectClass o1, ObjectClass o2 )
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

        superiorsTableViewer.refresh();
    }


    // ── Palpatine Reads Back The Full Predecessor Lineage ────────────────────────────
    // "We inherit from these platforms — read the names aloud for the record."
    // The wizard calls this at Finish time to set the superiorOids on the new object class.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the names (or OIDs) of all the superior object classes the user selected.
     * The wizard uses this list at finish time to set the superiorOids on the new
     * {@link ObjectClass}.
     * We return the first alias name for each, falling back to the OID if no names exist.
     *
     * @return  a list of superior object class name or OID strings; may be empty.
     */
    public List<String> getSuperiorsNameValue()
    {
        List<String> names = new ArrayList<String>();
        for ( ObjectClass oc : superiorsList )
        {
            List<String> aliases = oc.getNames();

            if ( ( aliases != null ) && ( aliases.size() > 0 ) )
            {
                names.add( aliases.get( 0 ) );
            }
            else
            {
                names.add( oc.getOid() );
            }
        }

        return names;
    }


    // ── Palpatine Declares The Station Classification ─────────────────────────────────
    // "This station is Structural — a real weapon platform, not a template or an add-on."
    // The class type is one of three options: Structural (a real LDAP entry class),
    // Abstract (a template that can't be used directly), or Auxiliary (an add-on mixin).
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the object class type the user selected via the radio buttons.
     * The type is one of STRUCTURAL (entries can be of this class directly), ABSTRACT
     * (template only, can't be instantiated), or AUXILIARY (mixin — can be added to entries
     * that are already of another structural class).
     * Defaults to STRUCTURAL if no radio has been clicked.
     *
     * @return  the selected {@link ObjectClassTypeEnum} value — never null.
     */
    public ObjectClassTypeEnum getClassTypeValue()
    {
        return type;
    }


    // ── Palpatine Marks The Station As Decommissioned In The Record ───────────────────
    // "Mark it obsolete in the archives — this design is retired but must remain on file."
    // An obsolete object class is still in the schema for backward compatibility but
    // signals that no new entries should be created with it.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user checked the "Obsolete" checkbox.
     * An obsolete object class is preserved in the schema for backward compatibility but
     * is flagged so implementations know not to use it for new entries.
     *
     * @return  true if the user marked this object class as obsolete.
     */
    public boolean getObsoleteValue()
    {
        return obsoleteCheckbox.getSelection();
    }
}
