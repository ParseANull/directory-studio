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
package org.apache.directory.studio.apacheds.configuration.editor;


import org.apache.directory.server.config.beans.AuthenticationInterceptorBean;
import org.apache.directory.server.config.beans.InterceptorBean;
import org.apache.directory.server.config.beans.PasswordPolicyBean;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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


// ── CLASS: PasswordPoliciesMasterDetailsBlock — THE IMPERIAL SECURITY REGISTRY ──────────
// Vader strides into the Death Star's security hub, where the left wall lists every active
// clearance policy and the right panel shows the full detail for whichever one he selects.
// That split-screen command center is exactly what this class renders: master list on the
// left, editable detail panel on the right.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * The master/details block that drives the Password Policies page of the ApacheDS
 * configuration editor.
 * It wires together a scrollable list of all known password policies (the master side) and
 * the full settings panel for whichever policy is currently selected (the details side).
 * Think of this class as the Imperial Security Registry — the left column lists every
 * clearance rule in the Empire; tap one and the right column opens its full dossier for
 * editing.
 *
 * <pre>
 * +------------------------------------------------------------------------------------+
 * | .----------------------------------. .-------------------------------------------. |
 * | | All password Policies            | | Password Policy Details                   | |
 * | +----------------------------------+ +-------------------------------------------+ |
 * | | +---------------------+          | | Set the properties of the password Policy | |
 * | | | Default (enabled)   | [ Add  ] | |  [X] Enabled                              | |
 * | | |                     | [Delete] | |  ID :          [//////////]               | |
 * | | |                     |          | |  Description : [////////////////////////] | |
 * | | |                     |          | |  Attribute   : [////////////////////////] | |
 * | | |                     |          | .-------------------------------------------. |
 * | | |                     |          | | Quality / Expiration / Options / Lockout  | |
 * | | +---------------------+          | |   ... (see PasswordPolicyDetailsPage)     | |
 * | +----------------------------------+ +-------------------------------------------+ |
 * +------------------------------------------------------------------------------------+
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordPoliciesMasterDetailsBlock extends MasterDetailsBlock
{
    private static final String NEW_ID = Messages.getString( "PasswordPoliciesMasterDetailsBlock.PasswordPolicyNewId" ); //$NON-NLS-1$

    private static final String AUTHENTICATION_INTERCEPTOR_ID = "authenticationInterceptor";

    /** The associated page */
    private PasswordPoliciesPage page;

    /** The Details Page */
    private PasswordPolicyDetailsPage detailsPage;

    // UI Fields
    private TableViewer viewer;
    private Button addButton;
    private Button deleteButton;


    // ── Assembling The Imperial Command Center ────────────────────────────────────────────
    // An Imperial officer walks into the new security hub and plugs it into the main console.
    // She hands it a reference to the Password Policies page so it can reach shared state.
    // We do the same: store the parent page so later methods can ask it for config data.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a new PasswordPoliciesMasterDetailsBlock and ties it to its parent page.
     * We need the page reference to reach the editor's config bean and managed form later.
     *
     * <p>For example — an Imperial officer initialises a new registry station:</p>
     * <pre>
     *   Officer boots the terminal and hands it the control room's master key.
     *   From that moment the terminal knows where to send every status update.
     * </pre>
     *
     * @param page  the PasswordPoliciesPage that hosts this block
     */
    public PasswordPoliciesMasterDetailsBlock( PasswordPoliciesPage page )
    {
        this.page = page;
    }


    // ── Splitting The Command Screen ──────────────────────────────────────────────────────
    // The Death Star's main display divides into two panes: the roster on the left takes up
    // 40% of the screen, the detail briefing on the right takes the remaining 60%.
    // We call super to lay out the sash form, then set those exact proportions.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Lays out the master/details split and sets the sash weights so the list gets 40% of
     * the width and the detail panel gets 60%.
     * Delegates the heavy lifting to the parent {@link MasterDetailsBlock}, then tweaks the
     * proportions to give the detail panel more breathing room.
     *
     * <p>For example — the Death Star ops team sizes their split-screen display:</p>
     * <pre>
     *   The roster on the left shrinks to show just names.
     *   The briefing panel on the right expands to fit all the policy settings.
     * </pre>
     *
     * @param managedForm  the Eclipse managed form that owns this block's widgets
     */
    public void createContent( IManagedForm managedForm )
    {
        super.createContent( managedForm );

        this.sashForm.setWeights( new int[]
            { 40, 60 } );
    }


    // ── Building The Master Policy Roster ────────────────────────────────────────────────
    // An Imperial archivist sets up the left-hand roster board in the security hub.
    // She nails up a scrollable list of every clearance policy, then pins an Add and
    // Delete button next to it so Vader can manage entries on the fly.
    // We do the same: build the section, table viewer, and buttons, then wire them up.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the master (left-hand) panel: a titled section containing a sorted table
     * of all password policies plus Add and Delete action buttons.
     * This is the list side of the master/details split — clicking an entry here drives
     * what appears in the detail panel on the right.
     *
     * <p>For example — the Imperial archivist assembles the clearance registry board:</p>
     * <pre>
     *   She mounts a scrollable roster, each row labelled "PolicyName (enabled/disabled)".
     *   An Add button lets commanders enrol new rules; Delete removes obsolete ones.
     *   Selecting a row fires a selection event so the detail briefing refreshes.
     * </pre>
     *
     * @param managedForm  the form that coordinates selection events between master and detail
     * @param parent       the SWT composite to build inside
     */
    protected void createMasterPart( final IManagedForm managedForm, Composite parent )
    {
        FormToolkit toolkit = managedForm.getToolkit();

        // Creating the Section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.setText( Messages.getString( "PasswordPoliciesMasterDetailsBlock.AllPasswordPolicies" ) ); //$NON-NLS-1$
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
        Table table = toolkit.createTable( client, SWT.NULL );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 2 );
        gd.heightHint = 20;
        gd.widthHint = 100;
        table.setLayoutData( gd );
        final SectionPart spart = new SectionPart( section );
        managedForm.addPart( spart );
        viewer = new TableViewer( table );
        viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                managedForm.fireSelectionChanged( spart, event.getSelection() );
            }
        } );
        viewer.setContentProvider( new ArrayContentProvider() );
        viewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof PasswordPolicyBean )
                {
                    PasswordPolicyBean passwordPolicy = ( PasswordPolicyBean ) element;

                    if ( passwordPolicy.isEnabled() )
                    {
                        return NLS.bind( "{0} (enabled)", passwordPolicy.getPwdId() );
                    }
                    else
                    {
                        return NLS.bind( "{0} (disabled)", passwordPolicy.getPwdId() );
                    }
                }

                return super.getText( element );
            }


            public Image getImage( Object element )
            {
                if ( element instanceof PasswordPolicyBean )
                {
                    PasswordPolicyBean passwordPolicy = ( PasswordPolicyBean ) element;

                    if ( PasswordPoliciesPage.isDefaultPasswordPolicy( passwordPolicy ) )
                    {
                        return ApacheDS2ConfigurationPlugin.getDefault().getImage(
                            ApacheDS2ConfigurationPluginConstants.IMG_PASSWORD_POLICY_DEFAULT );
                    }
                    else
                    {
                        return ApacheDS2ConfigurationPlugin.getDefault().getImage(
                            ApacheDS2ConfigurationPluginConstants.IMG_PASSWORD_POLICY );
                    }
                }

                return super.getImage( element );
            }
        } );

        viewer.setComparator( new ViewerComparator()
        {
            public int compare( Viewer viewer, Object e1, Object e2 )
            {
                if ( ( e1 instanceof PasswordPolicyBean ) && ( e2 instanceof PasswordPolicyBean ) )
                {
                    PasswordPolicyBean passwordPolicy1 = ( PasswordPolicyBean ) e1;
                    PasswordPolicyBean passwordPolicy2 = ( PasswordPolicyBean ) e2;

                    String passwordPolicy1Id = passwordPolicy1.getPwdId();
                    String passwordPolicy2Id = passwordPolicy2.getPwdId();

                    if ( ( passwordPolicy1Id != null ) && ( passwordPolicy2Id != null ) )
                    {
                        return passwordPolicy1Id.compareTo( passwordPolicy2Id );
                    }
                }

                return super.compare( viewer, e1, e2 );
            }
        } );

        // Creating the button(s)
        addButton = toolkit.createButton( client,
            Messages.getString( "PasswordPoliciesMasterDetailsBlock.Add" ), SWT.PUSH ); //$NON-NLS-1$
        addButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        deleteButton = toolkit.createButton( client,
            Messages.getString( "PasswordPoliciesMasterDetailsBlock.Delete" ), SWT.PUSH ); //$NON-NLS-1$
        deleteButton.setEnabled( false );
        deleteButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        initFromInput();
        addListeners();
    }


    // ── Loading The Security Dossiers ─────────────────────────────────────────────────────
    // An Imperial clerk walks to the filing room, grabs every existing clearance dossier,
    // and stacks them on the registry board for the officers to review.
    // We ask the authentication interceptor for its list of password policies and hand
    // that list to the table viewer as its input.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the table viewer from the current editor input by pulling the list of
     * password policies out of the authentication interceptor.
     * If the interceptor is missing (unusual config), we hand the viewer a null input so
     * it shows an empty list rather than crashing.
     *
     * <p>For example — the Imperial clerk loads the registry from the filing room:</p>
     * <pre>
     *   She locates the authentication interceptor's file cabinet.
     *   She drops all the clearance dossiers onto the roster board.
     *   If the cabinet is locked or missing, the board just stays empty.
     * </pre>
     */
    private void initFromInput()
    {
        AuthenticationInterceptorBean authenticationInterceptor = getAuthenticationInterceptor();

        if ( authenticationInterceptor != null )
        {
            viewer.setInput( authenticationInterceptor.getPasswordPolicies() );
        }
        else
        {
            viewer.setInput( null );
        }
    }


    // ── Refreshing The Registry Display ──────────────────────────────────────────────────
    // After a change is committed, the Imperial archivist re-reads the filing cabinet and
    // redraws every row on the roster board so nothing looks stale.
    // We call initFromInput to reload the data model, then tell the viewer to repaint.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the policy list from the config model and forces the table to repaint.
     * Call this after external changes (e.g. loading a new config file) that the viewer
     * wouldn't otherwise know about.
     *
     * <p>For example — the archivist refreshes the command board after a briefing update:</p>
     * <pre>
     *   She re-reads the filing cabinet to pick up any new orders.
     *   The roster board redraws so every officer sees the latest picture.
     * </pre>
     */
    public void refreshUI()
    {
        initFromInput();
        viewer.refresh();
    }


    // ── Finding The Authentication Interceptor ────────────────────────────────────────────
    // An Imperial agent scans the interceptor chain looking for the one guard post labelled
    // "authenticationInterceptor" — the checkpoint that enforces every password rule.
    // We walk the directory service's interceptor list and return that bean, or null if
    // it is somehow missing from the chain.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Locates the {@link AuthenticationInterceptorBean} in the directory service's
     * interceptor chain by matching its well-known ID.
     * We need this bean because it is the one that owns the list of password policies.
     *
     * <p>For example — an Imperial agent hunts for the authentication checkpoint:</p>
     * <pre>
     *   She walks the entire guard-post chain, checking each post's ID badge.
     *   When she finds the badge reading "authenticationInterceptor" she returns it.
     *   If no post wears that badge, she reports null — something is misconfigured.
     * </pre>
     *
     * @return  the authentication interceptor bean, or {@code null} if not found
     */
    private AuthenticationInterceptorBean getAuthenticationInterceptor()
    {
        // Looking for the authentication interceptor
        for ( InterceptorBean interceptor : page.getConfigBean().getDirectoryServiceBean().getInterceptors() )
        {
            if ( AUTHENTICATION_INTERCEPTOR_ID.equalsIgnoreCase( interceptor.getInterceptorId() )
                && ( interceptor instanceof AuthenticationInterceptorBean ) )
            {
                return ( AuthenticationInterceptorBean ) interceptor;
            }
        }

        return null;
    }


    // ── Arming The Registry Controls ─────────────────────────────────────────────────────
    // An Imperial technician wires up the buttons and selection triggers on the registry
    // board so every interaction fires the right response — selection enables Delete,
    // clicking Add creates a new policy, clicking Delete removes the selected one.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches event listeners to the table viewer and the Add/Delete buttons.
     * The viewer selection listener keeps the Delete button in sync with the current
     * selection (disabled for the default policy, enabled for custom ones).
     *
     * <p>For example — an Imperial technician hard-wires the registry's control panel:</p>
     * <pre>
     *   Selecting a row lights up the Delete button — unless it's the Emperor's default.
     *   Pressing Add triggers policy creation; pressing Delete triggers policy removal.
     *   Every action routes through the appropriate handler method below.
     * </pre>
     */
    private void addListeners()
    {
        viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                viewer.refresh();

                // Getting the selection of the table viewer
                StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();

                // Delete button is enabled when something is selected
                deleteButton.setEnabled( !selection.isEmpty() );

                // Delete button is not enabled in the case of the system partition
                if ( !selection.isEmpty() )
                {
                    PasswordPolicyBean passwordPolicy = ( PasswordPolicyBean ) selection.getFirstElement();
                    if ( PasswordPoliciesPage.isDefaultPasswordPolicy( passwordPolicy ) )
                    {
                        deleteButton.setEnabled( false );
                    }
                }
            }
        } );

        addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                addNewPasswordPolicy();
            }
        } );

        deleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                deleteSelectedPasswordPolicy();
            }
        } );
    }


    // ── Enrolling A New Clearance Rule ────────────────────────────────────────────────────
    // A junior Imperial officer drafts a brand-new access clearance policy on a fresh form,
    // fills in sensible default values, and files it into the authentication interceptor's
    // cabinet — then selects the new row so the detail panel opens immediately.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link PasswordPolicyBean} with safe defaults, registers it with the
     * authentication interceptor, refreshes the viewer, and selects the new row.
     * We auto-select so the user lands straight in the detail panel ready to customise.
     *
     * <p>For example — an Imperial officer files a new clearance rule:</p>
     * <pre>
     *   She stamps a fresh form with a unique ID like "newPasswordPolicy1".
     *   She fills in defaults: lockout on, 5 max failures, 5-char minimum length.
     *   The rule goes into the registry and she highlights it so it's ready to edit.
     * </pre>
     */
    private void addNewPasswordPolicy()
    {
        // Getting a new ID for the password policy
        String newId = getNewId();

        // Creating and configuring the new password policy
        PasswordPolicyBean newPasswordPolicy = new PasswordPolicyBean();
        newPasswordPolicy.setPwdId( newId );
        newPasswordPolicy.setPwdMaxAge( 0 );
        newPasswordPolicy.setPwdFailureCountInterval( 30 );
        newPasswordPolicy.setPwdAttribute( "userPassword" );
        newPasswordPolicy.setPwdMaxFailure( 5 );
        newPasswordPolicy.setPwdLockout( true );
        newPasswordPolicy.setPwdMustChange( false );
        newPasswordPolicy.setPwdLockoutDuration( 0 );
        newPasswordPolicy.setPwdMinLength( 5 );
        newPasswordPolicy.setPwdInHistory( 5 );
        newPasswordPolicy.setPwdExpireWarning( 600 );
        newPasswordPolicy.setPwdMinAge( 0 );
        newPasswordPolicy.setPwdAllowUserChange( true );
        newPasswordPolicy.setPwdGraceAuthNLimit( 5 );
        newPasswordPolicy.setPwdCheckQuality( 1 );
        newPasswordPolicy.setPwdMaxLength( 0 );
        newPasswordPolicy.setPwdGraceExpire( 0 );
        newPasswordPolicy.setPwdMinDelay( 0 );
        newPasswordPolicy.setPwdMaxDelay( 0 );
        newPasswordPolicy.setPwdMaxIdle( 0 );

        // Adding the new password policy to the authentication interceptor
        getAuthenticationInterceptor().addPasswordPolicies( newPasswordPolicy );

        // Updating the UI and editor
        viewer.refresh();
        viewer.setSelection( new StructuredSelection( newPasswordPolicy ) );
        setEditorDirty();
    }


    // ── Minting A Unique Policy ID ────────────────────────────────────────────────────────
    // The Imperial naming bureau counts up from 1 until it finds an ID that no existing
    // policy has claimed — "newPasswordPolicy1", then "newPasswordPolicy2", and so on.
    // We loop through the existing policies and increment the counter until we get a
    // collision-free name.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Generates a unique ID for a new password policy by appending an incrementing counter
     * to the base name until we find one that doesn't clash with any existing policy.
     * This keeps IDs predictable ("newPasswordPolicy1", "newPasswordPolicy2", ...) and
     * guarantees we never silently overwrite an existing entry.
     *
     * <p>For example — the Imperial naming bureau stamps a fresh clearance badge:</p>
     * <pre>
     *   She checks "newPasswordPolicy1" — already taken by another rule.
     *   She tries "newPasswordPolicy2" — the registry confirms it's free.
     *   That becomes the new policy's ID.
     * </pre>
     *
     * @return  a unique ID string that no existing password policy is currently using
     */
    private String getNewId()
    {
        int counter = 1;
        String name = NEW_ID;
        boolean ok = false;

        while ( !ok )
        {
            ok = true;
            name = NEW_ID + counter;

            for ( PasswordPolicyBean passwordPolicy : getAuthenticationInterceptor().getPasswordPolicies() )
            {
                if ( passwordPolicy.getPwdId().equalsIgnoreCase( name ) )
                {
                    ok = false;
                }
            }
            counter++;
        }

        return name;
    }


    // ── Purging A Clearance Rule From The Registry ────────────────────────────────────────
    // An Imperial security director selects a non-default policy on the registry board and
    // presses Delete — a confirmation dialog pops up to make sure it wasn't an accident.
    // If confirmed, we remove the policy from the interceptor and mark the editor dirty.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected password policy after prompting the user to confirm.
     * We guard against deleting the default policy (it cannot be removed) and against
     * empty selections to avoid a confusing no-op.
     *
     * <p>For example — the security director revokes an outdated clearance rule:</p>
     * <pre>
     *   She selects "oldPolicy" on the roster and hits Delete.
     *   A dialog asks: "Are you sure you want to delete 'oldPolicy'?"
     *   On confirmation, the policy vanishes from the interceptor and the editor turns dirty.
     * </pre>
     */
    private void deleteSelectedPasswordPolicy()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( !selection.isEmpty() )
        {
            PasswordPolicyBean passwordPolicy = ( PasswordPolicyBean ) selection.getFirstElement();
            if ( !PasswordPoliciesPage.isDefaultPasswordPolicy( passwordPolicy ) )
            {
                if ( MessageDialog
                    .openConfirm(
                        page.getManagedForm().getForm().getShell(),
                        Messages.getString( "PasswordPoliciesMasterDetailsBlock.ConfirmDelete" ), //$NON-NLS-1$
                        NLS.bind(
                            Messages.getString( "PasswordPoliciesMasterDetailsBlock.AreYouSureDeletePasswordPolicy" ), passwordPolicy.getPwdId() ) ) ) //$NON-NLS-1$
                {
                    getAuthenticationInterceptor().removePasswordPolicies( passwordPolicy );
                    setEditorDirty();
                }
            }
        }
    }


    // ── Hooking Up The Detail Briefing Panel ──────────────────────────────────────────────
    // The Death Star's security hub installs the detail briefing screen and tells the
    // framework: "whenever a PasswordPolicyBean is selected, show it on this panel."
    // We create a PasswordPolicyDetailsPage and register it for that class.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers {@link PasswordPolicyDetailsPage} as the details renderer for
     * {@link PasswordPolicyBean} objects.
     * Eclipse's MasterDetailsBlock calls this once during setup; from then on, selecting a
     * policy in the master list automatically loads its data into the details page.
     *
     * <p>For example — the hub technician installs the detail briefing screen:</p>
     * <pre>
     *   She tells the framework: "PasswordPolicyBean goes to PasswordPolicyDetailsPage."
     *   From that moment, every roster selection auto-populates the right-hand panel.
     * </pre>
     *
     * @param detailsPart  the Eclipse details part that manages which page is shown
     */
    protected void registerPages( DetailsPart detailsPart )
    {
        detailsPage = new PasswordPolicyDetailsPage( this );
        detailsPart.registerPage( PasswordPolicyBean.class, detailsPage );
    }


    // ── Skipping The Toolbar This Time ────────────────────────────────────────────────────
    // The Imperial ops team checked the spec and decided the registry board needs no toolbar.
    // This override exists because the framework requires it, but there is nothing to do.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Intentional no-op: this block does not need a toolbar, so we override the method
     * and leave it empty.
     * The {@link MasterDetailsBlock} contract demands we provide this method even when we
     * have nothing to add.
     *
     * @param managedForm  the form that would host any toolbar actions (unused here)
     */
    protected void createToolBarActions( IManagedForm managedForm )
    {
        // No toolbar needed.
    }


    // ── Flagging The Config File As Modified ──────────────────────────────────────────────
    // After any change to the registry, an Imperial dispatcher sends a "MODIFIED" signal
    // to the main editor console so it knows the config file has unsaved changes.
    // We flip the editor's dirty flag and refresh the viewer so the UI stays consistent.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent {@link ServerConfigurationEditor} as dirty (unsaved changes) and
     * triggers a viewer refresh so labels update immediately.
     * Any mutation to the policy list — add, delete, or field edit — should call this so
     * the editor's save button lights up.
     *
     * <p>For example — the Imperial dispatcher signals an unsaved change:</p>
     * <pre>
     *   She presses the "MODIFIED" button on the console after a policy is updated.
     *   The save indicator lights up, reminding officers to commit before shutdown.
     * </pre>
     */
    public void setEditorDirty()
    {
        ( ( ServerConfigurationEditor ) page.getEditor() ).setDirty( true );
        viewer.refresh();
    }


    // ── Committing The Registry To Disk ──────────────────────────────────────────────────
    // When the Empire issues the save order, the archivist tells the detail page to flush
    // whatever is on screen back into the in-memory model so nothing is lost.
    // We delegate to detailsPage.commit(true) which does the heavy lifting.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current state of the details page into the underlying config model.
     * Called by the editor's save flow to make sure whatever is showing in the detail panel
     * gets persisted alongside the rest of the configuration.
     *
     * <p>For example — the archivist files the open briefing document before shutdown:</p>
     * <pre>
     *   She takes whatever the officer was editing in the right-hand panel.
     *   She stamps it "COMMITTED" and pushes it back into the in-memory model.
     * </pre>
     */
    public void save()
    {
        detailsPage.commit( true );
    }
}
