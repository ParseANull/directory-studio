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


// ── CLASS: EmptyValueAddedEvent — R2-D2 OPENS AN EMPTY PANEL SLOT ───────────
// R2-D2 rolls up to the Death Star's computer interface and pops open a panel
// slot — the slot is ready, the connector is seated, but no data has been
// transferred yet.  The slot is there, waiting.
// This event fires when the UI adds a temporary empty-value placeholder to an
// LDAP attribute — specifically so the user can type a new value into it.
// It's different from {@link ValueAddedEvent} which fires when a real, non-empty
// value lands.  The empty placeholder is purely a UI device: it creates an
// editable row in the attribute table without yet writing anything to the server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an empty {@link IValue} placeholder was added to an {@link IEntry}'s
 * attribute in the in-memory model.
 * An empty value is a UI construct: it represents a row in the attribute table
 * that the user is about to fill in.  Nothing has been written to the LDAP server
 * yet.  Listeners respond by creating and focusing the inline editor for that row.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EmptyValueAddedEvent extends EntryModificationEvent
{

    /** The modified attribute. */
    private IAttribute modifiedAttribute;

    /** The added value. */
    private IValue addedValue;


    // ── R2-D2 Registers The Open Slot And Its Panel ───────────────────────────────
    // The droid logs: "slot opened on panel X, aboard ship Y, via channel Z."
    // All four context items — connection, entry, attribute, placeholder value —
    // are needed so the UI can find the exact row to open an editor for.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EmptyValueAddedEvent.
     *
     * <p>For example — the user clicks "New Value" on the "telephoneNumber" attribute:</p>
     * <pre>
     *   new EmptyValueAddedEvent(conn, userEntry, telephoneAttr, emptyPlaceholder);
     * </pre>
     *
     * @param connection         the browser connection.
     * @param modifiedEntry      the entry that received the empty placeholder.
     * @param modifiedAttribute  the attribute to which the placeholder was appended.
     * @param addedValue         the empty {@link IValue} placeholder.
     */
    public EmptyValueAddedEvent( IBrowserConnection connection, IEntry modifiedEntry, IAttribute modifiedAttribute,
        IValue addedValue )
    {
        super( connection, modifiedEntry );
        this.modifiedAttribute = modifiedAttribute;
        this.addedValue = addedValue;
    }


    // ── R2-D2 Identifies Which Panel The Slot Is In ──────────────────────────────
    // "Panel 7 — the communications array."  Without knowing the panel, the
    // technician can't find the right slot.  This returns the attribute so
    // the listener can locate the correct table row.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute to which the empty value placeholder was added.
     *
     * @return the {@link IAttribute} that now has an extra empty row; never {@code null}.
     */
    public IAttribute getModifiedAttribute()
    {
        return this.modifiedAttribute;
    }


    // ── R2-D2 Hands Over The Empty Connector ─────────────────────────────────────
    // "Here's the open slot — connector type, slot ID, ready for data."
    // The empty value object carries enough metadata for the UI to render
    // and focus the inline editor correctly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the empty {@link IValue} placeholder that was added.
     * The value's string representation is empty (""); it exists only to give
     * the UI a handle to open an inline editor on.
     *
     * @return the empty placeholder {@link IValue}; never {@code null}.
     */
    public IValue getAddedValue()
    {
        return this.addedValue;
    }


    // ── R2-D2 Logs The Open-Slot Notification ────────────────────────────────────
    // The droid's log entry is concise: "empty slot opened on telephoneNumber
    // at cn=John,dc=example,dc=com."  This method formats that log line.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Empty value added to 'telephoneNumber' at 'cn=John,...'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__empty_value_added_to_att_at_dn, new String[]
            { getModifiedAttribute().getDescription(), getModifiedEntry().getDn().getName() } );
    }

}
