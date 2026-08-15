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
package org.apache.directory.studio.aciitemeditor.sourceeditor;


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


// ── CLASS: ACICodeScanner — C-3PO COLOUR-CODING THE ACI DIRECTIVE ─────────────
// C-3PO reads a raw ACI directive aloud and, as he goes, annotates every word
// with a coloured diplomatic tag: keywords in one colour, grant values in green,
// deny values in red, and quoted strings in a neutral tone.
// ACICodeScanner is that annotation pass: it scans the raw ACI text and assigns
// a typed token (and thus a colour) to every word.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link RuleBasedScanner} that tokenises ACI text for syntax highlighting.
 * On construction it builds rules for quoted strings, whitespace, and all ACI
 * keywords — categorised into general keywords, grant values, deny values,
 * itemOrUserFirst tokens, and user-section tokens — then hands them to the
 * base scanner.
 * Think of this class as C-3PO colour-coding a diplomatic directive: every word
 * gets a typed token so the editor can paint it the right colour.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACICodeScanner extends RuleBasedScanner
{
    /** 'identificationTag' keyword */
    public static final String IDENTIFICATION_TAG_PARTITION = "identificationTag"; //$NON-NLS-1$

    /** 'precedence' keyword */
    public static final String PRECEDENCE_PARTITION = "precedence"; //$NON-NLS-1$

    /** 'authenticationLevel' keyword */
    public static final String AUTHENTICATION_LEVEL_PARTITION = "authenticationLevel"; //$NON-NLS-1$

    /** Keywords for the itemOrUserFirst Section */
    public static final String[] ITEM_OR_USER_FIRST_SECTION_PARTITION = new String[]
        { "itemOrUserFirst", "itemFirst", "userFirst" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    /** Keywords for 'userFirst' section */
    public static final String[] USER_SECTION = new String[]
        { "userClasses", "userPermissions" }; //$NON-NLS-1$ //$NON-NLS-2$

    /** Keywords for AciItems values */
    public static final String[] ACI_KEYWORDS = new String[]
        { "protectedItems", //$NON-NLS-1$
            "itemPermissions", //$NON-NLS-1$
            "entry", //$NON-NLS-1$
            "allUserAttributeTypes", //$NON-NLS-1$
            "attributeType", //$NON-NLS-1$
            "allAttributeValues", //$NON-NLS-1$
            "allUserAttributeTypesAndValues", //$NON-NLS-1$
            "attributeValue", //$NON-NLS-1$
            "selfValue", //$NON-NLS-1$
            "rangeOfValues", //$NON-NLS-1$
            "maxValueCount", //$NON-NLS-1$
            "maxImmSub", //$NON-NLS-1$
            "restrictedBy", //$NON-NLS-1$
            "classes", //$NON-NLS-1$
            "grantsAndDenials", //$NON-NLS-1$
            "allUsers", //$NON-NLS-1$
            "thisEntry", //$NON-NLS-1$
            "name", //$NON-NLS-1$
            "userGroup", //$NON-NLS-1$
            "subtree", //$NON-NLS-1$
            "type", //$NON-NLS-1$
            "valuesIn", //$NON-NLS-1$
            "none", //$NON-NLS-1$
            "simple", //$NON-NLS-1$
            "strong" }; //$NON-NLS-1$

    /** Keywords for grant values */
    public static final String[] ACI_GRANT_VALUES = new String[]
        { "grantAdd", //$NON-NLS-1$
            "grantDiscloseOnError", //$NON-NLS-1$
            "grantRead", //$NON-NLS-1$
            "grantRemove", //$NON-NLS-1$
            "grantBrowse", //$NON-NLS-1$
            "grantExport", //$NON-NLS-1$
            "grantImport", //$NON-NLS-1$
            "grantModify", //$NON-NLS-1$
            "grantRename", //$NON-NLS-1$
            "grantReturnDN", //$NON-NLS-1$
            "grantCompare", //$NON-NLS-1$
            "grantFilterMatch", //$NON-NLS-1$
            "grantInvoke", }; //$NON-NLS-1$

    /** Keywords for deny values */
    public static final String[] ACI_DENY_VALUES = new String[]
        { "denyAdd", //$NON-NLS-1$
            "denyDiscloseOnError", //$NON-NLS-1$
            "denyRead", //$NON-NLS-1$
            "denyRemove", //$NON-NLS-1$
            "denyBrowse", //$NON-NLS-1$
            "denyExport", //$NON-NLS-1$
            "denyImport", //$NON-NLS-1$
            "denyModify", //$NON-NLS-1$
            "denyRename", //$NON-NLS-1$
            "denyReturnDN", //$NON-NLS-1$
            "denyCompare", //$NON-NLS-1$
            "denyFilterMatch", //$NON-NLS-1$
            "denyInvoke" }; //$NON-NLS-1$


    // ── WIRE UP THE COLOUR RULES ──────────────────────────────────────────────
    // C-3PO sits down with the ACI directive and his colour-coded annotation kit.
    // He sets up one rule for quoted strings, one for whitespace, and then a
    // big word rule with every keyword pre-loaded; unrecognised words fall back
    // to the default (undefined) token.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an {@code ACICodeScanner} and wires it up with token rules for ACI syntax.
     * Fetches {@link IToken} instances from the supplied {@link ACITextAttributeProvider}
     * (which knows the colour and style for each token type), then registers:
     * <ul>
     *   <li>Single-line rules for double- and single-quoted strings</li>
     *   <li>A whitespace rule</li>
     *   <li>A word rule mapping every ACI keyword, grant value, deny value,
     *       itemOrUserFirst token, and user-section keyword to its token</li>
     * </ul>
     *
     * <p>For example — the source viewer configuration creates one of these:</p>
     * <pre>
     *   ACICodeScanner scanner = new ACICodeScanner(textAttributeProvider);
     *   // scanner is now ready to tokenise ACI text for syntax highlighting
     * </pre>
     *
     * @param provider  the text-attribute provider that maps token types to colours and styles
     */
    public ACICodeScanner( ACITextAttributeProvider provider )
    {
        List<IRule> rules = new ArrayList<IRule>();

        IToken keyword = new Token( provider.getAttribute( ACITextAttributeProvider.KEYWORD_ATTRIBUTE ) );
        IToken undefined = new Token( provider.getAttribute( ACITextAttributeProvider.DEFAULT_ATTRIBUTE ) );
        IToken string = new Token( provider.getAttribute( ACITextAttributeProvider.STRING_ATTRIBUTE ) );
        IToken grantValue = new Token( provider.getAttribute( ACITextAttributeProvider.GRANT_VALUE ) );
        IToken denyValue = new Token( provider.getAttribute( ACITextAttributeProvider.DENY_VALUE ) );
        IToken identification = new Token( provider.getAttribute( ACITextAttributeProvider.IDENTIFICATION_ATTRIBUTE ) );
        IToken precedence = new Token( provider.getAttribute( ACITextAttributeProvider.PRECEDENCE_ATTRIBUTE ) );
        IToken authenticationLevel = new Token( provider
            .getAttribute( ACITextAttributeProvider.AUTHENTICATIONLEVEL_ATTRIBUTE ) );
        IToken itemOrUserFirst = new Token( provider.getAttribute( ACITextAttributeProvider.ITEMORUSERFIRST_ATTRIBUTE ) );
        IToken user = new Token( provider.getAttribute( ACITextAttributeProvider.USER_ATTRIBUTE ) );

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

        // If the word isn't in the List, returns undefined
        WordRule worldRule = new WordRule( new AciWordDetector(), undefined );

        // Adding Keywords
        for ( String aciKeyword : ACI_KEYWORDS )
        {
            worldRule.addWord( aciKeyword, keyword );
        }

        // Adding GrantValues
        for ( String aciGrantValue : ACI_GRANT_VALUES )
        {
            worldRule.addWord( aciGrantValue, grantValue );
        }

        // Adding DenyValues
        for ( String aciDenyValue : ACI_DENY_VALUES )
        {
            worldRule.addWord( aciDenyValue, denyValue );
        }

        // Adding itemOrUserFirstSectionPartition
        for ( String itemOrUserFirstSectionPartitionValue : ITEM_OR_USER_FIRST_SECTION_PARTITION )
        {
            worldRule.addWord( itemOrUserFirstSectionPartitionValue, itemOrUserFirst );
        }

        // Adding User
        for ( String userSectionValue : USER_SECTION )
        {
            worldRule.addWord( userSectionValue, user );
        }

        worldRule.addWord( IDENTIFICATION_TAG_PARTITION, identification );

        worldRule.addWord( PRECEDENCE_PARTITION, precedence );

        worldRule.addWord( AUTHENTICATION_LEVEL_PARTITION, authenticationLevel );

        rules.add( worldRule );

        IRule[] param = new IRule[rules.size()];
        rules.toArray( param );
        setRules( param );
    }

    // ── CLASS: AciWordDetector — C-3PO'S WORD BOUNDARY SENSOR ────────────────
    // Before C-3PO can label a word, he must know where it starts and stops.
    // ACI words begin with a letter or a few special characters and may contain
    // alphanumerics plus a handful of symbols — exactly what this detector encodes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * JFace {@link IWordDetector} for ACI syntax.
     * Defines the character sets that begin and continue an ACI keyword or identifier,
     * so the {@link WordRule} knows when to start and stop consuming a token.
     * Think of this as C-3PO's word-boundary sensor: he starts listening when he
     * sees a letter (or dot, underscore, etc.) and stops at anything else.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    static class AciWordDetector implements IWordDetector
    {
        // ── CONTINUE CONSUMING A WORD ─────────────────────────────────────────
        // C-3PO keeps reading as long as the next character is still part of
        // the same diplomatic token: letters, digits, or the handful of special
        // chars used in ACI identifiers.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * {@inheritDoc}
         */
        public boolean isWordPart( char c )
        {
            return ( Character.isLetterOrDigit( c ) || c == '_' || c == '$' || c == '#' || c == '@' || c == '~'
                || c == '.' || c == '?' );
        }


        // ── START A NEW WORD ──────────────────────────────────────────────────
        // C-3PO begins a new annotation the moment he sees a character that
        // can only appear at the start of an ACI identifier.
        // ─────────────────────────────────────────────────────────────────────
        /**
         * {@inheritDoc}
         */
        public boolean isWordStart( char c )
        {
            return ( Character.isLetter( c ) || c == '.' || c == '_' || c == '?' || c == '$' );
        }
    }
}
