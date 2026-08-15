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


// ── CLASS: DnRule — CASSIAN READING THE "dn" LOCATOR IN STOLEN IMPERIAL CODE ─
// In the stolen ACL file Cassian intercepts, DN-based who-clauses come in
// several forms: bare "dn=", type-qualified "dn.exact=", type+comma+modifier
// "dn.regex,expand=", or level-based "dn.level{N}=". Cassian must recognise
// all of these variants in the source text so the syntax highlighter can
// colour them correctly. This rule tries each form in order, unreading the
// scanner if a form doesn't match, and returns the success token only when a
// complete DN clause prefix is confirmed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace text predicate rule that recognises the {@code dn} prefix of an
 * OpenLDAP ACL DN who-clause in the source editor. Handles all four forms:
 * <pre>
 *   dn=
 *   dn.TYPE=           (TYPE = regex|base|exact|one|subtree|children)
 *   dn.TYPE,expand=
 *   dn.level{N}=
 * </pre>
 * Returns the configured token when a match is found, or {@link Token#UNDEFINED}
 * when the current scanner position does not start with a recognised form.
 * Think of this rule as Cassian identifying the DN locator pattern in a
 * stolen Imperial access control file.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DnRule extends AbstractRule
{

    /**
     * The "dn" char sequence.
     */
    private static char[] DN_SEQUENCE = new char[]
        { 'd', 'n' };

    /**
     * The array of the types char sequences
     */
    private static char[][] TYPES_SEQUENCES = new char[][]
        {
            new char[]
                { 'r', 'e', 'g', 'e', 'x' },
            new char[]
                { 'b', 'a', 's', 'e' },
            new char[]
                { 'e', 'x', 'a', 'c', 't' },
            new char[]
                { 'o', 'n', 'e' },
            new char[]
                { 's', 'u', 'b', 't', 'r', 'e', 'e' },
            new char[]
                { 'c', 'h', 'i', 'l', 'd', 'r', 'e', 'n' }
    };

    /**
     * The "expand" char sequence.
     */
    private static char[] EXPAND_SEQUENCE = new char[]
        { 'e', 'x', 'p', 'a', 'n', 'd' };

    /**
     * The "level" char sequence.
     */
    private static char[] LEVEL_SEQUENCE = new char[]
        { 'l', 'e', 'v', 'e', 'l' };


    // ── Constructing the Rule With Its Token ──────────────────────────────────
    // Cassian picks up the label he'll attach to confirmed DN prefix matches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DnRule that returns the given token on a successful match.
     *
     * @param token  The token to return when the DN prefix pattern is recognised.
     */
    public DnRule( IToken token )
    {
        super( token );
    }


    // ── Evaluating the Scanner Position for the DN Pattern (Resume Variant) ───
    // Cassian reads ahead character by character to check whether the current
    // position starts a DN who-clause. He tries the simpler forms first and
    // falls back to more complex ones, unreading on every mismatch.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attempts to match a DN clause prefix starting at the current scanner
     * position. Tries each variant in order and returns the success token on the
     * first complete match. Returns {@link Token#UNDEFINED} if no variant matches.
     *
     * <p>For example — Cassian recognising "dn.exact=" in the source stream:</p>
     * <pre>
     *   // scanner is positioned at 'd' of "dn.exact=\"uid=*\""
     *   evaluate(scanner, false); // → token (KEYWORD_TOKEN)
     * </pre>
     *
     * @param scanner  The character scanner positioned at the potential match start.
     * @param resume   Whether evaluation is being resumed (not used in this implementation).
     * @return         The success token if a DN prefix was found; {@link Token#UNDEFINED} otherwise.
     */
    public IToken evaluate( ICharacterScanner scanner, boolean resume )
    {
        // Looking for "dn"
        if ( matchDn( scanner ) )
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
                    // Looking for ','
                    else if ( matchComma( scanner ) )
                    {
                        // Looking for "expand"
                        if ( matchExpand( scanner ) )
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
                // Looking for "level"
                else if ( matchLevel( scanner ) )
                {
                    // Looking for '{'
                    if ( matchOpenCurlyBracket( scanner ) )
                    {
                        // Looking for digits
                        boolean atLeastFoundOneDigit = false;
                        while ( matchDigit( scanner ) )
                        {
                            atLeastFoundOneDigit = true;
                        }

                        // Checking if we found at least one digit
                        // and the next char is '}'
                        if ( atLeastFoundOneDigit && ( matchCloseCurlyBracket( scanner ) ) )
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
        }

        return Token.UNDEFINED;
    }


    // ── Evaluating Without Resume State ───────────────────────────────────────
    // The simple entry point — delegates to the full evaluate() with resume=false.
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


    // ── Matching the "dn" Keyword ─────────────────────────────────────────────
    // Cassian checks for the literal two-character sequence "dn".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the "dn" char sequence matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if "dn" was consumed; {@code false} otherwise.
     */
    private boolean matchDn( ICharacterScanner scanner )
    {
        return matchCharSequence( scanner, DN_SEQUENCE );
    }


    // ── Matching One of the DN Type Keywords ─────────────────────────────────
    // Cassian checks each supported type (regex/base/exact/one/subtree/children)
    // in turn and accepts the first one that matches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if one of the DN type char sequences (regex, base, exact, one,
     * subtree, children) matches the scanner input.
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


    // ── Matching the "expand" Modifier ────────────────────────────────────────
    // Cassian checks for the "expand" modifier that can follow a comma after
    // the type in "dn.regex,expand=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the "expand" char sequence matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if "expand" was consumed; {@code false} otherwise.
     */
    private boolean matchExpand( ICharacterScanner scanner )
    {
        return matchCharSequence( scanner, EXPAND_SEQUENCE );
    }


    // ── Matching the "level" Keyword ──────────────────────────────────────────
    // Cassian checks for the "level" keyword before the "{N}" block.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the "level" char sequence matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if "level" was consumed; {@code false} otherwise.
     */
    private boolean matchLevel( ICharacterScanner scanner )
    {
        return matchCharSequence( scanner, LEVEL_SEQUENCE );
    }


    // ── Matching a Period Separator ───────────────────────────────────────────
    // Cassian checks for the dot separating "dn" from the type qualifier.
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


    // ── Matching a Comma Separator ────────────────────────────────────────────
    // Cassian checks for the comma between type and "expand" in "dn.regex,expand=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the ',' char matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if ',' was consumed; {@code false} otherwise.
     */
    private boolean matchComma( ICharacterScanner scanner )
    {
        return matchChar( scanner, ',' );
    }


    // ── Matching the Equals Sign ──────────────────────────────────────────────
    // Cassian checks for the '=' that terminates the DN prefix and precedes
    // the quoted DN pattern.
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


    // ── Matching an Open Curly Bracket ────────────────────────────────────────
    // Cassian checks for '{' that opens the level number in "dn.level{N}=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the '{' char matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if '{' was consumed; {@code false} otherwise.
     */
    private boolean matchOpenCurlyBracket( ICharacterScanner scanner )
    {
        return matchChar( scanner, '{' );
    }


    // ── Matching a Close Curly Bracket ────────────────────────────────────────
    // Cassian checks for '}' that closes the level number in "dn.level{N}=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if the '}' char matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if '}' was consumed; {@code false} otherwise.
     */
    private boolean matchCloseCurlyBracket( ICharacterScanner scanner )
    {
        return matchChar( scanner, '}' );
    }
}
