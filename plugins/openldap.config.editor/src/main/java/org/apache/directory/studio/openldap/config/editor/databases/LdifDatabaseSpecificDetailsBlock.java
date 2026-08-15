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


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.openldap.common.ui.widgets.DirectoryBrowserWidget;
import org.apache.directory.studio.openldap.config.model.database.OlcLdifConfig;


// ── CLASS: LdifDatabaseSpecificDetailsBlock — Palpatine's Plain-Text Archives ──
// Even the Empire needed a simple filing system for low-security records —
// plain text scrolls stored in a specific vault room, one file per entry.
// OpenLDAP's LDIF backend is exactly that: each LDAP entry is a plain LDIF file
// on disk. This block lets us configure where that vault room (directory) lives.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Database-specific UI block for the OpenLDAP LDIF backend ({@code olcLdifConfig}).
 * The LDIF backend stores LDAP entries as individual LDIF files on disk — great for
 * tiny, rarely-written datasets, or for the cn=config tree itself.
 * The only thing we need to configure here is the filesystem directory where those
 * files live. Think of it as the Imperial plain-text archives: one scroll per entry,
 * all tucked into a single known vault directory.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdifDatabaseSpecificDetailsBlock extends AbstractDatabaseSpecificDetailsBlock<OlcLdifConfig>
{
    /** The filesystem directory picker widget — points to the LDIF file vault. */
    private DirectoryBrowserWidget directoryBrowserWidget;


    // ── Designate the Vault Room ──────────────────────────────────────────────
    // The Imperial archivist receives the assignment: manage the plain-text
    // vault for this particular LDIF database. No live connection is needed
    // because LDIF is purely file-based.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the LDIF-specific block, binding it to the given details page and database model.
     * No browser connection is needed here — the LDIF backend is purely filesystem-based,
     * so there's no live LDAP connection to worry about.
     *
     * @param detailsPage  the parent details page that will receive dirty signals
     * @param database     the {@link OlcLdifConfig} model object we're editing
     */
    public LdifDatabaseSpecificDetailsBlock( DatabasesDetailsPage detailsPage, OlcLdifConfig database )
    {
        super( detailsPage, database );
    }


    // ── Build the Vault-Location Panel ────────────────────────────────────────
    // The archivist sets up the one field that matters for the plain-text vault:
    // where on disk the LDIF files are stored. Just a label and a directory
    // browser — nothing fancy for a simple backend.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the block's UI — a single directory-browser row for {@code olcDbDirectory}.
     * The LDIF backend only has one meaningful setting (where the files live),
     * so the UI is intentionally minimal.
     *
     * @param parent   the parent composite to attach our widgets to
     * @param toolkit  the JFace Forms toolkit for creating styled widgets
     * @return         the top-level composite for this block
     */
    public Composite createBlockContent( Composite parent, FormToolkit toolkit )
    {
        // Composite
        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Directory Widget
        toolkit.createLabel( composite, "Directory:" );
        directoryBrowserWidget = new DirectoryBrowserWidget( "" );
        directoryBrowserWidget.createWidget( composite, toolkit );

        return composite;
    }


    // ── Re-read the Current Vault Location ───────────────────────────────────
    // The archivist checks the current record to see where the vault is,
    // then updates the directory picker to show that location.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the directory picker from the current {@link OlcLdifConfig} model.
     * If the model has a directory path set, we display it; otherwise the picker
     * shows an empty string.
     */
    public void refresh()
    {
        removeListeners();

        if ( database != null )
        {
            // Directory Widget
            String directory = database.getOlcDbDirectory();

            if ( directory != null )
            {
                directoryBrowserWidget.setDirectoryPath( directory );
            }
            else
            {
                directoryBrowserWidget.setDirectoryPath( "" );
            }
        }

        addListeners();
    }


    // ── Start Watching for Changes ────────────────────────────────────────────
    // The archivist posts a guard on the directory picker — any change
    // will trigger the "editor is now dirty" signal upward.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the dirty listener to the directory widget.
     * From this point on, any user change to the directory path marks the editor dirty.
     */
    private void addListeners()
    {
        directoryBrowserWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
    }


    // ── Stop Watching for Changes ─────────────────────────────────────────────
    // The archivist removes the guard before we overwrite the field
    // programmatically (e.g. during refresh) — we don't want spurious
    // dirty signals when we're populating from the model.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches the dirty listener from the directory widget.
     * We call this before populating the widget from the model so we don't
     * accidentally mark the editor dirty when nothing has actually changed.
     */
    private void removeListeners()
    {
        directoryBrowserWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
    }


    // ── Commit the Vault Location Back to the Model ───────────────────────────
    // The archivist reads whatever the user typed in the directory field
    // and stamps it permanently into the LDIF config record.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes the current directory path from the UI widget back into the model.
     * Whatever the user browsed to or typed in the directory field gets written
     * to {@code olcDbDirectory} in the {@link OlcLdifConfig}.
     *
     * @param onSave  {@code true} if this is a full save; {@code false} for page-change commits
     */
    public void commit( boolean onSave )
    {
        if ( database != null )
        {
            database.setOlcDbDirectory( directoryBrowserWidget.getDirectoryPath() );
        }
    }
}
