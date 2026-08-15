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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.DetectedConnectionProperties;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.BaseDNEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DirectoryMetadataEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;


// ── CLASS: InitializeRootDSERunnable — R2-D2 READS THE DEATH STAR'S MANIFEST ─
// R2-D2 plugs into the Death Star's central computer and downloads the main
// system manifest: reactor location, hangar bays, shield generators, and the
// list of all connected sub-systems.  That manifest is the Root DSE — the
// zero-level entry every LDAP server exposes at an empty DN ("").  It lists
// the naming contexts (base DNs), the schema sub-entry, and what capabilities
// the server supports.  Once R2-D2 has the manifest he relays it back to the
// Rebel fleet so they know exactly what they're dealing with.
// This runnable loads the Root DSE attributes and uses them to populate the
// top of the browser tree — base DN entries, the schema sub-entry, and other
// server metadata entries.  It also runs {@link ServerTypeDetector} to figure
// out what brand of LDAP server we're talking to.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that loads (or reloads) the Root DSE of the LDAP server.
 * The Root DSE (Distinguished Service Entry) is a special entry at the empty DN
 * that every LDAP server must expose.  It tells us which naming contexts
 * (base DNs) exist, where the schema sub-entry lives, what LDAP protocol
 * versions and controls the server supports, and vendor information.
 * We use all of that to build the top level of the browser tree, detect server
 * type, and update the connection's {@link DetectedConnectionProperties}.
 * Think of it as R2-D2 reading the Death Star's main system manifest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitializeRootDSERunnable implements StudioConnectionBulkRunnableWithProgress
{
    // ── R2-D2's Requested Data Points ─────────────────────────────────────────
    /**
     * The set of Root DSE attribute names we always request.  This covers
     * naming contexts, schema sub-entry, LDAP version, SASL mechanisms,
     * supported extensions, controls, features, vendor name/version, and
     * all operational attributes.
     */
    public static final String[] ROOT_DSE_ATTRIBUTES =
        {
            SchemaConstants.NAMING_CONTEXTS_AT,
            SchemaConstants.SUBSCHEMA_SUBENTRY_AT,
            SchemaConstants.SUPPORTED_LDAP_VERSION_AT,
            SchemaConstants.SUPPORTED_SASL_MECHANISMS_AT,
            SchemaConstants.SUPPORTED_EXTENSION_AT,
            SchemaConstants.SUPPORTED_CONTROL_AT,
            SchemaConstants.SUPPORTED_FEATURES_AT,
            SchemaConstants.VENDOR_NAME_AT,
            SchemaConstants.VENDOR_VERSION_AT,
            SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES };

    private IRootDSE rootDSE;


    // ── R2-D2 Picks Up His Data Probe ─────────────────────────────────────────
    // Private — callers use {@link StudioBrowserJob} with the public
    // {@link #loadRootDSE} static method; the runnable form is internal.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance that will load the given Root DSE.
     *
     * @param rootDSE the Root DSE entry to populate.
     */
    private InitializeRootDSERunnable( IRootDSE rootDSE )
    {
        this.rootDSE = rootDSE;
    }


    // ── R2-D2 Plugs Into The Right Terminal ───────────────────────────────────
    // One connection per Root DSE — the connection it belongs to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying LDAP connection for the Root DSE's browser connection.
     *
     * @return single-element array with the raw {@link Connection}.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { rootDSE.getBrowserConnection().getConnection() };
    }


    // ── The Mission Name For The Progress Indicator ────────────────────────────
    // "Initializing attributes..." shown in the progress view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "initialise entry attributes" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__init_entries_title_attonly;
    }


    // ── R2-D2 Locks The Root DSE While He Reads It ────────────────────────────
    // Two jobs loading the Root DSE simultaneously would produce a mess.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Root DSE entry as the only locked object.
     *
     * @return the Root DSE.
     */
    public Object[] getLockedObjects()
    {
        return new IEntry[]
            { rootDSE };
    }


    // ── If R2-D2 Can't Read The Manifest ──────────────────────────────────────
    // "Could not initialise entry." error displayed to the user.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown when Root DSE loading fails.
     *
     * @return a localised error string.
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__init_entries_error_1;
    }


    // ── R2-D2 Scans The Entire Manifest ───────────────────────────────────────
    // Sets the progress task name and delegates to {@link #loadRootDSE}, which
    // does the actual work of searching, parsing, and populating.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads the Root DSE by delegating to {@link #loadRootDSE}.
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( " ", 3 ); //$NON-NLS-1$
        monitor.reportProgress( " " ); //$NON-NLS-1$

        monitor.setTaskName( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__init_entries_task, new String[]
            { rootDSE.getDn().getName() } ) );
        monitor.worked( 1 );

        monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__init_entries_progress_att,
            new String[]
                { rootDSE.getDn().getName() } ) );

        loadRootDSE( rootDSE.getBrowserConnection(), monitor );
    }


    // ── R2-D2 Sends The All-Clear Signal ──────────────────────────────────────
    // "Root DSE loaded — fire the AttributesInitializedEvent."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires an {@link AttributesInitializedEvent} so the browser tree knows
     * the Root DSE has been freshly populated.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        EventRegistry.fireEntryUpdated( new AttributesInitializedEvent( rootDSE ), this );
    }


    // ── R2-D2 Does The Full Download ──────────────────────────────────────────
    // 1. Clear old children and attributes.
    // 2. Two searches: one for operational + well-known attrs, one for user attrs
    //    (some dumb servers refuse combined "*" and "+" requests).
    // 3. Build the base DN entry list from namingContexts or via ONELEVEL search.
    // 4. Add schema sub-entry and other metadata entries.
    // 5. Try to initialise each base DN entry (check it actually exists).
    // 6. Set Root DSE flags (hasChildren=true, initialized=true, etc.).
    // 7. Populate DetectedConnectionProperties (vendorName/Version, server type).
    // Synchronized because multiple tree refreshes could race here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Loads the Root DSE of the given browser connection.  This is the main
     * workhorse that populates the root of the browser tree.  It is
     * {@code synchronized} to prevent two concurrent callers from corrupting
     * the Root DSE model.
     *
     * <p>Sequence:</p>
     * <ol>
     *   <li>Clear existing children and attributes from the Root DSE.</li>
     *   <li>Execute two LDAP OBJECT-scope searches at "" — one for operational
     *       attributes ({@code +}) and one for user attributes ({@code *}).</li>
     *   <li>Build a list of base DN entries either from the explicit configured
     *       base DN, the {@code namingContexts} attribute, or a fallback
     *       ONELEVEL search (for servers like Novell eDirectory that publish an
     *       empty namingContext).</li>
     *   <li>Add the schema sub-entry and other directory metadata entries.</li>
     *   <li>Try to confirm each base DN entry exists via a separate search;
     *       uncache it if not found.</li>
     *   <li>Mark the Root DSE as fully initialized.</li>
     *   <li>Update the connection's {@link DetectedConnectionProperties}.</li>
     * </ol>
     *
     * @param browserConnection the connection whose Root DSE to load.
     * @param monitor           the progress monitor.
     */
    public static synchronized void loadRootDSE( IBrowserConnection browserConnection, StudioProgressMonitor monitor )
    {
        // clear old children
        InitializeChildrenRunnable.clearCaches( browserConnection.getRootDSE(), true );

        // delete old attributes
        IAttribute[] oldAttributes = browserConnection.getRootDSE().getAttributes();

        if ( oldAttributes != null )
        {
            for ( IAttribute oldAttribute : oldAttributes )
            {
                browserConnection.getRootDSE().deleteAttribute( oldAttribute );
            }
        }

        // load well-known Root DSE attributes and operational attributes
        ISearch search = new Search( null, browserConnection, Dn.EMPTY_DN, ISearch.FILTER_TRUE,
            ROOT_DSE_ATTRIBUTES, SearchScope.OBJECT, 0, 0, Connection.AliasDereferencingMethod.NEVER,
            Connection.ReferralHandlingMethod.IGNORE, false, null, false );
        SearchRunnable.searchAndUpdateModel( browserConnection, search, monitor );

        // Load all user attributes. This is done because the BEA "LDAP server" (so called) is stupid
        // enough not to accept searches where "+" and "*" are provided on the list of parameters.
        // We have to do two searches...
        search = new Search( null, browserConnection, Dn.EMPTY_DN, ISearch.FILTER_TRUE, new String[]
            { SchemaConstants.ALL_USER_ATTRIBUTES }, SearchScope.OBJECT, 0, 0,
            Connection.AliasDereferencingMethod.NEVER, Connection.ReferralHandlingMethod.IGNORE, false, null, false );
        SearchRunnable.searchAndUpdateModel( browserConnection, search, monitor );

        // the list of entries under the Root DSE
        Map<Dn, IEntry> rootDseEntries = new HashMap<Dn, IEntry>();

        // 1st: add base DNs, either the specified or from the namingContexts attribute
        if ( !browserConnection.isFetchBaseDNs() && browserConnection.getBaseDN() != null
            && !"".equals( browserConnection.getBaseDN().toString() ) ) //$NON-NLS-1$
        {
            // only add the specified base Dn
            Dn dn = browserConnection.getBaseDN();
            IEntry entry = browserConnection.getEntryFromCache( dn );

            if ( entry == null )
            {
                entry = new BaseDNEntry( dn, browserConnection );
                browserConnection.cacheEntry( entry );
            }
            rootDseEntries.put( dn, entry );
        }
        else
        {
            // get base DNs from namingContexts attribute
            Set<String> namingContextSet = new HashSet<String>();
            IAttribute attribute = browserConnection.getRootDSE().getAttribute( SchemaConstants.NAMING_CONTEXTS_AT );

            if ( attribute != null )
            {
                String[] values = attribute.getStringValues();

                for ( int i = 0; i < values.length; i++ )
                {
                    namingContextSet.add( values[i] );
                }
            }

            if ( !namingContextSet.isEmpty() )
            {
                for ( String namingContext : namingContextSet )
                {
                    if ( namingContext.length() > 0 && namingContext.charAt( namingContext.length() - 1 ) == ' ' )
                    {
                        namingContext = namingContext.substring( 0, namingContext.length() - 1 );
                    }

                    if ( !"".equals( namingContext ) ) //$NON-NLS-1$
                    {
                        try
                        {
                            Dn dn = new Dn( namingContext );
                            IEntry entry = browserConnection.getEntryFromCache( dn );

                            if ( entry == null )
                            {
                                entry = new BaseDNEntry( dn, browserConnection );
                                browserConnection.cacheEntry( entry );
                            }

                            rootDseEntries.put( dn, entry );
                        }
                        catch ( LdapInvalidDnException e )
                        {
                            monitor.reportError( BrowserCoreMessages.model__error_setting_base_dn, e );
                        }
                    }
                    else
                    {
                        // special handling of empty namingContext (Novell eDirectory):
                        // perform a one-level search and add all result DNs to the set
                        searchRootDseEntries( browserConnection, rootDseEntries, monitor );
                    }
                }
            }
            else
            {
                // special handling of non-existing namingContexts attribute (Oracle Internet Directory)
                // perform a one-level search and add all result DNs to the set
                searchRootDseEntries( browserConnection, rootDseEntries, monitor );
            }
        }

        // 2nd: add schema sub-entry
        IEntry[] schemaEntries = getDirectoryMetadataEntries( browserConnection, SchemaConstants.SUBSCHEMA_SUBENTRY_AT );

        for ( IEntry entry : schemaEntries )
        {
            if ( entry instanceof DirectoryMetadataEntry )
            {
                ( ( DirectoryMetadataEntry ) entry ).setSchemaEntry( true );
            }

            rootDseEntries.put( entry.getDn(), entry );
        }

        // get other meta data entries
        IAttribute[] rootDseAttributes = browserConnection.getRootDSE().getAttributes();

        if ( rootDseAttributes != null )
        {
            for ( IAttribute attribute : rootDseAttributes )
            {
                IEntry[] metadataEntries = getDirectoryMetadataEntries( browserConnection, attribute.getDescription() );

                for ( IEntry entry : metadataEntries )
                {
                    rootDseEntries.put( entry.getDn(), entry );
                }
            }
        }

        // try to init entries
        StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );

        for ( IEntry entry : rootDseEntries.values() )
        {
            initBaseEntry( entry, dummyMonitor );
        }

        // set flags
        browserConnection.getRootDSE().setHasMoreChildren( false );
        browserConnection.getRootDSE().setAttributesInitialized( true );
        browserConnection.getRootDSE().setInitOperationalAttributes( true );
        browserConnection.getRootDSE().setChildrenInitialized( true );
        browserConnection.getRootDSE().setHasChildrenHint( true );
        browserConnection.getRootDSE().setDirectoryEntry( true );

        // Set detected connection properties
        DetectedConnectionProperties detectedConnectionProperties = browserConnection.getConnection()
            .getDetectedConnectionProperties();
        IAttribute vendorNameAttribute = browserConnection.getRootDSE().getAttribute( "vendorName" ); //$NON-NLS-1$

        if ( ( vendorNameAttribute != null ) && ( vendorNameAttribute.getValueSize() > 0 ) )
        {
            detectedConnectionProperties.setVendorName( vendorNameAttribute.getStringValue() );
        }

        IAttribute vendorVersionAttribute = browserConnection.getRootDSE().getAttribute( "vendorVersion" ); //$NON-NLS-1$

        if ( ( vendorVersionAttribute != null ) && ( vendorVersionAttribute.getValueSize() > 0 ) )
        {
            detectedConnectionProperties.setVendorVersion( vendorVersionAttribute.getStringValue() );
        }

        IAttribute supportedControlAttribute = browserConnection.getRootDSE().getAttribute( "supportedControl" ); //$NON-NLS-1$

        if ( ( supportedControlAttribute != null ) && ( supportedControlAttribute.getValueSize() > 0 ) )
        {
            detectedConnectionProperties.setSupportedControls( Arrays.asList( supportedControlAttribute
                .getStringValues() ) );
        }

        IAttribute supportedExtensionAttribute = browserConnection.getRootDSE().getAttribute( "supportedExtension" ); //$NON-NLS-1$

        if ( ( supportedExtensionAttribute != null ) && ( supportedExtensionAttribute.getValueSize() > 0 ) )
        {
            detectedConnectionProperties.setSupportedExtensions( Arrays.asList( supportedExtensionAttribute
                .getStringValues() ) );
        }

        IAttribute supportedFeaturesAttribute = browserConnection.getRootDSE().getAttribute( "supportedFeatures" ); //$NON-NLS-1$

        if ( ( supportedFeaturesAttribute != null ) && ( supportedFeaturesAttribute.getValueSize() > 0 ) )
        {
            detectedConnectionProperties.setSupportedFeatures( Arrays.asList( supportedFeaturesAttribute
                .getStringValues() ) );
        }

        detectedConnectionProperties
            .setServerType( ServerTypeDetector.detectServerType( browserConnection.getRootDSE() ) );

        ConnectionCorePlugin.getDefault().getConnectionManager()
            .connectionUpdated( browserConnection.getConnection() );
    }


    // ── R2-D2 Confirms Each Sub-System Is Reachable ───────────────────────────
    // After building the list of base DNs, we do an OBJECT search for each one
    // to make sure it actually exists on this server.  If the server returns no
    // results for a DN (it might be on a different replica), we remove it from
    // the cache so a phantom branch doesn't appear in the tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Validates that the given entry actually exists on the server and adds it
     * to the Root DSE's children.  If the server returns no result (e.g. the
     * naming context is on a different server), we remove the entry from cache
     * rather than showing a phantom node.
     *
     * @param entry   the candidate Root DSE child entry to validate.
     * @param monitor the progress monitor (dummy — errors are silently ignored).
     */
    private static void initBaseEntry( IEntry entry, StudioProgressMonitor monitor )
    {
        IBrowserConnection browserConnection = entry.getBrowserConnection();
        Dn dn = entry.getDn();

        // search the entry
        AliasDereferencingMethod derefAliasMethod = browserConnection.getAliasesDereferencingMethod();
        ReferralHandlingMethod handleReferralsMethod = browserConnection.getReferralsHandlingMethod();
        ISearch search = new Search( null, browserConnection, dn, ISearch.FILTER_TRUE, ISearch.NO_ATTRIBUTES,
            SearchScope.OBJECT, 1, 0, derefAliasMethod, handleReferralsMethod, true, null, false );
        SearchRunnable.searchAndUpdateModel( browserConnection, search, monitor );

        ISearchResult[] results = search.getSearchResults();

        if ( results != null && results.length == 1 )
        {
            // add entry to Root DSE
            ISearchResult result = results[0];
            entry = result.getEntry();
            browserConnection.getRootDSE().addChild( entry );
        }
        else
        {
            // Dn exists in the Root DSE, but doesn't exist in directory
            browserConnection.uncacheEntryRecursive( entry );
        }
    }


    // ── R2-D2 Reads The Metadata Sub-System Registry ─────────────────────────
    // Given an attribute name from the Root DSE (e.g. "subschemaSubentry"),
    // reads its DN values, looks them up in the browser cache, and creates
    // DirectoryMetadataEntry stubs for any not already cached.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads DN values from the specified Root DSE attribute and returns them
     * as {@link DirectoryMetadataEntry} objects.  Uses the browser connection's
     * entry cache to avoid creating duplicates.
     *
     * @param browserConnection   the connection whose Root DSE to inspect.
     * @param metadataAttributeName the attribute name whose values are DNs
     *                            (e.g. {@code subschemaSubentry}).
     * @return array of {@link IEntry} stubs for those DNs, possibly empty.
     */
    private static IEntry[] getDirectoryMetadataEntries( IBrowserConnection browserConnection,
        String metadataAttributeName )
    {
        List<Dn> metadataEntryDnList = new ArrayList<Dn>();
        IAttribute attribute = browserConnection.getRootDSE().getAttribute( metadataAttributeName );

        if ( attribute != null )
        {
            String[] values = attribute.getStringValues();

            for ( String dn : values )
            {
                if ( dn != null && !"".equals( dn ) ) //$NON-NLS-1$
                {
                    try
                    {
                        metadataEntryDnList.add( new Dn( dn ) );
                    }
                    catch ( LdapInvalidDnException e )
                    {
                    }
                }
            }
        }

        IEntry[] metadataEntries = new IEntry[metadataEntryDnList.size()];

        for ( int i = 0; i < metadataEntryDnList.size(); i++ )
        {
            Dn dn = metadataEntryDnList.get( i );
            metadataEntries[i] = browserConnection.getEntryFromCache( dn );

            if ( metadataEntries[i] == null )
            {
                metadataEntries[i] = new DirectoryMetadataEntry( dn, browserConnection );
                metadataEntries[i].setDirectoryEntry( true );
                browserConnection.cacheEntry( metadataEntries[i] );
            }
        }

        return metadataEntries;
    }


    // ── R2-D2 Falls Back To A One-Level Scan ──────────────────────────────────
    // Some servers (Novell eDirectory, Oracle Internet Directory) don't expose
    // namingContexts properly.  We do a ONELEVEL search at the Root DSE to
    // discover top-level entries instead.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fallback method used when {@code namingContexts} is absent or empty.
     * Performs an LDAP ONELEVEL search at the Root DSE and adds the result DNs
     * to the {@code rootDseEntries} map.
     *
     * @param browserConnection the connection.
     * @param rootDseEntries    the map to populate with discovered entries.
     * @param monitor           the progress monitor.
     */
    private static void searchRootDseEntries( IBrowserConnection browserConnection, Map<Dn, IEntry> rootDseEntries,
        StudioProgressMonitor monitor )
    {
        ISearch search = new Search( null, browserConnection, Dn.EMPTY_DN, ISearch.FILTER_TRUE,
            ISearch.NO_ATTRIBUTES, SearchScope.ONELEVEL, 0, 0, Connection.AliasDereferencingMethod.NEVER,
            Connection.ReferralHandlingMethod.IGNORE, false, null, false );
        SearchRunnable.searchAndUpdateModel( browserConnection, search, monitor );

        ISearchResult[] results = search.getSearchResults();

        if ( results != null )
        {
            for ( ISearchResult searchResult : results )
            {
                IEntry entry = searchResult.getEntry();
                rootDseEntries.put( entry.getDn(), entry );
            }
        }
    }
}
