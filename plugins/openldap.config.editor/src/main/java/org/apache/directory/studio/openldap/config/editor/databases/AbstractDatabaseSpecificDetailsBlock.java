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


import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.model.database.OlcDatabaseConfig;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;


// ── CLASS: AbstractDatabaseSpecificDetailsBlock — Palpatine's Archive Template ─
// Palpatine's Imperial archives hold many record categories, each with its own
// section layout, but they all share the same basic filing conventions: who owns
// the record, what database it came from, and which connection pulled it up.
// This abstract class is that common filing template — it holds the shared
// references and the "mark editor dirty" listeners that every concrete DB block needs.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base class for every database-specific UI block (BDB, MDB, LDIF, relay, etc.).
 * It carries the three references that every subclass needs — the details page,
 * the typed database config object, and the browser connection — plus the three
 * standard listeners that mark the editor dirty when the user edits a field.
 * Think of this as Palpatine's master archive template: concrete subclasses just
 * fill in the database-type-specific fields on top of this foundation.
 *
 * @param <D>  the concrete {@link OlcDatabaseConfig} subtype this block manages
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractDatabaseSpecificDetailsBlock<D extends OlcDatabaseConfig> implements
    DatabaseSpecificDetailsBlock
{
    /** The details page that owns this block — we call back to it to mark the editor dirty. */
    protected DatabasesDetailsPage detailsPage;

    /** The database config object we're editing — typed to the specific backend. */
    protected D database;

    /** The live LDAP browser connection, used for entry-browser widgets. */
    protected IBrowserConnection browserConnection;

    // Listeners — these three are the "user changed something" signals we attach
    // to every editable widget. They all do the same thing: tell the page the
    // editor is now dirty and needs to be saved.
    protected ModifyListener dirtyModifyListener = event -> detailsPage.setEditorDirty();

    protected WidgetModifyListener dirtyWidgetModifyListener = event -> detailsPage.setEditorDirty();

    protected SelectionListener dirtySelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            detailsPage.setEditorDirty();
        }
    };


    // ── Full Constructor: All Three References ────────────────────────────────
    // The archivist receives the full briefing: which ledger-keeper owns this
    // record, what the record's database model is, and which terminal
    // connection feeds the live data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a block with all three required references.
     * Use this constructor when the block needs to show entry-browser widgets
     * that require a live LDAP connection (e.g. DN pickers).
     *
     * @param detailsPage       the parent details page — we'll call {@code setEditorDirty()} on it
     * @param database          the typed database config object we're binding the UI to
     * @param browserConnection the LDAP browser connection for entry-picker widgets
     */
    public AbstractDatabaseSpecificDetailsBlock( DatabasesDetailsPage detailsPage, D database,
        IBrowserConnection browserConnection )
    {
        this.detailsPage = detailsPage;
        this.database = database;
        this.browserConnection = browserConnection;
    }


    // ── Connection-Free Constructor ───────────────────────────────────────────
    // Sometimes the archive record type doesn't need live data lookups —
    // no entry-browser, no DN picker. The archivist skips that terminal
    // and just takes the essentials.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a block without a browser connection.
     * Use for database types that don't need entry-picker widgets (e.g. null backend,
     * LDIF backend). Delegates to the full constructor with {@code null} as the connection.
     *
     * @param detailsPage  the parent details page
     * @param database     the typed database config object
     */
    public AbstractDatabaseSpecificDetailsBlock( DatabasesDetailsPage detailsPage, D database )
    {
        this( detailsPage, database, null );
    }


    // ── Retrieve the Owning Ledger-Keeper ─────────────────────────────────────
    // The archivist hands back the reference to the ledger-keeper in charge
    // of this section — so callers can reach the page without drilling through
    // private fields.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the details page that owns this block.
     * The caller (typically the master/details framework) uses this to coordinate
     * saves and refreshes across the different parts of the editor.
     *
     * @return the owning {@link DatabasesDetailsPage}
     */
    public DatabasesDetailsPage getDetailsPage()
    {
        return detailsPage;
    }
}
