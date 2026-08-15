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


import org.apache.directory.server.config.beans.JdbmPartitionBean;
import org.apache.directory.server.config.beans.MavibotPartitionBean;
import org.apache.directory.server.config.beans.PartitionBean;


// ── CLASS: PartitionType — IMPERIAL VAULT CLASSIFICATION SYSTEM ──────────────────────────
// The Empire stores data in two kinds of vaults: old-style JDBM storage units and the newer
// Mavibot B-Tree vaults.  When a configuration panel needs to know what kind of vault it's
// looking at, it uses this classification enum to decide which detail panel to show.
// This is the vault classification label — JDBM or MAVIBOT — nothing more.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * Classifies a partition as either JDBM or Mavibot storage type.
 * The configuration editor uses this to select the correct detail block
 * when displaying or editing partition settings.
 * Think of it as the Imperial vault classification tag: which kind of storage engine is this?
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum PartitionType
{
    /** The JDBM partition type */
    JDBM,

    /** The Mavibot partition type */
    MAVIBOT;


    // ── Sniffing Which Type A Bean Is ─────────────────────────────────────────────────────────
    // Given a raw PartitionBean, we check its concrete type and return the matching enum.
    // If neither matches (shouldn't happen in practice), we return null.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code PartitionType} corresponding to the given {@link PartitionBean}.
     * Returns {@code null} if the bean is neither a JDBM nor a Mavibot partition.
     *
     * @param partition  the partition bean to classify
     * @return {@link #JDBM}, {@link #MAVIBOT}, or {@code null}
     */
    public static PartitionType fromPartition( PartitionBean partition )
    {
        if ( partition instanceof JdbmPartitionBean )
        {
            return JDBM;
        }
        else if ( partition instanceof MavibotPartitionBean )
        {
            return MAVIBOT;
        }

        return null;
    }


    // ── Formatting The Type As A Human-Readable String ────────────────────────────────────────
    // The UI shows these as "JDBM" and "Mavibot" — the Mavibot case uses mixed-case since
    // that's the product's official stylisation.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable label for this partition type.
     * {@code JDBM} → {@code "JDBM"}; {@code MAVIBOT} → {@code "Mavibot"}.
     *
     * @return the display name
     */
    public String toString()
    {
        switch ( this )
        {
            case JDBM:
                return "JDBM";
            case MAVIBOT:
                return "Mavibot";
        }

        return super.toString();
    }
}
