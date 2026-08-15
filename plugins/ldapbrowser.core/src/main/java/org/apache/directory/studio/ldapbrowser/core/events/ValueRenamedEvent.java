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
import org.apache.directory.studio.ldapbrowser.core.model.IValue;


// ── CLASS: ValueRenamedEvent — VADER DECLARES "NO, I AM YOUR FATHER" ─────────
// In the carbon-freeze chamber on Cloud City, Vader reveals the truth to Luke:
// "No — I am your father."  The value (the fact of Luke's parentage) hasn't
// changed — it was always Vader — but the label, the attribute type, has
// shifted.  "Your father" moved from the attribute "Obi-Wan's story" to the
// attribute "Vader's identity."  Same data, different column header.
// In LDAP terms: a value is "renamed" when its attribute TYPE changes — the
// raw value content stays the same but it moves from one attribute to another.
// For example, a value sitting under "uid" might be re-attributed to "cn"
// (same string, different column).  We carry both the old value (old type)
// and the new value (new type) so listeners can update the UI accordingly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an {@link IValue} was "renamed" — i.e. its attribute type was
 * changed, moving it from one attribute to another on the same {@link IEntry}.
 * The string content of the value may or may not change; what changed is which
 * attribute type owns it.  We store both the old value (under the old attribute
 * type) and the new value (under the new attribute type).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueRenamedEvent extends EntryModificationEvent
{

    /** The old value with the old attribute type. */
    private IValue oldValue;

    /** The new value with the new attribute type. */
    private IValue newValue;


    // ── Vader Records Both The Old And New Label For The Revelation ───────────────
    // "Old label: Obi-Wan's padawan — that's what the Rebellion believed.
    //  New label: Vader's son — that's the truth."
    // Both value objects are stored so the listener can clean up the old
    // attribute row and create a new row under the correct attribute type.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ValueRenamedEvent.
     *
     * <p>For example — a value is moved from the "uid" attribute to "cn":</p>
     * <pre>
     *   new ValueRenamedEvent(conn, userEntry, oldUidValue, newCnValue);
     * </pre>
     *
     * @param connection    the browser connection through which the change occurred.
     * @param modifiedEntry the LDAP entry that owns the value.
     * @param oldValue      the value as it was (with its original attribute type).
     * @param newValue      the value as it is now (with the new attribute type).
     */
    public ValueRenamedEvent( IBrowserConnection connection, IEntry modifiedEntry, IValue oldValue, IValue newValue )
    {
        super( connection, modifiedEntry );
        this.oldValue = oldValue;
        this.newValue = newValue;
    }


    // ── "He is your father now" — Retrieve The New Label ─────────────────────────
    // "Vader's son, attribute: family."  The listener needs the new value to
    // insert the correct attribute-type row and populate it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value under its new attribute type after the rename.
     * Use this to insert (or refresh) the table row for the new attribute.
     *
     * @return the new {@link IValue} with the updated attribute type; never {@code null}.
     */
    public IValue getNewValue()
    {
        return newValue;
    }


    // ── "He was 'Obi-Wan's padawan'" — Retrieve The Old Label ────────────────────
    // "Old label: padawan.  Remove that row."  The listener uses the old value
    // to find and remove the row that used to display under the original type.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value as it was under its original attribute type.
     * Use this to locate and remove the old row in the attribute table.
     *
     * @return the old {@link IValue} with the original attribute type; never {@code null}.
     */
    public IValue getOldValue()
    {
        return oldValue;
    }


    // ── Vader Logs The Identity Revelation ───────────────────────────────────────
    // "Renamed 'oldUid → newCn' at 'cn=Luke Skywalker,...'"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Renamed 'uid:luke' to 'cn:Luke Skywalker' at 'cn=Luke,...'".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__renamed_oldval_by_newval_at_dn, new String[]
            { getOldValue().toString(), getNewValue().toString(), getModifiedEntry().getDn().getName() } );
    }

}
