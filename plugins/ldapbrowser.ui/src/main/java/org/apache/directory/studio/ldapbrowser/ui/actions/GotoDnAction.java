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


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.ClipboardUtils;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.common.dialogs.DnDialog;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.dnd.TextTransfer;


// ── CLASS: GotoDnAction — R2-D2 QUERIES FOR A SPECIFIC TERMINAL ADDRESS ─────
// R2-D2 rolls up to a Death Star computer terminal and queries for a specific
// system address — he knows what he's looking for and he goes straight there.
// This action lets the user type (or paste from clipboard) an LDAP Distinguished
// Name and jump directly to that entry in the browser, bypassing the need to
// expand the tree manually step by step.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Prompts the user for an LDAP Distinguished Name (DN) and navigates the
 * browser directly to that entry.
 * The dialog pre-populates with any DN found on the clipboard (as a convenience
 * — paste the DN you just copied, hit OK, and you're there). Only enabled when
 * the current input is an {@link IBrowserConnection}, because we need a live
 * connection to resolve the DN.
 * Think of R2-D2 entering the exact terminal ID he needs — no wandering,
 * no guessing, straight to the target.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GotoDnAction extends LocateInDitAction
{
    // ── R2 Initialises His Goto-DN Subroutine ────────────────────────────────
    // R2 boots up his direct-navigation module — nothing special to configure,
    // he's ready as soon as he's constructed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code GotoDnAction} with default state.
     * The connection and DN are resolved from the active context when the action fires.
     */
    public GotoDnAction()
    {
    }


    // ── R2 Announces His Navigation Function ─────────────────────────────────
    // R2 beeps the name of the function he's about to run — "Goto DN" —
    // so the crew knows which terminal command this is.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display name "Go to DN" for this action.
     *
     * @return  the menu label; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "GotoDnAction.GotoDN" ); //$NON-NLS-1$
    }


    // ── R2 Selects the Locate-DN Indicator Light ─────────────────────────────
    // R2 illuminates the correct indicator on his panel — the "locate DN in DIT"
    // icon — so users can identify this command in a toolbar or menu.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the "locate DN in DIT" icon.
     *
     * @return  the {@link ImageDescriptor} for the icon; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_LOCATE_DN_IN_DIT );
    }


    // ── R2 Checks He's Connected to a Server ─────────────────────────────────
    // R2 won't query a terminal that isn't live — he checks that the current
    // input is an active browser connection before enabling the action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} because this action has no registered Eclipse
     * command ID.
     *
     * @return  {@code null} always
     */
    public String getCommandId()
    {
        return null;
    }


    // ── R2 Confirms the Terminal Is Live ─────────────────────────────────────
    // R2 won't try to navigate without a connection — he first confirms
    // that the current input is an {@link IBrowserConnection}.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when the current workbench input is an
     * {@link IBrowserConnection}, meaning we have a live connection to resolve the DN against.
     *
     * @return  {@code true} if navigating to a typed DN is possible
     */
    public boolean isEnabled()
    {
        return getInput() instanceof IBrowserConnection;
    }


    // ── R2 Reads the DN From Clipboard and Dialog ────────────────────────────
    // R2 plugs into the clipboard first — if there's a DN already copied, he
    // pre-fills the dialog with it. Then he displays the DN dialog and waits
    // for the user to confirm. Once confirmed, he packages the connection and
    // DN together and hands them to the parent {@link LocateInDitAction} to
    // do the actual navigation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads a DN from the clipboard (if one is present) to pre-populate the
     * dialog, then shows a {@link DnDialog} asking the user to confirm or type
     * a DN. Returns a {@link ConnectionAndDn} bundle if the user clicked OK
     * and provided a valid DN; returns {@code null} if the user cancelled.
     *
     * @return  the connection and DN to navigate to, or {@code null} if cancelled
     */
    protected ConnectionAndDn getConnectionAndDn()
    {
        if ( getInput() instanceof IBrowserConnection )
        {
            IBrowserConnection conn = ( IBrowserConnection ) getInput();

            Dn dn = Utils.getLdapDn( ClipboardUtils.getFromClipboard( TextTransfer.getInstance(), String.class ) );

            DnDialog dialog = new DnDialog(
                getShell(),
                Messages.getString( "GotoDnAction.GotoDNAction" ), Messages.getString( "GotoDnAction.EnterDNAction" ), conn, dn ); //$NON-NLS-1$ //$NON-NLS-2$
            if ( dialog.open() == TextDialog.OK && dialog.getDn() != null )
            {
                dn = dialog.getDn();
                return new ConnectionAndDn( conn, dn );
            }
        }

        return null;
    }

}
