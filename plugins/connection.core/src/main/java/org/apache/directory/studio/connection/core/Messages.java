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

package org.apache.directory.studio.connection.core;


import org.eclipse.osgi.util.NLS;


// ── CLASS: Messages — C-3PO'S PHRASE BOOK FOR THE GALAXY ─────────────────────
// C-3PO speaks 6 million forms of communication — but he still needs a phrase
// book to look up the right words in the right language.
// This class is that phrase book for the connection.core plugin: all user-facing
// strings live in messages.properties and get loaded here via Eclipse NLS.
// Using NLS means translators can swap the properties file without touching code.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * NLS (National Language Support) message class for the connection.core plugin.
 * Every user-visible string in the plugin is declared here as a public static
 * field and resolved from {@code messages.properties} at class load time.
 * To add a new string: declare a public static field here, add the matching
 * key=value pair in messages.properties, and then reference
 * {@code Messages.your_field_name} wherever you need the string.
 * Think of this class as C-3PO's phrase book: all the words are defined in one
 * place, in the right language, and we just look them up by name.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Messages extends NLS
{
    /** The path to the messages properties file for this plugin. */
    private static final String BUNDLE_NAME = "org.apache.directory.studio.connection.core.messages"; //$NON-NLS-1$


    // ── CONSTRUCTOR — PRIVATE, NON-INSTANTIABLE ───────────────────────────────────
    // C-3PO doesn't let just anyone reprint his phrase book — it's a singleton
    // resource loaded once by the NLS framework.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this class is a static-field message container
     * and must not be instantiated directly.
     */
    private Messages()
    {
    }

    static
    {
        // initialize resource bundle
        NLS.initializeMessages( BUNDLE_NAME, Messages.class );
    }

    /** Message pattern for naming a copy: "Copy (N) of S". */
    public static String copy_n_of_s;

    /** Error message when a connection initializer extension fails to execute. */
    public static String error__execute_connection_initializer;

    /** Error message when an RDN string cannot be parsed. */
    public static String error__invalid_rdn;

    /** Error message when connections cannot be persisted to disk. */
    public static String error__saving_connections;

    /** Error message when connections cannot be loaded from disk. */
    public static String error__loading_connections;

    /** Error message when a connection listener extension point entry is invalid. */
    public static String error__unable_to_create_connection_listener;

    /** Error message when an LDAP logger extension point entry is invalid. */
    public static String error__unable_to_create_ldap_logger;

    /** Error message when the plugin properties file cannot be read. */
    public static String error__unable_to_get_plugin_properties;

    /** Error message when a write operation is attempted on a read-only connection. */
    public static String error__connection_is_readonly;

    /** Error message when a TLS certificate is not trusted. */
    public static String error__untrusted_certificate;

    /** Error message when no auth handler is registered. */
    public static String model__no_auth_handler;

    /** Error message when the auth handler returns no credentials. */
    public static String model__no_credentials;

    /** Job name for "check bind" with a single connection. */
    public static String jobs__check_bind_name;

    /** Task description while the check-bind job is running. */
    public static String jobs__check_bind_task;

    /** Error message when the check-bind operation fails. */
    public static String jobs__check_bind_error;

    /** Job name for "check network parameter". */
    public static String jobs__check_network_name;

    /** Task description while the check-network job is running. */
    public static String jobs__check_network_task;

    /** Error message when the check-network operation fails. */
    public static String jobs__check_network_error;

    /** Job name when opening exactly one connection. */
    public static String jobs__open_connections_name_1;

    /** Job name when opening multiple connections. */
    public static String jobs__open_connections_name_n;

    /** Task description while connections are being opened. */
    public static String jobs__open_connections_task;

    /** Error message when opening exactly one connection fails. */
    public static String jobs__open_connections_error_1;

    /** Error message when opening multiple connections fails. */
    public static String jobs__open_connections_error_n;

    /** Job name when closing exactly one connection. */
    public static String jobs__close_connections_name_1;

    /** Job name when closing multiple connections. */
    public static String jobs__close_connections_name_n;

    /** Task description while connections are being closed. */
    public static String jobs__close_connections_task;

    /** Error message when closing exactly one connection fails. */
    public static String jobs__close_connections_error_1;

    /** Error message when closing multiple connections fails. */
    public static String jobs__close_connections_error_n;

    /** Error message when the TLS trust manager cannot be created. */
    public static String StudioTrustManager_CantCreateTrustManager;

    /** Error message when adding a certificate to the trust store fails. */
    public static String StudioKeyStoreManager_CantAddCertificateToTrustStore;

    /** Error message when removing a certificate from the trust store fails. */
    public static String StudioKeyStoreManager_CantRemoveCertificateFromTrustStore;

    /** Error message when the trust store file cannot be read. */
    public static String StudioKeyStoreManager_CantReadTrustStore;

    /** Error message when the directory API connection wrapper has no underlying connection. */
    public static String DirectoryApiConnectionWrapper_NoConnection;

    /** Error message when the directory API connection wrapper cannot connect. */
    public static String DirectoryApiConnectionWrapper_UnableToConnect;

    /** Error message when an operation is attempted on an unsecured connection. */
    public static String DirectoryApiConnectionWrapper_UnsecuredConnection;

}
