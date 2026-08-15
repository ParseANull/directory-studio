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


// ── CLASS: EntryAddedEvent — THE REBELS ESTABLISH ECHO BASE ON HOTH ─────────
// After Yavin IV becomes too dangerous, the Rebellion scouts a new location and
// establishes Echo Base on the ice planet Hoth.  A new base — a new node in the
// Rebel network — appears where there was previously nothing.  The intelligence
// map gains a new marker: "Hoth, sector 7G, operational."
// This event fires whenever a background job successfully creates a new LDAP
// entry in the directory.  Any listener watching the tree knows a new node
// appeared and needs to be inserted into the browser view.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that a new {@link IEntry} was created in the LDAP directory.
 * Fired after a successful LDAP "add" operation — for example when the user
 * creates a new user or organisational unit.  Listeners — typically the LDAP
 * Browser tree — respond by inserting a new node under the appropriate parent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryAddedEvent extends EntryModificationEvent
{

    // ── The Rebellion Registers The New Base On The Map ──────────────────────────
    // "Echo Base established — connection: Rebel fleet comms; entry: Hoth."
    // The map gets both the network channel and the new location so every
    // commander can route communications through the right path.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EntryAddedEvent.
     *
     * <p>For example — fired after a new LDAP entry is created:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new EntryAddedEvent(conn, newEntry), source);
     * </pre>
     *
     * @param connection  the browser connection through which the entry was created.
     * @param addedEntry  the newly created LDAP entry.
     */
    public EntryAddedEvent( IBrowserConnection connection, IEntry addedEntry )
    {
        super( connection, addedEntry );
    }


    // ── The Map Announces: "New Base Established At Hoth" ────────────────────────
    // The briefing officer reads the message plainly: "Echo Base, Hoth system,
    // now operational."  That's all the log needs — which entry was added.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Added 'cn=NewUser,dc=example,dc=com'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__added_dn, new String[]
            { getModifiedEntry().getDn().getName() } );
    }

}
