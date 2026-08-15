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
package org.apache.directory.studio.schemaeditor.model.difference;


// ── CLASS: DifferenceType — Mace Windu's Four Possible Verdicts ──────────────
// After reviewing all the evidence, Mace Windu can render exactly four verdicts:
// the accused is innocent (IDENTICAL), newly arrived (ADDED), changed beyond
// recognition (MODIFIED), or simply gone (REMOVED).  This enum is that verdict
// slip — every Difference stamps one of these four on its dossier.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The four possible outcomes when we compare two schema objects: they're the
 * same, one was added, one was changed, or one was removed.
 * Every {@link Difference} carries one of these as its verdict, and the UI uses
 * it to decide which icon and colour to show next to a changed schema element.
 * Think of it as Mace Windu's four possible rulings after examining the evidence.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum DifferenceType
{
    /** No change detected — the source and destination are functionally the same. */
    IDENTICAL,

    /** The element exists in the destination but not in the source — it was created. */
    ADDED,

    /** The element exists in both but one or more properties differ. */
    MODIFIED,

    /** The element existed in the source but is gone from the destination. */
    REMOVED
}
