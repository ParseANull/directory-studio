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
package org.apache.directory.studio.schemaeditor.model.alias;


import java.util.ArrayList;
import java.util.List;


// ── CLASS: AliasesStringParser — C-3PO Translating The Jawa Dialect ──────────
// C-3PO sits beside R2-D2 in the Jawa sandcrawler, listening to the Jawas'
// rapid-fire shorthand. He hands each sound off to his scanning module
// (AliasesStringScanner), gets back labelled tokens, and assembles them into
// a list of properly-categorised Alias objects that the rest of the crew can
// actually use. Clean word? DefaultAlias. Bad first character? AliasWithStartError.
// Bad interior character? AliasWithPartError. C-3PO does not skip mistakes —
// he flags them precisely so they can be shown to the user in the editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Parses a comma-separated aliases string (e.g. {@code "cn, commonName"}) into
 * a list of {@link Alias} objects. Valid aliases become {@link DefaultAlias};
 * tokens with illegal start characters become {@link AliasWithStartError};
 * tokens with illegal interior characters become {@link AliasWithPartError}.
 * Call {@link #parse(String)} to populate, then {@link #getAliases()} to retrieve.
 * Think of this class as C-3PO's translation session: scanner feeds tokens,
 * parser assembles the meaning, result list is the crew's plain-English briefing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AliasesStringParser
{
    /** The scanner */
    private AliasesStringScanner scanner;

    /** The parsed aliases */
    private List<Alias> aliases;


    // ── C-3PO Boots Up His Translation Module ────────────────────────────────────
    // C-3PO initialises both his scanning sub-unit (AliasesStringScanner) and his
    // result list. He's ready to start listening the moment parse() is called.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new parser with a fresh scanner and an empty aliases list. Reusable —
     * each call to {@link #parse(String)} clears and repopulates the list, so you
     * can use one instance to parse multiple strings in sequence.
     */
    public AliasesStringParser()
    {
        this.scanner = new AliasesStringScanner();
        this.aliases = new ArrayList<Alias>();
    }


    // ── C-3PO Hands Over The Decoded Alias List ──────────────────────────────────
    // After the translation session (parse()), C-3PO hands the crew the full list
    // of decoded aliases — valid ones and error-tagged ones mixed together in input
    // order. The UI iterates this list to render names and highlight errors.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of {@link Alias} objects produced by the most recent call to
     * {@link #parse(String)}. The list preserves input order. Valid aliases are
     * {@link DefaultAlias}; aliases with errors are {@link AliasWithStartError} or
     * {@link AliasWithPartError}. Returns an empty list if parse() hasn't been called
     * yet or if the input was empty.
     *
     * @return  the parsed alias list; never null
     */
    public List<Alias> getAliases()
    {
        return aliases;
    }


    // ── C-3PO Translates The Full Jawa Broadcast ─────────────────────────────────
    // C-3PO resets his scanner to the new transmission, then loops through every
    // token until the broadcast ends (EOF). For each token he decides what kind of
    // Alias object to create: clean word, bad-start error, or bad-interior error.
    // Error tokens can be followed by a trailing substring token that C-3PO must
    // consume and append before moving on.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the given comma-separated aliases string and populates the internal
     * aliases list. Clears any previous parse result first, so this method is safe
     * to call multiple times on the same instance.
     * Whitespace and comma tokens are consumed and discarded; only alias-carrying
     * tokens produce entries in the list.
     *
     * <p>For example — C-3PO translates the broadcast:</p>
     * <pre>
     *   parse("cn, commonName, 1bad")
     *   → aliases = [DefaultAlias("cn"), DefaultAlias("commonName"),
     *                AliasWithStartError("1bad", '1')]
     * </pre>
     *
     * @param str  the comma-separated aliases string to parse; must not be null
     */
    public void parse( String str )
    {
        // reset state
        aliases.clear();
        scanner.reset( str );

        // handle error tokens before filter
        AliasesStringToken token = scanner.nextToken();

        // loop till aliases end or EOF
        do
        {
            switch ( token.getType() )
            {
                case AliasesStringToken.ALIAS:
                {
                    aliases.add( new DefaultAlias( token.getValue() ) );
                    break;
                }
                case AliasesStringToken.ERROR_ALIAS_START:
                {
                    String previousTokenValue = token.getValue();

                    token = scanner.nextToken();
                    if ( token.getType() == AliasesStringToken.ERROR_ALIAS_SUBSTRING )
                    {
                        aliases.add( new AliasWithStartError( previousTokenValue + token.getValue(), previousTokenValue
                            .charAt( 0 ) ) );
                        break;
                    }
                    else
                    {
                        aliases.add( new AliasWithStartError( previousTokenValue, previousTokenValue.charAt( 0 ) ) );
                        continue;
                    }
                }
                case AliasesStringToken.ERROR_ALIAS_PART:
                {
                    String previousTokenValue = token.getValue();

                    token = scanner.nextToken();
                    if ( token.getType() == AliasesStringToken.ERROR_ALIAS_SUBSTRING )
                    {
                        aliases.add( new AliasWithPartError( previousTokenValue + token.getValue(), previousTokenValue
                            .charAt( previousTokenValue.length() - 1 ) ) );
                        break;
                    }
                    else
                    {
                        aliases.add( new AliasWithPartError( previousTokenValue, previousTokenValue
                            .charAt( previousTokenValue.length() - 1 ) ) );
                        continue;
                    }
                }
                default:
                {
                    break;
                }
            }

            // next token
            token = scanner.nextToken();
        }
        while ( token.getType() != AliasesStringToken.EOF );
    }
}
