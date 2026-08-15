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


import java.text.ParseException;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Position;
import org.apache.directory.api.util.Strings;


// ── CLASS: OlcValSortValue — Lando's Single Ledger Entry with Sort Rule ───────
// Each entry in Lando's Cloud City trade ledger says: "for THIS attribute, under
// THESE entries (the base DN), sort the values using THIS algorithm — alpha-ascend,
// numeric-descend, etc." And optionally, values can carry their own weight tags
// so the most important ones always float to the top.
// OlcValSortValue represents a single such entry: the attribute name, the base DN
// scope, whether weighting is in play, and the sort method. It parses itself from
// the raw olcValSortAttr string format that OpenLDAP stores in cn=config.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single {@code olcValSortAttr} value, which defines one sort rule
 * for the valsort overlay. The format is:
 * {@code <attribute> "<baseDn>" [weighted] <sortMethod>}
 * Think of this as one entry in Lando's Cloud City trade ledger — attribute name,
 * scope, and ordering preference.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcValSortValue
{
    /** The 'weighted' constant string */
    private static final String WEIGHTED_STRING = "weighted";

    /** The attribute */
    private String attribute;

    /** The base DN */
    private Dn baseDn;

    /** The sort method */
    private OlcValSortMethodEnum sortMethod;

    private boolean isWeighted = false;


    // ── getAttribute — Lando Reads Which Attribute This Rule Covers ───────────────
    // Lando checks which attribute's values this sort rule applies to.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type name this sort rule applies to.
     *
     * <p>For example — Lando reads the attribute:</p>
     * <pre>
     *   String attr = valSortValue.getAttribute(); // "member"
     * </pre>
     *
     * @return  the attribute type name string
     */
    public String getAttribute()
    {
        return attribute;
    }


    // ── getBaseDn — Lando Reads the Ledger Scope ──────────────────────────────────
    // Lando checks which subtree this sort rule governs — only entries under this
    // DN get their attribute values sorted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the base DN that scopes this sort rule.
     * Only entries at or below this DN will have their attribute values sorted.
     *
     * <p>For example — Lando reads the scope:</p>
     * <pre>
     *   Dn base = valSortValue.getBaseDn();
     *   // e.g., ou=groups,dc=example,dc=com
     * </pre>
     *
     * @return  the base Dn for this sort rule
     */
    public Dn getBaseDn()
    {
        return baseDn;
    }


    // ── getSortMethod — Lando Reads the Ordering Algorithm ────────────────────────
    // Lando checks which ordering algorithm applies to this ledger entry:
    // alpha-ascend, alpha-descend, numeric-ascend, or numeric-descend.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sort method (algorithm) for this rule.
     *
     * <p>For example — Lando reads the ordering algorithm:</p>
     * <pre>
     *   OlcValSortMethodEnum method = valSortValue.getSortMethod();
     *   // e.g., ALPHA_ASCEND
     * </pre>
     *
     * @return  the OlcValSortMethodEnum for this sort rule
     */
    public OlcValSortMethodEnum getSortMethod()
    {
        return sortMethod;
    }


    // ── isWeighted — Lando Checks if Values Carry Their Own Priority Tags ─────────
    // Lando checks whether individual values in this attribute carry their own weight
    // tags (integer prefixes like "10:cn=admin,...") that override the global ordering.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this sort rule uses weighted ordering.
     * In weighted mode, attribute values carry integer prefix weights that determine
     * their position in the sorted output rather than the sort method alone.
     *
     * <p>For example — Lando checks for weighted mode:</p>
     * <pre>
     *   if ( valSortValue.isWeighted() ) {
     *       // values have "10:..." integer prefix weights
     *   }
     * </pre>
     *
     * @return  true if weighted mode is enabled, false otherwise
     */
    public boolean isWeighted()
    {
        return isWeighted;
    }


    // ── setAttribute — Lando Sets the Attribute for This Sort Rule ────────────────
    // Lando records which attribute this ledger entry governs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the attribute type name this sort rule applies to.
     *
     * <p>For example — Lando sets the attribute:</p>
     * <pre>
     *   valSortValue.setAttribute( "member" );
     * </pre>
     *
     * @param attribute  the attribute type name
     */
    public void setAttribute( String attribute )
    {
        this.attribute = attribute;
    }


    // ── setBaseDn — Lando Sets the Scope for This Sort Rule ──────────────────────
    // Lando records the subtree DN that scopes this ledger sort rule.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the base DN that scopes this sort rule.
     *
     * <p>For example — Lando sets the scope:</p>
     * <pre>
     *   valSortValue.setBaseDn( new Dn( "ou=groups,dc=example,dc=com" ) );
     * </pre>
     *
     * @param baseDn  the Dn scoping this sort rule
     */
    public void setBaseDn( Dn baseDn )
    {
        this.baseDn = baseDn;
    }


    // ── setSortMethod — Lando Sets the Ordering Algorithm ────────────────────────
    // Lando sets which ordering algorithm to use for this ledger entry's values.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the sort method (algorithm) for this rule.
     *
     * <p>For example — Lando sets the ordering algorithm:</p>
     * <pre>
     *   valSortValue.setSortMethod( OlcValSortMethodEnum.NUMERIC_DESCEND );
     * </pre>
     *
     * @param sortMethod  the OlcValSortMethodEnum to use
     */
    public void setSortMethod( OlcValSortMethodEnum sortMethod )
    {
        this.sortMethod = sortMethod;
    }


    // ── setWeighted — Lando Enables or Disables Priority Tags ────────────────────
    // Lando marks this sort rule as using value-level integer weight tags.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether this sort rule uses weighted ordering.
     *
     * <p>For example — Lando enables weighted mode:</p>
     * <pre>
     *   valSortValue.setWeighted( true );
     * </pre>
     *
     * @param isWeighted  true to enable weighted mode, false to disable
     */
    public void setWeighted( boolean isWeighted )
    {
        this.isWeighted = isWeighted;
    }


    // ── toString — Lando Writes the Sort Rule Back to LDAP Format ────────────────
    // Lando formats this ledger entry back into the compact string the valsort overlay
    // expects in the olcValSortAttr attribute.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns this sort rule as a string in the olcValSortAttr format:
     * {@code <attribute> "<baseDn>" [weighted] <sortMethod>}
     *
     * <p>For example — Lando formats the entry:</p>
     * <pre>
     *   valSortValue.toString();
     *   // e.g., "member \"ou=groups,dc=example,dc=com\" alpha-ascend"
     * </pre>
     *
     * @return  the formatted olcValSortAttr value string
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // Attribute
        sb.append( attribute );
        sb.append( ' ' );

        // Base DN
        boolean baseDnNeedsEscaping = false;

        if ( baseDn != null )
        {
            baseDnNeedsEscaping = needsEscaping( baseDn.toString() );
        }

        if ( baseDnNeedsEscaping )
        {
            sb.append( '"' );
        }

        sb.append( baseDn );

        if ( baseDnNeedsEscaping )
        {
            sb.append( '"' );
        }

        sb.append( ' ' );

        // Weighted
        if ( isWeighted )
        {
            sb.append( WEIGHTED_STRING );

            // Sort method
            if ( sortMethod != null )
            {
                // Sort method
                sb.append( ' ' );
                sb.append( sortMethod );
            }
        }
        else
        {
            // Sort method
            sb.append( sortMethod );
        }

        return sb.toString();
    }


    // ── needsEscaping — Lando Checks if the DN Needs Quotes ──────────────────────
    // Lando checks whether a DN string contains spaces and therefore needs to be
    // wrapped in double quotes in the attribute value string.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the given string contains spaces and needs to be quoted
     * in the serialized olcValSortAttr value.
     *
     * @param s  the string to check
     * @return   true if quoting is needed
     */
    private boolean needsEscaping( String s )
    {
        if ( s != null )
        {
            return s.contains( " " );
        }

        return false;
    }


    // ── parse — Lando Reads a Raw Sort Rule from the LDAP Attribute ──────────────
    // Lando reads a raw olcValSortAttr string from the LDAP attribute and parses it
    // into a structured OlcValSortValue object, handling quoted DNs, optional
    // "weighted" keywords, and sort method identification.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a raw {@code olcValSortAttr} attribute value string into an OlcValSortValue.
     * The format is: {@code <attribute> "<baseDn>" [weighted] <sortMethod>}
     * This method is synchronized because it uses a shared Position object.
     *
     * <p>For example — Lando reads a raw sort rule:</p>
     * <pre>
     *   OlcValSortValue val = OlcValSortValue.parse(
     *       "member \"ou=groups,dc=example,dc=com\" alpha-ascend" );
     *   val.getAttribute();   // "member"
     *   val.getSortMethod();  // ALPHA_ASCEND
     * </pre>
     *
     * @param s  the raw olcValSortAttr value string
     * @return   the parsed OlcValSortValue, or null if the string is null
     * @throws ParseException  if the string doesn't match the expected format
     */
    public static synchronized OlcValSortValue parse( String s ) throws ParseException
    {
        if ( s == null )
        {
            return null;
        }

        // Trimming the value
        s = Strings.trim( s );

        // Getting the chars of the string
        char[] chars = s.toCharArray();

        // Creating the position
        Position pos = new Position();
        pos.start = 0;
        pos.end = 0;
        pos.length = chars.length;

        return parseInternal( chars, pos );
    }


    // ── parseInternal — Lando's Internal Token Parser ────────────────────────────
    // Lando's internal loop reads tokens one at a time: attribute, base DN, optional
    // "weighted", and sort method. He handles the tricky logic of distinguishing
    // "weighted" from a sort method keyword.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal parse implementation for OlcValSortValue. Reads tokens sequentially
     * from the character array using a Position cursor.
     *
     * @param chars  the character array to parse
     * @param pos    the current position (mutated in place)
     * @return       the parsed OlcValSortValue, or null if input is empty
     * @throws ParseException  if any required token is missing or unrecognized
     */
    private static OlcValSortValue parseInternal( char[] chars, Position pos ) throws ParseException
    {
        OlcValSortValue olcValSortValue = new OlcValSortValue();

        // Empty (trimmed) string?
        if ( chars.length == 0 )
        {
            return null;
        }

        do
        {
            // Attribute
            String attribute = getQuotedOrNotQuotedOptionValue( chars, pos );

            if ( ( attribute != null ) && ( !attribute.isEmpty() ) )
            {
                olcValSortValue.setAttribute( attribute );
            }
            else
            {
                throw new ParseException( "Could not find the 'Attribute' value", pos.start );
            }

            // Base DN
            String baseDn = getQuotedOrNotQuotedOptionValue( chars, pos );

            if ( ( baseDn != null ) && ( !baseDn.isEmpty() ) )
            {
                try
                {
                    olcValSortValue.setBaseDn( new Dn( baseDn ) );
                }
                catch ( LdapInvalidDnException e )
                {
                    throw new ParseException( "Could not convert '" + baseDn + "' to a valid DN.", pos.start );
                }
            }
            else
            {
                throw new ParseException( "Could not find the 'Base DN value", pos.start );
            }

            // Getting the next item
            // It can either "weighted" or a sort method
            String weightedOrSortMethod = getQuotedOrNotQuotedOptionValue( chars, pos );

            if ( ( weightedOrSortMethod != null ) && ( !weightedOrSortMethod.isEmpty() ) )
            {
                // Weighted
                if ( isWeighted( weightedOrSortMethod ) )
                {
                    olcValSortValue.setWeighted( true );
                }
                // Sort Method
                else if ( isSortMethod( weightedOrSortMethod ) )
                {
                    olcValSortValue.setSortMethod( OlcValSortMethodEnum.fromString( weightedOrSortMethod ) );
                }
                else
                {
                    throw new ParseException( "Could not identify keyword '" + weightedOrSortMethod
                        + "' as a valid sort method.", pos.start );
                }
            }

            // Getting the next item
            // It should not exist if the previous item was "weighted" and
            // must a sort method it the previous item was "weighted"
            String sortMethod = getQuotedOrNotQuotedOptionValue( chars, pos );

            if ( ( sortMethod != null ) && ( !sortMethod.isEmpty() ) )
            {
                if ( olcValSortValue.isWeighted() )
                {
                    if ( isSortMethod( sortMethod ) )
                    {
                        olcValSortValue.setSortMethod( OlcValSortMethodEnum.fromString( sortMethod ) );
                    }
                    else
                    {
                        throw new ParseException( "Could not identify keyword '" + sortMethod
                            + "' as a valid sort method.", pos.start );
                    }
                }
                else
                {
                    throw new ParseException( "Keyword '" + sortMethod + "' is not allowed after sort method.",
                        pos.start );
                }
            }
        }
        while ( ( pos.start != pos.length ) && ( ( Strings.charAt( chars, pos.start ) ) != '\0' ) );

        return olcValSortValue;
    }


    /**
     * Indicates if the given string is "weighted".
     *
     * @param s the string to test
     * @return <code>true</code> if the given string is "weighted",
     *         <code>false</code> if not.
     */
    private static boolean isWeighted( String s )
    {
        return WEIGHTED_STRING.equalsIgnoreCase( s );
    }


    /**
     * Indicates if the given string is one of the sort methods.
     *
     * @param s the string to test
     * @return <code>true</code> if the given string is one of the sort methods,
     *         <code>false</code> if not.
     */
    private static boolean isSortMethod( String s )
    {
        return ( OlcValSortMethodEnum.fromString( s ) != null );
    }


    private static String getQuotedOrNotQuotedOptionValue( char[] chars, Position pos ) throws ParseException
    {
        if ( pos.start != pos.length )
        {
            char quoteChar = '\0';
            boolean isInQuotes = false;
            char c = Strings.charAt( chars, pos.start );
            char[] v = new char[chars.length - pos.start];
            int current = 0;

            do
            {
                if ( ( current == 0 ) && !isInQuotes )
                {
                    // Whitespace
                    if ( Character.isWhitespace( c ) )
                    {
                        // We ignore all whitespaces until we find the start of the value
                        pos.start++;
                        continue;
                    }
                    // Double quotes (") or single quotes (')
                    else if ( ( c == '"' ) || ( c == '\'' ) )
                    {
                        isInQuotes = true;
                        quoteChar = c;
                        pos.start++;
                        continue;
                    }
                    // Any other char is part of a value
                    else
                    {
                        v[current++] = c;
                        pos.start++;
                    }
                }
                else
                {
                    if ( isInQuotes )
                    {
                        // Double quotes (") or single quotes (')
                        if ( quoteChar == c )
                        {
                            isInQuotes = false;
                            pos.start++;
                            continue;
                        }
                        // Checking for escaped quotes
                        else if ( c == '\\' )
                        {
                            // Double quotes (")
                            if ( ( quoteChar == '"' ) && ( Strings.areEquals( chars, pos.start, "\\\"" ) >= 0 ) )
                            {
                                v[current++] = '"';
                                pos.start += 2;
                                continue;
                            }
                            // Single quotes (')
                            else if ( ( quoteChar == '\'' ) && ( Strings.areEquals( chars, pos.start, "\\'" ) >= 0 ) )
                            {
                                v[current++] = '\'';
                                pos.start += 2;
                                continue;
                            }
                        }
                        // Any other char is part of a value
                        else
                        {
                            v[current++] = c;
                            pos.start++;
                        }
                    }
                    else
                    {
                        // Whitespace
                        if ( Character.isWhitespace( c ) )
                        {
                            // Once we have found the start of the value, the first whitespace is the exit
                            break;
                        }
                        // Any other char is part of a value
                        else
                        {
                            v[current++] = c;
                            pos.start++;
                        }
                    }
                }
            }
            while ( ( c = Strings.charAt( chars, pos.start ) ) != '\0' );

            // Checking the resulting value
            if ( current == 0 )
            {
                throw new ParseException( "Couldn't find a value.", pos.start );
            }

            char[] value = new char[current];
            System.arraycopy( v, 0, value, 0, current );

            // Getting the value as a String
            return new String( value );
        }

        return null;
    }
}
