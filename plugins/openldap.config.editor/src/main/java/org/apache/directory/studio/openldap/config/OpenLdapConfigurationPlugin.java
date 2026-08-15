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
package org.apache.directory.studio.openldap.config;


import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;


// ── CLASS: OpenLdapConfigurationPlugin — Palpatine Issues Order 66 ───────────
// In Revenge of the Sith, Palpatine activates Order 66 — a single command that
// instantly coordinates the entire clone army, initializes all the prepared
// protocols, and locks in the Empire's operating rules across the galaxy. He is
// the single point of control; everything flows through him.
// This class is our Palpatine: it's the OSGi activator — the single instance
// that Eclipse creates when the plugin starts. It initializes the schema manager,
// manages the image registry, and loads plugin properties. Everything else in the
// plugin reaches back to this class via getDefault().
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi activator (plugin lifecycle controller) for the OpenLDAP Configuration
 * Editor plugin. Eclipse instantiates exactly one instance of this class when the
 * plugin is first used, and that instance lives until Eclipse shuts down.
 * We use it to lazily initialize the {@link SchemaManager} (which is expensive),
 * manage the SWT image registry, and load plugin properties.
 * Think of this class as the Empire's command center: everything in the plugin that
 * needs shared resources comes here to get them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapConfigurationPlugin extends AbstractUIPlugin
{
    /** The one and only instance of this plugin, set during construction. */
    private static OpenLdapConfigurationPlugin plugin;

    /** Lazily loaded plugin properties from plugin.properties. */
    private PropertyResourceBundle properties;

    /** Lazily initialized schema manager — building this is expensive so we defer it. */
    private SchemaManager schemaManager;


    // ── Palpatine Takes The Throne ────────────────────────────────────────────
    // The moment Palpatine declares himself Emperor, he registers himself as the
    // supreme authority — "I am the Senate." Eclipse calls this constructor once
    // when the plugin activates; we capture the reference in the static field
    // so that getDefault() can hand it out to everyone who asks.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the plugin instance and registers it as the shared singleton.
     * Eclipse calls this exactly once via the OSGi framework. After this returns,
     * {@link #getDefault()} will return this instance. We don't do any heavy
     * initialization here — that's deferred to the first actual request.
     */
    public OpenLdapConfigurationPlugin()
    {
        plugin = this;
    }


    // ── Palpatine Activates The Imperial Schema Registry ─────────────────────
    // Order 66 doesn't just flip a switch — it ensures all clone protocols are
    // loaded and verified before the operation is considered complete. Similarly,
    // we load the full OpenLDAP schema (and its dependencies) and check for errors
    // before handing the schema manager back to callers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's shared {@link SchemaManager}, initializing it on the
     * first call. Building the schema manager means loading the OpenLDAP config
     * schema plus its "system", "core", and "apache" dependencies — this is
     * expensive, so we do it lazily and cache the result. The method is synchronized
     * to handle concurrent plugin startup safely.
     *
     * @return           the initialized SchemaManager
     * @throws Exception if the schema files can't be loaded or if loading produced errors
     */
    public synchronized SchemaManager getSchemaManager() throws Exception
    {
        if ( schemaManager == null )
        {
            // Initializing the schema manager
            schemaManager = new DefaultSchemaManager( new OpenLdapSchemaLoader() );

            // Loading only the OpenLDAP schema (and its dependencies)
            schemaManager.loadWithDeps( OpenLdapSchemaLoader.OPENLDAPCONFIG_SCHEMA_NAME );

            // Checking if no error occurred when loading the schemas
            if ( !schemaManager.getErrors().isEmpty() )
            {
                schemaManager = null;
                throw new Exception( "Could not load the OpenLDAP schema correctly." );
            }
        }

        return schemaManager;
    }


    // ── Palpatine's Office Answers All Requests ───────────────────────────────
    // Every Imperial officer who needs a command or a resource goes to Palpatine's
    // office. His aide hands them the reference to the Emperor. This static method
    // is that aide — every other class in the plugin calls getDefault() to reach
    // the shared plugin instance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's shared singleton instance.
     * This is how the rest of the plugin reaches the activator to get images,
     * properties, and the schema manager. Eclipse guarantees this is non-null
     * after the plugin has started.
     *
     * @return  the one shared plugin instance
     */
    public static OpenLdapConfigurationPlugin getDefault()
    {
        return plugin;
    }


    // ── Palpatine's Propaganda Office Fetches Visual Assets ──────────────────
    // The Empire's propaganda machine needs visual assets — icons, emblems,
    // insignia. The office fetches the right graphic by key (a path within the
    // plugin bundle) and returns a descriptor so the caller can create the image
    // when they're ready. If the path doesn't resolve, we return null gracefully.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link ImageDescriptor} for the image at the given plugin-relative path.
     * Use the {@code IMG_*} constants from {@link OpenLdapConfigurationPluginConstants}
     * as keys. Returns null if the key is null or if the image file can't be found
     * in the plugin bundle — callers should handle null gracefully.
     *
     * @param key  the plugin-relative path to the image (e.g. "resources/icons/database.gif")
     * @return     an ImageDescriptor for the image, or null if not found
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


    // ── Palpatine's Office Delivers The Actual Imperial Icon ─────────────────
    // When the propaganda office needs to hang the actual Galactic Empire symbol
    // (not just a description of it), they fetch the live SWT Image from the
    // registry. If it's not cached yet, they create it and store it for next time.
    // Don't dispose it — the plugin will clean it up on shutdown.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the given plugin-relative path key.
     * Images are cached in the Eclipse image registry so we only create them once.
     * If the image isn't in the registry yet, we create it from its descriptor and
     * register it. Do NOT dispose the returned image — the plugin disposes all
     * registry images automatically when it stops.
     *
     * @param key  the plugin-relative path to the image (same keys as
     *             {@link #getImageDescriptor(String)})
     * @return     the SWT Image, or null if the key resolves to nothing
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


    // ── Palpatine's Office Retrieves The Imperial Operating Manual ────────────
    // The Emperor's decrees and operating parameters live in a properties file
    // that's loaded on first access. Everything from wizard IDs to configuration
    // keys comes from this file — we load it lazily and cache it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's {@link PropertyResourceBundle} loaded from plugin.properties.
     * This file holds configuration values like wizard IDs that are referenced at
     * runtime. We load it lazily on first access. If loading fails, we log an error
     * and return null, so callers should handle null.
     *
     * @return  the loaded property bundle, or null if plugin.properties couldn't be read
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
                        Messages.getString( "OpenLdapConfigurationPlugin.UnableGetProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
