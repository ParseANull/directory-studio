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
package org.apache.directory.studio.aciitemeditor;


import java.io.IOException;
import java.util.PropertyResourceBundle;

import org.apache.directory.api.ldap.aci.ACIItemParser;
import org.apache.directory.studio.aciitemeditor.sourceeditor.ACICodeScanner;
import org.apache.directory.studio.aciitemeditor.sourceeditor.ACITextAttributeProvider;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: Activator — THE GRAND MOFF'S COMMAND CENTRE STARTUP SEQUENCE ─────
// When the Death Star comes online, Grand Moff Tarkin's command centre runs a
// boot sequence: weapons systems initialised, comm channels established, and
// security clearance registries loaded so every officer can look up their codes.
// Nothing else in the battle station can function until the boot sequence completes.
// This OSGi activator is that boot sequence: it initialises the ACI parser, the
// syntax highlighter, the template store, and the image registry so every other
// class in the plugin can call getDefault() and get what it needs.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator for the ACI Item Editor plugin.
 * It initialises shared, expensive singletons — the ACI parser, the code scanner,
 * the text-attribute provider, and the template store — on plugin start, and
 * cleans them up on stop.
 * Every other class in the plugin reaches these singletons via {@link #getDefault()}.
 * Think of it as the Grand Moff's command-centre boot sequence: nothing useful happens
 * until this class has run.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Activator extends AbstractUIPlugin
{
    /** The shared instance */
    private static Activator plugin;

    /** The shared ACI Item parser */
    private ACIItemParser aciItemParser;

    /** The shared ACI Code Scanner */
    private ACICodeScanner aciCodeScanner;

    /** The shared ACI TextAttribute Provider */
    private ACITextAttributeProvider textAttributeProvider;

    /** The template store */
    private ContributionTemplateStore aciTemplateStore;

    /** The context type registry */
    private ContributionContextTypeRegistry aciTemplateContextTypeRegistry;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── THE COMMAND CENTRE CONSOLE IS ASSIGNED ────────────────────────────────────
    // Grand Moff Tarkin walks into the command centre and takes his seat at the main
    // console — from this moment on, all requests from the crew route through him.
    // We store a reference to this activator instance so every class can reach it via
    // getDefault(). Eclipse calls this constructor exactly once per plugin lifecycle.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the activator and records the singleton reference.
     * Eclipse calls this when the plugin is first loaded — we don't call it directly.
     */
    public Activator()
    {
        plugin = this;
    }


    // ── THE BOOT SEQUENCE RUNS ────────────────────────────────────────────────────
    // Tarkin's technicians run through the startup checklist: registry online, clearance
    // template store loaded, comm channels initialised. The battle station is now ready.
    // We initialise the template context-type registry and the template store here so
    // the source editor's content-assist can offer ACI snippet completions.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the plugin bundle is started. We use this to initialise
     * the ACI template context-type registry (which defines the completion-context
     * the editor uses) and the template store (which holds the actual snippet templates
     * that get proposed via Ctrl+Space).
     *
     * <p>For example — Tarkin's technicians run the startup checklist:</p>
     * <pre>
     *   context-type registry  →  "what kind of content is the editor dealing with?"
     *   template store         →  "which ACI snippets can we suggest?"
     * </pre>
     *
     * @param context  The OSGi bundle context provided by the framework.
     * @throws Exception if anything in the startup sequence fails.
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        // ACI Template ContextType Registry initialization
        if ( aciTemplateContextTypeRegistry == null )
        {
            aciTemplateContextTypeRegistry = new ContributionContextTypeRegistry();

            aciTemplateContextTypeRegistry.addContextType( ACIITemConstants.ACI_ITEM_TEMPLATE_ID );
            aciTemplateContextTypeRegistry.getContextType( ACIITemConstants.ACI_ITEM_TEMPLATE_ID ).addResolver(
                new GlobalTemplateVariables.Cursor() );
        }

        // ACI Template Store initialization
        if ( aciTemplateStore == null )
        {
            aciTemplateStore = new ContributionTemplateStore( getAciTemplateContextTypeRegistry(),
                getPreferenceStore(), "templates" ); //$NON-NLS-1$

            try
            {
                aciTemplateStore.load();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }


    // ── THE COMMAND CENTRE SHUTS DOWN ────────────────────────────────────────────
    // Tarkin orders the battle station to stand down: consoles power off, crew dismissed.
    // We null the singleton reference so any code that calls getDefault() after shutdown
    // gets null rather than a half-alive object, preventing subtle stale-state bugs.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the plugin bundle is stopped. We clear the singleton so
     * nothing can accidentally use a stopped plugin instance.
     *
     * <p>For example — Tarkin orders the command centre to stand down:</p>
     * <pre>
     *   plugin = null;  // no more Activator.getDefault() after this
     *   super.stop(context);
     * </pre>
     *
     * @param context  The OSGi bundle context provided by the framework.
     * @throws Exception if anything in the shutdown sequence fails.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }


    // ── OFFICERS REPORT TO THE COMMAND CONSOLE ────────────────────────────────────
    // Any officer on the Death Star who needs an authoritative resource goes to Tarkin's
    // console. There's only one console; everyone uses the same one.
    // This method is the equivalent — every other class calls getDefault() to reach the
    // shared activator instance and from there access parsers, images, and templates.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared plugin instance. Every class in the plugin calls this to reach
     * shared singletons (parser, scanner, image registry, template store).
     *
     * <p>For example — an officer queries the command console:</p>
     * <pre>
     *   ACIItemParser parser = Activator.getDefault().getACIItemParser();
     * </pre>
     *
     * @return the single shared {@link Activator} instance, or {@code null} if the plugin is stopped.
     */
    public static Activator getDefault()
    {
        return plugin;
    }


    // ── THE QUARTERMASTER ISSUES EMBLEMS ─────────────────────────────────────────
    // Every Imperial unit needs its emblem — the cog, the TIE silhouette — pulled from
    // the central supply depot by path. The depot hands out descriptors, not the actual
    // patches, because descriptors are cheap and safe to pass around.
    // We do the same: return an ImageDescriptor by plugin-relative path so callers can
    // create or cache the actual SWT Image themselves.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link ImageDescriptor} for the icon at the given plugin-relative path.
     * Prefer {@link #getImage(String)} if you need the actual SWT {@link Image}.
     *
     * <p>For example — the quartermaster issues an emblem descriptor by path:</p>
     * <pre>
     *   ImageDescriptor desc = Activator.getImageDescriptor("icons/aci_item.png");
     * </pre>
     *
     * @param path  Plugin-relative path to the image resource (e.g. {@code "icons/foo.png"}).
     * @return the {@link ImageDescriptor}; never null but may describe a missing image.
     */
    public static ImageDescriptor getImageDescriptor( String path )
    {
        return imageDescriptorFromPlugin( ACIITemConstants.PLUGIN_ID, path );
    }


    // ── THE IMAGE CACHE SERVES UP ACTUAL ICONS ────────────────────────────────────
    // The supply depot also keeps a shelf of already-stitched emblems so it doesn't have
    // to stitch a new one every time someone asks for the same icon. First call stitches;
    // every call after that returns the cached version.
    // This is the ImageRegistry pattern: create once, reuse always. Never dispose the
    // returned image — Eclipse disposes them automatically when the plugin stops.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} at the given plugin-relative path.
     * Images are cached in the plugin's {@link ImageRegistry} — we create each one at most
     * once, so callers must <em>never</em> call {@code image.dispose()} on the result.
     *
     * <p>For example — the depot hands back a cached emblem:</p>
     * <pre>
     *   Image icon = Activator.getDefault().getImage("icons/aci_item.png");
     *   label.setImage(icon);  // do NOT dispose icon — it belongs to the registry
     * </pre>
     *
     * @param path  Plugin-relative path to the image resource.
     * @return the cached {@link Image}, or {@code null} if the path doesn't resolve.
     */
    public Image getImage( String path )
    {
        Image image = getImageRegistry().get( path );

        if ( image == null )
        {
            ImageDescriptor id = getImageDescriptor( path );

            if ( id != null )
            {
                image = id.createImage();
                getImageRegistry().put( path, image );
            }
        }

        return image;
    }


    // ── THE FLEET SHARES ONE DECODER RING ────────────────────────────────────────
    // The entire Death Star runs on a single ACI cipher key — every terminal that needs
    // to decode an access-control string uses the same parser, lazily initialised on
    // first use to avoid wasting resources if no ACI editing happens.
    // The parser is NOT thread-safe, so callers must not share it across threads.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared {@link ACIItemParser} instance.
     * The parser is lazily created on first call. Because it's not thread-safe, callers
     * must not use it concurrently — only call this from the UI thread.
     *
     * <p>For example — an editor uses the fleet's shared cipher decoder:</p>
     * <pre>
     *   ACIItem item = Activator.getDefault().getACIItemParser().parse(aciString);
     * </pre>
     *
     * @return the shared parser; never null after first call.
     */
    public ACIItemParser getACIItemParser()
    {
        if ( aciItemParser == null )
        {
            aciItemParser = new ACIItemParser( null );
        }

        return aciItemParser;
    }


    // ── THE ENGINEERING TEAM MEASURES BUTTON SIZES ────────────────────────────────
    // Before installing the consoles, Tarkin's engineering team measures the standard
    // button width relative to the font the station uses — smaller fonts, narrower buttons.
    // This util method does the same: compute a DLU-based width in pixels so every
    // dialog in the plugin has consistently-sized buttons regardless of OS font scaling.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Calculates the standard JFace button width in pixels for the font used by the
     * given control. This ensures dialog buttons are consistently sized across different
     * OS font-size settings.
     *
     * <p>For example — the engineering team measures console button width:</p>
     * <pre>
     *   int w = Activator.getButtonWidth(shell);
     *   button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
     *   ((GridData)button.getLayoutData()).widthHint = w;
     * </pre>
     *
     * @param control  Any SWT control whose GC we can measure against.
     * @return the button width in pixels, calculated from dialog font metrics.
     */
    public static int getButtonWidth( Control control )
    {
        GC gc = new GC( control );

        try
        {
            gc.setFont( JFaceResources.getDialogFont() );
            FontMetrics fontMetrics = gc.getFontMetrics();

            int width = Dialog.convertHorizontalDLUsToPixels( fontMetrics, IDialogConstants.BUTTON_WIDTH );

            return width;
        }
        finally
        {
            gc.dispose();
        }
    }


    // ── THE COLOUR-CODING MANUAL IS RETRIEVED ─────────────────────────────────────
    // The ACI text editor needs to know which colours to use for which token types —
    // keywords in bold, grants in green, denials in red. The colour-coding manual
    // (the TextAttributeProvider) is created once and shared across the whole plugin.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared {@link ACITextAttributeProvider} that maps ACI token types
     * to their SWT {@link org.eclipse.jface.text.TextAttribute} (colour + style).
     * Created lazily on first call.
     *
     * <p>For example — the scanner retrieves the colour-coding manual:</p>
     * <pre>
     *   ACITextAttributeProvider p = Activator.getDefault().getTextAttributeProvider();
     *   IToken keyword = new Token(p.getAttribute(ACITextAttributeProvider.KEYWORD_ATTRIBUTE));
     * </pre>
     *
     * @return the shared provider; never null after first call.
     */
    public ACITextAttributeProvider getTextAttributeProvider()
    {
        if ( textAttributeProvider == null )
        {
            textAttributeProvider = new ACITextAttributeProvider();
        }

        return textAttributeProvider;
    }


    // ── THE SENSOR ARRAY IS POWERED UP ────────────────────────────────────────────
    // The sensor array (the code scanner) reads the raw ACI text token by token,
    // identifying keywords, strings, and grant/deny values so the syntax highlighter
    // can colour them correctly. Lazily initialised — no point spinning it up if no
    // ACI source editor is ever opened.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared {@link ACICodeScanner} used to tokenize ACI text for syntax
     * highlighting. Created lazily on first call, using the shared text-attribute provider.
     *
     * <p>For example — the source-viewer configuration fetches the scanner:</p>
     * <pre>
     *   ACICodeScanner scanner = Activator.getDefault().getAciCodeScanner();
     *   DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
     * </pre>
     *
     * @return the shared scanner; never null after first call.
     */
    public ACICodeScanner getAciCodeScanner()
    {
        if ( aciCodeScanner == null )
        {
            aciCodeScanner = new ACICodeScanner( getTextAttributeProvider() );
        }

        return aciCodeScanner;
    }


    // ── THE TEMPLATE REGISTRY IS CONSULTED ────────────────────────────────────────
    // The content-assist system needs to know what "context type" the ACI editor belongs
    // to before it can offer the right templates. The registry is the lookup table for this.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse template context-type registry for ACI items.
     * The registry maps a context-type ID (like {@code ACI_ITEM_TEMPLATE_ID}) to the
     * context type object used by the template completion processor.
     *
     * <p>For example — content-assist asks "which context am I in?":</p>
     * <pre>
     *   TemplateContextType type = Activator.getDefault()
     *       .getAciTemplateContextTypeRegistry()
     *       .getContextType(ACIITemConstants.ACI_ITEM_TEMPLATE_ID);
     * </pre>
     *
     * @return the context-type registry; may be null if the plugin hasn't started yet.
     */
    public ContributionContextTypeRegistry getAciTemplateContextTypeRegistry()
    {
        return aciTemplateContextTypeRegistry;
    }


    // ── THE SNIPPET LIBRARY IS RETRIEVED ──────────────────────────────────────────
    // When an operator presses Ctrl+Space in the ACI source editor, the template store
    // supplies the list of pre-built ACI snippets they can insert. The store persists
    // across sessions using the Eclipse preference store.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse template store that holds the user-defined and built-in ACI
     * item snippets for content-assist. Loaded from the preference store at plugin start.
     *
     * <p>For example — content-assist fetches available snippets:</p>
     * <pre>
     *   Template[] templates = Activator.getDefault()
     *       .getAciTemplateStore()
     *       .getTemplates(ACIITemConstants.ACI_ITEM_TEMPLATE_ID);
     * </pre>
     *
     * @return the template store; may be null if the plugin hasn't started yet.
     */
    public ContributionTemplateStore getAciTemplateStore()
    {
        return aciTemplateStore;
    }


    // ── THE MANIFEST IS READ FROM THE SUPPLY HOLD ─────────────────────────────────
    // The plugin.properties file holds the human-readable name, vendor, and version
    // strings for the plugin. If we can't load it, we log the error — but we don't
    // crash because the properties are not critical to runtime operation.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's {@code plugin.properties} file as a {@link PropertyResourceBundle}.
     * Lazily loaded on first call. If the file can't be found, we log an error and return null.
     *
     * <p>For example — the plugin reads its own manifest from the supply hold:</p>
     * <pre>
     *   String vendor = Activator.getDefault().getPluginProperties().getString("Bundle-Vendor");
     * </pre>
     *
     * @return the property bundle, or {@code null} if loading fails.
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
                    new Status( Status.ERROR, "org.apache.directory.studio.aciitemeditor", Status.OK, //$NON-NLS-1$
                        "Unable to get the plugin properties.", e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
