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


// ── CLASS: DefaultAlias — C-3PO Reads A Clean, Valid Droid Tag ───────────────
// After all the parsing work, C-3PO finds a droid whose tag is perfectly
// well-formed — letters and digits in exactly the right order, no garbled
// bits. He stores the name as-is and hands it back whenever anyone asks.
// This is the happy-path alias: clean text, no errors.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A fully valid, error-free alias parsed from the aliases string. This is the
 * normal case — the scanner found a properly-formed alias token and we wrap it
 * here. No error character, no partial parse; just the clean alias text.
 * Think of this as C-3PO successfully reading a perfectly legible droid tag:
 * "R2-D2 — name confirmed, no anomalies."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DefaultAlias extends AbstractAlias
{
    // ── C-3PO Stamps The Valid Droid Tag Into His Memory ─────────────────────────
    // The scanner handed C-3PO a clean token. He stores it without any error
    // annotation — this one is fine, move on.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a valid alias wrapping the given string. Called by the parser when
     * a clean, well-formed alias token is found in the input. All behaviour is
     * inherited from {@link AbstractAlias}.
     *
     * @param alias  the valid alias text (e.g. "cn", "commonName", "givenName")
     */
    public DefaultAlias( String alias )
    {
        super( alias );
    }
}
