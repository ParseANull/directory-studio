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
package org.apache.directory.studio.apacheds.configuration.jobs;


import java.util.UUID;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.api.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.partition.ldif.AbstractLdifPartition;


// ── CLASS: EntryBasedConfigurationPartition — THE IMPERIAL READING ROOM ──────
// Deep inside the Imperial archives, analysts set up a temporary reading room
// where a complete copy of the classified configuration documents is laid out
// in memory — every detail of the server's schematics available for inspection.
// This class is exactly that: an in-memory LDAP partition that holds all config
// entries so we can load, inspect, and diff them without touching live storage.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An in-memory LDAP partition that holds ApacheDS server configuration entries
 * during a load or save operation.
 * We keep every config entry alive in RAM here so the rest of the loading and
 * diffing code can query them repeatedly without hitting disk or a live server.
 * Think of this class as the Imperial archive's temporary reading room — a full
 * in-memory copy of the classified config documents, laid out for inspection
 * before any real write goes anywhere permanent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryBasedConfigurationPartition extends AbstractLdifPartition
{
    // ── Rebel Analyst Opens the Reading Room ────────────────────────────────
    // A Rebel analyst arrives at the makeshift archive and opens the door.
    // She hands over the schema manifest — the rulebook saying what attributes
    // are valid — so the room can be wired up properly before any docs go in.
    // This constructor passes the SchemaManager straight up to the superclass
    // so the partition knows the LDAP rules it must enforce from day one.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EntryBasedConfigurationPartition ready to hold config entries.
     * We pass the schema manager in up front so the partition knows how to
     * validate and store every LDAP entry we throw at it.
     *
     * @param schemaManager  the LDAP schema manager that defines valid attribute
     *                       types and object classes
     */
    public EntryBasedConfigurationPartition( SchemaManager schemaManager )
    {
        super( schemaManager );
    }


    // ── Archive Receives Its Official Designation ────────────────────────────
    // The Imperial librarian stamps the reading room with its official catalog
    // ID and marks the topmost filing slot — the suffix DN — so every entry
    // knows exactly where it lives in the directory hierarchy.
    // doInit() wires those two identifiers before handing off to the parent's
    // own startup routine to finish the rest of the initialisation sequence.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Initialises this partition by assigning it the id {@code "config"} and the
     * standard ApacheDS config suffix DN, then delegating to the superclass.
     * We need to set both values before the parent's init runs, otherwise the
     * partition won't know what root DN it's responsible for.
     *
     * @throws LdapException  if something in the parent's init sequence fails
     */
    protected void doInit() throws LdapException
    {
        setId( "config" ); //$NON-NLS-1$
        setSuffixDn( new Dn( ServerDNConstants.CONFIG_DN ) ); //$NON-NLS-1$

        super.doInit();
    }


    // ── Courier Files a Document in the Reading Room ─────────────────────────
    // A Rebel courier arrives with a data card and slides it into the correct
    // filing slot in the reading room.  Before it can be stored the archivist
    // makes sure the card carries the two mandatory Imperial stamps — without
    // them the catalog system will reject it outright.
    // addEntry() does exactly that: stamps the entry if needed, then commits it
    // to the in-memory partition via the standard LDAP add-operation path.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Stamps any missing mandatory operational attributes onto the entry, then
     * stores it in this partition.
     * Every LDAP entry needs a CSN (change sequence number) and a UUID before
     * the underlying store will accept it, so we add those here if they're absent.
     *
     * @param entry  the LDAP entry to add to the partition
     * @throws Exception  if the add operation fails (e.g. schema violation or duplicate DN)
     */
    public void addEntry( Entry entry ) throws Exception
    {
        // Adding mandatory operational attributes
        addMandatoryOpAt( entry );

        // Storing the entry
        add( new AddOperationContext( null, entry ) );
    }


    // ── Archivist Stamps the Required Imperial Seals ─────────────────────────
    // The Imperial archivist flips the data card over and checks for two seals:
    // the Change Sequence Number (CSN) and the Universally Unique Identifier.
    // If either seal is missing she stamps it on the spot, because the catalog
    // index will flatly refuse any document that lacks them.
    // This private helper makes that check and stamps the entry before storage.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Ensures the given entry carries both a CSN and a UUID operational attribute.
     * If either is missing we generate a fresh one and add it, because the
     * underlying LDAP store won't accept entries without those two fields.
     *
     * @param entry  the entry to stamp with the mandatory operational attributes
     * @throws LdapException  if adding the attribute to the entry fails
     */
    private void addMandatoryOpAt( Entry entry ) throws LdapException
    {
        // entryCSN
        if ( entry.get( SchemaConstants.ENTRY_CSN_AT ) == null )
        {
            entry.add( SchemaConstants.ENTRY_CSN_AT, defaultCSNFactory.newInstance().toString() );
        }

        // entryUUID
        if ( entry.get( SchemaConstants.ENTRY_UUID_AT ) == null )
        {
            String uuid = UUID.randomUUID().toString();
            entry.add( SchemaConstants.ENTRY_UUID_AT, uuid );
        }
    }
}
