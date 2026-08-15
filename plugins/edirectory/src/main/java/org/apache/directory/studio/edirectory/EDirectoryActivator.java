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
package org.apache.directory.studio.edirectory;


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


// ── CLASS: EDirectoryActivator — The Rebel Spy Network's Eclipse Plugin Panel ─
// The Alliance has a forward intelligence cell monitoring a Novell eDirectory
// server.  When the cell opens for business (bundle start) the control panel
// comes online, registers the shared singleton, and is ready to hand out
// images and plugin properties.  When the Alliance withdraws (bundle stop) the
// panel goes dark and the singleton is cleared.
// EDirectoryActivator is that control panel: the OSGi AbstractUIPlugin activator
// for the eDirectory support plugin, structurally identical to other Studio
// plugin activators.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator for the eDirectory plugin.
 * Controls the plugin life cycle (start/stop), holds the shared singleton,
 * and provides access to SWT images and plugin properties.
 * Think of this as the Rebel spy network's Eclipse control panel for Novell
 * eDirectory support: it powers up when needed and powers down cleanly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EDirectoryActivator extends AbstractUIPlugin
{
    /** The shared instance */
    private static EDirectoryActivator plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── Control Panel Powers Up — Store Singleton Reference ───────────────────
    // OSGi calls this constructor at activation time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the plugin instance and registers it as the shared singleton.
     * Called once by OSGi when the bundle is activated.
     */
    public EDirectoryActivator()
    {
        plugin = this;
    }


    // ── Cell Opens for Business ───────────────────────────────────────────────
    // Delegates to AbstractUIPlugin which sets up the image registry and other
    // Eclipse plugin housekeeping.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Starts the plugin.
     *
     * @param context  the OSGi bundle context.
     * @throws Exception  if the super start() throws.
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
    }


    // ── Alliance Withdraws — Clear the Singleton ──────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stops the plugin — nulls the singleton and delegates to
     * {@link AbstractUIPlugin#stop(BundleContext)}.
     *
     * @param context  the OSGi bundle context.
     * @throws Exception  if the super stop() throws.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── Get a Reference to the Control Panel ──────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared plugin instance.
     *
     * @return  the singleton, or {@code null} before activation or after deactivation.
     */
    public static EDirectoryActivator getDefault()
    {
        return plugin;
    }


    // ── Fetch an Image Descriptor from the Plugin Bundle ──────────────────────
    // Use the IMG_ constants from ValueEditorConstants for the key.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link ImageDescriptor} for the image at the given bundle-relative path.
     * Returns {@code null} if the key is {@code null} or the image is not found.
     *
     * @param key  the relative path to the image inside the bundle.
     * @return     the image descriptor, or {@code null}.
     */
    public ImageDescriptor getImageDescriptor( String key )
    {
        if ( key != null )
        {
            URL url = FileLocator.find( getBundle(), new Path( key ), null );

            if ( url != null )
            {
                return ImageDescriptor.createFromURL( url );
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }


    // ── Get a Cached SWT Image from the Registry ──────────────────────────────
    // Don't dispose the returned image — it's owned by the registry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the given bundle-relative path.
     * Caches images in the plugin's image registry.
     * Use the {@code IMG_} constants from ValueEditorConstants for the key.
     *
     * <p>Note: Don't dispose the returned SWT Image — it is disposed automatically
     * when the plugin is stopped.</p>
     *
     * @param key  the relative path to the image inside the bundle.
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


    // ── Read the Plugin's Technical Specifications File ───────────────────────
    // Loaded lazily and cached; logs an error if loading fails.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin properties loaded from {@code plugin.properties}.
     * Loaded lazily on first call and cached.  Logs an error if loading fails.
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.edirectory", Status.OK, //$NON-NLS-1$
                    Messages.getString( "EDirectoryActivator.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
