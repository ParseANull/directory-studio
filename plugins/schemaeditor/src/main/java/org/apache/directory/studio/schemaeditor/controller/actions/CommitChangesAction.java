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
import org.apache.directory.studio.schemaeditor.view.wizards.CommitChangesWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: CommitChangesAction — Clone Trooper Executes Order 66 ─────────────
// The clone trooper receives the encrypted order from Palpatine and executes it
// immediately — no deliberation, just a precise, committed action.
// This action does the same: when triggered, it opens the CommitChangesWizard so
// the user can push their local schema edits back to the live LDAP server.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Opens the CommitChangesWizard so the user can push local schema edits to the server.
 * After editing schemas offline, the user needs a way to write those changes back to
 * the live LDAP directory — this action kicks off that wizard flow.
 * Think of this as a clone trooper executing Order 66: one trigger, one decisive action,
 * changes sent out to the galaxy.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CommitChangesAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── Trooper Receives The Order: Ready State ───────────────────────────────
    // The clone trooper opens his encrypted briefing, confirms the target, and
    // stands ready — weapon loaded, hand on the trigger, awaiting the signal.
    // We set up our label, tooltip, and icon here so the toolbar button is fully
    // dressed before the user even sees it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new CommitChangesAction in a disabled state.
     * We start disabled because there may be nothing to commit yet; the owning
     * controller is responsible for enabling us when there are pending changes.
     */
    public CommitChangesAction()
    {
        super( Messages.getString( "CommitChangesAction.CommitChangesAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_COMMIT_CHANGES ) );
        setEnabled( false );
    }


    // ── Trooper Pulls The Trigger: Launch The Wizard ─────────────────────────
    // Order received — the trooper fires without hesitation, converting the standing
    // order into immediate, concrete action on the battlefield.
    // We spin up the CommitChangesWizard, initialise it against the current workbench
    // state, wrap it in a WizardDialog, and open it so the user steps through the commit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the CommitChangesWizard dialog so the user can review and push their edits.
     * We create the wizard fresh each time so it re-reads the current diff between the
     * local schema model and what's on the server.
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        CommitChangesWizard wizard = new CommitChangesWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── Relay Through The Chain Of Command ────────────────────────────────────
    // The order travels through the Imperial comm network and arrives at the trooper
    // via the standard workbench delegate channel — same outcome, different pipe.
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


    // ── Mission Complete: Nothing To Clean Up ─────────────────────────────────
    // The trooper holsters his weapon and reports mission complete — no gear to
    // hand back, no loose ends. We have nothing to release here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Briefing Room: No Special Window Instructions ─────────────────────────
    // The trooper checks in but already has everything he needs — no additional
    // briefing from this window is required.
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


    // ── Comm Chatter: Global Selection Ignored ────────────────────────────────
    // Background comm traffic doesn't change the trooper's standing order — the
    // commit action isn't selection-sensitive, so we ignore this callback.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op — committing changes is not selection-dependent.
     *
     * @param action     the workbench action proxy; not used
     * @param selection  the workbench selection; not used
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
