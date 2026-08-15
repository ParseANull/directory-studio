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


// ── CLASS: OlcLdifConfig — Palpatine's Flat-File LDIF Vault ──────────────────
// Palpatine keeps one vault that stores all its data as LDIF files on the
// filesystem — simple, portable, human-readable. Each entry is stored as a
// separate LDIF file in a directory tree. It's not the fastest vault, but it
// requires no database engine and can be inspected with a text editor.
// The LDIF backend stores LDAP entries as LDIF files on disk, one per entry.
// OlcLdifConfig has one key setting: olcDbDirectory, the root directory path.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcLdifConfig} object class, representing the
 * configuration of the OpenLDAP LDIF backend database.
 * The LDIF backend stores each LDAP entry as a separate LDIF-formatted file
 * on the filesystem, organized in a directory hierarchy that mirrors the DIT.
 * The only required configuration is the root directory path (olcDbDirectory).
 * Think of this as Palpatine's flat-file vault — no DB engine needed, just
 * organized text files in a directory.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcLdifConfig extends OlcDatabaseConfig
{
    /**
     * Field for the 'olcDbDirectory' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbDirectory", isOptional = false, version="2.4.0")
    private String olcDbDirectory;


    // ── getOlcDbDirectory — Palpatine Reads the Vault Root Directory ──────────────
    // Palpatine checks which directory on the filesystem this vault's LDIF files
    // are stored in — the root of the LDIF entry tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the filesystem path to the root directory where LDIF entry files are stored.
     * This is required — the LDIF backend needs a place to read and write its files.
     *
     * <p>For example — Palpatine reads the vault root:</p>
     * <pre>
     *   String dir = ldifConfig.getOlcDbDirectory(); // "/var/lib/ldap/ldif"
     * </pre>
     *
     * @return  the directory path string, or null if not yet set
     */
    public String getOlcDbDirectory()
    {
        return olcDbDirectory;
    }


    // ── setOlcDbDirectory — Palpatine Sets the Vault Root Directory ──────────────
    // Palpatine sets which directory on the filesystem this vault's LDIF files live in.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the filesystem path to the root directory for LDIF entry storage.
     *
     * <p>For example — Palpatine sets the vault root:</p>
     * <pre>
     *   ldifConfig.setOlcDbDirectory( "/var/lib/ldap/ldif" );
     * </pre>
     *
     * @param olcDbDirectory  the directory path (required — LDIF backend won't start without it)
     */
    public void setOlcDbDirectory( String olcDbDirectory )
    {
        this.olcDbDirectory = olcDbDirectory;
    }


    // ── getOlcDatabaseType — Palpatine Identifies the LDIF Vault Type ─────────────
    // Palpatine identifies this vault as the "ldif" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "ldif", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the LDIF vault type:</p>
     * <pre>
     *   ldifConfig.getOlcDatabaseType(); // "ldif"
     * </pre>
     *
     * @return  the lowercase database type string "ldif"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.LDIF.toString().toLowerCase();
    }
}
