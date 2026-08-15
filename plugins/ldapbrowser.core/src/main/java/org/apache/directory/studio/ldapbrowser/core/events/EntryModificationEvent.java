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


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: EntryModificationEvent — OBI-WAN SENSES A DISTURBANCE IN THE FORCE ─
// Obi-Wan Kenobi, meditating in his Tatooine hermitage, suddenly grips the
// wall — "I felt a great disturbance in the Force, as if millions of voices
// suddenly cried out in terror."  He doesn't yet know whether it's an explosion,
// a death, or a battle — only that *something* changed somewhere significant.
// This class is that initial disturbance notice: the base event that every
// specific LDAP-model change extends.  Listeners receive one of these and
// can inspect the connection and entry to learn what planet just exploded.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The root base class for all events that signal an {@link IEntry} modification.
 * Every specific event (attribute added, entry deleted, value renamed, etc.)
 * extends this class so that a single {@link EntryUpdateListener} can receive
 * all change notifications through one callback.
 * Think of this as Obi-Wan's disturbance: you know something happened to
 * an entry on a connection, but you have to look at the concrete subclass
 * to find out what.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryModificationEvent
{
    /** The event source */
    protected Object source;

    /** The connection. */
    protected IBrowserConnection connection;

    /** The entry. */
    protected IEntry modifiedEntry;


    // ── Obi-Wan Pinpoints The Planet From The Tremor ─────────────────────────────
    // The disturbance in the Force has a location: Alderaan, at this exact
    // co-ordinate in the galaxy, on connection "Leia's channel".
    // Obi-Wan records which star system rippled — connection + entry — so his
    // message to the Rebellion names the right place.
    // We capture the same two facts: which LDAP connection was active and which
    // entry changed, so any listener can act on precise information.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EntryModificationEvent, recording the connection on which
     * the change happened and the entry that was modified.
     * All concrete event subclasses call this constructor via {@code super(...)}.
     *
     * <p>For example — Obi-Wan records the disturbance location:</p>
     * <pre>
     *   connection  = "Alderaan's relay channel";   // which LDAP server
     *   modifiedEntry = entryAt("cn=Alderaan,..."); // which entry changed
     * </pre>
     *
     * @param connection    the browser connection through which the change occurred.
     * @param modifiedEntry the LDAP entry that was modified.
     */
    public EntryModificationEvent( IBrowserConnection connection, IEntry modifiedEntry )
    {
        this.connection = connection;
        this.modifiedEntry = modifiedEntry;
    }


    // ── Obi-Wan Identifies The Active Comlink Channel ────────────────────────────
    // "Which channel was transmitting when Alderaan went silent?" — Obi-Wan
    // checks his comlink log to identify the connection used.
    // A listener might support multiple LDAP servers; this getter tells it
    // which server's entry just changed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser connection on which this event occurred.
     * Useful when a single listener is registered against multiple connections
     * and needs to route the event to the right place.
     *
     * <p>For example — checking which server fired:</p>
     * <pre>
     *   if (event.getConnection() == myActiveConnection) { refresh(); }
     * </pre>
     *
     * @return the {@link IBrowserConnection} associated with this event; never {@code null}.
     */
    public IBrowserConnection getConnection()
    {
        return connection;
    }


    // ── Obi-Wan Identifies The Planet That Cried Out ─────────────────────────────
    // "It was Alderaan — I can still feel its echo."
    // Obi-Wan identifies the specific planet (entry) that produced the
    // disturbance so the Rebellion knows where to focus its response.
    // Listeners call this to get the exact LDAP entry that was touched.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP entry that was modified.
     * For move/rename events this is the new entry (or the parent entry);
     * for delete events it is the entry that no longer exists.
     *
     * <p>For example — updating the UI for the right entry:</p>
     * <pre>
     *   IEntry entry = event.getModifiedEntry();
     *   viewer.refresh(entry);
     * </pre>
     *
     * @return the modified {@link IEntry}; never {@code null}.
     */
    public IEntry getModifiedEntry()
    {
        return modifiedEntry;
    }


    // ── Obi-Wan Traces The Signal Back To Its Origin ─────────────────────────────
    // When the disturbance echoes through the Force, Obi-Wan can sometimes
    // sense who triggered it — a Jedi, a weapon, a specific ship.
    // The source field records the object that fired this event, so listeners
    // can skip re-processing their own changes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the object that fired this event.
     * The source is set by {@link EventRegistry#fireEntryUpdated} before
     * dispatching, so listeners can check whether they themselves were the
     * originator and skip redundant work.
     *
     * <p>For example — ignoring self-fired events:</p>
     * <pre>
     *   if (event.getSource() == this) { return; } // we did it, skip refresh
     * </pre>
     *
     * @return the source object, or {@code null} if not yet set.
     */
    public Object getSource()
    {
        return source;
    }


    // ── Obi-Wan Stamps The Signal With Its Origin ────────────────────────────────
    // Before relaying the disturbance to the Rebellion, Obi-Wan identifies
    // himself as the one who sensed and reported it — "This signal comes from
    // Obi-Wan Kenobi, transmitted from Tatooine."
    // The EventRegistry calls this just before firing so listeners can see
    // who originated the change.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the source object for this event.
     * Called by {@link EventRegistry#fireEntryUpdated} immediately before
     * dispatching to all listeners.  Not normally called by application code.
     *
     * <p>For example — EventRegistry stamps the source:</p>
     * <pre>
     *   entryUpdateEvent.setSource(callerObject);
     *   entryUpdateEventManager.fire(factory);
     * </pre>
     *
     * @param source the object that triggered this event; may be {@code null}.
     */
    public void setSource( Object source )
    {
        this.source = source;
    }

}
