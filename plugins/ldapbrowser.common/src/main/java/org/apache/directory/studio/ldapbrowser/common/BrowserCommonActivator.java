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
package org.apache.directory.studio.ldapbrowser.common;


import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.apache.directory.studio.connection.core.event.EventRunner;
import org.apache.directory.studio.connection.ui.UiThreadEventRunner;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: BrowserCommonActivator — YAVIN IV BASE POWERS UP ──────────────────
// The Rebel hangar bay on Yavin IV springs to life: lights blaze on, X-wings
// roll out, systems initialize across every station as the Death Star clock
// ticks down. This activator is that moment — it powers up every shared
// resource this plugin needs (fonts, colors, templates, the event runner)
// when OSGi starts the bundle, and shuts it all down cleanly when the bundle
// stops.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi activator (entry point) for the ldapbrowser.common plugin.
 * It manages the plugin's lifecycle — spinning up shared resources like font
 * registries, color registries, filter template stores, and the UI event runner
 * when the bundle starts, and disposing them when it stops.
 * Think of this class as the Rebel base control room that flips every switch
 * before the attack run begins.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserCommonActivator extends AbstractUIPlugin
{
    /** The shared instance */
    private static BrowserCommonActivator plugin;

    /** The font registry */
    private FontRegistry fontRegistry;

    /** The color registry */
    private ColorRegistry colorRegistry;

    /** The value editor preferences */
    private ValueEditorsPreferences valueEditorPreferences;

    /** The filter template store. */
    private ContributionTemplateStore filterTemplateStore;

    /** The filter template context type registry. */
    private ContributionContextTypeRegistry filterTemplateContextTypeRegistry;

    /** The event runner. */
    private EventRunner eventRunner;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── HANGAR BAY CONSTRUCTOR — BASE INSTANCE RECORDED ───────────────────────
    // At Yavin IV, as soon as the last Rebel pilot sprints into the hangar, the
    // base commander logs that the team is assembled and ready for deployment.
    // This constructor does the equivalent: it records this activator instance
    // as "the one" so every other class in the plugin can find it via getDefault().
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates the activator and registers it as the shared singleton instance.
     * OSGi calls this exactly once when the bundle is loaded, before {@link #start}.
     * We need this so that {@link #getDefault()} works as soon as the class is live.
     */
    public BrowserCommonActivator()
    {
        plugin = this;
    }


    // ── ALL SYSTEMS GO — BASE SPINS UP FOR THE ATTACK RUN ─────────────────────
    // General Dodonna gives the thumbs-up: power flows to the shields, the
    // targeting computers warm up, and every squadron checks in on the comms.
    // This method is our "all systems go" — it initializes the event runner,
    // font/color registries, value-editor preferences, and the LDAP filter
    // template machinery so the rest of the plugin has everything it needs.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when this bundle starts up. We initialize all shared
     * resources here — the event runner that dispatches events on the UI thread,
     * the font and color registries used across every view, the value-editor
     * preferences, and the LDAP filter template store used by the filter editor.
     * None of these can be lazy-initialized safely because multiple components
     * reach for them on startup.
     *
     * <p>For example — General Dodonna powering up Yavin IV:</p>
     * <pre>
     *   dodonna.activateShields();
     *   dodonna.warmUpTargetingComputers();
     *   dodonna.loadFilterTemplates();   // "May the Force be with you"
     * </pre>
     *
     * @param context  the OSGi bundle context provided by the framework — we
     *                 pass it up to the parent class and use it indirectly.
     * @throws Exception if any initialization step blows up (OSGi will mark
     *                   the bundle as failed).
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        if ( eventRunner == null )
        {
            eventRunner = new UiThreadEventRunner();
        }

        if ( fontRegistry == null )
        {
            fontRegistry = new FontRegistry( PlatformUI.getWorkbench().getDisplay() );
        }

        if ( colorRegistry == null )
        {
            colorRegistry = new ColorRegistry( PlatformUI.getWorkbench().getDisplay() );
        }

        valueEditorPreferences = new ValueEditorsPreferences();

        if ( filterTemplateContextTypeRegistry == null )
        {
            filterTemplateContextTypeRegistry = new ContributionContextTypeRegistry();
            filterTemplateContextTypeRegistry.addContextType( BrowserCommonConstants.FILTER_TEMPLATE_ID );
            filterTemplateContextTypeRegistry.getContextType( BrowserCommonConstants.FILTER_TEMPLATE_ID ).addResolver(
                new GlobalTemplateVariables.Cursor() );
        }

        if ( filterTemplateStore == null )
        {
            filterTemplateStore = new ContributionTemplateStore( getFilterTemplateContextTypeRegistry(),
                getPreferenceStore(), "templates" ); //$NON-NLS-1$
            try
            {
                filterTemplateStore.load();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }


    // ── EVACUATION COMPLETE — BASE POWERS DOWN AFTER VICTORY ──────────────────
    // After the Death Star explodes, Yavin IV goes dark: X-wings are powered
    // down, generators shut off, and the Rebels pack up and move on. This is
    // our shutdown sequence — every resource we initialized in start() gets
    // released here so we don't leave stale SWT handles or memory leaks behind.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when this bundle is stopping. We null out every resource
     * we created in {@link #start} so the JVM can reclaim the memory and SWT
     * doesn't complain about leaked handles. The filter template store is saved
     * to disk before we release it so user-created templates survive the restart.
     *
     * <p>For example — the Rebel base evacuating Yavin IV:</p>
     * <pre>
     *   templateStore.save();   // don't lose the pilots' nav data
     *   shields.shutdown();
     *   generators.powerDown();
     *   plugin = null;          // the base is gone
     * </pre>
     *
     * @param context  the OSGi bundle context — passed to the parent class.
     * @throws Exception if teardown fails in an unexpected way.
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );

        if ( eventRunner != null )
        {
            eventRunner = null;
        }

        if ( fontRegistry != null )
        {
            fontRegistry = null;
        }

        if ( colorRegistry != null )
        {
            colorRegistry = null;
        }

        if ( filterTemplateContextTypeRegistry != null )
        {
            filterTemplateContextTypeRegistry = null;
        }

        if ( filterTemplateStore != null )
        {
            try
            {
                filterTemplateStore.save();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            filterTemplateStore = null;
        }
    }


    // ── FINDING THE REBEL BASE — THE SHARED INSTANCE IS RETURNED ─────────────
    // Every Rebel pilot knows the coordinates of Yavin IV — the one base they
    // all report back to. This static method is that set of coordinates: anyone
    // anywhere in the plugin calls getDefault() to get the single shared
    // instance of the activator.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the single shared instance of this activator.
     * Because OSGi creates exactly one instance per bundle, this is effectively
     * the plugin's global service locator — everything that needs a font, color,
     * or image calls this first.
     *
     * <p>For example — Luke radioing back to Yavin IV base:</p>
     * <pre>
     *   BrowserCommonActivator base = BrowserCommonActivator.getDefault();
     *   Image icon = base.getImage( BrowserCommonConstants.IMG_ENTRY );
     * </pre>
     *
     * @return the shared activator instance, or {@code null} if the bundle has
     *         not yet started or has already stopped.
     */
    public static BrowserCommonActivator getDefault()
    {
        return plugin;
    }


    // ── TARGETING COMPUTER READS THE ICON — IMAGE DESCRIPTOR LOADED ───────────
    // Luke's targeting computer pulls up a schematic of the exhaust port — not
    // a rendered image, just the descriptor that says "here's where to find it."
    // This method is the same: it returns an ImageDescriptor (a recipe for the
    // image) rather than the SWT Image itself, which is useful when you want
    // to create the image lazily or in a different context.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Looks up an {@link ImageDescriptor} for the given key (a relative path
     * inside this plugin's bundle, like {@code "resources/icons/entry.gif"}).
     * Returns {@code null} if the key is null or the file doesn't exist.
     * Use this when you need to contribute an image to a registry or
     * toolbar contribution before the SWT Image is actually needed.
     *
     * <p>For example — the targeting computer reading the exhaust port schematic:</p>
     * <pre>
     *   ImageDescriptor desc = activator.getImageDescriptor( IMG_ENTRY );
     *   // desc tells you how to find the image, not the image itself
     * </pre>
     *
     * @param key  the relative bundle path to the image file — use one of the
     *             {@code IMG_} constants from {@link BrowserCommonConstants}.
     * @return an {@link ImageDescriptor} for the image, or {@code null} if not found.
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


    // ── X-WING DECAL RETRIEVED FROM THE HANGAR WALL ───────────────────────────
    // The Rebel hangar has a board with every squadron's insignia painted on it
    // — you point at the board, you get the right icon back instantly because
    // they're all pre-cached. This is our image registry: the first request
    // creates the SWT Image and pins it to the registry; every subsequent
    // request just returns the cached one.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the given key, creating and caching it
     * on first access. Subsequent calls with the same key return the cached
     * instance — no duplicate SWT resources.
     * Do NOT dispose the returned image yourself; the plugin disposes it
     * automatically when it stops.
     *
     * <p>For example — a pilot grabbing the right squadron patch from the board:</p>
     * <pre>
     *   Image entryIcon = activator.getImage( BrowserCommonConstants.IMG_ENTRY );
     *   label.setImage( entryIcon );
     * </pre>
     *
     * @param key  the relative bundle path — use an {@code IMG_} constant from
     *             {@link BrowserCommonConstants}.
     * @return the cached SWT {@link Image}, or {@code null} if the path doesn't exist.
     * @see BrowserCommonConstants
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


    // ── PILOT GRABS THE RIGHT HELMET — FONT RETRIEVED FROM REGISTRY ───────────
    // Every Rebel pilot has a custom helmet with their callsign painted on it.
    // The hangar crew keeps them all on a rack — you hand in the font spec,
    // you get the matching SWT Font back. If nobody's asked for that style
    // before, a new helmet gets made and added to the rack.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Font} matching the given {@link FontData} array,
     * creating and caching it in the font registry if this is the first request
     * for that particular font description.
     * Don't dispose the returned font — the plugin owns it and cleans it up.
     *
     * <p>For example — a pilot picking up their pre-stenciled helmet:</p>
     * <pre>
     *   Font bold = activator.getFont( new FontData[]{ new FontData("Arial", 10, SWT.BOLD) } );
     *   viewer.getControl().setFont( bold );
     * </pre>
     *
     * @param fontData  the array of font descriptors; we use index 0 as the
     *                  cache key — pass a real non-null array with at least one entry.
     * @return the corresponding cached SWT {@link Font}.
     */

    public Font getFont( FontData[] fontData )
    {
        if ( !fontRegistry.hasValueFor( fontData[0].toString() ) )
        {
            fontRegistry.put( fontData[0].toString(), fontData );
        }

        return fontRegistry.get( fontData[0].toString() );
    }


    // ── REBEL PAINT JOB — COLOR RETRIEVED FROM REGISTRY ──────────────────────
    // The Rebels paint their ships in coordinated colors so everyone knows whose
    // is whose. The color registry is the paint shop: hand in an RGB triple,
    // get the pre-mixed SWT Color back (mixed fresh the first time, reused after).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Color} for the given RGB value, creating and
     * caching it in the color registry on first access.
     * Don't dispose the returned color — the plugin disposes it when it stops.
     *
     * <p>For example — the Rebel paint crew mixing squadron colors:</p>
     * <pre>
     *   Color red = activator.getColor( new RGB(255, 0, 0) );
     *   label.setForeground( red );
     * </pre>
     *
     * @param rgb  the red-green-blue values of the color you want.
     * @return the corresponding cached SWT {@link Color}.
     */
    public Color getColor( RGB rgb )
    {
        if ( !colorRegistry.hasValueFor( rgb.toString() ) )
        {
            colorRegistry.put( rgb.toString(), rgb );
        }

        return colorRegistry.get( rgb.toString() );
    }


    // ── REQUESTING THE MISSION BRIEFING SCROLL — PREFS HANDED OVER ───────────
    // Before the trench run, every pilot is handed their personal mission scroll
    // — the value-editor preferences that say which editor handles which
    // attribute type. This accessor just hands out that scroll.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ValueEditorsPreferences} instance that maps LDAP
     * attribute types and syntaxes to their respective value editor classes.
     * This is the routing table that decides which widget opens when you
     * double-click an attribute value in the entry editor.
     *
     * <p>For example — the briefing officer handing each pilot their target list:</p>
     * <pre>
     *   ValueEditorsPreferences prefs = activator.getValueEditorsPreferences();
     *   String editorClass = prefs.getAttributeValueEditorMap().get( "cn" );
     * </pre>
     *
     * @return the value editors preferences object — never null after {@link #start}.
     */
    public ValueEditorsPreferences getValueEditorsPreferences()
    {
        return valueEditorPreferences;
    }


    // ── R2 HANDS OVER THE NAV COMPUTER TEMPLATES ──────────────────────────────
    // R2-D2 stores pre-built hyperspace route templates in his memory banks.
    // When the pilot needs one, R2 just hands it over — no computation needed.
    // This method hands over the pre-loaded filter template store the same way.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TemplateStore} for LDAP filter templates.
     * The filter editor uses this to offer pre-built filter snippets via
     * content assist (the autocomplete popup). Templates are loaded from
     * the preference store on startup and saved back on shutdown.
     *
     * <p>For example — R2 projecting saved nav routes for Luke to choose from:</p>
     * <pre>
     *   TemplateStore store = activator.getFilterTemplateStore();
     *   Template[] templates = store.getTemplates( FILTER_TEMPLATE_ID );
     * </pre>
     *
     * @return the filter template store — never null after {@link #start}.
     */
    public TemplateStore getFilterTemplateStore()
    {
        return filterTemplateStore;
    }


    // ── R2 OPENS THE TEMPLATE CONTEXT REGISTRY ────────────────────────────────
    // R2's nav computer doesn't just store routes — it knows the context each
    // route belongs to (this system, that region). The context type registry
    // is R2's index of which template group belongs where.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ContextTypeRegistry} that scopes LDAP filter templates.
     * Eclipse's template system requires each template to belong to a named
     * context type; this registry defines the context for LDAP filter strings
     * so the right templates appear in the right editor.
     *
     * <p>For example — R2 indexing nav routes by destination sector:</p>
     * <pre>
     *   ContextTypeRegistry reg = activator.getFilterTemplateContextTypeRegistry();
     *   TemplateContextType ctx = reg.getContextType( FILTER_TEMPLATE_ID );
     * </pre>
     *
     * @return the filter template context type registry — never null after {@link #start}.
     */
    public ContextTypeRegistry getFilterTemplateContextTypeRegistry()
    {
        return filterTemplateContextTypeRegistry;
    }


    // ── BASE COMMAND DISPATCHES ORDERS ON THE CORRECT CHANNEL ─────────────────
    // Yavin IV base uses a dedicated comms runner to make sure all orders reach
    // pilots on the right frequency (the UI thread). Using the wrong thread
    // to update SWT widgets causes crashes, so we always route through this runner.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EventRunner} that dispatches LDAP model events on the
     * SWT UI thread. SWT is single-threaded — any widget update must happen on
     * the display thread. The event runner ensures that model change notifications
     * are re-dispatched there automatically so callers don't have to think about it.
     *
     * <p>For example — base control routing all comms to the right pilot frequency:</p>
     * <pre>
     *   EventRunner runner = activator.getEventRunner();
     *   runner.execute( () -> viewer.refresh() );
     * </pre>
     *
     * @return the UI-thread event runner — never null after {@link #start}.
     */
    public EventRunner getEventRunner()
    {
        return eventRunner;
    }


    // ── READING THE REBEL ALLIANCE'S FOUNDING CHARTER ─────────────────────────
    // Mon Mothma keeps the Alliance's founding documents in a locked cabinet.
    // The first time someone needs them, a courier fetches them and they stay
    // in the room for easy access after. This method is that courier — it loads
    // plugin.properties lazily and caches it.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's {@link PropertyResourceBundle} (the contents of
     * {@code plugin.properties}), which holds IDs for commands, context types,
     * preference page IDs, and similar extension-point strings.
     * Loaded lazily on first access and cached thereafter.
     * Any load failure is logged to the Eclipse error log.
     *
     * <p>For example — Mon Mothma's courier fetching the founding charter:</p>
     * <pre>
     *   PropertyResourceBundle props = activator.getPluginProperties();
     *   String cmdId = props.getString( "Cmd_Delete_id" );
     * </pre>
     *
     * @return the plugin properties bundle — may be {@code null} if the file
     *         could not be read (error is logged).
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.ldapbrowser.common", Status.OK, //$NON-NLS-1$
                    "Unable to get the plugin properties.", e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
