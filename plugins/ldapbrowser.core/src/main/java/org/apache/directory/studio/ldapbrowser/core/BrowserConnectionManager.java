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


import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.directory.api.util.FileUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.core.io.ConnectionIOException;
import org.apache.directory.studio.ldapbrowser.core.events.BookmarkUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.BookmarkUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.BrowserConnectionUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.BrowserConnectionUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.model.BookmarkParameter;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Bookmark;
import org.apache.directory.studio.ldapbrowser.core.model.impl.BrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;


// ── CLASS: BrowserConnectionManager — LANDO RUNS CLOUD CITY'S ENTIRE FLEET ───
// Lando Calrissian doesn't just manage one docking bay — he runs ALL of Cloud
// City's operations: registering new ships as they arrive, decommissioning
// ones that depart, saving the manifest to the permanent ledger, and reacting
// to alerts from his communications team (searches, bookmarks, schema updates).
// This class is that full operations centre: it maps raw Eclipse {@link Connection}
// objects to browser-level {@link IBrowserConnection} objects, persists the
// mapping (plus searches and bookmarks) to disk, and keeps everything in sync
// when the lower-level connection layer fires events.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central manager for all {@link IBrowserConnection} objects in the
 * ldapbrowser.core plugin.
 * It wraps the lower-level Eclipse {@link Connection} objects with richer
 * {@link BrowserConnection} objects that carry the LDAP schema, search
 * history, and bookmarks.  It persists all of this to
 * {@code browserconnections.xml} and reloads it at startup.
 * It also listens to connection, search, and bookmark events so it can
 * auto-save whenever something changes.
 * Think of this class as Lando running Cloud City's entire fleet operations.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserConnectionManager implements ConnectionUpdateListener, BrowserConnectionUpdateListener,
    SearchUpdateListener, BookmarkUpdateListener
{

    /** The list of connections. */
    private Map<String, IBrowserConnection> connectionMap;


    // ── Lando Opens The Operations Centre ────────────────────────────────────────
    // Lando walks into the Cloud City operations centre on the first day,
    // initialises all the consoles, loads the existing ship manifest from the
    // permanent ledger, and registers himself with the comms team so he gets
    // alerts when ships arrive, depart, or change status.
    // We load all existing browser connections from disk (suppressing events so
    // we don't spam listeners during startup) and register with every relevant
    // event system.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the connection manager, loads persisted browser connections
     * from disk, and registers event listeners.
     * Event firing is suspended during the load phase so we don't flood
     * listeners with synthetic change events for every connection loaded from
     * the saved file.
     *
     * <p>For example — Lando opens the operations centre:</p>
     * <pre>
     *   EventRegistry.suspendEventFiringInCurrentThread();
     *   loadBrowserConnections();         // load the ledger
     *   EventRegistry.resumeEventFiringInCurrentThread();
     *   ConnectionEventRegistry.addConnectionUpdateListener(this, ...); // join the comms channel
     * </pre>
     */
    public BrowserConnectionManager()
    {
        this.connectionMap = new HashMap<String, IBrowserConnection>();

        // no need to fire events while loading connections
        EventRegistry.suspendEventFiringInCurrentThread();
        loadBrowserConnections();
        EventRegistry.resumeEventFiringInCurrentThread();

        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionCorePlugin.getDefault().getEventRunner() );
        EventRegistry.addSearchUpdateListener( this, BrowserCorePlugin.getDefault().getEventRunner() );
        EventRegistry.addBookmarkUpdateListener( this, BrowserCorePlugin.getDefault().getEventRunner() );
        EventRegistry.addBrowserConnectionUpdateListener( this, BrowserCorePlugin.getDefault().getEventRunner() );
    }


    // ── Lando Looks Up The Manifest Filename For A Bay ───────────────────────────
    // Lando needs to know where the cached schema file for a given bay is stored
    // on disk — "schema-{id}.ldif lives in the operations ledger directory."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the absolute filesystem path where the LDAP schema cache LDIF
     * file for a given browser connection ID is stored.
     * Schema files are named {@code schema-{sanitized-id}.ldif} and live in
     * the plugin's state location (Eclipse manages this directory).
     *
     * <p>For example — Lando looks up a bay's schema file path:</p>
     * <pre>
     *   String path = BrowserConnectionManager.getSchemaCacheFileName("abc123");
     *   // path == ".../state/schema-abc123.ldif"
     * </pre>
     *
     * @param id  the connection ID whose schema cache file path we want.
     * @return the absolute OS path string for the schema cache LDIF file.
     */
    public static final String getSchemaCacheFileName( String id )
    {
        return BrowserCorePlugin.getDefault().getStateLocation().append(
            "schema-" + Utils.getFilenameString( id ) + ".ldif" ).toOSString(); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── Lando Finds The Permanent Ledger File ────────────────────────────────────
    // Lando needs the path to the permanent fleet ledger on disk.
    // If the new ledger doesn't exist yet, he checks for old-format ledgers
    // left over from a previous installation and migrates them automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the absolute filesystem path of the {@code browserconnections.xml}
     * persistence file.
     * If the file doesn't exist we try to migrate an older file written by a
     * previous version of the plugin (different package name and/or workspace
     * directory) by reading it, updating class-name references, and writing it
     * to the new location.
     *
     * <p>For example — Lando locates the fleet ledger:</p>
     * <pre>
     *   String filename = BrowserConnectionManager.getBrowserConnectionStoreFileName();
     *   // "~/.ApacheDirectoryStudio/.../browserconnections.xml"
     * </pre>
     *
     * @return the absolute OS path of the browser-connection store file.
     */
    public static final String getBrowserConnectionStoreFileName()
    {
        String filename = BrowserCorePlugin.getDefault().getStateLocation()
            .append( "browserconnections.xml" ).toOSString(); //$NON-NLS-1$
        File file = new File( filename );
        if ( !file.exists() )
        {
            // try to convert old connections.xml:
            // 1st search it in current workspace with the old ldapstudio plugin ID
            // 2nd search it in old .ldapstudio workspace with the old ldapstudio plugin ID
            String[] oldFilenames = new String[2];
            oldFilenames[0] = filename.replace( "org.apache.directory.studio.ldapbrowser.core", //$NON-NLS-1$
                "org.apache.directory.ldapstudio.browser.core" ); //$NON-NLS-1$
            oldFilenames[1] = oldFilenames[0].replace( ".ApacheDirectoryStudio", ".ldapstudio" ); //$NON-NLS-1$ //$NON-NLS-2$
            for ( int i = 0; i < oldFilenames.length; i++ )
            {
                File oldFile = new File( oldFilenames[i] );
                if ( oldFile.exists() )
                {
                    try
                    {
                        String oldContent = FileUtils.readFileToString( oldFile, "UTF-8" ); //$NON-NLS-1$
                        String newContent = oldContent.replace( "org.apache.directory.ldapstudio.browser.core", //$NON-NLS-1$
                            "org.apache.directory.studio.ldapbrowser.core" ); //$NON-NLS-1$
                        FileUtils.writeStringToFile( file, newContent, "UTF-8" ); //$NON-NLS-1$
                        break;
                    }
                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        return filename;
    }


    // ── Lando Looks Up A Ship By Its Registry Number ─────────────────────────────
    // "Control, give me the full dossier for the ship with registry number 42-X."
    // Lando's operator does a direct lookup by ID in the connection map.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IBrowserConnection} with the given unique ID, or
     * {@code null} if no such connection is registered.
     * Connection IDs come from the underlying Eclipse connection layer.
     *
     * <p>For example — Lando looks up by registry number:</p>
     * <pre>
     *   IBrowserConnection bc = manager.getBrowserConnectionById("abc123");
     * </pre>
     *
     * @param id  the connection's unique string ID.
     * @return the matching {@link IBrowserConnection}, or {@code null}.
     */
    public IBrowserConnection getBrowserConnectionById( String id )
    {
        return connectionMap.get( id );
    }


    // ── Lando Looks Up A Ship By Its Callsign ────────────────────────────────────
    // "Control, find me the ship named 'Millennium Falcon'."
    // Lando delegates to the lower-level connection layer to find the raw
    // Connection by name, then wraps it with the browser connection dossier.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IBrowserConnection} whose underlying raw connection
     * has the given display name, or {@code null} if not found.
     *
     * <p>For example — Lando looks up by callsign:</p>
     * <pre>
     *   IBrowserConnection bc = manager.getBrowserConnectionByName("My LDAP Server");
     * </pre>
     *
     * @param name  the human-readable name of the connection to look up.
     * @return the matching {@link IBrowserConnection}, or {@code null}.
     */
    public IBrowserConnection getBrowserConnectionByName( String name )
    {
        Connection connection = ConnectionCorePlugin.getDefault().getConnectionManager().getConnectionByName( name );
        return getBrowserConnection( connection );
    }


    // ── Lando Looks Up A Ship By Its Raw Transponder ─────────────────────────────
    // "I have the raw transponder object for the Falcon — give me its full
    // Cloud City dossier."
    // We wrap the low-level {@link Connection} to find the corresponding
    // browser-level {@link IBrowserConnection}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IBrowserConnection} that wraps the given raw
     * {@link Connection}, or {@code null} if the connection is not registered
     * or the argument is {@code null}.
     *
     * <p>For example — Lando looks up by raw transponder:</p>
     * <pre>
     *   IBrowserConnection bc = manager.getBrowserConnection(rawConnection);
     * </pre>
     *
     * @param connection  the raw network connection to look up; may be
     *                    {@code null} (returns {@code null}).
     * @return the corresponding {@link IBrowserConnection}, or {@code null}.
     */
    public IBrowserConnection getBrowserConnection( Connection connection )
    {
        return connection != null ? getBrowserConnectionById( connection.getId() ) : null;
    }


    // ── Lando Prints The Full Fleet Manifest ─────────────────────────────────────
    // "Give me the complete list of every ship currently registered with Cloud
    // City."  Lando's operator hands back the full manifest as an array.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all registered browser connections as an array.
     * The order is unspecified (it's a map internally).
     *
     * <p>For example — Lando prints the fleet manifest:</p>
     * <pre>
     *   IBrowserConnection[] all = manager.getBrowserConnections();
     *   for (IBrowserConnection bc : all) { System.out.println(bc.toString()); }
     * </pre>
     *
     * @return an array (possibly empty, never {@code null}) of all registered
     *         {@link IBrowserConnection}s.
     */
    public IBrowserConnection[] getBrowserConnections()
    {
        return ( IBrowserConnection[] ) connectionMap.values().toArray( new IBrowserConnection[0] );
    }


    // ── Lando De-registers A Departed Ship ───────────────────────────────────────
    // A ship has left Cloud City permanently — Lando removes it from the active
    // manifest, deletes its personal schema file from the ledger, and saves the
    // updated manifest to disk.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to a connection being removed from the Eclipse connection layer.
     * We remove it from our map, delete its cached schema file, and persist
     * the updated connection store.
     *
     * <p>For example — Lando de-registers a ship:</p>
     * <pre>
     *   connectionMap.remove(connection.getId());
     *   schemaFile.delete();
     *   saveBrowserConnections();
     * </pre>
     *
     * @param connection  the raw connection that was removed.
     */
    public void connectionRemoved( Connection connection )
    {
        // update connection list
        connectionMap.remove( connection.getId() );

        // remove schema file
        File schemaFile = new File( getSchemaCacheFileName( connection.getId() ) );
        if ( schemaFile.exists() )
        {
            schemaFile.delete();
        }

        // make persistent
        saveBrowserConnections();
    }


    // ── Lando Registers A Newly Arrived Ship ─────────────────────────────────────
    // A new ship has docked at Cloud City — Lando logs it in the manifest with a
    // fresh browser connection dossier and saves the updated manifest to disk.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to a new connection being added to the Eclipse connection layer.
     * We wrap it in a {@link BrowserConnection}, register it in our map, and
     * persist the updated store.
     *
     * <p>For example — Lando registers a new arrival:</p>
     * <pre>
     *   connectionMap.put(connection.getId(), new BrowserConnection(connection));
     *   saveBrowserConnections();
     * </pre>
     *
     * @param connection  the newly added raw connection.
     */
    public void connectionAdded( Connection connection )
    {
        // update connection list
        BrowserConnection browserConnection = new BrowserConnection( connection );
        connectionMap.put( connection.getId(), browserConnection );

        // make persistent
        saveBrowserConnections();
    }


    // ── Lando Updates A Ship's Dossier And Schema ────────────────────────────────
    // A ship's registration details have changed — Lando updates the permanent
    // ledger and refreshes the schema file on disk.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to a connection being updated (name, host, authentication changed).
     * We save the browser connections store and also flush the schema cache for
     * that connection so we don't serve stale schema data.
     *
     * @param connection  the raw connection whose settings changed.
     */
    public void connectionUpdated( Connection connection )
    {
        saveBrowserConnections();
        saveSchema( getBrowserConnection( connection ) );
    }


    // ── Lando Acknowledges A Ship Opening (No Action Needed) ─────────────────────
    // A ship's engines have come online — Lando nods but doesn't update the
    // manifest; BrowserConnectionListener handles the actual bootstrap.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to a connection being opened.
     * The actual browser-model bootstrap (schema load, root DSE fetch) is
     * handled by {@link BrowserConnectionListener}; this manager does nothing
     * on open.
     *
     * @param connection  the raw connection that just opened.
     */
    public void connectionOpened( Connection connection )
    {
    }


    // ── Lando Acknowledges A Ship Closing (No Action Needed) ─────────────────────
    // A ship has powered down — Lando nods; BrowserConnectionListener handles
    // the cache wipe.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to a connection being closed.
     * Cache clearing is handled by {@link BrowserConnectionListener}; this
     * manager does nothing on close.
     *
     * @param connection  the raw connection that just closed.
     */
    public void connectionClosed( Connection connection )
    {
    }


    // ── Lando Acknowledges A Folder Change (No Action Needed) ────────────────────
    // The folder grouping of ships in the manifest changed — Lando doesn't need
    // to update his own records for this.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to a connection folder being modified.
     * We don't need to do anything in response — connection folders are a
     * UI-layer concern, not a browser-core concern.
     *
     * @param connectionFolder  the folder that was modified.
     */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
    }


    // ── Lando Acknowledges A Folder Added (No Action Needed) ─────────────────────
    // A new organisational folder was added to the manifest — Lando acknowledges
    // it but doesn't change his own records.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to a connection folder being added.
     * No action needed from the browser-core manager.
     *
     * @param connectionFolder  the newly added folder.
     */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
    }


    // ── Lando Acknowledges A Folder Removed (No Action Needed) ───────────────────
    // An organisational folder was removed from the manifest — Lando doesn't
    // change his own records.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to a connection folder being removed.
     * No action needed from the browser-core manager.
     *
     * @param connectionFolder  the removed folder.
     */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
    }


    // ── Lando Saves The Schema When A Schema Update Is Signaled ──────────────────
    // The comms team alerts Lando that the schema for a connection has just been
    // reloaded from the server — he saves the fresh schema to the schema cache
    // file on disk so it's available next startup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to browser-connection update events.
     * When a SCHEMA_UPDATED event arrives we flush the schema to its cache
     * file on disk so the next startup doesn't have to re-fetch it from the
     * LDAP server.
     *
     * @param browserConnectionUpdateEvent  the event describing what changed.
     */
    public void browserConnectionUpdated( BrowserConnectionUpdateEvent browserConnectionUpdateEvent )
    {
        if ( browserConnectionUpdateEvent.getDetail() == BrowserConnectionUpdateEvent.Detail.SCHEMA_UPDATED )
        {
            saveSchema( browserConnectionUpdateEvent.getBrowserConnection() );
        }
    }


    // ── Lando Saves The Manifest When A Search Changes ───────────────────────────
    // Lando's comms team reports that a search has been added, removed, renamed,
    // or had its parameters updated — he immediately saves the manifest to disk
    // so the change is not lost if the application crashes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to search update events by persisting the browser connections.
     * We save on SEARCH_ADDED, SEARCH_REMOVED, SEARCH_RENAMED, and
     * SEARCH_PARAMETER_UPDATED so the disk state is always current.
     *
     * @param searchUpdateEvent  the event describing what changed.
     */
    public void searchUpdated( SearchUpdateEvent searchUpdateEvent )
    {
        if ( searchUpdateEvent.getDetail() == SearchUpdateEvent.EventDetail.SEARCH_ADDED
            || searchUpdateEvent.getDetail() == SearchUpdateEvent.EventDetail.SEARCH_REMOVED
            || searchUpdateEvent.getDetail() == SearchUpdateEvent.EventDetail.SEARCH_RENAMED
            || searchUpdateEvent.getDetail() == SearchUpdateEvent.EventDetail.SEARCH_PARAMETER_UPDATED )
        {
            saveBrowserConnections();
        }
    }


    // ── Lando Saves The Manifest When A Bookmark Changes ─────────────────────────
    // Lando's comms team reports that a bookmark has been added, removed, or
    // updated — he immediately saves the manifest to disk.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reacts to bookmark update events by persisting the browser connections.
     * We save on BOOKMARK_ADDED, BOOKMARK_REMOVED, and BOOKMARK_UPDATED.
     *
     * @param bookmarkUpdateEvent  the event describing what changed.
     */
    public void bookmarkUpdated( BookmarkUpdateEvent bookmarkUpdateEvent )
    {
        if ( bookmarkUpdateEvent.getDetail() == BookmarkUpdateEvent.Detail.BOOKMARK_ADDED
            || bookmarkUpdateEvent.getDetail() == BookmarkUpdateEvent.Detail.BOOKMARK_REMOVED
            || bookmarkUpdateEvent.getDetail() == BookmarkUpdateEvent.Detail.BOOKMARK_UPDATED )
        {
            saveBrowserConnections();
        }
    }


    // ── Lando Saves The Fleet Manifest To The Permanent Ledger ───────────────────
    // Lando writes the full fleet manifest to the permanent ledger.  To protect
    // against mid-write crashes he writes to a temp file first, then moves it
    // into place — a poor man's atomic write.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Serialises all browser connections to {@code browserconnections.xml},
     * using a temp-file write strategy to protect against partial writes.
     * We write to {@code browserconnections.xml-temp} first, then copy its
     * contents to the real file, minimising the window during which the real
     * file is absent or incomplete.
     *
     * <p>For example — Lando saves the manifest:</p>
     * <pre>
     *   BrowserConnectionIO.save(new FileOutputStream("....-temp"), connectionMap);
     *   Files.move(tempFile, realFile);
     * </pre>
     */
    private void saveBrowserConnections()
    {
        // To avoid a corrupt file, save object to a temp file first
        try
        {
            BrowserConnectionIO.save( new FileOutputStream( getBrowserConnectionStoreFileName() + "-temp" ), //$NON-NLS-1$
                connectionMap );
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // move temp file to good file
        File file = new File( getBrowserConnectionStoreFileName() );
        File tempFile = new File( getBrowserConnectionStoreFileName() + "-temp" ); //$NON-NLS-1$
        if ( file.exists() )
        {
            file.delete();
        }

        try
        {
            String content = FileUtils.readFileToString( tempFile, "UTF-8" ); //$NON-NLS-1$
            FileUtils.writeStringToFile( file, content, "UTF-8" ); //$NON-NLS-1$
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //        Object[][] object = new Object[connectionMap.size()][3];
        //
        //        Iterator<IBrowserConnection> connectionIterator = connectionMap.values().iterator();
        //        for ( int i = 0; connectionIterator.hasNext(); i++ )
        //        {
        //            IBrowserConnection browserConnection = connectionIterator.next();
        //
        //            ISearch[] searches = browserConnection.getSearchManager().getSearches();
        //            SearchParameter[] searchParameters = new SearchParameter[searches.length];
        //            for ( int k = 0; k < searches.length; k++ )
        //            {
        //                searchParameters[k] = searches[k].getSearchParameter();
        //            }
        //
        //            IBookmark[] bookmarks = browserConnection.getBookmarkManager().getBookmarks();
        //            BookmarkParameter[] bookmarkParameters = new BookmarkParameter[bookmarks.length];
        //            for ( int k = 0; k < bookmarks.length; k++ )
        //            {
        //                bookmarkParameters[k] = bookmarks[k].getBookmarkParameter();
        //            }
        //
        //            object[i][0] = browserConnection.getConnection().getId();
        //            object[i][1] = searchParameters;
        //            object[i][2] = bookmarkParameters;
        //        }
        //
        //        save( object, getBrowserConnectionStoreFileName() );
    }


    // ── Lando Saves A Ship's Schema Dossier To The Archive ───────────────────────
    // Lando writes the schema details for a specific ship to its personal schema
    // archive file so we don't have to fetch them from the LDAP server every
    // startup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Writes the LDAP schema for the given browser connection to its schema
     * cache LDIF file.
     * If the browser connection is {@code null} or its raw connection is missing
     * the method returns silently.  Errors during writing are printed to stderr
     * (this is a best-effort cache, not critical path).
     *
     * <p>For example — Lando archives a ship's schema:</p>
     * <pre>
     *   saveSchema(myBrowserConnection);
     *   // writes to "schema-{id}.ldif" in the plugin state directory
     * </pre>
     *
     * @param browserConnection  the connection whose schema to save; may be
     *                           {@code null} (returns immediately).
     */
    private void saveSchema( IBrowserConnection browserConnection )
    {
        if ( browserConnection == null )
        {
            return;
        }

        try
        {
            String filename = getSchemaCacheFileName( browserConnection.getConnection().getId() );
            FileWriter writer = new FileWriter( filename );
            browserConnection.getSchema().saveToLdif( writer );
            writer.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    // ── Lando Loads The Permanent Ledger At Startup ───────────────────────────────
    // At the start of each day, Lando's operator loads the full fleet manifest
    // from the permanent ledger into the in-memory map.  This covers both the
    // new XML format and the old Java-serialization format written by older
    // versions, and tries a backup temp file if the primary file is corrupt.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads all browser connections (with their schema caches, searches, and
     * bookmarks) from the persistence files on disk into the in-memory map.
     * Handles three scenarios:
     * <ol>
     *   <li>New XML format via {@link BrowserConnectionIO#load}.</li>
     *   <li>Old Java-serialization (XMLDecoder) format for backward compat.</li>
     *   <li>Falls back to the {@code -temp} file if the primary is corrupt.</li>
     * </ol>
     *
     * <p>For example — Lando loads the morning manifest:</p>
     * <pre>
     *   loadBrowserConnections();
     *   // connectionMap now has all connections with their saved searches
     * </pre>
     */
    private void loadBrowserConnections()
    {
        Connection[] connections = ConnectionCorePlugin.getDefault().getConnectionManager().getConnections();
        for ( int i = 0; i < connections.length; i++ )
        {
            Connection connection = connections[i];
            BrowserConnection browserConnection = new BrowserConnection( connection );
            connectionMap.put( connection.getId(), browserConnection );

            try
            {
                String schemaFilename = getSchemaCacheFileName( connection.getId() );
                FileReader reader = new FileReader( schemaFilename );
                Schema schema = new Schema();
                schema.loadFromLdif( reader );
                browserConnection.setSchema( schema );
            }
            catch ( Exception e )
            {
            }
        }

        // java.beans.XMLDecoder
        try
        {
            String fileName = getBrowserConnectionStoreFileName();
            File file = new File( fileName );
            if ( file.exists() )
            {
                String oldContent = FileUtils.readFileToString( file, "UTF-8" ); //$NON-NLS-1$
                if ( !oldContent.contains( "java.beans.XMLDecoder" ) ) //$NON-NLS-1$
                {
                    // new file format
                    try
                    {
                        BrowserConnectionIO.load( new FileInputStream( getBrowserConnectionStoreFileName() ),
                            connectionMap );
                    }
                    catch ( Exception e )
                    {
                        // If loading failed, try with temp file
                        try
                        {
                            BrowserConnectionIO.load( new FileInputStream( getBrowserConnectionStoreFileName()
                                + "-temp" ), connectionMap ); //$NON-NLS-1$
                        }
                        catch ( FileNotFoundException e1 )
                        {
                            // TODO Auto-generated catch block
                            return;
                        }
                        catch ( ConnectionIOException e1 )
                        {
                            // TODO Auto-generated catch block
                            return;
                        }
                    }
                }
                else
                {
                    // old file format
                    Object[][] object = ( Object[][] ) this.load( getBrowserConnectionStoreFileName() );

                    if ( object != null )
                    {
                        try
                        {
                            for ( int i = 0; i < object.length; i++ )
                            {
                                String connectionId = ( String ) object[i][0];
                                IBrowserConnection browserConnection = getBrowserConnectionById( connectionId );

                                if ( browserConnection != null )
                                {
                                    if ( object[i].length > 0 )
                                    {
                                        SearchParameter[] searchParameters = ( SearchParameter[] ) object[i][1];
                                        for ( int k = 0; k < searchParameters.length; k++ )
                                        {
                                            ISearch search = new Search( browserConnection, searchParameters[k] );
                                            browserConnection.getSearchManager().addSearch( search );
                                        }
                                    }

                                    if ( object[i].length > 1 )
                                    {
                                        BookmarkParameter[] bookmarkParameters = ( BookmarkParameter[] ) object[i][2];
                                        for ( int k = 0; k < bookmarkParameters.length; k++ )
                                        {
                                            IBookmark bookmark = new Bookmark( browserConnection, bookmarkParameters[k] );
                                            browserConnection.getBookmarkManager().addBookmark( bookmark );
                                        }
                                    }
                                }
                            }
                        }
                        catch ( ArrayIndexOutOfBoundsException e )
                        {
                            // Thrown by decoder.readObject(), signals EOF
                        }
                        catch ( Exception e )
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
        }

    }


    // ── Lando Reads The Old-Format Ledger Via Legacy Decoder ─────────────────────
    // Some old ledgers were written in Java's native XML serialization format
    // (XMLDecoder).  Lando keeps a legacy decoder on hand to read those old files;
    // if the primary file is corrupt he tries the temp copy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deserialises the object graph from an old-style Java XMLDecoder file.
     * Used only for backward compatibility with files written by ancient versions
     * of the plugin.  If the primary file fails to load we try the {@code -temp}
     * backup; if that also fails we return {@code null}.
     *
     * <p>For example — Lando reads the legacy ledger:</p>
     * <pre>
     *   Object[][] data = (Object[][]) load("/path/to/browserconnections.xml");
     * </pre>
     *
     * @param filename  the path to the XML file to decode.
     * @return the deserialized object, or {@code null} if loading fails.
     */
    private synchronized Object load( String filename )
    {
        try
        {
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( ( new FileInputStream( filename ) ) ) );
            Object object = decoder.readObject();
            decoder.close();
            return object;
        }
        catch ( IOException ioe )
        {
            return null;
        }
        catch ( Exception e )
        {
            // if loading failed, try with temp file
            String tempFilename = filename + "-temp"; //$NON-NLS-1$
            try
            {
                XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( ( new FileInputStream( tempFilename ) ) ) );
                Object object = decoder.readObject();
                decoder.close();
                return object;
            }
            catch ( IOException ioe2 )
            {
                return null;
            }
            catch ( Exception e2 )
            {
                return null;
            }
        }
    }

    // ── CLASS: TypeSafeEnumPersistenceDelegate — HAN SHOOTS FIRST ────────────────
    // Han Solo doesn't wait for Greedo to draw — he handles the situation on his
    // own terms before the standard protocol would kick in.
    // This delegate intercepts Java's XMLEncoder before it tries to persist enum
    // constants via their default mechanism (which breaks for type-safe enums)
    // and substitutes a field-reference Expression instead.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * A {@link PersistenceDelegate} that correctly serialises type-safe enum
     * constants (pre-Java-5 style {@code public static final} fields) using
     * the old Java Beans XML encoder.
     * The standard encoder doesn't know how to reconstruct such objects, so we
     * teach it by finding the matching public static final field and emitting a
     * field-get expression.
     * Think of this as Han shooting first — we handle the enum before the
     * default serializer creates a mess.
     */
    class TypeSafeEnumPersistenceDelegate extends PersistenceDelegate
    {
        // ── Han Checks Whether He's Dealing With The Same Guy ────────────────────
        // Han sizes up the two figures across the table — if they're the same
        // person he doesn't need to shoot at all.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if both objects are reference-equal ({@code ==}).
         * For type-safe enum constants, identity equality is sufficient — there
         * is only one instance per constant.
         *
         * @param oldInstance  the object being serialised.
         * @param newInstance  the object already in the target graph.
         * @return {@code true} if they are the same object reference.
         */
        protected boolean mutatesTo( Object oldInstance, Object newInstance )
        {
            return oldInstance == newInstance;
        }


        // ── Han Produces A Field-Reference Instead Of A New Instance ─────────────
        // Han doesn't go through formal channels — he finds the exact field that
        // holds the enum constant and emits a direct reference expression for it.
        // ────────────────────────────────────────────────────────────────────────
        /**
         * Produces the {@link Expression} that the XMLEncoder will emit to
         * reconstruct this type-safe enum constant.
         * We scan all {@code public static final} fields of the class for one
         * whose value is reference-equal to {@code oldInstance}, then emit a
         * {@code field.get(null)} expression.
         *
         * <p>For example — Han finds the field and shoots the reference:</p>
         * <pre>
         *   for (Field f : type.getFields()) {
         *     if (f.get(null) == oldInstance) {
         *       return new Expression(oldInstance, f, "get", new Object[]{ null });
         *     }
         *   }
         * </pre>
         *
         * @param oldInstance  the enum constant to serialise.
         * @param out          the encoder (unused directly).
         * @return an {@link Expression} that reconstructs the constant.
         * @throws IllegalArgumentException if the class is non-public or no
         *                                   matching field is found.
         */
        protected Expression instantiate( Object oldInstance, Encoder out )
        {
            Class<?> type = oldInstance.getClass();
            if ( !Modifier.isPublic( type.getModifiers() ) )
            {
                throw new IllegalArgumentException( "Could not instantiate instance of non-public class: " //$NON-NLS-1$
                    + oldInstance );
            }

            for ( Field field : type.getFields() )
            {
                int mod = field.getModifiers();
                if ( Modifier.isPublic( mod ) && Modifier.isStatic( mod ) && Modifier.isFinal( mod )
                    && ( type == field.getDeclaringClass() ) )
                {
                    try
                    {
                        if ( oldInstance == field.get( null ) )
                        {
                            return new Expression( oldInstance, field, "get", new Object[] //$NON-NLS-1$
                                { null } );
                        }
                    }
                    catch ( IllegalAccessException exception )
                    {
                        throw new IllegalArgumentException( "Could not get value of the field: " + field, exception ); //$NON-NLS-1$
                    }
                }
            }
            throw new IllegalArgumentException( "Could not instantiate value: " + oldInstance ); //$NON-NLS-1$
        }
    }

}
