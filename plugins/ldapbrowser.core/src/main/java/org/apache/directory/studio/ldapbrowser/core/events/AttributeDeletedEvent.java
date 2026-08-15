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
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: AttributeDeletedEvent — OBI-WAN FEELS ALDERAAN GO SILENT ─────────
// "I felt a great disturbance in the Force — as if millions of voices suddenly
// cried out in terror, and were suddenly silenced."  Obi-Wan senses the
// complete destruction of Alderaan: not a wound, but a total absence.
// This event fires when an attribute is entirely removed from an LDAP entry —
// not just one value trimmed, but the whole attribute gone.  Listeners watching
// that entry know a data field has been wiped and must remove it from the UI.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an existing {@link IAttribute} was deleted from an {@link IEntry}.
 * This is distinct from {@link ValueDeletedEvent}: we fire this when the attribute
 * itself is removed entirely (all its values are gone), not merely when a single
 * value is pruned from a multi-value attribute.
 * Listeners — typically UI tree or table viewers — respond by removing the
 * attribute's row from the display.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeDeletedEvent extends EntryModificationEvent
{

    /** The deleted attribute. */
    private IAttribute deletedAttribute;


    // ── Obi-Wan Notes Which Planet Went Dark ─────────────────────────────────────
    // Obi-Wan doesn't just sense *a* silence — he senses Alderaan specifically.
    // He names the planet so the Rebellion knows precisely what was lost.
    // We store the exact attribute that was deleted so listeners can look it up
    // (e.g. remove the right row) without having to guess from the entry state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new AttributeDeletedEvent.
     *
     * <p>For example — the "mail" attribute is deleted from an entry:</p>
     * <pre>
     *   new AttributeDeletedEvent(conn, userEntry, mailAttribute);
     * </pre>
     *
     * @param connection         the browser connection through which the deletion occurred.
     * @param modifiedEntry      the LDAP entry from which the attribute was removed.
     * @param deletedAttribute   the attribute that was deleted (never {@code null}).
     */
    public AttributeDeletedEvent( IBrowserConnection connection, IEntry modifiedEntry, IAttribute deletedAttribute )
    {
        super( connection, modifiedEntry );
        this.deletedAttribute = deletedAttribute;
    }


    // ── Obi-Wan Identifies The Lost System By Name ───────────────────────────────
    // "It was Alderaan."  Not just a nameless void — a specific, named system.
    // Listeners call this to retrieve the attribute object so they can remove
    // the right row from the table rather than refreshing the entire entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute that was deleted from the entry.
     * The attribute object still exists in memory (the caller holds this event),
     * but it is no longer attached to the entry in the LDAP directory.
     *
     * @return the deleted {@link IAttribute}; never {@code null}.
     */
    public IAttribute getDeletedAttribute()
    {
        return deletedAttribute;
    }


    // ── Obi-Wan Reports The Silence To The Rebellion ─────────────────────────────
    // Obi-Wan's message to the Rebellion is concise but specific: "Alderaan is
    // gone — destroyed by the Death Star."  This method does the same for logs.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * status bar messages.
     *
     * @return a localised string like "Deleted attribute 'mail' from cn=John,dc=example,dc=com".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__deleted_att_from_dn, new String[]
            { getDeletedAttribute().getDescription(), getModifiedEntry().getDn().getName() } );
    }

}
