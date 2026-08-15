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
package org.apache.directory.studio.openldap.config.model.overlay;


// ── CLASS: OlcRwmMapValueTypeEnum — C-3PO Selects the Right Translation Table ─
// When C-3PO starts translating, he first decides which table to open — the
// attribute phrase-book or the object class concept-book. Each type of schema
// element has its own mapping table in the rwm overlay.
// This enum encodes exactly that choice: are we mapping an attribute type name,
// or an object class name? The keyword appears at the start of each olcRwmMap
// rule (e.g., "attribute uid login" vs. "objectclass inetOrgPerson person").
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the type discriminator at the start of an {@code olcRwmMap} rule,
 * indicating whether the rule maps an attribute type name or an object class name.
 * Think of this as C-3PO choosing the correct translation table before applying a rule.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum OlcRwmMapValueTypeEnum
{
    /** Enum value for 'attribute' */
    ATTRIBUTE,

    /** Enum value for 'objectclass' */
    OBJECTCLASS;

    /** The constant string for 'attribute' */
    private static final String ATTRIBUTE_STRING = "attribute";

    /** The constant string for 'objectclass' */
    private static final String OBJECTCLASS_STRING = "objectclass";


    // ── fromString — C-3PO Identifies Which Table to Open ─────────────────────────
    // C-3PO reads the keyword at the start of a mapping rule and decides which table
    // to open: attribute phrase-book or objectclass concept-book.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the type keyword from an olcRwmMap rule string into this enum.
     * Comparison is case-insensitive ("Attribute" and "ATTRIBUTE" both map to ATTRIBUTE).
     *
     * <p>For example — C-3PO identifies the table:</p>
     * <pre>
     *   OlcRwmMapValueTypeEnum type =
     *       OlcRwmMapValueTypeEnum.fromString( "objectclass" );
     *   // returns OBJECTCLASS
     * </pre>
     *
     * @param s  the keyword string from the olcRwmMap rule (e.g., "attribute", "objectclass")
     * @return   the matching enum constant, or null if the string doesn't match
     */
    public static OlcRwmMapValueTypeEnum fromString( String s )
    {
        if ( ATTRIBUTE_STRING.equalsIgnoreCase( s ) )
        {
            return ATTRIBUTE;
        }
        else if ( OBJECTCLASS_STRING.equalsIgnoreCase( s ) )
        {
            return OBJECTCLASS;
        }

        return null;
    }


    // ── toString — C-3PO Records the Table Name in the Attribute ─────────────────
    // C-3PO writes the table name back into the olcRwmMap attribute value in the
    // exact lowercase format the rwm overlay expects.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string representation suitable for the olcRwmMap attribute value.
     *
     * <p>For example — C-3PO records the table name:</p>
     * <pre>
     *   OlcRwmMapValueTypeEnum.ATTRIBUTE.toString();  // "attribute"
     *   OlcRwmMapValueTypeEnum.OBJECTCLASS.toString(); // "objectclass"
     * </pre>
     *
     * @return  the lowercase type keyword for the olcRwmMap attribute
     */
    @Override
    public String toString()
    {
        switch ( this )
        {
            case ATTRIBUTE:
                return ATTRIBUTE_STRING;
            case OBJECTCLASS:
                return OBJECTCLASS_STRING;
        }

        return super.toString();
    }
}
