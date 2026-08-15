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

package org.apache.directory.studio.connection.ui;


import java.security.cert.X509Certificate;
import java.util.Collection;

import org.apache.directory.api.ldap.model.exception.LdapTlsHandshakeFailCause;
import org.apache.directory.studio.connection.core.ICertificateHandler;
import org.apache.directory.studio.connection.ui.dialogs.CertificateTrustDialog;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ConnectionUICertificateHandler — THE FALCON'S SHIELD INSPECTOR (UI) ──
// When the Falcon's deflector shields detect an unfamiliar craft, someone has to
// look it up and decide whether to lower the shields.  That's what this class does
// for TLS certificates: when StudioTrustManager encounters a certificate it doesn't
// recognise, it calls verifyTrustLevel() here.
// We pop open the CertificateTrustDialog on the SWT UI thread (using syncExec so
// we block until the user decides), let the user inspect the certificate chain and
// the reason for the failure, and then return whatever trust level they chose
// (Not, Session, or Permanent).
// The core layer (StudioTrustManager) doesn't know or care about SWT —
// this class is the bridge between the TLS handshake and the dialog box.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Production implementation of {@link ICertificateHandler} that prompts the
 * user via {@link CertificateTrustDialog}.
 *
 * <p>When the TLS handshake cannot verify a server certificate using the
 * permanent/session trust stores or the JVM trust store, {@link
 * org.apache.directory.studio.connection.core.io.StudioTrustManager} calls
 * {@link #verifyTrustLevel} on this handler.  We open the certificate trust
 * dialog on the SWT UI thread and return the user's decision.</p>
 *
 * <p>This class is registered with {@link
 * org.apache.directory.studio.connection.core.ConnectionCorePlugin} during
 * {@link ConnectionUIPlugin#start}.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionUICertificateHandler implements ICertificateHandler
{
    // ── VERIFY TRUST LEVEL — SHOW THE CERT TRUST DIALOG ──────────────────────────
    // We run the dialog on the SWT UI thread via syncExec and capture the result
    // in a one-element array (the standard lambda-capture workaround for "final").
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Opens the {@link CertificateTrustDialog} on the SWT UI thread so the user
     * can inspect the certificate chain and the handshake failure reason, then
     * returns the trust level the user chose.
     *
     * @param host        The hostname we connected to (shown in the dialog title).
     * @param certChain   The server's X.509 certificate chain.
     * @param failCauses  The reasons the TLS handshake failed (e.g., untrusted root,
     *                    hostname mismatch).
     * @return  The user's trust decision: {@code Not}, {@code Session}, or
     *          {@code Permanent}.
     */
    public TrustLevel verifyTrustLevel( final String host, final X509Certificate[] certChain,
        final Collection<LdapTlsHandshakeFailCause> failCauses )
    {
        // ── SHOW THE DIALOG ON THE UI THREAD — BLOCK UNTIL THE USER DECIDES ───────
        // The TLS handshake is running on a background thread, so we syncExec
        // to switch to the UI thread, open the dialog, wait for the user, and
        // capture the result before returning to the background thread.
        // ──────────────────────────────────────────────────────────────────────────
        final TrustLevel[] trustLevel = new TrustLevel[1];
        PlatformUI.getWorkbench().getDisplay().syncExec( () -> {
            CertificateTrustDialog dialog = new CertificateTrustDialog( PlatformUI.getWorkbench().getDisplay()
                .getActiveShell(), host, certChain, failCauses );
            dialog.open();
            trustLevel[0] = dialog.getTrustLevel();
        } );

        return trustLevel[0];
    }
}
