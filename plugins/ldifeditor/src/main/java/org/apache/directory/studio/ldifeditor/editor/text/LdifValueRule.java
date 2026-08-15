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
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


// ── CLASS: LdifValueRule — C-3PO READS THE FOLDED TRANSMISSION BODY ──────────
// C-3PO reads a multi-line folded transmission value, recognising that a
// leading space on the continuation line means "this is still part of the
// previous value, not a new field" — so he glues the segments together before
// marking the whole thing as a single token.
// LdifValueRule does the same for the syntax highlighter: it consumes a
// value token body (including LDIF-folded continuation lines that start with
// a space) and returns a match only when at least one character was consumed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link IRule} that matches the body of an LDIF value token including
 * any LDIF-folded continuation lines (lines whose first character is a space).
 * Used inside {@link org.eclipse.jface.text.rules.RuleBasedScanner}-based
 * token highlighting to consume value segments correctly.
 * Think of this as C-3PO gluing multi-line folded values together before
 * handing the whole segment to the syntax highlighter.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifValueRule implements IRule
{

    /** The token to return when the rule matches at least one character. */
    private IToken token;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new value rule that returns {@code token} on a successful match.
     *
     * @param token  the token to emit when value content is found
     */
    public LdifValueRule( IToken token )
    {
        this.token = token;
    }


    // ── EVALUATE ─────────────────────────────────────────────────────────────
    // Ask matchContent to consume the value; return the token if anything
    // was consumed, UNDEFINED if nothing was.
    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link #token} if {@link #matchContent} consumed at least one
     * character, otherwise returns {@link Token#UNDEFINED}.</p>
     */
    public IToken evaluate( ICharacterScanner scanner )
    {

        if ( matchContent( scanner ) )
        {
            return this.token;
        }
        else
        {
            return Token.UNDEFINED;
        }

    }


    // ── MATCH CONTENT INCLUDING FOLDED LINES ─────────────────────────────────
    // Keep reading characters; when we hit a newline, peek at the next char.
    // A leading space means the line is a continuation — consume it and carry
    // on.  Any other character (or EOF) after the newline ends the value.
    /**
     * Consumes value characters from {@code scanner}, honouring LDIF line-
     * folding: a newline followed immediately by a space character is treated
     * as a continuation of the current value rather than a token boundary.
     * The trailing newline (if any) that ends the value is <em>not</em>
     * consumed — the scanner is unreaded past it.
     *
     * @param scanner  the character scanner positioned at the start of the
     *                 value body
     * @return {@code true} if at least one character was consumed
     */
    protected boolean matchContent( ICharacterScanner scanner )
    {

        int count = 0;

        int c = scanner.read();
        while ( c != ICharacterScanner.EOF )
        {

            // check for folding
            if ( c == '\n' || c == '\r' )
            {
                StringBuffer temp = new StringBuffer( 3 );
                if ( c == '\r' )
                {
                    c = scanner.read();
                    if ( c == '\n' )
                    {
                        temp.append( c );
                    }
                    else
                    {
                        scanner.unread();
                    }
                }
                else if ( c == '\n' )
                {
                    c = scanner.read();
                    if ( c == '\r' )
                    {
                        temp.append( c );
                    }
                    else
                    {
                        scanner.unread();
                    }
                }

                c = scanner.read();
                if ( c == ' ' && c != ICharacterScanner.EOF )
                {
                    // space after newline, continue
                    temp.append( c );
                    count += temp.length();
                    c = scanner.read();
                }
                else
                {
                    for ( int i = 0; i < temp.length(); i++ )
                        scanner.unread();
                    break;
                }
            }
            else
            {
                count++;
                c = scanner.read();
            }
        }
        scanner.unread();

        return count > 0;
    }

}
