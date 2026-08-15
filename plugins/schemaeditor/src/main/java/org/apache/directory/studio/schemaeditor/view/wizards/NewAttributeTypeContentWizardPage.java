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
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.view.dialogs.AttributeTypeSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;


// ── CLASS: NewAttributeTypeContentWizardPage — Luke Discovers What The Force Carries ──
// On Dagobah, Yoda teaches Luke that the Force isn't just power — it has substance, rules,
// and properties. Luke must understand what it actually carries before he can wield it.
// This page does the same: it captures the substance of a new attribute type — its syntax
// (what kind of data it holds), its usage (who uses it), and behavioral flags like
// single-value and collective.
// ────────────────────────────────────────────────────────────────────────────────────────
/**
 * The second wizard page in the New Attribute Type wizard, covering content details.
 * We collect the substance of the attribute here: what it inherits from (superior),
 * what kind of data it holds (syntax), how it's used (usage), and flags like obsolete or single-value.
 * Think of this page as Yoda explaining to Luke what the Force actually carries — the nature
 * of the power, not just its name.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewAttributeTypeContentWizardPage extends AbstractWizardPage
{
    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    // UI Fields
    private Text superiorText;
    private Button superiorButton;
    private ComboViewer usageComboViewer;
    private ComboViewer syntaxComboViewer;
    private Spinner lengthSpinner;
    private Button obsoleteCheckbox;
    private Button singleValueCheckbox;
    private Button collectiveCheckbox;
    private Button noUserModificationCheckbox;


    // ── Luke Arrives On Dagobah, Training Begins ─────────────────────────────────────
    // Luke crash-lands on Dagobah and meets Yoda, who sizes him up before training starts.
    // Yoda sets the stage: titles the lesson, explains what they'll cover, loads the imagery.
    // We do the same here — set the page title, description, and icon so the user knows
    // exactly what kind of information this step is asking for.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of this page and initializes its title, description, and icon.
     * We also grab the SchemaHandler here — we need it to populate the syntax combo later.
     * Without the SchemaHandler we can't look up what syntaxes exist in the loaded schema project.
     */
    protected NewAttributeTypeContentWizardPage()
    {
        super( "NewAttributeTypeContentWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewAttributeTypeContentWizardPage.AttributTypeContent" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewAttributeTypeContentWizardPage.EnterAttributeTypeContent" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_ATTRIBUTE_TYPE_NEW_WIZARD ) );
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── Yoda Builds The Training Obstacle Course ──────────────────────────────────────
    // Yoda constructs the vine-tangled Dagobah training circuit — the exact tools Luke will
    // use to learn about the Force: handstands, boulders, roots, all laid out in sequence.
    // Each widget we build here — the superior text field, the usage combo, the syntax combo,
    // the spinner, the checkboxes — is one station in that circuit.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT widgets that make up this wizard page.
     * Eclipse calls this once when the page is first displayed; we lay out three groups:
     * Superior/Usage, Syntax, and Properties, then call {@link #initFields()} to populate them.
     *
     * @param parent  the parent composite Eclipse hands us — we attach our own composite to it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Superior and Usage Group
        Group superiorUsageGroup = new Group( composite, SWT.NONE );
        superiorUsageGroup.setText( Messages.getString( "NewAttributeTypeContentWizardPage.SuperiorAndUsage" ) ); //$NON-NLS-1$
        superiorUsageGroup.setLayout( new GridLayout( 3, false ) );
        superiorUsageGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Superior
        Label superiorLabel = new Label( superiorUsageGroup, SWT.NONE );
        superiorLabel.setText( Messages.getString( "NewAttributeTypeContentWizardPage.Superior" ) ); //$NON-NLS-1$
        superiorText = new Text( superiorUsageGroup, SWT.BORDER );
        superiorText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        superiorText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent arg0 )
            {
                verifySuperior();
            }
        } );
        superiorButton = new Button( superiorUsageGroup, SWT.PUSH );
        superiorButton.setText( Messages.getString( "NewAttributeTypeContentWizardPage.Choose" ) ); //$NON-NLS-1$
        superiorButton.setLayoutData( new GridData( SWT.NONE, SWT.NONE, false, false ) );
        superiorButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                AttributeTypeSelectionDialog dialog = new AttributeTypeSelectionDialog();
                if ( dialog.open() == Dialog.OK )
                {
                    AttributeType selectedAT = dialog.getSelectedAttributeType();
                    List<String> aliases = selectedAT.getNames();
                    if ( ( aliases != null ) && ( aliases.size() > 0 ) )
                    {
                        superiorText.setText( aliases.get( 0 ) );
                    }
                    else
                    {
                        superiorText.setText( selectedAT.getOid() );
                    }
                }
            }
        } );

        // Usage
        Label usageLabel = new Label( superiorUsageGroup, SWT.NONE );
        usageLabel.setText( Messages.getString( "NewAttributeTypeContentWizardPage.Usage" ) ); //$NON-NLS-1$
        Combo usageCombo = new Combo( superiorUsageGroup, SWT.READ_ONLY );
        usageCombo.setLayoutData( new GridData( SWT.NONE, SWT.NONE, false, false, 2, 1 ) );
        usageComboViewer = new ComboViewer( usageCombo );
        usageComboViewer.setLabelProvider( new LabelProvider() );
        usageComboViewer.setContentProvider( new ArrayContentProvider() );
        usageComboViewer
            .setInput( new String[]
                {
                    Messages.getString( "NewAttributeTypeContentWizardPage.DirectoryOperation" ), Messages.getString( "NewAttributeTypeContentWizardPage.DistributedOperation" ), Messages.getString( "NewAttributeTypeContentWizardPage.DSAOperation" ), Messages.getString( "NewAttributeTypeContentWizardPage.UserApplications" ) } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        usageComboViewer.setSelection( new StructuredSelection( Messages
            .getString( "NewAttributeTypeContentWizardPage.UserApplications" ) ) ); //$NON-NLS-1$

        // Syntax Group
        Group syntaxGroup = new Group( composite, SWT.NONE );
        syntaxGroup.setText( Messages.getString( "NewAttributeTypeContentWizardPage.Syntax" ) ); //$NON-NLS-1$
        syntaxGroup.setLayout( new GridLayout( 2, false ) );
        syntaxGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Syntax
        Label syntaxLabel = new Label( syntaxGroup, SWT.NONE );
        syntaxLabel.setText( Messages.getString( "NewAttributeTypeContentWizardPage.SyntaxColon" ) ); //$NON-NLS-1$
        Combo syntaxCombo = new Combo( syntaxGroup, SWT.READ_ONLY );
        syntaxCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        syntaxComboViewer = new ComboViewer( syntaxCombo );
        syntaxComboViewer.setContentProvider( new ArrayContentProvider() );
        syntaxComboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof LdapSyntax )
                {
                    LdapSyntax syntax = ( LdapSyntax ) element;

                    // Getting description (and name for backward compatibility)
                    String description = syntax.getDescription();
                    String name = syntax.getName();

                    if ( ( description != null ) || ( name != null ) )
                    {
                        if ( description != null )
                        {
                            // Using description
                            return NLS
                                .bind(
                                    Messages.getString( "NewAttributeTypeContentWizardPage.NameOID" ), new String[] { description, syntax.getOid() } ); //$NON-NLS-1$
                        }
                        else
                        {
                            // Using name (for backward compatibility)
                            return NLS
                                .bind(
                                    Messages.getString( "NewAttributeTypeContentWizardPage.NameOID" ), new String[] { name, syntax.getOid() } ); //$NON-NLS-1$
                        }
                    }
                    else
                    {
                        return NLS
                            .bind(
                                Messages.getString( "NewAttributeTypeContentWizardPage.NoneOID" ), new String[] { syntax.getOid() } ); //$NON-NLS-1$
                    }
                }

                return super.getText( element );
            }
        } );

        // Syntax Length
        Label lengthLabel = new Label( syntaxGroup, SWT.NONE );
        lengthLabel.setText( Messages.getString( "NewAttributeTypeContentWizardPage.Length" ) ); //$NON-NLS-1$
        lengthSpinner = new Spinner( syntaxGroup, SWT.BORDER );
        lengthSpinner.setIncrement( 1 );
        lengthSpinner.setMinimum( 0 );
        lengthSpinner.setMaximum( Integer.MAX_VALUE );
        GridData lengthSpinnerGridData = new GridData( SWT.NONE, SWT.NONE, false, false );
        lengthSpinnerGridData.widthHint = 42;
        lengthSpinner.setLayoutData( lengthSpinnerGridData );

        // Properties Group
        Group propertiesGroup = new Group( composite, SWT.NONE );
        propertiesGroup.setText( Messages.getString( "NewAttributeTypeContentWizardPage.Properties" ) ); //$NON-NLS-1$
        propertiesGroup.setLayout( new GridLayout() );
        propertiesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Obsolete
        new Label( composite, SWT.NONE );
        obsoleteCheckbox = new Button( propertiesGroup, SWT.CHECK );
        obsoleteCheckbox.setText( Messages.getString( "NewAttributeTypeContentWizardPage.Obsolete" ) ); //$NON-NLS-1$

        // Single value
        new Label( composite, SWT.NONE );
        singleValueCheckbox = new Button( propertiesGroup, SWT.CHECK );
        singleValueCheckbox.setText( Messages.getString( "NewAttributeTypeContentWizardPage.SingleValue" ) ); //$NON-NLS-1$

        // Collective
        new Label( composite, SWT.NONE );
        collectiveCheckbox = new Button( propertiesGroup, SWT.CHECK );
        collectiveCheckbox.setText( Messages.getString( "NewAttributeTypeContentWizardPage.Collective" ) ); //$NON-NLS-1$

        // No User Modification
        new Label( composite, SWT.NONE );
        noUserModificationCheckbox = new Button( propertiesGroup, SWT.CHECK );
        noUserModificationCheckbox
            .setText( Messages.getString( "NewAttributeTypeContentWizardPage.NoUserModifcation" ) ); //$NON-NLS-1$

        initFields();

        setControl( composite );
    }


    // ── Yoda Loads The Training Roster Of Known Forces ────────────────────────────────
    // Before Luke's first session, Yoda lists every known aspect of the Force he might
    // encounter — alphabetically organized so nothing is missed.
    // We pull the syntaxes from the schema handler, sort them by description, and populate
    // the syntax combo so the user can pick one — defaulting to "(None)".
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the syntax combo viewer with all LDAP syntaxes known to the current schema project.
     * We sort them alphabetically by description (or name) and prepend a "(None)" option so
     * the user doesn't have to pick a syntax if they don't want one.
     * This runs at page construction time, not lazily, so the combo is ready when the page appears.
     */
    private void initFields()
    {
        if ( schemaHandler != null )
        {
            // Getting the syntaxes
            List<Object> syntaxes = new ArrayList<Object>( schemaHandler.getSyntaxes() );
            // Adding the (None) Syntax
            String none = Messages.getString( "NewAttributeTypeContentWizardPage.None" ); //$NON-NLS-1$
            syntaxes.add( none );

            // Sorting the syntaxes
            Collections.sort( syntaxes, new Comparator<Object>()
            {
                public int compare( Object o1, Object o2 )
                {
                    if ( ( o1 instanceof LdapSyntax ) && ( o2 instanceof LdapSyntax ) )
                    {
                        String o1description = ( ( LdapSyntax ) o1 ).getDescription();
                        String o2description = ( ( LdapSyntax ) o2 ).getDescription();

                        String o1Name = ( ( LdapSyntax ) o1 ).getName();
                        String o2Name = ( ( LdapSyntax ) o2 ).getName();

                        // Comparing by description
                        if ( ( o1description != null ) && ( o2description != null ) )
                        {
                            return o1description.compareToIgnoreCase( o2description );
                        }
                        // Comparing by name
                        else if ( ( o1Name != null ) && ( o2Name != null ) )
                        {
                            return o1Name.compareToIgnoreCase( o2Name );
                        }
                    }
                    else if ( ( o1 instanceof String ) && ( o2 instanceof LdapSyntax ) )
                    {
                        return Integer.MIN_VALUE;
                    }
                    else if ( ( o1 instanceof LdapSyntax ) && ( o2 instanceof String ) )
                    {
                        return Integer.MAX_VALUE;
                    }

                    // Default
                    return o1.toString().compareToIgnoreCase( o2.toString() );
                }
            } );

            // Setting the input
            syntaxComboViewer.setInput( syntaxes );
            syntaxComboViewer.setSelection( new StructuredSelection( none ) );
        }
    }


    // ── Yoda Checks Whether Luke Has The Right Ancestry ──────────────────────────────
    // Yoda probes Luke's lineage — "Strong in the Force your family is" — to verify the
    // superior attribute type is actually present in the schema.
    // If the user types a superior attribute name that doesn't exist in the loaded schema,
    // we flag it immediately so they can correct it before moving on.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates that whatever the user typed in the Superior field is a real attribute type
     * in the currently loaded schema project.
     * If it's empty we clear any error (optional field); if it's non-empty and unknown, we
     * show an error message so the user knows to fix it before finishing the wizard.
     */
    private void verifySuperior()
    {
        String superior = superiorText.getText();
        if ( ( superior != null ) && ( !superior.equals( "" ) ) ) //$NON-NLS-1$
        {
            if ( schemaHandler.getAttributeType( superiorText.getText() ) == null )
            {
                displayErrorMessage( Messages
                    .getString( "NewAttributeTypeContentWizardPage.ErrorSuperiorAttributeTypeNotExists" ) ); //$NON-NLS-1$
                return;
            }
        }

        displayErrorMessage( null );
    }


    // ── Luke Reports His Lineage To Yoda ─────────────────────────────────────────────
    // Yoda asks: "Who trained you?" and Luke answers with his master's name — or says nothing
    // if he has no formal lineage yet.
    // We return the text from the superior field — empty becomes null to signal "no superior".
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name or OID of the superior attribute type the user entered, or null if none.
     * The wizard uses this to set the superiorOid on the new AttributeType object at finish time.
     *
     * @return  the superior attribute type name/OID string, or null if the field is blank.
     */
    public String getSuperiorValue()
    {
        String superior = superiorText.getText();
        if ( ( superior != null ) && ( !superior.equals( "" ) ) ) //$NON-NLS-1$
        {
            return superior;
        }
        else
        {
            return null;
        }
    }


    // ── Yoda Reveals What Purpose This Aspect Of The Force Serves ────────────────────
    // Yoda explains to Luke: "Used by the directory operation this attribute is" or "User
    // applications — the common folk — are its purpose."
    // The Usage field tells the LDAP server whether this attribute is for user data,
    // directory-internal operations, or distributed server coordination.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP usage enum value that matches what the user selected in the Usage combo.
     * Usage controls who can see and write the attribute — user apps, directory operations, etc.
     * Defaults to {@link UsageEnum#USER_APPLICATIONS} if nothing is selected.
     *
     * @return  the selected {@link UsageEnum} value — never null.
     */
    public UsageEnum getUsageValue()
    {
        StructuredSelection selection = ( StructuredSelection ) usageComboViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            String selectedUsage = ( String ) selection.getFirstElement();
            if ( selectedUsage.equals( Messages.getString( "NewAttributeTypeContentWizardPage.DirectoryOperation" ) ) ) //$NON-NLS-1$
            {
                return UsageEnum.DIRECTORY_OPERATION;
            }
            else if ( selectedUsage.equals( Messages
                .getString( "NewAttributeTypeContentWizardPage.DistributedOperation" ) ) ) //$NON-NLS-1$
            {
                return UsageEnum.DISTRIBUTED_OPERATION;
            }
            else if ( selectedUsage.equals( Messages.getString( "NewAttributeTypeContentWizardPage.DSAOperation" ) ) ) //$NON-NLS-1$
            {
                return UsageEnum.DSA_OPERATION;
            }
            else if ( selectedUsage.equals( Messages.getString( "NewAttributeTypeContentWizardPage.UserApplications" ) ) ) //$NON-NLS-1$
            {
                return UsageEnum.USER_APPLICATIONS;
            }
            else
            {
                return UsageEnum.USER_APPLICATIONS;
            }
        }
        else
        {
            return UsageEnum.USER_APPLICATIONS;
        }
    }


    // ── Yoda Names The Shape Of The Force Luke Carries ───────────────────────────────
    // Yoda tells Luke: "Binary it is — ones and zeros, the data flows as integers."
    // In LDAP terms, the syntax is the data type — is this attribute a string? An integer?
    // A Distinguished Name? We return the OID of whatever syntax the user selected.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OID of the LDAP syntax the user selected in the Syntax combo.
     * The syntax OID tells the LDAP server how to parse and validate this attribute's values.
     * Returns null if the user chose "(None)", meaning no syntax constraint.
     *
     * @return  the syntax OID string, or null if no syntax was selected.
     */
    public String getSyntax()
    {
        Object selection = ( ( StructuredSelection ) syntaxComboViewer.getSelection() ).getFirstElement();

        if ( selection instanceof LdapSyntax )
        {
            return ( ( LdapSyntax ) selection ).getOid();
        }

        return null;
    }


    // ── Yoda Measures How Much Force Luke Can Channel At Once ─────────────────────────
    // Yoda sets a limit: "Only so many midichlorians can flow through one vessel at a time."
    // The syntax length limits how many bytes a single attribute value can be.
    // Zero means no limit, which is what the spinner defaults to.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum allowed byte length for a single value of this attribute type.
     * This corresponds to the optional length in the LDAP attribute type's syntax definition.
     * A value of zero means the server applies no length restriction.
     *
     * @return  the syntax length as an int, 0 meaning unlimited.
     */
    public int getSyntaxLengthValue()
    {
        return lengthSpinner.getSelection();
    }


    // ── Yoda Marks An Ancient Teaching As No Longer Valid ────────────────────────────
    // Some Force teachings from the old Jedi Order are marked "obsolete" — Yoda keeps them
    // in the archives but flags them so students know not to rely on them.
    // An obsolete attribute type still exists in the schema but signals that new entries
    // shouldn't use it anymore.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user checked the "Obsolete" checkbox.
     * An obsolete attribute type is still defined in the schema but is discouraged from
     * further use — kind of a deprecation flag in LDAP terms.
     *
     * @return  true if the user marked this attribute type as obsolete.
     */
    public boolean getObsoleteValue()
    {
        return obsoleteCheckbox.getSelection();
    }


    // ── Yoda Teaches Luke The Rule Of One ────────────────────────────────────────────
    // "One lightsaber you carry — not two, not three. Single value, its nature is."
    // A single-value attribute means each LDAP entry can hold at most one value for it.
    // Think of it like a field in a database table where UNIQUE applies.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user checked the "Single Value" checkbox.
     * When true, the LDAP server will reject any attempt to store more than one value
     * for this attribute in a single directory entry.
     *
     * @return  true if this attribute type is single-valued.
     */
    public boolean getSingleValueValue()
    {
        return singleValueCheckbox.getSelection();
    }


    // ── Yoda Explains The Shared Nature Of A Collective Force ─────────────────────────
    // Yoda tells Luke about collective Force bonds: "Shared across a group of entries,
    // this attribute is — every padawan in the class inherits it from their teacher."
    // Collective attributes in LDAP propagate down a subtree rather than living on one entry.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user checked the "Collective" checkbox.
     * Collective attributes in LDAP are inherited by all entries in a subtree; they can't
     * be set individually on a single entry.
     *
     * @return  true if this attribute type is collective.
     */
    public boolean getCollectiveValue()
    {
        return collectiveCheckbox.getSelection();
    }


    // ── Yoda Restricts Luke From Touching Certain Temple Archives ─────────────────────
    // "Modified by users, this record cannot be — only the server itself may update it."
    // Yoda locks certain holocron entries so only directory operations can change them,
    // not user applications. That's exactly what the No-User-Modification flag does.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user checked the "No User Modification" checkbox.
     * When true, this attribute can only be written by the directory server itself —
     * user applications that try to modify it will get an error from the LDAP server.
     *
     * @return  true if user modification is forbidden on this attribute type.
     */
    public boolean getNoUserModificationValue()
    {
        return noUserModificationCheckbox.getSelection();
    }
}
