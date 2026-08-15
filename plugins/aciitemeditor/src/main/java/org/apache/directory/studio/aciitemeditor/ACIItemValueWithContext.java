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
package org.apache.directory.studio.aciitemeditor;


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: ACIItemValueWithContext — THE IMPERIAL SECURITY BUREAU BRIEFING PACKET ──
// When Grand Moff Tarkin dispatches an officer to review security directives, he doesn't
// just hand over a raw clearance string — he provides the full briefing: which Star
// Destroyer, which sector, and what the current directive says. Without that context the
// officer can't verify anything against the live fleet roster.
// This class is that briefing packet: the ACI string, the directory connection, and the
// specific entry it belongs to, bundled together so the editor dialog has everything it needs.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Bundles an ACI item string together with the directory connection and LDAP entry
 * it belongs to, so the {@link ACIItemDialog} has enough context to parse and validate it.
 * Without the connection we can't browse the schema or look up DN values; without the
 * entry we don't know which object the ACI applies to.
 * Think of this as the Grand Moff's briefing packet — the raw directive plus the
 * fleet coordinates needed to make sense of it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemValueWithContext
{

    /** The connection, used to browse the directory. */
    private IBrowserConnection connection;

    /** The entry. */
    private IEntry entry;

    /** The ACI item. */
    private String aciItemValue;


    // ── THE BRIEFING PACKET IS ASSEMBLED ─────────────────────────────────────────
    // Grand Moff Tarkin sits at the command table on the Death Star, compiling a security
    // directive packet for his officers: sector coordinates, the target entry, and the
    // current ACI text are all sealed into a single pouch before dispatch.
    // Tarkin hands the sealed pouch to the officer — no field is left blank.
    // We do the same here: store connection, entry, and ACI string together so the
    // dialog always has everything it needs to open and validate the directive.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new context bundling an LDAP connection, a directory entry, and an ACI item string.
     * All three are required — the dialog can't do useful work without any one of them.
     *
     * <p>For example — Grand Moff Tarkin seals the briefing packet:</p>
     * <pre>
     *   connection  = the live link to the Death Star's directory
     *   entry       = the specific officer record being governed
     *   aciItemValue = the access-control directive as a raw string
     *   new ACIItemValueWithContext(connection, entry, aciItemValue)
     * </pre>
     *
     * @param connection  The browser connection used to resolve schema and DN lookups.
     * @param entry       The LDAP entry the ACI string belongs to.
     * @param aciItemValue  The ACI string itself — may be empty but never null.
     */
    public ACIItemValueWithContext( IBrowserConnection connection, IEntry entry, String aciItemValue )
    {
        this.connection = connection;
        this.entry = entry;
        this.aciItemValue = aciItemValue;
    }


    // ── THE OFFICER READS THE DIRECTIVE ──────────────────────────────────────────
    // The officer unseals the briefing pouch and reads the ACI directive aloud.
    // The text is the core of the packet — everything else is context for verifying it.
    // We return the raw ACI string so the editor can display or re-parse it.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw ACI item string stored in this context.
     * This is the string the editor will display and that gets round-tripped through
     * the parser when the user clicks OK.
     *
     * <p>For example — the officer reads the directive text from the sealed pouch:</p>
     * <pre>
     *   String aci = context.getACIItemValue();
     *   // aci == "{ identificationTag \"id1\", precedence 1, ... }"
     * </pre>
     *
     * @return the ACI item string; never null, but may be empty.
     */
    public String getACIItemValue()
    {
        return aciItemValue;
    }


    // ── THE OFFICER CHECKS THE FLEET LINK ────────────────────────────────────────
    // Before acting on the directive, the officer verifies that the comm channel to the
    // Death Star's main directory is live. Without it, no schema lookups can be made.
    // We return the browser connection so callers can query the LDAP schema or browse
    // entries while the ACI editor is open.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP browser connection associated with this context.
     * Used by value editors inside the ACI dialog to query the schema for valid attribute types.
     *
     * <p>For example — the officer checks the comm link before querying fleet records:</p>
     * <pre>
     *   IBrowserConnection conn = context.getConnection();
     *   Schema schema = conn.getSchema();
     * </pre>
     *
     * @return the browser connection; never null.
     */
    public IBrowserConnection getConnection()
    {
        return connection;
    }


    // ── THE OFFICER LOCATES THE TARGET ENTRY ─────────────────────────────────────
    // The briefing packet identifies the specific officer record on the roster that this
    // directive governs. The officer confirms they have the right file before proceeding.
    // We expose the entry so the ACI dialog can pre-populate fields and validate context.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP entry this ACI item belongs to.
     * The entry provides the DN context used by subtree and filter value editors.
     *
     * <p>For example — the officer confirms the target record in the fleet roster:</p>
     * <pre>
     *   IEntry entry = context.getEntry();
     *   Dn dn = entry.getDn(); // e.g. "cn=stormtrooper, ou=troops, dc=empire, dc=gal"
     * </pre>
     *
     * @return the LDAP entry; never null.
     */
    public IEntry getEntry()
    {
        return entry;
    }


    // ── THE PACKET IS SUMMARISED FOR LOGGING ─────────────────────────────────────
    // When the Grand Moff wants a quick status report, the officer reads out just the
    // directive text — not the full coordinates. A short summary is enough for the log.
    // We return the ACI string directly so this object can be printed in debug output.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ACI item string, or an empty string if it is null.
     * Useful for debug logging or displaying the value in a label.
     *
     * <p>For example — the officer gives a one-line status report to Tarkin:</p>
     * <pre>
     *   System.out.println("Current directive: " + context);
     *   // prints the raw ACI string, or "" if none set
     * </pre>
     *
     * @return the ACI string, or {@code ""} if null.
     */
    public String toString()
    {
        return aciItemValue == null ? "" : aciItemValue; //$NON-NLS-1$
    }

}
