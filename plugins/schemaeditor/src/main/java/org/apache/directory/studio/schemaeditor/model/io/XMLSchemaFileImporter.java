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


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.osgi.util.NLS;


// ── CLASS: XMLSchemaFileImporter — C-3PO Reading an XML Schema Scroll ─────────
// The XML schema file is written in Studio's own dialect — a structured XML
// vocabulary with its own element names and attribute conventions.  C-3PO reads
// every element, decodes each field from its XML representation, and assembles
// the proper AttributeType, ObjectClass, MatchingRule, and LdapSyntax objects
// the schema editor's model expects.  If anything is missing or malformed,
// C-3PO flags it immediately with a clear error.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Reads Apache Directory Studio's own XML schema format from an
 * {@link InputStream} and converts it into {@link Schema} objects.
 * Supports both single-schema ({@code <schema>} root) and multi-schema
 * ({@code <schemas>} root) files.  Delegates the actual DOM parsing to Dom4J.
 * Think of this class as C-3PO: he reads every XML element like a sentence in
 * a foreign dialect and translates it into the Java objects everyone else uses.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class XMLSchemaFileImporter
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


    // ── C-3PO Reads a Multi-Schema Scroll ────────────────────────────────────
    // The scroll has a {@code <schemas>} root; C-3PO iterates over each nested
    // {@code <schema>} element and translates each into a Schema object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses a multi-schema XML file (root element {@code <schemas>}) and returns
     * all contained schemas as an array.
     *
     * @param inputStream  the stream to read — must not be null
     * @param path         the file path, used for schema naming and error messages
     * @return             an array of Schema objects — never null
     * @throws XMLSchemaFileImportException  if the file can't be read or isn't a valid schemas file
     */
    public static Schema[] getSchemas( InputStream inputStream, String path ) throws XMLSchemaFileImportException
    {
        SAXReader reader = new SAXReader();
        Document document = null;
        try
        {
            document = reader.read( inputStream );
        }
        catch ( DocumentException e )
        {
            throw new XMLSchemaFileImportException( NLS.bind( Messages
                .getString( "XMLSchemaFileImporter.NotReadCorrectly" ), new String[] { path } ), e ); //$NON-NLS-1$
        }

        Element rootElement = document.getRootElement();
        if ( !rootElement.getName().equals( SCHEMAS_TAG ) )
        {
            throw new XMLSchemaFileImportException( NLS.bind( Messages
                .getString( "XMLSchemaFileImporter.NotValidSchema" ), new String[] { path } ) ); //$NON-NLS-1$
        }

        return readSchemas( rootElement, path );
    }


    // ── C-3PO Reads a Single-Schema Scroll ───────────────────────────────────
    // The scroll has a {@code <schema>} root; C-3PO reads the whole thing and
    // returns one Schema object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses a single-schema XML file (root element {@code <schema>}) and returns it.
     *
     * @param inputStream  the stream to read — must not be null
     * @param path         the file path, used for schema naming and error messages
     * @return             the parsed Schema — never null
     * @throws XMLSchemaFileImportException  if the file can't be read or is malformed
     */
    public static Schema getSchema( InputStream inputStream, String path ) throws XMLSchemaFileImportException
    {
        SAXReader reader = new SAXReader();
        Document document = null;
        try
        {
            document = reader.read( inputStream );
        }
        catch ( DocumentException e )
        {
            throw new XMLSchemaFileImportException( NLS.bind( Messages
                .getString( "XMLSchemaFileImporter.NotReadCorrectly" ), new String[] { path } ), e ); //$NON-NLS-1$
        }

        Element rootElement = document.getRootElement();

        return readSchema( rootElement, path );
    }


    // ── C-3PO Iterates Over a Bundle of Schema Scrolls ───────────────────────
    // C-3PO opens the bundle ({@code <schemas>} element), reads the label count,
    // and decodes each nested {@code <schema>} in turn.
    // This is also called directly by ProjectsImporter when reading schema backups.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all {@code <schema>} children of the given {@code <schemas>} element.
     * Exposed as public because {@link ProjectsImporter} calls it directly when
     * reading embedded schema backup elements.
     *
     * @param element  the {@code <schemas>} DOM element — must not be null
     * @param path     the file path, used for error messages
     * @return         an array of Schema objects — never null
     * @throws XMLSchemaFileImportException  if the element is not a valid {@code <schemas>} node
     */
    public static Schema[] readSchemas( Element element, String path ) throws XMLSchemaFileImportException
    {
        List<Schema> schemas = new ArrayList<Schema>();

        if ( !element.getName().equals( SCHEMAS_TAG ) )
        {
            throw new XMLSchemaFileImportException( NLS.bind( Messages
                .getString( "XMLSchemaFileImporter.NotValidSchema" ), new String[] { path } ) ); //$NON-NLS-1$
        }

        for ( Iterator<?> i = element.elementIterator( SCHEMA_TAG ); i.hasNext(); )
        {
            Element schemaElement = ( Element ) i.next();
            schemas.add( readSchema( schemaElement, path ) );
        }

        return schemas.toArray( new Schema[0] );
    }


    // ── C-3PO Decodes a Single Schema Scroll in Full ─────────────────────────
    // C-3PO reads the schema's name, then decodes each section in order:
    // attribute types, object classes, matching rules, syntaxes.
    // Exposed as public because ProjectsImporter reuses it for embedded schemas.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads a {@code <schema>} DOM element and builds the corresponding Schema.
     * Delegates to the four {@code readX} helpers for each element type.
     * Exposed as public because {@link ProjectsImporter} calls it directly.
     *
     * @param element  the {@code <schema>} DOM element — must not be null
     * @param path     the file path, used for schema naming and error messages
     * @return         a populated Schema — never null
     * @throws XMLSchemaFileImportException  if any required data is missing or invalid
     */
    public static Schema readSchema( Element element, String path ) throws XMLSchemaFileImportException
    {
        // Creating the schema with an empty name
        Schema schema = new Schema( getSchemaName( element, path ) );

        // Attribute Types
        readAttributeTypes( element, schema );

        // Object Classes
        readObjectClasses( element, schema );

        // Matching Rules
        readMatchingRules( element, schema );

        // Syntaxes
        readSyntaxes( element, schema );

        return schema;
    }


    // ── C-3PO Reads the Schema's Title ───────────────────────────────────────
    // C-3PO checks the {@code name} attribute on the {@code <schema>} element;
    // if it's there he uses it; otherwise he derives the name from the filename.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Determines the schema name from the {@code name} attribute of the element,
     * falling back to deriving it from the file path if the attribute is absent.
     *
     * @param element  the {@code <schema>} DOM element
     * @param path     the file path to fall back on
     * @return         the schema name — never null
     * @throws XMLSchemaFileImportException  if the element is not a {@code <schema>} element
     */
    private static String getSchemaName( Element element, String path ) throws XMLSchemaFileImportException
    {
        if ( !element.getName().equals( SCHEMA_TAG ) )
        {
            throw new XMLSchemaFileImportException( NLS.bind( Messages
                .getString( "XMLSchemaFileImporter.NotValidSchema" ), new String[] { path } ) ); //$NON-NLS-1$
        }

        Attribute nameAttribute = element.attribute( NAME_TAG );
        if ( ( nameAttribute != null ) && ( !nameAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            return nameAttribute.getValue();
        }
        else
        {
            return getNameFromPath( path );
        }
    }


    // ── C-3PO Reads the Filename for Naming ──────────────────────────────────
    // If there's no name attribute, C-3PO strips the {@code .xml} extension from
    // the filename and uses the base name as the schema name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Derives a schema name from a file path by stripping the {@code .xml} extension.
     *
     * @param path  the full file path
     * @return      the base filename without extension — never null
     */
    private static String getNameFromPath( String path )
    {
        File file = new File( path );
        String fileName = file.getName();
        if ( fileName.endsWith( ".xml" ) ) //$NON-NLS-1$
        {
            String[] fileNameSplitted = fileName.split( "\\." ); //$NON-NLS-1$
            return fileNameSplitted[0];
        }

        return fileName;
    }


    // ── C-3PO Reads the Attribute-Type Section ───────────────────────────────
    // C-3PO finds the {@code <attributetypes>} group in the scroll and reads
    // each {@code <attributetype>} child in turn.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all {@code <attributetype>} elements nested under {@code <attributetypes>}
     * in the given element, adding each to the schema.
     *
     * @param element  the {@code <schema>} element containing the attribute types
     * @param schema   the schema to populate
     * @throws XMLSchemaFileImportException  if any attribute type is malformed
     */
    private static void readAttributeTypes( Element element, Schema schema ) throws XMLSchemaFileImportException
    {
        for ( Iterator<?> i = element.elementIterator( ATTRIBUTE_TYPES_TAG ); i.hasNext(); )
        {
            Element attributesTypesElement = ( Element ) i.next();
            for ( Iterator<?> i2 = attributesTypesElement.elementIterator( ATTRIBUTE_TYPE_TAG ); i2.hasNext(); )
            {
                readAttributeType( ( Element ) i2.next(), schema );
            }
        }
    }


    // ── C-3PO Decodes a Single Attribute-Type Entry ───────────────────────────
    // One {@code <attributetype>} element: C-3PO reads the OID (required), then
    // every optional field — names, description, superior, usage, syntax, flags.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads a single {@code <attributetype>} element and adds the resulting
     * {@link AttributeType} to the given schema.
     * The {@code oid} attribute is mandatory; all others are optional.
     *
     * @param element  the {@code <attributetype>} DOM element
     * @param schema   the schema to add to
     * @throws XMLSchemaFileImportException  if the OID is missing or a value is unconvertible
     */
    private static void readAttributeType( Element element, Schema schema ) throws XMLSchemaFileImportException
    {
        AttributeType at = null;

        // OID
        Attribute oidAttribute = element.attribute( OID_TAG );
        if ( ( oidAttribute != null ) && ( !oidAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            at = new AttributeType( oidAttribute.getValue() );
        }
        else
        {
            throw new XMLSchemaFileImportException( Messages.getString( "XMLSchemaFileImporter.NoOIDInAttribute" ) ); //$NON-NLS-1$
        }

        // Schema
        at.setSchemaName( schema.getSchemaName() );

        // Aliases
        Element aliasesElement = element.element( ALIASES_TAG );
        if ( aliasesElement != null )
        {
            List<String> aliases = new ArrayList<String>();
            for ( Iterator<?> i = aliasesElement.elementIterator( ALIAS_TAG ); i.hasNext(); )
            {
                Element aliasElement = ( Element ) i.next();
                aliases.add( aliasElement.getText() );
            }
            if ( aliases.size() >= 1 )
            {
                at.setNames( aliases.toArray( new String[0] ) );
            }
        }

        // Description
        Element descriptionElement = element.element( DESCRIPTION_TAG );
        if ( ( descriptionElement != null ) && ( !descriptionElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setDescription( descriptionElement.getText() );
        }

        // Superior
        Element superiorElement = element.element( SUPERIOR_TAG );
        if ( ( superiorElement != null ) && ( !superiorElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setSuperiorOid( superiorElement.getText() );
        }

        // Usage
        Element usageElement = element.element( USAGE_TAG );
        if ( ( usageElement != null ) && ( !usageElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            try
            {
                at.setUsage( UsageEnum.valueOf( usageElement.getText() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new XMLSchemaFileImportException( Messages
                    .getString( "XMLSchemaFileImporter.UnceonvertableAttribute" ), e ); //$NON-NLS-1$
            }
        }

        // Syntax
        Element syntaxElement = element.element( SYNTAX_TAG );
        if ( ( syntaxElement != null ) && ( !syntaxElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setSyntaxOid( syntaxElement.getText() );
        }

        // Syntax Length
        Element syntaxLengthElement = element.element( SYNTAX_LENGTH_TAG );
        if ( ( syntaxLengthElement != null ) && ( !syntaxLengthElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            try
            {
                at.setSyntaxLength( Long.parseLong( syntaxLengthElement.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                throw new XMLSchemaFileImportException( Messages
                    .getString( "XMLSchemaFileImporter.UnconvertableInteger" ), e ); //$NON-NLS-1$
            }
        }

        // Obsolete
        Attribute obsoleteAttribute = element.attribute( OBSOLETE_TAG );
        if ( ( obsoleteAttribute != null ) && ( !obsoleteAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setObsolete( readBoolean( obsoleteAttribute.getValue() ) );
        }

        // Single Value
        Attribute singleValueAttribute = element.attribute( SINGLE_VALUE_TAG );
        if ( ( singleValueAttribute != null ) && ( !singleValueAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setSingleValued( readBoolean( singleValueAttribute.getValue() ) );
        }

        // Collective
        Attribute collectiveAttribute = element.attribute( COLLECTIVE_TAG );
        if ( ( collectiveAttribute != null ) && ( !collectiveAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setCollective( readBoolean( collectiveAttribute.getValue() ) );
        }

        // No User Modification
        Attribute noUserModificationAttribute = element.attribute( NO_USER_MODIFICATION_TAG );
        if ( ( noUserModificationAttribute != null ) && ( !noUserModificationAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setUserModifiable( !readBoolean( noUserModificationAttribute.getValue() ) );
        }

        // Equality
        Element equalityElement = element.element( EQUALITY_TAG );
        if ( ( equalityElement != null ) && ( !equalityElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setEqualityOid( equalityElement.getText() );
        }

        // Ordering
        Element orderingElement = element.element( ORDERING_TAG );
        if ( ( orderingElement != null ) && ( !orderingElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setOrderingOid( orderingElement.getText() );
        }

        // Substring
        Element substringElement = element.element( SUBSTRING_TAG );
        if ( ( substringElement != null ) && ( !substringElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            at.setSubstringOid( substringElement.getText() );
        }

        // Adding the attribute type to the schema
        schema.addAttributeType( at );
    }


    // ── C-3PO Reads the Object-Class Section ─────────────────────────────────
    // C-3PO finds the {@code <objectclasses>} group and reads each child entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all {@code <objectclass>} elements nested under {@code <objectclasses>}
     * and adds each to the schema.
     *
     * @param element  the {@code <schema>} element
     * @param schema   the schema to populate
     * @throws XMLSchemaFileImportException  if any object class is malformed
     */
    private static void readObjectClasses( Element element, Schema schema ) throws XMLSchemaFileImportException
    {
        for ( Iterator<?> i = element.elementIterator( OBJECT_CLASSES_TAG ); i.hasNext(); )
        {
            Element objectClassesElement = ( Element ) i.next();
            for ( Iterator<?> i2 = objectClassesElement.elementIterator( OBJECT_CLASS_TAG ); i2.hasNext(); )
            {
                readObjectClass( ( Element ) i2.next(), schema );
            }
        }
    }


    // ── C-3PO Decodes a Single Object-Class Entry ─────────────────────────────
    // One {@code <objectclass>}: C-3PO reads OID, names, description, superiors,
    // type, obsolete flag, and mandatory/optional attribute lists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads a single {@code <objectclass>} element and adds the result to the schema.
     * The {@code oid} attribute is mandatory; all others are optional.
     *
     * @param element  the {@code <objectclass>} DOM element
     * @param schema   the schema to add to
     * @throws XMLSchemaFileImportException  if the OID is missing or a value is unconvertible
     */
    private static void readObjectClass( Element element, Schema schema ) throws XMLSchemaFileImportException
    {
        ObjectClass oc = null;

        // OID
        Attribute oidAttribute = element.attribute( OID_TAG );
        if ( ( oidAttribute != null ) && ( !oidAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            oc = new ObjectClass( oidAttribute.getValue() );
        }
        else
        {
            throw new XMLSchemaFileImportException( Messages.getString( "XMLSchemaFileImporter.NoOIDInClass" ) ); //$NON-NLS-1$
        }

        // Schema
        oc.setSchemaName( schema.getSchemaName() );

        // Aliases
        Element aliasesElement = element.element( ALIASES_TAG );
        if ( aliasesElement != null )
        {
            List<String> aliases = new ArrayList<String>();
            for ( Iterator<?> i = aliasesElement.elementIterator( ALIAS_TAG ); i.hasNext(); )
            {
                Element aliasElement = ( Element ) i.next();
                aliases.add( aliasElement.getText() );
            }
            if ( aliases.size() >= 1 )
            {
                oc.setNames( aliases.toArray( new String[0] ) );
            }
        }

        // Description
        Element descriptionElement = element.element( DESCRIPTION_TAG );
        if ( ( descriptionElement != null ) && ( !descriptionElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            oc.setDescription( descriptionElement.getText() );
        }

        // Superiors
        Element superiorsElement = element.element( SUPERIORS_TAG );
        if ( superiorsElement != null )
        {
            List<String> superiors = new ArrayList<String>();
            for ( Iterator<?> i = superiorsElement.elementIterator( SUPERIOR_TAG ); i.hasNext(); )
            {
                Element superiorElement = ( Element ) i.next();
                superiors.add( superiorElement.getText() );
            }
            if ( superiors.size() >= 1 )
            {
                oc.setSuperiorOids( superiors );
            }
        }

        // Class Type
        Element classTypeElement = element.element( TYPE_TAG );
        if ( ( classTypeElement != null ) && ( !classTypeElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            try
            {
                oc.setType( ObjectClassTypeEnum.valueOf( classTypeElement.getText() ) );
            }
            catch ( IllegalArgumentException e )
            {
                throw new XMLSchemaFileImportException(
                    Messages.getString( "XMLSchemaFileImporter.UnconvertableValue" ), e ); //$NON-NLS-1$
            }
        }

        // Obsolete
        Attribute obsoleteAttribute = element.attribute( OBSOLETE_TAG );
        if ( ( obsoleteAttribute != null ) && ( !obsoleteAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            oc.setObsolete( readBoolean( obsoleteAttribute.getValue() ) );
        }

        // Mandatory Attribute Types
        Element mandatoryElement = element.element( MANDATORY_TAG );
        if ( mandatoryElement != null )
        {
            List<String> mandatoryATs = new ArrayList<String>();
            for ( Iterator<?> i = mandatoryElement.elementIterator( ATTRIBUTE_TYPE_TAG ); i.hasNext(); )
            {
                Element attributeTypeElement = ( Element ) i.next();
                mandatoryATs.add( attributeTypeElement.getText() );
            }
            if ( mandatoryATs.size() >= 1 )
            {
                oc.setMustAttributeTypeOids( mandatoryATs );
            }
        }

        // Optional Attribute Types
        Element optionalElement = element.element( OPTIONAL_TAG );
        if ( optionalElement != null )
        {
            List<String> optionalATs = new ArrayList<String>();
            for ( Iterator<?> i = optionalElement.elementIterator( ATTRIBUTE_TYPE_TAG ); i.hasNext(); )
            {
                Element attributeTypeElement = ( Element ) i.next();
                optionalATs.add( attributeTypeElement.getText() );
            }
            if ( optionalATs.size() >= 1 )
            {
                oc.setMayAttributeTypeOids( optionalATs );
            }
        }

        // Adding the object class to the schema
        schema.addObjectClass( oc );
    }


    // ── C-3PO Reads the Matching-Rules Section ────────────────────────────────
    // C-3PO finds the {@code <matchingrules>} group and reads each child entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all {@code <matchingrule>} elements nested under {@code <matchingrules>}
     * and adds each to the schema.
     *
     * @param element  the {@code <schema>} element
     * @param schema   the schema to populate
     * @throws XMLSchemaFileImportException  if any matching rule is malformed
     */
    private static void readMatchingRules( Element element, Schema schema ) throws XMLSchemaFileImportException
    {
        for ( Iterator<?> i = element.elementIterator( MATCHING_RULES_TAG ); i.hasNext(); )
        {
            Element matchingRulesElement = ( Element ) i.next();
            for ( Iterator<?> i2 = matchingRulesElement.elementIterator( MATCHING_RULE_TAG ); i2.hasNext(); )
            {
                readMatchingRule( ( Element ) i2.next(), schema );
            }
        }
    }


    // ── C-3PO Decodes a Single Matching-Rule Entry ────────────────────────────
    // One {@code <matchingrule>}: C-3PO reads OID, aliases, description, obsolete
    // flag, and the associated syntax OID.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads a single {@code <matchingrule>} element and adds the result to the schema.
     *
     * @param element  the {@code <matchingrule>} DOM element
     * @param schema   the schema to add to
     * @throws XMLSchemaFileImportException  if the OID is missing
     */
    private static void readMatchingRule( Element element, Schema schema ) throws XMLSchemaFileImportException
    {
        MatchingRule mr = null;

        // OID
        Attribute oidAttribute = element.attribute( OID_TAG );
        if ( ( oidAttribute != null ) && ( !oidAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            mr = new MatchingRule( oidAttribute.getValue() );
        }
        else
        {
            throw new XMLSchemaFileImportException( Messages.getString( "XMLSchemaFileImporter.NoMatchingRuleForOID" ) ); //$NON-NLS-1$
        }

        // Schema
        mr.setSchemaName( schema.getSchemaName() );

        // Aliases
        Element aliasesElement = element.element( ALIASES_TAG );
        if ( aliasesElement != null )
        {
            List<String> aliases = new ArrayList<String>();
            for ( Iterator<?> i = aliasesElement.elementIterator( ALIAS_TAG ); i.hasNext(); )
            {
                Element aliasElement = ( Element ) i.next();
                aliases.add( aliasElement.getText() );
            }
            if ( aliases.size() >= 1 )
            {
                mr.setNames( aliases.toArray( new String[0] ) );
            }
        }

        // Description
        Element descriptionElement = element.element( DESCRIPTION_TAG );
        if ( ( descriptionElement != null ) && ( !descriptionElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            mr.setDescription( descriptionElement.getText() );
        }

        // Obsolete
        Attribute obsoleteAttribute = element.attribute( OBSOLETE_TAG );
        if ( ( obsoleteAttribute != null ) && ( !obsoleteAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            mr.setObsolete( readBoolean( obsoleteAttribute.getValue() ) );
        }

        // Syntax OID
        Element syntaxOidElement = element.element( SYNTAX_OID_TAG );
        if ( ( syntaxOidElement != null ) && ( !syntaxOidElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            mr.setSyntaxOid( syntaxOidElement.getText() );
        }

        // Adding the matching rule to the schema
        schema.addMatchingRule( mr );
    }


    // ── C-3PO Reads the Syntaxes Section ─────────────────────────────────────
    // C-3PO finds the {@code <syntaxes>} group and reads each child entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads all {@code <syntax>} elements nested under {@code <syntaxes>}
     * and adds each to the schema.
     *
     * @param element  the {@code <schema>} element
     * @param schema   the schema to populate
     * @throws XMLSchemaFileImportException  if any syntax is malformed
     */
    private static void readSyntaxes( Element element, Schema schema ) throws XMLSchemaFileImportException
    {
        for ( Iterator<?> i = element.elementIterator( SYNTAXES_TAG ); i.hasNext(); )
        {
            Element syntaxElement = ( Element ) i.next();
            for ( Iterator<?> i2 = syntaxElement.elementIterator( SYNTAX_TAG ); i2.hasNext(); )
            {
                readSyntax( ( Element ) i2.next(), schema );
            }
        }
    }


    // ── C-3PO Decodes a Single Syntax Entry ──────────────────────────────────
    // One {@code <syntax>}: C-3PO reads OID, aliases, description, obsolete flag,
    // and the human-readable flag.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads a single {@code <syntax>} element and adds the result to the schema.
     *
     * @param element  the {@code <syntax>} DOM element
     * @param schema   the schema to add to
     * @throws XMLSchemaFileImportException  if the OID is missing or a boolean is invalid
     */
    private static void readSyntax( Element element, Schema schema ) throws XMLSchemaFileImportException
    {
        LdapSyntax syntax = null;

        // OID
        Attribute oidAttribute = element.attribute( OID_TAG );
        if ( ( oidAttribute != null ) && ( !oidAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            syntax = new LdapSyntax( oidAttribute.getValue() );
        }
        else
        {
            throw new XMLSchemaFileImportException( Messages.getString( "XMLSchemaFileImporter.InvalidSyntaxForOID" ) ); //$NON-NLS-1$
        }

        // Schema
        syntax.setSchemaName( schema.getSchemaName() );

        // Aliases
        Element aliasesElement = element.element( ALIASES_TAG );
        if ( aliasesElement != null )
        {
            List<String> aliases = new ArrayList<String>();
            for ( Iterator<?> i = aliasesElement.elementIterator( ALIAS_TAG ); i.hasNext(); )
            {
                Element aliasElement = ( Element ) i.next();
                aliases.add( aliasElement.getText() );
            }
            if ( aliases.size() >= 1 )
            {
                syntax.setNames( aliases.toArray( new String[0] ) );
            }
        }

        // Description
        Element descriptionElement = element.element( DESCRIPTION_TAG );
        if ( ( descriptionElement != null ) && ( !descriptionElement.getText().equals( "" ) ) ) //$NON-NLS-1$
        {
            syntax.setDescription( descriptionElement.getText() );
        }

        // Obsolete
        Attribute obsoleteAttribute = element.attribute( OBSOLETE_TAG );
        if ( ( obsoleteAttribute != null ) && ( !obsoleteAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            syntax.setObsolete( readBoolean( obsoleteAttribute.getValue() ) );
        }

        // Human Readible
        Attribute humanReadibleAttribute = element.attribute( HUMAN_READABLE_TAG );
        if ( ( humanReadibleAttribute != null ) && ( !humanReadibleAttribute.getValue().equals( "" ) ) ) //$NON-NLS-1$
        {
            syntax.setHumanReadable( readBoolean( humanReadibleAttribute.getValue() ) );
        }

        // Adding the syntax to the schema
        schema.addSyntax( syntax );
    }


    // ── C-3PO Translates "true"/"false" Strings into Booleans ────────────────
    // A simple two-word vocabulary: "true" or "false".  If C-3PO gets any other
    // word he flags it as an untranslatable value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses a "true" or "false" string into a boolean.
     * Throws if any other value is encountered, since the XML format has no
     * other valid values for boolean attributes.
     *
     * @param value  the string to parse — must be "true" or "false"
     * @return       the corresponding boolean value
     * @throws XMLSchemaFileImportException  if the value is neither "true" nor "false"
     */
    private static boolean readBoolean( String value ) throws XMLSchemaFileImportException
    {
        if ( value.equals( BOOLEAN_TRUE ) )
        {
            return true;
        }
        else if ( value.equals( BOOLEAN_FALSE ) )
        {
            return false;
        }
        else
        {
            throw new XMLSchemaFileImportException( Messages.getString( "XMLSchemaFileImporter.76" ) ); //$NON-NLS-1$
        }
    }

    /**
     * This enum represents the different types of schema files.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum SchemaFileType
    {
        SINGLE, MULTIPLE
    };


    // ── C-3PO Peeks at the Scroll Type Before Reading It ─────────────────────
    // C-3PO glances at the root element before committing to a full read —
    // is this a single-schema scroll or a multi-schema bundle?  He returns
    // the type so the caller knows which method to call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Determines whether the XML file contains a single schema or multiple schemas
     * by inspecting its root element.
     * Use the returned {@link SchemaFileType} to decide whether to call
     * {@link #getSchema} or {@link #getSchemas}.
     *
     * @param inputStream  the stream to inspect — consumed by this call
     * @param path         the file path, used in error messages
     * @return             {@code SINGLE} or {@code MULTIPLE}
     * @throws XMLSchemaFileImportException  if the file can't be read or has an unrecognised root
     */
    public static SchemaFileType getSchemaFileType( InputStream inputStream, String path )
        throws XMLSchemaFileImportException
    {
        SAXReader reader = new SAXReader();
        Document document = null;
        try
        {
            document = reader.read( inputStream );
        }
        catch ( DocumentException e )
        {
            throw new XMLSchemaFileImportException( NLS.bind( Messages
                .getString( "XMLSchemaFileImporter.NotReadCorrectly" ), new String[] { path } ), e ); //$NON-NLS-1$
        }

        Element rootElement = document.getRootElement();
        if ( rootElement.getName().equals( SCHEMA_TAG ) )
        {
            return SchemaFileType.SINGLE;
        }
        else if ( rootElement.getName().equals( SCHEMAS_TAG ) )
        {
            return SchemaFileType.MULTIPLE;
        }
        else
        {
            throw new XMLSchemaFileImportException( NLS.bind( Messages
                .getString( "XMLSchemaFileImporter.NotValidSchema" ), new String[] { path } ) ); //$NON-NLS-1$
        }
    }
}
