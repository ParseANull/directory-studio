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

package org.apache.directory.studio.ldapbrowser.core;


import java.io.IOException;
import java.util.PropertyResourceBundle;

import org.apache.directory.studio.connection.core.event.CoreEventRunner;
import org.apache.directory.studio.connection.core.event.EventRunner;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;


// ── CLASS: BrowserCorePlugin — THE EMPEROR ACTIVATING THE DEATH STAR ─────────
// Emperor Palpatine is the single point of authority for the entire Empire —
// when the Death Star comes online he activates it, hands out key command
// codes to his subordinates, and when the battle is lost he shuts it down.
// Nobody else can start or stop the station; everything goes through him.
// This class plays that role in OSGi: it's the Eclipse Plugin activator that
// starts the browser-core bundle (connection manager, event runner, prefs),
// owns the singleton instance, and cleans up on shutdown.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator for the {@code ldapbrowser.core} plugin.
 * Eclipse calls {@link #start} when the bundle is first needed and
 * {@link #stop} when the workbench is shutting down.  Everything in
 * ldapbrowser.core that needs a lifecycle — the connection manager, the event
 * runner, the preferences — is created and destroyed here.
 * Think of this class as the Emperor: the single authority that activates and
 * deactivates the entire browser-core station.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserCorePlugin extends Plugin
{
    /** The shared instance. */
    private static BrowserCorePlugin plugin;

    /** The connection manager */
    private BrowserConnectionManager connectionManager;

    /** The preferences */
    private BrowserCorePreferences preferences;

    /** The event runner. */
    private EventRunner eventRunner;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── Palpatine Establishes His Station ────────────────────────────────────────
    // When the Emperor steps into the Death Star command room he immediately
    // marks it as HIS station — "I am in command here" — and initialises the
    // bare minimum so the room is operational even before the shields are up.
    // We store the singleton reference and create the preferences object (which
    // has no I/O cost) so it's ready before start() is called.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the plugin activator and registers it as the singleton.
     * Eclipse instantiates this class exactly once per bundle activation.
     * We eagerly create {@link BrowserCorePreferences} here (it's cheap) so
     * callers can access preferences even before {@link #start} runs.
     *
     * <p>For example — Palpatine establishes command:</p>
     * <pre>
     *   plugin = this;                           // I am the station
     *   this.preferences = new BrowserCorePreferences(); // baseline config ready
     * </pre>
     */
    public BrowserCorePlugin()
    {
        super();
        plugin = this;
        this.preferences = new BrowserCorePreferences();
    }


    // ── Palpatine Powers Up The Death Star ───────────────────────────────────────
    // "Commence primary ignition." — The Emperor fires up the reactor, brings
    // the targeting systems online, and stations his commanders at their posts.
    // We start the event runner (delivers async model events) and create the
    // connection manager (tracks all browser connections) so the rest of the
    // plugin can get to work.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when this bundle is activated.
     * We initialise the event runner and connection manager here — both are
     * needed for the rest of the browser-core to function.
     *
     * <p>For example — Palpatine powers up:</p>
     * <pre>
     *   super.start(context);
     *   eventRunner = new CoreEventRunner();        // targeting systems online
     *   connectionManager = new BrowserConnectionManager(); // commanders at posts
     * </pre>
     *
     * @param context  the OSGi bundle context provided by the framework.
     * @throws Exception if the bundle cannot start (propagated to OSGi).
     */
    @Override
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        if ( eventRunner == null )
        {
            eventRunner = new CoreEventRunner();
        }

        if ( connectionManager == null )
        {
            connectionManager = new BrowserConnectionManager();
        }
    }


    // ── Palpatine Powers Down The Death Star ─────────────────────────────────────
    // The battle is lost.  "Shut it down." — The Emperor releases the targeting
    // systems and nulls out his commanders so GC can reclaim the memory.
    // We null the event runner and connection manager so no further events fire
    // and no connections are kept alive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when this bundle is deactivated.
     * We release the event runner and connection manager so background threads
     * can terminate and memory can be reclaimed.
     *
     * <p>For example — Palpatine powers down:</p>
     * <pre>
     *   eventRunner = null;        // targeting systems offline
     *   connectionManager = null;  // commanders dismissed
     *   super.stop(context);
     * </pre>
     *
     * @param context  the OSGi bundle context provided by the framework.
     * @throws Exception if shutdown fails (propagated to OSGi).
     */
    @Override
    public void stop( BundleContext context ) throws Exception
    {
        super.stop( context );

        if ( eventRunner != null )
        {
            eventRunner = null;
        }

        if ( connectionManager != null )
        {
            //            IConnection[] connections = connectionManager.getConnections();
            //            for ( int i = 0; i < connections.length; i++ )
            //            {
            //                connections[i].close();
            //            }
            connectionManager = null;
        }
    }


    // ── Palpatine's Hologram — Calling Him From Anywhere ─────────────────────────
    // Any officer in the Empire can call up a hologram of the Emperor to receive
    // his authority — they don't need to know where he physically is.
    // Callers use this static method to get the singleton plugin instance from
    // anywhere in the codebase without having an explicit reference.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton plugin instance.
     * This is the standard Eclipse pattern for accessing plugin-level services
     * (connection manager, preferences, event runner) from anywhere in the bundle.
     *
     * <p>For example — calling up the Emperor's hologram:</p>
     * <pre>
     *   BrowserCorePlugin.getDefault().getConnectionManager().getBrowserConnections();
     * </pre>
     *
     * @return the single {@link BrowserCorePlugin} instance, or {@code null}
     *         if the bundle has not been activated yet.
     */
    public static BrowserCorePlugin getDefault()
    {
        return plugin;
    }


    // ── Palpatine Hands Over The Connection Registry ──────────────────────────────
    // "You need the list of active Star Destroyers? See Admiral Piett."
    // Palpatine delegates connection-tracking to the BrowserConnectionManager —
    // this method hands callers the reference to that subordinate.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link BrowserConnectionManager} that tracks all open LDAP
     * browser connections.
     * Use this to look up a browser connection by ID or name, or to get the
     * full list of connections.
     *
     * <p>For example — getting the manager to look up a connection:</p>
     * <pre>
     *   IBrowserConnection bc =
     *     BrowserCorePlugin.getDefault().getConnectionManager()
     *       .getBrowserConnectionByName("My LDAP Server");
     * </pre>
     *
     * @return the connection manager; never {@code null} once {@link #start}
     *         has been called.
     */
    public BrowserConnectionManager getConnectionManager()
    {
        return connectionManager;
    }


    // ── Palpatine Hands Over The Preferences Codex ───────────────────────────────
    // "You need the protocol codes? Consult the Imperial Codex."
    // Palpatine keeps the preferences book locked in his chamber — this method
    // hands you the reference so you can read or update binary-attribute rules
    // and other browser-core settings.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link BrowserCorePreferences} wrapper for this plugin's
     * preference store.
     * Use it to get or set binary-attribute lists, binary-syntax lists, and
     * object-class icon mappings.
     *
     * <p>For example — reading the binary attribute list:</p>
     * <pre>
     *   BinaryAttribute[] bas =
     *     BrowserCorePlugin.getDefault().getCorePreferences().getBinaryAttributes();
     * </pre>
     *
     * @return the preferences wrapper; never {@code null}.
     */
    public BrowserCorePreferences getCorePreferences()
    {
        return preferences;
    }


    // ── Palpatine Dispatches His Messenger ───────────────────────────────────────
    // When events need to be delivered across the Empire, Palpatine uses a
    // dedicated courier — not himself, because the Emperor doesn't run messages.
    // The EventRunner is that courier: it queues and delivers model-change events
    // on the right thread so the UI stays consistent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EventRunner} responsible for dispatching LDAP browser
     * model-change events to registered listeners.
     * Events like "entry added" or "search updated" are queued here and
     * dispatched on the appropriate thread.
     *
     * <p>For example — scheduling an event dispatch:</p>
     * <pre>
     *   EventRunner runner = BrowserCorePlugin.getDefault().getEventRunner();
     *   runner.execute(myEvent); // delivered asynchronously
     * </pre>
     *
     * @return the event runner; never {@code null} once the plugin has started.
     */
    public EventRunner getEventRunner()
    {
        return eventRunner;
    }


    // ── Palpatine Reads The Imperial Technical Specifications ────────────────────
    // When someone asks Palpatine about the Death Star's technical specs he
    // doesn't have them memorised — he fetches the manual from the library the
    // first time, then keeps it cached so subsequent requests are instant.
    // We do the same: lazy-load plugin.properties on first access.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's {@code plugin.properties} file as a resource bundle.
     * The properties file is loaded lazily on first access and cached
     * thereafter — it contains things like the plugin version string.
     * If loading fails we log an error and return {@code null}.
     *
     * <p>For example — reading a property:</p>
     * <pre>
     *   String version =
     *     BrowserCorePlugin.getDefault().getPluginProperties()
     *       .getString("Bundle-Version");
     * </pre>
     *
     * @return the {@link PropertyResourceBundle}, or {@code null} if loading
     *         the properties file failed.
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
                getLog().log(
                    new Status( Status.ERROR, "org.apache.directory.studio.ldapbrowser.core", Status.OK, //$NON-NLS-1$
                        BrowserCoreMessages.activator_unable_get_plugin_properties, e ) );
            }
        }

        return properties;
    }
}
