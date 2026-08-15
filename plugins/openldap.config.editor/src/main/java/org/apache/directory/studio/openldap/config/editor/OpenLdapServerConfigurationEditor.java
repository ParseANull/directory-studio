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
package org.apache.directory.studio.openldap.config.editor;


import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

import org.apache.directory.studio.common.core.jobs.StudioJob;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.openldap.config.editor.databases.ConfigPage;
import org.apache.directory.studio.openldap.config.editor.databases.FrontendPage;
import org.apache.directory.studio.openldap.config.editor.pages.DatabasesPage;
import org.apache.directory.studio.openldap.config.editor.pages.ErrorPage;
import org.apache.directory.studio.openldap.config.editor.pages.LoadingPage;
import org.apache.directory.studio.openldap.config.editor.pages.OpenLDAPServerConfigurationEditorPage;
import org.apache.directory.studio.openldap.config.editor.pages.OptionsPage;
import org.apache.directory.studio.openldap.config.editor.pages.OverviewPage;
import org.apache.directory.studio.openldap.config.editor.pages.SecurityPage;
import org.apache.directory.studio.openldap.config.editor.pages.TuningPage;
import org.apache.directory.studio.openldap.config.jobs.LoadConfigurationRunnable;
import org.apache.directory.studio.openldap.config.model.OpenLdapConfiguration;
import org.apache.directory.studio.openldap.config.model.io.SaveConfigurationRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;


