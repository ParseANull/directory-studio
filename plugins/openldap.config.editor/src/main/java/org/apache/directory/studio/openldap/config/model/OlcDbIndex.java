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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.studio.openldap.common.ui.model.DbIndexTypeEnum;


// ── CLASS: OlcDbIndex — C-3PO DECODING THE JAWA INDEX MANIFEST ───────────────
// In the Jawa sandcrawler, C-3PO is the only one who can parse the strange
// compressed manifest the Jawas scratched out — something like "R2,C3 pres,eq" —
// and turn it into a structured list: "R2-D2 and C-3PO, indexed for presence
// and equality." That's exactly what this class does for OpenLDAP index strings.
// An olcDbIndex value like "cn,sn pres,eq,sub" is a compact string we need to
// decode into a list of attribute names and a set of index type flags, then
// be able to encode back to string for writing to LDAP.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Models a single OpenLDAP database index definition, which controls what
 * attributes are indexed and what kind of searches those indexes support.
 * Think of it as the line item on C-3PO's decoded manifest: which droids
 * (attributes) and what capabilities (index types: pres, eq, sub, approx) are
 * available for fast lookup.
 * Used by the config editor's index widget and the I/O layer to parse and
 * format {@code olcDbIndex} attribute values.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcDbIndex
{
    /** The "default" special flag tag */
    private static final String DEFAULT_FLAG = "default";

    /** The space ' ' separator */
    private static final String SPACE_SEPARATOR = " ";

    /** The comma ',' separator */
    private static final String COMMA_SEPARATOR = ",";

    /** The default flag */
    private boolean isDefault = false;

    /** The list of attributes */
    private List<String> attributes = new ArrayList<>();

    /** The list of index types */
    private Set<DbIndexTypeEnum> indexTypes = new HashSet<>();


    // ── C-3PO Opens A Blank Manifest Page ────────────────────────────────────────
    // C-3PO cracks open a fresh, empty manifest page to record a new index entry
    // by hand — the editor will populate it attribute by attribute through setters
    // and add calls rather than via string parsing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new empty OlcDbIndex with no attributes and no index types.
     * Use this when building an index programmatically (e.g. in the editor UI)
     * and then calling {@link #addAttribute} and {@link #addIndexType} to populate it.
     *
     * <p>For example — C-3PO opens a blank manifest page:</p>
     * <pre>
     *   OlcDbIndex index = new OlcDbIndex();
     *   index.addAttribute("cn");
     *   index.addIndexType(DbIndexTypeEnum.EQ);
     * </pre>
     */
    public OlcDbIndex()
    {
    }


    // ── C-3PO Decodes A Compressed Jawa Manifest Line ─────────────────────────────
    // The Jawas hand C-3PO a single line of their cryptic manifest — "cn,sn pres,eq"
    // — and he decodes it on the spot: first the attribute names separated by commas,
    // then a space, then the index types separated by commas. This constructor does
    // exactly that: parse a raw olcDbIndex string into structured fields.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses an {@code olcDbIndex} attribute string from the LDAP entry and populates
     * this instance from it. The format is {@code "attr1,attr2 type1,type2"} — or just
     * {@code "default type1,type2"} for the default index entry.
     *
     * <p>For example — C-3PO decodes a line from the Jawa manifest:</p>
     * <pre>
     *   OlcDbIndex idx = new OlcDbIndex("cn,sn pres,eq");
     *   // idx.getAttributes() → ["cn", "sn"]
     *   // idx.getIndexTypes() → {PRES, EQ}
     * </pre>
     *
     * @param s  the raw olcDbIndex string to parse, e.g. "cn,sn pres,eq" or "default eq"
     */
    public OlcDbIndex( String s )
    {
        if ( s != null )
        {
            String[] components = s.split( SPACE_SEPARATOR );

            if ( components.length > 0 )
            {
                String[] attrs = components[0].split( COMMA_SEPARATOR );

                if ( attrs.length > 0 )
                {
                    for ( String attribute : attrs )
                    {
                        addAttribute( attribute );
                    }
                }

                if ( components.length == 2 )
                {
                    String[] indexes = components[1].split( COMMA_SEPARATOR );

                    if ( indexes.length > 0 )
                    {
                        for ( String indexType : indexes )
                        {
                            DbIndexTypeEnum type = DbIndexTypeEnum.valueOf( indexType );

                            if ( type != null )
                            {
                                addIndexType( type );
                            }
                        }
                    }
                }
            }
        }
    }


    // ── C-3PO Records A Droid Name On The Manifest ────────────────────────────────
    // C-3PO writes each droid's name onto the manifest line — if the name is
    // "default" he marks the whole line as the default index rather than adding
    // a named attribute, because that's a special keyword in the Jawa dialect.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an attribute name to this index definition. If the attribute is the
     * special string {@code "default"} (case-insensitive), it sets the default flag
     * instead of adding to the attribute list — "default" is OpenLDAP's keyword for
     * the catch-all index entry that applies to attributes not explicitly listed.
     *
     * <p>For example — C-3PO writes the droid name (or the "default" marker):</p>
     * <pre>
     *   index.addAttribute("cn");      // adds "cn" to attribute list
     *   index.addAttribute("default"); // sets isDefault = true instead
     * </pre>
     *
     * @param attribute  the attribute name to add, or "default" to set the default flag
     */
    public void addAttribute( String attribute )
    {
        if ( DEFAULT_FLAG.equalsIgnoreCase( attribute ) )
        {
            setDefault( true );
        }
        else
        {
            attributes.add( attribute );
        }
    }


    // ── Checking Whether This Is The Default Index Entry ──────────────────────────
    // C-3PO checks whether the manifest line is the catch-all entry — the one the
    // Jawas use as a fallback when no specific droid name was listed. This flag
    // tells us whether this index covers all otherwise-unlisted attributes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this is a "default" index entry — meaning it applies
     * to all attributes not explicitly listed in other index entries. In OpenLDAP
     * config, {@code olcDbIndex: default eq} creates a baseline equality index for
     * everything not individually specified.
     *
     * <p>For example — C-3PO checks the manifest for the catch-all marker:</p>
     * <pre>
     *   if (index.isDefault()) {
     *     // this index applies to all unlisted attributes
     *   }
     * </pre>
     *
     * @return  {@code true} if this is the default index entry
     */
    public boolean isDefault()
    {
        return isDefault;
    }


    // ── Marking The Entry As The Default Catch-All ────────────────────────────────
    // C-3PO marks a manifest line with the "default" flag when told it applies to
    // all droids not individually named — it's a special designation that changes
    // how the line serializes back to the LDAP entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the default flag on this index entry. When {@code true}, this index covers
     * all attributes not explicitly listed in dedicated index entries.
     *
     * <p>For example — C-3PO marks the line as the catch-all:</p>
     * <pre>
     *   index.setDefault(true);
     *   // toString() will now produce "default eq" instead of "cn eq"
     * </pre>
     *
     * @param isDefault  {@code true} to mark this as the default index entry
     */
    public void setDefault( boolean isDefault )
    {
        this.isDefault = isDefault;
    }


    // ── Recording A Search Capability On The Manifest ─────────────────────────────
    // C-3PO adds a capability code to the manifest line — "pres" means the droid
    // can be located by existence check, "eq" for exact match, "sub" for substring.
    // These codes tell the storage engine what kind of index structure to build.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds an index type (capability) to this index definition — e.g. {@code EQ} for
     * equality searches, {@code PRES} for presence checks, {@code SUB} for substring.
     * Stored as a Set so duplicate types are automatically ignored.
     *
     * <p>For example — C-3PO records an equality-search capability on the line:</p>
     * <pre>
     *   index.addIndexType(DbIndexTypeEnum.EQ);
     *   index.addIndexType(DbIndexTypeEnum.PRES);
     *   // toString() → "cn pres,eq"
     * </pre>
     *
     * @param indexType  the index type to add
     */
    public void addIndexType( DbIndexTypeEnum indexType )
    {
        indexTypes.add( indexType );
    }


    // ── Wiping The Attribute List Clean ───────────────────────────────────────────
    // C-3PO erases the droid names column from the manifest line — useful when the
    // editor needs to replace the entire attribute set rather than add to it
    // incrementally.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all attribute names from this index entry. Call this before re-populating
     * the attribute list from scratch — for example when the user replaces the entire
     * attribute selection in the editor UI.
     *
     * <p>For example — C-3PO erases the droid names column:</p>
     * <pre>
     *   index.clearAttributes();
     *   index.addAttribute("mail");  // start fresh with a new attribute
     * </pre>
     */
    public void clearAttributes()
    {
        attributes.clear();
    }


    // ── Wiping The Capabilities Column Clean ──────────────────────────────────────
    // C-3PO erases the capability codes column — useful when the editor needs to
    // swap out the entire set of index types rather than modify them one at a time.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all index types from this index entry. Use before re-populating the
     * type set from scratch.
     *
     * <p>For example — C-3PO erases the capability codes column:</p>
     * <pre>
     *   index.clearIndexTypes();
     *   index.addIndexType(DbIndexTypeEnum.SUB);  // fresh set of capabilities
     * </pre>
     */
    public void clearIndexTypes()
    {
        indexTypes.clear();
    }


    // ── Striking A Droid Name From The Manifest ───────────────────────────────────
    // C-3PO crosses a specific droid name off the manifest line — that attribute
    // should no longer be covered by this index entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific attribute name from this index entry's attribute list.
     *
     * <p>For example — C-3PO crosses a droid name off the list:</p>
     * <pre>
     *   index.removeAttribute("sn");  // "sn" is no longer indexed by this entry
     * </pre>
     *
     * @param attribute  the attribute name to remove
     */
    public void removeAttribute( String attribute )
    {
        attributes.remove( attribute );
    }


    // ── Striking A Capability Code From The Manifest ──────────────────────────────
    // C-3PO crosses a capability code off the line — for example, removing the
    // "sub" code means this index entry no longer supports substring searches.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific index type from this index entry's type set.
     *
     * <p>For example — C-3PO crosses the substring-search code off the manifest:</p>
     * <pre>
     *   index.removeIndexType(DbIndexTypeEnum.SUB);  // no more substring search on these attrs
     * </pre>
     *
     * @param indexType  the index type to remove
     */
    public void removeIndexType( DbIndexTypeEnum indexType )
    {
        indexTypes.remove( indexType );
    }


    // ── Reading The List Of Indexed Attribute Names ────────────────────────────────
    // C-3PO reads out the decoded list of droid names from the manifest line — the
    // caller can iterate over them to display in the editor table or validate them
    // against the schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of attribute names covered by this index entry.
     * The list is the live internal list — treat it as read-only unless you know
     * what you're doing.
     *
     * <p>For example — C-3PO reads out the decoded attribute names:</p>
     * <pre>
     *   for (String attr : index.getAttributes()) {
     *     System.out.println("Indexed: " + attr);  // "cn", "sn", etc.
     *   }
     * </pre>
     *
     * @return  the mutable list of attribute names on this index entry
     */
    public List<String> getAttributes()
    {
        return attributes;
    }


    // ── Reading The Set Of Index Capability Codes ─────────────────────────────────
    // C-3PO reads out the decoded set of capability codes — "pres, eq, sub" —
    // so the caller knows exactly which types of searches this index supports.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the set of index type flags on this index entry — e.g. {EQ, PRES, SUB}.
     * The set is the live internal set — treat as read-only unless you know what
     * you're doing.
     *
     * <p>For example — C-3PO reads out the capability codes:</p>
     * <pre>
     *   Set&lt;DbIndexTypeEnum&gt; types = index.getIndexTypes();
     *   // {EQ, PRES} → this index supports equality and presence searches
     * </pre>
     *
     * @return  the mutable set of index types on this entry
     */
    public Set<DbIndexTypeEnum> getIndexTypes()
    {
        return indexTypes;
    }


    // ── C-3PO Re-Encodes The Manifest Line ───────────────────────────────────────
    // After the editor makes changes, C-3PO re-encodes the manifest line back into
    // the Jawa format — "cn,sn pres,eq" — so it can be written back to the LDAP
    // entry as an olcDbIndex attribute value. This is the reverse of the string
    // constructor.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes this index entry back to the compact string format used in LDAP
     * {@code olcDbIndex} attribute values — e.g. {@code "cn,sn pres,eq"} or
     * {@code "default eq"}.
     *
     * <p>For example — C-3PO re-encodes the manifest line for writing back to LDAP:</p>
     * <pre>
     *   OlcDbIndex idx = new OlcDbIndex("cn,sn pres,eq");
     *   idx.removeIndexType(DbIndexTypeEnum.PRES);
     *   String updated = idx.toString();  // "cn,sn eq"
     * </pre>
     *
     * @return  the LDAP-format string representation of this index entry
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        if ( isDefault )
        {
            sb.append( DEFAULT_FLAG );
        }
        else
        {
            if ( !attributes.isEmpty() )
            {
                for ( String attribute : attributes )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        sb.append( COMMA_SEPARATOR );
                    }

                    sb.append( attribute );
                }
            }
        }

        if ( !indexTypes.isEmpty() )
        {
            sb.append( SPACE_SEPARATOR );
            isFirst = true;

            for ( DbIndexTypeEnum indexType : indexTypes )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    sb.append( COMMA_SEPARATOR );
                }

                sb.append( indexType.toString() );
            }
        }

        return sb.toString();
    }
}
