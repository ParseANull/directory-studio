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
package org.apache.directory.studio.openldap.config.model.io;


import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.constants.LdapConstants;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.exception.LdapNoSuchObjectException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.util.tree.DnNode;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResult;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.openldap.config.ExpandedLdifUtils;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.editor.ConnectionServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.DirectoryServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditorUtils;
import org.apache.directory.studio.openldap.config.jobs.EntryBasedConfigurationPartition;
import org.apache.directory.studio.openldap.config.model.AuxiliaryObjectClass;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcConfig;
import org.apache.directory.studio.openldap.config.model.OlcGlobal;
import org.apache.directory.studio.openldap.config.model.OlcModuleList;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;
import org.apache.directory.studio.openldap.config.model.OpenLdapConfiguration;
import org.apache.directory.studio.openldap.config.model.database.OlcDatabaseConfig;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;


// ── CLASS: ConfigurationReader — R2-D2 Downloading the Death Star Plans ──────
// R2-D2 plugs into the Imperial data terminal, pulls the Death Star schematics
// out of the server, decodes each sector of data, and builds a complete picture
// of the station from raw bytes. ConfigurationReader does the same: it connects
// to an LDAP server (or reads from a local directory), fetches all the cn=config
// entries, and uses Java reflection to decode each entry into the right OlcConfig
// bean — giving us a live in-memory model of the OpenLDAP server configuration.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Reads an OpenLDAP server configuration from an LDAP server or a local
 * directory of LDIF files, and returns an {@link OpenLdapConfiguration} object
 * populated with the appropriate {@link OlcConfig} subclass beans.
 * <p>
 * This class uses Java reflection to map LDAP object classes to model classes
 * and to inject attribute values into bean fields annotated with
 * {@link ConfigurationElement}. Think of it as R2-D2 downloading and decoding
 * the Death Star plans — raw LDAP entries in, structured Java model out.
 * </p>
 * <p>
 * All methods are static; this class cannot be instantiated.
 * </p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConfigurationReader
{
    // ── constructor — R2-D2 Powers Up But Doesn't Take Orders Directly ────────────
    // R2-D2 is always ready to help but he's a tool, not an actor — you call his
    // static methods (his beeps and whistles) directly. You never "new" up an R2-D2.
    // This private constructor enforces the utility-class pattern.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a static utility class and cannot be instantiated.
     */
    private ConfigurationReader()
    {
        // Nothing to do
    }


    /** The package name where the model classes are stored */
    private static final String MODEL_PACKAGE_NAME = "org.apache.directory.studio.openldap.config.model";

    /** The package name where the database model classes are stored */
    private static final String DATABASE_PACKAGE_NAME = "org.apache.directory.studio.openldap.config.model.database";

    /** The package name where the overlay model classes are stored */
    private static final String OVERLAY_PACKAGE_NAME = "org.apache.directory.studio.openldap.config.model.overlay";


    // ── readConfiguration(ConnectionServerConfigurationInput) — R2 Reads a Live Server
    // R2-D2 jacks into the active Imperial network: he authenticates, locates the
    // configuration subtree at cn=config, and downloads every entry using a breadth-first
    // walk. Then he decodes each raw LDAP entry into the right OlcConfig bean.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the OpenLDAP configuration from a live LDAP server connection.
     * We connect to the server, search for all entries under cn=config, and
     * convert each one to the appropriate {@link OlcConfig} subclass using
     * reflection. Overlays are attached to their parent database; the global
     * config and databases are stored in the returned {@link OpenLdapConfiguration}.
     *
     * <p>For example — R2 downloads plans from the live Death Star:</p>
     * <pre>
     *   ConnectionServerConfigurationInput input = ...;
     *   OpenLdapConfiguration cfg = ConfigurationReader.readConfiguration( input );
     *   OlcGlobal global = cfg.getGlobal();
     * </pre>
     *
     * @param input  the connection and server configuration input
     * @return  the populated {@link OpenLdapConfiguration}
     * @throws Exception  if the server cannot be reached or the configuration cannot be read
     */
    public static OpenLdapConfiguration readConfiguration( ConnectionServerConfigurationInput input ) throws Exception
    {
        // Creating a new OpenLDAP configuration
        OpenLdapConfiguration configuration = new OpenLdapConfiguration();

        // Saving the connection to the configuration
        configuration.setConnection( input.getConnection() );

        // Getting the browser connection associated with the connection in the input
        IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
            .getBrowserConnection( input.getConnection() );

        // Find the location of the configuration
        Dn configurationDn = ConfigurationUtils.getConfigurationDn( browserConnection );

        // Reading the configuration entries on the server
        List<Entry> configurationEntries = readEntries( configurationDn, input, browserConnection );

        // Creating a map to store object created based on their DN
        Map<Dn, OlcConfig> dnToConfigObjectMap = new HashMap<>();

        // For each configuration entries we create an associated configuration
        // object and store it in the OpenLDAP configuration
        for ( Entry entry : configurationEntries )
        {
            // Converting the entry into a configuration object
            OlcConfig configurationObject = createConfigurationObject( entry );
            if ( configurationObject != null )
            {
                // Storing the object in the configuration objects map
                dnToConfigObjectMap.put( entry.getDn(), configurationObject );

                if ( configurationObject instanceof OlcOverlayConfig )
                {
                    OlcOverlayConfig overlayConfig = ( OlcOverlayConfig ) configurationObject;

                    OlcDatabaseConfig databaseConfig = ( OlcDatabaseConfig ) dnToConfigObjectMap.get( entry.getDn()
                        .getParent() );

                    if ( databaseConfig != null )
                    {
                        databaseConfig.addOverlay( overlayConfig );
                    }
                    else
                    {
                        configuration.add( overlayConfig );
                    }
                }
                else if ( configurationObject instanceof OlcGlobal )
                {
                    configuration.setGlobal( ( OlcGlobal ) configurationObject );
                }
                else if ( configurationObject instanceof OlcModuleList )
                {
                    configuration.add( (OlcModuleList)configurationObject );
                }
                else if ( configurationObject instanceof OlcDatabaseConfig )
                {
                    configuration.add( ( OlcDatabaseConfig ) configurationObject );
                }
                else
                {
                    configuration.add( configurationObject );
                }
            }
        }

        return configuration;
    }


    // ── readConfiguration(DirectoryServerConfigurationInput) — R2 Reads a Local Directory
    // R2-D2 reads from a local filesystem rather than a live server — he parses the
    // LDIF files in the directory and builds the same configuration model.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the OpenLDAP configuration from a local directory of LDIF files.
     * Delegates to {@link #readConfiguration(File)} using the directory from the input.
     *
     * <p>For example — R2 reads plans from a local storage cartridge:</p>
     * <pre>
     *   DirectoryServerConfigurationInput input = ...;
     *   OpenLdapConfiguration cfg = ConfigurationReader.readConfiguration( input );
     * </pre>
     *
     * @param input  the directory server configuration input (carries a local directory path)
     * @return  the populated {@link OpenLdapConfiguration}
     * @throws Exception  if the directory cannot be read or parsed
     */
    public static OpenLdapConfiguration readConfiguration( DirectoryServerConfigurationInput input ) throws Exception
    {
        return readConfiguration( input.getDirectory() );
    }


    // ── readConfiguration(File) — R2 Reads Plans from a Local LDIF Directory ──────
    // R2-D2 cracks open the local data cartridge, walks the directory tree of LDIF
    // files, and assembles the full configuration object tree from the entries found.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the OpenLDAP configuration from a local directory of LDIF files.
     * We use {@link ExpandedLdifUtils} to parse the directory into a DN tree,
     * then walk that tree to create {@link OlcConfig} beans for each entry.
     *
     * <p>For example — R2 reads plans from a local filesystem directory:</p>
     * <pre>
     *   File dir = new File( "/etc/openldap/slapd.d" );
     *   OpenLdapConfiguration cfg = ConfigurationReader.readConfiguration( dir );
     * </pre>
     *
     * @param directory  the local directory containing slapd.d LDIF files
     * @return  the populated {@link OpenLdapConfiguration}
     * @throws Exception  if the directory is empty or cannot be parsed
     */
    public static OpenLdapConfiguration readConfiguration( File directory ) throws Exception
    {
        // Creating a new OpenLDAP configuration
        OpenLdapConfiguration configuration = new OpenLdapConfiguration();

        // Reading the configuration entries disk
        DnNode<Entry> tree = readEntries( directory );

        // Creating configuration objects
        createConfigurationObjects( tree, configuration );

        return configuration;
    }


    // ── createConfigurationObjects(tree, config) — R2 Walks the Entry Tree ────────
    // R2-D2 traverses each sector of the data download, building the map of DN-to-bean
    // pairs so he can wire overlays to their parent databases correctly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates {@link OlcConfig} objects from a DN-based tree of LDAP entries and
     * populates the given configuration with them.
     * This overload initialises the DN-to-bean tracking map before recursing.
     *
     * @param tree           the DN tree of parsed LDAP entries
     * @param configuration  the configuration to populate
     * @throws ConfigurationException  if any entry cannot be converted to a bean
     */
    /**
     * Creates the configuration objects.
     *
     * @param tree the tree
     * @param configuration the configuration
     * @throws ConfigurationException
     */
    private static void createConfigurationObjects( DnNode<Entry> tree, OpenLdapConfiguration configuration )
        throws ConfigurationException
    {
        // Creating a map to store object created based on their DN
        Map<Dn, OlcConfig> dnToConfigObjectMap = new HashMap<>();

        createConfigurationObjects( tree, configuration, dnToConfigObjectMap );
    }


    // ── createConfigurationObjects(node, config, map) — R2 Decodes Each Entry Node ─
    // R2-D2 processes each node in the tree: decode the entry, identify what type of
    // config object it is (overlay? database? global?), link it to its parent, and
    // recurse into children. This recursive walk reconstructs the full hierarchy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Recursively walks the DN tree, converts each entry to an {@link OlcConfig} bean,
     * categorises it (global, database, overlay, or other), and stores it in both
     * the tracking map and the {@link OpenLdapConfiguration}.
     * Overlays are attached to their parent database using the tracking map.
     *
     * @param node                the current tree node
     * @param configuration       the configuration to populate
     * @param dnToConfigObjectMap a map from DN to the bean created for that DN (used to resolve overlay parents)
     * @throws ConfigurationException  if any entry cannot be converted
     */
    private static void createConfigurationObjects( DnNode<Entry> node, OpenLdapConfiguration configuration,
        Map<Dn, OlcConfig> dnToConfigObjectMap ) throws ConfigurationException
    {
        if ( node != null )
        {
            // Checking if the node as an element
            if ( node.hasElement() )
            {
                // Getting the entry for the node
                Entry entry = node.getElement();

                // Converting the entry into a configuration object
                OlcConfig configurationObject = createConfigurationObject( entry );

                if ( configurationObject != null )
                {
                    // Storing the object in the configuration objects map
                    dnToConfigObjectMap.put( entry.getDn(), configurationObject );

                    // Checking if it's an overlay
                    if ( configurationObject instanceof OlcOverlayConfig )
                    {
                        OlcOverlayConfig overlayConfig = ( OlcOverlayConfig ) configurationObject;

                        // Getting the associated database configuration object
                        OlcDatabaseConfig databaseConfig = ( OlcDatabaseConfig ) dnToConfigObjectMap.get( entry.getDn()
                            .getParent() );

                        if ( databaseConfig != null )
                        {
                            databaseConfig.addOverlay( overlayConfig );
                        }
                        else
                        {
                            configuration.add( overlayConfig );
                        }
                    }
                    // Checking if it's the "global' configuration object
                    else if ( configurationObject instanceof OlcGlobal )
                    {
                        configuration.setGlobal( ( OlcGlobal ) configurationObject );
                    }
                    // Checking if it's a database
                    else if ( configurationObject instanceof OlcDatabaseConfig )
                    {
                        configuration.add( (OlcDatabaseConfig)configurationObject );
                    }
                    // Any other object type
                    else
                    {
                        configuration.add( configurationObject );
                    }
                }
            }

            // Checking the node has some children
            if ( node.hasChildren() )
            {
                Collection<DnNode<Entry>> children = node.getChildren().values();

                for ( DnNode<Entry> child : children )
                {
                    createConfigurationObjects( child, configuration, dnToConfigObjectMap );
                }
            }
        }
    }


    // ── readEntries(File) — R2 Reads the LDIF Files from Disk ────────────────────
    // R2-D2 cracks open the local storage: he reads all the LDIF files in the slapd.d
    // directory and assembles them into a single DN tree. If the directory is empty
    // or unreadable, he panics (throws).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the LDIF files from the given local directory into a DN tree.
     * Uses {@link ExpandedLdifUtils#read(File)} to do the heavy lifting.
     * Throws if no entries are found — an empty slapd.d is not a valid configuration.
     *
     * <p>For example — R2 reads files from the local slapd.d directory:</p>
     * <pre>
     *   DnNode&lt;Entry&gt; tree = readEntries( new File( "/etc/openldap/slapd.d" ) );
     * </pre>
     *
     * @param directory  the local slapd.d directory
     * @return  a non-empty DN tree of parsed entries
     * @throws Exception  if the directory is empty or cannot be read
     */
    private static DnNode<Entry> readEntries( File directory )
        throws Exception
    {
        // Reading the entries tree
        DnNode<Entry> tree = ExpandedLdifUtils.read( directory );

        // Checking the read tree
        if ( ( tree != null ) && ( tree.size() != 0 ) )
        {
            return tree;
        }
        else
        {
            throw new Exception( "No entries found" );
        }
    }


    // ── getHighestStructuralObjectClass — R2 Identifies the Right Schematic Page ──
    // R2-D2 scans the object class list and finds the most-specific structural object
    // class. LDAP entries inherit from a chain of object classes, and we need the leaf
    // (most specific) one to know which Java bean class to instantiate.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds the most-derived (highest) structural object class in an entry's
     * {@code objectClass} attribute, by eliminating any object class that appears
     * as a superior of another.
     * We use the schema manager to look up the class hierarchy — the remaining
     * candidate after all superiors are removed is the one we want.
     *
     * <p>For example — R2 finds the leaf structural object class:</p>
     * <pre>
     *   ObjectClass oc = getHighestStructuralObjectClass( entry.get( "objectClass" ) );
     *   // oc.getName() == "olcMdbConfig"
     * </pre>
     *
     * @param objectClassAttribute  the {@code objectClass} attribute of an LDAP entry
     * @return  the most-derived structural {@link ObjectClass}
     * @throws ConfigurationException  if the schema cannot be accessed or the attribute is malformed
     */
    public static ObjectClass getHighestStructuralObjectClass( Attribute objectClassAttribute )
        throws ConfigurationException
    {
        Set<ObjectClass> candidates = new HashSet<>();

        try
        {
            SchemaManager schemaManager = OpenLdapConfigurationPlugin.getDefault().getSchemaManager();

            if ( ( objectClassAttribute != null ) && ( schemaManager != null ) )
            {
                // Create the set of candidates
                for ( Value objectClassValue : objectClassAttribute )
                {
                    ObjectClass oc = OpenLdapServerConfigurationEditorUtils.getObjectClass( schemaManager,
                        objectClassValue.getString() );

                    if ( ( oc != null ) && ( oc.isStructural() ) )
                    {
                        candidates.add( oc );
                    }
                }

                // Now find the parent OC
                for ( Value objectClassValue : objectClassAttribute )
                {
                    ObjectClass oc = OpenLdapServerConfigurationEditorUtils.getObjectClass( schemaManager,
                        objectClassValue.getString() );

                    if ( oc != null )
                    {
                        for ( String superiorName : oc.getSuperiorOids() )
                        {
                            ObjectClass superior = OpenLdapServerConfigurationEditorUtils.getObjectClass( schemaManager,
                                superiorName );

                            if ( ( superior != null ) && ( superior.isStructural() )
                                && ( candidates.contains( superior ) ) )
                            {
                                candidates.remove( superior );
                            }
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( e );
        }

        // The remaining OC in the candidates set is the one we are looking for
        return candidates.toArray( new ObjectClass[]
            {} )[0];
    }


    // ── getAuxiliaryObjectClasses — R2 Identifies Optional Schematic Overlays ─────
    // R2-D2 scans the object class list for auxiliary classes — optional structural
    // add-ons that augment the main object class, like overlay configuration mixed
    // into a database entry. We need these to inject their fields too.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all auxiliary {@link ObjectClass} objects found in an entry's
     * {@code objectClass} attribute.
     * Auxiliary object classes add extra attributes to an entry on top of its
     * structural class. We use these to find and inject auxiliary bean fields.
     *
     * <p>For example — R2 finds auxiliary object classes:</p>
     * <pre>
     *   ObjectClass[] aux = getAuxiliaryObjectClasses( entry.get( "objectClass" ) );
     * </pre>
     *
     * @param objectClassAttribute  the {@code objectClass} attribute of an LDAP entry
     * @return  an array of auxiliary {@link ObjectClass} objects; may be empty but never null
     * @throws ConfigurationException  if the schema cannot be accessed
     */
    public static ObjectClass[] getAuxiliaryObjectClasses( Attribute objectClassAttribute )
        throws ConfigurationException
    {
        List<ObjectClass> auxiliaryObjectClasses = new ArrayList<>();

        try
        {
            SchemaManager schemaManager = OpenLdapConfigurationPlugin.getDefault().getSchemaManager();

            if ( ( objectClassAttribute != null ) && ( schemaManager != null ) )
            {
                for ( Value objectClassValue : objectClassAttribute )
                {
                    ObjectClass oc = OpenLdapServerConfigurationEditorUtils.getObjectClass( schemaManager,
                        objectClassValue.getString() );

                    if ( ( oc != null ) && ( oc.isAuxiliary() ) )
                    {
                        auxiliaryObjectClasses.add( oc );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( e );
        }

        return auxiliaryObjectClasses.toArray( new ObjectClass[0] );
    }


    // ── readEntries(Dn, input, connection) — R2 Downloads Entries from a Live Server
    // R2-D2 connects to the live Death Star network, finds the config base entry, then
    // breadth-first-walks the entire cn=config subtree, collecting every entry. The
    // result is a flat list ready for bean conversion.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Searches the live LDAP server for all entries under the configuration DN
     * (cn=config) using a breadth-first walk.
     * We start at the config base entry and recursively search for children until
     * we have the complete set. Also sets up an in-memory partition on the input
     * for later use by the editor.
     *
     * <p>For example — R2 downloads entries from the live server:</p>
     * <pre>
     *   List&lt;Entry&gt; entries = readEntries( configDn, input, browserConn );
     * </pre>
     *
     * @param configurationDn   the DN of the cn=config entry
     * @param input             the server configuration input (holds the connection)
     * @param browserConnection the browser connection used for searches
     * @return  the complete list of configuration entries from the server
     * @throws Exception  if the server is unreachable or the config base entry is missing
     */
    public static List<Entry> readEntries( Dn configurationDn, ConnectionServerConfigurationInput input,
        IBrowserConnection browserConnection ) throws Exception
    {
        List<Entry> foundEntries = new ArrayList<>();

        IProgressMonitor progressMonitor = new NullProgressMonitor();
        StudioProgressMonitor monitor = new StudioProgressMonitor( progressMonitor );
        Connection connection = input.getConnection();

        // Creating the schema manager
        SchemaManager schemaManager = OpenLdapConfigurationPlugin.getDefault().getSchemaManager();

        // The DN corresponding to the configuration base

        // Creating the configuration partition
        EntryBasedConfigurationPartition configurationPartition = OpenLdapServerConfigurationEditorUtils
            .createConfigurationPartition( schemaManager, configurationDn );

        // Opening the connection (if needed)
        ConfigurationUtils.openConnection( connection, monitor );

        // Creating the search parameter
        SearchParameter configSearchParameter = new SearchParameter();
        configSearchParameter.setSearchBase( configurationDn );
        configSearchParameter.setFilter( LdapConstants.OBJECT_CLASS_STAR );
        configSearchParameter.setScope( SearchScope.OBJECT );
        configSearchParameter.setReturningAttributes( SchemaConstants.ALL_USER_ATTRIBUTES_ARRAY );

        // Looking for the 'ou=config' base entry
        Entry configEntry = null;
        StudioSearchResultEnumeration enumeration = SearchRunnable.search( browserConnection, configSearchParameter,
            monitor );

        // Checking if an error occurred
        if ( monitor.errorsReported() )
        {
            throw monitor.getException();
        }

        // Getting the entry
        if ( enumeration.hasMore() )
        {
            // Creating the base entry
            StudioSearchResult searchResult =  enumeration.next();
            configEntry = searchResult.getEntry();
        }
        enumeration.close();

        // Verifying we found the base entry
        if ( configEntry == null )
        {
            throw new LdapNoSuchObjectException( NLS.bind( "Unable to find the ''{0}'' base entry.", configurationDn ) );
        }

        // Creating a list to hold the entries that needs to be checked
        // for children and added to the partition
        List<Entry> entries = new ArrayList<>();
        entries.add( configEntry );

        // Looping on the entries list until it's empty
        while ( !entries.isEmpty() )
        {
            // Removing the first entry from the list
            Entry entry = entries.remove( 0 );

            // Adding the entry to the partition and the entries list
            configurationPartition.addEntry( entry );
            foundEntries.add( entry );

            SearchParameter searchParameter = new SearchParameter();
            searchParameter.setSearchBase( entry.getDn() );
            searchParameter.setFilter( LdapConstants.OBJECT_CLASS_STAR );
            searchParameter.setScope( SearchScope.ONELEVEL );
            searchParameter.setReturningAttributes( SchemaConstants.ALL_USER_ATTRIBUTES_ARRAY );

            // Looking for the children of the entry
            StudioSearchResultEnumeration childrenEnumeration = SearchRunnable.search( browserConnection,
                searchParameter, monitor );

            // Checking if an error occurred
            if ( monitor.errorsReported() )
            {
                throw monitor.getException();
            }

            while ( childrenEnumeration.hasMore() )
            {
                // Creating the child entry
                StudioSearchResult searchResult =  childrenEnumeration.next();
                Entry childEntry = searchResult.getEntry();

                // Adding the children to the list of entries
                entries.add( childEntry );
            }
            childrenEnumeration.close();
        }

        // Setting the created partition to the input
        input.setOriginalPartition( configurationPartition );

        return foundEntries;
    }


    // ── createConfigurationObject — R2 Decodes a Single Entry into a Java Bean ────
    // R2-D2 takes one raw LDAP entry, looks up its object class, determines which
    // package (model / database / overlay) and which Java class matches, instantiates
    // it via reflection, processes any auxiliary object classes, and then injects
    // all attribute values into the bean fields.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a single LDAP entry into the correct {@link OlcConfig} subclass bean.
     * We determine the bean class by mapping the entry's highest structural object
     * class name to the corresponding Java class in the model, database, or overlay
     * package. Auxiliary object classes are handled separately and attached to the bean.
     *
     * <p>For example — R2 decodes a single slapd.d entry:</p>
     * <pre>
     *   OlcConfig bean = createConfigurationObject( mdbEntry );
     *   // bean instanceof OlcMdbConfig == true
     * </pre>
     *
     * @param entry  the LDAP entry to convert
     * @return  the populated {@link OlcConfig} bean, or {@code null} if the entry has no objectClass
     * @throws ConfigurationException  if bean instantiation or value injection fails
     */
    private static OlcConfig createConfigurationObject( Entry entry )
        throws ConfigurationException
    {
        // Getting the 'objectClass' attribute
        Attribute objectClassAttribute = entry.get( SchemaConstants.OBJECT_CLASS_AT );

        if ( objectClassAttribute != null )
        {
            // Getting the highest structural object class based on schema
            ObjectClass highestStructuralObjectClass = getHighestStructuralObjectClass( objectClassAttribute );

            // Computing the class name for the bean corresponding to the structural object class
            String highestObjectClassName = highestStructuralObjectClass.getName();
            StringBuilder className = new StringBuilder();

            if ( objectClassAttribute.contains( "olcDatabaseConfig" ) )
            {
                className.append( DATABASE_PACKAGE_NAME );
            }
            else if ( objectClassAttribute.contains( "olcOverlayConfig" ) )
            {
                className.append( OVERLAY_PACKAGE_NAME );
            }
            else
            {
                className.append( MODEL_PACKAGE_NAME );
            }

            className.append( "." );
            className.append( Character.toUpperCase( highestObjectClassName.charAt( 0 ) ) );
            className.append( highestObjectClassName.substring( 1 ) );

            // Instantiating the object
            OlcConfig bean = null;

            try
            {
                Class<?> clazz = Class.forName( className.toString() );
                Constructor<?> constructor = clazz.getConstructor();
                bean = ( OlcConfig ) constructor.newInstance();
            }
            catch ( Exception e )
            {
                throw new ConfigurationException( e );
            }

            // Checking if the bean as been created
            if ( bean == null )
            {
                throw new ConfigurationException( "The instantiated bean for '" + highestObjectClassName + "' is null" );
            }

            // Checking auxiliary object classes
            ObjectClass[] auxiliaryObjectClasses = getAuxiliaryObjectClasses( objectClassAttribute );

            if ( ( auxiliaryObjectClasses != null ) && ( auxiliaryObjectClasses.length > 0 ) )
            {
                for ( ObjectClass auxiliaryObjectClass : auxiliaryObjectClasses )
                {
                    // Computing the class name for the bean corresponding to the auxiliary object class
                    String auxiliaryObjectClassName = auxiliaryObjectClass.getName();
                    className = new StringBuilder();
                    className.append( MODEL_PACKAGE_NAME );
                    className.append( "." );
                    className.append( Character.toUpperCase( auxiliaryObjectClassName.charAt( 0 ) ) );
                    className.append( auxiliaryObjectClassName.substring( 1 ) );

                    // Instantiating the object
                    AuxiliaryObjectClass auxiliaryObjectClassBean = null;

                    try
                    {
                        Class<?> clazz = Class.forName( className.toString() );
                        Constructor<?> constructor = clazz.getConstructor();
                        auxiliaryObjectClassBean = ( AuxiliaryObjectClass ) constructor.newInstance();
                    }
                    catch ( Exception e )
                    {
                        throw new ConfigurationException( e );
                    }

                    // Checking if the bean as been created
                    if ( auxiliaryObjectClassBean == null )
                    {
                        throw new ConfigurationException( "The instantiated auxiliary object class bean for '"
                            + auxiliaryObjectClassName + "' is null" );
                    }

                    // Reading all values
                    readValues( entry, auxiliaryObjectClassBean );

                    // Adding the auxiliary object class bean to the bean
                    bean.addAuxiliaryObjectClasses( auxiliaryObjectClassBean );
                }
            }

            // Reading all values
            readValues( entry, bean );

            // Storing the parent DN
            bean.setParentDn( entry.getDn().getParent() );

            return bean;
        }
        return null;
    }


    // ── readValues — R2 Injects Attribute Values into a Bean's Fields ─────────────
    // R2-D2 walks up the Java class hierarchy of the bean, scanning every declared
    // field for a @ConfigurationElement annotation. For each annotated field, he
    // finds the matching LDAP attribute in the entry and calls readAttributeValue
    // to inject the data into the field.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Injects attribute values from an LDAP entry into the annotated fields of a bean.
     * We walk up the class hierarchy (superclass by superclass) to catch fields
     * declared in parent classes. For each field annotated with
     * {@link ConfigurationElement}, we find the matching LDAP attribute and inject
     * its value(s) via {@link #readAttributeValue}.
     *
     * <p>For example — R2 injects attribute values into an OlcMdbConfig bean:</p>
     * <pre>
     *   readValues( mdbEntry, olcMdbConfig );
     *   // olcMdbConfig.getOlcDbDirectory() now holds the value from the entry
     * </pre>
     *
     * @param entry  the LDAP entry containing attribute values
     * @param bean   the bean to inject values into
     * @throws ConfigurationException  if a field value cannot be injected
     */
    private static void readValues( Entry entry, Object bean ) throws ConfigurationException
    {
        // Checking all fields of the bean (including super class fields)
        Class<?> clazz = bean.getClass();
        while ( clazz != null )
        {
            // Looping on all fields of the class
            Field[] fields = clazz.getDeclaredFields();
            for ( Field field : fields )
            {
                // Looking for the @ConfigurationElement annotation
                ConfigurationElement configurationElement = field.getAnnotation( ConfigurationElement.class );
                if ( configurationElement != null )
                {
                    // Checking if we're have a value  for the attribute type
                    String attributeType = configurationElement.attributeType();
                    if ( ( attributeType != null ) && ( !"".equals( attributeType ) ) )
                    {
                        Attribute attribute = entry.get( attributeType );
                        if ( ( attribute != null ) && ( attribute.size() > 0 ) )
                        {
                            // Making the field accessible (we get an exception if we don't do that)
                            field.setAccessible( true );

                            // loop on the values and inject them in the bean
                            for ( Value value : attribute )
                            {
                                readAttributeValue( bean, field, attribute, value );
                            }
                        }
                    }
                }
            }

            // Switching to the super class
            clazz = clazz.getSuperclass();
        }
    }


    // ── readAttributeValue — R2 Decodes a Single Attribute Value into the Right Type
    // R2-D2 reads one attribute value from the entry and injects it into the right
    // field in the bean. String, int, Integer, long, Long, Boolean, Dn, Set, and List
    // fields are all handled — R2 picks the right decoder for each type.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Injects a single LDAP attribute value into a bean field, handling all supported
     * Java types: String, int/Integer, long/Long, boolean/Boolean, Dn, Set, and List.
     * For collection types, we find and call the appropriate {@code addXxx()} method
     * on the bean using reflection.
     *
     * <p>For example — R2 decodes a single attribute value:</p>
     * <pre>
     *   readAttributeValue( bean, integerField, attribute, value );
     *   // field is now set to Integer.parseInt( value.getString() )
     * </pre>
     *
     * @param bean       the target bean to inject into
     * @param field      the field to set
     * @param attribute  the LDAP attribute containing the value
     * @param value      the specific attribute value to decode and inject
     * @throws ConfigurationException  if the value cannot be decoded or the field cannot be set
     */
    private static void readAttributeValue( Object bean, Field field, Attribute attribute, Value value )
        throws ConfigurationException
    {
        Class<?> type = field.getType();
        String addMethodName = "add" + Character.toUpperCase( field.getName().charAt( 0 ) )
            + field.getName().substring( 1 );
        String valueStr = value.getString();

        try
        {
            // String class
            if ( type == String.class )
            {
                Object stringValue = readSingleValue( type, attribute, valueStr );
                if ( stringValue != null )
                {
                    field.set( bean, stringValue );
                }
            }
            // Int primitive type
            else if ( type == int.class )
            {
                Object integerValue = readSingleValue( type, attribute, valueStr );
                if ( integerValue != null )
                {
                    field.setInt( bean, ( ( Integer ) integerValue ).intValue() );
                }
            }
            // Integer class
            else if ( type == Integer.class )
            {
                Object integerValue = readSingleValue( type, attribute, valueStr );
                if ( integerValue != null )
                {
                    field.set( bean, ( Integer ) integerValue );
                }
            }
            // Long primitive type
            else if ( type == long.class )
            {
                Object longValue = readSingleValue( type, attribute, valueStr );
                if ( longValue != null )
                {
                    field.setLong( bean, ( ( Long ) longValue ).longValue() );
                }
            }
            // Long class
            else if ( type == Long.class )
            {
                Object longValue = readSingleValue( type, attribute, valueStr );
                if ( longValue != null )
                {
                    field.setLong( bean, ( Long ) longValue );
                }
            }
            // Boolean primitive type
            else if ( type == boolean.class )
            {
                Object booleanValue = readSingleValue( type, attribute, valueStr );
                if ( booleanValue != null )
                {
                    field.setBoolean( bean, ( ( Boolean ) booleanValue ).booleanValue() );
                }
            }
            // Boolean class
            else if ( type == Boolean.class )
            {
                Object booleanValue = readSingleValue( type, attribute, valueStr );
                if ( booleanValue != null )
                {
                    field.set( bean, ( Boolean ) booleanValue );
                }
            }
            // Dn class
            else if ( type == Dn.class )
            {
                Object dnValue = readSingleValue( type, attribute, valueStr );
                if ( dnValue != null )
                {
                    field.set( bean, dnValue );
                }
            }
            // Set class
            else if ( type == Set.class )
            {
                Type genericFieldType = field.getGenericType();

                if ( genericFieldType instanceof ParameterizedType )
                {
                    ParameterizedType parameterizedType = ( ParameterizedType ) genericFieldType;
                    Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();
                    if ( ( fieldArgTypes != null ) && ( fieldArgTypes.length > 0 ) )
                    {
                        Class<?> fieldArgClass = ( Class<?> ) fieldArgTypes[0];

                        Object methodParameter = Array.newInstance( fieldArgClass, 1 );
                        Array.set( methodParameter, 0, readSingleValue( fieldArgClass, attribute, valueStr ) );

                        Method method = bean.getClass().getMethod( addMethodName, methodParameter.getClass() );

                        method.invoke( bean, methodParameter );
                    }
                }
            }
            // List class
            else if ( type == List.class )
            {
                Type genericFieldType = field.getGenericType();

                if ( genericFieldType instanceof ParameterizedType )
                {
                    ParameterizedType parameterizedType = ( ParameterizedType ) genericFieldType;
                    Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();
                    if ( ( fieldArgTypes != null ) && ( fieldArgTypes.length > 0 ) )
                    {
                        Class<?> fieldArgClass = ( Class<?> ) fieldArgTypes[0];

                        Object methodParameter = Array.newInstance( fieldArgClass, 1 );
                        Array.set( methodParameter, 0, readSingleValue( fieldArgClass, attribute, valueStr ) );

                        Method method = bean.getClass().getMethod( addMethodName, methodParameter.getClass() );

                        method.invoke( bean, methodParameter );
                    }
                }
            }
        }
        catch ( IllegalArgumentException | IllegalAccessException iae )
        {
            throw new ConfigurationException( "Cannot store '" + valueStr + "' into attribute "
                + attribute.getId() );
        }
        catch ( SecurityException se )
        {
            throw new ConfigurationException( "Cannot access to the class "
                + bean.getClass().getName() );
        }
        catch ( NoSuchMethodException nsme )
        {
            throw new ConfigurationException( "Cannot find a method " + addMethodName
                + " in the class "
                + bean.getClass().getName() );
        }
        catch ( InvocationTargetException ite )
        {
            throw new ConfigurationException( "Cannot invoke the class "
                + bean.getClass().getName() + ", "
                + ite.getMessage() );
        }
        catch ( NegativeArraySizeException nase )
        {
            // No way that can happen...
        }
    }


    // ── readSingleValue — R2 Decodes One Attribute Value to the Correct Java Type ──
    // R2-D2 reads a raw LDAP string value and converts it to the target Java type:
    // String stays a String, numbers get parsed, booleans get parsed, Dns get validated.
    // If the conversion fails, he raises a ConfigurationException rather than silently
    // storing garbage.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a raw LDAP attribute value string to the target Java type.
     * Supports String, int/Integer, long/Long, boolean/Boolean, and Dn.
     * For Dn types, we validate the string is a legal LDAP distinguished name.
     * Returns {@code null} for unrecognised types.
     *
     * <p>For example — R2 decodes a raw value string:</p>
     * <pre>
     *   Object val = readSingleValue( Integer.class, attr, "500" );
     *   // val == Integer(500)
     * </pre>
     *
     * @param type       the target Java type to convert to
     * @param attribute  the LDAP attribute (used in error messages)
     * @param value      the raw string value to convert
     * @return  the converted value, or {@code null} if the type is not supported
     * @throws ConfigurationException  if the value cannot be parsed into the target type
     */
    private static Object readSingleValue( Class<?> type, Attribute attribute, String value )
        throws ConfigurationException
    {
        try
        {
            // String class
            if ( type == String.class )
            {
                return value;
            }
            // Int primitive type
            else if ( type == int.class )
            {
                return new Integer( value );
            }
            // Integer class
            else if ( type == Integer.class )
            {
                return new Integer( value );
            }
            // Long class
            else if ( type == long.class )
            {
                return new Long( value );
            }
            // Boolean primitive type
            else if ( type == boolean.class )
            {
                return new Boolean( value );
            }
            // Boolean class
            else if ( type == Boolean.class )
            {
                return new Boolean( value );
            }
            // Dn class
            else if ( type == Dn.class )
            {
                try
                {
                    return new Dn( value );
                }
                catch ( LdapInvalidDnException lide )
                {
                    throw new ConfigurationException( "The Dn '" + value + "' for attribute " + attribute.getId()
                        + " is not a valid Dn" );
                }
            }

            return null;
        }
        catch ( IllegalArgumentException iae )
        {
            throw new ConfigurationException( "Cannot store '" + value + "' into attribute "
                + attribute.getId() );
        }
    }
}
