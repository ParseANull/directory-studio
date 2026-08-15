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
package org.apache.directory.studio.connection.core.io.api;


import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.directory.SearchControls;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

import org.apache.directory.api.ldap.codec.api.DefaultConfigurableBinaryAttributeDetector;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.filter.ExprNode;
import org.apache.directory.api.ldap.model.filter.FilterParser;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddRequestImpl;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.AliasDerefMode;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindRequestImpl;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.DeleteRequest;
import org.apache.directory.api.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.ModifyDnRequest;
import org.apache.directory.api.ldap.model.message.ModifyDnRequestImpl;
import org.apache.directory.api.ldap.model.message.ModifyDnResponse;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.ldap.client.api.SaslCramMd5Request;
import org.apache.directory.ldap.client.api.SaslDigestMd5Request;
import org.apache.directory.ldap.client.api.SaslGssApiRequest;
import org.apache.directory.ldap.client.api.exception.InvalidConnectionException;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;
import org.apache.directory.studio.connection.core.IAuthHandler;
import org.apache.directory.studio.connection.core.ICredentials;
import org.apache.directory.studio.connection.core.ILdapLogger;
import org.apache.directory.studio.connection.core.Messages;
import org.apache.directory.studio.connection.core.ReferralsInfo;
import org.apache.directory.studio.connection.core.io.ConnectionWrapper;
import org.apache.directory.studio.connection.core.io.ConnectionWrapperUtils;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.connection.core.io.StudioTrustManager;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.osgi.util.NLS;


