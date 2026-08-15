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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.openldap.common.ui.model.DatabaseTypeEnum;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;


// Like Princess Leia transmitting the Death Star plans with a clear
// selection of the one thing the Rebellion needs most, we present
// a dropdown of database types and let the operator pick exactly
// the one they want for the new database they're creating.
/**
 * A dialog for selecting the database type when creating a new OpenLDAP
 * database entry. We display a dropdown combo populated with all supported
 * database types (MDB, BDB, HDB, LDAP, LDIF, etc.) and capture the
 * operator's choice before handing it back to the editor.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +-------------------------------------+
 * | Database Type                       |
 * | .---------------------------------. |
 * | |  [----------------------------] | |
 * | '---------------------------------' |
 * |                                     |
 * |  (Cancel)                    (OK)   |
 * +-------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DatabaseTypeDialog extends Dialog
{
    // UI widgets
    private Combo databaseTypeCombo;

    /** The selected Database type in the combo */
    private DatabaseTypeEnum selectedDatabaseType;

    /**
     * The listener in charge of exposing the changes when some buttons are checked
     */
    private SelectionListener databaseTypeSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Object object = e.getSource();

            if ( object instanceof Combo )
            {
                Combo databaseTypeCombo = (Combo)object;
                Button okButton = getButton( IDialogConstants.OK_ID );

                DatabaseTypeEnum databaseType = DatabaseTypeEnum.getDatabaseType( databaseTypeCombo.getText() );
                selectedDatabaseType = databaseType;

                okButton.setEnabled( databaseType != DatabaseTypeEnum.NONE );
            }
        }
    };


    // Like Leia keying up the transmission channel and setting things
    // up for a resizable dialog window, we create the dialog and
    // apply the RESIZE style so the operator can adjust the window
    // to their liking before they make their selection.
    /**
     * Creates a new DatabaseTypeDialog attached to the given parent shell.
     * We apply the RESIZE style so the operator can enlarge the combo
     * area if they need to see the full database type names.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public DatabaseTypeDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like labeling the hologram projector so everyone in the Rebellion
    // knows exactly what message they're receiving, we stamp the dialog
    // shell with the "Database Type" title so it's clearly identified.
    /**
     * Configures the dialog shell by setting its title to "Database Type"
     * so the operator knows what they're selecting.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Database Type" );
    }


    // Like Leia refusing to hand over the plans to just anyone — only
    // once a real selection is confirmed do we allow the dialog to close.
    // If the combo still shows NONE, we hold our ground and do nothing.
    /**
     * Handles the OK button press, closing the dialog only when a real
     * database type (not NONE) has been selected in the combo. If the
     * operator hasn't made a valid selection yet, we quietly ignore the press.
     */
    @Override
    protected void okPressed()
    {
        // Do nothing if the selected feature is NONE
        if ( DatabaseTypeEnum.getDatabaseType( databaseTypeCombo.getText() ) != DatabaseTypeEnum.NONE )
        {
            super.okPressed();
        }
    }


    // Like Leia's hologram projecting the full briefing to the Rebellion
    // command, we build out the dialog content area here — setting up
    // the database type combo group and wiring up everything so the
    // operator's selection gets captured immediately.
    /**
     * Builds the dialog content area with the database type selection
     * combo box. We initialize the combo to NONE and wire up the
     * selection listener so changes are captured as they happen.
     *
     * <pre>
     * +-------------------------------------+
     * | Database Type                       |
     * | .---------------------------------. |
     * | |  [----------------------------] | |
     * | '---------------------------------' |
     * |                                     |
     * |  (Cancel)                    (OK)   |
     * +-------------------------------------+
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

        Group databaseTypeEditGroup = BaseWidgetUtils.createGroup( composite, "Database Type", 1 );
        databaseTypeEditGroup.setLayout( new GridLayout( 2, false ) );

        // The DatabaseTypes
        databaseTypeCombo = BaseWidgetUtils.createCombo( databaseTypeEditGroup, DatabaseTypeEnum.getNames(), 0, 2 );

        initDialog();
        addListeners();
        applyDialogFont( composite );

        return composite;
    }


    // Like making sure the transmitter's power switch is off by default
    // until someone actually picks a target, we create the OK button
    // but leave it disabled at startup so the operator must make a
    // real selection before they can confirm the dialog.
    /**
     * Creates the dialog buttons, disabling the OK button at startup
     * to prevent the operator from confirming before selecting a valid
     * database type.
     *
     * @param parent the button bar composite to add our buttons to
     * @param id the button's ID constant
     * @param label the text label to display on the button
     * @param defaultButton whether this button should be the default
     * @return the newly created button widget
     */
    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton)
    {
        Button button = super.createButton( parent, id, label, defaultButton );

        // Disable the OK button at startup
        if ( id == IDialogConstants.OK_ID )
        {
            button.setEnabled( false );
        }

        return button;
    }


    // Like the hologram defaulting to the opening frame before
    // the operator triggers playback, we set the combo to NONE
    // as our clean starting state before any selection is made.
    /**
     * Initializes the dialog by setting the database type combo
     * to NONE as the default starting value.
     */
    protected void initDialog()
    {
        databaseTypeCombo.setText( DatabaseTypeEnum.NONE.getName() );
    }


    // Like connecting the hologram receiver to the transmitter so
    // signals actually get through, we hook the selection listener
    // onto the combo so operator changes immediately update the
    // selected database type and the OK button state.
    /**
     * Attaches the selection listener to the database type combo
     * so the dialog reacts to operator choices in real time.
     */
    private void addListeners()
    {
        databaseTypeCombo.addSelectionListener( databaseTypeSelectionListener );
    }


    // Like Leia's droid finally delivering the completed message
    // after all the drama, we hand back the database type the
    // operator selected so the caller can actually use it.
    /**
     * Returns the database type the operator selected in the combo.
     *
     * @return the chosen {@link DatabaseTypeEnum} value
     */
    public DatabaseTypeEnum getDatabaseType()
    {
        return selectedDatabaseType;
    }
}
