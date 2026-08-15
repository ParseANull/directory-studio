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


// ── CLASS: MultiTabTemplateEntryEditor — MON MOTHMA BRIEFING SEPARATE COMMANDERS ─
// In the Rebel Alliance, each mission gets its own separate briefing room — Gold
// Squadron's briefing doesn't share a room with Red Squadron's. The MultiTab
// variant of the template entry editor does the same: each LDAP entry gets its own
// editor tab rather than all entries sharing one. Combined with isAutoSave()==false,
// the editor explicitly requires the user to save (Ctrl+S or File > Save) before
// changes are committed to the LDAP server.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Template entry editor that opens each LDAP entry in its own separate Eclipse
 * editor tab. This is the "multi-tab" variant — navigating to a new entry opens
 * a new editor tab rather than reusing an existing one. Inherits all behavior
 * from {@link TemplateEntryEditor} but opts out of auto-save so changes only
 * persist when the user explicitly saves.
 * Think of this as Mon Mothma running separate briefings per mission: each
 * entry gets its own briefing room.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MultiTabTemplateEntryEditor extends TemplateEntryEditor
{
    // ── IS AUTO SAVE: SEPARATE BRIEFINGS REQUIRE EXPLICIT COMMIT ─────────────────
    // Each briefing room locks its doors until the commander formally approves
    // the plan — no changes slip through by accident. isAutoSave()==false means
    // the editor waits for an explicit save gesture before writing to the LDAP
    // server, giving the user time to review changes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code false} to indicate that this editor does not auto-save
     * changes. The user must explicitly trigger a save (Ctrl+S or File > Save)
     * for modifications to be committed to the LDAP server.
     *
     * <p>For example — the briefing room locks its doors until the commander approves:</p>
     * <pre>
     *   isAutoSave() → false
     *   // Changes accumulate in the working copy until the user saves.
     * </pre>
     *
     * @return {@code false} always
     */
    public boolean isAutoSave()
    {
        return false;
    }
}
