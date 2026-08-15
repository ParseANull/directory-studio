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
package org.apache.directory.studio.schemaeditor.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Project.ProjectState;


// ── CLASS: ProjectsHandler — LANDO RUNNING CLOUD CITY ───────────────────────
// Lando Calrissian presides over Cloud City: he knows every resident by name,
// tracks which platform is open for business and which is shut down, mediates
// disputes between departments, and broadcasts city-wide announcements the
// moment anything changes.  This class is exactly that: it keeps track of all
// schema projects, which one is currently "open" (active), and notifies every
// registered listener whenever something changes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central registry and manager for all schema editor projects.
 * We maintain the list of known projects, a fast-lookup map by name, and
 * the concept of exactly one "open" (active) project at a time.
 * Think of this class as Lando — we run the whole operation, dispatch
 * announcements to listeners, and keep every platform synchronized.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsHandler
{
    /** The ProjectsHandler instance */
    private static ProjectsHandler instance;

    /** The projects List */
    private List<Project> projectsList;

    /** The projects Map */
    private Map<String, Project> projectsMap;

    /** The ProjectsHandler listeners */
    private List<ProjectsHandlerListener> projectsHandlerListeners;

    /** The projects listeners */
    private MultiValuedMap<Project, ProjectListener> projectsListeners;

    /** The open project */
    private Project openProject;


    // ── Lando Gets The Singleton Report ──────────────────────────────────────
    // When Han needs to talk to the administrator of Cloud City there's only
    // one person for the job — Lando.  One city, one administrator, one instance.
    // We lazy-create and return the single global ProjectsHandler so every
    // part of the plugin shares the same registry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton instance of the ProjectsHandler, creating it if needed.
     * We use lazy initialization because the handler isn't needed until the plugin
     * actually loads a project.
     *
     * <p>For example — there's only one Lando running Cloud City:</p>
     * <pre>
     *   ProjectsHandler handler = ProjectsHandler.getInstance();
     *   // Same object every time; safe to call from anywhere in the plugin
     * </pre>
     *
     * @return  the singleton ProjectsHandler instance
     */
    public static ProjectsHandler getInstance()
    {
        if ( instance == null )
        {
            instance = new ProjectsHandler();
        }

        return instance;
    }


    // ── Lando Opens The City For Business ────────────────────────────────────
    // Before Cloud City opens its landing pads, someone has to initialize the
    // docking registry, the resident roster, and the inter-department comm lines.
    // We set up empty lists and maps for projects and their listeners so the
    // handler is ready to receive its first project without throwing NPEs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — creates the empty data structures for this handler.
     * Only called once by {@link #getInstance()}; use that method to get a reference.
     *
     * <p>For example — Lando doesn't open two copies of the city ledger:</p>
     * <pre>
     *   private ProjectsHandler() {
     *       projectsList = new ArrayList<>();
     *       projectsMap  = new HashMap<>();
     *       // ... listeners ready to receive
     *   }
     * </pre>
     */
    private ProjectsHandler()
    {
        projectsList = new ArrayList<Project>();
        projectsMap = new HashMap<String, Project>();
        projectsHandlerListeners = new ArrayList<ProjectsHandlerListener>();
        projectsListeners = new ArrayListValuedHashMap();
    }


    // ── A New Ship Docks At Cloud City ────────────────────────────────────────
    // Lando waves a new freighter into bay seven, logs it in the docking
    // registry under its transponder ID, and broadcasts the arrival on the
    // city-wide comm.  We add the project to both our list and name map, then
    // notify all listeners so the UI can show the new entry immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a project to the handler and notifies all registered listeners.
     * We store it in both the ordered list (for display) and the name map
     * (for fast lookup), then fire {@code projectAdded} on every listener.
     *
     * <p>For example — Lando logs the Millennium Falcon into the docking registry:</p>
     * <pre>
     *   handler.addProject( myProject );
     *   // Every ProjectsHandlerListener.projectAdded() fires immediately
     * </pre>
     *
     * @param project  the project to add; must not be null
     */
    public void addProject( Project project )
    {
        projectsList.add( project );
        projectsMap.put( Strings.toLowerCase( project.getName() ), project );

        notifyProjectAdded( project );
    }


    // ── A Ship Departs Cloud City ─────────────────────────────────────────────
    // Lando removes the departing freighter from the docking registry and sends
    // a city-wide notice that bay seven is now vacant.
    // We remove the project from list and map, then fire the removal notification
    // so every listener can clean up any reference it holds to this project.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a project from the handler and notifies all registered listeners.
     * The project is removed from both the list and the name map, then
     * {@code projectRemoved} fires on every listener.
     *
     * <p>For example — Lando clears the Falcon from the registry when Han leaves:</p>
     * <pre>
     *   handler.removeProject( myProject );
     *   // Every ProjectsHandlerListener.projectRemoved() fires immediately
     * </pre>
     *
     * @param project  the project to remove; must already be registered
     */
    public void removeProject( Project project )
    {
        projectsList.remove( project );
        projectsMap.remove( Strings.toLowerCase( project.getName() ) );

        notifyProjectRemoved( project );
    }


    // ── Lando Checks The Registry By Name ────────────────────────────────────
    // A visitor asks for the docking record of the "Outrider" — Lando flips
    // to the right page in the registry and reads it back.
    // We look up by lower-cased name so the lookup is case-insensitive, just
    // like the LDAP world that these project names come from.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the project with the given name, or null if none is registered.
     * The lookup is case-insensitive — "MyProject" and "myproject" resolve to the same entry.
     *
     * <p>For example — Lando looks up "Outrider" regardless of how it was typed:</p>
     * <pre>
     *   Project p = handler.getProject( "myproject" );
     *   // Returns the project even if it was added as "MyProject"
     * </pre>
     *
     * @param name  the project name to look up
     * @return      the matching project, or null if not found
     */
    public Project getProject( String name )
    {
        return projectsMap.get( Strings.toLowerCase( name ) );
    }


    // ── Lando Reads Out The Full Docking Manifest ────────────────────────────
    // The city administrator reads every entry in the docking log, top to bottom,
    // so the Imperial officers can see everything at once.
    // We return the full list of registered projects for the Projects View to
    // display — the list is ordered by insertion so the user sees them consistently.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the full list of all registered projects in insertion order.
     * The caller gets a direct reference to our internal list — don't modify it externally.
     *
     * <p>For example — Lando hands over the complete docking manifest:</p>
     * <pre>
     *   List&lt;Project&gt; all = handler.getProjects();
     *   // Iterate to populate the Projects View table
     * </pre>
     *
     * @return  the live list of all projects; never null, may be empty
     */
    public List<Project> getProjects()
    {
        return projectsList;
    }


    // ── Lando Updates The Registry After A Name Change ───────────────────────
    // A ship changes its transponder callsign mid-stay; Lando removes the old
    // entry from the docking book, updates the ship's record, and writes the
    // new callsign in — then broadcasts the change so everyone is up to date.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Renames a project and notifies the project's own listeners of the change.
     * We update both the map key (old name out, new name in) and the project
     * object itself, then fire {@code projectRenamed} on listeners specific
     * to this project.
     *
     * <p>For example — the Falcon gets a new transponder ID, Lando updates the book:</p>
     * <pre>
     *   handler.renameProject( project, "NewName" );
     *   // project.getName() == "NewName" after this call
     * </pre>
     *
     * @param project  the project to rename; must be registered
     * @param name     the new name for the project
     */
    public void renameProject( Project project, String name )
    {
        projectsMap.remove( Strings.toLowerCase( project.getName() ) );
        project.setName( name );
        projectsMap.put( Strings.toLowerCase( name ), project );

        notifyProjectRenamed( project );
    }


    // ── Lando Checks Whether A Bay Is Already Reserved ───────────────────────
    // Before handing out a new docking bay assignment, Lando checks the registry
    // to make sure that callsign isn't already in use — two ships with the same
    // ID would be chaos.  We do the same: no two projects can share a name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether a project with the given name already exists.
     * Case-insensitive — "Foo" and "foo" count as the same name.
     * Use this before creating a new project to avoid silently overwriting one.
     *
     * <p>For example — Lando refuses to assign the same bay number twice:</p>
     * <pre>
     *   if ( handler.isProjectNameAlreadyTaken( "myproject" ) ) {
     *       // show "name already in use" error to user
     *   }
     * </pre>
     *
     * @param name  the candidate project name
     * @return      true if a project with that name (case-insensitive) already exists
     */
    public boolean isProjectNameAlreadyTaken( String name )
    {
        return projectsMap.containsKey( Strings.toLowerCase( name ) );
    }


    // ── Lando Opens The Main Trading Platform ────────────────────────────────
    // Lando declares one platform the active trading hub — all commerce flows
    // through it, the old hub is closed, and a city-wide broadcast goes out.
    // We mark the previously-open project as CLOSED, mark the new one OPEN,
    // and fire the change event so every listener can react.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Makes the given project the active (open) project, closing any previously open one.
     * We update the state on both the old and new project objects, then fire
     * {@code openProjectChanged} so every listener can reload its data.
     *
     * <p>For example — Lando redirects all traffic to the new platform:</p>
     * <pre>
     *   handler.openProject( myProject );
     *   // oldProject.getState() == CLOSED
     *   // myProject.getState()  == OPEN
     * </pre>
     *
     * @param project  the project to make active; must be registered
     */
    public void openProject( Project project )
    {
        Project oldOpenProject = openProject;
        if ( oldOpenProject != null )
        {
            oldOpenProject.setState( ProjectState.CLOSED );
        }

        openProject = project;
        openProject.setState( ProjectState.OPEN );

        notifyOpenProjectChanged( oldOpenProject, openProject );
    }


    // ── Lando Shuts Down A Platform ───────────────────────────────────────────
    // When Cloud City needs to mothball a platform, Lando locks it down and
    // broadcasts that there's no longer an active hub — traffic must wait.
    // We close the project, clear the openProject reference, and fire the
    // change event with null as the new project.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Closes the given project so that no project is currently active.
     * If the given project is not the currently-open one we do nothing.
     * After closing, {@code openProjectChanged(oldProject, null)} fires on
     * all listeners.
     *
     * <p>For example — Lando mothballs the platform, nothing is open for business:</p>
     * <pre>
     *   handler.closeProject( activeProject );
     *   // handler.getOpenProject() == null
     * </pre>
     *
     * @param project  the project to close; must be the currently-open project
     */
    public void closeProject( Project project )
    {
        Project oldOpenProject = openProject;
        if ( oldOpenProject.equals( project ) )
        {
            oldOpenProject.setState( ProjectState.CLOSED );
            openProject = null;
        }

        notifyOpenProjectChanged( oldOpenProject, openProject );
    }


    // ── Lando Reports Which Platform Is Live ─────────────────────────────────
    // When Han asks "which bay is actually open right now?" Lando points to
    // the active platform without hesitation.
    // We return the currently-open project so callers can read its schemas,
    // check its state, or display its name in the title bar.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently-open (active) project, or null if no project is open.
     * Most of the plugin's behavior is gated on this — if null, views should
     * be disabled and editors should refuse to load.
     *
     * <p>For example — Han needs to know which bay to fly to:</p>
     * <pre>
     *   Project active = handler.getOpenProject();
     *   if ( active == null ) { disableAllViews(); }
     * </pre>
     *
     * @return  the open project, or null
     */
    public Project getOpenProject()
    {
        return openProject;
    }


    // ── Lando Updates His Private Records ────────────────────────────────────
    // Behind closed doors, Lando scribbles a note updating the private ledger
    // about which platform is actually the active one — no broadcast, no fuss.
    // This is the internal setter used during deserialization/loading; it does
    // NOT fire change events, so use openProject() for normal runtime switching.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Directly sets the open project without firing any change notifications.
     * Use this only during project loading/initialization, not for normal
     * project switching — call {@link #openProject(Project)} for that so
     * listeners are properly notified.
     *
     * <p>For example — Lando quietly updates the private ledger:</p>
     * <pre>
     *   handler.setOpenProject( restoredProject ); // no events fired
     * </pre>
     *
     * @param project  the project to set as open; may be null
     */
    public void setOpenProject( Project project )
    {
        openProject = project;
    }


    // ── Lando Adds A City-Wide Comm Subscriber ───────────────────────────────
    // A department head tunes their comm unit to the city-wide broadcast
    // frequency so they hear every future announcement.
    // We add the listener to our list so it receives project-level events
    // (added, removed, open-changed) from this point on.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers a ProjectsHandlerListener to receive future project events.
     * Duplicate registrations are allowed but will cause duplicate callbacks —
     * be sure to call {@link #removeListener(ProjectsHandlerListener)} on dispose.
     *
     * <p>For example — a department head tunes in to the city-wide channel:</p>
     * <pre>
     *   handler.addListener( myListener );
     * </pre>
     *
     * @param listener  the listener to register; must not be null
     */
    public void addListener( ProjectsHandlerListener listener )
    {
        projectsHandlerListeners.add( listener );
    }


    // ── Lando Removes A Comm Subscriber ──────────────────────────────────────
    // A department head tunes their comm unit off the city-wide frequency —
    // they'll no longer be disturbed by announcements they don't need.
    // We remove the listener so it stops receiving future events.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters a previously added ProjectsHandlerListener.
     * Always call this when the listening component is disposed to avoid
     * firing callbacks into a garbage-collected or closed part.
     *
     * <p>For example — the department head tunes out at end of shift:</p>
     * <pre>
     *   handler.removeListener( myListener );
     * </pre>
     *
     * @param listener  the listener to remove; no-op if not registered
     */
    public void removeListener( ProjectsHandlerListener listener )
    {
        projectsHandlerListeners.remove( listener );
    }


    // ── Lando Adds A Per-Platform Comm Subscriber ────────────────────────────
    // A docking crew chief registers to hear announcements specific to bay seven
    // only — renaming of that particular ship's record, for instance.
    // We add a ProjectListener for a specific project, guarding against duplicates,
    // so the listener hears rename events for that project.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers a ProjectListener for rename events on a specific project.
     * This is a finer-grained subscription than ProjectsHandlerListener —
     * the callback fires only for rename events on the given project.
     * We silently drop duplicate registrations of the same listener+project pair.
     *
     * <p>For example — a crew chief listens only for changes to bay seven:</p>
     * <pre>
     *   handler.addListener( project, crewChiefListener );
     * </pre>
     *
     * @param project   the project to listen to
     * @param listener  the listener to register
     */
    public void addListener( Project project, ProjectListener listener )
    {
        if ( !projectsListeners.containsMapping( project, listener ) )
        {
            projectsListeners.put( project, listener );
        }
    }


    // ── Lando Removes A Per-Platform Comm Subscriber ─────────────────────────
    // The crew chief at bay seven signs off and removes their comm unit from
    // the bay-specific channel — no more rename announcements for them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters a ProjectListener from a specific project.
     * Call this when the listening component closes or no longer needs rename
     * events for this particular project.
     *
     * <p>For example — the crew chief at bay seven clocks out:</p>
     * <pre>
     *   handler.removeListener( project, crewChiefListener );
     * </pre>
     *
     * @param project   the project to stop listening to
     * @param listener  the listener to remove
     */
    public void removeListener( Project project, ProjectListener listener )
    {
        projectsListeners.removeMapping( project, listener );
    }


    // ── Lando Broadcasts "New Ship In Bay" ────────────────────────────────────
    // Lando keys the city-wide comm: "Attention: a new ship has docked in bay
    // seven."  Every department head on the frequency hears it and updates
    // their boards.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires {@code projectAdded} on all registered ProjectsHandlerListeners.
     * Called internally after a project is added to the registry.
     *
     * @param project  the project that was added
     */
    private void notifyProjectAdded( Project project )
    {
        for ( ProjectsHandlerListener listener : projectsHandlerListeners )
        {
            listener.projectAdded( project );
        }
    }


    // ── Lando Broadcasts "Ship Has Left The City" ─────────────────────────────
    // Lando keys the comm again: "Attention: bay seven is now vacant."
    // Every subscriber updates their boards to remove the departed ship.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires {@code projectRemoved} on all registered ProjectsHandlerListeners.
     * Called internally after a project is removed from the registry.
     *
     * @param project  the project that was removed
     */
    private void notifyProjectRemoved( Project project )
    {
        for ( ProjectsHandlerListener listener : projectsHandlerListeners )
        {
            listener.projectRemoved( project );
        }
    }


    // ── Lando Informs The Bay Crew Of The New Callsign ───────────────────────
    // Lando radios bay seven's crew directly: "That ship has a new callsign —
    // update your logs."  Only the crew watching that specific bay hears this.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires {@code projectRenamed} on all ProjectListeners registered for the given project.
     * These are the fine-grained, per-project listeners — not the global handler listeners.
     *
     * @param project  the project that was renamed
     */
    @SuppressWarnings("unchecked")
    private void notifyProjectRenamed( Project project )
    {
        List<ProjectListener> listeners = ( List<ProjectListener> ) projectsListeners.get( project );
        for ( ProjectListener listener : listeners )
        {
            listener.projectRenamed();
        }
    }


    // ── Lando Broadcasts The Platform Switch ─────────────────────────────────
    // Lando keys the city-wide comm one more time: "Attention all departments —
    // the active trading platform has changed."  Every listener hears both
    // which platform closed and which is now open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires {@code openProjectChanged} on all registered ProjectsHandlerListeners.
     * Called internally whenever the active project changes (open or close).
     *
     * @param oldProject  the project that was previously open (may be null)
     * @param newProject  the project that is now open (may be null)
     */
    private void notifyOpenProjectChanged( Project oldProject, Project newProject )
    {
        for ( ProjectsHandlerListener listener : projectsHandlerListeners )
        {
            listener.openProjectChanged( oldProject, newProject );
        }
    }
}
