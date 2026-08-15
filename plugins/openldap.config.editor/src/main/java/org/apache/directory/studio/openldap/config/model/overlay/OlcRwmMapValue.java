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

import org.apache.directory.api.util.Position;
import org.apache.directory.api.util.Strings;


// ── CLASS: OlcRwmMapValue — C-3PO's Single Phrase-Book Entry ─────────────────
// C-3PO's translation phrase-book has thousands of entries. Each entry says:
// "In Gungan, the word for X is Y" — or more precisely, "attribute uid" maps to
// "login" on the remote side. When C-3PO intercepts an LDAP request, he looks
// up each attribute or objectclass in his book and applies the translation.
// OlcRwmMapValue represents a single such entry: the schema element type (ATTRIBUTE
// or OBJECTCLASS), the local name, and the foreign (remote) name. It also knows how
// to parse itself from the raw string format OpenLDAP uses in the LDAP attribute
// (e.g., "attribute uid login").
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single {@code olcRwmMap} attribute value, which defines one
 * attribute-type or object-class name mapping for the rwm (rewrite/remap) overlay.
 * The format is: {@code <type> [<localName>] <foreignName>}
 * where type is "attribute" or "objectclass", localName is optional (defaults to
 * the wildcard "*"), and foreignName is the name on the remote schema.
 * Think of this as C-3PO's single phrase-book entry mapping one schema term
 * from the local dialect to the remote one.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcRwmMapValue
{
    /** The constant string for '*' */
    private static final String STAR_STRING = "*";

    /** The type */
    private OlcRwmMapValueTypeEnum type;

    /** The local name */
    private String localName;

    /** The foreign name */
    private String foreignName;


    // ── getType — C-3PO Checks Which Phrase-Book to Use ───────────────────────────
    // C-3PO checks the type flag on this entry to know which translation table applies:
    // attribute names or objectclass names.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the type discriminator for this mapping entry — either ATTRIBUTE or OBJECTCLASS.
     * This determines which schema table the rwm overlay applies the mapping to.
     *
     * <p>For example — C-3PO checks the phrase-book type:</p>
     * <pre>
     *   OlcRwmMapValueTypeEnum t = mapValue.getType(); // ATTRIBUTE or OBJECTCLASS
     * </pre>
     *
     * @return  the OlcRwmMapValueTypeEnum for this entry
     */
    public OlcRwmMapValueTypeEnum getType()
    {
        return type;
    }


    // ── getLocalName — C-3PO Reads the Local Term ─────────────────────────────────
    // C-3PO reads the local side of the translation — the attribute or objectclass
    // name as the local LDAP schema knows it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the local schema name (the name as the local side calls it).
     * May be null or the wildcard "*" when mapping all attributes or objectclasses.
     *
     * <p>For example — C-3PO reads the local term:</p>
     * <pre>
     *   String local = mapValue.getLocalName(); // "uid" or "*"
     * </pre>
     *
     * @return  the local attribute or objectclass name, or null if not set
     */
    public String getLocalName()
    {
        return localName;
    }


    // ── getForeignName — C-3PO Reads the Remote Term ──────────────────────────────
    // C-3PO reads the remote side of the translation — what the remote directory
    // server calls this attribute or objectclass.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the foreign (remote) schema name — what the remote directory calls this element.
     *
     * <p>For example — C-3PO reads the remote term:</p>
     * <pre>
     *   String foreign = mapValue.getForeignName(); // "login" or "*"
     * </pre>
     *
     * @return  the remote attribute or objectclass name
     */
    public String getForeignName()
    {
        return foreignName;
    }


    // ── isLocalNameStart — C-3PO Checks for the Wildcard Local Term ──────────────
    // C-3PO checks whether the local name is the wildcard "*" — meaning "all attributes"
    // or "all objectclasses" on the local side get this translation treatment.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the local name is the wildcard constant "*".
     * A wildcard local name means the mapping applies to all attributes or objectclasses
     * on the local side.
     *
     * <p>For example — C-3PO checks for a wildcard:</p>
     * <pre>
     *   if ( mapValue.isLocalNameStart() ) {
     *       // applies to all local attribute/objectclass names
     *   }
     * </pre>
     *
     * @return  true if localName equals "*", false otherwise
     */
    public boolean isLocalNameStart()
    {
        return STAR_STRING.equals( localName );
    }


    // ── isLocalForeignStart — C-3PO Checks for the Wildcard Remote Term ──────────
    // C-3PO checks whether the foreign name is the wildcard "*" — pass-through mode,
    // where local names are relayed to the remote side unchanged.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns true if the foreign name is the wildcard constant "*".
     * A wildcard foreign name means local names are passed through unchanged.
     *
     * <p>For example — C-3PO checks for a foreign wildcard:</p>
     * <pre>
     *   if ( mapValue.isLocalForeignStart() ) {
     *       // local names pass through to the remote side unchanged
     *   }
     * </pre>
     *
     * @return  true if foreignName equals "*", false otherwise
     */
    public boolean isLocalForeignStart()
    {
        return STAR_STRING.equals( foreignName );
    }


    // ── setType — C-3PO Marks Which Phrase-Book This Entry Belongs To ─────────────
    // C-3PO stamps the entry with its type so the lookup engine knows which table
    // to consult.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the type discriminator (ATTRIBUTE or OBJECTCLASS) for this mapping entry.
     *
     * <p>For example — C-3PO marks the entry type:</p>
     * <pre>
     *   mapValue.setType( OlcRwmMapValueTypeEnum.ATTRIBUTE );
     * </pre>
     *
     * @param type  the OlcRwmMapValueTypeEnum for this entry
     */
    public void setType( OlcRwmMapValueTypeEnum type )
    {
        this.type = type;
    }


    // ── setLocalName — C-3PO Records the Local Term ───────────────────────────────
    // C-3PO records the local schema name in this phrase-book entry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the local schema name for this mapping entry.
     *
     * <p>For example — C-3PO records the local term:</p>
     * <pre>
     *   mapValue.setLocalName( "uid" );
     * </pre>
     *
     * @param localName  the local attribute or objectclass name
     */
    public void setLocalName( String localName )
    {
        this.localName = localName;
    }


    // ── setForeignName — C-3PO Records the Remote Term ───────────────────────────
    // C-3PO records the remote side of the mapping — what the remote directory calls it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the foreign (remote) schema name for this mapping entry.
     *
     * <p>For example — C-3PO records the remote term:</p>
     * <pre>
     *   mapValue.setForeignName( "login" );
     * </pre>
     *
     * @param foreignName  the remote attribute or objectclass name
     */
    public void setForeignName( String foreignName )
    {
        this.foreignName = foreignName;
    }


    // ── toString — C-3PO Writes the Entry Back to LDAP Format ────────────────────
    // C-3PO formats the phrase-book entry back into the compact string form that
    // the rwm overlay stores in the olcRwmMap LDAP attribute.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string representation of this mapping entry in the format
     * expected by the olcRwmMap attribute, e.g., "attribute uid login" or
     * "objectclass inetOrgPerson person".
     *
     * <p>For example — C-3PO writes the entry back:</p>
     * <pre>
     *   mapValue.toString(); // "attribute uid login"
     * </pre>
     *
     * @return  the formatted olcRwmMap attribute value string
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // Type
        sb.append( type );

        // Local Name
        if ( ( localName != null ) && ( localName.length() > 0 ) )
        {
            sb.append( ' ' );
            sb.append( localName );
        }

        // Foreign Name
        if ( ( foreignName != null ) && ( foreignName.length() > 0 ) )
        {
            sb.append( ' ' );
            sb.append( foreignName );
        }

        return sb.toString();
    }


    // ── parse — C-3PO Reads a Raw Phrase-Book Entry from the LDAP Attribute ──────
    // C-3PO reads a raw olcRwmMap string from the LDAP attribute and parses it into
    // a structured OlcRwmMapValue object. He handles quoted and unquoted tokens,
    // optional local names, and validates that the type keyword is recognized.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a raw {@code olcRwmMap} attribute value string into an OlcRwmMapValue object.
     * The format is: {@code <type> [<localName>] <foreignName>}
     * This method is synchronized because it uses a shared Position object.
     *
     * <p>For example — C-3PO parses a raw attribute value:</p>
     * <pre>
     *   OlcRwmMapValue mapValue = OlcRwmMapValue.parse( "attribute uid login" );
     *   mapValue.getType();        // ATTRIBUTE
     *   mapValue.getLocalName();   // "uid"
     *   mapValue.getForeignName(); // "login"
     * </pre>
     *
     * @param s  the raw olcRwmMap value string to parse
     * @return   the parsed OlcRwmMapValue, or null if the string is null
     * @throws ParseException  if the string doesn't match the expected format
     */
    public static synchronized OlcRwmMapValue parse( String s ) throws ParseException
    {
        if ( s == null )
        {
            return null;
        }

        // Trimming the value
        s = Strings.trim( s );

        // Getting the chars of the string
        char[] chars = new char[s.length()];
        s.getChars( 0, s.length(), chars, 0 );

        // Creating the position
        Position pos = new Position();
        pos.start = 0;
        pos.end = 0;
        pos.length = chars.length;

        return parseInternal( chars, pos );
    }


    // ── parseInternal — C-3PO's Internal Token Parser ────────────────────────────
    // C-3PO's internal parsing loop reads tokens one at a time: first the type keyword,
    // then one or two names. He handles optional local names and validates types.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Internal parse implementation. Reads tokens (type, optional localName, foreignName)
     * from the character array using a Position cursor.
     *
     * @param chars  the character array to parse
     * @param pos    the current position in the array (mutated in place)
     * @return       the parsed OlcRwmMapValue, or null if input is empty
     * @throws ParseException  if any token is missing or unrecognized
     */
    private static OlcRwmMapValue parseInternal( char[] chars, Position pos ) throws ParseException
    {
        OlcRwmMapValue value = new OlcRwmMapValue();

        // Empty (trimmed) string?
        if ( chars.length == 0 )
        {
            return null;
        }

        do
        {
            // Type
            String typeString = getQuotedOrNotQuotedOptionValue( chars, pos );

            if ( ( typeString != null ) && ( !typeString.isEmpty() ) )
            {
                OlcRwmMapValueTypeEnum type = OlcRwmMapValueTypeEnum.fromString( typeString );

                if ( type != null )
                {
                    value.setType( type );
                }
                else
                {
                    throw new ParseException( "Could not identify keyword '" + typeString
                        + "' as a valid type.", pos.start );
                }
            }
            else
            {
                throw new ParseException( "Could not find the 'type' value", pos.start );
            }

            // First Name
            String firstName = getQuotedOrNotQuotedOptionValue( chars, pos );

            if ( ( firstName == null ) || ( firstName.isEmpty() ) )
            {
                throw new ParseException( "Could not find any 'localName' or 'foreignName' value", pos.start );
            }

            // Second Name
            String secondName = getQuotedOrNotQuotedOptionValue( chars, pos );

            if ( ( secondName == null ) || ( secondName.isEmpty() ) )
            {
                // Local Name is optional
                // If we got only one name, it's the foreign name
                value.setForeignName( firstName );
            }
            else
            {
                value.setLocalName( firstName );
                value.setForeignName( secondName );
            }
        }
        while ( ( pos.start != pos.length ) && ( ( Strings.charAt( chars, pos.start ) ) != '\0' ) );

        return value;
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
