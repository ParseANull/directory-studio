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


// ── CLASS: OpenLdapSchemaFileImportException — Han Shoots First ───────────────
// In the Mos Eisley cantina, Greedo has Han at gunpoint and things could go
// very wrong.  Han doesn't wait for the situation to escalate — he acts
// decisively and definitively the moment he knows it's going sideways.
// When we try to import an OpenLDAP schema file and something goes wrong —
// the file is unreadable, the syntax is broken — we don't let the error
// bubble up silently.  We wrap it in this strongly-typed exception and fire
// it back to the caller immediately, message and cause both included.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an OpenLDAP {@code .schema} file could not be imported, either
 * because the file could not be read or because it contained a syntax error.
 * Callers that invoke {@link OpenLdapSchemaFileImporter#getSchema} catch this
 * and present an error dialog to the user with the embedded message.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapSchemaFileImportException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── Han Takes the Shot — Message and Root Cause ───────────────────────────
    // Han knows exactly what went wrong and why; he wraps both the message
    // ("couldn't read the file") and the original cause (the IO or parse error)
    // into one decisive exception so nobody is left guessing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an exception that carries both a human-readable description and
     * the underlying cause that triggered the import failure.
     * The message typically includes the file path and, where available,
     * the line/column of the parse error.
     *
     * @param message  a human-readable description of what went wrong
     * @param cause    the underlying exception (IOException, ParseException, etc.)
     */
    public OpenLdapSchemaFileImportException( String message, Exception cause )
    {
        super( message, cause );
    }
}
