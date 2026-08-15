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


import java.io.IOException;
import java.util.PropertyResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: Activator — The Rebel Fleet Assembles at Sullust ──────────────────
// Before the Battle of Endor, Admiral Ackbar coordinates the Rebel fleet's
// assembly at the Sullust rendezvous point — ships power up systems, run
// self-checks, and are ready to jump to hyperspace on command.
// The Activator does exactly that for our Eclipse plugin: it's the first thing
// OSGi calls when the rcp bundle loads, and the last thing called when it stops.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator for the rcp plugin — the plugin's lifecycle manager.
 * Eclipse's OSGi runtime calls {@link #start} when the bundle first loads and
 * {@link #stop} when it shuts down. We also provide static helpers for obtaining
 * image descriptors and reading plugin.properties from the bundle's JAR.
 * Think of this class as Admiral Ackbar at the Sullust rendezvous — it
 * coordinates the fleet's readiness before the mission begins.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Activator extends AbstractUIPlugin
{

    //The shared instance.
    private static Activator plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── Fleet Arrives at Sullust — All Ships Report In ───────────────────────
    // The first Rebel cruiser drops out of hyperspace at Sullust and immediately
    // registers its position with fleet command so everyone knows it's present.
    // When OSGi instantiates our Activator it similarly registers itself as the
    // shared instance so the rest of the codebase can reach it via getDefault().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the Activator and registers it as the plugin's shared instance.
     * OSGi calls this constructor exactly once when the bundle loads. After this
     * point any code in the plugin can call {@link #getDefault()} to reach us.
     */
    public Activator()
    {
        plugin = this;
    }


    // ── Ackbar Gives the Order: "All Craft, Prepare to Jump!" ────────────────
    // Admiral Ackbar opens the fleet-wide channel and orders all ships to spin up
    // their hyperdrives — systems that were idle come alive, shields go up.
    // Our start() mirrors that: OSGi calls it when the bundle activates and we
    // let the parent AbstractUIPlugin do the heavy lifting of wiring Eclipse's
    // image registry and preference store.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when our bundle is activated — the plugin's "power on" moment.
     * We delegate to the parent class which wires up the image registry and
     * preference store that the rest of the plugin relies on.
     *
     * @param context  the OSGi bundle context provided by the runtime — we pass
     *                 it straight to the parent so it can do its initialisation.
     * @throws Exception  if the parent's startup sequence fails for any reason.
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
    }


    // ── The Fleet Disperses — Ships Stand Down After the Battle ──────────────
    // After the Death Star explodes at Yavin the Rebel fleet powers down to
    // standby mode; the shared registry of active ships is cleared.
    // When OSGi deactivates our bundle we null out the shared instance reference
    // so it can be garbage-collected, then let the parent clean up its resources.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when our bundle is deactivated — the plugin's "power off" moment.
     * We clear the shared-instance reference so nothing holds a stale pointer to us,
     * then call the parent to release image registries and preference stores.
     *
     * @param context  the OSGi bundle context — passed to the parent for cleanup.
     * @throws Exception  if the parent's shutdown sequence fails for any reason.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── Fleet Command Answers the Hail — "Activator, Report!" ────────────────
    // Any ship in the Rebel fleet can hail fleet command and get a direct line
    // to Admiral Ackbar's command cruiser — one shared point of authority.
    // getDefault() is that direct line: every other class in the plugin calls
    // it to reach our shared Activator instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the single shared Activator instance for the rcp plugin.
     * Eclipse plugins are singletons — there is exactly one Activator alive
     * at any time, and this static accessor is how the rest of the code reaches
     * things like the preference store and image registry.
     *
     * @return  the live Activator instance, or {@code null} if the bundle has
     *          not yet started or has already stopped.
     */
    public static Activator getDefault()
    {
        return plugin;
    }


    // ── R2-D2 Projects the Hologram — Fetching an Image by Path ─────────────
    // R2-D2 slots into the projector socket and retrieves the exact holographic
    // file by its archive path, handing a ready-to-use projection back to Luke.
    // getImageDescriptor() does the same: callers pass a plugin-relative path and
    // get back an Eclipse ImageDescriptor they can use immediately in a menu or toolbar.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an Eclipse {@link ImageDescriptor} for an icon inside our plugin bundle.
     * Image descriptors are lazy — they describe where an image lives without
     * actually loading the pixels until Eclipse needs to paint them on screen.
     *
     * <p>For example — R2-D2 retrieves the hologram by archive path:</p>
     * <pre>
     *   R2 accepts path "resources/icons/about.png"
     *   → locates the file inside the rcp plugin JAR
     *   → hands back a ready-to-project ImageDescriptor
     * </pre>
     *
     * @param path  plugin-relative path to the image file, e.g.
     *              {@code "resources/icons/about.png"} — see {@link ImageKeys}.
     * @return      an {@link ImageDescriptor} that Eclipse can use to paint the icon.
     */
    public static ImageDescriptor getImageDescriptor( String path )
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin( Application.PLUGIN_ID, path );
    }


    // ── R2 Opens the Archive — Reading the Bundle's Properties File ──────────
    // R2-D2 extends his probe arm into the archive socket aboard Home One,
    // pulls out the mission data file, and caches it in memory for the rest
    // of the mission so nobody has to read it from disk again.
    // getPluginProperties() does the same: it lazy-loads plugin.properties from
    // our bundle's JAR and caches the result in the {@code properties} field.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's property bundle, loading it from the JAR on first call.
     * The properties file lives at the root of our bundle as {@code plugin.properties}
     * and holds metadata like the plugin version that we want to display in the UI.
     * We cache the loaded bundle so we only hit the file system once.
     *
     * <p>For example — R2-D2 retrieves the mission data file:</p>
     * <pre>
     *   if (properties == null) {
     *     open stream to "plugin.properties" inside our bundle JAR
     *     parse it into a PropertyResourceBundle
     *     cache in this.properties
     *   }
     *   return properties  // same object on every subsequent call
     * </pre>
     *
     * @return  the loaded {@link PropertyResourceBundle}, or {@code null} if the
     *          file could not be read (an error is logged in that case).
     */
    public PropertyResourceBundle getPluginProperties()
    {
        if ( properties == null )
        {
            try
            {
                properties = new PropertyResourceBundle( FileLocator.openStream( this.getBundle(), new Path(
                    "plugin.properties" ), false ) ); //$NON-NLS-1$
            }
            catch ( IOException e )
            {
                // We can't use the PLUGIN_ID constant since loading the plugin.properties file has failed,
                // So we're using a default plugin id.
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.rcp", Status.OK, //$NON-NLS-1$
                    "Unable to get the plugin properties.", e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
