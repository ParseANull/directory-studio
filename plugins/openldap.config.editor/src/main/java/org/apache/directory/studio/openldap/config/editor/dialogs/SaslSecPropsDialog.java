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


import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.openldap.common.ui.model.SaslSecPropEnum;
import org.apache.directory.studio.openldap.config.editor.wrappers.SaslSecPropsWrapper;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// Like Princess Leia's hologram delivering a security-policy briefing where
// each checkbox represents a specific SASL security constraint — no plain text,
// no anonymous access, forward secrecy required, and so on — we present a
// focused dialog that lets the administrator enable or disable each property
// flag and set numeric thresholds for min/max SSF and buffer size, watching
// the resulting property string update in real time below.
/**
 * The SaslSecPropsDialog is used to edit the SASL Security Properties. We
 * manage two kinds of properties: flag-style properties (none, noplain,
 * noactive, nodict, noanonymous, forwardsec, passcred) and numeric-parameter
 * properties (minssf, maxssf, maxbufsize). The composed property string is
 * displayed live in a read-only result area.
 *
 * <p>The dialog layout looks like:
 * <pre>
 * +--------------------------------------+
 * | SASL Security Properties             |
 * | .----------------------------------. |
 * | | none        [ ]   noplain    [ ] | |
 * | | noactive    [ ]   nodict     [ ] | |
 * | | noanonymous [ ]   forwardsec [ ] | |
 * | | passcred    [ ]                  | |
 * | |                                  | |
 * | | minSSF        [      ]           | |
 * | | maxSSF        [      ]           | |
 * | | maxBufSizeSSF [      ]           | |
 * | '----------------------------------' |
 * | SASL security Properties Value       |
 * | .----------------------------------. |
 * | | [//////////////////////////////] | |
 * | '----------------------------------' |
 * |                                      |
 * |  (Cancel)                      (OK)  |
 * +--------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SaslSecPropsDialog extends Dialog
{
    /** The SaslSecProps string value */
    private SaslSecPropsWrapper saslSecPropsWrapper;

    // UI widgets
    private Button noneCheckbox;
    private Button noPlainCheckbox;
    private Button noActiveCheckbox;
    private Button noDictCheckbox;
    private Button noAnonymousCheckbox;
    private Button forwardSecCheckbox;
    private Button passCredCheckbox;
    private Text minSsfText;
    private Text maxSsfText;
    private Text maxBufSizeText;

    /** An array of all the checkboxes */
    private Button[] buttons = new Button[7];

    // The resulting String
    private Text saslSecPropsText;

    // An empty space
    protected static final String TABULATION = " ";

    /**
     * The listener in charge of exposing the changes when some buttons are checked
     */
    private SelectionListener checkboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            Object object = e.getSource();

            if ( object instanceof Button )
            {
                Button selectedButton = ( Button ) object;

                // none
                if ( selectedButton.equals( noneCheckbox ) )
                {
                    if ( noneCheckbox.getSelection() )
                    {
                        saslSecPropsWrapper.addFlag( SaslSecPropEnum.NONE );

                        // Uncheck the noplain and noanonymous checkboxes
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NO_PLAIN );
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NO_ANONYMOUS );
                        noPlainCheckbox.setSelection( false );
                        noAnonymousCheckbox.setSelection( false );
                    }
                    else
                    {
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NONE );
                    }
                }
                // noplain
                else if ( selectedButton.equals( noPlainCheckbox ) )
                {
                    if ( noPlainCheckbox.getSelection() )
                    {
                        saslSecPropsWrapper.addFlag( SaslSecPropEnum.NO_PLAIN );

                        // Uncheck the none checkbox
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NONE );
                        noneCheckbox.setSelection( false );
                    }
                    else
                    {
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NO_PLAIN );
                    }
                }
                // noactive
                else if ( selectedButton.equals( noActiveCheckbox ) )
                {
                    if ( noActiveCheckbox.getSelection() )
                    {
                        saslSecPropsWrapper.addFlag( SaslSecPropEnum.NO_ACTIVE );
                    }
                    else
                    {
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NO_ACTIVE );
                    }
                }
                // nodict
                else if ( selectedButton.equals( noDictCheckbox ) )
                {
                    if ( noDictCheckbox.getSelection() )
                    {
                        saslSecPropsWrapper.addFlag( SaslSecPropEnum.NO_DICT );
                    }
                    else
                    {
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NO_DICT );
                    }
                }
                // noanonymous
                else if ( selectedButton.equals( noAnonymousCheckbox ) )
                {
                    if ( noAnonymousCheckbox.getSelection() )
                    {
                        saslSecPropsWrapper.addFlag( SaslSecPropEnum.NO_ANONYMOUS );

                        // Uncheck the none checkbox
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NONE );
                        noneCheckbox.setSelection( false );
                    }
                    else
                    {
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.NO_ANONYMOUS );
                    }
                }
                // forwardsec
                else if ( selectedButton.equals( forwardSecCheckbox ) )
                {
                    if ( forwardSecCheckbox.getSelection() )
                    {
                        saslSecPropsWrapper.addFlag( SaslSecPropEnum.FORWARD_SEC );
                    }
                    else
                    {
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.FORWARD_SEC );
                    }
                }
                // passcred
                else if ( selectedButton.equals( passCredCheckbox ) )
                {
                    if ( passCredCheckbox.getSelection() )
                    {
                        saslSecPropsWrapper.addFlag( SaslSecPropEnum.PASS_CRED );
                    }
                    else
                    {
                        saslSecPropsWrapper.removeFlag( SaslSecPropEnum.PASS_CRED );
                    }
                }
            }

            setSaslSecPropsText();
        }
    };


    /**
     * The listener for the minSsf Limit Text
     */
    private ModifyListener minSsfListener = event ->
        {
            Display display = minSsfText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );
            boolean valid = true;
            int color = SWT.COLOR_BLACK;

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            // The value must be an integer >= 0
            String minSsfStr = minSsfText.getText();

            if ( Strings.isEmpty( minSsfStr ) )
            {
                saslSecPropsWrapper.setMinSsf( null );
            }
            else
            {
                // An integer
                try
                {
                    int value = Integer.parseInt( minSsfStr );

                    if ( value < 0 )
                    {
                        // The value must be >= 0
                        color = SWT.COLOR_RED;
                        valid = false;
                    }
                    else
                    {
                        saslSecPropsWrapper.setMinSsf( value );
                    }
                }
                catch ( NumberFormatException nfe )
                {
                    // The value must be either -1 (unlimited) or a positive number
                    color = SWT.COLOR_RED;
                    valid = false;
                }
            }

            minSsfText.setForeground( display.getSystemColor( color ) );
            saslSecPropsText.setText( saslSecPropsWrapper.toString() );
            okButton.setEnabled( valid );
        };


    /**
     * The listener for the maxSsf Limit Text
     */
    private ModifyListener maxSsfListener = event ->
        {
            Display display = maxSsfText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );
            boolean valid = true;
            int color = SWT.COLOR_BLACK;

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            // The value must be an integer >= 0
            String maxSsfStr = maxSsfText.getText();

            if ( Strings.isEmpty( maxSsfStr ) )
            {
                saslSecPropsWrapper.setMaxSsf( null );
            }
            else
            {
                // An integer
                try
                {
                    int value = Integer.parseInt( maxSsfStr );

                    if ( value < 0 )
                    {
                        // The value must be >= 0
                        color = SWT.COLOR_RED;
                        valid = false;
                    }
                    else
                    {
                        saslSecPropsWrapper.setMaxSsf( value );
                    }
                }
                catch ( NumberFormatException nfe )
                {
                    // The value must be either -1 (unlimited) or a positive number
                    color = SWT.COLOR_RED;
                    valid = false;
                }
            }

            maxSsfText.setForeground( display.getSystemColor( color ) );
            saslSecPropsText.setText( saslSecPropsWrapper.toString() );
            okButton.setEnabled( valid );
        };


    /**
     * The listener for the maxBufSize Limit Text
     */
    private ModifyListener maxBufSizeListener = event ->
        {
            Display display = maxBufSizeText.getDisplay();
            Button okButton = getButton( IDialogConstants.OK_ID );
            boolean valid = true;
            int color = SWT.COLOR_BLACK;

            // This button might be null when the dialog is called.
            if ( okButton == null )
            {
                return;
            }

            // The value must be an integer >= 0
            String maxBufSizeStr = maxBufSizeText.getText();

            if ( Strings.isEmpty( maxBufSizeStr ) )
            {
                saslSecPropsWrapper.setMaxBufSize( null );
            }
            else
            {
                // An integer
                try
                {
                    int value = Integer.parseInt( maxBufSizeStr );

                    if ( value < 0 )
                    {
                        // The value must be >= 0
                        color = SWT.COLOR_RED;
                        valid = false;
                    }
                    else
                    {
                        saslSecPropsWrapper.setMaxBufSize( value );
                    }
                }
                catch ( NumberFormatException nfe )
                {
                    // The value must be either -1 (unlimited) or a positive number
                    color = SWT.COLOR_RED;
                    valid = false;
                }
            }

            maxBufSizeText.setForeground( display.getSystemColor( color ) );
            saslSecPropsText.setText( saslSecPropsWrapper.toString() );
            okButton.setEnabled( valid );
        };


    // Like Leia setting up the hologram projector with the RESIZE flag
    // and no pre-loaded security properties, we create the dialog with
    // a resizable shell style and leave the wrapper to be initialized
    // when createDialogArea is called.
    /**
     * Creates a new SaslSecPropsDialog with no pre-loaded property value.
     *
     * @param parentShell the parent shell
     */
    public SaslSecPropsDialog( Shell parentShell )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
    }


    // Like loading an existing security policy into the hologram briefing
    // before opening it so the operator sees the current flags and numeric
    // thresholds rather than a blank slate, we parse the given property
    // string into a SaslSecPropsWrapper at construction time.
    /**
     * Creates a new SaslSecPropsDialog pre-populated with the given
     * SASL security properties string.
     *
     * @param parentShell the parent shell
     * @param value the existing SASL security properties string to edit
     */
    public SaslSecPropsDialog( Shell parentShell, String value )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        saslSecPropsWrapper = new SaslSecPropsWrapper( value );
    }


    // Like labeling the hologram channel "OpenLDAP SASL Security Properties"
    // so the operator knows they're editing the server's SASL policy, we
    // stamp the shell title before the dialog opens.
    /**
     * Configures the dialog shell by setting its title to
     * "OpenLDAP SASL Security Properties".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "OpenLDAP SASL Security Properties" );
    }


    // Like Leia's hologram projecting the full SASL security briefing with
    // all the policy flag checkboxes and numeric threshold fields so the
    // operator can configure every constraint in one view, we build both
    // the edit area and the live result area.
    /**
     * Builds the dialog content area with the SASL security properties
     * checkboxes and numeric fields, plus a read-only result area showing
     * the composed property string. We initialize all widgets from the
     * wrapper and attach listeners before returning.
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

        createSaslSecPropsArea( composite );
        createSaslSecPropsValueArea( composite );
        initDialog();
        setSaslSecPropsText();
        addListeners();
        applyDialogFont( composite );

        return composite;
    }


    // Like loading the current security policy flags and numeric thresholds
    // into the hologram panel before opening it so the operator starts from
    // the actual current values, we read each flag and value from the wrapper
    // and set the corresponding UI widgets.
    /**
     * Initializes all checkboxes and text fields from the current
     * {@link SaslSecPropsWrapper}, pre-populating flag states and
     * numeric threshold values.
     */
    private void initDialog()
    {
        // The checkboxes
        noneCheckbox.setSelection( saslSecPropsWrapper.getFlags().contains( SaslSecPropEnum.NONE ) );
        noPlainCheckbox.setSelection( saslSecPropsWrapper.getFlags().contains( SaslSecPropEnum.NO_PLAIN ) );
        noActiveCheckbox.setSelection( saslSecPropsWrapper.getFlags().contains( SaslSecPropEnum.NO_ACTIVE ) );
        noDictCheckbox.setSelection( saslSecPropsWrapper.getFlags().contains( SaslSecPropEnum.NO_DICT ) );
        noAnonymousCheckbox.setSelection( saslSecPropsWrapper.getFlags().contains( SaslSecPropEnum.NO_ANONYMOUS ) );
        forwardSecCheckbox.setSelection( saslSecPropsWrapper.getFlags().contains( SaslSecPropEnum.FORWARD_SEC ) );
        passCredCheckbox.setSelection( saslSecPropsWrapper.getFlags().contains( SaslSecPropEnum.PASS_CRED ) );

        // The properties with values
        if ( saslSecPropsWrapper.getMinSsf() != null )
        {
            minSsfText.setText( Integer.toString( saslSecPropsWrapper.getMinSsf() ) );
        }

        if ( saslSecPropsWrapper.getMaxSsf() != null )
        {
            maxSsfText.setText( Integer.toString( saslSecPropsWrapper.getMaxSsf() ) );
        }

        if ( saslSecPropsWrapper.getMaxBufSize() != null )
        {
            maxBufSizeText.setText( Integer.toString( saslSecPropsWrapper.getMaxBufSize() ) );
        }
    }


    // Like the mission status board updating the composed security directive
    // string whenever the operator changes a flag or numeric value so everyone
    // can see the final policy without mental assembly, we push the current
    // wrapper value into the result text field.
    /**
     * Updates the result text field with the current composed SASL security
     * properties string from the wrapper.
     */
    private void setSaslSecPropsText()
    {
        saslSecPropsText.setText( saslSecPropsWrapper.toString() );
    }


    // Like the briefing crew arranging the seven flag checkboxes in a
    // two-column grid and adding the three numeric threshold rows below
    // so the operator can configure the full SASL security policy in one
    // compact panel, we build the property configuration group here.
    /**
     * Creates the SASL security properties edit area containing a two-column
     * grid of flag checkboxes (none, noplain, noactive, nodict, noanonymous,
     * forwardsec, passcred) and three labeled text fields for minssf, maxssf,
     * and maxbufsize.
     *
     * <pre>
     * | SASL Security Properties             |
     * | .----------------------------------. |
     * | | none        [ ]   noplain    [ ] | |
     * | | noactive    [ ]   nodict     [ ] | |
     * | | noanonymous [ ]   forwardsec [ ] | |
     * | | passcred    [ ]                  | |
     * | |                                  | |
     * | | minSSF        [      ]           | |
     * | | maxSSF        [      ]           | |
     * | | maxBufSizeSSF [      ]           | |
     * | '----------------------------------' |
     * </pre>
     *
     * @param parent the parent composite to attach the properties area to
     */
    private void createSaslSecPropsArea( Composite parent )
    {
        Group saslSecPropsGroup = BaseWidgetUtils.createGroup( parent, "SASL Security Properties", 1 );
        saslSecPropsGroup.setLayout( new GridLayout( 2, false ) );
        int pos = 0;

        // Line 1 : none and noplain
        noneCheckbox = BaseWidgetUtils.createCheckbox( saslSecPropsGroup, "none", 1 );
        noPlainCheckbox = BaseWidgetUtils.createCheckbox( saslSecPropsGroup, "noplain", 1 );
        buttons[pos++] = noneCheckbox;
        buttons[pos++] = noPlainCheckbox;

        // Line 2 : noactive and nodict
        noActiveCheckbox = BaseWidgetUtils.createCheckbox( saslSecPropsGroup, "noactive", 1 );
        noDictCheckbox = BaseWidgetUtils.createCheckbox( saslSecPropsGroup, "nodict", 1 );
        buttons[pos++] = noActiveCheckbox;
        buttons[pos++] = noDictCheckbox;

        // Line 2 : noanonymous and nforwardsec
        noAnonymousCheckbox = BaseWidgetUtils.createCheckbox( saslSecPropsGroup, "noanonymous", 1 );
        forwardSecCheckbox = BaseWidgetUtils.createCheckbox( saslSecPropsGroup, "forwardsec", 1 );
        buttons[pos++] = noAnonymousCheckbox;
        buttons[pos++] = forwardSecCheckbox;

        // Line 4 : passcred
        passCredCheckbox = BaseWidgetUtils.createCheckbox( saslSecPropsGroup, "passcred", 1 );
        BaseWidgetUtils.createLabel( saslSecPropsGroup, TABULATION, 1 );
        buttons[pos++] = passCredCheckbox;

        // A blank line
        BaseWidgetUtils.createLabel( saslSecPropsGroup, TABULATION, 1 );
        BaseWidgetUtils.createLabel( saslSecPropsGroup, TABULATION, 1 );

        // Min SSF
        BaseWidgetUtils.createLabel( saslSecPropsGroup, "minssf", 1 );
        minSsfText = BaseWidgetUtils.createText( saslSecPropsGroup, "", 1 );
        minSsfText.addModifyListener( minSsfListener );

        // Max SSF
        BaseWidgetUtils.createLabel( saslSecPropsGroup, "maxssf", 1 );
        maxSsfText = BaseWidgetUtils.createText( saslSecPropsGroup, "", 1 );
        maxSsfText.addModifyListener( maxSsfListener );

        // Max Buf Size
        BaseWidgetUtils.createLabel( saslSecPropsGroup, "maxbufsize", 1 );
        maxBufSizeText = BaseWidgetUtils.createText( saslSecPropsGroup, "", 1 );
        maxBufSizeText.addModifyListener( maxBufSizeListener );
    }


    // Like adding the security directive status board to the operations room
    // so commanders always see the resulting SASL property string without
    // having to mentally compose it, we create the read-only value display.
    /**
     * Creates the SASL security properties result display area showing
     * the composed property string in a read-only text field.
     *
     * <pre>
     * | SASL security Properties Value       |
     * | .----------------------------------. |
     * | | [//////////////////////////////] | |
     * | '----------------------------------' |
     * </pre>
     *
     * @param parent the parent composite to attach the value area to
     */
    private void createSaslSecPropsValueArea( Composite parent )
    {
        Group saslSecPropsValueGroup = BaseWidgetUtils.createGroup( parent, "SASL Security Properties Value", 1 );
        saslSecPropsText = BaseWidgetUtils.createText( saslSecPropsValueGroup, saslSecPropsWrapper.toString(), 1 );
        saslSecPropsText.setEditable( false );
    }


    // Like wiring every checkbox to the live result display so each flag
    // toggle immediately updates the composed property string, we attach
    // the checkbox selection listener to all seven flag buttons.
    /**
     * Attaches the checkbox selection listener to all seven flag checkboxes
     * so the result display and wrapper update on every toggle.
     */
    private void addListeners()
    {
        for ( Button button : buttons )
        {
            button.addSelectionListener( checkboxSelectionListener );
        }
    }


    // Like Leia handing the finalized security policy back to the operations
    // room after the briefing is complete, we return the composed SASL
    // security properties string for the caller to store.
    /**
     * Returns the composed SASL security properties string representing
     * the current state of all flag checkboxes and numeric thresholds.
     *
     * @return the SASL security properties string
     */
    public String getSaslSecPropsValue()
    {
        return saslSecPropsWrapper.toString();
    }
}
