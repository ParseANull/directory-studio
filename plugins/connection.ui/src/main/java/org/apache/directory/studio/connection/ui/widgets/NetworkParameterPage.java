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

package org.apache.directory.studio.connection.ui.widgets;


import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.ldap.model.url.LdapUrl.Extension;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.HistoryUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;
import org.apache.directory.studio.connection.core.jobs.CheckNetworkParameterRunnable;
import org.apache.directory.studio.connection.ui.AbstractConnectionParameterPage;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.connection.ui.dialogs.CertificateInfoDialog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: NetworkParameterPage — REBEL SHIP HANGAR ADDRESS FORM ─────────────────
// When a new X-Wing joins the Rebel fleet the ground crew need to know its hangar
// bay (hostname), docking port (port number), how to dial in (encryption method),
// and how long to wait before giving up (timeout).  They also need to mark it as
// read-only if it's a borrowed ship and not to be modified.
// NetworkParameterPage is that intake form.  It is the "Network Parameter" tab in
// the New Connection Wizard and the Connection Properties dialog.  It collects:
//   - Connection name (the callsign)
//   - Hostname + port
//   - Timeout in seconds
//   - Encryption method (none / LDAPS / StartTLS)
//   - A link to the certificate validation preference page (or a warning if
//     certificate validation is disabled)
//   - A "View Certificate" button that connects, grabs the server cert, and opens
//     CertificateInfoDialog
//   - A "Check Network Parameter" button that verifies connectivity
//   - A "Read-only" checkbox
// validate() drives the enabled/disabled state of the two buttons and produces the
// "please enter X" messages shown in the wizard chrome.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link AbstractConnectionParameterPage} for the network-level connection settings.
 *
 * <p>This is the first tab in the New Connection Wizard and the Connection
 * Properties dialog.  It collects:</p>
 * <ul>
 *   <li>Connection name</li>
 *   <li>Hostname and port (with combo history)</li>
 *   <li>Timeout in seconds</li>
 *   <li>Encryption method (none / LDAPS / StartTLS)</li>
 *   <li>Read-only flag</li>
 * </ul>
 *
 * <p>Two action buttons are provided:</p>
 * <ul>
 *   <li><strong>Check Network Parameter</strong> — runs
 *       {@link CheckNetworkParameterRunnable} in a modal context and reports
 *       success/failure plus TLS protocol and cipher suite if applicable.</li>
 *   <li><strong>View Certificate</strong> — same runnable, then opens
 *       {@link CertificateInfoDialog} with the server's certificate chain.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NetworkParameterPage extends AbstractConnectionParameterPage
{
    // ── X-LDAP-URL EXTENSION CONSTANTS ────────────────────────────────────────────

    /** X-LDAP-URL extension key for the connection name. */
    private static final String X_CONNECTION_NAME = "X-CONNECTION-NAME"; //$NON-NLS-1$

    /** X-LDAP-URL extension key for the encryption method. */
    private static final String X_ENCRYPTION = "X-ENCRYPTION"; //$NON-NLS-1$

    /** X-ENCRYPTION value for LDAPS. */
    private static final String X_ENCRYPTION_LDAPS = "ldaps"; //$NON-NLS-1$

    /** X-ENCRYPTION value for StartTLS. */
    private static final String X_ENCRYPTION_START_TLS = "StartTLS"; //$NON-NLS-1$


    // ── UI FIELDS ─────────────────────────────────────────────────────────────────

    /** Text field for the connection name. */
    private Text nameText;

    /** Combo with hostname history. */
    private Combo hostCombo;

    /** Combo with port history; digits-only. */
    private Combo portCombo;

    /** Read-only combo for encryption method (None / LDAPS / StartTLS). */
    private Combo encryptionMethodCombo;

    /** Button that connects and shows the server's TLS certificate. */
    private Button viewServerCertificateButton;

    /** Button that verifies basic connectivity. */
    private Button checkConnectionButton;

    /** Checkbox: prevent add/delete/modify/rename on this connection. */
    private Button readOnlyConnectionCheckbox;

    /** Digits-only text field for the connection timeout (seconds). */
    private Text timeoutSecondsText;


    // ── LISTENERS ─────────────────────────────────────────────────────────────────

    /**
     * Listener for the SWT {@link Link} widget that points to the certificate
     * validation preference page.  Clicking the link opens the preference dialog
     * pre-focused on {@code CertificateValidationPreferencePage}.
     */
    private SelectionAdapter linkDataWidgetListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            String certificateValidationPreferencePageId = ConnectionUIPlugin.getDefault()
                .getPluginProperties().getString( "PrefPage_CertificateValidationPreferencePage_id" ); //$NON-NLS-1$

            PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn( Display.getDefault()
                .getActiveShell(), certificateValidationPreferencePageId, new String[]
                { certificateValidationPreferencePageId }, null );
            dialog.open();
        }
    };


    // ── PRIVATE GETTERS ───────────────────────────────────────────────────────────

    /**
     * Returns the connection name from the name text field.
     *
     * @return The connection name string.
     */
    private String getName()
    {
        return nameText.getText();
    }


    /**
     * Returns the hostname from the host combo.
     *
     * @return The hostname string.
     */
    private String getHostName()
    {
        return hostCombo.getText();
    }


    /**
     * Returns the port number parsed from the port combo text.
     *
     * @return The port number.
     */
    private int getPort()
    {
        return Integer.parseInt( portCombo.getText() );
    }


    /**
     * Returns the timeout in seconds from the timeout text field.
     *
     * <p>Defaults to {@code 30} seconds if the field is empty.</p>
     *
     * @return The timeout in seconds.
     */
    private int getTimeoutSeconds()
    {
        String timeoutSecondsString = timeoutSecondsText.getText();

        if ( Strings.isEmpty( timeoutSecondsString ) )
        {
            return 30;
        }
        else
        {
            return Integer.parseInt( timeoutSecondsString );
        }
    }


    /**
     * Returns the encryption method from the encryption combo selection.
     *
     * <p>Index 0 = NONE, index 1 = LDAPS, index 2 = START_TLS.</p>
     *
     * @return The selected {@link ConnectionParameter.EncryptionMethod}.
     */
    private ConnectionParameter.EncryptionMethod getEncyrptionMethod()
    {
        switch ( encryptionMethodCombo.getSelectionIndex() )
        {
            case 1:
                return ConnectionParameter.EncryptionMethod.LDAPS;

            case 2:
                return ConnectionParameter.EncryptionMethod.START_TLS;

            default:
                return ConnectionParameter.EncryptionMethod.NONE;
        }
    }


    /**
     * Builds a temporary {@link Connection} with the current field values for
     * use by the "Check" and "View Certificate" buttons.
     *
     * <p>The auth method is set to NONE — we only want to verify network
     * connectivity, not perform a bind.</p>
     *
     * @return A disposable {@link Connection} for testing.
     */
    private Connection getTestConnection()
    {
        ConnectionParameter connectionParameter = new ConnectionParameter( null, getHostName(), getPort(),
            getEncyrptionMethod(), ConnectionParameter.AuthenticationMethod.NONE, null, null, null, true, null,
            30000L );

        return new Connection( connectionParameter );
    }


    /**
     * Returns whether the read-only checkbox is checked.
     *
     * @return {@code true} if the connection should be read-only.
     */
    private boolean isReadOnly()
    {
        return readOnlyConnectionCheckbox.getSelection();
    }


    // ── CREATE COMPOSITE ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Builds the page UI:</p>
     * <ol>
     *   <li>Connection name text field.</li>
     *   <li>A "Network Parameter" group containing hostname combo, port combo,
     *       timeout field, encryption combo, a certificate-validation link (or
     *       warning), and the View Certificate / Check Network Parameter
     *       buttons.</li>
     *   <li>A Read-only checkbox below the group.</li>
     * </ol>
     *
     * @param parent The parent composite to create the page content inside.
     */
    @Override
    protected void createComposite( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // ── CONNECTION NAME ───────────────────────────────────────────────────────
        Composite nameComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        BaseWidgetUtils.createLabel( nameComposite, Messages.getString( "NetworkParameterPage.ConnectionName" ), 1 ); //$NON-NLS-1$
        nameText = BaseWidgetUtils.createText( nameComposite, StringUtils.EMPTY, 1 ); //$NON-NLS-1$

        BaseWidgetUtils.createSpacer( composite, 1 );

        // ── NETWORK PARAMETER GROUP ───────────────────────────────────────────────
        Group group = BaseWidgetUtils.createGroup( composite, Messages
            .getString( "NetworkParameterPage.NetworkParameter" ), 1 ); //$NON-NLS-1$

        IDialogSettings dialogSettings = ConnectionUIPlugin.getDefault().getDialogSettings();

        Composite groupComposite = BaseWidgetUtils.createColumnContainer( group, 3, 1 );

        // Hostname
        BaseWidgetUtils.createLabel( groupComposite, Messages.getString( "NetworkParameterPage.HostName" ), 1 ); //$NON-NLS-1$
        String[] hostHistory = HistoryUtils.load( dialogSettings,
            ConnectionUIConstants.DIALOGSETTING_KEY_HOST_HISTORY );
        hostCombo = BaseWidgetUtils.createCombo( groupComposite, hostHistory, -1, 2 );

        // Port
        BaseWidgetUtils.createLabel( groupComposite, Messages.getString( "NetworkParameterPage.Port" ), 1 ); //$NON-NLS-1$
        String[] portHistory = HistoryUtils.load( dialogSettings,
            ConnectionUIConstants.DIALOGSETTING_KEY_PORT_HISTORY );
        portCombo = BaseWidgetUtils.createCombo( groupComposite, portHistory, -1, 2 );
        portCombo.setTextLimit( 5 );
        portCombo.setText( "389" ); //$NON-NLS-1$

        // Timeout
        BaseWidgetUtils.createLabel( groupComposite, Messages.getString( "NetworkParameterPage.Timeout" ), 2 ); //$NON-NLS-1$
        timeoutSecondsText = BaseWidgetUtils.createText( groupComposite, "30", 1 ); //$NON-NLS-1$
        timeoutSecondsText.setTextLimit( 7 );

        // Encryption method
        String[] encMethods = new String[]
            {
                Messages.getString( "NetworkParameterPage.NoEncryption" ), //$NON-NLS-1$
                Messages.getString( "NetworkParameterPage.UseSSLEncryption" ), //$NON-NLS-1$
                Messages.getString( "NetworkParameterPage.UseStartTLS" ) //$NON-NLS-1$
            };

        BaseWidgetUtils.createLabel( groupComposite, Messages.getString( "NetworkParameterPage.EncryptionMethod" ), 1 ); //$NON-NLS-1$
        encryptionMethodCombo = BaseWidgetUtils.createReadonlyCombo( groupComposite, encMethods, 0, 2 );

        // ── CERT VALIDATION LINK OR WARNING ───────────────────────────────────────
        // Show a clickable link to the preferences page if validation is on, or a
        // plain warning label if validation is disabled.
        // ──────────────────────────────────────────────────────────────────────────
        boolean validateCertificates = ConnectionCorePlugin.getDefault().getPluginPreferences().getBoolean(
            ConnectionCoreConstants.PREFERENCE_VALIDATE_CERTIFICATES );

        if ( validateCertificates )
        {
            BaseWidgetUtils.createSpacer( groupComposite, 1 );

            Link link = BaseWidgetUtils.createLink( groupComposite,
                Messages.getString( "NetworkParameterPage.CertificateValidationLink" ), 2 ); //$NON-NLS-1$
            GridData linkGridData = new GridData( GridData.FILL_HORIZONTAL );
            linkGridData.horizontalSpan = 2;
            linkGridData.widthHint = 100;
            link.setLayoutData( linkGridData );
            link.addSelectionListener( linkDataWidgetListener );
        }
        else
        {
            BaseWidgetUtils.createSpacer( groupComposite, 1 );
            BaseWidgetUtils.createLabel( groupComposite, Messages
                .getString( "NetworkParameterPage.WarningCertificateValidation" ), 2 ); //$NON-NLS-1$
        }

        // ── ACTION BUTTONS ────────────────────────────────────────────────────────
        BaseWidgetUtils.createSpacer( groupComposite, 1 );
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.BOTTOM;
        viewServerCertificateButton = new Button( groupComposite, SWT.PUSH );
        viewServerCertificateButton.setLayoutData( gridData );
        viewServerCertificateButton.setText( Messages.getString( "NetworkParameterPage.ViewCertificate" ) ); //$NON-NLS-1$
        checkConnectionButton = new Button( groupComposite, SWT.PUSH );
        checkConnectionButton.setLayoutData( gridData );
        checkConnectionButton.setText( Messages.getString( "NetworkParameterPage.CheckNetworkParameter" ) ); //$NON-NLS-1$

        // ── READ-ONLY CHECKBOX ────────────────────────────────────────────────────
        readOnlyConnectionCheckbox = BaseWidgetUtils.createCheckbox( composite,
            Messages.getString( "NetworkParameterPage.ReadOnly" ), 1 ); //$NON-NLS-1$

        BaseWidgetUtils.createSpacer( composite, 1 );
        nameText.setFocus();
    }


    // ── VALIDATE ──────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Enables/disables the action buttons based on whether host and port are
     * filled in, then validates all required fields in order of priority:</p>
     * <ol>
     *   <li>Empty connection name → sets {@code message}.</li>
     *   <li>Empty hostname → sets {@code message}.</li>
     *   <li>Empty port → sets {@code message}.</li>
     *   <li>Duplicate connection name → sets {@code errorMessage}.</li>
     * </ol>
     */
    @Override
    protected void validate()
    {
        // ── BUTTON ENABLE STATE ───────────────────────────────────────────────────
        checkConnectionButton.setEnabled( !hostCombo.getText().equals( StringUtils.EMPTY ) &&
            !portCombo.getText().equals( StringUtils.EMPTY ) );

        viewServerCertificateButton.setEnabled( checkConnectionButton.isEnabled()
            && getEncyrptionMethod() != EncryptionMethod.NONE );

        // ── VALIDATION MESSAGES ───────────────────────────────────────────────────
        message = null;
        infoMessage = null;
        errorMessage = null;

        if ( Strings.isEmpty( portCombo.getText() ) ) //$NON-NLS-1$
        {
            message = Messages.getString( "NetworkParameterPage.PleaseEnterPort" ); //$NON-NLS-1$
        }

        if ( Strings.isEmpty( hostCombo.getText() ) ) //$NON-NLS-1$
        {
            message = Messages.getString( "NetworkParameterPage.PleaseEnterHostname" ); //$NON-NLS-1$
        }

        if ( Strings.isEmpty( nameText.getText() ) ) //$NON-NLS-1$
        {
            message = Messages.getString( "NetworkParameterPage.PleaseEnterConnectionName" ); //$NON-NLS-1$
        }

        // ── DEFAULT TIMEOUT ───────────────────────────────────────────────────────
        // If the timeout field is cleared, silently restore the default (30 s).
        // ──────────────────────────────────────────────────────────────────────────
        if ( Strings.isEmpty( timeoutSecondsText.getText() ) ) //$NON-NLS-1$
        {
            timeoutSecondsText.setText( "30" );
        }

        // ── DUPLICATE NAME ────────────────────────────────────────────────────────
        // If another connection already uses this name (and it's not the connection
        // we're currently editing), report it as an error.
        // ──────────────────────────────────────────────────────────────────────────
        if ( ConnectionCorePlugin.getDefault().getConnectionManager().getConnectionByName( nameText.getText() ) != null
            && ( ( connectionParameter == null ) || !nameText.getText().equals( connectionParameter.getName() ) ) )
        {
            errorMessage = NLS.bind(
                Messages.getString( "NetworkParameterPage.ConnectionExists" ), new String[] //$NON-NLS-1$
                { nameText.getText() } );
        }
    }


    // ── LOAD PARAMETERS ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Populates all fields from the given {@link ConnectionParameter}.  Selects
     * the correct encryption method combo index and converts the stored
     * timeout-millis to seconds.</p>
     *
     * @param parameter The existing connection parameters to load.
     */
    @Override
    protected void loadParameters( ConnectionParameter parameter )
    {
        connectionParameter = parameter;

        nameText.setText( CommonUIUtils.getTextValue( parameter.getName() ) );
        hostCombo.setText( CommonUIUtils.getTextValue( parameter.getHost() ) );
        portCombo.setText( Integer.toString( parameter.getPort() ) );
        int encryptionMethodIndex = 0;

        if ( parameter.getEncryptionMethod() == EncryptionMethod.LDAPS )
        {
            encryptionMethodIndex = 1;
        }
        else if ( parameter.getEncryptionMethod() == EncryptionMethod.START_TLS )
        {
            encryptionMethodIndex = 2;
        }

        encryptionMethodCombo.select( encryptionMethodIndex );
        readOnlyConnectionCheckbox.setSelection( parameter.isReadOnly() );
        timeoutSecondsText.setText( Long.toString( parameter.getTimeoutMillis() / 1000L ) );

    }


    // ── INIT LISTENERS ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Wires modify/selection/verify listeners on all fields so the wizard
     * chrome updates on every keystroke or selection change.  The port and timeout
     * fields only accept digit characters (verified via verify listeners).</p>
     */
    @Override
    protected void initListeners()
    {
        nameText.addModifyListener( event -> connectionPageModified() );

        hostCombo.addModifyListener( event -> connectionPageModified() );

        // ── PORT: DIGITS ONLY ─────────────────────────────────────────────────────
        portCombo.addVerifyListener( event -> {
            if ( !event.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                event.doit = false;
            }
        } );

        portCombo.addModifyListener( event -> connectionPageModified() );

        encryptionMethodCombo.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        // ── CHECK NETWORK PARAMETER BUTTON ────────────────────────────────────────
        checkConnectionButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             *
             * Runs {@link CheckNetworkParameterRunnable} in a modal progress context.
             * On success, shows a dialog with the result and, if TLS is active, the
             * negotiated protocol and cipher suite.
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                Connection connection = getTestConnection();
                CheckNetworkParameterRunnable runnable = new CheckNetworkParameterRunnable( connection );
                IStatus status = RunnableContextRunner.execute( runnable, runnableContext, true );

                if ( status.isOK() )
                {
                    String title = Messages.getString( "NetworkParameterPage.CheckNetworkParameter" ); //$NON-NLS-1$
                    String message = Messages.getString( "NetworkParameterPage.ConnectionEstablished" ); //$NON-NLS-1$

                    SSLSession sslSession = runnable.getSslSession();
                    if ( sslSession != null )
                    {
                        message += "\n\nProtocol: " + sslSession.getProtocol();
                        message += "\nCipher Suite: " + sslSession.getCipherSuite();
                    }
                    MessageDialog.openInformation( Display.getDefault().getActiveShell(), title, message );
                }
            }
        } );

        // ── VIEW SERVER CERTIFICATE BUTTON ────────────────────────────────────────
        viewServerCertificateButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             *
             * Runs {@link CheckNetworkParameterRunnable}, then opens
             * {@link CertificateInfoDialog} with the server's certificate chain.
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                Connection connection = getTestConnection();
                CheckNetworkParameterRunnable runnable = new CheckNetworkParameterRunnable( connection );
                IStatus status = RunnableContextRunner.execute( runnable, runnableContext, true );

                if ( status.isOK() )
                {
                    try
                    {
                        SSLSession sslSession = runnable.getSslSession();
                        Certificate[] certificates = sslSession.getPeerCertificates();
                        X509Certificate[] serverCertificates = new X509Certificate[certificates.length];
                        for ( int i = 0; i < certificates.length; i++ )
                        {
                            serverCertificates[i] = ( X509Certificate ) certificates[i];
                        }
                        new CertificateInfoDialog( Display.getDefault().getActiveShell(), serverCertificates ).open();
                    }
                    catch ( SSLPeerUnverifiedException e )
                    {
                        throw new RuntimeException( e );
                    }
                }
            }
        } );

        readOnlyConnectionCheckbox.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                connectionPageModified();
            }
        } );

        // ── TIMEOUT: DIGITS ONLY ──────────────────────────────────────────────────
        timeoutSecondsText.addModifyListener( event -> connectionPageModified() );

        timeoutSecondsText.addVerifyListener( event -> {
            if ( !event.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                event.doit = false;
            }
        } );
    }


    // ── SAVE PARAMETERS ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Writes name, host, port, encryption method, read-only flag, and
     * timeout (converted to millis) into the given {@link ConnectionParameter}.</p>
     *
     * @param parameter The parameter bean to populate.
     */
    @Override
    public void saveParameters( ConnectionParameter parameter )
    {
        parameter.setName( getName() );
        parameter.setHost( getHostName() );
        parameter.setPort( getPort() );
        parameter.setEncryptionMethod( getEncyrptionMethod() );
        parameter.setReadOnly( isReadOnly() );
        parameter.setTimeoutMillis( getTimeoutSeconds() * 1000L );
    }


    // ── SAVE DIALOG SETTINGS ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Persists the current hostname and port to the dialog settings history so
     * they appear in the combos next time.</p>
     */
    @Override
    public void saveDialogSettings()
    {
        IDialogSettings dialogSettings = ConnectionUIPlugin.getDefault().getDialogSettings();
        HistoryUtils.save( dialogSettings, ConnectionUIConstants.DIALOGSETTING_KEY_HOST_HISTORY, hostCombo.getText() );
        HistoryUtils.save( dialogSettings, ConnectionUIConstants.DIALOGSETTING_KEY_PORT_HISTORY, portCombo.getText() );
    }


    // ── SET FOCUS ─────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Gives keyboard focus to the connection name field, which is the first
     * thing a user needs to fill in.</p>
     */
    @Override
    public void setFocus()
    {
        nameText.setFocus();
    }


    // ── ARE PARAMETERS MODIFIED ───────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} if any reconnection-required field changed, or if
     * the connection name changed (name changes don't require reconnection but they
     * are still a modification).</p>
     *
     * @return {@code true} if any field was modified.
     */
    @Override
    public boolean areParametersModifed()
    {
        return isReconnectionRequired() || !StringUtils.equals( connectionParameter.getName(), getName() );
    }


    // ── IS RECONNECTION REQUIRED ──────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} if host, port, encryption method, read-only flag,
     * or timeout changed — all of which require closing and re-opening the
     * connection.  A name-only change does not require reconnection.</p>
     *
     * @return {@code true} if the connection must be closed and reopened.
     */
    @Override
    public boolean isReconnectionRequired()
    {
        return ( connectionParameter == null )
            || ( !StringUtils.equals( connectionParameter.getHost(), getHostName() ) )
            || ( connectionParameter.getPort() != getPort() )
            || ( connectionParameter.getEncryptionMethod() != getEncyrptionMethod() )
            || ( connectionParameter.isReadOnly() != isReadOnly() )
            || ( connectionParameter.getTimeoutMillis() != getTimeoutSeconds() * 1000L );
    }


    // ── MERGE PARAMETERS TO LDAP URL ──────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Serialises the network parameters into an LDAP URL using non-standard
     * X-LDAP-URL extensions:
     * <ul>
     *   <li>{@code X-CONNECTION-NAME} — the connection name.</li>
     *   <li>host and port are set directly on the URL.</li>
     *   <li>{@code X-ENCRYPTION=ldaps} or {@code X-ENCRYPTION=StartTLS} when
     *       applicable.</li>
     * </ul></p>
     *
     * @param parameter The connection parameters to serialise.
     * @param ldapUrl   The LDAP URL to write extensions into.
     */
    @Override
    public void mergeParametersToLdapURL( ConnectionParameter parameter, LdapUrl ldapUrl )
    {
        ldapUrl.getExtensions().add( new Extension( false, X_CONNECTION_NAME, parameter.getName() ) );
        ldapUrl.setHost( parameter.getHost() );
        ldapUrl.setPort( parameter.getPort() );

        switch ( parameter.getEncryptionMethod() )
        {
            case NONE:
                // default — no extension needed
                break;

            case LDAPS:
                ldapUrl.getExtensions().add( new Extension( false, X_ENCRYPTION, X_ENCRYPTION_LDAPS ) );
                break;

            case START_TLS:
                ldapUrl.getExtensions().add( new Extension( false, X_ENCRYPTION, X_ENCRYPTION_START_TLS ) );
                break;
        }
    }


    // ── MERGE LDAP URL TO PARAMETERS ─────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Reads back the network parameters from an LDAP URL.  If the
     * {@code X-CONNECTION-NAME} extension is absent, defaults to the current
     * date/time as the connection name.  The encryption method defaults to
     * {@link EncryptionMethod#NONE} if the {@code X-ENCRYPTION} extension is
     * absent or unrecognised.</p>
     *
     * @param ldapUrl   The LDAP URL to read extensions from.
     * @param parameter The connection parameter bean to populate.
     */
    @Override
    public void mergeLdapUrlToParameters( LdapUrl ldapUrl, ConnectionParameter parameter )
    {
        // ── CONNECTION NAME ───────────────────────────────────────────────────────
        String name = ldapUrl.getExtensionValue( X_CONNECTION_NAME );

        if ( StringUtils.isEmpty( name ) )
        {
            name = new SimpleDateFormat( "yyyy-MM-dd HH-mm-ss" ).format( new Date() ); //$NON-NLS-1$
        }

        parameter.setName( name );

        // ── HOST AND PORT ─────────────────────────────────────────────────────────
        parameter.setHost( ldapUrl.getHost() );
        parameter.setPort( ldapUrl.getPort() );

        // ── ENCRYPTION METHOD ─────────────────────────────────────────────────────
        String encryption = ldapUrl.getExtensionValue( X_ENCRYPTION );

        if ( StringUtils.isNotEmpty( encryption ) && X_ENCRYPTION_LDAPS.equalsIgnoreCase( encryption ) )
        {
            parameter.setEncryptionMethod( ConnectionParameter.EncryptionMethod.LDAPS );
        }
        else if ( StringUtils.isNotEmpty( encryption ) && X_ENCRYPTION_START_TLS.equalsIgnoreCase( encryption ) )
        {
            parameter.setEncryptionMethod( ConnectionParameter.EncryptionMethod.START_TLS );
        }
        else
        {
            parameter.setEncryptionMethod( ConnectionParameter.EncryptionMethod.NONE );
        }
    }
}
