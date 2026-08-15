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


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.constants.LdapConstants;
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
import org.apache.directory.api.ldap.model.schema.parsers.AttributeTypeDescriptionSchemaParser;
import org.apache.directory.api.ldap.model.schema.parsers.LdapSyntaxDescriptionSchemaParser;
import org.apache.directory.api.ldap.model.schema.parsers.MatchingRuleDescriptionSchemaParser;
import org.apache.directory.api.ldap.model.schema.parsers.ObjectClassDescriptionSchemaParser;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.core.io.ConnectionWrapper;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.osgi.util.NLS;


// ── CLASS: GenericSchemaConnector — Han's Fallback Hyperspace Route ───────────
// When Han doesn't know anything about the destination planet he uses the
// standard subspace beacon approach: he reads the root DSE for the
// subschemaSubentry pointer, then reads that one special entry to get all the
// schema data in one big subschema blob.  It's the RFC 4512 way — any LDAP
// server that follows the standard should respond.  This is the last resort
// connector, suitable for everything that isn't specifically ApacheDS.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A generic {@link SchemaConnector} that reads schema from any RFC 4512-compliant
 * LDAP server by fetching the subschema subentry.
 * We find the subschema DN via the root DSE's {@code subschemaSubentry} attribute,
 * then read that entry for attribute types, object classes, matching rules, and syntaxes.
 * This is the fallback — it works with virtually any LDAP server, unlike the
 * ApacheDS connector which relies on ApacheDS's DIT-based schema layout.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GenericSchemaConnector extends AbstractSchemaConnector implements SchemaConnector
{
    // ── Han Reads the Root DSE's Nav Beacon and Jumps ────────────────────────
    // Han pings the root DSE for the subschemaSubentry pointer, then jumps
    // straight to that DN and reads the single subschema entry that contains
    // everything: attribute types, object classes, syntaxes, matching rules.
    // We parse each section in turn, creating a single Schema object that holds
    // the full schema published by the server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Imports the schema from any RFC 4512-compliant LDAP server using the
     * subschema subentry approach.
     * We look up {@code subschemaSubentry} from the root DSE, then read that
     * entry for all schema element attributes.
     *
     * @param project  the project to populate — its connection must be open
     * @param monitor  progress monitor
     * @throws SchemaConnectorException  if the server can't be reached or the
     *                                   schema data can't be parsed
     */
    public void importSchema( Project project, StudioProgressMonitor monitor )
        throws SchemaConnectorException
    {
        monitor.beginTask( Messages.getString( "GenericSchemaConnector.FetchingSchema" ), 1 ); //$NON-NLS-1$
        List<Schema> schemas = new ArrayList<Schema>();
        project.setInitialSchema( schemas );
        ConnectionWrapper wrapper = project.getConnection().getConnectionWrapper();

        SearchControls constraintSearch = new SearchControls();
        constraintSearch.setSearchScope( SearchControls.OBJECT_SCOPE );
        constraintSearch.setReturningAttributes( new String[]
            {
                SchemaConstants.ATTRIBUTE_TYPES_AT,
                SchemaConstants.COMPARATORS_AT,
                SchemaConstants.DIT_CONTENT_RULES_AT,
                SchemaConstants.DIT_STRUCTURE_RULES_AT,
                SchemaConstants.LDAP_SYNTAXES_AT,
                SchemaConstants.MATCHING_RULES_AT,
                SchemaConstants.MATCHING_RULE_USE_AT,
                SchemaConstants.NAME_FORMS_AT,
                SchemaConstants.NORMALIZERS_AT,
                SchemaConstants.OBJECT_CLASSES_AT,
                SchemaConstants.SYNTAX_CHECKERS_AT
        } );
        String schemaDn = getSubschemaSubentry( wrapper, monitor );
        StudioSearchResultEnumeration answer = wrapper.search( schemaDn, "(objectclass=subschema)", constraintSearch, //$NON-NLS-1$
            DEREF_ALIAS_METHOD, HANDLE_REFERALS_METHOD, null, monitor, null );

        if ( answer != null )
        {
            try
            {
                // Looping the results
                while ( answer.hasMore() )
                {
                    // Creating the schema
                    Schema schema = new Schema( "schema" ); //$NON-NLS-1$
                    schema.setProject( project );
                    schemas.add( schema );

                    getSchema( schema, wrapper, answer.next().getEntry(), monitor );
                }
            }
            catch ( SchemaConnectorException e )
            {
                throw e;
            }
            catch ( Exception e )
            {
                throw new SchemaConnectorException( e );
            }
        }

        monitor.worked( 1 );
    }


    // ── Han Checks Whether the Beacon is Reachable ───────────────────────────
    // Before committing to the generic route, Han checks whether the root DSE
    // actually advertises a subschemaSubentry attribute — if it does, we can
    // use this connector; if not, we can't.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tests whether the server advertises a {@code subschemaSubentry} attribute
     * in its root DSE — if it does, this generic connector can handle it.
     *
     * @param connection  the LDAP connection to probe
     * @param monitor     progress monitor
     * @return            true if a subschema subentry DN was found
     */
    public boolean isSuitableConnector( Connection connection, StudioProgressMonitor monitor )
    {
        return getSubschemaSubentry( connection.getConnectionWrapper(), monitor ) != null;
    }


    // ── Han Reads the Subspace Beacon DN from the Root DSE ───────────────────
    // Han pings the root DSE asking for the subschemaSubentry attribute and reads
    // back the DN of the special schema entry.  If the attribute is absent or
    // multi-valued the server doesn't follow the standard, so we return null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads the {@code subschemaSubentry} attribute from the root DSE.
     * Returns null if the attribute is absent, multi-valued, or unreadable.
     *
     * @param wrapper  the connection wrapper to search with
     * @param monitor  progress monitor for the search
     * @return         the subschema subentry DN, or null if not found
     */
    private static String getSubschemaSubentry( ConnectionWrapper wrapper, StudioProgressMonitor monitor )
    {
        SearchControls constraintSearch = new SearchControls();
        constraintSearch.setSearchScope( SearchControls.OBJECT_SCOPE );
        constraintSearch.setReturningAttributes( new String[]
            { "subschemaSubentry" } ); //$NON-NLS-1$

        StudioSearchResultEnumeration answer = wrapper.search( "", LdapConstants.OBJECT_CLASS_STAR, constraintSearch, //$NON-NLS-1$ //$NON-NLS-2$
            DEREF_ALIAS_METHOD, HANDLE_REFERALS_METHOD, null, monitor, null );

        if ( answer != null )
        {
            try
            {
                if ( answer.hasMore() )
                {
                    Entry searchResult = answer.next().getEntry();

                    Attribute subschemaSubentryAttribute = searchResult.get( "subschemaSubentry" ); //$NON-NLS-1$
                    if ( subschemaSubentryAttribute == null )
                    {
                        return null;
                    }

                    if ( subschemaSubentryAttribute.size() != 1 )
                    {
                        return null;
                    }

                    String subschemaSubentry = null;

                    try
                    {
                        subschemaSubentry = subschemaSubentryAttribute.getString();
                    }
                    catch ( LdapInvalidAttributeValueException e )
                    {
                        return null;
                    }

                    return subschemaSubentry;
                }
            }
            catch ( LdapException e )
            {
                monitor.reportError( e );
            }
        }

        return null;
    }


    // ── Han Opens the Cargo Manifest and Unpacks Everything ──────────────────
    // Han reads the subschema subentry — one massive cargo manifest — and
    // walks through each section: attribute types, object classes, syntaxes,
    // matching rules.  He parses each line-item description string and builds
    // the corresponding schema object.  Anything he can't parse he logs and
    // counts; too many errors and he throws so the caller knows.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Parses a subschema subentry into schema objects and populates the given Schema.
     * Each multi-valued LDAP attribute (attributeTypes, objectClasses, etc.) contains
     * RFC 4512 description strings; we parse each one and add it to the schema.
     * If parsing fails for individual entries we log and count them; if the count
     * is non-zero at the end we throw a SchemaConnectorException.
     *
     * @param schema   the Schema to populate
     * @param wrapper  the connection wrapper (unused here, kept for signature consistency)
     * @param entry    the subschema subentry to parse
     * @param monitor  progress monitor
     * @throws SchemaConnectorException  if any elements could not be parsed
     */
    private static void getSchema( Schema schema, ConnectionWrapper wrapper, Entry entry,
        StudioProgressMonitor monitor ) throws SchemaConnectorException
    {
        // The counter for parser exceptions
        int parseErrorCount = 0;

        Attribute attributeTypesAttribute = entry.get( SchemaConstants.ATTRIBUTE_TYPES_AT );
        if ( attributeTypesAttribute != null )
        {
                for ( Value value : attributeTypesAttribute )
                {
                    try
                    {
                        AttributeTypeDescriptionSchemaParser parser = new AttributeTypeDescriptionSchemaParser();
                        parser.setQuirksMode( true );

                        AttributeType atd = parser.parse( value.getString() );

                        AttributeType impl = new AttributeType( atd.getOid() );
                        impl.setNames( atd.getNames().toArray( new String[0] ) );
                        impl.setDescription( atd.getDescription() );
                        impl.setSuperiorOid( atd.getSuperiorOid() );
                        impl.setUsage( atd.getUsage() );
                        impl.setSyntaxOid( atd.getSyntaxOid() );
                        impl.setSyntaxLength( atd.getSyntaxLength() );
                        impl.setObsolete( atd.isObsolete() );
                        impl.setCollective( atd.isCollective() );
                        impl.setSingleValued( atd.isSingleValued() );
                        impl.setUserModifiable( atd.isUserModifiable() );
                        impl.setEqualityOid( atd.getEqualityOid() );
                        impl.setOrderingOid( atd.getOrderingOid() );
                        impl.setSubstringOid( atd.getSubstringOid() );
                        impl.setSchemaName( schema.getSchemaName() );

                        // Active Directory hack
                        if ( impl.getSyntaxOid() != null && "OctetString".equalsIgnoreCase( impl.getSyntaxOid() ) ) //$NON-NLS-1$
                        {
                            impl.setSyntaxOid( SchemaConstants.OCTET_STRING_SYNTAX );
                        }

                        schema.addAttributeType( impl );
                    }
                    catch ( ParseException e )
                    {
                        // Logging the exception and incrementing the counter
                        PluginUtils.logError( "Unable to parse the attribute type.", e ); //$NON-NLS-1$
                        parseErrorCount++;
                    }
            }
        }

        Attribute objectClassesAttribute = entry.get( SchemaConstants.OBJECT_CLASSES_AT );
        if ( objectClassesAttribute != null )
        {
                for ( Value value : objectClassesAttribute )
                {
                    try
                    {
                        ObjectClassDescriptionSchemaParser parser = new ObjectClassDescriptionSchemaParser();
                        parser.setQuirksMode( true );
                        ObjectClass ocd = parser.parse( value.getString() );

                        ObjectClass impl = new ObjectClass( ocd.getOid() );
                        impl.setNames( ocd.getNames().toArray( new String[0] ) );
                        impl.setDescription( ocd.getDescription() );
                        impl.setSuperiorOids( ocd.getSuperiorOids() );
                        impl.setType( ocd.getType() );
                        impl.setObsolete( ocd.isObsolete() );
                        impl.setMustAttributeTypeOids( ocd.getMustAttributeTypeOids() );
                        impl.setMayAttributeTypeOids( ocd.getMayAttributeTypeOids() );
                        impl.setSchemaName( schema.getSchemaName() );

                        schema.addObjectClass( impl );
                    }
                    catch ( ParseException e )
                    {
                        // Logging the exception and incrementing the counter
                        PluginUtils.logError( "Unable to parse the object class.", e ); //$NON-NLS-1$
                        parseErrorCount++;
                    }
                }
        }

        Attribute ldapSyntaxesAttribute = entry.get( SchemaConstants.LDAP_SYNTAXES_AT );
        if ( ldapSyntaxesAttribute != null )
        {
                for ( Value value : ldapSyntaxesAttribute )
                {
                    try
                    {
                        LdapSyntaxDescriptionSchemaParser parser = new LdapSyntaxDescriptionSchemaParser();
                        parser.setQuirksMode( true );
                        LdapSyntax lsd = parser.parse( value.getString() );

                        LdapSyntax impl = new LdapSyntax( lsd.getOid() );
                        impl.setDescription( lsd.getDescription() );
                        impl.setNames( new String[]
                            { lsd.getDescription() } );
                        //impl.setObsolete( lsd.isObsolete() );
                        impl.setHumanReadable( true );
                        impl.setSchemaName( schema.getSchemaName() );

                        schema.addSyntax( impl );
                    }
                    catch ( ParseException e )
                    {
                        // Logging the exception and incrementing the counter
                        PluginUtils.logError( "Unable to parse the syntax.", e ); //$NON-NLS-1$
                        parseErrorCount++;
                    }
                }
        }

        // if online: assume all received syntaxes in attributes are valid -> create dummy syntaxes if missing
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            String syntaxOid = at.getSyntaxOid();

            if ( ( syntaxOid != null ) && ( schema.getSyntax( syntaxOid ) == null ) )
            {
                LdapSyntax impl = new LdapSyntax( syntaxOid );
                impl.setSchemaName( schema.getSchemaName() );
                String oidDescription = Utils.getOidDescription( syntaxOid );
                impl.setDescription( oidDescription != null ? oidDescription : "Dummy" ); //$NON-NLS-1$
                impl.setNames( new String[]
                    { impl.getDescription() } );
                schema.addSyntax( impl );
            }
        }

        Attribute matchingRulesAttribute = entry.get( SchemaConstants.MATCHING_RULES_AT );
        if ( matchingRulesAttribute != null )
        {
                for ( Value value : matchingRulesAttribute )
                {
                    try
                    {
                        MatchingRuleDescriptionSchemaParser parser = new MatchingRuleDescriptionSchemaParser();
                        parser.setQuirksMode( true );
                        MatchingRule mrd = parser.parse( value.getString() );

                        MatchingRule impl = new MatchingRule( mrd.getOid() );
                        impl.setDescription( mrd.getDescription() );
                        impl.setNames( mrd.getNames().toArray( new String[0] ) );
                        impl.setObsolete( mrd.isObsolete() );
                        impl.setSyntaxOid( mrd.getSyntaxOid() );
                        impl.setSchemaName( schema.getSchemaName() );

                        schema.addMatchingRule( impl );
                    }
                    catch ( ParseException e )
                    {
                        // Logging the exception and incrementing the counter
                        PluginUtils.logError( "Unable to parse the matching rule.", e ); //$NON-NLS-1$
                        parseErrorCount++;
                    }
                }
        }

        // if online: assume all received matching rules in attributes are valid -> create dummy matching rules if missing
        for ( AttributeType at : schema.getAttributeTypes() )
        {
            String equalityName = at.getEqualityOid();
            String orderingName = at.getOrderingOid();
            String substrName = at.getSubstringOid();
            checkMatchingRules( schema, equalityName, orderingName, substrName );
        }

        // Showing an error
        if ( parseErrorCount > 0 )
        {
            if ( parseErrorCount == 1 )
            {
                throw new SchemaConnectorException(
                    Messages.getString( "GenericSchemaConnector.OneSchemaElementCouldNotBeParsedError" ) ); //$NON-NLS-1$

            }
            else
            {
                throw new SchemaConnectorException( NLS.bind(
                    Messages.getString( "GenericSchemaConnector.MultipleSchemaElementsCouldNotBeParsedError" ), //$NON-NLS-1$
                    parseErrorCount ) );
            }
        }
    }


    // ── Han Creates Placeholder Crates for Missing Matching Rules ─────────────
    // If a cargo rule is referenced in the manifest but no crate arrived for it,
    // Han creates a dummy placeholder crate so the manifest stays consistent.
    // We create stub MatchingRule objects for any rule names referenced by
    // attribute types but not found in the schema's matching-rule list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates dummy {@link MatchingRule} stubs for any matching rule names that are
     * referenced by attribute types but not present in the schema.
     * This is necessary because some LDAP servers don't publish all their matching
     * rules in the subschema subentry.
     *
     * @param schema            the schema to check and potentially add stubs to
     * @param matchingRuleNames  the matching rule names (equality, ordering, substring) to check
     */
    private static void checkMatchingRules( Schema schema, String... matchingRuleNames )
    {
        for ( String matchingRuleName : matchingRuleNames )
        {
            if ( ( matchingRuleName != null ) && ( schema.getMatchingRule( matchingRuleName ) == null ) )
            {
                MatchingRule impl = new MatchingRule( matchingRuleName );
                impl.setSchemaName( schema.getSchemaName() );
                impl.setDescription( "Dummy" ); //$NON-NLS-1$
                impl.setNames( new String[]
                    { matchingRuleName } );
                schema.addMatchingRule( impl );
            }
        }
    }


    // ── Han Parks the Falcon — Export Not Yet Wired ──────────────────────────
    // Same situation as the ApacheDS connector: Han lands but nobody has wired
    // up the export bay yet.  Placeholder for a future implementation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Exports the project's schema to the connected LDAP server.
     * Not yet implemented — placeholder for a future feature.
     *
     * @param project  the project to export
     * @param monitor  progress monitor
     * @throws SchemaConnectorException  not currently thrown, declared for the interface
     */
    public void exportSchema( Project project, StudioProgressMonitor monitor )
        throws SchemaConnectorException
    {
        // TODO Auto-generated method stub
    }
}
