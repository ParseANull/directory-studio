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


// ── CLASS: AliasWithError — Han Solo Shoots First ────────────────────────────
// Han doesn't wait for the threat to fully materialise before responding. When
// he spots trouble (a bad character in the alias stream), he flags it
// immediately, pinning down the exact offending character rather than letting
// it silently corrupt the rest of the parse. This interface extends Alias with
// the ability to expose which character caused the problem.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Extends {@link Alias} with an error indicator — the specific character that
 * made this alias invalid. Used by the UI to highlight exactly where the user
 * made a typo in the aliases field.
 * Think of this as Han shooting first: we don't let the bad character slip by;
 * we capture it immediately so the UI can point right at the problem.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface AliasWithError extends Alias
{
    // ── Han Points At The Exact Threat ───────────────────────────────────────────
    // Han doesn't just say "trouble" — he points his blaster at the specific
    // target. Here we return the exact illegal character so the UI can mark it in
    // red and tell the user precisely what went wrong.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the specific character that made this alias invalid. The UI uses
     * this to render the error marker at exactly the right position in the text.
     * Could be an illegal start character (e.g. a digit where a letter is required)
     * or an illegal interior character (e.g. a space inside an alias).
     *
     * @return  the character that triggered the parse error
     */
    char getErrorChar();
}
