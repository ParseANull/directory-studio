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

package org.apache.directory.studio.actions;


import java.net.MalformedURLException;
import java.net.URL;

import org.apache.directory.studio.Messages;
import org.apache.directory.studio.PluginConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;


// ── CLASS: ReportABugAction — R2-D2 Transmits the Death Star Plans ───────────
// At the end of the Battle of Yavin, R2-D2 beams the collected battle data
// back to Rebel HQ so the engineers can study what went right and what went
// wrong — one quick transmission opens a channel directly to the analysts.
// ReportABugAction does exactly the same: when the user clicks "Report a Bug"
// we open the user's default web browser and navigate directly to the project's
// JIRA bug-tracker so they can file a report with one click.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the project's JIRA bug-tracker in the user's default web browser.
 * This is the action behind Help &gt; Report a Bug — it asks Eclipse to open
 * the external browser and navigate to the JIRA new-issue URL defined in
 * our message bundle.
 * Think of this as R2-D2 opening a direct transmission channel to Rebel HQ.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReportABugAction extends Action implements IWorkbenchWindowActionDelegate
{

    /** The workbench window */
    private IWorkbenchWindow workbenchWindow;


    // ── R2 Prepares the Transmission Without Knowing the Destination Yet ──────
    // R2 spins up his long-range transmitter and sets his ID before Leia has
    // confirmed which frequency to use — he'll get those details in a moment.
    // This no-arg constructor lets Eclipse create the action via the plugin.xml
    // extension point, before the workbench window is assigned via init().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action with its ID, label, and tooltip but no window reference yet.
     * Eclipse can instantiate actions via extension points before the workbench
     * window is available; {@link #init(IWorkbenchWindow)} provides it later.
     */
    public ReportABugAction()
    {
        setId( PluginConstants.ACTION_REPORT_A_BUG_ID ); //$NON-NLS-1$
        setText( Messages.getString( "ReportABugAction.Report_a_bug" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "ReportABugAction.Open_a_web_browser" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── R2 Gets the Frequency and Is Ready to Transmit ───────────────────────
    // Leia tells R2 "Channel 47, full power" and he locks in the workbench window
    // reference so he knows where to get the browser support object when the
    // transmission fires.  This constructor does both steps in one go.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action and immediately initialises it with the workbench window.
     * Convenience constructor for callers (like
     * {@link org.apache.directory.studio.ApplicationActionBarAdvisor}) that
     * already have the window at construction time.
     *
     * @param window  the workbench window — stored so {@link #run()} can obtain
     *                the browser support object.
     */
    public ReportABugAction( IWorkbenchWindow window )
    {
        this();
        init( window );
    }


    // ── R2 Powers Down the Transmitter After the Mission ──────────────────────
    // After the battle R2 powers down his long-range transmitter and clears
    // the frequency lock — nothing is held open once the mission is over.
    // dispose() nulls the window reference so we don't hold a stale pointer
    // to a disposed workbench window.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the workbench window reference when the action is disposed.
     * Eclipse calls this when the window closes so we don't hold a stale pointer.
     */
    public void dispose()
    {
        workbenchWindow = null;
    }


    // ── Leia Confirms the Frequency — R2 Locks It In ─────────────────────────
    // Leia reads out the channel frequency and R2 stores it in his nav system
    // so he can open the link when the transmission order comes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the workbench window reference for later use in {@link #run()}.
     * Eclipse calls this when our action delegate is wired up to a window.
     *
     * @param window  the workbench window — we use it to obtain the browser
     *                support object when the user triggers the action.
     */
    public void init( IWorkbenchWindow window )
    {
        workbenchWindow = window;
    }


    // ── The Mission Dispatcher Signals R2 — He Relays to Himself ─────────────
    // The mission dispatcher calls "R2, transmit now" and R2 simply passes the
    // signal on to his own transmission sequence.
    // run(IAction) satisfies the IWorkbenchWindowActionDelegate contract; we
    // ignore the proxy action and call the real run() directly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} — the IWorkbenchWindowActionDelegate callback.
     *
     * @param action  the proxy action — not used.
     */
    public void run( IAction action )
    {
        run();
    }


    // ── R2 Doesn't Care Who's Watching — He'll Transmit Regardless ────────────
    // R2 doesn't need to know who's looking at the galaxy map to decide whether
    // to open the JIRA link — the action is always enabled.
    // selectionChanged() exists only to satisfy the delegate contract.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Not used — this action is always enabled regardless of the current selection.
     * Exists only to satisfy the {@link IWorkbenchWindowActionDelegate} contract.
     *
     * @param action     the proxy action — not used.
     * @param selection  the current workbench selection — not used.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
    }


    // ── R2 Opens the Transmission Channel to Rebel HQ ────────────────────────
    // R2 spins up his antenna, locks onto the JIRA frequency hardcoded in
    // his memory banks, and opens a direct channel — the engineers at HQ
    // immediately see a new incoming report request.
    // run() asks Eclipse to open the external browser and navigate to the
    // JIRA URL pulled from our message bundle. Errors are silently swallowed
    // because there's nothing useful we can do if the browser won't open.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the project's JIRA issue tracker in the user's default external browser.
     * We ask the Eclipse browser support layer to launch the external browser with
     * the JIRA new-issue URL.  Any errors (browser not found, malformed URL) are
     * silently ignored — the user simply won't see a browser window open.
     */
    public void run()
    {
        try
        {
            workbenchWindow.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(
                new URL( Messages.getString( "ReportABugAction.JIRA_URL" ) ) ); //$NON-NLS-1$
        }
        catch ( PartInitException e )
        {
        }
        catch ( MalformedURLException e )
        {
        }
    }

}
