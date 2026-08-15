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

package org.apache.directory.studio.valueeditors;


import org.apache.directory.studio.ldapbrowser.common.dialogs.HexDialog;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: HexValueEditor — C-3PO READS R2'S RAW BINARY SIGIL ────────────────
// Aboard the Millennium Falcon, R2-D2 has just retrieved a raw binary data
// capsule from the Imperial archive — a sequence of hexadecimal bytes that no
// organic crew member can read directly. C-3PO steps in, opens the hex reader,
// and presents the raw sigil on screen. The crew can save it to a file or load
// a replacement, but they cannot edit the bytes inline — the sigil is too dense
// for manual entry.
// We do exactly that: this editor handles binary LDAP attribute values by
// opening the HexDialog, which lets the user view the raw bytes, save them to a
// file, or load new bytes from a file — but not edit them in place.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The default value editor for binary LDAP attribute values. It opens the
 * {@link HexDialog}, which displays the value as a hex dump and allows the user
 * to save the binary data to a file or load a replacement from a file. Direct
 * in-dialog editing of the bytes is not supported.
 * Think of this as C-3PO reading out R2's raw binary sigil — he can show it
 * and swap it, but he won't let you type raw hex by hand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HexValueEditor extends AbstractDialogBinaryValueEditor
{

    // ── C-3PO OPENS THE HEX READER FOR THE CREW ──────────────────────────────
    // R2 hands C-3PO a byte array — the raw binary sigil. C-3PO checks it is
    // actually a byte array (not some other object that sneaked in), then opens
    // the hex reader panel. If the crew confirms a replacement file, C-3PO
    // updates the value store; otherwise he closes the panel and walks away.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link HexDialog} so the user can view or replace the binary
     * value. We only proceed if the current value is a {@code byte[]}; if the
     * dialog is confirmed and the user provided replacement data, we store it
     * via {@code setValue()} and return {@code true} to signal that the value
     * changed. Any other outcome returns {@code false}.
     *
     * <p>For example — C-3PO opens the hex reader:</p>
     * <pre>
     *   value = byte[] { 0x04, 0xAB, 0xFF, ... }  (an X.509 certificate)
     *   C-3PO opens HexDialog showing the dump
     *   User clicks "Save to file" → exports the certificate
     *   User clicks "Load from file" → picks a new certificate file
     *   Dialog OK → setValue(newBytes), return true
     * </pre>
     *
     * @param shell  the parent SWT shell for the dialog
     * @return       {@code true} if the user confirmed the dialog and the value
     *               was updated; {@code false} if cancelled or the current value
     *               is not a {@code byte[]}
     */
    protected boolean openDialog( Shell shell )
    {
        Object value = getValue();

        if ( value instanceof byte[] )
        {
            byte[] initialData = ( byte[] ) value;
            HexDialog dialog = new HexDialog( shell, initialData );

            if ( ( dialog.open() == HexDialog.OK ) && dialog.getData() != null )
            {
                setValue( dialog.getData() );

                return true;
            }
        }

        return false;
    }

}
