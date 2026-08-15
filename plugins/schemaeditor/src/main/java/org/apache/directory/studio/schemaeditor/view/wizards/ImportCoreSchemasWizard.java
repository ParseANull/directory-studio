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


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.widget.CoreSchemasSelectionWidget.ServerTypeEnum;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ImportCoreSchemasWizard — C-3PO Downloads Core Protocol Languages ──
// When C-3PO arrives at a new location — whether Cloud City or the Rebellion's
// base — he connects to the local computer and downloads the core language
// protocols he needs to function: basic syntax, fundamental rules, the shared
// vocabulary that every subsequent conversation depends on.
// We do the same: pull in the foundational "core" LDAP schemas (inetOrgPerson,
// cosine, etc.) that other schemas build on top of.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The wizard that imports built-in "core" LDAP schemas into the current project.
 * Core schemas — like inetOrgPerson, cosine, nis, and apache — are the foundational
 * schema definitions that ship with LDAP servers.  Other schemas typically
 * inherit from them, so you usually need to import these first.
 * Think of C-3PO plugging into a new computer and downloading the base language
 * protocols: everything that comes later depends on getting these fundamentals in first.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportCoreSchemasWizard extends Wizard implements IImportWizard
{
    // The pages of the wizard
    private ImportCoreSchemasWizardPage page;


    // ── C-3PO Opens The Protocol Download Panel ────────────────────────────────
    // C-3PO connects to Cloud City's network and opens the protocol library
    // browser — scrolling through the available core language packages he can
    // download and install into his own memory banks.
    // Here we create and register the wizard's selection page so the user can
    // browse available core schemas before committing to an import.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers the page that lets users select which core schemas to import.
     * Eclipse calls this at wizard startup so our pages are available before
     * the dialog becomes visible.
     *
     * <p>For example — C-3PO opens the Cloud City protocol library:</p>
     * <pre>
     *   C-3PO interfaces with the local network: "Browsing available core protocols..."
     *   The page appears, listing all installable core schema packages.
     *   C-3PO (the user) selects what to download.
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        page = new ImportCoreSchemasWizardPage();

        // Adding pages
        addPage( page );
    }


    // ── C-3PO Installs The Selected Protocols ─────────────────────────────────
    // C-3PO selects his protocol packages from the Cloud City download menu —
    // "Bocce, Shyriiwook, Binary... yes, all of those, please" — and triggers
    // the installation, loading each one into his active memory bank.
    // When the user clicks Finish, we load each selected core schema from the
    // plugin bundle and add it to the open project's SchemaHandler.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the import when the user clicks the Finish button.
     * We read the user's selections from the page, load each named core schema
     * from the plugin resources via {@link PluginUtils#loadCoreSchema(ServerTypeEnum, String)},
     * associate it with the current project, and register it with the project's
     * {@link SchemaHandler}.
     * If no project is open, or if no schemas were selected, we do nothing and
     * return {@code true} (the wizard still closes cleanly).
     *
     * <p>For example — C-3PO installs the selected protocols:</p>
     * <pre>
     *   "Installing inetOrgPerson... done."
     *   "Installing cosine... done."
     *   Each schema is like a language pack loaded into C-3PO's active memory.
     *   If no project is open, there's nowhere to install — we politely skip.
     * </pre>
     *
     * @return {@code true} always — import errors are logged but don't block the wizard close
     */
    public boolean performFinish()
    {
        String[] selectedSchemas = page.getSelectedSchemas();;
        ServerTypeEnum serverType = page.getServerType();

        Project project = Activator.getDefault().getProjectsHandler().getOpenProject();
        if ( project != null )
        {
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

        return true;
    }


    // ── C-3PO Receives His Mission Parameters ─────────────────────────────────
    // When C-3PO first powers on in a new environment, he receives his operating
    // context from the local systems — who's in charge, what the setup is.
    // Eclipse calls init() right after constructing the wizard; we don't need
    // much from it here, so we just accept the call and move on.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initializes the wizard with the Eclipse workbench context.
     * Eclipse requires this method from the {@link IImportWizard} interface, but
     * this wizard needs no special setup at init time — everything happens in
     * {@link #addPages()} and {@link #performFinish()}.
     *
     * <p>For example — C-3PO receives his initial operating parameters:</p>
     * <pre>
     *   "Context received: Eclipse workbench, current selection: empty."
     *   C-3PO nods. "Very good. Awaiting further instructions."
     *   No special initialization required beyond acknowledging the call.
     * </pre>
     *
     * @param workbench  the Eclipse workbench — required by the interface, not used here
     * @param selection  the current UI selection — not used by this wizard
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
    }
}
