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
package org.apache.directory.studio.ldapservers.model;


// ── CLASS: LdapServerAdapterConfigurationPageModifyListener — THE ENGINEER WATCHING FOR CHANGES ─
// An Imperial engineer monitors the Death Star's control panels — the instant any dial is
// turned or any value is keyed in, an alarm light on the supervisor's board flickers.
// This is the simplest possible callback: "something on the configuration page changed —
// go re-validate and update the wizard buttons."
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A minimal callback interface that a {@link LdapServerAdapterConfigurationPage} fires
 * whenever any of its fields are modified.
 * The wizard container (or Properties dialog) implements this so it can re-run page validation
 * and update the "Next" / "Finish" button state as the user types.
 * Think of it as the supervisor's "change alert" light — it just blinks; the supervisor
 * decides what to do when it does.
 */
public interface LdapServerAdapterConfigurationPageModifyListener
{
    // ── The Change Alert Light Blinks ────────────────────────────────────────────────────────
    // A dial on the Death Star's control panel is turned — the change alert light on the
    // supervisor's board blinks once.  The supervisor checks the full panel state and
    // decides whether to enable the "fire" button.
    // Here, the wizard re-validates and updates "Finish" enablement.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by a {@link LdapServerAdapterConfigurationPage} whenever any of its input fields
     * has been changed by the user.
     * Implementors should trigger a re-validation of the page and update any dependent UI state
     * (e.g., enabling or disabling the wizard's "Finish" button).
     *
     * <p>For example — the supervisor reacts to a panel change:</p>
     * <pre>
     *   User types a new port number → page fires configurationPageModified()
     *   Wizard: page.validate() → page.isPageComplete() → update "Finish" button.
     * </pre>
     */
    void configurationPageModified();
}
