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
package org.apache.directory.studio.templateeditor.editor;


// ── CLASS: SingleTabTemplateEntryEditor — MON MOTHMA'S SHARED BRIEFING ROOM ──────
// In smaller Rebel outposts there's only one briefing table — whoever needs it next
// sits down and the previous mission's map gets rolled up. The SingleTab editor does
// the same: one persistent editor tab is reused for every LDAP entry the user
// opens, so navigating to a new entry replaces the previous one rather than opening
// a fresh tab. Like the multi-tab variant, it also requires an explicit save.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Template entry editor that reuses a single Eclipse editor tab for all LDAP
 * entries. Navigating to a new entry replaces the existing content in that tab —
 * think of it as the shared briefing table that gets wiped clean for the next
 * mission. Like {@link MultiTabTemplateEntryEditor}, changes require an explicit
 * save (Ctrl+S) rather than being committed automatically.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingleTabTemplateEntryEditor extends TemplateEntryEditor
{
    // ── IS AUTO SAVE: THE SHARED TABLE STILL WAITS FOR SIGN-OFF ─────────────────
    // Even though the briefing table is shared, Mon Mothma still requires a
    // commander's signature before any plan is finalized. isAutoSave()==false
    // ensures changes wait for an explicit save gesture.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code false} to indicate that this editor does not auto-save
     * changes. The user must explicitly trigger a save (Ctrl+S or File > Save)
     * for modifications to be committed to the LDAP server.
     *
     * <p>For example — the shared briefing table waits for sign-off:</p>
     * <pre>
     *   isAutoSave() → false
     *   // Changes wait in the working copy until the user saves.
     * </pre>
     *
     * @return {@code false} always
     */
    public boolean isAutoSave()
    {
        return false;
    }
}
