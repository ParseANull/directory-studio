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
package org.apache.directory.studio.apacheds.configuration.editor;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.api.ldap.model.constants.SupportedSaslMechanisms;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.server.config.beans.DirectoryServiceBean;
import org.apache.directory.server.config.beans.ExtendedOpHandlerBean;
import org.apache.directory.server.config.beans.InterceptorBean;
import org.apache.directory.server.config.beans.LdapServerBean;
import org.apache.directory.server.config.beans.SaslMechHandlerBean;
import org.apache.directory.server.config.beans.TcpTransportBean;
import org.apache.directory.server.config.beans.TransportBean;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;


// ── CLASS: LdapLdapsServersPage — THE COMMS AND TRANSPORT CONTROL ROOM ───────
// The Death Star's comms officers man the transport control room — deciding
// which frequencies are open, which ports are listening, which cipher suites
// are authorised, and how the station authenticates callers before they get in.
// This page is that room: every LDAP and LDAPS transport setting lives here,
// from port numbers to TLS cipher lists to SASL realms and hashing methods.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The LDAP/LDAPS Servers configuration page in the ServerConfigurationEditor.
 * Lets you enable or disable plain LDAP and encrypted LDAPS transports, set
 * ports and bind addresses, tune thread counts and backlog sizes, pick SSL
 * cipher suites and TLS protocol versions, configure SASL settings and
 * authentication mechanisms, and toggle advanced options like server-side
 * password hashing and StartTLS support.
 * Think of this page as the Death Star's comms/transport control room —
 * every knob and checkbox here controls how the server listens for and
 * authenticates incoming connections.
 *  
 * <pre>
 * +-------------------------------------------------------------------------------+
 * | +------------------------------------+ +------------------------------------+ |
 * | | .--------------------------------. | | .--------------------------------. | |
 * | | |V LDAP/LDAPS servers            | | | |V Supported Authn Mechanisms    | | |
 * | | +--------------------------------+ | | +--------------------------------+ | |
 * | | | [X] Enabled LDAP Server        | | | | [X] Simple      [X] GSSAPI     | | |
 * | | |  Address  : [////////////////] | | | | [X] CRAM-MD5    [X] Digest-MD5 | | |
 * | | |  Port     : [/////////]        | | | | [X] NTLM                       | | |
 * | | |  nbThreads: [/////////]        | | | |   Provider : [///////////////] | | |
 * | | |  backLog  : [/////////]        | | | | [X] GSS_SPNEGO                 | | |
 * | | | [X] Enabled LDAPS Server       | | | |   Provider : [///////////////] | | |
 * | | |  Address  : [////////////////] | | | | [X] Delegated                  | | |
 * | | |  Port     : [/////////]        | | | |   Host    : [////////////////] | | |
 * | | |  nbThreads: [/////////]        | | | |   Port    : [/////]            | | |
 * | | |  backLog  : [/////////]        | | | |   Ssl/tls : [====]             | | |
 * | | +--------------------------------+ | | |     Trust : [////////////////] | | |
 * | | .--------------------------------. | | |   Base DN : [////////////////] | | |
 * | | |V Server limits                 | | | +--------------------------------+ | |
 * | | +--------------------------------+ | | .--------------------------------. | |
 * | | |    Max time limit : [////////] | | | |V SASL Settings                 | | |
 * | | |    Max size limit : [////////] | | | +--------------------------------+ | |
 * | | |    Max PDU size   : [////////] | | | | SASL Host      : [///////////] | | |
 * | | +--------------------------------+ | | | SASL Principal : [///////////] | | |
 * | | .--------------------------------. | | | Search Base DN : [///////////] | | |
 * | | |V SSL/Start TLS keystore        | | | | SASL realms    :               | | |
 * | | +--------------------------------+ | | |   +-----------------+          | | |
 * | | |  keystore : [////////] (browse)| | | |   |                 | (add)    | | |
 * | | |  password : [////////////////] | | | |   |                 | (edit)   | | |
 * | | |             [X] Show password  | | | |   |                 | (delete) | | |
 * | | +--------------------------------+ | | |   +-----------------+          | | |
 * | | .--------------------------------. | | +--------------------------------+ | |
 * | | |V SSL Advanced Settings         | | |                                    | |
 * | | +--------------------------------+ | |                                    | |
 * | | |  [X] Require Client Auth       | | |                                    | |
 * | | |    [X] Request Client Auth     | | |                                    | |
 * | | |  Ciphers suite :               | | |                                    | |
 * | | |   +--------------------------+ | | |                                    | |
 * | | |   |[X] xyz                   | | | |                                    | |
 * | | |   |[X] abc                   | | | |                                    | |
 * | | |   |[X] def                   | | | |                                    | |
 * | | |   +--------------------------+ | | |                                    | |
 * | | | Enabled protocols :            | | |                                    | |
 * | | | [X] SSLv3  [X] TLSv1           | | |                                    | |
 * | | |        [X] TLSv1.1 [X] TLSv1.2 | | |                                    | |
 * | | +--------------------------------+ | |                                    | |
 * | | .--------------------------------. | |                                    | |
 * | | |V Advanced                      | | |                                    | |
 * | | +--------------------------------+ | |                                    | |
 * | | | [X] Enable TLS                 | | |                                    | |
 * | | | [X] Enable ServerSide PWD hash | | |                                    | |
 * | | |      hashing method {========} | | |                                    | |
 * | | | Replication pinger sleep [XXX] | | |                                    | |
 * | | | Disk sync delay [XXX]          | | |                                    | |
 * | | +--------------------------------+ | |                                    | |
 * | +------------------------------------+ +------------------------------------+ |
 * +-------------------------------------------------------------------------------+
 * </pre>
 * 
 * We manage the following parameters :
 * LDAP server controls. We manage :
 * <ul>
 * <li>the address</li>
 * <li>the port</li>
 * <li>the number of dedicated threads</li>
 * <li>the backlog size</li>
 * </ul> 
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapLdapsServersPage extends ServerConfigurationEditorPage
{
    private static final int DEFAULT_NB_THREADS = 4;
    private static final int DEFAULT_BACKLOG_SIZE = 50;
    private static final String TRANSPORT_ID_LDAP = "ldap"; //$NON-NLS-1$
    public static final String TRANSPORT_ID_LDAPS = "ldaps"; //$NON-NLS-1$
    private static final String SASL_MECHANISMS_SIMPLE = "SIMPLE"; //$NON-NLS-1$
    private static final String SSL_V3 = "SSLv3";
    private static final String TLS_V1_0 = "TLSv1";
    private static final String TLS_V1_1 = "TLSv1.1";
    private static final String TLS_V1_2 = "TLSv1.2";
    private static final String START_TLS_HANDLER_ID = "starttlshandler"; //$NON-NLS-1$
    private static final String START_TLS_HANDLER_CLASS = "org.apache.directory.server.ldap.handlers.extended.StartTlsHandler"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_ID = "passwordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA512 = "org.apache.directory.server.core.hash.Ssha512PasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA512 = "org.apache.directory.server.core.hash.Sha512PasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA384 = "org.apache.directory.server.core.hash.Ssha384PasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA384 = "org.apache.directory.server.core.hash.Sha384PasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA256 = "org.apache.directory.server.core.hash.Ssha256PasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA256 = "org.apache.directory.server.core.hash.Sha256PasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_CRYPT = "org.apache.directory.server.core.hash.CryptPasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SMD5 = "org.apache.directory.server.core.hash.Smd5PasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_MD5 = "org.apache.directory.server.core.hash.Md5PasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA = "org.apache.directory.server.core.hash.SshaPasswordHashingInterceptor"; //$NON-NLS-1$
    private static final String HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA = "org.apache.directory.server.core.hash.ShaPasswordHashingInterceptor"; //$NON-NLS-1$

    /** The Page ID*/
    public static final String ID = LdapLdapsServersPage.class.getName(); //$NON-NLS-1$

    /** The Page Title */
    private static final String TITLE = Messages.getString( "LdapLdapsServersPage.LdapLdapsServers" ); //$NON-NLS-1$

    // UI Controls
    /**
     * UI controls for the plain LDAP transport: the enable toggle, port,
     * bind address, thread count, and backlog size. Think of these as the
     * main channel controls on the Death Star's comms switchboard.
     */
    private Button enableLdapCheckbox;
    private Text ldapPortText;
    private Text ldapAddressText;
    private Text ldapNbThreadsText;
    private Text ldapBackLogSizeText;
    
    /** UI controls for the encrypted LDAPS transport — enable toggle, port, address, threads, backlog. */
    private Button enableLdapsCheckbox;
    private Text ldapsPortText;
    private Text ldapsAddressText;
    private Text ldapsNbThreadsText;
    private Text ldapsBackLogSizeText;
    private Button needClientAuthCheckbox;
    private Button wantClientAuthCheckbox;
    private boolean wantClientAuthStatus;
    
    /** The CiphersSuite controls */
    private CheckboxTableViewer ciphersSuiteTableViewer;
    
    /** The EnabledProtocols controls */
    private Button sslv3Checkbox;
    private Button tlsv1_0Checkbox;
    private Button tlsv1_1Checkbox;
    private Button tlsv1_2Checkbox;
    
    /** LDAP limits */
    private Text maxTimeLimitText;
    private Text maxSizeLimitText;
    private Text maxPduSizeText;
    
    /** The supported authentication controls */
    private Button authMechSimpleCheckbox;
    private Button authMechCramMd5Checkbox;
    private Button authMechDigestMd5Checkbox;
    private Button authMechGssapiCheckbox;
    private Button authMechNtlmCheckbox;
    private Text authMechNtlmText;
    private Button authMechGssSpnegoCheckbox;
    private Text authMechGssSpnegoText;

    /** The SASL controls */
    private Text saslHostText;
    private Text saslPrincipalText;
    private Text saslSearchBaseDnText;
    private TableViewer saslRealmsTableViewer;
    private Button addSaslRealmsButton;
    private Button editSaslRealmsButton;
    private Button deleteSaslRealmsButton;
    
    /** The Advanced controls */
    private Button enableTlsCheckbox;
    private Button enableServerSidePasswordHashingCheckbox;
    private ComboViewer hashingMethodComboViewer;
    private Text keystoreFileText;
    private Button keystoreFileBrowseButton;
    private Text keystorePasswordText;
    private Button showPasswordCheckbox;
    private Text replicationPingerSleepText;
    private Text diskSynchronizationDelayText;

    // UI Controls Listeners
    /**
     * Fires when the "Enable LDAP Server" checkbox is toggled.
     * Like flipping the main power switch on a Death Star comm array —
     * when enabled, all the dependent controls (port, address, thread
     * count, backlog) light up and become editable; when disabled, they
     * go dark and the transport bean is updated accordingly.
     */
    private SelectionAdapter enableLdapCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            boolean enabled = enableLdapCheckbox.getSelection();
            
            getLdapServerTransportBean().setEnabled( enabled );
            setEnabled( ldapPortText, enabled );
            setEnabled( ldapAddressText, enabled );
            setEnabled( ldapNbThreadsText, enabled );
            setEnabled( ldapBackLogSizeText, enabled );
        }
    };
    
    
    /**
     * Fires whenever the LDAP port field changes.
     * Parses the text as an integer and writes it straight to the
     * transport bean — like a gunner keying in a new targeting coordinate.
     */
    private ModifyListener ldapPortTextListener = event -> 
        {
            try
            {
                int port = Integer.parseInt( ldapPortText.getText() );
                
                getLdapServerTransportBean().setSystemPort( port );
            }
            catch ( NumberFormatException nfe1 )
            {
                System.out.println( "Wrong LDAP TCP Port : it must be an integer" );
            }
        };

    
    /**
     * Fires whenever the LDAP bind address field changes.
     * Writes the new address string straight to the transport bean —
     * like updating which frequency the comms array is broadcasting on.
     */
    private ModifyListener ldapAddressTextListener = event ->
        getLdapServerTransportBean().setTransportAddress( ldapAddressText.getText() );

    
    /**
     * Fires whenever the LDAP thread count field changes.
     * Parses the value and updates the transport bean — like assigning
     * the number of crew members to a particular comms station.
     */
    private ModifyListener ldapNbThreadsTextListener = event ->
        {
            try
            {
                int nbThreads = Integer.parseInt( ldapNbThreadsText.getText() );
                
                getLdapServerTransportBean().setTransportNbThreads( nbThreads );
            }
            catch ( NumberFormatException nfe2 )
            {
                System.out.println( "Wrong LDAP NbThreads : it must be an integer" );
            }
        };

    
    /**
     * Fires whenever the LDAP backlog size field changes.
     * Parses and writes the new backlog ceiling to the transport bean —
     * like telling the station's entrance how many ships can queue up.
     */
    private ModifyListener ldapBackLogSizeTextListener = event ->
        {
            try
            {
                int backLogSize = Integer.parseInt( ldapBackLogSizeText.getText() );
                
                getLdapServerTransportBean().setTransportBackLog( backLogSize );
            }
            catch ( NumberFormatException nfe3 )
            {
                System.out.println( "Wrong LDAP BackLog size : it must be an integer" );
            }
        };
    
    
    /**
     * Fires when the "Enable LDAPS Server" checkbox is toggled.
     * Like enabling the encrypted channel on the Death Star's secure
     * comms system — flipping this switch lights up the port, address,
     * thread count, and backlog controls for the SSL-encrypted transport.
     */
    private SelectionAdapter enableLdapsCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            boolean enabled = enableLdapsCheckbox.getSelection();
            
            getLdapsServerTransportBean().setEnabled( enabled );
            setEnabled( ldapsPortText, enabled );
            setEnabled( ldapsAddressText, enabled );
            setEnabled( ldapsNbThreadsText, enabled );
            setEnabled( ldapsBackLogSizeText, enabled );
        }
    };
    
    
    /**
     * Fires whenever the LDAPS port field changes.
     * Parses the text and writes the new port number to the LDAPS
     * transport bean — same idea as the LDAP port listener, just on
     * the encrypted channel.
     */
    private ModifyListener ldapsPortTextListener = event ->
        {
            try
            {
                int port = Integer.parseInt( ldapsPortText.getText() );
                
                getLdapsServerTransportBean().setSystemPort( port );
            }
            catch ( NumberFormatException nfe4 )
            {
                System.out.println( "Wrong LDAPS Port : it must be an integer" );
            }
        };
    
    
    /**
     * Fires whenever the LDAPS bind address field changes.
     * Writes the new address straight to the LDAPS transport bean —
     * the encrypted comms equivalent of the plain LDAP address listener.
     */
    private ModifyListener ldapsAddressTextListener = event ->
        getLdapsServerTransportBean().setTransportAddress( ldapsAddressText.getText() );

    
    /**
     * Fires whenever the LDAPS thread count field changes.
     * Parses the value and updates the LDAPS transport bean — like
     * staffing the secure comm station with the right number of crew.
     */
    private ModifyListener ldapsNbThreadsTextListener = event ->
        {
            try
            {
                int nbThreads = Integer.parseInt( ldapsNbThreadsText.getText() );
                
                getLdapsServerTransportBean().setTransportNbThreads( nbThreads );
            }
            catch ( NumberFormatException nfe5 )
            {
                System.out.println( "Wrong LDAPS NbThreads : it must be an integer" );
            }
        };

    
    /**
     * Fires whenever the LDAPS backlog size field changes.
     * Parses and writes the new backlog ceiling to the LDAPS transport
     * bean — how many encrypted connections can queue up waiting to be
     * accepted.
     */
    private ModifyListener ldapsBackLogSizeTextListener = event ->
        {
            try
            {
                int backLogSize = Integer.parseInt( ldapsBackLogSizeText.getText() );
                
                getLdapsServerTransportBean().setTransportBackLog( backLogSize );
            }
            catch ( NumberFormatException nfe6 )
            {
                System.out.println( "Wrong LDAPS BackLog size : it must be an integer" );
            }
        };
    
    
    /**
     * Fires when the "Require Client Auth" checkbox is toggled.
     * This is the strict door guard — when NeedClientAuth is on, the server
     * demands a certificate from every caller. We also force-check the
     * WantClientAuth box (requiring implies wanting) and disable it so
     * the user can't contradict themselves.
     */
    private SelectionAdapter needClientAuthListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            boolean enabled = needClientAuthCheckbox.getSelection();

            // Inject the flag in the config
            TransportBean ldapTransport = getLdapServerTransportBean();
            
            ldapTransport.setWantClientAuth( enabled );
            
            TransportBean ldapsTransport = getLdapsServerTransportBean();
            
            ldapsTransport.setWantClientAuth( enabled );

            // Turn on/off the NeedClientAuth
            if ( enabled )
            {
                wantClientAuthCheckbox.setSelection( enabled );
            }
            else
            {
                // restore the previous value
                wantClientAuthCheckbox.setSelection( wantClientAuthStatus );
            }
            
            // And disable it or enable it
            setEnabled( wantClientAuthCheckbox, !enabled );
            
            // last, 
        }
    };

    
    /**
     * Fires when the "Request Client Auth" checkbox is toggled.
     * This is the polite door guard — the server asks callers for a
     * certificate but doesn't reject them if they don't have one.
     * We update both transports and track the last chosen state so
     * NeedClientAuth can restore it when it's toggled off.
     */
    private SelectionAdapter wantClientAuthListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            boolean enabled = wantClientAuthCheckbox.getSelection();

            // Inject the flag in the config - for all the transports, as
            // it may be for SSL or startTLS - 
            TransportBean ldapTransport =  getLdapServerTransportBean();
            
            ldapTransport.setWantClientAuth( enabled );

            TransportBean ldapsTransport =  getLdapsServerTransportBean();
            
            ldapsTransport.setWantClientAuth( enabled );

            // Keep a track of the WantClientAuth flag
            wantClientAuthStatus = enabled;
        }
    };

    
    /**
     * Fires whenever the SASL host name field changes.
     * Writes the new hostname straight to the LDAP server bean —
     * like updating the call sign of the station on the secure network.
     */
    private ModifyListener saslHostTextListener = event ->
        getLdapServerBean().setLdapServerSaslHost( saslHostText.getText() );
    
    
    /**
     * Fires whenever the SASL principal field changes.
     * The principal is the server's Kerberos identity — think of it as
     * the Death Star's official ID badge on the Imperial network.
     */
    private ModifyListener saslPrincipalTextListener = event ->
        getLdapServerBean().setLdapServerSaslPrincipal( saslPrincipalText.getText() );

    
    /**
     * Fires whenever the SASL search base DN field changes.
     * Parses the text as a DN and updates the server bean — this is the
     * subtree the server searches when looking up SASL user accounts,
     * like pointing the scanner at a particular deck of the station.
     */
    private ModifyListener saslSearchBaseDnTextListener = event ->
        {
            String searchBaseDnValue = saslSearchBaseDnText.getText();

            try
            {
                Dn searchBaseDn = new Dn( searchBaseDnValue );
                getLdapServerBean().setSearchBaseDn( searchBaseDn );
            }
            catch ( LdapInvalidDnException e1 )
            {
                // Stay silent
            }
        };
    
    
    /**
     * Fires when the SASL realms table selection changes.
     * Enables or disables the Edit and Delete buttons depending on
     * whether something is selected — you can't edit what you haven't
     * chosen, like trying to target a ship that's not on the scanner.
     */
    private ISelectionChangedListener saslRealmsTableViewerSelectionChangedListener = event ->
        {
            StructuredSelection selection = ( StructuredSelection ) saslRealmsTableViewer.getSelection();

            editSaslRealmsButton.setEnabled( !selection.isEmpty() );
            deleteSaslRealmsButton.setEnabled( !selection.isEmpty() );
        };
    
    
    /**
     * Fires on a double-click in the SASL realms table.
     * A convenient shortcut — double-clicking a realm is the same as
     * selecting it and hitting Edit, like opening a dossier by tapping
     * on a name on the tactical display.
     */
    private IDoubleClickListener saslRealmsTableViewerDoubleClickListener = event -> editSaslRealmsAction();
    

    /**
     * Fires when the "Add" button next to the SASL realms table is clicked.
     * Pops up an input dialog, takes the new realm name, and adds it to
     * the server bean — like enrolling a new sector in the Imperial
     * authentication registry.
     */
    private SelectionListener addSaslRealmsButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            InputDialog dialog = new InputDialog( editSaslRealmsButton.getShell(),
                Messages.getString( "LdapLdapsServersPage.Add" ), //$NON-NLS-1$
                Messages.getString( "LdapLdapsServersPage.SaslRealms" ), //$NON-NLS-1$
                null, null );

            if ( dialog.open() == InputDialog.OK )
            {
                String newSaslRealms = dialog.getValue();

                getLdapServerBean().addSaslRealms( newSaslRealms );

                saslRealmsTableViewer.refresh();
                saslRealmsTableViewer.setSelection( new StructuredSelection( newSaslRealms ) );

                setEditorDirty();
            }
        }
    };
    
    
    /**
     * Fires when the "Edit" button next to the SASL realms table is clicked.
     * Delegates straight to {@code editSaslRealmsAction()} — we keep the
     * button listener and the double-click listener pointing at the same
     * action so behaviour is consistent whichever way the user triggers it.
     */
    private SelectionListener editSaslRealmsButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            editSaslRealmsAction();
        }
    };
    
    
    /**
     * Fires when the "Delete" button next to the SASL realms table is clicked.
     * Removes the currently selected realm from the server bean and refreshes
     * the table — like striking a sector off the Imperial access list.
     */
    private SelectionListener deleteSaslRealmsButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            String selectedSaslRealms = getSelectedSaslRealms();

            if ( selectedSaslRealms != null )
            {
                getLdapServerBean().getLdapServerSaslRealms().remove( selectedSaslRealms );
                saslRealmsTableViewer.refresh();

                setEditorDirty();
            }
        }
    };

    
    /**
     * Fires when the "Simple" auth mechanism checkbox is toggled.
     * Simple auth is the basic username/password handshake — no Kerberos,
     * no tokens, just credentials. We enable or disable it in the
     * SASL mechanism handler list accordingly.
     */
    private SelectionAdapter authMechSimpleCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setEnableSupportedAuthenticationMechanism( SASL_MECHANISMS_SIMPLE, authMechSimpleCheckbox.getSelection() );
        }
    };
    
    
    /**
     * Fires when the GSSAPI auth mechanism checkbox is toggled.
     * GSSAPI/Kerberos is the high-security equivalent of presenting an
     * Imperial code cylinder — we enable or disable it in the handler list.
     */
    private SelectionAdapter authMechGssapiCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setEnableSupportedAuthenticationMechanism( SupportedSaslMechanisms.GSSAPI,
                authMechGssapiCheckbox.getSelection() );
        }
    };

    
    /**
     * Fires when the CRAM-MD5 auth mechanism checkbox is toggled.
     * CRAM-MD5 is a challenge-response scheme — the server sends a
     * nonce and the client proves it knows the password without
     * transmitting it. We enable or disable its handler accordingly.
     */
    private SelectionAdapter authMechCramMd5CheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setEnableSupportedAuthenticationMechanism( SupportedSaslMechanisms.CRAM_MD5,
                authMechCramMd5Checkbox.getSelection() );
        }
    };
    
    
    /**
     * Fires when the DIGEST-MD5 auth mechanism checkbox is toggled.
     * Similar to CRAM-MD5 but with a richer challenge — think of it as
     * the more elaborate code-word exchange at the blast door.
     */
    private SelectionAdapter authMechDigestMd5CheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setEnableSupportedAuthenticationMechanism( SupportedSaslMechanisms.DIGEST_MD5,
                authMechDigestMd5Checkbox.getSelection() );
        }
    };
    
    
    /**
     * Fires when the GSS-SPNEGO auth mechanism checkbox is toggled.
     * SPNEGO lets the client and server negotiate which GSS mechanism to
     * use — like the Empire and Rebels agreeing on a mutual language
     * before exchanging credentials. We also toggle the provider text
     * field alongside the checkbox.
     */
    private SelectionAdapter authMechGssSpnegoCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setEnableSupportedAuthenticationMechanism( SupportedSaslMechanisms.GSS_SPNEGO,
                authMechGssSpnegoCheckbox.getSelection() );
            setEnabled( authMechGssSpnegoText, authMechGssSpnegoCheckbox.getSelection() );
        }
    };

    
    /**
     * Fires when the GSS-SPNEGO provider class name field changes.
     * Updates the NTLM mechanism provider on the GSS-SPNEGO handler —
     * this is the class that actually handles the SPNEGO token exchange.
     */
    private ModifyListener authMechGssSpnegoTextListener = event ->
        setNtlmMechProviderSupportedAuthenticationMechanism( SupportedSaslMechanisms.GSS_SPNEGO, authMechGssSpnegoText.getText() );
    
    
    /**
     * Fires when the NTLM auth mechanism checkbox is toggled.
     * NTLM is the Windows challenge-response protocol — an old trooper
     * who's still on duty. We enable or disable the handler and toggle
     * the provider field alongside the checkbox.
     */
    private SelectionAdapter authMechNtlmCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setEnableSupportedAuthenticationMechanism( SupportedSaslMechanisms.NTLM,
                authMechNtlmCheckbox.getSelection() );
            setEnabled( authMechNtlmText, authMechNtlmCheckbox.getSelection() );
        }
    };
    
    
    /**
     * Fires when the NTLM provider class name field changes.
     * Updates the NTLM mechanism provider on the NTLM SASL handler —
     * the class name of the third-party library doing the heavy lifting.
     */
    private ModifyListener authMechNtlmTextListener = event ->
        setNtlmMechProviderSupportedAuthenticationMechanism( SupportedSaslMechanisms.NTLM, authMechNtlmText.getText() );
    
    
    /**
     * Fires when the max time limit field changes.
     * The time limit caps how long the server will spend answering a
     * single search — like the Death Star's firing window; once time's
     * up, the operation gets cut off.
     */
    private ModifyListener maxTimeLimitTextListener = event ->
        getLdapServerBean().setLdapServerMaxTimeLimit( Integer.parseInt( maxTimeLimitText.getText() ) );
    
    
    /**
     * Fires when the max size limit field changes.
     * Caps how many entries the server will return for a single search —
     * like a maximum transmission packet size on the Imperial data network.
     */
    private ModifyListener maxSizeLimitTextListener = event ->
        getLdapServerBean().setLdapServerMaxSizeLimit( Integer.parseInt( maxSizeLimitText.getText() ) );
    
    
    /**
     * Fires when the max PDU size field changes.
     * Sets the maximum size of a single protocol data unit the server
     * will accept — the LDAP equivalent of a maximum cargo container
     * size coming through the docking bay.
     */
    private ModifyListener maxPduSizeTextListener = event ->
        getLdapServerBean().setMaxPDUSize( Integer.parseInt( maxPduSizeText.getText() ) );
    
    
    /**
     * Fires when the "Enable TLS" (StartTLS) checkbox is toggled.
     * StartTLS lets clients upgrade a plain LDAP connection to encrypted
     * in mid-flight — like a cloaking device that gets switched on after
     * the ship already left port. We enable or disable the TLS extended
     * op handler in the server bean accordingly.
     */
    private SelectionAdapter enableTlsCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setEnableTls( enableTlsCheckbox.getSelection() );
        }


        /**
         * Enables or disables the StartTLS extended operation handler.
         *
         * @param enabled  {@code true} to switch StartTLS on, {@code false} to switch it off
         */
        private void setEnableTls( boolean enabled )
        {
            getTlsExtendedOpHandlerBean().setEnabled( enabled );
        }
    };
    
    
    /**
     * Fires when the "Enable Server-side Password Hashing" checkbox is toggled.
     * When on, the server intercepts incoming passwords and hashes them before
     * storing — like the Death Star automatically encrypting transmissions
     * before they hit the data vault. We add or remove the hashing interceptor
     * from the directory service's interceptor chain.
     */
    private SelectionAdapter enableServerSidePasswordHashingCheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            if ( enableServerSidePasswordHashingCheckbox.getSelection() )
            {
                enableHashingPasswordInterceptor();
            }
            else
            {
                disableHashingPasswordInterceptor();
            }

            setEnabled( hashingMethodComboViewer.getCombo(), enableServerSidePasswordHashingCheckbox.getSelection() );
        }


        /**
         * Activates the password-hashing interceptor, creating it if it doesn't
         * exist yet.
         */
        private void enableHashingPasswordInterceptor()
        {
            // Getting the hashing password interceptor
            InterceptorBean hashingPasswordInterceptor = getHashingPasswordInterceptor();

            // If we didn't found one, we need to create it
            if ( hashingPasswordInterceptor == null )
            {
                // Creating a new hashing password interceptor
                hashingPasswordInterceptor = createHashingPasswordInterceptor();
            }

            // Enabling the interceptor
            hashingPasswordInterceptor.setEnabled( true );
        }


        /**
         * Deactivates the password-hashing interceptor if it exists.
         * If there's no interceptor, we do nothing — can't disable what
         * was never installed.
         */
        private void disableHashingPasswordInterceptor()
        {
            // Getting the hashing password interceptor
            InterceptorBean hashingPasswordInterceptor = getHashingPasswordInterceptor();

            if ( hashingPasswordInterceptor != null )
            {
                // Disabling the interceptor
                hashingPasswordInterceptor.setEnabled( false );
            }
        }
    };
    
    
    /**
     * Fires when the hashing method combo selection changes.
     * Updates the interceptor's class name to match the newly chosen
     * algorithm — like switching the Death Star's encryption module from
     * SHA to SSHA512 with a single selector turn.
     */
    private ISelectionChangedListener hashingMethodComboViewerListener = event -> updateHashingMethod();
    
    
    /**
     * Fires whenever the keystore file path field changes.
     * Writes the path to the server bean, or clears it if the field is
     * empty — like telling the security station where the code cylinders
     * are stored, or confirming they've been removed.
     */
    private ModifyListener keystoreFileTextListener = event ->
        {
            String keystoreFile = keystoreFileText.getText();

            if ( ( keystoreFile == null ) || ( keystoreFile.length() == 0 ) )
            {
                getLdapServerBean().setLdapServerKeystoreFile( null );
            }
            else
            {
                getLdapServerBean().setLdapServerKeystoreFile( keystoreFile );
            }
        };
    
    
    /**
     * Fires when the "Browse..." button next to the keystore path is clicked.
     * Opens a file chooser dialog pre-seeded with the current path so the
     * user can navigate easily — like guiding a ship into dock rather
     * than making it find its own berth from scratch.
     */
    private SelectionListener keystoreFileBrowseButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            FileDialog fileDialog = new FileDialog( keystoreFileBrowseButton.getShell(), SWT.OPEN );

            File file = new File( keystoreFileText.getText() );
            
            if ( file.isFile() )
            {
                fileDialog.setFilterPath( file.getParent() );
                fileDialog.setFileName( file.getName() );
            }
            else if ( file.isDirectory() )
            {
                fileDialog.setFilterPath( file.getPath() );
            }
            else
            {
                fileDialog.setFilterPath( null );
            }

            String returnedFileName = fileDialog.open();
            
            if ( returnedFileName != null )
            {
                keystoreFileText.setText( returnedFileName );
                setEditorDirty();
            }
        }
    };
    
    
    /**
     * Fires whenever the keystore password field changes.
     * Writes the password to the server bean, or clears it if the field
     * is empty — think of it as locking or unlocking the vault that holds
     * the server's TLS certificates.
     */
    private ModifyListener keystorePasswordTextListener = event ->
        {
            String keystorePassword = keystorePasswordText.getText();

            if ( ( keystorePassword == null ) || ( keystorePassword.length() == 0 ) )
            {
                getLdapServerBean().setLdapServerCertificatePassword( null );
            }
            else
            {
                getLdapServerBean().setLdapServerCertificatePassword( keystorePassword );
            }
        };
    
    
    /**
     * Fires when the "Show Password" checkbox is toggled.
     * Switches the password field between masked (bullet character) and
     * plain-text display — like flipping between encoded and decoded
     * transmissions on the comms console.
     */
    private SelectionListener showPasswordCheckboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            if ( showPasswordCheckbox.getSelection() )
            {
                keystorePasswordText.setEchoChar( '\0' );
            }
            else
            {
                keystorePasswordText.setEchoChar( '\u2022' );
            }
        }
    };

    
    /**
     * Fires when a cipher suite checkbox is checked or unchecked.
     * Maintains the list of enabled ciphers in the transport bean,
     * refusing to let the last cipher be deselected — you need at least
     * one working encryption algorithm, or the shield generator goes dark.
     */
    private ICheckStateListener ciphersSuiteTableViewerListener = event ->
        {
            TransportBean transport = getLdapTransportBean( TRANSPORT_ID_LDAP );
            
            if ( transport == null )
            {
                transport = getLdapTransportBean( TRANSPORT_ID_LDAPS );
            }
            
            if ( transport == null )
            {
                // TODO : the list should be disabled
                return;
            }
            
            // Checking if the last cipher is being unchecked
            if ( transport.getEnabledCiphers() == null )
            {
                // Ok, we don't have any selected cipher, which means all of them are selected
                transport.setEnabledCiphers( SupportedCipher.supportedCipherNamesJava8 );
            }
            
            if ( ( transport.getEnabledCiphers().size() == 1 ) && !event.getChecked() )
            {
                // Displaying an error to the user
                CommonUIUtils.openErrorDialog( Messages
                    .getString( "LdapLdapsServersPage.AtLeastOneCipherMustBeSelected" ) );

                // Reverting the current checked state
                ciphersSuiteTableViewer.setChecked( event.getElement(), !event.getChecked() );

                // Exiting
                return;
            }

            // Setting the editor as dirty
            setEditorDirty();

            // Clearing previous cipher suite
            transport.getEnabledCiphers().clear();

            // Getting all selected encryption types
            Object[] selectedCipherObjects = ciphersSuiteTableViewer.getCheckedElements();

            // Adding each selected cipher
            for ( Object cipher : selectedCipherObjects )
            {
                if ( cipher instanceof SupportedCipher )
                {
                    SupportedCipher supportedCipher = ( SupportedCipher ) cipher;

                    transport.getEnabledCiphers().add( supportedCipher.getCipher() );
                }
            }
        };
    
    
    /**
     * Fires when the SSLv3 protocol checkbox is toggled.
     * Adds or removes "SSLv3" from the transport's enabled-protocols list.
     * Note: SSLv3 is considered legacy — like bringing a Clone Wars-era
     * fighter to a modern engagement.
     */
    private SelectionAdapter sslv3CheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setProtocol( sslv3Checkbox.getSelection(), SSL_V3 );
        }
    };
    
    
    /**
     * Fires when the TLSv1.0 protocol checkbox is toggled.
     * Adds or removes "TLSv1" from the transport's enabled-protocols list —
     * the first-generation TLS trooper, still useful but showing its age.
     */
    private SelectionAdapter tlsv1_0CheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setProtocol( tlsv1_0Checkbox.getSelection(), TLS_V1_0 );
        }
    };
    
    
    /**
     * Fires when the TLSv1.1 protocol checkbox is toggled.
     * Adds or removes "TLSv1.1" from the transport's enabled-protocols list —
     * the mid-generation trooper, more capable than v1.0 but superseded.
     */
    private SelectionAdapter tlsv1_1CheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setProtocol( tlsv1_1Checkbox.getSelection(), TLS_V1_1 );
        }
    };
    
    
    /**
     * Fires when the TLSv1.2 protocol checkbox is toggled.
     * Adds or removes "TLSv1.2" from the transport's enabled-protocols list —
     * the recommended modern trooper for most deployments.
     */
    private SelectionAdapter tlsv1_2CheckboxListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            setProtocol( tlsv1_2Checkbox.getSelection(), TLS_V1_2 );
        }
    };

    
    /**
     * Fires when the replication pinger sleep field changes.
     * Sets how often (in seconds) the replication engine checks for
     * updates from its source — the heartbeat signal that keeps the
     * Death Star's replica nodes in sync with headquarters.
     */
    private ModifyListener replicationPingerSleepTextListener = event ->
        getLdapServerBean().setReplPingerSleep( Integer.parseInt( replicationPingerSleepText.getText() ) );
    
    
    /**
     * Fires when the disk synchronization delay field changes.
     * Sets how often (in milliseconds) the directory service flushes
     * in-memory changes to disk — like choosing how frequently the
     * Death Star's main log is committed to the permanent data vault.
     */
    private ModifyListener diskSynchronizationDelayTextListener = event ->
        getDirectoryServiceBean().setDsSyncPeriodMillis( Long.parseLong( diskSynchronizationDelayText.getText() ) );


    // ── Opening The Transport Control Room For Business ───────────────────────
    // A fresh-faced comms officer steps onto the Death Star transport deck for
    // the first time, registers with the central command, and waits for the
    // main editor to tell them what config to display.
    // We pass our ID and title to the parent FormPage, and the editor wires
    // us into its tab strip automatically.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new LdapLdapsServersPage and registers it with the given editor.
     * The page won't render any widgets until Eclipse calls
     * {@link #createFormContent}.
     *
     * @param editor  the ServerConfigurationEditor that owns this page
     */
    public LdapLdapsServersPage( ServerConfigurationEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── Laying Out The Control Room Floor Plan ────────────────────────────────
    // The Death Star's architect marks out two columns of workstations on the
    // transport deck: left side for LDAP/limits/SSL settings, right side for
    // auth mechanisms and SASL settings.
    // We build a TableWrapLayout with two equal columns and populate both
    // sides with their respective sections, then trigger the first UI refresh.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the page's two-column layout and all its sections.
     * Called by Eclipse when the tab is first made visible. We create the
     * left composite (LDAP transport, limits, SSL keystore, advanced SSL)
     * and the right composite (auth mechanisms, SASL settings, advanced),
     * then call {@link #refreshUI()} to populate everything from the config bean.
     *
     * @param parent   the form body composite Eclipse provides
     * @param toolkit  the form toolkit used to create styled widgets
     */
    protected void createFormContent( Composite parent, FormToolkit toolkit )
    {
        TableWrapLayout twl = new TableWrapLayout();
        twl.numColumns = 2;
        twl.makeColumnsEqualWidth = true;
        parent.setLayout( twl );

        // Left Composite
        Composite leftComposite = toolkit.createComposite( parent );
        leftComposite.setLayout( new GridLayout() );
        TableWrapData leftCompositeTableWrapData = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        leftCompositeTableWrapData.grabHorizontal = true;
        leftComposite.setLayoutData( leftCompositeTableWrapData );

        // Right Composite
        Composite rightComposite = toolkit.createComposite( parent );
        rightComposite.setLayout( new GridLayout() );
        TableWrapData rightCompositeTableWrapData = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        rightCompositeTableWrapData.grabHorizontal = true;
        rightComposite.setLayoutData( rightCompositeTableWrapData );

        // Creating the sections
        createLdapServerSection( toolkit, leftComposite );
        createLimitsSection( toolkit, leftComposite );
        createSslStartTlsKeystoreSection( toolkit, leftComposite );
        createSslAdvancedSettingsSection( toolkit, leftComposite );
        createSupportedAuthenticationMechanismsSection( toolkit, rightComposite );
        createSaslSettingsSection( toolkit, rightComposite );
        createAdvancedSection( toolkit, rightComposite );

        // Refreshing the UI
        refreshUI();
    }


    // ── Installing The Main Comms Switchboard ─────────────────────────────────
    // The chief comms engineer bolts the main LDAP and LDAPS switchboard into
    // the left-hand rack — toggle switches for enabling each channel, text
    // fields for port, address, thread count, and backlog.
    // We build the Form Section (expandable header + content area) and wire up
    // all the LDAP and LDAPS transport controls inside a 4-column grid.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "LDAP/LDAPS Servers" section with controls for enabling each
     * transport and editing its port, bind address, thread count, and backlog.
     * This section renders expanded by default since it's the most commonly
     * edited part of the page.
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the left-column composite to attach this section to
     */
    private void createLdapServerSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section, expanded
        Section section = toolkit.createSection( parent, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText( Messages.getString( "LdapLdapsServersPage.LdapLdapsServers" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout gridLayout = new GridLayout( 4, false );
        gridLayout.marginHeight = gridLayout.marginWidth = 0;
        composite.setLayout( gridLayout );
        section.setClient( composite );

        // Enable LDAP Server Checkbox
        enableLdapCheckbox = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.EnableLdapServer" ), SWT.CHECK ); //$NON-NLS-1$
        enableLdapCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, gridLayout.numColumns, 1 ) );

        // LDAP Server Port Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.Port" ) ); //$NON-NLS-1$
        ldapPortText = createPortText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, Integer.toString( DEFAULT_PORT_LDAP ) );

        // LDAP Server Address Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.Address" ) ); //$NON-NLS-1$
        ldapAddressText = createAddressText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, DEFAULT_ADDRESS );

        // LDAP Server nbThreads Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.NbThreads" ) ); //$NON-NLS-1$
        ldapNbThreadsText = createNbThreadsText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite,  Integer.toString( DEFAULT_NB_THREADS ) );

        // LDAP Server backlog Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.BackLogSize" ) ); //$NON-NLS-1$
        ldapBackLogSizeText = createBackLogSizeText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite,  Integer.toString( DEFAULT_BACKLOG_SIZE ) );

        // Enable LDAPS Server Checkbox
        enableLdapsCheckbox = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.EnableLdapsServer" ), SWT.CHECK ); //$NON-NLS-1$
        enableLdapsCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, gridLayout.numColumns, 1 ) );

        // LDAPS Server Port Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.Port" ) ); //$NON-NLS-1$
        ldapsPortText = createPortText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, Integer.toString( DEFAULT_PORT_LDAPS ) );

        // LDAPS Server Address Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.Address" ) ); //$NON-NLS-1$
        ldapsAddressText = createAddressText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, DEFAULT_ADDRESS );

        // LDAPS Server nbThreads Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.NbThreads" ) ); //$NON-NLS-1$
        ldapsNbThreadsText = createNbThreadsText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, Integer.toString( DEFAULT_NB_THREADS ) );

        // LDAPS Server backlog Text
        toolkit.createLabel( composite, TABULATION );
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.BackLogSize" ) ); //$NON-NLS-1$
        ldapsBackLogSizeText = createBackLogSizeText( toolkit, composite );
        createDefaultValueLabel( toolkit, composite, Integer.toString( DEFAULT_BACKLOG_SIZE ) );
    }


    // ── Setting The Station's Resource Guardrails ─────────────────────────────
    // The Death Star's systems officer sets hard limits on how long a query can
    // run, how many results it can return, and how large a single message can
    // be — preventing any one request from monopolising the comms system.
    // We build three integer text fields for max time, max size, and max PDU.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Server Limits" section with controls for max time limit,
     * max size limit, and max PDU size.
     * These three fields prevent runaway queries from overwhelming the server.
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the left-column composite to attach this section to
     */
    private void createLimitsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section, compacted
        Section section = toolkit.createSection( parent, Section.TITLE_BAR | Section.TWISTIE | Section.COMPACT );
        section.setText( Messages.getString( "LdapLdapsServersPage.Limits" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout glayout = new GridLayout( 2, false );
        composite.setLayout( glayout );
        section.setClient( composite );

        // Max. Time Limit Text
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.MaxTimeLimit" ) ); //$NON-NLS-1$
        maxTimeLimitText = BaseWidgetUtils.createIntegerText( toolkit, composite );
        maxTimeLimitText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Max. Size Limit Text
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.MaxSizeLimit" ) ); //$NON-NLS-1$
        maxSizeLimitText = BaseWidgetUtils.createIntegerText( toolkit, composite );
        maxSizeLimitText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Max. PDU Size Text
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.MaxPduSize" ) ); //$NON-NLS-1$
        maxPduSizeText = BaseWidgetUtils.createIntegerText( toolkit, composite );
        maxPduSizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Mounting The Security Vault Panel ─────────────────────────────────────
    // The security officer installs the vault panel on the left rack — a slot
    // for the keystore file path, a combination lock (password field), and a
    // "show combination" toggle for when you need to double-check it.
    // We build a 3-column grid with path text + Browse button, masked password
    // field, and a Show Password checkbox.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "SSL/Start TLS Keystore" section with controls for the
     * keystore file path, keystore password, and a show-password toggle.
     * This is where we point the server at the certificate store it needs
     * for TLS connections.
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the left-column composite to attach this section to
     */
    private void createSslStartTlsKeystoreSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section, compacted
        Section section = toolkit.createSection( parent, Section.TITLE_BAR | Section.TWISTIE | Section.COMPACT );
        section.setText( Messages.getString( "LdapLdapsServersPage.SslStartTlsKeystore" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout glayout = new GridLayout( 3, false );
        composite.setLayout( glayout );
        section.setClient( composite );

        // Keystore File Text
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.Keystore" ) ); //$NON-NLS-1$
        keystoreFileText = toolkit.createText( composite, "" ); //$NON-NLS-1$
        setGridDataWithDefaultWidth( keystoreFileText, new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        keystoreFileBrowseButton = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.Browse" ), SWT.PUSH ); //$NON-NLS-1$

        // Password Text
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.Password" ) ); //$NON-NLS-1$
        keystorePasswordText = toolkit.createText( composite, "" ); //$NON-NLS-1$
        keystorePasswordText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        keystorePasswordText.setEchoChar( '\u2022' );

        // Show Password Checkbox
        toolkit.createLabel( composite, "" ); //$NON-NLS-1$
        showPasswordCheckbox = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.ShowPassword" ), SWT.CHECK ); //$NON-NLS-1$
        showPasswordCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        showPasswordCheckbox.setSelection( false );
    }


    // ── Configuring The Encryption Hardpoint Controls ─────────────────────────
    // The weapons officer configures the station's defensive encryption — which
    // protocols are allowed, which cipher suites are armed, and whether clients
    // must present their own security credentials before getting through.
    // We build a 4-column section with client-auth checkboxes, a scrollable
    // cipher suite checklist, and protocol version toggles.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "SSL Advanced Settings" section covering client authentication
     * requirements, enabled cipher suites, and enabled TLS protocol versions.
     * We deal with: needClientAuth, wantClientAuth, enabledCiphersSuite, and
     * enabledProtocols (SSLv3, TLSv1.0, TLSv1.1, TLSv1.2).
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the left-column composite to attach this section to
     */
    private void createSslAdvancedSettingsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section, compacted
        Section section = toolkit.createSection( parent, Section.TITLE_BAR | Section.TWISTIE | Section.COMPACT );
        section.setText( Messages.getString( "LdapLdapsServersPage.SslAdvancedSettings" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout glayout = new GridLayout( 4, false );
        composite.setLayout( glayout );
        section.setClient( composite );

        // Enable LDAPS needClientAuth Checkbox
        needClientAuthCheckbox = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.NeedClientAuth" ), SWT.CHECK ); //$NON-NLS-1$
        needClientAuthCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 1, 1 ) );

        // Enable LDAPS wantClientAuth Checkbox. As the WantClientAuth is dependent on
        // the NeedClientAuth, we move it one column to the right
        toolkit.createLabel( composite, TABULATION );
        wantClientAuthCheckbox = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.WantClientAuth" ), SWT.CHECK ); //$NON-NLS-1$
        wantClientAuthCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Ciphers Suite label 
        Label ciphersLabel = toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.CiphersSuite" ), SWT.WRAP  ); //$NON-NLS-1$
        setBold( ciphersLabel );
        ciphersLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, glayout.numColumns, 1 ) );

        // Ciphers Suites Table Viewer
        ciphersSuiteTableViewer = new CheckboxTableViewer( new Table( composite, SWT.BORDER | SWT.CHECK ) );
        ciphersSuiteTableViewer.setContentProvider( new ArrayContentProvider() );
        ciphersSuiteTableViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object cipher )
            {
                if ( cipher instanceof SupportedCipher )
                {
                    SupportedCipher supportedCipher = ( SupportedCipher ) cipher;

                    return supportedCipher.getCipher();
                }

                return super.getText( cipher );
            }
        } );
        
        List<SupportedCipher> supportedCiphers = new ArrayList<>();
        
        for ( SupportedCipher supportedCipher : SupportedCipher.SUPPORTED_CIPHERS )
        {
            if ( supportedCipher.isJava8Implemented() )
            {
                supportedCiphers.add( supportedCipher );
            }
        }
        
        ciphersSuiteTableViewer.setInput( supportedCiphers );
        GridData ciphersSuiteTableViewerGridData = new GridData( SWT.FILL, SWT.NONE, true, false, glayout.numColumns, 5 );
        ciphersSuiteTableViewerGridData.heightHint = 60;
        ciphersSuiteTableViewer.getControl().setLayoutData( ciphersSuiteTableViewerGridData );

        // Enabled Protocols label 
        Label protocolsLabel = toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.EnabledProtocols" ), SWT.WRAP  ); //$NON-NLS-1$
        setBold( protocolsLabel );
        protocolsLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, glayout.numColumns, 1 ) );

        // Enabled Protocols
        // SSL V3
        sslv3Checkbox = toolkit.createButton( composite, SSL_V3, SWT.CHECK ); //$NON-NLS-1$
        sslv3Checkbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // TLS 1.0
        tlsv1_0Checkbox = toolkit.createButton( composite, TLS_V1_0, SWT.CHECK ); //$NON-NLS-1$
        tlsv1_0Checkbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // TLS 1.1
        tlsv1_1Checkbox = toolkit.createButton( composite, TLS_V1_1, SWT.CHECK ); //$NON-NLS-1$
        tlsv1_1Checkbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        // TLS 1.2
        tlsv1_2Checkbox = toolkit.createButton( composite, TLS_V1_2, SWT.CHECK ); //$NON-NLS-1$
        tlsv1_2Checkbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    }


    // ── Installing The Miscellaneous Systems Panel ────────────────────────────
    // The chief engineer's panel in the corner holds the controls nobody wants
    // to explain to visitors — StartTLS toggle, password-hashing intercept
    // switch, algorithm selector, replication heartbeat timer, and disk flush
    // delay. Important knobs, just not the flashy ones.
    // We build a 2-column section covering all these advanced settings.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Advanced" section covering StartTLS, server-side password
     * hashing (with algorithm selector), replication pinger sleep interval,
     * and disk synchronization delay.
     * These settings affect server behaviour at a deeper level than the
     * transport config above.
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the right-column composite to attach this section to
     */
    private void createAdvancedSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR | Section.TWISTIE | Section.COMPACT );
        section.setText( Messages.getString( "LdapLdapsServersPage.Advanced" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout glayout = new GridLayout( 2, false );
        composite.setLayout( glayout );
        section.setClient( composite );

        // Enable TLS Checkbox
        enableTlsCheckbox = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.EnableTls" ), SWT.CHECK ); //$NON-NLS-1$
        enableTlsCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Enable Server-side Password Hashing Checkbox
        enableServerSidePasswordHashingCheckbox = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.EnableServerSidePasswordHashing" ), //$NON-NLS-1$
            SWT.CHECK );
        enableServerSidePasswordHashingCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Server-side Password Hashing Composite 
        Composite hashingMethodComposite = toolkit.createComposite( composite );
        hashingMethodComposite.setLayout( new GridLayout( 3, false ) );
        hashingMethodComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Server-side Password Hashing Combo
        toolkit.createLabel( hashingMethodComposite, Messages.getString( "LdapLdapsServersPage.HashingMethod" ) ); //$NON-NLS-1$
        Combo hashingMethodCombo = new Combo( hashingMethodComposite, SWT.READ_ONLY | SWT.SINGLE );
        hashingMethodCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        toolkit.adapt( hashingMethodCombo );
        hashingMethodComboViewer = new ComboViewer( hashingMethodCombo );
        hashingMethodComboViewer.setContentProvider( new ArrayContentProvider() );
        hashingMethodComboViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof LdapSecurityConstants )
                {
                    LdapSecurityConstants hashingMethod = ( LdapSecurityConstants ) element;

                    return hashingMethod.getName();
                }

                return super.getText( element );
            }
        } );
        
        Object[] hashingMethods = new Object[]
            {
                LdapSecurityConstants.HASH_METHOD_SHA,
                LdapSecurityConstants.HASH_METHOD_SSHA,
                LdapSecurityConstants.HASH_METHOD_MD5,
                LdapSecurityConstants.HASH_METHOD_SMD5,
                LdapSecurityConstants.HASH_METHOD_CRYPT,
                LdapSecurityConstants.HASH_METHOD_SHA256,
                LdapSecurityConstants.HASH_METHOD_SSHA256,
                LdapSecurityConstants.HASH_METHOD_SHA384,
                LdapSecurityConstants.HASH_METHOD_SSHA384,
                LdapSecurityConstants.HASH_METHOD_SHA512,
                LdapSecurityConstants.HASH_METHOD_SSHA512,
                LdapSecurityConstants.HASH_METHOD_PKCS5S2
        };
        
        hashingMethodComboViewer.setInput( hashingMethods );
        setSelection( hashingMethodComboViewer, LdapSecurityConstants.HASH_METHOD_SSHA );
        toolkit.createLabel( hashingMethodComposite, "   " ); //$NON-NLS-1$
        Label defaultLabel = createDefaultValueLabel( toolkit, hashingMethodComposite, "SSHA" ); //$NON-NLS-1$
        defaultLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Replication Pinger Sleep
        toolkit.createLabel( composite, "Replication Pinger Sleep (sec):" );
        replicationPingerSleepText = BaseWidgetUtils.createIntegerText( toolkit, composite );
        replicationPingerSleepText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Disk Synchronization Delay
        toolkit.createLabel( composite, "Disk Synchronization Delay (ms):" );
        diskSynchronizationDelayText = BaseWidgetUtils.createIntegerText( toolkit, composite );
        diskSynchronizationDelayText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Registering The Alliance Identification Codes ─────────────────────────
    // The Death Star's security office decides which ID protocols are acceptable
    // at the gate — simple passcode, Kerberos token, NTLM badge, or something
    // more exotic — and each mechanism gets its own checkbox and optional
    // provider class field.
    // We build a 2-column checklist section for Simple, GSSAPI, CRAM-MD5,
    // DIGEST-MD5, NTLM, and GSS-SPNEGO, with text fields for NTLM/SPNEGO.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Supported Authentication Mechanisms" section.
     * Shows a checkbox for each SASL mechanism the server can support:
     * Simple, GSSAPI, CRAM-MD5, DIGEST-MD5, NTLM, and GSS-SPNEGO.
     * NTLM and SPNEGO also expose a provider class name field since they
     * need a third-party library to do the actual work.
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the right-column composite to attach this section to
     */
    private void createSupportedAuthenticationMechanismsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.setText( Messages.getString( "LdapLdapsServersPage.SupportedAuthenticationMechanisms" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        toolkit.paintBordersFor( composite );
        composite.setLayout( new GridLayout( 2, true ) );
        section.setClient( composite );

        // Simple Checkbox
        authMechSimpleCheckbox = toolkit.createButton( composite, "Simple", SWT.CHECK ); //$NON-NLS-1$
        authMechSimpleCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // GSSAPI Checkbox
        authMechGssapiCheckbox = toolkit.createButton( composite, "GSSAPI", SWT.CHECK ); //$NON-NLS-1$
        authMechGssapiCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // CRAM-MD5 Checkbox
        authMechCramMd5Checkbox = toolkit.createButton( composite, "CRAM-MD5", SWT.CHECK ); //$NON-NLS-1$
        authMechCramMd5Checkbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // DIGEST-MD5 Checkbox
        authMechDigestMd5Checkbox = toolkit.createButton( composite, "DIGEST-MD5", SWT.CHECK ); //$NON-NLS-1$
        authMechDigestMd5Checkbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // NTLM Checkbox and Text
        authMechNtlmCheckbox = toolkit.createButton( composite, "NTLM", SWT.CHECK ); //$NON-NLS-1$
        authMechNtlmCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        toolkit.createLabel( composite, "" ); //$NON-NLS-1$
        Composite authMechNtlmComposite = toolkit.createComposite( composite );
        authMechNtlmComposite.setLayout( new GridLayout( 3, false ) );
        toolkit.createLabel( authMechNtlmComposite, "   " ); //$NON-NLS-1$
        toolkit.createLabel( authMechNtlmComposite, Messages.getString( "LdapLdapsServersPage.Provider" ) ); //$NON-NLS-1$
        authMechNtlmText = toolkit.createText( authMechNtlmComposite, "" ); //$NON-NLS-1$
        authMechNtlmText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        authMechNtlmComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 2, 1 ) );

        // GSS-SPNEGO Checkbox and Text
        authMechGssSpnegoCheckbox = toolkit.createButton( composite, "GSS-SPNEGO", SWT.CHECK ); //$NON-NLS-1$
        authMechGssSpnegoCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        toolkit.createLabel( composite, "" ); //$NON-NLS-1$
        Composite authMechGssSpnegoComposite = toolkit.createComposite( composite );
        authMechGssSpnegoComposite.setLayout( new GridLayout( 3, false ) );
        toolkit.createLabel( authMechGssSpnegoComposite, "   " ); //$NON-NLS-1$
        toolkit.createLabel( authMechGssSpnegoComposite, Messages.getString( "LdapLdapsServersPage.Provider" ) ); //$NON-NLS-1$
        authMechGssSpnegoText = toolkit.createText( authMechGssSpnegoComposite, "" ); //$NON-NLS-1$
        authMechGssSpnegoText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        authMechGssSpnegoComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    }


    // ── Setting Up The SASL Authentication Bureau ─────────────────────────────
    // The Imperial authentication bureau has its own reception: a hostname
    // plaque (SASL host), an official station identity (principal), a directory
    // to search for users (search base DN), and a list of registered realms
    // that the bureau recognises.
    // We build the SASL settings section with text fields plus a realm table
    // with Add/Edit/Delete buttons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "SASL Settings" section covering the SASL host name,
     * SASL principal, search base DN, and a manageable list of SASL realms.
     * These values tell the server how to identify itself to SASL clients
     * and where to look for accounts during authentication.
     *
     * @param toolkit  the form toolkit for creating styled widgets
     * @param parent   the right-column composite to attach this section to
     */
    private void createSaslSettingsSection( FormToolkit toolkit, Composite parent )
    {
        // Creation of the section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR | Section.TWISTIE | Section.COMPACT );
        section.setText( Messages.getString( "LdapLdapsServersPage.SaslSettings" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout glayout = new GridLayout( 3, false );
        composite.setLayout( glayout );
        section.setClient( composite );

        // SASL Host Text
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.SaslHost" ) ); //$NON-NLS-1$
        saslHostText = toolkit.createText( composite, "" ); //$NON-NLS-1$
        setGridDataWithDefaultWidth( saslHostText, new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        Label defaultSaslHostLabel = createDefaultValueLabel( toolkit, composite, "ldap.example.com" ); //$NON-NLS-1$
        defaultSaslHostLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 3, 1 ) );

        // SASL Principal Text
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.SaslPrincipal" ) ); //$NON-NLS-1$
        saslPrincipalText = toolkit.createText( composite, "" ); //$NON-NLS-1$
        setGridDataWithDefaultWidth( saslPrincipalText, new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        Label defaultSaslPrincipalLabel = createDefaultValueLabel( toolkit, composite,
            "ldap/ldap.example.com@EXAMPLE.COM" ); //$NON-NLS-1$
        defaultSaslPrincipalLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 3, 1 ) );

        // Search Base Dn Text
        toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.SearchBaseDn" ) ); //$NON-NLS-1$
        saslSearchBaseDnText = toolkit.createText( composite, "" ); //$NON-NLS-1$
        setGridDataWithDefaultWidth( saslSearchBaseDnText, new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        Label defaultSaslSearchBaseDnLabel = createDefaultValueLabel( toolkit, composite, "ou=users,dc=example,dc=com" ); //$NON-NLS-1$
        defaultSaslSearchBaseDnLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 3, 1 ) );

        // SASL Realms label 
        Label saslRealmsLabel = toolkit.createLabel( composite, Messages.getString( "LdapLdapsServersPage.SaslRealms" ), SWT.WRAP  ); //$NON-NLS-1$
        setBold( saslRealmsLabel );
        saslRealmsLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, glayout.numColumns, 1 ) );

        // SASL realms Table Viewer
        saslRealmsTableViewer = new TableViewer( composite );
        saslRealmsTableViewer.setContentProvider( new ArrayContentProvider() );
        GridData saslRealmsTableViewerGridData = new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 3 );
        saslRealmsTableViewerGridData.heightHint = 60;
        saslRealmsTableViewer.getControl().setLayoutData( saslRealmsTableViewerGridData );

        // Add SASL realms Button
        addSaslRealmsButton = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.Add" ), SWT.PUSH ); //$NON-NLS-1$
        addSaslRealmsButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false, 1, 1 ) );

        // Edit SASL realms Button
        editSaslRealmsButton = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.Edit" ), SWT.PUSH ); //$NON-NLS-1$
        editSaslRealmsButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false, 1, 1 ) );
        editSaslRealmsButton.setEnabled( false );

        // Delete SASL realms Button
        deleteSaslRealmsButton = toolkit.createButton( composite,
            Messages.getString( "LdapLdapsServersPage.Delete" ), SWT.PUSH ); //$NON-NLS-1$
        deleteSaslRealmsButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false, 1, 1 ) );
        deleteSaslRealmsButton.setEnabled( false );
    }


    // ── Posting The Duty Officers At Every Station ────────────────────────────
    // The Death Star's shift commander assigns an officer to every workstation
    // on the transport deck — someone to react when a dial is turned or a
    // button is pressed.
    // We attach every listener to its corresponding widget so that UI changes
    // immediately flow through to the config bean and the dirty flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wires all UI listeners to their controls.
     * After calling this, any change the user makes to a widget on this page
     * propagates to the config bean and marks the editor dirty.
     * We call this at the end of {@link #refreshUI()} after populating the
     * widgets, to avoid spurious dirty events during population.
     */
    private void addListeners()
    {
        // Enable LDAP Checkbox
        addDirtyListener( enableLdapCheckbox );
        addSelectionListener( enableLdapCheckbox, enableLdapCheckboxListener );

        // LDAP Port Text
        addDirtyListener( ldapPortText );
        addModifyListener( ldapPortText, ldapPortTextListener );

        // LDAP Address Text
        addDirtyListener( ldapAddressText );
        addModifyListener( ldapAddressText, ldapAddressTextListener );

        // LDAP nbThreads Text
        addDirtyListener( ldapNbThreadsText );
        addModifyListener( ldapNbThreadsText, ldapNbThreadsTextListener );

        // LDAP BackLogSize Text
        addDirtyListener( ldapBackLogSizeText );
        addModifyListener( ldapBackLogSizeText, ldapBackLogSizeTextListener );

        // Enable LDAPS Checkbox
        addDirtyListener( enableLdapsCheckbox );
        addSelectionListener( enableLdapsCheckbox, enableLdapsCheckboxListener );

        // LDAPS Address Text
        addDirtyListener( ldapsAddressText );
        addModifyListener( ldapsAddressText, ldapsAddressTextListener );

        // LDAPS Port Text
        addDirtyListener( ldapsPortText );
        addModifyListener( ldapsPortText, ldapsPortTextListener );

        // LDAPS nbThreads Text
        addDirtyListener( ldapsNbThreadsText );
        addModifyListener( ldapsNbThreadsText, ldapsNbThreadsTextListener );

        // LDAPS BackLogSize Text
        addDirtyListener( ldapsBackLogSizeText );
        addModifyListener( ldapsBackLogSizeText, ldapsBackLogSizeTextListener );
        
        // Enable wantClientAuth Checkbox
        addDirtyListener( wantClientAuthCheckbox );
        addSelectionListener( wantClientAuthCheckbox, wantClientAuthListener );

        // Enable needClientAuth Checkbox
        addDirtyListener( needClientAuthCheckbox );
        addSelectionListener( needClientAuthCheckbox, needClientAuthListener );

        // Auth Mechanisms Simple Checkbox
        addDirtyListener( authMechSimpleCheckbox );
        addSelectionListener( authMechSimpleCheckbox, authMechSimpleCheckboxListener );

        // Auth Mechanisms GSSAPI Checkbox
        addDirtyListener( authMechGssapiCheckbox );
        addSelectionListener( authMechGssapiCheckbox, authMechGssapiCheckboxListener );

        // Auth Mechanisms CRAM-MD5 Checkbox
        addDirtyListener( authMechCramMd5Checkbox );
        addSelectionListener( authMechCramMd5Checkbox, authMechCramMd5CheckboxListener );

        // Auth Mechanisms DIGEST-MD5 Checkbox
        addDirtyListener( authMechDigestMd5Checkbox );
        addSelectionListener( authMechDigestMd5Checkbox, authMechDigestMd5CheckboxListener );

        // Auth Mechanisms NTLM Checkbox
        addDirtyListener( authMechNtlmCheckbox );
        addSelectionListener( authMechNtlmCheckbox, authMechNtlmCheckboxListener );

        // Auth Mechanisms NTLM Text
        addDirtyListener( authMechNtlmText );
        addModifyListener( authMechNtlmText, authMechNtlmTextListener );

        // Auth Mechanisms GSS SPNEGO Checkbox
        addDirtyListener( authMechGssSpnegoCheckbox );
        addSelectionListener( authMechGssSpnegoCheckbox, authMechGssSpnegoCheckboxListener );
        addModifyListener( authMechGssSpnegoText, authMechGssSpnegoTextListener );

        // Auth Mechanisms GSS SPNEGO Text
        addDirtyListener( authMechGssSpnegoText );
        addModifyListener( authMechGssSpnegoText, authMechGssSpnegoTextListener );

        // Keystore File Text
        addDirtyListener( keystoreFileText );
        addModifyListener( keystoreFileText, keystoreFileTextListener );

        // Keystore File Browse Button
        addSelectionListener( keystoreFileBrowseButton, keystoreFileBrowseButtonSelectionListener );

        // Password Text
        addDirtyListener( keystorePasswordText );
        addModifyListener( keystorePasswordText, keystorePasswordTextListener );

        // Show Password Checkbox
        addSelectionListener( showPasswordCheckbox, showPasswordCheckboxSelectionListener );

        // SASL Host Text
        addDirtyListener( saslHostText );
        addModifyListener( saslHostText, saslHostTextListener );

        // SASL Principal Text
        addDirtyListener( saslPrincipalText );
        addModifyListener( saslPrincipalText, saslPrincipalTextListener );

        // SASL Seach Base Dn Text
        addDirtyListener( saslSearchBaseDnText );
        addModifyListener( saslSearchBaseDnText, saslSearchBaseDnTextListener );

        // SASL Realms Table Viewer
        addSelectionChangedListener( saslRealmsTableViewer, saslRealmsTableViewerSelectionChangedListener );
        addDoubleClickListener( saslRealmsTableViewer, saslRealmsTableViewerDoubleClickListener );
        addSelectionListener( editSaslRealmsButton, editSaslRealmsButtonListener );
        addSelectionListener( addSaslRealmsButton, addSaslRealmsButtonListener );
        addSelectionListener( deleteSaslRealmsButton, deleteSaslRealmsButtonListener );

        // Max Time Limit Text
        addDirtyListener( maxTimeLimitText );
        addModifyListener( maxTimeLimitText, maxTimeLimitTextListener );

        // Max Size Limit Text
        addDirtyListener( maxSizeLimitText );
        addModifyListener( maxSizeLimitText, maxSizeLimitTextListener );

        // Max PDU Size Text
        addDirtyListener( maxPduSizeText );
        addModifyListener( maxPduSizeText, maxPduSizeTextListener );

        // Enable TLS Checkbox
        addDirtyListener( enableTlsCheckbox );
        addSelectionListener( enableTlsCheckbox, enableTlsCheckboxListener );

        // Hashing Password Checkbox
        addDirtyListener( enableServerSidePasswordHashingCheckbox );
        addSelectionListener( enableServerSidePasswordHashingCheckbox, enableServerSidePasswordHashingCheckboxListener );

        // Hashing Method Combo Viewer
        addDirtyListener( hashingMethodComboViewer );
        addSelectionChangedListener( hashingMethodComboViewer, hashingMethodComboViewerListener );

        // Advanced SSL Cipher Suites
        ciphersSuiteTableViewer.addCheckStateListener( ciphersSuiteTableViewerListener );

        // Advanced SSL Enabled Protocols
        // Enable sslv3 Checkbox
        addDirtyListener( sslv3Checkbox );
        addSelectionListener( sslv3Checkbox, sslv3CheckboxListener );

        // Enable tlsv1 Checkbox
        addDirtyListener( tlsv1_0Checkbox );
        addSelectionListener( tlsv1_0Checkbox, tlsv1_0CheckboxListener );

        // Enable tlsv1.1 Checkbox
        addDirtyListener( tlsv1_1Checkbox );
        addSelectionListener( tlsv1_1Checkbox, tlsv1_1CheckboxListener );

        // Enable tlsv1.2 Checkbox
        addDirtyListener( tlsv1_2Checkbox );
        addSelectionListener( tlsv1_2Checkbox, tlsv1_2CheckboxListener );

        // Replication Pinger Sleep
        addDirtyListener( replicationPingerSleepText );
        addModifyListener( replicationPingerSleepText, replicationPingerSleepTextListener );

        // Disk Synchronization Delay
        addDirtyListener( diskSynchronizationDelayText );
        addModifyListener( diskSynchronizationDelayText, diskSynchronizationDelayTextListener );
    }


    // ── Standing Down The Duty Officers ───────────────────────────────────────
    // Before the shift commander updates all the workstation displays with new
    // data, the duty officers are stood down so they don't mistake a display
    // refresh for an actual operator change and fire off spurious actions.
    // We detach every listener from its widget before we repopulate the UI in
    // refreshUI(), then reattach them afterwards via addListeners().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all UI listeners from their controls.
     * Called at the start of {@link #refreshUI()} to prevent the programmatic
     * widget population from triggering listener callbacks and marking the
     * editor dirty unnecessarily.
     */
    private void removeListeners()
    {
        // Enable LDAP Checkbox
        removeDirtyListener( enableLdapCheckbox );
        removeSelectionListener( enableLdapCheckbox, enableLdapCheckboxListener );

        // LDAP Port Text
        removeDirtyListener( ldapPortText );
        removeModifyListener( ldapPortText, ldapPortTextListener );

        // LDAP Address Text
        removeDirtyListener( ldapAddressText );
        removeModifyListener( ldapAddressText, ldapAddressTextListener );

        // LDAP NbThreads Text
        removeDirtyListener( ldapNbThreadsText );
        removeModifyListener( ldapNbThreadsText, ldapNbThreadsTextListener );

        // LDAP BackLogSize Text
        removeDirtyListener( ldapBackLogSizeText );
        removeModifyListener( ldapBackLogSizeText, ldapBackLogSizeTextListener );

        // Enable LDAPS Checkbox
        removeDirtyListener( enableLdapsCheckbox );
        removeSelectionListener( enableLdapsCheckbox, enableLdapsCheckboxListener );

        // LDAPS Port Text
        removeDirtyListener( ldapsPortText );
        removeModifyListener( ldapsPortText, ldapsPortTextListener );

        // LDAPS Address Text
        removeDirtyListener( ldapsAddressText );
        removeModifyListener( ldapsAddressText, ldapsAddressTextListener );

        // LDAPS NbThreads Text
        removeDirtyListener( ldapsNbThreadsText );
        removeModifyListener( ldapsNbThreadsText, ldapsNbThreadsTextListener );

        // LDAPS BackLogSize Text
        removeDirtyListener( ldapsBackLogSizeText );
        removeModifyListener( ldapsBackLogSizeText, ldapsBackLogSizeTextListener );
        
        // Enable wantClientAuth Checkbox
        removeDirtyListener( wantClientAuthCheckbox );
        removeSelectionListener( wantClientAuthCheckbox, wantClientAuthListener );

        // Enable needClientAuth Checkbox
        removeDirtyListener( needClientAuthCheckbox );
        removeSelectionListener( needClientAuthCheckbox, needClientAuthListener );

        // Auth Mechanisms Simple Checkbox
        removeDirtyListener( authMechSimpleCheckbox );
        removeSelectionListener( authMechSimpleCheckbox, authMechSimpleCheckboxListener );

        // Auth Mechanisms CRAM-MD5 Checkbox
        removeDirtyListener( authMechCramMd5Checkbox );
        removeSelectionListener( authMechCramMd5Checkbox, authMechCramMd5CheckboxListener );

        // Auth Mechanisms DIGEST-MD5 Checkbox
        removeDirtyListener( authMechDigestMd5Checkbox );
        removeSelectionListener( authMechDigestMd5Checkbox, authMechDigestMd5CheckboxListener );

        // Auth Mechanisms GSSAPI Checkbox
        removeDirtyListener( authMechGssapiCheckbox );
        removeSelectionListener( authMechGssapiCheckbox, authMechGssapiCheckboxListener );

        // Auth Mechanisms NTLM Checkbox
        removeDirtyListener( authMechNtlmCheckbox );
        removeSelectionListener( authMechNtlmCheckbox, authMechNtlmCheckboxListener );
        removeModifyListener( authMechNtlmText, authMechNtlmTextListener );

        // Auth Mechanisms NTLM Text
        removeDirtyListener( authMechNtlmText );
        removeModifyListener( authMechNtlmText, authMechNtlmTextListener );

        // Auth Mechanisms GSS SPNEGO Checkbox
        removeDirtyListener( authMechGssSpnegoCheckbox );
        removeSelectionListener( authMechGssSpnegoCheckbox, authMechGssSpnegoCheckboxListener );
        removeModifyListener( authMechGssSpnegoText, authMechGssSpnegoTextListener );

        // Auth Mechanisms GSS SPNEGO Text
        removeDirtyListener( authMechGssSpnegoText );
        removeModifyListener( authMechGssSpnegoText, authMechGssSpnegoTextListener );

        // Keystore File Text
        removeDirtyListener( keystoreFileText );
        removeModifyListener( keystoreFileText, keystoreFileTextListener );

        // Keystore File Browse Button
        removeSelectionListener( keystoreFileBrowseButton, keystoreFileBrowseButtonSelectionListener );

        // Password Text
        removeDirtyListener( keystorePasswordText );
        removeModifyListener( keystorePasswordText, keystorePasswordTextListener );

        // Show Password Checkbox
        removeSelectionListener( showPasswordCheckbox, showPasswordCheckboxSelectionListener );

        // SASL Host Text
        removeDirtyListener( saslHostText );
        removeModifyListener( saslHostText, saslHostTextListener );

        // SASL Principal Text
        removeDirtyListener( saslPrincipalText );
        removeModifyListener( saslPrincipalText, saslPrincipalTextListener );

        // SASL Seach Base Dn Text
        removeDirtyListener( saslSearchBaseDnText );
        removeModifyListener( saslSearchBaseDnText, saslSearchBaseDnTextListener );
        
        // SASL Realms
        removeSelectionChangedListener( saslRealmsTableViewer, saslRealmsTableViewerSelectionChangedListener );
        removeDoubleClickListener( saslRealmsTableViewer, saslRealmsTableViewerDoubleClickListener );
        
        // SASL Realms add/edit/delete buttons
        removeSelectionListener( addSaslRealmsButton, addSaslRealmsButtonListener );
        removeSelectionListener( editSaslRealmsButton, editSaslRealmsButtonListener );
        removeSelectionListener( deleteSaslRealmsButton, deleteSaslRealmsButtonListener );
        
        // Max Time Limit Text
        removeDirtyListener( maxTimeLimitText );
        removeModifyListener( maxTimeLimitText, maxTimeLimitTextListener );

        // Max Size Limit Text
        removeDirtyListener( maxSizeLimitText );
        removeModifyListener( maxSizeLimitText, maxSizeLimitTextListener );

        // Max PDU Size Text
        removeDirtyListener( maxPduSizeText );
        removeModifyListener( maxPduSizeText, maxPduSizeTextListener );

        // Hashing Password Checkbox
        removeDirtyListener( enableServerSidePasswordHashingCheckbox );
        removeSelectionListener( enableServerSidePasswordHashingCheckbox,
            enableServerSidePasswordHashingCheckboxListener );

        // Hashing Method Combo Viewer
        removeDirtyListener( hashingMethodComboViewer );
        removeSelectionChangedListener( hashingMethodComboViewer, hashingMethodComboViewerListener );

        // Advanced SSL Cipher Suites
        ciphersSuiteTableViewer.removeCheckStateListener( ciphersSuiteTableViewerListener );

        // Advanced SSL Enabled Protocols SSL v3
        removeDirtyListener( sslv3Checkbox );
        removeSelectionListener( sslv3Checkbox, sslv3CheckboxListener );

        // Advanced SSL Enabled Protocols TLS v1
        removeDirtyListener( tlsv1_0Checkbox );
        removeSelectionListener( tlsv1_0Checkbox, tlsv1_0CheckboxListener );

        // Advanced SSL Enabled Protocols TLS v1.1
        removeDirtyListener( tlsv1_1Checkbox );
        removeSelectionListener( tlsv1_1Checkbox, tlsv1_1CheckboxListener );

        // Advanced SSL Enabled Protocols TLS v1.2
        removeDirtyListener( tlsv1_2Checkbox );
        removeSelectionListener( tlsv1_2Checkbox, tlsv1_2CheckboxListener );


        // Advanced SSL Enabled Protocols add/edit/delete buttons removal

        // Replication Pinger Sleep
        removeDirtyListener( replicationPingerSleepText );
        removeModifyListener( replicationPingerSleepText, replicationPingerSleepTextListener );

        // Disk Synchronization Delay
        removeDirtyListener( diskSynchronizationDelayText );
        removeModifyListener( diskSynchronizationDelayText, diskSynchronizationDelayTextListener );
    }


    // ── Updating All The Workstation Displays ─────────────────────────────────
    // The shift commander takes a fresh readout from the config database and
    // pushes the latest values to every instrument on the transport deck —
    // port numbers, addresses, cipher lists, protocol toggles, all of it.
    // We remove listeners first to avoid feedback, populate every widget from
    // the config bean, then re-add listeners so future changes are captured.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Repopulates every widget on this page from the current config bean.
     * Called by the editor when the tab is switched to, or when the
     * configuration is reset. We bracket the population with
     * {@link #removeListeners()} and {@link #addListeners()} to avoid
     * spurious dirty events while we're setting values programmatically.
     */
    protected void refreshUI()
    {
        if ( isInitialized() )
        {
            removeListeners();

            // LDAP Server ------------------------------------------------------------------------
            TransportBean ldapServerTransportBean = getLdapServerTransportBean();
            setSelection( enableLdapCheckbox, ldapServerTransportBean.isEnabled() );
            
            boolean ldapEnabled = enableLdapCheckbox.getSelection();

            setEnabled( ldapPortText, ldapEnabled );
            setText( ldapPortText, Integer.toString( ldapServerTransportBean.getSystemPort() ) );

            setEnabled( ldapAddressText, ldapEnabled );
            setText( ldapAddressText, ldapServerTransportBean.getTransportAddress() );

            setEnabled( ldapNbThreadsText, ldapEnabled );
            setText( ldapNbThreadsText, Integer.toString( ldapServerTransportBean.getTransportNbThreads() ) );
            
            setEnabled( ldapBackLogSizeText, ldapEnabled );
            setText( ldapBackLogSizeText, Integer.toString( ldapServerTransportBean.getTransportBackLog() ) );

            // LDAPS Server -----------------------------------------------------------------------
            TransportBean ldapsServerTransportBean = getLdapsServerTransportBean();
            setSelection( enableLdapsCheckbox, ldapsServerTransportBean.isEnabled() );

            boolean ldapsEnabled = enableLdapsCheckbox.getSelection();

            setEnabled( ldapsPortText, ldapsEnabled );
            setText( ldapsPortText, Integer.toString( ldapsServerTransportBean.getSystemPort() ) );

            setEnabled( ldapsAddressText, ldapsEnabled );
            setText( ldapsAddressText, ldapsServerTransportBean.getTransportAddress() );

            setEnabled( ldapsNbThreadsText, ldapsEnabled );
            setText( ldapsNbThreadsText, Integer.toString( ldapsServerTransportBean.getTransportNbThreads() ) );
            
            setEnabled( ldapsBackLogSizeText, ldapsEnabled );
            setText( ldapsBackLogSizeText, Integer.toString( ldapsServerTransportBean.getTransportBackLog() ) );

            // SASL Properties --------------------------------------------------------------------
            LdapServerBean ldapServerBean = getLdapServerBean();
            setText( saslHostText, ldapServerBean.getLdapServerSaslHost() );
            setText( saslPrincipalText, ldapServerBean.getLdapServerSaslPrincipal() );
            setText( saslSearchBaseDnText, ldapServerBean.getSearchBaseDn().toString() );
            saslRealmsTableViewer.setInput( ldapServerBean.getLdapServerSaslRealms() );
            saslRealmsTableViewer.refresh();

            // Keystore Properties
            setText( keystoreFileText, ldapServerBean.getLdapServerKeystoreFile() );
            setText( keystorePasswordText, ldapServerBean.getLdapServerCertificatePassword() );

            // Supported Auth Mechanisms
            List<SaslMechHandlerBean> saslMechHandlers = ldapServerBean.getSaslMechHandlers();
            uncheckAllSupportedAuthenticationMechanisms();
            
            for ( SaslMechHandlerBean saslMechHandler : saslMechHandlers )
            {
                if ( SASL_MECHANISMS_SIMPLE.equalsIgnoreCase( saslMechHandler.getSaslMechName() ) )
                {
                    setSelection( authMechSimpleCheckbox, saslMechHandler.isEnabled() );
                }
                else if ( SupportedSaslMechanisms.GSSAPI.equalsIgnoreCase( saslMechHandler.getSaslMechName() ) )
                {
                    setSelection( authMechGssapiCheckbox, saslMechHandler.isEnabled() );
                }
                else if ( SupportedSaslMechanisms.CRAM_MD5.equalsIgnoreCase( saslMechHandler.getSaslMechName() ) )
                {
                    setSelection( authMechCramMd5Checkbox, saslMechHandler.isEnabled() );
                }
                else if ( SupportedSaslMechanisms.DIGEST_MD5.equalsIgnoreCase( saslMechHandler.getSaslMechName() ) )
                {
                    setSelection( authMechDigestMd5Checkbox, saslMechHandler.isEnabled() );
                }
                else if ( SupportedSaslMechanisms.GSS_SPNEGO.equalsIgnoreCase( saslMechHandler.getSaslMechName() ) )
                {
                    setSelection( authMechGssSpnegoCheckbox, saslMechHandler.isEnabled() );
                    setEnabled( authMechGssSpnegoText, saslMechHandler.isEnabled() );
                    setText( authMechGssSpnegoText, saslMechHandler.getNtlmMechProvider() );
                }
                else if ( SupportedSaslMechanisms.NTLM.equalsIgnoreCase( saslMechHandler.getSaslMechName() ) )
                {
                    setSelection( authMechNtlmCheckbox, saslMechHandler.isEnabled() );
                    setEnabled( authMechNtlmText, saslMechHandler.isEnabled() );
                    setText( authMechNtlmText, saslMechHandler.getNtlmMechProvider() );
                }
            }
            
            // Delegating authentication
            // TODO

            // Limits
            setText( maxTimeLimitText, Integer.toString( ldapServerBean.getLdapServerMaxTimeLimit() ) );
            setText( maxSizeLimitText, Integer.toString( ldapServerBean.getLdapServerMaxSizeLimit() ) );
            setText( maxPduSizeText, Integer.toString( ldapServerBean.getMaxPDUSize() ) );

            // Enable TLS Checkbox
            setSelection( enableTlsCheckbox, getTlsExtendedOpHandlerBean().isEnabled() );

            // Hashing Password widgets
            InterceptorBean hashingMethodInterceptor = getHashingPasswordInterceptor();
            
            if ( hashingMethodInterceptor == null )
            {
                // No hashing method interceptor
                setSelection( enableServerSidePasswordHashingCheckbox, false );
                setEnabled( hashingMethodComboViewer.getCombo(), enableServerSidePasswordHashingCheckbox.getSelection() );
                setSelection( hashingMethodComboViewer, LdapSecurityConstants.HASH_METHOD_SSHA );
            }
            else
            {
                LdapSecurityConstants hashingMethod = getHashingMethodFromInterceptor( hashingMethodInterceptor );
                
                if ( hashingMethod != null )
                {
                    // Setting selection for the hashing method
                    setSelection( enableServerSidePasswordHashingCheckbox, hashingMethodInterceptor.isEnabled() );
                    setEnabled( hashingMethodComboViewer.getCombo(),
                        enableServerSidePasswordHashingCheckbox.getSelection() );
                    setSelection( hashingMethodComboViewer, hashingMethod );
                }
                else
                {
                    // Couldn't determine which hashing method is used
                    setSelection( enableServerSidePasswordHashingCheckbox, false );
                    setEnabled( hashingMethodComboViewer.getCombo(),
                        enableServerSidePasswordHashingCheckbox.getSelection() );
                    setSelection( hashingMethodComboViewer, LdapSecurityConstants.HASH_METHOD_SSHA );
                }
            }

            // SSL/Start TLS Cipher Suites
            List<String> enabledCiphers = ldapServerTransportBean.getEnabledCiphers();
            List<SupportedCipher> supportedCiphers = new ArrayList<>();
            
            if ( enabledCiphers == null )
            {
                // We don't have any selected ciphers. Propose the full list
                for ( SupportedCipher cipher : SupportedCipher.supportedCiphersJava8 )
                {
                    supportedCiphers.add( cipher );
                }
            }
            else
            {
                for ( String supportedCipher : enabledCiphers )
                {
                    SupportedCipher cipher = SupportedCipher.getByName( supportedCipher );

                    if ( cipher != null )
                    {
                        supportedCiphers.add( cipher );
                    }
                }
            }
            
            ciphersSuiteTableViewer.setCheckedElements( supportedCiphers.toArray() );
            ciphersSuiteTableViewer.refresh();
            
            // SSL/Start TLS Enabled Protocols
            // Check if we have a LDAP transport
            TransportBean transportBean = getLdapTransportBean( TRANSPORT_ID_LDAP );
            
            // Ok, process the enabled protocols now
            List<String> enabledProtocols = transportBean.getEnabledProtocols();
                
            if ( enabledProtocols != null )
            {
                for ( String enabledProtocol : transportBean.getEnabledProtocols() )
                {
                    if ( SSL_V3.equalsIgnoreCase( enabledProtocol ) )
                    {
                        setSelection( sslv3Checkbox, true );
                    }
                    else if ( TLS_V1_0.equalsIgnoreCase( enabledProtocol ) )
                    {
                        setSelection( tlsv1_0Checkbox, true );
                    }
                    else if ( TLS_V1_1.equalsIgnoreCase( enabledProtocol ) )
                    {
                        setSelection( tlsv1_1Checkbox, true );
                    }
                    else if ( TLS_V1_2.equalsIgnoreCase( enabledProtocol ) )
                    {
                        setSelection( tlsv1_2Checkbox, true );
                    }
                }
            }
            
            // Replication Pinger Sleep
            setText( replicationPingerSleepText, Integer.toString( ldapServerBean.getReplPingerSleep() ) );

            // Disk Synchronization Delay
            setText( diskSynchronizationDelayText, Long.toString( getDirectoryServiceBean().getDsSyncPeriodMillis() ) );

            addListeners();
        }
    }


    // ── Clearing All The Access Badges ────────────────────────────────────────
    // Before re-scanning which mechanisms are actually configured, the security
    // office clears every badge from the board so we start from a blank slate.
    // We uncheck every auth mechanism checkbox and disable the provider text
    // fields — refreshUI() will re-check whatever the config bean says is on.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets all supported authentication mechanism checkboxes to unchecked
     * and disables the provider text fields for NTLM and GSS-SPNEGO.
     * Called at the start of the auth-mechanism refresh loop in
     * {@link #refreshUI()} so we don't accumulate stale selections.
     */
    private void uncheckAllSupportedAuthenticationMechanisms()
    {
        setSelection( authMechSimpleCheckbox, false );
        setSelection( authMechCramMd5Checkbox, false );
        setSelection( authMechDigestMd5Checkbox, false );
        setSelection( authMechGssapiCheckbox, false );
        setSelection( authMechNtlmCheckbox, false );
        setEnabled( authMechNtlmText, false );
        setSelection( authMechGssSpnegoCheckbox, false );
        setEnabled( authMechGssSpnegoText, false );
    }


    // ── Toggling A Specific Identification Protocol ───────────────────────────
    // The security office flips the switch for a particular access protocol on
    // the board — GSSAPI, CRAM-MD5, whatever the caller specifies.
    // We find the matching SaslMechHandlerBean by name and flip its enabled flag,
    // leaving every other mechanism untouched.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables a specific SASL authentication mechanism in the
     * server bean.
     * Walks the list of SASL mechanism handlers looking for one whose name
     * matches {@code mechanismName} (case-insensitive) and sets its enabled
     * flag. No-op if the mechanism isn't found.
     *
     * @param mechanismName  the SASL mechanism name (e.g., "GSSAPI", "SIMPLE")
     * @param enabled        {@code true} to enable, {@code false} to disable
     */
    private void setEnableSupportedAuthenticationMechanism( String mechanismName, boolean enabled )
    {
        List<SaslMechHandlerBean> saslMechHandlers = getLdapServerBean().getSaslMechHandlers();
        
        for ( SaslMechHandlerBean saslMechHandler : saslMechHandlers )
        {
            if ( mechanismName.equalsIgnoreCase( saslMechHandler.getSaslMechName() ) )
            {
                saslMechHandler.setEnabled( enabled );
                return;
            }
        }
    }


    // ── Assigning The Third-Party Contractor To A Protocol ────────────────────
    // Some authentication protocols are too specialised for in-house staff —
    // the Empire brings in an outside contractor (a third-party library class)
    // to handle NTLM or SPNEGO on their behalf.
    // We find the handler by mechanism name and set its NTLM provider class name
    // to the value the user typed in the provider text field.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the NTLM mechanism provider class name for the given SASL
     * authentication mechanism.
     * The provider is a third-party class (not bundled with ApacheDS) that
     * actually performs the NTLM or SPNEGO handshake.
     *
     * @param mechanismName     the SASL mechanism name to update
     * @param ntlmMechProvider  the fully-qualified class name of the provider
     */
    private void setNtlmMechProviderSupportedAuthenticationMechanism( String mechanismName, String ntlmMechProvider )
    {
        List<SaslMechHandlerBean> saslMechHandlers = getLdapServerBean().getSaslMechHandlers();
        
        for ( SaslMechHandlerBean saslMechHandler : saslMechHandlers )
        {
            if ( mechanismName.equalsIgnoreCase( saslMechHandler.getSaslMechName() ) )
            {
                saslMechHandler.setNtlmMechProvider( ntlmMechProvider );
                return;
            }
        }
    }


    // ── Pulling The LDAP Server's Dossier ─────────────────────────────────────
    // Every system on the Death Star has a technical dossier — the LDAP server's
    // dossier is the LdapServerBean inside the current directory service config.
    // We delegate to the static version, passing our current DirectoryServiceBean.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdapServerBean} for the current directory service.
     * Convenience wrapper around the static overload — uses the page's own
     * {@link #getDirectoryServiceBean()} as the source.
     *
     * @return the LDAP server bean (never {@code null}; created on demand)
     */
    private LdapServerBean getLdapServerBean()
    {
        return getLdapServerBean( getDirectoryServiceBean() );
    }


    // ── Retrieving Or Commissioning The LDAP Server Module ────────────────────
    // The Death Star's engineering manifest lists every module — if the LDAP
    // server module is already on the list, we grab its dossier; if it's not,
    // we commission a new one on the spot and add it to the manifest.
    // This static version is reusable by any caller that has a directory service
    // bean without needing to go through a page instance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdapServerBean} inside the given directory service bean,
     * creating and registering a fresh one if none exists yet.
     * Static so other pages (e.g., OverviewPage) can share this lookup logic.
     *
     * @param directoryServiceBean  the directory service whose LDAP server we want
     * @return the existing or newly created LDAP server bean
     */
    public static LdapServerBean getLdapServerBean( DirectoryServiceBean directoryServiceBean )
    {
        LdapServerBean ldapServerBean = directoryServiceBean.getLdapServerBean();

        if ( ldapServerBean == null )
        {
            // We don't have any LdapServer associated with this DirectoryService, create one
            ldapServerBean = new LdapServerBean();
            directoryServiceBean.addServers( ldapServerBean );
        }

        return ldapServerBean;
    }


    // ── Checking The Plain Channel's Configuration File ───────────────────────
    // The comms officer pulls up the configuration card for the plain (unencrypted)
    // LDAP channel — port, address, thread count, backlog — all in one bean.
    // We delegate to getLdapTransportBean with the LDAP transport ID.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} for the plain LDAP transport.
     * Convenience wrapper around {@link #getLdapTransportBean(String)} using
     * the constant transport ID "ldap".
     *
     * @return the LDAP transport bean (never {@code null}; created on demand)
     */
    private TransportBean getLdapServerTransportBean()
    {
        return getLdapTransportBean( TRANSPORT_ID_LDAP );
    }


    // ── Static Lookup: Plain Channel Config For Any Directory Service ─────────
    // Same as the instance version above, but static — useful when an external
    // caller has a directory service bean and doesn't want to construct a page
    // just to read one transport setting.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} for the plain LDAP transport of the
     * given directory service, creating it on demand if needed.
     *
     * @param directoryServiceBean  the directory service to look in
     * @return the LDAP transport bean (never {@code null})
     */
    public static TransportBean getLdapServerTransportBean( DirectoryServiceBean directoryServiceBean )
    {
        return getLdapTransportBean( directoryServiceBean, TRANSPORT_ID_LDAP );
    }


    // ── Checking The Encrypted Channel's Configuration File ───────────────────
    // The comms officer now pulls up the SSL-encrypted channel's config card —
    // same kind of transport bean, but with the SSL flag set.
    // We delegate to getLdapTransportBean with the LDAPS transport ID.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} for the encrypted LDAPS transport.
     * Convenience wrapper around {@link #getLdapTransportBean(String)} using
     * the constant transport ID "ldaps".
     *
     * @return the LDAPS transport bean (never {@code null}; created on demand)
     */
    private TransportBean getLdapsServerTransportBean()
    {
        return getLdapTransportBean( TRANSPORT_ID_LDAPS );
    }


    // ── Static Lookup: Encrypted Channel Config For Any Directory Service ─────
    // The static companion to the LDAPS instance getter — useful when external
    // code has a directory service bean and needs the LDAPS transport config
    // without having a page instance around.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} for the encrypted LDAPS transport of
     * the given directory service, creating it on demand if needed.
     *
     * @param directoryServiceBean  the directory service to look in
     * @return the LDAPS transport bean (never {@code null})
     */
    public static TransportBean getLdapsServerTransportBean( DirectoryServiceBean directoryServiceBean )
    {
        return getLdapTransportBean( directoryServiceBean, TRANSPORT_ID_LDAPS );
    }


    // ── Looking Up A Channel Config Card By Channel ID ────────────────────────
    // The filing clerk searches the manifest by channel ID ("ldap" or "ldaps")
    // and hands back the matching config card.
    // We delegate to the static version, passing our current directory service bean.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} with the given transport ID from the
     * current directory service's LDAP server.
     * Delegates to the static version using this page's config bean.
     *
     * @param id  the transport ID to look up ("ldap" or "ldaps")
     * @return the matching transport bean (never {@code null}; created on demand)
     */
    private TransportBean getLdapTransportBean( String id )
    {
        return getLdapTransportBean( getDirectoryServiceBean(), id );
    }


    // ── The Canonical Channel Config Lookup ───────────────────────────────────
    // All the other transport-getter methods eventually end up here — the one
    // true filing clerk who knows where every channel config card lives, and
    // who creates a fresh card (with sensible defaults) if none exists yet.
    // We walk the LdapServerBean's transport array looking for a match by ID.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} with the given ID from the LDAP server
     * inside {@code directoryServiceBean}, creating a new TCP transport (with
     * default port and address) if none is found.
     *
     * <p>For example — Obi-Wan checking the navicomputer:</p>
     * <pre>
     *   "These coordinates are not in our records."
     *   So he creates a new entry with sensible defaults
     *   rather than leaving the destination blank.
     * </pre>
     *
     * @param directoryServiceBean  the directory service to search
     * @param id                    the transport ID to find ("ldap" or "ldaps")
     * @return the existing or newly created transport bean
     */
    public static TransportBean getLdapTransportBean( DirectoryServiceBean directoryServiceBean, String id )
    {
        // First fetch the LdapServer bean
        LdapServerBean ldapServerBean = getLdapServerBean( directoryServiceBean );

        TransportBean transportBean = null;

        // Looking for the transports for this server
        TransportBean[] ldapServerTransportBeans = ldapServerBean.getTransports();
        
        if ( ldapServerTransportBeans != null )
        {
            for ( TransportBean ldapServerTransportBean : ldapServerTransportBeans )
            {
                if ( id.equals( ldapServerTransportBean.getTransportId() ) )
                {
                    transportBean = ldapServerTransportBean;
                    break;
                }
            }
        }

        // No corresponding transport has been found
        if ( transportBean == null )
        {
            // Creating a TCP transport bean
            transportBean = new TcpTransportBean();
            ldapServerBean.addTransports( transportBean );

            // ID
            transportBean.setTransportId( id );

            // Address
            transportBean.setTransportAddress( DEFAULT_ADDRESS );

            // Port
            if ( TRANSPORT_ID_LDAP.equals( id ) )
            {
                transportBean.setSystemPort( DEFAULT_PORT_LDAP );
            }
            else if ( TRANSPORT_ID_LDAPS.equals( id ) )
            {
                transportBean.setSystemPort( DEFAULT_PORT_LDAPS );
            }

            // SSL
            if ( TRANSPORT_ID_LDAPS.equals( id ) )
            {
                transportBean.setTransportEnableSSL( true );
            }
        }

        return transportBean;
    }


    // ── Finding The StartTLS Specialist ───────────────────────────────────────
    // The Death Star has a specialist team for TLS upgrades — they handle the
    // StartTLS extended operation. We look for their handler in the server's
    // extended-op list, and if nobody's been assigned yet, we draft one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ExtendedOpHandlerBean} for the StartTLS extended
     * operation, creating and registering a disabled one if it doesn't exist yet.
     *
     * @return the StartTLS extended operation handler bean (never {@code null})
     */
    private ExtendedOpHandlerBean getTlsExtendedOpHandlerBean()
    {
        // Getting the LDAP Server
        LdapServerBean ldapServerBean = getLdapServerBean();

        // Getting the list of extended operation handlers
        List<ExtendedOpHandlerBean> extendedOpHandlers = ldapServerBean.getExtendedOps();
        
        for ( ExtendedOpHandlerBean extendedOpHandlerBean : extendedOpHandlers )
        {
            // Looking for the Start TLS extended operation handler 
            if ( START_TLS_HANDLER_ID.equalsIgnoreCase( extendedOpHandlerBean.getExtendedOpId() ) )
            {
                return extendedOpHandlerBean;
            }
        }

        // We haven't found a corresponding extended operation handler,
        // we need to create it
        ExtendedOpHandlerBean extendedOpHandlerBean = new ExtendedOpHandlerBean();
        extendedOpHandlerBean.setExtendedOpId( START_TLS_HANDLER_ID );
        extendedOpHandlerBean.setExtendedOpHandlerClass( START_TLS_HANDLER_CLASS );
        extendedOpHandlerBean.setEnabled( false );
        extendedOpHandlers.add( extendedOpHandlerBean );
        
        return extendedOpHandlerBean;
    }


    // ── Checking If The Password Vault Guard Is On Duty ───────────────────────
    // The password hashing interceptor is like the vault guard who encrypts
    // credentials before they go into storage. We check the interceptor chain
    // to see if they're already assigned, or return null if the post is empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks for the password-hashing interceptor in the directory service's
     * interceptor chain.
     *
     * @return the hashing interceptor bean, or {@code null} if none is installed
     */
    private InterceptorBean getHashingPasswordInterceptor()
    {
        // Looking for the password hashing interceptor
        for ( InterceptorBean interceptor : getDirectoryServiceBean().getInterceptors() )
        {
            if ( HASHING_PASSWORD_INTERCEPTOR_ID.equalsIgnoreCase( interceptor.getInterceptorId() ) )
            {
                return interceptor;
            }
        }

        return null;
    }


    // ── Reading The Vault Guard's Badge Number ────────────────────────────────
    // Each password-hashing interceptor class has a fully-qualified class name
    // that encodes which algorithm it uses — like reading the badge number of
    // the vault guard to learn which cipher they specialise in.
    // We pattern-match the class name back to a LdapSecurityConstants enum value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reverse-maps an interceptor's class name to its {@link LdapSecurityConstants}
     * hashing method.
     * Each hashing algorithm has its own interceptor class (e.g.,
     * {@code SshaPasswordHashingInterceptor} maps to {@code HASH_METHOD_SSHA}).
     *
     * @param interceptor  the interceptor whose class name we inspect
     * @return the corresponding hash method constant, or {@code null} if unrecognised
     */
    private LdapSecurityConstants getHashingMethodFromInterceptor( InterceptorBean interceptor )
    {
        if ( interceptor != null )
        {
            String interceptorClassName = interceptor.getInterceptorClassName();

            if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SHA;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SSHA;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_MD5 ) )
            {
                return LdapSecurityConstants.HASH_METHOD_MD5;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SMD5 ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SMD5;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_CRYPT ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SMD5;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA256 ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SHA256;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA256 ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SSHA256;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA384 ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SHA384;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA384 ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SSHA384;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA512 ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SHA512;
            }
            else if ( interceptorClassName.equalsIgnoreCase( HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA512 ) )
            {
                return LdapSecurityConstants.HASH_METHOD_SSHA512;
            }
        }

        return null;
    }


    // ── Finding The Seniority Of The Key Derivation Officer ───────────────────
    // Interceptors in ApacheDS are ordered — each one has a rank in the chain.
    // The password-hashing interceptor must be inserted right after the key
    // derivation interceptor, so we need to know its rank first.
    // We scan the interceptor list and return the key derivation interceptor's
    // order, returning 0 if it hasn't been added yet.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the order (position in the chain) of the key derivation
     * interceptor, or 0 if it isn't present.
     * We use this to slot the password-hashing interceptor immediately after
     * the key derivation one when creating it from scratch.
     *
     * @return the key derivation interceptor's order, or 0 if not found
     */
    private int getKeyDerivationInterceptorOrder()
    {
        // Looking for the key derivation interceptor
        for ( InterceptorBean interceptor : getDirectoryServiceBean().getInterceptors() )
        {
            if ( "keyDerivationInterceptor".equalsIgnoreCase( interceptor.getInterceptorId() ) ) //$NON-NLS-1$
            {
                return interceptor.getInterceptorOrder();
            }
        }

        // No key derivation interceptor was found
        return 0;
    }


    // ── Commissioning A New Vault Guard ───────────────────────────────────────
    // The security chief drafts a brand-new vault guard, assigns them the right
    // algorithm class, gives them the rank just above the key derivation officer,
    // and bumps everyone else in the chain up by one to make room.
    // We build a new InterceptorBean with the correct ID, FQCN, and order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new password-hashing interceptor bean, assigns it the correct
     * position in the interceptor chain (just after the key derivation
     * interceptor), bumps the order of subsequent interceptors to make room,
     * and registers it with the directory service.
     *
     * @return the newly created and registered interceptor bean
     */
    private InterceptorBean createHashingPasswordInterceptor()
    {
        InterceptorBean hashingPasswordInterceptor = new InterceptorBean();

        // Interceptor ID
        hashingPasswordInterceptor.setInterceptorId( HASHING_PASSWORD_INTERCEPTOR_ID );

        // Interceptor FQCN
        hashingPasswordInterceptor.setInterceptorClassName( getFqcnForHashingMethod( getSelectedHashingMethod() ) );

        // Getting the order of the key derivation interceptor
        int keyDerivationInterceptorOrder = getKeyDerivationInterceptorOrder();

        // Assigning the order of the hashing password interceptor
        // It's order is: keyDerivationInterceptorOrder + 1
        hashingPasswordInterceptor.setInterceptorOrder( keyDerivationInterceptorOrder + 1 );

        // Updating the order of the interceptors after the key derivation interceptor
        for ( InterceptorBean interceptor : getDirectoryServiceBean().getInterceptors() )
        {
            if ( interceptor.getInterceptorOrder() > keyDerivationInterceptorOrder )
            {
                interceptor.setInterceptorOrder( interceptor.getInterceptorOrder() + 1 );
            }
        }

        // Adding the hashing password interceptor            
        getDirectoryServiceBean().addInterceptors( hashingPasswordInterceptor );

        return hashingPasswordInterceptor;
    }
    
    
    // ── Retraining The Vault Guard For A New Algorithm ────────────────────────
    // The security chief hands the vault guard a new manual — the algorithm
    // has changed, so the interceptor class name needs to be updated to match
    // whatever the user just selected in the combo box.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the password-hashing interceptor's class name to reflect the
     * currently selected hashing algorithm in the combo viewer.
     * Called whenever the user changes the algorithm drop-down while
     * server-side hashing is enabled.
     */
    private void updateHashingMethod()
    {
        // Getting the hashing password interceptor
        InterceptorBean hashingPasswordInterceptor = getHashingPasswordInterceptor();

        if ( hashingPasswordInterceptor != null )
        {
            // Updating the hashing method
            hashingPasswordInterceptor.setInterceptorClassName( getFqcnForHashingMethod( getSelectedHashingMethod() ) );
        }
    }


    // ── Translating An Algorithm Name To Its Interceptor Barracks ────────────
    // Each hashing algorithm is handled by a different interceptor class living
    // in a specific package — this method is the directory that maps algorithm
    // names (like SHA512) to the fully-qualified class names of their barracks.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Maps a {@link LdapSecurityConstants} hash method enum value to the
     * fully-qualified class name of the corresponding ApacheDS interceptor.
     *
     * <p>For example — picking the right specialist:</p>
     * <pre>
     *   HASH_METHOD_SSHA512
     *     -> "org.apache.directory.server.core.hash.Ssha512PasswordHashingInterceptor"
     * </pre>
     *
     * @param hashingMethod  the selected hashing algorithm
     * @return the FQCN of the corresponding interceptor class
     */
    private String getFqcnForHashingMethod( LdapSecurityConstants hashingMethod )
    {
        switch ( hashingMethod )
        {
            case HASH_METHOD_MD5:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_MD5;
                
            case HASH_METHOD_SMD5:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SMD5;
                
            case HASH_METHOD_CRYPT:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_CRYPT;
                
            case HASH_METHOD_SHA256:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA256;
                
            case HASH_METHOD_SSHA256:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA256;
                
            case HASH_METHOD_SHA384:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA384;
                
            case HASH_METHOD_SSHA384:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA384;
                
            case HASH_METHOD_SHA512:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA512;
                
            case HASH_METHOD_SSHA512:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA512;
                
            case HASH_METHOD_SHA:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SHA;
                
            case HASH_METHOD_SSHA:
            default:
                return HASHING_PASSWORD_INTERCEPTOR_FQCN_SSHA;
        }
    }
    
    


    // ── Reading Which Algorithm Is Currently On The Selector ─────────────────
    // The chief engineer glances at the algorithm selector dial and reads back
    // whichever value is currently showing — SHA, SSHA, MD5, or one of the
    // bigger siblings.
    // We get the current selection from the combo viewer and cast it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdapSecurityConstants} value currently selected in
     * the hashing method combo viewer.
     *
     * @return the selected hashing method, or {@code null} if nothing is selected
     */
    private LdapSecurityConstants getSelectedHashingMethod()
    {
        StructuredSelection selection = ( StructuredSelection ) hashingMethodComboViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            return ( LdapSecurityConstants ) selection.getFirstElement();
        }

        return null;
    }
    
    


    // ── Adding Or Removing A Protocol From The Allowed Frequency List ─────────
    // The comms officer updates the list of allowed transmission protocols —
    // adding the named one when we want to support it, or pulling it off the
    // list when we're retiring it. Both the LDAP and LDAPS transports share the
    // same enabled-protocols list, so we update both in one shot.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds or removes the given protocol string from the enabled-protocols
     * list on both the LDAP and LDAPS transport beans.
     *
     * @param enabled   {@code true} to add the protocol, {@code false} to remove it
     * @param protocol  the protocol name to add/remove (e.g., "TLSv1.2")
     */
    private void setProtocol( boolean enabled, String protocol )
    {
        if ( enabled )
        {
            // We have to compute the new list of enabled protocols
            List<String> enabledProtocols = getLdapTransportBean( TRANSPORT_ID_LDAP ).getEnabledProtocols();
            
            if ( enabledProtocols == null )
            {
                enabledProtocols = new ArrayList<>();
            }
            
            if ( !enabledProtocols.contains( protocol ) )
            {
                enabledProtocols.add( protocol );
            }
            
            getLdapTransportBean( TRANSPORT_ID_LDAP ).setEnabledProtocols( enabledProtocols );
            getLdapTransportBean( TRANSPORT_ID_LDAPS ).setEnabledProtocols( enabledProtocols );
        }
        else
        {
            // We have to compute the new list of enabled protocols
            List<String> enabledProtocols = getLdapTransportBean( TRANSPORT_ID_LDAP ).getEnabledProtocols();
            
            enabledProtocols.remove( protocol );
            getLdapTransportBean( TRANSPORT_ID_LDAP ).setEnabledProtocols( enabledProtocols );
            getLdapTransportBean( TRANSPORT_ID_LDAPS ).setEnabledProtocols( enabledProtocols );
        }
    }


    // ── Reading Which Realm Is Currently Highlighted ──────────────────────────
    // The operator glances at the realm table and reads back the currently
    // highlighted entry — if nothing is selected, they return null rather than
    // guessing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected SASL realm string from the table viewer.
     *
     * @return the selected realm name, or {@code null} if nothing is selected
     */
    private String getSelectedSaslRealms()
    {
        StructuredSelection selection = ( StructuredSelection ) saslRealmsTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            return ( String ) selection.getFirstElement();
        }

        return null;
    }


    // ── Editing An Existing Realm Record ──────────────────────────────────────
    // The registrar opens the dossier of the selected realm, lets the operator
    // update the name, then swaps the old entry for the new one in the server
    // bean and refreshes the display. Called from both the Edit button and a
    // double-click on the table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens an input dialog pre-populated with the currently selected SASL realm
     * name, lets the user edit it, then removes the old realm and adds the new
     * one to the server bean.
     * Shared by the Edit button listener and the double-click listener so both
     * trigger the same behaviour.
     */
    private void editSaslRealmsAction()
    {
        String selectedSaslRealms = getSelectedSaslRealms();

        if ( selectedSaslRealms != null )
        {
            InputDialog dialog = new InputDialog( editSaslRealmsButton.getShell(),
                Messages.getString( "LdapLdapsServersPage.Edit" ), //$NON-NLS-1$
                Messages.getString( "LdapLdapsServersPage.SaslRealms" ), //$NON-NLS-1$
                selectedSaslRealms, null );

            if ( dialog.open() == InputDialog.OK )
            {
                String newSaslRealms = dialog.getValue();

                getLdapServerBean().getLdapServerSaslRealms().remove( selectedSaslRealms );
                getLdapServerBean().addSaslRealms( newSaslRealms );

                saslRealmsTableViewer.refresh();
                saslRealmsTableViewer.setSelection( new StructuredSelection( newSaslRealms ) );

                setEditorDirty();
            }
        }
    }
}
