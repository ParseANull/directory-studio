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


// ── CLASS: OlcChainConfig — VADER'S REFERRAL-CHAINING AUGMENTATION ────────────
// After Vader's near-death at Mustafar, Palpatine's engineers bolt cybernetic limbs
// and a life-support system onto him — augmentations that extend what his biological
// base could do on its own. The chain overlay works exactly like that: it bolts
// referral-chaining capability onto a plain LDAP backend, so that when a client
// follows a referral, the server chases it transparently instead of just handing
// the referral URL back to the client. This class holds the configuration for those
// augmentation parameters.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcChainConfig} LDAP object class — models the OpenLDAP
 * chain overlay configuration, which makes slapd transparently follow LDAP referrals
 * on behalf of clients instead of returning them raw.
 * Think of this class as the spec sheet for Vader's life-support augmentation: how
 * aggressively it chases referrals, how deep it goes, and what to do when things go wrong.
 * Used by the config editor and the I/O layer to read/write the chain overlay entry
 * under a database in cn=config.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcChainConfig extends OlcOverlayConfig
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

    /**
     * Field for the 'olcChainMaxReferralDepth' attribute.
     */
    @ConfigurationElement(attributeType = "olcChainMaxReferralDepth", version="2.4.0")
    private Integer olcChainMaxReferralDepth;

    /**
     * Field for the 'olcChainReturnError' attribute.
     */
    @ConfigurationElement(attributeType = "olcChainReturnError", version="2.4.0")
    private Boolean olcChainReturnError;


    // ── Powering Up The Empty Life-Support Shell ──────────────────────────────────
    // Palpatine's engineers first lay out the empty life-support chassis on the
    // operating table before any components are installed. We need this default
    // constructor so the I/O layer can reflectively instantiate the bean and then
    // populate its fields from the LDAP entry one attribute at a time.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty OlcChainConfig instance with all augmentation fields unset.
     * The I/O layer calls this before calling setters to populate from LDAP.
     *
     * <p>For example — the engineers lay out the empty chassis:</p>
     * <pre>
     *   OlcChainConfig chain = new OlcChainConfig();
     *   // all fields null — reader will fill them from the overlay LDAP entry
     * </pre>
     */
    public OlcChainConfig()
    {
        super();
    }


    // ── Cloning Vader's Augmentation Spec ─────────────────────────────────────────
    // When the Empire needs a second Vader-class soldier, they pull the augmentation
    // blueprints and build an independent duplicate — same cybernetics, separate unit.
    // This copy constructor gives us an independent clone of an existing chain config
    // so we can edit one without disturbing the other.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Copy constructor — creates an independent clone of the given OlcChainConfig,
     * copying all four augmentation parameters. Changes to the copy won't affect the original.
     *
     * <p>For example — the Empire duplicates the augmentation spec:</p>
     * <pre>
     *   OlcChainConfig copy = new OlcChainConfig(original);
     *   copy.setOlcChainMaxReferralDepth(5);  // original depth unchanged
     * </pre>
     *
     * @param o  the source instance to copy all field values from
     */
    public OlcChainConfig( OlcChainConfig o )
    {
        super( o );
        olcChainCacheURI = o.olcChainCacheURI;
        olcChainingBehavior = o.olcChainingBehavior;
        olcChainMaxReferralDepth = o.olcChainMaxReferralDepth;
        olcChainReturnError = o.olcChainReturnError;
    }


    // ── Reading Whether The URI Cache Is Active ───────────────────────────────────
    // Vader's targeting system can cache the last-known location of enemy vessels
    // to avoid re-acquiring them from scratch every time. Similarly, olcChainCacheURI
    // controls whether the chain overlay caches target server URIs rather than
    // re-resolving every referral fresh.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code olcChainCacheURI} flag — whether the chain overlay should
     * cache URIs for referral targets it has already connected to.
     * Caching avoids repeated DNS lookups and connection setup for the same target.
     *
     * <p>For example — Vader's targeting system checks for cached coordinates:</p>
     * <pre>
     *   if (chain.getOlcChainCacheURI()) {
     *     // reuse the cached connection to the referral target server
     *   }
     * </pre>
     *
     * @return  {@code true} if URI caching is enabled, {@code false} if disabled, {@code null} if not configured
     */
    public Boolean getOlcChainCacheURI()
    {
        return olcChainCacheURI;
    }


    // ── Reading The Chaining Behavior Directive ───────────────────────────────────
    // The Emperor issues standing orders to Vader: "When you encounter a referral,
    // chase it unconditionally" or "only chain if the client explicitly requests it."
    // olcChainingBehavior is that standing directive — a string encoding when and
    // how aggressively the overlay should chain.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code olcChainingBehavior} string — a directive controlling when
     * the chain overlay will automatically follow referrals vs. pass them back to the client.
     * The format follows slapd's chaining behavior syntax.
     *
     * <p>For example — the Emperor's standing order to Vader:</p>
     * <pre>
     *   String behavior = chain.getOlcChainingBehavior();
     *   // e.g. "referrals critical" → chain all referrals, fail if unreachable
     * </pre>
     *
     * @return  the chaining behavior string, or {@code null} if not configured
     */
    public String getOlcChainingBehavior()
    {
        return olcChainingBehavior;
    }


    // ── Reading The Maximum Referral Depth Limit ──────────────────────────────────
    // Even Vader won't chase a target to the far edge of the Outer Rim indefinitely —
    // there's a practical limit on how many hyperspace jumps to follow before giving up.
    // olcChainMaxReferralDepth is that limit: how many referral hops the overlay will
    // follow before returning an error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code olcChainMaxReferralDepth} integer — the maximum number of
     * consecutive referrals the chain overlay will follow before giving up with an error.
     * Without a limit, a cycle of referrals would loop forever.
     *
     * <p>For example — Vader's pursuit range limit:</p>
     * <pre>
     *   int maxHops = chain.getOlcChainMaxReferralDepth();
     *   // e.g. 3 → chain will follow up to 3 referrals, then stop
     * </pre>
     *
     * @return  the max referral depth, or {@code null} if not configured (slapd uses its built-in default)
     */
    public Integer getOlcChainMaxReferralDepth()
    {
        return olcChainMaxReferralDepth;
    }


    // ── Reading The Error-Return Behavior ────────────────────────────────────────
    // When Vader's pursuit fails — the target escaped — he decides whether to report
    // the failure up the chain of command or quietly absorb it. olcChainReturnError
    // controls the same thing: if chaining fails, do we return the error to the
    // client or silently pretend the referral didn't happen?
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code olcChainReturnError} flag — when {@code true}, if the chain
     * overlay fails to follow a referral it returns an error to the client; when
     * {@code false}, it may silently swallow the failure.
     *
     * <p>For example — Vader decides whether to admit the target got away:</p>
     * <pre>
     *   if (chain.getOlcChainReturnError()) {
     *     // client will receive an error if chaining to the referral target fails
     *   }
     * </pre>
     *
     * @return  {@code true} if errors are surfaced to the client, or {@code null} if not configured
     */
    public Boolean getOlcChainReturnError()
    {
        return olcChainReturnError;
    }


    // ── Activating The URI Cache Module ──────────────────────────────────────────
    // Palpatine's engineers install (or remove) Vader's URI-targeting cache module
    // as a discrete upgrade to the life-support suit — flipping this switch changes
    // whether the suit remembers previously-contacted server addresses.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code olcChainCacheURI} flag. Pass {@code true} to enable caching of
     * referral target URIs, {@code false} to disable it, or {@code null} to leave it
     * unconfigured (slapd's default applies).
     *
     * <p>For example — engineers install the targeting-cache module:</p>
     * <pre>
     *   chain.setOlcChainCacheURI(true);  // suit now remembers target URIs
     * </pre>
     *
     * @param olcChainCacheURI  the URI caching flag to set
     */
    public void setOlcChainCacheURI( Boolean olcChainCacheURI )
    {
        this.olcChainCacheURI = olcChainCacheURI;
    }


    // ── Uploading The Standing Pursuit Order ──────────────────────────────────────
    // The Emperor transmits fresh standing orders to Vader's comm unit — new rules
    // of engagement for when to chase a referral and how hard.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code olcChainingBehavior} string — the directive controlling when
     * the overlay chains referrals. The value follows slapd's chaining behavior syntax.
     *
     * <p>For example — the Emperor updates Vader's standing orders:</p>
     * <pre>
     *   chain.setOlcChainingBehavior("referrals critical");
     *   // Vader will now chase all referrals and fail loudly if unreachable
     * </pre>
     *
     * @param olcChainingBehavior  the chaining behavior directive string
     */
    public void setOlcChainingBehavior( String olcChainingBehavior )
    {
        this.olcChainingBehavior = olcChainingBehavior;
    }


    // ── Adjusting The Maximum Pursuit Depth ───────────────────────────────────────
    // The Emperor recalibrates Vader's hyperspace-jump counter — how many hops Vader
    // will make before abandoning a target pursuit. Deeper pursuit costs more
    // resources, so this needs to be a deliberate setting.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code olcChainMaxReferralDepth} integer — the cap on how many
     * referral hops the chain overlay will follow before giving up.
     *
     * <p>For example — the Emperor recalibrates Vader's pursuit counter:</p>
     * <pre>
     *   chain.setOlcChainMaxReferralDepth(3);  // stop after 3 referral hops
     * </pre>
     *
     * @param olcChainMaxReferralDepth  the maximum number of referral hops to follow
     */
    public void setOlcChainMaxReferralDepth( Integer olcChainMaxReferralDepth )
    {
        this.olcChainMaxReferralDepth = olcChainMaxReferralDepth;
    }


    // ── Configuring The Failure-Report Module ─────────────────────────────────────
    // Palpatine decides whether Vader's suit should alert the bridge when a pursuit
    // fails, or quietly abort without raising an alarm. This toggle has serious
    // downstream consequences for client-side error handling.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code olcChainReturnError} flag — whether the overlay surfaces errors
     * to the client when it fails to follow a referral.
     *
     * <p>For example — Palpatine configures the failure-alert module:</p>
     * <pre>
     *   chain.setOlcChainReturnError(true);  // client hears about every chaining failure
     * </pre>
     *
     * @param olcChainReturnError  {@code true} to return errors to clients on chaining failure
     */
    public void setOlcChainReturnError( Boolean olcChainReturnError )
    {
        this.olcChainReturnError = olcChainReturnError;
    }


    // ── Duplicating The Augmentation Blueprint ────────────────────────────────────
    // The Imperial engineers photocopy Vader's full augmentation spec before shipping
    // it to a remote facility — an independent copy that can be modified without
    // altering the master on file in the Death Star archives.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates and returns an independent copy of this OlcChainConfig, including all
     * four chaining parameters. Useful for the editor's undo stack and diff comparison.
     *
     * <p>For example — the engineers duplicate the augmentation spec:</p>
     * <pre>
     *   OlcChainConfig snapshot = chainConfig.copy();
     *   // snapshot is fully independent — editing it won't affect the original
     * </pre>
     *
     * @return  a new OlcChainConfig with the same field values as this instance
     */
    @Override
    public OlcChainConfig copy()
    {
        return new OlcChainConfig( this );
    }
}
