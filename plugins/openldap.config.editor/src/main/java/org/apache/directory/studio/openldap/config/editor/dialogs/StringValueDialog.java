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
import org.apache.directory.studio.common.ui.wrappers.StringValueWrapper;
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


// Like Princess Leia transmitting a short, focused message with exactly
// the one piece of information that matters — nothing more, nothing less —
// we present a bare-bones dialog with a single labeled text field so the
// operator can type in or edit a simple multi-value string attribute.
/**
 * A dialog for entering or editing a single string value for a multi-value
 * attribute. We present one labeled text field and enable the OK button
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
public class StringValueDialog extends AddEditDialog<StringValueWrapper>
{
    // UI widgets
    /** The attribute name */
    private String attributeName;

    /** The String value */
    private Text stringValue;

    // Like Leia encoding the attribute name into the hologram header
    // so the recipient knows exactly what field they're editing,
    // we store the attribute name and configure the RESIZE style
    // so the dialog window can be expanded for long values.
    /**
     * Creates a new StringValueDialog for the given attribute name,
     * attached to the given parent shell. The attribute name is used
     * as both the dialog title and the text field label.
     *
     * @param parentShell the parent shell this dialog belongs to
     * @param attributeName the name of the attribute being edited
     */
    public StringValueDialog( Shell parentShell, String attributeName )
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


    // Like Leia stamping the hologram title with the attribute name so
    // the recipient knows exactly what message field they're reviewing,
    // we set the dialog shell title to the attribute name.
    /**
     * Configures the dialog shell by setting its title to the attribute
     * name so the operator knows which string field they're editing.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( attributeName );
    }


    // Like Leia's hologram projecting the one essential field with a
    // clear label so the operator knows exactly what to type in,
    // we build the dialog content area with a labeled text input
    // and wire up the live-update listener.
    /**
     * Builds the dialog content area with a single labeled text field
     * for the string value. The modify listener enables the OK button
     * as soon as the operator types anything.
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


    // Like the hologram operator loading the existing message content
    // before the editor opens so the recipient sees the current value
    // and can start editing from the right baseline, we pre-fill
    // the text field with the wrapper's current value string.
    /**
     * Initializes the text field from the current {@link StringValueWrapper},
     * pre-populating it with the existing value string (or leaving it
     * blank if the value is null).
     */
    protected void initDialog()
    {
        StringValueWrapper editedElement = getEditedElement();

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


    // Like Leia starting a brand-new transmission from a completely
    // blank template, we seed the dialog with an empty StringValueWrapper
    // so the operator starts from a clean slate.
    /**
     * Seeds the dialog with a new empty {@link StringValueWrapper} when the
     * operator is adding a brand-new string value entry.
     */
    public void addNewElement()
    {
        setEditedElement( new StringValueWrapper( "", true ) );
    }


    // Like handing the operator an existing message to edit and resend,
    // we clone the given wrapper and set the clone as the edited element
    // so the operator's changes don't affect the original until they confirm.
    /**
     * Seeds the dialog with a clone of the given {@link StringValueWrapper}
     * so the operator's edits don't affect the original until they confirm.
     *
     * @param editedElement the existing string value wrapper to clone
     */
    public void addNewElement( StringValueWrapper editedElement )
    {
        StringValueWrapper newElement = editedElement.clone();
        setEditedElement( newElement );
    }


    // Like connecting the live signal feed so every keystroke updates
    // the underlying data model in real time, we wire the modify listener
    // to the text field so the edited element stays current as the
    // operator types.
    /**
     * Attaches the modify listener to the string value text field so
     * the dialog updates the underlying wrapper and enables the OK
     * button whenever the operator types anything.
     */
    private void addListeners()
    {
        stringValue.addModifyListener( stringValueTextListener );
    }
}
