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


// ── CLASS: OlcOverlayConfig — Vader's Cybernetic Augmentations ───────────────
// After his defeat on Mustafar, Vader didn't replace himself — Palpatine bolted
// cybernetic augmentations onto what remained: life support, enhanced strength,
// and new capabilities all layered onto the existing Anakin Skywalker chassis.
// That's exactly what an OpenLDAP overlay does: it sits on top of a backend
// and adds new capabilities (logging, password policy, replication, etc.) without
// replacing the underlying data store. This is the base class all overlays extend.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base configuration bean for all OpenLDAP overlay types (olcOverlayConfig).
 * An overlay plugs into a database backend and intercepts or augments LDAP
 * operations — think of it as Vader's suit bolted onto the base chassis.
 * Concrete overlay classes (OlcPPolicyConfig, OlcAccessLogConfig, etc.) extend
 * this and add their specific configuration attributes.
 * Think of this class as the base cybernetic chassis that all augmentations share.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcOverlayConfig extends OlcConfig
{
    /**
     * Field for the 'olcDisabled' attribute. (Added in OpenLDAP 2.5.0)
     */
    @ConfigurationElement(attributeType = "olcDisabled", version="2.5.0")
    private Boolean olcDisabled;

    /**
     * Field for the 'olcOverlay' attribute.
     */
    @ConfigurationElement(attributeType = "olcOverlay", isOptional = false, isRdn = true, version="2.4.0")
    protected String olcOverlay;


    // ── Default Constructor — Vader's Empty Suit Awaits ───────────────────────────
    // The suit stands ready in Palpatine's medical bay — chassis assembled but
    // not yet activated, waiting for Anakin to be lowered in.
    // We need this blank constructor so subclasses and the I/O layer can instantiate
    // and then populate fields via setters.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty OlcOverlayConfig with no overlay type or disabled flag set.
     * Subclasses call this implicitly; the I/O layer uses it before populating fields.
     *
     * <p>For example — the suit stands empty, awaiting its occupant:</p>
     * <pre>
     *   OlcOverlayConfig overlay = new OlcOverlayConfig();
     *   overlay.setOlcOverlay( "ppolicy" );
     * </pre>
     */
    public OlcOverlayConfig()
    {
    }


    // ── Copy Constructor — Vader Replicates His Augmentation Profile ──────────────
    // Palpatine creates a duplicate record of Vader's augmentation configuration
    // so engineers can work on a copy without disturbing the live system.
    // We need deep copy semantics for safe editing and undo support.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a copy of an existing OlcOverlayConfig, duplicating the overlay name.
     * Subclasses call super(o) to copy base fields, then copy their own.
     *
     * <p>For example — Palpatine archives Vader's augmentation specs:</p>
     * <pre>
     *   OlcOverlayConfig copy = new OlcOverlayConfig( original );
     *   // copy.olcOverlay == original.olcOverlay, no shared references
     * </pre>
     *
     * @param o  the source OlcOverlayConfig to copy
     */
    public OlcOverlayConfig( OlcOverlayConfig o )
    {
        olcOverlay = o.olcOverlay;
    }


    // ── Get Disabled Flag — Check If Suit Is Powered Down ────────────────────────
    // The technician checks whether the life support systems are currently disabled.
    // A disabled overlay is still configured but tells slapd to skip it.
    // We expose this flag so the UI and I/O layer can read the current state.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this overlay is currently disabled.
     * When true, slapd will load the config entry but will not activate the overlay.
     * Useful for temporarily deactivating an overlay without deleting its config.
     *
     * <p>For example — the technician checks if the suit is powered down:</p>
     * <pre>
     *   if ( Boolean.TRUE.equals( overlay.getOlcDisabled() ) ) {
     *     // overlay is configured but sitting idle
     *   }
     * </pre>
     *
     * @return  true if disabled, false if active, null if not explicitly set (defaults to active)
     */
    public Boolean getOlcDisabled()
    {
        return olcDisabled;
    }


    // ── Get Overlay Name — Read the Augmentation Type Tag ────────────────────────
    // The technician reads the label stamped on the augmentation module to know
    // what capability it provides — "ppolicy", "accesslog", "syncprov", etc.
    // We expose the overlay type name for display and I/O serialization.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the olcOverlay type name — the short identifier for this overlay
     * (e.g., "ppolicy", "accesslog", "syncprov").
     * This is also the RDN attribute for the overlay entry in the DIT.
     *
     * <p>For example — the technician reads the module type label:</p>
     * <pre>
     *   String type = overlay.getOlcOverlay(); // "ppolicy"
     * </pre>
     *
     * @return  the overlay type string, or null if not set
     */
    public String getOlcOverlay()
    {
        return olcOverlay;
    }


    // ── Set Disabled Flag — Power Down the Augmentation ──────────────────────────
    // Palpatine flips the switch to take the overlay offline without removing it.
    // The module is still bolted on — it's just not running.
    // We store this flag so the I/O layer can serialize it back to LDAP.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the olcDisabled flag for this overlay.
     * Pass true to tell slapd to skip activating this overlay even though its config
     * entry exists. Pass false (or null) to keep it active.
     *
     * <p>For example — Palpatine powers down the module:</p>
     * <pre>
     *   overlay.setOlcDisabled( Boolean.TRUE ); // overlay sits idle
     * </pre>
     *
     * @param olcDisabled  true to disable, false to enable, null for default (enabled)
     */
    public void setOlcDisabled( Boolean olcDisabled )
    {
        this.olcDisabled = olcDisabled;
    }


    // ── Set Overlay Name — Install the Augmentation Module ───────────────────────
    // Palpatine's engineers stamp the module type onto the augmentation hardware
    // so everyone knows what capability this piece provides.
    // We set the overlay type identifier that becomes the RDN in the DIT.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the olcOverlay type name (e.g., "ppolicy", "accesslog").
     * This is the required RDN attribute for an overlay entry in the cn=config DIT.
     *
     * <p>For example — engineers stamp the module type:</p>
     * <pre>
     *   overlay.setOlcOverlay( "ppolicy" );
     * </pre>
     *
     * @param olcOverlay  the overlay type identifier string
     */
    public void setOlcOverlay( String olcOverlay )
    {
        this.olcOverlay = olcOverlay;
    }


    // ── Copy — Duplicate the Augmentation Config ──────────────────────────────────
    // Palpatine's engineering team produces a complete replica of the augmentation
    // spec so they can test changes without touching the live configuration.
    // We implement copy() for the I/O and edit layers to duplicate config objects safely.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcOverlayConfig.
     * Subclasses override this to also copy their specific fields.
     *
     * <p>For example — engineers duplicate the augmentation spec:</p>
     * <pre>
     *   OlcOverlayConfig copy = original.copy();
     *   copy.setOlcOverlay( "accesslog" ); // original unchanged
     * </pre>
     *
     * @return  a new OlcOverlayConfig that is a copy of this instance
     */
    public OlcOverlayConfig copy()
    {
        return new OlcOverlayConfig( this );
    }
}
