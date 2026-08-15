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

package org.apache.directory.studio.ldifparser.parser;


// ── CLASS: LdifToken — IMPERIAL COMMS SIGNAL TOKEN ───────────────────────────
// When C-3PO intercepts an Imperial comms burst he breaks it into labelled
// signal units: "that's a separator," "that's a changetype keyword," "that's
// an OID."  Each unit has a type code, a value string, and a position in the
// original stream.
// LdifToken is that labelled signal unit.  The scanner produces one token per
// matched lexical element; the parser consumes them to build structured model
// objects.  Token type constants cover every element of the RFC 2849 grammar.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An atomic lexical unit produced by {@link LdifScanner} and consumed by
 * {@link LdifParser}.
 * Each token carries a numeric {@code type} constant from this class (e.g.
 * {@link #DN_SPEC}, {@link #ATTRIBUTE}, {@link #VALUE}), the raw matched
 * {@code value} string, and the byte {@code offset} of the first character
 * in the source text.
 * Think of a token as the labelled signal unit C-3PO isolates when
 * decoding an incoming LDIF transmission — one small named chunk of the
 * grammar at a precise location in the stream.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifToken implements Comparable<LdifToken>
{
    // ── TOKEN TYPE CONSTANTS ──────────────────────────────────────────────────

    /** Sentinel type used before a token has been assigned a real type. */
    public static final int NEW = Integer.MIN_VALUE;

    /** Returned when the scanner encounters an unrecoverable input error. */
    public static final int ERROR = -2;

    /** Marks the end of the input stream. */
    public static final int EOF = -1;

    /** Catch-all for unrecognised content consumed during error recovery. */
    public static final int UNKNOWN = 0;

    /** A comment line starting with {@code #}. */
    public static final int COMMENT = 1;

    /** A line separator ({@code LF} or {@code CRLF}). */
    public static final int SEP = 2;

    /** The {@code "version"} keyword at the start of a version-spec line. */
    public static final int VERSION_SPEC = 4;

    /** A decimal number (e.g. the version number or a delete-old-rdn flag). */
    public static final int NUMBER = 5;

    /** An LDAP OID (e.g. {@code 2.16.840.1.113730.3.4.2}). */
    public static final int OID = 6;

    /** The {@code "dn"} keyword. */
    public static final int DN_SPEC = 11;

    /** The distinguished name value. */
    public static final int DN = 12;

    /** An attribute description (type plus optional options). */
    public static final int ATTRIBUTE = 21;

    /** A safe-value separator ({@code :}). */
    public static final int VALUE_TYPE_SAFE = 22;

    /** A Base64-value separator ({@code ::}). */
    public static final int VALUE_TYPE_BASE64 = 23;

    /** A URL-value separator ({@code :<}). */
    public static final int VALUE_TYPE_URL = 24;

    /** The payload value that follows a value-type separator. */
    public static final int VALUE = 27;

    /** The {@code "changetype"} keyword. */
    public static final int CHANGETYPE_SPEC = 30;

    /** The {@code "add"} changetype value. */
    public static final int CHANGETYPE_ADD = 31;

    /** The {@code "delete"} changetype value. */
    public static final int CHANGETYPE_DELETE = 32;

    /** The {@code "modify"} changetype value. */
    public static final int CHANGETYPE_MODIFY = 33;

    /** The {@code "moddn"} or {@code "modrdn"} changetype value. */
    public static final int CHANGETYPE_MODDN = 34;

    /** The {@code "add"} mod-type keyword inside a modify record. */
    public static final int MODTYPE_ADD_SPEC = 41;

    /** The {@code "delete"} mod-type keyword inside a modify record. */
    public static final int MODTYPE_DELETE_SPEC = 42;

    /** The {@code "replace"} mod-type keyword inside a modify record. */
    public static final int MODTYPE_REPLACE_SPEC = 43;

    /** The {@code "-"} separator that closes one mod-spec. */
    public static final int MODTYPE_SEP = 45;

    /** The {@code "control"} keyword on a control line. */
    public static final int CONTROL_SPEC = 51;

    /** The OID on a control line (type alias for clarity). */
    public static final int CONTROL_LDAPOID = 52;

    /** A criticality token with value {@code "true"}. */
    public static final int CONTROL_CRITICALITY_TRUE = 53;

    /** A criticality token with value {@code "false"}. */
    public static final int CONTROL_CRITICALITY_FALSE = 54;

    /** The {@code "newrdn"} keyword on a moddn line. */
    public static final int MODDN_NEWRDN_SPEC = 61;

    /** The {@code "deleteoldrdn"} keyword on a moddn line. */
    public static final int MODDN_DELOLDRDN_SPEC = 63;

    /** The {@code "newsuperior"} keyword on a moddn line. */
    public static final int MODDN_NEWSUPERIOR_SPEC = 65;


    // ── FIELDS ────────────────────────────────────────────────────────────────

    /** Byte offset of this token's first character in the source text. */
    private int offset;

    /** Numeric type constant (one of the {@code public static final int} fields). */
    private int type;

    /** The matched raw string from the source text. */
    private String value;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────
    // ── Signal Unit Constructor ────────────────────────────────────────────────
    // R2-D2 assembles a burst packet before handing it to C-3PO for processing.
    // Each packet gets a type label, the raw bit sequence, and the address it
    // came from.
    // We build a token the same way.
    /**
     * Creates a token with the given type, raw value, and source offset.
     *
     * @param type    one of the type constants defined in this class
     * @param value   the matched raw string
     * @param offset  byte offset of the token's first character
     */
    public LdifToken( int type, String value, int offset )
    {
        this.type = type;
        this.value = value;
        this.offset = offset;
    }


    // ── ACCESSORS ─────────────────────────────────────────────────────────────
    // ── Token Start Position ──────────────────────────────────────────────────
    // Rebel cartographers note the starting coordinates of each transmission
    // fragment so they can reassemble them in order.
    // We return the start offset so the parser can assemble model objects with
    // correct document positions.
    /**
     * Returns the start position of this token in the original LDIF source.
     *
     * @return byte offset of the first character
     */
    public int getOffset()
    {
        return offset;
    }


    // ── Token Length ──────────────────────────────────────────────────────────
    // The fragment length tells the cartographer how many bytes this signal
    // burst occupies in the original stream.
    // We derive length from the raw value string.
    /**
     * Returns the length of this token in the original LDIF source.
     *
     * @return number of characters in {@link #getValue()}
     */
    public int getLength()
    {
        return value.length();
    }


    // ── Token Type ────────────────────────────────────────────────────────────
    // Every fragment carries a classification code so the parser knows what
    // grammar role it plays.
    // We return the numeric type constant.
    /**
     * Returns the numeric type constant identifying what kind of token this is.
     *
     * @return one of the {@code public static final int} type constants
     */
    public int getType()
    {
        return type;
    }


    // ── Token Value ───────────────────────────────────────────────────────────
    // The fragment's raw content — unchanged from the source stream — is what
    // we need to build model objects without losing any original characters.
    // We return the value string as-is.
    /**
     * Returns the raw matched string for this token.
     *
     * @return the raw matched content
     */
    public String getValue()
    {
        return value;
    }


    // ── OBJECT METHODS ────────────────────────────────────────────────────────
    // ── Debug-Friendly Token Description ─────────────────────────────────────
    // When an operative radios back "token at position 47 of type 22 reads ':'"
    // the message is self-explanatory.
    // We produce the same format: type, offset, length, and the value
    // in quotes.
    /**
     * Returns a debug-friendly description of this token showing type, offset,
     * length, and raw value.
     *
     * @return a descriptive string
     */
    public String toString()
    {
        return "(type=" + type + ") " + "(offset=" + offset + ") " + "(length=" + getLength() + ") '" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            + value + "'"; //$NON-NLS-1$
    }


    // ── Offset-Based Ordering ─────────────────────────────────────────────────
    // Rebel archivists sort transmission fragments by their position in the
    // original stream so they can reconstruct the message in document order.
    // We compare tokens by offset alone so sorted collections reconstruct the
    // original document order.
    /**
     * Compares this token to another by source offset, enabling tokens to be
     * sorted into document order.
     *
     * @param ldifToken  the other token
     * @return negative if this token comes earlier, zero if same offset,
     *         positive if this token comes later
     */
    public int compareTo( LdifToken ldifToken )
    {
        return offset - ldifToken.offset;
    }
}
