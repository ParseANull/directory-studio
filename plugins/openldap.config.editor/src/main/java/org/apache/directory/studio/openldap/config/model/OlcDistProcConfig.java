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


// ── CLASS: OlcDistProcConfig — VADER'S DISTRIBUTED-COMMAND AUGMENTATION ──────
// After Mustafar, Palpatine's engineers bolt a distributed command interface onto
// Vader's life-support suit — allowing him to coordinate operations across multiple
// Star Destroyers at once instead of being tied to a single flagship. The
// distributed-procedure (distproc) overlay does the same for OpenLDAP: it bolts
// cross-server distributed operation capability onto a backend, letting the server
// chase and coordinate referrals across a cluster of LDAP servers.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcDistProcConfig} LDAP object class — models the
 * OpenLDAP distributed-procedure overlay configuration.
 * The distproc overlay extends a backend with the ability to transparently chain
 * and coordinate operations across multiple remote LDAP servers.
 * Think of this as the spec sheet for Vader's multi-fleet command augmentation:
 * whether to cache remote server URIs, and how to behave when chaining operations.
 * Used by the config editor and I/O layer to read/write the distproc overlay entry
 * under a database in cn=config.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcDistProcConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcChainCacheURI' attribute.
     */
    @ConfigurationElement(attributeType = "olcChainCacheURI", version="2.4.0")
    private Boolean olcChainCacheURI;

    /**
     * Field for the 'olcChainingBehavior' attribute.
     */
    @ConfigurationElement(attributeType = "olcChainingBehavior", version="2.4.0")
    private String olcChainingBehavior;


    // ── Powering Up The Empty Command Interface ────────────────────────────────────
    // Palpatine's engineers lay out the empty distributed-command chassis on the
    // operating table — nothing installed yet, just the mounting points.
    // The I/O layer needs this no-arg constructor to reflectively instantiate the
    // bean before populating it from the LDAP entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty OlcDistProcConfig with all fields unset.
     * The I/O layer calls this before populating from the LDAP entry via setters.
     *
     * <p>For example — the engineers lay out the empty command chassis:</p>
     * <pre>
     *   OlcDistProcConfig cfg = new OlcDistProcConfig();
     *   // all fields null — reader will fill them from the overlay LDAP entry
     * </pre>
     */
    public OlcDistProcConfig()
    {
        super();
    }


    // ── Cloning The Command Interface Spec ────────────────────────────────────────
    // When a second Vader-class unit needs the same distributed-command augmentation,
    // the engineers duplicate the spec sheet rather than starting from scratch —
    // an independent copy that can be reconfigured separately.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Copy constructor — creates an independent clone of the given OlcDistProcConfig,
     * copying both the URI cache flag and the chaining behavior string.
     * Changes to the copy won't affect the original.
     *
     * <p>For example — the engineers duplicate the augmentation spec:</p>
     * <pre>
     *   OlcDistProcConfig copy = new OlcDistProcConfig(original);
     *   copy.setOlcChainingBehavior("referrals");  // original unaffected
     * </pre>
     *
     * @param o  the instance to copy field values from
     */
    public OlcDistProcConfig( OlcDistProcConfig o )
    {
        super( o );
        olcChainCacheURI = o.olcChainCacheURI;
        olcChainingBehavior = o.olcChainingBehavior;
    }


    // ── Reading The URI Cache Module Status ───────────────────────────────────────
    // Vader's distributed command suit checks whether its URI targeting cache is
    // active — if it is, it reuses known remote server addresses instead of
    // re-resolving them every time it needs to dispatch a cross-fleet command.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code olcChainCacheURI} flag — whether the distproc overlay
     * caches URIs for remote LDAP servers it has already contacted.
     * Caching saves the overhead of re-resolving referral target URIs on every operation.
     *
     * <p>For example — Vader's suit checks the URI-cache module status:</p>
     * <pre>
     *   if (cfg.getOlcChainCacheURI()) {
     *     // reuse cached connection to the remote server
     *   }
     * </pre>
     *
     * @return  {@code true} if URI caching is enabled, or {@code null} if not configured
     */
    public Boolean getOlcChainCacheURI()
    {
        return olcChainCacheURI;
    }


    // ── Reading The Cross-Fleet Engagement Rules ───────────────────────────────────
    // The Emperor checks the standing engagement rules loaded into Vader's command
    // interface — under what conditions should he chain operations to a remote fleet,
    // and how aggressively should he pursue those referrals.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code olcChainingBehavior} string — the directive controlling when
     * and how the distproc overlay chains LDAP operations to remote servers.
     *
     * <p>For example — the Emperor reads Vader's engagement-rule directive:</p>
     * <pre>
     *   String behavior = cfg.getOlcChainingBehavior();
     *   // e.g. "referrals" → chain all referral responses transparently
     * </pre>
     *
     * @return  the chaining behavior string, or {@code null} if not configured
     */
    public String getOlcChainingBehavior()
    {
        return olcChainingBehavior;
    }


    // ── Installing The URI Cache Module ───────────────────────────────────────────
    // Palpatine's engineers install (or uninstall) the URI-targeting cache module
    // in Vader's command interface — a deliberate upgrade decision that affects
    // how efficiently the suit connects to remote fleets.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code olcChainCacheURI} flag. Pass {@code true} to enable caching of
     * referral target URIs, {@code false} to disable, or {@code null} to leave unconfigured.
     *
     * <p>For example — engineers toggle the URI-cache module:</p>
     * <pre>
     *   cfg.setOlcChainCacheURI(true);  // cache remote server URIs
     * </pre>
     *
     * @param olcChainCacheURI  the URI caching flag to set
     */
    public void setOlcChainCacheURI( Boolean olcChainCacheURI )
    {
        this.olcChainCacheURI = olcChainCacheURI;
    }


    // ── Uploading Updated Engagement Rules ────────────────────────────────────────
    // The Emperor transmits updated standing orders to Vader's command interface —
    // new rules about when to initiate cross-fleet operations and how to handle
    // referral responses from remote servers.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code olcChainingBehavior} string — the directive controlling when
     * the distproc overlay will chain operations to remote LDAP servers.
     *
     * <p>For example — the Emperor uploads updated engagement rules:</p>
     * <pre>
     *   cfg.setOlcChainingBehavior("referrals critical");
     *   // overlay will chain all referrals and fail loudly if the remote is unreachable
     * </pre>
     *
     * @param olcChainingBehavior  the chaining behavior directive to set
     */
    public void setOlcChainingBehavior( String olcChainingBehavior )
    {
        this.olcChainingBehavior = olcChainingBehavior;
    }


    // ── Duplicating The Full Augmentation Spec ────────────────────────────────────
    // Before the Empire ships Vader's command interface spec to a remote facility,
    // they produce a full independent duplicate — so both sides can work from their
    // own copy without interfering with each other's modifications.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates and returns an independent copy of this OlcDistProcConfig, including
     * both configuration fields. Useful for the editor's undo/redo stack.
     *
     * <p>For example — the Empire duplicates the command-interface spec:</p>
     * <pre>
     *   OlcDistProcConfig snapshot = cfg.copy();
     *   // snapshot is fully independent — editing it won't affect the original
     * </pre>
     *
     * @return  a new OlcDistProcConfig with the same field values as this instance
     */
    @Override
    public OlcDistProcConfig copy()
    {
        return new OlcDistProcConfig( this );
    }
}
