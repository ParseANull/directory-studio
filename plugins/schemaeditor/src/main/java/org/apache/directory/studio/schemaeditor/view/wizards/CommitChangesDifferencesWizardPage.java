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
package org.apache.directory.studio.schemaeditor.view.wizards;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.difference.DifferenceEngine;
import org.apache.directory.studio.schemaeditor.view.widget.DifferencesWidget;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: CommitChangesDifferencesWizardPage — Jyn Reviews Scarif Data ──────
// In Rogue One, Jyn Erso and Cassian Andor fight their way into the Scarif
// data vault and pull up the exact schematics they need to transmit — they
// have to see precisely what they're sending before they beam it to the fleet.
// This wizard page is that moment: before we commit any schema changes to the
// server, we show the user a clear diff — exactly what has changed, entry by
// entry — so they can review it with clear eyes before hitting Finish.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The differences review page inside the Commit Changes wizard.
 * It computes the delta between the project's initial schema snapshot and
 * whatever the schema looks like right now, then renders that diff in a
 * {@link DifferencesWidget} so the user can see exactly what will be pushed
 * to the server.
 * Think of this as Jyn pulling up the Death Star plans on the Scarif data
 * terminal — we show you everything before you beam it out.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CommitChangesDifferencesWizardPage extends WizardPage
{
    // UI Fields
    private DifferencesWidget differencesWidget;


    // ── Jyn Arrives At The Data Vault ────────────────────────────────────────
    // Jyn and Cassian reach the Scarif data vault corridor — before any files
    // move, they identify which terminal to use and what mission objective
    // they're here to accomplish.
    // Jyn announces to the control room: "We're here for the Death Star plans."
    // We configure the page title, description, and the wizard banner icon
    // so the user knows immediately what this step is about.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wires up the page identity: title, description, and the wizard header
     * image that appears while this page is active.
     * We don't accept any constructor arguments because all the data this page
     * needs comes straight from the active project at display time.
     *
     * <p>For example — Jyn steps up to the data terminal and states her mission:</p>
     * <pre>
     *   page.setTitle( "Review Changes" );
     *   page.setDescription( "Here is everything we're about to transmit." );
     *   page.setImageDescriptor( commitWizardBanner );
     * </pre>
     */
    protected CommitChangesDifferencesWizardPage()
    {
        super( "CommitChangesDifferencesWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "CommitChangesDifferencesWizardPage.CommitChanges" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "CommitChangesDifferencesWizardPage.DisplayModifications" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_COMMIT_CHANGES_WIZARD ) );
    }


    // ── Jyn Activates The Holographic Display ────────────────────────────────
    // Jyn slots the data card into the terminal and the holographic schematics
    // bloom outward — a three-dimensional picture of everything in the vault.
    // The full diff view materialises on screen: every changed attribute type,
    // every modified object class, laid out so nothing is hidden.
    // We build the SWT composite, drop the DifferencesWidget into it, and
    // load it with the computed diff so the user sees the full picture.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT widget tree for this page and loads the diff data into it.
     * Eclipse calls this when the page is about to become visible; we construct
     * the layout, embed the {@link DifferencesWidget}, and immediately populate
     * it so the user doesn't see a blank screen.
     *
     * <p>For example — Jyn activates the holographic display in the vault:</p>
     * <pre>
     *   Composite terminal = new Composite( vaultWall, SWT.NULL );
     *   DifferencesWidget hologram = new DifferencesWidget();
     *   hologram.createWidget( terminal );
     *   hologram.setInput( allThePlans );   // everything we're about to transmit
     * </pre>
     *
     * @param parent  the parent composite Eclipse hands us — we attach our
     *                own composite to it as a child.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        differencesWidget = new DifferencesWidget();
        differencesWidget.createWidget( composite );

        initFields();

        setControl( composite );
    }


    // ── Pulling The Right Plans From The Archive ──────────────────────────────
    // Once the terminal is live, Jyn has to navigate to the specific data
    // partition labelled "Stardust" and pull exactly those files — not the
    // whole archive, just the delta between what was and what is now.
    // She locates the correct data card and hands it to the display system.
    // We call DifferenceEngine to compare the project's original schema
    // snapshot against today's version and feed the result to the widget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Computes the diff between the project's initial schema snapshot and its
     * current schema state, then feeds that list of differences into the widget.
     * We do this on page initialisation so the display is ready the moment the
     * user sees this page.
     *
     * <p>For example — Jyn navigates to the "Stardust" partition and extracts
     * the delta:</p>
     * <pre>
     *   Project activeProject = vault.getOpenProject();
     *   List differences = DifferenceEngine.getDifferences(
     *       activeProject.getInitialSchema(),
     *       activeProject.getCurrentSchemas() );
     *   hologram.setInput( differences );
     * </pre>
     */
    private void initFields()
    {
        Project project = Activator.getDefault().getProjectsHandler().getOpenProject();

        differencesWidget.setInput( DifferenceEngine.getDifferences( project.getInitialSchema(), project
            .getSchemaHandler().getSchemas() ) );
    }


    // ── Powering Down The Terminal After Transmission ────────────────────────
    // After the plans have been beamed up to the fleet, Jyn and Cassian's
    // work on the Scarif terminal is done — the holographic display powers
    // down, circuits cool, resources are returned to the station.
    // The DifferencesWidget holds onto SWT resources (fonts, images, listeners)
    // that we must release explicitly when this page is torn down.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases the SWT and other OS-level resources held by the
     * {@link DifferencesWidget} when this page is destroyed.
     * Eclipse does not free widget resources automatically — we must call
     * {@code dispose()} ourselves or we'll leak handles.
     *
     * <p>For example — the Scarif data terminal goes dark after the mission:</p>
     * <pre>
     *   holographicDisplay.dispose();   // release GPU memory, SWT handles
     *   super.dispose();                // let the base page clean up too
     * </pre>
     */
    public void dispose()
    {
        differencesWidget.dispose();

        super.dispose();
    }
}
