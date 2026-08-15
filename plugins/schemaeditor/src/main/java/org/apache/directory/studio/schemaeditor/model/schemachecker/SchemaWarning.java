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
package org.apache.directory.studio.schemaeditor.model.schemachecker;


import org.apache.directory.api.ldap.model.schema.SchemaObject;


// ── INTERFACE: SchemaWarning — Mace Windu's Warning Protocol ─────────────────
// Mace Windu doesn't arrest Palpatine without evidence; he confronts him and
// keeps careful note of exactly which suspect he is watching.  A SchemaWarning
// follows the same protocol: it's not a hard error, but it names the specific
// schema object that deserves scrutiny.  Any concrete warning type implements
// this interface so the Problems view can display it and the SchemaChecker can
// collect all warnings uniformly, regardless of which specific rule was violated.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Marker interface for all schema warnings produced by {@link SchemaChecker}.
 * Every warning identifies the {@link SchemaObject} that triggered it, so the
 * UI can navigate directly to the offending element in the schema tree.
 * Current implementations: {@link NoAliasWarning}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface SchemaWarning
{
    // ── Mace Names the Suspect ────────────────────────────────────────────────
    // Every warning must be able to identify the schema object it is about;
    // without this the Problems view cannot link the warning to a tree node.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema object that triggered this warning.
     * The returned object is the live instance from the schema handler, so the
     * UI can use object identity to locate and highlight it.
     *
     * @return  the source schema object — never null
     */
    SchemaObject getSource();
}
