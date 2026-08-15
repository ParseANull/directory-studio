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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.SearchPageWrapper;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ExportBaseFromWizardPage — LUKE IDENTIFIES THE TARGET ──────────────
// Luke's first step on every export mission is "what are we exporting?"
// — connection, base DN, filter, scope, attributes. This abstract page wraps
// a SearchPageWrapper so every concrete export wizard can plug in its own
// SearchPageWrapper configuration (which fields to show or hide) while sharing
// the validation and save logic.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Abstract base class for the "From" (source selection) page in all export wizards.
 * Renders a {@link SearchPageWrapper} that lets the user define the LDAP search
 * that selects entries to export. Subclasses supply the SearchPageWrapper
 * configuration (visible/invisible fields) in their constructors.
 * Think of Luke identifying the target: "Which connection? Which base DN?
 * Which filter? Which attributes?" — this page answers all four.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class ExportBaseFromWizardPage extends WizardPage implements WidgetModifyListener
{

    /** The wizard. */
    protected ExportBaseWizard wizard;

    /** The search page wrapper. */
    protected SearchPageWrapper spw;


    // ── Luke Reads the Mission Brief Template ─────────────────────────────────────
    // The From page template is filled in by the subclass: page name, wizard
    // reference, and a pre-configured SearchPageWrapper.
    // We set the standard titles and mark the page initially complete since
    // the search defaults are usually valid.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportBaseFromWizardPage.
     * Sets the page title to "Data to Export" and description to "Please define
     * search parameters", then stores the wizard and SearchPageWrapper references.
     * The page starts as complete because the pre-populated search defaults
     * are typically valid; validation re-runs whenever the user edits any field.
     *
     * @param spw       the pre-configured SearchPageWrapper from the subclass.
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportBaseFromWizardPage( String pageName, ExportBaseWizard wizard, SearchPageWrapper spw )
    {
        super( pageName );
        setTitle( Messages.getString( "ExportBaseFromWizardPage.DataToExport" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ExportBaseFromWizardPage.PleaseDefineSearchParameters" ) ); //$NON-NLS-1$
        setPageComplete( true );

        this.wizard = wizard;
        this.spw = spw;
    }


    // ── Luke Sees the Target Selection Panel ─────────────────────────────────────
    // The SearchPageWrapper renders the full search configuration UI — base DN,
    // filter, scope, attribute checkboxes — inside a three-column grid.
    // It loads the current search state and registers for change events.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Renders the SearchPageWrapper inside a three-column grid layout,
     * loads the wizard's current search state into it, and registers this page
     * as a listener so {@link #widgetModified} fires on every change.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );

        spw.createContents( composite );
        spw.loadFromSearch( wizard.getSearch() );
        spw.addWidgetModifyListener( this );

        setControl( composite );
    }


    /**
     * Validates this page and sets the error message
     * if this page is not valid.
     */
    protected void validate()
    {
        setPageComplete( spw.isValid() );
        setErrorMessage( spw.getErrorMessage() );
    }


    // ── Luke Re-Checks the Target After Every Edit ────────────────────────────────
    // Every time the user changes something — the base DN, the filter, the scope —
    // Luke re-validates whether the mission parameters are complete.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Called by the SearchPageWrapper after every user change.
     * Re-validates the page so the wizard's "Next" button reflects
     * whether the search parameters are currently valid.
     *
     * @param event  the widget-modify event (unused; we re-read from the SPW directly).
     */
    public void widgetModified( WidgetModifyEvent event )
    {
        validate();
    }


    // ── Luke Logs His Target Selection ───────────────────────────────────────────
    // At the end of the briefing, Luke writes down his target parameters so
    // the delivery step can use them.
    // We push the SearchPageWrapper's state back into the wizard's search object.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the SearchPageWrapper's current state back into the wizard's search object.
     * Called by the wizard in {@code performFinish()} so the export runnable
     * receives the user's final search configuration.
     */
    public void saveDialogSettings()
    {
        spw.saveToSearch( wizard.getSearch() );
    }

}
