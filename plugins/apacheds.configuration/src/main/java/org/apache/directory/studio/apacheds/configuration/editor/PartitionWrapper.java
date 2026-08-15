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


import org.apache.directory.server.config.beans.PartitionBean;


// ── CLASS: PartitionWrapper — THE VAULT SHIPPING CRATE ───────────────────────────────────
// When the Empire ships a data vault (partition) through its logistics system, it gets put
// into a standardised shipping crate.  The crate is just packaging — the vault inside is
// the thing that matters — but the crate makes it easier to track, swap, or replace the vault
// without touching the logistics chain directly.
// This wrapper holds a PartitionBean so we can swap out the bean (e.g. JDBM → Mavibot)
// without changing every reference in the UI that points at the wrapper.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * A thin wrapper around a {@link PartitionBean}, used so the partition-details UI can hold
 * a stable reference to a "slot" and swap the underlying bean (e.g. when the user changes
 * the partition type from JDBM to Mavibot) without the master list losing track of the item.
 * Think of it as the Imperial shipping crate: the crate stays; the vault inside can change.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PartitionWrapper
{
    /** The wrapped partition */
    private PartitionBean partition;


    // ── Creating A Crate For This Partition Bean ──────────────────────────────────────────────
    // We stamp the crate with the initial partition bean so the UI has something to display
    // immediately after wrapping.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new wrapper around the given partition bean.
     *
     * @param partition  the partition bean to wrap
     */
    public PartitionWrapper( PartitionBean partition )
    {
        this.partition = partition;
    }


    // ── Retrieving The Vault From Its Crate ───────────────────────────────────────────────────
    // Any part of the UI that needs the underlying partition bean calls this getter.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the wrapped {@link PartitionBean}.
     *
     * @return the current partition bean; never {@code null} if the wrapper was constructed
     *         with a non-null partition
     */
    public PartitionBean getPartition()
    {
        return partition;
    }


    // ── Swapping The Vault Inside The Crate ───────────────────────────────────────────────────
    // When the user changes the storage type, we swap the bean inside the existing wrapper
    // instead of creating a brand-new wrapper (which would break master-list references).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the wrapped partition bean.
     * Use this when the user changes the partition type (e.g. JDBM to Mavibot) so existing
     * UI references to this wrapper stay valid.
     *
     * @param partition  the new partition bean to store
     */
    public void setPartition( PartitionBean partition )
    {
        this.partition = partition;
    }
}
