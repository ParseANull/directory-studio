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


// ── CLASS: XMLSchemaFileImportException — Han Shoots First ────────────────────
// The XML schema file is unreadable, the root element is wrong, or an OID is
// missing — something has gone wrong and we're not letting it silently propagate.
// Like Han in the Mos Eisley cantina, we act first: wrap the problem in a
// strongly-typed exception and fire it straight back to the caller so the error
// is handled at the right level with a useful message.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an XML schema file could not be imported, because the file could
 * not be parsed, had an invalid root element, or was missing required values
 * (like an OID on an attribute type).
 * Callers that invoke {@link XMLSchemaFileImporter} methods catch this and
 * present the embedded message to the user.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class XMLSchemaFileImportException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── Han Calls It Out — Message Only ──────────────────────────────────────
    // Han knows exactly what went wrong and says it clearly; no underlying
    // Java exception is available, just the plain description.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an exception with a human-readable description of the import failure.
     * Use this when the failure is a logical error (wrong root element, missing OID)
     * with no underlying Java exception to attach.
     *
     * @param message  a description of what went wrong
     */
    public XMLSchemaFileImportException( String message )
    {
        super( message );
    }


    // ── Han Takes the Shot — Message and Root Cause ───────────────────────────
    // Han has both the plain description and the underlying cause (DocumentException,
    // etc.); he wraps both so nobody upstream has to guess what went wrong.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an exception that carries both a human-readable description and
     * the underlying cause of the import failure.
     *
     * @param message  a description of what went wrong
     * @param cause    the underlying exception (e.g. DocumentException)
     */
    public XMLSchemaFileImportException( String message, Exception cause )
    {
        super( message, cause );
    }
}
