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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;


// ── CLASS: CoreSchemasSelectionWidget — LUKE PACKS HIS KIT ON TATOOINE ───────
// Luke stands at the crossroads before leaving for Alderaan: first he has to
// commit to a path (Rebel Alliance / ApacheDS, or the other way / OpenLDAP),
// and then tick off precisely which items he needs to take along for the mission.
// We mimic that same two-step selection: pick a server type, choose your schemas.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An SWT composite widget that lets users choose which "core" LDAP schemas to
 * import into a new schema project. It appears inside import wizards where we
 * are bootstrapping a project from an existing LDAP server installation.
 * Think of it as Luke's pre-mission checklist on Tatooine: first commit to a
 * server type (ApacheDS or OpenLDAP), then tick off the individual schemas you
 * actually need — the widget keeps both steps in one tidy UI panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CoreSchemasSelectionWidget
{
    // ── CLASS: ServerTypeEnum — CHOOSING YOUR ALLEGIANCE ─────────────────────────
    // Before Luke can pack, he has to choose a side: Rebel Alliance or stay on
    // Tatooine. Before we can show schemas, we need to know which server's built-in
    // list to display — the two flavors ship with quite different schema bundles.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Identifies which LDAP server flavor this widget is currently configured for.
     * ApacheDS and OpenLDAP each bundle a different set of built-in core schemas,
     * so we need this flag to know which list to load into the checkbox table.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum ServerTypeEnum
    {
        APACHE_DS, OPENLDAP
    }

    /** The array containing the 'core' from ApacheDS */
    private static final String[] coreSchemasFromApacheDS = new String[]
        { "adsconfig", "apache", "apachedns", "apachemeta", "autofs", "collective", "corba", "core", "cosine", "dhcp", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            "inetorgperson", "java", "krb5kdc", "mozilla", "nis", "pwdpolicy", "samba", "system" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$

    /** The array containing the 'core' from OpenLDAP */
    private static final String[] coreSchemasFromOpenLdap = new String[]
        { "collective", "corba", "core", "cosine", "dyngroup", "duaconf", "inetorgperson", "java", "misc", "nis", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            "openldap", "ppolicy", "system" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // UI Fields
    private Button typeApacheDSButton;
    private Button typeOpenLDAPButton;
    private CheckboxTableViewer coreSchemasTableViewer;


    // ── ASSEMBLING THE GEAR AT THE LARS HOMESTEAD ────────────────────────────────
    // Luke spreads everything out on the table before heading to Mos Eisley: radio
    // buttons for which faction he is joining, a checkboxed list of schemas to bring,
    // and "Select All" / "Deselect All" helpers so he does not have to tick 18 boxes
    // one by one. All of it ends up in one tidy composite ready to embed in a wizard.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the full SWT UI for this widget and attaches it to {@code parent}.
     * We create a server-type radio group at the top and a scrollable checkbox
     * table below it, plus convenience buttons for bulk selection.
     * The returned composite is what the caller should drop into its own layout.
     *
     * <p>For example — Luke assembles his pre-mission kit:</p>
     * <pre>
     *   "ApacheDS or OpenLDAP?" — radio buttons let Luke pick his faction.
     *   The checkbox table fills in with that faction's core schema list.
     *   Luke hits "Select All", unchecks "adsconfig", clicks Next.
     * </pre>
     *
     * @param parent  the SWT composite that will contain this widget's controls
     * @return        the top-level composite we built; embed it in a wizard page
     */
    public Composite createWidget( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Server Type Group
        Group serverTypeGroup = new Group( composite, SWT.NONE );
        serverTypeGroup.setText( Messages.getString( "CoreSchemasSelectionWidget.ServerType" ) ); //$NON-NLS-1$
        serverTypeGroup.setLayout( new GridLayout( 2, false ) );
        serverTypeGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Type ApacheDS Button
        typeApacheDSButton = new Button( serverTypeGroup, SWT.RADIO );
        typeApacheDSButton.setText( Messages.getString( "CoreSchemasSelectionWidget.ApacheDS" ) ); //$NON-NLS-1$
        typeApacheDSButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                resetTableViewerWithCoreSchemasFromApacheDS();
            }
        } );

        // Type OpenLDAP Button
        typeOpenLDAPButton = new Button( serverTypeGroup, SWT.RADIO );
        typeOpenLDAPButton.setText( Messages.getString( "CoreSchemasSelectionWidget.OpenLDAP" ) ); //$NON-NLS-1$
        typeOpenLDAPButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                resetTableViewerWithCoreSchemasFromOpenLdap();
            }
        } );

        // Core Schemas Label
        Label coreSchemaslabel = new Label( composite, SWT.NONE );
        coreSchemaslabel.setText( Messages.getString( "CoreSchemasSelectionWidget.ChooseCoreSchemas" ) ); //$NON-NLS-1$
        coreSchemaslabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Core Schemas TableViewer
        coreSchemasTableViewer = new CheckboxTableViewer( new Table( composite, SWT.BORDER | SWT.CHECK
            | SWT.FULL_SELECTION ) );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 2 );
        gridData.heightHint = 127;
        coreSchemasTableViewer.getTable().setLayoutData( gridData );
        coreSchemasTableViewer.setContentProvider( new ArrayContentProvider() );
        coreSchemasTableViewer.setLabelProvider( new LabelProvider()
        {
            public Image getImage( Object element )
            {
                return Activator.getDefault().getImage( PluginConstants.IMG_SCHEMA );
            }
        } );

        Button coreSchemasTableSelectAllButton = new Button( composite, SWT.PUSH );
        coreSchemasTableSelectAllButton.setText( Messages.getString( "CoreSchemasSelectionWidget.SelectAll" ) ); //$NON-NLS-1$
        coreSchemasTableSelectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        coreSchemasTableSelectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                coreSchemasTableViewer.setAllChecked( true );
            }
        } );

        Button coreSchemasTableDeselectAllButton = new Button( composite, SWT.PUSH );
        coreSchemasTableDeselectAllButton.setText( Messages.getString( "CoreSchemasSelectionWidget.DeselectAll" ) ); //$NON-NLS-1$
        coreSchemasTableDeselectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        coreSchemasTableDeselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                coreSchemasTableViewer.setAllChecked( false );
            }
        } );

        return composite;
    }


    // ── READING EVERYTHING IN THE BAG: CHECKED SCHEMAS ───────────────────────────
    // Luke peeks into his pack and reads off every item he has ticked, grayed or not.
    // He is not judging whether each item was deliberate — just reporting what is in
    // the bag. Grayed items (auto-selected dependencies) are included in this list.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns every schema name the user has checked in the table, including any
     * that are shown in the grayed (indeterminate) state. This is the raw checked
     * set — if you want only explicitly user-chosen schemas, use
     * {@link #getSelectedCoreSchemas()} instead.
     *
     * <p>For example — Luke reads back his full checklist:</p>
     * <pre>
     *   Checked items: "core" (solid), "cosine" (solid), "collective" (grayed).
     *   getCheckedCoreSchemas() returns all three — grayed or not.
     * </pre>
     *
     * @return  array of all currently checked schema names; may include grayed entries
     */
    public String[] getCheckedCoreSchemas()
    {
        return Arrays.asList( coreSchemasTableViewer.getCheckedElements() ).toArray( new String[0] );
    }


    // ── ITEMS LUKE IS UNSURE ABOUT: GRAYED SCHEMAS ───────────────────────────────
    // Luke marks some gear with a question mark — he ticked it but is not fully
    // committed, maybe it came along as a dependency of something else he chose.
    // Grayed schemas in our table represent auto-selected, non-explicit selections.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema names currently displayed as grayed (indeterminate) in the
     * table. Grayed entries are typically auto-selected dependencies rather than
     * deliberate user choices. We use the delta between checked and grayed to
     * compute the truly user-selected set in {@link #getSelectedCoreSchemas()}.
     *
     * <p>For example — Luke's uncertain items:</p>
     * <pre>
     *   Luke checked "collective" automatically because "core" needs it.
     *   getGrayedCoreSchemas() returns ["collective"] to signal: not a real choice.
     * </pre>
     *
     * @return  array of schema names in the grayed/indeterminate state
     */
    public String[] getGrayedCoreSchemas()
    {
        return Arrays.asList( coreSchemasTableViewer.getGrayedElements() ).toArray( new String[0] );
    }


    // ── WHAT LUKE ACTUALLY CHOSE TO BRING ALONG ──────────────────────────────────
    // This is Luke's real list — the things he deliberately packed, not items that
    // fell in because of some dependency. We subtract the grayed (implicit) items
    // from the checked (all) set to arrive at the user's genuine selections.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns only the schemas the user explicitly chose — those that are checked
     * but not grayed. Grayed items are considered implicitly-selected side effects
     * and are excluded here so callers get the real user intent.
     *
     * <p>For example — Luke's deliberate selections:</p>
     * <pre>
     *   Checked:  ["core", "cosine", "collective"]
     *   Grayed:   ["collective"]  (auto-added)
     *   Selected: ["core", "cosine"]  — what Luke actually wants
     * </pre>
     *
     * @return  array of schema names that are checked AND not grayed
     */
    public String[] getSelectedCoreSchemas()
    {
        List<String> selectedSchemas = new ArrayList<String>();

        selectedSchemas.addAll( Arrays.asList( getCheckedCoreSchemas() ) );
        selectedSchemas.removeAll( Arrays.asList( getGrayedCoreSchemas() ) );

        return selectedSchemas.toArray( new String[0] );
    }


    // ── WHICH PATH DID LUKE CHOOSE: READING THE RADIO BUTTON ────────────────────
    // Luke has committed to a direction — either he pressed the ApacheDS radio button
    // or the OpenLDAP one. We read his choice here. If he has not pressed either yet
    // we return null and let the caller figure out what to do with the ambiguity.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns which LDAP server type the user has selected via the radio buttons,
     * or {@code null} if neither is pressed. Callers typically use this to decide
     * which schema list to work with once the wizard page is complete.
     *
     * <p>For example — Luke announces his allegiance:</p>
     * <pre>
     *   Pressed "ApacheDS"  →  ServerTypeEnum.APACHE_DS
     *   Pressed "OpenLDAP"  →  ServerTypeEnum.OPENLDAP
     *   Still staring at the twin suns  →  null
     * </pre>
     *
     * @return  the selected {@link ServerTypeEnum}, or {@code null} if nothing is selected
     */
    public ServerTypeEnum getServerType()
    {
        if ( typeApacheDSButton.getSelection() )
        {
            return ServerTypeEnum.APACHE_DS;
        }
        else if ( typeOpenLDAPButton.getSelection() )
        {
            return ServerTypeEnum.OPENLDAP;
        }
        else
        {
            // Default
            return null;
        }
    }


    // ── UNCLE OWEN MAKES THE CALL: PRE-SELECTING A SERVER TYPE ───────────────────
    // Owen Lars used to decide things for Luke before Luke could object. Here the
    // caller does the same: it passes a server type and we select the right radio
    // button, then load the matching schema list so everything is already set up.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Pre-configures the widget by selecting a server type and loading the matching
     * schema list into the table. Call this after {@link #createWidget(Composite)} to
     * restore a previously saved wizard-page state. Passing {@code null} is a no-op.
     *
     * <p>For example — the wizard restores Luke's earlier choices:</p>
     * <pre>
     *   init(ServerTypeEnum.APACHE_DS)  →  ApacheDS radio selected, table repopulated
     *   init(ServerTypeEnum.OPENLDAP)   →  OpenLDAP radio selected, table repopulated
     *   init(null)                      →  nothing happens
     * </pre>
     *
     * @param selectedButton  which server type to pre-select; {@code null} means do nothing
     */
    public void init( ServerTypeEnum selectedButton )
    {
        // Setting the selected button
        if ( selectedButton != null )
        {
            switch ( selectedButton )
            {
                case APACHE_DS:
                    typeApacheDSButton.setSelection( true );
                    resetTableViewerWithCoreSchemasFromApacheDS();
                    break;
                case OPENLDAP:
                    typeOpenLDAPButton.setSelection( true );
                    resetTableViewerWithCoreSchemasFromOpenLdap();
                    break;
            }
        }
    }


    // ── SWITCHING TO REBEL SPECS: LOADING APACHEDS SCHEMAS ───────────────────────
    // Luke just pressed the ApacheDS radio button — time to swap out the old kit.
    // We wipe all existing checks first so no OpenLDAP-flavored items carry over,
    // then reload the table with the ApacheDS bundle of core schemas.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all current checks and repopulates the table with ApacheDS core schemas.
     * We call this whenever the user switches to the ApacheDS radio button so that
     * stale OpenLDAP selections do not bleed into the new context.
     *
     * <p>For example — Luke swaps schematics:</p>
     * <pre>
     *   Luke: "Forget the OpenLDAP specs — give me the Rebel ones."
     *   Table clears. ApacheDS schemas fill in. No old ticks remain.
     * </pre>
     */
    private void resetTableViewerWithCoreSchemasFromApacheDS()
    {
        coreSchemasTableViewer.setAllChecked( false );
        coreSchemasTableViewer.setInput( coreSchemasFromApacheDS );
    }


    // ── SWITCHING TO IMPERIAL SPECS: LOADING OPENLDAP SCHEMAS ────────────────────
    // Luke changed direction — now it's the OpenLDAP route. Same drill: clear
    // everything that was there before and load the correct schema set so the user
    // sees exactly what OpenLDAP ships with, no ApacheDS noise mixed in.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all current checks and repopulates the table with OpenLDAP core schemas.
     * Called when the user picks the OpenLDAP radio button so the table shows only
     * schemas relevant to that server flavor.
     *
     * <p>For example — Luke picks up the Imperial schematics instead:</p>
     * <pre>
     *   Luke: "Fine, OpenLDAP — hand me those specs."
     *   Table clears. OpenLDAP schemas fill in. No ApacheDS items remain.
     * </pre>
     */
    private void resetTableViewerWithCoreSchemasFromOpenLdap()
    {
        coreSchemasTableViewer.setAllChecked( false );
        coreSchemasTableViewer.setInput( coreSchemasFromOpenLdap );
    }


    // ── R2-D2 PRE-LOADS LUKE'S CHECKLIST: SETTING CHECKED SCHEMAS ───────────────
    // R2 beeps and announces the items already packed — Luke does not have to tick
    // them manually. We push a programmatic set of checked schema names into the
    // viewer so the UI reflects an externally-restored state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically checks a specific set of schema names in the table, replacing
     * whatever the user had checked. Use this to restore a saved wizard-page state —
     * for example when the user clicks "Back" and returns to this page.
     *
     * <p>For example — R2-D2 pre-loads the mission list:</p>
     * <pre>
     *   R2 beeps: "core, cosine, inetorgperson — already marked."
     *   Those three appear checked in the table without user interaction.
     * </pre>
     *
     * @param checkedCoreSchemas  names of schemas to check; items not present in the
     *                            current table input are silently ignored
     */
    public void setCheckedCoreSchemas( String[] checkedCoreSchemas )
    {
        coreSchemasTableViewer.setCheckedElements( checkedCoreSchemas );
    }


    // ── MARKING THE UNCERTAIN ITEMS: SETTING GRAYED SCHEMAS ─────────────────────
    // Luke marks some items with a question mark — in the bag but not really committed
    // to. Grayed schemas signal "auto-selected as a dependency, not by user choice."
    // Callers set this alongside setCheckedCoreSchemas to restore full state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Programmatically marks a set of schemas as grayed (indeterminate) in the table.
     * Grayed schemas appear visually distinct to show they were not deliberately
     * chosen by the user. Pair this with {@link #setCheckedCoreSchemas} when
     * restoring a previously saved selection state.
     *
     * <p>For example — Luke marks uncertain items:</p>
     * <pre>
     *   "collective" got checked automatically — Luke marks it gray: "not sure."
     *   setGrayedCoreSchemas(new String[]{"collective"}) renders it indeterminate.
     * </pre>
     *
     * @param grayedCoreSchemas  names of schemas to display in the grayed state
     */
    public void setGrayedCoreSchemas( String[] grayedCoreSchemas )
    {
        coreSchemasTableViewer.setGrayedElements( grayedCoreSchemas );
    }
}
