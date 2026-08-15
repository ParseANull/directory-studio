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
package org.apache.directory.studio.ldapbrowser.common.actions.proxy;


// ── CLASS: ActionHandlerManager — LANDO RUNS CLOUD CITY'S CONTROL CENTER ─────
// Lando Calrissian runs Cloud City like a seasoned administrator: each department
// has its own team, but Lando decides which department handles what and whether
// they're currently "on" or "off."  When the Imperials arrive, he can deactivate
// certain functions across the whole city with a single command.  When they leave
// (or when he joins the Rebellion), he reactivates them.  That's this interface:
// any component that manages a set of global Eclipse action handlers can
// implement it, and then Eclipse's part lifecycle can call activate/deactivate
// at the right moments — like Lando switching the city's systems on and off.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Contract for components that manage global Eclipse action handlers.
 *
 * <p>In Eclipse, "global action handlers" are the mechanism that lets views
 * and editors wire their own implementations into the workbench's standard
 * command slots (Copy, Paste, Delete, etc.).  When a view gains focus, it
 * activates its handlers; when it loses focus, it deactivates them so the
 * next focused part can take over.</p>
 *
 * <p>Any class that manages a set of these proxy actions — typically a view or
 * editor part — implements this interface so the lifecycle callbacks can
 * activate and deactivate cleanly.</p>
 *
 * <p>Think of this interface as Lando's authority over Cloud City's control
 * center: he can route power to the right department (activate) or shut it
 * down (deactivate) with a single command.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ActionHandlerManager
{
    // ── Lando Shuts Down Non-Essential Systems ────────────────────────────────
    // When the Imperials land and Lando needs to play it cool, he powers down
    // certain city functions — they're still there, just not responding.
    // We do the same: unregister our global action handlers so the workbench
    // command slots are freed up for whoever is now in focus.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Deactivates all global action handlers managed by this component.
     *
     * <p>Call this when the owning view or editor loses focus, or when it is
     * about to be closed.  After this call, the workbench's standard command
     * slots (e.g. Copy, Paste) will no longer be handled by our proxy actions.</p>
     *
     * <p>For example — Lando powers down Cloud City's comms array:</p>
     * <pre>
     *   Lando flips the master switch — all department links go dark.
     *   The Imperials can no longer issue orders through Cloud City's system.
     *   Our handlers are unregistered; the workbench looks elsewhere.
     * </pre>
     */
    void deactivateGlobalActionHandlers();


    // ── Lando Powers the City Back Up ────────────────────────────────────────
    // Once the Imperials are dealt with, Lando throws the switch and Cloud City
    // springs back to life — every department online, every channel open.
    // We reactivate our global action handlers so our view owns the command
    // slots again while it has focus.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Activates all global action handlers managed by this component.
     *
     * <p>Call this when the owning view or editor gains focus.  After this
     * call, the workbench's standard command slots route through our proxy
     * actions, which in turn delegate to the real {@link
     * org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction}
     * implementations.</p>
     *
     * <p>For example — Lando reopens Cloud City's systems after the Imperials
     * leave:</p>
     * <pre>
     *   Lando throws the master switch — all departments come back online.
     *   Every channel is live; orders route through the control center again.
     *   Our handlers are registered; the workbench routes commands to us.
     * </pre>
     */
    void activateGlobalActionHandlers();
}
