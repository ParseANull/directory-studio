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


// ── CLASS: IAuthHandler — HAN GETS HIS IMPERIAL ACCESS CODE AT THE DOCKING BAY
// At Cloud City's docking bay, Han needs the right access code to get the Falcon
// cleared for landing. Someone has to supply that code — either it's pre-loaded
// in the nav computer or Han has to radio back to base and ask.
// This interface is the contract for "whoever supplies the credential" —
// the core layer calls it, and the UI layer implements it with a password dialog.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface through which the connection core requests authentication
 * credentials from a higher-level layer (typically the UI plugin).
 * We define this interface in connection.core so the core LDAP machinery can ask
 * for credentials without coupling itself to SWT dialogs.
 * Think of this interface as Han's "radio back to base for the access code" protocol:
 * core asks, UI provides (perhaps by showing a dialog), and core uses whatever comes back.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IAuthHandler
{
    // ── GET CREDENTIALS — HAN RADIOS BASE FOR THE ACCESS CODE ────────────────────
    // Han presses the comms button: "I need the access code for docking bay 94."
    // Base either reads it off the manifest or pops up a window asking the pilot.
    // We call this method when we need credentials at bind time, and the implementation
    // either reads from the parameter bean or shows the user a password dialog.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the credentials needed to bind to the given connection.
     * The implementation may read credentials from {@code connectionParameter},
     * show a UI dialog to the user, or look them up from a password keystore.
     * Returning {@code null} cancels the authentication — the bind is aborted.
     *
     * <p>For example — Han radios for the access code:</p>
     * <pre>
     *   ICredentials creds = authHandler.getCredentials(params);
     *   if (creds == null) return; // user hit Cancel
     *   ldapConnection.bind(creds.getBindPrincipal(), creds.getBindPassword());
     * </pre>
     *
     * @param connectionParameter  The connection parameters describing who we're binding to.
     * @return  The {@link ICredentials} to use for the bind, or {@code null} to cancel.
     */
    ICredentials getCredentials( ConnectionParameter connectionParameter );
}
