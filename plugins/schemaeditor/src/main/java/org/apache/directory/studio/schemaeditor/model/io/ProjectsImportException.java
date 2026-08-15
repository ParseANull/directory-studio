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


// ── CLASS: ProjectsImportException — Han Shoots First ────────────────────────
// Something has gone wrong reading a project file — the XML is malformed, the
// root element isn't what we expected, a value can't be converted — and we're
// not going to sit there waiting for the error to cause further damage.  Like
// Han in the Mos Eisley cantina, we act first and decisively: wrap the problem
// in a strongly-typed exception and fire it straight back to the caller so they
// can tell the user what happened.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that a project file could not be imported, for any reason —
 * unreadable file, invalid XML structure, or unconvertible attribute value.
 * Callers that invoke {@link ProjectsImporter} methods catch this and present
 * the embedded message in an error dialog.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsImportException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── Han Takes the Shot with a Clear Description ───────────────────────────
    // Han doesn't mumble when things go wrong — he says exactly what the problem
    // is so everyone in the cantina knows why the blaster went off.
    // We wrap the message string in a plain Exception with no additional cause,
    // because some errors (like "not a valid project root element") don't have
    // an underlying Java exception to attach.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an exception with a plain human-readable message describing what
     * went wrong during project import.
     *
     * @param message  a clear description of the import failure
     */
    public ProjectsImportException( String message )
    {
        super( message );
    }
}
