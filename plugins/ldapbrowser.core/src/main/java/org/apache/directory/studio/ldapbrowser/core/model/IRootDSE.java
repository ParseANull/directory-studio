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

package org.apache.directory.studio.ldapbrowser.core.model;


// ── CLASS: IRootDSE — THE IMPERIAL STAR DESTROYER'S BRIDGE MANIFEST ──────────
// When the Millennium Falcon docks with an Imperial Star Destroyer, the first
// thing Han does is query the bridge manifest: "What weapons does this ship
// carry?  What communications frequencies does it support?  What docking bays
// are available?"  The bridge manifest is the ship's root data service entry —
// it tells you everything the ship supports before you start interacting with
// its systems.
// The Root DSE (Distinguished Service Entry) is the LDAP equivalent: it's the
// special entry at DN="" (the root) that every LDAP server publishes.  Reading
// it tells you what LDAP controls the server supports, what extensions it has,
// and what sub-trees (naming contexts) it serves.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the Root DSE (Distinguished Service Entry) of an LDAP server.
 * The Root DSE is accessible at the empty DN ({@code ""}) and contains
 * server capability metadata: supported controls, extensions, features, and
 * naming contexts.  Reading it first lets us tailor LDAP operations to what
 * this particular server actually supports.
 * Think of it as the Imperial Star Destroyer's bridge manifest: the capability
 * listing at the very top of the directory tree.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IRootDSE extends IEntry
{
    // ── Han Checks Which Extended Operations The Ship Supports ───────────────────
    // "Does this ship have a tractor beam?  Does it support the StartTLS extension?"
    // LDAP extensions are optional extra operations identified by OID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OIDs of all LDAP extensions supported by this server.
     * Extensions are optional operation types beyond the base LDAP protocol —
     * for example, the StartTLS extended operation (OID 1.3.6.1.4.1.1466.20037).
     *
     * @return the extension OID strings; may be empty, never {@code null}.
     */
    String[] getSupportedExtensions();


    // ── Han Checks Which Controls The Ship's Systems Accept ──────────────────────
    // "Will this ship's computer accept the 'Paged Results' control packet?"
    // LDAP controls are optional modifiers attached to any LDAP operation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OIDs of all LDAP controls supported by this server.
     * Controls modify the behavior of LDAP operations — examples include the
     * Paged Results control (OID 1.2.840.113556.1.4.319) and Sort control.
     *
     * @return the control OID strings; may be empty, never {@code null}.
     */
    String[] getSupportedControls();


    // ── Han Checks Which Optional Feature Flags Are Enabled ──────────────────────
    // "Does the ship have enhanced targeting (modify-increment)?"
    // LDAP features are capability flags defined by the server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OIDs of all LDAP features supported by this server.
     * Features are optional capability flags — for example, the Modify Increment
     * feature (OID 1.3.6.1.1.14).
     *
     * @return the feature OID strings; may be empty, never {@code null}.
     */
    String[] getSupportedFeatures();


    // ── Han Checks Whether A Specific Extension Is Available ─────────────────────
    // "Quick check — does this ship have a tractor beam (StartTLS)?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the server supports the extension with the given OID.
     *
     * @param oid the extension OID to check.
     * @return {@code true} if the extension is in the supported list.
     */
    boolean isExtensionSupported( String oid );


    // ── Han Checks Whether A Specific Control Is Accepted ────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the server accepts the control with the given OID.
     *
     * @param oid the control OID to check.
     * @return {@code true} if the control is in the supported list.
     */
    boolean isControlSupported( String oid );


    // ── Han Checks Whether A Specific Feature Flag Is Set ────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the server supports the feature with the given OID.
     *
     * @param oid the feature OID to check.
     * @return {@code true} if the feature is in the supported list.
     */
    boolean isFeatureSupported( String oid );
}
