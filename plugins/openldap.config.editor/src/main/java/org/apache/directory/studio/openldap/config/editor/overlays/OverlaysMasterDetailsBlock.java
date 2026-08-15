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
package org.apache.directory.studio.openldap.config.editor.overlays;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.editor.pages.OverlaysPage;
import org.apache.directory.studio.openldap.config.model.OlcChainConfig;
import org.apache.directory.studio.openldap.config.model.OlcConfig;
import org.apache.directory.studio.openldap.config.model.OlcDistProcConfig;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;
import org.apache.directory.studio.openldap.config.model.OlcPBindConfig;
import org.apache.directory.studio.openldap.config.model.OpenLdapConfiguration;
import org.apache.directory.studio.openldap.config.model.overlay.OlcAccessLogConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcAuditlogConfig;
import org.apache.directory.studio.openldap.config.model.overlay.OlcSyncProvConfig;


// ── CLASS: OverlaysMasterDetailsBlock — Vader Adding Subsystems to the Death Star ──
// When Vader arrives on the Death Star's command deck, he surveys the entire
// superstructure from the bridge — all subsystems laid out on the master
// display — and then walks into an individual module bay for a detailed
// inspection.  The Master/Details pattern in Eclipse Forms works the same way:
// the left "master" panel shows all overlays in a scrollable list, and
// whenever the user clicks one, the right "details" panel swaps in the
// appropriate configuration page for that specific overlay type.  This class
// owns the master panel and the wiring that connects list selection to
// detail pages.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Master/Details block that drives the Overlays tab of the OpenLDAP
 * server configuration editor.
 * It displays all configured overlays in a table on the left and delegates
 * the right-hand details panel to overlay-type-specific IDetailsPage
 * implementations.  Used directly by {@link OverlaysPage}.
 * Think of it as Vader's command deck: the master list gives the big picture,
 * and clicking an entry drops you into that subsystem's detail bay.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OverlaysMasterDetailsBlock extends MasterDetailsBlock
{
    /** The associated page */
    private OverlaysPage page;

    // UI Fields
    private TableViewer overlaysTableViewer;
    private Button addButton;
    private Button deleteButton;


    // ── Constructor — Vader Takes Command of the Deck ────────────────────────
    // Vader strides onto the bridge, takes his position at the central command
    // console, and accepts the link to the battle station's control systems.
    // We store a reference to our parent OverlaysPage here so we can pull the
    // current configuration whenever we need to populate the master list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OverlaysMasterDetailsBlock and links it to its parent page.
     * The page reference is how we reach the OpenLdapConfiguration model; we
     * can't do much without it, so it must be non-null.
     *
     * @param page  the OverlaysPage that owns this block — we call getConfiguration() on it
     */
    public OverlaysMasterDetailsBlock( OverlaysPage page )
    {
        super();
        this.page = page;
    }


    // ── createContent — Vader Arranges the Bridge Layout ─────────────────────
    // Vader steps back and surveys the whole bridge from end to end, then
    // adjusts the sash between the tactical display and the detailed scanner
    // readout so both operators have room to work.
    // We call the parent's createContent to build the split-panel layout, then
    // tune the sash weights so the details panel gets twice the space of the
    // master list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the master/details split-panel layout and adjusts the sash
     * weights so the details panel (right side) gets about two-thirds of
     * the available horizontal space.
     * Called by the Eclipse Forms framework when the page is created.
     *
     * @param managedForm  the form that owns this block — passed through to the superclass
     */
    @Override
    public void createContent( IManagedForm managedForm )
    {
        super.createContent( managedForm );

        // Giving the weights of both parts of the SashForm.
        sashForm.setWeights( new int[]
            { 1, 2 } );
    }


    // ── createMasterPart — Vader Activates the Tactical Display ──────────────
    // The bridge's tactical display lights up: it shows all active Death Star
    // subsystems in a scrollable panel with Add and Remove buttons for deploying
    // or decommissioning modules.  This is the left half of the editor — the
    // master list of every configured overlay.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the left-hand "master" panel containing the overlays table and
     * the Add/Delete action buttons.
     * We create a JFace TableViewer wired to the overlay configuration list,
     * attach a selection listener that fires the form's selection event (so
     * the details panel updates), and populate the table from the current
     * OpenLDAP configuration.
     *
     * @param managedForm  the form managing this block — used to fire selection events
     * @param parent       the SWT composite that this panel should fill
     */
    protected void createMasterPart( final IManagedForm managedForm, Composite parent )
    {
        FormToolkit toolkit = managedForm.getToolkit();

        // Creating the Section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.setText( "All Overlays" );
        section.marginWidth = 10;
        section.marginHeight = 5;
        Composite client = toolkit.createComposite( section, SWT.WRAP );
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout( layout );
        toolkit.paintBordersFor( client );
        section.setClient( client );

        // Creating the Table and Table Viewer
        Table overlaysTable = toolkit.createTable( client, SWT.NULL );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 2 );
        gd.heightHint = 20;
        gd.widthHint = 100;
        overlaysTable.setLayoutData( gd );
        final SectionPart sectionPart = new SectionPart( section );
        managedForm.addPart( sectionPart );

        overlaysTableViewer = new TableViewer( overlaysTable );
        overlaysTableViewer.addSelectionChangedListener( event ->
            managedForm.fireSelectionChanged( sectionPart, event.getSelection() ) );

        overlaysTableViewer.setContentProvider( new ArrayContentProvider() );

        overlaysTableViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof OlcOverlayConfig )
                {
                    OlcOverlayConfig overlay = ( OlcOverlayConfig ) element;

                    return overlay.getOlcOverlay();
                }

                return super.getText( element );
            }


            @Override
            public Image getImage( Object element )
            {
                if ( element instanceof OlcOverlayConfig )
                {
                    return OpenLdapConfigurationPlugin.getDefault().getImage(
                        OpenLdapConfigurationPluginConstants.IMG_OVERLAY );
                }

                return super.getImage( element );
            }
        } );

        // Creating the button(s)
        addButton = toolkit.createButton( client, "Add", SWT.PUSH );
        addButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        deleteButton = toolkit.createButton( client, "Delete", SWT.PUSH );
        deleteButton.setEnabled( false );
        deleteButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        initFromInput();
    }


    // ── initFromInput — Vader Downloads the Subsystem Manifest ───────────────
    // Before issuing any orders, Vader pulls up the full manifest of active
    // Death Star subsystems from the central computer and loads it onto the
    // tactical display — only OlcOverlayConfig entries make the cut.
    // We walk the full configuration element list, filter out the overlay
    // entries, and feed them into the table viewer as its input array.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reads the current OpenLDAP configuration and loads all OlcOverlayConfig
     * entries into the master table viewer.
     * We filter the configuration elements here rather than storing overlays
     * in a separate list, which keeps us in sync with whatever the model
     * currently holds.
     */
    private void initFromInput()
    {
        OpenLdapConfiguration configuration = page.getConfiguration();

        List<OlcConfig> configurationElements = configuration.getConfigurationElements();
        List<OlcOverlayConfig> overlayConfigurationElements = new ArrayList<>();

        for ( OlcConfig configurationElement : configurationElements )
        {
            if ( configurationElement instanceof OlcOverlayConfig )
            {
                overlayConfigurationElements.add( ( OlcOverlayConfig ) configurationElement );
            }
        }

        overlaysTableViewer.setInput( overlayConfigurationElements.toArray( new OlcOverlayConfig[0] ) );
    }


    // ── registerPages — Vader Assigns Crew to Each Subsystem Bay ─────────────
    // Vader walks through the Death Star's module bays and assigns a specialist
    // crew to each: the access-log bay gets one team, the sync-prov bay gets
    // another, and so on.  When a subsystem indicator lights up on the tactical
    // display, the right crew takes over.
    // We register a different IDetailsPage implementation for each overlay
    // class, so clicking a row in the master table surfaces the correct
    // configuration form on the right.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the per-overlay-type details pages with the Eclipse Forms
     * DetailsPart.
     * Each overlay class (AccessLog, AuditLog, Chain, etc.) gets its own
     * IDetailsPage, and the framework swaps between them automatically when
     * the user's selection changes in the master list.
     *
     * @param detailsPart  the Eclipse Forms DetailsPart that manages the right-hand panel
     */
    protected void registerPages( DetailsPart detailsPart )
    {
        AccessLogOverlayDetailsPage olcAccessLogOverlayDetailsPage = new AccessLogOverlayDetailsPage( this );
        detailsPart.registerPage( OlcAccessLogConfig.class, olcAccessLogOverlayDetailsPage );

        AuditLogOverlayDetailsPage olcAuditLogOverlayDetailsPage = new AuditLogOverlayDetailsPage( this );
        detailsPart.registerPage( OlcAuditlogConfig.class, olcAuditLogOverlayDetailsPage );

        ChainOverlayDetailsPage olcChainOverlayDetailsPage = new ChainOverlayDetailsPage( this );
        detailsPart.registerPage( OlcChainConfig.class, olcChainOverlayDetailsPage );

        DistProcOverlayDetailsPage oldDistProcOverlayDetailsPage = new DistProcOverlayDetailsPage( this );
        detailsPart.registerPage( OlcDistProcConfig.class, oldDistProcOverlayDetailsPage );

        PasswordPolicyOverlayDetailsPage olcPasswordPolicyOverlayDetailsPage = new PasswordPolicyOverlayDetailsPage(
            this );
        detailsPart.registerPage( OlcDistProcConfig.class, olcPasswordPolicyOverlayDetailsPage );

        PBindAccessOverlayDetailsPage olcPBindAccessOverlayDetailsPage = new PBindAccessOverlayDetailsPage( this );
        detailsPart.registerPage( OlcPBindConfig.class, olcPBindAccessOverlayDetailsPage );

        SyncProvOverlayDetailsPage olcSyncProvOverlayDetailsPage = new SyncProvOverlayDetailsPage( this );
        detailsPart.registerPage( OlcSyncProvConfig.class, olcSyncProvOverlayDetailsPage );
    }


    // ── createToolBarActions — Vader Skips the Optional Controls ─────────────
    // Vader surveys the bridge toolbar and decides none of the optional control
    // stations are needed for this mission — he leaves them unmanned.
    // We don't add any toolbar actions to this block either; the Add/Delete
    // buttons in the master panel are sufficient.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates toolbar actions for this block — intentionally empty because we
     * rely on the Add and Delete buttons in the master panel instead.
     * The Eclipse Forms framework calls this during setup; we must override it
     * even when we have nothing to add.
     *
     * @param managedForm  the form owning this block — not used here
     */
    protected void createToolBarActions( IManagedForm managedForm )
    {
        // No toolbar actions
    }
}
