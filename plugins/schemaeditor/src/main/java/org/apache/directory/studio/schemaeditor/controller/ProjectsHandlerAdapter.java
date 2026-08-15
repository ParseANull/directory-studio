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


// ── CLASS: ProjectsHandlerAdapter — C-3PO TRANSLATING FOR R2-D2 ─────────────
// C-3PO is fluent in over six million forms of communication, so when R2-D2
// beeps something that nobody else understands, C-3PO steps in with a default
// response — even if it's just polite silence — so the conversation doesn't
// grind to a halt.  This adapter provides no-op default implementations of
// every ProjectsHandlerListener method so subclasses only need to override
// the one event they actually care about.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Convenience base class providing empty implementations of all ProjectsHandlerListener methods.
 * Subclass this and override only the event callbacks you actually need; the
 * rest silently do nothing rather than forcing you to implement them all.
 * Think of this class as C-3PO — always ready with a response, even if that
 * response is polite, empty silence.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsHandlerAdapter implements ProjectsHandlerListener
{
    // ── C-3PO Nods Politely At The New Arrival ───────────────────────────────
    // A new delegate arrives in the Rebel briefing room; C-3PO inclines his
    // head with practiced courtesy and says absolutely nothing — the default
    // diplomatic silence.  Subclasses that care about new projects override this.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op implementation called when a project is added.
     * Override this if you need to react to a new project appearing in the workspace.
     *
     * @param project  the newly added project
     */
    public void projectAdded( Project project )
    {
    }


    // ── C-3PO Nods Politely At The Departure ─────────────────────────────────
    // A delegate leaves the Rebel briefing room; C-3PO offers a courteous
    // farewell nod and returns to his default stance — nothing else to say.
    // Subclasses that care about removed projects override this.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op implementation called when a project is removed.
     * Override this if you need to clean up after a project disappears from the workspace.
     *
     * @param project  the project that was removed
     */
    public void projectRemoved( Project project )
    {
    }


    // ── C-3PO Registers The Change In Protocol ───────────────────────────────
    // The senior delegate changes; C-3PO updates his mental register of who is
    // currently in charge, then waits in polite silence unless spoken to.
    // Subclasses that need to react when the active project switches override this.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Default no-op implementation called when the open project changes.
     * Override this if your component needs to reload data or toggle its enabled
     * state in response to a project switch.
     *
     * @param oldProject  the project that was previously open (may be null)
     * @param newProject  the project that is now open (may be null)
     */
    public void openProjectChanged( Project oldProject, Project newProject )
    {
    }
}
