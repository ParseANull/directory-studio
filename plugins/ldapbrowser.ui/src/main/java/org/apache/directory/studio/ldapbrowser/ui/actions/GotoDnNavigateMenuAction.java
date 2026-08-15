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
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.common.dialogs.DnDialog;
import org.apache.directory.studio.ldapbrowser.common.dialogs.TextDialog;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.views.connection.ConnectionView;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.PlatformUI;


// ── CLASS: GotoDnNavigateMenuAction — R2-D2 NAVIGATES FROM THE CONNECTION VIEW
// R2-D2 is already parked in the docking bay — he's plugged into a specific
// ship (a connection in the Connections view). When the pilot says "take me to
// this address", R2 checks which ship he's on, checks the clipboard for a
// pre-typed DN, and shows the dialog to confirm the destination. This variant
// of Goto-DN reads the connection from the Connections view selection rather
// than from the browser's current input, making it suitable for the Navigate menu.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Prompts the user for a DN and navigates the browser to that entry, using the
 * connection currently selected in the Connections view.
 * This is the Navigate-menu variant of {@link GotoDnAction} — it resolves the
 * connection from the Connections view selection rather than the browser input,
 * so it works even when the browser is showing a different connection.
 * Think of R2-D2 in the docking bay, already wired into the ship the user
 * highlighted, ready to navigate anywhere on that ship's network.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GotoDnNavigateMenuAction extends LocateInDitAction
{
    // ── R2 Announces His Navigation Role ─────────────────────────────────────
    // R2 beeps the same label as the standard GotoDnAction — "Go to DN" —
    // since from the user's perspective this is the same navigation action.
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


    // ── R2 Selects the Locate-DN Indicator ───────────────────────────────────
    // R2 lights up the "locate DN in DIT" indicator — same icon as
    // {@link GotoDnAction} since they perform the same conceptual action.
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


    // ── R2 Checks He's Plugged Into a Ship ───────────────────────────────────
    // R2 won't navigate without knowing which ship he's on — he checks that
    // exactly one connection is selected in the Connections view.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when exactly one {@link Connection} is selected in
     * the Connections view, giving us a known server to navigate within.
     *
     * @return  {@code true} if the Connections view has exactly one connection selected
     */
    public boolean isEnabled()
    {
        return ( getSelectedConnection() != null );
    }


    // ── R2 Reads the DN and Bundles Ship Plus Address ────────────────────────
    // R2 checks which ship he's on (the selected connection), pre-fills the
    // dialog with any DN from the clipboard, and waits for the user's OK.
    // Once confirmed, he hands the connection+DN bundle to the parent class
    // which handles the actual locate-and-scroll operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resolves the active connection from the Connections view, pre-populates
     * a {@link DnDialog} with any DN found on the clipboard, and returns the
     * confirmed connection+DN pair.
     * Returns {@code null} if no connection is selected, or if the user cancels.
     *
     * @return  the connection and target DN, or {@code null} if not applicable or cancelled
     */
    protected ConnectionAndDn getConnectionAndDn()
    {
        Connection selectedConnection = getSelectedConnection();

        if ( selectedConnection != null )
        {
            // Getting the browser connection associated with the connection
            IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnection( selectedConnection );

            // Getting the DN from the clipboard (if any)
            Dn dn = Utils.getLdapDn( ClipboardUtils.getFromClipboard( TextTransfer.getInstance(), String.class ) );

            // Displaying the DN dialog
            DnDialog dialog = new DnDialog(
                getShell(),
                Messages.getString( "GotoDnAction.GotoDNAction" ), Messages.getString( "GotoDnAction.EnterDNAction" ), connection, dn ); //$NON-NLS-1$ //$NON-NLS-2$
            if ( dialog.open() == TextDialog.OK && dialog.getDn() != null )
            {
                dn = dialog.getDn();
                return new ConnectionAndDn( connection, dn );
            }
        }

        return null;
    }


    // ── R2 Queries Which Ship He's Currently Docked To ───────────────────────
    // R2 looks up at the docking-bay monitor (the Connections view) to see
    // which ship is highlighted — that's the connection he'll navigate on.
    // We find the Connections view, grab its selection, and return the selected
    // {@link Connection} if exactly one is chosen.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Connection} currently selected in the Connections view,
     * or {@code null} if the view is not open, nothing is selected, multiple items
     * are selected, or the selected item is not a {@link Connection}.
     *
     * @return  the single selected connection, or {@code null}
     */
    private Connection getSelectedConnection()
    {
        // Getting the connections view
        ConnectionView connectionView = ( ConnectionView ) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getActivePage().findView( ConnectionView.getId() );

        if ( connectionView != null )
        {
            // Getting the selection of the connections view
            StructuredSelection selection = ( StructuredSelection ) connectionView.getMainWidget().getViewer()
                .getSelection();

            // Checking if only one object is selected
            if ( selection.size() == 1 )
            {
                Object selectedObject = selection.getFirstElement();

                // Checking if the selected object is a connection
                if ( selectedObject instanceof Connection )
                {
                    return ( Connection ) selectedObject;
                }
            }
        }

        return null;
    }

}
