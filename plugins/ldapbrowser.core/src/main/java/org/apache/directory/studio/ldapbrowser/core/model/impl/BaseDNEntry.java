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
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: BaseDNEntry — THE TOP LEVEL SECTOR OF THE DEATH STAR ──────────────
// The Death Star blueprint has a top-level sector that has no parent sector —
// it is the root of the whole structure.  BaseDNEntry is that root: an entry
// whose parent is always the RootDSE rather than another real entry, and whose
// DN is the configured base DN of the connection.  It cannot have its RDN or
// parent changed after construction.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents an LDAP entry at the root of a naming context (a base DN),
 * i.e. an entry that has no logical parent entry other than the
 * {@link RootDSE}.  The DN and connection are fixed at construction time.
 *
 * <p>Think of this as the top-level sector of the Death Star blueprint —
 * there is nothing above it in the directory tree.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BaseDNEntry extends AbstractEntry
{

    private static final long serialVersionUID = -5444229580355372176L;

    /** The base Dn. */
    protected Dn baseDn;

    /** The browser connection. */
    protected IBrowserConnection browserConnection;


    // ── No-Arg Constructor For Serialisation ─────────────────────────────────────
    protected BaseDNEntry()
    {
    }


    // ── Root Sector Constructor: DN + Connection Required ────────────────────────
    /**
     * Creates a new instance of BaseDNEntry.
     *
     * @param baseDn the base Dn of this naming context root; must not be {@code null}
     * @param browserConnection the browser connection this entry belongs to; must not be {@code null}
     */
    public BaseDNEntry( Dn baseDn, IBrowserConnection browserConnection )
    {
        assert baseDn != null;
        assert browserConnection != null;

        this.setDirectoryEntry( true );
        this.baseDn = baseDn;
        this.browserConnection = browserConnection;
    }


    // ── Blueprint: Return This Sector's Base DN ───────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IEntry#getDn()
     */
    public Dn getDn()
    {
        return baseDn;
    }


    // ── Blueprint: Parent Is Always The RootDSE ───────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IEntry#getParententry()
     */
    public IEntry getParententry()
    {
        return getBrowserConnection().getRootDSE();
    }


    // ── Blueprint: Return The Connection Owning This Sector ───────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IEntry#getBrowserConnection()
     */
    public IBrowserConnection getBrowserConnection()
    {
        return browserConnection;
    }


    // ── Base DN Cannot Be Re-Parented By RDN Change — No-Op ──────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.impl.AbstractEntry#setRdn(org.apache.directory.studio.ldapbrowser.core.model.RDN)
     */
    protected void setRdn( Rdn newRdn )
    {
    }


    // ── Base DN Cannot Be Re-Parented — No-Op ────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.impl.AbstractEntry#setParent(org.apache.directory.studio.ldapbrowser.core.model.IEntry)
     */
    protected void setParent( IEntry newParent )
    {
    }

}
