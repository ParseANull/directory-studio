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


// ── CLASS: SuperiorOCDifference — PALPATINE DROPS THE CHANCELLOR MASK ────────
// The moment Mace Windu presses his lightsaber to Palpatine's throat, Palpatine
// drops all pretense of the "Chancellor" persona — the inherited class above him
// ("elected official," "servant of the Republic") is gone, replaced by Sith Lord.
// This class records the schema-level equivalent: a superior object class (a parent
// in the object class hierarchy) was added to or removed from an object class.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to one superior (parent) object class in an object class definition.
 * In LDAP, object classes can inherit from one or more superior object classes,
 * picking up their mandatory and optional attributes along the way.
 * Think of this as Palpatine dropping the "Chancellor" inherited role — the parent
 * class above him has changed, and Mace is the one who caught it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SuperiorOCDifference extends AbstractPropertyDifference
{
    // ── Mace Names The New Parentage With A Classification ───────────────────────
    // Mace formally states which superior was added, removed, or swapped —
    // the evidence is specific and labeled.
    // We use this constructor when the diff engine already knows the verdict.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SuperiorOCDifference with an explicit difference type.
     * Use this when the engine already knows whether a superior object class
     * was introduced, dropped, or replaced in the hierarchy.
     *
     * <p>For example — Mace files the charge with a clear verdict:</p>
     * <pre>
     *   SuperiorOCDifference diff =
     *       new SuperiorOCDifference("person", null, DifferenceType.REMOVED);
     *   // "The 'person' parent class was stripped — that's the mask coming off."
     * </pre>
     *
     * @param source       the original superior object class name or OID (before the change)
     * @param destination  the updated superior object class name or OID (after the change)
     * @param type         ADDED, REMOVED, or MODIFIED — how the superior changed
     */
    public SuperiorOCDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Senses The Lineage Shift Without A Label ────────────────────────────
    // The Force tells Mace the ancestry has changed — the exact nature
    // defaults to MODIFIED until deeper analysis provides clarity.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SuperiorOCDifference using the default difference type (MODIFIED).
     * Use this when the parent object class has clearly changed but the engine
     * isn't yet committing to ADDED or REMOVED.
     *
     * @param source       the original superior object class (before the change)
     * @param destination  the updated superior object class (after the change)
     */
    public SuperiorOCDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
