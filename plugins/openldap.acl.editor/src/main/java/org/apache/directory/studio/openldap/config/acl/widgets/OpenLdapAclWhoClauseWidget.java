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


import java.text.MessageFormat;

import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPlugin;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclEditorPluginConstants;
import org.apache.directory.studio.openldap.config.acl.OpenLdapAclValueWithContext;
import org.apache.directory.studio.openldap.config.acl.dialogs.OpenLdapAccessLevelDialog;
import org.apache.directory.studio.openldap.config.acl.model.AclAccessLevel;
import org.apache.directory.studio.openldap.config.acl.model.AclAccessLevelLevelEnum;
import org.apache.directory.studio.openldap.config.acl.model.AclControlEnum;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClause;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseAnonymous;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseDn;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseDnAttr;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseEnum;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseGroup;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseSaslSsf;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseSelf;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseStar;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseTlsSsf;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseTransportSsf;
import org.apache.directory.studio.openldap.config.acl.model.AclWhoClauseUsers;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhoClauseDnAttributeComposite;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhoClauseDnComposite;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhoClauseGroupComposite;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhoClauseSaslSsfComposite;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhoClauseSsfComposite;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhoClauseTlsSsfComposite;
import org.apache.directory.studio.openldap.config.acl.widgets.composites.WhoClauseTransportSsfComposite;


