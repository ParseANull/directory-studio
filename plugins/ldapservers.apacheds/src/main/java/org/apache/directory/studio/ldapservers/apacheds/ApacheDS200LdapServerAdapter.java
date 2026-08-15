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

package org.apache.directory.studio.ldapservers.apacheds;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.server.config.beans.ChangePasswordServerBean;
import org.apache.directory.server.config.beans.ConfigBean;
import org.apache.directory.server.config.beans.DirectoryServiceBean;
import org.apache.directory.server.config.beans.DnsServerBean;
import org.apache.directory.server.config.beans.KdcServerBean;
import org.apache.directory.server.config.beans.LdapServerBean;
import org.apache.directory.server.config.beans.NtpServerBean;
import org.apache.directory.server.config.beans.TransportBean;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.apache.directory.studio.apacheds.configuration.editor.Configuration;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditor;
import org.apache.directory.studio.apacheds.configuration.jobs.LoadConfigurationRunnable;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.ui.filesystem.PathEditorInput;
import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.LdapServersUtils;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapter;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;
import org.apache.mina.util.AvailablePortFinder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;


// ── CLASS: ApacheDS200LdapServerAdapter — Han Solo Flying the Millennium Falcon ─
// Han Solo is the guy who actually flies the ship.  When the Alliance tells him
// to launch (start), he copies the engine libraries from the plugin bundle into
// the ship's lib folder (verifyAndCopyLibraries), sets up the partition and log
// folders (add), fires up the ApacheDS JVM with the right arguments
// (launchApacheDS), and starts the console tail thread so logs flow to the
// Eclipse console.  When they say "land" (stop) he gracefully signals ApacheDS
// to shut down and waits up to 3 minutes for it to finish.  When Chewie needs
// to fix the hyperdrive (repair), he launches ApacheDS in repair mode and again
// waits for it.  Checking ports before launch is the pre-flight checklist:
// if any configured protocol port is already in use, we tell the operator now
// rather than letting ApacheDS crash at startup.
// ApacheDS200LdapServerAdapter implements the LdapServerAdapter interface for
// ApacheDS version 2.0.0, bridging the Studio server-management UI to the
// ApacheDS JVM process lifecycle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An {@link LdapServerAdapter} for Apache DS version 2.0.0.
 * Handles the full lifecycle of an embedded ApacheDS 2.0.0 instance:
 * provisioning the server folder (add), opening the config editor
 * (openConfiguration), launching the JVM (start), stopping it gracefully
 * (stop), running partition repair (repair), and validating that required
 * ports are free before launch (checkPortsBeforeServerStart).
 * Also exposes static helpers so other classes (e.g. {@link CreateConnectionAction})
 * can read the server's parsed configuration and query individual protocol
 * enable/port settings.
 * Think of Han Solo flying the Millennium Falcon — this class is the engineer
 * who knows every switch in the cockpit.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApacheDS200LdapServerAdapter implements LdapServerAdapter
{
    // Various strings constants used in paths
    private static final String LOG4J_PROPERTIES = "log4j.properties"; //$NON-NLS-1$
    private static final String RESOURCES = "resources"; //$NON-NLS-1$
    private static final String LIBS = "libs"; //$NON-NLS-1$
    private static final String CONF = "conf"; //$NON-NLS-1$

    /** The array of libraries names */
    private static final String[] libraries = new String[]
        { "apacheds-service.jar" }; //$NON-NLS-1$

    private enum Action {
        START, REPAIR, STOP;
    }


    // ── Pre-Flight: Set Up the Server Folder Structure ────────────────────────
    // Before the Falcon can fly we need the right directory layout under the
    // server folder: conf/, ldif/, log/, and partitions/.  We also copy the
    // service JAR and log4j config from the plugin bundle.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Provisions a new ApacheDS 2.0.0 server instance.
     * Creates the required folder structure (conf, ldif, log, partitions),
     * verifies and copies the ApacheDS service JAR, copies the default
     * {@code config.ldif} and {@code log4j.properties} from the plugin bundle,
     * and creates an empty {@code apacheds.log} file.
     *
     * @param server   the server being added.
     * @param monitor  the progress monitor for this operation.
     * @throws Exception  if folder creation or file copy fails.
     * @see LdapServerAdapter#add(LdapServer, StudioProgressMonitor)
     */
    public void add( LdapServer server, StudioProgressMonitor monitor ) throws Exception
    {
        // Getting the bundle associated with the plugin
        Bundle bundle = ApacheDS200Plugin.getDefault().getBundle();

        // Verifying and copying ApacheDS 2.0.0 libraries
        LdapServersUtils.verifyAndCopyLibraries( bundle, new Path( RESOURCES ).append( LIBS ),
            getServerLibrariesFolder(), libraries, monitor,
            Messages.getString( "ApacheDS200LdapServerAdapter.VerifyingAndCopyingLibraries" ) ); //$NON-NLS-1$

        // Creating server folder structure
        monitor.subTask( Messages.getString( "ApacheDS200LdapServerAdapter.CreatingServerFolderStructure" ) ); //$NON-NLS-1$
        File serverFolder = LdapServersManager.getServerFolder( server ).toFile();
        File confFolder = new File( serverFolder, CONF );
        confFolder.mkdir();
        File ldifFolder = new File( serverFolder, "ldif" ); //$NON-NLS-1$
        ldifFolder.mkdir();
        File logFolder = new File( serverFolder, "log" ); //$NON-NLS-1$
        logFolder.mkdir();
        File partitionFolder = new File( serverFolder, "partitions" ); //$NON-NLS-1$
        partitionFolder.mkdir();

        // Copying configuration files
        monitor.subTask( Messages.getString( "ApacheDS200LdapServerAdapter.CopyingConfigurationFiles" ) ); //$NON-NLS-1$
        IPath resourceConfFolderPath = new Path( RESOURCES ).append( CONF );
        // call of getServerConfiguration() extracts the default configuration
        getServerConfiguration( server );
        LdapServersUtils.copyResource( bundle, resourceConfFolderPath.append( LOG4J_PROPERTIES ), new File( confFolder,
            LOG4J_PROPERTIES ) );

        // Creating an empty log file
        new File( logFolder, "apacheds.log" ).createNewFile(); //$NON-NLS-1$
    }


    // ── Delete: Nothing Extra to Do ───────────────────────────────────────────
    // The LdapServersManager handles deletion of the server folder itself;
    // we don't need any additional cleanup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deletes the server — no extra cleanup needed beyond the default behavior.
     *
     * @param server   the server being deleted.
     * @param monitor  the progress monitor.
     * @throws Exception  (never thrown by this implementation).
     * @see LdapServerAdapter#delete(LdapServer, StudioProgressMonitor)
     */
    public void delete( LdapServer server, StudioProgressMonitor monitor ) throws Exception
    {
        // Nothing to do (nothing more than the default behavior of
        // the delete action before this method is called)
    }


    // ── Open the Cockpit Configuration Panel ──────────────────────────────────
    // We open the ServerConfigurationEditor in the Eclipse workbench so the
    // operator can edit config.ldif visually.  Must run on the UI thread.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the server's {@code config.ldif} in the
     * {@link ServerConfigurationEditor} on the UI thread.
     *
     * @param server   the server whose configuration to open.
     * @param monitor  the progress monitor (used to report editor init errors).
     * @throws Exception  if opening the editor fails.
     * @see LdapServerAdapter#openConfiguration(LdapServer, StudioProgressMonitor)
     */
    public void openConfiguration( final LdapServer server, final StudioProgressMonitor monitor ) throws Exception
    {
        // Opening the editor
        Display.getDefault().syncExec( new Runnable()
        {
            public void run()
            {
                try
                {
                    PathEditorInput input = new PathEditorInput( LdapServersManager.getServerFolder( server )
                        .append( CONF ).append( ApacheDS2ConfigurationPluginConstants.OU_CONFIG_LDIF ) );
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .openEditor( input, ServerConfigurationEditor.ID );
                }
                catch ( PartInitException e )
                {
                    monitor.reportError( e );
                }
            }
        } );
    }


    // ── Start the Engines — Launch ApacheDS ───────────────────────────────────
    // We call startOrRepair() with Action.START, store the resulting ILaunch in
    // the server's custom object map so stop() can find it, then run the startup
    // watchdog that polls the LDAP port until the server responds.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Starts the ApacheDS 2.0.0 server.
     * Verifies/copies libraries, starts the console log tail thread, launches
     * the ApacheDS JVM, stores the {@link ILaunch} in the server, and runs
     * the startup watchdog to wait for the server to become responsive.
     *
     * @param server   the server to start.
     * @param monitor  the progress monitor.
     * @throws Exception  if launch fails.
     * @see LdapServerAdapter#start(LdapServer, StudioProgressMonitor)
     */
    public void start( LdapServer server, StudioProgressMonitor monitor ) throws Exception
    {
        ILaunch launch = startOrRepair( server, monitor, Action.START );

        // Storing the launch configuration as a custom object in the LDAP Server for later use
        server.putCustomObject( LdapServersUtils.LAUNCH_CONFIGURATION_CUSTOM_OBJECT, launch );

        // Running the startup listener watchdog
        LdapServersUtils.runStartupListenerWatchdog( server, getTestingPort( server ) );
    }


    // ── Chewie Fixes the Hyperdrive — Repair Mode ────────────────────────────
    // We launch ApacheDS with the "repair" argument, then poll every second
    // for up to 3 minutes waiting for the repair process to finish.  When done
    // we stop the console tail thread.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Starts ApacheDS in repair mode to rebuild corrupted partition index files.
     * Waits (polling every second) for up to 3 minutes for the repair process
     * to exit, then stops the console log tail thread.
     *
     * @param server   the server whose partitions need repairing.
     * @param monitor  the progress monitor.
     * @throws Exception  if launch fails.
     */
    public void repair( LdapServer server, StudioProgressMonitor monitor ) throws Exception
    {
        // repair
        startOrRepair( server, monitor, Action.REPAIR );

        // Await termination of the repair action
        long startTime = System.currentTimeMillis();
        final long watchDog = startTime + ( 1000 * 60 * 3 ); // 3 minutes
        do
        {
            Thread.sleep( 1000 );
        }
        while ( ( System.currentTimeMillis() < watchDog ) && ( LdapServerStatus.REPAIRING == server.getStatus() ) );

        // stop the console printer thread
        LdapServersUtils.stopConsolePrinterThread( server );
    }


    // ── Shared Launch Logic for Start and Repair ──────────────────────────────
    // Both start and repair share the same steps up to launchApacheDS(); the
    // only difference is the Action enum value we pass, which changes the
    // program argument ("repair" or nothing).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Shared implementation for {@link #start} and {@link #repair}.
     * Verifies/copies the service JAR, starts the console log tail thread,
     * and launches the ApacheDS JVM with the given action.
     *
     * @param server   the server.
     * @param monitor  the progress monitor.
     * @param action   {@link Action#START} or {@link Action#REPAIR}.
     * @return         the resulting {@link ILaunch}.
     * @throws Exception  if launch fails.
     */
    private ILaunch startOrRepair( LdapServer server, StudioProgressMonitor monitor, Action action ) throws Exception
    {
        // Getting the bundle associated with the plugin
        Bundle bundle = ApacheDS200Plugin.getDefault().getBundle();

        // Verifying and copying ApacheDS 2.0.0 libraries
        LdapServersUtils.verifyAndCopyLibraries( bundle, new Path( RESOURCES ).append( LIBS ),
            getServerLibrariesFolder(), libraries, monitor,
            Messages.getString( "ApacheDS200LdapServerAdapter.VerifyingAndCopyingLibraries" ) ); //$NON-NLS-1$

        // Starting the console printer thread
        LdapServersUtils.startConsolePrinterThread( server, LdapServersManager.getServerFolder( server )
            .append( "log" ) //$NON-NLS-1$
            .append( "apacheds.log" ).toFile() );//$NON-NLS-1$

        // Launching ApacheDS
        ILaunch launch = launchApacheDS( server, action );

        return launch;
    }


    // ── Build and Fire the Launch Configuration ────────────────────────────────
    // We assemble a Java application launch configuration: default JVM, the
    // apacheds-service.jar on the classpath, UberjarMain as main class, the
    // server folder path as the first program argument (plus optionally "repair"
    // or "stop"), and all the required -D JVM arguments for ApacheDS paths and
    // instance name.  Then we launch it in RUN mode and start the terminate-
    // listener thread so we know when the process exits.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and launches an Eclipse Java application launch configuration for ApacheDS.
     * Configures the JRE, classpath (service JAR), main class
     * ({@code org.apache.directory.server.UberjarMain}), program arguments
     * (server folder path; optionally "repair" or "stop"), and VM arguments
     * (log4j config, var/log/instance paths).
     * The configuration is marked private (hidden from the user's launch history)
     * and console output capture is disabled (we read the log file directly).
     *
     * @param server  the server to launch.
     * @param action  {@link Action#START}, {@link Action#REPAIR}, or {@link Action#STOP}.
     * @return        the resulting {@link ILaunch}.
     * @throws Exception  if the launch configuration creation or launch fails.
     */
    private static ILaunch launchApacheDS( LdapServer server, Action action )
        throws Exception
    {
        // Getting the default VM installation
        IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();

        // Creating a new editable launch configuration
        ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager()
            .getLaunchConfigurationType( IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION );
        ILaunchConfigurationWorkingCopy workingCopy = type.newInstance( null, server.getId() );

        // Setting the JRE container path attribute
        workingCopy.setAttribute( IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, vmInstall
            .getInstallLocation().toString() );

        // Setting the main type attribute
        workingCopy.setAttribute( IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
            "org.apache.directory.server.UberjarMain" ); //$NON-NLS-1$

        // Creating the classpath list
        List<String> classpath = new ArrayList<String>();
        for ( String library : libraries )
        {
            IRuntimeClasspathEntry libraryClasspathEntry = JavaRuntime
                .newArchiveRuntimeClasspathEntry( getServerLibrariesFolder().append( library ) );
            libraryClasspathEntry.setClasspathProperty( IRuntimeClasspathEntry.USER_CLASSES );

            classpath.add( libraryClasspathEntry.getMemento() );
        }

        // Setting the classpath type attribute
        workingCopy.setAttribute( IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath );

        // Setting the default classpath type attribute to false
        workingCopy.setAttribute( IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false );

        // The server folder path
        IPath serverFolderPath = LdapServersManager.getServerFolder( server );

        // Creating the program arguments string
        StringBuffer programArguments = new StringBuffer();
        programArguments.append( "\"" + serverFolderPath.toOSString() + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
        if ( action == Action.REPAIR )
        {
            programArguments.append( " " );
            programArguments.append( "\"repair\"" );
        }
        else if ( action == Action.STOP )
        {
            programArguments.append( " " );
            programArguments.append( "\"stop\"" );
        }

        // Setting the program arguments attribute
        workingCopy.setAttribute( IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
            programArguments.toString() );

        // Creating the VM arguments string
        StringBuffer vmArguments = new StringBuffer();
        vmArguments.append( "-Dlog4j.configuration=file:\"" //$NON-NLS-1$
            + serverFolderPath.append( CONF ).append( LOG4J_PROPERTIES ).toOSString() + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        vmArguments.append( " " ); //$NON-NLS-1$
        vmArguments.append( "-Dapacheds.var.dir=\"" + serverFolderPath.toOSString() + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
        vmArguments.append( " " ); //$NON-NLS-1$
        vmArguments.append( "-Dapacheds.log.dir=\"" + serverFolderPath.append( "log" ).toOSString() + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        vmArguments.append( " " ); //$NON-NLS-1$
        vmArguments.append( "-Dapacheds.instance=\"" + server.getName() + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
        vmArguments.append( " " ); //$NON-NLS-1$
        vmArguments.append( "-Dapacheds.controls=" ); //$NON-NLS-1$
        vmArguments.append( " " ); //$NON-NLS-1$
        vmArguments.append( "-Dapacheds.extendedOperations=" ); // $NON-NLS-1$

        // Setting the VM arguments attribute
        workingCopy.setAttribute( IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArguments.toString() );

        // Setting the launch configuration as private
        workingCopy.setAttribute( IDebugUIConstants.ATTR_PRIVATE, true );

        // Indicating that we don't want any console to show up
        workingCopy.setAttribute( DebugPlugin.ATTR_CAPTURE_OUTPUT, false );

        // Saving the launch configuration
        ILaunchConfiguration configuration = workingCopy.doSave();

        // Launching the launch configuration
        ILaunch launch = configuration.launch( ILaunchManager.RUN_MODE, new NullProgressMonitor() );

        // Starting the "terminate" listener thread
        LdapServersUtils.startTerminateListenerThread( server, launch );

        return launch;
    }


    // ── Land the Falcon — Graceful Shutdown ───────────────────────────────────
    // We launch a separate "stop" JVM process that signals the running ApacheDS
    // to shut down, then poll every second for up to 3 minutes waiting for the
    // STOPPING status to clear, then clean up the console thread and launch.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stops the ApacheDS 2.0.0 server gracefully.
     * Launches a separate JVM process with the {@code "stop"} argument that
     * signals the running server to shut down.  Polls every second for up to
     * 3 minutes for the server status to leave {@code STOPPING}.  Finally
     * stops the console log tail thread and terminates the original launch.
     *
     * @param server   the server to stop.
     * @param monitor  the progress monitor.
     * @throws Exception  if the stop launch fails.
     * @see LdapServerAdapter#stop(LdapServer, StudioProgressMonitor)
     */
    public void stop( LdapServer server, StudioProgressMonitor monitor ) throws Exception
    {
        // Graceful stop ApacheDS
        launchApacheDS( server, Action.STOP );

        // Await termination of the server
        long startTime = System.currentTimeMillis();
        final long watchDog = startTime + ( 1000 * 60 * 3 ); // 3 minutes
        do
        {
            Thread.sleep( 1000 );
        }
        while ( ( System.currentTimeMillis() < watchDog ) && ( LdapServerStatus.STOPPING == server.getStatus() ) );

        // Stopping the console printer thread
        LdapServersUtils.stopConsolePrinterThread( server );

        // Terminating the launch configuration
        LdapServersUtils.terminateLaunchConfiguration( server );
    }


    // ── Where We Keep the Engine JAR ──────────────────────────────────────────
    // The service JAR lives in the plugin's OSGi state location under "libs/";
    // this is per-workspace and per-plugin, so different workspaces don't share.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the path to the folder where the ApacheDS service JAR is stored.
     * The folder is inside the plugin's OSGi state location (workspace-specific).
     *
     * @return  the absolute path to the {@code libs/} folder.
     */
    private static IPath getServerLibrariesFolder()
    {
        return ApacheDS200Plugin.getDefault().getStateLocation().append( LIBS );
    }


    // ── Read the Ship's Current Configuration ─────────────────────────────────
    // We find the config.ldif file in the server's conf/ folder and parse it
    // through LoadConfigurationRunnable.readConfiguration().  This is called
    // both during add() (to extract the default config) and by other classes
    // (e.g. CreateConnectionAction) that need to query port numbers.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads and parses the server's {@code config.ldif} file.
     * The file lives at {@code <serverFolder>/conf/ou=config.ldif}.
     *
     * @param server  the server whose configuration to read.
     * @return        the parsed {@link Configuration} object.
     * @throws Exception           if reading or parsing fails.
     * @throws FileNotFoundException  if {@code config.ldif} does not exist.
     */
    public static Configuration getServerConfiguration( LdapServer server ) throws Exception
    {
        File configFile = LdapServersManager.getServerFolder( server ).append( CONF )
            .append( ApacheDS2ConfigurationPluginConstants.OU_CONFIG_LDIF ).toFile();
        return LoadConfigurationRunnable.readConfiguration( configFile );
    }


    // ── Which Port to Poll While Waiting for the Server to Start ─────────────
    // We pick the first enabled protocol's port in priority order:
    // LDAP > LDAPS > Kerberos > DNS > NTP > ChangePassword.
    // The startup watchdog connects to this port once per second until it gets
    // a response, which tells us the server is ready.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the port number the startup watchdog should probe.
     * We return the port of the first enabled protocol in this order:
     * LDAP, LDAPS, Kerberos, DNS, NTP, ChangePassword.
     * Returns {@code 0} if no protocol is enabled.
     *
     * @param server  the server.
     * @return        the port to probe, or {@code 0}.
     * @throws Exception  if reading the configuration fails.
     */
    private int getTestingPort( LdapServer server ) throws Exception
    {
        ConfigBean configuration = getServerConfiguration( server ).getConfigBean();

        // LDAP
        if ( isEnableLdap( configuration ) )
        {
            return getLdapPort( configuration );
        }
        // LDAPS
        else if ( isEnableLdaps( configuration ) )
        {
            return getLdapsPort( configuration );
        }
        // Kerberos
        else if ( isEnableKerberos( configuration ) )
        {
            return getKerberosPort( configuration );
        }
        // DNS
        else if ( isEnableDns( configuration ) )
        {
            return getDnsPort( configuration );
        }
        // NTP
        else if ( isEnableNtp( configuration ) )
        {
            return getNtpPort( configuration );
        }
        // ChangePassword
        else if ( isEnableChangePassword( configuration ) )
        {
            return getChangePasswordPort( configuration );
        }
        else
        {
            return 0;
        }
    }


    // ── Is the Main LDAP Port Open? ───────────────────────────────────────────
    // Looks up the "ldap" transport by ID and checks its enabled flag.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the LDAP transport is enabled in the configuration.
     *
     * @param configuration  the parsed config bean.
     * @return               {@code true} if the LDAP server transport is enabled.
     */
    public static boolean isEnableLdap( ConfigBean configuration )
    {
        TransportBean ldapServerTransportBean = getLdapServerTransportBean( configuration );

        if ( ldapServerTransportBean != null )
        {
            return ldapServerTransportBean.isEnabled();
        }

        return false;
    }


    // ── Is the LDAPS Port Open? ───────────────────────────────────────────────
    // Looks up the "ldaps" transport by ID and checks its enabled flag.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the LDAPS transport is enabled in the configuration.
     *
     * @param configuration  the parsed config bean.
     * @return               {@code true} if the LDAPS server transport is enabled.
     */
    public static boolean isEnableLdaps( ConfigBean configuration )
    {
        TransportBean ldapsServerTransportBean = getLdapsServerTransportBean( configuration );

        if ( ldapsServerTransportBean != null )
        {
            return ldapsServerTransportBean.isEnabled();
        }

        return false;
    }


    // ── Find the LDAP Transport Bean ─────────────────────────────────────────
    // Delegates to the ID-based lookup with the "ldap" ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} for the plain LDAP transport (ID "ldap").
     *
     * @param configuration  the parsed config bean.
     * @return               the LDAP transport bean, or {@code null} if not found.
     */
    private static TransportBean getLdapServerTransportBean( ConfigBean configuration )
    {
        return getLdapServerTransportBean( configuration, "ldap" ); //$NON-NLS-1$
    }


    // ── Find the LDAPS Transport Bean ────────────────────────────────────────
    // Delegates to the ID-based lookup with the "ldaps" ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} for the LDAPS transport (ID "ldaps").
     *
     * @param configuration  the parsed config bean.
     * @return               the LDAPS transport bean, or {@code null} if not found.
     */
    private static TransportBean getLdapsServerTransportBean( ConfigBean configuration )
    {
        return getLdapServerTransportBean( configuration, "ldaps" ); //$NON-NLS-1$
    }


    // ── Look Up a Transport Bean by ID ────────────────────────────────────────
    // Walks the LdapServerBean's transport array looking for the one whose
    // transportId matches the requested ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TransportBean} whose {@code transportId} equals {@code id},
     * or {@code null} if not found.
     *
     * @param configuration  the parsed config bean.
     * @param id             the transport ID to find, e.g. {@code "ldap"} or {@code "ldaps"}.
     * @return               the matching transport bean, or {@code null}.
     */
    private static TransportBean getLdapServerTransportBean( ConfigBean configuration, String id )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            LdapServerBean ldapServerBean = directoryServiceBean.getLdapServerBean();

            if ( ldapServerBean != null )
            {
                // Looking for the transport in the list
                TransportBean[] ldapServerTransportBeans = ldapServerBean.getTransports();
                if ( ldapServerTransportBeans != null )
                {
                    for ( TransportBean ldapServerTransportBean : ldapServerTransportBeans )
                    {
                        if ( id.equals( ldapServerTransportBean.getTransportId() ) )
                        {
                            return ldapServerTransportBean;
                        }
                    }
                }
            }
        }

        return null;
    }


    // ── Is the Kerberos Port Open? ────────────────────────────────────────────
    // Checks the KdcServerBean's enabled flag if present.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the Kerberos server is enabled.
     *
     * @param configuration  the parsed config bean.
     * @return               {@code true} if the KDC server is enabled.
     */
    public static boolean isEnableKerberos( ConfigBean configuration )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            KdcServerBean kdcServerBean = directoryServiceBean.getKdcServerBean();

            if ( kdcServerBean != null )
            {
                kdcServerBean.isEnabled();
            }
        }

        return false;
    }


    // ── Is the DNS Port Open? ─────────────────────────────────────────────────
    // Checks the DnsServerBean's enabled flag if present.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the DNS server is enabled.
     *
     * @param configuration  the parsed config bean.
     * @return               {@code true} if the DNS server is enabled.
     */
    public static boolean isEnableDns( ConfigBean configuration )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            DnsServerBean dnsServerBean = directoryServiceBean.getDnsServerBean();

            if ( dnsServerBean != null )
            {
                dnsServerBean.isEnabled();
            }
        }

        return false;
    }


    // ── Is the NTP Port Open? ─────────────────────────────────────────────────
    // Checks the NtpServerBean's enabled flag if present.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the NTP server is enabled.
     *
     * @param configuration  the parsed config bean.
     * @return               {@code true} if the NTP server is enabled.
     */
    public static boolean isEnableNtp( ConfigBean configuration )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            NtpServerBean ntpServerBean = directoryServiceBean.getNtpServerBean();

            if ( ntpServerBean != null )
            {
                ntpServerBean.isEnabled();
            }
        }

        return false;
    }


    // ── Is the Change Password Port Open? ─────────────────────────────────────
    // Checks the ChangePasswordServerBean's enabled flag if present.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the ChangePassword server is enabled.
     *
     * @param configuration  the parsed config bean.
     * @return               {@code true} if the ChangePassword server is enabled.
     */
    public static boolean isEnableChangePassword( ConfigBean configuration )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            ChangePasswordServerBean changePasswordServerBean = directoryServiceBean.getChangePasswordServerBean();

            if ( changePasswordServerBean != null )
            {
                changePasswordServerBean.isEnabled();
            }
        }

        return false;
    }


    // ── Get the LDAP Port Number ──────────────────────────────────────────────
    // Reads the system port number from the LDAP transport bean.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the port number of the LDAP transport, or {@code 0} if not configured.
     *
     * @param configuration  the parsed config bean.
     * @return               the LDAP port.
     */
    public static int getLdapPort( ConfigBean configuration )
    {
        TransportBean ldapServerTransportBean = getLdapServerTransportBean( configuration );

        if ( ldapServerTransportBean != null )
        {
            return ldapServerTransportBean.getSystemPort();
        }

        return 0;
    }


    // ── Get the LDAPS Port Number ─────────────────────────────────────────────
    // Reads the system port number from the LDAPS transport bean.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the port number of the LDAPS transport, or {@code 0} if not configured.
     *
     * @param configuration  the parsed config bean.
     * @return               the LDAPS port.
     */
    public static int getLdapsPort( ConfigBean configuration )
    {
        TransportBean ldapsServerTransportBean = getLdapsServerTransportBean( configuration );

        if ( ldapsServerTransportBean != null )
        {
            return ldapsServerTransportBean.getSystemPort();
        }

        return 0;
    }


    // ── Get the Kerberos Port Number ──────────────────────────────────────────
    // Walks the KDC transport list looking for the tcp or udp transport.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Kerberos (KDC) port number, or {@code 0} if not configured.
     * Looks for a transport whose ID is {@code "tcp"} or {@code "udp"}.
     *
     * @param configuration  the parsed config bean.
     * @return               the Kerberos port.
     */
    public static int getKerberosPort( ConfigBean configuration )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            KdcServerBean kdcServerBean = directoryServiceBean.getKdcServerBean();

            if ( kdcServerBean != null )
            {
                // Looking for the transport in the list
                TransportBean[] kdcServerTransportBeans = kdcServerBean.getTransports();

                if ( kdcServerTransportBeans != null )
                {
                    for ( TransportBean kdcServerTransportBean : kdcServerTransportBeans )
                    {
                        if ( ( "tcp".equals( kdcServerTransportBean.getTransportId() ) ) //$NON-NLS-1$
                            || ( "udp".equals( kdcServerTransportBean.getTransportId() ) ) ) //$NON-NLS-1$
                        {
                            return kdcServerTransportBean.getSystemPort();
                        }
                    }
                }
            }
        }

        return 0;
    }


    // ── Get the DNS Port Number ───────────────────────────────────────────────
    // Walks the DNS transport list for tcp or udp.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DNS server port number, or {@code 0} if not configured.
     *
     * @param configuration  the parsed config bean.
     * @return               the DNS port.
     */
    public static int getDnsPort( ConfigBean configuration )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            DnsServerBean dnsServerBean = directoryServiceBean.getDnsServerBean();

            if ( dnsServerBean != null )
            {
                // Looking for the transport in the list
                TransportBean[] dnsServerTransportBeans = dnsServerBean.getTransports();

                if ( dnsServerTransportBeans != null )
                {
                    for ( TransportBean dnsServerTransportBean : dnsServerTransportBeans )
                    {
                        if ( ( "tcp".equals( dnsServerTransportBean.getTransportId() ) ) //$NON-NLS-1$
                            || ( "udp".equals( dnsServerTransportBean.getTransportId() ) ) ) //$NON-NLS-1$
                        {
                            return dnsServerTransportBean.getSystemPort();
                        }
                    }
                }
            }
        }

        return 0;
    }


    // ── Get the NTP Port Number ───────────────────────────────────────────────
    // Walks the NTP transport list for tcp or udp.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the NTP server port number, or {@code 0} if not configured.
     *
     * @param configuration  the parsed config bean.
     * @return               the NTP port.
     */
    public static int getNtpPort( ConfigBean configuration )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            NtpServerBean ntpServerBean = directoryServiceBean.getNtpServerBean();

            if ( ntpServerBean != null )
            {
                // Looking for the transport in the list
                TransportBean[] ntpServerTransportBeans = ntpServerBean.getTransports();

                if ( ntpServerTransportBeans != null )
                {
                    for ( TransportBean ntpServerTransportBean : ntpServerTransportBeans )
                    {
                        if ( ( "tcp".equals( ntpServerTransportBean.getTransportId() ) ) //$NON-NLS-1$
                            || ( "udp".equals( ntpServerTransportBean.getTransportId() ) ) ) //$NON-NLS-1$
                        {
                            return ntpServerTransportBean.getSystemPort();
                        }
                    }
                }
            }
        }

        return 0;
    }


    // ── Get the Change Password Port Number ───────────────────────────────────
    // Walks the ChangePassword transport list for tcp or udp.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ChangePassword server port number, or {@code 0} if not configured.
     *
     * @param configuration  the parsed config bean.
     * @return               the ChangePassword port.
     */
    public static int getChangePasswordPort( ConfigBean configuration )
    {
        DirectoryServiceBean directoryServiceBean = configuration.getDirectoryServiceBean();

        if ( directoryServiceBean != null )
        {
            ChangePasswordServerBean changePasswordServerBean = directoryServiceBean.getChangePasswordServerBean();

            if ( changePasswordServerBean != null )
            {
                // Looking for the transport in the list
                TransportBean[] changePasswordServerTransportBeans = changePasswordServerBean.getTransports();

                if ( changePasswordServerTransportBeans != null )
                {
                    for ( TransportBean changePasswordServerTransportBean : changePasswordServerTransportBeans )
                    {
                        if ( ( "tcp".equals( changePasswordServerTransportBean.getTransportId() ) ) //$NON-NLS-1$
                            || ( "udp".equals( changePasswordServerTransportBean.getTransportId() ) ) ) //$NON-NLS-1$
                        {
                            return changePasswordServerTransportBean.getSystemPort();
                        }
                    }
                }
            }
        }

        return 0;
    }


    // ── Pre-Flight Checklist — Are All the Ports Free? ────────────────────────
    // For each enabled protocol we check whether its configured port is already
    // in use by another process.  Any collision is added to the "already in use"
    // list which the caller displays to the operator before aborting the launch.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks all configured protocol ports for availability before starting the server.
     * For each enabled protocol (LDAP, LDAPS, Kerberos, DNS, NTP, ChangePassword)
     * we probe the port using {@link AvailablePortFinder}.  Any port that is
     * already in use is added to the returned list so the caller can warn the
     * operator before aborting the launch attempt.
     *
     * @param server  the server to check.
     * @return        an array of human-readable port-conflict descriptions;
     *                empty if all ports are free.
     * @throws Exception  if reading the server configuration fails.
     * @see LdapServerAdapter#checkPortsBeforeServerStart(LdapServer)
     */
    public String[] checkPortsBeforeServerStart( LdapServer server ) throws Exception
    {
        List<String> alreadyInUseProtocolPortsList = new ArrayList<String>();

        ConfigBean configuration = getServerConfiguration( server ).getConfigBean();

        // LDAP
        if ( isEnableLdap( configuration ) )
        {
            if ( !AvailablePortFinder.available( getLdapPort( configuration ) ) )
            {
                alreadyInUseProtocolPortsList
                    .add( NLS.bind(
                        Messages.getString( "ApacheDS200LdapServerAdapter.LDAPPort" ), new Object[] { getLdapPort( configuration ) } ) ); //$NON-NLS-1$
            }
        }

        // LDAPS
        if ( isEnableLdaps( configuration ) )
        {
            if ( !AvailablePortFinder.available( getLdapsPort( configuration ) ) )
            {
                alreadyInUseProtocolPortsList
                    .add( NLS.bind(
                        Messages.getString( "ApacheDS200LdapServerAdapter.LDAPSPort" ), new Object[] { getLdapsPort( configuration ) } ) ); //$NON-NLS-1$
            }
        }

        // Kerberos
        if ( isEnableKerberos( configuration ) )
        {
            if ( !AvailablePortFinder.available( getKerberosPort( configuration ) ) )
            {
                alreadyInUseProtocolPortsList
                    .add( NLS
                        .bind(
                            Messages.getString( "ApacheDS200LdapServerAdapter.KerberosPort" ), new Object[] { getKerberosPort( configuration ) } ) ); //$NON-NLS-1$
            }
        }

        // DNS
        if ( isEnableDns( configuration ) )
        {
            if ( !AvailablePortFinder.available( getDnsPort( configuration ) ) )
            {
                alreadyInUseProtocolPortsList
                    .add( NLS.bind(
                        Messages.getString( "ApacheDS200LdapServerAdapter.DNSPort" ), new Object[] { getDnsPort( configuration ) } ) ); //$NON-NLS-1$
            }
        }

        // NTP
        if ( isEnableNtp( configuration ) )
        {
            if ( !AvailablePortFinder.available( getNtpPort( configuration ) ) )
            {
                alreadyInUseProtocolPortsList.add( NLS.bind(
                    Messages.getString( "ApacheDS200LdapServerAdapter.NTPPort" ), new Object[] //$NON-NLS-1$
                    { getNtpPort( configuration ) } ) );
            }
        }

        // Change Password
        if ( isEnableChangePassword( configuration ) )
        {
            if ( !AvailablePortFinder.available( getChangePasswordPort( configuration ) ) )
            {
                alreadyInUseProtocolPortsList
                    .add( NLS
                        .bind(
                            Messages.getString( "ApacheDS200LdapServerAdapter.ChangePasswordPort" ), new Object[] { getChangePasswordPort( configuration ) } ) ); //$NON-NLS-1$
            }
        }

        return alreadyInUseProtocolPortsList.toArray( new String[0] );
    }
}
