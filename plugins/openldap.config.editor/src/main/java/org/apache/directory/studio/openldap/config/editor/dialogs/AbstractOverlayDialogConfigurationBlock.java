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
package org.apache.directory.studio.openldap.config.editor.dialogs;


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;

import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// Like the engineers directing construction of the second Death Star,
// we lay down the structural skeleton for every overlay configuration
// block. Subclasses snap their specific modules onto this frame,
// and the whole station comes together piece by piece.
/**
 * Abstract base class for overlay dialog configuration blocks, providing
 * the shared wiring — dialog reference, overlay instance, and browser
 * connection — that every concrete block needs to function. We build
 * the scaffolding; subclasses fill in the details.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractOverlayDialogConfigurationBlock<O extends OlcOverlayConfig> implements
    OverlayDialogConfigurationBlock<O>
{
    /** The dialog */
    protected OverlayDialog dialog;

    /** The overlay */
    protected O overlay;

    /** The connection */
    protected IBrowserConnection browserConnection;


    // Like the construction crew bolting the first panel of the Death Star
    // into place, we wire this block up to its parent dialog. The connection
    // is minimal — just the dialog — but it's the anchor everything else
    // hangs from.
    /**
     * Creates a new overlay configuration block tied to the given dialog.
     * We store the dialog reference so subclasses can reach back up to
     * the parent when they need it.
     *
     * @param dialog the parent overlay dialog this block belongs to
     */
    public AbstractOverlayDialogConfigurationBlock( OverlayDialog dialog )
    {
        this.dialog = dialog;
    }


    // Like adding the hyperspace motivator and the main reactor in one go,
    // we set up this block with both the dialog reference and the browser
    // connection so the block can reach the LDAP server when it needs to.
    // Both parts are essential — missing either one and we can't function.
    /**
     * Creates a new overlay configuration block tied to the given dialog
     * and browser connection. The connection lets us reach out to the
     * LDAP server for live data lookups during configuration.
     *
     * @param dialog the parent overlay dialog this block belongs to
     * @param browserConnection the live LDAP browser connection we can query
     */
    public AbstractOverlayDialogConfigurationBlock( OverlayDialog dialog, IBrowserConnection browserConnection )
    {
        this.dialog = dialog;
        this.browserConnection = browserConnection;
    }


    // Like an engineer checking which station module they're working inside,
    // we hand back a reference to the parent dialog so callers know
    // which control room they're operating from.
    /**
     * Returns the overlay dialog this configuration block is attached to.
     *
     * @return the parent {@link OverlayDialog} instance
     */
    public OverlayDialog getDialog()
    {
        return dialog;
    }


    // Like pulling up the blueprints for the specific module being installed,
    // we return the overlay config object that this block is currently
    // editing. This is the data model behind the UI controls.
    /**
     * Returns the overlay configuration object currently being edited
     * by this block.
     *
     * @return the overlay config instance of type {@code O}
     */
    public O getOverlay()
    {
        return overlay;
    }


    // Like the construction superintendent reassigning a panel to a new
    // station module, we swap out the dialog reference so this block
    // points at a different parent dialog going forward.
    /**
     * Replaces the parent dialog reference with a new one. Use this
     * when the block needs to be rehoused in a different dialog.
     *
     * @param dialog the new parent overlay dialog to attach to
     */
    public void setDialog( OverlayDialog dialog )
    {
        this.dialog = dialog;
    }


    // Like loading a fresh set of technical schematics into the construction
    // system, we swap the overlay config object so the block is now
    // editing a different set of data. Everything else stays the same.
    /**
     * Replaces the overlay config object that this block is editing.
     * Call this to point the block at a different overlay configuration.
     *
     * @param overlay the new overlay config instance to edit
     */
    public void setOverlay( O overlay )
    {
        this.overlay = overlay;
    }
}
