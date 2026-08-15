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


import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.actions.CopyAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;


// ── CLASS: CopyUrlAction — C-3PO TRANSLATES THE ADDRESS INTO URL PROTOCOL ───
// When the Rebellion needs to share a location with another ship, C-3PO
// encodes it in a universally understood protocol format — something any
// starfighter nav computer can parse. An LDAP URL is exactly that: a
// self-contained address like {@code ldap://server:389/cn=Luke,ou=Rebels?}
// that encodes the server, the entry DN, and optionally the search scope
// and attributes. This action grabs the selected entry's URL and copies it
// to the clipboard so it can be shared, scripted, or bookmarked elsewhere.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Copies the LDAP URL of the currently selected entry, search, or attribute
 * to the system clipboard as plain text.
 * An LDAP URL encodes the connection host, port, base DN, and (for searches)
 * scope and filter into a single shareable string — useful for pasting into
 * scripts, documentation, or other LDAP tools.
 * Think of this as C-3PO translating the entry's address into universal
 * protocol notation that any nav computer understands.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyUrlAction extends BrowserAction
{

    // ── C-3PO Initialises His URL Translation Module ─────────────────────────
    // C-3PO boots up his LDAP-URL encoding subroutine — nothing to configure,
    // he just needs to be ready when someone points at an entry and says "encode that."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code CopyUrlAction} with default state.
     * The URL is resolved from the active selection at the moment {@link #run()}
     * is called, not at construction time.
     */
    public CopyUrlAction()
    {
    }


    // ── C-3PO Encodes the Selected Item as an LDAP URL ───────────────────────
    // C-3PO scans the room: searches first (they have the richest URLs), then
    // entries, attributes, attribute hierarchies, values, search results, and
    // bookmarks — in that priority order — and encodes the first match as an
    // LDAP URL string on the clipboard.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resolves an {@link LdapUrl} from the first selected object (search,
     * entry, attribute, value, search result, or bookmark — in that order)
     * and writes it to the system clipboard as a plain-text string.
     * If nothing with a URL is selected, this is a no-op.
     */
    public void run()
    {
        LdapUrl url = null;
        if ( getSelectedSearches().length > 0 )
        {
            url = getSelectedSearches()[0].getUrl();
        }
        else if ( getSelectedEntries().length > 0 )
        {
            url = getSelectedEntries()[0].getUrl();
        }
        else if ( getSelectedAttributes().length > 0 )
        {
            url = getSelectedAttributes()[0].getEntry().getUrl();
        }
        else if ( getSelectedAttributeHierarchies().length > 0 )
        {
            url = getSelectedAttributeHierarchies()[0].getAttribute().getEntry().getUrl();
        }
        else if ( getSelectedValues().length > 0 )
        {
            url = getSelectedValues()[0].getAttribute().getEntry().getUrl();
        }
        else if ( getSelectedSearchResults().length > 0 )
        {
            url = getSelectedSearchResults()[0].getEntry().getUrl();
        }
        else if ( getSelectedBookmarks().length > 0 )
        {
            url = getSelectedBookmarks()[0].getEntry().getUrl();
        }

        if ( url != null )
        {
            CopyAction.copyToClipboard( new Object[]
                { url.toString() }, new Transfer[]
                { TextTransfer.getInstance() } );
        }
    }


    // ── C-3PO Announces the Protocol Action ──────────────────────────────────
    // "Copy URL" — C-3PO names the action clearly so the crew knows
    // they're encoding the entry as an LDAP URL, not a DN.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name "Copy URL" for this action.
     *
     * @return  the label shown in menus and tooltips; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "CopyUrlAction.CopyURL" ); //$NON-NLS-1$
    }


    // ── C-3PO Displays His URL-Encoding Badge ────────────────────────────────
    // C-3PO selects the insignia that marks this as a URL copy operation,
    // distinct from the DN copy or attribute copy operations.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the "copy URL" icon shown in menus
     * and toolbars.
     *
     * @return  the {@link ImageDescriptor} for the URL copy icon; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_URL );
    }


    // ── C-3PO Checks the Command Registry ────────────────────────────────────
    // C-3PO finds no registered keyboard shortcut for this URL-copy protocol —
    // returning null tells Eclipse not to look for a key binding.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action has no registered Eclipse
     * command ID and therefore no keyboard shortcut binding.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── C-3PO Confirms He Has an Address to Encode ───────────────────────────
    // C-3PO won't transmit without an address — he checks that exactly one
    // search, entry, search result, or bookmark is selected (each of which has
    // a URL), or that at least one attribute or value is selected (which means
    // we can grab the URL from their parent entry).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when the selection contains exactly one item with a
     * URL (search, entry, search result, or bookmark), or at least one attribute
     * or value (whose parent entry's URL we can use).
     *
     * @return  {@code true} if an LDAP URL can be resolved from the current selection
     */
    public boolean isEnabled()
    {
        return getSelectedSearches().length + getSelectedEntries().length + getSelectedSearchResults().length
            + getSelectedBookmarks().length == 1
            || getSelectedAttributes().length + getSelectedValues().length > 0;
    }
}
