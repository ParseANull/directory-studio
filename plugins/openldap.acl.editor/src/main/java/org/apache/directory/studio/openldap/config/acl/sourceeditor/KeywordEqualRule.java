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


// ── CLASS: KeywordEqualRule — CASSIAN READING KEYWORD= DIRECTIVES IN ACL CODE ─
// Among the ACL directives Cassian intercepts, several are straightforward
// keyword= constructs: "attrs=", "filter=", "ssf=", "sasl_ssf=", "tls_ssf=",
// "transport_ssf=", and "dnattr=". Each is a fixed keyword followed immediately
// by an '=' sign. This rule tries every keyword in the list against the scanner
// stream and confirms the '=' terminator. If any match succeeds it colours the
// whole prefix as a keyword token.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace text predicate rule that recognises ACL keyword= constructs in the
 * OpenLDAP ACL source editor. Handles the following keywords:
 * <pre>
 *   attrs=  attr=  dnattr=  filter=  ssf=  sasl_ssf=  tls_ssf=  transport_ssf=
 * </pre>
 * Returns the configured token when {@code KEYWORD=} is found at the current
 * scanner position; returns {@link Token#UNDEFINED} otherwise.
 * Think of this rule as Cassian identifying the simple keyword= directives in
 * the stolen Imperial access control file.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class KeywordEqualRule extends AbstractRule
{
    /**
     * The array of the types char sequences
     */
    private static char[][] KEYWORDS_SEQUENCES = new char[][]
        {
            new char[]
                { 'a', 't', 't', 'r', 's' },
            new char[]
                { 'a', 't', 't', 'r' },
            new char[]
                { 'd', 'n', 'a', 't', 't', 'r' },
            new char[]
                { 'f', 'i', 'l', 't', 'e', 'r' },
            new char[]
                { 's', 's', 'f' },
            new char[]
                { 's', 'a', 's', 'l', '_', 's', 's', 'f' },
            new char[]
                { 't', 'l', 's', '_', 's', 's', 'f' },
            new char[]
                { 't', 'r', 'a', 'n', 's', 'p', 'o', 'r', 't', '_', 's', 's', 'f' },
            new char[]
                { 's', 'a', 's', 's', '_', 's', 's', 'f' }
    };


    // ── Constructing the Rule With Its Token ──────────────────────────────────
    // Cassian picks up the KEYWORD_ATTRIBUTE label he'll attach to keyword= matches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new KeywordEqualRule that returns the given token when a
     * {@code keyword=} pattern is found.
     *
     * @param token  The token to return on a successful match.
     */
    public KeywordEqualRule( IToken token )
    {
        super( token );
    }


    // ── Evaluating the Scanner for a Keyword= Pattern (Resume Variant) ────────
    // Cassian tries each keyword in the list. If one matches and is immediately
    // followed by '=', he returns the token. If nothing matches he returns
    // UNDEFINED and the scanner is left unchanged.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attempts to match a keyword= pattern starting at the current scanner
     * position. Tries each keyword in order and confirms the trailing '='.
     *
     * <p>For example — Cassian recognising "filter=" in the source stream:</p>
     * <pre>
     *   // scanner is positioned at 'f' of "filter=(uid=*)"
     *   evaluate(scanner, false); // → token (KEYWORD_TOKEN)
     * </pre>
     *
     * @param scanner  The character scanner.
     * @param resume   Whether evaluation is being resumed (not used).
     * @return         The success token if a keyword= pattern was found; {@link Token#UNDEFINED} otherwise.
     */
    public IToken evaluate( ICharacterScanner scanner, boolean resume )
    {
        // Looking for any keyword
        if ( matchKeyword( scanner ) )
        {
            // Looking for '='
            if ( matchEqual( scanner ) )
            {
                // Token evaluation complete
                return token;
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


    // ── Matching One of the Known ACL Keywords ────────────────────────────────
    // Cassian checks every keyword from the list in order and accepts the first
    // one that matches — the scanner is left after the matched characters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks if one of the defined keyword char sequences (attrs, attr, dnattr,
     * filter, ssf, sasl_ssf, tls_ssf, transport_ssf) matches the scanner input.
     *
     * @param scanner  The scanner input.
     * @return         {@code true} if a keyword was consumed; {@code false} otherwise.
     */
    private boolean matchKeyword( ICharacterScanner scanner )
    {
        for ( char[] typeSequence : KEYWORDS_SEQUENCES )
        {
            if ( matchCharSequence( scanner, typeSequence ) )
            {
                return true;
            }
        }

        return false;
    }


    // ── Matching the Equals Sign ──────────────────────────────────────────────
    // Cassian confirms the '=' immediately after the keyword — that's what
    // distinguishes "attrs" the keyword from "attrs" as a value.
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
}
