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
package org.apache.directory.studio.ldifeditor;


import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: LdifEditorActivator — REBEL BASE COMING ONLINE ────────────────────
// Before the Battle of Yavin the Rebel Alliance flips switches in the
// Great Temple, powers up communications, loads the star-chart templates,
// and registers every colour in the war-room display.
// This activator does the same thing for the LDIF editor: it wires up
// colours, template context types, and the template store at plugin start,
// and tears them all down cleanly when Eclipse shuts the bundle.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * OSGi {@link AbstractUIPlugin} activator for the LDIF Editor plugin.
 * Owns the shared {@link ColorRegistry}, {@link ContributionTemplateStore},
 * and {@link ContributionContextTypeRegistry} for the five LDIF template context
 * types (file, attr-val, modification-record, modification-item, moddn).
 * Think of this as the Rebel Base coming online: registers every resource the
 * editor needs, then shuts them down cleanly when Eclipse leaves.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifEditorActivator extends AbstractUIPlugin
{
    /** The shared instance */
    private static LdifEditorActivator plugin;

    /** Resource bundle */
    private ResourceBundle resourceBundle;

    /** The color registry */
    private ColorRegistry colorRegistry;

    /** The template store */
    private ContributionTemplateStore ldifTemplateStore;

    /** The context type registry */
    private ContributionContextTypeRegistry ldifTemplateContextTypeRegistry;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── CONSTRUCT THE ACTIVATOR ───────────────────────────────────────────────
    // The Rebel commanders arrive at the Great Temple before dawn.
    // They set themselves as the on-duty officer and load the message codebook.
    // We do the same: store the singleton reference and load the resource bundle.
    /**
     * Stores the singleton reference and loads the message resource bundle.
     * If the bundle is missing we continue without it (all {@code getString}
     * calls will return the key name instead of a translated string).
     */
    public LdifEditorActivator()
    {
        plugin = this;

        try
        {
            resourceBundle = ResourceBundle.getBundle( "org.apache.directory.studio.ldifeditor.messages" ); //$NON-NLS-1$
        }
        catch ( MissingResourceException x )
        {
            resourceBundle = null;
        }
    }


    // ── POWER UP THE PLUGIN ───────────────────────────────────────────────────
    // Alliance technicians raise the shield generators and slot the star-chart
    // templates into the briefing terminals.
    // We initialise the colour registry, register five template context types,
    // and load the persisted template store.
    /**
     * {@inheritDoc}
     *
     * <p>Initialises the colour registry, registers the five LDIF template
     * context types, and loads the persisted template store.</p>
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        if ( colorRegistry == null )
        {
            colorRegistry = new ColorRegistry( getWorkbench().getDisplay() );
        }

        if ( ldifTemplateContextTypeRegistry == null )
        {
            ldifTemplateContextTypeRegistry = new ContributionContextTypeRegistry();

            ldifTemplateContextTypeRegistry.addContextType( LdifEditorConstants.LDIF_FILE_TEMPLATE_ID );
            ldifTemplateContextTypeRegistry.getContextType( LdifEditorConstants.LDIF_FILE_TEMPLATE_ID ).addResolver(
                new GlobalTemplateVariables.Cursor() );

            ldifTemplateContextTypeRegistry.addContextType( LdifEditorConstants.LDIF_ATTR_VAL_RECORD_TEMPLATE_ID );
            ldifTemplateContextTypeRegistry.getContextType( LdifEditorConstants.LDIF_ATTR_VAL_RECORD_TEMPLATE_ID )
                .addResolver( new GlobalTemplateVariables.Cursor() );

            ldifTemplateContextTypeRegistry.addContextType( LdifEditorConstants.LDIF_MODIFICATION_RECORD_TEMPLATE_ID );
            ldifTemplateContextTypeRegistry.getContextType( LdifEditorConstants.LDIF_MODIFICATION_RECORD_TEMPLATE_ID )
                .addResolver( new GlobalTemplateVariables.Cursor() );

            ldifTemplateContextTypeRegistry.addContextType( LdifEditorConstants.LDIF_MODIFICATION_ITEM_TEMPLATE_ID );

            ldifTemplateContextTypeRegistry.addContextType( LdifEditorConstants.LDIF_MODDN_RECORD_TEMPLATE_ID );
        }

        if ( ldifTemplateStore == null )
        {
            ldifTemplateStore = new ContributionTemplateStore( getLdifTemplateContextTypeRegistry(),
                getPreferenceStore(), "templates" ); //$NON-NLS-1$
            try
            {
                ldifTemplateStore.load();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }


    // ── POWER DOWN THE PLUGIN ─────────────────────────────────────────────────
    // When the Empire arrives the Rebels evacuate: they save their star charts,
    // turn off the displays, and go dark.
    // We flush the template store to disk and null out every cached resource.
    /**
     * {@inheritDoc}
     *
     * <p>Saves the template store and releases the colour registry,
     * context-type registry, and template store.</p>
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );

        if ( colorRegistry != null )
        {
            colorRegistry = null;
        }

        if ( ldifTemplateContextTypeRegistry != null )
        {
            ldifTemplateContextTypeRegistry = null;
        }

        if ( ldifTemplateStore != null )
        {
            try
            {
                ldifTemplateStore.save();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            ldifTemplateStore = null;
        }
    }


    // ── RETRIEVE THE SHARED INSTANCE ──────────────────────────────────────────
    // The adjutant fetches the on-duty officer so a message can be relayed.
    // We return the singleton plugin instance.
    /**
     * Returns the shared plugin instance.
     *
     * <p>For example — an editor component needs a colour:</p>
     * <pre>
     *   Color c = LdifEditorActivator.getDefault().getColor(rgb);
     * </pre>
     *
     * @return the shared instance
     */
    public static LdifEditorActivator getDefault()
    {
        return plugin;
    }


    // ── LOOK UP OR CREATE A COLOUR ────────────────────────────────────────────
    // C-3PO consults the colour-wheel index and mixes the exact shade the
    // holographic display needs, then caches it for next time.
    // We consult the colour registry and create the SWT Color on first use.
    /**
     * Returns the SWT {@link Color} for the given {@link RGB} triple, creating
     * and caching it in the {@link ColorRegistry} on first request.
     *
     * <p>Do not dispose the returned colour — the registry owns its lifecycle.</p>
     *
     * @param rgb  the colour data
     * @return     the SWT Color
     */
    public Color getColor( RGB rgb )
    {
        if ( !colorRegistry.hasValueFor( rgb.toString() ) )
        {
            colorRegistry.put( rgb.toString(), rgb );
        }

        return colorRegistry.get( rgb.toString() );
    }


    // ── LOAD AN IMAGE DESCRIPTOR ──────────────────────────────────────────────
    // R2-D2 fetches the schematic from the bundle's resource path.
    // We resolve the path relative to the plugin bundle and return a descriptor.
    /**
     * Returns an {@link ImageDescriptor} for the image at {@code key}
     * (a path relative to the plugin bundle root).
     * Use the {@code IMG_} constants from {@link LdifEditorConstants} as keys.
     *
     * @param key  the bundle-relative path
     * @return     the image descriptor, or {@code null} if not found
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


    // ── LOAD AN IMAGE ─────────────────────────────────────────────────────────
    // R2-D2 retrieves the schematic, renders it, and hands it over — but keeps
    // a copy in the droid's internal cache for next time.
    // We load and registry-cache the SWT Image on first request.
    /**
     * Returns the SWT {@link Image} for the image at {@code key},
     * creating and caching it in the image registry on first request.
     * Use the {@code IMG_} constants from {@link LdifEditorConstants} as keys.
     *
     * <p>Do not dispose the returned image — the registry owns its lifecycle.</p>
     *
     * @param key  the bundle-relative path
     * @return     the SWT Image, or {@code null} if not found
     * @see LdifEditorConstants
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


    // ── GET THE TEMPLATE CONTEXT-TYPE REGISTRY ────────────────────────────────
    // The briefing officer hands over the roster of known template categories.
    /**
     * Returns the {@link ContextTypeRegistry} holding the five LDIF template
     * context types used by the content-assist processor.
     *
     * @return the LDIF template context-type registry
     */
    public ContextTypeRegistry getLdifTemplateContextTypeRegistry()
    {
        return ldifTemplateContextTypeRegistry;
    }


    // ── GET THE TEMPLATE STORE ────────────────────────────────────────────────
    // The briefing officer hands over the complete binder of LDIF snippets.
    /**
     * Returns the {@link TemplateStore} that persists user-defined and
     * built-in LDIF completion templates.
     *
     * @return the LDIF template store
     */
    public TemplateStore getLdifTemplateStore()
    {
        return ldifTemplateStore;
    }


    // ── GET THE RESOURCE BUNDLE ───────────────────────────────────────────────
    // The communications officer hands over the signal codebook.
    /**
     * Returns the plugin's message resource bundle, or {@code null} if loading
     * failed during construction.
     *
     * @return the resource bundle
     */
    public ResourceBundle getResourceBundle()
    {
        return resourceBundle;
    }


    // ── GET THE PLUGIN PROPERTIES ─────────────────────────────────────────────
    // The quartermaster retrieves the manifest that maps command IDs, wizard
    // IDs, and editor IDs from the plugin.properties file.
    /**
     * Returns the {@link PropertyResourceBundle} loaded from
     * {@code plugin.properties}, loading it lazily on first call.
     *
     * @return the plugin properties bundle
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.ldifeditor", Status.OK, //$NON-NLS-1$
                    "Unable to get the plugin properties.", e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
