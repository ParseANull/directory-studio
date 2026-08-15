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

package org.apache.directory.studio.ldapbrowser.core.model.impl;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;


// ── CLASS: DirectoryMetadataEntry — JEDI ARCHIVES SPECIAL SECTOR ─────────────
// The Jedi Archives has certain special vaults that are listed in the root
// catalogue but don't have children in the normal sense — the schema vault,
// the monitorContext, the configContext.  DirectoryMetadataEntry marks those
// special entries.  It extends BaseDNEntry and adds a flag to say "this one is
// the schema entry" so the UI can render it differently and skip loading its
// children when it is the schema sub-entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a special directory metadata entry that is listed in the root DSE,
 * such as the schema sub-entry, monitorContext, or configContext.
 * Extends {@link BaseDNEntry} with a {@code schemaEntry} flag that causes
 * {@link #hasChildren()} to return {@code false} when this entry is the
 * connection's schema DN.
 *
 * <p>Think of this as the Jedi Archives' special restricted vault — present in
 * the index but treated differently from regular entries.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DirectoryMetadataEntry extends BaseDNEntry
{

    private static final long serialVersionUID = 1340597532850853276L;

    /** The schema entry flag. */
    private boolean schemaEntry;


    // ── No-Arg Constructor For Serialisation ─────────────────────────────────────
    protected DirectoryMetadataEntry()
    {
    }


    // ── Jedi Archives: Create A Metadata Vault Entry ─────────────────────────────
    /**
     * Creates a new instance of DirectoryMetadataEntry.
     *
     * @param dn the Dn of this metadata entry
     * @param browserConnection the browser connection this entry belongs to
     */
    public DirectoryMetadataEntry( Dn dn, IBrowserConnection browserConnection )
    {
        super();
        this.baseDn = dn;
        this.browserConnection = browserConnection;
        this.schemaEntry = false;
    }


    // ── Mace Windu: Schema Vaults Have No Children In The Tree ───────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.impl.AbstractEntry#hasChildren()
     */
    public boolean hasChildren()
    {
        if ( getDn().equals( getBrowserConnection().getSchema().getDn() ) )
        {
            return false;
        }
        else
        {
            return super.hasChildren();
        }
    }


    // ── Jedi Archives: Is This The Schema Sub-Entry? ─────────────────────────────
    /**
     * Checks if is schema entry.
     *
     * @return {@code true} if this entry is the connection's schema sub-entry
     */
    public boolean isSchemaEntry()
    {
        return schemaEntry;
    }


    // ── Jedi Archives: Mark This Entry As The Schema Sub-Entry ───────────────────
    /**
     * Sets the schema entry flag.
     *
     * @param schemaEntry {@code true} to mark this entry as the schema sub-entry
     */
    public void setSchemaEntry( boolean schemaEntry )
    {
        this.schemaEntry = schemaEntry;
    }

}
