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


// ── CLASS: BulkModificationEvent — ORDER 66 EXECUTED ACROSS ALL SYSTEMS ─────
// Palpatine transmits Order 66: "Execute Order 66."  In a single moment, clone
// troopers across every star system simultaneously turn on their Jedi commanders.
// There's no granular report — nobody says "trooper X shot Jedi Y at location Z".
// The message is simply: "Everything just changed.  Assume nothing is the same."
// This event is the LDAP equivalent: something happened that could have touched
// many entries across the connection — a bulk copy, a mass import, a full tree
// refresh.  Rather than fire one event per entry, we fire a single
// BulkModificationEvent anchored to the RootDSE so that all listeners know
// "the world has changed — refresh everything."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that a bulk modification occurred on a browser connection.
 * Rather than firing individual events for each entry or attribute that was
 * touched, we fire a single BulkModificationEvent when operations like mass
 * entry copies or full tree refreshes happen.  The event is anchored to the
 * connection's RootDSE entry (the root of the LDAP tree), signalling listeners
 * that everything under this connection may have changed.
 * Listeners typically respond with a full refresh of the browser tree.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BulkModificationEvent extends EntryModificationEvent
{

    // ── Order 66 Is Broadcast On All Channels From The Top ───────────────────────
    // "Execute Order 66" goes out from the Emperor to every system at once.
    // The command originates at the top of the chain — the RootDSE — not at
    // any individual Jedi's location.  We pass only the connection because the
    // entry is always the RootDSE (the topmost entry, representing "everything").
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BulkModificationEvent anchored to the connection's RootDSE.
     * The RootDSE is the LDAP root entry (DN = ""), representing the entire
     * directory tree.  Using it as the event's "modified entry" signals that
     * the scope of this change is the whole connection, not a specific node.
     *
     * <p>For example — fired after a mass copy operation:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new BulkModificationEvent(browserConn), this);
     * </pre>
     *
     * @param connection the browser connection on which the bulk modification occurred.
     */
    public BulkModificationEvent( IBrowserConnection connection )
    {
        super( connection, connection.getRootDSE() );
    }


    // ── Order 66 Report: "Everything Just Changed" ───────────────────────────────
    // The clone trooper report is blunt: "Order 66 executed."  No itemisation.
    // Our toString() is equally blunt — it's a single "bulk modification" message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * status bar messages.
     *
     * @return the localised "bulk modification" string; never {@code null}.
     */
    public String toString()
    {
        return BrowserCoreMessages.event__bulk_modification;
    }

}
