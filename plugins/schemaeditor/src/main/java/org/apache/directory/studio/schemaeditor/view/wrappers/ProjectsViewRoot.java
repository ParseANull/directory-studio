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
package org.apache.directory.studio.schemaeditor.view.wrappers;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandlerListener;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;


// ── CLASS: ProjectsViewRoot — Lando Running Cloud City ───────────────────────
// On Bespin, Lando Calrissian runs Cloud City like a living system: ships arrive
// and dock (projects are added), ships depart (projects are removed), the
// "open contract" changes as new deals come in (the active project changes).
// Lando does not stand at the dock himself — he monitors the comms and dispatches
// handlers to update the city's registry whenever something changes.
// ProjectsViewRoot works the same way: it is the invisible root of the Projects
// View table, and it listens to the {@link ProjectsHandler} so that whenever a
// project is created or deleted, it immediately updates the table without the
// user having to do anything.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Serves as the invisible root node of the Schema Editor's Projects View and manages
 * the live synchronisation between the {@link ProjectsHandler} and the TableViewer.
 * When the user creates or deletes a schema project, the {@link ProjectsHandler} fires
 * an event. We catch it here, add or remove the corresponding {@link ProjectWrapper}
 * from our children list, and trigger a viewer refresh — all on the UI thread.
 * Think of Lando's operations centre: ships come and go, and Cloud City's registry
 * updates automatically so the view is always current.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsViewRoot extends AbstractTreeNode
{
    /** The TableViewer */
    private TableViewer viewer;

    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;


    // ── Lando Opens the Operations Centre and Hooks Up the Comms ─────────────
    // Before any ships can dock, Lando walks into the operations centre, fires up
    // the comm array, and registers his handlers: "Tell me when a ship arrives.
    // Tell me when one leaves. Tell me if the main contract changes."
    // Our constructor does the same: grab the handler, register a listener for all three events.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Projects View root node and wires it to the live {@link ProjectsHandler}.
     * We register an anonymous {@link ProjectsHandlerListener} that fires addProjectWrapper()
     * or deleteProjectWrapper() whenever a project is created or removed, then refreshes
     * the TableViewer on the UI thread via {@link Display#asyncExec}.
     *
     * <p>For example — Lando setting up the dock registry when Cloud City's comms come online:</p>
     * <pre>
     *   ProjectsViewRoot root = new ProjectsViewRoot( tableViewer );
     *   // From this point on, the table auto-updates when projects are added or removed.
     * </pre>
     *
     * @param tableViewer  the JFace TableViewer that shows the list of projects; must not be {@code null}
     */
    public ProjectsViewRoot( TableViewer tableViewer )
    {
        super( null );
        this.viewer = tableViewer;

        projectsHandler = Activator.getDefault().getProjectsHandler();
        projectsHandler.addListener( new ProjectsHandlerListener()
        {
            public void projectAdded( Project project )
            {
                addProjectWrapper( project );
                refreshProjectsViewer();
            }


            public void projectRemoved( Project project )
            {
                deleteProjectWrapper( project );
                refreshProjectsViewer();
            }


            public void openProjectChanged( Project oldProject, Project newProject )
            {
                refreshProjectsViewer();
            }
        } );
    }


    // ── A New Ship Docks — Lando Logs It in the Registry ─────────────────────
    // The docking bay signals a new arrival — Lando's team immediately creates
    // a docking record for the ship and adds it to Cloud City's manifest.
    // addProjectWrapper() wraps the new Project and attaches it to our children list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link ProjectWrapper} for the given project and adds it as a child of this root.
     * Called by the listener when the {@link ProjectsHandler} fires a projectAdded event.
     * The viewer is refreshed separately by the caller immediately after this returns.
     *
     * <p>For example — logging a newly arrived ship in Cloud City's docking manifest:</p>
     * <pre>
     *   addProjectWrapper( newProject );
     *   // A ProjectWrapper is now in our children list, ready to appear in the table.
     * </pre>
     *
     * @param project  the newly created project to wrap and add; must not be {@code null}
     */
    private void addProjectWrapper( Project project )
    {
        addChild( new ProjectWrapper( project, viewer ) );
    }


    // ── A Ship Departs — Lando Removes It from the Manifest ──────────────────
    // When a ship undocks and leaves Cloud City, Lando's team finds its record
    // in the docking manifest and strikes it out — no leftover ghost entries.
    // deleteProjectWrapper() finds the right ProjectWrapper by identity and removes it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and removes the {@link ProjectWrapper} that corresponds to the given project.
     * We scan our children list by project identity ({@code ==}) rather than equals()
     * to guarantee we remove the exact wrapper for this project, not a similarly named one.
     * Returns as soon as the matching wrapper is found and removed.
     *
     * <p>For example — striking a departed ship from Cloud City's docking manifest:</p>
     * <pre>
     *   deleteProjectWrapper( removedProject );
     *   // The matching ProjectWrapper is gone from our children list.
     * </pre>
     *
     * @param project  the project whose wrapper should be removed; must not be {@code null}
     */
    private void deleteProjectWrapper( Project project )
    {
        for ( TreeNode node : getChildren() )
        {
            ProjectWrapper pw = ( ProjectWrapper ) node;
            if ( project == pw.getProject() )
            {
                removeChild( node );
                return;
            }
        }
    }


    // ── Lando Broadcasts the Updated Manifest Across the City ─────────────────
    // After a ship arrives or departs, Lando's team pushes the updated docking list
    // to every terminal in Cloud City — asynchronously, so the operations centre
    // does not block waiting for every screen to update.
    // refreshProjectsViewer() schedules a viewer.refresh() on the SWT UI thread.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Schedules a refresh of the Projects View TableViewer on the SWT UI thread.
     * We use {@link Display#asyncExec} because this method can be called from a
     * non-UI thread (the event listener fires on whatever thread the handler uses).
     * The viewer will repaint its rows from our children list on the next UI pass.
     *
     * <p>For example — Lando pushing the updated ship manifest to all terminals in Cloud City:</p>
     * <pre>
     *   refreshProjectsViewer();
     *   // viewer.refresh() is scheduled on the UI thread — table updates shortly.
     * </pre>
     */
    public void refreshProjectsViewer()
    {
        Display.getDefault().asyncExec( new Runnable()
        {
            public void run()
            {
                viewer.refresh();
            }
        } );
    }


    // ── Is This the Same Operations Centre? ───────────────────────────────────
    // Cloud City has only one operations centre. If someone asks "is this the ops
    // centre?", the only valid answer is physical identity — are we talking about
    // the same room? equals() uses reference equality for the same reason.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} only if {@code obj} is literally the same object as this root.
     * We use reference equality ({@code ==}) because each Projects View instance has
     * exactly one root — two different roots represent two different view contexts.
     *
     * <p>For example — Lando confirming there's only one operations centre on Bespin:</p>
     * <pre>
     *   root.equals( root );   // true — same object
     *   root.equals( other );  // false — even if other is also a ProjectsViewRoot
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if and only if {@code obj == this}
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ProjectsViewRoot )
        {
            return this == obj;
        }

        return false;
    }
}
