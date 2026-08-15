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

package org.apache.directory.studio.ldapbrowser.core.model.filter.parser;


// ── CLASS: LdapFilterToken — C-3PO'S INDIVIDUAL JAWA WORD CARD ───────────────
// When C-3PO scans a Jawa phrase word by word, each word gets its own small card:
// "This word is an AND operator."  "This word is an attribute name starting at
// character 3."  "This word is unknown — flag it."  Each card has three fields:
// the type code (what kind of word), the raw text, and the start position in
// the original phrase.  C-3PO uses these cards to sort and reassemble the parse
// tree in the correct order.
// LdapFilterToken is that word card: an immutable value holder for a single
// lexical token produced by {@link LdapFilterScanner}.  The type constants
// identify every kind of token the scanner can produce.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single lexical token produced by {@link LdapFilterScanner}.
 * Each token carries its type code (one of the {@code static final int} constants
 * defined here), the raw text value it was scanned from, and its byte offset
 * in the original filter string.  Tokens are {@link Comparable} by offset so
 * they can be sorted back into source order.
 *
 * <p>Think of this as C-3PO's individual Jawa word card — type, text, and
 * position on one small record that the parser slots into the AST.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapFilterToken implements Comparable<LdapFilterToken>
{

    /** The token identifier for a new filter */
    public static final int NEW = Integer.MIN_VALUE;

    /** The token identifier for an error token */
    public static final int ERROR = -2;

    /** The token identifier for end of file */
    public static final int EOF = -1;

    /** The token identifier for an unknown token */
    public static final int UNKNOWN = 0;

    /** The token identifier for a whitespace */
    public static final int WHITESPACE = 1;

    /** The token identifier for the left parenthesis ( */
    public static final int LPAR = 11;

    /** The token identifier for the right parenthesis ) */
    public static final int RPAR = 12;

    /** The token identifier for the and operator & */
    public static final int AND = 21;

    /** The token identifier for the or operator | */
    public static final int OR = 22;

    /** The token identifier for the not operator ! */
    public static final int NOT = 23;

    /** The token identifier for the attribute = */
    public static final int ATTRIBUTE = 31;

    /** The token identifier for the equal filter type = */
    public static final int EQUAL = 41;

    /** The token identifier for the approx filter type ~= */
    public static final int APROX = 42;

    /** The token identifier for the greater or equal filter type >= */
    public static final int GREATER = 43;

    /** The token identifier for the less or equal filter type <= */
    public static final int LESS = 44;

    /** The token identifier for the present filter type =* */
    public static final int PRESENT = 45;

    /** The token identifier for the substring filter type =* */
    public static final int SUBSTRING = 46;

    /** The token identifier for a value. */
    public static final int VALUE = 51;

    /** The token identifier for the asterisk. */
    public static final int ASTERISK = 52;

    /** The token identifier for the attribute type in extensible filters. */
    public static final int EXTENSIBLE_ATTRIBUTE = 61;

    /** The token identifier for the colon before the Dn flag in extensible filters. */
    public static final int EXTENSIBLE_DNATTR_COLON = 62;

    /** The token identifier for the Dn flag in extensible filters. */
    public static final int EXTENSIBLE_DNATTR = 63;

    /** The token identifier for the colon before the matching rule OID in extensible filters. */
    public static final int EXTENSIBLE_MATCHINGRULEOID_COLON = 64;

    /** The token identifier for the matching rule OID in extensible filters. */
    public static final int EXTENSIBLE_MATCHINGRULEOID = 65;

    /** The token identifier for the colon before the equals in extensible filters. */
    public static final int EXTENSIBLE_EQUALS_COLON = 66;

    /** The offset. */
    private int offset;

    /** The type. */
    private int type;

    /** The value. */
    private String value;


    // ── C-3PO Fills Out A Word Card ───────────────────────────────────────────────
    // "Type: ATTRIBUTE (31).  Text: 'cn'.  Position: 1."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of LdapFilterToken.
     *
     * @param type   the token type (one of the constants defined in this class).
     * @param value  the raw text of this token.
     * @param offset the zero-based start position in the original filter string.
     */
    public LdapFilterToken( int type, String value, int offset )
    {
        this.type = type;
        this.value = value;
        this.offset = offset;
    }


    // ── C-3PO Reads The Position Field ───────────────────────────────────────────
    /**
     * Returns the start position of the token in the original filter
     *
     * @return the start position of the token
     */
    public int getOffset()
    {
        return offset;
    }


    // ── C-3PO Measures The Word Length ───────────────────────────────────────────
    /**
     * Returns the length of the token in the original filter
     *
     * @return the length of the token
     */
    public int getLength()
    {
        return value.length();
    }


    // ── C-3PO Reads The Type Code ─────────────────────────────────────────────────
    /**
     * Gets the token type.
     *
     * @return the token type
     */
    public int getType()
    {
        return type;
    }


    // ── C-3PO Reads The Raw Word Text ─────────────────────────────────────────────
    /**
     * Gets the value of the token in the original filter.
     *
     * @return the value of the token
     */
    public String getValue()
    {
        return value;
    }


    // ── C-3PO Prints The Token's Full Card Details ───────────────────────────────
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "(" + offset + ") " + "(" + type + ") " + value; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }


    // ── C-3PO Compares Two Cards By Position ──────────────────────────────────────
    // "Which word came first in the original phrase?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo( LdapFilterToken o )
    {
        if ( o instanceof LdapFilterToken )
        {
            LdapFilterToken token = ( LdapFilterToken ) o;
            return this.offset - token.offset;
        }
        else
        {
            throw new ClassCastException( "Not instanceof LapFilterToken: " + o.getClass().getName() ); //$NON-NLS-1$
        }
    }

}
