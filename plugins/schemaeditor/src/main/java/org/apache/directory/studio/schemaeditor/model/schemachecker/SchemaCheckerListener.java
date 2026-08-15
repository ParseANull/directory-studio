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
package org.apache.directory.studio.schemaeditor.model.schemachecker;


// ── INTERFACE: SchemaCheckerListener — Obi-Wan Sensing a Disturbance ─────────
// Obi-Wan Kenobi doesn't have to be in the room when something changes in the
// Force; he feels it wherever he is and responds.  Classes that implement this
// interface are registered with the SchemaChecker and notified — like Obi-Wan
// sensing the disturbance — the moment the checker finishes a full re-validation
// pass.  The Problems view, the status bar, and any other UI component that
// shows schema health registers here so it can refresh itself in response.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for components that need to react when the
 * {@link SchemaChecker} finishes a re-validation pass.
 * Implementations register via {@link SchemaChecker#addListener} and are
 * notified on the Job thread that runs the check — implementors must dispatch
 * UI work to the SWT display thread themselves.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface SchemaCheckerListener
{
    // ── Obi-Wan Feels the Disturbance ─────────────────────────────────────────
    // Called by SchemaChecker after each complete re-validation pass — whether
    // triggered by an attribute type add, remove, modify, or a schema reload.
    // The listener must not assume which thread calls this; marshal UI work to
    // the SWT display thread explicitly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the {@link SchemaChecker} has finished a complete re-validation
     * of the schema and its error/warning maps have been refreshed.
     * Implementations should call {@link SchemaChecker#getErrors()} and
     * {@link SchemaChecker#getWarnings()} to get the updated state.
     */
    void schemaCheckerUpdated();
}
