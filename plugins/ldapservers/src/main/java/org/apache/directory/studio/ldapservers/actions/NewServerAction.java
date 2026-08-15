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
package org.apache.directory.studio.ldapservers.actions;


import org.apache.directory.studio.ldapservers.LdapServersPlugin;
import org.apache.directory.studio.ldapservers.LdapServersPluginConstants;
import org.apache.directory.studio.ldapservers.wizards.NewServerWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewServerAction — THE REBEL ALLIANCE COMMISSIONS A NEW X-WING ─────────────────────
// Mon Mothma stands before the Rebel high command and signs the commission order for a
// brand-new T-65 X-wing fighter.  The mechanics are waiting in the hangar; the wizard
// walks through every spec — engine type, targeting system, pilot assignment — until
// the new ship is officially inducted into the fleet.
// That is exactly what we do here: open the {@link NewServerWizard} so the user can
// configure and create a fresh LDAP server entry from scratch.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse Action that launches the "New Server" wizard, guiding the user through creating
 * and configuring a brand-new LDAP server entry in Directory Studio.
 * We open a {@link WizardDialog} wrapping a {@link NewServerWizard} — the wizard itself
 * asks for the server type, name, and any adapter-specific settings, then registers the
 * new server with the plugin when the user finishes.
 * Think of this class as Mon Mothma signing the commission order that kicks off a full
 * X-wing procurement process.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewServerAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── MON MOTHMA SIGNS THE COMMISSION ORDER ────────────────────────────────────────────────
    // Mon Mothma picks up her stylus, writes "New X-wing" across the requisition form, and
    // stamps it with the Rebel Alliance seal — the hangar crew now knows to stand by.
    // We set up the action's label, command IDs, tooltip, and icon so the "New Server"
    // button appears correctly in the toolbar and responds to its keyboard shortcut.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and fully initialises the {@code NewServerAction}.
     * We set the display label, Eclipse command IDs (for keyboard binding), tooltip text,
     * and the server-new icon.  After construction we are ready to be placed on a toolbar
     * or menu and invoked by the user.
     *
     * <p>For example — Mon Mothma signs and seals the commission order:</p>
     * <pre>
     *   NewServerAction action = new NewServerAction();
     *   // label = "New Server", icon = IMG_SERVER_NEW, tooltip ready
     *   // place on toolbar — Mon Mothma's order is on the board
     * </pre>
     */
    public NewServerAction()
    {
        super( Messages.getString( "NewServerAction.NewServer" ) ); //$NON-NLS-1$
        setId( LdapServersPluginConstants.CMD_NEW_SERVER );
        setActionDefinitionId( LdapServersPluginConstants.CMD_NEW_SERVER );
        setToolTipText( Messages.getString( "NewServerAction.NewServerToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( LdapServersPlugin.getDefault().getImageDescriptor(
            LdapServersPluginConstants.IMG_SERVER_NEW ) );
    }


    // ── THE MECHANICS ROLL THE NEW X-WING ONTO THE LAUNCH PAD ────────────────────────────────
    // The hangar crew wheels the fresh T-65 X-wing out of storage, props open the intake
    // panel, and walks the technician through the step-by-step commissioning wizard.
    // We instantiate the {@link NewServerWizard}, wrap it in a {@link WizardDialog},
    // and open it so the user can walk through each configuration page in sequence.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link NewServerWizard} inside a modal {@link WizardDialog}.
     * The wizard walks the user through choosing a server type (e.g. ApacheDS, OpenLDAP)
     * and entering a name, then registers the new server when they click Finish.
     * If the user cancels, nothing is created.
     *
     * <p>For example — the mechanics walk through the X-wing commissioning checklist:</p>
     * <pre>
     *   NewServerWizard wizard = new NewServerWizard();
     *   wizard.init( workbench, StructuredSelection.EMPTY );
     *   WizardDialog dialog = new WizardDialog( shell, wizard );
     *   dialog.create();
     *   dialog.open();   // X-wing inducted into the fleet when user clicks Finish
     * </pre>
     */
    public void run()
    {
        // Instantiates and initializes the wizard
        NewServerWizard wizard = new NewServerWizard();
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── THE GROUND CREW RELAYS MON MOTHMA'S ORDER ────────────────────────────────────────────
    // A ground-crew officer receives Mon Mothma's commission order via the fleet comms relay
    // and passes it straight through to the hangar bay without modification.
    // Eclipse sometimes invokes us through the {@link IWorkbenchWindowActionDelegate} interface;
    // this overload just forwards to our primary {@link #run()} method.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@link IWorkbenchWindowActionDelegate} entry point that delegates to {@link #run()}.
     * Eclipse calls this variant when the action is triggered through a menu or key binding
     * registered via the workbench extension point.
     * We don't need anything from the {@code action} parameter; we just forward the call.
     *
     * @param action  The Eclipse proxy {@link IAction} — we ignore it and call {@link #run()}.
     */
    public void run( IAction action )
    {
        run();
    }


    // ── THE HANGAR BAY STANDS DOWN AFTER COMMISSIONING ───────────────────────────────────────
    // The hangar crew packs up their tools and disperses once the X-wing is in the fleet;
    // there is nothing left to clean up.
    // We implement this because {@link IWorkbenchWindowActionDelegate} requires it,
    // but we hold no resources that need releasing.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being torn down.
     * We hold no resources of our own, so this is intentionally empty.
     *
     * <p>For example — the hangar crew disperses, tools already stowed:</p>
     * <pre>
     *   // Nothing to do
     * </pre>
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── MON MOTHMA'S AIDE CHECKS IN, GETS WAVED OFF ──────────────────────────────────────────
    // An aide arrives at Mon Mothma's side to report the workbench window reference,
    // but Mon Mothma already has everything she needs and dismisses him politely.
    // Eclipse calls this when the delegate is associated with a window; we don't need
    // the window reference so we ignore it.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is first associated with a workbench window.
     * We don't need the window reference — everything we use comes from the workbench
     * singleton accessed through {@link PlatformUI} — so this is a no-op.
     *
     * @param window  The workbench window we are attached to — reported in, not needed.
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── THE FLEET SENSOR SWEEPS, REPORTS NO RELEVANT CHANGE ──────────────────────────────────
    // The Rebel fleet's long-range sensors continuously update their selection picture;
    // when nothing mission-critical changes they report back "all clear" and stand by.
    // Eclipse fires this to let us adjust our enabled state when the selection changes,
    // but enabling "New Server" doesn't depend on what's selected, so we do nothing.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the workbench selection changes.
     * Creating a new server doesn't depend on what is currently selected, so we don't
     * need to update our enabled state here — the action is always available.
     *
     * @param action     The proxy action we could enable or disable — not needed.
     * @param selection  The new workbench selection — irrelevant to us.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
