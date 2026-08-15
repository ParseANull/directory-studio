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

package org.apache.directory.studio.ldapbrowser.common.wizards;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReloadSchemaRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// ── CLASS: NewEntryObjectclassWizardPage — LUKE PICKING THE RIGHT KYBER CRYSTAL
// In the crystal cave on Ilum, Luke senses a cavern full of kyber crystals —
// structural crystals (the foundation of any blade), abstract ones, and
// auxiliary ones.  He must move at least one structural crystal into his
// collection; pulling in a crystal automatically drags in any superior crystals
// it depends on.  An instant-search filter lets him type the first letters of
// a crystal's name to narrow the glowing display.
// This wizard page mirrors that ritual: it shows all available LDAP object
// classes on the left and the selected ones on the right, with Add/Remove buttons
// in the middle and a live search field to filter the left-hand list.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The second page of {@link NewEntryWizard} (or first for {@link EditEntryWizard})
 * — lets the user choose which LDAP object classes to assign to the new entry.
 * Object classes are shown in two lists: available (left) and selected (right).
 * Selecting a structural class is mandatory; adding a class automatically pulls
 * in all of its superior classes, and removing one cascades to its subclasses.
 * Think of this page as Luke choosing his kyber crystals in the Ilum cave —
 * every crystal he picks shapes what his lightsaber (entry) can do.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewEntryObjectclassWizardPage extends WizardPage
{

    /** The Constant SIZING_SELECTION_WIDGET_HEIGHT. */
    private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;

    /** The Constant SIZING_SELECTION_WIDGET_WIDTH. */
    private final static int SIZING_SELECTION_WIDGET_WIDTH = 400;

    /** The wizard. */
    private NewEntryWizard wizard;

    /** The available object classes. */
    private List<ObjectClass> availableObjectClasses;

    /** The available object classes instant search. */
    private Text availableObjectClassesInstantSearch;

    /** The available object classes viewer. */
    private TableViewer availableObjectClassesViewer;

    /** The selected object classes. */
    private List<ObjectClass> selectedObjectClasses;

    /** The selected object classes viewer. */
    private TableViewer selectedObjectClassesViewer;

    /** The add button. */
    private Button addButton;

    /** The remove button. */
    private Button removeButton;

    private LabelProvider labelProvider = new LabelProvider()
    {
        // ── Reading a Crystal's Inscribed Name ────────────────────────────────
        // Luke holds a crystal up to the light and reads the name inscribed on
        // its facet — "inetOrgPerson", "person", "organizationalUnit" — each
        // crystal has a display name that tells him what kind it is.
        // We return the schema's toString representation of the ObjectClass.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Returns the display name for an {@link ObjectClass} element, as rendered
         * by {@link SchemaUtils#toString(ObjectClass)}.
         * Falls back to the default label provider for non-ObjectClass objects.
         *
         * <p>For example — Luke reads the name off a crystal's facet:</p>
         * <pre>
         *   getText( inetOrgPersonOC ) → "inetOrgPerson (2.16.840.1.113730.3.2.2)"
         * </pre>
         *
         * @param element  The viewer element; expected to be an {@link ObjectClass}.
         * @return         The display string for the object class.
         */
        public String getText( Object element )
        {
            if ( element instanceof ObjectClass )
            {
                ObjectClass ocd = ( ObjectClass ) element;
                return SchemaUtils.toString( ocd );
            }

            // Default
            return super.getText( element );
        }


        // ── Sensing a Crystal's Nature Through the Force ───────────────────────
        // Luke reaches out with the Force to sense whether a crystal is structural
        // (the backbone), abstract (a blueprint), or auxiliary (a supplement).
        // Each kind glows differently — we return a different icon for each type.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Returns the icon for an {@link ObjectClass} based on its type:
         * structural, abstract, or auxiliary.
         * Falls back to the default image for non-ObjectClass elements.
         *
         * <p>For example — Luke senses the crystal type and sees its aura:</p>
         * <pre>
         *   STRUCTURAL → IMG_OCD_STRUCTURAL (solid icon)
         *   ABSTRACT   → IMG_OCD_ABSTRACT   (outline icon)
         *   AUXILIARY  → IMG_OCD_AUXILIARY  (supplemental icon)
         * </pre>
         *
         * @param element  The viewer element; expected to be an {@link ObjectClass}.
         * @return         The {@link Image} corresponding to the object class type.
         */
        public Image getImage( Object element )
        {
            if ( element instanceof ObjectClass )
            {
                ObjectClass ocd = ( ObjectClass ) element;
                switch ( ocd.getType() )
                {
                    case STRUCTURAL:
                        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_OCD_STRUCTURAL );
                    case ABSTRACT:
                        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_OCD_ABSTRACT );
                    case AUXILIARY:
                        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_OCD_AUXILIARY );
                    default:
                        return BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_OCD );
                }
            }

            // Default
            return super.getImage( element );
        }
    };


    // ── Luke Enters the Crystal Cave for the First Time ───────────────────────
    // Luke steps across the cave threshold — the available and selected crystal
    // lists start empty, the title and description are set, and everything waits
    // until the page becomes visible and the schema crystals materialize.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code NewEntryObjectclassWizardPage} with its title,
     * description, and icon.  The available and selected object-class lists
     * are initialised as empty; they are populated when the page first becomes
     * visible via {@link #loadState()}.
     *
     * <p>For example — Luke crosses the cave threshold, crystal lists ready to fill:</p>
     * <pre>
     *   availableObjectClasses = new ArrayList&lt;&gt;(); // starts empty
     *   selectedObjectClasses  = new ArrayList&lt;&gt;(); // starts empty
     *   setPageComplete( false );  // must pick at least one crystal
     * </pre>
     *
     * @param pageName  Internal wizard page identifier.
     * @param wizard    The parent {@link NewEntryWizard} coordinating all pages.
     */
    public NewEntryObjectclassWizardPage( String pageName, NewEntryWizard wizard )
    {
        super( pageName );
        setTitle( Messages.getString( "NewEntryObjectclassWizardPage.ObjectClasses" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewEntryObjectclassWizardPage.ObjectClassesDescription" ) ); //$NON-NLS-1$
        setImageDescriptor( BrowserCommonActivator.getDefault().getImageDescriptor(
            BrowserCommonConstants.IMG_ENTRY_WIZARD ) );
        setPageComplete( false );

        this.wizard = wizard;
        this.availableObjectClasses = new ArrayList<ObjectClass>();
        this.selectedObjectClasses = new ArrayList<ObjectClass>();
    }


    // ── Luke Checks Whether He Has a Viable Crystal Selection ─────────────────
    // Luke surveys the crystals in his right hand — has he picked at least one
    // structural crystal?  Without a structural crystal the blade has no core.
    // If the selection is non-empty but lacks a structural class, Yoda appears
    // in Luke's mind as a warning, not a blocker.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current object-class selection and updates the Next button.
     * The page is complete as soon as at least one object class is selected.
     * A warning message is shown (not an error) if no structural object class
     * is included — an entry without a structural class is unusual but sometimes
     * necessary during editing.
     * Calls {@link #saveState()} to persist valid selections to the prototype entry.
     *
     * <p>For example — Luke checks his selected crystals for a structural one:</p>
     * <pre>
     *   if ( selectedObjectClasses.isEmpty() )  → Next disabled
     *   if ( no structural OC found )            → warning shown, Next still enabled
     *   else                                     → warning cleared, Next enabled
     * </pre>
     */
    private void validate()
    {
        if ( !selectedObjectClasses.isEmpty() )
        {
            boolean hasOneStructuralOC = false;
            for ( ObjectClass ocd : selectedObjectClasses )
            {
                if ( ocd.getType() == ObjectClassTypeEnum.STRUCTURAL )
                {
                    hasOneStructuralOC = true;
                    break;
                }
            }
            if ( !hasOneStructuralOC )
            {
                setMessage(
                    Messages.getString( "NewEntryObjectclassWizardPage.SelectStructuralObjectClass" ), WizardPage.WARNING ); //$NON-NLS-1$
            }
            else
            {
                setMessage( null );
            }

            setPageComplete( true );
            saveState();
        }
        else
        {
            setPageComplete( false );
            setMessage( null );
        }
    }


    // ── Luke Surveys the Cave — Available and Chosen Crystals Appear ──────────
    // When Luke steps into the cave the crystal clusters materialise: all known
    // crystals from the connection's schema fill the left wall, and any crystals
    // he already committed to in a previous visit glow on the right.
    // We repopulate both lists from the prototype entry's objectClass attribute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the available and selected object-class lists from the connection's
     * schema and the prototype entry's current objectClass attribute.
     * Called each time the page becomes visible so navigating back and forward
     * always reflects the current prototype state.
     *
     * <p>For example — Luke re-enters the cave; available and chosen crystals re-appear:</p>
     * <pre>
     *   availableObjectClasses ← all schema object classes
     *   selectedObjectClasses  ← those already on the prototype entry's objectClass attr
     * </pre>
     */
    private void loadState()
    {
        availableObjectClasses.clear();
        selectedObjectClasses.clear();

        if ( wizard.getSelectedConnection() != null )
        {
            availableObjectClasses.addAll( wizard.getSelectedConnection().getSchema().getObjectClassDescriptions() );

            DummyEntry newEntry = wizard.getPrototypeEntry();
            IAttribute ocAttribute = newEntry.getAttribute( SchemaConstants.OBJECT_CLASS_AT );
            if ( ocAttribute != null )
            {
                for ( IValue ocValue : ocAttribute.getValues() )
                {
                    if ( !ocValue.isEmpty() )
                    {
                        ObjectClass ocd = wizard.getSelectedConnection().getSchema()
                            .getObjectClassDescription( ocValue.getStringValue() );
                        availableObjectClasses.remove( ocd );
                        selectedObjectClasses.add( ocd );
                    }
                }
            }
        }

        availableObjectClassesViewer.refresh();
        selectedObjectClassesViewer.refresh();
    }


    // ── Luke Locks His Chosen Crystals Into the Blade Blueprint ──────────────
    // Luke decides on his final crystal set and presses them into the hilt mould
    // — the prototype entry's objectClass attribute is updated to reflect exactly
    // what Luke selected, replacing any previous choices.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Writes the currently selected object classes to the prototype entry's
     * {@code objectClass} attribute, replacing any previous values.
     * Called each time {@link #validate()} determines the selection is non-empty.
     *
     * <p>For example — Luke presses his chosen crystals into the lightsaber mould:</p>
     * <pre>
     *   ocAttribute.getValues() → cleared
     *   for each selectedObjectClass → ocAttribute.addValue( ocName )
     * </pre>
     */
    private void saveState()
    {
        DummyEntry newEntry = wizard.getPrototypeEntry();

        try
        {
            EventRegistry.suspendEventFiringInCurrentThread();

            // set new objectClass values
            IAttribute ocAttribute = newEntry.getAttribute( SchemaConstants.OBJECT_CLASS_AT );
            if ( ocAttribute == null )
            {
                ocAttribute = new Attribute( newEntry, SchemaConstants.OBJECT_CLASS_AT );
                newEntry.addAttribute( ocAttribute );
            }
            IValue[] values = ocAttribute.getValues();
            for ( IValue value : values )
            {
                ocAttribute.deleteValue( value );
            }
            for ( ObjectClass ocd : selectedObjectClasses )
            {
                ocAttribute.addValue( new Value( ocAttribute, ocd.getNames().get( 0 ) ) );
            }
        }
        finally
        {
            EventRegistry.resumeEventFiringInCurrentThread();
        }
    }


    // ── Luke Steps Into the Cave — Crystals Materialise ───────────────────────
    // Every time Luke enters the cave the crystals rearrange based on what he's
    // already committed to — we call loadState() to reflect the prototype entry,
    // validate() to set the Next button, and focus the search field so Luke can
    // type immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the wizard dialog whenever this page is shown or hidden.
     * On becoming visible we reload the object-class lists from the current
     * prototype state, re-validate, and focus the search field for keyboard navigation.
     *
     * <p>For example — Luke steps into the cave and the crystals re-arrange around him:</p>
     * <pre>
     *   if ( visible ) { loadState(); validate(); availableSearch.setFocus(); }
     * </pre>
     *
     * @param visible  {@code true} when this page is being shown, {@code false} when hidden.
     */
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );

        if ( visible )
        {
            loadState();
            validate();
            availableObjectClassesInstantSearch.setFocus();
        }
    }


    // ── The Crystal Cave Interior Is Revealed ─────────────────────────────────
    // Luke's eyes adjust to the cave light — he sees the full layout: the left
    // wall of available crystals with a search lantern, an Add/Remove panel in
    // the middle, and the right wall where his chosen crystals gather.
    // We build the SWT layout: instant-search + left TableViewer, button panel,
    // and right TableViewer, all wired with listeners and a reload button.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT controls for this page.
     * Creates a three-column layout: available object classes with a search
     * field and a schema-reload button on the left, Add/Remove buttons in the
     * middle, and selected object classes on the right.
     * Double-clicking in either list triggers the corresponding add or remove action.
     *
     * <p>For example — the crystal cave's full interior appears before Luke:</p>
     * <pre>
     *   [ Available OCs (filterable) ] [Add &gt;] [&lt; Remove] [ Selected OCs ]
     *   [ Search: _____________       ]                      [              ]
     *   [ inetOrgPerson               ]                      [ person       ]
     *   [ organizationalUnit          ]                      [ top          ]
     * </pre>
     *
     * @param parent  The parent composite supplied by the wizard dialog.
     */
    public void createControl( Composite parent )
    {

        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 3, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        Label availableLabel = new Label( composite, SWT.NONE );
        availableLabel.setText( Messages.getString( "NewEntryObjectclassWizardPage.AvailableObjectClasses" ) ); //$NON-NLS-1$
        Label buttonLabel = new Label( composite, SWT.NONE );
        buttonLabel.setText( "" ); //$NON-NLS-1$
        Label selectedLabel = new Label( composite, SWT.NONE );
        selectedLabel.setText( Messages.getString( "NewEntryObjectclassWizardPage.SelectedObjectClasses" ) ); //$NON-NLS-1$

        Composite availableObjectClassesComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        Composite availableObjectClassesInstantSearchComposite = BaseWidgetUtils.createColumnContainer(
            availableObjectClassesComposite, 2, 1 );
        availableObjectClassesInstantSearch = new Text( availableObjectClassesInstantSearchComposite, SWT.NONE
            | SWT.BORDER | SWT.SEARCH | SWT.CANCEL );
        availableObjectClassesInstantSearch.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        availableObjectClassesInstantSearch.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                availableObjectClassesViewer.refresh();
                if ( availableObjectClassesViewer.getTable().getItemCount() >= 1 )
                {
                    Object item = availableObjectClassesViewer.getElementAt( 0 );
                    availableObjectClassesViewer.setSelection( new StructuredSelection( item ) );
                }
            }
        } );
        availableObjectClassesInstantSearch.addKeyListener( new KeyAdapter()
        {
            public void keyPressed( KeyEvent e )
            {
                if ( e.keyCode == SWT.ARROW_DOWN )
                {
                    availableObjectClassesViewer.getTable().setFocus();
                }
                else if ( e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR )
                {
                    add( availableObjectClassesViewer.getSelection() );
                }
            }
        } );
        ControlDecoration availableObjectClassesInstantSearchDecoration = new ControlDecoration(
            availableObjectClassesInstantSearch, SWT.TOP | SWT.LEFT, composite );
        availableObjectClassesInstantSearchDecoration.setDescriptionText( Messages
            .getString( "NewEntryObjectclassWizardPage.FilterDescription" ) ); //$NON-NLS-1$
        availableObjectClassesInstantSearchDecoration.setImage( FieldDecorationRegistry.getDefault()
            .getFieldDecoration( FieldDecorationRegistry.DEC_CONTENT_PROPOSAL ).getImage() );
        Button reloadButton = new Button( availableObjectClassesInstantSearchComposite, SWT.PUSH | SWT.FLAT );
        reloadButton.setToolTipText( Messages.getString( "NewEntryObjectclassWizardPage.ReloadSchema" ) ); //$NON-NLS-1$
        reloadButton.setImage( BrowserCommonActivator.getDefault().getImage( BrowserCommonConstants.IMG_REFRESH ) );
        reloadButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                IBrowserConnection browserConnection = wizard.getSelectedConnection();
                ReloadSchemaRunnable runnable = new ReloadSchemaRunnable( browserConnection );
                RunnableContextRunner.execute( runnable, wizard.getContainer(), true );
                setVisible( true );
            }
        } );

        availableObjectClassesViewer = new TableViewer( availableObjectClassesComposite );
        GridData data = new GridData( GridData.FILL_BOTH );
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = ( int ) ( SIZING_SELECTION_WIDGET_WIDTH * 0.4 );
        availableObjectClassesViewer.getTable().setLayoutData( data );
        availableObjectClassesViewer.setContentProvider( new ArrayContentProvider() );
        availableObjectClassesViewer.setLabelProvider( labelProvider );
        availableObjectClassesViewer.setSorter( new ViewerSorter() );
        availableObjectClassesViewer.addFilter( new InstantSearchFilter( availableObjectClassesInstantSearch ) );
        availableObjectClassesViewer.setInput( availableObjectClasses );
        availableObjectClassesViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                add( event.getSelection() );
            }
        } );
        availableObjectClassesViewer.getTable().addKeyListener( new KeyAdapter()
        {
            public void keyPressed( KeyEvent e )
            {
                if ( e.keyCode == SWT.ARROW_UP )
                {
                    if ( availableObjectClassesViewer.getTable().getSelectionIndex() <= 0 )
                    {
                        availableObjectClassesInstantSearch.setFocus();
                    }
                }
            }
        } );

        Composite buttonComposite = new Composite( composite, SWT.NONE );
        gl = new GridLayout( 1, true );
        buttonComposite.setLayout( gl );
        data = new GridData( GridData.FILL_BOTH );
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        // data.widthHint = (int)(SIZING_SELECTION_WIDGET_WIDTH * 0.2);
        data.horizontalAlignment = SWT.CENTER;
        buttonComposite.setLayoutData( data );
        Label label0 = new Label( buttonComposite, SWT.NONE );
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        label0.setLayoutData( data );
        addButton = BaseWidgetUtils.createButton( buttonComposite, Messages
            .getString( "NewEntryObjectclassWizardPage.AddButton" ), 1 ); //$NON-NLS-1$
        removeButton = BaseWidgetUtils.createButton( buttonComposite, Messages
            .getString( "NewEntryObjectclassWizardPage.RemoveButton" ), 1 ); //$NON-NLS-1$
        Label label3 = new Label( buttonComposite, SWT.NONE );
        data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        label3.setLayoutData( data );

        addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                add( availableObjectClassesViewer.getSelection() );
            }
        } );

        removeButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                remove( selectedObjectClassesViewer.getSelection() );
            }
        } );

        selectedObjectClassesViewer = new TableViewer( composite );
        data = new GridData( GridData.FILL_BOTH );
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = ( int ) ( SIZING_SELECTION_WIDGET_WIDTH * 0.4 );
        selectedObjectClassesViewer.getTable().setLayoutData( data );
        selectedObjectClassesViewer.setContentProvider( new ArrayContentProvider() );
        selectedObjectClassesViewer.setLabelProvider( labelProvider );
        selectedObjectClassesViewer.setSorter( new ViewerSorter() );
        selectedObjectClassesViewer.setInput( selectedObjectClasses );
        selectedObjectClassesViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                remove( event.getSelection() );
            }
        } );

        setControl( composite );
    }


    // ── Luke Reaches for Crystals and Pulls Them to His Side ─────────────────
    // Luke extends his hand and draws a crystal from the left wall — it joins
    // his collection on the right.  Any superior crystals that crystal depends
    // on are automatically drawn along with it; a lightsaber blade needs its
    // whole crystal lineage.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Moves the selected object classes from the available list to the selected
     * list, recursively adding all superior (parent) object classes as well.
     * After adding, both viewers are refreshed, {@link #validate()} is called,
     * and the instant-search field is cleared to make room for the next search.
     *
     * <p>For example — Luke pulls a crystal and its ancestors toward him:</p>
     * <pre>
     *   add( selection of "inetOrgPerson" )
     *   // also pulls in "organizationalPerson", "person", "top" automatically
     * </pre>
     *
     * @param iselection  The viewer selection from the available list; expected
     *                    to contain {@link ObjectClass} elements.
     */
    private void add( ISelection iselection )
    {
        IStructuredSelection selection = ( IStructuredSelection ) iselection;
        Schema schema = wizard.getSelectedConnection().getSchema();
        Iterator<?> it = selection.iterator();
        while ( it.hasNext() )
        {
            ObjectClass ocd = ( ObjectClass ) it.next();
            if ( availableObjectClasses.contains( ocd ) && !selectedObjectClasses.contains( ocd ) )
            {
                availableObjectClasses.remove( ocd );
                selectedObjectClasses.add( ocd );

                // recursively add superior object classes
                List<ObjectClass> superiorObjectClassDescriptions = SchemaUtils
                    .getSuperiorObjectClassDescriptions( ocd, schema );
                if ( !superiorObjectClassDescriptions.isEmpty() )
                {
                    add( new StructuredSelection( superiorObjectClassDescriptions ) );
                }
            }
        }

        availableObjectClassesViewer.refresh();
        selectedObjectClassesViewer.refresh();
        validate();

        if ( !"".equals( availableObjectClassesInstantSearch.getText() ) ) //$NON-NLS-1$
        {
            availableObjectClassesInstantSearch.setText( "" ); //$NON-NLS-1$
            availableObjectClassesInstantSearch.setFocus();
        }
    }


    // ── Luke Returns a Crystal to the Cave Wall ────────────────────────────────
    // Luke decides a crystal isn't right for his blade and places it back on
    // the wall — but the cave is smart: any crystals that only existed because
    // of the one being returned are also pulled back, and any remaining crystals'
    // ancestor requirements are re-added to keep the selection consistent.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Moves the selected object classes from the selected list back to the
     * available list, recursively removing all sub-classes that depended on them.
     * After removal, superior classes of the remaining selected classes are
     * re-added to maintain consistency, and both viewers are refreshed.
     *
     * <p>For example — Luke returns "person" and the cave reclaims its subclass crystals:</p>
     * <pre>
     *   remove( selection of "person" )
     *   // "organizationalPerson" and "inetOrgPerson" are also removed
     *   // superiors of any remaining selected classes are re-added
     * </pre>
     *
     * @param iselection  The viewer selection from the selected list; expected
     *                    to contain {@link ObjectClass} elements.
     */
    private void remove( ISelection iselection )
    {
        IStructuredSelection selection = ( IStructuredSelection ) iselection;
        Schema schema = wizard.getSelectedConnection().getSchema();
        Iterator<?> it = selection.iterator();
        while ( it.hasNext() )
        {
            ObjectClass ocd = ( ObjectClass ) it.next();
            if ( !availableObjectClasses.contains( ocd ) && selectedObjectClasses.contains( ocd ) )
            {
                selectedObjectClasses.remove( ocd );
                availableObjectClasses.add( ocd );

                // recursively remove sub object classes
                List<ObjectClass> subObjectClassDescriptions = SchemaUtils
                    .getSuperiorObjectClassDescriptions( ocd, schema );
                if ( !subObjectClassDescriptions.isEmpty() )
                {
                    remove( new StructuredSelection( subObjectClassDescriptions ) );
                }
            }
        }

        // re-add superior object classes of remaining object classes
        List<ObjectClass> copy = new ArrayList<ObjectClass>( selectedObjectClasses );
        for ( ObjectClass ocd : copy )
        {
            List<ObjectClass> superiorObjectClassDescriptions = SchemaUtils
                .getSuperiorObjectClassDescriptions( ocd, schema );
            if ( !superiorObjectClassDescriptions.isEmpty() )
            {
                add( new StructuredSelection( superiorObjectClassDescriptions ) );
            }
        }

        availableObjectClassesViewer.refresh();
        selectedObjectClassesViewer.refresh();
        validate();
    }

    // ── CLASS: InstantSearchFilter — THE CRYSTAL-FINDING LANTERN ─────────────
    // Luke holds up a glowing lantern as he types — only the crystals whose
    // names start with the letters he enters catch the light; all others stay
    // dark.  This ViewerFilter is that lantern: it hides any object class whose
    // name doesn't start with the current search text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * A {@link ViewerFilter} that hides object classes whose names don't start
     * with the current text in the instant-search field.
     * Matching is case-insensitive and checks all known identifiers
     * (name and OID) of each {@link ObjectClass}.
     *
     */
    private class InstantSearchFilter extends ViewerFilter
    {

        /** The filter text. */
        private Text filterText;


        // ── Luke Lights the Crystal-Finding Lantern ────────────────────────────
        // Luke picks up the lantern and aims it at the crystal wall — from this
        // point on, only crystals that catch the lantern's beam are visible.
        // The constructor stores the search text widget so we can read it on
        // every filter pass.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Creates a new {@code InstantSearchFilter} bound to the given search field.
         *
         * <p>For example — Luke holds the lantern; the beam focuses on {@code filterText}:</p>
         * <pre>
         *   new InstantSearchFilter( availableObjectClassesInstantSearch )
         * </pre>
         *
         * @param filterText  The SWT {@link Text} widget whose content is used as
         *                    the prefix filter; read on every {@link #select} call.
         */
        private InstantSearchFilter( Text filterText )
        {
            this.filterText = filterText;
        }


        // ── The Lantern Beam Reveals Only Matching Crystals ───────────────────
        // Luke sweeps the lantern across the wall — if a crystal's name starts
        // with the letters Luke typed, it catches the light and stays visible;
        // otherwise it fades into the shadows.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if the given object class should be visible in the
         * available-list viewer — i.e., if any of its identifiers (names/OID) start
         * with the current search text (case-insensitive).
         * Non-{@link ObjectClass} elements are hidden.
         *
         * <p>For example — Luke sweeps the lantern and only matching crystals glow:</p>
         * <pre>
         *   filterText = "inet"
         *   select( viewer, parent, inetOrgPersonOC ) → true  (starts with "inet")
         *   select( viewer, parent, personOC )        → false (doesn't start with "inet")
         * </pre>
         *
         * @param viewer         The viewer applying this filter; not used directly.
         * @param parentElement  The parent element in the content tree; not used.
         * @param element        The element to test; expected to be an {@link ObjectClass}.
         * @return               {@code true} if the element passes the filter.
         */
        public boolean select( Viewer viewer, Object parentElement, Object element )
        {
            if ( element instanceof ObjectClass )
            {
                ObjectClass ocd = ( ObjectClass ) element;
                Collection<String> lowerCaseIdentifiers = SchemaUtils.getLowerCaseIdentifiers( ocd );
                for ( String s : lowerCaseIdentifiers )
                {
                    if ( Strings.toLowerCase( s ).startsWith( Strings.toLowerCase( filterText.getText() ) ) )
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
