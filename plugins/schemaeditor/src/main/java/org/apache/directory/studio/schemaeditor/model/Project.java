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
package org.apache.directory.studio.schemaeditor.model;


import java.util.List;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.io.SchemaConnector;
import org.apache.directory.studio.schemaeditor.model.io.SchemaConnectorException;


// ── CLASS: Project — Lando Running Cloud City ─────────────────────────────────
// Lando Calrissian keeps Cloud City running: he knows whether the city is open
// or shut (OPEN/CLOSED state), what kind of operation it is (tibanna-gas
// mining or tourist resort — OFFLINE/ONLINE type), and he coordinates the
// supply lines (schemas) that flow in from the galaxy. When an ONLINE city
// needs a fresh supply run from an LDAP server, Lando calls in the freighters
// (schemaConnector) and registers everything with the city's logistics desk
// (schemaHandler).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a Schema Editor project — the top-level container that groups
 * one or more schemas and tracks how they were obtained (offline file vs.
 * live LDAP server connection).
 * Think of this class as Lando running Cloud City: he manages state (open or
 * closed), knows which type of operation is running (offline or online), holds
 * the connection to the outside galaxy, and coordinates schema imports on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Project
{
    /**
     * This enum represents the different states of Project.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum ProjectState
    {
        /** An Open project*/
        OPEN,
        /** A Closed project*/
        CLOSED
    }

    /** The type of the project */
    private ProjectType type;

    /** The name of the project */
    private String name;

    /** The connection of the project */
    private Connection connection;

    /** The state of the project */
    private ProjectState state;

    /** The SchemaConnector of the project */
    private SchemaConnector schemaConnector;

    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    /** The initial schema of the Online Schema */
    private List<Schema> initialSchema;

    /** The flag for Online Schema Fetch */
    private boolean hasOnlineSchemaBeenFetched = false;


    // ── Lando Opens Cloud City With A Name And A Business Model ─────────────────
    // Lando signs the operating charter: the city has a name and a declared type
    // (mining station or resort) and starts in the CLOSED state until the docking
    // bay doors are manually opened.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new project with a given type and name, starting in the CLOSED state.
     * Use this when you know both up front, e.g. when loading a saved project file.
     *
     * <p>For example — Lando signs the charter:</p>
     * <pre>
     *   new Project(ProjectType.OFFLINE, "mySchemas")
     *   → type=OFFLINE, name="mySchemas", state=CLOSED
     * </pre>
     *
     * @param type  whether this is an OFFLINE or ONLINE project
     * @param name  the display name for this project
     */
    public Project( ProjectType type, String name )
    {
        init( type, name, ProjectState.CLOSED );
    }


    // ── Lando Opens A Default Cloud City ────────────────────────────────────────
    // Lando spins up a bare-bones city: no name yet, offline mode, doors closed.
    // Useful when building a project from scratch in a wizard where the user
    // hasn't filled in the details yet.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new project with default settings: type is OFFLINE, no name, and
     * state is CLOSED. Typically used when a wizard needs an empty container to
     * populate incrementally.
     */
    public Project()
    {
        init( ProjectType.OFFLINE, null, ProjectState.CLOSED );
    }


    // ── Lando Opens A Typed Cloud City Without A Name ────────────────────────────
    // Lando knows what kind of city he's running (mining or resort) but hasn't
    // settled on a name yet. Doors start closed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new project of the given type, with no name and in the CLOSED state.
     * Use this when the type is fixed at construction time but the name will be set
     * separately.
     *
     * @param type  whether this is an OFFLINE or ONLINE project
     */
    public Project( ProjectType type )
    {
        init( type, null, ProjectState.CLOSED );
    }


    // ── Lando's City Logistics Desk Gets Wired Up ───────────────────────────────
    // Lando's first act: wire up the logistics desk (SchemaHandler) so supply
    // routes can be registered. The name, type, and state get stamped on the
    // city charter.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Shared initialisation logic called by every constructor — sets type, name,
     * and state, then creates a fresh SchemaHandler for this project. We extract
     * this into a private method so the three public constructors stay clean and
     * DRY.
     *
     * @param type   the project type (OFFLINE or ONLINE)
     * @param name   the project name; may be null at construction time
     * @param state  the initial project state (always CLOSED at construction)
     */
    private void init( ProjectType type, String name, ProjectState state )
    {
        this.type = type;
        this.name = name;
        this.state = state;
        schemaHandler = new SchemaHandler();
    }


    // ── Lando Checks The City Operating Charter ──────────────────────────────────
    // Lando pulls out the charter to confirm whether this is a mining operation
    // (OFFLINE) or a full resort connected to the Empire's supply lines (ONLINE).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the type of this project — OFFLINE (schemas live in local files)
     * or ONLINE (schemas are fetched from a live LDAP server).
     * Callers use this to decide whether to show the connection panel in the UI.
     *
     * @return  the project type; never null after construction
     */
    public ProjectType getType()
    {
        return type;
    }


    // ── Lando Updates The Charter With A New Business Model ─────────────────────
    // The Empire changes the city's operating permit. Lando has to update the
    // charter to reflect the new mode.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Changes the project type. You'd call this when converting a project from
     * offline to online (or vice versa), though in practice this happens rarely
     * outside of import/restore flows.
     *
     * @param type  the new project type
     */
    public void setType( ProjectType type )
    {
        this.type = type;
    }


    // ── Lando Reads The City's Name Off The Sign ─────────────────────────────────
    // "Cloud City" — that's what the sign says. Lando checks it when he needs to
    // report back to someone who asked which city this is.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this project, as shown in the Projects view.
     * May be null for a newly constructed project that hasn't been named yet.
     *
     * @return  the project name, or null if not yet set
     */
    public String getName()
    {
        return name;
    }


    // ── Lando Puts A New Sign Above The Docking Bay ─────────────────────────────
    // Lando renames the city — maybe the marketing team has a better idea. Either
    // way, the new name gets stamped on everything.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the display name of this project. Callers should also fire a rename
     * event on the ProjectsHandler so the UI updates.
     *
     * @param name  the new project name
     */
    public void setName( String name )
    {
        this.name = name;
    }


    // ── Lando Checks Whether The Docking Bays Are Open ──────────────────────────
    // Are the docking bay doors open (OPEN) or sealed (CLOSED)? Lando checks before
    // deciding whether to accept incoming freighters.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current project state — OPEN (active, schemas loaded) or CLOSED
     * (inactive, schemas not yet populated). The Projects view uses this to decide
     * which actions to enable.
     *
     * @return  the current project state; never null
     */
    public ProjectState getState()
    {
        return state;
    }


    // ── Lando Opens Or Seals The Docking Bay Doors ──────────────────────────────
    // Lando throws the lever: OPEN to let freighters in, CLOSED to lock everything
    // down. This drives what the Projects view shows and what operations are allowed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the project state. Changing from CLOSED to OPEN is how we "open" a
     * project in the UI; going back to CLOSED is how we close it. The ProjectsHandler
     * listens for these transitions and refreshes the view.
     *
     * @param state  OPEN to activate the project, CLOSED to deactivate it
     */
    public void setState( ProjectState state )
    {
        this.state = state;
    }


    // ── Lando Checks In With The Logistics Desk ──────────────────────────────────
    // The logistics desk (SchemaHandler) tracks every schema registered in this
    // project. Lando hands a reference to it so callers can query or mutate the
    // schema registry directly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SchemaHandler that manages all schemas registered in this project.
     * This is the central index for looking up attribute types, object classes, etc.
     * within the project's schema set. Most callers go through this rather than
     * touching the Schema list directly.
     *
     * @return  the project's SchemaHandler; never null
     */
    public SchemaHandler getSchemaHandler()
    {
        return schemaHandler;
    }


    // ── Lando Checks Which Hyperspace Lane Leads Here ───────────────────────────
    // An ONLINE project is plugged into a specific LDAP server. The Connection
    // object is Cloud City's hyperspace coordinates — it tells us where to fly
    // when we need fresh schemas.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP server connection associated with this project, if any.
     * Only relevant for ONLINE projects — OFFLINE projects will have null here.
     * The connection is used by the SchemaConnector when fetching the live schema.
     *
     * @return  the server connection, or null for OFFLINE projects
     */
    public Connection getConnection()
    {
        return connection;
    }


    // ── Lando Plots The Hyperspace Coordinates ───────────────────────────────────
    // Lando enters the LDAP server's coordinates into the nav computer. From this
    // point, the city can request supplies from that specific server.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP server connection for this project. Call this when configuring
     * an ONLINE project — without a connection the schema connector won't know
     * where to fetch schemas from.
     *
     * @param connection  the server connection to associate with this project
     */
    public void setConnection( Connection connection )
    {
        this.connection = connection;
    }


    // ── Lando Calls In The Supply Freighters From The Server ────────────────────
    // An ONLINE project needs its schemas shipped in from the LDAP server. Lando
    // calls in the connector (like Han flying in with supplies), waits for the
    // cargo to arrive, and then registers each schema with the city's logistics
    // desk. He only does this once — the flag prevents a redundant second run.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Fetches the live schema from the connected LDAP server and registers it
     * with this project's SchemaHandler. We only do this once; if it's already
     * been fetched, this is a no-op. The monitor is used to report progress and
     * surface any connector errors without throwing.
     *
     * <p>For example — Lando calls in the supply run:</p>
     * <pre>
     *   fetchOnlineSchema(monitor)
     *   → connector imports schemas → each schema added to schemaHandler
     *   → hasOnlineSchemaBeenFetched = true
     * </pre>
     *
     * @param monitor  a progress monitor for reporting import progress and errors;
     *                 errors from the connector are reported here rather than thrown
     */
    public void fetchOnlineSchema( StudioProgressMonitor monitor )
    {
        if ( ( !hasOnlineSchemaBeenFetched ) && ( connection != null ) && ( schemaConnector != null ) )
        {
            try
            {
                schemaConnector.importSchema( this, monitor );
            }
            catch ( SchemaConnectorException e )
            {
                monitor.reportError( e );
            }

            // Adding each schema to the schema handler
            if ( initialSchema != null )
            {
                monitor.beginTask( Messages.getString( "Project.AddingSchemaToProject" ), initialSchema.size() ); //$NON-NLS-1$
                for ( Schema schema : initialSchema )
                {
                    getSchemaHandler().addSchema( schema );
                }
            }

            hasOnlineSchemaBeenFetched = true;
            monitor.done();
        }
    }


    // ── Lando Checks Whether The Supply Run Already Happened ────────────────────
    // Did the freighters already dock and unload? Lando checks the ledger before
    // calling in another supply run — no need to fly twice.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether {@link #fetchOnlineSchema} has already completed a successful
     * fetch. We use this flag to prevent redundant re-fetches when the project is
     * re-opened or the view is refreshed.
     *
     * @return  true if the online schema has already been fetched
     */
    public boolean hasOnlineSchemaBeenFetched()
    {
        return hasOnlineSchemaBeenFetched;
    }


    // ── Lando Checks The Original Cargo Manifest ─────────────────────────────────
    // When the first supply freighters landed, they left a manifest of exactly what
    // they delivered. Lando keeps this as a reference point — useful for computing
    // diffs between the initial state and any edits.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the initial snapshot of schemas as they were when first fetched from
     * the LDAP server. Used to compute a diff between the original server state and
     * any local edits the user has made. May be null if no fetch has occurred yet.
     *
     * @return  the initial schema list, or null if not yet set
     */
    public List<Schema> getInitialSchema()
    {
        return initialSchema;
    }


    // ── Lando Files The Original Cargo Manifest ──────────────────────────────────
    // After the first supply run, Lando records the exact cargo delivered so he
    // can compare it to future states. The SchemaConnector calls this during import.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the initial schema snapshot. This is called by the SchemaConnector
     * during {@link #fetchOnlineSchema} to record the server's original state so
     * we can later compute what the user changed.
     *
     * @param initialSchema  the list of schemas as fetched from the server
     */
    public void setInitialSchema( List<Schema> initialSchema )
    {
        this.initialSchema = initialSchema;
    }


    // ── Lando Checks Which Freighter Company Handles The Supply Run ──────────────
    // Different LDAP servers speak slightly different dialects. Lando picks the
    // right connector (ApacheDS vs. Generic) to match the server type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SchemaConnector responsible for importing schemas from the LDAP
     * server. The connector encapsulates all server-specific import logic. May be
     * null for OFFLINE projects or before the connector is configured.
     *
     * @return  the schema connector, or null if not configured
     */
    public SchemaConnector getSchemaConnector()
    {
        return schemaConnector;
    }


    // ── Lando Assigns A Freighter Company To The Route ──────────────────────────
    // Lando picks the right logistics partner for the supply line based on the
    // server type (ApacheDS, OpenLDAP, etc.) and registers them for future runs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the SchemaConnector that will be used when fetching schemas from the
     * LDAP server. The correct connector type depends on the server's dialect —
     * set this before calling {@link #fetchOnlineSchema}.
     *
     * @param schemaConnector  the connector implementation to use
     */
    public void setSchemaConnector( SchemaConnector schemaConnector )
    {
        this.schemaConnector = schemaConnector;
    }


    // ── Lando Checks Whether Two Cities Are The Same ─────────────────────────────
    // Two Cloud Cities are the same if they share the same name, type, and operating
    // state. Lando cross-checks all three before declaring a match.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if {@code obj} is a Project with the same name, type, and state
     * as this one. We use all three fields because two projects with the same name
     * but different types or states are meaningfully different.
     *
     * @param obj  the object to compare against
     * @return     true if the two projects are equivalent; false otherwise
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof Project )
        {
            Project project = ( Project ) obj;
            if ( !getName().equals( project.getName() ) )
            {
                return false;
            }
            else if ( !getType().equals( project.getType() ) )
            {
                return false;
            }
            else if ( !getState().equals( project.getState() ) )
            {
                return false;
            }

            return true;
        }

        // Default
        return super.equals( obj );
    }
}
