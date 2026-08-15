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
package org.apache.directory.studio.combinededitor;


import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: CombinedEditorPlugin — R2-D2 Powers Up in the Rebel Hangar ────────
// In the Rebel hangar on Yavin IV, R2-D2 beeps to life when the techs connect
// him to the base power grid — all his subsystems come online in the right order,
// and everyone in the hangar can reach him via a single well-known socket.
// CombinedEditorPlugin is that power-up sequence for the combinededitor bundle:
// OSGi calls start() and stop() to bring the plugin in and out of service,
// and getDefault() is the well-known socket every other class uses to reach it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator for the combinededitor plugin.
 * Eclipse's runtime calls {@link #start} when the bundle loads and {@link #stop}
 * when it unloads.  We also provide helpers for loading SWT images by key so
 * the rest of the plugin doesn't need to know how the image registry works.
 * Think of this class as R2-D2 powering up in the Rebel hangar — the one
 * shared instance that every other class reaches for.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CombinedEditorPlugin extends AbstractUIPlugin
{
    /** The shared instance */
    private static CombinedEditorPlugin plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── R2 Boots His Core Systems Before Any Mission Begins ──────────────────
    // R2-D2 starts a self-test sequence the moment power is applied — everything
    // is initialised, but the shared-instance pointer isn't set yet (that happens
    // in start() after the parent has finished its own initialisation).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Default no-arg constructor required by the OSGi framework.
     * The actual initialisation happens in {@link #start(BundleContext)} once
     * the parent class has set up the image registry and preference store.
     */
    public CombinedEditorPlugin()
    {
    }


    // ── R2 Announces Himself on All Channels — "I'm Online!" ──────────────────
    // After the parent's power-on sequence completes, R2 broadcasts his status
    // on all Rebel frequencies and registers himself as the active droid unit.
    // start() mirrors that: we call the parent first (so the image registry is
    // ready), then store ourselves as the shared plugin instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when our bundle activates — the plugin's "power on" moment.
     * We let the parent {@link AbstractUIPlugin} do its setup (image registry,
     * preference store), then register ourselves as the shared instance so the
     * rest of the codebase can reach us via {@link #getDefault()}.
     *
     * @param context  the OSGi bundle context — passed to the parent.
     * @throws Exception  if the parent's startup fails.
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
        plugin = this;
    }


    // ── R2 Powers Down and Clears His Registry Entry ─────────────────────────
    // When the Rebel techs disconnect R2 from the power grid he signs off all
    // channels and clears his registration so nobody tries to reach a dead socket.
    // stop() nulls the shared reference before calling the parent shutdown so
    // any late callers get null rather than a partially-disposed object.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when our bundle deactivates — the plugin's "power off" moment.
     * We null the shared reference first so nothing can reach a half-disposed
     * plugin, then delegate to the parent to clean up the image registry.
     *
     * @param context  the OSGi bundle context — passed to the parent.
     * @throws Exception  if the parent's shutdown fails.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── Any Rebel Hails R2 — One Shared Socket ────────────────────────────────
    // Any pilot in the Rebel fleet can ping R2's known address and get a direct
    // connection to the active droid unit — one shared point of authority.
    // getDefault() is that shared socket for the combinededitor plugin.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared plugin instance.
     * Eclipse plugins are singletons; this static accessor is the standard way
     * to reach the plugin's image registry, preference store, and other services.
     *
     * @return  the live {@link CombinedEditorPlugin} instance, or {@code null}
     *          if the bundle hasn't started or has already stopped.
     */
    public static CombinedEditorPlugin getDefault()
    {
        return plugin;
    }


    // ── R2 Extends His Probe — Retrieving an Image by Path ───────────────────
    // R2-D2 extends one of his probe arms into the holographic archive and
    // retrieves the image file at the given path, handing back a ready-to-use
    // descriptor without actually loading the pixel data yet.
    // getImageDescriptor() does the same: it returns a lazy ImageDescriptor
    // that Eclipse can use in menus and toolbars without hitting the disk early.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link ImageDescriptor} for the image at the given bundle-relative path.
     * Use the {@code IMG_} constants from {@link CombinedEditorPluginConstants} as keys.
     * Returns {@code null} if the path doesn't resolve inside the bundle.
     *
     * @param key  the bundle-relative path to the image file.
     * @return     an {@link ImageDescriptor}, or {@code null} if not found.
     */
    public ImageDescriptor getImageDescriptor( String key )
    {
        if ( key != null )
        {
            URL url = FileLocator.find( getBundle(), new Path( key ), null );
            if ( url != null )
                return ImageDescriptor.createFromURL( url );
            else
                return null;
        }
        else
        {
            return null;
        }
    }


    // ── R2 Retrieves the Image from His Cache — Or Loads It Fresh ────────────
    // R2 first checks his local memory cache for the image; if it's there he
    // hands it back immediately.  If not he loads it from the archive, caches
    // it, and then hands it back — same image object from then on.
    // getImage() does the same via Eclipse's ImageRegistry so the SWT Image
    // is shared and only disposed when the plugin stops.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the given bundle-relative path, loading it on demand.
     * Images are cached in the plugin's {@link org.eclipse.jface.resource.ImageRegistry}
     * and disposed automatically when the plugin stops — do NOT call {@code image.dispose()}
     * on the returned image yourself or you'll break every other caller.
     *
     * <p>For example — R2 checks his image cache:</p>
     * <pre>
     *   image = registry.get(key)
     *   if (image == null) {
     *     descriptor = getImageDescriptor(key)
     *     image = descriptor.createImage()
     *     registry.put(key, image)
     *   }
     *   return image
     * </pre>
     *
     * @param key  the bundle-relative path to the image file.
     * @return     the cached (or freshly loaded) SWT {@link Image}, or {@code null}
     *             if the path doesn't resolve.
     */
    public Image getImage( String key )
    {
        Image image = getImageRegistry().get( key );
        if ( image == null )
        {
            ImageDescriptor id = getImageDescriptor( key );
            if ( id != null )
            {
                image = id.createImage();
                getImageRegistry().put( key, image );
            }
        }
        return image;
    }


    // ── R2 Opens the Bundle's Mission Data File ───────────────────────────────
    // R2-D2 plugs into the archive socket and reads the bundle's plugin.properties
    // file, caching it so he doesn't have to hit the archive on every request.
    // getPluginProperties() does the same: lazy-load and cache the bundle's
    // property file so we can display things like the plugin version in the UI.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's property bundle, loading it from the JAR on first call.
     * The file {@code plugin.properties} is at the bundle root and holds metadata
     * like the plugin version.  We cache it after the first load.
     *
     * @return  the {@link PropertyResourceBundle}, or {@code null} if loading fails
     *          (an error is logged in that case).
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.combinededitor", Status.OK, //$NON-NLS-1$
                    Messages.getString( "CombinedEditorPlugin.UnableToGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
