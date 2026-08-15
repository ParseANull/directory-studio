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


// ── CLASS: OlcPBindConfig — Vader Augmenting the Bind with a Remote Proxy ────
// When Palpatine wants to verify loyalty, he doesn't do it personally — he routes
// the identity check through a trusted Imperial proxy on another Star Destroyer,
// with configurable timeouts and quarantine in case that proxy goes dark.
// The pbind overlay does exactly this: it proxies bind (authentication) operations
// to a remote LDAP server, with network timeout and quarantine settings for
// resilience. This class holds those settings.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Configuration bean for the 'olcPBindConfig' overlay — the proxy-bind (pbind)
 * overlay that forwards LDAP bind operations to a remote LDAP server URI.
 * Extends OlcOverlayConfig because pbind is an overlay bolted onto a database backend.
 * Think of this as Vader's augmented authentication relay: requests go to a proxy
 * before the real decision is made.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcPBindConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcDbURI' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbURI", isOptional = false, version="2.4.0")
    private String olcDbURI;

    /**
     * Field for the 'olcDbNetworkTimeout' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbNetworkTimeout", version="2.4.0")
    private String olcDbNetworkTimeout;

    /**
     * Field for the 'olcDbQuarantine' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbQuarantine", version="2.4.0")
    private String olcDbQuarantine;

    /**
     * Field for the 'olcStartTLS' attribute.
     */
    @ConfigurationElement(attributeType = "olcStartTLS", version="2.5.0")
    private String olcStartTLS;


    // ── Default Constructor — The Relay Console Stands Ready ─────────────────────
    // The authentication relay console is powered up, all dials at zero, waiting
    // for Palpatine to specify the target Star Destroyer and timeout thresholds.
    // We need this so the I/O layer can instantiate an empty bean and populate it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fresh, empty OlcPBindConfig with no URI or settings configured.
     * The I/O layer uses this and then populates via setters.
     *
     * <p>For example — the relay console initializes blank:</p>
     * <pre>
     *   OlcPBindConfig pbind = new OlcPBindConfig();
     *   pbind.setOlcDbURI( "ldap://auth.example.com" );
     * </pre>
     */
    public OlcPBindConfig()
    {
        super();
    }


    // ── Copy Constructor — Cloning the Relay Configuration ───────────────────────
    // Imperial engineers duplicate the relay configuration to a backup console,
    // ensuring both units have identical settings but are independently adjustable.
    // We deep-copy all fields so edits to the copy don't affect the original.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of an existing OlcPBindConfig.
     * Changes to the returned copy won't affect the original.
     *
     * <p>For example — engineers clone the relay config to a backup:</p>
     * <pre>
     *   OlcPBindConfig backup = new OlcPBindConfig( original );
     * </pre>
     *
     * @param o  the source OlcPBindConfig to copy
     */
    public OlcPBindConfig( OlcPBindConfig o )
    {
        super( o );
        olcDbURI = o.olcDbURI;
        olcDbNetworkTimeout = o.olcDbNetworkTimeout;
        olcDbQuarantine = o.olcDbQuarantine;
        olcStartTLS = o.olcStartTLS;
    }


    // ── Get Network Timeout — Check the Relay Response Deadline ──────────────────
    // Palpatine checks how long the system waits for a response from the proxy
    // before giving up and treating it as unreachable.
    // We expose this so the UI and I/O layer can read the timeout setting.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the olcDbNetworkTimeout — how long (in seconds) the pbind overlay
     * waits for a response from the remote LDAP server before declaring it unreachable.
     *
     * <p>For example — Palpatine checks the response deadline:</p>
     * <pre>
     *   String timeout = pbind.getOlcDbNetworkTimeout(); // e.g. "30"
     * </pre>
     *
     * @return  the network timeout string, or null if not set (defaults to system default)
     */
    public String getOlcDbNetworkTimeout()
    {
        return olcDbNetworkTimeout;
    }


    // ── Get Quarantine — Check the Offline Quarantine Policy ─────────────────────
    // Palpatine checks how the system handles a remote proxy that keeps going dark.
    // Quarantine specifies retry intervals when the remote server is repeatedly unavailable.
    // We expose this for the I/O layer to serialize.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the olcDbQuarantine setting — controls how the overlay backs off
     * and retries when the remote LDAP server is repeatedly unreachable.
     *
     * <p>For example — Palpatine checks the quarantine protocol:</p>
     * <pre>
     *   String q = pbind.getOlcDbQuarantine(); // e.g. "4096"
     * </pre>
     *
     * @return  the quarantine specification string, or null if not set
     */
    public String getOlcDbQuarantine()
    {
        return olcDbQuarantine;
    }


    // ── Get URI — Read the Proxy Target Address ───────────────────────────────────
    // Palpatine reads the coordinates of the Imperial proxy server that will
    // handle authentication validation on his behalf.
    // We expose the URI so the I/O layer and UI can read where bind requests go.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the olcDbURI — the LDAP URI of the remote server to which bind
     * operations are forwarded (e.g., "ldap://auth.example.com").
     *
     * <p>For example — Palpatine reads the proxy coordinates:</p>
     * <pre>
     *   String uri = pbind.getOlcDbURI(); // "ldap://auth.example.com"
     * </pre>
     *
     * @return  the proxy server URI, or null if not set
     */
    public String getOlcDbURI()
    {
        return olcDbURI;
    }


    // ── Get StartTLS — Check the Encryption Requirement ──────────────────────────
    // Palpatine checks whether the relay requires an encrypted channel to the proxy.
    // StartTLS upgrades a plain LDAP connection to TLS before sending credentials.
    // We expose this so the UI and I/O layer can read the TLS requirement.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the olcStartTLS setting — whether to use StartTLS when connecting
     * to the remote LDAP server for proxied bind operations.
     *
     * <p>For example — Palpatine checks the encryption requirement:</p>
     * <pre>
     *   String tls = pbind.getOlcStartTLS(); // e.g. "yes"
     * </pre>
     *
     * @return  the StartTLS setting string, or null if not set
     */
    public String getOlcStartTLS()
    {
        return olcStartTLS;
    }


    // ── Set Network Timeout — Configure the Relay Response Deadline ───────────────
    // Palpatine sets how long the relay waits before declaring the proxy unresponsive.
    // Too short and legitimate delays get flagged; too long and failures stall operations.
    // We store the timeout string for serialization back to LDAP.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the olcDbNetworkTimeout — the max wait time (in seconds) for a response
     * from the remote LDAP server.
     *
     * <p>For example — Palpatine configures the response deadline:</p>
     * <pre>
     *   pbind.setOlcDbNetworkTimeout( "30" );
     * </pre>
     *
     * @param olcDbNetworkTimeout  the timeout value as a string (e.g., "30" for 30 seconds)
     */
    public void setOlcDbNetworkTimeout( String olcDbNetworkTimeout )
    {
        this.olcDbNetworkTimeout = olcDbNetworkTimeout;
    }


    // ── Set Quarantine — Configure the Offline Backoff Policy ────────────────────
    // Palpatine configures the quarantine protocol: how long to wait between retries
    // when the proxy keeps failing to respond.
    // We store this so the overlay knows how to back off when the target is dark.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the olcDbQuarantine policy — retry interval and backoff behavior when
     * the remote LDAP server is repeatedly unavailable.
     *
     * <p>For example — Palpatine sets the quarantine protocol:</p>
     * <pre>
     *   pbind.setOlcDbQuarantine( "4096" );
     * </pre>
     *
     * @param olcDbQuarantine  the quarantine specification string
     */
    public void setOlcDbQuarantine( String olcDbQuarantine )
    {
        this.olcDbQuarantine = olcDbQuarantine;
    }


    // ── Set URI — Configure the Proxy Target Address ──────────────────────────────
    // Palpatine points the relay at a specific Star Destroyer's authentication node.
    // That URI is where all bind operations will be forwarded.
    // We store it so the overlay knows where to route incoming bind requests.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the olcDbURI — the remote LDAP server address that bind operations are
     * proxied to. This is required; without it the overlay can't function.
     *
     * <p>For example — Palpatine sets the proxy address:</p>
     * <pre>
     *   pbind.setOlcDbURI( "ldap://auth.example.com" );
     * </pre>
     *
     * @param olcDbURI  the LDAP URI of the remote authentication server
     */
    public void setOlcDbURI( String olcDbURI )
    {
        this.olcDbURI = olcDbURI;
    }


    // ── Set StartTLS — Configure the Encryption Requirement ──────────────────────
    // Palpatine mandates whether encrypted channels are required to the proxy.
    // Setting this ensures credentials aren't transmitted in the clear.
    // We store the TLS setting for serialization back to LDAP.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the olcStartTLS requirement for connections to the remote LDAP server.
     *
     * <p>For example — Palpatine mandates encrypted channels:</p>
     * <pre>
     *   pbind.setOlcStartTLS( "yes" );
     * </pre>
     *
     * @param olcStartTLS  the StartTLS setting (e.g., "yes", "critical")
     */
    public void setOlcStartTLS( String olcStartTLS )
    {
        this.olcStartTLS = olcStartTLS;
    }


    // ── Copy — Clone This Relay Config ───────────────────────────────────────────
    // The Imperial engineering team produces a full duplicate of the relay
    // configuration so they can test changes on the copy without disrupting live ops.
    // We delegate to the copy constructor which does the deep copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcPBindConfig.
     * The copy is fully independent — changes to it won't affect this instance.
     *
     * <p>For example — engineers clone the relay config:</p>
     * <pre>
     *   OlcPBindConfig copy = original.copy();
     *   copy.setOlcDbURI( "ldap://backup-auth.example.com" ); // original unchanged
     * </pre>
     *
     * @return  a new OlcPBindConfig that is a deep copy of this one
     */
    @Override
    public OlcPBindConfig copy()
    {
        return new OlcPBindConfig( this );
    }
}
