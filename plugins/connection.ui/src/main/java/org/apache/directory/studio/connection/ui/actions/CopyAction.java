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
import java.util.Arrays;
import java.util.List;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.ui.ConnectionParameterPage;
import org.apache.directory.studio.connection.ui.ConnectionParameterPageManager;
import org.apache.directory.studio.connection.ui.dnd.ConnectionTransfer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;


// ── CLASS: CopyAction — HAN TRANSMITS THE SHIP'S MANIFEST ─────────────────────────
// When the Rebel Alliance needs to share the details of a Falcon flight — hostnames,
// ports, auth settings — with another crew, Han transmits the manifest to the
// communications channel.  CopyAction does the same: it serialises the selected
// connections and folders to the system clipboard, both as a typed
// ConnectionTransfer (for paste within Directory Studio) and as plain-text LDAP
// URLs (for pasting into a terminal or text editor).
// After copying, it pings the paste-action proxy to update its enabled state.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Copies the selected connections and/or connection folders to the system clipboard.
 *
 * <p>Two clipboard formats are used simultaneously:</p>
 * <ul>
 *   <li>{@link ConnectionTransfer} — typed transfer for paste within Directory Studio.</li>
 *   <li>{@link TextTransfer} — plain-text LDAP URLs for pasting into external tools.</li>
 * </ul>
 *
 * <p>After the copy, the paired paste-action proxy is refreshed so it knows
 * clipboard content is available.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyAction extends StudioAction
{
    /** The paste action proxy whose enabled state must be refreshed after a copy. */
    private StudioActionProxy pasteActionProxy;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CopyAction}.
     *
     * @param pasteActionProxy  The paste action proxy to refresh after copying;
     *                          may be {@code null} if no paste proxy is registered.
     */
    public CopyAction( StudioActionProxy pasteActionProxy )
    {
        super();
        this.pasteActionProxy = pasteActionProxy;
    }


    // ── GET TEXT — CONTEXT-SENSITIVE LABEL ────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns a singular/plural label based on whether connections or folders are selected.
     */
    public String getText()
    {
        Connection[] connections = getSelectedConnections();
        ConnectionFolder[] connectionFolders = getSelectedConnectionFolders();

        if ( ( connections.length ) > 0 && ( connectionFolders.length == 0 ) )
        {
            if ( connections.length > 1 )
            {
                return Messages.getString( "CopyAction.CopyConnections" );
            }
            else
            {
                return Messages.getString( "CopyAction.CopyConnection" );
            }
        }
        else if ( ( connectionFolders.length > 0 ) && ( connections.length == 0 ) )
        {
            if ( connectionFolders.length > 1 )
            {
                return Messages.getString( "CopyAction.CopyFolders" );
            }
            else
            {
                return Messages.getString( "CopyAction.CopyFolder" );
            }
        }
        else
        {
            return Messages.getString( "CopyAction.Copy" ); //$NON-NLS-1$
        }
    }


    // ── GET IMAGE DESCRIPTOR — ECLIPSE SHARED COPY ICON ───────────────────────────
    /**
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_TOOL_COPY );
    }


    // ── GET COMMAND ID — MAPS TO EDIT > COPY ──────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getCommandId()
    {
        return IWorkbenchCommandConstants.EDIT_COPY;
    }


    // ── RUN — COPY SELECTED OBJECTS TO THE CLIPBOARD ──────────────────────────────
    /**
     * {@inheritDoc}
     * Copies selected connections and folders to the clipboard in both typed and
     * plain-text LDAP URL formats, then refreshes the paste-action proxy.
     */
    public void run()
    {
        Connection[] connections = getSelectedConnections();
        ConnectionFolder[] connectionFolders = getSelectedConnectionFolders();
        List<Object> objects = new ArrayList<>();
        objects.addAll( Arrays.asList( connections ) );
        objects.addAll( Arrays.asList( connectionFolders ) );
        String urls = getSelectedConnectionUrls();

        // ── COPY TO CLIPBOARD — BOTH TYPED AND TEXT FORMATS ───────────────────────
        if ( !objects.isEmpty() )
        {
            if ( urls != null && urls.length() > 0 )
            {
                copyToClipboard( new Object[]
                    { objects.toArray(), urls }, new Transfer[]
                    { ConnectionTransfer.getInstance(), TextTransfer.getInstance() } );
            }
            else
            {
                copyToClipboard( new Object[]
                    { objects.toArray() }, new Transfer[]
                    { ConnectionTransfer.getInstance() } );
            }
        }

        // ── NOTIFY THE PASTE ACTION ────────────────────────────────────────────────
        if ( pasteActionProxy != null )
        {
            pasteActionProxy.updateAction();
        }
    }


    // ── COPY TO CLIPBOARD — STATIC HELPER ─────────────────────────────────────────
    /**
     * Writes the given data objects (in the given transfer formats) to the system clipboard.
     * Creates a temporary {@link Clipboard} and disposes it after the write.
     *
     * @param data       The data objects to place in the clipboard.
     * @param dataTypes  The transfer agents corresponding to each data object.
     */
    public static void copyToClipboard( Object[] data, Transfer[] dataTypes )
    {
        Clipboard clipboard = null;

        try
        {
            clipboard = new Clipboard( Display.getCurrent() );
            clipboard.setContents( data, dataTypes );
        }
        finally
        {
            if ( clipboard != null )
            {
                clipboard.dispose();
            }
        }
    }


    // ── IS ENABLED — AT LEAST ONE CONNECTION OR FOLDER SELECTED ──────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled()
    {
        return getSelectedConnections().length + getSelectedConnectionFolders().length > 0;
    }


    // ── GET SELECTED CONNECTION URLS — SERIALISE TO LDAP URLS ─────────────────────
    // For each selected connection, we build an LdapUrl by asking every registered
    // ConnectionParameterPage to merge its fields into the URL.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Serialises the selected connections to a newline-separated string of LDAP URLs.
     * Uses every registered {@link ConnectionParameterPage} to populate each URL.
     *
     * @return  A newline-separated string of LDAP URL strings, possibly empty.
     */
    private String getSelectedConnectionUrls()
    {
        StringBuilder buffer = new StringBuilder();

        Connection[] connections = getSelectedConnections();
        ConnectionParameterPage[] connectionParameterPages = ConnectionParameterPageManager.getConnectionParameterPages();

        for ( Connection connection : connections )
        {
            ConnectionParameter parameter = connection.getConnectionParameter();
            LdapUrl ldapUrl = new LdapUrl();
            ldapUrl.setDn( Dn.EMPTY_DN );

            for ( ConnectionParameterPage connectionParameterPage : connectionParameterPages )
            {
                connectionParameterPage.mergeParametersToLdapURL( parameter, ldapUrl );
            }

            buffer.append( ldapUrl.toString() );
            buffer.append( ConnectionCoreConstants.LINE_SEPARATOR );
        }

        return buffer.toString();
    }
}
