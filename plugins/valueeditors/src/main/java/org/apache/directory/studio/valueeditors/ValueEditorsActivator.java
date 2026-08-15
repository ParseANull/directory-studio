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
package org.apache.directory.studio.valueeditors;


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


// ── CLASS: ValueEditorsActivator — THE REBEL BASE POWERING UP AT YAVIN IV ────
// At dawn on Yavin IV, the Rebel techs throw the master switch and every system
// in the base spins up: hangars open, computers boot, weapon racks are loaded.
// When the base goes dark at the end of the mission, everything shuts down cleanly.
// This activator does the same for the valueeditors plugin: it registers the bundle
// with Eclipse when the first value editor is needed and tears it all down cleanly
// when Eclipse shuts off.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * OSGi bundle activator (Eclipse calls it a "plugin activator") for the
 * valueeditors plugin.
 * Eclipse invokes {@link #start} when the plugin is first needed and
 * {@link #stop} when Eclipse exits.  In between, this class acts as the
 * single shared instance that any code in the plugin can reach via
 * {@link #getDefault()} to load images or read plugin properties.
 * Think of this class as the Rebel base's master power unit — everything in
 * the plugin depends on it being alive.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueEditorsActivator extends AbstractUIPlugin
{
    /** The shared instance */
    private static ValueEditorsActivator plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── Powering Up the Base ─────────────────────────────────────────────────
    // The Rebel tech hits the master switch and the Yavin IV base flickers to life.
    // Every subsystem registers itself, ready to serve the mission.
    // We store the singleton reference so all plugin code can find this activator.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the activator and stores it as the shared singleton instance.
     * Eclipse calls this constructor exactly once per plugin lifecycle — we don't
     * call it ourselves.  The singleton pattern here lets any code in the plugin
     * call {@link #getDefault()} to get hold of this instance.
     */
    public ValueEditorsActivator()
    {
        plugin = this;
    }


    // ── Throwing the Master Switch ───────────────────────────────────────────
    // The lead tech initiates the full Yavin IV base power-up sequence.
    // The generator hums, the main computers boot, and all subsystems go green.
    // We delegate to the Eclipse superclass to complete standard plugin startup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the plugin bundle first activates.
     * We hand off to the superclass which handles image-registry setup and the
     * rest of the standard Eclipse plugin lifecycle.
     *
     * @param context  The OSGi bundle context for this plugin — Eclipse passes
     *                 this in; we just forward it.
     * @throws Exception  if the superclass startup fails for any reason.
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
    }


    // ── Powering Down and Evacuating ────────────────────────────────────────
    // The Rebels have won (or lost) and it's time to evacuate Yavin IV.
    // The tech shuts off all systems in reverse order, and the lights go dark.
    // We null out the singleton so nobody tries to use a dead plugin reference.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the plugin bundle is stopping.
     * We clear the singleton reference first (so any stray code that calls
     * {@link #getDefault()} during shutdown gets {@code null} rather than a
     * half-dead object) and then let the superclass clean up the image registry
     * and other resources.
     *
     * @param context  The OSGi bundle context — Eclipse passes this in.
     * @throws Exception  if superclass shutdown fails.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── Signalling "Base Is Operational" ────────────────────────────────────
    // A pilot radios the base: "Is Yavin IV online?"  The tech confirms with
    // a thumbs-up and hands the pilot the master comms unit.
    // This method is the thumbs-up: it returns our one live activator instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton activator instance for the valueeditors plugin.
     * Every part of the plugin that needs images or plugin properties calls
     * this to get hold of the shared instance.  Returns {@code null} if the
     * plugin hasn't started yet or has already stopped.
     *
     * @return  The shared {@code ValueEditorsActivator}, or {@code null} if not running.
     */
    public static ValueEditorsActivator getDefault()
    {
        return plugin;
    }


    // ── Loading a Weapon From the Armoury ────────────────────────────────────
    // A Rebel tech wants the icon for the X-Wing bay, so she checks the armoury
    // manifest (the constants) and asks the quartermaster for the descriptor.
    // We resolve the bundle-relative path to an Eclipse ImageDescriptor — the
    // lightweight handle that lets SWT create the actual pixel image on demand.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link ImageDescriptor} for the icon at the given bundle-relative
     * path (typically one of the {@code IMG_*} constants in {@link ValueEditorsConstants}).
     * An {@code ImageDescriptor} is a lightweight recipe for creating an SWT
     * {@link org.eclipse.swt.graphics.Image} — useful when we want to pass an
     * image around without creating the pixel data straight away.
     *
     * <p>For example — the tech checks out the address-editor icon:</p>
     * <pre>
     *   ImageDescriptor desc = activator.getImageDescriptor(
     *       ValueEditorsConstants.IMG_ADDRESSEDITOR);
     *   // desc is now ready; call desc.createImage() when we need pixels
     * </pre>
     *
     * @param key  Bundle-relative path to the image file (e.g. {@code "resources/icons/foo.gif"}).
     * @return     The {@code ImageDescriptor}, or {@code null} if the path can't be resolved.
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


    // ── Handing Out the Actual Blaster ───────────────────────────────────────
    // The pilot doesn't just want the weapon description — she wants the blaster
    // itself, ready to fire.  The armoury caches each weapon so it's only
    // manufactured once; subsequent requests grab the cached copy.
    // We cache SWT Images in Eclipse's ImageRegistry so we create each one once
    // and never dispose of them manually (the registry handles that at shutdown).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the icon at the given bundle-relative path.
     * Unlike {@link #getImageDescriptor}, this hands back the actual pixel image,
     * ready to drop straight into a label or button.  We cache it in Eclipse's
     * {@link org.eclipse.jface.resource.ImageRegistry}, so the same pixels aren't
     * loaded from disk more than once.
     *
     * <p><strong>Do not dispose the returned image.</strong>  The registry owns it
     * and will dispose it automatically when the plugin stops.</p>
     *
     * <p>For example — the address dialog fetches its toolbar icon:</p>
     * <pre>
     *   Image icon = ValueEditorsActivator.getDefault()
     *       .getImage(ValueEditorsConstants.IMG_ADDRESSEDITOR);
     *   shell.setImage(icon);
     * </pre>
     *
     * @param key  Bundle-relative path to the image; use {@link ValueEditorsConstants} constants.
     * @return     The SWT {@code Image}, or {@code null} if the path can't be resolved.
     * @see ValueEditorsConstants
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


    // ── Reading the Base's Technical Manual ──────────────────────────────────
    // A technician needs to look up the spec sheet for the base's power coupling.
    // She opens the physical binder the first time she needs it; after that it
    // stays open on her workbench for the rest of the mission.
    // We lazily load plugin.properties once and cache it — cheap reads thereafter.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's {@code plugin.properties} as a
     * {@link PropertyResourceBundle}, loading it lazily on the first call.
     * This file holds metadata like plugin name and version that Eclipse doesn't
     * otherwise expose through a simple API.
     *
     * <p>For example — the Rebel tech reads the base's configuration sheet:</p>
     * <pre>
     *   PropertyResourceBundle props = activator.getPluginProperties();
     *   String version = props.getString("Bundle-Version");
     * </pre>
     *
     * @return  The loaded {@code PropertyResourceBundle}, or {@code null} if the
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.valueeditors", Status.OK, //$NON-NLS-1$
                    Messages.getString( "ValueEditorsActivator.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
