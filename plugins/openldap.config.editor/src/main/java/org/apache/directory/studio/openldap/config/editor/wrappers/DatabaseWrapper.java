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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: DatabaseWrapper — Han Solo's Cargo Hold Manifest ───────────────────
// Han Solo doesn't care what's in the hold — he just knows which hold it came
// from.  DatabaseWrapper is his manifest entry: it wraps one OlcDatabaseConfig
// object (a cargo hold) so the UI can reference it without knowing the details.
// You can open an empty hold (default constructor) or one pre-loaded with cargo
// (parameterized constructor), and toString forwards to the database's own
// description.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A thin UI wrapper around an {@link OlcDatabaseConfig} instance.
 * The wrapper is used by the Databases page to hold, display, and edit a
 * single database entry in the configuration.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DatabaseWrapper
{
    /** The wrapped database */
    private OlcDatabaseConfig database;


    // ── Default Constructor — An Empty Cargo Hold ──────────────────────────────
    // Han logs a new hold in the manifest before it's been loaded — the database
    // reference starts null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of DatabaseWrapper.
     */
    public DatabaseWrapper()
    {
    }


    // ── Parameterized Constructor — A Pre-Loaded Cargo Hold ───────────────────
    // The hold already has cargo in it when we register it — we take the
    // OlcDatabaseConfig reference directly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of DatabaseWrapper.
     *
     * @param database the wrapped database
     */
    public DatabaseWrapper( OlcDatabaseConfig database )
    {
        this.database = database;
    }


    // ── getDatabase — Open the Hold and Inspect the Cargo ─────────────────────
    // Han opens the cargo hold and sees what's inside.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Gets the wrapped database.
     *
     * @return the wrapped database
     */
    public OlcDatabaseConfig getDatabase()
    {
        return database;
    }


    // ── setDatabase — Swap the Cargo in the Hold ───────────────────────────────
    // Han loads a different cargo unit into the hold.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the wrapped database.
     *
     * @param database the wrapped database
     */
    public void setDatabase( OlcDatabaseConfig database )
    {
        this.database = database;
    }


    // ── toString — Read the Cargo Hold's Label ────────────────────────────────
    // Han reads the label on the crate — we delegate to the database's own
    // toString rather than duplicating the description here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return database.toString();
    }
}
