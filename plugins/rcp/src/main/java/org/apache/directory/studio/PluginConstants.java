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
package org.apache.directory.studio;


// ── CLASS: PluginConstants — R2-D2 Stores the Death Star Plans ───────────────
// Princess Leia loads the complete Death Star schematics into R2-D2 before the
// Tantive IV is boarded — every critical identifier in one compact droid memory
// unit, ready to be pulled out exactly when needed.
// We do the same: every action ID and preference key used across the rcp plugin
// lives here as a named constant so we never mis-type a string literal twice.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds every string constant used by the rcp plugin in one place.
 * Action IDs, preference keys — anything that gets referenced from more than
 * one class belongs here so we change it once and it's right everywhere.
 * Think of this class as R2-D2's memory unit: compact, reliable, never wrong.
 * Final reference — class shouldn't be extended.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class PluginConstants
{
    // ── R2-D2 Refuses to Open the Hatch — No One Instantiates the Plans ──────
    // Leia gave R2 the plans to carry, not to hand out indiscriminately.
    // This private constructor ensures nobody creates a PluginConstants object
    // by accident — every field here is static and that's how they should stay.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Blocks instantiation — this is a pure constants holder.
     * All fields are {@code public static final}; there is never a reason to
     * create an instance of this class.
     */
    private PluginConstants()
    {
    }

    /** The Add Extension Action ID */
    public static final String ACTION_ADD_EXTENSION_ID = "org.apache.directory.studio.newExtensions"; //$NON-NLS-1$

    /** The Manage Configuration Action ID */
    public static final String ACTION_MANAGE_CONFIGURATION_ID = "org.apache.directory.studio.manageConfiguration"; //$NON-NLS-1$

    /** The Open File Action ID */
    public static final String ACTION_OPEN_FILE_ID = "org.apache.directory.studio.openFile"; //$NON-NLS-1$

    /** The Report A Bug Action ID */
    public static final String ACTION_REPORT_A_BUG_ID = "org.apache.directory.studio.reportABug"; //$NON-NLS-1$

    /** The Update ActionID */
    public static final String ACTION_UPDATE_ID = "org.apache.directory.studio.newUpdates"; //$NON-NLS-1$

    /** The Update ActionID */
    public static final String PREFERENCE_EXIT_PROMPT_ON_CLOSE_LAST_WINDOW = "exitPromptOnCloseLastWindow"; //$NON-NLS-1$
}
