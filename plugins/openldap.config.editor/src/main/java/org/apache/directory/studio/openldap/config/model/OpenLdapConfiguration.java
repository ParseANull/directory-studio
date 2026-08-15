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
package org.apache.directory.studio.openldap.config.model;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.openldap.config.model.database.OlcDatabaseConfig;


// ── CLASS: OpenLdapConfiguration — Lando Running Cloud City ──────────────────
// Lando Calrissian doesn't just operate one part of Cloud City — he runs the
// entire thing: the tibanna gas mines, the residential towers, the security force,
// the landing pads. He knows where every department is and can hand you a connection
// to any of them on demand.
// OpenLdapConfiguration is the top-level holder for an entire OpenLDAP server's
// configuration: global settings (OlcGlobal), databases (OlcDatabaseConfig), loaded
// modules (OlcModuleList), and any miscellaneous config elements. It's the one object
// that represents the whole cn=config tree in memory.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The top-level in-memory model for a complete OpenLDAP server configuration.
 * It holds the global settings, all database configs, all loaded modules, and any
 * other configuration elements found under cn=config.
 * The I/O layer (ConfigurationReader/ConfigurationWriter) populates and reads
 * from this class. The editor pages use it to display and modify settings.
 * Think of this as Lando running Cloud City — the single hub that owns and
 * manages every department.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapConfiguration
{
    /** The connection */
    private Connection connection;

    /** The global configuration */
    private OlcGlobal global;

    /** The databases list */
    private List<OlcDatabaseConfig> databases = new ArrayList<>();

    /** The other configuration elements list*/
    private List<OlcConfig> configurationElements = new ArrayList<>();

    /** The loaded modules */
    private List<OlcModuleList> modules = new ArrayList<>();


    // ── Get Modules — Lando Reads the Module Roster ───────────────────────────────
    // Lando checks his books and hands over the full list of dynamically-loaded
    // module crews currently working in Cloud City.
    // We expose this for the I/O layer and editor pages that display module settings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of loaded module configurations (OlcModuleList entries).
     * Each entry corresponds to one olcModuleList entry in cn=config.
     *
     * <p>For example — Lando reads the module roster:</p>
     * <pre>
     *   List&lt;OlcModuleList&gt; mods = config.getModules();
     *   // [ {path: /usr/lib/ldap, loads: [back_mdb.la, ppolicy.la]} ]
     * </pre>
     *
     * @return  the live list of OlcModuleList objects; never null
     */
    public List<OlcModuleList> getModules()
    {
        return modules;
    }


    // ── Add Module — Lando Hires a New Crew Unit ──────────────────────────────────
    // Lando signs a contract with a new module unit and adds them to Cloud City's
    // active roster.
    // We add the OlcModuleList so the I/O layer can serialize it back to LDAP.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an OlcModuleList to the configuration.
     * Call this when the user adds a new module load entry in the editor.
     *
     * <p>For example — Lando onboards a new crew unit:</p>
     * <pre>
     *   OlcModuleList newMod = new OlcModuleList();
     *   newMod.setOlcModulePath( "/usr/lib/ldap" );
     *   config.add( newMod );
     * </pre>
     *
     * @param module  the OlcModuleList to add
     */
    public void add( OlcModuleList module )
    {
        modules.add( module );
    }


    // ── Remove Module — Lando Lets a Crew Unit Go ─────────────────────────────────
    // Lando removes a module crew from the active roster.
    // We remove the OlcModuleList so it won't be serialized back to LDAP.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes an OlcModuleList from the configuration.
     * Returns true if the list contained this element.
     *
     * <p>For example — Lando releases a crew unit:</p>
     * <pre>
     *   boolean removed = config.remove( oldMod );
     * </pre>
     *
     * @param module  the OlcModuleList to remove
     * @return        true if the module was found and removed
     */
    public boolean remove( OlcModuleList module )
    {
        return modules.remove( module );
    }


    // ── Clear Module List — Lando Resets the Roster ───────────────────────────────
    // Lando clears the entire module roster, typically before a full reload.
    // Cloud City continues operating on what's already running, but no new
    // module contracts are on the books.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the entire modules list.
     * Useful when reloading the configuration from scratch.
     *
     * <p>For example — Lando resets the roster before a reload:</p>
     * <pre>
     *   config.clearModuleList();
     * </pre>
     */
    public void clearModuleList()
    {
        modules.clear();
    }


    // ── Get Configuration Elements — Lando Lists All Departments ─────────────────
    // Lando hands over the full list of miscellaneous config objects that didn't
    // fit into the databases or global buckets — schema entries, includes, etc.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of miscellaneous configuration elements (everything that isn't
     * OlcGlobal, a database, or a module — e.g., schema entries, include files).
     *
     * <p>For example — Lando lists all departments:</p>
     * <pre>
     *   List&lt;OlcConfig&gt; elems = config.getConfigurationElements();
     * </pre>
     *
     * @return  the live list of OlcConfig objects; never null
     */
    public List<OlcConfig> getConfigurationElements()
    {
        return configurationElements;
    }


    // ── Add Configuration Element — Lando Registers a New Department ──────────────
    // Lando adds a new department to Cloud City's org chart.
    // We add any OlcConfig that doesn't fit the specific typed lists.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an OlcConfig element to the generic configuration elements list.
     * Returns true if the collection changed (it always does for List).
     *
     * <p>For example — Lando registers a new department:</p>
     * <pre>
     *   config.add( new OlcSchemaConfig() );
     * </pre>
     *
     * @param element  the OlcConfig to add
     * @return         true always (list always accepts the element)
     */
    public boolean add( OlcConfig element )
    {
        return configurationElements.add( element );
    }


    // ── Contains Configuration Element — Lando Checks the Registry ───────────────
    // Lando checks his org chart to see if a particular department is registered.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the configuration elements list contains the given element.
     *
     * <p>For example — Lando checks if a department is registered:</p>
     * <pre>
     *   boolean has = config.contains( mySchemaConfig );
     * </pre>
     *
     * @param element  the OlcConfig to look for
     * @return         true if present, false if not
     */
    public boolean contains( OlcConfig element )
    {
        return configurationElements.contains( element );
    }


    // ── Remove Configuration Element — Lando Deregisters a Department ────────────
    // Lando removes a department from his org chart and the configuration model.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes an OlcConfig element from the configuration elements list.
     *
     * <p>For example — Lando deregisters a department:</p>
     * <pre>
     *   config.remove( oldSchemaConfig );
     * </pre>
     *
     * @param element  the OlcConfig to remove
     * @return         true if the element was found and removed
     */
    public boolean remove( OlcConfig element )
    {
        return configurationElements.remove( element );
    }


    // ── Get Databases — Lando Lists All Vaults ───────────────────────────────────
    // Lando hands over the roster of all active database vaults — the primary data
    // stores that slapd is managing. Each vault (BDB, MDB, LDAP, etc.) is listed here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of all database configurations.
     * Each OlcDatabaseConfig represents one database entry under cn=config
     * (e.g., olcDatabase={1}mdb,cn=config).
     *
     * <p>For example — Lando lists all active vaults:</p>
     * <pre>
     *   List&lt;OlcDatabaseConfig&gt; dbs = config.getDatabases();
     * </pre>
     *
     * @return  the live list of OlcDatabaseConfig objects; never null
     */
    public List<OlcDatabaseConfig> getDatabases()
    {
        return databases;
    }


    // ── Add Database — Lando Opens a New Vault ───────────────────────────────────
    // Lando adds a new database vault to Cloud City's registry.
    // We add it so the I/O layer serializes it back to LDAP.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an OlcDatabaseConfig to the databases list.
     *
     * <p>For example — Lando opens a new vault:</p>
     * <pre>
     *   OlcMdbConfig mdb = new OlcMdbConfig();
     *   config.add( mdb );
     * </pre>
     *
     * @param database  the OlcDatabaseConfig to add
     * @return          true always
     */
    public boolean add( OlcDatabaseConfig database )
    {
        return databases.add( database );
    }


    // ── Clear Databases — Lando Empties the Vault Registry ───────────────────────
    // Lando clears the entire vault registry before a reload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the entire databases list.
     *
     * <p>For example — Lando empties the vault registry:</p>
     * <pre>
     *   config.clearDatabases();
     * </pre>
     */
    public void clearDatabases()
    {
        databases.clear();
    }


    // ── Remove Database — Lando Closes a Vault ───────────────────────────────────
    // Lando removes a database vault from the registry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes an OlcDatabaseConfig from the databases list.
     *
     * <p>For example — Lando decommissions a vault:</p>
     * <pre>
     *   config.remove( oldDb );
     * </pre>
     *
     * @param database  the OlcDatabaseConfig to remove
     * @return          true if found and removed
     */
    public boolean remove( OlcDatabaseConfig database )
    {
        return databases.remove( database );
    }


    // ── Get Connection — Check the Cloud City Comms Link ─────────────────────────
    // Lando checks which communications channel (LDAP connection) is currently
    // patched into the directory server that this configuration came from.
    // We expose this so the I/O layer can reconnect and push changes back.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection from which this configuration was loaded.
     * May be null if the config was loaded from a directory on disk rather than
     * a live server.
     *
     * <p>For example — Lando checks his comms link:</p>
     * <pre>
     *   Connection conn = config.getConnection(); // null if loaded from disk
     * </pre>
     *
     * @return  the Connection, or null if loaded from disk
     */
    public Connection getConnection()
    {
        return connection;
    }


    // ── Get Global — Check the Central Command Module ────────────────────────────
    // Lando checks the central command module that issues standing orders to the
    // whole of Cloud City — global settings that apply server-wide.
    // We expose OlcGlobal so editor pages can read and modify server-wide settings.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the global configuration object (OlcGlobal), which holds server-wide
     * settings like threads, TLS, logging, and security policies.
     *
     * <p>For example — Lando checks Cloud City's standing orders:</p>
     * <pre>
     *   OlcGlobal global = config.getGlobal();
     *   global.getOlcLogLevel(); // [ "stats" ]
     * </pre>
     *
     * @return  the OlcGlobal, or null if not yet loaded
     */
    public OlcGlobal getGlobal()
    {
        return global;
    }


    // ── Set Global — Install the Central Command Module ───────────────────────────
    // Lando installs (or replaces) the central command module that governs
    // Cloud City's server-wide standing orders.
    // We store OlcGlobal so the I/O layer can serialize it back to cn=config.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the global configuration (which belongs to cn=config in the DIT).
     *
     * <p>For example — Lando installs the central command module:</p>
     * <pre>
     *   config.setGlobal( loadedGlobal );
     * </pre>
     *
     * @param global  the OlcGlobal to set as the server-wide configuration
     */
    public void setGlobal( OlcGlobal global )
    {
        this.global = global;
    }


    // ── Set Connection — Patch In a New Comms Link ────────────────────────────────
    // Lando patches in a communications channel to the directory server so he can
    // push updates back when the user saves the configuration.
    // We store the connection so the I/O layer knows where to write changes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the LDAP connection associated with this configuration.
     * The I/O layer uses it when saving changes back to a live server.
     *
     * <p>For example — Lando patches in a comms link:</p>
     * <pre>
     *   config.setConnection( activeConnection );
     * </pre>
     *
     * @param connection  the LDAP Connection to associate with this configuration
     */
    public void setConnection( Connection connection )
    {
        this.connection = connection;
    }


    // ── Size — Count the Departments ─────────────────────────────────────────────
    // Lando does a quick headcount of how many departments (configuration elements)
    // are on the books.
    // We expose this for the I/O layer and any code that needs to know how many
    // generic config entries were loaded.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of generic configuration elements (not databases, not modules).
     *
     * <p>For example — Lando counts his departments:</p>
     * <pre>
     *   int count = config.size(); // e.g. 3 (schema, include, etc.)
     * </pre>
     *
     * @return  the size of the configurationElements list
     */
    public int size()
    {
        return configurationElements.size();
    }
}
