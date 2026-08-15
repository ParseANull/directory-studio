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

package org.apache.directory.studio.ldifeditor.editor.text;


import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


// ── CLASS: LdifRecordRule — REBEL PERIMETER SENTINEL MARKS EACH ZONE ─────────
// The Rebel base's perimeter sentinel walks the corridor and raises a flag the
// moment it detects the start of a new transmission record — reading character
// by character, looking for "dn:" at column zero after a blank separator line.
// LdifRecordRule is that sentinel: it implements Eclipse's IPredicateRule
// contract, consuming characters until it identifies the end boundary of one
// LDIF record, then returning the record token so the partition scanner can
// mark the zone.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IPredicateRule} used by {@link LdifPartitionScanner} to
 * detect the boundaries of individual LDIF records.
 * A record starts at column 0 with {@code dn:} and ends either at a blank
 * line followed by another {@code dn:}, a blank line followed by EOF, or
 * immediately at EOF.  All three boundary types cause the rule to return the
 * configured {@link IToken} (i.e. the {@code __ldif_record} token).
 * Think of this as the Rebel sentinel that marks the start and end of each
 * transmitted data record.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifRecordRule implements IPredicateRule
{

    /**
     * The three-character sequence that must appear at the start of every
     * LDIF record: {@code d}, {@code n}, {@code :}.
     */
    private static char[] DN_SEQUENCE = new char[]
        { 'd', 'n', ':' };

    /** The token to return when a complete record boundary is found. */
    private IToken recordToken;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new record rule that returns {@code recordToken} on a match.
     *
     * @param recordToken  the token to emit when an LDIF record is detected
     */
    public LdifRecordRule( IToken recordToken )
    {
        this.recordToken = recordToken;
    }


    // ── RETURN THE SUCCESS TOKEN ──────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * @return the record token passed to the constructor
     */
    public IToken getSuccessToken()
    {
        return this.recordToken;
    }


    // ── MATCH A NEWLINE SEQUENCE ──────────────────────────────────────────────
    // Handle all three line-ending styles: \n, \r, and \r\n (or \n\r).
    /**
     * Attempts to consume a newline ({@code \n}, {@code \r}, or {@code \r\n} /
     * {@code \n\r}) from {@code scanner}.
     *
     * @param scanner  the character scanner positioned just before the newline
     * @return the number of characters consumed (0, 1, or 2)
     */
    private int matchNewline( ICharacterScanner scanner )
    {

        int c = scanner.read();

        if ( c == '\r' )
        {
            c = scanner.read();
            if ( c == '\n' )
            {
                return 2;
            }
            else
            {
                scanner.unread();
                return 1;
            }
        }
        else if ( c == '\n' )
        {
            c = scanner.read();
            if ( c == '\r' )
            {
                return 2;
            }
            else
            {
                scanner.unread();
                return 1;
            }
        }
        else
        {
            scanner.unread();
            return 0;
        }
    }


    // ── MATCH THE "dn:" PREFIX ────────────────────────────────────────────────
    // Consume exactly 'd', 'n', ':'; unread everything and return 0 on mismatch.
    /**
     * Attempts to consume the three-character sequence {@code "dn:"} from
     * {@code scanner}.  Unreads all consumed characters on mismatch.
     *
     * @param scanner  the character scanner
     * @return {@code 3} on success, {@code 0} on mismatch
     */
    private int matchDnAndColon( ICharacterScanner scanner )
    {

        for ( int i = 0; i < DN_SEQUENCE.length; i++ )
        {

            int c = scanner.read();

            if ( c != DN_SEQUENCE[i] )
            {
                while ( i >= 0 )
                {
                    scanner.unread();
                    i--;
                }
                return 0;
            }

        }

        return DN_SEQUENCE.length;
    }


    // ── MATCH EOF ─────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} and leaves the scanner past EOF if the next
     * character is {@link ICharacterScanner#EOF}; otherwise unreads it and
     * returns {@code false}.
     *
     * @param scanner  the character scanner
     * @return {@code true} if at end of input
     */
    private boolean matchEOF( ICharacterScanner scanner )
    {
        int c = scanner.read();
        if ( c == ICharacterScanner.EOF )
        {
            return true;
        }
        else
        {
            scanner.unread();
            return false;
        }
    }


    // ── EVALUATE (FULL RECORD BOUNDARY SCAN) ─────────────────────────────────
    // Walk forward consuming characters; at each newline check whether we
    // have reached a record boundary (blank line + dn: or blank line + EOF
    // or plain EOF).  Return the record token on boundary, UNDEFINED otherwise.
    /**
     * {@inheritDoc}
     *
     * <p>The rule only fires when {@code scanner.getColumn() == 0}.  It then
     * reads forward character by character.  When a newline is encountered,
     * it attempts the boundary check: two newlines followed by {@code dn:},
     * two newlines followed by EOF, one newline followed by EOF, or bare
     * EOF — any of these constitutes the end of one LDIF record and causes
     * the rule to return {@link #recordToken}.  On a boundary miss, the
     * scanner is left just past the newlines so reading continues.</p>
     */
    public IToken evaluate( ICharacterScanner scanner, boolean resume )
    {

        if ( scanner.getColumn() != 0 )
        {
            return Token.UNDEFINED;
        }

        int c;

        do
        {
            c = scanner.read();

            if ( c == '\r' || c == '\n' )
            {

                // check end of record
                scanner.unread();

                if ( this.matchNewline( scanner ) > 0 )
                {

                    int nlCount = this.matchNewline( scanner );
                    if ( nlCount > 0 )
                    {
                        int dnCount = this.matchDnAndColon( scanner );
                        if ( dnCount > 0 )
                        {
                            while ( dnCount > 0 )
                            {
                                scanner.unread();
                                dnCount--;
                            }
                            return this.recordToken;
                        }
                        else if ( this.matchEOF( scanner ) )
                        {
                            return this.recordToken;
                        }
                        else
                        {
                            while ( nlCount > 0 )
                            {
                                scanner.unread();
                                nlCount--;
                            }
                        }
                    }
                    else if ( this.matchEOF( scanner ) )
                    {
                        return this.recordToken;
                    }
                }
                else if ( this.matchEOF( scanner ) )
                {
                    return this.recordToken;
                }

            }
            else if ( c == ICharacterScanner.EOF )
            {
                return this.recordToken;
            }
        }
        while ( true );

    }


    // ── EVALUATE (NO-RESUME VARIANT) ──────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #evaluate(ICharacterScanner, boolean)} with
     * {@code resume = false}.</p>
     */
    public IToken evaluate( ICharacterScanner scanner )
    {
        return this.evaluate( scanner, false );
    }

}
