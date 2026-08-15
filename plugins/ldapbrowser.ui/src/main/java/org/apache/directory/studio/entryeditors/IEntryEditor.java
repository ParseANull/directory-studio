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

package org.apache.directory.studio.entryeditors;


import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: IEntryEditor — THE TANTIVE IV BRIDGE, ALL PANELS WORKING TOGETHER ─
// The bridge of the Tantive IV has multiple stations — helm, comms, weapons — each
// manned by a different officer doing a specific job. They all serve the same ship,
// respond to the same captain, and share the same mission. This interface is the
// contract all entry editor panels must honour: every station must be able to
// report whether it can handle the current mission and receive change notifications.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The common contract that every entry editor panel in Directory Studio must implement.
 * Whether it's the default table editor, the LDIF text editor, or a plugin-contributed
 * custom panel, all of them are "stations on the bridge" — they must know which
 * entries they can handle, respond when the working copy changes, expose their input,
 * and declare their save strategy.
 * Think of this as the shipwide comms protocol: every station understands the same
 * commands even though each does a different job.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IEntryEditor
{
    // ── The Helm Officer Reports Whether This Ship Can Navigate That Route ───────
    // The helm officer checks the nav computer: "Can we get to that system with our
    // current hyperdrive config?" Not every ship can reach every destination.
    // We ask the editor the same: "Can you display this particular LDAP entry?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this editor knows how to handle the given LDAP entry.
     * The {@link EntryEditorManager} asks this of every registered editor in priority
     * order until it finds one that says yes — that editor then gets opened.
     * For example, a certificate editor might only handle entries with userCertificate
     * attributes; a generic table editor would say yes to everything.
     *
     * @param entry  the LDAP entry to test; never {@code null}
     * @return       {@code true} if this editor can meaningfully display and edit the entry
     */
    boolean canHandle( IEntry entry );


    // ── The Comms Officer Relays That the Mission Orders Have Changed ────────────
    // The comms officer on the Tantive IV bridge receives a new priority from the
    // captain and immediately alerts every station: "Orders updated — adjust."
    // When the working copy of the LDAP entry changes, we call this on every open
    // editor that holds a reference so they can refresh their UI.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the {@link EntryEditorManager} whenever the shared working copy of
     * the displayed entry has been modified.
     * The editor should refresh its UI to reflect the latest working-copy state.
     * {@code source} identifies who triggered the modification — the editor can use
     * this to skip unnecessary redraws if it was the one that made the change.
     *
     * @param source  the object that initiated the modification, or {@code null} if unknown
     */
    void workingCopyModified( Object source );


    // ── The Captain Requests the Current Mission Briefing From the Station ───────
    // "What entry are you displaying right now?" the captain asks the weapons officer.
    // The officer hands over the briefing dossier — the EntryEditorInput — so the
    // captain knows the full context: entry, extension, working-copy state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EntryEditorInput} currently loaded into this editor.
     * The manager and other framework code use this to correlate editors with entries
     * and to route working-copy notifications to the right panels.
     *
     * @return  the current editor input, or {@code null} if no input has been set yet
     */
    EntryEditorInput getEntryEditorInput();


    // ── Checking Whether the Station Is on Auto-Pilot or Manual Override ─────────
    // Some stations on the bridge run on auto-pilot — any change they make is
    // immediately transmitted to the rest of the fleet without waiting for the captain
    // to approve. Others require explicit "send now" before anything goes out.
    // We report which mode this editor uses: auto-save or manual-save.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this editor commits changes to the LDAP server automatically
     * on every modification (auto-save), or waits for the user to explicitly save.
     * The {@link EntryEditorManager} uses this to manage two separate sets of
     * working copies — auto-save editors never show a dirty state, while manual-save
     * editors track a diff against a reference copy.
     *
     * @return  {@code true} if every change is immediately written to the server;
     *          {@code false} if changes are held in a working copy until the user saves
     */
    boolean isAutoSave();
}
