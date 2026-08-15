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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.search.SearchPage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenSearchAction — R2-D2 PLUGS INTO THE DEATH STAR TERMINAL ───────
// R2-D2 finds a terminal on the Death Star, extends his probe, and opens a fresh
// query session — ready to start pulling data the moment someone gives him a
// request.  OpenSearchAction does the same: it opens the Eclipse Search Dialog
// pre-pointed at the LDAP search page, giving the user a ready terminal to type
// their search into.  Unlike NewSearchAction (which is a BrowserAction), this
// class also implements IWorkbenchWindowActionDelegate so it can be registered as
// a workbench-window action and appear in the top-level toolbar.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse Search Dialog with the LDAP browser search page, registered
 * as a workbench-window action delegate so it can live in the main toolbar.
 * It's functionally equivalent to {@code NewSearchAction} but implements the
 * {@link IWorkbenchWindowActionDelegate} contract required for top-level toolbar
 * contributions.
 * Think of this as R2-D2's "ready to query" state — one plug-in and the terminal
 * is live.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSearchAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── R2 Configures His Terminal Interface ─────────────────────────────────────
    // Before R2 plugs in he configures his probe for the right protocol — sets the
    // icon, the label, the push-button behaviour.  Our constructor does the same:
    // we call the parent Action constructor with the right style, set text, tooltip,
    // and icon, and mark ourselves enabled.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OpenSearchAction with its label, tooltip, icon, and push-button style.
     * We configure everything in the constructor so Eclipse can render the toolbar
     * button immediately without additional calls.
     */
    public OpenSearchAction()
    {
        super( Messages.getString( "OpenSearchAction.Search" ), Action.AS_PUSH_BUTTON ); //$NON-NLS-1$
        super.setText( Messages.getString( "OpenSearchAction.Search" ) ); //$NON-NLS-1$
        super.setToolTipText( Messages.getString( "OpenSearchAction.Search" ) ); //$NON-NLS-1$
        super.setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_SEARCH ) );
        super.setEnabled( true );
    }


    // ── R2 Initiates The Query ───────────────────────────────────────────────────
    // R2 sends the query packet — the terminal lights up, the search dialog appears.
    // We call NewSearchUI.openSearchDialog() pointed at our SearchPage ID so the
    // dialog opens on the LDAP tab, not a Java or file search tab.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Eclipse Search Dialog with the LDAP browser's search page active.
     * This is the core behaviour; both {@link #run()} and {@link #run(IAction)} delegate here.
     */
    public void run()
    {
        NewSearchUI.openSearchDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow(), SearchPage.getId() );
    }


    // ── R2 Connects To This Specific Workbench Terminal ──────────────────────────
    // R2 plugs his interface probe into the terminal that's in front of him right now.
    // init() is called by Eclipse when the action is registered with a specific window;
    // we don't need to store the window because we always go through PlatformUI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is bound to a workbench window.
     * We don't need to store the window reference because run() resolves it via
     * {@code PlatformUI.getWorkbench().getActiveWorkbenchWindow()} at call time.
     *
     * @param window  the workbench window this delegate is associated with
     */
    public void init( IWorkbenchWindow window )
    {
    }


    // ── Proxy Trigger: Action Wrapper Calls R2 ───────────────────────────────────
    // The Death Star's command console can trigger R2's query via its own button —
    // it's just a wrapper that calls R2's own run method.  This run(IAction) overload
    // is the IWorkbenchWindowActionDelegate form; we just delegate to our own run().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Implements {@link IWorkbenchWindowActionDelegate#run(IAction)}.
     * Eclipse calls this form when the action is triggered from the workbench toolbar.
     * We simply delegate to {@link #run()}.
     *
     * @param action  the proxy action Eclipse created; we ignore it and call our own run()
     */
    public void run( IAction action )
    {
        this.run();
    }


    // ── Terminal Doesn't Care About Selection ────────────────────────────────────
    // R2 doesn't need to know what the user clicked before he can open a terminal —
    // the search dialog is context-independent.  This method is a no-op.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the workbench selection changes.
     * Opening the search dialog doesn't depend on the selection, so we do nothing here.
     *
     * @param action     the proxy action
     * @param selection  the new selection; ignored
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
    }


    // ── R2 Unplugs Cleanly ──────────────────────────────────────────────────────
    // When R2 is done with a terminal he retracts his probe cleanly — no dangling
    // connections.  dispose() is called when the workbench window closes; we have
    // nothing to clean up so this is a no-op.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when this action delegate is no longer needed.
     * We hold no resources that need releasing, so this is a no-op.
     */
    public void dispose()
    {
    }
}
