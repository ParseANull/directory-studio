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
package org.apache.directory.studio.schemaeditor.model.io;


// ── CLASS: SchemaConnectorException — Han Shoots First ───────────────────────
// The hyperspace jump goes wrong — the Falcon drops out of lightspeed, the
// motivator blows, something fails hard.  Han doesn't shrug and carry on;
// he calls it out immediately so the crew can deal with it.  When a schema
// connector can't complete an import or export — network error, LDAP exception,
// parse failure — we wrap the problem in this strongly-typed exception and fire
// it back to the caller right away, with as much detail as we have available.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that a {@link SchemaConnector} operation (import or export) failed.
 * Provides four constructor overloads so callers can include a message, a root
 * cause, both, or neither, depending on what information is available.
 * The UI layer catches this and surfaces the message in an error dialog.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaConnectorException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── Han Calls Out: "Something Is Wrong" — No Details ─────────────────────
    // Han knows something went wrong but doesn't have specifics yet; he flags
    // it immediately with a bare exception.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SchemaConnectorException with no message or cause.
     * Use this only when no further information is available.
     */
    public SchemaConnectorException()
    {
        super();
    }


    // ── Han Calls Out: "The Motivator Blew" — Message and Cause ──────────────
    // Han has both a plain-English description of what went wrong and the
    // underlying technical cause; he reports both.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SchemaConnectorException with a human-readable message and
     * the underlying throwable that caused the failure.
     *
     * @param message    a description of what failed
     * @param throwable  the underlying cause — e.g. an LdapException
     */
    public SchemaConnectorException( String message, Throwable throwable )
    {
        super( message, throwable );
    }


    // ── Han Calls Out: "We Can't Make the Jump" — Message Only ───────────────
    // Han knows what went wrong and can describe it clearly, but there's
    // no lower-level Java exception to attach.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SchemaConnectorException with a human-readable message only.
     * Use this for logic errors where no underlying exception exists.
     *
     * @param message  a description of what failed
     */
    public SchemaConnectorException( String message )
    {
        super( message );
    }


    // ── Han Points to the Broken Component — Cause Only ──────────────────────
    // Han can show you exactly which part failed even if he can't articulate
    // in plain English what that means; he wraps the root cause and hands it over.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SchemaConnectorException wrapping an underlying throwable.
     * The message is derived from the throwable's own message.
     *
     * @param throwable  the underlying cause — e.g. a wrapped LDAP or IO exception
     */
    public SchemaConnectorException( Throwable throwable )
    {
        super( throwable );
    }
}
