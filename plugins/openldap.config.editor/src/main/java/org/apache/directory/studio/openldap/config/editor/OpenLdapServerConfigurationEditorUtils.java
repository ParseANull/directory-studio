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
package org.apache.directory.studio.openldap.config.editor;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.csn.CsnFactory;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.ObjectClassRegistry;
import org.apache.directory.api.ldap.util.tree.DnNode;
import org.apache.directory.api.util.DateUtils;
import org.apache.directory.api.util.TimeProvider;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.filesystem.PathEditorInput;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.jobs.ExecuteLdifRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.ExpandedLdifUtils;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.jobs.EntryBasedConfigurationPartition;
import org.apache.directory.studio.openldap.config.jobs.PartitionsDiffComputer;
import org.apache.directory.studio.openldap.config.model.OpenLdapConfiguration;
import org.apache.directory.studio.openldap.config.model.io.ConfigurationException;
import org.apache.directory.studio.openldap.config.model.io.ConfigurationReader;
import org.apache.directory.studio.openldap.config.model.io.ConfigurationUtils;
import org.apache.directory.studio.openldap.config.model.io.ConfigurationWriter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;


// ── CLASS: OpenLdapServerConfigurationEditorUtils — Yoda Lifts The X-Wing ────
// On Dagobah, Yoda doesn't struggle with Luke's X-wing the way Luke does —
// he simply closes his eyes, extends a hand, and the ship rises smoothly from
// the swamp.  All the messy physics of mud, mass, and displacement are handled
// invisibly; Luke just sees the result.
// This utility class is that invisible force: it takes the heavy lifting out
// of the editor — opening file dialogs, converting config beans to LDIF entries,
// writing them to disk, computing diffs, pushing changes to a live server —
// so the editor itself stays clean.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility methods that handle the complex I/O operations for the
 * OpenLDAP configuration editor.
 * This class is a non-instantiable helper — everything is static.  It covers
 * three main concerns: (1) prompting the user for a save location via dialogs,
 * (2) converting a config model to LDIF and writing it to disk, and (3) pushing
 * config changes to a live LDAP server by computing and executing a diff.
 * Think of Yoda on Dagobah: all the heavy Force-lifting is here so the editor
 * class doesn't have to break a sweat.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapServerConfigurationEditorUtils
{
    // ── Yoda Does Not Make Small Talk ─────────────────────────────────────────
    // Yoda is a vessel for the Force, not a chatty tourist — he doesn't exist
    // as an individual you can instantiate and have a conversation with.
    // We prevent instantiation because all methods here are static utilities;
    // there's no state to hold.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Private constructor — this is a static utility class and should never
     * be instantiated.
     */
    private OpenLdapServerConfigurationEditorUtils()
    {
        // Do nothing
    }


    // ── Yoda Opens A Portal To The Filesystem ─────────────────────────────────
    // Yoda extends the Force across the swamp, reaching out to sense the
    // X-wing's exact position in the murk — gathering the coordinates he
     // needs before he can act.
    // We must open the file dialog on the SWT UI thread; if called from a
    // background thread we'd get an invalid access error, so we syncExec.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Opens an SWT {@link FileDialog} on the UI thread and returns the path
     * the user selected (or {@code null} if they cancelled).
     * Because SWT widgets must be accessed from the display thread, we use
     * {@code Display.getDefault().syncExec()} to marshal the call safely.
     *
     * @param shell  the parent shell for the dialog
     * @return the selected file path, or {@code null} if cancelled
     */
    private static String openFileDialogInUIThread( final Shell shell )
    {
        // Defining our own encapsulating class for the result
        class DialogResult
        {
            private String result;

            public String getResult()
            {
                return result;
            }


            public void setResult( String result )
            {
                this.result = result;
            }
        }

        // Creating an object to hold the result
        final DialogResult result = new DialogResult();

        // Opening the dialog in the UI thread
        Display.getDefault().syncExec( () ->
            {
                FileDialog dialog = new FileDialog( shell, SWT.SAVE );
                result.setResult( dialog.open() );
            } );

        return result.getResult();
    }


    // ── Yoda Levitates The Ship To A New Resting Spot ─────────────────────────
    // Luke asks Yoda to move the X-wing from the swamp to dry land — Yoda
    // lifts it and sets it down somewhere new, leaving the original location
    // empty and the ship intact.
    // We prompt the user for a new directory, validate it, write the config
    // there, and return a new editor input pointing at the new location.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Performs the "Save As" operation: prompts the user for a new directory,
     * validates it, writes the configuration there, and optionally returns a
     * new editor input pointing at the new location.
     * In RCP mode we loop until the user picks a valid, writable, empty
     * directory (or cancels).  In IDE mode (not yet implemented) the behavior
     * may differ.
     *
     * @param configuration  the configuration model to write out
     * @param shell          the parent shell for dialogs
     * @param newInput       whether to return a new editor input after saving;
     *                       pass {@code true} when you want the editor to
     *                       switch to the new location
     * @return a new {@link IEditorInput} pointing at the saved location if
     *         {@code newInput} is true, or {@code null} if cancelled or if
     *         {@code newInput} is false
     * @throws Exception  if writing the configuration fails
     */
    public static IEditorInput saveAs( OpenLdapConfiguration configuration, Shell shell, boolean newInput ) throws Exception
    {
        // detect IDE or RCP:
        // check if perspective org.eclipse.ui.resourcePerspective is available
        boolean isIDE = CommonUIUtils.isIDEEnvironment();
        String path = null;

        if ( isIDE )
        {
        }
        else
        {
            boolean canOverwrite = false;

            while ( !canOverwrite )
            {
                // Opening the dialog
                // Open FileDialog
                path = openFileDialogInUIThread( shell );

                // Checking the returned path
                if ( path == null )
                {
                    // Cancel button has been clicked
                    return null;
                }

                // Getting the directory indicated by the user
                final File directory = new File( path );

                // Checking if the directory exists
                if ( !directory.exists() )
                {
                    CommonUIUtils.openErrorDialog( "The directory does not exist." );
                    continue;
                }

                // Checking if the location is a directory
                if ( !directory.isDirectory() )
                {
                    CommonUIUtils.openErrorDialog( "The location is not a directory." );
                    continue;
                }

                // Checking if the directory is writable
                if ( !directory.canWrite() )
                {
                    CommonUIUtils.openErrorDialog( "The directory is not writable." );
                    continue;
                }

                // Checking if the directory is empty
                if ( !isEmpty( directory ) )
                {
                    CommonUIUtils.openErrorDialog( "The directory is not empty." );
                    continue;
                }

                // The directory meets all requirements
                break;
            }
        }

        // Saving the file to disk
        saveConfiguration( configuration, new File( path ) );

        // Checking if a new input is required
        if ( newInput )
        {
            // Creating the new input for the editor
            return new PathEditorInput( new Path( path ) );
        }
        else
        {
            return null;
        }
    }


    // ── Yoda Checks The Swamp For Hidden Debris ───────────────────────────────
    // Before Yoda lifts the X-wing, he senses whether anything hidden under
    // the murk would block the ship's path — he won't move it into a tangle
    // of roots.
    // We check if a directory has any visible files before letting the user
    // save there — writing into a non-empty slapd.d would corrupt existing config.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the directory contains no visible files.
     * We filter out dot-files (hidden files on Unix) because slapd.d dirs
     * sometimes contain a .gitkeep or similar placeholder that shouldn't
     * block saving.
     *
     * @param directory  the directory to inspect — may be null, in which case
     *                   we return false (a null directory is not "empty")
     * @return {@code true} if no visible files exist in the directory
     */
    private static boolean isEmpty( File directory )
    {
        if ( directory != null )
        {
            String[] children = directory.list( ( dir, name ) ->
                    // Only accept visible files (which don't start with a dot).
                    !name.startsWith( "." )
                );

            return ( ( children == null ) || ( children.length == 0 ) );
        }

        return false;
    }


    // ── Yoda Opens A Portal To A Different Cavern ─────────────────────────────
    // When the terrain changes, Yoda senses the new lay of the land through
    // the Force — feeling out which cavern to use before committing the ship.
    // We open an SWT DirectoryDialog on the UI thread for cases where we need
    // the user to choose a folder (rather than a file).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Opens an SWT {@link DirectoryDialog} on the UI thread and returns the
     * directory path the user selected (or {@code null} if cancelled).
     * Like {@link #openFileDialogInUIThread}, we must syncExec to stay on
     * the SWT display thread.
     *
     * @param dialog  the pre-configured DirectoryDialog to open
     * @return the selected directory path, or {@code null} if cancelled
     */
    private static String openDirectoryDialogInUIThread( final DirectoryDialog dialog )
    {
        // Defining our own encapsulating class for the result
        class DialogResult
        {
            private String result;


            public String getResult()
            {
                return result;
            }


            public void setResult( String result )
            {
                this.result = result;
            }
        }

        // Creating an object to hold the result
        final DialogResult result = new DialogResult();

        // Opening the dialog in the UI thread
        Display.getDefault().syncExec( () -> result.setResult( dialog.open() ) );

        return result.getResult();
    }


    // ── Yoda Sets The Ship Down Without A Browser Connection ──────────────────
    // Sometimes Yoda lifts the X-wing purely through the Force, no co-pilot
    // needed — when there's no live connection to route through, he works alone.
    // We delegate to the full overload with a null browser connection for
    // cases where we're writing directly to disk without a live LDAP connection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Saves the configuration to the given directory without a browser
     * connection — used for direct-to-disk saves.
     * Delegates to {@link #saveConfiguration(IBrowserConnection, OpenLdapConfiguration, File)}
     * with a null connection.
     *
     * @param configuration  the configuration model to serialize
     * @param directory      the target directory to write LDIF files into
     * @throws Exception  if serialization or writing fails
     */
    public static void saveConfiguration( OpenLdapConfiguration configuration, File directory ) throws Exception
    {
        saveConfiguration( null, configuration, directory );
    }


    // ── Yoda Lifts And Places The X-Wing With Full Precision ─────────────────
    // Yoda lifts the X-wing from the swamp, dresses each component (adding
    // timestamps to each panel, stamping the registry UUID on the hull),
    // and sets it down exactly where it needs to be.
    // We convert the config model to LDIF entries, annotate each with
    // operational attributes (timestamps, CSN, UUID), build a DN tree,
    // and write it all to the target directory as slapd.d LDIF files.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Converts the configuration model to LDIF and writes it as a slapd.d
     * directory tree.
     * This is the core save-to-disk operation.  For each LDIF entry produced
     * by the config writer, we inject operational attributes (createTimestamp,
     * entryCSN, entryUUID, etc.) and then use {@link ExpandedLdifUtils} to
     * write the whole tree to the target directory.
     *
     * @param browserConnection  an optional browser connection; used by the
     *                           config writer for schema resolution — pass
     *                           null when saving without a live server
     * @param configuration      the configuration model to serialize
     * @param directory          the target slapd.d directory to write into
     * @throws Exception  if entry conversion, tree building, or file writing fails
     */
    public static void saveConfiguration( IBrowserConnection browserConnection, OpenLdapConfiguration configuration,
        File directory ) throws Exception
    {
        // Creating the configuration writer
        ConfigurationWriter configurationWriter = new ConfigurationWriter( browserConnection, configuration );

        SchemaManager schemaManager = OpenLdapConfigurationPlugin.getDefault().getSchemaManager();

        // Converting the configuration beans to entries
        List<LdifEntry> entries = configurationWriter.getConvertedLdifEntries( ConfigurationUtils
            .getDefaultConfigurationDn() );

        // Creating a tree to store entries
        DnNode<Entry> tree = new DnNode<>();

        CsnFactory csnFactory = new CsnFactory( 1 );

        for ( LdifEntry entry : entries )
        {
            // Getting the current generalized time
            String currentgeGeneralizedTime = DateUtils.getGeneralizedTime( TimeProvider.DEFAULT );

            // 'createTimestamp' attribute
            entry.addAttribute( "createTimestamp", currentgeGeneralizedTime );

            // 'creatorsName' attribute
            entry.addAttribute( "creatorsName", "cn=config" );

            // 'entryCSN' attribute
            entry.addAttribute( "entryCSN", csnFactory.newInstance().toString() );

            // 'entryUUID' attribute
            entry.addAttribute( "entryUUID", UUID.randomUUID().toString() );

            // 'modifiersName' attribute
            entry.addAttribute( "modifiersName", "cn=config" );

            // 'modifyTimestamp' attribute
            entry.addAttribute( "modifyTimestamp", currentgeGeneralizedTime );

            // 'structuralObjectClass' attribute
            entry.addAttribute( "structuralObjectClass", getStructuralObjectClass( entry ) );

            // Adding the entry to tree
            tree.add( new Dn( schemaManager, entry.getDn() ), entry.getEntry() );
        }

        try
        {
            Dn rootDn = ConfigurationUtils.getDefaultConfigurationDn();
            ExpandedLdifUtils.write( tree, new Dn( schemaManager, rootDn ), directory );
        }
        catch ( ConfigurationException e )
        {
            throw new IOException( e );
        }
    }


    // ── Yoda Reads The Ship's Structural Class Marking ───────────────────────
    // Before setting the X-wing down, Yoda identifies the ship's class —
    // is it a fighter, a freighter, a capital ship? — so it can be filed
    // correctly in the hangar registry.
    // We find the highest (most-specific) structural object class for an LDIF
    // entry so we can set the structuralObjectClass operational attribute
    // correctly before writing to disk.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name of the highest structural object class for the given
     * LDIF entry.
     * LDAP entries have a hierarchy of object classes; the structural one
     * is the most specific and must be recorded in the
     * {@code structuralObjectClass} operational attribute when writing slapd.d
     * files.  Falls back to {@code "top"} if nothing more specific is found.
     *
     * @param ldifEntry  the entry to inspect — may be null, in which case
     *                   we return {@code "top"}
     * @return the structural object class name, or {@code "top"} as a fallback
     * @throws ConfigurationException  if schema lookup fails unexpectedly
     */
    private static Object getStructuralObjectClass( LdifEntry ldifEntry ) throws ConfigurationException
    {
        if ( ldifEntry != null )
        {
            Entry entry = ldifEntry.getEntry();

            if ( entry != null )
            {
                ObjectClass structuralObjectClass = ConfigurationReader
                    .getHighestStructuralObjectClass( entry.get( SchemaConstants.OBJECT_CLASS_AT ) );

                if ( structuralObjectClass != null )
                {
                    return structuralObjectClass.getName();
                }
            }
        }

        return SchemaConstants.TOP_OC;
    }


    // ── Yoda Pushes The X-Wing Changes Back Into The Swamp ───────────────────
    // After Luke tinkers with the X-wing, Yoda computes exactly what changed
    // versus how it was when they pulled it out — and applies only those
    // targeted repairs, leaving everything else untouched.
    // We diff the original partition against the modified one, turn the diff
    // into LDIF modify operations, and execute them against the live server.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Saves the configuration to a live LDAP server by computing the diff
     * between the original snapshot and the current (edited) state, then
     * executing the resulting LDAP modifications.
     * This is the connection-backed save path.  We suspend LDAP event firing
     * during the operation so the browser doesn't react to our own changes.
     * If the server reports errors, we throw an {@link Exception} with a
     * descriptive message.  On success, we swap {@code input}'s original
     * partition to the new state so the next save diffs from the correct baseline.
     *
     * @param input    the connection-backed input carrying both the connection
     *                 and the original partition snapshot
     * @param editor   the editor holding the current configuration model
     * @param monitor  the progress monitor for reporting
     * @throws Exception  if the LDAP modification execution fails or the server
     *                    reports errors
     */
    public static void saveConfiguration( ConnectionServerConfigurationInput input, OpenLdapServerConfigurationEditor editor,
        IProgressMonitor monitor ) throws Exception
    {
        // Getting the browser connection associated with the connection in the input
        IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
            .getBrowserConnection( input.getConnection() );

        // Creating the configuration writer
        ConfigurationWriter configurationWriter = new ConfigurationWriter( browserConnection, editor.getConfiguration() );

        // Getting the original configuration partition and its schema manager
        EntryBasedConfigurationPartition originalPartition = input.getOriginalPartition();
        SchemaManager schemaManager = originalPartition.getSchemaManager();

        // Suspends event firing in current thread.
        ConnectionEventRegistry.suspendEventFiringInCurrentThread();

        try
        {
            // Creating a new configuration partition
            EntryBasedConfigurationPartition modifiedPartition = createConfigurationPartition( schemaManager,
                originalPartition.getSuffixDn() );

            for ( LdifEntry ldifEntry : configurationWriter.getConvertedLdifEntries() )
            {
                modifiedPartition.addEntry( new DefaultEntry( schemaManager, ldifEntry.getEntry() ) );
            }

            // Comparing both partitions to get the list of modifications to be applied
            List<LdifEntry> modificationsList = PartitionsDiffComputer.computeModifications( originalPartition,
                modifiedPartition, new String[] { SchemaConstants.ALL_USER_ATTRIBUTES } );

            // Building the resulting LDIF
            StringBuilder modificationsLdif = new StringBuilder();

            for ( LdifEntry ldifEntry : modificationsList )
            {
                modificationsLdif.append( ldifEntry.toString() );
            }

            // Creating a StudioProgressMonitor to run the LDIF with
            StudioProgressMonitor studioProgressMonitor = new StudioProgressMonitor( new NullProgressMonitor() );

            // Updating the configuration with the resulting LDIF
            ExecuteLdifRunnable.executeLdif( browserConnection, modificationsLdif.toString(), true, true,
                studioProgressMonitor );

            // Checking if there were errors during the execution of the LDIF
            if ( studioProgressMonitor.errorsReported() )
            {
                StringBuilder message = new StringBuilder();
                message.append( "Changes could not be saved to the connection." );

                Exception exception = studioProgressMonitor.getException();
                if ( exception != null )
                {
                    message.append( "\n\n" );
                    message.append( "Cause: " );
                    message.append( exception.getMessage() );

                    throw new Exception( message.toString(), exception );
                }
                else
                {
                    throw new Exception( message.toString() );
                }
            }
            else
            {
                // Swapping the new configuration partition
                input.setOriginalPartition( modifiedPartition );
            }
        }
        finally
        {
            // Resumes event firing in current thread.
            ConnectionEventRegistry.resumeEventFiringInCurrentThread();
        }
    }


    // ── Yoda Builds A New Landing Platform In The Swamp ──────────────────────
    // Before setting the ship down in a new spot, Yoda prepares the ground —
    // clearing an empty, initialized platform for the X-wing to rest on.
    // We create and initialize a fresh {@link EntryBasedConfigurationPartition}
    // so the modified config has a clean container to live in before we diff it.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates and initializes a new {@link EntryBasedConfigurationPartition}
     * ready to accept configuration entries.
     * We call {@code initialize()} before returning so the partition's internal
     * data structures are set up — callers can immediately start adding entries.
     *
     * @param schemaManager  the schema manager to validate entries against
     * @param configBaseDn   the DN that serves as the root of this partition
     * @return a freshly initialized partition with no entries
     * @throws LdapException  if the partition can't be initialized
     */
    public static EntryBasedConfigurationPartition createConfigurationPartition( SchemaManager schemaManager,
        Dn configBaseDn ) throws LdapException
    {
        EntryBasedConfigurationPartition configurationPartition = new EntryBasedConfigurationPartition(
            schemaManager, configBaseDn );
        configurationPartition.initialize();

        return configurationPartition;
    }


    // ── Yoda Identifies The Object By Its Signature In The Force ──────────────
    // Yoda can sense exactly what kind of object something is just by its
    // presence in the Force — fighter, freighter, life form, or machine.
    // We look up an object class in the schema registry by name, returning
    // the full {@link ObjectClass} descriptor if found.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ObjectClass} registered under the given name in the
     * schema manager, or {@code null} if not found.
     * We go through the OID registry to normalize the name before looking up
     * the actual class definition — this handles aliases and alternate names
     * correctly.
     *
     * @param schemaManager  the schema manager to query — may be null, in which
     *                       case we return null immediately
     * @param name           the object class name to look up — may be null
     * @return the {@link ObjectClass} descriptor, or {@code null} if not found
     */
    public static ObjectClass getObjectClass( SchemaManager schemaManager, String name )
    {
        // Checking the schema manager and name
        if ( ( schemaManager != null ) && ( name != null ) )
        {
            try
            {
                // Getting the object class registry
                ObjectClassRegistry ocRegistry = schemaManager.getObjectClassRegistry();

                if ( ocRegistry != null )
                {
                    // Getting the oid from the object class name
                    String oid = ocRegistry.getOidByName( name );

                    if ( oid != null )
                    {
                        // Getting the object class from the oid
                        return ocRegistry.get( oid );
                    }
                }
            }
            catch ( LdapException e )
            {
                // No OID found for the given name
                return null;
            }
        }

        return null;
    }
}
