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
package org.apache.directory.studio.ldapservers;


// ── CLASS: LdapServersManagerIOException — WHEN THE SCHEMATICS ARE UNREADABLE ───────────────
// In Rogue One, the Imperial engineers sometimes received garbled or corrupted transmissions
// of the Death Star schematics — the data was there, but it couldn't be parsed.
// This exception is thrown by {@link LdapServersManagerIO} whenever reading or writing
// the servers.xml file fails — corrupted XML, missing file, schema mismatch, etc.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Signals that something went wrong while reading or writing the servers.xml store.
 * Callers of {@link LdapServersManagerIO#read} and {@link LdapServersManagerIO#write}
 * catch this to display a meaningful error rather than letting a raw IOException bubble up.
 * Think of it as the distress signal Galen Erso sent when the schematics transfer went wrong.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServersManagerIOException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── Transmitting The Garbled Schematics Error ────────────────────────────────────────────
    // An Imperial engineer receives a corrupted data burst from the schematics relay station.
    // He flags the error with a precise message: "Section 7-G transmission unreadable."
    // We wrap that message into an exception so callers know exactly what went wrong.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new exception with a human-readable message describing why the IO operation failed.
     * We pass the message straight through to {@link Exception} so it shows up in error dialogs
     * and log entries.
     *
     * <p>For example — the engineer files an error report:</p>
     * <pre>
     *   "XML parse error at line 42: unexpected element 'ldapServer2'."
     *   throw new LdapServersManagerIOException( "XML parse error at line 42..." );
     * </pre>
     *
     * @param message  a plain-English description of what went wrong during the read or write
     */
    public LdapServersManagerIOException( String message )
    {
        super( message );
    }
}
