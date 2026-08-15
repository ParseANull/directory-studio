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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;


// ── CLASS: SchemaCodeScanner — C-3PO READS THE JAWA DIALECT ──────────────────
// On Tatooine, C-3PO listens to the Jawas' rapid-fire dialect and breaks it down:
// "That sound is a greeting. That sequence is a price. That one over there is a
// warning." He identifies each token's category before translating the whole thing.
// Our SchemaCodeScanner does the same: it reads raw schema source text character
// by character and classifies each token — keyword, OID, string literal, attribute
// type declaration, object class declaration, or plain unrecognised text — so the
// Eclipse editor can colour each one appropriately.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse {@link RuleBasedScanner} that tokenises LDAP schema definition files
 * (the OpenLDAP-style format with {@code attributetype} and {@code objectclass}
 * declarations). It applies a chain of rules — string literals, OIDs, keywords,
 * and named attribute/object-class declarations — and returns a styled token for
 * each recognised pattern. The tokens are later used by
 * {@link SchemaSourceViewerConfiguration} to drive syntax highlighting.
 * Think of it as C-3PO parsing the Jawa marketplace: every syllable gets classified
 * into a category before the full translation can be assembled.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaCodeScanner extends RuleBasedScanner
{
    String attributype = "attributetype"; //$NON-NLS-1$

    String objectclass = "objectclass"; //$NON-NLS-1$

    String[] keywords = new String[]
        { "NAME", "DESC", "OBSOLETE", "SUP", "EQUALITY", "ORDERING", "MUST", "MAY", "STRUCTURAL", "SUBSTR", "SYNTAX", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
            "SINGLE-VALUE", "COLLECTIVE", "NO-USER-MODIFICATION", "USAGE", "userApplications", "directoryOperation", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            "distributedOperation", "dSAOperation", "ABSTRACT", "STRUCTURAL", "AUXILIARY", "MUST", "MAY" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$


    // ── C-3PO PREPARES HIS TRANSLATION PROTOCOL ──────────────────────────────────
    // Before C-3PO can translate, he loads the grammar rules for Jawa: string delimiters
    // first (quoted phrases), then whitespace (pauses), then numeric OID sequences
    // (price codes), then the full word vocabulary (greetings, nouns, verbs).
    // We build the rule list in the same order: earlier rules have priority over later ones.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the scanner and wires up all the tokenisation rules in priority order.
     * The rules are evaluated left to right; the first rule that matches wins. We set up:
     * <ol>
     *   <li>Single-line string rules for double- and single-quoted literals.</li>
     *   <li>A whitespace rule so blanks are never mistakenly tokenised as words.</li>
     *   <li>An OID word rule for numeric dotted sequences (e.g. {@code 2.5.4.3}).</li>
     *   <li>A word rule that handles {@code attributetype}, {@code objectclass}, and
     *       all schema keywords; anything else gets the "undefined" (default) token.</li>
     * </ol>
     *
     * <p>For example — C-3PO loads the Jawa grammar modules:</p>
     * <pre>
     *   "\"inetOrgPerson\""  →  STRING token (bold green)
     *   "2.5.4.3"           →  OID token    (OID colour)
     *   "NAME"              →  keyword token (bold blue)
     *   "attributetype"     →  AT token      (bold red)
     *   "foobar"            →  undefined token (default colour)
     * </pre>
     *
     * @param provider  the {@link SchemaTextAttributeProvider} that maps token type names
     *                  to actual SWT {@link org.eclipse.jface.text.TextAttribute} objects
     */
    public SchemaCodeScanner( SchemaTextAttributeProvider provider )
    {
        List<IRule> rules = new ArrayList<IRule>();

        IToken keyword = new Token( provider.getAttribute( SchemaTextAttributeProvider.KEYWORD_ATTRIBUTE ) );
        IToken string = new Token( provider.getAttribute( SchemaTextAttributeProvider.STRING_ATTRIBUTE ) );
        IToken undefined = new Token( provider.getAttribute( SchemaTextAttributeProvider.DEFAULT_ATTRIBUTE ) );
        IToken ATToken = new Token( provider.getAttribute( SchemaTextAttributeProvider.ATTRIBUTETYPE_ATTRIBUTE ) );
        IToken OCToken = new Token( provider.getAttribute( SchemaTextAttributeProvider.OBJECTCLASS_ATTRIBUTE ) );
        IToken oid = new Token( provider.getAttribute( SchemaTextAttributeProvider.OID_ATTRIBUTE ) );

        // Rules for Strings
        rules.add( new SingleLineRule( "\"", "\"", string, '\0', true ) ); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add( new SingleLineRule( "'", "'", string, '\0', true ) ); //$NON-NLS-1$ //$NON-NLS-2$
        // Generic rule for whitespaces
        rules.add( new WhitespaceRule( new IWhitespaceDetector()
        {
            /**
             * Indicates if the given character is a whitespace
             * @param c the character to analyse
             * @return <code>true</code> if the character is to be considered as a whitespace,  <code>false</code> if not.
             * @see org.eclipse.jface.text.rules.IWhitespaceDetector#isWhitespace(char)
             */
            public boolean isWhitespace( char c )
            {
                return Character.isWhitespace( c );
            }
        } ) );

        WordRule wrOID = new WordRule( new SchemaOIDDetector(), oid );

        rules.add( wrOID );

        // If the word isn't in the List, returns undefined
        WordRule wr = new WordRule( new SchemaWordDetector(), undefined );

        // 'attributetype' rule
        wr.addWord( attributype, ATToken );

        // 'objectclass' rule
        wr.addWord( objectclass, OCToken );

        // Adding Keywords
        for ( String kw : keywords )
        {
            wr.addWord( kw, keyword );
        }

        rules.add( wr );

        IRule[] param = new IRule[rules.size()];
        rules.toArray( param );
        setRules( param );
    }

    // ── CLASS: SchemaWordDetector — C-3PO RECOGNISES JAWA WORDS ─────────────────
    // C-3PO listens to Jawa and distinguishes word boundaries from noise: a word
    // can start with a letter, a dot, a dollar sign, underscore, or question mark.
    // It can continue with those same characters plus digits, hyphens, hash, at-sign,
    // and tilde. Everything else signals the end of a word.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * An {@link IWordDetector} that recognises the broad character set used in LDAP
     * schema identifiers, keyword names, and descriptor tokens. Start characters include
     * letters, dots, underscores, dollar signs, and question marks. Continuation
     * characters add digits, hyphens, hashes, at-signs, and tildes.
     * Think of it as C-3PO's ear for Jawa word boundaries.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    static class SchemaWordDetector implements IWordDetector
    {
        // ── C-3PO RECOGNISES THE MIDDLE OF A JAWA WORD ───────────────────────────
        // Once a word has started, C-3PO keeps listening as long as the characters
        // look like part of the same token — letters, digits, hyphens, and the
        // special characters that LDAP schema identifiers can contain.
        // ─────────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if the given character can appear in the middle (or tail)
         * of a schema word token. This is a broad set that covers LDAP descriptor names,
         * OID aliases, and schema keyword fragments.
         *
         * @param c  the character to test
         * @return   {@code true} if it is a valid word-continuation character
         */
        public boolean isWordPart( char c )
        {
            return ( Character.isLetterOrDigit( c ) || c == '_' || c == '-' || c == '$' || c == '#' || c == '@'
                || c == '~' || c == '.' || c == '?' );
        }


        // ── C-3PO RECOGNISES THE START OF A JAWA WORD ────────────────────────────
        // C-3PO knows a new Jawa word is beginning when he hears a letter, a dot,
        // an underscore, a dollar sign, or a question mark. Digits alone do not start
        // words — those are handled by the OID detector instead.
        // ─────────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if the given character can begin a schema word token.
         * Digits are intentionally excluded because numeric-only starts belong to OIDs,
         * which are handled by the separate {@link SchemaOIDDetector}.
         *
         * @param c  the character to test
         * @return   {@code true} if it can start a schema word
         */
        public boolean isWordStart( char c )
        {
            return ( Character.isLetter( c ) || c == '.' || c == '_' || c == '?' || c == '$' );
        }
    }

    // ── CLASS: SchemaOIDDetector — C-3PO READS THE JAWA PRICE CODE ───────────────
    // Jawa merchants communicate prices and item codes as rapid sequences of digits
    // and dots — "2.5.4.3!" translates to a specific item. C-3PO's OID module
    // recognises exactly that: sequences that start with a digit and continue with
    // more digits and dots, stopping at anything else.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * An {@link IWordDetector} specialised for LDAP OID strings — dotted numeric
     * sequences like {@code 2.5.4.3} or {@code 1.3.6.1.4.1.1466.115.121.1.15}.
     * Start character is a digit; continuation characters are digits and dots.
     * We keep this separate from {@link SchemaWordDetector} so OIDs get their own
     * distinct token type and therefore a distinct colour in the editor.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    static class SchemaOIDDetector implements IWordDetector
    {
        // ── C-3PO RECOGNISES MORE DIGITS AND DOTS IN THE PRICE CODE ──────────────
        // Once C-3PO starts reading a price code, he keeps going as long as he
        // sees digits or dots. The moment he hits a letter or space, the code ends.
        // ─────────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if the given character can appear inside an OID token
         * after the first digit. Only digits and dots are valid OID continuation chars.
         *
         * @param c  the character to test
         * @return   {@code true} if it is a digit or a dot
         */
        public boolean isWordPart( char c )
        {
            return ( Character.isDigit( c ) || c == '.' );
        }


        // ── C-3PO HEARS THE START OF A PRICE CODE ────────────────────────────────
        // C-3PO's OID module activates the moment it hears an opening digit. Letters
        // and other characters bypass this detector and go to the word detector instead.
        // ─────────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if the given character can start an OID token. Only
         * a digit qualifies — if the first character is not a digit, the scanner
         * uses {@link SchemaWordDetector} instead.
         *
         * @param c  the character to test
         * @return   {@code true} if it is a digit (0-9)
         */
        public boolean isWordStart( char c )
        {
            return ( Character.isDigit( c ) );
        }
    }
}
