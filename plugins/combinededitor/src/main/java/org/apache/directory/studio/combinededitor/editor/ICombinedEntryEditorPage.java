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
package org.apache.directory.studio.combinededitor.editor;


import org.eclipse.swt.custom.CTabItem;


// ── CLASS: ICombinedEntryEditorPage — The Crew Station Contract on the Tantive IV ──
// Every crew station on the Tantive IV bridge must support the same basic
// operations: be turned on, be briefed when the mission changes, show what it
// knows, and be powered down cleanly.  Captain Antilles enforces this contract
// across all stations so the bridge works as a single coherent unit.
// ICombinedEntryEditorPage is that contract: every tab page in the combined
// editor (Template, Table, LDIF) must implement these methods so the outer
// CombinedEntryEditor can manage them uniformly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The contract that every tab page in the combined entry editor must satisfy.
 * {@link CombinedEntryEditor} drives three pages (Template, Table, LDIF) through
 * this interface — it initialises them lazily when first selected, tells them
 * when the editor input changes, and disposes them on close.
 * Think of this as Captain Antilles' bridge station protocol — every station
 * must answer the same set of commands.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ICombinedEntryEditorPage
{
    // ── Antilles Powers Down a Station — Clean Shutdown ───────────────────────
    // When the battle is over Antilles powers down each station in sequence,
    // releasing any held resources before the ship goes dark.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this page — SWT widgets, listeners, etc.
     * Called by {@link CombinedEntryEditor#dispose()} when the editor is closed.
     */
    void dispose();


    // ── New Orders Arrive — The Mission Has Changed ────────────────────────────
    // The bridge receives new coordinates; each station updates its displays
    // to reflect the new mission without having to be powered down and restarted.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the editor's input has changed to a different LDAP entry.
     * Pages that are already initialised should update their display to show
     * the new entry's data; uninitialised pages can ignore this until they're
     * first activated.
     */
    void editorInputChanged();


    // ── Which Ship Do You Belong To? ──────────────────────────────────────────
    // Each crew station can answer which ship it's on — that's the editor that
    // owns and manages it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link CombinedEntryEditor} that owns this page.
     *
     * @return  the owning editor — never {@code null} once the page is constructed.
     */
    CombinedEntryEditor getEditor();


    // ── Which Tab Are You? ────────────────────────────────────────────────────
    // Each bridge station has a labelled tab so Antilles can identify it at a
    // glance on the CTabFolder row at the bottom of the bridge display.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link CTabItem} that represents this page in the tab folder.
     * The combined editor uses this to detect which tab the user selects.
     *
     * @return  the tab item for this page.
     */
    CTabItem getTabItem();


    // ── Station Powers Up for the First Time — Lazy Initialisation ────────────
    // A crew station isn't fully set up until Antilles actually assigns someone
    // to sit at it — we don't waste resources equipping unused stations.
    // Similarly, pages are lazily initialised the first time their tab is selected.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the page's SWT widgets and sets up listeners.
     * We initialise lazily — only when the user first selects this page's tab —
     * so we don't build widgets that the user never looks at.
     * After this call, {@link #isInitialized()} must return {@code true}.
     */
    void init();


    // ── Is This Station Online? ───────────────────────────────────────────────
    // Antilles checks whether a station has been manned and is ready to respond
    // before he routes a command to it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether {@link #init()} has been called for this page.
     * The combined editor checks this before routing update commands — there's
     * no point updating a page whose widgets don't exist yet.
     *
     * @return  {@code true} if the page's widgets have been created.
     */
    boolean isInitialized();


    // ── Antilles Points — "You, Focus Here" ──────────────────────────────────
    // When Antilles wants a specific station officer to take keyboard focus,
    // he points directly at them and says "You're on this."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus into this page's primary control.
     * Eclipse calls this when the user switches to this tab or when the editor
     * itself receives focus, so the user can start typing without an extra click.
     */
    void setFocus();


    // ── Antilles Calls for a Status Update — Refresh All Displays ─────────────
    // Antilles calls "All stations: update your displays" when new information
    // arrives from the fleet — each station refreshes what it's showing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes this page's display from the current shared working copy entry.
     * Called when the entry's data changes (e.g. another page edited an attribute)
     * so this page stays consistent with the rest of the editor.
     */
    void update();
}
