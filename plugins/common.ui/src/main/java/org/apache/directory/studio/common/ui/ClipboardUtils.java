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
package org.apache.directory.studio.common.ui;


import java.util.function.Function;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

// ── CLASS: ClipboardUtils — R2-D2 RETRIEVING HOLOGRAPHIC DATA ───────────────
// Like R2-D2 pulling a holographic message from the Rebellion's shared memory
// banks, this utility class handles all clipboard interactions for us.  We open
// the memory slot, read out whatever data is stored there, and always clean up
// the droid's memory port when we are done — no leaked resources on our watch.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We collect all clipboard helper methods here so the rest of the codebase
 * does not have to worry about opening, reading, and closing the system
 * clipboard manually.  Every method properly disposes the clipboard handle
 * when it is finished, even if something goes wrong along the way.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ClipboardUtils
{

    // ── METHOD getFromClipboard — READING THE HOLOGRAPHIC MESSAGE ────────────
    // R2-D2 pops open the data port and reads whatever holographic message is
    // currently stored there, returning it as a raw Object.  If the requested
    // data type is not available, we hand back null — no beeping in distress.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We retrieve the clipboard contents for the given transfer type and
     * return them as a plain {@code Object}.  If nothing of that type is on
     * the clipboard, we return {@code null}.
     *
     * @param transfer the transfer agent describing the data type we want
     * @return the clipboard data, or {@code null} if unavailable
     */
    public static Object getFromClipboard( Transfer transfer )
    {
        return getFromClipboard( transfer, Object.class );
    }


    // ── METHOD getFromClipboard (typed) — DECODING THE HOLOGRAM ─────────────
    // R2-D2 reads the hologram and then runs it through the type decoder — if
    // the message is the right format, we hand back the strongly-typed result.
    // If the format does not match, we return null rather than blowing a fuse.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We retrieve the clipboard contents for the given transfer type and
     * cast them to the requested {@code type}.  If nothing of that type is
     * available, or the contents cannot be cast, we return {@code null}.
     *
     * @param <T>      the expected return type
     * @param transfer the transfer agent describing the data format
     * @param type     the class we expect the clipboard data to be
     * @return the typed clipboard data, or {@code null} if unavailable or wrong type
     */
    public static <T> T getFromClipboard( Transfer transfer, Class<T> type )
    {
        return withClipboard( clipboard -> {
            if ( isAvailable( transfer, clipboard ) )
            {
                Object contents = clipboard.getContents( transfer );
                if ( contents != null && type.isAssignableFrom( contents.getClass() ) )
                {
                    return type.cast( contents );
                }
            }
            return null;
        } );
    }


    // ── METHOD isAvailable — PINGING THE MEMORY PORT ────────────────────────
    // R2-D2 sends a quick probe to the memory port to see whether the requested
    // data format is actually present before we commit to a full read.  Returns
    // true if the clipboard holds data in that transfer format right now.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We check whether the system clipboard currently holds data in the format
     * described by the given transfer agent.  This is a cheap probe — use it
     * before attempting a full read to avoid unnecessary work.
     *
     * @param transfer the transfer agent describing the format to check
     * @return {@code true} if data of that type is on the clipboard
     */
    public static boolean isAvailable( Transfer transfer )
    {
        return withClipboard( clipboard -> {
            return isAvailable( transfer, clipboard );
        } );
    }


    // ── METHOD isAvailable (internal) — SCANNING THE DROID'S DATA BANKS ─────
    // With the clipboard already open, R2-D2 scans through all available data
    // formats and checks whether the requested one is among them.  This is the
    // workhorse that the public method delegates to.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We iterate over every data format currently on the clipboard and return
     * {@code true} as soon as we find one that matches the given transfer type.
     * We use an already-open clipboard handle here to avoid double-disposal.
     *
     * @param transfer  the format we are checking for
     * @param clipboard an already-opened clipboard instance
     * @return {@code true} if the format is available
     */
    private static Boolean isAvailable( Transfer transfer, Clipboard clipboard )
    {
        for ( org.eclipse.swt.dnd.TransferData transferData : clipboard.getAvailableTypes() )
        {
            if ( transfer.isSupportedType( transferData ) )
            {
                return true;
            }
        }
        return false;
    }


    // ── METHOD withClipboard — OPENING AND CLOSING THE DROID'S MEMORY PORT ──
    // R2-D2 opens the memory port, runs the requested function, then closes
    // the port in a finally block — no matter what happens.  This pattern
    // ensures we never leave a dangling clipboard handle behind.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We open a {@link Clipboard}, pass it to the given function, and always
     * dispose it in a finally block.  All our public clipboard methods funnel
     * through here so resource cleanup is handled in exactly one place.
     *
     * @param <T> the return type of the function
     * @param fn  a lambda that receives the open clipboard and returns a result
     * @return whatever the function returns, or {@code null} on failure
     */
    private static <T> T withClipboard( Function<Clipboard, T> fn )
    {
        Clipboard clipboard = null;
        try
        {
            clipboard = new Clipboard( Display.getCurrent() );
            return fn.apply( clipboard );
        }
        finally
        {
            if ( clipboard != null )
            {
                clipboard.dispose();
            }
        }
    }
}
