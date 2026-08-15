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
package org.apache.directory.studio.schemaeditor.controller.actions;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.wizards.ImportCoreSchemasWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ImportCoreSchemasAction — Construction of the Second Death Star ────
// When the Emperor commissions the second Death Star, Imperial engineers bootstrap
// the whole operation by importing the foundational blueprints — the core structural
// templates that every other system will be built on top of.
// This action does the same: it opens the wizard that loads the built-in core LDAP
// schemas (RFC-standard definitions like inetOrgPerson, organizationalUnit, etc.)
// into the current project so the user has a solid foundation to build on.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Opens the ImportCoreSchemasWizard so the user can load the bundled core LDAP schemas.
 * Core schemas (inetOrgPerson, cosine, nis, etc.) are the standard foundation that most
 * LDAP directories start from — importing them saves the user from defining those
 * well-known types from scratch.
 * Think of this as the Imperial engineering team importing the foundational Death Star
 * blueprints: all the critical structural specifications arrive in one step, ready for
 * the next layer of construction to begin.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportCoreSchemasAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── Engineering Team Receives The Blueprint Commission ────────────────────
    // The chief engineer accepts the commission, confirms the blueprint library is
    // accessible, and sets the project badge — ready to open the blueprint vault
    // the moment the Emperor gives the go-ahead.
    // We configure label, tooltip, and icon here so the toolbar button looks right
    // before the user ever interacts with it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ImportCoreSchemasAction in an enabled, ready state.
     * We start enabled because there's nothing project-specific to check — the
     * core schemas are always available from the plugin bundle.
     */
    public ImportCoreSchemasAction()
    {
        super( Messages.getString( "ImportCoreSchemasAction.CoreSchemaFilesAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT ) );
        setEnabled( true );
    }


    // ── Open The Blueprint Vault ───────────────────────────────────────────────
    // The Emperor nods, and the vault door slides open — engineers step through
    // and select which foundational blueprints to bring into the active project.
    // We spin up the ImportCoreSchemasWizard and open it in a dialog so the user
    // can pick which core schema bundles to import.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the ImportCoreSchemasWizard dialog.
     * The wizard presents a list of bundled core schema sets (RFC schemas, etc.)
     * and loads the ones the user selects into the active project.
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        ImportCoreSchemasWizard wizard = new ImportCoreSchemasWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── Order Relayed Through Imperial Engineering Comms ──────────────────────
    // The commission arrives through the standard Imperial engineering relay —
    // same vault opened, same wizard presented, regardless of the channel.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when called via the workbench action delegate channel.
     *
     * @param action  the workbench action proxy; we ignore it and call our own run()
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Blueprints Delivered: Nothing Left To Release ──────────────────────────
    // The engineers have what they need; the vault is closed, nothing left to hand
    // back. We hold no resources that need explicit cleanup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Engineering Team Reports In: No Window-Specific Orders ────────────────
    // The chief engineer checks in at the command post but receives no instructions
    // tied to this particular workbench window — the blueprints are always available.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op init — we don't need the workbench window reference.
     *
     * @param window  the workbench window; not used here
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Construction Comms Idle: Selection Irrelevant ─────────────────────────
    // The blueprint import doesn't depend on what the user has selected — core
    // schemas are always available regardless of context, so we ignore this callback.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op — importing core schemas is not selection-dependent.
     *
     * @param action     the workbench action proxy; not used
     * @param selection  the workbench selection; not used
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
