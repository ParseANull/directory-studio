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


// ── CLASS: ValueDeletedEvent — VADER STRIKES DOWN OBI-WAN ────────────────────
// In the Death Star's docking bay, Vader raises his lightsaber and strikes down
// Obi-Wan.  Obi-Wan was a value — a specific, named entry — in the Jedi Order's
// roster.  Now that value is gone, though the attribute (the Jedi attribute list)
// still exists, because Luke and Leia remain.
// This event fires when a single value is removed from an LDAP attribute that
// still has other values remaining.  For example, if a user has three email
// addresses and one is deleted, we fire this — not {@link AttributeDeletedEvent},
// because the "mail" attribute itself still survives with two values.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that a single {@link IValue} was deleted from an existing
 * {@link IAttribute} on an {@link IEntry}.
 * This is distinct from {@link AttributeDeletedEvent}: the attribute still
 * exists (with its remaining values); only one specific value was removed.
 * Listeners — typically the attribute table viewer — respond by removing the
 * deleted value's row while leaving the attribute section intact.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueDeletedEvent extends EntryModificationEvent
{

    /** The modified attribute. */
    private IAttribute modifiedAttribute;

    /** The deleted value. */
    private IValue deletedValue;


    // ── The Jedi Registry Notes The Fallen Knight ────────────────────────────────
    // "Obi-Wan Kenobi — struck down.  Attribute: Jedi Knights roster.
    //  Connection: Rebel Alliance records.  Entry: Alderaan (Leia's contacts)."
    // All three — connection, entry, attribute, deleted value — are recorded
    // so the listener can identify and remove the exact table row.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ValueDeletedEvent.
     *
     * <p>For example — one of a user's email addresses is deleted:</p>
     * <pre>
     *   new ValueDeletedEvent(conn, userEntry, mailAttribute, oldMailValue);
     * </pre>
     *
     * @param connection         the browser connection through which the deletion occurred.
     * @param modifiedEntry      the LDAP entry that owns the modified attribute.
     * @param modifiedAttribute  the attribute from which the value was deleted.
     * @param deletedValue       the value that was removed.
     */
    public ValueDeletedEvent( IBrowserConnection connection, IEntry modifiedEntry, IAttribute modifiedAttribute,
        IValue deletedValue )
    {
        super( connection, modifiedEntry );
        this.modifiedAttribute = modifiedAttribute;
        this.deletedValue = deletedValue;
    }


    // ── Identify The Roster Section Where The Deletion Happened ──────────────────
    // "Jedi Knights roster — that's the attribute section to refresh."
    // The listener uses this to find the right section of the attribute table.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute from which the value was deleted.
     * The attribute still exists in the entry (with its remaining values);
     * only this one value is gone.
     *
     * @return the {@link IAttribute} that was modified; never {@code null}.
     */
    public IAttribute getModifiedAttribute()
    {
        return modifiedAttribute;
    }


    // ── Retrieve The Fallen Value For The Memorial Log ────────────────────────────
    // "Obi-Wan Kenobi — the name that was removed."  The listener uses this to
    // identify and remove the specific row from the table.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value that was deleted from the attribute.
     * The object still exists in memory (this event holds a reference), but it
     * is no longer present in the LDAP directory or the attribute's value list.
     *
     * @return the deleted {@link IValue}; never {@code null}.
     */
    public IValue getDeletedValue()
    {
        return deletedValue;
    }


    // ── The Registry Logs: "Obi-Wan Removed From Jedi Roster" ───────────────────
    // "Deleted 'obi-wan@jedi.org' from 'mail' at 'cn=Leia,...'"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Deleted value 'foo' from 'mail' at 'cn=Leia,...'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__deleted_val_from_att_at_dn, new String[]
            { getDeletedValue().getStringValue(), getModifiedAttribute().getDescription(),
                getModifiedEntry().getDn().getName() } );
    }

}
