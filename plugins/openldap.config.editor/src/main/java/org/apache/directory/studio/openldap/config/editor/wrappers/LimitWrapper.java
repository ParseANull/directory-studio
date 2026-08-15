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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── INTERFACE: LimitWrapper — The Standard Limit Protocol ─────────────────────
// The Imperial command uses a shared protocol for all operational limits: every
// officer knows the difference between a global cap, a hard ceiling, and a soft
// advisory threshold.  LimitWrapper defines that shared protocol — the contract
// that both TimeLimitWrapper and SizeLimitWrapper must implement.  It also
// defines the sentinel constants used to represent special limit values
// (unlimited, hard=soft, etc.).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The shared interface for {@link TimeLimitWrapper} and {@link SizeLimitWrapper}.
 * It declares the three limit tiers (global, hard, soft), their accessors, and
 * the sentinel integer constants used in OpenLDAP configuration strings.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface LimitWrapper extends Comparable<LimitWrapper>
{
    // Define some of the used constants

    /** Sentinel meaning hard=soft (hard limit equals soft limit) */
    Integer HARD_SOFT = Integer.valueOf( -3 );

    /** Sentinel meaning unlimited (-1) */
    Integer UNLIMITED = Integer.valueOf( -1 );

    /** String token for the hard limit keyword */
    String HARD_STR = "hard";

    /** String token for the none keyword (equivalent to unlimited) */
    String NONE_STR = "none";

    /** String token for the soft keyword */
    String SOFT_STR = "soft";

    /** String token for the unlimited keyword */
    String UNLIMITED_STR = "unlimited";


    // ── clear — Reset All Limit Tiers to Null ─────────────────────────────────
    // The officer's performance record is wiped clean.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clear the TimeLimitWrapper (reset all the values to null)
     */
    void clear();


    // ── getGlobalLimit — Read the Overall Cap ─────────────────────────────────
    /**
     * @return the globalLimit
     */
    Integer getGlobalLimit();


    // ── setGlobalLimit — Set the Overall Cap ──────────────────────────────────
    /**
     * @param globalLimit the globalLimit to set
     */
    void setGlobalLimit( Integer globalLimit );


    // ── getSoftLimit — Read the Advisory Threshold ────────────────────────────
    /**
     * @return the softLimit
     */
    Integer getSoftLimit();


    // ── setSoftLimit — Set the Advisory Threshold ─────────────────────────────
    /**
     * @param softLimit the softLimit to set
     */
    void setSoftLimit( Integer softLimit );


    // ── getHardLimit — Read the Absolute Ceiling ──────────────────────────────
    /**
     * @return the hardLimit
     */
    Integer getHardLimit();


    // ── setHardLimit — Set the Absolute Ceiling ───────────────────────────────
    /**
     * @param hardLimit the hardLimit to set
     */
    void setHardLimit( Integer hardLimit );


    // ── getType — Return the Limit's Config Keyword ───────────────────────────
    // Returns "time" or "size" depending on the subtype, used to prefix
    // the serialized config string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @return The Limit's type
     */
    String getType();


    // ── isValid — Check If the Parsed Limit Is Coherent ───────────────────────
    /**
     * Tells if the LimitWrapper instance is valid
     * @return True if this is a valid instance
     */
    boolean isValid();
}
