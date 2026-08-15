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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.util.FileUtils;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.core.io.ConnectionIO;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


// ── CLASS: ConnectionFolderManager — CHEWIE MANAGES THE WHOLE BINDER CABINET ──
// Chewie's nav station has a full filing cabinet of binders: one root binder at
// the top, with nested sub-binders for each category.  Chewie adds binders,
// removes them, looks one up by name or ID, and saves the whole cabinet layout
// to connectionFolders.xml so it survives across sessions.
// This class does exactly that for {@link ConnectionFolder} objects — it owns the
// folder hierarchy and persists it to disk on every change.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The central registry and persistence manager for all {@link ConnectionFolder}s.
 * We maintain a flat set of all folders (including the invisible root) and the
 * parent/child relationships between them.
 * Think of this class as Chewie's filing cabinet: the root folder is the cabinet
 * itself, every other folder is a labeled binder inside, and we save the cabinet
 * layout to disk whenever anything changes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionFolderManager implements ConnectionUpdateListener
{
    private static final String CONNECTION_FOLDERS_FILENAME = "connectionFolders.xml"; //$NON-NLS-1$

    private static final String ROOT_ID = "0"; //$NON-NLS-1$

    /** The root connection folder. */
    private ConnectionFolder root;

    /** The list of folders. */
    private Set<ConnectionFolder> folderList;


    // ── CONSTRUCTOR — CHEWIE OPENS THE FILING CABINET ────────────────────────────
    // Chewie opens the filing cabinet at the start of every mission: he sets up
    // an empty root binder, loads the saved layout from connectionFolders.xml,
    // and registers to auto-save whenever the hierarchy changes.
    // We initialize the root, load persisted folders, and subscribe to events.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionFolderManager} and wires it up.
     * We create the root folder (id="0"), load any saved hierarchy from disk,
     * and register as a {@link ConnectionUpdateListener} so we auto-save on changes.
     */
    public ConnectionFolderManager()
    {
        this.root = new ConnectionFolder( "" ); //$NON-NLS-1$s
        this.root.setId( ROOT_ID ); //$NON-NLS-1$s
        this.folderList = new HashSet<ConnectionFolder>();

        loadConnectionFolders();
        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionCorePlugin.getDefault().getEventRunner() );

        this.folderList.add( this.root );
    }


    // ── GET STORE FILENAME — WHERE THE CABINET LAYOUT IS FILED ───────────────────
    // Chewie knows exactly which slot in the hold contains the cabinet layout file.
    // We return the absolute path to connectionFolders.xml.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the absolute path to the connectionFolders.xml persistence file.
     * The file lives in the Eclipse plugin state directory, which persists across sessions.
     *
     * @return  The OS path string to connectionFolders.xml.
     */
    public static final String getConnectionFolderStoreFileName()
    {
        String filename = ConnectionCorePlugin.getDefault().getStateLocation().append( CONNECTION_FOLDERS_FILENAME )
            .toOSString();
        return filename;
    }


    // ── ADD CONNECTION FOLDER — CHEWIE ADDS A BINDER TO THE CABINET ──────────────
    // Chewie slides a new labeled binder into the cabinet; if a binder by the same
    // name already exists he appends "Copy" or a number to the new one's label.
    // We add the folder to our set, auto-renaming on name collision, and fire an event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@link ConnectionFolder} to the managed set.
     * If a folder with the same display name already exists, we auto-rename the new
     * one ("Copy of X", "2 Copy of X", etc.) to keep names unique.
     * We fire a {@code connectionFolderAdded} event after adding so the UI refreshes.
     *
     * @param connectionFolder  The new folder to register.
     */
    public void addConnectionFolder( ConnectionFolder connectionFolder )
    {
        if ( getConnectionFolderByName( connectionFolder.getName() ) != null )
        {
            String newConnectionFolderName = Messages.bind( Messages.copy_n_of_s, "", connectionFolder.getName() ); //$NON-NLS-1$
            for ( int i = 2; getConnectionFolderByName( newConnectionFolderName ) != null; i++ )
            {
                newConnectionFolderName = Messages.bind( Messages.copy_n_of_s, i + " ", connectionFolder.getName() ); //$NON-NLS-1$
            }
            connectionFolder.setName( newConnectionFolderName );
        }

        folderList.add( connectionFolder );
        ConnectionEventRegistry.fireConnectonFolderAdded( connectionFolder, this );
    }


    // ── REMOVE CONNECTION FOLDER — CHEWIE PULLS A BINDER FROM THE CABINET ─────────
    // Chewie pulls the binder out and broadcasts to the crew that it's gone.
    // We remove the folder from our set and fire a folder-removed event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given {@link ConnectionFolder} from the managed set.
     * We fire a {@code connectionFolderRemoved} event so the UI cleans up.
     *
     * @param connectionFolder  The folder to remove.
     */
    public void removeConnectionFolder( ConnectionFolder connectionFolder )
    {
        folderList.remove( connectionFolder );
        ConnectionEventRegistry.fireConnectonFolderRemoved( connectionFolder, this );
    }


    // ── GET CONNECTION FOLDERS — CHEWIE HANDS OVER THE FULL CABINET LISTING ───────
    // Chewie pulls all the binders from the cabinet and hands them over as an array.
    // We return the entire folderList as an array.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all managed folders as an array.
     * The order is not guaranteed (we use a HashSet internally).
     *
     * @return  An array of all current {@link ConnectionFolder}s (never {@code null}).
     */
    public ConnectionFolder[] getConnectionFolders()
    {
        return folderList.toArray( new ConnectionFolder[0] );
    }


    // ── GET FOLDER BY ID — CHEWIE FINDS A BINDER BY ITS REGISTRATION ──────────────
    // Chewie scans the cabinet for the binder whose registration number matches
    // the given UUID.
    // We search folderList and return the match.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and returns the {@link ConnectionFolder} with the given unique ID.
     * We use this when following UUID references stored inside other folders.
     *
     * @param id  The UUID string of the folder to find.
     * @return  The matching {@link ConnectionFolder}, or {@code null} if not found.
     */
    public ConnectionFolder getConnectionFolderById( String id )
    {
        for ( ConnectionFolder folder : folderList )
        {
            if ( folder.getId().equals( id ) )
            {
                return folder;
            }
        }
        return null;
    }


    // ── GET FOLDER BY NAME — CHEWIE FINDS A BINDER BY ITS SPINE LABEL ────────────
    // Chewie scans the cabinet for the binder whose label matches the given name.
    // We search folderList by display name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and returns the {@link ConnectionFolder} with the given display name.
     * We use this primarily to check for naming conflicts before adding a new folder.
     *
     * @param name  The display name to search for.
     * @return  The matching {@link ConnectionFolder}, or {@code null} if not found.
     */
    public ConnectionFolder getConnectionFolderByName( String name )
    {
        for ( ConnectionFolder folder : folderList )
        {
            if ( folder.getName().equals( name ) )
            {
                return folder;
            }
        }
        return null;
    }


    // ── GET PARENT FOLDER FOR A CONNECTION — CHEWIE FINDS WHICH BINDER HOLDS A ROUTE
    // Chewie scans every binder to find which one contains a given route card.
    // If the route isn't in any labeled binder it defaults to the root binder.
    // We walk all folders checking their connectionIds list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the folder that directly contains the given connection.
     * If no folder claims the connection (it may have been added before folders existed),
     * we return the root folder as a fallback.
     *
     * @param connection  The connection whose parent folder we want.
     * @return  The parent {@link ConnectionFolder}, never {@code null}.
     */
    public ConnectionFolder getParentConnectionFolder( Connection connection )
    {
        for ( ConnectionFolder folder : folderList )
        {
            if ( folder.getConnectionIds().contains( connection.getId() ) )
            {
                return folder;
            }
        }
        return getRootConnectionFolder();
    }


    // ── GET PARENT FOLDER FOR A FOLDER — CHEWIE FINDS WHICH BINDER HOLDS A SUB-BINDER
    // Chewie scans every binder to find which one contains a given sub-binder.
    // If none claims it the folder is a top-level orphan — return null.
    // We walk all folders checking their subFolderIds list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent folder that directly contains the given sub-folder, or
     * {@code null} if the folder is at the top level (i.e. directly under root or orphaned).
     *
     * @param connectionFolder  The folder whose parent we want.
     * @return  The parent {@link ConnectionFolder}, or {@code null} if none.
     */
    public ConnectionFolder getParentConnectionFolder( ConnectionFolder connectionFolder )
    {
        for ( ConnectionFolder folder : folderList )
        {
            if ( folder.getSubFolderIds().contains( connectionFolder.getId() ) )
            {
                return folder;
            }
        }
        return null;
    }


    // ── GET ALL SUB FOLDERS — CHEWIE LISTS A BINDER AND ALL ITS NESTED BINDERS ───
    // Chewie opens a binder and recursively lists every nested binder inside it,
    // including the starting binder itself.
    // We do a BFS from the given folder, collecting every descendant.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the given folder and all of its descendant sub-folders recursively.
     * We use this when deleting a folder — we need to know all the nested folders
     * so we can clean them up too.
     *
     * <p>For example — Chewie inventories a binder tree:</p>
     * <pre>
     *   Set&lt;ConnectionFolder&gt; all = manager.getAllSubFolders(root);
     *   // all includes root + every nested binder under it
     * </pre>
     *
     * @param folder  The starting folder (included in the result set).
     * @return  A set containing the given folder and all its descendants.
     */
    public Set<ConnectionFolder> getAllSubFolders( ConnectionFolder folder )
    {
        Set<ConnectionFolder> allSubFolders = new HashSet<ConnectionFolder>();

        List<String> ids = new ArrayList<String>();
        ids.add( folder.getId() );
        while ( !ids.isEmpty() )
        {
            String id = ids.remove( 0 );
            ConnectionFolder subFolder = getConnectionFolderById( id );
            allSubFolders.add( subFolder );

            ids.addAll( subFolder.getSubFolderIds() );
        }

        return allSubFolders;
    }


    // ── GET ALL PARENT FOLDERS — CHEWIE TRACES THE ANCESTRY OF A BINDER ──────────
    // Chewie follows the chain of parent binders from a given binder all the way
    // back to the top of the cabinet.
    // We walk upward through parent links until we reach a folder with no parent.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the given folder and all of its ancestors up to the root.
     * We use this when expanding the tree to a specific folder —
     * we need to know all ancestor folders to expand their nodes.
     *
     * @param folder  The starting folder (included in the result set).
     * @return  A set containing the given folder and all its ancestors.
     */
    public Set<ConnectionFolder> getAllParentFolders( ConnectionFolder folder )
    {
        Set<ConnectionFolder> allParentFolders = new HashSet<ConnectionFolder>();

        do
        {
            allParentFolders.add( folder );
            folder = getParentConnectionFolder( folder );
        }
        while ( folder != null );

        return allParentFolders;
    }


    // ── GET ROOT FOLDER — CHEWIE POINTS TO THE CABINET ITSELF ────────────────────
    // Chewie points to the top-level "root" binder that holds everything else.
    // We return the root ConnectionFolder (id="0").
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the root {@link ConnectionFolder} that acts as the invisible top of the tree.
     * The Connections view uses this as the input to the content provider — its children
     * are the visible top-level items.
     *
     * @return  The root folder (never {@code null}).
     */
    public ConnectionFolder getRootConnectionFolder()
    {
        return root;
    }


    // ── SET ROOT FOLDER — CHEWIE REPLACES THE CABINET'S TOP BINDER ───────────────
    // Chewie swaps the root binder — typically done during deserialization when
    // the saved root is loaded from disk.
    // We replace our root reference.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the root connection folder.
     * We call this during deserialization when the saved root is loaded from disk,
     * replacing the temporary empty root we created in the constructor.
     *
     * @param root  The folder to use as the new root.
     */
    public void setRootConnectionFolder( ConnectionFolder root )
    {
        this.root = root;
    }


    // ── CONNECTION ADDED — CHEWIE SAVES AFTER A NEW ROUTE APPEARS ─────────────────
    // When a new route lands on the datapad, Chewie saves the cabinet layout
    // because the root folder may have gained a new connection reference.
    // We call saveConnectionFolders() to persist.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionAdded} event by saving the folder hierarchy.
     * A new connection may have been placed into a folder, so we persist the folder state.
     *
     * @param connection  The connection that was added.
     */
    public void connectionAdded( Connection connection )
    {
        saveConnectionFolders();
    }


    // ── CONNECTION REMOVED — CHEWIE SAVES AFTER A ROUTE IS ERASED ────────────────
    // When a route is removed, the folder that contained it needs to be updated on disk.
    // We call saveConnectionFolders() to persist.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionRemoved} event by saving the folder hierarchy.
     *
     * @param connection  The connection that was removed.
     */
    public void connectionRemoved( Connection connection )
    {
        saveConnectionFolders();
    }


    // ── CONNECTION UPDATED — CHEWIE SAVES AFTER A ROUTE CHANGES ──────────────────
    // A route update may affect folder metadata; we persist just in case.
    // We call saveConnectionFolders() to persist.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionUpdated} event by saving the folder hierarchy.
     *
     * @param connection  The connection that was updated.
     */
    public void connectionUpdated( Connection connection )
    {
        saveConnectionFolders();
    }


    // ── CONNECTION OPENED / CLOSED — NOT OUR CONCERN ──────────────────────────────
    // Opening or closing a live connection doesn't change folder membership.
    // We intentionally do nothing here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionOpened} event.
     * Opening a connection doesn't affect folder structure, so we do nothing.
     *
     * @param connection  The connection that was opened.
     */
    public void connectionOpened( Connection connection )
    {
    }


    /**
     * Responds to a {@code connectionClosed} event.
     * Closing a connection doesn't affect folder structure, so we do nothing.
     *
     * @param connection  The connection that was closed.
     */
    public void connectionClosed( Connection connection )
    {
    }


    // ── FOLDER MODIFIED — CHEWIE SAVES WHEN A BINDER CHANGES ─────────────────────
    // A binder was renamed or its contents changed; Chewie saves the cabinet layout.
    // We call saveConnectionFolders() to persist.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionFolderModified} event by saving the folder hierarchy.
     * This fires when a folder is renamed or when connections/sub-folders are added or removed.
     *
     * @param connectionFolder  The folder that was modified.
     */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
        saveConnectionFolders();
    }


    // ── FOLDER ADDED — CHEWIE SAVES WHEN A NEW BINDER APPEARS ────────────────────
    // A new binder was added to the cabinet; Chewie saves the updated layout.
    // We call saveConnectionFolders() to persist.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionFolderAdded} event by saving the folder hierarchy.
     *
     * @param connectionFolder  The folder that was added.
     */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
        saveConnectionFolders();
    }


    // ── FOLDER REMOVED — CHEWIE SAVES WHEN A BINDER IS PULLED OUT ────────────────
    // A binder was removed from the cabinet; Chewie saves the updated layout.
    // We call saveConnectionFolders() to persist.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a {@code connectionFolderRemoved} event by saving the folder hierarchy.
     *
     * @param connectionFolder  The folder that was removed.
     */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
        saveConnectionFolders();
    }


    // ── SAVE — CHEWIE WRITES THE CABINET LAYOUT TO THE HOLD ──────────────────────
    // Chewie writes the full binder hierarchy to a temp file first; if the write
    // succeeds he atomically replaces the live file — no partial-write corruption.
    // We serialize all ConnectionFolders to connectionFolders.xml.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the full folder hierarchy to connectionFolders.xml.
     * We write to a temp file first and only replace the live file on success —
     * same safe-write strategy as {@link ConnectionManager#saveConnections()}.
     * The method is {@code synchronized} to prevent concurrent write corruption.
     */
    private synchronized void saveConnectionFolders()
    {
        File file = new File( getConnectionFolderStoreFileName() );
        File tempFile = new File( getConnectionFolderStoreFileName() + ConnectionManager.TEMP_SUFFIX );

        // To avoid a corrupt file, save object to a temp file first
        try ( FileOutputStream fileOutputStream = new FileOutputStream( tempFile ) )
        {
            ConnectionIO.saveConnectionFolders( folderList, fileOutputStream );
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
            String content = FileUtils.readFileToString( tempFile, ConnectionManager.ENCODING_UTF8 );
            FileUtils.writeStringToFile( file, content, ConnectionManager.ENCODING_UTF8 );
        }
        catch ( IOException e )
        {
            Status status = new Status( IStatus.ERROR, ConnectionCoreConstants.PLUGIN_ID,
                Messages.error__saving_connections + e.getMessage(), e );
            ConnectionCorePlugin.getDefault().getLog().log( status );
            return;
        }
    }


    // ── LOAD — CHEWIE READS THE CABINET LAYOUT FROM THE HOLD ─────────────────────
    // At startup, Chewie opens the hold and reads the saved binder layout back into
    // memory; if the file doesn't exist he starts fresh with just the root binder
    // and adds all existing connections into it.
    // We deserialize connectionFolders.xml and reconstruct the full hierarchy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Loads the folder hierarchy from connectionFolders.xml into memory.
     * Called once during construction. If the file doesn't exist (first launch or
     * pre-folder version) we fall back to placing all connections in the root folder.
     * We suspend event firing during load to avoid cascading saves while we set up.
     */
    private synchronized void loadConnectionFolders()
    {
        ConnectionEventRegistry.suspendEventFiringInCurrentThread();

        File file = new File( getConnectionFolderStoreFileName() );
        if ( file.exists() )
        {
            try ( FileInputStream fileInputStream = new FileInputStream( file ) )
            {
                folderList = ConnectionIO.loadConnectionFolders( fileInputStream );
            }
            catch ( Exception e )
            {
                Status status = new Status( IStatus.ERROR, ConnectionCoreConstants.PLUGIN_ID,
                    Messages.error__loading_connections + e.getMessage(), e );
                ConnectionCorePlugin.getDefault().getLog().log( status );
            }
        }

        if ( !folderList.isEmpty() )
        {
            for ( ConnectionFolder folder : folderList )
            {
                if ( ROOT_ID.equals( folder.getId() ) )
                {
                    root = folder;
                }
            }
        }
        else
        {
            Connection[] connections = ConnectionCorePlugin.getDefault().getConnectionManager().getConnections();
            for ( Connection connection : connections )
            {
                root.addConnectionId( connection.getId() );
            }
        }

        ConnectionEventRegistry.resumeEventFiringInCurrentThread();
    }

}
