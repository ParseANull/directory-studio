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
package org.apache.directory.studio.openldap.config.model;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.name.Dn;


// ── CLASS: OlcConfig — THE MASTER DEATH STAR BLUEPRINT ───────────────────────
// The Death Star has a master schematic that every sub-section — the superlaser,
// the hangar bays, the detention block — is derived from. It defines the common
// structure: where the entry lives in the tree (its parent DN) and what auxiliary
// object classes are mixed in on top of its structural class. Every specific config
// bean (global settings, databases, overlays) inherits from this root just like
// every Death Star section inherits from that master schematic.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base class for all OpenLDAP {@code olcConfig} bean types in the cn=config tree.
 * It holds the two things every config entry has in common: where it lives
 * (its parent DN) and any auxiliary LDAP object classes mixed in alongside its
 * structural class.
 * Think of this as the master Death Star blueprint — every specific section
 * (global, database, overlay) extends it with its own detail pages.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcConfig
{
    /** The parent DN of the associated entry */
    protected Dn parentDn;

    /** The list of auxiliary object classes */
    protected List<AuxiliaryObjectClass> auxiliaryObjectClasses = new ArrayList<>();


    // ── Bolting On Extra Schema Mix-ins ───────────────────────────────────────────
    // The Death Star construction team bolts supplementary modules — shield arrays,
    // tractor beam housings — onto the base station frame one by one. Auxiliary
    // object classes work the same way: they're schema mix-ins that add extra
    // allowed attributes on top of the entry's structural class.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds one or more auxiliary object class descriptors to this config entry.
     * Auxiliary object classes in LDAP let you add extra attributes to an entry
     * beyond what its structural class allows — think of them as schema mix-ins.
     *
     * <p>For example — construction team attaches supplementary modules to the base frame:</p>
     * <pre>
     *   config.addAuxiliaryObjectClasses(new OlcFrontendConfig());
     *   // now the entry also carries frontend-specific attributes
     * </pre>
     *
     * @param auxiliaryObjectClasses  one or more auxiliary class instances to attach
     */
    public void addAuxiliaryObjectClasses( AuxiliaryObjectClass... auxiliaryObjectClasses )
    {
        for ( AuxiliaryObjectClass auxiliaryObjectClass : auxiliaryObjectClasses )
        {
            this.auxiliaryObjectClasses.add( auxiliaryObjectClass );
        }
    }


    // ── Reviewing The Full List Of Attached Modules ───────────────────────────────
    // Grand Moff Tarkin reviews the complete inventory of supplementary modules
    // bolted onto the Death Star before the inspection tour — he gets a copy of the
    // list, not the master, so his notes can't accidentally alter the station design.
    // We return a defensive copy for exactly the same reason.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the list of auxiliary object class instances attached
     * to this config entry. We return a copy so callers can't accidentally mutate our
     * internal list.
     *
     * <p>For example — Tarkin reviews a copy of the module inventory:</p>
     * <pre>
     *   List&lt;AuxiliaryObjectClass&gt; aux = config.getAuxiliaryObjectClasses();
     *   // safe to iterate or filter — modifying aux won't affect the real list
     * </pre>
     *
     * @return  a fresh defensive copy of the auxiliary object class list
     */
    public List<AuxiliaryObjectClass> getAuxiliaryObjectClasses()
    {
        List<AuxiliaryObjectClass> copy = new ArrayList<>( auxiliaryObjectClasses.size() );
        copy.addAll( auxiliaryObjectClasses );

        return copy;
    }


    // ── Counting The Bolted-On Modules ────────────────────────────────────────────
    // The station logistics officer quickly tallies how many supplementary modules
    // are attached before filing the manifest — no need to review each one, just
    // the count. We expose this for the same reason: fast checks without copying
    // the whole list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of auxiliary object classes currently attached to this config entry.
     * Handy for quick size checks without the overhead of copying the list.
     *
     * <p>For example — the logistics officer tallies the bolt-on count:</p>
     * <pre>
     *   int count = config.getAuxiliaryObjectClassesSize();
     *   if (count == 0) { /* no mix-ins attached *&#47; }
     * </pre>
     *
     * @return  the number of auxiliary object class instances attached
     */
    public int getAuxiliaryObjectClassesSize()
    {
        return auxiliaryObjectClasses.size();
    }


    // ── Locating The Section In The Station's Blueprint Tree ──────────────────────
    // Every section of the Death Star is placed at a specific location in the
    // station's hierarchical layout — "Deck 7, Section 4-Alpha." The parent DN is
    // exactly that: the location of this config entry's parent in the cn=config tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent DN of the LDAP entry this config bean corresponds to.
     * In the cn=config tree, every entry hangs under a parent — global settings hang
     * under "cn=config", databases hang under "cn=config" too, overlays hang under
     * their database entry. This DN tracks that location.
     *
     * <p>For example — locating the section in the station layout:</p>
     * <pre>
     *   Dn parent = config.getParentDn();
     *   // e.g. Dn("cn=config") for a top-level database entry
     * </pre>
     *
     * @return  the parent DN, or {@code null} if not yet set
     */
    public Dn getParentDn()
    {
        return parentDn;
    }


    // ── Filing The Section's Location In The Tree ─────────────────────────────────
    // When a new section is added to the Death Star blueprint, the chief architect
    // records exactly where it sits in the station hierarchy. We do the same when
    // the I/O layer reads a config entry — it sets the parent DN so we know where
    // this bean lives in the cn=config tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the parent DN of the LDAP entry this config bean corresponds to.
     * The reader calls this after loading the entry so the bean knows where it sits
     * in the cn=config tree — important for writing changes back to the correct location.
     *
     * <p>For example — the architect files the section's location:</p>
     * <pre>
     *   config.setParentDn(new Dn("cn=config"));
     *   // bean now knows its entry lives directly under cn=config
     * </pre>
     *
     * @param parentDn  the parent DN to record
     */
    public void setParentDn( Dn parentDn )
    {
        this.parentDn = parentDn;
    }


    // ── Duplicating A List Of Blueprint Annotations ───────────────────────────────
    // When the Death Star archive sends a section's annotation list to a remote
    // facility, they send a clean copy — changes at the remote site must not bleed
    // back into the archive's master. This utility handles that safe copy for all
    // the String lists our subclasses carry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Utility method that copies a {@code List<String>} into a fresh list, or returns
     * an empty list if the input is {@code null}. Used by subclasses (like OlcGlobal)
     * to implement defensive-copy getters and setters without duplicating this logic
     * in every subclass.
     *
     * <p>For example — the archive sends a clean copy of the annotations:</p>
     * <pre>
     *   List&lt;String&gt; copy = copyListString(original);
     *   // safe to hand to callers — mutations won't reach back into original
     * </pre>
     *
     * @param original  the list to copy, may be {@code null}
     * @return          a new list with the same strings, or an empty list if original was {@code null}
     */
    protected List<String> copyListString( List<String> original )
    {
        if ( original != null )
        {
            List<String> copy = new ArrayList<>( original.size() );
            copy.addAll( original );

            return copy;
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
