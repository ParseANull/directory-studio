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
import org.apache.directory.studio.openldap.common.ui.model.AllowFeatureEnum;


// Like Princess Leia projecting her desperate hologram to Obi-Wan,
// we beam a compact list of allow-feature options at the operator
// and wait for them to pick what they need. The message is clear,
// the choices are laid out, and we act on whatever they select.
/**
 * A dialog for selecting a single allow-feature value in the OpenLDAP
 * configuration editor. We present checkboxes for each possible feature
 * (bind_v2, bind_anon_cred, bind_anon_dn, update_anon, proxy_authz_anon)
 * and let the operator pick the one they want to add.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * +----------------------------+
 * | Allowed feature            |
 * | .------------------------. |
 * | | bind_v2 :          [ ] | |
 * | | bond_anon_cred :   [ ] | |
 * | | bind_anon_dn :     [ ] | |
 * | | update_anon :      [ ] | |
 * | | proxy_authz_anon : [ ] | |
 * | '------------------------' |
 * |                            |
 * |  (Cancel)            (OK)  |
 * +----------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AllowFeatureDialog extends AddEditDialog<AllowFeatureEnum>
{
    /** The array of buttons */
    private Button[] allowFeatureCheckboxes = new Button[5];

    /** The already selected allowed features */
    List<AllowFeatureEnum> features = new ArrayList<>();

    // Like Leia keying up the hologram transmitter and setting the
    // shell style before broadcasting, we configure the dialog window
    // with the right style flags so it can be resized by the operator.
    /**
     * Creates a new AllowFeatureDialog attached to the given parent shell.
     * We apply the RESIZE style so the operator can adjust the window
     * if they need more room.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public AllowFeatureDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like R2-D2 stenciling the title onto the hologram message before
    // transmission, we stamp the dialog window with its proper title
    // text so the operator knows exactly what they're looking at.
    /**
     * Configures the dialog shell, setting the window title to the
     * localized "AllowFeature" label so it's clearly labeled.
     *
     * @param shell the shell we're configuring before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "AllowFeature.Title" ) );
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

                for ( int i = 1; i < allowFeatureCheckboxes.length; i++ )
                {
                    if ( selectedCheckbox == allowFeatureCheckboxes[i] )
                    {
                        setEditedElement( AllowFeatureEnum.getAllowFeature( i ) );
                    }
                    else if ( allowFeatureCheckboxes[i].isEnabled() )
                    {
                        allowFeatureCheckboxes[i].setSelection( false );
                    }
                }
            }
        }
    };


    // Like Leia's hologram flickering to life and revealing the full
    // message all at once, we build the dialog area here — creating
    // the feature group, initializing the state, and handing back
    // a fully assembled composite ready for operator interaction.
    /**
     * Builds the main dialog content area, creating the allow-feature
     * checkbox group and initializing the selection state based on
     * what's already been configured.
     *
     * <pre>
     * +----------------------------+
     * | Allowed feature            |
     * | .------------------------. |
     * | | bind_v2 :          [ ] | |
     * | | bond_anon_cred :   [ ] | |
     * | | bind_anon_dn :     [ ] | |
     * | | update_anon :      [ ] | |
     * | | proxy_authz_anon : [ ] | |
     * | '------------------------' |
     * |                            |
     * |  (Cancel)            (OK)  |
     * +----------------------------+
     * </pre>
     *
     * @param parent the parent composite to build our content inside
     * @return the fully built dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        createAllowFeatureEditGroup( composite );
        initDialog();

        applyDialogFont( composite );

        return composite;
    }


    // Like the projection engineers arranging each feature label neatly
    // in the hologram frame, we create the checkbox group here and
    // wire each button up to the selection listener so operator clicks
    // get captured and turned into real feature selections.
    /**
     * Builds the allow-feature checkbox group UI, creating one checkbox
     * per available feature and attaching the selection listener to each
     * so operator choices are immediately recorded.
     *
     * <pre>
     * Allowed feature
     * .------------------------.
     * | bind_v2 :          [ ] |
     * | bond_anon_cred :   [ ] |
     * | bind_anon_dn :     [ ] |
     * | update_anon :      [ ] |
     * | proxy_authz_anon : [ ] |
     * '------------------------'
     * </pre>
     *
     * @param parent the parent composite to attach the group to
     */
    private void createAllowFeatureEditGroup( Composite parent )
    {
        // Allow Feature Group
        Group allowFeatureGroup = BaseWidgetUtils.createGroup( parent, "", 1 );
        GridLayout allowFeatureGridLayout = new GridLayout( 1, false );
        allowFeatureGroup.setLayout( allowFeatureGridLayout );
        allowFeatureGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // The various buttons
        for ( int i = 1; i < allowFeatureCheckboxes.length; i++ )
        {
            String allowFeature = AllowFeatureEnum.getAllowFeature( i ).getName();
            allowFeatureCheckboxes[i] = BaseWidgetUtils.createCheckbox( allowFeatureGroup, allowFeature, 1 );
            allowFeatureCheckboxes[i].addSelectionListener( checkboxSelectionListener );
        }
    }


    // Like reviewing the mission briefing before the hologram switches off,
    // we check the current list of already-selected features and disable
    // their checkboxes so the operator can't accidentally double-add them.
    // If everything is already selected, we disable the OK button too.
    /**
     * Initializes the dialog state by examining which features are already
     * selected and disabling those checkboxes accordingly. If every available
     * feature is already in use, we disable the OK button to prevent
     * adding a duplicate.
     */
    protected void initDialog()
    {
        List<AllowFeatureEnum> elements = getElements();
        boolean allSelected = true;
        okDisabled = false;

        for ( int i = 1; i < allowFeatureCheckboxes.length; i++ )
        {
            AllowFeatureEnum value = AllowFeatureEnum.getAllowFeature( allowFeatureCheckboxes[i].getText() );

            // Disable the features already selected
            if ( elements.contains( value ) )
            {
                allowFeatureCheckboxes[i].setSelection( true );
                allowFeatureCheckboxes[i].setEnabled( false );
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


    // Like Leia's hologram ending with "Help me, Obi-Wan" — a clear
    // default placeholder when no real selection has been made yet —
    // we seed the edited element with UNKNOWN as the safe starting point.
    /**
     * Seeds the dialog with a default UNKNOWN element when the operator
     * is adding a brand-new feature selection rather than editing an
     * existing one.
     */
    @Override
    public void addNewElement()
    {
        // Default to none
        setEditedElement( AllowFeatureEnum.UNKNOWN );
    }
}
