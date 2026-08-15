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
package org.apache.directory.studio.openldap.config.model.overlay;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// ── CLASS: OlcPPolicyConfig — Vader Enforcing Imperial Compliance Policy ─────
// Vader doesn't improvise when it comes to discipline in the Imperial fleet —
// Palpatine has issued a password compliance policy in the form of a written order
// (the default policy entry), and Vader enforces it to the letter: hash passwords
// on intake, lock out violators, forward updates to replica nodes.
// OlcPPolicyConfig maps the ppolicy overlay's settings: which policy entry is the
// default, whether to hash cleartext passwords, whether locked-out users see a
// lockout error or a generic failure, and whether updates propagate upstream.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcPPolicyConfig} object class, which configures the
 * OpenLDAP password policy (ppolicy) overlay.
 * The ppolicy overlay enforces password rules (max age, lockout, complexity) against
 * bind operations, applying the policy entry pointed to by olcPPolicyDefault.
 * Think of this as Vader enforcing Palpatine's password compliance policy — firm,
 * systematic, and not open to negotiation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcPPolicyConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcPPolicyDefault' attribute.
     */
    @ConfigurationElement(attributeType = "olcPPolicyDefault", version="2.4.0")
    private Dn olcPPolicyDefault;

    /**
     * Field for the 'olcPPolicyForwardUpdates' attribute.
     */
    @ConfigurationElement(attributeType = "olcPPolicyForwardUpdates", version="2.4.17")
    private Boolean olcPPolicyForwardUpdates;

    /**
     * Field for the 'olcPPolicyHashCleartext' attribute.
     */
    @ConfigurationElement(attributeType = "olcPPolicyHashCleartext", version="2.4.0")
    private Boolean olcPPolicyHashCleartext;

    /**
     * Field for the 'olcPPolicyUseLockout' attribute.
     */
    @ConfigurationElement(attributeType = "olcPPolicyUseLockout", version="2.4.0")
    private Boolean olcPPolicyUseLockout;


    // ── Default Constructor — Vader Puts on the Armor ─────────────────────────────
    // Vader suits up and sets his identity: "ppolicy" — the overlay type name that
    // becomes part of the LDAP entry's RDN in cn=config.
    // We call super() and set olcOverlay so OpenLDAP knows which overlay plugin this is.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlcPPolicyConfig with the overlay type set to "ppolicy".
     * The olcOverlay value is used as the RDN component in cn=config
     * (e.g., olcOverlay=ppolicy,olcDatabase={1}mdb,cn=config).
     *
     * <p>For example — Vader puts on the armor:</p>
     * <pre>
     *   OlcPPolicyConfig ppolicy = new OlcPPolicyConfig();
     *   ppolicy.getOlcOverlay(); // "ppolicy"
     * </pre>
     */
    public OlcPPolicyConfig()
    {
        super();
        olcOverlay = "ppolicy";
    }


    // ── Copy Constructor — Vader Replicates the Imperial Order ───────────────────
    // Vader makes an exact copy of the written compliance order — same policy DN,
    // same flags — so we can diff or revert changes in the editor without corrupting
    // the original.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given OlcPPolicyConfig.
     * Used by the editor to create a working copy before the user commits changes.
     *
     * <p>For example — Vader replicates the compliance order:</p>
     * <pre>
     *   OlcPPolicyConfig copy = new OlcPPolicyConfig( originalPPolicyConfig );
     * </pre>
     *
     * @param o  the OlcPPolicyConfig to copy; its fields are copied into this new instance
     */
    public OlcPPolicyConfig( OlcPPolicyConfig o )
    {
        super( o );
        olcPPolicyDefault = o.olcPPolicyDefault;
        olcPPolicyForwardUpdates = o.olcPPolicyForwardUpdates;
        olcPPolicyHashCleartext = o.olcPPolicyHashCleartext;
        olcPPolicyUseLockout = o.olcPPolicyUseLockout;
    }


    // ── getOlcPPolicyDefault — Vader Reads the Default Policy Entry ──────────────
    // Vader checks which policy entry is the standing order for password compliance —
    // the DN of the pwdPolicy object that applies when a user has no personal policy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN of the default password policy entry.
     * When a user has no personal pwdPolicy attribute, ppolicy applies this entry.
     *
     * <p>For example — Vader reads the default policy:</p>
     * <pre>
     *   Dn defaultPolicy = ppolicyConfig.getOlcPPolicyDefault();
     *   // e.g., cn=default,ou=policies,dc=example,dc=com
     * </pre>
     *
     * @return  the Dn of the default policy entry, or null if not configured
     */
    public Dn getOlcPPolicyDefault()
    {
        return olcPPolicyDefault;
    }


    // ── getOlcPPolicyForwardUpdates — Vader Checks the Replica Forwarding Flag ───
    // Vader checks whether password policy state updates (like failed bind counts)
    // should be forwarded upstream to the master when this replica's data is read-only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether ppolicy should forward updates to the master LDAP server.
     * Relevant on read-only replicas — if TRUE, password state changes (like
     * incrementing bad password counts) are forwarded rather than rejected.
     *
     * <p>For example — Vader checks the forwarding flag:</p>
     * <pre>
     *   Boolean forward = ppolicyConfig.getOlcPPolicyForwardUpdates(); // TRUE or FALSE
     * </pre>
     *
     * @return  TRUE if updates should be forwarded, FALSE if not, null if unconfigured
     */
    public Boolean getOlcPPolicyForwardUpdates()
    {
        return olcPPolicyForwardUpdates;
    }


    // ── getOlcPPolicyHashCleartext — Vader Checks the Hashing Mandate ────────────
    // Vader checks whether cleartext passwords flowing in from clients must be
    // immediately hashed — you don't store unencrypted credentials in Imperial vaults.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether ppolicy should automatically hash cleartext passwords on write.
     * If TRUE, any cleartext userPassword value is hashed using the configured scheme
     * before being stored — useful if clients don't hash on their end.
     *
     * <p>For example — Vader checks the hashing mandate:</p>
     * <pre>
     *   Boolean hash = ppolicyConfig.getOlcPPolicyHashCleartext(); // TRUE or FALSE
     * </pre>
     *
     * @return  TRUE if cleartext should be hashed, FALSE if not, null if unconfigured
     */
    public Boolean getOlcPPolicyHashCleartext()
    {
        return olcPPolicyHashCleartext;
    }


    // ── getOlcPPolicyUseLockout — Vader Checks the Lockout Response Mode ─────────
    // Vader checks whether locked-out users should receive a specific "Account Locked"
    // error (TRUE) or just a generic "Invalid Credentials" (FALSE — the sneaky default).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether ppolicy should return an explicit account-lockout error.
     * If TRUE, locked accounts get a distinct LDAP error code (AccountLocked) so
     * clients can differentiate lockout from bad credentials.
     * If FALSE, they get the same "invalid credentials" error — less informative
     * but also less revealing to an attacker.
     *
     * <p>For example — Vader checks the lockout response:</p>
     * <pre>
     *   Boolean useLockout = ppolicyConfig.getOlcPPolicyUseLockout(); // TRUE or FALSE
     * </pre>
     *
     * @return  TRUE to return AccountLocked errors, FALSE for generic failure, null if unconfigured
     */
    public Boolean getOlcPPolicyUseLockout()
    {
        return olcPPolicyUseLockout;
    }


    // ── setOlcPPolicyDefault — Vader Issues the Default Policy Order ──────────────
    // Vader signs the standing order that specifies which policy entry is the default
    // for users without a personal policy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN of the default password policy entry.
     * This is the policy applied to users who don't have a personal pwdPolicy attribute.
     *
     * <p>For example — Vader issues the default policy order:</p>
     * <pre>
     *   ppolicyConfig.setOlcPPolicyDefault(
     *       new Dn( "cn=default,ou=policies,dc=example,dc=com" ) );
     * </pre>
     *
     * @param olcPPolicyDefault  the Dn of the default password policy entry
     */
    public void setOlcPPolicyDefault( Dn olcPPolicyDefault )
    {
        this.olcPPolicyDefault = olcPPolicyDefault;
    }


    // ── setOlcPPolicyForwardUpdates — Vader Sets the Forwarding Flag ─────────────
    // Vader enables or disables forwarding of policy state updates from replica to master.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether ppolicy should forward policy state updates to the master.
     *
     * <p>For example — Vader enables forwarding on a replica:</p>
     * <pre>
     *   ppolicyConfig.setOlcPPolicyForwardUpdates( Boolean.TRUE );
     * </pre>
     *
     * @param olcPPolicyForwardUpdates  TRUE to forward, FALSE to reject, null to use default
     */
    public void setOlcPPolicyForwardUpdates( Boolean olcPPolicyForwardUpdates )
    {
        this.olcPPolicyForwardUpdates = olcPPolicyForwardUpdates;
    }


    // ── setOlcPPolicyHashCleartext — Vader Mandates Password Hashing ─────────────
    // Vader mandates that all incoming cleartext credentials be hashed before storage.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether ppolicy should hash cleartext passwords before storing them.
     *
     * <p>For example — Vader mandates hashing:</p>
     * <pre>
     *   ppolicyConfig.setOlcPPolicyHashCleartext( Boolean.TRUE );
     * </pre>
     *
     * @param olcPPolicyHashCleartext  TRUE to hash cleartext passwords, FALSE to leave as-is
     */
    public void setOlcPPolicyHashCleartext( Boolean olcPPolicyHashCleartext )
    {
        this.olcPPolicyHashCleartext = olcPPolicyHashCleartext;
    }


    // ── setOlcPPolicyUseLockout — Vader Toggles the Lockout Error Mode ───────────
    // Vader decides whether locked users should be told explicitly they're locked out
    // or just get a generic failure response.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether ppolicy should return an explicit AccountLocked error to locked users.
     *
     * <p>For example — Vader sets the lockout response mode:</p>
     * <pre>
     *   ppolicyConfig.setOlcPPolicyUseLockout( Boolean.TRUE );
     * </pre>
     *
     * @param olcPPolicyUseLockout  TRUE for explicit lockout errors, FALSE for generic failure
     */
    public void setOlcPPolicyUseLockout( Boolean olcPPolicyUseLockout )
    {
        this.olcPPolicyUseLockout = olcPPolicyUseLockout;
    }


    // ── copy — Vader Duplicates the Compliance Policy Object ─────────────────────
    // Vader creates an exact duplicate of the compliance policy object so the editor
    // can diff it against the original without risk of corruption.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcPPolicyConfig.
     * Used by the editor to create a working copy before the user commits changes.
     *
     * <p>For example — Vader duplicates the policy object:</p>
     * <pre>
     *   OlcPPolicyConfig copy = ppolicyConfig.copy();
     * </pre>
     *
     * @return  a new OlcPPolicyConfig with the same field values as this one
     */
    @Override
    public OlcPPolicyConfig copy()
    {
        return new OlcPPolicyConfig( this );
    }
}
