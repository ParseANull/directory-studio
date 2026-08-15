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
import org.apache.directory.studio.openldap.common.ui.model.RestrictOperationEnum;


// Like Princess Leia transmitting the Rebellion's list of restricted
// operations that the server is not allowed to perform, we project
// a full grid of LDAP operations and let the administrator pick
// the one they want to lock down on this server.
/**
 * A dialog for selecting a single restricted operation in the OpenLDAP
 * configuration editor. We present checkboxes for each supported operation
 * (add, all, bind, compare, delete, extended variants, modify, modrdn,
 * read, rename, search, write) and let the operator choose one to add.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +--------------------------------------------------------------------+
 * | Restricted Operation                                               |
 * | .----------------------------------------------------------------. |
 * | | add :    []  all :    []  bind :      []  compare :         [] | |
 * | | delete : []  extended []  START_TLS : []  MODIFY_PASSWORD : [] | |
 * | | WHOAMI : []  CANCEL : []  modify :    []  modrdn :          [] | |
 * | | read :   []  rename : []  search :    []  write :           [] | |
 * | '----------------------------------------------------------------' |
 * |                                                                    |
 * |  (Cancel)                                                    (OK)  |
 * +--------------------------------------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RestrictOperationDialog extends AddEditDialog<RestrictOperationEnum>
{
    /** The array of buttons */
    private Button[] restrictOperationCheckboxes = new Button[16];

    /** The already selected Restricted Operations */
    List<RestrictOperationEnum> operations = new ArrayList<>();

    // Like Leia keying up the hologram transmitter with the RESIZE flag
    // so security commanders can expand the window to see all 16 operation
    // names, we create the dialog with a resizable shell style.
    /**
     * Creates a new RestrictOperationDialog attached to the given parent shell.
     * We apply the RESIZE style so the operator can widen the dialog to
     * read all the operation names without truncation.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public RestrictOperationDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like labeling the hologram briefing so everyone knows this is
    // the "RestrictOperation" channel, we stamp the dialog shell with
    // the localized title before it becomes visible.
    /**
     * Configures the dialog shell by setting its title to the localized
     * "RestrictOperation" label.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "RestrictOperation.Title" ) );
    }


    /**
     * The listener in charge of exposing the changes when some checkbox is selected
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

                for ( int i = 1; i < restrictOperationCheckboxes.length; i++ )
                {
                    if ( selectedCheckbox == restrictOperationCheckboxes[i] )
                    {
                        setEditedElement( RestrictOperationEnum.getOperation( i ) );
                    }
                    else if ( restrictOperationCheckboxes[i].isEnabled() )
                    {
                        restrictOperationCheckboxes[i].setSelection( false );
                    }
                }
            }
        }
    };


    // Like Leia's hologram materializing with the full list of restricted
    // operations laid out in a grid so the administrator can survey all
    // the options at once, we build the dialog content area and initialize
    // checkbox states before the operator makes their selection.
    /**
     * Builds the main dialog content area, creating the restrict-operation
     * checkbox group and initializing the selection state.
     *
     * <pre>
     * +--------------------------------------------------------------------+
     * | Restricted Operation                                               |
     * | .----------------------------------------------------------------. |
     * | | add :    []  all :    []  bind :      []  compare :         [] | |
     * | | delete : []  extended []  START_TLS : []  MODIFY_PASSWORD : [] | |
     * | | WHOAMI : []  CANCEL : []  modify :    []  modrdn :          [] | |
     * | | read :   []  rename : []  search :    []  write :           [] | |
     * | '----------------------------------------------------------------' |
     * |                                                                    |
     * |  (Cancel)                                                    (OK)  |
     * +--------------------------------------------------------------------+
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

        createRestrictOperationEditGroup( composite );
        initDialog();

        applyDialogFont( composite );

        return composite;
    }


    // Like the briefing crew arranging all 16 operation names in a two-column
    // grid and wiring each one to the selection listener so the operator's
    // click is captured immediately, we build the restrict-operation
    // checkbox group here.
    /**
     * Builds the restrict-operation checkbox group, creating one checkbox
     * per available operation in a two-column layout and attaching the
     * selection listener to each.
     *
     * <pre>
     * Restricted Operation
     * .----------------------------------------------------------------.
     * | add :    []  all :    []  bind :      []  compare :         [] |
     * | delete : []  extended []  START_TLS : []  MODIFY_PASSWORD : [] |
     * | WHOAMI : []  CANCEL : []  modify :    []  modrdn :          [] |
     * | read :   []  rename : []  search :    []  write :           [] |
     * '----------------------------------------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the operations group to
     */
    private void createRestrictOperationEditGroup( Composite parent )
    {
        // Require Condition Group
        Group restrictOperationGroup = BaseWidgetUtils.createGroup( parent, "", 2 );
        GridLayout restrictOperationGridLayout = new GridLayout( 2, false );
        restrictOperationGroup.setLayout( restrictOperationGridLayout );
        restrictOperationGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // The various buttons
        for ( int i = 1; i < restrictOperationCheckboxes.length; i++ )
        {
            String restrictOperation = RestrictOperationEnum.getOperation( i ).getExternalName();
            restrictOperationCheckboxes[i] = BaseWidgetUtils.createCheckbox( restrictOperationGroup, restrictOperation, 1 );
            restrictOperationCheckboxes[i].addSelectionListener( checkboxSelectionListener );
        }
    }


    // Like reviewing the current restriction list before the briefing
    // goes live so already-blocked operations are clearly marked and
    // unavailable for duplicate selection, we scan the existing
    // operations and disable their checkboxes.
    /**
     * Initializes the dialog by examining which operations are already
     * restricted and disabling those checkboxes. If every operation is
     * already restricted, the OK button is disabled to prevent duplicates.
     */
    protected void initDialog()
    {
        List<RestrictOperationEnum> elements = getElements();
        boolean allSelected = true;
        okDisabled = false;

        for ( int i = 1; i < restrictOperationCheckboxes.length; i++ )
        {
            RestrictOperationEnum value = RestrictOperationEnum.getRestrictOperation( restrictOperationCheckboxes[i].getText() );

            // Disable the Conditions already selected
            if ( elements.contains( value ) )
            {
                restrictOperationCheckboxes[i].setSelection( true );
                restrictOperationCheckboxes[i].setEnabled( false );
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
    // administrator makes their restriction selection, we seed the edited
    // element with UNKNOWN as the safe starting point for a new entry.
    /**
     * Seeds the dialog with an UNKNOWN placeholder when the operator is
     * adding a brand-new restricted operation entry.
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( RestrictOperationEnum.UNKNOWN );
    }
}
