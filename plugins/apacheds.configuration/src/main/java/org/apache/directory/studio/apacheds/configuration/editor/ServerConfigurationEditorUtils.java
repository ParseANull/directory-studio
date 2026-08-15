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
package org.apache.directory.studio.apacheds.configuration.editor;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.UUID;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.csn.CsnFactory;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.util.FileUtils;
import org.apache.directory.api.util.Strings;
import org.apache.directory.server.config.ConfigWriter;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.partition.impl.btree.AbstractBTreePartition;
import org.apache.directory.server.core.partition.ldif.AbstractLdifPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.partition.ldif.SingleFileLdifPartition;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.apache.directory.studio.apacheds.configuration.jobs.EntryBasedConfigurationPartition;
import org.apache.directory.studio.apacheds.configuration.jobs.PartitionsDiffComputer;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.filesystem.PathEditorInput;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.jobs.ExecuteLdifRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;


// ── CLASS: ServerConfigurationEditorUtils — THE ENGINEERING TOOLKIT ──────────
// The Death Star engineering team keeps a set of specialised tools in a locked
// cabinet — static helpers used by any officer who needs to read or write the
// station's configuration without knowing the underlying LDIF plumbing.
// This class is that toolkit: all-static, no instances needed, just call the
// method you want and let it handle the messy partition and thread details.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility class for loading, saving, and writing ApacheDS server
 * configurations from within the ServerConfigurationEditor.
 * Think of this class as the Death Star engineering toolkit — a locked cabinet
 * of static helpers that any part of the editor can grab when it needs to read
 * or write config without worrying about the LDIF details underneath.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerConfigurationEditorUtils
{
    // ── Evacuating The Plans To A New Ship ────────────────────────────────────
    // When the Tantive IV is boarded, Leia quickly copies the Death Star plans
    // to a fresh data module and hands it off — old location, new destination.
    // We detect whether we're running inside an IDE or a standalone RCP shell
    // and show the appropriate "Save As" dialog, write the config to the chosen
    // path, and hand back a new editor input pointing to that location.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Implements the "Save As..." action for the config editor.
     * Detects whether we're in a full Eclipse IDE (workspace file picker) or a
     * standalone RCP app (plain OS file dialog), shows the right dialog, writes
     * the config to the chosen file, and returns a new editor input for it.
     *
     * @param monitor       progress monitor for the write operation
     * @param shell         the active shell (for dialog parenting)
     * @param input         the current editor input, used to pre-populate the dialog
     * @param configWriter  the writer that converts the in-memory bean to LDIF
     * @param configuration the full configuration object
     * @param newInput      if {@code true}, wrap the saved path in a new editor input
     * @return the new editor input, or {@code null} if the user cancelled
     * @throws Exception    if writing the config file fails
     */
    public static IEditorInput saveAs( IProgressMonitor monitor, Shell shell, IEditorInput input,
        ConfigWriter configWriter, Configuration configuration, boolean newInput )
        throws Exception
    {
        // detect IDE or RCP:
        // check if perspective org.eclipse.ui.resourcePerspective is available
        boolean isIDE = CommonUIUtils.isIDEEnvironment();

        if ( isIDE )
        {
            // Asking the user for the location where to 'save as' the file
            SaveAsDialog dialog = new SaveAsDialog( shell );

            String inputClassName = input.getClass().getName();

            if ( input instanceof FileEditorInput )
            {
                // FileEditorInput class is used when the file is opened
                // from a project in the workspace.
                dialog.setOriginalFile( ( ( FileEditorInput ) input ).getFile() );
            }
            else if ( input instanceof IPathEditorInput )
            {
                dialog.setOriginalFile( ResourcesPlugin.getWorkspace().getRoot()
                    .getFile( ( ( IPathEditorInput ) input ).getPath() ) );
            }
            else if ( inputClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" ) //$NON-NLS-1$
                || inputClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) ) //$NON-NLS-1$
            {
                // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
                // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
                // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
                // opening a file from the menu File > Open... in Eclipse 3.3.x
                dialog.setOriginalFile( ResourcesPlugin.getWorkspace().getRoot()
                    .getFile( new Path( input.getToolTipText() ) ) );
            }
            else
            {
                dialog.setOriginalName( ApacheDS2ConfigurationPluginConstants.CONFIG_LDIF );
            }

            // Open the dialog
            if ( openDialogInUIThread( dialog ) != Dialog.OK )
            {
                return null;
            }

            // Getting if the resulting file
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile( dialog.getResult() );

            // Creating the file if it does not exist
            if ( !file.exists() )
            {
                file.create( new ByteArrayInputStream( "".getBytes() ), true, null ); //$NON-NLS-1$
            }

            // Creating the new input for the editor
            FileEditorInput fei = new FileEditorInput( file );

            // Saving the file to disk
            File configFile = fei.getPath().toFile();
            saveConfiguration( configFile, configWriter, configuration );

            return fei;
        }
        else
        {
            boolean canOverwrite = false;
            String path = null;

            while ( !canOverwrite )
            {
                // Open FileDialog
                path = openFileDialogInUIThread( shell );

                if ( path == null )
                {
                    return null;
                }

                // Check whether file exists and if so, confirm overwrite
                final File externalFile = new File( path );

                if ( externalFile.exists() )
                {
                    String question = NLS.bind(
                        Messages.getString( "ServerConfigurationEditorUtils.TheFileAlreadyExistsWantToReplace" ), path ); //$NON-NLS-1$
                    MessageDialog overwriteDialog = new MessageDialog( shell,
                        Messages.getString( "ServerConfigurationEditorUtils.Question" ), null, question, //$NON-NLS-1$
                        MessageDialog.QUESTION, new String[]
                            { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 0 );
                    int overwrite = openDialogInUIThread( overwriteDialog );

                    switch ( overwrite )
                    {
                        case 0: // Yes
                            canOverwrite = true;
                            break;

                        case 1: // No
                            break;

                        case 2: // Cancel
                        default:
                            return null;
                    }
                }
                else
                {
                    canOverwrite = true;
                }
            }

            // Saving the file to disk
            saveConfiguration( new File(path), configWriter, configuration );

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
    }


    // ── Speaking Through The Protocol Droid ───────────────────────────────────
    // C-3PO relays a question from the Rebels to the Imperial commander on their
    // behalf — the actual conversation happens on the commander's bridge, not in
    // the Rebel hangar where the request originated.
    // We need the dialog to open on the SWT UI thread; this method marshals the
    // call via Display.syncExec and ferries the integer result back to us.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a JFace {@link Dialog} on the SWT UI thread and blocks until the
     * user closes it.
     * Background threads can't touch SWT widgets directly; this method handles
     * the thread hop so callers don't have to think about it.
     *
     * @param dialog  the dialog to open
     * @return the dialog's return code (e.g., {@link Dialog#OK})
     */
    private static int openDialogInUIThread( final Dialog dialog )
    {
        // Defining our own encapsulating class for the result
        class DialogResult
        {
            private int result;

            public int getResult()
            {
                return result;
            }


            public void setResult( int result )
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


    // ── Dispatching The Navigator To Chart The Course ─────────────────────────
    // The Millennium Falcon's navigator pops up a star chart on the UI console —
    // Han asks for a destination and waits for the co-ordinates to come back.
    // We open a plain SWT FileDialog on the UI thread and return the chosen
    // path string back to the calling thread via the same syncExec trick.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a plain SWT {@link FileDialog} on the UI thread and returns
     * the path the user selected, or {@code null} if they cancelled.
     *
     * @param shell  the parent shell for the dialog
     * @return the absolute file path chosen, or {@code null} on cancel
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


    // ── Uploading New Orders To The Fleet ─────────────────────────────────────
    // The Imperial communications officer compares the new battle orders against
    // the old ones, computes the delta, then transmits only the changes to each
    // Star Destroyer rather than re-sending the entire tactical database.
    // We diff the new config partition against the original, build a minimal
    // LDIF of modifications, and apply them to the live LDAP connection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves a configuration change to a live LDAP connection.
     * Rather than rewriting the entire tree, we diff the new config partition
     * against the original and apply only the delta as an LDIF modification
     * to the running server — minimising disruption to a live directory.
     *
     * @param input         the connection-backed editor input holding the original partition
     * @param configWriter  writer that converts the bean to LDIF entries
     * @param monitor       progress monitor for the LDAP operations
     * @throws Exception    if the LDAP operations fail or report errors
     */
    public static void saveConfiguration( ConnectionServerConfigurationInput input, ConfigWriter configWriter,
        IProgressMonitor monitor ) throws Exception
    {
        // Getting the original configuration partition
        EntryBasedConfigurationPartition originalPartition = input.getOriginalPartition();

        // Creating a new configuration partition
        SchemaManager schemaManager = ApacheDS2ConfigurationPlugin.getDefault().getSchemaManager();
        EntryBasedConfigurationPartition newconfigurationPartition = new EntryBasedConfigurationPartition(
            schemaManager );
        newconfigurationPartition.initialize();
        List<LdifEntry> convertedLdifEntries = configWriter.getConvertedLdifEntries();

        for ( LdifEntry ldifEntry : convertedLdifEntries )
        {
            newconfigurationPartition.addEntry( new DefaultEntry( schemaManager, ldifEntry.getEntry() ) );
        }

        // Suspends event firing in current thread.
        ConnectionEventRegistry.suspendEventFiringInCurrentThread();

        try
        {

            // Comparing both partitions to get the list of modifications to be applied
            PartitionsDiffComputer partitionsDiffComputer = new PartitionsDiffComputer();
            partitionsDiffComputer.setOriginalPartition( originalPartition );
            partitionsDiffComputer.setDestinationPartition( newconfigurationPartition );
            List<LdifEntry> modificationsList = partitionsDiffComputer.computeModifications( new String[]
                { SchemaConstants.ALL_USER_ATTRIBUTES } );

            // Building the resulting LDIF
            StringBuilder modificationsLdif = new StringBuilder();

            for ( LdifEntry ldifEntry : modificationsList )
            {
                modificationsLdif.append( ldifEntry.toString() );
                modificationsLdif.append( '\n' );
            }

            // Getting the browser connection associated with the connection
            IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnection( input.getConnection() );

            // Creating a StudioProgressMonitor to run the LDIF with
            StudioProgressMonitor studioProgressMonitor = new StudioProgressMonitor( new NullProgressMonitor() );

            // Updating the configuration with the resulting LDIF
            ExecuteLdifRunnable.executeLdif( browserConnection, modificationsLdif.toString(), true, true,
                studioProgressMonitor );

            // Checking if there were errors during the execution of the LDIF
            if ( studioProgressMonitor.errorsReported() )
            {
                throw new Exception(
                    Messages.getString( "ServerConfigurationEditorUtils.ChangesCouldNotBeSavedToConnection" ) ); //$NON-NLS-1$
            }
            else
            {
                // Swapping the new configuration partition
                input.setOriginalPartition( newconfigurationPartition );
            }
        }
        finally
        {
            // Resumes event firing in current thread.
            ConnectionEventRegistry.resumeEventFiringInCurrentThread();
        }
    }


    // ── Writing The New Schematics To Disk ────────────────────────────────────
    // The Death Star engineers commit the updated reactor schematics to a
    // physical blueprint — one scroll if it's a small change, a whole filing
    // cabinet if the full layout demands it.
    // We pick between a single-file LDIF partition and a multi-file directory
    // partition based on the filename, then write every config entry in.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves the configuration to a local file (or directory structure).
     * If the file is named {@code ou=config.ldif} we write a multi-file
     * directory layout; otherwise we write a single-file LDIF.
     * Each entry gets a CSN and UUID stamped in if they're missing.
     *
     * @param file          the target file to write to
     * @param configWriter  writer that provides the LDIF entries
     * @param configuration the configuration object (used for schema manager access)
     * @throws Exception    if partition initialisation or entry writing fails
     */
    public static void saveConfiguration( File file, ConfigWriter configWriter, Configuration configuration )
        throws Exception
    {
        SchemaManager schemaManager = ApacheDS2ConfigurationPlugin.getDefault().getSchemaManager();

        DnFactory dnFactory = null;

        CsnFactory csnFactory = new CsnFactory( 0 );

        if ( file != null )
        {
            // create partiton
            AbstractLdifPartition configPartition;

            if ( file.getName().equals( ApacheDS2ConfigurationPluginConstants.OU_CONFIG_LDIF ) )
            {
                File confDir = file.getParentFile();
                File ouConfigLdifFile = new File( confDir, ApacheDS2ConfigurationPluginConstants.OU_CONFIG_LDIF );
                File ouConfigDir = new File( confDir, ApacheDS2ConfigurationPluginConstants.OU_CONFIG );

                if ( ouConfigLdifFile.exists() && ouConfigDir.exists() )
                {
                    ouConfigLdifFile.delete();
                    FileUtils.deleteDirectory( ouConfigDir );
                }

                configPartition = createMultiFileConfiguration( confDir, schemaManager, dnFactory );
            }
            else
            {
                if ( file.exists() )
                {
                    file.delete();
                }

                configPartition = createSingleFileConfiguration( file, schemaManager, dnFactory );
            }

            // write entries to partition
            List<LdifEntry> convertedLdifEntries = configWriter.getConvertedLdifEntries();

            for ( LdifEntry ldifEntry : convertedLdifEntries )
            {
                Entry entry = new DefaultEntry( schemaManager, ldifEntry.getEntry() );

                if ( entry.get( SchemaConstants.ENTRY_CSN_AT ) == null )
                {
                    entry.add( SchemaConstants.ENTRY_CSN_AT, csnFactory.newInstance().toString() );
                }

                if ( entry.get( SchemaConstants.ENTRY_UUID_AT ) == null )
                {
                    String uuid = UUID.randomUUID().toString();
                    entry.add( SchemaConstants.ENTRY_UUID_AT, uuid );
                }

                configPartition.add( new AddOperationContext( null, entry ) );
            }
        }
    }


    // ── Sealing The Plans In One Envelope ─────────────────────────────────────
    // A single sealed envelope contains the complete Death Star schematics —
    // everything in one place, easy to hand off, easy to open back up.
    // We set up a SingleFileLdifPartition pointed at the chosen file so the
    // save logic can push entries into it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and initialises a {@link SingleFileLdifPartition} backed by the
     * given file.
     * The partition is the storage layer that actually serialises the LDIF
     * entries to disk in one self-contained file.
     *
     * @param configFile    the target file for the single-file LDIF partition
     * @param schemaManager the schema manager used by the partition
     * @param dnFactory     DN factory (may be {@code null})
     * @return an initialised partition ready for writing
     * @throws Exception    if partition setup fails
     */
    private static SingleFileLdifPartition createSingleFileConfiguration( File configFile, SchemaManager schemaManager,
        DnFactory dnFactory) throws Exception
    {
        SingleFileLdifPartition configPartition = new SingleFileLdifPartition( schemaManager, dnFactory );
        configPartition.setId( "config" );
        configPartition.setPartitionPath( configFile.toURI() );
        configPartition.setSuffixDn( new Dn( schemaManager, "ou=config" ) );
        configPartition.setSchemaManager( schemaManager );
        configPartition.initialize();

        return configPartition;
    }


    // ── Spreading The Plans Across The Whole Filing Cabinet ───────────────────
    // The Death Star's full engineering library fills an entire room of filing
    // cabinets — each subsystem gets its own drawer, kept separate for safety.
    // We set up a multi-file LdifPartition over a directory so each config
    // subtree lives in its own file within that folder.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and initialises a multi-file {@link LdifPartition} backed by
     * a directory.
     * Each config subtree gets its own LDIF file inside the directory, matching
     * the on-disk layout ApacheDS uses when running natively.
     *
     * @param confDir       the directory that will contain the LDIF files
     * @param schemaManager the schema manager used by the partition
     * @param dnFactory     DN factory (may be {@code null})
     * @return an initialised partition ready for writing
     * @throws Exception    if partition setup fails
     */
    private static LdifPartition createMultiFileConfiguration( File confDir, SchemaManager schemaManager,
        DnFactory dnFactory ) throws Exception
    {
        LdifPartition configPartition = new LdifPartition( schemaManager, dnFactory );
        configPartition.setId( "config" );
        configPartition.setPartitionPath( confDir.toURI() );
        configPartition.setSuffixDn( new Dn( schemaManager, "ou=config" ) );
        configPartition.setSchemaManager( schemaManager );
        configPartition.initialize();

        return configPartition;
    }


    // ── Running The Targeting Computer's Diff ─────────────────────────────────
    // The targeting computer compares the new attack vector against the
    // previously locked coordinates and outputs only the adjustment needed —
    // no point resending the whole firing solution if only the angle changed.
    // We build a temp partition from the config writer's entries, diff it
    // against the original partition, and return the minimal modification list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Computes the LDIF modifications needed to turn the original partition
     * into the new configuration.
     * Used as a building block for live-connection saves — we diff rather than
     * replace to avoid touching entries that haven't changed.
     *
     * @param configWriter       writer providing the target config entries
     * @param originalPartition  the current state stored in the directory
     * @return a list of LDIF modification entries to apply
     * @throws Exception  if diffing the partitions fails
     */
    // TODO: something link this should be used in future to only write changes to partition
    private static List<LdifEntry> computeModifications( ConfigWriter configWriter,
        AbstractBTreePartition originalPartition ) throws Exception
    {
        // Creating a new configuration partition
        SchemaManager schemaManager = ApacheDS2ConfigurationPlugin.getDefault().getSchemaManager();
        EntryBasedConfigurationPartition newconfigurationPartition = new EntryBasedConfigurationPartition(
            schemaManager );
        newconfigurationPartition.initialize();
        List<LdifEntry> convertedLdifEntries = configWriter.getConvertedLdifEntries();

        for ( LdifEntry ldifEntry : convertedLdifEntries )
        {
            newconfigurationPartition.addEntry( new DefaultEntry( schemaManager, ldifEntry.getEntry() ) );
        }

        // Comparing both partitions to get the list of modifications to be applied
        PartitionsDiffComputer partitionsDiffComputer = new PartitionsDiffComputer();
        partitionsDiffComputer.setOriginalPartition( originalPartition );
        partitionsDiffComputer.setDestinationPartition( newconfigurationPartition );
        return partitionsDiffComputer.computeModifications( new String[]
            { SchemaConstants.ALL_USER_ATTRIBUTES } );
    }


    // ── Ejecting The Empty Escape Pod ─────────────────────────────────────────
    // An escape pod with no one inside is worse than useless — we seal the
    // hatch and return an empty pod rather than trying to track a null capsule
    // across the galaxy.
    // If the caller hands us null, we return an empty string so downstream
    // code doesn't have to null-check everywhere.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code ""} if the input string is {@code null}, otherwise
     * returns the string unchanged.
     * A convenient null-to-empty guard for config values that must always
     * be non-null strings.
     *
     * @param s  the string to check
     * @return the original string, or {@code ""} if it was {@code null}
     */
    public static String checkNull( String s )
    {
        if ( s == null )
        {
            return "";
        }

        return s;
    }


    // ── Treating An Empty Signal As No Signal ─────────────────────────────────
    // The communications officer knows that a carrier wave with no message is
    // the same as silence — there's nothing useful in it, so we treat it as null.
    // If the caller gives us a blank or whitespace-only string, we return null
    // so the config layer stores no value rather than an empty placeholder.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} if the input string is empty or whitespace-only,
     * otherwise returns the string unchanged.
     * Use this when an empty config field should mean "not set" rather than
     * an explicit empty string stored in the config bean.
     *
     * @param s  the string to check
     * @return the original string, or {@code null} if it was blank
     */
    public static String checkEmptyString( String s )
    {
        if ( Strings.isEmpty( s ) )
        {
            return null;
        }

        return s;
    }
}
