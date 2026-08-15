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

package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandlerListener;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.alias.Alias;
import org.apache.directory.studio.schemaeditor.model.alias.AliasWithError;
import org.apache.directory.studio.schemaeditor.model.alias.AliasesStringParser;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.dialogs.EditAttributeTypeAliasesDialog;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingAttributeType;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingMatchingRule;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingSyntax;
import org.apache.directory.studio.schemaeditor.view.editors.schema.SchemaEditor;
import org.apache.directory.studio.schemaeditor.view.editors.schema.SchemaEditorInput;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: AttributeTypeEditorOverviewPage — LUKE'S BINARY SUNSET ON TATOOINE ───────
// Luke Skywalker stands on the Tatooine ridge, watching both suns set over the desert,
// taking stock of everything at once — the horizon, the future, the full sweep of what
// he's about to take on.  He's not focused on one detail; he's seeing the whole picture.
// This page is that moment for an attribute type.  In one scrollable form the user sees
// every property: aliases, OID, description, schema name, superior type, usage, syntax,
// length, boolean flags (obsolete / single-value / collective / no-user-modification),
// and all three matching rules (equality, ordering, substring).  It's the complete view
// of a single attribute type's identity — everything at a glance, everything editable.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Overview" tab page of the Attribute Type Editor — the primary editing surface.
 * It displays and allows editing of all attribute type properties through a JFace Forms
 * layout with two sections: "General Information" (identity and classification fields)
 * and "Matching Rules" (equality, ordering, substring comparators).
 * The page reacts to schema handler events (via an inner SchemaHandlerListener) so
 * combo boxes stay current when the schema changes while the editor is open.
 * Think of this as Luke's binary sunset: the full picture of a single attribute type,
 * all of it visible, all of it yours to work with.
 */
public class AttributeTypeEditorOverviewPage extends AbstractAttributeTypeEditorPage
{
    /** The page ID*/
    public static final String ID = AttributeTypeEditor.ID + ".overviewPage"; //$NON-NLS-1$

    /** The original schema */
    private Schema originalSchema;

    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    /** The SchemaHandler Listener */
    private SchemaHandlerListener schemaHandlerListener = new SchemaHandlerListener()
    {
        public void attributeTypeAdded( AttributeType at )
        {
            refreshUI();
        }


        public void attributeTypeModified( AttributeType at )
        {
            refreshUI();
        }


        public void attributeTypeRemoved( AttributeType at )
        {
            if ( !at.equals( getOriginalAttributeType() ) )
            {
                refreshUI();
            }
        }


        public void matchingRuleAdded( MatchingRule mr )
        {
            refreshUI();
        }


        public void matchingRuleModified( MatchingRule mr )
        {
            refreshUI();
        }


        public void matchingRuleRemoved( MatchingRule mr )
        {
            refreshUI();
        }


        public void objectClassAdded( ObjectClass oc )
        {
            refreshUI();
        }


        public void objectClassModified( ObjectClass oc )
        {
            refreshUI();
        }


        public void objectClassRemoved( ObjectClass oc )
        {
            refreshUI();
        }


        public void schemaAdded( Schema schema )
        {
            refreshUI();
        }


        public void schemaRemoved( Schema schema )
        {
            if ( !schema.equals( originalSchema ) )
            {
                refreshUI();
            }
        }


        public void schemaRenamed( Schema schema )
        {
            refreshUI();
        }


        public void syntaxAdded( LdapSyntax syntax )
        {
            refreshUI();
        }


        public void syntaxModified( LdapSyntax syntax )
        {
            refreshUI();
        }


        public void syntaxRemoved( LdapSyntax syntax )
        {
            refreshUI();
        }
    };

    // UI Fields
    private Text aliasesText;
    private Button aliasesButton;
    private Text oidText;
    private Hyperlink schemaLink;
    private Label schemaLabel;
    private Text descriptionText;
    private Hyperlink supLabel;
    private Combo supCombo;
    private ComboViewer supComboViewer;
    private Combo usageCombo;
    private Combo syntaxCombo;
    private ComboViewer syntaxComboViewer;
    private Text syntaxLengthText;
    private Button obsoleteCheckbox;
    private Button singleValueCheckbox;
    private Button collectiveCheckbox;
    private Button noUserModificationCheckbox;
    private Combo equalityCombo;
    private ComboViewer equalityComboViewer;
    private Combo orderingCombo;
    private ComboViewer orderingComboViewer;
    private Combo substringCombo;
    private ComboViewer substringComboViewer;

    // Listeners

