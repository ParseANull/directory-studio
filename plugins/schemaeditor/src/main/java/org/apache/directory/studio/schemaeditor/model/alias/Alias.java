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
package org.apache.directory.studio.schemaeditor.model.alias;


// ── CLASS: Alias — C-3PO Reading The Jawa Dialect ────────────────────────────
// C-3PO prides himself on knowing over six million forms of communication. In
// the Jawa camp, every droid has a shorthand name in the sand-people's dialect
// — that's the alias. This interface is the contract: any object that has a
// readable shorthand name is an Alias, and you can always ask it for that name.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The base contract for anything that carries an alias string — a human-readable
 * shorthand name for a schema element. Implementations may be valid, carry errors,
 * or represent partial parses; this interface just guarantees the alias string
 * is accessible.
 * Think of this as C-3PO's universal translation contract: any dialect of alias
 * must provide a way to get the raw text back.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface Alias
{
    // ── C-3PO Reads The Name Off The Droid's Tag ─────────────────────────────────
    // In the Jawa dialect, each droid has a tag. C-3PO reads it aloud so the
    // crew knows what they're dealing with. This is the one thing every alias
    // implementation must be able to do.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw alias string as it appeared in the input. May contain
     * error characters if the implementation is an {@link AliasWithError} —
     * callers that need clean names should check the type first.
     *
     * @return  the alias text; never null
     */
    String getAlias();
}
