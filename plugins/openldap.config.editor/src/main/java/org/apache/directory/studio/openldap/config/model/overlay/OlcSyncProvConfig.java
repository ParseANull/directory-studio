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

import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// ── CLASS: OlcSyncProvConfig — R2-D2 Broadcasting the Plans to the Fleet ─────
// After R2 downloads the Death Star plans, he doesn't just sit on them —
// he broadcasts updates to every Rebel ship in the fleet so they all have
// the latest version. That's exactly what the syncprov overlay does for LDAP:
// it turns a database into a replication provider, pushing delta updates to
// consumer replicas in real time. OlcSyncProvConfig holds the knobs:
// checkpoint intervals, session log size, and how to handle missing contextCSN.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcSyncProvConfig} object class, which configures the
 * OpenLDAP syncprov (Sync Provider) overlay.
 * Syncprov makes a database a replication source — consumer replicas connect and
 * request incremental updates via the LDAP Content Synchronization protocol (RFC 4533).
 * Think of this as R2 broadcasting the updated plans to the Rebel fleet.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcSyncProvConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcSpCheckpoint' attribute.
     */
    @ConfigurationElement(attributeType = "olcSpCheckpoint", version="2.4.0")
    private String olcSpCheckpoint;

    /**
     * Field for the 'olcSpNoPresent' attribute.
     */
    @ConfigurationElement(attributeType = "olcSpNoPresent", defaultValue = "FALSE", version="2.4.0")
    private Boolean olcSpNoPresent = false;

    /**
     * Field for the 'olcSpReloadHint' attribute.
     */
    @ConfigurationElement(attributeType = "olcSpReloadHint", defaultValue = "FALSE", version="2.4.0")
    private Boolean olcSpReloadHint = false;

    /**
     * Field for the 'olcSpSessionlog' attribute.
     */
    @ConfigurationElement(attributeType = "olcSpSessionlog", version="2.4.0")
    private Integer olcSpSessionlog;


    // ── Default Constructor — R2 Boots the Broadcast System ──────────────────────
    // R2 powers up the broadcast system with the overlay type name ("syncprov")
    // so OpenLDAP can attach it to the right database and start broadcasting.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlcSyncProvConfig with the overlay type set to "syncprov".
     * The olcOverlay field is used as the RDN in cn=config
     * (e.g., olcOverlay=syncprov,olcDatabase={1}mdb,cn=config).
     *
     * <p>For example — R2 boots the broadcast system:</p>
     * <pre>
     *   OlcSyncProvConfig syncProv = new OlcSyncProvConfig();
     *   syncProv.getOlcOverlay(); // "syncprov"
     * </pre>
     */
    public OlcSyncProvConfig()
    {
        super();
        olcOverlay = "syncprov";
    }


    // ── Copy Constructor — R2 Duplicates the Broadcast Config ────────────────────
    // R2 copies the current broadcast config so we can diff or revert changes
    // without modifying the original.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given OlcSyncProvConfig.
     * Used by the editor to create a working copy before the user commits changes.
     *
     * <p>For example — R2 duplicates the broadcast config:</p>
     * <pre>
     *   OlcSyncProvConfig copy = new OlcSyncProvConfig( originalSyncProvConfig );
     * </pre>
     *
     * @param o  the OlcSyncProvConfig to copy
     */
    public OlcSyncProvConfig( OlcSyncProvConfig o )
    {
        super();
        olcSpCheckpoint = o.olcSpCheckpoint;
        olcSpNoPresent = o.olcSpNoPresent;
        olcSpReloadHint = o.olcSpReloadHint;
        olcSpSessionlog = o.olcSpSessionlog;
    }


    // ── getOlcSpCheckpoint — R2 Reads the Checkpoint Interval ────────────────────
    // R2 checks how often to flush the contextCSN to disk so replicas can confirm
    // they're caught up without replaying the whole log after a restart.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the checkpoint interval string (e.g., "100 10") which controls how
     * often the contextCSN is written to the database — either every N operations
     * or every M minutes, whichever comes first.
     *
     * <p>For example — R2 reads the checkpoint interval:</p>
     * <pre>
     *   String checkpoint = syncProvConfig.getOlcSpCheckpoint(); // "100 10"
     * </pre>
     *
     * @return  the checkpoint interval string, or null if not configured
     */
    public String getOlcSpCheckpoint()
    {
        return olcSpCheckpoint;
    }


    // ── getOlcSpNoPresent — R2 Checks the Present Phase Flag ─────────────────────
    // R2 checks whether to skip the "present" phase during refresh — saving bandwidth
    // by omitting the list of all present entries and just sending changes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether to omit the present phase during a refresh cycle.
     * If TRUE, syncprov skips sending the list of all present entry UUIDs,
     * which can speed up sync but may miss deleted entries in some scenarios.
     *
     * <p>For example — R2 checks the present-phase flag:</p>
     * <pre>
     *   Boolean noPresent = syncProvConfig.getOlcSpNoPresent(); // FALSE (default)
     * </pre>
     *
     * @return  TRUE to skip the present phase, FALSE to include it (default), null if unconfigured
     */
    public Boolean getOlcSpNoPresent()
    {
        return olcSpNoPresent;
    }


    // ── getOlcSpReloadHint — R2 Checks the Reload Hint Flag ──────────────────────
    // R2 checks whether to send a reload hint to consumers, signaling that they
    // should do a full reload rather than an incremental sync when contextCSN is stale.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether syncprov should send a reload hint to consumers.
     * If TRUE, consumers that fall too far behind are told to reload the whole
     * database rather than try to catch up incrementally.
     *
     * <p>For example — R2 checks the reload hint flag:</p>
     * <pre>
     *   Boolean reloadHint = syncProvConfig.getOlcSpReloadHint(); // FALSE (default)
     * </pre>
     *
     * @return  TRUE to enable reload hints, FALSE to disable (default), null if unconfigured
     */
    public Boolean getOlcSpReloadHint()
    {
        return olcSpReloadHint;
    }


    // ── getOlcSpSessionlog — R2 Reads the Session Log Size ───────────────────────
    // R2 checks how large the in-memory delta log is — that's the list of recent
    // changes he keeps so consumers can catch up without a full reload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the size of the in-memory session log (number of operations).
     * The session log lets consumers that are slightly behind catch up with incremental
     * deltas rather than a full reload. A larger log handles more lag.
     *
     * <p>For example — R2 reads the session log size:</p>
     * <pre>
     *   Integer logSize = syncProvConfig.getOlcSpSessionlog(); // e.g., 100
     * </pre>
     *
     * @return  the session log size in operations, or null if not configured
     */
    public Integer getOlcSpSessionlog()
    {
        return olcSpSessionlog;
    }


    // ── setOlcSpCheckpoint — R2 Sets the Checkpoint Interval ─────────────────────
    // R2 sets how often to flush the contextCSN to disk — a safety net so replicas
    // can confirm their sync position after a provider restart.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the checkpoint interval string controlling how often contextCSN is persisted.
     *
     * <p>For example — R2 sets the checkpoint interval:</p>
     * <pre>
     *   syncProvConfig.setOlcSpCheckpoint( "100 10" ); // every 100 ops or 10 minutes
     * </pre>
     *
     * @param olcSpCheckpoint  the checkpoint interval, e.g. "ops minutes"
     */
    public void setOlcSpCheckpoint( String olcSpCheckpoint )
    {
        this.olcSpCheckpoint = olcSpCheckpoint;
    }


    // ── setOlcSpNoPresent — R2 Toggles the Present Phase ────────────────────────
    // R2 enables or disables the present phase in the refresh cycle.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether to omit the present phase during refresh cycles.
     *
     * <p>For example — R2 skips the present phase:</p>
     * <pre>
     *   syncProvConfig.setOlcSpNoPresent( Boolean.TRUE );
     * </pre>
     *
     * @param olcSpNoPresent  TRUE to skip present phase, FALSE to include it
     */
    public void setOlcSpNoPresent( Boolean olcSpNoPresent )
    {
        this.olcSpNoPresent = olcSpNoPresent;
    }


    // ── setOlcSpReloadHint — R2 Enables or Disables the Reload Hint ──────────────
    // R2 enables the reload hint so stale consumers know to do a full reload.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether syncprov should send reload hints to stale consumers.
     *
     * <p>For example — R2 enables reload hints:</p>
     * <pre>
     *   syncProvConfig.setOlcSpReloadHint( Boolean.TRUE );
     * </pre>
     *
     * @param olcSpReloadHint  TRUE to send reload hints, FALSE to not
     */
    public void setOlcSpReloadHint( Boolean olcSpReloadHint )
    {
        this.olcSpReloadHint = olcSpReloadHint;
    }


    // ── setOlcSpSessionlog — R2 Sets the Session Log Size ────────────────────────
    // R2 configures how many operations to keep in the delta log for catching up replicas.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the size of the in-memory session log.
     *
     * <p>For example — R2 sets the session log capacity:</p>
     * <pre>
     *   syncProvConfig.setOlcSpSessionlog( 100 );
     * </pre>
     *
     * @param olcSpSessionlog  the number of operations to retain in the session log
     */
    public void setOlcSpSessionlog( Integer olcSpSessionlog )
    {
        this.olcSpSessionlog = olcSpSessionlog;
    }


    // ── copy — R2 Duplicates the Broadcast Config Object ─────────────────────────
    // R2 creates an exact copy of the broadcast config for safe editing in the UI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcSyncProvConfig.
     * Used by the editor to create a working copy before the user commits changes.
     *
     * <p>For example — R2 duplicates the broadcast config:</p>
     * <pre>
     *   OlcSyncProvConfig copy = syncProvConfig.copy();
     * </pre>
     *
     * @return  a new OlcSyncProvConfig with the same field values
     */
    @Override
    public OlcSyncProvConfig copy()
    {
        return new OlcSyncProvConfig( this );
    }
}
