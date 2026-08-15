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

package org.apache.directory.studio.connection.ui;


import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.event.EventRunner;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


// ── CLASS: ConnectionUIPlugin — THE FALCON'S COCKPIT ACTIVATOR ────────────────────
// When the Rebel Alliance fires up a base, someone has to flip the master power
// switch, spin up all the subsystems, and wire them together.
// ConnectionUIPlugin is that switch for the connection.ui OSGi bundle.
// On start():
//   - We create the ExceptionHandler (the emergency alarm klaxon).
//   - We create a UiThreadEventRunner so connection events fire on the SWT thread.
//   - We wire the three UI handler singletons into ConnectionCorePlugin so that
//     the headless core layer can ask the UI for passwords, referral targets, and
//     certificate trust decisions.
// On stop(): we null everything out cleanly.
// The static getDefault() follows the standard Eclipse singleton pattern so any
// class in this plugin can grab the instance without needing a reference passed in.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * OSGi bundle activator for the {@code connection.ui} plugin.
 *
 * <p>Responsibilities on startup:</p>
 * <ul>
 *   <li>Creates the {@link ExceptionHandler} for showing error dialogs.</li>
 *   <li>Creates a {@link UiThreadEventRunner} and registers it with the core plugin
 *       so connection events are delivered on the SWT UI thread.</li>
 *   <li>Registers {@link UIAuthHandler}, {@link ConnectionUIReferralHandler}, and
 *       {@link ConnectionUICertificateHandler} with {@link ConnectionCorePlugin} so
 *       the headless core can pop up dialogs when it needs user input.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionUIPlugin extends AbstractUIPlugin
{
    /** The shared singleton instance of this plugin. */
    private static ConnectionUIPlugin plugin;

    /**
     * The exception handler for displaying runtime errors to the user.
     * Wired in at {@link #start}.
     */
    private ExceptionHandler exceptionHandler;

    /**
     * The event runner that dispatches connection events on the SWT UI thread.
     * Wired in at {@link #start}.
     */
    private EventRunner eventRunner;

    /** Cached plugin properties, loaded lazily from {@code plugin.properties}. */
    private PropertyResourceBundle properties;


    // ── CONSTRUCTOR — REGISTER THE SINGLETON ──────────────────────────────────────
    /**
     * Creates the plugin and registers it as the shared singleton instance.
     * Called by the OSGi framework — do not call this directly.
     */
    public ConnectionUIPlugin()
    {
        super();
        plugin = this;
    }


    // ── START — SPIN UP ALL SUBSYSTEMS ────────────────────────────────────────────
    // The order matters: we create our own subsystems first, then we hand the UI
    // handler implementations to the core plugin so it can forward requests to us.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Starts the plugin: creates the exception handler and event runner, and
     * registers the UI auth/referral/certificate handlers with the core plugin.
     */
    @Override
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        if ( exceptionHandler == null )
        {
            exceptionHandler = new ExceptionHandler();
        }

        if ( eventRunner == null )
        {
            eventRunner = new UiThreadEventRunner();
        }

        ConnectionCorePlugin defaultPlugin = ConnectionCorePlugin.getDefault();
        defaultPlugin.setAuthHandler( new UIAuthHandler() );
        defaultPlugin.setReferralHandler( new ConnectionUIReferralHandler() );
        defaultPlugin.setCertificateHandler( new ConnectionUICertificateHandler() );
    }


    // ── STOP — SHUT DOWN ALL SUBSYSTEMS ───────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Clears the shared singleton reference and disposes all subsystems.
     */
    @Override
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );

        if ( exceptionHandler != null )
        {
            exceptionHandler = null;
        }

        if ( eventRunner != null )
        {
            eventRunner = null;
        }
    }


    // ── GET DEFAULT — THE STANDARD ECLIPSE SINGLETON ACCESSOR ─────────────────────
    /**
     * Returns the shared plugin instance.
     *
     * @return  The singleton {@link ConnectionUIPlugin}.
     */
    public static ConnectionUIPlugin getDefault()
    {
        return plugin;
    }


    // ── GET EXCEPTION HANDLER ─────────────────────────────────────────────────────
    /**
     * Returns the exception handler used to display error dialogs.
     *
     * @return  The {@link ExceptionHandler}.
     */
    public ExceptionHandler getExceptionHandler()
    {
        return exceptionHandler;
    }


    // ── GET IMAGE DESCRIPTOR — LOOK UP AN ICON BY PATH ────────────────────────────
    /**
     * Returns an {@link ImageDescriptor} for the given bundle-relative resource path,
     * or {@code null} if the resource does not exist.
     * Use the {@code IMG_} constants from {@link ConnectionUIConstants} as keys.
     *
     * @param key  The bundle-relative path to the image resource.
     * @return  An {@link ImageDescriptor}, or {@code null}.
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


    // ── GET IMAGE — LOOK UP A CACHED SWT IMAGE ────────────────────────────────────
    // We use the Eclipse ImageRegistry as a cache so we create each image at most
    // once per session.  The registry disposes all images when the plugin stops.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the SWT {@link Image} for the given bundle-relative resource path.
     * Images are cached in the plugin's {@link org.eclipse.jface.resource.ImageRegistry}
     * and automatically disposed when the plugin stops.
     *
     * <p><strong>Do not dispose the returned image yourself.</strong></p>
     *
     * @param key  The bundle-relative path to the image resource.
     * @return  The SWT {@link Image}, or {@code null} if the resource does not exist.
     */
    public Image getImage( String key )
    {
        Image image = getImageRegistry().get( key );

        if ( image == null )
        {
            ImageDescriptor imageDescriptor = getImageDescriptor( key );

            if ( imageDescriptor != null )
            {
                image = imageDescriptor.createImage();
                getImageRegistry().put( key, image );
            }
        }

        return image;
    }


    // ── GET EVENT RUNNER — THE SWT THREAD EVENT DISPATCHER ────────────────────────
    /**
     * Returns the {@link EventRunner} that dispatches connection events on the
     * SWT UI thread.
     *
     * @return  The {@link UiThreadEventRunner} created at startup.
     */
    public EventRunner getEventRunner()
    {
        return eventRunner;
    }


    // ── GET PLUGIN PROPERTIES — LOAD plugin.properties ───────────────────────────
    /**
     * Returns the plugin's {@link PropertyResourceBundle} loaded from
     * {@code plugin.properties}.  Loaded lazily and cached after the first call.
     * Logs an error and returns {@code null} if the file cannot be found.
     *
     * @return  The {@link PropertyResourceBundle}, or {@code null} on failure.
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
                // so we use a hard-coded fallback plugin id.
                getLog().log( new Status( Status.ERROR, "org.apache.directory.studio.connection.ui", Status.OK, //$NON-NLS-1$
                    Messages.getString( "ConnectionUIPlugin.UnableGetPluginProperties" ), e ) ); //$NON-NLS-1$
            }
        }

        return properties;
    }
}
