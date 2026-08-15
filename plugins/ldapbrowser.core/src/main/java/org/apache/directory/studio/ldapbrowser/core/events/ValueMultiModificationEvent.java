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


// ── CLASS: ValueMultiModificationEvent — REY'S VISION IN MAZ'S BASEMENT ─────
// In Maz Kanata's castle, Rey touches the Skywalker lightsaber and is hit by
// a cascade of Force visions: Vader, young Luke, the Knights of Ren, a snowy
// forest, Jakku, all at once.  There's no single clean event — everything
// changed simultaneously in a torrent she can barely process.
// This event fires when multiple values on a single entry have been added,
// modified, deleted, and/or renamed in one batch operation.  Rather than
// flooding the event bus with a dozen individual value events, we fire one
// ValueMultiModificationEvent and let listeners do a full attribute refresh.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that multiple {@link IValue}s on an {@link IEntry} were added,
 * modified, deleted, and/or renamed in a single batch.
 * We fire this instead of several individual events when the entry's value
 * landscape has changed in too many ways to enumerate efficiently.  Listeners
 * should treat this as "the entry's values changed significantly — reload everything."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueMultiModificationEvent extends EntryModificationEvent
{

    // ── Rey Grounds Herself In The Entry That Changed ────────────────────────────
    // Even in the middle of a cascade of visions, Rey clings to one anchor:
    // the entry that caused the cascade — "it came from this lightsaber, this
    // connection."  Connection and entry are the only stable co-ordinates when
    // multiple things changed at once.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ValueMultiModificationEvent.
     * Only the connection and the modified entry are needed — the specific
     * values that changed are not tracked individually; listeners should
     * reload the entry's attribute list in full.
     *
     * <p>For example — fired after a paste or script modifies several values:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new ValueMultiModificationEvent(conn, modifiedEntry), source);
     * </pre>
     *
     * @param connection    the browser connection through which the changes occurred.
     * @param modifiedEntry the entry whose values were modified in bulk.
     */
    public ValueMultiModificationEvent( IBrowserConnection connection, IEntry modifiedEntry )
    {
        super( connection, modifiedEntry );
    }


    // ── Rey Summarises The Cascade For The Log ────────────────────────────────────
    // "Too many visions to list.  Bulk modification — reload everything."
    // The toString mirrors the BulkModificationEvent message because both
    // convey the same message: something significant happened, refresh the view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable description of this event, suitable for logs and
     * the status bar.
     *
     * @return the localised "bulk modification" string; never {@code null}.
     */
    public String toString()
    {
        return BrowserCoreMessages.event__bulk_modification;
    }

}
