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


// ── CLASS: OlcShellConfig — Palpatine Delegating Operations to Shell Scripts ──
// Even Palpatine sometimes delegates grunt-work to external agents — shell scripts
// that handle each LDAP operation type (search, add, modify, delete) by running
// an external program with the operation data piped in.
// The shell backend is OpenLDAP's "call an external script" backend — it spawns
// a shell process for each operation. OlcShellConfig has no additional attributes
// beyond the base class because the shell commands are configured through
// inherited attributes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcShellConfig} object class (note: the original Javadoc
 * incorrectly says olcMonitorConfig), representing the configuration of the
 * OpenLDAP shell backend database.
 * The shell backend delegates LDAP operations to external shell commands/scripts.
 * It inherits all settings from {@link OlcDatabaseConfig} and adds none of its own.
 * Think of this as Palpatine delegating each operation to an external agent
 * via shell script — adaptable but entirely dependent on the external script.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcShellConfig extends OlcDatabaseConfig
{
    // No other fields than those inherited from the 'OlcDatabaseConfig' class


    // ── getOlcDatabaseType — Palpatine Identifies the Shell Vault Type ────────────
    // Palpatine identifies this vault as the "shell" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "shell", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the shell vault type:</p>
     * <pre>
     *   shellConfig.getOlcDatabaseType(); // "shell"
     * </pre>
     *
     * @return  the lowercase database type string "shell"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.SHELL.toString().toLowerCase();
    }
}
