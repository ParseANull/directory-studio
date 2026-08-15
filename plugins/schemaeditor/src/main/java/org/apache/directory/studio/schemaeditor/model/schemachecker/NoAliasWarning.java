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


// ── CLASS: NoAliasWarning — Mace Windu Confronting Palpatine ─────────────────
// In the Jedi Council chamber Mace Windu spots the threat the others have missed:
// a schema object with no human-readable name (alias) — just a bare OID.  That's
// not necessarily wrong, but it's suspicious, like an unregistered Sith Lord.
// He doesn't raise an error — he raises a warning, keeping careful note of which
// object triggered his concern so it can be reviewed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link SchemaWarning} that flags a schema object which has no aliases
 * (human-readable names) — only a numeric OID.
 * Such objects are technically valid but are hard for users to identify; the
 * schema editor surfaces this as a warning in the Problems view.
 * Think of Mace raising his lightsaber at Palpatine: it's not a confirmed crime
 * yet, but the threat is real enough to call out clearly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NoAliasWarning implements SchemaWarning
{
    /** The source object */
    private SchemaObject source;


    // ── Mace Windu Raises His Hand: "This One Has No Name" ───────────────────
    // Mace records which object triggered the concern so the UI can highlight it.
    // Without the source reference, there's no way to point the user at the
    // offending element in the schema tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NoAliasWarning for the given schema object.
     * The schema object must be the actual instance from the schema handler —
     * the UI uses object identity to locate it in the tree.
     *
     * @param source  the schema object that has no aliases — must not be null
     */
    public NoAliasWarning( SchemaObject source )
    {
        this.source = source;
    }


    // ── Mace Points at the Suspect ────────────────────────────────────────────
    // Returns the schema object that triggered the warning so the Problems view
    // can navigate to it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public SchemaObject getSource()
    {
        return source;
    }
}
