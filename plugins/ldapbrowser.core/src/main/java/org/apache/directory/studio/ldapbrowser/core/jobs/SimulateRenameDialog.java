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
package org.apache.directory.studio.ldapbrowser.core.jobs;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;


// ── CLASS: SimulateRenameDialog — HAN ASKS "SHOULD WE JUMP TO LIGHTSPEED?" ──
// Han Solo realizes the Millennium Falcon can't do a clean hyperspace jump from
// this location (the LDAP server won't rename a non-leaf entry directly).
// He calls back to Princess Leia: "Do you want me to plot the long way around —
// copy all the kids, then delete the original?"  She says yes or no.
// When renaming a non-leaf entry fails with error 66 (NotAllowedOnNonLeaf),
// this dialog asks the user whether we should simulate the rename by recursively
// copying the whole subtree to the new DN and then deleting the original.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog interface that asks the user whether to simulate a rename by
 * recursively searching, copying, and deleting entries.
 * Some LDAP servers (particularly those without a "rename subtree" extension)
 * reject a modifyDN on a non-leaf entry with LDAP error 66 (NotAllowedOnNonLeaf).
 * When that happens, we offer a fallback: copy everything under the old DN to
 * the new DN, then delete the originals.  This dialog obtains the user's
 * decision.  Implementations live in the UI layer.
 * Think of it as Han asking whether to take the scenic route rather than
 * the direct hyperspace jump.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface SimulateRenameDialog
{

    // ── Han Checks The Nav Computer For Old And New Coordinates ──────────────
    // "I need to know where we are now and where we need to end up."
    // We supply both DNs so the dialog can display them for the user.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Provides the dialog with the connection and the old/new DNs so it can
     * display meaningful information to the user before they decide.
     *
     * @param browserConnection the connection to the LDAP server.
     * @param oldDn             the current DN of the entry to rename.
     * @param newDn             the desired new DN after the rename.
     */
    void setEntryInfo( IBrowserConnection browserConnection, Dn oldDn, Dn newDn );


    // ── Han Fires Up The Engines To Start The Negotiation ────────────────────
    // "Alright, let's see what the Princess says."  The dialog blocks until
    // the user makes a choice.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the dialog and blocks until the user closes it.
     *
     * @return the dialog's return code.
     */
    int open();


    // ── Han Checks Whether Leia Said "Plot The Long Route" ───────────────────
    // After the dialog is dismissed, the caller checks this flag.  If true, the
    // rename-entry runnable triggers the copy-then-delete simulation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user agreed to simulate the rename operation by
     * recursively copying the subtree to the new DN and then deleting the original.
     *
     * @return {@code true} if the user wants to simulate the rename;
     *         {@code false} to abort.
     */
    boolean isSimulateRename();
}
