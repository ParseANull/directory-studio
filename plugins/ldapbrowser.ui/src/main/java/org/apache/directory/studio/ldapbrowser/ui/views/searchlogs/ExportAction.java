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

package org.apache.directory.studio.ldapbrowser.ui.views.searchlogs;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.wizards.ExportSearchLogsWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;


// ── CLASS: ExportAction — CLONE TROOPERS EXECUTING THE MISSION ───────────────
// Order 66 has been issued and the clone troopers execute it without hesitation:
// collect the data, move it out, deliver it to the designated location.
// This action is the troopers: it grabs the current search log view's input,
// spins up the ExportSearchLogsWizard, and lets the user specify exactly
// where the log files should land on disk outside the workspace.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the ExportSearchLogsWizard so the user can save search log files
 * to a location outside the Eclipse workspace.
 * The log files normally live in an internal workspace folder; this action
 * lets the user extract them for archiving, sharing, or analysis.
 * Think of the clone troopers executing their mission: take the data and
 * deliver it to the specified destination.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportAction extends BrowserAction
{

    // ── The Troopers Stand Ready ──────────────────────────────────────────────────
    // Clone troopers are always ready; they need no special briefing beyond
    // the view they're attached to.
    // This no-arg constructor is all we need — the view reference comes through
    // the superclass's setInput() mechanism.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportAction. No parameters needed — the view input is
     * provided later via the proxy's {@code inputChanged()} callback.
     */
    public ExportAction()
    {
    }


    // ── The Troopers Have No Special Command Code ─────────────────────────────────
    // Clone troopers operate on direct orders, not keybindings — no shortcut
    // is assigned for this export action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code null} — this action has no Eclipse command ID and is
     * triggered only via the toolbar button.
     *
     * @return  {@code null}.
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── The Troopers Wear Their Export Insignia ──────────────────────────────────
    // Each trooper's armour marks their role; ours marks the export function.
    // We return the export icon so the toolbar button is instantly recognisable.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the export icon image descriptor for the toolbar button.
     *
     * @return  the image descriptor for the export/save icon.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT );
    }


    // ── The Troopers Read Their Mission Brief ────────────────────────────────────
    // Every trooper knows what the mission is called; we return the localised
    // action label so menus and tooltips are clear.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised label for this action (e.g. "Export Search Logs").
     *
     * @return  the localised action name string.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "ExportAction.ExportSearchLogs" ); //$NON-NLS-1$
    }


    // ── The Troopers Check They Can Proceed ──────────────────────────────────────
    // Before executing a mission the troopers confirm the target is valid —
    // they don't fire at nothing.
    // We only enable the action when there's a live connection with log files
    // to export; no connection, no point showing the wizard.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns {@code true} only when the view has a valid {@link SearchLogsViewInput}
     * whose connection is currently active (not null).
     * Without a live connection the server's log files may not be accessible.
     *
     * @return  {@code true} if there is a valid, connected input to export.
     */
    @Override
    public boolean isEnabled()
    {
        return ( getInput() instanceof SearchLogsViewInput )
            && ( ( SearchLogsViewInput ) getInput() ).getBrowserConnection().getConnection() != null;
    }


    // ── The Troopers Execute ─────────────────────────────────────────────────────
    // Order confirmed, target locked — the troopers move out. The ExportSearchLogsWizard
    // opens, the user picks the destination, and the log files are delivered.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Opens the ExportSearchLogsWizard dialog pre-configured for the current
     * connection. The wizard guides the user through choosing a destination
     * path and then copies the log files there.
     */
    @Override
    public void run()
    {
        SearchLogsViewInput input = ( SearchLogsViewInput ) getInput();
        if ( input.getBrowserConnection().getConnection() != null )
        {
            ExportSearchLogsWizard wizard = new ExportSearchLogsWizard();
            wizard.getSearch().setBrowserConnection( input.getBrowserConnection() );
            WizardDialog dialog = new WizardDialog( getShell(), wizard );
            dialog.setBlockOnOpen( true );
            dialog.create();
            dialog.open();
        }
    }

}
