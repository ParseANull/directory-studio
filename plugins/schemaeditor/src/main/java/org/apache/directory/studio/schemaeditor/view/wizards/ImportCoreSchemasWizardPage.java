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

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.widget.CoreSchemasSelectionWidget;
import org.apache.directory.studio.schemaeditor.view.widget.CoreSchemasSelectionWidget.ServerTypeEnum;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ImportCoreSchemasWizardPage — C-3PO Browsing The Protocol Library ──
// C-3PO stands at a Cloud City terminal, scrolling through the available
// core language protocol packages — he can see which ones are already installed
// (grayed out) and which ones he can still download.  He picks a server type
// (the dialect variant) and checks off the protocols he needs.
// This page is that terminal: choose the server flavor, select the core schemas,
// and the wizard will install them into your project.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The selection page inside {@link ImportCoreSchemasWizard}.
 * It shows a {@link CoreSchemasSelectionWidget} that lets the user pick a server
 * type (ApacheDS, OpenLDAP, etc.) and then check off which core schemas to import.
 * Schemas already in the project are shown as grayed out so the user knows
 * they don't need to re-import them.
 * Think of C-3PO at the Cloud City protocol library: he sees what's installed
 * already (grayed) and picks only the missing language packs.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportCoreSchemasWizardPage extends AbstractWizardPage
{
    // UI Fields
    private CoreSchemasSelectionWidget coreSchemaSelectionWidget;


    // ── C-3PO Boots Up At The Library Terminal ────────────────────────────────
    // C-3PO steps up to the Cloud City protocol download terminal and the screen
    // lights up: title, purpose, and the icon for the core-schemas import system.
    // Our constructor sets those metadata fields so Eclipse can render the page
    // header before any content widgets are built.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new page instance with title, description, and icon configured.
     * Eclipse needs these items before it calls {@link #createControl(Composite)},
     * so we supply them here in the constructor.
     *
     * <p>For example — C-3PO's terminal display comes online:</p>
     * <pre>
     *   Screen header lights up: "Import Core Schemas"
     *   Subtitle: "Please select core schemas to import."
     *   The protocol library icon appears in the wizard's top-right corner.
     * </pre>
     */
    protected ImportCoreSchemasWizardPage()
    {
        super( "ImportCoreSchemasWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ImportCoreSchemasWizardPage.ImportCoreSchemas" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ImportCoreSchemasWizardPage.PleaseSelectCoreSchemas" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT_WIZARD ) );
    }


    // ── C-3PO Renders The Protocol Selection Screen ────────────────────────────
    // The Cloud City terminal displays C-3PO's download interface: a server-type
    // selector at the top (ApacheDS or OpenLDAP dialect), followed by the list
    // of available core protocols — with already-installed ones grayed out so
    // he can't accidentally download the same thing twice.
    // We delegate all the heavy lifting to CoreSchemasSelectionWidget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the UI for this wizard page by delegating to {@link CoreSchemasSelectionWidget}.
     * We initialize the widget to ApacheDS server type by default.
     * We also gray out any schemas the current project already has, so the user
     * can't inadvertently select something that's already installed.
     * Eclipse calls this exactly once, just before the page becomes visible.
     *
     * <p>For example — C-3PO sees the protocol library screen:</p>
     * <pre>
     *   Server type: [ApacheDS ▼]
     *   [ ] inetOrgPerson   (available)
     *   [■] core            (grayed — already installed)
     *   [ ] nis             (available)
     *   C-3PO checks inetOrgPerson and nis, then clicks Next.
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides as our container
     */
    public void createControl( Composite parent )
    {
        coreSchemaSelectionWidget = new CoreSchemasSelectionWidget();
        Composite composite = coreSchemaSelectionWidget.createWidget( parent );
        coreSchemaSelectionWidget.init( ServerTypeEnum.APACHE_DS );

        Project project = Activator.getDefault().getProjectsHandler().getOpenProject();
        if ( project != null )
        {
            List<Schema> schemas = project.getSchemaHandler().getSchemas();
            List<String> schemaNames = new ArrayList<String>();
            for ( Schema schema : schemas )
            {
                schemaNames.add( schema.getSchemaName() );
            }

            coreSchemaSelectionWidget.setGrayedCoreSchemas( schemaNames.toArray( new String[0] ) );
        }

        dialogChanged();

        setControl( composite );
    }


    // ── C-3PO Checks That Everything Is Ready To Install ──────────────────────
    // Before C-3PO authorizes the download, he checks one thing: is there actually
    // an active project to install the protocols into?  If there's no open project,
    // the download has nowhere to go — installation would be pointless.
    // We show an error and lock the Finish button until a project exists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current page state and updates the error banner.
     * The only check we perform here is whether a schema project is currently open —
     * if not, there's nowhere to install the imported schemas.
     * More validation (e.g., checking that at least one schema is selected) is
     * handled inside the {@link CoreSchemasSelectionWidget} itself.
     *
     * <p>For example — C-3PO verifies the installation target:</p>
     * <pre>
     *   "Is a project open? Let me check..."
     *   If no project: "Error: No schema project to install into."
     *   If project exists: "All clear. Proceed with installation."
     * </pre>
     */
    private void dialogChanged()
    {
        // Checking if a Schema Project is open
        if ( Activator.getDefault().getSchemaHandler() == null )
        {
            displayErrorMessage( Messages.getString( "ImportCoreSchemasWizardPage.ErrorNoSchemaProjectOpen" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── C-3PO Reports Which Protocols He Selected ─────────────────────────────
    // After making his selections, C-3PO reads the checked items off the screen
    // and hands the list to the wizard so it knows what to download.
    // We delegate to the widget which knows the actual checkbox state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the names of the core schemas the user checked.
     * The wizard's {@link ImportCoreSchemasWizard#performFinish()} calls this
     * to know which schema definitions to load from the plugin bundle.
     *
     * <p>For example — C-3PO reads back his selection:</p>
     * <pre>
     *   "Selected protocols: inetOrgPerson, nis."
     *   The wizard uses these names to load the bundled schema resources.
     * </pre>
     *
     * @return  array of core schema names the user selected; may be empty but not null
     */
    public String[] getSelectedSchemas()
    {
        return coreSchemaSelectionWidget.getSelectedCoreSchemas();
    }


    // ── C-3PO Reports Which Dialect Library He Chose ──────────────────────────
    // C-3PO notes which protocol variant he selected from the top of the screen —
    // ApacheDS uses a slightly different schema flavour than OpenLDAP, the same way
    // Bocce and Shyriiwook have different grammatical rules even for the same concepts.
    // The wizard needs to know which "dialect" to use when loading the schemas.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server type the user selected in the widget.
     * The server type (ApacheDS, OpenLDAP, etc.) determines which bundled schema
     * resources the wizard loads — different servers ship with slightly different
     * core schema variants.
     * The wizard's {@link ImportCoreSchemasWizard#performFinish()} passes this
     * to {@code PluginUtils.loadCoreSchema()} as the lookup key.
     *
     * <p>For example — C-3PO confirms the protocol dialect:</p>
     * <pre>
     *   "Server dialect selected: ApacheDS."
     *   The wizard will load the ApacheDS-flavoured core schema resources.
     *   If OpenLDAP were chosen, it would load from a different resource set.
     * </pre>
     *
     * @return  the {@link ServerTypeEnum} value matching the user's selection
     */
    public ServerTypeEnum getServerType()
    {
        return coreSchemaSelectionWidget.getServerType();
    }
}
