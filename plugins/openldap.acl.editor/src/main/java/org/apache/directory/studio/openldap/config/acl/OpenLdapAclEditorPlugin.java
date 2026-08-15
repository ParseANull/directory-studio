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
package org.apache.directory.studio.openldap.config.acl;


import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.apache.directory.studio.openldap.config.acl.sourceeditor.OpenLdapAclCodeScanner;
import org.apache.directory.studio.openldap.config.acl.sourceeditor.OpenLdapAclTextAttributeProvider;


// ── CLASS: OpenLdapAclEditorPlugin — R2-D2 BOOTING THE REBEL FLEET SYSTEMS ──
// R2-D2 rolls aboard the Millennium Falcon, plugs into the ship's systems, and
// brings every subsystem online: navigation, weapons, life support — in the
// right order, on demand. This class does the same for our plugin: it wires
// up the code scanner, text-attribute provider, template registry, and template
// store when Eclipse starts us, and shuts everything down cleanly when we stop.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi activator (entry point) for the OpenLDAP ACL Editor plugin. Eclipse
 * calls {@link #start(BundleContext)} when the plugin is loaded and
 * {@link #stop(BundleContext)} when it is unloaded. We use this class to boot
 * shared singletons — the code scanner, text-attribute provider, template
 * registry, and template store — lazily so we only pay the cost when something
 * actually needs them.
 * Think of this class as R2-D2 plugging into the ship: one socket, everything
 * comes online in the right sequence, and on shutdown it all powers down cleanly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclEditorPlugin extends AbstractUIPlugin
{
    /** The shared instance */
    private static OpenLdapAclEditorPlugin plugin;

    /** The shared OpenLDAP ACL Code Scanner */
    private OpenLdapAclCodeScanner codeScanner;

    /** The shared OpenLDAP ACL TextAttribute Provider */
    private OpenLdapAclTextAttributeProvider textAttributeProvider;

    /** The context type registry */
    private ContributionContextTypeRegistry templateContextTypeRegistry;

    /** The template store */
    private ContributionTemplateStore templateStore;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── R2 Powers On and Registers Himself ───────────────────────────────────
    // The moment R2 connects to the ship's interface he announces himself and
    // stores a reference so the crew can always find him.
    // We store {@code this} in the static {@code plugin} field so any part of
    // the plugin can call {@link #getDefault()} to reach us.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the plugin activator and stores a self-reference in the static
     * {@code plugin} field. Eclipse calls this once on first use. We do as little
     * as possible here — heavy setup goes in {@link #start(BundleContext)}.
     */
    public OpenLdapAclEditorPlugin()
    {
        plugin = this;
    }


    // ── R2 Brings the Ship's Systems Online ──────────────────────────────────
    // R2 fires up navigation, shields, and comms in the right order as soon as
    // the Millennium Falcon's main power is restored.
    // We initialise the template context-type registry and template store here
    // so they are ready before any UI component asks for them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse/OSGi when the plugin bundle is started. We boot the
     * template context-type registry (so auto-complete knows which ACL template
     * "category" to offer) and load the template store from the preference store.
     * If the template store load fails we just print the stack trace — it is not
     * fatal; the editor will still open, just without saved templates.
     *
     * <p>For example — R2 initialising the ship's communication array:</p>
     * <pre>
     *   plugin.start(context);
     *   // templateContextTypeRegistry ready
     *   // templateStore loaded from preferences
     *   // plugin is live
     * </pre>
     *
     * @param context  The OSGi {@link BundleContext} Eclipse hands us — we pass
     *                 it up to the parent but otherwise mostly ignore it here.
     * @throws Exception  if the parent {@code start} throws — should be rare.
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        // OpenLDAP ACL Template ContextType Registry initialization
        if ( templateContextTypeRegistry == null )
        {
            templateContextTypeRegistry = new ContributionContextTypeRegistry();

            templateContextTypeRegistry.addContextType( OpenLdapAclEditorPluginConstants.TEMPLATE_ID );
            templateContextTypeRegistry.getContextType( OpenLdapAclEditorPluginConstants.TEMPLATE_ID ).addResolver(
                new GlobalTemplateVariables.Cursor() );
        }

        // OpenLDAP ACL Template Store initialization
        if ( templateStore == null )
        {
            templateStore = new ContributionTemplateStore( getTemplateContextTypeRegistry(),
                getPreferenceStore(), "templates" ); //$NON-NLS-1$

            try
            {
                templateStore.load();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }


    // ── R2 Powers Down the Ship Before Docking ───────────────────────────────
    // Before the Falcon enters the docking bay R2 shuts down non-essential
    // systems and clears the shared reference so no stray call can reach him.
    // We null out the static plugin reference here to avoid memory leaks in
    // Eclipse's OSGi runtime, then let the parent handle the rest.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse/OSGi when the plugin bundle is stopped. We null out the
     * static {@code plugin} reference so the garbage collector can reclaim our
     * memory, then delegate to the parent which closes the image registry and
     * other Eclipse-managed resources.
     *
     * <p>For example — R2 clearing his ship interface before shutdown:</p>
     * <pre>
     *   plugin.stop(context);
     *   // plugin == null
     *   // all Eclipse-managed resources freed
     * </pre>
     *
     * @param context  The OSGi {@link BundleContext}.
     * @throws Exception  if the parent {@code stop} throws.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── R2 Fires Up the Syntax-Coloring Array ────────────────────────────────
    // R2 has a specialised optical scanner that recognises Imperial security
    // codes at a glance. He boots it the first time anyone needs it, then keeps
    // it warm for subsequent calls.
    // Our code scanner does the same: lazily created on first request, then
    // reused for the lifetime of the plugin.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared {@link OpenLdapAclCodeScanner} singleton. The scanner
     * is what Eclipse's source editor uses to colour-code ACL keywords, strings,
     * and identifiers. We create it lazily on first call and reuse it after that
     * because creating a scanner has non-trivial cost (rule setup, token wiring).
     *
     * <p>For example — R2 activating his Imperial code analyser:</p>
     * <pre>
     *   OpenLdapAclCodeScanner scanner = plugin.getCodeScanner();
     *   // scanner is non-null and ready to highlight ACL text
     * </pre>
     *
     * @return  The shared {@link OpenLdapAclCodeScanner}; never {@code null}.
     */
    public OpenLdapAclCodeScanner getCodeScanner()
    {
        if ( codeScanner == null )
        {
            codeScanner = new OpenLdapAclCodeScanner( getTextAttributeProvider() );
        }

        return codeScanner;
    }


    // ── R2 Supplies the Colour Palette for Highlighted Text ──────────────────
    // R2's display shows each token type in a different colour — keywords in
    // blue, strings in green, plain text in white. The TextAttributeProvider
    // is that colour-palette lookup; R2 boots it once and keeps it around.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared {@link OpenLdapAclTextAttributeProvider} singleton.
     * This maps token-type strings (DEFAULT, KEYWORD, STRING) to the actual
     * SWT {@code TextAttribute} objects the editor uses to colour text. Like
     * the code scanner, we create it lazily and cache it.
     *
     * <p>For example — R2 booting his colour-display subsystem:</p>
     * <pre>
     *   OpenLdapAclTextAttributeProvider tap = plugin.getTextAttributeProvider();
     *   // tap.getAttribute("KEYWORD") returns a bold-blue TextAttribute
     * </pre>
     *
     * @return  The shared {@link OpenLdapAclTextAttributeProvider}; never {@code null}.
     */
    public OpenLdapAclTextAttributeProvider getTextAttributeProvider()
    {
        if ( textAttributeProvider == null )
        {
            textAttributeProvider = new OpenLdapAclTextAttributeProvider();
        }

        return textAttributeProvider;
    }


    // ── R2 Retrieves the Template Context Registry ────────────────────────────
    // The context registry tells the auto-complete engine which "category" of
    // templates to offer. R2 knows exactly which databank holds these records
    // and hands them over on request.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ContributionContextTypeRegistry} that categorises ACL
     * code templates. The content-assist processor needs this to know which
     * templates belong to the ACL editor context (as opposed to, say, Java or
     * XML templates that might also be registered in Eclipse).
     *
     * <p>For example — R2 retrieving the template category manifest:</p>
     * <pre>
     *   ContributionContextTypeRegistry reg = plugin.getTemplateContextTypeRegistry();
     *   // reg.getContextType(TEMPLATE_ID) returns our ACL context type
     * </pre>
     *
     * @return  The {@link ContributionContextTypeRegistry}; may be {@code null}
     *          before {@link #start(BundleContext)} has been called.
     */
    public ContributionContextTypeRegistry getTemplateContextTypeRegistry()
    {
        return templateContextTypeRegistry;
    }


    // ── R2 Opens the Snippet Store ───────────────────────────────────────────
    // R2 has a cache of pre-built ACL snippets that the crew can insert with
    // a few keystrokes. This method hands back that cache — the template store.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ContributionTemplateStore} holding any ACL code
     * templates the user has defined. Templates are "snippets" the user can
     * insert via auto-complete rather than typing the full ACL syntax by hand.
     * We persist them in Eclipse's preference store so they survive restarts.
     *
     * <p>For example — R2 handing Leia her pre-recorded message templates:</p>
     * <pre>
     *   ContributionTemplateStore store = plugin.getTemplateStore();
     *   // store.getTemplates(TEMPLATE_ID) returns user-defined ACL snippets
     * </pre>
     *
     * @return  The {@link ContributionTemplateStore}; may be {@code null} before
     *          {@link #start(BundleContext)} has been called.
     */
    public ContributionTemplateStore getTemplateStore()
    {
        return templateStore;
    }


    // ── R2 Surfaces the Single Shared Instance ───────────────────────────────
    // Anywhere on the ship, any crew member can ask "where is R2?" and get a
    // direct reference. This static getter does exactly that for the plugin.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the singleton instance of this activator that Eclipse created when
     * the plugin was started. Other classes in this plugin use this to reach shared
     * services without having to pass the plugin around as a parameter.
     *
     * <p>For example — any crew member locating R2 anywhere on the ship:</p>
     * <pre>
     *   OpenLdapAclEditorPlugin plugin = OpenLdapAclEditorPlugin.getDefault();
     *   Image icon = plugin.getImage(IMG_ADD);
     * </pre>
     *
     * @return  The shared plugin instance; {@code null} if the plugin has been
     *          stopped or has not yet started.
     */
    public static OpenLdapAclEditorPlugin getDefault()
    {
        return plugin;
    }


    // ── R2 Locates an Icon in the Ship's Asset Store ─────────────────────────
    // R2 can locate any graphic asset by its path reference and return a
    // descriptor the UI components can use to actually render the image.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Looks up an icon from the plugin's resources and returns an
     * {@link ImageDescriptor} the Eclipse UI framework can use to render it. We
     * resolve the path relative to this bundle using the OSGi file-locator so it
     * works whether we are running from source or from an installed JAR. Returns
     * {@code null} if the key is null or the file does not exist.
     *
     * <p>For example — R2 locating the "add" button icon by its asset path:</p>
     * <pre>
     *   ImageDescriptor desc = plugin.getImageDescriptor(IMG_ADD);
     *   // desc.createImage() gives a renderable SWT Image
     * </pre>
     *
     * @param key  Relative path to the image inside the plugin bundle
     *             (e.g. {@code "resources/icons/add.gif"}).
     * @return     An {@link ImageDescriptor} ready to create an SWT Image,
     *             or {@code null} if the file is not found.
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
        }

        return null;
    }


    // ── R2 Delivers the Actual SWT Image Object ───────────────────────────────
    // Rather than re-loading the image every time, R2 caches it in the image
    // registry — a map from path key to SWT Image. On the first request he
    // loads and stores it; after that he just hands back the cached copy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the given resource key, creating it if
     * necessary and caching it in Eclipse's {@code ImageRegistry}. The image is
     * owned by the registry; do not call {@code dispose()} on it — Eclipse will
     * clean it up when the plugin stops.
     *
     * <p>For example — R2 fetching and caching the toolbar icon for "delete":</p>
     * <pre>
     *   Image icon = plugin.getImage(IMG_DELETE);
     *   button.setImage(icon);
     *   // Never call icon.dispose() — the registry owns it
     * </pre>
     *
     * @param key  Relative path to the image inside the plugin bundle.
     * @return     The SWT {@link Image}, or {@code null} if the key is unknown.
     * @see OpenLdapAclEditorPluginConstants
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


    // ── R2 Opens the Plugin Properties File ──────────────────────────────────
    // The ship's technical manual (plugin.properties) contains the IDs and
    // configuration strings the plugin needs at runtime. R2 reads this file
    // once and caches the result so subsequent lookups are instant.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Loads and returns the {@code plugin.properties} resource bundle from
     * inside the plugin bundle. We use this to read things like the template
     * context-type ID that are defined in the plugin manifest rather than in
     * Java code. The properties are loaded lazily and cached.
     *
     * <p>For example — R2 opening the ship's technical specification sheet:</p>
     * <pre>
     *   String templateId = plugin.getPluginProperties().getString("CtxType_Template_id");
     *   // Returns the string value from plugin.properties
     * </pre>
     *
     * @return  The {@link PropertyResourceBundle} from {@code plugin.properties};
     *          may be {@code null} if the file cannot be opened (an error is
     *          logged in that case).
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.openldap.acl.editor", Status.OK, //$NON-NLS-1$
                    Messages.getString( "OpenLdapAclEditorPlugin.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
