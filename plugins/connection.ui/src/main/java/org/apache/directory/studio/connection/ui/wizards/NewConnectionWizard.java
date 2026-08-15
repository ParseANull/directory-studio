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

package org.apache.directory.studio.connection.ui.wizards;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.jobs.OpenConnectionsRunnable;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.apache.directory.studio.connection.ui.ConnectionParameterPage;
import org.apache.directory.studio.connection.ui.ConnectionParameterPageManager;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewConnectionWizard — ENROLLING A NEW REBEL SHIP ───────────────────────
// When a new X-Wing joins the Rebel fleet, Mon Mothma's staff walks through a
// multi-step intake form: first the ship's comms parameters, then authentication
// codes, then any special browser overrides.  Once all steps are complete the ship
// is registered in the fleet and immediately brought online.
// NewConnectionWizard is that intake form.  Each registered ConnectionParameterPage
// extension becomes a wizard step wrapped in a NewConnectionWizardPage.  On Finish,
// we collect all parameters, register a new Connection, add it to the selected
// folder, and fire an OpenConnectionsRunnable to bring it online immediately.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Multi-page {@link INewWizard} for creating and immediately opening a new
 * LDAP {@link Connection}.
 *
 * <p>Each {@link ConnectionParameterPage} registered via the extension point
 * {@code org.apache.directory.studio.connection.ui.connectionParameterPages}
 * becomes a dedicated wizard step wrapped in a {@link NewConnectionWizardPage}.</p>
 *
 * <p>On Finish:</p>
 * <ol>
 *   <li>Parameters are collected from all pages into a fresh
 *       {@link ConnectionParameter}.</li>
 *   <li>A {@link Connection} is created and registered with the
 *       {@link org.apache.directory.studio.connection.core.ConnectionManager}.</li>
 *   <li>The connection ID is added to the target {@link ConnectionFolder}
 *       (derived from the workbench selection).</li>
 *   <li>An {@link OpenConnectionsRunnable} is executed to open the connection.</li>
 * </ol>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewConnectionWizard extends Wizard implements INewWizard
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /**
     * The JFace wizard-page wrappers — one per {@link ConnectionParameterPage}.
     * Mirrors {@link #pages} index-for-index.
     */
    private NewConnectionWizardPage[] wizardPages;

    /**
     * The underlying parameter pages loaded from the extension registry.
     * Each page handles one category of connection settings.
     */
    private ConnectionParameterPage[] pages;

    /**
     * The target folder for the new connection.  Determined from the workbench
     * selection in {@link #init}; falls back to the root folder.
     */
    private ConnectionFolder selectedConnectionFolder;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link NewConnectionWizard}.
     *
     * <p>Sets the window title and enables the progress monitor container
     * (used by the "Check Network Parameter" test button).</p>
     */
    public NewConnectionWizard()
    {
        super();
        setWindowTitle( Messages.getString( "NewConnectionWizard.NewLdapConnection" ) ); //$NON-NLS-1$
        setNeedsProgressMonitor( true );
    }


    // ── GET ID ────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID registered in {@code plugin.xml}.
     *
     * @return The wizard extension-point ID string.
     */
    public static String getId()
    {
        return ConnectionUIConstants.NEW_WIZARD_NEW_CONNECTION;
    }


    // ── INIT ──────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Determines the target folder from the workbench selection:</p>
     * <ul>
     *   <li>If the selection contains a {@link ConnectionFolder}, that folder is
     *       used directly.</li>
     *   <li>If the selection contains a {@link Connection}, we use its parent
     *       folder.</li>
     *   <li>If neither, we fall back to the root connection folder.</li>
     * </ul>
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // ── DETERMINE TARGET FOLDER ───────────────────────────────────────────────
        Object firstElement = selection.getFirstElement();

        if ( firstElement instanceof ConnectionFolder )
        {
            selectedConnectionFolder = ( ConnectionFolder ) firstElement;
        }
        else if ( firstElement instanceof Connection )
        {
            Connection connection = ( Connection ) firstElement;
            selectedConnectionFolder = ConnectionCorePlugin.getDefault().getConnectionFolderManager()
                .getParentConnectionFolder( connection );
        }

        if ( selectedConnectionFolder == null )
        {
            selectedConnectionFolder = ConnectionCorePlugin.getDefault().getConnectionFolderManager()
                .getRootConnectionFolder();
        }
    }


    // ── ADD PAGES ─────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Loads all registered {@link ConnectionParameterPage} instances from the
     * extension registry and wraps each in a {@link NewConnectionWizardPage}.
     * Also sets the wizard container on each parameter page (needed for the
     * progress monitor used by the test button).</p>
     */
    public void addPages()
    {
        pages = ConnectionParameterPageManager.getConnectionParameterPages();
        wizardPages = new NewConnectionWizardPage[pages.length];

        for ( int i = 0; i < pages.length; i++ )
        {
            wizardPages[i] = new NewConnectionWizardPage( this, pages[i] );
            addPage( wizardPages[i] );
            pages[i].setRunnableContext( getContainer() );
        }
    }


    // ── CREATE PAGE CONTROLS ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>After the standard controls are created, wires Eclipse help context IDs
     * onto each wizard page's control.</p>
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        for ( NewConnectionWizardPage wizardPage : wizardPages )
        {
            PlatformUI.getWorkbench().getHelpSystem().setHelp( wizardPage.getControl(),
                ConnectionUIConstants.PLUGIN_ID + "." + "tools_newconnection_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }


    // ── CAN FINISH ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>The Finish button is enabled only when all parameter pages report
     * {@link ConnectionParameterPage#isValid()} == {@code true}.</p>
     */
    public boolean canFinish()
    {
        for ( ConnectionParameterPage page : pages )
        {
            if ( !page.isValid() )
            {
                return false;
            }
        }

        return true;
    }


    // ── PERFORM FINISH ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Collects parameters, creates and registers a new {@link Connection},
     * adds it to the target folder, and opens it immediately.</p>
     *
     * @return Always {@code true}.
     */
    public boolean performFinish()
    {
        // ── COLLECT PARAMETERS ────────────────────────────────────────────────────
        ConnectionParameter connectionParameter = new ConnectionParameter();

        for ( ConnectionParameterPage page : pages )
        {
            page.saveParameters( connectionParameter );
            page.saveDialogSettings();
        }

        // ── REGISTER THE NEW CONNECTION ───────────────────────────────────────────
        Connection conn = new Connection( connectionParameter );
        ConnectionCorePlugin.getDefault().getConnectionManager().addConnection( conn );

        // ── ADD TO FOLDER ─────────────────────────────────────────────────────────
        selectedConnectionFolder.addConnectionId( conn.getId() );

        // ── OPEN IMMEDIATELY ──────────────────────────────────────────────────────
        new StudioConnectionJob( new OpenConnectionsRunnable( conn ) ).execute();

        return true;
    }


    // ── GET TEST CONNECTION PARAMETERS ────────────────────────────────────────────
    /**
     * Builds a {@link ConnectionParameter} snapshot from all pages — used by the
     * "Check Network Parameter" test button to verify connectivity without saving.
     *
     * @return A {@link ConnectionParameter} built from the current field values.
     */
    public ConnectionParameter getTestConnectionParameters()
    {
        ConnectionParameter connectionParameter = new ConnectionParameter();

        for ( ConnectionParameterPage page : pages )
        {
            page.saveParameters( connectionParameter );
        }

        return connectionParameter;
    }
}
