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


// ── CLASS: OlcPasswdConfig — Palpatine's Password Vault ───────────────────────
// The Empire keeps a password vault — the system /etc/passwd file — as a special
// read-only LDAP database that exposes local Unix accounts as LDAP entries.
// Palpatine can use it to authenticate users from the host OS without replicating
// all the account data into a full LDAP directory. It's minimal, read-only, and
// inherits all its settings from the base database config.
// OlcPasswdConfig is the bean for this database type with no additional attributes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcPasswdConfig} object class (note: original Javadoc
 * incorrectly says olcMonitorConfig), representing the passwd backend database.
 * The passwd backend exposes Unix /etc/passwd entries as read-only LDAP objects,
 * allowing LDAP clients to browse local OS user accounts.
 * It inherits all settings from {@link OlcDatabaseConfig} and adds none of its own.
 * Think of this as Palpatine's minimal OS password vault — read-only, no custom
 * configuration needed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcPasswdConfig extends OlcDatabaseConfig
{
    // No other fields than those inherited from the 'OlcDatabaseConfig' class


    // ── getOlcDatabaseType — Palpatine Identifies the Vault Type ─────────────────
    // Palpatine identifies this vault as the "passwd" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "passwd", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the vault type:</p>
     * <pre>
     *   passwdConfig.getOlcDatabaseType(); // "passwd"
     * </pre>
     *
     * @return  the lowercase database type string "passwd"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.PASSWD.toString().toLowerCase();
    }
}
