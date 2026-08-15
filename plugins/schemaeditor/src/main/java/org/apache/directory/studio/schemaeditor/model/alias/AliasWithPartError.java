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


// ── CLASS: AliasWithPartError — Han Spots Trouble Mid-Draw ───────────────────
// Han started holstering his blaster — the first character was fine — but then
// Greedo did something suspicious halfway through the interaction (a bad
// interior character appeared after a valid start). Han's hand goes back to
// his blaster. The alias started well but went wrong somewhere in the middle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An alias whose first character is valid but which contains an illegal
 * character somewhere in the interior. The parser creates one of these when the
 * scanner returns an {@code ERROR_ALIAS_PART} token — the alias starts with a
 * letter, but then something illegal appears (a space, a punctuation mark, etc.).
 * Think of this as Han catching trouble mid-interaction: the opening was fine,
 * but the middle went wrong, and we flag it precisely.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AliasWithPartError extends AbstractAliasWithError
{
    // ── Han Locks Down The Mid-Draw Trouble ──────────────────────────────────────
    // Han records both what was said (the alias including the bad character) and
    // exactly which character broke the rules (the last character of the
    // ERROR_ALIAS_PART token). All logic lives in AbstractAliasWithError.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an alias-with-error for a token that had a valid start character
     * but an illegal character somewhere inside. The {@code errorChar} is the
     * specific illegal interior character the scanner flagged.
     *
     * @param alias      the full alias text including the illegal character
     * @param errorChar  the illegal character found in the interior of the alias
     */
    public AliasWithPartError( String alias, char errorChar )
    {
        super( alias, errorChar );
    }
}
