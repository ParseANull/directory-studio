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

package org.apache.directory.studio.ldapservers;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;
import org.apache.mina.util.AvailablePortFinder;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.framework.Bundle;


// ── CLASS: LdapServersUtils — THE REBEL ENGINEER'S TOOLKIT ──────────────────────────────
// Rebel engineers share a toolkit of utility functions: a watchdog that watches a port until
// the Falcon's hyperdrive comes online, a console printer that tails the Falcon's flight log
// and streams it to Cassian's intercept station, a library copier that ensures the right
// JAR files are in the right bay before takeoff, and a terminate routine that cuts the engine.
// This class is that toolkit: static helper methods shared across the server start/stop jobs.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility methods shared across the LDAP server start/stop job infrastructure.
 * <ul>
 *   <li>{@link #runStartupListenerWatchdog} — polls a TCP port until the server reports STARTED.</li>
 *   <li>{@link #startTerminateListenerThread} — listens for the Eclipse debug TERMINATE event.</li>
 *   <li>{@link #startConsolePrinterThread} — tails the server's log file to the console.</li>
 *   <li>{@link #stopConsolePrinterThread} — stops the log tailer.</li>
 *   <li>{@link #terminateLaunchConfiguration} — terminates the OS-level server process.</li>
 *   <li>{@link #verifyAndCopyLibraries} — copies JAR files to the server's lib folder if stale.</li>
 *   <li>{@link #copyResource} / {@code copyFile} — low-level stream copy helpers.</li>
 * </ul>
 * Think of it as the Rebel engineer's shared toolkit.
 */
public class LdapServersUtils
{
    /** The ID of the launch configuration custom object */
    public static final String LAUNCH_CONFIGURATION_CUSTOM_OBJECT = "launchConfiguration"; //$NON-NLS-1$

    /** The ID of the console printer custom object */
    public static final String CONSOLE_PRINTER_CUSTOM_OBJECT = "consolePrinter"; //$NON-NLS-1$


    // ── Watchdog: Poll The Port Until The Ship's Drive Comes Online ────────────────────────────
    // After the Falcon's crew fires the hyperdrive, a watchdog checks the port every second.
    // If the port becomes occupied within 3 minutes, the server is STARTED.
    // If the watchdog timer expires with the server still in STARTING state, we declare it STOPPED.
    // If port is 0 (no protocol enabled), skip the check and let the adapter declare STARTED itself.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Polls {@code port} once per second for up to 3 minutes until the server either opens the port
     * (→ sets status STARTED) or the status changes away from STARTING externally.
     * If the watchdog expires with status still STARTING, sets status to STOPPED.
     * If {@code port} is 0, returns immediately (caller handles STARTED status directly).
     *
     * <p>For example — waiting for the Falcon's hyperdrive to engage:</p>
     * <pre>
     *   runStartupListenerWatchdog(server, 10389)
     *   // polls port 10389 every 1 s for up to 180 s
     *   // port no longer available → server.setStatus(STARTED) → return
     * </pre>
     *
     * @param server  the server whose status we're watching
     * @param port    the TCP port to poll; 0 to skip polling entirely
     * @throws Exception if the thread is interrupted unexpectedly
     */
    public static void runStartupListenerWatchdog( LdapServer server, int port ) throws Exception
    {
        // If no protocol is enabled, we pass this and declare the server as started
        if ( port == 0 )
        {
            return;
        }

        // Getting the current time
        long startTime = System.currentTimeMillis();

        // Calculating the watch dog time
        final long watchDog = startTime + ( 1000 * 60 * 3 ); // 3 minutes

        // Looping until the end of the watchdog if the server is still 'starting'
        while ( ( System.currentTimeMillis() < watchDog ) && ( LdapServerStatus.STARTING == server.getStatus() ) )
        {
            // Trying to see if the port is available
            if ( AvailablePortFinder.available( port ) )
            {
                // The port is still available

                // We just wait one second before starting the test once again
                try
                {
                    Thread.sleep( 1000 );
                }
                catch ( InterruptedException e1 )
                {
                    // Nothing to do...
                }
            }
            else
            {
                // We set the state of the server to 'started'...
                server.setStatus( LdapServerStatus.STARTED );

                // ... and we exit the thread
                return;

            }
        }

        // If, at the end of the watch dog, the state of the server is
        // still 'starting' then, we declare the server as 'stopped'
        if ( LdapServerStatus.STARTING == server.getStatus() )
        {
            server.setStatus( LdapServerStatus.STOPPED );
        }
    }


    // ── Thread: Watch For The OS Process To Terminate ─────────────────────────────────────────
    // After the launch is created, we start a background thread that listens for Eclipse's
    // DebugPlugin TERMINATE event.  When the OS process dies (crash or clean shutdown),
    // we mark the server STOPPED and unregister ourselves.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Starts a background thread that registers an {@link IDebugEventSetListener} on
     * {@link DebugPlugin} and waits for a {@link DebugEvent#TERMINATE} event matching
     * the given {@code launch}.
     * On termination, sets the server's status to STOPPED and removes the listener.
     *
     * <p>For example — the sensor watches the hangar until the Falcon's engines go quiet:</p>
     * <pre>
     *   startTerminateListenerThread(server, launch)
     *   // OS process exits → TERMINATE event → server.setStatus(STOPPED)
     * </pre>
     *
     * @param server  the server to set STOPPED when the process terminates
     * @param launch  the Eclipse debug launch representing the running OS process
     */
    public static void startTerminateListenerThread( final LdapServer server, final ILaunch launch )
    {
        // Creating the thread
        Thread thread = new Thread()
        {
            public void run()
            {
                // Adding the listener
                DebugPlugin.getDefault().addDebugEventListener( new IDebugEventSetListener()
                {
                    public void handleDebugEvents( DebugEvent[] events )
                    {
                        // Looping on the debug events array
                        for ( DebugEvent debugEvent : events )
                        {
                            // We only care of event with kind equals to
                            // 'terminate'
                            if ( debugEvent.getKind() == DebugEvent.TERMINATE )
                            {
                                // Getting the source of the debug event
                                Object source = debugEvent.getSource();
                                if ( source instanceof RuntimeProcess )
                                {
                                    RuntimeProcess runtimeProcess = ( RuntimeProcess ) source;

                                    // Getting the associated launch
                                    ILaunch debugEventLaunch = runtimeProcess.getLaunch();
                                    if ( debugEventLaunch.equals( launch ) )
                                    {
                                        // The launch we had created is now terminated
                                        // The server is now stopped
                                        server.setStatus( LdapServerStatus.STOPPED );

                                        // Removing the listener
                                        DebugPlugin.getDefault().removeDebugEventListener( this );

                                        // ... and we exit the thread
                                        return;
                                    }
                                }
                            }
                        }
                    }
                } );
            }
        };

        // Starting the thread
        thread.start();
    }


    // ── Starting The Console Printer: Tailing The Server's Log File ──────────────────────────
    // Cassian's intercept station needs the Falcon's transmission log in real time.
    // We create a Tailer (a commons-io utility that tails a file like "tail -f") starting from
    // the end of the file, and stream each new line to the server's Eclipse console.
    // The Tailer is stored as a custom object on the server so we can stop it later.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Starts an Apache Commons IO {@link Tailer} that reads new lines from {@code serverLogsFile}
     * every 1 second (starting from the end of the file) and prints them to the server's
     * Eclipse Message Console via {@link ConsolesManager}.
     * The Tailer is stored as {@link #CONSOLE_PRINTER_CUSTOM_OBJECT} on the server.
     *
     * @param server          the server whose console should receive the log output
     * @param serverLogsFile  the log file to tail
     */
    public static void startConsolePrinterThread( LdapServer server, File serverLogsFile )
    {
        MessageConsole messageConsole = ConsolesManager.getDefault().getMessageConsole( server );
        MessageConsoleStream messageStream = messageConsole.newMessageStream();

        /*
         * DIRSTUDIO-1148: Tail the log file and update the console.
         * Tail from end only to avoid overwhelming the system in case the log file is large.
         */
        TailerListener l = new TailerListenerAdapter()
        {
            public void handle( String line )
            {
                messageStream.println( line );
            };
        };
        Tailer tailer = Tailer.create( serverLogsFile, l, 1000L, true );

        // Storing the tailer as a custom object in the LDAP Server for later use
        server.putCustomObject( CONSOLE_PRINTER_CUSTOM_OBJECT, tailer );
    }


    // ── Stopping The Console Printer ──────────────────────────────────────────────────────────
    // When the server stops, we don't need the log tailer any more.
    // We pull it from the server's custom objects and tell it to stop.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Stops the {@link Tailer} stored under {@link #CONSOLE_PRINTER_CUSTOM_OBJECT} on the server.
     * Does nothing if no tailer was stored (e.g., the server was never started).
     *
     * @param server  the server whose log tailer should be stopped
     */
    public static void stopConsolePrinterThread( LdapServer server )
    {
        // Getting the console printer
        Tailer tailer = ( Tailer ) server
            .removeCustomObject( CONSOLE_PRINTER_CUSTOM_OBJECT );
        if ( tailer != null )
        {
            // Closing the console printer
            tailer.stop();
        }
    }


    // ── Terminating The OS-Level Server Process ───────────────────────────────────────────────
    // When we stop the server, we need to terminate the OS process backing it.
    // We retrieve the stored ILaunch, check it isn't already terminated, and terminate it.
    // If no launch is found, we throw an exception — something went wrong with startup.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Terminates the OS-level server process by calling {@link ILaunch#terminate()} on the
     * launch stored as {@link #LAUNCH_CONFIGURATION_CUSTOM_OBJECT} on the server.
     * Throws an {@link Exception} if no launch object is found — indicating the server was
     * never properly started or the launch reference was lost.
     *
     * @param server  the server whose OS process should be terminated
     * @throws Exception if the launch cannot be found or the terminate call fails
     */
    public static void terminateLaunchConfiguration( LdapServer server ) throws Exception
    {
        // Getting the launch
        ILaunch launch = ( ILaunch ) server.removeCustomObject( LdapServersUtils.LAUNCH_CONFIGURATION_CUSTOM_OBJECT );
        if ( launch != null )
        {
            if ( ( !launch.isTerminated() ) )
            {
                // Terminating the launch
                launch.terminate();
            }
        }
        else
        {
            throw new Exception(
                Messages.getString( "LdapServersUtils.AssociatedLaunchConfigurationCouldNotBeFoundOrTerminated" ) ); //$NON-NLS-1$
        }
    }


    // ── Private: Verify And Copy Libraries Without Progress Monitor ───────────────────────────
    // The internal version of the library copier — no progress-monitor scaffolding.
    // Checks each JAR file: if it doesn't exist or the bundle is newer, we copy it from the
    // bundle's source path to the destination.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks each library in {@code libraries} and copies it from the bundle to the destination
     * folder if the destination file is absent or older than the bundle's last-modified time.
     * Shows an error dialog (non-fatal) if any individual copy fails.
     *
     * @param bundle                    the OSGi bundle containing the source libraries
     * @param sourceLibrariesPath       the path within the bundle where the JARs live
     * @param destinationLibrariesPath  the on-disk folder to copy into (created if absent)
     * @param libraries                 array of JAR file names to verify and copy
     */
    private static void verifyAndCopyLibraries( Bundle bundle, IPath sourceLibrariesPath,
        IPath destinationLibrariesPath, String[] libraries )
    {
        // Destination libraries folder
        File destinationLibraries = destinationLibrariesPath.toFile();
        if ( !destinationLibraries.exists() )
        {
            destinationLibraries.mkdir();
        }

        // Verifying and copying libraries (if needed)
        for ( String library : libraries )
        {
            File destinationLibraryFile = destinationLibrariesPath.append( library ).toFile();
            boolean newerFileExists = (bundle.getLastModified() > destinationLibraryFile.lastModified());
            if ( !destinationLibraryFile.exists() || newerFileExists )
            {
                try
                {
                    copyResource( bundle, sourceLibrariesPath.append( library ), destinationLibraryFile );
                }
                catch ( IOException e )
                {
                    CommonUIUtils.openErrorDialog( NLS.bind(
                        Messages.getString( "LdapServersUtils.ErrorCopyingLibrary" ), //$NON-NLS-1$
                        new String[]
                            { library, destinationLibraryFile.getAbsolutePath(), e.getMessage() } ) );
                }
            }
        }
    }


    // ── Public: Verify And Copy Libraries With Progress Monitor ──────────────────────────────
    // The public-facing overload used by server adapter plugins during the start job.
    // Sets a sub-task description on the monitor before delegating to the private version.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Verifies and copies all required JAR libraries for a server, reporting progress.
     * Sets a sub-task description on {@code monitor}, then delegates to the private
     * {@link #verifyAndCopyLibraries(Bundle, IPath, IPath, String[])} overload.
     *
     * <p>For example — copying ApacheDS JARs before starting the server:</p>
     * <pre>
     *   verifyAndCopyLibraries(bundle, "/lib", serverLibPath, libNames, monitor, "Copying libraries...")
     *   // each JAR is checked and copied if needed
     * </pre>
     *
     * @param bundle                    the OSGi bundle containing the source libraries
     * @param sourceLibrariesPath       the path within the bundle where the JARs live
     * @param destinationLibrariesPath  the on-disk folder to copy into
     * @param libraries                 array of JAR file names to verify and copy
     * @param monitor                   the progress monitor to report to
     * @param monitorTaskName           the sub-task name to display in the progress dialog
     */
    public static void verifyAndCopyLibraries( Bundle bundle, IPath sourceLibrariesPath,
        IPath destinationLibrariesPath, String[] libraries, StudioProgressMonitor monitor, String monitorTaskName )
    {
        // Creating the sub-task on the monitor
        monitor.subTask( monitorTaskName );

        // Verifying and copying the libraries
        verifyAndCopyLibraries( bundle, sourceLibrariesPath, destinationLibrariesPath, libraries );
    }


    // ── Copying A Single Resource From The Bundle ─────────────────────────────────────────────
    // We locate the resource inside the bundle via FileLocator, open input/output streams,
    // copy the bytes, and close everything.  Throws IOException if the copy fails.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Copies a single resource file from within an OSGi {@link Bundle} to a file on disk.
     * Uses {@link FileLocator#find} to resolve the bundle-relative path to a URL,
     * then copies the bytes via {@link #copyFile}.
     *
     * @param bundle       the OSGi bundle containing the resource
     * @param resource     the bundle-relative path to the resource file
     * @param destination  the destination file on disk
     * @throws IOException if the resource cannot be found or the copy fails
     */
    public static void copyResource( Bundle bundle, IPath resource, File destination ) throws IOException
    {
        // Getting he URL of the resource within the bundle
        URL resourceUrl = FileLocator.find( bundle, resource, null );

        // Creating the input and output streams
        InputStream resourceInputStream = resourceUrl.openStream();
        FileOutputStream resourceOutputStream = new FileOutputStream( destination );

        // Copying the resource
        copyFile( resourceInputStream, resourceOutputStream );

        // Closing the streams
        resourceInputStream.close();
        resourceOutputStream.close();
    }


    // ── Low-Level Byte-by-Byte Stream Copy ────────────────────────────────────────────────────
    // Reads 1 KB chunks from the input and writes them to the output.
    // Used by copyResource() to move JAR files from the bundle to the server's lib folder.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Copies all bytes from {@code inputStream} to {@code outputStream} using a 1 KB buffer.
     * Neither stream is closed by this method — the caller is responsible for closing them.
     *
     * @param inputStream   the source stream
     * @param outputStream  the destination stream
     * @throws IOException if reading or writing fails
     */
    private static void copyFile( InputStream inputStream, OutputStream outputStream ) throws IOException
    {
        byte[] buf = new byte[1024];
        int i = 0;
        while ( ( i = inputStream.read( buf ) ) != -1 )
        {
            outputStream.write( buf, 0, i );
        }
    }
}
