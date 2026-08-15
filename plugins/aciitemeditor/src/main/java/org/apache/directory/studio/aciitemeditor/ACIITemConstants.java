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
package org.apache.directory.studio.aciitemeditor;


// ── CLASS: ACIITemConstants — THE IMPERIAL SECURITY BUREAU CLEARANCE MANIFEST ───
// Grand Moff Tarkin's ISB keeps a master list of all clearance codes and identifiers
// used throughout the Death Star security network. Every officer who needs to reference
// a secure resource must look up the correct code here — no ad-hoc strings allowed.
// That's exactly what this interface does: one canonical home for every plugin-wide constant.
// ────────────────────────────────────────────────────────────────────────────────
/**
 * A single home for all string constants used by the ACI Item Editor plugin.
 * Rather than scattering magic strings across the codebase, we keep them here so
 * a renaming or ID change only needs to happen in one place.
 * Think of this as the Grand Moff's clearance manifest — every access token lives here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ACIITemConstants
{
    /** The plug-in ID */
    String PLUGIN_ID = ACIITemConstants.class.getPackage().getName();

    /** The ID for ACI Item Template */
    String ACI_ITEM_TEMPLATE_ID = PLUGIN_ID + ".templates"; //$NON-NLS-1$
}
