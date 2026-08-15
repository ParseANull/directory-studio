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


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: Entry — ONE ROOM ON THE DEATH STAR BLUEPRINT ──────────────────────
// Every room on the Death Star (except the very top sector) has a parent
// corridor it belongs to and a name that makes it unique within that corridor.
// Entry is that room: a concrete AbstractEntry that stores a parent IEntry
// and an RDN.  The DN is computed on the fly by appending the RDN to the
// parent's DN — no redundant storage, just navigation up the tree.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete {@link AbstractEntry} implementation representing a regular
 * LDAP directory entry with a parent entry and an RDN.
 * The DN is computed dynamically as {@code parent.getDn().add(rdn)}.
 * The connection is inherited from the parent chain.
 *
 * <p>Think of this as one room on the Death Star blueprint — it knows its name
 * (RDN) and which corridor it hangs off (parent), and its full address (DN)
 * is derived by walking up to the root.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Entry extends AbstractEntry
{

    private static final long serialVersionUID = -4718107307581983276L;

    /** The Rdn. */
    protected Rdn rdn;

    /** The parent entry. */
    protected IEntry parent;


    // ── No-Arg Constructor For Serialisation ─────────────────────────────────────
    protected Entry()
    {
    }


    // ── Death Star Room Constructor: Parent + RDN Required ───────────────────────
    /**
     * Creates a new instance of Entry.
     *
     * @param parent the parent entry; must not be {@code null}
     * @param rdn the Rdn; must not be {@code null} or empty
     */
    public Entry( IEntry parent, Rdn rdn )
    {
        assert parent != null;
        assert rdn != null;
        assert !"".equals( rdn.toString() ); //$NON-NLS-1$

        this.parent = parent;
        this.rdn = rdn;
    }


    // ── Performance Opt: Return The Stored RDN Directly ──────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.impl.AbstractEntry#getRdn()
     */
    public Rdn getRdn()
    {
        // performance opt.
        return rdn;
    }


    // ── DN Is Derived By Appending RDN To Parent's DN ────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IEntry#getDn()
     */
    public Dn getDn()
    {
        try
        {
            Dn dn = parent.getDn().add( rdn );

            return dn;
        }
        catch ( LdapInvalidDnException lide )
        {
            return null;
        }
    }


    // ── Blueprint: Return The Parent Corridor ─────────────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IEntry#getParententry()
     */
    public IEntry getParententry()
    {
        return parent;
    }


    // ── Connection Is Inherited From The Parent Chain ─────────────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.IEntry#getBrowserConnection()
     */
    public IBrowserConnection getBrowserConnection()
    {
        return getParententry().getBrowserConnection();
    }


    // ── Blueprint: Update This Room's RDN (e.g. after a rename) ──────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.impl.AbstractEntry#setRdn(org.apache.directory.studio.ldapbrowser.core.model.RDN)
     */
    protected void setRdn( Rdn newRdn )
    {
        this.rdn = newRdn;
    }


    // ── Blueprint: Move This Room To A New Parent Corridor ───────────────────────
    /**
     * @see org.apache.directory.studio.ldapbrowser.core.model.impl.AbstractEntry#setParent(org.apache.directory.studio.ldapbrowser.core.model.IEntry)
     */
    protected void setParent( IEntry newParent )
    {
        this.parent = newParent;
    }

}
