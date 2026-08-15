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


// ── CLASS: AttributeAddedEvent — OBI-WAN SENSES LUKE'S FORCE AWAKENING ──────
// As Luke begins his training on Dagobah, Obi-Wan's Force ghost senses a new
// ability awakening inside him — a presence that wasn't there before.  The Force
// just gained a new dimension in Luke's profile.
// This event fires whenever a brand-new LDAP attribute is written onto an entry
// for the first time — e.g. an admin adds a "mail" attribute to a user that
// never had one.  Any listener watching that entry now knows a new data field
// has appeared and can update the UI accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that a new {@link IAttribute} was added to an {@link IEntry}.
 * This is distinct from {@link ValueAddedEvent}: we fire this when the attribute
 * itself is new to the entry (it had zero values before), not merely when an
 * extra value is appended to an existing attribute.
 * Listeners — typically UI tree or table viewers — respond by inserting a new
 * row for the attribute.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeAddedEvent extends EntryModificationEvent
{

    /** The added attribute. */
    private IAttribute addedAttribute;


    // ── Obi-Wan Records Luke's Newly Awakened Skill ──────────────────────────────
    // The Force ghost notes exactly which ability has surfaced: telekinesis,
    // not a generic "something new."  He needs the specifics to know what to
    // teach next.
    // We store the exact attribute that was added — "mail", "telephoneNumber",
    // whatever — so listeners can reference it without re-querying the directory.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new AttributeAddedEvent.
     *
     * <p>For example — a "mail" attribute is added to an entry:</p>
     * <pre>
     *   new AttributeAddedEvent(conn, userEntry, mailAttribute);
     * </pre>
     *
     * @param connection       the browser connection through which the change was made.
     * @param modifiedEntry    the LDAP entry that received the new attribute.
     * @param addedAttribute   the attribute that was added (never {@code null}).
     */
    public AttributeAddedEvent( IBrowserConnection connection, IEntry modifiedEntry, IAttribute addedAttribute )
    {
        super( connection, modifiedEntry );
        this.addedAttribute = addedAttribute;
    }


    // ── Obi-Wan Names The Awakened Power ─────────────────────────────────────────
    // "He has learned to lift rocks with his mind."  Obi-Wan gives the Rebellion
    // the exact name of the new skill so they know what Luke now brings.
    // Listeners call this to find out which specific attribute just appeared on
    // the entry — they need the description, type, and values to render it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute that was added to the entry.
     * Listeners can call {@code getAddedAttribute().getDescription()} to get
     * the attribute type name and {@code getAddedAttribute().getValues()} for
     * its initial values.
     *
     * @return the newly added {@link IAttribute}; never {@code null}.
     */
    public IAttribute getAddedAttribute()
    {
        return addedAttribute;
    }


    // ── Obi-Wan Relays A Human-Readable Report ───────────────────────────────────
    // When Obi-Wan sends his message to the Rebellion he phrases it plainly:
    // "Luke has gained the ability to sense the future at entry cn=Luke,..."
    // This method does the same — formats a readable summary for logs and status
    // bars, binding the attribute description and entry DN into one string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * status bar messages.
     *
     * @return a localised string like "Added attribute 'mail' to cn=John,dc=example,dc=com".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__added_att_to_dn, new String[]
            { getAddedAttribute().getDescription(), getModifiedEntry().getDn().getName() } );
    }

}
