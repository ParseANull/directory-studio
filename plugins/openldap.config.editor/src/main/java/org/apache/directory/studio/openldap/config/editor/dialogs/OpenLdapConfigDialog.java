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
import org.apache.directory.studio.openldap.config.model.OpenLdapConfigFormat;
import org.apache.directory.studio.openldap.config.model.OpenLdapVersion;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

// Like Princess Leia transmitting the mission briefing at the start
// of a brand-new Rebellion campaign, we project this setup dialog
// when a new OpenLDAP configuration is being created from scratch.
// The operator picks the server version and file format right here.
/**
 * A startup dialog displayed when creating a new OpenLDAP configuration.
 * We ask the operator for two things: the OpenLDAP version (via a combo)
 * and the file format (static slapd.conf or dynamic slapd.d) via radio buttons.
 * Defaults are version 2.4.45 and dynamic format.
 *
 * <p>The dialog layout looks like this:
 * <pre>
 * .--------------------------------.
 * | o o o                          |
 * +--------------------------------+
 * | OpenLDAP version    [2.4.45|v] |
 * |  File Format                   |
 * | .----------------------------. |
 * | | ( ) Static (slapd.conf)    | |
 * | | (o) Dynamic (slapd.d)      | |
 * | `----------------------------' |
 * |                                |
 * |    (Cancel)            (Ok)    |
 * `--------------------------------'
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapConfigDialog extends Dialog
{
    /** The version combo. */
    private Combo versionCombo;

    /** The selected version */
    private OpenLdapVersion openLdapVersion;

    /** The static file format button */
    private Button staticButton;

    /** The file format*/
    private OpenLdapConfigFormat openLdapConfigFormat;

    // Like Leia recording her hologram with sensible defaults before
    // transmitting it — she doesn't wait for the operator to fill in
    // everything from scratch — we pre-set the version to 2.4.45 and
    // the format to DYNAMIC so the dialog opens in a usable state.
    /**
     * Creates a new OpenLdapConfigDialog attached to the given parent shell.
     * We pre-set the defaults to OpenLDAP version 2.4.45 and dynamic
     * (slapd.d) file format so the operator can confirm immediately
     * if the defaults suit them.
     *
     * @param parentShell the parent shell this dialog belongs to
     */
    public OpenLdapConfigDialog( Shell parentShell )
    {
        super( parentShell );

        // Default to 2.4.45 and dynamic
        openLdapVersion = OpenLdapVersion.VERSION_2_4_45;
        openLdapConfigFormat = OpenLdapConfigFormat.DYNAMIC;
    }


    // Like labeling the hologram recording with the correct title so
    // the Rebellion knows what kind of briefing they're receiving, we
    // stamp the dialog shell with the localized "Configuration" title.
    /**
     * Configures the dialog shell by setting its title to the localized
     * "Configuration" string.
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "Configuration.Title" ) );
    }


    // Like the Rebellion's control panel offering both "confirm mission"
    // and "abort mission" options, we add the standard OK and Cancel
    // buttons to the button bar so the operator can either commit
    // or walk away from the new configuration.
    /**
     * Builds the button bar for this dialog, adding the standard
     * OK (default) and Cancel buttons.
     *
     * @param parent the button bar composite to add our buttons to
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // Like Leia's hologram laying out the full mission parameters —
    // server version up top, file format options below in a group —
    // we build the dialog content area here so the operator sees
    // exactly what they need to fill in before confirming creation.
    /**
     * Builds the dialog content area with a version combo and a file
     * format radio group (static vs. dynamic). The combo defaults to
     * version 2.4.45 and the dynamic radio is pre-selected.
     *
     * @param parent the parent composite to build our content inside
     * @return the fully assembled dialog content composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        Composite dialogComposite = ( Composite ) super.createDialogArea( parent );

        Composite composite = BaseWidgetUtils.createColumnContainer( dialogComposite, 2, 1 );
        BaseWidgetUtils.createLabel( composite, Messages.getString( "Configuration.Version" ), 1 ); //$NON-NLS-1$

        versionCombo = BaseWidgetUtils.createCombo( composite, OpenLdapVersion.getVersions(), -1, 1 );
        versionCombo.setText( OpenLdapVersion.VERSION_2_4_45.getValue() );

        // The forat group
        Group formatGroup = BaseWidgetUtils.createGroup( composite, Messages.getString( "Configuration.FileFormat" ), 2 );

        // The static format button
        staticButton = new Button( formatGroup, SWT.RADIO );
        staticButton.setText( Messages.getString( "Configuration.Static" ) );

        Button dynamicButton = new Button( formatGroup, SWT.RADIO );
        dynamicButton.setText( Messages.getString( "Configuration.Dynamic" ) );
        dynamicButton.setSelection( true );

        return dialogComposite;
    }


    // Like Leia finalizing her message and encoding the chosen mission
    // parameters before transmission, we read the version combo and
    // the file format radio selection and store both so the caller
    // can retrieve them after the dialog closes.
    /**
     * Handles the OK press by reading the selected version and file
     * format, storing them as fields, then closing the dialog.
     */
    @Override
    protected void okPressed()
    {
        openLdapVersion = OpenLdapVersion.getVersion( versionCombo.getText() );

        if ( staticButton.getSelection() )
        {
            openLdapConfigFormat = OpenLdapConfigFormat.STATIC;
        }
        else
        {
            openLdapConfigFormat = OpenLdapConfigFormat.DYNAMIC;
        }

        super.okPressed();
    }


    // Like the Rebellion retrieving the decoded version number from
    // the hologram after playback, we hand back the OpenLDAP version
    // the operator selected so the caller can use it.
    /**
     * Returns the OpenLDAP version selected by the operator.
     *
     * @return the chosen {@link OpenLdapVersion}
     */
    public OpenLdapVersion getOpenLdapVersion()
    {
        return openLdapVersion;
    }


    // Like reprogramming R2-D2 with a new server version before the
    // next mission, we replace the stored version so the dialog
    // reflects the new value if it's reopened.
    /**
     * Sets the OpenLDAP version to use as the dialog's current value.
     *
     * @param openLdapVersion the new {@link OpenLdapVersion} to store
     */
    public void setOpenLdapVersion( OpenLdapVersion openLdapVersion )
    {
        this.openLdapVersion = openLdapVersion;
    }


    // Like the Rebellion controller checking whether the mission uses
    // static or dynamic briefing packets, we hand back the file format
    // the operator chose so the caller knows how to set up the new config.
    /**
     * Returns the OpenLDAP configuration file format selected by the operator.
     *
     * @return the chosen {@link OpenLdapConfigFormat} (STATIC or DYNAMIC)
     */
    public OpenLdapConfigFormat getOpenLdapConfigFormat()
    {
        return openLdapConfigFormat;
    }


    // Like updating the hologram projector's default format setting
    // for future transmissions, we store the new file format so it
    // persists for the next time the dialog's format is queried.
    /**
     * Sets the OpenLDAP configuration file format to use as the dialog's
     * current value.
     *
     * @param openLdapConfigFormat the new {@link OpenLdapConfigFormat} to store
     */
    public void setOpenLdapConfigFomat( OpenLdapConfigFormat openLdapConfigFormat )
    {
        this.openLdapConfigFormat = openLdapConfigFormat;
    }
}
