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
package org.apache.directory.studio.openldap.common.ui.model;


// ── CLASS: OverlayTypeEnum — FORCE ABILITIES FOR JEDI OVERLAYS ───────────────
// Think of each overlay as a distinct Force ability a Jedi can layer on top of
// their base training: Access Log records every action in the mission log,
// Audit Log keeps an immutable chain-of-custody record, Member Of
// automatically maintains group membership, Password Policy enforces Jedi code
// discipline, Referential Integrity keeps the holocron cross-references
// consistent, Rewrite/Remap lets a Master reshape the data flow, Sync Prov
// handles galactic replication, and Value Sorting keeps the archives orderly.
// UNKNOWN is the fallback for unrecognized overlay names.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized OpenLDAP overlay types. Each constant carries
 * the human-readable display name shown in the configuration editor.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum OverlayTypeEnum
{
    /** Access Log */
    ACCESS_LOG( "Access Log" ),

    /** Audit Log */
    AUDIT_LOG( "Audit Log" ),

    /** Member Of */
    MEMBER_OF( "Member Of" ),

    /** Password Policy */
    PASSWORD_POLICY( "Password Policy" ),

    /** Referential Integrity */
    REFERENTIAL_INTEGRITY( "Referential Integrity" ),

    /** Rewrite/Remap */
    REWRITE_REMAP( "Rewrite/Remap" ),

    /** Sync Prov (Replication) */
    SYNC_PROV( "Sync Prov (Replication)" ),

    /** Value Sorting */
    VALUE_SORTING( "Value Sorting" ),

    /** Unknown */
    UNKNOWN( "" );

    /** The Overlay name */
    private String name;

    // ── CONSTRUCTOR: OverlayTypeEnum — CATALOGUING A FORCE ABILITY ───────────
    // Each overlay gets its display name so the UI can show a meaningful label
    // rather than the raw enum identifier.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its human-readable display name.
     *
     * @param name  the display name for this overlay type
     */
    private OverlayTypeEnum( String name )
    {
        this.name = name;
    }

    // ── METHOD: getName — READING THE FORCE ABILITY LABEL ────────────────────
    // We return the display name as it should appear in the UI configuration
    // editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the human-readable display name for this overlay type
     * (e.g., {@code "Access Log"}).
     *
     * @return the overlay display name
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getOverlay — IDENTIFYING A FORCE ABILITY FROM ITS NAME ────────
    // We scan all constants for a case-insensitive match on the display name.
    // If no match is found we return UNKNOWN so callers always get a non-null
    // result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up an {@link OverlayTypeEnum} constant by its display name using
     * case-insensitive comparison. We return {@link #UNKNOWN} if no constant
     * matches.
     *
     * @param name  the overlay display name to look up
     * @return      the matching enum constant, or {@link #UNKNOWN}
     */
    public static OverlayTypeEnum getOverlay( String name )
    {
        for ( OverlayTypeEnum overlay : values() )
        {
            if ( overlay.name.equalsIgnoreCase( name ) )
            {
                return overlay;
            }
        }

        return UNKNOWN;
    }


    // ── METHOD: getNames — LISTING ALL FORCE ABILITIES ────────────────────────
    // We compile the full list of overlay display names for combo-box
    // population in the overlay configuration editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all overlay display name strings in declaration
     * order, suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( OverlayTypeEnum overlayType : values() )
        {
            names[pos] = overlayType.name;
            pos++;
        }

        return names;
    }
}
