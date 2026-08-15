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


// ── CLASS: MultiTabCombinedEntryEditor — Every Ship Gets Its Own Bridge ──────
// In the Rebel fleet, each capital ship has its own independent bridge crew —
// the Redemption, the Liberty, and Home One each run separate command operations
// in parallel without sharing a console.
// MultiTabCombinedEntryEditor mirrors that: every LDAP entry the user opens
// gets its own dedicated editor tab in the Eclipse workbench, so multiple
// entries can be viewed and edited side by side without sharing state.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A combined entry editor variant that opens each LDAP entry in its own
 * separate workbench editor tab.
 * The user can have several entries open simultaneously, each in its own tab.
 * Changes are not auto-saved — the user must explicitly save (Ctrl+S) to write
 * modifications back to the LDAP server.
 * Think of this as the Rebel fleet strategy: independent ships, each with their
 * own full bridge, all working towards the same goal.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultiTabCombinedEntryEditor extends CombinedEntryEditor
{
    // ── Each Ship Keeps Its Own Helm — No Auto-Pilot Saves ───────────────────
    // In the multi-ship Rebel fleet there's no central auto-pilot that saves
    // the helm settings for every ship automatically — each captain decides
    // when to commit a course change.  Similarly, we don't auto-save edits;
    // the user stays in control and saves explicitly when ready.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code false} because multi-tab mode requires explicit saves.
     * When auto-save is off, the editor marks itself dirty and waits for the
     * user to press Ctrl+S (or File > Save) before sending changes to the LDAP
     * server.
     *
     * @return  always {@code false}.
     */
    public boolean isAutoSave()
    {
        return false;
    }
}
