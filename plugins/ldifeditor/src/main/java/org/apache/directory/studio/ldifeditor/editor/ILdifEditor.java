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

package org.apache.directory.studio.ldifeditor.editor;


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: ILdifEditor — REBEL EDITOR CONTRACT ────────────────────────────────
// Every Rebel communications officer who handles LDIF communiqués must be able
// to produce two things: the current parsed model of the file, and the
// connection to the LDAP directory the file targets.
// ILdifEditor is that contract: both the full Eclipse editor and the embedded
// widget implement it so actions and configuration components can work
// against either without knowing which one they are talking to.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Marker interface for LDIF editor implementations.
 * Both the standalone {@code LdifEditor} and the embedded
 * {@code LdifEditorWidget} implement this interface, allowing actions and
 * configuration components to work with either without type-checking.
 * Think of this as the Rebel communications officer contract: every officer
 * must be able to hand over the parsed model and the active connection.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ILdifEditor extends IAdaptable
{
    // ── RETURN THE PARSED LDIF MODEL ──────────────────────────────────────────
    // The officer hands over the fully parsed representation of the
    // communiqué as a structured LdifFile object.
    /**
     * Returns the parsed {@link LdifFile} model for the document currently
     * open in this editor.
     *
     * @return the LDIF model
     */
    LdifFile getLdifModel();


    // ── RETURN THE ACTIVE CONNECTION ──────────────────────────────────────────
    // The officer provides the LDAP directory connection that the file targets
    // so schema-aware features (content assist, value editors) can use it.
    /**
     * Returns the {@link IBrowserConnection} associated with this editor,
     * or {@code null} if no connection is set.
     *
     * @return the browser connection, or {@code null}
     */
    IBrowserConnection getConnection();
}
