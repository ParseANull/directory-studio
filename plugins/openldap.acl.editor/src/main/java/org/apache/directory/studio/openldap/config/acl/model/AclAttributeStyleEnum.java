/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.model;


// ── CLASS: AclAttributeStyleEnum — IMPERIAL DATABASE QUERY MODES ─────────────
// When Tarkin's analysts search the Imperial database for a value they specify
// how to match it: exact match, base-object only, or a regex pattern. OpenLDAP
// uses these same matching styles in the "val" part of an attribute clause.
// This enum names those styles and lets us convert a style string from the
// ACL text back to the enum constant.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The matching style applied to the {@code val=} part of an attribute clause in
 * a what-clause. For example {@code attrs=userPassword val.exact="secret"} uses
 * EXACT matching. OpenLDAP supports exact, base, baseobject, and regex. NONE is
 * our sentinel meaning "no style was specified".
 * Think of this enum as the Imperial database's query modes — the analyst
 * specifies whether they want an exact record, a base-level scan, or a
 * pattern search.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclAttributeStyleEnum
{
    EXACT( "exact" ),
    BASE( "base" ),
    BASE_OBJECT( "baseobject" ),
    REGEX( "regex" ),
    NONE( "---" );

    /** The interned name */
    private String name;


    // ── Registering the Style With Its Protocol Name ───────────────────────────
    // Each query mode has a canonical protocol name the Imperial database uses;
    // we store that name so we can write it back into the ACL text or display
    // it in a combo box without any ad-hoc string literals scattered around.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a style constant with the OpenLDAP wire-format name.
     *
     * @param name  The exact string OpenLDAP uses for this style in ACL text.
     */
    private AclAttributeStyleEnum( String name )
    {
        this.name = name;
    }


    // ── Reading the Protocol Name ─────────────────────────────────────────────
    // The analyst reads off the query mode name exactly as stored in the
    // protocol spec, so nothing gets mistranslated en route to the server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLDAP wire-format name for this style. Use this when
     * building ACL text or populating a UI combo box with the known styles.
     *
     * <p>For example — Tarkin's analyst reading the query mode label:</p>
     * <pre>
     *   AclAttributeStyleEnum.EXACT.getName() // → "exact"
     *   AclAttributeStyleEnum.REGEX.getName() // → "regex"
     *   AclAttributeStyleEnum.NONE.getName()  // → "---"
     * </pre>
     *
     * @return  The OpenLDAP name string for this style.
     */
    public String getName()
    {
        return name;
    }


    // ── Looking Up a Style by Its Name ────────────────────────────────────────
    // When C-3PO parses the ACL text he finds the style keyword and needs to
    // convert it back to the enum constant. We iterate the values and compare
    // case-insensitively so "Exact" and "exact" both work.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the enum constant whose name matches the given string,
     * case-insensitively. Returns {@link #NONE} if no match is found —
     * that way callers never get a {@code null} back and can test for NONE
     * explicitly.
     *
     * <p>For example — C-3PO reading "exact" from the ACL and looking it up:</p>
     * <pre>
     *   AclAttributeStyleEnum style = AclAttributeStyleEnum.getStyle("exact");
     *   // style == AclAttributeStyleEnum.EXACT
     *   AclAttributeStyleEnum unknown = AclAttributeStyleEnum.getStyle("blah");
     *   // unknown == AclAttributeStyleEnum.NONE
     * </pre>
     *
     * @param name  The style name to look up (case-insensitive).
     * @return      The matching constant, or {@link #NONE} if nothing matches.
     */
    public static AclAttributeStyleEnum getStyle( String name )
    {
        for ( AclAttributeStyleEnum style : values() )
        {
            if ( style.name.equalsIgnoreCase( name ) )
            {
                return style;
            }
        }

        return NONE;
    }


    // ── Listing All Known Style Names ─────────────────────────────────────────
    // The combo box in the editor UI needs a String array of all style names
    // to populate its drop-down. We build that array from the enum values so
    // the UI and the model always agree on what is available.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@code String[]} of all style names in declaration order — useful
     * for populating UI combo boxes so users can pick a matching style.
     *
     * <p>For example — populating a style combo box in the visual editor:</p>
     * <pre>
     *   String[] styles = AclAttributeStyleEnum.getNames();
     *   // ["exact", "base", "baseobject", "regex", "---"]
     *   combo.setItems(styles);
     * </pre>
     *
     * @return  Array of all wire-format style name strings.
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( AclAttributeStyleEnum AclAttributeStyleEnum : values() )
        {
            names[pos] = AclAttributeStyleEnum.name;
            pos++;
        }

        return names;
    }
}
