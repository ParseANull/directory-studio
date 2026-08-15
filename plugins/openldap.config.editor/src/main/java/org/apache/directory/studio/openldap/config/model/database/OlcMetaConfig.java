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


// ── CLASS: OlcMetaConfig — Palpatine's Meta-Vault Aggregating Remote Servers ──
// Palpatine's meta-vault doesn't hold its own data — it presents a unified view
// of multiple remote LDAP servers as if they were one directory. The meta backend
// is like Palpatine's galaxy-wide information network: queries go in, get routed
// to the right remote server, and the results come back as a single seamless tree.
// OlcMetaConfig has no additional fields beyond the base database config because
// the target servers and mapping rules are configured through base-class attributes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcMetaConfig} object class, representing the
 * configuration of the OpenLDAP meta backend database.
 * The meta backend presents a virtual aggregated view of multiple remote LDAP servers
 * as a unified directory tree. It inherits all settings from {@link OlcDatabaseConfig}.
 * Think of this as Palpatine's meta-vault — a unified view across multiple remote vaults.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcMetaConfig extends OlcDatabaseConfig
{
    // ── getOlcDatabaseType — Palpatine Identifies the Meta Vault Type ─────────────
    // Palpatine identifies this vault as the "meta" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "meta", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the meta vault type:</p>
     * <pre>
     *   metaConfig.getOlcDatabaseType(); // "meta"
     * </pre>
     *
     * @return  the lowercase database type string "meta"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.META.toString().toLowerCase();
    }
}
