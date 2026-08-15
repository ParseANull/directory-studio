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


import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PropertyResourceBundle;

import org.apache.directory.api.ldap.model.exception.LdapTlsHandshakeFailCause;
import org.apache.directory.studio.connection.core.event.CoreEventRunner;
import org.apache.directory.studio.connection.core.event.EventRunner;
import org.apache.directory.studio.connection.core.io.api.LdifModificationLogger;
import org.apache.directory.studio.connection.core.io.api.LdifSearchLogger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;


// ── CLASS: ConnectionCorePlugin — THE FALCON'S CENTRAL COMPUTER ───────────────
// The Millennium Falcon's central computer boots up when you power on the ship,
// wires together every subsystem (nav computer, comms, shields, hyperdrive),
// and shuts everything down cleanly when you power off.
// This OSGi bundle activator does the same: it starts all the core services
// (ConnectionManager, FolderManager, trust stores, event runner, loggers) when
// Eclipse loads the plugin, and tears them down on stop.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator (plugin) for connection.core.
 * We start and stop the plugin lifecycle here: creating the {@link ConnectionManager},
 * {@link ConnectionFolderManager}, trust stores, event runner, and exposing
 * the auth/referral/certificate handlers that the UI layer plugs into.
 * Think of this class as the Falcon's central computer — it boots the whole
 * ship and provides a single access point ({@link #getDefault()}) for every subsystem.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionCorePlugin extends Plugin
{

    /** The file name of the permanent trust store */
    private static final String PERMANENT_TRUST_STORE = "permanent.jks"; //$NON-NLS-1$

    /** The password of the permanent trust store */
    private static final String PERMANENT_TRUST_STORE_PASSWORD = "changeit"; //$NON-NLS-1$

    /** The shared instance */
    private static ConnectionCorePlugin plugin;

    /** The connection manager */
    private ConnectionManager connectionManager;

    /** The connection folder manager */
    private ConnectionFolderManager connectionFolderManager;

    /** The passwords keystore manager */
    private PasswordsKeyStoreManager passwordsKeyStoreManager;

    /** The permanent trust store */
    private StudioKeyStoreManager permanentTrustStoreManager;

    /** The session trust store */
    private StudioKeyStoreManager sessionTrustStoreManager;

    /** The event runner. */
    private EventRunner eventRunner;

    /** The authentication handler */
    private IAuthHandler authHandler;

    /** The referral handler */
    private IReferralHandler referralHandler;

    /** The certificate handler */
    private ICertificateHandler certificateHandler;

    /** The LDAP loggers. */
    private List<ILdapLogger> ldapLoggers;

    /** The connection listeners. */
    private List<IConnectionListener> connectionListeners;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── CONSTRUCTOR — THE FALCON'S COMPUTER INITIALIZES ITSELF ────────────────────
    // When the Falcon's computer first powers on it stores a reference to itself
    // so any other system on the ship can find it.
    // We store a reference to this singleton instance in the static field.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the plugin instance and registers it as the shared singleton.
     * Eclipse calls this once per plugin lifecycle — don't call it yourself.
     */
    public ConnectionCorePlugin()
    {
        plugin = this;
    }


    // ── START — BOOTING THE FALCON ────────────────────────────────────────────────
    // Han flips the master switch and the Falcon's computer boots every subsystem
    // in sequence: event bus, connection registry, folder registry, key stores.
    // We initialize all the core services during OSGi bundle activation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse/OSGi when this plugin bundle is activated.
     * We initialize the event runner, connection manager, folder manager, password
     * keystore, and both trust stores (permanent + session).
     * We also force-start the Apache Directory API codec bundles that some versions
     * of OSGi fail to activate automatically.
     *
     * @param context  The OSGi bundle context provided by the framework.
     * @throws Exception  if any subsystem fails to initialize.
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        if ( eventRunner == null )
        {
            eventRunner = new CoreEventRunner();
        }

        if ( connectionManager == null )
        {
            connectionManager = new ConnectionManager();
        }

        if ( connectionFolderManager == null )
        {
            connectionFolderManager = new ConnectionFolderManager();
        }

        if ( passwordsKeyStoreManager == null )
        {
            passwordsKeyStoreManager = new PasswordsKeyStoreManager();
        }

        if ( permanentTrustStoreManager == null )
        {
            permanentTrustStoreManager = StudioKeyStoreManager.createFileKeyStoreManager( PERMANENT_TRUST_STORE,
                PERMANENT_TRUST_STORE_PASSWORD );
        }

        if ( sessionTrustStoreManager == null )
        {
            sessionTrustStoreManager = StudioKeyStoreManager.createMemoryKeyStoreManager();
        }

        // Nasty hack to get the API bundles started. DO NOT REMOVE
        Platform.getBundle( "org.apache.directory.api.ldap.codec.core" ).start();
        Platform.getBundle( "org.apache.directory.api.ldap.extras.codec" ).start();
        Platform.getBundle( "org.apache.directory.api.ldap.net.mina" ).start();
    }


    // ── STOP — POWERING DOWN THE FALCON ───────────────────────────────────────────
    // Han powers down the Falcon: he first disconnects every active connection
    // (so we don't leave dangling network sockets), then nulls out every subsystem.
    // We close all open connections and tear down all services during bundle stop.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse/OSGi when this plugin bundle is deactivated.
     * We disconnect all open connections so the LDAP server doesn't see zombie sockets,
     * then null out all subsystems so they can be garbage-collected.
     *
     * @param context  The OSGi bundle context provided by the framework.
     * @throws Exception  if the superclass stop fails.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );

        if ( eventRunner != null )
        {
            eventRunner = null;
        }

        if ( connectionManager != null )
        {
            Connection[] connections = connectionManager.getConnections();
            for ( int i = 0; i < connections.length; i++ )
            {
                connections[i].getConnectionWrapper().disconnect();
            }
            connectionManager = null;
        }

        if ( connectionFolderManager != null )
        {
            connectionFolderManager = null;
        }

        if ( permanentTrustStoreManager != null )
        {
            permanentTrustStoreManager = null;
        }

        if ( sessionTrustStoreManager != null )
        {
            sessionTrustStoreManager = null;
        }
    }


    // ── GET DEFAULT — REACHING THE FALCON'S MASTER CONTROL PANEL ─────────────────
    // Any crew member who needs to talk to the ship's systems goes to the master
    // control panel — there's only one, and it's always at the same spot.
    // We return the singleton plugin instance.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton instance of this plugin.
     * Every other class that needs a plugin service calls
     * {@code ConnectionCorePlugin.getDefault().getSomething()}.
     *
     * @return  The shared {@link ConnectionCorePlugin} instance.
     */
    public static ConnectionCorePlugin getDefault()
    {
        return plugin;
    }


    // ── GET CONNECTION MANAGER — CHEWIE'S ROUTE DATAPAD ──────────────────────────
    // Crew members who need the list of known server routes ask Chewie for his datapad.
    // We return the {@link ConnectionManager} that holds all registered connections.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ConnectionManager} that manages all saved connections.
     * Use this to add, remove, or look up connections by id or name.
     *
     * @return  The shared {@link ConnectionManager}.
     */
    public ConnectionManager getConnectionManager()
    {
        return connectionManager;
    }


    // ── GET FOLDER MANAGER — CHEWIE'S FILING CABINET ─────────────────────────────
    // Crew members who need the folder hierarchy ask Chewie for his filing cabinet.
    // We return the {@link ConnectionFolderManager} that manages the folder tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ConnectionFolderManager} that manages the folder hierarchy.
     * Use this to add, remove, or navigate connection folders.
     *
     * @return  The shared {@link ConnectionFolderManager}.
     */
    public ConnectionFolderManager getConnectionFolderManager()
    {
        return connectionFolderManager;
    }


    // ── GET EVENT RUNNER — THE FALCON'S COMMS RELAY ───────────────────────────────
    // The comms relay dispatches messages from the cockpit to every station.
    // We return the {@link EventRunner} that dispatches connection events to listeners.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EventRunner} used to dispatch connection lifecycle events.
     * The runner ensures events are delivered in the correct thread context
     * (important for UI-thread safety).
     *
     * @return  The shared {@link EventRunner}.
     */
    public EventRunner getEventRunner()
    {
        return eventRunner;
    }


    // ── GET PASSWORDS KEYSTORE — THE FALCON'S SAFE ────────────────────────────────
    // The Falcon has a hidden safe where the crew stores sensitive items —
    // access codes, passwords, identity documents.
    // We return the {@link PasswordsKeyStoreManager} that stores connection passwords.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link PasswordsKeyStoreManager} that stores connection bind passwords.
     * When password keystoring is enabled, passwords are kept here instead of in
     * the plain-text connections.xml file.
     *
     * @return  The shared {@link PasswordsKeyStoreManager}.
     */
    public PasswordsKeyStoreManager getPasswordsKeyStoreManager()
    {
        return passwordsKeyStoreManager;
    }


    // ── GET PERMANENT TRUST STORE — THE FALCON'S PERMANENT SHIELD REGISTRY ────────
    // The Falcon keeps a permanent registry of ships it unconditionally trusts —
    // entries survive reboots because they're written to disk.
    // We return the trust store that persists across sessions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the permanent {@link StudioKeyStoreManager} for trusted TLS certificates.
     * Certificates added here are trusted across Eclipse restarts — stored in permanent.jks.
     *
     * @return  The permanent trust store manager.
     */
    public StudioKeyStoreManager getPermanentTrustStoreManager()
    {
        return permanentTrustStoreManager;
    }


    // ── GET SESSION TRUST STORE — THE FALCON'S TEMPORARY CLEARANCE LIST ───────────
    // The Falcon also keeps a temporary clearance list for ships trusted only for
    // this mission — cleared when the ship powers down.
    // We return the in-memory trust store that doesn't survive Eclipse restarts.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the session-only {@link StudioKeyStoreManager} for temporarily trusted certificates.
     * Certificates here are trusted only for the current Eclipse session — they're gone on restart.
     *
     * @return  The session trust store manager.
     */
    public StudioKeyStoreManager getSessionTrustStoreManager()
    {
        return sessionTrustStoreManager;
    }


    // ── GET AUTH HANDLER — HAN CHECKS HOW TO PROVE IDENTITY ──────────────────────
    // Han needs someone to handle the docking-bay credential check.
    // If the UI hasn't registered a proper handler (e.g. in headless tests),
    // we fall back to a simple default that reads credentials from the parameters.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IAuthHandler} that supplies credentials at bind time.
     * If the UI has not registered a custom handler, we use a default one that reads
     * the bind principal and password directly from the {@link ConnectionParameter}.
     * The default handler returns {@code null} (cancels auth) if no password is configured.
     *
     * @return  The active {@link IAuthHandler}.
     */
    public IAuthHandler getAuthHandler()
    {
        if ( authHandler == null )
        {
            // if no authentication handler was set a default authentication handler is used
            // that only works if the bind password is stored within the connection parameters.
            authHandler = new IAuthHandler()
            {
                public ICredentials getCredentials( ConnectionParameter connectionParameter )
                {
                    if ( connectionParameter.getBindPrincipal() == null
                        || "".equals( connectionParameter.getBindPrincipal() ) ) //$NON-NLS-1$
                    {
                        return new Credentials( "", "", connectionParameter ); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    else if ( connectionParameter.getBindPassword() != null
                        && !"".equals( connectionParameter.getBindPassword() ) ) //$NON-NLS-1$
                    {
                        return new Credentials( connectionParameter.getBindPrincipal(), connectionParameter
                            .getBindPassword(), connectionParameter );
                    }
                    else
                    {
                        // no credentials provided in connection parameters
                        // returning null cancel the authentication
                        return null;
                    }
                }
            };
        }
        return authHandler;
    }


    // ── SET AUTH HANDLER — THE UI PLUGS IN ITS CREDENTIAL DIALOG ─────────────────
    // The UI registers its own auth handler that pops up a password dialog
    // instead of reading from the parameter bean.
    // We store the custom handler for use during bind operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a custom {@link IAuthHandler} that overrides the default.
     * The connection.ui plugin calls this during startup to provide a handler that
     * can show a password dialog when credentials are missing or expired.
     *
     * @param authHandler  The new auth handler to use.
     */
    public void setAuthHandler( IAuthHandler authHandler )
    {
        this.authHandler = authHandler;
    }


    // ── GET REFERRAL HANDLER — C-3PO HANDLES DIPLOMATIC REDIRECTS ────────────────
    // C-3PO intercepts referrals — "try this other address instead" messages —
    // and decides how to route them. If no protocol officer is on duty,
    // the default is to cancel and ignore the referral.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IReferralHandler} that decides what to do with LDAP referrals.
     * LDAP referrals are server redirects — "the data you want is over at this URL."
     * The default handler cancels referral chasing (returns {@code null}).
     * The UI registers a handler that asks the user which connection to follow.
     *
     * @return  The active {@link IReferralHandler}.
     */
    public IReferralHandler getReferralHandler()
    {
        if ( referralHandler == null )
        {
            // if no referral handler was set a default referral handler is used
            // that just cancels referral chasing
            referralHandler = new IReferralHandler()
            {
                public Connection getReferralConnection( List<String> referralUrls )
                {
                    // null cancels referral chasing
                    return null;
                }
            };
        }
        return referralHandler;
    }


    // ── SET REFERRAL HANDLER — THE UI PLUGS IN ITS REFERRAL DIALOG ───────────────
    // The UI registers its own referral handler that shows a dialog letting the
    // user pick which connection to follow the referral through.
    // We store the custom handler.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a custom {@link IReferralHandler}.
     * The connection.ui plugin calls this to provide a dialog-based referral resolver.
     *
     * @param referralHandler  The new referral handler to use.
     */
    public void setReferralHandler( IReferralHandler referralHandler )
    {
        this.referralHandler = referralHandler;
    }


    // ── GET CERTIFICATE HANDLER — THE FALCON'S SHIELD TRUST EVALUATOR ─────────────
    // The Falcon's shield computer evaluates incoming ships: friend, foe, or unknown?
    // If no evaluator is configured we default to "not trusted" (shields up, always).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ICertificateHandler} that evaluates untrusted TLS certificates.
     * When the server presents a certificate the JVM doesn't recognize, we ask this
     * handler what to do. The default handler always returns {@code TrustLevel.Not}.
     * The UI registers a handler that shows a dialog where the user can inspect and
     * optionally trust the certificate.
     *
     * @return  The active {@link ICertificateHandler}.
     */
    public ICertificateHandler getCertificateHandler()
    {
        if ( certificateHandler == null )
        {
            // if no certificate handler was set a default certificate handler is used
            // that just returns "No"
            certificateHandler = new ICertificateHandler()
            {
                public TrustLevel verifyTrustLevel( String host, X509Certificate[] certChain,
                    Collection<LdapTlsHandshakeFailCause> failCauses )
                {
                    return TrustLevel.Not;
                }
            };
        }
        return certificateHandler;
    }


    // ── SET CERTIFICATE HANDLER — THE UI PLUGS IN ITS TRUST DIALOG ───────────────
    // The UI registers its own certificate handler that shows the user the cert
    // details and lets them decide whether to trust it permanently or for this session.
    // We store the custom handler.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a custom {@link ICertificateHandler}.
     * The connection.ui plugin calls this to provide a user-visible trust dialog.
     *
     * @param certificateHandler  The new certificate handler to use.
     */
    public void setCertificateHandler( ICertificateHandler certificateHandler )
    {
        this.certificateHandler = certificateHandler;
    }


    // ── GET LDIF MODIFICATION LOGGER — FINDING THE CHANGE-LOG RECORDER ───────────
    // Among the crew, C-3PO is assigned to record every diplomatic exchange
    // (modification) in LDIF format.  We search the logger list for the right one.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdifModificationLogger} from the registered LDAP loggers, or
     * {@code null} if none is registered.
     * We use this to obtain a direct reference to the logger that writes modification LDIF files.
     *
     * @return  The {@link LdifModificationLogger}, or {@code null} if not found.
     */
    public LdifModificationLogger getLdifModificationLogger()
    {
        List<ILdapLogger> ldapLoggers = getLdapLoggers();
        for ( ILdapLogger ldapLogger : ldapLoggers )
        {
            if ( ldapLogger instanceof LdifModificationLogger )
            {
                return ( LdifModificationLogger ) ldapLogger;
            }
        }
        return null;
    }


    // ── GET LDIF SEARCH LOGGER — FINDING THE SEARCH-LOG RECORDER ─────────────────
    // C-3PO also keeps a separate log of every search query the crew sent out.
    // We search the logger list for the LdifSearchLogger.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdifSearchLogger} from the registered LDAP loggers, or
     * {@code null} if none is registered.
     *
     * @return  The {@link LdifSearchLogger}, or {@code null} if not found.
     */
    public LdifSearchLogger getLdifSearchLogger()
    {
        List<ILdapLogger> ldapLoggers = getLdapLoggers();
        for ( ILdapLogger ldapLogger : ldapLoggers )
        {
            if ( ldapLogger instanceof LdifSearchLogger )
            {
                return ( LdifSearchLogger ) ldapLogger;
            }
        }
        return null;
    }


    // ── GET LDAP LOGGERS — C-3PO ASSEMBLES THE LOGGING CREW ──────────────────────
    // C-3PO gathers every logging specialist registered through the Eclipse
    // extension point and brief them on the mission before the first LDAP call.
    // We lazily load all ILdapLogger extensions the first time they're needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all registered {@link ILdapLogger} instances.
     * We lazy-load them from the {@code org.apache.directory.studio.ldaplogger}
     * extension point on first call.
     * Each logger receives its id, name, and description from the extension metadata.
     *
     * @return  A list of all registered loggers (never {@code null}, may be empty).
     */
    public List<ILdapLogger> getLdapLoggers()
    {
        if ( ldapLoggers == null )
        {
            ldapLoggers = new ArrayList<ILdapLogger>();

            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry.getExtensionPoint( "org.apache.directory.studio.ldaplogger" ); //$NON-NLS-1$
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for ( IConfigurationElement member : members )
            {
                try
                {
                    ILdapLogger logger = ( ILdapLogger ) member.createExecutableExtension( "class" ); //$NON-NLS-1$
                    logger.setId( member.getAttribute( "id" ) ); //$NON-NLS-1$
                    logger.setName( member.getAttribute( "name" ) ); //$NON-NLS-1$
                    logger.setDescription( member.getAttribute( "description" ) ); //$NON-NLS-1$
                    ldapLoggers.add( logger );
                }
                catch ( Exception e )
                {
                    getLog().log(
                        new Status( IStatus.ERROR, ConnectionCoreConstants.PLUGIN_ID, 1,
                            Messages.error__unable_to_create_ldap_logger + member.getAttribute( "class" ), e ) ); //$NON-NLS-1$
                }
            }
        }

        return ldapLoggers;
    }


    // ── GET CONNECTION LISTENERS — OBI-WAN'S NETWORK OF FORCE SENSITIVES ─────────
    // Obi-Wan reaches out through the Force and gathers every sensitive who
    // needs to know when a connection opens or closes.
    // We lazy-load all IConnectionListener extensions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all registered {@link IConnectionListener} instances.
     * We lazy-load them from the {@code org.apache.directory.studio.connectionlistener}
     * extension point on first call.
     * Listeners are notified when connections open or close.
     *
     * @return  A list of all registered connection listeners (never {@code null}).
     */
    public List<IConnectionListener> getConnectionListeners()
    {
        if ( connectionListeners == null )
        {
            connectionListeners = new ArrayList<IConnectionListener>();

            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry
                .getExtensionPoint( "org.apache.directory.studio.connectionlistener" ); //$NON-NLS-1$
            IConfigurationElement[] members = extensionPoint.getConfigurationElements();
            for ( IConfigurationElement member : members )
            {
                try
                {
                    IConnectionListener listener = ( IConnectionListener ) member.createExecutableExtension( "class" ); //$NON-NLS-1$
                    connectionListeners.add( listener );
                }
                catch ( Exception e )
                {
                    getLog().log(
                        new Status( IStatus.ERROR, ConnectionCoreConstants.PLUGIN_ID, 1,
                            Messages.error__unable_to_create_connection_listener + member.getAttribute( "class" ), //$NON-NLS-1$
                            e ) );
                }
            }
        }

        return connectionListeners;
    }


    // ── GET PLUGIN PROPERTIES — R2 READS THE FALCON'S SPEC PLATE ────────────────
    // R2-D2 scans the Falcon's specification plate to read build metadata —
    // version numbers, vendor info, capability flags.
    // We lazy-load the plugin.properties resource bundle.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link PropertyResourceBundle} loaded from plugin.properties.
     * We use this to read static build metadata (version, vendor) from the bundle.
     * Loaded lazily on first access; errors are logged and we return {@code null}.
     *
     * @return  The {@link PropertyResourceBundle}, or {@code null} if loading failed.
     */
    public PropertyResourceBundle getPluginProperties()
    {
        if ( properties == null )
        {
            try
            {
                properties = new PropertyResourceBundle( FileLocator.openStream( this.getBundle(), new Path(
                    "plugin.properties" ), false ) ); //$NON-NLS-1$
            }
            catch ( IOException e )
            {
                // We can't use the PLUGIN_ID constant since loading the plugin.properties file has failed,
                // So we're using a default plugin id.
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.connection.core", Status.OK, //$NON-NLS-1$
                    Messages.error__unable_to_get_plugin_properties, e ) );
            }
        }

        return properties;
    }


    // ── GET DEFAULT KRB5 LOGIN MODULE — FINDING THE KERBEROS COMMAND CENTER ───────
    // The Empire has two different regional headquarters that issue Kerberos tickets:
    // Sun's and Apache Harmony's. We probe both to find whichever one is available.
    // We probe the JVM classpath and return the class name of the usable module.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the class name of the best available Kerberos login module on this JVM.
     * We try Sun's module first, then Apache Harmony's — whichever is findable via
     * {@code Class.forName()} wins. Returns an empty string if neither is present.
     *
     * @return  The fully-qualified class name of the Kerberos login module, or {@code ""}.
     */
    public String getDefaultKrb5LoginModule()
    {
        String defaultKrb5LoginModule = ""; //$NON-NLS-1$

        try
        {
            String sun = "com.sun.security.auth.module.Krb5LoginModule"; //$NON-NLS-1$
            Class.forName( sun );
            defaultKrb5LoginModule = sun;
        }
        catch ( ClassNotFoundException e )
        {
        }
        try
        {
            String apache = "org.apache.harmony.auth.module.Krb5LoginModule"; //$NON-NLS-1$
            Class.forName( apache );
            defaultKrb5LoginModule = apache;
        }
        catch ( ClassNotFoundException e )
        {
        }

        return defaultKrb5LoginModule;
    }


    // ── GET DEFAULT SCOPE PREFERENCES — READING THE FACTORY SETTINGS ─────────────
    // The Falcon ships with factory-default settings for every dial and switch;
    // this returns the node in the preference store where defaults live.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the default-scope Eclipse preferences node for this plugin.
     * Default-scope preferences hold the factory defaults that are used when no
     * instance-scope (user) override exists.
     *
     * @return  The default-scope {@link IEclipsePreferences} node.
     */
    public IEclipsePreferences getDefaultScopePreferences()
    {
        return DefaultScope.INSTANCE.getNode( ConnectionCoreConstants.PLUGIN_ID );
    }


    // ── FLUSH DEFAULT SCOPE PREFERENCES — CHEWIE WRITES THE FACTORY SETTINGS ──────
    // Chewie saves the factory-default settings to the backing store so they
    // survive a restart.
    // We flush the default-scope preferences node.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Flushes the default-scope preference node to its backing store.
     * We call this after programmatically writing default values to ensure
     * they're persisted immediately.
     *
     * @throws RuntimeException  wrapping a {@link BackingStoreException} if the flush fails.
     */
    public void flushDefaultScopePreferences()
    {
        try
        {
            getDefaultScopePreferences().flush();
        }
        catch ( BackingStoreException e )
        {
            throw new RuntimeException( e );
        }
    }


    // ── GET INSTANCE SCOPE PREFERENCES — READING THE USER'S CUSTOM SETTINGS ───────
    // The crew has their own custom settings on top of the factory defaults.
    // We return the instance-scope node where user overrides live.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the instance-scope Eclipse preferences node for this plugin.
     * Instance-scope preferences hold user-specific overrides that trump defaults.
     *
     * @return  The instance-scope {@link IEclipsePreferences} node.
     */
    public IEclipsePreferences getInstanceScopePreferences()
    {
        return InstanceScope.INSTANCE.getNode( ConnectionCoreConstants.PLUGIN_ID );
    }


    // ── FLUSH INSTANCE SCOPE PREFERENCES — CHEWIE WRITES THE USER'S SETTINGS ──────
    // Chewie saves the crew's custom settings to the backing store.
    // We flush the instance-scope preferences node.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Flushes the instance-scope preference node to its backing store.
     *
     * @throws RuntimeException  wrapping a {@link BackingStoreException} if the flush fails.
     */
    public void flushInstanceScopePreferences()
    {
        try
        {
            getInstanceScopePreferences().flush();
        }
        catch ( BackingStoreException e )
        {
            throw new RuntimeException( e );
        }
    }


    // ── GET MODIFICATION LOGS FILE COUNT — HOW MANY LOG FILES TO KEEP ─────────────
    // C-3PO only keeps the last N modification log files — older ones get rotated out.
    // We read the preference that controls how many rotating log files to keep.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of rotating modification log files to keep.
     * Older files are discarded when this limit is exceeded (JUL-style rotation).
     *
     * @return  The file count preference value.
     */
    public int getModificationLogsFileCount()
    {
        return Platform.getPreferencesService().getInt( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_COUNT, -1, null );
    }


    // ── GET MODIFICATION LOGS FILE SIZE — MAX SIZE PER LOG FILE ──────────────────
    // Each log file has a max size; when it fills up C-3PO starts a new one.
    // We read the preference for max file size in kilobytes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum size in kilobytes for each modification log file.
     * When a file exceeds this size, a new file is started.
     *
     * @return  The file size preference value in kilobytes.
     */
    public int getModificationLogsFileSize()
    {
        return Platform.getPreferencesService().getInt( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_FILE_SIZE, -1, null );
    }


    // ── IS MODIFICATION LOGS ENABLED — IS C-3PO RECORDING CHANGES ───────────────
    // Han can tell C-3PO to stop recording — for example when running bulk imports
    // where logging every change would be too noisy.
    // We return whether modification logging is enabled.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether LDAP modification logging is enabled.
     * When disabled, no LDIF modification log files are written.
     *
     * @return  {@code true} if modification logging is on.
     */
    public boolean isModificationLogsEnabled()
    {
        return Platform.getPreferencesService().getBoolean( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_ENABLE, true, null );
    }


    // ── GET MASKED ATTRIBUTES — C-3PO KNOWS WHICH FIELDS TO REDACT ───────────────
    // C-3PO won't record certain sensitive attributes (like userPassword) in the log
    // — he redacts them with asterisks to protect sensitive data.
    // We return the comma-separated list of attribute names to mask.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the comma-separated list of LDAP attribute names to mask in modification logs.
     * Attributes like {@code userPassword} are replaced with {@code ***} in the log files.
     *
     * @return  The masked-attributes preference string, or {@code null} if not set.
     */
    public String getMModificationLogsMaskedAttributes()
    {
        return Platform.getPreferencesService().getString( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_MASKED_ATTRIBUTES, null, null );
    }


    // ── GET SEARCH LOGS FILE COUNT — HOW MANY SEARCH LOG FILES TO KEEP ───────────
    // C-3PO rotates search logs too; this tells him how many to keep around.
    // We read the search log file count preference.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of rotating search log files to keep.
     *
     * @return  The file count preference value.
     */
    public int getSearchLogsFileCount()
    {
        return Platform.getPreferencesService().getInt( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_SEARCHLOGS_FILE_COUNT, -1, null );
    }


    // ── GET SEARCH LOGS FILE SIZE — MAX SIZE PER SEARCH LOG FILE ─────────────────
    // Each search log file has a max size too.
    // We read the search log file size preference.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum size in kilobytes for each search log file.
     *
     * @return  The file size preference value in kilobytes.
     */
    public int getSearchLogsFileSize()
    {
        return Platform.getPreferencesService().getInt( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_SEARCHLOGS_FILE_SIZE, -1, null );
    }


    // ── IS SEARCH REQUEST LOGS ENABLED — IS C-3PO RECORDING QUERIES ──────────────
    // C-3PO can log outgoing search requests (the queries we send to the server).
    // We return whether search-request logging is enabled.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether search-request logging is enabled.
     * When enabled, every LDAP search request is written to the search log in LDIF format.
     *
     * @return  {@code true} if search request logging is on.
     */
    public boolean isSearchRequestLogsEnabled()
    {
        return Platform.getPreferencesService().getBoolean( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_SEARCHREQUESTLOGS_ENABLE, true, null );
    }


    // ── IS SEARCH RESULT ENTRY LOGS ENABLED — IS C-3PO RECORDING THE ANSWERS ─────
    // C-3PO can also log the individual result entries the server sends back
    // (off by default because results can be very large).
    // We return whether search-result-entry logging is enabled.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether search-result-entry logging is enabled.
     * When enabled, every result entry from an LDAP search is written to the search log.
     * Off by default because search results can be enormous.
     *
     * @return  {@code true} if search result entry logging is on.
     */
    public boolean isSearchResultEntryLogsEnabled()
    {
        return Platform.getPreferencesService().getBoolean( ConnectionCoreConstants.PLUGIN_ID,
            ConnectionCoreConstants.PREFERENCE_SEARCHRESULTENTRYLOGS_ENABLE, false, null );
    }

}
