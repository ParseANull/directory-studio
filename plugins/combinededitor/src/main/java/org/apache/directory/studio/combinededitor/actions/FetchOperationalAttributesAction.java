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
package org.apache.directory.studio.combinededitor.actions;


import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.action.Action;


// ── CLASS: FetchOperationalAttributesAction — Han Reaches for His Blaster ────
// In the Mos Eisley cantina, Han Solo notices the Greedo situation and reaches
// for his blaster in one decisive motion — quick, targeted, no second-guessing.
// FetchOperationalAttributesAction works exactly that way: one click to toggle
// whether the browser fetches the hidden operational attributes of an LDAP entry
// and fires a background job to retrieve them if enabled.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toggles the fetching of operational attributes for the entry currently shown
 * in the combined editor.
 * Operational attributes (like {@code createTimestamp}, {@code entryUUID}) are
 * hidden by default because LDAP servers don't return them unless you ask.
 * This action asks — it flips the flag on the entry and fires a background
 * job to reload the attributes.
 * Think of this as Han reaching for his blaster: decisive, one action, done.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FetchOperationalAttributesAction extends Action
{
    /** The associated editor */
    private IEntryEditor editor;


    // ── Han Loads His Blaster — Wired to a Specific Target ───────────────────
    // Han picks up his DL-44 and confirms which target it's locked onto before
    // he sits down at the cantina table — everything is in place for when he
    // needs to act.
    // Our constructor stores the editor reference so run() knows which entry
    // to toggle when the user pulls the trigger.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action, wired to the given entry editor.
     * We store the editor so we can read its current entry when the action fires.
     *
     * @param editor  the entry editor whose current entry's operational-attribute
     *                flag this action toggles — must not be {@code null} at run time.
     */
    public FetchOperationalAttributesAction( IEntryEditor editor )
    {
        this.editor = editor;
    }


    // ── Han Confirms the Blaster Is Set to "Toggle" Mode ─────────────────────
    // Han's DL-44 has a mode selector; in this scene it's set to the checkbox
    // style — each press toggles the state rather than firing once and done.
    // getStyle() tells Eclipse to render this as a toggle/checkbox action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the action style so Eclipse renders it as a toggle (checkbox) button.
     * A toggle button shows its checked/unchecked state visually, which makes it
     * clear whether operational attributes are currently being fetched.
     *
     * @return  {@link Action#AS_CHECK_BOX} — this is a stateful toggle action.
     */
    @Override
    public int getStyle()
    {
        return Action.AS_CHECK_BOX;
    }


    // ── Han Announces the Action — "I'll Handle This" ─────────────────────────
    // Han says "I'll handle this" — the label for what he's about to do — so
    // everyone in the cantina knows what's happening.
    // getText() returns the human-readable label shown in menus and tooltips.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the action's display label for menus and toolbar tooltips.
     * We pull the text from the browser-common message bundle so it stays
     * consistent with the same action in the standalone entry editor.
     *
     * @return  the localised label for this action.
     */
    public String getText()
    {
        return org.apache.directory.studio.ldapbrowser.common.actions.Messages
            .getString( "FetchOperationalAttributesAction.FetchOperationalAttributes" ); //$NON-NLS-1$
    }


    // ── Han Checks Whether Greedo Is Still at the Table ──────────────────────
    // Han won't reach for his blaster if Greedo has already left — the action
    // is only meaningful when there's a valid target in the seat.
    // isEnabled() returns true only when there's a real entry loaded in the
    // editor and the connection isn't already fetching operational attributes
    // (in which case there's nothing for us to toggle).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this action is currently available for the user to trigger.
     * We're disabled if there's no entry in the editor, or if the connection-level
     * "fetch operational attributes" flag is already on (because then individual
     * entry-level fetching adds nothing — everything is already being fetched).
     *
     * @return  {@code true} if the action can be triggered right now.
     */
    public boolean isEnabled()
    {
        if ( editor != null )
        {
            IEntry entry = editor.getEntryEditorInput().getResolvedEntry();
            if ( entry != null )
            {
                entry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );

                return !entry.getBrowserConnection().isFetchOperationalAttributes();
            }
        }

        return false;
    }


    // ── Han Fires — The Operational Attributes Fetch Begins ──────────────────
    // Han reaches for his blaster, toggles the safety, and fires — the Greedo
    // problem is resolved in one decisive action.
    // run() flips the entry's "init operational attributes" flag and fires a
    // background StudioBrowserJob to reload the attributes from the server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Toggles operational-attribute fetching for the current entry and reloads.
     * We flip the entry's {@code initOperationalAttributes} flag — if it was off
     * we turn it on (and the reload job fetches them from the server); if it was
     * on we turn it off (and the reload job omits them next time).
     * The reload runs in a background {@link StudioBrowserJob} so the UI stays
     * responsive.
     */
    public void run()
    {
        if ( editor != null )
        {
            IEntry entry = editor.getEntryEditorInput().getResolvedEntry();
            entry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );

            boolean init = !entry.isInitOperationalAttributes();
            entry.setInitOperationalAttributes( init );
            new StudioBrowserJob( new InitializeAttributesRunnable( entry ) ).execute();
        }
    }
}
