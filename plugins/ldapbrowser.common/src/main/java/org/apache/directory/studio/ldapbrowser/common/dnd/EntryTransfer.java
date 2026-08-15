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

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserConnectionManager;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;


// ── CLASS: EntryTransfer — CHEWIE MOVING CARGO IN THE FALCON'S HOLD ──────────
// Chewie is loading crates onto the Millennium Falcon before a quick escape
// from Mos Eisley. He doesn't carry the entire contents of each crate across
// the docking bay — that would take forever. Instead, he stencils the crate ID
// and bay code on a slip of flimsiplast and hands that to the loader. At the
// destination, the loader looks up the full crate from the warehouse index.
// EntryTransfer does the same: instead of serializing whole IEntry objects
// (which live in memory with backreferences to everything), we only serialize
// the connection ID and the entry's DN, then reconstruct the live object on drop.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT {@link Transfer} type for dragging and dropping {@link IEntry} objects
 * between views in the LDAP browser. Because IEntry objects are heavyweight
 * in-memory structures, we only transfer the minimal identity information —
 * the connection ID and the entry's DN — and reconstruct the live object on
 * the receiving end by looking it up in the connection's cache.
 * Think of this class as Chewie's cargo slip: just enough info to find the
 * real crate in the hold.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryTransfer extends ByteArrayTransfer
{

    /** The Constant TYPENAME. */
    private static final String TYPENAME = BrowserCommonConstants.DND_ENTRY_TRANSFER;

    /** The Constant TYPEID. */
    private static final int TYPEID = registerType( TYPENAME );

    /** The instance. */
    private static EntryTransfer instance = new EntryTransfer();


    // ── CHEWIE GRABS THE MANIFEST CLIPBOARD ───────────────────────────────────
    // The Falcon keeps one manifest clipboard on the wall — there's only ever
    // one, and every loader grabs that same board. This static accessor returns
    // the single shared EntryTransfer instance that SWT uses as the type token.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton instance of this transfer type. SWT requires a
     * single shared instance per transfer type — passing different instances
     * would register different type IDs and break the drag-and-drop protocol.
     *
     * <p>For example — every loader grabbing the same manifest clipboard from
     * the Falcon's wall:</p>
     * <pre>
     *   Transfer[] transfers = new Transfer[]{ EntryTransfer.getInstance() };
     *   dragSource.setTransfer( transfers );
     * </pre>
     *
     * @return the shared {@link EntryTransfer} instance; never null.
     */
    public static EntryTransfer getInstance()
    {
        return instance;
    }


    // ── CHEWIE STENCILS THE FIRST CRATE SLIP ──────────────────────────────────
    // When Chewie first moves a crate, nobody else has the manifest yet, so
    // he makes one. This private constructor prevents anyone else from creating
    // additional instances — there should only ever be the one on the wall.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — use {@link #getInstance()} to get the singleton.
     * SWT transfer types must be singletons; direct construction is blocked to
     * enforce that invariant.
     */
    private EntryTransfer()
    {
    }


    // ── CHEWIE WRITES THE CARGO SLIP — JAVA TO PLATFORM BYTES ────────────────
    // Chewie takes each crate (IEntry), reads its bay code (connection ID) and
    // crate number (DN), writes those two identifiers onto a length-prefixed
    // flimsiplast slip, and hands the byte stream to the docking computer.
    // He does NOT pack the crate's entire contents — just the ID information
    // needed to find it again on the other side.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Converts an array of {@link IEntry} objects into a platform-native byte
     * array for transfer. Only the connection ID and each entry's DN string are
     * serialized — in that order, each prefixed with a 4-byte length int.
     * The full IEntry objects are NOT transferred; they are looked up from the
     * cache on the receiving end.
     * Silently ignores any input that is not an {@code IEntry[]} or that uses
     * an unsupported transfer type.
     *
     * <p>For example — Chewie writing each crate's bay-code and ID on the slip:</p>
     * <pre>
     *   // For each entry:  [4 bytes: connectionId length][connectionId bytes]
     *   //                  [4 bytes: dn length][dn bytes]
     * </pre>
     *
     * @param object        the object to transfer — must be an {@code IEntry[]};
     *                      any other type is silently rejected.
     * @param transferData  the platform-specific transfer slot to write bytes into.
     */
    @Override
    public void javaToNative( Object object, TransferData transferData )
    {
        if ( !( object instanceof IEntry[] ) )
        {
            return;
        }

        if ( isSupportedType( transferData ) )
        {
            IEntry[] entries = ( IEntry[] ) object;
            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream writeOut = new DataOutputStream( out );

                for ( int i = 0; i < entries.length; i++ )
                {
                    byte[] connectionId = entries[i].getBrowserConnection().getConnection().getId().getBytes( "UTF-8" ); //$NON-NLS-1$
                    writeOut.writeInt( connectionId.length );
                    writeOut.write( connectionId );
                    byte[] dn = entries[i].getDn().getName().getBytes( "UTF-8" ); //$NON-NLS-1$
                    writeOut.writeInt( dn.length );
                    writeOut.write( dn );
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


    // ── CHEWIE READS THE CARGO SLIP — PLATFORM BYTES BACK TO JAVA ────────────
    // On the other side of the docking bay, Chewie reads the slip: first the
    // bay code (connection ID) to find the right warehouse, then the crate
    // number (DN) to look up the actual crate. If the warehouse doesn't know
    // the crate, it's dropped from the list. He repeats for every entry in
    // the byte stream until there's nothing left to read.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Reconstructs an array of {@link IEntry} objects from the platform-native
     * byte array. Reads each (connectionId, dn) pair, looks up the connection
     * via {@link BrowserConnectionManager#getBrowserConnectionById}, then looks
     * up the entry via {@link IBrowserConnection#getEntryFromCache}.
     * Entries not found in cache (e.g., the connection is gone) are simply omitted.
     * Returns null if the transfer data is invalid, unsupported, or all lookups fail.
     *
     * <p>For example — Chewie decoding the slip and fetching the real crates:</p>
     * <pre>
     *   IEntry[] entries = (IEntry[]) EntryTransfer.getInstance().nativeToJava( data );
     *   // entries are the live in-memory objects from the connection cache
     * </pre>
     *
     * @param transferData  the platform-specific transfer slot containing the
     *                      previously serialized bytes.
     * @return an {@code IEntry[]} of resolved live objects, or {@code null} if
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

                List<IEntry> entryList = new ArrayList<IEntry>();
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

                        IEntry entry = null;
                        if ( readIn.available() > 1 && connection != null )
                        {
                            int size = readIn.readInt();
                            byte[] dn = new byte[size];
                            readIn.read( dn );
                            entry = connection.getEntryFromCache( new Dn( new String( dn, "UTF-8" ) ) ); //$NON-NLS-1$
                        }
                        else
                        {
                            return null;
                        }

                        if ( entry != null )
                        {
                            entryList.add( entry );
                        }
                    }
                    while ( readIn.available() > 1 );

                    readIn.close();
                }
                catch ( IOException ex )
                {
                    return null;
                }

                return entryList.isEmpty() ? null : entryList.toArray( new IEntry[0] );
            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return null;

    }


    // ── CHEWIE READS THE MANIFEST TYPE TAG ────────────────────────────────────
    // The docking computer needs to know which type of manifest this is —
    // entries, searches, or values. The type name is the human-readable label
    // that SWT uses to match drag sources to drop targets.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the registered type name for this transfer, used by SWT to
     * match drag sources and drop targets. The name is the
     * {@link BrowserCommonConstants#DND_ENTRY_TRANSFER} constant.
     *
     * @return a one-element array containing the transfer type name string.
     */
    @Override
    protected String[] getTypeNames()
    {
        return new String[]
            { TYPENAME };
    }


    // ── CHEWIE READS THE MANIFEST TYPE CODE ───────────────────────────────────
    // Alongside the human-readable label, SWT also works with a numeric type ID
    // registered at class-load time. Both must match for a drag-and-drop
    // handshake to succeed.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the registered numeric type ID for this transfer, used by SWT
     * internally to match drag sources and drop targets at the OS level.
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
