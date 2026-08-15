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
package org.apache.directory.studio.openldap.config.model;

// ── CLASS: OpenLdapVersion — The Death Star Construction Revision Number ──────
// Each iteration of the Death Star came with a different revision of the
// engineering specs — the first used a flawed exhaust port design, the second
// fixed that (and added a larger superlaser). Engineers needed to track which
// blueprint version they were working from to know which components were available.
// OpenLdapVersion plays the same role: each enum constant marks a specific release
// where new configuration attributes appeared, so the editor knows which attributes
// are valid for the server version the user is managing.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Enumerates the supported OpenLDAP release versions.
 * Each constant carries a version string (e.g., "2.4.34") and the enum is used
 * to guard which configuration attributes are available for a given server version.
 * Think of each constant as a revision stamp on the Death Star engineering blueprints
 * — only the components listed in that revision are available to the builders.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum OpenLdapVersion
{
    VERSION_2_4_0( "2.4.0" ),       // Most of the configuration AT were already defined in 2.4.0
                                    // Version prior to 2.4.6 were beta or alpha
    VERSION_2_4_6( "2.4.6" ),       // olcSortVals, olcServerID, olcTLSCRLFile
    VERSION_2_4_7( "2.4.7" ),       // olcIndexIntLen
    VERSION_2_4_8( "2.4.8" ),       //olcDbCryptFile, olcDbCryptKey, olcDbSocketPath, olcDbSocketExtensions, olcMemberOfDanglingError
    VERSION_2_4_9( "2.4.9" ),
    VERSION_2_4_10( "2.4.10" ),     // olcRefintModifiersName
    VERSION_2_4_11( "2.4.11" ),
    VERSION_2_4_12( "2.4.12" ),     // olcDbNoRefs, olcDbNoUndefFilter, olcLdapSyntaxes
    VERSION_2_4_13( "2.4.13" ),     // olcDbPageSize, olcAddContentAcl
    VERSION_2_4_14( "2.4.14" ),
    VERSION_2_4_15( "2.4.15" ),
    VERSION_2_4_16( "2.4.16" ),
    VERSION_2_4_17( "2.4.17" ),     // olcPPolicyForwardUpdates, olcSaslAuxprops, olcWriteTimeout
    VERSION_2_4_18( "2.4.18" ),     // olcTCPBuffer
    VERSION_2_4_19( "2.4.19" ),
    VERSION_2_4_20( "2.4.20" ),     // olcSyncUseSubentry
    VERSION_2_4_21( "2.4.21" ),
    VERSION_2_4_22( "2.4.22" ),     // olcExtraAttrs, olcDbIDAssertPassThru, olcSaslAuxpropsDontUseCopy, olcSaslAuxpropsDontUseCopyIgnore
    VERSION_2_4_23( "2.4.23" ),
    VERSION_2_4_24( "2.4.24" ),     // olcDbBindAllowed
    VERSION_2_4_25( "2.4.25" ),
    VERSION_2_4_26( "2.4.26" ),
    VERSION_2_4_27( "2.4.27" ),     // olcDbMaxReaders, olcDbMaxSize, olcDbMode, olcDbNoSync, olcDbSearchStack
    VERSION_2_4_28( "2.4.28" ),
    VERSION_2_4_29( "2.4.29" ),
    VERSION_2_4_30( "2.4.30" ),
    VERSION_2_4_31( "2.4.31" ),
    VERSION_2_4_32( "2.4.32" ),
    VERSION_2_4_33( "2.4.33" ),     // olcDbEnvFlags
    VERSION_2_4_34( "2.4.34" ),     // olcDbKeepalive, olcDbOnErr, olcIndexHash64
    VERSION_2_4_35( "2.4.35" ),
    VERSION_2_4_36( "2.4.36" ),     // olcDisabled, olcListenerThreads, olcThreadQueues
    VERSION_2_4_37( "2.4.37" ),     // olcTLSProtocolMin
    VERSION_2_4_38( "2.4.38" ),
    VERSION_2_4_39( "2.4.39" ),
    VERSION_2_4_40( "2.4.40" ),
    VERSION_2_4_41( "2.4.41" ),
    VERSION_2_4_42( "2.4.42" ),
    VERSION_2_4_43( "2.4.43" ),
    VERSION_2_4_44( "2.4.44" ),
    VERSION_2_4_45( "2.4.45" );

    /** The interned version */
    private String version;

    // ── Private Constructor — Stamp the Blueprint Revision Number ────────────────
    // Each version of the Death Star blueprint gets its revision number stamped
    // on the cover before being filed in the Imperial archives.
    // Each enum constant calls this to bind the human-readable version string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor that binds the version string to the enum constant.
     * Called implicitly by each constant declaration above.
     *
     * <p>For example — the blueprint gets its revision stamp:</p>
     * <pre>
     *   VERSION_2_4_34( "2.4.34" );  // "olcIndexHash64 added in this revision"
     * </pre>
     *
     * @param version  the OpenLDAP version string, e.g. "2.4.34"
     */
    private OpenLdapVersion( String version )
    {
        this.version = version;
    }


    // ── getVersion — Look Up Blueprint by Revision String ────────────────────────
    // An engineer scans the revision archive looking for the blueprint labelled
    // with a specific version string — if it's not found, we fall back to the
    // baseline 2.4.0 revision rather than crashing the whole operation.
    // We return the enum constant matching the version string for feature-gating.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the enum constant whose version string matches the given input.
     * Case-insensitive match. Falls back to VERSION_2_4_0 if no match is found,
     * which is the safe baseline — all config attributes from 2.4.0 will be known.
     *
     * <p>For example — the engineer looks up blueprint "2.4.34":</p>
     * <pre>
     *   OpenLdapVersion v = OpenLdapVersion.getVersion( "2.4.34" );
     *   // returns VERSION_2_4_34
     * </pre>
     *
     * @param version  the version string to look up (e.g., "2.4.34")
     * @return         the matching enum constant, or VERSION_2_4_0 if not found
     */
    public static OpenLdapVersion getVersion( String version )
    {
        for ( OpenLdapVersion openLDAPVersion : OpenLdapVersion.values() )
        {
            if ( openLDAPVersion.version.equalsIgnoreCase( version ) )
            {
                return openLDAPVersion;
            }
        }

        return OpenLdapVersion.VERSION_2_4_0;
    }


    // ── getValue — Read the Revision Label ───────────────────────────────────────
    // The engineer reads the version number stamped on this particular blueprint.
    // That's the human-readable string we store and serialize.
    // We expose it so code can get "2.4.34" out of VERSION_2_4_34.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the version string bound to this constant (e.g., "2.4.34").
     * Use this when you need the string form for comparison or display.
     *
     * <p>For example — the engineer reads the revision label:</p>
     * <pre>
     *   String v = VERSION_2_4_34.getValue(); // "2.4.34"
     * </pre>
     *
     * @return  the OpenLDAP version string for this constant
     */
    public String getValue()
    {
        return version;
    }


    // ── getVersions — List All Revisions Newest First ────────────────────────────
    // The Imperial archive curator hands over a list of all blueprint revision numbers,
    // newest revisions at the top so engineers start from the most current spec.
    // We return a String array in reverse order (newest first) for UI dropdowns.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all known version strings in reverse order — newest first.
     * This is handy for populating UI dropdowns where users pick the server version.
     *
     * <p>For example — the curator hands over the full revision list:</p>
     * <pre>
     *   String[] versions = OpenLdapVersion.getVersions();
     *   // versions[0] == "2.4.45" (newest), versions[last] == "2.4.0" (oldest)
     * </pre>
     *
     * @return  a String[] of version strings, newest first
     */
    public static String[] getVersions()
    {
        OpenLdapVersion[] values = OpenLdapVersion.values();
        String[] versions = new String[values.length];
        int i = values.length - 1;

        for ( OpenLdapVersion value : values )
        {
            versions[i--] = value.version;
        }

        return versions;
    }
}
