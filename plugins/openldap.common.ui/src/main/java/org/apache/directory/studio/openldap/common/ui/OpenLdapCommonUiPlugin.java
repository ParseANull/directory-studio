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
package org.apache.directory.studio.openldap.common.ui;


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


// ── CLASS: OpenLdapCommonUiPlugin — REBEL BASE POWERING UP ───────────────────
// This is the moment the generators rumble to life at Echo Base and every
// console lights up: we initialize the shared plugin instance, load the image
// registry, and stand ready to serve requests from every corner of the UI.
// When the base is evacuated (stop), we cut power cleanly so nothing leaks.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We are the OSGi activator for the openldap.common.ui plugin — think of us as
 * the power plant for Echo Base. We start when Eclipse loads the plugin, hold
 * the single shared instance that every other class can reach via
 * {@link #getDefault()}, and provide centralized access to images and plugin
 * properties. We also shut down cleanly so resources are never leaked.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapCommonUiPlugin extends AbstractUIPlugin
{
    /** The shared instance */
    private static OpenLdapCommonUiPlugin plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── CONSTRUCTOR: OpenLdapCommonUiPlugin — FLIPPING THE MAIN SWITCH ───────
    // The moment the constructor runs is the moment we set ourselves as the
    // single shared instance — like the base commander taking the command chair
    // the instant they step onto the bridge. OSGi calls this exactly once.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the plugin instance and register ourselves as the shared
     * singleton. OSGi guarantees this constructor is called exactly once during
     * the plugin lifecycle.
     */
    public OpenLdapCommonUiPlugin()
    {
        plugin = this;
    }


    // ── METHOD: start — POWERING UP THE REBEL BASE ───────────────────────────
    // All systems come online in sequence as the base wakes from standby.
    // We delegate to the parent activator first, which handles the Eclipse
    // framework plumbing, before any plugin-specific startup would go here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
    }


    // ── METHOD: stop — EVACUATING ECHO BASE ──────────────────────────────────
    // The order to abandon base has come in: we null out the shared reference
    // so the garbage collector can reclaim memory, then let the parent activator
    // finish the teardown sequence. After this, no one should call getDefault().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── METHOD: getDefault — REACHING THE COMMAND CENTER ─────────────────────
    // Any officer on the base who needs to contact command just calls this —
    // it hands back the one-and-only shared plugin instance so callers don't
    // need to track the reference themselves.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the single shared plugin instance so any class in the plugin
     * can reach plugin services without having to hold their own reference.
     *
     * @return the shared plugin instance
     */
    public static OpenLdapCommonUiPlugin getDefault()
    {
        return plugin;
    }


    // ── METHOD: getImageDescriptor — PULLING A HOLOGRAM FROM THE ARCHIVE ─────
    // The hologram archive is indexed by file path; if the path key exists we
    // build a descriptor from the bundle resource URL. If the key is null or
    // the URL can't be found we return null rather than throwing, keeping callers
    // safe even when an image happens to be missing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We resolve an image key (relative filesystem path) to an
     * {@link ImageDescriptor} by looking it up in the plugin bundle. Use the
     * {@code IMG_} constants from {@link OpenLdapCommonUiConstants} as the key.
     * We return {@code null} rather than throwing if the key is null or the
     * resource cannot be located.
     *
     * @param key  the key (relative path to the image in the filesystem)
     * @return     the image descriptor, or {@code null} if not found
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


    // ── METHOD: getImage — FETCHING A HOLOGRAM FROM THE REGISTRY ─────────────
    // The image registry is our pre-loaded hologram library — if the image for
    // a given key is already there we hand it back immediately. If it hasn't
    // been loaded yet we create it from the descriptor and cache it so the next
    // caller gets an instant hit. Never dispose the returned image yourself —
    // the registry owns it and destroys it when the base shuts down.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up an SWT {@link Image} by key, creating and caching it on first
     * access via the plugin's image registry. Use the {@code IMG_} constants
     * from {@link OpenLdapCommonUiConstants} as the key.
     * <p>
     * Note: Don't dispose the returned SWT Image. It is disposed
     * automatically when the plugin is stopped.
     *
     * @param key  the key (relative path to the image on filesystem)
     * @return     the SWT Image, or {@code null} if the descriptor cannot be resolved
     * @see OpenLdapCommonUiConstants
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


    // ── METHOD: getPluginProperties — READING THE BASE MANIFEST ──────────────
    // Every base keeps a manifest listing its capabilities and configuration.
    // We load it lazily — the first call triggers the read from disk; after that
    // it's cached in memory. If the file can't be found we log the failure and
    // return null so callers can handle a missing manifest gracefully.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the plugin's {@code plugin.properties} resource bundle, loading
     * it lazily on first call and caching the result for all subsequent calls.
     * If loading fails we log an error to the Eclipse log and return {@code null}.
     *
     * @return the plugin properties bundle, or {@code null} if loading failed
     */
    public PropertyResourceBundle getPluginProperties()
    {
        if ( properties == null )
        {
            try
            {
                properties = new PropertyResourceBundle( FileLocator.openStream( getBundle(), new Path(
                    "plugin.properties" ), false ) ); //$NON-NLS-1$
            }
            catch ( IOException e )
            {
                // We can't use the PLUGIN_ID constant since loading the plugin.properties file has failed,
                // So we're using a default plugin id.
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.openldap.common.ui", Status.OK, //$NON-NLS-1$
                    Messages.getString( "OpenLdapCommonUiPlugin.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
