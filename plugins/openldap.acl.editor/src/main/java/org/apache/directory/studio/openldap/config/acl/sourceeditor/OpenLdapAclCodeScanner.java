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


// ── CLASS: OpenLdapAclCodeScanner — CASSIAN SETTING UP THE COLOUR-CODED DISPLAY
// Cassian configures his rebel intelligence terminal for maximum clarity: quoted
// strings are highlighted in value colour, ACL keywords (by, to, dn, manage,
// read, …) are highlighted in bold attribute-type colour, and everything else
// uses the default foreground colour. The scanner applies four specific-pattern
// rules (DnRule, GroupRule, KeywordEqualRule, StarRule) before falling back to a
// WordRule that colour-codes a fixed list of plain keywords. The result is a
// RuleBasedScanner that the presentation reconciler uses to repaint the source
// editor whenever the document changes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link RuleBasedScanner} that provides syntax colouring for the
 * OpenLDAP ACL source editor. Registers the following rules (in priority order):
 * <ol>
 *   <li>Single-line string rules for {@code "…"} and {@code '…'} (STRING token)</li>
 *   <li>Whitespace rule (no token, consumed silently)</li>
 *   <li>{@link DnRule}, {@link GroupRule}, {@link KeywordEqualRule}, {@link StarRule}
 *       (KEYWORD token for complex prefixed keywords)</li>
 *   <li>{@link WordRule} with {@link OpenLdapAclWordDetector} mapping each entry
 *       in {@link #aclKeywords} to the KEYWORD token</li>
 * </ol>
 *
 * <p>Think of this class as Cassian's colour-coded display setup — each syntactic
 * category gets the right shade before the officer reads the ACL text.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclCodeScanner extends RuleBasedScanner
{
    // ── The Full List of Plain ACL Keywords ───────────────────────────────────
    // Cassian memorises every plain keyword in the ACL grammar. The WordRule
    // maps each of these strings to the KEYWORD token so they are highlighted
    // in bold whenever they appear in the source text.
    // ─────────────────────────────────────────────────────────────────────────
    /** Keywords */
    public static final String[] aclKeywords = new String[]
        { "anonymous", //$NON-NLS-1$
            "auth", //$NON-NLS-1$
            "break", //$NON-NLS-1$
            "by", //$NON-NLS-1$
            "c", //$NON-NLS-1$
            "compare", //$NON-NLS-1$
            "continue", //$NON-NLS-1$
            "disclose", //$NON-NLS-1$
            "dnattr=", //$NON-NLS-1$
            "entry", //$NON-NLS-1$
            "m", //$NON-NLS-1$
            "manage", //$NON-NLS-1$
            "none", //$NON-NLS-1$
            "one", //$NON-NLS-1$
            "r", //$NON-NLS-1$
            "read", //$NON-NLS-1$
            "s", //$NON-NLS-1$
            "search", //$NON-NLS-1$
            "self", //$NON-NLS-1$
            "ssf=", //$NON-NLS-1$
            "stop", //$NON-NLS-1$
            "to", //$NON-NLS-1$
            "users", //$NON-NLS-1$
            "x", //$NON-NLS-1$
            "w", //$NON-NLS-1$
            "write" }; //$NON-NLS-1$


    // ── Setting Up the Scanner Rules ──────────────────────────────────────────
    // Cassian builds the rule list in priority order: quoted strings first, then
    // whitespace, then complex prefix rules, and finally the plain keyword word
    // rule. The provider supplies the TextAttribute objects for each token.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the scanner and registers all colouring rules. Rules are evaluated
     * in list order — the first rule that matches wins. The scanner is stored in
     * the plugin singleton and shared across all source viewer instances.
     *
     * <p>For example — Cassian's display showing "by users read" in bold:</p>
     * <pre>
     *   OpenLdapAclCodeScanner scanner = new OpenLdapAclCodeScanner(provider);
     *   // install via DefaultDamagerRepairer in the source viewer configuration
     * </pre>
     *
     * @param provider  The {@link OpenLdapAclTextAttributeProvider} that supplies
     *                  colour/style {@link org.eclipse.jface.text.TextAttribute}
     *                  objects for each token type.
     */
    public OpenLdapAclCodeScanner( OpenLdapAclTextAttributeProvider provider )
    {
        List<IRule> rules = new ArrayList<IRule>();

        IToken keyword = new Token( provider.getAttribute( OpenLdapAclTextAttributeProvider.KEYWORD_ATTRIBUTE ) );
        IToken string = new Token( provider.getAttribute( OpenLdapAclTextAttributeProvider.STRING_ATTRIBUTE ) );
        IToken undefined = new Token( provider.getAttribute( OpenLdapAclTextAttributeProvider.DEFAULT_ATTRIBUTE ) );

        // Rules for Strings
        rules.add( new SingleLineRule( "\"", "\"", string, '\0', true ) ); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add( new SingleLineRule( "'", "'", string, '\0', true ) ); //$NON-NLS-1$ //$NON-NLS-2$

        // Generic rule for whitespaces
        rules.add( new WhitespaceRule( new IWhitespaceDetector()
        {
            public boolean isWhitespace( char c )
            {
                return Character.isWhitespace( c );
            }
        } ) );

        // Rules for specific not simple keywords
        rules.add( new DnRule( keyword ) );
        rules.add( new GroupRule( keyword ) );
        rules.add( new KeywordEqualRule( keyword ) );
        rules.add( new StarRule( keyword ) );

        // If the word isn't in the List, returns undefined
        WordRule wr = new WordRule( new OpenLdapAclWordDetector(), undefined );
        rules.add( wr );

        // Adding keywords
        for ( String aclKeyword : aclKeywords )
        {
            wr.addWord( aclKeyword, keyword );
        }

        IRule[] param = new IRule[rules.size()];
        rules.toArray( param );
        setRules( param );
    }

    // ── Word Detector: What Counts as a Word in ACL Text ─────────────────────
    // Cassian's word detector is lenient — it includes letters, digits, and a
    // wide variety of punctuation characters that appear in DN patterns, OIDs,
    // and attribute names. This allows the WordRule to correctly identify both
    // plain keywords and complex tokens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * An {@link IWordDetector} for OpenLDAP ACL source text. Accepts a broad
     * set of characters as part of a word so that DN components, attribute names,
     * and ACL keywords are all recognised correctly.
     *
     * <p>Word start: letter, {@code . _ ? $}</p>
     * <p>Word part: letter/digit plus {@code _ $ # @ ~ . ? * !}</p>
     *
     * @author <a href="mailto:$dev@directory.apache.org">Apache Directory Project</a>
     */
    static class OpenLdapAclWordDetector implements IWordDetector
    {
        // ── Checking Whether a Character Can Continue a Word ──────────────────
        // Cassian checks whether the current character is still part of the word
        // he is reading — includes letters, digits, and ACL-relevant punctuation.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if the given character may appear inside a word
         * in ACL source text. Allows letters, digits, and the characters
         * {@code _ $ # @ ~ . ? * !}.
         *
         * @param c  The character to test.
         * @return   {@code true} if it is a valid word-interior character.
         */
        public boolean isWordPart( char c )
        {
            return ( Character.isLetterOrDigit( c ) || c == '_' || c == '$' || c == '#' || c == '@' || c == '~'
                || c == '.' || c == '?' || c == '*' || c == '!' );
        }


        // ── Checking Whether a Character Can Start a Word ─────────────────────
        // Cassian checks whether the current character can begin a new word token.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * Returns {@code true} if the given character may start a word in ACL
         * source text. Allows letters and the characters {@code . _ ? $}.
         *
         * @param c  The character to test.
         * @return   {@code true} if it is a valid word-start character.
         */
        public boolean isWordStart( char c )
        {
            return ( Character.isLetter( c ) || c == '.' || c == '_' || c == '?' || c == '$' );
        }
    }
}
