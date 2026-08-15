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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.openldap.config.model.database.OlcNullConfig;


// ── CLASS: NullDatabaseSpecificDetailsBlock — Obi-Wan Disables the Tractor Beam ─
// Obi-Wan Kenobi slips through the Death Star's lower decks and silently
// disables the tractor beam — a perfectly minimal action with maximum effect.
// The OpenLDAP null backend is the LDAP equivalent: it accepts all operations
// and discards them. Almost nothing to configure. The only question is whether
// to allow bind operations or silently reject them too.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Database-specific UI block for the OpenLDAP null backend ({@code olcNullConfig}).
 * The null backend is a black hole: it accepts every LDAP operation, does nothing
 * with it, and reports success. It's useful for testing and for suppressing
 * operations on subtrees you don't want to serve.
 * The only configurable setting is {@code olcDbBindAllowed} — whether clients
 * are even permitted to bind against this database.
 * Think of Obi-Wan's tractor beam scene: minimal controls, but the right one
 * at the right moment makes all the difference.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NullDatabaseSpecificDetailsBlock extends AbstractDatabaseSpecificDetailsBlock<OlcNullConfig>
{
    /** The single UI control: a checkbox for {@code olcDbBindAllowed}. */
    private Button allowBindCheckbox;


    // ── Disable or Enable the One Control ────────────────────────────────────
    // Obi-Wan approaches the tractor beam control panel — there's really only
    // one lever that matters here. We receive the page and model references
    // and get ready to control that single lever.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the null-database block, binding it to the given page and model.
     * No browser connection is needed — the null backend has no live data to browse.
     *
     * @param detailsPage  the parent details page
     * @param database     the {@link OlcNullConfig} model object
     */
    public NullDatabaseSpecificDetailsBlock( DatabasesDetailsPage detailsPage, OlcNullConfig database )
    {
        super( detailsPage, database );
    }


    // ── Build the One-Lever Control Panel ─────────────────────────────────────
    // Obi-Wan finds the panel and locates the single relevant control.
    // We create one checkbox: "Allow bind to the database."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the block's UI — a single "allow bind" checkbox.
     * The null backend has exactly one configurable item, {@code olcDbBindAllowed}.
     * If unchecked, even bind requests are silently swallowed; if checked, clients
     * can authenticate against this database (which always succeeds).
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     * @return         the top-level composite
     */
    public Composite createBlockContent( Composite parent, FormToolkit toolkit )
    {
        // Composite
        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Allow Bind
        allowBindCheckbox = toolkit.createButton( composite, "Allow bind to the database", SWT.CHECK );

        return composite;
    }


    // ── Re-read the Lever Position ────────────────────────────────────────────
    // Obi-Wan glances at the panel to see what position the lever is currently in.
    // We read the model's olcDbBindAllowed value and update the checkbox to match.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the allow-bind checkbox from the current {@link OlcNullConfig} model.
     * If the model has no value set (null), we default the checkbox to unchecked.
     */
    public void refresh()
    {
        removeListeners();

        if ( database != null )
        {
            // Allow Bind
            Boolean allowBind = database.getOlcDbBindAllowed();

            if ( allowBind != null )
            {
                allowBindCheckbox.setSelection( allowBind );
            }
            else
            {
                allowBindCheckbox.setSelection( false );
            }
        }

        addListeners();
    }


    // ── Watch the Lever ───────────────────────────────────────────────────────
    // Obi-Wan arms the panel's change detector — any movement of the lever
    // will be noticed and reported.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the dirty listener to the allow-bind checkbox.
     * Any user change triggers the "editor is dirty" signal upward.
     */
    private void addListeners()
    {
        allowBindCheckbox.addSelectionListener( dirtySelectionListener );
    }


    // ── Stop Watching the Lever ───────────────────────────────────────────────
    // Obi-Wan disarms the detector before touching the panel himself,
    // so his own programmatic update doesn't trigger a false alarm.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches the dirty listener from the allow-bind checkbox.
     * Called before populating the checkbox from the model so we don't
     * fire spurious dirty signals.
     */
    private void removeListeners()
    {
        allowBindCheckbox.removeSelectionListener( dirtySelectionListener );
    }


    // ── Commit the Lever Position to the Record ───────────────────────────────
    // Obi-Wan's choice is recorded — the tractor beam is either disabled or not.
    // We push the checkbox's current state into the model's olcDbBindAllowed field.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes the current checkbox state back into the model.
     * The UI's allow-bind toggle maps directly to {@code olcDbBindAllowed} in
     * the {@link OlcNullConfig}.
     *
     * @param onSave  {@code true} for a full save; {@code false} for page-change commits
     */
    public void commit( boolean onSave )
    {
        if ( database != null )
        {
            // Allow Bind
            database.setOlcDbBindAllowed( allowBindCheckbox.getSelection() );
        }
    }
}
