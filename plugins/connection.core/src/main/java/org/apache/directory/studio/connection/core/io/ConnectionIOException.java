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


// ── CLASS: ConnectionIOException — THE FALCON'S MANIFEST IS UNREADABLE ────────
// When Chewie tries to load the Falcon's route manifest (connections.xml) and
// finds it corrupted or in an unrecognized format, he panics — the Falcon can't
// fly without a valid route manifest.
// This exception signals exactly that: something went wrong while reading or
// validating the connections XML file, with a human-readable message describing
// what the problem was.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Checked exception thrown when reading or validating the connections XML file fails.
 * This covers DOM parse failures, unexpected root element names, and invalid
 * attribute values (e.g. an unrecognized encryption method string).
 * Callers must catch or re-throw this; it is not a RuntimeException because
 * the caller should handle the error gracefully (show an error message, use
 * defaults, etc.) rather than letting it propagate unchecked.
 * Think of this as the Falcon panicking because the route manifest is garbled:
 * it tells you exactly what it couldn't read and why.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionIOException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── CONSTRUCTOR — PACKAGE THE ERROR MESSAGE ───────────────────────────────────
    // Chewie jots down what went wrong so the crew knows where to start debugging.
    // We pass the message to the superclass so it appears in exception toString.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionIOException} with the given detail message.
     *
     * @param message  A human-readable description of the read or validation failure.
     */
    public ConnectionIOException( String message )
    {
        super( message );
    }
}
