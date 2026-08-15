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
package org.apache.directory.studio.templateeditor;


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

// ── CLASS: EntryTemplatePlugin — GRAND ADMIRAL THRAWN COMMANDING THE FLEET ───────
// Grand Admiral Thrawn stands on the bridge of the Chimaera — he coordinates every
// resource, every squad, every piece of intelligence the Empire has. Nothing moves
// without going through him. This class is the OSGi activator (the "plugin class")
// for the Template Editor: Eclipse calls start() and stop() to bring the plugin to
// life or shut it down. From here we bootstrap the TemplatesManager and hand out
// images to every corner of the plugin. Every other class reaches back to
// getDefault() when it needs the shared instance, just as officers look to Thrawn
// for orders.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * OSGi bundle activator and singleton access point for the Entry Template plugin.
 * Eclipse calls {@link #start(BundleContext)} when the plugin loads and
 * {@link #stop(BundleContext)} when it unloads. All shared resources — the
 * {@link TemplatesManager}, the image registry, and plugin properties — live here.
 * Think of this class as Grand Admiral Thrawn on the Chimaera bridge: every other
 * component defers to {@link #getDefault()} to reach the shared command center.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryTemplatePlugin extends AbstractUIPlugin
{
    /** The shared instance */
    private static EntryTemplatePlugin plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;

    /** The templates manager */
    private TemplatesManager templatesManager;


    // ── CONSTRUCTOR: THRAWN TAKES COMMAND OF THE BRIDGE ─────────────────────────
    // Thrawn boards the Chimaera — no fanfare, no ceremony. The ship is already
    // waiting; OSGi will call start() momentarily to bring everything online.
    // The constructor itself does nothing because Eclipse manages the lifecycle.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-arg constructor required by the OSGi framework — Eclipse instantiates
     * this class reflectively. Actual initialization happens in
     * {@link #start(BundleContext)}.
     */
    public EntryTemplatePlugin()
    {
    }


    // ── START: THRAWN BRINGS THE FLEET TO BATTLE READINESS ───────────────────────
    // Thrawn strides onto the bridge, the crew snaps to attention, and every
    // station powers up in sequence: navigation, weapons, intelligence. Here we
    // save the shared instance and create the TemplatesManager so it can load
    // all templates before the first entry editor ever opens.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the plugin bundle is activated. We stash the shared
     * instance and bring the {@link TemplatesManager} online so it can begin
     * loading templates from extension points and the user's disk.
     *
     * <p>For example — Thrawn powers up the fleet:</p>
     * <pre>
     *   plugin = this;                                  // "I have the bridge."
     *   templatesManager = new TemplatesManager(...);   // Fleet brought to readiness.
     * </pre>
     *
     * @param context  the OSGi bundle context supplied by the framework
     * @throws Exception  if the superclass start fails
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
        plugin = this;

        // Creating the templates manager
        templatesManager = new TemplatesManager( getPreferenceStore() );
    }


    // ── STOP: THRAWN ORDERS AN ORDERLY RETREAT ───────────────────────────────────
    // When the battle is lost, Thrawn gives the order to withdraw — no panic, no
    // wasted resources. The shared instance is nulled so the GC can clean up,
    // and the superclass handles the rest of the teardown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the plugin bundle is deactivated (workbench shutdown
     * or deliberate unload). We release the shared reference so the GC can clean up.
     *
     * <p>For example — Thrawn orders the retreat:</p>
     * <pre>
     *   plugin = null;   // "Chimaera, withdraw."
     * </pre>
     *
     * @param context  the OSGi bundle context supplied by the framework
     * @throws Exception  if the superclass stop fails
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── GET DEFAULT: OFFICERS LOOK TO THE BRIDGE ─────────────────────────────────
    // Any officer who needs Thrawn's orders walks to the bridge and asks. This
    // static method is the code equivalent — every other class calls getDefault()
    // to reach the single shared plugin instance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the one shared plugin instance. Any component in the plugin that
     * needs shared resources (templates, images, preferences) goes through this.
     *
     * <p>For example — an officer reports to the bridge:</p>
     * <pre>
     *   EntryTemplatePlugin.getDefault().getTemplatesManager();
     *   // "Admiral, which templates should we use for this entry?"
     * </pre>
     *
     * @return the shared plugin instance; {@code null} if the plugin has not yet
     *         been started or has already been stopped
     */
    public static EntryTemplatePlugin getDefault()
    {
        return plugin;
    }


    // ── GET TEMPLATES MANAGER: THRAWN HANDS OVER THE INTELLIGENCE DOSSIER ────────
    // Thrawn's intelligence officer maintains detailed dossiers on every template
    // available to the fleet. When a component needs to know which templates exist
    // or which one is the default, it requests the dossier here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin-wide {@link TemplatesManager} that owns the registry of
     * all loaded templates. Created during {@link #start(BundleContext)} and lives
     * for the plugin's entire lifetime.
     *
     * <p>For example — an officer checks the intelligence dossier:</p>
     * <pre>
     *   TemplatesManager mgr = EntryTemplatePlugin.getDefault().getTemplatesManager();
     *   Template def = mgr.getDefaultTemplate("inetOrgPerson");
     * </pre>
     *
     * @return the shared {@link TemplatesManager}; never {@code null} after start
     */
    public TemplatesManager getTemplatesManager()
    {
        return templatesManager;
    }


    // ── GET IMAGE DESCRIPTOR: THRAWN REQUESTS AN INTELLIGENCE PHOTOGRAPH ─────────
    // Thrawn's aide fetches a photograph from the archive by its path. If the
    // photograph exists, the aide hands it over. If it's missing, they return
    // nothing rather than crash the briefing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Looks up an {@link ImageDescriptor} by its bundle-relative path (the same
     * constant strings defined in {@link EntryTemplatePluginConstants}).
     * Use the {@code IMG_*} constants as keys. Returns {@code null} rather than
     * crashing if the path doesn't resolve.
     *
     * <p>For example — Thrawn's aide retrieves a photograph:</p>
     * <pre>
     *   ImageDescriptor icon =
     *       EntryTemplatePlugin.getDefault().getImageDescriptor(
     *           EntryTemplatePluginConstants.IMG_TEMPLATE);
     * </pre>
     *
     * @param key  the bundle-relative image path (e.g. {@code "resources/icons/template.gif"})
     * @return the {@link ImageDescriptor}, or {@code null} if the key is {@code null}
     *         or the resource cannot be found
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


    // ── GET IMAGE: THRAWN RETRIEVES AND CACHES A HOLOGRAPHIC DISPLAY ─────────────
    // Thrawn's archive keeps a cached copy of every hologram it has ever displayed.
    // The first request fetches and stores it; every subsequent request returns the
    // cached copy instantly. Don't dispose the image yourself — Thrawn's archive
    // handles cleanup when the bridge shuts down.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a cached SWT {@link Image} for the given bundle-relative key.
     * The image registry is used so each image is created at most once and
     * disposed automatically when the plugin stops.
     *
     * <p><strong>Do not dispose the returned image</strong> — it is shared and
     * will be disposed by the plugin's image registry when the workbench shuts down.</p>
     *
     * <p>For example — Thrawn's archive caches the hologram:</p>
     * <pre>
     *   Image img = EntryTemplatePlugin.getDefault()
     *       .getImage(EntryTemplatePluginConstants.IMG_SWITCH_TEMPLATE);
     *   // First call creates and caches it. Later calls return the cached copy.
     * </pre>
     *
     * @param key  the bundle-relative image path; use {@code IMG_*} constants
     * @return the SWT {@link Image}, or {@code null} if the key cannot be resolved
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


    // ── GET PLUGIN PROPERTIES: THRAWN CONSULTS THE IMPERIAL CODEBOOK ─────────────
    // Every operation has a codebook — ship names, version numbers, protocol IDs.
    // Thrawn reads it from the archive on first request and keeps it handy. If the
    // codebook can't be opened, the error is logged so the fleet knows something
    // is wrong without crashing the entire ship.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's {@code plugin.properties} file parsed into a
     * {@link PropertyResourceBundle}. The file is loaded lazily on first call and
     * cached. Any load error is logged but does not throw — the method returns
     * {@code null} on failure so callers must null-check.
     *
     * <p>For example — Thrawn opens the codebook:</p>
     * <pre>
     *   PropertyResourceBundle props =
     *       EntryTemplatePlugin.getDefault().getPluginProperties();
     *   String version = props.getString("Bundle-Version");
     * </pre>
     *
     * @return the plugin properties bundle, or {@code null} if the file failed to load
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.templateeditor", Status.OK, //$NON-NLS-1$
                    Messages.getString( "EntryTemplatePlugin.UnableToGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
