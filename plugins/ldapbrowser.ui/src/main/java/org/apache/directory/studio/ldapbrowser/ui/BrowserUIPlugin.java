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

package org.apache.directory.studio.ldapbrowser.ui;


import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.apache.directory.studio.entryeditors.EntryEditorManager;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: BrowserUIPlugin — Han Solo Jumps to Hyperspace ────────────────────
// Han slams the hyperdrive lever forward on the Millennium Falcon: systems spin
// up, life support engages, the navicomputer locks in coordinates, and in a flash
// of blue light the whole ship is somewhere completely different.
// BrowserUIPlugin is that moment — the OSGi activator that fires up everything
// the Browser UI plugin needs (images, the entry editor manager, properties) and
// tears it all down cleanly when the plugin is stopped.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi activator (main plugin class) for the Browser UI plugin.
 * It boots the plugin's shared services on startup and shuts them down on stop,
 * and acts as the single static access point for images, properties, and managers.
 * Think of this class as Han Solo at the Falcon's controls — it's the one that
 * makes the whole ship jump to hyperspace and brings it safely out the other side.
 */
public class BrowserUIPlugin extends AbstractUIPlugin
{
    /** The shared instance */
    private static BrowserUIPlugin plugin;

    /** The entry editor manager */
    private EntryEditorManager entryEditorManager;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── Han Grabs the Controls ────────────────────────────────────────────────
    // Han drops into the pilot's seat of the Millennium Falcon, takes the
    // controls, and makes himself the one everyone calls when they need the ship.
    // This constructor sets our singleton reference so the rest of the plugin
    // can call {@code getDefault()} and always get back this one instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the plugin and registers this instance as the shared singleton.
     * Eclipse's OSGi framework calls this exactly once when the bundle is first
     * activated; we just need to store the reference for later retrieval.
     */
    public BrowserUIPlugin()
    {
        plugin = this;
    }


