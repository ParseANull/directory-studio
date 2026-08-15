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
package org.apache.directory.studio.ldapservers.views;


import org.apache.directory.studio.ldapservers.LdapServersPlugin;
import org.apache.directory.studio.ldapservers.LdapServersPluginConstants;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ServersViewLabelProvider — THE CLOUD CITY STATUS DISPLAY PANEL ────────────────
// Cloud City's control room has a big status board: each row shows a facility name (left)
// and an animated status icon with text (right).  A blinking animation cycles through three
// states — one dot, two dots, three dots — for anything in-progress.
// This class is that display panel: it supplies column text and column images for the Servers
// view, including animated frames for STARTING, STOPPING, and REPAIRING states.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Provides text and images for each cell in the Servers view's two-column tree.
 * Column 0: server name and server icon.
 * Column 1: animated status text ("Starting...") and animated status icon.
 * Think of it as Cloud City's animated status display panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServersViewLabelProvider extends LabelProvider implements ITableLabelProvider
{
    // Static strings for dots
    private static final String THREE_DOTS = "..."; //$NON-NLS-1$
    private static final String TWO_DOTS = ".."; //$NON-NLS-1$
    private static final String ONE_DOT = "."; //$NON-NLS-1$

    /** The counter used for dots */
    private int dotsCount = 1;


    // ── Reading The Name And Status Off The Status Board ─────────────────────────────────────
    // The board's left column shows the facility name; the right column shows the current status
    // in plain text.  For in-progress states (STARTING, STOPPING, REPAIRING) the text animates
    // with a trailing dot sequence to signal ongoing work.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for a cell.
     * Column 0 returns the server's name; column 1 returns the localised status string.
     * In-progress states append a dot sequence that cycles with each animation tick.
     *
     * <p>For example — the board shows "My ApacheDS" | "Starting...":</p>
     * <pre>
     *   getColumnText(server, 0) → "My ApacheDS"
     *   getColumnText(server, 1) → "Starting..."  (or ".." or "." depending on frame)
     * </pre>
     *
     * @param element      the element (expected to be an {@link LdapServer})
     * @param columnIndex  0 for name, 1 for status text
     * @return the text to display in the cell
     */
    public String getColumnText( Object element, int columnIndex )
    {
        if ( element instanceof LdapServer )
        {
            LdapServer server = ( LdapServer ) element;
            if ( columnIndex == 0 )
            {
                return server.getName();
            }
            else if ( columnIndex == 1 )
            {
                LdapServerStatus status = ( ( LdapServer ) element ).getStatus();
                switch ( status )
                {
                    case STARTED:
                        return Messages.getString( "ServersViewLabelProvider.Started" ); //$NON-NLS-1$
                    case STARTING:
                        return Messages.getString( "ServersViewLabelProvider.Starting" ) + getDots(); //$NON-NLS-1$
                    case STOPPED:
                        return Messages.getString( "ServersViewLabelProvider.Stopped" ); //$NON-NLS-1$
                    case STOPPING:
                        return Messages.getString( "ServersViewLabelProvider.Stopping" ) + getDots(); //$NON-NLS-1$
                    case UNKNOWN:
                        return Messages.getString( "ServersViewLabelProvider.Unknown" ); //$NON-NLS-1$
                    case REPAIRING:
                        return Messages.getString( "ServersViewLabelProvider.Repairing" ) + getDots(); //$NON-NLS-1$
                }
            }

        }

        return super.getText( element );
    }


    // ── Getting The Right Number Of Dots For The Animation Frame ─────────────────────────────
    // The animation thread calls animate() every 200 ms, advancing dotsCount from 1 to 3.
    // getDots() converts that counter to ".", "..", or "..." for the trailing status text.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the dot string (".","..","...") corresponding to the current animation frame.
     * Used by {@link #getColumnText} for in-progress statuses.
     *
     * @return the dot suffix for the current animation frame
     */
    private String getDots()
    {
        if ( dotsCount == 1 )
        {
            return ServersViewLabelProvider.ONE_DOT;
        }
        else if ( dotsCount == 2 )
        {
            return ServersViewLabelProvider.TWO_DOTS;
        }
        else
        {
            return ServersViewLabelProvider.THREE_DOTS;
        }
    }


    // ── Picking The Right Status Icon For This Frame ─────────────────────────────────────────
    // Column 0 always gets the generic server icon; column 1 gets a status icon.
    // For STARTING/STOPPING/REPAIRING, three different image variants cycle with the animation
    // to give a spinning or blinking effect.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon image for a cell.
     * Column 0 returns the generic server icon.
     * Column 1 returns the status icon — animated (three frames) for STARTING/STOPPING/REPAIRING.
     *
     * @param element      the element (expected to be an {@link LdapServer})
     * @param columnIndex  0 for server icon, 1 for status icon
     * @return the image to display in the cell
     */
    public Image getColumnImage( Object element, int columnIndex )
    {
        if ( element instanceof LdapServer )
        {
            if ( columnIndex == 0 )
            {
                return LdapServersPlugin.getDefault().getImage( LdapServersPluginConstants.IMG_SERVER );
            }
            else if ( columnIndex == 1 )
            {
                switch ( ( ( LdapServer ) element ).getStatus() )
                {
                    case STARTED:
                        return LdapServersPlugin.getDefault().getImage( LdapServersPluginConstants.IMG_SERVER_STARTED );
                    case REPAIRING:
                    case STARTING:
                        switch ( dotsCount )
                        {
                            case 1:
                                return LdapServersPlugin.getDefault().getImage(
                                    LdapServersPluginConstants.IMG_SERVER_STARTING1 );
                            case 2:
                                return LdapServersPlugin.getDefault().getImage(
                                    LdapServersPluginConstants.IMG_SERVER_STARTING2 );
                            case 3:
                                return LdapServersPlugin.getDefault().getImage(
                                    LdapServersPluginConstants.IMG_SERVER_STARTING3 );
                        }
                    case STOPPED:
                        return LdapServersPlugin.getDefault().getImage( LdapServersPluginConstants.IMG_SERVER_STOPPED );
                    case STOPPING:
                        switch ( dotsCount )
                        {
                            case 1:
                                return LdapServersPlugin.getDefault().getImage(
                                    LdapServersPluginConstants.IMG_SERVER_STOPPING1 );
                            case 2:
                                return LdapServersPlugin.getDefault().getImage(
                                    LdapServersPluginConstants.IMG_SERVER_STOPPING2 );
                            case 3:
                                return LdapServersPlugin.getDefault().getImage(
                                    LdapServersPluginConstants.IMG_SERVER_STOPPING3 );
                        }
                    case UNKNOWN:
                        return LdapServersPlugin.getDefault().getImage( LdapServersPluginConstants.IMG_SERVER );
                }
            }
        }

        return super.getImage( element );
    }


    // ── Advancing The Animation Frame ────────────────────────────────────────────────────────
    // The animation thread fires every 200 ms and calls this to advance the frame counter.
    // It wraps from 3 back to 1 so the dots cycle: ".", "..", "...", ".", "..", "..."
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Advances the animation dot counter by one, wrapping back to 1 after 3.
     * Called by {@link ServersTableViewer}'s animation thread every 200 ms for in-progress servers.
     */
    public void animate()
    {
        dotsCount++;

        if ( dotsCount > 3 )
        {
            dotsCount = 1;
        }
    }
}
