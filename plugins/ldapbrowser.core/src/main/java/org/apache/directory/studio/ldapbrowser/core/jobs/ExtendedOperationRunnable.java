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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;


// ── CLASS: ExtendedOperationRunnable — LANDO ACTIVATES A CLOUD CITY SYSTEM ───
// Lando Calrissian has special administrative access to Cloud City's computer
// core.  When he needs to trigger an unusual facility operation — the carbon-
// freeze chamber, the emergency shield generator, a system diagnostic — he
// sends a non-standard command packet (an extended request) directly to the
// facility's control system and waits for the response.
// An LDAP "extended operation" works the same way: it's a catch-all mechanism
// for server-specific operations that don't fit the standard LDAP verbs.
// Examples include STARTLS (OID 1.3.6.1.4.1.1466.20037), Password Modify
// (OID 1.3.6.1.4.1.4203.1.11.1), and WhoAmI (OID 1.3.6.1.4.1.4203.1.11.3).
// This runnable takes an {@link ExtendedRequest} packet, sends it over the
// connection, and stores the {@link ExtendedResponse} for the caller.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that executes an LDAP extended operation.
 * Extended operations are LDAP's escape hatch for server-specific commands
 * that aren't covered by the standard search/add/modify/delete/rename verbs.
 * We send the supplied {@link ExtendedRequest} to the server and store the
 * {@link ExtendedResponse}; callers retrieve it via {@link #getResponse()}
 * after the job completes.
 * Think of it as Lando using his admin codes to activate a facility system
 * that has no standard control panel button.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtendedOperationRunnable implements StudioConnectionRunnableWithProgress
{
    private IBrowserConnection connection;

    private ExtendedRequest request;

    private ExtendedResponse response;


    // ── Lando Cues Up The Facility Command ───────────────────────────────────────
    // "I'm going to activate the carbon-freeze chamber — here's my admin
    //  access code and the command packet."  We store both the connection
    //  and the request so run() can send them together.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExtendedOperationRunnable.
     * The response is stored internally; retrieve it with {@link #getResponse()}
     * after the job has run.
     *
     * <p>For example — sending a Password Modify extended request:</p>
     * <pre>
     *   ExtendedRequest req = new PasswordModifyRequestImpl();
     *   new StudioBrowserJob(new ExtendedOperationRunnable(conn, req)).execute();
     * </pre>
     *
     * @param connection the browser connection to send the request on.
     * @param request    the extended request to send.
     */
    public ExtendedOperationRunnable( final IBrowserConnection connection, ExtendedRequest request )
    {
        this.connection = connection;
        this.request = request;
    }


    // ── Lando Uses His Own Control Panel ─────────────────────────────────────────
    // "The command goes through this terminal — Cloud City's main comms hub."
    // We return the raw connection so the job framework can acquire it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw connection used to send the extended request.
     *
     * @return single-element array with the underlying {@link Connection}.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { connection.getConnection() };
    }


    // ── The Operation Name For The Progress Bar ───────────────────────────────────
    // "Activating facility system..." shown in the Eclipse progress UI.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Extended operation" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__extended_operation_name;
    }


    // ── Lando Locks The Admin Console During The Command ─────────────────────────
    // Only one admin command runs at a time — we lock the connection so other
    // concurrent jobs don't interfere.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects locked while this job runs.  We lock the browser
     * connection to prevent concurrent extended operations on the same server.
     *
     * @return the browser connection.
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { connection };
    }


    // ── The Error Report If The Facility System Fails ────────────────────────────
    // "The carbon-freeze chamber malfunctioned — extended operation error."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown if the extended operation fails.
     *
     * @return a localised "Extended operation failed" error string.
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__extended_operation_error;
    }


    // ── Lando Sends The Command Packet And Waits ─────────────────────────────────
    // Lando punches in the OID for the facility system he wants to activate,
    // sends the request, and waits for the acknowledgment.  If something goes
    // wrong he logs the exception into the monitor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sends the extended request to the LDAP server and stores the response.
     * The OID of the request is used to name the progress task (Eclipse will
     * look up a human-readable name for it via {@link Utils#getOidDescription}).
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {

        monitor.beginTask( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__extended_operation_task,
            new String[]
            { Utils.getOidDescription( request.getRequestName() ) } ), 2 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        try
        {
            response = connection.getConnection().getConnectionWrapper().extended( request, monitor );
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }
    }


    // ── Lando Receives The Facility's Reply ───────────────────────────────────────
    // "Chamber activated — response received."  The caller reads this after
    // the job completes.  Extended operations are synchronous from the caller's
    // perspective — they launch the job and then poll the result.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ExtendedResponse} from the LDAP server.
     * Call this only after the job has completed.
     *
     * @return the response, or {@code null} if the operation failed or no
     *         response was received.
     */
    public ExtendedResponse getResponse()
    {
        return response;
    }
}
