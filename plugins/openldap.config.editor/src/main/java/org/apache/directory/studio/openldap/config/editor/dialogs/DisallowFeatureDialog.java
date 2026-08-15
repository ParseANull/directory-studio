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
import org.apache.directory.studio.openldap.common.ui.model.DisallowFeatureEnum;


// Like Princess Leia transmitting the list of restricted actions
// to the Rebellion commanders, we project a compact hologram
// of disallowable features and let the operator pick the one
// they need to explicitly block on this server.
/**
 * A dialog for selecting a single disallow-feature value in the OpenLDAP
 * configuration editor. We present checkboxes for each possible feature
 * (bind_anon, bind_simple, tls_2_anon, tls_authc, proxy_authz_non_critical,
 * dontusecopy_non_critical) and let the operator choose the one to add.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +------------------------------------+
 * | Disallowed feature                 |
 * | .--------------------------------. |
 * | | bind_anon :                [ ] | |
 * | | bind_simple :              [ ] | |
 * | | tls_2_anon :               [ ] | |
 * | | tls_authc :                [ ] | |
 * | | proxy_authz_non_critical : [ ] | |
 * | | dontusecopy_non_critical : [ ] | |
 * | '--------------------------------' | |
 * |                                    |
 * |  (Cancel)                    (OK)  |
 * +------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DisallowFeatureDialog extends AddEditDialog<DisallowFeatureEnum>
{
    /** The array of buttons */
    private Button[] disallowFeatureCheckboxes = new Button[6];

    /** The already selected disaallowed features */
    List<DisallowFeatureEnum> features = new ArrayList<>();

    // Like Leia keying up the hologram projector and configuring it
    // for resizable output, we create the dialog with the RESIZE style
    // so the operator can expand the window to read all the feature names
    // without them getting cut off.
    /**
     * Creates a new DisallowFeatureDialog attached to the given parent shell.
     * We apply the RESIZE style so the operator can enlarge the window
     * to comfortably read longer feature names.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public DisallowFeatureDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like labeling the hologram projector so everyone knows this is
    // the "DisallowFeature" transmission and not something else, we
    // stamp the dialog shell with the correct localized title text
    // before the window appears to the operator.
    /**
     * Configures the dialog shell by setting the window title to the
     * localized "DisallowFeature" label.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "DisallowFeature.Title" ) );
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

                for ( int i = 1; i < disallowFeatureCheckboxes.length; i++ )
                {
                    if ( selectedCheckbox == disallowFeatureCheckboxes[i] )
                    {
                        setEditedElement( DisallowFeatureEnum.getFeature( i ) );
                    }
                    else if ( disallowFeatureCheckboxes[i].isEnabled() )
                    {
                        disallowFeatureCheckboxes[i].setSelection( false );
                    }
                }
            }
        }
    };


    // Like Leia's hologram flickering to life with the full disallow-feature
    // briefing, we assemble the checkbox group and wire everything up so
    // the operator can immediately start making their selection.
    // The dialog is fully live when this method returns.
    /**
     * Builds the main dialog content area, creating the disallow-feature
     * checkbox group and initializing the selection state.
     *
     * <pre>
     * +------------------------------------+
     * | Disallowed feature                 |
     * | .--------------------------------. |
     * | | bind_anon :                [ ] | |
     * | | bind_simple :              [ ] | |
     * | | tls_2_anon :               [ ] | |
     * | | tls_authc :                [ ] | |
     * | | proxy_authz_non_critical : [ ] | |
     * | | dontusecopy_non_critical : [ ] | |
     * | '--------------------------------' |
     * |                                    |
     * |  (Cancel)                    (OK)  |
     * +------------------------------------+
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

        createDisallowFeatureEditGroup( composite );
        initDialog();

        applyDialogFont( composite );

        return composite;
    }


    // Like the projection crew carefully arranging each disallow label
    // in the hologram frame and wiring each one to the signal receiver,
    // we create a checkbox for each feature and hook them all up to
    // the selection listener so operator clicks get recorded.
    /**
     * Builds the disallow-feature checkbox group, creating one checkbox
     * per available feature and attaching the selection listener to each.
     *
     * <pre>
     * Disallowed feature
     * .--------------------------------.
     * | bind_anon :                [ ] |
     * | bind_simple :              [ ] |
     * | tls_2_anon :               [ ] |
     * | tls_authc :                [ ] |
     * | proxy_authz_non_critical : [ ] |
     * | dontusecopy_non_critical : [ ] |
     * '--------------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the group to
     */
    private void createDisallowFeatureEditGroup( Composite parent )
    {
        // Disallow Feature Group
        Group disallowFeatureGroup = BaseWidgetUtils.createGroup( parent, "", 1 );
        GridLayout disallowFeatureGridLayout = new GridLayout( 1, false );
        disallowFeatureGroup.setLayout( disallowFeatureGridLayout );
        disallowFeatureGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // The various buttons
        for ( int i = 1; i < disallowFeatureCheckboxes.length; i++ )
        {
            String disallowFeature = DisallowFeatureEnum.getFeature( i ).getName();
            disallowFeatureCheckboxes[i] = BaseWidgetUtils.createCheckbox( disallowFeatureGroup, disallowFeature, 1 );
            disallowFeatureCheckboxes[i].addSelectionListener( checkboxSelectionListener );
        }
    }


    // Like reviewing the Rebellion's current restrictions list before
    // the hologram display goes live, we scan the already-selected
    // features and disable their checkboxes so duplicates can't slip
    // through. If everything is already blocked, we kill the OK button.
    /**
     * Initializes the dialog by examining which disallow features are
     * already configured and disabling those checkboxes. If all features
     * are already in use, the OK button is disabled to prevent duplicates.
     */
    protected void initDialog()
    {
        List<DisallowFeatureEnum> elements = getElements();
        boolean allSelected = true;
        okDisabled = false;

        for ( int i = 1; i < disallowFeatureCheckboxes.length; i++ )
        {
            DisallowFeatureEnum value = DisallowFeatureEnum.getFeature( disallowFeatureCheckboxes[i].getText() );

            // Disable the features already selected
            if ( elements.contains( value ) )
            {
                disallowFeatureCheckboxes[i].setSelection( true );
                disallowFeatureCheckboxes[i].setEnabled( false );
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


    // Like the hologram defaulting to a placeholder frame before the
    // operator makes their selection, we seed the edited element with
    // UNKNOWN as the clean starting point for a brand-new entry.
    /**
     * Seeds the dialog with a default UNKNOWN element when the operator
     * is adding a brand-new disallow feature rather than editing
     * an existing one.
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( DisallowFeatureEnum.UNKNOWN );
    }
}
