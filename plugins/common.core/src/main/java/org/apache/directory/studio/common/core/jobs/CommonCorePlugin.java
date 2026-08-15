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

package org.apache.directory.studio.common.core.jobs;


import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


// ── CLASS: CommonCorePlugin — Opening Yavin 4 Mission Control ─────────────────
// When the Rebel Alliance opens the Yavin 4 mission control room for business
// it sets up the central communications board (starts the watcher job) and
// records the shared instance so any rebel anywhere in the base can call
// getDefault() to reach it.  When Alliance HQ shuts down the base, it
// signals the sentinel officer to stand down (stop()) and clears the instance.
// CommonCorePlugin is that mission control room: the OSGi activator that
// starts and stops the background watcher job and holds the shared singleton.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator for the Common Core plugin.
 * Controls the plugin life cycle: on {@link #start} we create and schedule the
 * {@link StudioProgressMonitorWatcherJob} (a background sentinel that polls
 * active monitors once per second); on {@link #stop} we shut it down.
 * The shared instance is available to any code in the bundle via
 * {@link #getDefault()}.
 * Think of this as Yavin 4 mission control: it opens the room when the Alliance
 * needs it and closes it cleanly when they evacuate.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CommonCorePlugin extends Plugin
{
    /** The shared plugin instance. */
    private static CommonCorePlugin plugin;

    /** The watcher job */
    private StudioProgressMonitorWatcherJob studioProgressMonitorWatcherJob;


    // ── Mission Control Opens Its Doors ───────────────────────────────────────
    // The constructor fires at bundle-activation time.  We store 'this' in the
    // static field immediately so other classes can reach the shared instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the plugin instance and registers it as the shared singleton.
     * Called once by OSGi when the bundle is activated.
     */
    public CommonCorePlugin()
    {
        super();
        plugin = this;
    }


    // ── Alliance Activates the Sentinel Duty Roster ───────────────────────────
    // At start() we spin up the watcher job (the sentinel officer who checks
    // every active monitor once per second) as a system job so it doesn't
    // show up in the user-visible job list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Starts the plugin — creates and schedules the background
     * {@link StudioProgressMonitorWatcherJob} as a hidden system job.
     *
     * @param context  the OSGi bundle context.
     * @throws Exception  if the parent start() throws.
     * @see Plugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        studioProgressMonitorWatcherJob = new StudioProgressMonitorWatcherJob();
        studioProgressMonitorWatcherJob.setSystem( true );
        studioProgressMonitorWatcherJob.schedule();
    }


    // ── Alliance Evacuates Yavin — Stand Down the Sentinel ───────────────────
    // At stop() we tell the watcher job to exit its loop, null everything out,
    // and let OSGi clean up.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stops the plugin — signals the {@link StudioProgressMonitorWatcherJob}
     * to exit its loop, then nulls out all references.
     *
     * @param context  the OSGi bundle context.
     * @throws Exception  if the parent stop() throws.
     * @see Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;

        studioProgressMonitorWatcherJob.stop();
        studioProgressMonitorWatcherJob = null;

        super.stop( context );
    }


    // ── Get a Reference to Mission Control ────────────────────────────────────
    // Any rebel anywhere in the base can call getDefault() to reach the shared
    // plugin instance — e.g. to register a new progress monitor with the
    // watcher job.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared CommonCorePlugin instance.
     *
     * @return  the singleton plugin instance, or {@code null} before activation
     *          or after deactivation.
     */
    public static CommonCorePlugin getDefault()
    {
        return plugin;
    }


    // ── Reach the Sentinel Officer Directly ───────────────────────────────────
    // The watcher job reference is needed by StudioProgressMonitor's constructor
    // so it can register itself with the sentinel for cancellation polling.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the background {@link StudioProgressMonitorWatcherJob}.
     * Used by {@link StudioProgressMonitor} to register new monitors with the
     * sentinel for cancellation and rate-limiting.
     *
     * @return  the watcher job instance.
     */
    public StudioProgressMonitorWatcherJob getStudioProgressMonitorWatcherJob()
    {
        return studioProgressMonitorWatcherJob;
    }

}
