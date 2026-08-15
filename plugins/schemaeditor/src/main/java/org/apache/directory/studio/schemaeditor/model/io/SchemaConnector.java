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
package org.apache.directory.studio.schemaeditor.model.io;


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.schemaeditor.model.Project;


// ── CLASS: SchemaConnector — Han's Hyperspace Jump Contract ───────────────────
// Every pilot has a different hyperspace route: the Millennium Falcon uses one
// set of coordinates to reach ApacheDS, another to reach a generic LDAP server.
// But they all share the same contract: can you make the jump to THIS destination?
// Do the jump.  Reverse the jump.  Tell me your name.  This interface defines
// that contract — every concrete connector (ApacheDsSchemaConnector,
// GenericSchemaConnector) must honour all of these operations.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Defines the contract for anything that can read or write an LDAP schema over
 * a live connection.
 * Concrete implementations are registered via the OSGi extension point and
 * selected at runtime based on which one reports {@code isSuitableConnector == true}
 * for the target server.
 * Think of this interface as Han Solo's navigation contract: every ship that can
 * make the jump to hyperspace must be able to check whether the destination is
 * reachable, make the jump (import), and in theory reverse it (export).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface SchemaConnector
{
    // ── Han Checks Whether He Can Reach This Destination ─────────────────────
    // Before plotting the jump Han pings the nav beacon: "can the Falcon reach
    // this system?"  If yes, we commit; if no, we try a different route.
    // Implementations probe the server (e.g. check vendorName or subschemaSubentry)
    // to decide whether they can handle it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tests whether this connector is compatible with the given LDAP server.
     * Typically probes a root DSE attribute to identify the server brand.
     *
     * @param connection  the connection to the LDAP server — must be open
     * @param monitor     progress monitor for the probe search
     * @return            true if this connector can import/export schema for this server
     */
    boolean isSuitableConnector( Connection connection, StudioProgressMonitor monitor );


    // ── Han Makes the Jump — Schema Import ───────────────────────────────────
    // Han punches in the coordinates and makes the jump; the Falcon arrives at
    // the destination fully loaded with the server's schema data, all stowed in
    // the project's schema handler.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fetches the LDAP server's schema and loads it into the given project.
     * Implementations search the server over the live connection and populate
     * the project's schema handler with the discovered attribute types, object
     * classes, matching rules, and syntaxes.
     *
     * @param project  the project to populate — its connection must be open
     * @param monitor  progress monitor so the user can track progress
     * @throws SchemaConnectorException  if the connection fails or the schema can't be read
     */
    void importSchema( Project project, StudioProgressMonitor monitor )
        throws SchemaConnectorException;


    // ── Han Makes the Return Jump — Schema Export ─────────────────────────────
    // Han heads back to the destination and delivers the cargo (the modified
    // schema) — writing it back to the LDAP server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Writes the project's current schema back to the connected LDAP server.
     * Not all implementations support this; many currently throw or are no-ops.
     *
     * @param project  the project whose schema should be exported
     * @param monitor  progress monitor
     * @throws SchemaConnectorException  if the connection fails or the export can't complete
     */
    void exportSchema( Project project, StudioProgressMonitor monitor )
        throws SchemaConnectorException;


    // ── Han Reads the Ship's Name off the Hull ────────────────────────────────
    // Every connector has a display name the user sees in the connection wizard.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable display name of this connector.
     *
     * @return  the connector name — may be null if not configured
     */
    String getName();


    // ── Han Paints the Ship's Name on the Hull ────────────────────────────────
    // Sets the display name, typically called by the extension-point reader.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the human-readable display name of this connector.
     *
     * @param name  the display name to set
     */
    void setName( String name );


    // ── Han Checks the Transponder ID ────────────────────────────────────────
    // Every connector has a unique extension-point ID used to persist the
    // connector choice in the project file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the unique extension-point ID of this connector.
     *
     * @return  the ID string
     */
    String getId();


    // ── Han Programs the New Transponder ID ───────────────────────────────────
    // Sets the ID, typically called by the extension-point reader when the
    // connector is instantiated from plugin.xml.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the unique identifier of this connector.
     *
     * @param id  the ID string from the extension point
     */
    void setId( String id );


    // ── Han Reads the Ship's Technical Description ────────────────────────────
    // A longer description shown in tooltips or the details pane of the
    // connection wizard.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the description of this connector.
     *
     * @return  the description — may be null
     */
    String getDescription();


    // ── Han Updates the Ship's Registry Entry ────────────────────────────────
    // Sets the description, typically called by the extension-point reader.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the description of this connector.
     *
     * @param description  the description to store
     */
    void setDescription( String description );
}
