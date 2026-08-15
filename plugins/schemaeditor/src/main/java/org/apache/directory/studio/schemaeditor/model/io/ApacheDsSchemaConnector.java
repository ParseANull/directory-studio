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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.constants.LdapConstants;
import org.apache.directory.api.ldap.model.constants.MetaSchemaConstants;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.LdapSyntax;
import org.apache.directory.api.ldap.model.schema.MatchingRule;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.io.ConnectionWrapper;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResult;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;


// ── CLASS: ApacheDsSchemaConnector — Han Jumping to Hyperspace via ApacheDS ──
// Han has a specific flight plan for reaching ApacheDS: he queries the
// "ou=schema" subtree, enumerates each named schema, then dives into that
// schema's sub-tree to collect attribute types, object classes, matching rules,
// and syntaxes.  It's a precise, ApacheDS-flavored hyperspace route — different
// from the generic fallback Han uses for other servers.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link SchemaConnector} that reads (and eventually writes) schema data from
 * an Apache Directory Server by walking its DIT-based schema sub-tree under
 * {@code ou=schema}.
 * It is the ApacheDS-specific jump route: it identifies whether the server is
 * actually ApacheDS by checking the {@code vendorName} root DSE attribute, then
 * fetches each named schema and its constituent schema objects.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApacheDsSchemaConnector extends AbstractSchemaConnector implements SchemaConnector
{
    /**
     * This enum represents the different types of nodes that can be found while
     * reading the schema from the DIT.
     */
    private enum SchemaNodeTypes
    {
        ATTRIBUTE_TYPE, OBJECT_CLASS, MATCHING_RULE, SYNTAX, UNKNOWN
    }


    // ── Han Jumps to the ApacheDS Schema Partition ───────────────────────────
    // Han punches the coordinates for the ApacheDS schema DIT into the nav
    // computer and makes the jump: first he finds all the named schema containers
    // under ou=schema, then for each one he dives in and retrieves its contents.
    // We search ou=schema for metaSchema entries, then call getSchema() for each,
    // collecting the resulting Schema objects into the project.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fetches the complete schema from an Apache Directory Server and stores it
     * in the given project's initial schema list.
     * We do a one-level search under {@code ou=schema} to find all named schemas,
     * then a subtree search within each to collect their elements.
     *
     * @param project  the project to populate — its connection must already be open
     * @param monitor  progress monitor so the user can see what's happening
     * @throws SchemaConnectorException  if the LDAP search or result parsing fails
     */
    public void importSchema( Project project, StudioProgressMonitor monitor )
        throws SchemaConnectorException
    {
        monitor.beginTask( Messages.getString( "ApacheDsSchemaConnector.FetchingSchema" ), 1 ); //$NON-NLS-1$
        List<Schema> schemas = new ArrayList<Schema>();
        project.setInitialSchema( schemas );
        ConnectionWrapper wrapper = project.getConnection().getConnectionWrapper();

        // Looking for all the defined schemas
        SearchControls constraintSearch = new SearchControls();
        constraintSearch.setSearchScope( SearchControls.ONELEVEL_SCOPE );

        StudioSearchResultEnumeration answer = wrapper
            .search( SchemaConstants.OU_SCHEMA, "(objectclass=metaSchema)", constraintSearch, DEREF_ALIAS_METHOD, //$NON-NLS-1$ //$NON-NLS-2$
                HANDLE_REFERALS_METHOD, null, monitor, null );

        if ( answer != null )
        {
            try
            {
                while ( answer.hasMore() )
                {
                    StudioSearchResult searchResult = answer.next();

                    // Getting the 'cn' Attribute
                    Attribute cnAttribute = searchResult.getEntry()
                        .get( SchemaConstants.CN_AT );

                    // Looping on the values
                    if ( cnAttribute != null )
                    {
                        for ( Value cnValue : cnAttribute )
                        {
                            Schema schema = getSchema( wrapper, cnValue.getString(), monitor );
                            schema.setProject( project );
                            schemas.add( schema );
                        }
                    }
                }
            }
            catch ( Exception e )
            {
                throw new SchemaConnectorException( e );
            }
        }

        monitor.worked( 1 );
    }


    // ── Han Sniffs the Root DSE for ApacheDS Markings ────────────────────────
    // Before committing to the ApacheDS route, Han pings the root DSE and reads
    // the vendorName attribute — "Apache Software Foundation" means it's ApacheDS
    // and he's cleared to use the schema DIT approach.
    // Any other vendor name (or a missing attribute) means this connector is the
    // wrong ship for this port, and we return false so the plugin tries a different one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Probes the root DSE to determine whether this server is Apache Directory Server.
     * We read the {@code vendorName} operational attribute and check whether it
     * equals "Apache Software Foundation" (case-insensitive).
     *
     * @param connection  the LDAP connection to probe
     * @param monitor     progress monitor for the search
     * @return            true if and only if the server identifies as ApacheDS
     */
    public boolean isSuitableConnector( Connection connection, StudioProgressMonitor monitor )
    {
        ConnectionWrapper wrapper = connection.getConnectionWrapper();

        SearchControls constraintSearch = new SearchControls();
        constraintSearch.setSearchScope( SearchControls.OBJECT_SCOPE );
        constraintSearch.setReturningAttributes( new String[]
            { SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES } );

        StudioSearchResultEnumeration answer = wrapper.search( "", LdapConstants.OBJECT_CLASS_STAR, constraintSearch, //$NON-NLS-1$ //$NON-NLS-2$
            DEREF_ALIAS_METHOD, HANDLE_REFERALS_METHOD, null, monitor, null );

        if ( answer != null )
        {
            try
            {
                if ( answer.hasMore() )
                {
                    Entry entry = answer.next().getEntry();

                    Attribute vendorNameAttribute = entry.get( SchemaConstants.VENDOR_NAME_AT );

                    if ( vendorNameAttribute == null )
                    {
                        return false;
                    }

                    if ( vendorNameAttribute.size() != 1 )
                    {
                        return false;
                    }

                    String vendorName = null;
                    try
                    {
                        vendorName = vendorNameAttribute.getString();
                    }
                    catch ( LdapInvalidAttributeValueException e )
                    {
                        return false;
                    }

                    return ( ( vendorName != null ) && vendorName.equalsIgnoreCase( "Apache Software Foundation" ) ); //$NON-NLS-1$
                }
            }
            catch ( LdapException e )
            {
                monitor.reportError( e );
            }
        }

        return false;
    }


    // ── Han Dives into a Single Schema's Corridor ────────────────────────────
    // Han enters the named schema's section of the DIT ("cn=core,ou=schema"),
    // reads every entry he finds, classifies it by type, and builds up the
    // Schema object like filling a cargo hold.
    // We do a subtree search under the schema's DN and dispatch each entry to
    // the right createX() helper based on its objectClass.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fetches all schema elements under the named schema's DIT node and assembles
     * them into a {@link Schema} object.
     * Each LDAP entry is classified as an attribute type, object class, matching
     * rule, or syntax and handed off to the corresponding factory method.
     *
     * @param wrapper  the LDAP connection wrapper to use for searching
     * @param name     the schema name (e.g. "core", "system")
     * @param monitor  progress monitor
     * @return         a fully populated Schema — never null
     * @throws LdapException  if the search or result traversal fails
     */
    private static Schema getSchema( ConnectionWrapper wrapper, String name, StudioProgressMonitor monitor )
        throws LdapException
    {
        monitor.subTask( name );

        // Creating the schema
        Schema schema = new Schema( name );

        // Looking for the nodes of the schema
        SearchControls constraintSearch = new SearchControls();
        constraintSearch.setSearchScope( SearchControls.SUBTREE_SCOPE );

        StudioSearchResultEnumeration answer = wrapper.search( "cn=" + name + ", ou=schema", LdapConstants.OBJECT_CLASS_STAR, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            constraintSearch, DEREF_ALIAS_METHOD, HANDLE_REFERALS_METHOD, null, monitor, null );

        if ( answer != null )
        {
            try
            {
                while ( answer.hasMore() )
                {
                    Entry entry = answer.next().getEntry();

                    switch ( getNodeType( entry ) )
                    {
                        case ATTRIBUTE_TYPE:
                            AttributeType at = createAttributeType( entry );
                            at.setSchemaName( name );
                            schema.addAttributeType( at );
                            break;

                        case OBJECT_CLASS:
                            ObjectClass oc = createObjectClass( entry );
                            oc.setSchemaName( name );
                            schema.addObjectClass( oc );
                            break;

                        case MATCHING_RULE:
                            MatchingRule mr = createMatchingRule( entry );
                            mr.setSchemaName( name );
                            schema.addMatchingRule( mr );
                            break;

                        case SYNTAX:
                            LdapSyntax syntax = createSyntax( entry );
                            syntax.setSchemaName( name );
                            schema.addSyntax( syntax );
                            break;

                        default:
                            break;
                    }
                }
            }
            catch ( LdapInvalidAttributeValueException e )
            {
                monitor.reportError( e );
            }
        }

        return schema;
    }


    // ── Han Reads the Cargo Bay Label ─────────────────────────────────────────
    // Each crate in the cargo hold has a label: ATTRIBUTE_TYPE, OBJECT_CLASS, etc.
    // Han checks the objectClass attribute on the LDAP entry to figure out which
    // type of schema element this crate contains.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Classifies a DIT entry as an attribute type, object class, matching rule,
     * syntax, or unknown, by inspecting its objectClass attribute.
     *
     * @param entry  the LDAP entry to classify
     * @return       the corresponding {@link SchemaNodeTypes} value
     */
    private static SchemaNodeTypes getNodeType( Entry entry )
    {
        if ( entry.hasObjectClass( SchemaConstants.META_ATTRIBUTE_TYPE_OC ) )
        {
            return SchemaNodeTypes.ATTRIBUTE_TYPE;
        }
        else if ( entry.hasObjectClass( SchemaConstants.META_OBJECT_CLASS_OC ) )
        {
            return SchemaNodeTypes.OBJECT_CLASS;
        }
        else if ( entry.hasObjectClass( SchemaConstants.META_MATCHING_RULE_OC ) )
        {
            return SchemaNodeTypes.MATCHING_RULE;
        }
        else if ( entry.hasObjectClass( SchemaConstants.META_SYNTAX_OC ) )
        {
            return SchemaNodeTypes.SYNTAX;
        }
        else
        {
            return SchemaNodeTypes.UNKNOWN;
        }
    }


    // ── Han Unpacks an Attribute-Type Crate ──────────────────────────────────
    // Han pries open the crate labelled ATTRIBUTE_TYPE and reads each property
    // off the packing slip — OID, names, syntax, matching rules, flags.
    // We read the metaSchema attributes from the entry and set them on a new
    // AttributeType instance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link AttributeType} from a DIT entry in the ApacheDS schema partition.
     * We read the m-oid, m-name, m-syntax, etc. attributes and set each on a new instance.
     *
     * @param entry  the LDAP entry representing the attribute type
     * @return       a populated AttributeType — never null
     * @throws LdapInvalidAttributeValueException  if any attribute value can't be read
     */
    private static AttributeType createAttributeType( Entry entry ) throws LdapInvalidAttributeValueException
    {
        AttributeType at = new AttributeType( getStringValue( entry, MetaSchemaConstants.M_OID_AT ) );
        at.setNames( getStringValues( entry, MetaSchemaConstants.M_NAME_AT ) );
        at.setDescription( getStringValue( entry, MetaSchemaConstants.M_DESCRIPTION_AT ) );
        at.setObsolete( getBooleanValue( entry, MetaSchemaConstants.M_OBSOLETE_AT ) );
        at.setSuperiorOid( getStringValue( entry, MetaSchemaConstants.M_SUP_ATTRIBUTE_TYPE_AT ) );
        at.setUsage( getUsage( entry ) );
        at.setSyntaxOid( getStringValue( entry, MetaSchemaConstants.M_SYNTAX_AT ) );
        at.setSyntaxLength( getSyntaxLength( entry ) );
        at.setCollective( getBooleanValue( entry, MetaSchemaConstants.M_COLLECTIVE_AT ) );
        at.setSingleValued( getBooleanValue( entry, MetaSchemaConstants.M_SINGLE_VALUE_AT ) );
        at.setUserModifiable( getBooleanValue( entry, MetaSchemaConstants.M_NO_USER_MODIFICATION_AT ) );
        at.setEqualityOid( getStringValue( entry, MetaSchemaConstants.M_EQUALITY_AT ) );
        at.setOrderingOid( getStringValue( entry, MetaSchemaConstants.M_ORDERING_AT ) );
        at.setSubstringOid( getStringValue( entry, MetaSchemaConstants.M_SUBSTR_AT ) );

        return at;
    }


    // ── Han Unpacks an Object-Class Crate ────────────────────────────────────
    // Next crate: OBJECT_CLASS.  Han reads off the OID, names, type, superiors,
    // mandatory and optional attribute lists from the packing slip.
    // We build an ObjectClass from the meta-schema attributes on the entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link ObjectClass} from a DIT entry in the ApacheDS schema partition.
     *
     * @param sr  the LDAP entry representing the object class
     * @return    a populated ObjectClass — never null
     * @throws LdapInvalidAttributeValueException  if any attribute value can't be read
     */
    private static ObjectClass createObjectClass( Entry sr ) throws LdapInvalidAttributeValueException
    {
        ObjectClass oc = new ObjectClass( getStringValue( sr, MetaSchemaConstants.M_OID_AT ) );
        oc.setNames( getStringValues( sr, MetaSchemaConstants.M_NAME_AT ) );
        oc.setDescription( getStringValue( sr, MetaSchemaConstants.M_DESCRIPTION_AT ) );
        oc.setObsolete( getBooleanValue( sr, MetaSchemaConstants.M_OBSOLETE_AT ) );
        oc.setSuperiorOids( getStringValues( sr, MetaSchemaConstants.M_SUP_OBJECT_CLASS_AT ) );
        oc.setType( getType( sr ) );
        oc.setMayAttributeTypeOids( getStringValues( sr, MetaSchemaConstants.M_MAY_AT ) );
        oc.setMustAttributeTypeOids( getStringValues( sr, MetaSchemaConstants.M_MUST_AT ) );

        return oc;
    }


    // ── Han Unpacks a Matching-Rule Crate ────────────────────────────────────
    // Another crate: MATCHING_RULE.  Han reads OID, names, description, obsolete
    // flag, and the associated syntax OID from the packing slip.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds a {@link MatchingRule} from a DIT entry in the ApacheDS schema partition.
     *
     * @param entry  the LDAP entry representing the matching rule
     * @return       a populated MatchingRule — never null
     * @throws LdapInvalidAttributeValueException  if any attribute value can't be read
     */
    private static MatchingRule createMatchingRule( Entry entry ) throws LdapInvalidAttributeValueException
    {
        MatchingRule mr = new MatchingRule( getStringValue( entry, MetaSchemaConstants.M_OID_AT ) );
        mr.setNames( getStringValues( entry, MetaSchemaConstants.M_NAME_AT ) );
        mr.setDescription( getStringValue( entry, MetaSchemaConstants.M_DESCRIPTION_AT ) );
        mr.setObsolete( getBooleanValue( entry, MetaSchemaConstants.M_OBSOLETE_AT ) );
        mr.setSyntaxOid( getStringValue( entry, MetaSchemaConstants.M_SYNTAX_AT ) );

        return mr;
    }


    // ── Han Unpacks a Syntax Crate ────────────────────────────────────────────
    // Last crate type: SYNTAX.  Han reads OID, names, description, obsolete
    // flag, and the human-readable flag from the packing slip.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link LdapSyntax} from a DIT entry in the ApacheDS schema partition.
     *
     * @param entry  the LDAP entry representing the syntax
     * @return       a populated LdapSyntax — never null
     * @throws LdapInvalidAttributeValueException  if any attribute value can't be read
     */
    private static LdapSyntax createSyntax( Entry entry ) throws LdapInvalidAttributeValueException
    {
        LdapSyntax syntax = new LdapSyntax( getStringValue( entry, MetaSchemaConstants.M_OID_AT ) );
        syntax.setNames( getStringValues( entry, MetaSchemaConstants.M_NAME_AT ) );
        syntax.setDescription( getStringValue( entry, MetaSchemaConstants.M_DESCRIPTION_AT ) );
        syntax.setObsolete( getBooleanValue( entry, MetaSchemaConstants.M_OBSOLETE_AT ) );
        syntax.setHumanReadable( isHumanReadable( entry ) );

        return syntax;
    }


    // ── Han Reads the "Usage" Label off the Crate ────────────────────────────
    // Some crates are marked for USER_APPLICATIONS, some for DIRECTORY_OPERATION;
    // Han reads the usage label and returns the right UsageEnum value.
    // If the label is missing or unreadable we default to USER_APPLICATIONS.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads the usage value from the LDAP entry's m-usage attribute.
     * Defaults to {@link UsageEnum#USER_APPLICATIONS} if the attribute is absent or invalid.
     *
     * @param entry  the LDAP entry to read from
     * @return       the UsageEnum value — never null
     * @throws LdapInvalidAttributeValueException  if the raw attribute value can't be read as a string
     */
    private static UsageEnum getUsage( Entry entry ) throws LdapInvalidAttributeValueException
    {
        Attribute at = entry.get( MetaSchemaConstants.M_USAGE_AT );

        if ( at == null )
        {
            return UsageEnum.USER_APPLICATIONS;
        }
        else
        {
            try
            {
                return UsageEnum.getUsage( at.getString() );
            }
            catch ( IllegalArgumentException e )
            {
                return UsageEnum.USER_APPLICATIONS;
            }
            catch ( NullPointerException e )
            {
                return UsageEnum.USER_APPLICATIONS;
            }
        }
    }


    // ── Han Reads the Size Sticker on a Syntax Crate ─────────────────────────
    // Some crates have a maximum size sticker; Han reads the integer off it.
    // If there's no sticker (attribute is absent) he returns -1 to signal "no limit."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads the syntax length (m-length attribute) from the LDAP entry.
     * Returns -1 if the attribute is absent or not a valid integer.
     *
     * @param entry  the LDAP entry to read from
     * @return       the syntax length as a positive int, or -1 if not present
     * @throws LdapInvalidAttributeValueException  if the raw attribute value can't be read
     */
    private static int getSyntaxLength( Entry entry ) throws LdapInvalidAttributeValueException
    {
        Attribute at = entry.get( MetaSchemaConstants.M_LENGTH_AT );

        if ( at == null )
        {
            return -1;
        }
        else
        {
            try
            {
                return Integer.parseInt( at.getString() );
            }
            catch ( NumberFormatException e )
            {
                return -1;
            }
        }
    }


    // ── Han Reads a Single Text Label ─────────────────────────────────────────
    // Han reads the plain-text label on a specific slot of the packing slip.
    // Returns null if the slot is blank (attribute absent).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string value of the named attribute from the given entry.
     * Returns null if the attribute is not present.
     *
     * @param entry          the LDAP entry to read from
     * @param schemaElement  the attribute name to look up
     * @return               the attribute's string value, or null if absent
     * @throws LdapInvalidAttributeValueException  if the value can't be decoded as a string
     */
    private static String getStringValue( Entry entry, String schemaElement ) throws LdapInvalidAttributeValueException
    {
        Attribute at = entry.get( schemaElement );

        if ( at == null )
        {
            return null;
        }
        else
        {
            return at.getString();
        }
    }


    // ── Han Reads a Yes/No Checkbox ───────────────────────────────────────────
    // Some fields on the packing slip are just checkboxes — TRUE or FALSE.
    // Han reads the box; if it's blank he assumes false.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the boolean value of the named attribute from the given entry.
     * Returns false if the attribute is absent.
     *
     * @param entry          the LDAP entry to read from
     * @param schemaElement  the attribute name to look up
     * @return               the parsed boolean value, or false if absent
     * @throws LdapInvalidAttributeValueException  if the value can't be decoded as a string
     */
    private static boolean getBooleanValue( Entry entry, String schemaElement ) throws LdapInvalidAttributeValueException
    {
        Attribute at = entry.get( schemaElement );

        if ( at == null )
        {
            return false;
        }
        else
        {
            return Boolean.parseBoolean( at.getString() );
        }
    }


    // ── Han Reads a Multi-Value List off the Packing Slip ────────────────────
    // Some fields list multiple values — e.g. all the names an attribute type
    // goes by.  Han reads every line and returns them as a list.
    // We stream the attribute's values and collect each as a String.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns all string values of the named attribute as a list.
     * Returns an empty list if the attribute is absent.
     *
     * @param entry          the LDAP entry to read from
     * @param schemaElement  the attribute name to look up
     * @return               a List of string values — never null, may be empty
     */
    private static List<String> getStringValues( Entry entry, String schemaElement )
    {
        Attribute at = entry.get( schemaElement );
        Spliterator<Value> spliterator = Optional.ofNullable( at ).map( Attribute::spliterator ).orElseGet( Spliterators::emptySpliterator );
        return StreamSupport.stream( spliterator, false ).map( Value::getString ).collect( Collectors.toList() );
    }


    // ── Han Reads the Department Classification Label ─────────────────────────
    // Crates labelled STRUCTURAL, ABSTRACT, or AUXILIARY are handled differently;
    // Han reads the classification and returns the right ObjectClassTypeEnum.
    // Defaults to STRUCTURAL if the label is missing or unrecognised.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads the object class type (STRUCTURAL, ABSTRACT, or AUXILIARY) from the entry.
     * Defaults to {@link ObjectClassTypeEnum#STRUCTURAL} if the attribute is absent or invalid.
     *
     * @param entry  the LDAP entry to read from
     * @return       the ObjectClassTypeEnum value — never null
     * @throws LdapInvalidAttributeValueException  if the raw value can't be decoded
     */
    private static ObjectClassTypeEnum getType( Entry entry ) throws LdapInvalidAttributeValueException
    {
        Attribute at = entry.get( MetaSchemaConstants.M_TYPE_OBJECT_CLASS_AT );

        if ( at == null )
        {
            return ObjectClassTypeEnum.STRUCTURAL;
        }
        else
        {
            try
            {
                return ObjectClassTypeEnum.getClassType( at.getString() );
            }
            catch ( IllegalArgumentException e )
            {
                return ObjectClassTypeEnum.STRUCTURAL;
            }
            catch ( NullPointerException e )
            {
                return ObjectClassTypeEnum.STRUCTURAL;
            }
        }
    }


    // ── Han Checks the "Human Readable" Sticker ──────────────────────────────
    // Some syntax crates are marked X-NOT-HUMAN-READABLE; Han checks the sticker
    // and returns the inverse — if it's marked not-human-readable, we return false.
    // If the sticker is absent we default to false (unknown readability).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Determines whether the syntax in the given entry is human-readable by
     * checking the {@code X-NOT-HUMAN-READABLE} extension attribute.
     * Returns false (not readable) if the attribute is absent.
     *
     * @param entry  the LDAP entry to check
     * @return       true if the syntax is human-readable, false otherwise
     * @throws LdapInvalidAttributeValueException  if the raw value can't be decoded
     */
    private static boolean isHumanReadable( Entry entry ) throws LdapInvalidAttributeValueException
    {
        Attribute at = entry.get( MetaSchemaConstants.X_NOT_HUMAN_READABLE_AT );

        if ( at == null )
        {
            return false;
        }
        else
        {
            return !Boolean.parseBoolean( at.getString() );
        }
    }


    // ── Han Parks the Falcon — Export Not Yet Wired ──────────────────────────
    // Han lands at the destination and realises nobody has told him what to
    // unload yet — the export docking bay is still under construction.
    // This method is a placeholder; schema export to ApacheDS is not yet implemented.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Exports the project's schema to the connected ApacheDS instance.
     * Not yet implemented — this is a placeholder for a future feature.
     *
     * @param project  the project whose schema should be exported
     * @param monitor  progress monitor
     * @throws SchemaConnectorException  not currently thrown, but declared for the interface
     */
    public void exportSchema( Project project, StudioProgressMonitor monitor )
        throws SchemaConnectorException
    {
        // TODO Auto-generated method stub
    }
}
