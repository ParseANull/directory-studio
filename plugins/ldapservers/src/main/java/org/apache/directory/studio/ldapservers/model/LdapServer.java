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

package org.apache.directory.studio.ldapservers.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IActionFilter;


// ── CLASS: LdapServer — THE MILLENNIUM FALCON (EACH SHIP IS UNIQUE) ──────────────────────
// The Millennium Falcon is one-of-a-kind: its own name, its own ID on file at the
// Imperial registry, its own current operating status (docked, in hyperspace, in combat),
// and a set of custom modifications Han made over the years.
// Each LdapServer instance is exactly that — a unique server with its own UUID, name,
// status (STOPPED/STARTING/STARTED/STOPPING), adapter extension, and configuration map.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The core domain model object representing a single configured LDAP server instance.
 * Holds the server's identity (UUID, name), its current lifecycle status, its adapter
 * extension (which knows how to start/stop/configure it), and two key-value maps:
 * one for runtime custom objects (like the active ILaunch) and one for persisted
 * configuration parameters (like port numbers).
 * Think of this class as the ship's manifest entry: everything that defines one Falcon.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServer implements IAdaptable
{
    /** The ID of the server */
    private String id;

    /** The name of the server*/
    private String name;

    /** The status of the server */
    private LdapServerStatus status = LdapServerStatus.STOPPED;

    /** The LDAP Server Adapter Extension */
    private LdapServerAdapterExtension ldapServerAdapterExtension;

    /** The list of listeners */
    private List<LdapServerListener> listeners = new ArrayList<LdapServerListener>();

    /** The Map for custom objects */
    private Map<String, Object> customObjectsMap = new HashMap<String, Object>();

    /** The Map for configuration parameters  */
    private Map<String, Object> configurationParameters = new HashMap<String, Object>();


    // ── A New Ship Rolls Off The Assembly Line ───────────────────────────────────────────────
    // A fresh ship arrives at the hangar — no name assigned yet, but the registry already
    // issued it a unique hull number so it can be tracked immediately.
    // We generate a UUID for the ID so the server is identifiable even before the user names it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new unnamed LDAP server with a freshly generated UUID.
     * Use this when the server name will be set later (e.g., during deserialization).
     *
     * <p>For example — a new hull number is stamped:</p>
     * <pre>
     *   new LdapServer() → id = "f7a8c3d2-..."; name = null; status = STOPPED.
     * </pre>
     */
    public LdapServer()
    {
        id = createId();
    }


    // ── A Named Ship Rolls Off The Assembly Line ─────────────────────────────────────────────
    // The Rebellion christens a new ship immediately — name and hull number assigned together
    // at the moment it joins the fleet.
    // We record the name and generate a fresh UUID in one step.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new LDAP server with the given name and a freshly generated UUID.
     * Use this when creating a server through the "New Server" wizard where a name is chosen upfront.
     *
     * @param name  the human-readable display name for this server instance
     */
    public LdapServer( String name )
    {
        this.name = name;
        id = createId();
    }


    // ── The Registry Issues A New Hull Number ────────────────────────────────────────────────
    // The Imperial (or Rebel) registry stamps a fresh UUID on every new ship — guaranteed unique
    // across all vessels ever registered, no collisions.
    // We use Java's UUID.randomUUID() for this — simple, collision-resistant, no coordination needed.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Generates a new random UUID string to serve as a server's unique identifier.
     * UUIDs are used instead of sequential IDs so multiple workspaces can each create servers
     * independently without ID conflicts when configurations are shared or exported.
     *
     * @return a new UUID string in the standard {@code xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx} format
     */
    private static String createId()
    {
        return UUID.randomUUID().toString();
    }


    // ── A New Department Plugs Into The Falcon's Comms ──────────────────────────────────────
    // A new crew module wants to receive status updates from the Falcon's systems —
    // it plugs its comm line into the ship's internal network.
    // We add the listener so it starts receiving server lifecycle events.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a {@link LdapServerListener} to receive name and status change events for this server.
     * Duplicate registrations are silently ignored — a listener only gets one notification per event.
     *
     * @param listener  the component that wants to track this specific server's lifecycle changes
     */
    public void addListener( LdapServerListener listener )
    {
        if ( !listeners.contains( listener ) )
        {
            listeners.add( listener );
        }
    }


    // ── Retrieving A Custom Cargo Item From The Hold ─────────────────────────────────────────
    // The Falcon's cargo hold has labelled compartments — Han can pull out any item by its label.
    // Custom objects are runtime-only items (like the active ILaunch) stored under a key.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the runtime custom object stored under the given key, or {@code null} if absent.
     * The custom objects map is for transient, runtime-only data — things like the active
     * {@code ILaunch} or the log-tailer thread — that must not be persisted to disk.
     *
     * @param key  the label identifying the stored object (see constants in {@code LdapServersUtils})
     * @return the stored object, or {@code null} if nothing is stored under that key
     */
    public Object getCustomObject( String key )
    {
        return customObjectsMap.get( key );
    }


    // ── Checking The Ship's Configured Flight Parameter ─────────────────────────────────────
    // The Falcon's flight computer holds configured parameters — nav settings, fuel ratios —
    // that persist between missions.  Pull any one out by its parameter name.
    // Configuration parameters are persisted to XML; they're things like port numbers.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the persisted configuration parameter stored under the given key, or {@code null}.
     * Configuration parameters (port numbers, paths, etc.) are saved to {@code ldapServers.xml}
     * so they survive restarts; this is distinct from the transient custom objects map.
     *
     * @param key  the parameter name
     * @return the parameter value (String, Integer, or Boolean depending on what was stored),
     *         or {@code null} if not set
     */
    public Object getConfigurationParameter( String key )
    {
        return configurationParameters.get( key );
    }


    // ── Getting The Full Flight Configuration ────────────────────────────────────────────────
    // Han needs to inspect the Falcon's entire configuration map — all parameters at once —
    // so he can save or transmit the full profile.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full map of all persisted configuration parameters for this server.
     * Used by {@link org.apache.directory.studio.ldapservers.LdapServersManagerIO} when
     * serializing the server to XML.
     *
     * @return the live configuration parameters map — do not modify directly
     */
    public Map<String, Object> getConfigurationParameters()
    {
        return configurationParameters;
    }


    // ── Checking The Ship's Hull Registration Number ─────────────────────────────────────────
    // Every ship has a stamped-in hull number — unique, stable, never changes even when the
    // ship is renamed.  It's the reliable key for all lookups and persistence.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this server's unique identifier (UUID).
     * The ID is assigned at creation and never changes, even if the server is renamed.
     * It is used as the primary key in the servers map and as the name of the server's
     * data folder on disk.
     *
     * @return the server's UUID string
     */
    public String getId()
    {
        return id;
    }


    // ── Checking Which Hyperdrive Module Is Installed ────────────────────────────────────────
    // The Falcon uses a specific hyperdrive variant — its adapter extension.
    // Knowing which one is installed lets us call the right start/stop/configure routines.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdapServerAdapterExtension} that handles start, stop, delete, and
     * open-configuration operations for this server.
     * The extension is set at creation time (via the wizard) and restored from XML at load time.
     *
     * @return the adapter extension, or {@code null} if not yet assigned
     */
    public LdapServerAdapterExtension getLdapServerAdapterExtension()
    {
        return ldapServerAdapterExtension;
    }


    // ── Reading The Ship's Name Off The Hull ─────────────────────────────────────────────────
    // "Millennium Falcon" is painted right on the side — that's the human-readable identity
    // shown in the UI, in error messages, and in progress dialogs.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server's human-readable display name.
     *
     * @return the name, or {@code null} if not yet set
     */
    public String getName()
    {
        return name;
    }


    // ── Checking The Ship's Current Operational Status ───────────────────────────────────────
    // The flight board shows whether the Falcon is DOCKED (STOPPED), STARTING, IN FLIGHT
    // (STARTED), or LANDING (STOPPING).  Every UI element reads from this to show the right icon.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current lifecycle status of this server.
     * The status drives the icon and available actions in the Servers view — e.g., you can only
     * click "Stop" when the status is STARTED.
     *
     * @return the current {@link LdapServerStatus}; starts as {@code STOPPED}
     */
    public LdapServerStatus getStatus()
    {
        return status;
    }


    // ── Stashing A Runtime Object In The Cargo Hold ──────────────────────────────────────────
    // Han stashes the Kessel Run coordinates in a labelled compartment so he can retrieve them
    // mid-flight without searching the whole hold.
    // Custom objects are runtime-only — they don't get serialised to disk.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores a runtime custom object under the given key.
     * Used by adapters and utilities to attach transient data (like the active {@code ILaunch}
     * or the log-tailer thread) to the server without polluting the persisted configuration.
     *
     * @param key    a label identifying this object (use constants from {@code LdapServersUtils})
     * @param value  the object to store
     */
    public void putCustomObject( String key, Object value )
    {
        customObjectsMap.put( key, value );
    }


    // ── Recording A New Flight Parameter ────────────────────────────────────────────────────
    // Han updates the Falcon's flight configuration — new port, new fuel ratio — and it gets
    // saved so the next startup uses the same settings.
    // Configuration parameters are persisted to XML.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores a persisted configuration parameter under the given key.
     * Unlike custom objects, these are written to {@code ldapServers.xml} and survive restarts.
     * Values can be String, Integer, or Boolean.
     *
     * @param key    the parameter name
     * @param value  the parameter value (String, Integer, or Boolean)
     */
    public void putConfigurationParameter( String key, Object value )
    {
        configurationParameters.put( key, value );
    }


    // ── Removing A Runtime Object From The Hold ──────────────────────────────────────────────
    // Han retrieves the Kessel Run coordinates and removes the compartment label — they're no
    // longer needed after the jump.
    // We return the removed object so the caller can close or stop it (e.g., stop the tailer).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes and returns the custom object stored under the given key.
     * Use this when you need to clean up a runtime resource — the return value lets you
     * stop or close the object after removal.
     *
     * @param key  the label of the object to remove
     * @return the previously stored object, or {@code null} if nothing was stored under that key
     */
    public Object removeCustomObject( String key )
    {
        return customObjectsMap.remove( key );
    }


    // ── Clearing A Flight Parameter ──────────────────────────────────────────────────────────
    // Han removes an old nav waypoint from the flight computer — it's no longer part of the route.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes and returns the configuration parameter stored under the given key.
     *
     * @param key  the parameter name to remove
     * @return the previously stored value, or {@code null} if absent
     */
    public Object removeConfigurationParameter( String key )
    {
        return configurationParameters.remove( key );
    }


    // ── A Department Unplugs From The Falcon's Comms ────────────────────────────────────────
    // The module is being shut down — it disconnects its comm line so it stops receiving
    // status updates from the ship.
    // We remove the listener so it is no longer notified of name or status changes.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters a previously added {@link LdapServerListener}.
     * After removal the listener will no longer receive name or status change events for this server.
     *
     * @param listener  the listener to unsubscribe
     */
    public void removeListener( LdapServerListener listener )
    {
        if ( !listeners.contains( listener ) )
        {
            listeners.remove( listener );
        }
    }


    // ── Updating The Ship's Hull Registration Number ─────────────────────────────────────────
    // Occasionally a ship's registry entry is corrected — we stamp the new hull number onto
    // the record.  This only makes sense during deserialization from XML.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Overrides the server's ID.
     * Normally the ID is set by the constructor and never changes; this setter exists solely
     * for use by {@link org.apache.directory.studio.ldapservers.LdapServersManagerIO} when
     * restoring servers from the XML store.
     *
     * @param id  the UUID string to assign
     */
    public void setId( String id )
    {
        this.id = id;
    }


    // ── Reprogramming The Adapter Module ─────────────────────────────────────────────────────
    // Swapping out the Falcon's hyperdrive variant — we record which adapter extension
    // this server is now bound to so start/stop/configure calls go to the right plugin.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Associates this server with the given adapter extension.
     * The extension determines how this server is started, stopped, configured, and deleted.
     * Set once during creation or load; not expected to change at runtime.
     *
     * @param ldapServerAdapterExtension  the adapter extension to bind to this server
     */
    public void setLdapServerAdapterExtension( LdapServerAdapterExtension ldapServerAdapterExtension )
    {
        this.ldapServerAdapterExtension = ldapServerAdapterExtension;
    }


    // ── Repainting The Ship's Name On The Hull ───────────────────────────────────────────────
    // Lando renamed the Falcon after winning it from Han — a quick repaint, and then the
    // whole fleet gets notified of the new call sign.
    // We fire a RENAMED event to all listeners when the name actually changes.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the server's display name and notifies all listeners of the rename.
     * If the new name is the same object reference as the current one, we skip the update
     * and the event to avoid spurious notifications.
     *
     * @param name  the new human-readable name for this server
     */
    public void setName( String name )
    {
        if ( this.name == name )
        {
            return;
        }

        this.name = name;

        fireServerNameChangeEvent();
    }


    // ── Announcing The New Call Sign Over The Intercom ──────────────────────────────────────
    // Lando's intercom: "Attention Cloud City — the Falcon is now operating under a new name."
    // We fire the RENAMED event to every attached listener.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Fires a {@link LdapServerEventType#RENAMED} event to all registered listeners.
     * Called internally by {@link #setName} when the name actually changes.
     *
     * <p>For example — a rename broadcast:</p>
     * <pre>
     *   setName("ApacheDS-Prod") → fireServerNameChangeEvent()
     *     → each listener.serverChanged(new LdapServerEvent(this, RENAMED))
     * </pre>
     */
    private void fireServerNameChangeEvent()
    {
        for ( LdapServerListener listener : listeners.toArray( new LdapServerListener[0] ) )
        {
            listener.serverChanged( new LdapServerEvent( this, LdapServerEventType.RENAMED ) );
        }
    }


    // ── Updating The Flight Board Status ────────────────────────────────────────────────────
    // The Falcon's status board flips: DOCKED → LAUNCHING → IN FLIGHT.
    // Every subscribed system gets the update so icons and actions refresh immediately.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the server's lifecycle status and notifies listeners.
     * No-op if the new status is the same as the current one (avoids pointless UI refreshes).
     * The watchdog threads and adapter implementations call this to report progress.
     *
     * @param status  the new {@link LdapServerStatus} — e.g., STARTING, STARTED, STOPPING, STOPPED
     */
    public void setStatus( LdapServerStatus status )
    {
        if ( this.status == status )
        {
            return;
        }

        this.status = status;

        fireServerStateChangeEvent();
    }


    // ── Broadcasting The Status Change Over The Intercom ────────────────────────────────────
    // "Attention crew — the Falcon's reactor has reached full power. Status: STARTED."
    // Every listener gets the STATUS_CHANGED event so displays and actions can update.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Fires a {@link LdapServerEventType#STATUS_CHANGED} event to all registered listeners.
     * Called internally by {@link #setStatus} when the status actually changes.
     *
     * <p>For example — a status broadcast:</p>
     * <pre>
     *   setStatus(STARTED) → fireServerStateChangeEvent()
     *     → each listener.serverChanged(new LdapServerEvent(this, STATUS_CHANGED))
     *     → ServersView icon updates to the "started" green icon.
     * </pre>
     */
    private void fireServerStateChangeEvent()
    {
        for ( LdapServerListener listener : listeners.toArray( new LdapServerListener[0] ) )
        {
            listener.serverChanged( new LdapServerEvent( this, LdapServerEventType.STATUS_CHANGED ) );
        }
    }


    // ── The Falcon Hands Over Its Action Filter To Eclipse ──────────────────────────────────
    // Eclipse asks the Falcon: "Do you know how to implement IActionFilter?"
    // The Falcon delegates to its dedicated filter adapter — the one that knows which toolbar
    // buttons should be enabled for the Falcon's current status.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Eclipse adapter mechanism — returns the appropriate helper object for the requested type.
     * Currently we only adapt to {@link IActionFilter}, which Eclipse uses to evaluate
     * action conditions like "is this server currently stopped?" to enable or disable toolbar buttons.
     *
     * @param adapter  the interface Eclipse is asking us to adapt to
     * @return the {@link LdapServerActionFilterAdapter} for IActionFilter, or {@code null} otherwise
     */
    public Object getAdapter( Class adapter )
    {
        if ( adapter.isAssignableFrom( IActionFilter.class ) )
        {
            return LdapServerActionFilterAdapter.getInstance();
        }

        return null;
    }
}
