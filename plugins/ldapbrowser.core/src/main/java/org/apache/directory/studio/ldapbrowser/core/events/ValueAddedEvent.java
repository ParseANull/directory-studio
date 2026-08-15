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
import org.apache.directory.studio.ldapbrowser.core.model.IValue;


// ── CLASS: ValueAddedEvent — HAN ADDS A NEW ROUTE TO THE FALCON'S NAVICOMP ──
// Han Solo is in the Falcon's cockpit and punches a new hyperspace route into
// the navigation computer.  The route (a value) is appended to the ship's
// existing route library (the attribute).  "Kessel Run — added."  Chewie
// grunts approval; the navicomp confirms the new entry.
// This event fires when a real, non-empty {@link IValue} is appended to an
// existing LDAP attribute.  The attribute already existed on the entry; we
// just added another value to it.  For example, adding a second email address
// to a user who already has one: the "mail" attribute gains a new value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that a new {@link IValue} was added to an existing {@link IAttribute}
 * on an {@link IEntry}.
 * This is distinct from {@link AttributeAddedEvent}: the attribute itself was
 * already present; we just appended another value to it.  Listeners — typically
 * the attribute table viewer — respond by inserting a new value row under the
 * existing attribute section.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueAddedEvent extends EntryModificationEvent
{

    /** The modified attribute. */
    private IAttribute modifiedAttribute;

    /** The added value. */
    private IValue addedValue;


    // ── Han Logs The New Route: Ship, Attribute Panel, Value ─────────────────────
    // "Connection: Millennium Falcon comms.  Attribute panel: hyperspace routes.
    //  New value: Kessel Run, 12 parsecs."
    // All three references — attribute and value — are stored alongside the
    // standard connection+entry from the parent, so listeners have the full
    // picture without an extra server round-trip.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ValueAddedEvent.
     *
     * <p>For example — a second email address is added to a user:</p>
     * <pre>
     *   new ValueAddedEvent(conn, userEntry, mailAttribute, newMailValue);
     * </pre>
     *
     * @param connection         the browser connection through which the change was made.
     * @param modifiedEntry      the LDAP entry that owns the modified attribute.
     * @param modifiedAttribute  the attribute that received the new value.
     * @param addedValue         the value that was added.
     */
    public ValueAddedEvent( IBrowserConnection connection, IEntry modifiedEntry, IAttribute modifiedAttribute,
        IValue addedValue )
    {
        super( connection, modifiedEntry );
        this.modifiedAttribute = modifiedAttribute;
        this.addedValue = addedValue;
    }


    // ── Han Identifies Which Panel The Route Was Added To ────────────────────────
    // "Hyperspace routes panel — that's where the new Kessel Run entry sits."
    // Listeners use the attribute to find the right section of the attribute
    // table and refresh or extend it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute to which the new value was appended.
     *
     * @return the {@link IAttribute} that was modified; never {@code null}.
     */
    public IAttribute getModifiedAttribute()
    {
        return modifiedAttribute;
    }


    // ── Han Retrieves The New Route Data ─────────────────────────────────────────
    // "Kessel Run — string value, 12 parsecs."  The listener renders this value
    // in a new row under the attribute section.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value that was added to the attribute.
     *
     * @return the newly added {@link IValue}; never {@code null}.
     */
    public IValue getAddedValue()
    {
        return addedValue;
    }


    // ── Chewie Logs The New Route For The Ship's Records ─────────────────────────
    // "Added 'han.solo@rebelbase.org' to 'mail' at 'cn=Han Solo,dc=rebel,...'"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Added value 'foo' to 'mail' at 'cn=Han,...'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__added_val_to_att_at_dn, new String[]
            { getAddedValue().getStringValue(), getModifiedAttribute().getDescription(),
                getModifiedEntry().getDn().getName() } );
    }

}
