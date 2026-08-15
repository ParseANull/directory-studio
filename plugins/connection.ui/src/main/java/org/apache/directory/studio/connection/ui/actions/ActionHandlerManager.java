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
package org.apache.directory.studio.connection.ui.actions;


// ── INTERFACE: ActionHandlerManager — THE FALCON'S WEAPONS SAFETY SWITCH ──────────
// In the middle of a firefight, Han sometimes needs to lock and unlock the weapons
// array — lock them so the controls don't interfere with navigation, unlock them
// when it's time to shoot.  ActionHandlerManager is that safety switch for Eclipse
// global action handlers (the key bindings and toolbar contributions that respond
// to Delete, Copy, Paste, Properties, etc.).
// When a view-specific action is about to run (e.g., Delete a connection), the
// Connections view's global handlers need to be temporarily deactivated so they
// don't conflict with the IDE's global handlers.  After the action finishes, they
// are reactivated.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Contract for activating and deactivating the Eclipse global action handlers
 * registered by a view (e.g., the Connections view).
 *
 * <p>Global action handlers (Copy, Paste, Delete, Properties, …) can conflict
 * with one another if multiple views register handlers for the same command.
 * Implementations of this interface are called by {@link StudioActionProxy} before
 * and after executing a {@link StudioAction} to prevent such conflicts.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ActionHandlerManager
{

    // ── DEACTIVATE — WEAPONS SAFETY ON ────────────────────────────────────────────
    /**
     * Deactivates (unregisters) the global action handlers that this view contributes,
     * so they do not conflict with the currently running action.
     */
    void deactivateGlobalActionHandlers();


    // ── ACTIVATE — WEAPONS SAFETY OFF ─────────────────────────────────────────────
    /**
     * Reactivates (re-registers) the global action handlers after an action has
     * finished executing.
     */
    void activateGlobalActionHandlers();

}
