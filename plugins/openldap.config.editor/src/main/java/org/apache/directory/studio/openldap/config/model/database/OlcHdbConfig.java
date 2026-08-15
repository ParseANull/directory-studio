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
package org.apache.directory.studio.openldap.config.model.database;


import org.apache.directory.studio.openldap.common.ui.model.DatabaseTypeEnum;


// ── CLASS: OlcHdbConfig — Palpatine's Hierarchical BDB Vault ──────────────────
// Palpatine's HDB vault is an enhanced version of his BDB vault — it uses the same
// BerkeleyDB engine underneath but adds hierarchical DN indexing to speed up
// subtree searches. All the same configuration options apply (inherited from
// OlcBdbConfig) — HDB is just BDB with smarter DN indexing built in.
// OlcHdbConfig extends OlcBdbConfig and only overrides the database type identifier.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcHdbConfig} object class, representing the
 * HDB (Hierarchical Database) backend, which is BerkeleyDB with additional
 * hierarchical DN indexing for faster subtree and sub-suffix searches.
 * All configuration is inherited from {@link OlcBdbConfig} — HDB and BDB share
 * the same configuration schema; only the type name differs.
 * Think of this as Palpatine's advanced BDB vault with hierarchical index support.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcHdbConfig extends OlcBdbConfig
{
    // ── getOlcDatabaseType — Palpatine Identifies the HDB Vault Type ──────────────
    // Palpatine identifies this vault as the "hdb" type so OpenLDAP loads the
    // correct hierarchical BDB backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "hdb", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the HDB vault type:</p>
     * <pre>
     *   hdbConfig.getOlcDatabaseType(); // "hdb"
     * </pre>
     *
     * @return  the lowercase database type string "hdb"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.HDB.toString().toLowerCase();
    }
}
