/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.dialogs;

import org.apache.directory.api.ldap.model.schema.SchemaUtils;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.acl.model.AclAttribute;
import org.apache.directory.studio.openldap.config.acl.wrapper.AclAttributeWrapper;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

// ── CLASS: AclAttributeDialog — GRAND MOFF ISSUING ATTRIBUTE SECURITY DIRECTIVES
// Grand Moff Tarkin's officers present a terminal to whoever is editing the
// ACL attribute list. The officer (this dialog) shows five radio buttons —
// Attribute, Entry, Children, ObjectClass, ObjectClass Exclusion — and a
// text field for the name. The officer validates the name against Imperial
// schema rules (SchemaUtils) before letting the user confirm. If the name
// already exists on the manifest (duplicate), the text turns red and OK
// is locked. This dialog is the UI equivalent of filling out the attribute
// line on Tarkin's access manifest.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A modal add/edit dialog for a single {@link AclAttributeWrapper} row. Shows
 * five radio buttons (Attribute / Entry / Children / ObjectClass / ObjectClass
 * Exclusion) plus a text field for the attribute or class name. OK is enabled
 * only when the name is schema-valid and not a duplicate of an existing row.
 *
 * <pre>
 * +---------------------------------------------+
 * | ACL Attribute                               |
 * | .-----------------------------------------. |
 * | | (o) Attribute                           | |
 * | | (o) Entry                               | |
 * | | (o) Children                            | |
 * | | (o) ObjectClass                         | |
 * | | (o) ObjectClass exclusion               | |
 * | |                                         | |
 * | | Value : [/////////////////////////////] | |
 * | '-----------------------------------------' |
 * |                                             |
 * |  (Cancel)                             (OK)  |
 * +---------------------------------------------+
 * </pre>
 *
 * Think of this class as Tarkin's attribute assignment terminal — five
 * categories, one name, and a strict validation gate before the OK button
 * lights up.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclAttributeDialog extends AddEditDialog<AclAttributeWrapper>
{
    /** The connection to the LDAP server */
    private IBrowserConnection connection;

    // The UI widgets
    /** The Attribute checkbox */
    private Button attributeCheckbox;

    /** The entry checkbox */
    private Button entryCheckbox;

    /** The children checkbox */
    private Button childrenCheckbox;

    /** The OjectClass checkbox */
    private Button objectClassCheckbox;

    /** The OjectClass Exclusioncheckbox */
    private Button objectClassExclusionCheckbox;

    /** The Attribute Value text */
    private Text attributevalueText;

    /** A flag set when we clear the AttributeValue text */
    private boolean clearText;


    // ── Listening for the Attribute Radio Button ───────────────────────────────
    // When the officer clicks "Attribute", the terminal clears the name field
    // (can't reuse an entry/children name) and disables OK until a valid name
    // is typed.
    // ─────────────────────────────────────────────────────────────────────────
    /** A listener for the AttributeCheckBox */
    private SelectionListener attributeCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Button selection = (Button)e.getSource();

            if ( selection.getSelection() )
            {
                // Clear the AttributeValue Text and disable it
                clearText = true;
                attributevalueText.setText( "" );
                attributevalueText.setEnabled( true );
                getButton( IDialogConstants.OK_ID ).setEnabled( false );
            }
        }
    };

    // ── Listening for the Entry Radio Button ──────────────────────────────────
    // "Entry" is a fixed token — no name field needed. The officer clears the
    // text, disables the text field, and immediately enables OK.
    // ─────────────────────────────────────────────────────────────────────────
    /** A listener for the EntryCheckBox */
    private SelectionListener entryCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Button selection = (Button)e.getSource();

            if ( selection.getSelection() )
            {
                // Clear the AttributeValue Text and disable it
                clearText = true;
                attributevalueText.setText( "" );
                attributevalueText.setEnabled( false );
                getEditedElement().getAclAttribute().setName( "entry" );
                getButton( IDialogConstants.OK_ID ).setEnabled( true );
            }
        }
    };

    // ── Listening for the Children Radio Button ───────────────────────────────
    // Same as entry — "children" is a fixed token. Clear text, disable field,
    // enable OK immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /** A listener for the ChildrenCheckBox */
    private SelectionListener childrenCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Button selection = (Button)e.getSource();

            if ( selection.getSelection() )
            {
                // Clear the AttributeValue Text and disable it
                clearText = true;
                attributevalueText.setText( "" );
                attributevalueText.setEnabled( false );
                getEditedElement().getAclAttribute().setName( "children" );
                getButton( IDialogConstants.OK_ID ).setEnabled( true );
            }
        }
    };

    // ── Listening for the ObjectClass Radio Button ────────────────────────────
    // When the officer picks "ObjectClass", the text field is cleared and
    // enabled — the officer must type a valid OC name before OK lights up.
    // ─────────────────────────────────────────────────────────────────────────
    /** A listener for the ObjectClassCheckBox */
    private SelectionListener objectClassCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Button selection = (Button)e.getSource();

            if ( selection.getSelection() )
            {
                // Clear the AttributeValue Text and enable it
                clearText = true;
                attributevalueText.setText( "" );
                attributevalueText.setEnabled( true );
                getButton( IDialogConstants.OK_ID ).setEnabled( false );
            }
        }
    };

    // ── Listening for the ObjectClass Exclusion Radio Button ──────────────────
    // Same flow as ObjectClass — clear, enable field, wait for a valid name.
    // ─────────────────────────────────────────────────────────────────────────
    /** A listener for the ObjectClassExclusionCheckBox */
    private SelectionListener objectClassExclusionCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Button selection = (Button)e.getSource();

            if ( selection.getSelection() )
            {
                // Clear the AttributeValue Text and enable it
                clearText = true;
                attributevalueText.setText( "" );
                attributevalueText.setEnabled( true );
                getButton( IDialogConstants.OK_ID ).setEnabled( false );
            }
        }
    };

    // ── Validating the Name Text Field on Every Keystroke ─────────────────────
    // Every character the officer types runs through validation: is it a valid
    // schema name? Is it a duplicate? If either check fails, the text turns red
    // and OK is locked.
    // ─────────────────────────────────────────────────────────────────────────
    /** A listener for the AttributeValuetext */
    private ModifyListener attributeValueTextListener = new ModifyListener()
    {
        @Override
        public void modifyText( ModifyEvent e )
        {
            // if the text has been clear by a button, don't do anything but switch the flag
            if ( clearText )
            {
                clearText = false;
                return;
            }

            Button okButton = getButton( IDialogConstants.OK_ID );

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            String attributeValue = attributevalueText.getText();
            boolean isAttribute = attributeCheckbox.getSelection();
            boolean isObjectClass = objectClassCheckbox.getSelection();
            boolean isObjectExclusionClass = objectClassExclusionCheckbox.getSelection();
            boolean isEntry = entryCheckbox.getSelection();
            boolean isChildren = childrenCheckbox.getSelection();

            // Check that is a valid name, if needed
            if ( isAttribute || isObjectClass || isObjectExclusionClass )
            {
                if ( Strings.isEmpty( attributeValue ) )
                {
                    okButton.setEnabled( false );
                    return;
                }

                if ( !SchemaUtils.isAttributeNameValid( attributeValue) )
                {
                    okButton.setEnabled( false );
                    return;
                }
            }

            // Handle the various use cases
            String result;

            if ( isAttribute )
            {
                // This is an attribute
                result = attributeValue;
            }
            else if ( isEntry )
            {
                // This is the special attribute value Entry
                result = AclAttribute.ENTRY;
            }
            else if ( isChildren )
            {
                // This is the special attribute value Children
                result = AclAttribute.CHILDREN;
            }
            else if ( isObjectClass )
            {
                // This is an ObjectClass
                StringBuilder buffer = new StringBuilder();
                buffer.append( AclAttribute.OC ).append( attributeValue );

                result = buffer.toString();
            }
            else
            {
                // This is an ObjectClass exclusion
                StringBuilder buffer = new StringBuilder( AclAttribute.OC_EX );
                buffer.append( AclAttribute.OC_EX ).append( attributeValue );

                result = buffer.toString();
            }

            getEditedElement().setAclAttribute( result );

            // Check that the element does not already exist
            if ( getElements().contains( getEditedElement() ) )
            {
                attributevalueText.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.ERROR_COLOR ) );
                okButton.setEnabled( false );
            }
            else
            {
                attributevalueText.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.DEFAULT_COLOR ) );
                okButton.setEnabled( true );
            }
        }
    };


    // ── Constructing the Dialog ────────────────────────────────────────────────
    // Tarkin's officer sets up the terminal with a parent shell and a connection
    // to the live directory — so the name field can later do schema lookups.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new add/edit dialog for a single {@link AclAttributeWrapper}.
     *
     * <p>For example — the table widget opening the dialog for a new row:</p>
     * <pre>
     *   AclAttributeDialog dialog = new AclAttributeDialog(shell, connection);
     *   dialog.addNewElement();
     *   dialog.open();
     * </pre>
     *
     * @param shell       The parent shell for this dialog.
     * @param connection  The LDAP browser connection used for schema lookups; may be {@code null}.
     */
    public AclAttributeDialog( Shell shell, IBrowserConnection connection )
    {
        super( shell );
        //shell.setText( Messages.getString( "AclAttribute.Title" ) );
        this.connection = connection;
    }


    // ── Building the Dialog Area ───────────────────────────────────────────────
    // The officer sets up the entire terminal panel: the group with radio buttons
    // and text field, then initialises the widgets from the current element.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog body area. Creates the attribute edit group (radio buttons +
     * name text field) and initialises the widgets from the currently edited element.
     *
     * <pre>
     * +---------------------------------------------+
     * | ACL Attribute                               |
     * | .-----------------------------------------. |
     * | | (o) Attribute                           | |
     * | | (o) entry                               | |
     * | | (o) children                            | |
     * | | (o) ObjectClass                         | |
     * | | (o) ObjectClass exclusion               | |
     * | |                                         | |
     * | | Value : [/////////////////////////////] | |
     * | '-----------------------------------------' |
     * |                                             |
     * |  (Cancel)                             (OK)  |
     * +---------------------------------------------+
     * </pre>
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     * @param parent  The parent composite provided by the dialog framework.
     * @return        The top-level control for the dialog body.
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        createAclAttributeEditGroup( composite );
        initDialog();

        applyDialogFont( composite );

        return composite;
    }


    // ── Creating the Radio Button and Text Field Group ────────────────────────
    // Tarkin's terminal layout: five radio buttons stacked top to bottom, then
    // a "Value:" label and a free text field below.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the attribute input group containing the five category radio buttons
     * and the name text field. Listeners are attached immediately after creation.
     *
     * <pre>
     * ACL Attribute
     * .-----------------------------------------.
     * | (o) Attribute                           |
     * | (o) entry                               |
     * | (o) children                            |
     * | (o) ObjectClass                         |
     * | (o) ObjectClass exclusion               |
     * |                                         |
     * | Value : [/////////////////////////////] |
     * '-----------------------------------------'
     * </pre>
     *
     * @param parent  The composite to build the group inside.
     */
    private void createAclAttributeEditGroup( Composite parent )
    {
        // Disallow Feature Group
        Group aclAttributeGroup = BaseWidgetUtils.createGroup( parent, "", 1 );
        GridLayout aclAttributeGridLayout = new GridLayout( 2, false );
        aclAttributeGroup.setLayout( aclAttributeGridLayout );
        aclAttributeGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // The Attribute checkbox
        attributeCheckbox = BaseWidgetUtils.createRadiobutton( aclAttributeGroup, "Attribute", 2 );
        attributeCheckbox.addSelectionListener( attributeCheckboxListener );

        // The entry checkbox
        entryCheckbox = BaseWidgetUtils.createRadiobutton( aclAttributeGroup, "Entry", 2 );
        entryCheckbox.addSelectionListener( entryCheckboxListener );

        // The children checkbox
        childrenCheckbox = BaseWidgetUtils.createRadiobutton( aclAttributeGroup, "Children", 2 );
        childrenCheckbox.addSelectionListener( childrenCheckboxListener );

        // The OjectClass checkbox
        objectClassCheckbox = BaseWidgetUtils.createRadiobutton( aclAttributeGroup, "ObjectClass", 2 );
        objectClassCheckbox.addSelectionListener( objectClassCheckboxListener );

        // The OjectClass Exclusioncheckbox
        objectClassExclusionCheckbox = BaseWidgetUtils.createRadiobutton( aclAttributeGroup, "ObjectClass Exclusion", 2 );
        objectClassExclusionCheckbox.addSelectionListener( objectClassExclusionCheckboxListener );

        // The Value Text
        BaseWidgetUtils.createLabel( aclAttributeGroup, "Value : ", 1 );
        attributevalueText = BaseWidgetUtils.createText( aclAttributeGroup, "", 1 );
        attributevalueText.addModifyListener( attributeValueTextListener );
    }


    // ── Loading the Dialog With the Current Element's Values ──────────────────
    // The officer reads the element that is being edited and pre-selects the
    // correct radio button and name text so the user sees the current state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the dialog widgets from the currently edited element. Selects
     * the appropriate radio button and populates the text field based on whether
     * the attribute is an entry, children, attribute type, objectClass, or
     * objectClass exclusion.
     */
    @Override
    protected void initDialog()
    {
        AclAttributeWrapper editedElement = (AclAttributeWrapper)getEditedElement();

        if ( editedElement != null )
        {
            AclAttribute aclAttribute =  editedElement.getAclAttribute();

            if ( aclAttribute.isEntry() )
            {
                entryCheckbox.setEnabled( true );
            }
            else if ( aclAttribute.isChildren() )
            {
                childrenCheckbox.setEnabled( true );
            }
            else if ( aclAttribute.isAttributeType() )
            {
                attributeCheckbox.setEnabled( true );
                attributevalueText.setText( CommonUIUtils.getTextValue( aclAttribute.getName() ) );
            }
            else if ( aclAttribute.isObjectClass() )
            {
                objectClassCheckbox.setEnabled( true );
                attributevalueText.setText( CommonUIUtils.getTextValue( aclAttribute.getName() ) );
            }
            else
            {
                objectClassExclusionCheckbox.setEnabled( true );
                attributevalueText.setText( CommonUIUtils.getTextValue( aclAttribute.getName() ) );
            }
        }
    }


    // ── Preparing a Blank Element for the Add Flow ────────────────────────────
    // When the user clicks Add, the officer initialises an empty wrapper so
    // the dialog starts with a default extensibleObject element.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the table widget's Add button to create a blank
     * {@link AclAttributeWrapper} for the user to fill in. Defaults to
     * extensibleObject (via {@link AclAttribute#AclAttribute(IBrowserConnection)}).
     *
     * {@inheritDoc}
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( new AclAttributeWrapper( new AclAttribute( "", connection ) ) );
    }


    // ── Preparing a Pre-Filled Element for the Edit Flow ─────────────────────
    // When the user clicks Edit on an existing row, the officer clones the
    // wrapper so the dialog can change it without touching the live row until
    // OK is confirmed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the table widget's Edit button. Clones the given wrapper so the
     * dialog operates on a copy — the original row is only updated when the user
     * clicks OK.
     *
     * <p>For example — editing an existing row without corrupting the original:</p>
     * <pre>
     *   dialog.addNewElement(existingWrapper);
     *   // dialog opens; user edits; on OK the clone replaces the row
     * </pre>
     *
     * @param editedElement  The wrapper to clone for editing.
     */
    public void addNewElement( AclAttributeWrapper editedElement )
    {
        AclAttributeWrapper newElement = (AclAttributeWrapper)editedElement.clone();
        setEditedElement( newElement );
    }
}
