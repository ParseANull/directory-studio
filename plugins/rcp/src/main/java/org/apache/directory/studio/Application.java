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

package org.apache.directory.studio;


import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


// ── CLASS: Application — Mon Mothma Gives the Order at the Rebel Briefing ────
// In the Yavin briefing room, Mon Mothma stands before the assembled Rebel
// leadership and gives the mission order: "Many Bothans died to bring us this
// information."  That single command sets every subsequent event in motion.
// This class is exactly that: the Eclipse entry point that fires up the entire
// Directory Studio workbench and keeps it running until the user closes it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse RCP application entry point — the {@code main()} of Directory Studio.
 * When the user launches the app, Equinox calls {@link #start} which creates the
 * SWT display, spins up the Eclipse workbench, and blocks until the user quits.
 * Think of this class as Mon Mothma issuing the mission order: one method call
 * that sets the entire operation in motion.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Application implements IApplication
{
    /** The plugin ID */
    public static final String PLUGIN_ID = "org.apache.directory.studio.rcp"; //$NON-NLS-1$


    // ── Mon Mothma Gives the Order — The Mission Begins ──────────────────────
    // Mon Mothma steps up to the holographic display in the Yavin briefing room
    // and delivers her order: "You may fire when ready."  From that moment every
    // Rebel pilot, droid, and officer has their role and the clock is running.
    // start() is our equivalent: it creates the SWT display, hands control to the
    // Eclipse workbench event loop, and only returns when the user quits.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Launches the Directory Studio workbench and blocks until the user exits.
     * We create an SWT {@link Display} (the OS window system connection), hand it
     * to {@code PlatformUI.createAndRunWorkbench} along with our
     * {@link ApplicationWorkbenchAdvisor}, and then sit in the event loop until
     * the user closes the last window.  If the workbench wants a restart (e.g.
     * after installing a plugin update) we return {@link IApplication#EXIT_RESTART};
     * otherwise we return {@link IApplication#EXIT_OK}.
     *
     * <p>For example — Mon Mothma issues the mission order:</p>
     * <pre>
     *   context.applicationRunning()  // Equinox splash screen dismissed
     *   display = PlatformUI.createDisplay()  // OS connection established
     *   PlatformUI.createAndRunWorkbench(display, advisor)  // event loop runs
     *   // ... user works for hours ...
     *   user closes last window → return EXIT_OK
     * </pre>
     *
     * @param context  the Equinox application context — we don't use it directly
     *                 but it's required by the {@link IApplication} contract.
     * @return         {@link IApplication#EXIT_RESTART} if the workbench requested
     *                 a restart, {@link IApplication#EXIT_OK} otherwise.
     * @throws Exception  if something goes badly wrong during workbench creation.
     */
    public Object start( IApplicationContext context ) throws Exception
    {
        Display display = PlatformUI.createDisplay();

        try
        {
            int returnCode = PlatformUI.createAndRunWorkbench( display, new ApplicationWorkbenchAdvisor() );
            if ( returnCode == PlatformUI.RETURN_RESTART )
            {
                return IApplication.EXIT_RESTART;
            }
            else
            {
                return IApplication.EXIT_OK;
            }
        }
        finally
        {
            display.dispose();
        }
    }


    // ── The Rebel Base Evacuates — Emergency Shutdown ─────────────────────────
    // After the Battle of Yavin the Rebel base goes to emergency evacuation
    // protocols: Mon Mothma orders all operations to cease and the base to clear
    // out before Imperial reinforcements arrive.
    // stop() is called when something outside the normal event loop (like the OS)
    // asks us to terminate — we reach into the workbench and tell it to close.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Forces the workbench to shut down from outside the normal event loop.
     * This is called by Equinox when it needs to stop our application
     * programmatically — not the usual "user closes the window" path.
     * We synchronously post a close request to the SWT display thread because
     * workbench operations must happen on the UI thread.
     *
     * <p>For example — Mon Mothma orders emergency base evacuation:</p>
     * <pre>
     *   stop() called from OS signal handler (non-UI thread)
     *   → display.syncExec posts workbench.close() to the UI thread
     *   → workbench closes all windows and shuts down
     * </pre>
     */
    public void stop()
    {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if ( workbench == null )
        {
            return;
        }
        final Display display = workbench.getDisplay();
        display.syncExec( new Runnable()
        {
            public void run()
            {
                if ( !display.isDisposed() )
                {
                    workbench.close();
                }
            }
        } );
    }
}
