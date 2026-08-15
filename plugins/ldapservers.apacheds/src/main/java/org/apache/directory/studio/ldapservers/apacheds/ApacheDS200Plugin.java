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


// ── CLASS: ApacheDS200Plugin — The Millennium Falcon's Main Control Panel ─────
// The Millennium Falcon's main control panel comes alive when the crew boards
// and goes dark when they leave.  The panel holds the shared systems everyone
// needs: the image registry (for toolbar icons), the plugin properties file
// (for version strings and configuration), and the singleton reference so any
// code anywhere in the ship can reach it with getDefault().
// ApacheDS200Plugin is that control panel: the OSGi activator for the
// ldapservers.apacheds plugin.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator for the ApacheDS 2.0.0 server plugin.
 * Controls the plug-in life cycle: delegates to {@link AbstractUIPlugin} for
 * start/stop, holds the shared singleton, and provides access to images and
 * plugin properties.
 * Think of this as the Millennium Falcon's main control panel: it comes online
 * when the bundle activates and provides all shared resources to the crew.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApacheDS200Plugin extends AbstractUIPlugin
{
    /** The shared plugin instance. */
    private static ApacheDS200Plugin plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── Falcon Powers Up — Store the Singleton Instance ───────────────────────
    // OSGi calls this constructor at activation time.  We store 'this' in the
    // static field immediately so getDefault() works for all later callers.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the plugin instance and registers it as the shared singleton.
     * Called once by OSGi when the bundle is activated.
     */
    public ApacheDS200Plugin()
    {
        plugin = this;
    }


    // ── Crew Boards and the Panel Comes Alive ─────────────────────────────────
    // Delegates to AbstractUIPlugin which sets up the image registry and other
    // Eclipse plugin housekeeping.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Starts the plugin.  Delegates to {@link AbstractUIPlugin#start(BundleContext)}.
     *
     * @param context  the OSGi bundle context.
     * @throws Exception  if the super start() throws.
     * @see AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
    }


    // ── Crew Leaves and the Panel Goes Dark ───────────────────────────────────
    // We null the singleton reference and delegate to AbstractUIPlugin.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stops the plugin.  Nulls the singleton reference and delegates to
     * {@link AbstractUIPlugin#stop(BundleContext)}.
     *
     * @param context  the OSGi bundle context.
     * @throws Exception  if the super stop() throws.
     * @see AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── Get a Reference to the Control Panel ──────────────────────────────────
    // Any crew member anywhere in the Falcon can call getDefault() to reach the
    // shared plugin instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared plugin instance.
     *
     * @return  the singleton instance, or {@code null} before activation or
     *          after deactivation.
     */
    public static ApacheDS200Plugin getDefault()
    {
        return plugin;
    }


    // ── Fetch an Image Descriptor from the Ship's Locker ──────────────────────
    // Looks up an image file inside the plugin bundle by relative path.
    // Returns null rather than throwing if the file doesn't exist, so callers
    // can decide how to handle a missing icon gracefully.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link ImageDescriptor} for the image at the given bundle-relative path.
     * Use the {@code IMG_} constants from BrowserWidgetsConstants for the key.
     *
     * @param key  the relative path to the image file inside the bundle.
     * @return     the image descriptor, or {@code null} if the path is null or not found.
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


    // ── Get a Cached SWT Image from the Ship's Locker ─────────────────────────
    // Uses the JFace ImageRegistry to avoid recreating SWT images on every call.
    // Don't dispose the returned image — it's owned by the registry and will be
    // disposed automatically when the plugin stops.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the given bundle-relative path.
     * The image is cached in the plugin's {@link org.eclipse.jface.resource.ImageRegistry}
     * so subsequent calls are cheap.
     * Use the {@code IMG_} constants from BrowserWidgetsConstants for the key.
     *
     * <p>Note: Don't dispose the returned SWT Image — it is disposed automatically
     * when the plugin is stopped.</p>
     *
     * @param key  the relative path to the image file inside the bundle.
     * @return     the SWT image, or {@code null} if not found.
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


    // ── Read the Falcon's Technical Specifications File ───────────────────────
    // The plugin.properties file contains version strings and other metadata
    // we need at runtime.  We load it lazily and cache it; if loading fails
    // we log an error but don't crash — the rest of the plugin still works.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin properties loaded from {@code plugin.properties}.
     * Loaded lazily on first call and cached.  Logs an error if loading fails
     * but does not throw — callers receive {@code null} in that case.
     *
     * @return  the plugin properties, or {@code null} if loading failed.
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
                getLog().log(
                    new Status( Status.ERROR, "org.apache.directory.studio.ldapservers.apacheds", Status.OK, //$NON-NLS-1$
                        Messages.getString( "ApacheDS200Plugin.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
