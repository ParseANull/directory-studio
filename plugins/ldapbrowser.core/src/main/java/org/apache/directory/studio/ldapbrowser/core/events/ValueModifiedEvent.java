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


// ── CLASS: ValueModifiedEvent — LANDO ALTERS THE DEAL ───────────────────────
// Lando Calrissian made a deal with Darth Vader: hand over the Rebels in
// exchange for Cloud City's independence.  Then Vader alters the deal.  The
// deal still exists — the same attribute, the same slot — but its terms (its
// value) have been replaced.  "I am altering the deal.  Pray I don't alter it
// any further."
// This event fires when a single existing LDAP value is replaced with a new
// one in the same attribute slot — a modification, not an add-then-delete.
// Listeners receive both the old value and the new value so they can update
// the specific table cell without reloading the whole attribute.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an existing {@link IValue} was replaced with a new value in the
 * same attribute slot on an {@link IEntry}.
 * This fires when the user edits a value in place — the attribute (the "deal")
 * remains, but its content changed from {@code oldValue} to {@code newValue}.
 * Listeners receive both so they can diff-update the UI rather than refreshing
 * the entire entry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueModifiedEvent extends EntryModificationEvent
{

    /** The modified attribute. */
    private IAttribute modifiedAttribute;

    /** The old value. */
    private IValue oldValue;

    /** The new value. */
    private IValue newValue;


    // ── Vader Alters The Terms: Old Deal, New Deal, Same Contract ────────────────
    // "The deal WAS: hand over Solo.  The deal IS NOW: hand over everyone."
    // Both the old and new terms are recorded alongside the contract reference
    // (attribute) and the signatory (entry).  The listener can show a diff
    // in the UI: strike through the old value, display the new one.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ValueModifiedEvent.
     *
     * <p>For example — a user's phone number is corrected:</p>
     * <pre>
     *   new ValueModifiedEvent(conn, userEntry, phoneAttr, oldPhone, newPhone);
     * </pre>
     *
     * @param connection         the browser connection through which the change was made.
     * @param modifiedEntry      the LDAP entry whose attribute value changed.
     * @param modifiedAttribute  the attribute that holds the changed value.
     * @param oldValue           the value as it was before the modification.
     * @param newValue           the value as it is after the modification.
     */
    public ValueModifiedEvent( IBrowserConnection connection, IEntry modifiedEntry, IAttribute modifiedAttribute,
        IValue oldValue, IValue newValue )
    {
        super( connection, modifiedEntry );
        this.modifiedAttribute = modifiedAttribute;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }


    // ── Identify Which Contract Slot Was Altered ──────────────────────────────────
    // "The deal was in section 3 — telephone numbers."  The listener needs the
    // attribute to find the right section of the attribute table.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute that contains the modified value.
     *
     * @return the {@link IAttribute}; never {@code null}.
     */
    public IAttribute getModifiedAttribute()
    {
        return modifiedAttribute;
    }


    // ── Retrieve The Original Terms ───────────────────────────────────────────────
    // "The old deal said: +1-555-0100."  Needed to locate the row in the UI
    // and display what changed (or animate the transition from old to new).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value as it was before the modification.
     * Use this to identify which row in the attribute table to update.
     *
     * @return the old {@link IValue}; never {@code null}.
     */
    public IValue getOldValue()
    {
        return oldValue;
    }


    // ── Retrieve The Altered Terms ────────────────────────────────────────────────
    // "The new deal says: +1-555-0199."  This is what the listener renders
    // in the table cell after the edit.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value as it is after the modification.
     * Use this to update the UI row with the new data.
     *
     * @return the new {@link IValue}; never {@code null}.
     */
    public IValue getNewValue()
    {
        return newValue;
    }


    // ── Lando Logs The Altered Deal ───────────────────────────────────────────────
    // "Replaced '+1-555-0100' with '+1-555-0199' at 'telephoneNumber' for 'cn=Lando,...'"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Replaced 'old' with 'new' at 'telephoneNumber' for 'cn=Lando,...'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__replaced_oldval_by_newval_at_att_at_dn,
            new String[]
                { getOldValue().getStringValue(), getNewValue().getStringValue(),
                    getModifiedAttribute().getDescription(), getModifiedEntry().getDn().getName() } );
    }

}
