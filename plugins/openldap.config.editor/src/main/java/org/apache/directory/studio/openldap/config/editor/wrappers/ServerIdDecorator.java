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

// ── CLASS: ServerIdDecorator — The Fleet Registry of Ship Identification Numbers ─
// Every Star Destroyer in the Imperial fleet carries a unique identification
// number (sometimes combined with a comm URL).  ServerIdDecorator manages the
// fleet registry for the ServerID table: it wires the table to the
// ServerIdDialog, renders each ServerIdWrapper as its number (and optional URL),
// and sorts entries by server ID then URL.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link TableDecorator} for the ServerID table.  It connects the table to
 * {@link ServerIdDialog} and renders each {@link ServerIdWrapper} as a numeric
 * ID (optionally followed by a URL).  Sorting is by server ID first, then URL.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerIdDecorator extends TableDecorator<ServerIdWrapper>
{
    // ── Constructor — Opening the Fleet Registry ───────────────────────────────
    // The registry clerk opens the fleet ID log and connects it to the
    // ServerIdDialog where entries can be added or changed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Create a new instance of ServerIdDecorator
     * @param parentShell The parent Shell
     */
    public ServerIdDecorator( Shell parentShell )
    {
        setDialog( new ServerIdDialog( parentShell ) );
    }


    // ── getText — Read the Ship Identification Number ──────────────────────────
    // The registry clerk reads the ship's ID number (and optional comm URL)
    // off the log entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Construct the label for a ServerID. It can be a number in [0..999], or an URL
     *
     * @return the server ID string, or the default label if the element is not
     *         a {@link ServerIdWrapper}
     */
    @Override
    public String getText( Object element )
    {
        if ( element instanceof ServerIdWrapper )
        {
            return element.toString();
        }

        return super.getText( element );
    }


    // ── getImage — No Icon on the Registry Entry ───────────────────────────────
    // The fleet registry is text only — no images.
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


    // ── compare — Sort Server IDs by Natural Ordering ─────────────────────────
    // The registry is kept in ID order; null entries trail.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare( ServerIdWrapper e1, ServerIdWrapper e2 )
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
