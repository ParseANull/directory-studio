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


// ── CLASS: ICredentials — HAN'S IMPERIAL ACCESS BADGE AND CODE ───────────────
// At the Cloud City docking bay, Han's Imperial access package has two parts:
// who he claims to be (bind principal) and the secret code that proves it
// (bind password).  He also carries a copy of the whole flight manifest
// (connection parameter) for context.
// This interface defines the shape of that package — the credential bundle
// the connection machinery passes to the LDAP server at bind time.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds the authentication credentials for an LDAP bind operation.
 * We separate credentials from {@link ConnectionParameter} so the auth handler can
 * return runtime credentials (possibly from a dialog or keystore) that differ
 * from what's stored in the parameter bean.
 * Think of this interface as Han's docking-bay access package: principal + password,
 * with the full connection context tucked in for reference.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ICredentials
{
    // ── GET CONNECTION PARAMETER — HANDING OVER THE FULL FLIGHT MANIFEST ─────────
    // Han presents his full flight manifest alongside his ID badge —
    // it gives the checkpoint the complete context for the connection.
    // We return the ConnectionParameter so the bind code can read all connection settings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ConnectionParameter} associated with these credentials.
     * This gives the bind code access to all connection settings (host, port, encryption,
     * auth method, etc.) alongside the runtime credentials.
     *
     * @return  The backing {@link ConnectionParameter}.
     */
    ConnectionParameter getConnectionParameter();


    // ── GET BIND PRINCIPAL — HAN SHOWS HIS ID ─────────────────────────────────────
    // Han holds up his ID card showing his name and rank — who he claims to be.
    // We return the bind principal (a DN or username) the server will authenticate.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind principal — the "who we are" part of authentication.
     * For simple binds this is typically a Distinguished Name like
     * {@code cn=admin,dc=example,dc=com}. For SASL it may be a Kerberos principal.
     *
     * @return  The bind principal string, or {@code null} for anonymous.
     */
    String getBindPrincipal();


    // ── GET BIND PASSWORD — HAN WHISPERS THE SECRET CODE ─────────────────────────
    // Han leans in and whispers his Imperial override code to the checkpoint guard —
    // the secret that proves the badge is real.
    // We return the bind password the LDAP server needs to verify our identity.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind password that proves the principal's identity.
     * May be {@code null} for anonymous or Kerberos connections where no
     * explicit password is needed.
     *
     * @return  The bind password string, or {@code null}.
     */
    String getBindPassword();
}
