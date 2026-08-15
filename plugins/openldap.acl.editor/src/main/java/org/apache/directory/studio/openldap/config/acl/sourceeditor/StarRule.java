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


// ── CLASS: StarRule — CASSIAN SPOTTING THE CATCH-ALL "*" IN THE IMPERIAL FILE ─
// Among all the specific access control directives in the stolen Imperial file,
// "*" is the simplest: it means "everyone" or "all resources". Cassian doesn't
// need to read ahead — one character is enough. If the next byte is an asterisk
// he marks it as the star token; otherwise he puts the byte back and moves on.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace text predicate rule that recognises the single-character wildcard
 * {@code *} in the OpenLDAP ACL source editor. Used to syntax-highlight the
 * universal who-clause ({@code by * none}) and the universal what-clause
 * ({@code to *}).
 *
 * <p>Returns the configured token when {@code *} is the next character, or
 * {@link Token#UNDEFINED} otherwise.</p>
 *
 * <p>Think of this rule as Cassian spotting Tarkin's catch-all stamp
 * in the access file — one character, unmistakable.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StarRule extends AbstractRule
{

    // ── Constructing the Rule With Its Token ──────────────────────────────────
    // Cassian picks up the STAR label he'll attach to '*' matches.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new StarRule that returns the given token when {@code *} is found.
     *
     * @param token  The token to return on a successful match.
     */
    public StarRule( IToken token )
    {
        super( token );
    }


    // ── Evaluating the Scanner for a Star Character (Resume Variant) ──────────
    // Cassian reads one character. If it's '*' he stamps it as the star token.
    // If not, he unreads (the AbstractRule helper handles that) and returns
    // UNDEFINED so the next rule in the scanner can try its pattern.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attempts to match a {@code *} character at the current scanner position.
     * Consumes the character and returns the success token if it is {@code *};
     * otherwise the scanner is left unchanged and {@link Token#UNDEFINED} is returned.
     *
     * <p>For example — Cassian confirming the catch-all in "to *":</p>
     * <pre>
     *   // scanner is positioned at '*' of "to *"
     *   evaluate(scanner, false); // → token
     * </pre>
     *
     * @param scanner  The character scanner.
     * @param resume   Whether evaluation is being resumed (not used).
     * @return         The success token if {@code *} was found; {@link Token#UNDEFINED} otherwise.
     */
    public IToken evaluate( ICharacterScanner scanner, boolean resume )
    {
        // Looking for '*'
        if ( matchChar( scanner, '*' ) )
        {
            // Token evaluation complete
            return token;
        }
        else
        {
            // Not what was expected
            return Token.UNDEFINED;
        }
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
}
