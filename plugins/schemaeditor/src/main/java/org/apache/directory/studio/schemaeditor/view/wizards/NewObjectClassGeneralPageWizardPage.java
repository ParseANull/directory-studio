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

import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.alias.Alias;
import org.apache.directory.studio.schemaeditor.model.alias.AliasWithPartError;
import org.apache.directory.studio.schemaeditor.model.alias.AliasWithStartError;
import org.apache.directory.studio.schemaeditor.model.alias.AliasesStringParser;
import org.apache.directory.studio.schemaeditor.view.dialogs.EditObjectClassAliasesDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// ── CLASS: NewObjectClassGeneralPageWizardPage — Palpatine Issues The Station's Identity ─
// In the Imperial Senate, Palpatine stands at the podium and announces the identity of the
// new station: which Imperial order it belongs to, its official designation number, the
// names by which the galaxy will know it, and its stated purpose.
// This is the identity briefing — not the technical specs, just the who and what.
// This page captures the same for a new LDAP object class: schema affiliation, OID,
// aliases, and description.
// ───────────────────────────────────────────────────────────────────────────────────────
/**
 * The first wizard page in the New Object Class wizard, collecting identity information.
 * We ask for the schema container, a globally unique OID, human-readable aliases, and a
 * description for the new object class.
 * Think of Palpatine formally declaring the identity of a new Imperial initiative at the
 * Senate: "This belongs to Order 66, designation 1.3.6.1.4.1.99, known as inetOrgPerson."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewObjectClassGeneralPageWizardPage extends AbstractWizardPage
{
    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    /** The aliases */
    private List<Alias> aliases;

    /** The selected schema */
    private Schema selectedSchema;

    // UI fields
    private ComboViewer schemaComboViewer;
    private Combo oidCombo;
    private Text aliasesText;
    private Button aliasesButton;
    private Text descriptionText;


    // ── Palpatine Calls The Senate To Order ──────────────────────────────────────────
    // Palpatine rises, announces the session's title and purpose, and displays the Imperial
    // seal — everything ceremonially initialized before the real work begins.
    // We set this page's title, description, and image here, then grab the schema handler
    // and initialize an empty alias list.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs this wizard page and sets its title, description, and image.
     * We grab the SchemaHandler at construction time and create an empty alias list that
     * will be populated as the user types in the Aliases field.
     */
    protected NewObjectClassGeneralPageWizardPage()
    {
        super( "NewObjectClassGeneralPageWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewObjectClassGeneralPageWizardPage.ObjectClass" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewObjectClassGeneralPageWizardPage.CreateObjectClass" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_OBJECT_CLASS_NEW_WIZARD ) );

        schemaHandler = Activator.getDefault().getSchemaHandler();
        aliases = new ArrayList<Alias>();
    }


    // ── Palpatine Unfolds The Full Declaration Document ──────────────────────────────
    // Palpatine unrolls the full declaration scroll: schema affiliation at the top, then
    // the designation (OID), then the aliases, then the official description — all in order.
    // We build the two SWT groups with their widgets, wire up their listeners, and call
    // initFields() to populate the schema combo.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT widgets for this page: a Schema group and a Naming/Description group.
     * Eclipse calls this once when the page first becomes visible.
     * Each field has a listener that calls {@link #dialogChanged()} to keep validation live.
     *
     * @param parent  the parent composite Eclipse provides — we nest our layout inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Schema Group
        Group schemaGroup = new Group( composite, SWT.NONE );
        schemaGroup.setText( Messages.getString( "NewObjectClassGeneralPageWizardPage.Schema" ) ); //$NON-NLS-1$
        schemaGroup.setLayout( new GridLayout( 2, false ) );
        schemaGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Schema
        Label schemaLabel = new Label( schemaGroup, SWT.NONE );
        schemaLabel.setText( Messages.getString( "NewObjectClassGeneralPageWizardPage.SchemaColon" ) ); //$NON-NLS-1$
        Combo schemaCombo = new Combo( schemaGroup, SWT.READ_ONLY );
        schemaCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        schemaComboViewer = new ComboViewer( schemaCombo );
        schemaComboViewer.setContentProvider( new ArrayContentProvider() );
        schemaComboViewer.setLabelProvider( new LabelProvider()
        {
            /**
             * {@inheritDoc}
             */
            public String getText( Object element )
            {
                if ( element instanceof Schema )
                {
                    return ( ( Schema ) element ).getSchemaName();
                }

                // Default
                return super.getText( element );
            }
        } );
        schemaComboViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            /**
             * {@inheritDoc}
             */
            public void selectionChanged( SelectionChangedEvent event )
            {
                dialogChanged();
            }
        } );

        // Naming and Description Group
        Group namingDescriptionGroup = new Group( composite, SWT.NONE );
        namingDescriptionGroup
            .setText( Messages.getString( "NewObjectClassGeneralPageWizardPage.NamingAndDescription" ) ); //$NON-NLS-1$
        namingDescriptionGroup.setLayout( new GridLayout( 3, false ) );
        namingDescriptionGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // OID
        Label oidLabel = new Label( namingDescriptionGroup, SWT.NONE );
        oidLabel.setText( Messages.getString( "NewObjectClassGeneralPageWizardPage.OID" ) ); //$NON-NLS-1$
        oidCombo = new Combo( namingDescriptionGroup, SWT.DROP_DOWN | SWT.BORDER );
        oidCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        oidCombo.addModifyListener( new ModifyListener()
        {
            /**
             * {@inheritDoc}
             */
            public void modifyText( ModifyEvent arg0 )
            {
                dialogChanged();
            }
        } );
        oidCombo.addVerifyListener( new VerifyListener()
        {
            /**
             * {@inheritDoc}
             */
            public void verifyText( VerifyEvent e )
            {
                if ( !e.text.matches( "([0-9]*\\.?)*" ) ) //$NON-NLS-1$
                {
                    e.doit = false;
                }
            }
        } );
        oidCombo.setItems( PluginUtils.loadDialogSettingsHistory( PluginConstants.DIALOG_SETTINGS_OID_HISTORY ) );

        // Aliases
        Label aliasesLabel = new Label( namingDescriptionGroup, SWT.NONE );
        aliasesLabel.setText( Messages.getString( "NewObjectClassGeneralPageWizardPage.Aliases" ) ); //$NON-NLS-1$
        aliasesText = new Text( namingDescriptionGroup, SWT.BORDER );
        aliasesText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        aliasesText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                AliasesStringParser parser = new AliasesStringParser();
                parser.parse( aliasesText.getText() );
                List<Alias> parsedAliases = parser.getAliases();
                aliases.clear();
                for ( Alias parsedAlias : parsedAliases )
                {
                    aliases.add( parsedAlias );
                }

                dialogChanged();
            }
        } );
        aliasesButton = new Button( namingDescriptionGroup, SWT.PUSH );
        aliasesButton.setText( Messages.getString( "NewObjectClassGeneralPageWizardPage.Edit" ) ); //$NON-NLS-1$
        aliasesButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            public void widgetSelected( SelectionEvent arg0 )
            {
                EditObjectClassAliasesDialog dialog = new EditObjectClassAliasesDialog( getAliasesValue() );

                if ( dialog.open() == EditObjectClassAliasesDialog.OK )
                {
                    String[] newAliases = dialog.getAliases();

                    StringBuffer sb = new StringBuffer();
                    for ( String newAlias : newAliases )
                    {
                        sb.append( newAlias );
                        sb.append( ", " ); //$NON-NLS-1$
                    }
                    sb.deleteCharAt( sb.length() - 1 );
                    sb.deleteCharAt( sb.length() - 1 );

                    AliasesStringParser parser = new AliasesStringParser();
                    parser.parse( sb.toString() );
                    List<Alias> parsedAliases = parser.getAliases();
                    aliases.clear();
                    for ( Alias parsedAlias : parsedAliases )
                    {
                        aliases.add( parsedAlias );
                    }

                    fillInAliasesLabel();
                    dialogChanged();
                }
            }
        } );

        // Description
        Label descriptionLabel = new Label( namingDescriptionGroup, SWT.NONE );
        descriptionLabel.setText( Messages.getString( "NewObjectClassGeneralPageWizardPage.Description" ) ); //$NON-NLS-1$
        descriptionText = new Text( namingDescriptionGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL );
        GridData descriptionGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 );
        descriptionGridData.heightHint = 67;
        descriptionText.setLayoutData( descriptionGridData );
        descriptionText.addModifyListener( new ModifyListener()
        {
            /**
             * {@inheritDoc}
             */
            public void modifyText( ModifyEvent arg0 )
            {
                dialogChanged();
            }
        } );

        initFields();

        setControl( composite );
    }


    // ── Palpatine Checks Whether The Senate Is Even In Session ───────────────────────
    // Before issuing a declaration, Palpatine checks whether there are any senators present
    // to receive it — if not, he locks the doors and displays an error message.
    // We disable all fields if there's no schema project open, or populate the schema combo
    // from the handler and pre-select a schema if one was injected.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Schema combo from the loaded schema project, sorted alphabetically.
     * If no schema project is open (schemaHandler is null), we disable all fields and
     * show an error explaining why.
     * We also pre-select a schema if one was set via {@link #setSelectedSchema(Schema)}.
     */
    private void initFields()
    {
        if ( schemaHandler == null )
        {
            schemaComboViewer.getCombo().setEnabled( false );
            oidCombo.setEnabled( false );
            aliasesText.setEnabled( false );
            aliasesButton.setEnabled( false );
            descriptionText.setEnabled( false );

            displayErrorMessage( Messages.getString( "NewObjectClassGeneralPageWizardPage.ErrorNoSchemaProjectOpen" ) ); //$NON-NLS-1$
        }
        else
        {
            // Filling the Schemas table
            List<Schema> schemas = new ArrayList<Schema>();
            schemas.addAll( schemaHandler.getSchemas() );

            Collections.sort( schemas, new Comparator<Schema>()
            {
                public int compare( Schema o1, Schema o2 )
                {
                    return o1.getSchemaName().compareToIgnoreCase( o2.getSchemaName() );
                }
            } );

            schemaComboViewer.setInput( schemas );

            if ( selectedSchema != null )
            {
                schemaComboViewer.setSelection( new StructuredSelection( selectedSchema ) );
            }

            displayErrorMessage( null );
        }

        setPageComplete( false );
    }


    // ── Palpatine Audits The Declaration Every Time A Senator Edits It ───────────────
    // Every time a senator crosses out a word or adds an amendment, Palpatine re-reads the
    // full declaration to make sure it's still valid before stamping it with the Imperial seal.
    // We re-validate all fields in priority order and show the first error or warning.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Re-validates all fields whenever the user changes any input on this page.
     * We check in order: schema selected, OID present and valid, OID unique, at least one
     * alias, all aliases syntactically valid.
     * The page stays incomplete until everything passes.
     */
    private void dialogChanged()
    {
        if ( schemaComboViewer.getSelection().isEmpty() )
        {
            displayErrorMessage( Messages.getString( "NewObjectClassGeneralPageWizardPage.ErrorNoSchemaSpecified" ) ); //$NON-NLS-1$
            return;
        }
        if ( oidCombo.getText().equals( "" ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "NewObjectClassGeneralPageWizardPage.ErrorNoOIDSpecified" ) ); //$NON-NLS-1$
            return;
        }
        if ( ( !oidCombo.getText().equals( "" ) ) && ( !Oid.isOid( oidCombo.getText() ) ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "NewObjectClassGeneralPageWizardPage.ErrorIncorrectOID" ) ); //$NON-NLS-1$
            return;
        }
        if ( ( !oidCombo.getText().equals( "" ) ) && ( Oid.isOid( oidCombo.getText() ) ) //$NON-NLS-1$
            && ( schemaHandler.isOidAlreadyTaken( oidCombo.getText() ) ) )
        {
            displayErrorMessage( Messages.getString( "NewObjectClassGeneralPageWizardPage.ErrorObjectOIDExists" ) ); //$NON-NLS-1$
            return;
        }
        if ( aliases.size() == 0 )
        {
            displayWarningMessage( Messages.getString( "NewObjectClassGeneralPageWizardPage.ErrorObjectClassNoName" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            for ( Alias alias : aliases )
            {
                if ( alias instanceof AliasWithStartError )
                {
                    displayErrorMessage( NLS
                        .bind(
                            Messages.getString( "NewObjectClassGeneralPageWizardPage.AliasStartInvalid" ), new Object[] { alias, ( ( AliasWithStartError ) alias ).getErrorChar() } ) ); //$NON-NLS-1$
                    return;
                }
                else if ( alias instanceof AliasWithPartError )
                {
                    displayErrorMessage( NLS
                        .bind(
                            Messages.getString( "NewObjectClassGeneralPageWizardPage.AliasPartInvalid" ), new Object[] { alias, ( ( AliasWithPartError ) alias ).getErrorChar() } ) ); //$NON-NLS-1$
                    return;
                }
            }
        }

        displayErrorMessage( null );
    }


    // ── Palpatine Rewrites The Official Name List After An Amendment ──────────────────
    // After a senator amends the list of official names for the new initiative, Palpatine
    // rewrites the declaration text so it reflects the current canonical list.
    // We rebuild the aliases text field from the in-memory alias list.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the aliases text field from the current in-memory alias list.
     * Called after the user accepts changes from the Edit Aliases dialog so the text
     * field stays synchronized with the parsed internal representation.
     */
    private void fillInAliasesLabel()
    {
        StringBuffer sb = new StringBuffer();

        for ( Alias alias : aliases )
        {
            sb.append( alias );
            sb.append( ", " ); //$NON-NLS-1$
        }

        sb.deleteCharAt( sb.length() - 1 );
        sb.deleteCharAt( sb.length() - 1 );

        aliasesText.setText( sb.toString() );
    }


    // ── Palpatine Reads Back Which Order This Initiative Belongs To ───────────────────
    // "This belongs to the Core schema — it is an Imperial standard, not a local matter."
    // The schema name tells the wizard where to register the new object class.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name of the schema the user selected in the Schema combo.
     * The wizard uses this at finish time to place the new object class in the correct schema.
     *
     * @return  the schema name string, or null if nothing is selected.
     */
    public String getSchemaValue()
    {
        StructuredSelection selection = ( StructuredSelection ) schemaComboViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            Schema schema = ( Schema ) selection.getFirstElement();

            return schema.getSchemaName();
        }
        else
        {
            return null;
        }
    }


    // ── Palpatine Reads Back The Official Designation Number ─────────────────────────
    // "Imperial designation: 1.2.840.113556.1.5.9 — recorded for posterity."
    // The OID is the globally unique dotted-number identifier for this object class.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OID string the user typed into the OID combo.
     * This becomes the primary identifier for the new object class across all LDAP servers.
     *
     * @return  the OID string as the user entered it.
     */
    public String getOidValue()
    {
        return oidCombo.getText();
    }


    // ── Palpatine Reads Back All Official Names For The Initiative ────────────────────
    // "It shall be known as inetOrgPerson, also as organizationalPerson — both names
    // are official." Palpatine lists every alias that will be recognized by the galaxy.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of alias strings the user entered for this object class.
     * Aliases are the friendly names LDAP clients use to reference the class (e.g. "person",
     * "inetOrgPerson"). We return plain strings, discarding internal parse metadata.
     *
     * @return  a list of alias strings; may be empty if the user hasn't entered any.
     */
    public List<String> getAliasesValue()
    {
        List<String> aliasesValue = new ArrayList<String>();

        for ( Alias alias : aliases )
        {
            aliasesValue.add( alias.toString() );
        }

        return aliasesValue;
    }


    // ── Palpatine Reads The Official Statement Of Purpose ────────────────────────────
    // "Its purpose: to hold the personal data of citizens in the Imperial records system."
    // The description is the free-form human-readable explanation of the object class.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the description text the user typed for this object class.
     * The description is stored in the schema definition and visible to LDAP clients
     * that query schema information.
     *
     * @return  the description string, possibly empty if the user left it blank.
     */
    public String getDescriptionValue()
    {
        return descriptionText.getText();
    }


    // ── Palpatine Pre-Loads The Target Schema Before The Session Opens ────────────────
    // Before the Senate convenes, Palpatine has already designated which order the new
    // initiative belongs to — the paperwork is pre-filled when senators arrive.
    // Context-menu callers inject the known schema so the user doesn't have to pick it.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects a schema in the Schema combo before the page is displayed.
     * The wizard calls this when the user invoked the action from a schema context (e.g.
     * right-clicking a schema node) so the page opens with the right schema already chosen.
     *
     * @param schema  the Schema to pre-select — if null, no pre-selection is applied.
     */
    public void setSelectedSchema( Schema schema )
    {
        selectedSchema = schema;
    }
}
