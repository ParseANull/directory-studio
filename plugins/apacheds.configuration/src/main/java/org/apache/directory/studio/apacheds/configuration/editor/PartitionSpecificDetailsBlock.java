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
package org.apache.directory.studio.apacheds.configuration.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;



// ── CLASS: PartitionSpecificDetailsBlock — Vault Tech Spec Contract ───────
// Deep inside the Death Star's vault wing, each vault type — JDBM or Mavibot
// — has its own specialist panel for storage-engine-specific knobs and dials.
// This interface is the standing order that every such specialist panel must
// obey: build your UI, expose your parent page, refresh from the model,
// and commit changes back when the user hits Save.
// ─────────────────────────────────────────────────────────────────────────
/**
 * Contract for a partition-type-specific settings panel inside {@link PartitionDetailsPage}.
 * Concrete implementations — one for JDBM, one for Mavibot — build their own
 * UI block and plug it into the "Partition Specific Settings" section of the
 * details page.
 * Think of this interface as the standing order that every vault specialist
 * panel must follow so the details page can swap them in and out without knowing
 * which storage engine it's talking to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface PartitionSpecificDetailsBlock
{
    // ── Specialist Panel Builds Its Instrument Cluster ───────────────────────
    // The JDBM specialist, or the Mavibot specialist, wheels in their unique
    // rack of dials and readouts and bolts it into the vault inspection frame.
    // Each implementor constructs whatever storage-engine-specific widgets it
    // needs inside the given parent composite, using the toolkit for styling.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds and returns the type-specific UI block inside the given parent composite.
     * The returned composite is later disposed and replaced when the user switches
     * partition types, so keep it self-contained.
     *
     * <p>For example — the JDBM specialist rigs up their panel:</p>
     * <pre>
     *   A composite bearing JDBM-specific controls (optimizer toggle,
     *   cache size spinner, etc.) is created inside the parent and returned.
     *   The caller attaches it to the Section client area.
     * </pre>
     *
     * @param parent   the parent composite provided by the details page section
     * @param toolkit  the form toolkit used to create consistently styled widgets
     * @return         the newly constructed composite containing the type-specific controls
     */
    Composite createBlockContent( Composite parent, FormToolkit toolkit );


    // ── Specialist Reports Back to the Chief Inspector ───────────────────────
    // After the specialist finishes wiring up their panel, the vault chief
    // may need to reach back to the main inspection dossier — to mark it dirty,
    // for instance, when the specialist's controls change.
    // This method hands the specialist a reference back to the owning details page.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link PartitionDetailsPage} that owns this specific settings block.
     * Implementations use this reference to call back to the parent page — for
     * example, to mark the editor dirty when a storage-engine setting changes.
     *
     * @return  the parent details page that hosts this block
     */
    PartitionDetailsPage getDetailsPage();


    // ── Specialist Updates Their Readouts from the Vault Dossier ────────────
    // The vault chief slides a fresh data packet across the desk; the JDBM or
    // Mavibot specialist picks it up and updates every indicator on their panel
    // to match the latest values from the partition bean.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reloads all the type-specific UI controls from the current partition bean.
     * Call this whenever the selected partition changes or after the model is
     * updated externally, so the widgets always reflect the live configuration.
     */
    void refresh();


    // ── Specialist Files the Updated Spec Sheet Back into the Dossier ────────
    // The inspection is over; the specialist reads every dial on their panel
    // and writes the final values back into the official vault record so they
    // persist when the chief hits Save.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Writes the current widget values back into the partition model bean.
     * Called by the parent details page whenever changes need to be persisted —
     * either because the user explicitly saved or because the form page is
     * about to be navigated away from.
     *
     * @param onSave  {@code true} when the user triggered an explicit Save action;
     *                {@code false} when committing for other reasons, such as a
     *                page switch in a multi-page editor
     */
    void commit( boolean onSave );
}
