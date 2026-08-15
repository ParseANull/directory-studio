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


// ── CLASS: EmptyValueDeletedEvent — R2-D2 CLOSES THE EMPTY PANEL SLOT ───────
// R2-D2 opened a slot on the Death Star's computer interface but nothing was
// inserted — the user pressed Cancel.  The droid retracts the connector and
// closes the slot.  The panel looks exactly as it did before; the temporary
// opening is gone.
// This event fires when the UI discards an empty-value placeholder — typically
// because the user pressed Escape or moved focus away without typing anything.
// Listeners remove the empty row from the attribute table so the display snaps
// back to its pre-edit state.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an empty {@link IValue} placeholder was removed from an
 * {@link IEntry}'s attribute in the in-memory model.
 * The empty value was created by {@link EmptyValueAddedEvent} to give the user
 * an editable row.  If the user cancels without entering data this event fires
 * so the listener can remove the placeholder row and restore the table.
 * Nothing was written to the LDAP server in either direction.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EmptyValueDeletedEvent extends EntryModificationEvent
{

    /** The modified attribute. */
    private IAttribute modifiedAttribute;

    /** The deleted value. */
    private IValue deletedValue;


    // ── R2-D2 Logs Which Slot Was Closed And On Which Panel ───────────────────────
    // "Slot retracted on panel X, aboard ship Y, channel Z — no data transferred."
    // All four coordinates are captured so the listener can identify and remove
    // the exact UI row.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EmptyValueDeletedEvent.
     *
     * <p>For example — the user presses Escape on a new empty value row:</p>
     * <pre>
     *   new EmptyValueDeletedEvent(conn, userEntry, telephoneAttr, emptyPlaceholder);
     * </pre>
     *
     * @param connection         the browser connection.
     * @param modifiedEntry      the entry from which the placeholder was removed.
     * @param modifiedAttribute  the attribute whose placeholder row was removed.
     * @param deletedValue       the empty {@link IValue} placeholder that was removed.
     */
    public EmptyValueDeletedEvent( IBrowserConnection connection, IEntry modifiedEntry, IAttribute modifiedAttribute,
        IValue deletedValue )
    {
        super( connection, modifiedEntry );
        this.modifiedAttribute = modifiedAttribute;
        this.deletedValue = deletedValue;
    }


    // ── R2-D2 Names The Panel The Slot Was On ────────────────────────────────────
    // "Panel 7 — the slot is now closed."  The listener needs the attribute
    // to find the right table section to remove the empty row from.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute from which the empty value placeholder was removed.
     *
     * @return the {@link IAttribute}; never {@code null}.
     */
    public IAttribute getModifiedAttribute()
    {
        return this.modifiedAttribute;
    }


    // ── R2-D2 Identifies The Specific Connector That Was Retracted ────────────────
    // "Connector ID 42 — retracted."  The listener uses this to locate and
    // remove the exact empty row rather than refreshing the whole table.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the empty {@link IValue} placeholder that was removed.
     * The value object still exists in memory (this event holds a reference),
     * but it is no longer attached to the attribute's value list.
     *
     * @return the removed empty placeholder; never {@code null}.
     */
    public IValue getDeletedValue()
    {
        return this.deletedValue;
    }


    // ── R2-D2 Logs The Slot-Closure ──────────────────────────────────────────────
    // "Empty slot closed on telephoneNumber at cn=John,dc=example,dc=com."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Empty value deleted from 'telephoneNumber' at 'cn=John,...'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__empty_value_deleted_from_att_at_dn, new String[]
            { getModifiedAttribute().getDescription(), getModifiedEntry().getDn().getName() } );
    }

}