    // ── Punch It, Chewie — Hyperdrive Engaged ─────────────────────────────────
    // The Falcon's systems power up in sequence: shields, engines, navicomputer.
    // Han doesn't leave port without everything ready.
    // We do the same here — the entry editor manager is created and ready before
    // any part of the UI tries to open an entry editor panel.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when the bundle activates — our plugin's equivalent of
     * punching the hyperdrive. We initialise the {@link EntryEditorManager} here
     * so it's available the moment the UI starts rendering entry editors.
     *
     * @param context  the OSGi bundle context, passed to the superclass to do
     *                 all the standard Eclipse plugin startup housekeeping
     * @throws Exception if the superclass startup fails for any reason
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        if ( entryEditorManager == null )
        {
            entryEditorManager = new EntryEditorManager();
        }
    }


    // ── Falcon Powers Down After the Jump ─────────────────────────────────────
    // Coming out of hyperspace, Han cuts the engines and powers down non-essential
    // systems — you don't leave the hyperdrive spinning when you land.
    // We mirror that here: dispose the entry editor manager and clear the singleton
    // so nothing holds a stale reference after the plugin unloads.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when the bundle is being stopped — the orderly shutdown of
     * our plugin. We dispose the entry editor manager (releasing any listeners or
     * resources it holds) and null out the singleton so nobody accidentally uses
     * a dead plugin instance.
     *
     * @param context  the OSGi bundle context; forwarded to the superclass
     * @throws Exception if the superclass shutdown fails
     */
    public void stop( BundleContext context ) throws Exception
    {
        super.stop( context );

        if ( entryEditorManager != null )
        {
            entryEditorManager.dispose();
            entryEditorManager = null;
        }

        plugin = null;
    }


    // ── The Falcon Is Always Docked Right Here ────────────────────────────────
    // No matter where you are in the galaxy, you know exactly where to find Han's
    // ship — it's the one with the dented hull in Bay 94.
    // {@code getDefault()} is that Bay 94: the single well-known address for our
    // plugin instance that every other class calls to reach shared services.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared singleton instance of this plugin.
     * Every class in the plugin that needs an image, a property, or the entry
     * editor manager goes through this method — it's our central dispatch point.
     *
     * @return the singleton {@code BrowserUIPlugin} instance, or {@code null}
     *         if the plugin hasn't started yet (shouldn't normally happen in practice)
     */
    public static BrowserUIPlugin getDefault()
    {
        return plugin;
    }


    //    public static String getResourceString( String key )
    //    {
    //        ResourceBundle bundle = getDefault().getResourceBundle();
    //        try
    //        {
    //            return ( bundle != null ) ? bundle.getString( key ) : key;
    //        }
    //        catch ( MissingResourceException e )
    //        {
    //            return key;
    //        }
    //    }

    // ── Han Pulls Up the Star Map ─────────────────────────────────────────────
    // Han needs coordinates — he consults the navicomputer, which knows the path
    // to every location in the galaxy as a vector, not a rendered image.
    // An {@code ImageDescriptor} is exactly that: a lightweight recipe for
    // creating a real SWT {@code Image} later, without actually allocating it yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up an {@code ImageDescriptor} for the given resource path key.
     * Use the {@code IMG_*} constants from {@link BrowserUIConstants} as keys.
     * A descriptor is a lazy handle — it describes how to create the image without
     * allocating the SWT resource yet, which is useful when you need to pass images
     * to JFace contributions before the UI is fully up.
     *
     * @param key  the relative path to the image file within the plugin bundle,
     *             e.g. {@code "resources/icons/entry_default.gif"}
     * @return an {@code ImageDescriptor} for the image, or {@code null} if the
     *         path can't be resolved inside the bundle
     * @see BrowserUIConstants
     */
    public ImageDescriptor getImageDescriptor( String key )
    {
        if ( key != null )
        {
            URL url = this.find( new Path( key ) );
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


    // ── Han Fires Up the Real Engines ─────────────────────────────────────────
    // The star map gave coordinates; now Han actually powers the sublight engines
    // and the Falcon moves for real. The first flight to a new destination takes
    // a moment; after that, the navicomputer has it cached.
    // {@code getImage} works the same way: first call creates the SWT Image and
    // caches it in Eclipse's ImageRegistry; every call after is instant.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a fully allocated SWT {@code Image} for the given resource path key.
     * Use the {@code IMG_*} constants from {@link BrowserUIConstants} as the key.
     * The image is cached in the plugin's {@code ImageRegistry} after the first
     * load, so repeated calls are cheap. Do NOT dispose the returned image — the
     * registry owns it and will dispose it automatically when the plugin stops.
     *
     * @param key  the relative path to the image file, e.g.
     *             {@code "resources/icons/search.gif"}
     * @return the cached SWT {@code Image}, or {@code null} if the path can't
     *         be resolved or the descriptor can't produce an image
     * @see BrowserUIConstants
     */
    public Image getImage( String key )
    {
        Image image = getImageRegistry().get( key );
        if ( image == null )
        {
            ImageDescriptor id = this.getImageDescriptor( key );
            if ( id != null )
            {
                image = id.createImage();
                getImageRegistry().put( key, image );
            }
        }
        return image;
    }


    // ── Han Hands Off the Crew Manifest ──────────────────────────────────────
    // Every ship needs a crew chief — someone who knows which passengers need
    // which berths. The entry editor manager is that crew chief for editors.
    // We hand it off here so callers can ask it "which editor opens this entry?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EntryEditorManager} that handles which entry editor
     * extension should be used for any given LDAP entry.
     * It's created during {@link #start(BundleContext)} and disposed during
     * {@link #stop(BundleContext)}, so it's always valid while the plugin is alive.
     *
     * @return the shared {@code EntryEditorManager} instance
     */
    public EntryEditorManager getEntryEditorManager()
    {
        return entryEditorManager;
    }


    // ── Han Reads the Falcon's Flight Manual ──────────────────────────────────
    // The Millennium Falcon has a battered properties binder in the cockpit — IDs,
    // frequencies, docking codes — that Han consults when he needs an exact value.
    // We load our {@code plugin.properties} file lazily the same way: only when
    // something actually needs a value do we open the file and parse it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's {@code plugin.properties} file as a
     * {@code PropertyResourceBundle}, loading it lazily on first access.
     * This file contains all the plug-in contribution IDs (editor IDs, view IDs,
     * preference page IDs, etc.) that we expose as constants in
     * {@link BrowserUIConstants}. If the file can't be read, we log an error and
     * return {@code null} — callers should handle that defensively.
     *
     * @return the {@code PropertyResourceBundle} from {@code plugin.properties},
     *         or {@code null} if the file couldn't be opened
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.ldapbrowser.ui", Status.OK, //$NON-NLS-1$
                    Messages.getString( "BrowserUIPlugin.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
