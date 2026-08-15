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

package org.apache.directory.studio.connection.core.io;


import java.security.KeyStore;
import java.security.cert.CertPathValidatorException.Reason;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.directory.api.ldap.model.exception.LdapTlsHandshakeExceptionClassifier;
import org.apache.directory.api.ldap.model.exception.LdapTlsHandshakeFailCause;
import org.apache.directory.api.ldap.model.exception.LdapTlsHandshakeFailCause.LdapApiReason;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ICertificateHandler;
import org.apache.directory.studio.connection.core.Messages;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;


// ── CLASS: StudioTrustManager — THE FALCON'S SHIELD DEFLECTOR ─────────────────
// The Falcon's shield deflector challenges every incoming ship before letting it
// dock: "Is your transponder code in our permanent trusted list?  Our session
// scratch pad?  Does the JVM's built-in registry know you?  And does your hull
// ID match your transponder?"
// If the answer to all of those is "no" or "uncertain," the deflector calls the
// crew for a manual trust decision.  The crew decides: permanently trust this
// ship, trust it just for this session, or reject it.
// This class implements that challenge logic for TLS server certificate verification.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Custom {@link X509TrustManager} implementation that extends the JVM's default
 * trust checking with three additional trust tiers: permanently trusted (stored in
 * a file-backed keystore), session-trusted (stored in memory only), and interactive
 * (prompts the user via {@link ICertificateHandler}).
 *
 * <p>When verifying a server certificate, we check in order:</p>
 * <ol>
 *   <li>Permanently trusted store — if the cert is there, accept immediately</li>
 *   <li>Session trust store — if the cert is there, accept immediately</li>
 *   <li>JVM default trust manager — if OK, accept</li>
 *   <li>Certificate validity check — collect any failure</li>
 *   <li>Hostname verification — collect any failure</li>
 *   <li>If any check failed, ask the user via {@link ICertificateHandler}</li>
 * </ol>
 * The user can choose to trust permanently (cert goes in the file store), for
 * this session only (cert goes in the memory store), or not at all (exception thrown).
 * Think of this as the Falcon's deflector: it tries every clearance list before
 * asking the captain for a manual override.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioTrustManager implements X509TrustManager
{
    /** The JVM's standard X.509 trust manager — our fallback. */
    private X509TrustManager jvmTrustManager;

    /** The hostname we're connecting to — used for certificate hostname verification. */
    private String host;


    // ── CONSTRUCTOR — WRAP THE JVM'S DEFAULT TRUST MANAGER ────────────────────────
    // We keep the JVM trust manager as our base and add our custom tiers on top.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link StudioTrustManager} that delegates to the given JVM trust manager
     * as the final trust check before prompting the user.
     *
     * @param jvmTrustManager  The JVM's standard {@link X509TrustManager}.
     * @throws Exception  If initialization fails (unused in practice, kept for API compatibility).
     */
    public StudioTrustManager( X509TrustManager jvmTrustManager ) throws Exception
    {
        this.jvmTrustManager = jvmTrustManager;
    }


    // ── SET HOST — RECORD THE TARGET HOSTNAME ──────────────────────────────────────
    // We need the hostname to verify that the server's certificate CN/SAN matches
    // what we're actually connecting to.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the hostname being connected to.
     * This is used during hostname verification to ensure the server certificate's
     * Common Name or Subject Alternative Names include the target host.
     *
     * @param host  The target hostname (or IP address).
     */
    public void setHost( String host )
    {
        this.host = host;
    }


    // ── CHECK CLIENT TRUSTED — DELEGATE TO THE JVM MANAGER ────────────────────────
    // We don't add anything to client-side certificate validation — the JVM's
    // default manager handles it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * We simply delegate client certificate validation to the JVM trust manager.
     */
    public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException
    {
        jvmTrustManager.checkClientTrusted( chain, authType );
    }


    // ── CHECK SERVER TRUSTED — THE FULL SHIELD CHALLENGE LOGIC ───────────────────
    // We try three automatic clearance lists, then run two additional checks.
    // If anything fails, we collect all fail causes and ask the user for a
    // manual trust decision.  The user's choice determines the outcome.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Verifies the server's certificate chain.
     * We check the permanent trust store, the session trust store, the JVM default
     * trust manager, certificate validity, and hostname verification.
     * If any check fails, we call {@link ICertificateHandler#verifyTrustLevel} to
     * let the user decide.  Throwing {@link CertificateException} rejects the connection.
     *
     * @param chain     The server's certificate chain.
     * @param authType  The authentication type (e.g. "RSA").
     * @throws CertificateException  If the certificate is not trusted and the user rejected it.
     */
    public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException
    {
        // check permanent trusted certificates, return on success
        try
        {
            X509TrustManager permanentTrustManager = getPermanentTrustManager();
            if ( permanentTrustManager != null )
            {
                permanentTrustManager.checkServerTrusted( chain, authType );
                return;
            }
        }
        catch ( CertificateException ce )
        {
        }

        // check temporary trusted certificates, return on success
        try
        {
            X509TrustManager sessionTrustManager = getSessionTrustManager();
            if ( sessionTrustManager != null )
            {
                sessionTrustManager.checkServerTrusted( chain, authType );
                return;
            }
        }
        catch ( CertificateException ce )
        {
        }

        // below here no manually trusted certificate (either permanent or temporary) matched
        Map<Reason, LdapTlsHandshakeFailCause> failCauses = new LinkedHashMap<>();
        CertificateException certificateException = null;

        // perform trust check of JVM trust manager
        try
        {
            jvmTrustManager.checkServerTrusted( chain, authType );
        }
        catch ( CertificateException ce )
        {
            certificateException = ce;
            LdapTlsHandshakeFailCause failCause = LdapTlsHandshakeExceptionClassifier.classify( ce, chain[0] );
            failCauses.put( failCause.getReason(), failCause );
        }

        // perform a certificate validity check
        try
        {
            chain[0].checkValidity();
        }
        catch ( CertificateException ce )
        {
            certificateException = ce;
            LdapTlsHandshakeFailCause failCause = LdapTlsHandshakeExceptionClassifier.classify( ce, chain[0] );
            failCauses.put( failCause.getReason(), failCause );
        }

        // perform host name verification
        try
        {
            DefaultHostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
            hostnameVerifier.verify( host, chain[0] );
        }
        catch ( SSLException ssle )
        {
            certificateException = new CertificateException( ssle );
            LdapTlsHandshakeFailCause failCause = new LdapTlsHandshakeFailCause( ssle, ssle,
                LdapApiReason.HOST_NAME_VERIFICATION_FAILED, "Hostname verification failed" );
            failCauses.put( failCause.getReason(), failCause );
        }

        if ( !failCauses.isEmpty() )
        {
            // either trust check or host name verification
            // ask for confirmation
            ICertificateHandler ch = ConnectionCorePlugin.getDefault().getCertificateHandler();
            ICertificateHandler.TrustLevel trustLevel = ch.verifyTrustLevel( host, chain, failCauses.values() );
            switch ( trustLevel )
            {
                case Permanent:
                    ConnectionCorePlugin.getDefault().getPermanentTrustStoreManager().addCertificate( chain[0] );
                    break;
                case Session:
                    ConnectionCorePlugin.getDefault().getSessionTrustStoreManager().addCertificate( chain[0] );
                    break;
                case Not:
                    throw certificateException;
            }
        }
    }


    // ── GET ACCEPTED ISSUERS — DELEGATE TO JVM MANAGER ────────────────────────────
    // We return the JVM's list of trusted root CA certificates.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * We delegate to the JVM trust manager's accepted issuers.
     */
    public X509Certificate[] getAcceptedIssuers()
    {
        return jvmTrustManager.getAcceptedIssuers();
    }


    // ── GET PERMANENT TRUST MANAGER — BUILD FROM THE PERSISTENT STORE ─────────────
    // We load the permanent trust store and build a trust manager from it.
    // Returns null if the store is empty (no manually trusted certs yet).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link X509TrustManager} backed by the permanent trust store,
     * or {@code null} if the store is empty.
     *
     * @return  The permanent {@link X509TrustManager}, or {@code null}.
     * @throws CertificateException  If the trust store cannot be loaded.
     */
    private X509TrustManager getPermanentTrustManager() throws CertificateException
    {
        KeyStore permanentTrustStore = ConnectionCorePlugin.getDefault().getPermanentTrustStoreManager().getKeyStore();
        X509TrustManager permanentTrustManager = getTrustManager( permanentTrustStore );
        return permanentTrustManager;
    }


    // ── GET SESSION TRUST MANAGER — BUILD FROM THE IN-MEMORY STORE ───────────────
    // We build a trust manager from the session-only (memory-backed) trust store.
    // Returns null if the store is empty.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link X509TrustManager} backed by the session trust store,
     * or {@code null} if the store is empty.
     *
     * @return  The session {@link X509TrustManager}, or {@code null}.
     * @throws CertificateException  If the trust store cannot be loaded.
     */
    private X509TrustManager getSessionTrustManager() throws CertificateException
    {
        KeyStore sessionTrustStore = ConnectionCorePlugin.getDefault().getSessionTrustStoreManager().getKeyStore();
        X509TrustManager sessionTrustManager = getTrustManager( sessionTrustStore );
        return sessionTrustManager;
    }


    // ── GET TRUST MANAGER — BUILD FROM ANY KEYSTORE ───────────────────────────────
    // We use the JVM's TrustManagerFactory to build a trust manager from the given
    // KeyStore.  If the store is empty (no aliases), we return null to signal
    // "no custom certs in this store."
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link X509TrustManager} from the given {@link KeyStore}.
     * Returns {@code null} if the keystore has no entries (empty trust list).
     *
     * @param trustStore  The keystore to build the trust manager from.
     * @return  An {@link X509TrustManager} initialized from the store, or {@code null}.
     * @throws CertificateException  If factory initialization fails.
     */
    private X509TrustManager getTrustManager( KeyStore trustStore ) throws CertificateException
    {
        try
        {
            Enumeration<String> aliases = trustStore.aliases();
            if ( aliases.hasMoreElements() )
            {
                TrustManagerFactory factory = TrustManagerFactory.getInstance( TrustManagerFactory
                    .getDefaultAlgorithm() );
                factory.init( trustStore );
                TrustManager[] permanentTrustManagers = factory.getTrustManagers();
                TrustManager permanentTrustManager = permanentTrustManagers[0];
                return ( X509TrustManager ) permanentTrustManager;
            }
        }
        catch ( Exception e )
        {
            throw new CertificateException( Messages.StudioTrustManager_CantCreateTrustManager, e );
        }

        return null;
    }

}
