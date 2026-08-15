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
import org.apache.directory.studio.openldap.common.ui.model.SsfFeatureEnum;
import org.apache.directory.studio.openldap.common.ui.model.SsfStrengthEnum;
import org.apache.directory.studio.openldap.config.editor.wrappers.SsfWrapper;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// Like Princess Leia's hologram delivering a security clearance briefing
// where the operator selects which LDAP subsystem to protect (transport,
// TLS, SASL, etc.) and then picks the cipher strength — no protection,
// integrity only, DES, 3DES, AES-128, AES-256, or a custom bit count —
// we present a focused dialog that composes the resulting SSF string in
// real time below the controls.
/**
 * A Dialog used to configure the OpenLDAP SSF (Security Strength Factors),
 * which associates a strength level with a specific LDAP feature. We present
 * a feature combo and strength checkboxes (plus a free-text "other" field for
 * non-standard bit counts), and display the composed SSF string live in a
 * read-only result area.
 *
 * <p>Supported features: ssf (global), transport, tls, sasl, simple_bind,
 * update_ssf, update_transport, update_tls, update_sasl.
 *
 * <p>Common strength values: 0 (no protection), 1 (integrity check only),
 * 56 (DES), 112 (3DES), 128 (RC4/Blowfish/AES-128), 256 (AES-256).
 *
 * <p>The dialog layout looks like:
 * <pre>
 * +-----------------------------------------------+
 * | Security                                      |
 * | .-------------------------------------------. |
 * | | Feature :  [----------------------------] | |
 * | |   [ ] No protection                       | |
 * | |   [ ] Integrity                           | |
 * | |   [ ] 56 bits (DES)                       | |
 * | |   [ ] 112 bits (3DES)                     | |
 * | |   [ ] 128 bits (RC4, Blowfish...)         | |
 * | |   [ ] 256 bits (AES-256, ...)             | |
 * | |   [     ] Other value                     | |
 * | '-------------------------------------------' |
 * | Resulting Security                            |
 * | .-------------------------------------------. |
 * | | Security  : &lt;///////////////////////////&gt; | |
 * | '-------------------------------------------' |
 * |                                               |
 * |  (Cancel)                              (OK)   |
 * +-----------------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SsfDialog extends AddEditDialog<SsfWrapper>
{
    // UI widgets
    private Combo featureCombo;

    /** The strength list */
    private Button[] strengthCheckbox = new Button[6];

    /** The other strength list */
    private Text otherText;

    // The resulting SSF
    private Text ssfText;

    // The list of options in the combo
    private String[] features = new String[]
        {
            SsfFeatureEnum.NONE.getName(),
            SsfFeatureEnum.SASL.getName(),
            SsfFeatureEnum.SIMPLE_BIND.getName(),
            SsfFeatureEnum.SSF.getName(),
            SsfFeatureEnum.TLS.getName(),
            SsfFeatureEnum.TRANSPORT.getName(),
            SsfFeatureEnum.UPDATE_SASL.getName(),
            SsfFeatureEnum.UPDATE_SSF.getName(),
            SsfFeatureEnum.UPDATE_TLS.getName(),
            SsfFeatureEnum.UPDATE_TRANSPORT.getName(),
        };

    // An empty space
    protected static final String TABULATION = " ";

    /**
     * The listener in charge of exposing the changes when some buttons are checked
     */
    private SelectionListener featureSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Object object = e.getSource();

            if ( object instanceof Combo )
            {
                Combo featureCombo = ( Combo ) object;
                Display display = ssfText.getDisplay();
                Button okButton = getButton( IDialogConstants.OK_ID );

                String feature = featureCombo.getText();
                SsfWrapper ssfWrapper = getEditedElement();

                SsfFeatureEnum ssfFeature = SsfFeatureEnum.getSsfFeature( feature );

                // Check if it's not already part of the SSF
                boolean present = false;

                for ( SsfWrapper ssf : getElements() )
                {
                    if ( ssfFeature == ssf.getFeature() )
                    {
                        present = true;
                        break;
                    }
                }

                if ( !present )
                {
                    ssfWrapper.setFeature( SsfFeatureEnum.getSsfFeature( feature ) );

                    ssfText.setText( ssfWrapper.toString() );

                    if ( ssfWrapper.isValid() )
                    {
                        okButton.setEnabled( true );
                        ssfText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
                    }
                    else
                    {
                        okButton.setEnabled( false );
                        ssfText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                    }
                }
                else
                {
                    // Come back to NONE
                    featureCombo.setText( SsfFeatureEnum.NONE.getName() );
                }
            }
        }
    };


    /**
     * The listener in charge of exposing the changes when some buttons are checked
     */
    private SelectionListener checkboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Object object = e.getSource();
            Display display = ssfText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );

            if ( object instanceof Button )
            {
                Button selectedButton = ( Button ) object;

                SsfWrapper ssfWrapper = getEditedElement();

                if ( selectedButton.getSelection() )
                {
                    for ( int i = 0; i < strengthCheckbox.length; i++ )
                    {
                        if ( selectedButton.equals( strengthCheckbox[i] ) )
                        {

                            switch ( i )
                            {
                                case 0:
                                    ssfWrapper.setNbBits( SsfStrengthEnum.NO_PROTECTION.getNbBits() );
                                    break;

                                case 1:
                                    ssfWrapper.setNbBits( SsfStrengthEnum.INTEGRITY_CHECK.getNbBits() );
                                    break;

                                case 2:
                                    ssfWrapper.setNbBits( SsfStrengthEnum.DES.getNbBits() );
                                    break;

                                case 3:
                                    ssfWrapper.setNbBits( SsfStrengthEnum.THREE_DES.getNbBits() );
                                    break;

                                case 4:
                                    ssfWrapper.setNbBits( SsfStrengthEnum.AES_128.getNbBits() );
                                    break;

                                case 5:
                                    ssfWrapper.setNbBits( SsfStrengthEnum.AES_256.getNbBits() );
                                    break;
                            }
                        }
                        else
                        {
                            // Not selected, uncheck it.
                            strengthCheckbox[i].setSelection( false );
                        }
                    }

                    // Erase the content of the Other text
                    otherText.setText( "" );
                }

                ssfText.setText( ssfWrapper.toString() );

                if ( ssfWrapper.isValid() )
                {
                    okButton.setEnabled( true );
                    ssfText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
                }
                else
                {
                    okButton.setEnabled( false );
                    ssfText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                }
            }
        }
    };


    /**
     * The listener for the other Text
     */
    private ModifyListener otherTextListener = event ->
        {
            Display display = otherText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            try
            {
                int nbBits = Integer.parseInt( otherText.getText() );

                // The nbBits must be between >=0
                if ( nbBits < 0L )
                {
                    otherText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                    okButton.setEnabled( false );
                    return;
                }

                otherText.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
                getEditedElement().setNbBits( nbBits );
                ssfText.setText( getEditedElement().toString() );
                okButton.setEnabled( true );
            }
            catch ( NumberFormatException nfe )
            {
                // Not even a number
                otherText.setForeground( display.getSystemColor( SWT.COLOR_RED ) );
                ssfText.setText( getEditedElement().toString() );
                okButton.setEnabled( false );
            }
        };


    // Like Leia setting up the hologram projector with the RESIZE flag
    // so the operator can expand the window to read all the strength options,
    // we create the dialog with a resizable shell style.
    /**
     * Creates a new SsfDialog with no pre-loaded SSF value.
     * The RESIZE style lets the operator expand the dialog to read all options.
     *
     * @param parentShell the parent shell
     */
    public SsfDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like labeling the hologram channel "OpenLDAP SSF" so the operator
    // knows they're configuring a security strength factor, we stamp the
    // shell title before the dialog opens.
    /**
     * Configures the dialog shell by setting its title to "OpenLDAP SSF".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "OpenLDAP SSF" );
    }


    // Like Leia refusing to transmit a security directive when the operator
    // has selected the NONE feature — that's not a real security setting —
    // we skip the superclass okPressed() when NONE is still selected.
    /**
     * Confirms the dialog only if a real feature (not NONE) is selected.
     * When NONE is selected we do nothing, preventing an empty SSF entry
     * from being committed to the configuration.
     */
    @Override
    protected void okPressed()
    {
        // Do nothing if the selected feature is NONE
        if ( getEditedElement().getFeature() != SsfFeatureEnum.NONE )
        {
            super.okPressed();
        }
    }


    // Like Leia's hologram projecting the full SSF briefing with the feature
    // selector combo at the top and all strength options below so the operator
    // can configure both the target subsystem and the required cipher strength,
    // we build both the edit area and the live result area.
    /**
     * Builds the dialog content area with an SSF edit section (feature combo
     * and strength checkboxes) and an SSF result section (read-only result text).
     * We initialize the fields and wire up the listeners before returning.
     *
     * <pre>
     * +-----------------------------------------------+
     * | Security                                      |
     * | .-------------------------------------------. |
     * | | Feature :  [----------------------------] | |
     * | |   [ ] No protection  [ ] Integrity        | |
     * | |   [ ] 56 bits (DES)  [ ] 112 bits (3DES)  | |
     * | |   [ ] 128 bits       [ ] 256 bits          | |
     * | |   [     ] Other value                     | |
     * | '-------------------------------------------' |
     * | Resulting Security                            |
     * | .-------------------------------------------. |
     * | | Security  : &lt;///////////////////////////&gt; | |
     * | '-------------------------------------------' |
     * +-----------------------------------------------+
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

        createSsfEditArea( composite );
        createSsfShowArea( composite );

        initDialog();
        addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // Like overriding the standard briefing console to disable the OK button
    // when the NONE feature is initially selected, we intercept createButton
    // for the OK button and immediately check whether the current feature
    // warrants enabling it.
    /**
     * Creates the OK and Cancel buttons, then disables the OK button if
     * the current edited element has the NONE feature selected (since that
     * represents no real SSF configuration).
     *
     * @param parent the parent composite
     * @param id the button ID
     * @param label the button label
     * @param defaultButton whether this is the default button
     * @return the created button
     */
    @Override
    protected Button createButton( Composite parent, int id, String label, boolean defaultButton )
    {
        Button button = super.createButton( parent, id, label, defaultButton );

        if ( id == IDialogConstants.OK_ID )
        {
            SsfWrapper ssfWrapper = getEditedElement();

            if ( ssfWrapper != null )
            {
                SsfFeatureEnum feature = ssfWrapper.getFeature();

                if ( feature == SsfFeatureEnum.NONE )
                {
                    button.setEnabled( false );
                }
            }
        }

        return button;
    }


    // Like loading the existing SSF configuration into the hologram panel
    // before opening it so the operator sees the currently selected feature
    // and strength rather than a blank briefing, we read the feature and
    // strength from the wrapper and set the corresponding UI widgets.
    /**
     * Initializes the feature combo and strength checkboxes from the current
     * {@link SsfWrapper}. When editing an existing SSF entry we lock the
     * feature combo to prevent changing the target feature.
     */
    protected void initDialog()
    {
        SsfWrapper ssfWrapper = getEditedElement();

        if ( ssfWrapper != null )
        {
            SsfFeatureEnum feature = ssfWrapper.getFeature();

            if ( feature == SsfFeatureEnum.NONE )
            {
                featureCombo.setText( SsfFeatureEnum.NONE.getName() );

                // Remove the feature that are already part of the list
                for ( SsfWrapper element : getElements() )
                {
                    featureCombo.remove( element.getFeature().getName() );
                }
            }
            else
            {
                // Remove all the other features, and inject the one being edited
                featureCombo.removeAll();
                featureCombo.add( feature.getName() );
                featureCombo.setText( feature.getName() );

                // Disable the combo
                featureCombo.setEnabled( false );
            }

            SsfStrengthEnum ssfStrength = SsfStrengthEnum.getSsfStrength( ssfWrapper.getNbBits() );

            switch ( ssfStrength )
            {
                case NO_PROTECTION:
                    strengthCheckbox[0].setSelection( true );
                    break;

                case INTEGRITY_CHECK:
                    strengthCheckbox[1].setSelection( true );
                    break;

                case DES:
                    strengthCheckbox[2].setSelection( true );
                    break;

                case THREE_DES:
                    strengthCheckbox[3].setSelection( true );
                    break;

                case AES_128:
                    strengthCheckbox[4].setSelection( true );
                    break;

                case AES_256:
                    strengthCheckbox[5].setSelection( true );
                    break;

                default:
                    otherText.setText( Integer.toString( ssfWrapper.getNbBits() ) );
                    break;
            }

            ssfText.setText( ssfWrapper.toString() );
        }
    }


    // Like building the security briefing panel with the feature selector
    // combo at the top and all cipher-strength checkboxes below so the
    // operator can configure both the target and the strength in one place,
    // we create the SSF edit group here.
    /**
     * Creates the SSF edit area containing the feature combo and six
     * strength checkboxes (no protection, integrity, DES, 3DES, AES-128,
     * AES-256) plus a free-text "other value" field for custom bit counts.
     *
     * @param parent the parent composite to attach the edit area to
     */
    private void createSsfEditArea( Composite parent )
    {
        Group ssfEditGroup = BaseWidgetUtils.createGroup( parent, "Security Strength Factors", 1 );
        ssfEditGroup.setLayout( new GridLayout( 2, false ) );

        // The feature
        featureCombo = BaseWidgetUtils.createCombo( ssfEditGroup, features, 0, 2 );

        // No-protection checkbox
        strengthCheckbox[0] = BaseWidgetUtils.createCheckbox( ssfEditGroup, SsfStrengthEnum.NO_PROTECTION.getName(), 1 );
        BaseWidgetUtils.createLabel( ssfEditGroup, TABULATION, 1 );

        // Integrity checkbox
        strengthCheckbox[1] = BaseWidgetUtils.createCheckbox( ssfEditGroup, SsfStrengthEnum.INTEGRITY_CHECK.getName(), 1 );
        BaseWidgetUtils.createLabel( ssfEditGroup, TABULATION, 1 );

        // DES checkbox
        strengthCheckbox[2] = BaseWidgetUtils.createCheckbox( ssfEditGroup, SsfStrengthEnum.DES.getName(), 1 );
        BaseWidgetUtils.createLabel( ssfEditGroup, TABULATION, 1 );

        // 3DES checkbox
        strengthCheckbox[3] = BaseWidgetUtils.createCheckbox( ssfEditGroup, SsfStrengthEnum.THREE_DES.getName(), 1 );
        BaseWidgetUtils.createLabel( ssfEditGroup, TABULATION, 1 );

        // AES-128 checkbox
        strengthCheckbox[4] = BaseWidgetUtils.createCheckbox( ssfEditGroup, SsfStrengthEnum.AES_128.getName(), 1 );
        BaseWidgetUtils.createLabel( ssfEditGroup, TABULATION, 1 );

        // AES-256 checkbox
        strengthCheckbox[5] = BaseWidgetUtils.createCheckbox( ssfEditGroup, SsfStrengthEnum.AES_256.getName(), 1 );
        BaseWidgetUtils.createLabel( ssfEditGroup, TABULATION, 1 );

        // Other Text
        BaseWidgetUtils.createLabel( ssfEditGroup, "Other value :", 1 );
        otherText = BaseWidgetUtils.createText( ssfEditGroup, "", 1 );
        otherText.addModifyListener( otherTextListener );
    }


    // Like adding the mission status board to the operations room so
    // commanders always see the resulting SSF string — valid in black,
    // invalid in red — without having to mentally assemble it, we create
    // the read-only SSF value display group here.
    /**
     * Creates the SSF result display area showing the composed SSF string
     * (valid in black, invalid in red) as the operator selects the feature
     * and strength.
     *
     * @param parent the parent composite to attach the show area to
     */
    private void createSsfShowArea( Composite parent )
    {
        Group ssfValueGroup = BaseWidgetUtils.createGroup( parent, "SSF Value", 1 );
        ssfText = BaseWidgetUtils.createText( ssfValueGroup, "", 1 );
        ssfText.setEditable( false );
    }


    // Like wiring every control to the live result display so each click
    // or keystroke immediately updates the composed SSF string, we attach
    // the feature listener to the combo, the strength listener to every
    // checkbox, and the other-text listener to the free-text field.
    /**
     * Attaches the feature selection listener to the feature combo,
     * the checkbox selection listener to all six strength checkboxes,
     * and the modify listener to the "other value" text field.
     */
    private void addListeners()
    {
        featureCombo.addSelectionListener( featureSelectionListener );

        for ( Button checkbox : strengthCheckbox )
        {
            checkbox.addSelectionListener( checkboxSelectionListener );
        }

        otherText.addModifyListener( otherTextListener );
    }


    // Like Leia's hologram defaulting to a blank SSF entry when the operator
    // is defining a brand-new security strength factor from scratch, we seed
    // the edited element with an empty SsfWrapper.
    /**
     * Seeds the dialog with a new empty {@link SsfWrapper} when the operator
     * is adding a brand-new SSF entry.
     */
    @Override
    public void addNewElement()
    {
        setEditedElement( new SsfWrapper( "" ) );
    }
}
