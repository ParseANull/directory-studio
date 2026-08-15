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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: TcpBufferDecorator — The Star Destroyer's Comm Relay Buffer ────────
// The Star Destroyer's communications officer monitors the receive and transmit
// buffer sizes for every comm relay — each entry shows the listener URL, the
// direction (read or write), and the buffer size.  TcpBufferDecorator does the
// same for the TCPBuffer table: it wires the table to the TcpBufferDialog and
// renders each TcpBufferWrapper as its full configuration string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the TCPBuffer table in the Tuning page.
 * It connects the table to {@link TcpBufferDialog} and renders each
 * {@link TcpBufferWrapper} as its string representation
 * (optional listener URL, optional direction, and buffer size).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TcpBufferDecorator extends TableDecorator<TcpBufferWrapper>
{
    // ── Constructor — Connecting the Comm Relay Monitor ───────────────────────
    // The communications officer connects the buffer monitor to the editing
    // dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of TcpBufferDecorator
     * @param parentShell The parent Shell
     */
    public TcpBufferDecorator( Shell parentShell )
    {
        setDialog( new TcpBufferDialog( parentShell ) );
    }


    // ── getText — Read the Buffer Configuration String ────────────────────────
    // The communications officer reads the buffer entry off the monitor — we
    // delegate to the wrapper's toString.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a TCPBuffer.
     *
     * @return the TCP buffer configuration string, or the default label if the
     *         element is not a {@link TcpBufferWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof TcpBufferWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Buffer Monitor ───────────────────────────────
    // The monitor shows numbers only — no images.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Get the image. We have none (may be we could add one for URLs ?)
     *
     * @return always {@code null}
     */
    @Override
    public Image getImage( Object element )
    {
        return null;
    }


    // ── compare — Sort Buffer Entries by Size Then Listener ───────────────────
    // The communications officer sorts by buffer size first; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( TcpBufferWrapper e1, TcpBufferWrapper e2 )
    {
        if ( e1 != null )
        {
            return e1.compareTo( e2 );
        }
        else
        {
            if ( e2 == null )
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }
}
