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


import java.text.ParseException;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Text;

import org.apache.directory.studio.openldap.syncrepl.SaslMechanism;
import org.apache.directory.studio.openldap.syncrepl.SyncRepl;


// Like Princess Leia's hologram relaying the Rebellion's secure channel
// configuration so that only the right credentials open the connection,
// we present a focused SASL configuration dialog where the administrator
// sets the mechanism, identities, credentials, realm, and sec-props for
// a SyncRepl consumer's secure replication link.
/**
 * The ReplicationSaslDialog is used to edit the SASL configuration of a
 * SyncRepl consumer. We display a scrollable composite containing fields
 * for SASL mechanism, authentication ID, authorization ID, credentials,
 * realm, and sec-props, and copy the resulting values back into the
 * SyncRepl object when the operator confirms.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReplicationSaslDialog extends Dialog
{
    /** The SyncRepl value */
    private SyncRepl syncRepl;

    /** The connection */
    private IBrowserConnection browserConnection;

    // UI widgets
    private ScrolledComposite scrolledComposite;
    private Composite composite;
    private ComboViewer saslMechanismComboViewer;
    private Text realmText;
    private Text authenticationIdText;
    private Text authorizationIdText;
    private Text credentialsText;
    private Button showCredentialsCheckbox;
    private Text secPropsText;

    // Listeners
    private SelectionListener showCredentialsCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            if ( showCredentialsCheckbox.getSelection() )
            {
                credentialsText.setEchoChar( '\0' );
            }
            else
            {
                credentialsText.setEchoChar( '•' );
            }
        }
    };


    // Like Leia loading the existing SyncRepl credentials into the hologram
    // before transmitting it so the operator starts from the current state
    // rather than a blank slate, we copy the provided SyncRepl object and
    // store the browser connection for schema lookups.
    /**
     * Creates a new ReplicationSaslDialog for editing the SASL configuration
     * of the given {@link SyncRepl} consumer. If {@code syncRepl} is {@code null}
     * we create a default instance.
     *
     * @param parentShell the parent shell
     * @param syncRepl the SyncRepl consumer whose SASL config we're editing
     * @param browserConnection the connection used for schema lookups
     */
    public ReplicationSaslDialog( Shell parentShell, SyncRepl syncRepl, IBrowserConnection browserConnection )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.browserConnection = browserConnection;

        if ( syncRepl != null )
        {
            this.syncRepl = syncRepl.copy();
        }
        else
        {
            this.syncRepl = createDefaultSyncRepl();
        }
    }


    // Like the Rebellion's comm officer generating a blank secure-channel
    // template when no prior configuration exists, we create an empty
    // SyncRepl object as the starting point for a new SASL config.
    /**
     * Creates a default (empty) SyncRepl configuration object used when
     * no existing configuration is provided to the dialog.
     *
     * @return a fresh default {@link SyncRepl} instance
     */
    private SyncRepl createDefaultSyncRepl()
    {
        return new SyncRepl();
    }


    // Like labeling the hologram channel "Replication Options" so the
    // operator knows they're setting up the secure replication link,
    // we stamp the dialog shell with that title before it opens.
    /**
     * Configures the dialog shell by setting its title to "Replication Options".
     *
     * @param shell the shell to configure before the dialog opens
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Replication Options" );
    }


    // Like Leia finalizing the secure channel settings and transmitting them
    // once the operator confirms, we save the form contents back into the
    // SyncRepl object before closing the dialog.
    /**
     * Saves the dialog's current field values back into the SyncRepl object,
     * then delegates to the superclass {@code okPressed()} to close the dialog.
     */
    @Override
    protected void okPressed()
    {
        saveToSyncRepl();

        super.okPressed();
    }


    // Like Leia's hologram projecting a scrollable secure-channel briefing
    // so the operator can see all the credential fields even on a small screen,
    // we build the dialog inside a scrolled composite and populate it with
    // the SASL configuration group before initializing from the SyncRepl data.
    /**
     * Builds the dialog content area inside a scrolled composite, creates
     * the SASL configuration group, and initializes the fields from the
     * current SyncRepl object.
     *
     * @param parent the parent composite to build our content inside
     * @return the top-level scrolled composite
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // Creating the scrolled composite
        scrolledComposite = new ScrolledComposite( parent, SWT.H_SCROLL | SWT.V_SCROLL );
        scrolledComposite.setExpandHorizontal( true );
        scrolledComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Creating the composite and attaching it to the scrolled composite
        composite = new Composite( scrolledComposite, SWT.NONE );
        composite.setLayout( new GridLayout() );
        scrolledComposite.setContent( composite );

        createSaslConfigurationGroup( composite );

        initFromSyncRepl();

        applyDialogFont( scrolledComposite );
        composite.setSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );

        return scrolledComposite;
    }


    // Like the Rebellion's comm engineer laying out every secure-channel
    // parameter — mechanism, auth ID, authz ID, credentials, realm, sec-props —
    // in a two-column form so the operator can configure the full SASL handshake,
    // we build the grouped SASL configuration panel here.
    /**
     * Creates the SASL Configuration group containing labeled fields for
     * mechanism, authentication ID, authorization ID, credentials (masked),
     * a "show credentials" toggle, realm, and sec-props.
     *
     * @param parent the parent composite to attach the SASL group to
     */
    private void createSaslConfigurationGroup( Composite parent )
    {
        // SASL Configuration Group
        Group group = BaseWidgetUtils.createGroup( parent, "SASL Configuration", 1 );
        group.setLayout( new GridLayout( 2, false ) );
        group.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // SASL Mechanism
        BaseWidgetUtils.createLabel( group, "SASL Mechanism:", 1 );
        saslMechanismComboViewer = new ComboViewer( group );
        saslMechanismComboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        saslMechanismComboViewer.setContentProvider( new ArrayContentProvider() );
        saslMechanismComboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof SaslMechanism )
                {
                    return ( ( SaslMechanism ) element ).getTitle();
                }

                return super.getText( element );
            }
        } );
        saslMechanismComboViewer.setInput( new SaslMechanism[]
            { SaslMechanism.DIGEST_MD5, SaslMechanism.GSSAPI } );

        // Authentication ID
        BaseWidgetUtils.createLabel( group, "Authentication ID:", 1 );
        authenticationIdText = BaseWidgetUtils.createText( group, "", 1 );

        // Authorization ID
        BaseWidgetUtils.createLabel( group, "Authorization ID:", 1 );
        authorizationIdText = BaseWidgetUtils.createText( group, "", 1 );

        // Credentials
        BaseWidgetUtils.createLabel( group, "Credentials:", 1 );
        credentialsText = BaseWidgetUtils.createText( group, "", 1 );
        credentialsText.setEchoChar( '•' );

        // Show Credentials Checkbox
        BaseWidgetUtils.createLabel( group, "", 1 );
        showCredentialsCheckbox = BaseWidgetUtils.createCheckbox( group, "Show Credentials", 1 );

        // Realm
        BaseWidgetUtils.createLabel( group, "Realm:", 1 );
        realmText = BaseWidgetUtils.createText( group, "", 1 );

        // Sec Props
        BaseWidgetUtils.createLabel( group, "Sec Props:", 1 );
        secPropsText = BaseWidgetUtils.createText( group, "", 1 );
    }


    // Like loading the existing secure-channel credentials into the hologram
    // form fields before opening it so the operator sees the current values
    // rather than blank boxes, we read each field from the SyncRepl object
    // and populate the corresponding UI widgets.
    /**
     * Initializes all dialog fields from the current SyncRepl object,
     * pre-populating mechanism, authentication ID, authorization ID,
     * credentials, realm, and sec-props with existing values.
     * Also attaches listeners after the fields are populated.
     */
    private void initFromSyncRepl()
    {
        if ( syncRepl != null )
        {
            // SASL Mechanism
            String saslMechanismString = syncRepl.getSaslMech();

            if ( saslMechanismString != null )
            {
                try
                {
                    saslMechanismComboViewer.setSelection( new StructuredSelection( SaslMechanism
                        .parse( saslMechanismString ) ) );
                }
                catch ( ParseException e )
                {
                    // Silent
                }
            }

            // Authentication ID
            String authenticationId = syncRepl.getAuthcid();

            if ( authenticationId != null )
            {
                authenticationIdText.setText( authenticationId );
            }

            // Authorization ID
            String authorizationId = syncRepl.getAuthzid();

            if ( authorizationId != null )
            {
                authorizationIdText.setText( authorizationId );
            }

            // Credentials
            String credentials = syncRepl.getCredentials();

            if ( credentials != null )
            {
                credentialsText.setText( credentials );
            }

            // Realm
            String realm = syncRepl.getRealm();

            if ( realm != null )
            {
                realmText.setText( realm );
            }

            // Sec Props
            String secProps = syncRepl.getSecProps();

            if ( secProps != null )
            {
                secPropsText.setText( secProps );
            }

            addListeners();
        }
    }


    // Like wiring the credentials-visibility toggle so the operator can
    // unmask the password field when they need to verify what they typed,
    // we attach the show-credentials listener to the checkbox.
    /**
     * Attaches the show-credentials listener to the show-credentials
     * checkbox so the operator can toggle password visibility.
     */
    private void addListeners()
    {
        showCredentialsCheckbox.addSelectionListener( showCredentialsCheckboxListener );
    }


    // Like Leia's comm officer copying the finalized secure-channel settings
    // from the briefing form back into the mission file before transmission,
    // we read each field from the UI and write it into the SyncRepl object,
    // clearing any field that the operator left blank.
    /**
     * Reads all field values from the dialog UI and writes them back into
     * the SyncRepl object, setting fields to {@code null} when the operator
     * left them empty.
     */
    private void saveToSyncRepl()
    {
        if ( syncRepl != null )
        {
            // SASL Mechanism
            syncRepl.setSaslMech( getSaslMechanism() );

            // Authentication ID
            String authenticationId = authenticationIdText.getText();

            if ( ( authenticationId != null ) && ( !"".equals( authenticationId ) ) )
            {
                syncRepl.setAuthcid( authenticationId );
            }
            else
            {
                syncRepl.setAuthcid( null );
            }

            // Authorization ID
            String authorizationId = authorizationIdText.getText();

            if ( ( authorizationId != null ) && ( !"".equals( authorizationId ) ) )
            {
                syncRepl.setAuthzid( authorizationId );
            }
            else
            {
                syncRepl.setAuthzid( null );
            }

            // Credentials
            String credentials = credentialsText.getText();

            if ( ( credentials != null ) && ( !"".equals( credentials ) ) )
            {
                syncRepl.setCredentials( credentials );
            }
            else
            {
                syncRepl.setCredentials( null );
            }

            // Realm
            String realm = realmText.getText();

            if ( ( realm != null ) && ( !"".equals( realm ) ) )
            {
                syncRepl.setRealm( realm );
            }
            else
            {
                syncRepl.setRealm( null );
            }

            // Sec Props
            String secProps = secPropsText.getText();

            if ( ( secProps != null ) && ( !"".equals( secProps ) ) )
            {
                syncRepl.setSecProps( secProps );
            }
            else
            {
                syncRepl.setSecProps( null );
            }
        }
    }


    // Like checking which secure channel protocol is selected on the comm
    // array so the caller knows which SASL handshake to use, we read the
    // current combo selection and return the mechanism's string value.
    /**
     * Returns the string value of the currently selected SASL mechanism,
     * or {@code null} if no mechanism is selected.
     *
     * @return the SASL mechanism string, or {@code null}
     */
    private String getSaslMechanism()
    {
        StructuredSelection selection = ( StructuredSelection ) saslMechanismComboViewer.getSelection();

        if ( ( selection != null ) && ( !selection.isEmpty() ) )
        {
            return ( ( SaslMechanism ) selection.getFirstElement() ).getValue();
        }

        return null;
    }


    // Like Leia handing the updated mission file to the fleet commander
    // after the briefing so they can act on the finalized replication
    // credentials, we return the edited SyncRepl object for the caller
    // to use.
    /**
     * Returns the SyncRepl value edited by this dialog. The returned
     * object reflects all changes the operator confirmed via OK.
     *
     * @return the edited SyncRepl value
     */
    public SyncRepl getSyncRepl()
    {
        return syncRepl;
    }
}
