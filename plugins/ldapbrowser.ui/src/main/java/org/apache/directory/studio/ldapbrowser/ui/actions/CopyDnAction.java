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
import org.apache.directory.studio.ldapbrowser.common.actions.CopyAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;


// ── CLASS: CopyDnAction — C-3PO READS OUT THE FULL ADDRESS ──────────────────
// When the Rebel pilots need the coordinates to the Death Star, C-3PO doesn't
// hand them a binary blob — he translates the raw navigation data into plain
// galactic-standard text that everyone can use. A DN (Distinguished Name) is
// the LDAP address for an entry (e.g., "cn=Luke,ou=Rebels,dc=galaxy,dc=com"),
// and this action translates it into a plain string on the clipboard.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Copies the Distinguished Name (DN) of the currently selected LDAP entry,
 * attribute, or value to the system clipboard as plain text.
 * A DN is the unique path to an entry in the LDAP tree, analogous to a file
 * system path — copying it lets users paste it into search dialogs, config
 * files, or scripts without retyping.
 * Think of this as C-3PO reading the full protocol address aloud so the crew
 * can plug it into their navigation computer.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CopyDnAction extends BrowserAction
{

    // ── C-3PO Initialises His DN Protocol Module ──────────────────────────────
    // C-3PO powers up his address-translation subroutine — nothing to configure,
    // he just needs to be ready for when someone points at an entry and says "read that."
    // We construct with no arguments; the DN is discovered at runtime from the selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code CopyDnAction} with default state.
     * The DN to copy is resolved from the active selection at the moment
     * {@link #run()} is called, not at construction time.
     */
    public CopyDnAction()
    {
    }


    // ── C-3PO Reads the Address Into the Commlink ─────────────────────────────
    // C-3PO scans the selected object — is it an entry? An attribute? A value?
    // He walks the list in priority order, extracts the right DN, and announces
    // it clearly over the comm channel (the clipboard).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resolves the DN from the first selected object (entry, attribute hierarchy,
     * attribute, value, search result, or bookmark — checked in that order) and
     * writes it to the system clipboard as plain text.
     * If nothing with a DN is selected, this is a no-op.
     */
    public void run()
    {
        String dn = null;
        if ( getSelectedEntries().length > 0 )
        {
            dn = getSelectedEntries()[0].getDn().getName();
        }
        else if ( getSelectedAttributes().length > 0 )
        {
            dn = getSelectedAttributes()[0].getEntry().getDn().getName();
        }
        else if ( getSelectedAttributeHierarchies().length > 0 )
        {
            dn = getSelectedAttributeHierarchies()[0].getAttribute().getEntry().getDn().getName();
        }
        else if ( getSelectedValues().length > 0 )
        {
            dn = getSelectedValues()[0].getAttribute().getEntry().getDn().getName();
        }
        else if ( getSelectedSearchResults().length > 0 )
        {
            dn = getSelectedSearchResults()[0].getDn().getName();
        }
        else if ( getSelectedBookmarks().length > 0 )
        {
            dn = getSelectedBookmarks()[0].getDn().getName();
        }

        if ( dn != null )
        {
            CopyAction.copyToClipboard( new Object[]
                { dn }, new Transfer[]
                { TextTransfer.getInstance() } );
        }
    }


    // ── C-3PO Announces the Name of the Protocol ─────────────────────────────
    // "I believe the correct term is 'Copy DN', sir" — C-3PO labels the action
    // clearly so everyone knows what they're about to trigger.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name "Copy DN" for this action.
     *
     * @return  the label shown in menus and tooltips
     */
    public String getText()
    {
        return Messages.getString( "CopyDnAction.CopyDN" ); //$NON-NLS-1$
    }


    // ── C-3PO Displays His Address-Copy Badge ─────────────────────────────────
    // C-3PO points to the right insignia on his chest — the icon that visually
    // identifies this as the "copy DN" operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the "copy DN" icon shown in menus
     * and toolbars.
     *
     * @return  the {@link ImageDescriptor} for the icon; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_COPY_DN );
    }


    // ── C-3PO Checks His Command Registry ────────────────────────────────────
    // C-3PO searches his database for a registered keyboard shortcut binding —
    // finds none, and reports accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action has no registered Eclipse
     * command ID and therefore no keyboard shortcut.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── C-3PO Confirms There's an Address to Read ────────────────────────────
    // C-3PO won't translate what isn't there — he first confirms that exactly
    // one entry, search result, or bookmark is selected (or any attribute/value),
    // giving us exactly one unambiguous DN to copy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when there is exactly one entry, search result, or
     * bookmark selected, or when at least one attribute or value is selected
     * (each of which belongs to an entry with a DN).
     * Eclipse uses this to decide whether to grey out the menu item.
     *
     * @return  {@code true} if a DN is unambiguously resolvable from the current selection
     */
    public boolean isEnabled()
    {
        return getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length == 1
            || getSelectedAttributes().length + getSelectedValues().length > 0;
    }
}
