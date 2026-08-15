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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.schemaeditor.model.Schema;


// ── CLASS: DifferenceEngine — Yoda Lifts The X-Wing From The Swamp ───────────
// On Dagobah, Luke stands before his half-sunken X-wing and says it's too big,
// too heavy, too impossible to lift.  Yoda closes his eyes, reaches out with the
// Force, and the entire fighter rises from the swamp — every component accounted
// for, placed precisely on solid ground.  DifferenceEngine is that moment: given
// two lists of Schema objects (any size), it lifts every attribute type and object
// class out of the swamp of raw data and surfaces exactly what changed between
// them, no matter how deeply nested the difference is.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The core comparison engine: given two lists of {@link Schema} objects, it
 * computes a structured list of {@link SchemaDifference} records — one per
 * schema — each containing the exact attribute types and object classes that
 * were added, removed, or modified, plus the specific property-level changes
 * within each element.
 * Think of it as Yoda lifting Luke's X-wing: no matter how large or tangled
 * the schema, every change surfaces precisely and in the right place.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DifferenceEngine
{
    // ── Yoda Lifts Two Full Schema Lists Out Of The Swamp ─────────────────────
    // Luke hands Yoda two muddy swamp snapshots (the "before" list and the
    // "after" list).  Yoda index-maps every schema by lowercase name, walks
    // both lists systematically — schemas only in L1 are REMOVED, schemas only
    // in L2 are ADDED, schemas in both get a deep-dive comparison.
    // "Do, or do not.  There is no try."  Every schema gets a verdict.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two lists of schemas and returns one {@link SchemaDifference} per
     * schema encountered across both lists.
     * Schemas only in {@code l1} are REMOVED; schemas only in {@code l2} are
     * ADDED; schemas in both get a full property-by-property deep comparison.
     *
     * <p>For example — Yoda compares the before and after swamp contents:</p>
     * <pre>
     *   List&lt;SchemaDifference&gt; diffs = DifferenceEngine.getDifferences( before, after );
     *   // Each entry says "schema X was ADDED / REMOVED / MODIFIED / IDENTICAL"
     *   // plus nested AT and OC differences for anything that changed.
     * </pre>
     *
     * @param l1  the "before" list of schemas (may be empty, not null)
     * @param l2  the "after" list of schemas (may be empty, not null)
     * @return    a list of {@link SchemaDifference} objects covering every schema in both lists
     */
    public static List<SchemaDifference> getDifferences( List<Schema> l1, List<Schema> l2 )
    {
        List<SchemaDifference> differences = new ArrayList<SchemaDifference>();

        // Building Maps for schemas
        Map<String, Schema> mapL1 = new HashMap<String, Schema>();
        for ( Schema schema : l1 )
        {
            mapL1.put( Strings.toLowerCase( schema.getSchemaName() ), schema );
        }
        Map<String, Schema> mapL2 = new HashMap<String, Schema>();
        for ( Schema schema : l2 )
        {
            mapL2.put( Strings.toLowerCase( schema.getSchemaName() ), schema );
        }

        // Looping on schemas from the first list
        for ( Schema schemaFromL1 : l1 )
        {
            Schema schemaFromL2 = mapL2.get( Strings.toLowerCase( schemaFromL1.getSchemaName() ) );
            if ( schemaFromL2 == null )
            {
                SchemaDifference schemaDifference = new SchemaDifference( schemaFromL1, null, DifferenceType.REMOVED );
                differences.add( schemaDifference );

                // Adding attribute types
                for ( AttributeType at : schemaFromL1.getAttributeTypes() )
                {
                    schemaDifference.addAttributeTypeDifference( new AttributeTypeDifference( null, at,
                        DifferenceType.REMOVED ) );
                }

                // Adding object classes
                for ( ObjectClass oc : schemaFromL1.getObjectClasses() )
                {
                    schemaDifference.addObjectClassDifference( new ObjectClassDifference( null, oc,
                        DifferenceType.REMOVED ) );
                }
            }
            else
            {
                SchemaDifference schemaDifference = new SchemaDifference( schemaFromL1, schemaFromL2,
                    DifferenceType.IDENTICAL );
                differences.add( schemaDifference );

                // Building Maps for attribute types
                Map<String, AttributeType> atMapL1 = new HashMap<String, AttributeType>();
                for ( AttributeType at : schemaFromL1.getAttributeTypes() )
                {
                    atMapL1.put( at.getOid(), at );
                }
                Map<String, AttributeType> atMapL2 = new HashMap<String, AttributeType>();
                for ( AttributeType at : schemaFromL2.getAttributeTypes() )
                {
                    atMapL2.put( at.getOid(), at );
                }

                // Looping on the attribute types from the Schema from the first list
                for ( AttributeType atFromL1 : schemaFromL1.getAttributeTypes() )
                {
                    AttributeType atFromL2 = atMapL2.get( atFromL1.getOid() );
                    if ( atFromL2 == null )
                    {
                        AttributeTypeDifference attributeTypeDifference = new AttributeTypeDifference( atFromL1, null,
                            DifferenceType.REMOVED );
                        schemaDifference.addAttributeTypeDifference( attributeTypeDifference );
                        schemaDifference.setType( DifferenceType.MODIFIED );
                    }
                    else
                    {
                        AttributeTypeDifference attributeTypeDifference = new AttributeTypeDifference( atFromL1,
                            atFromL2, DifferenceType.IDENTICAL );
                        schemaDifference.addAttributeTypeDifference( attributeTypeDifference );

                        List<PropertyDifference> atDifferences = getDifferences( atFromL1, atFromL2 );
                        if ( atDifferences.size() > 0 )
                        {
                            attributeTypeDifference.setType( DifferenceType.MODIFIED );
                            attributeTypeDifference.addDifferences( atDifferences );
                            schemaDifference.setType( DifferenceType.MODIFIED );
                        }
                    }
                }

                // Looping on the attribute types from the Schema from the second list
                for ( AttributeType atFromL2 : schemaFromL2.getAttributeTypes() )
                {
                    AttributeType atFromL1 = atMapL1.get( atFromL2.getOid() );
                    if ( atFromL1 == null )
                    {
                        AttributeTypeDifference attributeTypeDifference = new AttributeTypeDifference( null, atFromL2,
                            DifferenceType.ADDED );
                        schemaDifference.addAttributeTypeDifference( attributeTypeDifference );
                        schemaDifference.setType( DifferenceType.MODIFIED );
                    }
                    // If atFromL1 exists, then it has already been processed when looping on the first list.
                }

                // Building Maps for object classes
                Map<String, ObjectClass> ocMapL1 = new HashMap<String, ObjectClass>();
                for ( ObjectClass oc : schemaFromL1.getObjectClasses() )
                {
                    ocMapL1.put( oc.getOid(), oc );
                }
                Map<String, ObjectClass> ocMapL2 = new HashMap<String, ObjectClass>();
                for ( ObjectClass oc : schemaFromL2.getObjectClasses() )
                {
                    ocMapL2.put( oc.getOid(), oc );
                }

                // Looping on the object classes from the Schema from the first list
                for ( ObjectClass ocFromL1 : schemaFromL1.getObjectClasses() )
                {
                    ObjectClass ocFromL2 = ocMapL2.get( ocFromL1.getOid() );
                    if ( ocFromL2 == null )
                    {
                        ObjectClassDifference objectClassDifference = new ObjectClassDifference( ocFromL1, null,
                            DifferenceType.REMOVED );
                        schemaDifference.addObjectClassDifference( objectClassDifference );
                        schemaDifference.setType( DifferenceType.MODIFIED );
                    }
                    else
                    {
                        ObjectClassDifference objectClassDifference = new ObjectClassDifference( ocFromL1, ocFromL2,
                            DifferenceType.IDENTICAL );
                        schemaDifference.addObjectClassDifference( objectClassDifference );

                        List<PropertyDifference> ocDifferences = getDifferences( ocFromL1, ocFromL2 );
                        if ( ocDifferences.size() > 0 )
                        {
                            objectClassDifference.setType( DifferenceType.MODIFIED );
                            objectClassDifference.addDifferences( ocDifferences );
                            schemaDifference.setType( DifferenceType.MODIFIED );
                        }
                    }
                }

                // Looping on the object classes from the Schema from the second list
                for ( ObjectClass ocFromL2 : schemaFromL2.getObjectClasses() )
                {
                    ObjectClass ocFromL1 = ocMapL1.get( ocFromL2.getOid() );
                    if ( ocFromL1 == null )
                    {
                        ObjectClassDifference objectClassDifference = new ObjectClassDifference( null, ocFromL2,
                            DifferenceType.ADDED );
                        schemaDifference.addObjectClassDifference( objectClassDifference );
                        schemaDifference.setType( DifferenceType.MODIFIED );
                    }
                    // If ocFromL1 exists, then it has already been processed when looping on the first list.
                }
            }
        }

        // Looping on schemas from the second list
        for ( Schema schemaFromL2 : l2 )
        {
            Schema schemaFromL1 = mapL1.get( Strings.toLowerCase( schemaFromL2.getSchemaName() ) );
            if ( schemaFromL1 == null )
            {
                SchemaDifference schemaDifference = new SchemaDifference( null, schemaFromL2, DifferenceType.ADDED );
                differences.add( schemaDifference );

                // Adding attribute types
                for ( AttributeType at : schemaFromL2.getAttributeTypes() )
                {
                    schemaDifference.addAttributeTypeDifference( new AttributeTypeDifference( null, at,
                        DifferenceType.ADDED ) );
                }

                // Adding object classes
                for ( ObjectClass oc : schemaFromL2.getObjectClasses() )
                {
                    schemaDifference.addObjectClassDifference( new ObjectClassDifference( null, oc,
                        DifferenceType.ADDED ) );
                }
            }
        }

        return differences;
    }


    // ── Yoda Compares Two Object Classes Property By Property ─────────────────
    // With two object classes hovering in front of him, Yoda checks every
    // property in turn: aliases, description, obsolete flag, class type, superior
    // classes, mandatory attribute types, optional attribute types.  For each one
    // that differs, he produces a PropertyDifference and adds it to the list.
    // "Judge me by my size, do you?"  No — he judges by every field.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link ObjectClass} objects property by property and returns a
     * list of {@link PropertyDifference} objects for every field that changed.
     * Returns an empty list if the two object classes are functionally identical.
     * The result is added to the containing {@link ObjectClassDifference} by the
     * calling code.
     *
     * <p>For example — Yoda's object-class inspection:</p>
     * <pre>
     *   List&lt;PropertyDifference&gt; diffs = DifferenceEngine.getDifferences( oc1, oc2 );
     *   // diffs might contain: [AliasDifference(ADDED,"sn"), DescriptionDifference(MODIFIED)]
     * </pre>
     *
     * @param oc1  the source ObjectClass ("before")
     * @param oc2  the destination ObjectClass ("after")
     * @return     a list of property-level differences; empty if unchanged
     */
    public static List<PropertyDifference> getDifferences( ObjectClass oc1, ObjectClass oc2 )
    {
        List<PropertyDifference> differences = new ArrayList<PropertyDifference>();

        // Aliases
        differences.addAll( getAliasesDifferences( oc1, oc2 ) );

        // Description
        PropertyDifference descriptionDifference = getDescriptionDifference( oc1, oc2 );
        if ( descriptionDifference != null )
        {
            differences.add( descriptionDifference );
        }

        // Obsolete
        PropertyDifference obsoleteDifference = getObsoleteDifference( oc1, oc2 );
        if ( obsoleteDifference != null )
        {
            differences.add( obsoleteDifference );
        }

        // Class type
        PropertyDifference classTypeDifference = getClassTypeDifference( oc1, oc2 );
        if ( classTypeDifference != null )
        {
            differences.add( classTypeDifference );
        }

        // Superior classes
        differences.addAll( getSuperiorClassesDifferences( oc1, oc2 ) );

        // Mandatory attribute types
        differences.addAll( getMandatoryAttributeTypesDifferences( oc1, oc2 ) );

        // Optional attribute types
        differences.addAll( getOptionalAttributeTypesDifferences( oc1, oc2 ) );

        return differences;
    }


    // ── Yoda Compares Two Attribute Types Property By Property ────────────────
    // Yoda hovers two attribute types side-by-side and runs a thorough property
    // scan: aliases, description, obsolete, usage, superior AT, syntax, syntax
    // length, single-value, collective, no-user-modification, equality, ordering,
    // and substring matching rules.  Any property that differs generates a
    // PropertyDifference in the result list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link AttributeType} objects property by property and returns
     * a list of {@link PropertyDifference} objects for every field that changed.
     * Returns an empty list if the two attribute types are functionally identical.
     * This is the most detailed comparison in the engine — attribute types have
     * more properties than any other schema element type.
     *
     * <p>For example — Yoda's attribute-type inspection:</p>
     * <pre>
     *   List&lt;PropertyDifference&gt; diffs = DifferenceEngine.getDifferences( at1, at2 );
     *   // might return [SyntaxDifference(MODIFIED), EqualityDifference(ADDED)]
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a list of property-level differences; empty if unchanged
     */
    public static List<PropertyDifference> getDifferences( AttributeType at1, AttributeType at2 )
    {
        List<PropertyDifference> differences = new ArrayList<PropertyDifference>();

        // Aliases
        differences.addAll( getAliasesDifferences( at1, at2 ) );

        // Description
        PropertyDifference descriptionDifference = getDescriptionDifference( at1, at2 );
        if ( descriptionDifference != null )
        {
            differences.add( descriptionDifference );
        }

        // Obsolete
        PropertyDifference obsoleteDifference = getObsoleteDifference( at1, at2 );
        if ( obsoleteDifference != null )
        {
            differences.add( obsoleteDifference );
        }

        // Usage
        PropertyDifference usageDifference = getUsageDifference( at1, at2 );
        if ( usageDifference != null )
        {
            differences.add( usageDifference );
        }

        // Superior
        PropertyDifference superiorDifference = getSuperiorDifference( at1, at2 );
        if ( superiorDifference != null )
        {
            differences.add( superiorDifference );
        }

        // Syntax
        PropertyDifference syntaxDifference = getSyntaxDifference( at1, at2 );
        if ( syntaxDifference != null )
        {
            differences.add( syntaxDifference );
        }

        // Syntax length
        PropertyDifference syntaxLengthDifference = getSyntaxLengthDifference( at1, at2 );
        if ( syntaxLengthDifference != null )
        {
            differences.add( syntaxLengthDifference );
        }

        // Single value
        PropertyDifference singleValueDifference = getSingleValueDifference( at1, at2 );
        if ( singleValueDifference != null )
        {
            differences.add( singleValueDifference );
        }

        // Collective
        PropertyDifference collectiveDifference = getCollectiveDifference( at1, at2 );
        if ( collectiveDifference != null )
        {
            differences.add( collectiveDifference );
        }

        // No user modification
        PropertyDifference noUserModificationDifference = getNoUserModificationDifference( at1, at2 );
        if ( noUserModificationDifference != null )
        {
            differences.add( noUserModificationDifference );
        }

        // Equality
        PropertyDifference equalityDifference = getEqualityDifference( at1, at2 );
        if ( equalityDifference != null )
        {
            differences.add( equalityDifference );
        }

        // Ordering
        PropertyDifference orderingDifference = getOrderingDifference( at1, at2 );
        if ( orderingDifference != null )
        {
            differences.add( orderingDifference );
        }

        // Substring
        PropertyDifference substringDifference = getSubstringDifference( at1, at2 );
        if ( substringDifference != null )
        {
            differences.add( substringDifference );
        }

        return differences;
    }


    // ── Yoda Scans The Name Lists ─────────────────────────────────────────────
    // Yoda checks every name in the first object's alias list against the second's.
    // Names that appear in so1 but not so2 are REMOVED; names in so2 but not so1
    // are ADDED.  One AliasDifference per name change.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the alias (name) lists of two schema objects and returns one
     * {@link AliasDifference} for each alias that was added or removed.
     * Returns an empty list if both name lists are identical.
     *
     * <p>For example — Yoda scans the name plates:</p>
     * <pre>
     *   // so1 has ["cn"], so2 has ["cn","commonName"]
     *   // result: one AliasDifference(ADDED, newValue="commonName")
     * </pre>
     *
     * @param so1  the source SchemaObject ("before")
     * @param so2  the destination SchemaObject ("after")
     * @return     a list of alias-level differences; empty if name lists match
     */
    private static List<PropertyDifference> getAliasesDifferences( SchemaObject so1, SchemaObject so2 )
    {
        List<PropertyDifference> differences = new ArrayList<PropertyDifference>();

        List<String> so1Names = so1.getNames();
        List<String> so2Names = so2.getNames();

        for ( String name : so1Names )
        {
            if ( !so2Names.contains( name ) )
            {
                PropertyDifference diff = new AliasDifference( so1, so2, DifferenceType.REMOVED );
                diff.setOldValue( name );
                differences.add( diff );
            }
        }

        for ( String name : so2Names )
        {
            if ( !so1Names.contains( name ) )
            {
                PropertyDifference diff = new AliasDifference( so1, so2, DifferenceType.ADDED );
                diff.setNewValue( name );
                differences.add( diff );
            }
        }

        return differences;
    }


    // ── Yoda Reads The Description Plaque ─────────────────────────────────────
    // Yoda checks the description field — null vs. non-null cases generate ADDED
    // or REMOVED; both non-null but different generate MODIFIED.  If both are null
    // or identical, null is returned (no difference).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code DESCRIPTION} fields of two schema objects and returns a
     * {@link DescriptionDifference} if they differ, or {@code null} if they match.
     *
     * <p>For example — Yoda reads the description plaque:</p>
     * <pre>
     *   // so1.description = null, so2.description = "Common name"
     *   // result: DescriptionDifference(ADDED, newValue="Common name")
     * </pre>
     *
     * @param so1  the source SchemaObject ("before")
     * @param so2  the destination SchemaObject ("after")
     * @return     a {@link DescriptionDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getDescriptionDifference( SchemaObject so1, SchemaObject so2 )
    {
        String so1Description = so1.getDescription();
        String so2Description = so2.getDescription();

        if ( ( so1Description == null ) && ( so2Description != null ) )
        {
            PropertyDifference diff = new DescriptionDifference( so1, so2, DifferenceType.ADDED );
            diff.setNewValue( so2Description );
            return diff;
        }
        else if ( ( so1Description != null ) && ( so2Description == null ) )
        {
            PropertyDifference diff = new DescriptionDifference( so1, so2, DifferenceType.REMOVED );
            diff.setOldValue( so1Description );
            return diff;
        }
        else if ( ( so1Description != null ) && ( so2Description != null ) )
        {
            if ( !so1Description.equals( so2Description ) )
            {
                PropertyDifference diff = new DescriptionDifference( so1, so2, DifferenceType.MODIFIED );
                diff.setOldValue( so1Description );
                diff.setNewValue( so2Description );
                return diff;
            }
        }

        return null;
    }


    // ── Yoda Checks The Obsolete Flag ─────────────────────────────────────────
    // The obsolete flag is a boolean; Yoda simply compares them.  If they differ,
    // he notes a MODIFIED ObsoleteDifference with old and new boolean values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code OBSOLETE} flags of two schema objects and returns an
     * {@link ObsoleteDifference} if the flag was toggled, or {@code null} if
     * both are the same.
     *
     * <p>For example — Yoda checks the obsolete label:</p>
     * <pre>
     *   // so1.obsolete = false, so2.obsolete = true
     *   // result: ObsoleteDifference(oldValue=false, newValue=true)
     * </pre>
     *
     * @param so1  the source SchemaObject ("before")
     * @param so2  the destination SchemaObject ("after")
     * @return     an {@link ObsoleteDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getObsoleteDifference( SchemaObject so1, SchemaObject so2 )
    {
        boolean so1Obsolete = so1.isObsolete();
        boolean so2Obsolete = so2.isObsolete();

        if ( so1Obsolete != so2Obsolete )
        {
            PropertyDifference diff = new ObsoleteDifference( so1, so2 );
            diff.setOldValue( so1Obsolete );
            diff.setNewValue( so2Obsolete );
            return diff;
        }

        return null;
    }


    // ── Yoda Checks The Class Type Badge ─────────────────────────────────────
    // The class type badge says STRUCTURAL, AUXILIARY, or ABSTRACT.  If the two
    // object classes carry different badges, Yoda produces a ClassTypeDifference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code objectClass} types of two ObjectClass objects and
     * returns a {@link ClassTypeDifference} if they differ, or {@code null} if
     * they are the same.
     *
     * <p>For example — Yoda checks the class badge:</p>
     * <pre>
     *   // oc1.type = STRUCTURAL, oc2.type = AUXILIARY
     *   // result: ClassTypeDifference(oldValue=STRUCTURAL, newValue=AUXILIARY)
     * </pre>
     *
     * @param oc1  the source ObjectClass ("before")
     * @param oc2  the destination ObjectClass ("after")
     * @return     a {@link ClassTypeDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getClassTypeDifference( ObjectClass oc1, ObjectClass oc2 )
    {
        ObjectClassTypeEnum oc1ClassType = oc1.getType();
        ObjectClassTypeEnum oc2ClassType = oc2.getType();

        if ( oc1ClassType != oc2ClassType )
        {
            PropertyDifference diff = new ClassTypeDifference( oc1, oc2 );
            diff.setOldValue( oc1ClassType );
            diff.setNewValue( oc2ClassType );
            return diff;
        }

        return null;
    }


    // ── Yoda Scans The Superior Class Roster ─────────────────────────────────
    // Yoda checks which object classes each OC inherits from.  Any superior OID
    // in oc1 but not oc2 is REMOVED; any in oc2 but not oc1 is ADDED.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the list of superior object class OIDs between two ObjectClass
     * objects and returns one {@link SuperiorOCDifference} per OID that was added
     * or removed.
     *
     * <p>For example — Yoda scans the inheritance roster:</p>
     * <pre>
     *   // oc1 inherits from ["top"], oc2 from ["top","person"]
     *   // result: one SuperiorOCDifference(ADDED, newValue="person")
     * </pre>
     *
     * @param oc1  the source ObjectClass ("before")
     * @param oc2  the destination ObjectClass ("after")
     * @return     a list of {@link SuperiorOCDifference} objects; empty if unchanged
     */
    private static List<PropertyDifference> getSuperiorClassesDifferences( ObjectClass oc1, ObjectClass oc2 )
    {
        List<PropertyDifference> differences = new ArrayList<PropertyDifference>();

        List<String> oc1Sups = oc1.getSuperiorOids();
        List<String> oc2Sups = oc2.getSuperiorOids();

        for ( String name : oc1Sups )
        {
            if ( !oc2Sups.contains( name ) )
            {
                PropertyDifference diff = new SuperiorOCDifference( oc1, oc2, DifferenceType.REMOVED );
                diff.setOldValue( name );
                differences.add( diff );
            }
        }

        for ( String name : oc2Sups )
        {
            if ( !oc1Sups.contains( name ) )
            {
                PropertyDifference diff = new SuperiorOCDifference( oc1, oc2, DifferenceType.ADDED );
                diff.setNewValue( name );
                differences.add( diff );
            }
        }

        return differences;
    }


    // ── Yoda Scans The Mandatory Attribute Type Roster ────────────────────────
    // Yoda checks the MUST list — attribute types that every entry of this OC
    // must have.  Each OID that appeared or disappeared gets a MandatoryATDifference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the MUST attribute type lists of two ObjectClass objects and
     * returns one {@link MandatoryATDifference} per OID that was added or removed.
     *
     * <p>For example — Yoda checks the required-fields list:</p>
     * <pre>
     *   // oc1.must = ["cn","sn"], oc2.must = ["cn"]
     *   // result: one MandatoryATDifference(REMOVED, oldValue="sn")
     * </pre>
     *
     * @param oc1  the source ObjectClass ("before")
     * @param oc2  the destination ObjectClass ("after")
     * @return     a list of {@link MandatoryATDifference} objects; empty if unchanged
     */
    private static List<PropertyDifference> getMandatoryAttributeTypesDifferences( ObjectClass oc1,
        ObjectClass oc2 )
    {
        List<PropertyDifference> differences = new ArrayList<PropertyDifference>();

        List<String> oc1Musts = oc1.getMustAttributeTypeOids();
        List<String> oc2Musts = oc2.getMustAttributeTypeOids();

        for ( String name : oc1Musts )
        {
            if ( !oc2Musts.contains( name ) )
            {
                PropertyDifference diff = new MandatoryATDifference( oc1, oc2, DifferenceType.REMOVED );
                diff.setOldValue( name );
                differences.add( diff );
            }
        }

        for ( String name : oc2Musts )
        {
            if ( !oc1Musts.contains( name ) )
            {
                PropertyDifference diff = new MandatoryATDifference( oc1, oc2, DifferenceType.ADDED );
                diff.setNewValue( name );
                differences.add( diff );
            }
        }

        return differences;
    }


    // ── Yoda Scans The Optional Attribute Type Roster ─────────────────────────
    // Same pattern as mandatory, but for the MAY list — optional attribute types.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the MAY attribute type lists of two ObjectClass objects and
     * returns one {@link OptionalATDifference} per OID that was added or removed.
     *
     * <p>For example — Yoda checks the optional-fields list:</p>
     * <pre>
     *   // oc1.may = ["mail"], oc2.may = ["mail","telephoneNumber"]
     *   // result: one OptionalATDifference(ADDED, newValue="telephoneNumber")
     * </pre>
     *
     * @param oc1  the source ObjectClass ("before")
     * @param oc2  the destination ObjectClass ("after")
     * @return     a list of {@link OptionalATDifference} objects; empty if unchanged
     */
    private static List<PropertyDifference> getOptionalAttributeTypesDifferences( ObjectClass oc1,
        ObjectClass oc2 )
    {
        List<PropertyDifference> differences = new ArrayList<PropertyDifference>();

        List<String> oc1Mays = oc1.getMayAttributeTypeOids();
        List<String> oc2Mays = oc2.getMayAttributeTypeOids();

        for ( String name : oc1Mays )
        {
            if ( !oc2Mays.contains( name ) )
            {
                PropertyDifference diff = new OptionalATDifference( oc1, oc2, DifferenceType.REMOVED );
                diff.setOldValue( name );
                differences.add( diff );
            }
        }

        for ( String name : oc2Mays )
        {
            if ( !oc1Mays.contains( name ) )
            {
                PropertyDifference diff = new OptionalATDifference( oc1, oc2, DifferenceType.ADDED );
                diff.setNewValue( name );
                differences.add( diff );
            }
        }

        return differences;
    }


    // ── Yoda Checks The Usage Label ───────────────────────────────────────────
    // The USAGE field on an attribute type says who it's for: userApplications,
    // directoryOperation, distributedOperation, or dSAOperation.  A change here
    // means the attribute's purpose was reclassified — Yoda notes it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code USAGE} values of two AttributeType objects and returns
     * a {@link UsageDifference} if they differ, or {@code null} if they match.
     *
     * <p>For example — Yoda checks the usage label:</p>
     * <pre>
     *   // at1.usage = userApplications, at2.usage = directoryOperation
     *   // result: UsageDifference(oldValue=userApplications, newValue=directoryOperation)
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a {@link UsageDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getUsageDifference( AttributeType at1, AttributeType at2 )
    {
        UsageEnum at1Usage = at1.getUsage();
        UsageEnum at2Usage = at2.getUsage();

        if ( at1Usage != at2Usage )
        {
            PropertyDifference diff = new UsageDifference( at1, at2 );
            diff.setOldValue( at1Usage );
            diff.setNewValue( at2Usage );
            return diff;
        }

        return null;
    }


    // ── Yoda Checks The Superior Attribute Type ────────────────────────────────
    // The SUPERIOR field for an attribute type can be absent (null), set, or
    // changed — Yoda handles all three cases and returns null if unchanged.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code SUP} (superior attribute type OID) of two AttributeType
     * objects and returns a {@link SuperiorATDifference} if it changed, or
     * {@code null} if it's the same.
     *
     * <p>For example — Yoda checks the inheritance link:</p>
     * <pre>
     *   // at1.sup = null, at2.sup = "name"
     *   // result: SuperiorATDifference(ADDED, newValue="name")
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a {@link SuperiorATDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getSuperiorDifference( AttributeType at1, AttributeType at2 )
    {
        String at1Superior = at1.getSuperiorOid();
        String at2Superior = at2.getSuperiorOid();

        if ( ( at1Superior == null ) && ( at2Superior != null ) )
        {
            PropertyDifference diff = new SuperiorATDifference( at1, at2, DifferenceType.ADDED );
            diff.setNewValue( at2Superior );
            return diff;
        }
        else if ( ( at1Superior != null ) && ( at2Superior == null ) )
        {
            PropertyDifference diff = new SuperiorATDifference( at1, at2, DifferenceType.REMOVED );
            diff.setOldValue( at1Superior );
            return diff;
        }
        else if ( ( at1Superior != null ) && ( at2Superior != null ) )
        {
            if ( !at1Superior.equals( at2Superior ) )
            {
                PropertyDifference diff = new SuperiorATDifference( at1, at2, DifferenceType.MODIFIED );
                diff.setOldValue( at1Superior );
                diff.setNewValue( at2Superior );
                return diff;
            }
        }

        return null;
    }


    // ── Yoda Checks The Syntax OID ────────────────────────────────────────────
    // The SYNTAX field defines the data format an attribute value must conform to
    // (e.g. Integer, DirectoryString).  Yoda checks if the OID changed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code SYNTAX} OIDs of two AttributeType objects and returns a
     * {@link SyntaxDifference} if they differ, or {@code null} if they match.
     *
     * <p>For example — Yoda inspects the syntax OID:</p>
     * <pre>
     *   // at1.syntax = "1.3.6.1.4.1.1466.115.121.1.26" (IA5String)
     *   // at2.syntax = "1.3.6.1.4.1.1466.115.121.1.15" (DirectoryString)
     *   // result: SyntaxDifference(MODIFIED, old=..26, new=..15)
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a {@link SyntaxDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getSyntaxDifference( AttributeType at1, AttributeType at2 )
    {
        String at1Syntax = at1.getSyntaxOid();
        String at2Syntax = at2.getSyntaxOid();

        if ( ( at1Syntax == null ) && ( at2Syntax != null ) )
        {
            PropertyDifference diff = new SyntaxDifference( at1, at2, DifferenceType.ADDED );
            diff.setNewValue( at2Syntax );
            return diff;
        }
        else if ( ( at1Syntax != null ) && ( at2Syntax == null ) )
        {
            PropertyDifference diff = new SyntaxDifference( at1, at2, DifferenceType.REMOVED );
            diff.setOldValue( at1Syntax );
            return diff;
        }
        else if ( ( at1Syntax != null ) && ( at2Syntax != null ) )
        {
            if ( !at1Syntax.equals( at2Syntax ) )
            {
                PropertyDifference diff = new SyntaxDifference( at1, at2, DifferenceType.MODIFIED );
                diff.setOldValue( at1Syntax );
                diff.setNewValue( at2Syntax );
                return diff;
            }
        }

        return null;
    }


    // ── Yoda Checks The Syntax Length Cap ─────────────────────────────────────
    // The syntax length constrains the maximum value length (e.g. max 256 chars).
    // A zero means "unconstrained"; Yoda treats 0↔non-zero as ADDED/REMOVED.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code SYNTAX} length constraints of two AttributeType objects
     * and returns a {@link SyntaxLengthDifference} if they differ, or {@code null}
     * if they match.
     * A length of 0 means "not specified" — going from 0 to non-zero is ADDED;
     * non-zero to 0 is REMOVED; non-zero to a different non-zero is MODIFIED.
     *
     * <p>For example — Yoda checks the length cap:</p>
     * <pre>
     *   // at1.syntaxLength = 0 (unconstrained), at2.syntaxLength = 256
     *   // result: SyntaxLengthDifference(ADDED, newValue=256L)
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a {@link SyntaxLengthDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getSyntaxLengthDifference( AttributeType at1, AttributeType at2 )
    {
        long at1SyntaxLength = at1.getSyntaxLength();
        long at2SyntaxLength = at2.getSyntaxLength();

        if ( ( at1SyntaxLength == 0 ) && ( at2SyntaxLength != 0 ) )
        {
            PropertyDifference diff = new SyntaxLengthDifference( at1, at2, DifferenceType.ADDED );
            diff.setNewValue( at2SyntaxLength );
            return diff;
        }
        else if ( ( at1SyntaxLength != 0 ) && ( at2SyntaxLength == 0 ) )
        {
            PropertyDifference diff = new SyntaxLengthDifference( at1, at2, DifferenceType.REMOVED );
            diff.setOldValue( at1SyntaxLength );
            return diff;
        }
        else if ( ( at1SyntaxLength != 0 ) && ( at2SyntaxLength != 0 ) )
        {
            if ( at1SyntaxLength != at2SyntaxLength )
            {
                PropertyDifference diff = new SyntaxLengthDifference( at1, at2, DifferenceType.MODIFIED );
                diff.setOldValue( at1SyntaxLength );
                diff.setNewValue( at2SyntaxLength );
                return diff;
            }
        }

        return null;
    }


    // ── Yoda Checks The Single-Value Flag ─────────────────────────────────────
    // The SINGLE-VALUE flag says whether an attribute can hold only one value.
    // If the flag was toggled, Yoda records a SingleValueDifference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code SINGLE-VALUE} flags of two AttributeType objects and
     * returns a {@link SingleValueDifference} if the flag was toggled, or
     * {@code null} if they match.
     *
     * <p>For example — Yoda checks the single-value flag:</p>
     * <pre>
     *   // at1.singleValued = false, at2.singleValued = true
     *   // result: SingleValueDifference(oldValue=false, newValue=true)
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a {@link SingleValueDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getSingleValueDifference( AttributeType at1, AttributeType at2 )
    {
        boolean at1SingleValued = at1.isSingleValued();
        boolean at2SingleValued = at2.isSingleValued();

        if ( at1SingleValued != at2SingleValued )
        {
            PropertyDifference diff = new SingleValueDifference( at1, at2 );
            diff.setOldValue( at1SingleValued );
            diff.setNewValue( at2SingleValued );
            return diff;
        }

        return null;
    }


    // ── Yoda Checks The Collective Flag ───────────────────────────────────────
    // The COLLECTIVE flag marks subtree-wide shared attributes.  If toggled, Yoda
    // records a CollectiveDifference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code COLLECTIVE} flags of two AttributeType objects and
     * returns a {@link CollectiveDifference} if the flag was toggled, or
     * {@code null} if they match.
     *
     * <p>For example — Yoda checks the collective flag:</p>
     * <pre>
     *   // at1.collective = false, at2.collective = true
     *   // result: CollectiveDifference(oldValue=false, newValue=true)
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a {@link CollectiveDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getCollectiveDifference( AttributeType at1, AttributeType at2 )
    {
        boolean at1Collective = at1.isCollective();
        boolean at2Collective = at2.isCollective();

        if ( at1Collective != at2Collective )
        {
            PropertyDifference diff = new CollectiveDifference( at1, at2 );
            diff.setOldValue( at1Collective );
            diff.setNewValue( at2Collective );
            return diff;
        }

        return null;
    }


    // ── Yoda Checks The User-Modification Lock ────────────────────────────────
    // The NO-USER-MODIFICATION flag locks an attribute against user writes.  If
    // the lock state changed, Yoda records a NoUserModificationDifference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code NO-USER-MODIFICATION} flags of two AttributeType objects
     * and returns a {@link NoUserModificationDifference} if the flag was toggled,
     * or {@code null} if they match.
     * Note: the getter is {@code isUserModifiable()} — the sense is inverted
     * relative to the schema keyword, so we compare the modifiable flags directly.
     *
     * <p>For example — Yoda checks the user-modification lock:</p>
     * <pre>
     *   // at1.userModifiable = true, at2.userModifiable = false
     *   // result: NoUserModificationDifference(oldValue=true, newValue=false)
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a {@link NoUserModificationDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getNoUserModificationDifference( AttributeType at1, AttributeType at2 )
    {
        boolean at1IsUserModifiable = at1.isUserModifiable();
        boolean at2IsUserModifiable = at2.isUserModifiable();

        if ( at1IsUserModifiable != at2IsUserModifiable )
        {
            PropertyDifference diff = new NoUserModificationDifference( at1, at2 );
            diff.setOldValue( at1IsUserModifiable );
            diff.setNewValue( at2IsUserModifiable );
            return diff;
        }

        return null;
    }


    // ── Yoda Checks The Equality Matching Rule ────────────────────────────────
    // The EQUALITY OID defines how values of this attribute type are compared for
    // equality.  Null → non-null is ADDED; non-null → null is REMOVED; different
    // OIDs is MODIFIED.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code EQUALITY} matching rule OIDs of two AttributeType objects
     * and returns an {@link EqualityDifference} if they differ, or {@code null} if
     * they match.
     *
     * <p>For example — Yoda checks the equality rule:</p>
     * <pre>
     *   // at1.equalityOid = null, at2.equalityOid = "caseIgnoreMatch"
     *   // result: EqualityDifference(ADDED, newValue="caseIgnoreMatch")
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     an {@link EqualityDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getEqualityDifference( AttributeType at1, AttributeType at2 )
    {
        String at1Equality = at1.getEqualityOid();
        String at2Equality = at2.getEqualityOid();

        if ( ( at1Equality == null ) && ( at2Equality != null ) )
        {
            PropertyDifference diff = new EqualityDifference( at1, at2, DifferenceType.ADDED );
            diff.setNewValue( at2Equality );
            return diff;
        }
        else if ( ( at1Equality != null ) && ( at2Equality == null ) )
        {
            PropertyDifference diff = new EqualityDifference( at1, at2, DifferenceType.REMOVED );
            diff.setOldValue( at1Equality );
            return diff;
        }
        else if ( ( at1Equality != null ) && ( at2Equality != null ) )
        {
            if ( !at1Equality.equals( at2Equality ) )
            {
                PropertyDifference diff = new EqualityDifference( at1, at2, DifferenceType.MODIFIED );
                diff.setOldValue( at1Equality );
                diff.setNewValue( at2Equality );
                return diff;
            }
        }

        return null;
    }


    // ── Yoda Checks The Ordering Matching Rule ────────────────────────────────
    // The ORDERING OID defines how values are sorted.  Same null/non-null/changed
    // logic as equality.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code ORDERING} matching rule OIDs of two AttributeType objects
     * and returns an {@link OrderingDifference} if they differ, or {@code null} if
     * they match.
     *
     * <p>For example — Yoda checks the ordering rule:</p>
     * <pre>
     *   // at1.orderingOid = "integerOrderingMatch", at2.orderingOid = null
     *   // result: OrderingDifference(REMOVED, oldValue="integerOrderingMatch")
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     an {@link OrderingDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getOrderingDifference( AttributeType at1, AttributeType at2 )
    {
        String at1Ordering = at1.getOrderingOid();
        String at2Ordering = at2.getOrderingOid();

        if ( ( at1Ordering == null ) && ( at2Ordering != null ) )
        {
            PropertyDifference diff = new OrderingDifference( at1, at2, DifferenceType.ADDED );
            diff.setNewValue( at2Ordering );
            return diff;
        }
        else if ( ( at1Ordering != null ) && ( at2Ordering == null ) )
        {
            PropertyDifference diff = new OrderingDifference( at1, at2, DifferenceType.REMOVED );
            diff.setOldValue( at1Ordering );
            return diff;
        }
        else if ( ( at1Ordering != null ) && ( at2Ordering != null ) )
        {
            if ( !at1Ordering.equals( at2Ordering ) )
            {
                PropertyDifference diff = new OrderingDifference( at1, at2, DifferenceType.MODIFIED );
                diff.setOldValue( at1Ordering );
                diff.setNewValue( at2Ordering );
                return diff;
            }
        }

        return null;
    }


    // ── Yoda Checks The Substring Matching Rule ────────────────────────────────
    // The SUBSTR OID defines how wildcard searches are matched.  Same null/non-null
    // logic as equality and ordering.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares the {@code SUBSTR} matching rule OIDs of two AttributeType objects
     * and returns a {@link SubstringDifference} if they differ, or {@code null} if
     * they match.
     *
     * <p>For example — Yoda checks the substring rule:</p>
     * <pre>
     *   // at1.substringOid = "caseIgnoreSubstringsMatch"
     *   // at2.substringOid = "caseExactSubstringsMatch"
     *   // result: SubstringDifference(MODIFIED, old=..Ignore.., new=..Exact..)
     * </pre>
     *
     * @param at1  the source AttributeType ("before")
     * @param at2  the destination AttributeType ("after")
     * @return     a {@link SubstringDifference} if changed, or {@code null} if identical
     */
    private static PropertyDifference getSubstringDifference( AttributeType at1, AttributeType at2 )
    {
        String at1Substring = at1.getSubstringOid();
        String at2Substring = at2.getSubstringOid();

        if ( ( at1Substring == null ) && ( at2Substring != null ) )
        {
            PropertyDifference diff = new SubstringDifference( at1, at2, DifferenceType.ADDED );
            diff.setNewValue( at2Substring );
            return diff;
        }
        else if ( ( at1Substring != null ) && ( at2Substring == null ) )
        {
            PropertyDifference diff = new SubstringDifference( at1, at2, DifferenceType.REMOVED );
            diff.setOldValue( at1Substring );
            return diff;
        }
        else if ( ( at1Substring != null ) && ( at2Substring != null ) )
        {
            if ( !at1Substring.equals( at2Substring ) )
            {
                PropertyDifference diff = new SubstringDifference( at1, at2, DifferenceType.MODIFIED );
                diff.setOldValue( at1Substring );
                diff.setNewValue( at2Substring );
                return diff;
            }
        }

        return null;
    }
}
