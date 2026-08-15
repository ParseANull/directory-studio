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

package org.apache.directory.studio.connection.ui.dnd;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionFolderManager;
import org.apache.directory.studio.connection.core.ConnectionManager;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;


// ── CLASS: ConnectionTransfer — THE REBEL MANIFEST TRANSMISSION PROTOCOL ──────────
// When the Rebel Alliance needs to move fleet data from one terminal to another —
// via drag-and-drop — they need a protocol that serialises ship IDs and cargo-bay IDs
// into a byte stream the OS clipboard understands, and then deserialises them back.
// ConnectionTransfer is that protocol.  It is a singleton SWT ByteArrayTransfer that
// serialises Connection and ConnectionFolder objects as their string IDs, then uses
// the ConnectionManager / ConnectionFolderManager to look up the live objects on the
// receiving end.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Transfer} implementation for drag-and-drop of {@link Connection} and
 * {@link ConnectionFolder} objects within Directory Studio.
 *
 * <p>Only the connection/folder ID (a UUID string) is serialised into the platform
 * clipboard buffer — not the full object.  On the receiving end the ID is looked
 * up via {@link ConnectionManager#getConnectionById} or
 * {@link ConnectionFolderManager#getConnectionFolderById}.</p>
 *
 * <p>This class is a singleton; use {@link #getInstance()} to obtain the shared
 * instance.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ConnectionTransfer extends ByteArrayTransfer
{
    // ── CONSTANTS ─────────────────────────────────────────────────────────────────

    /** The clipboard type name — must match {@link ConnectionUIConstants#TYPENAME}. */
    private static final String TYPENAME = ConnectionUIConstants.TYPENAME;

    /** The numeric type ID registered with the OS clipboard subsystem. */
    private static final int TYPEID = registerType( TYPENAME );

    /** The singleton instance. */
    private static ConnectionTransfer instance = new ConnectionTransfer();


    // ── CONSTRUCTOR — PRIVATE SINGLETON ───────────────────────────────────────────
    /**
     * Private constructor — use {@link #getInstance()} to obtain the shared instance.
     */
    private ConnectionTransfer()
    {
        super();
    }


    // ── GET INSTANCE ──────────────────────────────────────────────────────────────
    /**
     * Returns the shared singleton instance of {@link ConnectionTransfer}.
     *
     * @return The singleton {@link ConnectionTransfer}.
     */
    public static ConnectionTransfer getInstance()
    {
        return instance;
    }


    // ── JAVA TO NATIVE ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Serialises an array of {@link Connection} and/or {@link ConnectionFolder}
     * objects into the platform clipboard buffer.  Only the ID string of each
     * object is written — the full object graph is not transferred.</p>
     *
     * <p>The byte layout for each entry is:</p>
     * <pre>
     *   [4 bytes: id length] [N bytes: id UTF-8]
     * </pre>
     *
     * <p>Silently does nothing if {@code object} is not an {@code Object[]} or
     * if the type is not supported.</p>
     */
    @Override
    public void javaToNative( Object object, TransferData transferData )
    {
        if ( !( object instanceof Object[] ) )
        {
            return;
        }

        if ( isSupportedType( transferData ) )
        {
            Object[] objects = ( Object[] ) object;

            try ( ByteArrayOutputStream out = new ByteArrayOutputStream() )
            {
                try ( DataOutputStream writeOut = new DataOutputStream( out ) )
                {
                    // ── SERIALISE EACH OBJECT ─────────────────────────────────────
                    // Write the ID as length-prefixed UTF-8 bytes for each Connection
                    // or ConnectionFolder.  Unknown types are skipped silently.
                    // ──────────────────────────────────────────────────────────────
                    for ( int i = 0; i < objects.length; i++ )
                    {
                        if ( objects[i] instanceof Connection )
                        {
                            byte[] idBytes = ( ( Connection ) objects[i] ).getConnectionParameter().getId().getBytes();
                            writeOut.writeInt( idBytes.length );
                            writeOut.write( idBytes );
                        }
                        else if ( objects[i] instanceof ConnectionFolder )
                        {
                            byte[] idBytes = ( ( ConnectionFolder ) objects[i] ).getId().getBytes();
                            writeOut.writeInt( idBytes.length );
                            writeOut.write( idBytes );
                        }
                    }

                    byte[] buffer = out.toByteArray();
                    super.javaToNative( buffer, transferData );
                }
            }
            catch ( IOException e )
            {
                // If serialisation fails we just silently drop the transfer.
            }
        }
    }


    // ── NATIVE TO JAVA ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Deserialises the platform clipboard buffer back into an array of
     * {@link Connection} and/or {@link ConnectionFolder} live objects.
     * Each ID is looked up in the {@link ConnectionManager} first; if not found
     * there it is looked up in the {@link ConnectionFolderManager}.  IDs that
     * resolve to neither are silently skipped.</p>
     *
     * @return An {@code Object[]} containing the resolved connections/folders,
     *         or {@code null} if the type is not supported or the buffer is corrupt.
     */
    @Override
    public Object nativeToJava( TransferData transferData )
    {
        if ( isSupportedType( transferData ) )
        {
            byte[] buffer = ( byte[] ) super.nativeToJava( transferData );

            if ( buffer == null )
            {
                return null;
            }

            List<Object> objectList = new ArrayList<>();

            try ( ByteArrayInputStream in = new ByteArrayInputStream( buffer ) )
            {
                try ( DataInputStream readIn = new DataInputStream( in ) )
                {
                    // ── DESERIALISE EACH ID ───────────────────────────────────────
                    // Read length-prefixed ID bytes, resolve to Connection or
                    // ConnectionFolder, and add to the result list.
                    // ──────────────────────────────────────────────────────────────
                    do
                    {
                        if ( readIn.available() > 1 )
                        {
                            int size = readIn.readInt();
                            byte[] idBytes = new byte[size];

                            if ( readIn.read( idBytes ) != size )
                            {
                                // Partial read — the buffer is truncated or corrupt.
                                return null;
                            }

                            Connection connection = ConnectionCorePlugin.getDefault().getConnectionManager()
                                .getConnectionById( new String( idBytes ) );

                            if ( connection == null )
                            {
                                ConnectionFolder folder = ConnectionCorePlugin.getDefault().getConnectionFolderManager()
                                    .getConnectionFolderById( new String( idBytes ) );

                                if ( folder != null )
                                {
                                    objectList.add( folder );
                                }
                            }
                            else
                            {
                                objectList.add( connection );
                            }
                        }
                    }
                    while ( readIn.available() > 1 );
                }
            }
            catch ( IOException ex )
            {
                return null;
            }

            return objectList.toArray();
        }

        return null;
    }


    // ── GET TYPE NAMES ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the single type name registered for this transfer.
     */
    protected String[] getTypeNames()
    {
        return new String[]
            { TYPENAME };
    }


    // ── GET TYPE IDS ──────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the single type ID registered for this transfer.
     */
    protected int[] getTypeIds()
    {
        return new int[]
            { TYPEID };
    }
}
