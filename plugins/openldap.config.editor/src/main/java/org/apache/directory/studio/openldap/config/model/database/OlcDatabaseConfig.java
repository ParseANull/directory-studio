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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcConfig;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// ── CLASS: OlcDatabaseConfig — Lando Running Cloud City's Master Records ────────
// Lando Calrissian doesn't just own Cloud City — he manages every aspect of it:
// who has access (olcAccess), what the colony is known as (olcSuffix), who the
// administrator is (olcRootDN), whether the station is read-only mode (olcReadOnly),
// how many records can be returned per query (olcSizeLimit), replication settings,
// security requirements, and more. OlcDatabaseConfig is exactly that — the common
// master configuration record for any OpenLDAP database backend, regardless of
// whether it's BDB, MDB, LDAP proxy, shell, or anything else. Every real database
// class (OlcMdbConfig, OlcBdbConfig, OlcLDAPConfig, etc.) extends this class.
// The overlays list glues the database to its augmentation modules (ppolicy, memberOf, etc.).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcDatabaseConfig} object class. It stores the common parameters
 * for any DB:
 * <ul>
 *   <li>{@code olcAccess} — access control rules for this database (multi-valued, ordered)</li>
 *   <li>{@code olcAddContentAcl} — if true, use content of the Add request as the ACL target</li>
 *   <li>{@code olcDatabase} (MUST) — the RDN name and type of the database</li>
 *   <li>{@code olcDisabled} — if true, the database is disabled and slapd won't serve it</li>
 *   <li>{@code olcExtraAttrs} — additional attributes to return in search results</li>
 *   <li>{@code olcHidden} — if true, the database is hidden from rootDSE searches</li>
 *   <li>{@code olcLastMod} — if true, slapd automatically updates modifyTimestamp and modifiersName</li>
 *   <li>{@code olcLimits} — per-user resource limits (size, time, etc.)</li>
 *   <li>{@code olcMaxDerefDepth} — max alias dereferencing depth</li>
 *   <li>{@code olcMirrorMode} — if true, this is a mirror-mode replica (can accept writes)</li>
 *   <li>{@code olcMonitoring} — if true, expose database statistics in cn=Monitor</li>
 *   <li>{@code olcPlugin} — list of plugin module specifications</li>
 *   <li>{@code olcReadOnly} — if true, the database is read-only</li>
 *   <li>{@code olcReplica} — obsolete replication spec (use syncrepl instead)</li>
 *   <li>{@code olcReplicaArgsFile}, {@code olcReplicaPidFile}, {@code olcReplicationInterval}, {@code olcReplogFile} — legacy replication</li>
 *   <li>{@code olcRequires} — list of required conditions for operations (e.g., authc)</li>
 *   <li>{@code olcRestrict} — list of restricted operations</li>
 *   <li>{@code olcRootDN} — the DN of the database administrator</li>
 *   <li>{@code olcRootPW} — the hashed password for the root DN</li>
 *   <li>{@code olcSchemaDN} — the DN where schema is published</li>
 *   <li>{@code olcSecurity} — security strength factor requirements</li>
 *   <li>{@code olcSizeLimit} — max entries returned per search</li>
 *   <li>{@code olcSubordinate} — marks this database as subordinate to another</li>
 *   <li>{@code olcSuffix} — the naming context (DN suffix) this database serves</li>
 *   <li>{@code olcSyncrepl} — syncrepl replication consumer configuration</li>
 *   <li>{@code olcSyncUseSubentry} — if true, use a subentry for sync cookies</li>
 *   <li>{@code olcTimeLimit} — max time for search operations</li>
 *   <li>{@code olcUpdateDN} — DN allowed to make updates (for slaves)</li>
 *   <li>{@code olcUpdateRef} — referral to send for update operations on a slave</li>
 * </ul>
 * Plus a transient {@code overlays} list (not stored as an LDAP attribute) that holds
 * the {@link OlcOverlayConfig} objects attached to this database.
 * Think of this as Lando's master Cloud City administration record — every operational
 * knob for any database backend is defined here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcDatabaseConfig extends OlcConfig
{
    /**
     * Field for the 'olcAccess' attribute, which is an ordered multi-value String attribute
     */
    @ConfigurationElement(attributeType = "olcAccess", version="2.4.0")
    private List<String> olcAccess = new ArrayList<>();


    /**
     * Field for the 'olcAddContentAcl' attribute.
     */
    @ConfigurationElement(attributeType = "olcAddContentAcl", version="2.4.13")
    private Boolean olcAddContentAcl;

    /**
     * Field for the 'olcDatabase' attribute.
     */
    @ConfigurationElement(attributeType = "olcDatabase", isOptional = false, isRdn = true, version="2.4.0")
    private String olcDatabase;

    /**
     * Field for the 'olcDisabled' attribute. (Added in OpenLDAP 2.4.36)
     */
    @ConfigurationElement(attributeType = "olcDisabled", version="2.4.36")
    private Boolean olcDisabled;

    /**
     * Field for the 'olcExtraAttrs' attribute. (Added in OpenLDAP 2.4.22)
     */
    @ConfigurationElement(attributeType = "olcExtraAttrs", version="2.4.22")
    private List<String> olcExtraAttrs;

    /**
     * Field for the 'olcHidden' attribute.
     */
    @ConfigurationElement(attributeType = "olcHidden", version="2.4.0")
    private Boolean olcHidden;

    /**
     * Field for the 'olcLastMod' attribute.
     */
    @ConfigurationElement(attributeType = "olcLastMod", version="2.4.0")
    private Boolean olcLastMod;

    /**
     * Field for the 'olcLimits' attribute.
     */
    @ConfigurationElement(attributeType = "olcLimits", version="2.4.0")
    private List<String> olcLimits = new ArrayList<>();

    /**
     * Field for the 'olcMaxDerefDepth' attribute.
     */
    @ConfigurationElement(attributeType = "olcMaxDerefDepth", version="2.4.0")
    private Integer olcMaxDerefDepth;

    /**
     * Field for the 'olcMirrorMode' attribute.
     */
    @ConfigurationElement(attributeType = "olcMirrorMode", version="2.4.0")
    private Boolean olcMirrorMode;

    /**
     * Field for the 'olcMonitoring' attribute.
     */
    @ConfigurationElement(attributeType = "olcMonitoring", version="2.4.0")
    private Boolean olcMonitoring;

    /**
     * Field for the 'olcPlugin' attribute.
     */
    @ConfigurationElement(attributeType = "olcPlugin", version="2.4.0")
    private List<String> olcPlugin = new ArrayList<>();

    /**
     * Field for the 'olcReadOnly' attribute.
     */
    @ConfigurationElement(attributeType = "olcReadOnly", version="2.4.0")
    private Boolean olcReadOnly;

    /**
     * Field for the 'olcReplica' attribute.
     */
    @ConfigurationElement(attributeType = "olcReplica", version="2.4.0")
    private List<String> olcReplica = new ArrayList<>();

    /**
     * Field for the 'olcReplicaArgsFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcReplicaArgsFile", version="2.4.0")
    private String olcReplicaArgsFile;

    /**
     * Field for the 'olcReplicaPidFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcReplicaPidFile", version="2.4.0")
    private String olcReplicaPidFile;

    /**
     * Field for the 'olcReplicationInterval' attribute.
     */
    @ConfigurationElement(attributeType = "olcReplicationInterval", version="2.4.0")
    private Integer olcReplicationInterval;

    /**
     * Field for the 'olcReplogFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcReplogFile", version="2.4.0")
    private String olcReplogFile;

    /**
     * Field for the 'olcRequires' attribute.
     */
    @ConfigurationElement(attributeType = "olcRequires", version="2.4.0")
    private List<String> olcRequires = new ArrayList<>();

    /**
     * Field for the 'olcRestrict' attribute.
     */
    @ConfigurationElement(attributeType = "olcRestrict", version="2.4.0")
    private List<String> olcRestrict = new ArrayList<>();

    /**
     * Field for the 'olcRootDN' attribute.
     */
    @ConfigurationElement(attributeType = "olcRootDN", version="2.4.0")
    private Dn olcRootDN;

    /**
     * Field for the 'olcRootPW' attribute.
     */
    @ConfigurationElement(attributeType = "olcRootPW", version="2.4.0")
    private String olcRootPW;

    /**
     * Field for the 'olcSchemaDN' attribute.
     */
    @ConfigurationElement(attributeType = "olcSchemaDN", version="2.4.0")
    private Dn olcSchemaDN;

    /**
     * Field for the 'olcSecurity' attribute.
     */
    @ConfigurationElement(attributeType = "olcSecurity", version="2.4.0")
    private List<String> olcSecurity = new ArrayList<>();

    /**
     * Field for the 'olcSizeLimit' attribute.
     */
    @ConfigurationElement(attributeType = "olcSizeLimit", version="2.4.0")
    private String olcSizeLimit;

    /**
     * Field for the 'olcSubordinate' attribute.
     */
    @ConfigurationElement(attributeType = "olcSubordinate", version="2.4.0")
    private String olcSubordinate;

    /**
     * Field for the 'olcSuffix' attribute.
     */
    @ConfigurationElement(attributeType = "olcSuffix", version="2.4.0")
    private List<Dn> olcSuffix = new ArrayList<>();

    /**
     * Field for the 'olcSyncrepl' attribute.
     */
    @ConfigurationElement(attributeType = "olcSyncrepl", version="2.4.0")
    private List<String> olcSyncrepl = new ArrayList<>();

    /**
     * Field for the 'olcSyncUseSubentry' attribute. (Added in OpenLDAP 2.4.20)
     */
    @ConfigurationElement(attributeType = "olcSyncUseSubentry", version="2.4.20")
    private Boolean olcSyncUseSubentry;

    /**
     * Field for the 'olcTimeLimit' attribute.
     */
    @ConfigurationElement(attributeType = "olcTimeLimit", version="2.4.0")
    private List<String> olcTimeLimit = new ArrayList<>();

    /**
     * Field for the 'olcUpdateDN' attribute.
     */
    @ConfigurationElement(attributeType = "olcUpdateDN", version="2.4.0")
    private Dn olcUpdateDN;

    /**
     * Field for the 'olcUpdateRef' attribute.
     */
    @ConfigurationElement(attributeType = "olcUpdateRef", version="2.4.0")
    private List<String> olcUpdateRef = new ArrayList<>();

    /**
     * The overlays list
     */
    private List<OlcOverlayConfig> overlays = new ArrayList<>();


    // ── addOlcAccess — Lando Adds an Access Control Rule to the Station ───────────
    // Lando adds an access rule to Cloud City's master record — each rule says who
    // can read or write which parts of the LDAP tree. Rules are ordered; slapd checks
    // them top-to-bottom and applies the first match.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more access control rule strings to the {@code olcAccess} list.
     * Each string is a complete ACL rule in OpenLDAP slapd.conf(5) format.
     *
     * <p>For example — Lando adds an ACL rule to the colony records:</p>
     * <pre>
     *   dbConfig.addOlcAccess( "to * by dn.exact=cn=admin,dc=example,dc=com write" );
     * </pre>
     *
     * @param strings  the ACL rule strings to add
     */
    public void addOlcAccess( String... strings )
    {
        for ( String string : strings )
        {
            olcAccess.add( string );
        }
    }


    // ── addOlcExtraAttrs — Lando Adds Extra Attribute Specs to the Record ─────────
    // Lando adds extra attribute specs — additional attributes that slapd should
    // return to clients even if they're not stored in the database, computed on-the-fly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more extra-attributes specification strings to the
     * {@code olcExtraAttrs} list.
     *
     * <p>For example — Lando adds extra attributes:</p>
     * <pre>
     *   dbConfig.addOlcExtraAttrs( "entryDN" );
     * </pre>
     *
     * @param strings  the extra attribute spec strings to add (The olcExtraAttrs to add)
     */
    public void addOlcExtraAttrs( String... strings )
    {
        for ( String string : strings )
        {
            olcExtraAttrs.add( string );
        }
    }


    // ── addOlcLimits — Lando Adds a Resource Limit Rule ──────────────────────────
    // Lando adds a resource limit — something like "this user can only get 500 results
    // per query". Limits prevent any one user from overwhelming the LDAP server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more resource limit specification strings to the
     * {@code olcLimits} list.
     *
     * <p>For example — Lando adds a size limit rule:</p>
     * <pre>
     *   dbConfig.addOlcLimits( "dn.exact=cn=admin,dc=ex,dc=com size=unlimited" );
     * </pre>
     *
     * @param strings  the limit rule strings to add
     */
    public void addOlcLimits( String... strings )
    {
        for ( String string : strings )
        {
            olcLimits.add( string );
        }
    }


    // ── addOlcPlugin — Lando Adds a Plugin Module Spec ────────────────────────────
    // Lando adds a plugin module specification. This is an older extensibility
    // mechanism (now largely superseded by overlays).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more plugin module specification strings to the
     * {@code olcPlugin} list.
     *
     * <p>For example — Lando adds a plugin spec:</p>
     * <pre>
     *   dbConfig.addOlcPlugin( "preoperation /path/to/plugin.so" );
     * </pre>
     *
     * @param strings  the plugin module spec strings to add
     */
    public void addOlcPlugin( String... strings )
    {
        for ( String string : strings )
        {
            olcPlugin.add( string );
        }
    }


    // ── addOlcReplica — Lando Adds a Legacy Replication Target ────────────────────
    // Lando adds a legacy slurpd-style replica spec. This is obsolete — syncrepl is
    // the modern way to replicate — but the field is still here for compatibility.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more legacy replica specification strings to the
     * {@code olcReplica} list.
     *
     * <p>For example — Lando adds a legacy replica spec:</p>
     * <pre>
     *   dbConfig.addOlcReplica( "host=replica.example.com" );
     * </pre>
     *
     * @param strings  the replica spec strings to add
     */
    public void addOlcReplica( String... strings )
    {
        for ( String string : strings )
        {
            olcReplica.add( string );
        }
    }


    // ── addOlcRequires — Lando Adds a Required-Condition Rule ─────────────────────
    // Lando adds a "requires" rule — a condition that must be true before slapd will
    // process operations. For example "authc" requires that the client be authenticated.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more required-condition strings to the {@code olcRequires} list.
     *
     * <p>For example — Lando requires authentication:</p>
     * <pre>
     *   dbConfig.addOlcRequires( "authc" );
     * </pre>
     *
     * @param strings  the required-condition strings to add
     */
    public void addOlcRequires( String... strings )
    {
        for ( String string : strings )
        {
            olcRequires.add( string );
        }
    }


    // ── addOlcRestrict — Lando Adds a Restricted Operation ────────────────────────
    // Lando restricts certain operations on the database — for example, restricting
    // search or bind operations to authenticated users only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more restricted-operation strings to the {@code olcRestrict} list.
     *
     * <p>For example — Lando restricts write operations:</p>
     * <pre>
     *   dbConfig.addOlcRestrict( "write" );
     * </pre>
     *
     * @param strings  the restricted-operation strings to add
     */
    public void addOlcRestrict( String... strings )
    {
        for ( String string : strings )
        {
            olcRestrict.add( string );
        }
    }


    // ── addOlcSecurity — Lando Adds a Security Strength Factor Requirement ────────
    // Lando adds a security strength factor (SSF) requirement — for example requiring
    // TLS on connections before certain operations are allowed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more security strength factor requirement strings to the
     * {@code olcSecurity} list.
     *
     * <p>For example — Lando requires TLS encryption:</p>
     * <pre>
     *   dbConfig.addOlcSecurity( "tls=128" );
     * </pre>
     *
     * @param strings  the security requirement strings to add
     */
    public void addOlcSecurity( String... strings )
    {
        for ( String string : strings )
        {
            olcSecurity.add( string );
        }
    }


    // ── addOlcSuffix — Lando Adds a Naming Context (DN Suffix) ────────────────────
    // Lando adds a suffix DN — the naming context that this database serves.
    // For example, "dc=example,dc=com" means this DB holds all entries under that DN.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more suffix DNs to the {@code olcSuffix} list.
     * Each DN defines a naming context (subtree) that this database serves.
     *
     * <p>For example — Lando adds the naming context for the colony:</p>
     * <pre>
     *   dbConfig.addOlcSuffix( new Dn( "dc=example,dc=com" ) );
     * </pre>
     *
     * @param dns  the suffix DNs to add
     */
    public void addOlcSuffix( Dn... dns )
    {
        for ( Dn dn : dns )
        {
            olcSuffix.add( dn );
        }
    }


    // ── addOlcSyncrepl — Lando Adds a Syncrepl Replication Consumer Config ────────
    // Lando adds a syncrepl consumer spec — this turns the database into a replication
    // consumer that syncs from a master/provider using the LDAP Content Sync protocol.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more syncrepl consumer configuration strings to the
     * {@code olcSyncrepl} list.
     *
     * <p>For example — Lando adds a syncrepl consumer config:</p>
     * <pre>
     *   dbConfig.addOlcSyncrepl( "rid=001 provider=ldap://master.example.com..." );
     * </pre>
     *
     * @param strings  the syncrepl consumer configuration strings to add
     */
    public void addOlcSyncrepl( String... strings )
    {
        for ( String string : strings )
        {
            olcSyncrepl.add( string );
        }
    }


    // ── addOlcTimeLimit — Lando Adds a Time Limit Rule ────────────────────────────
    // Lando sets a time limit on search operations — after this many seconds, slapd
    // stops the search and returns a time-limit-exceeded result.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more time limit specification strings to the
     * {@code olcTimeLimit} list.
     *
     * <p>For example — Lando adds a search time limit:</p>
     * <pre>
     *   dbConfig.addOlcTimeLimit( "size.soft=500 size.hard=unlimited" );
     * </pre>
     *
     * @param strings  the time limit strings to add
     */
    public void addOlcTimeLimit( String... strings )
    {
        for ( String string : strings )
        {
            olcTimeLimit.add( string );
        }
    }


    // ── addOlcUpdateRef — Lando Adds an Update Referral ───────────────────────────
    // Lando adds an update referral — when this database is a read-only slave, writes
    // are redirected to this referral URI (the master server's URL).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more update referral URI strings to the
     * {@code olcUpdateRef} list.
     *
     * <p>For example — Lando adds an update referral to the master:</p>
     * <pre>
     *   dbConfig.addOlcUpdateRef( "ldap://master.example.com" );
     * </pre>
     *
     * @param strings  the update referral URI strings to add
     */
    public void addOlcUpdateRef( String... strings )
    {
        for ( String string : strings )
        {
            olcUpdateRef.add( string );
        }
    }


    // ── clearOlcAccess — Lando Clears All ACL Rules ───────────────────────────────
    // Lando removes all access control rules from the record. Handle with care —
    // without ACL rules, OpenLDAP falls back to its default restrictive behaviour.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all access control rule strings from the {@code olcAccess} list.
     *
     * <p>For example — Lando clears all ACL rules:</p>
     * <pre>
     *   dbConfig.clearOlcAccess();
     * </pre>
     */
    public void clearOlcAccess()
    {
        olcAccess.clear();
    }


    // ── clearOlcExtraAttrs — Lando Clears the Extra Attributes ────────────────────
    // Lando removes all extra attribute specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all extra-attributes specification strings from the
     * {@code olcExtraAttrs} list.
     *
     * <p>For example — Lando clears extra attrs:</p>
     * <pre>
     *   dbConfig.clearOlcExtraAttrs();
     * </pre>
     */
    public void clearOlcExtraAttrs()
    {
        olcExtraAttrs.clear();
    }


    // ── clearOlcLimits — Lando Clears All Resource Limit Rules ────────────────────
    // Lando removes all resource limit rules — after this, the global limits apply.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all resource limit specification strings from the
     * {@code olcLimits} list.
     *
     * <p>For example — Lando clears all resource limits:</p>
     * <pre>
     *   dbConfig.clearOlcLimits();
     * </pre>
     */
    public void clearOlcLimits()
    {
        olcLimits.clear();
    }


    // ── clearOlcPlugin — Lando Removes All Plugin Specs ───────────────────────────
    // Lando removes all plugin module specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all plugin module specification strings from the
     * {@code olcPlugin} list.
     *
     * <p>For example — Lando clears all plugin specs:</p>
     * <pre>
     *   dbConfig.clearOlcPlugin();
     * </pre>
     */
    public void clearOlcPlugin()
    {
        olcPlugin.clear();
    }


    // ── clearOlcReplica — Lando Clears All Legacy Replica Specs ───────────────────
    // Lando removes all legacy slurpd replication targets.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all legacy replica specification strings from the
     * {@code olcReplica} list.
     *
     * <p>For example — Lando clears all replica specs:</p>
     * <pre>
     *   dbConfig.clearOlcReplica();
     * </pre>
     */
    public void clearOlcReplica()
    {
        olcReplica.clear();
    }


    // ── clearOlcRequires — Lando Removes All Required-Condition Rules ──────────────
    // Lando removes all required-condition rules.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all required-condition strings from the {@code olcRequires} list.
     *
     * <p>For example — Lando clears required conditions:</p>
     * <pre>
     *   dbConfig.clearOlcRequires();
     * </pre>
     */
    public void clearOlcRequires()
    {
        olcRequires.clear();
    }


    // ── clearOlcRestrict — Lando Removes All Restricted Operations ────────────────
    // Lando removes all operation restrictions from the database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all restricted-operation strings from the {@code olcRestrict} list.
     *
     * <p>For example — Lando clears all restrictions:</p>
     * <pre>
     *   dbConfig.clearOlcRestrict();
     * </pre>
     */
    public void clearOlcRestrict()
    {
        olcRestrict.clear();
    }


    // ── clearOlcSecurity — Lando Removes All Security Requirements ────────────────
    // Lando removes all security strength factor requirements.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all security strength factor requirement strings from the
     * {@code olcSecurity} list.
     *
     * <p>For example — Lando clears all security requirements:</p>
     * <pre>
     *   dbConfig.clearOlcSecurity();
     * </pre>
     */
    public void clearOlcSecurity()
    {
        olcSecurity.clear();
    }


    // ── clearOlcSuffix — Lando Removes All Naming Contexts ────────────────────────
    // Lando removes all suffix DNs — after this, the database serves nothing until
    // new suffixes are added.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all suffix DNs from the {@code olcSuffix} list.
     *
     * <p>For example — Lando clears all naming contexts:</p>
     * <pre>
     *   dbConfig.clearOlcSuffix();
     * </pre>
     */
    public void clearOlcSuffix()
    {
        olcSuffix.clear();
    }


    // ── clearOlcSyncrepl — Lando Clears All Syncrepl Consumer Configs ─────────────
    // Lando removes all syncrepl consumer configurations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all syncrepl consumer configuration strings from the
     * {@code olcSyncrepl} list.
     *
     * <p>For example — Lando clears all syncrepl configs:</p>
     * <pre>
     *   dbConfig.clearOlcSyncrepl();
     * </pre>
     */
    public void clearOlcSyncrepl()
    {
        olcSyncrepl.clear();
    }


    // ── clearOlcTimeLimit — Lando Clears All Time Limit Rules ─────────────────────
    // Lando removes all time limit specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all time limit specification strings from the
     * {@code olcTimeLimit} list.
     *
     * <p>For example — Lando clears all time limits:</p>
     * <pre>
     *   dbConfig.clearOlcTimeLimit();
     * </pre>
     */
    public void clearOlcTimeLimit()
    {
        olcTimeLimit.clear();
    }


    // ── clearOlcUpdateRef — Lando Clears All Update Referrals ─────────────────────
    // Lando removes all update referrals.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all update referral URI strings from the {@code olcUpdateRef} list.
     *
     * <p>For example — Lando clears all update referrals:</p>
     * <pre>
     *   dbConfig.clearOlcUpdateRef();
     * </pre>
     */
    public void clearOlcUpdateRef()
    {
        olcUpdateRef.clear();
    }


    // ── getOlcAccess — Lando Reads the Access Control Rules ───────────────────────
    // Lando reads the list of ACL rules for this database — these rules control who
    // can read or modify which entries and attributes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the access control rule strings for this database.
     *
     * <p>For example — Lando reads the ACL rules:</p>
     * <pre>
     *   List&lt;String&gt; rules = dbConfig.getOlcAccess();
     * </pre>
     *
     * @return  a copy of the ACL rule list; never null
     */
    public List<String> getOlcAccess()
    {
        return copyListString( olcAccess );
    }


    // ── getOlcAddContentAcl — Lando Checks the Add-Content-ACL Flag ───────────────
    // Lando checks whether slapd uses the content of an Add request as the ACL target.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the {@code olcAddContentAcl} flag is set.
     * When true, slapd applies ACL rules to the content of add operations.
     *
     * <p>For example — Lando reads the add-content ACL flag:</p>
     * <pre>
     *   Boolean b = dbConfig.getOlcAddContentAcl(); // true
     * </pre>
     *
     * @return  the flag, or null if not set
     */
    public Boolean getOlcAddContentAcl()
    {
        return olcAddContentAcl;
    }


    // ── getOlcDatabase — Lando Reads the Database Type/Name ───────────────────────
    // Lando reads the "olcDatabase" attribute — the RDN value that identifies this
    // database in the cn=config DIT. It's both the type identifier and the RDN.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code olcDatabase} value — the database type/name used as the RDN
     * in the cn=config DIT entry.
     *
     * <p>For example — Lando reads the database identifier:</p>
     * <pre>
     *   String db = dbConfig.getOlcDatabase(); // "{1}mdb"
     * </pre>
     *
     * @return  the database identifier string
     */
    public String getOlcDatabase()
    {
        return olcDatabase;
    }


    // ── getOlcDisabled — Lando Checks Whether the Database is Disabled ────────────
    // Lando checks whether the database has been disabled. A disabled database is
    // loaded but doesn't process any requests — useful for maintenance.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the database is disabled.
     * When true, slapd loads the database but doesn't serve any requests for it.
     *
     * <p>For example — Lando checks if the database is active:</p>
     * <pre>
     *   Boolean dis = dbConfig.getOlcDisabled(); // false
     * </pre>
     *
     * @return  true if disabled, false if active, null if not set
     */
    public Boolean getOlcDisabled()
    {
        return olcDisabled;
    }


    // ── getOlcExtraAttrs — Lando Reads the Extra Attributes List ──────────────────
    // Lando reads the extra attribute specs — attributes returned to clients beyond
    // what's stored in the database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the extra-attributes specification strings.
     *
     * <p>For example — Lando reads the extra attrs:</p>
     * <pre>
     *   List&lt;String&gt; extras = dbConfig.getOlcExtraAttrs();
     * </pre>
     *
     * @return  a copy of the extra attrs list; never null
     */
    public List<String> getOlcExtraAttrs()
    {
        return copyListString( olcExtraAttrs );
    }


    // ── getOlcHidden — Lando Checks Whether the Database is Hidden ────────────────
    // Lando checks whether the database is hidden from directory service queries —
    // a hidden database doesn't appear in rootDSE namingContexts.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the database is hidden from the rootDSE namingContexts.
     *
     * <p>For example — Lando checks if the database is hidden:</p>
     * <pre>
     *   Boolean hidden = dbConfig.getOlcHidden(); // false
     * </pre>
     *
     * @return  true if hidden, false if visible, null if not set
     */
    public Boolean getOlcHidden()
    {
        return olcHidden;
    }


    // ── getOlcLastMod — Lando Checks Whether Auto-Modification Timestamps Are On ──
    // Lando checks whether slapd automatically updates modifyTimestamp and
    // modifiersName whenever an entry is changed. Usually you want this on.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether slapd automatically maintains lastmod operational attributes
     * (modifyTimestamp, modifiersName) on writes.
     *
     * <p>For example — Lando reads the lastmod flag:</p>
     * <pre>
     *   Boolean lm = dbConfig.getOlcLastMod(); // true
     * </pre>
     *
     * @return  true if lastmod is enabled, false if not, null if not set
     */
    public Boolean getOlcLastMod()
    {
        return olcLastMod;
    }


    // ── getOlcLimits — Lando Reads the Resource Limit Rules ───────────────────────
    // Lando reads the per-user resource limits — size and time limits that apply to
    // specific users or groups.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the resource limit specification strings.
     *
     * <p>For example — Lando reads the limit rules:</p>
     * <pre>
     *   List&lt;String&gt; limits = dbConfig.getOlcLimits();
     * </pre>
     *
     * @return  a copy of the limits list; never null
     */
    public List<String> getOlcLimits()
    {
        return copyListString( olcLimits );
    }


    // ── getOlcMaxDerefDepth — Lando Reads the Max Alias Dereferencing Depth ───────
    // Lando reads the maximum depth slapd will follow alias chains before giving up
    // and returning a referral loop error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum alias dereferencing depth for this database.
     * slapd stops following aliases after this many hops.
     *
     * <p>For example — Lando reads the max deref depth:</p>
     * <pre>
     *   Integer depth = dbConfig.getOlcMaxDerefDepth(); // 15
     * </pre>
     *
     * @return  the max deref depth, or null if not set
     */
    public Integer getOlcMaxDerefDepth()
    {
        return olcMaxDerefDepth;
    }


    // ── getOlcMirrorMode — Lando Checks Whether Mirror Mode is On ─────────────────
    // Lando checks whether this database is in mirror mode — a configuration where
    // two replicas can both accept writes, syncing with each other via syncrepl.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether mirror mode replication is enabled for this database.
     * When true, this replica can accept writes (it's a peer, not a read-only slave).
     *
     * <p>For example — Lando checks if mirror mode is active:</p>
     * <pre>
     *   Boolean mirror = dbConfig.getOlcMirrorMode(); // false
     * </pre>
     *
     * @return  true if mirror mode is enabled, false if not, null if not set
     */
    public Boolean getOlcMirrorMode()
    {
        return olcMirrorMode;
    }


    // ── getOlcMonitoring — Lando Reads the Monitoring Flag ────────────────────────
    // Lando checks whether the database exposes its statistics in the cn=Monitor tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether monitoring statistics are exposed for this database in cn=Monitor.
     *
     * <p>For example — Lando reads the monitoring flag:</p>
     * <pre>
     *   Boolean mon = dbConfig.getOlcMonitoring(); // true
     * </pre>
     *
     * @return  true if monitoring is enabled, false if not, null if not set
     */
    public Boolean getOlcMonitoring()
    {
        return olcMonitoring;
    }


    // ── getOlcPlugin — Lando Reads the Plugin Module Specs ────────────────────────
    // Lando reads the list of plugin module specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the plugin module specification strings.
     *
     * <p>For example — Lando reads the plugin specs:</p>
     * <pre>
     *   List&lt;String&gt; plugins = dbConfig.getOlcPlugin();
     * </pre>
     *
     * @return  a copy of the plugin list; never null
     */
    public List<String> getOlcPlugin()
    {
        return copyListString( olcPlugin );
    }


    // ── getOlcReadOnly — Lando Reads the Read-Only Flag ───────────────────────────
    // Lando checks whether the database is in read-only mode. A read-only database
    // rejects all write operations with an "unwilling to perform" error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the database is in read-only mode.
     * When true, write operations are rejected.
     *
     * <p>For example — Lando reads the read-only flag:</p>
     * <pre>
     *   Boolean ro = dbConfig.getOlcReadOnly(); // false
     * </pre>
     *
     * @return  true if read-only, false if writable, null if not set
     */
    public Boolean getOlcReadOnly()
    {
        return olcReadOnly;
    }


    // ── getOlcReplica — Lando Reads the Legacy Replica Specs ──────────────────────
    // Lando reads the obsolete slurpd-style replica specs — keep for compatibility.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the legacy replica specification strings.
     *
     * <p>For example — Lando reads the replica specs:</p>
     * <pre>
     *   List&lt;String&gt; replicas = dbConfig.getOlcReplica();
     * </pre>
     *
     * @return  a copy of the replica list; never null
     */
    public List<String> getOlcReplica()
    {
        return copyListString( olcReplica );
    }


    // ── getOlcReplicaArgsFile — Lando Reads the Legacy Replica Args File Path ─────
    // Lando reads the path to the slurpd args file — an obsolete replication artefact.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the path to the legacy slurpd replica args file.
     *
     * <p>For example — Lando reads the replica args file path:</p>
     * <pre>
     *   String path = dbConfig.getOlcReplicaArgsFile();
     * </pre>
     *
     * @return  the replica args file path, or null if not set
     */
    public String getOlcReplicaArgsFile()
    {
        return olcReplicaArgsFile;
    }


    // ── getOlcReplicaPidFile — Lando Reads the Legacy Replica PID File Path ───────
    // Lando reads the path to the slurpd PID file — an obsolete replication artefact.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the path to the legacy slurpd replica PID file.
     *
     * <p>For example — Lando reads the replica PID file path:</p>
     * <pre>
     *   String path = dbConfig.getOlcReplicaPidFile();
     * </pre>
     *
     * @return  the replica PID file path, or null if not set
     */
    public String getOlcReplicaPidFile()
    {
        return olcReplicaPidFile;
    }


    // ── getOlcReplicationInterval — Lando Reads the Legacy Replication Interval ───
    // Lando reads the slurpd polling interval in seconds — an obsolete replication field.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the legacy slurpd replication polling interval in seconds.
     *
     * <p>For example — Lando reads the replication interval:</p>
     * <pre>
     *   Integer interval = dbConfig.getOlcReplicationInterval(); // 30
     * </pre>
     *
     * @return  the replication interval in seconds, or null if not set
     */
    public Integer getOlcReplicationInterval()
    {
        return olcReplicationInterval;
    }


    // ── getOlcReplogFile — Lando Reads the Legacy Replog File Path ────────────────
    // Lando reads the slurpd replication log file path — an obsolete replication artefact.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the path to the legacy slurpd replication log file.
     *
     * <p>For example — Lando reads the replog file path:</p>
     * <pre>
     *   String path = dbConfig.getOlcReplogFile();
     * </pre>
     *
     * @return  the replog file path, or null if not set
     */
    public String getOlcReplogFile()
    {
        return olcReplogFile;
    }


    // ── getOlcRequires — Lando Reads the Required-Condition Rules ─────────────────
    // Lando reads the list of conditions that must be satisfied before slapd processes
    // operations on this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the required-condition strings.
     *
     * <p>For example — Lando reads required conditions:</p>
     * <pre>
     *   List&lt;String&gt; reqs = dbConfig.getOlcRequires();
     * </pre>
     *
     * @return  a copy of the requires list; never null
     */
    public List<String> getOlcRequires()
    {
        return copyListString( olcRequires );
    }


    // ── getOlcRestrict — Lando Reads the Restricted Operations ───────────────────
    // Lando reads the list of restricted operations for this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the restricted-operation strings.
     *
     * <p>For example — Lando reads restricted operations:</p>
     * <pre>
     *   List&lt;String&gt; restrictions = dbConfig.getOlcRestrict();
     * </pre>
     *
     * @return  a copy of the restrict list; never null
     */
    public List<String> getOlcRestrict()
    {
        return copyListString( olcRestrict );
    }


    // ── getOlcRootDN — Lando Reads the Database Administrator DN ──────────────────
    // Lando reads the DN of the database's root/admin user — the account that bypasses
    // ACL checks and has unlimited access to this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the root DN (the database administrator's distinguished name) for this database.
     * The root DN bypasses ACL checks and always has full access.
     *
     * <p>For example — Lando reads the admin DN:</p>
     * <pre>
     *   Dn rootDn = dbConfig.getOlcRootDN(); // cn=admin,dc=example,dc=com
     * </pre>
     *
     * @return  the root DN, or null if not set
     */
    public Dn getOlcRootDN()
    {
        return olcRootDN;
    }


    // ── getOlcRootPW — Lando Reads the Administrator's Password Hash ──────────────
    // Lando reads the hashed password for the root DN. This is typically an SSHA or
    // SHA-256 hash — never the plaintext password (hopefully).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the root DN's password (should be a hash like "{SSHA}..." not plaintext).
     *
     * <p>For example — Lando reads the root password hash:</p>
     * <pre>
     *   String pw = dbConfig.getOlcRootPW(); // "{SSHA}..."
     * </pre>
     *
     * @return  the root password string, or null if not set
     */
    public String getOlcRootPW()
    {
        return olcRootPW;
    }


    // ── getOlcSchemaDN — Lando Reads the Schema Publishing DN ────────────────────
    // Lando reads the DN where the directory's schema is published. Normally this
    // is "cn=Subschema" and most clients know to look there for schema definitions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN where this database publishes its schema.
     *
     * <p>For example — Lando reads the schema DN:</p>
     * <pre>
     *   Dn schemaDn = dbConfig.getOlcSchemaDN(); // cn=Subschema
     * </pre>
     *
     * @return  the schema DN, or null if not set
     */
    public Dn getOlcSchemaDN()
    {
        return olcSchemaDN;
    }


    // ── getOlcSecurity — Lando Reads the Security Strength Factor Requirements ────
    // Lando reads the list of security requirements for this database — minimum
    // encryption strength factors required for operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the security strength factor requirement strings.
     *
     * <p>For example — Lando reads security requirements:</p>
     * <pre>
     *   List&lt;String&gt; secRules = dbConfig.getOlcSecurity();
     * </pre>
     *
     * @return  a copy of the security list; never null
     */
    public List<String> getOlcSecurity()
    {
        return copyListString( olcSecurity );
    }


    // ── getOlcSizeLimit — Lando Reads the Maximum Search Result Size ───────────────
    // Lando reads the maximum number of entries slapd returns per search. Clients
    // asking for more than this get a "size limit exceeded" response.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search result size limit for this database.
     * Can be a plain number or a policy string like "size.soft=100 size.hard=200".
     *
     * <p>For example — Lando reads the size limit:</p>
     * <pre>
     *   String limit = dbConfig.getOlcSizeLimit(); // "500"
     * </pre>
     *
     * @return  the size limit string, or null if not set
     */
    public String getOlcSizeLimit()
    {
        return olcSizeLimit;
    }


    // ── getOlcSubordinate — Lando Reads the Subordinate Marker ────────────────────
    // Lando reads whether this database is a subordinate — placed under another database
    // in the hierarchy, sharing a common suffix but at a lower position.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the subordinate marker value, indicating this database is a subordinate
     * of another in the directory hierarchy.
     *
     * <p>For example — Lando reads the subordinate marker:</p>
     * <pre>
     *   String sub = dbConfig.getOlcSubordinate(); // "TRUE"
     * </pre>
     *
     * @return  the subordinate string value, or null if not set
     */
    public String getOlcSubordinate()
    {
        return olcSubordinate;
    }


    // ── getOlcSuffix — Lando Reads the Database Naming Contexts ───────────────────
    // Lando reads the list of suffix DNs this database is responsible for — these
    // are the naming contexts (subtree roots) that this database serves.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of suffix DNs (naming contexts) this database serves.
     * Note: this returns the live list, not a defensive copy.
     *
     * <p>For example — Lando reads the naming contexts:</p>
     * <pre>
     *   List&lt;Dn&gt; suffixes = dbConfig.getOlcSuffix(); // [dc=example,dc=com]
     * </pre>
     *
     * @return  the live list of suffix DNs; never null
     */
    public List<Dn> getOlcSuffix()
    {
        return olcSuffix;
    }


    // ── getOlcSyncrepl — Lando Reads the Syncrepl Consumer Configs ────────────────
    // Lando reads the syncrepl consumer specifications — each string tells slapd how
    // to connect to a provider and sync data from it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the syncrepl consumer configuration strings.
     *
     * <p>For example — Lando reads the syncrepl configs:</p>
     * <pre>
     *   List&lt;String&gt; syncs = dbConfig.getOlcSyncrepl();
     * </pre>
     *
     * @return  a copy of the syncrepl list; never null
     */
    public List<String> getOlcSyncrepl()
    {
        return copyListString( olcSyncrepl );
    }


    // ── getOlcSyncUseSubentry — Lando Reads the Sync Subentry Flag ────────────────
    // Lando reads whether syncrepl stores its sync cookie in a subentry rather than
    // in the root entry of the consumer. The subentry approach is recommended for
    // new deployments.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether syncrepl uses a subentry to store its sync cookie.
     *
     * <p>For example — Lando reads the sync subentry flag:</p>
     * <pre>
     *   Boolean sub = dbConfig.getOlcSyncUseSubentry(); // true
     * </pre>
     *
     * @return  true if subentry is used, false if not, null if not set
     */
    public Boolean getOlcSyncUseSubentry()
    {
        return olcSyncUseSubentry;
    }


    // ── getOlcTimeLimit — Lando Reads the Search Time Limit Rules ─────────────────
    // Lando reads the time limit rules — after how many seconds slapd stops a search
    // and returns a time-limit-exceeded result.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the time limit specification strings.
     *
     * <p>For example — Lando reads the time limit rules:</p>
     * <pre>
     *   List&lt;String&gt; timeLimits = dbConfig.getOlcTimeLimit();
     * </pre>
     *
     * @return  a copy of the timeLimit list; never null
     */
    public List<String> getOlcTimeLimit()
    {
        return copyListString( olcTimeLimit );
    }


    // ── getOlcUpdateDN — Lando Reads the Update DN ────────────────────────────────
    // Lando reads the DN authorised to apply updates on a slave replica. Writes from
    // the master arrive under this DN so they bypass normal ACL restrictions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN authorised to apply replicated updates on a slave database.
     *
     * <p>For example — Lando reads the update DN:</p>
     * <pre>
     *   Dn updateDn = dbConfig.getOlcUpdateDN();
     * </pre>
     *
     * @return  the update DN, or null if not set
     */
    public Dn getOlcUpdateDN()
    {
        return olcUpdateDN;
    }


    // ── getOlcUpdateRef — Lando Reads the Update Referral URIs ────────────────────
    // Lando reads the update referral URIs — when write operations arrive at a
    // read-only slave, slapd redirects them to these master server URIs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the update referral URI strings.
     *
     * <p>For example — Lando reads the update referrals:</p>
     * <pre>
     *   List&lt;String&gt; refs = dbConfig.getOlcUpdateRef();
     * </pre>
     *
     * @return  a copy of the updateRef list; never null
     */
    public List<String> getOlcUpdateRef()
    {
        return copyListString( olcUpdateRef );
    }


    // ── getOverlays — Lando Reads the Overlay Module List ─────────────────────────
    // Lando reads the list of overlay modules stacked on top of this database.
    // Overlays are like plugins — ppolicy, memberOf, accesslog, etc.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live list of {@link OlcOverlayConfig} objects attached to this database.
     * Overlays augment database behaviour (e.g., ppolicy enforces password policy,
     * memberOf maintains reverse group membership).
     *
     * <p>For example — Lando reads the overlay modules:</p>
     * <pre>
     *   List&lt;OlcOverlayConfig&gt; overlays = dbConfig.getOverlays();
     * </pre>
     *
     * @return  the live overlay list; never null
     */
    public List<OlcOverlayConfig> getOverlays()
    {
        return overlays;
    }


    // ── setOverlays — Lando Replaces the Entire Overlay Stack ─────────────────────
    // Lando replaces the entire list of overlays attached to this database. This
    // replaces whatever was there before with a new set of overlay configurations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the overlay list for this database with the given list.
     *
     * <p>For example — Lando replaces the overlay stack:</p>
     * <pre>
     *   dbConfig.setOverlays( Arrays.asList( ppolicyConfig, memberOfConfig ) );
     * </pre>
     *
     * @param overlays  the new overlay list
     */
    public void setOverlays( List<OlcOverlayConfig> overlays )
    {
        this.overlays = overlays;
    }


    // ── clearOverlays — Lando Removes All Overlays from the Database ──────────────
    // Lando removes all overlays from the database — after this, no augmentation
    // modules are stacked on top of the database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all overlay configurations from this database.
     *
     * <p>For example — Lando removes all overlays:</p>
     * <pre>
     *   dbConfig.clearOverlays();
     * </pre>
     */
    public void clearOverlays()
    {
        overlays.clear();
    }


    // ── addOverlay — Lando Attaches an Overlay Module to the Database ─────────────
    // Lando plugs in an overlay module — something like ppolicy, memberOf, or
    // accesslog — onto this database. Overlays get stacked in order and are processed
    // in sequence for every LDAP operation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an overlay configuration object to this database's overlay stack.
     * Returns {@code true} if the overlay was added (delegates to List.add).
     *
     * <p>For example — Lando attaches an overlay to the database:</p>
     * <pre>
     *   boolean added = dbConfig.addOverlay( ppolicyConfig ); // true
     * </pre>
     *
     * @param o  the overlay configuration to add
     * @return   {@code true} if the overlay was added to the list
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean addOverlay( OlcOverlayConfig o )
    {
        return overlays.add( o );
    }


    // ── removeOverlay — Lando Detaches an Overlay Module from the Database ────────
    // Lando detaches a specific overlay module — removes it from the stack. The
    // overlay is no longer applied to LDAP operations on this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific overlay configuration from this database's overlay stack.
     * Returns {@code true} if it was present and removed.
     *
     * <p>For example — Lando removes an overlay from the database:</p>
     * <pre>
     *   boolean removed = dbConfig.removeOverlay( ppolicyConfig ); // true
     * </pre>
     *
     * @param o  the overlay configuration to remove
     * @return   {@code true} if the overlay was found and removed
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean removeOverlay( OlcOverlayConfig o )
    {
        return overlays.remove( o );
    }


    // ── setOlcAccess — Lando Replaces All ACL Rules ───────────────────────────────
    // Lando replaces the entire set of access control rules for this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the access control rule list for this database.
     *
     * <p>For example — Lando replaces ACL rules:</p>
     * <pre>
     *   dbConfig.setOlcAccess( Arrays.asList( "to * by * read" ) );
     * </pre>
     *
     * @param olcAccess  the new ACL rule list
     */
    public void setOlcAccess( List<String> olcAccess )
    {
        this.olcAccess = copyListString( olcAccess );
    }


    // ── setOlcAddContentAcl — Lando Sets the Add-Content-ACL Flag ────────────────
    // Lando sets whether slapd uses add-request content as the ACL target.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether add-request content is used as the ACL target.
     *
     * <p>For example — Lando sets the add-content ACL flag:</p>
     * <pre>
     *   dbConfig.setOlcAddContentAcl( true );
     * </pre>
     *
     * @param olcAddContentAcl  the flag to set
     */
    public void setOlcAddContentAcl( Boolean olcAddContentAcl )
    {
        this.olcAddContentAcl = olcAddContentAcl;
    }


    // ── setOlcDatabase — Lando Sets the Database Type/Name Identifier ────────────
    // Lando sets the olcDatabase value — the type/name identifier used as the RDN
    // in the cn=config DIT.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code olcDatabase} value for this database configuration.
     *
     * <p>For example — Lando sets the database identifier:</p>
     * <pre>
     *   dbConfig.setOlcDatabase( "{1}mdb" );
     * </pre>
     *
     * @param olcDatabase  the database identifier string
     */
    public void setOlcDatabase( String olcDatabase )
    {
        this.olcDatabase = olcDatabase;
    }


    // ── setOlcDisabled — Lando Disables or Enables the Database ───────────────────
    // Lando disables or re-enables the database. A disabled database is loaded but
    // does not process any LDAP operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether the database is disabled.
     *
     * <p>For example — Lando disables the database for maintenance:</p>
     * <pre>
     *   dbConfig.setOlcDisabled( true );
     * </pre>
     *
     * @param olcDisabled  true to disable; false to enable
     */
    public void setOlcDisabled( Boolean olcDisabled )
    {
        this.olcDisabled = olcDisabled;
    }


    // ── setOlcExtraAttrs — Lando Replaces the Extra Attributes List ───────────────
    // Lando replaces the list of extra attribute specs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the extra-attributes specification list.
     *
     * <p>For example — Lando sets the extra attrs:</p>
     * <pre>
     *   dbConfig.setOlcExtraAttrs( Arrays.asList( "entryDN" ) );
     * </pre>
     *
     * @param olcExtraAttrs  the new extra attributes list
     */
    public void setOlcExtraAttrs( List<String> olcExtraAttrs )
    {
        this.olcExtraAttrs = copyListString( olcExtraAttrs );
    }


    // ── setOlcHidden — Lando Sets the Hidden Flag ─────────────────────────────────
    // Lando hides or unhides the database from rootDSE namingContexts.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether the database is hidden from the rootDSE namingContexts.
     *
     * <p>For example — Lando hides the database:</p>
     * <pre>
     *   dbConfig.setOlcHidden( true );
     * </pre>
     *
     * @param olcHidden  true to hide; false to expose
     */
    public void setOlcHidden( Boolean olcHidden )
    {
        this.olcHidden = olcHidden;
    }


    // ── setOlcLastMod — Lando Toggles Automatic Modification Timestamps ───────────
    // Lando toggles whether slapd automatically maintains modifyTimestamp and
    // modifiersName on every write.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether slapd automatically maintains lastmod operational attributes.
     *
     * <p>For example — Lando enables lastmod:</p>
     * <pre>
     *   dbConfig.setOlcLastMod( true );
     * </pre>
     *
     * @param olcLastMod  true to enable lastmod; false to disable
     */
    public void setOlcLastMod( Boolean olcLastMod )
    {
        this.olcLastMod = olcLastMod;
    }


    // ── setOlcLimits — Lando Replaces All Resource Limit Rules ────────────────────
    // Lando replaces the entire set of resource limit specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the resource limit specification list.
     *
     * <p>For example — Lando replaces all resource limits:</p>
     * <pre>
     *   dbConfig.setOlcLimits( Arrays.asList( "dn.exact=cn=admin ... unlimited" ) );
     * </pre>
     *
     * @param olcLimits  the new limits list
     */
    public void setOlcLimits( List<String> olcLimits )
    {
        this.olcLimits = copyListString( olcLimits );
    }


    // ── setOlcMaxDerefDepth — Lando Sets the Max Alias Dereferencing Depth ────────
    // Lando sets the maximum depth slapd follows alias chains before giving up.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the maximum alias dereferencing depth for this database.
     *
     * <p>For example — Lando sets max deref depth:</p>
     * <pre>
     *   dbConfig.setOlcMaxDerefDepth( 15 );
     * </pre>
     *
     * @param olcMaxDerefDepth  the max deref depth to set
     */
    public void setOlcMaxDerefDepth( Integer olcMaxDerefDepth )
    {
        this.olcMaxDerefDepth = olcMaxDerefDepth;
    }


    // ── setOlcMirrorMode — Lando Sets the Mirror Mode Flag ────────────────────────
    // Lando enables or disables mirror mode replication.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether mirror mode replication is enabled for this database.
     *
     * <p>For example — Lando enables mirror mode:</p>
     * <pre>
     *   dbConfig.setOlcMirrorMode( true );
     * </pre>
     *
     * @param olcMirrorMode  true to enable mirror mode; false to disable
     */
    public void setOlcMirrorMode( Boolean olcMirrorMode )
    {
        this.olcMirrorMode = olcMirrorMode;
    }


    // ── setOlcMonitoring — Lando Toggles the Monitoring Flag ──────────────────────
    // Lando enables or disables statistics monitoring for this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether monitoring statistics are exposed for this database.
     *
     * <p>For example — Lando enables monitoring:</p>
     * <pre>
     *   dbConfig.setOlcMonitoring( true );
     * </pre>
     *
     * @param olcMonitoring  true to enable monitoring; false to disable
     */
    public void setOlcMonitoring( Boolean olcMonitoring )
    {
        this.olcMonitoring = olcMonitoring;
    }


    // ── setOlcPlugin — Lando Replaces the Plugin Module Specs ─────────────────────
    // Lando replaces the entire list of plugin module specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the plugin module specification list.
     *
     * <p>For example — Lando sets the plugin specs:</p>
     * <pre>
     *   dbConfig.setOlcPlugin( Arrays.asList( "preoperation /path/to/plugin.so" ) );
     * </pre>
     *
     * @param olcPlugin  the new plugin spec list
     */
    public void setOlcPlugin( List<String> olcPlugin )
    {
        this.olcPlugin = copyListString( olcPlugin );
    }


    // ── setOlcReadOnly — Lando Sets the Read-Only Flag ────────────────────────────
    // Lando puts the database in read-only mode — or takes it out.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether the database is in read-only mode.
     *
     * <p>For example — Lando makes the database read-only:</p>
     * <pre>
     *   dbConfig.setOlcReadOnly( true );
     * </pre>
     *
     * @param olcReadOnly  true for read-only; false for writable
     */
    public void setOlcReadOnly( Boolean olcReadOnly )
    {
        this.olcReadOnly = olcReadOnly;
    }


    // ── setOlcReplica — Lando Replaces the Legacy Replica Specs ───────────────────
    // Lando replaces the full set of legacy slurpd-style replica specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the legacy replica specification list.
     *
     * <p>For example — Lando replaces replica specs:</p>
     * <pre>
     *   dbConfig.setOlcReplica( Arrays.asList( "host=replica.example.com" ) );
     * </pre>
     *
     * @param olcReplica  the new replica spec list
     */
    public void setOlcReplica( List<String> olcReplica )
    {
        this.olcReplica = copyListString( olcReplica );
    }


    // ── setOlcReplicaArgsFile — Lando Sets the Legacy Replica Args File Path ──────
    // Lando sets the path to the slurpd args file.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the path to the legacy slurpd replica args file.
     *
     * <p>For example — Lando sets the replica args file:</p>
     * <pre>
     *   dbConfig.setOlcReplicaArgsFile( "/var/run/slapd/replica.args" );
     * </pre>
     *
     * @param olcReplicaArgsFile  the path to the args file
     */
    public void setOlcReplicaArgsFile( String olcReplicaArgsFile )
    {
        this.olcReplicaArgsFile = olcReplicaArgsFile;
    }


    // ── setOlcReplicaPidFile — Lando Sets the Legacy Replica PID File Path ────────
    // Lando sets the path to the slurpd PID file.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the path to the legacy slurpd replica PID file.
     *
     * <p>For example — Lando sets the replica PID file:</p>
     * <pre>
     *   dbConfig.setOlcReplicaPidFile( "/var/run/slapd/slurpd.pid" );
     * </pre>
     *
     * @param olcReplicaPidFile  the path to the PID file
     */
    public void setOlcReplicaPidFile( String olcReplicaPidFile )
    {
        this.olcReplicaPidFile = olcReplicaPidFile;
    }


    // ── setOlcReplicationInterval — Lando Sets the Legacy Replication Interval ────
    // Lando sets the slurpd polling interval in seconds.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the legacy slurpd replication polling interval in seconds.
     *
     * <p>For example — Lando sets the replication interval:</p>
     * <pre>
     *   dbConfig.setOlcReplicationInterval( 30 );
     * </pre>
     *
     * @param olcReplicationInterval  the polling interval in seconds
     */
    public void setOlcReplicationInterval( Integer olcReplicationInterval )
    {
        this.olcReplicationInterval = olcReplicationInterval;
    }


    // ── setOlcReplogFile — Lando Sets the Legacy Replog File Path ─────────────────
    // Lando sets the slurpd replication log file path.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the path to the legacy slurpd replication log file.
     *
     * <p>For example — Lando sets the replog file path:</p>
     * <pre>
     *   dbConfig.setOlcReplogFile( "/var/lib/ldap/replog" );
     * </pre>
     *
     * @param olcReplogFile  the replog file path to set
     */
    public void setOlcReplogFile( String olcReplogFile )
    {
        this.olcReplogFile = olcReplogFile;
    }


    // ── setOlcRequires — Lando Replaces the Required-Condition Rules ──────────────
    // Lando replaces the list of conditions required before slapd processes operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the required-condition specification list.
     *
     * <p>For example — Lando sets required conditions:</p>
     * <pre>
     *   dbConfig.setOlcRequires( Arrays.asList( "authc" ) );
     * </pre>
     *
     * @param olcRequires  the new requires list
     */
    public void setOlcRequires( List<String> olcRequires )
    {
        this.olcRequires = copyListString( olcRequires );
    }


    // ── setOlcRestrict — Lando Replaces the Restricted Operations ─────────────────
    // Lando replaces the list of restricted operations for this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the restricted-operation specification list.
     *
     * <p>For example — Lando restricts write operations:</p>
     * <pre>
     *   dbConfig.setOlcRestrict( Arrays.asList( "write" ) );
     * </pre>
     *
     * @param olcRestrict  the new restrict list
     */
    public void setOlcRestrict( List<String> olcRestrict )
    {
        this.olcRestrict = copyListString( olcRestrict );
    }


    // ── setOlcRootDN — Lando Sets the Database Administrator DN ───────────────────
    // Lando sets the root DN — the all-powerful administrator account for this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the root DN (database administrator's distinguished name) for this database.
     *
     * <p>For example — Lando sets the admin DN:</p>
     * <pre>
     *   dbConfig.setOlcRootDN( new Dn( "cn=admin,dc=example,dc=com" ) );
     * </pre>
     *
     * @param olcRootDN  the root DN to set
     */
    public void setOlcRootDN( Dn olcRootDN )
    {
        this.olcRootDN = olcRootDN;
    }


    // ── setOlcRootPW — Lando Sets the Administrator's Password Hash ───────────────
    // Lando sets the hashed password for the root DN.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the root DN password (should be a hash like "{SSHA}...", not plaintext).
     *
     * <p>For example — Lando sets the root password hash:</p>
     * <pre>
     *   dbConfig.setOlcRootPW( "{SSHA}hashed-value" );
     * </pre>
     *
     * @param olcRootPW  the root password string to set
     */
    public void setOlcRootPW( String olcRootPW )
    {
        this.olcRootPW = olcRootPW;
    }


    // ── setOlcSchemaDN — Lando Sets the Schema Publishing DN ──────────────────────
    // Lando sets the DN where the directory's schema is published.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN where this database publishes its schema.
     *
     * <p>For example — Lando sets the schema DN:</p>
     * <pre>
     *   dbConfig.setOlcSchemaDN( new Dn( "cn=Subschema" ) );
     * </pre>
     *
     * @param olcSchemaDN  the schema DN to set
     */
    public void setOlcSchemaDN( Dn olcSchemaDN )
    {
        this.olcSchemaDN = olcSchemaDN;
    }


    // ── setOlcSecurity — Lando Replaces the Security Strength Factor Requirements ─
    // Lando replaces the list of security strength factor requirements for this database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the security strength factor requirement list.
     *
     * <p>For example — Lando sets security requirements:</p>
     * <pre>
     *   dbConfig.setOlcSecurity( Arrays.asList( "tls=128" ) );
     * </pre>
     *
     * @param olcSecurity  the new security requirements list
     */
    public void setOlcSecurity( List<String> olcSecurity )
    {
        this.olcSecurity = copyListString( olcSecurity );
    }


    // ── setOlcSizeLimit — Lando Sets the Maximum Search Result Size ───────────────
    // Lando sets the maximum number of entries slapd returns per search.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the search result size limit for this database.
     *
     * <p>For example — Lando sets the size limit:</p>
     * <pre>
     *   dbConfig.setOlcSizeLimit( "500" );
     * </pre>
     *
     * @param olcSizeLimit  the size limit string to set
     */
    public void setOlcSizeLimit( String olcSizeLimit )
    {
        this.olcSizeLimit = olcSizeLimit;
    }


    // ── setOlcSubordinate — Lando Sets the Subordinate Marker ─────────────────────
    // Lando marks this database as subordinate to another database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the subordinate marker for this database.
     *
     * <p>For example — Lando marks the database as subordinate:</p>
     * <pre>
     *   dbConfig.setOlcSubordinate( "TRUE" );
     * </pre>
     *
     * @param olcSubordinate  the subordinate marker value to set
     */
    public void setOlcSubordinate( String olcSubordinate )
    {
        this.olcSubordinate = olcSubordinate;
    }


    // ── setOlcSuffix — Lando Replaces the Database Naming Contexts ────────────────
    // Lando replaces the list of suffix DNs that define which subtrees this database
    // is responsible for.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the suffix DN list for this database.
     *
     * <p>For example — Lando sets the naming contexts:</p>
     * <pre>
     *   dbConfig.setOlcSuffix( Arrays.asList( new Dn( "dc=example,dc=com" ) ) );
     * </pre>
     *
     * @param olcSuffix  the new suffix DN list
     */
    public void setOlcSuffix( List<Dn> olcSuffix )
    {
        this.olcSuffix = olcSuffix;
    }


    // ── setOlcSyncrepl — Lando Replaces the Syncrepl Consumer Configs ─────────────
    // Lando replaces the syncrepl consumer configuration list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the syncrepl consumer configuration list.
     *
     * <p>For example — Lando sets syncrepl configs:</p>
     * <pre>
     *   dbConfig.setOlcSyncrepl( Arrays.asList( "rid=001 provider=ldap://..." ) );
     * </pre>
     *
     * @param olcSyncrepl  the new syncrepl config list
     */
    public void setOlcSyncrepl( List<String> olcSyncrepl )
    {
        this.olcSyncrepl = copyListString( olcSyncrepl );
    }


    // ── setOlcSyncUseSubentry — Lando Sets the Sync Subentry Flag ─────────────────
    // Lando sets whether syncrepl stores its sync cookie in a subentry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether syncrepl uses a subentry to store its sync cookie.
     *
     * <p>For example — Lando enables sync subentry:</p>
     * <pre>
     *   dbConfig.setOlcSyncUseSubentry( true );
     * </pre>
     *
     * @param olcSyncUseSubentry  true to use a subentry; false to use the root entry
     */
    public void setOlcSyncUseSubentry( Boolean olcSyncUseSubentry )
    {
        this.olcSyncUseSubentry = olcSyncUseSubentry;
    }


    // ── setOlcTimeLimit — Lando Replaces the Time Limit Rules ─────────────────────
    // Lando replaces the list of search time limit specifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the time limit specification list.
     *
     * <p>For example — Lando sets time limits:</p>
     * <pre>
     *   dbConfig.setOlcTimeLimit( Arrays.asList( "3600" ) );
     * </pre>
     *
     * @param olcTimeLimit  the new time limit list
     */
    public void setOlcTimeLimit( List<String> olcTimeLimit )
    {
        this.olcTimeLimit = copyListString( olcTimeLimit );
    }


    // ── setOlcUpdateDN — Lando Sets the Update DN ─────────────────────────────────
    // Lando sets the DN authorised to apply replicated updates on this slave database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN authorised to apply replicated updates on this slave database.
     *
     * <p>For example — Lando sets the update DN:</p>
     * <pre>
     *   dbConfig.setOlcUpdateDN( new Dn( "cn=replicator,dc=example,dc=com" ) );
     * </pre>
     *
     * @param olcUpdateDN  the update DN to set
     */
    public void setOlcUpdateDN( Dn olcUpdateDN )
    {
        this.olcUpdateDN = olcUpdateDN;
    }


    // ── setOlcUpdateRef — Lando Replaces the Update Referral URIs ─────────────────
    // Lando replaces the update referral list — the URIs to redirect writes to on
    // a read-only slave database.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the update referral URI list.
     *
     * <p>For example — Lando sets the update referral:</p>
     * <pre>
     *   dbConfig.setOlcUpdateRef( Arrays.asList( "ldap://master.example.com" ) );
     * </pre>
     *
     * @param olcUpdateRef  the new update referral URI list
     */
    public void setOlcUpdateRef( List<String> olcUpdateRef )
    {
        this.olcUpdateRef = copyListString( olcUpdateRef );
    }


    // ── getOlcDatabaseType — Lando Returns the Default Database Type ───────────────
    // Lando returns the generic default type — this base class returns "default".
    // Subclasses override this to return their specific type (mdb, bdb, ldap, etc.).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string.
     * The base implementation returns {@code "default"}.
     * Subclasses override this to return their specific type (e.g., "mdb", "bdb", "ldap").
     *
     * <p>For example — Lando reads the database type:</p>
     * <pre>
     *   dbConfig.getOlcDatabaseType(); // "default"
     *   mdbConfig.getOlcDatabaseType(); // "mdb"
     * </pre>
     *
     * @return  the database type string; "default" in this base class
     */
    public String getOlcDatabaseType()
    {
        return "default";
    }
}
