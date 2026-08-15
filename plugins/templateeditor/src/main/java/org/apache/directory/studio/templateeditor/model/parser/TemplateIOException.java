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
package org.apache.directory.studio.templateeditor.model.parser;


// ── CLASS: TemplateIOException — C-3PO CANNOT PARSE THE JAWA DIALECT ────────────
// When C-3PO tries to parse a Jawa dialect and fails — the XML is garbled, a
// required element is missing, or the structure doesn't match expectations — he
// raises his hands and announces the failure with a clear message. That's this
// exception: a checked exception thrown by {@link TemplateIO} whenever a template
// XML file can't be loaded or saved, with a human-readable description of what
// went wrong.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Checked exception thrown by {@link TemplateIO} when loading or saving a template
 * XML file fails. Wraps the failure reason as a human-readable message so callers
 * can report it to the user without digging into a raw {@code IOException} or
 * XML-parser exception.
 * Think of this as C-3PO announcing he cannot parse the Jawa dialect.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateIOException extends Exception
{
    private static final long serialVersionUID = 1L;


    // ── CONSTRUCTOR: REPORT THE PARSING FAILURE ───────────────────────────────────
    // C-3PO announces exactly what went wrong and why he couldn't understand the
    // input — the message is passed up to the caller who shows it in an error dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code TemplateIOException} with a human-readable description of
     * the failure.
     *
     * @param message  a plain-English description of what went wrong (e.g. "Missing
     *                 required element 'id' in template file")
     */
    public TemplateIOException( String message )
    {
        super( message );
    }
}
