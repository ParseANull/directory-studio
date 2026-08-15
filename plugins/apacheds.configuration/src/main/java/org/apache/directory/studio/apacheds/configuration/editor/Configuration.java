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
package org.apache.directory.studio.apacheds.configuration.editor;


import org.apache.directory.server.config.beans.ConfigBean;
import org.apache.directory.server.core.partition.impl.btree.AbstractBTreePartition;
import org.apache.directory.server.core.partition.ldif.AbstractLdifPartition;


// ── CLASS: Configuration — DEATH STAR TECHNICAL SCHEMATICS ───────────────────────────────────────
// In Rogue One, Galen Erso's Death Star schematics are the complete engineering blueprint:
// every system spec is captured in the document, and the physical reactor unit is the
// hardware installation those specs describe.  You need both — the schematic and the hardware.
// This class is exactly that pairing: a {@link ConfigBean} (the full logical specification
// of the ApacheDS server) combined with the {@link AbstractBTreePartition} that physically
// stores those configuration entries on disk.  The editor passes this object around as the
// single authoritative source of truth for everything it reads and writes.
// ─────────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Top-level configuration holder that pairs the logical server spec with its
 * physical storage layer. The {@link ConfigBean} carries every server setting
 * (ports, interceptors, partitions, etc.) in memory-friendly Java objects, while
 * the {@link AbstractBTreePartition} is the on-disk B-tree store those settings
 * live in. Think of this class as the Death Star technical schematics: one object
 * that gives you both the blueprint and a handle on the physical installation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Configuration
{

    private ConfigBean configBean;
    private AbstractBTreePartition configPartition;


    // ── LOADING THE SCHEMATICS INTO THE BRIEFING ROOM ────────────────────────────────────────────
    // Galen hands over the finished technical readout together with the actual reactor module.
    // The briefing room now has everything it needs: the spec and the hardware.
    // We store both so the editor can read settings from the bean and persist via the partition.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code Configuration} by pairing a logical specification
     * with its physical storage layer. Both parts are required at construction
     * time — we do not support lazy-init here.
     *
     * <p>For example — Galen delivers the schematics and the reactor module together:</p>
     * <pre>
     *   Configuration cfg = new Configuration(configBean, configPartition);
     *   // cfg is now the single source of truth for the editor session
     * </pre>
     *
     * @param configBean       The logical server configuration (all settings as Java objects).
     * @param configPartition  The B-tree partition that stores those settings on disk.
     */
    public Configuration( ConfigBean configBean, AbstractBTreePartition configPartition )
    {
        this.configBean = configBean;
        this.configPartition = configPartition;
    }


    // ── READING THE LOGICAL SCHEMATICS ───────────────────────────────────────────────────────────
    // The Imperial engineer picks up the paper readout and reads the system specs.
    // Every setting — ports, interceptor chain, partition list — is in there.
    // getConfigBean hands back that logical readout so callers can inspect and mutate it.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ConfigBean} that holds all server settings in memory.
     * The editor reads from and writes to this object throughout the session;
     * changes are committed back to disk via the config partition when the user saves.
     *
     * <p>For example — the engineer reads the logical spec:</p>
     * <pre>
     *   ConfigBean spec = cfg.getConfigBean();
     *   DirectoryServiceBean ds = spec.getDirectoryServiceBean();
     * </pre>
     *
     * @return  The logical configuration bean; never {@code null} after construction.
     */
    public ConfigBean getConfigBean()
    {
        return configBean;
    }


    // ── REPLACING THE LOGICAL SCHEMATICS ─────────────────────────────────────────────────────────
    // The Death Star engineers swap out the old blueprint for a revised version.
    // The physical reactor stays where it is; only the paper readout changes.
    // setConfigBean does the same: swap in a new bean without touching the storage layer.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the logical server specification. Use this when you have loaded a
     * fresh {@link ConfigBean} — for example after re-reading the config from disk
     * or from a live LDAP connection — and want the editor to reflect the new data.
     *
     * <p>For example — the engineers install a revised blueprint:</p>
     * <pre>
     *   cfg.setConfigBean(freshlyParsedBean);
     *   editor.refreshUI();  // pick up the new spec
     * </pre>
     *
     * @param configBean  The new logical configuration bean to store.
     */
    public void setConfigBean( ConfigBean configBean )
    {
        this.configBean = configBean;
    }


    // ── INSPECTING THE PHYSICAL REACTOR ──────────────────────────────────────────────────────────
    // The engineer walks down to the reactor chamber and inspects the actual hardware.
    // getConfigPartition hands back the on-disk B-tree store so callers can read entries
    // directly or use it as the persistence target when saving.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the B-tree partition that physically stores the configuration entries.
     * The partition is used during save operations to write changed settings back
     * to disk, and during load to read the raw LDAP entries that are then parsed
     * into the {@link ConfigBean}.
     *
     * <p>For example — the engineer inspects the physical reactor unit:</p>
     * <pre>
     *   AbstractBTreePartition store = cfg.getConfigPartition();
     *   store.lookup(configEntryDn);  // read a raw config entry
     * </pre>
     *
     * @return  The underlying B-tree partition; never {@code null} after construction.
     */
    public AbstractBTreePartition getConfigPartition()
    {
        return configPartition;
    }


    // ── SWAPPING THE PHYSICAL REACTOR ────────────────────────────────────────────────────────────
    // The Imperial engineers decommission the old reactor and slot in a new one.
    // The schematics (bean) stay the same; only the physical storage unit changes.
    // setConfigPartition does that swap — useful when we need to point at a different backing store.
    // ────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the physical storage partition. Accepts an {@link AbstractLdifPartition}
     * (which extends {@link AbstractBTreePartition}) so callers can supply an LDIF-backed
     * partition without needing to cast.
     *
     * <p>For example — the engineers swap in a new reactor module:</p>
     * <pre>
     *   cfg.setConfigPartition(newLdifPartition);
     *   // subsequent save operations will target the new store
     * </pre>
     *
     * @param configPartition  The new partition to use as the configuration store.
     */
    public void setConfigPartition( AbstractLdifPartition configPartition )
    {
        this.configPartition = configPartition;
    }

}
