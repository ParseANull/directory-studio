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


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.action.Action;


// ── CLASS: ShowDefaultSchemaAction — Luke's Binary Sunset ─────────────────────
// Luke stands on the ridge of the Lars homestead, gazing at Tatooine's twin suns
// as they sink below the horizon — no particular destination, just the full
// picture of what's out there beyond the farm.  This action does the same thing:
// it lets the user step back from a connection-specific schema and see the
// built-in default schema, the baseline view of what LDAP looks like before any
// server customisation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A toggle action that switches the schema browser between the live
 * connection schema and the built-in default schema.
 * It lives in the schema browser toolbar and tells all five schema pages
 * to reload with whichever schema is now active.
 * Think of this class as Luke watching the binary sunset: checking the
 * "Show Default Schema" button is the moment you look past the server-specific
 * details and see the universal LDAP baseline.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowDefaultSchemaAction extends Action
{
    /** The schema browser */
    private SchemaBrowser schemaBrowser;


    // ── Luke Turns Away From The Farm ─────────────────────────────────────────────
    // Luke climbs the ridge and stares toward the horizon, deciding to finally see
    // what lies beyond what he already knows.
    // We wire up the action label, tooltip, and icon here so the toolbar item
    // looks right before the user ever clicks it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the action, wiring it to the schema browser it will control.
     * We set up the label, tooltip, and icon in the constructor so the toolbar
     * item is fully configured before it ever renders.
     *
     * <p>For example — Luke prepares to look at the binary sunset:</p>
     * <pre>
     *   action = new ShowDefaultSchemaAction(schemaBrowser);
     *   // action.getText() == "Show Default Schema"
     *   // action.isChecked() == false  (shows live schema by default)
     * </pre>
     *
     * @param schemaBrowser  the schema browser editor whose pages we will refresh when toggled
     */
    public ShowDefaultSchemaAction( SchemaBrowser schemaBrowser )
    {
        super( Messages.getString( "ShowDefaultSchemaAction.ShowDefaultSchema" ), Action.AS_CHECK_BOX ); //$NON-NLS-1$
        super.setToolTipText( Messages.getString( "ShowDefaultSchemaAction.ShowDefaultSchemaToolTip" ) ); //$NON-NLS-1$
        super.setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor(
            BrowserUIConstants.IMG_DEFAULT_SCHEMA ) );
        super.setEnabled( true );

        this.schemaBrowser = schemaBrowser;
    }


    // ── Twin Suns Rise Or Set ─────────────────────────────────────────────────────
    // Luke watches as one sun dips below the horizon while the other begins to
    // follow — the landscape transforms depending on which light dominates.
    // We forward the new checked state to the schema browser so every page
    // swaps between live and default schema simultaneously.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Fires when the user clicks the toolbar toggle, flipping the schema source.
     * We delegate straight to {@code schemaBrowser.setShowDefaultSchema()} which
     * propagates the change to all five schema pages at once.
     *
     * <p>For example — Luke decides to look at the horizon:</p>
     * <pre>
     *   // user clicks the toggle
     *   run();
     *   // schemaBrowser now shows default LDAP schema
     * </pre>
     */
    @Override
    public void run()
    {
        this.schemaBrowser.setShowDefaultSchema( isChecked() );
    }


    // ── Luke Returns To The Homestead ─────────────────────────────────────────────
    // After the suns set Luke goes back inside, the moment of reflection over.
    // We null out the reference to the schema browser so we don't accidentally
    // keep it alive after the editor has closed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up when the schema browser this action belongs to is closing.
     * We null the reference to avoid holding onto the editor after it's gone,
     * which would be a memory leak.
     *
     * <p>For example — Luke walks back inside at nightfall:</p>
     * <pre>
     *   dispose();
     *   // schemaBrowser == null; action is inert
     * </pre>
     */
    public void dispose()
    {
        this.schemaBrowser = null;
    }

}
