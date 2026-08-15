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

package org.apache.directory.studio.ldapbrowser.core.events;


import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: EntryRenamedEvent — ANAKIN SKYWALKER BECOMES DARTH VADER ──────────
// In the Chancellor's office, Anakin kneels and swears himself to Palpatine.
// "From this day forward, you will be known as… Darth Vader."  His location
// hasn't changed — he's still on Coruscant, still in the same order — but his
// identity has.  "Anakin Skywalker" is retired; "Darth Vader" is his new RDN
// in the Sith directory.
// This event fires when an LDAP "modrdn" operation changes an entry's relative
// distinguished name (RDN) — same parent, different name — giving it a new full
// DN.  We carry both old and new entry objects so listeners can update the tree.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an {@link IEntry} was renamed in the LDAP directory — the LDAP
 * "modrdn" operation that changes only the entry's RDN, not its parent.
 * The entry stays under the same parent but gets a new RDN and therefore a new
 * full DN.  We store both the old entry (with its original DN) and the new
 * entry (with the updated DN) so listeners can remove the old tree node and
 * replace it with the new one.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryRenamedEvent extends EntryModificationEvent
{

    /** The old entry. */
    private IEntry oldEntry;

    /** The new entry. */
    private IEntry newEntry;


    // ── Palpatine Records Both Names In The Sith Registry ────────────────────────
    // "Formerly: Anakin Skywalker.  New designation: Darth Vader.
    //  Location: Coruscant — unchanged."
    // Both names (old and new entry) are recorded so the Rebellion's intelligence
    // files can mark Anakin as gone and Vader as new.
    // We anchor the event to the new entry's parent (Coruscant), since the parent
    // didn't change — only the entry's own identity did.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EntryRenamedEvent.
     * We anchor this event to the new entry's parent because the parent itself
     * didn't change; the entry's own RDN is what was modified.
     *
     * <p>For example — fired after an LDAP modrdn operation completes:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new EntryRenamedEvent(oldEntry, newEntry), source);
     * </pre>
     *
     * @param oldEntry the entry with its original DN (now stale in the directory).
     * @param newEntry the entry with the updated DN (now live in the directory).
     */
    public EntryRenamedEvent( IEntry oldEntry, IEntry newEntry )
    {
        super( newEntry.getBrowserConnection(), newEntry.getParententry() );
        this.oldEntry = oldEntry;
        this.newEntry = newEntry;
    }


    // ── Retrieve The New Designation ─────────────────────────────────────────────
    // "What is he now called?" — Darth Vader.  The listeners need the new DN
    // to place the updated node in the tree at the right position.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry at its new RDN after the rename.
     * Use this to insert (or refresh) the tree node with the new name.
     *
     * @return the {@link IEntry} with the updated DN; never {@code null}.
     */
    public IEntry getNewEntry()
    {
        return newEntry;
    }


    // ── Retrieve The Old Designation ─────────────────────────────────────────────
    // "What was he called before?" — Anakin Skywalker.  Needed to remove the
    // old tree node before inserting the new one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry with its original DN before the rename.
     * Use this to remove the old tree node.  This entry object is now stale —
     * its DN no longer exists in the LDAP directory.
     *
     * @return the {@link IEntry} with the original DN; never {@code null}.
     */
    public IEntry getOldEntry()
    {
        return oldEntry;
    }


    // ── The Registry Logs Both Names ─────────────────────────────────────────────
    // "Renamed: cn=Anakin Skywalker,... → cn=Darth Vader,..."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Renamed 'cn=OldName,...' to 'cn=NewName,...'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__renamed_olddn_to_newdn, new String[]
            { getOldEntry().getDn().getName(), getNewEntry().getDn().getName() } );
    }

}
