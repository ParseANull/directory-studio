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


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenSearchResultAction — LUKE'S BINARY SUNSET ─────────────────────
// After R2-D2 delivers Leia's message, Luke doesn't just file it away — he walks
// out to the ridge and stands there, taking in the full picture of the twin suns
// on the horizon.  OpenSearchResultAction does the same with a search result: the
// user has run a search and found an entry, and now they want to see where it lives
// in the full directory tree.  This action reveals the big picture — it locates the
// selected search result in the Browser View so the user can see its context.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Reveals the selected search result in the LDAP Browser View, showing the entry
 * in its full directory-tree context.
 * A search result is just a node in a flat list; this action takes that node and
 * selects the corresponding entry in the hierarchical browser so the user can see
 * where it fits in the larger picture.
 * Think of this class as Luke stepping out to the ridge: it pulls back the camera
 * so you see the whole landscape, not just the item in isolation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSearchResultAction extends BrowserAction
{
    // ── Luke Grabs His Cloak Before Heading Out ──────────────────────────────────
    // Luke doesn't go to the ridge unprepared — he grabs what he needs first.  Our
    // constructor calls super() to wire into the BrowserAction framework so we have
    // access to the selection service when run() fires.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenSearchResultAction and wires it into the BrowserAction framework.
     */
    public OpenSearchResultAction()
    {
        super();
    }


    // ── Luke Walks Out And Takes In The Horizon ──────────────────────────────────
    // Luke steps outside, finds the ridge, and stands there looking at the twin
    // suns.  run() finds the Browser View (opening it if it isn't visible), then
    // calls select() on it to highlight the search result's entry — making the user
    // see the full context around that one node.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reveals the selected search result in the Browser View.
     * If the Browser View isn't currently open we open and activate it first.
     * Then we call {@code BrowserView.select()} with the search result so the
     * tree expands to show the entry and selects it.
     * Does nothing if no search result is selected or if more than one is.
     */
    public void run()
    {
        if ( getSelectedSearchResults().length == 1 )
        {
            String targetId = BrowserView.getId();
            IViewPart targetView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
                targetId );
            if ( targetView == null )
            {
                try
                {
                    targetView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                        targetId, null, IWorkbenchPage.VIEW_ACTIVATE );
                }
                catch ( PartInitException e )
                {
                }
            }
            if ( targetView instanceof BrowserView )
            {
                ( ( BrowserView ) targetView ).select( getSelectedSearchResults()[0] );
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate( targetView );
            }
        }
    }


    // ── The Horizon Has A Name ───────────────────────────────────────────────────
    // Luke could always put a name to what he was looking at — "that's Tatooine's
    // second sun."  getText() returns the localised label for this action so Eclipse
    // can display it clearly in context menus.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display label for this action.
     *
     * @return the menu-item text, e.g. "Open Search Result in Browser"
     */
    public String getText()
    {
        return Messages.getString( "OpenSearchResultAction.OpenResult" ); //$NON-NLS-1$
    }


    // ── The Sunset Has Its Own Visual ───────────────────────────────────────────
    // The binary sunset has a distinctive look — warm orange light.  Our icon
    // gives the action a recognisable visual presence in menus and toolbars.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's icon.
     *
     * @return the image descriptor for the "open search result" icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_OPEN_SEARCHRESULT );
    }


    // ── Alliance Has A Standard Command For This ─────────────────────────────────
    // The Rebel Alliance had a standard code for "show me the full picture" — we
    // bind to a registered Eclipse command ID so keybindings can trigger us too.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding.
     * We use the registered open-search-result command so keyboard shortcuts work.
     *
     * @return the command ID for "open search result"
     */
    public String getCommandId()
    {
        return BrowserUIConstants.CMD_OPEN_SEARCH_RESULT;
    }


    // ── You Need One Result To Walk Toward ───────────────────────────────────────
    // Luke can only walk toward a sunset that exists — if there's no sun on the
    // horizon, there's nowhere to go.  We enable this action only when exactly one
    // search result is selected.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether this action is available for the current selection.
     * We require exactly one search result to be selected — more or fewer and the
     * action is meaningless.
     *
     * @return true if exactly one search result is currently selected
     */
    public boolean isEnabled()
    {
        return getSelectedSearchResults().length == 1;
    }
}
