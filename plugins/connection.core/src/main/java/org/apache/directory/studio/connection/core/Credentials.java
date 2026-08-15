/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor name agreements.  See the NOTICE file
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


// ── CLASS: Credentials — HAN BUNDLES HIS IMPERIAL ACCESS PACKAGE ─────────────
// Han puts his ID card, his secret code, and a copy of the flight manifest
// into one envelope — that's the package he presents at every checkpoint.
// This concrete class is that envelope: a simple value object carrying the
// bind principal, bind password, and connection parameter together so the
// LDAP connection machinery has everything it needs at bind time.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Default implementation of {@link ICredentials}.
 * We bundle the bind principal, bind password, and the backing
 * {@link ConnectionParameter} into a single immutable value object.
 * The auth handler creates one of these and hands it back to the connection
 * machinery, which then passes it straight to the LDAP bind call.
 * Think of this class as Han's access package: everything the checkpoint
 * needs in one envelope, handed over in one move.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Credentials implements ICredentials
{

    /** The bind principal. */
    private String bindPrincipal;

    /** The bind password. */
    private String bindPassword;

    /** The connection parameter. */
    private ConnectionParameter connectionParameter;


    // ── CONSTRUCTOR — HAN SEALS THE ACCESS PACKAGE ────────────────────────────────
    // Han puts his ID card (principal), secret code (password), and manifest
    // (connection parameter) into the envelope and seals it for delivery.
    // We initialize all three fields in one shot.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fully-populated {@link Credentials} object.
     * The auth handler builds this and returns it to the bind machinery.
     *
     * <p>For example — Han seals the package:</p>
     * <pre>
     *   return new Credentials("cn=admin,dc=example,dc=com", "secret", params);
     * </pre>
     *
     * @param bindPrincipal       The DN or username to authenticate as.
     * @param bindPassword        The password that proves identity.
     * @param connectionParameter The full connection settings for context.
     */
    public Credentials( String bindPrincipal, String bindPassword, ConnectionParameter connectionParameter )
    {
        this.bindPrincipal = bindPrincipal;
        this.bindPassword = bindPassword;
        this.connectionParameter = connectionParameter;
    }


    // ── GET CONNECTION PARAMETER — HAN HANDS OVER THE FLIGHT MANIFEST ─────────────
    // Han pulls the flight manifest out of the envelope for the checkpoint officer.
    // We return the backing ConnectionParameter.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public ConnectionParameter getConnectionParameter()
    {
        return connectionParameter;
    }


    // ── GET BIND PRINCIPAL — HAN SHOWS HIS ID CARD ────────────────────────────────
    // Han holds up his ID card showing who he is.
    // We return the bind principal string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getBindPrincipal()
    {
        return bindPrincipal;
    }


    // ── GET BIND PASSWORD — HAN WHISPERS THE SECRET CODE ─────────────────────────
    // Han leans in and recites the override code.
    // We return the bind password string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getBindPassword()
    {
        return bindPassword;
    }

}
