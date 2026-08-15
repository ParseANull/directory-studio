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
package org.apache.directory.studio.openldap.config.model.overlay;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// ── CLASS: OlcAuditlogConfig — Vader's Suit Writing to a Physical Scroll ──────
// Unlike the accesslog overlay (which writes to an LDAP database), the auditlog
// overlay is simpler and more literal: it writes every LDAP modification to a plain
// text file on disk, in LDIF format, one record per write operation.
// Vader's equivalent: not a digital log in the Imperial database, but a physical
// scroll written by hand and locked in a personal vault — a flat, permanent record
// that doesn't depend on the broader infrastructure.
// OlcAuditlogConfig just holds the list of file paths to write those audit logs to.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcAuditlogConfig} object class, which configures the
 * OpenLDAP audit log (auditlog) overlay.
 * The auditlog overlay writes every write operation (add, modify, delete, modrdn)
 * to a flat LDIF file on disk. It's simpler than accesslog — no separate LDAP
 * database needed, just filesystem paths.
 * Think of this as Vader's physical scroll log — immutable, flat, always-on.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcAuditlogConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcAuditlogFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcAuditlogFile", version="2.4.0")
    private List<String> olcAuditlogFile = new ArrayList<>();


    // ── Default Constructor — Vader Opens the Physical Scroll Vault ───────────────
    // Vader initializes the audit log system with the overlay type name ("auditlog")
    // so OpenLDAP loads the right flat-file logging plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlcAuditlogConfig with the overlay type set to "auditlog".
     *
     * <p>For example — Vader opens the physical scroll vault:</p>
     * <pre>
     *   OlcAuditlogConfig auditLog = new OlcAuditlogConfig();
     *   auditLog.getOlcOverlay(); // "auditlog"
     * </pre>
     */
    public OlcAuditlogConfig()
    {
        super();
        olcOverlay = "auditlog";
    }


    // ── Copy Constructor — Vader Copies the Scroll Vault Config ──────────────────
    // Vader makes an exact copy of the scroll vault config for safe editing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given OlcAuditlogConfig.
     *
     * <p>For example — Vader copies the scroll config:</p>
     * <pre>
     *   OlcAuditlogConfig copy = new OlcAuditlogConfig( originalAuditlogConfig );
     * </pre>
     *
     * @param o  the OlcAuditlogConfig to copy
     */
    public OlcAuditlogConfig( OlcAuditlogConfig o )
    {
        super( o );
        olcAuditlogFile = new ArrayList<>( olcAuditlogFile );
    }


    // ── addOlcAuditlogFile — Vader Adds a Scroll File Path ───────────────────────
    // Vader adds another scroll file path — another destination for audit log records.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more file path strings to the olcAuditlogFile list.
     * Each path is a filesystem location where LDIF audit records are written.
     *
     * <p>For example — Vader adds a scroll file path:</p>
     * <pre>
     *   auditlogConfig.addOlcAuditlogFile( "/var/log/slapd/audit.ldif" );
     * </pre>
     *
     * @param strings  the file paths to add
     */
    public void addOlcAuditlogFile( String... strings )
    {
        for ( String string : strings )
        {
            olcAuditlogFile.add( string );
        }
    }


    // ── clearOlcAuditlogFile — Vader Clears the Scroll File List ─────────────────
    // Vader erases all file paths from the audit log configuration.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the list of audit log file paths.
     *
     * <p>For example — Vader clears the file list:</p>
     * <pre>
     *   auditlogConfig.clearOlcAuditlogFile();
     * </pre>
     */
    public void clearOlcAuditlogFile()
    {
        olcAuditlogFile.clear();
    }


    // ── getOlcAuditlogFile — Vader Reads the Scroll File Paths ───────────────────
    // Vader reads the list of file paths where audit logs are being written.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of filesystem paths where audit log LDIF records are written.
     *
     * <p>For example — Vader reads the scroll file paths:</p>
     * <pre>
     *   List&lt;String&gt; files = auditlogConfig.getOlcAuditlogFile();
     *   // ["/var/log/slapd/audit.ldif"]
     * </pre>
     *
     * @return  the live list of file path strings; never null
     */
    public List<String> getOlcAuditlogFile()
    {
        return olcAuditlogFile;
    }


    // ── setOlcAuditlogFile — Vader Replaces the Scroll File List ─────────────────
    // Vader replaces the entire list of audit log file paths.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire list of audit log file paths.
     *
     * <p>For example — Vader sets a new file list:</p>
     * <pre>
     *   auditlogConfig.setOlcAuditlogFile(
     *       Arrays.asList( "/var/log/slapd/audit.ldif" ) );
     * </pre>
     *
     * @param olcAuditlogFile  the new list of file path strings
     */
    public void setOlcAuditlogFile( List<String> olcAuditlogFile )
    {
        this.olcAuditlogFile = olcAuditlogFile;
    }


    // ── copy — Vader Duplicates the Auditlog Config ───────────────────────────────
    // Vader creates an exact copy of the audit log config for safe editing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcAuditlogConfig.
     *
     * <p>For example — Vader duplicates the auditlog config:</p>
     * <pre>
     *   OlcAuditlogConfig copy = auditlogConfig.copy();
     * </pre>
     *
     * @return  a new OlcAuditlogConfig with the same field values as this one
     */
    @Override
    public OlcAuditlogConfig copy()
    {
        return new OlcAuditlogConfig( this );
    }
}
