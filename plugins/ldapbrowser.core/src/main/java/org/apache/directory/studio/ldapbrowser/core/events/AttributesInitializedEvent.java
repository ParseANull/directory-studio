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
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: AttributesInitializedEvent — OBI-WAN'S FORCE GHOST FIRST APPEARS ─
// After Obi-Wan becomes one with the Force, he appears to Luke for the first
// time on Hoth: "Luke... I don't believe it."  For Luke, Obi-Wan's full
// presence — knowledge, wisdom, guidance — is suddenly available where before
// there was only the faint whisper of a name.
// This event fires when a job fetches ALL the attributes for an entry from the
// LDAP server for the first time.  Before this fires, the entry was a shell —
// its attributes hadn't been loaded.  After it fires, the entry's full data
// is in memory and the UI can render the complete attribute list.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that the {@link IAttribute}s of an {@link IEntry} were loaded from
 * the LDAP directory for the first time (or refreshed after a reload).
 * We fire this after a background job completes an LDAP search that populates
 * the entry's attribute map.  Listeners — typically the attribute table viewer —
 * respond by populating or refreshing their display.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributesInitializedEvent extends EntryModificationEvent
{

    // ── Obi-Wan Materialises Fully At The Entry's Location ───────────────────────
    // Luke doesn't have to specify connection and entry separately — Obi-Wan's
    // ghost simply appears at Luke's location, bringing everything with him.
    // The constructor takes just the entry and extracts the connection from it,
    // so callers don't have to pass both separately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new AttributesInitializedEvent for the given entry.
     * We pull the connection straight from the entry itself, so you only
     * need to pass the entry.
     *
     * <p>For example — fired after a background attribute-load job finishes:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new AttributesInitializedEvent(loadedEntry), source);
     * </pre>
     *
     * @param initializedEntry the entry whose attributes were just loaded from
     *                         the directory.
     */
    public AttributesInitializedEvent( IEntry initializedEntry )
    {
        super( initializedEntry.getBrowserConnection(), initializedEntry );
    }


    // ── Obi-Wan Announces His Full Presence Has Arrived ──────────────────────────
    // Luke's comlink log reads: "Force ghost of Obi-Wan Kenobi appeared at
    // Hoth, entry: Luke Skywalker."  This is the readable summary of the event.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return a localised string like "Attributes of 'cn=John,dc=example,dc=com' initialized".
     */
    public String toString()
    {
        return BrowserCoreMessages.bind( BrowserCoreMessages.event__dn_attributes_initialized, new String[]
            { getModifiedEntry().getDn().getName() } );
    }

}
