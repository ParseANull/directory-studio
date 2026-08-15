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
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.model.ProjectType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// ── CLASS: NewProjectWizardInformationPage — LEIA'S HOLOGRAM BRIEFING ─────────
// Princess Leia records her hologram message for Obi-Wan: she names herself,
// states her mission, and chooses which channel to broadcast on — in one concise
// briefing she conveys who she is and what kind of help she needs.
// This page does exactly that for a new project: the user types the project's
// name and picks whether it's an online (live LDAP connection) or offline (local
// schema files) project, giving us everything we need to route the rest of the
// wizard.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * First wizard page in the new-project flow — collects the project name and type.
 * The project name is the human-readable label that appears in the Schema Projects
 * view; the type determines whether we'll talk to a live LDAP server (online) or
 * work with local schema files (offline).
 * Think of this page as Leia's hologram: short, direct, and it tells us exactly
 * what we need to know to plot the next step.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewProjectWizardInformationPage extends AbstractWizardPage
{
    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;

    // UI Fields
    private Text nameText;
    private Button typeOnlineRadio;
    private Button typeOfflineRadio;


    // ── Leia Activates Her Hologram Recorder ─────────────────────────────────
    // Leia presses the activation button on R2-D2's hologram projector, setting
    // her title, her description of the mission, and readying the recorder for
    // her message — but she hasn't spoken yet.
    // We similarly set the page's title, description, and image, and grab the
    // ProjectsHandler so we can check name uniqueness when the user types.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of this information wizard page.
     * Sets the page's title, description, and icon, and looks up the
     * {@link ProjectsHandler} so we can validate that the project name
     * the user types isn't already in use.
     *
     * <p>For example — Leia readies the recorder with the mission header:</p>
     * <pre>
     *   setTitle( "Create Schema Project" );
     *   setDescription( "Specify a name and a type for this project." );
     *   projectsHandler = Activator.getDefault().getProjectsHandler();
     * </pre>
     */
    protected NewProjectWizardInformationPage()
    {
        super( "NewProjectWizardInformationPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "NewProjectWizardInformationPage.CreateSchemaProject" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "NewProjectWizardInformationPage.SpecifiyNameAndType" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_PROJECT_NEW_WIZARD ) );
        projectsHandler = Activator.getDefault().getProjectsHandler();
    }


    // ── Leia Delivers Her Hologram Message ───────────────────────────────────
    // Leia's hologram flickers to life: a name field glows in the projection —
    // "Help me, Obi-Wan" — and two options appear: "Send via droid" or "Send
    // via courier."  The droid route is pre-selected as the safer default.
    // We build those same controls here: a text field for the name and radio
    // buttons for online vs. offline (offline is the safer default because it
    // doesn't require a live server connection).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the SWT widgets for this page: a name text field and, if any
     * schema connectors are registered, radio buttons to choose online vs. offline.
     * If no schema connectors are available (no plugins installed that can talk
     * to a live server), we skip the radio buttons entirely — offline is the
     * only option.
     *
     * <p>For example — Leia's hologram displays her name and two mission options:</p>
     * <pre>
     *   nameText        → "Project name: ________________"
     *   typeOfflineRadio → ( ) Offline Schema   ← pre-selected
     *   typeOnlineRadio  → ( ) Online Schema
     * </pre>
     *
     * @param parent  the SWT container Eclipse provides for our widgets
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout( 2, false );
        composite.setLayout( layout );

        // Name
        Label nameLabel = new Label( composite, SWT.NONE );
        nameLabel.setText( Messages.getString( "NewProjectWizardInformationPage.ProjectName" ) ); //$NON-NLS-1$
        nameText = new Text( composite, SWT.BORDER );
        nameText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        if ( PluginUtils.getSchemaConnectors().size() > 0 )
        {
            // Type Group
            Group typeGroup = new Group( composite, SWT.NONE );
            typeGroup.setText( Messages.getString( "NewProjectWizardInformationPage.Type" ) ); //$NON-NLS-1$
            typeGroup.setLayout( new GridLayout() );
            typeGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

            typeOfflineRadio = new Button( typeGroup, SWT.RADIO );
            typeOfflineRadio.setText( Messages.getString( "NewProjectWizardInformationPage.OfflineSchema" ) ); //$NON-NLS-1$
            typeOfflineRadio.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

            typeOnlineRadio = new Button( typeGroup, SWT.RADIO );
            typeOnlineRadio.setText( Messages.getString( "NewProjectWizardInformationPage.OnlineSchema" ) ); //$NON-NLS-1$
            typeOnlineRadio.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        }

        initFields();
        addListeners();

        setControl( composite );
    }


    // ── The Hologram Defaults To "Droid Delivery" ────────────────────────────
    // Before Leia speaks, the delivery method defaults to "via droid" — the
    // tried-and-true offline channel.  The hologram is blank until she fills it in.
    // We pre-select the offline radio and clear any stale error so the page opens
    // clean each time the wizard is launched.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resets the page to its initial state: offline radio selected, no error
     * message shown, page marked incomplete.
     * We call this after building the widgets so the page always starts in a
     * known-good state regardless of any prior wizard run.
     *
     * <p>For example — the hologram recorder defaults to offline delivery:</p>
     * <pre>
     *   typeOfflineRadio.setSelection( true );
     *   displayErrorMessage( null );
     *   setPageComplete( false );
     * </pre>
     */
    private void initFields()
    {
        if ( typeOfflineRadio != null )
        {
            typeOfflineRadio.setSelection( true );
        }

        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── R2-D2 Watches For Changes To The Message ─────────────────────────────
    // R2-D2 stands guard next to the hologram recorder, beeping whenever Leia
    // edits her name or changes the delivery channel — triggering the recording
    // system to re-validate the message.
    // We wire up the name field's ModifyListener and both radio buttons'
    // SelectionListeners so that any UI change immediately re-validates.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches event listeners to the name field and type radio buttons.
     * Any change in either widget fires {@link #dialogChanged()}, which
     * re-validates the current input and updates the error banner and
     * page-complete state.
     *
     * <p>For example — R2-D2 monitors the hologram controls for edits:</p>
     * <pre>
     *   nameText.addModifyListener( e -> dialogChanged() );
     *   typeOfflineRadio.addSelectionListener( e -> dialogChanged() );
     *   typeOnlineRadio.addSelectionListener( e -> dialogChanged() );
     * </pre>
     */
    private void addListeners()
    {
        nameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dialogChanged();
            }
        } );

        SelectionListener dialogChangedSelectionListener = new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                dialogChanged();
            }
        };

        typeOfflineRadio.addSelectionListener( dialogChangedSelectionListener );
        typeOnlineRadio.addSelectionListener( dialogChangedSelectionListener );
    }


    // ── Leia Checks Her Message Before Sending ───────────────────────────────
    // Before committing the hologram to R2-D2's memory, Leia reviews it: is the
    // name field filled in?  Is this name already registered with the Alliance?
    // If anything looks wrong, the recorder flashes a warning.
    // We do the same: check that the name field isn't empty and that no existing
    // project already has that name, then update the error banner accordingly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current field values and updates the error banner plus the
     * page-complete status.  Called any time the user edits the name or changes
     * the project type.
     * We check that the name isn't blank and that it isn't already taken by
     * another project — both would prevent the wizard from proceeding.
     *
     * <p>For example — Leia reviews her hologram before transmitting:</p>
     * <pre>
     *   if ( name.isEmpty() )              error( "No name specified!" );
     *   if ( projectAlreadyExists( name ) ) error( "That project already exists!" );
     *   else                               clearError();
     * </pre>
     */
    private void dialogChanged()
    {
        // Name
        if ( nameText.getText().equals( "" ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "NewProjectWizardInformationPage.ErrorNoNameSpecified" ) ); //$NON-NLS-1$
            return;
        }
        else if ( projectsHandler.isProjectNameAlreadyTaken( nameText.getText() ) )
        {
            displayErrorMessage( Messages.getString( "NewProjectWizardInformationPage.ErrorProjectNameExists" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── Leia Reads The Project Name Aloud ────────────────────────────────────
    // Obi-Wan asks Leia what the mission is called; she reads the project name
    // clearly off the hologram so he can write it in his mission log.
    // We simply return whatever the user typed in the name field.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the project name the user typed into the name field.
     * The wizard's {@code performFinish()} uses this to name the new project.
     *
     * <p>For example — Leia states the mission name for the record:</p>
     * <pre>
     *   String missionName = informationPage.getProjectName();
     *   // "Operation: Tatooine Schema"
     * </pre>
     *
     * @return the project name string; never {@code null}, but may be empty if
     *         the user hasn't typed anything yet (validation prevents that in practice)
     */
    public String getProjectName()
    {
        return nameText.getText();
    }


    // ── Leia Declares Online Or Offline Channel ──────────────────────────────
    // Leia specifies whether she's broadcasting live via the HoloNet (online)
    // or pre-recording for physical delivery by R2-D2 (offline) — the channel
    // determines what the Alliance does next.
    // We translate the radio-button state into a typed {@link ProjectType}.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the project type — {@link ProjectType#ONLINE} or {@link ProjectType#OFFLINE}
     * — based on which radio button the user selected.
     * If no schema connectors are registered the radio buttons aren't shown
     * and we always return {@code OFFLINE} as the safe default.
     *
     * <p>For example — Leia specifies her broadcast channel:</p>
     * <pre>
     *   if ( typeOnlineRadio.getSelection() )  return ProjectType.ONLINE;
     *   else                                   return ProjectType.OFFLINE;
     * </pre>
     *
     * @return the selected {@link ProjectType}; defaults to {@code OFFLINE} if
     *         the radio buttons aren't present
     */
    public ProjectType getProjectType()
    {
        if ( typeOnlineRadio != null )
        {
            if ( typeOnlineRadio.getSelection() )
            {
                return ProjectType.ONLINE;
            }
            else
            {
                return ProjectType.OFFLINE;
            }
        }

        // Default
        return ProjectType.OFFLINE;
    }
}
