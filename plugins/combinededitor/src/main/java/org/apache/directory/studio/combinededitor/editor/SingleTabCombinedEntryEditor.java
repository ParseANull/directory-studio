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


// ── CLASS: SingleTabCombinedEntryEditor — One Shared Bridge for the Whole Fleet ─
// Imagine the entire Rebel fleet sharing a single command bridge: when Mon Mothma
// clicks to view the Liberty's status, the display on Home One refreshes to show
// the Liberty — no second window, just one console that always shows whichever
// ship she last selected.
// SingleTabCombinedEntryEditor works the same way: all LDAP entries the user opens
// reuse the same single editor tab, so clicking a different entry in the browser
// tree refreshes the one tab rather than opening a new one.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A combined entry editor variant that reuses a single workbench editor tab for
 * all entries.
 * Opening a different entry navigates the existing tab to the new entry rather
 * than opening another tab.  This keeps the workbench tidy when the user browses
 * many entries in quick succession.
 * Changes are not auto-saved — the user must explicitly save to commit edits.
 * Think of this as Mon Mothma's single-console command style: one window,
 * always tuned to the current entry of interest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingleTabCombinedEntryEditor extends CombinedEntryEditor
{
    // ── One Console, No Auto-Save — The Navigator Decides When to Commit ──────
    // On a shared bridge the helmsman doesn't save the new course automatically
    // every time Mon Mothma asks to look at a different ship — he waits for an
    // explicit "Set course" order.  Our isAutoSave() follows the same policy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code false} because single-tab mode still requires explicit saves.
     * Even though entries share a tab, the user must press Ctrl+S to commit any
     * edits back to the LDAP server; we never silently overwrite data.
     *
     * @return  always {@code false}.
     */
    public boolean isAutoSave()
    {
        return false;
    }
}
