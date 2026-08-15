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

package org.apache.directory.studio.connection.core;


import java.security.cert.X509Certificate;
import java.util.Collection;

import org.apache.directory.api.ldap.model.exception.LdapTlsHandshakeFailCause;


// ── CLASS: ICertificateHandler — THE FALCON'S SHIELDS CHALLENGE AN UNKNOWN SHIP
// When an unidentified ship approaches the Falcon, Han checks its transponder.
// If it's not on his trusted list he has three options: definitely don't let it
// through (Not), let it through just for this trip (Session), or permanently
// add it to the trusted list (Permanent).
// This interface is that decision protocol — applied to TLS certificates.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface through which the connection core asks a higher-level layer
 * (typically the UI plugin) whether to trust an unrecognized TLS certificate.
 * We define this in connection.core so the TLS machinery can ask for a trust
 * decision without coupling itself to SWT dialogs.
 * Think of this interface as the Falcon's IFF (Identify Friend or Foe) protocol:
 * core detects an unknown cert and asks "do we trust this ship?" — the UI shows
 * the certificate details and lets the pilot decide.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ICertificateHandler
{

    /**
     * The trust level of a certificate.
     */
    enum TrustLevel
    {
        /** Don't trust a certificate. */
        Not,

        /** Trust a certificate within the current session. */
        Session,

        /** Trust a certificate permanently. */
        Permanent;
    }


    // ── VERIFY TRUST LEVEL — HAN DECIDES WHETHER TO LET THE SHIP THROUGH ─────────
    // The unidentified ship sends its transponder code and explains why it failed
    // the standard check.  Han looks at the codes and decides: block it, let it
    // through for this trip, or add it to the permanent safe list.
    // We call this when a TLS handshake fails because the cert is untrusted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Evaluates the trust level for an unrecognized TLS certificate chain.
     * Called when the JVM's default trust checks fail — for example a self-signed cert
     * or an expired cert the user has not yet accepted.
     * The implementation may inspect the chain and show the user a dialog.
     *
     * <p>For example — Han evaluates the transponder:</p>
     * <pre>
     *   TrustLevel decision = handler.verifyTrustLevel(host, chain, fails);
     *   switch (decision) {
     *     case Not: throw new CertificateException("Shields up, denied.");
     *     case Session: trustForNow(chain);
     *     case Permanent: trustForever(chain);
     *   }
     * </pre>
     *
     * @param host        The hostname of the LDAP server whose cert is under scrutiny.
     * @param certChain   The full TLS certificate chain presented by the server.
     * @param failCauses  The reasons the standard validation rejected this chain.
     * @return  A {@link TrustLevel} indicating whether and how long to trust the cert.
     */
    TrustLevel verifyTrustLevel( String host, X509Certificate[] certChain,
        Collection<LdapTlsHandshakeFailCause> failCauses );

}