// ── CLASS: OpenLdapServerConfigurationEditor — Imperial Death Star Engineering ─
// The Empire's engineering corps doesn't just build the Death Star randomly —
// they have a central command: one editor where Grand Moff Tarkin can switch
// between the superlaser schematics, the tractor-beam controls, the detention
// block layout, and the reactor ventilation systems.  Every subsystem is a
// separate tab; the editor orchestrates them all.
// This class is that command center: a multi-page Eclipse FormEditor where each
// page (Overview, Databases, Security, Tuning, Options) is a different station
// in the Death Star's control room, all sharing the same OpenLdapConfiguration
// model object.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main Eclipse editor for an OpenLDAP server configuration.
 * It extends {@link FormEditor} — a multi-page form editor — and manages the
 * full lifecycle: loading the config in the background, displaying it across
 * several themed pages (Overview, Databases, Security, Tuning, Options), and
 * saving it back to the server or directory when the user is done.
 * Think of it as Grand Moff Tarkin's command station: all the controls for
 * the Death Star's systems live here, coordinated by a single authority.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapServerConfigurationEditor extends FormEditor implements IPageChangedListener
{
    /** The Eclipse editor ID — used in plugin.xml to register this editor. */
    public static final String ID = OpenLdapServerConfigurationEditor.class.getName();

    /** Whether the editor has unsaved changes — drives the tab's dirty indicator. */
    private boolean dirty = false;

    /** The in-memory OpenLDAP configuration model we're editing. */
    private OpenLdapConfiguration configuration;

    // The pages for the Open LDAP configuration
    /** The Overview page */
    private OverviewPage overviewPage;

    /** The page which is used for loading the configuration */
    private LoadingPage loadingPage;

    /** The Frontend database page */
    private FrontendPage frontendPage;

    /** The Config database page */
    private ConfigPage configPage;

    /** The page showing the user's databases */
    private DatabasesPage databasesPage;

    /** The options page */
    private OptionsPage optionsPage;

    /** The Security page */
    private SecurityPage securityPage;

    /** The Tuning page */
    private TuningPage tuningPage;


    // ── Tarkin Takes Command Of The Station ───────────────────────────────────
    // Grand Moff Tarkin strides onto the Death Star's bridge, confirms his
    // credentials, names the mission, and immediately orders a status report.
    // We initialize the editor: set its tab name from the input, mark new
    // configs as dirty (they haven't been saved yet), register our page-change
    // listener, and kick off the background load job.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Initializes the editor with its site and input.
     * We set the tab label, start a background job to load the configuration,
     * and mark the editor dirty if this is a brand-new (unsaved) config.
     *
     * @param site   the Eclipse workbench site wiring us into the UI
     * @param input  the editor input describing where to load the config from
     * @throws PartInitException  if Eclipse can't wire up the editor part
     */
    @Override
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        super.init( site, input );
        setPartName( input.getName() );

        // Checking if the input is a new server configuration file
        // New server configuration file have a dirty state
        // set to true since they are not saved yet
        setDirty( input instanceof NewServerConfigurationInput );

        addPageChangedListener( this );

        readConfiguration();
    }


    // ── Officers Dispatch The Scout Probe ─────────────────────────────────────
    // Tarkin dispatches an Imperial probe droid to gather intelligence before
    // he makes any decisions — the droid works independently and reports back.
    // We schedule a background {@link LoadConfigurationRunnable} job so the UI
    // stays responsive while the config is fetched from the server or disk.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Schedules the background job that loads the configuration from the input.
     * We run this in a separate thread so the editor UI doesn't freeze while
     * waiting for a potentially slow LDAP server or large file.
     */
    private void readConfiguration()
    {
        // Creating and scheduling the job to load the configuration
        StudioJob<StudioRunnableWithProgress> job = new StudioJob<>(
            new LoadConfigurationRunnable( this ) );
        job.schedule();
    }


    // ── Officer Reports From A Different Station ───────────────────────────────
    // When an officer moves between stations on the bridge, Tarkin expects a
    // status update — "Sir, the tractor beam is now online."
    // When the user switches tabs, we tell the newly-visible page to refresh
    // its UI so it reflects the latest state of the shared configuration model.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the user switches to a different editor page.
     * We ask the newly-selected page to refresh its widgets from the current
     * configuration model — necessary because changes on one page affect the
     * shared model that other pages display.
     *
     * @param event  the page-change event carrying a reference to the new page
     */
    public void pageChanged( PageChangedEvent event )
    {
        Object selectedPage = event.getSelectedPage();

        if ( selectedPage instanceof OpenLDAPServerConfigurationEditorPage )
        {
            ( ( OpenLDAPServerConfigurationEditorPage ) selectedPage ).refreshUI();
        }
    }


    // ── Death Star Installs Temporary Loading Console ─────────────────────────
    // While the main weapon systems are being installed, the Death Star's
    // bridge shows a single temporary status console — "SYSTEMS INITIALIZING."
    // We add only the LoadingPage at first; the real config pages appear once
    // the load job finishes and calls back into {@link #configurationLoaded}.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse to add the initial page(s) to our multi-page editor.
     * We start with just the loading page, which shows a spinner while the
     * background job fetches the config.  The real pages are swapped in by
     * {@link #hideLoadingPageAndDisplayConfigPages()} once loading completes.
     */
    protected void addPages()
    {
        try
        {
            loadingPage = new LoadingPage( this );
            addPage( loadingPage );
        }
        catch ( PartInitException e )
        {
        }

        showOrHideTabFolder();
    }


    // ── Bridge Hides Tabs When Only One Station Is Active ─────────────────────
    // When the Death Star is still under construction, only one console is
    // operational — there's no point showing a tab bar for a single terminal.
    // We collapse the tab strip to zero height when only one page is showing
    // (the loading or error page), and restore it to normal when multiple pages
    // are present.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Shows or hides the tab folder header depending on how many pages we have.
     * With only one page (loading or error), the tab header wastes space —
     * we hide it by setting tab height to 0.  Once the real pages load, we
     * restore the header so the user can navigate between tabs.
     */
    private void showOrHideTabFolder()
    {
        Composite container = getContainer();

        if ( container instanceof CTabFolder )
        {
            CTabFolder folder = ( CTabFolder ) container;

            if ( getPageCount() == 1 )
            {
                folder.setTabHeight( 0 );
            }
            else
            {
                folder.setTabHeight( -1 );
            }

            folder.layout( true, true );
        }
    }


    // ── Tarkin Orders All Stations To Log Their State ─────────────────────────
    // Before transmitting the Death Star's status to Imperial Command, Tarkin
    // orders every station officer to file their latest readings.
    // We first ask each page to commit its current widget state to the model,
    // then fire a background save job to persist those changes.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Saves the editor — called by Eclipse when the user hits Ctrl+S or
     * clicks the save button.
     * We first flush all the editor pages' UI changes to the shared model,
     * then schedule a background job to write the model back to the server
     * or directory.
     *
     * @param monitor  the Eclipse progress monitor for reporting save progress
     */
    public void doSave( IProgressMonitor monitor )
    {
        // Saving pages
        doSavePages( monitor );

        // Saving the configuration using a job
        StudioJob<StudioRunnableWithProgress> job = new StudioJob<>(
            new SaveConfigurationRunnable( this ) );
        job.schedule();
    }


    // ── Tarkin Files A Copy In Imperial Archives ───────────────────────────────
    // When Tarkin wants a backup copy of the Death Star's specs filed in a
    // different location, he dispatches a courier with a full printout —
    // the original station keeps running while the copy is filed elsewhere.
    // We prompt the user for a new save location and write the config there,
    // then update the editor's input so it now points at the new location.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Performs a "Save As" — prompts the user for a new location and saves
     * the configuration there.
     * This runs in the Eclipse workbench window's progress service so we get
     * a progress dialog.  After saving, the editor input is updated to point
     * at the new location and the dirty flag is cleared.
     */
    public void doSaveAs()
    {
        try
        {
            getSite().getWorkbenchWindow().run( false, false, new IRunnableWithProgress()
            {
                public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        monitor.beginTask( "Saving Server Configuration", IProgressMonitor.UNKNOWN );
                        doSaveAs( monitor );
                        monitor.done();
                    }
                    catch ( Exception e )
                    {
                        // TODO handle the exception
                    }
                }
            } );
        }
        catch ( Exception e )
        {
            // TODO handle the exception
            e.printStackTrace();
        }
    }


    // ── Courier Delivers The Copy To The New Archive ──────────────────────────
    // The courier reaches the new Imperial archive, hands over the printout,
    // and files the new location back to Tarkin so future dispatches go there.
    // We flush pages, call the save utility with the user's chosen path, and
    // if successful, swap the editor input to point at the new location.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * The actual "Save As" implementation — called by {@link #doSaveAs()} inside
     * a progress runnable.
     * We flush pages, delegate to {@link OpenLdapServerConfigurationEditorUtils#saveAs}
     * to write to the new location, and if successful update the editor's input
     * and clear the dirty flag.
     *
     * @param monitor  the progress monitor to report to
     * @throws Exception  if the save fails — the caller should display an error
     */
    public void doSaveAs( IProgressMonitor monitor ) throws Exception
    {
        // Saving pages
        doSavePages( monitor );

        // Saving the configuration as a new file and getting the associated new editor input
        IEditorInput newInput = OpenLdapServerConfigurationEditorUtils.saveAs( getConfiguration(), getSite().getShell(), true );

        // Checking if the 'save as' is successful
        if ( newInput != null )
        {
            // Setting the new input to the editor
            setInput( newInput );

            // Resetting the dirty state of the editor
            setDirty( false );

            // Updating the title and tooltip texts
            Display.getDefault().syncExec( () -> setPartName( getEditorInput().getName() ) );
        }
    }


    // ── All Stations File Their Latest Readings ───────────────────────────────
    // Tarkin signals each bridge station: Weapons, report. Navigation, report.
    // Each officer updates the central log.
    // We call doSave() on every page that's been created, giving each one
    // the chance to flush its widget state into the shared configuration model.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Asks each editor page to commit its current UI state to the configuration
     * model before we write it to disk.
     * We null-check each page because some pages (like frontendPage) are only
     * created after the load job completes — they may not exist yet if something
     * went wrong during loading.
     *
     * @param monitor  the progress monitor to pass through to each page's save
     */
    private void doSavePages( IProgressMonitor monitor )
    {
        if ( databasesPage != null )
        {
            databasesPage.doSave( monitor );
        }

        if ( frontendPage != null )
        {
            frontendPage.doSave( monitor );
        }

        if ( securityPage != null )
        {
            securityPage.doSave( monitor );
        }

        if ( tuningPage != null )
        {
            tuningPage.doSave( monitor );
        }

        if ( configPage != null )
        {
            configPage.doSave( monitor );
        }
    }


    // ── Death Star's Archives Are Always Open For Copying ─────────────────────
    // Imperial Command can always request a duplicate set of schematics —
    // "Save As" is never blocked.
    // We always return true so the Eclipse "Save As" menu item is always
    // enabled for our editor.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} — we always support "Save As" for config files.
     * Eclipse calls this to know whether to enable the "Save As" menu item.
     *
     * @return {@code true} always
     */
    public boolean isSaveAsAllowed()
    {
        return true;
    }


    // ── Bridge Status Light Shows Unsaved Changes ─────────────────────────────
    // The Death Star's command console has a blinking amber light when there
    // are uncommitted changes to a station's configuration — it stays lit
    // until Tarkin formally signs off on the update.
    // We report the dirty flag so Eclipse can show the asterisk on the tab.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the editor has unsaved changes.
     * Eclipse uses this to decide whether to show the asterisk on the tab
     * and whether to prompt the user to save on close.
     *
     * @return {@code true} if there are unsaved changes
     */
    @Override
    public boolean isDirty()
    {
        return dirty;
    }


    // ── Tarkin Signs Or Clears The Pending-Changes Flag ──────────────────────
    // Tarkin either signs off on a batch of changes (clearing the amber light)
    // or marks new unsaved edits as pending (lighting it again).
    // We update the flag and fire the Eclipse PROP_DIRTY property change on the
    // UI thread so the tab's asterisk updates immediately.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the editor's dirty flag and notifies Eclipse so the UI updates.
     * We fire the notification asynchronously on the UI thread — safe to call
     * from background threads like our load/save jobs.
     *
     * @param dirty  {@code true} to mark the editor as having unsaved changes
     */
    public void setDirty( boolean dirty )
    {
        this.dirty = dirty;

        Display.getDefault().asyncExec( () -> firePropertyChange( PROP_DIRTY ) );
    }


    // ── Tarkin Reads The Current Station Schematics ───────────────────────────
    // When Tarkin wants to review the Death Star's full technical specification,
    // he calls for the current schematic bundle.
    // We expose the shared configuration model so pages and utilities can read
    // the current state.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current in-memory OpenLDAP configuration model.
     * All editor pages share this object — they read from and write to it
     * directly.  The save job serializes it to disk or to the server.
     *
     * @return the configuration model, or {@code null} if loading hasn't
     *         completed yet
     */
    public OpenLdapConfiguration getConfiguration()
    {
        return configuration;
    }


    // ── Tarkin Installs A New Set Of Schematics ───────────────────────────────
    // When updated blueprints arrive from Coruscant, Tarkin replaces the old
    // ones on the command table with the new version.
    // We swap the configuration model reference — pages that read from it
    // will get the new version on their next refresh.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the shared configuration model.
     * Called by the load job when it finishes building the model from the
     * input source.  Pages shouldn't hold references to the old model after
     * this is called.
     *
     * @param configuration  the new configuration model to display
     */
    public void setConfiguration( OpenLdapConfiguration configuration )
    {
        this.configuration = configuration;
    }


    // ── Coruscant Overrides The Current Death Star Plans ─────────────────────
    // Palpatine occasionally sends revised plans that supersede whatever
    // Tarkin had on the table — all stations must update their displays.
    // We swap the config model, mark the editor dirty (since the reset
    // represents unsaved changes), and tell every page to refresh.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Resets the editor to a new configuration and refreshes all pages.
     * This is used when the user imports or reloads a configuration —
     * we replace the model, mark the editor dirty (because this new state
     * hasn't been saved yet), and force all pages to re-read the model.
     *
     * @param configuration  the new configuration to display — replaces
     *                       whatever was there before
     */
    public void resetConfiguration( OpenLdapConfiguration configuration )
    {
        setConfiguration( configuration );

        setDirty( true );

        overviewPage.refreshUI();
        optionsPage.refreshUI();
        databasesPage.refreshUI();
        frontendPage.refreshUI();
        securityPage.refreshUI();
        tuningPage.refreshUI();
        configPage.refreshUI();
    }


    // ── Scout Droid Returns And Bridge Comes To Life ───────────────────────────
    // The probe droid's intelligence arrives at the Death Star — suddenly the
    // holo-displays light up with the target system's data and every station
    // snaps to attention.
    // The load job calls us back with the finished model; we store it and swap
    // the loading page for the real configuration pages.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by the background load job once it successfully reads the
     * configuration from the server or directory.
     * We store the model and replace the loading page with all the real
     * editor pages so the user can start working.
     *
     * @param configuration  the fully-loaded configuration model
     */
    public void configurationLoaded( OpenLdapConfiguration configuration )
    {
        setConfiguration( configuration );

        hideLoadingPageAndDisplayConfigPages();
    }


    // ── Probe Droid Sends Back A Distress Signal ──────────────────────────────
    // Sometimes the probe droid doesn't make it — all that comes back is a
    // garbled error signal.  The bridge replaces the holo-display with a red
    // alert schematic showing what went wrong.
    // The load job calls us when it fails; we swap the loading page for an
    // error page showing the exception so the user knows what happened.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called by the background load job when it fails to read the configuration.
     * We clear the dirty flag (a failed load shouldn't leave the editor in a
     * dirty state) and replace the loading page with an error page so the user
     * sees a meaningful failure message instead of a spinner.
     *
     * @param exception  the exception that caused the load to fail
     */
    public void configurationLoadFailed( Exception exception )
    {
        // Overriding the default dirty setting
        // (especially in the case of a new configuration file)
        setDirty( false );

        hideLoadingPageAndDisplayErrorPage( exception );
    }


    // ── Bridge Stations Come Online One By One ────────────────────────────────
    // After the Death Star's initial boot sequence, the loading screen goes dark
    // and the real stations light up: Weapons, Navigation, Detention, Life Support.
    // We remove the temporary loading page and add all the real configuration
    // pages in sequence, then activate the first one.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Swaps the loading page for the full set of configuration editor pages.
     * Called on the UI thread by {@link #configurationLoaded(OpenLdapConfiguration)}
     * after the background job delivers the model.
     * Pages are added in the order they should appear as tabs: Overview,
     * Databases, Security, Tuning, Options.
     */
    private void hideLoadingPageAndDisplayConfigPages()
    {
        // Removing the loading page
        removePage( 0 );

        // Adding the configuration pages
        try
        {
            overviewPage = new OverviewPage( this );
            addPage( overviewPage);

            databasesPage = new DatabasesPage( this );
            addPage( databasesPage );

            securityPage = new SecurityPage( this );
            addPage( securityPage );

            tuningPage = new TuningPage( this );
            addPage( tuningPage );

            optionsPage = new OptionsPage( this );
            addPage( optionsPage );
        }
        catch ( PartInitException e )
        {
            // Will never happen
        }

        // Activating the first page
        setActivePage( 0 );

        showOrHideTabFolder();
    }


    // ── Bridge Shows Red Alert When The Droid Fails ───────────────────────────
    // If the probe droid's signal is corrupted, the bridge doesn't just sit
    // there showing a spinner — a big red alert screen replaces it explaining
    // what went wrong.
    // We remove the loading page and add an ErrorPage that surfaces the
    // exception for the user.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Swaps the loading page for an error page when configuration loading fails.
     * Called on the UI thread by
     * {@link #configurationLoadFailed(Exception)}.
     * The error page surfaces the exception message so the user has something
     * actionable to look at.
     *
     * @param exception  the exception to display on the error page
     */
    private void hideLoadingPageAndDisplayErrorPage( Exception exception )
    {
        // Removing the loading page
        removePage( 0 );

        // Adding the error page
        try
        {
            addPage( new ErrorPage( this, exception ) );
        }
        catch ( PartInitException e )
        {
        }

        // Activating the first page
        setActivePage( 0 );

        showOrHideTabFolder();
    }


    // ── Tarkin Navigates Directly To A Specific Station ───────────────────────
    // Tarkin strides past all the other bridge stations directly to the Weapons
    // console when he needs to authorize a firing sequence.
    // We scan the pages list for one matching the given class and make it
    // active — useful when code wants to jump the user to a specific tab.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Activates the editor page whose class matches {@code pageClass}.
     * Other parts of the plugin call this to programmatically switch the
     * user to a specific tab — for example, jumping to the Databases page
     * after adding a new database entry.
     * Does nothing if no page of that class is found.
     *
     * @param pageClass  the class of the page to activate — must not be null
     */
    public void showPage( Class<?> pageClass )
    {
        if ( pageClass != null )
        {
            Enumeration<?> enumeration = pages.elements();

            while ( enumeration.hasMoreElements() )
            {
                Object page = enumeration.nextElement();

                if ( pageClass.isInstance( page ) )
                {
                    setActivePage( pages.indexOf( page ) );
                    return;
                }
            }
        }
    }


    // ── Tarkin Identifies Which Ship Docked At The Death Star ─────────────────
    // Tarkin checks the manifest to see which ship brought the prisoners —
    // the Tantive IV, perhaps, or another Rebel vessel in disguise.
    // We check whether our editor input is a connection-backed input; if so,
    // we return the live connection so callers (like the save job) can use it.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection backing this editor, if any.
     * Returns {@code null} if this editor was opened from a directory or a
     * new (unsaved) config rather than a live connection.
     * The save job uses this to write modifications back to the live server.
     *
     * @return the connection, or {@code null} for non-connection-backed inputs
     */
    public Connection getConnection()
    {
        IEditorInput editorInput = getEditorInput();

        if ( editorInput instanceof ConnectionServerConfigurationInput )
        {
            return ( ( ConnectionServerConfigurationInput ) editorInput ).getConnection();
        }

        return null;
    }
}
