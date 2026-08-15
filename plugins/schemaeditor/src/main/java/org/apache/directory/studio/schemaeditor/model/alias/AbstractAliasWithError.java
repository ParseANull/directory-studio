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


// ── CLASS: AbstractAliasWithError — Han Solo Shoots First ─────────────────────
// In the Mos Eisley cantina, Han doesn't wait for Greedo to fire — he identifies
// the exact threat (Greedo's blaster, aimed right at him) and acts on it
// immediately. This class pairs the alias text with the precise character that
// caused the problem. The alias string tells you what was parsed; the errorChar
// tells you exactly where Han fired.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base implementation for aliases that contain an error, combining the alias
 * text with the specific character that triggered the parse failure. Concrete
 * subclasses ({@link AliasWithStartError}, {@link AliasWithPartError}) extend
 * this to distinguish where in the alias the error occurred.
 * Think of this as Han Solo's targeting reticle: we store both what was
 * attempted (the alias) and exactly what went wrong (the error character).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractAliasWithError extends AbstractAlias implements AliasWithError
{
    /** The error character */
    private char errorChar;


    // ── Han Locks On The Specific Threat Character ───────────────────────────────
    // Han identifies both the droid's name (alias) and the precise character
    // in the cantina that drew his blaster (errorChar). Both are stored so the UI
    // can later highlight exactly where in the alias string the problem lives.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores both the full alias text and the specific character that made it
     * invalid. Subclasses chain up to this constructor — there's no extra logic,
     * just field assignment and delegation to AbstractAlias.
     *
     * @param alias      the full alias string including the invalid character
     * @param errorChar  the exact character that caused the parse error
     */
    public AbstractAliasWithError( String alias, char errorChar )
    {
        super( alias );
        this.errorChar = errorChar;
    }


    // ── Han Points His Blaster At The Exact Problem ──────────────────────────────
    // "It's that guy, right there." Han doesn't generalise — he gives you the
    // specific character, at the specific position, that started the trouble.
    // The UI uses this to paint the red squiggle in exactly the right place.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the character that made this alias invalid. The UI uses this to
     * underline or highlight the specific problem character in the aliases text
     * field, rather than just flagging the whole alias as broken.
     *
     * @return  the illegal character that triggered the parse error
     */
    public char getErrorChar()
    {
        return errorChar;
    }
}
