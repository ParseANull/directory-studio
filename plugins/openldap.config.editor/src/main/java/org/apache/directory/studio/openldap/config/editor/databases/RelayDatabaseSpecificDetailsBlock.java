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


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.openldap.common.ui.widgets.EntryWidget;
import org.apache.directory.studio.openldap.config.model.database.OlcRelayConfig;


// ── CLASS: RelayDatabaseSpecificDetailsBlock — Palpatine's Imperial Relay Network ─
// The Empire didn't always have a Death Star in every system — sometimes they
// relayed commands through an intermediary base to reach a distant outpost.
// OpenLDAP's relay backend works the same way: it re-routes LDAP operations
// to a different DN subtree within the same server, avoiding duplication.
// The only thing to configure is which DN to relay to.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Database-specific UI block for the OpenLDAP relay backend ({@code olcRelayConfig}).
 * The relay backend forwards LDAP operations to a different DN on the same server.
 * It's used when you want to expose a subtree under a different suffix without
 * duplicating data — the relay database re-routes queries to the real backend.
 * The only configurable item is the {@code olcRelay} attribute: the target DN.
 * Think of this as the Imperial relay station: you configure where to forward,
 * and everything else is transparent.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RelayDatabaseSpecificDetailsBlock extends AbstractDatabaseSpecificDetailsBlock<OlcRelayConfig>
{
    /** The entry-browser widget for selecting the target relay DN. */
    private EntryWidget relayEntryWidget;


    // ── Designate the Relay Target ────────────────────────────────────────────
    // The Imperial relay station receives its configuration briefing: which
    // page manages it, what model object it represents, and which live LDAP
    // connection to use for the DN picker.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the relay-database block.
     * A browser connection is required here because we show an entry-picker
     * widget that lets the user browse the live LDAP tree to select the relay DN.
     *
     * @param detailsPage       the parent details page
     * @param database          the {@link OlcRelayConfig} model object
     * @param browserConnection the LDAP browser connection for the DN picker
     */
    public RelayDatabaseSpecificDetailsBlock( DatabasesDetailsPage detailsPage, OlcRelayConfig database,
        IBrowserConnection browserConnection )
    {
        super( detailsPage, database, browserConnection );
    }


    // ── Build the Relay-Target Selector ──────────────────────────────────────
    // The relay station's control panel has exactly one control: pick the
    // target DN where we forward all incoming traffic.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the block's UI — a single DN-picker row for the {@code olcRelay} attribute.
     * The relay backend only needs one thing configured: the target DN to forward to.
     * We show an {@link EntryWidget} so the user can browse or type a DN.
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     * @return         the top-level composite for this block
     */
    public Composite createBlockContent( Composite parent, FormToolkit toolkit )
    {
        // Composite
        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout( 3, false ) );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Relay
        toolkit.createLabel( composite, "Relay:" );
        relayEntryWidget = new EntryWidget( browserConnection );
        relayEntryWidget.createWidget( composite, toolkit );
        relayEntryWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        return composite;
    }


    // ── Re-read the Relay Target from the Model ───────────────────────────────
    // The relay station checks its current forwarding address and updates
    // the DN picker to display it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the relay DN entry widget from the current {@link OlcRelayConfig} model.
     * If the model has a relay DN, we show it in the widget; null models are ignored.
     */
    public void refresh()
    {
        removeListeners();

        if ( database != null )
        {
            // Relay
            relayEntryWidget.setInput( database.getOlcRelay() );
        }

        addListeners();
    }


    // ── Watch for Target Changes ──────────────────────────────────────────────
    // The relay station monitors the forwarding-address control for changes —
    // any edit marks the editor as dirty and needing to be saved.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches the dirty listener to the relay entry widget.
     * Any user edit to the relay DN will fire the "editor is dirty" signal.
     */
    private void addListeners()
    {
        relayEntryWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
    }


    // ── Stop Watching for Target Changes ─────────────────────────────────────
    // The relay station suspends monitoring before we programmatically update
    // the field from the model, to avoid spurious dirty signals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches the dirty listener from the relay entry widget.
     * Called before populating the widget from the model to avoid false dirty signals.
     */
    private void removeListeners()
    {
        relayEntryWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
    }


    // ── Commit the Relay Target Back to the Model ─────────────────────────────
    // The relay station stores the chosen forwarding address in the official record.
    // If the user left the DN empty or cleared it, we null out the relay attribute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes the current relay DN value back into the {@link OlcRelayConfig} model.
     * If the widget holds an empty or null DN, we clear the {@code olcRelay} attribute.
     *
     * @param onSave  {@code true} for a full editor save; {@code false} for page-change commits
     */
    public void commit( boolean onSave )
    {
        if ( database != null )
        {
            // Relay
            Dn relay = relayEntryWidget.getDn();

            if ( ( relay != null ) & ( !Dn.EMPTY_DN.equals( relay ) ) )
            {
                database.setOlcRelay( relay );
            }
            else
            {
                database.setOlcRelay( null );
            }
        }
    }
}
