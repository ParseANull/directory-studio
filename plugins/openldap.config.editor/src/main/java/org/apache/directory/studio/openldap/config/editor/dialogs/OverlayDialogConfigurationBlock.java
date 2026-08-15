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


import org.eclipse.swt.widgets.Composite;

import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// Like the engineering blueprint for a Death Star module — every
// panel that snaps onto the station has to implement these exact
// connection points or the whole thing won't fit together. We
// define the contract that every overlay configuration block must fulfill.
/**
 * Interface that every overlay dialog configuration block must implement.
 * We define the standard lifecycle contract: create content, refresh the UI,
 * save data back to the overlay, and provide accessors for the dialog and
 * overlay references. Concrete blocks snap into the overlay dialog via this contract.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface OverlayDialogConfigurationBlock<O extends OlcOverlayConfig>
{
    // Like the crew bolting the first structural panel of a Death Star
    // module into place, this is where the block builds its UI widgets
    // and attaches them to the given parent composite. Once this runs,
    // the block is visible and ready for operator interaction.
    /**
     * Builds and attaches this block's UI content to the given parent composite.
     * Implementations create their form controls here.
     *
     * @param parent the parent composite to attach this block's UI to
     */
    void createBlockContent( Composite parent );


    // Like checking which control room a module is docked to, we return
    // the overlay dialog that this configuration block is currently
    // attached to so callers know who owns this block.
    /**
     * Returns the overlay dialog this configuration block is attached to.
     *
     * @return the parent {@link OverlayDialog} instance
     */
    OverlayDialog getDialog();


    // Like pulling up the technical schematic for the specific module
    // being configured, we return the overlay config object that
    // this block is currently editing so callers can inspect it.
    /**
     * Returns the overlay configuration object currently being edited
     * by this block.
     *
     * @return the overlay config instance of type {@code O}
     */
    O getOverlay();


    // Like syncing a module's status displays to the latest sensor data
    // coming in from the station's central computer, we push the current
    // overlay config values back into the UI so the operator sees
    // the most up-to-date picture.
    /**
     * Refreshes this block's UI controls from the current overlay
     * configuration data. Call this after the underlying data changes
     * to keep the display in sync.
     */
    void refresh();


    // Like the engineering crew locking in the final configuration
    // parameters before the module goes operational, we serialize
    // whatever the operator typed or selected back into the overlay
    // config object so the changes aren't lost.
    /**
     * Writes the current UI control values back into the overlay
     * configuration object so they can be persisted.
     */
    void save();


    // Like reassigning a module to a different control room after
    // a station reorganization, we update the dialog reference so
    // this block knows which parent dialog it now belongs to.
    /**
     * Replaces the parent dialog reference for this configuration block.
     *
     * @param dialog the new parent overlay dialog to attach to
     */
    void setDialog( OverlayDialog dialog );


    // Like swapping the technical blueprint a module is working from,
    // we update the overlay config reference so this block is now
    // editing a different overlay configuration instance.
    /**
     * Replaces the overlay config object that this block is editing.
     *
     * @param overlay the new overlay config instance to edit
     */
    void setOverlay( O overlay );
}
