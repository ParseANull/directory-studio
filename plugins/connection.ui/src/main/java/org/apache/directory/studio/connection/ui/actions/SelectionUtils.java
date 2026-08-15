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

package org.apache.directory.studio.connection.ui.actions;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;


// ── CLASS: SelectionUtils — THE REBEL BRIEFING ROOM ROSTER READER ─────────────────
// When Mon Mothma needs to know which pilots are in the briefing room, she looks
// at the attendance list and filters by rank.  SelectionUtils does the same
// for Eclipse selections: given an ISelection, it extracts specific object types
// (Connection, ConnectionFolder, String, Object) from the structured selection list.
// Every StudioAction calls into these helpers to figure out what the user has
// currently selected in the Connections view before deciding whether to enable
// itself and what to do when run.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility methods for extracting typed objects from an Eclipse
 * {@link ISelection}.
 *
 * <p>All extraction methods work by scanning the elements of an
 * {@link IStructuredSelection} and collecting those that are instances
 * of the requested type.  An empty array is returned if there are no matches
 * or if the selection is not structured.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SelectionUtils
{
    // ── GET PROPERTIES — EXTRACT STRINGS ──────────────────────────────────────────
    /**
     * Extracts all {@link String} elements from the given selection.
     *
     * @param selection  The current JFace selection.
     * @return  An array of Strings, possibly empty.
     */
    public static String[] getProperties( ISelection selection )
    {
        List<Object> list = getTypes( selection, String.class );

        return list.toArray( new String[list.size()] );
    }


    // ── GET TYPES — GENERIC TYPED EXTRACTOR ───────────────────────────────────────
    // The workhorse: iterates the structured selection and keeps each element
    // that passes type.isInstance().
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all elements from the given selection that are instances of
     * the given type.
     *
     * @param selection  The current JFace selection.
     * @param type       The type to filter by.
     * @return  A list of matching elements; never {@code null}.
     */
    private static List<Object> getTypes( ISelection selection, Class<?> type )
    {
        List<Object> list = new ArrayList<>();

        if ( selection instanceof IStructuredSelection )
        {
            IStructuredSelection structuredSelection = ( IStructuredSelection ) selection;

            for ( Object object : structuredSelection.toList() )
            {
                if ( type.isInstance( object ) )
                {
                    list.add( object );
                }
            }
        }

        return list;
    }


    // ── GET CONNECTIONS — EXTRACT CONNECTION OBJECTS ───────────────────────────────
    /**
     * Extracts all {@link Connection} elements from the given selection.
     *
     * @param selection  The current JFace selection.
     * @return  An array of {@link Connection}s, possibly empty.
     */
    public static Connection[] getConnections( ISelection selection )
    {
        List<Object> list = getTypes( selection, Connection.class );

        return list.toArray( new Connection[list.size()] );
    }


    // ── GET CONNECTION FOLDERS — EXTRACT FOLDER OBJECTS ───────────────────────────
    /**
     * Extracts all {@link ConnectionFolder} elements from the given selection.
     *
     * @param selection  The current JFace selection.
     * @return  An array of {@link ConnectionFolder}s, possibly empty.
     */
    public static ConnectionFolder[] getConnectionFolders( ISelection selection )
    {
        List<Object> list = getTypes( selection, ConnectionFolder.class );

        return list.toArray( new ConnectionFolder[list.size()] );
    }


    // ── GET OBJECTS — EXTRACT ALL OBJECTS ─────────────────────────────────────────
    /**
     * Extracts all objects from the given selection.
     *
     * @param selection  The current JFace selection.
     * @return  An array of all selected objects, possibly empty.
     */
    public static Object[] getObjects( ISelection selection )
    {
        List<Object> list = getTypes( selection, Object.class );

        return list.toArray( new Object[list.size()] );
    }
}
