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
package org.apache.directory.studio.ldapservers;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.util.FileUtils;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;


// ── CLASS: LdapServersManager — LANDO'S CLOUD CITY CONTROL CENTRE ─────────────────────────
// Lando Calrissian runs Cloud City from a central operations room — every power generator,
// landing pad, and life-support system appears on his board, added or removed as the city
// grows or shrinks.  He persists the layout to disk so a restart doesn't lose the map.
// We do the same: track every configured LDAP server in a list + map, save them to
// ldapServers.xml on disk, and fire events when servers are added, removed, or updated.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * The central registry for all LDAP server instances defined in this Studio workspace.
 * Maintains an in-memory list and ID-keyed map, persists servers to {@code ldapServers.xml},
 * and notifies registered {@link LdapServersManagerListener}s whenever the registry changes.
 * Think of this class as Lando's operations centre: one authoritative board, backed by disk,
 * always consistent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServersManager
{
    private static final String SERVERS = "servers"; //$NON-NLS-1$

    /** The default instance */
    private static LdapServersManager instance;

    /** The list of servers */
    private List<LdapServer> serversList;

    /** The map of servers identified by ID */
    private Map<String, LdapServer> serversIdMap;

    /** The listeners */
    private List<LdapServersManagerListener> listeners;


    // ── Lando Initializes His Operations Room ───────────────────────────────────────────────
    // Before opening Cloud City for business, Lando sets up an empty operations room —
    // blank boards, no readings yet, ready to track everything once data flows in.
    // We create the manager with no servers or listeners; {@link #loadServersFromStore()} fills it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the singleton manager instance — kept private because there must be exactly one.
     * Collections are initialized lazily inside {@link #loadServersFromStore()} so startup
     * order is explicit and controlled.
     *
     * <p>For example — Lando's empty control room:</p>
     * <pre>
     *   Lando: "Systems online. Boards are clear. Awaiting server data."
     *   serversList = null; serversIdMap = null; listeners = null.
     * </pre>
     */
    private LdapServersManager()
    {
    }


    // ── Lando's One Shared Operations Room ──────────────────────────────────────────────────
    // Everyone in Cloud City who needs to know about the power grid uses Lando's same control
    // room — there is no second control room, and no one builds their own copy.
    // Classic singleton: one shared manager for the entire application.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton {@link LdapServersManager}, creating it on the first call.
     * Every component that needs to add, remove, or look up servers goes through this
     * single instance so the list stays consistent everywhere.
     *
     * <p>For example — Cloud City's shared control room:</p>
     * <pre>
     *   The Rebellion asks: "Who's managing the servers?"
     *   Lando steps forward: "That's me. Always the same guy."
     * </pre>
     *
     * @return the single shared {@link LdapServersManager}
     */
    public static LdapServersManager getDefault()
    {
        if ( instance == null )
        {
            instance = new LdapServersManager();
        }

        return instance;
    }


    // ── Docking A New Ship At The Landing Pad ───────────────────────────────────────────────
    // A new vessel requests a berth at Cloud City — Lando assigns it a pad, logs it in the
    // manifest, and announces its arrival over the intercom so everyone knows.
    // We add the server to our list + map, save to disk, and fire {@code serverAdded} to listeners.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a server to the registry, persists the updated list to disk, and notifies listeners.
     * This is the public entry point for adding a brand-new server — use it after the
     * "New Server" wizard completes.
     *
     * <p>For example — a new ship arrives at Cloud City:</p>
     * <pre>
     *   Lando: "Landing pad 7 assigned to 'ApacheDS-Local'."
     *   Server added to list, saved to ldapServers.xml, listeners notified.
     * </pre>
     *
     * @param server  the server to register — must not already be in the list
     */
    public void addServer( LdapServer server )
    {
        addServer( server, true );

        saveServersToStore();
    }


    // ── Quietly Adding A Ship Without The Announcement ──────────────────────────────────────
    // During Cloud City's initial setup, Lando loads the existing manifest from disk — he
    // adds each ship to the board silently, without broadcasting each one over the intercom.
    // We use this private variant during {@link #loadServersFromStore()} to avoid spurious events.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal helper that adds a server to the in-memory registry with optional listener
     * notification.
     * Skips duplicates silently. Called by the public {@link #addServer(LdapServer)} (with
     * notification) and by {@link #loadServersFromStore()} (without notification).
     *
     * <p>For example — Lando quietly updates the manifest:</p>
     * <pre>
     *   During startup: addServer(server, false) — board updated, no intercom blast.
     *   During user action: addServer(server, true) — board updated AND announcement made.
     * </pre>
     *
     * @param server           the server to add
     * @param notifyListeners  {@code true} to fire {@code serverAdded} events; {@code false}
     *                         to update silently (used during bulk loading)
     */
    private void addServer( LdapServer server, boolean notifyListeners )
    {
        if ( !serversList.contains( server ) )
        {
            // Adding the server
            serversList.add( server );
            serversIdMap.put( server.getId(), server );

            // Notifying listeners
            if ( notifyListeners )
            {
                for ( LdapServersManagerListener listener : listeners.toArray( new LdapServersManagerListener[0] ) )
                {
                    listener.serverAdded( server );
                }
            }
        }
    }


    // ── Removing A Ship From The Manifest ───────────────────────────────────────────────────
    // A vessel leaves Cloud City permanently — Lando strikes it from the manifest, clears its
    // landing pad, and broadcasts the departure so all systems can clean up.
    // We remove the server from list + map, save to disk, and fire {@code serverRemoved}.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a server from the registry, persists the updated list to disk, and notifies listeners.
     * This is the public entry point used by the Delete action after the user confirms deletion.
     *
     * <p>For example — a ship departs Cloud City:</p>
     * <pre>
     *   Lando: "Pad 7 is now clear. 'ApacheDS-Local' has left the grid."
     *   Server removed from list, saved to ldapServers.xml, listeners notified.
     * </pre>
     *
     * @param server  the server to remove — if it isn't in the list, this is a no-op
     */
    public void removeServer( LdapServer server )
    {
        removeServer( server, true );

        saveServersToStore();
    }


    // ── Quietly Removing Without The Broadcast ──────────────────────────────────────────────
    // Lando can silently remove a ship from the internal registry without triggering alarms —
    // useful during batch operations where events would be premature or redundant.
    // This private variant handles the actual removal logic for both public and internal callers.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal helper that removes a server from the in-memory registry with optional listener
     * notification.
     * Skips silently if the server isn't present.
     *
     * @param server           the server to remove
     * @param notifyListeners  {@code true} to fire {@code serverRemoved}; {@code false} to remove silently
     */
    private void removeServer( LdapServer server, boolean notifyListeners )
    {
        if ( serversList.contains( server ) )
        {
            // Removing the server
            serversList.remove( server );
            serversIdMap.remove( server.getId() );

            // Notifying listeners
            if ( notifyListeners )
            {
                for ( LdapServersManagerListener listener : listeners.toArray( new LdapServersManagerListener[0] ) )
                {
                    listener.serverRemoved( server );
                }
            }
        }
    }


    // ── Checking The Landing Pad Manifest ───────────────────────────────────────────────────
    // Lando glances at his manifest: "Is that ship already registered here?"
    // A quick membership check before trying to add a duplicate or react to a missing server.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given server is currently registered in the manager.
     * Useful as a guard before performing operations that assume the server is (or isn't) tracked.
     *
     * @param server  the server to look for
     * @return {@code true} if the server is in our registry; {@code false} if not
     */
    public boolean containsServer( LdapServer server )
    {
        return serversList.contains( server );
    }


    // ── Plugging A New Comms Line Into The Control Room ─────────────────────────────────────
    // A new department in Cloud City wants to receive landing-pad notifications — Lando patches
    // their comm line into the intercom system so they get every future announcement.
    // We add the listener to our list so it receives future server events.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a listener to receive server lifecycle events (added, removed, updated).
     * Duplicate registrations are ignored — a listener is never notified twice for the same event.
     *
     * @param listener  the component that wants to know when servers change
     */
    public void addListener( LdapServersManagerListener listener )
    {
        if ( !listeners.contains( listener ) )
        {
            listeners.add( listener );
        }
    }


    // ── Unplugging A Comms Line From The Control Room ───────────────────────────────────────
    // A department shuts down and disconnects from Cloud City's intercom — no more notifications
    // will be routed to them, freeing up the line.
    // We remove the listener from our list so it stops receiving events.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters a previously added listener so it no longer receives server events.
     * If the listener wasn't registered, this is a safe no-op.
     *
     * @param listener  the component to unsubscribe from server lifecycle events
     */
    public void removeListener( LdapServersManagerListener listener )
    {
        if ( listeners.contains( listener ) )
        {
            listeners.remove( listener );
        }
    }


    // ── Lando Reads The Manifest Off Disk ───────────────────────────────────────────────────
    // Each time Cloud City restarts, Lando reads the saved manifest from the records room —
    // the main file first, then the backup if the main is corrupt.
    // We load servers from ldapServers.xml (with ldapServers-temp.xml as fallback).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes all collections and loads the server list from the on-disk store.
     * Tries the primary file ({@code ldapServers.xml}) first; falls back to the temp file
     * if the primary is corrupt or unreadable; shows an error dialog if both fail.
     * Called once at plugin startup by {@link LdapServersPlugin#start}.
     *
     * <p>For example — Lando restores Cloud City's manifest:</p>
     * <pre>
     *   Primary file OK → servers loaded, no fuss.
     *   Primary corrupt → try temp file.
     *   Both corrupt → show error dialog to the user.
     * </pre>
     */
    public void loadServersFromStore()
    {
        // Initializing lists and maps
        serversList = new ArrayList<LdapServer>();
        serversIdMap = new HashMap<String, LdapServer>();
        listeners = new ArrayList<LdapServersManagerListener>();

        File store = getServersStorePath().toFile();
        File tempStore = getServersStoreTempPath().toFile();
        boolean loadFailed = false;
        String exceptionMessage = ""; //$NON-NLS-1$

        // We try to load the servers file
        if ( store.exists() )
        {
            try
            {
                InputStream inputStream = new FileInputStream( store );
                List<LdapServer> servers = LdapServersManagerIO.read( inputStream );
                for ( LdapServer server : servers )
                {
                    addServer( server, false );
                }
                return;
            }
            catch ( FileNotFoundException e )
            {
                loadFailed = true;
                exceptionMessage = e.getMessage();
            }
            catch ( LdapServersManagerIOException e )
            {
                loadFailed = true;
                exceptionMessage = e.getMessage();
            }

            if ( loadFailed )
            {
                if ( tempStore.exists() )
                {
                    // If something went wrong, we try to load the temp servers file
                    try
                    {
                        InputStream inputStream = new FileInputStream( tempStore );
                        List<LdapServer> servers = LdapServersManagerIO.read( inputStream );
                        for ( LdapServer server : servers )
                        {
                            addServer( server, false );
                        }
                        return;
                    }
                    catch ( Exception e )
                    {
                        CommonUIUtils.openErrorDialog( Messages.getString( "LdapServersManager.ErrorLoadingServer" ) //$NON-NLS-1$
                            + e.getMessage() );
                    }
                }
                else
                {
                    CommonUIUtils.openErrorDialog( Messages.getString( "LdapServersManager.ErrorLoadingServer" ) //$NON-NLS-1$
                        + exceptionMessage );
                }
            }
        }
    }


    // ── Lando Writes The Manifest Back To Disk ──────────────────────────────────────────────
    // After any change to Cloud City's configuration, Lando writes the updated manifest to the
    // records room — first to a temp copy so a crash mid-write doesn't destroy the original.
    // We write to ldapServers-temp.xml first, then copy to ldapServers.xml.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the current server list to disk using a write-to-temp-then-copy strategy.
     * Writing to the temp file first protects against data loss if the process crashes
     * partway through — the primary file is only replaced once the temp write succeeds.
     * Falls back to writing directly to the primary file if the temp strategy itself fails.
     *
     * <p>For example — Lando safeguards the manifest:</p>
     * <pre>
     *   Step 1: Write all servers to ldapServers-temp.xml.
     *   Step 2: Copy temp to ldapServers.xml.
     *   If step 1 or 2 fails → write directly to ldapServers.xml as a last resort.
     * </pre>
     */
    public void saveServersToStore()
    {
        File store = getServersStorePath().toFile();
        File tempStore = getServersStoreTempPath().toFile();
        boolean saveFailed = false;

        try
        {
            // Saving the servers to the temp servers file
            OutputStream outputStream = new FileOutputStream( tempStore );
            LdapServersManagerIO.write( serversList, outputStream );

            // Copying the temp servers file to the final location
            String content = FileUtils.readFileToString( tempStore, "UTF-8" ); //$NON-NLS-1$
            FileUtils.writeStringToFile( store, content, "UTF-8" ); //$NON-NLS-1$
        }
        catch ( Exception e )
        {
            saveFailed = true;
        }

        if ( saveFailed )
        {
            // If an error occurs when saving to the temp servers file or
            // when copying the temp servers file to the final location,
            // we try to save the servers directly to the final location.
            try
            {
                // Saving the servers to the temp servers file
                OutputStream outputStream = new FileOutputStream( store );
                LdapServersManagerIO.write( serversList, outputStream );
                outputStream.close();
            }
            catch ( Exception e )
            {
                CommonUIUtils
                    .openErrorDialog( Messages.getString( "LdapServersManager.ErrorLoadingServer" ) + e.getMessage() ); //$NON-NLS-1$
            }
        }
    }


    // ── Finding The Records Room Path ───────────────────────────────────────────────────────
    // Every manifest document lives in Cloud City's central records room at a known address.
    // We compute the path to ldapServers.xml inside Eclipse's plugin state location.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IPath} to the primary {@code ldapServers.xml} store file.
     * This file lives inside Eclipse's plugin state directory, which is specific to the
     * current workspace and survives plugin restarts.
     *
     * @return the path to {@code ldapServers.xml}
     */
    private IPath getServersStorePath()
    {
        return LdapServersPlugin.getDefault().getStateLocation().append( "ldapServers.xml" ); //$NON-NLS-1$
    }


    // ── Finding The Temporary Records Room Path ──────────────────────────────────────────────
    // Cloud City keeps a scratch copy of the manifest in a side room during updates —
    // if the main room catches fire mid-write, the scratch copy is still safe.
    // We compute the path to ldapServers-temp.xml for use as a safe write buffer.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IPath} to the temporary write buffer {@code ldapServers-temp.xml}.
     * We write here first, then promote to the primary file — this two-phase strategy
     * prevents corruption if the process is killed mid-write.
     *
     * @return the path to {@code ldapServers-temp.xml}
     */
    private IPath getServersStoreTempPath()
    {
        return LdapServersPlugin.getDefault().getStateLocation().append( "ldapServers-temp.xml" ); //$NON-NLS-1$
    }


    // ── Checking Whether A Landing Pad Name Is Free ─────────────────────────────────────────
    // Before assigning a new vessel a name, Lando checks the manifest for conflicts —
    // "ApacheDS-Local" is taken; "ApacheDS-Test" is free.
    // We compare case-insensitively because user-visible names should be obviously distinct.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the given name is not already used by any registered server.
     * Server names must be unique so the UI can unambiguously identify each server;
     * the comparison is case-insensitive so "My Server" and "my server" count as the same name.
     *
     * @param name  the candidate name to check
     * @return {@code true} if the name is free to use; {@code false} if another server already uses it
     */
    public boolean isNameAvailable( String name )
    {
        for ( LdapServer serverInstance : serversList )
        {
            if ( serverInstance.getName().equalsIgnoreCase( name ) )
            {
                return false;
            }
        }

        return true;
    }


    // ── Reading The Full Manifest Roster ────────────────────────────────────────────────────
    // Lando hands over the complete list of every vessel currently on the grid.
    // Callers like the Servers view use this to populate the table.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the complete list of all registered {@link LdapServer} instances.
     * This is the live list — callers should not modify it directly; use
     * {@link #addServer} and {@link #removeServer} instead.
     *
     * @return the ordered list of servers
     */
    public List<LdapServer> getServersList()
    {
        return serversList;
    }


    // ── Looking Up A Vessel By Its Registry ID ──────────────────────────────────────────────
    // Given a specific vessel registry number, Lando looks it up instantly in his indexed board.
    // We use the ID-keyed map for O(1) lookup instead of scanning the whole list.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server registered under the given UUID-style ID, or {@code null} if not found.
     * IDs are stable across renames, so this is the reliable way to retrieve a specific server.
     *
     * @param id  the server's unique identifier
     * @return the matching {@link LdapServer}, or {@code null} if no server has that ID
     */
    public LdapServer getServerById( String id )
    {
        return serversIdMap.get( id );
    }


    // ── Locating The Docking Bay For All Ships ──────────────────────────────────────────────
    // Cloud City's general docking bay — the directory that contains every individual
    // server's own private berth folder.
    // We return the path to the "servers/" folder inside Eclipse's plugin state location.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IPath} to the shared "servers" folder that contains individual per-server
     * subdirectories.
     * Each server gets its own subfolder (named after its ID) for storing server-specific files
     * like config data and mementos.
     *
     * @return the path to the "servers/" directory
     */
    public static IPath getServersFolder()
    {
        return LdapServersPlugin.getDefault().getStateLocation().append( SERVERS );
    }


    // ── Locating A Specific Ship's Private Berth ────────────────────────────────────────────
    // Each vessel in Cloud City has its own private landing bay identified by the ship's ID.
    // We append the server's UUID to the general docking bay path to get its specific folder.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IPath} to the given server's private data folder.
     * Server-specific files (configuration snapshots, mementos) are stored here, isolated
     * from other servers' data.
     *
     * @param server  the server whose folder we want; if {@code null}, we return {@code null}
     * @return the path to this server's folder, or {@code null} if server is null
     */
    public static IPath getServerFolder( LdapServer server )
    {
        if ( server != null )
        {
            return getServersFolder().append( server.getId() );
        }

        return null;
    }


    // ── Preparing A New Private Berth For A New Ship ────────────────────────────────────────
    // A new vessel arrives at Cloud City — Lando creates a new landing bay for it, first
    // making sure the general docking area exists, then carving out a private slot.
    // We mkdir the servers/ folder and then mkdir the server's own subdirectory.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the servers/ parent folder and the given server's private subfolder if they don't
     * already exist.
     * This must be called after a server is first registered so there is a place to store
     * its files (configuration snapshots, mementos, etc.).
     *
     * @param server  the newly registered server needing a private folder; no-op if null
     */
    public static void createNewServerFolder( LdapServer server )
    {
        if ( server != null )
        {
            // Creating if needed the 'servers' folder
            File serversFolder = getServersFolder().toFile();
            if ( !serversFolder.exists() )
            {
                serversFolder.mkdir();
            }

            // Creating the specific server folder
            File serverFolder = getServerFolder( server ).toFile();
            if ( !serverFolder.exists() )
            {
                serverFolder.mkdir();
            }
        }
    }


    // ── Reading A Ship's Private Logbook ────────────────────────────────────────────────────
    // Each vessel in Cloud City stores a private logbook (memento) in its berth — containing
    // adapter-specific data that survives restarts.  Lando retrieves it on request.
    // We read the server's memento.xml file and return a writable XMLMemento.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a writable {@link IMemento} for the given server, backed by the server's
     * {@code memento.xml} file.
     * Adapter extensions use mementos to persist their own custom state (ports, config paths, etc.)
     * across Studio restarts without needing to know about the servers.xml format.
     * Returns {@code null} if the server is null or if any IO error occurs.
     *
     * <p>For example — Lando retrieves and unlocks a ship's logbook:</p>
     * <pre>
     *   File exists → read its current content into a writable memento.
     *   File missing → create it empty, then open it.
     *   IO error → return null (caller must handle gracefully).
     * </pre>
     *
     * @param server  the server whose memento we want
     * @return a writable {@link IMemento} pre-populated with existing data, or {@code null}
     */
    public static IMemento getMementoForServer( LdapServer server )
    {
        try
        {
            if ( server != null )
            {
                // Creating the File of the memento (if needed)
                File mementoFile = getServerFolder( server ).append( "memento.xml" ).toFile(); //$NON-NLS-1$
                if ( !mementoFile.exists() )
                {
                    mementoFile.createNewFile();
                }

                // Getting a (read-only) memento from the File
                XMLMemento readMemento = XMLMemento.createReadRoot( new FileReader( mementoFile ) );

                // Converting the read memento to a writable memento
                XMLMemento memento = XMLMemento.createWriteRoot( "memento" ); //$NON-NLS-1$
                memento.putMemento( readMemento );

                return memento;
            }

            return null;
        }
        catch ( Exception e )
        {
            return null;
        }
    }
}
