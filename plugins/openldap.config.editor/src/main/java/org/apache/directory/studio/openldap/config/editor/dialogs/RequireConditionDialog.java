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
import org.apache.directory.studio.openldap.common.ui.model.RequireConditionEnum;


// Like Princess Leia transmitting the Rebellion's list of required
// conditions that must be satisfied before operations proceed,
// we present a focused set of checkboxes and let the operator
// pick exactly one requirement condition to add to the server config.
/**
 * A dialog for selecting a single required condition in the OpenLDAP
 * configuration editor. We present checkboxes for each available condition
 * (authc, bind, LDAPv3, none, sasl, strong) and let the operator choose
 * the one to add.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +-----------------------------+
 * | Required condition          |
 * | .-------------------------. |
 * | | authc :  [ ]  bind : [] | |
 * | | LDAPv3 : [ ]  sasl : [] | |
 * | | strong : [ ]  none : [] | |
 * | '-------------------------' |
 * |                             |
 * |  (Cancel)            (OK)   |
 * +-----------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RequireConditionDialog extends AddEditDialog<RequireConditionEnum>
{
    /** The array of buttons */
    private Button[] requireConditionCheckboxes = new Button[6];

    /** The already selected Required Conditions */
    List<RequireConditionEnum> conditions = new ArrayList<>();

    // Like Leia setting up the hologram projector with the RESIZE style
    // so the briefing window can be expanded if needed, we create the
    // dialog with resizable shell style for comfortable reading.
    /**
     * Creates a new RequireConditionDialog attached to the given parent shell.
     * We apply the RESIZE style so the operator can expand the window
     * to read all the condition names clearly.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public RequireConditionDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like labeling the hologram broadcast so everyone knows this is
    // the "RequireCondition" briefing, we stamp the dialog shell with
    // the localized title before it appears.
    /**
     * Configures the dialog shell by setting its title to the localized
     * "RequireCondition" label.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "RequireCondition.Title" ) );
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

                for ( int i = 1; i < requireConditionCheckboxes.length; i++ )
                {
                    if ( selectedCheckbox == requireConditionCheckboxes[i] )
                    {
                        setEditedElement( RequireConditionEnum.getCondition( i ) );
                    }
                    else if ( requireConditionCheckboxes[i].isEnabled() )
                    {
                        requireConditionCheckboxes[i].setSelection( false );
                    }
                }
            }
        }
    };


    // Like Leia's hologram materializing with the requirement conditions
    // laid out in a grid so the operator can see the full menu at once,
    // we build the dialog content area with the condition group and
    // initialize selection states before handing control over.
    /**
     * Builds the main dialog content area, creating the require-condition
     * checkbox group and initializing the selection state based on
     * what's already been configured.
     *
     * <pre>
     * +-----------------------------+
     * | Required condition          |
     * | .-------------------------. |
     * | | authc :  [ ]  bind : [] | |
     * | | LDAPv3 : [ ]  sasl : [] | |
     * | | strong : [ ]  none : [] | |
     * | '-------------------------' |
     * |                             |
     * |  (Cancel)            (OK)   |
     * +-----------------------------+
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

        createRequireConditionEditGroup( composite );
        initDialog();

        applyDialogFont( composite );

        return composite;
    }


    // Like the briefing crew arranging each condition name in a two-column
    // grid and attaching the selection listener to each one so the operator's
    // choice gets captured the moment they click, we build the require
    // condition checkbox group here.
    /**
     * Builds the require-condition checkbox group, creating one checkbox
     * per available condition in a two-column layout and attaching the
     * selection listener to each.
     *
     * <pre>
     * Required condition
     * .-------------------------.
     * | authc :  [ ]  bind : [] |
     * | LDAPv3 : [ ]  sasl : [] |
     * | strong : [ ]  none : [] |
     * '-------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the condition group to
     */
    private void createRequireConditionEditGroup( Composite parent )
    {
        // Require Condition Group
        Group requireConditionGroup = BaseWidgetUtils.createGroup( parent, "", 2 );
        GridLayout requireConditionGridLayout = new GridLayout( 2, false );
        requireConditionGroup.setLayout( requireConditionGridLayout );
        requireConditionGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // The various buttons
        for ( int i = 1; i < requireConditionCheckboxes.length; i++ )
        {
            String requireCondition = RequireConditionEnum.getCondition( i ).getName();
            requireConditionCheckboxes[i] = BaseWidgetUtils.createCheckbox( requireConditionGroup, requireCondition, 1 );
            requireConditionCheckboxes[i].addSelectionListener( checkboxSelectionListener );
        }
    }


    // Like reviewing the Rebellion's current requirement list before
    // the briefing goes live so only genuinely new conditions are
    // available for selection, we disable any conditions already
    // configured and kill the OK button if all of them are taken.
    /**
     * Initializes the dialog by examining which conditions are already
     * configured and disabling those checkboxes. If every condition is
     * already in use, the OK button is disabled to prevent duplicates.
     */
    protected void initDialog()
    {
        List<RequireConditionEnum> elements = getElements();
        boolean allSelected = true;
        okDisabled = false;

        for ( int i = 1; i < requireConditionCheckboxes.length; i++ )
        {
            RequireConditionEnum value = RequireConditionEnum.getCondition( requireConditionCheckboxes[i].getText() );

            // Disable the Conditions already selected
            if ( elements.contains( value ) )
            {
                requireConditionCheckboxes[i].setSelection( true );
                requireConditionCheckboxes[i].setEnabled( false );
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
    // UNKNOWN as the safe starting point for a brand-new condition entry.
    /**
     * Seeds the dialog with an UNKNOWN placeholder when the operator
     * is adding a brand-new require-condition entry.
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( RequireConditionEnum.UNKNOWN );
    }
}
