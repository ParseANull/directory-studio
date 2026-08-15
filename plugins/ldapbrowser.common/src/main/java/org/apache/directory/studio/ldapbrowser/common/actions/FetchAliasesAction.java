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

package org.apache.directory.studio.ldapbrowser.common.actions;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: FetchAliasesAction — LUKE CALLING HIS LIGHTSABER FROM THE SNOW ────
// Luke Skywalker is hanging upside down in a wampa's cave on Hoth. His
// lightsaber is buried in the snow just out of reach. He reaches out with
// the Force, and after a moment — it flies into his hand. That's the key
// toggle here: when alias dereferencing is enabled on the connection, a
// selected LDAP entry may have "alias" children (pointers to other entries
// elsewhere in the tree). This checkbox action controls whether we follow
// those pointers to fetch the real entries, or leave the aliases as-is.
// Toggle it on and we re-initialize the entry's children with alias following
// enabled. Toggle it off and we stop following the pointer.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A checkbox action that toggles whether alias entries are dereferenced (followed)
 * when loading the children of the selected LDAP entries. When enabled, LDAP alias
 * objects in the tree are resolved to their target entries rather than shown as
 * plain alias nodes. Only available when the connection's alias-dereferencing
 * method is not set to NEVER.
 * Think of this class as Luke's Force pull — it reaches out past the obvious
 * path to fetch what's really there.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FetchAliasesAction extends BrowserAction
{
    // ── LUKE ARRIVES IN THE CAVE ───────────────────────────────────────────────
    // Luke is new to the situation — no special setup required. The action
    // gets its state by querying the current selection at runtime.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new FetchAliasesAction. No configuration is needed at
     * construction time — the enabled/checked states are computed dynamically
     * from the current entry selection.
     */
    public FetchAliasesAction()
    {
    }


    // ── LUKE'S MOVE IS A FORCE PULL, NOT A PUSH ───────────────────────────────
    // This action renders as a checkbox in the menu — toggling it on and off
    // controls the fetch-aliases state for the selected entries.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@link Action#AS_CHECK_BOX}, indicating this action renders as a
     * toggle checkbox in menus. The checked state reflects whether alias
     * dereferencing is currently active for the selected entries.
     *
     * @return the SWT/JFace style constant for a checkbox action.
     */
    @Override
    public int getStyle()
    {
        return Action.AS_CHECK_BOX;
    }


    // ── LUKE CALLS THE LIGHTSABER BY NAME ─────────────────────────────────────
    // The label is the action's name in the context menu — fetched from the NLS
    // message bundle so it can be localized.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action ("Fetch Aliases" or the localized
     * equivalent). Retrieved from the NLS message bundle.
     *
     * @return the localized action label.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "FetchOperationalAttributesAction.FetchAliases" ); //$NON-NLS-1$
    }


    // ── LUKE REACHES OUT IN SILENCE — NO ICON ─────────────────────────────────
    // This action has no icon — it appears as text only in the context menu.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this action has no icon and appears as text only.
     *
     * @return null.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── NO KEYBINDING — THE FORCE HAS NO SHORTCUT ─────────────────────────────
    // No standard Eclipse command ID is associated with this action.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this action has no Eclipse command ID and no
     * keyboard shortcut.
     *
     * @return null.
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── LUKE CAN ONLY REACH OUT IF THE FORCE PERMITS ──────────────────────────
    // The action is available only when there are entries selected AND the
    // connection's alias-dereferencing method is not NEVER. If the connection
    // is configured to never dereference aliases, there is nothing to toggle.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when at least one entry is selected and the connection's
     * alias-dereferencing method is not {@link AliasDereferencingMethod#NEVER}.
     * If the connection is set to never follow aliases, this action cannot do
     * anything useful and is disabled.
     *
     * @return {@code true} if alias fetching can be toggled for the current selection.
     */
    @Override
    public boolean isEnabled()
    {
        List<IEntry> entries = getEntries();
        return !entries.isEmpty()
            && entries.iterator().next().getBrowserConnection().getAliasesDereferencingMethod() != AliasDereferencingMethod.NEVER;
    }


    // ── LUKE CHECKS WHETHER THE LIGHTSABER IS ALREADY IN HIS HAND ────────────
    // The checkbox is checked when ALL selected entries already have
    // fetchAliases = true. If even one doesn't, we show it unchecked.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when all selected entries currently have alias
     * fetching enabled ({@code isFetchAliases() == true}). Returns {@code false}
     * if any entry has it disabled, or if the selection is empty.
     *
     * @return {@code true} if all selected entries are currently fetching aliases.
     */
    @Override
    public boolean isChecked()
    {
        boolean checked = true;
        List<IEntry> entries = getEntries();
        if ( entries.isEmpty() )
        {
            checked = false;
        }
        else
        {
            for ( IEntry entry : entries )
            {
                if ( !entry.isFetchAliases() )
                {
                    checked = false;
                }
            }
        }
        return checked;
    }


    // ── LUKE PULLS — THE LIGHTSABER FLIES INTO HIS HAND ──────────────────────
    // When the user clicks the checkbox, we toggle the fetchAliases flag on
    // every selected entry to the opposite of the current checked state, then
    // fire off an InitializeChildrenRunnable to re-fetch the children with the
    // new setting applied. The UI refreshes to show or hide alias-resolved entries.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Toggles alias fetching for all selected entries and re-initializes their
     * children. Sets each entry's {@code fetchAliases} flag to the opposite of
     * the current checked state, then dispatches a background
     * {@link InitializeChildrenRunnable} to reload the children with the new setting.
     *
     * <p>For example — Luke's Force pull reaching past the obvious path:</p>
     * <pre>
     *   entry.setFetchAliases( !isChecked() );  // flip the toggle
     *   new StudioBrowserJob( new InitializeChildrenRunnable( true, entries ) ).execute();
     * </pre>
     */
    @Override
    public void run()
    {
        IEntry[] entries = getEntries().toArray( new IEntry[0] );
        boolean init = !isChecked();
        for ( IEntry entry : entries )
        {
            entry.setFetchAliases( init );
        }
        new StudioBrowserJob( new InitializeChildrenRunnable( true, entries ) ).execute();
    }


    // ── LUKE IDENTIFIES WHICH ENTRIES HE CAN REACH ────────────────────────────
    // Returns the list of entries in the current selection. Only directly selected
    // entries count — search results and bookmarks are not included for this action.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP entries targeted by this action — specifically, the
     * directly selected entries only (search results and bookmarks are excluded).
     *
     * @return a list of selected entries; may be empty but never null.
     */
    protected List<IEntry> getEntries()
    {
        List<IEntry> entriesList = new ArrayList<IEntry>();
        entriesList.addAll( Arrays.asList( getSelectedEntries() ) );
        return entriesList;
    }

}
