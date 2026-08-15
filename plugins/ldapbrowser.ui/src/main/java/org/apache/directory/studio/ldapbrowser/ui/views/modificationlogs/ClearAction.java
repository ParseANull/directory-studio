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
package org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: ClearAction — OBI-WAN DISABLES THE TRACTOR BEAM ──────────────────
// Deep in the Death Star, Obi-Wan reaches the control terminal and, with quiet
// resolve, disengages the tractor beam that's holding the Millennium Falcon.
// The hold is gone — the Falcon is free. Nothing structural changes; the beam
// is simply switched off and the record is clear.
// This class does exactly that: it wipes the modification log files for the
// current connection, releasing storage and giving the user a clean slate.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toolbar/context action that deletes all on-disk modification log files
 * for the currently displayed connection.
 * This is destructive and permanent, so we always confirm with the user first.
 * Think of this as Obi-Wan disabling the tractor beam — once done, the hold
 * is gone and there's no pulling those log entries back.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ClearAction extends BrowserAction
{

    /** The modification logs view. */
    private ModificationLogsView view;


    // ── Constructor: Obi-Wan Locates the Control Terminal ────────────────────
    // Obi-Wan navigates the Death Star's corridors and finds the specific
    // control panel for the tractor beam — he needs to know exactly which
    // beam he's about to disable before touching anything.
    // We store the view reference so we can call clearInput() and refresh
    // on it when the user confirms the deletion.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this clear action with a reference to the modification logs view.
     * We need the view to call its universal listener's {@code clearInput()}
     * method and trigger a refresh after deletion.
     *
     * <p>For example — Obi-Wan identifies the tractor beam control panel:</p>
     * <pre>
     *   ClearAction action = new ClearAction( modificationLogsView );
     *   // action now knows which view's log files to delete
     * </pre>
     *
     * @param view  the modification logs view whose log files we will delete on run
     */
    public ClearAction( ModificationLogsView view )
    {
        this.view = view;
    }


    // ── getCommandId: Obi-Wan Has No Special Clearance Code ──────────────────
    // Obi-Wan doesn't need an Imperial clearance code for this terminal;
    // he accesses it directly through his own mastery of the situation.
    // We return null because this action isn't bound to a global Eclipse command.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action, or null if there isn't one.
     * We're not registered as a global command handler, so null is correct here.
     *
     * <p>For example — Obi-Wan bypasses the clearance system entirely:</p>
     * <pre>
     *   String id = action.getCommandId(); // null — no command binding needed
     * </pre>
     *
     * @return  always {@code null} — this action has no Eclipse command ID
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── getImageDescriptor: Obi-Wan Picks Up His Lightsaber ──────────────────
    // Obi-Wan reaches for his lightsaber before heading to the control panel —
    // the right tool identified for the job at hand.
    // We return the "clear" icon descriptor so the toolbar button looks right.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon to show on the toolbar button for this action.
     * We use the standard "clear" image from the plugin's image registry.
     *
     * <p>For example — Obi-Wan raises his lightsaber — the right tool, right moment:</p>
     * <pre>
     *   ImageDescriptor img = action.getImageDescriptor();
     *   // Returns the broom/clear icon for the toolbar
     * </pre>
     *
     * @return  the {@link ImageDescriptor} for the clear toolbar icon
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_CLEAR );
    }


    // ── getText: Obi-Wan Announces His Intent ────────────────────────────────
    // Before touching the panel, Obi-Wan quietly says "I'm disabling the beam"
    // so his allies know exactly what he's doing.
    // We return the localized label from the resource bundle for the menu/button.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action.
     * Used as the toolbar tooltip text and context-menu item label.
     *
     * <p>For example — Obi-Wan states his plan before acting:</p>
     * <pre>
     *   String label = action.getText(); // "Clear" (localized)
     * </pre>
     *
     * @return  the localized "Clear" label string
     */
    @Override
    public String getText()
    {
        return Messages.getString( "ClearAction.Clear" ); //$NON-NLS-1$
    }


    // ── isEnabled: Obi-Wan Checks That the Beam Is Actually On ───────────────
    // Obi-Wan doesn't walk to a panel that isn't controlling anything;
    // first he confirms the tractor beam is engaged and there's something to disable.
    // We return true only if the current view input is a valid log input object.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns true only when there's a valid modification log input loaded in the view.
     * We gate on the input type because clearing makes no sense when nothing is displayed.
     *
     * <p>For example — Obi-Wan confirms the tractor beam is active before touching the panel:</p>
     * <pre>
     *   boolean enabled = action.isEnabled();
     *   // true only if getInput() instanceof ModificationLogsViewInput
     * </pre>
     *
     * @return  {@code true} if the current input is a {@link ModificationLogsViewInput}
     */
    @Override
    public boolean isEnabled()
    {
        return getInput() instanceof ModificationLogsViewInput;
    }


    // ── run: Obi-Wan Disengages the Tractor Beam ─────────────────────────────
    // Obi-Wan confirms with a glance that this is the right moment, then flips
    // the switch — the tractor beam shuts off, the Falcon is free.
    // We show a confirmation dialog first; if the user confirms, we call
    // clearInput() on the listener and immediately refresh the view.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Deletes all modification log files for the current connection after user confirmation.
     * We show a modal confirm dialog; if the user clicks OK we call
     * {@link ModificationLogsViewUniversalListener#clearInput()} which wipes the files
     * and clears the text widget, then runs a {@link RefreshAction} to reset the view state.
     *
     * <p>For example — Obi-Wan disables the beam after one final check:</p>
     * <pre>
     *   if ( MessageDialog.openConfirm(...) ) {
     *       view.getUniversalListener().clearInput(); // beam off, logs gone
     *       new RefreshAction( view ).run();           // view reset
     *   }
     * </pre>
     */
    @Override
    public void run()
    {
        if ( MessageDialog.openConfirm( this.getShell(),
            Messages.getString( "ClearAction.Delete" ), Messages.getString( "ClearAction.DeleteAllLogFiles" ) ) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
            view.getUniversalListener().clearInput();
            new RefreshAction( view ).run();
        }
    }


    // ── dispose: Obi-Wan Steps Away From the Panel ───────────────────────────
    // After disabling the beam, Obi-Wan steps back from the control panel and
    // moves on — his work here is done, no resources left running.
    // We just delegate to super; the base class handles listener cleanup.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action.
     * We delegate entirely to the base {@link BrowserAction#dispose()} — there's
     * nothing extra to clean up here.
     *
     * <p>For example — Obi-Wan steps away, mission complete:</p>
     * <pre>
     *   action.dispose(); // super handles the cleanup
     * </pre>
     */
    @Override
    public void dispose()
    {
        super.dispose();
    }

}
