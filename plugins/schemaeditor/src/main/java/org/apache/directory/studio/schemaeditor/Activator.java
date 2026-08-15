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
package org.apache.directory.studio.schemaeditor;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;

import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandlerListener;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaChecker;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.schema.SchemaEditor;
import org.apache.directory.studio.schemaeditor.view.widget.SchemaCodeScanner;
import org.apache.directory.studio.schemaeditor.view.widget.SchemaTextAttributeProvider;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: Activator — Han Solo Punches the Falcon Into Hyperspace ────────────
// Han sits in the Millennium Falcon cockpit and slams the hyperdrive lever forward:
// engines roar, stars blur, and the ship leaps into lightspeed — everything alive
// at once. When they drop back to realspace, systems spin down and the crew gets
// to work. Our Activator is Han — it fires up the plugin when OSGi says "go" and
// powers everything down cleanly when OSGi says "stop."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The OSGi bundle activator that manages the Schema Editor plugin's lifecycle.
 * It starts and stops all the core services (handlers, checker, projects) when
 * Eclipse loads or unloads the plugin bundle. Think of this class as Han Solo
 * at the Falcon's controls — it's the single point that brings everything online
 * and keeps the ship running until it's time to shut down.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Activator extends AbstractUIPlugin
{
    /** The shared instance */
    private static Activator plugin;

    /** the Schema Code Scanner */
    private ITokenScanner schemaCodeScanner;

    /** The Schema Text Attribute Provider */
    private SchemaTextAttributeProvider schemaTextAttributeProvider;

    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    /** The SchemaCheker */
    private SchemaChecker schemaChecker;

    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;

    /** The plugin properties */
    private PropertyResourceBundle properties;


    // ── Han Settles Into The Pilot's Seat ────────────────────────────────────────
    // Han drops into the Falcon's cockpit and flips switches to power up the core
    // systems before the jump — navicomputer, deflector shields, hyperdrive motivator.
    // We do the same here: wire up the singleton reference and initialize the two
    // central services (ProjectsHandler and SchemaChecker) so they're ready before
    // start() fires.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of Activator and wires up the core singleton services.
     * OSGi calls this constructor once when the bundle is loaded, so we use it to
     * grab the singleton handles for ProjectsHandler and SchemaChecker right away.
     * The plugin field is set here so getDefault() works as soon as OSGi instantiates us.
     */
    public Activator()
    {
        plugin = this;
        projectsHandler = ProjectsHandler.getInstance();
        schemaChecker = SchemaChecker.getInstance();
    }


    // ── Han Vents The Docking Bay Before Departure ────────────────────────────────
    // Before jumping to hyperspace, Han makes sure the docking clamps are released
    // and nothing is still plugged in — he doesn't want to drag a fuel line across
    // the galaxy. Closing the schema editors is exactly that: detaching the open
    // tabs so nothing holds stale references to the old project.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and closes all open Schema Editor tabs (attribute type, object class, and
     * schema editors) in the active workbench page. We need this whenever the active
     * project changes, because each editor holds a reference to that project's schema
     * data — leaving them open after a project switch would show the wrong data.
     * We ask nicely first (prompt to save), then force-close if the user declined.
     */
    private void closeProjectEditors()
    {
        // Listing all the editors from the Schema Editor Plugin.
        List<IEditorReference> editors = new ArrayList<IEditorReference>();
        for ( IEditorReference editorReference : getWorkbench().getActiveWorkbenchWindow().getActivePage()
            .getEditorReferences() )
        {
            if ( ( editorReference.getId().equals( AttributeTypeEditor.ID ) )
                || ( editorReference.getId().equals( ObjectClassEditor.ID ) )
                || ( editorReference.getId().equals( SchemaEditor.ID ) ) )
            {
                editors.add( editorReference );
            }
        }

        // Closing the opened editors
        if ( !getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditors(
            editors.toArray( new IEditorReference[0] ), true ) )
        {
            // If all the editors have not been closed, we force them to be closed.
            getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditors(
                editors.toArray( new IEditorReference[0] ), false );
        }
    }


    // ── Han Pulls The Hyperdrive Lever — We're Gone ───────────────────────────────
    // "Punch it, Chewie!" — Han slams the lever and the Falcon leaps into hyperspace.
    // Every system comes online simultaneously: shields, navigation, life support.
    // Our start() is that moment — we load saved projects, wire up the listener that
    // tracks project changes, and make sure the schema checker is ready to validate.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when the plugin bundle activates; this is our "engines on" moment.
     * We load any previously saved projects from disk and register a listener on the
     * ProjectsHandler so we react to project switches by closing stale editors and
     * reloading the schema checker. Every save/close/open of a project also triggers
     * a disk-persist so nothing is lost.
     *
     * @param context  the OSGi bundle context provided by the container — we pass it
     *                 straight to the superclass and don't use it directly ourselves.
     * @throws Exception  if the superclass start fails; OSGi will treat this as a
     *                    bundle activation failure.
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        // Loading the projects
        PluginUtils.loadProjects();

        projectsHandler.addListener( new ProjectsHandlerListener()
        {
            public void openProjectChanged( Project oldProject, Project newProject )
            {
                closeProjectEditors();

                if ( newProject == null )
                {
                    schemaHandler = null;
                }
                else
                {
                    schemaHandler = newProject.getSchemaHandler();
                }

                schemaChecker.reload();

                PluginUtils.saveProjects();
            }


            public void projectAdded( Project project )
            {
                PluginUtils.saveProjects();
            }


            public void projectRemoved( Project project )
            {
                PluginUtils.saveProjects();
            }
        } );
    }


    // ── The Falcon Drops Out Of Hyperspace — Engines Wind Down ───────────────────
    // As the Falcon decelerates back to realspace, Han throttles back and powers down
    // non-essential systems in an orderly sequence. We mirror that here: persist the
    // projects to disk before calling super.stop(), so nothing is lost when the bundle
    // is evicted from the OSGi container.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by OSGi when the plugin bundle is being stopped; this is our "engines off"
     * moment. We save all open projects to disk before handing control back to the
     * superclass, so the user's work survives the shutdown. After this returns, the
     * shared instance is cleared so nobody gets a stale reference.
     *
     * @param context  the OSGi bundle context — passed straight to the superclass.
     * @throws Exception  if the superclass stop fails.
     */
    public void stop( BundleContext context ) throws Exception
    {
        // Saving the projects
        PluginUtils.saveProjects();

        super.stop( context );
        plugin = null;
    }


    // ── Chewie Points To The Navicomputer Readout ─────────────────────────────────
    // Any crew member who needs to check the ship's status looks at the same central
    // display — there's only one navicomputer on the Falcon. getDefault() is that
    // single display: every part of the plugin calls it to get the one shared Activator
    // instance rather than creating their own.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the single shared instance of this Activator that OSGi created for us.
     * This is the standard Eclipse pattern for accessing plugin-wide state — everyone
     * calls this rather than keeping their own reference. Returns null if the plugin
     * has not been started yet or has already been stopped.
     *
     * @return  the shared Activator instance, or null if the plugin is not running.
     */
    public static Activator getDefault()
    {
        return plugin;
    }


    // ── Han Checks The Hyperdrive Motivator Status ─────────────────────────────────
    // Han reaches over and reads the hyperdrive motivator gauge — it tells him whether
    // the ship's core drive system is healthy. The SchemaHandler is our motivator: it
    // tracks everything about the currently open project's schema, and getSchemaHandler
    // is how the rest of the plugin reads that gauge.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SchemaHandler for the currently active project, or null if no project
     * is open. The SchemaHandler is the central registry for all attribute types and
     * object classes in the active schema — editors and views use it to read and write
     * schema elements. It is swapped out automatically when the user switches projects.
     *
     * @return  the active SchemaHandler, or null when no project is currently open.
     */
    public SchemaHandler getSchemaHandler()
    {
        return schemaHandler;
    }


    // ── Han Glances At The Shield Generator Readout ───────────────────────────────
    // Han wants to know whether the deflector shields are nominal — one glance at the
    // panel gives him the answer. The SchemaChecker is our shield generator: it
    // continuously scans the schema for errors and warnings, and getSchemaChecker is
    // the panel that tells the rest of the plugin what it found.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SchemaChecker singleton that validates the active schema for errors
     * and warnings. Views like the Problems View use this to know what to display.
     * The checker reloads automatically when the active project changes.
     *
     * @return  the shared SchemaChecker instance — never null once the plugin has started.
     */
    public SchemaChecker getSchemaChecker()
    {
        return schemaChecker;
    }


    // ── Chewie Monitors The Docking Bay Control Panel ─────────────────────────────
    // Chewie keeps an eye on the bay status board that tracks which ships are docked
    // and which have launched. The ProjectsHandler is that board: it knows every
    // project the user has loaded and which one is currently "open."
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ProjectsHandler singleton that manages the list of all known schema
     * projects and tracks which one is currently open. Controllers and views use this
     * to enumerate projects and react to the user opening or closing one.
     *
     * @return  the shared ProjectsHandler instance — never null once the plugin has started.
     */
    public ProjectsHandler getProjectsHandler()
    {
        return projectsHandler;
    }


    // ── R2 Patches Into The Falcon's Targeting Computer ───────────────────────────
    // R2-D2 slots into the X-wing's astromech socket and the targeting computer lights
    // up — the same scanner that reads and highlights token patterns during combat.
    // Our SchemaCodeScanner does the same for schema text: it tokenizes the schema
    // syntax so the editor can color-highlight keywords and values.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared ITokenScanner used to syntax-highlight schema text in the
     * editor. We lazy-initialize it on first call to avoid creating it when no editor
     * is open. The scanner is backed by the SchemaTextAttributeProvider for its color
     * and font rules.
     *
     * @return  the shared SchemaCodeScanner — created on first call, reused thereafter.
     */
    public ITokenScanner getSchemaCodeScanner()
    {
        if ( schemaCodeScanner == null )
        {
            schemaCodeScanner = new SchemaCodeScanner( getSchemaTextAttributeProvider() );
        }

        return schemaCodeScanner;
    }


    // ── R2 Calibrates The Display Brightness Settings ─────────────────────────────
    // R2 taps into the cockpit display bus and nudges the contrast and color balance
    // so the readouts are legible in any lighting. The SchemaTextAttributeProvider is
    // that calibration layer — it defines the colors and fonts that the code scanner
    // uses when highlighting schema syntax.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared SchemaTextAttributeProvider that supplies color and font rules
     * to the schema code scanner. Lazy-initialized just like the scanner itself. This
     * is private because callers should go through getSchemaCodeScanner() rather than
     * talking to the provider directly.
     *
     * @return  the shared SchemaTextAttributeProvider — created on first call.
     */
    private SchemaTextAttributeProvider getSchemaTextAttributeProvider()
    {
        if ( schemaTextAttributeProvider == null )
        {
            schemaTextAttributeProvider = new SchemaTextAttributeProvider();
        }

        return schemaTextAttributeProvider;
    }


    // ── Han Pulls A Schematic From The Falcon's Parts Locker ─────────────────────
    // Han needs a replacement coupling but doesn't want to drag the whole crate of
    // parts to the engine room — he grabs the schematic card that describes exactly
    // what the part looks like and sends someone to fetch it. An ImageDescriptor is
    // exactly that schematic card: a lightweight description of an image that lets
    // SWT create the actual pixels only when needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an ImageDescriptor (a lightweight image spec, not the actual pixels) for
     * the given resource path. Use the IMG_ constants from PluginConstants for the key.
     * Returns null if the key is null or no resource exists at that path — callers
     * should handle null rather than assuming the image is always present.
     *
     * @param key  the relative path to the image inside the plugin bundle (e.g.
     *             {@code PluginConstants.IMG_SCHEMA}). Must not be null.
     * @return     the ImageDescriptor, or null if the resource was not found.
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


    // ── Han Retrieves A Spare Part From The Manifest ──────────────────────────────
    // Han doesn't re-order parts he already has in the hold — he checks the manifest
    // first, and if the part is there he grabs it; if not, he creates a work order and
    // logs it. Our ImageRegistry is that manifest: it caches SWT Images by key so we
    // create each image only once per session.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT Image for the given resource path, creating and caching it if
     * this is the first request for that key. Use the IMG_ constants from PluginConstants
     * for the key. Do NOT dispose the returned image — it lives in the ImageRegistry and
     * will be disposed automatically when the plugin stops.
     *
     * @param key  the relative path to the image inside the plugin bundle (e.g.
     *             {@code PluginConstants.IMG_ATTRIBUTE_TYPE}).
     * @return     the SWT Image, or null if no resource exists at that path.
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


    // ── Han Reads The Falcon's Operating Manual ───────────────────────────────────
    // The Falcon's maintenance manual lives in a locker in the hold — Han fetches it
    // the first time he needs a spec, then keeps it on the console for quick reference.
    // Our plugin.properties file is that manual: it holds IDs and metadata the plugin
    // needs at runtime, and we load it once and cache it in the properties field.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the plugin's property bundle loaded from {@code plugin.properties}, which
     * carries runtime IDs for views, editors, commands, and preference pages. We
     * lazy-load it on first access and cache it; if loading fails we log an error and
     * return null, so callers must handle null. This is typically the root cause when
     * you see "Could not load plugin properties" in the error log.
     *
     * @return  the PropertyResourceBundle for this plugin, or null if the file could
     *          not be read from the bundle.
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
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.schemaeditor", Status.OK, //$NON-NLS-1$
                    Messages.getString( "Activator.UnablePluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
