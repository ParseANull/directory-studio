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


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.openldap.config.editor.wrappers.DnWrapper;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// Like Princess Leia transmitting a single precise coordinate to
// the Rebellion — just the one thing that needs to be right —
// we present a focused dialog with a single text field for the DN,
// validate it live, and only allow confirmation when it parses cleanly.
/**
 * A dialog for entering or editing a single Distinguished Name (DN) value.
 * We validate the DN as the operator types, turning the text red if the
 * DN is malformed, and disabling the OK button until a valid DN is provided.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +---------------------------------------+
 * | .-----------------------------------. |
 * | | DN  : [                         ] | |
 * | '-----------------------------------' |
 * |                                       |
 * |  (cancel)                       (OK)  |
 * +---------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DnValueDialog extends AddEditDialog<DnWrapper>
{
    // UI widgets
    /** The DN Text */
    private Text dnText;

    // Like Leia keying up the hologram projector with the RESIZE flag
    // so whoever receives the transmission can adjust the view,
    // we create the dialog with a resizable shell style so the
    // operator can expand the text field if their DN is long.
    /**
     * Creates a new DnValueDialog attached to the given parent shell.
     * We apply the RESIZE style so the operator can widen the dialog
     * to comfortably fit long DN strings.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public DnValueDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    /**
     * The listener for the DN Text
     */
    private ModifyListener dnValueTextListener = event ->
        {
            Display display = dnText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            try
            {
                Dn dn = new Dn( dnText.getText() );

                getEditedElement().setDn( dn );
                okButton.setEnabled( true );
                dnText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
            }
            catch ( LdapInvalidDnException e1 )
            {
                okButton.setEnabled( false );
                dnText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
            }
        };


    // Like Leia labeling the hologram projector so the recipient knows
    // this is the "DN" transmission and not something else, we stamp
    // the dialog shell with the "DN" title before it becomes visible.
    /**
     * Configures the dialog shell by setting its title to "DN".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "DN" );
    }


    // Like Leia's hologram projecting the single crucial message field —
    // just the DN input box, nothing extra — we build the dialog area
    // here with a clean labeled text field and wire up the live-validation
    // listener so the operator gets immediate feedback.
    /**
     * Builds the dialog content area with a labeled DN text field.
     * We attach the modify listener so the field turns red and the
     * OK button disables whenever the entered DN is syntactically invalid.
     *
     * <pre>
     * +---------------------------------------+
     * | .-----------------------------------. |
     * | | Dn  : [                         ] | |
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

        // DnValue Group
        Group dnValueGroup = BaseWidgetUtils.createGroup( parent, null, 1 );
        GridLayout stringValueGroupGridLayout = new GridLayout( 2, false );
        dnValueGroup.setLayout( stringValueGroupGridLayout );
        dnValueGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // DN Text
        BaseWidgetUtils.createLabel( dnValueGroup, "DN :", 1 );
        dnText = BaseWidgetUtils.createText( dnValueGroup, "", 1 );
        dnText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        initDialog();
        addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // Like the hologram operator loading the existing target coordinates
    // before opening the transmission so the operator starts from the
    // right position, we pre-populate the DN text field with whatever
    // the current DnWrapper already holds.
    /**
     * Initializes the DN text field from the current {@link DnWrapper},
     * pre-populating it with the existing DN string (or leaving it
     * blank if the DN is null).
     */
    protected void initDialog()
    {
        DnWrapper editedElement = getEditedElement();

        if ( editedElement != null )
        {
            Dn dn = editedElement.getDn();

            if ( dn == null )
            {
                dnText.setText( "" );
            }
            else
            {
                dnText.setText( dn.toString() );
            }
        }
    }


    // Like Leia starting a new transmission from scratch with an empty
    // message container, we seed the edited element with an empty DN
    // so the operator's first keystroke goes into a fresh wrapper.
    /**
     * Seeds the dialog with an empty {@link DnWrapper} when the operator
     * is adding a brand-new DN value rather than editing an existing one.
     */
    public void addNewElement()
    {
        setEditedElement( new DnWrapper( Dn.EMPTY_DN ) );
    }


    // Like handing the operator a pre-addressed but unsent transmission
    // to edit and resend, we take an existing DnWrapper and set it
    // directly as the edited element — no clone needed since Dn is immutable.
    /**
     * Sets the given {@link DnWrapper} as the element to edit.
     * Since {@link Dn} is immutable, no cloning is necessary here.
     *
     * @param editedElement the existing DN wrapper to load into the dialog for editing
     */
    public void addNewElement( DnWrapper editedElement )
    {
        // No need to clone, the Dn is immutable
        setEditedElement( editedElement );
    }


    // Like connecting the hologram receiver to the transmission line
    // so incoming signals update the display immediately, we wire
    // the modify listener to the DN text field so every keystroke
    // triggers live validation.
    /**
     * Attaches the modify listener to the DN text field so the dialog
     * validates the DN on every keystroke and updates the OK button
     * and text color accordingly.
     */
    private void addListeners()
    {
        dnText.addModifyListener( dnValueTextListener );
    }
}
