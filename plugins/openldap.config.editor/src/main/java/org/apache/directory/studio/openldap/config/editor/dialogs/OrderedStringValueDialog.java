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
package org.apache.directory.studio.openldap.config.editor.dialogs;


import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.apache.directory.studio.openldap.config.editor.wrappers.OrderedStringValueWrapper;


// Like Princess Leia beaming a simple one-line message to the Rebellion —
// just the essential content with no extra noise — we present a bare-bones
// dialog with a single text field so the operator can type in or edit
// one ordered string value and confirm their entry.
/**
 * A dialog for entering or editing a single ordered string value.
 * We present a single labeled text field and enable the OK button
 * as soon as the operator types anything into it.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +---------------------------------------+
 * | .-----------------------------------. |
 * | | Value  : [                      ] | |
 * | '-----------------------------------' |
 * |                                       |
 * |  (cancel)                       (OK)  |
 * +---------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OrderedStringValueDialog extends AddEditDialog<OrderedStringValueWrapper>
{
    // UI widgets
    /** The attribute name */
    private String attributeName;

    /** The String value */
    private Text stringValue;

    // Like Leia encoding her hologram with the attribute name label
    // and the RESIZE style so the recipient can expand the view,
    // we store the attribute name and configure the dialog shell
    // to be resizable for long attribute values.
    /**
     * Creates a new OrderedStringValueDialog for the given attribute name,
     * attached to the given parent shell. The attribute name is used as
     * both the dialog title and the text field label.
     *
     * @param parentShell the parent shell this dialog belongs to
     * @param attributeName the name of the attribute being edited
     */
    public OrderedStringValueDialog( Shell parentShell, String attributeName )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.attributeName = attributeName;
    }


    /**
     * The listener for the String Text
     */
    private ModifyListener stringValueTextListener = event ->
        {
            Button okButton = getButton( IDialogConstants.OK_ID );

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            getEditedElement().setValue( stringValue.getText() );
            okButton.setEnabled( true );
        };


    // Like Leia stamping the hologram with the attribute name so the
    // recipient knows exactly which field they're editing, we set the
    // dialog shell title to the attribute name.
    /**
     * Configures the dialog shell by setting its title to the attribute
     * name so the operator knows which field they're editing.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( attributeName );
    }


    // Like Leia's hologram projecting the single editable line with
    // a clear label so the operator knows exactly what to type,
    // we build the dialog with one labeled text field and wire up
    // the modify listener before handing control back.
    /**
     * Builds the dialog content area with a single labeled text field
     * for the ordered string value. The modify listener is attached so
     * the OK button enables as soon as the operator types anything.
     *
     * <pre>
     * +---------------------------------------+
     * | .-----------------------------------. |
     * | | Value  : [                      ] | |
     * | '-----------------------------------' |
     * |                                       |
     * |  (cancel)                       (OK)  |
     * +---------------------------------------+
     * </pre>
     *
     * @param parent the parent composite to build our content inside
     * @return the fully assembled dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        // StringValue Group
        Group stringValueGroup = BaseWidgetUtils.createGroup( parent, null, 1 );
        GridLayout stringValueGroupGridLayout = new GridLayout( 2, false );
        stringValueGroup.setLayout( stringValueGroupGridLayout );
        stringValueGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // String Text
        BaseWidgetUtils.createLabel( stringValueGroup, attributeName + ":", 1 );
        stringValue = BaseWidgetUtils.createText( stringValueGroup, "", 1 );
        stringValue.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        initDialog();
        addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // Like the hologram operator loading the current message content
    // before transmission so the recipient sees the existing value
    // and knows what they're editing, we pre-fill the text field
    // with whatever the wrapper already holds.
    /**
     * Initializes the text field from the current {@link OrderedStringValueWrapper},
     * pre-populating it with the existing value string (or leaving it
     * blank if the value is null).
     */
    protected void initDialog()
    {
        OrderedStringValueWrapper editedElement = getEditedElement();

        if ( editedElement != null )
        {
            String value = editedElement.getValue();

            if ( value == null )
            {
                stringValue.setText( "" );
            }
            else
            {
                stringValue.setText( editedElement.getValue() );
            }
        }
    }


    // Like Leia starting a completely fresh transmission with a blank
    // message and maximum ordering index, we seed the dialog with a
    // new empty wrapper so the operator can type in a brand-new value.
    /**
     * Seeds the dialog with a new empty {@link OrderedStringValueWrapper}
     * at the maximum ordering index when the operator is adding a new entry.
     */
    public void addNewElement()
    {
        setEditedElement( new OrderedStringValueWrapper( Integer.MAX_VALUE, "", true ) );
    }


    // Like handing the operator an existing message to edit and resend,
    // we clone the given wrapper and set the clone as the edited element
    // so any changes don't affect the original until the operator confirms.
    /**
     * Seeds the dialog with a clone of the given {@link OrderedStringValueWrapper}
     * so the operator's edits don't affect the original until they confirm.
     *
     * @param editedElement the existing ordered string value wrapper to clone
     */
    public void addNewElement( OrderedStringValueWrapper editedElement )
    {
        OrderedStringValueWrapper newElement = editedElement.clone();
        setEditedElement( newElement );
    }


    // Like connecting the hologram receiver to the live signal so changes
    // show up in real time, we attach the modify listener to the text
    // field so the edited element gets updated on every keystroke.
    /**
     * Attaches the modify listener to the string value text field so
     * the dialog updates the underlying wrapper and enables the OK button
     * whenever the operator types anything.
     */
    private void addListeners()
    {
        stringValue.addModifyListener( stringValueTextListener );
    }
}
