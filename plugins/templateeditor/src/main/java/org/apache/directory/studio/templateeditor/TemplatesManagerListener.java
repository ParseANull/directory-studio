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
package org.apache.directory.studio.templateeditor;


import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: TemplatesManagerListener — REBEL COMMAND RECEIVING BATTLE UPDATES ─────
// In the Rebel briefing room on Yavin 4, Mon Mothma and the commanders listen for
// real-time battlefield updates: ships added, ships lost, squadrons enabled or
// standing down. Each update triggers an immediate coordinated response.
// This interface plays the same role — any object that wants to know when templates
// are added, removed, enabled, or disabled registers here and gets called back live.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Observer contract for the {@link TemplatesManager} event bus.
 * Implement this interface and register with the manager to hear about every
 * lifecycle change to any template: added, removed, enabled, or disabled.
 * Think of this interface as the Rebel command channel — Mon Mothma broadcasts
 * the status change and every registered listener responds accordingly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface TemplatesManagerListener
{
    // ── TEMPLATE ADDED: NEW SHIP JOINS THE REBEL FLEET ──────────────────────────
    // A fresh X-Wing rolls off the transport and Mon Mothma announces its arrival
    // to the assembled commanders so they can update the battle roster.
    // The managers fires this so that anything displaying or depending on the
    // template list can react immediately — no polling required.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called right after a new template lands in the manager's registry.
     * Use this to refresh any UI that shows the full template list, or to do
     * any per-template initialization work your component needs.
     *
     * <p>For example — a new X-Wing joins the fleet:</p>
     * <pre>
     *   Mon Mothma: "Red Five has arrived."
     *   Commanders update their battle roster and assign the pilot.
     *   templateAdded(template) plays the same role in code.
     * </pre>
     *
     * @param template  the template that was just registered with the manager
     */
    void templateAdded( Template template );


    // ── TEMPLATE REMOVED: SHIP LOST FROM THE REBEL ROSTER ───────────────────────
    // A starfighter goes down and Mon Mothma notifies all commanders to strike it
    // from the active roster so no one wastes resources trying to coordinate with it.
    // This callback lets listeners clean up any references or UI rows tied to the
    // template before it disappears entirely.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called right after a template is removed from the manager's registry.
     * Clean up any UI rows, cached references, or listeners you attached to it.
     *
     * <p>For example — a starfighter is lost in battle:</p>
     * <pre>
     *   Mon Mothma: "Gold Leader is gone. Strike it from the roster."
     *   Commanders remove it from all active assignments.
     *   templateRemoved(template) does the same in code.
     * </pre>
     *
     * @param template  the template that was just unregistered
     */
    void templateRemoved( Template template );


    // ── TEMPLATE ENABLED: SQUADRON CLEARED FOR ACTIVE DUTY ──────────────────────
    // A squadron that was standing down gets the green light from command and
    // Mon Mothma broadcasts "Red Squadron, you're go for launch." The template
    // equivalent: a template previously disabled is now eligible to match entries.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a template transitions from disabled to enabled.
     * The template is now back in the active pool — it can be matched against
     * LDAP entries and displayed in the editor chooser.
     *
     * <p>For example — Red Squadron gets launch clearance:</p>
     * <pre>
     *   Mon Mothma: "Red Squadron, you're cleared for launch."
     *   Pilots scramble; the squadron re-enters the active roster.
     *   templateEnabled(template) mirrors this in the template lifecycle.
     * </pre>
     *
     * @param template  the template that just became enabled
     */
    void templateEnabled( Template template );


    // ── TEMPLATE DISABLED: SQUADRON STANDS DOWN FROM DUTY ───────────────────────
    // Mon Mothma orders a damaged squadron to stand down — they're still in the
    // hangar but off the active duty list until further notice. A disabled template
    // stays registered but the manager won't use it for entry matching.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a template transitions from enabled to disabled.
     * The template is still registered but will be skipped during entry matching
     * and will not appear in the template chooser for end users.
     *
     * <p>For example — a damaged squadron stands down:</p>
     * <pre>
     *   Mon Mothma: "Blue Squadron, stand down. You're off active rotation."
     *   They stay in the hangar but won't fly today.
     *   templateDisabled(template) is the code equivalent.
     * </pre>
     *
     * @param template  the template that just became disabled
     */
    void templateDisabled( Template template );
}
