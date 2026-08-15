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


// ── CLASS: ProjectListener — OBI-WAN SENSES A DISTURBANCE ───────────────────
// Obi-Wan sits quietly aboard the Millennium Falcon and suddenly feels a
// tremor through the Force: "I felt a great disturbance... as if millions of
// voices suddenly cried out in terror."  He didn't have to see it — he just
// knew something had changed.  This interface lets objects register that same
// kind of passive awareness: they get notified the moment a project is renamed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for receiving project-level change events.
 * Any class that needs to react when a project is renamed should implement
 * this interface and register with the ProjectsHandler.
 * Think of this as Obi-Wan's Force sensitivity — implementors don't poll;
 * they simply feel the disturbance and act on it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ProjectListener
{
    // ── Obi-Wan Feels The Disturbance ────────────────────────────────────────
    // Obi-Wan's eyes flutter shut for a moment, then open: something has
    // changed in the Force.  He doesn't ask what — he simply responds.
    // This method fires the instant a project's name changes so that any
    // UI or logic that displays the name can update itself immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the project this listener is registered against has been renamed.
     * Implementors should refresh any UI element or cached state that displays
     * or depends on the project's name.
     *
     * <p>For example — Obi-Wan reacts the moment the disturbance is felt:</p>
     * <pre>
     *   public void projectRenamed() {
     *       titleLabel.setText( project.getName() );
     *   }
     * </pre>
     */
    public void projectRenamed();
}