// ── CLASS: DirectoryApiConnectionWrapper — THE FALCON'S ACTUAL HYPERDRIVE ENGINE ──
// ConnectionWrapper is the abstract contract (the blueprint).  This class is the
// real thing: the Millennium Falcon's actual hyperdrive unit, built from the
// Apache Directory API's LdapNetworkConnection.
// Every LDAP operation — connect, bind, search, modify, add, delete, rename,
// extended — is implemented here.  Each operation is packaged inside an
// InnerRunnable that runs on the current job thread with a cancel listener
// attached so the progress dialog can interrupt it.
// TLS is handled by injecting StudioTrustManager into the LdapConnectionConfig,
// which gives us our layered certificate trust check (permanent → session → JVM).
// Auth supports Anonymous, Simple, CRAM-MD5, DIGEST-MD5, and GSSAPI/Kerberos.
// Referrals on write operations are handled by checkAndHandleReferral(), which
// opens a connection to the referral target and re-fires the operation there.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The only production implementation of {@link ConnectionWrapper}.
 * Uses the Apache Directory API {@link LdapNetworkConnection} to communicate
 * with the LDAP server over TCP (plain, LDAPS, or StartTLS).
 *
 * <p>Key design points:</p>
 * <ul>
 *   <li>All operations run inside an {@link InnerRunnable} on the current job thread
 *       so they can be interrupted by the progress dialog's Cancel button.</li>
 *   <li>TLS certificate trust is handled by {@link StudioTrustManager}, which checks
 *       permanent/session trust stores before falling back to the JVM trust store
 *       and finally prompting the user.</li>
 *   <li>Authentication supports ANONYMOUS, SIMPLE, SASL_CRAM_MD5, SASL_DIGEST_MD5,
 *       and SASL_GSSAPI (Kerberos).  Credentials are obtained from the
 *       {@link IAuthHandler} registered with the plugin.</li>
 *   <li>Write operations (modify, rename, add, delete) check for a referral response
 *       and automatically re-issue the operation against the referral target.</li>
 *   <li>If the connection drops mid-session, {@link #checkConnectionAndRunAndMonitor}
 *       reconnects and retries once before giving up.</li>
 * </ul>
 * Think of this as the Falcon's real hyperdrive engine: it does all the actual work
 * that the {@link ConnectionWrapper} interface promises.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DirectoryApiConnectionWrapper implements ConnectionWrapper
{
    /** Monotonically increasing request number used to correlate log entries across operations. */
    private static int searchRequestNum = 0;

    /** The Studio connection model that owns this wrapper. */
    private Connection connection;

    /** The underlying Apache Directory API LDAP network connection. */
    private LdapNetworkConnection ldapConnection;

    /** Detector that tells the codec which attribute types carry binary (non-string) values. */
    private DefaultConfigurableBinaryAttributeDetector binaryAttributeDetector;

    /**
     * The thread currently executing an LDAP operation.
     * Stored so the cancel listener can interrupt it if the user clicks Cancel.
     */
    private Thread jobThread;


    // ── CONSTRUCTOR — BIND TO THE STUDIO CONNECTION ────────────────────────────────
    // The wrapper needs the connection model so it can read its parameters
    // (host, port, auth, encryption) and ask the auth handler for credentials.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link DirectoryApiConnectionWrapper} for the given connection.
     *
     * @param connection  The Studio connection model this wrapper implements.
     */
    public DirectoryApiConnectionWrapper( Connection connection )
    {
        this.connection = connection;
    }


    // ── CONNECT — FIRE UP THE HYPERDRIVE ──────────────────────────────────────────
    // We delegate to doConnect() and clean up on failure.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Establishes the TCP (and optionally TLS) connection to the directory server.
     * On failure, we disconnect and report the error to the monitor.
     */
    public void connect( StudioProgressMonitor monitor )
    {
        ldapConnection = null;
        jobThread = null;

        try
        {
            doConnect( monitor );
        }
        catch ( Exception e )
        {
            disconnect();
            monitor.reportError( e );
        }
    }


    // ── DO CONNECT — THE ACTUAL SOCKET + TLS SETUP ────────────────────────────────
    // We build the LdapConnectionConfig (host, port, timeout, TLS settings),
    // inject our StudioTrustManager if TLS is requested, then create the
    // LdapNetworkConnection inside an InnerRunnable.  We use a temp variable
    // during the connection process so other threads can't see a half-open connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the {@link LdapConnectionConfig}, sets up TLS trust managers if needed,
     * and creates the {@link LdapNetworkConnection} (including StartTLS upgrade if configured).
     * Uses a local temp variable during construction to avoid exposing a partially connected
     * socket to other threads.
     *
     * @param monitor  Progress monitor for cancellation and error reporting.
     * @throws Exception  If connection or TLS setup fails.
     */
    private void doConnect( final StudioProgressMonitor monitor ) throws Exception
    {
        ldapConnection = null;

        LdapConnectionConfig ldapConnectionConfig = new LdapConnectionConfig();
        ldapConnectionConfig.setLdapHost( connection.getHost() );
        ldapConnectionConfig.setLdapPort( connection.getPort() );

        long timeoutMillis = connection.getTimeoutMillis();

        if ( timeoutMillis < 0 )
        {
            timeoutMillis = 30000L;
        }

        ldapConnectionConfig.setTimeout( timeoutMillis );

        binaryAttributeDetector = new DefaultConfigurableBinaryAttributeDetector();
        ldapConnectionConfig.setBinaryAttributeDetector( binaryAttributeDetector );

        AtomicReference<StudioTrustManager> studioTrustmanager = new AtomicReference<>();

        if ( ( connection.getEncryptionMethod() == EncryptionMethod.LDAPS )
            || ( connection.getEncryptionMethod() == EncryptionMethod.START_TLS ) )
        {
            ldapConnectionConfig.setUseSsl( connection.getEncryptionMethod() == EncryptionMethod.LDAPS );
            ldapConnectionConfig.setUseTls( connection.getEncryptionMethod() == EncryptionMethod.START_TLS );

            try
            {
                // get default trust managers (using JVM "cacerts" key store)
                TrustManagerFactory factory = TrustManagerFactory.getInstance( TrustManagerFactory
                    .getDefaultAlgorithm() );
                factory.init( ( KeyStore ) null );
                TrustManager[] defaultTrustManagers = factory.getTrustManagers();

                // create wrappers around the trust managers
                StudioTrustManager[] trustManagers = new StudioTrustManager[defaultTrustManagers.length];

                for ( int i = 0; i < defaultTrustManagers.length; i++ )
                {
                    trustManagers[i] = new StudioTrustManager( ( X509TrustManager ) defaultTrustManagers[i] );
                    trustManagers[i].setHost( connection.getHost() );
                }
                studioTrustmanager.set( trustManagers[0] );

                ldapConnectionConfig.setTrustManagers( trustManagers );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException( e );
            }
        }

        InnerRunnable runnable = new InnerRunnable()
        {
            public void run()
            {
                /*
                 * Use local temp variable while the connection is being established and secured.
                 * This process can take a while and the user might be asked to inspect the server
                 * certificate. During that process the connection must not be used.
                 */
                LdapNetworkConnection ldapConnectionUnderConstruction = null;
                try
                {
                    // Set lower timeout for connecting
                    long oldTimeout = ldapConnectionConfig.getTimeout();
                    ldapConnectionConfig.setTimeout( Math.min( oldTimeout, 5000L ) );

                    // Connecting
                    ldapConnectionUnderConstruction = new LdapNetworkConnection( ldapConnectionConfig );
                    ldapConnectionUnderConstruction.connect();

                    // DIRSTUDIO-1219: Establish TLS layer if TLS is enabled and SSL is not
                    if ( ldapConnectionConfig.isUseTls() && !ldapConnectionConfig.isUseSsl() )
                    {
                        ldapConnectionUnderConstruction.startTls();
                    }

                    // Set original timeout again
                    ldapConnectionConfig.setTimeout( oldTimeout );
                    ldapConnectionUnderConstruction.setTimeOut( oldTimeout );

                    // Now set the LDAP connection once the (optional) security layer is in place
                    ldapConnection = ldapConnectionUnderConstruction;

                    if ( !isConnected() )
                    {
                        throw new Exception( Messages.DirectoryApiConnectionWrapper_UnableToConnect );
                    }

                    // DIRSTUDIO-1219: Verify secure connection if ldaps:// or StartTLS is configured
                    if ( ldapConnectionConfig.isUseTls() || ldapConnectionConfig.isUseSsl() )
                    {
                        if ( !isSecured() )
                        {
                            throw new Exception( Messages.DirectoryApiConnectionWrapper_UnsecuredConnection );
                        }
                    }
                }
                catch ( Exception e )
                {
                    exception = toStudioLdapException( e );

                    try
                    {
                        if ( ldapConnectionUnderConstruction != null )
                        {
                            ldapConnectionUnderConstruction.close();
                        }
                    }
                    catch ( Exception exception )
                    {
                        // Nothing to do
                    }
                    finally
                    {
                        ldapConnection = null;
                        binaryAttributeDetector = null;
                    }
                }
            }
        };

        runAndMonitor( runnable, monitor );

        if ( runnable.getException() != null )
        {
            throw runnable.getException();
        }
    }


    // ── DISCONNECT — CUT POWER TO THE ENGINES ─────────────────────────────────────
    // We interrupt the job thread (if there is one), close the LDAP connection,
    // and clear both fields so the wrapper knows it's dead.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Interrupts the current job thread (if any) and closes the underlying
     * {@link LdapNetworkConnection}.
     */
    public void disconnect()
    {
        if ( jobThread != null )
        {
            Thread t = jobThread;
            jobThread = null;
            t.interrupt();
        }
        if ( ldapConnection != null )
        {
            try
            {
                ldapConnection.close();
            }
            catch ( Exception e )
            {
                // ignore
            }
            ldapConnection = null;
            binaryAttributeDetector = null;
        }
    }


    // ── BIND — AUTHENTICATE AGAINST THE SERVER ────────────────────────────────────
    // We delegate to doBind().  On failure, we disconnect and report the error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Sends the LDAP bind request using the connection's configured authentication method.
     * On failure, disconnects and reports the error to the monitor.
     */
    public void bind( StudioProgressMonitor monitor )
    {
        try
        {
            doBind( monitor );
        }
        catch ( Exception e )
        {
            disconnect();
            monitor.reportError( e );
        }
    }


    // ── BIND SIMPLE — SEND A PLAIN BIND REQUEST ───────────────────────────────────
    // Convenience method that builds a simple-auth BindRequest and fires it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sends a simple authentication bind request with the given principal and password.
     *
     * @param bindPrincipal  The bind DN string.
     * @param bindPassword   The bind password.
     * @return  The server's {@link BindResponse}.
     * @throws LdapException  If the bind request fails.
     */
    private BindResponse bindSimple( String bindPrincipal, String bindPassword ) throws LdapException
    {
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName( bindPrincipal );
        bindRequest.setCredentials( bindPassword );

        return ldapConnection.bind( bindRequest );
    }


    // ── DO BIND — FULL AUTHENTICATION LOGIC ───────────────────────────────────────
    // We switch on the auth method: ANONYMOUS, SIMPLE, CRAM-MD5, DIGEST-MD5,
    // or GSSAPI.  For everything except ANONYMOUS, we ask the IAuthHandler for
    // credentials first.  For GSSAPI, we also configure the JAAS login module
    // from preferences and optionally inject the KRB5 realm/KDC manually.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Implements the full bind sequence.
     * Selects the auth method from the connection parameters, obtains credentials
     * from the {@link IAuthHandler}, and sends the appropriate bind request.
     * For GSSAPI, configures the JAAS {@link Configuration} via {@link InnerConfiguration}.
     *
     * @param monitor  Progress monitor for cancellation and error reporting.
     * @throws Exception  If the connection is not open or binding fails.
     */
    private void doBind( final StudioProgressMonitor monitor ) throws Exception
    {
        if ( isConnected() )
        {
            InnerRunnable runnable = new InnerRunnable()
            {
                public void run()
                {
                    try
                    {
                        BindResponse bindResponse = null;

                        // No Authentication
                        if ( connection.getConnectionParameter()
                            .getAuthMethod() == ConnectionParameter.AuthenticationMethod.NONE )
                        {
                            BindRequest bindRequest = new BindRequestImpl();
                            bindResponse = ldapConnection.bind( bindRequest );
                        }
                        else
                        {
                            // Setup credentials
                            IAuthHandler authHandler = ConnectionCorePlugin.getDefault().getAuthHandler();
                            if ( authHandler == null )
                            {
                                Exception exception = new Exception( Messages.model__no_auth_handler );
                                monitor.setCanceled( true );
                                monitor.reportError( Messages.model__no_auth_handler, exception );
                                throw exception;
                            }
                            ICredentials credentials = authHandler
                                .getCredentials( connection.getConnectionParameter() );
                            if ( credentials == null )
                            {
                                Exception exception = new Exception();
                                monitor.setCanceled( true );
                                monitor.reportError( Messages.model__no_credentials, exception );
                                throw exception;
                            }
                            if ( credentials.getBindPrincipal() == null || credentials.getBindPassword() == null )
                            {
                                Exception exception = new Exception( Messages.model__no_credentials );
                                monitor.reportError( Messages.model__no_credentials, exception );
                                throw exception;
                            }
                            String bindPrincipal = credentials.getBindPrincipal();
                            String bindPassword = credentials.getBindPassword();

                            switch ( connection.getConnectionParameter().getAuthMethod() )
                            {
                                case SIMPLE:
                                    // Simple Authentication
                                    bindResponse = bindSimple( bindPrincipal, bindPassword );
                                    break;

                                case SASL_CRAM_MD5:
                                    // CRAM-MD5 Authentication
                                    SaslCramMd5Request cramMd5Request = new SaslCramMd5Request();
                                    cramMd5Request.setUsername( bindPrincipal );
                                    cramMd5Request.setCredentials( bindPassword );
                                    cramMd5Request
                                        .setQualityOfProtection( connection.getConnectionParameter().getSaslQop() );
                                    cramMd5Request.setSecurityStrength( connection.getConnectionParameter()
                                        .getSaslSecurityStrength() );
                                    cramMd5Request.setMutualAuthentication( connection.getConnectionParameter()
                                        .isSaslMutualAuthentication() );

                                    bindResponse = ldapConnection.bind( cramMd5Request );
                                    break;

                                case SASL_DIGEST_MD5:
                                    // DIGEST-MD5 Authentication
                                    SaslDigestMd5Request digestMd5Request = new SaslDigestMd5Request();
                                    digestMd5Request.setUsername( bindPrincipal );
                                    digestMd5Request.setCredentials( bindPassword );
                                    digestMd5Request.setRealmName( connection.getConnectionParameter().getSaslRealm() );
                                    digestMd5Request.setQualityOfProtection( connection.getConnectionParameter()
                                        .getSaslQop() );
                                    digestMd5Request.setSecurityStrength( connection.getConnectionParameter()
                                        .getSaslSecurityStrength() );
                                    digestMd5Request.setMutualAuthentication( connection.getConnectionParameter()
                                        .isSaslMutualAuthentication() );

                                    bindResponse = ldapConnection.bind( digestMd5Request );
                                    break;

                                case SASL_GSSAPI:
                                    // GSSAPI Authentication
                                    SaslGssApiRequest gssApiRequest = new SaslGssApiRequest();

                                    Preferences preferences = ConnectionCorePlugin.getDefault().getPluginPreferences();
                                    boolean useKrb5SystemProperties = preferences
                                        .getBoolean( ConnectionCoreConstants.PREFERENCE_USE_KRB5_SYSTEM_PROPERTIES );
                                    String krb5LoginModule = preferences
                                        .getString( ConnectionCoreConstants.PREFERENCE_KRB5_LOGIN_MODULE );

                                    if ( !useKrb5SystemProperties )
                                    {
                                        gssApiRequest.setUsername( bindPrincipal );
                                        gssApiRequest.setCredentials( bindPassword );
                                        gssApiRequest.setQualityOfProtection( connection
                                            .getConnectionParameter().getSaslQop() );
                                        gssApiRequest.setSecurityStrength( connection
                                            .getConnectionParameter()
                                            .getSaslSecurityStrength() );
                                        gssApiRequest.setMutualAuthentication( connection
                                            .getConnectionParameter()
                                            .isSaslMutualAuthentication() );
                                        gssApiRequest
                                            .setLoginModuleConfiguration( new InnerConfiguration(
                                                krb5LoginModule ) );

                                        switch ( connection.getConnectionParameter().getKrb5Configuration() )
                                        {
                                            case FILE:
                                                gssApiRequest.setKrb5ConfFilePath( connection.getConnectionParameter()
                                                    .getKrb5ConfigurationFile() );
                                                break;
                                            case MANUAL:
                                                gssApiRequest.setRealmName( connection.getConnectionParameter()
                                                    .getKrb5Realm() );
                                                gssApiRequest.setKdcHost( connection.getConnectionParameter()
                                                    .getKrb5KdcHost() );
                                                gssApiRequest.setKdcPort( connection.getConnectionParameter()
                                                    .getKrb5KdcPort() );
                                                break;
                                            default:
                                                break;
                                        }
                                    }

                                    bindResponse = ldapConnection.bind( gssApiRequest );
                                    break;
                            }
                        }

                        checkResponse( bindResponse );
                    }
                    catch ( Exception e )
                    {
                        exception = toStudioLdapException( e );
                    }
                }
            };

            runAndMonitor( runnable, monitor );

            if ( runnable.getException() != null )
            {
                throw runnable.getException();
            }
        }
        else
        {
            throw new Exception( Messages.DirectoryApiConnectionWrapper_NoConnection );
        }
    }


    // ── UNBIND — DELEGATE TO DISCONNECT ───────────────────────────────────────────
    // The Apache Directory API's LdapNetworkConnection closes the socket on disconnect,
    // so we just use that.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Sends an LDAP unbind by closing the underlying connection.
     */
    public void unbind()
    {
        disconnect();
    }


    // ── IS CONNECTED — CHECK IF THE SOCKET IS OPEN ────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code true} if the underlying {@link LdapNetworkConnection} is open.
     */
    public boolean isConnected()
    {
        return ( ldapConnection != null && ldapConnection.isConnected() );
    }


    // ── IS SECURED — CHECK IF TLS IS ACTIVE ───────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code true} if the connection is open and TLS-secured.
     */
    public boolean isSecured()
    {
        return isConnected() && ldapConnection.isSecured();
    }


    // ── GET SSL SESSION — RETURN THE TLS SESSION ──────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns the {@link SSLSession} from the underlying connection, or {@code null}.
     */
    @Override
    public SSLSession getSslSession()
    {
        return isConnected() ? ldapConnection.getSslSession() : null;
    }


    // ── SET BINARY ATTRIBUTES — CONFIGURE THE CODEC ───────────────────────────────
    // We clear the current list and re-populate it from the given collection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Configures the {@link DefaultConfigurableBinaryAttributeDetector} with the
     * given set of attribute type names.
     */
    public void setBinaryAttributes( Collection<String> binaryAttributes )
    {
        if ( binaryAttributeDetector != null )
        {
            // Clear the initial list
            binaryAttributeDetector.setBinaryAttributes();

            // Add each binary attribute
            for ( String binaryAttribute : binaryAttributes )
            {
                binaryAttributeDetector.addBinaryAttribute( binaryAttribute );
            }
        }
    }


    // ── SEARCH — FIRE THE SENSOR ARRAY ────────────────────────────────────────────
    // We build a SearchRequest, open the cursor in an InnerRunnable, and wrap
    // the cursor in a StudioSearchResultEnumeration (which handles lazy pull
    // and referral following).  We log the request via all registered ILdapLoggers.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Sends the LDAP search request and returns a {@link StudioSearchResultEnumeration}
     * for lazy result iteration with referral handling.
     * Returns {@code null} on error (the monitor will have the error details).
     */
    public StudioSearchResultEnumeration search( final String searchBase, final String filter,
        final SearchControls searchControls, final AliasDereferencingMethod aliasesDereferencingMethod,
        final ReferralHandlingMethod referralsHandlingMethod, final Control[] controls,
        final StudioProgressMonitor monitor, final ReferralsInfo referralsInfo )
    {
        final long requestNum = searchRequestNum++;

        InnerRunnable runnable = new InnerRunnable()
        {
            public void run()
            {
                try
                {
                    // Preparing the search request
                    SearchRequest request = new SearchRequestImpl();
                    request.setBase( new Dn( searchBase ) );
                    ExprNode node = FilterParser.parse( filter, true );
                    request.setFilter( node );
                    request.setScope( convertSearchScope( searchControls ) );
                    if ( searchControls.getReturningAttributes() != null )
                    {
                        request.addAttributes( searchControls.getReturningAttributes() );
                    }
                    if ( controls != null )
                    {
                        request.addAllControls( controls );
                    }
                    request.setSizeLimit( searchControls.getCountLimit() );
                    request.setTimeLimit( searchControls.getTimeLimit() );
                    request.setDerefAliases( convertAliasDerefMode( aliasesDereferencingMethod ) );

                    // Performing the search operation
                    SearchCursor cursor = ldapConnection.search( request );

                    // Returning the result of the search
                    searchResultEnumeration = new StudioSearchResultEnumeration( connection, cursor, searchBase, filter,
                        searchControls, aliasesDereferencingMethod, referralsHandlingMethod, controls, requestNum,
                        monitor, referralsInfo );
                }
                catch ( Exception e )
                {
                    exception = toStudioLdapException( e );
                }

                for ( ILdapLogger logger : getLdapLoggers() )
                {
                    if ( searchResultEnumeration != null )
                    {
                        logger.logSearchRequest( connection, searchBase, filter, searchControls,
                            aliasesDereferencingMethod, controls, requestNum, exception );
                    }
                    else
                    {
                        logger.logSearchRequest( connection, searchBase, filter, searchControls,
                            aliasesDereferencingMethod, controls, requestNum, exception );
                        logger.logSearchResultDone( connection, 0, requestNum, exception );
                    }
                }
            }
        };

        try
        {
            checkConnectionAndRunAndMonitor( runnable, monitor );
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
            return null;
        }

        if ( runnable.isCanceled() )
        {
            monitor.setCanceled( true );
        }
        if ( runnable.getException() != null )
        {
            monitor.reportError( runnable.getException() );
            return null;
        }
        else
        {
            return runnable.getResult();
        }
    }


    // ── CONVERT SEARCH SCOPE — MAP JNDI SCOPE INT TO APACHE DIR API ENUM ──────────
    /**
     * Converts a JNDI {@link SearchControls} scope constant to the Apache Directory API
     * {@link SearchScope} enum.
     *
     * @param searchControls  The search controls carrying the scope constant.
     * @return  The equivalent {@link SearchScope}.
     */
    private SearchScope convertSearchScope( SearchControls searchControls )
    {
        int scope = searchControls.getSearchScope();
        if ( scope == SearchControls.OBJECT_SCOPE )
        {
            return SearchScope.OBJECT;
        }
        else if ( scope == SearchControls.ONELEVEL_SCOPE )
        {
            return SearchScope.ONELEVEL;
        }
        else if ( scope == SearchControls.SUBTREE_SCOPE )
        {
            return SearchScope.SUBTREE;
        }
        else
        {
            return SearchScope.SUBTREE;
        }
    }


    // ── CONVERT ALIAS DEREF MODE — MAP STUDIO ENUM TO APACHE DIR API ENUM ─────────
    /**
     * Converts the Studio {@link AliasDereferencingMethod} to the Apache Directory API
     * {@link AliasDerefMode}.
     *
     * @param aliasesDereferencingMethod  The Studio alias dereferencing method.
     * @return  The equivalent {@link AliasDerefMode}.
     */
    private AliasDerefMode convertAliasDerefMode( AliasDereferencingMethod aliasesDereferencingMethod )
    {
        switch ( aliasesDereferencingMethod )
        {
            case ALWAYS:
                return AliasDerefMode.DEREF_ALWAYS;
            case FINDING:
                return AliasDerefMode.DEREF_FINDING_BASE_OBJ;
            case NEVER:
                return AliasDerefMode.NEVER_DEREF_ALIASES;
            case SEARCH:
                return AliasDerefMode.DEREF_IN_SEARCHING;
            default:
                return AliasDerefMode.DEREF_ALWAYS;
        }
    }


    // ── MODIFY ENTRY — SEND AN LDAP MODIFY REQUEST ────────────────────────────────
    // Read-only connections are rejected immediately.  Otherwise we build a
    // ModifyRequest, fire it, check for referral (and re-fire if needed), check
    // the response code, and log via all registered ILdapLoggers.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Sends an LDAP modify request.
     * Rejects read-only connections.  Logs the operation via {@link ILdapLogger}s.
     * Automatically handles referral responses by re-issuing the operation on the
     * referral target.
     */
    public void modifyEntry( final Dn dn, final Collection<Modification> modifications, final Control[] controls,
        final StudioProgressMonitor monitor, final ReferralsInfo referralsInfo )
    {
        if ( connection.isReadOnly() )
        {
            monitor
                .reportError(
                    new Exception( NLS.bind( Messages.error__connection_is_readonly, connection.getName() ) ) );
            return;
        }

        InnerRunnable runnable = new InnerRunnable()
        {
            public void run()
            {
                try
                {
                    // Preparing the modify request
                    ModifyRequest request = new ModifyRequestImpl();
                    request.setName( dn );
                    if ( modifications != null )
                    {
                        for ( Modification modification : modifications )
                        {
                            request.addModification( modification );
                        }
                    }
                    if ( controls != null )
                    {
                        request.addAllControls( controls );
                    }

                    // Performing the modify operation
                    ModifyResponse modifyResponse = ldapConnection.modify( request );

                    // Handle referral
                    ReferralHandlingDataConsumer consumer = referralHandlingData -> referralHandlingData.connectionWrapper
                        .modifyEntry( new Dn( referralHandlingData.referralDn ), modifications, controls, monitor,
                            referralHandlingData.newReferralsInfo );

                    if ( checkAndHandleReferral( modifyResponse, monitor, referralsInfo, consumer ) )
                    {
                        return;
                    }

                    // Checking the response
                    checkResponse( modifyResponse );
                }
                catch ( Exception e )
                {
                    exception = toStudioLdapException( e );
                }

                for ( ILdapLogger logger : getLdapLoggers() )
                {
                    logger.logChangetypeModify( connection, dn, modifications, controls, exception );
                }
            }
        };

        try
        {
            checkConnectionAndRunAndMonitor( runnable, monitor );
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }

        if ( runnable.isCanceled() )
        {
            monitor.setCanceled( true );
        }
        if ( runnable.getException() != null )
        {
            monitor.reportError( runnable.getException() );
        }
    }


    // ── RENAME ENTRY — SEND AN LDAP MODDN REQUEST ─────────────────────────────────
    /**
     * {@inheritDoc}
     * Sends an LDAP modDN (rename or move) request.
     * Rejects read-only connections.  Handles referrals.  Logs via {@link ILdapLogger}s.
     */
    public void renameEntry( final Dn oldDn, final Dn newDn, final boolean deleteOldRdn,
        final Control[] controls, final StudioProgressMonitor monitor, final ReferralsInfo referralsInfo )
    {
        if ( connection.isReadOnly() )
        {
            monitor
                .reportError(
                    new Exception( NLS.bind( Messages.error__connection_is_readonly, connection.getName() ) ) );
            return;
        }

        InnerRunnable runnable = new InnerRunnable()
        {
            public void run()
            {
                try
                {
                    // Preparing the rename request
                    ModifyDnRequest request = new ModifyDnRequestImpl();
                    request.setName( oldDn );
                    request.setDeleteOldRdn( deleteOldRdn );
                    request.setNewRdn( newDn.getRdn() );
                    request.setNewSuperior( newDn.getParent() );
                    if ( controls != null )
                    {
                        request.addAllControls( controls );
                    }

                    // Performing the rename operation
                    ModifyDnResponse modifyDnResponse = ldapConnection.modifyDn( request );

                    // Handle referral
                    ReferralHandlingDataConsumer consumer = referralHandlingData -> referralHandlingData.connectionWrapper
                        .renameEntry( oldDn, newDn, deleteOldRdn, controls,
                            monitor, referralHandlingData.newReferralsInfo );

                    if ( checkAndHandleReferral( modifyDnResponse, monitor, referralsInfo, consumer ) )
                    {
                        return;
                    }

                    // Checking the response
                    checkResponse( modifyDnResponse );
                }
                catch ( Exception e )
                {
                    exception = toStudioLdapException( e );
                }

                for ( ILdapLogger logger : getLdapLoggers() )
                {
                    logger.logChangetypeModDn( connection, oldDn, newDn, deleteOldRdn, controls, exception );
                }
            }
        };

        try
        {
            checkConnectionAndRunAndMonitor( runnable, monitor );
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }

        if ( runnable.isCanceled() )
        {
            monitor.setCanceled( true );
        }
        if ( runnable.getException() != null )
        {
            monitor.reportError( runnable.getException() );
        }
    }


    // ── CREATE ENTRY — SEND AN LDAP ADD REQUEST ────────────────────────────────────
    /**
     * {@inheritDoc}
     * Sends an LDAP add request to create the given entry.
     * Rejects read-only connections.  Handles referrals.  Logs via {@link ILdapLogger}s.
     */
    public void createEntry( final Entry entry, final Control[] controls,
        final StudioProgressMonitor monitor, final ReferralsInfo referralsInfo )
    {
        if ( connection.isReadOnly() )
        {
            monitor
                .reportError(
                    new Exception( NLS.bind( Messages.error__connection_is_readonly, connection.getName() ) ) );
            return;
        }

        InnerRunnable runnable = new InnerRunnable()
        {
            public void run()
            {
                try
                {
                    // Preparing the add request
                    AddRequest request = new AddRequestImpl();
                    request.setEntry( entry );
                    if ( controls != null )
                    {
                        request.addAllControls( controls );
                    }

                    // Performing the add operation
                    AddResponse addResponse = ldapConnection.add( request );

                    // Handle referral
                    ReferralHandlingDataConsumer consumer = referralHandlingData -> {
                        Entry entryWithReferralDn = entry.clone();
                        entryWithReferralDn.setDn( referralHandlingData.referralDn );
                        referralHandlingData.connectionWrapper.createEntry( entryWithReferralDn,
                            controls, monitor, referralHandlingData.newReferralsInfo );
                    };

                    if ( checkAndHandleReferral( addResponse, monitor, referralsInfo, consumer ) )
                    {
                        return;
                    }

                    // Checking the response
                    checkResponse( addResponse );
                }
                catch ( Exception e )
                {
                    exception = toStudioLdapException( e );
                }

                for ( ILdapLogger logger : getLdapLoggers() )
                {
                    logger.logChangetypeAdd( connection, entry, controls, exception );
                }
            }
        };

        try
        {
            checkConnectionAndRunAndMonitor( runnable, monitor );
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }

        if ( runnable.isCanceled() )
        {
            monitor.setCanceled( true );
        }
        if ( runnable.getException() != null )
        {
            monitor.reportError( runnable.getException() );
        }
    }


    // ── DELETE ENTRY — SEND AN LDAP DELETE REQUEST ────────────────────────────────
    /**
     * {@inheritDoc}
     * Sends an LDAP delete request for the given DN.
     * Rejects read-only connections.  Handles referrals.  Logs via {@link ILdapLogger}s.
     */
    public void deleteEntry( final Dn dn, final Control[] controls, final StudioProgressMonitor monitor,
        final ReferralsInfo referralsInfo )
    {
        if ( connection.isReadOnly() )
        {
            monitor
                .reportError(
                    new Exception( NLS.bind( Messages.error__connection_is_readonly, connection.getName() ) ) );
            return;
        }

        InnerRunnable runnable = new InnerRunnable()
        {
            public void run()
            {
                try
                {
                    // Preparing the delete request
                    DeleteRequest request = new DeleteRequestImpl();
                    request.setName( dn );
                    if ( controls != null )
                    {
                        request.addAllControls( controls );
                    }

                    // Performing the delete operation
                    DeleteResponse deleteResponse = ldapConnection.delete( request );

                    // Handle referral
                    ReferralHandlingDataConsumer consumer = referralHandlingData -> referralHandlingData.connectionWrapper
                        .deleteEntry( new Dn( referralHandlingData.referralDn ), controls, monitor,
                            referralHandlingData.newReferralsInfo );

                    if ( checkAndHandleReferral( deleteResponse, monitor, referralsInfo, consumer ) )
                    {
                        return;
                    }

                    // Checking the response
                    checkResponse( deleteResponse );
                }
                catch ( Exception e )
                {
                    exception = toStudioLdapException( e );
                }

                for ( ILdapLogger logger : getLdapLoggers() )
                {
                    logger.logChangetypeDelete( connection, dn, controls, exception );
                }
            }
        };

        try
        {
            checkConnectionAndRunAndMonitor( runnable, monitor );
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }

        if ( runnable.isCanceled() )
        {
            monitor.setCanceled( true );
        }
        if ( runnable.getException() != null )
        {
            monitor.reportError( runnable.getException() );
        }
    }


    // ── EXTENDED — SEND AN LDAP EXTENDED OPERATION ────────────────────────────────
    /**
     * {@inheritDoc}
     * Sends an LDAP extended operation and returns the server's response.
     * Rejects read-only connections.
     */
    @Override
    public ExtendedResponse extended( ExtendedRequest request, StudioProgressMonitor monitor )
    {
        if ( connection.isReadOnly() )
        {
            monitor
                .reportError(
                    new Exception( NLS.bind( Messages.error__connection_is_readonly, connection.getName() ) ) );
            return null;
        }

        ExtendedResponse[] outerResponse = new ExtendedResponse[1];

        InnerRunnable runnable = new InnerRunnable()
        {
            public void run()
            {
                try
                {
                    ExtendedResponse response = ldapConnection.extended( request );
                    outerResponse[0] = response;

                    // TODO: handle referrals?

                    // Checking the response
                    checkResponse( response );
                }
                catch ( Exception e )
                {
                    exception = toStudioLdapException( e );
                }

                for ( ILdapLogger logger : getLdapLoggers() )
                {
                }
            }
        };

        try
        {
            checkConnectionAndRunAndMonitor( runnable, monitor );
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }

        if ( runnable.isCanceled() )
        {
            monitor.setCanceled( true );
        }
        if ( runnable.getException() != null )
        {
            monitor.reportError( runnable.getException() );
        }

        return outerResponse[0];
    }


    // ── INNER CLASS: InnerRunnable — THE MISSION PACKET ───────────────────────────
    // Each LDAP operation is packaged into an InnerRunnable: the run() logic,
    // plus storage for the result (search enumeration), any exception, and a
    // cancel flag.  The outer method creates one, calls runAndMonitor(), and
    // then checks the stored state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Abstract base class for all per-operation runnables.
     * Subclasses implement {@link #run()} to perform the actual LDAP operation.
     * The result, exception, and cancel flag are stored in the fields so the
     * outer method can inspect them after {@link #runAndMonitor} returns.
     */
    abstract class InnerRunnable implements Runnable
    {
        /** The search result enumeration produced by a search operation; null for write ops. */
        protected StudioSearchResultEnumeration searchResultEnumeration = null;
        /** The exception thrown during the operation, if any. */
        protected StudioLdapException exception = null;
        /** {@code true} if the operation was cancelled. */
        protected boolean canceled = false;


        /**
         * Returns the exception thrown during the operation, or {@code null} on success.
         *
         * @return  The {@link StudioLdapException}, or {@code null}.
         */
        public Exception getException()
        {
            return exception;
        }


        /**
         * Returns the search result enumeration produced by a search operation,
         * or {@code null} for write operations.
         *
         * @return  The {@link StudioSearchResultEnumeration}, or {@code null}.
         */
        public StudioSearchResultEnumeration getResult()
        {
            return searchResultEnumeration;
        }


        /**
         * Returns {@code true} if the operation was cancelled.
         *
         * @return  {@code true} if cancelled.
         */
        public boolean isCanceled()
        {
            return canceled;
        }


        /**
         * Resets the runnable state for a retry.
         */
        public void reset()
        {
            searchResultEnumeration = null;
            exception = null;
            canceled = false;
        }
    }


    // ── REFERRAL HANDLING DATA CONSUMER — THE REDIRECT LAMBDA TYPE ────────────────
    /**
     * Functional interface whose {@link #accept} method re-issues the current
     * LDAP operation against a referral target.
     */
    @FunctionalInterface
    private interface ReferralHandlingDataConsumer
    {
        /**
         * Re-issues the LDAP operation using the referral connection and DN.
         *
         * @param t  The referral handling data containing the connection and DN.
         * @throws LdapException  If the re-issued operation fails.
         */
        void accept( ReferralHandlingData t ) throws LdapException;
    }


    // ── CHECK AND HANDLE REFERRAL — DETECT REFERRAL RESULT CODE AND RE-FIRE ────────
    // When a write operation response has result code REFERRAL, we use
    // ConnectionWrapperUtils to get a live connection for the referral target,
    // extract the referral DN, and call the consumer lambda to re-issue the op.
    // Returns true if we handled the referral (caller should return without logging).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Inspects the given response for a {@link ResultCodeEnum#REFERRAL} result code.
     * If found, resolves the referral connection, extracts the target DN, and calls
     * the consumer to re-issue the operation on the referral target.
     * Returns {@code true} if the referral was handled (the caller should return immediately).
     *
     * @param response       The LDAP response to inspect.
     * @param monitor        Progress monitor for cancellation.
     * @param referralsInfo  Referral tracking context.
     * @param consumer       Lambda that re-issues the operation against the referral target.
     * @return  {@code true} if a referral was detected and handled.
     * @throws LdapException  If re-issuing the operation fails.
     */
    private boolean checkAndHandleReferral( ResultResponse response, StudioProgressMonitor monitor,
        ReferralsInfo referralsInfo, ReferralHandlingDataConsumer consumer ) throws LdapException
    {
        if ( response == null )
        {
            return false;
        }

        LdapResult ldapResult = response.getLdapResult();
        if ( ldapResult == null || !ResultCodeEnum.REFERRAL.equals( ldapResult.getResultCode() ) )
        {
            return false;
        }

        if ( referralsInfo == null )
        {
            referralsInfo = new ReferralsInfo( true );
        }

        Referral referral = ldapResult.getReferral();
        referralsInfo.addReferral( referral );
        Referral nextReferral = referralsInfo.getNextReferral();

        Connection referralConnection = ConnectionWrapperUtils.getReferralConnection( nextReferral, monitor, this );
        if ( referralConnection == null )
        {
            monitor.setCanceled( true );
            return true;
        }

        List<String> urls = new ArrayList<>( referral.getLdapUrls() );
        String referralDn = new LdapUrl( urls.get( 0 ) ).getDn().getName();
        ReferralHandlingData referralHandlingData = new ReferralHandlingData( referralConnection.getConnectionWrapper(),
            referralDn, referralsInfo );
        consumer.accept( referralHandlingData );

        return true;
    }


    // ── INNER CLASS: ReferralHandlingData — THE REFERRAL REDIRECT PACKET ──────────
    /**
     * Simple data holder for referral re-dispatch: carries the target wrapper,
     * the resolved referral DN, and the referral tracking context.
     */
    static class ReferralHandlingData
    {
        /** The connection wrapper to use for the re-issued operation. */
        ConnectionWrapper connectionWrapper;
        /** The DN to use for the re-issued operation, extracted from the referral URL. */
        String referralDn;
        /** Referral tracking context passed to the re-issued operation. */
        ReferralsInfo newReferralsInfo;


        /**
         * Creates a new {@link ReferralHandlingData}.
         *
         * @param connectionWrapper  The referral connection wrapper.
         * @param referralDn         The target DN from the referral URL.
         * @param newReferralsInfo   Referral tracking context.
         */
        ReferralHandlingData( ConnectionWrapper connectionWrapper, String referralDn, ReferralsInfo newReferralsInfo )
        {
            this.connectionWrapper = connectionWrapper;
            this.referralDn = referralDn;
            this.newReferralsInfo = newReferralsInfo;
        }
    }


    // ── CHECK CONNECTION AND RUN AND MONITOR — ENSURE LIVE + RETRY ONCE ───────────
    // Before any operation, we make sure the connection is live.  If not, we
    // reconnect and re-bind.  We run the operation once; if it throws
    // InvalidConnectionException, we reconnect and retry once.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Ensures the connection is live before running the given runnable.
     * If the connection is not open, reconnects and re-binds.
     * If the runnable throws {@link InvalidConnectionException}, reconnects and retries once.
     *
     * @param runnable  The operation to run.
     * @param monitor   Progress monitor for cancellation.
     * @throws Exception  If reconnecting or the operation itself fails.
     */
    private void checkConnectionAndRunAndMonitor( final InnerRunnable runnable, final StudioProgressMonitor monitor )
        throws Exception
    {
        // check connection
        if ( !isConnected() )
        {
            doConnect( monitor );
            doBind( monitor );
        }
        if ( ldapConnection == null )
        {
            throw new InvalidConnectionException( Messages.DirectoryApiConnectionWrapper_NoConnection );
        }

        // loop for reconnection
        for ( int i = 0; i <= 1; i++ )
        {
            runAndMonitor( runnable, monitor );

            // check reconnection
            if ( ( i == 0 ) && ( runnable.getException() instanceof InvalidConnectionException ) )
            {
                doConnect( monitor );
                doBind( monitor );
                runnable.reset();
            }
            else
            {
                break;
            }
        }
    }


    // ── RUN AND MONITOR — ATTACH CANCEL LISTENER AND EXECUTE ─────────────────────
    // We attach a cancel listener that interrupts the job thread and closes the
    // LDAP connection if the user hits Cancel.  We record the current thread as
    // jobThread so the listener can interrupt it.  After run() returns, we remove
    // the listener and clear jobThread.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the given runnable on the current thread with a cancel listener attached.
     * If the monitor is cancelled, the job thread is interrupted and the LDAP
     * connection is closed.
     *
     * @param runnable  The runnable to execute.
     * @param monitor   Progress monitor whose cancellation will interrupt this operation.
     * @throws CancelException  If the monitor was already cancelled before this method.
     */
    private void runAndMonitor( final InnerRunnable runnable, final StudioProgressMonitor monitor )
        throws CancelException
    {
        if ( !monitor.isCanceled() )
        {
            // monitor
            StudioProgressMonitor.CancelListener listener = event -> {
                if ( monitor.isCanceled() )
                {
                    if ( jobThread != null && jobThread.isAlive() )
                    {
                        jobThread.interrupt();
                    }

                    if ( ldapConnection != null )
                    {
                        try
                        {
                            ldapConnection.close();
                        }
                        catch ( Exception e )
                        {
                        }

                        ldapConnection = null;
                    }
                }
            };

            monitor.addCancelListener( listener );
            jobThread = Thread.currentThread();

            // run
            try
            {
                runnable.run();
            }
            finally
            {
                monitor.removeCancelListener( listener );
                jobThread = null;
            }

            if ( monitor.isCanceled() )
            {
                throw new CancelException();
            }
        }
    }


    // ── INNER CLASS: InnerConfiguration — THE GSSAPI JAAS CONFIG ─────────────────
    // GSSAPI authentication requires a JAAS Configuration so the JVM knows which
    // Kerberos login module to use.  This inner class builds that configuration
    // dynamically from the connection's preferences and credential mode.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * JAAS {@link Configuration} for GSSAPI (Kerberos) authentication.
     * Builds an {@link AppConfigurationEntry} for the configured login module
     * with options that match the connection's Kerberos credential configuration
     * (native ticket cache vs. obtain-TGT).
     */
    private final class InnerConfiguration extends Configuration
    {
        /** The JAAS login module class name (e.g. com.sun.security.auth.module.Krb5LoginModule). */
        private String krb5LoginModule;
        /** Cached configuration entries, built on first access. */
        private AppConfigurationEntry[] configList = null;


        /**
         * Creates a new {@link InnerConfiguration} for the given login module.
         *
         * @param krb5LoginModule  The JAAS login module class name.
         */
        public InnerConfiguration( String krb5LoginModule )
        {
            this.krb5LoginModule = krb5LoginModule;
        }


        /**
         * Returns the JAAS application configuration entries.
         * Built lazily on first call and cached thereafter.
         *
         * @param applicationName  Ignored — we always return the same entry.
         * @return  An array containing the single Kerberos login module entry.
         */
        public AppConfigurationEntry[] getAppConfigurationEntry( String applicationName )
        {
            if ( configList == null )
            {
                HashMap<String, Object> options = new HashMap<>();

                // TODO: this only works for Sun JVM
                options.put( "refreshKrb5Config", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
                switch ( connection.getConnectionParameter().getKrb5CredentialConfiguration() )
                {
                    case USE_NATIVE:
                        options.put( "useTicketCache", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
                        options.put( "doNotPrompt", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
                        break;
                    case OBTAIN_TGT:
                        options.put( "doNotPrompt", "false" ); //$NON-NLS-1$ //$NON-NLS-2$
                        break;
                }

                configList = new AppConfigurationEntry[1];
                configList[0] = new AppConfigurationEntry( krb5LoginModule, LoginModuleControlFlag.REQUIRED, options );
            }
            return configList;
        }


        /**
         * {@inheritDoc}
         * No-op — we don't cache external config state.
         */
        @Override
        public void refresh()
        {
        }
    }


    // ── GET LDAP LOGGERS — FETCH REGISTERED LOGGERS FROM THE PLUGIN ───────────────
    /**
     * Returns the list of registered {@link ILdapLogger} instances from the plugin registry.
     *
     * @return  List of active loggers.
     */
    private List<ILdapLogger> getLdapLoggers()
    {
        return ConnectionCorePlugin.getDefault().getLdapLoggers();
    }


    // ── CHECK RESPONSE — VERIFY THE RESULT CODE ────────────────────────────────────
    // The Apache Directory API's ResultCodeEnum.processResponse() throws an
    // LdapException if the result code is not SUCCESS (or one of the OK codes).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Verifies that the given response's LDAP result code indicates success.
     * Delegates to {@link ResultCodeEnum#processResponse(ResultResponse)}, which
     * throws an {@link LdapException} on failure.
     *
     * @param response  The response to check; no-op if {@code null}.
     * @throws Exception  If the result code is not a success code.
     */
    private void checkResponse( ResultResponse response ) throws Exception
    {
        if ( response != null )
        {
            ResultCodeEnum.processResponse( response );
        }
    }


    // ── TO STUDIO LDAP EXCEPTION — WRAP ANY EXCEPTION FOR CALLERS ─────────────────
    // All exceptions from the Apache Directory API are converted to StudioLdapException
    // so callers only have to deal with one exception type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts the given exception to a {@link StudioLdapException}.
     * Returns {@code null} if the input is {@code null}.
     *
     * @param exception  The exception to wrap.
     * @return  A {@link StudioLdapException} wrapping the input, or {@code null}.
     */
    private StudioLdapException toStudioLdapException( Exception exception )
    {
        if ( exception == null )
        {
            return null;
        }
        else if ( exception instanceof LdapException )
        {
            return new StudioLdapException( ( LdapException ) exception );
        }
        else
        {
            return new StudioLdapException( exception );
        }
    }

}
