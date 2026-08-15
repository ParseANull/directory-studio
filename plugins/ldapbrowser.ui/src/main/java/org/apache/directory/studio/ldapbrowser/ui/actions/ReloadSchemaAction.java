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


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReloadSchemaRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: ReloadSchemaAction — R2-D2 PLUGS IN FOR A FRESH QUERY ─────────────
// R2-D2 already pulled the Death Star schematics once, but they might be stale —
// maybe the Empire updated the detention block layout.  So he rolls back to the
// terminal, unplugs the old session, and runs a completely fresh query, overwriting
// what he had before with the latest data.  ReloadSchemaAction does exactly this for
// the LDAP schema: it discards the cached schema for the selected connection and
// fetches a fresh copy from the server, so the browser reflects any schema changes
// the admin made since the last load.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Forces a fresh reload of the LDAP schema from the server for the currently
 * selected connection, discarding any cached copy.
 * Schema changes on the server (new object classes, new attribute types, etc.)
 * are not automatically picked up — this action is how the user explicitly
 * refreshes the local schema cache.
 * Think of this class as R2-D2 re-querying the terminal: same interface, fresh data.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReloadSchemaAction extends BrowserAction
{
    // ── R2 Extends His Interface Probe ──────────────────────────────────────────
    // Before R2 can query a terminal he extends his probe — basic readiness.
    // Our constructor calls super() to wire the BrowserAction infrastructure so
    // we have access to the selection service in run() and isEnabled().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ReloadSchemaAction wired into the BrowserAction framework.
     */
    public ReloadSchemaAction()
    {
        super();
    }


    // ── R2 Runs The Fresh Query ──────────────────────────────────────────────────
    // R2 clears his cache and sends a fresh read request to the terminal.  run()
    // resolves the active connection, then wraps a ReloadSchemaRunnable in a
    // StudioBrowserJob so the network call happens in a background thread and the
    // UI stays responsive.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Triggers a background job to reload the schema for the selected connection.
     * We first check that a valid, connected connection is selected; if so we kick
     * off a {@code ReloadSchemaRunnable} in a {@code StudioBrowserJob}.
     * Does nothing if no valid connection is in the selection (isEnabled() guards
     * against most such cases, but we check again for safety).
     */
    public void run()
    {
        IBrowserConnection connection = getConnectionToRefresh();
        if ( connection != null )
        {
            new StudioBrowserJob( new ReloadSchemaRunnable( connection ) ).execute();
        }
    }


    // ── Terminal Display Shows The Operation Name ────────────────────────────────
    // The terminal's display showed what R2 was running.  getText() returns the
    // localised label Eclipse puts in menus and toolbar tooltips.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display label for this action.
     *
     * @return the menu-item text, e.g. "Reload Schema"
     */
    public String getText()
    {
        return Messages.getString( "ReloadSchemaAction.ReloadSchema" ); //$NON-NLS-1$
    }


    // ── R2's Refresh Light ───────────────────────────────────────────────────────
    // R2 has a little indicator light that pulses when he's running a refresh — our
    // refresh icon plays the same role in the toolbar.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's toolbar/menu icon.
     * We use the standard refresh icon from the browser plugin's image registry.
     *
     * @return the image descriptor for the refresh icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_REFRESH );
    }


    // ── No Broadcast Code ───────────────────────────────────────────────────────
    // R2's terminal queries were local, ad-hoc operations — no broadcast needed.
    // This action has no registered Eclipse command ID.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding.
     * No global command is registered for schema reload.
     *
     * @return always null
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Terminal Must Be Active To Query ────────────────────────────────────────
    // R2 can only query a terminal that's powered on.  isEnabled() checks that
    // exactly one connection is selected and that it's currently live (connected).
    // If it's offline, there's no schema to fetch.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether schema reload is available for the current selection.
     * We delegate to {@link #getConnectionToRefresh()} — if it returns non-null,
     * there's exactly one selected connection and it's live.
     *
     * @return true if a connected connection is selected
     */
    public boolean isEnabled()
    {
        return getConnectionToRefresh() != null;
    }


    // ── R2 Picks The Right Terminal ─────────────────────────────────────────────
    // R2 doesn't plug into a powered-down terminal — he checks that the station is
    // active.  getConnectionToRefresh() finds the one selected connection and
    // verifies it's connected before returning it; returns null otherwise.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resolves the single selected browser connection, but only if it's connected.
     * We require exactly one connection in the selection and verify that its
     * underlying connection wrapper reports it as connected.  If either condition
     * fails we return null and the caller should abort.
     *
     * @return the live {@link IBrowserConnection} for the selected connection,
     *         or null if the selection is empty, has multiple connections, or is offline
     */
    private IBrowserConnection getConnectionToRefresh()
    {
        Connection[] connections = getSelectedConnections();
        if ( connections.length != 1 )
        {
            return null;
        }
        Connection connection = connections[0];
        if ( !connection.getConnectionWrapper().isConnected() )
        {
            return null;
        }
        IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
            .getBrowserConnection( connection );
        return browserConnection;
    }

}
