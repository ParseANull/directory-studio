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
package org.apache.directory.studio.openldap.config.model.database;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.openldap.common.ui.model.DatabaseTypeEnum;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;


// ── CLASS: OlcDbSocketConfig — Palpatine Routing Operations Through a Unix Socket
// Palpatine has a vault that routes every LDAP operation through a Unix domain
// socket to an external process — that external process handles all the actual data.
// The socket backend is the most minimalist LDAP-to-external-service bridge:
// the slapd process connects to a Unix socket path and speaks a simple protocol.
// OlcDbSocketConfig holds the socket path and the list of protocol extensions.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcDbSocketConfig} object class, representing the
 * configuration of the OpenLDAP socket backend database.
 * The socket backend forwards LDAP operations to an external process listening on
 * a Unix domain socket. The socket path is required; optional extensions can
 * add protocol capabilities negotiated at connection time.
 * Think of this as Palpatine routing all vault operations through a secure socket
 * to an external handler.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcDbSocketConfig extends OlcDatabaseConfig
{
    /**
     * Field for the 'olcDbSocketPath' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbSocketPath", isOptional = false, version="2.4.8")
    private String olcDbSocketPath;

    /**
     * Field for the 'olcDbSocketExtensions' attribute.
     */
    @ConfigurationElement(attributeType = "olcDbSocketExtensions", version="2.4.8")
    private List<String> olcDbSocketExtensions = new ArrayList<>();


    // ── addOlcDbSocketExtensions — Palpatine Adds Protocol Extension Strings ──────
    // Palpatine adds protocol extension strings negotiated with the external handler.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more socket protocol extension strings to the extensions list.
     *
     * <p>For example — Palpatine adds protocol extensions:</p>
     * <pre>
     *   socketConfig.addOlcDbSocketExtensions( "binddn", "peername" );
     * </pre>
     *
     * @param strings  the extension strings to add
     */
    public void addOlcDbSocketExtensions( String... strings )
    {
        for ( String string : strings )
        {
            olcDbSocketExtensions.add( string );
        }
    }


    // ── clearOlcDbSocketExtensions — Palpatine Clears the Extensions ──────────────
    // Palpatine clears all socket protocol extensions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all socket protocol extension strings.
     *
     * <p>For example — Palpatine clears the extensions:</p>
     * <pre>
     *   socketConfig.clearOlcDbSocketExtensions();
     * </pre>
     */
    public void clearOlcDbSocketExtensions()
    {
        olcDbSocketExtensions.clear();
    }


    // ── getOlcDbSocketExtensions — Palpatine Reads the Protocol Extensions ────────
    // Palpatine reads the list of protocol extension strings for the socket backend.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the socket protocol extension strings.
     *
     * <p>For example — Palpatine reads the extensions:</p>
     * <pre>
     *   List&lt;String&gt; exts = socketConfig.getOlcDbSocketExtensions();
     * </pre>
     *
     * @return  a copy of the extensions list; never null
     */
    public List<String> getOlcDbSocketExtensions()
    {
        return copyListString( olcDbSocketExtensions );
    }


    // ── getOlcDbSocketPath — Palpatine Reads the Socket Path ──────────────────────
    // Palpatine reads the Unix socket path that slapd connects to in order to
    // forward LDAP operations to the external handler.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Unix domain socket path this backend connects to.
     * This is required — the socket backend can't operate without it.
     *
     * <p>For example — Palpatine reads the socket path:</p>
     * <pre>
     *   String path = socketConfig.getOlcDbSocketPath(); // "/var/run/ldap.sock"
     * </pre>
     *
     * @return  the socket path string, or null if not yet set
     */
    public String getOlcDbSocketPath()
    {
        return olcDbSocketPath;
    }


    // ── setOlcDbSocketExtensions — Palpatine Replaces the Protocol Extensions ─────
    // Palpatine replaces the full list of protocol extensions.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the list of socket protocol extension strings.
     *
     * <p>For example — Palpatine sets the extensions:</p>
     * <pre>
     *   socketConfig.setOlcDbSocketExtensions( Arrays.asList( "binddn" ) );
     * </pre>
     *
     * @param olcDbSocketExtensions  the new list of extension strings
     */
    public void setOlcDbSocketExtensions( List<String> olcDbSocketExtensions )
    {
        this.olcDbSocketExtensions = copyListString( olcDbSocketExtensions );
    }


    // ── setOlcDbSocketPath — Palpatine Sets the Socket Path ───────────────────────
    // Palpatine sets the Unix socket path the backend will connect to.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the Unix domain socket path for this backend.
     *
     * <p>For example — Palpatine sets the socket path:</p>
     * <pre>
     *   socketConfig.setOlcDbSocketPath( "/var/run/ldap.sock" );
     * </pre>
     *
     * @param olcDbSocketPath  the socket path string (required)
     */
    public void setOlcDbSocketPath( String olcDbSocketPath )
    {
        this.olcDbSocketPath = olcDbSocketPath;
    }


    // ── getOlcDatabaseType — Palpatine Identifies the Socket Vault Type ───────────
    // Palpatine identifies this vault as the "sock" type so OpenLDAP loads the
    // correct socket backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier for the socket backend.
     *
     * <p>For example — Palpatine identifies the socket vault type:</p>
     * <pre>
     *   socketConfig.getOlcDatabaseType(); // "sock"
     * </pre>
     *
     * @return  the lowercase database type string for the socket backend
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.DB_SOCKET.toString().toLowerCase();
    }
}
