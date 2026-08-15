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

package org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser;


import org.apache.directory.studio.ldapbrowser.core.jobs.ReloadSchemaRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.action.Action;


// ── CLASS: ReloadSchemaAction — Yoda Lifting The X-Wing ──────────────────────
// On Dagobah, Luke's X-wing is stuck in the swamp — unusable, heavy, seemingly
// gone.  Yoda reaches out with the Force and lifts it clear, making it fresh and
// ready to fly again.  This action does exactly that for the LDAP schema: it
// reaches into the server, pulls down a brand-new copy of the schema, and makes
// the schema browser display the current reality rather than a stale snapshot.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toolbar action that re-fetches the schema from the LDAP server and refreshes
 * all schema browser pages to reflect any changes since we last loaded.
 * It lives in the schema page toolbar and is only enabled when a real connection
 * is selected and "Show Default Schema" is not active.
 * Think of this class as Yoda lifting the X-wing: we reach out to the server,
 * pull the schema clear of whatever stale state it was in, and set it down
 * fresh for the user to inspect.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReloadSchemaAction extends Action
{

    /** The schema page */
    private SchemaPage schemaPage;


    // ── Yoda Prepares To Lift The X-Wing ─────────────────────────────────────────
    // Yoda stands at the water's edge, eyes closed, gathering himself before
    // reaching out to the submerged X-wing.
    // We configure the action's label, tooltip, and icon here so it is ready to
    // display the moment the toolbar renders.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action and wires it to the schema page that owns the toolbar.
     * We set up the label, tooltip, and refresh icon in the constructor; the
     * enabled state is managed separately via {@code updateEnabledState()}.
     *
     * <p>For example — Yoda walks to the swamp's edge:</p>
     * <pre>
     *   action = new ReloadSchemaAction(schemaPage);
     *   // action.getText() == "Reload Schema"
     *   // action.isEnabled() == true (updated later based on connection)
     * </pre>
     *
     * @param schemaPage  the schema page whose toolbar this action belongs to
     */
    public ReloadSchemaAction( SchemaPage schemaPage )
    {
        super( Messages.getString( "ReloadSchemaAction.ReloadSchema" ) ); //$NON-NLS-1$
        super.setToolTipText( Messages.getString( "ReloadSchemaAction.ReloadSchemaToolTip" ) ); //$NON-NLS-1$
        super.setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_REFRESH ) );
        super.setEnabled( true );

        this.schemaPage = schemaPage;
    }


    // ── Yoda Lifts The X-Wing From The Swamp ─────────────────────────────────────
    // Yoda reaches out, the X-wing rises from the murky water, dripping and
    // renewed, ready to fly.
    // We fire a background job to re-fetch the schema from the server, then
    // call refresh() so the browser pages update immediately.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Triggers a schema reload against the currently selected LDAP connection.
     * We submit a {@link ReloadSchemaRunnable} as a background {@link StudioBrowserJob}
     * so the UI stays responsive while the server responds, then refresh the
     * browser to show the updated schema.
     * If no connection is selected we do nothing — there is nothing to reload from.
     *
     * <p>For example — Yoda raises the X-wing:</p>
     * <pre>
     *   run();
     *   // background job contacts server, fetches schema
     *   // schemaPage.getSchemaBrowser().refresh() called when done
     * </pre>
     */
    @Override
    public void run()
    {
        final IBrowserConnection browserConnection = schemaPage.getConnection();
        if ( browserConnection != null )
        {
            new StudioBrowserJob( new ReloadSchemaRunnable( browserConnection ) ).execute();
            schemaPage.getSchemaBrowser().refresh();
        }
    }


    // ── Luke's X-Wing Is Returned To The Forest ───────────────────────────────────
    // After the demonstration Yoda steps away, the X-wing rests safely on the bank,
    // no longer his concern.
    // We null the schema page reference here so this action can be garbage-collected
    // cleanly when the schema browser closes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases the reference to the schema page when the editor is closing.
     * Nulling out the reference prevents us from accidentally holding the page
     * alive after it has been disposed.
     *
     * <p>For example — Yoda steps back from the swamp:</p>
     * <pre>
     *   dispose();
     *   // schemaPage == null; action is inert
     * </pre>
     */
    public void dispose()
    {
        schemaPage = null;
    }


    // ── Yoda Checks If The Swamp Is Deep Enough ───────────────────────────────────
    // Before trying to lift the X-wing Yoda senses whether there is actually
    // something in the water — if it is already gone, there is nothing to lift.
    // We enable the action only when a real connection is selected and the user
    // is not looking at the hardcoded default schema.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes whether this action should be clickable right now.
     * The action is only meaningful when there is a live connection to reload
     * from and the user is not viewing the static default schema.
     *
     * <p>For example — Yoda senses the X-wing is still in the swamp:</p>
     * <pre>
     *   updateEnabledState();
     *   // setEnabled(true)  if connection != null and !showDefaultSchema
     *   // setEnabled(false) otherwise
     * </pre>
     */
    public void updateEnabledState()
    {
        setEnabled( schemaPage.getConnection() != null && !schemaPage.isShowDefaultSchema() );
    }

}
