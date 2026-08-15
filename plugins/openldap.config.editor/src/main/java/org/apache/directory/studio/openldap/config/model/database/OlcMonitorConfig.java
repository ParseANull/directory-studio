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


// ── CLASS: OlcMonitorConfig — Palpatine's Internal Status Vault ───────────────
// Even Palpatine keeps an eye on the Death Star's own health metrics — power
// consumption, troop movements, system load. The monitor backend is the Empire's
// internal status vault: a read-only LDAP database that exposes slapd's own
// runtime statistics (connections, operations, threads) under cn=Monitor.
// OlcMonitorConfig is the bean for this database type — it has no additional
// settings beyond what OlcDatabaseConfig provides, because the monitor backend
// is built-in and has no tunable parameters.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcMonitorConfig} object class, representing the
 * configuration of the OpenLDAP monitor backend database.
 * The monitor backend provides a read-only LDAP view of slapd's runtime statistics
 * under the {@code cn=Monitor} suffix — connections, operations, threads, and more.
 * It inherits all settings from {@link OlcDatabaseConfig} and adds none of its own.
 * Think of this as Palpatine's internal status vault — self-contained, read-only,
 * exposing the Empire's own operational metrics.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcMonitorConfig extends OlcDatabaseConfig
{
    // No other fields than those inherited from the 'OlcDatabaseConfig' class


    // ── getOlcDatabaseType — Palpatine Identifies the Vault Type ─────────────────
    // Palpatine identifies this vault as the "monitor" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "monitor", which is used as the
     * type discriminator in the cn=config DIT
     * (e.g., olcDatabase={-1}monitor,cn=config).
     *
     * <p>For example — Palpatine identifies the vault type:</p>
     * <pre>
     *   monitorConfig.getOlcDatabaseType(); // "monitor"
     * </pre>
     *
     * @return  the lowercase database type string "monitor"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.MONITOR.toString().toLowerCase();
    }
}
