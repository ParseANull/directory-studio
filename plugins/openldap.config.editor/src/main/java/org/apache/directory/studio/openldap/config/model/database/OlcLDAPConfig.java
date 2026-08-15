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
import org.apache.directory.studio.openldap.common.ui.model.DatabaseTypeEnum;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;


// ── CLASS: OlcLDAPConfig — Cassian Andor Relaying Intelligence Through the Empire
// Cassian Andor is a spy and relay master — he passes information through multiple
// intermediaries, assumes different identities, and carefully controls who knows what.
// The LDAP backend (back-ldap) is exactly that: it relays every LDAP operation from
// slapd through to a remote LDAP server. Cassian's identity assertion tricks map to
// olcDbIDAssert*, his careful connection management maps to olcDbConnectionPoolMax
// and olcDbConnTtl, and his need-to-know security maps to olcDbACLAuthcDn.
// This is the biggest, most configurable backend config class in the model — it
// has 28+ fields covering proxy authentication, connection pooling, TLS, timeouts,
// referral handling, and protocol version negotiation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcLDAPConfig} object class, representing the
 * configuration of the OpenLDAP LDAP proxy backend (back-ldap).
 * The LDAP backend forwards LDAP operations to one or more remote LDAP servers
 * specified via {@code olcDbURI}. It supports connection pooling, TLS, identity
 * assertion (running operations under a different DN at the remote server),
 * quarantine logic for unreachable servers, and more.
 * Think of this as Cassian Andor's intelligence relay station — every request
 * gets proxied through to a remote LDAP server with configurable authentication
 * and connection management.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcLDAPConfig extends OlcDatabaseConfig
{
    /**
     * Field for the 'olcDbACLAuthcDn' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbACLAuthcDn", version="2.4.0")
    private Dn olcDbACLAuthcDn;

    /**
     * Field for the 'olcDbACLBind' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbACLBind", version="2.4.0")
    private String olcDbACLBind;

    /**
     * Field for the 'olcDbACLPasswd' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbACLPasswd", version="2.4.0")
    private String olcDbACLPasswd;

    /**
     * Field for the 'olcDbCancel' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbCancel", version="2.4.0")
    private String olcDbCancel;

    /**
     * Field for the 'olcDbChaseReferrals' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbChaseReferrals", version="2.4.0")
    private Boolean olcDbChaseReferrals;

    /**
     * Field for the 'olcDbConnectionPoolMax' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbConnectionPoolMax", version="2.4.0")
    private Integer olcDbConnectionPoolMax;

    /**
     * Field for the 'olcDbConnTtl' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbConnTtl", version="2.4.0")
    private String olcDbConnTtl;

    /**
     * Field for the 'olcDbIDAssertAuthcDn' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIDAssertAuthcDn", version="2.4.0")
    private Dn olcDbIDAssertAuthcDn;

    /**
     * Field for the 'olcDbIDAssertAuthzFrom' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIDAssertAuthzFrom", version="2.4.0")
    private List<String> olcDbIDAssertAuthzFrom = new ArrayList<>();

    /**
     * Field for the 'olcDbIDAssertBind' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIDAssertBind", version="2.4.0")
    private String olcDbIDAssertBind;

    /**
     * Field for the 'olcDbIDAssertMode' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIDAssertMode", version="2.4.0")
    private String olcDbIDAssertMode;

    /**
     * Field for the 'olcDbIDAssertPassThru' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIDAssertPassThru", version="2.4.22")
    private List<String> olcDbIDAssertPassThru = new ArrayList<>();

    /**
     * Field for the 'olcDbIDAssertPasswd' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIDAssertPasswd", version="2.4.0")
    private String olcDbIDAssertPasswd;

    /**
     * Field for the 'olcDbIdleTimeout' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbIdleTimeout", version="2.4.0")
    private String olcDbIdleTimeout;

    /**
     * Field for the 'olcDbKeepalive' attribute. (Added in OpenLDAP 2.4.34)
     */
    @ConfigurationElement(attributeType = "olcDbKeepalive", version="2.4.34")
    private String olcDbKeepalive;

    /**
     * Field for the 'olcDbNetworkTimeout' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbNetworkTimeout", version="2.4.0")
    private String olcDbNetworkTimeout;

    /**
     * Field for the 'olcDbNoRefs' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbNoRefs", version="2.4.12")
    private Boolean olcDbNoRefs;

    /**
     * Field for the 'olcDbNoUndefFilter' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbNoUndefFilter", version="2.4.12")
    private Boolean olcDbNoUndefFilter;

    /**
     * Field for the 'olcDbOnErr' attribute. (Added in OpenLDAP 2.4.34)
     */
    @ConfigurationElement(attributeType = "olcDbOnErr", version="2.4.34")
    private String olcDbOnErr;

    /**
     * Field for the 'olcDbProtocolVersion' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbProtocolVersion", version="2.4.0")
    private Integer olcDbProtocolVersion;

    /**
     * Field for the 'olcDbProxyWhoAmI' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbProxyWhoAmI", version="2.4.0")
    private Boolean olcDbProxyWhoAmI;

    /**
     * Field for the 'olcDbQuarantine' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbQuarantine", version="2.4.0")
    private String olcDbQuarantine;

    /**
     * Field for the 'olcDbRebindAsUser' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbRebindAsUser", version="2.4.0")
    private Boolean olcDbRebindAsUser;

    /**
     * Field for the 'olcDbSessionTrackingRequest' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbSessionTrackingRequest", version="2.4.0")
    private Boolean olcDbSessionTrackingRequest;

    /**
     * Field for the 'olcDbSingleConn' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbSingleConn", version="2.4.0")
    private Boolean olcDbSingleConn;

    /**
     * Field for the 'olcDbStartTLS' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbStartTLS", version="2.4.0")
    private String olcDbStartTLS;

    /**
     * Field for the 'olcDbTFSupport' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbTFSupport", version="2.4.0")
    private String olcDbTFSupport;

    /**
     * Field for the 'olcDbTimeout' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbTimeout", version="2.4.0")
    private String olcDbTimeout;

    /**
     * Field for the 'olcDbURI' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbURI", version="2.4.0")
    private String olcDbURI;

    /**
     * Field for the 'olcDbUseTemporaryConn' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbUseTemporaryConn", version="2.4.0")
    private Boolean olcDbUseTemporaryConn;


    // ── addOlcDbIDAssertAuthzFrom — Cassian Adds an Identity Assertion Source Rule ─
    // Cassian adds an "authorized from" rule — this specifies which identities are
    // allowed to trigger identity assertion at the remote server. Only clients whose
    // DN matches one of these patterns can have their identity proxied through.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more identity assertion authorization source strings to the
     * {@code olcDbIDAssertAuthzFrom} list.
     * Each string is a pattern identifying which client DNs are allowed to use
     * identity assertion (olcDbIDAssertMode) at the remote server.
     *
     * <p>For example — Cassian adds an IDAssert source rule:</p>
     * <pre>
     *   ldapConfig.addOlcDbIDAssertAuthzFrom( "dn.subtree=dc=example,dc=com" );
     * </pre>
     *
     * @param strings  the IDAssert authorization source strings to add
     */
    public void addOlcDbIDAssertAuthzFrom( String... strings )
    {
        for ( String string : strings )
        {
            olcDbIDAssertAuthzFrom.add( string );
        }
    }


    // ── addOlcDbIDAssertPassThru — Cassian Adds an Identity Pass-Through Rule ──────
    // Cassian adds a pass-through rule — for clients matching these patterns, their
    // credentials are forwarded directly to the remote server rather than asserting
    // a proxy identity. It's like Cassian letting certain operatives contact the
    // remote server with their own credentials.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more identity assertion pass-through pattern strings to the
     * {@code olcDbIDAssertPassThru} list.
     * Clients matching these patterns bypass identity assertion and bind directly.
     *
     * <p>For example — Cassian adds a pass-through rule:</p>
     * <pre>
     *   ldapConfig.addOlcDbIDAssertPassThru( "dn.exact=cn=trusted,dc=example,dc=com" );
     * </pre>
     *
     * @param strings  the pass-through pattern strings to add
     */
    public void addOlcDbIDAssertPassThru( String... strings )
    {
        for ( String string : strings )
        {
            olcDbIDAssertPassThru.add( string );
        }
    }


    // ── clearOlcDbIDAssertAuthzFrom — Cassian Clears the IDAssert Source Rules ─────
    // Cassian removes all identity assertion authorization source rules.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all identity assertion authorization source strings from the
     * {@code olcDbIDAssertAuthzFrom} list.
     *
     * <p>For example — Cassian clears IDAssert source rules:</p>
     * <pre>
     *   ldapConfig.clearOlcDbIDAssertAuthzFrom();
     * </pre>
     */
    public void clearOlcDbIDAssertAuthzFrom()
    {
        olcDbIDAssertAuthzFrom.clear();
    }


    // ── clearOlcDbIDAssertPassThru — Cassian Clears the Pass-Through Rules ─────────
    // Cassian removes all identity assertion pass-through patterns.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all identity assertion pass-through pattern strings from the
     * {@code olcDbIDAssertPassThru} list.
     *
     * <p>For example — Cassian clears pass-through rules:</p>
     * <pre>
     *   ldapConfig.clearOlcDbIDAssertPassThru();
     * </pre>
     */
    public void clearOlcDbIDAssertPassThru()
    {
        olcDbIDAssertPassThru.clear();
    }


    // ── getOlcDbACLAuthcDn — Cassian Reads the ACL Authentication DN ──────────────
    // Cassian reads the DN used to authenticate against the remote server for ACL
    // evaluation — a special privileged account that can see everything needed for ACL checks.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN used to authenticate to the remote server for ACL evaluation.
     * This is a privileged account at the remote end used specifically for slapd's ACL checks.
     *
     * <p>For example — Cassian reads the ACL auth DN:</p>
     * <pre>
     *   Dn dn = ldapConfig.getOlcDbACLAuthcDn();
     * </pre>
     *
     * @return  the ACL authentication DN, or null if not set
     */
    public Dn getOlcDbACLAuthcDn()
    {
        return olcDbACLAuthcDn;
    }


    // ── getOlcDbACLBind — Cassian Reads the ACL Bind Parameters ───────────────────
    // Cassian reads the bind parameters used when slapd connects to the remote server
    // for ACL evaluation. This describes how to authenticate the ACL check connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind parameters string for the remote ACL evaluation connection.
     *
     * <p>For example — Cassian reads the ACL bind string:</p>
     * <pre>
     *   String bind = ldapConfig.getOlcDbACLBind();
     * </pre>
     *
     * @return  the ACL bind string, or null if not set
     */
    public String getOlcDbACLBind()
    {
        return olcDbACLBind;
    }


    // ── getOlcDbACLPasswd — Cassian Reads the ACL Authentication Password ─────────
    // Cassian reads the password used alongside the ACL auth DN when authenticating
    // to the remote server for ACL evaluation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the password for the ACL authentication DN on the remote server.
     *
     * <p>For example — Cassian reads the ACL auth password:</p>
     * <pre>
     *   String passwd = ldapConfig.getOlcDbACLPasswd();
     * </pre>
     *
     * @return  the ACL auth password, or null if not set
     */
    public String getOlcDbACLPasswd()
    {
        return olcDbACLPasswd;
    }


    // ── getOlcDbCancel — Cassian Reads the Cancel Operation Handling Mode ─────────
    // Cassian reads how the LDAP backend handles Cancel extended operations. Options
    // are "ABANDON" (just abandon the operation) or "IGNORE" (pass through the cancel).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the cancel operation handling mode for this LDAP proxy backend.
     * Controls what happens when a Cancel extended operation arrives from a client.
     *
     * <p>For example — Cassian reads the cancel mode:</p>
     * <pre>
     *   String cancel = ldapConfig.getOlcDbCancel(); // "ABANDON"
     * </pre>
     *
     * @return  the cancel mode string, or null if not set
     */
    public String getOlcDbCancel()
    {
        return olcDbCancel;
    }


    // ── getOlcDbChaseReferrals — Cassian Reads the Chase-Referrals Flag ───────────
    // Cassian reads whether the LDAP backend follows referrals from the remote server.
    // When true, if the remote server sends a referral, slapd will follow it automatically.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the LDAP proxy backend automatically chases referrals
     * from the remote server.
     *
     * <p>For example — Cassian reads the chase-referrals flag:</p>
     * <pre>
     *   Boolean chase = ldapConfig.getOlcDbChaseReferrals(); // true
     * </pre>
     *
     * @return  true if referrals are chased, false if not, null if not set
     */
    public Boolean getOlcDbChaseReferrals()
    {
        return olcDbChaseReferrals;
    }


    // ── getOlcDbConnectionPoolMax — Cassian Reads the Connection Pool Size ─────────
    // Cassian reads the maximum number of connections to keep in the pool to the remote
    // server. A larger pool handles more concurrent operations but uses more resources.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum size of the connection pool for this LDAP proxy backend.
     * The backend maintains a pool of connections to the remote server to avoid
     * the overhead of connecting for every operation.
     *
     * <p>For example — Cassian reads the connection pool size:</p>
     * <pre>
     *   Integer max = ldapConfig.getOlcDbConnectionPoolMax(); // 10
     * </pre>
     *
     * @return  the max connection pool size, or null if not set
     */
    public Integer getOlcDbConnectionPoolMax()
    {
        return olcDbConnectionPoolMax;
    }


    // ── getOlcDbConnTtl — Cassian Reads the Connection Time-to-Live ───────────────
    // Cassian reads how long a connection to the remote server is kept idle in the pool
    // before being closed. Long TTLs reduce reconnection overhead; short ones free resources.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the time-to-live for idle connections in the pool.
     * After this duration an idle connection is closed and removed from the pool.
     *
     * <p>For example — Cassian reads the connection TTL:</p>
     * <pre>
     *   String ttl = ldapConfig.getOlcDbConnTtl(); // "1h"
     * </pre>
     *
     * @return  the connection TTL string, or null if not set
     */
    public String getOlcDbConnTtl()
    {
        return olcDbConnTtl;
    }


    // ── getOlcDbIDAssertAuthcDn — Cassian Reads the Identity Assertion Bind DN ────
    // Cassian reads the DN that slapd uses when binding to the remote server to
    // perform identity assertion. This is the "privileged account" at the remote
    // end that has permission to assert other identities.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN slapd binds with at the remote server to perform identity assertion.
     * This must be an account at the remote server with sufficient privileges to act
     * as a proxy for other identities.
     *
     * <p>For example — Cassian reads the IDAssert auth DN:</p>
     * <pre>
     *   Dn dn = ldapConfig.getOlcDbIDAssertAuthcDn();
     * </pre>
     *
     * @return  the IDAssert authentication DN, or null if not set
     */
    public Dn getOlcDbIDAssertAuthcDn()
    {
        return olcDbIDAssertAuthcDn;
    }


    // ── getOlcDbIDAssertAuthzFrom — Cassian Reads the IDAssert Authorization Sources
    // Cassian reads the list of client identity patterns allowed to trigger identity
    // assertion at the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the identity assertion authorization source strings.
     * Each string is a pattern identifying which client DNs may use identity assertion.
     *
     * <p>For example — Cassian reads the IDAssert source rules:</p>
     * <pre>
     *   List&lt;String&gt; from = ldapConfig.getOlcDbIDAssertAuthzFrom();
     * </pre>
     *
     * @return  a copy of the IDAssert authorization source list; never null
     */
    public List<String> getOlcDbIDAssertAuthzFrom()
    {
        return copyListString( olcDbIDAssertAuthzFrom );
    }


    // ── getOlcDbIDAssertBind — Cassian Reads the IDAssert Bind Parameters ─────────
    // Cassian reads the bind parameters that override the default bind when performing
    // identity assertion at the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind parameter string for identity assertion at the remote server.
     *
     * <p>For example — Cassian reads the IDAssert bind string:</p>
     * <pre>
     *   String bind = ldapConfig.getOlcDbIDAssertBind();
     * </pre>
     *
     * @return  the IDAssert bind string, or null if not set
     */
    public String getOlcDbIDAssertBind()
    {
        return olcDbIDAssertBind;
    }


    // ── getOlcDbIDAssertMode — Cassian Reads the Identity Assertion Mode ──────────
    // Cassian reads the identity assertion mode — controls how slapd presents the
    // client's identity to the remote server (e.g., "none", "self", "legacy",
    // "anonymous", "sasl:EXTERNAL").
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the identity assertion mode string.
     * This controls how slapd presents the client's identity to the remote server.
     * Common values: "none", "self", "legacy", "anonymous", "sasl:EXTERNAL".
     *
     * <p>For example — Cassian reads the IDAssert mode:</p>
     * <pre>
     *   String mode = ldapConfig.getOlcDbIDAssertMode(); // "self"
     * </pre>
     *
     * @return  the IDAssert mode string, or null if not set
     */
    public String getOlcDbIDAssertMode()
    {
        return olcDbIDAssertMode;
    }


    // ── getOlcDbIDAssertPassThru — Cassian Reads the Identity Pass-Through Rules ───
    // Cassian reads the list of client patterns that bypass identity assertion and
    // bind directly to the remote server with their own credentials.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the identity assertion pass-through pattern strings.
     *
     * <p>For example — Cassian reads the pass-through patterns:</p>
     * <pre>
     *   List&lt;String&gt; passThru = ldapConfig.getOlcDbIDAssertPassThru();
     * </pre>
     *
     * @return  a copy of the IDAssert pass-through list; never null
     */
    public List<String> getOlcDbIDAssertPassThru()
    {
        return copyListString( olcDbIDAssertPassThru );
    }


    // ── getOlcDbIDAssertPasswd — Cassian Reads the Identity Assertion Password ─────
    // Cassian reads the password used when binding as the IDAssert auth DN at the
    // remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the password for the identity assertion authentication DN at the remote server.
     *
     * <p>For example — Cassian reads the IDAssert password:</p>
     * <pre>
     *   String pw = ldapConfig.getOlcDbIDAssertPasswd();
     * </pre>
     *
     * @return  the IDAssert password, or null if not set
     */
    public String getOlcDbIDAssertPasswd()
    {
        return olcDbIDAssertPasswd;
    }


    // ── getOlcDbIdleTimeout — Cassian Reads the Idle Connection Timeout ───────────
    // Cassian reads how long a connection to the remote server can stay idle before
    // slapd closes it automatically — even if it's actively in the connection pool.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the idle timeout duration for remote server connections.
     * After this duration of inactivity, the connection is closed.
     *
     * <p>For example — Cassian reads the idle timeout:</p>
     * <pre>
     *   String timeout = ldapConfig.getOlcDbIdleTimeout(); // "10m"
     * </pre>
     *
     * @return  the idle timeout string, or null if not set
     */
    public String getOlcDbIdleTimeout()
    {
        return olcDbIdleTimeout;
    }


    // ── getOlcDbKeepalive — Cassian Reads the TCP Keepalive Settings ──────────────
    // Cassian reads the TCP keepalive settings for connections to the remote server.
    // Keepalives detect broken connections before LDAP operations time out.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the TCP keepalive settings for remote server connections.
     * Format: "idle:interval:count" (all in seconds/count).
     *
     * <p>For example — Cassian reads the keepalive settings:</p>
     * <pre>
     *   String ka = ldapConfig.getOlcDbKeepalive(); // "60:30:3"
     * </pre>
     *
     * @return  the keepalive settings string, or null if not set
     */
    public String getOlcDbKeepalive()
    {
        return olcDbKeepalive;
    }


    // ── getOlcDbNetworkTimeout — Cassian Reads the Network Operation Timeout ───────
    // Cassian reads the timeout for network-level operations (connect, send, receive)
    // to the remote server. If the network doesn't respond within this time, the
    // connection is abandoned.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the network operation timeout for connections to the remote server.
     *
     * <p>For example — Cassian reads the network timeout:</p>
     * <pre>
     *   String netTimeout = ldapConfig.getOlcDbNetworkTimeout(); // "5s"
     * </pre>
     *
     * @return  the network timeout string, or null if not set
     */
    public String getOlcDbNetworkTimeout()
    {
        return olcDbNetworkTimeout;
    }


    // ── getOlcDbNoRefs — Cassian Reads the No-Referrals Flag ──────────────────────
    // Cassian reads whether the backend suppresses referrals from the remote server.
    // When true, referrals are dropped rather than being forwarded to clients.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether referrals from the remote server are suppressed.
     * When true, referral results from the remote are dropped and not forwarded to clients.
     *
     * <p>For example — Cassian reads the no-referrals flag:</p>
     * <pre>
     *   Boolean noRefs = ldapConfig.getOlcDbNoRefs(); // false
     * </pre>
     *
     * @return  true if referrals are suppressed, false if not, null if not set
     */
    public Boolean getOlcDbNoRefs()
    {
        return olcDbNoRefs;
    }


    // ── getOlcDbNoUndefFilter — Cassian Reads the No-Undefined-Filter Flag ─────────
    // Cassian reads whether searches with undefined filter components are suppressed.
    // When true, searches containing unknown attribute types in the filter return
    // an empty result rather than an error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether searches with undefined filter components return empty results
     * instead of an "undefined filter" error.
     *
     * <p>For example — Cassian reads the no-undefined-filter flag:</p>
     * <pre>
     *   Boolean noUndef = ldapConfig.getOlcDbNoUndefFilter(); // false
     * </pre>
     *
     * @return  true if undefined filters return empty, false if they error, null if not set
     */
    public Boolean getOlcDbNoUndefFilter()
    {
        return olcDbNoUndefFilter;
    }


    // ── getOlcDbOnErr — Cassian Reads the On-Error Action ─────────────────────────
    // Cassian reads what to do when the remote server returns an error. Options include
    // "continue" (try the next server), "stop" (propagate the error), or "report"
    // (log and propagate).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the on-error action for this LDAP proxy backend.
     * Controls what slapd does when the remote server returns an error.
     *
     * <p>For example — Cassian reads the on-error action:</p>
     * <pre>
     *   String onErr = ldapConfig.getOlcDbOnErr(); // "continue"
     * </pre>
     *
     * @return  the on-error action string, or null if not set
     */
    public String getOlcDbOnErr()
    {
        return olcDbOnErr;
    }


    // ── getOlcDbProtocolVersion — Cassian Reads the LDAP Protocol Version ─────────
    // Cassian reads which version of the LDAP protocol slapd uses when connecting
    // to the remote server (2 or 3; modern servers require 3).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP protocol version used when connecting to the remote server.
     * Should be 3 for modern deployments; 2 for legacy compatibility only.
     *
     * <p>For example — Cassian reads the protocol version:</p>
     * <pre>
     *   Integer ver = ldapConfig.getOlcDbProtocolVersion(); // 3
     * </pre>
     *
     * @return  the protocol version (2 or 3), or null if not set
     */
    public Integer getOlcDbProtocolVersion()
    {
        return olcDbProtocolVersion;
    }


    // ── getOlcDbProxyWhoAmI — Cassian Reads the Proxy-WhoAmI Flag ─────────────────
    // Cassian reads whether WhoAmI extended operations are proxied through to the
    // remote server. When true, the WhoAmI response reflects the remote server's view
    // of the authenticated identity.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether WhoAmI extended operations are proxied to the remote server.
     *
     * <p>For example — Cassian reads the proxy-WhoAmI flag:</p>
     * <pre>
     *   Boolean proxy = ldapConfig.getOlcDbProxyWhoAmI(); // false
     * </pre>
     *
     * @return  true if WhoAmI is proxied, false if not, null if not set
     */
    public Boolean getOlcDbProxyWhoAmI()
    {
        return olcDbProxyWhoAmI;
    }


    // ── getOlcDbQuarantine — Cassian Reads the Quarantine Settings ────────────────
    // Cassian reads the quarantine configuration — when the remote server is
    // unreachable, the quarantine logic temporarily suspends attempts to connect
    // to it, avoiding repeated failed connections. Like putting a blown relay in
    // quarantine until it can be reset.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the quarantine configuration string for this LDAP proxy backend.
     * Quarantine prevents slapd from repeatedly trying an unreachable remote server.
     *
     * <p>For example — Cassian reads the quarantine config:</p>
     * <pre>
     *   String q = ldapConfig.getOlcDbQuarantine(); // "threshold=5,interval=30"
     * </pre>
     *
     * @return  the quarantine configuration string, or null if not set
     */
    public String getOlcDbQuarantine()
    {
        return olcDbQuarantine;
    }


    // ── getOlcDbRebindAsUser — Cassian Reads the Rebind-As-User Flag ──────────────
    // Cassian reads whether the backend rebinds to the remote server using the
    // client's credentials on subsequent requests. This allows the full user
    // credentials to be forwarded on reconnect.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the backend rebinds to the remote server as the client user
     * on subsequent requests.
     *
     * <p>For example — Cassian reads the rebind-as-user flag:</p>
     * <pre>
     *   Boolean rebind = ldapConfig.getOlcDbRebindAsUser(); // false
     * </pre>
     *
     * @return  true if rebind-as-user is enabled, false if not, null if not set
     */
    public Boolean getOlcDbRebindAsUser()
    {
        return olcDbRebindAsUser;
    }


    // ── getOlcDbSessionTrackingRequest — Cassian Reads the Session-Tracking Flag ───
    // Cassian reads whether session tracking information is forwarded in requests to
    // the remote server (RFC 3876 session tracking control).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether session tracking requests (RFC 3876) are forwarded to the remote server.
     *
     * <p>For example — Cassian reads the session tracking flag:</p>
     * <pre>
     *   Boolean st = ldapConfig.getOlcDbSessionTrackingRequest(); // false
     * </pre>
     *
     * @return  true if session tracking is forwarded, false if not, null if not set
     */
    public Boolean getOlcDbSessionTrackingRequest()
    {
        return olcDbSessionTrackingRequest;
    }


    // ── getOlcDbSingleConn — Cassian Reads the Single-Connection Flag ─────────────
    // Cassian reads whether a single connection is used per client (rather than
    // drawing from the pool). Useful for backends that require stateful connections.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether a single connection per client session is enforced.
     *
     * <p>For example — Cassian reads the single-connection flag:</p>
     * <pre>
     *   Boolean single = ldapConfig.getOlcDbSingleConn(); // false
     * </pre>
     *
     * @return  true if single-connection mode is on, false if not, null if not set
     */
    public Boolean getOlcDbSingleConn()
    {
        return olcDbSingleConn;
    }


    // ── getOlcDbStartTLS — Cassian Reads the StartTLS Configuration ───────────────
    // Cassian reads the StartTLS parameters for connections to the remote server.
    // This is how the proxy upgrades a plain connection to TLS.
    // ────────────────────────────────────────────────────────────────────="────────
    /**
     * Returns the StartTLS configuration string for connections to the remote server.
     *
     * <p>For example — Cassian reads the StartTLS config:</p>
     * <pre>
     *   String tls = ldapConfig.getOlcDbStartTLS(); // "starttls=yes tls_reqcert=demand"
     * </pre>
     *
     * @return  the StartTLS configuration string, or null if not set
     */
    public String getOlcDbStartTLS()
    {
        return olcDbStartTLS;
    }


    // ── getOlcDbTFSupport — Cassian Reads the True/False Filter Support Mode ───────
    // Cassian reads the T/F (true/false) filter support configuration, which tells
    // the backend how to handle LDAP filters like (&) (always true) and (|) (always false)
    // when the remote server doesn't support them natively.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the true/false filter support configuration string.
     * Controls how absolute true/false filters are handled at the remote server.
     *
     * <p>For example — Cassian reads the TF support mode:</p>
     * <pre>
     *   String tf = ldapConfig.getOlcDbTFSupport(); // "yes"
     * </pre>
     *
     * @return  the TF support string, or null if not set
     */
    public String getOlcDbTFSupport()
    {
        return olcDbTFSupport;
    }


    // ── getOlcDbTimeout — Cassian Reads the LDAP Operation Timeout ────────────────
    // Cassian reads the timeout for individual LDAP operations at the remote server.
    // Operations that take longer than this are abandoned.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP operation timeout for the remote server.
     *
     * <p>For example — Cassian reads the operation timeout:</p>
     * <pre>
     *   String timeout = ldapConfig.getOlcDbTimeout(); // "30s"
     * </pre>
     *
     * @return  the operation timeout string, or null if not set
     */
    public String getOlcDbTimeout()
    {
        return olcDbTimeout;
    }


    // ── getOlcDbURI — Cassian Reads the Remote Server URI(s) ──────────────────────
    // Cassian reads the URI(s) of the remote LDAP server(s) to proxy operations to.
    // Multiple URIs provide failover: slapd tries each in order until one connects.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP URI(s) of the remote server(s) for this proxy backend.
     * Multiple URIs separated by spaces provide failover.
     *
     * <p>For example — Cassian reads the remote server URI:</p>
     * <pre>
     *   String uri = ldapConfig.getOlcDbURI(); // "ldaps://ldap1.example.com ldap://ldap2.example.com"
     * </pre>
     *
     * @return  the remote server URI string, or null if not set
     */
    public String getOlcDbURI()
    {
        return olcDbURI;
    }


    // ── getOlcDbUseTemporaryConn — Cassian Reads the Temporary-Connection Flag ─────
    // Cassian reads whether temporary (non-pooled) connections are used for operations
    // that require a fresh authenticated connection each time.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether temporary (non-pooled) connections are used for operations.
     *
     * <p>For example — Cassian reads the temporary-connection flag:</p>
     * <pre>
     *   Boolean tempConn = ldapConfig.getOlcDbUseTemporaryConn(); // false
     * </pre>
     *
     * @return  true if temporary connections are used, false if not, null if not set
     */
    public Boolean getOlcDbUseTemporaryConn()
    {
        return olcDbUseTemporaryConn;
    }


    // ── setOlcDbACLAuthcDn — Cassian Sets the ACL Authentication DN ───────────────
    // Cassian sets the DN used to authenticate to the remote server for ACL evaluation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN used to authenticate to the remote server for ACL evaluation.
     *
     * @param olcDbACLAuthcDn  the ACL authentication DN to set
     */
    public void setOlcDbACLAuthcDn( Dn olcDbACLAuthcDn )
    {
        this.olcDbACLAuthcDn = olcDbACLAuthcDn;
    }


    // ── setOlcDbACLBind — Cassian Sets the ACL Bind Parameters ───────────────────
    // Cassian sets the bind parameters for the remote ACL evaluation connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the bind parameters string for the remote ACL evaluation connection.
     *
     * @param olcDbACLBind  the ACL bind string to set
     */
    public void setOlcDbACLBind( String olcDbACLBind )
    {
        this.olcDbACLBind = olcDbACLBind;
    }


    // ── setOlcDbACLPasswd — Cassian Sets the ACL Authentication Password ──────────
    // Cassian sets the password for the ACL authentication DN.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the password for the ACL authentication DN on the remote server.
     *
     * @param olcDbACLPasswd  the ACL auth password to set
     */
    public void setOlcDbACLPasswd( String olcDbACLPasswd )
    {
        this.olcDbACLPasswd = olcDbACLPasswd;
    }


    // ── setOlcDbCancel — Cassian Sets the Cancel Operation Handling Mode ──────────
    // Cassian sets how Cancel extended operations are handled by this backend.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the cancel operation handling mode for this LDAP proxy backend.
     *
     * @param olcDbCancel  the cancel mode string to set (e.g., "ABANDON")
     */
    public void setOlcDbCancel( String olcDbCancel )
    {
        this.olcDbCancel = olcDbCancel;
    }


    // ── setOlcDbChaseReferrals — Cassian Sets the Chase-Referrals Flag ────────────
    // Cassian sets whether the backend follows referrals from the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether the LDAP proxy backend automatically chases referrals from the remote server.
     *
     * @param olcDbChaseReferrals  true to chase referrals; false to return them to clients
     */
    public void setOlcDbChaseReferrals( Boolean olcDbChaseReferrals )
    {
        this.olcDbChaseReferrals = olcDbChaseReferrals;
    }


    // ── setOlcDbConnectionPoolMax — Cassian Sets the Connection Pool Size ──────────
    // Cassian sets the maximum number of connections in the pool to the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the maximum size of the connection pool for this LDAP proxy backend.
     *
     * @param olcDbConnectionPoolMax  the max connection pool size to set
     */
    public void setOlcDbConnectionPoolMax( Integer olcDbConnectionPoolMax )
    {
        this.olcDbConnectionPoolMax = olcDbConnectionPoolMax;
    }


    // ── setOlcDbConnTtl — Cassian Sets the Connection Time-to-Live ───────────────
    // Cassian sets how long idle connections are kept before being closed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the time-to-live for idle connections in the pool.
     *
     * @param olcDbConnTtl  the connection TTL string to set (e.g., "1h")
     */
    public void setOlcDbConnTtl( String olcDbConnTtl )
    {
        this.olcDbConnTtl = olcDbConnTtl;
    }


    // ── setOlcDbIDAssertAuthcDn — Cassian Sets the IDAssert Bind DN ───────────────
    // Cassian sets the DN used for identity assertion binds at the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN slapd binds with at the remote server to perform identity assertion.
     *
     * @param olcDbIDAssertAuthcDn  the IDAssert authentication DN to set
     */
    public void setOlcDbIDAssertAuthcDn( Dn olcDbIDAssertAuthcDn )
    {
        this.olcDbIDAssertAuthcDn = olcDbIDAssertAuthcDn;
    }


    // ── setOlcDbIDAssertAuthzFrom — Cassian Replaces the IDAssert Source Rules ─────
    // Cassian replaces the list of client patterns allowed to trigger identity assertion.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the identity assertion authorization source list.
     *
     * @param olcDbIDAssertAuthzFrom  the new IDAssert authorization source list
     */
    public void setOlcDbIDAssertAuthzFrom( List<String> olcDbIDAssertAuthzFrom )
    {
        this.olcDbIDAssertAuthzFrom = copyListString( olcDbIDAssertAuthzFrom );
    }


    // ── setOlcDbIDAssertBind — Cassian Sets the IDAssert Bind Parameters ──────────
    // Cassian sets the bind parameters for identity assertion at the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the bind parameter string for identity assertion at the remote server.
     *
     * @param olcDbIDAssertBind  the IDAssert bind string to set
     */
    public void setOlcDbIDAssertBind( String olcDbIDAssertBind )
    {
        this.olcDbIDAssertBind = olcDbIDAssertBind;
    }


    // ── setOlcDbIDAssertMode — Cassian Sets the Identity Assertion Mode ────────────
    // Cassian sets the mode controlling how the client's identity is presented to the
    // remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the identity assertion mode string.
     *
     * @param olcDbIDAssertMode  the IDAssert mode to set (e.g., "self", "anonymous")
     */
    public void setOlcDbIDAssertMode( String olcDbIDAssertMode )
    {
        this.olcDbIDAssertMode = olcDbIDAssertMode;
    }


    // ── setOlcDbIDAssertPassThru — Cassian Replaces the Pass-Through Rules ─────────
    // Cassian replaces the list of patterns for clients that bypass identity assertion.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the identity assertion pass-through pattern list.
     *
     * @param olcDbIDAssertPassThru  the new IDAssert pass-through list
     */
    public void setOlcDbIDAssertPassThru( List<String> olcDbIDAssertPassThru )
    {
        this.olcDbIDAssertPassThru = copyListString( olcDbIDAssertPassThru );
    }


    // ── setOlcDbIDAssertPasswd — Cassian Sets the IDAssert Password ───────────────
    // Cassian sets the password used when binding as the IDAssert DN at the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the password for the identity assertion authentication DN at the remote server.
     *
     * @param olcDbIDAssertPasswd  the IDAssert password to set
     */
    public void setOlcDbIDAssertPasswd( String olcDbIDAssertPasswd )
    {
        this.olcDbIDAssertPasswd = olcDbIDAssertPasswd;
    }


    // ── setOlcDbIdleTimeout — Cassian Sets the Idle Connection Timeout ────────────
    // Cassian sets how long a connection can stay idle before being closed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the idle timeout duration for remote server connections.
     *
     * @param olcDbIdleTimeout  the idle timeout string to set (e.g., "10m")
     */
    public void setOlcDbIdleTimeout( String olcDbIdleTimeout )
    {
        this.olcDbIdleTimeout = olcDbIdleTimeout;
    }


    // ── setOlcDbKeepalive — Cassian Sets the TCP Keepalive Settings ───────────────
    // Cassian sets the TCP keepalive parameters for remote server connections.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the TCP keepalive settings for remote server connections.
     *
     * @param olcDbKeepalive  the keepalive settings string to set (e.g., "60:30:3")
     */
    public void setOlcDbKeepalive( String olcDbKeepalive )
    {
        this.olcDbKeepalive = olcDbKeepalive;
    }


    // ── setOlcDbNetworkTimeout — Cassian Sets the Network Operation Timeout ────────
    // Cassian sets the network-level timeout for connections to the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the network operation timeout for connections to the remote server.
     *
     * @param olcDbNetworkTimeout  the network timeout string to set
     */
    public void setOlcDbNetworkTimeout( String olcDbNetworkTimeout )
    {
        this.olcDbNetworkTimeout = olcDbNetworkTimeout;
    }


    // ── setOlcDbNoRefs — Cassian Sets the No-Referrals Flag ───────────────────────
    // Cassian enables or disables suppression of referrals from the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether referrals from the remote server are suppressed.
     *
     * @param olcDbNoRefs  true to suppress referrals; false to forward them
     */
    public void setOlcDbNoRefs( Boolean olcDbNoRefs )
    {
        this.olcDbNoRefs = olcDbNoRefs;
    }


    // ── setOlcDbNoUndefFilter — Cassian Sets the No-Undefined-Filter Flag ──────────
    // Cassian sets whether undefined filter components return empty results or errors.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether searches with undefined filter components return empty results.
     *
     * @param olcDbNoUndefFilter  true to return empty; false to error
     */
    public void setOlcDbNoUndefFilter( Boolean olcDbNoUndefFilter )
    {
        this.olcDbNoUndefFilter = olcDbNoUndefFilter;
    }


    // ── setOlcDbOnErr — Cassian Sets the On-Error Action ──────────────────────────
    // Cassian sets what to do when the remote server returns an error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the on-error action for this LDAP proxy backend.
     *
     * @param olcDbOnErr  the on-error action string to set (e.g., "continue")
     */
    public void setOlcDbOnErr( String olcDbOnErr )
    {
        this.olcDbOnErr = olcDbOnErr;
    }


    // ── setOlcDbProtocolVersion — Cassian Sets the LDAP Protocol Version ──────────
    // Cassian sets which LDAP protocol version to use when connecting to the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP protocol version for connections to the remote server.
     *
     * @param olcDbProtocolVersion  the protocol version (2 or 3) to set
     */
    public void setOlcDbProtocolVersion( Integer olcDbProtocolVersion )
    {
        this.olcDbProtocolVersion = olcDbProtocolVersion;
    }


    // ── setOlcDbProxyWhoAmI — Cassian Sets the Proxy-WhoAmI Flag ─────────────────
    // Cassian enables or disables proxying of WhoAmI extended operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether WhoAmI extended operations are proxied to the remote server.
     *
     * @param olcDbProxyWhoAmI  true to proxy WhoAmI; false to handle locally
     */
    public void setOlcDbProxyWhoAmI( Boolean olcDbProxyWhoAmI )
    {
        this.olcDbProxyWhoAmI = olcDbProxyWhoAmI;
    }


    // ── setOlcDbQuarantine — Cassian Sets the Quarantine Configuration ─────────────
    // Cassian sets the quarantine configuration that prevents repeated connection
    // attempts to an unreachable remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the quarantine configuration for this LDAP proxy backend.
     *
     * @param olcDbQuarantine  the quarantine configuration string to set
     */
    public void setOlcDbQuarantine( String olcDbQuarantine )
    {
        this.olcDbQuarantine = olcDbQuarantine;
    }


    // ── setOlcDbRebindAsUser — Cassian Sets the Rebind-As-User Flag ───────────────
    // Cassian enables or disables rebinding to the remote server as the client user.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether the backend rebinds to the remote server as the client user.
     *
     * @param olcDbRebindAsUser  true to rebind as user; false to use pooled connections
     */
    public void setOlcDbRebindAsUser( Boolean olcDbRebindAsUser )
    {
        this.olcDbRebindAsUser = olcDbRebindAsUser;
    }


    // ── setOlcDbSessionTrackingRequest — Cassian Sets the Session-Tracking Flag ────
    // Cassian enables or disables forwarding of session tracking controls.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether session tracking requests are forwarded to the remote server.
     *
     * @param olcDbSessionTrackingRequest  true to forward session tracking; false to not
     */
    public void setOlcDbSessionTrackingRequest( Boolean olcDbSessionTrackingRequest )
    {
        this.olcDbSessionTrackingRequest = olcDbSessionTrackingRequest;
    }


    // ── setOlcDbSingleConn — Cassian Sets the Single-Connection Flag ───────────────
    // Cassian enables or disables single-connection mode for this backend.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether a single connection per client session is enforced.
     *
     * @param olcDbSingleConn  true for single-connection mode; false for pooled
     */
    public void setOlcDbSingleConn( Boolean olcDbSingleConn )
    {
        this.olcDbSingleConn = olcDbSingleConn;
    }


    // ── setOlcDbStartTLS — Cassian Sets the StartTLS Configuration ────────────────
    // Cassian sets the StartTLS parameters for connections to the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the StartTLS configuration for connections to the remote server.
     *
     * @param olcDbStartTLS  the StartTLS configuration string to set
     */
    public void setOlcDbStartTLS( String olcDbStartTLS )
    {
        this.olcDbStartTLS = olcDbStartTLS;
    }


    // ── setOlcDbTFSupport — Cassian Sets the True/False Filter Support Mode ────────
    // Cassian sets how absolute true/false LDAP filters are handled at the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the true/false filter support configuration for this LDAP proxy backend.
     *
     * @param olcDbTFSupport  the TF support string to set (e.g., "yes", "no")
     */
    public void setOlcDbTFSupport( String olcDbTFSupport )
    {
        this.olcDbTFSupport = olcDbTFSupport;
    }


    // ── setOlcDbTimeout — Cassian Sets the LDAP Operation Timeout ────────────────
    // Cassian sets the timeout for individual LDAP operations at the remote server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP operation timeout for the remote server.
     *
     * @param olcDbTimeout  the operation timeout string to set (e.g., "30s")
     */
    public void setOlcDbTimeout( String olcDbTimeout )
    {
        this.olcDbTimeout = olcDbTimeout;
    }


    // ── setOlcDbURI — Cassian Sets the Remote Server URI(s) ───────────────────────
    // Cassian sets the URI(s) of the remote LDAP server(s) to proxy operations to.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP URI(s) of the remote server(s) for this proxy backend.
     *
     * @param olcDbURI  the remote server URI string to set
     */
    public void setOlcDbURI( String olcDbURI )
    {
        this.olcDbURI = olcDbURI;
    }


    // ── setOlcDbUseTemporaryConn — Cassian Sets the Temporary-Connection Flag ──────
    // Cassian enables or disables use of temporary (non-pooled) connections.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether temporary (non-pooled) connections are used for operations.
     *
     * @param olcDbUseTemporaryConn  true to use temporary connections; false for pooled
     */
    public void setOlcDbUseTemporaryConn( Boolean olcDbUseTemporaryConn )
    {
        this.olcDbUseTemporaryConn = olcDbUseTemporaryConn;
    }


    // ── getOlcDatabaseType — Cassian Identifies This as the LDAP Proxy Backend ─────
    // Cassian identifies this relay station as the "ldap" type — the LDAP proxy backend.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "ldap", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Cassian identifies the LDAP proxy backend:</p>
     * <pre>
     *   ldapConfig.getOlcDatabaseType(); // "ldap"
     * </pre>
     *
     * @return  the lowercase database type string "ldap"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.LDAP.toString().toLowerCase();
    }
}
