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
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


// ── CLASS: GroupRule — CASSIAN IDENTIFYING THE "group" LOCATOR IN IMPERIAL CODE
// In a stolen Imperial access control file, group-based who-clauses look like
// "group=", "group/oc=", "group/oc/attr=", "group.exact=", or combinations of
// those forms. Cassian reads the stream character by character, trying each
// valid prefix form in order and unreading on every mismatch. When he finds a
// complete match — "group" followed by optional "/oc[/attr]" and optional
// ".type", all terminated by "=" — he returns the token so the syntax
// highlighter can colour it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace text predicate rule that recognises the {@code group} prefix of an
 * OpenLDAP ACL group who-clause in the source editor. Handles all forms:
 * <pre>
 *   group=
 *   group/OBJECTCLASS=
 *   group/OBJECTCLASS/ATTR=
 *   group/OBJECTCLASS.TYPE=
 *   group/OBJECTCLASS/ATTR.TYPE=
 *   group.TYPE=
 * </pre>
 * where TYPE is one of {@code expand} or {@code exact}.
 * Returns the configured token on success, or {@link Token#UNDEFINED} otherwise.
 * Think of this rule as Cassian recognising the group-based access directive
 * pattern in the stolen Imperial ACL file.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GroupRule extends AbstractRule
{

    /**
     * The "dn" char sequence.
     */
    private static char[] GROUP_SEQUENCE = new char[]
        { 'g', 'r', 'o', 'u', 'p' };

    /**
     * The array of the types char sequences
     */
    private static char[][] TYPES_SEQUENCES = new char[][]
        {
            new char[]
                { 'e', 'x', 'p', 'a', 'n', 'd' },
            new char[]
                { 'e', 'x', 'a', 'c', 't' }
    };


    // ── Constructing the Rule With Its Token ──────────────────────────────────
    // Cassian picks up the label to attach to confirmed group prefix matches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new GroupRule that returns the given token on a successful match.
     *
     * @param token  The token to return when the group prefix pattern is recognised.
     */
    public GroupRule( IToken token )
    {
        super( token );
    }


    // ── Evaluating the Scanner Position for the Group Pattern ─────────────────
    // Cassian reads ahead character by character, trying the shorter forms first
    // (group=) and falling back to longer ones (group/oc/attr.type=). Each
    // mismatch causes an unread back to where the attempt started.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attempts to match a group clause prefix starting at the current scanner
     * position. Tries each variant in order and returns the success token on the
     * first complete match. Returns {@link Token#UNDEFINED} if no variant matches.
     *
     * <p>For example — Cassian recognising "group/groupOfNames=" in the source stream:</p>
     * <pre>
     *   // scanner positioned at 'g' of "group/groupOfNames=\"cn=...\""
     *   evaluate(scanner, false); // → token
     * </pre>
     *
     * @param scanner  The character scanner positioned at the potential match start.
     * @param resume   Whether evaluation is being resumed (not used).
     * @return         The success token if a group prefix was found; {@link Token#UNDEFINED} otherwise.
     */
    public IToken evaluate( ICharacterScanner scanner, boolean resume )
    {
        // Looking for "group"
        if ( matchGroup( scanner ) )
        {

            // Looking for '='
            if ( matchEqual( scanner ) )
            {
                // Token evaluation complete
                return token;
            }
            // Looking for '/'
            else if ( matchSlash( scanner ) )
            {
                // Going forward until we find a '=', '/' or '.' char
                boolean atLeastFoundOneChar = false;
                while ( doesNotMatchEqualSlashOrDot( scanner ) )
                {
                    atLeastFoundOneChar = true;
                }

                // Checking if we found at least one char
                if ( atLeastFoundOneChar )
                {
                    // Looking for '='
                    if ( matchEqual( scanner ) )
                    {
                        // Token evaluation complete
                        return token;
                    }
                    // Looking for '/'
                    else if ( matchSlash( scanner ) )
                    {
                        // Going forward until we find a '=' or '.' char
                        boolean atLeastFoundOneChar2 = false;
                        while ( doesNotMatchEqualOrDot( scanner ) )
                        {
                            atLeastFoundOneChar2 = true;
                        }

                        // Checking if we found at least one char
                        if ( atLeastFoundOneChar2 )
                        {
                            // Looking for '='
                            if ( matchEqual( scanner ) )
                            {
                                // Token evaluation complete
                                return token;
                            }
                            // Looking for '.'
                            else if ( matchDot( scanner ) )
                            {
                                // Looking for one of the types
                                if ( matchType( scanner ) )
                                {
                                    // Looking for '='
                                    if ( matchEqual( scanner ) )
                                    {
                                        // Token evaluation complete
                                        return token;
                                    }
                                }
                            }
                        }
                    }
                    // Looking for '.'
                    else if ( matchDot( scanner ) )
                    {
                        // Looking for one of the types
                        if ( matchType( scanner ) )
                        {
                            // Looking for '='
                            if ( matchEqual( scanner ) )
                            {
                                // Token evaluation complete
                                return token;
                            }
                        }
                    }
                }
            }
            // Looking for '.'
            else if ( matchDot( scanner ) )
            {
                // Looking for one of the types
                if ( matchType( scanner ) )
                {
                    // Looking for '='
                    if ( matchEqual( scanner ) )
                    {
                        // Token evaluation complete
                        return token;
                    }
                }
            }
        }

        return Token.UNDEFINED;
    }


    // ── Evaluating Without Resume State ───────────────────────────────────────
    // Delegates to the full evaluate() with resume=false.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #evaluate(ICharacterScanner, boolean)} with
     * {@code resume = false}.
     *
     * {@inheritDoc}
     *
     * @param scanner  The character scanner.
     * @return         The success token or {@link Token#UNDEFINED}.
     */
    public IToken evaluate( ICharacterScanner scanner )
    {
        return this.evaluate( scanner, false );
    }


    // ── Matching the "group" Keyword ──────────────────────────────────────────
    // Cassian checks for the literal five-character sequence "group".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the "group" char sequence matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if "group" was consumed; {@code false} otherwise.
     */
    private boolean matchGroup( ICharacterScanner scanner )
    {
        return matchCharSequence( scanner, GROUP_SEQUENCE );
    }


    // ── Matching One of the Group Type Keywords ───────────────────────────────
    // Cassian checks for "expand" or "exact" type qualifiers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if one of the type char sequences (expand, exact) matches the
     * scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if a type sequence was consumed; {@code false} otherwise.
     */
    private boolean matchType( ICharacterScanner scanner )
    {
        for ( char[] typeSequence : TYPES_SEQUENCES )
        {
            if ( matchCharSequence( scanner, typeSequence ) )
            {
                return true;
            }
        }

        return false;
    }


    // ── Matching a Forward Slash ───────────────────────────────────────────────
    // Cassian checks for the '/' that introduces the objectClass or attr name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the '/' char matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if '/' was consumed; {@code false} otherwise.
     */
    private boolean matchSlash( ICharacterScanner scanner )
    {
        return matchChar( scanner, '/' );
    }


    // ── Matching a Period Separator ───────────────────────────────────────────
    // Cassian checks for the dot that precedes the type qualifier.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the '.' char matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if '.' was consumed; {@code false} otherwise.
     */
    private boolean matchDot( ICharacterScanner scanner )
    {
        return matchChar( scanner, '.' );
    }


    // ── Matching the Equals Sign ──────────────────────────────────────────────
    // Cassian checks for the '=' that terminates the group prefix.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the '=' char matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if '=' was consumed; {@code false} otherwise.
     */
    private boolean matchEqual( ICharacterScanner scanner )
    {
        return matchChar( scanner, '=' );
    }


    // ── Checking That the Next Char Is Not '=', '/', or '.' ──────────────────
    // Cassian consumes characters that are part of an objectClass or attr name —
    // anything that is NOT a terminator (=, /, ., EOF).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} and consumes a character if the next char is NOT
     * {@code '='}, {@code '/'}, {@code '.'}, or EOF. Used to scan past objectClass
     * or attribute name characters while looking for the terminator.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if a non-terminator character was consumed.
     */
    private boolean doesNotMatchEqualSlashOrDot( ICharacterScanner scanner )
    {
        if ( matchEOF( scanner ) )
        {
            scanner.unread();

            return false;
        }
        else if ( matchChar( scanner, '=' ) )
        {
            scanner.unread();

            return false;
        }
        else if ( matchChar( scanner, '/' ) )
        {
            scanner.unread();

            return false;
        }
        else if ( matchChar( scanner, '.' ) )
        {
            scanner.unread();

            return false;
        }
        else
        {
            scanner.read();

            return true;
        }
    }


    // ── Checking That the Next Char Is Not '=' or '.' ─────────────────────────
    // Similar to doesNotMatchEqualSlashOrDot but stops at '=' and '.' only —
    // used when scanning an attribute name after the second slash.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} and consumes a character if the next char is NOT
     * {@code '='}, {@code '.'}, or EOF. Used to scan past attribute name characters
     * (after the second slash) while looking for the terminator.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if a non-terminator character was consumed.
     */
    private boolean doesNotMatchEqualOrDot( ICharacterScanner scanner )
    {
        if ( matchEOF( scanner ) )
        {
            scanner.unread();

            return false;
        }
        else if ( matchChar( scanner, '=' ) )
        {
            scanner.unread();

            return false;
        }
        else if ( matchChar( scanner, '.' ) )
        {
            scanner.unread();

            return false;
        }
        else
        {
            scanner.read();

            return true;
        }
    }
}
