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


// ── CLASS: ChildrenInitializedEvent — VADER REVEALS HE HAS A SON ─────────────
// In Cloud City's carbon-freeze chamber, Darth Vader tells Luke: "I am your
// father."  Suddenly, what Luke thought was a lone-warrior story becomes a
// family tree — Luke has a parent, and that parent has sub-nodes (Luke, Leia)
// that the Rebellion never knew about.
// This event fires when a background job fetches the child entries under a
// given LDAP entry for the first time.  Before the job ran, the parent node
// showed a placeholder "Loading…".  After this event, the children are in
// memory and the tree can expand to reveal them.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that the child entries of an {@link IEntry} were loaded from the
 * LDAP directory for the first time (or refreshed after a reload).
 * We fire this after a background job completes a one-level LDAP search that
 * populates the entry's children list.  Listeners — typically the LDAP Browser
 * tree viewer — respond by expanding the node and rendering its child rows.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ChildrenInitializedEvent extends EntryModificationEvent
{

    // ── The Parent Reveals Its Children Are Now Known ─────────────────────────────
    // Vader steps forward; Luke's whole family tree reshapes in an instant.
    // All we need is the parent entry — we pull the connection from it so
    // callers don't have to pass both.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ChildrenInitializedEvent for the given entry.
     * The connection is extracted from the entry itself, so you only need the
     * entry reference.
     *
     * <p>For example — fired after a background child-load job finishes:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new ChildrenInitializedEvent(parentEntry), source);
     * </pre>
     *
     * @param initializedEntry the LDAP entry whose children were just loaded
     *                         from the directory.
     */
    public ChildrenInitializedEvent( IEntry initializedEntry )
    {
        super( initializedEntry.getBrowserConnection(), initializedEntry );
    }


    // ── "I Am Your Father" — Summary For The Log ─────────────────────────────────
    // The revelation is concise: "Children of ou=Users,dc=example,dc=com initialized."
    // That's all the log needs — which node just got its sub-tree populated.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Children of 'ou=Users,dc=example,dc=com' initialized".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__dn_children_initialized, new String[]
            { getModifiedEntry().getDn().getName() } );
    }

}
