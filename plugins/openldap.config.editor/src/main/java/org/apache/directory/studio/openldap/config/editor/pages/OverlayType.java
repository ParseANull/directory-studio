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
package org.apache.directory.studio.openldap.config.editor.pages;


// ── CLASS: OverlayType — Vader's Death Star Subsystem Manifest ───────────────
// When Vader surveys the Death Star from the command bridge, he has a manifest
// of every subsystem bay that can be installed: access logging, audit logging,
// chaining, distributed processing, proxy-bind, password policy, sync provider.
// Each bay slot is either "NONE" (empty) or one of the known subsystem types.
// OverlayType is that manifest — a Java enum that names every overlay type the
// Overlays page knows how to display and configure.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An enumeration of all OpenLDAP overlay types that the configuration editor
 * understands.
 * The Overlays page uses this enum when it needs to know what kind of overlay
 * the user is working with so it can route the selection to the correct
 * IDetailsPage implementation.
 * Think of it as Vader's official list of Death Star subsystem bays: each name
 * corresponds to a specific overlay module with its own configuration form.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum OverlayType
{
    /** None */
    NONE,

    /** Acess Log */
    ACCESS_LOG,

    /** Audit Log */
    AUDIT_LOG,

    /** Chain */
    CHAIN,

    /** Dist Proc */
    DIST_PROC,

    /** PBind */
    PBIND,

    /** Password Policy */
    PASSWORD_POLICY,

    /** SyncProv */
    SYNC_PROV;
}
