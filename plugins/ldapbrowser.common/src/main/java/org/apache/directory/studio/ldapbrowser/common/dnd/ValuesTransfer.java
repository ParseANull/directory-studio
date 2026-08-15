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
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;


// ── CLASS: ValuesTransfer — CHEWIE MOVING INDIVIDUAL CARGO ITEMS IN THE HOLD ─
// Chewie is shifting individual items from one crate to another in the Falcon's
// hold — not whole crates this time, but specific contents. For each item, he
// writes four things on the slip: which bay it came from (connection ID), which
// crate (entry DN), which shelf inside the crate (attribute name), and what the
// item actually looks like (the value, flagged as string or binary). At the
// destination, the loader uses all four identifiers to find and verify the
// exact item in the warehouse.
// ValuesTransfer does the same for LDAP attribute values: it serializes the
// minimal identity chain (connection + DN + attribute + value content) and
// reconstructs live IValue objects on drop.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT {@link Transfer} type for dragging and dropping {@link IValue} objects
 * between views in the LDAP browser. Serializes the full identity chain needed
 * to reconstruct the live value: connection ID, entry DN, attribute description,
 * and the raw value bytes (with a string/binary flag). The live IValue is
 * reconstructed on drop by traversing the connection cache.
 * Think of this class as Chewie's four-part item tag: bay, crate, shelf, item.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValuesTransfer extends ByteArrayTransfer
{

    /** The Constant TYPENAME. */
    private static final String TYPENAME = BrowserCommonConstants.DND_VALUES_TRANSFER;

    /** The Constant TYPEID. */
    private static final int TYPEID = registerType( TYPENAME );

    /** The instance. */
    private static ValuesTransfer instance = new ValuesTransfer();


    // ── CHEWIE GRABS THE VALUES MANIFEST BOARD ────────────────────────────────
    // There's one manifest board in the hold for value-level cargo slips. Every
    // view registers the same instance for drop and drag so the type IDs match.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton instance of this transfer type. SWT requires one
     * shared instance per type to keep the registered type ID consistent.
     *
     * <p>For example — registering this transfer for a drag source:</p>
     * <pre>
     *   Transfer[] transfers = new Transfer[]{ ValuesTransfer.getInstance() };
     *   dragSource.setTransfer( transfers );
     * </pre>
     *
     * @return the shared {@link ValuesTransfer} instance; never null.
     */
    public static ValuesTransfer getInstance()
    {
        return instance;
    }


    // ── CHEWIE STENCILS THE FIRST VALUES SLIP ─────────────────────────────────
    // One manifest board, created once. The private constructor blocks any
    // attempt to create duplicates.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — use {@link #getInstance()} to get the singleton.
     */
    private ValuesTransfer()
    {
    }


    // ── CHEWIE WRITES THE FOUR-PART ITEM TAG ──────────────────────────────────
    // For each IValue, Chewie writes four fields onto the slip:
    // 1. Bay code (connection ID) — where does this come from?
    // 2. Crate number (entry DN) — which entry owns the attribute?
    // 3. Shelf label (attribute description) — which attribute?
    // 4. Item content (value bytes, with a boolean flag: string or binary?)
    // Every field is length-prefixed so the reader knows where each field ends.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Converts an array of {@link IValue} objects into a platform-native byte
     * array. For each value, serializes: connection ID, entry DN, attribute
     * description, a string/binary boolean flag, and the raw value bytes —
     * all length-prefixed with 4-byte ints.
     * Silently ignores input that is not an {@code IValue[]}.
     *
     * <p>For example — Chewie writing a four-part tag for each cargo item:</p>
     * <pre>
     *   // [connectionId len][connectionId][dn len][dn]
     *   // [attrName len][attrName][boolean isString][value len][value bytes]
     * </pre>
     *
     * @param object        the object to transfer — must be an {@code IValue[]}.
     * @param transferData  the platform-specific transfer slot to write bytes into.
     */
    @Override
    public void javaToNative( Object object, TransferData transferData )
    {
        if ( !( object instanceof IValue[] ) )
        {
            return;
        }

        if ( isSupportedType( transferData ) )
        {
            IValue[] values = ( IValue[] ) object;
            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream writeOut = new DataOutputStream( out );

                for ( int i = 0; i < values.length; i++ )
                {
                    byte[] connectionId = values[i].getAttribute().getEntry().getBrowserConnection().getConnection()
                        .getId().getBytes( "UTF-8" ); //$NON-NLS-1$
                    writeOut.writeInt( connectionId.length );
                    writeOut.write( connectionId );
                    byte[] dn = values[i].getAttribute().getEntry().getDn().getName().getBytes( "UTF-8" ); //$NON-NLS-1$
                    writeOut.writeInt( dn.length );
                    writeOut.write( dn );
                    byte[] attributeName = values[i].getAttribute().getDescription().getBytes( "UTF-8" ); //$NON-NLS-1$
                    writeOut.writeInt( attributeName.length );
                    writeOut.write( attributeName );
                    if ( values[i].isString() )
                    {
                        byte[] value = values[i].getStringValue().getBytes( "UTF-8" ); //$NON-NLS-1$
                        writeOut.writeBoolean( true );
                        writeOut.writeInt( value.length );
                        writeOut.write( value );
                    }
                    else if ( values[i].isBinary() )
                    {
                        byte[] value = values[i].getBinaryValue();
                        writeOut.writeBoolean( false );
                        writeOut.writeInt( value.length );
                        writeOut.write( value );
                    }
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


    // ── CHEWIE READS THE FOUR-PART TAG AND FETCHES THE REAL ITEM ─────────────
    // At the destination, Chewie reads all four fields from each slip: he
    // finds the right connection, then the right entry in its cache, then the
    // right attribute on that entry, then scans the attribute's values to find
    // the one matching the transferred content. If any step fails (connection
    // gone, entry evicted, attribute missing), that value is simply skipped.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Reconstructs an array of {@link IValue} objects from the platform-native
     * byte array. For each serialized record, looks up the connection, entry,
     * attribute, and matching value in sequence. Values that cannot be found
     * (e.g., because the connection closed or the entry was evicted from cache)
     * are simply omitted from the result.
     * Returns null if the data is invalid or no values could be reconstructed.
     *
     * <p>For example — Chewie reading the tag and retrieving each real cargo item:</p>
     * <pre>
     *   IValue[] values = (IValue[]) ValuesTransfer.getInstance().nativeToJava( data );
     *   // values are the live in-memory objects from the connection cache
     * </pre>
     *
     * @param transferData  the platform-specific transfer slot containing the
     *                      previously serialized bytes.
     * @return an {@code IValue[]} of resolved live objects, or {@code null} if
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

                List<IValue> valueList = new ArrayList<IValue>();
                try
                {
                    ByteArrayInputStream in = new ByteArrayInputStream( buffer );
                    DataInputStream readIn = new DataInputStream( in );

                    do
                    {
                        IBrowserConnection connection = null;
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

                        IAttribute attribute = null;
                        if ( readIn.available() > 1 && entry != null )
                        {
                            int size = readIn.readInt();
                            byte[] attributeName = new byte[size];
                            readIn.read( attributeName );
                            attribute = entry.getAttribute( new String( attributeName, "UTF-8" ) ); //$NON-NLS-1$
                        }
                        else
                        {
                            return null;
                        }

                        IValue value = null;
                        if ( readIn.available() > 1 && attribute != null )
                        {
                            boolean isString = readIn.readBoolean();
                            int size = readIn.readInt();
                            byte[] val = new byte[size];
                            readIn.read( val );
                            String test = new String( val, "UTF-8" ); //$NON-NLS-1$

                            IValue[] values = attribute.getValues();
                            for ( int i = 0; i < values.length; i++ )
                            {
                                if ( isString && values[i].isString() && test.equals( values[i].getStringValue() ) )
                                {
                                    value = values[i];
                                    break;
                                }
                                else if ( !isString && values[i].isBinary()
                                    && test.equals( new String( values[i].getBinaryValue(), "UTF-8" ) ) ) //$NON-NLS-1$
                                {
                                    value = values[i];
                                    break;
                                }
                            }
                        }
                        else
                        {
                            return null;
                        }

                        if ( value != null )
                        {
                            valueList.add( value );
                        }
                    }
                    while ( readIn.available() > 1 );

                    readIn.close();
                }
                catch ( IOException ex )
                {
                    return null;
                }

                return valueList.isEmpty() ? null : valueList.toArray( new IValue[valueList.size()] );
            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return null;

    }


    // ── CHEWIE READS THE VALUES MANIFEST TYPE TAG ─────────────────────────────
    // The type tag identifies this as a values-level manifest, not entries or
    // searches. SWT uses this string when deciding whether a particular drop
    // target can accept data from a given drag source.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the registered type name for this transfer. SWT uses this string
     * to match drag sources and drop targets for value objects.
     *
     * @return a one-element array containing the transfer type name string.
     */
    @Override
    protected String[] getTypeNames()
    {
        return new String[]
            { TYPENAME };
    }


    // ── CHEWIE READS THE VALUES MANIFEST TYPE CODE ────────────────────────────
    // The numeric type ID is the OS-level handle. Both the string and the integer
    // must match between source and target for a successful DnD handshake.
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
