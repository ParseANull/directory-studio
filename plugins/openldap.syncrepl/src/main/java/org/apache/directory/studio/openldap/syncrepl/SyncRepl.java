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
package org.apache.directory.studio.openldap.syncrepl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// ── CLASS: SyncRepl — The Sector Command's Full Intelligence Sync Configuration ─
// Grand Moff Tarkin issues each sector command a complete intelligence
// synchronisation briefing: which master server to contact (provider), what
// credentials to use (bindmethod, binddn, credentials), which part of the
// directory tree to mirror (searchbase, scope, filter, attrs), how often to
// poll or whether to stay connected (type, interval), what to do if the link
// fails (retry), and all the TLS security settings.
// SyncRepl is that full briefing: a Java bean holding every field of an
// OpenLDAP syncrepl directive.  It can be parsed from a directive string
// (via SyncReplParser), modified through its setters, and serialised back to
// a directive string via toString().
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A Java bean representation of an OpenLDAP {@code syncrepl} directive.
 * Contains all the configuration parameters needed to set up LDAP replication
 * from a provider (master) server to this consumer (replica) server.
 * Use {@link SyncReplParser} to parse an existing syncrepl string into an
 * instance, the setters to modify it, and {@link #toString()} to serialise it
 * back to a directive string for the config file.
 * Think of this as Tarkin's complete sector intelligence synchronisation
 * briefing — every field of the syncrepl directive in one place.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyncRepl
{
    /** The replica ID */
    private String rid;

    /** The provider */
    private Provider provider;

    /** The search base */
    private String searchBase;

    /** The type */
    private Type type;

    /** The interval */
    private Interval interval;

    /** The retry */
    private Retry retry;

    /** The filter */
    private String filter;

    /** The scope */
    private Scope scope;

    /** The attributes */
    private List<String> attributes = new ArrayList<String>();

    /** The attrsonly flag */
    private boolean isAttrsOnly;

    /** The size limit */
    private int sizeLimit = -1;

    /** The time limit */
    private int timeLimit = -1;

    /** The schema checking */
    private SchemaChecking schemaChecking;

    /** The network timeout */
    private int networkTimeout = -1;

    /** The timeout */
    private int timeout = -1;

    /** The bind method */
    private BindMethod bindMethod;

    /** The bind dn */
    private String bindDn;

    /** The sasl mech */
    private String saslMech;

    /** The authentication id */
    private String authcid;

    /** The authorization id */
    private String authzid;

    /** The credentials */
    private String credentials;

    /** The realm */
    private String realm;

    /** The sec props */
    private String secProps;

    /** The keep alive */
    private KeepAlive keepAlive;

    /** The Start TLS */
    private StartTls startTls;

    /** The TLS cert */
    private String tlsCert;

    /** The TLS key */
    private String tlsKey;

    /** The TLS cacert */
    private String tlsCacert;

    /** The TLS cacert dir */
    private String tlsCacertDir;

    /** The TLS reqcert */
    private TlsReqCert tlsReqcert;

    /** The TLS cipher suite */
    private String tlsCipherSuite;

    /** The TLS crl check */
    private TlsCrlCheck tlsCrlcheck;

    /** The log base */
    private String logBase;

    /** The log filter */
    private String logFilter;

    /** The sync data */
    private SyncData syncData;


    // ── New Sector Command Briefing — All Fields at Defaults ──────────────────
    // A blank briefing object; the caller populates fields via setters or by
    // parsing an existing syncrepl directive string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty SyncRepl configuration.
     * All optional fields default to {@code null} or {@code -1} (for integer
     * fields where -1 means "not set").
     */
    public SyncRepl()
    {
        // TODO Auto-generated constructor stub
    }


    // ── Issue a Default Briefing Template ─────────────────────────────────────
    // Tarkin issues a blank briefing template with no fields filled in, ready
    // for the sector command to customise.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SyncRepl object with all default values.
     * Currently identical to the no-arg constructor; this method exists so
     * callers can signal intent to create a "default" configuration rather than
     * an arbitrary blank one.
     *
     * @return  a new, empty {@link SyncRepl} instance.
     */
    public static SyncRepl createDefault()
    {
        SyncRepl syncRepl = new SyncRepl();

        return syncRepl;
    }


    // ── Issue a Copy of the Briefing to Another Sector Command ────────────────
    // Two sector commands might share the same baseline configuration; we
    // deep-copy all fields (including nested objects) so each command's copy
    // is fully independent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of the given SyncRepl configuration, or {@code null} if the input is {@code null}.
     * All nested objects ({@link Provider}, {@link Interval}, {@link Retry},
     * {@link KeepAlive}) are deep-copied; all enum and String fields are
     * copied by value (they are already immutable).
     *
     * @param syncRepl  the configuration to copy.
     * @return          a new independent {@link SyncRepl} with identical field values.
     */
    public static SyncRepl copy( SyncRepl syncRepl )
    {
        if ( syncRepl != null )
        {
            SyncRepl syncReplCopy = new SyncRepl();

            syncReplCopy.setRid( syncRepl.getRid() );
            syncReplCopy.setProvider( Provider.copy( syncRepl.getProvider() ) );
            syncReplCopy.setSearchBase( syncRepl.getSearchBase() );
            syncReplCopy.setType( syncRepl.getType() );
            syncReplCopy.setInterval( Interval.copy( syncRepl.getInterval() ) );
            syncReplCopy.setRetry( Retry.copy( syncRepl.getRetry() ) );
            syncReplCopy.setFilter( syncRepl.getFilter() );
            syncReplCopy.setScope( syncRepl.getScope() );
            syncReplCopy.addAttribute( syncRepl.getAttributes() );
            syncReplCopy.setAttrsOnly( syncRepl.isAttrsOnly() );
            syncReplCopy.setSizeLimit( syncRepl.getSizeLimit() );
            syncReplCopy.setTimeLimit( syncRepl.getTimeLimit() );
            syncReplCopy.setSchemaChecking( syncRepl.getSchemaChecking() );
            syncReplCopy.setNetworkTimeout( syncRepl.getNetworkTimeout() );
            syncReplCopy.setTimeout( syncRepl.getTimeout() );
            syncReplCopy.setBindMethod( syncRepl.getBindMethod() );
            syncReplCopy.setBindDn( syncRepl.getBindDn() );
            syncReplCopy.setSaslMech( syncRepl.getSaslMech() );
            syncReplCopy.setAuthcid( syncRepl.getAuthcid() );
            syncReplCopy.setAuthzid( syncRepl.getAuthzid() );
            syncReplCopy.setCredentials( syncRepl.getCredentials() );
            syncReplCopy.setRealm( syncRepl.getRealm() );
            syncReplCopy.setSecProps( syncRepl.getSecProps() );
            syncReplCopy.setKeepAlive( KeepAlive.copy( syncRepl.getKeepAlive() ) );
            syncReplCopy.setStartTls( syncRepl.getStartTls() );
            syncReplCopy.setTlsCert( syncRepl.getTlsCert() );
            syncReplCopy.setTlsKey( syncRepl.getTlsKey() );
            syncReplCopy.setTlsCacert( syncRepl.getTlsCacert() );
            syncReplCopy.setTlsCacertDir( syncRepl.getTlsCacertDir() );
            syncReplCopy.setTlsReqcert( syncRepl.getTlsReqcert() );
            syncReplCopy.setTlsCipherSuite( syncRepl.getTlsCipherSuite() );
            syncReplCopy.setTlsCrlcheck( syncRepl.getTlsCrlcheck() );
            syncReplCopy.setLogBase( syncRepl.getLogBase() );
            syncReplCopy.setLogFilter( syncRepl.getLogFilter() );
            syncReplCopy.setSyncData( syncRepl.getSyncData() );

            return syncReplCopy;
        }

        return null;
    }


    // ── Copy This Briefing ────────────────────────────────────────────────────
    // Convenience instance method delegating to the static copy().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this SyncRepl configuration.
     *
     * @return  a new independent {@link SyncRepl} with identical field values.
     */
    public SyncRepl copy()
    {
        return SyncRepl.copy( this );
    }


    /**
     * Returns the replica ID (rid) — a numeric string identifying this consumer
     * among all consumers connecting to the same provider.
     *
     * @return  the replica ID string.
     */
    public String getRid()
    {
        return rid;
    }


    /**
     * Returns the LDAP search base DN — the root of the subtree to replicate.
     *
     * @return  the search base DN string.
     */
    public String getSearchBase()
    {
        return searchBase;
    }


    /**
     * Returns the replication type — {@link Type#REFRESH_ONLY} (poll) or
     * {@link Type#REFRESH_AND_PERSIST} (persistent connection).
     *
     * @return  the replication type.
     */
    public Type getType()
    {
        return type;
    }


    /**
     * Returns the polling interval used in refreshOnly mode.
     *
     * @return  the polling interval, or {@code null} if not set.
     */
    public Interval getInterval()
    {
        return interval;
    }


    /**
     * Returns the reconnection retry schedule.
     *
     * @return  the retry schedule, or {@code null} if not set.
     */
    public Retry getRetry()
    {
        return retry;
    }


    /**
     * Returns the LDAP search filter applied to entries being replicated.
     *
     * @return  the filter string, or {@code null} if not set.
     */
    public String getFilter()
    {
        return filter;
    }


    /**
     * Returns the LDAP search scope — how deep under the search base to replicate.
     *
     * @return  the scope, or {@code null} if not set.
     */
    public Scope getScope()
    {
        return scope;
    }


    /**
     * Returns the list of attribute types to include in the replication.
     *
     * @return  the attribute list as an array; never {@code null}.
     */
    public String[] getAttributes()
    {
        return attributes.toArray( new String[0] );
    }


    /**
     * Returns {@code true} if only attribute types (not values) are replicated.
     *
     * @return  the attrsOnly flag.
     */
    public boolean isAttrsOnly()
    {
        return isAttrsOnly;
    }


    /**
     * Returns the maximum number of entries the provider will return per sync,
     * or {@code -1} if no limit is set.
     *
     * @return  the size limit.
     */
    public int getSizeLimit()
    {
        return sizeLimit;
    }


    /**
     * Returns the maximum time (in seconds) the provider will spend on a search,
     * or {@code -1} if no limit is set.
     *
     * @return  the time limit.
     */
    public int getTimeLimit()
    {
        return timeLimit;
    }


    /**
     * Returns the schema checking flag — whether the consumer validates
     * replicated entries against its local schema.
     *
     * @return  the schema checking setting, or {@code null} if not set.
     */
    public SchemaChecking getSchemaChecking()
    {
        return schemaChecking;
    }


    /**
     * Returns the network-level connection timeout in seconds, or {@code -1} if not set.
     *
     * @return  the network timeout.
     */
    public int getNetworkTimeout()
    {
        return networkTimeout;
    }


    /**
     * Returns the LDAP operation timeout in seconds, or {@code -1} if not set.
     *
     * @return  the operation timeout.
     */
    public int getTimeout()
    {
        return timeout;
    }


    /**
     * Returns the bind method — {@link BindMethod#SIMPLE} or {@link BindMethod#SASL}.
     *
     * @return  the bind method, or {@code null} if not set.
     */
    public BindMethod getBindMethod()
    {
        return bindMethod;
    }


    /**
     * Returns the bind DN used for simple authentication.
     *
     * @return  the bind DN string, or {@code null} if not set.
     */
    public String getBindDn()
    {
        return bindDn;
    }


    /**
     * Returns the SASL mechanism name (e.g. {@code "DIGEST-MD5"} or {@code "GSSAPI"}).
     *
     * @return  the SASL mechanism string, or {@code null} if not set.
     */
    public String getSaslMech()
    {
        return saslMech;
    }


    /**
     * Returns the SASL authentication identity (authcid).
     *
     * @return  the authcid string, or {@code null} if not set.
     */
    public String getAuthcid()
    {
        return authcid;
    }


    /**
     * Returns the SASL authorisation identity (authzid).
     *
     * @return  the authzid string, or {@code null} if not set.
     */
    public String getAuthzid()
    {
        return authzid;
    }


    /**
     * Returns the credentials (password) used for simple or SASL authentication.
     *
     * @return  the credentials string, or {@code null} if not set.
     */
    public String getCredentials()
    {
        return credentials;
    }


    /**
     * Returns the SASL realm.
     *
     * @return  the realm string, or {@code null} if not set.
     */
    public String getRealm()
    {
        return realm;
    }


    /**
     * Returns the SASL security properties string.
     *
     * @return  the secprops string, or {@code null} if not set.
     */
    public String getSecProps()
    {
        return secProps;
    }


    /**
     * Returns the TCP keep-alive configuration for the replication connection.
     *
     * @return  the keep-alive settings, or {@code null} if not set.
     */
    public KeepAlive getKeepAlive()
    {
        return keepAlive;
    }


    /**
     * Returns the StartTLS policy — whether to upgrade the connection to TLS.
     *
     * @return  the StartTLS setting, or {@code null} if not set.
     */
    public StartTls getStartTls()
    {
        return startTls;
    }


    /**
     * Returns the path to the TLS client certificate file.
     *
     * @return  the tls_cert path string, or {@code null} if not set.
     */
    public String getTlsCert()
    {
        return tlsCert;
    }


    /**
     * Returns the path to the TLS client private key file.
     *
     * @return  the tls_key path string, or {@code null} if not set.
     */
    public String getTlsKey()
    {
        return tlsKey;
    }


    /**
     * Returns the path to the CA certificate file used to verify the provider.
     *
     * @return  the tls_cacert path string, or {@code null} if not set.
     */
    public String getTlsCacert()
    {
        return tlsCacert;
    }


    /**
     * Returns the path to the directory containing CA certificate files.
     *
     * @return  the tls_cacertdir path string, or {@code null} if not set.
     */
    public String getTlsCacertDir()
    {
        return tlsCacertDir;
    }


    /**
     * Returns the TLS certificate requirement policy.
     *
     * @return  the tls_reqcert setting, or {@code null} if not set.
     */
    public TlsReqCert getTlsReqcert()
    {
        return tlsReqcert;
    }


    /**
     * Returns the TLS cipher suite specification string.
     *
     * @return  the tls_ciphersuite string, or {@code null} if not set.
     */
    public String getTlsCipherSuite()
    {
        return tlsCipherSuite;
    }


    /**
     * Returns the TLS CRL check policy.
     *
     * @return  the tls_crlcheck setting, or {@code null} if not set.
     */
    public TlsCrlCheck getTlsCrlcheck()
    {
        return tlsCrlcheck;
    }


    /**
     * Returns the log base DN (used with accesslog sync data source).
     *
     * @return  the logbase DN string, or {@code null} if not set.
     */
    public String getLogBase()
    {
        return logBase;
    }


    /**
     * Returns the log filter (used with accesslog sync data source).
     *
     * @return  the logfilter string, or {@code null} if not set.
     */
    public String getLogFilter()
    {
        return logFilter;
    }


    /**
     * Returns the sync data source — which data source the provider uses
     * to generate the replication stream.
     *
     * @return  the sync data setting, or {@code null} if not set.
     */
    public SyncData getSyncData()
    {
        return syncData;
    }


    /**
     * Sets the replica ID (rid).
     *
     * @param rid  a numeric string identifying this consumer, e.g. {@code "001"}.
     */
    public void setRid( String rid )
    {
        this.rid = rid;
    }


    /**
     * Returns the provider — the URL of the master LDAP server.
     *
     * @return  the provider, or {@code null} if not set.
     */
    public Provider getProvider()
    {
        return provider;
    }


    /**
     * Sets the provider (master server URL).
     *
     * @param provider  the provider URL.
     */
    public void setProvider( Provider provider )
    {
        this.provider = provider;
    }


    /**
     * Sets the LDAP search base DN.
     *
     * @param searchBase  the base DN string.
     */
    public void setSearchBase( String searchBase )
    {
        this.searchBase = searchBase;
    }


    /**
     * Sets the replication type.
     *
     * @param type  {@link Type#REFRESH_ONLY} or {@link Type#REFRESH_AND_PERSIST}.
     */
    public void setType( Type type )
    {
        this.type = type;
    }


    /**
     * Sets the polling interval (used in refreshOnly mode).
     *
     * @param interval  the polling interval.
     */
    public void setInterval( Interval interval )
    {
        this.interval = interval;
    }


    /**
     * Sets the reconnection retry schedule.
     *
     * @param retry  the retry schedule.
     */
    public void setRetry( Retry retry )
    {
        this.retry = retry;
    }


    /**
     * Sets the LDAP search filter.
     *
     * @param filter  the filter string, e.g. {@code "(objectClass=*)"}.
     */
    public void setFilter( String filter )
    {
        this.filter = filter;
    }


    /**
     * Sets the LDAP search scope.
     *
     * @param scope  one of {@link Scope#BASE}, {@link Scope#ONE}, {@link Scope#SUB}, {@link Scope#SUBORD}.
     */
    public void setScope( Scope scope )
    {
        this.scope = scope;
    }


    /**
     * Appends one or more attribute type names to the replication attribute list.
     *
     * @param attributes  attribute type names to include in the sync.
     */
    public void addAttribute( String... attributes )
    {
        if ( attributes != null )
        {
            for ( String attribute : attributes )
            {
                this.attributes.add( attribute );
            }
        }
    }


    /**
     * Removes one or more attribute type names from the replication attribute list.
     *
     * @param attributes  attribute type names to exclude from the sync.
     */
    public void removeAttribute( String... attributes )
    {
        if ( attributes != null )
        {
            for ( String attribute : attributes )
            {
                this.attributes.remove( attribute );
            }
        }
    }


    /**
     * Replaces the entire attribute list with the given array.
     *
     * @param attributes  the new list of attribute type names.
     */
    public void setAttributes( String[] attributes )
    {
        this.attributes.clear();
        this.attributes.addAll( Arrays.asList( attributes ) );
    }


    /**
     * Sets whether to replicate only attribute types (no values).
     *
     * @param isAttrsOnly  {@code true} to replicate schema structure only.
     */
    public void setAttrsOnly( boolean isAttrsOnly )
    {
        this.isAttrsOnly = isAttrsOnly;
    }


    /**
     * Sets the search size limit.  Use {@code -1} to clear.
     *
     * @param sizeLimit  the maximum number of entries per sync operation.
     */
    public void setSizeLimit( int sizeLimit )
    {
        this.sizeLimit = sizeLimit;
    }


    /**
     * Sets the search time limit in seconds.  Use {@code -1} to clear.
     *
     * @param timeLimit  the maximum search time in seconds.
     */
    public void setTimeLimit( int timeLimit )
    {
        this.timeLimit = timeLimit;
    }


    /**
     * Sets the schema checking flag.
     *
     * @param schemaChecking  {@link SchemaChecking#ON} or {@link SchemaChecking#OFF}.
     */
    public void setSchemaChecking( SchemaChecking schemaChecking )
    {
        this.schemaChecking = schemaChecking;
    }


    /**
     * Sets the network connection timeout in seconds.  Use {@code -1} to clear.
     *
     * @param networkTimeout  the TCP connection timeout in seconds.
     */
    public void setNetworkTimeout( int networkTimeout )
    {
        this.networkTimeout = networkTimeout;
    }


    /**
     * Sets the LDAP operation timeout in seconds.  Use {@code -1} to clear.
     *
     * @param timeout  the LDAP operation timeout in seconds.
     */
    public void setTimeout( int timeout )
    {
        this.timeout = timeout;
    }


    /**
     * Sets the bind method.
     *
     * @param bindMethod  {@link BindMethod#SIMPLE} or {@link BindMethod#SASL}.
     */
    public void setBindMethod( BindMethod bindMethod )
    {
        this.bindMethod = bindMethod;
    }


    /**
     * Sets the bind DN for simple authentication.
     *
     * @param bindDn  the bind DN string.
     */
    public void setBindDn( String bindDn )
    {
        this.bindDn = bindDn;
    }


    /**
     * Sets the SASL mechanism name string (e.g. {@code "DIGEST-MD5"}).
     *
     * @param saslMech  the mechanism name.
     */
    public void setSaslMech( String saslMech )
    {
        this.saslMech = saslMech;
    }


    /**
     * Sets the SASL authentication identity.
     *
     * @param authcid  the authentication ID.
     */
    public void setAuthcid( String authcid )
    {
        this.authcid = authcid;
    }


    /**
     * Sets the SASL authorisation identity.
     *
     * @param authzid  the authorisation ID.
     */
    public void setAuthzid( String authzid )
    {
        this.authzid = authzid;
    }


    /**
     * Sets the credentials (password) for authentication.
     *
     * @param credentials  the credentials string.
     */
    public void setCredentials( String credentials )
    {
        this.credentials = credentials;
    }


    /**
     * Sets the SASL realm.
     *
     * @param realm  the realm string.
     */
    public void setRealm( String realm )
    {
        this.realm = realm;
    }


    /**
     * Sets the SASL security properties string.
     *
     * @param secProps  the secprops string.
     */
    public void setSecProps( String secProps )
    {
        this.secProps = secProps;
    }


    /**
     * Sets the TCP keep-alive configuration.
     *
     * @param keepAlive  the keep-alive settings.
     */
    public void setKeepAlive( KeepAlive keepAlive )
    {
        this.keepAlive = keepAlive;
    }


    /**
     * Sets the StartTLS policy.
     *
     * @param startTls  {@link StartTls#YES} or {@link StartTls#CRITICAL}.
     */
    public void setStartTls( StartTls startTls )
    {
        this.startTls = startTls;
    }


    /**
     * Sets the path to the TLS client certificate file.
     *
     * @param tlsCert  the path string.
     */
    public void setTlsCert( String tlsCert )
    {
        this.tlsCert = tlsCert;
    }


    /**
     * Sets the path to the TLS client private key file.
     *
     * @param tlsKey  the path string.
     */
    public void setTlsKey( String tlsKey )
    {
        this.tlsKey = tlsKey;
    }


    /**
     * Sets the path to the CA certificate file.
     *
     * @param tlsCacert  the path string.
     */
    public void setTlsCacert( String tlsCacert )
    {
        this.tlsCacert = tlsCacert;
    }


    /**
     * Sets the path to the directory containing CA certificate files.
     *
     * @param tlsCacertDir  the path string.
     */
    public void setTlsCacertDir( String tlsCacertDir )
    {
        this.tlsCacertDir = tlsCacertDir;
    }


    /**
     * Sets the TLS certificate requirement policy.
     *
     * @param tlsReqcert  the policy — one of the {@link TlsReqCert} constants.
     */
    public void setTlsReqcert( TlsReqCert tlsReqcert )
    {
        this.tlsReqcert = tlsReqcert;
    }


    /**
     * Sets the TLS cipher suite string.
     *
     * @param tlsCipherSuite  the cipher suite specification.
     */
    public void setTlsCipherSuite( String tlsCipherSuite )
    {
        this.tlsCipherSuite = tlsCipherSuite;
    }


    /**
     * Sets the TLS CRL check policy.
     *
     * @param tlsCrlcheck  the CRL check policy — one of the {@link TlsCrlCheck} constants.
     */
    public void setTlsCrlcheck( TlsCrlCheck tlsCrlcheck )
    {
        this.tlsCrlcheck = tlsCrlcheck;
    }


    /**
     * Sets the log base DN for the accesslog sync data source.
     *
     * @param logBase  the log base DN string.
     */
    public void setLogBase( String logBase )
    {
        this.logBase = logBase;
    }


    /**
     * Sets the log filter for the accesslog sync data source.
     *
     * @param logFilter  the filter string.
     */
    public void setLogFilter( String logFilter )
    {
        this.logFilter = logFilter;
    }


    /**
     * Sets the sync data source.
     *
     * @param syncData  one of the {@link SyncData} constants.
     */
    public void setSyncData( SyncData syncData )
    {
        this.syncData = syncData;
    }


    // ── Write the Full Briefing Back into the OpenLDAP Config File ────────────
    // We iterate through all fields in directive-canonical order, appending each
    // set field as "key=value" with appropriate quoting.  Only non-null / non-(-1)
    // fields are included — unset fields are silently omitted, matching OpenLDAP's
    // behaviour of using defaults for anything not specified.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this SyncRepl configuration to an OpenLDAP syncrepl directive string.
     * Only fields with non-null (or non-{@code -1} for integers) values are emitted.
     * String values that may contain spaces are enclosed in double quotes;
     * existing double quotes within those strings are escaped.
     *
     * @return  the syncrepl directive string, e.g.
     *          {@code "rid=001 provider=ldap://ldap.example.com searchbase=\"dc=example,dc=com\" type=refreshOnly"}.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // Replica ID
        if ( rid != null )
        {
            sb.append( "rid=" );
            sb.append( rid );
        }

        // Provider
        if ( provider != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "provider=" );
            sb.append( provider.toString() );
        }

        // Search Base
        if ( searchBase != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "searchbase=" );
            sb.append( '"' );
            sb.append( escapeDoubleQuotes( searchBase ) );
            sb.append( '"' );
        }

        // Type
        if ( type != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "type=" );
            sb.append( type );
        }

        // Interval
        if ( interval != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "interval=" );
            sb.append( interval );
        }

        // Retry
        if ( retry != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "retry=" );
            sb.append( '"' );
            sb.append( escapeDoubleQuotes( retry.toString() ) );
            sb.append( '"' );
        }

        // Filter
        if ( filter != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "filter=" );
            sb.append( '"' );
            sb.append( escapeDoubleQuotes( filter ) );
            sb.append( '"' );
        }

        // Scope
        if ( scope != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "scope=" );
            sb.append( scope );
        }

        // Attributes
        if ( ( attributes != null ) && ( attributes.size() > 0 ) )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "attrs=" );
            sb.append( '"' );

            // Looping on all attributes
            for ( int i = 0; i < attributes.size(); i++ )
            {
                // Adding the attribute
                sb.append( attributes.get( i ) );

                // Adding the separator (except for the last one)
                if ( i != attributes.size() - 1 )
                {
                    sb.append( ',' );
                }
            }

            sb.append( '"' );
        }

        // Attrsonly Flag
        if ( isAttrsOnly )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "attrsonly" );
        }

        // Size Limit
        if ( sizeLimit != -1 )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "sizelimit=" );
            sb.append( sizeLimit );
        }

        // Time Limit
        if ( timeLimit != -1 )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "timelimit=" );
            sb.append( timeLimit );
        }

        // Schema Checking
        if ( schemaChecking != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "schemachecking=" );
            sb.append( schemaChecking );
        }

        // Network Timeout
        if ( networkTimeout != -1 )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "network-timeout=" );
            sb.append( networkTimeout );
        }

        // Timeout
        if ( timeout != -1 )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "timeout=" );
            sb.append( timeout );
        }

        // Bind Method
        if ( bindMethod != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "bindmethod=" );
            sb.append( bindMethod );
        }

        // Bind DN
        if ( bindDn != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "binddn=" );
            sb.append( '"' );
            sb.append( bindDn );
            sb.append( '"' );
        }

        // SASL Mech
        if ( saslMech != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "saslmech=" );
            sb.append( saslMech );
        }

        // Authentication ID
        if ( authcid != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "authcid=" );
            sb.append( '"' );
            sb.append( authcid );
            sb.append( '"' );
        }

        // Authorization ID
        if ( authzid != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "authzid=" );
            sb.append( '"' );
            sb.append( authzid );
            sb.append( '"' );
        }

        // Credentials
        if ( credentials != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "credentials=" );
            sb.append( credentials );
        }

        // Realm
        if ( realm != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "realm=" );
            sb.append( realm );
        }

        // Sec Props
        if ( secProps != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "secProps=" );
            sb.append( secProps );
        }

        // Keep Alive
        if ( keepAlive != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "keepalive=" );
            sb.append( keepAlive );
        }

        // Start TLS
        if ( startTls != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "starttls=" );
            sb.append( startTls );
        }

        // TLS Cert
        if ( tlsCert != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "tls_cert=" );
            sb.append( tlsCert );
        }

        // TLS Key
        if ( tlsKey != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "tls_key=" );
            sb.append( tlsKey );
        }

        // TLS Cacert
        if ( tlsCacert != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "tls_cacert=" );
            sb.append( tlsCacert );
        }

        // TLS Cacert Dir
        if ( tlsCacertDir != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "tls_cacertdir=" );
            sb.append( tlsCacertDir );
        }

        // TLS Reqcert
        if ( tlsReqcert != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "tls_reqcert=" );
            sb.append( tlsReqcert );
        }

        // TLS Cipher Suite
        if ( tlsCipherSuite != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "tls_ciphersuite=" );
            sb.append( tlsCipherSuite );
        }

        //  TLS Crl Check
        if ( tlsCrlcheck != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "tls_crlcheck=" );
            sb.append( tlsCrlcheck );
        }

        // Log Base
        if ( logBase != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "logbase=" );
            sb.append( '"' );
            sb.append( escapeDoubleQuotes( logBase ) );
            sb.append( '"' );
        }

        // Log Filter
        if ( logFilter != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "logfilter=" );
            sb.append( '"' );
            sb.append( escapeDoubleQuotes( logFilter ) );
            sb.append( '"' );
        }

        // Sync Data
        if ( syncData != null )
        {
            appendSpaceIfNeeded( sb );
            sb.append( "syncdata=" );
            sb.append( syncData );
        }

        return sb.toString();
    }


    // ── Add a Space Between Directive Tokens ──────────────────────────────────
    // OpenLDAP expects space-separated key=value tokens; we add a space before
    // each token after the first one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a single space to {@code sb} if it is non-null and non-empty.
     * Used to separate key=value tokens in the directive string.
     *
     * @param sb  the string builder to append to.
     */
    private void appendSpaceIfNeeded( StringBuilder sb )
    {
        if ( ( sb != null ) && ( sb.length() > 0 ) )
        {
            sb.append( " " );
        }
    }


    // ── Escape Any Double Quotes in a Value String ────────────────────────────
    // Values that may contain spaces are wrapped in double quotes; if the value
    // itself contains a double quote we escape it with a backslash so the config
    // parser can distinguish the embedded quote from the closing delimiter.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a copy of {@code text} with all {@code "} characters replaced by {@code \"}.
     * Returns {@code null} if the input is {@code null}.
     *
     * @param text  the string to escape.
     * @return      the escaped string, or {@code null}.
     */
    private String escapeDoubleQuotes( String text )
    {
        if ( text != null )
        {
            return text.replace( "\"", "\\\"" );
        }

        return null;
    }
}
