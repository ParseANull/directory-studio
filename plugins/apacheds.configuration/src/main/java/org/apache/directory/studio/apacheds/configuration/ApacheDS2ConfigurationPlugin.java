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
package org.apache.directory.studio.apacheds.configuration;


import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.loader.JarLdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: ApacheDS2ConfigurationPlugin — THE DEATH STAR ENGINEERING WING ACTIVATOR ────────
// When the Empire boots up, the engineering wing powers on: it loads its schematics (the
// adsconfig LDAP schema), stocks the image registry with icons, and sets itself up as the
// shared plugin instance that every other class in this plugin can reference.
// This is that activator: the OSGi bundle lifecycle entry point for the apacheds.configuration
// plugin, managing startup, shutdown, images, plugin properties, and the LDAP schema manager.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi activator (plug-in class) for the ApacheDS 2.x Configuration plugin.
 * Manages the plugin's lifecycle, the shared {@link SchemaManager} for parsing ApacheDS
 * configuration LDIF files, and the image registry for editor icons.
 * Think of it as the Death Star engineering wing's main power switch.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApacheDS2ConfigurationPlugin extends AbstractUIPlugin
{
    /** The shared instance */
    private static ApacheDS2ConfigurationPlugin plugin;

    /** The plugin properties */
    private PropertyResourceBundle properties;

    /** The schema manager */
    private SchemaManager schemaManager;


    // ── Engineering Wing Powers On ────────────────────────────────────────────────────────────
    // When the plugin is instantiated by the OSGi framework, we store ourselves as the shared
    // instance so the rest of the plugin can call getDefault().
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the plugin and registers it as the shared instance.
     * Called by the OSGi framework; do not call directly.
     */
    public ApacheDS2ConfigurationPlugin()
    {
        plugin = this;
    }


    // ── Starting The Engineering Wing ─────────────────────────────────────────────────────────
    // Eclipse calls this when the plugin's bundle is first activated.
    // The schema manager is initialized lazily (on first call to getSchemaManager()) so we
    // don't pay the cost unless the configuration editor is actually opened.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the plugin's OSGi bundle is activated.
     * Delegates to the superclass for standard initialization.
     *
     * @param context  the OSGi bundle context
     * @throws Exception if the superclass start fails
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
    }


    // ── Shutting Down The Engineering Wing ────────────────────────────────────────────────────
    // Eclipse calls this when the plugin is deactivated (e.g., on Studio shutdown).
    // Standard superclass handling releases the image registry and other resources.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the plugin's OSGi bundle is deactivated.
     * Delegates to the superclass for standard cleanup.
     *
     * @param context  the OSGi bundle context
     * @throws Exception if the superclass stop fails
     */
    public void stop( BundleContext context ) throws Exception
    {
        super.stop( context );
    }


    // ── Loading The ApacheDS Configuration Schema (Lazily) ────────────────────────────────────
    // The configuration editor needs a SchemaManager to interpret the adsconfig LDAP schema
    // (attribute types, object classes used in config.ldif).
    // We load it lazily: on first call we use JarLdifSchemaLoader to find and load "adsconfig"
    // and its dependencies from the classpath JARs, then cache the result.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lazily initialised {@link SchemaManager} for the {@code adsconfig} LDAP schema.
     * On the first call, loads the schema from the bundled JAR using {@link JarLdifSchemaLoader},
     * then validates that no errors occurred.
     * Throws an {@link Exception} if schema loading fails — the editor can't function without it.
     *
     * <p>For example — the engineering wing needs the schematics:</p>
     * <pre>
     *   getSchemaManager() → loads adsconfig schema → caches → returns SchemaManager
     *   Subsequent calls → returns the cached instance
     * </pre>
     *
     * @return the initialized schema manager
     * @throws Exception if schema loading fails or produces errors
     */
    public SchemaManager getSchemaManager() throws Exception
    {
        // Is the schema manager initialized?
        if ( schemaManager == null )
        {
            // Initializing the schema loader and schema manager
            SchemaLoader loader = new JarLdifSchemaLoader();
            schemaManager = new DefaultSchemaManager( loader );

            // Loading only the 'adsconfig' schema with its dependencies
            schemaManager.loadWithDeps( "adsconfig" ); //$NON-NLS-1$

            // Checking if no error occurred when loading the schemas
            if ( schemaManager.getErrors().size() != 0 )
            {
                throw new Exception( Messages.getString( "ApacheDS2ConfigurationPlugin.CouldNotLoadSchemaCorrectly" ) ); //$NON-NLS-1$
            }
        }

        return schemaManager;
    }


    // ── Returning The Shared Plugin Instance ─────────────────────────────────────────────────
    // The classic Eclipse plugin singleton pattern: everyone calls getDefault() to get the one
    // shared instance that was registered at construction time.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared plugin instance — the single {@link ApacheDS2ConfigurationPlugin}
     * that Eclipse keeps alive for the duration of the Studio session.
     *
     * @return the shared instance
     */
    public static ApacheDS2ConfigurationPlugin getDefault()
    {
        return plugin;
    }


    // ── Getting An Image Descriptor By Resource Path ──────────────────────────────────────────
    // Image descriptors are lightweight references — useful for menus and toolbars that want
    // to create images on demand.  We locate the resource within the bundle via FileLocator.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link ImageDescriptor} for the resource at the given bundle-relative path.
     * Use the {@code IMG_*} constants from {@link ApacheDS2ConfigurationPluginConstants} as the key.
     * Returns {@code null} if the key is null or the resource cannot be found.
     *
     * @param key  the bundle-relative path to the image resource (e.g., {@code "resources/icons/editor.gif"})
     * @return the image descriptor, or {@code null} if not found
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


    // ── Getting A Cached SWT Image By Resource Path ───────────────────────────────────────────
    // SWT Images must not be created repeatedly (that leaks native handles).
    // We use the Eclipse ImageRegistry as a cache: look it up, create-and-cache on miss.
    // Don't dispose the returned Image — the registry disposes it when the plugin stops.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a cached SWT {@link Image} for the given bundle-relative path.
     * Uses the plugin's {@code ImageRegistry} as a cache — creates and caches the image on
     * first access.
     * Do NOT dispose the returned image; it is managed by the registry.
     *
     * @param key  the bundle-relative path to the image resource
     * @return the SWT Image, or {@code null} if the resource cannot be found
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


    // ── Loading The Plugin's Property File ────────────────────────────────────────────────────
    // plugin.properties holds human-visible plugin metadata like the vendor name and version.
    // We load it lazily; if it fails we log the error but don't crash the plugin.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lazily loaded {@link PropertyResourceBundle} from {@code plugin.properties}.
     * On first call, opens the file from the bundle; logs an error and returns {@code null}
     * if the file cannot be read.
     *
     * @return the plugin properties bundle, or {@code null} if loading failed
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
                    new Status( Status.ERROR, "org.apache.directory.studio.apacheds.configuration", Status.OK, //$NON-NLS-1$
                        Messages.getString( "ApacheDS2ConfigurationPlugin.UnableGetProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
