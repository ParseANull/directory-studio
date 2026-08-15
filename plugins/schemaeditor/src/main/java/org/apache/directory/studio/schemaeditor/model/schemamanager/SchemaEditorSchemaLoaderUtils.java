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
package org.apache.directory.studio.schemaeditor.model.schemamanager;


import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.api.util.Strings;


// ── CLASS: SchemaEditorSchemaLoaderUtils — C-3PO's Translation Toolkit ───────
// C-3PO's translation isn't a single speech act — it's dozens of small
// conversion steps, one per field.  This utility class holds all of those steps
// as static helpers.  Each method reads one field from a schema model object
// and encodes it as an LDAP attribute in the target Entry — the format the
// DefaultSchemaManager understands.  The four public toEntry() methods are the
// top-level translation calls; the private addXxx() methods are the
// word-by-word vocabulary lookup that backs them.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static helper class for {@link SchemaEditorSchemaLoader}.
 * Converts schema editor model objects ({@link AttributeType}, {@link MatchingRule},
 * {@link ObjectClass}, {@link LdapSyntax}) into LDAP {@link Entry} representations
 * consumable by Apache Directory API's schema registry.
 * Every method that writes an optional field first checks whether the value is
 * present before creating the attribute, so the entry stays minimal and correct.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaEditorSchemaLoaderUtils
{
    private static final String M_COLLECTIVE = "m-collective"; //$NON-NLS-1$
    private static final String M_DESCRIPTION = "m-description"; //$NON-NLS-1$
    private static final String M_EQUALITY = "m-equality"; //$NON-NLS-1$
    private static final String M_LENGTH = "m-length"; //$NON-NLS-1$
    private static final String M_MAY = "m-may"; //$NON-NLS-1$
    private static final String M_MUST = "m-must"; //$NON-NLS-1$
    private static final String M_NAME = "m-name"; //$NON-NLS-1$
    private static final String M_NO_USER_MODIFICATION = "m-noUserModification"; //$NON-NLS-1$
    private static final String M_OBSOLETE = "m-obsolete"; //$NON-NLS-1$
    private static final String M_OID = "m-oid"; //$NON-NLS-1$
    private static final String M_ORDERING = "m-ordering"; //$NON-NLS-1$
    private static final String M_SINGLE_VALUE = "m-singleValue"; //$NON-NLS-1$
    private static final String M_SUBSTR = "m-substr"; //$NON-NLS-1$
    private static final String M_SUP_ATTRIBUTE_TYPE = "m-supAttributeType"; //$NON-NLS-1$
    private static final String M_SUP_OBJECT_CLASS = "m-supObjectClass"; //$NON-NLS-1$
    private static final String M_SYNTAX = "m-syntax"; //$NON-NLS-1$
    private static final String M_TYPE_OBJECT_CLASS = "m-typeObjectClass"; //$NON-NLS-1$
    private static final String M_USAGE = "m-usage"; //$NON-NLS-1$
    private static final String TRUE = "TRUE"; //$NON-NLS-1$


    // ── C-3PO Translates an Attribute Type into LDAP Entry ───────────────────
    // C-3PO reads the AttributeType object field by field — OID, names, superior,
    // matching rules, syntax, flags — and encodes each as an LDAP attribute in
    // a fresh DefaultEntry.  The result is what the schema registry expects.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts the given attribute type to an equivalent LDAP {@link Entry}
     * representation consumable by the Apache Directory API schema registry.
     * All optional fields are written only when present.
     *
     * @param attributeType  the attribute type to convert — must not be null
     * @return               the entry representation — never null
     * @throws LdapException  if any field value is invalid (e.g. malformed DN)
     */
    public static Entry toEntry( AttributeType attributeType ) throws LdapException
    {
        // Creating a blank entry
        Entry entry = new DefaultEntry();

        // Setting calculated DN
        entry.setDn( getDn( attributeType, SchemaConstants.ATTRIBUTE_TYPES_PATH ) );

        // Values common to all schema objects
        addSchemaObjectValues( attributeType, SchemaConstants.META_ATTRIBUTE_TYPE_OC, entry );

        // Superior value
        addSuperiorValue( attributeType, entry );

        // Equality matching rule value
        addEqualityValue( attributeType, entry );

        // Ordering matching rule value
        addOrderingValue( attributeType, entry );

        // Substrings matching rule value
        addSubstrValue( attributeType, entry );

        // Syntax value
        addSyntaxValue( attributeType, entry );

        // Single value value
        addSingleValueValue( attributeType, entry );

        // Collective value
        addCollectiveValue( attributeType, entry );

        // No user modification value
        addNoUserModificationValue( attributeType, entry );

        // Usage value
        addUsageValue( attributeType, entry );

        return entry;
    }


    // ── C-3PO Translates a Matching Rule into LDAP Entry ─────────────────────
    // C-3PO reads the MatchingRule and encodes the common fields plus the syntax
    // OID that the rule operates on.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts the given matching rule to an equivalent LDAP {@link Entry}.
     *
     * @param matchingRule  the matching rule to convert — must not be null
     * @return              the entry representation — never null
     * @throws LdapException  if any field value is invalid
     */
    public static Entry toEntry( MatchingRule matchingRule ) throws LdapException
    {
        // Creating a blank entry
        Entry entry = new DefaultEntry();

        // Setting calculated DN
        entry.setDn( getDn( matchingRule, SchemaConstants.MATCHING_RULES_PATH ) );

        // Values common to all schema objects
        addSchemaObjectValues( matchingRule, SchemaConstants.META_MATCHING_RULE_OC, entry );

        String syntax = matchingRule.getSyntaxOid();
        if ( !Strings.isEmpty( syntax ) )
        {
            Attribute attribute = new DefaultAttribute( M_SYNTAX, syntax );
            entry.add( attribute );
        }

        return entry;
    }


    // ── C-3PO Translates an Object Class into LDAP Entry ─────────────────────
    // C-3PO reads the ObjectClass and encodes: common fields, superiors, class
    // type, mandatory attribute list, and optional attribute list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts the given object class to an equivalent LDAP {@link Entry}.
     *
     * @param objectClass  the object class to convert — must not be null
     * @return             the entry representation — never null
     * @throws LdapException  if any field value is invalid
     */
    public static Entry toEntry( ObjectClass objectClass ) throws LdapException
    {
        // Creating a blank entry
        Entry entry = new DefaultEntry();

        // Setting calculated DN
        entry.setDn( getDn( objectClass, SchemaConstants.OBJECT_CLASSES_PATH ) );

        // Values common to all schema objects
        addSchemaObjectValues( objectClass, SchemaConstants.META_OBJECT_CLASS_OC, entry );

        // Superiors value
        addSuperiorsValue( objectClass, entry );

        // Class type value
        addClassTypeValue( objectClass, entry );

        // Musts value
        addMustsValue( objectClass, entry );

        // Mays value
        addMaysValue( objectClass, entry );

        return entry;
    }


    // ── C-3PO Translates an LDAP Syntax into LDAP Entry ─────────────────────
    // C-3PO reads the LdapSyntax and encodes only the common schema object
    // fields; syntaxes carry no extra type-specific attributes beyond those.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts the given LDAP syntax to an equivalent LDAP {@link Entry}.
     * Syntaxes only carry the common schema object attributes (OID, names,
     * description, obsolete), so no extra fields are added.
     *
     * @param syntax  the syntax to convert — must not be null
     * @return        the entry representation — never null
     * @throws LdapException  if any field value is invalid
     */
    public static Entry toEntry( LdapSyntax syntax ) throws LdapException
    {
        // Creating a blank entry
        Entry entry = new DefaultEntry();

        // Setting calculated DN
        entry.setDn( getDn( syntax, SchemaConstants.MATCHING_RULES_PATH ) );

        // Values common to all schema objects
        addSchemaObjectValues( syntax, SchemaConstants.META_MATCHING_RULE_OC, entry );

        return entry;
    }


    // ── C-3PO Computes the DN for Any Schema Object ───────────────────────────
    // Builds the standard four-component DN:
    //   ou=schema / cn=<schemaName> / <objectPath> / m-oid=<oid>
    // The DN anchors the entry in the schema DIT so the registry can locate it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the LDAP DN for the given schema object under the given object path
     * (e.g. {@code attributetypes}, {@code objectclasses}).
     *
     * @param schemaObject  the schema object whose DN is needed
     * @param objectPath    the sub-path constant from {@link SchemaConstants}
     * @return              the four-component DN — never null
     * @throws LdapInvalidDnException  if the schema name or OID produces an invalid DN component
     */
    private static Dn getDn( SchemaObject schemaObject, String objectPath ) throws LdapInvalidDnException
    {
        try
        {
            return Dn.EMPTY_DN
                .add( new Rdn( SchemaConstants.OU_SCHEMA ) )
                .add( new Rdn( SchemaConstants.CN_AT, Rdn.escapeValue( schemaObject.getSchemaName() ) ) )
                .add( new Rdn( objectPath ) )
                .add( new Rdn( M_OID, schemaObject.getOid() ) );
        }
        catch ( LdapInvalidAttributeValueException liave )
        {
            throw new LdapInvalidDnException( liave.getLocalizedMessage(), liave );
        }
    }


    // ── C-3PO Writes the Fields Common to All Schema Objects ─────────────────
    // objectClass, OID, names (aliases), description, and obsolete flag are the
    // same for every schema object type.  This helper encodes them all.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the attributes shared by every schema object type:
     * objectClass (with top, metaTop, and the specific OC), m-oid, m-name,
     * m-description, and m-obsolete (when true).
     *
     * @param schemaObject      the source schema object
     * @param objectClassValue  the specific meta-objectClass value (e.g. {@code metaAttributeType})
     * @param entry             the target entry
     * @throws LdapException    if any attribute operation fails
     */
    private static void addSchemaObjectValues( SchemaObject schemaObject, String objectClassValue, Entry entry )
        throws LdapException
    {
        // ObjectClass
        addObjectClassValue( schemaObject, objectClassValue, entry );

        // OID
        addOidValue( schemaObject, entry );

        // Names
        addNamesValue( schemaObject, entry );

        // Description
        addDescriptionValue( schemaObject, entry );

        // Obsolete
        addObsoleteValue( schemaObject, entry );
    }


    // ── C-3PO Sets the objectClass Attribute ─────────────────────────────────
    // Every schema entry has objectClass values of "top", "metaTop", and the
    // specific meta-objectClass for its type.  All three are written here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the objectClass attribute to the entry with three values:
     * {@code top}, {@code metaTop}, and the supplied specific OC value.
     *
     * @param schemaObject      the source schema object (used for context only)
     * @param objectClassValue  the specific meta-objectClass to add
     * @param entry             the target entry
     * @throws LdapException    if the attribute cannot be added
     */
    private static void addObjectClassValue( SchemaObject schemaObject, String objectClassValue, Entry entry )
        throws LdapException
    {
        Attribute objectClassAttribute = new DefaultAttribute( SchemaConstants.OBJECT_CLASS_AT );
        entry.add( objectClassAttribute );
        objectClassAttribute.add( SchemaConstants.TOP_OC );
        objectClassAttribute.add( SchemaConstants.META_TOP_OC );
        objectClassAttribute.add( objectClassValue );
    }


    // ── C-3PO Writes the OID Attribute ───────────────────────────────────────
    // Encodes the schema object's OID as the m-oid attribute value, but only
    // if the OID is non-empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-oid} attribute to the entry if the schema object's OID
     * is non-empty.
     *
     * @param schemaObject  the source schema object
     * @param entry         the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addOidValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        String oid = schemaObject.getOid();
        if ( !Strings.isEmpty( oid ) )
        {
            Attribute attribute = new DefaultAttribute( M_OID, oid );
            entry.add( attribute );
        }
    }


    // ── C-3PO Writes All Alias Names ─────────────────────────────────────────
    // Each human-readable alias becomes a separate value in the m-name attribute.
    // If the names list is null or empty, no m-name attribute is written.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-name} attribute to the entry with one value per alias.
     * No attribute is added when the names list is null or empty.
     *
     * @param schemaObject  the source schema object
     * @param entry         the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addNamesValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        List<String> names = schemaObject.getNames();
        if ( ( names != null ) && !names.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_NAME );
            entry.add( attribute );

            for ( String name : names )
            {
                attribute.add( name );
            }
        }
    }


    // ── C-3PO Writes the Description Field ───────────────────────────────────
    // Encodes the human-readable description as m-description, but only if it
    // is non-empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-description} attribute to the entry if the schema
     * object has a non-empty description.
     *
     * @param schemaObject  the source schema object
     * @param entry         the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addDescriptionValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        String description = schemaObject.getDescription();
        if ( !Strings.isEmpty( description ) )
        {
            Attribute attribute = new DefaultAttribute( M_DESCRIPTION, description );
            entry.add( attribute );
        }
    }


    // ── C-3PO Marks Obsolete Objects ─────────────────────────────────────────
    // The m-obsolete attribute is only written when the object is actually
    // obsolete; omitting it implies false.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-obsolete} attribute (value "TRUE") to the entry
     * only when the schema object is marked obsolete.
     *
     * @param schemaObject  the source schema object
     * @param entry         the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addObsoleteValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        if ( schemaObject.isObsolete() )
        {
            Attribute attribute = new DefaultAttribute( M_OBSOLETE, TRUE );
            entry.add( attribute );
        }
    }


    // ── C-3PO Writes the Attribute Type's Superior ───────────────────────────
    // Encodes the direct superior attribute type name as m-supAttributeType,
    // but only if a superior is defined.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-supAttributeType} attribute to the entry if the attribute
     * type has a named superior.
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addSuperiorValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String superior = attributeType.getSuperiorName();
        if ( !Strings.isEmpty( superior ) )
        {
            Attribute attribute = new DefaultAttribute( M_SUP_ATTRIBUTE_TYPE, superior );
            entry.add( attribute );
        }
    }


    // ── C-3PO Writes the Equality Matching Rule ───────────────────────────────
    // Encodes the equality matching rule name as m-equality, if present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-equality} attribute to the entry if the attribute type
     * specifies an equality matching rule.
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addEqualityValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String equality = attributeType.getEqualityName();
        if ( !Strings.isEmpty( equality ) )
        {
            Attribute attribute = new DefaultAttribute( M_EQUALITY, equality );
            entry.add( attribute );
        }
    }


    // ── C-3PO Writes the Ordering Matching Rule ───────────────────────────────
    // Encodes the ordering matching rule name as m-ordering, if present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-ordering} attribute to the entry if the attribute type
     * specifies an ordering matching rule.
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addOrderingValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String ordering = attributeType.getOrderingName();
        if ( !Strings.isEmpty( ordering ) )
        {
            Attribute attribute = new DefaultAttribute( M_ORDERING, ordering );
            entry.add( attribute );
        }
    }


    // ── C-3PO Writes the Substring Matching Rule ──────────────────────────────
    // Encodes the substring matching rule name as m-substr, if present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-substr} attribute to the entry if the attribute type
     * specifies a substring matching rule.
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addSubstrValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String substr = attributeType.getSubstringName();
        if ( !Strings.isEmpty( substr ) )
        {
            Attribute attribute = new DefaultAttribute( M_SUBSTR, substr );
            entry.add( attribute );
        }
    }


    // ── C-3PO Writes the Syntax and Length ────────────────────────────────────
    // Encodes the syntax OID as m-syntax; if a max length is defined (not -1),
    // also writes m-length.  Both are written together or neither is written.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-syntax} attribute (and optionally {@code m-length})
     * to the entry.  Only written when the attribute type has a syntax name.
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if an attribute cannot be added
     */
    private static void addSyntaxValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String syntax = attributeType.getSyntaxName();
        if ( !Strings.isEmpty( syntax ) )
        {
            Attribute attribute = new DefaultAttribute( M_SYNTAX, syntax );
            entry.add( attribute );

            long syntaxLength = attributeType.getSyntaxLength();
            if ( syntaxLength != -1 )
            {
                attribute = new DefaultAttribute( M_LENGTH, Long.toString( syntaxLength) ); //$NON-NLS-1$
                entry.add( attribute );
            }
        }
    }


    // ── C-3PO Marks Single-Valued Attributes ─────────────────────────────────
    // The m-singleValue attribute is only written when the flag is true; the
    // API interprets its absence as "multi-valued".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-singleValue} attribute (value "TRUE") to the entry
     * only when the attribute type is single-valued.
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addSingleValueValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( attributeType.isSingleValued() )
        {
            Attribute attribute = new DefaultAttribute( M_SINGLE_VALUE, TRUE );
            entry.add( attribute );
        }
    }


    // ── C-3PO Marks Collective Attributes ────────────────────────────────────
    // The m-collective attribute is only written when the flag is true.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-collective} attribute (value "TRUE") to the entry
     * only when the attribute type is collective.
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addCollectiveValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( attributeType.isCollective() )
        {
            Attribute attribute = new DefaultAttribute( M_COLLECTIVE, TRUE );
            entry.add( attribute );
        }
    }


    // ── C-3PO Marks Operational Attributes ───────────────────────────────────
    // The m-noUserModification attribute is written when the attribute is NOT
    // user-modifiable (i.e. it's an operational attribute).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-noUserModification} attribute (value "TRUE") to the
     * entry when the attribute type is not user-modifiable.
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addNoUserModificationValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( !attributeType.isUserModifiable() )
        {
            Attribute attribute = new DefaultAttribute( M_NO_USER_MODIFICATION, TRUE );
            entry.add( attribute );
        }
    }


    // ── C-3PO Encodes the Usage Value ────────────────────────────────────────
    // The usage attribute is only written when it's not the default
    // (USER_APPLICATIONS); the API treats its absence as USER_APPLICATIONS.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-usage} attribute to the entry when the attribute type's
     * usage is not {@code userApplications} (the default, implied by absence).
     *
     * @param attributeType  the attribute type
     * @param entry          the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addUsageValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        UsageEnum usage = attributeType.getUsage();
        if ( usage != UsageEnum.USER_APPLICATIONS )
        {
            Attribute attribute = new DefaultAttribute( M_USAGE, usage.render() );
            entry.add( attribute );
        }
    }


    // ── C-3PO Writes the Object Class's Superior OIDs ────────────────────────
    // Encodes all superiors as multi-valued m-supObjectClass; written only when
    // at least one superior is defined.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-supObjectClass} attribute to the entry with one value
     * per superior.  No attribute is added when the superiors list is null or empty.
     *
     * @param objectClass  the object class
     * @param entry        the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addSuperiorsValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> superiors = objectClass.getSuperiorOids();
        if ( ( superiors != null ) && !superiors.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_SUP_OBJECT_CLASS );
            entry.add( attribute );

            for ( String superior : superiors )
            {
                attribute.add( superior );
            }
        }
    }


    // ── C-3PO Encodes the Object Class Type ──────────────────────────────────
    // Written only when the type is not STRUCTURAL; the API treats absence as
    // STRUCTURAL.  AUXILIARY and ABSTRACT are written explicitly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-typeObjectClass} attribute to the entry when the object
     * class type is not {@code STRUCTURAL} (the default implied by absence).
     *
     * @param objectClass  the object class
     * @param entry        the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addClassTypeValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        ObjectClassTypeEnum classType = objectClass.getType();
        if ( classType != ObjectClassTypeEnum.STRUCTURAL )
        {
            Attribute attribute = new DefaultAttribute( M_TYPE_OBJECT_CLASS, classType.toString() );
            entry.add( attribute );
        }
    }


    // ── C-3PO Lists the Mandatory Attribute Types ─────────────────────────────
    // Encodes all MUST attribute type OIDs as multi-valued m-must; written only
    // when at least one MUST is defined.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-must} attribute to the entry with one value per mandatory
     * attribute type OID.  No attribute is added when the list is null or empty.
     *
     * @param objectClass  the object class
     * @param entry        the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addMustsValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> musts = objectClass.getMustAttributeTypeOids();
        if ( ( musts != null ) && !musts.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_MUST );
            entry.add( attribute );

            for ( String must : musts )
            {
                attribute.add( must );
            }
        }
    }


    // ── C-3PO Lists the Optional Attribute Types ──────────────────────────────
    // Encodes all MAY attribute type OIDs as multi-valued m-may; written only
    // when at least one MAY is defined.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the {@code m-may} attribute to the entry with one value per optional
     * attribute type OID.  No attribute is added when the list is null or empty.
     *
     * @param objectClass  the object class
     * @param entry        the target entry
     * @throws LdapException  if the attribute cannot be added
     */
    private static void addMaysValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> mays = objectClass.getMayAttributeTypeOids();
        if ( ( mays != null ) && !mays.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_MAY );
            entry.add( attribute );

            for ( String may : mays )
            {
                attribute.add( may );
            }
        }
    }
}
