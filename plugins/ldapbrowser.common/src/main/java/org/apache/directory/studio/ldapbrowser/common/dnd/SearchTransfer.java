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

package org.apache.directory.studio.ldapbrowser.common.dnd;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserConnectionManager;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;


// ── CLASS: SearchTransfer — CHEWIE MOVING NAV COMPUTER CARDS IN THE HOLD ─────
// Chewie is shifting a stack of navigation mission cards from one locker to
// another in the Falcon's cargo hold. Each card isn't the full mission briefing
// — it's just the connection bay code and the mission name written on a slip of
// flimsiplast. At the destination, the nav computer looks up the full mission
// record by name from the correct connection's search manager.
// SearchTransfer works the same way: when the user drags a saved search from
// one view to another, we only transfer the connection ID and the search name,
// then look up the live ISearch object from the connection's search manager on drop.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT {@link Transfer} type for dragging and dropping {@link ISearch} objects
 * between views in the LDAP browser. Only the connection ID and the search name
 * are serialized — the live {@link ISearch} object is reconstructed on drop by
 * looking it up in the connection's search manager.
 * Think of this class as Chewie's nav-card slip: just the bay code and mission
 * name, not the whole briefing package.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchTransfer extends ByteArrayTransfer
{

    /** The Constant TYPENAME. */
    private static final String TYPENAME = BrowserCommonConstants.DND_SEARCH_TRANSFER;

    /** The Constant TYPEID. */
    private static final int TYPEID = registerType( TYPENAME );

    /** The instance. */
    private static SearchTransfer instance = new SearchTransfer();


    // ── CHEWIE STENCILS THE FIRST NAV-CARD SLIP ───────────────────────────────
    // When the Falcon needs a transfer manifest for search objects, Chewie
    // makes one. The private constructor prevents anyone else from creating
    // extra instances — there's only room for one manifest on the board.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — use {@link #getInstance()} to get the singleton.
     * SWT transfer types must be singletons; direct construction is blocked.
     */
    private SearchTransfer()
    {
    }


    // ── CHEWIE GRABS THE NAV-CARD MANIFEST BOARD ──────────────────────────────
    // Every loader in the hold reaches for the same board on the wall. This
    // static accessor returns the one shared SearchTransfer instance that SWT
    // uses as the type token for search drag-and-drop operations.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton instance of this transfer type. SWT requires one
     * shared instance per type so the type ID stays consistent across drag
     * source and drop target registrations.
     *
     * <p>For example — every view registering the same search transfer type:</p>
     * <pre>
     *   Transfer[] transfers = new Transfer[]{ SearchTransfer.getInstance() };
     *   dragSource.setTransfer( transfers );
     * </pre>
     *
     * @return the shared {@link SearchTransfer} instance; never null.
     */
    public static SearchTransfer getInstance()
    {
        return instance;
    }


    // ── CHEWIE WRITES THE SEARCH SLIP — JAVA TO PLATFORM BYTES ───────────────
    // Chewie takes each nav-card (ISearch), reads the bay code (connection ID)
    // and mission name (search name), writes them as two length-prefixed UTF-8
    // strings into the byte stream, then hands it to the docking computer.
    // He skips any input that isn't an ISearch[] or uses an unsupported type.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Converts an array of {@link ISearch} objects into a platform-native byte
     * array for transfer. Only the connection ID and each search's name are
     * serialized — in that order, each prefixed with a 4-byte length int.
     * Silently ignores any input that is not an {@code ISearch[]} or that
     * targets an unsupported transfer type.
     *
     * <p>For example — Chewie writing each mission's bay-code and name on the slip:</p>
     * <pre>
     *   // For each search:  [4 bytes: connectionId length][connectionId bytes]
     *   //                   [4 bytes: searchName length][searchName bytes]
     * </pre>
     *
     * @param object        the object to transfer — must be an {@code ISearch[]};
     *                      anything else is silently rejected.
     * @param transferData  the platform-specific transfer slot to write bytes into.
     */
    @Override
    public void javaToNative( Object object, TransferData transferData )
    {
        if ( !( object instanceof ISearch[] ) )
        {
            return;
        }

        if ( isSupportedType( transferData ) )
        {
            ISearch[] searches = ( ISearch[] ) object;
            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream writeOut = new DataOutputStream( out );

                for ( ISearch search : searches )
                {
                    byte[] connectionId = search.getBrowserConnection().getConnection().getId().getBytes( "UTF-8" ); //$NON-NLS-1$
                    writeOut.writeInt( connectionId.length );
                    writeOut.write( connectionId );
                    byte[] searchName = search.getName().getBytes( "UTF-8" ); //$NON-NLS-1$
                    writeOut.writeInt( searchName.length );
                    writeOut.write( searchName );
                }

                byte[] buffer = out.toByteArray();
                writeOut.close();

                super.javaToNative( buffer, transferData );

            }
            catch ( IOException e )
            {
            }
        }
    }


    // ── CHEWIE READS THE SLIP AND FETCHES THE REAL MISSION ────────────────────
    // On the receiving end, Chewie reads the slip: the bay code tells him which
    // connection to ask, and the mission name tells the connection's nav computer
    // which ISearch record to retrieve. If the connection is gone or the search
    // isn't there anymore, that entry is simply skipped.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Reconstructs an array of {@link ISearch} objects from the platform-native
     * byte array. Reads each (connectionId, searchName) pair, looks up the
     * connection via {@link BrowserConnectionManager#getBrowserConnectionById},
     * then retrieves the search via
     * {@link IBrowserConnection#getSearchManager()}.
     * Searches not found are omitted. Returns null if the data is invalid or
     * all lookups fail.
     *
     * <p>For example — Chewie decoding the slip and fetching the real mission record:</p>
     * <pre>
     *   ISearch[] searches = (ISearch[]) SearchTransfer.getInstance().nativeToJava( data );
     * </pre>
     *
     * @param transferData  the platform-specific transfer slot containing the
     *                      previously serialized bytes.
     * @return an {@code ISearch[]} of resolved live objects, or {@code null} if
     *         nothing could be reconstructed.
     */
    @Override
    public Object nativeToJava( TransferData transferData )
    {
        try
        {
            if ( isSupportedType( transferData ) )
            {
                byte[] buffer = ( byte[] ) super.nativeToJava( transferData );
                if ( buffer == null )
                {
                    return null;
                }

                List<ISearch> searchList = new ArrayList<ISearch>();
                try
                {
                    IBrowserConnection connection = null;
                    ByteArrayInputStream in = new ByteArrayInputStream( buffer );
                    DataInputStream readIn = new DataInputStream( in );

                    do
                    {
                        if ( readIn.available() > 1 )
                        {
                            int size = readIn.readInt();
                            byte[] connectionId = new byte[size];
                            readIn.read( connectionId );
                            connection = BrowserCorePlugin.getDefault().getConnectionManager()
                                .getBrowserConnectionById( new String( connectionId, "UTF-8" ) ); //$NON-NLS-1$
                        }

                        ISearch search = null;
                        if ( readIn.available() > 1 && connection != null )
                        {
                            int size = readIn.readInt();
                            byte[] searchName = new byte[size];
                            readIn.read( searchName );
                            search = connection.getSearchManager().getSearch( new String( searchName, "UTF-8" ) ); //$NON-NLS-1$
                        }
                        else
                        {
                            return null;
                        }

                        if ( search != null )
                        {
                            searchList.add( search );
                        }
                    }
                    while ( readIn.available() > 1 );

                    readIn.close();
                }
                catch ( IOException ex )
                {
                    return null;
                }

                return searchList.isEmpty() ? null : searchList.toArray( new ISearch[0] );
            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return null;

    }


    // ── CHEWIE READS THE NAV-CARD MANIFEST TYPE TAG ───────────────────────────
    // The docking computer reads the type tag on the manifest to know this is
    // a search manifest, not an entry or value manifest. SWT uses this string
    // to match drag sources with compatible drop targets.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the registered type name for this transfer. SWT uses this string
     * to match drag sources and drop targets for search objects.
     *
     * @return a one-element array containing the transfer type name string.
     */
    @Override
    protected String[] getTypeNames()
    {
        return new String[]
            { TYPENAME };
    }


    // ── CHEWIE READS THE NAV-CARD MANIFEST TYPE CODE ──────────────────────────
    // The numeric type ID is the OS-level handle SWT uses alongside the string
    // name to confirm that source and target agree on the data format.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the registered numeric type ID for this transfer. Used by SWT
     * at the OS level to match drag sources and drop targets.
     *
     * @return a one-element array containing the integer type ID.
     */
    @Override
    protected int[] getTypeIds()
    {
        return new int[]
            { TYPEID };
    }

}
