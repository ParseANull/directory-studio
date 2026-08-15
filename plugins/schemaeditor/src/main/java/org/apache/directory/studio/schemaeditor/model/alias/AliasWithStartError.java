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


// ── CLASS: AliasWithStartError — Han Shoots Before Greedo Even Reaches ───────
// Greedo opened with an illegal move — his very first gesture was wrong. Han
// didn't wait for the rest of the interaction; he fired at the first character.
// This class represents an alias that went wrong on character zero: something
// appeared that can't legally start an alias (a digit, a space, punctuation).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An alias whose very first character is illegal. LDAP alias names must start
 * with a letter (a-z or A-Z); anything else is an error at position zero.
 * The parser creates one of these when the scanner returns an
 * {@code ERROR_ALIAS_START} token.
 * Think of this as Han firing the moment Greedo's hand moved — the very first
 * character was wrong, and we flag it without waiting for the rest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AliasWithStartError extends AbstractAliasWithError
{
    // ── Han Fires On The Opening Move ────────────────────────────────────────────
    // Greedo's first character is illegal — Han stores it immediately. The alias
    // text and the offending start character go straight into AbstractAliasWithError.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an alias-with-error for a token whose first character is not a
     * valid alias-start character (i.e. not a letter). The {@code errorChar} is
     * that first illegal character.
     *
     * @param alias      the full alias text (starting with the illegal character)
     * @param errorChar  the illegal first character of the alias
     */
    public AliasWithStartError( String alias, char errorChar )
    {
        super( alias, errorChar );
    }
}
