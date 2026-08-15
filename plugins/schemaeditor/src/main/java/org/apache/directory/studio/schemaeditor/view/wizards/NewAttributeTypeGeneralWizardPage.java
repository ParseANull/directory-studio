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
import org.apache.directory.studio.schemaeditor.view.dialogs.EditAttributeTypeAliasesDialog;
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


// ── CLASS: NewAttributeTypeGeneralWizardPage — Obi-Wan Briefs Luke In The Cantina ────
// In the Mos Eisley cantina, Obi-Wan sits Luke down and gives him the essentials before
// they leave for Alderaan: who you are, what your mission is, what name the galaxy knows
// you by. No technical details yet — just identity and purpose.
// This page plays the same role: it collects the identity of a new attribute type — which
// schema it belongs to, its globally unique OID, any aliases (human-readable names), and
// a description. Everything else comes later.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * The first wizard page in the New Attribute Type wizard, collecting identity information.
 * We ask for the schema the attribute belongs to, its OID (a dotted-number globally unique
 * identifier), its aliases (friendly names), and a description.
 * Think of this page as Obi-Wan's cantina briefing for Luke — just the essentials needed
 * to identify who this new attribute type is before we dig into what it can do.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewAttributeTypeGeneralWizardPage extends AbstractWizardPage
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


    // ── Obi-Wan And Luke Take A Corner Booth ─────────────────────────────────────────
    // Obi-Wan claims a quiet corner of the cantina, orders two drinks, and opens the
    // conversation: "You will come to know me, and your own identity, before we leave."
    // We initialize the page's title, description, and icon here — plus the schema handler
    // and an empty alias list ready to be filled in as the user types.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs this wizard page and sets up its title, description, and icon.
     * We also grab the SchemaHandler (our connection to all loaded schema projects) and
     * initialize an empty alias list that we'll populate as the user edits the Aliases field.
     */
    protected NewAttributeTypeGeneralWizardPage()
    {
        super( "NewAttributeTypeGeneralWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewAttributeTypeGeneralWizardPage.AttributeType" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewAttributeTypeGeneralWizardPage.CreateNewAttributeType" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_ATTRIBUTE_TYPE_NEW_WIZARD ) );

        schemaHandler = Activator.getDefault().getSchemaHandler();
        aliases = new ArrayList<Alias>();
    }


    // ── Obi-Wan Lays Out The Briefing Documents On The Table ─────────────────────────
    // Obi-Wan pulls out maps, contacts, and a holographic summary — each piece of paper
    // is one section of the briefing: schema membership, the OID, aliases, a description.
    // We build each SWT widget group here in order, then call initFields() to populate them.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT widgets that make up this wizard page and wires up their listeners.
     * Eclipse calls this method once, the first time this page becomes visible.
     * We lay out a Schema group and a Naming/Description group, then populate them with
     * {@link #initFields()}.
     *
     * @param parent  the parent composite Eclipse provides — we create our own child composite inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Schema Group
        Group schemaGroup = new Group( composite, SWT.NONE );
        schemaGroup.setText( Messages.getString( "NewAttributeTypeGeneralWizardPage.Schema" ) ); //$NON-NLS-1$
        schemaGroup.setLayout( new GridLayout( 2, false ) );
        schemaGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Schema
        Label schemaLabel = new Label( schemaGroup, SWT.NONE );
        schemaLabel.setText( Messages.getString( "NewAttributeTypeGeneralWizardPage.SchemaColon" ) ); //$NON-NLS-1$
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
        namingDescriptionGroup.setText( Messages.getString( "NewAttributeTypeGeneralWizardPage.NamingAndDescription" ) ); //$NON-NLS-1$
        namingDescriptionGroup.setLayout( new GridLayout( 3, false ) );
        namingDescriptionGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // OID
        Label oidLabel = new Label( namingDescriptionGroup, SWT.NONE );
        oidLabel.setText( Messages.getString( "NewAttributeTypeGeneralWizardPage.OID" ) ); //$NON-NLS-1$
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
        aliasesLabel.setText( Messages.getString( "NewAttributeTypeGeneralWizardPage.Aliases" ) ); //$NON-NLS-1$
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
        aliasesButton.setText( Messages.getString( "NewAttributeTypeGeneralWizardPage.Edit" ) ); //$NON-NLS-1$
        aliasesButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            public void widgetSelected( SelectionEvent arg0 )
            {
                EditAttributeTypeAliasesDialog dialog = new EditAttributeTypeAliasesDialog( getAliasesValue() );

                if ( dialog.open() == EditAttributeTypeAliasesDialog.OK )
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
        descriptionLabel.setText( Messages.getString( "NewAttributeTypeGeneralWizardPage.Description" ) ); //$NON-NLS-1$
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


    // ── Obi-Wan Checks Whether There Are Any Schema Projects To Work With ─────────────
    // Before briefing Luke, Obi-Wan first checks whether the Rebellion even has an active
    // base — if there's no schema project open, there's nothing to work with.
    // We disable all fields and show an error, or populate the schema combo from the handler.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Schema combo with all schemas from the current schema project, sorted
     * alphabetically by name.
     * If there's no schema handler (no project is open), we disable every field and show
     * an error so the user knows why nothing is editable.
     * We also mark the page incomplete at this point — the user must fill things in first.
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

            displayErrorMessage( Messages.getString( "NewAttributeTypeGeneralWizardPage.ErrorNoSchemaProjectOpen" ) ); //$NON-NLS-1$
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


    // ── Obi-Wan Re-Reads The Briefing After Luke Scribbles On It ─────────────────────
    // Every time Luke crosses something out or writes a new name on the map, Obi-Wan
    // re-reads the whole thing to check it still makes sense before they ship out.
    // We do the same: re-validate all fields every time the user changes something, and
    // display the first error or warning we encounter.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Re-validates all fields whenever the user changes any input on this page.
     * We check in priority order: schema selected, OID present, OID valid, OID unique,
     * at least one alias, and all aliases syntactically valid.
     * The page stays incomplete until all checks pass.
     */
    private void dialogChanged()
    {
        if ( schemaComboViewer.getSelection().isEmpty() )
        {
            displayErrorMessage( Messages.getString( "NewAttributeTypeGeneralWizardPage.ErrorNoSchemaSpecified" ) ); //$NON-NLS-1$
            return;
        }
        if ( oidCombo.getText().equals( "" ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "NewAttributeTypeGeneralWizardPage.ErrorNoOIDSpecified" ) ); //$NON-NLS-1$
            return;
        }
        if ( ( !oidCombo.getText().equals( "" ) ) && ( !Oid.isOid( oidCombo.getText() ) ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "NewAttributeTypeGeneralWizardPage.ErrorIncorrectOID" ) ); //$NON-NLS-1$
            return;
        }
        if ( ( !oidCombo.getText().equals( "" ) ) && ( Oid.isOid( oidCombo.getText() ) ) //$NON-NLS-1$
            && ( schemaHandler.isOidAlreadyTaken( oidCombo.getText() ) ) )
        {
            displayErrorMessage( Messages.getString( "NewAttributeTypeGeneralWizardPage.ErrorObjectOIDExists" ) ); //$NON-NLS-1$
            return;
        }
        if ( aliases.size() == 0 )
        {
            displayWarningMessage( Messages.getString( "NewAttributeTypeGeneralWizardPage.ErrorAttributeTypeNoName" ) ); //$NON-NLS-1$
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
                            Messages.getString( "NewAttributeTypeGeneralWizardPage.AliasStartInvalid" ), new Object[] { alias, ( ( AliasWithStartError ) alias ).getErrorChar() } ) ); //$NON-NLS-1$
                    return;
                }
                else if ( alias instanceof AliasWithPartError )
                {
                    displayErrorMessage( NLS
                        .bind(
                            Messages.getString( "NewAttributeTypeGeneralWizardPage.AliasPartInvalid" ), new Object[] { alias, ( ( AliasWithPartError ) alias ).getErrorChar() } ) ); //$NON-NLS-1$
                    return;
                }
            }
        }

        displayErrorMessage( null );
    }


    // ── Obi-Wan Rewrites Luke's Call Signs On The Mission Briefing ────────────────────
    // Obi-Wan takes the canonical alias list and writes them back into the briefing document
    // so the map shows "Red Five, Skywalker" instead of a jumble of internal codes.
    // We do the same: rebuild the alias text field from our internal alias list after the
    // user edits aliases through the dialog.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the aliases text field from the current in-memory alias list.
     * We call this after the user accepts changes from the Edit Aliases dialog, so the
     * text field stays in sync with the parsed internal representation.
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


    // ── Obi-Wan Reads Back Which Jedi Order Luke Belongs To ──────────────────────────
    // "You belong to the New Hope schema, Luke — that is the order you serve."
    // The schema name tells the wizard which schema container to add the new attribute to.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name of the schema the user selected in the Schema combo.
     * The wizard uses this at finish time to register the new attribute type under the
     * correct schema in the schema handler.
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


    // ── Obi-Wan Reads Back Luke's Galactic Serial Number ─────────────────────────────
    // Every Jedi in the order has a unique number in the Temple archives — Obi-Wan reads it
    // back from the briefing document so it goes on the official record.
    // The OID (Object Identifier) is a globally unique dotted number that identifies this
    // attribute type across every LDAP server in the universe.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OID string the user typed into the OID combo.
     * This is the globally unique dotted-number identifier for the new attribute type
     * (e.g. "1.2.3.4.5"). The wizard sets this as the primary key when creating the type.
     *
     * @return  the OID string as the user entered it.
     */
    public String getOidValue()
    {
        return oidCombo.getText();
    }


    // ── Obi-Wan Lists All The Call Signs Luke Goes By ────────────────────────────────
    // "The galaxy knows you as Luke, Skywalker, Red Five — all of these point to you."
    // Aliases are the human-readable names for the attribute type; the LDAP server accepts
    // any of them interchangeably alongside the OID.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of alias strings the user entered for this attribute type.
     * Aliases are the friendly names (like "cn", "commonName") that LDAP clients use
     * instead of the raw OID. We return them as plain strings, stripping internal parse metadata.
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


    // ── Obi-Wan Reads Back The Mission Description ────────────────────────────────────
    // "Here is what this mission is about, in plain words — so the rest of the Rebellion
    // understands what we're doing." Obi-Wan reads the description text back to Luke.
    // We just return whatever the user typed in the description text area.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the description text the user typed for this attribute type.
     * The description is a free-form human-readable explanation of what the attribute is for.
     * It ends up stored in the schema and is visible to any LDAP client that reads schema info.
     *
     * @return  the description string, possibly empty if the user left it blank.
     */
    public String getDescriptionValue()
    {
        return descriptionText.getText();
    }


    // ── Obi-Wan Pre-Selects The Destination Before The Briefing Starts ───────────────
    // Before Luke even sits down, Obi-Wan has already pointed to Alderaan on the map —
    // the destination is pre-selected so the briefing starts with context.
    // Callers (like the wizard itself) can pre-select the schema before the page is shown.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects a schema in the Schema combo before the page is displayed.
     * The wizard calls this when it knows from context (e.g. user right-clicked on a schema
     * node) which schema the new attribute type should go into.
     *
     * @param schema  the Schema object to pre-select — if null, nothing is pre-selected.
     */
    public void setSelectedSchema( Schema schema )
    {
        selectedSchema = schema;
    }
}
