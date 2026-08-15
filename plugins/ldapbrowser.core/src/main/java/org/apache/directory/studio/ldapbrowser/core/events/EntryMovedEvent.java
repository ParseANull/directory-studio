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


// ── CLASS: EntryMovedEvent — THE REBEL FLEET EVACUATES TO SECTOR SEVEN ──────
// After the Empire detects Echo Base on Hoth, Admiral Ackbar orders an
// emergency evacuation.  The Rebel fleet — all its ships, their crews, their
// data — jumps from Hoth's sector to a rendezvous point in Sector Seven.
// The identity of each ship (its name, its captain) doesn't change; only its
// location in space does.
// This event fires when an LDAP entry is moved (LDAP "modrdn" with a new
// superior) to a different parent in the tree.  The entry's RDN stays the same
// but its parent — and therefore its full DN — changes.  We carry both the old
// and new entry objects so listeners can clean up the old tree node and insert
// the new one.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an {@link IEntry} was moved to a different parent in the LDAP
 * directory — the LDAP "modrdn with new superior" operation.
 * The entry's relative distinguished name (RDN) is unchanged; only its parent
 * changes, giving it a new full DN.  We store both the old entry (at its
 * original location) and the new entry (at the new location) so that listeners
 * can remove the old node from the tree and insert the new one.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryMovedEvent extends EntryModificationEvent
{

    /** The old entry. */
    private IEntry oldEntry;

    /** The new entry. */
    private IEntry newEntry;


    // ── The Fleet Logs Both The Old And New Sector Positions ─────────────────────
    // "Ship departed: Hoth, sector 7G.  Ship arrived: Sector Seven rendezvous."
    // Both co-ordinates are logged so the intelligence map knows exactly where
    // to remove the old marker and where to place the new one.
    // We anchor the event to the new entry's parent (the new sector) rather
    // than the entry itself, since the entry object at the old location is gone.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EntryMovedEvent.
     * We anchor this event to the new entry's parent because the entry's own
     * identity (RDN) hasn't changed — only its location has.
     *
     * <p>For example — fired after an LDAP move operation completes:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new EntryMovedEvent(oldEntry, newEntry), source);
     * </pre>
     *
     * @param oldEntry the entry at its original parent location (now stale).
     * @param newEntry the entry at its new parent location (now live in the directory).
     */
    public EntryMovedEvent( IEntry oldEntry, IEntry newEntry )
    {
        super( newEntry.getBrowserConnection(), newEntry.getParententry() );
        this.oldEntry = oldEntry;
        this.newEntry = newEntry;
    }


    // ── Retrieve The New Sector Position ─────────────────────────────────────────
    // "Where is the fleet NOW?" — the intelligence officer needs the new co-
    // ordinates to update the map with the live position.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry at its new location after the move.
     * Use this to insert the new tree node or refresh the browser view with
     * the updated DN.
     *
     * @return the {@link IEntry} at the new parent location; never {@code null}.
     */
    public IEntry getNewEntry()
    {
        return newEntry;
    }


    // ── Retrieve The Old Sector Position ─────────────────────────────────────────
    // "Where was the fleet BEFORE?" — needed to erase the old marker from the map.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry as it was at its original location before the move.
     * Use this to remove the old tree node.  Note that this entry object is now
     * stale — it no longer matches anything in the LDAP directory.
     *
     * @return the {@link IEntry} at the old parent location; never {@code null}.
     */
    public IEntry getOldEntry()
    {
        return oldEntry;
    }


    // ── The Fleet Log: "Moved From Hoth To Sector Seven" ────────────────────────
    // "Wing Commander Rex, departed Hoth, arrived Sector Seven rendezvous."
    // The log captures RDN, old parent, new parent — exactly what the status
    // bar needs.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Moved 'cn=Rex' from 'ou=Hoth,...' to 'ou=Endor,...'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__moved_oldrdn_from_oldparent_to_newparent,
            new String[]
                { getOldEntry().getDn().getRdn().getName(), getOldEntry().getParententry().getDn().getName(),
                    getNewEntry().getParententry().getDn().getName() } );
    }

}
