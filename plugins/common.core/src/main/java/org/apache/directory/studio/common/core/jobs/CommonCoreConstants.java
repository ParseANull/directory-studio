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

package org.apache.directory.studio.common.core.jobs;


// ── CLASS: CommonCoreConstants — Yavin 4 Mission Control Identifiers ──────────
// In the Rebel Alliance's mission control room at Yavin 4 every mission and
// every mission family has a codename stamped on every briefing packet.
// PLUGIN_ID is the Alliance codename for the entire common-core system.
// JOB_FAMILY_ID is the family badge that groups all active Studio jobs together
// so the scheduler can ask "does this job belong to the Alliance family?" via
// belongsTo().  These are just string constants; nothing more to it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * String constants used across the Common Core plugin.
 * {@link #PLUGIN_ID} identifies the plugin bundle; {@link #JOB_FAMILY_ID}
 * groups all Studio jobs into a single family so the Eclipse job scheduler
 * can find them by family.
 * Think of these as the Alliance codenames stamped on every mission briefing
 * packet issued from Yavin 4 mission control.
 * Final reference — this class shouldn't be extended.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class CommonCoreConstants
{

    /** The plug-in ID */
    public static final String PLUGIN_ID = CommonCoreConstants.class.getPackage().getName();

    /** The Job family ID */
    public static final String JOB_FAMILY_ID = PLUGIN_ID + ".family"; //$NON-NLS-1$


    // ── Prevent Instantiation — These Are Just Labels ──────────────────────────
    // Rebels don't instantiate a code book; they just look up the codes inside.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this class is a holder of static constants only.
     * Ensures no construction of this class; also ensures there is no need for
     * the {@code final} keyword above (implicit super constructor is not visible
     * for default constructor), but is still self-documenting.
     */
    private CommonCoreConstants()
    {
    }
}
