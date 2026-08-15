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

import org.apache.directory.studio.connection.core.ConnectionManager;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: SelectAllAction — PALPATINE'S "SO BE IT, JEDI" ────────────────────
// In Return of the Jedi, the Emperor looks out at the Rebel fleet, at Luke,
// at everything — and declares "So be it, Jedi" with a sweeping, totalizing
// calm.  It's a gesture of absolute dominion: everything in his field of view
// falls under his command in that instant.  That's Select All: one action that
// sweeps through the entire visible contents of the current viewer and claims
// them all in the selection.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements the "Select All" action for the LDAP browser viewers.
 *
 * <p>Depending on what's loaded as the viewer's input, we select different
 * things:</p>
 * <ul>
 *   <li><b>Entry input</b> — selects all attributes and all their values</li>
 *   <li><b>ConnectionManager input</b> — selects all connections</li>
 *   <li><b>Connection in a ConnectionManager viewer</b> — selects all connections</li>
 * </ul>
 *
 * <p>Think of Palpatine surveying the battle and saying "everything you see is
 * mine" — we walk the entire visible content and mark it all as selected.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SelectAllAction extends BrowserAction
{
    private Viewer viewer;


    // ── Palpatine Takes His Seat on the Throne ────────────────────────────────
    // The Emperor settles into the throne aboard the Death Star with a
    // specific view in mind — he needs to see the whole battle from up there.
    // We likewise need a specific viewer reference so we know whose contents
    // to select.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code SelectAllAction} bound to the given viewer.
     * We hold onto the viewer reference so {@link #run()} knows which widget
     * to push the selection into.
     *
     * @param viewer  the {@link Viewer} whose full contents will be selected
     *                when this action fires; must not be {@code null}
     */
    public SelectAllAction( Viewer viewer )
    {
        this.viewer = viewer;
    }


    // ── The Emperor's Decree Is Simply Named ─────────────────────────────────
    // No flowery preamble — "Select All" is the declaration, plain and total.
    // We return the localized label from the message bundle.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action — typically
     * "Select All".
     *
     * @return the localized label string; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "SelectAllAction.SelectAll" ); //$NON-NLS-1$
    }


    // ── The Emperor Needs No Personal Emblem ─────────────────────────────────
    // Palpatine's authority is implicit — he doesn't need a badge on his robe.
    // Select All has no custom icon; the label carries all the meaning.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action.  Select All has no custom icon, so
     * we return {@code null} — the action appears as text in menus.
     *
     * @return {@code null} — no custom icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── The Emperor's Frequency Is Universal ─────────────────────────────────
    // Every officer on every ship knows the frequency the Emperor broadcasts on.
    // Our command ID is the standard Eclipse SELECT_ALL command — Ctrl+A on any
    // platform hits us through the command framework.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse workbench SELECT_ALL command ID.  This ties us to
     * the Ctrl+A (or platform equivalent) keybinding.
     *
     * @return the workbench SELECT_ALL command ID
     */
    public String getCommandId()
    {
        return IWorkbenchActionDefinitionIds.SELECT_ALL;
    }


    // ── The Emperor Is Always Ready to Declare Dominion ──────────────────────
    // "So be it, Jedi" — Palpatine is never NOT ready to make that declaration.
    // Select All is always enabled; there's no situation where it should be
    // grayed out.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * This action is always enabled — there's no context in which Select All
     * makes no sense.
     *
     * @return {@code true}, always
     */
    public boolean isEnabled()
    {
        return true;
    }


    // ── "Everything You See Is Mine" ─────────────────────────────────────────
    // Palpatine gestures broadly and every element in the room is now under
    // his dominion — entries, attributes, values, connections, all of them.
    // We walk the viewer's input and build a complete selection list, then
    // push it into the viewer so everything appears highlighted.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the action: builds a complete selection from the viewer's
     * current input and pushes it back into the viewer.
     *
     * <p>For example — the Emperor's declaration sweeps everything:</p>
     * <pre>
     *   Palpatine: "So be it, Jedi." [gestures at the entire battle]
     *   Every ship, every trooper, every plan falls under his gaze.
     *   viewer.setSelection(new StructuredSelection(everything));
     * </pre>
     *
     * <ul>
     *   <li>If the input is an {@link IEntry}, we select all of its attributes
     *       and all of each attribute's values.</li>
     *   <li>If the input is a {@link ConnectionManager} (or connections are
     *       already selected in a ConnectionManager viewer), we select all
     *       connections.</li>
     * </ul>
     */
    public void run()
    {
        if ( getInput() instanceof IEntry )
        {
            List selectionList = new ArrayList();
            IAttribute[] attributes = ( ( IEntry ) getInput() ).getAttributes();
            if ( attributes != null )
            {
                selectionList.addAll( Arrays.asList( attributes ) );
                for ( int i = 0; i < attributes.length; i++ )
                {
                    selectionList.addAll( Arrays.asList( attributes[i].getValues() ) );
                }
            }
            StructuredSelection selection = new StructuredSelection( selectionList );
            this.viewer.setSelection( selection );
        }
        else if ( getInput() instanceof ConnectionManager )
        {
            StructuredSelection selection = new StructuredSelection( ( ( ConnectionManager ) getInput() )
                .getConnections() );
            this.viewer.setSelection( selection );
        }
        else if ( getSelectedConnections().length > 0 && viewer.getInput() instanceof ConnectionManager )
        {
            StructuredSelection selection = new StructuredSelection( ( ( ConnectionManager ) viewer.getInput() )
                .getConnections() );
            this.viewer.setSelection( selection );
        }
    }
}
