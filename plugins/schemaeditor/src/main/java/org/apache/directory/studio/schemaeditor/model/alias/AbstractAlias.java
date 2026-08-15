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


// ── CLASS: AbstractAlias — C-3PO Stores The Droid's Translation ──────────────
// C-3PO has memorised six million languages. For the alias dialect, every alias
// text is stored in his memory banks the moment it is parsed. Subclasses just
// inherit that storage — they don't need to re-implement getAlias() or toString()
// because C-3PO already handles the translation layer.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base implementation of the {@link Alias} interface that stores the alias
 * string and provides {@link #getAlias()} and {@link #toString()}. Concrete
 * subclasses ({@link DefaultAlias}, {@link AbstractAliasWithError}) extend this
 * rather than re-implementing the storage from scratch.
 * Think of this as C-3PO's core translation module: once an alias text is
 * loaded into memory, any subclass can retrieve it without writing extra code.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractAlias implements Alias
{
    /** The alias */
    private String alias;


    // ── C-3PO Loads The Dialect String Into Memory ───────────────────────────────
    // The moment C-3PO encounters a new Jawa term, he stores it in his linguistic
    // database. Every concrete alias implementation calls super(alias) here so the
    // text is available for getAlias() and toString() without further ceremony.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the alias string for later retrieval. All concrete alias constructors
     * chain up to this so we don't duplicate the field assignment in every subclass.
     *
     * @param alias  the raw alias text as parsed from the input string
     */
    public AbstractAlias( String alias )
    {
        this.alias = alias;
    }


    // ── C-3PO Reads Back The Stored Dialect Term ─────────────────────────────────
    // C-3PO consults his memory banks and reads out the alias text verbatim.
    // No processing — just the raw string that was loaded during construction.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw alias string stored at construction time. Implements the
     * {@link Alias} contract — callers use this to get the text for display,
     * validation, or serialisation.
     *
     * @return  the alias text; never null
     */
    public String getAlias()
    {
        return alias;
    }


    // ── C-3PO Announces The Alias To The Room ────────────────────────────────────
    // "The droid's name is R2-D2." C-3PO speaks the alias aloud whenever someone
    // asks for a string representation — handy for logging and debugging.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the alias string — same as {@link #getAlias()}. Useful for IDE
     * debugger displays and log output without having to call getAlias() explicitly.
     *
     * @return  the alias text
     */
    public String toString()
    {
        return alias;
    }
}
