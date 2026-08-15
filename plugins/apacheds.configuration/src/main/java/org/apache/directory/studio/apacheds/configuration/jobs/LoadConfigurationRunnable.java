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

package org.apache.directory.studio.apacheds.configuration.jobs;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.LdapConstants;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapNoSuchObjectException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.config.ConfigPartitionInitializer;
import org.apache.directory.server.config.ConfigPartitionReader;
import org.apache.directory.server.config.ReadOnlyConfigurationPartition;
import org.apache.directory.server.config.beans.ConfigBean;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.partition.impl.btree.AbstractBTreePartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.apache.directory.studio.apacheds.configuration.editor.Configuration;
import org.apache.directory.studio.apacheds.configuration.editor.ConnectionServerConfigurationInput;
import org.apache.directory.studio.apacheds.configuration.editor.NewServerConfigurationInput;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditor;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResult;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;


// ── CLASS: LoadConfigurationRunnable — REBEL INTELLIGENCE COURIER ─────────────
// Jyn Erso's team races to retrieve the Death Star plans from wherever they're
// stored — a physical data card, a directory full of split files, or a live
// Imperial uplink.  Whatever the source, the courier decodes the raw data and
// hands a clean Configuration object back to Rebel command.
// This class is that courier: a background job that reads an ApacheDS config
// LDIF (single file, multi-file directory, or live LDAP connection) and returns
// a parsed Configuration for the editor to display and edit.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background job ({@link StudioRunnableWithProgress}) that loads an ApacheDS
 * server configuration from whatever source the editor is pointed at.
 * The source can be a default template (new server), a single {@code config.ldif}
 * file, a multi-file {@code ou=config} directory, or a live LDAP connection to a
 * running ApacheDS instance.
 * Think of this class as a Rebel intelligence courier — she retrieves and decodes
 * the stolen Death Star plans from a data card or a live uplink, then hands the
 * clean blueprint back to Rebel command so they can plan the attack.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LoadConfigurationRunnable implements StudioRunnableWithProgress
{
    /** The associated editor */
    private ServerConfigurationEditor editor;


    // ── Courier Accepts the Mission Briefing ─────────────────────────────────
    // General Draven hands the courier her mission dossier — it contains the
    // editor reference she needs to report back to once the plans are in hand.
    // We stash the editor here so run() and getConfiguration() know where to
    // deliver the loaded Configuration once the retrieval is complete.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Wires this runnable to the editor that kicked off the load.
     * We hold onto the editor so we can call {@code configurationLoaded()} or
     * {@code configurationLoadFailed()} on the UI thread when we're done.
     *
     * @param editor  the ServerConfigurationEditor that triggered this load job
     */
    public LoadConfigurationRunnable( ServerConfigurationEditor editor )
    {
        super();
        this.editor = editor;
    }


    // ── Courier Radios the Distress Signal ───────────────────────────────────
    // If something goes wrong mid-mission, the courier sends back a coded error
    // message so headquarters knows exactly what failed and can act accordingly.
    // This method returns the localised "Unable to load configuration" string
    // that the Studio job framework will show if the run() call throws.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable error message shown when the load job fails.
     * The message is looked up from the plugin's message bundle so it can be
     * localised for different languages.
     *
     * @return  a short description of the failure, e.g. "Unable to load configuration"
     */
    public String getErrorMessage()
    {
        return Messages.getString( "LoadConfigurationRunnable.UnableToLoadConfiguration" ); //$NON-NLS-1$
    }


    // ── Courier Works Alone, Needs No Escort ─────────────────────────────────
    // The courier reads her data independently and doesn't block or compete
    // with any other ongoing operations — no convoy needed, no shared resources
    // to guard.  Returning an empty array tells the Studio job framework that
    // this runnable holds no exclusive locks and won't deadlock other jobs.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reports which objects this job holds exclusive locks on — none, in our case.
     * The Studio job framework uses this list to detect and prevent deadlocks
     * between concurrent jobs.
     *
     * @return  an empty array because we don't lock any shared objects
     */
    public Object[] getLockedObjects()
    {
        return new Object[0];
    }


    // ── Courier Announces Her Mission Codename ───────────────────────────────
    // Every Rebel mission has a codename so the briefing room board can track
    // its status.  This one is "Load Configuration" — short and unmistakable.
    // getName() supplies the display label the Eclipse Jobs progress dialog
    // shows while this runnable is executing in the background.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this job, shown in the Eclipse progress monitor
     * while the configuration is being loaded.
     *
     * @return  the localised job name, e.g. "Load Configuration"
     */
    public String getName()
    {
        return Messages.getString( "LoadConfigurationRunnable.LoadConfiguration" ); //$NON-NLS-1$
    }


    // ── Courier Executes the Full Retrieval Mission ───────────────────────────
    // The courier kicks off the real work: figure out where the plans are stored,
    // fetch them, and — if all goes well — hop back to Rebel command on the UI
    // thread to hand the Configuration to the editor.
    // If anything explodes mid-mission she radios the error back through the
    // monitor and tells the editor the mission failed so the user gets a useful
    // error dialog rather than a silent blank screen.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * The main entry point for this background job.
     * We grab the editor's input, call {@link #getConfiguration} to load the
     * config from whatever source it points at, then push the result back to
     * the UI thread via {@code asyncExec}.  Any exception is forwarded to both
     * the progress monitor and the editor.
     *
     * @param monitor  the Studio progress monitor used to track and report progress
     */
    public void run( StudioProgressMonitor monitor )
    {
        IEditorInput input = editor.getEditorInput();

        try
        {
            final Configuration configuration = getConfiguration( input, monitor );

            if ( configuration != null )
            {
                Display.getDefault().asyncExec( new Runnable()
                {
                    public void run()
                    {
                        editor.configurationLoaded( configuration );
                    }
                } );
            }
        }
        catch ( Exception e )
        {
            ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                new Status( Status.ERROR, "org.apache.directory.studio.apacheds.configuration",
                    e.getMessage() ) );

            // Reporting the error to the monitor
            monitor.reportError( e );

            // Reporting the error to the editor
            final Exception exception = e;

            Display.getDefault().asyncExec( new Runnable()
            {
                public void run()
                {
                    editor.configurationLoadFailed( exception );
                }
            } );
        }
    }


    // ── Courier Identifies the Source of the Plans ───────────────────────────
    // Before running, the courier checks the mission dossier to decide where to
    // go: a template vault for brand-new servers, a live Imperial uplink for a
    // running server, or a filing cabinet on the local filesystem for an existing
    // config file.  Each branch delegates to the right read helper and returns
    // the decoded Configuration, or null if the source type is unrecognised.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Inspects the editor input and dispatches to the right reader for that
     * source type.
     *
     * <p>For example — Jyn figures out where the plans are stored:</p>
     * <pre>
     *   New server?      pull the bundled default config.ldif from the OSGi bundle.
     *   Live connection? query the running ApacheDS server over LDAP.
     *   Workspace file?  read config.ldif or ou=config directory from disk.
     * </pre>
     *
     * @param input    the editor input describing where the config lives
     * @param monitor  the progress monitor for the running job
     * @return         the parsed Configuration, or {@code null} if the input type
     *                 is not recognised
     * @throws Exception  if reading or parsing the configuration fails
     */
    public Configuration getConfiguration( IEditorInput input, StudioProgressMonitor monitor ) throws Exception
    {
        String inputClassName = input.getClass().getName();

        // If the input is a NewServerConfigurationInput, then we only
        // need to get the server configuration and return
        if ( input instanceof NewServerConfigurationInput )
        {
            Bundle bundle = Platform.getBundle( "org.apache.directory.server.config" );
            URL resource = bundle.getResource( "config.ldif" );
            InputStream is = resource.openStream();
            return readSingleFileConfiguration( is );
        }

        // If the input is a ConnectionServerConfigurationInput, then we
        // read the server configuration from the selected connection
        if ( input instanceof ConnectionServerConfigurationInput )
        {
            return readConfiguration( ( ConnectionServerConfigurationInput ) input, monitor );
        }
        else if ( input instanceof FileEditorInput )
        // The 'FileEditorInput' class is used when the file is opened
        // from a project in the workspace.
        {
            File file = ( ( FileEditorInput ) input ).getFile().getLocation().toFile();
            return readConfiguration( file );
        }
        else if ( input instanceof IPathEditorInput )
        {
            File file = ( ( IPathEditorInput ) input ).getPath().toFile();
            return readConfiguration( file );
        }
        else if ( inputClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" ) //$NON-NLS-1$
            || inputClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) ) //$NON-NLS-1$
        // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
        // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
        // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
        // opening a file from the menu File > Open... in Eclipse 3.3.x
        {
            // We use the tooltip to get the full path of the file
            File file = new File( input.getToolTipText() );
            return readConfiguration( file );
        }

        return null;
    }


    // ── Courier Reads the Data Card From the Filing Cabinet ──────────────────
    // The courier opens the local filing cabinet and checks what kind of card
    // she's dealing with: a single-file config.ldif, or the ou.ldif entry-point
    // that heads up a multi-file directory layout.  She then passes it to the
    // matching reader and brings the decoded plans back to command.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reads an ApacheDS configuration from a local {@link File}.
     * We check the filename to decide whether this is a single {@code config.ldif}
     * or the {@code ou.ldif} entry-point of a multi-file ou=config directory, and
     * delegate to the appropriate reader.
     *
     * @param file  the config file on disk ({@code config.ldif} or {@code ou.ldif})
     * @return      the parsed Configuration, or {@code null} if the file is null
     *              or the filename is not recognised
     * @throws Exception  if reading or parsing the file fails
     */
    public static Configuration readConfiguration( File file ) throws Exception
    {
        if ( file != null )
        {
            if(file.getName().equals( ApacheDS2ConfigurationPluginConstants.CONFIG_LDIF )) {
                return readSingleFileConfiguration( file );
            }
            else if(file.getName().equals( ApacheDS2ConfigurationPluginConstants.OU_CONFIG_LDIF )) {
                return readMultiFileConfigureation( file.getParentFile() );
            }
        }

        return null;
    }


    // ── Courier Pops the Data Card Into the Reader ───────────────────────────
    // The courier slots the physical data card into the card reader, which turns
    // it into a raw byte stream.  She then hands that stream off to the
    // stream-based reader, which does the actual LDIF parsing and decoding.
    // This method is just the thin adapter that opens the FileInputStream.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link FileInputStream} on the given config.ldif file and delegates
     * to {@link #readSingleFileConfiguration(InputStream)}.
     * Just a thin adapter so callers can pass a {@link File} rather than managing
     * the stream themselves.
     *
     * @param configLdifFile  the config.ldif file to read
     * @return                the parsed Configuration
     * @throws Exception      if the file can't be opened or the config is malformed
     */
    private static Configuration readSingleFileConfiguration( File configLdifFile ) throws Exception
    {
        InputStream is = new FileInputStream( configLdifFile );

        // Reading the configuration partition
        return readSingleFileConfiguration( is );
    }


    // ── Courier Decodes the Plans From a Raw Data Stream ────────────────────
    // Whether the data card came from a physical drive or a live uplink, at
    // some point it becomes a raw stream of bytes.  The courier feeds that
    // stream into the ReadOnlyConfigurationPartition decoder, which parses the
    // LDIF bytes into structured entries, then hands the assembled partition
    // to the final reader for interpretation.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Parses a single config.ldif from an {@link InputStream} into a Configuration.
     * We wrap the stream in a {@link ReadOnlyConfigurationPartition}, initialise it
     * (which triggers the LDIF parse), then call the partition-based reader to
     * build the final Configuration object model.
     *
     * @param is  the input stream containing the raw LDIF data
     * @return    the parsed Configuration
     * @throws Exception  if the LDIF is malformed or schema validation fails
     */
    private static Configuration readSingleFileConfiguration( InputStream is ) throws Exception
    {
        // Creating a partition associated from the input stream
        ReadOnlyConfigurationPartition configurationPartition = new ReadOnlyConfigurationPartition( is,
            ApacheDS2ConfigurationPlugin.getDefault().getSchemaManager() );

        configurationPartition.initialize();

        // Reading the configuration partition
        return readConfiguration( configurationPartition );
    }


    // ── Rebel Team Reassembles Multi-Segment Plans ───────────────────────────
    // The Death Star plans were split across dozens of data discs stored in
    // separate filing drawers — exactly the multi-file layout ApacheDS uses for
    // its ou=config directory.  The team feeds all the drawers to the
    // ConfigPartitionInitializer, which reassembles the segments in order before
    // handing them off for reading.  Synchronized because only one thread should
    // be reassembling the layout at a time to avoid interleaving file reads.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reads an ApacheDS configuration from a multi-file ou=config directory layout.
     * We let {@link ConfigPartitionInitializer} assemble all the individual LDIF
     * files into a single {@link LdifPartition}, then pass that to the partition
     * reader.  Synchronized because the initializer touches shared filesystem
     * resources and isn't thread-safe.
     *
     * @param confDirectory  the directory containing the ou=config subtree files
     * @return               the parsed Configuration
     * @throws Exception     if the directory can't be read or the config is malformed
     */
    private static synchronized Configuration readMultiFileConfigureation( File confDirectory ) throws Exception
    {
        InstanceLayout instanceLayout = new InstanceLayout( confDirectory.getParentFile() );

        SchemaManager schemaManager = ApacheDS2ConfigurationPlugin.getDefault().getSchemaManager();

        DnFactory dnFactory = null;

        ConfigPartitionInitializer init = new ConfigPartitionInitializer( instanceLayout, dnFactory, schemaManager );
        LdifPartition configurationPartition = init.initConfigPartition();

        return readConfiguration( configurationPartition );
    }


    // ── Analyst Decodes the Assembled Partition Into a Blueprint ─────────────
    // With all the data card segments assembled into a single coherent partition,
    // the Rebel analyst feeds it through the ConfigPartitionReader — the decoder
    // ring that turns raw LDAP entries into a structured, strongly-typed object
    // model.  That model is then wrapped in our Configuration and returned.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reads a {@link ConfigBean} from the given partition and wraps it in a
     * {@link Configuration}.
     * This is the final decoding step — the {@link ConfigPartitionReader} walks
     * every entry in the partition and assembles the strongly-typed configuration
     * object model that the editor works with.
     *
     * @param partition  the already-initialised partition holding the config entries
     * @return           the parsed Configuration, or {@code null} if the partition is null
     * @throws LdapException  if reading an entry from the partition fails
     */
    private static Configuration readConfiguration( AbstractBTreePartition partition ) throws LdapException
    {
        if ( partition != null )
        {
            ConfigPartitionReader cpReader = new ConfigPartitionReader( partition );
            ConfigBean configBean = cpReader.readConfig();
            return new Configuration( configBean, partition );
        }

        return null;
    }


    // ── Courier Downloads Plans Over a Live Imperial Uplink ──────────────────
    // Rather than grabbing a static data card, the courier taps directly into
    // the live Imperial network, walks the entire ou=config subtree entry by
    // entry (breadth-first), and copies every node into a fresh in-memory
    // partition.  Once the copy is complete she reads the Configuration locally
    // so the live server is never burdened with repeated queries later on.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Loads the configuration by searching a live ApacheDS server over LDAP.
     * We open the connection, search down the {@code ou=config} subtree one level
     * at a time (breadth-first), copy every entry into a fresh
     * {@link EntryBasedConfigurationPartition}, store that partition on the input
     * for later diffing, then decode the Configuration from the local copy.
     *
     * <p>For example — Jyn taps the live Imperial uplink:</p>
     * <pre>
     *   1. Open connection and authenticate (bind).
     *   2. Fetch the root ou=config entry.
     *   3. Walk children level by level, copying into the in-memory partition.
     *   4. Decode the completed partition into a Configuration object.
     * </pre>
     *
     * @param input    the connection input carrying the live LDAP connection details
     * @param monitor  the progress monitor — we throw its exception if errors are reported
     * @return         the parsed Configuration, or {@code null} if input is null
     * @throws Exception  if the connection fails, ou=config isn't found, or parsing fails
     */
    private Configuration readConfiguration( ConnectionServerConfigurationInput input, StudioProgressMonitor monitor ) throws Exception
    {
        if ( input != null )
        {
            SchemaManager schemaManager = ApacheDS2ConfigurationPlugin.getDefault().getSchemaManager();

            // Getting the browser connection associated with the connection in the input
            IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnection( input.getConnection() );

            // Creating and initializing the configuration partition
            EntryBasedConfigurationPartition configurationPartition = new EntryBasedConfigurationPartition(
                schemaManager );
            configurationPartition.initialize();

            // Opening the connection
            openConnection( input, monitor );

            // Creating the search parameter
            SearchParameter configSearchParameter = new SearchParameter();
            configSearchParameter.setSearchBase( new Dn( ServerDNConstants.CONFIG_DN ) ); //$NON-NLS-1$
            //configSearchParameter.setSearchBase( new Dn( "ou=config" ) ); //$NON-NLS-1$
            configSearchParameter.setFilter( LdapConstants.OBJECT_CLASS_STAR ); //$NON-NLS-1$
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
                // Creating the 'ou=config' base entry
                StudioSearchResult searchResult = enumeration.next();
                configEntry = new DefaultEntry( schemaManager, searchResult.getEntry() );
            }

            enumeration.close();

            // Verifying we found the 'ou=config' base entry
            if ( configEntry == null )
            {
                ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                    new Status( Status.ERROR, "org.apache.directory.studio.apacheds.configuration",
                        Messages.getString( "LoadConfigurationRunnable.UnableToFindConfigBaseEntry" ) ) );
                throw new LdapNoSuchObjectException(
                    Messages.getString( "LoadConfigurationRunnable.UnableToFindConfigBaseEntry" ) ); //$NON-NLS-1$
            }

            // Creating a list to hold the entries that need to be checked
            // for children and added to the partition
            List<Entry> entries = new ArrayList<Entry>();
            entries.add( configEntry );

            // Looping on the entries list until it's empty
            while ( !entries.isEmpty() )
            {
                // Removing the first entry from the list
                Entry entry = entries.remove( 0 );

                // Adding the entry to the partition
                configurationPartition.addEntry( entry );

                SearchParameter searchParameter = new SearchParameter();
                searchParameter.setSearchBase( entry.getDn() );
                searchParameter.setFilter( LdapConstants.OBJECT_CLASS_STAR ); //$NON-NLS-1$
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
                    // Adding the children to the list of entries
                    StudioSearchResult searchResult = childrenEnumeration.next();
                    entries.add( new DefaultEntry( schemaManager, searchResult.getEntry() ) );
                }

                childrenEnumeration.close();
            }

            // Setting the created partition to the input
            input.setOriginalPartition( configurationPartition );

            return readConfiguration( configurationPartition );
        }

        return null;
    }


    // ── Courier Patches Into the Imperial Network ────────────────────────────
    // Before downloading anything the courier has to establish a secure channel:
    // connect at the transport layer, authenticate with credentials (bind), and
    // notify all registered listeners that the link is now live and open.
    // If the connection is already established we skip straight past this and
    // go directly to reading the configuration data.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Opens and authenticates the LDAP connection if it isn't already connected.
     * After connecting and binding, we fire {@link ConnectionEventRegistry#fireConnectionOpened}
     * so that any other Studio components watching this connection know it's live.
     * If the connection is already open this method is a no-op.
     *
     * @param input    the editor input carrying the connection to open
     * @param monitor  the progress monitor — connect/bind errors surface here
     */
    private void openConnection( ConnectionServerConfigurationInput input, StudioProgressMonitor monitor )
    {
        Connection connection = input.getConnection();

        if ( connection != null && !connection.getConnectionWrapper().isConnected() )
        {
            connection.getConnectionWrapper().connect( monitor );

            if ( connection.getConnectionWrapper().isConnected() )
            {
                connection.getConnectionWrapper().bind( monitor );
            }

            if ( connection.getConnectionWrapper().isConnected() )
            {
                for ( IConnectionListener listener : ConnectionCorePlugin.getDefault()
                    .getConnectionListeners() )
                {
                    listener.connectionOpened( connection, monitor );
                }

                ConnectionEventRegistry.fireConnectionOpened( connection, input );
            }
        }
    }
}
