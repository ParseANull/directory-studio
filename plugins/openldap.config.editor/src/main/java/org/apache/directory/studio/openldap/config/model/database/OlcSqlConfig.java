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


// ── CLASS: OlcSqlConfig — Palpatine's SQL Vault ───────────────────────────────
// Palpatine has a vault that maps his LDAP directory entries to rows in a
// relational SQL database — the SQL backend is a bridge between the LDAP world
// and traditional relational database storage. LDAP queries get translated into
// SQL queries against whatever database the admin has configured.
// OlcSqlConfig has no additional fields beyond the base database config
// (note: original Javadoc incorrectly says olcMonitorConfig).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcSqlConfig} object class (note: the original Javadoc
 * incorrectly says olcMonitorConfig), representing the configuration of the
 * OpenLDAP SQL backend database.
 * The SQL backend stores LDAP entries as rows in a relational database, translating
 * LDAP operations into SQL queries. It inherits all settings from {@link OlcDatabaseConfig}.
 * Think of this as Palpatine's relational SQL vault — LDAP on the outside, SQL inside.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcSqlConfig extends OlcDatabaseConfig
{
    // No other fields than those inherited from the 'OlcDatabaseConfig' class


    // ── getOlcDatabaseType — Palpatine Identifies the SQL Vault Type ──────────────
    // Palpatine identifies this vault as the "sql" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "sql", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the SQL vault type:</p>
     * <pre>
     *   sqlConfig.getOlcDatabaseType(); // "sql"
     * </pre>
     *
     * @return  the lowercase database type string "sql"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.SQL.toString().toLowerCase();
    }
}
