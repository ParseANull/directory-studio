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

package org.apache.directory.studio.openldap.config.acl.sourceeditor;


import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;


// ── CLASS: AbstractRule — CASSIAN ANDOR READING STOLEN IMPERIAL CODE ─────────
// Cassian Andor stands over a stolen Imperial terminal, reading the raw byte
// stream character by character. He has a toolkit of low-level moves: read a
// char, unread it if it doesn't match, check for EOF, check for a digit. Each
// concrete rule (DnRule, GroupRule, StarRule, …) is a specific pattern Cassian
// is looking for in the ACL source stream. This abstract base provides the
// shared reading primitives — the toolkit — so the concrete rules only need to
// implement the pattern-specific logic.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base for JFace text scanner predicate rules used in the OpenLDAP ACL
 * source editor. Provides the low-level character-matching helpers that concrete
 * rules call to implement their {@code evaluate()} methods.
 *
 * <p>Each helper reads from an {@link ICharacterScanner}, returning {@code true}
 * if the expected character (or sequence) was consumed, or {@code false} if the
 * scanner was left unchanged (unread was called to restore position).</p>
 *
 * <p>Think of this class as Cassian's decryption toolkit — the concrete rules
 * use these helpers to identify patterns like {@code dn=}, {@code group=},
 * {@code *}, and ACL keywords in the source text.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractRule implements IPredicateRule
{
    /** The token */
    protected IToken token;


    // ── Constructing a Rule With Its Success Token ────────────────────────────
    // Cassian notes which file label to apply when this pattern is found — the
    // token is returned by evaluate() if the match succeeds.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new rule that returns the given token on a successful match.
     * Concrete subclasses call this from their own constructors.
     *
     * @param token  The {@link IToken} to return when a match succeeds; must not be {@code null}.
     */
    public AbstractRule( IToken token )
    {
        this.token = token;
    }


    // ── Returning the Success Token ────────────────────────────────────────────
    // JFace's RuleBasedScanner calls this to find out what token a successful
    // match should produce — the value set in the constructor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the token produced by this rule when a successful match is found.
     *
     * @return  The success {@link IToken} given to the constructor.
     */
    public IToken getSuccessToken()
    {
        return token;
    }


    // ── Matching an Exact Character Sequence ─────────────────────────────────
    // Cassian checks whether the next N bytes in the stream are exactly the
    // sequence he expects. If any byte mismatches he unreads everything and
    // reports failure — no partial matches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attempts to consume an exact character sequence from the scanner. If any
     * character does not match, all consumed characters are pushed back and
     * {@code false} is returned.
     *
     * <p>For example — Cassian checking for the literal "dn":</p>
     * <pre>
     *   matchCharSequence(scanner, "dn".toCharArray()); // true if "dn" is next
     * </pre>
     *
     * @param scanner   The character scanner to read from.
     * @param sequence  The expected character sequence.
     * @return          {@code true} if the full sequence was consumed; {@code false} if not.
     */
    boolean matchCharSequence( ICharacterScanner scanner, char[] sequence )
    {
        for ( int i = 0; i < sequence.length; i++ )
        {
            int c = scanner.read();

            if ( c != sequence[i] )
            {
                while ( i >= 0 )
                {
                    scanner.unread();
                    i--;
                }

                return false;
            }

        }

        return true;
    }


    // ── Checking That a Sequence Does NOT Match ────────────────────────────────
    // The inverse of matchCharSequence — Cassian checks that the next bytes are
    // NOT the given sequence. If they are he unreads and returns false.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the scanner does <em>not</em> match the given
     * character sequence (i.e. the scanner state is left unchanged and the
     * sequence was not present).
     *
     * @param scanner   The character scanner.
     * @param sequence  The sequence to check absence of.
     * @return          {@code true} if the sequence was NOT matched.
     */
    boolean doesNotMatchCharSequence( ICharacterScanner scanner, char[] sequence )
    {
        return !matchCharSequence( scanner, sequence );
    }


    // ── Matching a Single Character ───────────────────────────────────────────
    // Cassian reads one byte and checks it. If it doesn't match he puts it back
    // and moves on.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attempts to consume a single character from the scanner. If the next
     * character does not match, it is pushed back and {@code false} is returned.
     *
     * <p>For example — checking for an equals sign:</p>
     * <pre>
     *   matchChar(scanner, '='); // true if '=' is the next character
     * </pre>
     *
     * @param scanner    The character scanner.
     * @param character  The expected character.
     * @return           {@code true} if the character was consumed; {@code false} otherwise.
     */
    boolean matchChar( ICharacterScanner scanner, char character )
    {
        int c = scanner.read();

        if ( c == character )
        {
            return true;
        }
        else
        {
            scanner.unread();
            return false;
        }
    }


    // ── Matching End-of-File ───────────────────────────────────────────────────
    // Cassian checks whether the stream is exhausted. If there are more bytes he
    // puts the read back and returns false.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the next scanner read returns {@link ICharacterScanner#EOF}.
     * If not, the character is pushed back.
     *
     * @param scanner  The character scanner.
     * @return         {@code true} if the scanner is at EOF.
     */
    boolean matchEOF( ICharacterScanner scanner )
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


    // ── Checking That the Next Character Does Not Match ───────────────────────
    // The inverse of matchChar — useful in rules that must verify a lookahead
    // character is NOT a specific value before committing to a token.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the next character from the scanner does NOT match
     * the given character (the scanner is left unchanged).
     *
     * @param scanner    The character scanner.
     * @param character  The character to check absence of.
     * @return           {@code true} if the character was NOT matched.
     */
    boolean doesNotMatchChar( ICharacterScanner scanner, char character )
    {
        return !matchChar( scanner, character );
    }


    // ── Matching a Single Decimal Digit ──────────────────────────────────────
    // Cassian checks whether the next byte is a digit (0-9). If not, it is
    // pushed back and false is returned. Used when scanning numeric tokens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attempts to consume a single decimal digit ({@code '0'} through {@code '9'})
     * from the scanner. If the next character is not a digit, it is pushed back.
     *
     * <p>For example — consuming the numeric portion of an SSF value:</p>
     * <pre>
     *   matchDigit(scanner); // true for "128=" → consumes '1'
     * </pre>
     *
     * @param scanner  The character scanner.
     * @return         {@code true} if a digit was consumed; {@code false} otherwise.
     */
    boolean matchDigit( ICharacterScanner scanner )
    {
        int c = scanner.read();

        if ( ( c >= '0' ) && ( c <= '9' ) )
        {
            return true;
        }
        else
        {
            scanner.unread();
            return false;
        }
    }
}
