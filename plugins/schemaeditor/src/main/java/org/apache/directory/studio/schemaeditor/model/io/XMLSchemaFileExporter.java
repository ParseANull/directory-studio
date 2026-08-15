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
package org.apache.directory.studio.schemaeditor.model.io;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


// ── CLASS: XMLSchemaFileExporter — Yoda Lifting Schema into XML Form ──────────
// Luke's X-wing is buried in the Dagobah swamp — it's real, it's intact, but
// it's in the wrong form.  Yoda uses the Force to transform it from "sunken
// object" to "airworthy ship" without changing a single bolt.  This class does
// the same: it takes fully-formed Schema objects and lifts them into pretty-printed
// XML — attribute types, object classes, matching rules, and syntaxes all
// serialised into a form that can be saved, shared, and re-imported later.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Converts {@link Schema} objects into Apache Directory Studio's own XML format,
 * which is used for project save files and export operations.
 * All four schema element types are supported: attribute types, object classes,
 * matching rules, and syntaxes.  The output is UTF-8 pretty-printed XML.
 * Think of this class as Yoda: it takes something real but in the wrong form
 * (Java objects) and transforms it into something usable (XML on disk).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class XMLSchemaFileExporter
{
    // The Tags
    private static final String ALIAS_TAG = "alias"; //$NON-NLS-1$
    private static final String ALIASES_TAG = "aliases"; //$NON-NLS-1$
    private static final String ATTRIBUTE_TYPE_TAG = "attributetype"; //$NON-NLS-1$
    private static final String ATTRIBUTE_TYPES_TAG = "attributetypes"; //$NON-NLS-1$
    private static final String BOOLEAN_FALSE = "false"; //$NON-NLS-1$
    private static final String BOOLEAN_TRUE = "true"; //$NON-NLS-1$
    private static final String COLLECTIVE_TAG = "collective"; //$NON-NLS-1$
    private static final String DESCRIPTION_TAG = "description"; //$NON-NLS-1$
    private static final String EQUALITY_TAG = "equality"; //$NON-NLS-1$
    private static final String HUMAN_READABLE_TAG = "humanreadable"; //$NON-NLS-1$
    private static final String MANDATORY_TAG = "mandatory"; //$NON-NLS-1$
    private static final String MATCHING_RULE_TAG = "matchingrule"; //$NON-NLS-1$
    private static final String MATCHING_RULES_TAG = "matchingrules"; //$NON-NLS-1$
    private static final String NAME_TAG = "name"; //$NON-NLS-1$
    private static final String NO_USER_MODIFICATION_TAG = "nousermodification"; //$NON-NLS-1$
    private static final String OBJECT_CLASS_TAG = "objectclass"; //$NON-NLS-1$
    private static final String OBJECT_CLASSES_TAG = "objectclasses"; //$NON-NLS-1$
    private static final String OBSOLETE_TAG = "obsolete"; //$NON-NLS-1$
    private static final String OID_TAG = "oid"; //$NON-NLS-1$
    private static final String OPTIONAL_TAG = "optional"; //$NON-NLS-1$
    private static final String ORDERING_TAG = "ordering"; //$NON-NLS-1$
    private static final String SCHEMA_TAG = "schema"; //$NON-NLS-1$
    private static final String SCHEMAS_TAG = "schemas"; //$NON-NLS-1$
    private static final String SINGLE_VALUE_TAG = "singlevalue"; //$NON-NLS-1$
    private static final String SUBSTRING_TAG = "substring"; //$NON-NLS-1$
    private static final String SUPERIOR_TAG = "superior"; //$NON-NLS-1$
    private static final String SUPERIORS_TAG = "superiors"; //$NON-NLS-1$
    private static final String SYNTAX_LENGTH_TAG = "syntaxlength"; //$NON-NLS-1$
    private static final String SYNTAX_OID_TAG = "syntaxoid"; //$NON-NLS-1$
    private static final String SYNTAX_TAG = "syntax"; //$NON-NLS-1$
    private static final String SYNTAXES_TAG = "syntaxes"; //$NON-NLS-1$
    private static final String TYPE_TAG = "type"; //$NON-NLS-1$
    private static final String USAGE_TAG = "usage"; //$NON-NLS-1$


    // ── Yoda Lifts One X-Wing into Flight-Ready Form ──────────────────────────
    // One schema, one focused effort — Yoda raises it from Java memory into a
    // pretty-printed XML string ready to be written to a .lsd file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts a single {@link Schema} to its Studio XML representation.
     * The result is a UTF-8 pretty-printed XML string with a {@code <schema>} root.
     *
     * @param schema  the schema to convert — must not be null
     * @return        the XML string
     * @throws IOException  if the internal ByteArrayOutputStream somehow fails to write
     */
    public static String toXml( Schema schema ) throws IOException
    {
        // Creating the Document
        Document document = DocumentHelper.createDocument();

        // Adding the schema
        addSchema( schema, document );

        // Creating the output stream we're going to put the XML in
        OutputStream os = new ByteArrayOutputStream();
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$

        // Writing the XML.
        XMLWriter writer = new XMLWriter( os, outformat );
        writer.write( document );
        writer.flush();
        writer.close();

        return os.toString();
    }


    // ── Yoda Lifts a Whole Squadron at Once ──────────────────────────────────
    // Multiple schemas, one concentrated effort — Yoda raises them all under a
    // shared {@code <schemas>} wrapper, pretty-printed as a single XML string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Converts an array of {@link Schema} objects to a single XML string with a
     * {@code <schemas>} root element.
     * Used when exporting multiple schemas in one file (e.g. for project backups).
     *
     * @param schemas  the schemas to convert — may be empty but not null
     * @return         the XML string
     * @throws IOException  if serialisation fails
     */
    public static String toXml( Schema[] schemas ) throws IOException
    {
        // Creating the Document and the 'root' Element
        Document document = DocumentHelper.createDocument();

        addSchemas( schemas, document );

        // Creating the output stream we're going to put the XML in
        OutputStream os = new ByteArrayOutputStream();
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$

        // Writing the XML.
        XMLWriter writer = new XMLWriter( os, outformat );
        writer.write( document );
        writer.flush();
        writer.close();

        return os.toString();
    }


    // ── Yoda Raises a Fleet and Docks Them Under One Canopy ──────────────────
    // Multiple X-wings raised and parked neatly under a {@code <schemas>} hangar
    // element — this is the shared helper that both toXml(Schema[]) and
    // ProjectsExporter use to add schema lists into a parent DOM branch.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@code <schemas>} element containing one {@code <schema>} child per
     * entry in the array to the given DOM branch.
     * This is the shared building block used by both single-file and project exports.
     *
     * @param schemas  the schemas to add — may be null (produces an empty element)
     * @param branch   the DOM branch to attach to
     */
    public static void addSchemas( Schema[] schemas, Branch branch )
    {
        Element element = branch.addElement( SCHEMAS_TAG );

        if ( schemas != null )
        {
            for ( Schema schema : schemas )
            {
                addSchema( schema, element );
            }
        }
    }


    // ── Yoda Raises a Single Ship and Stows Its Cargo ────────────────────────
    // One schema is lifted into a {@code <schema>} DOM element, its name recorded
    // as an attribute, and each category of schema object (attribute types, object
    // classes, matching rules, syntaxes) stowed as a child group.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@code <schema>} element to the given DOM branch.
     * Emits child groups for attribute types, object classes, matching rules,
     * and syntaxes — only the groups that have at least one element are written.
     *
     * @param schema  the schema to serialise — must not be null
     * @param branch  the DOM branch to attach to
     */
    public static void addSchema( Schema schema, Branch branch )
    {
        Element element = branch.addElement( SCHEMA_TAG );
        if ( schema != null )
        {
            // Name
            String name = schema.getSchemaName();
            if ( ( name != null ) && ( !name.equals( "" ) ) ) //$NON-NLS-1$
            {
                element.addAttribute( NAME_TAG, name );
            }

            // Attribute Types
            List<AttributeType> ats = schema.getAttributeTypes();
            if ( ( ats != null ) && ( ats.size() >= 1 ) )
            {
                Element attributeTypesNode = element.addElement( ATTRIBUTE_TYPES_TAG );
                for ( AttributeType at : ats )
                {
                    toXml( at, attributeTypesNode );
                }
            }

            // Object Classes
            List<ObjectClass> ocs = schema.getObjectClasses();
            if ( ( ocs != null ) && ( ocs.size() >= 1 ) )
            {
                Element objectClassesNode = element.addElement( OBJECT_CLASSES_TAG );
                for ( ObjectClass oc : ocs )
                {
                    toXml( oc, objectClassesNode );
                }
            }

            // Matching Rules
            List<MatchingRule> mrs = schema.getMatchingRules();
            if ( ( mrs != null ) && ( mrs.size() >= 1 ) )
            {
                Element matchingRulesNode = element.addElement( MATCHING_RULES_TAG );
                for ( MatchingRule mr : mrs )
                {
                    toXml( mr, matchingRulesNode );
                }
            }

            // Syntaxes
            List<LdapSyntax> syntaxes = schema.getSyntaxes();
            if ( ( syntaxes != null ) && ( syntaxes.size() >= 1 ) )
            {
                Element syntaxesNode = element.addElement( SYNTAXES_TAG );
                for ( LdapSyntax syntax : syntaxes )
                {
                    toXml( syntax, syntaxesNode );
                }
            }
        }
    }


    // ── Yoda Lifts an Attribute-Type Component into XML Form ─────────────────
    // One fuselage panel — an attribute type — raised and placed precisely into
    // the DOM tree, every property as a child element or attribute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds an {@code <attributetype>} element to the given root element, encoding
     * all properties of the given {@link AttributeType} as child elements and attributes.
     *
     * @param at    the attribute type to serialise — must not be null
     * @param root  the parent element to add to
     */
    private static void toXml( AttributeType at, Element root )
    {
        Element atNode = root.addElement( ATTRIBUTE_TYPE_TAG );

        // OID
        String oid = at.getOid();
        if ( ( oid != null ) && ( !oid.equals( "" ) ) ) //$NON-NLS-1$
        {
            atNode.addAttribute( OID_TAG, oid );
        }

        // Aliases
        List<String> aliases = at.getNames();
        if ( ( aliases != null ) && ( aliases.size() >= 1 ) )
        {
            Element aliasesNode = atNode.addElement( ALIASES_TAG );
            for ( String alias : aliases )
            {
                aliasesNode.addElement( ALIAS_TAG ).setText( alias );
            }
        }

        // Description
        String description = at.getDescription();
        if ( ( description != null ) && ( !description.equals( "" ) ) ) //$NON-NLS-1$
        {
            atNode.addElement( DESCRIPTION_TAG ).setText( description );
        }

        // Superior
        String superior = at.getSuperiorOid();
        if ( ( superior != null ) && ( !superior.equals( "" ) ) ) //$NON-NLS-1$
        {
            atNode.addElement( SUPERIOR_TAG ).setText( superior );
        }

        // Usage
        UsageEnum usage = at.getUsage();
        if ( usage != null )
        {
            atNode.addElement( USAGE_TAG ).setText( usage.toString() );
        }

        // Syntax
        String syntax = at.getSyntaxOid();
        if ( ( syntax != null ) && ( !syntax.equals( "" ) ) ) //$NON-NLS-1$
        {
            atNode.addElement( SYNTAX_TAG ).setText( syntax );
        }

        // Syntax Length
        long syntaxLength = at.getSyntaxLength();
        if ( syntaxLength > 0 )
        {
            atNode.addElement( SYNTAX_LENGTH_TAG ).setText( "" + syntaxLength ); //$NON-NLS-1$
        }

        // Obsolete
        if ( at.isObsolete() )
        {
            atNode.addAttribute( OBSOLETE_TAG, BOOLEAN_TRUE );
        }
        else
        {
            atNode.addAttribute( OBSOLETE_TAG, BOOLEAN_FALSE );
        }

        // Single Value
        if ( at.isSingleValued() )
        {
            atNode.addAttribute( SINGLE_VALUE_TAG, BOOLEAN_TRUE );
        }
        else
        {
            atNode.addAttribute( SINGLE_VALUE_TAG, BOOLEAN_FALSE );
        }

        // Collective
        if ( at.isCollective() )
        {
            atNode.addAttribute( COLLECTIVE_TAG, BOOLEAN_TRUE );
        }
        else
        {
            atNode.addAttribute( COLLECTIVE_TAG, BOOLEAN_FALSE );
        }

        // No User Modification
        if ( at.isUserModifiable() )
        {
            atNode.addAttribute( NO_USER_MODIFICATION_TAG, BOOLEAN_FALSE );
        }
        else
        {
            atNode.addAttribute( NO_USER_MODIFICATION_TAG, BOOLEAN_TRUE );
        }

        // Equality
        String equality = at.getEqualityOid();
        if ( ( equality != null ) && ( !equality.equals( "" ) ) ) //$NON-NLS-1$
        {
            atNode.addElement( EQUALITY_TAG ).setText( equality );
        }

        // Ordering
        String ordering = at.getOrderingOid();
        if ( ( ordering != null ) && ( !ordering.equals( "" ) ) ) //$NON-NLS-1$
        {
            atNode.addElement( ORDERING_TAG ).setText( ordering );
        }

        // Substring
        String substring = at.getSubstringOid();
        if ( ( substring != null ) && ( !substring.equals( "" ) ) ) //$NON-NLS-1$
        {
            atNode.addElement( SUBSTRING_TAG ).setText( substring );
        }
    }


    // ── Yoda Lifts an Object-Class Hull Section into XML Form ─────────────────
    // Another component — an object class — raised and serialised precisely:
    // OID, aliases, description, superiors, type, obsolete flag, must/may lists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds an {@code <objectclass>} element to the given root element, encoding
     * all properties of the given {@link ObjectClass} as child elements and attributes.
     *
     * @param oc    the object class to serialise — must not be null
     * @param root  the parent element to add to
     */
    private static void toXml( ObjectClass oc, Element root )
    {
        Element ocNode = root.addElement( OBJECT_CLASS_TAG );

        // OID
        String oid = oc.getOid();
        if ( ( oid != null ) && ( !oid.equals( "" ) ) ) //$NON-NLS-1$
        {
            ocNode.addAttribute( OID_TAG, oid );
        }

        // Aliases
        List<String> aliases = oc.getNames();
        if ( ( aliases != null ) && ( aliases.size() >= 1 ) )
        {
            Element aliasesNode = ocNode.addElement( ALIASES_TAG );
            for ( String alias : aliases )
            {
                aliasesNode.addElement( ALIAS_TAG ).setText( alias );
            }
        }

        // Description
        String description = oc.getDescription();
        if ( ( description != null ) && ( !description.equals( "" ) ) ) //$NON-NLS-1$
        {
            ocNode.addElement( DESCRIPTION_TAG ).setText( description );
        }

        // Superiors
        List<String> superiors = oc.getSuperiorOids();
        if ( ( superiors != null ) && ( superiors.size() >= 1 ) )
        {
            Element superiorsNode = ocNode.addElement( SUPERIORS_TAG );
            for ( String superior : superiors )
            {
                superiorsNode.addElement( SUPERIOR_TAG ).setText( superior );
            }
        }

        // Type
        ObjectClassTypeEnum type = oc.getType();
        if ( type != null )
        {
            ocNode.addElement( TYPE_TAG ).setText( type.toString() );
        }

        // Obsolete
        if ( oc.isObsolete() )
        {
            ocNode.addAttribute( OBSOLETE_TAG, BOOLEAN_TRUE );
        }
        else
        {
            ocNode.addAttribute( OBSOLETE_TAG, BOOLEAN_FALSE );
        }

        // Mandatory Attribute Types
        List<String> mandatoryATs = oc.getMustAttributeTypeOids();
        if ( ( mandatoryATs != null ) && ( mandatoryATs.size() >= 1 ) )
        {
            Element mandatoryNode = ocNode.addElement( MANDATORY_TAG );
            for ( String mandatoryAT : mandatoryATs )
            {
                mandatoryNode.addElement( ATTRIBUTE_TYPE_TAG ).setText( mandatoryAT );
            }
        }

        // Optional Attribute Types
        List<String> optionalATs = oc.getMayAttributeTypeOids();
        if ( ( optionalATs != null ) && ( optionalATs.size() >= 1 ) )
        {
            Element optionalNode = ocNode.addElement( OPTIONAL_TAG );
            for ( String optionalAT : optionalATs )
            {
                optionalNode.addElement( ATTRIBUTE_TYPE_TAG ).setText( optionalAT );
            }
        }
    }


    // ── Yoda Lifts a Matching-Rule Component into XML Form ────────────────────
    // A matching rule — OID, aliases, description, obsolete, syntax OID —
    // raised and placed into a {@code <matchingrule>} element.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@code <matchingrule>} element to the given root element.
     *
     * @param mr    the matching rule to serialise — must not be null
     * @param root  the parent element to add to
     */
    private static void toXml( MatchingRule mr, Element root )
    {
        Element mrNode = root.addElement( MATCHING_RULE_TAG );

        // OID
        String oid = mr.getOid();
        if ( ( oid != null ) && ( !oid.equals( "" ) ) ) //$NON-NLS-1$
        {
            mrNode.addAttribute( OID_TAG, oid );
        }

        // Aliases
        List<String> aliases = mr.getNames();
        if ( ( aliases != null ) && ( aliases.size() >= 1 ) )
        {
            Element aliasesNode = mrNode.addElement( ALIASES_TAG );
            for ( String alias : aliases )
            {
                aliasesNode.addElement( ALIAS_TAG ).setText( alias );
            }
        }

        // Description
        String description = mr.getDescription();
        if ( ( description != null ) && ( !description.equals( "" ) ) ) //$NON-NLS-1$
        {
            mrNode.addElement( DESCRIPTION_TAG ).setText( description );
        }

        // Obsolete
        if ( mr.isObsolete() )
        {
            mrNode.addAttribute( OBSOLETE_TAG, BOOLEAN_TRUE );
        }
        else
        {
            mrNode.addAttribute( OBSOLETE_TAG, BOOLEAN_FALSE );
        }

        // Syntax OID
        String syntaxOid = mr.getSyntaxOid();
        if ( ( syntaxOid != null ) && ( !syntaxOid.equals( "" ) ) ) //$NON-NLS-1$
        {
            mrNode.addElement( SYNTAX_OID_TAG ).setText( syntaxOid );
        }
    }


    // ── Yoda Lifts a Syntax Pod into XML Form ────────────────────────────────
    // A syntax definition — OID, aliases, description, obsolete, human-readable —
    // raised and placed into a {@code <syntax>} element.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@code <syntax>} element to the given root element.
     *
     * @param syntax  the syntax to serialise — must not be null
     * @param root    the parent element to add to
     */
    private static void toXml( LdapSyntax syntax, Element root )
    {
        Element syntaxNode = root.addElement( SYNTAX_TAG );

        // OID
        String oid = syntax.getOid();
        if ( ( oid != null ) && ( !oid.equals( "" ) ) ) //$NON-NLS-1$
        {
            syntaxNode.addAttribute( OID_TAG, oid );
        }

        // Aliases
        List<String> aliases = syntax.getNames();
        if ( ( aliases != null ) && ( aliases.size() >= 1 ) )
        {
            Element aliasesNode = syntaxNode.addElement( ALIASES_TAG );
            for ( String alias : aliases )
            {
                aliasesNode.addElement( ALIAS_TAG ).setText( alias );
            }
        }

        // Description
        String description = syntax.getDescription();
        if ( ( description != null ) && ( !description.equals( "" ) ) ) //$NON-NLS-1$
        {
            syntaxNode.addElement( DESCRIPTION_TAG ).setText( description );
        }

        // Obsolete
        if ( syntax.isObsolete() )
        {
            syntaxNode.addAttribute( OBSOLETE_TAG, BOOLEAN_TRUE );
        }
        else
        {
            syntaxNode.addAttribute( OBSOLETE_TAG, BOOLEAN_FALSE );
        }

        // Human Readible
        if ( syntax.isHumanReadable() )
        {
            syntaxNode.addAttribute( HUMAN_READABLE_TAG, BOOLEAN_TRUE );
        }
        else
        {
            syntaxNode.addAttribute( HUMAN_READABLE_TAG, BOOLEAN_FALSE );
        }
    }
}
