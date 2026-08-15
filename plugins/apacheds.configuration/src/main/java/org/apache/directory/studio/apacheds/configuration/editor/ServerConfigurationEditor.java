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
package org.apache.directory.studio.apacheds.configuration.editor;


import java.lang.reflect.InvocationTargetException;

import org.apache.directory.server.config.ConfigWriter;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.jobs.LoadConfigurationRunnable;
import org.apache.directory.studio.apacheds.configuration.jobs.SaveConfigurationRunnable;
import org.apache.directory.studio.common.core.jobs.StudioJob;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
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


// ── CLASS: ServerConfigurationEditor — THE DEATH STAR COMMAND CENTER ─────────
// Grand Moff Tarkin surveys every workstation on the Death Star's command deck —
// six panels, one unified view of the station's status and configuration.
// This class is that command center: a multi-page FormEditor where each tab is
// a workstation, and we're the officer who keeps them all in sync, loads the
// config from disk (or a live connection), and saves it back when asked.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The central multi-page editor for ApacheDS server configuration.
 * It hosts six tabs — Overview, LDAP/LDAPS, Kerberos, Partitions,
 * Password Policies, and Replication — and manages the full lifecycle of
 * loading, displaying, and saving the server configuration.
 * Think of this class as the Death Star command center: six workstations
 * (pages), one officer (this class) keeping the whole station running.
 * This editor expose 6 pages into a form with 6 tags :
 * <ul>
 * <li>Overview : the basic configuration</li>
 * <li>LDAP/LDAPS : the configuration for the LDAP/S server</li>
 * <li>Kerberos : the configuration for the Kerberos server</li>
 * <li>Partitions : The partitions configuration</li>
 * <li>PasswordPolicy : The password policy configuration</li>
 * <li>Replication : The replicationconfiguration</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerConfigurationEditor extends FormEditor implements IPageChangedListener
{
    /** The Editor ID */
    public static final String ID = ServerConfigurationEditor.class.getName();

    /** The flag indicating if the editor is dirty */
    private boolean dirty = false;

    /** The configuration including bean and underlying parttiton */
    private Configuration configuration;

    /** The pages */
    private LoadingPage loadingPage;
    private OverviewPage overviewPage;
    private LdapLdapsServersPage ldapLdapsServersPage;
    private KerberosServerPage kerberosServerPage;
    private PartitionsPage partitionsPage;
    private PasswordPoliciesPage passwordPolicyPage;
    private ReplicationPage replicationPage;




    // ── Powering Up The Command Deck ──────────────────────────────────────────
    // Admiral Ozzel strides onto the bridge and takes his post, initialising
    // every display panel before the crew gets to work.
    // We do the same: register the editor with its site, name it after the
    // input file, flag brand-new configs as dirty right away, and kick off
    // the background job to load the config data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the editor — wires it into the Eclipse workbench, names it
     * after its input file, and fires off the background job to load the config.
     * If the input is a brand-new (unsaved) configuration, we immediately mark
     * the editor dirty so Eclipse knows to prompt for a save on close.
     *
     * @param site   the editor site Eclipse provides
     * @param input  the file or connection the editor is opening
     * @throws PartInitException  if Eclipse can't register the editor site
     */
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        super.init( site, input );
        setPartName( input.getName() );

        // Checking if the input is a new server configuration file
        if ( input instanceof NewServerConfigurationInput )
        {
            // New server configuration file have a dirty state
            // set to true since they are not saved yet
            setDirty( true );
        }

        addPageChangedListener( this );

        readConfiguration();
    }


    // ── Scanning The Sector For Intelligence ──────────────────────────────────
    // R2-D2 slots into the X-wing's navicomputer and starts pulling coordinates
    // from the server before anyone has even sat in the cockpit.
    // We create and schedule a LoadConfigurationRunnable job here — config
    // parsing happens in the background so the UI stays responsive.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Spins up a background job to load the server config from disk or a
     * live LDAP connection — whichever the editor input points to.
     * The job calls back into {@link #configurationLoaded} or
     * {@link #configurationLoadFailed} when it finishes.
     */
    private void readConfiguration()
    {
        // Creating and scheduling the job to load the configuration
        StudioJob<StudioRunnableWithProgress> job = new StudioJob<StudioRunnableWithProgress>(
            new LoadConfigurationRunnable( this ) );
        job.schedule();
    }


    // ── Officer On Deck Reports In ────────────────────────────────────────────
    // A new officer steps up to their workstation on the command deck and the
    // displays instantly update to show the latest tactical picture.
    // We call refreshUI() on whichever page just became active so it always
    // shows fresh data from the current config bean.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the user switches tabs.
     * We tell the newly-selected page to refresh its UI from the config bean
     * so it's always showing the latest values — even if another page changed
     * something while this one was hidden.
     *
     * @param event  the page-change event carrying the newly-selected page object
     */
    public void pageChanged( PageChangedEvent event )
    {
        Object selectedPage = event.getSelectedPage();

        if ( selectedPage instanceof ServerConfigurationEditorPage )
        {
            ( ( ServerConfigurationEditorPage ) selectedPage ).refreshUI();
        }
    }


    // ── Assembling The Battle Station ─────────────────────────────────────────
    // Imperial engineers bolt the loading bay into the Death Star frame first —
    // a placeholder while the real systems finish powering up behind the scenes.
    // We add just a LoadingPage here; the real config pages arrive once the
    // background load job finishes and calls us back.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the initial pages to the editor.
     * We only add the {@link LoadingPage} right now — a "please wait" screen —
     * and the real config pages get swapped in by
     * {@link #hideLoadingPageAndDisplayConfigPages()} once the config is ready.
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


    // ── One Tab Means No Tab ──────────────────────────────────────────────────
    // While the Death Star is still under construction there's only one chamber —
    // no need to label it, everyone knows where they are.
    // When we have exactly one page (the loading screen), we collapse the tab
    // bar to zero height so it doesn't look weird; with multiple pages we
    // restore it to its natural height.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Shows or hides the tab strip depending on how many pages are loaded.
     * With a single page (the loading screen) the tab strip is pointless and
     * visually noisy, so we zero its height. With multiple pages we restore it.
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


    // ── Transmitting The Battle Plans ─────────────────────────────────────────
    // General Dodonna relays the Death Star attack coordinates to every
    // starfighter — pages are confirmed ready first, then the master plan is
    // written out to the target.
    // We flush all pages that need pre-save work, then schedule a
    // SaveConfigurationRunnable job to write the config back to its source.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves the editor. Flushes any page-specific state (e.g., partition edits)
     * and then launches a background job to persist the config to its source
     * (a local file or a live LDAP connection).
     *
     * @param monitor  Eclipse progress monitor for the save operation
     */
    public void doSave( IProgressMonitor monitor )
    {
        // Saving pages
        doSavePages( monitor );

        // Saving the configuration using a job
        StudioJob<StudioRunnableWithProgress> job = new StudioJob<StudioRunnableWithProgress>(
            new SaveConfigurationRunnable( this ) );
        job.schedule();
    }


    // ── Copying The Plans To A New Disk ──────────────────────────────────────
    // Leia copies the Death Star plans to a new data chip and hands it to R2-D2
    // — the original stays on the ship, the new copy heads out into the galaxy.
    // We wrap the real save-as logic in an IRunnableWithProgress so Eclipse can
    // show a spinner while we work through the file dialog and write.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Eclipse's entry point for the "File &rarr; Save As..." action.
     * Wraps {@link #doSaveAs(IProgressMonitor)} in a progress runner so the
     * user sees a busy indicator while we write the config to its new location.
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
                        monitor.beginTask(
                            Messages.getString( "ServerConfigurationEditor.SavingServerConfiguration" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$
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


    // ── Filing The New Co-ordinates ────────────────────────────────────────────
    // Once Leia's chip reaches the Rebel base, it's logged under a new reference
    // number — the old one is retired and the new path is official from now on.
    // We flush pages, delegate the actual write to ServerConfigurationEditorUtils,
    // then update our input and clear the dirty flag if all went well.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * The real "Save As" implementation — flushes pages, opens a file picker,
     * writes the config to the chosen path, then rewires the editor to point
     * at the new file so future saves go there automatically.
     *
     * @param monitor  progress monitor for the save
     * @return {@code true} if the user picked a path and the write succeeded
     * @throws Exception  if writing the config file fails
     */
    public boolean doSaveAs( IProgressMonitor monitor ) throws Exception
    {
        // Saving pages
        doSavePages( monitor );

        // Saving the configuration as a new file and getting the associated new editor input
        IEditorInput newInput = ServerConfigurationEditorUtils.saveAs( monitor, getSite().getShell(),
            getEditorInput(), getConfigWriter(), getConfiguration(), true );

        // Checking if the 'save as' is successful
        boolean success = newInput != null;

        if ( success )
        {
            // Setting the new input to the editor
            setInput( newInput );

            // Resetting the dirty state of the editor
            setDirty( false );

            // Updating the title and tooltip texts
            Display.getDefault().syncExec( new Runnable()
            {
                public void run()
                {
                    setPartName( getEditorInput().getName() );
                }
            } );
        }

        return success;
    }


    // ── Making Sure Everyone Filed Their Report ────────────────────────────────
    // Before the Death Star can fire, every station officer must confirm
    // readiness — the Partitions page in particular needs to flush any pending
    // structural edits before the main save job runs.
    // We call doSave() on the partitions page (on the UI thread) to commit any
    // in-progress changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Asks each page that needs special pre-save work to flush its state.
     * Currently only the partitions page requires this — it manages its own
     * internal edits that aren't automatically reflected in the config bean
     * until it gets a chance to commit them.
     *
     * @param monitor  progress monitor passed through to each page's save
     */
    private void doSavePages( final IProgressMonitor monitor )
    {
        if ( partitionsPage != null )
        {
            Display.getDefault().syncExec( new Runnable()
            {
                public void run()
                {
                    partitionsPage.doSave( monitor );
                }
            } );
        }
    }


    // ── The Plans Can Always Leave The Station ────────────────────────────────
    // The Empire wants those plans on every Star Destroyer — copies are always
    // allowed, no questions asked.
    // We unconditionally return true so Eclipse enables the "Save As..." menu
    // item for this editor type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tells Eclipse that "Save As..." is always allowed for this editor.
     * We always return {@code true} — config files can always be saved to
     * a different location.
     *
     * @return always {@code true}
     */
    public boolean isSaveAsAllowed()
    {
        return true;
    }


    // ── Is The Station Still Under Construction? ──────────────────────────────
    // An Imperial inspector checks whether the Death Star still has unsaved
    // blueprints on the drafting table before signing off on final completion.
    // We return our dirty flag so Eclipse knows whether to show the save prompt
    // when the user tries to close the editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether the editor has unsaved changes.
     * Eclipse calls this constantly to decide whether to enable the save action
     * and show the asterisk (*) in the editor tab title.
     *
     * @return {@code true} if there are unsaved changes
     */
    public boolean isDirty()
    {
        return dirty;
    }


    // ── Raising The Alert Status ──────────────────────────────────────────────
    // When a gunner fires the superlaser, the station goes to battle stations —
    // every display on the command deck updates to show combat readiness.
    // We flip our dirty flag and fire a PROP_DIRTY property change on the UI
    // thread so Eclipse updates the save button and title asterisk immediately.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the editor's dirty (unsaved-changes) flag and notifies Eclipse.
     * The notification goes out on the UI thread via asyncExec so this method
     * is safe to call from background jobs without causing thread violations.
     *
     * @param dirty  {@code true} to mark the editor as having unsaved changes
     */
    public void setDirty( boolean dirty )
    {
        this.dirty = dirty;

        Display.getDefault().asyncExec( new Runnable()
        {
            public void run()
            {
                firePropertyChange( PROP_DIRTY );
            }
        } );
    }


    // ── Pulling Up The Station Schematics ─────────────────────────────────────
    // An engineer at the command console calls up the Death Star's full technical
    // readout — every beam emitter, every exhaust port, everything in one object.
    // We hand back our configuration object so pages and utilities can read or
    // modify the config bean stored inside it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current server configuration.
     * The {@link Configuration} wraps the ApacheDS config bean plus the
     * underlying LDAP partition it was loaded from.
     *
     * @return the loaded configuration, or {@code null} if not yet loaded
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }


    // ── Installing New Station Software ──────────────────────────────────────
    // Imperial technicians upload a fresh firmware image to the Death Star's
    // targeting computer — the old settings are quietly overwritten.
    // We store the new configuration so all pages will use it on their next
    // refresh cycle.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the current configuration with a new one.
     * Normally called by the background load job via {@link #configurationLoaded},
     * but can also be used to inject a freshly-reset config.
     *
     * @param configuration  the new configuration to store
     */
    public void setConfiguration( Configuration configuration )
    {
        this.configuration = configuration;
    }


    // ── Rebooting All Station Systems ─────────────────────────────────────────
    // Darth Vader orders a full station reset — every workstation clears its
    // old display and re-renders from the updated tactical database.
    // We store the new config, mark the editor dirty, then tell every page to
    // refresh so the UI instantly reflects the changes across all six tabs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Swaps in a new configuration and forces every page to re-render.
     * Used when the user picks "Reset to defaults" or imports a new config —
     * we want all six pages to redisplay from the fresh bean immediately
     * rather than waiting for the user to click each tab.
     *
     * @param configuration  the replacement configuration to apply
     */
    public void resetConfiguration( Configuration configuration )
    {
        setConfiguration( configuration );

        setDirty( true );

        overviewPage.refreshUI();
        ldapLdapsServersPage.refreshUI();
        kerberosServerPage.refreshUI();
        partitionsPage.refreshUI();
        passwordPolicyPage.refreshUI();
        replicationPage.refreshUI();
    }


    // ── Hyperspace Jump Successful ────────────────────────────────────────────
    // The navicomputer reports "jump complete" and the crew rushes to their
    // stations — the mission can now begin in earnest.
    // The background load job calls us here when it finishes successfully; we
    // stash the config and replace the loading screen with the real pages.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the background load job when the config has been read
     * successfully from its source (file or live connection).
     * We store the configuration and swap the loading screen for the real
     * six-page editor layout.
     *
     * @param configuration  the freshly loaded configuration
     */
    public void configurationLoaded( Configuration configuration )
    {
        setConfiguration( configuration );

        hideLoadingPageAndDisplayConfigPages();
    }


    // ── The Hyperdrive Is Down ────────────────────────────────────────────────
    // The Millennium Falcon's motivator fails mid-jump — Han bangs on the
    // console and the crew gets an error panel instead of their destination.
    // The load job calls us here when it hits an error; we clear the dirty flag
    // (there's nothing to save yet) and hand off to the error page display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by the background load job when it could not read the config.
     * We clear the dirty flag (a failed load means there's nothing new to
     * save) and display an error page with the exception details so the user
     * knows what went wrong.
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


    // ── The Loading Bay Doors Open ────────────────────────────────────────────
    // The Death Star's loading bay doors finally slide open — the real hangar
    // with all six TIE fighter bays is revealed behind them.
    // We yank the loading page out, replace it with all six real config pages,
    // then jump to page zero (Overview) so the user lands somewhere sensible.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the loading spinner page and installs the real configuration
     * pages — Overview, LDAP/LDAPS, Kerberos, Partitions, Password Policies,
     * and Replication — then activates the first one.
     */
    private void hideLoadingPageAndDisplayConfigPages()
    {
        // Removing the loading page
        removePage( 0 );

        // Adding the configuration pages
        try
        {
            overviewPage = new OverviewPage( this );
            addPage( overviewPage );
            ldapLdapsServersPage = new LdapLdapsServersPage( this );
            addPage( ldapLdapsServersPage );
            kerberosServerPage = new KerberosServerPage( this );
            addPage( kerberosServerPage );
            partitionsPage = new PartitionsPage( this );
            addPage( partitionsPage );
            passwordPolicyPage = new PasswordPoliciesPage( this );
            addPage( passwordPolicyPage );
            replicationPage = new ReplicationPage( this );
            addPage( replicationPage );
        }
        catch ( PartInitException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Activating the first page
        setActivePage( 0 );

        showOrHideTabFolder();
    }


    // ── Closing The Blast Doors On A Disaster ─────────────────────────────────
    // When something catastrophic goes wrong in the reactor bay, the blast doors
    // slam shut and an alarm panel replaces the normal status display.
    // We ditch the loading screen and show an ErrorPage so the user sees the
    // exact exception and can decide how to respond.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the loading page and replaces it with an {@link ErrorPage}
     * that displays the exception to the user.
     * The error page gives enough detail for the user (or a developer) to
     * understand what went wrong and whether the file can be recovered.
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


    // ── Switching To A Specific Workstation ───────────────────────────────────
    // Tarkin wants to see the superlaser controls — the duty officer finds that
    // workstation by name and brings it to the front of the main display.
    // We walk the pages list to find the first page that is an instance of the
    // requested class and make it active.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Makes the page matching the given class the active (visible) tab.
     * Useful for programmatic navigation — e.g., jumping straight to the
     * Partitions page from a link on the Overview page.
     *
     * @param pageClass  the class of the page to show; no-op if {@code null}
     *                   or if no page of that class is found
     */
    public void showPage( Class<?> pageClass )
    {
        if ( pageClass != null )
        {
            for ( Object page : pages )
            {
                if ( pageClass.isInstance( page ) )
                {
                    setActivePage( pages.indexOf( page ) );

                    return;
                }
            }
        }
    }


    // ── Loading The Torpedo Firing Solution ───────────────────────────────────
    // The targeting computer assembles the precise firing solution from the
    // Death Star's current configuration data before committing to a shot.
    // We build a ConfigWriter from the current schema manager and config bean
    // so callers can serialise the in-memory config to LDIF entries.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns a {@link ConfigWriter} for the current configuration.
     * The ConfigWriter knows how to turn the in-memory config bean into LDIF
     * entries that can be written to a file or pushed to a live LDAP server.
     *
     * @return a freshly built ConfigWriter loaded with the current config bean
     * @throws Exception  if the schema manager is not available
     */
    public ConfigWriter getConfigWriter() throws Exception
    {
        return new ConfigWriter( ApacheDS2ConfigurationPlugin.getDefault().getSchemaManager(),
            configuration.getConfigBean() );
    }
}
