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
package org.apache.directory.studio.templateeditor.actions;


import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: RefreshAction — R2-D2 RUNNING A DIAGNOSTIC SCAN ──────────────────────
// R2-D2 plugs into the Jedi starfighter's computer and runs a full diagnostic:
// "Beep-boop! Re-fetching all attributes from the server." He doesn't care what
// the previous reading said — he goes back to the source and pulls fresh data.
// This action does the same: when triggered, it re-fetches all attributes for the
// currently displayed LDAP entry from the live LDAP server, discarding any
// cached/stale values. The refresh runs in a background job via the Studio
// connection framework so the UI stays responsive during the operation.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Action that re-fetches all attributes for the currently displayed LDAP entry
 * from the live directory server. Delegates to {@link InitializeAttributesRunnable}
 * wrapped in a {@link StudioConnectionJob} so the network call runs in the
 * background. The action is only enabled when the editor has a resolved entry to
 * refresh. Think of this as R2-D2 running a full diagnostic scan on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RefreshAction extends Action
{
    /** The associated editor */
    private IEntryEditor editor;


    // ── CONSTRUCTOR: R2-D2 ASSIGNED TO A STARFIGHTER ─────────────────────────────
    // R2-D2 is plugged into a specific ship's computer. This action is assigned to
    // a specific entry editor — it always refreshes that editor's current entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action bound to the given entry editor. The editor reference
     * is used to locate the current entry when the user triggers a refresh.
     *
     * <p>For example — R2-D2 is plugged into the X-Wing:</p>
     * <pre>
     *   new RefreshAction(templateEntryEditor);
     *   // "R2 assigned. Ready to run diagnostics on demand."
     * </pre>
     *
     * @param editor  the entry editor whose current entry will be refreshed
     */
    public RefreshAction( IEntryEditor editor )
    {
        this.editor = editor;
    }


    // ── GET IMAGE DESCRIPTOR: R2-D2 DISPLAYS HIS REFRESH ICON ────────────────────
    // R2-D2 lights up his "running diagnostic" indicator. We delegate to the
    // browser-common image registry to get the standard refresh icon so we're
    // consistent with other refresh actions in the LDAP browser.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the standard "Refresh" image descriptor from the browser-common
     * image registry. This keeps our toolbar icon visually consistent with the
     * rest of the LDAP browser.
     *
     * @return the refresh {@link ImageDescriptor}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_REFRESH );
    }


    // ── GET TEXT: R2-D2 ANNOUNCES WHAT HE'S ABOUT TO DO ─────────────────────────
    // R2-D2 beeps in English: "Reload Attributes." We delegate to the browser
    // common message bundle for the label so it stays localized and consistent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized action label. Delegates to the browser-common message
     * bundle so the text is consistent with the same action in other editors.
     *
     * @return the localized "Reload Attributes" action label
     */
    public String getText()
    {
        return org.apache.directory.studio.ldapbrowser.common.actions.Messages
            .getString( "RefreshAction.RelaodAttributes" ); //$NON-NLS-1$
    }


    // ── IS ENABLED: R2-D2 CHECKS IF THERE IS A SHIP TO SCAN ─────────────────────
    // R2-D2 can't run a diagnostic if there's no ship. We're enabled only when
    // the editor has a resolved entry — otherwise there's nothing to refresh.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} only when the editor has a resolved (non-{@code null})
     * LDAP entry to refresh. Without one, firing the action would do nothing useful.
     *
     * @return {@code true} if an entry is available; {@code false} otherwise
     */
    public boolean isEnabled()
    {
        if ( editor != null )
        {
            return ( editor.getEntryEditorInput().getResolvedEntry() != null );
        }

        return false;
    }


    // ── GET ACTION DEFINITION ID: R2-D2 REGISTERS WITH THE STANDARD SHORTCUT ─────
    // R2-D2 responds to the standard "F5 = Refresh" keyboard binding by advertising
    // the Eclipse platform's standard refresh command ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse platform refresh command ID so this action binds to the
     * standard F5 keyboard shortcut.
     *
     * @return the command ID {@code "org.eclipse.ui.file.refresh"}
     */
    public String getActionDefinitionId()
    {
        return "org.eclipse.ui.file.refresh"; //$NON-NLS-1$
    }


    // ── RUN: R2-D2 RUNS THE FULL DIAGNOSTIC ──────────────────────────────────────
    // R2-D2 fires up the diagnostic and re-initializes all attribute values from
    // the server. The job runs in a background thread via StudioConnectionJob so
    // the UI doesn't freeze during the LDAP network call.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Re-fetches all attributes for the currently displayed entry by scheduling
     * an {@link InitializeAttributesRunnable} via a {@link StudioConnectionJob}.
     * The job runs in a background thread; the editor will update itself when the
     * attributes arrive.
     *
     * <p>For example — R2-D2 runs the full diagnostic:</p>
     * <pre>
     *   new StudioConnectionJob(new InitializeAttributesRunnable(entry)).execute();
     *   // All attributes re-fetched from the LDAP server in the background.
     * </pre>
     */
    public void run()
    {
        if ( editor != null )
        {
            IEntry entry = editor.getEntryEditorInput().getResolvedEntry();
            new StudioConnectionJob( new InitializeAttributesRunnable( entry ) ).execute();
        }
    }
}
