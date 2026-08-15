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


import org.apache.directory.api.ldap.model.constants.SaslQoP;
import org.apache.directory.api.ldap.model.constants.SaslSecurityStrength;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.ConnectionParameter.AuthenticationMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.Krb5Configuration;
import org.apache.directory.studio.connection.core.ConnectionParameter.Krb5CredentialConfiguration;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.io.ConnectionWrapper;
import org.apache.directory.studio.connection.core.io.api.DirectoryApiConnectionWrapper;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IActionFilter;


// ── CLASS: Connection — HAN SOLO PUNCHES THE FALCON TO HYPERSPACE ────────────
// Han sits at the Millennium Falcon's controls; every dial, switch, and nav
// coordinate in that cockpit represents one aspect of the jump to a server.
// This class IS the Falcon: it bundles the nav parameters (host, port, auth)
// with the actual hyperdrive (ConnectionWrapper) that carries bytes to the server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The runtime model for a single saved LDAP connection.
 * We hold the connection's settings (via {@link ConnectionParameter}) and
 * the live network channel (via {@link ConnectionWrapper}) in one place.
 * Think of this class as the Millennium Falcon: the parameters are the nav
 * coordinates Han punches in, and the wrapper is the hyperdrive that actually
 * gets us there.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Connection implements ConnectionPropertyPageProvider, IAdaptable
{
    /**
     * Enum for alias dereferencing method.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum AliasDereferencingMethod
    {
        /** Never. */
        NEVER(0),

        /** Always. */
        ALWAYS(1),

        /** Finding. */
        FINDING(2),

        /** Search. */
        SEARCH(3);

        private final int ordinal;


        // ── ALIAS ENUM CONSTRUCTOR — LABELING THE JUMP DIAL POSITION ─────────────────
        // Han's hyperdrive dial has four positions: never jump, always jump, jump on
        // target-acquire, and jump during search sweep.
        // We store each position as an integer ordinal so we can serialize it to disk.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Wires an alias-dereferencing strategy to a stable integer ordinal.
         * We need the ordinal so we can save and restore the setting as a plain number.
         *
         * @param ordinal  The integer that represents this position on the "alias dial."
         */
        private AliasDereferencingMethod( int ordinal )
        {
            this.ordinal = ordinal;
        }


        // ── READ THE DIAL POSITION — HAN READS THE JUMP SETTING ──────────────────────
        // Han glances at the hyperdrive dial to confirm the current jump mode.
        // He reads the physical position number from the panel.
        // We return the integer ordinal so callers can serialize this enum value.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Returns the integer ordinal for this alias-dereferencing mode.
         * We need this when saving the setting to a preference store or XML file —
         * enums don't serialize themselves, but ints do.
         *
         * <p>For example — Han checks the dial:</p>
         * <pre>
         *   dial.read() → 2  // FINDING mode
         * </pre>
         *
         * @return  The stable integer that identifies this dereferencing mode.
         */
        public int getOrdinal()
        {
            return ordinal;
        }


        // ── RESTORE THE DIAL FROM A NUMBER — CHEWIE READS BACK THE SAVED SETTING ────
        // Chewie consults the Falcon's saved flight plan and maps each stored digit
        // back to the dial position it represents.
        // We do the same: convert a persisted int back to the right enum constant.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Looks up an {@link AliasDereferencingMethod} by its integer ordinal.
         * We use this when loading a saved connection from disk — the file stores an
         * int, and we need to get the proper enum value back out.
         *
         * <p>For example — Chewie re-reads the flight plan:</p>
         * <pre>
         *   storedValue = 2
         *   AliasDereferencingMethod.getByOrdinal(storedValue) → FINDING
         * </pre>
         *
         * @param ordinal  The integer ordinal we read from storage.
         * @return  The matching {@link AliasDereferencingMethod}, or {@code null}
         *          if the ordinal doesn't match any known mode.
         */
        public static AliasDereferencingMethod getByOrdinal( int ordinal )
        {
            switch ( ordinal )
            {
                case 0:
                    return NEVER;

                case 1:
                    return ALWAYS;

                case 2:
                    return FINDING;

                case 3:
                    return SEARCH;

                default:
                    return null;
            }
        }
    }

    /**
     * Enum for referral handling method.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum ReferralHandlingMethod
    {
        /** Ignore. */
        IGNORE(0),

        /** Follow automatically. */
        FOLLOW(1),

        /** Manage. */
        //MANAGE(2),

        /** Follow manually. */
        FOLLOW_MANUALLY(3);

        private final int ordinal;


        // ── REFERRAL ENUM CONSTRUCTOR — SETTING THE DETOUR POLICY ────────────────────
        // When the Falcon's destination turns out to be "try this other beacon instead,"
        // Han needs a policy: ignore the redirect, follow it automatically, or ask first.
        // We record that policy as a stable int ordinal for persistence.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Wires a referral-handling strategy to a stable integer ordinal.
         * LDAP referrals are like hyperspace beacon redirects — the server says
         * "what you want is actually over there." We need to know how to respond.
         *
         * @param ordinal  The integer that identifies this referral policy on disk.
         */
        private ReferralHandlingMethod( int ordinal )
        {
            this.ordinal = ordinal;
        }


        // ── READ THE REFERRAL DIAL — HAN CHECKS THE DETOUR SWITCH ────────────────────
        // Han glances at the navigation panel to see the current referral policy.
        // He reads the integer that labels the active setting.
        // We return it so callers can persist the chosen mode.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Returns the integer ordinal for this referral-handling mode.
         * We use this when saving the connection's referral policy to preferences or XML.
         *
         * @return  The stable integer that identifies this referral mode.
         */
        public int getOrdinal()
        {
            return ordinal;
        }


        // ── RESTORE REFERRAL POLICY FROM INT — CHEWIE READS THE SAVED DETOUR ─────────
        // Chewie consults the Falcon's saved flight plan and maps each stored number
        // back to the referral policy it represents.
        // We convert a persisted int back to the right enum constant.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Looks up a {@link ReferralHandlingMethod} by its integer ordinal.
         * We call this when loading a saved connection — the file holds an int,
         * and we need the enum back.
         *
         * <p>For example — Chewie restores the flight plan:</p>
         * <pre>
         *   storedValue = 1
         *   ReferralHandlingMethod.getByOrdinal(storedValue) → FOLLOW
         * </pre>
         *
         * @param ordinal  The integer ordinal read from storage.
         * @return  The matching {@link ReferralHandlingMethod}, or {@code null}
         *          if the ordinal doesn't match any known mode.
         */
        public static ReferralHandlingMethod getByOrdinal( int ordinal )
        {
            switch ( ordinal )
            {
                case 0:
                    return IGNORE;

                case 1:
                    return FOLLOW;

                case 2:
                    return FOLLOW_MANUALLY;

                case 3:
                    return FOLLOW_MANUALLY;

                default:
                    return null;
            }
        }
    }

    /** The connection parameter */
    private ConnectionParameter connectionParameter;

    /** The connection wrapper */
    private ConnectionWrapper connectionWrapper;

    /** The detected connection properties */
    private DetectedConnectionProperties detectedConnectionProperties;


    // ── CONSTRUCTOR — LOADING THE NAV COORDINATES INTO THE FALCON ────────────────
    // Han slides the nav data cartridge into the Falcon's computer — host, port,
    // credentials, encryption mode, all of it.
    // The Falcon (Connection) is now configured and ready to jump, but hasn't fired
    // the engines yet; the actual network connection opens later.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new Connection backed by the given parameters.
     * We're not opening a network socket yet — just loading the nav data
     * into the Falcon's computer so we know where to jump when the time comes.
     *
     * <p>For example — Han loads coordinates:</p>
     * <pre>
     *   ConnectionParameter params = new ConnectionParameter("My LDAP", "ldap.example.com", 389, ...);
     *   Connection conn = new Connection(params);
     *   // ready to call conn.getConnectionWrapper().connect()
     * </pre>
     *
     * @param connectionParameter  Everything we need to reach the LDAP server —
     *                             host, port, auth method, credentials, timeouts.
     */
    public Connection( ConnectionParameter connectionParameter )
    {
        this.connectionParameter = connectionParameter;
        detectedConnectionProperties = new DetectedConnectionProperties( this );
    }


    // ── CLONE — CHEWIE DUPLICATES THE FALCON'S NAV CARTRIDGE ─────────────────────
    // Chewie makes a copy of the Falcon's nav data so Han can edit the duplicate
    // without corrupting the live flight plan still in the computer.
    // We copy all the parameters into a fresh Connection so the UI can show an
    // "edit connection" dialog without modifying the running connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of this connection.
     * We need this when the user opens an "Edit Connection" dialog — we give
     * the dialog a clone so it can mutate freely without affecting the live object.
     *
     * <p>For example — Chewie duplicates the cartridge:</p>
     * <pre>
     *   Connection editCopy = (Connection) liveConnection.clone();
     *   dialog.populate(editCopy);  // safe to modify
     * </pre>
     *
     * @return  A new {@link Connection} with its own {@link ConnectionParameter}
     *          carrying all the same settings as this one.
     */
    public Object clone()
    {
        ConnectionParameter cp = new ConnectionParameter( getName(), getHost(), getPort(), getEncryptionMethod(),
            getAuthMethod(), getBindPrincipal(), getBindPassword(), getSaslRealm(), isReadOnly(),
            getConnectionParameter().getExtendedProperties() , getTimeoutMillis() );

        return new Connection( cp );
    }


    // ── GET THE HYPERDRIVE — HAN GRABS THE THROTTLE ───────────────────────────────
    // Han reaches for the hyperdrive lever — the piece of the Falcon that actually
    // pushes bytes across the network.
    // We return the ConnectionWrapper (our Apache Directory API wrapper) that handles
    // all LDAP operations: search, add, modify, delete.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live network connection wrapper for this connection.
     * The wrapper is where LDAP operations actually happen — search, bind, modify.
     * We create it lazily (first call) using the Apache Directory API.
     *
     * <p>For example — Han grabs the throttle:</p>
     * <pre>
     *   ConnectionWrapper wrapper = connection.getConnectionWrapper();
     *   wrapper.search(baseDn, filter, controls, monitor);
     * </pre>
     *
     * @return  The {@link ConnectionWrapper} that talks to the LDAP server.
     */
    public ConnectionWrapper getConnectionWrapper()
    {
        return getDirectoryApiConnectionWrapper();
    }


    // ── LAZY-INIT THE HYPERDRIVE — CHEWIE INSTALLS THE MOTIVATOR IF MISSING ──────
    // Chewie checks whether the hyperdrive motivator is already installed; if not,
    // he slaps in the DirectoryApi variant so the Falcon can actually jump.
    // We lazily create a DirectoryApiConnectionWrapper the first time it's needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns (or lazily creates) the {@link DirectoryApiConnectionWrapper} for this connection.
     * We use the Apache Directory API as our LDAP implementation.
     * If the wrapper hasn't been created yet — or is the wrong type — we swap it in now.
     *
     * <p>For example — Chewie installs the motivator:</p>
     * <pre>
     *   if (!(connectionWrapper instanceof DirectoryApiConnectionWrapper)) {
     *       connectionWrapper = new DirectoryApiConnectionWrapper(this);
     *   }
     * </pre>
     *
     * @return  A {@link ConnectionWrapper} that is guaranteed to be a
     *          {@link DirectoryApiConnectionWrapper}.
     */
    private ConnectionWrapper getDirectoryApiConnectionWrapper()
    {
        if ( !( connectionWrapper instanceof DirectoryApiConnectionWrapper ) )
        {
            connectionWrapper = new DirectoryApiConnectionWrapper( this );
        }

        return connectionWrapper;
    }


    // ── GET THE NAV CARTRIDGE — HAN READS THE FLIGHT PLAN ─────────────────────────
    // Han pulls out the nav cartridge to inspect all the jump parameters.
    // He needs to know what's stored before he commits to the jump.
    // We return the raw ConnectionParameter so callers can read or update settings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ConnectionParameter} that holds all this connection's settings.
     * You'd use this when you need direct access to the full parameter bean —
     * for example when saving to disk or editing in a dialog.
     *
     * @return  The {@link ConnectionParameter} backing this connection.
     */
    public ConnectionParameter getConnectionParameter()
    {
        return connectionParameter;
    }


    // ── SWAP THE NAV CARTRIDGE — HAN UPDATES THE FLIGHT PLAN MID-MISSION ─────────
    // Han swaps in a new nav cartridge while the Falcon is still running —
    // new destination, new credentials, same ship.
    // After swapping, he broadcasts to the crew that the flight plan changed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces this connection's settings with a new {@link ConnectionParameter}.
     * We fire a "connection updated" event afterward so any UI listening to this
     * connection (like the Connections view) refreshes automatically.
     *
     * <p>For example — Han swaps the cartridge:</p>
     * <pre>
     *   connection.setConnectionParameter(newParams);
     *   // ConnectionEventRegistry.fireConnectionUpdated() is called for us
     * </pre>
     *
     * @param connectionParameter  The new parameter bean to adopt.
     */
    public void setConnectionParameter( ConnectionParameter connectionParameter )
    {
        this.connectionParameter = connectionParameter;
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── GET DETECTED PROPERTIES — R2 REPORTS WHAT HE FOUND ON THE SERVER ─────────
    // After plugging into the server, R2-D2 scans and reports back what he detected:
    // server type, supported controls, rootDSE attributes.
    // We return the DetectedConnectionProperties that were discovered at connect time.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the properties we auto-detected when we connected to the LDAP server.
     * Things like the vendor name, supported LDAP controls, and schema location
     * are discovered at connect time and stored here rather than in the user-supplied params.
     *
     * @return  The {@link DetectedConnectionProperties} for this connection.
     */
    public DetectedConnectionProperties getDetectedConnectionProperties()
    {
        return detectedConnectionProperties;
    }


    // ── SET DETECTED PROPERTIES — R2 UPDATES HIS SCAN RESULTS ───────────────────
    // R2-D2 finishes his scan of the server and writes his findings to the Falcon's
    // diagnostic log, replacing any stale data from the last mission.
    // We store the fresh DetectedConnectionProperties so other components can use them.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the auto-detected properties for this connection.
     * We call this after connecting, once we've read the server's rootDSE and
     * figured out what kind of server we're talking to.
     *
     * @param detectedConnectionProperties  The freshly detected server properties.
     */
    public void setDetectedConnectionProperties( DetectedConnectionProperties detectedConnectionProperties )
    {
        this.detectedConnectionProperties = detectedConnectionProperties;
    }


    // ── GET AUTH METHOD — HAN CHECKS WHICH ID CODE HE'S USING ───────────────────
    // Han glances at his ID badge to see whether he's going in as an anonymous
    // visitor, with a simple password, or with a SASL/Kerberos credential.
    // We delegate to ConnectionParameter and return the configured auth method.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the authentication method configured for this connection.
     * The method tells us how we'll prove our identity to the LDAP server —
     * anonymous, simple bind, SASL DIGEST-MD5, GSSAPI (Kerberos), etc.
     *
     * @return  The {@link AuthenticationMethod} we'll use when binding.
     */
    public AuthenticationMethod getAuthMethod()
    {
        return connectionParameter.getAuthMethod();
    }


    // ── GET BIND PASSWORD — HAN RETRIEVES HIS IMPERIAL OVERRIDE CODE ─────────────
    // Han digs in his jacket pocket for the Imperial override code he needs
    // to get past the docking bay security checkpoint.
    // We return the bind password stored in the connection parameter.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind password for this connection.
     * This is the secret we send to the LDAP server to authenticate ourselves.
     * It may be {@code null} for anonymous or Kerberos connections.
     *
     * @return  The bind password string, or {@code null} if not set.
     */
    public String getBindPassword()
    {
        return connectionParameter.getBindPassword();
    }


    // ── GET BIND PRINCIPAL — HAN SHOWS HIS ID CARD ───────────────────────────────
    // Han holds up his ID card at the checkpoint — it shows his name and rank,
    // which is what the guard checks before letting him through.
    // We return the bind principal (usually a DN like "cn=admin,dc=example,dc=com").
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bind principal (the "who are we" part of authentication).
     * For simple binds this is a Distinguished Name like {@code cn=admin,dc=example,dc=com}.
     * For SASL it might be a username or Kerberos principal.
     *
     * @return  The bind principal string, or {@code null} for anonymous.
     */
    public String getBindPrincipal()
    {
        return connectionParameter.getBindPrincipal();
    }


    // ── GET ENCRYPTION METHOD — HAN CHECKS IF THE SHIELDS ARE UP ─────────────────
    // Han glances at the shield display to see whether the Falcon is flying dark
    // (no encryption), behind full LDAPS shields, or using StartTLS shields.
    // We return the configured encryption mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the encryption method for this connection.
     * This tells us whether we connect with no encryption (NONE), SSL from the
     * start (LDAPS), or negotiate encryption after connecting (StartTLS).
     *
     * @return  The {@link EncryptionMethod} in use.
     */
    public EncryptionMethod getEncryptionMethod()
    {
        return connectionParameter.getEncryptionMethod();
    }


    // ── GET ID — THE FALCON'S HULL REGISTRATION NUMBER ───────────────────────────
    // Every ship in the galaxy has a unique hull registration number burned into
    // the transponder; the Falcon's is YT-1300 492727ZED.
    // We return the connection's UUID — its stable identity across rename/edit.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the unique ID for this connection.
     * This is a UUID that stays stable even if the user renames the connection.
     * Other parts of the app use it to refer to this connection unambiguously.
     *
     * @return  The UUID string that uniquely identifies this connection.
     */
    public String getId()
    {
        return connectionParameter.getId();
    }


    // ── GET HOST — HAN READS THE DESTINATION COORDINATES ─────────────────────────
    // Han reads the target star system coordinates off the nav computer —
    // that's where the Falcon is pointed.
    // We return the hostname or IP address of the LDAP server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the hostname or IP address of the LDAP server.
     * This is the "where are we jumping to" in our hyperspace analogy.
     *
     * @return  The server's hostname or IP, e.g. {@code "ldap.example.com"}.
     */
    public String getHost()
    {
        return connectionParameter.getHost();
    }


    // ── GET NAME — READING THE LABEL ON THE NAV CARTRIDGE ────────────────────────
    // Han squints at the label written on the side of the nav cartridge —
    // "Alderaan Run", "Kessel Run", etc.
    // We return the human-readable name the user gave this connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this connection as the user set it.
     * This is what shows up in the Connections view — a friendly label
     * like "Production LDAP" rather than a hostname.
     *
     * @return  The user-assigned connection name.
     */
    public String getName()
    {
        return connectionParameter.getName();
    }


    // ── GET PORT — READING THE DOCKING BAY NUMBER ────────────────────────────────
    // Han needs the docking bay number at the destination to know which airlock
    // to aim for when the Falcon drops out of hyperspace.
    // We return the TCP port the LDAP server is listening on.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the TCP port the LDAP server listens on.
     * Standard LDAP is 389; LDAPS is 636. Some servers use non-standard ports.
     *
     * @return  The port number, e.g. {@code 389} or {@code 636}.
     */
    public int getPort()
    {
        return connectionParameter.getPort();
    }


    // ── GET SASL REALM — HAN CHECKS HIS SECTOR CLEARANCE CODE ────────────────────
    // Han's Imperial code includes a sector clearance prefix — the realm that
    // scopes his credential to a specific authentication domain.
    // We return the SASL realm used for DIGEST-MD5 or GSSAPI authentication.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL realm for this connection.
     * A SASL realm is like an authentication "domain" — it scopes the credential.
     * Only relevant for SASL auth methods like DIGEST-MD5.
     *
     * @return  The SASL realm string, or {@code null} if not configured.
     */
    public String getSaslRealm()
    {
        return connectionParameter.getSaslRealm();
    }


    // ── GET SASL QOP — CHECKING THE SHIELD GRADE SETTING ─────────────────────────
    // Han checks whether the Falcon's shields are set to authentication-only,
    // integrity-check, or full privacy mode — each costs more power.
    // We return the SASL quality-of-protection level for secured SASL sessions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL quality-of-protection (QoP) for this connection.
     * QoP ranges from auth-only (cheapest) to auth-int (adds integrity) to
     * auth-conf (adds encryption). Only matters for SASL connections.
     *
     * @return  The configured {@link SaslQoP} level.
     */
    public SaslQoP getSaslQop()
    {
        return connectionParameter.getSaslQop();
    }


    // ── GET SASL SECURITY STRENGTH — CHECKING SHIELD POWER RATING ────────────────
    // Han checks what cipher strength his shields are rated for —
    // LOW, MEDIUM, or HIGH deflector output.
    // We return the SASL security strength required for this connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SASL security strength for this connection.
     * This controls which cipher suites are acceptable during SASL negotiation.
     * Only relevant for SASL connections.
     *
     * @return  The configured {@link SaslSecurityStrength}.
     */
    public SaslSecurityStrength getSaslSecurityStrength()
    {
        return connectionParameter.getSaslSecurityStrength();
    }


    // ── IS SASL MUTUAL AUTH — DOES HAN ALSO VERIFY THE GUARD'S BADGE ─────────────
    // Han doesn't just show his ID — he also checks the guard's credentials
    // to make sure he's not being lured into a trap.
    // We check whether the server must also prove its identity to us (mutual auth).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether SASL mutual authentication is required.
     * When true, the server must also prove its identity to us —
     * not just the other way around. Prevents man-in-the-middle attacks.
     *
     * @return  {@code true} if mutual authentication is required.
     */
    public boolean isSaslMutualAuthentication()
    {
        return connectionParameter.isSaslMutualAuthentication();
    }


    // ── GET KRB5 CREDENTIAL CONFIG — HOW HAN GOT HIS KERBEROS TICKET ─────────────
    // Han either already has an Imperial pass in his pocket (USE_NATIVE) or
    // needs to go to the checkpoint and get one issued (OBTAIN_TGT).
    // We return how we source the Kerberos TGT for GSSAPI authentication.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Kerberos credential configuration for this connection.
     * This tells us whether to use tickets already in the OS ticket cache
     * (USE_NATIVE) or to obtain a fresh TGT ourselves (OBTAIN_TGT).
     *
     * @return  The {@link Krb5CredentialConfiguration}.
     */
    public Krb5CredentialConfiguration getKrb5CredentialConfiguration()
    {
        return connectionParameter.getKrb5CredentialConfiguration();
    }


    // ── GET KRB5 CONFIGURATION — WHERE THE FALCON LOOKS FOR KDC INFO ─────────────
    // Han checks whether the Falcon should use the default Imperial databank,
    // a custom config file he brought along, or manually specified coordinates.
    // We return how Kerberos is configured for this connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the source of Kerberos configuration for this connection.
     * Options are: use the system/JVM default (DEFAULT), use a specific krb5.conf
     * file (FILE), or specify KDC host/realm directly (MANUAL).
     *
     * @return  The {@link Krb5Configuration} mode.
     */
    public Krb5Configuration getKrb5Configuration()
    {
        return connectionParameter.getKrb5Configuration();
    }


    // ── GET KRB5 CONFIG FILE — HAN READS FROM HIS CUSTOM IMPERIAL CODEBOOK ───────
    // Han has a custom codebook that overrides the standard Imperial databank —
    // he pulls out the path to that file so the system knows where to read from.
    // We return the file path to the krb5.conf to use.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file path to a custom Kerberos configuration file.
     * Only relevant when {@link #getKrb5Configuration()} returns FILE.
     * The file should be a standard krb5.conf.
     *
     * @return  The path to the krb5.conf file, or {@code null} if not set.
     */
    public String getKrb5ConfigurationFile()
    {
        return connectionParameter.getKrb5ConfigurationFile();
    }


    // ── GET KRB5 REALM — HAN CHECKS WHICH IMPERIAL SECTOR HE'S OPERATING IN ──────
    // The Empire's authentication authority is divided by sector; Han knows
    // which sector's KDC issues credentials for the destination system.
    // We return the Kerberos realm name for GSSAPI auth.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Kerberos realm for this connection.
     * The realm is the authentication domain, typically matching the DNS domain
     * in uppercase, e.g. {@code "EXAMPLE.COM"}.
     *
     * @return  The Kerberos realm string, or {@code null} if using defaults.
     */
    public String getKrb5Realm()
    {
        return connectionParameter.getKrb5Realm();
    }


    // ── GET KRB5 KDC HOST — HAN GETS THE ADDRESS OF THE IMPERIAL AUTH STATION ────
    // Han needs the address of the sector's Key Distribution Center —
    // the authentication station that issues his tickets.
    // We return the hostname of the KDC server to use.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the hostname of the Kerberos Key Distribution Center (KDC).
     * Only relevant when Kerberos is configured in MANUAL mode.
     *
     * @return  The KDC hostname, or {@code null} if using system defaults.
     */
    public String getKrb5KdcHost()
    {
        return connectionParameter.getKrb5KdcHost();
    }


    // ── GET KRB5 KDC PORT — HAN FINDS THE IMPERIAL AUTH STATION'S DOCKING BAY ────
    // Han needs the specific port number on the KDC server to reach the
    // authentication service — defaults to port 88 (standard Kerberos).
    // We return the KDC port for MANUAL Kerberos configuration.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the port number of the Kerberos KDC.
     * Defaults to 88 (the standard Kerberos port). Only used in MANUAL mode.
     *
     * @return  The KDC port number.
     */
    public int getKrb5KdcPort()
    {
        return connectionParameter.getKrb5KdcPort();
    }


    // ── IS READ ONLY — HAN CHECKS IF HE'S ON A RECONNAISSANCE MISSION ────────────
    // Han's orders sometimes say "observe only, don't touch anything" —
    // he won't fire, won't modify, just look.
    // We return whether this connection is locked to read-only LDAP operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this connection is restricted to read-only operations.
     * When {@code true} we refuse to execute any modify, add, or delete operations.
     * Useful for connecting to production servers without risking accidental writes.
     *
     * @return  {@code true} if this connection is read-only.
     */
    public boolean isReadOnly()
    {
        return connectionParameter.isReadOnly();
    }


    // ── GET TIMEOUT — HAN SETS HIS PATIENCE LIMIT FOR THE JUMP ──────────────────
    // Han gives the motivator exactly so many seconds to warm up; if it doesn't
    // respond in time he aborts the jump rather than sitting forever.
    // We return the maximum milliseconds to wait for an LDAP operation to complete.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the operation timeout in milliseconds.
     * If the LDAP server doesn't respond within this window, we give up
     * and report an error rather than blocking the UI indefinitely.
     * Default is 30,000 ms (30 seconds).
     *
     * @return  The timeout in milliseconds.
     */
    public long getTimeoutMillis()
    {
        return connectionParameter.getTimeoutMillis();
    }


    // ── SET AUTH METHOD — HAN SWITCHES ID BADGES ──────────────────────────────────
    // Han swaps out his ID badge mid-mission — maybe he switches from anonymous
    // to SASL because the checkpoint policy changed.
    // We update the parameter and fire an update event so the UI reflects the change.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the authentication method and notifies listeners.
     * We call this when the user edits the connection's auth settings in the dialog.
     *
     * @param authMethod  The new authentication method to use for this connection.
     */
    public void setAuthMethod( AuthenticationMethod authMethod )
    {
        connectionParameter.setAuthMethod( authMethod );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET BIND PASSWORD — HAN UPDATES HIS IMPERIAL ACCESS CODE ─────────────────
    // The Empire rotated its access codes; Han punches in the new one
    // so the Falcon's transponder will pass the checkpoint.
    // We update the stored password and fire an update event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the bind password and notifies listeners.
     * We call this when the user changes the password in the connection editor.
     *
     * @param bindPassword  The new password to use when binding to the LDAP server.
     */
    public void setBindPassword( String bindPassword )
    {
        connectionParameter.setBindPassword( bindPassword );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET BIND PRINCIPAL — HAN UPDATES HIS IDENTITY ON THE MANIFEST ────────────
    // Han changes the name on his manifest — he's not flying under "Han Solo"
    // any more, but under a new alias for the mission.
    // We update the bind principal (DN or username) and fire an update event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the bind principal and notifies listeners.
     * The principal is the "who are we" part of authentication —
     * usually a Distinguished Name like {@code cn=admin,dc=example,dc=com}.
     *
     * @param bindPrincipal  The new bind principal to use.
     */
    public void setBindPrincipal( String bindPrincipal )
    {
        connectionParameter.setBindPrincipal( bindPrincipal );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET ENCRYPTION METHOD — HAN RAISES OR LOWERS THE SHIELDS ─────────────────
    // Han decides whether to fly dark (no shields), behind LDAPS full shields,
    // or to negotiate shields en route with StartTLS.
    // We update the encryption setting and fire an update event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the encryption method and notifies listeners.
     * We call this when the user changes SSL/TLS settings in the connection editor.
     *
     * @param encryptionMethod  The new encryption mode — NONE, LDAPS, or START_TLS.
     */
    public void setEncryptionMethod( EncryptionMethod encryptionMethod )
    {
        connectionParameter.setEncryptionMethod( encryptionMethod );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET HOST — HAN REPROGRAMS THE DESTINATION COORDINATES ────────────────────
    // Han changes the target star system in the nav computer — new mission,
    // new destination, same Falcon.
    // We update the host and fire an update event so the UI refreshes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the server hostname and notifies listeners.
     * We call this when the user edits the host field in the connection editor.
     *
     * @param host  The new hostname or IP address of the LDAP server.
     */
    public void setHost( String host )
    {
        connectionParameter.setHost( host );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET NAME — HAN RE-LABELS THE NAV CARTRIDGE ───────────────────────────────
    // Han scratches out the old label on the cartridge and writes a new one —
    // renaming the connection while keeping all the settings intact.
    // We update the display name and fire an update event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the display name of this connection and notifies listeners.
     * This is the friendly label shown in the Connections view.
     * Renaming does not affect the connection's unique ID.
     *
     * @param name  The new display name for this connection.
     */
    public void setName( String name )
    {
        connectionParameter.setName( name );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET PORT — HAN UPDATES THE DOCKING BAY NUMBER ────────────────────────────
    // The destination station moved its docking bays; Han updates the bay number
    // in the nav computer so the Falcon aims at the right airlock.
    // We update the port and fire an update event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the server port and notifies listeners.
     * We call this when the user edits the port field in the connection editor.
     *
     * @param port  The new TCP port number for the LDAP server.
     */
    public void setPort( int port )
    {
        connectionParameter.setPort( port );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET SASL REALM — HAN UPDATES HIS SECTOR CLEARANCE PREFIX ─────────────────
    // Han's sector clearance prefix changes when he crosses into a new auth domain;
    // he punches the new realm code into the transponder.
    // We update the SASL realm and fire an update event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the SASL realm and notifies listeners.
     * The realm scopes SASL credentials to a specific authentication domain.
     *
     * @param realm  The new SASL realm string.
     */
    public void setSaslRealm( String realm )
    {
        connectionParameter.setSaslRealm( realm );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET READ ONLY — HAN SWITCHES BETWEEN RECON AND ACTIVE MODES ──────────────
    // Han flips the switch between reconnaissance mode (look, don't touch)
    // and active mode (weapons free).
    // We update the read-only flag and fire an update event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the read-only flag and notifies listeners.
     * When set to {@code true}, the connection will refuse any write operations
     * (add, modify, delete) to protect production data.
     *
     * @param isReadOnly  {@code true} to lock this connection to read-only.
     */
    public void setReadOnly( boolean isReadOnly )
    {
        connectionParameter.setReadOnly( isReadOnly );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── SET TIMEOUT — HAN ADJUSTS HIS PATIENCE DIAL ──────────────────────────────
    // Han dials in how long he'll wait for the motivator to warm up before
    // aborting the jump — shorter on a smuggling run, longer for a recon mission.
    // We update the timeout and fire an update event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the operation timeout and notifies listeners.
     * LDAP operations that exceed this deadline are cancelled and treated as errors.
     *
     * @param timeoutMillis  The new timeout in milliseconds (e.g. 30000 for 30 seconds).
     */
    public void setTimeoutMillis( long timeoutMillis )
    {
        connectionParameter.setTimeoutMillis( timeoutMillis );
        ConnectionEventRegistry.fireConnectionUpdated( this, this );
    }


    // ── GET ADAPTER — C-3PO TRANSLATES THE FALCON INTO WHATEVER THE CALLER NEEDS ─
    // C-3PO can translate the Falcon's manifest into any of over six million forms
    // of communication — Eclipse's adapter pattern does the same thing.
    // We return this Connection as a Connection, or route to the IActionFilter.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adapts this connection to the requested interface type.
     * Eclipse uses adapters all over the place to avoid hard coupling — callers
     * ask for {@code IActionFilter} or {@code Connection}, and we hand back the
     * right object without exposing our internals.
     *
     * <p>For example — C-3PO translates:</p>
     * <pre>
     *   Object filter = connection.getAdapter(IActionFilter.class);
     *   // → ConnectionActionFilterAdapter.getInstance()
     * </pre>
     *
     * @param adapter  The class the caller wants an instance of.
     * @return  This connection (for {@code Connection.class}), the shared
     *          {@link ConnectionActionFilterAdapter} (for {@code IActionFilter}),
     *          or {@code null} if we can't adapt.
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter )
    {
        if ( adapter == Connection.class )
        {
            return this;
        }
        else if ( adapter.isAssignableFrom( IActionFilter.class ) )
        {
            return ConnectionActionFilterAdapter.getInstance();
        }

        return null;
    }


    // ── GET URL — HAN COMPUTES THE FULL JUMP COORDINATES AS AN LDAP URL ──────────
    // Han feeds the destination system and docking bay number into the nav
    // computer, which spits out the full jump coordinates as a single formatted string.
    // We build an LdapUrl from this connection's host and port.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link LdapUrl} for this connection's host and port.
     * Useful when we need to represent the server as a URL string,
     * for example in log messages or when passing coords to other components.
     *
     * <p>For example — Han computes the coordinates:</p>
     * <pre>
     *   LdapUrl url = connection.getUrl();
     *   // → ldap://ldap.example.com:389
     * </pre>
     *
     * @return  An {@link LdapUrl} set to this connection's host and port.
     */
    public LdapUrl getUrl()
    {
        LdapUrl url = new LdapUrl();
        url.setHost( getHost() );
        url.setPort( getPort() );

        return url;
    }

}
