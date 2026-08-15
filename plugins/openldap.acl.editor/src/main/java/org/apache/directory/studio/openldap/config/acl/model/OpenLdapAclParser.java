/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.model;


import java.io.StringReader;
import java.text.ParseException;

import antlr.CharBuffer;
import antlr.LexerSharedInputState;
import antlr.RecognitionException;
import antlr.TokenStreamException;


// ── CLASS: OpenLdapAclParser — C-3PO DECODING THE JAWA DIALECT OF ACL TEXT ──
// C-3PO fluent in over six million forms of communication — including the
// dense, terse ACL syntax that OpenLDAP uses. He reads the raw text one
// character at a time, recognises tokens (access, to, by, dn, ssf, etc.),
// assembles them into structured clauses, and hands back a fully populated
// AclItem that the rest of the codebase can work with. This class wraps the
// ANTLR-generated lexer and parser pair into a single thread-safe, reusable
// entry point. The heavy lifting is in AntlrAclLexer and AntlrAclParser;
// this class just wires them up and exposes one clean method.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A reusable, thread-safe wrapper around the ANTLR-generated
 * {@link AntlrAclParser} and {@link AntlrAclLexer}. Call {@link #parse(String)}
 * to convert a raw ACL string into a fully populated {@link AclItem} model.
 *
 * <p>Grammar summary:</p>
 * <pre>
 * parse                ::= SP* aclItem SP* EOF
 * aclItem              ::= ( ID_access SP+ )? ID_to ( SP+ (what_star | what_dn | what_attrs | what_filter | ( ID_by SP+ who ) ) )+
 * what_star            ::= STAR
 * what_dn              ::= ID_dn ( DOT what_dn_type )? SP+ EQUAL SP+ DOUBLE_QUOTED_STRING
 * what_dn_type         ::= ID_regex | ID_base | ID_exact | ID_one | ID_subtree | ID_children
 * what_attrs           ::= ( ID_attrs | ID_attr ) SP* EQUAL SP* ATTR_IDENT ( SEP ATTR_IDENT )*
 *                              ( SP* VAL ( SLASH MATCHING_RULE )? ( DOT what_attrs_style)? ) SP* EQUAL SP* DOUBLE_QUOTED_STRING )?
 * what_attrs_attr_ident::= ATTR_IDENT
 * what_attrs_style     ::= EXACT | BASE | BASE_OBJECT | REGEX | ONE | ONE_LEVEL | SUB | SUB_TREE | CHILDREN
 * what_filter          ::= ID_filter SP* EQUAL SP* FILTER
 * who                  ::= ( who_star | who_anonymous | who_users | who_self | who_dn | who_dnattr | who_group | who_ssf |
 *                              who_transport_ssf | who_tls_ssf | who_sasl_ssf ) ( SP+ who_access_level )? ( SP+  who_control )?
 * who_anonymous        ::= ID_anonymous
 * who_users            ::= ID_users
 * who_self             ::= ID_self
 * who_star             ::= STAR
 * who_dnattr           ::= ID_dnattr SP* EQUAL SP* ATTR_IDENT
 * who_group            ::= ID_group ( SLASH ATTR_IDENT ( SLASH ATTR_IDENT )? )? ( DOT who_group_type )?
 *                              EQUAL DOUBLE_QUOTED_STRING
 * who_group_type       ::= ID_exact | ID_expand
 * who_dn               ::= ID_dn ( DOT who_dn_type ( SEP who_dn_modifier )? )? SP* EQUAL SP* DOUBLE_QUOTED_STRING
 * who_dn_type          ::= ID_regex | ID_base | ID_exact | ID_one | ID_subtree | ID_children |
 *                              ID_level OPEN_CURLY token:INTEGER CLOSE_CURLY
 * who_dn_modifier      ::= ID_expand
 * who_access_level     ::= ID_self SP+ ( who_access_level_level | who_access_level_priv )? |
 *                              ( who_access_level_level | who_access_level_priv )?
 * who_access_level_level           ::= ID_manage | ID_write | ID_read | ID_search | ID_compare | ID_auth | ID_disclose | ID_none
 * who_access_level_priv            ::= who_access_level_priv_modifier ( who_access_level_priv_priv )+
 * who_access_level_priv_modifier   ::= EQUAL | PLUS | MINUS
 * who_access_level_priv_priv       ::= ID_m | ID_w | ID_r | ID_s | ID_c | ID_x
 * who_control          ::= ID_stop | ID_continue | ID_break
 * who_ssf              ::= strength:SSF
 * who_transport_ssf    ::= TRANSPORT_SSF
 * who_tls_ssf          ::= strength:TLS_SSF
 * who_sasl_ssf         ::= strength:SASL_SSF
 * </pre>
 *
 * Think of this class as C-3PO's universal translation service for ACL text —
 * hand him a raw string, he returns a structured {@link AclItem}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclParser
{
    /** the antlr generated parser being wrapped */
    private AntlrAclParser parser;

    /** the antlr generated lexer being wrapped */
    private AntlrAclLexer lexer;


    // ── C-3PO Initialises His Translation Circuits ────────────────────────────
    // When C-3PO boots up he pre-loads the grammar and sets up the translation
    // pipeline. We create the lexer and parser once (with an empty input) and
    // then reuse them across calls by resetting the input state each time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ACL parser, initialising the ANTLR lexer and parser with an
     * empty input. We create both objects once and reuse them across {@link #parse}
     * calls — creating an ANTLR parser/lexer pair on every call would be wasteful.
     * Thread safety is guaranteed by synchronising {@link #parse(String)}.
     */
    public OpenLdapAclParser()
    {
        this.lexer = new AntlrAclLexer( new StringReader( "" ) );
        this.parser = new AntlrAclParser( lexer );
    }


    // ── C-3PO Translates a Raw ACL String Into Structured Data ────────────────
    // Cassian hands C-3PO a page of stolen Imperial code — the raw ACL text.
    // C-3PO scans each character (lexer), recognises the tokens (parser), and
    // assembles the full AclItem model. Because the lexer and parser are shared
    // (we only have one pair), we synchronise this method to prevent two threads
    // from corrupting each other's input state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses a raw OpenLDAP ACL string and returns a populated {@link AclItem}
     * model. We reset the lexer and parser state before each parse so this
     * instance is fully reusable. The method is {@code synchronized} because
     * the underlying ANTLR lexer and parser are stateful and not thread-safe.
     *
     * <p>For example — C-3PO translating a stolen ACL line:</p>
     * <pre>
     *   OpenLdapAclParser p = new OpenLdapAclParser();
     *   AclItem item = p.parse("access to * by users read by * none");
     *   item.getWhatClause().toString(); // → "*"
     *   item.getWhoClauses().size();     // → 2
     * </pre>
     *
     * @param s  The raw ACL string to parse.
     * @return   The fully populated {@link AclItem}; never {@code null} on success.
     * @throws ParseException  if the ACL text is syntactically invalid — includes
     *                         the error message and column position from ANTLR.
     */
    public synchronized AclItem parse( String s ) throws ParseException
    {
        try
        {
            LexerSharedInputState state = new LexerSharedInputState( new CharBuffer( new StringReader( s ) ) );
            this.lexer.setInputState( state );
            this.parser.getInputState().reset();

            parser.parse();

            return parser.getAclItem();
        }
        catch ( TokenStreamException e )
        {
            throw new ParseException( "Unable to read ACL: " + e.getMessage(), -1 );
        }
        catch ( RecognitionException e )
        {
            throw new ParseException( "Unable to read ACL: " + e.getMessage() + " - [Line:" + e.getLine()
                + " - Column:" + e.getColumn() + "]", e.getColumn() );
        }
    }
}
