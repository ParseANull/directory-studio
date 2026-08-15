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
package org.apache.directory.studio.openldap.config.jobs;


// ── CLASS: EntryBasedConfigurationPartition — The Emperor's Sealed Config Archive
// The Emperor's most sensitive configuration data is locked in a sealed archive
// held beneath the Imperial Palace.  EntryBasedConfigurationPartition is that
// archive — an in-memory LDIF partition rooted at "cn=config" that the editor
// populates by calling addEntry.  Before storing each entry the archivist stamps
// it with two mandatory operational attributes (entryCSN and entryUUID) so the
// archive is consistent with the rest of the directory's operational metadata.
// ─────────────────────────────────────────────────────────────────────────────
import java.util.UUID;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.core.api.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.partition.ldif.AbstractLdifPartition;


/**
 * This class implements a read-only configuration partition.
 * It is an in-memory LDIF partition rooted at "cn=config" that the
 * OpenLDAP configuration editor populates by adding entries one by one.
 * Before each entry is stored, mandatory operational attributes
 * (entryCSN, entryUUID) are injected if absent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryBasedConfigurationPartition extends AbstractLdifPartition
{
    // ── Constructor — Seal the Archive at the Given Suffix ────────────────────
    // The archivist initializes the partition under the given schema manager
    // and sets its root suffix DN.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of EntryBasedConfigurationPartition.
     *
     * @param schemaManager the schema manager
     * @param suffixDn the suffix DN
     */
    public EntryBasedConfigurationPartition( SchemaManager schemaManager, Dn suffixDn )
    {
        super( schemaManager );
        this.suffixDn = suffixDn;
    }


    // ── doInit — Assign the Partition ID and Suffix ───────────────────────────
    // On initialization the archivist fixes the partition ID to "config" and
    // the suffix to "cn=config", then delegates to the superclass setup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInit() throws LdapException
    {
        setId( "config" );
        setSuffixDn( new Dn( "cn=config" ) );

        super.doInit();
    }


    // ── addEntry — Stamp and Store a Config Entry ──────────────────────────────
    // Before placing an entry in the archive, the archivist ensures it carries
    // the mandatory entryCSN and entryUUID operational attributes, then calls
    // the underlying add operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the given entry.
     *
     * @param entry
     *      the entry
     * @throws Exception
     */
    public void addEntry( Entry entry ) throws Exception
    {
        // Adding mandatory operational attributes
        addMandatoryOpAt( entry );

        // Storing the entry
        add( new AddOperationContext( null, entry ) );
    }


    // ── addMandatoryOpAt — Inject Missing CSN and UUID ───────────────────────
    // If the entry has no entryCSN the archivist generates a new one from the
    // default CSN factory.  If it has no entryUUID the archivist generates a
    // fresh random UUID.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the CSN and UUID attributes to the entry if they are not present.
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
