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
package org.apache.directory.studio.openldap.config.editor.databases;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;


// ── CLASS: DatabaseSpecificDetailsBlock — Palpatine's Imperial Archives ───────
// In the Emperor's archives, each record type demands its own presentation
// format — military, financial, interrogation transcripts. They all live
// in the same vault but display and commit differently.
// This interface defines the contract that every database-specific UI block
// must fulfil, so the editor can treat BDB, MDB, LDIF, etc. uniformly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The contract that every database-specific UI block must implement.
 * Different OpenLDAP backend databases (BDB, MDB, LDIF, relay, null…) each
 * expose a different set of settings to the user, so we have one implementation
 * of this interface per database type — and the editor calls them all the same way.
 * Think of this interface as Palpatine's archive filing standard: every record type
 * must be presentable, refreshable, and committable on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface DatabaseSpecificDetailsBlock
{
    // ── Archive Record Layout ─────────────────────────────────────────────────
    // The Imperial archivist opens a fresh scroll and lays out the fields
    // for a specific record category — ranks, serial numbers, allegiances.
    // Here we're doing the same: creating all the SWT widgets that belong
    // to this particular backend type and wiring them into the parent composite.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT/JFace widgets that belong to this database-specific block.
     * Called once when the details page is first shown for this backend type.
     * The returned composite is plugged into the parent layout by the caller.
     *
     * <p>For example — the Imperial archivist lays out the MDB scrolls:</p>
     * <pre>
     *   archivist.prepareSection( vaultComposite, imperialToolkit );
     *   // MDB fields: environment path, max size, max DBs, ...
     * </pre>
     *
     * @param parent   the parent composite to attach widgets to
     * @param toolkit  the JFace Forms toolkit used to create styled widgets
     * @return         the top-level composite for this block, ready for layout
     */
    Composite createBlockContent( Composite parent, FormToolkit toolkit );


    // ── Locate the Associated Details Ledger ──────────────────────────────────
    // The Imperial archivist looks up which ledger-keeper (DetailsPage) owns
    // this particular block of records, so it knows where to report changes.
    // We need this reference to fire "editor is now dirty" signals upward.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link DatabasesDetailsPage} that owns this block.
     * We need this so the block can notify the page to mark the editor as dirty
     * whenever a field changes.
     *
     * @return the parent details page
     */
    DatabasesDetailsPage getDetailsPage();


    // ── Refresh the Record Display ────────────────────────────────────────────
    // The archivist re-reads the scroll and updates every visible field
    // to match whatever the model currently holds — discarding any
    // unsaved scribbles the user may have made.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the UI widgets from the current in-memory model.
     * Call this after the user selects a different database in the master list,
     * or after an external refresh. It overwrites whatever is in the fields with
     * the current model state — so don't call it if you still need the user's edits.
     */
    void refresh();


    // ── Commit Changes Back to the Archive ────────────────────────────────────
    // The archivist takes whatever the visitor wrote on the blank fields
    // and stamps it permanently into the official record — or at least
    // into the in-memory model that will be saved later.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes the current UI field values back into the in-memory model.
     * This is the write direction: widgets → model.  Called either when the
     * user saves the editor ({@code onSave=true}) or when the page is about
     * to navigate away ({@code onSave=false}).
     *
     * @param onSave  {@code true} if we're doing a full save, {@code false}
     *                if we're just committing before a page switch
     */
    void commit( boolean onSave );
}
