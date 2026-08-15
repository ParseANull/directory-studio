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
package org.apache.directory.studio.ldapservers.wizards;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.ldapservers.LdapServerAdapterExtensionsManager;
import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterConfigurationPage;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: NewServerWizard — THE IMPERIAL REQUISITION PROCESS FOR A NEW INSTALLATION ─────
// When an Imperial officer wants a new LDAP installation, they open the requisition wizard.
// Step 1: pick the server type from the catalogue (which vendor, which model).
// Step 2: if that server type has configuration options, fill in the engineering form.
// Step 3: hit "Finish" — the Empire creates the installation folder and provisions the server.
// This wizard handles that entire requisition flow.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The "New Server" wizard — a JFace {@link Wizard} that walks the user through creating a new
 * LDAP server instance.
 * <ol>
 *   <li>Page 1 ({@link NewServerWizardSelectionPage}): pick the adapter type and enter a name.</li>
 *   <li>Page 2 (optional {@link NewServerWizardConfigurationPage}): adapter-specific settings.</li>
 * </ol>
 * On Finish: creates the server object, adds it to {@link LdapServersManager}, creates the
 * on-disk folder, and calls the adapter's {@code add()} method via a progress dialog.
 * Think of it as the Imperial requisition process for commissioning a new LDAP installation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewServerWizard extends Wizard implements INewWizard
{
    /** The wizard page */
    private NewServerWizardSelectionPage adapterSelectionPage;

    private Map<String, NewServerWizardConfigurationPage> configurationPages = new HashMap<String, NewServerWizardConfigurationPage>();


    // ── Loading The Requisition Form Pages ───────────────────────────────────────────────────
    // The wizard asks the registry for every installed adapter and pre-creates a configuration
    // page for each one that has one.  These pages are added lazily — only the page for the
    // selected adapter type becomes visible during the wizard run.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the selection page (always present) and one configuration page per adapter that
     * declares a configuration-page class.
     * Configuration pages are pre-created so the wizard can navigate to them without delay.
     */
    public void addPages()
    {
        adapterSelectionPage = new NewServerWizardSelectionPage();
        addPage( adapterSelectionPage );

        List<LdapServerAdapterExtension> ldapServerAdapterExtensions = LdapServerAdapterExtensionsManager.getDefault()
            .getLdapServerAdapterExtensions();
        for ( LdapServerAdapterExtension ldapServerAdapterExtension : ldapServerAdapterExtensions )
        {
            String configurationPageClassName = ldapServerAdapterExtension.getConfigurationPageClassName();
            if ( ( configurationPageClassName != null ) && ( !"".equals( configurationPageClassName ) ) ) //$NON-NLS-1$
            {
                try
                {
                    LdapServerAdapterConfigurationPage configurationPage = ldapServerAdapterExtension
                        .getNewConfigurationPageInstance();
                    NewServerWizardConfigurationPage configurationWizardPage = new NewServerWizardConfigurationPage(
                        configurationPage );
                    configurationPages.put( ldapServerAdapterExtension.getId(), configurationWizardPage );
                    addPage( configurationWizardPage );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }


    // ── Filing The Requisition — Creating The Server ──────────────────────────────────────────
    // When the officer clicks Finish, we build the new server object, apply the name and adapter,
    // save any configuration-page values, then hand it to LdapServersManager and the adapter.
    // The adapter's add() call may take time (creating directories, writing config), so we run
    // it in a progress dialog.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks Finish.
     * Creates a new {@link LdapServer}, applies the name and adapter extension, saves any
     * configuration-page values, registers the server with {@link LdapServersManager},
     * creates the on-disk folder, and calls the adapter's {@code add()} method.
     * Runs the heavy lifting inside a {@link org.eclipse.jface.operation.IRunnableWithProgress}
     * so a progress bar is shown.
     *
     * @return {@code true} to close the wizard (we always return true, even if add() fails)
     */
    public boolean performFinish()
    {
        // Getting server name and adapter extension
        final String serverName = adapterSelectionPage.getServerName();
        final LdapServerAdapterExtension adapterExtension = adapterSelectionPage.getLdapServerAdapterExtension();

        // Getting the configuration page (in any
        NewServerWizardConfigurationPage configurationPage = getConfigurationPage();

        // Creating the new server
        final LdapServer server = new LdapServer();
        server.setName( serverName );
        server.setLdapServerAdapterExtension( adapterExtension );

        // Saving the configuration page (is any)
        if ( configurationPage != null )
        {
            configurationPage.saveConfiguration( server );
        }

        try
        {
            getContainer().run( true, false, new IRunnableWithProgress()
            {
                public void run( IProgressMonitor monitor )
                {
                    // Creating a StudioProgressMonitor
                    StudioProgressMonitor spm = new StudioProgressMonitor( monitor );

                    // Setting the title
                    spm.beginTask( Messages.getString( "NewServerWizard.CreatingLdapServer" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$
                    spm.subTask( Messages.getString( "NewServerWizard.CreatingServerFolder" ) ); //$NON-NLS-1$

                    // Adding the new server to the servers handler
                    LdapServersManager.getDefault().addServer( server );

                    // Creating the folder for the new server
                    LdapServersManager.createNewServerFolder( server );

                    try
                    {
                        // Letting the LDAP Server Adapter finish the creation of the server
                        adapterExtension.getInstance().add( server, spm );
                    }
                    catch ( Exception e )
                    {
                        // Reporting the error to the progress monitor
                        spm.reportError( e );
                    }

                    // Reporting to the monitors that we're done
                    spm.done();
                }
            } );
        }
        catch ( Exception e )
        {
            // Will never occur
        }

        return true;
    }


    // ── Setting The Wizard Title Before It Opens ──────────────────────────────────────────────
    // The wizard window gets its title before any pages are shown.
    // We also need a progress monitor because server creation runs in a background task.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the wizard is opened.
     * Sets the wizard window title and enables the progress monitor for the Finish operation.
     *
     * @param workbench  the current workbench
     * @param selection  the current workbench selection (unused)
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setWindowTitle( Messages.getString( "NewServerWizard.NewLdapServer" ) ); //$NON-NLS-1$
        setNeedsProgressMonitor( true );
    }


    // ── Routing Between Wizard Pages ─────────────────────────────────────────────────────────
    // After the selection page, the wizard routes to the configuration page for the chosen
    // adapter (if it has one) or straight to Finish.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the page that follows the given page.
     * After the selection page, returns the configuration page for the currently selected adapter
     * (if any); after the configuration page, returns {@code null} (Finish is next).
     *
     * @param page  the current wizard page
     * @return the next page, or {@code null} if Finish is the next step
     */
    public IWizardPage getNextPage( IWizardPage page )
    {
        IWizardPage configurationPage = getConfigurationPage();

        if ( adapterSelectionPage.equals( page ) )
        {
            return configurationPage;
        }

        return null;
    }


    // ── Checking Whether Finish Is Available ──────────────────────────────────────────────────
    // Finish is only available when the selection page is complete AND either there is no
    // configuration page or the configuration page is also complete.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the wizard is ready to finish — selection page is complete and,
     * if a configuration page exists for the selected adapter, that page is also complete.
     *
     * @return {@code true} if the Finish button should be enabled
     */
    public boolean canFinish()
    {
        if ( adapterSelectionPage.isPageComplete() )
        {
            IWizardPage configurationPage = getConfigurationPage();

            if ( configurationPage != null )
            {
                return configurationPage.isPageComplete();
            }
            else
            {
                return true;
            }
        }

        return false;
    }


    // ── Finding The Configuration Page For The Currently Selected Adapter ────────────────────
    // Not all adapter types have a configuration page; this helper returns the right one
    // (or null) based on what's currently selected on page 1.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link NewServerWizardConfigurationPage} for the adapter extension currently
     * selected on the first wizard page, or {@code null} if that adapter has no configuration page.
     *
     * @return the configuration page, or {@code null}
     */
    private NewServerWizardConfigurationPage getConfigurationPage()
    {
        LdapServerAdapterExtension ldapServerAdapterExtension = adapterSelectionPage
            .getLdapServerAdapterExtension();

        if ( ldapServerAdapterExtension != null )
        {
            String configurationPageClassName = ldapServerAdapterExtension.getConfigurationPageClassName();

            if ( ( configurationPageClassName != null ) && ( !"".equals( configurationPageClassName ) ) ) //$NON-NLS-1$
            {
                return configurationPages.get( ldapServerAdapterExtension.getId() );
            }
        }

        return null;
    }
}
