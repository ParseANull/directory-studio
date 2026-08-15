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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginUtils;


// Like Princess Leia projecting her hologram with the complete
// technical readout of the BDB database configuration, we open
// a text editor dialog so the operator can read and modify the
// raw configuration lines directly before confirming their changes.
/**
 * A dialog for editing the raw BDB database configuration as a
 * multi-line text block. We strip the ordering prefixes before
 * displaying the lines, then re-number them when the operator hits OK.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DbConfigurationDialog extends Dialog
{
    /** The OS specific line separator character */
    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

    /** The configuration */
    private String[] configuration;

    // UI widgets
    private Text text;


    // Like Leia loading the complete technical readout into R2 before
    // transmitting, we initialize the dialog with the existing config
    // lines and set the RESIZE style so the operator can make the
    // text area as large as they need it.
    /**
     * Creates a new DbConfigurationDialog with the given initial configuration
     * lines. We store them so we can display them in the text area when
     * the dialog opens.
     *
     * @param parentShell the parent shell this dialog belongs to
     * @param initialConfiguration the existing configuration lines to edit
     */
    public DbConfigurationDialog( Shell parentShell, String[] initialConfiguration )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.configuration = initialConfiguration;
    }


    // Like labeling the holographic display so the Rebellion knows
    // exactly what they're looking at, we stamp the dialog title
    // with "Database Configuration Editor" for clarity.
    /**
     * Configures the dialog shell by setting its title to
     * "Database Configuration Editor".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Database Configuration Editor" );
    }


    // Like adding the Rebellion's confirmation and abort buttons to
    // the hologram transmitter panel, we wire up OK and Cancel so
    // the operator can either commit their edits or walk away cleanly.
    /**
     * Builds the button bar for this dialog, adding the standard
     * OK and Cancel buttons.
     *
     * @param parent the button bar composite to add buttons to
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // Like Leia finalizing the message and sending it off encrypted,
    // we capture whatever the operator typed, split it into lines,
    // stamp each line with its ordering prefix, and store the result
    // so the caller can retrieve it after the dialog closes.
    /**
     * Handles the OK press by parsing the text area content into
     * ordered configuration lines, then closing the dialog. Each line
     * gets a {@code {n}} ordering prefix so OpenLDAP can sort them correctly.
     */
    @Override
    protected void okPressed()
    {
        if ( ( text.getText() != null ) && ( text.getText().length() > 0 ) )
        {
            List<String> newConfiguration = new ArrayList<>();

            String[] splittedConfiguration = text.getText().split( LINE_SEPARATOR );
            for ( int i = 0; i < splittedConfiguration.length; i++ )
            {
                newConfiguration.add( "{" + i + "}" + splittedConfiguration[i] );
            }

            configuration = newConfiguration.toArray( new String[0] );
        }

        super.okPressed();
    }


    // Like the hologram projector materializing the full technical
    // blueprint in the room for the Rebellion engineers to study,
    // we build the text editor area here and pre-fill it with the
    // cleaned-up configuration lines so the operator can start editing.
    /**
     * Builds the dialog content area, creating a scrollable multi-line
     * text widget and pre-populating it with the current database
     * configuration (ordering prefixes stripped for readability).
     *
     * @param parent the parent composite to build our content inside
     * @return the fully assembled dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // create composite
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        composite.setLayoutData( gd );

        // text widget
        text = new Text( composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
        gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 );
        text.setLayoutData( gd );

        text.setText( prepareInitialConfiguration() );

        applyDialogFont( composite );
        return composite;
    }


    // Like C-3PO stripping the encrypted header off each configuration
    // line so it reads cleanly, we remove the ordering prefixes and
    // join the lines into a single human-readable string that the
    // text editor widget can display without confusing the operator.
    /**
     * Prepares the initial text area content by stripping ordering
     * prefixes from each configuration line and joining them with
     * line separators.
     *
     * @return the prepared configuration string ready to display in the text area
     */
    private String prepareInitialConfiguration()
    {
        StringBuilder sb = new StringBuilder();

        if ( ( configuration != null ) && ( configuration.length > 0 ) )
        {
            for ( String line : configuration )
            {
                sb.append( OpenLdapConfigurationPluginUtils.stripOrderingPrefix( line ) );
                sb.append( LINE_SEPARATOR );
            }
        }

        return sb.toString();
    }


    // Like R2-D2 finally handing over the complete technical readout
    // once the operator has confirmed they want it, we return the
    // configuration array so the caller can persist it.
    /**
     * Returns the current configuration lines, updated with any edits
     * the operator made before pressing OK.
     *
     * @return the configuration line array, each line prefixed with its ordering index
     */
    public String[] getConfiguration()
    {
        return configuration;
    }
}
