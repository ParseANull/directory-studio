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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.AddEditDialog;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.swt.SWT;
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
import org.apache.directory.studio.openldap.common.ui.model.PasswordHashEnum;


// Like Princess Leia transmitting the list of approved encryption
// methods to Rebellion security officers, we project a hologram
// of password hash options and let the administrator pick the one
// they want to add to the server's accepted hash list.
/**
 * A dialog for selecting a single password hash method to add to the
 * OpenLDAP configuration. We present checkboxes for each supported
 * hash ({CLEARTEXT}, {CRYPT}, {LANMAN}, {MD5}, {SMD5}, {SHA}, {SSHA},
 * {UNIX}) and let the operator pick one per dialog invocation.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +-----------------------------------------+
 * | Password hash                           |
 * | .-------------------------------------. |
 * | | CLear Text : [ ]   Crypt :      [ ] | |
 * | | LANMAN :     [ ]   MD5 :        [ ] | |
 * | | SMD5 :       [ ]   SHA :        [ ] | |
 * | | SSHA :       [ ]   Unix :       [ ] | |
 * | '-------------------------------------' |
 * |                                         |
 * |  (Cancel)                         (OK)  |
 * +-----------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordHashDialog extends AddEditDialog<PasswordHashEnum>
{
    /** The array of buttons */
    private Button[] passwordHashCheckboxes = new Button[9];

    /** The already selected hashes */
    List<PasswordHashEnum> hashes = new ArrayList<>();

    // Like Leia keying up the hologram transmission with the RESIZE flag
    // so the security briefing window can be expanded for readability,
    // we create the dialog with resizable shell style so the operator
    // can see all the hash names clearly.
    /**
     * Creates a new PasswordHashDialog attached to the given parent shell.
     * We apply the RESIZE style so the operator can enlarge the window
     * if the hash names are hard to read at the default size.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public PasswordHashDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like labeling the hologram so the security team knows this is
    // the "Password Hash" briefing, we stamp the dialog shell with
    // the localized title before the window becomes visible.
    /**
     * Configures the dialog shell by setting its title to the localized
     * "PasswordHash" label.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "PasswordHash.Title" ) );
    }

    /**
     * The listener in charge of exposing the changes when some checkbox is selectionned
     */
    private SelectionListener checkboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Object object = e.getSource();

            if ( object instanceof Button )
            {
                Button selectedCheckbox = (Button)object;

                for ( int i = 1; i < passwordHashCheckboxes.length; i++ )
                {
                    if ( selectedCheckbox == passwordHashCheckboxes[i] )
                    {
                        setEditedElement( PasswordHashEnum.getPasswordHash( i ) );
                    }
                    else if ( passwordHashCheckboxes[i].isEnabled() )
                    {
                        passwordHashCheckboxes[i].setSelection( false );
                    }
                }
            }
        }
    };


    // Like Leia's hologram materializing with the full list of encryption
    // options laid out in a neat grid, we build the dialog content area
    // and initialize the checkbox states so already-selected hashes
    // are disabled and unavailable for re-selection.
    /**
     * Builds the main dialog content area, creating the password hash
     * checkbox group and initializing the selection state based on
     * what's already been configured.
     *
     * <pre>
     * +-----------------------------------------+
     * | Password hash                           |
     * | .-------------------------------------. |
     * | | CLear Text : [ ]   Crypt :      [ ] | |
     * | | LANMAN :     [ ]   MD5 :        [ ] | |
     * | | SMD5 :       [ ]   SHA :        [ ] | |
     * | | SSHA :       [ ]   Unix :       [ ] | |
     * | '-------------------------------------' |
     * |                                         |
     * |  (Cancel)                         (OK)  |
     * +-----------------------------------------+
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

        createPasswordHashEditGroup( composite );
        initDialog();

        applyDialogFont( composite );

        return composite;
    }


    // Like the security briefing crew arranging each hash algorithm name
    // neatly in the hologram grid and wiring each one up to the signal
    // receiver, we create a checkbox for each hash and attach the
    // selection listener so operator clicks are captured immediately.
    /**
     * Builds the password hash checkbox group, creating one checkbox per
     * available hash method in a two-column layout and attaching the
     * selection listener to each.
     *
     * <pre>
     * Password hash
     * .------------------.
     * | CLear Text : [ ] |
     * | Crypt :      [ ] |
     * | LANMAN :     [ ] |
     * | MD5 :        [ ] |
     * | SMD5 :       [ ] |
     * | SHA :        [ ] |
     * | SSHA :       [ ] |
     * | Unix :       [ ] |
     * '------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the hash group to
     */
    private void createPasswordHashEditGroup( Composite parent )
    {
        // Password Hash Group
        Group passwordHashGroup = BaseWidgetUtils.createGroup( parent, "", 2 );
        GridLayout passwordHashGridLayout = new GridLayout( 2, false );
        passwordHashGroup.setLayout( passwordHashGridLayout );
        passwordHashGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // The various buttons
        for ( int i = 1; i < passwordHashCheckboxes.length; i++ )
        {
            PasswordHashEnum passwordHash = PasswordHashEnum.getPasswordHash( i );
            Button button = BaseWidgetUtils.createCheckbox( passwordHashGroup, passwordHash.getName(), 1 );
            passwordHashCheckboxes[i] = button;
            passwordHashCheckboxes[i].addSelectionListener( checkboxSelectionListener );
        }
    }


    // Like the security briefing checking which hash methods are already
    // in the approved list before going live, we scan the configured
    // hashes and disable those checkboxes so the operator can't add
    // duplicates. If all hashes are already configured, we kill the OK button.
    /**
     * Initializes the dialog by examining which hash methods are already
     * configured and disabling those checkboxes accordingly. If every
     * available hash is already in use, the OK button is disabled.
     */
    protected void initDialog()
    {
        List<PasswordHashEnum> elements = getElements();
        boolean allSelected = true;
        okDisabled = false;

        for ( int i = 1; i < passwordHashCheckboxes.length; i++ )
        {
            PasswordHashEnum value = PasswordHashEnum.getPasswordHash( passwordHashCheckboxes[i].getText() );

            // Disable the hashes already selected
            if ( elements.contains( value ) )
            {
                passwordHashCheckboxes[i].setSelection( true );
                passwordHashCheckboxes[i].setEnabled( false );
            }
            else
            {
                allSelected = false;
            }
        }

        if ( allSelected )
        {
            // Disable the OK button
            okDisabled = true;
        }
    }


    // Like Leia's hologram defaulting to a blank placeholder before the
    // operator makes their selection, we seed the edited element with
    // NO_CHOICE as the safe starting point for a brand-new entry.
    /**
     * Seeds the dialog with a NO_CHOICE placeholder when the operator
     * is adding a brand-new password hash entry rather than editing
     * an existing one.
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( PasswordHashEnum.NO_CHOICE );
    }
}
