/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.widgets;


import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.model.AclItem;


// ── CLASS: OpenLdapAclVisualEditorComposite — GRAND MOFF'S HOLOTABLE ─────────
// Grand Moff Tarkin stands before the Death Star's holotable, studying two
// panels: the "Access to What" panel (the target clause) and the "Access by
// Who" panel (the list of who-clause rows). This composite is that holotable.
// It extends ScrolledComposite so both panels can be scrolled when the window
// is too small. The WHAT widget and the WHO builder widget are created inside a
// single-column inner Composite that fills the scrolled viewport. refresh()
// pushes the current context model into both widgets. getInput() assembles a
// new AclItem from the current model's clauses and returns its toString().
// saveWidgetSettings() delegates to the WHAT widget for any persistent state
// (e.g. expand/collapse state of the attributes expandable section).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main visual editor composite. Extends {@link ScrolledComposite} and
 * contains:
 * <ul>
 *   <li>An "Access to What" group (via {@link OpenLdapAclWhatClauseWidget})</li>
 *   <li>An "Access by Who" group (via {@link OpenLdapAclWhoClausesBuilderWidget})</li>
 * </ul>
 * The two sub-widgets share the same {@link OpenLdapAclValueWithContext} context
 * and keep the model up to date via their own listeners.
 *
 * <p>Think of this class as Grand Moff Tarkin's holotable — a scrollable command
 * surface with the WHAT and WHO panels side by side.</p>
 *
 * @see OpenLdapAclWhatClauseWidget
 * @see OpenLdapAclWhoClausesBuilderWidget
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclVisualEditorComposite extends ScrolledComposite
{
    /** The ACL context */
    private OpenLdapAclValueWithContext context;

    // UI widgets
    /** The WHAT clause Widget */
    private OpenLdapAclWhatClauseWidget whatClauseWidget;

    /** The WHO clause widget */
    private OpenLdapAclWhoClausesBuilderWidget whoClausesBuilderWidget;


    // ── Constructing the Visual Editor Holotable ───────────────────────────────
    // Grand Moff Tarkin opens the holotable: creates the scrollable viewport,
    // creates the single-column inner composite, then installs both the WHAT
    // and WHO widgets inside it. The ScrolledComposite is configured to expand
    // both axes and compute the minimum size from the inner composite.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new visual editor composite. Builds the scrollable viewport,
     * creates the inner {@link GridLayout} composite, and installs the
     * {@link OpenLdapAclWhatClauseWidget} and {@link OpenLdapAclWhoClausesBuilderWidget}
     * inside it.
     *
     * <p>For example — the tab folder creating the Visual tab content:</p>
     * <pre>
     *   OpenLdapAclVisualEditorComposite visual =
     *       new OpenLdapAclVisualEditorComposite(container, context, SWT.NONE);
     *   visual.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
     * </pre>
     *
     * @param parent   The parent composite.
     * @param context  The shared ACL context.
     * @param style    SWT style bits (H_SCROLL and V_SCROLL are added automatically).
     */
    public OpenLdapAclVisualEditorComposite( Composite parent, OpenLdapAclValueWithContext context, int style )
    {
        super( parent, style | SWT.H_SCROLL | SWT.V_SCROLL );

        this.context = context;

        // Creating the composite
        Composite visualEditorComposite = new Composite( this, SWT.NONE );
        visualEditorComposite.setLayout( new GridLayout() );
        visualEditorComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Creating the WhatClause widget
        whatClauseWidget = new OpenLdapAclWhatClauseWidget( this, visualEditorComposite, context );

        // Creating the WhoClause widget
        whoClausesBuilderWidget = new OpenLdapAclWhoClausesBuilderWidget( this, context );
        whoClausesBuilderWidget.create( visualEditorComposite );

        // Configuring the composite
        setContent( visualEditorComposite );
        setExpandHorizontal( true );
        setExpandVertical( true );
        setMinSize( visualEditorComposite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    }


    // ── Refreshing Both Panels From the Context Model ─────────────────────────
    // When the tab folder switches to the Visual tab, Tarkin calls refresh() to
    // redraw both the WHAT and WHO panels from the current ACL context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes both sub-widgets from the current ACL context. Called by the
     * tab folder when switching to the Visual Editor tab.
     *
     * <p>For example — syncing after the Source tab was edited:</p>
     * <pre>
     *   visualComposite.refresh();
     *   // WHAT and WHO panels now reflect the parsed ACL model.
     * </pre>
     */
    public void refresh()
    {
        // Setting the input ACL to the widgets
        whatClauseWidget.refresh();
        whoClausesBuilderWidget.refresh();
    }


    // ── Building the Canonical ACL String From the Model ─────────────────────
    // Tarkin assembles a new AclItem from the current context's what and who
    // clauses and returns its toString() as the canonical ACL string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the canonical ACL string assembled from the current model. Constructs
     * a new {@link AclItem} from the context's what clause and who clauses, then
     * returns its {@link AclItem#toString()}.
     *
     * @return  The canonical ACL string derived from the current GUI state.
     * @throws ParseException  If the current model is syntactically invalid (unused here; declared for interface).
     */
    public String getInput() throws ParseException
    {
        AclItem aclItem = new AclItem( context.getAclItem().getWhatClause(), context.getAclItem().getWhoClauses() );

        return aclItem.toString();
    }


    // ── Persisting Widget State Before Close ──────────────────────────────────
    // Tarkin orders his aide to save the current panel expand/collapse state
    // so the holotable looks the same next time it is opened.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Saves any widget-level preferences (e.g. expand/collapse state of the
     * attributes expandable section) by delegating to
     * {@link OpenLdapAclWhatClauseWidget#saveWidgetSettings()}.
     */
    public void saveWidgetSettings()
    {
        whatClauseWidget.saveWidgetSettings();
    }
}
