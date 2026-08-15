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
package org.apache.directory.studio.openldap.config.editor.pages;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.editor.overlays.OverlaysMasterDetailsBlock;


// ── CLASS: OverlaysPage — The Overlays Station on the Star Destroyer Bridge ───
// On the bridge of a Star Destroyer, the overlays station monitors all the
// pluggable subsystem modules that extend the ship's core capabilities: the
// access-log recorder, the audit trail, the sync provider that keeps outposts
// up to date.  OverlaysPage is that station — one tab in the multi-tab editor
// that gives the administrator a master/details view of every OpenLDAP overlay
// configured on the server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "Overlays" tab page of the OpenLDAP server configuration editor.
 * It hosts the {@link OverlaysMasterDetailsBlock} which provides a master/
 * details split: the left pane lists all configured overlays, and the right
 * pane shows the type-specific settings for whichever overlay is selected.
 * Think of it as the overlays workstation on a Star Destroyer's bridge —
 * one focused station in a multi-station command deck.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OverlaysPage extends OpenLDAPServerConfigurationEditorPage
{
    /** The Page ID*/
    public static final String ID = OverlaysPage.class.getName(); //$NON-NLS-1$

    /** The Page Title */
    private static final String TITLE = "Overlays";


    // UI Controls

    // ── Constructor — The Overlays Station Comes Online ───────────────────────
    // A crew member takes the overlays station, logs in with their credentials
    // (the editor reference), and prepares to manage all pluggable subsystems.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OverlaysPage and registers it with the given editor.
     * The editor reference lets us reach the shared OpenLdapConfiguration model
     * through the base class.
     *
     * @param editor  the OpenLdapServerConfigurationEditor that owns this page
     */
    public OverlaysPage( OpenLdapServerConfigurationEditor editor )
    {
        super( editor, ID, TITLE );
    }


    // ── createFormContent — The Station Builds Its Console ────────────────────
    // The overlays workstation powers up and assembles its split-panel display:
    // the list of overlays on the left and the configuration detail on the right.
    // We hand the work to OverlaysMasterDetailsBlock.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds this page's form content by creating and activating the
     * overlays master/details block.
     * We pass the block our managed form so it can register section parts and
     * wire up selection events.
     *
     * @param parent   the SWT composite provided by the base class
     * @param toolkit  the Eclipse Forms toolkit for creating styled widgets
     */
    protected void createFormContent( Composite parent, FormToolkit toolkit )
    {
        OverlaysMasterDetailsBlock masterDetailsBlock = new OverlaysMasterDetailsBlock( this );
        masterDetailsBlock.createContent( getManagedForm() );
    }


    // ── refreshUI — The Station Refreshes Its Status Displays ─────────────────
    // New data arrives from the server; the overlays station operator checks
    // whether there's anything to refresh.  Currently nothing to do here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes this page's UI from the current configuration model.
     * Currently a no-op because the overlays master/details block manages its
     * own refresh via selection events.
     */
    public void refreshUI()
    {
        // Nothing to do
    }
}
