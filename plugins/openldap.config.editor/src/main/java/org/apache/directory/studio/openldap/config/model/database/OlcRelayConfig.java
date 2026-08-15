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
package org.apache.directory.studio.openldap.config.model.database;


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.openldap.common.ui.model.DatabaseTypeEnum;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;


// ── CLASS: OlcRelayConfig — Palpatine's Relay Vault Redirecting Requests ──────
// Palpatine keeps a relay vault that doesn't hold any data itself — it just
// redirects all incoming requests to another vault in the Empire's network.
// The relay backend does the same: it receives LDAP operations under one suffix
// and transparently forwards them to another configured suffix (olcRelay DN),
// where the real data lives. It's used to overlay multiple subtrees or provide
// alternate naming contexts for the same data.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcRelayConfig} object class, representing the
 * configuration of the OpenLDAP relay backend database.
 * The relay backend is a pass-through that redirects operations to another
 * database suffix. The target suffix is specified by the {@code olcRelay} attribute.
 * Think of this as Palpatine's relay vault — no data of its own, just redirects
 * all traffic to wherever the real vault is.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcRelayConfig extends OlcDatabaseConfig
{
    /**
     * Field for the 'olcRelay' attribute.
     */
    @ConfigurationElement(attributeType = "olcRelay", version="2.4.0")
    private Dn olcRelay;


    // ── getOlcRelay — Palpatine Reads the Relay Target Vault Address ──────────────
    // Palpatine checks which vault (DN suffix) all incoming requests are being
    // redirected to — the real data store behind this relay.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN of the target suffix that this relay backend forwards operations to.
     *
     * <p>For example — Palpatine reads the relay target:</p>
     * <pre>
     *   Dn target = relayConfig.getOlcRelay();
     *   // e.g., dc=real,dc=example,dc=com
     * </pre>
     *
     * @return  the target Dn, or null if not configured
     */
    public Dn getOlcRelay()
    {
        return olcRelay;
    }


    // ── setOlcRelay — Palpatine Points the Relay at a Target Vault ───────────────
    // Palpatine configures which vault (DN suffix) this relay should forward all requests to.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the target suffix DN that this relay backend forwards all operations to.
     *
     * <p>For example — Palpatine sets the relay target:</p>
     * <pre>
     *   relayConfig.setOlcRelay( new Dn( "dc=real,dc=example,dc=com" ) );
     * </pre>
     *
     * @param olcRelay  the target Dn to forward operations to
     */
    public void setOlcRelay( Dn olcRelay )
    {
        this.olcRelay = olcRelay;
    }


    // ── getOlcDatabaseType — Palpatine Identifies the Relay Vault Type ────────────
    // Palpatine identifies this vault as the "relay" type so OpenLDAP loads the
    // correct backend plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the database type identifier string "relay", used as the type
     * discriminator in the cn=config DIT.
     *
     * <p>For example — Palpatine identifies the relay vault type:</p>
     * <pre>
     *   relayConfig.getOlcDatabaseType(); // "relay"
     * </pre>
     *
     * @return  the lowercase database type string "relay"
     */
    @Override
    public String getOlcDatabaseType()
    {
        return DatabaseTypeEnum.RELAY.toString().toLowerCase();
    }
}
