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

package org.apache.directory.studio.connection.ui.widgets;


import org.apache.commons.lang3.StringUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


// ── CLASS: ConnectionLabelProvider — REBEL BASE SHIP STATUS BOARD ─────────────────
// Walk into the Rebel base hangar and look at the status board: each X-Wing has a
// name tag, a coloured light (green = ready / red = down / yellow = encrypted), and
// a warning placard when a ship is flying on an unsecured channel.
// ConnectionLabelProvider is that status board for the JFace TreeViewer.
// getText() produces the label (appending " (LDAPS)" or " (StartTLS)" and a big
// "UNSECURED!" warning when the connection is live but the channel isn't encrypted).
// getImage() picks the right icon from the image registry based on connected state
// and encryption configuration.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link LabelProvider} for the connection tree widget.
 *
 * <p>Provides display text and icons for {@link Connection} and
 * {@link ConnectionFolder} tree elements.</p>
 *
 * <p>Text rules for connections:</p>
 * <ul>
 *   <li>Appends {@code " (LDAPS)"} or {@code " (StartTLS)"} to the name when
 *       encryption is configured.</li>
 *   <li>Inserts {@code " UNSECURED! "} before the encryption suffix when the
 *       connection is live but the underlying transport reports it is not
 *       actually secured.</li>
 * </ul>
 *
 * <p>Icon rules for connections:</p>
 * <ul>
 *   <li>Connected + secured → SSL connected icon.</li>
 *   <li>Connected + not secured → plain connected icon.</li>
 *   <li>Disconnected + encryption configured → SSL disconnected icon.</li>
 *   <li>Disconnected + no encryption → plain disconnected icon.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionLabelProvider extends LabelProvider
{
    // ── GET TEXT ──────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the display name for a connection or folder:</p>
     * <ul>
     *   <li>{@link ConnectionFolder} — returns {@link ConnectionFolder#getName()}.</li>
     *   <li>{@link Connection} — returns the connection name, optionally followed
     *       by an {@code " UNSECURED! "} warning and an encryption suffix.</li>
     *   <li>{@code null} — returns an empty string.</li>
     *   <li>Anything else — delegates to {@code toString()}.</li>
     * </ul>
     *
     * @param obj The tree element to label.
     * @return The display string for the element.
     */
    @Override
    public String getText( Object obj )
    {
        if ( obj instanceof ConnectionFolder )
        {
            ConnectionFolder folder = ( ConnectionFolder ) obj;

            return folder.getName();
        }

        if ( obj instanceof Connection )
        {
            Connection conn = ( Connection ) obj;

            boolean isConnected = conn.getConnectionWrapper().isConnected();
            boolean isSecured = conn.getConnectionWrapper().isSecured();

            // ── UNSECURED WARNING ─────────────────────────────────────────────────
            // If we are connected but the wrapper says we are not secured (e.g. TLS
            // negotiation failed silently), we prefix " UNSECURED! " so the operator
            // can see immediately that something is wrong.
            // ──────────────────────────────────────────────────────────────────────
            String unsecuredWarning = isConnected && !isSecured ? " UNSECURED! " : ""; //$NON-NLS-1$ //$NON-NLS-2$

            if ( conn.getEncryptionMethod() == EncryptionMethod.LDAPS )
            {
                return conn.getName() + unsecuredWarning + " (LDAPS)"; //$NON-NLS-1$
            }
            else if ( conn.getEncryptionMethod() == EncryptionMethod.START_TLS )
            {
                return conn.getName() + unsecuredWarning + " (StartTLS)"; //$NON-NLS-1$
            }
            else
            {
                return conn.getName();
            }
        }
        else if ( obj == null )
        {
            return StringUtils.EMPTY; //$NON-NLS-1$
        }
        else
        {
            return obj.toString();
        }
    }


    // ── GET IMAGE ─────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Returns the icon for a connection or folder:</p>
     * <ul>
     *   <li>{@link ConnectionFolder} → folder icon.</li>
     *   <li>Connected + secured → {@code IMG_CONNECTION_SSL_CONNECTED}.</li>
     *   <li>Connected + not secured → {@code IMG_CONNECTION_CONNECTED}.</li>
     *   <li>Disconnected + encryption configured → {@code IMG_CONNECTION_SSL_DISCONNECTED}.</li>
     *   <li>Disconnected + plain → {@code IMG_CONNECTION_DISCONNECTED}.</li>
     * </ul>
     *
     * @param obj The tree element to decorate with an icon.
     * @return The {@link Image} for the element, or {@code null} for unknown types.
     */
    @Override
    public Image getImage( Object obj )
    {
        if ( obj instanceof ConnectionFolder )
        {
            return ConnectionUIPlugin.getDefault().getImage( ConnectionUIConstants.IMG_CONNECTION_FOLDER );
        }
        else if ( obj instanceof Connection )
        {
            Connection conn = ( Connection ) obj;

            boolean isConnected = conn.getConnectionWrapper().isConnected();
            boolean isSecured = conn.getConnectionWrapper().isSecured();
            boolean isEncryptionConfigured = conn.getEncryptionMethod().isEncrytped();

            // ── CONNECTED ─────────────────────────────────────────────────────────
            if ( isConnected )
            {
                return isSecured ? ConnectionUIPlugin.getDefault().getImage(
                    ConnectionUIConstants.IMG_CONNECTION_SSL_CONNECTED )
                    : ConnectionUIPlugin.getDefault().getImage(
                        ConnectionUIConstants.IMG_CONNECTION_CONNECTED );
            }
            else
            {
                // ── DISCONNECTED ──────────────────────────────────────────────────
                return isEncryptionConfigured ? ConnectionUIPlugin.getDefault().getImage(
                    ConnectionUIConstants.IMG_CONNECTION_SSL_DISCONNECTED )
                    : ConnectionUIPlugin.getDefault().getImage(
                        ConnectionUIConstants.IMG_CONNECTION_DISCONNECTED );
            }
        }
        else
        {
            return null;
        }
    }
}
