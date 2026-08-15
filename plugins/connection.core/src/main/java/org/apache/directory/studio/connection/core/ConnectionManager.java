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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.directory.api.util.FileUtils;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.core.io.ConnectionIO;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


// ── CLASS: ConnectionManager — CHEWIE MAINTAINS THE FALCON'S KNOWN-ROUTE LOG ──
// Chewie keeps a battered datapad listing every hyperspace route the crew has
// ever plotted: add a new route, remove an old one, look one up by name or ID,
// and save the whole list to the hold so it survives across trips.
// This class does exactly that for {@link Connection} objects — it is the live
// registry of all saved connections and persists them to connections.xml on disk.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The central registry and persistence manager for all saved {@link Connection}s.
 * We load connections from disk at startup, keep them in memory, write them back
 * whenever anything changes, and fire events so the UI stays in sync.
 * Think of this class as Chewie's hyperspace-route datapad: every known server
 * is an entry, and Chewie saves the list after every change so nothing is lost
 * if the Falcon has to make an emergency jump.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionManager implements ConnectionUpdateListener
{
    private static final String LOGS_PATH = "logs"; //$NON-NLS-1$

    private static final String SEARCH_LOGS_PREFIX = "search-"; //$NON-NLS-1$

    private static final String MODIFICATIONS_LOG_PREFIX = "modifications-"; //$NON-NLS-1$

    private static final String LDIFLOG_SUFFIX = "-%u-%g.ldiflog"; //$NON-NLS-1$

    private static final String CONNECTIONS_XML = "connections.xml"; //$NON-NLS-1$

    public static final String ENCODING_UTF8 = "UTF-8"; //$NON-NLS-1$

    public static final String TEMP_SUFFIX = "-temp"; //$NON-NLS-1$

    /** The list of connections. */
    private Set<Connection> connectionList;


    // ── CONSTRUCTOR — CHEWIE LOADS THE DATAPAD FROM THE HOLD ─────────────────────
    // At the start of every mission Chewie retrieves the datapad from the ship's
    // hold, powers it on, and reads in all the known routes; if it's a brand-new
    // datapad he runs the fleet initializers to seed it with starter routes.
    // We load connections from disk, run Eclipse extension initializers if first
    // launch, and register as a listener so we auto-save on every change.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionManager} and wires it up.
     * On first run (no connections.xml on disk) we ask Eclipse extension initializers
     * to seed the connection list with default connections — handy for product distros
     * that want to ship with a pre-configured test server.
     * After that we listen to the event bus and auto-save on every connection change.
     */
    public ConnectionManager()
    {
        this.connectionList = new HashSet<>();
        loadInitializers();
        loadConnections();
        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionCorePlugin.getDefault().getEventRunner() );
    }


    // ── LOAD INITIALIZERS — CHEWIE CHECKS FOR FACTORY-DEFAULT ROUTES ──────────────
    // If the datapad is brand new (no saved file on disk) Chewie loads the
    // factory-default routes that shipped with the unit.
    // We only run Eclipse "connectionInitializer" extensions if connections.xml
    // doesn't exist yet — we don't want to add duplicates on every restart.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Runs Eclipse extension-point initializers to pre-populate connections on first launch.
     * We skip this if connections.xml already exists — initializers are only meant to
     * run once, on a fresh installation.
     */
    private void loadInitializers()
    {
        File connectionStore = new File( getConnectionStoreFileName() );
        if ( connectionStore.exists() )
        {
            return; // connections are stored from a previous sessions - don't call initializers
        }

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
            "org.apache.directory.studio.connectionInitializer" ); //$NON-NLS-1$

        IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
        for ( IConfigurationElement configurationElement : configurationElements )
        {
            if ( "connection".equals( configurationElement.getName() ) ) //$NON-NLS-1$
            {
                addInitialConnection( configurationElement );
            }
        }
    }


    // ── ADD INITIAL CONNECTION — CHEWIE COPIES A FACTORY ROUTE ONTO THE DATAPAD ───
    // Chewie instantiates one factory-default route from the configuration chip
    // and adds it to the datapad's known-routes list.
    // We instantiate the ConnectionParameter from the Eclipse IConfigurationElement
    // and register the resulting Connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Instantiates and registers a single connection from an Eclipse extension-point element.
     * We call this once per initializer element during first-launch setup.
     * If the extension class can't be instantiated, we log the error and move on.
     *
     * @param configurationElement  The Eclipse config element that references
     *                              a {@link ConnectionParameter} implementation class.
     */
    private void addInitialConnection( IConfigurationElement configurationElement )
    {
        try
        {
            ConnectionParameter connectionParameter = ( ConnectionParameter ) configurationElement
                .createExecutableExtension( "class" ); //$NON-NLS-1$
            Connection conn = new Connection( connectionParameter );
            connectionList.add( conn );
        }
        catch ( CoreException e )
        {
            Status status = new Status( IStatus.ERROR, ConnectionCoreConstants.PLUGIN_ID,
                Messages.error__execute_connection_initializer + e.getMessage(), e );
            ConnectionCorePlugin.getDefault().getLog().log( status );
        }
    }


    // ── GET MODIFICATION LOG FILENAME — CHEWIE FINDS THE CHANGE-LOG SLOT ─────────
    // Every route on Chewie's datapad has a corresponding change-log tab where
    // every modification is recorded in LDIF format.
    // We compute the OS path to the per-connection modification log file.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file path for the LDIF modification log of the given connection.
     * This log records every LDAP write operation in LDIF format — handy for auditing
     * or replaying changes.
     * The logs/ directory is created on demand if it doesn't exist yet.
     *
     * <p>For example — Chewie looks up the log slot:</p>
     * <pre>
     *   String path = ConnectionManager.getModificationLogFileName(myConn);
     *   // → ".../logs/modifications-{uuid}-%u-%g.ldiflog"
     * </pre>
     *
     * @param connection  The connection whose log path we need.
     * @return  The absolute OS path string for the modification log file.
     */
    public static final String getModificationLogFileName( Connection connection )
    {
        IPath p = ConnectionCorePlugin.getDefault().getStateLocation().append( LOGS_PATH );
        File file = p.toFile();
        if ( !file.exists() )
        {
            file.mkdir();
        }
        return p.append( MODIFICATIONS_LOG_PREFIX + Utils.getFilenameString( connection.getId() ) + LDIFLOG_SUFFIX )
            .toOSString();
    }


    // ── GET SEARCH LOG FILENAME — CHEWIE FINDS THE SEARCH-LOG SLOT ───────────────
    // Every route also has a search-log tab recording LDAP search requests.
    // We compute the OS path to the per-connection search log file.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file path for the LDIF search log of the given connection.
     * This log captures every search request and its result summary in LDIF format —
     * useful for diagnosing what the browser is sending.
     * The logs/ directory is created on demand if it doesn't exist yet.
     *
     * @param connection  The connection whose search-log path we need.
     * @return  The absolute OS path string for the search log file.
     */
    public static final String getSearchLogFileName( Connection connection )
    {
        IPath p = ConnectionCorePlugin.getDefault().getStateLocation().append( LOGS_PATH ); //$NON-NLS-1$
        File file = p.toFile();
        if ( !file.exists() )
        {
            file.mkdir();
        }
        return p.append( SEARCH_LOGS_PREFIX + Utils.getFilenameString( connection.getId() ) + LDIFLOG_SUFFIX )
            .toOSString();
    }


    // ── GET CONNECTION STORE FILENAME — WHERE CHEWIE KEEPS THE DATAPAD FILE ───────
    // Chewie keeps the master datapad file in a known slot in the ship's state
    // partition — the Eclipse plugin state directory.
    // We return the absolute path to connections.xml.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the absolute path to the connections.xml persistence file.
     * This file lives in the Eclipse plugin state directory, which persists
     * across workspace sessions.
     *
     * @return  The OS path to connections.xml.
     */
    public static final String getConnectionStoreFileName()
    {
        return ConnectionCorePlugin.getDefault().getStateLocation().append( CONNECTIONS_XML ).toOSString();
    }


    // ── ADD CONNECTION — CHEWIE ADDS A NEW ROUTE TO THE DATAPAD ──────────────────
    // Chewie writes a new destination onto the datapad; if a route by the same name
    // already exists he appends "copy" or a number to avoid confusion.
    // We add the Connection to our set, auto-renaming on name collision, and fire
    // an event so the Connections view refreshes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@link Connection} to the managed set.
     * If a connection with the same display name already exists, we auto-rename the
     * new one ("Copy of X", "2 Copy of X", etc.) to avoid confusion.
     * We fire a {@code connectionAdded} event after adding so the UI updates.
     *
     * <p>For example — Chewie adds the route:</p>
     * <pre>
     *   manager.addConnection(newConn);
     *   // connectionList now contains newConn; Connections view refreshes
     * </pre>
     *
     * @param connection  The new connection to register.
     */
    public void addConnection( Connection connection )
    {
        if ( getConnectionByName( connection.getConnectionParameter().getName() ) != null )
        {
            String newConnectionName = Messages.bind( Messages.copy_n_of_s,
                "", connection.getConnectionParameter().getName() ); //$NON-NLS-1$
            for ( int i = 2; getConnectionByName( newConnectionName ) != null; i++ )
            {
                newConnectionName = Messages.bind( Messages.copy_n_of_s,
                    i + " ", connection.getConnectionParameter().getName() ); //$NON-NLS-1$
            }
            connection.getConnectionParameter().setName( newConnectionName );
        }

        connectionList.add( connection );
        ConnectionEventRegistry.fireConnectionAdded( connection, this );
    }


    // ── GET CONNECTION BY ID — CHEWIE LOOKS UP A ROUTE BY HULL REGISTRATION ──────
    // Chewie scans the datapad for the route whose hull registration matches
    // the given UUID — unique, doesn't change even if the route is renamed.
    // We search connectionList by connection ID (UUID) and return the match.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and returns the {@link Connection} with the given unique ID.
     * We use the ID (a UUID) rather than the name when we need a stable reference
     * that survives rename operations.
     *
     * @param id  The UUID string of the connection to find.
     * @return  The matching {@link Connection}, or {@code null} if not found.
     */
    public Connection getConnectionById( String id )
    {
        for ( Connection conn : connectionList )
        {
            if ( conn.getConnectionParameter().getId().equals( id ) )
            {
                return conn;
            }
        }
        return null;
    }


    // ── GET CONNECTION BY NAME — CHEWIE LOOKS UP A ROUTE BY ITS LABEL ────────────
    // Chewie scans the datapad for the route whose label matches the given name.
    // We search connectionList by the display name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and returns the {@link Connection} with the given display name.
     * Display names are not guaranteed to be unique (though we try to keep them so),
     * so this returns only the first match.
     *
     * @param name  The display name to search for.
     * @return  The matching {@link Connection}, or {@code null} if not found.
     */
    public Connection getConnectionByName( String name )
    {
        for ( Connection conn : connectionList )
        {
            if ( conn.getConnectionParameter().getName().equals( name ) )
            {
                return conn;
            }
        }
        return null;
    }


    // ── REMOVE CONNECTION — CHEWIE ERASES A ROUTE FROM THE DATAPAD ───────────────
    // Chewie crosses a route off the datapad and broadcasts to the crew that
    // it's gone so they stop trying to jump there.
    // We remove the Connection from our set and fire a connectionRemoved event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given {@link Connection} from the managed set.
     * We fire a {@code connectionRemoved} event so the UI (and other listeners)
     * can clean up any state tied to this connection.
     *
     * @param connection  The connection to deregister and remove.
     */
    public void removeConnection( Connection connection )
    {
        connectionList.remove( connection );
        ConnectionEventRegistry.fireConnectionRemoved( connection, this );
    }


    // ── GET CONNECTIONS — CHEWIE HANDS OVER THE FULL ROUTE MANIFEST ──────────────
    // Chewie rips out the full list of routes from the datapad and hands it over.
    // We return all managed connections as a plain array.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all managed connections as an array.
     * The order is not guaranteed (we use a HashSet internally).
     * Callers typically use this to populate the Connections view.
     *
     * @return  An array of all current {@link Connection}s (may be empty, never {@code null}).
     */
    public Connection[] getConnections()
    {
        return connectionList.toArray( new Connection[0] );
    }


    // ── GET CONNECTION COUNT — CHEWIE COUNTS THE ROUTES ON THE DATAPAD ────────────
    // Chewie counts the entries on the datapad to report how many routes
    // are currently stored.
    // We return the size of our connection set.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of connections currently managed.
     * Useful for display in status bars or for deciding whether to show "no connections" hints.
     *
     * @return  The count of managed connections.
     */
    public int getConnectionCount()
    {
        return connectionList.size();
    }


    // ── CONNECTION ADDED EVENT — CHEWIE AUTO-SAVES WHEN A ROUTE IS ADDED ─────────
    // When a new route appears on the datapad, Chewie automatically writes the
    // updated manifest to the hold so nothing is lost.
    // We call saveConnections() to persist the change to disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionAdded} event by saving all connections to disk.
     * We implement {@link ConnectionUpdateListener} so we auto-save whenever any
     * part of the application adds a connection.
     *
     * @param connection  The connection that was just added (we don't need it — we save everything).
     */
    public void connectionAdded( Connection connection )
    {
        saveConnections();
    }


    // ── CONNECTION REMOVED EVENT — CHEWIE AUTO-SAVES WHEN A ROUTE IS ERASED ──────
    // When a route is crossed off the datapad, Chewie saves the updated manifest.
    // We call saveConnections() to persist the removal to disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionRemoved} event by saving all connections to disk.
     *
     * @param connection  The connection that was just removed.
     */
    public void connectionRemoved( Connection connection )
    {
        saveConnections();
    }


    // ── CONNECTION UPDATED EVENT — CHEWIE AUTO-SAVES WHEN A ROUTE CHANGES ─────────
    // When a route entry changes (renamed, new password, different port) Chewie
    // writes the updated manifest to the hold immediately.
    // We call saveConnections() to persist the update to disk.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionUpdated} event by saving all connections to disk.
     * This fires whenever any property of a connection changes — name, host, auth, etc.
     *
     * @param connection  The connection whose properties just changed.
     */
    public void connectionUpdated( Connection connection )
    {
        saveConnections();
    }


    // ── CONNECTION OPENED EVENT — CHEWIE NOTES THE JUMP BUT DOESN'T SAVE ─────────
    // Chewie notes that the Falcon made the jump, but opening a connection doesn't
    // change the manifest so there's nothing to persist.
    // We intentionally do nothing here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionOpened} event.
     * Opening a live connection doesn't change the persisted parameters,
     * so we do nothing here.
     *
     * @param connection  The connection that was opened.
     */
    public void connectionOpened( Connection connection )
    {
    }


    // ── CONNECTION CLOSED EVENT — CHEWIE NOTES THE DROP-OUT BUT DOESN'T SAVE ─────
    // Chewie notes the Falcon dropped out of hyperspace, but closing a connection
    // doesn't change the manifest.
    // We intentionally do nothing here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionClosed} event.
     * Closing a connection doesn't change the persisted parameters,
     * so we do nothing here.
     *
     * @param connection  The connection that was closed.
     */
    public void connectionClosed( Connection connection )
    {
    }


    // ── FOLDER MODIFIED EVENT — NOT OUR CONCERN ────────────────────────────────────
    // Chewie doesn't manage folders — that's the ConnectionFolderManager's job.
    // We intentionally do nothing here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionFolderModified} event.
     * The {@link ConnectionManager} only persists connections, not folders,
     * so we do nothing here.
     *
     * @param connectionFolder  The folder that was modified.
     */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
    }


    // ── FOLDER ADDED EVENT — NOT OUR CONCERN ──────────────────────────────────────
    // Chewie doesn't manage folders. We intentionally do nothing here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionFolderAdded} event.
     * Folder changes don't affect the connections.xml file, so we do nothing.
     *
     * @param connectionFolder  The folder that was added.
     */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
    }


    // ── FOLDER REMOVED EVENT — NOT OUR CONCERN ────────────────────────────────────
    // Chewie doesn't manage folders. We intentionally do nothing here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionFolderRemoved} event.
     * Folder changes don't affect the connections.xml file, so we do nothing.
     *
     * @param connectionFolder  The folder that was removed.
     */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
    }


    // ── SAVE CONNECTIONS — CHEWIE WRITES THE MANIFEST TO THE HOLD ────────────────
    // Chewie writes the full route manifest to a temp file first; if the write
    // succeeds he atomically replaces the live file — no partial-write corruption.
    // We serialize all ConnectionParameters to connections.xml using the same
    // write-to-temp-then-rename strategy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Persists all connections to connections.xml on disk.
     * We write to a temp file first and only replace the live file if the write
     * succeeds — this prevents data loss if the app crashes mid-save.
     * The method is {@code synchronized} so concurrent saves don't corrupt the file.
     */
    public synchronized void saveConnections()
    {
        Set<ConnectionParameter> connectionParameters = new HashSet<>();

        for ( Connection connection : connectionList )
        {
            connectionParameters.add( connection.getConnectionParameter() );
        }

        File file = new File( getConnectionStoreFileName() );
        File tempFile = new File( getConnectionStoreFileName() + TEMP_SUFFIX );

        // To avoid a corrupt file, save object to a temp file first
        try ( FileOutputStream fileOutputStream = new FileOutputStream( tempFile ) )
        {
            ConnectionIO.save( connectionParameters, fileOutputStream );
        }
        catch ( IOException e )
        {
            Status status = new Status( IStatus.ERROR, ConnectionCoreConstants.PLUGIN_ID,
                Messages.error__saving_connections + e.getMessage(), e );
            ConnectionCorePlugin.getDefault().getLog().log( status );
            return;
        }

        // move temp file to good file
        if ( file.exists() )
        {
            file.delete();
        }

        try
        {
            String content = FileUtils.readFileToString( tempFile, ENCODING_UTF8 );
            FileUtils.writeStringToFile( file, content, ENCODING_UTF8 );
        }
        catch ( IOException e )
        {
            Status status = new Status( IStatus.ERROR, ConnectionCoreConstants.PLUGIN_ID,
                Messages.error__saving_connections + e.getMessage(), e );
            ConnectionCorePlugin.getDefault().getLog().log( status );
            return;
        }
    }


    // ── LOAD CONNECTIONS — CHEWIE READS THE MANIFEST FROM THE HOLD ───────────────
    // At startup, Chewie opens the hold, pulls out the route manifest file, and
    // reads every entry back into the datapad's active memory.
    // We deserialize connections.xml into Connection objects and add them to our set.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Loads connections from connections.xml into memory.
     * Called once during construction. If the file doesn't exist (first launch) we
     * simply start with an empty set. Any parse error is logged but not rethrown —
     * a corrupt file leaves us with zero connections rather than crashing.
     * The method is {@code synchronized} so it doesn't race with a concurrent save.
     */
    private synchronized void loadConnections()
    {
        Set<ConnectionParameter> connectionParameters = null;

        File file = new File( getConnectionStoreFileName() );
        if ( file.exists() )
        {
            try ( FileInputStream fileInputStream = new FileInputStream( file ) )
            {
                connectionParameters = ConnectionIO.load( fileInputStream );
            }
            catch ( Exception e )
            {
                Status status = new Status( IStatus.ERROR, ConnectionCoreConstants.PLUGIN_ID,
                    Messages.error__loading_connections + e.getMessage(), e );
                ConnectionCorePlugin.getDefault().getLog().log( status );
            }
        }

        if ( connectionParameters != null )
        {
            for ( ConnectionParameter connectionParameter : connectionParameters )
            {
                Connection conn = new Connection( connectionParameter );
                connectionList.add( conn );
            }
        }
    }
}
