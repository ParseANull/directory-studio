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


// ── CLASS: LdapServersPlugin — THE MILLENNIUM FALCON'S STARTUP SEQUENCE ──────────────────────
// The Millennium Falcon's pre-flight sequence: Han hits the ignition, the sub-light engines
// spool up in a particular order — navi-computer first, then life support, then shields —
// and on shutdown everything powers down just as carefully to avoid a coolant disaster.
// Eclipse calls {@link #start} when the plugin loads (navi-computer, life support) and
// {@link #stop} when the workbench closes (safe shutdown, state saved to disk).
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator (Eclipse "plugin") for the {@code ldapservers} bundle.
 * Controls the plugin lifecycle: on {@link #start} we load adapter extensions and restore
 * the server list from disk; on {@link #stop} we persist the server list before going dark.
 * Think of this class as the Falcon's ignition panel — it wires up the whole ship in the
 * right sequence and shuts it down cleanly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServersPlugin extends AbstractUIPlugin
{
    /** The shared plugin instance. */
    private static LdapServersPlugin plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;

    /** The LDAP Server Adapter Extensions Manager */
    private LdapServerAdapterExtensionsManager ldapServerAdapterExtensionsManager = LdapServerAdapterExtensionsManager
        .getDefault();

    /** The LDAP Servers Manager */
    private LdapServersManager ldapServersManager = LdapServersManager.getDefault();


    // ── Han Hits The Ignition Switch ────────────────────────────────────────────────────────
    // Han flips the main switch — the Falcon shudders to life and the shared instance pointer
    // is set so anyone asking "where's the ship?" gets the right answer.
    // We record {@code this} as the singleton so {@link #getDefault()} works from this point on.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the OSGi framework when the bundle class is first instantiated.
     * We store {@code this} in the static {@code plugin} field so other classes can reach
     * the plugin singleton via {@link #getDefault()} without going through OSGi APIs.
     *
     * <p>For example — Han initializes the Falcon:</p>
     * <pre>
     *   new LdapServersPlugin() → plugin = this.
     *   Now: LdapServersPlugin.getDefault() returns the live ship instance.
     * </pre>
     */
    public LdapServersPlugin()
    {
        plugin = this;
    }


    // ── Spooling Up The Navi-Computer And Life Support ──────────────────────────────────────
    // Chewie runs through the startup checklist — first the navi-computer (adapter extensions
    // so we know which servers the plugin can manage), then life support (load the server list
    // from disk so the Servers view is populated).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the ldapservers bundle is activated.
     * We first load all server adapter extensions from Eclipse's extension registry, then
     * restore the previously saved server list from disk.
     * The adapter extensions must be loaded before the servers so that the server-deserialization
     * code can link each server to its adapter by ID.
     *
     * @param context  the OSGi bundle context — passed to the super implementation
     * @throws Exception  if anything in the startup chain throws (Eclipse will log and handle it)
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        // Loading the LDAP Server Adapters extensions
        ldapServerAdapterExtensionsManager.loadLdapServerAdapterExtensions();

        // Loading the servers to the LDAP Servers Manager
        ldapServersManager.loadServersFromStore();
    }


    // ── Powering Down The Falcon Safely ─────────────────────────────────────────────────────
    // Before the Falcon goes dark, Han saves the hyperspace coordinates — the navi-computer
    // writes them to the ship's memory so the next startup can restore the route.
    // We save the current server list to disk so nothing is lost when the workbench closes.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the ldapservers bundle is being shut down.
     * We persist the current server list before going dark so the next startup finds everything
     * intact. Then we clear the static reference and call {@code super.stop()} to release
     * OSGi resources.
     *
     * @param context  the OSGi bundle context — passed to the super implementation
     * @throws Exception  if anything in the shutdown chain throws
     */
    public void stop( BundleContext context ) throws Exception
    {
        // Loading the servers to the LDAP Servers Manager
        ldapServersManager.saveServersToStore();

        plugin = null;
        super.stop( context );
    }


    // ── Asking "Where's The Falcon?" ────────────────────────────────────────────────────────
    // Every crew member who needs the ship just calls out: "Where's the Falcon?" and Han
    // points them to the same docked ship — there's only one.
    // The singleton accessor used everywhere in the plugin to reach plugin services.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton plugin instance.
     * Other classes call this to reach the image registry, plugin properties, and other
     * services the plugin provides.
     *
     * @return the shared {@link LdapServersPlugin} instance
     */
    public static LdapServersPlugin getDefault()
    {
        return plugin;
    }


    // ── Loading A Navigation Chart From The Bundle ──────────────────────────────────────────
    // The Falcon carries star charts as image files in its data core — R2-D2 looks one up
    // by path and hands back a descriptor so the display can render it.
    // We resolve the image path relative to this bundle's resources.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link ImageDescriptor} for the image at the given bundle-relative path.
     * Use the {@code IMG_*} constants from {@link LdapServersPluginConstants} as keys.
     * Returns {@code null} if the path doesn't resolve to anything in the bundle.
     *
     * <p>For example — R2-D2 looks up a star chart:</p>
     * <pre>
     *   key = "resources/icons/server_started.gif"
     *   → ImageDescriptor pointing at the started-server icon in our bundle.
     * </pre>
     *
     * @param key  the bundle-relative path to the image file
     * @return the {@link ImageDescriptor}, or {@code null} if not found or key is null
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


    // ── Retrieving A Cached Star Chart ──────────────────────────────────────────────────────
    // Once R2-D2 has rendered a star chart for display, the Falcon caches it so we don't
    // re-render the same chart on every repaint — the image registry is that cache.
    // We look up in the registry first, create from descriptor if absent, then cache it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the given bundle-relative path, using an image registry
     * as a cache.
     * The first call for a given key creates the image and registers it; subsequent calls return
     * the cached copy. Do NOT dispose the returned image — it is owned by the registry and
     * disposed automatically when the plugin stops.
     *
     * @param key  the bundle-relative path (use {@code IMG_*} constants from
     *             {@link LdapServersPluginConstants})
     * @return the cached or newly created {@link Image}, or {@code null} if the path doesn't resolve
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


    // ── Consulting The Ship's Technical Manual ───────────────────────────────────────────────
    // The Falcon's technical manual (plugin.properties) contains command IDs and other
    // configuration strings that are too dynamic to hard-code.  Chewie loads it on demand.
    // We lazily load the PropertyResourceBundle from plugin.properties.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's {@link PropertyResourceBundle} loaded from {@code plugin.properties}.
     * Used by {@link LdapServersPluginConstants} to look up command IDs and other string constants
     * that are contributed to Eclipse as extension point properties.
     * The bundle is loaded lazily and cached; if loading fails, an error is logged.
     *
     * @return the {@link PropertyResourceBundle}, or {@code null} if the file could not be read
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.ldapservers", Status.OK, //$NON-NLS-1$
                    Messages.getString( "LdapServersPlugin.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
