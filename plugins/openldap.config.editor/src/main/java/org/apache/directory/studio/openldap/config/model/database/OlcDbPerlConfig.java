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


// ── CLASS: OlcDbPerlConfig — Palpatine Delegating Operations to Perl Scripts ──
// Even Palpatine sometimes delegates data handling to a scripting layer — in this
// case Perl modules that implement the LDAP backend logic. The Perl backend loads
// a user-written Perl module and calls into it for each LDAP operation.
// OlcDbPerlConfig has no additional configuration fields beyond the base class
// (note: original Javadoc incorrectly says olcMonitorConfig).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcDbPerlConfig} object class (note: the original Javadoc
 * incorrectly says olcMonitorConfig), representing the Perl backend database.
 * The Perl backend delegates LDAP operations to a Perl module, allowing custom
 * data-handling logic to be implemented in Perl.
 * Inherits all settings from {@link OlcDatabaseConfig}.
 * Think of this as Palpatine's Perl-scripted data delegation backend.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcDbPerlConfig extends OlcDatabaseConfig
{
    // ── getOlcDatabaseType — Palpatine Identifies the Perl Vault Type ─────────────
    // Palpatine identifies this vault as the "perl" type so OpenLDAP loads the
    // correct Perl backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier for the Perl backend, used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the Perl vault type:</p>
     * <pre>
     *   perlConfig.getOlcDatabaseType(); // "perl"
     * </pre>
     *
     * @return  the lowercase database type string for the Perl backend
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.DB_PERL.toString().toLowerCase();
    }
}
