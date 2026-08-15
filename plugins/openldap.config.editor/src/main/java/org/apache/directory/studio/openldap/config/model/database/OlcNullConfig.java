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
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;


// ── CLASS: OlcNullConfig — Palpatine's Void Vault ─────────────────────────────
// Palpatine keeps a void vault — a backend that accepts all requests but stores
// nothing. It's like the trash compactor on the Death Star: everything goes in,
// nothing comes back out (except bind operations, optionally).
// The null backend is used for testing, benchmarking, or as a placeholder for
// suffix subtrees that don't need real storage. OlcNullConfig has exactly one
// setting beyond the base class: olcDbBindAllowed, which controls whether bind
// operations against this null database are permitted to succeed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcNullConfig} object class, representing the
 * configuration of the OpenLDAP null backend database.
 * The null backend accepts all operations but discards them — nothing is stored,
 * nothing is returned. It's useful for testing write performance or as a placeholder.
 * The only configurable option is whether bind operations are allowed to succeed.
 * Think of this as Palpatine's void vault — accepts everything, stores nothing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcNullConfig extends OlcDatabaseConfig
{
    /**
     * Field for the 'olcDbBindAllowed' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbBindAllowed", version="2.4.24")
    private Boolean olcDbBindAllowed;


    // ── getOlcDbBindAllowed — Palpatine Checks if Binds Are Permitted ─────────────
    // Palpatine checks whether the void vault allows bind operations to succeed.
    // If TRUE, LDAP clients can bind against this null database (useful for testing
    // authentication pipelines without needing real user data).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether bind operations are allowed to succeed against this null database.
     * If TRUE, binds are accepted (even though no data is stored). If FALSE or null,
     * binds return "No such object".
     *
     * <p>For example — Palpatine checks the bind permission:</p>
     * <pre>
     *   Boolean bindAllowed = nullConfig.getOlcDbBindAllowed(); // TRUE or FALSE
     * </pre>
     *
     * @return  TRUE if binds are permitted, FALSE or null if not
     */
    public Boolean getOlcDbBindAllowed()
    {
        return olcDbBindAllowed;
    }


    // ── setOlcDbBindAllowed — Palpatine Sets the Bind Permission ──────────────────
    // Palpatine configures whether the void vault permits bind operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether bind operations are allowed to succeed against this null database.
     *
     * <p>For example — Palpatine enables binds on the void vault:</p>
     * <pre>
     *   nullConfig.setOlcDbBindAllowed( Boolean.TRUE );
     * </pre>
     *
     * @param olcDbBindAllowed  TRUE to allow binds, FALSE or null to reject them
     */
    public void setOlcDbBindAllowed( Boolean olcDbBindAllowed )
    {
        this.olcDbBindAllowed = olcDbBindAllowed;
    }


    // ── getOlcDatabaseType — Palpatine Identifies the Vault Type ─────────────────
    // Palpatine identifies this vault as the "null" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "null", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the void vault type:</p>
     * <pre>
     *   nullConfig.getOlcDatabaseType(); // "null"
     * </pre>
     *
     * @return  the lowercase database type string "null"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.NULL.toString().toLowerCase();
    }
}
