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
import java.util.List;
import java.util.UUID;

import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;


// ── CLASS: ConnectionFolder — CHEWIE ORGANIZES ROUTES INTO LABELED BINDERS ───
// Chewie keeps his hyperspace routes sorted into labeled binders on the
// Falcon's nav station: "Imperial Space", "Outer Rim", "Safe Houses".
// Each binder (folder) holds route IDs and can nest inside other binders.
// This class is that binder: a named container that organises Connection UUIDs
// and sub-folder UUIDs into a logical hierarchy shown in the Connections view.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A named container that organises connections and nested sub-folders in the
 * Connections view tree.
 * We store connection UUIDs and sub-folder UUIDs rather than direct references so
 * the hierarchy can be serialized cleanly to XML and the objects can be looked up
 * lazily via the plugin managers.
 * Think of this class as one of Chewie's labeled route binders — it doesn't fly
 * itself, but it keeps the crew's nav cartridges neatly categorized.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionFolder implements Cloneable
{
    private String id;

    private String name;

    private List<String> subFolderIds;

    private List<String> connectionIds;


    // ── DEFAULT CONSTRUCTOR — CHEWIE GRABS A BLANK BINDER ────────────────────────
    // Chewie reaches into the storage rack and pulls out an empty, unlabeled binder
    // ready to be filled and labeled.
    // We create an empty folder with empty lists — the id and name are set separately.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty {@link ConnectionFolder} with no name and no contents.
     * We use this during deserialization, where the id and name are set via setters.
     */
    public ConnectionFolder()
    {
        this.subFolderIds = new ArrayList<String>();
        this.connectionIds = new ArrayList<String>();
    }


    // ── NAMED CONSTRUCTOR — CHEWIE LABELS AND OPENS A NEW BINDER ─────────────────
    // Chewie writes the route category name on the binder's spine and assigns it
    // a unique registration number before putting it on the shelf.
    // We create a folder with a fresh UUID and the given display name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a named {@link ConnectionFolder} with a fresh unique ID.
     * We use this when the user clicks "New Connection Folder" in the UI.
     *
     * <p>For example — Chewie labels the binder:</p>
     * <pre>
     *   ConnectionFolder folder = new ConnectionFolder("Outer Rim Servers");
     *   // folder.getId() → new UUID, folder.getName() → "Outer Rim Servers"
     * </pre>
     *
     * @param name  The display name for this folder, shown in the Connections view.
     */
    public ConnectionFolder( String name )
    {
        this();
        this.id = createId();
        this.name = name;
    }


    // ── CLONE — CHEWIE DUPLICATES A BINDER AND ALL ITS CONTENTS ──────────────────
    // Chewie photocopies the entire binder — every route entry inside it and every
    // nested sub-binder — creating fresh copies so the original is untouched.
    // We deep-clone the folder: new connections are registered with the connection
    // manager and new sub-folders with the folder manager.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep clone of this folder, including all nested connections and sub-folders.
     * Every connection inside is cloned and re-registered; every sub-folder is recursively
     * cloned the same way.
     * We use this when the user copies a folder — the clone gets fresh UUIDs so it
     * doesn't conflict with the original.
     *
     * @return  A new {@link ConnectionFolder} (as {@link Object}) containing clones of all
     *          this folder's connections and sub-folders.
     */
    public Object clone()
    {
        ConnectionFolder folder = new ConnectionFolder( getName() );

        // clone connections
        ConnectionManager connectionManager = ConnectionCorePlugin.getDefault().getConnectionManager();
        for ( String id : connectionIds )
        {
            Connection connection = connectionManager.getConnectionById( id );
            if ( connection != null )
            {
                Connection newConnection = ( Connection ) connection.clone();
                connectionManager.addConnection( newConnection );
                folder.addConnectionId( newConnection.getId() );
            }
        }

        // clone subfolders
        ConnectionFolderManager connectionFolderManager = ConnectionCorePlugin.getDefault()
            .getConnectionFolderManager();
        for ( String id : subFolderIds )
        {
            ConnectionFolder subFolder = connectionFolderManager.getConnectionFolderById( id );
            if ( subFolder != null )
            {
                ConnectionFolder newFolder = ( ConnectionFolder ) subFolder.clone();
                connectionFolderManager.addConnectionFolder( newFolder );
                folder.addSubFolderId( newFolder.getId() );
            }
        }

        return folder;
    }


    // ── ADD CONNECTION ID — CHEWIE SLOTS A ROUTE CARD INTO THE BINDER ────────────
    // Chewie slides a route card into the binder and announces to the crew
    // that the binder's contents have changed.
    // We append the connection UUID to our list and fire a folder-modified event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a connection's UUID to this folder's membership list.
     * We fire a {@code connectionFolderModified} event so the Connections view
     * tree refreshes and shows the new connection under this folder.
     *
     * @param connectionId  The UUID of the connection to add.
     */
    public void addConnectionId( String connectionId )
    {
        connectionIds.add( connectionId );
        ConnectionEventRegistry.fireConnectonFolderModified( this, this );
    }


    // ── REMOVE CONNECTION ID — CHEWIE PULLS A ROUTE CARD FROM THE BINDER ─────────
    // Chewie pulls the route card out of the binder and tells the crew the
    // binder has changed.
    // We remove the connection UUID from our list and fire a folder-modified event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a connection's UUID from this folder's membership list.
     * We fire a {@code connectionFolderModified} event so the Connections view
     * tree refreshes to reflect the removal.
     *
     * @param connectionId  The UUID of the connection to remove.
     */
    public void removeConnectionId( String connectionId )
    {
        connectionIds.remove( connectionId );
        ConnectionEventRegistry.fireConnectonFolderModified( this, this );
    }


    // ── ADD SUB FOLDER ID — CHEWIE NESTS ONE BINDER INSIDE ANOTHER ───────────────
    // Chewie slides a smaller binder into the main binder and tells the crew
    // that the hierarchy has changed.
    // We append the sub-folder UUID to our list and fire a folder-modified event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a sub-folder's UUID to this folder's sub-folder list.
     * We fire a {@code connectionFolderModified} event so the Connections view
     * tree shows the new nesting level.
     *
     * @param folderId  The UUID of the sub-folder to nest inside this folder.
     */
    public void addSubFolderId( String folderId )
    {
        subFolderIds.add( folderId );
        ConnectionEventRegistry.fireConnectonFolderModified( this, this );
    }


    // ── REMOVE SUB FOLDER ID — CHEWIE PULLS A NESTED BINDER OUT ──────────────────
    // Chewie pulls a nested binder from the main one and tells the crew
    // that the hierarchy has changed.
    // We remove the sub-folder UUID from our list and fire a folder-modified event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a sub-folder's UUID from this folder's sub-folder list.
     * We fire a {@code connectionFolderModified} event so the tree collapses that level.
     *
     * @param folderId  The UUID of the sub-folder to remove.
     */
    public void removeSubFolderId( String folderId )
    {
        subFolderIds.remove( folderId );
        ConnectionEventRegistry.fireConnectonFolderModified( this, this );
    }


    // ── GET ID — READING THE BINDER'S REGISTRATION NUMBER ────────────────────────
    // Chewie reads the registration number on the binder's spine.
    // If somehow the number is missing he stamps a new one on the spot.
    // We return the UUID, generating one lazily if somehow it's null.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this folder's unique ID, generating one lazily if absent.
     * The ID is a UUID that stays stable across renames and session restarts.
     *
     * @return  The UUID string identifying this folder.
     */
    public String getId()
    {
        if ( id == null )
        {
            id = createId();
        }
        return id;
    }


    // ── SET ID — STAMPING THE BINDER'S REGISTRATION NUMBER ───────────────────────
    // Chewie stamps an existing number onto the binder — used when loading from disk.
    // We store the provided id.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets this folder's unique ID.
     * We call this during deserialization to restore the saved UUID rather than
     * generating a new one.
     *
     * @param id  The UUID string to assign.
     */
    public void setId( String id )
    {
        this.id = id;
    }


    // ── GET NAME — READING THE LABEL ON THE BINDER'S SPINE ───────────────────────
    // Chewie reads the name written on the binder's spine.
    // We return the folder's display name.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this folder's display name.
     * This is what appears in the Connections view tree node.
     *
     * @return  The folder's display name.
     */
    public String getName()
    {
        return name;
    }


    // ── SET NAME — CHEWIE RELABELS THE BINDER'S SPINE ────────────────────────────
    // Chewie crosses out the old label and writes a new name on the binder's spine,
    // then announces to the crew that the binder metadata changed.
    // We update the name and fire a folder-modified event so the tree label refreshes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates this folder's display name and fires a {@code connectionFolderModified} event.
     * The event causes the Connections view to refresh the label for this folder node.
     *
     * @param name  The new display name.
     */
    public void setName( String name )
    {
        this.name = name;
        ConnectionEventRegistry.fireConnectonFolderModified( this, this );
    }


    // ── GET SUB FOLDER IDS — CHEWIE LISTS THE NESTED BINDERS ─────────────────────
    // Chewie scans the main binder and lists all nested binders that are still
    // registered with the folder manager — stale references are silently skipped.
    // We return only IDs that correspond to currently registered folders.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the UUIDs of all sub-folders that currently exist in the folder manager.
     * We filter out any stale IDs (referring to deleted folders) rather than returning
     * them and letting callers crash with a null lookup.
     *
     * @return  A fresh list of valid sub-folder UUID strings.
     */
    public List<String> getSubFolderIds()
    {
        List<String> ids = new ArrayList<String>();
        for ( String id : subFolderIds )
        {
            if ( ConnectionCorePlugin.getDefault().getConnectionFolderManager().getConnectionFolderById( id ) != null )
            {
                ids.add( id );
            }
        }
        return ids;
    }


    // ── SET SUB FOLDER IDS — CHEWIE REPLACES THE LIST OF NESTED BINDERS ──────────
    // Chewie empties out the list of nested binders and replaces it wholesale —
    // typically used during deserialization when restoring the whole hierarchy.
    // We replace subFolderIds and fire a folder-modified event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire sub-folder ID list and fires a {@code connectionFolderModified} event.
     * We call this during deserialization to restore the full hierarchy at once.
     *
     * @param subFolderIds  The new list of sub-folder UUID strings.
     */
    public void setSubFolderIds( List<String> subFolderIds )
    {
        this.subFolderIds = subFolderIds;
        ConnectionEventRegistry.fireConnectonFolderModified( this, this );
    }


    // ── GET CONNECTION IDS — CHEWIE LISTS THE ROUTE CARDS IN THE BINDER ──────────
    // Chewie flips through the binder and lists all route cards still registered
    // with the connection manager — stale IDs are silently filtered out.
    // We return only IDs that correspond to currently registered connections.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the UUIDs of all connections currently in this folder that still exist
     * in the connection manager.
     * We filter stale IDs so callers don't have to deal with dead references.
     *
     * @return  A fresh list of valid connection UUID strings.
     */
    public List<String> getConnectionIds()
    {
        List<String> ids = new ArrayList<String>();
        for ( String id : connectionIds )
        {
            if ( ConnectionCorePlugin.getDefault().getConnectionManager().getConnectionById( id ) != null )
            {
                ids.add( id );
            }
        }
        return ids;
    }


    // ── SET CONNECTION IDS — CHEWIE REPLACES THE ROUTE CARDS IN THE BINDER ───────
    // Chewie empties the binder and restacks it with a fresh set of route cards —
    // used during deserialization to restore the folder's membership list.
    // We replace connectionIds and fire a folder-modified event.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire connection ID list and fires a {@code connectionFolderModified} event.
     * We call this during deserialization to restore the full membership list at once.
     *
     * @param connectionIds  The new list of connection UUID strings.
     */
    public void setConnectionIds( List<String> connectionIds )
    {
        this.connectionIds = connectionIds;
        ConnectionEventRegistry.fireConnectonFolderModified( this, this );
    }


    // ── CREATE ID — STAMPING A FRESH REGISTRATION NUMBER ─────────────────────────
    // The shipyard stamps a fresh, guaranteed-unique registration number on every
    // new binder that comes off the line.
    // We generate a UUID for this folder.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Generates a fresh UUID to use as this folder's unique identifier.
     * Called during construction so every new folder gets a distinct ID immediately.
     *
     * @return  A new random UUID string.
     */
    private String createId()
    {
        return UUID.randomUUID().toString();
    }


    // ── HASH CODE — THE BINDER'S NUMERIC HASH ────────────────────────────────────
    // Chewie reads the registration number and computes a hash so the binder
    // can be stored in a hash set efficiently.
    // We derive the hash from our UUID id.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a hash code based on this folder's unique ID.
     *
     * @return  The hash code.
     */
    public int hashCode()
    {
        return getId().hashCode();
    }


    // ── EQUALS — CHEWIE CHECKS IF TWO BINDERS HAVE THE SAME REGISTRATION ─────────
    // Chewie compares the registration numbers on two binders to see if they're
    // the same physical binder.
    // We compare by UUID, not by name or contents.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a {@link ConnectionFolder} with the same UUID.
     *
     * @param obj  The object to compare with.
     * @return  {@code true} if both have the same unique ID.
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ConnectionFolder )
        {
            ConnectionFolder other = ( ConnectionFolder ) obj;
            return getId().equals( other.getId() );
        }
        return false;
    }

}
