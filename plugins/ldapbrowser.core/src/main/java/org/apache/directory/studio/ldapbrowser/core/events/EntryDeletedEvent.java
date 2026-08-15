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
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: EntryDeletedEvent — THE DEATH STAR DESTROYS ALDERAAN ─────────────
// Grand Moff Tarkin gives the command, and the Death Star fires.  Alderaan —
// a planet full of two billion people — ceases to exist.  It's not moved, not
// renamed, not modified.  It's simply gone from the galaxy map.
// This event fires after a successful LDAP "delete" operation removes an entry
// from the directory.  The tree viewer hears the event and removes the node —
// exactly as the galaxy map would quietly erase Alderaan's marker.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an {@link IEntry} was deleted from the LDAP directory.
 * Fired after a successful LDAP "delete" operation.  Listeners — typically the
 * LDAP Browser tree — respond by removing the node from the tree and clearing
 * any cached data for that DN.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryDeletedEvent extends EntryModificationEvent
{

    // ── The Galaxy Map Marks The Deletion Before The Dust Settles ────────────────
    // "Alderaan — deleted.  Connection: Imperial tactical comms."  The map
    // records both which channel transmitted the order and which world is gone,
    // so the listeners know exactly what to remove from the display.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EntryDeletedEvent.
     *
     * <p>For example — fired after an LDAP delete operation completes:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new EntryDeletedEvent(conn, deletedEntry), source);
     * </pre>
     *
     * @param connection    the browser connection through which the deletion occurred.
     * @param deletedEntry  the entry that was deleted (no longer in the directory).
     */
    public EntryDeletedEvent( IBrowserConnection connection, IEntry deletedEntry )
    {
        super( connection, deletedEntry );
    }


    // ── The Map Logs: "Alderaan Has Been Destroyed" ───────────────────────────────
    // The tactical officer reads out the DN: "cn=Alderaan,... deleted."
    // Concise, final — exactly what the logs and status bar need.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Deleted 'cn=OldUser,dc=example,dc=com'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__deleted_dn, new String[]
            { getModifiedEntry().getDn().getName() } );
    }

}
