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


import org.apache.directory.api.ldap.model.exception.LdapException;


// ── CLASS: CancelException — HAN HITS THE ABORT SWITCH ────────────────────────
// Sometimes Han aborts a mission mid-flight — the operator hit Cancel on the
// progress dialog and we need to stop the LDAP operation cleanly.
// This is the typed signal that says "we cancelled on purpose" as opposed to
// "something blew up."  Code that catches LdapException can specifically check
// for CancelException to distinguish a user abort from an actual error.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Typed {@link LdapException} that signals the intentional cancellation of an
 * in-progress LDAP request.
 * We throw this instead of a generic {@link LdapException} when the user clicks
 * Cancel on a progress dialog, so callers can tell the difference between
 * "something went wrong" and "we stopped on purpose."
 * Think of this as Han punching the abort switch: the mission stops, but it's
 * not an error — it's a deliberate decision.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CancelException extends LdapException
{

    private static final long serialVersionUID = 1L;


    // ── DEFAULT CONSTRUCTOR — ABORT WITH NO REASON ─────────────────────────────────
    // Han hits abort without saying anything.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link CancelException} with no message.
     */
    public CancelException()
    {
        super();
    }


    // ── MESSAGE CONSTRUCTOR — ABORT WITH A REASON ──────────────────────────────────
    // Han hits abort and tells the crew why.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link CancelException} with a descriptive message.
     *
     * @param message  Human-readable description of why the operation was cancelled.
     */
    public CancelException( String message )
    {
        super( message );
    }

}
