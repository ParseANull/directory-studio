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
package org.apache.directory.studio.schemaeditor.view.wizards;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.ProjectType;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.io.GenericSchemaConnector;
import org.apache.directory.studio.schemaeditor.model.io.SchemaConnector;
import org.apache.directory.studio.schemaeditor.view.widget.CoreSchemasSelectionWidget.ServerTypeEnum;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: NewProjectWizard — LUKE'S JOURNEY FROM TATOOINE TO THE DEATH STAR ─
// Luke starts on Tatooine — no idea what's coming — then Obi-Wan shows him the
// bigger picture, they pick up Han and the Falcon in Mos Eisley, and each step
// of the journey gates the next one depending on the path chosen.  The wizard
// mirrors this exactly: an information page, then a fork — online projects head
// to the connection page, offline projects skip straight to schemas selection —
// and at the end the project is assembled and opened like the Rebel base coming
// into view for the first time.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Wizard that guides the user through creating a new Schema Editor project.
 * A project is the top-level container for schemas in Directory Studio — it
 * holds either a live connection to an LDAP server (online) or a locally-
 * managed set of schema files (offline).
 * Think of this wizard as Luke's journey: the information page sets the
 * destination, the middle page depends on which path the user chose (online vs.
 * offline), and {@code performFinish()} actually fires the engines and opens
 * the project.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewProjectWizard extends Wizard implements INewWizard
{
    public static final String ID = PluginConstants.NEW_WIZARD_NEW_PROJECT_WIZARD;

    // The pages of the wizard
    private NewProjectWizardInformationPage informationPage;
    private NewProjectWizardConnectionSelectionPage connectionSelectionPage;
    private NewProjectWizardSchemasSelectionPage schemasSelectionPage;


    // ── Obi-Wan Lays Out The Mission Plan ───────────────────────────────────
    // Obi-Wan sits Luke down in his desert hut and explains the whole journey
    // ahead: "You will come with me to Alderaan — first the cantina, then the
    // Falcon, then the Death Star."  Each waypoint is a page in the wizard.
    // We create all three pages here even though only two of them will ever be
    // shown in any given run — the routing logic in getNextPage() decides which
    // branch the user takes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers all wizard pages for the new-project flow.
     * We always create all three pages up front — Eclipse's wizard framework
     * needs them registered even if only two will be visited in a given run.
     * {@code getNextPage()} handles the branching based on project type.
     *
     * <p>For example — Obi-Wan briefs Luke on the three-leg journey:</p>
     * <pre>
     *   informationPage    = "First, we leave Tatooine — name your destination."
     *   connectionPage     = "If going live, meet Han in the cantina."
     *   schemasSelectionPage = "If staying local, pick your starmap at the base."
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        informationPage = new NewProjectWizardInformationPage();
        connectionSelectionPage = new NewProjectWizardConnectionSelectionPage();
        schemasSelectionPage = new NewProjectWizardSchemasSelectionPage();

        // Adding pages
        addPage( informationPage );
        addPage( connectionSelectionPage );
        addPage( schemasSelectionPage );
    }


    // ── The Falcon Jumps To Hyperspace ───────────────────────────────────────
    // Han pulls back the hyperdrive lever, the stars streak into lines, and the
    // Millennium Falcon rockets toward Alderaan — no going back now.  The whole
    // crew is committed: project name confirmed, connection or schemas loaded,
    // engines fired.
    // We do the same here: build the Project object, fetch the schema if online
    // (or load the selected core schemas if offline), register the project, and
    // open it — the user lands inside their new workspace.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks "Finish" — creates the project, loads its
     * schemas (live from the LDAP server for online, or from bundled core files
     * for offline), registers it with the projects handler, and opens it.
     * For online projects this involves a network round-trip wrapped in a
     * progress-monitor dialog so the UI stays responsive.
     *
     * <p>For example — Han commits to hyperspace and the crew holds on:</p>
     * <pre>
     *   Project project = new Project( ONLINE, "My LDAP Project" );
     *   project.setConnection( selectedConnection );
     *   project.fetchOnlineSchema( monitor );   // network call — may take a moment
     *   projectsHandler.addProject( project );
     *   projectsHandler.openProject( project );
     * </pre>
     *
     * @return {@code true} always — errors surface through the progress monitor dialog
     */
    public boolean performFinish()
    {
        String projectName = informationPage.getProjectName();
        ProjectType projectType = informationPage.getProjectType();

        // Creating the project
        final Project project = new Project( projectType, projectName );

        if ( projectType.equals( ProjectType.ONLINE ) )
        // Project is an "Online Project"
        {
            // Setting the connection to use
            project.setConnection( connectionSelectionPage.getSelectedConnection() );

            RunnableContextRunner.execute( new StudioConnectionRunnableWithProgress()
            {
                public void run( StudioProgressMonitor monitor )
                {
                    // Getting the correct SchemaConnector for this connection
                    List<SchemaConnector> correctSchemaConnectors = getCorrectSchemaConnectors(
                        project.getConnection(), monitor );

                    // If no suitable SchemaConnector has been found, we display an
                    // error message and return false;
                    if ( correctSchemaConnectors.size() == 0 )
                    {
                        monitor.reportError(
                            "No suitable SchemaConnector has been found for the choosen Directory Server.", //$NON-NLS-1$
                            new NoSuitableSchemaConnectorException() );
                    }

                    // Check if generic schema connector is included, then remove it to use a specific one
                    if ( correctSchemaConnectors.size() > 1 )
                    {
                        for ( SchemaConnector schemaConnector : correctSchemaConnectors )
                        {
                            if ( schemaConnector instanceof GenericSchemaConnector )
                            {
                                correctSchemaConnectors.remove( schemaConnector );
                                break;
                            }
                        }
                    }

                    // Getting the correct SchemaConnector
                    SchemaConnector correctSchemaConnector = null;
                    if ( correctSchemaConnectors.size() == 1 )
                    {
                        correctSchemaConnector = correctSchemaConnectors.get( 0 );
                    }
                    else
                    {
                        // TODO display a dialog in which the user can select the correct schema connector
                    }

                    project.setSchemaConnector( correctSchemaConnector );

                    // Fetching the Online Schema
                    project.fetchOnlineSchema( monitor );
                }


                public String getName()
                {
                    return Messages.getString( "NewProjectWizard.FetchingSchema" ); //$NON-NLS-1$;
                }


                public Object[] getLockedObjects()
                {
                    return null;
                }


                public String getErrorMessage()
                {
                    return Messages.getString( "NewProjectWizard.ErrorWhileFetchingSchema" ); //$NON-NLS-1$;
                }


                public Connection[] getConnections()
                {
                    return null;
                }

            }, getContainer(), true );
        }
        else if ( projectType.equals( ProjectType.OFFLINE ) )
        // Project is an "Offline Project"
        {
            // Getting the selected 'core' schemas
            String[] selectedSchemas = schemasSelectionPage.getSelectedSchemas();
            ServerTypeEnum serverType = schemasSelectionPage.getServerType();
            if ( ( selectedSchemas != null ) && ( serverType != null ) )
            {
                SchemaHandler schemaHandler = project.getSchemaHandler();
                for ( String selectedSchema : selectedSchemas )
                {
                    Schema schema = PluginUtils.loadCoreSchema( serverType, selectedSchema );
                    if ( schema != null )
                    {
                        schema.setProject( project );
                        schemaHandler.addSchema( schema );
                    }
                }
            }
        }

        ProjectsHandler projectsHandler = Activator.getDefault().getProjectsHandler();
        projectsHandler.addProject( project );
        projectsHandler.openProject( project );

        return true;
    }


    // ── R2-D2 Scans For The Right Docking Bay ───────────────────────────────
    // R2-D2 plugs into the Death Star's computer and scans through thousands of
    // registered systems looking for the ones that are actually compatible with
    // their mission — not every port will do.
    // We do the same: iterate every registered SchemaConnector plugin and ask
    // each one whether it can handle the given LDAP connection, collecting the
    // suitable ones.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Finds all {@link SchemaConnector} plugins that can talk to the given LDAP connection.
     * Different LDAP servers expose their schemas in different ways, so we have
     * multiple connectors registered as plugins and we need to find the right one
     * for this particular server.
     *
     * <p>For example — R2-D2 narrows down compatible docking bays:</p>
     * <pre>
     *   for ( SchemaConnector sc : allConnectors ) {
     *       if ( sc.isSuitableConnector( connection, monitor ) ) {
     *           suitableList.add( sc );   // "Beep boop — this one fits!"
     *       }
     *   }
     * </pre>
     *
     * @param connection  the LDAP connection we're trying to find a connector for
     * @param monitor     progress monitor so we can report if something goes wrong
     * @return            list of connectors that declared themselves compatible; may be empty
     */
    private List<SchemaConnector> getCorrectSchemaConnectors( Connection connection, StudioProgressMonitor monitor )
    {
        List<SchemaConnector> suitableSchemaConnectors = new ArrayList<SchemaConnector>();

        // Looping on the SchemaConnectors
        List<SchemaConnector> schemaConectors = PluginUtils.getSchemaConnectors();
        for ( SchemaConnector schemaConnector : schemaConectors )
        {
            // Testing if the SchemaConnector is suitable for this connection
            if ( schemaConnector.isSuitableConnector( connection, monitor ) )
            {
                suitableSchemaConnectors.add( schemaConnector );
            }
        }

        return suitableSchemaConnectors;
    }


    // ── Luke Forks Left At Mos Eisley ────────────────────────────────────────
    // Leaving Tatooine, Luke and Obi-Wan take different roads depending on what
    // they know: if they need Han's ship (online), they head to the cantina; if
    // they already have what they need (offline), they bypass Mos Eisley and
    // head straight for the spaceport.
    // This method does the same routing: after the information page, we branch
    // to either the connection page or the schemas selection page.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the page that should follow the given one — implementing the
     * online/offline branch point in the wizard.
     * After the information page, online projects go to connection selection and
     * offline projects skip to schemas selection.  After either of those, we
     * return {@code null} because there's nothing left.
     *
     * <p>For example — Obi-Wan routes Luke based on the mission:</p>
     * <pre>
     *   if ( projectType == ONLINE )  return connectionSelectionPage;
     *   if ( projectType == OFFLINE ) return schemasSelectionPage;
     *   return null;  // we've arrived
     * </pre>
     *
     * @param page  the page the user is currently on
     * @return      the next page to show, or {@code null} if we're at the end of the line
     */
    public IWizardPage getNextPage( IWizardPage page )
    {
        if ( page.equals( informationPage ) )
        {
            if ( informationPage.getProjectType().equals( ProjectType.ONLINE ) )
            {
                return connectionSelectionPage;
            }
            else if ( informationPage.getProjectType().equals( ProjectType.OFFLINE ) )
            {
                return schemasSelectionPage;
            }
        }

        // Default
        return null;
    }


    // ── Luke Backtracks Through Mos Eisley ───────────────────────────────────
    // If Luke realizes he picked the wrong cantina, he retraces his steps back
    // through the main street to Obi-Wan at the entrance — no matter which side-
    // alley he went down, the way back is always to the information page.
    // The connection page and schemas page both share the same "back" target.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the page the user should go back to from the given one.
     * Both branch pages (connection selection and schemas selection) back up
     * to the information page — that's the only branching point in this wizard.
     *
     * <p>For example — Luke heads back to the cantina entrance:</p>
     * <pre>
     *   if ( onConnectionPage || onSchemasPage ) return informationPage;
     *   return null;  // already at the start
     * </pre>
     *
     * @param page  the page the user wants to navigate back from
     * @return      the previous page, or {@code null} if we're already on the first page
     */
    public IWizardPage getPreviousPage( IWizardPage page )
    {
        if ( ( page.equals( connectionSelectionPage ) ) || ( page.equals( schemasSelectionPage ) ) )
        {
            return informationPage;
        }

        // Default
        return null;
    }


    // ── Han Checks If The Falcon Is Jump-Ready ───────────────────────────────
    // Before Han pulls the hyperdrive lever, he looks at the readouts: all
    // systems green?  He only punches it when the Falcon is actually ready to
    // make the jump — not a moment before.
    // Similarly, we only let the user click "Finish" once there's enough info
    // on the current page to actually build the project.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Decides whether the "Finish" button should be enabled on the current page.
     * The information page is never enough on its own — the user must get to
     * either the connection page (and have a connection selected) or the schemas
     * selection page before we allow them to finish.
     *
     * <p>For example — Han checks the Falcon's jump readiness:</p>
     * <pre>
     *   if ( onInfoPage )           return false;   // "Not ready yet, kid."
     *   if ( onSchemasPage )        return true;    // "Punch it!"
     *   if ( connectionPageDone )   return true;    // "All systems go."
     * </pre>
     *
     * @return {@code true} if the user has enough info entered to finish the wizard
     */
    public boolean canFinish()
    {
        IWizardPage currentPage = getContainer().getCurrentPage();

        if ( currentPage.equals( informationPage ) )
        {
            return false;
        }
        else if ( currentPage.equals( schemasSelectionPage ) )
        {
            return true;
        }
        else if ( currentPage.equals( connectionSelectionPage ) )
        {
            return connectionSelectionPage.isPageComplete();
        }
        else
        {
            return false;
        }
    }


    // ── Chewbacca Warms Up The Engines ───────────────────────────────────────
    // While Han negotiates passage with Greedo, Chewie goes ahead and fires up
    // the Falcon's engines so they're ready to go the moment the crew boards.
    // We do the same here: tell Eclipse we need a progress monitor dialog so
    // it's ready for the potentially slow schema-fetch in performFinish().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes this wizard from the Eclipse workbench context.
     * The only thing we actually do here is flag that we need a progress monitor,
     * because online projects have to fetch the schema over the network and that
     * can take a while.
     *
     * <p>For example — Chewie pre-heats the hyperdrive before departure:</p>
     * <pre>
     *   setNeedsProgressMonitor( true );
     *   // "Rrawwrr!" (Translation: "Progress bar is ready, Captain.")
     * </pre>
     *
     * @param workbench  the active Eclipse workbench — not used directly
     * @param selection  the current UI selection when the wizard was opened — not used
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setNeedsProgressMonitor( true );
    }

    class NoSuitableSchemaConnectorException extends Exception
    {
        private static final long serialVersionUID = 1L;
    }
}
