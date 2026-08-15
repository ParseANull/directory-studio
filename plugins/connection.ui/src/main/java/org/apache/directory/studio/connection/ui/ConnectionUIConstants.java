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

package org.apache.directory.studio.connection.ui;


// ── CLASS: ConnectionUIConstants — THE FALCON'S INSTRUMENT PANEL LABELS ──────────
// Every switch and dial in the Falcon's cockpit has a label plate bolted to it.
// This class is that collection of label plates for the connection.ui plugin:
// icon resource paths, dialog settings keys used to remember typed-in history,
// command IDs for the Eclipse command framework, and so on.
// All constants are public static final — there's no mutable state here.
// The class is final and non-instantiable to make it clear it's a bag of labels,
// not a service.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Non-instantiable constants class for the {@code connection.ui} plugin.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Plugin ID — used in {@link org.eclipse.core.runtime.Status} and extension-point lookups.</li>
 *   <li>Dialog settings keys — used to persist auto-complete history across sessions.</li>
 *   <li>Icon resource paths — passed to {@link ConnectionUIPlugin#getImage(String)}.</li>
 *   <li>Eclipse command IDs — used by {@link org.apache.directory.studio.connection.ui.actions}.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class ConnectionUIConstants
{
    /** The OSGi bundle / plug-in ID for this plugin. */
    public static final String PLUGIN_ID = ConnectionUIConstants.class.getPackage().getName();

    // ── DIALOG SETTINGS KEYS — HISTORY COMBO PERSISTENCE ─────────────────────────
    /** Dialog settings key for the list of previously entered host names. */
    public static final String DIALOGSETTING_KEY_HOST_HISTORY = "hostHistory"; //$NON-NLS-1$

    /** Dialog settings key for the list of previously entered port numbers. */
    public static final String DIALOGSETTING_KEY_PORT_HISTORY = "portHistory"; //$NON-NLS-1$

    /** Dialog settings key for the list of previously entered bind principals (bind DNs). */
    public static final String DIALOGSETTING_KEY_PRINCIPAL_HISTORY = "principalHistory"; //$NON-NLS-1$

    /** Dialog settings key for the list of previously entered SASL realm names. */
    public static final String DIALOGSETTING_KEY_REALM_HISTORY = "saslrealmHistory"; //$NON-NLS-1$

    // ── ICON RESOURCE PATHS ───────────────────────────────────────────────────────
    /** Path to the certificate icon. */
    public static final String IMG_CERTIFICATE = "resources/icons/certificate.gif"; //$NON-NLS-1$

    /** Path to the "add connection" icon. */
    public static final String IMG_CONNECTION_ADD = "resources/icons/connection_add.gif"; //$NON-NLS-1$

    /** Path to the icon shown for a connected (open) plain connection. */
    public static final String IMG_CONNECTION_CONNECTED = "resources/icons/connection_connected.gif"; //$NON-NLS-1$

    /** Path to the icon shown for a disconnected (closed) plain connection. */
    public static final String IMG_CONNECTION_DISCONNECTED = "resources/icons/connection_disconnected.gif"; //$NON-NLS-1$

    /** Path to the icon shown for a connected SSL/TLS connection. */
    public static final String IMG_CONNECTION_SSL_CONNECTED = "resources/icons/connection_ssl_connected.gif"; //$NON-NLS-1$

    /** Path to the icon shown for a disconnected SSL/TLS connection. */
    public static final String IMG_CONNECTION_SSL_DISCONNECTED = "resources/icons/connection_ssl_disconnected.gif"; //$NON-NLS-1$

    /** Path to the "connect" action icon. */
    public static final String IMG_CONNECTION_CONNECT = "resources/icons/connection_connect.gif"; //$NON-NLS-1$

    /** Path to the "disconnect" action icon. */
    public static final String IMG_CONNECTION_DISCONNECT = "resources/icons/connection_disconnect.gif"; //$NON-NLS-1$

    /** Path to the New Connection Wizard banner icon. */
    public static final String IMG_CONNECTION_WIZARD = "resources/icons/connection_wizard.gif"; //$NON-NLS-1$

    /** Path to the connection folder icon. */
    public static final String IMG_CONNECTION_FOLDER = "resources/icons/connection_folder.gif"; //$NON-NLS-1$

    /** Path to the "add connection folder" icon. */
    public static final String IMG_CONNECTION_FOLDER_ADD = "resources/icons/connection_folder_add.gif"; //$NON-NLS-1$

    /** Path to the SCIM connection icon (placeholder; replace with real SCIM artwork). */
    public static final String IMG_SCIM_CONNECTION = "resources/icons/scim_connection.gif"; //$NON-NLS-1$

    /** Path to the "expand all" toolbar icon. */
    public static final String IMG_EXPANDALL = "resources/icons/expandall.gif"; //$NON-NLS-1$

    /** Path to the "collapse all" toolbar icon. */
    public static final String IMG_COLLAPSEALL = "resources/icons/collapseall.gif"; //$NON-NLS-1$

    /** Path to the Export Certificate Wizard banner icon. */
    public static final String IMG_CERTIFICATE_EXPORT_WIZARD = "resources/icons/certificate_export_wizard.gif"; //$NON-NLS-1$

    // ── DRAG-AND-DROP ─────────────────────────────────────────────────────────────
    /** The Transfer type name used in drag-and-drop operations for connections. */
    public static final String TYPENAME = "org.apache.directory.studio.ldapbrowser.connection"; //$NON-NLS-1$

    // ── ECLIPSE COMMAND IDs ───────────────────────────────────────────────────────
    /** Eclipse command ID for the Copy action in the Connections view. */
    public static final String CMD_COPY = "org.apache.directory.studio.ldapbrowser.action.copy"; //$NON-NLS-1$

    /** Eclipse command ID for the Paste action in the Connections view. */
    public static final String CMD_PASTE = "org.apache.directory.studio.ldapbrowser.action.paste"; //$NON-NLS-1$

    /** Eclipse command ID for the Delete action in the Connections view. */
    public static final String CMD_DELETE = "org.apache.directory.studio.ldapbrowser.action.delete"; //$NON-NLS-1$

    /** Eclipse command ID for the Properties action in the Connections view. */
    public static final String CMD_PROPERTIES = "org.apache.directory.studio.ldapbrowser.action.properties"; //$NON-NLS-1$

    // ── NEW WIZARD ID ─────────────────────────────────────────────────────────────
    /** The extension point ID of the New Connection Wizard, read from plugin.properties. */
    public static final String NEW_WIZARD_NEW_CONNECTION = ConnectionUIPlugin.getDefault().getPluginProperties()
        .getString( "NewWizards_NewConnectionWizard_id" ); //$NON-NLS-1$


    /**
     * Prevents instantiation.  All members are static constants.
     */
    private ConnectionUIConstants()
    {
    }
}
