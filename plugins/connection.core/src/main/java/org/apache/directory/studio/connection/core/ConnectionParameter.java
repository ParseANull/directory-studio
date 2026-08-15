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

package org.apache.directory.studio.connection.core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.directory.api.ldap.model.constants.SaslQoP;
import org.apache.directory.api.ldap.model.constants.SaslSecurityStrength;


// ── CLASS: ConnectionParameter — HAN PLOTS THE HYPERSPACE JUMP COORDINATES ────
// Before Han punches the Falcon into hyperspace he has to punch in all the
// coordinates: destination star system (host), docking bay (port), shield level
// (encryption), and access codes (auth / bind principal / password).
// This bean is that full set of nav coordinates — serializable, editable, and
// handed off to the Connection that actually fires the engines.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A plain Java bean that holds every setting needed to reach an LDAP server.
 * We keep it separate from {@link Connection} so it can be serialized to disk,
 * copied for editing, and passed around without dragging live network state.
 * Think of this class as Han's hyperspace nav cartridge: every dial, code, and
 * coordinate is stored here, and the Connection (Falcon) reads them at jump time.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionParameter
{
    /**
     * Enum for the used encryption method.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum EncryptionMethod
    {
        /** No encryption. */
        NONE,

        /** SSL encryption. */
        LDAPS,

        /** Encryption using Start TLS extension. */
        START_TLS

        ;

        // ── IS ENCRYPTED — CHECKING WHETHER THE SHIELDS ARE RAISED ───────────────────
        // Han glances at the deflector display to see if any shield is active.
        // NONE means flying dark; LDAPS or START_TLS means the shields are up.
        // We return true if encryption of any kind is configured.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Returns whether this mode involves any encryption.
         * Handy for guard clauses where we need to know "are we encrypted at all?"
         *
         * @return  {@code false} only for NONE; {@code true} for LDAPS and START_TLS.
         */
        public boolean isEncrytped()
        {
            return this != NONE;
        }
    }

    /**
     * Enum for the used authentication method.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum AuthenticationMethod
    {
        /** No authentication, anonymous bind. */
        NONE(0),

        /** Simple authentication, simple bind. */
        SIMPLE(1),

        /** SASL authentication using DIGEST-MD5. */
        SASL_DIGEST_MD5(2),

        /** SASL authentication using CRAM-MD5. */
        SASL_CRAM_MD5(3),

        /** SASL authentication using GSSAPI. */
        SASL_GSSAPI(4);

        private int value;

        // ── AUTH ENUM CONSTRUCTOR — STAMPING THE ACCESS BADGE TYPE ───────────────────
        // Imperial checkpoints issue different badge types: visitor (NONE), basic
        // clearance (SIMPLE), encrypted challenge (SASL_*) — each has a numeric code.
        // We store that code so the auth method can be serialized to an integer.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Wires an authentication strategy to a stable integer value for serialization.
         *
         * @param value  The integer that represents this auth method on disk.
         */
        private AuthenticationMethod( int value )
        {
            this.value = value;
        }


        // ── GET AUTH VALUE — READING THE BADGE NUMBER ─────────────────────────────────
        // Han reads the number stamped on his badge before showing it at the gate —
        // it's the numeric code the checkpoint computer understands.
        // We return the integer value so callers can serialize this enum.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Returns the integer value for this authentication method.
         * We use this when writing the auth method to an XML file or preference store.
         *
         * @return  The integer identifier for this auth method.
         */
        public int getValue()
        {
            return value;
        }
    }

    // ── KRB5 CREDENTIAL CONFIGURATION — WHERE HAN SOURCES HIS KERBEROS TICKET ────
    // Han either already has an Imperial pass cached in his pocket (USE_NATIVE)
    // or needs to go to the sector's auth station to get one issued (OBTAIN_TGT).
    /**
     * Controls where we source the Kerberos TGT for GSSAPI authentication.
     * USE_NATIVE means the OS ticket cache; OBTAIN_TGT means we prompt for
     * a username/password and obtain a TGT ourselves.
     */
    public enum Krb5CredentialConfiguration
    {
        USE_NATIVE, OBTAIN_TGT
    }

    // ── KRB5 CONFIGURATION — HOW WE FIND THE KDC ─────────────────────────────────
    // DEFAULT = trust the JVM / OS; FILE = load a custom krb5.conf; MANUAL = type in KDC.
    /**
     * Controls how we locate the Kerberos Key Distribution Center.
     * DEFAULT delegates to the JVM; FILE reads a specified krb5.conf; MANUAL
     * lets the user type in the KDC host and realm directly.
     */
    public enum Krb5Configuration
    {
        DEFAULT, FILE, MANUAL
    }

    /**
     * Discriminates the wire protocol for this connection.
     * LDAP is the historical default; SCIM identifies connections managed by the
     * scim.* plugin family.  Persisted as an XML attribute; missing attribute
     * defaults to LDAP for backward compatibility.
     */
    public enum ConnectionProtocol
    {
        LDAP, SCIM
    }

    /** The unique id. */
    private String id;

    /** The symbolic name. */
    private String name;

    /** The host name or IP address of the LDAP server. */
    private String host;

    /** The port of the LDAP server. */
    private int port;

    /** The encryption method. */
    private EncryptionMethod encryptionMethod;

    /** The authentication method. */
    private AuthenticationMethod authMethod;

    /** The bind principal, typically a Dn. */
    private String bindPrincipal;

    /** The bind password. */
    private String bindPassword;

    /** The SASL realm. */
    private String saslRealm;

    /** The SASL qualitiy of protection. */
    private SaslQoP saslQop = SaslQoP.AUTH;

    /** The SASL security strength. */
    private SaslSecurityStrength saslSecurityStrength = SaslSecurityStrength.HIGH;

    /** The SASL mutual authentication flag. */
    private boolean saslMutualAuthentication = true;

    /** The Kerberos credential configuration. */
    private Krb5CredentialConfiguration krb5CredentialConfiguration = Krb5CredentialConfiguration.USE_NATIVE;

    /** The Kerberos configuration. */
    private Krb5Configuration krb5Configuration = Krb5Configuration.DEFAULT;

    /** The Kerberos configuration file. */
    private String krb5ConfigurationFile;

    /** The Kerberos realm. */
    private String krb5Realm;

    /** The Kerberos KDC host. */
    private String krb5KdcHost;

    /** The Kerberos KDC port. */
    private int krb5KdcPort = 88;

    /** The read only flag. */
    private boolean isReadOnly;

    /** The extended properties. */
    private Map<String, String> extendedProperties;

    /** The connection timeout. Default to 30 seconds */
    private long timeoutMillis = 30000L;

    /** The wire protocol for this connection. Default LDAP for backward compat. */
    private ConnectionProtocol connectionProtocol = ConnectionProtocol.LDAP;


    // ── DEFAULT CONSTRUCTOR — HAN GRABS A BLANK NAV CARTRIDGE ─────────────────────
    // Han reaches into the storage locker and pulls out a fresh, blank nav cartridge
    // — no destination programmed yet, just an empty data store ready to be filled.
    // We create an empty ConnectionParameter with only a fresh extended-properties map.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an empty {@link ConnectionParameter} with no settings filled in.
     * We use this when building a new connection from scratch (e.g. the "New Connection"
     * wizard starts here and sets fields one by one).
     *
     * <p>For example — Han grabs the blank cartridge:</p>
     * <pre>
     *   ConnectionParameter params = new ConnectionParameter();
     *   params.setHost("ldap.example.com");
     *   params.setPort(389);
     * </pre>
     */
    public ConnectionParameter()
    {
        this.extendedProperties = new HashMap<>();
    }


    // ── FULL CONSTRUCTOR — HAN PROGRAMS ALL COORDINATES AT ONCE ──────────────────
    // Han sits down and punches every coordinate into the nav cartridge in one go:
    // destination, docking bay, shield level, access codes — the full flight plan.
    // We initialize every field from the supplied arguments and generate a fresh UUID.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fully configured {@link ConnectionParameter} in one shot.
     * We use this when cloning a connection or loading one from disk where we
     * already have all values and just need to populate the bean.
     *
     * <p>For example — Han programs the cartridge:</p>
     * <pre>
     *   ConnectionParameter p = new ConnectionParameter(
     *       "My LDAP", "ldap.example.com", 389,
     *       EncryptionMethod.START_TLS, AuthenticationMethod.SIMPLE,
     *       "cn=admin,dc=example,dc=com", "secret",
     *       null, false, null, 30000L);
     * </pre>
     *
     * @param name                The user-visible connection label.
     * @param host                The LDAP server hostname or IP.
     * @param port                The TCP port (389, 636, etc.).
     * @param encryptionMethod    Whether to use NONE, LDAPS, or START_TLS.
     * @param authMethod          How we'll authenticate — anonymous, simple, or SASL.
     * @param bindPrincipal       The DN or username to bind with.
     * @param bindPassword        The password for the bind.
     * @param saslRealm           The SASL authentication realm (may be {@code null}).
     * @param isReadOnly          If {@code true}, no write operations will be allowed.
     * @param extendedProperties  A map of plugin-specific extras (may be {@code null}).
     * @param timeoutMillis       How long to wait for a response before giving up.
     */
    public ConnectionParameter( String name, String host, int port, EncryptionMethod encryptionMethod,
        AuthenticationMethod authMethod, String bindPrincipal, String bindPassword,
        String saslRealm, boolean isReadOnly, Map<String, String> extendedProperties, long timeoutMillis )
    {
        this.id = createId();
        this.name = name;
        this.host = host;
        this.port = port;
        this.encryptionMethod = encryptionMethod;
        this.authMethod = authMethod;
        this.bindPrincipal = bindPrincipal;
        this.bindPassword = bindPassword;
        this.saslRealm = saslRealm;
        this.isReadOnly = isReadOnly;
        this.extendedProperties = new HashMap<>();

        if ( extendedProperties != null )
        {
            this.extendedProperties.putAll( extendedProperties );
        }

        this.timeoutMillis = timeoutMillis;
    }


    // ── GET AUTH METHOD — HAN READS HIS CURRENT BADGE TYPE ───────────────────────
    // Han checks which style of ID badge he's currently carrying for the mission.
    // We return the authentication method this parameter bean has stored.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the stored authentication method.
     * This tells callers (and ultimately the connection wrapper) how we'll prove
     * our identity to the LDAP server at bind time.
     *
     * @return  The {@link AuthenticationMethod}.
     */
    public AuthenticationMethod getAuthMethod()
    {
        return authMethod;
    }


    // ── SET AUTH METHOD — HAN SWAPS HIS BADGE ────────────────────────────────────
    // Han trades his anonymous visitor pass for an Imperial officer's badge —
    // or switches to the encrypted SASL credential for a high-security sector.
    // We store the new authentication method in this bean.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the authentication method.
     * Switch between NONE (anonymous), SIMPLE (password), and SASL variants.
     *
     * @param authMethod  The new authentication method.
     */
    public void setAuthMethod( AuthenticationMethod authMethod )
    {
        this.authMethod = authMethod;
    }


    // ── GET BIND PASSWORD — HAN RETRIEVES HIS ACCESS CODE ────────────────────────
    // Han pulls the access code out of his jacket — it's what gets him past
    // the checkpoint and onto the station.
    // We return the bind password stored in this bean.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind password.
     * This is sent to the LDAP server during simple-bind authentication.
     * May be {@code null} for anonymous or Kerberos connections.
     *
     * @return  The bind password, or {@code null}.
     */
    public String getBindPassword()
    {
        return bindPassword;
    }


    // ── SET BIND PASSWORD — HAN UPDATES HIS ACCESS CODE ──────────────────────────
    // Han replaces his old access code with a freshly issued one so the
    // transponder will pass the checkpoint on the next mission.
    // We store the new password in this bean.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the bind password.
     * We store it here in the parameter bean; the connection uses it at bind time.
     *
     * @param bindPassword  The new password to store.
     */
    public void setBindPassword( String bindPassword )
    {
        this.bindPassword = bindPassword;
    }


    // ── GET SASL REALM — HAN CHECKS HIS SECTOR CODE ──────────────────────────────
    // Han reads the sector clearance code that scopes his credential to this
    // particular authentication domain.
    // We return the SASL realm string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL realm.
     * The realm scopes a SASL credential — think of it like a domain suffix.
     * Only needed for DIGEST-MD5 and similar mechanisms.
     *
     * @return  The SASL realm, or {@code null} if not set.
     */
    public String getSaslRealm()
    {
        return saslRealm;
    }


    // ── SET SASL REALM — HAN UPDATES HIS SECTOR CODE ─────────────────────────────
    // Han punches in the new sector clearance code on his transponder before
    // the mission moves to a different authentication domain.
    // We store the updated SASL realm.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the SASL realm.
     *
     * @param saslRealm  The SASL realm string to store.
     */
    public void setSaslRealm( String saslRealm )
    {
        this.saslRealm = saslRealm;
    }


    // ── IS READ ONLY — CHECKING IF HAN'S ON A RECON-ONLY MISSION ─────────────────
    // Han checks his orders: "observe and report" means read-only;
    // "extract the prisoner" means writes are allowed.
    // We return whether this connection should block all write operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this connection is restricted to read-only operations.
     * When {@code true}, the UI disables add/modify/delete actions.
     *
     * @return  {@code true} if this connection is read-only.
     */
    public boolean isReadOnly()
    {
        return isReadOnly;
    }


    // ── SET READ ONLY — HAN FLIPS THE WEAPONS-FREE SWITCH ────────────────────────
    // Han's commander flips the switch: safe mode (read-only) vs. weapons-free.
    // We store the new read-only flag.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the read-only flag.
     *
     * @param isReadOnly  Pass {@code true} to prevent any write operations.
     */
    public void setReadOnly( boolean isReadOnly )
    {
        this.isReadOnly = isReadOnly;
    }


    // ── GET BIND PRINCIPAL — HAN SHOWS HIS ID CARD ───────────────────────────────
    // Han holds up his ID card showing his rank and name — "cn=Han Solo,ou=Smugglers".
    // We return the stored bind principal (usually a DN).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind principal — the "who we are" credential for authentication.
     * For simple binds this is typically a Distinguished Name like
     * {@code cn=admin,dc=example,dc=com}.
     *
     * @return  The bind principal, or {@code null} for anonymous.
     */
    public String getBindPrincipal()
    {
        return bindPrincipal;
    }


    // ── SET BIND PRINCIPAL — HAN UPDATES HIS NAME ON THE MANIFEST ────────────────
    // Han scratches out the old name on the manifest and writes a new alias —
    // same ship, new identity for the checkpoint.
    // We store the updated bind principal.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the bind principal.
     *
     * @param bindPrincipal  The DN or username to use at bind time.
     */
    public void setBindPrincipal( String bindPrincipal )
    {
        this.bindPrincipal = bindPrincipal;
    }


    // ── GET ENCRYPTION METHOD — HAN CHECKS HIS SHIELD SETTING ────────────────────
    // Han glances at the shield panel: dark (NONE), full LDAPS shields, or
    // negotiated StartTLS shields after contact.
    // We return the stored encryption mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the encryption method configured for this connection.
     * NONE = no TLS; LDAPS = SSL from the first byte; START_TLS = negotiate TLS
     * over the plain connection before sending any LDAP messages.
     *
     * @return  The {@link EncryptionMethod}.
     */
    public EncryptionMethod getEncryptionMethod()
    {
        return encryptionMethod;
    }


    // ── SET ENCRYPTION METHOD — HAN RAISES OR LOWERS THE SHIELDS ─────────────────
    // Han adjusts the shield setting for the jump — plain sailing, full shields,
    // or raise shields after we clear the station.
    // We store the new encryption mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the encryption method.
     *
     * @param encryptionMethod  NONE, LDAPS, or START_TLS.
     */
    public void setEncryptionMethod( EncryptionMethod encryptionMethod )
    {
        this.encryptionMethod = encryptionMethod;
    }


    // ── GET ID — READING THE FALCON'S HULL REGISTRATION ──────────────────────────
    // Every ship has a unique hull registration burned into the transponder.
    // If no ID has been assigned yet, we generate one on the spot.
    // We return the stable UUID that uniquely identifies this connection across renames.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the unique ID for these connection parameters.
     * The ID is a UUID that stays constant even if the user renames the connection.
     * If somehow no ID was set (e.g. deserialized from old data), we generate one now.
     *
     * @return  The UUID string identifying this connection.
     */
    public String getId()
    {
        if ( id == null )
        {
            id = createId();
        }
        return id;
    }


    // ── SET ID — STAMPING THE HULL REGISTRATION ───────────────────────────────────
    // The shipyard stamps a hull registration number — or, during deserialization,
    // we restore the one that was saved to disk.
    // We store the provided id string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the unique ID.
     * We call this during deserialization to restore the id that was saved with the
     * connection rather than generating a new one.
     *
     * @param id  The UUID string to assign.
     */
    public void setId( String id )
    {
        this.id = id;
    }


    // ── GET HOST — READING THE DESTINATION STAR SYSTEM ───────────────────────────
    // Han reads the destination star system out of the nav cartridge before jumping.
    // We return the LDAP server hostname or IP.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP server hostname or IP address.
     *
     * @return  The hostname, e.g. {@code "ldap.example.com"} or {@code "192.168.1.5"}.
     */
    public String getHost()
    {
        return host;
    }


    // ── SET HOST — HAN PROGRAMS A NEW DESTINATION ────────────────────────────────
    // Han dials in the new star system coordinates before the jump.
    // We store the new hostname.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP server hostname or IP address.
     *
     * @param host  The server hostname to reach.
     */
    public void setHost( String host )
    {
        this.host = host;
    }


    // ── GET NAME — READING THE LABEL ON THE CARTRIDGE ────────────────────────────
    // Han reads the label he scrawled on the side of the nav cartridge.
    // We return the user-assigned display name for this connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the user-assigned display name for this connection.
     * This is what shows up in the Connections view.
     *
     * @return  The connection name, e.g. {@code "Production LDAP"}.
     */
    public String getName()
    {
        return name;
    }


    // ── SET NAME — HAN RE-LABELS THE CARTRIDGE ───────────────────────────────────
    // Han crosses out the old label and writes a new one on the cartridge.
    // We store the updated display name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the display name.
     *
     * @param name  The new name to show in the Connections view.
     */
    public void setName( String name )
    {
        this.name = name;
    }


    // ── GET PORT — READING THE DOCKING BAY NUMBER ────────────────────────────────
    // Han reads the docking bay number on the cartridge so he knows which airlock
    // to aim at when the Falcon drops out of hyperspace.
    // We return the LDAP server port.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the TCP port the LDAP server listens on.
     *
     * @return  The port number, typically 389 or 636.
     */
    public int getPort()
    {
        return port;
    }


    // ── SET PORT — HAN UPDATES THE DOCKING BAY NUMBER ────────────────────────────
    // The station moved to a new bay; Han corrects the cartridge.
    // We store the updated port number.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the TCP port.
     *
     * @param port  The port number to connect to.
     */
    public void setPort( int port )
    {
        this.port = port;
    }


    // ── GET SASL QOP — CHECKING THE SHIELD PROTECTION GRADE ──────────────────────
    // Han checks whether his shields are set to auth-only, integrity, or privacy mode.
    // We return the SASL quality-of-protection level.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL quality-of-protection level.
     * Ranges from auth-only (cheapest) to auth-conf (full encryption on the session).
     *
     * @return  The {@link SaslQoP}.
     */
    public SaslQoP getSaslQop()
    {
        return saslQop;
    }


    // ── SET SASL QOP — HAN DIALS IN THE SHIELD PROTECTION LEVEL ─────────────────
    // Han adjusts the shield generator to the required protection grade
    // before initiating the SASL handshake.
    // We store the new QoP setting.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the SASL quality-of-protection level.
     *
     * @param saslQop  AUTH, AUTH_INT, or AUTH_CONF.
     */
    public void setSaslQop( SaslQoP saslQop )
    {
        this.saslQop = saslQop;
    }


    // ── GET SASL SECURITY STRENGTH — CHECKING THE CIPHER RATING ──────────────────
    // Han checks what cipher grade his shields are rated for — LOW, MEDIUM, HIGH.
    // We return the SASL security strength.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL security strength.
     * Controls which cipher suites are acceptable during SASL negotiation.
     *
     * @return  The {@link SaslSecurityStrength}.
     */
    public SaslSecurityStrength getSaslSecurityStrength()
    {
        return saslSecurityStrength;
    }


    // ── SET SASL SECURITY STRENGTH — HAN SETS THE CIPHER GRADE ──────────────────
    // Han dials the cipher grade on the shield generator before engaging SASL.
    // We store the new security strength.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the SASL security strength.
     *
     * @param saslSecurityStrength  LOW, MEDIUM, or HIGH.
     */
    public void setSaslSecurityStrength( SaslSecurityStrength saslSecurityStrength )
    {
        this.saslSecurityStrength = saslSecurityStrength;
    }


    // ── IS SASL MUTUAL AUTH — DOES HAN ALSO VERIFY THE GUARD'S BADGE ─────────────
    // Han doesn't just show his ID; he also demands to see the guard's credentials
    // so he knows he hasn't walked into an Imperial trap.
    // We return whether mutual authentication is required.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether SASL mutual authentication is required.
     * When {@code true}, both we and the server must prove identity to each other.
     *
     * @return  {@code true} if the server must also authenticate to us.
     */
    public boolean isSaslMutualAuthentication()
    {
        return saslMutualAuthentication;
    }


    // ── SET SASL MUTUAL AUTH — HAN TOGGLES THE "CHECK THEIR ID TOO" POLICY ───────
    // Han toggles whether he also checks the guard's credentials or just shows
    // his own and walks on through.
    // We store the mutual-auth flag.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether SASL mutual authentication is required.
     *
     * @param saslMutualAuthentication  {@code true} to require server-side identity proof.
     */
    public void setSaslMutualAuthentication( boolean saslMutualAuthentication )
    {
        this.saslMutualAuthentication = saslMutualAuthentication;
    }


    // ── GET KRB5 CREDENTIAL CONFIG — HOW HAN SOURCES HIS KERBEROS TICKET ─────────
    // Han checks whether he's already carrying a valid TGT in his pocket
    // or whether he needs to stop at the sector auth station first.
    // We return the Kerberos credential configuration.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns how we source the Kerberos TGT.
     * USE_NATIVE = use the OS ticket cache; OBTAIN_TGT = prompt for credentials.
     *
     * @return  The {@link Krb5CredentialConfiguration}.
     */
    public Krb5CredentialConfiguration getKrb5CredentialConfiguration()
    {
        return krb5CredentialConfiguration;
    }


    // ── SET KRB5 CREDENTIAL CONFIG — HAN DECIDES HOW TO GET HIS TICKET ───────────
    // Han decides whether to use his cached pass or to queue up at the auth station.
    // We store the new Kerberos credential configuration.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets how we source the Kerberos TGT.
     *
     * @param krb5CredentialConfiguration  USE_NATIVE or OBTAIN_TGT.
     */
    public void setKrb5CredentialConfiguration( Krb5CredentialConfiguration krb5CredentialConfiguration )
    {
        this.krb5CredentialConfiguration = krb5CredentialConfiguration;
    }


    // ── GET KRB5 CONFIGURATION — WHERE THE FALCON LOOKS FOR KDC INFO ─────────────
    // Han decides whether to trust the ship's default databank, use a custom
    // codebook he brought, or type in KDC coordinates manually.
    // We return the Kerberos configuration mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the mode we use to locate the Kerberos Key Distribution Center.
     * DEFAULT, FILE, or MANUAL — see {@link Krb5Configuration} for details.
     *
     * @return  The {@link Krb5Configuration}.
     */
    public Krb5Configuration getKrb5Configuration()
    {
        return krb5Configuration;
    }


    // ── SET KRB5 CONFIGURATION — HAN PICKS HIS KDC LOOKUP STRATEGY ───────────────
    // Han picks whether the nav computer finds the KDC automatically or whether
    // he has to type in the coordinates by hand.
    // We store the new Kerberos configuration mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the Kerberos configuration mode.
     *
     * @param krb5Configuration  DEFAULT, FILE, or MANUAL.
     */
    public void setKrb5Configuration( Krb5Configuration krb5Configuration )
    {
        this.krb5Configuration = krb5Configuration;
    }


    // ── GET KRB5 CONFIG FILE — HAN READS HIS CUSTOM CODEBOOK PATH ────────────────
    // Han pulls out the custom Imperial codebook he brought for this mission
    // and reads the file path off the cover.
    // We return the path to the custom krb5.conf file.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the path to the Kerberos configuration file.
     * Only used when {@link #getKrb5Configuration()} returns FILE.
     *
     * @return  The absolute path to a krb5.conf file, or {@code null}.
     */
    public String getKrb5ConfigurationFile()
    {
        return krb5ConfigurationFile;
    }


    // ── SET KRB5 CONFIG FILE — HAN STOWS HIS CUSTOM CODEBOOK ─────────────────────
    // Han stows the custom codebook and writes its storage path into the cartridge
    // so the nav computer knows where to find it.
    // We store the path to the custom krb5.conf.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the path to the Kerberos configuration file.
     *
     * @param krb5ConfigurationFile  Absolute path to the krb5.conf file.
     */
    public void setKrb5ConfigurationFile( String krb5ConfigurationFile )
    {
        this.krb5ConfigurationFile = krb5ConfigurationFile;
    }


    // ── GET KRB5 REALM — HAN READS HIS SECTOR DESIGNATION ───────────────────────
    // Han checks which Imperial sector's auth system he's registered in —
    // that sector name is the Kerberos realm.
    // We return the configured Kerberos realm string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Kerberos realm.
     * The realm is the authentication domain, e.g. {@code "EXAMPLE.COM"}.
     *
     * @return  The Kerberos realm, or {@code null} if using system defaults.
     */
    public String getKrb5Realm()
    {
        return krb5Realm;
    }


    // ── SET KRB5 REALM — HAN UPDATES HIS SECTOR DESIGNATION ─────────────────────
    // Han updates his sector designation after crossing into a new auth domain.
    // We store the new Kerberos realm.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the Kerberos realm.
     *
     * @param krb5Realm  The realm string, e.g. {@code "EXAMPLE.COM"}.
     */
    public void setKrb5Realm( String krb5Realm )
    {
        this.krb5Realm = krb5Realm;
    }


    // ── GET KRB5 KDC HOST — HAN LOOKS UP THE AUTH STATION ADDRESS ────────────────
    // Han pulls the address of the sector's Key Distribution Center out of the
    // cartridge — that's where he goes to get his Kerberos ticket.
    // We return the KDC hostname.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Kerberos KDC hostname.
     * Only used in MANUAL Kerberos configuration mode.
     *
     * @return  The KDC hostname, or {@code null} if not set.
     */
    public String getKrb5KdcHost()
    {
        return krb5KdcHost;
    }


    // ── SET KRB5 KDC HOST — HAN WRITES DOWN THE AUTH STATION ADDRESS ─────────────
    // Han jots the KDC address on the cartridge for future reference.
    // We store the KDC hostname.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the Kerberos KDC hostname.
     *
     * @param krb5KdcHost  The KDC hostname to use in MANUAL mode.
     */
    public void setKrb5KdcHost( String krb5KdcHost )
    {
        this.krb5KdcHost = krb5KdcHost;
    }


    // ── GET KRB5 KDC PORT — READING THE AUTH STATION DOCKING BAY ────────────────
    // Han reads the port number (docking bay) for the sector's KDC.
    // We return the KDC port, defaulting to 88.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Kerberos KDC port.
     * Defaults to 88, the standard Kerberos port.
     *
     * @return  The KDC port number.
     */
    public int getKrb5KdcPort()
    {
        return krb5KdcPort;
    }


    // ── SET KRB5 KDC PORT — HAN UPDATES THE AUTH STATION DOCKING BAY ─────────────
    // The station moved its auth bay to a non-standard dock; Han updates the number.
    // We store the new KDC port.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the Kerberos KDC port.
     *
     * @param krb5KdcPort  The port number the KDC listens on.
     */
    public void setKrb5KdcPort( int krb5KdcPort )
    {
        this.krb5KdcPort = krb5KdcPort;
    }


    // ── GET EXTENDED PROPERTIES — HAN READS THE CARGO MANIFEST EXTRAS ────────────
    // The Falcon's cargo manifest has extra compartments for smuggled goods —
    // plugin-specific settings that don't fit the standard parameter fields.
    // We return the whole extended-properties map so callers can read any plugin setting.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full extended-properties map.
     * This is a catch-all bag for plugin-specific settings that don't have their own
     * field in this class. Each plugin picks its own key prefix to avoid collisions.
     *
     * @return  The mutable map of extended properties (never {@code null}).
     */
    public Map<String, String> getExtendedProperties()
    {
        return extendedProperties;
    }


    // ── SET EXTENDED PROPERTIES — HAN LOADS A NEW CARGO MANIFEST ─────────────────
    // Han swaps in a completely new cargo manifest for a different mission —
    // all the old compartments are replaced by the new set.
    // We replace the entire extended-properties map.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire extended-properties map.
     * We rarely call this directly; prefer {@link #setExtendedProperty} for single values.
     *
     * @param extendedProperties  The new map to use.
     */
    public void setExtendedProperties( Map<String, String> extendedProperties )
    {
        this.extendedProperties = extendedProperties;
    }


    // ── SET EXTENDED PROPERTY — HAN STASHES A SPECIFIC ITEM IN A COMPARTMENT ─────
    // Han slides a specific item into one of the Falcon's hidden compartments,
    // labeled by its key so he can find it again.
    // We put a single key/value pair into the extended-properties map.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores a single string value under the given key in extended properties.
     * Plugins use this to persist custom settings alongside the standard connection params.
     *
     * @param key    The property key (typically namespaced by the plugin, e.g. {@code "browser.pageSize"}).
     * @param value  The string value to store.
     */
    public void setExtendedProperty( String key, String value )
    {
        extendedProperties.put( key, value );
    }


    // ── GET EXTENDED PROPERTY — HAN RETRIEVES AN ITEM FROM A COMPARTMENT ─────────
    // Han reaches into the labeled compartment and pulls out whatever's stored there,
    // or comes back empty-handed if no one put anything in that slot.
    // We return the string value for the given key, or null if missing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string value stored under the given key, or {@code null} if absent.
     *
     * @param key  The property key to look up.
     * @return  The stored value, or {@code null} if the key doesn't exist.
     */
    public String getExtendedProperty( String key )
    {
        return extendedProperties.get( key );
    }


    // ── SET EXTENDED LIST STRING PROPERTY — HAN PACKS A LIST INTO ONE COMPARTMENT ─
    // Han has a list of items to smuggle but only one compartment — he tapes them
    // together with semicolons so they fit as one cargo unit.
    // We join a List<String> with semicolons and store it as a single string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores a {@code List<String>} by joining entries with {@code ;} and saving as a
     * single string.
     * This is how plugins persist ordered lists of values without needing a separate
     * serialization format.
     *
     * @param key    The property key.
     * @param value  The list to store; {@code null} or empty stores an empty string.
     */
    public void setExtendedListStringProperty( String key, List<String> value )
    {
        StringBuilder sb = new StringBuilder();
        if ( ( value != null ) && ( !value.isEmpty() ) )
        {
            for ( String string : value )
            {
                sb.append( string );
                sb.append( ';' );
            }
            sb.deleteCharAt( sb.length() - 1 );
        }

        extendedProperties.put( key, sb.toString() );
    }


    // ── GET EXTENDED LIST STRING PROPERTY — HAN UNPACKS THE TAPED CARGO ──────────
    // Han peels the tape off the bundle and separates the items back into
    // individual units, handing them back as a list.
    // We split the stored semicolon-delimited string back into a List<String>.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@code List<String>} by splitting the stored value on {@code ;}.
     * Reverses the encoding done by {@link #setExtendedListStringProperty}.
     *
     * @param key  The property key.
     * @return  The decoded list, or {@code null} if the key doesn't exist or is empty.
     */
    public List<String> getExtendedListStringProperty( String key )
    {
        String s = extendedProperties.get( key );
        if ( s != null )
        {
            String[] array = s.split( ";" ); //$NON-NLS-1$
            if ( ( array != null ) && ( array.length > 0 ) )
            {
                return new ArrayList<>( Arrays.asList( array ) );
            }
        }

        return null;
    }


    // ── SET EXTENDED INT PROPERTY — HAN STORES A NUMBERED SETTING ────────────────
    // Han writes a numbered dial setting into the labeled compartment,
    // converting it to a string because all compartments hold strings.
    // We store an int as its string representation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores an integer value by converting it to a string and saving under the given key.
     *
     * @param key    The property key.
     * @param value  The integer to store.
     */
    public void setExtendedIntProperty( String key, int value )
    {
        extendedProperties.put( key, Integer.toString( value ) );
    }


    // ── GET EXTENDED INT PROPERTY — HAN READS BACK A NUMBERED SETTING ────────────
    // Han reaches into the compartment and parses the number written on the label.
    // If the compartment is empty, he reports -1 as a sentinel.
    // We parse the stored string back to an int, or return -1 if absent.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an integer stored under the given key, or {@code -1} if the key doesn't exist.
     *
     * @param key  The property key.
     * @return  The integer value, or {@code -1} if not set.
     */
    public int getExtendedIntProperty( String key )
    {
        String s = extendedProperties.get( key );

        if ( s != null )
        {
            return Integer.parseInt( s );
        }
        else
        {
            return -1;
        }
    }


    // ── SET EXTENDED BOOL PROPERTY — HAN RECORDS A YES/NO SWITCH STATE ────────────
    // Han writes "true" or "false" on the label of a compartment to record
    // whether a particular switch is on or off.
    // We store a boolean as "true"/"false".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores a boolean value as its string representation under the given key.
     *
     * @param key    The property key.
     * @param value  The boolean to store.
     */
    public void setExtendedBoolProperty( String key, boolean value )
    {
        extendedProperties.put( key, Boolean.toString( value ) );
    }


    // ── GET EXTENDED BOOL PROPERTY — HAN READS A YES/NO SWITCH STATE ─────────────
    // Han reads the "true"/"false" label on the compartment and reports
    // whether the switch is on. Empty compartment = off (false).
    // We parse the stored string back to a boolean.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a boolean stored under the given key, or {@code false} if absent.
     *
     * @param key  The property key.
     * @return  The stored boolean value, or {@code false} if not set.
     */
    public boolean getExtendedBoolProperty( String key )
    {
        String s = extendedProperties.get( key );

        if ( s != null )
        {
            return Boolean.parseBoolean( s );
        }
        else
        {
            return false;
        }
    }


    // ── GET TIMEOUT MILLIS — HAN READS HIS PATIENCE LIMIT ────────────────────────
    // Han checks how many seconds he's set on the countdown before he aborts
    // the jump if the motivator doesn't respond.
    // We return the stored timeout in milliseconds.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the operation timeout in milliseconds.
     * Default is 30,000 ms (30 seconds).
     *
     * @return  The timeout in milliseconds.
     */
    public long getTimeoutMillis()
    {
        return timeoutMillis;
    }


    // ── SET TIMEOUT MILLIS — HAN ADJUSTS HIS COUNTDOWN ───────────────────────────
    // Han dials a new countdown into the nav computer — shorter for urgent runs,
    // longer when the server is across a slow link.
    // We store the new timeout.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the operation timeout in milliseconds.
     *
     * @param timeoutMillis  How long to wait before giving up on an LDAP operation.
     */
    public void setTimeoutMillis( long timeoutMillis )
    {
        this.timeoutMillis = timeoutMillis;
    }


    /**
     * Returns the wire protocol for this connection (LDAP or SCIM).
     * Defaults to {@link ConnectionProtocol#LDAP} for existing connections loaded from disk.
     *
     * @return The {@link ConnectionProtocol} for this connection.
     */
    public ConnectionProtocol getConnectionProtocol()
    {
        return connectionProtocol;
    }


    /**
     * Sets the wire protocol for this connection.
     *
     * @param connectionProtocol The {@link ConnectionProtocol} to set.
     */
    public void setConnectionProtocol( ConnectionProtocol connectionProtocol )
    {
        this.connectionProtocol = connectionProtocol;
    }


    // ── CREATE ID — THE SHIPYARD STAMPS A FRESH HULL REGISTRATION ────────────────
    // The shipyard stamps a new, unique hull registration number on every ship
    // it builds — guaranteed to never clash with any other vessel.
    // We generate a UUID for the new connection parameter.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Generates a fresh UUID to use as this parameter's unique identifier.
     * We call this in the constructor so every new connection parameter gets a
     * distinct ID right away.
     *
     * @return  A new random UUID string.
     */
    private String createId()
    {
        return UUID.randomUUID().toString();
    }


    // ── HASH CODE — THE FALCON'S TRANSPONDER HASH ─────────────────────────────────
    // Every transponder broadcasts a hash derived from the hull registration
    // so the station computer can put ships into buckets quickly.
    // We derive the hash from our UUID id.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a hash code based on this parameter's unique ID.
     * We use the UUID so two ConnectionParameter instances with the same ID
     * are guaranteed to land in the same hash bucket.
     *
     * @return  The hash code.
     */
    public int hashCode()
    {
        return getId().hashCode();
    }


    // ── EQUALS — CHECKING IF TWO CARTRIDGES CONTAIN THE SAME REGISTRATION ─────────
    // Han checks whether two nav cartridges belong to the same ship
    // by comparing their hull registration numbers.
    // We compare by UUID id, not by field values.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a {@link ConnectionParameter} with the same UUID.
     * We consider two parameters equal when they have the same id —
     * which means they represent the same persisted connection.
     *
     * @param obj  The object to compare with.
     * @return  {@code true} if both have the same unique ID.
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ConnectionParameter )
        {
            ConnectionParameter other = ( ConnectionParameter ) obj;
            return getId().equals( other.getId() );
        }
        return false;
    }

}
