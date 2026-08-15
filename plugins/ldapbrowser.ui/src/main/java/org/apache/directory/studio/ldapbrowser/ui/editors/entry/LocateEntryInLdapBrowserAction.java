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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: LocateEntryInLdapBrowserAction — R2-D2 FINDS THE TRACTOR BEAM ──────
// R2-D2 is plugged into the Death Star computer. He searches for the power
// coupling location, navigates the internal map, and pinpoints the exact panel
// in the Death Star where Obi-Wan needs to go to disable the tractor beam.
// LocateEntryInLdapBrowserAction does the same: from the entry editor, it finds
// the underlying LDAP entry, opens the LDAP Browser view if needed, and selects
// the entry in the DIT tree so the user can see it in context.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An action that locates the entry currently shown in the editor within the LDAP Browser DIT tree.
 * When triggered, it opens the Browser view (or brings it to the front) and highlights
 * the matching entry so users can see it in its directory context — parents, siblings, children.
 * Think of this as R2 providing the "you are here" pin on the Death Star map.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LocateEntryInLdapBrowserAction extends Action
{
    /** The entry editor */
    protected EntryEditor entryEditor;

    /** The menu manager */
    protected EntryEditorShowInMenuManager showInMenuManager;


    // ── R2 PLUGS IN AND LINKS TO HIS MISSION HANDLER ─────────────────────────
    // R2-D2 jacks into the terminal and establishes the link back to the rebel
    // team — so that when he finds the target, he knows who to report it to.
    // We store the editor and menu manager references so we can ask for the
    // current input when the action fires.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action and links it to the given entry editor and menu manager.
     * We need both: the editor to get the current entry input when the action runs,
     * and the menu manager to ask for the resolved input (which handles the
     * entry/search-result/bookmark distinction).
     *
     * @param entryEditor       The entry editor whose input we'll locate in the DIT.
     * @param showInMenuManager The menu manager that knows the current resolved input.
     */
    public LocateEntryInLdapBrowserAction( EntryEditor entryEditor, EntryEditorShowInMenuManager showInMenuManager )
    {
        super();
        this.entryEditor = entryEditor;
        this.showInMenuManager = showInMenuManager;
    }


    // ── R2 BEAMS THE LOCATION TO OBI-WAN ─────────────────────────────────────
    // R2 has the coordinates — he transmits them to Obi-Wan who walks straight
    // to the right panel without wandering through the Death Star corridors.
    // We resolve the entry from the editor input and call select() to highlight
    // it in the LDAP Browser view, opening the view first if it's not visible.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the locate action — finds and highlights the entry in the Browser view.
     * We resolve the {@link IEntry} from the editor input and delegate to {@link #select(Object)}
     * to open or reveal the Browser view and highlight the matching tree node.
     */
    public void run()
    {
        if ( entryEditor != null )
        {
            EntryEditorInput editorInput = entryEditor.getEntryEditorInput();

            if ( editorInput != null )
            {
                IEntry entry = editorInput.getResolvedEntry();

                if ( entry != null )
                {
                    select( entry );
                }
            }
        }
    }


    // ── R2 NAVIGATES OBI-WAN TO THE EXACT PANEL ───────────────────────────────
    // R2 opens the corridor map, finds the tractor beam panel, and guides Obi-Wan
    // directly to it — opening a new door if the panel's corridor isn't visible yet.
    // We find or open the Browser view and call its select() method to highlight
    // the given object in the DIT tree, then activate the view so it's front-and-center.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reveals and selects the given object in the LDAP Browser view.
     * If the Browser view isn't currently open, we open it first. Then we cast
     * to {@link BrowserView} and call its {@code select} method to highlight
     * the tree node corresponding to the object.
     *
     * @param o  The object to select in the Browser view; typically an {@link IEntry},
     *           {@link org.apache.directory.studio.ldapbrowser.core.model.ISearchResult},
     *           or {@link org.apache.directory.studio.ldapbrowser.core.model.IBookmark}.
     */
    protected void select( Object o )
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
            ( ( BrowserView ) targetView ).select( o );
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate( targetView );
        }
    }


    // ── R2 LABELS THE BUTTON ON THE TERMINAL ─────────────────────────────────
    // R2 outputs a label for the menu item so the crew knows which button to press.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display text for this action's menu item.
     * Shows a localized label naming the LDAP Browser view as the destination.
     *
     * @return the action's menu label string.
     */
    public String getText()
    {
        return Messages.getString( "LocateEntryInLdapBrowserAction.LDAPBrowser" ); //$NON-NLS-1$
    }


    // ── R2 OUTPUTS THE TOOLTIP FOR THE HOLO-DISPLAY ──────────────────────────
    // R2 formats a short tooltip so crew members hovering over the button
    // get a one-line explanation of what it does.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tooltip text for this action — same as the menu label.
     *
     * @return the tooltip string.
     */
    public String getToolTipText()
    {
        return getText();
    }


    // ── R2 ATTACHES THE RIGHT ICON TO THE BUTTON ──────────────────────────────
    // R2 selects the correct icon from the image registry — the one that looks
    // like a DIT tree with a pin in it — and attaches it to the menu item.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon image descriptor for this action.
     * We use the "locate entry in DIT" icon from the browser UI plugin's image registry.
     *
     * @return the {@link ImageDescriptor} for this action's icon.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_LOCATE_ENTRY_IN_DIT );
    }
}
