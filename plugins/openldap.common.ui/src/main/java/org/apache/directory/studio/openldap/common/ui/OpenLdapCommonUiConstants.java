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
package org.apache.directory.studio.openldap.common.ui;


// ── CLASS: OpenLdapCommonUiConstants — REBEL BASE MISSION BRIEFING BOARD ─────
// Picture the big display board in the Echo Base command room where all the
// permanent mission codes and identifiers are pinned up for every officer to
// reference. This class holds only immutable constants — no logic, no state —
// just the shared labels the entire plugin needs. Because nobody should ever
// construct an instance of a board, the constructor is deliberately sealed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We keep all shared constants for the openldap.common.ui plugin here so every
 * component in the plugin reads from one authoritative source. This class is
 * final and un-instantiable by design — it's a pure constant holder, not a
 * service object.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class OpenLdapCommonUiConstants
{
    // ── CONSTRUCTOR: OpenLdapCommonUiConstants — SEALING THE BRIEFING BOARD ──
    // Nobody should walk up to the mission board and "create" a new one —
    // there's only one, and it lives on the wall. This private constructor
    // enforces that invariant and makes the design intent self-documenting.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — we prevent instantiation because this class is a
     * pure constant holder. There is never a need to create an instance.
     * Implicit super constructor is not visible for the default constructor,
     * but this is still self-documenting.
     */
    private OpenLdapCommonUiConstants()
    {
    }

    /** The plug-in ID */
    public static final String PLUGIN_ID = OpenLdapCommonUiConstants.class.getPackage().getName();

    public static final String DIALOGSETTING_KEY_DIRECTORY_HISTORY = "directoryHistory"; //$NON-NLS-1$
}