    /** The listener for the Aliases Text Widget */
    private ModifyListener aliasesTextModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            AliasesStringParser parser = new AliasesStringParser();
            parser.parse( aliasesText.getText() );
            List<Alias> parsedAliases = parser.getAliases();
            modifiedAttributeType.setNames( new String[0] );
            List<String> aliasesList = new ArrayList<String>();
            for ( Alias parsedAlias : parsedAliases )
            {
                if ( !( parsedAlias instanceof AliasWithError ) )
                {
                    aliasesList.add( parsedAlias.getAlias() );
                }
            }
            modifiedAttributeType.setNames( aliasesList.toArray( new String[0] ) );
            setEditorDirty();
        }
    };

    /** The listener for the Edit Aliases Button Widget */
    private SelectionAdapter aliasesButtonListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            EditAttributeTypeAliasesDialog dialog = new EditAttributeTypeAliasesDialog(
                modifiedAttributeType.getNames() );
            if ( dialog.open() == EditAttributeTypeAliasesDialog.OK )
            {
                modifiedAttributeType.setNames( dialog.getAliases() );
                if ( ( modifiedAttributeType.getNames() != null ) && ( modifiedAttributeType.getNames().size() != 0 ) )
                {
                    aliasesText.setText( ViewUtils.concateAliases( modifiedAttributeType.getNames() ) );
                }
                else
                {
                    aliasesText.setText( "" ); //$NON-NLS-1$
                }
                setEditorDirty();
            }
        }
    };

    /** The Modify listener for the OID Text Widget */
    private ModifyListener oidTextModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            oidText.setForeground( ViewUtils.COLOR_BLACK );
            oidText.setToolTipText( "" ); //$NON-NLS-1$

            String oid = oidText.getText();

            if ( Oid.isOid( oid ) )
            {
                if ( ( getOriginalAttributeType().getOid().equals( oid ) )
                    || !( schemaHandler.isOidAlreadyTaken( oid ) ) )
                {
                    getModifiedAttributeType().setOid( oid );
                    setEditorDirty();
                }
                else
                {
                    oidText.setForeground( ViewUtils.COLOR_RED );
                    oidText.setToolTipText( Messages.getString( "AttributeTypeEditorOverviewPage.ElementOIDExists" ) ); //$NON-NLS-1$
                }
            }
            else
            {
                oidText.setForeground( ViewUtils.COLOR_RED );
                oidText.setToolTipText( Messages.getString( "AttributeTypeEditorOverviewPage.MalformedOID" ) ); //$NON-NLS-1$
            }
        }
    };

    /** The Verify listener for the OID Text Widget */
    private VerifyListener oidTextVerifyListener = new VerifyListener()
    {
        public void verifyText( VerifyEvent e )
        {
            if ( !e.text.matches( "([0-9]*\\.?)*" ) ) //$NON-NLS-1$
            {
                e.doit = false;
            }
        }
    };

    /** The listener for the Schema Hyperlink Widget*/
    private HyperlinkAdapter schemaLinkListener = new HyperlinkAdapter()
    {
        public void linkActivated( HyperlinkEvent e )
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

            SchemaEditorInput input = new SchemaEditorInput( schemaHandler
                .getSchema( getModifiedAttributeType().getSchemaName() ) );
            String editorId = SchemaEditor.ID;
            try
            {
                page.openEditor( input, editorId );
            }
            catch ( PartInitException exception )
            {
                PluginUtils.logError( "error when opening the editor", exception ); //$NON-NLS-1$
            }
        }
    };

    /** The listener for the Description Text Widget */
    private ModifyListener descriptionTextListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            int caretPosition = descriptionText.getCaretPosition();
            getModifiedAttributeType().setDescription( descriptionText.getText() );
            descriptionText.setSelection( caretPosition );
            setEditorDirty();
        }
    };

    /** The listener for the Sup Label Widget*/
    private HyperlinkAdapter supLabelListener = new HyperlinkAdapter()
    {
        public void linkActivated( HyperlinkEvent e )
        {
            Object selectedItem = ( ( StructuredSelection ) supComboViewer.getSelection() ).getFirstElement();

            if ( selectedItem instanceof AttributeType )
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                AttributeTypeEditorInput input = new AttributeTypeEditorInput( ( AttributeType ) selectedItem );
                try
                {
                    page.openEditor( input, AttributeTypeEditor.ID );
                }
                catch ( PartInitException exception )
                {
                    PluginUtils.logError( "error when opening the editor", exception ); //$NON-NLS-1$
                }
            }
        }
    };

    /** The listener for the Sup Combo Widget */
    private ISelectionChangedListener supComboViewerListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            Object selectedItem = ( ( StructuredSelection ) supComboViewer.getSelection() ).getFirstElement();

            if ( selectedItem instanceof AttributeType )
            {
                AttributeType at = ( AttributeType ) selectedItem;
                List<String> names = at.getNames();
                if ( ( names != null ) && ( names.size() > 0 ) )
                {
                    modifiedAttributeType.setSuperiorOid( names.get( 0 ) );
                }
                else
                {
                    modifiedAttributeType.setSuperiorOid( at.getOid() );
                }
            }
            else if ( selectedItem instanceof NonExistingAttributeType )
            {
                NonExistingAttributeType neat = ( NonExistingAttributeType ) selectedItem;

                if ( NonExistingAttributeType.NONE.equals( neat.getName() ) )
                {
                    modifiedAttributeType.setSuperiorOid( null );
                }
                else
                {
                    modifiedAttributeType.setSuperiorOid( ( ( NonExistingAttributeType ) selectedItem ).getName() );
                }
            }
            setEditorDirty();
        }
    };

    /** The listener for the Usage Combo Widget */
    private ModifyListener usageComboListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            if ( usageCombo.getSelectionIndex() == 0 )
            {
                modifiedAttributeType.setUsage( UsageEnum.DIRECTORY_OPERATION );
            }
            else if ( usageCombo.getSelectionIndex() == 1 )
            {
                modifiedAttributeType.setUsage( UsageEnum.DISTRIBUTED_OPERATION );
            }
            else if ( usageCombo.getSelectionIndex() == 2 )
            {
                modifiedAttributeType.setUsage( UsageEnum.DSA_OPERATION );
            }
            else if ( usageCombo.getSelectionIndex() == 3 )
            {
                modifiedAttributeType.setUsage( UsageEnum.USER_APPLICATIONS );
            }
            setEditorDirty();
        }
    };

    /** The listener for the Syntax Combo Widget */
    private ISelectionChangedListener syntaxComboViewerListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            Object selectedItem = ( ( StructuredSelection ) syntaxComboViewer.getSelection() ).getFirstElement();

            if ( selectedItem instanceof LdapSyntax )
            {
                modifiedAttributeType.setSyntaxOid( ( ( LdapSyntax ) selectedItem ).getOid() );
            }
            else if ( selectedItem instanceof NonExistingSyntax )
            {
                NonExistingSyntax nes = ( NonExistingSyntax ) selectedItem;

                if ( NonExistingMatchingRule.NONE.equals( nes.getDescription() ) )
                {
                    modifiedAttributeType.setSyntaxOid( null );
                }
                else
                {
                    modifiedAttributeType.setSyntaxOid( ( ( NonExistingSyntax ) selectedItem ).getDescription() );
                }
            }
            setEditorDirty();
        }
    };

    /** The Modify listener for the Syntax Length Text Widget */
    private ModifyListener syntaxLengthTextModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            if ( syntaxLengthText.getText().length() <= 0 )
            {
                modifiedAttributeType.setSyntaxLength( -1 );
            }
            else
            {
                modifiedAttributeType.setSyntaxLength( Integer.parseInt( syntaxLengthText.getText() ) );
            }
            setEditorDirty();
        }
    };

    /** The Verify listener for the Syntax Length Text Widget */
    private VerifyListener syntaxLengthTextVerifyListener = new VerifyListener()
    {
        public void verifyText( VerifyEvent e )
        {
            if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                e.doit = false;
            }
        }
    };

    /** The listener for the Obsolete Checbox Widget */
    private SelectionAdapter obsoleteCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getModifiedAttributeType().setObsolete( obsoleteCheckbox.getSelection() );
            setEditorDirty();
        }
    };

    /** The listener for the Single-Value Checkbox Widget */
    private SelectionAdapter singleValueCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getModifiedAttributeType().setSingleValued( singleValueCheckbox.getSelection() );
            setEditorDirty();
        }
    };

    /** The listener for the Collective Checkbox Widget */
    private SelectionAdapter collectiveCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getModifiedAttributeType().setCollective( collectiveCheckbox.getSelection() );
            setEditorDirty();
        }
    };

    /** The listener for the No-User-Modification Widget */
    private SelectionAdapter noUserModificationCheckboxListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            getModifiedAttributeType().setUserModifiable( !noUserModificationCheckbox.getSelection() );
            setEditorDirty();
        }
    };

    /** The listener for the Equality Combo Widget */
    private ISelectionChangedListener equalityComboViewerListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            Object selectedItem = ( ( StructuredSelection ) equalityComboViewer.getSelection() ).getFirstElement();

            if ( selectedItem instanceof MatchingRule )
            {
                modifiedAttributeType.setEqualityOid( ( ( MatchingRule ) selectedItem ).getName() );
            }
            else if ( selectedItem instanceof NonExistingMatchingRule )
            {
                NonExistingMatchingRule nemr = ( NonExistingMatchingRule ) selectedItem;

                if ( NonExistingMatchingRule.NONE.equals( nemr.getName() ) )
                {
                    modifiedAttributeType.setEqualityOid( null );
                }
                else
                {
                    modifiedAttributeType.setEqualityOid( ( ( NonExistingMatchingRule ) selectedItem ).getName() );
                }
            }
            setEditorDirty();
        }
    };

    /** The listener for the Ordering Combo Widget */
    private ISelectionChangedListener orderingComboViewerListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            Object selectedItem = ( ( StructuredSelection ) orderingComboViewer.getSelection() ).getFirstElement();

            if ( selectedItem instanceof MatchingRule )
            {
                modifiedAttributeType.setOrderingOid( ( ( MatchingRule ) selectedItem ).getName() );
            }
            else if ( selectedItem instanceof NonExistingMatchingRule )
            {
                NonExistingMatchingRule nemr = ( NonExistingMatchingRule ) selectedItem;

                if ( NonExistingMatchingRule.NONE.equals( nemr.getName() ) )
                {
                    modifiedAttributeType.setOrderingOid( null );
                }
                else
                {
                    modifiedAttributeType.setOrderingOid( ( ( NonExistingMatchingRule ) selectedItem ).getName() );
                }
            }
            setEditorDirty();
        }
    };

    /** The listener for the Substring Combo Widget */
    private ISelectionChangedListener substringComboViewerListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            AttributeType modifiedAttributeType = getModifiedAttributeType();
            Object selectedItem = ( ( StructuredSelection ) substringComboViewer.getSelection() ).getFirstElement();

            if ( selectedItem instanceof MatchingRule )
            {
                modifiedAttributeType.setSubstringOid( ( ( MatchingRule ) selectedItem ).getName() );
            }
            else if ( selectedItem instanceof NonExistingMatchingRule )
            {
                NonExistingMatchingRule nemr = ( NonExistingMatchingRule ) selectedItem;

                if ( NonExistingMatchingRule.NONE.equals( nemr.getName() ) )
                {
                    modifiedAttributeType.setSubstringOid( null );
                }
                else
                {
                    modifiedAttributeType.setSubstringOid( ( ( NonExistingMatchingRule ) selectedItem ).getName() );
                }
            }
            setEditorDirty();
        }
    };

    /** The filter listener for Mouse Wheel events */
    private Listener mouseWheelFilter = new Listener()
    {
        public void handleEvent( Event event )
        {
            // Hiding Mouse Wheel events for Combo widgets
            if ( event.widget instanceof Combo )
            {
                event.doit = false;
            }
        }
    };


    // ── Luke Steps onto the Ridge and Takes It All In ────────────────────────────────
    // Luke walks out to the ridge on Tatooine, squints into the twin-sun sunset, and
    // begins to take stock of everything before him — his path, the schema, the tools
    // he'll need.  The schemaHandler and the schema listener are his eyes and ears,
    // keeping this overview page up-to-date as the schema changes around him.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Overview page and registers a comprehensive schema handler listener
     * so that every relevant schema change (attribute types, matching rules, syntaxes,
     * schemas) triggers a UI refresh.
     * We register the listener here (in the constructor) so it's active before the
     * form is built; {@link #dispose()} removes it symmetrically.
     *
     * <p>For example — Luke takes up his vantage point:</p>
     * <pre>
     *   // Constructor runs → register schemaHandlerListener.
     *   // Future schema events → refreshUI() → combos/fields stay current.
     * </pre>
     *
     * @param editor  the {@link AttributeTypeEditor} that owns this page; provides
     *                the attribute type objects and dirty-flag management
     */
    public AttributeTypeEditorOverviewPage( AttributeTypeEditor editor )
    {
        super( editor, ID, Messages.getString( "AttributeTypeEditorOverviewPage.Overview" ) ); //$NON-NLS-1$
        schemaHandler = Activator.getDefault().getSchemaHandler();
        schemaHandler.addListener( schemaHandlerListener );
    }


    // ── Luke Looks at the Whole Horizon ──────────────────────────────────────────────
    // Luke takes in the full sunset panorama: he builds out the complete view of the
    // attribute type — two sections (General Information and Matching Rules), all fields
    // populated, all listeners wired.  Everything he needs to understand and modify
    // this attribute type is laid out in front of him.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the Overview page UI inside the given Eclipse managed form.
     * We create two JFace Forms sections — "General Information" and "Matching Rules" —
     * populate all fields from the working-copy attribute type, wire all listeners,
     * and register a dynamic-help context.
     * Called once by Eclipse when the tab is first displayed.
     *
     * <p>For example — Luke sees the whole picture:</p>
     * <pre>
     *   createGeneralInformationSection(...);  // aliases, OID, desc, schema, sup, usage, syntax, flags
     *   createMatchingRulesSection(...);       // equality, ordering, substring combos
     *   fillInUiFields();                      // populate from the attribute type
     *   addListeners();                        // wire all event handlers
     * </pre>
     *
     * @param managedForm  the Eclipse-managed form container for this page
     */
    protected void createFormContent( IManagedForm managedForm )
    {
        super.createFormContent( managedForm );

        // Creating the base UI
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        GridLayout layout = new GridLayout();
        form.getBody().setLayout( layout );

        // General Information Section
        createGeneralInformationSection( form.getBody(), toolkit );

        // Matching Rules Section
        createMatchingRulesSection( form.getBody(), toolkit );

        // Filling the UI with values from the attribute type
        fillInUiFields();

        // Listeners initialization
        addListeners();

        // Help Context for Dynamic Help
        PlatformUI.getWorkbench().getHelpSystem().setHelp( form,
            PluginConstants.PLUGIN_ID + "." + "attribute_type_editor" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── Luke Scans the Full Identity Dossier ─────────────────────────────────────────
    // Luke's mentor Obi-Wan hands him a complete identity file on his heritage: names,
    // origins, capabilities, flags — everything that defines who he is.  This method
    // builds the UI for that dossier: aliases, OID, description, schema, superior type,
    // usage, syntax, length, and four boolean property checkboxes.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "General Information" form section with all identity and classification
     * fields for the attribute type.
     * This section contains: Aliases text field + "Edit Aliases" button, OID text field,
     * Description text area, Schema hyperlink, Superior Type combo, Usage combo, Syntax
     * combo, Syntax Length text field, and four property checkboxes (Obsolete, Single-Value,
     * Collective, No-User-Modification).
     *
     * @param parent   the parent SWT composite (the scrolled form body)
     * @param toolkit  the FormToolkit used to create Forms-styled widgets
     */
    private void createGeneralInformationSection( Composite parent, FormToolkit toolkit )
    {
        // General Information Section
        Section section_general_information = toolkit.createSection( parent, Section.DESCRIPTION | Section.EXPANDED
            | Section.TITLE_BAR );
        section_general_information.setDescription( Messages
            .getString( "AttributeTypeEditorOverviewPage.SpecifyGeneralInformation" ) ); //$NON-NLS-1$
        section_general_information
            .setText( Messages.getString( "AttributeTypeEditorOverviewPage.GeneralInformation" ) ); //$NON-NLS-1$

        // Creating the layout of the section
        Composite client_general_information = toolkit.createComposite( section_general_information );
        client_general_information.setLayout( new GridLayout( 2, false ) );
        toolkit.paintBordersFor( client_general_information );
        section_general_information.setClient( client_general_information );
        section_general_information.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Adding elements to the section

        // ALIASES Field
        toolkit
            .createLabel( client_general_information, Messages.getString( "AttributeTypeEditorOverviewPage.Aliases" ) ); //$NON-NLS-1$
        Composite aliasComposite = toolkit.createComposite( client_general_information );
        GridLayout aliasCompositeGridLayout = new GridLayout( 2, false );
        toolkit.paintBordersFor( aliasComposite );
        aliasCompositeGridLayout.marginHeight = 1;
        aliasCompositeGridLayout.marginWidth = 1;
        aliasComposite.setLayout( aliasCompositeGridLayout );
        aliasComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        aliasesText = toolkit.createText( aliasComposite, "" ); //$NON-NLS-1$
        aliasesText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        aliasesButton = toolkit.createButton( aliasComposite, Messages
            .getString( "AttributeTypeEditorOverviewPage.EditAliases" ), SWT.PUSH ); //$NON-NLS-1$
        aliasesButton.setLayoutData( new GridData( SWT.NONE, SWT.CENTER, false, false ) );

        // OID Field
        toolkit.createLabel( client_general_information, Messages.getString( "AttributeTypeEditorOverviewPage.OID" ) ); //$NON-NLS-1$
        oidText = toolkit.createText( client_general_information, "" ); //$NON-NLS-1$
        oidText.setLayoutData( new GridData( SWT.FILL, 0, true, false ) );

        // DESCRIPTION Field
        toolkit.createLabel( client_general_information, Messages
            .getString( "AttributeTypeEditorOverviewPage.Description" ) ); //$NON-NLS-1$
        descriptionText = toolkit.createText( client_general_information, "", SWT.MULTI | SWT.V_SCROLL ); //$NON-NLS-1$
        GridData descriptionGridData = new GridData( SWT.FILL, SWT.NONE, true, false );
        descriptionGridData.heightHint = 42;
        descriptionText.setLayoutData( descriptionGridData );

        // SCHEMA Field
        schemaLink = toolkit.createHyperlink( client_general_information, Messages
            .getString( "AttributeTypeEditorOverviewPage.Schema" ), SWT.WRAP ); //$NON-NLS-1$
        schemaLabel = toolkit.createLabel( client_general_information, "" ); //$NON-NLS-1$
        schemaLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // SUP Combo
        supLabel = toolkit.createHyperlink( client_general_information, Messages
            .getString( "AttributeTypeEditorOverviewPage.SuperiorType" ), SWT.WRAP ); //$NON-NLS-1$
        supCombo = new Combo( client_general_information, SWT.READ_ONLY | SWT.SINGLE );
        supCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        supComboViewer = new ComboViewer( supCombo );
        supComboViewer.setContentProvider( new ATESuperiorComboContentProvider() );
        supComboViewer.setLabelProvider( new ATESuperiorComboLabelProvider() );

        // USAGE Combo
        toolkit.createLabel( client_general_information, Messages.getString( "AttributeTypeEditorOverviewPage.Usage" ) ); //$NON-NLS-1$
        usageCombo = new Combo( client_general_information, SWT.READ_ONLY | SWT.SINGLE );
        usageCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        initUsageCombo();

        // SYNTAX Combo
        toolkit
            .createLabel( client_general_information, Messages.getString( "AttributeTypeEditorOverviewPage.Syntax" ) ); //$NON-NLS-1$
        syntaxCombo = new Combo( client_general_information, SWT.READ_ONLY | SWT.SINGLE );
        syntaxCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        syntaxComboViewer = new ComboViewer( syntaxCombo );
        syntaxComboViewer.setContentProvider( new ATESyntaxComboContentProvider() );
        syntaxComboViewer.setLabelProvider( new ATESyntaxComboLabelProvider() );

        // SYNTAX LENGTH Field
        toolkit.createLabel( client_general_information, Messages
            .getString( "AttributeTypeEditorOverviewPage.SyntaxLength" ) ); //$NON-NLS-1$
        syntaxLengthText = toolkit.createText( client_general_information, "" ); //$NON-NLS-1$
        syntaxLengthText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // PROPERTIES composite
        toolkit.createLabel( client_general_information, "" ); // Filling the first column //$NON-NLS-1$
        Composite propertiesComposite = toolkit.createComposite( client_general_information );
        GridLayout propertiesCompositeGridLayout = new GridLayout( 2, true );
        propertiesCompositeGridLayout.horizontalSpacing = 0;
        propertiesCompositeGridLayout.verticalSpacing = 0;
        propertiesCompositeGridLayout.marginHeight = 0;
        propertiesCompositeGridLayout.marginWidth = 0;
        propertiesComposite.setLayout( propertiesCompositeGridLayout );
        propertiesComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // OBSOLETE Checkbox
        obsoleteCheckbox = toolkit.createButton( propertiesComposite, Messages
            .getString( "AttributeTypeEditorOverviewPage.Obsolete" ), SWT.CHECK ); //$NON-NLS-1$
        obsoleteCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // SINGLE-VALUE Checkbox
        singleValueCheckbox = toolkit.createButton( propertiesComposite, Messages
            .getString( "AttributeTypeEditorOverviewPage.SingleValue" ), SWT.CHECK ); //$NON-NLS-1$
        singleValueCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // COLLECTIVE Checkbox
        toolkit.createLabel( client_general_information, "" ); // Filling the first column //$NON-NLS-1$
        collectiveCheckbox = toolkit.createButton( propertiesComposite, Messages
            .getString( "AttributeTypeEditorOverviewPage.Collective" ), SWT.CHECK ); //$NON-NLS-1$
        collectiveCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // NO-USER-MODIFICATION Checkbox
        noUserModificationCheckbox = toolkit.createButton( propertiesComposite,
            Messages.getString( "AttributeTypeEditorOverviewPage.NoUserModification" ), SWT.CHECK ); //$NON-NLS-1$
        noUserModificationCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Luke Studies the Matching-Rule Star Charts ───────────────────────────────────
    // Alongside the identity dossier, Obi-Wan gives Luke star charts that describe how
    // the Force works for this particular path — the equality, ordering, and substring
    // matching rules that govern how LDAP searches will compare values of this attribute.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Matching Rules" form section with three combo viewers.
     * This section contains: Equality combo, Ordering combo, and Substring combo — each
     * backed by {@link ATEMatchingRulesComboContentProvider} and
     * {@link ATEMatchingRulesComboLabelProvider}.
     * Matching rules are LDAP algorithms that define how two attribute values are compared
     * for equality, less-than ordering, and substring searching.
     *
     * @param parent   the parent SWT composite (the scrolled form body)
     * @param toolkit  the FormToolkit used to create Forms-styled widgets
     */
    private void createMatchingRulesSection( Composite parent, FormToolkit toolkit )
    {
        // Matching Rules Section
        Section section_matching_rules = toolkit.createSection( parent, Section.DESCRIPTION | Section.EXPANDED
            | Section.TITLE_BAR );
        section_matching_rules.setDescription( Messages
            .getString( "AttributeTypeEditorOverviewPage.SpecifyMatchingRules" ) ); //$NON-NLS-1$
        section_matching_rules.setText( Messages.getString( "AttributeTypeEditorOverviewPage.MatchingRules" ) ); //$NON-NLS-1$

        // Creating the layout of the section
        Composite client_matching_rules = toolkit.createComposite( section_matching_rules );
        GridLayout layout_matching_rules = new GridLayout();
        layout_matching_rules.numColumns = 2;
        client_matching_rules.setLayout( layout_matching_rules );
        toolkit.paintBordersFor( client_matching_rules );
        section_matching_rules.setClient( client_matching_rules );
        section_matching_rules.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // EQUALITY Combo
        toolkit.createLabel( client_matching_rules, Messages.getString( "AttributeTypeEditorOverviewPage.Equality" ) ); //$NON-NLS-1$
        equalityCombo = new Combo( client_matching_rules, SWT.READ_ONLY | SWT.SINGLE );
        equalityCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        equalityComboViewer = new ComboViewer( equalityCombo );
        equalityComboViewer.setContentProvider( new ATEMatchingRulesComboContentProvider() );
        equalityComboViewer.setLabelProvider( new ATEMatchingRulesComboLabelProvider() );

        // ORDERING Combo
        toolkit.createLabel( client_matching_rules, Messages.getString( "AttributeTypeEditorOverviewPage.Ordering" ) ); //$NON-NLS-1$
        orderingCombo = new Combo( client_matching_rules, SWT.READ_ONLY | SWT.SINGLE );
        orderingCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        orderingComboViewer = new ComboViewer( orderingCombo );
        orderingComboViewer.setContentProvider( new ATEMatchingRulesComboContentProvider() );
        orderingComboViewer.setLabelProvider( new ATEMatchingRulesComboLabelProvider() );

        // SUBSTRING Combo
        toolkit.createLabel( client_matching_rules, Messages.getString( "AttributeTypeEditorOverviewPage.Substring" ) ); //$NON-NLS-1$
        substringCombo = new Combo( client_matching_rules, SWT.READ_ONLY | SWT.SINGLE );
        substringCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        substringComboViewer = new ComboViewer( substringCombo );
        substringComboViewer.setContentProvider( new ATEMatchingRulesComboContentProvider() );
        substringComboViewer.setLabelProvider( new ATEMatchingRulesComboLabelProvider() );
    }


    // ── Obi-Wan Explains the Four Paths ──────────────────────────────────────────────
    // Obi-Wan explains to Luke that there are four ways an attribute type can be used:
    // by directory operations, distributed operations, DSA operations, or user
    // applications — like four paths leading out from the canyon.  He puts the options
    // in order and labels each one clearly.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Usage combo with the four LDAP attribute type usage values.
     * The order matches the {@link UsageEnum} ordinals used in {@link #fillInUiFields}
     * to select the correct item by index: 0=Directory Operation, 1=Distributed
     * Operation, 2=DSA Operation, 3=User Applications.
     * Called once during {@link #createGeneralInformationSection}.
     */
    private void initUsageCombo()
    {
        usageCombo.add( "Directory Operation", 0 ); //$NON-NLS-1$
        usageCombo.add( "Distributed Operation", 1 ); //$NON-NLS-1$
        usageCombo.add( "DSA Operation", 2 ); //$NON-NLS-1$
        usageCombo.add( "User Applications", 3 ); //$NON-NLS-1$
    }


    // ── Luke Reads All the Instrument Readings at Once ───────────────────────────────
    // Luke scans the whole horizon and updates every instrument: aliases, OID,
    // description, schema, superior type, usage, syntax, length, flags, and all three
    // matching rules.  Every field gets the current value from the working-copy attribute
    // type pushed into it.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates every UI widget on the Overview page from the current working-copy
     * attribute type.
     * Called on initial form creation and on every {@link #refreshUI()} cycle (after
     * listeners are removed and before they are re-added).
     * The method delegates the more complex combo-fill logic to private helper methods
     * ({@link #fillSupCombo()}, {@link #fillInUsageCombo()}, {@link #fillSyntaxCombo()},
     * {@link #fillEqualityCombo()}, {@link #fillOrderingCombo()}, {@link #fillSubstringCombo()})
     * to keep this method readable.
     */
    protected void fillInUiFields()
    {
        // Getting the modified attribute type
        AttributeType modifiedAttributeType = getModifiedAttributeType();

        originalSchema = schemaHandler.getSchema( getOriginalAttributeType().getSchemaName() );

        // ALIASES Label
        if ( ( modifiedAttributeType.getNames() != null ) && ( modifiedAttributeType.getNames().size() != 0 ) )
        {
            aliasesText.setText( ViewUtils.concateAliases( modifiedAttributeType.getNames() ) );
        }
        else
        {
            aliasesText.setText( "" ); //$NON-NLS-1$
        }

        // OID Field
        if ( modifiedAttributeType.getOid() != null )
        {
            oidText.setText( modifiedAttributeType.getOid() );
        }

        // SCHEMA Field
        schemaLabel.setText( modifiedAttributeType.getSchemaName() );

        // DESCRIPTION Field
        if ( modifiedAttributeType.getDescription() != null )
        {
            descriptionText.setText( modifiedAttributeType.getDescription() );
        }

        // SUP Combo
        fillSupCombo();

        // USAGE Combo
        fillInUsageCombo();

        // SYNTAX Combo
        fillSyntaxCombo();

        // SYNTAX LENGTH Field
        if ( modifiedAttributeType.getSyntaxLength() > 0 )
        {
            syntaxLengthText.setText( modifiedAttributeType.getSyntaxLength() + "" ); //$NON-NLS-1$
        }

        // OBSOLETE Checkbox
        obsoleteCheckbox.setSelection( modifiedAttributeType.isObsolete() );

        // SINGLE-VALUE Checkbox
        singleValueCheckbox.setSelection( modifiedAttributeType.isSingleValued() );

        // COLLECTIVE Checkbox
        collectiveCheckbox.setSelection( modifiedAttributeType.isCollective() );

        // NO-USER-MODIFICATION Checkbox
        noUserModificationCheckbox.setSelection( !modifiedAttributeType.isUserModifiable() );

        // EQUALITY Combo
        fillEqualityCombo();

        // ORDERING Combo
        fillOrderingCombo();

        // SUBSTRING Combo
        fillSubstringCombo();
    }


    // ── Luke Sets the Superior-Type Bearing ──────────────────────────────────────────
    // Luke checks the star charts and finds the bearing for the superior type — where
    // does this attribute type get its properties from?  If the superior type is unknown
    // (referenced but not in the schema), he adds a phantom entry so the bearing can
    // still be displayed.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the "Superior Type" combo and selects the correct entry.
     * If the working-copy attribute type has no superior (null OID), we select "(None)".
     * If it has a superior that resolves to a real AttributeType, we select that.
     * If it references an unresolvable name, we add a NonExistingAttributeType to the
     * combo input and select it — this preserves the reference visually.
     * We always create a fresh ATESuperiorComboInput keyed to the original attribute
     * type (to get the correct sub-type filter).
     */
    private void fillSupCombo()
    {
        supComboViewer.setInput( new ATESuperiorComboInput( getOriginalAttributeType() ) );

        String supAtName = getModifiedAttributeType().getSuperiorOid();
        if ( supAtName == null )
        {
            supComboViewer.setSelection( new StructuredSelection( new NonExistingAttributeType(
                NonExistingAttributeType.NONE ) ), true );
        }
        else
        {
            AttributeType supAT = schemaHandler.getAttributeType( supAtName );
            if ( supAT != null )
            {
                supComboViewer.setSelection( new StructuredSelection( supAT ), true );
            }
            else
            {
                ATESuperiorComboInput input = ( ATESuperiorComboInput ) supComboViewer.getInput();
                NonExistingAttributeType neat = new NonExistingAttributeType( supAtName );
                if ( !input.getChildren().contains( neat ) )
                {
                    input.addChild( neat );
                }
                supComboViewer.refresh();
                supComboViewer.setSelection( new StructuredSelection( neat ), true );
            }
        }
    }


    // ── Luke Sets the Usage Dial ──────────────────────────────────────────────────────
    // Luke finds the usage dial and rotates it to the correct setting — one of four
    // positions depending on how this attribute type is categorised in the LDAP schema.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Selects the correct Usage combo item based on the working-copy attribute type's
     * usage value.
     * The combo items are at fixed indices (0-3) matching the order populated by
     * {@link #initUsageCombo()}: 0=Directory Operation, 1=Distributed Operation,
     * 2=DSA Operation, 3=User Applications.
     */
    private void fillInUsageCombo()
    {
        UsageEnum usage = getModifiedAttributeType().getUsage();

        switch ( usage )
        {
            case DIRECTORY_OPERATION:
                usageCombo.select( 0 );
                return;
            case DISTRIBUTED_OPERATION:
                usageCombo.select( 1 );
                return;
            case DSA_OPERATION:
                usageCombo.select( 2 );
                return;
            case USER_APPLICATIONS:
                usageCombo.select( 3 );
                return;
        }
    }


    // ── Luke Tunes the Syntax Scanner ────────────────────────────────────────────────
    // Luke tunes the scanner to the right syntax — the data format this attribute type
    // expects values to conform to.  If the syntax OID isn't in the schema, he creates
    // a phantom entry so the scanner still shows something useful.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Syntax combo and selects the correct entry.
     * Same pattern as {@link #fillSupCombo()}: null OID → "(None)", known OID → select
     * the real LdapSyntax, unknown OID → inject a NonExistingSyntax placeholder and
     * select it.
     */
    private void fillSyntaxCombo()
    {
        syntaxComboViewer.setInput( new ATESyntaxComboInput() );

        String syntaxOID = getModifiedAttributeType().getSyntaxOid();
        if ( syntaxOID == null )
        {
            syntaxComboViewer.setSelection( new StructuredSelection( new NonExistingSyntax( NonExistingSyntax.NONE ) ),
                true );
        }
        else
        {
            LdapSyntax syntax = schemaHandler.getSyntax( syntaxOID );
            if ( syntax != null )
            {
                syntaxComboViewer.setSelection( new StructuredSelection( syntax ), true );
            }
            else
            {
                ATESyntaxComboInput input = ( ATESyntaxComboInput ) syntaxComboViewer.getInput();
                NonExistingSyntax nes = new NonExistingSyntax( syntaxOID );
                if ( !input.getChildren().contains( nes ) )
                {
                    input.addChild( nes );
                }
                syntaxComboViewer.refresh();
                syntaxComboViewer.setSelection( new StructuredSelection( nes ), true );
            }
        }
    }


    // ── Luke Calibrates the Equality Sensor ──────────────────────────────────────────
    // Luke calibrates the equality sensor — how does the LDAP directory decide whether
    // two values of this attribute are equal?  If the referenced rule isn't in the
    // schema, he injects a phantom entry so the sensor shows the stored name.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Equality matching-rule combo and selects the correct entry.
     * Same null/resolve/placeholder pattern as the Syntax and Superior combos.
     */
    private void fillEqualityCombo()
    {
        equalityComboViewer.setInput( new ATEMatchingRulesComboInput() );

        String equalityName = getModifiedAttributeType().getEqualityOid();
        if ( equalityName == null )
        {
            equalityComboViewer.setSelection( new StructuredSelection( new NonExistingMatchingRule(
                NonExistingMatchingRule.NONE ) ), true );
        }
        else
        {
            MatchingRule matchingRule = schemaHandler.getMatchingRule( equalityName );
            if ( matchingRule != null )
            {
                equalityComboViewer.setSelection( new StructuredSelection( matchingRule ), true );
            }
            else
            {
                ATEMatchingRulesComboInput input = ( ATEMatchingRulesComboInput ) equalityComboViewer.getInput();
                NonExistingMatchingRule nemr = new NonExistingMatchingRule( equalityName );
                if ( !input.getChildren().contains( nemr ) )
                {
                    input.addChild( nemr );
                }
                equalityComboViewer.refresh();
                equalityComboViewer.setSelection( new StructuredSelection( nemr ), true );
            }
        }
    }


    // ── Luke Calibrates the Ordering Sensor ──────────────────────────────────────────
    // Luke calibrates the ordering sensor — how does the LDAP directory sort and compare
    // values less-than/greater-than?  Phantom entry injected if the rule is missing.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Ordering matching-rule combo and selects the correct entry.
     * Identical pattern to {@link #fillEqualityCombo()} — null/resolve/placeholder.
     */
    private void fillOrderingCombo()
    {
        orderingComboViewer.setInput( new ATEMatchingRulesComboInput() );

        String orderingName = getModifiedAttributeType().getOrderingOid();
        if ( orderingName == null )
        {
            orderingComboViewer.setSelection( new StructuredSelection( new NonExistingMatchingRule(
                NonExistingMatchingRule.NONE ) ), true );
        }
        else
        {
            MatchingRule matchingRule = schemaHandler.getMatchingRule( orderingName );
            if ( matchingRule != null )
            {
                orderingComboViewer.setSelection( new StructuredSelection( matchingRule ), true );
            }
            else
            {
                ATEMatchingRulesComboInput input = ( ATEMatchingRulesComboInput ) orderingComboViewer.getInput();
                NonExistingMatchingRule nemr = new NonExistingMatchingRule( orderingName );
                if ( !input.getChildren().contains( nemr ) )
                {
                    input.addChild( nemr );
                }
                orderingComboViewer.refresh();
                orderingComboViewer.setSelection( new StructuredSelection( nemr ), true );
            }
        }

    }


    // ── Luke Calibrates the Substring Sensor ─────────────────────────────────────────
    // Luke calibrates the substring sensor — how does the LDAP directory handle wildcard
    // searches like "Luke*" or "*walker"?  Phantom entry injected if the rule is missing.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Substring matching-rule combo and selects the correct entry.
     * Identical pattern to {@link #fillEqualityCombo()} — null/resolve/placeholder.
     */
    private void fillSubstringCombo()
    {
        substringComboViewer.setInput( new ATEMatchingRulesComboInput() );

        String substringName = getModifiedAttributeType().getSubstringOid();
        if ( substringName == null )
        {
            substringComboViewer.setSelection( new StructuredSelection( new NonExistingMatchingRule(
                NonExistingMatchingRule.NONE ) ), true );
        }
        else
        {
            MatchingRule matchingRule = schemaHandler.getMatchingRule( substringName );
            if ( matchingRule != null )
            {
                substringComboViewer.setSelection( new StructuredSelection( matchingRule ), true );
            }
            else
            {
                ATEMatchingRulesComboInput input = ( ATEMatchingRulesComboInput ) substringComboViewer.getInput();
                NonExistingMatchingRule nemr = new NonExistingMatchingRule( substringName );
                if ( !input.getChildren().contains( nemr ) )
                {
                    input.addChild( nemr );
                }
                substringComboViewer.refresh();
                substringComboViewer.setSelection( new StructuredSelection( nemr ), true );
            }
        }
    }


    // ── Luke Arms Every Sensor on the Ridge ──────────────────────────────────────────
    // Luke activates every sensor array on the Tatooine ridge — text fields, combo
    // selectors, checkboxes, hyperlinks, mouse-wheel filters — so that any change he
    // makes is immediately captured and reflected in the attribute type model.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches all event listeners to all UI widgets on the Overview page.
     * Called after {@link #fillInUiFields()} in the refresh cycle so that programmatic
     * field updates don't fire user-edit events.
     * We also install a display-level mouse-wheel filter to prevent scroll-wheel events
     * from changing combo values accidentally — a common usability issue with read-only
     * SWT combos.
     */
    protected void addListeners()
    {
        addModifyListener( aliasesText, aliasesTextModifyListener );
        addSelectionListener( aliasesButton, aliasesButtonListener );
        addModifyListener( oidText, oidTextModifyListener );
        addVerifyListener( oidText, oidTextVerifyListener );
        addHyperlinkListener( schemaLink, schemaLinkListener );
        addModifyListener( descriptionText, descriptionTextListener );
        addHyperlinkListener( supLabel, supLabelListener );
        addSelectionChangedListener( supComboViewer, supComboViewerListener );
        addModifyListener( usageCombo, usageComboListener );
        addSelectionChangedListener( syntaxComboViewer, syntaxComboViewerListener );
        addModifyListener( syntaxLengthText, syntaxLengthTextModifyListener );
        addVerifyListener( syntaxLengthText, syntaxLengthTextVerifyListener );
        addSelectionListener( obsoleteCheckbox, obsoleteCheckboxListener );
        addSelectionListener( singleValueCheckbox, singleValueCheckboxListener );
        addSelectionListener( collectiveCheckbox, collectiveCheckboxListener );
        addSelectionListener( noUserModificationCheckbox, noUserModificationCheckboxListener );
        addSelectionChangedListener( equalityComboViewer, equalityComboViewerListener );
        addSelectionChangedListener( orderingComboViewer, orderingComboViewerListener );
        addSelectionChangedListener( substringComboViewer, substringComboViewerListener );

        Display.getCurrent().addFilter( SWT.MouseWheel, mouseWheelFilter );
    }


    // ── Luke Powers Down the Sensor Array ────────────────────────────────────────────
    // Before a refresh or shutdown, Luke powers down all sensors so no spurious events
    // fire while the instrumentation is being recalibrated.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all event listeners from all UI widgets on the Overview page.
     * Called before {@link #fillInUiFields()} in the refresh cycle so that programmatic
     * field updates don't trigger user-edit handlers.
     * We also remove the display-level mouse-wheel filter symmetrically.
     */
    protected void removeListeners()
    {
        removeModifyListener( aliasesText, aliasesTextModifyListener );
        removeSelectionListener( aliasesButton, aliasesButtonListener );
        removeModifyListener( oidText, oidTextModifyListener );
        removeVerifyListener( oidText, oidTextVerifyListener );
        removeHyperlinkListener( schemaLink, schemaLinkListener );
        removeModifyListener( descriptionText, descriptionTextListener );
        removeHyperlinkListener( supLabel, supLabelListener );
        removeSelectionChangedListener( supComboViewer, supComboViewerListener );
        removeModifyListener( usageCombo, usageComboListener );
        removeSelectionChangedListener( syntaxComboViewer, syntaxComboViewerListener );
        removeModifyListener( syntaxLengthText, syntaxLengthTextModifyListener );
        removeVerifyListener( syntaxLengthText, syntaxLengthTextVerifyListener );
        removeSelectionListener( obsoleteCheckbox, obsoleteCheckboxListener );
        removeSelectionListener( singleValueCheckbox, singleValueCheckboxListener );
        removeSelectionListener( collectiveCheckbox, collectiveCheckboxListener );
        removeSelectionListener( noUserModificationCheckbox, noUserModificationCheckboxListener );
        removeSelectionChangedListener( equalityComboViewer, equalityComboViewerListener );
        removeSelectionChangedListener( orderingComboViewer, orderingComboViewerListener );
        removeSelectionChangedListener( substringComboViewer, substringComboViewerListener );

        Display.getCurrent().removeFilter( SWT.MouseWheel, mouseWheelFilter );
    }


    // ── Luke Leaves the Ridge and Powers Down ────────────────────────────────────────
    // Luke's vigil is over; he powers down the sensor array and deregisters from the
    // schema monitoring system before leaving the ridge for the last time.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up on page disposal: removes the schema handler listener (symmetrically
     * matching the registration done in the constructor) and delegates to the parent
     * dispose for SWT resource cleanup.
     * Always deregister the listener before calling super.dispose() to prevent callbacks
     * on disposed widgets.
     */
    public void dispose()
    {
        schemaHandler.removeListener( schemaHandlerListener );

        super.dispose();
    }
}
