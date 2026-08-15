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


// ── CLASS: PropertyDifference — MACE'S FORMAL CHARGE SHEET ──────────────────
// Before Mace Windu confronts Palpatine, the Jedi Council prepares a formal
// charge sheet — each line names what Palpatine was before (old value) and
// what he has become (new value), with a clear verdict (the difference type).
// This interface is that charge sheet: every specific property diff (alias,
// syntax, ordering, etc.) must be able to state its before-and-after values.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Contract for any single-property schema difference — carries the old value, the new value,
 * and (via {@link Difference}) the source element, destination element, and change type.
 * Every concrete diff class ({@link AliasDifference}, {@link SyntaxDifference}, etc.)
 * implements this so callers can uniformly ask "what was it before, what is it now?"
 * Think of this as Mace's charge sheet template: every charge must answer two questions —
 * what was the accused before, and what has it become?
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface PropertyDifference extends Difference
{
    // ── Reading What The Accused Was Before ───────────────────────────────────────
    // Mace reads the first column of the charge: "Prior to the incident,
    // Chancellor Palpatine was..."
    // This method returns the value the property held in the original schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the property's value before the change — the "what it used to be" half of the diff.
     * Callers use this to display the before-state in the UI or generate a diff report.
     * May be {@code null} if the property was added (it had no prior value).
     *
     * @return  the old property value, or {@code null} if the property was newly introduced
     */
    Object getOldValue();


    // ── Recording What The Accused Was Before ────────────────────────────────────
    // The Jedi archivist fills in the first column: "Old value confirmed."
    // We store the before-state so it can be presented alongside the new value.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the property's value before the change.
     * Usually called by {@code DifferenceEngine} or a concrete diff constructor
     * while populating the charge record.
     *
     * @param oldValue  the original value of the property; {@code null} is valid for added properties
     */
    void setOldValue( Object oldValue );


    // ── Reading What The Accused Has Become ───────────────────────────────────────
    // Mace reads the second column of the charge: "And now it has become..."
    // This method returns the value the property holds in the updated schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the property's value after the change — the "what it is now" half of the diff.
     * Callers pair this with {@link #getOldValue()} to show a before/after comparison.
     * May be {@code null} if the property was removed.
     *
     * @return  the new property value, or {@code null} if the property was deleted
     */
    Object getNewValue();


    // ── Recording What The Accused Has Become ────────────────────────────────────
    // The Jedi archivist fills in the second column: "New value confirmed."
    // We store the after-state so the full charge is complete.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the property's value after the change.
     * Usually called by {@code DifferenceEngine} or a concrete diff constructor
     * to complete the charge record.
     *
     * @param newValue  the updated value of the property; {@code null} is valid for removed properties
     */
    void setNewValue( Object newValue );
}
