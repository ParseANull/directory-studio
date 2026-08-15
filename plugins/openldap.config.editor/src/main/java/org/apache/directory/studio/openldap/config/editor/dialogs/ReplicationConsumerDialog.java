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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.FilterWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.apache.directory.studio.openldap.common.ui.dialogs.AttributeDialog;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.syncrepl.BindMethod;
import org.apache.directory.studio.openldap.syncrepl.Provider;
import org.apache.directory.studio.openldap.syncrepl.SaslMechanism;
import org.apache.directory.studio.openldap.syncrepl.Scope;
import org.apache.directory.studio.openldap.syncrepl.StartTls;
import org.apache.directory.studio.openldap.syncrepl.SyncRepl;
import org.apache.directory.studio.openldap.syncrepl.Type;


// Like Leia's hologram flickering to life with the Rebel Alliance's most complete
// intelligence briefing — presenting the full SyncRepl consumer configuration in a
// single scrolled dialog with four major sections (consumer identity, provider connection,
// authentication method, and data scope), wiring every field back to the SyncRepl model
// so the administrator can configure replication from replica ID all the way through to
// the attribute list and attributes-only flag — we project this dialog.
/**
 * The ReplicationConsumerDialog is used to edit the configuration of a SyncRepl consumer.
 * We present four groups: Replication Consumer (replica ID, replication type, and a button
 * to configure options), Replication Provider Connection (host, port, encryption method),
 * Authentication (Simple or SASL tabs), and Replication Data Configuration (search base DN,
 * filter, scope, attributes table, and attributes-only checkbox). We keep OK disabled until
 * at minimum a replica ID, a provider host, and a search base DN have all been provided.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReplicationConsumerDialog extends Dialog
{
    /** The Simple Authentication tab item index */
    private static final int SIMPLE_AUTHENTICATION_TAB_ITEM_INDEX = 0;

    /** The SASL Authentication tab item index */
    private static final int SASL_AUTHENTICATION_TAB_ITEM_INDEX = 1;

    /** The SyncRepl value */
    private SyncRepl syncRepl;

    /** The connection */
    private IBrowserConnection browserConnection;

    private List<String> attributes = new ArrayList<>();

    // UI widgets
    private Button okButton;
    private ScrolledComposite scrolledComposite;
    private Composite composite;
    private Text replicaIdText;
    private ComboViewer replicationTypeComboViewer;
    private Button configureReplicationButton;
    private Text hostText;
    private Text portText;
    private ComboViewer encryptionMethodComboViewer;
    private Button configureStartTlsButton;
    private TabFolder authenticationTabFolder;
    private Text bindDnText;
    private Text credentialsText;
    private Button showCredentialsCheckbox;
    private Label saslAuthenticationLabel;
    private Button configureSaslAuthenticationButton;
    private EntryWidget searchBaseDnEntryWidget;
    private FilterWidget filterWidget;
    private ComboViewer scopeComboViewer;
    private TableViewer attributesTableViewer;
    private Button addAttributeButton;
    private Button editAttributeButton;
    private Button deleteAttributeButton;
    private Button attributesOnlyCheckbox;

    // Listeners
    private VerifyListener integerVerifyListener = event ->
        {
            if ( !event.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                event.doit = false;
            }
        };

    private ModifyListener replicatIdTextListener = event ->
        {
            String replicaId = replicaIdText.getText();

            if ( ( replicaId != null ) && ( !"".equals( replicaId ) ) )
            {
                syncRepl.setRid( replicaId );
            }
            else
            {
                syncRepl.setRid( null );
            }

            updateOkButtonEnableState();
        };

    private ISelectionChangedListener replicationTypeComboViewerListener = event -> syncRepl.setType( getReplicationType() );

    private SelectionListener configureReplicationButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            ReplicationOptionsDialog dialog = new ReplicationOptionsDialog( getShell(), syncRepl, browserConnection );
            if ( dialog.open() == ReplicationOptionsDialog.OK )
            {
                syncRepl = dialog.getSyncRepl();
                refreshUI();
            }
        }
    };

    private ModifyListener hostTextListener = event ->
        {
            syncRepl.setProvider( getProvider() );

            updateOkButtonEnableState();
        };

    private ModifyListener portTextListener = event -> syncRepl.setProvider( getProvider() );

    private ISelectionChangedListener encryptionMethodComboViewerListener = event ->
        {
            syncRepl.setProvider( getProvider() );

            // Getting the selected encryption method
            EncryptionMethod encryptionMethod = getEncryptionMethod();

            if ( ( encryptionMethod == EncryptionMethod.NO_ENCRYPTION )
                || ( encryptionMethod == EncryptionMethod.SSL_ENCRYPTION_LDAPS ) )
            {
                configureStartTlsButton.setEnabled( false );
            }
            else if ( encryptionMethod == EncryptionMethod.START_TLS_EXTENSION )
            {
                configureStartTlsButton.setEnabled( true );
            }
        };

    private SelectionListener configureStartTlsButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            // TODO
        }
    };

    private SelectionListener authenticationTabFolderListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            // Simple Authentication
            if ( authenticationTabFolder.getSelectionIndex() == SIMPLE_AUTHENTICATION_TAB_ITEM_INDEX )
            {
                syncRepl.setBindMethod( BindMethod.SIMPLE );

                // Reseting SASL authentication parameters
                syncRepl.setSaslMech( null );
                syncRepl.setAuthcid( null );
                syncRepl.setAuthzid( null );
                syncRepl.setCredentials( null );
                syncRepl.setRealm( null );
                syncRepl.setSecProps( null );
            }
            // SASL Authentication
            else if ( authenticationTabFolder.getSelectionIndex() == SASL_AUTHENTICATION_TAB_ITEM_INDEX )
            {
                syncRepl.setBindMethod( BindMethod.SASL );

                // Reseting simple authentication parameters
                syncRepl.setBindDn( null );
                syncRepl.setCredentials( null );
            }

            refreshUI();
        }
    };

    private ModifyListener bindDnTextListener = event ->
        {
            String bindDn = bindDnText.getText();

            if ( ( bindDn != null ) && ( !"".equals( bindDn ) ) )
            {
                syncRepl.setBindDn( bindDn );
            }
            else
            {
                syncRepl.setBindDn( null );
            }
        };

    private ModifyListener credentialsTextListener = event ->
        {
            String credentials = credentialsText.getText();

            if ( ( credentials != null ) && ( !"".equals( credentials ) ) )
            {
                syncRepl.setCredentials( credentials );
            }
            else
            {
                syncRepl.setCredentials( null );
            }
        };

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

    private SelectionListener configureSaslAuthenticationButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            ReplicationSaslDialog dialog = new ReplicationSaslDialog( getShell(), syncRepl, browserConnection );
            if ( dialog.open() == ReplicationSaslDialog.OK )
            {
                syncRepl = dialog.getSyncRepl();
                refreshUI();
            }
        }
    };

    private WidgetModifyListener searchBaseDnEntryWidgetListener = event ->
        {
            Dn searchBaseDn = searchBaseDnEntryWidget.getDn();

            if ( ( searchBaseDn != null ) && ( !Dn.EMPTY_DN.equals( searchBaseDn ) ) )
            {
                syncRepl.setSearchBase( searchBaseDn.getName() );
            }
            else
            {
                syncRepl.setSearchBase( null );
            }

            updateOkButtonEnableState();
        };

    private WidgetModifyListener filterWidgetListener = event ->
        {
            String filter = filterWidget.getFilter();

            if ( ( filter != null ) && ( !"".equals( filter ) ) )
            {
                syncRepl.setFilter( filter );
            }
            else
            {
                syncRepl.setFilter( null );
            }
        };

    private ISelectionChangedListener scopeComboViewerListener = event -> syncRepl.setScope( getScope() );

    private ISelectionChangedListener attributesTableViewerSelectionChangedListener = event -> updateAttributesTableButtonsState();

    private IDoubleClickListener attributesTableViewerDoubleClickListener = event -> editAttributeButtonAction();

    private SelectionListener addAttributeButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            addAttributeButtonAction();
        }
    };

    private SelectionListener editAttributeButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            editAttributeButtonAction();
        }
    };

    private SelectionListener deleteAttributeButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            deleteAttributeButtonAction();
        }
    };

    private SelectionListener attributesOnlyCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            syncRepl.setAttrsOnly( attributesOnlyCheckbox.getSelection() );
        }
    };


    // Like Leia's hologram projector powering up with no prior intelligence loaded —
    // we know who the recipient is (the browser connection) but the SyncRepl message
    // has not been composed yet, so we initialise a fresh default SyncRepl and store
    // it ready for the administrator to fill in every field from scratch.
    /**
     * Creates a new instance of ReplicationConsumerDialog with a fresh default
     * SyncRepl configuration.
     *
     * @param parentShell the parent shell
     * @param browserConnection the connection used by DN and filter widgets for lookups
     */
    public ReplicationConsumerDialog( Shell parentShell, IBrowserConnection browserConnection )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.browserConnection = browserConnection;
        this.syncRepl = createDefaultSyncRepl();
    }


    // Like Leia's hologram re-projecting an existing message — the SyncRepl
    // configuration has already been transmitted once, so we take a defensive
    // copy of it rather than editing the original, and fall back to a fresh
    // default if the caller passes null.
    /**
     * Creates a new instance of ReplicationConsumerDialog backed by the given
     * SyncRepl configuration. If {@code syncRepl} is non-null we work on a
     * copy of it; otherwise we start with a fresh default.
     *
     * @param parentShell the parent shell
     * @param syncRepl the existing SyncRepl to edit, or {@code null} to start with defaults
     * @param browserConnection the connection used by DN and filter widgets for lookups
     */
    public ReplicationConsumerDialog( Shell parentShell, SyncRepl syncRepl, IBrowserConnection browserConnection )
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


    // Like Leia's transmission beacon initialising a blank hologram packet —
    // all parameters at their zero-point defaults, ready for the administrator
    // to supply every detail — we create and return a fresh empty SyncRepl.
    /**
     * Creates and returns a default, fully-initialised SyncRepl configuration
     * with all parameters at their initial values.
     *
     * @return a new default {@link SyncRepl} instance
     */
    private SyncRepl createDefaultSyncRepl()
    {
        return new SyncRepl();
    }


    // Like Leia's hologram labelling its header before the message begins —
    // making sure the recipient instantly knows this briefing is titled
    // "Replication Consumer" — we set the shell text here.
    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( "Replication Consumer" );
    }


    // Like Leia's hologram establishing which controls end the briefing —
    // creating the OK button as the primary action and the Cancel button as
    // the exit, then immediately evaluating whether OK should start enabled
    // based on the current SyncRepl state — we set up the button bar.
    /**
     * Creates the OK and Cancel buttons for the button bar and immediately
     * calls {@link #updateOkButtonEnableState()} to set the initial enabled
     * state of the OK button.
     *
     * @param parent the button bar composite
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        updateOkButtonEnableState();
    }


    // Like Leia's hologram unfolding its complete briefing inside a scrollable
    // viewport — creating the scrolled composite, then building four major
    // sections (consumer identity, provider connection, authentication, and data
    // configuration) before loading all current SyncRepl values into the widgets —
    // we assemble the full dialog content area.
    /**
     * Creates the dialog area: a {@link ScrolledComposite} containing the
     * four replication configuration groups, all populated via {@link #refreshUI()}.
     *
     * @param parent the parent composite
     * @return the top-level scrolled composite
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
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

        createReplicationConsumerGroup( composite );
        createReplicationProviderGroup( composite );
        createReplicationAuthenticationGroup( composite );
        createReplicationDataGroup( composite );

        refreshUI();

        applyDialogFont( scrolledComposite );
        composite.setSize( composite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );

        return scrolledComposite;
    }


    // Like Leia's hologram opening with the consumer's own identity — replica ID
    // so the provider can tell which consumer is connecting, replication type
    // (Refresh And Persist or Refresh Only), and a button that launches the full
    // ReplicationOptionsDialog where all the fine-tuned sync options live — we
    // create the Replication Consumer group.
    /**
     * Creates the "Replication Consumer" group containing a replica ID text field,
     * a replication type combo (Refresh And Persist / Refresh Only), and a
     * "Configure Replication Options..." button that opens the
     * {@link ReplicationOptionsDialog}.
     *
     * @param parent the parent composite
     */
    private void createReplicationConsumerGroup( Composite parent )
    {
        // Replication Provider Group
        Group group = BaseWidgetUtils.createGroup( parent, "Replication Consumer", 1 );
        GridLayout groupGridLayout = new GridLayout( 2, false );
        group.setLayout( groupGridLayout );
        group.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Replica ID
        BaseWidgetUtils.createLabel( group, "Replica ID:", 1 );
        replicaIdText = BaseWidgetUtils.createText( group, "", 1 );
        replicaIdText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Replication Type
        BaseWidgetUtils.createLabel( group, "Replication Type:", 1 );
        replicationTypeComboViewer = new ComboViewer( group );
        replicationTypeComboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        replicationTypeComboViewer.setContentProvider( new ArrayContentProvider() );
        replicationTypeComboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof Type )
                {
                    Type type = ( Type ) element;

                    switch ( type )
                    {
                        case REFRESH_AND_PERSIST:
                            return "Refresh And Persist";
                        case REFRESH_ONLY:
                            return "Refresh Only";
                    }
                }

                return super.getText( element );
            }
        } );
        replicationTypeComboViewer.setInput( new Type[]
            { Type.REFRESH_AND_PERSIST, Type.REFRESH_ONLY } );

        // Configure Replication Options Button
        BaseWidgetUtils.createLabel( group, "", 1 );
        configureReplicationButton = BaseWidgetUtils.createButton( group, "Configure Replication Options...", 1 );
        configureReplicationButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }


    // Like Leia's hologram specifying the hyperspace coordinates of the provider —
    // the host and port the consumer must connect to, the encryption method to
    // use (plain, LDAPS, or Start TLS), and a button to configure the Start TLS
    // parameters in detail — we create the Replication Provider Connection group.
    /**
     * Creates the "Replication Provider Connection" group containing a provider
     * host text field, a provider port text field, an encryption method combo
     * (No Encryption / SSL LDAPS / Start TLS), and a "Configure Start TLS..."
     * button (enabled only when Start TLS is selected).
     *
     * @param parent the parent composite
     */
    private void createReplicationProviderGroup( Composite parent )
    {
        // Replication Provider Group
        Group group = BaseWidgetUtils.createGroup( parent, "Replication Provider Connection", 1 );
        GridLayout groupGridLayout = new GridLayout( 2, false );
        group.setLayout( groupGridLayout );
        group.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Host
        BaseWidgetUtils.createLabel( group, "Provider Host:", 1 );
        hostText = BaseWidgetUtils.createText( group, "", 1 );
        hostText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Port
        BaseWidgetUtils.createLabel( group, "Provider Port:", 1 );
        portText = BaseWidgetUtils.createText( group, "", 1 );
        portText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Encryption Method
        BaseWidgetUtils.createLabel( group, "Encryption Method:", 1 );
        encryptionMethodComboViewer = new ComboViewer( group );
        encryptionMethodComboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        encryptionMethodComboViewer.setContentProvider( new ArrayContentProvider() );
        encryptionMethodComboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof EncryptionMethod )
                {
                    EncryptionMethod encryptionMethod = ( EncryptionMethod ) element;

                    switch ( encryptionMethod )
                    {
                        case NO_ENCRYPTION:
                            return "No Encryption";
                        case SSL_ENCRYPTION_LDAPS:
                            return "Use SSL Encryption (ldaps://)";
                        case START_TLS_EXTENSION:
                            return "Use Start TLS Extension";
                    }
                }

                return super.getText( element );
            }
        } );
        encryptionMethodComboViewer.setInput( new EncryptionMethod[]
            {
                EncryptionMethod.NO_ENCRYPTION,
                EncryptionMethod.SSL_ENCRYPTION_LDAPS,
                EncryptionMethod.START_TLS_EXTENSION } );

        // Configure Start TLS Button
        BaseWidgetUtils.createLabel( group, "", 1 );
        configureStartTlsButton = BaseWidgetUtils.createButton( group, "Configure Start TLS...", 1 );
        configureStartTlsButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        configureStartTlsButton.setEnabled( false );
    }


    // Like Leia's hologram carrying authentication credentials for the Rebel contact —
    // offering two modes side-by-side in a tab folder: simple authentication (bind DN
    // and password with a show/hide toggle) or SASL authentication (a summary label
    // showing the current mechanism and IDs, plus a button to open the SASL dialog) —
    // we create the Authentication group.
    /**
     * Creates the "Authentication" group containing a {@link TabFolder} with two tabs:
     * "Simple Authentication" (bind DN text, credentials text with masking and a show
     * checkbox) and "SASL Authentication" (a summary label and a "Configure SASL
     * Authentication..." button that opens the {@link ReplicationSaslDialog}).
     *
     * @param parent the parent composite
     */
    private void createReplicationAuthenticationGroup( Composite parent )
    {
        // Replication Provider Group
        Group group = BaseWidgetUtils.createGroup( parent, "Authentication", 1 );
        GridLayout groupGridLayout = new GridLayout( 2, false );
        group.setLayout( groupGridLayout );
        group.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Authentication
        authenticationTabFolder = new TabFolder( group, SWT.TOP );
        authenticationTabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );

        // Simple Authentication Composite
        Composite simpleAuthenticationComposite = new Composite( authenticationTabFolder, SWT.NONE );
        simpleAuthenticationComposite.setLayout( new GridLayout( 2, false ) );
        simpleAuthenticationComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Bind DN Text
        BaseWidgetUtils.createLabel( simpleAuthenticationComposite, "Bind DN:", 1 );
        bindDnText = BaseWidgetUtils.createText( simpleAuthenticationComposite, "", 1 );
        bindDnText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Credentials Text
        BaseWidgetUtils.createLabel( simpleAuthenticationComposite, "Credentials:", 1 );
        credentialsText = BaseWidgetUtils.createText( simpleAuthenticationComposite, "", 1 );
        credentialsText.setEchoChar( '•' );

        // Show Credentials Checkbox
        BaseWidgetUtils.createLabel( simpleAuthenticationComposite, "", 1 );
        showCredentialsCheckbox = BaseWidgetUtils.createCheckbox( simpleAuthenticationComposite, "Show Credentials", 1 );

        // Simple Authentication TabItem
        TabItem simpleAuthenticationTabItem = new TabItem( authenticationTabFolder, SWT.NONE,
            SIMPLE_AUTHENTICATION_TAB_ITEM_INDEX );
        simpleAuthenticationTabItem.setText( "Simple Authentication" );
        simpleAuthenticationTabItem.setControl( simpleAuthenticationComposite );

        // SASL Authentication Composite
        Composite saslAuthenticationComposite = new Composite( authenticationTabFolder, SWT.NONE );
        saslAuthenticationComposite.setLayout( new GridLayout() );
        saslAuthenticationComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // SASL Authentication Wrapped Label
        saslAuthenticationLabel = new Label( saslAuthenticationComposite, SWT.WRAP | SWT.CENTER );
        saslAuthenticationLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, true ) );

        // Configure SASL Authentication Button
        configureSaslAuthenticationButton = BaseWidgetUtils.createButton( saslAuthenticationComposite,
            "Configure SASL Authentication...", 1 );
        configureSaslAuthenticationButton.setLayoutData( new GridData( SWT.CENTER, SWT.BOTTOM, true, false ) );

        // SASL Authentication TabItem
        TabItem saslAuthenticationTabItem = new TabItem( authenticationTabFolder, SWT.NONE,
            SASL_AUTHENTICATION_TAB_ITEM_INDEX );
        saslAuthenticationTabItem.setText( "SASL Authentication" );
        saslAuthenticationTabItem.setControl( saslAuthenticationComposite );
    }


    // Like Leia's hologram spelling out precisely which part of the galaxy's
    // directory the consumer should replicate — search base DN, LDAP filter,
    // scope (Subtree, Subordinate, One Level, or Base), a table of specific
    // attribute names to include, and an attributes-only checkbox so the consumer
    // can skip retrieving actual values — we build the Replication Data Configuration group.
    /**
     * Creates the "Replication Data Configuration" group containing a search base
     * DN {@link EntryWidget}, a filter {@link FilterWidget}, a scope combo
     * (SUB / SUBORD / ONE / BASE), an attributes {@link TableViewer} with Add,
     * Edit, and Delete buttons, and an "Attributes Only" checkbox.
     *
     * @param parent the parent composite
     */
    private void createReplicationDataGroup( Composite parent )
    {
        // Replication Data Group
        Group group = BaseWidgetUtils.createGroup( parent, "Replication Data Configuration", 1 );
        GridLayout groupGridLayout = new GridLayout( 3, false );
        groupGridLayout.verticalSpacing = groupGridLayout.marginHeight = 0;
        group.setLayout( groupGridLayout );
        group.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Search Base DN Text
        BaseWidgetUtils.createLabel( group, "Search Base DN:", 1 );
        searchBaseDnEntryWidget = new EntryWidget( browserConnection, Dn.EMPTY_DN );
        searchBaseDnEntryWidget.createWidget( group );

        // Filter Text
        BaseWidgetUtils.createLabel( group, "Filter:", 1 );
        filterWidget = new FilterWidget();
        filterWidget.setBrowserConnection( browserConnection );
        filterWidget.createWidget( group );

        // Scope Combo Viewer
        BaseWidgetUtils.createLabel( group, "Scope:", 1 );
        scopeComboViewer = new ComboViewer( group );
        scopeComboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        scopeComboViewer.setContentProvider( new ArrayContentProvider() );
        scopeComboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof Scope )
                {
                    Scope scope = ( Scope ) element;

                    switch ( scope )
                    {
                        case BASE:
                            return "Base";
                        case ONE:
                            return "One Level";
                        case SUB:
                            return "Subtree";
                        case SUBORD:
                            return "Subordinate Subtree";
                    }
                }

                return super.getText( element );
            }
        } );
        scopeComboViewer.setInput( new Scope[]
            { Scope.SUB, Scope.SUBORD, Scope.ONE, Scope.BASE } );

        // Attributes Table Viewer
        BaseWidgetUtils.createLabel( group, "Attributes:", 1 );
        Composite attributesTableComposite = new Composite( group, SWT.NONE );
        attributesTableComposite.setLayout( new GridLayout( 2, false ) );
        attributesTableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
        attributesTableViewer = new TableViewer( attributesTableComposite );
        attributesTableViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 3 ) );
        attributesTableViewer.setContentProvider( new ArrayContentProvider() );
        attributesTableViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES.equals( element ) )
                {
                    return SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES + " " + "<all operational attributes>";
                }
                else if ( SchemaConstants.ALL_USER_ATTRIBUTES.equals( element ) )
                {
                    return SchemaConstants.ALL_USER_ATTRIBUTES + " " + "<all user attributes>";
                }

                return super.getText( element );
            }


            @Override
            public Image getImage( Object element )
            {
                return OpenLdapConfigurationPlugin.getDefault().getImage(
                    OpenLdapConfigurationPluginConstants.IMG_ATTRIBUTE );
            }
        } );
        attributesTableViewer.setInput( attributes );

        // Add Attribute Button
        addAttributeButton = BaseWidgetUtils.createButton( attributesTableComposite, "Add...", 1 );
        addAttributeButton.setLayoutData( createNewButtonGridData() );

        // Edit Attribute Button
        editAttributeButton = BaseWidgetUtils.createButton( attributesTableComposite, "Edit...", 1 );
        editAttributeButton.setEnabled( false );
        editAttributeButton.setLayoutData( createNewButtonGridData() );

        // Delete Attribute Button
        deleteAttributeButton = BaseWidgetUtils.createButton( attributesTableComposite, "Delete", 1 );
        deleteAttributeButton.setEnabled( false );
        deleteAttributeButton.setLayoutData( createNewButtonGridData() );

        // Attributes Only Checkbox
        BaseWidgetUtils.createLabel( group, "", 1 ); //$NON-NLS-1$
        attributesOnlyCheckbox = BaseWidgetUtils.createCheckbox( group, "Attributes Only (no values)", 1 );
        attributesOnlyCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    }


    // Like Leia's hologram keeping track of which attribute in the list is
    // currently highlighted — enabling Edit and Delete only when an entry
    // is selected, disabling them when the table has nothing selected —
    // we update the attributes table button states.
    /**
     * Enables or disables the Edit and Delete attribute buttons based on
     * whether the attributes {@link TableViewer} currently has a selection.
     */
    private void updateAttributesTableButtonsState()
    {
        StructuredSelection selection = ( StructuredSelection ) attributesTableViewer.getSelection();

        editAttributeButton.setEnabled( !selection.isEmpty() );
        deleteAttributeButton.setEnabled( !selection.isEmpty() );
    }


    // Like Leia's hologram adding a new piece of intelligence to the attribute
    // roster — opening an AttributeDialog to let the administrator name the
    // attribute to replicate, then appending it to our local list and syncing
    // it back into the SyncRepl model — we handle the add-attribute action.
    /**
     * Opens an {@link AttributeDialog} to let the user pick an attribute name.
     * On confirmation, adds the chosen attribute to the local list and updates
     * the SyncRepl's attribute array, then refreshes the table viewer.
     */
    private void addAttributeButtonAction()
    {
        AttributeDialog dialog = new AttributeDialog( addAttributeButton.getShell(), browserConnection );
        if ( dialog.open() == AttributeDialog.OK )
        {
            String attribute = dialog.getAttribute();

            attributes.add( attribute );
            syncRepl.setAttributes( attributes.toArray( new String[0] ) );
            attributesTableViewer.refresh();
            attributesTableViewer.setSelection( new StructuredSelection( attribute ) );
        }
    }


    // Like Leia's hologram revising an existing intelligence item on the attribute
    // roster — pulling the selected entry into an AttributeDialog pre-filled with
    // its current name, then splicing the edited value back at the same position
    // in the list and re-syncing to the SyncRepl model — we handle the edit action.
    /**
     * Opens an {@link AttributeDialog} pre-populated with the currently selected
     * attribute name. On confirmation, replaces the old entry at the same index in
     * the local list, updates the SyncRepl's attribute array, and refreshes the viewer.
     */
    private void editAttributeButtonAction()
    {
        StructuredSelection selection = ( StructuredSelection ) attributesTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            String selectedAttribute = ( String ) selection.getFirstElement();
            AttributeDialog dialog = new AttributeDialog( editAttributeButton.getShell(), browserConnection,
                selectedAttribute );
            if ( dialog.open() == AttributeDialog.OK )
            {
                String attribute = dialog.getAttribute();
                int selectedAttributeIndex = attributes.indexOf( selectedAttribute );

                attributes.remove( selectedAttributeIndex );
                attributes.add( selectedAttributeIndex, attribute );
                syncRepl.setAttributes( attributes.toArray( new String[0] ) );
                attributesTableViewer.refresh();
                attributesTableViewer.setSelection( new StructuredSelection( attribute ) );
            }
        }
    }


    // Like Leia's hologram striking a piece of outdated intelligence from the
    // attribute roster — removing the selected entry from our local list and
    // writing the trimmed array back into the SyncRepl model before refreshing
    // the table — we handle the delete-attribute action.
    /**
     * Removes the currently selected attribute from the local list, updates
     * the SyncRepl's attribute array accordingly, and refreshes the attributes
     * table viewer.
     */
    private void deleteAttributeButtonAction()
    {
        StructuredSelection selection = ( StructuredSelection ) attributesTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            String selectedAttribute = ( String ) selection.getFirstElement();
            attributes.remove( selectedAttribute );
            syncRepl.setAttributes( attributes.toArray( new String[0] ) );
            attributesTableViewer.refresh();

            //            updateAttributesTableButtonsState();
        }
    }


    // Like Leia's hologram stamping a standard-width frame around each action
    // button so the three buttons in the attributes panel are all the same width
    // and align neatly — we create the reusable button GridData here.
    /**
     * Creates and returns a {@link GridData} instance sized to
     * {@link IDialogConstants#BUTTON_WIDTH}, used to give the attributes table's
     * Add, Edit, and Delete buttons a consistent minimum width.
     *
     * @return a new {@link GridData} with a button-width hint
     */
    private GridData createNewButtonGridData()
    {
        GridData gd = new GridData( SWT.FILL, SWT.BEGINNING, false, false );
        gd.widthHint = IDialogConstants.BUTTON_WIDTH;
        return gd;
    }


    // Like Leia's hologram refreshing its projection from the current intelligence
    // record — first removing all listeners so programmatic widget updates do not
    // fire spurious model changes, then re-populating every field (replica ID,
    // replication type, provider host/port/encryption, authentication mode, bind DN
    // or SASL summary, search base DN, filter, scope, attributes list, and
    // attributes-only flag) from the SyncRepl object, then re-adding the listeners —
    // we update the entire UI in one pass.
    /**
     * Refreshes all dialog widgets from the current {@link SyncRepl} state.
     * Listeners are removed before population and restored afterwards to prevent
     * feedback loops. Covers all four configuration groups.
     */
    private void refreshUI()
    {
        if ( syncRepl != null )
        {
            removeListeners();

            //
            // Replication Consumer
            //

            // Replica ID
            String replicaId = syncRepl.getRid();

            if ( replicaId != null )
            {
                replicaIdText.setText( replicaId );
            }
            else
            {
                replicaIdText.setText( "" );
            }

            // Replication Type
            Type replicationType = syncRepl.getType();

            if ( replicationType != null )
            {
                replicationTypeComboViewer.setSelection( new StructuredSelection( replicationType ) );
            }
            else
            {
                replicationTypeComboViewer.setSelection( new StructuredSelection( Type.REFRESH_AND_PERSIST ) );
            }

            //
            // Replication Provider Connection
            //

            // Provider
            Provider provider = syncRepl.getProvider();

            if ( provider != null )
            {
                // Provider Host
                String providerHost = provider.getHost();

                if ( providerHost != null )
                {
                    hostText.setText( providerHost );
                }
                else
                {
                    hostText.setText( "" );
                }

                // Provider Port
                int providerPort = provider.getPort();

                if ( providerPort != Provider.NO_PORT )
                {
                    portText.setText( Integer.toString( providerPort ) );
                }
                else
                {
                    portText.setText( "" );
                }

                // Encryption Type
                boolean isLdaps = provider.isLdaps();
                StartTls startTls = syncRepl.getStartTls();

                if ( isLdaps && ( startTls == null ) )
                {
                    // SSL Encryption (LDAPS)
                    encryptionMethodComboViewer.setSelection( new StructuredSelection(
                        EncryptionMethod.SSL_ENCRYPTION_LDAPS ) );
                }
                else if ( !isLdaps && ( startTls != null ) )
                {
                    // Start TLS
                    encryptionMethodComboViewer.setSelection( new StructuredSelection(
                        EncryptionMethod.START_TLS_EXTENSION ) );
                }
                else
                {
                    // No Encryption Type
                    encryptionMethodComboViewer
                        .setSelection( new StructuredSelection( EncryptionMethod.NO_ENCRYPTION ) );
                }
            }
            else
            {
                hostText.setText( "" );
                portText.setText( "" );
                encryptionMethodComboViewer.setSelection( new StructuredSelection( EncryptionMethod.NO_ENCRYPTION ) );
            }

            //
            // Authentication
            //
            BindMethod bindMethod = syncRepl.getBindMethod();

            if ( ( bindMethod == null ) || ( bindMethod == BindMethod.SIMPLE ) )
            {
                // Simple Authentication
                authenticationTabFolder.setSelection( SIMPLE_AUTHENTICATION_TAB_ITEM_INDEX );

                // Bind DN
                String bindDn = syncRepl.getBindDn();

                if ( bindDn != null )
                {
                    bindDnText.setText( bindDn );
                }
                else
                {
                    bindDnText.setText( "" );
                }

                // Credentials
                String credentials = syncRepl.getCredentials();

                if ( credentials != null )
                {
                    credentialsText.setText( credentials );
                }
                else
                {
                    credentialsText.setText( "" );
                }

                // SASL Authentication Label
                saslAuthenticationLabel.setText( getSaslAuthenticationLabelText() );
            }
            else
            {
                // SASL Authentication
                authenticationTabFolder.setSelection( SASL_AUTHENTICATION_TAB_ITEM_INDEX );

                // SASL Authentication Label
                saslAuthenticationLabel.setText( getSaslAuthenticationLabelText() );
                saslAuthenticationLabel.update();

                // Simple Authentication fields
                bindDnText.setText( "" );
                credentialsText.setText( "" );
            }

            //
            // Replication Data Configuration
            //

            // Search Base DN
            String searchBaseDn = syncRepl.getSearchBase();

            if ( searchBaseDn != null )
            {
                try
                {
                    searchBaseDnEntryWidget.setInput( browserConnection, new Dn( searchBaseDn ) );
                }
                catch ( LdapInvalidDnException e )
                {
                    // Silent
                    searchBaseDnEntryWidget.setInput( browserConnection, Dn.EMPTY_DN );
                }
            }
            else
            {
                searchBaseDnEntryWidget.setInput( browserConnection, Dn.EMPTY_DN );
            }

            // Filter
            String filter = syncRepl.getFilter();

            if ( filter != null )
            {
                filterWidget.setFilter( filter );
            }
            else
            {
                filterWidget.setFilter( "" );
            }

            // Scope
            Scope scope = syncRepl.getScope();

            if ( scope != null )
            {
                scopeComboViewer.setSelection( new StructuredSelection( scope ) );
            }
            else
            {
                scopeComboViewer.setSelection( new StructuredSelection( Scope.SUB ) );
            }

            // Attributes
            String[] attributes = syncRepl.getAttributes();
            this.attributes.clear();

            if ( attributes != null )
            {
                this.attributes.addAll( Arrays.asList( attributes ) );
                attributesTableViewer.refresh();
            }

            // Attributes Only
            attributesOnlyCheckbox.setSelection( syncRepl.isAttrsOnly() );

            addListeners();
        }
    }


    // Like Leia's hologram composing the SASL status line that appears on the
    // Authentication tab — summarising whether an authentication ID or authorisation ID
    // has been set, and which SASL mechanism will be used, so the administrator
    // can see at a glance whether SASL is configured — we build the label text.
    /**
     * Builds and returns the descriptive text displayed on the SASL Authentication
     * tab. Reports the authentication ID (preferred) or authorisation ID and the
     * SASL mechanism title, or a "not configured" message when neither ID is set.
     *
     * @return the SASL authentication label text
     */
    private String getSaslAuthenticationLabelText()
    {
        // SASL Mechanism
        String saslMechanismString = syncRepl.getSaslMech();
        String saslMechanismTitle = "(none)";
        try
        {
            SaslMechanism saslMechanism = SaslMechanism.parse( saslMechanismString );
            saslMechanismTitle = saslMechanism.getTitle();
        }
        catch ( ParseException e )
        {
            // Silent
        }

        // Authentication ID
        String authenticationId = syncRepl.getAuthcid();

        if ( ( authenticationId != null ) && ( !"".equals( authenticationId ) ) )
        {
            return NLS.bind( "Authentication ID is ''{0}'', with ''{1}'' SASL mechanism.", authenticationId,
                saslMechanismTitle );
        }

        // Authorization ID
        String authorizationId = syncRepl.getAuthzid();

        if ( ( authorizationId != null ) && ( !"".equals( authorizationId ) ) )
        {
            return NLS.bind( "Authorization ID is ''{0}'', with ''{1}'' SASL mechanism.", authorizationId,
                saslMechanismTitle );
        }

        return "SASL Authentication isn't configured.";
    }


    // Like Leia's hologram reading which replication mode the administrator
    // selected — Refresh And Persist or Refresh Only — we pull the current
    // selection from the replication type combo and return it as a Type enum.
    /**
     * Returns the {@link Type} currently selected in the replication type
     * combo viewer, or {@code null} if nothing is selected.
     *
     * @return the selected replication type, or {@code null}
     */
    private Type getReplicationType()
    {
        StructuredSelection selection = ( StructuredSelection ) replicationTypeComboViewer.getSelection();

        if ( ( selection != null ) && ( !selection.isEmpty() ) )
        {
            return ( Type ) selection.getFirstElement();
        }

        return null;
    }


    // Like Leia's hologram encoding the provider coordinates into a single
    // Provider object — reading host, port, and the LDAPS flag from the
    // relevant widgets and packaging them up — we build and return the Provider.
    /**
     * Builds a {@link Provider} from the current values of the host text,
     * port text, and encryption method combo widgets. The LDAPS flag is set
     * when {@link EncryptionMethod#SSL_ENCRYPTION_LDAPS} is selected.
     *
     * @return a new {@link Provider} reflecting the current widget state
     */
    private Provider getProvider()
    {
        Provider provider = new Provider();

        // Host
        String host = hostText.getText();

        if ( ( host != null ) && ( !"".equals( host ) ) )
        {
            provider.setHost( host );
        }
        else
        {
            provider.setHost( null );
        }

        // Port
        String portString = portText.getText();

        if ( ( host != null ) && ( !"".equals( host ) ) )
        {
            try
            {
                provider.setPort( Integer.parseInt( portString ) );
            }
            catch ( NumberFormatException e )
            {
                // Silent
                provider.setPort( Provider.NO_PORT );
            }
        }
        else
        {
            provider.setPort( Provider.NO_PORT );
        }

        // Encryption Type
        provider.setLdaps( EncryptionMethod.SSL_ENCRYPTION_LDAPS == getEncryptionMethod() );

        return provider;
    }


    // Like Leia's hologram reading which encryption channel the administrator
    // chose for the provider connection — No Encryption, SSL LDAPS, or Start TLS —
    // we pull the selected EncryptionMethod enum value from the combo and return it.
    /**
     * Returns the {@link EncryptionMethod} currently selected in the encryption
     * method combo viewer, or {@code null} if nothing is selected.
     *
     * @return the selected encryption method, or {@code null}
     */
    private EncryptionMethod getEncryptionMethod()
    {
        StructuredSelection selection = ( StructuredSelection ) encryptionMethodComboViewer.getSelection();

        if ( ( selection != null ) && ( !selection.isEmpty() ) )
        {
            return ( EncryptionMethod ) selection.getFirstElement();
        }

        return null;
    }


    // Like Leia's hologram specifying how deep into the directory tree the
    // consumer should reach — reading which Scope enum is selected in the scope
    // combo and returning it — we extract and return the current scope value.
    /**
     * Returns the {@link Scope} currently selected in the scope combo viewer,
     * or {@code null} if nothing is selected.
     *
     * @return the selected scope, or {@code null}
     */
    private Scope getScope()
    {
        StructuredSelection selection = ( StructuredSelection ) scopeComboViewer.getSelection();

        if ( ( selection != null ) && ( !selection.isEmpty() ) )
        {
            return ( Scope ) selection.getFirstElement();
        }

        return null;
    }


    // Like Leia's hologram switching on all of its sensors — wiring every text
    // field, combo, checkbox, table, and button to its corresponding listener so
    // every change the administrator makes is immediately reflected in the SyncRepl
    // model and the OK button state — we add all listeners in one place.
    /**
     * Registers all field listeners: modify listeners on text fields, selection
     * listeners on checkboxes and buttons, selection-changed listeners on combo
     * viewers and the table viewer, a double-click listener on the attributes table,
     * and widget-modify listeners on the entry and filter widgets.
     */
    private void addListeners()
    {
        replicaIdText.addModifyListener( replicatIdTextListener );
        replicaIdText.addVerifyListener( integerVerifyListener );
        replicationTypeComboViewer.addSelectionChangedListener( replicationTypeComboViewerListener );
        configureReplicationButton.addSelectionListener( configureReplicationButtonListener );
        hostText.addModifyListener( hostTextListener );
        portText.addModifyListener( portTextListener );
        portText.addVerifyListener( integerVerifyListener );
        encryptionMethodComboViewer.addSelectionChangedListener( encryptionMethodComboViewerListener );
        configureStartTlsButton.addSelectionListener( configureStartTlsButtonListener );
        authenticationTabFolder.addSelectionListener( authenticationTabFolderListener );
        bindDnText.addModifyListener( bindDnTextListener );
        credentialsText.addModifyListener( credentialsTextListener );
        showCredentialsCheckbox.addSelectionListener( showCredentialsCheckboxListener );
        configureSaslAuthenticationButton.addSelectionListener( configureSaslAuthenticationButtonListener );
        searchBaseDnEntryWidget.addWidgetModifyListener( searchBaseDnEntryWidgetListener );
        filterWidget.addWidgetModifyListener( filterWidgetListener );
        scopeComboViewer.addSelectionChangedListener( scopeComboViewerListener );
        attributesTableViewer.addSelectionChangedListener( attributesTableViewerSelectionChangedListener );
        attributesTableViewer.addDoubleClickListener( attributesTableViewerDoubleClickListener );
        addAttributeButton.addSelectionListener( addAttributeButtonListener );
        editAttributeButton.addSelectionListener( editAttributeButtonListener );
        deleteAttributeButton.addSelectionListener( deleteAttributeButtonListener );
        attributesOnlyCheckbox.addSelectionListener( attributesOnlyCheckboxListener );
    }


    // Like Leia's hologram going dark momentarily while we overwrite the projection
    // with updated intelligence — detaching every listener before we repopulate all
    // the widgets so no spurious model-update events fire during the refresh — we
    // cleanly remove all listeners here.
    /**
     * Removes all field listeners that were registered by {@link #addListeners()}.
     * Called at the start of {@link #refreshUI()} to prevent feedback loops while
     * the widgets are being repopulated from the SyncRepl model.
     */
    private void removeListeners()
    {
        replicaIdText.removeModifyListener( replicatIdTextListener );
        replicaIdText.removeVerifyListener( integerVerifyListener );
        replicationTypeComboViewer.removeSelectionChangedListener( replicationTypeComboViewerListener );
        configureReplicationButton.removeSelectionListener( configureReplicationButtonListener );
        hostText.removeModifyListener( hostTextListener );
        portText.removeModifyListener( portTextListener );
        portText.removeVerifyListener( integerVerifyListener );
        encryptionMethodComboViewer.removeSelectionChangedListener( encryptionMethodComboViewerListener );
        configureStartTlsButton.removeSelectionListener( configureStartTlsButtonListener );
        authenticationTabFolder.removeSelectionListener( authenticationTabFolderListener );
        bindDnText.removeModifyListener( bindDnTextListener );
        credentialsText.removeModifyListener( credentialsTextListener );
        showCredentialsCheckbox.removeSelectionListener( showCredentialsCheckboxListener );
        configureSaslAuthenticationButton.removeSelectionListener( configureSaslAuthenticationButtonListener );
        searchBaseDnEntryWidget.removeWidgetModifyListener( searchBaseDnEntryWidgetListener );
        filterWidget.removeWidgetModifyListener( filterWidgetListener );
        scopeComboViewer.removeSelectionChangedListener( scopeComboViewerListener );
        attributesTableViewer.removeSelectionChangedListener( attributesTableViewerSelectionChangedListener );
        attributesTableViewer.removeDoubleClickListener( attributesTableViewerDoubleClickListener );
        addAttributeButton.removeSelectionListener( addAttributeButtonListener );
        editAttributeButton.removeSelectionListener( editAttributeButtonListener );
        deleteAttributeButton.removeSelectionListener( deleteAttributeButtonListener );
        attributesOnlyCheckbox.removeSelectionListener( attributesOnlyCheckboxListener );
    }


    // Like Leia's hologram verifying that the transmission is complete before
    // authorising the send — checking that a replica ID has been given, that a
    // provider host has been specified, and that a non-empty search base DN has
    // been chosen, keeping OK disabled until all three conditions are met — we
    // update the OK button enabled state.
    /**
     * Enables the OK button only when a replica ID, a provider host, and a
     * non-empty search base DN are all present; disables it as soon as any of
     * the three required fields is missing.
     */
    private void updateOkButtonEnableState()
    {
        // Replica ID
        String replicaId = replicaIdText.getText();

        if ( ( replicaId == null ) || "".equals( replicaId ) )
        {
            okButton.setEnabled( false );
            return;
        }

        // Host
        String host = hostText.getText();

        if ( ( host == null ) || "".equals( host ) )
        {
            okButton.setEnabled( false );
            return;
        }

        // Search Base DN
        Dn searchBaseDn = searchBaseDnEntryWidget.getDn();

        if ( ( searchBaseDn == null ) || Dn.EMPTY_DN.equals( searchBaseDn ) )
        {
            // TODO add another check
            // The Search Base DN must be within the database naming context
            okButton.setEnabled( false );
            return;
        }

        okButton.setEnabled( true );
    }


    // Like Leia's hologram handing its finished intelligence packet to the
    // waiting Rebel officer — returning the SyncRepl object that holds all
    // the configuration the administrator just set up so the caller can
    // persist it — we expose getSyncRepl().
    /**
     * Returns the {@link SyncRepl} value configured by this dialog.
     * After the dialog is closed with OK the returned object reflects all
     * changes the administrator made.
     *
     * @return the SyncRepl value
     */
    public SyncRepl getSyncRepl()
    {
        return syncRepl;
    }

    // Like Leia's hologram encoding the transmission channel — specifying whether
    // the connection to the provider travels in the clear, over SSL (LDAPS), or
    // is upgraded via the Start TLS extension — we define the EncryptionMethod enum.
    /**
     * Enum representing the encryption method used for the provider connection.
     * We map the three options (plain, SSL LDAPS, Start TLS) to combo-viewer items.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private enum EncryptionMethod
    {
        NO_ENCRYPTION,
        SSL_ENCRYPTION_LDAPS,
        START_TLS_EXTENSION
    }
}
