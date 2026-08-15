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

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// ── CLASS: OlcAccessLogConfig — Vader's Suit Recording Every Move ─────────────
// Vader's life-support suit doesn't just keep him alive — it logs everything:
// every command issued, every door opened, every breath. That data is written to
// a secure database so the Emperor can review it later. The accesslog overlay does
// the same for LDAP: it records every operation (add, modify, delete, search,
// bind) into a separate "log database" with timestamps and results.
// OlcAccessLogConfig configures that overlay: which database to log into
// (olcAccessLogDB), what "old" entry attribute values to capture before modify ops
// (olcAccessLogOld), which operation types to log (olcAccessLogOps), how long to
// retain log entries (olcAccessLogPurge), and whether to only log successes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcAccessLogConfig} object class, which configures the
 * OpenLDAP access log (accesslog) overlay.
 * The accesslog overlay logs LDAP operations to a separate LDAP database, providing
 * an audit trail of all changes and access patterns.
 * Think of this as Vader's suit recording every move to Palpatine's secure logs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcAccessLogConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcAccessLogDB' attribute.
     */
    @ConfigurationElement(attributeType = "olcAccessLogDB", isOptional = false, version="2.4.0")
    private Dn olcAccessLogDB;

    /**
     * Field for the 'olcAccessLogOld' attribute.
     */
    @ConfigurationElement(attributeType = "olcAccessLogOld", version="2.4.0")
    private String olcAccessLogOld;

    /**
     * Field for the 'olcAccessLogOldAttr' attribute.
     */
    @ConfigurationElement(attributeType = "olcAccessLogOldAttr", version="2.4.0")
    private List<String> olcAccessLogOldAttr = new ArrayList<>();

    /**
     * Field for the 'olcAccessLogOps' attribute.
     */
    @ConfigurationElement(attributeType = "olcAccessLogOps", version="2.4.0")
    private List<String> olcAccessLogOps = new ArrayList<>();

    /**
     * Field for the 'olcAccessLogPurge' attribute.
     */
    @ConfigurationElement(attributeType = "olcAccessLogPurge", version="2.4.0")
    private String olcAccessLogPurge;

    /**
     * Field for the 'olcAccessLogSuccess' attribute.
     */
    @ConfigurationElement(attributeType = "olcAccessLogSuccess", version="2.4.0")
    private Boolean olcAccessLogSuccess;


    // ── Default Constructor — Vader Powers Up the Recording System ────────────────
    // Vader's suit logs are initialized with the overlay type name ("accesslog")
    // so OpenLDAP loads the right logging plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlcAccessLogConfig with the overlay type set to "accesslog".
     *
     * <p>For example — Vader powers up the recording system:</p>
     * <pre>
     *   OlcAccessLogConfig accessLog = new OlcAccessLogConfig();
     *   accessLog.getOlcOverlay(); // "accesslog"
     * </pre>
     */
    public OlcAccessLogConfig()
    {
        super();
        olcOverlay = "accesslog";
    }


    // ── Copy Constructor — Vader Copies the Recording Config ─────────────────────
    // Vader makes an exact copy of the recording config so the editor can modify
    // it safely without altering the original.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given OlcAccessLogConfig.
     *
     * <p>For example — Vader copies the recording config:</p>
     * <pre>
     *   OlcAccessLogConfig copy = new OlcAccessLogConfig( originalAccessLogConfig );
     * </pre>
     *
     * @param o  the OlcAccessLogConfig to copy
     */
    public OlcAccessLogConfig( OlcAccessLogConfig o )
    {
        super( o );
        olcAccessLogDB = o.olcAccessLogDB;
        olcAccessLogOld = o.olcAccessLogOld;
        olcAccessLogOldAttr = new ArrayList<>( olcAccessLogOldAttr );
        olcAccessLogOps = new ArrayList<>( o.olcAccessLogOps );
        olcAccessLogPurge = o.olcAccessLogPurge;
        olcAccessLogSuccess = o.olcAccessLogSuccess;
    }


    // ── addOlcAccessLogOldAttr — Vader Adds an Attribute to Pre-Modify Capture ──
    // Vader adds another attribute to the list of values his suit captures from an
    // entry BEFORE a modify operation changes it — so the old state is preserved.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more attribute type names to the olcAccessLogOldAttr list.
     * These attributes have their pre-modification values captured in the log entry,
     * giving you a before/after view of the change.
     *
     * <p>For example — Vader adds userPassword to pre-modify capture:</p>
     * <pre>
     *   accessLogConfig.addOlcAccessLogOldAttr( "userPassword", "sn" );
     * </pre>
     *
     * @param strings  the attribute type names to capture before modifications
     */
    public void addOlcAccessLogOldAttr( String... strings )
    {
        for ( String string : strings )
        {
            olcAccessLogOldAttr.add( string );
        }
    }


    // ── addOlcAccessLogOps — Vader Adds an Operation Type to the Log ──────────────
    // Vader adds another operation type to the recording filter — which LDAP
    // operation categories his suit should capture (e.g., writes, reads, all).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more operation type strings to the olcAccessLogOps list.
     * Valid values include "writes", "reads", "session", "all" and specific op names.
     *
     * <p>For example — Vader enables write logging:</p>
     * <pre>
     *   accessLogConfig.addOlcAccessLogOps( "writes" );
     * </pre>
     *
     * @param strings  the operation type strings to log
     */
    public void addOlcAccessLogOps( String... strings )
    {
        for ( String string : strings )
        {
            olcAccessLogOps.add( string );
        }
    }


    // ── clearOlcAccessLogOldAttr — Vader Clears the Pre-Modify Attribute List ────
    // Vader erases the list of attributes to capture before modify operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the list of attributes captured before modifications.
     *
     * <p>For example — Vader clears the pre-modify capture list:</p>
     * <pre>
     *   accessLogConfig.clearOlcAccessLogOldAttr();
     * </pre>
     */
    public void clearOlcAccessLogOldAttr()
    {
        olcAccessLogOldAttr.clear();
    }


    // ── clearOlcAccessLogOps — Vader Clears the Operation Log Filter ─────────────
    // Vader clears the list of operation types being logged.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the list of logged operation types.
     *
     * <p>For example — Vader clears the log filter:</p>
     * <pre>
     *   accessLogConfig.clearOlcAccessLogOps();
     * </pre>
     */
    public void clearOlcAccessLogOps()
    {
        olcAccessLogOps.clear();
    }


    // ── getOlcAccessLogDB — Vader Reads the Log Database DN ──────────────────────
    // Vader checks which secure database his suit writes the audit records to.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN of the LDAP database where audit log entries are written.
     * This is the suffix DN of the separate log database (required — not optional).
     *
     * <p>For example — Vader reads the log database DN:</p>
     * <pre>
     *   Dn logDb = accessLogConfig.getOlcAccessLogDB();
     *   // e.g., cn=log
     * </pre>
     *
     * @return  the Dn of the log database
     */
    public Dn getOlcAccessLogDB()
    {
        return olcAccessLogDB;
    }


    // ── getOlcAccessLogOld — Vader Reads the Pre-Modify Filter ───────────────────
    // Vader reads the search filter that determines which entries have their old
    // attribute values captured before modifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search filter string that specifies which entries have their
     * pre-modification attribute values recorded (olcAccessLogOld).
     * When a modify operation matches this filter, the overlay captures the old values.
     *
     * <p>For example — Vader reads the pre-modify filter:</p>
     * <pre>
     *   String oldFilter = accessLogConfig.getOlcAccessLogOld();
     *   // e.g., "(objectClass=person)"
     * </pre>
     *
     * @return  the LDAP filter string, or null if not configured
     */
    public String getOlcAccessLogOld()
    {
        return olcAccessLogOld;
    }


    // ── getOlcAccessLogOldAttr — Vader Reads the Pre-Modify Attribute List ───────
    // Vader reads the list of attributes whose old values are captured before modifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of attribute type names captured before modifications.
     *
     * <p>For example — Vader reads the pre-modify attribute list:</p>
     * <pre>
     *   List&lt;String&gt; attrs = accessLogConfig.getOlcAccessLogOldAttr();
     *   // ["userPassword", "sn"]
     * </pre>
     *
     * @return  the live list of attribute names; never null
     */
    public List<String> getOlcAccessLogOldAttr()
    {
        return olcAccessLogOldAttr;
    }


    // ── getOlcAccessLogOps — Vader Reads the Logged Operation Types ──────────────
    // Vader reads which LDAP operation categories his suit is configured to log.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of LDAP operation type strings that are logged.
     *
     * <p>For example — Vader reads the operation log filter:</p>
     * <pre>
     *   List&lt;String&gt; ops = accessLogConfig.getOlcAccessLogOps();
     *   // ["writes"]
     * </pre>
     *
     * @return  the live list of operation type strings; never null
     */
    public List<String> getOlcAccessLogOps()
    {
        return olcAccessLogOps;
    }


    // ── getOlcAccessLogPurge — Vader Reads the Log Retention Policy ──────────────
    // Vader checks how old log entries can get before they're purged from the database —
    // even the Empire doesn't keep infinite records.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the log purge specification, which controls when old log entries are
     * deleted from the log database. Format is typically "age interval"
     * (e.g., "7+00:00:00 1+00:00:00" — keep 7 days, purge every 1 day).
     *
     * <p>For example — Vader reads the retention policy:</p>
     * <pre>
     *   String purge = accessLogConfig.getOlcAccessLogPurge(); // "7+00:00:00 1+00:00:00"
     * </pre>
     *
     * @return  the purge specification string, or null if not configured
     */
    public String getOlcAccessLogPurge()
    {
        return olcAccessLogPurge;
    }


    // ── getOlcAccessLogSuccess — Vader Checks the Success-Only Flag ──────────────
    // Vader checks whether to log only successful operations or all attempts.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether only successful operations are logged.
     * If TRUE, failed operations (e.g., invalid credentials) are not logged.
     * If FALSE (default), both successful and failed operations are recorded.
     *
     * <p>For example — Vader checks the success-only flag:</p>
     * <pre>
     *   Boolean successOnly = accessLogConfig.getOlcAccessLogSuccess(); // TRUE or FALSE
     * </pre>
     *
     * @return  TRUE to log successes only, FALSE to log all, null if unconfigured
     */
    public Boolean getOlcAccessLogSuccess()
    {
        return olcAccessLogSuccess;
    }


    // ── setOlcAccessLogDB — Vader Sets the Log Database ──────────────────────────
    // Vader points his recording system at a specific secure database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN of the LDAP database to write audit log entries into.
     *
     * <p>For example — Vader sets the log database:</p>
     * <pre>
     *   accessLogConfig.setOlcAccessLogDB( new Dn( "cn=log" ) );
     * </pre>
     *
     * @param olcAccessLogDB  the Dn of the log database (required)
     */
    public void setOlcAccessLogDB( Dn olcAccessLogDB )
    {
        this.olcAccessLogDB = olcAccessLogDB;
    }


    // ── setOlcAccessLogOld — Vader Sets the Pre-Modify Entry Filter ──────────────
    // Vader sets the filter that selects which entries trigger pre-modify capture.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the search filter that triggers pre-modification value capture.
     *
     * <p>For example — Vader sets the pre-modify filter:</p>
     * <pre>
     *   accessLogConfig.setOlcAccessLogOld( "(objectClass=person)" );
     * </pre>
     *
     * @param olcAccessLogOld  the LDAP search filter string
     */
    public void setOlcAccessLogOld( String olcAccessLogOld )
    {
        this.olcAccessLogOld = olcAccessLogOld;
    }


    // ── setOlcAccessLogOldAttr — Vader Sets the Pre-Modify Attribute List ────────
    // Vader replaces the full list of attributes to capture before modifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the list of attributes captured before modifications.
     *
     * <p>For example — Vader sets the pre-modify attribute list:</p>
     * <pre>
     *   accessLogConfig.setOlcAccessLogOldAttr( Arrays.asList( "userPassword" ) );
     * </pre>
     *
     * @param olcAccessLogOldAttr  the new list of attribute names
     */
    public void setOlcAccessLogOldAttr( List<String> olcAccessLogOldAttr )
    {
        this.olcAccessLogOldAttr = olcAccessLogOldAttr;
    }


    // ── setOlcAccessLogOps — Vader Sets the Logged Operation Types ───────────────
    // Vader replaces the full list of operation types to log.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the list of LDAP operation types to log.
     *
     * <p>For example — Vader sets the operation log filter:</p>
     * <pre>
     *   accessLogConfig.setOlcAccessLogOps( Arrays.asList( "writes" ) );
     * </pre>
     *
     * @param olcAccessLogOps  the new list of operation type strings
     */
    public void setOlcAccessLogOps( List<String> olcAccessLogOps )
    {
        this.olcAccessLogOps = olcAccessLogOps;
    }


    // ── setOlcAccessLogPurge — Vader Sets the Log Retention Policy ───────────────
    // Vader sets how long log entries are kept before being purged.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the log purge specification controlling retention of old log entries.
     *
     * <p>For example — Vader sets the purge policy:</p>
     * <pre>
     *   accessLogConfig.setOlcAccessLogPurge( "7+00:00:00 1+00:00:00" );
     * </pre>
     *
     * @param olcAccessLogPurge  the purge specification string
     */
    public void setOlcAccessLogPurge( String olcAccessLogPurge )
    {
        this.olcAccessLogPurge = olcAccessLogPurge;
    }


    // ── setOlcAccessLogSuccess — Vader Sets the Success-Only Flag ────────────────
    // Vader configures whether to log only successful operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether to log only successful operations.
     *
     * <p>For example — Vader enables success-only logging:</p>
     * <pre>
     *   accessLogConfig.setOlcAccessLogSuccess( Boolean.TRUE );
     * </pre>
     *
     * @param olcAccessLogSuccess  TRUE to log successes only, FALSE to log all operations
     */
    public void setOlcAccessLogSuccess( Boolean olcAccessLogSuccess )
    {
        this.olcAccessLogSuccess = olcAccessLogSuccess;
    }


    // ── copy — Vader Copies the Access Log Config ─────────────────────────────────
    // Vader creates a duplicate of the recording config for safe editing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcAccessLogConfig.
     *
     * <p>For example — Vader copies the access log config:</p>
     * <pre>
     *   OlcAccessLogConfig copy = accessLogConfig.copy();
     * </pre>
     *
     * @return  a new OlcAccessLogConfig with the same field values as this one
     */
    @Override
    public OlcAccessLogConfig copy()
    {
        return new OlcAccessLogConfig( this );
    }
}