// ── CLASS: OpenLdapAclWhoClauseWidget — GRAND MOFF FILLING A SINGLE WHO ROW ─
// Grand Moff Tarkin fills in one row of the security manifest: which subject
// class (clause combo), what access level they receive, and how to control
// the evaluation chain afterward. This widget renders that single row as three
// combo viewers (clause, access level, control) plus a toolbar with Add/Delete/
// Move-Up/Move-Down buttons. When the clause type changes a configuration sub-
// composite (e.g. WhoClauseDnComposite) is dynamically created or disposed below
// the top row. All changes are propagated back to the builder widget via
// notifyListeners() so the model stays in sync.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A single WHO-clause row in the visual ACL editor. Renders three combo viewers
 * (clause type, access level, control) and a toolbar (Add, Delete, Move Up,
 * Move Down). When a clause type that requires configuration (DN, DN-in-attribute,
 * Group, SASL-SSF, SSF, TLS-SSF, Transport-SSF) is selected a sub-composite is
 * created below the top row.
 *
 * <p>Think of this class as Grand Moff Tarkin assigning a single row of the
 * security directive — subject class, clearance level, and continuation control.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenLdapAclWhoClauseWidget extends AbstractWidget implements SelectionListener
{
    /** The ACL context */
    private OpenLdapAclValueWithContext context;

    /** The array of clauses */
    private Object[] clauses = new Object[]
        {
            new ClauseComboViewerName(),
            AclWhoClauseEnum.STAR,
            AclWhoClauseEnum.ANONYMOUS,
            AclWhoClauseEnum.USERS,
            AclWhoClauseEnum.SELF,
            AclWhoClauseEnum.DN,
            AclWhoClauseEnum.DNATTR,
            AclWhoClauseEnum.GROUP,
            AclWhoClauseEnum.SASL_SSF,
            AclWhoClauseEnum.SSF,
            AclWhoClauseEnum.TLS_SSF,
            AclWhoClauseEnum.TRANSPORT_SSF
    };

    /** The array of access levels */
    private Object[] accessLevels = new Object[]
        {
            new AccessLevelComboViewerName(),
            AclAccessLevelLevelEnum.MANAGE,
            AclAccessLevelLevelEnum.WRITE,
            AclAccessLevelLevelEnum.READ,
            AclAccessLevelLevelEnum.SEARCH,
            AclAccessLevelLevelEnum.COMPARE,
            AclAccessLevelLevelEnum.AUTH,
            AclAccessLevelLevelEnum.DISCLOSE,
            AclAccessLevelLevelEnum.NONE,
            new AccessLevelComboViewerCustom()
    };

    /** The array of controls */
    private Object[] controls = new Object[]
        {
            new ControlComboViewerName(),
            AclControlEnum.STOP,
            AclControlEnum.CONTINUE,
            AclControlEnum.BREAK
    };

    /** The parent builder widget */
    private OpenLdapAclWhoClausesBuilderWidget builderWidget;

    /** The row index */
    private int index;

    /** The clause */
    private AclWhoClause clause;

    /** The current clause selection */
    private Object currentClauseSelection = clauses[0];

    /** The current access level selection */
    private Object currentAccessLevelSelection = accessLevels[0];

    /** The current control selection */
    private Object currentControlSelection = controls[0];

    /** The current custom access level */
    private AclAccessLevel currentCustomAccessLevel;

    // UI Widgets
    private Composite composite;
    private Composite configurationComposite;
    private ComboViewer clauseComboViewer;
    private ComboViewer accessLevelComboViewer;
    private ComboViewer controlComboViewer;
    private ToolBar toolbar;
    private ToolItem addButton;
    private ToolItem deleteButton;
    private ToolItem moveUpButton;
    private ToolItem moveDownButton;

    // ── Listener: Clause Combo Changed ─────────────────────────────────────────
    // When Tarkin selects a new clause type the old configuration sub-composite
    // is disposed, the new clause is created in the model, and the appropriate
    // configuration UI is created below the top row.
    // ─────────────────────────────────────────────────────────────────────────
    /** Listener for the clause combo viewer. Disposes/creates the config sub-composite. */
    private ISelectionChangedListener clauseComboViewerListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            // Getting the selected clause
            Object selection = ( ( StructuredSelection ) clauseComboViewer
                .getSelection() ).getFirstElement();

            // Only changing the UI when the selection is different
            if ( currentClauseSelection != selection )
            {
                // Storing the current selection
                currentClauseSelection = selection;

                // Disposing the current composite
                if ( ( configurationComposite != null ) && ( !configurationComposite.isDisposed() ) )
                {
                    configurationComposite.dispose();
                    configurationComposite = null;
                }

                // Setting the clause from the current selection
                setClause();

                // Creating the configuration UI
                createConfigurationUI();

                // Notifying listeners
                notifyListeners();
            }
        }
    };

    // ── Listener: Access Level Combo Changed ────────────────────────────────────
    // When Tarkin picks "Custom..." the access-level dialog opens. If he cancels
    // the combo reverts to its previous value. Otherwise the custom access level
    // is stored.
    // ─────────────────────────────────────────────────────────────────────────
    /** Listener for the access level combo viewer. Opens access-level dialog for Custom. */
    private ISelectionChangedListener accessLevelComboViewerListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            // Getting the selected access level
            Object selection = ( ( StructuredSelection ) accessLevelComboViewer
                .getSelection() ).getFirstElement();

            // Special case for the 'custom' item
            if ( accessLevels[accessLevels.length - 1].equals( selection ) )
            {
                // Getting the current access level
                AclAccessLevel accessLevel = null;
                if ( clause != null )
                {
                    accessLevel = clause.getAccessLevel();
                }

                // Opening a dialog to edit the access level
                OpenLdapAccessLevelDialog accessLevelDialog = new OpenLdapAccessLevelDialog( accessLevel );
                if ( accessLevelDialog.open() == OpenLdapAccessLevelDialog.OK )
                {
                    // Getting the access level from the dialog
                    currentCustomAccessLevel = accessLevelDialog.getAccessLevel();
                }
                else
                {
                    // The dialog has been canceled

                    // Only changing the UI when the selection is different
                    if ( currentAccessLevelSelection != selection )
                    {
                        accessLevelComboViewer.removeSelectionChangedListener( accessLevelComboViewerListener );
                        accessLevelComboViewer
                            .setSelection( new StructuredSelection( currentAccessLevelSelection ) );
                        accessLevelComboViewer.addSelectionChangedListener( accessLevelComboViewerListener );
                    }

                    // We exit here
                    return;
                }
            }

            // Only changing the UI when the selection is different or we have a custom access level value to store
            if ( ( currentAccessLevelSelection != selection ) || ( currentCustomAccessLevel != null ) )
            {
                // Storing the current selection
                currentAccessLevelSelection = selection;

                // Setting the access level from the current selection
                setAccessLevel();

                // Is it a simple access level?
                AclAccessLevel accessLevel = clause.getAccessLevel();
                if ( isSimple( accessLevel ) )
                {
                    currentAccessLevelSelection = accessLevel.getLevel();
                }
                // Is it a custom access level?
                else if ( isCustom( accessLevel ) )
                {
                    currentAccessLevelSelection = accessLevels[accessLevels.length - 1];
                }
                // Bogus case
                else
                {
                    currentAccessLevelSelection = accessLevels[0];
                }

                // Setting the correct selection and refreshing the combo viewer to update the labels
                accessLevelComboViewer.removeSelectionChangedListener( accessLevelComboViewerListener );
                accessLevelComboViewer
                    .setSelection( new StructuredSelection( currentAccessLevelSelection ) );
                accessLevelComboViewer.addSelectionChangedListener( accessLevelComboViewerListener );
                accessLevelComboViewer.refresh();

                // Notifying listeners
                notifyListeners();
            }
        }
    };

    // ── Listener: Control Combo Changed ─────────────────────────────────────────
    // When Tarkin changes the control (stop/continue/break) the model is updated.
    // ─────────────────────────────────────────────────────────────────────────
    /** Listener for the control combo viewer. Updates the clause control. */
    private ISelectionChangedListener controlComboViewerListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            // Getting the selected control
            Object selection = ( ( StructuredSelection ) controlComboViewer
                .getSelection() ).getFirstElement();

            // Only changing the UI when the selection is different
            if ( currentControlSelection != selection )
            {
                // Storing the current selection
                currentControlSelection = selection;

                // Setting the control from the current selection
                setControl();

                // Notifying listeners
                notifyListeners();
            }
        }
    };


    // ── Constructing a Single Who Clause Row ─────────────────────────────────
    // Tarkin stores the builder widget (for callbacks), the initial clause,
    // its position index, and the context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new who clause row widget. The combos and toolbar are created
     * later by {@link #create(Composite)}.
     *
     * @param builderWidget  The parent builder widget (for add/delete/move callbacks).
     * @param context        The shared ACL context.
     * @param clause         The initial ACL who-clause for this row.
     * @param index          The zero-based position of this row in the who-clause list.
     */
    public OpenLdapAclWhoClauseWidget( OpenLdapAclWhoClausesBuilderWidget builderWidget,
                OpenLdapAclValueWithContext context, AclWhoClause clause, int index )
    {
        this.builderWidget = builderWidget;
        this.clause = clause;
        this.index = index;
        this.context = context;
    }


    // ── Building the Row UI ───────────────────────────────────────────────────
    // Tarkin creates the two-column layout: the left column holds the three
    // combo viewers; the right column holds the toolbar. The combos are
    // initialised from the clause, then listeners are attached.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the row UI inside the given parent: a two-column composite
     * holding the clause/access-level/control combo viewers and the
     * Add/Delete/Move-Up/Move-Down toolbar.
     *
     * @param parent  The parent composite (usually the who-group).
     */
    public void create( Composite parent )
    {
        // Creating the widget base composite
        composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Creating the top composites
        Composite topComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );
        Composite topSubComposite = BaseWidgetUtils.createColumnContainer( topComposite, 3, true, 1 );

        // Creating the clause, access level and control combo viewers
        createClauseComboViewer( topSubComposite );
        createAccessLevelComboViewer( topSubComposite );
        createControlComboViewer( topSubComposite );

        // Creating the toolbar and buttons
        createToolbarAndButtons( topComposite );

        // Initializing the UI with the clause
        initWithClause();

        // Adding the listeners to the UI widgets
        addListeners();
    }


    // ── Initialising Combos From the Current Clause ───────────────────────────
    // Tarkin pre-selects the three combos based on the clause that was passed
    // to the constructor. Falls back to the placeholder rows when clause is null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the three combo viewers from the current clause. Falls back to
     * the placeholder entries (index 0 of each array) when the clause or its
     * access level/control are null.
     */
    private void initWithClause()
    {
        if ( clause != null )
        {
            // Clause
            currentClauseSelection = AclWhoClauseEnum.get( clause );

            // Access Level
            AclAccessLevel accessLevel = clause.getAccessLevel();
            if ( accessLevel == null )
            {
                currentAccessLevelSelection = accessLevels[0];
            }
            else
            {
                // Is it a simple access level?
                if ( isSimple( accessLevel ) )
                {
                    currentAccessLevelSelection = accessLevel.getLevel();
                }
                // Is it a custom access level?
                else if ( isCustom( accessLevel ) )
                {
                    currentAccessLevelSelection = accessLevels[accessLevels.length - 1];
                }
                // Bogus case
                else
                {
                    currentAccessLevelSelection = accessLevels[0];
                }
            }

            // Control
            AclControlEnum control = clause.getControl();
            if ( control == null )
            {
                currentControlSelection = controls[0];
            }
            else
            {
                currentControlSelection = control;
            }
        }
        else
        {
            // Defaulting to the first row of the arrays
            currentClauseSelection = clauses[0];
            currentAccessLevelSelection = accessLevels[0];
            currentControlSelection = controls[0];
        }

        // Setting the selection for the combo viewers
        clauseComboViewer.setSelection( new StructuredSelection( currentClauseSelection ) );
        accessLevelComboViewer.setSelection( new StructuredSelection( currentAccessLevelSelection ) );
        controlComboViewer.setSelection( new StructuredSelection( currentControlSelection ) );
    }


    // ── Detecting a Simple Access Level ──────────────────────────────────────
    // A simple access level has a named level and no self-privilege modifier.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the access level is "simple" — i.e. it has a named
     * level (manage/write/read/…) with no self-privilege modifier.
     *
     * @param accessLevel  The access level to test; may be {@code null}.
     * @return             {@code true} if simple.
     */
    private boolean isSimple( AclAccessLevel accessLevel )
    {
        if ( accessLevel != null )
        {
            return ( !accessLevel.isSelf() ) && ( accessLevel.getLevel() != null );
        }

        return false;
    }


    // ── Detecting a Custom Access Level ──────────────────────────────────────
    // A custom access level uses self-privilege or explicit privilege flags.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the access level is "custom" — i.e. it uses the
     * self-privilege modifier or has explicit privilege flags.
     *
     * @param accessLevel  The access level to test; may be {@code null}.
     * @return             {@code true} if custom.
     */
    private boolean isCustom( AclAccessLevel accessLevel )
    {
        if ( accessLevel != null )
        {
            return ( accessLevel.isSelf() )
                || ( ( accessLevel.getPrivilegeModifier() != null ) && ( accessLevel.getPrivileges().size() > 0 ) );
        }

        return false;
    }


    // ── Creating the Clause Combo Viewer ─────────────────────────────────────
    // The clause combo lists all who-clause types with human-readable labels.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the clause type {@link ComboViewer} with a custom label provider
     * that maps each {@link AclWhoClauseEnum} to a human-readable string.
     *
     * @param parent  The parent composite.
     */
    private void createClauseComboViewer( Composite parent )
    {
        clauseComboViewer = new ComboViewer( BaseWidgetUtils.createReadonlyCombo( parent, new String[0], -1, 1 ) );
        clauseComboViewer.setContentProvider( new ArrayContentProvider() );
        clauseComboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof ClauseComboViewerName )
                {
                    return "< Clause >";
                }
                else if ( element instanceof AclWhoClauseEnum )
                {
                    AclWhoClauseEnum value = ( AclWhoClauseEnum ) element;
                    switch ( value )
                    {
                        case STAR:
                            return "Anyone (*)";
                        case ANONYMOUS:
                            return "Anonymous";
                        case USERS:
                            return "Users";
                        case SELF:
                            return "Self";
                        case DN:
                            return "DN";
                        case DNATTR:
                            return "DN in attribute";
                        case GROUP:
                            return "Group";
                        case SASL_SSF:
                            return "SASL SSF";
                        case SSF:
                            return "SSF";
                        case TLS_SSF:
                            return "TLS SSF";
                        case TRANSPORT_SSF:
                            return "Transport SSF";
                    }
                }

                return super.getText( element );
            }
        } );
        clauseComboViewer.setInput( clauses );
        clauseComboViewer.setSelection( new StructuredSelection( currentClauseSelection ) );
    }


    // ── Creating the Access Level Combo Viewer ────────────────────────────────
    // The access level combo lists named levels plus a Custom option that opens
    // the access-level dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the access level {@link ComboViewer}. Lists named levels
     * (manage…none) plus a "Custom…" option that opens
     * {@link OpenLdapAccessLevelDialog}.
     *
     * @param parent  The parent composite.
     */
    private void createAccessLevelComboViewer( Composite parent )
    {
        accessLevelComboViewer = new ComboViewer(
            BaseWidgetUtils.createReadonlyCombo( parent, new String[0], -1, 1 ) );
        accessLevelComboViewer.setContentProvider( new ArrayContentProvider() );
        accessLevelComboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof AccessLevelComboViewerName )
                {
                    return "< Access Level >";
                }
                else if ( element instanceof AccessLevelComboViewerCustom )
                {
                    if ( ( clause != null ) && ( isCustom( clause.getAccessLevel() ) ) )
                    {
                        return MessageFormat.format( "Custom... [{0}]", clause.getAccessLevel() );
                    }

                    return "Custom...";
                }
                else if ( element instanceof AclAccessLevelLevelEnum )
                {
                    AclAccessLevelLevelEnum value = ( AclAccessLevelLevelEnum ) element;
                    switch ( value )
                    {
                        case MANAGE:
                            return "Manage";
                        case WRITE:
                            return "Write";
                        case READ:
                            return "Read";
                        case SEARCH:
                            return "Search";
                        case COMPARE:
                            return "Compare";
                        case AUTH:
                            return "Auth";
                        case DISCLOSE:
                            return "Disclose";
                        case NONE:
                            return "None";
                    }
                }

                return super.getText( element );
            }
        } );
        accessLevelComboViewer.setInput( accessLevels );
        accessLevelComboViewer.setSelection( new StructuredSelection( currentAccessLevelSelection ) );
    }


    // ── Creating the Control Combo Viewer ─────────────────────────────────────
    // The control combo lists stop/continue/break options.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the control {@link ComboViewer} with Stop, Continue, and Break options.
     *
     * @param parent  The parent composite.
     */
    private void createControlComboViewer( Composite parent )
    {
        controlComboViewer = new ComboViewer(
            BaseWidgetUtils.createReadonlyCombo( parent, new String[0], -1, 1 ) );
        controlComboViewer.setContentProvider( new ArrayContentProvider() );
        controlComboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof ControlComboViewerName )
                {
                    return "< Control >";
                }
                else if ( element instanceof AclControlEnum )
                {
                    AclControlEnum value = ( AclControlEnum ) element;
                    switch ( value )
                    {
                        case STOP:
                            return "Stop";
                        case CONTINUE:
                            return "Continue";
                        case BREAK:
                            return "Break";
                    }
                }

                return super.getText( element );
            }
        } );
        controlComboViewer.setInput( controls );
        controlComboViewer.setSelection( new StructuredSelection( currentControlSelection ) );
    }


    // ── Creating the Toolbar and Buttons ──────────────────────────────────────
    // The toolbar holds four icon buttons: Add, Delete, Move Up, Move Down.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT {@link ToolBar} with Add, Delete, Move-Up, and Move-Down
     * {@link ToolItem}s, each configured with its icon and tooltip.
     *
     * @param parent  The parent composite.
     */
    private void createToolbarAndButtons( Composite parent )
    {
        // Creating the toolbar
        toolbar = new ToolBar( parent, SWT.HORIZONTAL );

        // Creating the 'Add' button
        addButton = new ToolItem( toolbar, SWT.PUSH );
        addButton.setToolTipText( "Add" );
        addButton.setImage( OpenLdapAclEditorPlugin.getDefault().getImage(
            OpenLdapAclEditorPluginConstants.IMG_ADD ) );

        // Creating the 'Delete' button
        deleteButton = new ToolItem( toolbar, SWT.PUSH );
        deleteButton.setToolTipText( "Delete" );
        deleteButton.setImage( OpenLdapAclEditorPlugin.getDefault().getImage(
            OpenLdapAclEditorPluginConstants.IMG_DELETE ) );

        // Creating the 'Move Up' button
        moveUpButton = new ToolItem( toolbar, SWT.PUSH );
        moveUpButton.setToolTipText( "Move Up" );
        moveUpButton.setImage( OpenLdapAclEditorPlugin.getDefault().getImage(
            OpenLdapAclEditorPluginConstants.IMG_UP ) );
        // Creating the 'Move Down' button
        moveDownButton = new ToolItem( toolbar, SWT.PUSH );
        moveDownButton.setToolTipText( "Move Down" );
        moveDownButton.setImage( OpenLdapAclEditorPlugin.getDefault().getImage(
            OpenLdapAclEditorPluginConstants.IMG_DOWN ) );
    }


    // ── Attaching All Event Listeners ─────────────────────────────────────────
    // Listeners for all three combo viewers and all four toolbar buttons are
    // registered here after the UI is fully constructed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the selection listeners for all three combo viewers and all four
     * toolbar buttons. Called at the end of {@link #create(Composite)}.
     */
    private void addListeners()
    {
        // Adding the selection listener for the clause combo viewer
        clauseComboViewer.addSelectionChangedListener( clauseComboViewerListener );

        // Adding the selection listener for the access level combo viewer
        accessLevelComboViewer.addSelectionChangedListener( accessLevelComboViewerListener );

        // Adding the selection listener for the control combo viewer
        controlComboViewer.addSelectionChangedListener( controlComboViewerListener );

        // Adding toolbar buttons listeners
        addButton.addSelectionListener( this );
        deleteButton.addSelectionListener( this );
        moveUpButton.addSelectionListener( this );
        moveDownButton.addSelectionListener( this );
    }


    // ── Creating the Configuration Sub-Composite ─────────────────────────────
    // When a clause type that requires additional input is selected (DN, DN-attr,
    // Group, or a crypto-strength type), a dedicated sub-composite is created
    // below the top row.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the clause-type-specific configuration sub-composite below the
     * top row. Clause types that require no input (* / Anonymous / Users / Self)
     * produce nothing; types that require input (DN, DN-in-attribute, Group,
     * SASL-SSF, SSF, TLS-SSF, Transport-SSF) produce their own composite.
     */
    private void createConfigurationUI()
    {
        if ( currentClauseSelection instanceof AclWhoClauseEnum )
        {
            AclWhoClauseEnum currentClauseSelectionValue = ( AclWhoClauseEnum ) currentClauseSelection;
            switch ( currentClauseSelectionValue )
            {
                case STAR:
                    break; // Nothing to configure
                case ANONYMOUS:
                    break; // Nothing to configure
                case USERS:
                    break; // Nothing to configure
                case SELF:
                    break; // Nothing to configure
                case DN:
                    createUIWhoClauseDn();
                    break;
                case DNATTR:
                    createUIWhoClauseDnAttr();
                    break;
                case GROUP:
                    createUIWhoClauseGroup();
                    break;
                case SASL_SSF:
                    createCompositeWhoClauseSaslSsf();
                    break;
                case SSF:
                    createCompositeWhoClauseSsf();
                    break;
                case TLS_SSF:
                    createCompositeWhoClauseTlsSsf();
                    break;
                case TRANSPORT_SSF:
                    createCompositeWhoClauseTransportSsf();
                    break;
            }
        }
    }


    // ── Creating DN Sub-Composite ─────────────────────────────────────────────
    /**
     * Creates the DN sub-composite ({@link WhoClauseDnComposite}) below the top row.
     */
    private void createUIWhoClauseDn()
    {
        configurationComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        WhoClauseDnComposite composite = new WhoClauseDnComposite( context, builderWidget.visualEditorComposite );
        composite.createComposite( configurationComposite );
    }


    // ── Creating DN-in-Attribute Sub-Composite ───────────────────────────────
    /**
     * Creates the DN-in-attribute sub-composite ({@link WhoClauseDnAttributeComposite})
     * below the top row.
     */
    private void createUIWhoClauseDnAttr()
    {
        configurationComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        WhoClauseDnAttributeComposite composite = new WhoClauseDnAttributeComposite( context, builderWidget.visualEditorComposite );
        composite.createComposite( configurationComposite );
    }


    // ── Creating Group Sub-Composite ─────────────────────────────────────────
    /**
     * Creates the Group sub-composite ({@link WhoClauseGroupComposite}) below the top row.
     */
    private void createUIWhoClauseGroup()
    {
        configurationComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        WhoClauseGroupComposite composite = new WhoClauseGroupComposite( context, builderWidget.visualEditorComposite );
        composite.createComposite( configurationComposite );
    }


    // ── Creating SASL SSF Sub-Composite ──────────────────────────────────────
    /**
     * Creates the SASL-SSF sub-composite ({@link WhoClauseSaslSsfComposite}) below the top row.
     */
    private void createCompositeWhoClauseSaslSsf()
    {
        configurationComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        WhoClauseSaslSsfComposite composite = new WhoClauseSaslSsfComposite( context, builderWidget.visualEditorComposite );
        composite.createComposite( configurationComposite );
    }


    // ── Creating SSF Sub-Composite ────────────────────────────────────────────
    /**
     * Creates the SSF sub-composite ({@link WhoClauseSsfComposite}) below the top row.
     */
    private void createCompositeWhoClauseSsf()
    {
        configurationComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        WhoClauseSsfComposite composite = new WhoClauseSsfComposite( context, builderWidget.visualEditorComposite );
        composite.createComposite( configurationComposite );
    }


    // ── Creating TLS SSF Sub-Composite ───────────────────────────────────────
    /**
     * Creates the TLS-SSF sub-composite ({@link WhoClauseTlsSsfComposite}) below the top row.
     */
    private void createCompositeWhoClauseTlsSsf()
    {
        configurationComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        WhoClauseTlsSsfComposite composite = new WhoClauseTlsSsfComposite( context, builderWidget.visualEditorComposite );
        composite.createComposite( configurationComposite );
    }


    // ── Creating Transport SSF Sub-Composite ─────────────────────────────────
    /**
     * Creates the Transport-SSF sub-composite ({@link WhoClauseTransportSsfComposite})
     * below the top row.
     */
    private void createCompositeWhoClauseTransportSsf()
    {
        configurationComposite = BaseWidgetUtils.createColumnContainer( composite, 1, 1 );

        WhoClauseTransportSsfComposite composite = new WhoClauseTransportSsfComposite(
            context, builderWidget.visualEditorComposite );
        composite.createComposite( configurationComposite );
    }


    // ── Creating a Basic (Named) Access Level ─────────────────────────────────
    /**
     * Constructs a simple {@link AclAccessLevel} with the given named level.
     *
     * @param level  The named access level.
     * @return       A new {@code AclAccessLevel} with {@code level} set.
     */
    private AclAccessLevel createBasicAccessLevel( AclAccessLevelLevelEnum level )
    {
        AclAccessLevel accessLevel = new AclAccessLevel();
        accessLevel.setLevel( level );
        return accessLevel;
    }


    // ── Setting the Clause From the Current Combo Selection ──────────────────
    // Tarkin creates the correct concrete clause class for the selected type,
    // then immediately sets the access level and control on it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the concrete {@link AclWhoClause} for the current clause combo
     * selection and stores it in {@link #clause}. Also applies the current
     * access level and control selections. Sets clause to {@code null} when
     * the placeholder row is selected.
     */
    private void setClause()
    {
        if ( currentClauseSelection instanceof AclWhoClauseEnum )
        {
            AclWhoClauseEnum clauseSelection = ( AclWhoClauseEnum ) currentClauseSelection;

            // Creating the clause associated with the selection
            switch ( clauseSelection )
            {
                case STAR:
                    clause = new AclWhoClauseStar();
                    break;
                case ANONYMOUS:
                    clause = new AclWhoClauseAnonymous();
                    break;
                case USERS:
                    clause = new AclWhoClauseUsers();
                    break;
                case SELF:
                    clause = new AclWhoClauseSelf();
                    break;
                case DN:
                    clause = new AclWhoClauseDn();
                    break;
                case DNATTR:
                    clause = new AclWhoClauseDnAttr();
                    break;
                case GROUP:
                    clause = new AclWhoClauseGroup();
                    break;
                case SASL_SSF:
                    clause = new AclWhoClauseSaslSsf();
                    break;
                case SSF:
                    clause = new AclWhoClauseSsf();
                    break;
                case TLS_SSF:
                    clause = new AclWhoClauseTlsSsf();
                    break;
                case TRANSPORT_SSF:
                    clause = new AclWhoClauseTransportSsf();
                    break;
            }

            // Also setting access level and control
            setAccessLevel();
            setControl();
        }
        else
        {
            clause = null;
        }
    }


    // ── Setting the Access Level From the Current Combo Selection ────────────
    // Dispatches over the selected access level and applies the corresponding
    // AclAccessLevel to the current clause.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Applies the current access level combo selection to the current clause.
     * Named levels create a simple {@link AclAccessLevel}; the Custom entry
     * applies the value from the last custom-access-level dialog run.
     */
    private void setAccessLevel()
    {
        if ( currentAccessLevelSelection instanceof AclAccessLevelLevelEnum )
        {
            AclAccessLevelLevelEnum accessLevelSelection = ( AclAccessLevelLevelEnum ) currentAccessLevelSelection;

            // Creating the access level associated with the selection
            switch ( accessLevelSelection )
            {
                case MANAGE:
                    setAccessLevel( createBasicAccessLevel( AclAccessLevelLevelEnum.MANAGE ) );
                    break;
                case WRITE:
                    setAccessLevel( createBasicAccessLevel( AclAccessLevelLevelEnum.WRITE ) );
                    break;
                case READ:
                    setAccessLevel( createBasicAccessLevel( AclAccessLevelLevelEnum.READ ) );
                    break;
                case SEARCH:
                    setAccessLevel( createBasicAccessLevel( AclAccessLevelLevelEnum.SEARCH ) );
                    break;
                case COMPARE:
                    setAccessLevel( createBasicAccessLevel( AclAccessLevelLevelEnum.COMPARE ) );
                    break;
                case AUTH:
                    setAccessLevel( createBasicAccessLevel( AclAccessLevelLevelEnum.AUTH ) );
                    break;
                case DISCLOSE:
                    setAccessLevel( createBasicAccessLevel( AclAccessLevelLevelEnum.DISCLOSE ) );
                    break;
                case NONE:
                    setAccessLevel( createBasicAccessLevel( AclAccessLevelLevelEnum.NONE ) );
                    break;
            }
        }
        else if ( currentAccessLevelSelection instanceof AccessLevelComboViewerCustom )
        {
            setAccessLevel( currentCustomAccessLevel );
            currentCustomAccessLevel = null;
        }
        else
        {
            // Resetting access level
            setAccessLevel( null );
        }
    }


    // ── Applying an Access Level to the Clause ────────────────────────────────
    /**
     * Calls {@link AclWhoClause#setAccessLevel(AclAccessLevel)} on the current clause.
     * No-op when {@link #clause} is {@code null}.
     *
     * @param accessLevel  The access level to apply; may be {@code null} to clear it.
     */
    private void setAccessLevel( AclAccessLevel accessLevel )
    {
        if ( clause != null )
        {
            clause.setAccessLevel( accessLevel );
        }
    }


    // ── Setting the Control From the Current Combo Selection ─────────────────
    /**
     * Applies the current control combo selection to the current clause.
     */
    private void setControl()
    {
        if ( currentControlSelection instanceof AclControlEnum )
        {
            AclControlEnum controlSelection = ( AclControlEnum ) currentControlSelection;

            // Creating the clause associated with the selection
            switch ( controlSelection )
            {
                case STOP:
                    setControl( AclControlEnum.STOP );
                    break;
                case CONTINUE:
                    setControl( AclControlEnum.CONTINUE );
                    break;
                case BREAK:
                    setControl( AclControlEnum.BREAK );
                    break;
            }
        }
        else
        {
            // Resetting control
            setControl( null );
        }
    }


    // ── Applying a Control to the Clause ─────────────────────────────────────
    /**
     * Calls {@link AclWhoClause#setControl(AclControlEnum)} on the current clause.
     * No-op when {@link #clause} is {@code null}.
     *
     * @param control  The control to apply; may be {@code null} to clear it.
     */
    private void setControl( AclControlEnum control )
    {
        if ( clause != null )
        {
            clause.setControl( control );
        }
    }


    // ── Returning the Current Who Clause ─────────────────────────────────────
    /**
     * Returns the current {@link AclWhoClause} for this row.
     *
     * @return  The current clause; may be {@code null} when the placeholder is selected.
     */
    public AclWhoClause getClause()
    {
        return clause;
    }


    // ── Returning the Row Index ───────────────────────────────────────────────
    /**
     * Returns the zero-based position of this row in the who-clause list.
     *
     * @return  The row index.
     */
    public int getIndex()
    {
        return index;
    }


    // ── Dispatching Toolbar Button Events ────────────────────────────────────
    // When a toolbar button is pressed Tarkin calls the corresponding method
    // on the builder widget, which modifies the model and refreshes the rows.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches toolbar button selection events to the builder widget.
     *
     * {@inheritDoc}
     */
    public void widgetSelected( SelectionEvent e )
    {
        Object source = e.getSource();

        if ( source == addButton )
        {
            builderWidget.addNewClause( this );
        }
        else if ( source == deleteButton )
        {
            builderWidget.deleteClause( this );
        }
        else if ( source == moveUpButton )
        {
            builderWidget.moveUpClause( this );
        }
        else if ( source == moveDownButton )
        {
            builderWidget.moveDownClause( this );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void widgetDefaultSelected( SelectionEvent e )
    {
        // Nothing to do
    }


    // ── Accessor: Add Button ──────────────────────────────────────────────────
    /**
     * Returns the Add toolbar button.
     *
     * @return  The Add {@link ToolItem}.
     */
    public ToolItem getAddButton()
    {
        return addButton;
    }


    // ── Accessor: Delete Button ───────────────────────────────────────────────
    /**
     * Returns the Delete toolbar button.
     *
     * @return  The Delete {@link ToolItem}.
     */
    public ToolItem getDeleteButton()
    {
        return deleteButton;
    }


    // ── Accessor: Move Up Button ──────────────────────────────────────────────
    /**
     * Returns the Move Up toolbar button.
     *
     * @return  The Move Up {@link ToolItem}.
     */
    public ToolItem getMoveUpButton()
    {
        return moveUpButton;
    }


    // ── Accessor: Move Down Button ────────────────────────────────────────────
    /**
     * Returns the Move Down toolbar button.
     *
     * @return  The Move Down {@link ToolItem}.
     */
    public ToolItem getMoveDownButton()
    {
        return moveDownButton;
    }


    // ── Disposing All Created SWT Widgets ─────────────────────────────────────
    // Tarkin powers down this row's widgets and releases all SWT resources.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the base composite, the clause combo, the toolbar, and the
     * configuration sub-composite (if any). Called by the builder widget when
     * rebuilding the rows.
     */
    public void dispose()
    {
        // Composite
        if ( ( composite != null ) && ( !composite.isDisposed() ) )
        {
            composite.dispose();
        }

        // Combo Viewer
        if ( ( clauseComboViewer != null ) && ( clauseComboViewer.getCombo() != null )
            && ( !clauseComboViewer.getCombo().isDisposed() ) )
        {
            clauseComboViewer.getCombo().dispose();
        }

        // Toolbar
        if ( ( toolbar != null ) && ( !toolbar.isDisposed() ) )
        {
            toolbar.dispose();
        }

        // Configuration composite
        if ( ( configurationComposite != null ) && ( !configurationComposite.isDisposed() ) )
        {
            configurationComposite.dispose();
        }
    }

    /**
     * A private placeholder object for the first row of the clause combo viewer.
     * Displays "&lt; Clause &gt;" as a prompt.
     */
    class ClauseComboViewerName
    {
    }

    /**
     * A private placeholder object for the first row of the access level combo viewer.
     * Displays "&lt; Access Level &gt;" as a prompt.
     */
    private class AccessLevelComboViewerName
    {
    }

    /**
     * A private placeholder object for the last row of the access level combo viewer.
     * Displays "Custom..." (or "Custom... [value]" when a custom level is active).
     */
    private class AccessLevelComboViewerCustom
    {
    }

    /**
     * A private placeholder object for the first row of the control combo viewer.
     * Displays "&lt; Control &gt;" as a prompt.
     */
    private class ControlComboViewerName
    {
    }
}
