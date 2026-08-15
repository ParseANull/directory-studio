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
package org.apache.directory.studio.openldap.config.model;


import java.util.ArrayList;
import java.util.List;


// ── CLASS: OlcGlobal — Palpatine Issuing Order 66 ────────────────────────────
// Palpatine's Order 66 touches every part of the Empire simultaneously: threads,
// timeouts, TLS certificates, password hashing, SASL realms, TCP buffers — all
// set in a single sweeping command. OlcGlobal is exactly that. It is the global
// cn=config entry for slapd, holding every server-wide setting in one place.
// Change the thread pool size here and every database sees the effect.
// Enable TLS here and it applies to all LDAP listeners.
// Think of this class as the Emperor's command console: 60+ knobs, all wired
// to the entire Empire at once.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcGlobal} object class, representing the global
 * slapd cn=config settings entry.
 * <p>
 * This is the top-level configuration object for an OpenLDAP server — it controls
 * server-wide settings including thread pools, TLS certificates, SASL authentication,
 * logging, index parameters, connection limits, password hashing, and more.
 * Changes here affect every database and overlay configured on the server.
 * Think of this class as Palpatine issuing Order 66 — one command, everything changes.
 * </p>
 * <p>
 * Attributes added in specific OpenLDAP releases:
 * </p>
 * <ul>
 *   <li>olcTCPBuffer (List&lt;String&gt;) : 2.4.18</li>
 *   <li>olcSaslAuxpropsDontUseCopy (String) : 2.4.22</li>
 *   <li>olcSaslAuxpropsDontUseCopyIgnore (Boolean) : 2.4.22</li>
 *   <li>olcIndexHash64 (Boolean) : 2.4.34</li>
 *   <li>olcListenerThreads (Integer) : 2.4.36</li>
 *   <li>olcThreadQueues (Integer) : 2.4.36</li>
 *   <li>olcTLSProtocolMin (String) : 2.4.37</li>
 *   <li>olcTLSECName (String) : 2.4.??? (not yet released)</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcGlobal extends OlcConfig
{
    /**
     * Field for the 'cn' attribute.
     */
    @ConfigurationElement(attributeType = "cn", isRdn = true, defaultValue="config", isOptional = false, version="2.4.0")
    private List<String> cn = new ArrayList<>();

    /**
     * Field for the 'olcAllows' attribute.
     */
    @ConfigurationElement(attributeType = "olcAllows", version="2.4.0")
    private List<String> olcAllows = new ArrayList<>();

    /**
     * Field for the 'olcArgsFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcArgsFile", version="2.4.0")
    private String olcArgsFile;

    /**
     * Field for the 'olcAttributeOptions' attribute.
     */
    @ConfigurationElement(attributeType = "olcAttributeOptions", version="2.4.0")
    private List<String> olcAttributeOptions = new ArrayList<>();

    /**
     * Field for the 'olcAttributeTypes' attribute.
     */
    @ConfigurationElement(attributeType = "olcAttributeTypes", version="2.4.0")
    private List<String> olcAttributeTypes = new ArrayList<>();

    /**
     * Field for the 'olcAuthIDRewrite' attribute.
     */
    @ConfigurationElement(attributeType = "olcAuthIDRewrite", version="2.4.0")
    private List<String> olcAuthIDRewrite = new ArrayList<>();

    /**
     * Field for the 'olcAuthzPolicy' attribute.
     */
    @ConfigurationElement(attributeType = "olcAuthzPolicy", version="2.4.0")
    private String olcAuthzPolicy;

    /**
     * Field for the 'olcAuthzRegexp' attribute.
     */
    @ConfigurationElement(attributeType = "olcAuthzRegexp", version="2.4.0")
    private List<String> olcAuthzRegexp = new ArrayList<>();

    /**
     * Field for the 'olcConcurrency' attribute.
     */
    @ConfigurationElement(attributeType = "olcConcurrency", version="2.4.0")
    private Integer olcConcurrency;

    /**
     * Field for the 'olcConfigDir' attribute.
     */
    @ConfigurationElement(attributeType = "olcConfigDir", version="2.4.0")
    private String olcConfigDir;

    /**
     * Field for the 'olcConfigFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcConfigFile", version="2.4.0")
    private String olcConfigFile;

    /**
     * Field for the 'olcConnMaxPending' attribute.
     */
    @ConfigurationElement(attributeType = "olcConnMaxPending", version="2.4.0")
    private Integer olcConnMaxPending;

    /**
     * Field for the 'olcConnMaxPendingAuth' attribute.
     */
    @ConfigurationElement(attributeType = "olcConnMaxPendingAuth", version="2.4.0")
    private Integer olcConnMaxPendingAuth;

    /**
     * Field for the 'olcDisallows' attribute.
     */
    @ConfigurationElement(attributeType = "olcDisallows", version="2.4.0")
    private List<String> olcDisallows = new ArrayList<>();

    /**
     * Field for the 'olcDitContentRules' attribute.
     */
    @ConfigurationElement(attributeType = "olcDitContentRules", version="2.4.0")
    private List<String> olcDitContentRules = new ArrayList<>();

    /**
     * Field for the 'olcGentleHUP' attribute.
     */
    @ConfigurationElement(attributeType = "olcGentleHUP", version="2.4.0")
    private Boolean olcGentleHUP;

    /**
     * Field for the 'olcIdleTimeout' attribute.
     */
    @ConfigurationElement(attributeType = "olcIdleTimeout", version="2.4.0")
    private Integer olcIdleTimeout;

    /**
     * Field for the 'olcIndexHash64' attribute. (Added in OpenLDAP 2.4.34)
     */
    @ConfigurationElement(attributeType = "olcIndexHash64", version="2.4.34")
    private Boolean olcIndexHash64;

    /**
     * Field for the 'olcIndexIntLen' attribute.
     */
    @ConfigurationElement(attributeType = "olcIndexIntLen", version="2.4.7")
    private Integer olcIndexIntLen;

    /**
     * Field for the 'olcIndexSubstrAnyLen' attribute.
     */
    @ConfigurationElement(attributeType = "olcIndexSubstrAnyLen", version="2.4.0")
    private Integer olcIndexSubstrAnyLen;

    /**
     * Field for the 'olcIndexSubstrAnyStep' attribute.
     */
    @ConfigurationElement(attributeType = "olcIndexSubstrAnyStep", version="2.4.0")
    private Integer olcIndexSubstrAnyStep;

    /**
     * Field for the 'olcIndexSubstrIfMaxLen' attribute.
     */
    @ConfigurationElement(attributeType = "olcIndexSubstrIfMaxLen", version="2.4.0")
    private Integer olcIndexSubstrIfMaxLen;

    /**
     * Field for the 'olcIndexSubstrIfMinLen' attribute.
     */
    @ConfigurationElement(attributeType = "olcIndexSubstrIfMinLen", version="2.4.0")
    private Integer olcIndexSubstrIfMinLen;

    /**
     * Field for the 'olcLdapSyntaxes' attribute.
     */
    @ConfigurationElement(attributeType = "olcLdapSyntaxes", version="2.4.12")
    private List<String> olcLdapSyntaxes = new ArrayList<>();

    /**
     * Field for the 'olcListenerThreads' attribute. (Added in OpenLDAP 2.4.36)
     */
    @ConfigurationElement(attributeType = "olcListenerThreads", version="2.4.36")
    private Integer olcListenerThreads;

    /**
     * Field for the 'olcLocalSSF' attribute.
     */
    @ConfigurationElement(attributeType = "olcLocalSSF", version="2.4.0")
    private Integer olcLocalSSF;

    /**
     * Field for the 'olcLogFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcLogFile", version="2.4.0")
    private String olcLogFile;

    /**
     * Field for the 'olcLogLevel' attribute.
     */
    @ConfigurationElement(attributeType = "olcLogLevel", version="2.4.0")
    private List<String> olcLogLevel = new ArrayList<>();

    /**
     * Field for the 'olcObjectClasses' attribute.
     */
    @ConfigurationElement(attributeType = "olcObjectClasses", version="2.4.0")
    private List<String> olcObjectClasses = new ArrayList<>();

    /**
     * Field for the 'olcObjectIdentifier' attribute.
     */
    @ConfigurationElement(attributeType = "olcObjectIdentifier", version="2.4.0")
    private List<String> olcObjectIdentifier = new ArrayList<>();

    /**
     * Field for the 'olcPasswordCryptSaltFormat' attribute.
     */
    @ConfigurationElement(attributeType = "olcPasswordCryptSaltFormat", version="2.4.0")
    private String olcPasswordCryptSaltFormat;

    /**
     * Field for the 'olcPasswordHash' attribute.
     */
    @ConfigurationElement(attributeType = "olcPasswordHash", version="2.4.0")
    private List<String> olcPasswordHash = new ArrayList<>();

    /**
     * Field for the 'olcPidFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcPidFile", version="2.4.0")
    private String olcPidFile;

    /**
     * Field for the 'olcPluginLogFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcPluginLogFile", version="2.4.0")
    private String olcPluginLogFile;

    /**
     * Field for the 'olcReadOnly' attribute.
     */
    @ConfigurationElement(attributeType = "olcReadOnly", version="2.4.0")
    private Boolean olcReadOnly;

    /**
     * Field for the 'olcReferral' attribute.
     */
    @ConfigurationElement(attributeType = "olcReferral", version="2.4.0")
    private String olcReferral;

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
     * Field for the 'olcReverseLookup' attribute.
     */
    @ConfigurationElement(attributeType = "olcReverseLookup", version="2.4.0")
    private Boolean olcReverseLookup;

    /**
     * Field for the 'olcRootDSE' attribute.
     */
    @ConfigurationElement(attributeType = "olcRootDSE", version="2.4.0")
    private List<String> olcRootDSE;

    /**
     * Field for the 'olcSaslAuxprops' attribute.
     */
    @ConfigurationElement(attributeType = "olcSaslAuxprops", version="2.4.17")
    private String olcSaslAuxprops;

    /**
     * Field for the 'olcSaslAuxpropsDontUseCopy' attribute. (Added in OpenLDAP 2.4.22)
     */
    @ConfigurationElement(attributeType = "olcSaslAuxpropsDontUseCopy", version="2.4.22")
    private String olcSaslAuxpropsDontUseCopy;

    /**
     * Field for the 'olcSaslAuxpropsDontUseCopyIgnore' attribute. (Added in OpenLDAP 2.4.22)
     */
    @ConfigurationElement(attributeType = "olcSaslAuxpropsDontUseCopyIgnore", version="2.4.22")
    private Boolean olcSaslAuxpropsDontUseCopyIgnore;

    /**
     * Field for the 'olcSaslHost' attribute.
     */
    @ConfigurationElement(attributeType = "olcSaslHost", version="2.4.0")
    private String olcSaslHost;

    /**
     * Field for the 'olcSaslRealm' attribute.
     */
    @ConfigurationElement(attributeType = "olcSaslRealm", version="2.4.0")
    private String olcSaslRealm;

    /**
     * Field for the 'olcSaslSecProps' attribute.
     */
    @ConfigurationElement(attributeType = "olcSaslSecProps", version="2.4.0")
    private String olcSaslSecProps;

    /**
     * Field for the 'olcSecurity' attribute.
     */
    @ConfigurationElement(attributeType = "olcSecurity", version="2.4.0")
    private List<String> olcSecurity = new ArrayList<>();

    /**
     * Field for the 'olcServerID' attribute.
     */
    @ConfigurationElement(attributeType = "olcServerID", version="2.4.6")
    private List<String> olcServerID = new ArrayList<>();

    /**
     * Field for the 'olcSizeLimit' attribute.
     */
    @ConfigurationElement(attributeType = "olcSizeLimit", version="2.4.0")
    private String olcSizeLimit;

    /**
     * Field for the 'olcSockbufMaxIncoming' attribute.
     */
    @ConfigurationElement(attributeType = "olcSockbufMaxIncoming", version="2.4.0")
    private Integer olcSockbufMaxIncoming;

    /**
     * Field for the 'olcSockbufMaxIncomingAuth' attribute.
     */
    @ConfigurationElement(attributeType = "olcSockbufMaxIncomingAuth", version="2.4.0")
    private String olcSockbufMaxIncomingAuth;

    /**
     * Field for the 'olcTCPBuffer' attribute. (Added in OpenLDAP 2.4.18)
     */
    @ConfigurationElement(attributeType = "olcTCPBuffer", version="2.4.18")
    private List<String> olcTCPBuffer = new ArrayList<>();

    /**
     * Field for the 'olcThreads' attribute
     */
    @ConfigurationElement(attributeType = "olcThreads", version="2.4.0")
    private Integer olcThreads;

    /**
     * Field for the 'olcThreadQueues' attribute.
     */
    @ConfigurationElement(attributeType = "olcThreadQueues", version="2.4.36")
    private Integer olcThreadQueues;

    /**
     * Field for the 'olcTimeLimit' attribute.
     */
    @ConfigurationElement(attributeType = "olcTimeLimit", version="2.4.0")
    private List<String> olcTimeLimit = new ArrayList<>();

    /**
     * Field for the 'olcTLSCACertificateFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSCACertificateFile", version="2.4.0")
    private String olcTLSCACertificateFile;

    /**
     * Field for the 'olcTLSCACertificatePath' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSCACertificatePath", version="2.4.0")
    private String olcTLSCACertificatePath;

    /**
     * Field for the 'olcTLSCertificateFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSCertificateFile", version="2.4.0")
    private String olcTLSCertificateFile;

    /**
     * Field for the 'olcTLSCertificateKeyFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSCertificateKeyFile", version="2.4.0")
    private String olcTLSCertificateKeyFile;

    /**
     * Field for the 'olcTLSCipherSuite' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSCipherSuite", version="2.4.0")
    private String olcTLSCipherSuite;

    /**
     * Field for the 'olcTLSCRLCheck' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSCRLCheck", version="2.4.0")
    private String olcTLSCRLCheck;

    /**
     * Field for the 'olcTLSCRLFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSCRLFile", version="2.4.6")
    private String olcTLSCRLFile;

    /**
     * Field for the 'olcTLSDHParamFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSDHParamFile", version="2.4.0")
    private String olcTLSDHParamFile;

    /**
     * Field for the 'olcTLSECName' attribute. (Added in OpenLDAP 2.4.41)
     */
    @ConfigurationElement(attributeType = "olcTLSECName", version="2.5")
    private String olcTLSECName;

    /**
     * Field for the 'olcTLSProtocolMin' attribute. (Added in OpenLDAP 2.4.37)
     */
    @ConfigurationElement(attributeType = "olcTLSProtocolMin", version="2.4.37")
    private String olcTLSProtocolMin;

    /**
     * Field for the 'olcTLSRandFile' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSRandFile", version="2.4.0")
    private String olcTLSRandFile;

    /**
     * Field for the 'olcTLSVerifyClient' attribute.
     */
    @ConfigurationElement(attributeType = "olcTLSVerifyClient", version="2.4.0")
    private String olcTLSVerifyClient;

    /**
     * Field for the 'olcToolThreads' attribute.
     */
    @ConfigurationElement(attributeType = "olcToolThreads", version="2.4.0")
    private Integer olcToolThreads;

    /**
     * Field for the 'olcWriteTimeout' attribute.
     */
    @ConfigurationElement(attributeType = "olcWriteTimeout", version="2.4.17")
    private Integer olcWriteTimeout;


    // ── addCn — Palpatine Adds a Name to the Global Config Entry ─────────────────
    // Palpatine appends another alias to the global config RDN list. In practice
    // there's almost always just one (the default is "config"), but the schema allows
    // multiple cn values on this entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more common name (cn) values to the global config entry's RDN list.
     * In practice this will almost always be just "config" — but multiple values are allowed.
     *
     * @param strings  the cn values to add
     */
    public void addCn( String... strings )
    {
        for ( String string : strings )
        {
            cn.add( string );
        }
    }


    // ── addOlcAllows — Palpatine Grants Additional LDAP Protocol Allowances ───────
    // Palpatine extends which non-standard LDAP features slapd permits — things like
    // bind_v2 (LDAP v2 binds) or update_anon (anonymous updates). Each call extends
    // the allowances list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more protocol feature allowances to the {@code olcAllows} list.
     * Common values: "bind_v2", "update_anon".
     *
     * @param strings  the allowance strings to add
     */
    public void addOlcAllows( String... strings )
    {
        for ( String string : strings )
        {
            olcAllows.add( string );
        }
    }


    // ── addOlcAttributeOptions — Palpatine Adds Schema Attribute Option Definitions
    // Palpatine extends the server's supported attribute options (e.g., language tags
    // like "lang-en"). Each string defines a supported attribute option pattern.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more attribute option definitions to {@code olcAttributeOptions}.
     *
     * @param strings  the attribute option strings to add
     */
    public void addOlcAttributeOptions( String... strings )
    {
        for ( String string : strings )
        {
            olcAttributeOptions.add( string );
        }
    }


    // ── addOlcAttributeTypes — Palpatine Adds Custom Attribute Type Definitions ───
    // Palpatine extends the server's schema with new attribute type definitions.
    // Each string is an RFC 4512 AttributeTypeDescription.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more attribute type definition strings to {@code olcAttributeTypes}.
     * Each string should be an RFC 4512 AttributeTypeDescription.
     *
     * @param strings  the attribute type definition strings to add
     */
    public void addOlcAttributeTypes( String... strings )
    {
        for ( String string : strings )
        {
            olcAttributeTypes.add( string );
        }
    }


    // ── addOlcAuthIDRewrite — Palpatine Adds Authentication ID Rewrite Rules ──────
    // Palpatine adds a SASL authentication ID rewrite rule — these transform the SASL
    // authentication ID into a DN or other format that slapd can use for ACL checks.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more SASL authentication ID rewrite rules to {@code olcAuthIDRewrite}.
     * These rules (using a sub/regsub syntax) transform SASL auth IDs to DNs.
     *
     * @param strings  the rewrite rule strings to add
     */
    public void addOlcAuthIDRewrite( String... strings )
    {
        for ( String string : strings )
        {
            olcAuthIDRewrite.add( string );
        }
    }


    // ── addOlcAuthzRegexp — Palpatine Adds Authorization Regexp Rules ─────────────
    // Palpatine adds authorization identity mapping rules — regex patterns that map
    // SASL authentication IDs to LDAP DNs for authorization purposes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more authorization identity mapping regexp rules to {@code olcAuthzRegexp}.
     * Each string is a regex pattern and replacement that maps SASL IDs to LDAP DNs.
     *
     * @param strings  the authz regexp strings to add
     */
    public void addOlcAuthzRegexp( String... strings )
    {
        for ( String string : strings )
        {
            olcAuthzRegexp.add( string );
        }
    }


    // ── addOlcDisallows — Palpatine Restricts LDAP Protocol Features ──────────────
    // Palpatine removes access to certain LDAP protocol features globally —
    // for example, "bind_simple_unprotected" to prevent clear-text bind without TLS.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more protocol feature disallowances to the {@code olcDisallows} list.
     * Common values: "bind_simple_unprotected", "bind_anon".
     *
     * @param strings  the disallowance strings to add
     */
    public void addOlcDisallows( String... strings )
    {
        for ( String string : strings )
        {
            olcDisallows.add( string );
        }
    }


    // ── addOlcDitContentRules — Palpatine Extends the DIT Content Rule Schema ─────
    // Palpatine adds custom DIT content rules that constrain which object classes
    // and attributes can appear together in an entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more DIT content rule definition strings to {@code olcDitContentRules}.
     *
     * @param strings  the DIT content rule definition strings to add
     */
    public void addOlcDitContentRules( String... strings )
    {
        for ( String string : strings )
        {
            olcDitContentRules.add( string );
        }
    }


    // ── addOlcLdapSyntaxes — Palpatine Extends the LDAP Syntax Definitions ────────
    // Palpatine adds custom LDAP syntax definitions to the server's schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more LDAP syntax definition strings to {@code olcLdapSyntaxes}.
     *
     * @param strings  the LDAP syntax definition strings to add
     */
    public void addOlcLdapSyntaxes( String... strings )
    {
        for ( String string : strings )
        {
            olcLdapSyntaxes.add( string );
        }
    }


    // ── addOlcLogLevel — Palpatine Adds Logging Levels ───────────────────────────
    // Palpatine increases the verbosity of slapd's logging — each string is either
    // a numeric level or a keyword like "stats", "acl", "sync".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more log level strings to {@code olcLogLevel}.
     * Can be numeric values or keywords like "stats", "acl", "sync", "none".
     *
     * @param strings  the log level strings to add
     */
    public void addOlcLogLevel( String... strings )
    {
        for ( String string : strings )
        {
            olcLogLevel.add( string );
        }
    }


    // ── addOlcObjectClasses — Palpatine Adds Custom Object Class Definitions ──────
    // Palpatine extends the server's schema with new object class definitions.
    // Each string is an RFC 4512 ObjectClassDescription.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more object class definition strings to {@code olcObjectClasses}.
     * Each string should be an RFC 4512 ObjectClassDescription.
     *
     * @param strings  the object class definition strings to add
     */
    public void addOlcObjectClasses( String... strings )
    {
        for ( String string : strings )
        {
            olcObjectClasses.add( string );
        }
    }


    // ── addOlcObjectIdentifier — Palpatine Adds OID Macro Definitions ─────────────
    // Palpatine registers short OID macro names to simplify schema definitions —
    // e.g., "myOrg 1.2.3.4" lets schema definitions use "myOrg" instead of "1.2.3.4".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more OID macro definition strings to {@code olcObjectIdentifier}.
     * Format: "macroName OID" where OID may itself reference a previously defined macro.
     *
     * @param strings  the OID macro definition strings to add
     */
    public void addOlcObjectIdentifier( String... strings )
    {
        for ( String string : strings )
        {
            olcObjectIdentifier.add( string );
        }
    }


    // ── addOlcPasswordHash — Palpatine Mandates Additional Password Hash Schemes ──
    // Palpatine adds password hashing algorithm names that slapd uses when hashing
    // new passwords — e.g., "{SSHA}", "{ARGON2}".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more password hash scheme names to {@code olcPasswordHash}.
     * Common values: "{SSHA}", "{SHA}", "{MD5}", "{ARGON2}".
     *
     * @param strings  the password hash scheme names to add
     */
    public void addOlcPasswordHash( String... strings )
    {
        for ( String string : strings )
        {
            olcPasswordHash.add( string );
        }
    }


    // ── addOlcRequires — Palpatine Mandates Authentication Requirements ────────────
    // Palpatine mandates that certain conditions be met before operations are allowed —
    // for example, "authc" (must be authenticated) or "LDAPv3".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more global requirement strings to {@code olcRequires}.
     * Common values: "authc", "LDAPv3", "strong".
     *
     * @param strings  the requirement strings to add
     */
    public void addOlcRequires( String... strings )
    {
        for ( String string : strings )
        {
            olcRequires.add( string );
        }
    }


    // ── addOlcRestrict — Palpatine Adds Global Operation Restrictions ─────────────
    // Palpatine restricts which LDAP operations are permitted globally — for example,
    // "search" to disallow anonymous searches, or "write" to make the server read-only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more global operation restriction strings to {@code olcRestrict}.
     * Common values: "add", "delete", "modify", "search", "write".
     *
     * @param strings  the restriction strings to add
     */
    public void addOlcRestrict( String... strings )
    {
        for ( String string : strings )
        {
            olcRestrict.add( string );
        }
    }


    // ── addOlcSecurity — Palpatine Adds Security Strength Factor Requirements ─────
    // Palpatine requires that connections meet certain security strength factors
    // (SSFs) before operations are permitted — e.g., "ssf=128 tls=128".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more security strength factor requirements to {@code olcSecurity}.
     * Format: "ssf=N tls=N sasl=N update_ssf=N simple_bind=N".
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


    // ── addOlcServerID — Palpatine Assigns Server IDs for Multi-Master Replication ─
    // Palpatine assigns numeric IDs to each server in a multi-master replication setup.
    // Each string is of the form "N URI" — numeric ID and optionally the server's URI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more server ID strings to {@code olcServerID}.
     * Format: "N" or "N ldap://server.uri/" — required for multi-master replication.
     *
     * @param strings  the server ID strings to add
     */
    public void addOlcServerID( String... strings )
    {
        for ( String string : strings )
        {
            olcServerID.add( string );
        }
    }


    // ── addOlcTCPBuffer — Palpatine Configures TCP Buffer Sizes ──────────────────
    // Palpatine adjusts the kernel-level TCP send and receive buffer sizes for LDAP
    // connections — useful for high-throughput deployments.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more TCP buffer configuration strings to {@code olcTCPBuffer}.
     * Format: "read=N write=N listener=URI" (each component is optional).
     *
     * @param strings  the TCP buffer strings to add
     */
    public void addOlcTCPBuffer( String... strings )
    {
        for ( String string : strings )
        {
            olcTCPBuffer.add( string );
        }
    }


    // ── addOlcTimeLimit — Palpatine Adds Time Limit Directives ───────────────────
    // Palpatine sets maximum time limits for LDAP operations — either globally or
    // per-user. Format: "time[.soft]=N time[.hard]=N".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more time limit strings to {@code olcTimeLimit}.
     * Format: "time=N" or "time.soft=N time.hard=N" (global or per-DN limits).
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


    // ── clearCn — Palpatine Clears the Global Config Name List ───────────────────
    // Palpatine wipes the cn list — rarely needed since cn is the RDN and must remain.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all values from the {@code cn} list.
     * Use with caution — {@code cn} is the RDN for this entry.
     */
    public void clearCn()
    {
        cn.clear();
    }


    // ── clearOlcAllows — Palpatine Revokes All LDAP Protocol Allowances ───────────
    /**
     * Clears all values from the {@code olcAllows} list.
     */
    public void clearOlcAllows()
    {
        olcAllows.clear();
    }


    // ── clearOlcAttributeOptions — Palpatine Clears Attribute Option Definitions ──
    /**
     * Clears all values from the {@code olcAttributeOptions} list.
     */
    public void clearOlcAttributeOptions()
    {
        olcAttributeOptions.clear();
    }


    // ── clearOlcAttributeTypes — Palpatine Clears Custom Attribute Type Definitions
    /**
     * Clears all values from the {@code olcAttributeTypes} list.
     */
    public void clearOlcAttributeTypes()
    {
        olcAttributeTypes.clear();
    }


    // ── clearOlcAuthIDRewrite — Palpatine Clears Auth ID Rewrite Rules ────────────
    /**
     * Clears all values from the {@code olcAuthIDRewrite} list.
     */
    public void clearOlcAuthIDRewrite()
    {
        olcAuthIDRewrite.clear();
    }


    // ── clearOlcAuthzRegexp — Palpatine Clears Authorization Regexp Rules ─────────
    /**
     * Clears all values from the {@code olcAuthzRegexp} list.
     */
    public void clearOlcAuthzRegexp()
    {
        olcAuthzRegexp.clear();
    }


    // ── clearOlcDisallows — Palpatine Clears All Protocol Disallowances ───────────
    /**
     * Clears all values from the {@code olcDisallows} list.
     */
    public void clearOlcDisallows()
    {
        olcDisallows.clear();
    }


    // ── clearOlcDitContentRules — Palpatine Clears DIT Content Rules ──────────────
    /**
     * Clears all values from the {@code olcDitContentRules} list.
     */
    public void clearOlcDitContentRules()
    {
        olcDitContentRules.clear();
    }


    // ── clearOlcLdapSyntaxes — Palpatine Clears LDAP Syntax Definitions ──────────
    /**
     * Clears all values from the {@code olcLdapSyntaxes} list.
     */
    public void clearOlcLdapSyntaxes()
    {
        olcLdapSyntaxes.clear();
    }


    // ── clearOlcLogLevel — Palpatine Silences All Logging ────────────────────────
    /**
     * Clears all values from the {@code olcLogLevel} list, effectively silencing logging.
     */
    public void clearOlcLogLevel()
    {
        olcLogLevel.clear();
    }


    // ── clearOlcObjectClasses — Palpatine Clears Custom Object Class Definitions ──
    /**
     * Clears all values from the {@code olcObjectClasses} list.
     */
    public void clearOlcObjectClasses()
    {
        olcObjectClasses.clear();
    }


    // ── clearOlcObjectIdentifier — Palpatine Clears OID Macro Definitions ─────────
    /**
     * Clears all values from the {@code olcObjectIdentifier} list.
     */
    public void clearOlcObjectIdentifier()
    {
        olcObjectIdentifier.clear();
    }


    // ── clearOlcPasswordHash — Palpatine Clears Password Hash Scheme List ─────────
    /**
     * Clears all values from the {@code olcPasswordHash} list.
     */
    public void clearOlcPasswordHash()
    {
        olcPasswordHash.clear();
    }


    // ── clearOlcRequires — Palpatine Revokes All Global Requirements ──────────────
    /**
     * Clears all values from the {@code olcRequires} list.
     */
    public void clearOlcRequires()
    {
        olcRequires.clear();
    }


    // ── clearOlcRestrict — Palpatine Lifts All Global Restrictions ───────────────
    /**
     * Clears all values from the {@code olcRestrict} list.
     */
    public void clearOlcRestrict()
    {
        olcRestrict.clear();
    }


    // ── clearOlcSecurity — Palpatine Clears All Security Requirements ─────────────
    /**
     * Clears all values from the {@code olcSecurity} list.
     */
    public void clearOlcSecurity()
    {
        olcSecurity.clear();
    }


    // ── clearOlcServerID — Palpatine Clears Server ID Assignments ────────────────
    /**
     * Clears all values from the {@code olcServerID} list.
     */
    public void clearOlcServerID()
    {
        olcServerID.clear();
    }


    // ── clearOlcTCPBuffer — Palpatine Clears TCP Buffer Configurations ────────────
    /**
     * Clears all values from the {@code olcTCPBuffer} list.
     */
    public void clearOlcTCPBuffer()
    {
        olcTCPBuffer.clear();
    }


    // ── clearOlcTimeLimit — Palpatine Clears All Time Limit Directives ────────────
    /**
     * Clears all values from the {@code olcTimeLimit} list.
     */
    public void clearOlcTimeLimit()
    {
        olcTimeLimit.clear();
    }


    // ── getCn — Palpatine Reads the Global Config Entry Name ─────────────────────
    // Palpatine reads the canonical name of the global config entry — almost always
    // just ["config"], the RDN of the cn=config entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the {@code cn} list — the RDN values for this entry.
     * Normally contains just "config".
     *
     * @return  a copy of the cn list; never null
     */
    public List<String> getCn()
    {
        return copyListString( cn );
    }


    // ── getOlcAllows — Palpatine Reads Protocol Allowances ───────────────────────
    /**
     * Returns a defensive copy of the {@code olcAllows} list.
     *
     * @return  a copy of the olcAllows list; never null
     */
    public List<String> getOlcAllows()
    {
        return copyListString( olcAllows );
    }


    // ── getOlcArgsFile — Palpatine Reads the slapd Arguments File Path ────────────
    // Palpatine reads the path to the file where slapd records its command-line
    // arguments at startup.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the path to the slapd arguments file.
     * slapd writes its startup arguments to this file when it starts.
     *
     * @return  the args file path, or null if not set
     */
    public String getOlcArgsFile()
    {
        return olcArgsFile;
    }


    // ── getOlcAttributeOptions — Palpatine Reads Attribute Option Definitions ─────
    /**
     * Returns a defensive copy of the {@code olcAttributeOptions} list.
     *
     * @return  a copy of the olcAttributeOptions list; never null
     */
    public List<String> getOlcAttributeOptions()
    {
        return copyListString( olcAttributeOptions );
    }


    // ── getOlcAttributeTypes — Palpatine Reads Custom Attribute Type Definitions ──
    /**
     * Returns a defensive copy of the {@code olcAttributeTypes} list.
     *
     * @return  a copy of the olcAttributeTypes list; never null
     */
    public List<String> getOlcAttributeTypes()
    {
        return copyListString( olcAttributeTypes );
    }


    // ── getOlcAuthIDRewrite — Palpatine Reads Auth ID Rewrite Rules ───────────────
    /**
     * Returns a defensive copy of the {@code olcAuthIDRewrite} list.
     *
     * @return  a copy of the olcAuthIDRewrite list; never null
     */
    public List<String> getOlcAuthIDRewrite()
    {
        return copyListString( olcAuthIDRewrite );
    }


    // ── getOlcAuthzPolicy — Palpatine Reads the Authorization Policy ──────────────
    // Palpatine reads the authz policy — controls who is allowed to authorize as whom
    // (e.g., "to" or "any"). This is the global policy for olcAuthzRegexp rules.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the authorization policy string.
     * Controls how SASL authorization identity mapping rules are applied.
     * Common values: "none", "any", "self", "from", "to".
     *
     * @return  the authz policy string, or null if not set
     */
    public String getOlcAuthzPolicy()
    {
        return olcAuthzPolicy;
    }


    // ── getOlcAuthzRegexp — Palpatine Reads Authorization Regexp Rules ────────────
    /**
     * Returns a defensive copy of the {@code olcAuthzRegexp} list.
     *
     * @return  a copy of the olcAuthzRegexp list; never null
     */
    public List<String> getOlcAuthzRegexp()
    {
        return copyListString( olcAuthzRegexp );
    }


    // ── getOlcConcurrency — Palpatine Reads the Thread Concurrency Target ─────────
    // Palpatine reads the desired level of thread concurrency — a hint to the OS
    // thread scheduler about how many threads should run in parallel.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the thread concurrency hint for the operating system scheduler.
     * On most modern systems this has little effect but may help on some platforms.
     *
     * @return  the concurrency value, or null if not set
     */
    public Integer getOlcConcurrency()
    {
        return olcConcurrency;
    }


    // ── getOlcConfigDir — Palpatine Reads the Configuration Directory Path ────────
    /**
     * Returns the path to the slapd.d configuration directory.
     *
     * @return  the config directory path, or null if not set
     */
    public String getOlcConfigDir()
    {
        return olcConfigDir;
    }


    // ── getOlcConfigFile — Palpatine Reads the Configuration File Path ─────────────
    /**
     * Returns the path to the slapd.conf configuration file (legacy format).
     *
     * @return  the config file path, or null if not set
     */
    public String getOlcConfigFile()
    {
        return olcConfigFile;
    }


    // ── getOlcConnMaxPending — Palpatine Reads Max Pending Anonymous Connections ───
    // Palpatine reads the maximum number of pending connections allowed from
    // unauthenticated (anonymous) clients before slapd starts rejecting new ones.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of pending operations from anonymous connections.
     *
     * @return  the max pending anonymous connection count, or null if not set
     */
    public Integer getOlcConnMaxPending()
    {
        return olcConnMaxPending;
    }


    // ── getOlcConnMaxPendingAuth — Palpatine Reads Max Pending Authenticated Connections
    /**
     * Returns the maximum number of pending operations from authenticated connections.
     *
     * @return  the max pending authenticated connection count, or null if not set
     */
    public Integer getOlcConnMaxPendingAuth()
    {
        return olcConnMaxPendingAuth;
    }


    // ── getOlcDisallows — Palpatine Reads Protocol Disallowances ─────────────────
    /**
     * Returns a defensive copy of the {@code olcDisallows} list.
     *
     * @return  a copy of the olcDisallows list; never null
     */
    public List<String> getOlcDisallows()
    {
        return copyListString( olcDisallows );
    }


    // ── getOlcDitContentRules — Palpatine Reads DIT Content Rules ────────────────
    /**
     * Returns a defensive copy of the {@code olcDitContentRules} list.
     *
     * @return  a copy of the olcDitContentRules list; never null
     */
    public List<String> getOlcDitContentRules()
    {
        return copyListString( olcDitContentRules );
    }


    // ── getOlcGentleHUP — Palpatine Reads the Gentle HUP Setting ─────────────────
    // Palpatine reads whether SIGHUP causes a graceful restart (draining existing
    // connections) rather than an abrupt reload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether a SIGHUP causes a graceful restart (draining connections first).
     *
     * @return  true if gentle SIGHUP is enabled; false if not; null if not set
     */
    public Boolean getOlcGentleHUP()
    {
        return olcGentleHUP;
    }


    // ── getOlcIdleTimeout — Palpatine Reads the Connection Idle Timeout ───────────
    /**
     * Returns the idle timeout in seconds for connections.
     * slapd closes connections that have been idle for this long.
     *
     * @return  the idle timeout in seconds, or null if not set
     */
    public Integer getOlcIdleTimeout()
    {
        return olcIdleTimeout;
    }


    // ── getOlcIndexHash64 — Palpatine Reads the 64-bit Index Hash Setting ─────────
    /**
     * Returns whether 64-bit hashes are used for index keys (improves performance
     * on 64-bit platforms by reducing hash collisions).
     *
     * @return  true if 64-bit index hashing is enabled; false if not; null if not set
     */
    public Boolean getOlcIndexHash64()
    {
        return olcIndexHash64;
    }


    // ── getOlcIndexIntLen — Palpatine Reads the Integer Index Key Length ──────────
    /**
     * Returns the number of significant bytes used for integer index keys.
     *
     * @return  the integer index key length, or null if not set
     */
    public Integer getOlcIndexIntLen()
    {
        return olcIndexIntLen;
    }


    // ── getOlcIndexSubstrAnyLen — Palpatine Reads Substring Any-Index Length ──────
    /**
     * Returns the length of substrings used for "any" (mid-string) substring index keys.
     *
     * @return  the substring any-index key length, or null if not set
     */
    public Integer getOlcIndexSubstrAnyLen()
    {
        return olcIndexSubstrAnyLen;
    }


    // ── getOlcIndexSubstrAnyStep — Palpatine Reads Substring Any-Index Step ───────
    /**
     * Returns the step size used when generating "any" substring index keys.
     *
     * @return  the substring any-index step size, or null if not set
     */
    public Integer getOlcIndexSubstrAnyStep()
    {
        return olcIndexSubstrAnyStep;
    }


    // ── getOlcIndexSubstrIfMaxLen — Palpatine Reads Substring Final-Index Max Length
    /**
     * Returns the maximum length for "final" (suffix) substring index keys.
     *
     * @return  the substring final-index max length, or null if not set
     */
    public Integer getOlcIndexSubstrIfMaxLen()
    {
        return olcIndexSubstrIfMaxLen;
    }


    // ── getOlcIndexSubstrIfMinLen — Palpatine Reads Substring Final-Index Min Length
    /**
     * Returns the minimum length for "final" (suffix) substring index keys.
     *
     * @return  the substring final-index min length, or null if not set
     */
    public Integer getOlcIndexSubstrIfMinLen()
    {
        return olcIndexSubstrIfMinLen;
    }


    // ── getOlcLdapSyntaxes — Palpatine Reads LDAP Syntax Definitions ─────────────
    /**
     * Returns a defensive copy of the {@code olcLdapSyntaxes} list.
     *
     * @return  a copy of the olcLdapSyntaxes list; never null
     */
    public List<String> getOlcLdapSyntaxes()
    {
        return copyListString( olcLdapSyntaxes );
    }


    // ── getOlcListenerThreads — Palpatine Reads the Listener Thread Count ─────────
    /**
     * Returns the number of threads dedicated to accepting new LDAP connections.
     *
     * @return  the listener thread count, or null if not set
     */
    public Integer getOlcListenerThreads()
    {
        return olcListenerThreads;
    }


    // ── getOlcLocalSSF — Palpatine Reads the Local Security Strength Factor ───────
    /**
     * Returns the security strength factor (SSF) for local LDAP connections
     * (i.e., connections over a Unix domain socket, which are implicitly trusted).
     *
     * @return  the local SSF value, or null if not set
     */
    public Integer getOlcLocalSSF()
    {
        return olcLocalSSF;
    }


    // ── getOlcLogFile — Palpatine Reads the Log File Path ────────────────────────
    /**
     * Returns the path to the slapd log file (alternative to syslog).
     *
     * @return  the log file path, or null if not set
     */
    public String getOlcLogFile()
    {
        return olcLogFile;
    }


    // ── getOlcLogLevel — Palpatine Reads the Active Log Levels ───────────────────
    /**
     * Returns a defensive copy of the {@code olcLogLevel} list.
     *
     * @return  a copy of the olcLogLevel list; never null
     */
    public List<String> getOlcLogLevel()
    {
        return copyListString( olcLogLevel );
    }


    // ── getOlcObjectClasses — Palpatine Reads Custom Object Class Definitions ─────
    /**
     * Returns a defensive copy of the {@code olcObjectClasses} list.
     *
     * @return  a copy of the olcObjectClasses list; never null
     */
    public List<String> getOlcObjectClasses()
    {
        return copyListString( olcObjectClasses );
    }


    // ── getOlcObjectIdentifier — Palpatine Reads OID Macro Definitions ────────────
    /**
     * Returns a defensive copy of the {@code olcObjectIdentifier} list.
     *
     * @return  a copy of the olcObjectIdentifier list; never null
     */
    public List<String> getOlcObjectIdentifier()
    {
        return copyListString( olcObjectIdentifier );
    }


    // ── getOlcPasswordCryptSaltFormat — Palpatine Reads the Password Crypt Salt Format
    /**
     * Returns the format string used when generating crypt password salt.
     * Follows the format accepted by crypt(3).
     *
     * @return  the crypt salt format string, or null if not set
     */
    public String getOlcPasswordCryptSaltFormat()
    {
        return olcPasswordCryptSaltFormat;
    }


    // ── getOlcPasswordHash — Palpatine Reads the Password Hash Scheme List ────────
    /**
     * Returns a defensive copy of the {@code olcPasswordHash} list.
     *
     * @return  a copy of the olcPasswordHash list; never null
     */
    public List<String> getOlcPasswordHash()
    {
        return copyListString( olcPasswordHash );
    }


    // ── getOlcPidFile — Palpatine Reads the PID File Path ────────────────────────
    /**
     * Returns the path to the file where slapd records its process ID (PID) at startup.
     *
     * @return  the PID file path, or null if not set
     */
    public String getOlcPidFile()
    {
        return olcPidFile;
    }


    // ── getOlcPluginLogFile — Palpatine Reads the Plugin Log File Path ────────────
    /**
     * Returns the path to the plugin-specific log file.
     *
     * @return  the plugin log file path, or null if not set
     */
    public String getOlcPluginLogFile()
    {
        return olcPluginLogFile;
    }


    // ── getOlcReadOnly — Palpatine Reads the Global Read-Only Setting ─────────────
    /**
     * Returns whether the entire server is in global read-only mode.
     * When true, all write operations (add, delete, modify) are rejected.
     *
     * @return  true if global read-only mode is on; false if not; null if not set
     */
    public Boolean getOlcReadOnly()
    {
        return olcReadOnly;
    }


    // ── getOlcReferral — Palpatine Reads the Global Referral URI ─────────────────
    /**
     * Returns the global referral URI. When set, slapd returns this referral for
     * requests that it cannot service itself (e.g., when it is a slave/consumer).
     *
     * @return  the global referral URI string, or null if not set
     */
    public String getOlcReferral()
    {
        return olcReferral;
    }


    // ── getOlcReplogFile — Palpatine Reads the Replication Log File Path ──────────
    /**
     * Returns the path to the replication log file (used by slurpd — legacy).
     *
     * @return  the replog file path, or null if not set
     */
    public String getOlcReplogFile()
    {
        return olcReplogFile;
    }


    // ── getOlcRequires — Palpatine Reads Global Requirements ─────────────────────
    /**
     * Returns a defensive copy of the {@code olcRequires} list.
     *
     * @return  a copy of the olcRequires list; never null
     */
    public List<String> getOlcRequires()
    {
        return copyListString( olcRequires );
    }


    // ── getOlcReverseLookup — Palpatine Reads the Reverse DNS Lookup Setting ──────
    /**
     * Returns whether slapd performs reverse DNS lookups on client IP addresses
     * to populate the connection's hostname field.
     *
     * @return  true if reverse lookups are performed; false if not; null if not set
     */
    public Boolean getOlcReverseLookup()
    {
        return olcReverseLookup;
    }


    // ── getOlcRestrict — Palpatine Reads Global Operation Restrictions ────────────
    /**
     * Returns a defensive copy of the {@code olcRestrict} list.
     *
     * @return  a copy of the olcRestrict list; never null
     */
    public List<String> getOlcRestrict()
    {
        return copyListString( olcRestrict );
    }


    // ── getOlcRootDSE — Palpatine Reads Root DSE Extra Attributes ────────────────
    // Palpatine reads the list of LDIF files that provide additional attributes for
    // the root DSE entry (the special "" DN that describes the server's capabilities).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the {@code olcRootDSE} list.
     * Each string is a path to an LDIF file providing additional root DSE attributes.
     *
     * @return  a copy of the olcRootDSE list; may be null if never set
     */
    public List<String> getOlcRootDSE()
    {
        return copyListString( olcRootDSE );
    }


    // ── getOlcSaslAuxprops — Palpatine Reads the SASL Auxprops Plugin List ────────
    /**
     * Returns the SASL auxprops plugin name(s) used for auxiliary property lookup.
     *
     * @return  the SASL auxprops string, or null if not set
     */
    public String getOlcSaslAuxprops()
    {
        return olcSaslAuxprops;
    }


    // ── getOlcSaslAuxpropsDontUseCopy — Palpatine Reads the Don't-Use-Copy Setting ─
    /**
     * Returns the attribute type(s) for which SASL auxprops must not use a cached copy.
     *
     * @return  the dont-use-copy attribute type list string, or null if not set
     */
    public String getOlcSaslAuxpropsDontUseCopy()
    {
        return olcSaslAuxpropsDontUseCopy;
    }


    // ── getOlcSaslAuxpropsDontUseCopyIgnore — Palpatine Reads the Ignore Flag ─────
    /**
     * Returns whether SASL auxprops failures on dont-use-copy attributes are ignored.
     *
     * @return  true if errors are ignored; false if not; null if not set
     */
    public Boolean getOlcSaslAuxpropsDontUseCopyIgnore()
    {
        return olcSaslAuxpropsDontUseCopyIgnore;
    }


    // ── getOlcSaslHost — Palpatine Reads the SASL Host Name ──────────────────────
    /**
     * Returns the FQDN used as the SASL service host name.
     * This is used in SASL GSSAPI (Kerberos) exchanges.
     *
     * @return  the SASL host name, or null if not set
     */
    public String getOlcSaslHost()
    {
        return olcSaslHost;
    }


    // ── getOlcSaslRealm — Palpatine Reads the SASL Realm ─────────────────────────
    /**
     * Returns the SASL realm name used for SASL authentication.
     *
     * @return  the SASL realm string, or null if not set
     */
    public String getOlcSaslRealm()
    {
        return olcSaslRealm;
    }


    // ── getOlcSaslSecProps — Palpatine Reads the SASL Security Properties ─────────
    /**
     * Returns the SASL security properties string.
     * Controls minimum/maximum SSF, maximum buffer size, and other SASL options.
     *
     * @return  the SASL security properties string, or null if not set
     */
    public String getOlcSaslSecProps()
    {
        return olcSaslSecProps;
    }


    // ── getOlcSecurity — Palpatine Reads the Security Strength Factor Requirements ─
    /**
     * Returns a defensive copy of the {@code olcSecurity} list.
     *
     * @return  a copy of the olcSecurity list; never null
     */
    public List<String> getOlcSecurity()
    {
        return copyListString( olcSecurity );
    }


    // ── getOlcServerID — Palpatine Reads the Server ID List ──────────────────────
    /**
     * Returns a defensive copy of the {@code olcServerID} list.
     *
     * @return  a copy of the olcServerID list; never null
     */
    public List<String> getOlcServerID()
    {
        return copyListString( olcServerID );
    }


    // ── getOlcSizeLimit — Palpatine Reads the Global Search Size Limit ────────────
    /**
     * Returns the global default search result size limit.
     * Format: "size=N" or "size.soft=N size.hard=N".
     *
     * @return  the size limit string, or null if not set
     */
    public String getOlcSizeLimit()
    {
        return olcSizeLimit;
    }


    // ── getOlcSockbufMaxIncoming — Palpatine Reads the Max Incoming Socket Buffer Size
    /**
     * Returns the maximum size of the incoming socket buffer for unauthenticated connections.
     *
     * @return  the max incoming socket buffer size, or null if not set
     */
    public Integer getOlcSockbufMaxIncoming()
    {
        return olcSockbufMaxIncoming;
    }


    // ── getOlcSockbufMaxIncomingAuth — Palpatine Reads the Authenticated Buffer Size
    /**
     * Returns the maximum incoming socket buffer size for authenticated connections.
     *
     * @return  the max authenticated incoming socket buffer size string, or null if not set
     */
    public String getOlcSockbufMaxIncomingAuth()
    {
        return olcSockbufMaxIncomingAuth;
    }


    // ── getOlcTCPBuffer — Palpatine Reads the TCP Buffer Configuration ────────────
    /**
     * Returns a defensive copy of the {@code olcTCPBuffer} list.
     *
     * @return  a copy of the olcTCPBuffer list; never null
     */
    public List<String> getOlcTCPBuffer()
    {
        return copyListString( olcTCPBuffer );
    }


    // ── getOlcThreads — Palpatine Reads the Worker Thread Pool Size ───────────────
    /**
     * Returns the number of worker threads in slapd's thread pool.
     * More threads handle more concurrent requests, but use more memory.
     *
     * @return  the thread pool size, or null if not set
     */
    public Integer getOlcThreads()
    {
        return olcThreads;
    }


    // ── getOlcThreadQueues — Palpatine Reads the Thread Queue Count ───────────────
    /**
     * Returns the number of task queues used by the thread pool.
     * Multiple queues reduce lock contention on multi-core systems.
     *
     * @return  the thread queue count, or null if not set
     */
    public Integer getOlcThreadQueues()
    {
        return olcThreadQueues;
    }


    // ── getOlcTimeLimit — Palpatine Reads the Global Time Limit List ─────────────
    /**
     * Returns a defensive copy of the {@code olcTimeLimit} list.
     *
     * @return  a copy of the olcTimeLimit list; never null
     */
    public List<String> getOlcTimeLimit()
    {
        return copyListString( olcTimeLimit );
    }


    // ── getOlcTLSCACertificateFile — Palpatine Reads the TLS CA Certificate File ──
    /**
     * Returns the path to the TLS CA certificate file (PEM format).
     *
     * @return  the TLS CA certificate file path, or null if not set
     */
    public String getOlcTLSCACertificateFile()
    {
        return olcTLSCACertificateFile;
    }


    // ── getOlcTLSCACertificatePath — Palpatine Reads the TLS CA Certificate Directory
    /**
     * Returns the path to the directory containing TLS CA certificates (OpenSSL hash format).
     *
     * @return  the TLS CA certificate directory path, or null if not set
     */
    public String getOlcTLSCACertificatePath()
    {
        return olcTLSCACertificatePath;
    }


    // ── getOlcTLSCertificateFile — Palpatine Reads the TLS Server Certificate File ─
    /**
     * Returns the path to the server's TLS certificate file (PEM format).
     *
     * @return  the TLS certificate file path, or null if not set
     */
    public String getOlcTLSCertificateFile()
    {
        return olcTLSCertificateFile;
    }


    // ── getOlcTLSCertificateKeyFile — Palpatine Reads the TLS Private Key File ─────
    /**
     * Returns the path to the server's TLS private key file (PEM format).
     *
     * @return  the TLS private key file path, or null if not set
     */
    public String getOlcTLSCertificateKeyFile()
    {
        return olcTLSCertificateKeyFile;
    }


    // ── getOlcTLSCipherSuite — Palpatine Reads the TLS Cipher Suite ───────────────
    /**
     * Returns the TLS cipher suite string (OpenSSL format).
     *
     * @return  the cipher suite string, or null if not set
     */
    public String getOlcTLSCipherSuite()
    {
        return olcTLSCipherSuite;
    }


    // ── getOlcTLSCRLCheck — Palpatine Reads the TLS CRL Check Mode ───────────────
    /**
     * Returns the TLS Certificate Revocation List check mode.
     * Values: "none", "peer", "all".
     *
     * @return  the CRL check mode string, or null if not set
     */
    public String getOlcTLSCRLCheck()
    {
        return olcTLSCRLCheck;
    }


    // ── getOlcTLSCRLFile — Palpatine Reads the TLS CRL File Path ─────────────────
    /**
     * Returns the path to the TLS Certificate Revocation List (CRL) file.
     *
     * @return  the CRL file path, or null if not set
     */
    public String getOlcTLSCRLFile()
    {
        return olcTLSCRLFile;
    }


    // ── getOlcTLSECName — Palpatine Reads the TLS Elliptic Curve Name ─────────────
    /**
     * Returns the name of the elliptic curve used for TLS ECDHE key exchange.
     *
     * @return  the elliptic curve name, or null if not set
     */
    public String getOlcTLSECName()
    {
        return olcTLSECName;
    }


    // ── getOlcTLSDHParamFile — Palpatine Reads the TLS DH Parameters File ─────────
    /**
     * Returns the path to the Diffie-Hellman parameters file for TLS (PEM format).
     *
     * @return  the DH parameters file path, or null if not set
     */
    public String getOlcTLSDHParamFile()
    {
        return olcTLSDHParamFile;
    }


    // ── getOlcTLSProtocolMin — Palpatine Reads the Minimum TLS Protocol Version ───
    /**
     * Returns the minimum TLS protocol version slapd will accept.
     * Format: "3.0" (SSLv3), "3.1" (TLS 1.0), "3.2" (TLS 1.1), "3.3" (TLS 1.2).
     *
     * @return  the minimum TLS protocol version string, or null if not set
     */
    public String getOlcTLSProtocolMin()
    {
        return olcTLSProtocolMin;
    }


    // ── getOlcTLSRandFile — Palpatine Reads the TLS Random Seed File ─────────────
    /**
     * Returns the path to the file used to seed the TLS random number generator.
     *
     * @return  the TLS rand file path, or null if not set
     */
    public String getOlcTLSRandFile()
    {
        return olcTLSRandFile;
    }


    // ── getOlcTLSVerifyClient — Palpatine Reads the TLS Client Verification Mode ──
    /**
     * Returns the TLS client certificate verification mode.
     * Values: "never", "allow", "try", "demand", "hard".
     *
     * @return  the client verification mode string, or null if not set
     */
    public String getOlcTLSVerifyClient()
    {
        return olcTLSVerifyClient;
    }


    // ── getOlcToolThreads — Palpatine Reads the Tool Thread Count ────────────────
    /**
     * Returns the number of threads used by slapd command-line tools (slapindex, etc.).
     *
     * @return  the tool thread count, or null if not set
     */
    public Integer getOlcToolThreads()
    {
        return olcToolThreads;
    }


    // ── getOlcWriteTimeout — Palpatine Reads the Write Operation Timeout ──────────
    /**
     * Returns the timeout in seconds for write operations.
     * If a write operation takes longer than this, the connection is closed.
     *
     * @return  the write timeout in seconds, or null if not set
     */
    public Integer getOlcWriteTimeout()
    {
        return olcWriteTimeout;
    }


    // ── setCn — Palpatine Sets the Global Config Entry Name List ─────────────────
    /**
     * Replaces the {@code cn} list with a defensive copy of the given list.
     *
     * @param cn  the new cn list to set
     */
    public void setCn( List<String> cn )
    {
        this.cn = copyListString( cn );
    }


    // ── setOlcAllows — Palpatine Sets the Protocol Allowances List ───────────────
    /**
     * Replaces the {@code olcAllows} list with a defensive copy of the given list.
     *
     * @param olcAllows  the new allows list to set
     */
    public void setOlcAllows( List<String> olcAllows )
    {
        this.olcAllows = copyListString( olcAllows );
    }


    // ── setOlcArgsFile — Palpatine Sets the slapd Arguments File Path ─────────────
    /**
     * Sets the path to the slapd arguments file.
     *
     * @param olcArgsFile  the args file path to set
     */
    public void setOlcArgsFile( String olcArgsFile )
    {
        this.olcArgsFile = olcArgsFile;
    }


    // ── setOlcAttributeOptions — Palpatine Sets Attribute Option Definitions ──────
    /**
     * Replaces the {@code olcAttributeOptions} list with a defensive copy.
     *
     * @param olcAttributeOptions  the new attribute options list to set
     */
    public void setOlcAttributeOptions( List<String> olcAttributeOptions )
    {
        this.olcAttributeOptions = copyListString( olcAttributeOptions );
    }


    // ── setOlcAttributeTypes — Palpatine Sets Custom Attribute Type Definitions ───
    /**
     * Replaces the {@code olcAttributeTypes} list with a defensive copy.
     *
     * @param olcAttributeTypes  the new attribute type definitions list to set
     */
    public void setOlcAttributeTypes( List<String> olcAttributeTypes )
    {
        this.olcAttributeTypes = copyListString( olcAttributeTypes );
    }


    // ── setOlcAuthIDRewrite — Palpatine Sets Auth ID Rewrite Rules ───────────────
    /**
     * Replaces the {@code olcAuthIDRewrite} list with a defensive copy.
     *
     * @param olcAuthIDRewrite  the new auth ID rewrite rules to set
     */
    public void setOlcAuthIDRewrite( List<String> olcAuthIDRewrite )
    {
        this.olcAuthIDRewrite = copyListString( olcAuthIDRewrite );
    }


    // ── setOlcAuthzPolicy — Palpatine Sets the Authorization Policy ───────────────
    /**
     * Sets the global authorization policy string.
     *
     * @param olcAuthzPolicy  the authz policy to set
     */
    public void setOlcAuthzPolicy( String olcAuthzPolicy )
    {
        this.olcAuthzPolicy = olcAuthzPolicy;
    }


    // ── setOlcAuthzRegexp — Palpatine Sets the Authorization Regexp Rules ──────────
    /**
     * Replaces the {@code olcAuthzRegexp} list with a defensive copy.
     *
     * @param olcAuthzRegexp  the new authz regexp rules to set
     */
    public void setOlcAuthzRegexp( List<String> olcAuthzRegexp )
    {
        this.olcAuthzRegexp = copyListString( olcAuthzRegexp );
    }


    // ── setOlcConcurrency — Palpatine Sets the Thread Concurrency Target ──────────
    /**
     * Sets the thread concurrency hint for the operating system scheduler.
     *
     * @param olcConcurrency  the concurrency value to set
     */
    public void setOlcConcurrency( Integer olcConcurrency )
    {
        this.olcConcurrency = olcConcurrency;
    }


    // ── setOlcConfigDir — Palpatine Sets the Configuration Directory Path ─────────
    /**
     * Sets the path to the slapd.d configuration directory.
     *
     * @param olcConfigDir  the config directory path to set
     */
    public void setOlcConfigDir( String olcConfigDir )
    {
        this.olcConfigDir = olcConfigDir;
    }


    // ── setOlcConfigFile — Palpatine Sets the Configuration File Path ─────────────
    /**
     * Sets the path to the slapd.conf configuration file (legacy format).
     *
     * @param olcConfigFile  the config file path to set
     */
    public void setOlcConfigFile( String olcConfigFile )
    {
        this.olcConfigFile = olcConfigFile;
    }


    // ── setOlcConnMaxPending — Palpatine Sets Max Pending Anonymous Connections ────
    /**
     * Sets the maximum number of pending operations from anonymous connections.
     *
     * @param olcConnMaxPending  the max pending anonymous count to set
     */
    public void setOlcConnMaxPending( Integer olcConnMaxPending )
    {
        this.olcConnMaxPending = olcConnMaxPending;
    }


    // ── setOlcConnMaxPendingAuth — Palpatine Sets Max Pending Authenticated Connections
    /**
     * Sets the maximum number of pending operations from authenticated connections.
     *
     * @param olcConnMaxPendingAuth  the max pending authenticated count to set
     */
    public void setOlcConnMaxPendingAuth( Integer olcConnMaxPendingAuth )
    {
        this.olcConnMaxPendingAuth = olcConnMaxPendingAuth;
    }


    // ── setOlcDisallows — Palpatine Sets the Protocol Disallowances List ──────────
    /**
     * Replaces the {@code olcDisallows} list with a defensive copy.
     *
     * @param olcDisallows  the new disallows list to set
     */
    public void setOlcDisallows( List<String> olcDisallows )
    {
        this.olcDisallows = copyListString( olcDisallows );
    }


    // ── setOlcDitContentRules — Palpatine Sets DIT Content Rule Definitions ───────
    /**
     * Replaces the {@code olcDitContentRules} list with a defensive copy.
     *
     * @param olcDitContentRules  the new DIT content rules to set
     */
    public void setOlcDitContentRules( List<String> olcDitContentRules )
    {
        this.olcDitContentRules = copyListString( olcDitContentRules );
    }


    // ── setOlcGentleHUP — Palpatine Sets Gentle SIGHUP Behavior ──────────────────
    /**
     * Sets whether SIGHUP causes a graceful restart (draining existing connections).
     *
     * @param olcGentleHUP  true for gentle HUP; false for immediate reload
     */
    public void setOlcGentleHUP( Boolean olcGentleHUP )
    {
        this.olcGentleHUP = olcGentleHUP;
    }


    // ── setOlcIdleTimeout — Palpatine Sets the Connection Idle Timeout ────────────
    /**
     * Sets the idle timeout in seconds for connections.
     *
     * @param olcIdleTimeout  the idle timeout in seconds to set
     */
    public void setOlcIdleTimeout( Integer olcIdleTimeout )
    {
        this.olcIdleTimeout = olcIdleTimeout;
    }


    // ── setOlcIndexHash64 — Palpatine Sets the 64-bit Index Hash Flag ─────────────
    /**
     * Sets whether 64-bit hashes are used for index keys.
     *
     * @param olcIndexHash64  true to enable 64-bit index hashing
     */
    public void setOlcIndexHash64( Boolean olcIndexHash64 )
    {
        this.olcIndexHash64 = olcIndexHash64;
    }


    // ── setOlcIndexIntLen — Palpatine Sets the Integer Index Key Length ────────────
    /**
     * Sets the number of significant bytes used for integer index keys.
     *
     * @param olcIndexIntLen  the integer index key length to set
     */
    public void setOlcIndexIntLen( Integer olcIndexIntLen )
    {
        this.olcIndexIntLen = olcIndexIntLen;
    }


    // ── setOlcIndexSubstrAnyLen — Palpatine Sets the Substring Any-Index Length ───
    /**
     * Sets the length of substrings used for "any" substring index keys.
     *
     * @param olcIndexSubstrAnyLen  the substring any-index key length to set
     */
    public void setOlcIndexSubstrAnyLen( Integer olcIndexSubstrAnyLen )
    {
        this.olcIndexSubstrAnyLen = olcIndexSubstrAnyLen;
    }


    // ── setOlcIndexSubstrAnyStep — Palpatine Sets the Substring Any-Index Step ────
    /**
     * Sets the step size for "any" substring index key generation.
     *
     * @param olcIndexSubstrAnyStep  the substring any-index step size to set
     */
    public void setOlcIndexSubstrAnyStep( Integer olcIndexSubstrAnyStep )
    {
        this.olcIndexSubstrAnyStep = olcIndexSubstrAnyStep;
    }


    // ── setOlcIndexSubstrIfMaxLen — Palpatine Sets Substring Final-Index Max Length ─
    /**
     * Sets the maximum length for "final" (suffix) substring index keys.
     *
     * @param olcIndexSubstrIfMaxLen  the substring final-index max length to set
     */
    public void setOlcIndexSubstrIfMaxLen( Integer olcIndexSubstrIfMaxLen )
    {
        this.olcIndexSubstrIfMaxLen = olcIndexSubstrIfMaxLen;
    }


    // ── setOlcIndexSubstrIfMinLen — Palpatine Sets Substring Final-Index Min Length ─
    /**
     * Sets the minimum length for "final" (suffix) substring index keys.
     *
     * @param olcIndexSubstrIfMinLen  the substring final-index min length to set
     */
    public void setOlcIndexSubstrIfMinLen( Integer olcIndexSubstrIfMinLen )
    {
        this.olcIndexSubstrIfMinLen = olcIndexSubstrIfMinLen;
    }


    // ── setOlcLdapSyntaxes — Palpatine Sets LDAP Syntax Definitions ──────────────
    /**
     * Replaces the {@code olcLdapSyntaxes} list with a defensive copy.
     *
     * @param olcLdapSyntaxes  the new LDAP syntax definitions to set
     */
    public void setOlcLdapSyntaxes( List<String> olcLdapSyntaxes )
    {
        this.olcLdapSyntaxes = copyListString( olcLdapSyntaxes );
    }


    // ── setOlcListenerThreads — Palpatine Sets the Listener Thread Count ──────────
    /**
     * Sets the number of threads dedicated to accepting new LDAP connections.
     *
     * @param olcListenerThreads  the listener thread count to set
     */
    public void setOlcListenerThreads( Integer olcListenerThreads )
    {
        this.olcListenerThreads = olcListenerThreads;
    }


    // ── setOlcLocalSSF — Palpatine Sets the Local Security Strength Factor ────────
    /**
     * Sets the security strength factor for local LDAP connections.
     *
     * @param olcLocalSSF  the local SSF value to set
     */
    public void setOlcLocalSSF( Integer olcLocalSSF )
    {
        this.olcLocalSSF = olcLocalSSF;
    }


    // ── setOlcLogFile — Palpatine Sets the Log File Path ─────────────────────────
    /**
     * Sets the path to the slapd log file.
     *
     * @param olcLogFile  the log file path to set
     */
    public void setOlcLogFile( String olcLogFile )
    {
        this.olcLogFile = olcLogFile;
    }


    // ── setOlcLogLevel — Palpatine Sets the Active Log Levels ────────────────────
    /**
     * Replaces the {@code olcLogLevel} list with a defensive copy.
     *
     * @param olcLogLevel  the new log level strings to set
     */
    public void setOlcLogLevel( List<String> olcLogLevel )
    {
        this.olcLogLevel = copyListString( olcLogLevel );
    }


    // ── setOlcObjectClasses — Palpatine Sets Custom Object Class Definitions ──────
    /**
     * Replaces the {@code olcObjectClasses} list with a defensive copy.
     *
     * @param olcObjectClasses  the new object class definitions to set
     */
    public void setOlcObjectClasses( List<String> olcObjectClasses )
    {
        this.olcObjectClasses = copyListString( olcObjectClasses );
    }


    // ── setOlcObjectIdentifier — Palpatine Sets OID Macro Definitions ─────────────
    /**
     * Replaces the {@code olcObjectIdentifier} list with a defensive copy.
     *
     * @param olcObjectIdentifier  the new OID macro definitions to set
     */
    public void setOlcObjectIdentifier( List<String> olcObjectIdentifier )
    {
        this.olcObjectIdentifier = copyListString( olcObjectIdentifier );
    }


    // ── setOlcPasswordCryptSaltFormat — Palpatine Sets the Crypt Salt Format ──────
    /**
     * Sets the crypt(3) salt format string used when hashing passwords.
     *
     * @param olcPasswordCryptSaltFormat  the crypt salt format string to set
     */
    public void setOlcPasswordCryptSaltFormat( String olcPasswordCryptSaltFormat )
    {
        this.olcPasswordCryptSaltFormat = olcPasswordCryptSaltFormat;
    }


    // ── setOlcPasswordHash — Palpatine Sets the Password Hash Scheme List ─────────
    /**
     * Replaces the {@code olcPasswordHash} list with a defensive copy.
     *
     * @param olcPasswordHash  the new password hash scheme list to set
     */
    public void setOlcPasswordHash( List<String> olcPasswordHash )
    {
        this.olcPasswordHash = copyListString( olcPasswordHash );
    }


    // ── setOlcPidFile — Palpatine Sets the PID File Path ─────────────────────────
    /**
     * Sets the path to the slapd PID file.
     *
     * @param olcPidFile  the PID file path to set
     */
    public void setOlcPidFile( String olcPidFile )
    {
        this.olcPidFile = olcPidFile;
    }


    // ── setOlcPluginLogFile — Palpatine Sets the Plugin Log File Path ─────────────
    /**
     * Sets the path to the plugin-specific log file.
     *
     * @param olcPluginLogFile  the plugin log file path to set
     */
    public void setOlcPluginLogFile( String olcPluginLogFile )
    {
        this.olcPluginLogFile = olcPluginLogFile;
    }


    // ── setOlcReadOnly — Palpatine Sets the Global Read-Only Mode ────────────────
    /**
     * Sets whether the entire server is in global read-only mode.
     *
     * @param olcReadOnly  true to enable global read-only mode
     */
    public void setOlcReadOnly( Boolean olcReadOnly )
    {
        this.olcReadOnly = olcReadOnly;
    }


    // ── setOlcReferral — Palpatine Sets the Global Referral URI ──────────────────
    /**
     * Sets the global referral URI returned for requests the server cannot handle.
     *
     * @param olcReferral  the referral URI string to set
     */
    public void setOlcReferral( String olcReferral )
    {
        this.olcReferral = olcReferral;
    }


    // ── setOlcReplogFile — Palpatine Sets the Replication Log File Path ───────────
    /**
     * Sets the path to the replication log file (legacy slurpd-based replication).
     *
     * @param olcReplogFile  the replog file path to set
     */
    public void setOlcReplogFile( String olcReplogFile )
    {
        this.olcReplogFile = olcReplogFile;
    }


    // ── setOlcRequires — Palpatine Sets the Global Requirements List ──────────────
    /**
     * Replaces the {@code olcRequires} list with a defensive copy.
     *
     * @param olcRequires  the new requirements list to set
     */
    public void setOlcRequires( List<String> olcRequires )
    {
        this.olcRequires = copyListString( olcRequires );
    }


    // ── setOlcReverseLookup — Palpatine Sets the Reverse DNS Lookup Flag ──────────
    /**
     * Sets whether slapd performs reverse DNS lookups on client IP addresses.
     *
     * @param olcReverseLookup  true to enable reverse DNS lookups
     */
    public void setOlcReverseLookup( Boolean olcReverseLookup )
    {
        this.olcReverseLookup = olcReverseLookup;
    }


    // ── setOlcRestrict — Palpatine Sets the Global Operation Restrictions ─────────
    /**
     * Replaces the {@code olcRestrict} list with a defensive copy.
     *
     * @param olcRestrict  the new restrictions list to set
     */
    public void setOlcRestrict( List<String> olcRestrict )
    {
        this.olcRestrict = copyListString( olcRestrict );
    }


    // ── setOlcRootDSE — Palpatine Sets the Root DSE Supplement Files ─────────────
    /**
     * Replaces the {@code olcRootDSE} list with a defensive copy.
     * Each string is a path to an LDIF file providing extra root DSE attributes.
     *
     * @param olcRootDSE  the new root DSE file paths list to set
     */
    public void setOlcRootDSE( List<String> olcRootDSE )
    {
        this.olcRootDSE = copyListString( olcRootDSE );
    }


    // ── setOlcSaslAuxprops — Palpatine Sets the SASL Auxprops Plugin ─────────────
    /**
     * Sets the SASL auxprops plugin name(s) for auxiliary property lookup.
     *
     * @param olcSaslAuxprops  the SASL auxprops string to set
     */
    public void setOlcSaslAuxprops( String olcSaslAuxprops )
    {
        this.olcSaslAuxprops = olcSaslAuxprops;
    }


    // ── setOlcSaslAuxpropsDontUseCopy — Palpatine Sets the Don't-Use-Copy Attribute
    /**
     * Sets the attribute type(s) for which SASL auxprops must not use a cached copy.
     *
     * @param olcSaslAuxpropsDontUseCopy  the dont-use-copy attribute type list string to set
     */
    public void setOlcSaslAuxpropsDontUseCopy( String olcSaslAuxpropsDontUseCopy )
    {
        this.olcSaslAuxpropsDontUseCopy = olcSaslAuxpropsDontUseCopy;
    }


    // ── setOlcSaslAuxpropsDontUseCopyIgnore — Palpatine Sets the Ignore Flag ──────
    /**
     * Sets whether SASL auxprops failures on dont-use-copy attributes are silently ignored.
     *
     * @param olcSaslAuxpropsDontUseCopyIgnore  true to ignore errors; false to propagate them
     */
    public void setOlcSaslAuxpropsDontUseCopyIgnore( Boolean olcSaslAuxpropsDontUseCopyIgnore )
    {
        this.olcSaslAuxpropsDontUseCopyIgnore = olcSaslAuxpropsDontUseCopyIgnore;
    }


    // ── setOlcSaslHost — Palpatine Sets the SASL Host Name ───────────────────────
    /**
     * Sets the FQDN used as the SASL service host name.
     *
     * @param olcSaslHost  the SASL host name to set
     */
    public void setOlcSaslHost( String olcSaslHost )
    {
        this.olcSaslHost = olcSaslHost;
    }


    // ── setOlcSaslRealm — Palpatine Sets the SASL Realm ──────────────────────────
    /**
     * Sets the SASL realm name used for SASL authentication.
     *
     * @param olcSaslRealm  the SASL realm string to set
     */
    public void setOlcSaslRealm( String olcSaslRealm )
    {
        this.olcSaslRealm = olcSaslRealm;
    }


    // ── setOlcSaslSecProps — Palpatine Sets the SASL Security Properties ──────────
    /**
     * Sets the SASL security properties string.
     *
     * @param olcSaslSecProps  the SASL security properties string to set
     */
    public void setOlcSaslSecProps( String olcSaslSecProps )
    {
        this.olcSaslSecProps = olcSaslSecProps;
    }


    // ── setOlcSecurity — Palpatine Sets the Security Strength Factor Requirements ─
    /**
     * Replaces the {@code olcSecurity} list with a defensive copy.
     *
     * @param olcSecurity  the new security requirement strings to set
     */
    public void setOlcSecurity( List<String> olcSecurity )
    {
        this.olcSecurity = copyListString( olcSecurity );
    }


    // ── setOlcServerID — Palpatine Sets the Server ID List ───────────────────────
    /**
     * Replaces the {@code olcServerID} list with a defensive copy.
     *
     * @param olcServerID  the new server ID strings to set
     */
    public void setOlcServerID( List<String> olcServerID )
    {
        this.olcServerID = copyListString( olcServerID );
    }


    // ── setOlcSizeLimit — Palpatine Sets the Global Search Size Limit ─────────────
    /**
     * Sets the global default search result size limit.
     *
     * @param olcSizeLimit  the size limit string to set
     */
    public void setOlcSizeLimit( String olcSizeLimit )
    {
        this.olcSizeLimit = olcSizeLimit;
    }


    // ── setOlcSockbufMaxIncoming — Palpatine Sets the Max Incoming Socket Buffer ───
    // Note: the parameter is a primitive int (not Integer), matching the original source.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the maximum incoming socket buffer size for unauthenticated connections.
     * Note: this setter takes a primitive {@code int}, not {@code Integer}.
     *
     * @param olcSockbufMaxIncoming  the max incoming socket buffer size to set
     */
    public void setOlcSockbufMaxIncoming( int olcSockbufMaxIncoming )
    {
        this.olcSockbufMaxIncoming = olcSockbufMaxIncoming;
    }


    // ── setOlcSockbufMaxIncomingAuth — Palpatine Sets the Authenticated Buffer Size ─
    /**
     * Sets the maximum incoming socket buffer size for authenticated connections.
     *
     * @param olcSockbufMaxIncomingAuth  the max authenticated incoming socket buffer size string to set
     */
    public void setOlcSockbufMaxIncomingAuth( String olcSockbufMaxIncomingAuth )
    {
        this.olcSockbufMaxIncomingAuth = olcSockbufMaxIncomingAuth;
    }


    // ── setOlcTCPBuffer — Palpatine Sets the TCP Buffer Configuration ─────────────
    /**
     * Replaces the {@code olcTCPBuffer} list with a defensive copy.
     *
     * @param olcTCPBuffer  the new TCP buffer configuration strings to set
     */
    public void setOlcTCPBuffer( List<String> olcTCPBuffer )
    {
        this.olcTCPBuffer = copyListString( olcTCPBuffer );
    }


    // ── setOlcThreads — Palpatine Sets the Worker Thread Pool Size ───────────────
    /**
     * Sets the number of worker threads in slapd's thread pool.
     *
     * @param olcThreads  the thread pool size to set
     */
    public void setOlcThreads( Integer olcThreads )
    {
        this.olcThreads = olcThreads;
    }


    // ── setOlcThreadQueues — Palpatine Sets the Thread Queue Count ───────────────
    /**
     * Sets the number of task queues used by the thread pool.
     *
     * @param olcThreadQueues  the thread queue count to set
     */
    public void setOlcThreadQueues( Integer olcThreadQueues )
    {
        this.olcThreadQueues = olcThreadQueues;
    }


    // ── setOlcTimeLimit — Palpatine Sets the Global Time Limit List ──────────────
    /**
     * Replaces the {@code olcTimeLimit} list with a defensive copy.
     *
     * @param olcTimeLimit  the new time limit strings to set
     */
    public void setOlcTimeLimit( List<String> olcTimeLimit )
    {
        this.olcTimeLimit = copyListString( olcTimeLimit );
    }


    // ── setOlcTLSCACertificateFile — Palpatine Sets the TLS CA Certificate File ───
    /**
     * Sets the path to the TLS CA certificate file (PEM format).
     *
     * @param olcTLSCACertificateFile  the TLS CA certificate file path to set
     */
    public void setOlcTLSCACertificateFile( String olcTLSCACertificateFile )
    {
        this.olcTLSCACertificateFile = olcTLSCACertificateFile;
    }


    // ── setOlcTLSCACertificatePath — Palpatine Sets the TLS CA Certificate Directory
    /**
     * Sets the path to the directory containing TLS CA certificates.
     *
     * @param olcTLSCACertificatePath  the TLS CA certificate directory path to set
     */
    public void setOlcTLSCACertificatePath( String olcTLSCACertificatePath )
    {
        this.olcTLSCACertificatePath = olcTLSCACertificatePath;
    }


    // ── setOlcTLSCertificateFile — Palpatine Sets the TLS Server Certificate File ─
    /**
     * Sets the path to the server's TLS certificate file (PEM format).
     *
     * @param olcTLSCertificateFile  the TLS certificate file path to set
     */
    public void setOlcTLSCertificateFile( String olcTLSCertificateFile )
    {
        this.olcTLSCertificateFile = olcTLSCertificateFile;
    }


    // ── setOlcTLSCertificateKeyFile — Palpatine Sets the TLS Private Key File ─────
    /**
     * Sets the path to the server's TLS private key file (PEM format).
     *
     * @param olcTLSCertificateKeyFile  the TLS private key file path to set
     */
    public void setOlcTLSCertificateKeyFile( String olcTLSCertificateKeyFile )
    {
        this.olcTLSCertificateKeyFile = olcTLSCertificateKeyFile;
    }


    // ── setOlcTLSCipherSuite — Palpatine Sets the TLS Cipher Suite ───────────────
    /**
     * Sets the TLS cipher suite string (OpenSSL format).
     *
     * @param olcTLSCipherSuite  the TLS cipher suite string to set
     */
    public void setOlcTLSCipherSuite( String olcTLSCipherSuite )
    {
        this.olcTLSCipherSuite = olcTLSCipherSuite;
    }


    // ── setOlcTLSCRLCheck — Palpatine Sets the TLS CRL Check Mode ────────────────
    /**
     * Sets the TLS Certificate Revocation List check mode.
     *
     * @param olcTLSCRLCheck  the CRL check mode to set ("none", "peer", or "all")
     */
    public void setOlcTLSCRLCheck( String olcTLSCRLCheck )
    {
        this.olcTLSCRLCheck = olcTLSCRLCheck;
    }


    // ── setOlcTLSCRLFile — Palpatine Sets the TLS CRL File Path ──────────────────
    /**
     * Sets the path to the TLS Certificate Revocation List file.
     *
     * @param olcTLSCRLFile  the CRL file path to set
     */
    public void setOlcTLSCRLFile( String olcTLSCRLFile )
    {
        this.olcTLSCRLFile = olcTLSCRLFile;
    }


    // ── setOlcTLSDHParamFile — Palpatine Sets the TLS DH Parameters File ──────────
    /**
     * Sets the path to the Diffie-Hellman parameters file for TLS.
     *
     * @param olcTLSDHParamFile  the DH parameters file path to set
     */
    public void setOlcTLSDHParamFile( String olcTLSDHParamFile )
    {
        this.olcTLSDHParamFile = olcTLSDHParamFile;
    }


    // ── setOlcTLSECName — Palpatine Sets the TLS Elliptic Curve Name ─────────────
    /**
     * Sets the name of the elliptic curve for TLS ECDHE key exchange.
     *
     * @param olcTLSECName  the elliptic curve name to set (e.g., "prime256v1")
     */
    public void setOlcTLSECName( String olcTLSECName )
    {
        this.olcTLSECName = olcTLSECName;
    }


    // ── setOlcTLSProtocolMin — Palpatine Sets the Minimum TLS Protocol Version ────
    /**
     * Sets the minimum TLS protocol version slapd will accept.
     *
     * @param olcTLSProtocolMin  the minimum TLS protocol version string to set
     */
    public void setOlcTLSProtocolMin( String olcTLSProtocolMin )
    {
        this.olcTLSProtocolMin = olcTLSProtocolMin;
    }


    // ── setOlcTLSRandFile — Palpatine Sets the TLS Random Seed File ──────────────
    /**
     * Sets the path to the TLS random number generator seed file.
     *
     * @param olcTLSRandFile  the TLS rand file path to set
     */
    public void setOlcTLSRandFile( String olcTLSRandFile )
    {
        this.olcTLSRandFile = olcTLSRandFile;
    }


    // ── setOlcTLSVerifyClient — Palpatine Sets the TLS Client Verification Mode ───
    /**
     * Sets the TLS client certificate verification mode.
     *
     * @param olcTLSVerifyClient  the client verification mode to set
     */
    public void setOlcTLSVerifyClient( String olcTLSVerifyClient )
    {
        this.olcTLSVerifyClient = olcTLSVerifyClient;
    }


    // ── setOlcToolThreads — Palpatine Sets the Tool Thread Count ─────────────────
    /**
     * Sets the number of threads used by slapd command-line tools.
     *
     * @param olcToolThreads  the tool thread count to set
     */
    public void setOlcToolThreads( Integer olcToolThreads )
    {
        this.olcToolThreads = olcToolThreads;
    }


    // ── setOlcWriteTimeout — Palpatine Sets the Write Operation Timeout ───────────
    /**
     * Sets the timeout in seconds for write operations.
     *
     * @param olcWriteTimeout  the write timeout in seconds to set
     */
    public void setOlcWriteTimeout( Integer olcWriteTimeout )
    {
        this.olcWriteTimeout = olcWriteTimeout;
    }
}
