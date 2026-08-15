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
import org.apache.directory.studio.ldapbrowser.ui.search.SearchPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewSearchAction — R2-D2 PLUGS INTO THE DEATH STAR TERMINAL ────────
// On the Death Star, R2-D2 rolls up to an information terminal, extends his probe,
// and initiates a fresh query session — "Help me find the detention block controls."
// He doesn't reuse a stale connection; he starts a brand new query.  NewSearch-
// Action does exactly that: it opens the Eclipse Search Dialog with our custom LDAP
// search page ready, giving the user a fresh terminal to query the directory.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse Search Dialog with the LDAP browser's search page displayed,
 * letting the user define and run a new LDAP search against the connected directory.
 * Think of this as R2-D2 rolling up to a fresh terminal — a clean query session,
 * no previous results muddying the picture.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewSearchAction extends BrowserAction
{
    // ── R2 Powers Up His Query Probe ────────────────────────────────────────────
    // R2-D2 extends his interface probe before he reaches the terminal — he's
    // ready to connect the moment he gets there.  Our constructor calls super()
    // so the BrowserAction infrastructure is fully initialised before run() fires.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NewSearchAction and wires it into the Eclipse action framework.
     * The parent BrowserAction sets up selection listeners; we just prep ourselves.
     */
    public NewSearchAction()
    {
        super();
    }


    // ── R2 Initiates A Fresh Query Session ──────────────────────────────────────
    // R2 plugs in, clears the previous session, and opens a new query prompt.
    // We call NewSearchUI.openSearchDialog() with our SearchPage ID so Eclipse opens
    // the search dialog on the LDAP tab, not some other search page.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Eclipse Search Dialog, pre-selecting the LDAP browser's search page.
     * The user can fill in a base DN, search filter, scope, and attributes to return,
     * then run the search against the connected directory.
     */
    public void run()
    {
        NewSearchUI.openSearchDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow(), SearchPage.getId() );
    }


    // ── Terminal Shows The Query Name ────────────────────────────────────────────
    // The Death Star terminal displayed the query name as R2 worked.  getText()
    // returns the localised label Eclipse puts in menus and toolbars for this action.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display label for this action.
     *
     * @return the menu-item text, e.g. "New Search…"
     */
    public String getText()
    {
        return Messages.getString( "NewSearchAction.NewSearch" ); //$NON-NLS-1$
    }


    // ── R2's Query Light Blinks On ──────────────────────────────────────────────
    // When R2 plugs in, a little blue light blinks on his dome — you know he's
    // searching.  Our search-new icon plays that role in the UI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's toolbar/menu icon.
     *
     * @return the image descriptor for the "new search" icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_SEARCH_NEW );
    }


    // ── R2 Uses The Standard Search Terminal Protocol ────────────────────────────
    // R2 doesn't invent a custom protocol — he uses the standard Death Star terminal
    // interface.  We bind to Eclipse's standard "open search dialog" command ID so
    // the platform's default keybinding (usually Ctrl+H) also triggers our action.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding.
     * We use the platform's standard "openSearchDialog" command ID so keyboard
     * shortcuts defined for opening the search dialog also activate this action.
     *
     * @return the Eclipse search dialog command ID
     */
    public String getCommandId()
    {
        return "org.eclipse.search.ui.openSearchDialog"; //$NON-NLS-1$
    }


    // ── R2 Is Always Ready To Query ─────────────────────────────────────────────
    // R2 can plug into any terminal at any time — he doesn't need a pre-selected
    // node.  Starting a new search has no selection preconditions, so we always
    // return true.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether the New Search action is available.
     * Opening the search dialog has no selection prerequisites, so this always
     * returns true.
     *
     * @return always true
     */
    public boolean isEnabled()
    {
        return true;
    }
}
