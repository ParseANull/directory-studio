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


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.connection.core.Utils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: PropertiesAction — C-3PO READS ALIEN DIGNITARY CREDENTIALS ────────
// Whenever the Millennium Falcon crew encounters a new dignitary or official,
// C-3PO steps forward and recites their full credentials: name, rank, title,
// affiliation, special protocols.  Formal, detailed, authoritative.
// That's exactly what the Properties action does: the user selects something
// in the browser — an entry, an attribute, a bookmark, a search — and we open
// the full property dialog so every detail is laid out for inspection.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse property dialog for whichever object is currently selected
 * in the LDAP browser.
 *
 * <p>Depending on what's selected (a value, attribute, attribute hierarchy,
 * search, bookmark, entry, or search result), we route to the appropriate
 * property page.  The selection logic figures out both the target object and
 * the page ID, then hands off to Eclipse's {@link PreferencesUtil} to build
 * and display the dialog.</p>
 *
 * <p>Think of this class as C-3PO: given any object in our galaxy, he can
 * look up and recite every formal detail about it — title, description,
 * protocols.  We do the same, just with LDAP objects.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PropertiesAction extends BrowserAction
{
    // ── C-3PO Powers Up, Ready to Recite ─────────────────────────────────────
    // C-3PO boots up in the briefing room — no special arguments needed.
    // He's ready to read out credentials the moment someone points at a subject.
    // Our constructor is equally minimal: just delegate to the parent.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code PropertiesAction}.  Delegates straight to the
     * parent constructor — the base class wires up all standard Eclipse action
     * plumbing.
     */
    public PropertiesAction()
    {
        super();
    }


    // ── C-3PO Announces His Function to the Room ─────────────────────────────
    // When asked what he does, C-3PO says plainly: "I am C-3PO, human-cyborg
    // relations."  Precise, formal, localized for the audience.
    // We return the localized label for this action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action — what shows up in
     * menus and tooltip text.  Typically reads "Properties".
     *
     * @return the localized action label; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "PropertiesAction.Properties" ); //$NON-NLS-1$
    }


    // ── C-3PO Has No Personal Insignia ───────────────────────────────────────
    // C-3PO is protocol, not combat — he carries no weapon emblem, no faction
    // badge.  Just the words.
    // The Properties action similarly has no custom icon; Eclipse supplies the
    // standard properties widget visually.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action.  Properties uses no custom icon —
     * Eclipse's property dialog provides its own visual identity — so we
     * return {@code null} here.
     *
     * @return {@code null} — no custom icon for this action
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── C-3PO Is Registered Under the Protocol Droid Authority ───────────────
    // In the Imperial registry, C-3PO's function code maps to "Protocol: read
    // credentials on demand."  Our command ID maps to Eclipse's standard
    // properties command so Ctrl+Enter (or platform equivalent) works.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse workbench command ID for the Properties action.
     * This wires us into the standard "Alt+Enter" (or platform equivalent)
     * keybinding so properties open consistently across the IDE.
     *
     * @return the workbench PROPERTIES command ID
     */
    public String getCommandId()
    {
        return IWorkbenchActionDefinitionIds.PROPERTIES;
    }


    // ── C-3PO Can Only Read Credentials for One Subject at a Time ────────────
    // C-3PO cannot simultaneously recite the titles of two dignitaries —
    // he focuses on exactly one subject.  If two are presented, he demurs.
    // We only enable Properties when exactly one object is selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this action should be enabled.  Properties only makes
     * sense when there is exactly one selectable object in context — one
     * entry, one search result, one bookmark, one search, one attribute, one
     * value, or one single-attribute hierarchy.  Multiple selections would
     * leave us with nothing coherent to show.
     *
     * @return {@code true} if exactly one object is selected; {@code false} otherwise
     */
    public boolean isEnabled()
    {

        return getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length
            + getSelectedSearches().length == 1
            || getSelectedAttributes().length + getSelectedValues().length == 1
            || ( getSelectedAttributeHierarchies().length == 1 && getSelectedAttributeHierarchies()[0].size() == 1 );

    }


    // ── C-3PO Recites the Full Dossier ───────────────────────────────────────
    // C-3PO sizes up the dignitary — is it a Hutt lord?  A senator?  A
    // protocol unit like himself?  He selects the right page from his databanks
    // and delivers the complete briefing.
    // We inspect what's selected, pick the right property page ID, and open
    // Eclipse's property dialog pointed at the correct page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the action: inspects the current selection to determine the
     * target element and the appropriate property page ID, then opens an
     * Eclipse property dialog.
     *
     * <p>For example — C-3PO identifies the dignitary and opens the right
     * dossier:</p>
     * <pre>
     *   C-3PO: "This is Jabba the Hutt — I'll pull the Crime Lord protocol."
     *   [Selects PROP_ENTRY page, points the dialog at the entry object]
     *   The full property dialog opens, showing every known detail.
     * </pre>
     *
     * <p>Priority order: value &gt; attribute &gt; attribute hierarchy &gt;
     * search &gt; bookmark &gt; entry &gt; search result.</p>
     */
    public void run()
    {

        IAdaptable element = null;
        String pageId = null;
        String title = null;

        if ( getSelectedValues().length == 1 )
        {
            element = ( IAdaptable ) getSelectedValues()[0];
            pageId = BrowserCommonConstants.PROP_VALUE;
            title = getSelectedValues()[0].toString();
        }
        else if ( getSelectedAttributes().length == 1 )
        {
            element = ( IAdaptable ) getSelectedAttributes()[0];
            pageId = BrowserCommonConstants.PROP_ATTRIBUTE;
            title = getSelectedAttributes()[0].toString();
        }
        else if ( getSelectedAttributeHierarchies().length == 1 )
        {
            IAttribute att = getSelectedAttributeHierarchies()[0].getAttribute();
            element = att;
            pageId = BrowserCommonConstants.PROP_ATTRIBUTE;
            title = att.toString();
        }
        else if ( getSelectedSearches().length == 1 )
        {
            element = ( IAdaptable ) getSelectedSearches()[0];
            pageId = BrowserCommonConstants.PROP_SEARCH;
            title = getSelectedSearches()[0].getName();
        }
        else if ( getSelectedBookmarks().length == 1 )
        {
            element = ( IAdaptable ) getSelectedBookmarks()[0];
            pageId = BrowserCommonConstants.PROP_BOOKMARK;
            title = getSelectedBookmarks()[0].getName();
        }
        else if ( getSelectedEntries().length == 1 )
        {
            element = ( IAdaptable ) getSelectedEntries()[0];
            pageId = BrowserCommonConstants.PROP_ENTRY;
            title = getSelectedEntries()[0].getDn().getName();
        }
        else if ( getSelectedSearchResults().length == 1 )
        {
            element = ( IAdaptable ) getSelectedSearchResults()[0];
            pageId = BrowserCommonConstants.PROP_ENTRY;
            title = getSelectedSearchResults()[0].getDn().getName();
        }

        if ( element != null )
        {
            PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn( getShell(), element, pageId, null, null );
            if ( dialog != null )
            {
                title = Utils.shorten( title, 30 );
            }

            dialog.getShell().setText( NLS.bind( Messages.getString( "PropertiesAction.PropertiesForX" ), title ) ); //$NON-NLS-1$
            dialog.open();

        }
    }
}
