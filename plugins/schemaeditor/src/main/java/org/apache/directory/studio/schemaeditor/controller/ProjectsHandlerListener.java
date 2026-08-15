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


import org.apache.directory.studio.schemaeditor.model.Project;


// ── CLASS: ProjectsHandlerListener — OBI-WAN SENSES A DISTURBANCE ───────────
// Obi-Wan stands on the bridge of a star cruiser and suddenly feels the Force
// ripple: a new presence arrives, another departs, the balance shifts entirely.
// This interface gives implementors that same Force-sensitivity for the
// ProjectsHandler: they're called whenever a project is added, removed, or
// the active (open) project changes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for receiving events from the ProjectsHandler.
 * Implementors are notified when projects are added or removed from the
 * workspace, and when the currently-open project changes.
 * Think of this as Obi-Wan's Force awareness — you register once and the
 * disturbances come to you, no polling required.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ProjectsHandlerListener
{
    // ── Obi-Wan Senses A New Presence ────────────────────────────────────────
    // A new ship drops out of hyperspace and Obi-Wan feels it arrive — his
    // senses sharpen, he turns toward it.  This method fires the instant a
    // new project lands in the ProjectsHandler so listeners can update their
    // views or lists to show the new arrival.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a project has been added to the ProjectsHandler.
     * Implementors should refresh any list or tree that shows available projects.
     *
     * <p>For example — Obi-Wan acknowledges the new arrival:</p>
     * <pre>
     *   public void projectAdded( Project project ) {
     *       viewer.refresh();
     *   }
     * </pre>
     *
     * @param project  the newly added project
     */
    void projectAdded( Project project );


    // ── Obi-Wan Feels A Presence Vanish ──────────────────────────────────────
    // Alderaan's billions vanish in an instant — Obi-Wan winces as the Force
    // goes suddenly quiet where there was once so much life.
    // This method fires when a project is removed so listeners can clean up
    // any references or UI elements tied to that project.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a project has been removed from the ProjectsHandler.
     * Implementors should remove any UI representation of this project and
     * drop any cached references they hold to it.
     *
     * <p>For example — Obi-Wan's world goes quiet:</p>
     * <pre>
     *   public void projectRemoved( Project project ) {
     *       viewer.refresh();
     *   }
     * </pre>
     *
     * @param project  the project that was removed
     */
    void projectRemoved( Project project );


    // ── The Balance Of The Force Shifts ──────────────────────────────────────
    // Obi-Wan feels the whole center of gravity in the Force tilt — the old
    // power fades, a new one rises to take its place.  The active project
    // has switched: one is closed, another is now open.
    // Listeners need to know both the before and after so they can transition
    // their state cleanly rather than guessing what changed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the currently-open project changes.
     * Either {@code oldProject} or {@code newProject} may be null — null for
     * oldProject means nothing was open before; null for newProject means
     * the user just closed the last project.
     *
     * <p>For example — Obi-Wan tracks the shift from old power to new:</p>
     * <pre>
     *   public void openProjectChanged( Project oldProject, Project newProject ) {
     *       if ( newProject != null ) { loadSchemas( newProject ); }
     *       else                      { clearView(); }
     *   }
     * </pre>
     *
     * @param oldProject  the project that was open before (may be null)
     * @param newProject  the project that is now open (may be null)
     */
    void openProjectChanged( Project oldProject, Project newProject );
}
