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
package org.apache.directory.studio.openldap.config.editor.wrappers;

// ── CLASS: DbIndexWrapper — The Imperial Archivist's Attribute Index Card ─────
// The Imperial archivist maintains a card catalogue for the data vault: each
// card lists one or more attribute names on the left and the index types
// (presence, equality, substring, etc.) on the right.  A special card marked
// "default" sets the default index for any attribute not otherwise listed.
// DbIndexWrapper is one of those index cards — it parses the olcDbIndex string,
// keeps the attribute set and index-type set sorted, and serializes them back
// to a config string.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A wrapper for an olcDbIndex value used by BDB, MDB, and HDB databases.
 * It parses the "{attribute,...} {indexType,...}" format, stores the parts in
 * sorted sets, and serializes them back on toString.  A special "default" flag
 * replaces all named attributes with the keyword "default".
 *
 * <pre>
 * olcDbIndex ::= ATTR attr-list index-types-e | 'default' index-types
 * attr-list  ::= ',' ATTR attr-list | e
 * index-types ::= type types-e
 * types-e    ::= ',' type types-e | e
 * type       ::= 'pres' | 'eq' | 'sub' | 'approx' | 'subinitial' | 'subany' |
 *                'subfinal' | 'substr' | 'notags' | 'nolang' | 'nosubtypes'
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DbIndexWrapper implements Cloneable, Comparable<DbIndexWrapper>
{
    /** A flag set when the 'default' special attribute is used */
    private boolean isDefault;

    /** The set of configured attributes */
    private Set<String> attributes = new TreeSet<>( ( string1, string2 ) ->
            {
                if ( string1 == null )
                {
                    return -1;
                }
                else if ( string2 == null )
                {
                    return 1;
                }

                return string1.compareTo( string2 );
            }
    );

    /** The set of configured attributes */
    private Set<DbIndexTypeEnum> indexTypes = new TreeSet<>();


    // ── Constructor — Reading and Parsing an Index Card ───────────────────────
    // The archivist reads the index card string and fills in the attribute set
    // and index-type set.  If the card begins with "default", all named
    // attributes are discarded.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Build a DbIndexWrapper from a String containing the description of the index.
     *
     * @param indexStr The String that describes the index
     */
    public DbIndexWrapper( String indexStr )
    {
        // We first have to parse the attributes. It's a list of Strings, or OIDs, separated
        // by ',' and that ends at the first ' ' or the end of the line.
        int pos = 0;
        int startPos = 0;
        boolean endAttributes = false;

        // Valid chars are 'a'-'z', 'A'-'Z', '0'-'9', '.', '-' and '_'
        for ( pos = 0; pos < indexStr.length(); pos++ )
        {
            char c = indexStr.charAt( pos );
            endAttributes = c == ' ';

            if ( ( c == ',' ) || endAttributes )
            {
                // This is the end of the attribute
                String attrStr = indexStr.substring( startPos, pos );

                if ( "default".equalsIgnoreCase( attrStr ) )
                {
                    isDefault = true;
                    startPos = pos + 1;
                    break;
                }

                // Check that it's a valid Attribute
                attributes.add( Strings.toLowerCaseAscii( attrStr ) );
                startPos = pos + 1;
            }

            if ( endAttributes )
            {
                break;
            }
        }

        // If the 'default' special attribute is present, we can discard all the other attributes
        if ( isDefault )
        {
            attributes.clear();
        }

        if ( endAttributes )
        {
            pos++;

            // Ok, we are done with the attributes, let's process the indexTypes now,
            // starting where we left
            for ( ; pos < indexStr.length(); pos++ )
            {
                char c = indexStr.charAt( pos );

                if ( c == ',' )
                {
                    String indexTypeName = indexStr.substring( startPos, pos );

                    // Check if we have this indexType
                    DbIndexTypeEnum indexType = DbIndexTypeEnum.getIndexType( indexTypeName );

                    if ( indexType != DbIndexTypeEnum.NONE )
                    {
                        indexTypes.add( indexType );
                    }

                    startPos = pos + 1;
                }
            }

            if ( pos != startPos )
            {
                // Search for the index type
                String indexTypeName = indexStr.substring( startPos, pos );

                // Check if we have this indexType
                DbIndexTypeEnum indexType = DbIndexTypeEnum.getIndexType( indexTypeName );

                if ( indexType != DbIndexTypeEnum.NONE )
                {
                    indexTypes.add( indexType );
                }
            }
        }
    }


    // ── isDefault — Check if This Card Uses the Default Keyword ───────────────
    /**
     * @return the isDefault
     */
    public boolean isDefault()
    {
        return isDefault;
    }


    // ── setDefault — Mark This Card as the Default Index Entry ─────────────────
    /**
     * @param isDefault the isDefault to set
     */
    public void setDefault( boolean isDefault )
    {
        this.isDefault = isDefault;
    }


    // ── getAttributes — Return the Named Attribute Set ────────────────────────
    /**
     * @return The set of attributes
     */
    public Set<String> getAttributes()
    {
        return attributes;
    }


    // ── getIndexTypes — Return the Index Type Set ──────────────────────────────
    /**
     * @return the indexTypes
     */
    public Set<DbIndexTypeEnum> getIndexTypes()
    {
        return indexTypes;
    }


    // ── getTypes — Alias for getIndexTypes ────────────────────────────────────
    /**
     * @return The set of index types
     */
    public Set<DbIndexTypeEnum> getTypes()
    {
        return indexTypes;
    }


    // ── clone — Duplicate the Index Card ──────────────────────────────────────
    // The archivist makes a copy of the card, deep-cloning both sorted sets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#clone()
     */
    public DbIndexWrapper clone()
    {
        try
        {
            DbIndexWrapper clone = (DbIndexWrapper)super.clone();

            // Clone the attributes
            clone.attributes = new TreeSet<>();
            clone.attributes.addAll( attributes );

            // Clone the indexTypes
            clone.indexTypes = new TreeSet<>();
            clone.indexTypes.addAll( indexTypes );

            return clone;
        }
        catch ( CloneNotSupportedException cnse )
        {
            return null;
        }
    }


    // ── hashCode — Compute a Hash from the Attribute Set ──────────────────────
    // Hash is based only on attribute names (not index types).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        int h = 37;

        // Only iterate on Attributes
        for ( String attribute : attributes )
        {
            h += h*17 + attribute.hashCode();
        }

        return h;
    }


    // ── equals — Check If Two Index Cards Are the Same ────────────────────────
    // Two index cards are equal when their compareTo returns 0.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#equals()
     */
    public boolean equals( Object that )
    {
        if ( that == this )
        {
            return true;
        }

        if ( ! ( that instanceof DbIndexWrapper ) )
        {
            return false;
        }

        return compareTo( (DbIndexWrapper)that ) == 0;
    }


    // ── compareTo — Compare Index Cards by Their Attribute Sets ───────────────
    // The archivist compares attribute sets element by element; the shorter set
    // comes first when all shared elements are equal.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Comparable#compareTo()
     */
    public int compareTo( DbIndexWrapper that )
    {
        // Compare by attributes only.
        if ( that == null )
        {
            return 1;
        }

        // we will iterate on the two sets in parallel.
        int limit = Math.min( attributes.size(), that.attributes.size() );
        Iterator<String> thisIterator = attributes.iterator();
        Iterator<String> thatIterator = that.attributes.iterator();

        for ( int i = 0; i <  limit; i++ )
        {
            int result = thisIterator.next().compareTo( thatIterator.next() );

            if ( result != 0)
            {
                return result;
            }
        }

        return attributes.size() - that.attributes.size();
    }


    // ── toString — Serialize the Index Card to a Config String ─────────────────
    // The archivist writes out the card in the olcDbIndex format:
    // "attr1,attr2 type1,type2" or "default type1,type2".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // first, the Attribute, if it's not default
        if ( isDefault )
        {
            if ( indexTypes.isEmpty() )
            {
                // No types either ? return a blank String
                return "";
            }

            sb.append( "default" );
        }
        else
        {
            boolean isFirst = true;

            for ( String attribute : attributes )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    sb.append( ',' );
                }

                sb.append( attribute );
            }
        }

        // A space before the indexTypes
        sb.append( ' ' );

        if ( indexTypes.isEmpty() )
        {
            // No type ? Use default
            sb.append( "default" );
        }
        else
        {
            boolean isFirst = true;

            for ( DbIndexTypeEnum indexType : indexTypes )
            {
                if ( isFirst )
                {
                    isFirst = false;
                }
                else
                {
                    sb.append( ',' );
                }

                sb.append( indexType.getName() );
            }
        }

        return sb.toString();
    }
}
