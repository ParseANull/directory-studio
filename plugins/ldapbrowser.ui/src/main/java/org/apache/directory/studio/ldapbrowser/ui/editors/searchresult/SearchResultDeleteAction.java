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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import java.util.Collection;
import java.util.HashSet;

import org.apache.directory.studio.ldapbrowser.common.actions.DeleteAction;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: SearchResultDeleteAction — Han Shoots First (Carefully) ────────────
// In the Mos Eisley cantina, Han Solo shoots first — but he doesn't shoot the
// innocent bystanders at the next table.  He is precise: Greedo, and only Greedo.
// This class is that precision: when Delete is triggered in the search result
// editor, we must NOT delete the whole LDAP entry — only the selected attribute
// value.  So we override getEntries() to return an empty set, which blocks the
// parent's "delete entry" path while leaving the attribute-delete path intact.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A delete action safe for use in the search result editor context.
 * The parent {@link DeleteAction} would delete the selected LDAP entries if we
 * let it — but in the search result table, the user only ever wants to delete a
 * selected attribute value, never the whole entry row.  We block the entry-delete
 * path by returning an empty collection from {@link #getEntries()}.
 * Think of this as Han shooting precisely — taking out what needs to go, nothing more.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultDeleteAction extends DeleteAction
{

    private static Collection<IEntry> EMPTY = new HashSet<IEntry>();


    // ── Han Aims Past the Bystanders ─────────────────────────────────────────
    // Han lines up his shot: Greedo is in the sights, the other patrons are not.
    // We return an empty collection here — no entries in scope, so the parent
    // class will skip the "delete entries" branch and only process attribute values.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an empty entry collection so the parent class skips entry deletion.
     * In the search result editor we only want to delete attribute values — the
     * parent's entry-delete logic would nuke the whole LDAP entry, which is never
     * the right action when the user hits Delete on a table cell.
     *
     * <p>For example — Han aims past the bystanders:</p>
     * <pre>
     *   getEntries() → empty set
     *   parent sees no entries → skips entry delete
     *   selected attribute value is deleted instead
     * </pre>
     *
     * @return an empty, immutable-ish collection — no entries will be deleted
     */
    protected Collection<IEntry> getEntries()
    {
        return EMPTY;
    }

}
