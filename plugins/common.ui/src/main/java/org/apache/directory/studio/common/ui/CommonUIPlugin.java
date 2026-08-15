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

package org.apache.directory.studio.common.ui;


import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: CommonUIPlugin — REBEL BASE POWERING UP AT YAVIN IV ──────────────
// This is the Rebel base coming to life: generators spinning up, comm systems
// going online, and the color registry warming up its displays.  As the OSGi
// activator for the common.ui plugin, we manage the plugin lifecycle from the
// moment the first X-wing rolls into the hangar until the last light blinks
// out.  Every other plugin in Directory Studio depends on us being up first.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We are the activator that controls the common.ui plug-in's lifecycle.  We
 * start up the color registry, load images on demand, and give the rest of the
 * application a single shared instance to call into.  Think of us as the base
 * commander who keeps the lights on for everyone else.
 */
public class CommonUIPlugin extends AbstractUIPlugin
{
    /** The shared plugin instance. */
    private static CommonUIPlugin plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;

    /** The color registry */
    private ColorRegistry colorRegistry;

    // ── CONSTRUCTOR CommonUIPlugin — RAISING THE REBEL FLAG ─────────────────
    // The moment the OSGi framework instantiates us, we register ourselves as
    // the shared instance.  Every other part of the plugin will call
    // getDefault() to reach us, so we make sure we are findable right away.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We register ourselves as the shared plugin instance the moment OSGi
     * creates us.  This is a standard Eclipse activator pattern — do not call
     * this constructor directly.
     */
    public CommonUIPlugin()
    {
        plugin = this;
    }


    // ── METHOD start — SPINNING UP THE REBEL BASE GENERATORS ────────────────
    // The generators roar to life at Yavin IV: we initialize the color registry
    // against the workbench display so every subsequent color lookup has a home.
    // Without this, the base stays dark and nobody can see the battle plans.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We start the plugin by calling the parent {@code start} and then
     * initializing our color registry against the current workbench display.
     * This is called once by OSGi when the bundle activates.
     *
     * @param context the OSGi bundle context
     * @throws Exception if startup fails for any reason
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        if ( colorRegistry == null )
        {
            colorRegistry = new ColorRegistry( PlatformUI.getWorkbench().getDisplay() );
        }
    }


    // ── METHOD stop — POWERING DOWN THE REBEL BASE ───────────────────────────
    // The battle is over: we null out our shared reference and release the color
    // registry so the JVM can reclaim memory.  After this, getDefault() will
    // return null and no further operations should be attempted on us.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We shut down the plugin by clearing the shared instance reference and
     * releasing the color registry before delegating to the parent's stop.
     * OSGi calls this once when the bundle deactivates.
     *
     * @param context the OSGi bundle context
     * @throws Exception if shutdown fails for any reason
     * @see AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;

        if ( colorRegistry != null )
        {
            colorRegistry = null;
        }

        super.stop( context );
    }


    // ── METHOD getDefault — LOCATING REBEL HEADQUARTERS ─────────────────────
    // When any X-wing pilot needs orders, they call into Rebel HQ.  We return
    // the single shared plugin instance so callers can access images, colors,
    // and preferences without needing to know our internals.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return the single shared instance of this plugin, which is the entry
     * point for all image, color, and preference lookups.  Returns {@code null}
     * if the plugin has not started yet or has already been stopped.
     *
     * @return the shared plugin instance
     */
    public static CommonUIPlugin getDefault()
    {
        return plugin;
    }


    // ── METHOD getImageDescriptor — REQUESTING A HOLOGRAPHIC SCHEMATIC ───────
    // Intelligence hands over a holographic schematic (ImageDescriptor) for any
    // icon we request by path.  If the path does not resolve to a real file in
    // our bundle, we return null rather than crashing the mission.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We look up an {@link ImageDescriptor} by the given relative file path
     * within our bundle.  Use the {@code IMG_} constants from
     * {@code CommonUIConstants} for the key.  Returns {@code null} if the path
     * does not resolve.
     *
     * @param key the relative path to the image inside the plugin bundle
     * @return the image descriptor, or {@code null} if not found
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


    // ── METHOD getImage — MATERIALIZING THE HOLOGRAM ─────────────────────────
    // Intel retrieves the actual SWT Image from the hangar, creating it on first
    // request and caching it in the image registry for every subsequent call.
    // Do not dispose the returned image — we manage its lifetime for you.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return an SWT {@link Image} for the given key, creating and caching it
     * on first use.  Use the {@code IMG_} constants from {@code CommonUIConstants}
     * for the key.  Do not dispose the returned image — we dispose it
     * automatically when the plugin stops.
     *
     * @param key the relative path to the image inside the plugin bundle
     * @return the SWT Image, or {@code null} if the key cannot be resolved
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


    // ── METHOD getPluginProperties — READING THE BASE MANIFEST ───────────────
    // The base manifest contains all the named properties for our installation.
    // We load it lazily from the bundle on first access and cache it; if the
    // file is missing we log the error and move on rather than crashing.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We lazily load and return the {@link PropertyResourceBundle} from our
     * {@code plugin.properties} file.  The bundle is cached after the first
     * load.  If the file cannot be found, we log an error and return
     * {@code null}.
     *
     * @return the plugin properties bundle, or {@code null} on load failure
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.common.ui", Status.OK, //$NON-NLS-1$
                    Messages.getString( "CommonUIPlugin.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }


    // ── METHOD getColor (RGB) — MIXING PAINT IN THE REBEL WORKSHOP ───────────
    // The rebel workshop keeps a registry of every paint color it has mixed so
    // it never has to mix the same one twice.  We cache SWT Colors by their RGB
    // string key, creating a new Color only on first request.  Do not dispose.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We return an SWT {@link Color} for the given RGB value, creating and
     * registering it on first use.  The color registry manages the lifecycle,
     * so do not dispose the returned color yourself — we handle that when the
     * plugin stops.
     *
     * @param rgb the RGB color data to look up or create
     * @return the SWT Color corresponding to the given RGB
     */
    public Color getColor( RGB rgb )
    {
        if ( !colorRegistry.hasValueFor( rgb.toString() ) )
        {
            colorRegistry.put( rgb.toString(), rgb );
        }

        return colorRegistry.get( rgb.toString() );
    }


    // ── METHOD getColor (name) — READING THE NAMED PAINT POT ─────────────────
    // Each paint pot in the rebel workshop has a name on the label.  If the
    // preference store says this color is still at its factory default, we
    // return null to let the system theme take over — overriding theme colors
    // when high-contrast mode is active would be a tactical blunder.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We look up the color preference stored under the given {@code name} and
     * return the corresponding SWT {@link Color}.  If the preference is still
     * at its default value (meaning no user or scheme override), we return
     * {@code null} so the active Eclipse theme's color is used instead.  This
     * avoids overriding system colors when a high-contrast theme is active.
     *
     * @param name the preference store key for the color
     * @return the SWT Color, or {@code null} if the preference is at its default
     */
    public Color getColor( String name )
    {
        IPreferenceStore store = getPreferenceStore();
        if ( store.isDefault( name ) )
        {
            return null;
        }
        RGB rgb = PreferenceConverter.getColor( store, name );
        Color color = getColor( rgb );
        return color;
    }

}
