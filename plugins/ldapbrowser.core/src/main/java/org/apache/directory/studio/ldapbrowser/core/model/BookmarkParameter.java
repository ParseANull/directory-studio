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

package org.apache.directory.studio.ldapbrowser.core.model;


import java.io.Serializable;

import org.apache.directory.api.ldap.model.name.Dn;


// ── CLASS: BookmarkParameter — LANDO'S NAVICOMP WAYPOINT CARD ────────────────
// When Lando saves a hyperspace waypoint he needs two things: the actual
// galactic coordinates (the DN that locates the entry) and a friendly label
// so he knows what it is later ("Bespin docking bay 327").  He writes both
// on a card and slots it into the navicomp.  This card is serializable — it
// survives a power cycle and can be read back into the navicomp on the next
// boot.
// BookmarkParameter is that card: a plain, serializable bean holding the DN
// and the human-readable name of a bookmark.  Used as the backing store for
// IBookmark and persisted to XML when saving connections.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A serializable bean that holds the parameters of a bookmark (a saved link
 * to a specific LDAP entry).  Used to make bookmarks persistent across
 * sessions and to transfer bookmark data between the model and the UI.
 * Think of this as Lando's navicomp waypoint card — galactic coordinates
 * (DN) and a friendly name, all on one small record.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BookmarkParameter implements Serializable
{

    /** The serialVersionUID. */
    private static final long serialVersionUID = 105108281861642267L;

    /** The target Dn. */
    private Dn dn;

    /** The symbolic name. */
    private String name;


    // ── Lando Prepares A Blank Waypoint Card ─────────────────────────────────────
    // "Empty card — fill in the coordinates and label before using."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of BookmarkParameter.
     */
    public BookmarkParameter()
    {
    }


    // ── Lando Writes The Coordinates And Label On The Card At Once ───────────────
    // "Coordinates: uid=vader,dc=empire,dc=net.  Label: 'Dark Lord'."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of BookmarkParameter.
     *
     * @param dn   the target Dn (galactic coordinates to the entry).
     * @param name the symbolic name (friendly label for the UI).
     */
    public BookmarkParameter( Dn dn, String name )
    {
        this.dn = dn;
        this.name = name;
    }


    // ── Lando Reads The Waypoint Coordinates ─────────────────────────────────────
    // "Which DN does this bookmark point to?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the target Dn.
     *
     * @return the target Dn
     */
    public Dn getDn()
    {
        return dn;
    }


    // ── Lando Updates The Waypoint Coordinates ────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the target Dn.
     *
     * @param dn the target Dn
     */
    public void setDn( Dn dn )
    {
        this.dn = dn;
    }


    // ── Lando Reads The Friendly Label ────────────────────────────────────────────
    // "The human-readable name for this bookmark."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the symbolic name.
     *
     * @return the symbolic name
     */
    public String getName()
    {
        return name;
    }


    // ── Lando Updates The Friendly Label ──────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the symbolic name.
     *
     * @param name the symbolic name
     */
    public void setName( String name )
    {
        this.name = name;
    }

}
