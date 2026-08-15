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
import org.apache.directory.studio.schemaeditor.controller.ProjectListener;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.eclipse.jface.viewers.TableViewer;


// ── CLASS: ProjectWrapper — Vader's Suit Monitors Every Vital Sign ────────────
// Vader's armour does not merely cover — it watches. Sensors embedded throughout
// the suit track his life signs and relay status changes to the ship's systems the
// moment anything changes. If his condition shifts, the suit knows immediately
// and the bridge is updated.
// ProjectWrapper works exactly the same way: it wraps a {@link Project} object in
// a tree-node shell and installs a {@link ProjectListener} inside that shell.
// The moment the project is renamed, the listener fires and the TableViewer refreshes —
// no polling, no manual refresh, just immediate, reactive feedback from within the wrapper.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Adapts a {@link Project} so it can appear as a row in the Schema Editor's Projects View table.
 * In addition to wrapping the project for the JFace TableViewer, we install a
 * {@link ProjectListener} inside the constructor so that the table refreshes automatically
 * whenever the project is renamed.
 * A ProjectWrapper is always a leaf — it never has children (projects are not nested
 * under one another in this view), so {@link #hasChildren()} is overridden to return {@code false}.
 * Think of it as Vader's suit: the project lives inside, sensors watch for changes,
 * and the bridge display updates the moment something shifts.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectWrapper extends AbstractTreeNode
{
    /** The wrapped Project */
    private Project project;

    /** The TableViewer */
    private TableViewer viewer;


    // ── The Suit Seals Around Vader and Activates Its Sensors ────────────────
    // In the Coruscant medical bay, the armour closes around Anakin, the life-support
    // systems activate, and the suit's monitoring link to the Star Destroyer bridge
    // is established — all in one sequence before the doors even open.
    // Our constructor wraps the project, stores the viewer reference, and registers
    // a ProjectListener in one uninterrupted flow.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a wrapper around the given {@link Project} and installs a rename listener.
     * We register a {@link ProjectListener} with the {@link org.apache.directory.studio.schemaeditor.controller.ProjectsHandler}
     * so that when the project is renamed the TableViewer automatically refreshes its row,
     * showing the new name without any manual intervention from the caller.
     *
     * <p>For example — the suit activating its monitoring link the moment it seals around Vader:</p>
     * <pre>
     *   ProjectWrapper wrapper = new ProjectWrapper( myProject, tableViewer );
     *   // From this moment, if myProject is renamed, tableViewer refreshes automatically.
     * </pre>
     *
     * @param project      the {@link Project} to wrap and monitor; must not be {@code null}
     * @param tableViewer  the JFace TableViewer to refresh when the project is renamed
     */
    public ProjectWrapper( Project project, final TableViewer tableViewer )
    {
        super( null );
        this.project = project;
        this.viewer = tableViewer;

        Activator.getDefault().getProjectsHandler().addListener( project, new ProjectListener()
        {
            public void projectRenamed()
            {
                viewer.refresh();
            }
        } );
    }


    // ── The Suit Reports Vader's Current Status ───────────────────────────────
    // When the bridge officer asks "Lord Vader, status?" the suit's readout panel
    // gives back the raw stats — the actual Sith Lord inside, not the armour stats.
    // getProject() hands the caller the underlying {@link Project} object directly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Project} held inside this wrapper.
     * Callers that need to read the project name, access its schemas, or compare
     * project identities use this to get past the wrapper and reach the real model object.
     *
     * <p>For example — the bridge officer reading Vader's actual vital-sign data:</p>
     * <pre>
     *   Project p = wrapper.getProject();
     *   String name = p.getName();  // "MyDirectoryProject"
     * </pre>
     *
     * @return  the wrapped {@link Project}; never {@code null} if the wrapper was built correctly
     */
    public Project getProject()
    {
        return project;
    }


    // ── The Suit Has No Subordinate Components Beneath It ────────────────────
    // Vader's suit is a single unit — it doesn't branch into sub-suits or nested
    // armour sections. In the Projects View, projects are flat rows, not expandable
    // nodes. hasChildren() always returns false to suppress the expand arrow.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — project nodes are leaves in the Projects View table.
     * Projects are displayed as flat, non-expandable rows. Returning {@code false} prevents
     * the TableViewer from drawing an expand arrow and avoids any pointless getChildren() calls.
     *
     * <p>For example — the suit has no nested sub-components to expand into:</p>
     * <pre>
     *   wrapper.hasChildren();  // always false — projects are leaf rows
     * </pre>
     *
     * @return  {@code false}, always
     */
    public boolean hasChildren()
    {
        return false;
    }


    // ── Are Two Suits Wearing the Same Sith Lord? ─────────────────────────────
    // If two identical black suits appear on the bridge, the crew checks the
    // life-sign readout: same project inside? Only then do they confirm it is
    // the same "Vader" — the wrapper's identity is the identity of the project.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if {@code obj} is a {@link ProjectWrapper} that wraps the same
     * {@link Project} as this one.
     * We do not compare parents here because ProjectWrapper always has a {@code null} parent
     * (it sits directly under a ProjectsViewRoot, but that link is tracked there, not here).
     *
     * <p>For example — the bridge crew confirming two suits contain the same Sith Lord:</p>
     * <pre>
     *   wrapper1.equals( wrapper2 );
     *   // true only if both wrap the same Project instance (by Project.equals())
     * </pre>
     *
     * @param obj  the object to compare against
     * @return     {@code true} if the wrappers hold the same project
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ProjectWrapper )
        {
            ProjectWrapper projectWrapper = ( ProjectWrapper ) obj;

            if ( ( project != null ) && ( !project.equals( projectWrapper.getProject() ) ) )
            {
                return false;
            }

            return true;
        }

        return false;
    }


    // ── The Suit's Unique Life-Sign Signature ─────────────────────────────────
    // Every Imperial officer's life-sign beacon transmits a unique hash signature —
    // the combination of their identity and their current assignment on the ship.
    // hashCode() gives each wrapper a numeric fingerprint driven by the wrapped project.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Computes a hash code that combines the parent's hash (from {@code super.hashCode()})
     * with the wrapped {@link Project}'s own hash code.
     * Consistent with {@link #equals}: equal wrappers produce the same hash.
     *
     * <p>For example — the unique life-sign signature broadcast from Vader's suit:</p>
     * <pre>
     *   int hash = wrapper.hashCode();
     *   // 37 * parentHash + project.hashCode()
     * </pre>
     *
     * @return  a hash code consistent with {@link #equals}
     */
    public int hashCode()
    {
        int result = super.hashCode();

        if ( project != null )
        {
            result = 37 * result + project.hashCode();
        }

        return result;
    }
}
