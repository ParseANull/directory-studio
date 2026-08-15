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


// ── CLASS: OlcNdbConfig — Palpatine's MySQL Cluster Vault ─────────────────────
// Palpatine has a vault backed by MySQL Cluster (NDB) — a distributed, in-memory
// database that provides high availability at the cost of complexity.
// The NDB backend stores LDAP entries in MySQL NDB (Network DataBase) tables,
// giving LDAP operations the reliability of a clustered SQL engine.
// OlcNdbConfig has no additional fields beyond the base database config
// (note: original Javadoc incorrectly says olcMonitorConfig).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcNdbConfig} object class (note: the original Javadoc
 * incorrectly says olcMonitorConfig), representing the configuration of the
 * OpenLDAP NDB backend database.
 * The NDB backend stores LDAP entries in a MySQL NDB (MySQL Cluster) database.
 * It inherits all settings from {@link OlcDatabaseConfig} and adds none of its own.
 * Think of this as Palpatine's distributed MySQL Cluster vault.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcNdbConfig extends OlcDatabaseConfig
{
    // No other fields than those inherited from the 'OlcDatabaseConfig' class


    // ── getOlcDatabaseType — Palpatine Identifies the NDB Vault Type ──────────────
    // Palpatine identifies this vault as the "ndb" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "ndb", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the NDB vault type:</p>
     * <pre>
     *   ndbConfig.getOlcDatabaseType(); // "ndb"
     * </pre>
     *
     * @return  the lowercase database type string "ndb"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.NDB.toString().toLowerCase();
    }
}
