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
package org.apache.directory.studio.schemaeditor.model.alias;


// ── CLASS: AliasesStringToken — C-3PO Labels Each Segment Of The Message ──────
// When C-3PO deciphers a Jawa broadcast, he breaks it into discrete labelled
// segments: "this part is a valid word", "this part is a comma separator",
// "this part I don't recognise — it starts with an illegal glyph." Each
// segment has a type code, a position offset in the original broadcast, and
// the raw text of that segment. That's this class.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single lexical token produced by {@link AliasesStringScanner}
 * while scanning the aliases string. Carries three things: the token type
 * (what kind of element this is), the raw value (the substring), and the
 * offset (where in the original string it started). The parser uses these to
 * build the final list of {@link Alias} objects.
 * Think of this as C-3PO's labelled message segment: type says what it is,
 * value says what it says, offset says where in the broadcast it appeared.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AliasesStringToken implements Comparable<AliasesStringToken>
{
    /** The token identifier for the start */
    public static final int START = Integer.MIN_VALUE;

    /** The token identifier for end of file */
    public static final int EOF = -1;

    /** The token identifier for a whitespace */
    public static final int WHITESPACE = 0;

    /** The token identifier for a comma ',' */
    public static final int COMMA = 1;

    /** The token identifier for an alias */
    public static final int ALIAS = 2;

    /** The token identifier for an error at the start of an alias */
    public static final int ERROR_ALIAS_START = 3;

    /** The token identifier for an error in a part (not the first character) of an alias */
    public static final int ERROR_ALIAS_PART = 4;

    /** The token identifier for the substring following an error in an alias */
    public static final int ERROR_ALIAS_SUBSTRING = 5;

    /** The offset. */
    private int offset;

    /** The type. */
    private int type;

    /** The value. */
    private String value;


    // ── C-3PO Stamps A Message Segment With Its Label ────────────────────────────
    // C-3PO annotates the decoded segment with its type code, position in the
    // broadcast, and the raw text. These three together are everything the parser
    // needs to construct the alias list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new token with the given type, value, and position. Called only
     * by {@link AliasesStringScanner#nextToken()} — outside code should never
     * construct tokens directly; consume them from the scanner instead.
     *
     * @param type    one of the type constants on this class (ALIAS, COMMA, EOF, etc.)
     * @param value   the raw substring this token covers
     * @param offset  the zero-based start position of this token in the original string
     */
    public AliasesStringToken( int type, String value, int offset )
    {
        this.type = type;
        this.value = value;
        this.offset = offset;
    }


    // ── C-3PO Reports The Segment's Position In The Broadcast ───────────────────
    // "That word started at position 7 in the transmission." The UI uses the
    // offset to position error markers at exactly the right character.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index in the original aliases string where this token
     * begins. The UI uses this to place error underlines in exactly the right spot.
     *
     * @return  the start position of this token in the input string
     */
    public int getOffset()
    {
        return offset;
    }


    // ── C-3PO Reports How Long The Segment Is ────────────────────────────────────
    // "That word is four characters long." Together with the offset, the length
    // lets the UI paint an underline covering exactly this token's span.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the character length of this token's value. Together with
     * {@link #getOffset()}, this lets the UI highlight the exact character range
     * that this token covers.
     *
     * @return  the number of characters in this token
     */
    public int getLength()
    {
        return value.length();
    }


    // ── C-3PO Reads The Segment's Type Label ─────────────────────────────────────
    // "This segment is classified as ALIAS." The parser switches on this to decide
    // which kind of Alias object to construct.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the type of this token — one of the int constants defined on this
     * class (ALIAS, COMMA, WHITESPACE, ERROR_ALIAS_START, etc.). The parser uses
     * this in a switch statement to decide what to do with the token.
     *
     * @return  the token type constant
     */
    public int getType()
    {
        return type;
    }


    // ── C-3PO Reads Back The Segment's Raw Text ──────────────────────────────────
    // "The text of that segment was 'cn'." The parser reads the value to construct
    // the alias string — or to append error substrings when building error aliases.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw text substring that this token covers. For an ALIAS token
     * this is the alias name itself; for an ERROR token it's the text including
     * the bad character; for COMMA it's just ","; for WHITESPACE it's the spaces.
     *
     * @return  the token's substring value; never null
     */
    public String getValue()
    {
        return value;
    }


    // ── C-3PO Announces The Full Token Details ───────────────────────────────────
    // C-3PO reads out the full token annotation: position, type code, and value.
    // Handy for debugging the scanner/parser pipeline.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a debug-friendly string in the format {@code (offset) (type) value}.
     * Not intended for user-visible display — use {@link #getValue()} for that.
     *
     * @return  a debug string showing offset, type, and value
     */
    public String toString()
    {
        return "(" + offset + ") " + "(" + type + ") " + value; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }


    // ── C-3PO Orders Segments By Their Position In The Broadcast ─────────────────
    // When C-3PO sorts the decoded segments, he puts them in order of where they
    // appeared in the original transmission. Ordering by offset gives us
    // left-to-right sequence.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Compares this token to another by their offsets, so a sorted collection of
     * tokens comes out in left-to-right order within the original input string.
     * Throws {@link ClassCastException} if {@code o} is not an AliasesStringToken.
     *
     * @param o  the token to compare against
     * @return   negative if this token appears earlier, positive if later, zero if same position
     */
    public int compareTo( AliasesStringToken o )
    {
        if ( o instanceof AliasesStringToken )
        {
            AliasesStringToken token = ( AliasesStringToken ) o;
            return this.offset - token.offset;
        }
        else
        {
            throw new ClassCastException( "Not instanceof AliasesToken: " + o.getClass().getName() ); //$NON-NLS-1$
        }
    }

}
